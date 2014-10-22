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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.gs.ui.rcp.main.bsi.dialogs.CnATreeElementSelectionDialog;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.bpm.PersonTypeSelectDialog;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.service.commands.LoadConfiguration;

/**
 * Sets the assignee of one or more selected tasks in {@link TaskView}.
 * This handler is configured in plugin.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AssignHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(AssignHandler.class);
    
    private Shell shell;
    
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
            if (selection != null && selection instanceof IStructuredSelection) {
                Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();            
                String type = selectElementType();                
                CnATreeElementSelectionDialog dialog = new CnATreeElementSelectionDialog(shell, type, null);
                dialog.setShowScopeCheckbox(false);
                if (dialog.open() == Window.OK) {
                    Set<String> taskIdSet = getSelectedTasks(selection);
                    List<CnATreeElement> userList = dialog.getSelectedElements();
                    if(userList.size()==1) {
                        CnATreeElement element = userList.get(0);                
                        LoadConfiguration command = new LoadConfiguration(element);
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        Configuration configuration = command.getConfiguration();
                        if(configuration!=null) {
                            getTaskService().setAssignee(taskIdSet, configuration.getUser());
                            getTaskService().setAssigneeVar(taskIdSet, configuration.getUser());
                            TaskChangeRegistry.tasksAdded();  
                        } else {
                            MessageDialog.openWarning(shell, "Warning", "Can not set assign person. No account data is set.");
                        }                         
                    }
                        
                }
                    
            }
        } catch(Exception e) {
            LOG.error("Error while assigning user to task.", e);
        }
        return null;
    }

    private Set<String> getSelectedTasks(ISelection selection) {
        Set<String> taskIdSet = new HashSet<String>();
        for (Iterator iterator = ((IStructuredSelection)selection).iterator(); iterator.hasNext();) {
            ITask task = (ITask) iterator.next();
            taskIdSet.add(task.getId());
        }
        return taskIdSet;
    }
    
    private String selectElementType() {
        final PersonTypeSelectDialog typeDialog = new PersonTypeSelectDialog(shell);
        if (typeDialog.open() == Window.OK) { 
            return typeDialog.getElementType();
        } else {
            throw new CompletionAbortedException("Canceled by user.");
        }
    }
    
    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TASKCHANGEASSIGNEE;
    }

}
