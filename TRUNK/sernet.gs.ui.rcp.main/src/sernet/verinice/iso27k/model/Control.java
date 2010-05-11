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
package sernet.verinice.iso27k.model;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.TagHelper;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.samt.model.SamtTopic;

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
	
	public static final String PROP_IMPLEMENTED = "control_implemented";
	public static final String PROP_IMPLEMENTED_NO = "control_implemented_no";
	public static final String PROP_IMPLEMENTED_YES = "control_implemented_yes";
	public static final String PROP_IMPLEMENTED_PARTLY = "control_implemented_partly";
	public static final String PROP_IMPLEMENTED_NA = "control_implemented_na";

	// this is another way to meassure control implementation:
	public static final String PROP_MATURITY = "control_maturity"; //$NON-NLS-1$
	public static final String PROP_WEIGHT1 = "control_weight"; //$NON-NLS-1$
	public static final String PROP_WEIGHT2 = "control_ownweight"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD1 = "control_min1"; //$NON-NLS-1$
	public static final String PROP_THRESHOLD2 = "control_min2"; //$NON-NLS-1$
	
	/**
     * Maps the implement status of this IControl implementation to a generic
     * {@link IControl} implement status.
     * 
     * @see {@link IControl.getImplementStatus()}
     */
    public static final Map<String, String> IMPLEMENT_STATUS_MAP;
    
    static {
        IMPLEMENT_STATUS_MAP = new Hashtable<String, String>();
        IMPLEMENT_STATUS_MAP.put(PROP_IMPLEMENTED_NOTEDITED, IControl.IMPLEMENTED_NOT_EDITED);
        IMPLEMENT_STATUS_MAP.put(PROP_IMPLEMENTED_NA, IControl.IMPLEMENTED_NA);
        IMPLEMENT_STATUS_MAP.put(PROP_IMPLEMENTED_YES, IControl.IMPLEMENTED_YES);
        IMPLEMENT_STATUS_MAP.put(PROP_IMPLEMENTED_PARTLY, IControl.IMPLEMENTED_PARTLY);
        IMPLEMENT_STATUS_MAP.put(PROP_IMPLEMENTED_NO, IControl.IMPLEMENTED_NO);
    }

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
	
    public String getImplemented() {
        String result = IControl.PROP_IMPLEMENTED_NOTEDITED;
        PropertyList properties = getEntity().getProperties(PROP_IMPLEMENTED);
        if (properties != null && properties.getProperties() != null && !properties.getProperties().isEmpty()) {       
            Property property = properties.getProperty(0);
            if (property != null && !property.getPropertyValue().isEmpty()) { //$NON-NLS-1$
                result = property.getPropertyValue();
            }
        }
        return result;
    }
	
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.model.IControl#getImplementStatus()
     */
    @Override
    public String getImplementStatus() {
        return IMPLEMENT_STATUS_MAP.get(getImplemented());
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
	    String simpleValue = getEntity().getSimpleValue(PROP_MATURITY);
	    return Integer.parseInt(simpleValue);
	}

	/**
	 * Returns the used weight.
	 * @return
	 */
	public int getWeight2() {
	    return getEntity().getInt(SamtTopic.PROP_WEIGHT2);
	}
	
	public int getThreshold1() {
	    return getEntity().getInt(SamtTopic.PROP_THRESHOLD1);
    }

	public int getThreshold2() {
	    return getEntity().getInt(SamtTopic.PROP_THRESHOLD2);
	}

	
    /**
     * Returns the used weight.
     * @return
     */
    public int getWeight1() {
        return getEntity().getInt(SamtTopic.PROP_WEIGHT1);
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
