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

package org.netbeans.modules.git.ui.history;

import java.awt.Color;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.util.NoContentPanel;
import java.io.File;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.history.SearchHistoryTopComponent.DiffResultsViewFactory;
import org.netbeans.modules.git.ui.history.SummaryView.GitLogEntry;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.history.AbstractSummaryView.SummaryViewMaster.SearchHighlight;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;

import static java.util.Locale.ROOT;

/**
 * Contains all components of the Search History panel.
 *
 * @author Maros Sandor
 */
class SearchHistoryPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener, DocumentListener, ActionListener {

    private final File[]                roots;
    private final SearchCriteriaPanel   criteria;
    
    private Action                  searchAction;
    private SearchExecutor          currentSearch;
    private Search                  currentAdditionalSearch;

    private boolean                 criteriaVisible;
    private boolean                 searchInProgress;
    private List<RepositoryRevision> results;
    private SummaryView             summaryView;    
    private DiffResultsView         diffView;
    private Action nextAction;
    private Action prevAction;
    private final File repository;
    private final ExplorerManager explorerManager;
    
    private static final Icon ICON_COLLAPSED = UIManager.getIcon("Tree.collapsedIcon"); //NOI18N
    private static final Icon ICON_EXPANDED = UIManager.getIcon("Tree.expandedIcon"); //NOI18N
    
    private int showingResults;
    private List<GitLogEntry> logEntries;
    private boolean selectFirstRevision;
    private DiffResultsViewFactory diffViewFactory;
    private boolean searchStarted;
    private String currentBranch;
    private final RepositoryInfo info;
    private final PropertyChangeListener list;

    enum FilterKind {

        ALL(null, NbBundle.getMessage(SearchHistoryPanel.class, "Filter.All")), //NOI18N
        MESSAGE(SearchHighlight.Kind.MESSAGE, NbBundle.getMessage(SearchHistoryPanel.class, "Filter.Message")), //NOI18N
        USER(SearchHighlight.Kind.AUTHOR, NbBundle.getMessage(SearchHistoryPanel.class, "Filter.User")), //NOI18N
        ID(SearchHighlight.Kind.REVISION, NbBundle.getMessage(SearchHistoryPanel.class, "Filter.Commit")), //NOI18N
        FILE(SearchHighlight.Kind.FILE, NbBundle.getMessage(SearchHistoryPanel.class, "Filter.File")); //NOI18N

        private final String label;
        private final SearchHighlight.Kind kind;
        
        FilterKind (SearchHighlight.Kind kind, String label) {
            this.kind = kind;
            this.label = label;
        }
        
        @Override
        public final String toString () {
            return label;
        }
    }
    private final Timer filterTimer;

    /** Creates new form SearchHistoryPanel */
    public SearchHistoryPanel(File repository, RepositoryInfo info, File [] roots, SearchCriteriaPanel criteria) {
        this.roots = roots;
        this.repository = repository;
        this.info = info;
        this.criteria = criteria;
        this.diffViewFactory = new SearchHistoryTopComponent.DiffResultsViewFactory();
        criteriaVisible = true;
        explorerManager = new ExplorerManager ();
        initComponents();
        initializeFilter();
        filterTimer = new Timer(500, this);
        filterTimer.setRepeats(false);
        filterTimer.stop();
        setupComponents();
        info.addPropertyChangeListener(list = WeakListeners.propertyChange(this, info));
        aquaBackgroundWorkaround();
        refreshComponents(true);
    }
    
