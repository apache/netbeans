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

package org.netbeans.modules.versioning.historystore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.modules.Places;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public class StorageManager {
    private static StorageManager INSTANCE;
    private final Map<String, Storage> storages;
    static final Logger LOG = Logger.getLogger(StorageManager.class.getPackage().getName());
    
    public static synchronized StorageManager getInstance () {
        if (INSTANCE == null) {
            INSTANCE = new StorageManager();
            INSTANCE.cleanUp();
        }
        return INSTANCE;
    }

    private StorageManager() {
        storages = new HashMap<String, Storage>(3);
    }
    
    /**
     * Returns or creates (if does not yet exist) a storage for the given root
     * @param root root of whatever you want to persist. This will mostly be the path to the root of a working copy or a repository url
     * @return
     */
    public Storage getStorage (String root) {
        String storeFileName = Storage.getMD5(root);
        return getStorageIntern(storeFileName);
    }
    
    private Storage getStorageIntern (String storeName) {
        synchronized (storages) {
            Storage storage = storages.get(storeName);
            if (storage == null) {
                storage = createStorage(storeName);
                storages.put(storeName, storage);
            }
            return storage;
        }
    }

    private Storage createStorage (String storeFileName) {
        File storageBaseFolder = getBaseFolder();
        File storageFolder = new File(storageBaseFolder, storeFileName);
        return new Storage(storageFolder);
    }

    private File getBaseFolder() {
        return Places.getCacheSubdirectory("vcshistory"); // NOI18N
    }

    private void cleanUp() {
        if (NbPreferences.forModule(StorageManager.class).getInt(Storage.PREF_KEY_TTL, Storage.INDEFINITE_TTL) == Storage.INDEFINITE_TTL) { //NOI18N store data indefinitely
            return;
        }
        Utils.postParallel(new Runnable() {
            @Override
            public void run() {
                LOG.log(Level.FINE, "SM.cleanUp: cleanup started"); //NOI18N
                long ts = System.currentTimeMillis();
                File baseFolder = getBaseFolder();
                File[] storageFolders = baseFolder.listFiles();
                if (storageFolders != null) {
                    for (File storageFolder : storageFolders) {
                        // cleanup every storage
                        Storage storage = getStorageIntern(storageFolder.getName());
                        storage.cleanUp();
                    }
                    synchronized (storages) {
                        // lock and eventually delete the whole cache
                        storageFolders = baseFolder.listFiles();
                        if (storageFolders != null && storageFolders.length == 0) {
                            Utils.deleteRecursively(baseFolder);
                        }
                    }
                }
                LOG.log(Level.FINE, "SM.cleanUp: cleanup complete in {0} ms", System.currentTimeMillis() - ts); //NOI18N
            }
        }, 0);
    }
}
