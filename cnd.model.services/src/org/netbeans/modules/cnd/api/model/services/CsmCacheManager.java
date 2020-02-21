/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.api.model.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Exceptions;

/**
 * code model caches to be used in the same thread by (possibly different)
 * clients. Clients enter and leave caching activity. During active caching
 * transaction cache storage accumulates all entries created by clients. On
 * release all cache entries are disposed. Transactions can be nested, in this
 * case only the most top level release force dispose of cached values. It
 * allows sibling code to communicate and share results.
 *
 * Think about transaction granularity, because it might be memory consuming to
 * wrap unrelated activity under the same transaction. Normal usage would be:
 * start caching transaction before proceeding i.e. references in one file.
 * <pre>
 * try {
 *   CsmCacheManager.enter();
 *   ...
 *   // work i.e. with file references resolving
 *   ...
 * } finally {
 *   CsmCacheManager.leave();
 * }
 * </pre>
 *
 * There are several ways to access cached information including shortcut help
 * methods to get/put into default cache.
 * <pre>
 * CsmCacheMap.Value value = CsmCacheManager.getValue(key);
 * if (value != null) {
 *  result = value.getResult();
 * } else {
 *  long time = System.currentTimeMillis();
 *  ... calculate
 *  time = System.currentTimeMillis() - time;
 *  CsmCacheMap.Value newValue = CsmCacheMap.toValue(cachedResult, time, traceName);
 *  CsmCacheMap.Value prevValue = CsmCacheManager.putValue(key, newValue);
 * }
 * </pre>
 *
 */
public final class CsmCacheManager {

    /**
     * enter caching activity (or increase level)
     */
    public static void enter() {
        storagesPool.get().enterImpl();
    }

    /**
     * leave caching
     */
    public static void leave() {
        storagesPool.get().leaveImpl();
    }

    /**
     * checks if cache transaction was started for the current thread.
     *
     * @return true if cache is active, false otherwise.
     */
    public static boolean isActive() {
        return storagesPool.get().isActive();
    }

