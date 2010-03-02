/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 *     Daniel Murygin <dm@sernet.de>
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.AuditView;
import sernet.gs.ui.rcp.main.bsi.views.TodoView;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * <p>
 * Extensions of this abstract class loads {@link MassnahmenUmsetzung} instances out of the database
 * and wraps them in {@link TodoViewItem}s.
 * </p>
 * 
 * <p>
 * Lists of TodoViewItems are needed in the {@link AuditView} and
 * {@link TodoView}.
 * </p>
 * 
 * @see FindMassnahmeById
 * @sse {@link FindMassnahmenForITVerbund}
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("serial")
public abstract class FindMassnahmenAbstract extends GenericCommand {

	protected List<TodoViewItem> all = new ArrayList<TodoViewItem>(2000);
	private Set<String> executionSet;
	private Set<String> sealSet;

	/**
	 * Initialize lazy loaded field values needed for the view.
	 * 
	 * @param all
	 * @throws CommandException
	 */
	protected void fillList(List<MassnahmenUmsetzung> alleMassnahmen) throws CommandException {
		int count = 0;
		Set<UnresolvedItem> unresolvedItems = new HashSet<UnresolvedItem>();
		Set<MassnahmenUmsetzung> unresolvedMeasures = new HashSet<MassnahmenUmsetzung>();

		for (MassnahmenUmsetzung mn : alleMassnahmen) {
			// log.debug("Processing Massnahme: " + count);
			// hydrate(mn);

			String umsetzung = mn.getUmsetzung();
			String siegelStufe = String.valueOf(mn.getStufe());

			if ((getExecutionSet() == null || getExecutionSet().contains(umsetzung)) && (getSealSet() == null || getSealSet().contains(siegelStufe))) {

				TodoViewItem item = new TodoViewItem();

				if (mn.getParent() instanceof GefaehrdungsUmsetzung) {
					item.setParentTitle( // risikoanalyse.getparent()
							mn.getParent().getParent().getParent().getTitle());
				} else {
					item.setParentTitle(mn.getParent().getParent().getTitle());
				}

				item.setTitel(mn.getTitle());
				item.setUmsetzung(umsetzung);
				item.setUmsetzungBis(mn.getUmsetzungBis());
				item.setNaechsteRevision(mn.getNaechsteRevision());

				item.setStufe(siegelStufe.charAt(0));
				item.setUrl(mn.getUrl());
				item.setStand(mn.getStand());
				item.setDbId(mn.getDbId());

				unresolvedItems.add(new UnresolvedItem(item, mn.getDbId(), mn.getEntity().getProperties(MassnahmenUmsetzung.P_UMSETZUNGDURCH_LINK), mn.getEntity().getProperties(MassnahmenUmsetzung.P_NAECHSTEREVISIONDURCH_LINK)));

			}
		}

		// find persons linked directly:
		FindLinkedPersons findCommand = new FindLinkedPersons(unresolvedItems);
		findCommand = this.getCommandService().executeCommand(findCommand);
		all.addAll(findCommand.getResolvedItems());
		unresolvedItems = findCommand.getUnresolvedItems();

		// find persons according to roles and relation:
		FindResponsiblePersons command = new FindResponsiblePersons(unresolvedItems, MassnahmenUmsetzung.P_VERANTWORTLICHE_ROLLEN_UMSETZUNG);
		command = this.getCommandService().executeCommand(command);
		unresolvedItems = command.getResolvedItems();
		for (UnresolvedItem resolvedItem : unresolvedItems) {
			all.add(resolvedItem.getItem());
		}
	}

	public List<TodoViewItem> getAll() {
		return all;
	}

	public Set<String> getExecutionSet() {
		return executionSet;
	}

	public void setExecutionSet(Set<String> umsetzungSet) {
		this.executionSet = umsetzungSet;
	}

	public Set<String> getSealSet() {
		return sealSet;
	}

	public void setSealSet(Set<String> sealSet) {
		this.sealSet = sealSet;
	}

}
