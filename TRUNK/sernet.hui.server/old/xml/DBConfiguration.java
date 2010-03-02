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
package sernet.hui.server.connect.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfiguration {

	private static String PROPS_FILE = "/home/aprack/config/etc/db.properties";
	
	private Properties props;

	public DBConfiguration() throws IOException {
		File f = new File(PROPS_FILE);
		FileInputStream fis = new FileInputStream(f);
		props = new Properties();
		props.load(fis);
		fis.close();
	}

	public String getEnvDir() {
		return props.getProperty("envdir");
	}

	public String getContainer() {
		return props.getProperty("container");
	}

}
