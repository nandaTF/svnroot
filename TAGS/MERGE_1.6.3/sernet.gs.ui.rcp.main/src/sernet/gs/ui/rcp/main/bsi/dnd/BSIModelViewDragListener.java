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
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BausteinUmsetzungTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IBSIStrukturElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ISO27kGroupTransfer;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.IMassnahmeUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.iso27k.IISO27kElement;

public class BSIModelViewDragListener implements DragSourceListener {

	private TreeViewer viewer;
	
	public BSIModelViewDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
    public void dragFinished(DragSourceEvent event) {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
    public void dragSetData(DragSourceEvent event) {
	    IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
	    if(validateDrag(event)){
	        event.data = DNDHelper.castDataArray(selection.toArray());
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
    public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
		if(selection==null) {
			event.doit = false;
			return;	
		}
		List selectionList = new ArrayList(selection.size());
		
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			selectionList.add(o);
			if (!(o instanceof BausteinUmsetzung || o instanceof IBSIStrukturElement || o instanceof IISO27kElement || o instanceof IMassnahmeUmsetzung)
				 || o instanceof ITVerbund) {
				event.doit = false;
				return;	
			}
		}
		event.doit = true;
	}
	
	/**
	 * @param event
	 * @return
	 */
	private boolean validateDrag(DragSourceEvent event){
        return (BausteinElementTransfer.getInstance().isSupportedType(event.dataType)
                || IBSIStrukturElementTransfer.getInstance().isSupportedType(event.dataType)
                || IBSIStrukturElementTransfer.getInstance().isSupportedType(event.dataType)
                || BausteinUmsetzungTransfer.getInstance().isSupportedType(event.dataType)
                || ISO27kElementTransfer.getInstance().isSupportedType(event.dataType)
                || ISO27kGroupTransfer.getInstance().isSupportedType(event.dataType));
	}

}
