/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.api.search.provider.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchFilterDefinition.FolderResult;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper that checks whether files are filtered or not.
 *
 * It combines default filters (defined by search scope, e.g. sharability
 * filter) and custom filters (defined by search options, e.g. filter for ignore
 * list).
 *
 * Instance are statefull. As it is possible to skip filtering by a filter in
 * whole subtree by usin {@link FolderResult#TRAVERSE_ALL_SUBFOLDERS}, stack of
 * filters has to be used.
 *
 * @author jhavlin
 */
class FilterHelper {

    private List<SearchFilterDefinition> filters = new ArrayList<SearchFilterDefinition>();
    private boolean active;
    private Stack<List<SearchFilterDefinition>> stack;

    /**
     * Create helper for file filtering.
     *
     * @param defaultFilters List of default filters. Can be null or empty.
     * @param options Search options. Cannot be empty.
     */
    public FilterHelper(List<SearchFilterDefinition> defaultFilters,
            SearchScopeOptions options) {

        if (options == null) {
            throw new NullPointerException("Options cannot be null.");  //NOI18N
        }

        /*
         * Add default filters only if we do not want to search in generated
         * resources.
         */
        if (defaultFilters != null) {
            for (SearchFilterDefinition fof : defaultFilters) {
                if (options.isSearchInGenerated()) {
                    if (fof != SearchInfoDefinitionFactory.SHARABILITY_FILTER) {
                        filters.add(fof);
                    }
                } else {
                    filters.add(fof);
                }
            }
        }
        for (SearchFilterDefinition fof : options.getFilters()) {
            filters.add(fof);
        }
        stack = new Stack<List<SearchFilterDefinition>>();
        stack.push(filters);
        /*
         * If helper is not active, filters are not used at all and all files
         * are searched.
         */
        active = !filters.isEmpty();
    }

    /**
     * Check if file
     * <code>f</code> can be searched.
     */
    public boolean fileAllowed(File f, SearchListener listener) {
        if (active) {
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                return fileAllowed(fo, listener);
            } else {
                listener.generalError(
                        new RuntimeException(
                        "No FileObject for file " + f.getPath()));      //NOI18N
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Check if file object
     * <code>fo</code> can be searched.
     */
    public boolean fileAllowed(FileObject fo, SearchListener listener) {
        if (active) {
            for (SearchFilterDefinition fl : stack.peek()) {
                if (!fl.searchFile(fo)) {
                    listener.fileSkipped(fo, fl, null);
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * Check if directory
     * <code>d</code> can be searched.
     *
     * @return True if directory can be searched, false if it cannot.
     */
    public boolean directoryAllowed(File d, SearchListener listener) {
        if (active) {
            FileObject fo = FileUtil.toFileObject(d);
            if (fo != null) {
                return directoryAllowed(fo, listener);
            } else {
                listener.generalError(
                        new RuntimeException(
                        "No FileObject for directory " + d.getPath())); //NOI18N
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Check if directory
     * <code>d</code> can be searched.
     *
     * @return True if directory can be searched, false if it cannot.
     */
    public boolean directoryAllowed(FileObject d, SearchListener listener) {
        if (active) {
            // remaining filters for the next level
            List<SearchFilterDefinition> remainingFilters = null;
            boolean result = true;
            cycle:
            for (SearchFilterDefinition filter : stack.peek()) {
                final FolderResult traverseCommand = filter.traverseFolder(d);
                switch (traverseCommand) {
                    case TRAVERSE:
                        break;
                    case DO_NOT_TRAVERSE:
                        result = false;
                        listener.fileSkipped(d, filter, null);
                        break cycle;
                    case TRAVERSE_ALL_SUBFOLDERS:
                        if (remainingFilters == null) {
                            remainingFilters =
                                    new LinkedList<SearchFilterDefinition>(
                                    stack.peek());
                        }
                        remainingFilters.remove(filter);
                        break;
                    default:
                        assert false;
                        break;
                }
            }
            if (result) {
                if (remainingFilters != null) {
                    stack.push(remainingFilters);
                } else {
                    stack.push(stack.peek()); // use the same list
                }
            }
            return result;
        } else {
            return true;
        }
    }

    /**
     * Pop stack of filter lists. It should be called when the whole directory
     * was searched.
     */
    public void popStack() {
        if (active) {
            stack.pop();
        }
    }
}
