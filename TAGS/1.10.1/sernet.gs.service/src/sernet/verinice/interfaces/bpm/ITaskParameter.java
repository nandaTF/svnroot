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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface ITaskParameter extends Serializable {
    
    String getUsername();
    
    void setUsername(String username);
    
    boolean getAllUser();
    
    void setAllUser(boolean allUser);
    
    Date getSince();
    
    void setSince(Date since);
    
    Date getDueDateFrom();
    
    void setDueDateFrom(Date dueDateFrom);
    
    Date getDueDateTo();
    
    void setDueDateTo(Date dueDateTo);
    
    Boolean getRead();
    
    void setRead(Boolean read);
    
    Boolean getUnread();
    
    void setUnread(Boolean unread);
    
    String getAuditUuid();
    
    void setAuditUuid(String uuid);
    
    List<String> getGroupIdList();
    
    void setGroupIdList(List<String> groupIdList);
    
    public String getProcessKey();

    public String getTaskId();

    public void setProcessKey(String processKey);

    public void setTaskId(String taskId);
    
    public void setBlacklist(Set<String> blacklist);
    
    public Set<String> getBlacklist();
}
