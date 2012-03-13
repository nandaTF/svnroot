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
package sernet.verinice.rcp.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementFilter;
import sernet.verinice.service.commands.LoadTreeItem;

/**
 * ElementManager loads domain objects ({@link CnATreeElement}) for {@link TreeContentProvider}
 * from the verinice backend.
 * 
 * ElementManager caches objects to ensure that they are loaded only once.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class ElementManager {

    private static final Logger LOG = Logger.getLogger(ElementManager.class);

    private ElementCache cache;
    
    private List<IParameter> paramerterList; 
    
    private ICommandService commandService;
    
    /**
     * Creates a new ElementManager with a new ElementCache
     */ 
    public ElementManager() {
        super();
        cache = new ElementCache();
    }
    
    /**
     * Returns the children of a {@link CnATreeElement}.
     * 
     * All children of the parent element and properties of these children 
     * are loaded and initialized (and not lazy).
     * 
     * Calling this method might change parameter parentElement.
     * Children set in parentElement is replaced by return value.
     * 
     * @param parentElement The parent of the children
     * @return An array with the children of the parentElement
     */
    public CnATreeElement[] getChildren(CnATreeElement parentElement) {
        try {
            if(parentElement instanceof NullModel) {
                return new CnATreeElement[0];
            }       
            CacheObject cachedElement = cache.getCachedObject(parentElement);
            CnATreeElement elementWithChildren = null;
            if(cachedElement!=null && cachedElement.isChildrenPropertiesLoaded()) {
                elementWithChildren = cachedElement.getElement();
            } else {
                elementWithChildren = loadElementWithChildren(parentElement);
                parentElement.setChildren(elementWithChildren.getChildren());
            }
            return extractChildren(elementWithChildren);
        } catch(RuntimeException re) {
            LOG.error("RuntimeException while getting children", re);
            throw re;
        } catch(Exception e) {
            LOG.error("Exception while getting children", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Returns true if element has children false if not.
     * 
     * This method never does remote calls. If return value is not found in cache 
     * it logs a warning and returns <code>true</code>.
     * 
     * @param parentElement A CnATreeElement
     * @return True if element has children, false if not
     */
    public boolean hasChildren(CnATreeElement parentElement) {
        try {
            boolean hasChildren = true;
            CacheObject cachedElement = cache.getCachedObject(parentElement);
            if(cachedElement!=null) {
                hasChildren = (cachedElement.getHasChildren()==ChildrenExist.YES);
            } else {
                String uuid = (parentElement!=null) ? parentElement.getUuid() : "unknown";
                LOG.warn("Can't determine if element has children (returning true). Element not found in cache, uuid: " + uuid);
            }
            return hasChildren;
        } catch(RuntimeException re) {
            LOG.error("RuntimeException while getting children", re);
            throw re;
        } catch(Exception e) {
            LOG.error("Exception while getting children", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method is called when an element has changed.
     * 
     * @param element Changed element
     */
    public void elementChanged(CnATreeElement element) {
        try {         
            replaceOrAddInCache(element,false,checkChildren(element));
            updateParentInCache(element);  
        } catch(RuntimeException re) {
            LOG.error("RuntimeException in elementChanged", re);
            throw re;
        } catch(Exception e) {
            LOG.error("Exception in elementChanged", e);
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * @param child
     */
    public void elementAdded(CnATreeElement element) {
        try {       
            replaceOrAddInCache(element,false,checkChildren(element));
            updateParentInCache(element);  
        } catch(RuntimeException re) {
            LOG.error("RuntimeException in elementAdded", re);
            throw re;
        } catch(Exception e) {
            LOG.error("Exception in elementAdded", e);
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * Clears the cache. Removes all cached elements.
     */
    public void clearCache() {
        cache.clear();       
    }
    
    /**
     * @param tagFilter
     */
    public void addParameter(IParameter parameter) {
        getParameterList().add(parameter);
    }
    
    public List<IParameter> getParameterList() {
        if(paramerterList==null) {
            paramerterList = new ArrayList<IParameter>();
        }
        return paramerterList;
    }

    /**
     * Replaces (or adds) an element in cache.
     * 
     * Children loaded cache proeperty is set to false.
     * 
     * Take care that children set of the element is initialized
     * ans not a Hibernate proxy, otherwise a lazy-exception ist thrown.
     * 
     * @param element A CnATreeElement
     */
    private void replaceOrAddInCache(CnATreeElement element, boolean childrenPropertiesLoaded, ChildrenExist hasChildren) {
        cache.addObject(new CacheObject(element, childrenPropertiesLoaded, hasChildren));
    }
  
    
    /**
     * Updates the parent of an element in cache.
     * 
     * If parent of the element exists in cache the element is 
     * replaced in children set of parent.
     * 
     * Take care that parent set of the element is initialized
     * and not a Hibernate proxy, otherwise a lazy-exception ist thrown.
     * 
     * @param element A CnATreeElement
     */
    private void updateParentInCache(CnATreeElement element) {
        CacheObject cachObjectParent = cache.getCachedObject(element.getParent());
        if(cachObjectParent!=null) {
            CnATreeElement parentFromCache = cachObjectParent.getElement();
            boolean exists = parentFromCache.getChildren().remove(element);
            if(exists) {
                LOG.debug("Old element removed from parent child set in cache...");
            }
            boolean added = parentFromCache.getChildren().add(element);
            if(added) {
                LOG.debug("Element added to parent child set in cache.");
            } else {
                LOG.warn("Can not add element to parent's child set in cache.");
            }
            CacheObject newCacheObjectParent = new CacheObject(parentFromCache, cachObjectParent.isChildrenPropertiesLoaded());           
            cache.addObject(newCacheObjectParent);
        } 
        
    }
    
    /**
     * Loads an element with children with one remote call.
     * 
     * Which parts of the element are loaded and initialized:
     * <ul>
     * <li>Entity and properties</li>
     * <li>Children</li>
     * <li>Children-entity and children-properties</li>
     * <li>Grandchildren (children of the children)</li>
     * </ul>
     * 
     * @param element A CnATreeElement
     * @return Element with initialized children and children properties
     * @throws CommandException If executing of command fails
     */
    private CnATreeElement loadElementWithChildren(CnATreeElement element) throws CommandException {
        RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true).setChildrenProperties(true);
        if(cache.getCachedObject(element)==null) {
            // no element found in cache, load properties AND children
            ri.setProperties(true);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loading parent and children from database, parent uuid: " + element.getUuid());
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Loading children from database, parent uuid: " + element.getUuid());
        }
        LoadTreeItem command = new LoadTreeItem(element.getUuid(),ri,ElementFilter.getConvertToMap(getParameterList()));
        command = getCommandService().executeCommand(command);
        CnATreeElement elementWithChildren = command.getElement();
        addToCache(elementWithChildren,command.getHasChildrenMap());
        return elementWithChildren;

    }
    
    private CacheObject addToCache(CnATreeElement element, Map<String, Boolean> hasChildrenMap) {
        CacheObject cachedElement = cache.getCachedObject(element);
        if(cachedElement==null) {
            // add retrived element to cache
            cachedElement = new CacheObject(element, true);
            cache.addObject(cachedElement);
        } else {
            // element found in cache, replace children in cached element
            CnATreeElement elementFromCache = cachedElement.getElement();
            elementFromCache.setChildren(element.getChildren());
            cachedElement = new CacheObject(elementFromCache, true);
            cache.addObject(cachedElement);
        }
        // add children to cache
        for (CnATreeElement child : element.getChildren()) {
            ChildrenExist hasChildren = ChildrenExist.convert(hasChildrenMap.get(child.getUuid()));        
            cache.addObject(new CacheObject(child, false, hasChildren));
        }
        return cachedElement;
    }
    
    private static ChildrenExist checkChildren(CnATreeElement element) {
        ChildrenExist hasChildren = ChildrenExist.UNKNOWN;     
        if(element.getChildren().size()>0) {
            hasChildren = ChildrenExist.YES; 
        } else {
            hasChildren = ChildrenExist.NO; 
        }
        return hasChildren;
    }
    
    private static CnATreeElement[] extractChildren(CnATreeElement cachedElement) {     
        Set<CnATreeElement> childrenSet = cachedElement.getChildren();
        CnATreeElement[] children = new CnATreeElement[childrenSet.size()];
        int n = 0;
        for (CnATreeElement child : childrenSet) {
            children[n] = child;
            n++;
        }        
        return children;    
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private ICommandService createCommandService() {
        return ServiceFactory.lookupCommandService();
    }

}
