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
package sernet.verinice.interfaces.bpm;

import java.util.List;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ITaskService {

    /**
     * Returns the task list for currently logged in user.
     * If no tasks exists or current user cannot be determined an empty list is returned.
     * 
     * @return the task list for current user
     */
    List<ITask> getTaskList();
    
    
    /**
     * Returns the task list for user with name username.
     * If no tasks exists an empty list is returned.
     * 
     * @param username a username
     * @return task list for an user
     */
    List<ITask> getTaskList(String username);
    
    void completeTask(String taskId);
}
