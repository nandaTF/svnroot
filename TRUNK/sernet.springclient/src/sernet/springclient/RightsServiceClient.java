/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.ConfigurationType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class RightsServiceClient implements IRightsServiceClient{

    private static final Logger LOG = Logger.getLogger(RightsServiceClient.class);
    
    IAuthService authService;
    IRightsService rightsServiceExecuter;
    List<Userprofile> userprofile;
    Map<String, Action> actionMap;
    Profiles profiles;
    Map<String, Profile> profileMap;
    Auth auth;
    List<String> userNameList;
    List<String> groupNameList;
    Properties messages;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsServiceClient#containsAction(java.lang.String)
     */
    public boolean isEnabled(String actionId) {
        boolean returnValue = false;
        try {
            returnValue = isBlacklist();
        
            if(getUserprofile()!=null) {
                returnValue = actionMap.get(actionId)!=null && isWhitelist() || 
                              actionMap.get(actionId)==null && isBlacklist();
            }
            return returnValue;
        } catch (Exception e) {
            LOG.error("Error while checking action. Returning false", e);
            return returnValue;
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        if(auth==null) {
            auth=getRightsServiceExecuter().getConfiguration();
        }
        return auth;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#updateConfiguration(sernet.verinice.model.auth.Auth)
     */
    @Override
    public void updateConfiguration(Auth auth) {
        getRightsServiceExecuter().updateConfiguration(auth);
        this.auth = auth;
        this.userprofile = null;
        this.profiles = null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUserprofile(java.lang.String)
     */
    @Override
    public List<Userprofile> getUserprofile(String username) {
        return getRightsServiceExecuter().getUserprofile(username);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsServiceClient#getUserprofile()
     */
    @Override
    public List<Userprofile> getUserprofile() {
        if(userprofile==null) {
            userprofile = loadUserprofile();
        }
        return userprofile;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        if(profiles==null) {
            profiles = loadProfiles();
        }
        return profiles;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUsernames()
     */
    @Override
    public List<String> getUsernames() {
        if(userNameList==null) {
            userNameList = getRightsServiceExecuter().getUsernames();
        }
        return userNameList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getGroupnames()
     */
    @Override
    public List<String> getGroupnames() {
        if(groupNameList==null) {
            groupNameList = getRightsServiceExecuter().getGroupnames();
        }
        return groupNameList;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getMessage(java.lang.String)
     */
    @Override
    public String getMessage(String key) {
        String defaultMessage = key;
        if (LOG.isDebugEnabled()) {
            // switch to debug in log4j.xml to find untranslated messages
            defaultMessage = defaultMessage + " (!)";
        }
        return getAllMessages().getProperty(key, key);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getAllMessages()
     */
    @Override
    public Properties getAllMessages() {
        if(messages==null) {
            messages = getRightsServiceExecuter().getAllMessages();
        }
        return messages;
    }
    
    private Profiles loadProfiles() {
        Profiles profiles = getRightsServiceExecuter().getProfiles();   
        profileMap = new HashMap<String, Profile>();
        for (Profile profile : profiles.getProfile()) {
            profileMap.put(profile.getName(), profile);
        }
        return profiles;
    }
    
    private  List<Userprofile> loadUserprofile() {
        List<Userprofile> userprofileList = getRightsServiceExecuter().getUserprofile(getAuthService().getUsername());        
        if(userprofileList==null || userprofileList.isEmpty() ) {
            // no userprofile found, create an empty dummy userprofile
            Userprofile userprofile = new Userprofile();
            userprofile.setLogin(getAuthService().getUsername());
            userprofileList = new ArrayList<Userprofile>(1);
            userprofileList.add(userprofile);
        }
        actionMap = new HashMap<String, Action>();
        for (Userprofile userprofile : userprofileList) {  
            List<ProfileRef> profileList = userprofile.getProfileRef();
            if(profileList!=null) {
                for (ProfileRef profileRef : profileList) {
                    Profile profileWithActions = getProfileMap().get(profileRef.getName());
                    if(profileWithActions!=null) {
                        List<Action> actionList = profileWithActions.getAction();
                        for (Action action : actionList) {
                            actionMap.put(action.getId(), action);            
                        }
                    } else {
                        LOG.error("Could not find profile " + profileRef.getName() + " of user " + getAuthService().getUsername());
                    }
                }
            }
        }
        return userprofileList;
    }

    /**
     * @return the profileMap
     */
    public Map<String, Profile> getProfileMap() {
        if(profileMap==null) {
            loadProfiles();
        }
        return profileMap;
    }

    public boolean isBlacklist() {
        return ConfigurationType.BLACKLIST.equals(getConfiguration().getType());
    }
    
    public boolean isWhitelist() {
        return ConfigurationType.WHITELIST.equals(getConfiguration().getType());
    }

    /**
     * @return the authService
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /**
     * @param authService the authService to set
     */
    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

    /**
     * @return the rightsServiceExecuter
     */
    public IRightsService getRightsServiceExecuter() {
        return rightsServiceExecuter;
    }

    /**
     * @param rightsServiceExecuter the rightsServiceExecuter to set
     */
    public void setRightsServiceExecuter(IRightsService rightsServiceExecuter) {
        this.rightsServiceExecuter = rightsServiceExecuter;
    }

}
