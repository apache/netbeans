/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        list = new ArrayList<SearchFilterDefinition>(2);
        list.add(SHARABILITY_FILTER);
        list.add(VISIBILITY_FILTER);
        return Collections.unmodifiableList(list);
    }
}
