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
package sernet.verinice.service.commands;

import org.apache.log4j.Logger;

import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.GenericCommand;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class WaitCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(WaitCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(WaitCommand.class);
        }
        return log;
    }
    
    private long sleepTimeInMs = 0;
    
    private String result = null;
    
    public static WaitCommand createCommandForSeconds(long seconds) {
        return new WaitCommand(seconds * 1000);
    }
    
    public static WaitCommand createCommandForMinutes(long minutes) {
        return createCommandForSeconds(60*minutes);
    }
    
    public static WaitCommand createCommandForHours(long hours) {
        return createCommandForMinutes(60*hours);
    }
    
    
    /**
     * @param sleepTimeInMs
     */
    public WaitCommand(long sleepTimeInMs) {
        super();
        this.sleepTimeInMs = sleepTimeInMs;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (getLog().isInfoEnabled()) {
            getLog().info("Will sleep now for " + TimeFormatter.getHumanRedableTime(sleepTimeInMs) + "...");
        }
        try {
            Thread.sleep(sleepTimeInMs);
            result = "I had a great time on the verinice server! I stayed here for " + TimeFormatter.getHumanRedableTime(sleepTimeInMs) + ".";
            if (getLog().isInfoEnabled()) {
                getLog().info("Sleeping time (" + TimeFormatter.getHumanRedableTime(sleepTimeInMs) + ") is over.");
            }
        } catch (InterruptedException e) {
            getLog().error("Hey, don't wake me up again. I'm sleeping.");
        }
    }

    public String getResult() {
        return result;
    }

}
