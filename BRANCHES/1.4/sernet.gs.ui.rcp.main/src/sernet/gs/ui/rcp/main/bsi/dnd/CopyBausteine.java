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
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.service.CopyBausteineService;
import sernet.verinice.iso27k.rcp.RcpProgressObserver;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.IProgressRunnable;

/**
 * Operation with copies elements and adds them to a group.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyBausteine implements IProgressRunnable {

	private CopyBausteineService service;
	
	private List<CnATreeElement> selectedElementList;

	private List<Baustein> elements;
	

	@SuppressWarnings("unchecked")
	public CopyBausteine(IStructuredSelection sel, List<Baustein> elements) {
		selectedElementList = new ArrayList<CnATreeElement>();
		if(sel!=null) {
			// extract all selected CnATreeElement as copy-targets
			for (Iterator iter = sel.iterator(); iter.hasNext();) {
				Object o = (Object) iter.next();
				if (o instanceof CnATreeElement) {
					selectedElementList.add((CnATreeElement) o);
				} else {
					// dont't do anything if one element is not a CnATreeElement
					selectedElementList.clear();
					break;
				}
			}
		}
		this.elements = elements;
	}
	
	public CopyBausteine(CnATreeElement element, List<Baustein> elements) {
        selectedElementList = new ArrayList<CnATreeElement>();
        if(element!=null) {
            selectedElementList.add(element);
        }
        this.elements = elements;
    }
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {	
	    IProgressObserver progressObserver = new RcpProgressObserver(monitor);
		service = new CopyBausteineService(progressObserver,this.selectedElementList, elements);
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


    /* (non-Javadoc)
     * @see sernet.verinice.rcp.IProgressRunnable#openInformation()
     */
    @Override
    public void openInformation() {
        // TODO Auto-generated method stub
        
    }

}
