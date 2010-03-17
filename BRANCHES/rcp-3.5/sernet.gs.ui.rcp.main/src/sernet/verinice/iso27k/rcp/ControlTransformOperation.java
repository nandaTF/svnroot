/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.service.ControlTransformService;
import sernet.verinice.iso27k.service.IProgressObserver;

/**
 * Operation with transforms items from the {@link CatalogView}
 * to {@link Control}s. Created controls are added to a {@link ControlGroup}.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ControlTransformOperation implements IRunnableWithProgress {

	private static final Logger LOG = Logger.getLogger(ControlTransformOperation.class);

	private IProgressObserver progressObserver;
	
	private ControlTransformService service;
	
	@SuppressWarnings("unchecked")
	private Group selectedGroup;
	

	@SuppressWarnings("unchecked")
	public ControlTransformOperation(Group selectedGroup) {
		this.selectedGroup = selectedGroup;	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {	
		progressObserver = new RcpProgressObserver(monitor);
		service = new ControlTransformService(progressObserver,this.selectedGroup);
		service.run();
	}

	/**
	 * @return
	 */
	public int getNumberOfControls() {
		int n = 0;
		if(service!=null) {
			n = service.getNumberOfControls();
		}
		return n;
	}

}
