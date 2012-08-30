/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dialogs.SanityCheckDialog;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinUmsetzungTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IBSIStrukturElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kGroupTransfer;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 *
 */
public class BSIModelViewDropListener extends ViewerDropAdapter implements RightEnabledUserInteraction, DropPerformer {

    private TreeViewer viewer;

    private Logger LOG = Logger.getLogger(BSIModelViewDropListener.class);

    private boolean isActive = false;

    private Object target = null;

    public BSIModelViewDropListener(TreeViewer viewer){
        super(viewer);
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object data) {
        Object toDrop = data;
        if(data == null){
            if(LOG.isDebugEnabled()){
                LOG.debug("data is null - setting to selected Object");
            }
            
            data = ((IStructuredSelection)this.getViewer().getSelection()).toArray();
        }
        List items = DNDHelper.arrayToList(data);
        Object firstOne = items.get(0);
        if (toDrop != null && (toDrop instanceof Object[])) {
            Object[] o = (Object[])toDrop;
            if(o.length > 0){
                firstOne = o[0];
            }
        } else if(toDrop != null && (toDrop instanceof Object)){
            firstOne = toDrop;
        }

        if(isActive()) {
            if(firstOne instanceof Baustein && target.getClass().getPackage().getName().contains("model.bsi")){
                ArrayList<Baustein> list = new ArrayList<Baustein>(0);
                for(Object object : items){
                    if(object instanceof Baustein){
                        list.add((Baustein)object);
                    }
                }
                return dropBaustein((CnATreeElement) target, viewer, list.toArray(new Baustein[list.size()]));
            } else if(firstOne != null && (firstOne instanceof IBSIStrukturElement || firstOne instanceof BausteinUmsetzung || firstOne instanceof IISO27kElement)) {
                CnATreeElement element = (CnATreeElement) target;
                LinkDropper dropper = new LinkDropper();
                ArrayList<CnATreeElement> list = new ArrayList<CnATreeElement>();
                for(Object object : items){
                    if(object instanceof CnATreeElement){
                        list.add((CnATreeElement)object);
                    }
                }
                return dropper.dropLink(list, element);
            }
        }
        return false;
    }

    @Override
    public void drop(DropTargetEvent event){
        LOG.debug("entered drop(DropTargetEvent event)");
        target = (CnATreeElement) determineTarget(event);
        super.drop(event);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateDrop, target: " + ((CnATreeElement)target).getTitle());
        }

        if(!checkRights())
            return false;
        if (target == null){
            return isActive=false;
        } else {
            this.target = target;
        }
        if (!(target instanceof CnATreeElement))
            return isActive=false;
        
        if (target instanceof IBSIStrukturKategorie)
            return isActive=false;

        if(target instanceof BausteinUmsetzung && !(IBSIStrukturElementTransfer.getInstance().isSupportedType(transferType))){
            return isActive = false;
        }
        else if(target instanceof IBSIStrukturElement && isSupportedData(transferType)){
            return isActive = true;
        }
        if(target instanceof IISO27kGroup && BausteinElementTransfer.getInstance().isSupportedType(transferType)){
            return isActive = false;
        }
        return isActive=true;
    }

