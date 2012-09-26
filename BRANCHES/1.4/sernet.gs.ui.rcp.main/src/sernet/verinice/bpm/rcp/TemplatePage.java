/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm.rcp;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.Preferences;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;

/**
 * Wizard page of wizard {@link IndividualProcessWizard}.
 * User can save all task parameter as a template on this page.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TemplatePage extends WizardPage {

    private static final Logger LOG = Logger.getLogger(TemplatePage.class);

    public static final String NAME = "TEMPLATE_PAGE"; //$NON-NLS-1$

    private Preferences preferences;
    private Preferences bpmPreferences;
    private Hashtable<String, IndividualServiceParameter> templateMap;
    
    Label title, date, reminder, assignee, assigneeRelation;
    Text description, properties;
    Button button;

    protected TemplatePage() {
        super(NAME);
        setTitle(Messages.TemplatePage_1);
        setMessage(Messages.TemplatePage_2);
    }

    private void addFormElements(Composite container) {
 
        final Label titleLabel = new Label(container, SWT.NONE);
        titleLabel.setText(Messages.TemplatePage_3);
        title = new Label(container, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        title.setLayoutData(gd);
        FontData[] fD = title.getFont().getFontData();
        for (int i = 0; i < fD.length; i++) {
            fD[i].setStyle(SWT.BOLD);
        }
        Font newFont = new Font(getShell().getDisplay(), fD);
        title.setFont(newFont);

        final Label descriptionLabel = new Label(container, SWT.NONE);
        descriptionLabel.setText(Messages.TemplatePage_4);
        description = new Text(container, SWT.MULTI | SWT.LEAD | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = 100;
        description.setLayoutData(gd);
        description.setFont(newFont);

        final Label dateLabel = new Label(container, SWT.NONE);
        dateLabel.setText(Messages.TemplatePage_5);
        date = new Label(container, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        date.setLayoutData(gd);
        date.setFont(newFont);

        final Label reminderLabel = new Label(container, SWT.NONE);
        reminderLabel.setText(Messages.TemplatePage_6);
        reminder = new Label(container, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        reminder.setLayoutData(gd);
        reminder.setFont(newFont);

        final Label assigneeLabel = new Label(container, SWT.NONE);
        assigneeLabel.setText(Messages.TemplatePage_7);
        assignee = new Label(container, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        assignee.setLayoutData(gd);
        assignee.setFont(newFont);
        assignee.setText(Messages.TemplatePage_8);

        final Label relationLabel = new Label(container, SWT.NONE);
        relationLabel.setText(Messages.TemplatePage_9);
        assigneeRelation = new Label(container, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        assigneeRelation.setLayoutData(gd);
        assigneeRelation.setFont(newFont);
        assigneeRelation.setText(Messages.TemplatePage_10);

        final Label propertyLabel = new Label(container, SWT.NONE);
        propertyLabel.setText(Messages.TemplatePage_11);
        properties = new Text(container, SWT.MULTI | SWT.LEAD | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
        gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.heightHint = 50;
        properties.setLayoutData(gd);
        properties.setFont(newFont);

        button = new Button(container, SWT.PUSH);
        button.setText(Messages.TemplatePage_12);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                saveTemplate(false);
            }
        });

    }

    public void saveTemplate(boolean onlyIfNew) {
        try {
            IndividualServiceParameter parameter = ((IndividualProcessWizard) getWizard()).getParameter();
            if(getTemplateMap().get(parameter.getTitle())==null || !onlyIfNew )  {         
                getTemplateMap().put(parameter.getTitle(), parameter);
                String value = IndividualProcessWizard.toString(templateMap);
                getBpmPreferences().put(IndividualProcessWizard.PREFERENCE_NAME, value);
                getPreferences().flush();
                setMessage(Messages.TemplatePage_13 + parameter.getTitle());
            }
        } catch (Exception e) {
            LOG.error("Error while saving template", e); //$NON-NLS-1$
            setErrorMessage(Messages.TemplatePage_15);
        }
    }

    private Hashtable<String, IndividualServiceParameter> getTemplateMap()  {
        if(templateMap==null) {
            String value = getBpmPreferences().get(IndividualProcessWizard.PREFERENCE_NAME, null);
            if (value != null) {
                templateMap = (Hashtable<String, IndividualServiceParameter>) IndividualProcessWizard.fromString(value);
            } else {
                templateMap = new Hashtable<String, IndividualServiceParameter>();
            }
        }
        return templateMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            IndividualServiceParameter parameter = ((IndividualProcessWizard) getWizard()).getParameter();
            String taskTitle = parameter.getTitle();
            title.setText(taskTitle);
            if(getTemplateMap().get(taskTitle)!=null) {
                button.setText(Messages.TemplatePage_0);
            } else {
                button.setText(Messages.TemplatePage_12);
            }
            description.setText(parameter.getDescription());
            date.setText(DateFormat.getDateInstance().format(parameter.getDueDate()));
            reminder.setText(String.valueOf(parameter.getReminderPeriodDays()));
            if (parameter.getAssignee() != null) {
                assignee.setText(parameter.getAssignee());
            }
            if (parameter.getAssigneeRelationName() != null) {
                assigneeRelation.setText(parameter.getAssigneeRelationName());
            }
            if (parameter.getPropertyNames() != null) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (String prop : parameter.getPropertyNames()) {
                    if(!first) {
                        sb.append(", ");                         //$NON-NLS-1$
                    } else {
                        first = false;
                    }
                    sb.append(prop);
                }
                properties.setText(sb.toString());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 10;
        composite.setLayout(layout);
        // layout.marginHeight = 10;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        addFormElements(composite);

        // Build the separator line
        // Label separator = new Label(composite, SWT.HORIZONTAL |
        // SWT.SEPARATOR);
        // separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite.pack();

        // Required to avoid an error in the system
        setControl(composite);
        setPageComplete(true);
    }

    private String trimTitleByWidthSize(GC gc, String elementTitle, int width) {
        String newTitle = elementTitle.substring(0, elementTitle.length() - 1);
        Point size = gc.textExtent(newTitle + "..."); //$NON-NLS-1$
        if (size.x > width) {
            newTitle = trimTitleByWidthSize(gc, newTitle, width);
        }
        return newTitle;
    }

    public Preferences getPreferences() {
        if(preferences==null) {
            preferences = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        }
        return preferences;
    }

    public Preferences getBpmPreferences() {
        if(bpmPreferences==null) {
            bpmPreferences = getPreferences().node(IndividualProcessWizard.PREFERENCE_NODE_NAME);
        }
        return bpmPreferences;
    }

}
