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
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Load elements with matching data source and external ID.
 * Should usually return only one element - if the external ID was really unique. 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadCnAElementByExternalID extends GenericCommand {

	private String id;

	private List<CnATreeElement> list = new ArrayList<CnATreeElement>();

	private String sourceID;
	
	private boolean fetchLinksDown = false;
    
    private boolean fetchLinksUp = false;
    
    private boolean parent = false;
    
    private boolean properties = false;

	public LoadCnAElementByExternalID( String sourceID, String id) {
		this(sourceID,id,false,false);
	}
	
	/**
     * @param id
     * @param sourceID
     * @param fetchLinksDown
     * @param fetchLinksUp
     */
    public LoadCnAElementByExternalID(String sourceID ,String id, boolean fetchLinksDown, boolean fetchLinksUp) {
        super();
        this.id = id;
        this.sourceID = sourceID;
        this.fetchLinksDown = fetchLinksDown;
        this.fetchLinksUp = fetchLinksUp;
    }

    @Override
    public void execute() {
		IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
		DetachedCriteria criteria = DetachedCriteria.forClass(CnATreeElement.class);
		criteria.add(Restrictions.eq("sourceId", sourceID));
        criteria.add(Restrictions.eq("extId", id));
        criteria.setFetchMode("children", FetchMode.JOIN);
        if(fetchLinksDown) {
            criteria.setFetchMode("linksDown", FetchMode.JOIN);
            criteria.setFetchMode("linksDown.dependency", FetchMode.JOIN);
        }
        if(fetchLinksUp) {
            criteria.setFetchMode("linksUp", FetchMode.JOIN);
            criteria.setFetchMode("linksUp.dependant", FetchMode.JOIN);
        }
        if(parent) {
            criteria.setFetchMode("parent", FetchMode.JOIN);
            criteria.setFetchMode("parent.permissions", FetchMode.JOIN);
        }
        if(properties) {
            criteria.setFetchMode("entity", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
            criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        list = dao.findByCriteria(criteria);
	}

	public void setParent(boolean parent) {
        this.parent = parent;
    }

    public boolean isProperties() {
        return properties;
    }

    public void setProperties(boolean properties) {
        this.properties = properties;
    }

    public List<CnATreeElement> getElements() {
		return list;
	}


}
