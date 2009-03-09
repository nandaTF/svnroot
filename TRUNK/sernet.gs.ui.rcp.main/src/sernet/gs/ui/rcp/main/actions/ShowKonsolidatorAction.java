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
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.Application;
import sernet.gs.ui.rcp.main.ApplicationActionBarAdvisor;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ICommandIds;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.KonsolidatorDialog;
import sernet.gs.ui.rcp.main.bsi.editors.EditorRegistry;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.Konsolidator;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.wizards.ExportWizard;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.KonsolidatorCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

public class ShowKonsolidatorAction extends Action implements
		ISelectionListener {

	public static final String ID = "sernet.gs.ui.rcp.main.actions.showkonsolidatoraction";

	private final IWorkbenchWindow window;

	public ShowKonsolidatorAction(IWorkbenchWindow window, String label) {
		this.window = window;
		setText(label);
		setId(ID);
		//setActionDefinitionId(ID);
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(
				ImageCache.KONSOLIDATOR));
		window.getSelectionService().addSelectionListener(this);
		setToolTipText("Gleiche Bausteine und ihre Maßnahmen konsolidieren.");
	}

	public void run() {
		IStructuredSelection selection = (IStructuredSelection) window
				.getSelectionService().getSelection(BsiModelView.ID);
		if (selection == null)
			return;
		final List<BausteinUmsetzung> selectedElements = new ArrayList<BausteinUmsetzung>();
		EntityType entType = null;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof BausteinUmsetzung)
				selectedElements.add((BausteinUmsetzung) o);
		}

		final KonsolidatorDialog dialog = new KonsolidatorDialog(window
				.getShell(), selectedElements);
		if (dialog.open() != InputDialog.OK
				|| dialog.getSource() == null)
			return;

		if (!KonsolidatorDialog.askConsolidate(window.getShell()))
			return;

		try {
			// close editors first:
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().closeAllEditors(true /* ask save */);

			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.setTaskName("Konsolidiere Bausteine...");
							monitor.beginTask("Konsolidator", selectedElements
									.size() + 1);
							
							BausteinUmsetzung source = dialog.getSource();
							
							try {
								// change targets on server:
								KonsolidatorCommand command = new KonsolidatorCommand(selectedElements, source);
								command = ServiceFactory.lookupCommandService()
										.executeCommand(command);
								
								// reload state from server:
								for (BausteinUmsetzung bausteinUmsetzung : selectedElements) {
									CnAElementFactory.getLoadedModel().databaseChildChanged(bausteinUmsetzung);
								}
								
							} catch (CommandException e) {
								ExceptionUtil.log(e, "Fehler beim konsolidieren.");
							}
							
							
							
							monitor.done();
						}
					});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Error executing bulk edit.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Aborted.");
		}
	}

	/**
	 * Action is enabled when only items of the same type are selected.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection input) {
		if (input instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) input;
			
			if (selection.size() < 2) {
				setEnabled(false);
				return;
			}
			
			String kapitel = null;
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object o = iter.next();
				 if (o instanceof BausteinUmsetzung) {
					 BausteinUmsetzung bst = (BausteinUmsetzung) o;
					 if (kapitel == null) {
						 kapitel = bst.getKapitel();
					 } else {
						 if (!bst.getKapitel().equals(kapitel)) {
							 setEnabled(false);
							 return;
						 }
					 }
				 } else {
					 setEnabled(false);
					 return;
				 }
			}
			setEnabled(true);
			return;
		}
		// no structured selection:
		setEnabled(false);
	}

	private void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
}
