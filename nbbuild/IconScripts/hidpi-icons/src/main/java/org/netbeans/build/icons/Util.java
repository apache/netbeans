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
package org.netbeans.build.icons;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;

public final class Util {

    private Util() {
    }

    /**
     * Put a non-null value into a map, throwing an exception if the key was already assigned to a
     * value. The map is assumed not to contain null values.
     */
    public static <K, V> void putChecked(Map<K, V> map, K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (map.putIfAbsent(key, value) != null) {
            throw new IndexOutOfBoundsException("Duplicate key " + key + " in map " + map);
        }
    }

    public static <K> void addChecked(Set<K> set, K value) {
        if (!set.add(value)) {
            throw new IndexOutOfBoundsException("Duplicate value " + value + " in set " + set);
        }
    }


    /**
     * Get a value from a map, throwing an exception if the key was not assigned, or was assigned to
     * a null value.
     */
    public static <K, V> @Nonnull V getChecked(Map<K, V> map, K key) {
        V ret = map.get(key);
        if (ret == null) {
            if (!map.containsKey(key)) {
                throw new NoSuchElementException("Could not find key " + key + " in map " + map);
            } else {
                throw new NullPointerException("The key " + key + " was assigned to a null value");
            }
        }
        return ret;
    }

    public static <K, V> ImmutableSetMultimap<K, V> reverse(Map<V, K> map) {
        SetMultimap<K, V> ret = LinkedHashMultimap.create();
        for (Entry<V, K> entry : map.entrySet()) {
            ret.put(entry.getValue(), entry.getKey());
        }
        return ImmutableSetMultimap.copyOf(ret);
    }
}
