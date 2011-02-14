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
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccountDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.PasswordException;
import sernet.gs.ui.rcp.main.service.commands.UsernameExistsException;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.SaveConfiguration;

public class ConfigurationAction implements IObjectActionDelegate {

	// FIXME: externalize strings
	
	private static final Logger LOG = Logger.getLogger(ConfigurationAction.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.personconfiguration"; //$NON-NLS-1$

	private static final String[] ALLOWED_ROLES = new String[] { ApplicationRoles.ROLE_ADMIN };

	private Configuration configuration;

	private IWorkbenchPart targetPart;
	
	ICommandService commandService;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Activator.inheritVeriniceContextState();

		// If this code is run then something is wrong, because the action
		// should have been
		// disabled programmatically. See method selectionChanged().
		boolean hasRole = AuthenticationHelper.getInstance().currentUserHasRole(ALLOWED_ROLES);
		if (!hasRole) {
			MessageDialog.openWarning((Shell) targetPart.getAdapter(Shell.class), Messages.ConfigurationAction_0, Messages.ConfigurationAction_1);
			return;
		}

		IWorkbenchWindow window = targetPart.getSite().getWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		if (selection == null) {
			return;
		}
		EntityType entType = null;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			try {
				Object o = iter.next();
				if( o instanceof CnATreeElement) {

    				CnATreeElement elmt = (CnATreeElement) o;
    
    				LOG.debug("Loading configuration for user " + elmt.getTitle()); //$NON-NLS-1$
    				LoadConfiguration command = new LoadConfiguration(elmt);
    				command = ServiceFactory.lookupCommandService().executeCommand(command);
    				configuration = command.getConfiguration();
    
    				if (configuration == null) {
    					// create new configuration
    					LOG.debug("No config found, creating new configuration object."); //$NON-NLS-1$
    					CreateConfiguration command2 = new CreateConfiguration(elmt);
    					command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
    					configuration = command2.getConfiguration();
    				}
    
    				entType = HitroUtil.getInstance().getTypeFactory().getEntityType(configuration.getEntity().getEntityType());
				}

				entType = HitroUtil.getInstance().getTypeFactory().getEntityType(configuration.getEntity().getEntityType());
			} catch (CommandException e) {
				ExceptionUtil.log(e, Messages.ConfigurationAction_2);
			} catch (RuntimeException e) {
				ExceptionUtil.log(e, Messages.ConfigurationAction_3);
			}
		}

		final AccountDialog dialog = new AccountDialog(window.getShell(), entType, true, Messages.ConfigurationAction_4, configuration.getEntity());
		if (dialog.open() != Window.OK) {
			return;
		}

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Activator.inheritVeriniceContextState();
					try {
						final boolean updatePassword = updateNameAndPassword(dialog.getUserName(), dialog.getPassword(),dialog.getPassword2());
						// save configuration:
						SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, updatePassword);			
						command = getCommandService().executeCommand(command);
					} catch (final UsernameExistsException e) {
						final String logMessage = "Configuration can not be saved. Username exists: " + e.getUsername(); //$NON-NLS-1$
						final String messageTitle = Messages.ConfigurationAction_7; 
						final String userMessage = NLS.bind(Messages.ConfigurationAction_7, e.getUsername());		
						handleException(e, logMessage, messageTitle, userMessage);	
					} catch (final PasswordException e) {
						final String logMessage = "Configuration can not be saved. " + e.getMessage(); //$NON-NLS-1$
						final String messageTitle = Messages.ConfigurationAction_6; 
						final String userMessage = e.getMessage();		
						handleException(e, logMessage, messageTitle, userMessage);	
					} catch (Exception e) {
						LOG.error("Error while saving configuration.", e); //$NON-NLS-1$
						ExceptionUtil.log(e, Messages.ConfigurationAction_5);
					}
				}

				private void handleException(final Exception e, final String logMessage, final String messageTitle, final String userMessage) {
					LOG.info(logMessage);
					if (LOG.isDebugEnabled()) {
						LOG.debug("stacktrace: ", e); //$NON-NLS-1$
					}
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(Display.getDefault().getActiveShell(),messageTitle, userMessage);
						}
					});
				}

			});
		} catch (Exception e) {
			LOG.error("Error while saving configuration.", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.ConfigurationAction_5);
		} 
	}

	/**
	 * Checks if the user has entered a new password. If so, the cleartext password is
	 * saved.
	 * @param string 
	 * 
	 * @param entity
	 *            the entity containing the users input
	 * @param string 
	 * @return true if a new cleartext password was saved, that needs to be
	 *         hashed.
	 */
	private boolean updateNameAndPassword(String name, String newPassword, String newPassword2) {
		boolean updated = false;
		final String oldName = configuration.getUser();
		if(isNewName(oldName,name) && (newPassword==null || newPassword.isEmpty())) {
			// Exception message must be translated here
			throw new PasswordException(Messages.ConfigurationAction_9);
		}	
		configuration.setUser(name);
		if(newPassword!=null && !newPassword.isEmpty()) {
			if(!newPassword.equals(newPassword2)) {
				// Exception message must be translated here
				throw new PasswordException(Messages.ConfigurationAction_10);
			}
			configuration.setPass(newPassword);
			updated=true;
		}
		return updated;
	}

	private boolean isNewName(String oldName, String name) {
		boolean result=false;
		if(oldName!=null) {
			if(name==null) {
				result=true;
			} else {
				result = !oldName.equals(name);
			}
		} else {
			result = name!=null;
		}
		return result;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (action.isEnabled()) {
			// Conditions for availability of this action:
			// - Database connection must be open (Implicitly assumes that login
			// credentials have
			// been transferred and that the server can be queried. This is
			// neccessary since this
			// method will be called before the server connection is enabled.)
			// - permission handling is needed by IAuthService implementation
			// - user has administrator privileges
			boolean b = CnAElementHome.getInstance().isOpen() 
			    && ServiceFactory.isPermissionHandlingNeeded() 
			    && AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });

			action.setEnabled(b);
		}
	}
	
	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = createCommandServive();
		}
		return commandService;
	}

	private ICommandService createCommandServive() {
		return ServiceFactory.lookupCommandService();
	}

}
