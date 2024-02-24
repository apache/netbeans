#Signature file v4.1
#Version 2.79

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public javax.swing.text.DefaultEditorKit
cons public init()
fld public final static java.lang.String EndOfLineStringProperty = "__EndOfLine__"
fld public final static java.lang.String backwardAction = "caret-backward"
fld public final static java.lang.String beepAction = "beep"
fld public final static java.lang.String beginAction = "caret-begin"
fld public final static java.lang.String beginLineAction = "caret-begin-line"
fld public final static java.lang.String beginParagraphAction = "caret-begin-paragraph"
fld public final static java.lang.String beginWordAction = "caret-begin-word"
fld public final static java.lang.String copyAction = "copy-to-clipboard"
fld public final static java.lang.String cutAction = "cut-to-clipboard"
fld public final static java.lang.String defaultKeyTypedAction = "default-typed"
fld public final static java.lang.String deleteNextCharAction = "delete-next"
fld public final static java.lang.String deleteNextWordAction = "delete-next-word"
fld public final static java.lang.String deletePrevCharAction = "delete-previous"
fld public final static java.lang.String deletePrevWordAction = "delete-previous-word"
fld public final static java.lang.String downAction = "caret-down"
fld public final static java.lang.String endAction = "caret-end"
fld public final static java.lang.String endLineAction = "caret-end-line"
fld public final static java.lang.String endParagraphAction = "caret-end-paragraph"
fld public final static java.lang.String endWordAction = "caret-end-word"
fld public final static java.lang.String forwardAction = "caret-forward"
fld public final static java.lang.String insertBreakAction = "insert-break"
fld public final static java.lang.String insertContentAction = "insert-content"
fld public final static java.lang.String insertTabAction = "insert-tab"
fld public final static java.lang.String nextWordAction = "caret-next-word"
fld public final static java.lang.String pageDownAction = "page-down"
fld public final static java.lang.String pageUpAction = "page-up"
fld public final static java.lang.String pasteAction = "paste-from-clipboard"
fld public final static java.lang.String previousWordAction = "caret-previous-word"
fld public final static java.lang.String readOnlyAction = "set-read-only"
fld public final static java.lang.String selectAllAction = "select-all"
fld public final static java.lang.String selectLineAction = "select-line"
fld public final static java.lang.String selectParagraphAction = "select-paragraph"
fld public final static java.lang.String selectWordAction = "select-word"
fld public final static java.lang.String selectionBackwardAction = "selection-backward"
fld public final static java.lang.String selectionBeginAction = "selection-begin"
fld public final static java.lang.String selectionBeginLineAction = "selection-begin-line"
fld public final static java.lang.String selectionBeginParagraphAction = "selection-begin-paragraph"
fld public final static java.lang.String selectionBeginWordAction = "selection-begin-word"
fld public final static java.lang.String selectionDownAction = "selection-down"
fld public final static java.lang.String selectionEndAction = "selection-end"
fld public final static java.lang.String selectionEndLineAction = "selection-end-line"
fld public final static java.lang.String selectionEndParagraphAction = "selection-end-paragraph"
fld public final static java.lang.String selectionEndWordAction = "selection-end-word"
fld public final static java.lang.String selectionForwardAction = "selection-forward"
fld public final static java.lang.String selectionNextWordAction = "selection-next-word"
fld public final static java.lang.String selectionPreviousWordAction = "selection-previous-word"
fld public final static java.lang.String selectionUpAction = "selection-up"
fld public final static java.lang.String upAction = "caret-up"
fld public final static java.lang.String writableAction = "set-writable"
innr public static BeepAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertTabAction
innr public static PasteAction
meth public java.lang.String getContentType()
meth public javax.swing.Action[] getActions()
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.EditorKit

CLSS public abstract javax.swing.text.EditorKit
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.String getContentType()
meth public abstract javax.swing.Action[] getActions()
meth public abstract javax.swing.text.Caret createCaret()
meth public abstract javax.swing.text.Document createDefaultDocument()
meth public abstract javax.swing.text.ViewFactory getViewFactory()
meth public abstract void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public abstract void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object clone()
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
supr java.lang.Object

