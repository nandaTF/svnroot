/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.Perspective;
import sernet.gs.ui.rcp.main.actions.ShowAccessControlEditAction;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.bsi.actions.BausteinZuordnungAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropPerformer;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.LebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ObjektLebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.rcp.IAttachedToPerspective;

/**
 * View for model of own "ITVerbund" with associated controls, risk analysis
 * etc.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class BsiModelView extends ViewPart implements IAttachedToPerspective {

	private static final Logger LOG = Logger.getLogger(BsiModelView.class);
	
	public static final String ID = "sernet.gs.ui.rcp.main.views.bsimodelview"; //$NON-NLS-1$

	private Action doubleClickAction;

	private DrillDownAdapter drillDownAdapter;

	private BSIModel model;

	private TreeViewer viewer;

	private BSIModelViewFilterAction filterAction;

	private BSIModelViewContentProvider contentProvider;

	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if ((event.getProperty().equals(PreferenceConstants.DB_URL) || event.getProperty().equals(PreferenceConstants.DB_USER) || event.getProperty().equals(PreferenceConstants.DB_DRIVER) || event.getProperty().equals(PreferenceConstants.DB_PASS))) {
				CnAElementFactory.getInstance().closeModel();
				setNullModel();
			}
		}
	};

	private Action expandAllAction;

	private Action collapseAction;

	private ShowBulkEditAction bulkEditAction;

	private ShowAccessControlEditAction accessControlEditAction;

	private Action selectEqualsAction;

	private ShowKonsolidatorAction konsolidatorAction;

	private Action selectLinksAction;

	private TreeViewerCache cache;

	private BausteinZuordnungAction bausteinZuordnungAction;

	private MetaDropAdapter dropAdapter;
	
	private IModelLoadListener modelLoadListener;

	public void setNullModel() {
		model = new NullModel();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(model);
				viewer.refresh();
			}
		});
	}

	/**
	 * The constructor.
	 */
	public BsiModelView() {
		this.cache = new TreeViewerCache();
	}

	private void addBSIFilter() {
		viewer.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IDatenschutzElement) {
					return false;
				}
				return true;
			}

		});
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			initView(parent);
			startInitDataJob();
		} catch (Exception e) {
			LOG.error("Error while creating organization view", e);
			ExceptionUtil.log(e, "Error while opening ISM-View.");
		}
	}

	private void initView(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(contentProvider = new BSIModelViewContentProvider(cache));
		viewer.setLabelProvider(new BSIModelViewLabelProvider(cache));
		viewer.setSorter(new CnAElementByTitelSorter());

		getSite().setSelectionProvider(viewer);
		makeActions();
		createPullDownMenu();
		hookContextMenu();
		hookDoubleClickAction();
		hookDNDListeners();
		hookGlobalActions();
		addBSIFilter();
		fillLocalToolBar();
		Activator.getDefault().getPluginPreferences().addPropertyChangeListener(this.prefChangeListener);
		setNullModel();
	}
	
	/**
	 * 
	 */
	protected void startInitDataJob() {
		WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
					initData();
				} catch (Exception e) {
					LOG.error("Error while loading data.", e);
					status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.",e); //$NON-NLS-1$
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		JobScheduler.scheduleInitJob(initDataJob);		
	}
	
	private void initData() {
		if(CnAElementFactory.isModelLoaded()) {
			setModel(CnAElementFactory.getLoadedModel());
		} else if(modelLoadListener==null) {
			// model is not loaded yet: add a listener to load data when it's laoded
			modelLoadListener = new IModelLoadListener() {
				public void closed(BSIModel model) {
					// nothing to do
				}
				public void loaded(BSIModel model) {
					startInitDataJob();
				}		
			};
			CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
		}
	}

	private void hookGlobalActions() {
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker("content")); //$NON-NLS-1$
		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator());
		manager.add(new GroupMarker("special")); //$NON-NLS-1$
		manager.add(bulkEditAction);
		manager.add(accessControlEditAction);
		manager.add(selectEqualsAction);
		manager.add(selectLinksAction);
		selectEqualsAction.setEnabled(bausteinSelected());
		manager.add(konsolidatorAction);
		manager.add(bausteinZuordnungAction);

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		manager.add(expandAllAction);
		manager.add(collapseAction);

		manager.add(new Separator());

	}

	private boolean bausteinSelected() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (!sel.isEmpty() && sel.size() == 1 && sel.getFirstElement() instanceof BausteinUmsetzung) {
			return true;
		}
		return false;
	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.filterAction);
		// manager.add(expandAllAction);

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());

		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDNDListeners() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(), FileTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDropSupport(operations, types, dropAdapter);
		viewer.addDragSupport(operations, types, new BSIModelViewDragListener(viewer));
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void makeActions() {

		selectEqualsAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof BausteinUmsetzung) {
					BausteinUmsetzung sourceBst = (BausteinUmsetzung) o;
					ArrayList newsel = new ArrayList(10);
					newsel.add(sourceBst);

					try {
						LoadCnAElementByType<BausteinUmsetzung> command = new LoadCnAElementByType<BausteinUmsetzung>(BausteinUmsetzung.class);
						command = ServiceFactory.lookupCommandService().executeCommand(command);
						List<BausteinUmsetzung> bausteine = command.getElements();

						for (BausteinUmsetzung bst : bausteine) {
							if (bst.getKapitel().equals(sourceBst.getKapitel())) {
								newsel.add(bst);
							}
						}
					} catch (CommandException e) {
						ExceptionUtil.log(e, "");
					}

					viewer.setSelection(new StructuredSelection(newsel));
				}
			}
		};
		selectEqualsAction.setText("Gleiche Bausteine selektieren");

		selectLinksAction = new Action() {
			@Override
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof IBSIStrukturElement && o instanceof CnATreeElement) {
					CnATreeElement elmt = (CnATreeElement) o;
					Set<CnALink> links = elmt.getLinksUp();
					CnALink[] foundLinks = links.toArray(new CnALink[links.size()]);
					viewer.setSelection(new StructuredSelection(foundLinks));
				}
			}
		};
		selectLinksAction.setText("Alle Verknüpfungen zu diesem Objekt markieren");

		bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(), "Bulk Edit...");

		accessControlEditAction = new ShowAccessControlEditAction(getViewSite().getWorkbenchWindow(), "Zugriffsrechte...");

		konsolidatorAction = new ShowKonsolidatorAction(getViewSite().getWorkbenchWindow(), "Konsolidator...");

		bausteinZuordnungAction = new BausteinZuordnungAction(getViewSite().getWorkbenchWindow());

		doubleClickAction = new Action() {
			@Override
			public void run() {
				Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();

				if (sel instanceof FinishedRiskAnalysis) {
					FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) sel;
					RiskAnalysisWizard wizard = new RiskAnalysisWizard(analysis.getParent(), analysis);
					wizard.init(PlatformUI.getWorkbench(), null);
					WizardDialog wizDialog = new org.eclipse.jface.wizard.WizardDialog(new Shell(), wizard);
					wizDialog.setPageSize(800, 600);
					wizDialog.open();
				} else {
					// open editor:
					EditorFactory.getInstance().updateAndOpenObject(sel);
				}
			}
		};

		BSIModelElementFilter modelElementFilter = new BSIModelElementFilter(viewer);
		// The model filter is normally used for the view. By giving the filter
		// also to the content provider this can be used to minimize database
		// access.
		contentProvider.setModelElementFilter(modelElementFilter);
		filterAction = new BSIModelViewFilterAction(viewer, Messages.BsiModelView_3, new MassnahmenUmsetzungFilter(viewer), new MassnahmenSiegelFilter(viewer), new LebenszyklusPropertyFilter(viewer), new ObjektLebenszyklusPropertyFilter(viewer), modelElementFilter, new TagFilter(viewer));

		expandAllAction = new Action() {
			@Override
			public void run() {
				expandAll();
			}
		};
		expandAllAction.setText("Alle aufklappen");
		expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setText("Alle zuklappen");
		collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));

		dropAdapter = new MetaDropAdapter(viewer);
		dropAdapter.addAdapter(new BSIModelViewDropPerformer());

	}

	private void expandAll() {
		// TODO: do this a new thread and show user a progress bar
		viewer.expandAll();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		menuManager.add(filterAction);
		menuManager.add(expandAllAction);
		menuManager.add(collapseAction);

		menuManager.add(new Separator());
	}

	public void setModel(BSIModel model2) {
		this.model = model2;
		model.addBSIModelListener(new BSIModelViewUpdater(viewer, cache));

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					viewer.setInput(model);
					viewer.refresh();
				} catch (Exception e) {
					ExceptionUtil.log(e, "Konnte Modell nicht laden.");
				}
			}
		});
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
	 */
	public String getPerspectiveId() {
		return Perspective.ID;
	}
}
