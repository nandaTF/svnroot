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
package sernet.gs.ui.rcp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.core.UpdateCore;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;

/**
 * Prepare the workspace directory for the application. Created needed files
 * etc.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class CnAWorkspace {

	private static final Logger log = Logger.getLogger(CnAWorkspace.class);

	private static final String OFFICEDIR = "office"; //$NON-NLS-1$

	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$

	private static String workDir;

	// copy binary data using 100K buffer:
	static final int BUFF_SIZE = 100000;

	static final byte[] buffer = new byte[BUFF_SIZE];

	/**
	 * Version number to check against version file. When changing this, also
	 * change version number in skeleton file "conf/configuration.version"
	 */
	public static final Object CONFIG_CURRENT_VERSION = "0.8.1"; //$NON-NLS-1$

	protected static final String VERINICEDB = "verinicedb"; //$NON-NLS-1$

	protected static final String TEMPIMPORTDB = "tempGstoolImportDb"; //$NON-NLS-1$

	private static final String POLICY_FILE = "updatePolicyURL"; //$NON-NLS-1$

	private static final Object LOCAL_UPDATE_SITE_URL = "/Verinice-Update-Site-2010"; //$NON-NLS-1$

    private static CnAWorkspace instance;

	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		private boolean modechangeWarning = true;

		public void propertyChange(PropertyChangeEvent event) {
			if ((event.getProperty().equals(PreferenceConstants.GS_DB_URL) || event.getProperty().equals(PreferenceConstants.GS_DB_USER) || event.getProperty().equals(PreferenceConstants.GS_DB_PASS))) {

				Preferences prefs = Activator.getDefault().getPluginPreferences();
				try {
					String dbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);

					createGstoolImportDatabaseConfig(dbUrl, prefs.getString(PreferenceConstants.GS_DB_USER), prefs.getString(PreferenceConstants.GS_DB_PASS));

				} catch (Exception e) {
					ExceptionUtil.log(e, Messages.CnAWorkspace_0);
				}
			}

			if (event.getProperty().equals(PreferenceConstants.OPERATION_MODE) || event.getProperty().equals(PreferenceConstants.VNSERVER_URI)) {
				try {
					updatePolicyFile();

					if (!modechangeWarning) {
						modechangeWarning = false;
						MessageDialog.openWarning(Display.getCurrent().getActiveShell(), Messages.CnAWorkspace_1, Messages.CnAWorkspace_2);
					}
				} catch (Exception e) {
					ExceptionUtil.log(e, Messages.CnAWorkspace_3);
				}
			}
		}
	};

	private File confDir;

	protected CnAWorkspace() {
	}

	public String createTempImportDbUrl() {
		String tmpDerbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s", CnAWorkspace.getInstance().getWorkdir().replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return tmpDerbyUrl.replace(VERINICEDB, TEMPIMPORTDB);
	}

	public String getTempImportDbDirName() {
		return CnAWorkspace.getInstance().getWorkdir() + File.separator + TEMPIMPORTDB;
	}

	public static CnAWorkspace getInstance() {
		if (instance == null) {
			instance = new CnAWorkspace();
			Activator.getDefault().getPluginPreferences().addPropertyChangeListener(instance.prefChangeListener);
		}
		return instance;
	}

	/**
	 * Initialize new workspace folder with config and other files that are
	 * distributed with the application.
	 * 
	 * 
	 */
	public void prepare(boolean force) {
		prepareWorkDir();
		updatePolicyFile();
		
		if (!force && confDir.exists() && confDir.isDirectory()) {
			File confFile = new File(confDir, "configuration.version"); //$NON-NLS-1$
			if (confFile.exists()) {
				Properties props = new Properties();
				FileInputStream fis;
				try {
					fis = new FileInputStream(confFile);
					props.load(fis);

					if (props.get("version").equals(CONFIG_CURRENT_VERSION)) { //$NON-NLS-1$
						log.debug("Arbeitsverzeichnis bereits vorhanden, wird nicht neu erzeugt: " + confDir.getAbsolutePath()); //$NON-NLS-1$
						return;
					}
				} catch (Exception e) {
					log.debug(e);
				}
			}

		}

		CnAWorkspace instance = new CnAWorkspace();
		try {
			instance.createConfDir();
			instance.createHtmlDir();
			instance.createOfficeDir();
			instance.createDatabaseConfig();
			instance.updatePolicyFile();
		} catch (Exception e) {
			ExceptionUtil.log(e, NLS.bind(Messages.CnAWorkspace_4, confDir.getAbsolutePath()));
		}

	}

	public void prepareWorkDir() {
		URL url = Platform.getInstanceLocation().getURL();
		String path = url.getPath().replaceAll("/", "\\" + File.separator); //$NON-NLS-1$ //$NON-NLS-2$
		workDir = (new File(path)).getAbsolutePath();
		confDir = new File(url.getPath() + File.separator + "conf"); //$NON-NLS-1$
		if(!confDir.exists()) {
		    if (log.isDebugEnabled()) {
                log.debug("Conf dir does not exits: " + confDir.getAbsolutePath());
            }
		    try {
		        confDir.mkdir();
		        if (log.isDebugEnabled()) {
	                log.debug("Conf dir created: " + confDir.getAbsolutePath());
	            }
		    } catch( Exception e ) {
		        log.error("Error while creating Conf dir: " + confDir.getAbsolutePath(), e);
		    }
		}
	}

	public void updatePolicyFile() {
		Preferences prefs = Activator.getDefault().getPluginPreferences();

		if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER)) {
			removePolicyFile();
		} else {
			try {
				createPolicyFile(prefs);
			} catch (MalformedURLException e) {
				log.error("Konnte Update-Policy File nicht erzeugen.", e); //$NON-NLS-1$
			} catch (IOException e) {
				log.error("Konnte Update-Policy File nicht erzeugen.", e); //$NON-NLS-1$
			}
		}
	}

	private void removePolicyFile() {
		// remove policy file / path to policy file. thereby setting update site
		// to default:
		removeFile(getConfDir(), "policy.xml"); //$NON-NLS-1$
		UpdateCore.getPlugin().getPluginPreferences().setValue("updatePolicyURL", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void createPolicyFile(Preferences prefs) throws IOException, MalformedURLException {
		// create update policy file to set update site to local verinice
		// server:
		HashMap<String, String> settings = new HashMap<String, String>(1);
		settings.put("updatesiteurl", createUpdateSiteUrl(prefs.getString(PreferenceConstants.VNSERVER_URI))); //$NON-NLS-1$
		createTextFile("conf" + File.separator + "skel_policy.xml", getConfDir(), "policy.xml", settings); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// set path to policy.xml with changed update site (on local server):
		File policyFile = new File(getConfDir() + File.separator + "policy.xml"); //$NON-NLS-1$
		UpdateCore.getPlugin().getPluginPreferences().setValue("updatePolicyURL", policyFile.toURI().toURL().toString()); //$NON-NLS-1$
	}

	/**
	 * @param dir
	 * @param string
	 */
	private void removeFile(String dir, String name) {
		File fileToDelete = new File(dir + File.separator + name);
		boolean success = fileToDelete.delete();
		if (success) {
			log.debug(name + " was successfully deleted."); //$NON-NLS-1$
		} else {
			log.debug(name + " was NOT deleted."); //$NON-NLS-1$
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private String createUpdateSiteUrl(String serverUrl) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(serverUrl);
		stringBuilder.append(LOCAL_UPDATE_SITE_URL);
		return stringBuilder.toString();
	}

	public String getWorkdir() {
		return workDir;
	}

	/**
	 * Returns the full path to configuration dir of the client:
	 * usually: <verinice_install_dir>/workspace/conf
	 * e.g.: /home/fifi/verinice-1.1.0/workspace/conf
	 * 
	 * @return the full path to configuration dir of the client
	 */
	public String getConfDir() {
		return workDir + File.separator + "conf"; //$NON-NLS-1$
	}

	private void createConfDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File confDir = new File(url.getPath() + File.separator + "conf"); //$NON-NLS-1$
		confDir.mkdirs();

		createTextFile("conf" + File.separator + "reports.properties_skeleton", 
		               workDir, 
		               "conf" + File.separator + "reports.properties"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		createTextFile("conf" + File.separator + "configuration.version", workDir); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void createOfficeDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File officeDir = new File(url.getPath() + File.separator + OFFICEDIR);
		officeDir.mkdirs();

		createBinaryFile(OFFICEDIR + File.separator + "report.ods", workDir); //$NON-NLS-1$
		createBinaryFile(OFFICEDIR + File.separator + "report.odt", workDir); //$NON-NLS-1$
		createBinaryFile(OFFICEDIR + File.separator + "sernet.png", workDir); //$NON-NLS-1$

	}

	private void createHtmlDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File htmlDir = new File(url.getPath() + File.separator + "html"); //$NON-NLS-1$
		htmlDir.mkdirs();

		createTextFile("html" + File.separator + "screen.css", workDir); //$NON-NLS-1$ //$NON-NLS-2$
		createTextFile("html" + File.separator + "about.html", workDir); //$NON-NLS-1$ //$NON-NLS-2$
		createBinaryFile("browserdefault.png", workDir + File.separator + "html"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Copy resource from classpath (i.e. inside JAR file) to local filesystem.
	 * 
	 * @param infile
	 * @param toDir
	 * @throws IOException
	 */
	private void createBinaryFile(String infile, String toDir) throws IOException {

		backupFile(toDir, infile);

		String infileResource = infile.replace('\\', '/');
		InputStream in = getClass().getClassLoader().getResourceAsStream(infileResource);
		OutputStream out = null;
		try {
			out = new FileOutputStream(toDir + File.separator + infile);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public void createGstoolImportDatabaseConfig(String url, String user, String pass) throws NullPointerException, IOException {
		HashMap<String, String> settings = new HashMap<String, String>(5);
		settings.put("url", url.replace("\\", "\\\\")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		settings.put("user", user); //$NON-NLS-1$
		settings.put("pass", pass); //$NON-NLS-1$

		// import from .mdb file over odbc bridge goes into temporary derby db
		// first:
		if (url.indexOf("odbc") > -1) { //$NON-NLS-1$
			// change db url to temporary DB when importing from mdb file
			String dbUrl = createTempImportDbUrl();
			settings.put("url", dbUrl); //$NON-NLS-1$
			settings.put("driver", PreferenceConstants.DB_DRIVER_DERBY); //$NON-NLS-1$
			settings.put("dialect", PreferenceConstants.DB_DIALECT_derby); //$NON-NLS-1$
		} else {
			// direct import from ms sql server or desktop engine:
			settings.put("driver", PreferenceConstants.GS_DB_DRIVER_JTDS); //$NON-NLS-1$
			settings.put("dialect", PreferenceConstants.GS_DB_DIALECT_JTDS); //$NON-NLS-1$
		}

		createTextFile(
		        "conf" + File.separator + "skel_hibernate-vampire.cfg.xml", //$NON-NLS-1$ //$NON-NLS-2$
		        workDir, 
		        "conf" + File.separator + "hibernate-vampire.cfg.xml", //$NON-NLS-1$ //$NON-NLS-2$
		        settings);  
	}

	public void createGstoolImportDatabaseConfig() throws NullPointerException, IOException {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		createGstoolImportDatabaseConfig(prefs.getString(PreferenceConstants.GS_DB_URL), prefs.getString(PreferenceConstants.GS_DB_USER), prefs.getString(PreferenceConstants.GS_DB_PASS));
	}

	private void createTextFile(String infile, String toDir) throws NullPointerException, IOException {
		createTextFile(infile, toDir, infile, null);
	}

	private void createTextFile(String infile, String toDir, String outfile) throws NullPointerException, IOException {
		createTextFile(infile, toDir, outfile, null);
	}
	
	/**
     * Create a text file in the local file system from a resource (i.e. inside
     * a JAR file distributed with the application).
     * 
     * Adapt line-feeds to local settings and optionally replace variables with
     * values given in a hashmap.
     * 
     * @param infile relative path to a file accessed by a class loader 
     * @param toDir path to directory of the created file
     * @param outfile name of created file
     * @param variables property added to the file, may be null
     * @throws NullPointerException
     * @throws IOException
     */
    protected void createTextFile(String infile, String toDir, String outfile, Map<String, String> variables) throws NullPointerException, IOException {
        createTextFile(infile, VeriniceCharset.CHARSET_DEFAULT, toDir, outfile, variables);
    }

	/**
	 * Create a text file in the local file system from a resource (i.e. inside
	 * a JAR file distributed with the application).
	 * 
	 * Adapt line-feeds to local settings and optionally replace variables with
	 * values given in a hashmap.
	 * 
	 * Files are converted from charsetInfile to CHARSET_DEFAULT.
	 * 
	 * @param 
	 *     infile relative path to a file accessed by a class loader 
	 * @param 
	 *     charsetInfile charset of input file 
	 *     Files are converted from charsetInfile to CHARSET_DEFAULT
	 * @param 
	 *     toDir path to directory of the created file
	 * @param 
	 *     outfile name of created file
	 * @param 
	 *     variables property added to the file, may be null
	 * @throws 
	 *     NullPointerException
	 * @throws
	 *     IOException
	 */
	protected void createTextFile(String infile, Charset charsetInfile, String toDir, String outfile, Map<String, String> variables) throws NullPointerException, IOException {

		String infileResource = infile.replace('\\', '/');
		InputStream is = getClass().getClassLoader().getResourceAsStream(infileResource);
		InputStreamReader inRead = new InputStreamReader(is,charsetInfile);
		BufferedReader bufRead = new BufferedReader(inRead);
		StringBuffer skelFile = new StringBuffer();

		// write from skel file, replacing newline characters to system
		// specific:
		String line;
		Pattern var = Pattern.compile("\\{(.*)\\}"); //$NON-NLS-1$
		while ((line = bufRead.readLine()) != null) {
			line = line.replaceFirst("\n", LINE_SEP); //$NON-NLS-1$
			if (variables != null) {
				Matcher match = var.matcher(line);
				if (match.find()) {
					line = match.replaceFirst(variables.get(match.group(1)));
				}
			}
			skelFile.append(line + LINE_SEP);
		}
		bufRead.close();
		inRead.close();
		is.close();

		backupFile(toDir, outfile);
		FileOutputStream fout = new FileOutputStream(toDir + File.separator + outfile, false);
		OutputStreamWriter outWrite = new OutputStreamWriter(fout,VeriniceCharset.CHARSET_DEFAULT);
		outWrite.write(skelFile.toString());
		outWrite.close();
		fout.close();
	}

	private void backupFile(String dir, String filepath) throws IOException {
		File file = new File(dir + File.separator + filepath);
		if (file.exists()) {
			File outfile = new File(dir + File.separator + filepath + ".bak"); //$NON-NLS-1$
			FileUtils.copyFile(file, outfile);
		}
	}

	public synchronized void createDatabaseConfig() throws NullPointerException, IOException, IllegalStateException {

		Preferences prefs = Activator.getDefault().getPluginPreferences();

		Activator.getDefault().getInternalServer().configure(prefs.getString(PreferenceConstants.DB_URL), prefs.getString(PreferenceConstants.DB_USER), prefs.getString(PreferenceConstants.DB_PASS), prefs.getString(PreferenceConstants.DB_DRIVER), prefs.getString(PreferenceConstants.DB_DIALECT));

		createGstoolImportDatabaseConfig(prefs.getString(PreferenceConstants.GS_DB_URL), prefs.getString(PreferenceConstants.GS_DB_USER), prefs.getString(PreferenceConstants.GS_DB_PASS));
	}

	public void prepare() {
		prepare(false);
	}

}
