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
 *     Robert Schuster <r.schuster@tarent.de> - rewritten to use set of classes
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.commands.CommandException;

/**
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class BSIModelFilterDialog extends FilterDialog {

	private static final Logger log = Logger.getLogger(BSIModelFilterDialog.class);

	private String lebenszyklus = "";
	private Combo combo;
	private Set<Class<?>> filteredClasses;
	private Combo comboObjektLZ;
	private String objektLebenszyklus = "";
	private CheckboxTableViewer viewer;
	private String[] tagPattern;
	private Composite container;
	private Group tagGroup;
	
	private static HashMap<String, Class<?>> possibleFilters = new HashMap<String, Class<?>>();
	
	static
	{
		// Initializes the set of classes which can be filtered. The key to the classes
		// is the label of the button that allows switching the filter.
		// Note: If the labels change. This part must be adjusted as well. The best idea
		// is to use the i18n and use the same string for the button and this code.
		possibleFilters.put("Bausteinzuordnungen", BausteinUmsetzung.class);
		possibleFilters.put("Maßnahmenumsetzungen", MassnahmenUmsetzung.class);
		//possibleFilters.put("Verknüpfungen", LinkKategorie.class); 
	}
	
	private static final String[] LZ_ITEMS = new String[] {
		"<alle>",
		"Planung",
		"Beschaffung",
		"Umsetzung",
		"Betrieb",
		"Aussonderung",
		"Notfallvorsorge"
	};
	
	private static final String[] LZ_ZIELOBJEKTE_ITEMS = new String[] {
		"<alle>",
         "Betrieb",
         "Planung" ,
         "Grundinstallation", 
         "Konfiguration" ,
         "Test" ,
         "Auslieferung", 
         "Reparatur",
         "Standby",
         "Reserve"
	};
	private String[] checkedElements;


	public BSIModelFilterDialog(Shell parent,
			String[] umsetzung, 
			String[] siegel,
			String lebenszyklus,
			String objektLebenszyklus,
			Set<Class<?>> filteredClasses, 
			String[] tags) {
		super(parent, umsetzung, siegel, null);
		setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.lebenszyklus = lebenszyklus;
		this.objektLebenszyklus = objektLebenszyklus;
		this.filteredClasses = filteredClasses;
		if (this.filteredClasses == null)
			this.filteredClasses = new HashSet<Class<?>>();
		this.tagPattern = tags;
		
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		
		Label intro = new Label(container, SWT.NONE);
		intro.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false, 2, 1));
		intro.setText("Filtern nach folgenden Kriterien:");
		
		Group boxesComposite = createUmsetzungGroup(container);
		Group boxesComposite2 = createSiegelGroup(container);
		
		createLebenszyklusDropDown(container);
		createObjektLebenszyklusDropDown(container);
		
		createUmsetzungCheckboxes(boxesComposite);
		createSiegelCheckboxes(boxesComposite2);
		
		Group group = createAusblendenGroup(container);
		createAusblendenCheckboxes(group);
		
		tagGroup = createTagfilterGroup(container);
		
		initContent();
		container.layout();
		return container;
	}
	
	private Group createTagfilterGroup(Composite parent) {
		Group groupComposite = new Group(parent, SWT.BORDER);
		groupComposite.setText("Nach Tag selektieren");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		groupComposite.setLayoutData(gridData);
		groupComposite.setLayout(new GridLayout(1, false));
		
		ScrolledComposite comp = new ScrolledComposite(groupComposite, SWT.V_SCROLL);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setExpandHorizontal(true);

		viewer = CheckboxTableViewer.newCheckList(comp, SWT.BORDER);
		Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		
		comp.setContent(viewer.getControl());
		
		// workaround to prevent tableviewer size from exceeding shell size:
		comp.setMinSize(100,100);

		TableColumn checkboxColumn = new TableColumn(table, SWT.LEFT);
		checkboxColumn.setText("");
		checkboxColumn.setWidth(35);

		TableColumn imageColumn = new TableColumn(table, SWT.LEFT);
		imageColumn.setText("Tag");
		imageColumn.setWidth(100);
		
		viewer.setContentProvider(new ArrayContentProvider());
		
		viewer.setLabelProvider(new ITableLabelProvider() {

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 1)
					return (String) element;
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
		
		return groupComposite;
	}
	
	public String[] getCheckedElements() {
		return checkedElements;
	}


	private Group createAusblendenGroup(Composite parent) {
		Group boxesComposite = new Group(parent, SWT.BORDER);
		boxesComposite.setText("Ausblenden");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		boxesComposite.setLayoutData(gridData);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		boxesComposite.setLayout(layout2);
		return boxesComposite;
	
	}
	
	private boolean getFilterSelectionForButton(Button b)
	{
		return filteredClasses.contains(possibleFilters.get(b.getText()));
	}
	
	private class SelectionHelper extends SelectionAdapter
	{
		private Button b;
		
		SelectionHelper(Button b)
		{
			this.b = b;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (b.getSelection())
				filteredClasses.add(possibleFilters.get(b.getText()));
			else
				filteredClasses.remove(possibleFilters.get(b.getText()));
		}
	}
	
private void createAusblendenCheckboxes(Group parent) {
		
		final Button button1 = new Button(parent, SWT.CHECK);
		button1.setText("Bausteinzuordnungen");
		button1.setSelection(getFilterSelectionForButton(button1));
		button1.addSelectionListener(new SelectionHelper(button1));
		
		final Button button2 = new Button(parent, SWT.CHECK);
		button2.setText("Maßnahmenumsetzungen");
		button2.setSelection(getFilterSelectionForButton(button2));
		button2.addSelectionListener(new SelectionHelper(button2));
		
//		final Button button3 = new Button(parent, SWT.CHECK);
//		button3.setText("Verknüpfungen");
//		button3.setSelection(getFilterSelectionForButton(button3));
//		button3.addSelectionListener(new SelectionHelper(button3));
			
	}

	private void createLebenszyklusDropDown(Composite container) {
		Label label = new Label(container, SWT.None);
		label.setText("Maßnahmen für Lebenszyklus");
		label.pack();
		
		combo = new Combo(container, SWT.NONE);
		combo.setItems(LZ_ITEMS);
		combo.setText(lebenszyklus==null ? "" : lebenszyklus);
		combo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setLZ();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createObjektLebenszyklusDropDown(Composite container) {
		Label label = new Label(container, SWT.None);
		label.setText("Objekte in Lebenszyklus");
		label.pack();
		
		comboObjektLZ = new Combo(container, SWT.NONE);
		comboObjektLZ.setItems(LZ_ZIELOBJEKTE_ITEMS);
		comboObjektLZ.setText(objektLebenszyklus==null ? "" : objektLebenszyklus);
		comboObjektLZ.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setObjektLZ();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void setLZ() {
		if (combo.getSelectionIndex() == 0)
			lebenszyklus = "";
		else 
			this.lebenszyklus = LZ_ITEMS[combo.getSelectionIndex()];
	}

	private void setObjektLZ() {
		if (comboObjektLZ.getSelectionIndex() == 0)
			objektLebenszyklus = "";
		else 
			objektLebenszyklus = LZ_ZIELOBJEKTE_ITEMS[comboObjektLZ.getSelectionIndex()];
	}
	
	public String getLebenszyklus() {
		return lebenszyklus;
	}
	
	
	protected void initContent() {
		super.initContent();
		if (CnAElementFactory.isModelLoaded()) {
			List<String> tags;
			try {
				tags = CnAElementHome.getInstance().getTags();
				tags.add(0, TagFilter.NO_TAG);
				viewer.setInput(tags);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Konnte Tags für Filter nicht laden.");
			}
			
			// workaround to prevent tableviewer size from exceeding shell size:
			viewer.getTable().setSize(200,200);
			
			if (tagPattern != null)
				viewer.setCheckedElements(tagPattern);
			tagGroup.getParent().layout(true);
		}
		
	}
	
	@Override
	public boolean close() {
		// get checked objects, cast to string:
		List<Object> tagList = Arrays.asList(viewer.getCheckedElements());
		this.checkedElements = (String[]) tagList.toArray(new String[tagList.size()]);
		return super.close();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Filter Einstellungen");
		
		// workaround to prevent tableviewer size from exceeding shell size:
		newShell.setSize(400,500);
	}


	public Set<Class<?>> getFilteredClasses() {
		return this.filteredClasses;
	}


	public String getObjektLebenszyklus() {
		return objektLebenszyklus;
	}
}
