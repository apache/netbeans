/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.uid;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities.CachedUID;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.WeakSet;

/**
 *
 */
public class UIDManager {

    private final UIDStorage storage;
    private static final int UID_MANAGER_DEFAULT_CAPACITY;
    private static final int UID_MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            UID_MANAGER_DEFAULT_SLICED_NUMBER = 32;
            UID_MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            UID_MANAGER_DEFAULT_SLICED_NUMBER = 128;
            UID_MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final UIDManager instance = new UIDManager();

    /** Creates a new instance of UIDManager */
    private UIDManager() {
        storage = new UIDStorage(UID_MANAGER_DEFAULT_SLICED_NUMBER, UID_MANAGER_DEFAULT_CAPACITY);
    }

    public static UIDManager instance() {
        return instance;
    }
    private static final class Lock {}
    private final Object lock = new Lock();

    /**
     * returns shared uid instance equal to input one.
     *
     * @param uid - interested shared uid
     * @return the shared instance of uid
     * @exception NullPointerException If the <code>uid</code> parameter
     *                                 is <code>null</code>.
     */
    public final <T> CsmUID<T> getSharedUID(CsmUID<T> uid) {
        if (uid == null) {
            throw new NullPointerException("null string is illegal to share"); // NOI18N
        }
        CsmUID<T> outUID;
        synchronized (lock) {
            outUID = storage.getSharedUID(uid);
        }
        assert (outUID != null);
        assert (outUID.equals(uid));
        return outUID;
    }

    public final void dispose() {
        synchronized (lock) {
            storage.dispose();
        }
    }

    public final void clearProjectCache(Key key) {
        int projectIndex = KeyUtilities.getProjectIndex(key);
        synchronized (lock) {
            storage.clearCache(projectIndex);
        }
    }

    private static final class UIDStorage {

        private final WeakSet<CsmUID<?>>[] instances;
        private final int segmentMask; // mask
        private final int initialCapacity;

        private UIDStorage(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            this.initialCapacity = initialCapacity;
            @SuppressWarnings("unchecked")
            WeakSet<CsmUID<?>>[] ar = new WeakSet[ssize];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = new WeakSet<>(initialCapacity);
            }
            instances = ar;
        }

        private WeakSet<CsmUID<?>> getDelegate(CsmUID<?> uid) {
            int index = uid.hashCode() & segmentMask;
            return instances[index];
        }

        @SuppressWarnings("unchecked")
        private <T> CsmUID<T> getSharedUID(CsmUID<T> uid) {
            return (CsmUID<T>) getDelegate(uid).putIfAbsent(uid);
        }

        private void clearCache(int projectIndex) {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    Object[] arr = instances[i].toArray();
                    for (Object o : arr) {
                        if (o instanceof CachedUID<?>) {
                            CachedUID<?> cached = (CachedUID<?>) o;
                            if (o instanceof KeyBasedUID<?>) {
                                Key k = ((KeyBasedUID<?>)o).getKey();
                                if (projectIndex == KeyUtilities.getProjectIndex(k)) {
                                    cached.clear();
                                }
                            }
                        }
                    }
                }
            }
        }

        private void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS) {
                        Object[] arr = instances[i].toArray();
                        System.out.println("Dispose UID cache " + instances[i].size()); // NOI18N
                        Map<Class, Integer> uidClasses = new HashMap<>();
                        Map<Class, Integer> keyClasses = new HashMap<>();
                        for (Object o : arr) {
                            if (o != null) {
                                incCounter( uidClasses, o);
                                if (o instanceof KeyBasedUID<?>) {
                                    Key k = ((KeyBasedUID<?>)o).getKey();
                                    incCounter( keyClasses, k);
                                }
                            }
                        }
                        for (Map.Entry<Class, Integer> e : uidClasses.entrySet()) {
                            System.out.println("   " + e.getValue() + " of " + e.getKey().getName()); // NOI18N
                        }
                        System.out.println("-----------"); // NOI18N
                        for (Map.Entry<Class, Integer> e : keyClasses.entrySet()) {
                            System.out.println("   " + e.getValue() + " of " + e.getKey().getName()); // NOI18N
                        }
                    }
                    instances[i].clear();
                    instances[i].resize(initialCapacity);
                }
            }
        }

        private void incCounter(Map<Class, Integer> uidClasses, Object o) {
            Integer num = uidClasses.get(o.getClass());
            if (num != null) {
                num = Integer.valueOf(num.intValue() + 1);
            } else {
                num = Integer.valueOf(1);
            }
            uidClasses.put(o.getClass(), num);
        }
    }
}
