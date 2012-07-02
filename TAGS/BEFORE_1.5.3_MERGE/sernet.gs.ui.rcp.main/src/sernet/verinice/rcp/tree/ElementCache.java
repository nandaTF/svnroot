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

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.CnATreeElement;

/**
 * In memory cache for {@link CnATreeElement}s.
 * Elements are cached with ehcache: http://ehcache.org/
 * 
 * Cache is configured by parameter:
 * MAX_ELEMENTS_IN_MEMORY 
 * TIME_TO_LIVE_SECONDS  
 * TIME_TO_IDLE_SECONDS
 * See comments for details.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class ElementCache {

    private static final Logger LOG = Logger.getLogger(ElementCache.class);
	
    /*
     * Configuration parameter
     */  
    //Maximal number of elements in cache
    private static final int MAX_ELEMENTS_IN_MEMORY = 5000;  
    // Time to live in seconds, 7200s = 2h
    private static final int TIME_TO_LIVE_SECONDS = 7200;    
    // Time to idle in seconds, 7200s = 2h
    private static final int TIME_TO_IDLE_SECONDS = 7200;
    
	private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;
	
	public ElementCache() {
		createCache();
	}
	
	public void addObject(Object o) {
	    if(o!=null && o instanceof CnATreeElement) {
	        addObject((CnATreeElement)o);
	    } else {
            LOG.warn("Object is null or not an CnATreeElement. Will not add this to cache.");
        }
 	}
	
	public void addObject(CacheObject cacheObject) {
	    try {
            if(ElementChecker.checkNull(cacheObject)) {
                // for debug only
                boolean replaced = false;
                if (LOG.isDebugEnabled()) {
                    replaced = getCache().get(cacheObject.getElement().getUuid())!=null;
                    ElementChecker.checkParent(cacheObject.getElement());
                    ElementChecker.checkChildrenSet(cacheObject.getElement());
                }
                getCache().put(new Element(cacheObject.getElement().getUuid(), cacheObject));
                if (LOG.isInfoEnabled()) {               
                    if(replaced) {
                        LOG.info("Element replaced, uuid: " + cacheObject.getElement().getUuid() + ", has children: " + cacheObject.getHasChildren() + ", children loaded: " + cacheObject.isChildrenPropertiesLoaded() );
                    } else {
                        LOG.info("Element added, uuid: " + cacheObject.getElement().getUuid() + ", has children: " + cacheObject.getHasChildren() + ", children loaded: " + cacheObject.isChildrenPropertiesLoaded());
                    }
                    if (LOG.isDebugEnabled()) {
                        Statistics s = getCache().getStatistics();
                        LOG.info("Cache size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());                  
                    }
                }
            } else {
                ElementChecker.logIfNull(cacheObject, "Will not add this to cache.");
            }
        } catch(Throwable t) {
            LOG.error("Error while adding object",t);
        }
	}

    public void clear() {
        if (LOG.isDebugEnabled()) {
            Statistics s = getCache().getStatistics();
            LOG.debug("Size of cache before clearing, size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());
        }
		manager.clearAll();
		if (LOG.isInfoEnabled()) {
            LOG.info("Cache cleared");
        }
	}
	
	public CacheObject getCachedObject(CnATreeElement e) {
        try {
            CacheObject cacheObject = null;
            if(e!=null) {
                Element element = getCache().get(e.getUuid());
                if(element!=null) {
                    cacheObject = ((CacheObject) element.getObjectValue());            
                    if (LOG.isDebugEnabled()) {
                        if(cacheObject!=null) {
                            LOG.debug("Cache hit for uuid: " + e.getUuid() + ", has children: " + cacheObject.getHasChildren() + ", children loaded: " + cacheObject.isChildrenPropertiesLoaded());
                        } else {
                            LOG.debug("No cached element for uuid: " + e.getUuid());
                        }
                    }
                }   
            }
            return cacheObject;
        } catch(Exception t) {
            LOG.error("Error while getting object",t);
            return null;
        }
    }
	
	public void remove(CnATreeElement element) {
        try {
            remove(element.getUuid());
        } catch(Exception t) {
            LOG.error("Error while removing object",t);
        }   
    }

	public void remove(String uuid) {
	    try {
	        getCache().remove(uuid);
	        if (LOG.isInfoEnabled()) {
                LOG.info("Element removed, uuid: " + uuid);
            }
        } catch(Exception t) {
            LOG.error("Error while removing object",t);
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
        cache = new Cache(cacheId, MAX_ELEMENTS_IN_MEMORY, false, false, TIME_TO_LIVE_SECONDS, TIME_TO_IDLE_SECONDS);
        manager.addCache(cache);
        if (LOG.isInfoEnabled()) {
            LOG.info("In memory cache created. MAX_ELEMENTS_IN_MEMORY: " + MAX_ELEMENTS_IN_MEMORY + ", TIME_TO_LIVE_SECONDS: " + TIME_TO_LIVE_SECONDS + ", TIME_TO_IDLE_SECONDS: " + TIME_TO_IDLE_SECONDS + ", cacheId: " + cacheId );
        }
        return cache;
    }

    private void shutdownCache() {
        if(manager!=null && !Status.STATUS_SHUTDOWN.equals(manager.getStatus())) {
            manager.shutdown();
            if (LOG.isInfoEnabled()) {
                LOG.info("Cache shutdown." );
            }
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
