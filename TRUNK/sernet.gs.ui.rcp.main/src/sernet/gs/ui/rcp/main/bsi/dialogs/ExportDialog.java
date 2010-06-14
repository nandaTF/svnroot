/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas@becker.name>.
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
 *     Andreas Becker <andreas@becker.name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

/**
 * Dialog class for the export dialog.
 * 
 * @author Andreas Becker
 */
public class ExportDialog extends TitleAreaDialog
{
	public ExportDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		// Dialog title, message and layout:
		setTitle(Messages.ExportDialog_0);
		setMessage(Messages.ExportDialog_1, IMessageProvider.INFORMATION);
		
		final Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout)composite.getLayout()).marginWidth = 10;
		((GridLayout)composite.getLayout()).marginHeight = 10;
		
		// Text field for source ID:
		final Composite compositeSourceId = new Composite(composite, SWT.NONE);
		compositeSourceId.setLayout(new RowLayout());
		final Label lblSourceId = new Label(compositeSourceId, SWT.NONE);
		lblSourceId.setText(Messages.ExportDialog_7);
		final Text txtSourceId = new Text(compositeSourceId, SWT.SINGLE | SWT.BORDER);
		
		// Radio Buttons for selection of an IT network:
		final Label lblITVerbund = new Label(composite, SWT.NONE);
		lblITVerbund.setText(Messages.ExportDialog_2);
		
		LoadCnAElementByType<ITVerbund> cmdLoadVerbuende = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
		
		try
		{
			cmdLoadVerbuende = ServiceFactory.lookupCommandService().executeCommand(cmdLoadVerbuende);
		}
		catch (CommandException ex)
		{
//			ex.printStackTrace();
			setMessage(Messages.ExportDialog_8, IMessageProvider.ERROR);
			return null;
		}
		
		List<ITVerbund> verbuende = cmdLoadVerbuende.getElements();
		Iterator<ITVerbund> verbuendeIter = verbuende.iterator();
		
		while( verbuendeIter.hasNext() )
		{
			final Button radioITVerbund = new Button(composite,SWT.RADIO);
			ITVerbund itVerbund = verbuendeIter.next();
			radioITVerbund.setText(itVerbund.getTitle()); //$NON-NLS-1$
			radioITVerbund.setData(itVerbund.getId() );
		}
		
		// Checkboxes for restriction to certain object types:
		final Button checkboxRestrict = new Button(composite,SWT.CHECK);
		checkboxRestrict.setText(Messages.ExportDialog_3);
		GridData checkboxRestrictData = new GridData();
		checkboxRestrictData.verticalIndent = 15;
		checkboxRestrict.setLayoutData(checkboxRestrictData);
		
		final Group groupObjectTypes = new Group(composite, SWT.NONE);
		GridData groupObjectTypesData = new GridData();
		groupObjectTypesData.verticalIndent = 10;
		groupObjectTypesData.horizontalIndent = 20;
		groupObjectTypes.setLayoutData(groupObjectTypesData);
		GridLayout groupObjectTypesLayout = new GridLayout();
		groupObjectTypesLayout.numColumns = 3;
		groupObjectTypes.setLayout(groupObjectTypesLayout);
		
		Collection<EntityType> entityTypes = HUITypeFactory.getInstance().getAllEntityTypes();
		Iterator<EntityType> entityTypesIter = entityTypes.iterator();
		
		while( entityTypesIter.hasNext() )
		{
			EntityType entityType = entityTypesIter.next();
			
			if(!(entityType.getId().equals("itverbund"))) //$NON-NLS-1$
			{
				final Button cbxEntityType = new Button(groupObjectTypes, SWT.CHECK);
				cbxEntityType.setData(entityType.getId());
				cbxEntityType.setText(entityType.getName());
				cbxEntityType.setEnabled(false);
			}
		}
		
		checkboxRestrict.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Control[] children = groupObjectTypes.getChildren();
				for( int i=0; i<children.length; i++ )
				{
					if( children[i] instanceof Button )
						( (Button) children[i] ).setEnabled(checkboxRestrict.getSelection() );
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		
		// Text field + button to browse for storage location:
		final Composite compositeSaveLocation = new Composite(composite,SWT.NONE);
		compositeSaveLocation.setLayout(new RowLayout(SWT.HORIZONTAL));
		((RowLayout) compositeSaveLocation.getLayout()).marginTop = 15;
		final Label labelLocation = new Label(compositeSaveLocation, SWT.NONE);
		labelLocation.setText(Messages.ExportDialog_5);
		final Text txtLocation = new Text(compositeSaveLocation, SWT.SINGLE | SWT.BORDER);
		short textLocationWidth = 300;
		txtLocation.setSize(textLocationWidth, 30);
		final RowData textLocationData = new RowData();
		textLocationData.width = textLocationWidth;
		txtLocation.setLayoutData(textLocationData);
		composite.pack();
		final Button buttonBrowseLocations = new Button(compositeSaveLocation, SWT.NONE);
		buttonBrowseLocations.setText(Messages.ExportDialog_6);
		
		buttonBrowseLocations.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[]{ "*.xml" }); //$NON-NLS-1$
				String exportPath = dialog.open();
				if( exportPath != null )
				{
					txtLocation.setText(exportPath);
				}
				else
				{
					txtLocation.setText(""); //$NON-NLS-1$
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		
		return composite;
	}
}
