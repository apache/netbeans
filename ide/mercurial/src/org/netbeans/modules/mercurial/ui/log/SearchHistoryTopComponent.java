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
package org.netbeans.modules.mercurial.ui.log;

import java.awt.event.ActionEvent;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.util.*;
import java.io.File;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.branch.BranchSelector;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.diff.DiffSetupSource;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.versioning.util.Utils;

/**
 * @author Maros Sandor
 */
public class SearchHistoryTopComponent extends TopComponent implements DiffSetupSource {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;

    public SearchHistoryTopComponent() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSN_SearchHistoryT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSD_SearchHistoryT_Top_Component")); // NOI18N
    }
    
    public SearchHistoryTopComponent(File[] files, String branchName, String revision) {
        this();
        initComponents(files, revision, revision, branchName);
    }

    /**
     * Support for openning file history with a specific DiffResultsView
     * @param file it's history shall be shown
     * @param fac factory creating a specific DiffResultsView - just override its createDiffResultsView method
     */
    SearchHistoryTopComponent(File file, DiffResultsViewFactory fac) {
        this();
        initComponents(new File[] {file}, null, null, ""); //NOI18N
        shp.setDiffResultsViewFactory(fac);
    }

    public void search (boolean showCriteria) {        
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }
    
    public void searchOut() {  
        shp.setOutSearch();
        scp.setTo("");
        shp.setSearchCriteria(false);
        shp.executeSearch();
    }

    public void searchIncoming() {  
        shp.setIncomingSearch();
        scp.setTo("");
        shp.setSearchCriteria(false);
        shp.executeSearch();
    }

    void activateDiffView (boolean selectFirstRevision) {
        shp.activateDiffView(selectFirstRevision);
    }

    private void initComponents(final File[] roots, String from, String to, String branchName) {
        setLayout(new BorderLayout());
        scp = new SearchCriteriaPanel();
        if (from != null){ 
            scp.setFrom(from);
        }
        if (to != null){
            scp.setTo(to);
        }
        shp = new SearchHistoryPanel(roots, scp);
        add(shp);
        shp.setCurrentBranch(branchName);
        if (!HgBranch.DEFAULT_NAME.equals(branchName) && HgModuleConfig.getDefault().isSearchOnBranchEnabled(branchName)) {
            // only for branches other than default
            scp.setBranch(branchName);
        }
        if (roots.length > 0) {
            scp.btnSelectBranch.addActionListener(new BranchSelectorOpener(roots, scp));
        }
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
        if (shp.isIncomingSearch()) {
            return "Hg.IncomingSearchHistoryTopComponent";    // NOI18N
        } else if (shp.isOutSearch()) {
            return "Hg.OutSearchHistoryTopComponent";    // NOI18N
        }
        return "Hg.SearchHistoryTopComponent";    // NOI18N
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
    
    private static class BranchSelectorOpener implements ActionListener {
        private final SearchCriteriaPanel scp;
        private final File root;

        public BranchSelectorOpener (File[] roots, SearchCriteriaPanel scp) {
            this.scp = scp;
            this.root = roots[0];
        }
        
        @Override
        public void actionPerformed (ActionEvent e) {
            scp.btnSelectBranch.setEnabled(false);
            Utils.postParallel(new Runnable() {
                @Override
                public void run () {
                    final String branchName;
                    File repoRoot = Mercurial.getInstance().getRepositoryRoot(root);
                    if (repoRoot == null) {
                        branchName = null;
                    } else {
                        BranchSelector selector = new BranchSelector(repoRoot);
                        if (selector.showGeneralDialog()) {
                            branchName = selector.getBranchName();
                        } else {
                            branchName = null;
                        }
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            scp.btnSelectBranch.setEnabled(true);
                            if (branchName != null) {
                                scp.setBranch(branchName);
                            }
                        }
                    });
                }
            }, 0);
        }
    }
}
