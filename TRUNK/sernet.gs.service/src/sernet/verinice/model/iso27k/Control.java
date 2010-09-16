/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.iso27k;

import java.util.Collection;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.bsi.TagHelper;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class Control extends CnATreeElement implements IISO27kElement, IControl {

	public static final String TYPE_ID = "control"; //$NON-NLS-1$
	public static final String PROP_ABBR = "control_abbr"; //$NON-NLS-1$
	public static final String PROP_NAME = "control_name"; //$NON-NLS-1$
	public static final String PROP_TAG = "control_tag"; //$NON-NLS-1$
	public static final String PROP_DESC = "control_desc"; //$NON-NLS-1$
	
	// control implementation state:
	// see IControl.java
	
	// this is another way to measure control implementation:
	public static final String PROP_MATURITY = "control_maturity"; //$NON-NLS-1$
	public static final String PROP_WEIGHT1 = "control_weight"; //$NON-NLS-1$
	public static final String PROP_WEIGHT2 = "control_ownweight"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD1 = "control_min1"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD2 = "control_min2"; //$NON-NLS-1$
	
	public static final String PROP_EFFECTIVENESS_CONFIDENTIALITY ="control_effectiveness_confidentiality"; 
    public static final String PROP_EFFECTIVENESS_INTEGRITY="control_effectiveness_integrity" ;
    public static final String PROP_EFFECTIVENESS_AVAILABILITY="control_effectiveness_availability"; 
    public static final String PROP_EFFECTIVENESS_PROBABILITY="control_eff_probability"; 
	
   
	/**
	 * Creates an empty asset
	 */
	public Control() {
		super();
		setEntity(new Entity(TYPE_ID));
	}
	
	public Control(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
        // sets the localized title via HUITypeFactory from message bundle
        setTitel(getTypeFactory().getMessage(TYPE_ID));
    }
	
   
	
   
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.common.model.CnATreeElement#getTitel()
	 */
	@Override
	public String getTitle() {
		return getEntity().getSimpleValue(PROP_NAME);
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
	}
	
	public String getAbbreviation() {
		return getEntity().getSimpleValue(PROP_ABBR);
	}
	
	public void setAbbreviation(String abbreviation) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ABBR), abbreviation);
	}
	
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	public String getDescription() {
		return getEntity().getSimpleValue(PROP_DESC);
	}
	
	public void setDescription(String description) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_DESC), description);
	}

	
	public void setMaturity(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_MATURITY), value);
	}
	
	public int getMaturity() {
	    return getEntity().getInt(PROP_MATURITY);
	}
	
	public String getImplementation() {
        PropertyList properties = getEntity().getProperties(PROP_IMPL);
        if (properties == null || properties.getProperties() == null
                || properties.getProperties().size() < 1)
            return IMPLEMENTED_NOTEDITED;

        Property property = properties.getProperty(0);
        if (property != null && !property.getPropertyValue().equals("")) //$NON-NLS-1$
            return property.getPropertyValue();
        return IMPLEMENTED_NOTEDITED;
    }
	
	public boolean isImplemented() {
	    return getImplementation().equals(IMPLEMENTED_YES);
	}

	/**
	 * Returns the used weight.
	 * @return
	 */
	public int getWeight2() {
	    return getEntity().getInt(PROP_WEIGHT2);
	}
	
	public int getThreshold1() {
	    return getEntity().getInt(PROP_THRESHOLD1);
    }

	public int getThreshold2() {
	    return getEntity().getInt(PROP_THRESHOLD2);
	}

	
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight1() {
        return getEntity().getInt(PROP_WEIGHT1);
    }
	
	/**
	 * Sets the suggested weight for maturity calculation.
	 * @param value
	 */
	public void setWeight1(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_WEIGHT1), value);
		
	}
	
	/**
	 * Sets the actually used weight for maturity calculation.
	 * @param value
	 */
	public void setWeight2(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_WEIGHT2), value);
		
	}

	public void setThreshold1(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_THRESHOLD1), value);
		
	}

	public void setThreshold2(String value) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_THRESHOLD2), value);
		
	}

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IControl#getMaturityPropertyId()
     */
    @Override
    public String getMaturityPropertyId() {
        return PROP_MATURITY;
    }

}
