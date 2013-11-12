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
package sernet.gs.ui.rcp.main.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.TimeFormatter;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.rcp.async.Job;
import sernet.verinice.service.commands.WaitCommand;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AsyncCommandCallAction extends Action {
    
    private static final Logger LOG = Logger.getLogger(AsyncCommandCallAction.class);
    
    public static final String ID = "sernet.gs.ui.rcp.main.actions.AsyncCommandCallAction"; //$NON-NLS-1$
    
    public static AsyncCommandCallAction createActionForSeconds(long seconds) {
        return new AsyncCommandCallAction(seconds * 1000);
    }
    
    public static AsyncCommandCallAction createActionForMinutes(long minutes) {
        return createActionForSeconds(60*minutes);
    }
    
    public static AsyncCommandCallAction createActionForHours(long hours) {
        return createActionForMinutes(60*hours);
    }
    
    private long sleepTimeInMs = 0;
    
    /**
     * @param sleepTimeInMs
     */
    public AsyncCommandCallAction(long sleepTimeInMs) {
        super();
        this.sleepTimeInMs = sleepTimeInMs;
        setText("Wait for " + TimeFormatter.getHumanRedableTime(sleepTimeInMs) + " on server...");
        setId(ID + "-" + sleepTimeInMs);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_OUT));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        WaitCommand command = new WaitCommand(sleepTimeInMs);
        try {
            Job job = new Job("Wait command", command);
            JobScheduler.scheduleJob(job, new Mutex());
        } catch (Exception e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),"Error", "Error while calling command asynchronously.");
        }
    }
}
