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
package org.netbeans.modules.cnd.repository.api;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.repository.impl.ShuttedDownRepositoryImpl;
import org.netbeans.modules.cnd.repository.impl.UninitializedRepositoryImpl;
import org.netbeans.modules.cnd.repository.impl.spi.LayerListener;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.impl.spi.RepositoryImplementation;
import org.netbeans.modules.cnd.repository.impl.spi.RepositoryImplementationFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.openide.util.Lookup;

/**
 *
 */
public final class Repository {

    private static final AtomicReference<RepositoryImplementation> implRef = new AtomicReference<RepositoryImplementation>(new UninitializedRepositoryImpl("Not initialized repository")); // NOI18N
    // A lock for life-cycle methods (startup/shutdown)
    private static final Object lock = new Object();
    private static int persistMechanismVersion = -1;

    public static Persistent get(Key key) {
        return implRef.get().get(key);
    }

    // Called concurrently
    public static void put(Key key, Persistent obj) {
        implRef.get().put(key, obj);
    }

    public static void remove(Key key) {
        implRef.get().remove(key);
    }

    public static void hang(Key key, Persistent obj) {
        implRef.get().hang(key, obj);
    }

    /**
     * Performs initialization of the Repository.
     *
     * It is allowed to call this method several times (with the SAME version),
     * although initialization will be done only once.
     *
     * This method blocks execution until initialization is done. Returning from
     * the method denotes that Repository is in ready-to-use state.
     *
     * @param persistMechanismVersion version of persist mechanism to use
     * @throws IllegalStateException if was called several times with different
     * persistMechanismVersion
     */
    public static void startup(int persistMechanismVersion) {
        if (persistMechanismVersion < 0) {
            throw new IllegalArgumentException();
        }
        /*
         * Use this lock to make sure that even if several threads call this
         * method in parallel then returning from the method would denote that
         * initialization is done.
         * From the other hand will do initialization only once.
         */
        synchronized (lock) {
            if (Repository.persistMechanismVersion == persistMechanismVersion) {
                return;
            }
            if (Repository.persistMechanismVersion != -1) {
                throw new IllegalStateException(
                        "Repository persistMechanismVersion is already set to " // NOI18N
                        + Repository.persistMechanismVersion);
            }
            RepositoryImplementationFactory factory = Lookup.getDefault().lookup(RepositoryImplementationFactory.class);
            implRef.set(factory.createRepository(persistMechanismVersion));
            Repository.persistMechanismVersion = persistMechanismVersion;
        }
    }

    /**
     * Once shutdown is called no further operations allowed until startup is
     * called again.
     */
    public static void shutdown() {
        synchronized (lock) {
            if (Repository.persistMechanismVersion == -1) {
                return;
            }
            try {
                RepositoryImplementation impl = implRef.get();
                impl.shutdown();
            } finally {
                Repository.persistMechanismVersion = -1;
                implRef.set(new ShuttedDownRepositoryImpl("Shutted down repository")); // NOI18N
            }
        }
    }

    public static void openUnit(int unitId) {
        implRef.get().openUnit(unitId);
        RepositoryListenersManager.getInstance().fireUnitOpenedEvent(unitId);
    }

    public static void closeUnit(int unitId, boolean cleanRepository,
            Set<Integer> requiredUnits) {
        implRef.get().closeUnit(unitId, cleanRepository, requiredUnits);
        RepositoryListenersManager.getInstance().fireUnitClosedEvent(unitId);
    }

    public static void removeUnit(int unitId) {
        implRef.get().removeUnit(unitId);
    }

    public static int getFileIdByName(int unitId, CharSequence fileName) {
        return implRef.get().getFileIdByName(unitId, fileName);
    }

    public static CharSequence getFileNameById(int unitId, int fileId) {
        return implRef.get().getFileNameById(unitId, fileId);
    }

    public static CharSequence getFileNameByIdSafe(int unitId, int fileId) {
        return implRef.get().getFileNameByIdSafe(unitId, fileId);
    }

    /**
     *
     * @param unitId
     * @return <code>null</code> if wrong/non existent unitId.
     */
    public static CharSequence getUnitName(int unitId) {
        return implRef.get().getUnitName(unitId);
    }

    // Called concurrently
    public static int getUnitIdForStorage(UnitDescriptor unitDescriptor, int storageID) {
        return implRef.get().getUnitID(unitDescriptor, storageID);
    }

    // Called concurrently
    public static int getUnitId(UnitDescriptor unitDescriptor) {
        return implRef.get().getUnitID(unitDescriptor);
    }
    
    public static void registerRepositoryListener(RepositoryListener listener) {
        RepositoryListenersManager.getInstance().registerListener(listener);
    }    
              
    public static void unregisterRepositoryListener(RepositoryListener listener) {
        RepositoryListenersManager.getInstance().unregisterListener(listener);
    }

    public static void addRepositoryExceptionListener(RepositoryExceptionListener listener) {
        RepositoryExceptions.addRepositoryExceptionListener(listener);
    }

    public static void removeRepositoryExceptionListener(RepositoryExceptionListener listener) {
        RepositoryExceptions.removeRepositoryExceptionListener(listener);
    }

    public static LayeringSupport getLayeringSupport(int sourceUnitId) {
        return implRef.get().getLayeringSupport(sourceUnitId);
    }
}
