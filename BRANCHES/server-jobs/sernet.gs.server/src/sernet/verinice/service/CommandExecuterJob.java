/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.service;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.ICommandService;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CommandExecuterJob extends QuartzJobBean implements StatefulJob {

    private static final Logger LOG = Logger.getLogger(CommandExecuterJob.class);
    
    public static final String KEY_COMMAND = "command";
    public static final String KEY_AUTHENTICATION = "authentication";
    
    private ICommand command;
    
    private Authentication authentication;
    
    private ICommandService commandService;
    
    /* (non-Javadoc)
     * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting job execution, group: " + context.getJobDetail().getGroup() + ", name: " + context.getJobDetail().getName() + "...");
        }
        boolean authenticationAdded = false;
        Authentication oldAuthentication = null;
        SecurityContext securityContext = null;
        try {
            if(getCommand()!=null) {
                securityContext = SecurityContextHolder.getContext();
                oldAuthentication = securityContext.getAuthentication();
                securityContext.setAuthentication(getAuthentication());
                authenticationAdded = true;
                command = getCommandService().executeCommand(getCommand()); 
                context.setResult(command);
            } else {
                LOG.warn("Command is null. Nothing to execute");
            }
        } catch (Exception e) {
            LOG.error("Error while executing command", e);
        } finally {
            if(securityContext!=null && authenticationAdded) {
                securityContext.setAuthentication(oldAuthentication);
            }
        }
        
        
    }

    public ICommand getCommand() {
        return command;
    }

    public void setCommand(ICommand command) {
        this.command = command;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

}
