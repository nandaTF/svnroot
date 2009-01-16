package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.actions.ShowKonsolidatorAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBSIModelViewAction;
import sernet.gs.ui.rcp.main.bsi.dnd.PasteBsiModelViewAction;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.filter.LebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenUmsetzungFilter;
import sernet.gs.ui.rcp.main.bsi.filter.ObjektLebenszyklusPropertyFilter;
import sernet.gs.ui.rcp.main.bsi.filter.TagFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard.RiskAnalysisWizard;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewCloseDBAction;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewFilterAction;
import sernet.gs.ui.rcp.main.bsi.views.actions.BSIModelViewOpenDBAction;
import sernet.gs.ui.rcp.main.common.model.ChangeLogWatcher;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.common.model.ObjectDeletedException;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * View for model of own "ITVerbund" with associated controls, risk analysis
 * etc.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class BsiModelView extends ViewPart {
	

	

	public static final String ID = "sernet.gs.ui.rcp.main.views.bsimodelview"; //$NON-NLS-1$

	private Action doubleClickAction;

	private DrillDownAdapter drillDownAdapter;

	private BSIModel model;

	private TreeViewer viewer;

	private BSIModelViewFilterAction filterAction;

	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if ((event.getProperty().equals(PreferenceConstants.DB_URL)
					|| event.getProperty().equals(PreferenceConstants.DB_USER)
					|| event.getProperty()
							.equals(PreferenceConstants.DB_DRIVER) || event
					.getProperty().equals(PreferenceConstants.DB_PASS))) {
				CnAElementFactory.getInstance().closeModel();
				setNullModel();
			}
		}
	};

	private Action openDBAction;

	private BSIModelViewCloseDBAction closeDBAction;

	private PasteBsiModelViewAction pasteAction;

	private Action expandAllAction;

	private Action collapseAction;

	private ShowBulkEditAction bulkEditAction;

	private Action selectEqualsAction;

	private ShowKonsolidatorAction konsolidatorAction;

	private CopyBSIModelViewAction copyAction;

	private Action selectLinksAction;

	private TreeViewerCache cache;

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
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IDatenschutzElement)
					return false;
				return true;
			}

		});
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new BSIModelViewContentProvider(cache));
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
		Activator.getDefault().getPluginPreferences()
				.addPropertyChangeListener(this.prefChangeListener);
		setNullModel();

	}

	private void hookGlobalActions() {
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.PASTE.getId(), pasteAction);
		getViewSite().getActionBars().setGlobalActionHandler(
				ActionFactory.COPY.getId(), copyAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker("content")); //$NON-NLS-1$
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		manager.add(new Separator());
		manager.add(copyAction);
		manager.add(pasteAction);
		manager.add(bulkEditAction);
		manager.add(selectEqualsAction);
		manager.add(selectLinksAction);
		selectEqualsAction.setEnabled(bausteinSelected());
		manager.add(konsolidatorAction);

		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		manager.add(expandAllAction);
		manager.add(collapseAction);

		manager.add(new Separator());
		manager.add(this.openDBAction);
		manager.add(closeDBAction);

	}

	private boolean bausteinSelected() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (!sel.isEmpty() && sel.size() == 1
				&& sel.getFirstElement() instanceof BausteinUmsetzung)
			return true;
		return false;
	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.openDBAction);
		manager.add(this.closeDBAction);
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
		Transfer[] types = new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDropSupport(operations, types, new BSIModelViewDropListener(
				viewer));
		viewer.addDragSupport(operations, types, new BSIModelViewDragListener(
				viewer));
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
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer
						.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof BausteinUmsetzung) {
					BausteinUmsetzung sourceBst = (BausteinUmsetzung) o;
					ArrayList newsel = new ArrayList(10);
					newsel.add(sourceBst);
					// FIXME lazyliy load bausteine on access
					ArrayList<BausteinUmsetzung> alleBausteine = model
							.getBausteine();
					for (BausteinUmsetzung bst : alleBausteine) {
						if (bst.getKapitel().equals(sourceBst.getKapitel()))
							newsel.add(bst);
					}
					viewer.setSelection(new StructuredSelection(newsel));
				}
			}
		};
		selectEqualsAction.setText("Gleiche Bausteine selektieren");

		selectLinksAction = new Action() {
			public void run() {
				IStructuredSelection sel = (IStructuredSelection) viewer
						.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof IBSIStrukturElement
						&& o instanceof CnATreeElement) {
					CnATreeElement elmt = (CnATreeElement) o;
					Set<CnALink> links = elmt.getLinksUp();
					CnALink[] foundLinks = (CnALink[]) links
							.toArray(new CnALink[links.size()]);
					viewer.setSelection(new StructuredSelection(foundLinks));
				}
			}
		};
		selectLinksAction
				.setText("Alle Verknüpfungen zu diesem Objekt markieren");

		bulkEditAction = new ShowBulkEditAction(getViewSite()
				.getWorkbenchWindow(), "Bulk Edit...");

		konsolidatorAction = new ShowKonsolidatorAction(getViewSite()
				.getWorkbenchWindow(), "Konsolidator...");

		doubleClickAction = new Action() {
			public void run() {
				Object sel = ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
			
				if (sel instanceof FinishedRiskAnalysis) {
					FinishedRiskAnalysis analysis = (FinishedRiskAnalysis) sel;
					RiskAnalysisWizard wizard = new RiskAnalysisWizard(analysis
							.getParent(), analysis);
					wizard.init(PlatformUI.getWorkbench(), null);
					WizardDialog wizDialog = new org.eclipse.jface.wizard.WizardDialog(
							new Shell(), wizard);
					wizDialog.setPageSize(800, 600);
					wizDialog.open();
				}
				
				else if (sel instanceof CnALink) {
					// jump to linked item:
					viewer.setSelection(new StructuredSelection(((CnALink) sel)
							.getDependency()), true);
				}
				
				else
					// open editor:
					EditorFactory.getInstance().updateAndOpenObject(sel);
			}
		};

		filterAction = new BSIModelViewFilterAction(viewer,
				Messages.BsiModelView_3, new MassnahmenUmsetzungFilter(viewer),
				new MassnahmenSiegelFilter(viewer),
				new LebenszyklusPropertyFilter(viewer),
				new ObjektLebenszyklusPropertyFilter(viewer),
				new BSIModelElementFilter(viewer), new TagFilter(viewer));

		expandAllAction = new Action() {
			@Override
			public void run() {
				viewer.expandAll();
			}
		};
		expandAllAction.setText("Alle aufklappen");
		expandAllAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setText("Alle zuklappen");
		collapseAction.setImageDescriptor(ImageCache.getInstance()
				.getImageDescriptor(ImageCache.COLLAPSEALL));

		copyAction = new CopyBSIModelViewAction(this, "Kopieren");
		pasteAction = new PasteBsiModelViewAction(this.viewer, "Einfügen");

		openDBAction = new BSIModelViewOpenDBAction(this, viewer);

		closeDBAction = new BSIModelViewCloseDBAction(this, viewer);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(openDBAction);
		menuManager.add(closeDBAction);
		menuManager.add(filterAction);
		menuManager.add(expandAllAction);
		menuManager.add(collapseAction);

		menuManager.add(new Separator());
		menuManager.add(copyAction);
		menuManager.add(pasteAction);

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

}