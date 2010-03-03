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
package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.IInternalServer;

/**
 * Main preference page for CnA Tool Settings.
 * 
 * @author akoderman[at]sernet[dot]de
 *
 */
public class KatalogePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	private static final Logger log = Logger.getLogger(KatalogePreferencePage.class);

	private FileFieldEditor zipfilePath;
	private FileFieldEditor datenschutzZipPath;
	private RadioGroupFieldEditor gsAccessMethod;
	private DirectoryFieldEditor bsiUrl;
	

	public KatalogePreferencePage() {
		super(GRID);
		
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("KatalogePreferencePage.0") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.1") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.2") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.3") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.4")); //$NON-NLS-1$
		
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		gsAccessMethod = new RadioGroupFieldEditor(PreferenceConstants.GSACCESS,
				Messages.getString("KatalogePreferencePage.5"), //$NON-NLS-1$
				1,
				new String[][] {
					{Messages.getString("KatalogePreferencePage.6"), PreferenceConstants.GSACCESS_DIR},  //$NON-NLS-1$
					{Messages.getString("KatalogePreferencePage.7"), PreferenceConstants.GSACCESS_ZIP} //$NON-NLS-1$
				},
				getFieldEditorParent());
		addField(gsAccessMethod);
		
		zipfilePath = new FileFieldEditor(PreferenceConstants.BSIZIPFILE, 
				Messages.getString("KatalogePreferencePage.8"), //$NON-NLS-1$
				getFieldEditorParent());
		zipfilePath.setFileExtensions(new String[] {"*.zip;*.ZIP", "*.*"});
		addField(zipfilePath);

		bsiUrl = new DirectoryFieldEditor(PreferenceConstants.BSIDIR,
				Messages.getString("KatalogePreferencePage.9"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(bsiUrl);

		datenschutzZipPath = new FileFieldEditor(PreferenceConstants.DSZIPFILE, 
				Messages.getString("KatalogePreferencePage.10"), //$NON-NLS-1$
				getFieldEditorParent());
		datenschutzZipPath.setFileExtensions(new String[] {"*.zip;*.ZIP", "*.*"});
		addField(datenschutzZipPath);

		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		
		final Link link = new Link(parent, SWT.NONE);
		link.setText(Messages.getString("KatalogePreferencePage.11") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.12") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.13") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.14") + //$NON-NLS-1$
				Messages.getString("KatalogePreferencePage.15")); //$NON-NLS-1$
		
		link.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				Program.launch(event.text);
			}

		});

		return super.createContents(parent);
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
			IInternalServer internalServer = Activator.getDefault().getInternalServer();
			Preferences prefs = Activator.getDefault().getPluginPreferences();
			String accessMethod = prefs.getString(PreferenceConstants.GSACCESS);
			
			if (event.getSource() == zipfilePath && accessMethod.equals(PreferenceConstants.GSACCESS_ZIP))
			{
				try {
					internalServer.setGSCatalogURL(
							new File(zipfilePath.getStringValue()).toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("GS catalog zip file path is an invalid URL.");
				}
			}
			else if (event.getSource() == bsiUrl && accessMethod.equals(PreferenceConstants.GSACCESS_DIR))
			{
				try {
					internalServer.setGSCatalogURL(
							new File(bsiUrl.getStringValue()).toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("GS catalog directory path is an invalid URL.");
				}
			}
			else if (event.getSource() == datenschutzZipPath)
			{
				try {
					internalServer.setDSCatalogURL(
							new File(datenschutzZipPath.getStringValue()).toURI().toURL());
				} catch (MalformedURLException e) {
					log.warn("DS catalog zip file path is an invalid URL.");
				}
			}
			else if (event.getSource() == gsAccessMethod)
			{
				if (accessMethod.equals(PreferenceConstants.GSACCESS_DIR))
				{
					try {
						internalServer.setGSCatalogURL(
								new File(prefs.getString(PreferenceConstants.BSIDIR)).toURI().toURL());
					} catch (MalformedURLException e) {
						log.warn("GS catalog directory path is an invalid URL.");
					}
				}
				else
				{
					try {
						internalServer.setGSCatalogURL(
								new File(prefs.getString(PreferenceConstants.BSIZIPFILE)).toURI().toURL());
					} catch (MalformedURLException e) {
						log.warn("GS catalog zip file path is an invalid URL.");
					}
				}
				
			}
		}
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid())
			return;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// only editable when server is not used, client has direct access to GS catalogues
		// otherwise server is used to access gs catalogue data to ensure that all clients are
		// working on the same data
		if (visible) {
			String opmode = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE);
			setEnabledFields(opmode.equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER));
		}
	}
	
	private void setEnabledFields(boolean enable) {
		bsiUrl.setEnabled(enable, getFieldEditorParent());
		datenschutzZipPath.setEnabled(enable, getFieldEditorParent());
		gsAccessMethod.setEnabled(enable, getFieldEditorParent());
		zipfilePath.setEnabled(enable, getFieldEditorParent());
		
		if (enable) {
			setMessage(null);
		}
		else {
			setMessage("Die Kataloge werden vom Server bezogen und müssen hier nicht konfiguriert werden.");
		}
	}
	
	@Override
	public boolean isValid() {
		// always allow user to navigate away from page:
		return true;
	}
	
}
