/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
