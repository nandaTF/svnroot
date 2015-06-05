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
package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

public class AddBausteinActionDelegate extends AddModulandMethodActionDelegate {
    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

      public void run(IAction action) {
    	  
    	  try {
             
    		  Object sel = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection()).getFirstElement();
                 if (sel instanceof CnATreeElement) {
                     boolean inheritIcon = Activator.getDefault().getPreferenceStore()
                             .getBoolean(PreferenceConstants.INHERIT_SPECIAL_GROUP_ICON);
                  CnATreeElement parent = (CnATreeElement) sel;
                  CnATreeElement  newBaustein = CnAElementFactory.getInstance().saveNew(parent, BausteinUmsetzung.TYPE_ID, null, inheritIcon);
            	  
                  EditorFactory.getInstance().openEditor(newBaustein);
              }
					
              
             
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.AddBausteinActionDelegate_0);
        }
    }
}


