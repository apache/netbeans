/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap.KeyedValue;

/**
 * A weak cache of objects which reference the key.
 * The key is not stored at all, we use the hash code instead. The values must
 * be able to provide the key for verification, as multiple keys can provide
 * identical has codes. <p>
 * Neither key nor value can be <code>null</code> and keys must not change their
 * hash codes over time. <p>
 * This map is not synchronized.
 * 
 * @author Martin Entlicher
 */
public final class WeakCacheMap<K, V extends KeyedValue<K>> extends AbstractMap<K, V> {
    
    private final Map<Integer, List<Reference<V>>> cache = new HashMap<>();

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (List<Reference<V>> values : cache.values()) {
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        if (values != null) {
            V retv = null;
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                } else if (retv == null && key.equals(v.getKey())) {
                    retv = v;
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
            return retv;
        } else {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        V existingv = null;
        if (values == null) {
            values = new LinkedList<>();
            cache.put(hash, values);
        } else {
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                } else if (existingv == null && key.equals(v.getKey())) {
                    existingv = v;
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
        }
        values.add(new WeakReference<>(value));
        return existingv;
    }

    @Override
    public V remove(Object key) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        if (values != null) {
            V retv = null;
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null || retv == null && key.equals(v.getKey())) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
            return retv;
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static interface KeyedValue<K> {
        K getKey();
    }
    
}
