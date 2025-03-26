/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.lib2;

import org.netbeans.api.editor.settings.SimpleValueNames;

/**
 * This class contains settings names copied over from SettingsNames and ExtSettingsNames.
 * It exists merely to allow editor infrastructure to use these constants for
 * backwards compatibility reasons without having to depend on editor.deprecated.pre61settings module.
 * 
 * The SettingsNames, ExtSettingsNames classes do not use string literals, but they refer
 * directly to this class.
 * 
 * Rules:
 * This class should not contain names of settings that belong to the API category 'public'.
 * Such names ought to be listed in SimpleValueNames class. The names for settings
 * from other categories (devel, friend, private, deprecated) may be listed here,
 * but generally 'devel', 'friend' settings should eventually end up in SimpleValueNames too.
 * Fields with names of deprecated settings should be deprecated too.
 * 
 */
public final class EditorPreferencesKeys {

    private EditorPreferencesKeys() {
        //no-op
    }
    
    // -----------------------------------------------------------------------
    // --- from SettingsNames
    // -----------------------------------------------------------------------

    public static final String CARET_BLINK_RATE = SimpleValueNames.CARET_BLINK_RATE;
    public static final @Deprecated String CARET_COLOR_INSERT_MODE = SimpleValueNames.CARET_COLOR_INSERT_MODE;
    public static final @Deprecated String CARET_COLOR_OVERWRITE_MODE = SimpleValueNames.CARET_COLOR_OVERWRITE_MODE;
    public static final String CARET_ITALIC_INSERT_MODE = SimpleValueNames.CARET_ITALIC_INSERT_MODE;
    public static final String CARET_ITALIC_OVERWRITE_MODE = SimpleValueNames.CARET_ITALIC_OVERWRITE_MODE;
    public static final String CARET_TYPE_INSERT_MODE = SimpleValueNames.CARET_TYPE_INSERT_MODE;
    public static final String CARET_TYPE_OVERWRITE_MODE = SimpleValueNames.CARET_TYPE_OVERWRITE_MODE;
    public static final String THICK_CARET_WIDTH = SimpleValueNames.THICK_CARET_WIDTH;
    public static final String CODE_FOLDING_ENABLE = SimpleValueNames.CODE_FOLDING_ENABLE;
    public static final String EXPAND_TABS = SimpleValueNames.EXPAND_TABS;
    public static final String HYPERLINK_ACTIVATION_MODIFIERS = SimpleValueNames.HYPERLINK_ACTIVATION_MODIFIERS;
    public static final String INDENT_SHIFT_WIDTH = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String LINE_HEIGHT_CORRECTION = SimpleValueNames.LINE_HEIGHT_CORRECTION;
    public static final String LINE_NUMBER_VISIBLE = SimpleValueNames.LINE_NUMBER_VISIBLE;
    public static final String MARGIN = SimpleValueNames.MARGIN;
    public static final String SCROLL_FIND_INSETS = SimpleValueNames.SCROLL_FIND_INSETS;
    public static final String SCROLL_JUMP_INSETS = SimpleValueNames.SCROLL_JUMP_INSETS;
    public static final String SPACES_PER_TAB = SimpleValueNames.SPACES_PER_TAB;
    public static final String STATUS_BAR_CARET_DELAY = SimpleValueNames.STATUS_BAR_CARET_DELAY;
    public static final String STATUS_BAR_VISIBLE = SimpleValueNames.STATUS_BAR_VISIBLE;
    public static final String TAB_SIZE = SimpleValueNames.TAB_SIZE;
    public static final String TEXT_LEFT_MARGIN_WIDTH = SimpleValueNames.TEXT_LEFT_MARGIN_WIDTH;
    public static final @Deprecated String TEXT_LIMIT_LINE_COLOR = SimpleValueNames.TEXT_LIMIT_LINE_COLOR;
    public static final String TEXT_LIMIT_LINE_VISIBLE = SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE;
    public static final String TEXT_LIMIT_WIDTH = SimpleValueNames.TEXT_LIMIT_WIDTH;
    public static final String PAIR_CHARACTERS_COMPLETION = SimpleValueNames.COMPLETION_PAIR_CHARACTERS;
    
    
    /** Acceptor that recognizes the identifier characters.
    * If set it's used instead of the default Syntax.isIdentifierPart() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String IDENTIFIER_ACCEPTOR = "identifier-acceptor"; // NOI18N

    /** Acceptor that recognizes the whitespace characters.
    * If set it's used instead of the default Syntax.isWhitespace() call.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String WHITESPACE_ACCEPTOR = "whitespace-acceptor"; // NOI18N

    /** Map of the string abbreviations. The second string (value) means
     * the full version of the first string (key).
     * Values: java.util.Map instances holding
     *      [abbrev-string, expanded-abbrev-string] pairs
     * 
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    @Deprecated
    public static final String ABBREV_MAP = "abbrev-map"; // NOI18N

    /** Map of macro definitions. The second string (value) contains
     * encoded functionality of the macro in the following notation:
     * Macro ::= S? Element (S Element)* S?
     * Element ::= Action | '"' Text '"'
     * Action ::= ( ( ( char - ( S | '\' ) ) | '\\' | ( '\' S ) )+
     * Text ::= ( ( char - ( '"' | '\' ) ) | ( '\"' | '\\' ) )*
     * 
     * @deprecated Not supported anymore. There is no public API for accessing
     *   editor macros.
     */
    @Deprecated
    public static final String MACRO_MAP = "macro-map"; // NOI18N

