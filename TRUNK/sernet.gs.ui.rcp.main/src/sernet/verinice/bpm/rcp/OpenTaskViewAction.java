package sernet.verinice.bpm.rcp;

import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ActionRightIDs;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.OpenViewAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;

public class OpenTaskViewAction extends OpenViewAction {

    Boolean isActive = null;
    
    public OpenTaskViewAction(IWorkbenchWindow window) {
        super(window, "Tasks", TaskView.ID, ImageCache.VIEW_TASK);
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            public void closed(BSIModel model) {                
            }
            public void loaded(BSIModel model) {             
            }
            public void loaded(ISO27KModel model) {
                setEnabled(isActive());               
            }
        });
        setRightID(ActionRightIDs.TASKVIEW);
        setEnabled(checkRights());
    }
    
    private boolean isActive() {
        if(isActive==null) {
            isActive = ServiceFactory.lookupProcessService().isActive();
        }
        return isActive.booleanValue();
    }

}