CLSS public org.netbeans.editor.BaseKit
cons public init()
fld public final static int MAGIC_POSITION_MAX = 2147483646
fld public final static java.lang.String DOC_REPLACE_SELECTION_PROPERTY = "doc-replace-selection-property"
fld public final static java.lang.String abbrevExpandAction = "abbrev-expand"
fld public final static java.lang.String abbrevResetAction = "abbrev-reset"
fld public final static java.lang.String adjustCaretBottomAction = "adjust-caret-bottom"
fld public final static java.lang.String adjustCaretCenterAction = "adjust-caret-center"
fld public final static java.lang.String adjustCaretTopAction = "adjust-caret-top"
fld public final static java.lang.String adjustWindowBottomAction = "adjust-window-bottom"
fld public final static java.lang.String adjustWindowCenterAction = "adjust-window-center"
fld public final static java.lang.String adjustWindowTopAction = "adjust-window-top"
fld public final static java.lang.String annotationsCyclingAction = "annotations-cycling"
fld public final static java.lang.String collapseAllFoldsAction = "collapse-all-folds"
fld public final static java.lang.String collapseFoldAction = "collapse-fold"
fld public final static java.lang.String copySelectionElseLineDownAction = "copy-selection-else-line-down"
fld public final static java.lang.String copySelectionElseLineUpAction = "copy-selection-else-line-up"
fld public final static java.lang.String cutToLineBeginAction = "cut-to-line-begin"
fld public final static java.lang.String cutToLineEndAction = "cut-to-line-end"
fld public final static java.lang.String expandAllFoldsAction = "expand-all-folds"
fld public final static java.lang.String expandFoldAction = "expand-fold"
fld public final static java.lang.String findNextAction = "find-next"
fld public final static java.lang.String findPreviousAction = "find-previous"
fld public final static java.lang.String findSelectionAction = "find-selection"
fld public final static java.lang.String firstNonWhiteAction = "first-non-white"
fld public final static java.lang.String formatAction = "format"
fld public final static java.lang.String generateGutterPopupAction = "generate-gutter-popup"
fld public final static java.lang.String indentAction = "indent"
fld public final static java.lang.String insertDateTimeAction = "insert-date-time"
fld public final static java.lang.String jumpListNextAction = "jump-list-next"
fld public final static java.lang.String jumpListNextComponentAction = "jump-list-next-component"
fld public final static java.lang.String jumpListPrevAction = "jump-list-prev"
fld public final static java.lang.String jumpListPrevComponentAction = "jump-list-prev-component"
fld public final static java.lang.String lastNonWhiteAction = "last-non-white"
fld public final static java.lang.String lineFirstColumnAction = "caret-line-first-column"
fld public final static java.lang.String macroActionPrefix = "macro-"
fld public final static java.lang.String moveSelectionElseLineDownAction = "move-selection-else-line-down"
fld public final static java.lang.String moveSelectionElseLineUpAction = "move-selection-else-line-up"
fld public final static java.lang.String pasteFormatedAction = "paste-formated"
fld public final static java.lang.String redoAction = "redo"
fld public final static java.lang.String reformatLineAction = "reformat-line"
fld public final static java.lang.String reindentLineAction = "reindent-line"
fld public final static java.lang.String removeLineAction = "remove-line"
fld public final static java.lang.String removeLineBeginAction = "remove-line-begin"
fld public final static java.lang.String removeNextWordAction = "remove-word-next"
fld public final static java.lang.String removePreviousWordAction = "remove-word-previous"
fld public final static java.lang.String removeSelectionAction = "remove-selection"
fld public final static java.lang.String removeTabAction = "remove-tab"
fld public final static java.lang.String removeTrailingSpacesAction = "remove-trailing-spaces"
fld public final static java.lang.String scrollDownAction = "scroll-down"
fld public final static java.lang.String scrollUpAction = "scroll-up"
fld public final static java.lang.String selectIdentifierAction = "select-identifier"
fld public final static java.lang.String selectNextParameterAction = "select-next-parameter"
fld public final static java.lang.String selectionFirstNonWhiteAction = "selection-first-non-white"
fld public final static java.lang.String selectionLastNonWhiteAction = "selection-last-non-white"
fld public final static java.lang.String selectionLineFirstColumnAction = "selection-line-first-column"
fld public final static java.lang.String selectionPageDownAction = "selection-page-down"
fld public final static java.lang.String selectionPageUpAction = "selection-page-up"
fld public final static java.lang.String shiftLineLeftAction = "shift-line-left"
fld public final static java.lang.String shiftLineRightAction = "shift-line-right"
fld public final static java.lang.String splitLineAction = "split-line"
fld public final static java.lang.String startMacroRecordingAction = "start-macro-recording"
fld public final static java.lang.String startNewLineAction = "start-new-line"
fld public final static java.lang.String stopMacroRecordingAction = "stop-macro-recording"
fld public final static java.lang.String switchCaseAction = "switch-case"
fld public final static java.lang.String toLowerCaseAction = "to-lower-case"
fld public final static java.lang.String toUpperCaseAction = "to-upper-case"
fld public final static java.lang.String toggleHighlightSearchAction = "toggle-highlight-search"
fld public final static java.lang.String toggleLineNumbersAction = "toggle-line-numbers"
fld public final static java.lang.String toggleTypingModeAction = "toggle-typing-mode"
fld public final static java.lang.String undoAction = "undo"
fld public final static java.lang.String wordMatchNextAction = "word-match-next"
fld public final static java.lang.String wordMatchPrevAction = "word-match-prev"
innr public static BackwardAction
innr public static BeepAction
innr public static BeginAction
innr public static BeginLineAction
innr public static BeginWordAction
innr public static CompoundAction
innr public static CopyAction
innr public static CutAction
innr public static DefaultKeyTypedAction
innr public static DeleteCharAction
innr public static DownAction
innr public static EndAction
innr public static EndLineAction
innr public static EndWordAction
innr public static ForwardAction
innr public static InsertBreakAction
innr public static InsertContentAction
innr public static InsertStringAction
innr public static InsertTabAction
innr public static KitCompoundAction
innr public static NextWordAction
innr public static PageDownAction
innr public static PageUpAction
innr public static PasteAction
innr public static PreviousWordAction
innr public static ReadOnlyAction
innr public static RemoveTrailingSpacesAction
innr public static SelectAllAction
innr public static SelectLineAction
innr public static SelectWordAction
innr public static SplitLineAction
innr public static UpAction
innr public static WritableAction
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getCustomActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected javax.swing.Action[] getMacroActions()
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.BaseTextUI createTextUI()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth protected org.netbeans.editor.EditorUI createPrintEditorUI(org.netbeans.editor.BaseDocument,boolean,boolean)
meth protected void executeDeinstallActions(javax.swing.JEditorPane)
meth protected void executeInstallActions(javax.swing.JEditorPane)
meth protected void initDocument(org.netbeans.editor.BaseDocument)
meth protected void updateActions()
meth public final javax.swing.Action[] getActions()
meth public java.lang.Object clone()
meth public java.util.List<javax.swing.Action> translateActionNameList(java.util.List<java.lang.String>)
meth public javax.swing.Action getActionByName(java.lang.String)
meth public javax.swing.text.Caret createCaret()
meth public javax.swing.text.Document createDefaultDocument()
meth public javax.swing.text.ViewFactory getViewFactory()
meth public org.netbeans.editor.MultiKeymap getKeymap()
meth public org.netbeans.editor.Syntax createFormatSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action[] mapToActions(java.util.Map)
meth public static org.netbeans.editor.BaseKit getKit(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static void addActionsToMap(java.util.Map<java.lang.String,javax.swing.Action>,javax.swing.Action[],java.lang.String)
meth public void deinstall(javax.swing.JEditorPane)
meth public void install(javax.swing.JEditorPane)
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.DefaultEditorKit
hfds IN_PASTE,KEYMAPS_AND_ACTIONS_LOCK,KIT_CNT_PREALLOC,LOG,PROP_NAVIGATE_BOUNDARIES,copyActionDef,cutActionDef,deleteNextCharActionDef,deletePrevCharActionDef,insertBreakActionDef,insertTabActionDef,keyBindingsUpdaterInited,keymapTrackers,kitActionMaps,kitActions,kitKeymaps,kits,pasteActionDef,redoActionDef,removeSelectionActionDef,removeTabActionDef,searchableKit,serialVersionUID,undoActionDef
hcls ClearUIForNullKitListener,DefaultSyntax,DefaultSyntaxTokenContext,KeybindingsAndPreferencesTracker,NullTextUI,SearchableKit

CLSS public org.netbeans.editor.ext.ExtKit
cons public init()
fld public final static java.lang.String TRIMMED_TEXT = "trimmed-text"
fld public final static java.lang.String allCompletionShowAction = "all-completion-show"
fld public final static java.lang.String buildPopupMenuAction = "build-popup-menu"
fld public final static java.lang.String buildToolTipAction = "build-tool-tip"
fld public final static java.lang.String codeSelectAction = "code-select"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String commentAction = "comment"
fld public final static java.lang.String completionShowAction = "completion-show"
fld public final static java.lang.String completionTooltipShowAction = "tooltip-show"
fld public final static java.lang.String documentationShowAction = "documentation-show"
fld public final static java.lang.String escapeAction = "escape"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String findAction = "find"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String gotoAction = "goto"
fld public final static java.lang.String gotoDeclarationAction = "goto-declaration"
fld public final static java.lang.String gotoHelpAction = "goto-help"
fld public final static java.lang.String gotoSourceAction = "goto-source"
fld public final static java.lang.String gotoSuperImplementationAction = "goto-super-implementation"
fld public final static java.lang.String matchBraceAction = "match-brace"
fld public final static java.lang.String replaceAction = "replace"
fld public final static java.lang.String selectionMatchBraceAction = "selection-match-brace"
fld public final static java.lang.String showPopupMenuAction = "show-popup-menu"
fld public final static java.lang.String toggleCaseIdentifierBeginAction = "toggle-case-identifier-begin"
fld public final static java.lang.String toggleCommentAction = "toggle-comment"
fld public final static java.lang.String toggleToolbarAction = "toggle-toolbar"
fld public final static java.lang.String uncommentAction = "uncomment"
innr public static AllCompletionShowAction
innr public static BuildPopupMenuAction
innr public static BuildToolTipAction
innr public static CodeSelectAction
innr public static CommentAction
innr public static CompletionShowAction
innr public static CompletionTooltipShowAction
innr public static DocumentationShowAction
innr public static EscapeAction
innr public static ExtDefaultKeyTypedAction
innr public static ExtDeleteCharAction
innr public static GotoAction
innr public static GotoDeclarationAction
innr public static MatchBraceAction
innr public static PrefixMakerAction
innr public static ShowPopupMenuAction
innr public static ToggleCaseIdentifierBeginAction
innr public static ToggleCommentAction
innr public static UncommentAction
meth protected javax.swing.Action[] createActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
supr org.netbeans.editor.BaseKit
hfds debugPopupMenu,editorBundleHash,noExtEditorUIClass
hcls BaseKitLocalizedAction

CLSS public org.netbeans.modules.csl.api.RuleContext
cons public init()
fld public int astOffset
fld public int caretOffset
fld public int lexOffset
fld public int selectionEnd
fld public int selectionStart
fld public org.netbeans.editor.BaseDocument doc
 anno 0 org.netbeans.api.annotations.common.NonNull()
fld public org.netbeans.modules.csl.api.HintsProvider$HintsManager manager
 anno 0 org.netbeans.api.annotations.common.NonNull()
fld public org.netbeans.modules.csl.spi.ParserResult parserResult
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public org.netbeans.modules.editor.NbEditorKit
cons public init()
fld public final static java.lang.String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"
fld public final static java.lang.String generateFoldPopupAction = "generate-fold-popup"
fld public final static java.lang.String generateGoToPopupAction = "generate-goto-popup"
innr public NbStopMacroRecordingAction
innr public final static NbToggleLineNumbersAction
innr public static GenerateFoldPopupAction
innr public static NbBuildPopupMenuAction
innr public static NbBuildToolTipAction
innr public static NbGenerateGoToPopupAction
innr public static NbRedoAction
innr public static NbUndoAction
innr public static ToggleToolbarAction
intf java.util.concurrent.Callable
meth protected javax.swing.Action[] createActions()
meth protected javax.swing.Action[] getDeclaredActions()
meth protected org.netbeans.editor.EditorUI createEditorUI()
meth protected void addSystemActionMapping(java.lang.String,java.lang.Class)
meth protected void toolTipAnnotationsLock(javax.swing.text.Document)
meth protected void toolTipAnnotationsUnlock(javax.swing.text.Document)
meth protected void updateActions()
meth public java.lang.Object call()
meth public java.lang.String getContentType()
meth public javax.swing.text.Document createDefaultDocument()
supr org.netbeans.editor.ext.ExtKit
hfds ACTIONS_TOPCOMPONENT,ACTION_CREATEITEM,ACTION_EXTKIT_BYNAME,ACTION_FOLDER,ACTION_SEPARATOR,ACTION_SYSTEM,LOG,SEPARATOR,contentTypeTable,nbRedoActionDef,nbUndoActionDef,serialVersionUID,systemAction2editorAction
hcls LayerSubFolderMenu,PopupInitializer,SubFolderData

CLSS public org.netbeans.modules.html.editor.api.HtmlEditorUtils
cons public init()
meth public static org.netbeans.modules.csl.api.OffsetRange getDocumentOffsetRange(org.netbeans.modules.html.editor.lib.api.elements.Element,org.netbeans.modules.parsing.api.Snapshot)
meth public static org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.html.editor.lib.api.elements.Element)
supr java.lang.Object

