#Signature file v4.1
#Version 1.80

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.api.editor.settings.AttributesUtilities
meth public !varargs static javax.swing.text.AttributeSet createComposite(javax.swing.text.AttributeSet[])
meth public !varargs static javax.swing.text.AttributeSet createImmutable(java.lang.Object[])
meth public !varargs static javax.swing.text.AttributeSet createImmutable(javax.swing.text.AttributeSet[])
supr java.lang.Object
hfds ATTR_DISMANTLED_STRUCTURE
hcls BigComposite,Composite2,Composite4,CompositeAttributeSet,Immutable,Proxy

CLSS public final org.netbeans.api.editor.settings.CodeTemplateDescription
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getAbbreviation()
meth public java.lang.String getDescription()
meth public java.lang.String getMimePath()
meth public java.lang.String getParametrizedText()
meth public java.lang.String getUniqueId()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getContexts()
supr java.lang.Object
hfds abbreviation,contexts,description,mimePath,parametrizedText,uniqueId

CLSS public abstract org.netbeans.api.editor.settings.CodeTemplateSettings
cons public init()
meth public abstract java.util.List<org.netbeans.api.editor.settings.CodeTemplateDescription> getCodeTemplateDescriptions()
meth public abstract javax.swing.KeyStroke getExpandKey()
supr java.lang.Object

CLSS public final org.netbeans.api.editor.settings.EditorStyleConstants
fld public final static java.lang.Object BottomBorderLineColor
fld public final static java.lang.Object Default
fld public final static java.lang.Object DisplayName
fld public final static java.lang.Object LeftBorderLineColor
fld public final static java.lang.Object RenderingHints
fld public final static java.lang.Object RightBorderLineColor
fld public final static java.lang.Object Tooltip
fld public final static java.lang.Object TopBorderLineColor
fld public final static java.lang.Object WaveUnderlineColor
meth public java.lang.String toString()
supr java.lang.Object
hfds representation

CLSS public final org.netbeans.api.editor.settings.FontColorNames
fld public final static java.lang.String BLOCK_SEARCH_COLORING = "block-search"
fld public final static java.lang.String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"
fld public final static java.lang.String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"
fld public final static java.lang.String CARET_ROW_COLORING = "highlight-caret-row"
fld public final static java.lang.String CODE_FOLDING_BAR_COLORING = "code-folding-bar"
fld public final static java.lang.String CODE_FOLDING_COLORING = "code-folding"
fld public final static java.lang.String DEFAULT_COLORING = "default"
fld public final static java.lang.String DOCUMENTATION_POPUP_COLORING = "documentation-popup-coloring"
fld public final static java.lang.String GUARDED_COLORING = "guarded"
fld public final static java.lang.String HIGHLIGHT_SEARCH_COLORING = "highlight-search"
fld public final static java.lang.String INC_SEARCH_COLORING = "inc-search"
fld public final static java.lang.String INDENT_GUIDE_LINES = "indent-guide-lines"
fld public final static java.lang.String LINE_NUMBER_COLORING = "line-number"
fld public final static java.lang.String SELECTION_COLORING = "selection"
fld public final static java.lang.String STATUS_BAR_BOLD_COLORING = "status-bar-bold"
fld public final static java.lang.String STATUS_BAR_COLORING = "status-bar"
fld public final static java.lang.String TEXT_LIMIT_LINE_COLORING = "text-limit-line-color"
supr java.lang.Object

CLSS public abstract org.netbeans.api.editor.settings.FontColorSettings
cons public init()
fld public final static java.lang.String PROP_FONT_COLORS = "fontColors"
 anno 0 java.lang.Deprecated()
