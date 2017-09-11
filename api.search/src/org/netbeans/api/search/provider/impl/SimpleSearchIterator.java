/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            = new Stack<Enumeration<? extends FileObject>>();   //unsynced stack
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
     * as it needs not be applied on the folder's children. The remaining list
     * of filters is returned as a result.
     *
     * @param folder folder to compute children filters for
     * @return list of filters to be applied on the folder's children; or
     * <code>null</code> if the folder should not be traversed
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

        TreeMap<String, T> map = new TreeMap<String, T>();
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
