/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
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
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.data.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.sync.SyncRequest;

/**
 * Creates an XML representation of the given list of
 * CnATreeElements.
 * 
 * @author <andreas[at]becker[dot]name>
 */
@SuppressWarnings("serial")
public class ExportCommand extends GenericCommand
{
	
	// Blacklist of object types that should not be exported as an object:
	private static HashSet<String> blacklist = new HashSet<String>();
	
	/*+++
	 * List of already-exported objects' IDs, to
	 * prevent multiple inclusion of a single object
	 * in <syncData>, e.g. due to explicitly selecting
	 * a father element and one of its successors:
	 *++++++++++++++++++++++++++++++++++++++++++++++++*/
	private HashMap<String, String> exportedObjectIDs = new HashMap<String, String>();
	
	static
	{
		blacklist.add(RaeumeKategorie.TYPE_ID);
		blacklist.add(NKKategorie.TYPE_ID);
		blacklist.add(ServerKategorie.TYPE_ID);
		blacklist.add(PersonenKategorie.TYPE_ID);
		blacklist.add(GebaeudeKategorie.TYPE_ID);
		blacklist.add(ClientsKategorie.TYPE_ID);
		blacklist.add(AnwendungenKategorie.TYPE_ID);
		blacklist.add(SonstigeITKategorie.TYPE_ID);
		blacklist.add(TKKategorie.TYPE_ID);
	}
	
	private List<CnATreeElement> elements;
	private String sourceId;
	private HashMap<String,String> entityTypesToBeExported;
	
	private byte[] result;
	
	public ExportCommand( List<CnATreeElement> elements, String sourceId )
	{
		this.elements = elements;
		this.sourceId = sourceId;
	}
	
	public ExportCommand( List<CnATreeElement> elements, String sourceId, HashMap<String,String> entityTypesToBeExported )
	{
		this( elements, sourceId );
		this.entityTypesToBeExported = entityTypesToBeExported; 
	}
	
	public void execute()
	{
		String timestamp = Long.toString(Calendar.getInstance().getTimeInMillis());
		exportedObjectIDs = new HashMap<String, String>();
		
		SyncData sd = new SyncData();
		
		SyncMapping sm = new SyncMapping();
		List<MapObjectType> mapObjectTypeList = sm.getMapObjectType();
		
		SyncRequest sr = new SyncRequest();
		sr.setSourceId(sourceId);
		sr.setSyncData(sd);
		sr.setSyncMapping(sm);
		
		/*+++++
		 * Add one <syncObject> element for each
		 * given CnATreeElement to <syncData>: 
		 *+++++++++++++++++++++++++++++++++++++++*/
		for( CnATreeElement element : elements )
		{
			export(sd.getSyncObject(), element, timestamp);
		}
		
		/*+++++
		 * Add Sync Mapping, which in our case describes
		 * the identity map, i.e. it maps every object type
		 * and every attribute type to itself. 
		 *+++++++++++++++++++++++++++++++++++++++++++++++++*/
		
		Collection<EntityType> entityTypes = HUITypeFactory.getInstance().getAllEntityTypes();
		Iterator<EntityType> entityTypesIter = entityTypes.iterator();
		
		while( entityTypesIter.hasNext() )
		{
			EntityType entityType = entityTypesIter.next();
			
			// Add <mapObjectType> element for this entity type to <syncMapping>:
			MapObjectType mapObjectType = new MapObjectType();
			
			mapObjectType.setIntId(entityType.getId());
			mapObjectType.setExtId(entityType.getId());
			
			List<MapAttributeType> mapAttributeTypes = mapObjectType.getMapAttributeType();
			for (PropertyType propertyType : entityType.getPropertyTypes())
			{
				// Add <mapAttributeType> for this property type to current <mapObjectType>:
				MapAttributeType mapAttributeType = new MapAttributeType();
				
				mapAttributeType.setExtId(propertyType.getId());
				mapAttributeType.setIntId(propertyType.getId());
				
				mapAttributeTypes.add(mapAttributeType);
			}
			
			mapObjectTypeList.add(mapObjectType);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bos);
		JAXB.marshal(sr, pw);
		
		result = bos.toByteArray();
	}

	/**
	 * Export (i.e. "create XML representation of" the given cnATreeElement
	 * and its successors. For this, child elements are exported recursively.
	 * All elements that have been processed are returned as a list of
	 * {@code syncObject}s with their respective attributes, represented
	 * as {@code syncAttribute}s.
	 * 
	 * @param cnATreeElement
	 * @return List<Element>
	 */
	private void export(List<SyncObject> list, CnATreeElement cnATreeElement, String timestamp )
	{		
		hydrate( cnATreeElement );
		
		/*++++++++++
		 * Export the given CnATreeElement, iff it is NOT blacklisted (i.e. an IT network
		 * or category element) AND, if we should restrict the exported objects to certain
		 * entity types, this element's entity type IS allowed:
		 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		
		String typeId = cnATreeElement.getTypeId();
		if ((!blacklist.contains(typeId))
				&& (exportedObjectIDs.get( cnATreeElement.getId()) == null )
				&& (entityTypesToBeExported == null || entityTypesToBeExported.get(typeId) != null) )
		{
			SyncObject syncObject = new SyncObject();
			syncObject.setExtId(cnATreeElement.getId());
			syncObject.setExtObjectType(typeId);

			List<SyncAttribute> attributes = syncObject.getSyncAttribute();
			
			/*+++++
			 * Retrieve all properties from the entity:
			 *+++++++++++++++++++++++++++++++++++++++++*/
			
			Map<String, PropertyList> properties = cnATreeElement.getEntity().getTypedPropertyLists();

			for( String s : properties.keySet() )
			{
				String propertyValue = cnATreeElement.getEntity().getSimpleValue(s);

				if( propertyValue != null )
				{
					SyncAttribute syncAttribute = new SyncAttribute();
					
					// Add <syncAttribute> to this <syncObject>:
					syncAttribute.setName(s);
					syncAttribute.setValue(propertyValue);
					attributes.add(syncAttribute);
				}			
			}

			list.add(syncObject);
			exportedObjectIDs.put( cnATreeElement.getId(), new String() );
		}
		
		/*++++
		 * Handle children recursively:
		 *+++++++++++++++++++++++++++++*/
		Set<CnATreeElement> children = cnATreeElement.getChildren();
		
		for( CnATreeElement child : children )
		{
			export(list, child, timestamp);
		}
	}
	
	/*************************************************
	 * Hydrate {@code element}, including all of its
	 * successor elements and properties.
	 * 
	 * @param element
	 *************************************************/
	private void hydrate(CnATreeElement element)
	{
		if (element == null)
			return;
		
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAOforTypedElement(element);
		//element = dao.retrieve(element.getDbId(), RetrieveInfo.getPropertyChildrenInstance());
		
		HydratorUtil.hydrateElement( dao, element, true);
		HydratorUtil.hydrateEntity( dao, element);
		
		Set<CnATreeElement> children = element.getChildren();
		for (CnATreeElement child : children)
		{
			if (child instanceof BausteinUmsetzung)
			{
				// next element:
				continue;
			}
			
			hydrate(child);
		}
	}

	public byte[] getResult() {
		return result; 
	}
	
}
