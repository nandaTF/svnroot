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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.ui.rcp.main.bsi.model.Attachment;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

/**
 * Loads files/attachmets meta data for a {@link CnATreeElement}
 * or for all elements if no id is set.
 * File data will not be loaded by this command. 
 * Use {@link LoadAttachmentFile} to load file data from database.
 * 
 * @see LoadAttachmentFile
 * @see FileView
 * @see Attachment
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadAttachments extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadAttachments.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadAttachments.class);
		}
		return log;
	}

	private Integer cnAElementId;
	
	private List<Attachment> attachmentList;
	
	public List<Attachment> getAttachmentList() {
		return attachmentList;
	}

	public void setAttachmentList(List<Attachment> noteList) {
		this.attachmentList = noteList;
	}

	/**
	 * Creates a new command, to load attachments for {@link CnATreeElement}
	 * with id cnAElementId.
	 * If cnAElementId all attachments will be loaded.
	 * 
	 * @param cnAElementId Id of a {@link CnATreeElement} or null
	 */
	public LoadAttachments(Integer cnAElementId) {
		super();
		this.cnAElementId = cnAElementId;
	}

	/**
	 * Loads attachments for for {@link CnATreeElement}
	 * with id cnAElementId.
	 * File data will not be loaded by this command. 
 	 * Use {@link LoadAttachmentFile} to load file data from database.
	 * 
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing, id is: " + getCnAElementId() + "...");
		}
		IBaseDao<Attachment, Serializable> dao = getDaoFactory().getDAO(Attachment.class);
		DetachedCriteria crit = DetachedCriteria.forClass(Attachment.class);
		if(getCnAElementId()!=null) {
			crit.add(Restrictions.eq("cnATreeElementId", getCnAElementId()));
		}
		crit.setFetchMode("entity", FetchMode.JOIN);
		crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
		crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
		crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<Attachment> attachmentList = dao.findByCriteria(crit);
		if (getLog().isDebugEnabled()) {
			getLog().debug("number of attachments found: " + attachmentList.size());
		}
		for (Attachment attachment : attachmentList) {
			Entity entity = attachment.getEntity();
			if(entity!=null) {
				for (PropertyList pl : entity.getTypedPropertyLists().values()) {
					for (Property p : pl.getProperties()) {
						p.setParent(entity);
					}
				}
			}
		}
		setAttachmentList(attachmentList);
		
	}

	public void setCnAElementId(Integer cnAElementId) {
		this.cnAElementId = cnAElementId;
	}


	public Integer getCnAElementId() {
		return cnAElementId;
	}

}
