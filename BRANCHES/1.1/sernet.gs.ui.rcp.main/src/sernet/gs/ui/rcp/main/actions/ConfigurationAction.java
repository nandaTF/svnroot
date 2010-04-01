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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.UsernameExistsException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveConfiguration;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.Property;

public class ConfigurationAction implements IObjectActionDelegate {

	private static final Logger LOG = Logger.getLogger(ConfigurationAction.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.personconfiguration";

	private static final String[] ALLOWED_ROLES = new String[] { ApplicationRoles.ROLE_ADMIN };

	private Configuration configuration;

	private IWorkbenchPart targetPart;

	private String oldPassword;
	
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
			MessageDialog.openWarning((Shell) targetPart.getAdapter(Shell.class), "Autorisierung", "Ihr Account ist nicht berechtigt, die gewählte Funktion auszuführen.");
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
				if (o == null || !(o instanceof Person)) {
					continue;
				}

				Person elmt = null;
				if (o instanceof Person) {
					elmt = (Person) o;
				}

				LOG.debug("Loading configuration for user " + elmt.getTitle());
				LoadConfiguration command = new LoadConfiguration(elmt);
				command = ServiceFactory.lookupCommandService().executeCommand(command);
				configuration = command.getConfiguration();

				if (configuration == null) {
					// create new configuration
					LOG.debug("No config found, creating new configuration object.");
					CreateConfiguration command2 = new CreateConfiguration(elmt);
					command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
					configuration = command2.getConfiguration();
				}

				entType = HitroUtil.getInstance().getTypeFactory().getEntityType(configuration.getEntity().getEntityType());
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Laden der Konfiguration");
			} catch (RuntimeException e) {
				ExceptionUtil.log(e, "Fehler beim Laden der Konfiguration");
			}
		}

		emptyPasswordField(configuration.getEntity());
		
		final BulkEditDialog dialog = new BulkEditDialog(window.getShell(), entType, true, "Benutzereinstellungen", configuration.getEntity());
		if (dialog.open() != Window.OK) {
			return;
		}

		final boolean updatePassword = updatePassword(configuration.getEntity());

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					Activator.inheritVeriniceContextState();

					// save configuration:
					SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, updatePassword);
					try {
						command = getCommandService().executeCommand(command);
					} catch (final UsernameExistsException e) {
						LOG.info("Configuration can not be saved. Username exists: " + e.getUsername());
						if (LOG.isDebugEnabled()) {
							LOG.debug("stacktrace: ", e);
						}
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(Display.getDefault().getActiveShell(),
										"Benutzername schon vergeben", "Der Account konnte nicht gespeichert werden. Benutzername '" + e.getUsername() + "' wird bereits verwendet. Wählen Sie einen anderen Benutzernamen.");
							}
						});	
					} catch (Exception e) {
						LOG.error("Error while saving configuration.", e);
						ExceptionUtil.log(e, "Error saving account configuration.");
					}
				}

			});
		} catch (Exception e) {
			LOG.error("Error while saving configuration.", e);
			ExceptionUtil.log(e, "Error saving account configuration.");
		} 
	}


	/**
	 * Remove (hashed) password from field, save hash in case user does NOT
	 * enter a new one.
	 * 
	 * @param entity
	 */
	private void emptyPasswordField(Entity entity) {
		Property passwordProperty = entity.getProperties(Configuration.PROP_PASSWORD).getProperty(0);
		if (passwordProperty != null) {
			oldPassword = passwordProperty.getPropertyValue();
			passwordProperty.setPropertyValue("", false);
		}
	}

	/**
	 * Checks if the user has entered a new password. If not, the previously
	 * saved hashed password is restored. If so, the cleartext password is
	 * saved.
	 * 
	 * @param entity
	 *            the entity containing the users input
	 * @return true if a new cleartext password was saved, that needs to be
	 *         hashed.
	 */
	private boolean updatePassword(Entity entity) {
		Property passwordProperty = entity.getProperties(Configuration.PROP_PASSWORD).getProperty(0);
		if (passwordProperty != null) {
			if (passwordProperty.getPropertyValue().length() > 0) {
				// new password:
				return true;
			} else {
				// no new password set, insert old one (hash) again:
				passwordProperty.setPropertyValue(oldPassword, false);
			}
		}
		return false;
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
			boolean b = CnAElementHome.getInstance().isOpen() && ServiceFactory.isPermissionHandlingNeeded() && AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });

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
