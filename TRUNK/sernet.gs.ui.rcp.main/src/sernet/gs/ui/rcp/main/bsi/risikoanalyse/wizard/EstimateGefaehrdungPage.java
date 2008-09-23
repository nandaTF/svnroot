package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.ChooseGefaehrdungPage.SearchFilter;

/**
 * WizardPage lists all previously selected Gefaehrdungen for the
 * user to decide which Gefaehrdungen further processing need.
 * 
 * @author ahanekop@sernet.de
 */
public class EstimateGefaehrdungPage extends WizardPage {

	private Composite composite;
	private TableColumn checkboxColumn;
	private TableColumn imageColumn;
	private TableColumn numberColumn;
	private TableColumn nameColumn;
	private TableColumn descriptionColumn;
	private CheckboxTableViewer viewer;
	private OwnGefaehrdungenFilter ownGefaehrdungFilter = new OwnGefaehrdungenFilter();
	private GefaehrdungenFilter gefaehrdungFilter = new GefaehrdungenFilter();
	private SearchFilter searchFilter = new SearchFilter();
	private RiskAnalysisWizard wizard;

	/**
	 * Constructor sets title and description of WizardPage.
	 */
	protected EstimateGefaehrdungPage() {
		super("Gefährdungsbewertung");
		setTitle("Gefährdungsbewertung");
		setDescription("Wählen Sie die Gefährdungen aus, denen NICHT" +
				" ausreichend Rechnung getragen wurde.");
	}

	/**
	 * Adds widgets to the wizardPage.
	 * Called once at startup of Wizard.
	 * 
	 *  @param parent the parent Composite
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);
		setControl(composite);

		/* CheckboxTableViewer */
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		final Table table = viewer.getTable();
		GridData data1 = new GridData();
		data1.grabExcessHorizontalSpace = true;
		data1.grabExcessVerticalSpace = true;
		data1.horizontalAlignment = SWT.FILL;
		data1.verticalAlignment = SWT.FILL;
		table.setLayoutData(data1);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		checkboxColumn = new TableColumn(table, SWT.LEFT);
		checkboxColumn.setText("");
		checkboxColumn.setWidth(35);

		imageColumn = new TableColumn(table, SWT.LEFT);
		imageColumn.setText("");
		imageColumn.setWidth(35);

		numberColumn = new TableColumn(table, SWT.LEFT);
		numberColumn.setText("Nummer");
		numberColumn.setWidth(100);

		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(100);

		descriptionColumn = new TableColumn(table, SWT.LEFT);
		descriptionColumn.setText("Beschreibung");
		descriptionColumn.setWidth(200);

