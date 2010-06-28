package sernet.verinice.report.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import sernet.verinice.interfaces.ICommandService;

public class Activator implements BundleActivator {
	
    // The shared instance
    private static Activator plugin;
    
    private ServiceTracker commandServiceTracker;
	
	public void start(BundleContext context) throws Exception {
		plugin = this;

		// Reach ICommandService implementation via service tracker since the instance
		// is provided via Spring (and should not be instantiated by OSGi)
		commandServiceTracker = new ServiceTracker(context, ICommandService.class.getName(), null);
		commandServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		commandServiceTracker.close();
	}
	
	public static Activator getDefault()
	{
		return plugin;
	}

	public ICommandService getCommandService()
	{
		return (ICommandService) commandServiceTracker.getService();
	}
	
}
