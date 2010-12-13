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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.interfaces.IProcessCommand;
import sernet.verinice.interfaces.IProcessService;
import sernet.verinice.model.iso27k.Control;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UpdateControlEntity extends UpdateElementEntity<Control> implements IProcessCommand {

    private transient IProcessService processService;
    
    /**
     * @param element
     * @param fireUpdates
     * @param stationId
     */
    public UpdateControlEntity(Control element, boolean fireUpdates, String stationId) {
        super(element, fireUpdates, stationId);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessCommand#getProcessService()
     */
    @Override
    public IProcessService getProcessService() {
        return processService;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessCommand#setProcessService(sernet.verinice.interfaces.IProcessService)
     */
    @Override
    public void setProcessService(IProcessService processService) {
        this.processService = processService;      
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity#beforeUpdate()
     */
    @Override
    protected void beforeUpdate() {
        // TODO Auto-generated method stub
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity#afterUpdate()
     */
    @Override
    protected void afterUpdate() {
        // there is no process service in standalone mode
        if(getProcessService()!=null) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("control", getElement());
            getProcessService().startProcess("control-execution", props);
        }
    }

    

}
