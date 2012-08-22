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
package sernet.verinice.bpm;

import org.jbpm.api.task.Task;

import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * Default ITaskDescriptionHandler which reads description of task from
 * sernet/verinice/bpm/messages[_de].properties
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DefaultTaskDescriptionHandler implements ITaskDescriptionHandler {

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadDescription(org.jbpm.api.task.Task)
     */
    @Override
    public String loadDescription(Task task) {
        return Messages.getString(task.getName() + ITaskService.DESCRIPTION_SUFFIX);
    }

}
