/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

        return new Iterable<FileObject>() {

            @Override
            public Iterator<FileObject> iterator() {
                return createFilesToSearchIterator(options, listener,
                        terminated);
            }
        };
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

        return new Iterable<URI>() {

            @Override
            public Iterator<URI> iterator() {
                return createUrisToSearchIterator(options, listener,
                        terminated);
            }
        };
    }
}