meth public abstract javax.swing.text.AttributeSet getFontColors(java.lang.String)
meth public abstract javax.swing.text.AttributeSet getTokenFontColors(java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.api.editor.settings.KeyBindingSettings
cons public init()
meth public abstract java.util.List<org.netbeans.api.editor.settings.MultiKeyBinding> getKeyBindings()
supr java.lang.Object

CLSS public final org.netbeans.api.editor.settings.MultiKeyBinding
cons public init(javax.swing.KeyStroke,java.lang.String)
cons public init(javax.swing.KeyStroke[],java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int getKeyStrokeCount()
meth public int hashCode()
meth public java.lang.String getActionName()
meth public java.lang.String toString()
meth public java.util.List<javax.swing.KeyStroke> getKeyStrokeList()
meth public javax.swing.KeyStroke getKeyStroke(int)
supr java.lang.Object
hfds actionName,keyStrokeList
hcls UnmodifiableArrayList

CLSS public final org.netbeans.api.editor.settings.SimpleValueNames
fld public final static java.lang.String ALT_HYPERLINK_ACTIVATION_MODIFIERS = "alt-hyperlink-activation-modifiers"
fld public final static java.lang.String BRACE_FIRST_TOOLTIP = "editor-brace-first-tooltip"
fld public final static java.lang.String BRACE_SHOW_OUTLINE = "editor-brace-outline"
fld public final static java.lang.String CARET_BLINK_RATE = "caret-blink-rate"
fld public final static java.lang.String CARET_COLOR_INSERT_MODE = "caret-color-insert-mode"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CARET_COLOR_OVERWRITE_MODE = "caret-color-overwrite-mode"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CARET_ITALIC_INSERT_MODE = "caret-italic-insert-mode"
fld public final static java.lang.String CARET_ITALIC_OVERWRITE_MODE = "caret-italic-overwrite-mode"
fld public final static java.lang.String CARET_TYPE_INSERT_MODE = "caret-type-insert-mode"
fld public final static java.lang.String CARET_TYPE_OVERWRITE_MODE = "caret-type-overwrite-mode"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"
fld public final static java.lang.String CODE_FOLDING_COLLAPSE_TAGS = "code-folding-collapse-tags"
fld public final static java.lang.String CODE_FOLDING_ENABLE = "code-folding-enable"
fld public final static java.lang.String COMPLETION_AUTO_POPUP = "completion-auto-popup"
fld public final static java.lang.String COMPLETION_AUTO_POPUP_DELAY = "completion-auto-popup-delay"
fld public final static java.lang.String COMPLETION_CASE_SENSITIVE = "completion-case-sensitive"
fld public final static java.lang.String COMPLETION_INSTANT_SUBSTITUTION = "completion-instant-substitution"
fld public final static java.lang.String COMPLETION_NATURAL_SORT = "completion-natural-sort"
fld public final static java.lang.String COMPLETION_PAIR_CHARACTERS = "pair-characters-completion"
fld public final static java.lang.String COMPLETION_PANE_MAX_SIZE = "completion-pane-max-size"
fld public final static java.lang.String COMPLETION_PANE_MIN_SIZE = "completion-pane-min-size"
fld public final static java.lang.String COMPLETION_PARAMETER_TOOLTIP = "completion-parameter-tooltip"
fld public final static java.lang.String EDITOR_SEARCH_TYPE = "editor-search-type"
fld public final static java.lang.String ENABLE_INDENTATION = "enable-indent"
fld public final static java.lang.String EXPAND_TABS = "expand-tabs"
fld public final static java.lang.String HIGHLIGHT_CARET_ROW = "highlight-caret-row"
fld public final static java.lang.String HIGHLIGHT_MATCH_BRACE = "highlight-match-brace"
fld public final static java.lang.String HYPERLINK_ACTIVATION_MODIFIERS = "hyperlink-activation-modifiers"
fld public final static java.lang.String INDENT_SHIFT_WIDTH = "indent-shift-width"
fld public final static java.lang.String JAVADOC_AUTO_POPUP = "javadoc-auto-popup"
fld public final static java.lang.String JAVADOC_AUTO_POPUP_DELAY = "javadoc-auto-popup-delay"
fld public final static java.lang.String JAVADOC_BG_COLOR = "javadoc-bg-color"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String JAVADOC_POPUP_NEXT_TO_CC = "javadoc-popup-next-to-cc"
fld public final static java.lang.String JAVADOC_PREFERRED_SIZE = "javadoc-preferred-size"
fld public final static java.lang.String LINE_HEIGHT_CORRECTION = "line-height-correction"
fld public final static java.lang.String LINE_NUMBER_VISIBLE = "line-number-visible"
fld public final static java.lang.String MARGIN = "margin"
fld public final static java.lang.String NON_PRINTABLE_CHARACTERS_VISIBLE = "non-printable-characters-visible"
fld public final static java.lang.String ON_SAVE_REFORMAT = "on-save-reformat"
fld public final static java.lang.String ON_SAVE_REMOVE_TRAILING_WHITESPACE = "on-save-remove-trailing-whitespace"
fld public final static java.lang.String ON_SAVE_USE_GLOBAL_SETTINGS = "on-save-use-global-settings"
fld public final static java.lang.String POPUP_MENU_ENABLED = "popup-menu-enabled"
fld public final static java.lang.String SCROLL_FIND_INSETS = "scroll-find-insets"
fld public final static java.lang.String SCROLL_JUMP_INSETS = "scroll-jump-insets"
fld public final static java.lang.String SHOW_DEPRECATED_MEMBERS = "show-deprecated-members"
fld public final static java.lang.String SPACES_PER_TAB = "spaces-per-tab"
fld public final static java.lang.String STATUS_BAR_CARET_DELAY = "status-bar-caret-delay"
fld public final static java.lang.String STATUS_BAR_VISIBLE = "status-bar-visible"
fld public final static java.lang.String TAB_SIZE = "tab-size"
fld public final static java.lang.String TEXT_LEFT_MARGIN_WIDTH = "text-left-margin-width"
fld public final static java.lang.String TEXT_LIMIT_LINE_COLOR = "text-limit-line-color"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String TEXT_LIMIT_LINE_VISIBLE = "text-limit-line-visible"
fld public final static java.lang.String TEXT_LIMIT_WIDTH = "text-limit-width"
fld public final static java.lang.String TEXT_LINE_WRAP = "text-line-wrap"
fld public final static java.lang.String THICK_CARET_WIDTH = "thick-caret-width"
fld public final static java.lang.String TOOLBAR_VISIBLE_PROP = "toolbarVisible"
supr java.lang.Object

