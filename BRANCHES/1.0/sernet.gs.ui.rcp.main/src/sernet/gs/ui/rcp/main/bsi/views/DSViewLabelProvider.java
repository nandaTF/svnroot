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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.IBaseLabelProvider;

import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class DSViewLabelProvider extends BSIModelViewLabelProvider implements
		IBaseLabelProvider {

	public DSViewLabelProvider(TreeViewerCache cache) {
		super(cache);
	}
	
	@Override
	public String getText(Object obj) {
		if (obj instanceof ITVerbund) {
			String title = super.getText(obj);
			return "Datenschutzkonzept: " + title;
		}
		else if (obj instanceof AnwendungenKategorie)
			return "Verfahren";
		
		// else return object title:
		return super.getText(obj);
	}

}
