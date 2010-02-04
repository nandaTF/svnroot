/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.core.runtime.IProgressMonitor;

import sernet.verinice.iso27k.service.IProgressObserver;

/**
 * RCP implementation of the {@link IProgressObserver}
 * which delegates all action to
 * a {@link IProgressMonitor}.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class RcpProgressObserver implements IProgressObserver {

	private IProgressMonitor monitor;
	
	/**
	 * @param monitor
	 */
	public RcpProgressObserver(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IProgressObserver#isCanceled()
	 */
	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IProgressObserver#processed(int)
	 */
	public void processed(int i) {
		monitor.worked(i);
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IProgressObserver#setTaskName(java.lang.String)
	 */
	public void setTaskName(String text) {
		monitor.setTaskName(text);

	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IProgressObserver#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int numberOfControls) {
		monitor.beginTask(name, numberOfControls);
		
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IProgressObserver#done()
	 */
	public void done() {
		monitor.done();
	}

}
