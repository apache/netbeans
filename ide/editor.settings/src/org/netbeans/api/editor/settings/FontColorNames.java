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

package org.netbeans.api.editor.settings;

import org.openide.modules.PatchedPublic;

/**
 * Fonts and Colors settings names
 *
 * @author Martin Roskanin
 */
// When introduce new name add also to org.netbeans.modules.editor.lib.ColoringMap.FONT_COLOR_NAMES_COLORINGS;
public final class FontColorNames {

    /** Default coloring for the drawing. */
    public static final String DEFAULT_COLORING = "default"; // NOI18N

    /**
     * Coloring that will be used for line numbers displayed on the left
     * side on the screen.
     */
    public static final String LINE_NUMBER_COLORING = "line-number"; // NOI18N
    
    public static final String INDENT_GUIDE_LINES = "indent-guide-lines"; // NOI18N

    /** Coloring used for guarded blocks */
    public static final String GUARDED_COLORING = "guarded"; // NOI18N

    /**
     * Coloring that will be used for code folding icons displayed in editor
     */
    public static final String CODE_FOLDING_COLORING = "code-folding"; // NOI18N

    /** Coloring that will be used for code folding side bar */
    public static final String CODE_FOLDING_BAR_COLORING = "code-folding-bar"; // NOI18N
    
    /** Coloring used for selection */
    public static final String SELECTION_COLORING = "selection"; // NOI18N

    /** Coloring used for highlight search */
    public static final String HIGHLIGHT_SEARCH_COLORING = "highlight-search"; // NOI18N

    /** Coloring used for incremental search */
    public static final String INC_SEARCH_COLORING = "inc-search"; // NOI18N

    /** Coloring used for block search */
    public static final String BLOCK_SEARCH_COLORING = "block-search"; // NOI18N
    
    /** Coloring used for the status bar */
    public static final String STATUS_BAR_COLORING = "status-bar"; // NOI18N

    /** Coloring used to mark important text in the status bar */
    public static final String STATUS_BAR_BOLD_COLORING = "status-bar-bold"; // NOI18N
    
    /** Coloring used to highlight the row where the caret resides */
    public static final String CARET_ROW_COLORING = "highlight-caret-row"; // NOI18N

    /** 
     * Coloring used for drawing the text limit line (eg. 80 chars boundary). 
     * @since 1.15
     */
    public static final String TEXT_LIMIT_LINE_COLORING = "text-limit-line-color"; //NOI18N

    /** Coloring used for the caret in the insert mode */
    public static final String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"; // NOI18N
    
    /** Coloring used for the caret in the overwrite mode */
    public static final String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"; // NOI18N

    /** Coloring for the documentation popup window, eg. javadoc popup in code completion. */
    public static final String DOCUMENTATION_POPUP_COLORING = "documentation-popup-coloring"; //NOI18N

    @PatchedPublic
    private FontColorNames() {
        // to prevent instantialization
    }
}