//    /* (non-Javadoc)
//     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
//     */
//    @Override
//    public boolean validateDrop(Object target, int operation, TransferData transferType) {
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("validateDrop, target: " + ((CnATreeElement)target).getTitle());
//        }
//
//        if(!checkRights())
//            return false;
//        if (target == null){
//            return isActive=false;
//        } else {
//            this.target = target;
//        }
//        if (!(target instanceof CnATreeElement))
//            return isActive=false;
//
//        if (target instanceof IBSIStrukturKategorie)
//            return isActive=false;
//
//        if(target instanceof BausteinUmsetzung && !(IBSIStrukturElementTransfer.getInstance().isSupportedType(transferType))){
//            return isActive = false;
//        }
//        else if(target instanceof IBSIStrukturElement && isSupportedData(transferType)){
//            return isActive = true;
//        } else if(target instanceof IISO27kElement && isSupportedData(transferType)){
//            return isActive = true;
//        }
//        Object data = gfe
//        //-------------------------------------------------------------------------
//        if(firstOne instanceof BausteinUmsetzung){
//            if(target instanceof BausteinUmsetzung){
//                if(!((BausteinUmsetzung)firstOne).getKapitel().equals(((BausteinUmsetzung)target).getKapitel())){
//                    LOG.debug("p1");
//                    isActive = false;
//                } else if(!(target instanceof IBSIStrukturElement)){
//                    LOG.debug("p2");
//                    isActive = false;
//                }
//            }
//        }
//
//        if (firstOne instanceof IBSIStrukturElement || firstOne instanceof IISO27kElement) {
//            for (Object item : o) {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("validateDrop, draged item: " + item );
//                }
//                if (target.equals(item)){
//                    LOG.debug("p3");
//                    isActive=false;
//                }
//                else if (!(item instanceof IBSIStrukturElement || item instanceof IISO27kElement)){
//                    LOG.debug("p4");
//                    isActive=false;
//                } else if(!(target instanceof CnATreeElement)){
//                    LOG.debug("p5");
//                    isActive=false;
//                }
//            }
//        } else if(!(target instanceof CnATreeElement)){
//            LOG.debug("p6");
//            isActive = false;
//        } else {
//            for (Object object : o) {
//                CnATreeElement cont = (CnATreeElement) target;
//                if (!cont.canContain(object)){
//                    LOG.debug("p7");
//                    isActive=false;
//                }
//            }
//        }
//        //-------------------------------------------------------------------------
//        
//        return isActive=true;
//    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BSIDND;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // nothing
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    public boolean isActive() {
        return isActive;
    }

    private boolean dropBaustein(final CnATreeElement target, Viewer viewer,final Baustein[] bausteine) {
        if (!CnAElementHome.getInstance().isNewChildAllowed(target))
            return false;

        Check: for (Baustein baustein : bausteine) {
            int targetSchicht = 0;
            if (target instanceof IBSIStrukturElement)
                targetSchicht = ((IBSIStrukturElement) target).getSchicht();

            if (baustein.getSchicht() != targetSchicht) {
                if (!SanityCheckDialog.checkLayer(viewer.getControl().getShell(), baustein.getSchicht(),
                        targetSchicht))
                    return false;
                else
                    break Check; // user say he knows what he's doing, stop
                // checking.
            }

        }

        try {
            Job dropJob = new Job(Messages.getString("BSIModelViewDropListener.3")) { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    Activator.inheritVeriniceContextState();

                    try {
                        createBausteinUmsetzung(bausteine, target);
                    } catch (Exception e) {
                        Logger.getLogger(this.getClass()).error("Drop failed", e); //$NON-NLS-1$
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };
            dropJob.setUser(true);
            dropJob.setSystem(false);
            dropJob.schedule();
        } catch (Exception e) {
            LOG.error(Messages.getString("BSIModelViewDropListener.5"), e); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private void createBausteinUmsetzung(Baustein[] toDrop, CnATreeElement target) throws Exception {
        CnATreeElement saveNew = null;
        for (Baustein baustein : toDrop) {
            saveNew = CnAElementFactory.getInstance().saveNew(target,
                    BausteinUmsetzung.TYPE_ID,
                    new BuildInput<Baustein>(baustein),
                    false /* do not notify single elements*/);
        }
        // notifying for the last element is sufficient to update all views:
        CnAElementFactory.getLoadedModel().childAdded(target,saveNew);
    }

    @Override
    public void dropAccept(DropTargetEvent event){
    }

    private boolean isSupportedData(TransferData transferType){
        return (BausteinElementTransfer.getInstance().isSupportedType(transferType)
                || IBSIStrukturElementTransfer.getInstance().isSupportedType(transferType)
                || BausteinUmsetzungTransfer.getInstance().isSupportedType(transferType)
                || ISO27kElementTransfer.getInstance().isSupportedType(transferType)
                || ISO27kGroupTransfer.getInstance().isSupportedType(transferType));
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        return performDrop(data);
    }

}
