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
package org.netbeans.modules.gradle.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import org.netbeans.modules.gradle.spi.GradleSettings;

/**
 *
 * @author lkishalmi
 */
public abstract class AbstractDiskCache <K extends Serializable, T extends Serializable> implements Serializable {

    private static final Logger LOG = Logger.getLogger(AbstractDiskCache.class.getName());

    protected K key;
    private boolean valid = false;
    private transient CacheEntry<T> entry;

    protected AbstractDiskCache() {}
    
    protected AbstractDiskCache(K key) {
        this.key = key;
    }

    public synchronized final CacheEntry<T> loadEntry() {
        CacheEntry<T> ret = entry;
        if (ret == null && !GradleSettings.getDefault().isCacheDisabled()) {
            File cacheFile = cacheFile();
            if (cacheFile.canRead()) {
                try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(cacheFile))) {
                    ret = (CacheEntry<T>) is.readObject();
                    valid = true;
                } catch (ClassNotFoundException | IOException ex) {
                    LOG.log(INFO, "Could no load project info from {0} due to: {1}", new Object[]{cacheFile, ex.getMessage()});
                    cacheFile.delete();
                }
            }
            entry = ret;
        }
        return ret;

    }

    public synchronized final T loadData() {
        CacheEntry<T> e = loadEntry();
        return e != null && e.isValid() ? e.getData() : null;
    }

    public synchronized final void storeData(T data) {
        File cacheFile = cacheFile();
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            CacheEntry e = new CacheEntryImpl(data);
            os.writeObject(e);
            entry = e;
        } catch (IOException ex) {
            LOG.log(INFO, "Failed to persist info to {0} due to {1}", new Object[]{cacheFile, ex.getMessage()});
            cacheFile.delete();
        }
    }

    public synchronized final void invalidate() {
        File cacheFile = cacheFile();
        valid = false;
        if (cacheFile.canRead()) {
            cacheFile.delete();
        }
    }
    protected abstract int cacheVersion();
    protected abstract File cacheFile();
    protected abstract Set<File> cacheInvalidators();

    public interface CacheEntry <D extends Serializable> extends Serializable {
        boolean isCompatible();
        boolean isValid();
        D getData();
    }

    private final class CacheEntryImpl <T extends Serializable> implements CacheEntry<T>  {
        int version;

        long timestamp;
        Set<File> sourceFiles;
        T data;

        protected CacheEntryImpl() {
        }

        protected CacheEntryImpl(T data) {
            timestamp = System.currentTimeMillis();
            version = cacheVersion();
            this.sourceFiles = cacheInvalidators();
            this.data = data;
        }
        
        @Override
        public boolean isCompatible() {
            return version == cacheVersion();
        }

        @Override
        public boolean isValid() {
            boolean ret = (data != null) && isCompatible();
            if (AbstractDiskCache.this.valid && ret && (sourceFiles != null)) {
                for (File f : sourceFiles) {
                    if (!f.exists() || (f.lastModified() > timestamp)) {
                        ret = false;
                        break;
                    }
                }
            }
            return ret;
        }

        @Override
        public T getData() {
            return data;
        }

        @Override
        public String toString() {
            Class dataClass = data != null ? data.getClass() : null;
            return "CacheEntryImpl{" + "data:version=" + dataClass + ":" + version + ", timestamp=" + new Date(timestamp) + ", sourceFiles=" + sourceFiles + '}';
        }
        
    }
}