		/**
		 * listener adds/removes Gefaehrdungen to Arrays of Gefaehrdungen
		 */
		viewer.addCheckStateListener(new ICheckStateListener() {

			/**
			 * Notifies of a change to the checked state of an element.
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				Gefaehrdung currentGefaehrdung = (Gefaehrdung) event
						.getElement();
				List<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen =
					((RiskAnalysisWizard) getWizard()).getAllGefaehrdungsUmsetzungen();

				/* switch from Gefaehrdung to GefaehrdungsUmsetzung */
				if (event.getChecked()) {
					/* checkbox set */

					try {
						
						GefaehrdungsUmsetzung newGefaehrdungsUmsetzung = GefaehrdungsUmsetzungFactory
						.build(((RiskAnalysisWizard) getWizard())
								.getFinishedRiskAnalysis(),
								currentGefaehrdung);
						
						((RiskAnalysisWizard)getWizard()).getFinishedRiskAnalysis().addChild(newGefaehrdungsUmsetzung);
						newGefaehrdungsUmsetzung.setOkay(false);
						
						/* add to arrListGefaehrdungsUmsetzungen */
						arrListGefaehrdungsUmsetzungen.add(newGefaehrdungsUmsetzung);
						
					} catch (Exception e) {
						Logger.getLogger(this.getClass()).debug(e.toString());
					}
					
				} else {
					/* checkbox unset */

					/* remove from arrListGefaehrdungsUmsetzungen */
					for (GefaehrdungsUmsetzung gefaehrdung : arrListGefaehrdungsUmsetzungen) {
						if (currentGefaehrdung.getId().equals(
								gefaehrdung.getId())) {
							((RiskAnalysisWizard)getWizard()).getFinishedRiskAnalysis().removeChild(gefaehrdung);
							gefaehrdung.setOkay(true);
							arrListGefaehrdungsUmsetzungen.remove(gefaehrdung);
							break;
						}
					}
				}

				((RiskAnalysisWizard) getWizard()).setCanFinish(false);
				checkPageComplete();
			}
		});
		
		/* group the Filter checkboxes with composite */
		Composite compositeFilter = new Composite(composite, SWT.NULL);
		GridLayout gridLayoutFilters = new GridLayout();
		gridLayoutFilters.numColumns = 2;
		compositeFilter.setLayout(gridLayoutFilters);
		GridData gridCompositeFilter = new GridData();
		gridCompositeFilter.horizontalAlignment = SWT.LEFT;
		gridCompositeFilter.verticalAlignment = SWT.TOP;
		compositeFilter.setLayoutData(gridCompositeFilter);

		/* filter button - OwnGefaehrdungen only */
		Button buttonOwnGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
		buttonOwnGefaehrdungen.setText("nur eigene Gefährdungen anzeigen");
		GridData gridOwnGefaehrdungen = new GridData();
		gridOwnGefaehrdungen.horizontalSpan = 2;
		buttonOwnGefaehrdungen.setLayoutData(gridOwnGefaehrdungen);

		/* Listener adds/removes Filter ownGefaehrdungFilter */
		buttonOwnGefaehrdungen.addSelectionListener(new SelectionAdapter() {

			/**
			 * Adds/removes Filter depending on event.
			 * 
			 * @param event event containing information about the selection
			 */
			public void widgetSelected(SelectionEvent event) {
				Button button = (Button) event.widget;
				if (button.getSelection()) {
					viewer.addFilter(ownGefaehrdungFilter);
					viewer.refresh();
				} else {
					viewer.removeFilter(ownGefaehrdungFilter);
					viewer.refresh();
					selectAssignedGefaehrdungen();
				}
			}
		});

		/* filter button - BSI Gefaehrdungen only */
		Button buttonGefaehrdungen = new Button(compositeFilter, SWT.CHECK);
		buttonGefaehrdungen.setText("nur BSI Gefährdungen anzeigen");
		GridData gridGefaehrdungen = new GridData();
		gridGefaehrdungen.horizontalSpan = 2;
		buttonGefaehrdungen.setLayoutData(gridGefaehrdungen);

		/* Listener adds/removes Filter gefaehrdungFilter*/
		buttonGefaehrdungen.addSelectionListener(new SelectionAdapter() {

			/**
			 * Adds/removes Filter depending on event.
			 * 
			 * @param event event containing information about the selection
			 */
			public void widgetSelected(SelectionEvent event) {
				Button button = (Button) event.widget;
				if (button.getSelection()) {
					viewer.addFilter(gefaehrdungFilter);
					viewer.refresh();
				} else {
					viewer.removeFilter(gefaehrdungFilter);
					viewer.refresh();
					selectAssignedGefaehrdungen();
				}
			}
		});

		/* filter button - search */
		new Label(compositeFilter, SWT.NULL).setText("suche:");
		Text textSearch = new Text(compositeFilter, SWT.SINGLE | SWT.BORDER);
		GridData dataSearch = new GridData();
		dataSearch.horizontalAlignment = SWT.FILL;
		textSearch.setLayoutData(dataSearch);

		/* Listener adds/removes Filter searchFilter */
		textSearch.addModifyListener(new ModifyListener() {

			/**
			 * Adds/removes Filter when Text is modified depending on event.
			 * 
			 * @param event event containing information about the selection
			 */
			public void modifyText(ModifyEvent event) {
				Text text = (Text) event.widget;
				if (text.getText().length() > 0) {
					
					ViewerFilter[] filters = viewer.getFilters();
					SearchFilter thisFilter = null;
					boolean contains = false;
					
					for (ViewerFilter item : filters) {
						if (item instanceof SearchFilter) {
							contains = true;
							thisFilter = (SearchFilter) item;
						}
					}
					if (contains) {
						/* filter is already active - update filter */
						thisFilter.setPattern(text.getText());
						viewer.refresh();
						
					} else {
						/* filter is not active - add */
						searchFilter.setPattern(text.getText());
						viewer.addFilter(searchFilter);
						viewer.refresh();
					}
				} else {
					viewer.removeFilter(searchFilter);
					viewer.refresh();
					selectAssignedGefaehrdungen();
				}
			}
			
		});
	}
	
	/**
	 * Marks all checkboxes of Gefaehrdungen that are selected as not okay.
	 */
	private void selectAssignedGefaehrdungen() {
		List<GefaehrdungsUmsetzung> gefaehrdungenToCheck
			= ((RiskAnalysisWizard)getWizard()).getNotOKGefaehrdungsUmsetzungen();
		List<Gefaehrdung> associatedGefaehrdungen =
			((RiskAnalysisWizard) getWizard()).getAssociatedGefaehrdungen();

		alleGefaehrdungen: for (Gefaehrdung gefaehrdung : associatedGefaehrdungen) {
				for (GefaehrdungsUmsetzung toCheck : gefaehrdungenToCheck) {
					if (gefaehrdung.getId().equals(toCheck.getId())) {
						viewer.setChecked(gefaehrdung, true);
						continue alleGefaehrdungen; 
				}
			}
		}		
	}

	/**
	 * Sets the control to the given visibility state.
	 * 
	 * @param visible boolean indicating if content should be visible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			initContents();
		}
	}

	/**
	 * Fills the CheckboxTableViewer with all previously selected
	 * Gefaehrdungen.
	 * Is processed each time the WizardPage is set visible.
	 */
	private void initContents() {
		wizard = ((RiskAnalysisWizard) getWizard());
		cleanUpAllGefaehrdungsUmsetzungen();
		List<Gefaehrdung> arrListAssociatedGefaehrdungen =
			wizard.getAssociatedGefaehrdungen();
		

		/* map a domain model object into multiple images and text labels */
		viewer.setLabelProvider(new CheckboxTableViewerLabelProvider());
		/* map domain model into array */
		viewer.setContentProvider(new ArrayContentProvider());
		/* associate domain model with viewer */
		viewer.setInput(arrListAssociatedGefaehrdungen);
		viewer.setSorter(new GefaehrdungenSorter());
		packAllColumns();
		
		checkPageComplete();
	}

	/**
	 * For repeated execution of the wizard:
	 * 
	 * Remove objects that were previously selected in this list, during the last execution of the wizard 
	 * but have been removed this time by the user on the previous page.
	 */
	private void cleanUpAllGefaehrdungsUmsetzungen() {

	List<Gefaehrdung> currentGefaehrdungen = wizard.getAssociatedGefaehrdungen();

		oldGefaehrdungen: for (GefaehrdungsUmsetzung oldGefaehrdung: wizard.getAllGefaehrdungsUmsetzungen()) {
			boolean umsetzungFound = false;
			for (Gefaehrdung currentGefaehrdung: currentGefaehrdungen) {
				if (oldGefaehrdung.getId().equals(currentGefaehrdung.getId())) {
					umsetzungFound = true;
					continue oldGefaehrdungen;
				}
			}
			if (!umsetzungFound) {
				wizard.getObjectsToDelete().add(oldGefaehrdung);
			}
		}
		
		wizard.removeOldObjects();
	
	}

	/**
	 * Adjusts all columns of the CheckboxTableViewer.
	 */
	private void packAllColumns() {
		checkboxColumn.pack();
		imageColumn.pack();
		numberColumn.pack();
		nameColumn.pack();
		descriptionColumn.pack();
	}

	/**
	 * Activates the next button, if the List of selected Gefaehrdungen is not empty.
	 */
	private void checkPageComplete() {
		if (((RiskAnalysisWizard) getWizard()).getAllGefaehrdungsUmsetzungen().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}

	/**
	 * Filter to extract all OwnGefaehrdungen in CheckboxTableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class OwnGefaehrdungenFilter extends ViewerFilter {

		/**
		 * Returns true, if the given element is an OwnGefaehrdung.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof OwnGefaehrdung)
				return true;
			return false;
		}
	}

	/**
	 * Filter to extract all Gefaehrdungen in CheckboxTableViewer.
	 * 
	 * @author ahanekop@sernet.de
	 */
	class GefaehrdungenFilter extends ViewerFilter {

		/**
		 * Returns true, if the given element is a Gefaehrdung.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (!(element instanceof OwnGefaehrdung)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Filter to extract all (Own)Gefaehrdungen matching a given String. 
	 *
	 * @author ahanekop@sernet.de
	 */
	class SearchFilter extends ViewerFilter {

		private Pattern pattern;

		/**
		 * Updates the Pattern.
		 * 
		 * @param searchString the String to search for
		 */
		void setPattern(String searchString) {
			pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
		}

		/**
		 * Selects all (Own)Gefaehrdungen matching the Pattern.
		 * 
		 * @param viewer the Viewer to operate on
		 * @param parentElement not used
		 * @param element given element
		 * @return true if element passes test, false else
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			Gefaehrdung gefaehrdung = (Gefaehrdung) element;
			String gefaehrdungTitle = gefaehrdung.getTitel();
			Matcher matcher = pattern.matcher(gefaehrdungTitle);

			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
		}
	}
}