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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.spi.search.impl.CompoundSearchInfoDefinition;
import org.netbeans.spi.search.impl.FlatSearchInfoDefinition;
import org.netbeans.spi.search.impl.SharabilityFilter;
import org.netbeans.spi.search.impl.SimpleSearchInfoDefinition;
import org.netbeans.spi.search.impl.SubnodesSearchInfoDefinition;
import org.netbeans.spi.search.impl.VisibilityFilter;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.util.Parameters;

/**
 * Factory for creating
 * <code>SearchInfoDefinitions</code> objects.
 *
 * It can be used if you want to add custom SearchInfoDefinitions to lookups of
 * nodes.
 *
 * @see SearchInfo
 * @author Marian Petras
 */
public final class SearchInfoDefinitionFactory {
    /**
     * Filter for unsharable files. One of default filters.
     */
    public static final SearchFilterDefinition SHARABILITY_FILTER = SharabilityFilter.getInstance();
    /**
     * Filter for invisible files. One of default filters.
     */
    public static final SearchFilterDefinition VISIBILITY_FILTER = VisibilityFilter.getInstance();
    /**
     * Array with default filters
     */
    private static final SearchFilterDefinition[] DEFAULT_FILTERS =
            new SearchFilterDefinition[]{SHARABILITY_FILTER, VISIBILITY_FILTER};
    /**
     * List of default filter definitions.
     */
    public static final List<SearchFilterDefinition> DEFAULT_FILTER_DEFS =
            createDefaultFilterDefList();

    /**
     * Hide constructor.
     */
    private SearchInfoDefinitionFactory() {
    }

    /**
     * Creates a
     * <code>SearchInfoDefinition</code> object for a given folder. The returned
     * <code>SearchInfoDefinition</code> object's method {@link SearchInfoDefinition#canSearch()}
     * always returns
     * <code>true</code> and iterates through
     * <code>FileObject</code>s found in the given folder. Files and folders
     * that do not pass any of the given filters are skipped (not searched). If
     * multiple filters are passed, the filters are applied on each file/folder
     * in the same order as in the array passed to this method.
     *
     * @param root File or folder where the search should start
     * @param filters filters to be used when searching
     * 
     * @return
     * <code>SearchInfo</code> object which iterates through
     * <code>FileObject</code>s found in the specified folder and (optionally)
     * its subfolders
     * @see SearchFilterDefinition
     */
    public static @NonNull SearchInfoDefinition createSearchInfo(
            @NonNull final FileObject root,
            @NonNull final SearchFilterDefinition[] filters) {
        Parameters.notNull("root", root);                               //NOI18N
        return new SimpleSearchInfoDefinition(root, filters);
    }

    /**
     * Create a search definition info for non-recursive searching in a single
     * directory.
     *
     * @param root Directory or file. If file is passed, only that single file
     * will be searched.
     */
    public static @NonNull SearchInfoDefinition createFlatSearchInfo(
            @NonNull final FileObject root,
            @NonNull final SearchFilterDefinition[] filters) {
        Parameters.notNull("root", root);                               //NOI18N
        return new FlatSearchInfoDefinition(root, filters);
    }

    /**
     * Create a search info definition for non-recursive searching in a single
     * directory. Use default filters.
     * 
     * @param root Directory or file. If file is passed, only that single file
     * will be searched.
     */
    public static @NonNull SearchInfoDefinition createFlatSearchInfo(
            @NonNull final FileObject root) {
        return createFlatSearchInfo(root, DEFAULT_FILTERS);
    }

    /**
     * Convenience method for creating a search info definition for a root node
     * with default filters.
     *
     * @see #createSearchInfo(FileObject, SearchFilterDefinition[])
     */
    public static @NonNull SearchInfoDefinition createSearchInfo(
            @NonNull final FileObject root) {
        return createSearchInfo(root, DEFAULT_FILTERS);
    }

    /**
     * Creates a
     * <code>SearchInfoDefinition</code> object for given folders. The returned
     * <code>SearchInfoDefinition</code> object's method {@link SearchInfoDefinition#canSearch() }
     * always returns <code>true</code> and iterates through
     * <code>FileObject</code>s found in the given folders. Files and folders
     * that do not pass any of the given filters are skipped (not searched). If
     * multiple filters are passed, the filters are applied on each file/folder
     * in the same order as in the array passed to this method.
     *
     * @param roots folders which should be searched
     * @param filters filters to be used when searching; or
     * <code>null</code> if no filters should be used
     * @return
     * <code>SearchInfo</code> object which iterates through
     * <code>FileObject</code>s found in the specified folders and their
     * subfolders
     * @see SearchFilterDefinition
     */
    public static @NonNull SearchInfoDefinition createSearchInfo(
            @NonNull final FileObject[] roots,
            @NonNull final SearchFilterDefinition[] filters) {
        Parameters.notNull("roots", roots);                             //NOI18N
        if (roots.length == 0) {
            return SimpleSearchInfoDefinition.EMPTY_SEARCH_INFO;
        }

        if (roots.length == 1) {
            return createSearchInfo(roots[0], filters);
        }

        SearchInfoDefinition[] nested = new SearchInfoDefinition[roots.length];
        for (int i = 0; i < roots.length; i++) {
            nested[i] = createSearchInfo(roots[i], filters);
        }
        return new CompoundSearchInfoDefinition(nested);
    }

    /**
     * Convenience method for creating search info for a list of roots with
     * default filters.
     *
     * @see #createSearchInfo(.FileObject[], SearchFilterDefinition[])
     */
    public static @NonNull SearchInfoDefinition createSearchInfo(
            @NonNull FileObject[] roots) {
        return createSearchInfo(roots, DEFAULT_FILTERS);
    }
    
    /**
     * Creates a
     * <code>SearchInfoDefinition</code> object combining
     * <code>SearchInfoDefinition</code> objects of the node's
     * children.
     *
     * Method {@link SearchInfoDefinition#canSearch()} of the resulting
     * <code>SearchInfoDefinition</code> objects returns always true.
     * The iterator iterates through all
     * <code>FileObject</code>s returned by the subnode's
     * <code>SearchInfoDefinition</code> iterators.
     *
     * <p> In case you want to create node children in the node constructor, you
     * can create children in one constructor and pass them to another
     * constructor:</p>
     * <pre class="nonnormative"">
     * {@code
     * public ExampleNode() {
     *    this(Children.create(someChildrenFactory, true));
     * }
     *
     * private ExampleNode(Children ch) {
     *    super(ch, Lookups.singleton(
     *       SearchInfoDefinitionFactory.createSearchInfoBySubnodes(ch)));
     *    // ...
     * }}</pre>
     * 
     * @param children  Children to create
     * <code>SearchInfoDefinition</code> for
     * @return
     * <code>SearchInfoDefinition</code> object representing combination of
     * <code>SearchInfoDefinition</code> objects of child nodes.
     */
    public static @NonNull SearchInfoDefinition createSearchInfoBySubnodes(
            @NonNull Children children) {
        Parameters.notNull("children", children);                       //NOI18N
        return new SubnodesSearchInfoDefinition(children);
    }

    /**
     * Create immutable list of default filter definitions. It will be assigned
     * to constant {@link #DEFAULT_FILTER_DEFS}.
     */
    private static List<SearchFilterDefinition> createDefaultFilterDefList() {
        List<SearchFilterDefinition> list;
        list = new ArrayList<>(2);
        list.add(SHARABILITY_FILTER);
        list.add(VISIBILITY_FILTER);
        return Collections.unmodifiableList(list);
    }
}
