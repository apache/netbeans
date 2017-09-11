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


/** The names of SimpleValuesSettings.
 *
 *  @author Martin Roskanin. Miloslav Metelka
 */
public final class SimpleValueNames {

    /**
     * Number of spaces to draw when the '\t' character
     * is found in the text. Better said when the drawing-engine
     * finds a '\t' character it computes the next multiple
     * of TAB_SIZE and continues drawing from that position.
     * Values: java.lang.Integer instances
     */
    public static final String TAB_SIZE = "tab-size"; // NOI18N

    /**
     * Whether expand typed tabs to spaces. The number of spaces to substitute
     * per one typed tab is determined by SPACES_PER_TAB setting.
     * Values: java.lang.Boolean instances
     */
    public static final String EXPAND_TABS = "expand-tabs"; // NOI18N

    /**
     * How many spaces substitute per one typed tab. This parameter has
     * effect only when EXPAND_TABS setting is set to true.
     * This parameter has no influence on how
     * the existing tabs are displayed.
     * Values: java.lang.Integer instances
     */
    public static final String SPACES_PER_TAB = "spaces-per-tab"; // NOI18N

    /**
     * Shift-width says how many spaces should the formatter use
     * to indent the more inner level of code. This setting is independent of <tt>TAB_SIZE</tt>
     * and <tt>SPACES_PER_TAB</tt>.
     * Values: java.lang.Integer instances
     */
    public static final String INDENT_SHIFT_WIDTH = "indent-shift-width"; // NOI18N

    /**
     * The type of the editor caret in the insert mode.
     * <p>The available types are:
     * <ul>
     * <li><code>line-caret</code> - default 2point caret
     * <li><code>thin-line-caret</code> - swing-like thin caret
     * <li><code>block-caret</code> - a block covering a whole character
     * </ul>
     * 
     * Values: java.lang.String instances
     * @see org.netbeans.editor.BaseCaret
     */
    public static final String CARET_TYPE_INSERT_MODE = "caret-type-insert-mode"; // NOI18N

    /**
     * The type of the editor caret in the overwrite mode.
     * <p>The available types are:
     * <ul>
     * <li><code>line-caret</code> - default 2point caret
     * <li><code>thin-line-caret</code> - swing-like thin caret
     * <li><code>block-caret</code> - a block covering a whole character
     * </ul>
     * 
     * Values: java.lang.String instances
     * @see org.netbeans.editor.BaseCaret
     */
    public static final String CARET_TYPE_OVERWRITE_MODE = "caret-type-overwrite-mode"; // NOI18N

    /** Width of caret when <code>thick-line-caret</code> is selected
     * Values: java.lang.Integer instances
     * @since 1.21
     */
    public static final String THICK_CARET_WIDTH = "thick-caret-width"; // NOI18N
    
    /**
     * If true, the insert mode caret will be italicized if the underlying font is italic.
     * Values: java.lang.Boolean instances
     */
    public static final String CARET_ITALIC_INSERT_MODE = "caret-italic-insert-mode"; // NOI18N

    /**
     * If true, the overwrite mode caret will be italicized if the underlying font is italic.
     * Values: java.lang.Boolean instances
     */
    public static final String CARET_ITALIC_OVERWRITE_MODE = "caret-italic-overwrite-mode"; // NOI18N

    /**
     * Caret color for insert mode.
     * Values: java.awt.Color instances
     * @deprecated Use FontColorNames#CARET_INSERT_MODE_COLORING.
     */
    @Deprecated
    public static final String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"; // NOI18N

    /**
     * Caret color for overwrite mode.
     * Values: java.awt.Color instances
     * @deprecated Use FontColorNames#CARET_OVERWRITE_MODE_COLORING.
     */
    @Deprecated
    public static final String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"; // NOI18N

    /**
     * Caret blink rate in milliseconds.
     * Values: java.lang.Integer
     */
    public static final String CARET_BLINK_RATE = "caret-blink-rate"; // NOI18N

    /**
     * Whether to display line numbers on the left part of the screen.
     * Values: java.lang.Boolean instances
     */
    public static final String LINE_NUMBER_VISIBLE = "line-number-visible"; // NOI18N

    /**
     * How much should the view jump when scrolling goes off the screen.
     * Insets are used so that it can be specified for each direction specifically.
     * Each inset value can be positive or negative. The positive value means
     * the number of lines for the top and the bottom and the number of characters
     * for the left and the right. The negative value means percentage of the editor
     * component height for the top and the bottom and percentage of the editor
     * component width for the left and the right.
     * Values: java.awt.Insets instances
     */
    public static final String SCROLL_JUMP_INSETS = "scroll-jump-insets"; // NOI18N

