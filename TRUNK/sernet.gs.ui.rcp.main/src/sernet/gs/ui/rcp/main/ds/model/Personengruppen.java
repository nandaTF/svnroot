package sernet.gs.ui.rcp.main.ds.model;

import org.eclipse.swt.graphics.Image;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Personengruppen extends CnATreeElement 
	implements IDatenschutzElement {
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "personengruppen";
	

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Personengruppen(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
	}
	
	 Personengruppen() {
	}
	
	@Override
	public String getTitel() {
		return getEntityType().getName();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return false;
	}
	
	

}
