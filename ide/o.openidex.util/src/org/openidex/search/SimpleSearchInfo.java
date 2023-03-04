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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author  Marian Petras
 */
class SimpleSearchInfo implements SearchInfo.Files {

    /**
     * Empty search info object.
     * Its method {@link SearchInfo#canSearch canSearch()}
     * always returns <code>true</code>. Its iterator
     * (returned by method
     * {@link SearchInfo#objectsToSearch objectsToSearch()}) has no elements.
     */
    static final SearchInfo.Files EMPTY_SEARCH_INFO
        = new SearchInfo.Files() {
            public boolean canSearch() {
                return true;
            }
            public Iterator<DataObject> objectsToSearch() {
                return Collections.<DataObject>emptyList().iterator();
            }
            public Iterator<FileObject> filesToSearch() {
                return Collections.<FileObject>emptyList().iterator();
            }
        };
        
    /** */
    private final DataFolder rootFolder;
    /** */
    private final boolean recursive;
    /** */
    private final FileObjectFilter[] filters;
    
    /**
     * Creates a new instance of SimpleSearchInfo
     *
     * @param  folder  <!-- PENDING -->
     * @param  filters  <!-- PENDING, accepts null -->
     * @exception  java.lang.IllegalArgumentException
     *             if the <code>folder</code> argument is <code>null</code>
     */
    SimpleSearchInfo(DataFolder folder,
                     boolean recursive,
                     FileObjectFilter[] filters) {
        if (folder == null) {
            throw new IllegalArgumentException();
        }
        
        if ((filters != null) && (filters.length == 0)) {
            filters = null;
        }
        this.rootFolder = folder;
        this.recursive = recursive;
        this.filters = filters;
    }

    /**
     */
    public boolean canSearch() {
        return (filters != null)
               ? checkFolderAgainstFilters(rootFolder.getPrimaryFile())
               : true;
    }

    /**
     */
    public Iterator<DataObject> objectsToSearch() {
        return Utils.toDataObjectIterator(filesToSearch());
    }

    /**
     */
    public Iterator<FileObject> filesToSearch() {
        return new SimpleSearchIterator(rootFolder,
                                        recursive,
                                        filters != null ? Arrays.asList(filters)
                                                        : null);
    }
    
    /**
     */
    private boolean checkFolderAgainstFilters(final FileObject folder) {
        assert folder.isFolder();
        
        for (int i = 0; i < filters.length; i++) {
            if (filters[i].traverseFolder(folder)
                    == FileObjectFilter.DO_NOT_TRAVERSE) {
                return false;
            }
        }
        return true;
    }
    
}
