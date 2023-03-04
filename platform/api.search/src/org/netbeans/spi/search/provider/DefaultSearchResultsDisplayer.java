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
package org.netbeans.spi.search.provider;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.search.ui.DefaultSearchResultsPanel;
import org.netbeans.spi.search.provider.SearchProvider.Presenter;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.util.Parameters;

/**
 * Default search results displayer.
 *
 * @author jhavlin
 * @since api.search/1.1
 */
public final class DefaultSearchResultsDisplayer<U>
        extends SearchResultsDisplayer<U> {

    private static final ResultNodeShiftSupport DEFAULT_NODE_SHIFT_SUPPORT =
            new TrivialResultNodeShiftSupport();
    private final SearchResultsDisplayer.NodeDisplayer<U> helper;
    private final SearchComposition<U> searchComposition;
    private final Presenter presenter;
    private final String title;
    private ResultNodeShiftSupport shiftSupport = DEFAULT_NODE_SHIFT_SUPPORT;
    private DefaultSearchResultsPanel<U> panel = null;

    DefaultSearchResultsDisplayer(
            SearchResultsDisplayer.NodeDisplayer<U> helper,
            SearchComposition<U> searchComposition,
            Presenter presenter, String title) {
        this.helper = helper;
        this.searchComposition = searchComposition;
        this.presenter = presenter;
        this.title = title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized JComponent getVisualComponent() {
        if (panel == null) {
            panel = new DefaultSearchResultsPanel<U>(helper,
                    searchComposition, presenter) {
                @Override
                protected void onDetailShift(Node n) {
                    shiftSupport.relevantNodeSelected(n);
                }

                @Override
                protected boolean isDetailNode(Node n) {
                    return shiftSupport.isRelevantNode(n);
                }
            };
        }
        return panel;
    }

    private DefaultSearchResultsPanel<U> getPanel() {
        if (panel == null) {
            getVisualComponent();
        }
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMatchingObject(U object) {
        Parameters.notNull("object", object);
        panel.addMatchingObject(object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted() {
        super.searchStarted();
        panel.searchStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished() {
        super.searchFinished();
        panel.searchFinished();
    }

    /**
     * Get outline view. You can alter it to display additional node properties,
     * or set custom cell renderer.
     *
     * @return OutlineView used in the results displayer.
     */
    public @NonNull OutlineView getOutlineView() {
        return panel.getOutlineView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInfoNode(Node infoNode) {
        getPanel().setInfoNode(infoNode);
    }

    /**
     * Set a custom {@link ResultNodeShiftSupport} for this displayer.
     */
    public void setResultNodeShiftSupport(
            ResultNodeShiftSupport resultNodeShiftSupport) {
        this.shiftSupport = resultNodeShiftSupport;
    }

    /**
     * Add a button to the results displayer toolbar. It will be shown right
     * above the stop button.
     *
     * @param button Button to add.
     */
    public void addButton(@NonNull AbstractButton button) {
        Parameters.notNull("button", button);
        getPanel().addButton(button);
    }

    /**
     * Class definining which nodes should be selected when Previous or Next
     * button is pressed and what action should be performed.
     */
    public abstract static class ResultNodeShiftSupport {

        /**
         * Method that checks whether a node should be selected when Next or
         * Previous button is pressed.
         *
         * @param node Node to check.
         * @return True if {@code node} is a node that Next and Previous buttons
         * should consider, false it if is structural or informational node
         * only.
         */
        public abstract boolean isRelevantNode(Node node);

        /**
         * This method is called when a relevant node is selected by pressing
         * Next or Previous button.
         *
         * Clients should implement this method to perform an appropriate
         * action, e.g. to show the relevant part of found file in editor.
         *
         * @param node Node that has been just selected.
         */
        public abstract void relevantNodeSelected(Node node);
    }

    /**
     * Trivial implementation of {@link ResultNodeShiftSupport} that consider
     * only leaf nodes as relevant and that do nothing when a relevant node is
     * selected.
     */
    private static class TrivialResultNodeShiftSupport
            extends ResultNodeShiftSupport {

        @Override
        public boolean isRelevantNode(Node node) {
            if (node == null) {
                return false;
            } else {
                Node parent = node.getParentNode();
                return node.isLeaf() && parent != null
                        && parent.getParentNode() != null;
            }
        }

        @Override
        public void relevantNodeSelected(Node node) {
        }
    }
}