    /**
     * How much space must be reserved in each direction for the find operation.
     * It's here to ensure the found information will be visible in some
     * context around it.
     * Insets are used so that it can be specified for each direction specifically.
     * Each inset value can be positive or negative. The positive value means
     * the number of lines for the top and the bottom and the number of characters
     * for the left and the right. The negative value means percentage of the editor
     * component height for the top and the bottom and percentage of the editor
     * component width for the left and the right.
     * Values: java.awt.Insets instances
     */
    public static final String SCROLL_FIND_INSETS = "scroll-find-insets"; // NOI18N

    /**
     * Margin for the editor component
     * Values: java.awt.Insets instances
     */
    public static final String MARGIN = "margin"; // NOI18N

    /**
     * Width of the margin on the left side of the text just after the line-number bar.
     * Values: java.awt.Integer instances
     */
    public static final String TEXT_LEFT_MARGIN_WIDTH = "text-left-margin-width"; // NOI18N

    /**
     * Float constant by which the height of the character obtained from
     * the font is multiplied. It defaults to 1.
     * Values: java.lang.Float instances
     */
    public static final String LINE_HEIGHT_CORRECTION = "line-height-correction"; // NOI18N
    

    /**
     * Whether status bar should be visible or not.
     * Values: java.lang.Boolean instances
     */
    public static final String STATUS_BAR_VISIBLE = "status-bar-visible"; // NOI18N

    /**
     * Delay for updating information about caret in the status bar.
     * Values: java.lang.Integer instances
     */
    public static final String STATUS_BAR_CARET_DELAY = "status-bar-caret-delay"; // NOI18N

    /**
     * Whether the line displaying the text limit should be displayed.
     * Values: java.lang.Boolean instances
     */
    public static final String TEXT_LIMIT_LINE_VISIBLE = "text-limit-line-visible"; // NOI18N

    /**
     * Which color should be used for the line showing the text limit.
     * Values: java.awt.Color instances
     * @deprecated Use FontColorNames#TEXT_LIMIT_LINE_COLORING
     */
    @Deprecated
    public static final String TEXT_LIMIT_LINE_COLOR = "text-limit-line-color"; // NOI18N

    /**
     * After how many characters the text limit line should be displayed.
     * Values: java.awt.Integer instances
     */
    public static final String TEXT_LIMIT_WIDTH = "text-limit-width"; // NOI18N
    
    /**
     * Determines if and what kind of line wrapping algorithm should be used in the editor.
     * Values: java.lang.String instances
     * <ul>
     * <li>none
     * <li>words
     * <li>chars
     * </ul>
     * @since 1.29
     */
    public static final String TEXT_LINE_WRAP = "text-line-wrap"; //NOI18N

    /**
     * Enable/Disable code folding 
     * Values: java.lang.Boolean instances
     */
    public static final String CODE_FOLDING_ENABLE = "code-folding-enable"; //NOI18N
    
    /** 
     * Collapse initial comment.
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"; //NOI18N
    /** 
     * Collapse method bodies.
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"; //NOI18N
    
    /** 
     * Collapse inner classes.
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"; //NOI18N
    
    /** 
     * Collapse imports or includes section.
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"; //NOI18N
    
    /** 
     * Collapse documentation (eg. javadoc).
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"; //NOI18N

    /**
     * Collapse tags or other non java code blocks.
     * @since 1.19
     */
    public static final String CODE_FOLDING_COLLAPSE_TAGS = "code-folding-collapse-tags"; //NOI18N

    /**
     * Highlight the row where the caret currently is. The ExtCaret must be used.
     * Values: java.lang.Boolean 
     */
    // XXX: currently not used
    public static final String HIGHLIGHT_CARET_ROW = "highlight-caret-row"; // NOI18N

    /**
     * Highlight the matching brace (if the caret currently stands after the brace).
     * The ExtCaret must be used.
     * Values: java.lang.Boolean 
     */
    // XXX: currently not used
    public static final String HIGHLIGHT_MATCH_BRACE = "highlight-match-brace"; // NOI18N
    
    /**
     * Whether the code completion window should popup automatically.
     * Values: java.lang.Boolean
     */
    public static final String COMPLETION_AUTO_POPUP = "completion-auto-popup"; // NOI18N

    /**
     * Whether the code completion query search will be case  sensitive
     * Values: java.lang.Boolean
     */
    public static final String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"; // NOI18N

    /**
     * Whether the code completion sorting will be natural
     * Values: java.lang.Boolean
     */
    public static final String COMPLETION_NATURAL_SORT = "completion-natural-sort"; // NOI18N
    
    /**
     * Whether perform instant substitution, if the search result contains only one item
     * Values: java.lang.Boolean
     */
    public static final String COMPLETION_INSTANT_SUBSTITUTION = "completion-instant-substitution"; // NOI18N

    /**
     * The delay after which the completion window is shown automatically.
     * Values: java.lang.Integer
     */
    public static final String COMPLETION_AUTO_POPUP_DELAY = "completion-auto-popup-delay"; // NOI18N

    /**
     * The minimum size of the completion pane component.
     * Values: java.awt.Dimension
     */
    public static final String COMPLETION_PANE_MIN_SIZE = "completion-pane-min-size"; // NOI18N