    /** Map of the action abbreviations. The second string (value) is
     * the name of the action to execute. The action must be available
     * in the kit actions. It can be added through <tt>CUSTOM_ACTION_LIST</tt>.
     * The original abbreviation string
     * is first removed from the text before the action is executed.
     * If there is the same abbreviation in the <tt>ABBREV_MAP</tt> map
     * it has a precedence over the <tt>ABBREV_ACTION_MAP</tt>.
     * Values: java.util.Map instances holding
     *   [abbrev-string, name-of-the-action-to-execute] pairs
     * 
     * @deprecated Not supported anymore. For code templates use
     *   Editor Settings, Editor Settings Storage and Editor Code Templates APIs.
     */
    @Deprecated
    public static final String ABBREV_ACTION_MAP = "abbrev-action-map"; // NOI18N

    /** Acceptor checking whether abbreviation should be attempted
    * after the appropriate character was typed.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_EXPAND_ACCEPTOR = "abbrev-expand-acceptor"; // NOI18N

    /** Acceptor checking whether typed character that performed
    * abbreviation expansion should be added to the text or not.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_ADD_TYPED_CHAR_ACCEPTOR
    = "abbrev-add-typed-char-acceptor"; // NOI18N

    /** Acceptor checking whether typed character should reset
    * abbreviation accounting. By default all non-letterOrDigit chars
    * reset the abbreviation accounting.
    * Values: org.netbeans.editor.Acceptor instances
    */
    public static final String ABBREV_RESET_ACCEPTOR = "abbrev-reset-acceptor"; // NOI18N

    /** Whether to display line numbers when printing to the printer.
    * Values: java.lang.Boolean instances
    */
    public static final String PRINT_LINE_NUMBER_VISIBLE = "print-line-number-visible"; // NOI18N

    /** How much space will be added additionaly when the component needs to be
    * resized.
    * Each dimension value can be positive or negative. The positive value means
    * the number of lines for the height and the number of characters
    * for the width. The negative value means percentage of the editor
    * component height for the height and percentage of the editor
    * component width for the width.
    * Values: java.awt.Dimension instances
    */
    public static final String COMPONENT_SIZE_INCREMENT = "component-size-increment"; // NOI18N

    /** Rendering hints to be used for the painting.
    * Values: java.util.Map instances
    */
    public static final String RENDERING_HINTS = "rendering-hints"; // NOI18N

    /** Key binding list for particular kit.
     * Values: java.util.List instances holding
     *   javax.swing.JTextComponent.KeyBinding instances
     *   or org.netbeans.editor.MultiKeyBinding instances
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    @Deprecated
    public static final String KEY_BINDING_LIST = "key-bindings"; // NOI18N

    /** Whether the input-methods should be enabled.
    * Values: java.lang.Boolean
    */
    public static final String INPUT_METHODS_ENABLED = "input-methods-enabled"; // NOI18N

