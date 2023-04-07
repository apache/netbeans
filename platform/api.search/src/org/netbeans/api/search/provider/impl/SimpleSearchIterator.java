/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.search.provider.impl;

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

/**
 *
 * @author Marian Petras
 */
public class SimpleSearchIterator extends AbstractFileObjectIterator {

    /**
     * current enumeration of children
     */
    private Enumeration<? extends FileObject> childrenEnum;
    /**
     * stack of the ancestor folders' children enumerations
     */
    private final Stack<Enumeration<? extends FileObject>> enums
            = new Stack<>();   //unsynced stack
    /**
     * whether value of {@link #nextObject} is up-to-date
     */
    private boolean upToDate = false;
    /**
     * <code>FileObject</code> to be returned the next time method {@link #next()}
     * is called
     */
    private FileObject nextObject;
    /**
     * Search root
     */
    private FileObject rootFile;

    private SearchListener listener;

    private FileNameMatcher fileNameMatcher;

    private FilterHelper filterHelper;

    private boolean searchInArchives;

    private AtomicBoolean terminated;

    /**
     */
    public SimpleSearchIterator(FileObject root,
            SearchScopeOptions options,
            List<SearchFilterDefinition> filters,
            SearchListener listener,
            AtomicBoolean terminated) {
        this.rootFile = root;
        if (rootFile.isFolder()) {
            this.childrenEnum = sortEnum(rootFile.getChildren(false));
        } else {
            this.childrenEnum = Enumerations.singleton(rootFile);
        }
        this.listener = listener;
        this.fileNameMatcher = FileNameMatcher.create(options);
        this.searchInArchives = options.isSearchInArchives();
        this.filterHelper = new FilterHelper(filters, options);
        this.terminated = terminated;
    }

    /**
     */
    @Override
    public boolean hasNext() {
        assert !EventQueue.isDispatchThread();
        if (!upToDate) {
            update();
        }
        return nextObject != null;
    }

    /**
     */
    @Override
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
                    processFolder(file);
                } else if (searchInArchives && isArchive(file)) {
                    listener.directoryEntered(file.getPath());
                    FileObject archRoot = FileUtil.getArchiveRoot(file);
                    processFolder(archRoot);
                } else {
                    if (fileNameMatcher.pathMatches(file)
                            && checkFileFilters(file)) {
                        nextObject = file;
                        break;
                    }
                }
            } else {

                nextObject = null;

                if (enums.isEmpty()) {
                    childrenEnum = null;
                    continue;
                }

                /*
                 * pop an element from the stack of children enumerations:
                 */
                childrenEnum = enums.pop();

                /*
                 * pop an element from the stack of FileObjectFilters:
                 */
                filterHelper.popStack();
            }
        } while (childrenEnum != null);

        upToDate = true;
    }

    private void processFolder(FileObject folder) {

        if (!terminated.get() && checkFolderFilters(folder)) {
            listener.directoryEntered(folder.getPath());
            enums.push(childrenEnum);
            childrenEnum = sortEnum(folder.getChildren(false));
        }
    }

    /**
     * Computes a list of filters to be applied on the folder's children. The
     * current list of filters is used as a base and then each filter is checked
     * with the folder as a parameter. <p> If any of the filters returns
     * <code>DO_NOT_TRAVERSE</code>,
     * <code>the method returns
     * <code>null</code> and no further filters are checked. If a filter returns
     * <code>TRAVERSE_ALL_SUBFOLDERS</code>, the filter is removed from the base
     * as it needs not be applied on the folder's children.
     *
     * @param folder folder to compute children filters for
     * @return True if directory can be searched, false if it cannot.
     */
    private boolean checkFolderFilters(final FileObject folder) {
        assert folder.isFolder();
        return filterHelper.directoryAllowed(folder, listener);
    }

    /**
     * Checks whether the file passes all of the current {@link #filters}.
     *
     * @param file file to be checked
     * @return
     * <code>true</code> if the file passed all of the filters,
     * <code>false</code> otherwise
     */
    private boolean checkFileFilters(FileObject file) {
        assert file.isFolder() == false;
        return filterHelper.fileAllowed(file, listener);
    }

    private boolean isArchive(FileObject fo) {
        return isArchiveExtension(fo.getExt()) && FileUtil.isArchiveFile(fo);
    }

    private boolean isArchiveExtension(String ext) {
        return "zip".equalsIgnoreCase(ext) || "jar".equalsIgnoreCase(ext);
    }

    /**
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sort enumeration by fila names;
     *
     */
    static <T extends FileObject> Enumeration<T> sortEnum(
            Enumeration<T> enm) {

        TreeMap<String, T> map = new TreeMap<>();
        while (enm.hasMoreElements()) {
            T o = enm.nextElement();
            map.put(o.getNameExt(), o);
        }
        final Iterator<T> iterator = map.values().iterator();
        return new Enumeration<T>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return iterator.next();
            }
        };
    }
}
