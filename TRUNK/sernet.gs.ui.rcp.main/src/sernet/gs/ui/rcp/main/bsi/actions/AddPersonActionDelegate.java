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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class AddPersonActionDelegate implements IObjectActionDelegate {

private IWorkbenchPart targetPart;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	public void run(IAction action) {
		try {
			Object sel = ((IStructuredSelection)targetPart.getSite()
					.getSelectionProvider().getSelection()).getFirstElement();
			
			CnATreeElement newElement=null;
			if (sel instanceof PersonenKategorie) {
				PersonenKategorie kat = (PersonenKategorie) sel;
				newElement = CnAElementFactory.getInstance().saveNew(kat, Person.TYPE_ID, null);
			}
			
			if (newElement != null)
				EditorFactory.getInstance().openEditor(newElement);
		}
		catch (Exception e) {
			ExceptionUtil.log(e, "Konnte Person nicht hinzufügen.");
		}
	
	}
	public void selectionChanged(IAction action, ISelection selection) {

	}

}
