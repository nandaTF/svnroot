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
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 */
public class LoadReportScenarioGroupColourCount extends GenericCommand implements ICachedCommand{
    
    private static transient Logger LOG = Logger.getLogger(LoadReportScenarioGroupColourCount.class); 

    private Integer rootElmt;
    
    private List<ArrayList<String>> results;
    
    private int[] numOfYellowFields;
    
    private HashMap<String, Object> localCommandCache;
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[] { 
        "GROUPTITLE",
        "COUNTRED",
        "COUNTYELLOW",
        "SCENARIOCOUNT"
        };
    
    public LoadReportScenarioGroupColourCount(Integer root, int[] yellowFields){
        this.rootElmt = root;
        results = new ArrayList<ArrayList<String>>(0);
        this.numOfYellowFields = yellowFields;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                LoadReportRedYellowScenarioGroups groupLoader = new LoadReportRedYellowScenarioGroups(rootElmt, numOfYellowFields);
                groupLoader = getCommandService().executeCommand(groupLoader);
                for(List<String> list : groupLoader.getResults()){
                    int overallCount = 0;
                    int groupdbid = Integer.parseInt(list.get(2));
                    LoadPolymorphicCnAElementById elmtLoader = new LoadPolymorphicCnAElementById(new Integer[]{new Integer(groupdbid)});
                    CnATreeElement elmt = getCommandService().executeCommand(elmtLoader).getElements().get(0);
                    if(elmt.getParent().getDbId().intValue() != elmt.getScopeId().intValue()){
                        int redCount = 0;
                        int yellowCount = 0;
                        ArrayList<String> result = new ArrayList<String>(0);
                        result.add(list.get(0));
                        LoadReportNotGreenScenarios scenarioColourLoader = new LoadReportNotGreenScenarios(groupdbid, numOfYellowFields);
                        scenarioColourLoader = getCommandService().executeCommand(scenarioColourLoader);
                        for(List<String> scenarioResult : scenarioColourLoader.getResult()){
                            if(scenarioResult.get(1).equals("red")){
                                redCount++;
                                overallCount++;
                            } else if(scenarioResult.get(1).equals("yellow")){
                                yellowCount++;
                                overallCount++;
                            }
                        }
                        result.add(String.valueOf(redCount));
                        result.add(String.valueOf(yellowCount));
                        result.add(String.valueOf(overallCount));
                        results.add(result);
                    } 
                }
            } catch (CommandException e){
                getLog().error("Errow while executing command", e);
            }
        }
    }
    
    private Logger getLog(){
        if(LOG == null){
            LOG = Logger.getLogger(LoadReportScenarioGroupColourCount.class);
        }
        return LOG;
    }
    
    public List<ArrayList<String>> getResult(){
        return results;
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder(); 
        cacheID.append(String.valueOf(this.getClass().getSimpleName().hashCode()));
        cacheID.append(String.valueOf(rootElmt.hashCode()));
        for(int i : numOfYellowFields){
            cacheID.append(String.valueOf(i));
        }
        return cacheID.toString();
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<ArrayList<String>>) result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }

}