    /**
     * The maximum size of the completion pane component.
     * Values: java.awt.Dimension
     */
    public static final String COMPLETION_PANE_MAX_SIZE = "completion-pane-max-size"; // NOI18N
    
    /**
     * Whether to complete pair characters such as [], (), {}, ''  and tags
     * Values: java.lang.Boolean
     * @since 1.18
     */
    public static final String COMPLETION_PAIR_CHARACTERS = "pair-characters-completion"; // NOI18N
    
    /**
     * Background color of javaDoc popup window 
     *  Values: java.awt.Color
     * @deprecated Use FontColorNames#DOCUMENTATION_POPUP_COLORING
     */
    @Deprecated
    public static final String JAVADOC_BG_COLOR = "javadoc-bg-color"; //NOI18N
    
    /**
     * The delay after which the javaDoc window is shown automatically.
     *   Values: java.lang.Integer
     */
    public static final String JAVADOC_AUTO_POPUP_DELAY = "javadoc-auto-popup-delay"; //NOI18N
    
    /**
     * The preferred size of javaDoc popup window
     *   Values: java.awt.Dimension
     */ 
    public static final String JAVADOC_PREFERRED_SIZE = "javadoc-preferred-size"; //NOI18N

    /**
     * Whether the javaDoc window should popup automatically.
     * Values: java.lang.Boolean
     */
    public static final String JAVADOC_AUTO_POPUP = "javadoc-auto-popup"; // NOI18N
    
    /**
     * Whether show deprecated members in code completion popup window
     * Values: java.lang.Boolean
     */
    public static final String SHOW_DEPRECATED_MEMBERS = "show-deprecated-members"; // NOI18N

    /**
     * Modifiers for which the hyperlinks should be enabled.
     * @since 1.15
     */
    public static final String HYPERLINK_ACTIVATION_MODIFIERS = "hyperlink-activation-modifiers"; //NOI18N
    
    /**
     * Modifiers for which the alternative hyperlinks should be enabled.
     * @since 1.24
     */
    public static final String ALT_HYPERLINK_ACTIVATION_MODIFIERS = "alt-hyperlink-activation-modifiers"; //NOI18N

    /** 
     * Whether popup menu will be displayed on mouse right-click or not.
     * It's set to true by default.
     * Values: java.lang.Boolean
     * @since 1.15
     */
    public static final String POPUP_MENU_ENABLED = "popup-menu-enabled"; // NOI18N
    
    /**
     * @since 1.15
     */
    public static final String TOOLBAR_VISIBLE_PROP = "toolbarVisible"; // NOI18N

    /**
     * Whether javadoc popup should be displayed to the right of CC popup
     * @since 1.22
     */
    public static final String JAVADOC_POPUP_NEXT_TO_CC = "javadoc-popup-next-to-cc"; // NOI18N

    /**
     * Whether to display non-printable characters (eg. space, tab and new-line).
     * Values: java.lang.Boolean instances
     * @since 1.30
     */
    public static final String NON_PRINTABLE_CHARACTERS_VISIBLE = "non-printable-characters-visible"; //NOI18N

    /**
     * Determines whether to remove trailing whitespace when saving files and how exactly to do that.
     * Values: java.lang.String instances
     * <ul>
     * <li>never
     * <li>always
     * <li>modified-lines
     * </ul>
     * @since 1.33
     */
    public static final String ON_SAVE_REMOVE_TRAILING_WHITESPACE = "on-save-remove-trailing-whitespace"; //NOI18N

    /**
     * Determines whether and how a document being saved should be reformatted.
     * Values: java.lang.String instances
     * <ul>
     * <li>never
     * <li>always
     * <li>modified-lines
     * </ul>
     * @since 1.44
     */
    public static final String ON_SAVE_REFORMAT = "on-save-reformat"; //NOI18N

    /**
     * Determines whether use on-save settings for all languages (global mime-type "")
     * or specific settings for a mime-type.
     * Values: java.lang.Boolean instances
     * @since 1.44
     */
    public static final String ON_SAVE_USE_GLOBAL_SETTINGS = "on-save-use-global-settings"; //NOI18N

    /**
     * Determines editor search type.
     * Values: java.lang.String instances
     * <ul>
     * <li>default
     * <li>closing
     * </ul>
     */
    public static final String EDITOR_SEARCH_TYPE = "editor-search-type"; //NOI18N
    
    /**
     * Show brace matching outline in the sidebar of the editor.
     * Values: Boolean instances.
     * Default: true
     */
    public static final String BRACE_SHOW_OUTLINE = "editor-brace-outline"; // NOI18N

    /**
     * Show tooltips if the brace start line is hidden (outside the screen).
     * Values: Boolean instances.
     * Default: true
     */
    public static final String BRACE_FIRST_TOOLTIP = "editor-brace-first-tooltip"; // NOI18N

    @PatchedPublic
    private SimpleValueNames() {
        // to prevent instantialization
    }
}
