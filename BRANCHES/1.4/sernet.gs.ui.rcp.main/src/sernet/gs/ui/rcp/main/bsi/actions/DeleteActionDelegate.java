/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.util.Log;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfigurationByUser;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITypedElement;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Delete items on user request.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
@SuppressWarnings("restriction")
public class DeleteActionDelegate implements IObjectActionDelegate {

    private static final Logger LOG = Logger.getLogger(DeleteActionDelegate.class);
    
    private IWorkbenchPart targetPart;
    
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @SuppressWarnings("unchecked")
    public void run(IAction action) {
        try {
            Activator.inheritVeriniceContextState();
    
            final IStructuredSelection selection = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection());
    
            if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), 
                    Messages.DeleteActionDelegate_0, 
                    NLS.bind(Messages.DeleteActionDelegate_1, selection.size()))) {
                return;
            }
    
            // ask twice if IT verbund
            boolean goahead = true;
            final List<CnATreeElement> deleteList = createList(selection.toList());
            Iterator iterator = deleteList.iterator();
            Object object;
            while (iterator.hasNext()) {
                object = iterator.next();
                if (object instanceof ITVerbund || object instanceof IISO27kRoot) {
                    if (!goahead) {
                        return;
                    }
    
                    String title = Messages.DeleteActionDelegate_3;
                    String message = Messages.DeleteActionDelegate_4;
                    if (object instanceof ITVerbund) {
                        title = Messages.DeleteActionDelegate_5;
                        message = NLS.bind(Messages.DeleteActionDelegate_6, ((ITVerbund) object).getTitle());
                    }
                    if (object instanceof IISO27kRoot) {
                        title = Messages.DeleteActionDelegate_8;
                        message = NLS.bind(Messages.DeleteActionDelegate_9, ((IISO27kRoot) object).getTitle());
                    }
    
                    if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), title, message)) {
                        goahead = false;
                        return;
                    }
                }
            }
        
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        Activator.inheritVeriniceContextState();
                        monitor.beginTask(Messages.DeleteActionDelegate_11, selection.size());
    
                        for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                            Object sel = iter.next();
    
                            if (sel instanceof IBSIStrukturElement 
                                    || sel instanceof BausteinUmsetzung 
                                    || sel instanceof FinishedRiskAnalysis 
                                    || sel instanceof GefaehrdungsUmsetzung 
                                    || sel instanceof ITVerbund 
                                    || sel instanceof IISO27kRoot 
                                    || sel instanceof IISO27kElement 
                                    || sel instanceof ImportIsoGroup) {
    
                                // do not delete last ITVerbund:                          
                                if (sel instanceof ITVerbund && CnAElementHome.getInstance().getItverbuende().size() < 2) {
                                    ExceptionUtil.log(new Exception(Messages.DeleteActionDelegate_12), Messages.DeleteActionDelegate_13);
                                    return;
                                }
                                                  
                                final CnATreeElement el = (CnATreeElement) sel;
                                el.setParent(loadChildren(el.getParent())); // inserted because of strange lazy exceptions on only some elements
                                monitor.setTaskName(NLS.bind(Messages.DeleteActionDelegate_14, el.getTitle()));
                                
                                if(el instanceof Organization){
                                    for(CnATreeElement child : el.getChildren()){
                                        if((child instanceof PersonGroup) || (child instanceof AuditGroup)){
                                            if(hasAccountChildren(child)){
                                                Display.getDefault().asyncExec(new Runnable() {
                                                    
                                                    @Override
                                                    public void run() {
                                                        if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "TitelOvicz", "Das zu löschende Element beinhaltet Personen die über einen Benutzeraccount verfügen. Wollen Sie diese wirklich alle löschen?\n(die Personen können sich dann nicht mehr am System anmelden)")){
                                                            try {
                                                                deletePersonIsos(el);
                                                                deleteElement(el);
                                                                monitor.worked(1);                           
                                                            } catch (Exception e) {
                                                                ExceptionUtil.log(e, e.getLocalizedMessage());
                                                            }
                                                        } else {
                                                            return;
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                } else if(el instanceof PersonIso && getConfiguration((PersonIso)el) != null){
                                    preparePersonIsoForDelete((PersonIso)el);
                                    try {
                                        deleteElement(el);
                                        monitor.worked(1);                           
                                    } catch (Exception e) {
                                        ExceptionUtil.log(e, e.getLocalizedMessage());
                                    }
                                    
                                } else {
                                    try {
                                        deleteElement(el);
                                        monitor.worked(1);                           
                                    } catch (Exception e) {
                                        ExceptionUtil.log(e, e.getLocalizedMessage());
                                    }
                                }
                            }
                        }
    
                        // notify all listeners:
                        CnATreeElement child = (CnATreeElement) deleteList.iterator().next();
                        CnAElementFactory.getModel(child).databaseChildRemoved(child);
                    } catch (Exception e) {
                        LOG.error("Error while deleting element.", e);
                        ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            LOG.error("Error while deleting element.", e);
            ExceptionUtil.log(e.getCause(), Messages.DeleteActionDelegate_16);
        } catch (InterruptedException e) {
            LOG.error("Error while deleting element.", e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        } catch (Throwable e) {
            LOG.error("Error while deleting element(s).", e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        }
    }
    
    private void deleteElement(CnATreeElement element){
        try {
            if(element instanceof CnATreeElement){
                element.getParent().removeChild(element);
                CnAElementHome.getInstance().remove(element);
            } 
        } catch (Exception e) {
            ExceptionUtil.log(e, e.getLocalizedMessage());
        }
    }
    
    // deletes configuration object, if there is one related to the parameter personIso
    private void preparePersonIsoForDelete(PersonIso personIso){
        Configuration c = getConfiguration(personIso);
        RemoveConfiguration command = new RemoveConfiguration(c);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            Log.error(Messages.DeleteActionDelegate_15, e);
        }
    }
    
    private void deletePersonIsos(CnATreeElement parent){
        if(!parent.isChildrenLoaded()){
            parent = loadChildren(parent);
        }
        CnATreeElement oneChild = null;
        for(CnATreeElement child : parent.getChildren()){
            // if element is PersonIso, delete configuration if existant
            if(child instanceof PersonIso){
                try {
                    if(getConfiguration((PersonIso)child) != null){
                        preparePersonIsoForDelete((PersonIso)child);
                    }
                    RemoveElement command = new RemoveElement(child);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    if(oneChild == null)
                        oneChild = child;
                } catch (Exception e) {
                    LOG.error(Messages.DeleteActionDelegate_15, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                }
            // if element is can contain PersonIso Objects, recall method 
            } else if(child instanceof PersonGroup || child instanceof AuditGroup || child instanceof Audit){
                deletePersonIsos(child);
            }
        }
        // refresh workaround, TODO
        if(oneChild != null){
            CnAElementFactory.getModel(parent).childRemoved(parent, oneChild);
        }
    }
    
    private boolean hasAccountChildren (CnATreeElement parent){
        boolean hasAccountChildren = false;
        if(!parent.isChildrenLoaded()){
            parent = loadChildren(parent);
        }
        for(CnATreeElement child : parent.getChildren()){
            if(child instanceof PersonIso){
               if(getConfiguration((PersonIso)child) != null){
                   return true;
               }
               if(hasAccountChildren){
                   return true;
               }
            } else if(child instanceof PersonGroup || child instanceof Audit){
                return hasAccountChildren(child);
            }
        }
        return hasAccountChildren;    
    }
    
    private Configuration getConfiguration(PersonIso personIso){
        LoadConfigurationByUser command = new LoadConfigurationByUser(personIso);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_16);
        }
        if(command.getConfiguration() != null){
            return command.getConfiguration();
        }
        return null;
    }
    
    protected List<CnATreeElement> createList(List elementList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;
        for (Object sel : elementList) {
            if (sel instanceof IBSIStrukturElement 
                    || sel instanceof BausteinUmsetzung 
                    || sel instanceof FinishedRiskAnalysis 
                    || sel instanceof GefaehrdungsUmsetzung 
                    || sel instanceof ITVerbund 
                    || sel instanceof IISO27kRoot 
                    || sel instanceof IISO27kElement 
                    || sel instanceof ImportIsoGroup) {
                createList((CnATreeElement) sel,tempList,insertList, depth, removed);
            }
        }
        return insertList;
    }

    private void createList(CnATreeElement element, List<CnATreeElement> tempList, List<CnATreeElement> insertList, int depth, int removed) {
        if(!tempList.contains(element)) {
            tempList.add(element);
            if(depth==0) {
                insertList.add(element);
            }
            if(element instanceof IISO27kGroup && element.getChildren()!=null) {
                depth++;
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createList(child,tempList,insertList,depth,removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        boolean allowed = ((RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE)).isEnabled(ActionRightIDs.DELETEITEM);
        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof CnATreeElement) {
        	CnATreeElement element = (CnATreeElement) sel;
            boolean b = CnAElementHome.getInstance().isDeleteAllowed(element);

            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(b & allowed);
            }
        }
    }

    private CnATreeElement loadChildren(CnATreeElement element){
        if(element.isChildrenLoaded())
            return element;
        
        LoadChildrenForExpansion command = new LoadChildrenForExpansion(element);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            return command.getElementWithChildren();
        } catch (CommandException e) {
            LOG.error("Error while deleting element(s).", e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        }
        return null;
    }

}
