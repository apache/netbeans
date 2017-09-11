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
package org.netbeans.api.search;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.search.provider.SearchFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Search root is a pair containing a folder (or file) and a set of search
 * filters.
 */
public final class SearchRoot {

    private List<SearchFilter> filters;
    private FileObject rootFile;
    private URI rootUri;
    private static final List<SearchFilter> EMPTY_FILTER_LIST =
            Collections.emptyList();
    private static final Logger LOG = Logger.getLogger(
            SearchRoot.class.getName());

    /**
     * Create a new search root, defined by a folder and a set of filters.
     *
     * @param rootFile Root file, cannot be null.
     * @param filters List of default filters, can be null.
     */
    public SearchRoot(@NonNull FileObject rootFile,
            @NullAllowed List<SearchFilter> filters) {

        Parameters.notNull("rootFile", rootFile);                       //NOI18N
        this.rootFile = rootFile;
        this.filters = filters == null ? EMPTY_FILTER_LIST : filters;
    }

    /**
     * Create a new search root, defined by a folder and a set of filters.
     *
     * @param rootUri Root URI, cannot be null.
     * @param filters List of default filters, can be null.
     *
     * @since org.netbeans.api.search/1.4
     */
    public SearchRoot(@NonNull URI rootUri,
            @NullAllowed List<SearchFilter> filters) {

        Parameters.notNull("rootFile", rootFile);                       //NOI18N
        this.rootUri = rootUri;
        this.filters = filters == null ? EMPTY_FILTER_LIST : filters;
    }

    /**
     * Get list of filters.
     *
     * @return List of default filters. Can be empty list, but never null.
     */
    public @NonNull List<SearchFilter> getFilters() {
        return filters;
    }

    /**
     * Get the file object.
     *
     * @return Root file (regular file or folder). Never null.
     */
    public @NonNull FileObject getFileObject() {
        if (rootFile == null) {
            try {
                FileObject fo = FileUtil.toFileObject(new File(rootUri));
                if (fo == null) {
                    rootFile = createFakeFile(rootUri, null);
                } else {
                    rootFile = fo;
                }
            } catch (Exception e) {
                rootFile = createFakeFile(rootUri, e);
            }
        }
        return rootFile;
    }

    /**
     * Get URI of the search root.
     *
     * @since org.netbeans.api.search/1.4
     */
    public @NonNull URI getUri() {
        if (rootUri == null) {
            rootUri = rootFile.toURI();
        }
        return rootUri;
    }

    private FileObject createFakeFile(URI uri, Throwable t) {
        LOG.log(Level.INFO, "Invalid URI: " + uri, t);                  //NOI18N
        return FileUtil.createMemoryFileSystem().getRoot();
    }
}