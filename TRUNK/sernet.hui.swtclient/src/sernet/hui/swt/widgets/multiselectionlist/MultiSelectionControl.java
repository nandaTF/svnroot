/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.swt.widgets.multiselectionlist;


import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.swt.widgets.Colors;
import sernet.hui.swt.widgets.IHuiControl;
import sernet.snutils.AssertException;

/**
 * @author prack
 */
public class MultiSelectionControl implements IHuiControl {
	
	
	private Entity entity;
	private PropertyType type;
	private Composite parent;
	Text text;
	private boolean editable = false;
	private Color bgColor;
	private Color fgColor;
	private boolean referencesEntities;
	private boolean crudButtons;
    private boolean cnalinkreference;
    private boolean showValidationHint;
	private boolean useValidationGUIHints;
	
	
	public Control getControl() {
		return text;
	}
	
	/**
	 * @param entity
	 * @param type
	 * @param reference 
	 * @param crudButtons 
	 * @param composite
	 */
	public MultiSelectionControl(Entity entity, PropertyType type, 
	        Composite parent, boolean edit, boolean reference, boolean crudButtons, boolean cnalinkreference, 
	        boolean showValidationHint, boolean useValidationGuiHints) {
		this.entity = entity;
		this.type = type;
		this.parent = parent;
		this.editable = edit;
		this.referencesEntities = reference;
		this.crudButtons = crudButtons;
		this.cnalinkreference = cnalinkreference;
		this.showValidationHint = showValidationHint;
		this.useValidationGUIHints = useValidationGuiHints;
	}
	
	/**
	 * 
	 */
	public void create() {
		Label label = new Label(parent, SWT.NULL);
		String labelText = type.getName();
		if(showValidationHint && useValidationGUIHints){
		    FontData fontData = label.getFont().getFontData()[0];
		    Font font = new Font(parent.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(),
		            SWT.BOLD));
		    label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		    label.setFont(font);
		}
		label.setText(type.getName());
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout contLayout = new GridLayout(2, false);
		contLayout.horizontalSpacing = 5;
		contLayout.marginLeft=0;
		contLayout.marginWidth=0;
		contLayout.marginHeight=0;
		container.setLayout(contLayout);
		
		GridData containerLData = new GridData();
		containerLData.horizontalAlignment = GridData.FILL;
		containerLData.grabExcessHorizontalSpace = true;
		container.setLayoutData(containerLData);
		
		
		text = new Text(container, SWT.BORDER);
		text.setEditable(false);
		text.setToolTipText(this.type.getTooltiptext());
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bgColor = text.getBackground();
		fgColor = text.getForeground();
		
		Button editBtn = new Button(container, SWT.PUSH);
		editBtn.setText(Messages.MultiSelectionControl_1);
		editBtn.setToolTipText(this.type.getTooltiptext());
		editBtn.setEnabled(editable);
		editBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				showSelectionDialog();
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				showSelectionDialog();
			}
		});
		
		// buttons not available for cnalink references
		if (crudButtons && ! cnalinkreference) {
			// create buttons to add / delete new properties:
			Button addBtn = new Button(container, SWT.PUSH);
			addBtn.setText(Messages.MultiSelectionControl_2);
			addBtn.setEnabled(editable);
			addBtn.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent arg0) {
					showAddDialog();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) {
					widgetSelected(arg0);
				}
			});
		}
		
		
		writeToTextField();
	}

	public void writeToTextField() {
		if (referencesEntities)
			writeEntitiesToTextField();
		else if (cnalinkreference)
		    writeLinkedObjectsToTextField();
		else
			writeOptionsToTextField();
	}

	/**
     * Get all linked objects for linktype and write their names to the text field.
     * Uses a runtime callback (reference resolver) to do this.
     */
    private void writeLinkedObjectsToTextField() {
        String referencedCnaLinkType = type.getReferencedCnaLinkType();
        String names = type.getReferenceResolver().getTitlesOfLinkedObjects(referencedCnaLinkType, entity.getUuid());
        text.setText(names);
    }

    private void writeEntitiesToTextField() {
		StringBuffer names = new StringBuffer();
		List properties = entity.getProperties(type.getId()).getProperties();
		if (properties == null)
			return;
		
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			String referencedId = prop.getPropertyValue();
			IMLPropertyOption ref = getEntity(referencedId);
			if (ref==null)
				continue;
			String optionName = ref.getName();
			if (names.length()==0)
				names.append(optionName);
			else
				names.append(" / " + optionName);
				
		}
		text.setText(names.toString());
	}

	private IMLPropertyOption getEntity(String referencedId) {
		for (IMLPropertyOption entity : type.getReferencedEntities()) {
			if (entity.getId().equals(referencedId))
				return entity;
		}
		return null;
	}

	/**
	 * Writes the names of all selected options in the text field.
	 * 
	 * @param propertyValue
	 * @param b
	 * @throws AssertException 
	 */
	private void writeOptionsToTextField() {
		StringBuffer names = new StringBuffer();
		List properties = entity.getProperties(type.getId()).getProperties();
		if (properties == null)
			return;
		
		for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			String optionId = prop.getPropertyValue();
			IMLPropertyOption opt = type.getOption(optionId);
			if (opt==null)
				continue;
			String optionName = opt.getName();
			if (names.length()==0)
				names.append(optionName);
			else
				names.append(" / " + optionName);
				
		}
		text.setText(names.toString());
		
	}
	
	void showSelectionDialog() {
		Display display = Display.getDefault();
        Shell shell = new Shell(display);
        if (cnalinkreference) {
            createLinks();
        }
        else {
            MultiSelectionDialog dialog = new MultiSelectionDialog(shell, SWT.NULL, 
                    this.entity, this.type, this.referencesEntities);
            dialog.open();
        }
	}
	
	 public void createLinks() {
         // create new link to object
         String linkType = type.getReferencedCnaLinkType();
         String referencedEntityType = type.getReferencedEntityTypeId();
         type.getReferenceResolver().createLinks(referencedEntityType, linkType, entity.getUuid());
         writeLinkedObjectsToTextField();
     }
	
	/** 
	 * This is currently only implemented for "roles" in Configuration objects as a special case.
	 */
	void showAddDialog() {
		InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), Messages.MultiSelectionControl_3, Messages.MultiSelectionControl_4, "", new IInputValidator() {
			public String isValid(String newText) {
				if (newText.length()<1)
					return Messages.MultiSelectionControl_5;
				return null;
			}
		});
		
		if (dialog.open() == Window.OK) {
			type.getReferenceResolver().addNewEntity(this.entity, dialog.getValue());
		}
		
		writeToTextField();
	}

	public void select(IMLPropertyType type, IMLPropertyOption option) throws AssertException {
		writeOptionsToTextField();
	}

	public void unselect(IMLPropertyType type, IMLPropertyOption option) throws AssertException {
		writeOptionsToTextField();
	}

	public void setFocus() {
		this.text.setFocus();
	}
	
	public boolean validate() {
	    boolean valid = true;
	    for(Entry<String, Boolean> entry : type.validate(text.getText(), null).entrySet()){
	        if(!entry.getValue().booleanValue()){
	            valid = false;
	            break;
	        }
	    }
		if (valid) {
			text.setForeground(fgColor);
			text.setBackground(bgColor);
			return true;
		}
		
		if(useValidationGUIHints){
			text.setForeground(Colors.BLACK);
			text.setBackground(Colors.YELLOW);
		}
		return false;
	
	}
	
	public void update() {
		// TODO Auto-generated method stub
	}

}
