package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.TreeViewer;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;

/**
 * Listener to check for model changes and update the connected tree viewer.
 * 
 * Uses cache to resolve objects being updated. This cache is shared between ContentProvider, LabelProvider and 
 * Updater (this class) to update only actually displayed objects based on object identity 
 * (as defined by the objects' equals() method.
 * 
 * This is necessary because objects loaded from the database may be instantiated multiple times in memory.
 * Also, loaded objects may not be fully initialised or have less elements loaded than the ones in the cache
 * that are already displayed.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class BSIModelViewUpdater implements IBSIModelListener {

	private TreeViewer viewer;
	private ThreadSafeViewerUpdate updater;
	
	// cache to figure out if an element is currently displayed in the tree or not
	private TreeViewerCache cache;

	BSIModelViewUpdater(TreeViewer viewer, TreeViewerCache cache) {
		this.viewer = viewer;
		this.cache = cache;
		this.updater = new ThreadSafeViewerUpdate(viewer);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		
		CnATreeElement cachedObject = cache.getCachedObject(child);
		if (cachedObject == null)
			return; // not currently displayed or already changed object itself so nothing to update

		if (cachedObject != child) {
			// update entity of cached object:
			try {
				CnAElementHome.getInstance().refresh(cachedObject);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Aktualisieren der Baumansicht.");
			}
		}
		updater.refresh();
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
	}

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		updater.refresh();
	}

	public void linkChanged(CnALink link) {
		// is top element visible?
		CnATreeElement oldElement = cache.getCachedObject(link.getParent()
				.getParent());
		
		if (oldElement != null) {
			// load and add linkkategory:
			oldElement.setLinks(link.getParent());
			link.getParent().setParent(oldElement);
			
			// replace old instance of link with new one:
			oldElement.removeLinkDown(link);
			oldElement.addLinkDown(link);
			updater.refresh(link);
			updater.refresh(link.getParent());
			updater.reveal(link);
		}
	}
	
	public void linkAdded(CnALink link) {
		CnATreeElement cachedParent = cache.getCachedObject(link.getDependant());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.setLinks(link.getParent());
			updater.reveal(link.getParent());
			does link add replace personenkategorie with proxy?
		}
		updater.refresh();
	}
	
	
	public void linkRemoved(CnALink link) {
		// is top element visible?
		CnATreeElement oldElement = cache.getCachedObject(link.getParent()
				.getParent());
		
		if (oldElement != null) {
			// load and add linkkategory:
			oldElement.setLinks(link.getParent());
			link.getParent().setParent(oldElement);
			
			oldElement.removeLinkDown(link);
			updater.remove(link);
		}
	}

	public void databaseChildAdded(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.addChild(child);
		}
		updater.refresh();
	}

	public void databaseChildChanged(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
		}
		updater.refresh();
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
		}
		updater.refresh();
	}
}
