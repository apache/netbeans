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
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * Implementation of search filter that is associated with a search root.
 *
 * All search providers should respect search filters. It is only relevant if
 * custom algorithm for for traversing is used instead of standard iterating.
 *
 * @author jhavlin
 */
public abstract class SearchFilter {

    /**
     * Result of filtering a folder.
     */
    public static enum FolderResult {

        /**
         * Constant representing answer &quot;do not traverse the folder&quot;.
         */
        DO_NOT_TRAVERSE,
        /**
         * Constant representing answer &quot;traverse the folder&quot;.
         */
        TRAVERSE,
        /**
         * Constant representing answer &quot;traverse the folder and all its
         * direct and indirect children (both files and subfolders)&quot;.
         */
        TRAVERSE_ALL_SUBFOLDERS
    }

    /**
     * Answers a question whether a given file should be searched. The file must
     * be a plain file (not folder).
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is a folder
     */
    public abstract boolean searchFile(@NonNull FileObject file)
            throws IllegalArgumentException;

    /**
     * Answers a question whether a given URI should be searched. The URI must
     * stand for a plain file (not folder).
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>URI</code> is a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public abstract boolean searchFile(@NonNull URI fileUri);

    /**
     * Answers a questions whether a given folder should be traversed (its
     * contents searched). The passed argument must be a folder.
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is not a folder
     */
    public abstract @NonNull FolderResult traverseFolder(
            @NonNull FileObject folder) throws IllegalArgumentException;

    /**
     * Answers a questions whether a given URI should be traversed (its
     * contents searched). The passed URI must stand for a folder.
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>URI</code> is not a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public abstract @NonNull FolderResult traverseFolder(
            @NonNull URI folderUri) throws IllegalArgumentException;
}
