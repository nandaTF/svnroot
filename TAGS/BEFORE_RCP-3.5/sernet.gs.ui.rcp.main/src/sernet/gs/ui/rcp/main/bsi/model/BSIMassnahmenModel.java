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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.remoting.RemoteConnectFailureException;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.scraper.GSScraper;
import sernet.gs.scraper.IGSPatterns;
import sernet.gs.scraper.IGSSource;
import sernet.gs.scraper.PatternBfDI2008;
import sernet.gs.scraper.PatternGSHB2005_2006;
import sernet.gs.scraper.PatternGSHB2009;
import sernet.gs.scraper.URLGSSource;
import sernet.gs.scraper.ZIPGSSource;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetBausteinText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetGefaehrdungText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.GetMassnahmeText;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;

public class BSIMassnahmenModel {
	
	private static final Logger LOG = Logger.getLogger(BSIMassnahmenModel.class);
	
	private static final Logger log = Logger.getLogger(BSIMassnahmenModel.class);

	private static final String DS_B01005_BFDI = "b01005_bfdi"; //$NON-NLS-1$

	private static final String DS_B_1_5 = "B 1.5"; //$NON-NLS-1$

	private static final String DS_2008 = "2008"; //$NON-NLS-1$

	private static final String B01005 = "b01005"; //$NON-NLS-1$

	private static List<Baustein> cache;

	private static GSScraper scrape;

	private static String previouslyReadFile = ""; //$NON-NLS-1$

	private static GSScraper dsScrape;

	private static String previouslyReadFileDS = ""; //$NON-NLS-1$
	
	private IBSIConfig config;
	
	// not configured by Spring
	private ILayoutConfig layoutConfig;

	private String encoding = "utf-8";
	
	public BSIMassnahmenModel(IBSIConfig config)
	{
		this.config = config;
	}

