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
package sernet.verinice.bpm.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.HtmlWriter;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.bpm.TaskLoader;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskListener;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.iso27k.rcp.Iso27kPerspective;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.service.commands.LoadAncestors;

/**
 * RCP view to display task loaded by instances of {@link ITaskService}.
 * 
 * New tasks are loaded by a {@link ITaskListener} registered at
 * {@link TaskLoader}.
 * 
 * Double clicking a task opens {@link CnATreeElement} in an editor. View
 * toolbar provides a button to complete tasks.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskView extends ViewPart implements IAttachedToPerspective {

    private static final Logger LOG = Logger.getLogger(TaskView.class);

    public static final String ID = "sernet.verinice.bpm.rcp.TaskView";

    private static final String[] ALLOWED_ROLES = new String[] { ApplicationRoles.ROLE_ADMIN };

    private TreeViewer treeViewer;
    
    private Browser textPanel;

    private TaskLabelProvider labelProvider;

    private TaskContentProvider contentProvider;

    private Action refreshAction;

    private Action doubleClickAction;

    private Action myTasksAction;

    private Action cancelTaskAction;

    private ICommandService commandService;

    private boolean onlyMyTasks = true;

    private Composite parent = null;
    
    private RightsServiceClient rightsService;
    
    private IModelLoadListener modelLoadListener;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
     * .Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;
        Composite container = createContainer(parent);
        createInfoPanel(container);
        createTreeViewer(container);
        initData();
        makeActions();
        addActions();
        addListener();
    }

    public String getRightID() {
        return ActionRightIDs.TASKVIEW;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // empty
    }
    
    private void initData() {
        if(CnAElementFactory.isModelLoaded()) {
            loadTasks();
        } else if(modelLoadListener==null) {
            // model is not loaded yet: add a listener to load data when it's laoded
            modelLoadListener = new IModelLoadListener() {
                @Override
                public void closed(BSIModel model) {
                    // nothing to do
                }
                public void loaded(BSIModel model) {
                    initData();
                }
                @Override
                public void loaded(ISO27KModel model) {
                    // work is done in loaded(BSIModel model)                        
                }               
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }

    private void loadTasks() {
        TaskParameter param = new TaskParameter();
        param.setAllUser(!onlyMyTasks);
        List<ITask> taskList = Collections.emptyList();
        final LoadTaskJob job = new LoadTaskJob(param);
        final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                try {
                    progressService.run(true, true, job);
                } catch (Throwable t) {
                    LOG.error("Error while loading tasks.", t);
                    showError("Error", "Error while loading task.");
                }
            }
        });
        
        taskList = job.getTaskList();
        
        final List<ITask> finalTaskList = taskList;
        
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    getViewer().setInput(finalTaskList);
                }
            });
            
        } catch (Throwable t) {
            LOG.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }
    
    private Composite createContainer(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layoutRoot = new GridLayout(1, false);
        layoutRoot.marginWidth = 2;
        layoutRoot.marginHeight = 2;
        composite.setLayout(layoutRoot);
        GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        composite.setLayoutData(gd);
        return composite;
    }
    
    private void createInfoPanel(Composite container) {
        textPanel = new Browser(container, SWT.NONE);
        textPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL ));
        final GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        gridData.heightHint = 80;
        textPanel.setLayoutData(gridData);
    }

    private void createTreeViewer(Composite parent) {
        this.treeViewer = new TreeViewer(parent);
        Tree tree = this.treeViewer.getTree();
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.treeViewer.getControl().setLayoutData(gridData);
        this.treeViewer.setUseHashlookup(true);

        /*** Tree table specific code starts ***/

        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TableLayout layout = new TableLayout();
        TreeColumn treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText(Messages.TaskViewColumn_0);

        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText(Messages.TaskViewColumn_1);

        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText(Messages.TaskViewColumn_2);

        treeColumn = new TreeColumn(tree, SWT.LEFT);
        treeColumn.setText(Messages.TaskViewColumn_3);

        // set initial column widths
        layout.addColumnData(new ColumnWeightData(40, true));
        layout.addColumnData(new ColumnWeightData(30, false));
        layout.addColumnData(new ColumnWeightData(15, false));
        layout.addColumnData(new ColumnWeightData(15, false));

        tree.setLayout(layout);

        for (TreeColumn tc : tree.getColumns()) {
            tc.pack();
        }

        tree.addListener(SWT.Expand, getCollapseExpandListener());
        tree.addListener(SWT.Collapse, getCollapseExpandListener());

        /*** Tree table specific code ends ***/
        this.contentProvider = new TaskContentProvider(this.treeViewer);
        this.treeViewer.setContentProvider(this.contentProvider);
        this.labelProvider = new TaskLabelProvider(onlyMyTasks);
        this.treeViewer.setLabelProvider(labelProvider);
    }

    private Listener getCollapseExpandListener() {
        Listener listener = new Listener() {

            @Override
            public void handleEvent(Event e) {
                final TreeItem treeItem = (TreeItem) e.item;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (TreeColumn tc : treeItem.getParent().getColumns()) {
                            tc.pack();
                        }
                    }
                });
            }
        };
        return listener;
    }

    private void makeActions() {
        refreshAction = new Action() {
            @Override
            public void run() {
                loadTasks();
            }
        };
        refreshAction.setText(Messages.ButtonRefresh);
        refreshAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));

        doubleClickAction = new Action() {
            public void run() {
                if (getViewer().getSelection() instanceof IStructuredSelection && ((IStructuredSelection) getViewer().getSelection()).getFirstElement() instanceof TaskInformation) {
                    try {
                        TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
                        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                        LoadAncestors loadControl = new LoadAncestors(task.getType(), task.getUuid(), ri);
                        loadControl = getCommandService().executeCommand(loadControl);
                        EditorFactory.getInstance().updateAndOpenObject(loadControl.getElement());
                    } catch (Throwable t) {
                        LOG.error("Error while opening control.", t);
                    }
                }
            }
        };
        myTasksAction = new Action(Messages.ButtonUserTasks, SWT.TOGGLE) {
            public void run() {
                onlyMyTasks = !onlyMyTasks;
                myTasksAction.setChecked(onlyMyTasks);
                labelProvider.setOnlyMyTasks(onlyMyTasks);
                loadTasks();
            }
        };
        myTasksAction.setChecked(onlyMyTasks);
        myTasksAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_PERSON));

        cancelTaskAction = new Action(Messages.ButtonCancel, SWT.TOGGLE) {
            public void run() {
                try {
                    cancelTask();
                    this.setChecked(false);
                } catch (Throwable e) {
                    showError("Error", "Error while canceling task.");
                    LOG.error("Error while canceling task.", e);
                }
            }
        };
        cancelTaskAction.setEnabled(false);      
        cancelTaskAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_NEIN));
        
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        configureActions();
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            configureActions();
        }
    }
    
    private void configureActions() {
        cancelTaskAction.setEnabled(getRightsService().isEnabled(ActionRightIDs.TASKDELETE));
        myTasksAction.setEnabled(getRightsService().isEnabled(ActionRightIDs.TASKSHOWALL));
    }

    @Deprecated
    private boolean isAdminUser(String username) {
        if (username.equals(ServiceFactory.lookupAuthService().getAdminUsername())) {
            return true;
        }
        for (String role : ServiceFactory.lookupAuthService().getRoles()) {
            if (role.equals(ApplicationRoles.ROLE_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param next
     */
    protected void completeTask(TaskInformation task, String outcomeId) {
        if (outcomeId == null) {
            ServiceFactory.lookupTaskService().completeTask(task.getId());
        } else {
            ServiceFactory.lookupTaskService().completeTask(task.getId(), outcomeId);
        }
        this.contentProvider.removeTask(task);
    }

    private void addActions() {
        addToolBarActions();
        getViewer().addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void addToolBarActions() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
        manager.add(myTasksAction);
        manager.add(cancelTaskAction);
    }

    private void addListener() {
        TaskLoader.addTaskListener(new ITaskListener() {
            @Override
            public void newTasks(List<ITask> taskList) {
                addTasks(taskList);
            }
        });
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
                manager.removeAll();
                addToolBarActions();
                cancelTaskAction.setEnabled(false);
                if (getViewer().getSelection() instanceof IStructuredSelection && ((IStructuredSelection) getViewer().getSelection()).getFirstElement() instanceof TaskInformation) {
                    try {
                        cancelTaskAction.setEnabled(getRightsService().isEnabled(ActionRightIDs.TASKDELETE));
                        TaskInformation task = (TaskInformation) ((IStructuredSelection) getViewer().getSelection()).getFirstElement();
                        getInfoPanel().setText( HtmlWriter.getPage(task.getDescription()));
                        List<KeyValue> outcomeList = task.getOutcomes();
                        for (KeyValue keyValue : outcomeList) {
                            CompleteTaskAction completeAction = new CompleteTaskAction(keyValue.getKey());
                            completeAction.setText(keyValue.getValue());
                            completeAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.MASSNAHMEN_UMSETZUNG_JA));
                            ActionContributionItem item = new ActionContributionItem(completeAction);
                            item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
                            manager.add(item);
                        }

                    } catch (Throwable t) {
                        LOG.error("Error while opening control.", t);
                    }
                }
                getViewSite().getActionBars().updateActionBars();
            }
        });
    }

    /**
     * @param taskList
     */
    protected void addTasks(final List<ITask> taskList) {
        List<ITask> currentTaskList = (List<ITask>) getViewer().getInput();
        if (currentTaskList != null) {
            for (ITask task : currentTaskList) {
                if (!taskList.contains(task)) {
                    taskList.add(task);
                }
            }
        }
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                getViewer().setInput(taskList);
            }
        });
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }

    protected TreeViewer getViewer() {
        return treeViewer;
    }
    
    protected Browser getInfoPanel() {
        return textPanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    @Override
    public String getPerspectiveId() {
        return Iso27kPerspective.ID;
    }

    protected void showError(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                MessageDialog.openError(TaskView.this.getSite().getShell(), title, message);
            }
        });     
    }

    protected void showInformation(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                MessageDialog.openInformation(TaskView.this.getSite().getShell(), title, message);
            }
        }); 
        
    }

    private void cancelTask() throws InvocationTargetException, InterruptedException {
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        final StructuredSelection selection = (StructuredSelection) getViewer().getSelection();
        final int number = selection.size();
        if (number > 0) {
            if (MessageDialog.openConfirm(parent.getShell(), Messages.ConfirmTaskDelete_0, Messages.bind(Messages.ConfirmTaskDelete_1, selection.size()))) {
                progressService.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Activator.inheritVeriniceContextState();
                        for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                            Object sel = iterator.next();
                            if (sel instanceof TaskInformation) {
                                TaskInformation task = (TaskInformation) sel;
                                ServiceFactory.lookupTaskService().cancelTask(task.getId());
                                TaskView.this.contentProvider.removeTask(task);
                            }
                        }
                    }
                });
                showInformation("Information", number + " task(s) deleted.");
            }

        }
    }

    /**
     * @return the rightsService
     */
    public RightsServiceClient getRightsService() {
        if(rightsService==null) {
            rightsService = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    private final class CompleteTaskAction extends Action {

        final String id = TaskView.class.getName() + ".complete";
        String outcomeId;

        public CompleteTaskAction(String outcomeId) {
            super();
            this.outcomeId = outcomeId;
            setId(id + "." + outcomeId);
        }

        @Override
        public void run() {
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            try {
                final StructuredSelection selection = (StructuredSelection) getViewer().getSelection();
                final int number = selection.size();
                progressService.run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Activator.inheritVeriniceContextState();
                        for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
                            Object sel = iterator.next();
                            if (sel instanceof TaskInformation) {
                                completeTask((TaskInformation) sel, outcomeId);
                            }
                        }
                        loadTasks();
                    }
                });
                showInformation("Information", number + " task(s) completed.");
            } catch (Throwable t) {
                showError("Error", "Error while completing task.");
                LOG.error("Error while completing tasks.", t);
            }
        }
    }
    
    private final class LoadTaskJob implements IRunnableWithProgress {      
        private ITaskParameter param;       
        private List<ITask> taskList;

        public LoadTaskJob(ITaskParameter param) {
            super();
            this.param = param;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            Activator.inheritVeriniceContextState();
            taskList = ServiceFactory.lookupTaskService().getTaskList(param);
            Collections.sort(taskList);
        }
        
        public List<ITask> getTaskList() {
            return taskList;
        }
    }
}
