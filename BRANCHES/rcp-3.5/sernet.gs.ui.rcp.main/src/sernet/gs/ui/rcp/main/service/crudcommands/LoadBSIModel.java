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

import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;

@SuppressWarnings("serial")
public class LoadBSIModel extends GenericCommand implements INoAccessControl {

	private BSIModel model;

	public LoadBSIModel() {
	}
	
	public void execute() {
		List<BSIModel> models = getDaoFactory().getDAO(BSIModel.class).findAll();
		if (models != null && models.size()>0)
			model = models.get(0);
		
		// TODO rschuster: What about extra BSIModels? Should we remove them?
	}

	public BSIModel getModel() {
		return model;
	}
	
	

}
