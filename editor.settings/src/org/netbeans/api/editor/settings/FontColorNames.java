/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
