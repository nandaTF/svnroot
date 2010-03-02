/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;

/**
 * 
 * @author koderman@sernet.de
 *
 */
public class ObjektLebenszyklusPropertyFilter extends StringPropertyFilter {

	public ObjektLebenszyklusPropertyFilter(StructuredViewer viewer) {
		super(viewer, "");
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof CnATreeElement)
				|| element instanceof ITVerbund
				|| !(element instanceof IBSIStrukturElement)
				|| element instanceof IBSIStrukturKategorie)
			return true;
		
		
		
		Entity entity = ((CnATreeElement)element).getEntity();
		String propertyTypeId = ((CnATreeElement)element).getTypeId() + "_status";
		String value = entity.getSimpleValue(propertyTypeId);
		Matcher matcher = regex.matcher(value);
		if (matcher.find())
			return true;
		return false;
	}

}