    /**
     * Query for cached value in shared map-based cache.
     *
     * @param key key in shared map-based cache
     * @return If no cache transaction were started then null is returned. If
     * cache transaction is active, then value associated with key if any.
     * @see getCacheMap#getMapBasedCache
     */
    public static Object get(@NonNull Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        CsmCacheMap map = getSharedCache();
        if (map == null) {
            return null;
        }
        CsmCacheMap.Value value = map.get(key);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "getValue {1}->{2}\n", new Object[]{key, value});
        }
        return value != null ? value.getResult() : null;
    }

    /**
     * cache the value if has active cache transaction, no-op otherwise.
     *
     * @param key key
     * @param value value to put in shared map-based cache
     * @return If no cache transaction were started then null is returned and
     * nothing is put into cache. If cache transaction is active, then returns
     * previous value associated with key if any.
     */
    public static Object put(@NonNull Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        CsmCacheMap map = getSharedCache();
        if (map == null) {
            return null;
        }
        CsmCacheMap.Value prev = map.put(key, CsmCacheMap.toValue(value, Integer.MAX_VALUE));
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "putValue {1}->{2} (replaced {3})\n", new Object[] {key, value, prev});
        }
        return prev != null ? prev.getResult() : null;
    }

    /**
     * If cache transaction is started returns shared map-based cache for key, null
     * otherwise
     *
     * @return map-based cache for key if cache transaction is started, null
     * otherwise
     */
    public static CsmCacheMap getSharedCache() {
        return (CsmCacheMap) storagesPool.get().getEntry(CsmCacheStorage.class, INIT_AS_MAP);
    }

    /**
     * If cache transaction is started returns custom cache entry for key, null
     * otherwise.
     *
     * @param <T> custom cache entry class
     * @param entryKey custom cache entry key
     * @param init initializer to create entry for key if it is absent in cache,
     * or null if only interested in existing entries
     * @return custom cache entry for key if cache transaction is started, null
     * otherwise
     */
    public static <T extends CsmClientCache> T getClientCache(@NonNull Object entryKey, Callable<T> init) {
        @SuppressWarnings("unchecked")
        T entry = (T)storagesPool.get().getEntry(entryKey, init);
        return entry;
    }

    /**
     * interface for custom entries in cache storage
     */
    public interface CsmClientCache {

        /**
         * called when cache storage is disposed and cache entry is removed
         */
        public void cleanup();
    }

    private static final Callable<CsmCacheMap> INIT_AS_MAP = new Callable<CsmCacheMap>() {

        @Override
        public CsmCacheMap call() {
            return new CsmCacheMap(CsmCacheStorage.class.getSimpleName());
        }

    };

    //<editor-fold defaultstate="collapsed" desc="implementation">
    static final Logger LOGGER = Logger.getLogger(CsmCacheManager.class.getSimpleName());

    private CsmCacheManager() {
    }

    private static final ThreadLocal<CsmCacheStorage> storagesPool = new ThreadLocal<CsmCacheStorage>() {

        @Override
        protected CsmCacheStorage initialValue() {
            return new CsmCacheStorage();
        }
    };

    private final static class CsmCacheStorage {

        private final Map<Object, CsmClientCache> cacheEntries = new HashMap<Object, CsmClientCache>();
        private int activeReferences;
        private long initTime;
        private Exception initStack;
        private Exception releasedStack;
        
        CsmCacheStorage() {
            activeReferences = 0;
        }

        void enterImpl() {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "CsmCacheStorage: Enter {0}:[{1}]{2}\n", new Object[] {activeReferences, Thread.currentThread().getId(), Thread.currentThread().getName()});
            }
            if (activeReferences == 0) {
                initTime = System.currentTimeMillis();
                if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                    initStack = new Exception("Created for " + Thread.currentThread().getName()); // NOI18N
                }
            }
            activeReferences++;
        }

        void leaveImpl() {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "CsmCacheStorage: Leave {0}:[{1}]{3}\n", new Object[]{activeReferences, Thread.currentThread().getId(), Thread.currentThread().getName()});
            }
            if (activeReferences == 0) {
                traceError();
                return;
            }
            if (--activeReferences == 0) {
                initTime = System.currentTimeMillis() - initTime;
                if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                    releasedStack = new Exception("Released for " + Thread.currentThread().getName()); // NOI18N
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "CsmCacheStorage: Used {0}ms; Dispose {1}:[{2}]{3}\n", new Object[]{initTime, cacheEntries.size(), Thread.currentThread().getId(), Thread.currentThread().getName()});
                }
                // release all entries
                for (CsmClientCache entry : cacheEntries.values()) {
                    entry.cleanup();
                }
                cacheEntries.clear();
            }
        }

        /**
         * checks if cache transaction was started for the current thread.
         *
         * @return true if cache is active, false otherwise.
         */
        boolean isActive() {
            if (!CndTraceFlags.USE_CSM_CACHE) {
                return false;
            }
            return activeReferences > 0;
        }

        /**
         * get entry for key (or create new one if absent and initializer is
         * provided).
         *
         * @param <T> cache entry class
         * @param entryKey key of interested entry
         * @param init initializer to create entry for key if it is absent in
         * cache, or null if only interested in existing entries
         * @return entry for key
         */
        CsmClientCache getEntry(@NonNull Object entryKey, Callable<? extends CsmClientCache> init) {
            if (!CndTraceFlags.USE_CSM_CACHE) {
                return null;
            }
            if (activeReferences == 0) {
                CndUtils.printStackTraceOnce(new Exception("no any active cache transaction:" + entryKey + ": use CsmCacheManager.enter(); try { } finally { CsmCacheManager.leave(); }")); // NOI18N
                return null;
            }
            CsmClientCache out = cacheEntries.get(entryKey);
            if (out == null && init != null) {
                try {
                    out = init.call();
                    cacheEntries.put(entryKey, out);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return out;
        }

        private void traceError() {
            if (releasedStack != null) {
                LOGGER.log(Level.INFO, "Unexpected Release:\n{0}\n", new Exception());
                LOGGER.log(Level.INFO, "Already Released:\n{0}\n", releasedStack);
                LOGGER.log(Level.INFO, "Was created at:\n{0}\n", initStack);
            } else {
                LOGGER.log(Level.WARNING, "Unexpected Release:\n{0}\n", new Exception());
            }
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append("activeReferences=").append(activeReferences).append("\n"); // NOI18N
            for (Map.Entry<Object, CsmClientCache> entry : cacheEntries.entrySet()) {
                out.append(entry.getKey()).append("=>\n{").append(entry.getValue()).append("}\n"); // NOI18N
            }
            return out.toString();
        }
        
        
    }
//</editor-fold>
}
