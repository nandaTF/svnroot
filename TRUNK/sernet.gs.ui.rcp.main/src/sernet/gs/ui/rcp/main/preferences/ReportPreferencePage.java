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
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman@sernet.de
 *
 */
public class ReportPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {


	private DirectoryFieldEditor ooDir;
	private FileFieldEditor ooTemplate;
	private FileFieldEditor ooDocumentTemplate;

	public ReportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("ReportPreferencePage.0") + //$NON-NLS-1$
				Messages.getString("ReportPreferencePage.1")); //$NON-NLS-1$
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		ooDir = new DirectoryFieldEditor(PreferenceConstants.OODIR,
				Messages.getString("ReportPreferencePage.2"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(ooDir);
		
		ooTemplate = new FileFieldEditor(PreferenceConstants.OOTEMPLATE, 
				Messages.getString("ReportPreferencePage.3"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(ooTemplate);

		ooDocumentTemplate = new FileFieldEditor(PreferenceConstants.OOTEMPLATE_TEXT, 
				Messages.getString("ReportPreferencePage.4"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(ooDocumentTemplate);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE))
			checkState();
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid())
			return;
		
		setValid(true);
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}
