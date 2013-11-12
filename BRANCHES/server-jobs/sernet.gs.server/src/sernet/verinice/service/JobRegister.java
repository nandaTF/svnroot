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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import sernet.verinice.interfaces.ICommand;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JobRegister implements IJobRegister<ICommand>, JobListener {

    public static final String NAME = JobRegister.class.getName(); 
    
    public static final String FINISHED_KEY_SUFFIX = "_FINISHED";
    public static final String RESULT_KEY_SUFFIX = "_RESULT";
    
    private JobCache jobCache;
    
    /* (non-Javadoc)
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IJobRegister#isJobDone(java.lang.String)
     */
    @Override
    public boolean isJobDone(String name) {
        return getJobCache().get(createFinishedJobKey(name))!=null;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IJobRegister#getResult(java.lang.String)
     */
    @Override
    public ICommand getResult(String name) {
        return (ICommand) getJobCache().get(createResultJobKey(name));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IJobRegister#clean(java.lang.String)
     */
    @Override
    public void clean(String name) {
        getJobCache().remove(createFinishedJobKey(name));
        getJobCache().remove(createResultJobKey(name));
    }

    /* (non-Javadoc)
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
        getJobCache().put(createFinishedJobKey(context),context.getJobDetail().getName());
        getJobCache().put(createResultJobKey(context),context.getResult());
    }
    
    private String createFinishedJobKey(JobExecutionContext context) {
        String name = context.getJobDetail().getName();
        return createFinishedJobKey(name);
    }
    
    private String createResultJobKey(JobExecutionContext context) {
        String name = context.getJobDetail().getName();
        return createResultJobKey(name);
    }

    protected String createFinishedJobKey(String name) {
        StringBuilder sb = new StringBuilder(name);
        return sb.append(FINISHED_KEY_SUFFIX).toString();
    }
    
    protected String createResultJobKey(String name) {
        StringBuilder sb = new StringBuilder(name);
        return sb.append(RESULT_KEY_SUFFIX).toString();
    }

    public JobCache getJobCache() {
        return jobCache;
    }

    public void setJobCache(JobCache jobCache) {
        this.jobCache = jobCache;
    }

}
