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
