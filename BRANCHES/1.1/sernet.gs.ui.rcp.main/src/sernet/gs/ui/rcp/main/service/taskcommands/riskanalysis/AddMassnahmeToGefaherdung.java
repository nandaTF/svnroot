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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.hui.common.connect.ITypedElement;

/**
 * Assign a control instance to a threat instance.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class AddMassnahmeToGefaherdung extends GenericCommand implements IAuthAwareCommand {

	private RisikoMassnahmenUmsetzung child;
	private GefaehrdungsUmsetzung parent;

	private transient IAuthService authService;

	
	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService service) {
		this.authService = service;
	}
	
	/**
	 * @param parent
	 * @param child
	 */
	public AddMassnahmeToGefaherdung(GefaehrdungsUmsetzung parent,
			RisikoMassnahmenUmsetzung child) {
		this.child = child;
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
	    IBaseDao dao = getDaoFactory().getDAOforTypedElement(child);
		child = (RisikoMassnahmenUmsetzung) dao.merge(child);
		getDaoFactory().getDAOforTypedElement(parent).reload(parent, parent.getDbId());
		

		if (authService.isPermissionHandlingNeeded())
		{
			child.setPermissions(
				Permission.clonePermissions(
						child,
						parent.getPermissions()));
		}
		
		parent.addGefaehrdungsBaumChild(child);
		child.setParent(parent);
	}

	public RisikoMassnahmenUmsetzung getChild() {
		return child;
	}

	public GefaehrdungsUmsetzung getParent() {
		return parent;
	}
	
}
