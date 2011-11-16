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
package sernet.verinice.iso27k.rcp.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.ActionRightIDs;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CutHandler extends AbstractHandler implements RightEnabledUserInteraction {

private static final Logger LOG = Logger.getLogger(CopyHandler.class);
	
	List<CnATreeElement> selectedElementList = new ArrayList<CnATreeElement>();
	
	private String rightID = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    this.rightID = ActionRightIDs.ISMCUT;
	    if(checkRights()){
    		changeSelection(HandlerUtil.getCurrentSelection(event));
    		CnPItems.clearCopyItems();
    		CnPItems.clearCutItems();
    		CnPItems.setCutItems(selectedElementList);
	    }
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public void changeSelection(ISelection selection) {
		try {
			selectedElementList.clear();
			if(selection instanceof IStructuredSelection) {
				for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
					Object sel = iterator.next();
					if(sel instanceof CnATreeElement) {
						selectedElementList.add((CnATreeElement) sel);
					}
				}			
			}		
		} catch (Exception e) {
			LOG.error("Could not execute selectionChanged", e);
		}
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ISMCUT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO nothing
    }


}
