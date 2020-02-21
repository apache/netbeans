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
package org.netbeans.modules.cnd.indexing.impl;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(path=LayerListener.PATH, service=org.netbeans.modules.cnd.repository.impl.spi.LayerListener.class)
public final class TextIndexStorageManager implements LayerListener{

    public static final String FIELD_IDS = "ids"; // NOI18N
    public static final String FIELD_UNIT_ID = "unitId"; // NOI18N
    private static final Object lock = new Object();
    // storageID <-> storage
    private static final Map<Integer, TextIndexStorage> storages = new HashMap<Integer, TextIndexStorage>();
    

    @OnStart
    public static class Startup implements Runnable, RepositoryListener {
        @Override
        public void run() {
            Repository.registerRepositoryListener(this);
        } 

        @Override
        public boolean unitOpened(int unitId) {
            return true;
        }

        @Override
        public void unitRemoved(int unitId) {
            if (unitId < 0) {
                return;
            }
            synchronized (lock) {
                TextIndexStorage index = TextIndexStorageManager.get(unitId);
                if (index != null) {
                    index.unitRemoved(unitId);
                }
            }            
            
        }                 

        @Override
        public void unitClosed(int unitId) {
        }
    }    

    @OnStop
    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            shutdown();
        }
    }

    public static TextIndexStorage get(int unitID) {
        synchronized (lock) {
            LayeringSupport ls = Repository.getLayeringSupport(unitID);
            if (ls == null) {
                return null;
            }
            Integer storageID = Integer.valueOf(ls.getStorageID());
            TextIndexStorage storage;
            synchronized (storages) {
                storage = storages.get(storageID);
                if (storage == null) {
                    storage = new TextIndexStorage(ls);
                    storages.put(storageID, storage);
                }
            }
            return storage;
        } 
    }

    @Override
    public boolean layerOpened(LayerDescriptor layerDescriptor) {
        boolean isOK = true;
        synchronized (storages) {
            for (TextIndexStorage textIndexStorage : storages.values()) {
                isOK &= textIndexStorage.isValid();
            }
        }
        return isOK;

    }
    
    

    public static void shutdown() {
        synchronized (storages) {
            for (TextIndexStorage storage : storages.values()) {
                storage.shutdown();
            }
            storages.clear();
        }
    }
    
}
