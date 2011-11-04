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
package sernet.verinice.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.auth.Userprofiles;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Service to read and change the authorization configuration of verinice.
 * 
 * This implementation loads and saves configuration in an XML file
 * defined in schema <i>verinice-auth.xsd</i>.
 * 
 * Configuration is defined in two documents:
 * <ul>
 * <li>WEB-INF/verinice-auth-default.xml: Default configuration. This file is never changed by an administrator.</li>
 * <li>WEB-INF/verinice-auth.xml: Configuration. Settings in this file overwrite verinice-auth-default.xml.</li>
 * </ul>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class XmlRightsService implements IRightsService {

    private final Logger log = Logger.getLogger(XmlRightsService.class);
    
    Resource authConfigurationDefault;
    
    Resource authConfiguration;
    
    Resource authConfigurationSchema;
    
    Auth auth;
    
    JAXBContext context;
    
    Schema schema;
    
    private IBaseDao<Configuration, Integer> configurationDao;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        if(auth==null) {
            auth = loadConfiguration();
            if (log.isDebugEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Merged auth configuration: ");
                }
                logAuth(auth);
            }
        }      
        return auth;
    }

    private void logAuth(Auth auth) {
        try {
            if (log.isDebugEnabled()) {
                Marshaller marshaller = getContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");  
                StringWriter sw = new StringWriter();
                marshaller.marshal(auth, sw);
                log.debug(sw.toString());
            }
        } catch (Throwable e) {
            log.error("Error while logging auth", e);
        }
    }


    /**
     * Loads the configuration by merging the default and installation configuration
     * 
     * @return the authorization configuration
     */
    private Auth loadConfiguration() {
        try {
            Unmarshaller um = getContext().createUnmarshaller();            
            um.setSchema(getSchema());
            
            // read default configuration
            Auth auth = (Auth) um.unmarshal(getAuthConfigurationDefault().getInputStream());
            Auth authUser = null;
            
            // check if configuration exists
            if(getAuthConfiguration().exists()) {
                authUser = (Auth) um.unmarshal(getAuthConfiguration().getInputStream());
                if (log.isDebugEnabled()) {
                    log.debug("uri: " + getAuthConfiguration().getURI().getPath());
                    log.debug("file path: " + getAuthConfiguration().getFile().getPath());
                }
                // invert default configuration if different type 
                if(!auth.getType().equals(authUser.getType())) {
                    auth = AuthHelper.invert(auth);
                }
                // merge both configurations
                auth = AuthHelper.merge(new Auth[]{authUser,auth});
            }
            
            return auth;
        } catch (RuntimeException e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw new RuntimeException(e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#updateConfiguration(sernet.verinice.model.auth.Auth)
     */
    @Override
    public void updateConfiguration(Auth auth) {
        // remove profiles from verinice-auth-default.xml
        Profiles profilesMod = new Profiles();     
        for (Profile profile : auth.getProfiles().getProfile()) {
            if(!OriginType.DEFAULT.equals(profile.getOrigin())) {
                profilesMod.getProfile().add(profile);
            }
        }
        auth.setProfiles(profilesMod);
        // remove userprofiles from verinice-auth-default.xml
        Userprofiles userprofilesMod = new Userprofiles();     
        for (Userprofile userprofile : auth.getUserprofiles().getUserprofile()) {
            if(!OriginType.DEFAULT.equals(userprofile.getOrigin())) {
                userprofilesMod.getUserprofile().add(userprofile);
            }
        }
        auth.setUserprofiles(userprofilesMod);
        
        Marshaller marshaller;
        try {
            marshaller = getContext().createMarshaller();       
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); 
            marshaller.setSchema(getSchema());
            marshaller.marshal( auth, new FileOutputStream( getAuthConfiguration().getFile().getPath() ) );
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Returns the userprofiles of an user by selecting the 
     * userprofile of the user and the userprofiles of the
     * groups the user belongs to.
     * 
     * @see sernet.verinice.interfaces.IRightsService#getUserprofile(java.lang.String)
     */
    @Override
    public List<Userprofile> getUserprofile(String username) {
        List<String> roleList = getRoleList(username);
        // add the username to the list
        roleList.add(username);   
        List<Userprofile> userprofileList = new ArrayList<Userprofile>(1);
        List<Userprofile> allUserprofileList = getConfiguration().getUserprofiles().getUserprofile();
        for (Userprofile userprofile : allUserprofileList) {
            if(roleList.contains(userprofile.getLogin())) {
                userprofileList.add(userprofile);
            }
        }
        return userprofileList;
    }

    /**
     * Returns an list with the role/groups of an user. 
     * The returned list contains the user name.
     * 
     * @param username an username
     * @return the role/groups of an user
     */
    private List<String> getRoleList(String username) {
        // select all groups of the user
        String HQL = "select roleprops.propertyValue from Configuration as conf " + //$NON-NLS-1$
                "inner join conf.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$
                "inner join conf.entity as entity2 " + //$NON-NLS-1$
                "inner join entity2.typedPropertyLists as propertyList2 " + //$NON-NLS-1$
                "inner join propertyList2.properties as roleprops " + //$NON-NLS-1$
                "where props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue = ? " + //$NON-NLS-1$
                "and roleprops.propertyType = ?"; //$NON-NLS-1$
        Object[] params = new Object[]{Configuration.PROP_USERNAME,username,Configuration.PROP_ROLES};        
        List<String> roleList = getConfigurationDao().findByQuery(HQL,params);
        return roleList;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        return getConfiguration().getProfiles();
    }

    /**
     * @return the authConfigurationDefault
     */
    public Resource getAuthConfigurationDefault() {
        return authConfigurationDefault;
    }

    /**
     * @param authConfigurationDefault the authConfigurationDefault to set
     */
    public void setAuthConfigurationDefault(Resource authConfigurationDefault) {
        this.authConfigurationDefault = authConfigurationDefault;
    }

    /**
     * @return the authConfiguration
     */
    public Resource getAuthConfiguration() {
        return authConfiguration;
    }

    /**
     * @param authConfiguration the authConfiguration to set
     */
    public void setAuthConfiguration(Resource authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    /**
     * @return the authConfigurationSchema
     */
    public Resource getAuthConfigurationSchema() {
        return authConfigurationSchema;
    }

    /**
     * @param authConfigurationSchema the authConfigurationSchema to set
     */
    public void setAuthConfigurationSchema(Resource authConfigurationSchema) {
        this.authConfigurationSchema = authConfigurationSchema;
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Integer> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * @return the context
     */
    private JAXBContext getContext() {
        if(context==null) {
            try {
                context = JAXBContext.newInstance(Auth.class);
            } catch (JAXBException e) {
                log.error("Error while creating JAXB context.", e);
            }
        }
        return context;
    }
    
    private Schema getSchema() {
        if(schema==null) {
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                schema = sf.newSchema(getAuthConfigurationSchema().getURL());
            } catch (Exception e) {
                log.error("Error while creating schema.", e);
            } 
        }
        return schema;
    }

}
