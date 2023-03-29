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
package org.netbeans.api.java.source.support;

import com.sun.source.tree.Tree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Jan Lahoda
 */
public class CancellableTreePathScanner<R,P> extends ErrorAwareTreePathScanner<R,P> {

    private final AtomicBoolean internalCanceled;
    private final AtomicBoolean canceled;

    /**Construct a new CancellableTreePathScanner which can be canceled by calling
     * the {@link #cancel} method.
     */
    public CancellableTreePathScanner() {
        this(null);
    }

    /**Construct a new CancellableTreePath Scanner which can be canceled either by calling
     * the {@link #cancel} method, or by setting <code>true</code> into the provided
     * <code>canceled</code> {@link AtomicBoolean}.
     * 
     * @param canceled an {@link AtomicBoolean} through which this scanner can be canceled.
     *                 The scanner never changes the state of the {@link AtomicBoolean}.
     * @since 0.29
     */
    public CancellableTreePathScanner(AtomicBoolean canceled) {
        this.canceled = canceled;
        
        this.internalCanceled = new AtomicBoolean();
    }

    protected boolean isCanceled() {
        return internalCanceled.get() || (canceled != null && canceled.get());
    }

    public void cancel() {
        internalCanceled.set(true);
    }

    /** {@inheritDoc}
     */
    public R scan(Tree tree, P p) {
        if (isCanceled())
            return null;
        
        return super.scan(tree, p);
    }

    /** {@inheritDoc}
     */
    public R scan(Iterable<? extends Tree> trees, P p) {
        if (isCanceled())
            return null;
        
        return super.scan(trees, p);
    }

}
