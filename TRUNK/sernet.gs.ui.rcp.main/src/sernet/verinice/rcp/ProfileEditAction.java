/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccessControlEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.UserprofileDialog;

/**
 * {@link Action} that creates a dialog to modify the access rights of a
 * {@link CnATreeElement}.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * 
 */
public class ProfileEditAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.profileeditaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    public ProfileEditAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.USERPROFILE));
        setToolTipText(Messages.ProfileEditAction_0);
        window.getSelectionService().addSelectionListener(this);
        setRightID(ActionRightIDs.EDITPROFILE);
        setEnabled(checkRights());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();      
        final UserprofileDialog profiledialog = new UserprofileDialog(window.getShell());
        if (profiledialog.open() != Window.OK) {
            return;
        }      
    }

    public void dispose() {
        window.getSelectionService().removeSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        
    }

}
