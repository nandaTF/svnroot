/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bpm.CompleteHandlerRegistry;
import sernet.verinice.bpm.ICompleteClientHandler;
import sernet.verinice.model.bpm.TaskInformation;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
final class CompleteTaskAction extends Action {

    private static final Logger LOG = Logger.getLogger(CompleteTaskAction.class);
    
    private final TaskView taskView;
    final String id = TaskView.class.getName() + ".complete";
    String outcomeId;

    public CompleteTaskAction(TaskView taskView, String outcomeId) {
        super();
        this.taskView = taskView;
        this.outcomeId = outcomeId;
        setId(id + "." + outcomeId);
    }

    @Override
    public void run() {
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        try {
            final StructuredSelection selection = (StructuredSelection) this.taskView.getViewer().getSelection();
            final int number = selection.size();       
                progressService.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Activator.inheritVeriniceContextState();
                        try {
                            for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                                Object sel = iterator.next();
                                if (sel instanceof TaskInformation) {
                                    completeTask((TaskInformation) sel, outcomeId);
                                }
                            }
                        } catch (CompletionAbortedException e) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Completion aborted: " + e.getMessage());
                            }
                            return;
                        }
                        CompleteTaskAction.this.taskView.showInformation("Information", number + " task(s) completed.");
                        CompleteTaskAction.this.taskView.loadTasks();
                    }
                });          
        } catch (Throwable t) {
            LOG.error("Error while completing tasks.", t);
            this.taskView.showError("Error", "Error while completing task.");
        }
    }
    
    protected void completeTask(TaskInformation task, String outcomeId) {
        String type = task.getType();
        ICompleteClientHandler handler = CompleteHandlerRegistry.getHandler(new StringBuilder(type).append(".").append(outcomeId).toString());
        Map<String, Object> parameter = null;
        if(handler!=null) {
            handler.setShell(this.taskView.getViewSite().getShell());
            parameter = handler.execute();          
        }
            
        if (outcomeId == null) {
            ServiceFactory.lookupTaskService().completeTask(task.getId());
        } else {
            ServiceFactory.lookupTaskService().completeTask(task.getId(), outcomeId, parameter);
        }
        taskView.removeTask(task);
    }
}