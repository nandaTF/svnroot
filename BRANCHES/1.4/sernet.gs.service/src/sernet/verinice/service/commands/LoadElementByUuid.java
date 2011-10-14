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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

public class LoadElementByUuid<T extends CnATreeElement> extends GenericCommand {

	private String uuid;
	private T element;
    private String typeId;
    private RetrieveInfo ri;
    private boolean isJoin = true;

    private transient IBaseDao<T, Serializable> dao;
    
    
	public LoadElementByUuid(String uuid) {
        this(null,uuid,null);
    }
	
	public LoadElementByUuid(String uuid, RetrieveInfo ri) {
        this(null,uuid,ri);
    }

    public LoadElementByUuid(String typeId, String uuid) {
        this(typeId,uuid,null);
    }

    public LoadElementByUuid(String typeId, String uuid, RetrieveInfo ri) {
		super();
        this.uuid= uuid;
		this.typeId = typeId;
		if(ri!=null) {
		    this.ri=ri;
		} else {
		    ri = new RetrieveInfo();
		}
	}

	public void execute() {
	    if(isJoin()) {
	        executeWithJoin();
	    } else {
	        executeWithSelect();
	    }
	}

	private void executeWithSelect() {
	    RetrieveInfo riElement = new RetrieveInfo();
	    riElement.setProperties(ri.isProperties());
        riElement.setPermissions(ri.isPermissions());
        riElement.setLinksDown(ri.isLinksDown());
        riElement.setLinksUp(ri.isLinksUp());
        element = getDao().findByUuid(this.uuid,riElement);
        if(element!=null) {
            if(ri.isParent()) {     
                RetrieveInfo riParent = new RetrieveInfo();
                riParent.setPermissions(ri.isParentPermissions());
                riParent.setChildren(ri.isSiblings());                
                CnATreeElement parent = getDao().retrieve(element.getParentId(),riParent);
                element.setParent(parent);
            }
            if(ri.isChildren()) {
                StringBuilder sb = new StringBuilder();
                sb.append("from CnATreeElement as e");
                if(ri.isChildrenProperties()) {
                    sb.append(" left join fetch e.entity.typedPropertyLists.properties");
                }
                sb.append(" where e.parentId = ?");
                final String hql = sb.toString();
                List<CnATreeElement> children = getDao().findByQuery(hql, new Object[]{element.getDbId()});
                element.setChildren(new HashSet<CnATreeElement>(children));
            }
        }
	}
	
    /**
     * 
     */
    private void executeWithJoin() {
		element = getDao().findByUuid(this.uuid,ri);
    }

	public T getElement() {
		return element;
	}

    /**
     * @return the isJoin
     */
    public boolean isJoin() {
        return isJoin;
    }

    /**
     * @param isJoin the isJoin to set
     */
    public void setJoin(boolean isJoin) {
        this.isJoin = isJoin;
    }

    /**
     * @return the dao
     */
    public IBaseDao<T, Serializable> getDao() {
        if(dao==null) {
            if(typeId==null) {
                dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(CnATreeElement.class);
            } else {
                dao = getDaoFactory().getDAO(typeId);
            }       
        }
        return dao;
    }
	

}
