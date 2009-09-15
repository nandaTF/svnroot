/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - added support for access control
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
import sernet.hui.common.VeriniceContext;

/**
 * Command service that executes commands using hibernate DAOs to access the
 * database.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HibernateCommandService implements ICommandService {
	
	private Logger log = Logger.getLogger(HibernateCommandService.class);

	// injected by spring
	private DAOFactory daoFactory;
	
	private ICommandExceptionHandler exceptionHandler;
	
	private IAuthService authService;
	
	private boolean dbOpen = false;
	
	private VeriniceContext.State workObjects;
	
	private HashMap<String, Object[]> roleMap = new HashMap<String, Object[]>();
	
	/**
	 * This method is encapsulated in a transaction by the Spring container.
	 * Hibernate session will be opened before this method executes the given
	 * command and closed afterwards.
	 * 
	 * Database access in a single transaction is thereby enabled for the
	 * command, the necessary data access objects can be requested from the
	 * given DAO factory.
	 * 
	 * A command can execute other commands to fulfill its purpose using the
	 * reference to the command service.
	 */
	public <T extends ICommand> T executeCommand(T command) throws CommandException {
		VeriniceContext.setState(workObjects);
		
		if (!dbOpen)
			throw new CommandException("DB connection closed.");

//		Logger.getLogger(this.getClass()).debug(
//				"Service executing command: "
//						+ command.getClass().getSimpleName() 
//				+ " / user: " + getAuthService().getUsername());
		
		Logger.getLogger(this.getClass()).debug(
				"Service executing command: "
				+ command.getClass().getSimpleName()); 
		
		try {
			// inject service and database access:
			command.setDaoFactory(daoFactory);
			command.setCommandService(this);

			// inject authentication service if command is aware of it:
			if (command instanceof IAuthAwareCommand) {
				IAuthAwareCommand authCommand = (IAuthAwareCommand) command;
				authCommand.setAuthService(authService);
			}
			
			// When a command is being executed that should be subject to access
			// control (this is the default) and the logged in user is non-
			// privileged the filter is configured and activated.
			if (!(command instanceof INoAccessControl)
					&& !hasAdminRole(authService.getRoles()))
			{
				log.debug("Enabling security access filter for user: " + authService.getUsername());
				setAccessFilterEnabled(true);
			}
			
			// execute actions, compute results:
			command.execute();
			
			setAccessFilterEnabled(false);
			
			// log changes:
			if (command instanceof IChangeLoggingCommand) {
				log((IChangeLoggingCommand) command);
			}			
			
			// clean up:
			command.clear();
		} 
		catch (Exception e) {
			// TODO ak kein exception handler -> ganz böse
			if (exceptionHandler != null)
				exceptionHandler.handle(e);
		}
		return command;
	}

	private void log(IChangeLoggingCommand notifyCommand) {
		List<CnATreeElement> changedElements = notifyCommand.getChangedElements();
		for (CnATreeElement changedElement : changedElements) {
			
			// save reference to element, if it has not been deleted:
			CnATreeElement referencedElement = null;
			if (notifyCommand.getChangeType() != ChangeLogEntry.TYPE_DELETE)
				referencedElement = changedElement;
				
			ChangeLogEntry logEntry = new ChangeLogEntry(changedElement,
					notifyCommand.getChangeType(),
					getAuthService().getUsername(),
					notifyCommand.getStationId(),
					GregorianCalendar.getInstance().getTime());
			log(logEntry, referencedElement);
		}
	}

	/**
	 * @param logEntry
	 */
	private void log(ChangeLogEntry logEntry, CnATreeElement referencedElement) {
		Logger.getLogger(this.getClass()).debug("Logging change type '" + logEntry.getChangeDescription() 
				+ "' for element of type " + logEntry.getElementClass() + " with ID " + logEntry.getElementId());
		daoFactory.getDAO(ChangeLogEntry.class).saveOrUpdate(logEntry);
	}

	/**
	 * Injected by spring framework
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory) {
		dbOpen = true;
		this.daoFactory = daoFactory;
	}

	public ICommandExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(ICommandExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService authService) {
		this.authService = authService;
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		this.workObjects = workObjects;
	}

	public VeriniceContext.State getWorkObjects() {
		return workObjects;
	}
	
	private void setAccessFilterEnabled(boolean enable)
	{
		IBaseDao<BSIModel, Serializable> dao = daoFactory.getDAO(BSIModel.class);
	
		if (enable)
		{
			final Object[] roles = getRolesAsParameterList(authService.getUsername());
			
			dao.executeCallback(new HibernateCallback()
			{

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					session.enableFilter("userAccessReadFilter")
						.setParameterList("currentRoles",roles)
						.setParameter("readAllowed", Boolean.TRUE);
					return null;
				}
				
			});
		}
		else
		{
			dao.executeCallback(new HibernateCallback()
			{

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					
					session.disableFilter("userAccessReadFilter");
					
					return null;
				}
				
			});
			
		}
	}
	
	private boolean hasAdminRole(String[] roles)
	{
		for (String r : roles)
		{
			if (ApplicationRoles.ROLE_ADMIN.equals(r))
				return true;
		}
		
		return false;
	}
	
	private Object[] getRolesAsParameterList(String user)
	{
		Object[] result = roleMap.get(user);
		if (result == null)
		{
			IBaseDao<Configuration, Serializable> dao = daoFactory.getDAO(Configuration.class);
			List<Configuration> configurations = dao.findAll();
			
			for (Configuration c : configurations)
			{
				if (user.equals(c.getUser()) && result == null)
				{
					result = c.getRoles().toArray();
					configurations.clear();
					
					// Put result into map and save asking the DB next time.
					roleMap.put(user, result);
					
					// TODO: Whenever an admin modifies the roles the roleMap should be cleared.
					// We could introduce a special command just for this.
					
					return result;
				}
			}
			
			// This should not happen because the login was done using an existing username
			// and if that does not exist any more something must have gone wrong.
			throw new IllegalStateException();

		}
		
		return result;
	}
	
	public void discardRoleMap()
	{
		roleMap.clear();
	}
	
}
