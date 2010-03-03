/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.proxy.HibernateProxy;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * This report prints out the inventory of all recorded
 * IT assets such as clients, applications, rooms etc.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class StrukturanalyseReport extends BsiReport
	implements IBSIReport {

	// this is necessary because hibernate returns proxy objects that will not implement the marker interface IBSIStrukturelement
	// TODO akoderman change marker interface to object composition: add adaptable interface for strukturelements to model classes
	public static final String[] strukturElementTypes = new String[] {
		Anwendung.TYPE_ID,
		BSIModel.TYPE_ID,
		Client.TYPE_ID,
		Gebaeude.TYPE_ID,
		ITVerbund.TYPE_ID,
		NetzKomponente.TYPE_ID,
		Person.TYPE_ID,
		Raum.TYPE_ID,
		Server.TYPE_ID,
		SonstIT.TYPE_ID,
		TelefonKomponente.TYPE_ID
	};
	
	public StrukturanalyseReport(Properties reportProperties) {
		super(reportProperties);
	}

	private ArrayList<CnATreeElement> items;
	private ArrayList<CnATreeElement> categories;
	
	
	public String getTitle() {
		return "[BSI] Strukturanalyse nach BSI-GS";
	}

	private void getStrukturElements(CnATreeElement parent) {
		for (CnATreeElement child : parent.getChildren()) {
			
			if ( isStrukturElement(child) ) {
				items.add(child);
				if (! categories.contains(child.getParent()))
					categories.add(child.getParent());
			}
			getStrukturElements(child);
		}
	}

	/**
	 * @param child
	 * @return
	 */
	private boolean isStrukturElement(CnATreeElement child) {
		for (String strukturType : strukturElementTypes) {
			if (child.getEntityType() != null && child.getEntityType().getId().equals(strukturType))
				return true;
		}
		return false;
	}

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		categories = new ArrayList<CnATreeElement>();
		
		ITVerbund verbund = getItverbund();
		getStrukturElements(verbund);
		return items;
	}

	private ArrayList<CnATreeElement> getItems(CnATreeElement category) {
		if (items == null)
			getItems();
		ArrayList<CnATreeElement> categoryItems = new ArrayList<CnATreeElement>();
		for (CnATreeElement item : items) {
			if (item.getParent().equals(category))
				categoryItems.add(item);
		}
		return categoryItems;
	}
	
	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		
		AllCategories: for (CnATreeElement category : categories) {
			ArrayList<IOOTableRow> categoryRows = new ArrayList<IOOTableRow>();
			boolean wroteHeader = false;
			
			/* fill rows with
			 * categoryrow ("Mitarbeiter")
			 * propertyheadersrow ("Name, Vorname, ...")
			 * propertiesrow1 (Meier, Klaus)
			 * propertiesrow2 (Müller, Heinz)
			 * etc...
			 */
			
			
			AllItems: for (CnATreeElement child : getItems(category)) {
				List<String> columns = shownColumns.get(child.getEntity().getEntityType());
				if (columns.size() == 0)
					break AllItems;
				
				if (!wroteHeader) {
					categoryRows.add(new PropertyHeadersRow(
							child,
							columns,
							IOOTableRow.ROW_STYLE_SUBHEADER));
					wroteHeader = true;
				}
				categoryRows.add(new PropertiesRow(
						child, 
						columns,
						IOOTableRow.ROW_STYLE_ELEMENT));
			}

			// only add if header + items present:
			if (categoryRows.size() > 1) {
				rows.add(new SimpleRow(IOOTableRow.ROW_STYLE_HEADER, category.getTitle()));
				rows.addAll(categoryRows);
			}
				
		}
		return rows;
	}

	
}
