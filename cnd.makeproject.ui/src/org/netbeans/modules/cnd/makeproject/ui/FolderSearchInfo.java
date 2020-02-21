/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.FileNameMatcher;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder.FileObjectNameMatcher;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class FolderSearchInfo extends SearchInfoDefinition {

    private final Folder folder;

    FolderSearchInfo(Folder folder) {
        this.folder = folder;
    }

    @Override
    public boolean canSearch() {
        return true;
    }

    @Override
    public List<SearchRoot> getSearchRoots() {
        Set<FileObject> set = folder.getAllItemsAsFileObjectSet(false, new FileObjectNameMatcher() {

            @Override
            public boolean pathMatches(FileObject fileObject) {
                return true;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }
        });
        Set<FileObject> roots = new HashSet<>();
        for (FileObject fo : set) {
            FileObject parent = fo.getParent();
            if (parent == null) {
                continue;
            }
            FileObject curr = parent;
            boolean found = false;
            while(curr != null) {
                if (roots.contains(curr)) {
                    found = true;
                    break;
                }
                curr = curr.getParent();
            }
            if (!found) {
                List<FileObject> list = new ArrayList<>(roots);
                roots.clear();
                for (FileObject fo2 : list) {
                     FileObject parent2 = fo2.getParent();
                     FileObject curr2 = parent2;
                     boolean found2 = false;
                     while(curr2 != null) {
                        if (parent.equals(curr2)) {
                            found2 = true;
                            break;
                        }
                        curr2 = curr2.getParent();
                    }
                     if (!found2) {
                         roots.add(fo2);
                     }
                }
                roots.add(parent);
            }
        }
        List<SearchRoot> res = new ArrayList<>();
        roots.forEach((fo) -> {
            res.add(new SearchRoot(fo, null));
        });
        return res;
    }

    @Override
    public Iterator<FileObject> filesToSearch(final SearchScopeOptions options, SearchListener listener, final AtomicBoolean terminated) {
        return folder.getAllItemsAsFileObjectSet(false, new FileObjectNameMatcherImpl(options, terminated)).iterator();
    }

    public static final class FileObjectNameMatcherImpl implements FileObjectNameMatcher {

        private final SearchScopeOptions options;
        private final AtomicBoolean terminated;
        private final FileNameMatcher delegate;

        public FileObjectNameMatcherImpl(SearchScopeOptions options, AtomicBoolean terminated) {
            this.options = options;
            this.terminated = terminated;
            delegate = FileNameMatcher.create(options);
        }

        @Override
        public boolean pathMatches(FileObject fileObject) {
            if (delegate.pathMatches(fileObject)) {
                    for (SearchFilterDefinition filter : options.getFilters()) {
                        if (!filter.searchFile(fileObject)) {
                            return false;
                        }
                    }
                    return true;
            }
            return false;
        }

        @Override
        public boolean isTerminated() {
            return terminated.get();
        }
    };
}
