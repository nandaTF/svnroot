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
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccessControlEditDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.PasswordDialog;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.ChangeOwnPassword;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Action to allow users to change their own password if stored in the verinice DB.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeOwnPasswordAction extends Action  {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.changeownpasswordaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    // TODO externalize strings
    
    public ChangeOwnPasswordAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
        setToolTipText("Change your own password for verinice.");
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        // this action works for normal users, admins are supposed to change their password differently, since admin acounts can also be defined in the config file
        // where they cannot be edited from within the application.
        // (admins can change the passwords for anybody, this action here only works for the currently logged in user)
        boolean isAdmin = AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });
        if (isAdmin) {
            MessageDialog.openInformation(window.getShell(), "Administrator", "You are an administrator. For security reasons you have to change your password settings in your user's account settings or - in case of the fallback administrator account - in the configuration file of the verinice.PRO server.");
            return;
        }
        
        Activator.inheritVeriniceContextState();
        PasswordDialog passwordDialog = new PasswordDialog(this.window.getShell());
        if (passwordDialog.open() == Window.OK) {
            ChangeOwnPassword command = new ChangeOwnPassword(passwordDialog.getPassword());
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
            } catch (CommandException e) {
                ExceptionUtil.log(e, "Could not change password.");
            }
        }
    }


}
