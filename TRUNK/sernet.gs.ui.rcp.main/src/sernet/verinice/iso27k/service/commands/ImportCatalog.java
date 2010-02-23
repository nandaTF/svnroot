/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service.commands;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.verinice.iso27k.service.Catalog;
import sernet.verinice.iso27k.service.ICatalog;
import sernet.verinice.iso27k.service.ICatalogImporter;
import sernet.verinice.iso27k.service.Item;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Command to import a CSV file.
 * 
 * ImportCatalog creates a {@link Catalog} from the file content.
 * To import an file create a new {@link ImportCatalog} by passing
 * the full file path and execute the command
 * 
 * File content is read from the file and stored in an byte array by contructor.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("serial")
public class ImportCatalog extends GenericCommand implements ICatalogImporter {

	private transient Logger log = Logger.getLogger(ImportCatalog.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(ImportCatalog.class);
		}
		return log;
	}
	
	ImportConfiguration config;

	CsvFile csvFile;
	
	Catalog catalog = new Catalog();
	
	public ImportCatalog(String filePath) throws IOException {
		super();
		csvFile = new CsvFile(filePath);
	}
	
	public ImportCatalog(byte[] fileContent) throws IOException {
		super();
		csvFile = new CsvFile(fileContent);
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		try {
			config = new ImportConfiguration(csvFile);			
			importCatalog();
		} catch(Exception e) {
			getLog().error("Error while executing", e);
			throw new RuntimeCommandException("Fehler beim Importieren des Katalogs.", e);
		}	
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.ICatalogImporter#importCatalog()
	 */
	public void importCatalog() {
		try {
			CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvFile.getFileContent()), Charset.forName("UTF8"))),config.getSeperator(),'"',false);
			String[] nextLine;
			Item item = null;
		    while ((nextLine = reader.readNext()) != null) {
		        // nextLine[] is an array of values from the line	
		    	
		        if (getLog().isDebugEnabled()) {
		        	getLog().debug("#: " + nextLine[0]);
		        	getLog().debug("heading: " + nextLine[1]);
		        	getLog().debug("type: " + nextLine[2]);
		        	getLog().debug("text: " + nextLine[3]);
				}	        
		        if(nextLine[0]!=null && nextLine[0].length()>0) {
		        	if(item!=null) {
		        		// buffer the old item
		        		catalog.bufferItem(item);
		        	}
		        	// create a new one
		        	item = new Item(nextLine[1],nextLine[2]);
		        	item.setNumberString(nextLine[0].trim());
		        	StringBuilder sb = new StringBuilder();
		        	sb.append("<p>").append(nextLine[3]).append("</p>");
		        	item.setDescription(sb.toString());
		        } else {
		        	// add a new paragraph to the existing item
		        	StringBuilder sb = new StringBuilder(item.getDescription());
		        	sb.append("<p>").append(nextLine[3]).append("</p>");
		        	item.setDescription(sb.toString());
		        }
		        
		    }
		    // buffer the last item
		    catalog.bufferItem(item);
		    
		    // create the tree
		    catalog.processItemBuffer();
		    
		    if (getLog().isDebugEnabled()) {
		    	getLog().debug(catalog);
			}
		    
		} catch(IOException e) {
			getLog().error("Error while importing", e);
			throw new RuntimeException("Error while importing", e);
		}
	}

	public void setCsvFile(CsvFile csvFile) {
		this.csvFile = csvFile;
	}

	public CsvFile getCsvFile() {
		return csvFile;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.ICatalogImporter#getTree()
	 */
	public ICatalog getCatalog() {
		return catalog;
	}

}
