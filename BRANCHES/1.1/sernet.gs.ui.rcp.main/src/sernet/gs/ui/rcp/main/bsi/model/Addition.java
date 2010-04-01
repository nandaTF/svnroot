/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArraySet;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;


/**
 * Base class for all entities which add additional data to {@link CnATreeElement}s
 * 
 * @see Note
 * @see Attachment
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Addition implements Serializable{
	
	private transient CopyOnWriteArraySet<INoteChangedListener>  listeners;
	
	Integer dbId;

	Integer cnATreeElementId;
	
	private transient String cnAElementTitel;
	
	Entity entity;

	public Addition() {
		super();
	}
	
	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	public Integer getCnATreeElementId() {
		return cnATreeElementId;
	}

	public void setCnATreeElementId(Integer cnATreeElementId) {
		this.cnATreeElementId = cnATreeElementId;
	}
	
	public String getCnAElementTitel() {
		return cnAElementTitel;
	}

	public void setCnAElementTitel(String cnAElementTitel) {
		this.cnAElementTitel = cnAElementTitel;
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	protected HUITypeFactory getTypeFactory() {
		return HitroUtil.getInstance().getTypeFactory();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Addition other = (Addition) obj;
		if (dbId == null) {
			if (other.dbId != null)
				return false;
		} else if (!dbId.equals(other.dbId))
			return false;
		return true;
	}

	public void fireChange() {
		for (INoteChangedListener list : getListener()) {
			list.noteChanged();
		}
	}
	
	public interface INoteChangedListener {
		public void noteChanged();
	}
	
	public CopyOnWriteArraySet<INoteChangedListener> getListener() {
		if(listeners==null) {
			listeners = new CopyOnWriteArraySet<INoteChangedListener>();
		}
		return listeners;
	}
	
	public void addListener(INoteChangedListener listener) {
		getListener().add(listener);
	}
	
	public void removeListener(INoteChangedListener listener) {
		getListener().remove(listener);
	}


}
