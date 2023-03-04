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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;

/**
 *
 * @author  Marian Petras
 */
class SimpleSearchIterator implements Iterator<FileObject> {

    /** current enumeration of children */
    private Enumeration<? extends FileObject> childrenEnum;
    /**
     * filters to be applied on the current enumeration of children
     * ({@link #childrenEnum})
     */
    private List<FileObjectFilter> filters;
    /**
     * contains either an equal copy of {@link #filters} or <code>null</code>
     */
    private List<FileObjectFilter> filtersCopy;
    /** */
    private final boolean recursive;
    /** */
    private boolean searchInArchives = false; // TODO make configurable
    /** stack of the ancestor folders' children enumerations */
    private final List<Enumeration<? extends FileObject>> enums
            = new ArrayList<Enumeration<? extends FileObject>>();            //unsynced stack
    /**
     * stack of filter lists to be applied on children of the ancestor folders
     * ({@link #enums})
     */
    private final List<List<FileObjectFilter>> filterLists
            = new ArrayList<List<FileObjectFilter>>();      //unsynced stack
    /** whether value of {@link #nextObject} is up-to-date */
    private boolean upToDate = false;
    /**
     * <code>DataObject</code> to be returned the next time method
     * {@link #next()} is called
     */
    private FileObject nextObject;

    /**
     */
    SimpleSearchIterator(DataFolder folder,
                         boolean recursive,
                         List<FileObjectFilter> filters) {
        this.childrenEnum = folder.getPrimaryFile().getChildren(false);
        this.recursive = recursive;
        this.filters = (filters != null) ? new ArrayList<FileObjectFilter>(filters)
                                         : null;
    }

    /**
     */
    public boolean hasNext() {
        if (!upToDate) {
            update();
        }
        return nextObject != null;
    }

    /** 
     */
    public FileObject next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        upToDate = false;
        return nextObject;
    }

    /**
     */
    private void update() {
        assert upToDate == false;
        assert childrenEnum != null;
        do {
            if (childrenEnum.hasMoreElements()) {
                FileObject file = childrenEnum.nextElement();
                if (file.isFolder()) {
                    if (recursive) {
                        processFolder(file);
                    }
                } else {
                    if (FileUtil.isArchiveFile(file)
                            && recursive && searchInArchives) {
                        processFolder(FileUtil.getArchiveRoot(file));
                    }
                    if ((filters == null) || checkFileFilters(file)) {
                        nextObject = file;
                        break;
                    }
                }
            } else {
                assert enums.isEmpty() == filterLists.isEmpty();
                
                nextObject = null;
                
                if (enums.isEmpty()) {
                    childrenEnum = null;
                    continue;
                }
                
                /* pop an element from the stack of children enumerations: */
                childrenEnum = enums.remove(enums.size() - 1);
                
                /* pop an element from the stack of FileObjectFilters: */
                filters = filterLists.remove(filterLists.size() - 1);
                if ((filtersCopy != null)
                        && (filtersCopy.size() != filters.size())) {
                    filtersCopy = null;
                }
            }
        } while (childrenEnum != null);
        
        upToDate = true;
    }
    
    private void processFolder(FileObject folder) {
        
        if (filters != null) {
            final List<FileObjectFilter> subfolderFilters = 
                    checkFolderFilters(folder);
            if (subfolderFilters == null) {
                return;
            }
            filterLists.add(filters);
            if (subfolderFilters.size() != filters.size()) {
                filters = (!subfolderFilters.isEmpty())
                        ? subfolderFilters
                        : null;
            }
        } else {
            filterLists.add(null);
        }
        enums.add(childrenEnum);
        childrenEnum = folder.getChildren(false);
    }

    /**
     * Computes a list of filters to be applied on the folder's children.
     * The current list of filters is used as a base and then each filter
     * is checked with the folder as a parameter.
     * <p>
     * If any of the filters returns <code>DO_NOT_TRAVERSE</code>,
     * <code>the method returns <code>null</code> and no further filters
     * are checked.
     * If a filter returns <code>TRAVERSE_ALL_SUBFOLDERS</code>,
     * the filter is removed from the base as it needs not be applied
     * on the folder's children. The remaining list of filters is returned
     * as a result.
     *
     * @param  folder  folder to compute children filters for
     * @return  list of filters to be applied on the folder's children;
     *          or <code>null</code> if the folder should not be traversed
     */
    private List<FileObjectFilter> checkFolderFilters(final FileObject folder) {
        assert folder.isFolder();
        assert filters != null;
        
        if (filtersCopy == null) {
            filtersCopy = new ArrayList<FileObjectFilter>(filters);
        }
        
        List<FileObjectFilter> result = filtersCopy;
        cycle:
        for (Iterator<FileObjectFilter> i = result.iterator(); i.hasNext(); ) {
            FileObjectFilter filter = i.next();
            final int traverseCommand = filter.traverseFolder(folder);
            switch (traverseCommand) {
                case FileObjectFilter.TRAVERSE:
                    break;
                case FileObjectFilter.DO_NOT_TRAVERSE:
                    result = null;
                    break cycle;
                case FileObjectFilter.TRAVERSE_ALL_SUBFOLDERS:
                    i.remove();
                    filtersCopy = null;
                    break;
                default:
                    assert false;
                    break;
            }
        }
        
        return result;
    }
    
    /**
     * Checks whether the file passes all of the current
     * {@link #filters}.
     *
     * @param  file  file to be checked
     * @return  <code>true</code> if the file passed all of the filters,
     *          <code>false</code> otherwise
     */
    private boolean checkFileFilters(FileObject file) {
        assert file.isFolder() == false;
        assert filters != null;
        
        for (FileObjectFilter filter : filters) {
            if (!filter.searchFile(file)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}
