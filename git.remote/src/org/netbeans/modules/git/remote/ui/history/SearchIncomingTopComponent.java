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
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.TopComponent;

@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_NEVER, preferredID="GitRemote.SearchIncomingTopComponent")
@NbBundle.Messages({
    "ACSN_SearchIncomingT_Top_Component=Search Incoming",
    "ACSD_SearchIncomingT_Top_Component=Search Incoming"
})
public class SearchIncomingTopComponent extends TopComponent {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;
    private final VCSFileProxy[] files;
    private final VCSFileProxy repository;
    private final RepositoryInfo info;
    
    public SearchIncomingTopComponent (VCSFileProxy repository, RepositoryInfo info, VCSFileProxy[] files) {
        this.repository = repository;
        this.info = info;
        this.files = files;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchIncomingTopComponent.class, "ACSN_SearchIncomingT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchIncomingTopComponent.class, "ACSD_SearchIncomingT_Top_Component")); // NOI18N
        initComponents();
        scp.setupRemoteSearch(SearchExecutor.Mode.REMOTE_IN);
    }

    public void search (boolean showCriteria) {
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }
    
    void setBranch (String branch) {
        Parameters.notNull("branch", branch);
        shp.setBranch(branch);
        scp.setBranch(branch);
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
}
