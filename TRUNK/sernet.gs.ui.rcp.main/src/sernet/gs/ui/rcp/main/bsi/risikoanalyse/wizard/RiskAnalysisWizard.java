/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah@sernet.de>
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
 *     Anne Hanekop <ah@sernet.de> 	- initial API and implementation
 *     ak@sernet.de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenFactory;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.AssociateGefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.DisassociateGefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.LoadAssociatedGefaehrdungen;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.StartNewRiskAnalysis;

/**
 * Wizard to accomplish a 'BSI-Standard 100-3' risk-analysis. RiskAnalysisWizard
 * 
 * @author ahanekop@sernet.de
 */
public class RiskAnalysisWizard extends Wizard implements IExportWizard {

	private boolean canFinish = false;
	private CnATreeElement cnaElement;
	private ChooseGefaehrdungPage chooseGefaehrdungPage;
	private EstimateGefaehrdungPage estimateGefaehrdungPage;
	private RiskHandlingPage riskHandlingPage;
	private AdditionalSecurityMeasuresPage additionalSecurityMeasuresPage;

	/**
	 * Element to save all relevant data in DB on completion of wizard. Also
	 * parent for all GefährdungsUmsetzung objects.
	 */
	private FinishedRiskAnalysis finishedRiskAnalysis = null;

	/* list of all Gefaehrdungen - ChooseGefaehrungPage_OK */
	private ArrayList<Gefaehrdung> allGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of all own Gefaehrdungen of type OwnGefaehrdung -
	 * ChooseGefaehrungPage_OK
	 */
	private List<OwnGefaehrdung> allOwnGefaehrdungen = new ArrayList<OwnGefaehrdung>();

	/* list of all MassnahmenUmsetzungen - AdditionalSecurityMeasuresPage */
	private ArrayList<MassnahmenUmsetzung> allMassnahmenUmsetzungen = new ArrayList<MassnahmenUmsetzung>();

	// Are we editing a previous Risk Analysis?
	private boolean previousAnalysis = false;
	private FinishedRiskAnalysisLists finishedRiskLists;

	public RiskAnalysisWizard(CnATreeElement treeElement) {
		cnaElement = treeElement;
	}

	public RiskAnalysisWizard(CnATreeElement parent,
			FinishedRiskAnalysis analysis) {
		this(parent);

		try {
			LoadChildrenForExpansion command = new LoadChildrenForExpansion(
					analysis);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			finishedRiskAnalysis = (FinishedRiskAnalysis) command
					.getElementWithChildren();
		} catch (CommandException e) {
			ExceptionUtil.log(e,
					"Konnte gespeicherte Risikoanalyse nicht neu laden.");
		}
	}

	/**
	 * Cause update to risk analysis object in loaded model.
	 * 
	 * @return always true
	 */
	@Override
	public boolean performFinish() {
		// all has been saved by now, so no need to do anything except refresh the view:
		
		// FIXME server: just reload risk analysis instead of complete model.

		CnAElementFactory.getInstance().reloadModelFromDatabase();

		return true;
	}

	/**
	 * Cause update to risk analysis object in loaded model.
	 * 
	 * @return always true
	 */
	@Override
	public boolean performCancel() {
		// FIXME server: just reload risk analysis instead of complete model.
		CnAElementFactory.getInstance().reloadModelFromDatabase();
		return true;
	}

