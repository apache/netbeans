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

package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * This class implements the {@link Set} interface, backed by an
 * {@link ArrayList}.
 * <p>The {@link ArrayList} supports :
 * <ul>
 * <li>the reordering of elements after adding each new element (see {@link
 *     #ordering(boolean)}). The elements are reordered using their {@linkplain
 *     Comparable natural ordering}. Reordering of elements is disabled by
 *     default.</li>
 * <li>the avoiding to add {@code null} elements (see {@link
 *     #nullIsAllowed(boolean)}). A {@code null} element can be added by 
 *     default.</li>
 * <li>the disabling of adding more elements than it is specified at creation
 *     time. (see {@link ArraySet#ArraySet(int)})</li>
 * </ul>
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * List<Object> objects = new ArraySet<Object>(LIMIT).
 *                            ordering(true).
 *                            nullIsAllowed(false);
 * }</pre>
 *
 * @author Victor G. Vasilyev <vvg@netbeans.org>
 */
public class ArraySet<E extends Comparable<E>>
        extends ArrayList<E> implements Set<E> {
    private int limit;
    private boolean isSorted;
    private boolean isNullAllowed;

    /**
     * Creates new {@code ArraySet}.
     * @param limit max number of elements that can be added to this
     *        {@code ArraySet}. <b>Note:</b> <i>If reordering of elements is
     *        switched on then you should avoid using a big limit due to
     *        performance problems.</i> Seems 500 elements with a quick
     *        {@link Comparable#compareTo(java.lang.Object)} method
     *        implementation is OK.
     */
    public ArraySet(int limit) {
        this.limit = limit;
    }

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if {@link #size()} {@code >= limit}.
     * @throws IllegalArgumentException if {@code null} element is not allowed
     * and a specified element {@code e} is {@code null}.
     */
    @Override
    public boolean add(E e) throws IllegalStateException,
                                   IllegalArgumentException {
        if(!isNullAllowed && e == null) {
            throw new IllegalArgumentException();
        }
        if(size() >= limit) {
            throw new IllegalStateException();
        }
        if (contains(e)) {
            throw new IllegalArgumentException();
        }
        if(super.add(e)) {
            if(isSorted) {
                Collections.sort(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets mode of ordering.
     * @param doOrdering if {@code true} - ordering of elements will be allowed,
     *        and it will be performed during the next call of the
     *        {@link #add(java.lang.Object) add} method, otherwise, if {@code
     *        false} - ordering will be disabled.
     * @return this {@code ArraySet}.
     */
    public ArraySet<E> ordering(boolean doOrdering) {
        this.isSorted = doOrdering;
        return this;
    }

    /**
     * Sets mode of adding the {@code null} element.
     * @param isNullAllowed if {@code true} - adding the {@code null} element 
     *        will be allowed, otherwise, if {@code false} - adding will be 
     *        disabled.
     * @return this {@code ArraySet}.
     */
    public ArraySet<E> nullIsAllowed(boolean isNullAllowed) {
        this.isNullAllowed = isNullAllowed;
        return this;
    }

}
