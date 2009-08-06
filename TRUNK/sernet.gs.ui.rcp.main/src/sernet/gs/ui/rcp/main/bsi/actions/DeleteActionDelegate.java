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
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Delete items on user request.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class DeleteActionDelegate implements IObjectActionDelegate {

	private IWorkbenchPart targetPart;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		Activator.inheritVeriniceContextState();

		final IStructuredSelection selection = ((IStructuredSelection) targetPart
				.getSite().getSelectionProvider().getSelection());

		if (!MessageDialog
				.openQuestion(
						(Shell) targetPart.getAdapter(Shell.class),
						"Wirklich löschen?",
						"Alle "
								+ selection.size()
								+ " markierten Elemente werden entfernt. Vorsicht: diese Operation "
								+ "kann nicht rückgängig gemacht werden! Stellen Sie sicher, dass Sie über " +
										"Backups der Datenbank verfügen.\n\n"
								+ "Wirklich löschen?")) {
			return;
		}

		// ask twice if IT verbund
		boolean goahead = true;
		boolean skipQuestion = false;
		Iterator iterator = selection.iterator();
		Object object;
		while (iterator.hasNext()) {
			if ((object = iterator.next()) instanceof ITVerbund) {
				if (!goahead)
					return;
				
				
				
				if (!MessageDialog
						.openQuestion(
								(Shell) targetPart.getAdapter(Shell.class),
								"IT-Verbund wirklich löschen?",
								"Sie haben den IT-Verbund " 
								+ ((ITVerbund)object).getTitel() +
								" zum Löschen markiert. "
										+ "Das wird alle darin enthaltenen Objekte entfernen "
										+ "(Server, Clients, Personen...)\n\n"
										+ "Wirklich wirklich löschen, wirklich?")) {
					skipQuestion = true;
					goahead = false;
					return;
				}
				else {
					skipQuestion = true;
				}
			}
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Lösche Objekte", selection
									.size());
							
							for (Iterator iter = selection.iterator(); iter
									.hasNext();) {
								Object sel = (Object) iter.next();

								if (sel instanceof IBSIStrukturElement
										|| sel instanceof BausteinUmsetzung
										|| sel instanceof FinishedRiskAnalysis
										|| sel instanceof GefaehrdungsUmsetzung
										|| sel instanceof ITVerbund) {

									// do not delete last ITVerbund:
									try {
										if (sel instanceof ITVerbund
												&& CnAElementHome.getInstance()
												.getItverbuende().size() < 2) {
											ExceptionUtil.log(new Exception("Letzter IT-Verbund kann nicht gelöscht werden.") , 
													"Sie haben versucht, den letzten IT-Verbund zu löschen. " +
													"Es muss immer ein IT-Verbund in der Datenbank verbleiben. " +
													"Wenn Sie diesen IT-Verbund löschen möchten, legen Sie zunächst einen neuen, leeren " +
											"IT-Verbund an.");
											return;
										}
									} catch (Exception e) {
										Logger.getLogger(this.getClass())
												.debug(e);
									}

									CnATreeElement el = (CnATreeElement) sel;

									try {
										monitor.setTaskName("Lösche: "
												+ el.getTitel());
										el.getParent().removeChild(el);
										CnAElementHome.getInstance().remove(el);
										monitor.worked(1);
										

									} catch (Exception e) {
										ExceptionUtil
												.log(e,
														"Fehler beim Löschen von Element.");
									}

								}
							}
							
							// notify all listeners:
							CnATreeElement child = (CnATreeElement) selection.iterator().next();
							CnAElementFactory.getLoadedModel().databaseChildRemoved(child);
						}
					});
		} catch (InvocationTargetException e) {
			ExceptionUtil.log(e, "Error while deleting object.");
		} catch (InterruptedException e) {
			ExceptionUtil.log(e, "Interrupted: Delete objects.");
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

}
