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
package org.netbeans.modules.cnd.search.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.cnd.search.ui.DirectoryChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 */
public final class SearchBrowseHostScope {

    private static FileObject root;
    private final ExecutionEnvironment env;
    private SearchScopeDefinition browseScope = new BrowseHostScopeDefinition();
    private SearchScopeDefinition lastScope = new LastSearchBrowseHostScope();

    public SearchBrowseHostScope(ExecutionEnvironment env) {
        this.env = env;
    }

    final class BrowseHostScopeDefinition extends SearchScopeDefinition {

        private final SearchInfo searchInfo;

        public BrowseHostScopeDefinition() {
            searchInfo = SearchInfoUtils.createForDefinition(new BrowseHostInfoDefinition());
        }

        @Override
        public String getTypeId() {
            return SearchBrowseHostScope.class.getName();
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SearchBrowseHostScope.class, "LBL_BrowseHostScopeDefinitionName", env.getDisplayName()); // NOI18N
        }

        @Override
        public boolean isApplicable() {
            return ConnectionManager.getInstance().isConnectedTo(env);
        }

        @Override
        public SearchInfo getSearchInfo() {
            return searchInfo;
        }

        @Override
        public int getPriority() {
            return 701;
        }

        @Override
        public void clean() {
        }

        @Override
        public void selected() {
            chooseRoots();
            notifyListeners();
        }

        private FileObject[] chooseRoots() {
            FileObject dir = DirectoryChooser.chooseDirectory(WindowManager.getDefault().getMainWindow(), env, null);
            if (dir != null) {
                root = dir;
                return new FileObject[]{root};
            }
            return new FileObject[0];
        }

        private class BrowseHostInfoDefinition extends SearchInfoDefinition {

            private SearchInfo delegate;

            @Override
            public boolean canSearch() {
                return true;
            }

            @Override
            public Iterator<FileObject> filesToSearch(SearchScopeOptions options, SearchListener listener, AtomicBoolean terminated) {
                return getDelegate().getFilesToSearch(options, listener, terminated).iterator();
            }

            @Override
            public List<SearchRoot> getSearchRoots() {
                return getDelegate().getSearchRoots();
            }

            private synchronized SearchInfo getDelegate() {
                if (delegate == null) {
                    delegate = createDelegate();
                }
                return delegate;
            }

            private SearchInfo createDelegate() {
                FileObject[] fileObjects = chooseRoots();
                return SearchInfoUtils.createSearchInfoForRoots(fileObjects);
            }
        }
    }

    private static class LastSearchBrowseHostScope extends SearchScopeDefinition {

        @Override
        public String getTypeId() {
            return SearchBrowseHostScope.class.getName();
        }

        @Override
        public String getDisplayName() {
            if (root != null) {
                return NbBundle.getMessage(SearchBrowseHostScope.class, "LBL_BrowseHostScopeBrowseName", // NOI18N
                        root.getNameExt(),
                        FileSystemProvider.getExecutionEnvironment(root).getDisplayName());
            } else {
                return NbBundle.getMessage(SearchBrowseHostScope.class, "LBL_NoSelection"); // NOI18N
            }
        }

        @Override
        public boolean isApplicable() {
            return root != null;
        }

        @Override
        public SearchInfo getSearchInfo() {
            return SearchInfoUtils.createSearchInfoForRoots(new FileObject[]{root});
        }

        @Override
        public int getPriority() {
            return 700;
        }

        @Override
        public void clean() {
        }
    }

    public SearchScopeDefinition getBrowseScope() {
        return browseScope;
    }

    public SearchScopeDefinition getLastScope() {
        return lastScope;
    }
}
