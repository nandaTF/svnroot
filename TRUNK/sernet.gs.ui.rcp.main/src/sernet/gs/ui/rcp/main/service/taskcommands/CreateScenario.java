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

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.CreateLink;

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
    private static final String THREAT_RELATION_ID = "rel_incscen_threat";
    private static final String VULN_RELATION_ID = "rel_incscen_vulnerability";

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
            // FIXME externalize strings
            
            CreateElement<IncidentScenario> cmd = new CreateElement<IncidentScenario>(group, IncidentScenario.class, true);
            cmd = getCommandService().executeCommand(cmd);
            IncidentScenario incidentScenario = cmd.getNewElement();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Scenario: ");
            sb.append(threat.getTitle().substring(0, threat.getTitle().length()<21 ? threat.getTitle().length() : 20  ));
            sb.append(threat.getTitle().length()<21 ? "" : "[...]");
            sb.append(" - ");
            
            sb.append(vulnerability.getTitle().substring(0, vulnerability.getTitle().length()<21 ? vulnerability.getTitle().length() : 20));
            sb.append(vulnerability.getTitle().length()<21 ? "" : "[...]");
            
            incidentScenario.setTitel(sb.toString());
            
            CreateLink<CnALink, Threat, IncidentScenario> cmd2 = new CreateLink<CnALink, Threat, IncidentScenario>(threat, incidentScenario, THREAT_RELATION_ID );
            cmd2 = getCommandService().executeCommand(cmd2);

            CreateLink<CnALink, Vulnerability, IncidentScenario> cmd3 = new CreateLink<CnALink, Vulnerability, IncidentScenario>(vulnerability, incidentScenario, VULN_RELATION_ID);
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


