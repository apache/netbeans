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
package org.netbeans.api.search.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SearchScopeDefinitionProvider;

/**
 * Class containing methods for creating controller objects for GUI components
 * and adjusting their properties so that they can be used in search forms of
 * search dialog.
 *
 * @author jhavlin
 */
public class ComponentUtils {

    private ComponentUtils() {
        // hiding default constructor
    }

    /**
     * Adjust a {@link JComboBox} to act as component for selecting file name
     * pattern, and return a controller object for interacting with it.
     *
     * Such ajdusted combo box is useful if you need a valid file name pattern
     * for {@link SearchScopeOptions} that you want to pass to file object
     * iterator of a {@link SearchInfo}.
     *
     * You can bind this combo box with scope options panel to toggle its
     * regular-expresion mode.
     *
     * @see #adjustPanelForOptions(JPanel, boolean, FileNameController)
     *
     * @param jComboBox Freshly created component that will be adjusted.
     * @return Controller for modified {@code jComboBox}.
     */
    @SuppressWarnings("rawtypes")
    public static @NonNull FileNameController adjustComboForFileName(
            @NonNull JComboBox jComboBox) {
        return new FileNameController(jComboBox);
    }

    /**
     * Adjust a {@link JComboBox} to act as component for selecting search
     * scope, and return a controller object for interacting with it.
     *
     * @see ScopeController#getSearchInfo()
     *
     * @param jComboBox Freshly created component that will be modified.
     * @param preferredScopeId Preferred search scope ID (e.g. "open projects",
     * "main projects", "node selection").
     * @param extraSearchScopes Extra search scopes that should be visible among
     * standard search scopes created by {@link SearchScopeDefinitionProvider}s.
     * @return Controller for modified {@code jComboBox}.
     */
    public static @NonNull ScopeController adjustComboForScope(
            @SuppressWarnings("rawtypes") @NonNull JComboBox jComboBox,
            @NullAllowed String preferredScopeId,
            @NonNull SearchScopeDefinition... extraSearchScopes) {
        return new ScopeController(jComboBox,
                preferredScopeId == null
                ? FindDialogMemory.getDefault().getScopeTypeId()
                : preferredScopeId,
                extraSearchScopes);
    }

    /**
     * Adjust an empty panel for specifying search scope options. Several
     * checkboxes and other controls will be addded to it, and its layout can be
     * altered.
     *
     * Such ajdusted panel is useful if you want to let users customize options
     * for {@link SearchScopeOptions} that you want to pass to file object
     * iterator of a {@link SearchInfo}.
     *
     * @see ScopeOptionsController#getSearchScopeOptions()
     *
     * @param jPanel Empty (with no child components) panel to adjust.
     * @param searchAndReplace True if options for search-and-replace mode
     * should be shown, false to show options for search-only mode.
     * @param fileNameController File-name combo box that will be bound to this
     * settings panel.
     * @return Controller for adjusted {@code jPanel} with controls for setting
     * search options (search in archives, search in generated sources, use
     * ignore list, treat file name pattern as regular expression matching file
     * path).
     */
    public static @NonNull ScopeOptionsController adjustPanelForOptions(
            @NonNull JPanel jPanel, boolean searchAndReplace,
            @NonNull FileNameController fileNameController) {
         return new ScopeOptionsController(jPanel, fileNameController,
                searchAndReplace);
    }

    /**
     * Adjust two empty panels for specifying search scope options. Several
     * checkboxes and other controls will be addded to them, and their layout
     * can be altered.
     *
     * Such ajdusted panels are useful if you want to let users customize
     * options for {@link SearchScopeOptions} that you want to pass to file
     * object iterator of a {@link SearchInfo}.
     *
     * @see ScopeOptionsController#getSearchScopeOptions()
     *
     * @param scopePanel Empty (with no child components) panel to adjust for
     * controls related to search scope (search in archives, search in generated
     * sources).
     * @param fileNamePanel Empty (with no child components) panel to adjust for
     * controls related to file name pattern (regular expression, ignore list).
     * @param searchAndReplace True if options for search-and-replace mode
     * should be shown, false to show options for search-only mode.
     * @param fileNameController File-name combo box that will be bound to this
     * settings panel.
     * @return Controller for adjusted {@code JPanel}s with controls for setting
     * search options (search in archives, search in generated sources, use
     * ignore list, treat file name pattern as regular expression matching file
     * path).
     *
     * @since api.search/1.12
     */
    public static @NonNull ScopeOptionsController adjustPanelsForOptions(
            @NonNull JPanel scopePanel, @NonNull JPanel fileNamePanel,
            boolean searchAndReplace,
            @NonNull FileNameController fileNameController) {
        return new ScopeOptionsController(scopePanel, fileNamePanel,
                fileNameController, searchAndReplace);
    }

    /**
     * Adjust a {@link JComboBox} to act as component for selecting search text
     * pattern, and return a controller object for interacting with it.
     *
     * You can bind this combo box with some abstract buttons (usually check
     * boxes) to set pattern options.
     *
     * @param jComboBox Freshly created component that will be adjusted.
     * @return Controller for modified {@code jComboBox}.
     * @since api.search/1.1
     */
    public static @NonNull SearchPatternController adjustComboForSearchPattern(
            @SuppressWarnings("rawtypes") @NonNull JComboBox jComboBox) {
        return new SearchPatternController(jComboBox);
    }
}
