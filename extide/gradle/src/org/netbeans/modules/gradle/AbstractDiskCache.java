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
package org.netbeans.modules.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/**
 *
 * @author lkishalmi
 */
public abstract class AbstractDiskCache <K, T extends Serializable>  {

    private static final Logger LOG = Logger.getLogger(AbstractDiskCache.class.getName());

    protected final K key;
    private boolean valid = false;

    protected AbstractDiskCache(K key) {
        this.key = key;
    }

    public synchronized final CacheEntry<T> loadEntry() {
        File cacheFile = cacheFile();
        CacheEntry<T> ret = null;
        if (cacheFile.canRead()) {
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(cacheFile))) {
                try {
                    ret = (CacheEntry<T>) is.readObject();
                    valid = true;
                } catch (ClassNotFoundException ex) {
                    LOG.log(FINE, "Invalid cache entry.", ex);
                }
            } catch (IOException ex) {
                LOG.log(FINE, "Could no load project info from " + cacheFile, ex);
            }
        }
        return ret;

    }

    public synchronized final T loadData() {
        CacheEntry<T> entry = loadEntry();
        return entry != null && entry.isValid() ? entry.getData() : null;
    }

    public synchronized final void storeData(T data) {
        File cacheFile = cacheFile();
        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
        }
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
            os.writeObject(new CacheEntryImpl(data));
        } catch (IOException ex) {
            LOG.log(FINE, "Failed to persist info to" + cacheFile, ex);
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
            boolean ret = isCompatible();
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
    }
}
