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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Iterable wrapping another iterable, yielding an interator, that only offers
 * values, for this the supplied filter returns a true value.
 */
class FilteringIterable<T> implements Iterable<T> {
    private final Iterable<? extends T> delegate;
    private final Function<T,Boolean> filter;

    public FilteringIterable(Iterable<? extends T> delegate, Function<T,Boolean> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Iterator<T> iterator() {
        return new FilteringIterator(delegate.iterator(), filter);
    }

    private static class FilteringIterator<T> implements Iterator<T> {
        private final Iterator<? extends T> delegate;
        private final Function<T,Boolean> filter;

        public FilteringIterator(Iterator<? extends T> delegate, Function<T,Boolean> filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        private T next;

        @Override
        public boolean hasNext() {
            fillNextIfPossible();
            return next != null;
        }

        @Override
        public T next() {
            fillNextIfPossible();
            if(next == null) {
                throw new NoSuchElementException();
            }
            T value = next;
            next = null;
            return value;
        }

        private void fillNextIfPossible() {
            if (next == null) {
                while (delegate.hasNext()) {
                    T nextCandiate = delegate.next();
                    if(filter.apply(nextCandiate)) {
                        next = nextCandiate;
                        break;
                    }
                }
            }
        }
    }
}

