/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
