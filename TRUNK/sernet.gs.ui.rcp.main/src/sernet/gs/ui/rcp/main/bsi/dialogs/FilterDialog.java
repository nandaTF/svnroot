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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

/**
 * Top level Dialog class for filter dialogs. Provides methods to create
 * selections for commonly used filter patterns.
 * 
 * @author koderman@sernet.de
 *
 */
public abstract class FilterDialog extends Dialog {

	protected HashMap<String, Button> typeFieldsUmsetzung;
	protected HashMap<String, Button> typeFieldsSiegel;
	protected HashMap<String, Button> typeFieldsSchicht;
	
	protected HashSet<String> selectedUmsetzungTypes;
	protected HashSet<String> selectedSiegelTypes;
	protected HashSet<String> selectedSchichtTypes;

		public FilterDialog(Shell parent,
				String[] umsetzung, 
				String[] siegel,
				String[] schicht) {
			super(parent);
			this.selectedUmsetzungTypes = new HashSet<String>();
			this.selectedSiegelTypes = new HashSet<String>();
			this.selectedSchichtTypes = new HashSet<String>();
			
			if (umsetzung != null) {
				for (String type : umsetzung) {
					this.selectedUmsetzungTypes.add(type);
				}
			}

			if (siegel != null) {
				for (String type : siegel) {
					this.selectedSiegelTypes.add(type);
				}
			}

			if (schicht != null) {
				for (String type : schicht) {
					this.selectedSchichtTypes.add(type);
				}
			}
		}
	

	public String[] getUmsetzungSelection() {
		return (String[]) this.selectedUmsetzungTypes
		.toArray(new String[this.selectedUmsetzungTypes.size()]);
	}

	public String[] getSiegelSelection() {
		return (String[]) this.selectedSiegelTypes
		.toArray(new String[this.selectedSiegelTypes.size()]);
	}
	
	public String[] getSchichtSelection() {
		return (String[]) this.selectedSchichtTypes
		.toArray(new String[this.selectedSchichtTypes.size()]);
	}
	
	

	protected void createUmsetzungCheckboxes(Composite parent) {
		typeFieldsUmsetzung = new HashMap<String, Button>();
		String[] umsetzungStati = MassnahmenUmsetzung.getUmsetzungStati();
		PropertyType propertyType = HUITypeFactory.getInstance()
			.getEntityType(MassnahmenUmsetzung.TYPE_ID)
			.getPropertyType(MassnahmenUmsetzung.P_UMSETZUNG);
		
		for (final String status : umsetzungStati) {
			final Button button = new Button(parent, SWT.CHECK);
			if (status.equals(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET))
				// "not edited" is not really a status, but rather the lack of one:
				button.setText("unbearbeitet");
			else
				button.setText(propertyType.getOption(status).getName());
			typeFieldsUmsetzung.put(status, button);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (button.getSelection())
						selectedUmsetzungTypes.add(status);
					else
						selectedUmsetzungTypes.remove(status);
				}
			});
		}
	}

	protected void createSiegelCheckboxes(Composite parent) {
		typeFieldsSiegel = new HashMap<String, Button>();
		String[] siegelStufen = MassnahmenUmsetzung.getStufen();
		
		for (final String stufe : siegelStufen) {
			final Button button = new Button(parent, SWT.CHECK);
			button.setText(stufe);
			typeFieldsSiegel.put(stufe, button);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (button.getSelection())
						selectedSiegelTypes.add(stufe);
					else
						selectedSiegelTypes.remove(stufe);
				}
			});
		}
	}
	
	protected void createSchichtCheckboxes(Composite parent) {
		typeFieldsSchicht = new HashMap<String, Button>();
		String[] schichten = BausteinUmsetzung.getSchichten();
		String[] bezeichnung = BausteinUmsetzung.getSchichtenBezeichnung();
		
		int i=0;
		for (final String schicht : schichten) {
			final Button button = new Button(parent, SWT.CHECK);
			button.setText(bezeichnung[i++]);
			typeFieldsSchicht.put(schicht, button);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if (button.getSelection())
						selectedSchichtTypes.add(schicht);
					else
						selectedSchichtTypes.remove(schicht);
				}
			});
		}
	}
	
	protected void initContent() {
		if (typeFieldsUmsetzung != null) {
			String[] umsetzungStati = MassnahmenUmsetzung.getUmsetzungStati();
			for (String umsetzung : umsetzungStati) {
				Button button = typeFieldsUmsetzung.get(umsetzung);
				button.setSelection(selectedUmsetzungTypes.contains(umsetzung));
			}
		}

		if (typeFieldsSiegel != null) {
			String[] siegelStati = MassnahmenUmsetzung.getStufen();
			for (String stufe : siegelStati) {
				Button button = typeFieldsSiegel.get(stufe);
				button.setSelection(selectedSiegelTypes.contains(stufe));
			}
		}
		
		if (typeFieldsSchicht != null) {
			String[] schichten = BausteinUmsetzung.getSchichten();
			for (String schicht : schichten) {
				Button button = typeFieldsSchicht.get(schicht);
				button.setSelection(selectedSchichtTypes.contains(schicht));
			}
		}
		
	}


	protected Group createSiegelGroup(Composite parent) {
		Group boxesComposite2 = new Group(parent, SWT.BORDER);
		boxesComposite2.setText("Siegelstufe");
		GridData gridData2 = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		boxesComposite2.setLayoutData(gridData2);
		GridLayout layout3 = new GridLayout();
		layout3.numColumns = 4;
		boxesComposite2.setLayout(layout3);
		return boxesComposite2;
	}


	protected Group createUmsetzungGroup(Composite parent) {
		Group boxesComposite = new Group(parent, SWT.BORDER);
		boxesComposite.setText("Umsetzung");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		boxesComposite.setLayoutData(gridData);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 5;
		boxesComposite.setLayout(layout2);
		return boxesComposite;
	}
	
	protected Group createSchichtenGroup(Composite parent) {
		Group boxesComposite = new Group(parent, SWT.BORDER);
		boxesComposite.setText("Schicht");
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER,
				true, false, 2, 1);
		boxesComposite.setLayoutData(gridData);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 5;
		boxesComposite.setLayout(layout2);
		return boxesComposite;
	}

}
