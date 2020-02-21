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
package org.netbeans.modules.subversion.remote.ui.history;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.util.*;
import java.awt.BorderLayout;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.ui.diff.DiffSetupSource;
import org.netbeans.modules.subversion.remote.ui.diff.Setup;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * 
 */
public class SearchHistoryTopComponent extends TopComponent implements DiffSetupSource {

    private SearchHistoryPanel shp;

    public SearchHistoryTopComponent() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSN_SearchHistoryT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSD_SearchHistoryT_Top_Component")); // NOI18N
    }
    
    public SearchHistoryTopComponent(Context context) {
        this(context, null, null);
    }

    SearchHistoryTopComponent(VCSFileProxy[] files) {
        this();
        initComponents(files, null, null);
    }
    
    public SearchHistoryTopComponent(Context context, Date from, Date to) {
        this();
        initComponents(context.getRootFiles(), from, to);
    }

    /**
     * Support for openning file history with a specific DiffResultsView
     * @param file it's history shall be shown
     * @param fac factory creating a specific DiffResultsView - just override its createDiffResultsView method
     */
    SearchHistoryTopComponent(VCSFileProxy file, DiffResultsViewFactory fac) {
        this();
        initComponents(new VCSFileProxy[] {file}, null, null);
        shp.setDiffResultsViewFactory(fac);
    }

    public SearchHistoryTopComponent(SVNUrl repositoryUrl, VCSFileProxy localRoot, long revision) {
        this();
        initComponents(repositoryUrl, localRoot, revision);
    }

    public void search (boolean showCriteria) {        
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }

    void activateDiffView (boolean selectFirstRevision) {
        shp.activateDiffView(selectFirstRevision);
    }
    
    private void initComponents(SVNUrl repositoryUrl, VCSFileProxy localRoot, long revision) {
        setLayout(new BorderLayout());
        SearchCriteriaPanel scp = new SearchCriteriaPanel(repositoryUrl);
        scp.setFrom(Long.toString(revision));
        scp.setTo(Long.toString(revision));
        shp = new SearchHistoryPanel(repositoryUrl, localRoot, scp);
        add(shp);
    }

    private void initComponents(VCSFileProxy[] roots, Date from, Date to) {
        setLayout(new BorderLayout());
        SearchCriteriaPanel scp = new SearchCriteriaPanel(roots);
        if (from != null) {
            scp.setFrom(SearchExecutor.simpleDateFormat.format(from));
        }
        if (to != null) {
            scp.setTo(SearchExecutor.simpleDateFormat.format(to));
        }
        shp = new SearchHistoryPanel(roots, scp);
        add(shp);
    }

    @Override
    public int getPersistenceType(){
       return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    protected void componentClosed() {
       shp.windowClosed();
       super.componentClosed();
    }
    
    @Override
    protected String preferredID(){
       return "Svn.SearchHistoryTopComponent";    // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public Collection<Setup> getSetups() {
        return shp.getSetups();
    }

    @Override
    public String getSetupDisplayName() {
        return getDisplayName();
    }

    /**
     * Provides an initial diff view. To display a specific one, override createDiffResultsView.
     */
    public static class DiffResultsViewFactory {
        DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
            return new DiffResultsView(panel, results);
        }
    }
}
