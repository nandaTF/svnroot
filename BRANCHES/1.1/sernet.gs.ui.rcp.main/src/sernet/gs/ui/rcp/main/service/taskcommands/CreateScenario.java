/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.proxy.HibernateProxyHelper;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateLink;
import sernet.verinice.iso27k.model.IncidentScenario;
import sernet.verinice.iso27k.model.IncidentScenarioGroup;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.model.Threat;
import sernet.verinice.iso27k.model.Vulnerability;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class CreateScenario extends GenericCommand {

    private Integer threatdbId;
    private Integer vulndbId;
    private IncidentScenario incScen;

    /**
     * @param threat
     * @param vuln
     */
    public CreateScenario(Threat threat, Vulnerability vuln) {
        threatdbId = threat.getDbId();
        vulndbId = vuln.getDbId();
        
    }
    
    /**
     * @param threat
     * @return
     */
    private Organization findOrganization(CnATreeElement elmt) {
        if (elmt.getParent().getTypeId().equals(Organization.TYPE_ID)) {
            return getDaoFactory().getDAO(Organization.class).findById(elmt.getParent().getDbId());
        }
        return findOrganization(elmt.getParent());
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    public void execute() {
        IBaseDao<Threat, Serializable> threatDao = getDaoFactory().getDAO(Threat.class);
        IBaseDao<Vulnerability, Serializable> vulnDao = getDaoFactory().getDAO(Vulnerability.class);
        Threat threat = threatDao.findById(threatdbId);
        Vulnerability vulnerability = vulnDao.findById(vulndbId);
        
        Organization org = findOrganization(threat);
        IncidentScenarioGroup group = findScenarioGroup(org);
        if (group == null)
            return;
        
        try {
            CreateElement<IncidentScenario> cmd = new CreateElement<IncidentScenario>(group, IncidentScenario.class, true);
            cmd = getCommandService().executeCommand(cmd);
            IncidentScenario incidentScenario = cmd.getNewElement();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Scenario: ");
            sb.append(threat.getTitle().substring(0, threat.getTitle().length()<21 ? threat.getTitle().length() : 20  ));
            sb.append("[...]");
            sb.append(vulnerability.getTitle().substring(0, vulnerability.getTitle().length()<21 ? vulnerability.getTitle().length() : 20));
            sb.append("[...]");
            incidentScenario.setTitel(sb.toString());
            
            CreateLink<CnALink, IncidentScenario, Threat> cmd2 = new CreateLink<CnALink, IncidentScenario, Threat>(threat, incidentScenario);
            cmd2 = getCommandService().executeCommand(cmd2);

            CreateLink<CnALink, IncidentScenario, Vulnerability> cmd3 = new CreateLink<CnALink, IncidentScenario, Vulnerability>(vulnerability, incidentScenario);
            cmd3 = getCommandService().executeCommand(cmd3);

            this.incScen = incidentScenario;
        } catch (CommandException e) {
            throw new RuntimeCommandException(e);
        }
    }

    /**
     * @param org
     * @return
     */
    private IncidentScenarioGroup findScenarioGroup(Organization org) {
        Set<CnATreeElement> children = org.getChildren();
        for (CnATreeElement cnATreeElement : children) {
            if (cnATreeElement.getTypeId().equals(IncidentScenarioGroup.TYPE_ID))
                return (IncidentScenarioGroup) cnATreeElement;
        }
        return null;
    }
    
    public IncidentScenario getNewElement() {
        return incScen;
    }

}


