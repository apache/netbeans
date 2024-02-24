#Signature file v4.1
#Version 2.81.0

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.EventListener

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

CLSS public abstract interface javax.swing.event.DocumentListener
intf java.util.EventListener
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent)

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

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public abstract interface org.netbeans.api.lexer.TokenHierarchyListener
intf java.util.EventListener
meth public abstract void tokenHierarchyChanged(org.netbeans.api.lexer.TokenHierarchyEvent)

CLSS public abstract org.netbeans.editor.BaseAction
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
fld protected int updateMask
fld public final static int ABBREV_RESET = 4
 anno 0 java.lang.Deprecated()
fld public final static int CLEAR_STATUS_TEXT = 32
fld public final static int MAGIC_POSITION_RESET = 2
fld public final static int NO_RECORDING = 64
fld public final static int SAVE_POSITION = 128
fld public final static int SELECTION_REMOVE = 1
fld public final static int UNDO_MERGE_RESET = 8
fld public final static int WORD_MATCH_RESET = 16
fld public final static java.lang.String ICON_RESOURCE_PROPERTY = "IconResource"
fld public final static java.lang.String LOCALE_DESC_PREFIX = "desc-"
fld public final static java.lang.String LOCALE_POPUP_PREFIX = "popup-"
fld public final static java.lang.String NO_KEYBINDING = "no-keybinding"
fld public final static java.lang.String POPUP_MENU_TEXT = "PopupMenuText"
meth protected boolean asynchonous()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth protected java.lang.Object createDefaultValue(java.lang.String)
meth protected java.lang.Object findValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getDefaultShortDescription()
meth protected void actionNameUpdate(java.lang.String)
meth public abstract void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public final void actionPerformed(java.awt.event.ActionEvent)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getPopupMenuText(javax.swing.text.JTextComponent)
meth public javax.swing.JMenuItem getPopupMenuItem(javax.swing.text.JTextComponent)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void updateComponent(javax.swing.text.JTextComponent)
meth public void updateComponent(javax.swing.text.JTextComponent,int)
supr javax.swing.text.TextAction
hfds UILOG,UI_LOG_DETAILED,recording,serialVersionUID

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

CLSS public static org.netbeans.editor.ext.ExtKit$GotoDeclarationAction
 outer org.netbeans.editor.ext.ExtKit
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public boolean gotoDeclaration(javax.swing.text.JTextComponent)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds serialVersionUID

