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
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadChildrenForExpansion extends GenericCommand {
	
	private static final Logger log = Logger.getLogger(LoadChildrenForExpansion.class);

	private static final long serialVersionUID = 6961271339569071215L;
	
	private CnATreeElement parent;
	private Integer dbId;
	private Class<? extends CnATreeElement> clazz;
	
	private Set<Class<?>> filteredClasses;

	public LoadChildrenForExpansion(CnATreeElement parent) {
		this(parent, new HashSet<Class<?>>());
	}

	public LoadChildrenForExpansion(CnATreeElement parent, Set<Class<?>> filteredClasses) {
		// slim down for transfer:
		dbId = parent.getDbId();
		clazz = parent.getClass();
		this.parent = null;
		this.filteredClasses = filteredClasses;
	}
	
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		parent = dao.findById(dbId);
		
		log.debug("Loading children for " + parent.getTitel());
		hydrate(parent);
		
		if (log.isDebugEnabled() && !filteredClasses.isEmpty())
			log.debug("Skipping the following model classes: " + filteredClasses);
		
		Set<CnATreeElement> children = parent.getChildren();
		for (CnATreeElement child : children) {
			if (!filteredClasses.contains(child.getClass()))
				hydrate(child);
		}
	}

	private void hydrate(CnATreeElement element) {
		if (element == null)
			return;
		
		if (element instanceof MassnahmenUmsetzung) {
			MassnahmenUmsetzung mn = (MassnahmenUmsetzung) element;
			mn.getKapitelValue();
			mn.getTitel();
			mn.getUmsetzung();
			mn.getUrl();
			mn.getStand();
			return;
		}
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(element), 
				element, true);
		
		// initialize all children:
		if (element instanceof FinishedRiskAnalysis
				|| element instanceof GefaehrdungsUmsetzung) {
			Set<CnATreeElement> children = element.getChildren();
			for (CnATreeElement child : children) {
				hydrate(child);
			}
		}
		
		if (element instanceof BausteinUmsetzung) {
			BausteinUmsetzung bst = (BausteinUmsetzung) element;
			bst.getKapitel();
			log.debug("Hydrating Baustein " + bst.getKapitel());
			
			Set<CnATreeElement> massnahmen = bst.getChildren();
			for (CnATreeElement massnahme : massnahmen) {
				hydrate(massnahme);
			}
		}
		
		if (element.getLinksDown().size() > 0 ) {
			Set<CnALink> links = element.getLinks().getChildren();
			for (CnALink link : links) {
				link.getTitle();
				link.getDependency().getUuid();
			}
		}
	}
	
	public CnATreeElement getElementWithChildren() {
		return parent;
	}
}
