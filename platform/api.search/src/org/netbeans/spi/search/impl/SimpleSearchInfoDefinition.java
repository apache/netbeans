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
package org.netbeans.spi.search.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.api.search.provider.impl.DefinitionUtils;
import org.netbeans.api.search.provider.impl.SimpleSearchIterator;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchFilterDefinition.FolderResult;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marian Petras
 */
public final class SimpleSearchInfoDefinition extends SearchInfoDefinition {

    /**
     * Empty search info object. Its method {@link SearchInfoDefinition#canSearch canSearch()}
     * always returns
     * <code>true</code>. Its iterator (returned by method {@link SearchInfo#objectsToSearch objectsToSearch()})
     * has no elements.
     */
    public static final SearchInfoDefinition EMPTY_SEARCH_INFO = new SearchInfoDefinition() {

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
                SearchListener listener, AtomicBoolean terminated) {
            return Collections.<FileObject>emptyList().iterator();
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            return Collections.emptyList();
        }
    };
    /**
     *
     */
    private final FileObject rootFile;    
    /**
     *
     */
    private final SearchFilterDefinition[] filters;

    /**
     * Creates a new instance of SimpleSearchInfo
     *
     * @param rootFile <!-- PENDING -->
     * @param filters <!-- PENDING, accepts null -->
     * @exception java.lang.IllegalArgumentException if the
     * <code>folder</code> argument is
     * <code>null</code>
     */
    public SimpleSearchInfoDefinition(FileObject rootFile,
            SearchFilterDefinition[] filters) {
        if (rootFile == null) {
            throw new IllegalArgumentException();
        }

        if ((filters != null) && (filters.length == 0)) {
            filters = null;
        }
        this.rootFile = rootFile;
        this.filters = filters != null ? niceFilters(rootFile, filters) : null;
    }

    /**
     * Return sub-array of filters that are nice to a file.
     */
    private static SearchFilterDefinition[] niceFilters(FileObject fo,
            SearchFilterDefinition[] allFilters) {

        boolean[] mask = new boolean[allFilters.length]; // mask for bad filters
        if (fo.isFolder()) {
            for (int i = 0; i < allFilters.length; i++) {
                FolderResult result = allFilters[i].traverseFolder(fo);
                mask[i] = (result != FolderResult.DO_NOT_TRAVERSE);
            }
        } else {
            assert fo.isData();
            for (int i = 0; i < allFilters.length; i++) {
                mask[i] = allFilters[i].searchFile(fo);
            }
        }
        SearchFilterDefinition[] nice =
                new SearchFilterDefinition[countTrues(mask)];
        int niceIndex = 0;
        for (int i = 0; i < allFilters.length; i++) {
            if (mask[i]) {
                nice[niceIndex++] = allFilters[i];
            }
        }
        return nice;
    }

    /**
     * Count true values in a boolean array.
     */
    private static int countTrues(boolean[] booleans) {
        int trues = 0;
        for (boolean b : booleans) {
            if (b) {
                trues++;
            }
        }
        return trues;
    }

    /**
     */
    @Override
    public boolean canSearch() {
        return (filters != null)
                ? checkFolderAgainstFilters(rootFile)
                : true;
    }

    /**
     */
    @Override
    public Iterator<FileObject> filesToSearch(SearchScopeOptions options,
        SearchListener listener, AtomicBoolean terminated) {
        return new SimpleSearchIterator(rootFile,
                options,
                filters != null ? Arrays.asList(filters)
                : null, listener, terminated);
    }

    /**
     */
    private boolean checkFolderAgainstFilters(final FileObject folder) {

        if (folder.isFolder()) {
            for (SearchFilterDefinition filter : filters) {
                if (!isSuppressableFilter(filter)
                        && filter.traverseFolder(folder)
                        == FolderResult.DO_NOT_TRAVERSE) {
                    return false;
                }
            }
        } else {
            for (SearchFilterDefinition filter : filters) {
                if (!isSuppressableFilter(filter)
                        && !filter.searchFile(folder)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Test whether filter can be supressed by user.
     */
    private boolean isSuppressableFilter(SearchFilterDefinition filter) {
        return filter == SearchInfoDefinitionFactory.SHARABILITY_FILTER;
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        SearchRoot searchRoot = new SearchRoot(rootFile,
                DefinitionUtils.createSearchFilterList(filters));
        return Collections.singletonList(searchRoot);
    }
}
