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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.service.CopyService;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.rcp.IProgressRunnable;

/**
 * Operation with copies elements and adds them to a group.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyTreeElements implements IProgressRunnable {

	private static final Logger LOG = Logger.getLogger(CopyTreeElements.class);

	private IProgressObserver progressObserver;
	
	private CopyService service;
	
	private CnATreeElement selectedGroup;

	private List<CnATreeElement> elements;
	

	@SuppressWarnings("unchecked")
	public CopyTreeElements(CnATreeElement selectedGroup, List<CnATreeElement> elements) {
		this.selectedGroup = selectedGroup;
		this.elements = elements;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {	
		progressObserver = new RcpProgressObserver(monitor);
		service = new CopyService(progressObserver,this.selectedGroup, elements);
		service.run();
	}

	/**
	 * @return
	 */
	public int getNumberOfElements() {
		int n = 0;
		if(service!=null) {
			n = service.getNumberOfElements();
		}
		return n;
	}

}
