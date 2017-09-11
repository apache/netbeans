/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.lucene;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
final class LRUCache<K,V extends Evictable> {

    private final LinkedHashMap<K, V> cache;
    private final ReadWriteLock lock;

    public LRUCache (final EvictionPolicy<? super K,? super V> policy) {
        this.lock = new ReentrantReadWriteLock();
        this.cache = new LinkedHashMap<K, V>(10,0.75f,true) {
            @Override
            protected boolean removeEldestEntry(Entry<K, V> eldest) {
                final boolean evict = policy.shouldEvict(this.size(), eldest.getKey(), eldest.getValue());
                if (evict) {
                    eldest.getValue().evicted();
                }
                return evict;
            }
        };
    }

    public void put (final K key, final V evictable) {
        assert key != null;
        assert evictable != null;
        this.lock.writeLock().lock();
        try {
            this.cache.put(key, evictable);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public V get(final K key) {
        assert key != null;
        this.lock.readLock().lock();
        try {
            return this.cache.get(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public V remove (final K key) {
        assert key != null;
        this.lock.writeLock().lock();
        try {
            return this.cache.remove(key);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @NonNull
    public Collection<? extends V> clear() {
        final Collection<V> res = new ArrayDeque<>();
        this.lock.writeLock().lock();
        try {
            for (Iterator<Entry<K, V>> it = this.cache.entrySet().iterator(); it.hasNext();) {
                Map.Entry<K,V> e = it.next();
                res.add(e.getValue());
                it.remove();
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return res;
    }

    @Override
    public String toString () {
        this.lock.readLock().lock();
        try {
            return this.cache.toString();
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
