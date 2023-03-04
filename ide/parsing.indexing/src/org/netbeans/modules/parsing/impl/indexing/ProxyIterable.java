/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Iterable over list of lists
 * @author Tomas Zezula
 */
//@NotThreadSafe
final class ProxyIterable<T> implements Iterable<T> {

    private final Collection<? extends Iterable<? extends T>> delegates;
    private final boolean allowDuplicates;
    private final AtomicReference<List<T>> cache;

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates) {
        this(delegates, true, false);
    }

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates, boolean allowDuplicates) {
        this(delegates, allowDuplicates, false);
    }

    public ProxyIterable(Collection<? extends Iterable<? extends T>> delegates, boolean allowDuplicates, boolean cacheValues) {
        assert delegates != null;
        this.delegates = delegates;
        this.allowDuplicates = allowDuplicates;
        this.cache = cacheValues ? new AtomicReference<List<T>>() : null;
    }

    public Iterator<T> iterator() {
        final List<T> _cache = cache == null ? null : cache.get();
        if (_cache != null) {
            return _cache.iterator();
        } else {
            return new ProxyIterator<T>(delegates.iterator(), allowDuplicates, cache);
        }
    }

    @Override
    public String toString() {
        return "ProxyIterable@" + Integer.toHexString(System.identityHashCode(this)) + " [" + delegates + "]"; //NOI18N
    }

    private static final class ProxyIterator<T> implements Iterator<T> {

        private final Iterator<? extends Iterable< ? extends T>> iterables;
        private final Set<T> seen;
        private final AtomicReference<List<T>> cacheRef;
        private final List<T> cache;
        private Iterator<? extends T> currentIterator;
        private T currentObject;

        public ProxyIterator(
                Iterator<? extends Iterable< ? extends T>> iterables,
                boolean allowDuplicates,
                final AtomicReference<List<T>> cacheRef) {
            assert iterables != null;
            this.iterables = iterables;
            this.seen = allowDuplicates ? null : new HashSet<T>();
            this.cacheRef = cacheRef;
            this.cache = this.cacheRef == null ? null : new ArrayList<T>();
        }

        @Override
        public boolean hasNext() {
            if (currentObject != null) {
                return true;
            }

out:        for(;;) {
                if (currentIterator != null) {
                    while(currentIterator.hasNext()) {
                        T o = currentIterator.next();
                        if (seen == null || seen.add(o)) {
                            currentObject = o;
                            break out;
                        }
                    }
                }
                if (iterables.hasNext()) {
                    currentIterator = iterables.next().iterator();
                } else {
                    currentIterator = null;
                    currentObject = null;
                    break out;
                }
            }
            final boolean result = currentObject != null;
            if (!result && this.cacheRef != null) {
                this.cacheRef.compareAndSet(null, cache);
            }
            return result;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T res = currentObject;
            currentObject = null;
            assert res != null;
            if (cache != null) {
                cache.add(res);
            }
            return res;
        }        

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
