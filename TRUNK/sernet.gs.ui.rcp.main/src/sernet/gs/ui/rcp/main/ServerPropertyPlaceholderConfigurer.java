/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 *     
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * An implementation of the {@link PropertyPlaceholderConfigurer} class which
 * provides programmatically overridden properties which are crucial for the
 * verinice server's database connection.
 * 
 * <p>With this class the integrated (OSGi-ified) veriniceserver gains the
 * ability to receive database connection preferences from the client.</p>  
 * 
 * <p>An instance of this class needs to be available in the OSGi-ified
 * veriniceserver's Spring configuration.</p> 
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class ServerPropertyPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {
	
	private static Logger log = Logger.getLogger(ServerPropertyPlaceholderConfigurer.class);
	
	private static Properties overrideProperties = new Properties();

	protected String resolvePlaceholder(String placeholder, Properties props) {
		
		String value = overrideProperties.getProperty(placeholder);
		if (value != null)
		{
			log.debug("placeholder '" + placeholder + "' overidden with: " + value);
			return value;
		}
		
		return props.getProperty(placeholder);
	}
	
	/**
	 * Makes the database connection properties available to the internal server.
	 * 
	 * <p>The properties will be used when the server's spring configuration
	 * is processed. This is when the server's bundle starts!</p>
	 * 
	 * @param url
	 * @param user
	 * @param pass
	 * @param driver
	 * @param dialect
	 */
	public static void pushDatabaseConfigToInternalServer(String url, String user, String pass,
			String driver, String dialect) {
		
		overrideProperties.put("jdbc.url", url);
		overrideProperties.put("jdbc.username", user);
		overrideProperties.put("jdbc.password", pass);
		overrideProperties.put("jdbc.driverClass", driver);
		overrideProperties.put("jdbc.hibernate.dialect", dialect);

		// Derby uses a different hibernate configuration file.
		if (url.contains("derby"))
			overrideProperties.put("jdbc.hibernate.config",  "classpath:/server_hibernate_derby.cfg.xml");
	}


}
