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