	/**
	 * Needs to be implemented because of IExportWizard/IWorkbenchWizard. Entry
	 * point.
	 * 
	 * @param workbench
	 *            the current workbench
	 * @param selection
	 *            the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			if (finishedRiskAnalysis == null) {
				StartNewRiskAnalysis command = new StartNewRiskAnalysis(
						cnaElement);
				command = ServiceFactory.lookupCommandService().executeCommand(
						command);
				finishedRiskAnalysis = command.getFinishedRiskAnalysis();
				finishedRiskLists = command.getFinishedRiskLists();
			} else {
				finishedRiskLists = FinishedRiskAnalysisListsHome.getInstance()
						.loadById(finishedRiskAnalysis.getDbId());
				previousAnalysis = true;
			}
		} catch (CommandException e) {
			ExceptionUtil
					.log(e,
							"Datenzugriffsfehler beim Erzeugen / Laden der Risikoanalyse.");
		}

		loadAllGefaehrdungen();
		loadAllMassnahmen();
		loadAssociatedGefaehrdungen();

		loadOwnGefaehrdungen();
		addOwnGefaehrdungen();

		addRisikoMassnahmenUmsetzungen(loadRisikomassnahmen());

	}

	private List<RisikoMassnahme> loadRisikomassnahmen() {
		try {
			return RisikoMassnahmeHome.getInstance().loadAll();
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Datenzugriff.");
			return null;
		}
	}

	/**
	 * Adds extra pages to the Wizard.
	 */
	public void addPages() {
		setWindowTitle("Risikoanalyse auf Basis von IT-Grundschutz");

		chooseGefaehrdungPage = new ChooseGefaehrdungPage();
		addPage(chooseGefaehrdungPage);

		estimateGefaehrdungPage = new EstimateGefaehrdungPage();
		addPage(estimateGefaehrdungPage);

		riskHandlingPage = new RiskHandlingPage();
		addPage(riskHandlingPage);

		additionalSecurityMeasuresPage = new AdditionalSecurityMeasuresPage();
		addPage(additionalSecurityMeasuresPage);
	}

	/**
	 * Sets the List of Gefaehrdungen associated to the chosen IT-system.
	 * 
	 * @param newAssociatedGefaehrdungen
	 *            ArrayList of Gefaehrdungen
	 */
	public void setAssociatedGefaehrdungen(
			ArrayList<GefaehrdungsUmsetzung> newAssociatedGefaehrdungen) {
		finishedRiskLists
				.setAssociatedGefaehrdungen(newAssociatedGefaehrdungen);
	}

