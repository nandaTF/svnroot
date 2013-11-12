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

import java.util.UUID;

import org.apache.log4j.Logger;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import sernet.verinice.interfaces.AsyncCommandCallResult;
import sernet.verinice.interfaces.IAsyncCommandService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommand;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class QuartzAsyncCommandService implements IAsyncCommandService {

    private static final Logger LOG = Logger.getLogger(QuartzAsyncCommandService.class);
    
    private IJobRegister<ICommand> jobRegister;
    
    /**
     * Factory to create GsmProcessValidator instances
     * configured in veriniceserver-jbpm.xml
     */
    private ObjectFactory commandExecuterJobFactory;
    
    private Scheduler scheduler;
    
    private IAuthService authService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#executeCommand(sernet.verinice.interfaces.ICommand)
     */
    @Override
    public <T extends ICommand> AsyncCommandCallResult<T> executeCommand(T command) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing command asynchronously: " + command.getClass().getSimpleName() + "..."); 
        }
        try {
            // creates a new (prototype) instances of the GsmProcessStarter spring bean
            // see veriniceserver-common.xml and http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-aware-beanfactoryaware
            JobDetailBean job = (JobDetailBean) getCommandExecuterJobFactory().getObject();
            
            String group = getAuthService().getUsername();
            String name = createName(command);
            
            job.setGroup(group);
            job.setName(name);
            
            job.getJobDataMap().put(CommandExecuterJob.KEY_COMMAND, command);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            job.getJobDataMap().put(CommandExecuterJob.KEY_AUTHENTICATION, authentication);
            
            //Register this job to the scheduler           
            getScheduler().addJob(job, true);                
            // execute immediately
            getScheduler().triggerJob(name,group);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("New job triggered, group: " + group + ", name: " + name);
            }
            
            return new AsyncCommandCallResult<T>(group, name) ;
        } catch (SchedulerException e) {
            LOG.error("Error while starting job", e);
            throw new RuntimeException(e);
        }
        
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#isDone(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> boolean isDone(AsyncCommandCallResult<T> futureResult) {
        return getJobRegister().isJobDone(futureResult.getName());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#getResult(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> T getResult(AsyncCommandCallResult<T> futureResult) {
        return (T) getJobRegister().getResult(futureResult.getName());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#clean(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> void clean(AsyncCommandCallResult<T> futureResult) {
        getJobRegister().clean(futureResult.getName());
    }
    
    public String createName(Object job) {
        return job.getClass().getName() + "@" + UUID.randomUUID().toString();
    }
    
    private void addJobListener() {
        try {
            if(getJobRegister()!=null && getScheduler()!=null) {
                if(getScheduler().getGlobalJobListener(JobRegister.NAME)==null) {                  
                        getScheduler().addGlobalJobListener((JobListener) getJobRegister());               
                }
            }
        } catch (SchedulerException e) {
            LOG.error("Error while adding job listener", e);
        }
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        addJobListener();
    }

    public ObjectFactory getCommandExecuterJobFactory() {
        return commandExecuterJobFactory;
    }

    public void setCommandExecuterJobFactory(ObjectFactory commandExecuterJobFactory) {
        this.commandExecuterJobFactory = commandExecuterJobFactory;
    }

    public IJobRegister<ICommand> getJobRegister() {
        return jobRegister;
    }

    public void setJobRegister(IJobRegister<ICommand> jobRegister) {
        this.jobRegister = jobRegister;
        addJobListener();
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
