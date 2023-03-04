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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.netbeans.modules.search.Constants;
import org.netbeans.modules.search.ui.AbstractSearchResultsPanel.RootNode;
import org.netbeans.spi.search.provider.SearchComposition;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.netbeans.spi.search.provider.SearchResultsDisplayer;
import org.netbeans.spi.search.provider.SearchResultsDisplayer.NodeDisplayer;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public class DefaultSearchResultsPanel<T> extends AbstractSearchResultsPanel {

    private List<T> matchingObjects = new ArrayList<>();
    private final NodeDisplayer<T> nodeDisplayer;
    private ResultsNode resultsNode;
    private OutlineView outlineView;

    public DefaultSearchResultsPanel(
            SearchResultsDisplayer.NodeDisplayer<T> nodeDisplayer,
            SearchComposition<T> searchComposition,
            Presenter searchProviderPresenter) {

        super(searchComposition, searchProviderPresenter);
        this.resultsNode = new ResultsNode();
        this.nodeDisplayer = nodeDisplayer;
        resultsNode.update();
        outlineView = new OutlineView(UiUtils.getText(
                "BasicSearchResultsPanel.outline.nodes"));              //NOI18N
        outlineView.getOutline().setRootVisible(false);
        initExpandButton();
        getContentPanel().add(outlineView);
    }

    private void initExpandButton() {
        btnExpand.addActionListener((ActionEvent e) -> {
            getOutlineView().expandNode(resultsNode);
            for (Node n : resultsNode.getChildren().getNodes(true)) {
                toggleExpand(n, btnExpand.isSelected());
            }
        });
        btnExpand.setEnabled(true);
    }

    public void addMatchingObject(T object) {
        matchingObjects.add(object);
        resultsNode.update();
        afterMatchingNodeAdded();
    }

    /**
     * Root node shows search info and statistics.
     */
    private class ResultsNode extends AbstractNode {

        private ResultsNodeChildren children;

        public ResultsNode() {
            this(new ResultsNodeChildren());
        }

        public ResultsNode(ResultsNodeChildren children) {
            super(children);
            this.children = children;
        }

        void update() {
            setDisplayName(NbBundle.getMessage(Constants.class,
                    "TXT_RootSearchedNodes", //NOI18N
                    matchingObjects.size()));
            children.update();
        }
    }

    /**
     * Children of the root node represent matching object that have been found
     * so far.
     */
    private class ResultsNodeChildren extends Children.Keys<T> {

        @Override
        protected Node[] createNodes(T key) {
            return new Node[]{nodeDisplayer.matchToNode(key)};
        }

        void update() {
            this.setKeys(matchingObjects);
        }
    }

    @Override
    public void searchFinished() {
        super.searchFinished();
        resultsNode.setDisplayName(NbBundle.getMessage(Constants.class,
                "TEXT_MSG_FOUND_X_NODES", //NOI18N
                matchingObjects.size()));
    }

    /**
     * Get {@link OutlineView} used for displaying result nodes.
     */
    @Override
    public OutlineView getOutlineView() {
        return outlineView;
    }

    /**
     * Get button for moving to the previous result item. It is hidden by
     * default. You can set it visible and add a {@link ActionListener}.
     */
    public JButton getButtonPrevious() {
        return btnPrev;
    }

    /**
     * Get button for moving to the next result item. It is hidden by default.
     * You can set it visible and add a {@link ActionListener}.
     */
    public JButton getButtonNext() {
        return btnNext;
    }

    /**
     * Get button for expanding/collapsing of the result tree. It is hidden by
     * default. You can set it visible and add a {@link ActionListener}.
     */
    public JToggleButton getButtonExpand() {
        return btnExpand;
    }

    /**
     * Add a custom button to the toolbar.
     */
    @Override
    public void addButton(AbstractButton button) {
        super.addButton(button);
    }

    public void setInfoNode(Node infoNode) {
        Node root = new RootNode(resultsNode, infoNode);
        getExplorerManager().setRootContext(root);
        getOutlineView().expandNode(resultsNode);
    }

    @Override
    protected boolean isDetailNode(Node n) {
        return true;
    }
}
