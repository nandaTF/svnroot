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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class UpdateMultipleElements<T> extends GenericCommand {

	private List<T> elements;

	public UpdateMultipleElements(List<T> elements) {
		this.elements = elements;
	}
	
	public void execute() {
		ArrayList<T> mergedElements = new ArrayList<T>(elements.size());
		if (elements != null && elements.size()>0) {
			IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory()
				.getDAO(elements.get(0).getClass());
			for (T element : elements) {
				T mergedElement = dao.merge(element, true);
				mergedElements.add(mergedElement);
			}
		}
		elements = null;
	}

	

}
