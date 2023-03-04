/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.parsing.lucene;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class IndexCacheFactory {
    
    private static final Logger LOG = Logger.getLogger(IndexCacheFactory.class.getName());
    private static final String PROP_CACHE_SIZE = "java.index.size";    //NOI18N
    private static final IndexCacheFactory instance = new IndexCacheFactory();
    private final RAMController ramController;
    private final LRUCache<URI, Evictable> nioCache;
    private final LRUCache<URI, Evictable> ramCache;

    private IndexCacheFactory() {
        this.ramController = new RAMController();
        this.nioCache = new LRUCache<>(new NIOPolicy());
        this.ramCache = new LRUCache<>(new RAMPolicy(ramController));
    }

    @NonNull
    LRUCache<URI,Evictable> getNIOCache() {
        return nioCache;
    }

    @NonNull
    LRUCache<URI,Evictable> getRAMCache() {
        return ramCache;
    }

    @NonNull
    RAMController getRAMController() {
        return ramController;
    }

    public static IndexCacheFactory getDefault() {
        return instance;
    }

    static final class RAMController {
        private static final float DEFAULT_CACHE_SIZE = 0.05f;
        private static final long maxCacheSize = getCacheSize();
        private final AtomicLong currentCacheSize = new AtomicLong();


        public long acquire(final long bytes) {
            return currentCacheSize.addAndGet(bytes);
        }

        public long release(final long bytes) {
            return currentCacheSize.addAndGet(~bytes + 1);
        }

        public boolean isFull() {
            return currentCacheSize.get() > maxCacheSize;
        }

        public boolean shouldLoad(final long bytes) {
            //Todo: Preffer NMAP to RAM
            return bytes < maxCacheSize;
        }

        private static long getCacheSize() {
            float per = -1.0f;
            final String propVal = System.getProperty(PROP_CACHE_SIZE);
            if (propVal != null) {
                try {
                    per = Float.parseFloat(propVal);
                } catch (NumberFormatException nfe) {
                    //Handled below
                }
            }
            if (per<0) {
                per = DEFAULT_CACHE_SIZE;
            }
            return (long) (per * Runtime.getRuntime().maxMemory());
        }
    }

    private static final class NIOPolicy implements EvictionPolicy<URI,Evictable> {
        private static final int DEFAULT_SIZE = 400;
        private static final boolean NEEDS_REMOVE =  Boolean.getBoolean("IndexCache.force") || (BaseUtilities.isUnix() && !BaseUtilities.isMac());  //NOI18N
        private static final int MAX_SIZE;
        static {
            int value = DEFAULT_SIZE;
            final String sizeStr = System.getProperty("IndexCache.size");   //NOI18N
            if (sizeStr != null) {
                try {
                    value = Integer.parseInt(sizeStr);
                } catch (NumberFormatException nfe) {
                    LOG.warning("Wrong (non integer) cache size: " + sizeStr);  //NOI18N
                }
            }            
            MAX_SIZE = value;
            LOG.fine("NEEDS_REMOVE: " + NEEDS_REMOVE +" MAX_SIZE: " + MAX_SIZE);    //NOI18N
        }

        @Override
        public boolean shouldEvict(int size, URI key, Evictable value) {
            return NEEDS_REMOVE && size>MAX_SIZE;
        }
    }

    private static final class RAMPolicy implements EvictionPolicy<URI, Evictable> {

        private final RAMController controller;

        RAMPolicy(@NonNull final RAMController controller) {
            Parameters.notNull("controller", controller);   //NOI18N
            this.controller = controller;
        }

        @Override
        public boolean shouldEvict(int size, URI key, Evictable value) {
            return controller.isFull();
        }


        
    }
}
