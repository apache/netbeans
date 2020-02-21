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

package org.netbeans.modules.cnd.utils.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.WeakSet;


/**
 * APT string table manager
 * Responsibility:
 *  - only one instance per String object
 *  - based on weak references to allow GC of unused strings
 * 
 */
public abstract class APTStringManager  {
    public enum CacheKind {
        Single,
        Sliced
    }
    
    public abstract CharSequence getString(CharSequence text);
    public abstract void dispose();

    private static final Map<String, APTStringManager> instances = Collections.synchronizedMap(new HashMap<String, APTStringManager>());

    private static final int STRING_MANAGER_DEFAULT_CAPACITY;
    private static final int STRING_MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            STRING_MANAGER_DEFAULT_SLICED_NUMBER = 32;
            STRING_MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            STRING_MANAGER_DEFAULT_SLICED_NUMBER = 128;
            STRING_MANAGER_DEFAULT_CAPACITY = 128;
        }
    }

    /*package*/ static final String TEXT_MANAGER="Manager of sharable texts"; // NOI18N
    /*package*/ static final int    TEXT_MANAGER_INITIAL_CAPACITY=STRING_MANAGER_DEFAULT_CAPACITY;
    /*package*/ static final String FILE_PATH_MANAGER="Manager of sharable file paths"; // NOI18N
    /*package*/ static final int    FILE_PATH_MANAGER_INITIAL_CAPACITY=STRING_MANAGER_DEFAULT_CAPACITY;
    
    public static APTStringManager instance(String name, CacheKind kind) {
        switch (kind){
            case Single:
                return instance(name, STRING_MANAGER_DEFAULT_CAPACITY);
            case Sliced:
                return instance(name, STRING_MANAGER_DEFAULT_SLICED_NUMBER, STRING_MANAGER_DEFAULT_CAPACITY);
        }
        throw new java.lang.IllegalArgumentException();
    }

    private static APTStringManager instance(String name, int initialCapacity) {
        APTStringManager instance = instances.get(name);
        if (instance == null) {
            instance = new APTSingleStringManager(name, initialCapacity);
            instances.put(name, instance);
        }
        return instance;
    }  

    private static APTStringManager instance(String name, int sliceNumber, int initialCapacity) {
        APTStringManager instance = instances.get(name);
        if (instance == null) {
            instance = new APTCompoundStringManager(name, sliceNumber, initialCapacity);
            instances.put(name, instance);
        }
        return instance;
    }  

    /*package*/ static final class APTSingleStringManager extends APTStringManager {
        private final WeakSet<CharSequence> storage;
        private final int initialCapacity;
        // To gebug
        private final String name;

        /** Creates a new instance of APTStringManager */
        private APTSingleStringManager(String name, int initialCapacity) {
            storage = new WeakSet<CharSequence>(initialCapacity);
            this.initialCapacity = initialCapacity;
            // To gebug
            this.name = name;
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
        public final CharSequence getString(CharSequence text) {
            if (text == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }
            CharSequence outText = null;

            synchronized (lock) {
                outText = storage.putIfAbsent(text);
            }
            assert (outText != null);
            assert (outText.equals(text));
            return outText;
        }

        @Override
        public final void dispose() {
            if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS){
                Object[] arr;
                synchronized (lock) {
                    arr = storage.toArray();
                }
                System.out.println("Dispose cache "+name+" "+arr.length + " " + getClass().getName()); // NOI18N
                Map<Class, Integer> classes = new HashMap<Class,Integer>();
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
                for(Map.Entry<Class,Integer> e:classes.entrySet()){
                    System.out.println("   "+e.getValue()+" of "+e.getKey().getName()); // NOI18N
                }
            }
            if (storage.size() > 0) {
                storage.clear();
                storage.resize(initialCapacity);
            }
        }
    }
    
    /*package*/ static final class APTCompoundStringManager extends APTStringManager {
        private final APTStringManager[] instances;
//        private final int sliceNumber; // primary number for better distribution
        private final int segmentMask; // mask
        // To gebug
        private final String name;
        /*package*/APTCompoundStringManager(String name, int sliceNumber) {
            this(name, sliceNumber, APTStringManager.TEXT_MANAGER_INITIAL_CAPACITY);
        }
        /*package*/APTCompoundStringManager(String name, int sliceNumber, int initialCapacity) {
//            this.sliceNumber = sliceNumber;
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            instances = new APTStringManager[ssize];
            for (int i = 0; i < instances.length; i++) {
                instances[i] = new APTSingleStringManager(name, initialCapacity);
            }
            this.name = name;
        }
        
        private APTStringManager getDelegate(CharSequence text) {
            if (text == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }            
            int index = text.hashCode() & segmentMask;
//            if (index < 0) {
//                index += sliceNumber;
//            }
            return instances[index];
        }
        
        @Override
        public final CharSequence getString(CharSequence text) {
            return getDelegate(text).getString(text);
        }

        @Override
        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                instances[i].dispose();
            }            
        }        
    }    
}
