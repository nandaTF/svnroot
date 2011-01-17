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
package sernet.gs.ui.rcp.main.service.migrationcommands;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BSIModel;

public abstract class DbMigration extends GenericCommand {
	public abstract double getVersion();
	
	protected void updateVersion() {
		Logger.getLogger(this.getClass()).debug("Setting DB version to " + getVersion());
		try {
			LoadBSIModel command2 = new LoadBSIModel();
			command2 = getCommandService().executeCommand(command2);
			BSIModel model = command2.getModel();
			model.setDbVersion(getVersion());
			SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
			command4 = getCommandService().executeCommand(command4);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
}
