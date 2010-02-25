/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah@sernet.de>
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
 *     Anne Hanekop <ah@sernet.de> 	- initial API and implementation
 *     ak@sernet.de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;

/**
 * Interface must be implemented by elements, which can be added to
 * the TreeViewer containing Gefaehrdungen and Massnahmen.
 * 
 * @author ahanekop@sernet.de
 */
public interface IGefaehrdungsBaumElement {
	
	/**
	 * Returns the description of the element.
	 * 
	 * @return the description of the element
	 */
	public String getDescription();

	/**
	 * Returns the children of the element.
	 *  
	 * @return the list of children of the element
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren();

	/**
	 * Returns the parent element.
	 * 
	 * @return the parent element
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent();

	/**
	 * Returns the title of the element.
	 * 
	 * @return the title of the element
	 */
	public String getText();
	
}
