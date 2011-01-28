/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.web;

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.richfaces.component.html.HtmlExtendedDataTable;
import org.richfaces.model.selection.Selection;
import org.richfaces.model.selection.SimpleSelection;

import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskParameter;
/**
 * JSF managed bean for view and edit Tasks, template: todo/task.xhtml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskBean {

    private static final Logger LOG = Logger.getLogger(TaskBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.TaskMessages";

    private EditBean editBean;
    
    List<ITask> taskList;
    
    ITask selectedTask;
    
    private String outcomeId;
    
    private boolean showRead = true;
    
    private boolean showUnread = true;
    
    private HtmlExtendedDataTable table;
    
    private Selection selection = new SimpleSelection();
    
    /**
     * @return
     */
    public List<ITask> loadTasks() {  
        ITaskParameter parameter = new TaskParameter();
        parameter.setRead(getShowRead());
        parameter.setUnread(getShowUnread());    
        return taskList = getTaskService().getTaskList(parameter);
    }
    
    public void openTask() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("openTask() called ...");
        }
        try {
            Iterator<Object> iterator = getSelection().getKeys();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                table.setRowKey(key);
                if (table.isRowAvailable()) {
                    setSelectedTask( (ITask) table.getRowData());
                }
                getTaskService().markAsRead(getSelectedTask().getId());
                getSelectedTask().setIsRead(true);
                getSelectedTask().setStyle(ITask.STYLE_READ);
                
                getEditBean().setUuid(getSelectedTask().getUuid());
                getEditBean().setTitle(getSelectedTask().getControlTitle());
                getEditBean().setTypeId(getSelectedTask().getType());
                setOutcomeId(null);
                getEditBean().init();
            }
        } catch (Throwable t) {
            LOG.error("Error while opening task", t);
        }
    }
    
    public void completeTask() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("completeTask() called ...");
        }
        if(getSelectedTask()!=null) {
            getTaskService().completeTask(getSelectedTask().getId(),getOutcomeId());
            getTaskList().remove(getSelectedTask());
            setSelectedTask(null);
            setSelection(null);
            getEditBean().clear();
            Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "taskCompleted"));  
        }
    }
    
    public EditBean getEditBean() {
        return editBean;
    }

    public void setEditBean(EditBean editBean) {
        this.editBean = editBean;
    }

    public List<ITask> getTaskList() {
        if(this.taskList==null) {
            this.taskList = loadTasks();
        }
        return taskList;
    }

    public void setTaskList(List<ITask> taskList) {     
        this.taskList = taskList;
    }

    public ITask getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(ITask selectedTask) {
        this.selectedTask = selectedTask;
    }

    public String getOutcomeId() {
        return outcomeId;
    }

    public void setOutcomeId(String outcomeId) {
        this.outcomeId = outcomeId;
    }

    public boolean getShowRead() {
        return showRead;
    }

    public void setShowRead(boolean showRead) {
        this.showRead = showRead;
    }

    public boolean getShowUnread() {
        return showUnread;
    }

    public void setShowUnread(boolean showUnread) {
        this.showUnread = showUnread;
    }

    public HtmlExtendedDataTable getTable() {
        return table;
    }

    public void setTable(HtmlExtendedDataTable table) {
        this.table = table;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
    
    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
    
    public void english() {
        Util.english();
    }
    
    public void german() {
        Util.german();
    }
    
}