    /* Find properties.
    * They are read by FindSupport when its instance is being initialized.
    * FIND_WHAT: java.lang.String - search expression
    * FIND_REPLACE_BY: java.lang.String - replace string
    * FIND_HIGHLIGHT_SEARCH: java.lang.Boolean - highlight matching strings in text
    * FIND_INC_SEARCH: java.lang.Boolean - show matching strings immediately
    * FIND_BACKWARD_SEARCH: java.lang.Boolean - search in backward direction
    * FIND_WRAP_SEARCH: java.lang.Boolean - if end of doc reached, start from begin
    * FIND_MATCH_CASE: java.lang.Boolean - match case of letters
    * FIND_SMART_CASE: java.lang.Boolean - case insensitive search if FIND_MATCH_CASE
    *   is false and all letters of FIND_WHAT are small, case sensitive otherwise
    * FIND_WHOLE_WORDS: java.lang.Boolean - match only whole words
    * FIND_REG_EXP: java.lang.Boolean - use regular expressions in search expr
    * FIND_HISTORY: java.util.List - History of search expressions
    * FIND_HISTORY_SIZE: java.lang.Integer - Maximum size of the history
    * FIND_BLOCK_SEARCH: java.lang.Boolean - search in block
    * FIND_BLOCK_SEARCH_START: java.lang.Integer - start offset of the block
    * FIND_BLOCK_SEARCH_END: java.lang.Integer - end offset of the block
    * 
    */
    public static final String FIND_WHAT = "find-what"; // NOI18N
    public static final String FIND_REPLACE_WITH = "find-replace-with"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH = "find-highlight-search"; // NOI18N
    public static final String FIND_INC_SEARCH = "find-inc-search"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY = "find-inc-search-delay"; // NOI18N
    public static final String FIND_BACKWARD_SEARCH = "find-backward-search"; // NOI18N
    public static final String FIND_WRAP_SEARCH = "find-wrap-search"; // NOI18N
    public static final String FIND_MATCH_CASE = "find-match-case"; // NOI18N
    public static final String FIND_SMART_CASE = "find-smart-case"; // NOI18N
    public static final String FIND_WHOLE_WORDS = "find-whole-words"; // NOI18N
    public static final String FIND_REG_EXP = "find-reg-exp"; // NOI18N
    public static final String FIND_HISTORY = "find-history"; // NOI18N
    public static final String FIND_HISTORY_SIZE = "find-history-size"; // NOI18N
    public static final String FIND_BLOCK_SEARCH = "find-block-search"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_START = "find-block-search-start"; //NOI18N
    public static final String FIND_BLOCK_SEARCH_END = "find-block-search-end"; //NOI18N


    /** Number of characters that can be searched. If the value is larger
    * than the document size, the document is used but the next document
    * will not be used. The zero value disables the word match completely.
    * Specify Integer.MAX_VALUE to search all the documents regardless
    * of the size.
    * Values: java.lang.Integer instances
    */
    public static final String WORD_MATCH_SEARCH_LEN = "word-match-search-len"; // NOI18N

    /** Wrap the word match searching
    * on current document after it reaches the end/begining of
    * current document. All the other documents except the current (first) one
    * are searched from begining in forward direction.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_WRAP_SEARCH = "word-match-wrap-search"; // NOI18N

    /** Word list that is searched as last resort in word matching.
    * It can contain the words that are used often by the user.
    * If this property is set, these words are searched regardless
    * of WORD_MATCH_SEARCH_LEN setting.
    * Values: java.lang.String instances
    */
    public static final String WORD_MATCH_STATIC_WORDS = "word-match-static-words"; // NOI18N

    /** Whether to use case sensitive search or not.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_MATCH_CASE = "word-match-match-case"; // NOI18N

    /** Whether to use case insensitive search if all the letters are small
    * and case sensitive search if at least one letter is capital.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_SMART_CASE = "word-match-smart-case"; // NOI18N

    /** Whether the word matching should return the match even if the matching
    * word has only one char. The WORD_MATCH_MATCH_CASE setting is ignored
    * in case this setting is on.
    * Values: java.lang.Boolean instances
    */
    public static final String WORD_MATCH_MATCH_ONE_CHAR = "word-match-match-one-char"; // NOI18N

