/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.gs.ui.rcp.main.service.crudcommands.PrepareObjectWithAccountDataForDeletion;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.RightsEnabledHandler;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DeleteHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(DeleteHandler.class);
    
    protected static final String DEFAULT_ERR_MSG = "Error while deleting element.";
    
    protected IWorkbenchPart targetPart;
    
    
    public DeleteHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        changeSelection(HandlerUtil.getCurrentSelection(event));
        try {
            Activator.inheritVeriniceContextState();
            final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
           
            if (!MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), Messages.DeleteActionDelegate_0, NLS.bind(Messages.DeleteActionDelegate_1, selection.size()))) {
                return null;
            }

            final List<CnATreeElement> deleteList = createList(selection.toList());       

            if (!deleteList.isEmpty() && deleteList.get(0) instanceof IISO27kElement) {
                doDeleteIso(deleteList);
            } else {
                doDelete(deleteList);
            }
        } catch (InvocationTargetException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e.getCause(), Messages.DeleteActionDelegate_16);
        } catch (InterruptedException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        } catch (Exception e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        }
        return null;
    }
    
    
    protected void doDelete(final List<CnATreeElement> deleteList) throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Object sel = null;
                try {
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.DeleteActionDelegate_14, IProgressMonitor.UNKNOWN);
                    for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                        sel = iter.next();

                        // do not delete last ITVerbund:
                        if (sel instanceof ITVerbund && CnAElementHome.getInstance().getItverbuende().size() < 2) {
                            ExceptionUtil.log(new Exception(Messages.DeleteActionDelegate_12), Messages.DeleteActionDelegate_13);
                            return;
                        }

                        CnATreeElement el = (CnATreeElement) sel;
                        removeElement(el);
                    }
                } catch (DataIntegrityViolationException dive) {
                    deleteElementWithAccountAsync((CnATreeElement) sel);
                } catch (Exception e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                } finally {
                    if(monitor!=null) {
                        monitor.done();
                    }
                }
            }             
        });
    }
    
    protected void doDeleteIso(final List<CnATreeElement> deleteList) throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                Object sel = null;
                try {
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.DeleteActionDelegate_11, deleteList.size());
                    for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                        sel = iter.next();

                        CnATreeElement el = (CnATreeElement) sel;
                        monitor.setTaskName(Messages.DeleteActionDelegate_14);

                        removeElement(el);
                        monitor.worked(1);
                    }
                } catch (DataIntegrityViolationException dive) {
                    deleteElementWithAccountAsync((CnATreeElement) sel);
                } catch (Exception e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                }
            }
        });
    }
    
    protected List<CnATreeElement> createList(List<CnATreeElement> elementList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;  
        if(elementList.size()>1) {
            for (CnATreeElement element : elementList) {
                createList(element, tempList, insertList, depth, removed);       
            }
        } else {
            // add last element
            insertList.add(elementList.get(0));
        }
        return insertList;
    }
    
    private void createList(CnATreeElement element, List<CnATreeElement> tempList, List<CnATreeElement> insertList, int depth, int removed) {
        if (!tempList.contains(element)) {
            tempList.add(element);
            if (depth == 0) {
                insertList.add(element);
            }
            if (element instanceof IISO27kGroup && element.getChildren() != null) {
                int newDepth = depth++;
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createList(child, tempList, insertList, newDepth, removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }
    
    protected void deleteElementWithAccountAsync(final CnATreeElement element) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteElementWithAccount(element);
                } catch (CommandException e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                } catch (DataIntegrityViolationException de) {
                    LOG.error(DEFAULT_ERR_MSG, de);
                } catch (Exception e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                }
            }
        });
    }
    
    protected void deleteElementWithAccount(final CnATreeElement element) throws CommandException {
        GenericCommand command = null;
        if (loadConfiguration(element)) {
            if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.DeleteActionDelegate_0, Messages.DeleteActionDelegate_18)) {
                command = new PrepareObjectWithAccountDataForDeletion(element);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
            } else {
                return;
            }                                    
        }
        removeElement(element);
    }
    
    protected void removeElement(CnATreeElement elementToRemove) throws CommandException {     
        CnAElementHome.getInstance().remove(elementToRemove);
        CnAElementFactory.getModel(elementToRemove).databaseChildRemoved(elementToRemove);
    }
    
    protected boolean loadConfiguration(CnATreeElement elmt) {
        String[] types = new String[] { Person.TYPE_ID, PersonIso.TYPE_ID };
        ICommandService service = ServiceFactory.lookupCommandService();
        for (String type : types) {
            try {
                LoadReportElements command = new LoadReportElements(type, elmt.getDbId());
                command = service.executeCommand(command);
                for (CnATreeElement person : command.getElements()) {
                    LoadConfiguration command2 = new LoadConfiguration(person);
                    command2 = service.executeCommand(command2);
                    if (command2.getConfiguration() != null) {
                        return true;
                    }
                }
            } catch (CommandException e) {
                LOG.error("Error determing existence of configuration objects", e);
            }
        }

        return false;
    }
    
    protected CnATreeElement loadChildren(CnATreeElement element) throws CommandException {
        LoadChildrenForExpansion command = new LoadChildrenForExpansion(element);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        element = command.getElementWithChildren();
        element.setChildrenLoaded(true);
        return element;
    }


    public void changeSelection(ISelection selection) {
        boolean allowed = checkRights();
        boolean isWriteAllowed = true;
        
        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (allowed && sel instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) sel;
            isWriteAllowed = CnAElementHome.getInstance().isDeleteAllowed(element);         
        }
        
        // Only change state when it is enabled, since we do not want to
        // trash the enablement settings of plugin.xml
        if (this.isEnabled()) {
            this.setEnabled(isWriteAllowed & allowed);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.DELETEITEM;
    }

}
