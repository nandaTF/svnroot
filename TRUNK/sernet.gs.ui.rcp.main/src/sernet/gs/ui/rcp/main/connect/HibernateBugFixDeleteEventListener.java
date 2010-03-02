/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.connect;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.event.EventSource;
import org.hibernate.event.def.DefaultDeleteEventListener;
import org.hibernate.persister.entity.EntityPersister;

/**
 * Workaround for Hibernate Bug HHH-2146, see
 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-2146?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#action_26488
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HibernateBugFixDeleteEventListener extends
		DefaultDeleteEventListener {
	protected void deleteTransientEntity(EventSource session, Object entity,
			boolean cascadeDeleteEnabled, EntityPersister persister,
			Set transientEntities) {
		super.deleteTransientEntity(session, entity, cascadeDeleteEnabled,
				persister, transientEntities == null ? new HashSet()
						: transientEntities);
	}
}