    /** List of actions that will be added to the standard list of actions
    * for the particular kit. Using this mechanism, user can add actions
    * and possibly map them to the keys without overriding kit classes.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be added
    * and BaseKit actions will be added first.
    * Values: java.util.List instances
    */
    public static final String CUSTOM_ACTION_LIST = "custom-action-list"; // NOI18N

    /** List of actions which is executed when
    * editor kit is installed into component. Actions are executed one by one
    * in the order they occur in the list.
    * At the time the kit is installed, the document is not yet assigned.
    * To perform some actions on document, use the DOC_INSTALL_ACTION_LIST.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String KIT_INSTALL_ACTION_NAME_LIST = "kit-install-action-name-list"; // NOI18N

    /** List of actions that are executed when
    * editor kit is being removed from component. Actions are executed one by one
    * in the order they occur in the list.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String KIT_DEINSTALL_ACTION_NAME_LIST = "kit-deinstall-action-name-list"; // NOI18N

    /** List of actions which is executed when
    * the new document is installed into component. Actions are executed one by one
    * in the order they occur in the list.
    * NOTICE!: This option has INCREMENTAL HANDLING, i.e. current kit list but also 
    * all the super kit lists are used. For example if there is a list of actions
    * both for JavaKit and BaseKit classes, both list of actions will be executed
    * and JavaKit actions will be executed first.
    * Values: java.util.List instances
    */
    public static final String DOC_INSTALL_ACTION_NAME_LIST = "doc-install-action-name-list"; // NOI18N

    /** Whether the home key should go to column 1 or first go to text start
    * on the given line and then to the column 1.
    * Values: java.lang.Boolean
    * @deprecated As of 05/09/2001
    */
    @Deprecated
    public static final String HOME_KEY_COLUMN_ONE = "home-key-column-one"; // NOI18N

    /** Finder for finding the next word. If it's not set,
    * the <tt>FinderFactory.NextWordFwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String NEXT_WORD_FINDER = "next-word-finder"; // NOI18N

    /** Finder for finding the previous word. If it's not set,
    * the <tt>FinderFactory.WordStartBwdFinder</tt> is used.
    * Values: org.netbeans.editor.Finder
    */
    public static final String PREVIOUS_WORD_FINDER = "previous-word-finder"; // NOI18N

    /** Whether the word move should stop on the '\n' character. This setting
    * affects both the 
    * Values: java.lang.Boolean
    */
    public static final String WORD_MOVE_NEWLINE_STOP = "word-move-newline-stop"; // NOI18N

    /** Whether to trim the white space characters (except '\n') from
    * the end of the line.
    * Values: java.lang.Boolean instances
    */
    //  public static final String TRIM_SPACES = "trim-spaces"; // NOI18N

    /** Buffer size for reading into the document from input stream or reader.
    * Values: java.lang.Integer
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String READ_BUFFER_SIZE = "read-buffer-size"; // NOI18N

    /** Buffer size for writing from the document to output stream or writer.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String WRITE_BUFFER_SIZE = "write-buffer-size"; // NOI18N

    /** Read mark distance is used when performing initial read
    * of the document. It denotes the distance in chars of two adjacent
    * syntax marks inserted into the document.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String READ_MARK_DISTANCE = "read-mark-distance"; // NOI18N

    /** Implicit mark distance for inserting to the document.
    * If the insert is made then the distance between nearest syntax
    * marks around insertion point is checked and if it's greater
    * than the max mark distance then another mark(s) are inserted
    * automatically with the distance given by this setting.
    * Values: java.lang.Integer instances instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MARK_DISTANCE = "mark-distance"; // NOI18N

    /** Maximum mark distance. When there is an insertion done in document
    * and the distance between marks gets greater than this setting, another
    * mark will be inserted automatically.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MAX_MARK_DISTANCE = "max-mark-distance"; // NOI18N

    /** Minimum mark distance for removals. When there is a removal done
    * in document and it makes the marks to get closer than this value, then
    * the marks the additional marks that are closer than the distance
    * given by this setting are removed automatically.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String MIN_MARK_DISTANCE = "min-mark-distance"; // NOI18N

    /** Size of one batch of characters loaded into syntax segment
    * when updating syntax marks. It prevents checking and loading
    * of syntax segment at every syntax mark. Instead it loads
    * at least the amount of characters given by this setting.
    * This whole process is done only in case the changes in syntax
    * extend the end of current line. If the syntax changes don't
    * extend to the next line, this setting has no effect.
    * Values: java.lang.Integer instances
    * WARNING! This is critical parameter for editor functionality.
    * Please see DefaultSettings.java for values of this setting
    */
    public static final String SYNTAX_UPDATE_BATCH_SIZE = "syntax-update-batch-size"; // NOI18N

