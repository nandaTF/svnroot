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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.Map.Entry;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;

@SuppressWarnings("serial")
public class CreateConfiguration extends GenericCommand {

	private CnATreeElement person;
	private Configuration configuration;
	
	private static String PROP_ROLE = "configuration_rolle";
	private static String DEFAULT_USER_GROUP = "default-user-group";
	

	public CreateConfiguration(CnATreeElement elmt) {
		this.person = elmt;
	}
	
	@SuppressWarnings("restriction")
	public void execute() {
		configuration = new Configuration();
		if (person == null)
			throw new RuntimeCommandException("Default Konfiguration wurde bereits gesetzt.");
			
		configuration.setPerson(person);
        HUITypeFactory factory = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
        PropertyType pType = factory.getPropertyType(Configuration.TYPE_ID, PROP_ROLE);
		configuration.getEntity().setSimpleValue(pType, DEFAULT_USER_GROUP);
		getDaoFactory().getDAO(Configuration.class).saveOrUpdate(configuration);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
	

}
