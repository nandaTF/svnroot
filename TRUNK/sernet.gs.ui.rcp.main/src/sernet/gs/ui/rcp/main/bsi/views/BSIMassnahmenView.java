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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.Perspective;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIMassnahmenViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBSIMassnahmenViewAction;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;
import sernet.gs.ui.rcp.main.bsi.filter.GefaehrdungenFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.views.actions.MassnahmenViewFilterAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.rcp.RightsEnabledView;

/**
 * View for parsed BSI IT-Grundschutz catalogues.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class BSIMassnahmenView extends RightsEnabledView implements IAttachedToPerspective {

	// private Clipboard clipboard;

	public static final String ID = "sernet.gs.ui.rcp.main.views.bsimassnahmenview"; //$NON-NLS-1$

	private TreeViewer viewer;

	private MassnahmenViewFilterAction filterAction;

	private MassnahmenSiegelFilter siegelFilter;

	private CopyBSIMassnahmenViewAction copyAction;

	private GefaehrdungenFilter gefaehrdungenFilter;

	private Action expandAllAction;

	private Action collapseAction;
    
    private IModelLoadListener modelLoadListener;
	
	@Override
    public String getRightID(){
	    return ActionRightIDs.BSIMASSNAHMEN;
	}
	
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

	@Override
    public void createPartControl(Composite parent) {
	    super.createPartControl(parent);
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new KapitelSorter());

		startInitDataJob();

		viewer.setInput(BSIKatalogInvisibleRoot.getInstance());
		BSIKatalogInvisibleRoot.getInstance().addListener(new BSIKatalogInvisibleRoot.ISelectionListener() {
			@Override
            public void cataloguesChanged() {
				refresh();
			}
		});

		createActions();
		createFilters();
		createPullDownMenu();
		hookDNDListener();
		hookContextMenu();
		hookGlobalActions();
		getSite().setSelectionProvider(viewer);
		fillLocalToolBar();

		refresh();
	}
	
	protected void startInitDataJob() {
	    if(CnAElementFactory.getLoadedModel()!=null) {
	        WorkspaceJob job = new OpenCataloguesJob(Messages.BSIMassnahmenView_0);
	        JobScheduler.scheduleInitJob(job);
        } else if(modelLoadListener==null) {
            // model is not loaded yet: add a listener to load data when it's laoded
            modelLoadListener = new IModelLoadListener() {
                @Override
                public void closed(BSIModel model) {
                    // nothing to do
                }
                @Override
                public void loaded(BSIModel model) {
                    // work is done in loaded(ISO27KModel model)
                }

                @Override
                public void loaded(ISO27KModel model) {
                    startInitDataJob();
                }
                
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
	    
	}
	
	

	private void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
            public void run() {
				viewer.refresh();
			}
		});
	}

	private void hookGlobalActions() {
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
	}

	private void createActions() {
		copyAction = new CopyBSIMassnahmenViewAction(this, Messages.BSIMassnahmenView_4);

		expandAllAction = new Action() {
			@Override
			public void run() {
				viewer.expandAll();
			}
		};
		expandAllAction.setText(Messages.BSIMassnahmenView_5);
		expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

		collapseAction = new Action() {
			@Override
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setText(Messages.BSIMassnahmenView_6);
		collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new GroupMarker("content")); //$NON-NLS-1$
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		manager.add(filterAction);
		manager.add(expandAllAction);
		manager.add(collapseAction);

		manager.add(new Separator());
		manager.add(copyAction);

	}

	private void fillLocalToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(this.filterAction);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
            public void menuAboutToShow(IMenuManager manager) {
				BSIMassnahmenView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDNDListener() {
	    final Transfer igsmTransfer = IGSModelElementTransfer.getInstance();
	    Transfer[] types = new Transfer[] {igsmTransfer};
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(operations, types, new BSIMassnahmenViewDragListener(viewer));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
    public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createFilters() {
		siegelFilter = new MassnahmenSiegelFilter(viewer);
		gefaehrdungenFilter = new GefaehrdungenFilter(viewer);
	}

	private void createPullDownMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
		filterAction = new MassnahmenViewFilterAction(viewer, Messages.BSIMassnahmenView_3, this.siegelFilter, this.gefaehrdungenFilter);
		menuManager.add(filterAction);
		menuManager.add(copyAction);
		menuManager.add(expandAllAction);
		menuManager.add(collapseAction);
	}

	public List<Baustein> getSelectedBausteine() {
		ArrayList<Baustein> result = new ArrayList<Baustein>();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Baustein) {
				result.add((Baustein) o);
			}
		}
		return result;
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) viewer.getSelection();
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
	 */
	@Override
    public String getPerspectiveId() {
		return Perspective.ID;
	}
	
	private static class KapitelSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof Massnahme && e2 instanceof Massnahme) {
				// sort chapters correctly by converting 2.45, 2.221, 3.42
				// to 2045, 2221, 3024

				return (Integer.valueOf(((Massnahme) e1).getKapitelValue()).compareTo(((Massnahme) e2).getKapitelValue()));
			}

			if (e1 instanceof Gefaehrdung && e2 instanceof Gefaehrdung) {
				return (Integer.valueOf(((Gefaehrdung) e1).getKapitelValue()).compareTo(((Gefaehrdung) e2).getKapitelValue()));

			}

			if (e1 instanceof Baustein && e2 instanceof Baustein) {
				// sort chapters correctly by converting 2.45, 2.221, 3.42
				// to 2045, 2221, 3024
				return (Integer.valueOf(((Baustein) e1).getKapitelValue()).compareTo(((Baustein) e2).getKapitelValue()));
			}

			return super.compare(viewer, e1, e2);
		}
	}

	static class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		@Override
        public void dispose() {
		}

		@Override
        public Object[] getChildren(Object parent) {
		    final int childrenListSize = 100;
			if (parent instanceof Baustein) {
				ArrayList<IGSModel> children = new ArrayList<IGSModel>(childrenListSize);
				children.addAll(((Baustein) parent).getGefaehrdungen());
				children.addAll(((Baustein) parent).getMassnahmen());
				return children.toArray();
			} else if (parent instanceof BSIKatalogInvisibleRoot) {
				return ((BSIKatalogInvisibleRoot) parent).getBausteine().toArray();
			}
			return new Object[0];
		}

		@Override
        public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		@Override
        public Object getParent(Object child) {
			return null;
		}

		@Override
        public boolean hasChildren(Object parent) {
			if (parent instanceof Baustein){
				return ((Baustein) parent).getMassnahmen().size() > 0;
			} else if (parent instanceof BSIKatalogInvisibleRoot){
				return ((BSIKatalogInvisibleRoot) parent).getBausteine().size() > 0;
			}
			return false;
		}

		@Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
	}

	static class ViewLabelProvider extends LabelProvider {

		@Override
        public Image getImage(Object obj) {

			if (obj instanceof Baustein){
				return ImageCache.getInstance().getImage(ImageCache.BAUSTEIN);
			}
			if (obj instanceof Massnahme) {
				Massnahme mn = (Massnahme) obj;
				char stufe = mn.getSiegelstufe();
				switch (stufe) {
				case 'A':
					return ImageCache.getInstance().getImage(ImageCache.STUFE_A);
				case 'B':
					return ImageCache.getInstance().getImage(ImageCache.STUFE_B);
				case 'C':
					return ImageCache.getInstance().getImage(ImageCache.STUFE_C);
				case 'Z':
					return ImageCache.getInstance().getImage(ImageCache.STUFE_Z);
				case 'W':
					return ImageCache.getInstance().getImage(ImageCache.STUFE_W);
				}
			}

			if (obj instanceof Gefaehrdung){
				return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
			}
			return ImageCache.getInstance().getImage(ImageCache.UNKNOWN);

		}

		@Override
        public String getText(Object obj) {
			if (obj instanceof Massnahme) {
				Massnahme mn = (Massnahme) obj;
				return mn.getId() + " " + mn.getTitel() + " [" //$NON-NLS-1$ //$NON-NLS-2$
						+ mn.getSiegelstufe() + "] (" + mn.getLZAsString() //$NON-NLS-1$
						+ ")"; //$NON-NLS-1$
			}

			if (obj instanceof Gefaehrdung) {
				Gefaehrdung gef = (Gefaehrdung) obj;
				return gef.getId() + " " + gef.getTitel() + " [" + gef.getKategorieAsString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return obj.toString();
		}
	}

}
