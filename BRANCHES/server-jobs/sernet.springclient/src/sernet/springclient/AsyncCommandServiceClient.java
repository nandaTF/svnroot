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
package sernet.springclient;

import sernet.verinice.interfaces.AsyncCommandCallResult;
import sernet.verinice.interfaces.IAsyncCommandService;
import sernet.verinice.interfaces.ICommand;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AsyncCommandServiceClient implements IAsyncCommandService {

    private IAsyncCommandService asyncCommandService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#executeCommand(sernet.verinice.interfaces.ICommand)
     */
    @Override
    public <T extends ICommand> AsyncCommandCallResult<T> executeCommand(T command) {
        AsyncCommandCallResult<T> result = getAsyncCommandService().executeCommand(command);
        result.setAsyncCommandService(getAsyncCommandService());
        return result;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#getResult(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> T getResult(AsyncCommandCallResult<T> futureResult) {
        return getAsyncCommandService().getResult(futureResult);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#clean(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> void clean(AsyncCommandCallResult<T> futureResult) {
        getAsyncCommandService().clean(futureResult);        
    }

    public IAsyncCommandService getAsyncCommandService() {
        return asyncCommandService;
    }

    public void setAsyncCommandService(IAsyncCommandService asyncCommandService) {
        this.asyncCommandService = asyncCommandService;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAsyncCommandService#isDone(sernet.verinice.interfaces.AsyncCommandCallResult)
     */
    @Override
    public <T extends ICommand> boolean isDone(AsyncCommandCallResult<T> futureResult) {
        return getAsyncCommandService().isDone(futureResult);
    }


}
