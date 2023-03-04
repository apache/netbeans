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
package org.netbeans.api.search;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.search.BasicSearchProvider;
import org.netbeans.modules.search.ResultView;
import org.netbeans.modules.search.SearchPanel;
import org.netbeans.spi.search.provider.SearchProvider;

/**
 * This class enables users to show search dialog and start searches
 * programatically.
 *
 * @author jhavlin
 */
public final class SearchControl {

    private SearchControl() {
        // hiding default constructor
    }

    /**
     * Shows dialog for basic search task.
     *
     * If options are not specified (null is passed), previous or default values
     * are used.
     */
    public static void openFindDialog(
            @NullAllowed SearchPattern searchPattern,
            @NullAllowed SearchScopeOptions searchScopeOptions,
            @NullAllowed Boolean useIgnoreList,
            @NullAllowed String scopeId) {

        SearchControl.openFindDialog(BasicSearchProvider.createBasicPresenter(
                false, searchPattern, null, false, searchScopeOptions,
                useIgnoreList, scopeId));
    }

    /**
     * Shows dialog for basic replace task.
     *
     * If options are not specified (null is passed), previous or default values
     * are used.
     */
    public static void openReplaceDialog(
            @NullAllowed SearchPattern searchPattern,
            @NullAllowed String replaceString,
            @NullAllowed Boolean preserveCase,
            @NullAllowed SearchScopeOptions searchScopeOptions,
            @NullAllowed Boolean useIgnoreList,
            @NullAllowed String scopeId) {

        SearchControl.openReplaceDialog(
                BasicSearchProvider.createBasicPresenter(true, searchPattern,
                replaceString, preserveCase, searchScopeOptions, useIgnoreList,
                scopeId));
    }

    /**
     * Show find dialog with a concrete presenter for one of providers.
     *
     * @param presenter Presenter to use, possibly initialized with proper
     * values.
     */
    public static void openFindDialog(SearchProvider.Presenter presenter) {
        SearchControl.openDialog(false, presenter);
    }

    /**
     * Show replace dialog with a concrete presenter for one of providers.
     *
     * @param presenter Presenter to use, possibly initialized with proper
     * values.
     */
    public static void openReplaceDialog(SearchProvider.Presenter presenter) {
        SearchControl.openDialog(true, presenter);
    }

    /**
     * Open dialog with one explicit presenter.
     */
    private static void openDialog(boolean replaceMode,
            SearchProvider.Presenter presenter) {
        SearchPanel current = SearchPanel.getCurrentlyShown();
        if (current != null) {
            current.close();
        }
        if (ResultView.getInstance().isFocused()) {
            ResultView.getInstance().markCurrentTabAsReusable();
        }
        new SearchPanel(replaceMode, presenter).showDialog();
    }

    /**
     * Start basic search for specified parameters.
     *
     * @param scopeId Identifier of search scope (e.g. "main project", 
     * "open projects", "node selection", "browse"). If not specified, the 
     * default one is used.
     * @throws IllegalArgumentException if neither non-trivial file name pattern
     * nor non-empty text search pattern is specified.
     */
    public static void startBasicSearch(
            @NonNull SearchPattern searchPattern,
            @NonNull SearchScopeOptions searchScopeOptions,
            @NullAllowed String scopeId) throws IllegalArgumentException {
        BasicSearchProvider.startSearch(searchPattern, searchScopeOptions,
                scopeId);
    }
}
