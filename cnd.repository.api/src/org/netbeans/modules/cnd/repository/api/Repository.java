/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