CLSS public abstract org.netbeans.modules.csl.api.AbstractCamelCasePosition
cons public init(java.lang.String,javax.swing.Action)
meth protected abstract int newOffset(javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
meth protected abstract void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public final void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
meth public java.lang.String getShortDescription()
supr org.netbeans.editor.BaseAction
hfds originalAction

CLSS public abstract org.netbeans.modules.csl.api.CodeCompletionContext
cons public init()
meth public abstract boolean isCaseSensitive()
meth public abstract boolean isPrefixMatch()
meth public abstract int getCaretOffset()
meth public abstract java.lang.String getPrefix()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType getQueryType()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.spi.ParserResult getParserResult()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.csl.api.CodeCompletionHandler
innr public final static !enum QueryType
meth public abstract java.lang.String document(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getPrefix(org.netbeans.modules.csl.spi.ParserResult,int,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String resolveTemplateVariable(java.lang.String,org.netbeans.modules.csl.spi.ParserResult,int,java.lang.String,java.util.Map)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Set<java.lang.String> getApplicableTemplates(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType getAutoQuery(javax.swing.text.JTextComponent,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.CodeCompletionResult complete(org.netbeans.modules.csl.api.CodeCompletionContext)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementHandle resolveLink(java.lang.String,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ParameterInfo parameters(org.netbeans.modules.csl.spi.ParserResult,int,org.netbeans.modules.csl.api.CompletionProposal)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()

CLSS public final static !enum org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType
 outer org.netbeans.modules.csl.api.CodeCompletionHandler
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType ALL_COMPLETION
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType COMPLETION
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType DOCUMENTATION
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType NONE
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType STOP
fld public final static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType TOOLTIP
meth public static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.CodeCompletionHandler$QueryType>

CLSS public abstract interface org.netbeans.modules.csl.api.CodeCompletionHandler2
intf org.netbeans.modules.csl.api.CodeCompletionHandler
meth public abstract org.netbeans.modules.csl.api.Documentation documentElement(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.modules.csl.api.CodeCompletionResult
cons public init()
fld public final static org.netbeans.modules.csl.api.CodeCompletionResult NONE
meth public abstract boolean isFilterable()
meth public abstract boolean isTruncated()
meth public abstract java.util.List<org.netbeans.modules.csl.api.CompletionProposal> getItems()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public boolean insert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void afterInsert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void beforeInsert(org.netbeans.modules.csl.api.CompletionProposal)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.csl.api.ColoringAttributes
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> CLASS_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> CONSTRUCTOR_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> CUSTOM1_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> CUSTOM2_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> CUSTOM3_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> FIELD_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> GLOBAL_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> METHOD_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> PARAMETER_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> REGEXP_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> STATIC_FIELD_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> STATIC_SET
fld public final static java.util.EnumSet<org.netbeans.modules.csl.api.ColoringAttributes> UNUSED_SET
fld public final static org.netbeans.modules.csl.api.ColoringAttributes ABSTRACT
fld public final static org.netbeans.modules.csl.api.ColoringAttributes ANNOTATION_TYPE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes CLASS
fld public final static org.netbeans.modules.csl.api.ColoringAttributes CONSTRUCTOR
fld public final static org.netbeans.modules.csl.api.ColoringAttributes CUSTOM1
fld public final static org.netbeans.modules.csl.api.ColoringAttributes CUSTOM2
fld public final static org.netbeans.modules.csl.api.ColoringAttributes CUSTOM3
fld public final static org.netbeans.modules.csl.api.ColoringAttributes DECLARATION
fld public final static org.netbeans.modules.csl.api.ColoringAttributes DEPRECATED
fld public final static org.netbeans.modules.csl.api.ColoringAttributes ENUM
fld public final static org.netbeans.modules.csl.api.ColoringAttributes FIELD
fld public final static org.netbeans.modules.csl.api.ColoringAttributes GLOBAL
fld public final static org.netbeans.modules.csl.api.ColoringAttributes INTERFACE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes LOCAL_VARIABLE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes LOCAL_VARIABLE_DECLARATION
fld public final static org.netbeans.modules.csl.api.ColoringAttributes MARK_OCCURRENCES
fld public final static org.netbeans.modules.csl.api.ColoringAttributes METHOD
fld public final static org.netbeans.modules.csl.api.ColoringAttributes PACKAGE_PRIVATE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes PARAMETER
fld public final static org.netbeans.modules.csl.api.ColoringAttributes PRIVATE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes PROTECTED
fld public final static org.netbeans.modules.csl.api.ColoringAttributes PUBLIC
fld public final static org.netbeans.modules.csl.api.ColoringAttributes REGEXP
fld public final static org.netbeans.modules.csl.api.ColoringAttributes STATIC
fld public final static org.netbeans.modules.csl.api.ColoringAttributes TYPE_PARAMETER_DECLARATION
fld public final static org.netbeans.modules.csl.api.ColoringAttributes TYPE_PARAMETER_USE
fld public final static org.netbeans.modules.csl.api.ColoringAttributes UNDEFINED
fld public final static org.netbeans.modules.csl.api.ColoringAttributes UNUSED
innr public final static Coloring
meth public static org.netbeans.modules.csl.api.ColoringAttributes valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.ColoringAttributes$Coloring add(org.netbeans.modules.csl.api.ColoringAttributes$Coloring,org.netbeans.modules.csl.api.ColoringAttributes)
meth public static org.netbeans.modules.csl.api.ColoringAttributes$Coloring empty()
meth public static org.netbeans.modules.csl.api.ColoringAttributes[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.ColoringAttributes>

CLSS public final static org.netbeans.modules.csl.api.ColoringAttributes$Coloring
 outer org.netbeans.modules.csl.api.ColoringAttributes
intf java.util.Collection<org.netbeans.modules.csl.api.ColoringAttributes>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add(org.netbeans.modules.csl.api.ColoringAttributes)
meth public boolean addAll(java.util.Collection<? extends org.netbeans.modules.csl.api.ColoringAttributes>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int hashCode()
meth public int size()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public java.util.Iterator<org.netbeans.modules.csl.api.ColoringAttributes> iterator()
meth public void clear()
supr java.lang.Object
hfds value

CLSS public abstract interface org.netbeans.modules.csl.api.CompletionProposal
meth public abstract boolean isSmart()
meth public abstract int getAnchorOffset()
meth public abstract int getSortPrioOverride()
meth public abstract java.lang.String getCustomInsertTemplate()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getInsertPrefix()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getSortText()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.ImageIcon getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getElement()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.csl.api.CslActions
meth public static javax.swing.Action createCamelCasePositionAction(javax.swing.Action,boolean)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action createDeleteToCamelCasePositionAction(javax.swing.Action,boolean)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action createGoToDeclarationAction()
meth public static javax.swing.Action createGoToMarkOccurrencesAction(boolean)
meth public static javax.swing.Action createInstantRenameAction()
meth public static javax.swing.Action createSelectCamelCasePositionAction(javax.swing.Action,boolean)
 anno 0 java.lang.Deprecated()
meth public static javax.swing.Action createSelectCodeElementAction(boolean)
meth public static javax.swing.Action createToggleBlockCommentAction()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.csl.api.DataLoadersBridge
cons public init()
meth public <%0 extends java.lang.Object> {%%0} getCookie(javax.swing.text.JTextComponent,java.lang.Class<{%%0}>) throws java.io.IOException
meth public abstract <%0 extends java.lang.Object> {%%0} getCookie(org.openide.filesystems.FileObject,java.lang.Class<{%%0}>) throws java.io.IOException
meth public abstract <%0 extends java.lang.Object> {%%0} getSafeCookie(org.openide.filesystems.FileObject,java.lang.Class<{%%0}>)
meth public abstract java.beans.PropertyChangeListener getDataObjectListener(org.openide.filesystems.FileObject,org.openide.filesystems.FileChangeListener) throws java.io.IOException
meth public abstract java.lang.Object createInstance(org.openide.filesystems.FileObject)
meth public abstract java.lang.String getLine(javax.swing.text.Document,int)
meth public abstract javax.swing.JEditorPane[] getOpenedPanes(org.openide.filesystems.FileObject)
meth public abstract javax.swing.text.StyledDocument getDocument(org.openide.filesystems.FileObject)
meth public abstract org.openide.cookies.EditorCookie isModified(org.openide.filesystems.FileObject)
meth public abstract org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
meth public abstract org.openide.filesystems.FileObject getPrimaryFile(org.openide.filesystems.FileObject)
meth public abstract org.openide.nodes.Node getNodeDelegate(javax.swing.text.JTextComponent)
meth public org.openide.filesystems.FileObject getFileObject(javax.swing.text.JTextComponent)
meth public static org.netbeans.modules.csl.api.DataLoadersBridge getDefault()
supr java.lang.Object
hfds instance

CLSS public abstract interface org.netbeans.modules.csl.api.DeclarationFinder
innr public abstract interface static AlternativeLocation
innr public final static DeclarationLocation
meth public abstract org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation findDeclaration(org.netbeans.modules.csl.spi.ParserResult,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getReferenceSpan(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation
 outer org.netbeans.modules.csl.api.DeclarationFinder
intf java.lang.Comparable<org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation>
meth public abstract java.lang.String getDisplayHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public abstract org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation getLocation()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getElement()

CLSS public final static org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation
 outer org.netbeans.modules.csl.api.DeclarationFinder
cons public init(java.net.URL)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(org.openide.filesystems.FileObject,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(org.openide.filesystems.FileObject,int,org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
fld public final static org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation NONE
meth public int getOffset()
meth public java.lang.String getInvalidMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public java.net.URL getUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation> getAlternativeLocations()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.csl.api.ElementHandle getElement()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void addAlternative(org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setInvalidMessage(java.lang.String)
supr java.lang.Object
hfds alternatives,element,fileObject,invalidMessage,offset,url

CLSS public final org.netbeans.modules.csl.api.DeleteToNextCamelCasePosition
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.Action)
fld public final static java.lang.String deleteNextCamelCasePosition = "delete-next-camel-case-position"
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.SelectNextCamelCasePosition

CLSS public final org.netbeans.modules.csl.api.DeleteToPreviousCamelCasePosition
 anno 0 java.lang.Deprecated()
cons public init(javax.swing.Action)
fld public final static java.lang.String deletePreviousCamelCasePosition = "delete-previous-camel-case-position"
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.SelectPreviousCamelCasePosition

CLSS public final org.netbeans.modules.csl.api.Documentation
meth public java.lang.String getContent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.net.URL getUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.csl.api.Documentation create(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.csl.api.Documentation create(java.lang.String,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds content,url

CLSS public final org.netbeans.modules.csl.api.EditHistory
cons public init()
intf javax.swing.event.DocumentListener
intf org.netbeans.api.lexer.TokenHierarchyListener
meth public boolean isInDamagedRegion(int)
meth public boolean isInDamagedRegion(org.netbeans.modules.csl.api.OffsetRange)
meth public boolean isValid()
meth public boolean wasModified(org.netbeans.api.lexer.TokenId)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public int convertEditedToOriginal(int)
meth public int convertOriginalToEdited(int)
meth public int getEditedEnd()
meth public int getEditedSize()
meth public int getOriginalEnd()
meth public int getOriginalSize()
meth public int getSizeDelta()
meth public int getStart()
meth public int getVersion()
meth public java.lang.String toString()
meth public static org.netbeans.modules.csl.api.EditHistory getCombinedEdits(int,org.netbeans.modules.csl.api.EditHistory)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void add(org.netbeans.modules.csl.api.EditHistory)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void changed(org.netbeans.api.lexer.TokenChange)
meth public void changedUpdate(javax.swing.event.DocumentEvent)
meth public void insertUpdate(javax.swing.event.DocumentEvent)
meth public void removeUpdate(javax.swing.event.DocumentEvent)
meth public void setValid(boolean)
meth public void testHelperNotifyToken(boolean,org.netbeans.api.lexer.TokenId)
meth public void tokenHierarchyChanged(org.netbeans.api.lexer.TokenHierarchyEvent)
supr java.lang.Object
hfds ADDED,MAX_KEEP,REMOVED,delta,editedEnd,edits,originalEnd,previous,start,tokenIds,valid,version
hcls Edit

CLSS public org.netbeans.modules.csl.api.EditList
cons public init(javax.swing.text.Document)
cons public init(org.netbeans.editor.BaseDocument)
innr public final static Edit
meth public int firstEditLine(javax.swing.text.Document)
meth public int firstLine(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public javax.swing.text.Position createPosition(int)
meth public javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias)
meth public org.netbeans.modules.csl.api.EditList replace(int,int,java.lang.String,boolean,int)
meth public org.netbeans.modules.csl.api.OffsetRange getRange()
meth public void apply()
meth public void applyTo(javax.swing.text.Document)
meth public void applyToDocument(org.netbeans.editor.BaseDocument)
 anno 0 java.lang.Deprecated()
meth public void setFormatAll(boolean)
supr java.lang.Object
hfds LOG,doc,edits,formatAll,positions
hcls DelegatedPosition

CLSS public final static org.netbeans.modules.csl.api.EditList$Edit
 outer org.netbeans.modules.csl.api.EditList
intf java.lang.Comparable<org.netbeans.modules.csl.api.EditList$Edit>
meth public int compareTo(org.netbeans.modules.csl.api.EditList$Edit)
meth public int getOffset()
meth public int getRemoveLen()
meth public java.lang.String getInsertText()
meth public java.lang.String toString()
supr java.lang.Object
hfds format,insertText,offset,offsetOrdinal,removeLen

CLSS public abstract org.netbeans.modules.csl.api.EditRegions
cons public init()
meth public abstract void edit(org.openide.filesystems.FileObject,java.util.Set<org.netbeans.modules.csl.api.OffsetRange>,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.csl.api.EditRegions getInstance()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds editRegions

CLSS public final org.netbeans.modules.csl.api.EditorOptions
meth public boolean getExpandTabs()
meth public boolean getMatchBrackets()
meth public int getRightMargin()
meth public int getSpacesPerTab()
meth public int getTabSize()
meth public static org.netbeans.modules.csl.api.EditorOptions get(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds mimeType,preferences

CLSS public abstract interface org.netbeans.modules.csl.api.ElementHandle
innr public static UrlHandle
meth public abstract boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIn()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public static org.netbeans.modules.csl.api.ElementHandle$UrlHandle
 outer org.netbeans.modules.csl.api.ElementHandle
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
intf org.netbeans.modules.csl.api.ElementHandle
meth public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String getIn()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds url

CLSS public final !enum org.netbeans.modules.csl.api.ElementKind
fld public final static org.netbeans.modules.csl.api.ElementKind ATTRIBUTE
fld public final static org.netbeans.modules.csl.api.ElementKind CALL
fld public final static org.netbeans.modules.csl.api.ElementKind CLASS
fld public final static org.netbeans.modules.csl.api.ElementKind CONSTANT
fld public final static org.netbeans.modules.csl.api.ElementKind CONSTRUCTOR
fld public final static org.netbeans.modules.csl.api.ElementKind DB
fld public final static org.netbeans.modules.csl.api.ElementKind ERROR
fld public final static org.netbeans.modules.csl.api.ElementKind FIELD
fld public final static org.netbeans.modules.csl.api.ElementKind FILE
fld public final static org.netbeans.modules.csl.api.ElementKind GLOBAL
fld public final static org.netbeans.modules.csl.api.ElementKind INTERFACE
fld public final static org.netbeans.modules.csl.api.ElementKind KEYWORD
fld public final static org.netbeans.modules.csl.api.ElementKind METHOD
fld public final static org.netbeans.modules.csl.api.ElementKind MODULE
fld public final static org.netbeans.modules.csl.api.ElementKind OTHER
fld public final static org.netbeans.modules.csl.api.ElementKind PACKAGE
fld public final static org.netbeans.modules.csl.api.ElementKind PARAMETER
fld public final static org.netbeans.modules.csl.api.ElementKind PROPERTY
fld public final static org.netbeans.modules.csl.api.ElementKind RULE
fld public final static org.netbeans.modules.csl.api.ElementKind TAG
fld public final static org.netbeans.modules.csl.api.ElementKind TEST
fld public final static org.netbeans.modules.csl.api.ElementKind VARIABLE
meth public static org.netbeans.modules.csl.api.ElementKind valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.ElementKind[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.ElementKind>

CLSS public abstract interface org.netbeans.modules.csl.api.Error
innr public abstract interface static Badging
meth public abstract boolean isLineError()
meth public abstract int getEndPosition()
meth public abstract int getStartPosition()
meth public abstract java.lang.Object[] getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getKey()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.Severity getSeverity()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static org.netbeans.modules.csl.api.Error$Badging
 outer org.netbeans.modules.csl.api.Error
intf org.netbeans.modules.csl.api.Error
meth public abstract boolean showExplorerBadge()

CLSS public abstract interface org.netbeans.modules.csl.api.Formatter
meth public abstract boolean needsParserResult()
meth public abstract int hangingIndentSize()
meth public abstract int indentSize()
meth public abstract void reformat(org.netbeans.modules.editor.indent.spi.Context,org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void reindent(org.netbeans.modules.editor.indent.spi.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.csl.api.GoToDeclarationAction
cons public init()
meth public boolean gotoDeclaration(javax.swing.text.JTextComponent)
supr org.netbeans.editor.ext.ExtKit$GotoDeclarationAction

CLSS public final org.netbeans.modules.csl.api.GoToMarkOccurrencesAction
 anno 0 java.lang.Deprecated()
cons public init(boolean)
meth protected java.lang.Object getDefaultShortDescription()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds next,nextActionName,prevActionName

CLSS public abstract interface org.netbeans.modules.csl.api.GsfLanguage
meth public abstract boolean isIdentifierChar(char)
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getLineCommentPrefix()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getPreferredExtension()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<java.lang.String> getBinaryLibraryPathIds()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Set<java.lang.String> getLibraryPathIds()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Set<java.lang.String> getSourcePathIds()
 anno 0 java.lang.Deprecated()
meth public abstract org.netbeans.api.lexer.Language getLexerLanguage()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.modules.csl.api.Hint
cons public init(org.netbeans.modules.csl.api.Rule,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.csl.api.OffsetRange,java.util.List<org.netbeans.modules.csl.api.HintFix>,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public int getPriority()
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.csl.api.HintFix> getFixes()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.OffsetRange getRange()
meth public org.netbeans.modules.csl.api.Rule getRule()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds description,file,fixes,priority,range,rule

CLSS public abstract interface org.netbeans.modules.csl.api.HintFix
meth public abstract boolean isInteractive()
meth public abstract boolean isSafe()
meth public abstract java.lang.String getDescription()
meth public abstract void implement() throws java.lang.Exception

CLSS public final !enum org.netbeans.modules.csl.api.HintSeverity
fld public final static org.netbeans.modules.csl.api.HintSeverity CURRENT_LINE_WARNING
fld public final static org.netbeans.modules.csl.api.HintSeverity ERROR
fld public final static org.netbeans.modules.csl.api.HintSeverity INFO
fld public final static org.netbeans.modules.csl.api.HintSeverity WARNING
meth public org.netbeans.spi.editor.hints.Severity toEditorSeverity()
meth public static org.netbeans.modules.csl.api.HintSeverity valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.HintSeverity[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.HintSeverity>

CLSS public abstract interface org.netbeans.modules.csl.api.HintsProvider
innr public abstract static HintsManager
meth public abstract java.util.List<org.netbeans.modules.csl.api.Rule> getBuiltinRules()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.RuleContext createRuleContext()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void cancel()
meth public abstract void computeErrors(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,java.util.List<org.netbeans.modules.csl.api.Error>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public abstract void computeHints(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void computeSelectionHints(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void computeSuggestions(org.netbeans.modules.csl.api.HintsProvider$HintsManager,org.netbeans.modules.csl.api.RuleContext,java.util.List<org.netbeans.modules.csl.api.Hint>,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract static org.netbeans.modules.csl.api.HintsProvider$HintsManager
 outer org.netbeans.modules.csl.api.HintsProvider
cons public init()
meth public abstract boolean isEnabled(org.netbeans.modules.csl.api.Rule$UserConfigurableRule)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Rule$SelectionRule> getSelectionHints()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<?,java.util.List<? extends org.netbeans.modules.csl.api.Rule$AstRule>> getHints()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<?,java.util.List<? extends org.netbeans.modules.csl.api.Rule$AstRule>> getHints(boolean,org.netbeans.modules.csl.api.RuleContext)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<?,java.util.List<? extends org.netbeans.modules.csl.api.Rule$AstRule>> getSuggestions()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<?,java.util.List<? extends org.netbeans.modules.csl.api.Rule$ErrorRule>> getErrors()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.prefs.Preferences getPreferences(org.netbeans.modules.csl.api.Rule$UserConfigurableRule)
meth public abstract org.netbeans.spi.options.OptionsPanelController getOptionsController()
meth public abstract void refreshHints(org.netbeans.modules.csl.api.RuleContext)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final static org.netbeans.modules.csl.api.HintsProvider$HintsManager getManagerForMimeType(java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.csl.api.HtmlFormatter
cons public init()
fld protected int maxLength
fld protected int textLength
meth public abstract java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void active(boolean)
meth public abstract void appendHtml(java.lang.String)
meth public abstract void appendText(java.lang.String,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void deprecated(boolean)
meth public abstract void emphasis(boolean)
meth public abstract void name(org.netbeans.modules.csl.api.ElementKind,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void parameters(boolean)
meth public abstract void reset()
meth public abstract void type(boolean)
meth public void appendText(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setMaxLength(int)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.csl.api.IndexSearcher
innr public abstract interface static Helper
innr public abstract static Descriptor
meth public abstract java.util.Set<? extends org.netbeans.modules.csl.api.IndexSearcher$Descriptor> getSymbols(org.netbeans.api.project.Project,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,org.netbeans.modules.csl.api.IndexSearcher$Helper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<? extends org.netbeans.modules.csl.api.IndexSearcher$Descriptor> getTypes(org.netbeans.api.project.Project,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,org.netbeans.modules.csl.api.IndexSearcher$Helper)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract static org.netbeans.modules.csl.api.IndexSearcher$Descriptor
 outer org.netbeans.modules.csl.api.IndexSearcher
cons public init()
meth public abstract int getOffset()
meth public abstract java.lang.String getContextName()
meth public abstract java.lang.String getOuterName()
meth public abstract java.lang.String getProjectName()
meth public abstract java.lang.String getSimpleName()
meth public abstract java.lang.String getTypeName()
meth public abstract javax.swing.Icon getIcon()
meth public abstract javax.swing.Icon getProjectIcon()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getElement()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void open()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.csl.api.IndexSearcher$Helper
 outer org.netbeans.modules.csl.api.IndexSearcher
meth public abstract javax.swing.Icon getIcon(org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void open(org.openide.filesystems.FileObject,org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.modules.csl.api.InstantRenameAction
 anno 0 java.lang.Deprecated()
cons public init()
meth protected java.lang.Class getShortDescriptionBundleClass()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction

CLSS public abstract interface org.netbeans.modules.csl.api.InstantRenamer
meth public abstract boolean isRenameAllowed(org.netbeans.modules.csl.spi.ParserResult,int,java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.OffsetRange> getRenameRegions(org.netbeans.modules.csl.spi.ParserResult,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.csl.api.KeystrokeHandler
meth public abstract boolean afterCharInserted(javax.swing.text.Document,int,javax.swing.text.JTextComponent,char) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean beforeCharInserted(javax.swing.text.Document,int,javax.swing.text.JTextComponent,char) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean charBackspaced(javax.swing.text.Document,int,javax.swing.text.JTextComponent,char) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract int beforeBreak(javax.swing.text.Document,int,javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract int getNextWordOffset(javax.swing.text.Document,int,boolean)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.List<org.netbeans.modules.csl.api.OffsetRange> findLogicalRanges(org.netbeans.modules.csl.spi.ParserResult,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange findMatching(javax.swing.text.Document,int)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final !enum org.netbeans.modules.csl.api.Modifier
fld public final static org.netbeans.modules.csl.api.Modifier ABSTRACT
fld public final static org.netbeans.modules.csl.api.Modifier DEPRECATED
fld public final static org.netbeans.modules.csl.api.Modifier PRIVATE
fld public final static org.netbeans.modules.csl.api.Modifier PROTECTED
fld public final static org.netbeans.modules.csl.api.Modifier PUBLIC
fld public final static org.netbeans.modules.csl.api.Modifier STATIC
meth public static org.netbeans.modules.csl.api.Modifier valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.Modifier[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.Modifier>

CLSS public org.netbeans.modules.csl.api.NextCamelCasePosition
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String,javax.swing.Action)
cons public init(javax.swing.Action)
fld public final static java.lang.String nextCamelCasePosition = "next-camel-case-position"
meth protected int newOffset(javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.AbstractCamelCasePosition

CLSS public abstract org.netbeans.modules.csl.api.OccurrencesFinder<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,org.netbeans.modules.csl.api.ColoringAttributes> getOccurrences()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setCaretPosition(int)
supr org.netbeans.modules.parsing.spi.ParserResultTask<{org.netbeans.modules.csl.api.OccurrencesFinder%0}>

CLSS public final org.netbeans.modules.csl.api.OffsetRange
cons public init(int,int)
fld public final static org.netbeans.modules.csl.api.OffsetRange NONE
intf java.lang.Comparable<org.netbeans.modules.csl.api.OffsetRange>
meth public boolean containsInclusive(int)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean overlaps(org.netbeans.modules.csl.api.OffsetRange)
meth public int compareTo(org.netbeans.modules.csl.api.OffsetRange)
meth public int getEnd()
meth public int getLength()
meth public int getStart()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.api.OffsetRange boundTo(int,int)
supr java.lang.Object
hfds end,start

CLSS public abstract interface org.netbeans.modules.csl.api.OverridingMethods
meth public abstract boolean isOverriddenBySupported(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation> overriddenBy(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends org.netbeans.modules.csl.api.DeclarationFinder$AlternativeLocation> overrides(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.modules.csl.api.ParameterInfo
cons public init(java.util.List<java.lang.String>,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
fld public final static org.netbeans.modules.csl.api.ParameterInfo NONE
meth public int getAnchorOffset()
meth public int getCurrentIndex()
meth public java.util.List<java.lang.String> getNames()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds anchorOffset,index,names

CLSS public abstract interface org.netbeans.modules.csl.api.PreviewableFix
intf org.netbeans.modules.csl.api.HintFix
meth public abstract boolean canPreview()
meth public abstract org.netbeans.modules.csl.api.EditList getEditList() throws java.lang.Exception

CLSS public org.netbeans.modules.csl.api.PreviousCamelCasePosition
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String,javax.swing.Action)
cons public init(javax.swing.Action)
fld public final static java.lang.String previousCamelCasePosition = "previous-camel-case-position"
meth protected int newOffset(javax.swing.text.JTextComponent) throws javax.swing.text.BadLocationException
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.AbstractCamelCasePosition

CLSS public abstract interface org.netbeans.modules.csl.api.Rule
innr public abstract interface static AstRule
innr public abstract interface static ErrorRule
innr public abstract interface static SelectionRule
innr public abstract interface static UserConfigurableRule
meth public abstract boolean appliesTo(org.netbeans.modules.csl.api.RuleContext)
meth public abstract boolean showInTasklist()
meth public abstract java.lang.String getDisplayName()
meth public abstract org.netbeans.modules.csl.api.HintSeverity getDefaultSeverity()

CLSS public abstract interface static org.netbeans.modules.csl.api.Rule$AstRule
 outer org.netbeans.modules.csl.api.Rule
intf org.netbeans.modules.csl.api.Rule$UserConfigurableRule
meth public abstract java.util.Set<?> getKinds()

CLSS public abstract interface static org.netbeans.modules.csl.api.Rule$ErrorRule
 outer org.netbeans.modules.csl.api.Rule
intf org.netbeans.modules.csl.api.Rule
meth public abstract java.util.Set<?> getCodes()

CLSS public abstract interface static org.netbeans.modules.csl.api.Rule$SelectionRule
 outer org.netbeans.modules.csl.api.Rule
intf org.netbeans.modules.csl.api.Rule

CLSS public abstract interface static org.netbeans.modules.csl.api.Rule$UserConfigurableRule
 outer org.netbeans.modules.csl.api.Rule
intf org.netbeans.modules.csl.api.Rule
meth public abstract boolean getDefaultEnabled()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getId()
meth public abstract javax.swing.JComponent getCustomizer(java.util.prefs.Preferences)

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

CLSS public final org.netbeans.modules.csl.api.SelectCodeElementAction
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,boolean)
fld public final static java.lang.String selectNextElementAction = "select-element-next"
fld public final static java.lang.String selectPreviousElementAction = "select-element-previous"
meth public java.lang.String getShortDescription()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds selectNext
hcls SelectionHandler,SelectionInfo

CLSS public org.netbeans.modules.csl.api.SelectNextCamelCasePosition
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String,javax.swing.Action)
cons public init(javax.swing.Action)
fld public final static java.lang.String selectNextCamelCasePosition = "select-next-camel-case-position"
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.NextCamelCasePosition

CLSS public org.netbeans.modules.csl.api.SelectPreviousCamelCasePosition
 anno 0 java.lang.Deprecated()
cons protected init(java.lang.String,javax.swing.Action)
cons public init(javax.swing.Action)
fld public final static java.lang.String selectPreviousCamelCasePosition = "select-previous-camel-case-position"
meth protected void moveToNewOffset(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
supr org.netbeans.modules.csl.api.PreviousCamelCasePosition

CLSS public abstract org.netbeans.modules.csl.api.SemanticAnalyzer<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract java.util.Map<org.netbeans.modules.csl.api.OffsetRange,java.util.Set<org.netbeans.modules.csl.api.ColoringAttributes>> getHighlights()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.parsing.spi.ParserResultTask<{org.netbeans.modules.csl.api.SemanticAnalyzer%0}>

CLSS public final !enum org.netbeans.modules.csl.api.Severity
fld public final static org.netbeans.modules.csl.api.Severity ERROR
fld public final static org.netbeans.modules.csl.api.Severity FATAL
fld public final static org.netbeans.modules.csl.api.Severity INFO
fld public final static org.netbeans.modules.csl.api.Severity WARNING
meth public static org.netbeans.modules.csl.api.Severity valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.Severity[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.Severity>

CLSS public abstract interface org.netbeans.modules.csl.api.StructureItem
innr public abstract interface static CollapsedDefault
innr public abstract interface static InheritedItem
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isLeaf()
meth public abstract int hashCode()
meth public abstract java.lang.String getHtml(org.netbeans.modules.csl.api.HtmlFormatter)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getSortText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.StructureItem> getNestedItems()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.ImageIcon getCustomIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract long getEndPosition()
meth public abstract long getPosition()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getElementHandle()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isInherited(org.netbeans.modules.csl.api.StructureItem)

CLSS public abstract interface static org.netbeans.modules.csl.api.StructureItem$CollapsedDefault
 outer org.netbeans.modules.csl.api.StructureItem
intf org.netbeans.modules.csl.api.StructureItem
meth public abstract boolean isCollapsedByDefault()

CLSS public abstract interface static org.netbeans.modules.csl.api.StructureItem$InheritedItem
 outer org.netbeans.modules.csl.api.StructureItem
intf org.netbeans.modules.csl.api.StructureItem
meth public abstract boolean isInherited()
meth public abstract org.netbeans.modules.csl.api.ElementHandle getDeclaringElement()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.csl.api.StructureScanner
innr public final static Configuration
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.StructureItem> scan(org.netbeans.modules.csl.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.csl.api.OffsetRange>> folds(org.netbeans.modules.csl.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.StructureScanner$Configuration getConfiguration()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final static org.netbeans.modules.csl.api.StructureScanner$Configuration
 outer org.netbeans.modules.csl.api.StructureScanner
cons public init(boolean,boolean)
cons public init(boolean,boolean,int)
meth public boolean isFilterable()
meth public boolean isSortable()
meth public int getExpandDepth()
meth public java.awt.Component getCustomFilter()
meth public void setCustomFilter(java.awt.Component)
meth public void setExpandDepth(int)
supr java.lang.Object
hfds customFilter,expandDepth,filterable,sortable

CLSS public org.netbeans.modules.csl.api.ToggleBlockCommentAction
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(org.netbeans.modules.csl.spi.CommentHandler)
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.editor.BaseAction
hfds LOG,commentHandler,serialVersionUID

CLSS public final org.netbeans.modules.csl.api.UiUtils
meth public static boolean open(org.netbeans.modules.parsing.api.Source,org.netbeans.modules.csl.api.ElementHandle)
meth public static boolean open(org.openide.filesystems.FileObject,int)
meth public static javax.swing.ImageIcon getElementIcon(org.netbeans.modules.csl.api.ElementKind,java.util.Collection<org.netbeans.modules.csl.api.Modifier>)
meth public static org.netbeans.modules.csl.api.KeystrokeHandler getBracketCompletion(javax.swing.text.Document,int)
supr java.lang.Object
hfds AWT_TIMEOUT,LOG,NON_AWT_TIMEOUT

CLSS public abstract interface org.netbeans.modules.csl.spi.CommentHandler
innr public abstract static DefaultCommentHandler
meth public abstract int[] getAdjustedBlocks(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract int[] getCommentBlocks(javax.swing.text.Document,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getCommentEndDelimiter()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getCommentStartDelimiter()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract static org.netbeans.modules.csl.spi.CommentHandler$DefaultCommentHandler
 outer org.netbeans.modules.csl.spi.CommentHandler
cons public init()
intf org.netbeans.modules.csl.spi.CommentHandler
meth public int[] getAdjustedBlocks(javax.swing.text.Document,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public int[] getCommentBlocks(javax.swing.text.Document,int,int)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.csl.spi.DefaultCompletionProposal
cons public init()
fld protected boolean smart
fld protected int anchorOffset
fld protected org.netbeans.modules.csl.api.ElementKind elementKind
intf org.netbeans.modules.csl.api.CompletionProposal
meth public boolean beforeDefaultAction()
meth public boolean isSmart()
meth public int getAnchorOffset()
meth public int getSortPrioOverride()
meth public java.lang.String getCustomInsertTemplate()
meth public java.lang.String getInsertPrefix()
meth public java.lang.String getLhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getName()
meth public java.lang.String getRhsHtml(org.netbeans.modules.csl.api.HtmlFormatter)
meth public java.lang.String getSortText()
meth public java.lang.String[] getParamListDelimiters()
meth public java.util.List<java.lang.String> getInsertParams()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public javax.swing.ImageIcon getIcon()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public void setAnchorOffset(int)
meth public void setKind(org.netbeans.modules.csl.api.ElementKind)
meth public void setSmart(boolean)
supr java.lang.Object

CLSS public org.netbeans.modules.csl.spi.DefaultCompletionResult
cons public init(java.util.List<org.netbeans.modules.csl.api.CompletionProposal>,boolean)
fld protected boolean filterable
fld protected boolean truncated
fld protected java.util.List<org.netbeans.modules.csl.api.CompletionProposal> list
meth public boolean isFilterable()
meth public boolean isTruncated()
meth public java.util.List<org.netbeans.modules.csl.api.CompletionProposal> getItems()
meth public void setFilterable(boolean)
meth public void setTruncated(boolean)
supr org.netbeans.modules.csl.api.CodeCompletionResult

CLSS public org.netbeans.modules.csl.spi.DefaultDataLoadersBridge
cons public init()
meth public java.beans.PropertyChangeListener getDataObjectListener(org.openide.filesystems.FileObject,org.openide.filesystems.FileChangeListener) throws java.io.IOException
meth public java.lang.Object createInstance(org.openide.filesystems.FileObject)
meth public java.lang.Object getCookie(org.openide.filesystems.FileObject,java.lang.Class) throws java.io.IOException
meth public java.lang.Object getSafeCookie(org.openide.filesystems.FileObject,java.lang.Class)
meth public java.lang.String getLine(javax.swing.text.Document,int)
meth public javax.swing.JEditorPane[] getOpenedPanes(org.openide.filesystems.FileObject)
meth public javax.swing.text.StyledDocument getDocument(org.openide.filesystems.FileObject)
meth public org.openide.cookies.EditorCookie isModified(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
meth public org.openide.filesystems.FileObject getPrimaryFile(org.openide.filesystems.FileObject)
meth public org.openide.nodes.Node getNodeDelegate(javax.swing.text.JTextComponent)
supr org.netbeans.modules.csl.api.DataLoadersBridge
hfds LOG
hcls DataObjectListener

CLSS public org.netbeans.modules.csl.spi.DefaultError
cons public init(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,int,int,boolean,org.netbeans.modules.csl.api.Severity)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,int,int,org.netbeans.modules.csl.api.Severity)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
intf org.netbeans.modules.csl.api.Error
meth public boolean isLineError()
meth public int getEndPosition()
meth public int getStartPosition()
meth public java.lang.Object[] getParameters()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getKey()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.api.Severity getSeverity()
meth public org.openide.filesystems.FileObject getFile()
meth public static org.netbeans.modules.csl.api.Error createDefaultError(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,int,int,boolean,org.netbeans.modules.csl.api.Severity)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 8 org.netbeans.api.annotations.common.NonNull()
meth public void setOffsets(int,int)
meth public void setParameters(java.lang.Object[])
supr java.lang.Object
hfds description,displayName,end,file,key,lineError,parameters,severity,start

CLSS public abstract org.netbeans.modules.csl.spi.DefaultLanguageConfig
cons public init()
intf org.netbeans.modules.csl.api.GsfLanguage
meth public abstract java.lang.String getDisplayName()
meth public abstract org.netbeans.api.lexer.Language getLexerLanguage()
meth public boolean hasFormatter()
meth public boolean hasHintsProvider()
meth public boolean hasOccurrencesFinder()
meth public boolean hasStructureScanner()
 anno 0 java.lang.Deprecated()
meth public boolean isIdentifierChar(char)
meth public boolean isUsingCustomEditorKit()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getLineCommentPrefix()
meth public java.lang.String getPreferredExtension()
meth public java.util.Set<java.lang.String> getBinaryLibraryPathIds()
meth public java.util.Set<java.lang.String> getLibraryPathIds()
meth public java.util.Set<java.lang.String> getSourcePathIds()
meth public org.netbeans.modules.csl.api.CodeCompletionHandler getCompletionHandler()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.DeclarationFinder getDeclarationFinder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.Formatter getFormatter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.HintsProvider getHintsProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.IndexSearcher getIndexSearcher()
meth public org.netbeans.modules.csl.api.InstantRenamer getInstantRenamer()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.KeystrokeHandler getKeystrokeHandler()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.OccurrencesFinder getOccurrencesFinder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.OverridingMethods getOverridingMethods()
meth public org.netbeans.modules.csl.api.SemanticAnalyzer getSemanticAnalyzer()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.api.StructureScanner getStructureScanner()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.csl.spi.CommentHandler getCommentHandler()
meth public org.netbeans.modules.parsing.spi.Parser getParser()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory getIndexerFactory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.csl.spi.ErrorFilter
fld public final static java.lang.String FEATURE_TASKLIST = "tasklist"
innr public abstract interface static Factory
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> filter(org.netbeans.modules.csl.spi.ParserResult)

CLSS public abstract interface static org.netbeans.modules.csl.spi.ErrorFilter$Factory
 outer org.netbeans.modules.csl.spi.ErrorFilter
meth public abstract org.netbeans.modules.csl.spi.ErrorFilter createErrorFilter(java.lang.String)

CLSS public final org.netbeans.modules.csl.spi.GsfUtilities
meth public static boolean endsWith(java.lang.StringBuilder,java.lang.String)
meth public static boolean isCodeTemplateEditing(javax.swing.text.Document)
meth public static boolean isRowEmpty(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static boolean isRowWhite(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static boolean open(org.openide.filesystems.FileObject,int,java.lang.String)
meth public static int getLastKnownCaretOffset(org.netbeans.modules.parsing.api.Snapshot,java.util.EventObject)
meth public static int getLineIndent(javax.swing.text.Document,int)
meth public static int getLineIndent(org.netbeans.editor.BaseDocument,int)
 anno 0 java.lang.Deprecated()
meth public static int getRowEnd(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int getRowFirstNonWhite(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int getRowLastNonWhite(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int getRowStart(java.lang.CharSequence,int) throws javax.swing.text.BadLocationException
meth public static int setLineIndentation(javax.swing.text.Document,int,int) throws javax.swing.text.BadLocationException
meth public static int setLineIndentation(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String truncate(java.lang.String,int)
meth public static javax.swing.text.Document getADocument(org.openide.filesystems.FileObject,boolean)
meth public static javax.swing.text.Document getADocument(org.openide.filesystems.FileObject,boolean,boolean)
meth public static javax.swing.text.JTextComponent getOpenPane()
meth public static javax.swing.text.JTextComponent getPaneFor(org.openide.filesystems.FileObject)
meth public static org.netbeans.editor.BaseDocument getBaseDocument(org.openide.filesystems.FileObject,boolean)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.editor.BaseDocument getDocument(org.openide.filesystems.FileObject,boolean)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.editor.BaseDocument getDocument(org.openide.filesystems.FileObject,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.FileObject findFileObject(javax.swing.text.Document)
meth public static org.openide.filesystems.FileObject findFileObject(javax.swing.text.JTextComponent)
meth public static org.openide.text.CloneableEditorSupport findCloneableEditorSupport(org.openide.filesystems.FileObject)
meth public static void extractZip(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
supr java.lang.Object
hfds BIG_FILE_THRESHOLD_MB,LOG,enforcedCaretOffsets

CLSS public abstract interface !annotation org.netbeans.modules.csl.spi.LanguageRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean useCustomEditorKit()
meth public abstract !hasdefault boolean useMultiview()
meth public abstract java.lang.String[] mimeType()

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public final org.netbeans.modules.csl.spi.support.CancelSupport
meth public boolean isCancelled()
meth public static org.netbeans.modules.csl.spi.support.CancelSupport getDefault()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INSTANCE,selfSpi
hcls SpiSupportAccessorImpl

CLSS public final org.netbeans.modules.csl.spi.support.ModificationResult
cons public init()
innr public static CreateFileDifference
innr public static Difference
intf org.netbeans.modules.refactoring.spi.ModificationResult
meth public java.lang.String getResultingSource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public java.util.List<? extends org.netbeans.modules.csl.spi.support.ModificationResult$Difference> getDifferences(org.openide.filesystems.FileObject)
meth public java.util.Set<? extends org.openide.filesystems.FileObject> getModifiedFileObjects()
meth public java.util.Set<java.io.File> getNewFiles()
meth public void addDifferences(org.openide.filesystems.FileObject,java.util.List<org.netbeans.modules.csl.spi.support.ModificationResult$Difference>)
meth public void commit() throws java.io.IOException
supr java.lang.Object
hfds COMPARATOR,committed,diffs

CLSS public static org.netbeans.modules.csl.spi.support.ModificationResult$CreateFileDifference
 outer org.netbeans.modules.csl.spi.support.ModificationResult
cons public init(java.io.File,java.lang.String)
meth public final java.io.File getFile()
meth public java.lang.String toString()
supr org.netbeans.modules.csl.spi.support.ModificationResult$Difference
hfds file

CLSS public static org.netbeans.modules.csl.spi.support.ModificationResult$Difference
 outer org.netbeans.modules.csl.spi.support.ModificationResult
cons public init(org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind,org.openide.text.PositionRef,org.openide.text.PositionRef,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind,org.openide.text.PositionRef,org.openide.text.PositionRef,java.lang.String,java.lang.String,java.lang.String)
innr public final static !enum Kind
meth public boolean isCommitToGuards()
meth public boolean isExcluded()
meth public java.lang.String getDescription()
meth public java.lang.String getNewText()
meth public java.lang.String getOldText()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind getKind()
meth public org.openide.text.PositionRef getEndPosition()
meth public org.openide.text.PositionRef getStartPosition()
meth public void exclude(boolean)
meth public void setCommitToGuards(boolean)
supr java.lang.Object
hfds description,endPos,excluded,ignoreGuards,kind,newText,oldText,startPos

CLSS public final static !enum org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind
 outer org.netbeans.modules.csl.spi.support.ModificationResult$Difference
fld public final static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind CHANGE
fld public final static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind CREATE
fld public final static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind INSERT
fld public final static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind REMOVE
meth public static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.csl.spi.support.ModificationResult$Difference$Kind>

CLSS public abstract org.netbeans.modules.parsing.api.Task
cons public init()
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

CLSS public abstract org.netbeans.modules.parsing.spi.ParserResultTask<%0 extends org.netbeans.modules.parsing.spi.Parser$Result>
cons public init()
meth public abstract int getPriority()
meth public abstract void run({org.netbeans.modules.parsing.spi.ParserResultTask%0},org.netbeans.modules.parsing.spi.SchedulerEvent)
supr org.netbeans.modules.parsing.spi.SchedulerTask

CLSS public abstract org.netbeans.modules.parsing.spi.SchedulerTask
meth public abstract int getPriority()
meth public abstract java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> getSchedulerClass()
meth public abstract void cancel()
supr org.netbeans.modules.parsing.api.Task

CLSS public abstract interface org.netbeans.modules.refactoring.spi.ModificationResult
meth public abstract java.lang.String getResultingSource(org.openide.filesystems.FileObject) throws java.io.IOException
meth public abstract java.util.Collection<? extends java.io.File> getNewFiles()
meth public abstract java.util.Collection<? extends org.openide.filesystems.FileObject> getModifiedFileObjects()
meth public abstract void commit() throws java.io.IOException

