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

import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.nodes.Node;

/**
 * Define how search results (and search controls) should be displayed.
 *
 * @param <T> Type of result of file content matching.
 *
 * @author jhavlin
 */
public abstract class SearchResultsDisplayer<T> {

    /** Constructor for subclasses. */
    protected SearchResultsDisplayer() {}

    /**
     * Get component that will be shown in the Search Results window. Is should
     * be created lazily.
     */
    public abstract @NonNull JComponent getVisualComponent();

    /**
     * This method is called when a new matching object is
     * found. It should add representation of this matching object to model of
     * created visual component.
     */
    public abstract void addMatchingObject(@NonNull T object);

    /**
     * Called right after the search was started. Default implementation does
     * nothing.
     */
    public void searchStarted() {
    }

    /**
     * Called right after the search was finished. Default implementation does
     * nothing.
     */
    public void searchFinished() {
    }

    /**
     * Get default displayer that shows results as a tree of nodes.
     *
     * @param helper Helper that returns nodes for matching objects.
     * @param searchComposition Search composition of the displayer is created
     * for.
     * @param presenter Presenter that can be shown to modify search criteria.
     * @param title Title that will be shown in the tab of search results
     * window.
     */
    public static <U> DefaultSearchResultsDisplayer<U> createDefault(
            @NonNull NodeDisplayer<U> helper,
            @NonNull SearchComposition<U> searchComposition,
            @NullAllowed SearchProvider.Presenter presenter,
            @NonNull String title) {

        return new DefaultSearchResultsDisplayer<>(helper, searchComposition,
                presenter, title);
    }

    /**
     * Helper class for transforming matching objects to nodes.
     */
    public abstract static class NodeDisplayer<T> {
        
        /** Constructor for subclasses. */
        protected NodeDisplayer() {}

        public abstract Node matchToNode(T match);

    }

    /**
     * Return title of this displayer. It will be displayed in the tab within
     * search results window.
     */
    public abstract @NonNull String getTitle();

    /**
     * Set node that display information from the search listener.
     *
     * This method is called right after a new displayer is created, before
     * method {@link #getVisualComponent()}.
     *
     * The default implementation does nothing. Override it if you want to add
     * the info node to your UI.
     */
    public void setInfoNode(Node infoNode) {
    }

    /**
     * Called right after the displayer is closed. It should be overriden to
     * release all held resources. The default implementation does nothing.
     */
    public void closed() {
    }
}