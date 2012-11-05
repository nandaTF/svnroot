/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Finding;

/**
 *
 */
public class LoadReportISASignificantFindings extends GenericCommand {
    
    private static Logger LOG = Logger.getLogger(LoadReportISASignificantFindings.class);
    
    private static String SHOW_FINDING_IN_REPORT = "finding_showInISAReport";
    private static String FINDING_DESCRIPTION = "finding_desc";
    
    private List<List<String>> result;
    
    private Integer rootElmt;
    
    public static final String[] COLUMNS = new String[]{"TITLE", "DESCRIPTION"};
    
    public LoadReportISASignificantFindings(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            result = new ArrayList<List<String>>(0);
            LoadReportElements command = new LoadReportElements(Finding.TYPE_ID, rootElmt, true);
            command = getCommandService().executeCommand(command);
            if (command.getElements() == null || command.getElements().size() == 0) {
                return;
            }
            for(CnATreeElement c : command.getElements()){
                if(c instanceof Finding){
                    Finding f = (Finding)c;
                    if(Integer.parseInt(f.getEntity().getSimpleValue(SHOW_FINDING_IN_REPORT)) == 1){
                        ArrayList<String> row = new ArrayList<String>(0);
                        row.add(f.getTitle());
                        row.add(f.getEntity().getSimpleValue(FINDING_DESCRIPTION));
                        result.add(row);
                    }
                }
            }
        } catch (CommandException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public List<List<String>> getResult(){
        return result;
    }

}
