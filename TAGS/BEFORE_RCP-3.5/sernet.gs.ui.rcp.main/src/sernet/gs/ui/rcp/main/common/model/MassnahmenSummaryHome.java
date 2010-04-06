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
package sernet.gs.ui.rcp.main.common.model;

import java.util.Map;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedLayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.CompletedZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteStepsSummary;
import sernet.gs.ui.rcp.main.service.statscommands.IncompleteZyklusSummary;
import sernet.gs.ui.rcp.main.service.statscommands.LayerSummary;
import sernet.gs.ui.rcp.main.service.statscommands.UmsetzungSummary;

public class MassnahmenSummaryHome {

	public Map<String, Integer> getNotCompletedZyklusSummary() throws CommandException {
		IncompleteZyklusSummary command = new IncompleteZyklusSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedZyklusSummary() throws CommandException {
		CompletedZyklusSummary command = new CompletedZyklusSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getSchichtenSummary()throws CommandException {
		LayerSummary command = new LayerSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedSchichtenSummary()throws CommandException {
		CompletedLayerSummary command = new CompletedLayerSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getNotCompletedStufenSummary()throws CommandException {
		IncompleteStepsSummary command = new IncompleteStepsSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getCompletedStufenSummary() throws CommandException{
		CompletedStepsSummary command = new CompletedStepsSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

	public Map<String, Integer> getUmsetzungenSummary()throws CommandException {
		UmsetzungSummary command = new UmsetzungSummary();
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getSummary();
	}

}