    /** How many lines should be processed at once in the various text
    * processing. This is used for example when processing the text
    * by syntax scanner.
    */
    public static final String LINE_BATCH_SIZE = "line-batch-size"; // NOI18N

    /** Ignore the changes made to the settings through the visual property editors
    * working over the methods manipulating the settings. Generally there can be
    * two ways to change the settings in the IDE. One way is to directly
    * call the appropriate methods. The other way can be through some visual
    * property editors. This flag should be checked by the property editors
    * and if set to true, no changes to the settings should be performed.
    * This allows advanced users to completely control the settings through code
    * without any external undesired changes.
    */
    public static final String IGNORE_VISUAL_CHANGES = "ignore-visual-changes"; // NOI18N

    /** List of the names of the additional colorings that need to be taken
     * into account for the particular kit. The colorings that come from
     * syntax coloring need not be specified here. Instead
     * the <tt>TOKEN_CONTEXT_LIST</tt> holds the list of token contexts
     * that can be used by the given kit.
     * The coloring names are without the suffix just like the predefined coloring names.
     * Values: java.util.List instances
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    @Deprecated
    public static final String COLORING_NAME_LIST = "coloring-name-list"; // NOI18N

    /** Suffix added to the coloring settings. The resulting name is used
     * as the name of the setting.
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    @Deprecated
    public static final String COLORING_NAME_SUFFIX = "-coloring"; // NOI18N

    /** Suffix added to the print coloring settings. The resulting name is used
     * as the name of the setting.
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    @Deprecated
    public static final String COLORING_NAME_PRINT_SUFFIX = "-print-coloring"; // NOI18N


    /** Default coloring for the drawing. 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String DEFAULT_COLORING = "default"; // NOI18N

    /** Coloring that will be used for line numbers displayed on the left
     * side on the screen.
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String LINE_NUMBER_COLORING = "line-number"; // NOI18N
    
    /** Coloring used for guarded blocks 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String GUARDED_COLORING = "guarded"; // NOI18N

    /** Coloring that will be used for code folding icons displayed in editor
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String CODE_FOLDING_COLORING = "code-folding"; // NOI18N

    /** Coloring that will be used for code folding side bar
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String CODE_FOLDING_BAR_COLORING = "code-folding-bar"; // NOI18N
    
    /** Coloring used for selection 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String SELECTION_COLORING = "selection"; // NOI18N

    /** Coloring used for highlight search 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String HIGHLIGHT_SEARCH_COLORING = "highlight-search"; // NOI18N

    /** Coloring used for incremental search 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String INC_SEARCH_COLORING = "inc-search"; // NOI18N

    /** Coloring used for block search
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String BLOCK_SEARCH_COLORING = "block-search"; // NOI18N
    
    /** Coloring used for the status bar 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String STATUS_BAR_COLORING = "status-bar"; // NOI18N

    /** Coloring used to mark important text in the status bar 
     * @deprecated Use {@link org.netbeans.api.editor.settings.FontColorNames} instead.
     */
    @Deprecated
    public static final String STATUS_BAR_BOLD_COLORING = "status-bar-bold"; // NOI18N

    
    // -----------------------------------------------------------------------
    // --- from ExtSettingsNames
    // -----------------------------------------------------------------------

