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
 *     Robert Schuster <r.schuster@tarent.de> - use custom SQL/HQL for query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLinkRoot;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.swt.widgets.URL.URLUtil;

/**
 * Retrieves the properties which are URLs as a {@link DocumentLinkRoot}
 * structure.
 */
public class FindURLs extends GenericCommand {

	private static final long serialVersionUID = 9207422070204886804L;

	private static final Logger log = Logger.getLogger(FindURLs.class);

	private DocumentLinkRoot root = new DocumentLinkRoot();

	private HibernateCallback hcb;

	public FindURLs(Set<String> allIDs) {
		hcb = new FindURLsCallbackWithCnATreeElement(allIDs);
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		
		/* Requests the links and resolves the CnATreeElement instances which use
		 * them. (This is needed for the {@link DocumentView}).
		 * 
		 * The result is a list of Object arrays where: The URL is at index 0 and
		 * the dbid of an CnATreeElement is at index 1.
		 */
		List<Object[]> resultList = (List<Object[]>) dao.findByCallback(hcb);
		
		// Creates a list of Integers from the second argument. This is needed in the
		// FindCnATreeElementsCallback.
		List<Integer> treeElementIds = new ArrayList<Integer>();
		for (Object[] result : resultList)
		{
			treeElementIds.add((Integer) result[1]);
		}
		
		// Retrieves all the CnATreeElement instances which have a document link
		// according to the query contained in FindURLsCallbackWithCnATreeElement.
		List<CnATreeElement> treeElements = (List<CnATreeElement>)
			dao.findByCallback(new FindCnATreeElementsCallback(treeElementIds));
		
		// Fills the DocumentRoot structure by iterating the URLs and preparing the
		// individual DocumentReference instances.
		final int size = resultList.size();
		for (int i = 0; i < size; i++)
		{
			String rawURL = (String) resultList.get(i)[0];
			
			String name = URLUtil.getName(rawURL);
			String url = URLUtil.getHref(rawURL);
			
			DocumentLink link = root.getDocumentLink(name, url);
			if (link == null) {
				link = new DocumentLink(name, url);
				root.addChild(link);
			}
			
			CnATreeElement element = treeElements.get(i);
			
			DocumentReference reference = new DocumentReference(element);
			element.getTitel();

			link.addChild(reference);
		}
	}

	public DocumentLinkRoot getUrls() {
		return root;
	}

	@SuppressWarnings("serial")
	private class FindURLsCallbackWithCnATreeElement implements
			HibernateCallback, Serializable {
		
		private Set<String> types;
		
		FindURLsCallbackWithCnATreeElement(Set<String> types) {
			this.types = types;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			/**
			 * Retrieves the property elements which are URLs along with
			 * the CnATreeElement id that uses it.
			 */
			 Query query = session.createSQLQuery(
					"select p.propertyValue, e.dbid "
							+ "from properties p, entity e "
							+ "where p.propertytype in (:types) "
							+ "and p.propertyvalue != '' "
							+ "and p.parent = e.dbid ")
					.addScalar("propertyvalue", Hibernate.STRING)
					.addScalar("dbid", Hibernate.INTEGER)
					.setParameterList("types", types, Hibernate.STRING);
							
			 if (log.isDebugEnabled())
				 log.debug("created statement: " + query.getQueryString());

			return query.list();
		}

	}
	
	@SuppressWarnings("serial")
	private class FindCnATreeElementsCallback implements HibernateCallback, Serializable
	{
		private List<Integer> treeElementIds;
		
		public FindCnATreeElementsCallback(List<Integer> treeElementIds) {
			this.treeElementIds = treeElementIds;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			/*
			 * Retrieves all the CnATreeElements whose ids are mentioned in the
			 * treeElementIds list.
			 */
			Query query = session.createQuery(
					"from CnATreeElement elmt "
					+ "where elmt.entity.dbId in (:treeElementIds)")
					.setParameterList("treeElementIds", treeElementIds);

			if (log.isDebugEnabled())
				log.debug("created statement: " + query.getQueryString());
			
			return query.list();
		}
		
	}

}
