/*******************************************************************************
 * Copyright (c) 2012 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.LinkedList;
import java.util.List;

import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;


/**
 * The Konsolidator copys values from one object to another,
 * filling in values that the user has already entered. 
 * 
 * @author Julia Haas <jh[at]sernet[dot]de>
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class GSMKonsolidator {
   // public static final List<String> PROPERTY_TYPE_BLACKLIST = Arrays.asList(MassnahmenUmsetzung.getStufen());
    //private List<String> propertyTypeBlacklist;
	/**
	 * Copy values for all Massnahmen from one BausteinUmsetzung to another.
	 * 
	 * @param source
	 * @param target
	 * @return 
	 */
	public static List<CnATreeElement> konsolidiereMassnahmen(BausteinUmsetzung source,
			BausteinUmsetzung target) {
	
		List<CnATreeElement> changedElements = new LinkedList<CnATreeElement>();
		
		for (MassnahmenUmsetzung mn: target.getMassnahmenUmsetzungen()) {
			MassnahmenUmsetzung sourceMn = source.getMassnahmenUmsetzung(mn.getUrl());
			if (sourceMn != null) {
		//	    mn.getEntity().copyEntity(source, propertyTypeBlacklist);
				mn.getEntity().copyEntity(sourceMn.getEntity());
			    changedElements.add(mn);
			}
		}
		return changedElements;
	}
	

	
}
