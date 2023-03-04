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
package org.netbeans.spi.search.impl;

import java.io.File;
import java.net.URI;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.SharabilityQuery.Sharability;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Marian Petras
 */
public final class SharabilityFilter extends SearchFilterDefinition {

    private static final SharabilityFilter INSTANCE = new SharabilityFilter();

    private SharabilityFilter() {
    }

    /**
     */
    @Override
    public boolean searchFile(FileObject file)
            throws IllegalArgumentException {
        if (file.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "file (not folder) expected");                      //NOI18N
        }
        File f = FileUtil.toFile(file);
        if (f == null && !file.canWrite()) {
            // non-standard file objects, e.g. ZIP archive items.
            return true;
        } else {
            return SharabilityQuery.getSharability(file)
                    != Sharability.NOT_SHARABLE;
        }
    }

    @Override
    public boolean searchFile(URI uri) {
        return SharabilityQuery.getSharability(uri)
                != Sharability.NOT_SHARABLE;
    }

    @Override
    public FolderResult traverseFolder(URI uri)
            throws IllegalArgumentException {
        switch (SharabilityQuery.getSharability(uri)) {
            case SHARABLE:
                return FolderResult.TRAVERSE_ALL_SUBFOLDERS;
            case MIXED:
                return FolderResult.TRAVERSE;
            case UNKNOWN:
                return FolderResult.TRAVERSE;
            case NOT_SHARABLE:
                return FolderResult.DO_NOT_TRAVERSE;
            default:
                return FolderResult.TRAVERSE;
        }
    }

    /**
     */
    @Override
    public FolderResult traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        if (!folder.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "folder expected");                                 //NOI18N
        }
        File f = FileUtil.toFile(folder);
        if (f == null && !folder.canWrite()) {
            // non-standard file objects, e.g. ZIP archive items.
            return FolderResult.TRAVERSE;
        } else {
            Sharability sharability = SharabilityQuery.getSharability(folder);
            switch (sharability) {
                case NOT_SHARABLE:
                    return FolderResult.DO_NOT_TRAVERSE;
                case SHARABLE:
                    return FolderResult.TRAVERSE_ALL_SUBFOLDERS;
                default:
                    return FolderResult.TRAVERSE;
            }
        }
    }

    public static SearchFilterDefinition getInstance() {
        return INSTANCE;
    }
}
