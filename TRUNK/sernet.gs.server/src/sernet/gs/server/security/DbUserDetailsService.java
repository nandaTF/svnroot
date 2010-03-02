/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.server.commands.LoadUserConfiguration;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;

/**
 * Provides access to user details in the verinice database.
 * These can be created by any admin-user in the verinice frontend itself.
 * 
 * Additionally, one initial user can be configured in applicationContext.xml itself,
 * as a backup administrative account and for initial setting up of the database.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DbUserDetailsService implements UserDetailsService {
	
	private ICommandService commandService;

	// injected by spring
	private LoadUserConfiguration loadUserConfigurationCommand;
	
	// injected by spring
	private String adminuser = "";

	// injected by spring
	private String adminpass = "";

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		if (adminuser.length() > 0 && adminpass.length() > 0
				&& username.equals(adminuser))
			return defaultUser();
		
		Logger.getLogger(this.getClass()).debug("Loading user from DB: " + username);

		try {
			commandService.executeCommand(loadUserConfigurationCommand);
		} catch (CommandException e) {
			throw new RuntimeException("Failed to retrieve user configurations.", e);
		}
		
		List<Entity> entities = loadUserConfigurationCommand.getEntities();

		for (Entity entity : entities) {
			if (isUser(username, entity)) {
				return databaseUser(entity);
			}
		}
		throw new UsernameNotFoundException(Messages
				.getString("DbUserDetailsService.4")); //$NON-NLS-1$
	}

	private UserDetails defaultUser() {
		VeriniceUserDetails user = new VeriniceUserDetails(adminuser, adminpass);
		user.addRole(ApplicationRoles.ROLE_ADMIN);
		user.addRole(ApplicationRoles.ROLE_USER);
		return user;
	}

	private UserDetails databaseUser(Entity entity) {
		VeriniceUserDetails userDetails = new VeriniceUserDetails(entity
				.getSimpleValue(Configuration.PROP_USERNAME), entity
				.getSimpleValue(Configuration.PROP_PASSWORD));
		
		// All non-privileged users have the role "ROLE_USER".
		userDetails.addRole(ApplicationRoles.ROLE_USER);
		
		// if set in the entity, the user may also have the admin role:
		if (entity.isSelected(Configuration.PROP_ISADMIN, "configuration_isadmin_yes"))
			userDetails.addRole(ApplicationRoles.ROLE_ADMIN);
			
		
		return userDetails;
	}

	private boolean isUser(String username, Entity entity) {
		return entity.getSimpleValue(Configuration.PROP_USERNAME).equals(
				username);

	}

	public void setAdminuser(String adminuser) {
		this.adminuser = adminuser;
	}

	public void setAdminpass(String adminpass) {
		this.adminpass = adminpass;
	}

	public LoadUserConfiguration getLoadUserConfigurationCommand() {
		return loadUserConfigurationCommand;
	}

	public void setLoadUserConfigurationCommand(
			LoadUserConfiguration loadUserConfigurationCommand) {
		this.loadUserConfigurationCommand = loadUserConfigurationCommand;
	}

	public ICommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

}
