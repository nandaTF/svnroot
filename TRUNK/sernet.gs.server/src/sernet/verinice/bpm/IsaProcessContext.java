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

import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class IsaProcessContext {

    String uuidAudit;
    
    ControlGroup controlGroup;
    
    SamtTopic samtTopic;

    int numberOfProcesses;
    
    public void increaseProcessNumber() {
        numberOfProcesses++;
    }
    
    public IsaProcessContext() {
        super();
    }

    public String getUuidAudit() {
        return uuidAudit;
    }

    public void setUuidAudit(String uuidAudit) {
        this.uuidAudit = uuidAudit;
    }

    public ControlGroup getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(ControlGroup controlGroup) {
        this.controlGroup = controlGroup;
    }

    public SamtTopic getSamtTopic() {
        return samtTopic;
    }

    public void setSamtTopic(SamtTopic samtTopic) {
        this.samtTopic = samtTopic;
    }

    public int getNumberOfProcesses() {
        return numberOfProcesses;
    }

    public void setNumberOfProcesses(int numberOfProcesses) {
        this.numberOfProcesses = numberOfProcesses;
    }
}
