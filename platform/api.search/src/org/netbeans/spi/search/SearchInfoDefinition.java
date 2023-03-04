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
package org.netbeans.spi.search;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.openide.filesystems.FileObject;

/**
 * Defines which
 * <code>FileObject</code>s should be searched. Iterator returned by this
 * interface's method enumerates
 * <code>FileObject</code>s that should be searched. <p>
 * <code>SearchInfoDefinition</code> objects are used in
 * action <em>Find in Projects</em>. Action obtains
 * <code>SearchInfoDefinition</code> from 
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Lookup</code>
 * of nodes or projects</a> the action was invoked on.
 * </p>
 *
 * SearchInfoDefinition should be registered to lookups of nodes only if default
 * behavior is not appropriate. By default, recursive search is started in a
 * file or directory that is registered in node's lookup, using default filters
 * or filters defined in an ancestor node (see {@link SubTreeSearchOptions}).
 *
 * In most cases, you do not need to create custom implementation. You can 
 * use some factory method in {@link SearchInfoDefinitionFactory}.
 * 
 * <div clas="nonnormative">
 * <p>Example:</p>
 * <pre>
 * {@code
 * import org.openide.util.lookup.Lookups;
 *
 * public class MyNode extends AbstractNode {
 *
 *   public MyNode(FileObject folder) {
 *     super(new MyNodeChildren(folder),
 *       Lookups.singleton(
 *         SearchInfoDescriptionFactory.createSearchInfo(
 *           folder,
 *           new SearchFilterDescription[] {
 *             SearchInfoDescriptionFactory.VISIBILITY_FILTER
 *           }
 *         )
 *       );
 *     )
 *   }
 *   ...
 * }}</pre>
 * </div>
 * 
 * Note: Do use custom search info definitions only if default behavior does not
 * suit your needs. Ensure that search results are intuitive.
 * 
 * @see SearchInfoDefinitionFactory
 * @see FileObject
 * @see <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getLookup()"><code>Node.getLookup()</code></a>
 * @see <a href="@org-netbeans-modules-projectapi@/org/netbeans/api/project/Project.html#getLookup()"><code>Project.getLookup()</code></a>
 *
 * @author Marian Petras
 */
public abstract class SearchInfoDefinition {

    /**
     * Determines whether the object which provided this
     * <code>SearchInfo</code> can be searched. This method determines whether
     * the <em>Find</em> action should be enabled for the object or not.
     * This method must be very quick as it may be called frequently and its
     * speed may influence responsiveness of the whole application. If the exact
     * algorithm for determination of the result value should be slow, it is
     * better to return
     * <code>true</code> than make the method slow.
     *
     * @return
     * <code>false</code> if the object is known that it cannot be searched;
     * <code>true</code> otherwise
     */
    public abstract boolean canSearch();

    /**
     * Specifies which
     * <code>FileObject</code>s should be searched. The returned
     * <code>Iterator</code> needn't implement method {@link java.util.Iterator#remove remove()}
     * (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * @param options File name pattern, traversing options and custom filters.
     * @param listener Listener that should be notified about important events
     * and progress.
     * @param terminated Object that can be asked whether the search has
     * been terminated by the user.
     * @return iterator which iterates over
     * <code>FileObject</code>s to be searched
     */
    public abstract @NonNull Iterator<FileObject> filesToSearch(
            @NonNull SearchScopeOptions options,
            @NonNull SearchListener listener,
            @NonNull AtomicBoolean terminated);

    /**
     * Returns list of files or folders where the search starts. It can be used
     * for computing relative paths, or as starting points for alternative
     * search providers.
     *
     * @return List of search roots.
     */
    public abstract @NonNull List<SearchRoot> getSearchRoots();

    /**
     * Specifies which
     * <code>URIs</code>s should be searched. The returned
     * <code>Iterator</code> needn't implement method
     * {@link java.util.Iterator#remove remove()} (i.e. it may throw
     * <code>UnsupportedOperationException</code> instead of actual
     * implementation).
     *
     * The default implementation uses internaly FileObject iterator returned by
     * {@link #filesToSearch(SearchScopeOptions, SearchListener, AtomicBoolean)}
     *
     * @param options File name pattern, traversing options and custom filters.
     * @param listener Listener that should be notified about important events
     * and progress.
     * @param terminated Object that can be asked whether the search has been
     * terminated by the user.
     * @return iterator which iterates over
     * <code>FileObject</code>s to be searched
     *
     * @since org.netbeans.api.search/1.4
     */
    public @NonNull
    Iterator<URI> urisToSearch(
            @NonNull SearchScopeOptions options,
            @NonNull SearchListener listener,
            @NonNull AtomicBoolean terminated) {

        final Iterator<FileObject> inner = filesToSearch(options,
                listener, terminated);
        return new Iterator<URI>() {
            @Override
            public boolean hasNext() {
                return inner.hasNext();
            }

            @Override
            public URI next() {
                FileObject next = inner.next();
                return next == null ? null : next.toURI();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
