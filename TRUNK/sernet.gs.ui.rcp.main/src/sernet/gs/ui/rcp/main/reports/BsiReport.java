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
package sernet.gs.ui.rcp.main.reports;

import java.io.Serializable;
import java.util.Properties;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

@SuppressWarnings("serial")
public abstract class BsiReport implements Serializable, IBSIReport {

	protected Properties reportProperties;
	private ITVerbund itverbund;
	
	public ITVerbund getItverbund() {
		return itverbund;
	}

	public void setItverbund(ITVerbund itverbund) {
		this.itverbund = itverbund;
	}

	public BsiReport(Properties reportProperties) {
		this.reportProperties = reportProperties;
	}

	/**
	 * Check if list of default columns for export contains the given column.
	 * 
	 */
	public boolean isDefaultColumn(String property_id) {
		String prop = reportProperties.getProperty(getClass().getSimpleName());
		if (prop == null)
			return false;
		return (prop.indexOf(property_id) > -1 );
	}


}
