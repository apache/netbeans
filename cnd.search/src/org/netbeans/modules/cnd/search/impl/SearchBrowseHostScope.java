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
