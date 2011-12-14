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


import java.util.List;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.CnATreeElement;


/**
 * Cache that is shared between a view's content and label provider and it's model update listener.
 * 
 * Used to allow lazy loading of tree elements, resolving already displayed
 * items when a new instance for an already displayed object is returned from the database.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("restriction")
public class TreeViewerCache {

    private static final Logger LOG = Logger.getLogger(TreeViewerCache.class);
	
	private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
	
	public TreeViewerCache() {
		createCache();
	}
	
	public void addObject(Object o) {
	    if(o!=null && o instanceof CnATreeElement) {
	        addObject((CnATreeElement)o);
	    } else {
            LOG.warn("Object is null or not an CnATreeElement. Will not add this to cache.");
        }
 	}
	
	public void addObject(CnATreeElement e) {
	    try {
            if(e!=null) {
                getCache().put(new Element(e.getUuid(), e));
                if (LOG.isDebugEnabled()) {
                    Statistics s = getCache().getStatistics();
                    LOG.debug("Element added, uuid: " + e.getUuid() + ", size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());
                }
            } else {
                LOG.warn("Object is null. Will not add this to cache.");
            }
        } catch(Throwable t) {
            LOG.error("Error while adding object",t);
        }
	}
	
	public void clear() {
		manager.clearAll();
		if (LOG.isDebugEnabled()) {
		    Statistics s = getCache().getStatistics();
            LOG.debug("Cache cleared, size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());
        }
	}
	
	public CnATreeElement getCachedObject(CnATreeElement e) {
	    try {
	        CnATreeElement value = null;
	        if(e!=null) {
    	        Element element = getCache().get(e.getUuid());
    	        if(element!=null) {
    	            value = (CnATreeElement) element.getObjectValue();
    	            if (LOG.isDebugEnabled()) {
                        if(value!=null) {
                            LOG.debug("Cache hit for uuid: " + e.getUuid() + ", children loaded: " + e.isChildrenLoaded());
                        } else {
                            LOG.debug("No cached element for uuid: " + e.getUuid());
                        }
                    }
    	        }   
	        }
    	    return value;
    	} catch(Throwable t) {
            LOG.error("Error while getting object",t);
            return null;
        }
	}

	public void clear(CnATreeElement oldElement) {
	    try {
	        getCache().remove(oldElement.getUuid());
        } catch(Throwable t) {
            LOG.error("Error while adding object",t);
        }	
	}

	/**
	 * @param id
	 * @return
	 */
	public CnATreeElement getCachedObjectById(Integer id) {
	    try {
    		synchronized(cache) {
    		    List<String> keyList = getCache().getKeys();
    			for (String key : keyList) {
    			   Element element =  getCache().get(key);
    				if (element.getObjectValue() instanceof CnATreeElement) {
    					CnATreeElement cnaElmt = (CnATreeElement) element.getObjectValue();
    					if (cnaElmt.getDbId().equals(id))
    						return cnaElmt;
    				}
    			}
    			return null;
    		}
    	} catch(Throwable t) {
            LOG.error("Error while getting object",t);
            return null;
        }
	}
	
	private Cache getCache() {     
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = manager.getCache(cacheId);
        }
        return cache;
    }
    
    private Cache createCache() {
        shutdownCache();
        cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, 5000, false, false, 3600, 3600);
        manager.addCache(cache);
        return cache;
    }

    private void shutdownCache() {
        if(manager!=null && !Status.STATUS_SHUTDOWN.equals(manager.getStatus())) {
            manager.shutdown();
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        shutdownCache();
        super.finalize();
    }

	
}
