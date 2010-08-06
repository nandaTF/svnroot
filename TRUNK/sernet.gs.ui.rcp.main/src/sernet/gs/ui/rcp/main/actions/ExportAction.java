/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
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
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.actions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.LinkedList;

import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.ExportDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ExportCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.model.common.CnATreeElement;

/**
 * {@link Action} that exports selected objects from the
 * database to an XML file at the selected path. This uses
 * {@link ExportDialog} to retrieve user selections.
 */
public class ExportAction extends Action
{
	public static final String ID = "sernet.gs.ui.rcp.main.export"; //$NON-NLS-1$
	
	private static final Logger LOG = Logger.getLogger(ExportAction.class);
	
	private IWorkbenchWindow window;

	public ExportAction(IWorkbenchWindow window, String label)
	{
		this.window = window;
		setText(label);
		setId(ID);
	}
	
	/*
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public void run()
	{
		ExportDialog dialog = new ExportDialog(Display.getCurrent().getActiveShell());
		if( dialog.open() == Dialog.OK )
		{
			LinkedList<CnATreeElement> exportElements = new LinkedList<CnATreeElement>();
			exportElements.add(dialog.getSelectedITNetwork());
			ExportCommand exportCommand;
			
			if( dialog.isRestrictedToEntityTypes() )
			{
				exportCommand = new ExportCommand(exportElements, dialog.getSourceId(), dialog.getEntityTypesToBeExported());
			}
			else
			{
				exportCommand = new ExportCommand(exportElements, dialog.getSourceId());
			}
				
			
			try
			{
				exportCommand = ServiceFactory.lookupCommandService().executeCommand(exportCommand);
			}
			catch(CommandException ex)
			{
				LOG.error("Error while exporting.", ex);
			}
			
			try {
				IOUtils.write(exportCommand.getResult(),
						ExportAction.getExportOutputStream(dialog.getStorageLocation(), dialog.getEncryptOutput()));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			
		}
	}
	
	public static OutputStream getExportOutputStream(String path, boolean encryptOutput )
	{
		OutputStream os;
		try {
			os = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
		
		if (encryptOutput) {
			EncryptionDialog encDialog = new EncryptionDialog(Display.getDefault().getActiveShell());
			if (encDialog.open() == Dialog.OK) {
				IEncryptionService service = ServiceComponent.getDefault().getEncryptionService();
				
				try {
					EncryptionMethod encMethod = encDialog
							.getSelectedEncryptionMethod();
					if (encMethod == EncryptionMethod.PASSWORD) {
						os = service
								.encrypt(os, encDialog.getEnteredPassword());
					} else if (encMethod == EncryptionMethod.X509_CERTIFICATE) {
						os = service.encrypt(os, encDialog
								.getSelectedX509CertificateFile());
					}
				} catch (IOException ioe) {
					throw new IllegalArgumentException(ioe);
				} catch (CertificateNotYetValidException e) {
					throw new IllegalArgumentException(e);
				} catch (CertificateExpiredException e) {
					throw new IllegalArgumentException(e);
				} catch (CertificateException e) {
					throw new IllegalArgumentException(e);
				} catch (EncryptionException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
		
		return os;
	}

}
