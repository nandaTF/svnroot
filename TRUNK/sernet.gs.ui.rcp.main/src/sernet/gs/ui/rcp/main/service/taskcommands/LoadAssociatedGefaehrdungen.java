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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.GefaehrdungsUtil;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;

public class LoadAssociatedGefaehrdungen extends GenericCommand {

	private CnATreeElement cnaElement;
	private List<Baustein> alleBausteine;
	private ArrayList<GefaehrdungsUmsetzung> associatedGefaehrdungen;

	public LoadAssociatedGefaehrdungen(CnATreeElement cnaElement) {
		this.cnaElement = cnaElement;
	}

	public void execute() {
		associatedGefaehrdungen = new ArrayList<GefaehrdungsUmsetzung>();
		
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(cnaElement);
		dao.reload(cnaElement, cnaElement.getDbId());
		
		Set<CnATreeElement> children = cnaElement.getChildren();
		CollectBausteine: for (CnATreeElement cnATreeElement : children) {
			if (!(cnATreeElement instanceof BausteinUmsetzung))
				continue CollectBausteine;

			BausteinUmsetzung bausteinUmsetzung = (BausteinUmsetzung) cnATreeElement;
			Baustein baustein = findBausteinForId(bausteinUmsetzung.getKapitel());
			if (baustein == null)
				continue;

			for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
				if (!GefaehrdungsUtil.listContainsById(associatedGefaehrdungen, gefaehrdung)) {
					associatedGefaehrdungen.add(
							GefaehrdungsUmsetzungFactory.build(
									null, gefaehrdung));
				}
			}
		}
		
	}

	private Baustein findBausteinForId(String id) {
		if (alleBausteine == null) {
			LoadBausteine bstsCommand = new LoadBausteine();
			try {
				bstsCommand = getCommandService().executeCommand(bstsCommand);
			} catch (CommandException e) {
				throw new RuntimeCommandException(e);
			}
			alleBausteine = bstsCommand.getBausteine();
		}
		
		for (Baustein baustein : alleBausteine) {
			if (baustein.getId().equals(id))
				return baustein;
		}
		return null;
	}

	public void clear() {
		alleBausteine = null;
		cnaElement = null;
	}

	public ArrayList<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

}
