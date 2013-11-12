/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.service;

import java.io.File;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class JobCache {
    
    private static final Logger LOG = Logger.getLogger(JobCache.class);
    
    private static final String CONF_FILE_NAME = "job.ehcache.xml";
    
    /*
     * Configuration parameter
     */  
    private static final String cacheId = "sernet.verinice.service.JobCache";
    private static final int MAX_ELEMENTS_IN_MEMORY = 500;      
    private static final int TIME_TO_LIVE_SECONDS = 0; // 0 mean infinite
    private static final int TIME_TO_IDLE_SECONDS = 0; // 0 mean infinite  
    private static final int DISK_EXPIRY_THREAD_INTERVAL_SECONDS = 720;    
    private static final String DISK_STORE_PATH;   
    static {
        String tempDir = System.getProperty("java.io.tmpdir");
        DISK_STORE_PATH = new StringBuilder().append(tempDir).append(File.separatorChar).append(cacheId).append(File.separatorChar).toString();
    }
    
    private transient CacheManager manager = null;
    
    private transient Cache cache = null;
    
    public JobCache() {
        super();
    }
    

    public void put(String key, Object value) {
        getCache().put(new Element(key, value));  
        getCache().flush();
    }
    
    public Object get(String key) {
        Object result = null;
        try {    
            Element cachedElement = getCache().get(key);
            if(cachedElement!=null) {
                result = cachedElement.getObjectValue();              
            }          
        } catch(Exception t) {
            LOG.error("Error while getting object, key: " + key, t);
        } 
        return result;
    }
    
    public void remove(String key) {
        getCache().remove(key);       
    }

    private Cache getCache() {     
        if(cache==null) {
            cache = loadCache();
        }
        return cache;
    }
    
    private Cache loadCache() {
        cache = getManager().getCache(cacheId);
        if(cache==null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache not found in configuration. Creating new cache now.");
            }
            cache = createCache(); 
        }
        if(!Status.STATUS_ALIVE.equals(cache.getStatus())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache is not alive is re-created now.");
            }
            getManager().removeCache(cacheId);
            cache = createCache(); 
        }
        if (LOG.isDebugEnabled()) {
            Statistics s = cache.getStatistics();
            LOG.debug("Cache loaded, size: " + s.getObjectCount() + ", hits: " + s.getCacheHits());
        }
        return cache;
    }
      
    private Cache createCache() {
        cache = new Cache(
                cacheId, 
                MAX_ELEMENTS_IN_MEMORY, 
                null,
                true,
                DISK_STORE_PATH,
                true, 
                TIME_TO_LIVE_SECONDS, 
                TIME_TO_IDLE_SECONDS, 
                true, 
                DISK_EXPIRY_THREAD_INTERVAL_SECONDS,
                null);
        getManager().addCache(cache);
        if (LOG.isInfoEnabled()) {
            LOG.info("Cache created. MAX_ELEMENTS_IN_MEMORY: " + MAX_ELEMENTS_IN_MEMORY + ", TIME_TO_LIVE_SECONDS: " + TIME_TO_LIVE_SECONDS + ", TIME_TO_IDLE_SECONDS: " + TIME_TO_IDLE_SECONDS + ", cacheId: " + cacheId );
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
    
    private CacheManager getManager() {
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus())) {
            createManager();
        }
        return manager;
    }
    
    private CacheManager createManager() {
        shutdownCache();
        URL url = this.getClass().getResource(CONF_FILE_NAME);
        manager = CacheManager.create(url);
        if (LOG.isDebugEnabled()) {
            LOG.debug("CacheManager created, configuration file: " + this.getClass().getPackage().getName() + File.separatorChar + CONF_FILE_NAME);
        }
        return manager;
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
