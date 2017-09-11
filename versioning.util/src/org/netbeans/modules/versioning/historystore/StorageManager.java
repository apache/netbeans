/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
