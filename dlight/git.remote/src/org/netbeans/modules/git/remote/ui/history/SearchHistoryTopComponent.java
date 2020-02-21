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
package org.netbeans.modules.git.remote.ui.history;

import java.awt.BorderLayout;
import java.util.List;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_NEVER, preferredID="GitRemote.SearchHistoryTopComponent")
public class SearchHistoryTopComponent extends TopComponent {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;
    private final VCSFileProxy[] files;
    private final VCSFileProxy repository;
    private final RepositoryInfo info;
    
    public SearchHistoryTopComponent (VCSFileProxy repository, RepositoryInfo info, VCSFileProxy[] files) {
        this.repository = repository;
        this.info = info;
        this.files = files;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSN_SearchHistoryT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSD_SearchHistoryT_Top_Component")); // NOI18N
        initComponents();
    }
    
    SearchHistoryTopComponent (VCSFileProxy repository, RepositoryInfo info, VCSFileProxy file, DiffResultsViewFactory fac) {
        this(repository, info, new VCSFileProxy[] { file });
        shp.setDiffResultsViewFactory(fac);
    }

    public void search (boolean showCriteria) {
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }

    void setSearchCommitFrom (String commitId) {
        if (commitId != null) {
            scp.tfFrom.setText(commitId);
        }
    }
    
    void setSearchCommitTo (String commitId) {
        if (commitId != null) {
            scp.tfTo.setText(commitId);
        }
    }
    
    void setBranch (String branch) {
        if (branch != null) {
            shp.setBranch(branch);
            if (GitModuleConfig.getDefault().isSearchOnlyCurrentBranchEnabled()) {
                scp.setBranch(branch);
            }
        }
    }

    void activateDiffView (boolean selectFirstRevision) {
        shp.activateDiffView(selectFirstRevision);
    }

    private void initComponents () {
        setLayout(new BorderLayout());
        scp = new SearchCriteriaPanel();
        shp = new SearchHistoryPanel(repository, info, files, scp);
        add(shp);
    }

    @Override
    protected void componentClosed () {
        shp.release();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    /**
     * Provides an initial diff view. To display a specific one, override createDiffResultsView.
     */
    static class DiffResultsViewFactory {
        DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
            return new DiffResultsView(panel, results);
        }
    }
}
