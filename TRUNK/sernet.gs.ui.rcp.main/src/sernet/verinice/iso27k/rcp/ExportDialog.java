/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.LayoutStyle;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.action.ExportAction;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ExportDialog extends TitleAreaDialog {
    private static final Logger LOG = Logger.getLogger(ExportDialog.class);

    /**
     * Indicates if the output should be encrypted.
     */
    private boolean encryptOutput = false;
    private CnATreeElement selectedElement;
    private String filePath;
    private String sourceId;
    
    private Text txtLocation;
    
    public ExportDialog(Shell activeShell) {
        this(activeShell, null);
    }

    /**
     * @param activeShell
     * @param selectedOrganization
     */
    public ExportDialog(Shell activeShell, Organization selectedOrganization) {
        super(activeShell);
        selectedElement = selectedOrganization;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        /*
         * ++++ Dialog title, message and layout:
         * ++++++++++++++++++++++++++++++++++
         */

        setTitle(Messages.SamtExportDialog_0);
        setMessage(Messages.SamtExportDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        composite.setLayoutData(gd);
        
        /*
         * ++++ Widgets for selection of an IT network:
         * ++++++++++++++++++++++++++++++++++++++++
         */

        LoadCnAElementByType<Organization> cmdLoadOrganization = new LoadCnAElementByType<Organization>(Organization.class);
        try {
            cmdLoadOrganization = ServiceFactory.lookupCommandService().executeCommand(cmdLoadOrganization);
        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }
        
        LoadCnAElementByType<ITVerbund> cmdItVerbund = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
        try {
            cmdItVerbund = ServiceFactory.lookupCommandService().executeCommand(cmdItVerbund);
        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }

        final Group groupOrganization = new Group(composite, SWT.NONE);
        groupOrganization.setText(Messages.SamtExportDialog_2);
        GridLayout groupOrganizationLayout = new GridLayout(1, true);
        groupOrganization.setLayout(groupOrganizationLayout);
        gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.minimumWidth = 662;
        groupOrganization.setLayoutData(gd);

        

        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedElement = (CnATreeElement) ((Button) e.getSource()).getData();
                if(txtLocation!=null) {
                    filePath = selectedElement.getTitle() + ".xml";
                    txtLocation.setText(filePath);
                }
                super.widgetSelected(e);
            }
        };

        CnATreeElement oldSelectedElement = selectedElement;
        selectedElement = null;
        List<Organization> organizationList = cmdLoadOrganization.getElements();
        Iterator<Organization> organizationIter = organizationList.iterator();
        while (organizationIter.hasNext()) {
            final Button radioOrganization = new Button(groupOrganization, SWT.RADIO);
            Organization organization = organizationIter.next();
            radioOrganization.setText(organization.getTitle());
            radioOrganization.setData(organization);
            radioOrganization.addSelectionListener(organizationListener);
            if (oldSelectedElement != null && oldSelectedElement.equals(organization)) {
                radioOrganization.setSelection(true);
                selectedElement = organization;              
            }
            if (organizationList.size() == 1) {
                radioOrganization.setSelection(true);
                selectedElement = organization;
            }
        }
        
        List<ITVerbund> itVerbundList = cmdItVerbund.getElements();
        Iterator<ITVerbund> itVerbundIter = itVerbundList.iterator();
        while (itVerbundIter.hasNext()) {
            final Button radio = new Button(groupOrganization, SWT.RADIO);
            ITVerbund verbund = itVerbundIter.next();
            radio.setText(verbund.getTitle());
            radio.setData(verbund);
            radio.addSelectionListener(organizationListener);
            if (oldSelectedElement != null && oldSelectedElement.equals(verbund)) {
                radio.setSelection(true);
                selectedElement = verbund;              
            }
            if (organizationList.size() == 1 && selectedElement==null) {
                radio.setSelection(true);
                selectedElement = verbund;
            }
        }
        
        /*
         * ++++ Widgets for source-id
         * ++++++++++++++++++++++++++++++++++++++
         */
        
        final Composite sourceIdComposite = new Composite(composite, SWT.NONE);
        sourceIdComposite.setLayout(new GridLayout(3,false));
        ((GridLayout) sourceIdComposite.getLayout()).marginTop = 15;
        gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace=true;
        sourceIdComposite.setLayoutData(gd);
        
        final Label sourceIdLabel = new Label(sourceIdComposite, SWT.NONE);
        sourceIdLabel.setText(Messages.SamtExportDialog_14);
        final Text sourceIdText = new Text(sourceIdComposite, SWT.BORDER);
        gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.minimumWidth = 150;
        sourceIdText.setLayoutData(gd);
        sourceIdText.addModifyListener(new ModifyListener() {         
            @Override
            public void modifyText(ModifyEvent e) {
                sourceId = sourceIdText.getText();          
            }
        });      

        /*
         * +++++ Widgets to browse for storage location:
         * ++++++++++++++++++++++++++++++++++++++++
         */

        final Label labelLocation = new Label(sourceIdComposite, SWT.NONE);
        labelLocation.setText(Messages.SamtExportDialog_6);
        txtLocation = new Text(sourceIdComposite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace=true;
        gd.minimumWidth = 302;
        txtLocation.setLayoutData(gd);
        txtLocation.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                filePath = txtLocation.getText();

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // nothing to do
            }
        });
        
        final Button buttonBrowseLocations = new Button(sourceIdComposite, SWT.NONE);
        buttonBrowseLocations.setText(Messages.SamtExportDialog_7);
        buttonBrowseLocations.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setText(Messages.SamtExportDialog_3);
                if(txtLocation!=null && txtLocation.getText()!=null && !txtLocation.getText().isEmpty()) {                 
                    try {
                        dialog.setFileName(getFileNameFromPath(txtLocation.getText()));
                    } catch (Exception e1) {
                        LOG.warn("Can not set file name", e1);
                        dialog.setFileName("");
                    }
                }             
                dialog.setFilterExtensions(new String[] { 
                        "*"+ExportAction.EXTENSION_XML, //$NON-NLS-1$
                        "*"+ExportAction.EXTENSION_PASSWORD_ENCRPTION, //$NON-NLS-1$
                        "*"+ExportAction.EXTENSION_CERTIFICATE_ENCRPTION }); //$NON-NLS-1$
                // FIXME: externalize strings 
                dialog.setFilterNames(new String[] { 
                        Messages.SamtExportDialog_15,
                        Messages.SamtExportDialog_16,
                        Messages.SamtExportDialog_17 });
                String exportPath = dialog.open();
                if (exportPath != null) {
                    txtLocation.setText(ExportAction.addExtension(exportPath,ExportAction.EXTENSION_XML));
                    filePath = exportPath;
                } else {
                    txtLocation.setText(""); //$NON-NLS-1$
                    filePath = ""; //$NON-NLS-1$
                }
            }

            

        });
        
        
        /*
         * ++++ Widgets to enable/disable encryption:
         * ++++++++++++++++++++++++++++++++++++++
         */

        final Button encryptionCheckbox = new Button(sourceIdComposite, SWT.CHECK);
        encryptionCheckbox.setText(Messages.SamtExportDialog_5);
        gd = new GridData();
        gd.horizontalSpan = 3;
        encryptionCheckbox.setLayoutData(gd);
        encryptionCheckbox.setSelection(encryptOutput);
        encryptionCheckbox.setEnabled(true);
        encryptionCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkBox = (Button) e.getSource();
                encryptOutput = checkBox.getSelection();
            }
        });
        
        if(selectedElement!=null) {
            filePath = selectedElement.getTitle() + ".xml";
            txtLocation.setText(filePath);
        }
        
        sourceIdComposite.pack();     
        composite.pack();     
        return composite;
    }
    
    private String getFileNameFromPath(String path) {
        if(path!=null && path.indexOf(File.separatorChar)!=-1) {
            path = path.substring(path.lastIndexOf(File.separatorChar)+1);
        }
        return path;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        StringBuilder sb = new StringBuilder();
        if (filePath == null || filePath.isEmpty()) {
            sb.append(Messages.SamtExportDialog_10);
        } else {
            try {
                new File(filePath).createNewFile();
            } catch (Exception e) {
                sb.append(Messages.SamtExportDialog_11);
            }
        }
        if (selectedElement == null) {
            sb.append(Messages.SamtExportDialog_12);
        }
        if (sb.length() > 0) {
            sb.append(Messages.SamtExportDialog_13);
            setMessage(sb.toString(), IMessageProvider.ERROR);
        } else {
            super.okPressed();
        }
    }

    /* Getters and Setters: */

    public CnATreeElement getSelectedElement() {
        return selectedElement;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean getEncryptOutput() {
        return encryptOutput;
    }

    public String getSourceId() {
        return sourceId;
    }

}
