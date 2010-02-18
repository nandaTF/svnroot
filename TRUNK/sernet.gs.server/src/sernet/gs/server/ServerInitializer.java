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
package sernet.gs.server;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.IHibernateCommandService;
import sernet.hui.common.VeriniceContext;

/**
 * Initialize environemnt on Verinice server on startup.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerInitializer {
	
	private final Logger log = Logger.getLogger(ServerInitializer.class);
	
	private static VeriniceContext.State state;
	
	private IHibernateCommandService hibernateCommandService;
	
	private IProgress nullMonitor = new IProgress() {
		public void beginTask(String name, int totalWork) {
		}

		public void done() {
		}

		public void setTaskName(String string) {
		}

		public void subTask(String string) {
		}

		public void worked(int work) {
		}
		
	};
	
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

	public void initialize() {
		Logger.getLogger(this.getClass()).debug("Initializing server context...");
		// After this we can use the getInstance() methods from HitroUtil and
		// GSScraperUtil
		VeriniceContext.setState(state);
		
		// The work objects in the HibernateCommandService can only be set
		// at this point because otherwise we would have a circular dependency
		// in the Spring configuration (= commandService needs workObjects
		// and vice versa)
		hibernateCommandService.setWorkObjects(state);
		
		GSScraperUtil gsScraperUtil = GSScraperUtil.getInstance();
		// initialize grundschutz scraper:
		try {
			gsScraperUtil.getModel().loadBausteine(nullMonitor);
		} catch (Exception e) {
			log.error("Fehler beim Laden der Grundschutzkataloge: " + e.getMessage());
			if (log.isDebugEnabled()) {
				log.debug("stacktrace: " + e);
			}
		}
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		ServerInitializer.state = workObjects;
	}

	public VeriniceContext.State getWorkObjects() {
		return state;
	}

	public void setHibernateCommandService(IHibernateCommandService hibernateCommandService) {
		this.hibernateCommandService = hibernateCommandService;
	}

	public IHibernateCommandService getHibernateCommandService() {
		return hibernateCommandService;
	}

}
