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
package org.netbeans.modules.gradle.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;

/**
 *
 * @author lkishalmi
 */
public abstract class AbstractDiskCache<K, T extends Serializable> {

    private static final Logger LOG = Logger.getLogger(AbstractDiskCache.class.getName());
    protected K key;
    private WeakReference<CacheEntry<T>> entryRef;

    protected AbstractDiskCache() {}
    
    protected AbstractDiskCache(K key) {
        this.key = key;
    }

    public final synchronized T loadData() {
        CacheEntry<T> e = loadEntry();
        return e != null ? e.getData() : null;
    }

    public final boolean isCompatible() {
        CacheEntry<T> e = loadEntry();
        return e != null && e.isCompatible(this);        
    }

    public final boolean isValid() {
        CacheEntry<T> e = loadEntry();
        return e != null && e.isValid(this);        
    }
    
    public final synchronized void storeData(T data) {
        CacheEntry<T> entry = new CacheEntry<>(this, data);
        if (doStoreEntry(entry)) {
            entryRef = new WeakReference<>(entry);        
        }
    }

    public final synchronized void invalidate() {
        entryRef = null;
        File cacheFile = cacheFile();
        if (cacheFile.canRead()) {
            cacheFile.delete();
        }
    }

    protected final synchronized CacheEntry<T> loadEntry() {
        CacheEntry<T> ret = entryRef != null ? entryRef.get() : null;
        if (ret == null && !GradleExperimentalSettings.getDefault().isCacheDisabled()) {
            ret = doLoadEntry();
            entryRef = ret != null ? new WeakReference<>(ret) : null;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    protected CacheEntry<T> doLoadEntry() {
        CacheEntry<T> ret = null;
        File cacheFile = cacheFile();
        if (cacheFile.canRead()) {
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(cacheFile))) {
                ret = (CacheEntry<T>) is.readObject();
            } catch (ClassNotFoundException | IOException ex) {
                LOG.log(INFO, "Could no load project info from {0} due to: {1}", new Object[]{cacheFile, ex.getMessage()});
                cacheFile.delete();
            }
        }
        return ret;        
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected boolean doStoreEntry(CacheEntry<T> entry) {
        File cacheFile = cacheFile();
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            os.writeObject(entry);
            return true;
        } catch (IOException ex) {
            LOG.log(INFO, "Failed to persist info to {0} due to {1}", new Object[]{cacheFile, ex.getMessage()});
            cacheFile.delete();
        }
        return false;
    }
    
    protected abstract int cacheVersion();
    protected abstract File cacheFile();
    protected abstract Set<File> cacheInvalidators();

    public static final class CacheEntry <T extends Serializable> implements Serializable  {
        static final long serialVersionUID = 1L;
        
        int version;

        long timestamp;
        Set<File> sourceFiles;
        T data;

        protected CacheEntry() {
        }

        protected CacheEntry(AbstractDiskCache<?, T> cache, T data) {
            timestamp = System.currentTimeMillis();
            version = cache.cacheVersion();
            this.sourceFiles = cache.cacheInvalidators();
            this.data = data;
        }
        
        public boolean isCompatible(AbstractDiskCache<?, T> cache) {
            return version == cache.cacheVersion();
        }

        public boolean isValid(AbstractDiskCache<?, T> cache) {
            boolean ret = (data != null) && isCompatible(cache);
            if (ret && (sourceFiles != null)) {
                for (File f : sourceFiles) {
                    if (!f.exists() || (f.lastModified() > timestamp)) {
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            Class<?> dataClass = data != null ? data.getClass() : null;
            return "CacheEntryImpl{" + "data:version=" + dataClass + ":" + version + ", timestamp=" + new Date(timestamp) + ", sourceFiles=" + sourceFiles + '}';
        }
    }
}
