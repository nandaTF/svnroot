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
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.report.service.impl.TocHelper2;

/**
 *
 */
public class LoadISAQuestionRadarChartData extends GenericCommand {
    
    private static transient Logger LOG = Logger.getLogger(LoadISAQuestionRadarChartData.class);
    private static final int THRESHOLD_VALUE = 3;
    
    public static String[] COLUMNS = new String[]{"title", 
                                                  "riskValue",
                                                  "threshold"
                                                 };
    
    private static final int MINIMUM_CHART_ENTRIES = 7;

    private Integer rootElmnt;
    
    private List<List<String>> result;
    
    public LoadISAQuestionRadarChartData(Integer root){
        this.rootElmnt = root;
        result = new ArrayList<List<String>>(0);
    }
    
    public LoadISAQuestionRadarChartData(String root){
        this(new Integer(Integer.parseInt(root)));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            LoadReportElements command = new LoadReportElements(SamtTopic.TYPE_ID, rootElmnt, true);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> elements = command.getElements();
            for(CnATreeElement e : elements){
                if(e instanceof SamtTopic){
                    SamtTopic topic = (SamtTopic)e;
                    ArrayList<String> list = new ArrayList<String>(0);
                    list.add(adjustTitle(topic.getTitle()));
                    list.add(String.valueOf(getMaturityByWeight(topic)));
                    list.add(String.valueOf(THRESHOLD_VALUE));
                    list.trimToSize();
                    result.add(list);
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while determing samt topics", e);
        }
        
    }
    
    public List<List<String>> getResult(){
        if(result.size() < MINIMUM_CHART_ENTRIES){
            addPaddingValues();
        }
        return result;
    }
    
    public void addPaddingValues(){
        while(result.size() < MINIMUM_CHART_ENTRIES){
            ArrayList<String> paddingEntry = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < result.size(); i++){
                sb.append(" ");
            }
            paddingEntry.add(sb.toString());
            paddingEntry.add(String.valueOf(0));
            paddingEntry.add(String.valueOf(THRESHOLD_VALUE));
            result.add(paddingEntry);
        }
    }
    
    public Integer getWeights(ControlGroup cg) {
        int weight = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                weight += control.getWeight2();
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                weight += getWeights(control);
            }
        }
        return weight;
    }
    
    /**
     * @return
     */
    public Integer getWeightedMaturity(IControl contr) {
        int value = getMaturity(contr) * contr.getWeight2();
        return value;
    }
    
    public Double getMaturityByWeight(IControl contr) {
        double result = ((double)getWeightedMaturity(contr)) / ((double)contr.getWeight2());
        return result;
    }
    
    public int getMaturity(IControl control) {
        return control.getMaturity();
    }
    
    private String adjustTitle(String title){
        if(TocHelper2.getStringDisplaySize(title) > 50){
            StringBuilder sb = new StringBuilder();
            StringTokenizer tokenizer = new StringTokenizer(title); // space is one of the standard delimiters
            while(tokenizer.hasMoreElements()){
                sb.append(tokenizer.nextToken());
                if(TocHelper2.getStringDisplaySize(title) > 25){
                    sb.append("\n");
                } else {
                    sb.append(" ");
                }
               
            }
            
        }
        
        return title;
    }
    
}
