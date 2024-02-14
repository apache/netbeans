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
package org.netbeans.api.search.provider.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchListener;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian Petras
 */
public class CompoundSearchInfo extends SearchInfo {

    /**
     *
     */
    private final SearchInfo[] elements;

    /**
     * Creates a new instance of CompoundSearchInfo
     *
     * @param elements elements of this
     * <code>SearchInfo</code>
     * @exception java.lang.IllegalArgumentException if the argument was
     * <code>null</code>
     */
    public CompoundSearchInfo(SearchInfo... elements) {
        if (elements == null) {
            throw new IllegalArgumentException();
        }

        this.elements = elements.length != 0 ? elements
                : null;
    }

    /**
     */
    @Override
    public boolean canSearch() {
        if (elements != null) {
            for (SearchInfo element : elements) {
                if (element.canSearch()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    @Override
    public Iterator<FileObject> createFilesToSearchIterator(
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {
        if (elements == null) {
            return Collections.<FileObject>emptyList().iterator();
        }

        List<SearchInfo> searchableElements =
                new ArrayList<>(elements.length);
        for (SearchInfo element : elements) {
            if (element.canSearch()) {
                searchableElements.add(element);
            }
        }
        return new AbstractCompoundIterator<SearchInfo, FileObject>(
                searchableElements.toArray(new SearchInfo[0]),
                options, listener, terminated) {
            @Override
            protected Iterator<FileObject> getIteratorFor(SearchInfo element,
                    SearchScopeOptions options, SearchListener listener,
                    AtomicBoolean terminated) {
                return element.getFilesToSearch(options, listener,
                        terminated).iterator();
            }
        };
    }

    @Override
    protected Iterator<URI> createUrisToSearchIterator(
            SearchScopeOptions options, SearchListener listener,
            AtomicBoolean terminated) {
        if (elements == null) {
            return Collections.<URI>emptyList().iterator();
        }

        List<SearchInfo> searchableElements =
                new ArrayList<>(elements.length);
        for (SearchInfo element : elements) {
            if (element.canSearch()) {
                searchableElements.add(element);
            }
        }
        return new AbstractCompoundIterator<SearchInfo, URI>(
                searchableElements.toArray(new SearchInfo[0]),
                options, listener, terminated) {
            @Override
            protected Iterator<URI> getIteratorFor(SearchInfo element,
                    SearchScopeOptions options, SearchListener listener,
                    AtomicBoolean terminated) {
                return element.getUrisToSearch(
                        options, listener, terminated).iterator();
            }
        };
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        List<SearchRoot> allRoots = new LinkedList<>();
        for (SearchInfo si : elements) {
            allRoots.addAll(si.getSearchRoots());
        }
        return allRoots;
    }
}
