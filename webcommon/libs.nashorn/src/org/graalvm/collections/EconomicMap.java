/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.collections;

import java.util.function.BiFunction;

/**
 * Memory efficient map data structure.
 *
 * @since 1.0
 */
public interface EconomicMap<K, V> extends UnmodifiableEconomicMap<K, V> {
    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     *
     * @since 1.0
     */
    void clear();

    /**
     * Removes the mapping for {@code key} from this map if it is present. The map will not contain
     * a mapping for {@code key} once the call returns.
     *
     * @return the previous value associated with {@code key}, or {@code null} if there was no
     *         mapping for {@code key}.
     * @since 1.0
     */
    V removeKey(K key);

    /**
     * Replaces each entry's value with the result of invoking {@code function} on that entry until
     * all entries have been processed or the function throws an exception. Exceptions thrown by the
     * function are relayed to the caller.
     *
     * @since 1.0
     */
    void replaceAll(BiFunction<? super K, ? super V, ? extends V> function);

    /**
     * Creates a new map that guarantees insertion order on the key set with the default
     * {@link Equivalence#DEFAULT} comparison strategy for keys.
     *
     * @since 1.0
     */
    static <K, V> EconomicMap<K, V> create() {
        return new EconomicMapStub<>();
    }

    /**
     * Creates a new map that guarantees insertion order on the key set with the default
     * {@link Equivalence#DEFAULT} comparison strategy for keys and initializes with a specified
     * capacity.
     *
     * @since 1.0
     */
    static <K, V> EconomicMap<K, V> create(int initialCapacity) {
        return new EconomicMapStub(initialCapacity);
    }

    /**
     * Creates a new map that guarantees insertion order on the key set with the default
     * {@link Equivalence#DEFAULT} comparison strategy for keys and copies all elements from the
     * specified existing map.
     *
     * @since 1.0
     */
    static <K, V> EconomicMap<K, V> create(UnmodifiableEconomicMap<K, V> m) {
        return new EconomicMapStub<>(m);
    }
}

