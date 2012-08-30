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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.CSRMassnahmenSummaryHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 *
 */
public class LoadReportAvgMaturity extends GenericCommand {
    
    private static Logger LOG = Logger.getLogger(LoadReportAvgMaturity.class);
    
    private int matCount = 0;
    private double matSum = 0.0;
    
    private List<List<String>> result;
    
    private Integer rootElmt;
    
    public static final String[] COLUMNS = new String[]{"avgMaturity", "SGID"};
    
    public LoadReportAvgMaturity(Integer root){
        this.rootElmt = root;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        result = new ArrayList<List<String>>(0);
        try{
            FindSGCommand command = new FindSGCommand(true, rootElmt);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            ControlGroup cg = command.getSelfAssessmentGroup();
            ArrayList<ControlGroup> list = new ArrayList<ControlGroup>();
            list.add(cg);
            for(ControlGroup g : list){
                CSRMassnahmenSummaryHome dao = new CSRMassnahmenSummaryHome();
                Map<String, Double> items1 = dao.getControlGroups(g);
                for(Entry<String, Double> entry : items1.entrySet()){
                    addMaturity(entry.getValue());
                }
            }
            ArrayList<String> tmplist = new ArrayList<String>(0);
            tmplist.add(String.valueOf(getMaturityAvg()));
            tmplist.add(String.valueOf(cg.getDbId()));
            result.add(tmplist);
        } catch (Exception e){
            getLog().error("Error while computing avgMaturity", e);
        }
    }
        
    private Logger getLog(){
        if(LOG == null){
            LOG = Logger.getLogger(LoadReportAvgMaturity.class);
        }
        return LOG;
    }

    private void addMaturity(double mat){
        if(mat > 0){
            matSum += mat;
            matCount++;
        }
    }

    private String getMaturityAvg(){
        if(matCount > 0){
            double d = new Double(matSum).doubleValue() / new Double(matCount).doubleValue();
            String s = String.valueOf(round(d, 3));
            return s;
        } else return "0.0";
    }
    
    private double round(double value, int precision)
    {
        double rounded = Math.round(value * Math.pow(10d, precision));
        return rounded / Math.pow(10d, precision);
    } 
    
    public List<List<String>> getResult(){
        return result;
    }

}
