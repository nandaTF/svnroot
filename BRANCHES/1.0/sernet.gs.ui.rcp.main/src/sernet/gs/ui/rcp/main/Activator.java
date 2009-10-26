/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.IInternalServer;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	
	Logger log = Logger.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.gs.ui.rcp.main";

	// The shared instance
	private static Activator plugin;
	
	private static VeriniceContext.State state;
	
	private IInternalServer internalServer;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}
	
	public static IWorkbenchPage getActivePage()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;

		return page;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		// set workdir preference:
		CnAWorkspace.getInstance().prepareWorkDir();
		
		Preferences prefs = getPluginPreferences();
		
		// set service factory location to local / remote according to preferences:
		boolean standalone = prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);

		// Start server only when it is needed.
		if (standalone)
		{
			Bundle bundle = Platform.getBundle("sernet.gs.server");
			if (bundle == null)
				log.warn("verinice server bundle is not available. Assuming it is started separately.");
			else if (bundle.getState() == Bundle.INSTALLED
							|| bundle.getState() == Bundle.RESOLVED) {
						log.debug("Manually starting GS Server");
						bundle.start();
					}
			
			ServiceReference sr = context.getServiceReference(IInternalServer.class.getName());
			if (sr == null)
				throw new IllegalStateException("Cannot retrieve internal server service.");
			
			internalServer = (IInternalServer) context.getService(sr);
		}
		else
			internalServer = new ServerDummy();
		
		if (prefs.getString(PreferenceConstants.GSACCESS).equals(PreferenceConstants.GSACCESS_DIR))
		{
			try
			{
				internalServer.setGSCatalogURL(
						new File(prefs.getString(PreferenceConstants.BSIDIR)).toURI().toURL());
			} catch (MalformedURLException mfue)
			{
				log.warn("Stored GS catalog dir is an invalid URL.");
			}
		}
		else
		{
			try
			{
				internalServer.setGSCatalogURL(
						new File(prefs.getString(PreferenceConstants.BSIZIPFILE)).toURI().toURL());
			} catch (MalformedURLException mfue)
			{
				log.warn("Stored GS catalog zip file path is an invalid URL.");
			}

		}
		
		try
		{
			internalServer.setDSCatalogURL(
					new File(prefs.getString(PreferenceConstants.DSZIPFILE)).toURI().toURL());
		} catch (MalformedURLException mfue)
		{
			log.warn("Stored DS catalog zip file path is an invalid URL.");
		}
		
		// Provide initial DB connection details to server.
		internalServer.configure(prefs
				.getString(PreferenceConstants.DB_URL), prefs
				.getString(PreferenceConstants.DB_USER), prefs
				.getString(PreferenceConstants.DB_PASS), prefs
				.getString(PreferenceConstants.DB_DRIVER), prefs
				.getString(PreferenceConstants.DB_DIALECT));
		
		// prepare client's workspace:
		CnAWorkspace.getInstance().prepare();
		
		try {
			ServiceFactory.openCommandService();
		} catch (Exception e) {
			// if this fails, try rewriting config:
			log.error("Exception while connection to command service, forcing recreation of " +
					"service factory configuration from preferences.", e);
			CnAWorkspace.getInstance().prepare(true);
		}
		
		// When the service factory is initialized the client's work objects can be accessed.
		// The line below initializes the VeriniceContext initially.
		VeriniceContext.setState(state = ServiceFactory.getClientWorkObjects());
	}



	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		CnAElementHome.getInstance().close();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public IInternalServer getInternalServer()
	{
		return internalServer;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Initializes the current thread with the VeriniceContext.State
	 * of the client application.
	 * 
	 * <p>Calling this method is needed when the Activator was run on a
	 * different thread then the Application class.</p>
	 */
	public static void inheritVeriniceContextState()
	{
		VeriniceContext.setState(state);
	}
	
	/**
	 * Implementation of {@link IInternalServer} which does nothing.
	 * 
	 * <p>An instance of this class exists when the client does not
	 * use the internal server. There are however codepaths where
	 * methods from an <code>IInternalServer</code> instance are
	 * invoked unconditionally.</p>
	 *
	 */
	private static class ServerDummy implements IInternalServer
	{

		public void configure(String url, String user, String pass,
				String driver, String dialect) {
			// Intentionally do nothing.
		}

		public void start() {
			// Intentionally do nothing.
		}

		public void stop() {
			// Intentionally do nothing.
		}
		
		public boolean isRunning() {
			return true;
		}
		
		public void setGSCatalogURL(URL _) {
			// Intentionally do nothing.
		}
		
		public void setDSCatalogURL(URL _) {
			// Intentionally do nothing.
		}
		
		
	}
}
