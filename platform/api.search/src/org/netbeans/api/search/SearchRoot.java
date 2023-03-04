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