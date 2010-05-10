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
package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.hui.common.connect.HuiRelation;

/**
 * Association class for links between items.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class CnALink implements Serializable {

	public static final int DEPENDANT_ON      	= 1;
	public static final int ADMINISTRATED_BY 	= 2;
//	public static final int USED_BY 			= 3;
	public static final int LOCATED_IN 		= 4;
	
	// user entered comment:
	private String comment;
	
	/**
	 * Takes an object and a link and gives back the correct display name for the object on the *other* side
	 * of the link. 
	 * Which side is the other side, is determined on whether the link goes from or to the object.
	 * 
	 * @param link
	 */
	public static String getRelationObjectTitle(CnATreeElement inputElmt, CnALink link) {
		if (CnALink.isDownwardLink(inputElmt, link))
			return link.getDependency().getTitle();
		else
			return link.getDependant().getTitle();
	}
	
	/**
	 * Returns the correct title for a link for e given element and a given link.
	 * The correct name is determined using the direction in which the link points to the object.
	 * 
	 * @param link
	 * @return
	 */
	public static String getRelationName(CnATreeElement fromElement, CnALink link) {
		HuiRelation relation = HitroUtil.getInstance().getTypeFactory().getRelation(link.getRelationId());
		String name;
		if (relation == null) {
			name = isDownwardLink(fromElement, link) ? "" : ""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			name = isDownwardLink(fromElement, link) ? relation.getName() : relation.getReversename();
		}
		return name;
	}
	
	/**
	 * @param link
	 * @return
	 */
	public static boolean isDownwardLink(CnATreeElement fromElement, CnALink link) {
		return fromElement.getLinksDown().contains(link);
	}
	
	public String getRelationId() {
		return getId().relationId;
	}

	public String getComment() {
		return comment;
	}

	public static class Id implements Serializable {
		private Integer dependantId;
		private Integer dependencyId;
		private String relationId;
		
		public Id() {}
		
		public Id(Integer dependantId, Integer dependencyId) {
			this(dependantId, dependencyId, ""); //$NON-NLS-1$
		}

		public Id(Integer dependantId, Integer dependencyId, String relationId) {;
			this.dependantId = dependantId;
			this.dependencyId = dependencyId;
			this.relationId = relationId;
		}
		
		public boolean equals(Object o) {
			if (o != null && o instanceof Id) {
				Id that = (Id)o;
				return this.dependantId.equals(that.dependantId)
					&& this.dependencyId.equals(that.dependencyId)
					&& this.relationId.equals(that.relationId);
			} 
			else {
				return false;
			}
		}
		
		public int hashCode() {
			if (dependantId == null || dependencyId == null || relationId == null)	
					return super.hashCode();
			return dependantId.hashCode() + dependencyId.hashCode() + relationId.hashCode();
		}
		
	}

	private Id id;
	
	// link type category as definied by integer constand (see above):
	private int linkType =0;
	
	private CnATreeElement dependant;
	private CnATreeElement dependency;
	
	protected CnALink() {}
	
	public CnALink(CnATreeElement dependant, CnATreeElement dependency, String relationId, String comment) {
		// set linked items:
		this.dependant = dependant;
		this.dependency = dependency;
		this.comment = comment;
		
		// set IDs:
		getId().dependantId = dependant.getDbId();
		getId().dependencyId = dependency.getDbId();
		getId().relationId = relationId;
	
		// maintain bi-directional association:
		dependency.addLinkUp(this);
		dependant.addLinkDown(this);
		this.linkType = linkTypeFor(dependency);
	}
	
	protected void setComment(String comment) {
		this.comment = comment;
	}

	protected void setLinkType(int linkType) {
		this.linkType = linkType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj != null && obj instanceof CnALink) {
			CnALink that = (CnALink) obj;
			return this.getId().equals(that.getId());
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	
	public void remove() {
		dependant.removeLinkDown(this);
		dependency.removeLinkUp(this);

	}

	private int linkTypeFor(CnATreeElement target) {
		if (target instanceof Person)
			return ADMINISTRATED_BY;
		if (target instanceof Raum || target instanceof Gebaeude)
			return LOCATED_IN;
		return DEPENDANT_ON;
	}

	public CnATreeElement getDependant() {
		return dependant;
	}

	public void setDependant(CnATreeElement dependant) {
		this.dependant = dependant;
	}

	public CnATreeElement getDependency() {
		return dependency;
	}

	public void setDependency(CnATreeElement dependency) {
		this.dependency = dependency;
	}

	public synchronized Id getId() {
		 if (this.id == null)
			 this.id = new Id();
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public int getLinkType() {
		return linkType;
	}

	public String getTitle() {
		return typeTitle() + dependency.getTitle();
	}

	private String typeTitle() {
		switch (linkType) {
		case DEPENDANT_ON:
			return Messages.getString("CnALink.0"); //$NON-NLS-1$
		case ADMINISTRATED_BY:
			return Messages.getString("CnALink.4"); //$NON-NLS-1$
//		case USED_BY:
//			return Messages.CnALink_used;
		case LOCATED_IN:
			return Messages.getString("CnALink.1"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

//	public LinkKategorie getParent() {
//		return dependant.getLinks();
//	}

	
}
