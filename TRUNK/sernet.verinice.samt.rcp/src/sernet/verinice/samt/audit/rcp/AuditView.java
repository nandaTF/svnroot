/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.audit.rcp;

import java.util.Collections;
import java.util.List;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.commands.LoadElementByClass;
import sernet.verinice.iso27k.service.commands.LoadLinkedElements;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class AuditView extends GroupView<AuditGroup> {

    public static final String ID = "sernet.verinice.samt.audit.rcp.AuditView"; //$NON-NLS-1$
    
    
    protected List<AuditGroup> getElementList() throws CommandException {
        LoadElementByClass<AuditGroup> command = new LoadElementByClass<AuditGroup>(new AuditGroup());
        command = getCommandService().executeCommand(command);
        final List<AuditGroup> elementList = command.getElementList();
        return elementList;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.GroupView#getLinkedElements(int)
     */
    @Override
    protected List<CnATreeElement> getLinkedElements(int selectedId) throws CommandException {
        LoadLinkedElements<CnATreeElement> command = new LoadLinkedElements<CnATreeElement>(Audit.class,selectedId);
        command = getCommandService().executeCommand(command);
        return command.getElementList();
    }
}
