/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.server.security;

import java.util.ArrayList;
import java.util.List;

import javax.naming.ldap.InitialLdapContext;

import org.richfaces.iterator.ForEachIterator;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.providers.ldap.LdapAuthenticator;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.server.commands.LoadUserConfiguration;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.configuration.Configuration;

public class LdapAuthenticatorImpl implements LdapAuthenticator {
    
    // injected by spring
    private DefaultSpringSecurityContextSource contextFactory;
    
    // injected by spring
    private String principalPrefix = "";
    
    // injected by spring
    private String guestUser = "";
    
    // injected by spring
    private String adminuser = "";
    
    // injected by spring
    private String passwordRealm ="";

    /**
     * @param passwordRealm the passwordRealm to set
     */
    public void setPasswordRealm(String passwordRealm) {
        this.passwordRealm = passwordRealm;
    }

    /**
     * @param adminuser the adminuser to set
     */
    public void setAdminuser(String adminuser) {
        this.adminuser = adminuser;
    }

    /**
     * @param adminpass the adminpass to set
     */
    public void setAdminpass(String adminpass) {
        this.adminpass = adminpass;
    }

    // injected by spring
    private String adminpass = "";

    
    // injected by spring
    private ICommandService commandService;

    // injected by spring
    private LoadUserConfiguration loadUserConfigurationCommand;
    
    /**
     * @param commandService the commandService to set
     */
    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * @param loadUserConfigurationCommand the loadUserConfigurationCommand to set
     */
    public void setLoadUserConfigurationCommand(LoadUserConfiguration loadUserConfigurationCommand) {
        this.loadUserConfigurationCommand = loadUserConfigurationCommand;
    }

    public DirContextOperations authenticate(Authentication authentication) {

        // Grab the username and password out of the authentication object.
        String username = authentication.getName();
        String principal = principalPrefix + username;
        String password = "";
        if (authentication.getCredentials() != null) {
            password = authentication.getCredentials().toString();
        }
        
        // If we have a valid username and password, try to authenticate.
        if (!("".equals(principal.trim())) && !("".equals(password.trim()))) {
            
            // compare against the admin definied in the config file:
            if (adminuser.length() > 0 && adminpass.length() > 0
                    && username.equals(adminuser)) {
                checkAdminPassword(username, password);
                return defaultAdministrator(); 
            }
            
            // authenticate against LDAP:
            InitialLdapContext ldapContext = (InitialLdapContext) contextFactory.getReadWriteContext(principal, password);

            // if successfull, try to find user account in DB under same name:
            try {
                loadUserConfigurationCommand.setUsername(username);
                commandService.executeCommand(loadUserConfigurationCommand);
            } catch (CommandException e) {
                throw new RuntimeException("Failed to retrieve user configuration.", e);
            }
            
            List<Entity> entities = loadUserConfigurationCommand.getEntities();
            
            if (entities != null && entities.size()>0) {
                for (Entity entity : entities) {
                    if (DbUserDetailsService.isUser(username, entity)) {
                        return ldapUser(entity);
                    }
                }
            }
            
            // no user found, we could have a guest account defined, in this case associate the authenticated ldap user 
            // with the guest account:
            if (guestUser != null && guestUser.length() > 0) {
                try {
                    loadUserConfigurationCommand.setUsername(guestUser);
                    commandService.executeCommand(loadUserConfigurationCommand);
                } catch (CommandException e) {
                    throw new RuntimeException("Failed to retrieve guest user configuration.", e);
                }
                entities = loadUserConfigurationCommand.getEntities();
                
                if (entities != null && entities.size()>0) {
                    for (Entity entity : entities) {
                        if (DbUserDetailsService.isUser(guestUser, entity)) {
                            // replace username in entity
                            return ldapUser(entity, new String[] {ApplicationRoles.ROLE_GUEST});
                        }
                    }
                    
                }
            }
            
            throw new UsernameNotFoundException("No matching account or guest account found for authenticated directory user " 
                    + username 
                    + " in the verinice database. Create an account for the user in verinice first, matching the directory's account name.");
        } else {
            throw new BadCredentialsException("Blank username and/or password!");
        }
    }

    /**
     * @param entity
     * @param roleGuest
     * @return
     */
    private DirContextOperations ldapUser(Entity entity, String[] specialRoles) {
        DirContextOperations authAdapter = new DirContextAdapter();
        List<String> roles = new ArrayList<String>();
        
        // All non-privileged users have the role "ROLE_USER".
        roles.add(ApplicationRoles.ROLE_USER);
        
        // if set in the entity, the user may also have the admin role:
        if (entity.isSelected(Configuration.PROP_ISADMIN, "configuration_isadmin_yes"))
            roles.add(ApplicationRoles.ROLE_ADMIN);
        
        // add special roles:
        if (specialRoles != null && specialRoles.length>0) {
            for (String role: specialRoles) {
                roles.add(role);
            }
        }
        
        String[] rolesArray= (String[]) roles.toArray(new String[roles.size()]);
        authAdapter.setAttributeValues(LdapAuthenticationProvider.ROLES_ATTRIBUTE, rolesArray);
        return authAdapter;
        
    }

    /**
     * @return
     */
    private DirContextOperations defaultAdministrator() {
        DirContextOperations authAdapter = new DirContextAdapter();
        List<String> roles = new ArrayList<String>();
        
        roles.add(ApplicationRoles.ROLE_USER);
        roles.add(ApplicationRoles.ROLE_ADMIN);
        
        String[] rolesArray= (String[]) roles.toArray(new String[roles.size()]);
        authAdapter.setAttributeValues(LdapAuthenticationProvider.ROLES_ATTRIBUTE, rolesArray);
        return authAdapter;
    }

    /**
     * @param username
     * @param password
     */
    private void checkAdminPassword(String username, String password) {
        String hash = DigestProcessingFilter.encodePasswordInA1Format(username,
                passwordRealm, password);
        if (hash.equals(adminpass))
            return;
        throw new BadCredentialsException("Wrong username / password for administrative user.");
    }

    /**
     * @param guestUser the guestUser to set
     */
    public void setGuestUser(String guestUser) {
        this.guestUser = guestUser;
    }

    /**
     * @param entity
     * @return
     */
    private DirContextOperations ldapUser(Entity entity) {
        return ldapUser(entity, null);
    }

    public DefaultSpringSecurityContextSource getContextFactory() {
        return contextFactory;
    }

    /**
     * Set the context factory to use for generating a new LDAP context.
     * 
     * @param contextFactory
     */
    public void setContextFactory(DefaultSpringSecurityContextSource contextFactory) {
        this.contextFactory = contextFactory;
    }

    public String getPrincipalPrefix() {
        return principalPrefix;
    }

    /**
     * Set the string to be prepended to all principal names prior to attempting
     * authentication against the LDAP server. (For example, if the Active
     * Directory wants the domain-name-plus backslash prepended, use this.)
     * 
     * @param principalPrefix
     */
    public void setPrincipalPrefix(String principalPrefix) {
        if (principalPrefix != null) {
            this.principalPrefix = principalPrefix;
        } else {
            this.principalPrefix = "";
        }
    }
}