CLSS public org.netbeans.modules.html.editor.api.HtmlKit
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String HTML_MIME_TYPE = "text/html"
intf org.openide.util.HelpCtx$Provider
meth protected javax.swing.Action[] createActions()
meth public java.lang.Object clone()
meth public java.lang.String getContentType()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void install(javax.swing.JEditorPane)
supr org.netbeans.modules.editor.NbEditorKit
hfds contentType,serialVersionUID

CLSS public org.netbeans.modules.html.editor.api.Utils
cons public init()
meth public static java.lang.String getWebPageMimeType(org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.lexer.Token<org.netbeans.api.html.lexer.HTMLTokenId> findTagOpenToken(org.netbeans.api.lexer.TokenSequence)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.api.html.lexer.HTMLTokenId> getJoinedHtmlSequence(javax.swing.text.Document,int)
meth public static org.netbeans.api.lexer.TokenSequence<org.netbeans.api.html.lexer.HTMLTokenId> getJoinedHtmlSequence(org.netbeans.api.lexer.TokenHierarchy,int)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction
cons public init(org.openide.filesystems.FileObject,java.lang.String)
fld protected org.openide.filesystems.FileObject file
innr public SourceElementHandle
meth public boolean isEnabled()
meth public org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction$SourceElementHandle createSourceElementHandle() throws org.netbeans.modules.parsing.spi.ParseException
meth public org.openide.filesystems.FileObject getFileObject()
supr javax.swing.AbstractAction
hfds elementPath

CLSS public org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction$SourceElementHandle
 outer org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction
meth public boolean isResolved()
meth public org.netbeans.modules.html.editor.lib.api.elements.OpenTag getOpenTag()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds file,openTag,snapshot

CLSS public org.netbeans.modules.html.editor.api.actions.DeleteElementAction
cons public init(org.openide.filesystems.FileObject,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction

CLSS public org.netbeans.modules.html.editor.api.actions.ModifyElementRulesAction
cons public init(org.openide.filesystems.FileObject,java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.netbeans.modules.html.editor.api.actions.AbstractSourceElementAction
hfds diff,pos

CLSS public org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons protected init(java.lang.String,int)
cons protected init(java.lang.String,int,java.lang.String)
cons protected init(org.netbeans.modules.html.editor.lib.api.HelpItem,java.lang.String,int,java.lang.String)
fld protected boolean shift
fld protected final static int DEFAULT_SORT_PRIORITY = 20
fld protected int substitutionOffset
fld protected java.lang.String helpId
fld protected java.lang.String text
fld protected org.netbeans.modules.html.editor.lib.api.HelpItem help
innr public static Attribute
innr public static AttributeValue
innr public static AutocompleteEndTag
innr public static BooleanAttribute
innr public static CharRefItem
innr public static EndTag
innr public static FileAttributeValue
innr public static GoUpFileAttributeValue
innr public static HtmlCssValueCompletionItem
innr public static Tag
intf org.netbeans.spi.editor.completion.CompletionItem
meth protected boolean substituteText(javax.swing.text.JTextComponent,int)
meth protected boolean substituteText(javax.swing.text.JTextComponent,int,int)
meth protected boolean substituteText(javax.swing.text.JTextComponent,java.lang.String,int,int)
meth protected int getMoveBackLength()
meth protected java.lang.String getLeftHtmlText()
meth protected java.lang.String getRightHtmlText()
meth protected java.lang.String getSubstituteText()
meth protected javax.swing.ImageIcon getIcon()
meth public boolean equals(java.lang.Object)
meth public boolean hasHelp()
meth public boolean instantSubstitution(javax.swing.text.JTextComponent)
meth public int getAnchorOffset()
meth public int getPreferredWidth(java.awt.Graphics,java.awt.Font)
meth public int getSortPriority()
meth public int hashCode()
meth public java.lang.CharSequence getInsertPrefix()
meth public java.lang.CharSequence getSortText()
meth public java.lang.String getHelp()
meth public java.lang.String getHelpId()
meth public java.lang.String getItemText()
meth public java.lang.String toString()
meth public java.net.URL getHelpURL()
meth public org.netbeans.modules.html.editor.lib.api.HelpItem getHelpItem()
meth public org.netbeans.spi.editor.completion.CompletionTask createDocumentationTask()
meth public org.netbeans.spi.editor.completion.CompletionTask createToolTipTask()
meth public static java.lang.String hexColorCode(java.awt.Color)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createAttribute(org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute,java.lang.String,int,boolean,java.lang.String)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createAttributeValue(java.lang.String,int)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createAttributeValue(java.lang.String,int,boolean)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createAttributeValue(java.lang.String,int,boolean,int)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createAutocompleteEndTag(java.lang.String,int)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createBooleanAttribute(java.lang.String,int,boolean,java.lang.String)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createCharacterReference(java.lang.String,char,int,java.lang.String)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createCssValue(java.lang.String,int,boolean)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createEndTag(java.lang.String,int,java.lang.String,int,org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createEndTag(org.netbeans.modules.html.editor.lib.api.model.HtmlTag,java.lang.String,int,java.lang.String,int,org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createFileCompletionItem(org.openide.filesystems.FileObject,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createGoUpFileCompletionItem(int,java.awt.Color,javax.swing.ImageIcon)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem createTag(org.netbeans.modules.html.editor.lib.api.model.HtmlTag,java.lang.String,int,java.lang.String,boolean)
meth public void defaultAction(javax.swing.text.JTextComponent)
meth public void prepareHelp()
meth public void processKeyEvent(java.awt.event.KeyEvent)
meth public void render(java.awt.Graphics,java.awt.Font,java.awt.Color,java.awt.Color,int,int,boolean)
supr java.lang.Object
hfds END_FONT

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$Attribute
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons public init(java.lang.String,int,boolean,java.lang.String)
cons public init(java.lang.String,int,boolean,java.lang.String,boolean)
cons public init(java.lang.String,int,boolean,org.netbeans.modules.html.editor.lib.api.HelpItem)
cons public init(org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute,java.lang.String,int,boolean,java.lang.String)
meth protected int getMoveBackLength()
meth protected java.awt.Color getAttributeColor()
meth protected java.lang.String getLeftHtmlText()
meth protected java.lang.String getSubstituteText()
meth public boolean hasHelp()
meth public int getSortPriority()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds attr,autocompleteQuotes,required

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$AttributeValue
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons public init(java.lang.String,int,boolean)
cons public init(java.lang.String,int,boolean,int)
meth protected java.lang.String getSubstituteText()
meth public int getSortPriority()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds addQuotation,sortPriority

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$AutocompleteEndTag
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons public init(java.lang.String,int)
meth protected int getMoveBackLength()
meth public boolean instantSubstitution(javax.swing.text.JTextComponent)
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$BooleanAttribute
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons public init(java.lang.String,int,boolean,java.lang.String)
meth protected java.lang.String getLeftHtmlText()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds ATTR_NAME_COLOR,required

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$CharRefItem
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
meth protected java.lang.String getLeftHtmlText()
meth protected java.lang.String getRightHtmlText()
meth protected java.lang.String getSubstituteText()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds FG,value

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
innr public final static !enum Type
meth protected java.lang.String getLeftHtmlText()
meth protected java.lang.String getSubstituteText()
meth public boolean hasHelp()
meth public int getSortPriority()
meth public java.lang.CharSequence getSortText()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds orderIndex,tag,type

CLSS public final static !enum org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag
fld public final static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type DEFAULT
fld public final static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type OPTIONAL_EXISTING
fld public final static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type OPTIONAL_MISSING
fld public final static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type REQUIRED_EXISTING
fld public final static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type REQUIRED_MISSING
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type[] values()
supr java.lang.Enum<org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$EndTag$Type>
hfds bold,color,sortPriority

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$FileAttributeValue
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
intf java.beans.PropertyChangeListener
intf org.netbeans.spi.editor.completion.LazyCompletionItem
meth protected java.lang.String getLeftHtmlText()
meth protected javax.swing.ImageIcon getIcon()
meth public boolean accept()
meth public java.lang.CharSequence getSortText()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds color,folder,icon,visible

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$GoUpFileAttributeValue
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
meth public int getSortPriority()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$FileAttributeValue

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$HtmlCssValueCompletionItem
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
meth protected java.lang.String getLeftHtmlText()
meth protected javax.swing.ImageIcon getIcon()
meth public int getSortPriority()
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds GRAY_COLOR_CODE,ICON,RELATED_SELECTOR_COLOR,related

CLSS public static org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem$Tag
 outer org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
cons protected init(java.lang.String,int,java.lang.String,boolean)
cons protected init(org.netbeans.modules.html.editor.lib.api.model.HtmlTag,java.lang.String,int,java.lang.String,boolean)
meth protected java.lang.String getLeftHtmlText()
meth protected java.lang.String getRightHtmlText()
meth protected java.lang.String getSubstituteText()
meth protected javax.swing.ImageIcon getIcon()
meth public boolean hasHelp()
meth public int getSortPriority()
meth public void defaultAction(javax.swing.text.JTextComponent)
supr org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem
hfds DEFAULT_FG_COLOR,GRAY_COLOR,HTML_TAG_ICON,MATHML_TAG_ICON,SVG_TAG_ICON,possible,tag

CLSS public abstract interface org.netbeans.modules.html.editor.api.gsf.CustomAttribute
meth public abstract boolean isRequired()
meth public abstract boolean isValueRequired()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.html.editor.lib.api.HelpItem getHelp()

CLSS public abstract interface org.netbeans.modules.html.editor.api.gsf.CustomTag
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.html.editor.api.gsf.CustomAttribute> getAttributes()
meth public abstract org.netbeans.modules.html.editor.lib.api.HelpItem getHelp()

CLSS public abstract interface org.netbeans.modules.html.editor.api.gsf.ErrorBadgingRule

CLSS public final org.netbeans.modules.html.editor.api.gsf.HtmlErrorFilterContext
cons public init()
meth public boolean isOnlyBadging()
meth public void setOnlyBadging(boolean)
supr org.netbeans.modules.csl.api.RuleContext
hfds onlyBadging

CLSS public org.netbeans.modules.html.editor.api.gsf.HtmlExtension
cons public init()
innr public static CompletionContext
meth public boolean isApplicationPiece(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult)
meth public boolean isCustomAttribute(org.netbeans.modules.html.editor.lib.api.elements.Attribute,org.netbeans.modules.html.editor.lib.api.HtmlSource)
meth public boolean isCustomTag(org.netbeans.modules.html.editor.lib.api.elements.Named,org.netbeans.modules.html.editor.lib.api.HtmlSource)
meth public java.util.Collection<org.netbeans.modules.html.editor.api.gsf.CustomAttribute> getCustomAttributes(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Collection<org.netbeans.modules.html.editor.api.gsf.CustomTag> getCustomTags()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<org.netbeans.spi.editor.completion.CompletionItem> completeAttributeValue(org.netbeans.modules.html.editor.api.gsf.HtmlExtension$CompletionContext)
meth public java.util.List<org.netbeans.spi.editor.completion.CompletionItem> completeAttributes(org.netbeans.modules.html.editor.api.gsf.HtmlExtension$CompletionContext)
meth public java.util.List<org.netbeans.spi.editor.completion.CompletionItem> completeOpenTags(org.netbeans.modules.html.editor.api.gsf.HtmlExtension$CompletionContext)
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getUndeclaredNamespaces(org.netbeans.modules.html.editor.lib.api.HtmlSource)
meth public java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.util.Set<org.netbeans.modules.csl.api.ColoringAttributes>> getHighlights(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,org.netbeans.modules.parsing.spi.SchedulerEvent)
meth public org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation findDeclaration(org.netbeans.modules.csl.spi.ParserResult,int)
meth public org.netbeans.modules.csl.api.OffsetRange getReferenceSpan(javax.swing.text.Document,int)
meth public void computeErrors(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,java.util.List<org.netbeans.modules.csl.api.Error>)
meth public void computeSelectionHints(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,int,int)
meth public void computeSuggestions(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,int)
supr java.lang.Object

CLSS public static org.netbeans.modules.html.editor.api.gsf.HtmlExtension$CompletionContext
 outer org.netbeans.modules.html.editor.api.gsf.HtmlExtension
cons public init(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,int,int,int,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,int,int,int,java.lang.String,java.lang.String,org.netbeans.modules.html.editor.lib.api.elements.Element)
cons public init(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,int,int,int,java.lang.String,java.lang.String,org.netbeans.modules.html.editor.lib.api.elements.Element,java.lang.String,boolean)
meth public boolean isValueQuoted()
meth public int getAstoffset()
meth public int getCCItemStartOffset()
meth public int getOriginalOffset()
meth public java.lang.String getAttributeName()
meth public java.lang.String getItemText()
meth public java.lang.String getPrefix()
meth public org.netbeans.modules.html.editor.api.gsf.HtmlParserResult getResult()
meth public org.netbeans.modules.html.editor.lib.api.elements.Element getCurrentNode()
supr java.lang.Object
hfds astoffset,attributeName,ccItemStartOffset,currentNode,itemText,originalOffset,preText,result,valueQuoted

CLSS public org.netbeans.modules.html.editor.api.gsf.HtmlParserResult
fld public final static java.lang.String FALLBACK_DTD_PROPERTY_NAME = "fallbackDTD"
intf org.netbeans.modules.html.editor.lib.api.HtmlParsingResult
meth protected void invalidate()
meth public boolean isValid()
meth public java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
meth public java.util.List<org.netbeans.modules.csl.api.Error> getDiagnostics(java.util.Set<org.netbeans.modules.csl.api.Severity>)
meth public java.util.Map<java.lang.String,java.lang.String> getNamespaces()
meth public java.util.Map<java.lang.String,org.netbeans.modules.html.editor.lib.api.elements.Node> roots()
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion getDetectedHtmlVersion()
meth public org.netbeans.modules.html.editor.lib.api.HtmlVersion getHtmlVersion()
meth public org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult getSyntaxAnalyzerResult()
meth public org.netbeans.modules.html.editor.lib.api.elements.Element findByPhysicalRange(int,boolean)
meth public org.netbeans.modules.html.editor.lib.api.elements.Node findBySemanticRange(int,boolean)
meth public org.netbeans.modules.html.editor.lib.api.elements.Node root()
meth public org.netbeans.modules.html.editor.lib.api.elements.Node root(java.lang.String)
meth public org.netbeans.modules.html.editor.lib.api.elements.Node rootOfUndeclaredTagsParseTree()
meth public static org.netbeans.modules.html.editor.lib.api.elements.Node getBoundNode(org.netbeans.modules.csl.api.Error)
supr org.netbeans.modules.csl.spi.ParserResult
hfds errors,isValid,result
hcls Accessor,Lkp

CLSS public org.netbeans.modules.html.editor.api.index.HtmlIndex
fld public final static int VERSION = 3
fld public final static java.lang.String NAME = "html"
fld public final static java.lang.String REFERS_KEY = "imports"
innr public static AllDependenciesMaps
meth public java.util.Collection<org.openide.filesystems.FileObject> find(java.lang.String,java.lang.String)
meth public java.util.List<java.net.URL> getAllRemoteDependencies() throws java.io.IOException
meth public org.netbeans.modules.html.editor.api.index.HtmlIndex$AllDependenciesMaps getAllDependencies() throws java.io.IOException
meth public org.netbeans.modules.web.common.api.DependenciesGraph getDependencies(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.html.editor.api.index.HtmlIndex get(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.netbeans.modules.html.editor.api.index.HtmlIndex get(org.netbeans.api.project.Project,boolean) throws java.io.IOException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void notifyChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds INDEXES,changeSupport,querySupport

CLSS public static org.netbeans.modules.html.editor.api.index.HtmlIndex$AllDependenciesMaps
 outer org.netbeans.modules.html.editor.api.index.HtmlIndex
cons public init(java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>,java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getDest2source()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getSource2dest()
supr java.lang.Object
hfds dest2source,source2dest

CLSS public abstract interface org.netbeans.modules.html.editor.lib.api.HtmlParsingResult
meth public abstract java.util.Map<java.lang.String,java.lang.String> getNamespaces()
meth public abstract java.util.Map<java.lang.String,org.netbeans.modules.html.editor.lib.api.elements.Node> roots()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion getDetectedHtmlVersion()
meth public abstract org.netbeans.modules.html.editor.lib.api.HtmlVersion getHtmlVersion()
meth public abstract org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzerResult getSyntaxAnalyzerResult()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node root()
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node root(java.lang.String)
meth public abstract org.netbeans.modules.html.editor.lib.api.elements.Node rootOfUndeclaredTagsParseTree()

CLSS public abstract org.netbeans.modules.html.editor.spi.HintFixProvider
cons public init()
fld public final static java.lang.String UNKNOWN_ATTRIBUTE_FOUND = "unknown_attribute_found"
fld public final static java.lang.String UNKNOWN_ELEMENT_CONTEXT = "unknown_element_context"
fld public final static java.lang.String UNKNOWN_ELEMENT_FOUND = "unknown_element_found"
innr public final static Context
meth public abstract java.util.List<org.netbeans.modules.csl.api.HintFix> getHintFixes(org.netbeans.modules.html.editor.spi.HintFixProvider$Context)
supr java.lang.Object

CLSS public final static org.netbeans.modules.html.editor.spi.HintFixProvider$Context
 outer org.netbeans.modules.html.editor.spi.HintFixProvider
cons public init(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,java.util.Map<java.lang.String,java.lang.Object>)
meth public java.util.Map<java.lang.String,java.lang.Object> getMetadata()
meth public org.netbeans.modules.html.editor.api.gsf.HtmlParserResult getResult()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds metadata,result,snapshot

CLSS public abstract org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin
cons public init()
meth public abstract boolean processToken()
meth public abstract boolean startProcessing(org.netbeans.modules.html.editor.api.gsf.HtmlParserResult,org.netbeans.modules.parsing.api.Snapshot,org.netbeans.api.lexer.TokenSequence<org.netbeans.api.html.lexer.HTMLTokenId>,java.util.List<org.netbeans.modules.parsing.api.Embedding>)
meth public void endProcessing()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

CLSS public abstract interface org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean instantSubstitution(javax.swing.text.JTextComponent)
meth public abstract int getPreferredWidth(java.awt.Graphics,java.awt.Font)
meth public abstract int getSortPriority()
meth public abstract java.lang.CharSequence getInsertPrefix()
meth public abstract java.lang.CharSequence getSortText()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask createDocumentationTask()
meth public abstract org.netbeans.spi.editor.completion.CompletionTask createToolTipTask()
meth public abstract void defaultAction(javax.swing.text.JTextComponent)
meth public abstract void processKeyEvent(java.awt.event.KeyEvent)
meth public abstract void render(java.awt.Graphics,java.awt.Font,java.awt.Color,java.awt.Color,int,int,boolean)
meth public boolean shouldSingleClickInvokeDefaultAction()

CLSS public abstract interface org.netbeans.spi.editor.completion.LazyCompletionItem
intf org.netbeans.spi.editor.completion.CompletionItem
meth public abstract boolean accept()

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

