/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.commands;

import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Command that wants to notify other clients of changes.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IChangeLoggingCommand {

	/**
	 * The session id of the client making the changes.
	 * @return
	 */
	public String getStationId();
	
	/**
	 * @return the modifiec element
	 */
	public List<CnATreeElement> getChangedElements();
	
	/**
	 * @return
	 */
	public int getChangeType();
}
