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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.iso27k.service.CutService;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Operation with copies elements and adds them to a group.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class CutOperation implements IRunnableWithProgress {

	private CutService service;
	
	private CnATreeElement selectedGroup;

	private List<CnATreeElement> elements;
	
	private List<ElementChange> changes;
	
	private boolean inheritPermissions = false;

	@SuppressWarnings("unchecked")
	public CutOperation(CnATreeElement selectedGroup, List<CnATreeElement> elements) {
		this.selectedGroup = selectedGroup;
		this.elements = elements;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {	
	    IProgressObserver progressObserver = new RcpProgressObserver(monitor);
		service = new CutService(progressObserver,this.selectedGroup, elements);
		service.setInheritPermissions(isInheritPermissions());
		service.run();
		changes = service.getElementChanges();
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


    public List<ElementChange> getChanges() {
        return changes;
    }


    public boolean isInheritPermissions() {
        return inheritPermissions;
    }


    public void setInheritPermissions(boolean inheritPermissions) {
        this.inheritPermissions = inheritPermissions;
    }

}