	/**
	 * Loads the 
	 * @param mon
	 * @return
	 * @throws GSServiceException
	 * @throws IOException
	 */
	public synchronized List<Baustein> loadBausteine(IProgress mon)
			throws GSServiceException, IOException {
		if (config instanceof BSIConfigurationRemoteSource) {
			log.debug("Lade Kataloge von Verinice-Server...");
			return loadBausteineRemote();
		}

		String gsPath = config.getGsPath();
		String dsPath = config.getDsPath();
		boolean fromZipFile = config.isFromZipFile();
		IGSSource gsSource = null;
		String cacheDir = config.getCacheDir();

		log.debug("Lesen der GS-Kataloge: " + gsPath);

		// did user really change the path to file?
		if (! (previouslyReadFile.equals(gsPath) && previouslyReadFileDS.equals(dsPath))
				) {
			previouslyReadFile = gsPath;
			previouslyReadFileDS = dsPath;

			try {
				if (fromZipFile)
					gsSource = new ZIPGSSource(gsPath);
				else
					gsSource = new URLGSSource(gsPath);
			} catch (IOException e) {
				LOG.error("GS-Kataloge nicht gefunden: " + gsPath + " Download unter: https://www.bsi.bund.de"); //$NON-NLS-1$
				if (LOG.isDebugEnabled()) {
					LOG.debug("stacktrace: ", e);
				}
				return null;
			}

			if (gsSource.getVintage().equals(IGSSource.VINTAGE_2009))
				scrape = new GSScraper(gsSource, new PatternGSHB2009());
			else
				scrape = new GSScraper(gsSource, new PatternGSHB2005_2006());
				
			
			scrape.setCacheDir(cacheDir); //$NON-NLS-1$
			
			Logger.getLogger(BSIMassnahmenModel.class).debug("Setting GS-Cache to " + scrape.getCacheDir()); //$NON-NLS-1$
			mon.beginTask("Laden und Zwischenspeichern der GS-Kataloge...", 5);
			List<Baustein> alleBst = new ArrayList<Baustein>();

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[0]);
			alleBst.addAll(scrapeBausteine("b01")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[1]);
			alleBst.addAll(scrapeBausteine("b02")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[2]);
			alleBst.addAll(scrapeBausteine("b03")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[3]);
			alleBst.addAll(scrapeBausteine("b04")); //$NON-NLS-1$
			mon.worked(1);

			mon.subTask(BausteinUmsetzung.getSchichtenBezeichnung()[4]);
			alleBst.addAll(scrapeBausteine("b05")); //$NON-NLS-1$
			mon.worked(1);
			
			// if a source for data privacy module is defined, replace the temporary module with the real one:
			if (dsPath != null && dsPath.length() > 0) {
				try {
					ZIPGSSource dsSource = new ZIPGSSource(dsPath);
					dsScrape = new GSScraper(dsSource, new PatternBfDI2008());
					dsScrape.setCacheDir(cacheDir); //$NON-NLS-1$
					
					Baustein dsBaustein = scrapeDatenschutzBaustein();
					
					searchDataPrivacyModule: for (Iterator iterator = alleBst.iterator(); iterator.hasNext();) {
						Baustein baustein = (Baustein) iterator.next();
						if (baustein.getUrl().indexOf(B01005) > -1) {
							alleBst.remove(baustein);
							break searchDataPrivacyModule;
						}
					}
					alleBst.add(dsBaustein);
				} catch (Exception e) {
					Logger.getLogger(BSIMassnahmenModel.class).debug("Datenschutz-Baustein nicht gefunden."); //$NON-NLS-1$
				}
			}
			
			cache = alleBst;
			mon.done();
			Logger.getLogger(BSIMassnahmenModel.class).debug(
					"GS-Kataloge loaded.");

		}
		return cache;
	}

	private List<Baustein> loadBausteineRemote() throws GSServiceException {
		// use remote source
		try {
			LoadBausteine command = new LoadBausteine();
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			return command.getBausteine();
		} catch (CommandException e) {
			log.warn("execution of command failed: " + e.getLocalizedMessage());;
			throw new GSServiceException(e.getCause());
		} catch(RemoteConnectFailureException re)
		{
			log.error("error connecting to server: " + re.getLocalizedMessage());
			
			// TODO rschuster: Display a nice error dialog and asking the user to
			// check whether the server URL is valid (or the server is down?)
			// If this happens in internal server mode than something is very bad.
			
			throw new GSServiceException(re.getCause());
		}
	}

	private Baustein scrapeDatenschutzBaustein() throws GSServiceException {
    	Baustein b = new Baustein();
    	b.setStand(DS_2008);
    	b.setId(DS_B_1_5);
    	b.setTitel("Datenschutz BfDI"); //$NON-NLS-1$
    	b.setUrl(DS_B01005_BFDI);
    	b.setSchicht(1);
    	
    	List<Massnahme> massnahmen = dsScrape.getMassnahmen(b.getUrl());
		b.setMassnahmen(massnahmen);

		List<Gefaehrdung> gefaehrdungen = dsScrape.getGefaehrdungen(b.getUrl());
		b.setGefaehrdungen(gefaehrdungen);
    	
    	return b;
	}

	public InputStream getBaustein(String url, String stand) 
		throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getBausteinFromServer(url, stand);
		}	
		
		InputStream bausteinText = null;
		try {
			bausteinText = scrape.getBausteinText(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				bausteinText = dsScrape.getBausteinText(url, stand);
		}
		return bausteinText;
	}

	private InputStream getBausteinFromServer(String url, String stand) throws GSServiceException {
		GetBausteinText command = new GetBausteinText(url, stand);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String bausteinText = command.getBausteinText();
			return stringToStream(bausteinText, command.getEncoding());
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	private InputStream stringToStream(String text, String encoding) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(text.getBytes(encoding));
	}

	public InputStream getMassnahme(String url, String stand) throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getMassnahmeFromServer(url, stand);
		}	
		
		InputStream massnahme = null;
		try {
			massnahme = scrape.getMassnahme(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				massnahme = dsScrape.getMassnahme(url, stand);
		}
		return massnahme;
	}
	
	public String getMassnahmeHtml(String url, String stand) throws GSServiceException {
		try {
			InputStreamReader read = new InputStreamReader(getMassnahme(url, stand), encoding ); //$NON-NLS-1$
			BufferedReader buffRead = new BufferedReader(read);
			StringBuilder b = new StringBuilder();
			String line;
			boolean skip = false;
			boolean skipComplete = false;
			
			String cssFile = getLayoutConfig().getCssFilePath();
				
			while ((line = buffRead.readLine()) != null) {
				if (!skipComplete) {
					if (line.matches(".*div.*id=\"menuoben\".*")
							|| line.matches(".*div.*class=\"standort\".*")) //$NON-NLS-1$
						skip = true;
					else if (line.matches(".*div.*id=\"content\".*")) { //$NON-NLS-1$
						skip = false;
						skipComplete = true;
					}
				}
	
				// we strip away images et al to keep just the information we
				// need:
				line = line.replace("../../media/style/css/screen.css", cssFile); //$NON-NLS-1$
				line = line.replace("../../../screen.css", cssFile); //$NON-NLS-1$
				line = line.replace("../../screen.css", cssFile); //$NON-NLS-1$
				line = line.replace("../screen.css", cssFile); //$NON-NLS-1$
				line = line.replaceAll("<a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("</a.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replaceAll("<img.*?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
				line = line.replace((char) 160, ' '); // replace non-breaking spaces
	
				if (!skip) {
					b.append(line);
				}
			}
			return b.toString();
		} catch(Exception e) {
			log.error("Error in getMassnahmeHtml", e);
			throw new GSServiceException("Error in getMassnahmeHtml", e);
		}
	}

	private InputStream getMassnahmeFromServer(String url, String stand) throws GSServiceException {
		try {
			GetMassnahmeText command = new GetMassnahmeText(url, stand);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String text = command.getText();
			encoding = command.getEncoding();
			return stringToStream(text, getEncoding());
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	private List<Baustein> scrapeBausteine(String schicht)
			throws GSServiceException {
		List<Baustein> bausteine = scrape.getBausteine(schicht);
		for (Baustein baustein : bausteine) {
			List<Massnahme> massnahmen = scrape
					.getMassnahmen(baustein.getUrl());
			baustein.setMassnahmen(massnahmen);

			List<Gefaehrdung> gefaehrdungen = scrape
					.getGefaehrdungen(baustein.getUrl());
			baustein.setGefaehrdungen(gefaehrdungen);
		}
		return bausteine;
	}

	public InputStream getGefaehrdung(String url, String stand) 
		throws GSServiceException {
		
		if (config instanceof BSIConfigurationRemoteSource) {
			return getGefaehrdungFromServer(url, stand);
		}	
		
		InputStream gefaehrdung = null;
		try {
			gefaehrdung = scrape.getGefaehrdung(url, stand);
		} catch (Exception e) {
			if (dsScrape != null)
				gefaehrdung = dsScrape.getGefaehrdung(url, stand);
		}
		return gefaehrdung;
	}

	private InputStream getGefaehrdungFromServer(String url, String stand) throws GSServiceException {
		try {
			GetGefaehrdungText command = new GetGefaehrdungText(url, stand);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			String text = command.getText();
			encoding = command.getEncoding();
			return stringToStream(text, getEncoding());
		} catch (CommandException e) {
			throw new GSServiceException(e.getCause());
		} catch (UnsupportedEncodingException e) {
			throw new GSServiceException(e.getCause());
		}
	}

	/**
	 * Discards already loaded data.
	 */
	private void flushCache() {
		if (scrape!= null)
			scrape.flushCache();
		if (dsScrape!= null)
			dsScrape.flushCache();
	}

	/**
	 * Changes the {@link IBSIConfig} instance that is used for this
	 * model.
	 * 
	 * <p>Note: Changing the configuration object may make loading the 
	 * catalogues from a different location. For this reason the method
	 * has the side effect of flushing already loaded data.</p> 
	 * 
	 * @param config
	 */
	public void setBSIConfig(IBSIConfig config) {
		flushCache();
		
		this.config = config;
	}

	public IBSIConfig getBSIConfig() {
		return config;
	}

	public ILayoutConfig getLayoutConfig() {
		if(layoutConfig==null) {
			layoutConfig = new RcpLayoutConfig();
		}
		return layoutConfig;
	}

	public void setLayoutConfig(ILayoutConfig layoutConfig) {
		this.layoutConfig = layoutConfig;
	}
	
	public String getEncoding() {
		if (scrape != null )
			return scrape.getPatterns().getEncoding();
		if (this.encoding != null)
			return encoding;
		
		return "iso-8859-1";
	}
	
}
