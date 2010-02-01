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
 ******************************************************************************/
package sernet.hui.common.connect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class EntityType {
	private String id;
	private String name;
	
	private List<IEntityElement> elements = new ArrayList<IEntityElement>();
	private Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();
	private List<PropertyGroup> propertyGroups = new ArrayList<PropertyGroup>();
	
	// map of target EntityType ID : set of relation descriptions 
	private Map<String, Set<HuiRelation>> relations = new HashMap<String, Set<HuiRelation>>();
	
	public void addPropertyType(PropertyType prop) {
		propertyTypes.put(prop.getId(), prop);
		elements.add(prop);
	}
	
	public void addPropertyGroup(PropertyGroup group) {
		propertyGroups.add(group);
		elements.add(group);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PropertyType> getPropertyTypes() {
		ArrayList<PropertyType> types = new ArrayList<PropertyType>(propertyTypes.values().size());
		types.addAll(propertyTypes.values());
		return types;
	}

	public List<IEntityElement> getElements() {
		return elements;
	}

	public List<PropertyGroup> getPropertyGroups() {
		return propertyGroups;
	}

	public PropertyType getPropertyType(String id) {
		PropertyType type = this.propertyTypes.get(id);
		if (type != null)
			return type;
		
		// search in groups:
		for (PropertyGroup group : this.propertyGroups) {
			if ((type = group.getPropertyType(id)) != null)
				return type;
		}
		// none found:
		return null;
	}

	/**
	 * Add definition for a relation from the XML file to this EntityType.
	 * 
	 * 
	 * @param relation
	 */
	public void addRelation(HuiRelation relation) { 
		if (relations.get(relation.getTo()) == null) {
			this.relations.put(relation.getTo(), new HashSet<HuiRelation>());
		}
		this.relations.get(relation.getTo()).add(relation);
	}
	
	/**
	 * Returns all possible relation from this EntityType to the specified EntityType. I.e. from "product" to "person" the following 
	 * relations could be defined: "bought by", "sold by", "manufactured by" etc.
	 * 
	 * Links (i.e. CnALinks) should only be created for allowed relations.
	 * 
	 * @param toEntityType
	 * @return
	 */
	public Set<HuiRelation> getPossibleRelations(String toEntityType) {
		return relations.get(toEntityType) != null 
			? relations.get(toEntityType)
			: new HashSet<HuiRelation>(0);
	}

	/**
	 * Returns all possible (meaning "defined in SNCA.xml") relations as a flat list.
	 * @return all possible relations from this element to other elements.
	 */
	public Set<HuiRelation> getPossibleRelations() {
		HashSet<HuiRelation> allRelations = new HashSet<HuiRelation>();
		Set<Entry<String, Set<HuiRelation>>> entrySet = relations.entrySet();
		for (Entry<String, Set<HuiRelation>> entry : entrySet) {
			Set<HuiRelation> relationsToOneOtherType = entry.getValue();
			allRelations.addAll(relationsToOneOtherType);
		}
		return allRelations;
	}
	
	/**
	 * @param typeId
	 * @return
	 */
	public HuiRelation getPossibleRelation(String typeId) {
		Set<Entry<String, Set<HuiRelation>>> entrySet = relations.entrySet();
		for (Entry<String, Set<HuiRelation>> entry : entrySet) {
			Set<HuiRelation> value = entry.getValue();
			for (HuiRelation huiRelation : value) {
				if (huiRelation.getId().equals(typeId))
					return huiRelation;
			}
		}
		return null;
	}
	
	
}
