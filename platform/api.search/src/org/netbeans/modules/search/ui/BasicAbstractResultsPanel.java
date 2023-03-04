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
package org.netbeans.modules.search.ui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.BasicSearchCriteria;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.Manager;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.PrintDetailsTask;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.ResultView;
import org.netbeans.modules.search.TextDetail;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public abstract class BasicAbstractResultsPanel
        extends AbstractSearchResultsPanel implements PropertyChangeListener {

    @StaticResource
    private static final String SHOW_DETAILS_ICON =
            "org/netbeans/modules/search/res/search.gif";               //NOI18N
    @StaticResource
    private static final String FOLDER_VIEW_ICON =
            "org/netbeans/modules/search/res/logical_view.png";         //NOI18N
    @StaticResource
    private static final String FLAT_VIEW_ICON =
            "org/netbeans/modules/search/res/file_view.png";            //NOI18N
    private static final String MODE_FLAT = "flat";                     //NOI18N
    private static final String MODE_TREE = "tree";                     //NOI18N
    protected ResultModel resultModel;
    protected JToggleButton btnTreeView;
    protected JToggleButton btnFlatView;
    protected JButton showDetailsButton;
    protected boolean details;
    protected BasicComposition composition;
    protected final ResultsOutlineSupport resultsOutlineSupport;
    private NodeListener resultsNodeAdditionListener;
    private volatile boolean finished = false;
    protected static final boolean isMacLaf =
            "Aqua".equals(UIManager.getLookAndFeel().getID());          //NOI18N
    protected static final Color macBackground =
            UIManager.getColor("NbExplorerView.background");            //NOI18N

    public BasicAbstractResultsPanel(ResultModel resultModel,
            BasicComposition composition, boolean details,
            ResultsOutlineSupport resultsOutlineSupport) {

        super(composition, composition.getSearchProviderPresenter());
        this.composition = composition;
        this.details = details;
        this.resultModel = resultModel;
        this.resultsOutlineSupport = resultsOutlineSupport;
        getExplorerManager().setRootContext(
                resultsOutlineSupport.getRootNode());
        initButtons();
        initResultNodeAdditionListener();
        if (MODE_TREE.equals(
                FindDialogMemory.getDefault().getResultsViewMode())) {
            resultsOutlineSupport.setFolderTreeMode();
        }
        setRootDisplayName(NbBundle.getMessage(ResultView.class,
                "TEXT_SEARCHING___"));                                  //NOI18N
        initAccessibility();
        this.resultModel.addPropertyChangeListener(
                ResultModel.PROP_RESULTS_EDIT, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // update the root node after change in model
        if (finished) {
            setFinalRootNodeText();
        } else {
            updateRootNodeText();
        }
    }

    public void update() {
        if (details && btnExpand.isVisible() && !btnExpand.isEnabled()) {
            btnExpand.setEnabled(resultModel.size() > 0);
        }
        EventQueue.invokeLater(this::updateShiftButtons);
        resultsOutlineSupport.update();
    }

    protected void initButtons() {
        final FindDialogMemory memory = FindDialogMemory.getDefault();
        btnTreeView = new JToggleButton();
        btnTreeView.setEnabled(true);
        btnTreeView.setIcon(ImageUtilities.loadImageIcon(FOLDER_VIEW_ICON,
                true));
        btnTreeView.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_TREE_VIEW"));                              //NOI18N
        btnTreeView.setSelected(
                MODE_TREE.equals(memory.getResultsViewMode()));
        btnTreeView.addActionListener((ActionEvent e) -> toggleView(!btnTreeView.isSelected()));
        btnFlatView = new JToggleButton();
        btnFlatView.setEnabled(true);
        btnFlatView.setIcon(ImageUtilities.loadImageIcon(FLAT_VIEW_ICON,
                true));
        btnFlatView.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_FLAT_VIEW"));                              //NOI18N
        btnFlatView.setSelected(!btnTreeView.isSelected());
        btnFlatView.addActionListener((ActionEvent e) -> toggleView(btnFlatView.isSelected()));
        addButton(btnTreeView);
        addButton(btnFlatView);
        if (!details) {
            btnPrev.setVisible(false);
            btnNext.setVisible(false);
            btnExpand.setVisible(false);
            return;
        }
        btnExpand.setSelected(true);
        btnExpand.addActionListener((ActionEvent e) -> toggleExpandNodeChildren(btnExpand.isSelected()));
        showDetailsButton = new JButton();
        showDetailsButton.setEnabled(false);
        showDetailsButton.setIcon(ImageUtilities.loadImageIcon(
                SHOW_DETAILS_ICON, true));
        showDetailsButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_FILL"));                                   //NOI18N
        showDetailsButton.addActionListener((ActionEvent e) -> fillOutput());
        showDetailsButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ResultView.class,
                "ACS_TEXT_BUTTON_FILL"));                               //NOI18N
        addButton(showDetailsButton);
    }

    private void toggleView(boolean flat) {
        FindDialogMemory memory = FindDialogMemory.getDefault();
        if (flat) {
            resultsOutlineSupport.setFlatMode();
            memory.setResultsViewMode(MODE_FLAT);
        } else {
            resultsOutlineSupport.setFolderTreeMode();
            memory.setResultsViewMode(MODE_TREE);
        }
        btnTreeView.setSelected(!flat);
        btnFlatView.setSelected(flat);
        try {
            getExplorerManager().setSelectedNodes(new Node[]{
                        resultsOutlineSupport.getResultsNode()});
        } catch (PropertyVetoException ex) {
        }
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);

        AccessibleContext accessCtx;
        OutlineView outlineView = resultsOutlineSupport.getOutlineView();

        accessCtx = outlineView.getHorizontalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_HorizontalScrollbar"));          //NOI18N

        accessCtx = outlineView.getVerticalScrollBar().getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_VerticalScrollbar"));            //NOI18N

        accessCtx = outlineView.getAccessibleContext();
        accessCtx.setAccessibleName(
                bundle.getString("ACSN_ResultTree"));                   //NOI18N
        accessCtx.setAccessibleDescription(
                bundle.getString("ACSD_ResultTree"));                   //NOI18N
    }

    private void toggleExpandNodeChildren(boolean expand) {
        Node resultsNode = resultsOutlineSupport.getResultsNode();
        enableExpandListener(false);
        try {
            for (Node n : resultsNode.getChildren().getNodes()) {
                toggleExpand(n, expand);
            }
        } finally {
            enableExpandListener(true);
        }
    }

    @Override
    public void searchFinished() {
        super.searchFinished();
        this.finished = true;
        if (details && resultModel.size() > 0 && showDetailsButton != null) {
            showDetailsButton.setEnabled(true);
        }
        setFinalRootNodeText();
    }

    /**
     * Send search details to output window.
     */
    public void fillOutput() {
        Manager.getInstance().schedulePrintTask(
                new PrintDetailsTask(resultModel.getMatchingObjects(),
                composition.getBasicSearchCriteria()));
    }

    public void addMatchingObject(MatchingObject mo) {
        resultsOutlineSupport.addMatchingObject(mo);
        updateRootNodeText();
        afterMatchingNodeAdded();
    }

    @Override
    public final OutlineView getOutlineView() {
        return resultsOutlineSupport.getOutlineView();
    }

    protected final void setFinalRootNodeText() {

        int resultSize = resultModel.size();

        if (resultModel.wasLimitReached()) {
            setRootDisplayName(
                    NbBundle.getMessage(
                    ResultView.class,
                    "TEXT_MSG_FOUND_X_NODES_LIMIT", //NOI18N
                    resultSize,
                    resultModel.getTotalDetailsCount())
                    + ' ' + resultModel.getLimitDisplayName());         //NOI18N
            return;
        }

        String baseMsg;
        if (resultSize == 0) {
            baseMsg = NbBundle.getMessage(ResultView.class,
                    "TEXT_MSG_NO_NODE_FOUND");                          //NOI18N
        } else {
            String bundleKey;
            Object[] args;
            if (resultModel.isSearchAndReplace()) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_REPLACE";           //NOI18N
                args = new Object[4];
            } else if (resultModel.canHaveDetails()) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_FULLTEXT";          //NOI18N
                args = new Object[3];
            } else {
                bundleKey = "TEXT_MSG_FOUND_X_NODES";                   //NOI18N
                args = new Object[1];
            }
            args[0] = resultModel.size();
            if (args.length > 1) {
                args[1] = resultModel.getTotalDetailsCount();
            }
            if (args.length > 2) {
                BasicSearchCriteria bsc = composition.getBasicSearchCriteria();
                args[2] = UiUtils.escapeHtml(bsc.getTextPatternExpr());
                if (args.length > 3) {
                    args[3] = UiUtils.escapeHtml(bsc.getReplaceExpr());
                }
            }
            baseMsg = NbBundle.getMessage(ResultView.class, bundleKey, args);
        }
        String exMsg = resultModel.getExceptionMsg();
        String msg = exMsg == null ? baseMsg
                : baseMsg + " (" + exMsg + ")";      //NOI18N
        setRootDisplayName(msg);
    }

    private void setRootDisplayName(String displayName) {
        resultsOutlineSupport.setResultsNodeText(displayName);
    }

    protected void updateRootNodeText() {
        Integer objectsCount = resultModel.size();
        if (details) {
            Integer detailsCount = resultModel.getTotalDetailsCount();
            setRootDisplayName(NbBundle.getMessage(ResultView.class,
                    "TXT_RootSearchedNodesFulltext", //NOI18N
                    objectsCount, detailsCount));
        } else {
            setRootDisplayName(NbBundle.getMessage(ResultView.class,
                    "TXT_RootSearchedNodes", objectsCount));            //NOI18N
        }
    }

    private void enableExpandListener(boolean enable) {
        resultsOutlineSupport.setExpansionListenerEnabled(enable);
    }

    private void initResultNodeAdditionListener() {
        resultsNodeAdditionListener = new NodeAdapter() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                if (btnExpand != null) {
                    for (final Node n : ev.getDelta()) {
                        if (btnExpand.isSelected()) {
                            EventQueue.invokeLater(() -> {
                                enableExpandListener(false);
                                try {
                                    toggleExpand(n, true);
                                } finally {
                                    enableExpandListener(true);
                                }
                            });
                        }
                        addChildAdditionListener(n);
                    }
                }
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                if (btnExpand != null) {
                    for (Node removedChild : ev.getDelta()) {
                        removeChildAdditionListener(removedChild);
                    }
                }
            }
        };
        resultsOutlineSupport.getResultsNode().getChildren().getNodes(true);
        resultsOutlineSupport.getResultsNode().addNodeListener(
                resultsNodeAdditionListener);
    }

    private void addChildAdditionListener(Node addedNode) {
        for (Node n : addedNode.getChildren().getNodes(true)) {
            addChildAdditionListener(n);
        }
        addedNode.addNodeListener(resultsNodeAdditionListener);

    }

    private void removeChildAdditionListener(Node removedNode) {
        for (Node n : removedNode.getChildren().getNodes(true)) {
            removeChildAdditionListener(n);
        }
        removedNode.removeNodeListener(resultsNodeAdditionListener);
    }

    @Override
    public boolean requestFocusInWindow() {
        return getOutlineView().requestFocusInWindow();
    }

    @Override
    protected boolean isDetailNode(Node n) {
        return n.getLookup().lookup(TextDetail.class) != null;
    }

    @Override
    protected void onDetailShift(Node next) {
        TextDetail textDetail = next.getLookup().lookup(
                TextDetail.class);
        if (textDetail != null) {
            textDetail.showDetail(TextDetail.DH_GOTO);
        }
    }

    public void closed() {
        resultsOutlineSupport.closed();
    }

    protected boolean isFinished() {
        return finished;
    }
}
