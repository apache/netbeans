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

package org.netbeans.modules.profiler.oql.engine.api.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * Provides an iterator over instances of <I> using instances of <T> for traversal
 * @author Jaroslav Bachorik
 */
public abstract class TreeIterator<I, T> implements Iterator<I> {
    private Stack<T> toInspect = new Stack<T>();
    private Set<T> inspected = new HashSet<T>();

    private T popped = null;
    private Iterator<I> inspecting = null;

    public TreeIterator(T root) {
        toInspect.push(root);
        inspected.add(root);
    }
    
    public boolean hasNext() {
        setupIterator();
        return inspecting != null && inspecting.hasNext();
    }

    public I next() {
        setupIterator();

        if (inspecting == null || !inspecting.hasNext()) {
            throw new NoSuchElementException();
        }

        I retVal = inspecting.next();
        return retVal;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    protected abstract Iterator<I> getSameLevelIterator(T popped);
    protected abstract Iterator<T> getTraversingIterator(T popped);

    private void setupIterator() {
        while (!toInspect.isEmpty() && (inspecting == null || !inspecting.hasNext())) {
            popped = toInspect.pop();
            if (popped != null) {
                inspecting = getSameLevelIterator(popped);
                Iterator<T> recurseIter = getTraversingIterator(popped);
                while (recurseIter.hasNext()) {
                    T inspectNext = recurseIter.next();
                    if (inspectNext == null) continue;
                    if (!inspected.contains(inspectNext)) {
                        toInspect.push(inspectNext);
                        inspected.add(inspectNext);
                    }
                }
            } else {
                inspecting = null;
            }
        }
    }
}
