package sernet.verinice.encryption;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import sernet.verinice.encryption.impl.EncryptionService;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.verinice.encryption";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		context.registerService(IEncryptionService.class.getName(), new EncryptionService(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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

}
