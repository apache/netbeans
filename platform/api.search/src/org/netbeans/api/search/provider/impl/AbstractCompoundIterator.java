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
package org.netbeans.api.search.provider.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;

/**
 *
 * @author jhavlin
 */
public abstract class AbstractCompoundIterator<E, T> implements Iterator<T> {

    /**
     *
     */
    private final E[] elements;
    /**
     *
     */
    private int elementIndex;
    /**
     *
     */
    private Iterator<T> elementIterator = null;
    /**
     *
     */
    private T nextObject;
    /**
     *
     */
    private boolean upToDate;
    private SearchScopeOptions options;
    private SearchListener listener;
    private AtomicBoolean terminated;

    /**
     * Creates a new instance of
     * <code>CompoundSearchIterator</code>.
     *
     * @param elements elements of the compound iterator
     * @exception java.lang.IllegalArgumentException if the argument is
     * <code>null</code>
     */
    public AbstractCompoundIterator(E[] elements,
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {

        if (elements == null) {
            throw new IllegalArgumentException("Elements are null");    //NOI18N
        } else if (options == null) {
            throw new IllegalArgumentException("Options are null");     //NOI18N
        }

        this.options = options;

        if (elements.length == 0) {
            this.elements = null;
            elementIndex = 0;
            upToDate = true;                //hasNext() returns always false
        } else {
            this.elements = elements;
            upToDate = false;
        }
        this.listener = listener;
        this.terminated = terminated;
    }

    /**
     */
    @Override
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return (elements != null) && (elementIndex < elements.length);
    }

    /**
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert upToDate == false;

        if (elementIterator == null) {
            elementIterator = getIteratorFor(elements[elementIndex = 0], options,
                    listener, terminated);
        }

        while (!elementIterator.hasNext()) {
            elements[elementIndex] = null;

            if (++elementIndex == elements.length) {
                break;
            }
            elementIterator = getIteratorFor(elements[elementIndex], options,
                    listener, terminated);
        }

        if (elementIndex < elements.length) {
            nextObject = elementIterator.next();
        } else {
            elementIterator = null;
            nextObject = null;
        }

        upToDate = true;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract Iterator<T> getIteratorFor(E element,
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated);
}
