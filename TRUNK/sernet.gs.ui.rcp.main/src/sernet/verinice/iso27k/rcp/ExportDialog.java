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
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeSelection;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.FileUtil;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.action.ExportAction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.MultiselectWidget;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class ExportDialog extends TitleAreaDialog {
    private static final Logger LOG = Logger.getLogger(ExportDialog.class);

    private static final String[] EXTENSION_ARRAY = new String[] {VeriniceArchive.EXTENSION_VERINICE_ARCHIVE,ExportAction.EXTENSION_XML};
    
    private static final String DEFAULT_ORGANIZATION_TITLE = "organization";
    
    /**
     * Indicates if the output should be encrypted.
     */
    private boolean encryptOutput = false;
    private boolean reImport = false;
    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private String filePath;
    private String sourceId;
    
    private ElementMultiselectWidget organizationWidget = null;
    
    private Text sourceIdText;
    private Text txtLocation;
    private String defaultFolder;
    private Button useDefaultFolderButton;
    private boolean useDefaultFolder = true;
    private String organizationTitle = DEFAULT_ORGANIZATION_TITLE;
    
    // ExportCommand.EXPORT_FORMAT_VERINICE_ARCHIV or ExportCommand.EXPORT_FORMAT_XML_PURE 
    private int format = SyncParameter.EXPORT_FORMAT_DEFAULT;
    
    private boolean serverConnectionMode = false;
    
    public ExportDialog(Shell activeShell) {
        this(activeShell, (CnATreeElement)null);
    }

    public ExportDialog(Shell activeShell, boolean serverConnectionMode, String filePath) {
        super(activeShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.serverConnectionMode = serverConnectionMode;
        this.filePath = filePath;
    }
    
    /**
     * @param activeShell
     * @param selectedOrganization
     */
    public ExportDialog(Shell activeShell, CnATreeElement selectedOrganization) {
        super(activeShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        selectedElement = selectedOrganization;
    }
    
    public ExportDialog(Shell activeShell, ITreeSelection selection){
        this(activeShell, (CnATreeElement)null);
        this.selection = selection;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;
        final int sourceIdCompositeNumColumns = 3;
        final int sourceIdCompositeMarginTop = 15;
        final int reimportChechboxHorizontalSpan = sourceIdCompositeNumColumns;
        final int sourceIdTextMinimumWidth = 150;
        final int txtLocationMinimumWidth = 302;
        final int udfbHorizontalSpan = reimportChechboxHorizontalSpan;
        final int encryptionCheckboxHorizontalSpan = udfbHorizontalSpan;
        
        initDefaultFolder();
        
        /*
         * Dialog title, message and layout:
         */

        setTitle(Messages.SamtExportDialog_0);
        setMessage(Messages.SamtExportDialog_1, IMessageProvider.INFORMATION);

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        composite.setLayoutData(gd);
        
        try {
            organizationWidget = new ElementMultiselectWidget(composite, selection, selectedElement);
            setOrgTitle();
            
        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }
        
        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                if(checkbox.getSelection()) {
                    setOrgTitle();
                    if(txtLocation!=null) {
                        if(isFilepath()) {
                            filePath = FileUtil.getFolderFromPath(txtLocation.getText()) + organizationTitle + getDefaultExtension();
                        } else {                  
                            filePath = defaultFolder + organizationTitle + getDefaultExtension();
                        }
                        txtLocation.setText(filePath);
                    }
                    setSourceId(organizationWidget.getSelectedElement());
                } 
                super.widgetSelected(e);
            }
        };
        
        organizationWidget.addSelectionLiustener(organizationListener);
        
        
        if(!serverConnectionMode) {    
            final Composite sourceIdComposite = new Composite(composite, SWT.NONE);
            sourceIdComposite.setLayout(new GridLayout(sourceIdCompositeNumColumns,false));
            ((GridLayout) sourceIdComposite.getLayout()).marginTop = sourceIdCompositeMarginTop;
            gd = new GridData(SWT.FILL, SWT.BOTTOM, true,false);
            gd.grabExcessHorizontalSpace=true;
            sourceIdComposite.setLayoutData(gd);
            
            /*
             * Widgets for re-import
             */
            
            final Button reImportCheckbox = new Button(sourceIdComposite, SWT.CHECK);
            reImportCheckbox.setText(Messages.ExportDialog_0);
            gd = new GridData();
            gd.horizontalSpan = reimportChechboxHorizontalSpan;
            reImportCheckbox.setLayoutData(gd);
            reImportCheckbox.setSelection(reImport);
            reImportCheckbox.setEnabled(true);
            reImportCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button checkBox = (Button) e.getSource();
                    reImport = checkBox.getSelection();
                }
            });

            /*
             *  Widgets to enable/disable encryption:
             */
    
            final Button encryptionCheckbox = new Button(sourceIdComposite, SWT.CHECK);
            encryptionCheckbox.setText(Messages.SamtExportDialog_5);
            gd = new GridData();
            gd.horizontalSpan = encryptionCheckboxHorizontalSpan;
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
            sourceIdComposite.pack(); 
            
            /*
             * Widgets for source-id
             */
            
            final Label sourceIdLabel = new Label(sourceIdComposite, SWT.NONE);
            sourceIdLabel.setText(Messages.SamtExportDialog_14);
            sourceIdText = new Text(sourceIdComposite, SWT.BORDER);
            gd = new GridData(GridData.GRAB_HORIZONTAL);
            gd.horizontalSpan = 2;
            gd.minimumWidth = sourceIdTextMinimumWidth;
            sourceIdText.setLayoutData(gd);
            sourceIdText.addModifyListener(new ModifyListener() {         
                @Override
                public void modifyText(ModifyEvent e) {
                    sourceId = sourceIdText.getText();          
                }
            });      
            
            setSourceId(organizationWidget.getSelectedElement());
    
            /*
             * Widgets to browse for storage location:
             */
    
            final Label labelLocation = new Label(sourceIdComposite, SWT.NONE);
            labelLocation.setText(Messages.SamtExportDialog_6);
            txtLocation = new Text(sourceIdComposite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            gd = new GridData(SWT.FILL, SWT.TOP, true,false);
            gd.grabExcessHorizontalSpace=true;
            gd.minimumWidth = txtLocationMinimumWidth;
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
                    if(isFilepath()) {                 
                        try {   
                            dialog.setFilterPath(FileUtil.getFolderFromPath(txtLocation.getText()));
                            dialog.setFileName(FilenameUtils.getName(txtLocation.getText()));                      
                        } catch (Exception e1) {
                            LOG.debug(Messages.ExportDialog_1, e1);
                            dialog.setFileName(""); //$NON-NLS-1$
                        }
                    } else {
                        //set default folder for exports which could set 
                        if(useDefaultFolder){
                            dialog.setFilterPath(defaultFolder);
                        }
                    }
                    dialog.setFilterExtensions(new String[] {
                            "*"+EXTENSION_ARRAY[0], //$NON-NLS-1$
                            "*"+EXTENSION_ARRAY[1] }); //$NON-NLS-1$          
                    dialog.setFilterNames(new String[] {
                            Messages.ExportDialog_2,
                            Messages.SamtExportDialog_15 });
                    // set the default extension to EXTENSION_VERINICE_ARCHIVE
                    dialog.setFilterIndex(0);
                    String exportPath = dialog.open();
                    // set export-format to filter index of dialog
                    // filter index must match ExportCommand.EXPORT_FORMAT_VERINICE_ARCHIV 
                    // or ExportCommand.EXPORT_FORMAT_XML_PURE
                    setFormat(dialog.getFilterIndex());
                    if (exportPath != null) {
                        txtLocation.setText(ExportAction.addExtension(exportPath,EXTENSION_ARRAY[dialog.getFilterIndex()]));
                        filePath = exportPath;
                    } else {
                        txtLocation.setText(""); //$NON-NLS-1$
                        filePath = ""; //$NON-NLS-1$
                    }
                }

                });
                
                useDefaultFolderButton = new Button(sourceIdComposite, SWT.CHECK);
                useDefaultFolderButton.setText(Messages.ExportDialog_3);
                useDefaultFolderButton.setSelection(useDefaultFolder);
                useDefaultFolderButton.setEnabled(true);
                GridData  useDefaultFolderButtonGridData = new GridData();
                useDefaultFolderButtonGridData.horizontalSpan = udfbHorizontalSpan;
                useDefaultFolderButton.setLayoutData(useDefaultFolderButtonGridData);
                useDefaultFolderButton.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Button checkBox = (Button) e.getSource();
                        useDefaultFolder = checkBox.getSelection();
                    }
                });

        }
            
        if(organizationWidget.getSelectedElement()!=null) {
            filePath = defaultFolder + organizationTitle + getDefaultExtension();
            txtLocation.setText(filePath);
        }
               
        composite.pack();     
        return composite;
    }

    private void setOrgTitle() {
        String title = null;
        if(organizationWidget.getSelectedElement()!=null) {
            title = organizationWidget.getSelectedElement().getTitle();
        }
        if(title!=null) {
            organizationTitle = convertToFileName(title);
            //organizationTitle = title.replaceAll("[^a-zA-Z]", ""); //hier ist es das  Umlaute-Problem, die werden ersetzt und nicht ordentlich ausgeschrieben!!!
        } else {
            organizationTitle = DEFAULT_ORGANIZATION_TITLE;
        }
    }
    
    private boolean isFilepath() {
        return txtLocation!=null && txtLocation.getText()!=null && !txtLocation.getText().isEmpty();
    }
    
    private String initDefaultFolder() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_EXPORT);      
        if(defaultFolder==null || defaultFolder.isEmpty()) {
            defaultFolder = System.getProperty("user.home");
        }       
        if (!defaultFolder.endsWith(System.getProperty("file.separator"))) {
            defaultFolder = defaultFolder + System.getProperty("file.separator");
        }
        return defaultFolder;
    }
    
    protected String setupDirPath(){
       String currentPath = txtLocation.getText();
        String path = currentPath;
        if(currentPath!=null && !currentPath.isEmpty()) {
             int lastSlash = currentPath.lastIndexOf(System.getProperty("file.separator"));
            if(lastSlash!=-1) {
                path = currentPath.substring(0,lastSlash+1);
            }else{
                path = currentPath.substring(0,lastSlash);
            }
        }
        if(!currentPath.equals(path)) {
            txtLocation.setText(path);
        }
        return path;        
    }
    
    /**
     * @return
     */
    protected String getDefaultExtension() {
        return VeriniceArchive.EXTENSION_VERINICE_ARCHIVE;
    }

    private void setSourceId(CnATreeElement element) {
		if(element!=null && element.getSourceId()!=null) {
			this.sourceId = element.getSourceId();
			if(sourceIdText!=null) {
				sourceIdText.setText(element.getSourceId());
			}
		} else {
			this.sourceId = null;
			if(sourceIdText!=null) {
				sourceIdText.setText(""); //$NON-NLS-1$
			}
		}
	}

	
	
	private static String convertToFileName(String label) {
        String filename = ""; //$NON-NLS-1$
        if(label!=null) {
            filename = label.replace(' ', '_');
            filename = filename.replace("ä", "\u00E4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ü", "\u00FC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ö", "\u00F6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ä", "\u00C4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ü", "\u00DC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ö", "\u00D6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ß", "\u00DF"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(";", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("<", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(">", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("|", ""); //$NON-NLS-1$ //$NON-NLS-2$
           }
        return filename;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        StringBuilder sb = new StringBuilder();
        if (filePath == null || filePath.isEmpty()) {
            sb.append(Messages.SamtExportDialog_10);
        } else {
            try {
                File test = new File(filePath);
                if(test.createNewFile()) {
                    test.delete();
                }
            } catch (Exception e) {
                sb.append(Messages.SamtExportDialog_11);
            }
        }
        if (organizationWidget.getSelectedElement() == null) {
            sb.append(Messages.SamtExportDialog_12);
        }
        if (sb.length() > 0) {
            sb.append(Messages.SamtExportDialog_13);
            setMessage(sb.toString(), IMessageProvider.ERROR);
        } else {
            String currentPath = setupDirPath();
            if(useDefaultFolder){
                Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DEFAULT_FOLDER_EXPORT, currentPath);
            }
            super.okPressed();
        }
    }

	public String getFilePath() {
        return filePath;
    }

    public boolean getEncryptOutput() {
        return encryptOutput;
    }

    public boolean getReImport() {
        return reImport;
    }
    public String getSourceId() {
        return sourceId;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int exportFormat) {
        this.format = exportFormat;
    }

    /**
     * @return
     */
    public Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }

    /**
     * @return
     */
    public CnATreeElement getSelectedElement() {
        return organizationWidget.getSelectedElement();
    }
    public boolean getUseDefaultFolder(){
        return useDefaultFolder;
    }

    public static String[] getExtensionArray() {
        return EXTENSION_ARRAY;
    }

}
