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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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
     * @param folder <!-- PENDING -->
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
            for (int i = 0; i < filters.length; i++) {
                if (!isSuppressableFilter(filters[i])
                        && filters[i].traverseFolder(folder)
                        == FolderResult.DO_NOT_TRAVERSE) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < filters.length; i++) {
                if (!isSuppressableFilter(filters[i])
                        && !filters[i].searchFile(folder)) {
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
