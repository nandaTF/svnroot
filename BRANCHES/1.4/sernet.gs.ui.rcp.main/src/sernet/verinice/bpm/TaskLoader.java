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
package sernet.verinice.bpm;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.bpm.rcp.TaskChangeRegistry;
import sernet.verinice.interfaces.bpm.ITaskListener;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskParameter;

/**
 * Loads new tasks using the ITaskService.
 * TaskLoader is for use in a Spring/Quartz cron job.
 * See: sernet/springclient/veriniceclient.xml
 * 
 * Method load is called periodically and loads 
 * newly created tasks after last call.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class TaskLoader {

    private static final Logger LOG = Logger.getLogger(TaskLoader.class);
    
    private ITaskService taskService;
    
    private Date lastChecked = null;
    
    /**
     * Is called periodically by Spring/Quartz cron job 
     * and loads newly created tasks after last call.
     */
    public void load() {
        List<ITask> taskList = Collections.emptyList();
        Date now = new Date(System.currentTimeMillis());
        if(lastChecked!=null) {
            ITaskParameter parameter = new TaskParameter();
            parameter.setSince(lastChecked);
            taskList = getTaskService().getTaskList(parameter);
            TaskChangeRegistry.tasksAdded(taskList);       
        }
        lastChecked = now;
    }
    
    public ITaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }
    
}
