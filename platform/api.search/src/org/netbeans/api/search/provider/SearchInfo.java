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
package org.netbeans.api.search.provider;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.filesystems.FileObject;

/**
 * Info about searching under a node or a set of nodes.
 *
 * @author jhavlin
 */
public abstract class SearchInfo {

    /**
     * Checks that searching is possible.
     *
     * @return False is searching is not possible (it is not supported or it is
     * sure that there are no files to search), true if there is chance that
     * some files can be found.
     */
    public abstract boolean canSearch();

    /**
     * Get search roots. This information can be used for computing relative
     * paths, or in custom algorithms for file traversing.
     *
     * @return List of search roots associated with this search info.
     */
    public abstract @NonNull List<SearchRoot> getSearchRoots();

    /**
     * Create {@link Iterator} that iterates over all files in the search scope
     * that comply with search options and search filters.
     *
     * @param options Custom options. This object encapsulates custom search
     * filters, file name pattern and general search settings.
     * @param listener Listener that is notified when some important event
     * occurs during searching. Listener passed to {@link SearchComposition}
     * should be used here.
     * @param terminated Object that can be asked by the iterator whether
     * the search has been terminated.
     *
     * @return Iterator over all files that comply with specified options (in
     * the scope of this search info).
     */
    protected abstract @NonNull Iterator<FileObject> createFilesToSearchIterator(
            @NonNull SearchScopeOptions options,
            @NonNull SearchListener listener,
            @NonNull AtomicBoolean terminated);

    /**
     * Create {@link Iterator} that iterates over all URIs in the search scope
     * that comply with search options and search filters.
     *
     * @param options Custom options. This object encapsulates custom search
     * filters, file name pattern and general search settings.
     * @param listener Listener that is notified when some important event
     * occurs during searching. Listener passed to {@link SearchComposition}
     * should be used here.
     * @param terminated Object that can be asked by the iterator whether the
     * search has been terminated.
     *
     * @return Iterator over all URIs that comply with specified options (in
     * the scope of this search info).
     *
     * @since org.netbeans.api.search/1.4
     */
    protected abstract @NonNull Iterator<URI> createUrisToSearchIterator(
            @NonNull SearchScopeOptions options,
            @NonNull SearchListener listener,
            @NonNull AtomicBoolean terminated);

    /**
     * Get {@link Iterable} that iterates over all files in the search scope
     * that comply with search options and search filters.
     *
     * @param options Custom options. This object encapsulates custom search
     * filters, file name pattern and general search settings.
     * @param listener Listener that is notified when some important event
     * occurs during searching. Listener passed to {@link SearchComposition}
     * should be used here.
     * @param terminated Object that can be asked by the iterator whether
     * the search has been terminated.
     * 
     * <div class="nonnormative">
     * <p>
     *  This method can be used in for-each loops:
     * </p>
     * <pre>
     * {@code
     * for (FileObject fo: searchInfo.getFilesToSearch(opts,listnr,term) {
     *   ResultType result = somehowCheckFileContentMatches(fo);
     *   if (result != null) {
     *     searchResultsDisplayer.addMatchingObject(result);
     *   }
     * }}
     * </pre>
     * </div>
     */
    public final @NonNull Iterable<FileObject> getFilesToSearch(
            @NonNull final SearchScopeOptions options,
            @NonNull final SearchListener listener,
            @NonNull final AtomicBoolean terminated) {
        return () -> createFilesToSearchIterator(options, listener, terminated);
    }

    /**
     * Get {@link Iterable} that iterates over all URIs in the search scope
     * that comply with search options and search filters.
     *
     * @param options Custom options. This object encapsulates custom search
     * filters, file name pattern and general search settings.
     * @param listener Listener that is notified when some important event
     * occurs during searching. Listener passed to {@link SearchComposition}
     * should be used here.
     * @param terminated Object that can be asked by the iterator whether
     * the search has been terminated.
     *
     * <div class="nonnormative">
     * <p>
     *  This method can be used in for-each loops:
     * </p>
     * <pre>
     * {@code
     * for (URI uri: searchInfo.getUrisToSearch(opts,listnr,term) {
     *   ResultType result = somehowCheckFileContentMatches(fo);
     *   if (result != null) {
     *     searchResultsDisplayer.addMatchingObject(result);
     *   }
     * }}
     * </pre>
     * </div>
     *
     * @since org.netbeans.api.search/1.4
     */
    public final @NonNull Iterable<URI> getUrisToSearch(
            @NonNull final SearchScopeOptions options,
            @NonNull final SearchListener listener,
            @NonNull final AtomicBoolean terminated) {
        return () -> createUrisToSearchIterator(options, listener, terminated);
    }
}
