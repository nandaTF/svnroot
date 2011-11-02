/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;

import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.RightEnabledUserInteraction;

public class RightsEnabledAction extends Action implements RightEnabledUserInteraction {
    
    private String rightID = null;
    
    /* TODO: implement logic for right management on orgs */
    private boolean orgRelatedCheck = false;
    
    public RightsEnabledAction(String rightID){
        this.setRightID(rightID);
    }
    
    public RightsEnabledAction(){
    }
   

    public boolean checkRights(){
        /**
         * no right management should be used
         */
        if(getRightID() == null){
            return true; 
        }
        /**
         * id  set but empty, right not granted, action disabled
         */
        else if(getRightID().equals("")){
            return false;
        /**
        * right management enabled, check rights and return true if right enabled / false if not
        */
        } else {
            RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
            return service.containsAction(getRightID());
        }
    }
    
    public String getRightID() {
        return rightID;
    }

    /**
     * Overwrite/call this, to enable right-management for the action implementing this class
     * @param rightID
     */
    public void setRightID(String rightID) {
        this.rightID = rightID;
    }
    
    /**
     * is the action related to an organization or is it globally allowed / not allowed
     * @return
     */
    public boolean isOrgRelated() {
        return orgRelatedCheck;
    }

    public void setOrgRelated(boolean orgRelated) {
        this.orgRelatedCheck = orgRelated;
    }
    
    
}
