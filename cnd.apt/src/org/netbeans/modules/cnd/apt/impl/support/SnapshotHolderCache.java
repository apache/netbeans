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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.utils.cache.MapSnapshot.Holder;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.WeakSet;


/**
 * Cache to share Holder objects (internal part of frozen APTMacroMapSnapshot)
 * Responsibility:
 *  - only one instance containing the same set of macro objects
 *  - based on weak references to allow GC of unused instances
 *
 */
public abstract class SnapshotHolderCache  {
    public enum CacheKind {
        Single,
        Sliced
    }

    private SnapshotHolderCache() {
    }

    abstract Holder getHolder(Holder arr);
    public abstract void dispose();

    private static final int MACRO_MANAGER_DEFAULT_CAPACITY;
    private static final int MACRO_MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            MACRO_MANAGER_DEFAULT_SLICED_NUMBER = 32;
            MACRO_MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            MACRO_MANAGER_DEFAULT_SLICED_NUMBER = 128;
            MACRO_MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final SnapshotHolderCache instance = create(false);

    private static SnapshotHolderCache create(boolean single) {
        if (single) {
            return new APTSingleHolderManager(MACRO_MANAGER_DEFAULT_CAPACITY);
        } else {
            return new APTCompoundHolderManager(MACRO_MANAGER_DEFAULT_SLICED_NUMBER, MACRO_MANAGER_DEFAULT_CAPACITY);
        }
    }

    public static SnapshotHolderCache getManager() {
        return instance;
    }

    private static final class APTSingleHolderManager extends SnapshotHolderCache {
        private final WeakSet<Holder> storage;
        private final int initialCapacity;

        /** Creates a new instance of HolderCache */
        private APTSingleHolderManager(int initialCapacity) {
            storage = new WeakSet<Holder>(initialCapacity);
            this.initialCapacity = initialCapacity;
        }

        private static final class Lock {}
        private final Object lock = new Lock();

        /**
         * returns shared string instance equal to input text.
         *
         * @param test - interested shared string
         * @return the shared instance of text
         * @exception NullPointerException If the <code>text</code> parameter
         *                                 is <code>null</code>.
         */
        @Override
        public Holder getHolder(Holder arr) {
            if (arr == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }
            Holder outHolder = null;

            synchronized (lock) {
                outHolder = storage.putIfAbsent(arr);
            }
            assert (outHolder != null);
            assert (outHolder.equals(arr));
            return outHolder;
        }

        @Override
        public final void dispose() {
            if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS) {
                Object[] arr = storage.toArray();
                System.out.println("Dispose macro cache "+arr.length + " " + getClass().getName()); // NOI18N
                Map<Class<?>, Integer> classes = new HashMap<Class<?>,Integer>();
                for(Object o : arr){
                    if (o != null) {
                        Integer i = classes.get(o.getClass());
                        if (i != null) {
                            i = Integer.valueOf(i.intValue() + 1);
                        } else {
                            i = Integer.valueOf(1);
                        }
                        classes.put(o.getClass(), i);
                    }
                }
                for(Map.Entry<Class<?>,Integer> e:classes.entrySet()){
                    System.out.println("   "+e.getValue()+" of "+e.getKey().getName()); // NOI18N
                }
            }
            if (storage.size() > 0) {
                storage.clear();
                storage.resize(initialCapacity);
            }
        }
    }

    private static final class APTCompoundHolderManager extends SnapshotHolderCache {
        private final SnapshotHolderCache[] instances;
//        private final int sliceNumber; // primary number for better distribution
        private final int segmentMask; // mask
        private APTCompoundHolderManager(int sliceNumber) {
            this(sliceNumber, SnapshotHolderCache.MACRO_MANAGER_DEFAULT_CAPACITY);
        }
        private APTCompoundHolderManager(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            instances = new SnapshotHolderCache[ssize];
            for (int i = 0; i < instances.length; i++) {
                instances[i] = new APTSingleHolderManager(initialCapacity);
            }
        }

        private SnapshotHolderCache getDelegate(Holder macro) {
            if (macro == null) {
                throw new NullPointerException("null macro is illegal to share"); // NOI18N
            }
            int index = macro.hashCode() & segmentMask;
            return instances[index];
        }

        @Override
        public Holder getHolder(Holder macro) {
            return getDelegate(macro).getHolder(macro);
        }

        @Override
        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                instances[i].dispose();
            }
        }
    }
}
