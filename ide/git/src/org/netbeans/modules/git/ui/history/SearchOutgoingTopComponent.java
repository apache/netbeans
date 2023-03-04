/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.git.ui.history;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.io.File;
import java.awt.BorderLayout;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.openide.util.Parameters;

@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_NEVER, preferredID="Git.SearchOutgoingTopComponent")
@NbBundle.Messages({
    "ACSN_SearchOutgoingT_Top_Component=Search Incoming",
    "ACSD_SearchOutgoingT_Top_Component=Search Incoming"
})
public class SearchOutgoingTopComponent extends TopComponent {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;
    private final File[] files;
    private final File repository;
    private final RepositoryInfo info;
    
    public SearchOutgoingTopComponent (File repository, RepositoryInfo info, File[] files, boolean searchInContext) {
        this.repository = repository;
        this.info = info;
        this.files = files;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchOutgoingTopComponent.class, "ACSN_SearchOutgoingT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchOutgoingTopComponent.class, "ACSD_SearchOutgoingT_Top_Component")); // NOI18N
        initComponents();
        scp.setupRemoteSearch(SearchExecutor.Mode.REMOTE_OUT, searchInContext);
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
