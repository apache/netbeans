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
