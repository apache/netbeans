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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.WeakSet;


/**
 * APT macro table manager
 * Responsibility:
 *  - only one instance per macro object
 *  - based on weak references to allow GC of unused macros
 *
 */
public abstract class APTMacroCache  {
    public enum CacheKind {
        Single,
        Sliced
    }

    private APTMacroCache() {
    }

    public abstract APTMacro getMacro(APTMacro macro);
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
    private static final APTMacroCache instance = create(false);

    private static APTMacroCache create(boolean single) {
        if (single) {
            return new APTSingleMacroManager(MACRO_MANAGER_DEFAULT_CAPACITY);
        } else {
            return new APTCompoundMacroManager(MACRO_MANAGER_DEFAULT_SLICED_NUMBER, MACRO_MANAGER_DEFAULT_CAPACITY);
        }
    }

    public static APTMacroCache getManager() {
        return instance;
    }
    
    private static final class APTSingleMacroManager extends APTMacroCache {
        private final WeakSet<APTMacro> storage;
        private final int initialCapacity;

        /** Creates a new instance of APTMacroCache */
        private APTSingleMacroManager(int initialCapacity) {
            storage = new WeakSet<APTMacro>(initialCapacity);
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
        public APTMacro getMacro(APTMacro macro) {
            if (macro == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }
            APTMacro outMacro = null;

            synchronized (lock) {
                outMacro = storage.putIfAbsent(macro);
            }
            assert (outMacro != null);
            assert (outMacro.equals(macro));
            return outMacro;
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

    private static final class APTCompoundMacroManager extends APTMacroCache {
        private final APTMacroCache[] instances;
//        private final int sliceNumber; // primary number for better distribution
        private final int segmentMask; // mask
        private APTCompoundMacroManager(int sliceNumber) {
            this(sliceNumber, APTMacroCache.MACRO_MANAGER_DEFAULT_CAPACITY);
        }
        private APTCompoundMacroManager(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            instances = new APTMacroCache[ssize];
            for (int i = 0; i < instances.length; i++) {
                instances[i] = new APTSingleMacroManager(initialCapacity);
            }
        }

        private APTMacroCache getDelegate(APTMacro macro) {
            if (macro == null) {
                throw new NullPointerException("null macro is illegal to share"); // NOI18N
            }
            int index = macro.hashCode() & segmentMask;
            return instances[index];
        }

        @Override
        public APTMacro getMacro(APTMacro macro) {
            return getDelegate(macro).getMacro(macro);
        }

        @Override
        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                instances[i].dispose();
            }
        }
    }
}