    private void aquaBackgroundWorkaround() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            jToolBar1.setBackground(color); 
            resultsPanel.setBackground(color); 
            searchCriteriaPanel.setBackground(color); 
            searchCriteriaPanel.setBackground(color); 
            criteria.setBackground(color); 
        }
    }

    public void disableFileChangesOption(boolean b) {
        fileInfoCheckBox.setEnabled(false);
        fileInfoCheckBox.setSelected(false);
    }

    boolean isShowInfo() {
        return fileInfoCheckBox.isSelected();
    }

    void setBranch (String branch) {
        this.currentBranch = branch;
    }

    void setSearchCriteria(boolean b) {
        criteriaVisible = b;
        refreshComponents(false);
    }

    private void enableFilters (boolean enabled) {
        lblFilter.setEnabled(enabled);
        cmbFilterKind.setEnabled(enabled);
        lblFilterContains.setEnabled(enabled);
        txtFilter.setEnabled(enabled);
    }

    private void setupComponents() {
        searchCriteriaPanel.add(criteria);
        searchAction = new AbstractAction(NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "TT_Search")); // NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                executeSearch();
            }
        };
        searchCriteriaPanel.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "search"); // NOI18N
        searchCriteriaPanel.getActionMap().put("search", searchAction); // NOI18N
        bSearch.setAction(searchAction);
        Mnemonics.setLocalizedText(bSearch, NbBundle.getMessage(SearchHistoryPanel.class,  "CTL_Search")); // NOI18N
        
        Dimension d1 = tbSummary.getPreferredSize();
        Dimension d2 = tbDiff.getPreferredSize();
        if (d1.width > d2.width) {
            tbDiff.setPreferredSize(d1);
        }

        nextAction = createJumpAction("Next", bNext, () -> diffView.onNextButton()); // NOI18N
        prevAction = createJumpAction("Prev", bPrev, () -> diffView.onPrevButton()); // NOI18N

        criteria.tfFrom.getDocument().addDocumentListener(this);
        criteria.tfTo.getDocument().addDocumentListener(this);
        
        fileInfoCheckBox.setSelected(GitModuleConfig.getDefault().getShowFileInfo());
        
        criteria.btnSelectBranch.addActionListener(this);
    }

    private Action createJumpAction(String prevOrNext, JButton button, Runnable onActionPerformed) {
        Action mainAction = Actions.forID("System", "org.netbeans.core.actions.Jump"+prevOrNext+"Action"); // NOI18N
        String hotkey = ""; // NOI18N
        if (mainAction != null) {
            KeyStroke ks = (KeyStroke) mainAction.getValue(Action.ACCELERATOR_KEY);
            if (ks != null) {
                hotkey = Actions.keyStrokeToString(ks);
            }
        }
        ImageIcon icon = ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/diff-"+prevOrNext.toLowerCase(ROOT)+".png", false); // NOI18N
        Action callbackAction = new AbstractAction(null, icon) {
            @Override public void actionPerformed(ActionEvent e) {
                onActionPerformed.run();
            }
        };
        callbackAction.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SearchHistoryPanel.class, "CTL_DiffPanel_"+prevOrNext+"_Tooltip", hotkey)); // NOI18N
        button.setAction(callbackAction);
        getActionMap().put("jump"+prevOrNext, callbackAction); // NOI18N
        return callbackAction;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        explorerManager.addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    @Override
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    @NbBundle.Messages("LBL_SearchHistory_NoSearchYet=<No Results - Search Not Started>")
    final void refreshComponents(boolean refreshResults) {
        if (refreshResults) {
            resultsPanel.removeAll();
            if (results == null) {
                if (!searchStarted) {
                    resultsPanel.add(new NoContentPanel(Bundle.LBL_SearchHistory_NoSearchYet()));
                } else if (searchInProgress) {
                    resultsPanel.add(new NoContentPanel(NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_Searching"))); // NOI18N
                } else {
                    String errMessage = currentSearch.getErrorMessage();
                    resultsPanel.add(new NoContentPanel(errMessage == null
                            ? NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistory_NoResults") //NOI18N
                            : errMessage));
                }
            } else {
                if (tbSummary.isSelected()) {
                    if (summaryView == null) {
                        summaryView = new SummaryView(this, logEntries = createLogEntries(results));
                    }
                    resultsPanel.add(summaryView.getComponent());
                    summaryView.requestFocusInWindow();
                } else {
                    if (diffView == null) {
                        diffView = diffViewFactory.createDiffResultsView(this, filter(results));
                    }
                    resultsPanel.add(diffView.getComponent());
                    if (selectFirstRevision) {
                        selectFirstRevision();
                    }
                }
            }
            resultsPanel.revalidate();
            resultsPanel.repaint();
        }
        updateActions();
        fileInfoCheckBox.setVisible(tbSummary.isSelected());
        layoutButton.setVisible(!tbSummary.isSelected());
        bPrev.setVisible(!tbSummary.isSelected());
        bNext.setVisible(!tbSummary.isSelected());
        jSeparator3.setVisible(!tbSummary.isSelected());

        searchCriteriaPanel.setVisible(criteriaVisible);
        bSearch.setVisible(criteriaVisible);
        expandCriteriaButton.setIcon(criteriaVisible ? ICON_EXPANDED : ICON_COLLAPSED);
        if (criteria.getLimit() <= 0) {
            criteria.setLimit(SearchExecutor.UNLIMITTED);
        }
        enableFilters(results != null);
        revalidate();
        repaint();
    }

    private void selectFirstRevision () {
        if (diffView != null && results != null && !results.isEmpty()) {
            diffView.select(results.get(0));
        }
    }
 
    final void updateActions () {
        nextAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isNextEnabled());
        prevAction.setEnabled(!tbSummary.isSelected() && diffView != null && diffView.isPrevEnabled());
    }

    void setResults(List<RepositoryRevision> newResults, int limit) {
        setResults(newResults, false, limit);
    }

    private void setResults(List<RepositoryRevision> newResults, boolean searching, int limit) {
        this.results = newResults;
        this.searchInProgress = searching;
        showingResults = limit;
        if (newResults != null && newResults.size() < limit) {
            showingResults = -1;
        }
        summaryView = null;
        releaseDiff();
        diffView = null;
        refreshComponents(true);
    }

    public File[] getRoots() {
        return roots;
    }

    public SearchCriteriaPanel getCriteria() {
        return criteria;
    }

    void executeSearch() {
        searchStarted = true;
        cancelBackgroundTasks();
        setResults(null, true, -1);
        GitModuleConfig.getDefault().setShowHistoryMerges(criteria.isIncludeMerges());
        if (currentBranch != null) {
            // search history opened with request to work only on current branch
            // did user change this setting and cleared the branch field?
            GitModuleConfig.getDefault().setSearchOnlyCurrentBranchEnabled(criteria.getBranch() != null);
        }
        try {
            currentSearch = new SearchExecutor(this);
            currentSearch.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress", repository)); //NOI18N
        } catch (IllegalArgumentException ex) {
            GitClientExceptionHandler.annotate(ex.getLocalizedMessage());
        }
    }
    
    void cancelBackgroundTasks () {
        if (currentSearch != null) {
            currentSearch.cancel();
        }
        if (currentAdditionalSearch != null) {
            currentAdditionalSearch.cancel();
        }
    }    

    void showDiff (RepositoryRevision.Event... events) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(events);
    }

    public void showDiff(RepositoryRevision container) {
        tbDiff.setSelected(true);
        refreshComponents(true);
        diffView.select(container);
    }
    
    public static int compareRevisions(String r1, String r2) {
        StringTokenizer st1 = new StringTokenizer(r1, "."); // NOI18N
        StringTokenizer st2 = new StringTokenizer(r2, "."); // NOI18N
        for (;;) {
            if (!st1.hasMoreTokens()) {
                return st2.hasMoreTokens() ? -1 : 0;
            }
            if (!st2.hasMoreTokens()) {
                return st1.hasMoreTokens() ? 1 : 0;
            }
            int n1 = Integer.parseInt(st1.nextToken());
            int n2 = Integer.parseInt(st2.nextToken());
            if (n1 != n2) return n2 - n1;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        bSearch = new javax.swing.JButton();
        searchCriteriaPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        tbSummary = new javax.swing.JToggleButton();
        tbDiff = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        layoutButton = new javax.swing.JToggleButton();
        fillerX = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        lblFilter = new javax.swing.JLabel();
        cmbFilterKind = new javax.swing.JComboBox();
        lblFilterContains = new javax.swing.JLabel();
        txtFilter = new javax.swing.JTextField();
        resultsPanel = new javax.swing.JPanel();
        expandCriteriaButton = new org.netbeans.modules.versioning.history.LinkButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 0, 8));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/git/ui/history/Bundle"); // NOI18N
        bSearch.setToolTipText(bundle.getString("TT_Search")); // NOI18N

        searchCriteriaPanel.setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        buttonGroup1.add(tbSummary);
        tbSummary.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(tbSummary, bundle.getString("CTL_ShowSummary")); // NOI18N
        tbSummary.setToolTipText(bundle.getString("TT_Summary")); // NOI18N
        tbSummary.addActionListener(this::onViewToggle);
        jToolBar1.add(tbSummary);

        buttonGroup1.add(tbDiff);
        org.openide.awt.Mnemonics.setLocalizedText(tbDiff, bundle.getString("CTL_ShowDiff")); // NOI18N
        tbDiff.setToolTipText(bundle.getString("TT_ShowDiff")); // NOI18N
        tbDiff.addActionListener(this::onViewToggle);
        jToolBar1.add(tbDiff);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setMaximumSize(new java.awt.Dimension(2, 32767));
        jToolBar1.add(jSeparator2);

        bNext.setIcon(org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/diff-next.png", false));
        jToolBar1.add(bNext);
        bNext.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_NextDifference")); // NOI18N
        bNext.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "ACSD_NextDifference")); // NOI18N

        bPrev.setIcon(org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/diff-prev.png", false));
        jToolBar1.add(bPrev);
        bPrev.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_PrevDifference")); // NOI18N
        bPrev.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "ACSD_PrevDifference")); // NOI18N

        jToolBar1.add(jSeparator3);

        layoutButton.setIcon(org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/switch_layout.png", false));
        layoutButton.setToolTipText(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "TT_SwitchLayout")); // NOI18N
        layoutButton.setFocusable(false);
        layoutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        layoutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        layoutButton.addActionListener(this::layoutButtonActionPerformed);
        jToolBar1.add(layoutButton);

        org.openide.awt.Mnemonics.setLocalizedText(fileInfoCheckBox, org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "LBL_SearchHistoryPanel_AllInfo")); // NOI18N
        fileInfoCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "LBL_TT_SearchHistoryPanel_AllInfo")); // NOI18N
        fileInfoCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        fileInfoCheckBox.addActionListener(this::fileInfoCheckBoxActionPerformed);
        jToolBar1.add(fileInfoCheckBox);
        jToolBar1.add(fillerX);

        org.openide.awt.Mnemonics.setLocalizedText(lblFilter, org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "filterLabel.text")); // NOI18N
        lblFilter.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jToolBar1.add(lblFilter);

        cmbFilterKind.setMaximumSize(new java.awt.Dimension(300, 32767));
        cmbFilterKind.addActionListener(this::cmbFilterKindActionPerformed);
        jToolBar1.add(cmbFilterKind);

        org.openide.awt.Mnemonics.setLocalizedText(lblFilterContains, org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "containsLabel")); // NOI18N
        lblFilterContains.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jToolBar1.add(lblFilterContains);

        txtFilter.setMaximumSize(new java.awt.Dimension(350, 100));
        txtFilter.setPreferredSize(new java.awt.Dimension(350, 23));
        jToolBar1.add(txtFilter);

        resultsPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(expandCriteriaButton, org.openide.util.NbBundle.getMessage(SearchHistoryPanel.class, "CTL_expandCriteriaButton.text")); // NOI18N
        expandCriteriaButton.addActionListener(this::expandCriteriaButtonActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchCriteriaPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
            .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(expandCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(bSearch))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(expandCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchCriteriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bSearch)
                .addGap(9, 9, 9))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void onViewToggle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onViewToggle
        refreshComponents(true);
    }//GEN-LAST:event_onViewToggle

    private void fileInfoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileInfoCheckBoxActionPerformed
        GitModuleConfig.getDefault().setShowFileInfo(fileInfoCheckBox.isSelected());
        if (summaryView != null) {
            summaryView.refreshView();
        }
    }//GEN-LAST:event_fileInfoCheckBoxActionPerformed

    private void expandCriteriaButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandCriteriaButtonActionPerformed
        criteriaVisible = !searchCriteriaPanel.isVisible();
        searchCriteriaPanel.setVisible(criteriaVisible);
        bSearch.setVisible(criteriaVisible);
        expandCriteriaButton.setIcon(criteriaVisible ? ICON_EXPANDED : ICON_COLLAPSED);
    }//GEN-LAST:event_expandCriteriaButtonActionPerformed

    private void cmbFilterKindActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterKindActionPerformed
        EventQueue.invokeLater(() -> {
            if (!cmbFilterKind.isPopupVisible()) {
                txtFilter.requestFocusInWindow();
            }
        });
        if (filterTimer != null && !txtFilter.getText().isBlank()) {
            filterTimer.restart();
        }
    }//GEN-LAST:event_cmbFilterKindActionPerformed

    private void layoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutButtonActionPerformed
        diffView.switchLayout();
    }//GEN-LAST:event_layoutButtonActionPerformed

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged(e);
    }
    
    private void documentChanged (DocumentEvent e) {
        if (e.getDocument() == txtFilter.getDocument()) {
            filterTimer.restart();
        } else {
            validateUserInput();
        }
    }
    
    private void validateUserInput() {
        if (!criteria.validateUserInput()) {
            bSearch.setEnabled(false);
            return;
        }
        bSearch.setEnabled(true);
    }  

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == filterTimer) {
            if (summaryView != null) {
                summaryView.refreshView();
            }
            if (diffView != null) {
                diffView.refreshResults(filter(results));
            }
        } else if (e.getSource() == criteria.btnSelectBranch) {
            BranchSelector selector = new BranchSelector(repository);
            if (selector.open()) {
                criteria.setBranch(selector.getSelectedBranch());
            }
        }
    }  
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton bNext = new javax.swing.JButton();
    final javax.swing.JButton bPrev = new javax.swing.JButton();
    private javax.swing.JButton bSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cmbFilterKind;
    private org.netbeans.modules.versioning.history.LinkButton expandCriteriaButton;
    final javax.swing.JCheckBox fileInfoCheckBox = new javax.swing.JCheckBox();
    private javax.swing.Box.Filler fillerX;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton layoutButton;
    private javax.swing.JLabel lblFilter;
    private javax.swing.JLabel lblFilterContains;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JPanel searchCriteriaPanel;
    private javax.swing.JToggleButton tbDiff;
    private javax.swing.JToggleButton tbSummary;
    private javax.swing.JTextField txtFilter;
    // End of variables declaration//GEN-END:variables

    File getRepository () {
        return repository;
    }

    void release () {
        info.removePropertyChangeListener(list);
        releaseDiff();
    }
    
    private void releaseDiff () {
        if (diffView != null) {
            diffView.cancelBackgroundTasks();
        }
        cancelBackgroundTasks();
    }
    
    List<RepositoryRevision> getResults () {
        return results;
    }

    boolean hasMoreResults () {
        return showingResults > -1;
    }
    
    void getMoreRevisions (PropertyChangeListener callback, int count) {
        if (currentSearch == null) {
            throw new IllegalStateException("No search task active"); //NOI18N
        }
        if (currentAdditionalSearch != null) {
            currentAdditionalSearch.cancel();
        }
        if (count < 0 || showingResults < 0) {
            count = -1;
        } else {
            count += showingResults;
        }
        currentAdditionalSearch = new Search(count);
        currentAdditionalSearch.start(Git.getInstance().getRequestProcessor(repository), repository,
                NbBundle.getMessage(SearchHistoryPanel.class, "MSG_SearchHistoryPanel.GettingMoreRevisions")); //NOI18N
    }

    Collection<SearchHighlight> getSearchHighlights() {
        String filterText = txtFilter.getText().strip();
        Object selectedFilterKind = cmbFilterKind.getSelectedItem();
        if (filterText.isEmpty() || !(selectedFilterKind instanceof FilterKind)) {
            return Collections.emptyList();
        } else if (selectedFilterKind == FilterKind.ALL) {
            return List.of(new SearchHighlight(SearchHighlight.Kind.AUTHOR, filterText),
                           new SearchHighlight(SearchHighlight.Kind.MESSAGE, filterText),
                           new SearchHighlight(SearchHighlight.Kind.REVISION, filterText),
                           new SearchHighlight(SearchHighlight.Kind.FILE, filterText));
        } else {
            return List.of(new SearchHighlight(((FilterKind) selectedFilterKind).kind, filterText));
        }
    }

    private void initializeFilter () {
        DefaultComboBoxModel filterModel = new DefaultComboBoxModel();
        filterModel.addElement(FilterKind.ALL);
        filterModel.addElement(FilterKind.ID);
        filterModel.addElement(FilterKind.MESSAGE);
        filterModel.addElement(FilterKind.USER);
        filterModel.addElement(FilterKind.FILE);
        cmbFilterKind.setModel(filterModel);
        cmbFilterKind.setSelectedItem(FilterKind.ALL);
        txtFilter.getDocument().addDocumentListener(this);
    }

    private List<RepositoryRevision> filter(List<RepositoryRevision> results) {
        FilterKind kind = (FilterKind)cmbFilterKind.getSelectedItem();
        String filter = txtFilter.getText().strip().toLowerCase(ROOT);
        return results.stream()
                      .filter(rev -> applyFilter(rev, kind, filter))
                      .collect(Collectors.toList());
    }

    // TODO record
    private final class CachedFilterResult {
        private final FilterKind kind;
        private final String text;
        private final boolean matches;
        private CachedFilterResult(FilterKind kind, String text, boolean matches) {
            this.kind = kind;
            this.text = text;
            this.matches = matches;
        }
        private boolean isValidFor(FilterKind kind, String text) {
            return this.kind == kind && this.text.equals(text);
        }
    }

    private final Map<RepositoryRevision, CachedFilterResult> filterResultCache = new WeakHashMap<>();

    boolean applyFilter(RepositoryRevision rev) {
        return applyFilter(rev, (FilterKind)cmbFilterKind.getSelectedItem(), txtFilter.getText().strip().toLowerCase(ROOT));
    }

    private boolean applyFilter(RepositoryRevision rev, FilterKind kind, String text) {
        CachedFilterResult result = filterResultCache.get(rev);
        if (result == null || !result.isValidFor(kind, text)) {
            result = new CachedFilterResult(kind, text, applyFilterImpl(rev, kind, text));
            filterResultCache.put(rev, result);
        }
        return result.matches;
    }

    private boolean applyFilterImpl(RepositoryRevision rev, FilterKind kind, String text) {
        GitRevisionInfo log = rev.getLog();
        return text.isEmpty()
            || (allOrEquals(kind, FilterKind.MESSAGE) && log.getFullMessage().toLowerCase(ROOT).contains(text))
            || (allOrEquals(kind, FilterKind.USER) && log.getAuthor().toString().toLowerCase(ROOT).contains(text))
            || (allOrEquals(kind, FilterKind.ID) && (log.getRevision().contains(text) || contains(rev.getBranches(), text) || contains(rev.getTags(), text)))
            || (allOrEquals(kind, FilterKind.FILE) && containsFiles(log, text));
    }

    private static boolean allOrEquals(FilterKind toCheck, FilterKind required) {
        return toCheck == FilterKind.ALL || toCheck == required;
    }

    private boolean containsFiles(GitRevisionInfo log, String text) {
        try {
            return log.getModifiedFiles().values().stream()
                            .anyMatch(f -> f.getRelativePath().toLowerCase(ROOT).contains(text));
        } catch (GitException ex) {
            return false;
        }
    }

    private static boolean contains (GitBranch[] items, String needle) {
        for (GitBranch item : items) {
            if (item.getName() != GitBranch.NO_BRANCH && item.getName().toLowerCase(ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean contains (GitTag[] items, String needle) {
        for (GitTag item : items) {
            if (item.getTagName().toLowerCase(ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private class Search extends GitProgressSupport {
        private final int count;
        private final SearchExecutor executor;

        private Search (int count) {
            this.count = count;
            this.executor = currentSearch;
        }

        @Override
        protected void perform () {
            final List<RepositoryRevision> newResults;
            try {
                newResults = executor.search(count, getClient(), getProgressMonitor());
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                return;
            }
            if (!isCanceled()) {
                EventQueue.invokeLater(() -> {
                    if (!isCanceled()) {
                        Set<String> visibleRevisions = new HashSet<>(results.size());
                        for (RepositoryRevision rev : results) {
                            visibleRevisions.add(rev.getLog().getRevision());
                        }
                        
                        List<RepositoryRevision> toAdd = new ArrayList<>(newResults.size());
                        for (RepositoryRevision rev : newResults) {
                            if (!visibleRevisions.contains(rev.getLog().getRevision())) {
                                toAdd.add(rev);
                            }
                        }
                        results.addAll(toAdd);
                        if (count == -1) {
                            showingResults = -1;
                        } else {
                            showingResults = count;
                        }
                        if (showingResults > newResults.size()) {
                            showingResults = -1;
                        }
                        logEntries = createLogEntries(results);
                        if (diffView != null) {
                            diffView.refreshResults(results);
                        }
                        if (summaryView != null) {
                            summaryView.entriesChanged(logEntries);
                        }
                    }
                });
            }
        }
    }

    List<GitLogEntry> createLogEntries(List<RepositoryRevision> results) {
        List<GitLogEntry> ret = new LinkedList<>();
        for (RepositoryRevision repositoryRevision : results) {
            ret.add(new SummaryView.GitLogEntry(repositoryRevision, this));
        }
        return ret;
    }

    void activateDiffView (boolean selectFirstRevision) {
        tbDiff.setSelected(true);
        this.selectFirstRevision = selectFirstRevision;
        selectFirstRevision();
    }

    /**
     * Sets the factory creating the appropriate DiffResultsView to display.
     * @param fac factory creating the appropriate DiffResultsView to display. If null then a default factory will be created.
     */
    void setDiffResultsViewFactory(SearchHistoryTopComponent.DiffResultsViewFactory fac) {
        if (fac != null) {
            this.diffViewFactory = fac;
        }
    }

}
