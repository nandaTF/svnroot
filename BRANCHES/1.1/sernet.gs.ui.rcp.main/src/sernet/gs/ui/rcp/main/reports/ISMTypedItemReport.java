/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.proxy.HibernateProxy;

import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
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
import sernet.verinice.iso27k.model.Organization;

/**
 * Export all items of the given CnaTreeElementType.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ISMTypedItemReport extends BsiReport
	implements IBSIReport, ISMReport {

	public ISMTypedItemReport(Properties reportProperties) {
		super(reportProperties);
	}

	private ArrayList<CnATreeElement> items;
	private Organization organization;
	private String entityTypeId;
	
	public String getEntityTypeId() {
		return entityTypeId;
	}

	public void setEntityTypeId(String entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	public String getTitle() {
		return "[ISM] Export all items of selected type";
	}

	public ArrayList<CnATreeElement> getItems() {
		if (items != null)
			return items;
		items = new ArrayList<CnATreeElement>();
		
		Organization org = getOrganization();
		addAllItems(org);
		return items;
	}

	
	/**
	 * Recursively add all children.
	 * @param verbund
	 */
	private void addAllItems(CnATreeElement elmt) {
		if (elmt.getEntityType().getId().equals(entityTypeId)) {
			items.add(elmt);
		}
		for (CnATreeElement child: elmt.getChildren()) {
			addAllItems(child);
		}
	}

	public ArrayList<IOOTableRow> getReport(PropertySelection shownColumns) {
		ArrayList<IOOTableRow> rows = new ArrayList<IOOTableRow>();
		List<String> columns = shownColumns.get(items.get(0).getEntity().getEntityType());

		rows.add(new PropertiesRow(
				items.get(0),
				columns,
				IOOTableRow.ROW_STYLE_SUBHEADER));

		for (CnATreeElement child : items) {
			rows.add(new PropertiesRow(
					child, 
					columns, 
					IOOTableRow.ROW_STYLE_ELEMENT));
		}
		
		return rows;
	}

	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization org) {
		this.organization = org;
	}	
}