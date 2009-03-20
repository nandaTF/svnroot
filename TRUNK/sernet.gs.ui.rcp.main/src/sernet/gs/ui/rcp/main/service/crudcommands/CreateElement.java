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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

/**
 * Create and save new element of type type to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateElement<T extends CnATreeElement> extends GenericCommand 
	implements IChangeLoggingCommand {

	private CnATreeElement container;
	private Class<T> type;
	protected T child;
	private String stationId;

	public CreateElement(CnATreeElement container, Class<T> type) {
		this.container = container;
		this.type = type;
		this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao 
			= (IBaseDao<T, Serializable>) getDaoFactory().getDAO(type);
		IBaseDao<Object, Serializable> containerDAO = getDaoFactory().getDAOForObject(container);
		
		try {
			containerDAO.reload(container, container.getDbId());
			
			// get constructor with parent-parameter and create new object:
			child = type.getConstructor(CnATreeElement.class).newInstance(container);
			child = dao.merge(child, false);
			container.addChild(child);
			child.setParent(container);
			
			
			// initialize UUID, used to find container in display in views:
			container.getUuid();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	public T getNewElement() {
		return child;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_INSERT;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
		result.add(child);
		return result;
	}
	
	

}
