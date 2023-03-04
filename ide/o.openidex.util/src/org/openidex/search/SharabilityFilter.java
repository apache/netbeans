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

package org.openidex.search;

import java.io.File;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Marian Petras
 */
final class SharabilityFilter implements FileObjectFilter {

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
            return SharabilityQuery.getSharability(FileUtil.toFile(file))
                    != SharabilityQuery.NOT_SHARABLE;
        }
    }

    /**
     */
    @Override
    public int traverseFolder(FileObject folder)
            throws IllegalArgumentException {
        if (!folder.isFolder()) {
            throw new java.lang.IllegalArgumentException(
                    "folder expected");                                 //NOI18N
        }
        File f = FileUtil.toFile(folder);
        if (f == null && !folder.canWrite()) {
            // non-standard file objects, e.g. ZIP archive items.
            return TRAVERSE;
        } else {
            final int sharability = SharabilityQuery.getSharability(f);
            switch (sharability) {
                case SharabilityQuery.NOT_SHARABLE:
                    return DO_NOT_TRAVERSE;
                case SharabilityQuery.SHARABLE:
                    return TRAVERSE_ALL_SUBFOLDERS;
                default:
                    return TRAVERSE;
            }
        }
    }
}
