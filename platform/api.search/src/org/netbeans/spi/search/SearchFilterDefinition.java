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

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Implementations of this class define which files and folders should be
 * searched and which should be skipped during search over a directory
 * structure.
 *
 * @author Marian Petras, jhavlin
 */
public abstract class SearchFilterDefinition {

    private static final Logger LOG =
            Logger.getLogger(SearchFilterDefinition.class.getName());
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
     * Every file matching SearchScopeOptions criteria will be passed to this
     * method. Please make sure that the computation is as fast as possible.
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
     * Answers a questions whether a given folder should be traversed (its
     * contents searched). The passed argument must be a folder.
     *
     * Every traversed folder will be passed to this
     * method. Please make sure the computation is as fast as possible!
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
     * Answers a question whether a file with given URI should be searched. The
     * file must be URI of a plain file (not folder).
     *
     * The default implementation creates a {@link FileObject} instance for each
     * URI and passes it to {@link #searchFile(FileObject)}. Override to improve
     * performance.
     *
     * @return
     * <code>true</code> if the given file should be searched;
     * <code>false</code> if not
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public boolean searchFile(@NonNull URI uri) {
        File f = null;
        try {
            f = new File(uri);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.INFO, null, iae);
            return false;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            return false;
        } else {
            return searchFile(fo);
        }
    }

    /**
     * Answers a questions whether a folder with given URI should be traversed
     * (its contents searched). The passed argument must be URI of a folder.
     *
     * The default implementation creates a {@link FileObject} instance for each
     * URI and passes it to {@link #traverseFolder(FileObject)}. Override to
     * improve performance.
     *
     * @return One of constants of {@link FolderResult}. If
     * <code>TRAVERSE_ALL_SUBFOLDERS</code> is returned, this filter will not be
     * applied on the folder's children (both direct and indirect, both files
     * and folders)
     * @exception java.lang.IllegalArgumentException if the passed
     * <code>FileObject</code> is not a folder
     *
     * @since org.netbeans.api.search/1.4
     */
    public @NonNull FolderResult traverseFolder(
            @NonNull URI uri) throws IllegalArgumentException {
        File f = null;
        try {
            f = new File(uri);
        } catch (IllegalArgumentException iae) {
            LOG.log(Level.INFO, null, iae);
            return FolderResult.DO_NOT_TRAVERSE;
        }
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null) {
            return FolderResult.DO_NOT_TRAVERSE;
        } else {
            return traverseFolder(fo);
        }
    }
}
