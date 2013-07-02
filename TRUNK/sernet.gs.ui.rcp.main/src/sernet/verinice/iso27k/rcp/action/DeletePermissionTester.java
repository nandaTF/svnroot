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

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class DeletePermissionTester extends PropertyTester {

	private static final Logger LOG = Logger.getLogger(DeletePermissionTester.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CnATreeElement selectedElement = (CnATreeElement) receiver;
        boolean allowed = CnAElementHome.getInstance().isNewChildAllowed(selectedElement);           
        if (LOG.isDebugEnabled()) {
            LOG.debug("DeletePermission for " + selectedElement + ": " + allowed);
        }
		return allowed;
	}
	
}
