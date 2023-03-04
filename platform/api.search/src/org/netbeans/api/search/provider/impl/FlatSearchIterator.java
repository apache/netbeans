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

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;

/**
 *
 * @author Marian Petras
 */
public class FlatSearchIterator extends AbstractFileObjectIterator {

    /**
     * current enumeration of children
     */
    private Enumeration<? extends FileObject> childrenEnum;
    private boolean upToDate = false;
    /**
     * <code>FileObject</code> to be returned the next time method {@link #next()}
     * is called
     */
    private FileObject nextObject;
    /**
     * Search root
     */
    private FileObject rootFile;

    private SearchListener listener;

    private FileNameMatcher fileNameMatcher;

    private FilterHelper filterHelper;

    private AtomicBoolean terminated;

    /**
     */
    public FlatSearchIterator(FileObject root,
            SearchScopeOptions options,
            List<SearchFilterDefinition> filters,
            SearchListener listener,
            AtomicBoolean terminated) {
        this.rootFile = root;
        if (rootFile.isFolder()) {
            this.childrenEnum = SimpleSearchIterator.sortEnum(
                    rootFile.getChildren(false));
        } else {
            this.childrenEnum = Enumerations.singleton(rootFile);
        }
        this.listener = listener;
        this.fileNameMatcher = FileNameMatcher.create(options);
        this.filterHelper = new FilterHelper(filters, options);
        this.terminated = terminated;
    }

    /**
     */
    @Override
    public boolean hasNext() {
        assert !EventQueue.isDispatchThread();
        if (terminated.get()) {
            return false;
        } else {
            if (!upToDate) {
                update();
            }
            return nextObject != null;
        }
    }

    /**
     */
    @Override
    public FileObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert !upToDate;
        while (childrenEnum.hasMoreElements()) {
            FileObject fo = childrenEnum.nextElement();
            if (fo.isData() && fileNameMatcher.pathMatches(fo) 
                    && filterHelper.fileAllowed(fo, listener)) {
                nextObject = fo;
                upToDate = true;
                return;
            }
        }
        nextObject = null;
        upToDate = true;
    }

    /**
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