	/**
	 * Returns the current List of Gefaehrdungen associated to the chosen
	 * IT-system.
	 * 
	 * @return ArrayList of currently associated Gefaehrdungen
	 */
	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return finishedRiskLists.getAssociatedGefaehrdungen();
	}

	/**
	 * Saves all Gefaehrdungen availiable from BSI IT-Grundschutz-Kataloge.
	 */
	private void loadAllGefaehrdungen() {
		List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance()
				.getBausteine();
		alleBausteine: for (Baustein baustein : bausteine) {
			if (baustein.getGefaehrdungen() == null)
				continue;
			alleGefaehrdungen: for (Gefaehrdung gefaehrdung : baustein
					.getGefaehrdungen()) {
				Boolean duplicate = false;
				alleTitel: for (IGSModel element : allGefaehrdungen) {
					if (element.getId().equals(gefaehrdung.getId())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					allGefaehrdungen.add(gefaehrdung);
				}
			}
		}
	}

	/**
	 * Saves all Massnahmen for the chosen IT-system in a List.
	 */
	private void loadAllMassnahmen() {
		List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance()
				.getBausteine();

		NullModel nullModel = new NullModel() {
			@Override
			public boolean canContain(Object obj) {
				return true;
			}
		};

		MassnahmenFactory massnahmenFactory = new MassnahmenFactory();
		alleBausteine: for (Baustein baustein : bausteine) {
			alleMassnahmen: for (Massnahme massnahme : baustein.getMassnahmen()) {
				Boolean duplicate = false;
				alleTitel: for (MassnahmenUmsetzung vorhandeneMassnahmenumsetzung : allMassnahmenUmsetzungen) {
					if (vorhandeneMassnahmenumsetzung.getName().equals(
							massnahme.getTitel())) {
						duplicate = true;
						break alleTitel;
					}
				}
				if (!duplicate) {
					MassnahmenUmsetzung massnahmeUmsetzung;
					try {
						massnahmeUmsetzung = massnahmenFactory
								.createMassnahmenUmsetzung(massnahme);
						allMassnahmenUmsetzungen.add(massnahmeUmsetzung);
					} catch (Exception e) {
						Logger
								.getLogger(this.getClass())
								.error(
										"Fehler beim Erstellen der Massnahmenumsetzung: ",
										e);
					}

				}
			}
		}
	}

	/**
	 * Saves all Gefaehrdungen associated to the chosen IT-system in a List.
	 * 
	 * @throws CommandException
	 */
	private void loadAssociatedGefaehrdungen() {
		try {
			LoadAssociatedGefaehrdungen command = new LoadAssociatedGefaehrdungen(
					cnaElement);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			this.finishedRiskLists.getAssociatedGefaehrdungen().addAll(
					command.getAssociatedGefaehrdungen());
		} catch (CommandException e) {
			ExceptionUtil.log(e,
					"Fehler beim Laden der zugeordneten Gefährdungen.");
		}

	}

	/**
	 * Saves all own Gefaehrdungen in a List.
	 */
	private void loadOwnGefaehrdungen() {
		try {
			allOwnGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Datenzugriff.");
		}
	}

	/**
	 * Sets the List of all Gefaehrdungen.
	 * 
	 * @param newAllGefaehrdungen
	 *            ArrayList of all Gefaehrdungen
	 */
	public void setAllGefaehrdungen(ArrayList<Gefaehrdung> newAllGefaehrdungen) {
		allGefaehrdungen = newAllGefaehrdungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen availiable from BSI
	 * IT-Grundschutz-Kataloge plus the own Gefaehrdungen.
	 * 
	 * @return ArrayList of all Gefaehrdungen
	 */
	public ArrayList<Gefaehrdung> getAllGefaehrdungen() {
		return allGefaehrdungen;
	}

	/**
	 * Sets the List of all own Gefaehrdungen.
	 * 
	 * @param newAllOwnGefaehrdungen
	 *            List of own Gefaehrdungen
	 */
	public void setAllOwnGefaehrdungen(
			ArrayList<OwnGefaehrdung> newAllOwnGefaehrdungen) {
		allOwnGefaehrdungen = newAllOwnGefaehrdungen;
	}

	/**
	 * Returns the List of all own Gefaehrdungen.
	 * 
	 * @return ArrayList of all own Gefaehrdungen
	 */
	public List<OwnGefaehrdung> getAllOwnGefaehrdungen() {
		return allOwnGefaehrdungen;
	}

	/**
	 * Returns the List of all Gefaehrdungen of type GefaehrdungsUmsetzung.
	 * 
	 * @return ArrayList of GefaehrdungsUmsetzung
	 */
	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return finishedRiskLists.getAllGefaehrdungsUmsetzungen();
	}

	public FinishedRiskAnalysisLists getFinishedRiskAnalysisLists() {
		return this.finishedRiskLists;
	}

	/**
	 * Allows repeating the Wizard with result from a previous anaylsis.
	 * 
	 * @param oldAnalysis
	 *            old analysis to repeat all steps.
	 */
	public void setOldAnalysis(FinishedRiskAnalysis oldAnalysis) {
		this.finishedRiskAnalysis = oldAnalysis;
	}

	/**
	 * Returns the chosen IT-system to make the risk-analysis for.
	 * 
	 * @return the IT-system to make the risk-analysis for
	 */
	public CnATreeElement getCnaElement() {
		return cnaElement;
	}

	/**
	 * Adds the own Gefaehrdungen to the List of all Gefaehrdungen from BSI
	 * IT-Grundschutz-Kataloge.
	 */
	public void addOwnGefaehrdungen() {
		for (OwnGefaehrdung element : allOwnGefaehrdungen) {
			/* add to list of all Gefaehrdungen */
			if (!(allGefaehrdungen.contains(element))) {
				allGefaehrdungen.add(element);
			}
		}
	}

	/**
	 * Adds the own Massnahmen to the List of all Massnahmen from BSI
	 * IT-Grundschutz-Kataloge.
	 */
	public void addRisikoMassnahmenUmsetzungen(
			List<RisikoMassnahme> allRisikoMassnahmen) {
		for (RisikoMassnahme massnahme : allRisikoMassnahmen) {
			addRisikoMassnahmeUmsetzung(massnahme);

		}
	}

	public void addRisikoMassnahmeUmsetzung(RisikoMassnahme massnahme) {
		try {
			RisikoMassnahmenUmsetzung massnahmeUmsetzung = RisikoMassnahmenUmsetzungFactory
					.buildFromRisikomassnahme(massnahme, null, null);

			/* add to list of all MassnahmenUmsetzungen */
			if (!(allMassnahmenUmsetzungen.contains(massnahmeUmsetzung))) {
				allMassnahmenUmsetzungen.add(massnahmeUmsetzung);
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					"Fehler beim Erstellen der Massnahmenumsetzung: ", e);
		}
	}

	/**
	 * Returns the List of Gefaehrdungen which need additional security
	 * measures.
	 * 
	 * @return the List of Gefaehrdungen of type GefaehrdungsUmsetzung
	 */
	public List<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
		return finishedRiskLists.getNotOKGefaehrdungsUmsetzungen();
	}

	/**
	 * Returns the List of all Massnahmen of type MassnahmenUmsetzung.
	 * 
	 * @return ArrayList of all Massnahmen of type MassnahmenUmsetzung
	 */
	public ArrayList<MassnahmenUmsetzung> getAllMassnahmenUmsetzungen() {
		return allMassnahmenUmsetzungen;
	}

	/**
	 * Saves the List of all Massnahmen of type MassnahmenUmsetzung.
	 * 
	 * @param newAllMassnahmenUmsetzungen
	 *            the allMassnahmenUmsetzungen to set
	 */
	public void setAllMassnahmenUmsetzungen(
			ArrayList<MassnahmenUmsetzung> newAllMassnahmenUmsetzungen) {
		allMassnahmenUmsetzungen = newAllMassnahmenUmsetzungen;
	}

	/**
	 * Returns whether this wizard could be finished without further user
	 * interaction.
	 * 
	 * @return true, if wizard can be finished, false else
	 */
	@Override
	public boolean canFinish() {
		return canFinish;
	}

	/**
	 * Saves a new state of canFinish.
	 * 
	 * @param newCanFinish
	 *            set true, if wizard can be finished at this point, false else
	 */
	public void setCanFinish(boolean newCanFinish) {
		canFinish = newCanFinish;
	}

	/**
	 * Returns the parent Element of .
	 * 
	 * @return the parent Element
	 */
	public CnATreeElement getFinishedRiskAnalysis() {
		return this.finishedRiskAnalysis;
	}

	public void addAssociatedGefaehrdung(Gefaehrdung currentGefaehrdung) {

		try {
			if (!GefaehrdungsUtil.listContainsById(finishedRiskLists
					.getAssociatedGefaehrdungen(), currentGefaehrdung)) {
				/* Add to List of Associated Gefaehrdungen */
				AssociateGefaehrdungsUmsetzung command = new AssociateGefaehrdungsUmsetzung(
						finishedRiskLists.getDbId(), currentGefaehrdung, this.finishedRiskAnalysis.getDbId());
				command = ServiceFactory.lookupCommandService().executeCommand(
						command);

				finishedRiskLists = command.getFinishedRiskLists();
			}
		} catch (CommandException e) {
			ExceptionUtil.log(e, "");
		}
	}

	public void removeAssociatedGefaehrdung(Gefaehrdung currentGefaehrdung)
			throws Exception {
		/* remove from List of Associated Gefaehrdungen */
		DisassociateGefaehrdungsUmsetzung command = new DisassociateGefaehrdungsUmsetzung(
				finishedRiskAnalysis, finishedRiskLists.getDbId(),
				currentGefaehrdung);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		finishedRiskLists = command.getFinishedRiskLists();
		finishedRiskAnalysis = command.getFinishedRiskAnalysis();
	}

	public void setFinishedRiskLists(FinishedRiskAnalysisLists finishedRiskLists) {
		this.finishedRiskLists = finishedRiskLists;
	}
}
