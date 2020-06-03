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

/**
 *
 */
public final class TinyMaps {
    private static final Map<?,?> NO_CONTENT = Collections.emptyMap();
    // marker interface for optimized maps
    interface CompactMap<K, V> {
        public Map<K, V> expandForNextKeyIfNeeded(K newElem);
    };

    private TinyMaps() {
    }

    public static <K, V> Map<K, V> createMap(int initialCapacity) {
        switch (initialCapacity) {
            case 0:
                @SuppressWarnings("unchecked")
                Map<K, V> out = (Map<K, V>) NO_CONTENT;
                return out;
            case 1:
                return new TinySingletonMap<K, V>();
            case 2:
                return new TinyTwoValuesMap<K, V>();
            case 3:
            case 4:
                return new TinyMap4<K, V>();
            case 5:
            case 6:
                return new TinyMap6<K, V>();
            case 7:
            case 8:
                return new TinyMap8<K, V>();
        }
        if (initialCapacity <= 16) {
            return new TinyMap16<K, V>();
        } else if (initialCapacity <= 32) {
            return new TinyHashMap32<K, V>();
        } else if (initialCapacity <= 64) {
            return new TinyHashMap64<K, V>();
        } else if (initialCapacity <= 128) {
            return new TinyHashMap128<K, V>();
        }
        return new TinyHashMap<K, V>(initialCapacity);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> expandForNextKey(Map<K, V> orig, K newElem) {
        if (orig instanceof CompactMap<?, ?>) {
            return ((CompactMap<K, V>)orig).expandForNextKeyIfNeeded(newElem);
        } else if (orig == NO_CONTENT) {
            return createMap(1);
        } else {
            return orig;
        }
    }

    static final class TinyMap4<K, V> extends TinyArrayMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyMap4() {
            super(4);
        }

        TinyMap4(TinyTwoValuesMap<K, V> twoValues) {
            super(twoValues, 4);
        }

        TinyMap4(Map<K, V> twoValues) {
            super(4, twoValues);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() <= 3 || containsKey(newElem)) {
                return this;
            }
            return new TinyMap6<K, V>(this);
        }
    }

    static final class TinyMap6<K, V> extends TinyArrayMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyMap6() {
            super(6);
        }

        TinyMap6(TinyArrayMap<K, V> other) {
            super(other, 6);
        }

        TinyMap6(Map<K, V> other) {
            super(6, other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() <= 5 || containsKey(newElem)) {
                return this;
            }
            return new TinyMap8<K, V>(this);
        }
    }

    static final class TinyMap8<K, V> extends TinyArrayMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyMap8() {
            super(8);
        }

        TinyMap8(TinyArrayMap<K, V> other) {
            super(other, 8);
        }

        TinyMap8(Map<K, V> other) {
            super(8, other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() <= 7 || containsKey(newElem)) {
                return this;
            }
            return new TinyMap16<K, V>(this);
        }
    }

    static final class TinyMap16<K, V> extends TinyArrayMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyMap16() {
            super(16);
        }

        TinyMap16(TinyArrayMap<K, V> other) {
            super(other, 16);
        }

//        TinyHashMap16(Map<K, V> other) {
//            super(16, other);
//        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() < 16 || containsKey(newElem)) {
                return this;
            }
            return new TinyHashMap32<K, V>(this);
        }
    }

    static final class TinyHashMap32<K, V> extends HashMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyHashMap32() {
            super(32);
        }

        TinyHashMap32(Map<K, V> other) {
            super(other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() < 32 || containsKey(newElem)) {
                return this;
            }
            return new TinyHashMap64<K, V>(this);
        }
    }

    static final class TinyHashMap64<K, V> extends HashMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyHashMap64() {
            super(64);
        }

        TinyHashMap64(Map<K, V> other) {
            super(other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() < 64 || containsKey(newElem)) {
                return this;
            }
            return new TinyHashMap128<K, V>(this);
        }
    }

    static final class TinyHashMap128<K, V> extends HashMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyHashMap128() {
            super(128);
        }

        TinyHashMap128(Map<K, V> other) {
            super(other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            if (size() < 128 || containsKey(newElem)) {
                return this;
            }
            return new TinyHashMap<K, V>(this);
        }
    }

    static final class TinyHashMap<K, V> extends HashMap<K, V> implements TinyMaps.CompactMap<K, V> {

        public TinyHashMap(int initialCapacity) {
            super(initialCapacity);
        }

        TinyHashMap(Map<K, V> other) {
            super(other);
        }

        @Override
        public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
            return this;
        }
    }
}