    public static final String COMPLETION_AUTO_POPUP = SimpleValueNames.COMPLETION_AUTO_POPUP;
    public static final String COMPLETION_AUTO_POPUP_DELAY = SimpleValueNames.COMPLETION_AUTO_POPUP_DELAY;
    public static final String COMPLETION_CASE_SENSITIVE = SimpleValueNames.COMPLETION_CASE_SENSITIVE;
    public static final String COMPLETION_INSTANT_SUBSTITUTION = SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION;
    public static final String COMPLETION_NATURAL_SORT = SimpleValueNames.COMPLETION_NATURAL_SORT;
    public static final String COMPLETION_PANE_MAX_SIZE = SimpleValueNames.COMPLETION_PANE_MAX_SIZE;
    public static final String COMPLETION_PANE_MIN_SIZE = SimpleValueNames.COMPLETION_PANE_MIN_SIZE;
    public static final String HIGHLIGHT_CARET_ROW = SimpleValueNames.HIGHLIGHT_CARET_ROW;
    public static final String HIGHLIGHT_MATCH_BRACE = SimpleValueNames.HIGHLIGHT_MATCH_BRACE;
    public static final String JAVADOC_AUTO_POPUP = SimpleValueNames.JAVADOC_AUTO_POPUP;
    public static final String JAVADOC_AUTO_POPUP_DELAY = SimpleValueNames.JAVADOC_AUTO_POPUP_DELAY;
    public static final @Deprecated String JAVADOC_BG_COLOR = SimpleValueNames.JAVADOC_BG_COLOR;
    public static final String JAVADOC_PREFERRED_SIZE = SimpleValueNames.JAVADOC_PREFERRED_SIZE;
    public static final String POPUP_MENU_ENABLED = SimpleValueNames.POPUP_MENU_ENABLED;
    public static final String SHOW_DEPRECATED_MEMBERS = SimpleValueNames.SHOW_DEPRECATED_MEMBERS;
    
    /** List of the action names that should be shown in the popup menu.
    * Null name means separator.
    * Values: java.util.List containing java.lang.String instances
    */
    public static final String POPUP_MENU_ACTION_NAME_LIST = "popup-menu-action-name-list"; // NOI18N

    /** List of the action names that should be shown in the popup menu
     * when JEditorPane is shown in the dialogs. It corresponds
     * Null name means separator.
     * Values: java.util.List containing java.lang.String instances
     */
    public static final String DIALOG_POPUP_MENU_ACTION_NAME_LIST = "dialog-popup-menu-action-name-list"; // NOI18N

    /** 
     * Coloring used to highlight the row where the caret resides
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String HIGHLIGHT_CARET_ROW_COLORING = "highlight-caret-row"; // NOI18N

    /** 
     * Coloring used to highlight the matching brace
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String HIGHLIGHT_MATCH_BRACE_COLORING = "highlight-match-brace"; // NOI18N

    /** Delay (milliseconds) after which the matching brace
     * will be updated. This is intended to eliminate flicker
     * if the user holds the arrow key pressed.
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String HIGHLIGHT_MATCH_BRACE_DELAY = "highlight-match-brace-delay"; // NOI18N

    /** Whether the fast and simple matching should be used for higlighting
     * the matching brace. Its disadvantage is that it doesn't ignore the comments
     * and string and character constants in the search.
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String CARET_SIMPLE_MATCH_BRACE = "caret-simple-match-brace"; // NOI18N

    /** The delay after which the completion window is refreshed.
    * Values: java.lang.Integer
    */
    public static final String COMPLETION_REFRESH_DELAY = "completion-refresh-delay"; // NOI18N

    /** Acceptor sensitive to characters that cause that
     * that the current line will be reformatted immediately.
     */
    public static final String INDENT_HOT_CHARS_ACCEPTOR = "indent-hot-chars-acceptor"; // NOI18N

    /** Whether lines should be indented on an indent hot key if there is non whitespace before
     * the typed hot key. See editor issue #10771.
     * Values: java.lang.Boolean
     */
    public static final String REINDENT_WITH_TEXT_BEFORE = "reindent-with-text-before"; // NOI18N

    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Integer
     */
    public static final String FAST_IMPORT_SELECTION = "fast-import-selection"; // NOI18N
    
    /** Whether the fast import should offer packages instead of classes
     * Values: java.lang.Boolean
     * @deprecated replaced by FAST_IMPORT_SELECTION
     */
    @Deprecated
    public static final String FAST_IMPORT_PACKAGE = "fast-import-package"; // NOI18N
    
    /**
     * The Code Completion DB is always updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String ALWAYS = "pd_always";//NOI18N
    /** 
     * The Code Completion DB is never updated after mounting a new filesystem.
     * @deprecated Without any replacement.
     */
    @Deprecated
    public static final String NEVER = "pd_never";//NOI18N
    
}
