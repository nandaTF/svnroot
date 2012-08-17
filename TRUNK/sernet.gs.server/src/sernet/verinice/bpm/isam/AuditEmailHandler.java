/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm.isam;

import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.bpm.GenericEmailHandler;
import sernet.verinice.bpm.IEmailHandler;
import sernet.verinice.bpm.IRemindService;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AuditEmailHandler extends GenericEmailHandler implements IEmailHandler {
    
    private static final Logger LOG = Logger.getLogger(AuditEmailHandler.class);
    
    private static final String TEMPLATE = "AuditReminder";
    
    private static final String TEMPLATE_ELEMENT_TITLE = "elementTitle";
  
    public void addParameter(String uuidElement, Map<String, String> parameter) {
        CnATreeElement element = getRemindService().retrieveElement(uuidElement, RetrieveInfo.getPropertyInstance());
        String title = element.getTitle();
        parameter.put(TEMPLATE_ELEMENT_TITLE, title);
        parameter.put(IRemindService.TEMPLATE_SUBJECT, "Audit starts in 6 weeks: " + title);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#getTemplate()
     */
    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

 
}
