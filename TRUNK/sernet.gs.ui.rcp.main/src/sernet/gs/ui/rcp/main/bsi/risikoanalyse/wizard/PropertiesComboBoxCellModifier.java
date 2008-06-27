package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.beans.PropertyEditor;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdung;

public class PropertiesComboBoxCellModifier implements ICellModifier {

	public boolean canModify(Object element, String property) {
		 if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property))
			 return true;
		 return false;
	}
	
	public Object getValue(Object element, String property) {
	    GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) element;
	    
	    if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property))
	      return gef.getAlternativeIndex();
	    
	    return null;
	  }

	public void modify(Object element, String property, Object value) {
		Object elmt = ((TableItem)element).getData();
		
		if (elmt instanceof GefaehrdungsUmsetzung) {
			GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) elmt;
			if (RiskHandlingPage.CHOICE_COLUMN_ID.equals(property)) {
				gef.setAlternative((String) value);
			}
		}
	}
}
