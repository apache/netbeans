#Signature file v4.1
#Version 1.23.0

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.beans.BeanInfo
fld public final static int ICON_COLOR_16x16 = 1
fld public final static int ICON_COLOR_32x32 = 2
fld public final static int ICON_MONO_16x16 = 3
fld public final static int ICON_MONO_32x32 = 4
meth public abstract int getDefaultEventIndex()
meth public abstract int getDefaultPropertyIndex()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.beans.BeanDescriptor getBeanDescriptor()
meth public abstract java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public abstract java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public abstract java.beans.MethodDescriptor[] getMethodDescriptors()
meth public abstract java.beans.PropertyDescriptor[] getPropertyDescriptors()

CLSS public java.beans.SimpleBeanInfo
cons public init()
intf java.beans.BeanInfo
meth public int getDefaultEventIndex()
meth public int getDefaultPropertyIndex()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image loadImage(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract javax.swing.text.AbstractDocument
cons protected init(javax.swing.text.AbstractDocument$Content)
cons protected init(javax.swing.text.AbstractDocument$Content,javax.swing.text.AbstractDocument$AttributeContext)
fld protected final static java.lang.String BAD_LOCATION = "document location failure"
fld protected javax.swing.event.EventListenerList listenerList
fld public final static java.lang.String BidiElementName = "bidi level"
fld public final static java.lang.String ContentElementName = "content"
fld public final static java.lang.String ElementNameAttribute = "$ename"
fld public final static java.lang.String ParagraphElementName = "paragraph"
fld public final static java.lang.String SectionElementName = "section"
innr public BranchElement
innr public DefaultDocumentEvent
innr public LeafElement
innr public abstract AbstractElement
innr public abstract interface static AttributeContext
innr public abstract interface static Content
innr public static ElementEdit
intf java.io.Serializable
intf javax.swing.text.Document
meth protected final java.lang.Thread getCurrentWriter()
meth protected final javax.swing.text.AbstractDocument$AttributeContext getAttributeContext()
meth protected final javax.swing.text.AbstractDocument$Content getContent()
meth protected final void writeLock()
meth protected final void writeUnlock()
meth protected javax.swing.text.Element createBranchElement(javax.swing.text.Element,javax.swing.text.AttributeSet)
meth protected javax.swing.text.Element createLeafElement(javax.swing.text.Element,javax.swing.text.AttributeSet,int,int)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public <%0 extends java.util.EventListener> {%%0}[] getListeners(java.lang.Class<{%%0}>)
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public final java.lang.Object getProperty(java.lang.Object)
meth public final javax.swing.text.Position getEndPosition()
meth public final javax.swing.text.Position getStartPosition()
meth public final void putProperty(java.lang.Object,java.lang.Object)
meth public final void readLock()
meth public final void readUnlock()
meth public int getAsynchronousLoadPriority()
meth public int getLength()
meth public java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public java.util.Dictionary<java.lang.Object,java.lang.Object> getDocumentProperties()
meth public javax.swing.event.DocumentListener[] getDocumentListeners()
meth public javax.swing.event.UndoableEditListener[] getUndoableEditListeners()
meth public javax.swing.text.DocumentFilter getDocumentFilter()
meth public javax.swing.text.Element getBidiRootElement()
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void dump(java.io.PrintStream)
meth public void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void render(java.lang.Runnable)
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void setAsynchronousLoadPriority(int)
meth public void setDocumentFilter(javax.swing.text.DocumentFilter)
meth public void setDocumentProperties(java.util.Dictionary<java.lang.Object,java.lang.Object>)
supr java.lang.Object

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

CLSS public abstract interface javax.swing.text.Document
fld public final static java.lang.String StreamDescriptionProperty = "stream"
fld public final static java.lang.String TitleProperty = "title"
meth public abstract int getLength()
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract java.lang.String getText(int,int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Element getDefaultRootElement()
meth public abstract javax.swing.text.Element[] getRootElements()
meth public abstract javax.swing.text.Position createPosition(int) throws javax.swing.text.BadLocationException
meth public abstract javax.swing.text.Position getEndPosition()
meth public abstract javax.swing.text.Position getStartPosition()
meth public abstract void addDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void getText(int,int,javax.swing.text.Segment) throws javax.swing.text.BadLocationException
meth public abstract void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public abstract void putProperty(java.lang.Object,java.lang.Object)
meth public abstract void remove(int,int) throws javax.swing.text.BadLocationException
meth public abstract void removeDocumentListener(javax.swing.event.DocumentListener)
meth public abstract void removeUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public abstract void render(java.lang.Runnable)

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

CLSS public abstract interface javax.swing.text.StyledDocument
intf javax.swing.text.Document
meth public abstract java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public abstract java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public abstract javax.swing.text.Element getCharacterElement(int)
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public abstract javax.swing.text.Style getLogicalStyle(int)
meth public abstract javax.swing.text.Style getStyle(java.lang.String)
meth public abstract void removeStyle(java.lang.String)
meth public abstract void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public abstract void setLogicalStyle(int,javax.swing.text.Style)
meth public abstract void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)

CLSS public abstract javax.swing.text.TextAction
cons public init(java.lang.String)
meth protected final javax.swing.text.JTextComponent getFocusedComponent()
meth protected final javax.swing.text.JTextComponent getTextComponent(java.awt.event.ActionEvent)
meth public final static javax.swing.Action[] augmentList(javax.swing.Action[],javax.swing.Action[])
supr javax.swing.AbstractAction

CLSS public abstract interface org.netbeans.api.editor.document.CustomUndoDocument
meth public abstract void addUndoableEdit(javax.swing.undo.UndoableEdit)

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface org.netbeans.editor.AtomicLockDocument
 anno 0 java.lang.Deprecated()
intf javax.swing.text.Document
meth public abstract void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
meth public abstract void atomicLock()
meth public abstract void atomicUndo()
meth public abstract void atomicUnlock()
meth public abstract void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)

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

CLSS public org.netbeans.editor.BaseDocument
cons public init(boolean,java.lang.String)
cons public init(java.lang.Class,boolean)
 anno 0 java.lang.Deprecated()
fld protected boolean inited
fld protected boolean modified
fld protected javax.swing.text.Element defaultRootElem
fld public final static java.lang.String BLOCKS_FINDER_PROP = "blocks-finder"
fld public final static java.lang.String FILE_NAME_PROP = "file-name"
fld public final static java.lang.String FORMATTER = "formatter"
fld public final static java.lang.String ID_PROP = "id"
fld public final static java.lang.String KIT_CLASS_PROP = "kit-class"
fld public final static java.lang.String LINE_BATCH_SIZE = "line-batch-size"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LINE_LIMIT_PROP = "line-limit"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LS_CR = "\r"
fld public final static java.lang.String LS_CRLF = "\r\n"
fld public final static java.lang.String LS_LF = "\n"
fld public final static java.lang.String MIME_TYPE_PROP = "mimeType"
fld public final static java.lang.String READ_LINE_SEPARATOR_PROP = "__EndOfLine__"
fld public final static java.lang.String STRING_BWD_FINDER_PROP = "string-bwd-finder"
fld public final static java.lang.String STRING_FINDER_PROP = "string-finder"
fld public final static java.lang.String UNDO_MANAGER_PROP = "undo-manager"
fld public final static java.lang.String WRAP_SEARCH_MARK_PROP = "wrap-search-mark"
fld public final static java.lang.String WRITE_LINE_SEPARATOR_PROP = "write-line-separator"
innr protected static LazyPropertyMap
innr public abstract interface static PropertyEvaluator
intf org.netbeans.api.editor.document.CustomUndoDocument
intf org.netbeans.api.editor.document.LineDocument
intf org.netbeans.editor.AtomicLockDocument
meth protected final int getAtomicDepth()
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void fireChangedUpdate(javax.swing.event.DocumentEvent)
meth protected void fireInsertUpdate(javax.swing.event.DocumentEvent)
meth protected void fireRemoveUpdate(javax.swing.event.DocumentEvent)
meth protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent)
meth protected void insertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void postRemoveUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preInsertUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent,javax.swing.text.AttributeSet)
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth protected void removeUpdate(javax.swing.text.AbstractDocument$DefaultDocumentEvent)
meth public boolean isIdentifierPart(char)
meth public boolean isModifiable()
meth public boolean isModified()
meth public boolean isWhitespace(char)
meth public char[] getChars(int,int) throws javax.swing.text.BadLocationException
meth public char[] getChars(int[]) throws javax.swing.text.BadLocationException
meth public final boolean isAtomicLock()
meth public final java.lang.Class getKitClass()
 anno 0 java.lang.Deprecated()
meth public final void atomicLock()
 anno 0 java.lang.Deprecated()
meth public final void atomicUnlock()
 anno 0 java.lang.Deprecated()
meth public final void breakAtomicLock()
meth public final void extWriteLock()
meth public final void extWriteUnlock()
meth public int find(org.netbeans.editor.Finder,int,int) throws javax.swing.text.BadLocationException
meth public int getShiftWidth()
 anno 0 java.lang.Deprecated()
meth public int getTabSize()
meth public int processText(org.netbeans.editor.TextBatchProcessor,int,int) throws javax.swing.text.BadLocationException
meth public java.lang.String getText(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.String toString()
meth public java.lang.String toStringDetail()
meth public javax.swing.text.Element getDefaultRootElement()
meth public javax.swing.text.Element getParagraphElement(int)
meth public javax.swing.text.Element[] getRootElements()
meth public javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.Annotations getAnnotations()
meth public org.netbeans.editor.CharSeq getText()
meth public org.netbeans.editor.SyntaxSupport getSyntaxSupport()
 anno 0 java.lang.Deprecated()
meth public void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void addAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void addDocumentListener(javax.swing.event.DocumentListener)
meth public void addPostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void addUndoableEdit(javax.swing.undo.UndoableEdit)
meth public void addUndoableEditListener(javax.swing.event.UndoableEditListener)
meth public void addUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void atomicUndo()
meth public void checkTrailingSpaces(int)
meth public void getChars(int,char[],int,int) throws javax.swing.text.BadLocationException
meth public void insertString(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void invalidateSyntaxMarks()
meth public void print(org.netbeans.editor.PrintContainer)
meth public void print(org.netbeans.editor.PrintContainer,boolean,boolean,int,int)
meth public void print(org.netbeans.editor.PrintContainer,boolean,java.lang.Boolean,int,int)
meth public void read(java.io.Reader,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void remove(int,int) throws javax.swing.text.BadLocationException
meth public void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
meth public void removeAtomicLockListener(org.netbeans.editor.AtomicLockListener)
 anno 0 java.lang.Deprecated()
meth public void removeDocumentListener(javax.swing.event.DocumentListener)
meth public void removePostModificationDocumentListener(javax.swing.event.DocumentListener)
meth public void removeUpdateDocumentListener(javax.swing.event.DocumentListener)
meth public void render(java.lang.Runnable)
meth public void repaintBlock(int,int)
 anno 0 java.lang.Deprecated()
meth public void replace(int,int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth public void resetUndoMerge()
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setPostModificationDocumentListener(javax.swing.event.DocumentListener)
 anno 0 java.lang.Deprecated()
meth public void write(java.io.Writer,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr javax.swing.text.AbstractDocument
hfds DEACTIVATE_LEXER_THRESHOLD,EDITABLE_PROP,LAST_MODIFICATION_TIMESTAMP_PROP,LOG,LOG_LISTENER,MODIFICATION_LISTENER_PROP,SUPPORTS_MODIFICATION_LISTENER_PROP,VERSION_PROP,annotations,annotationsLock,atomicDepth,atomicEdits,atomicLockEventInstance,atomicLockListenerList,composedText,debugNoText,debugRead,debugStack,deprecatedKitClass,filterBypass,fixLineSyntaxState,identifierAcceptor,lastModifyUndoEdit,lastPositionEditedByTyping,lineRootElement,mimeType,modifiable,postModificationDepth,postModificationDocumentListener,postModificationDocumentListenerList,prefs,prefsListener,removeUpdateLineUndo,runExclusiveDepth,shiftWidth,syntaxSupport,tabSize,text,undoEditWrappers,undoMergeReset,updateDocumentListenerList,weakPrefsListener,whitespaceAcceptor
hcls Accessor,AtomicCompoundEdit,BaseDocumentServices,FilterBypassImpl,MimeTypePropertyEvaluator,OldListenerAdapter,PlainEditorKit,ServicesImpl

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

CLSS public org.netbeans.editor.Formatter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
meth public boolean expandTabs()
meth public int getShiftWidth()
meth public int getSpacesPerTab()
meth public int getTabSize()
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public int reformat(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.lang.Class getKitClass()
meth public java.lang.String getIndentString(int)
meth public java.lang.String getIndentString(org.netbeans.editor.BaseDocument,int)
meth public static org.netbeans.editor.Formatter getFormatter(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.editor.Formatter getFormatter(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void setFormatter(java.lang.Class,org.netbeans.editor.Formatter)
 anno 0 java.lang.Deprecated()
meth public void changeBlockIndent(org.netbeans.editor.BaseDocument,int,int,int) throws javax.swing.text.BadLocationException
meth public void changeRowIndent(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public void indentLock()
meth public void indentUnlock()
meth public void insertTabString(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth public void reformatLock()
meth public void reformatUnlock()
meth public void setExpandTabs(boolean)
meth public void setShiftWidth(int)
meth public void setSpacesPerTab(int)
meth public void setTabSize(int)
meth public void shiftLine(org.netbeans.editor.BaseDocument,int,boolean) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds ISC_MAX_INDENT_SIZE,ISC_MAX_TAB_SIZE,customExpandTabs,customShiftWidth,customSpacesPerTab,customTabSize,expandTabs,indentStringCache,indentUtilsClassRef,inited,kitClass,kitClass2Formatter,mimePath2Formatter,noIndentUtils,prefs,prefsListener,shiftWidth,spacesPerTab,tabSize

CLSS public org.netbeans.editor.GuardedDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class,boolean,javax.swing.text.StyleContext)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean,javax.swing.text.StyleContext)
fld protected java.lang.String normalStyleName
fld protected javax.swing.text.StyleContext styles
fld public final static java.lang.String FMT_GUARDED_INSERT_LOCALE = "FMT_guarded_insert"
fld public final static java.lang.String FMT_GUARDED_REMOVE_LOCALE = "FMT_guarded_remove"
fld public final static java.lang.String GUARDED_ATTRIBUTE = "guarded"
fld public final static javax.swing.text.SimpleAttributeSet guardedSet
fld public final static javax.swing.text.SimpleAttributeSet unguardedSet
intf javax.swing.text.StyledDocument
meth protected org.netbeans.editor.BaseDocumentEvent createDocumentEvent(int,int,javax.swing.event.DocumentEvent$EventType)
meth protected void preInsertCheck(int,java.lang.String,javax.swing.text.AttributeSet) throws javax.swing.text.BadLocationException
meth protected void preRemoveCheck(int,int) throws javax.swing.text.BadLocationException
meth public boolean isPosGuarded(int)
meth public java.awt.Color getBackground(javax.swing.text.AttributeSet)
meth public java.awt.Color getForeground(javax.swing.text.AttributeSet)
meth public java.awt.Font getFont(javax.swing.text.AttributeSet)
meth public java.lang.String toStringDetail()
meth public java.util.Enumeration getStyleNames()
meth public javax.swing.text.Element getCharacterElement(int)
meth public javax.swing.text.Style addStyle(java.lang.String,javax.swing.text.Style)
meth public javax.swing.text.Style getLogicalStyle(int)
meth public javax.swing.text.Style getStyle(java.lang.String)
meth public org.netbeans.editor.MarkBlockChain getGuardedBlockChain()
meth public void removeStyle(java.lang.String)
meth public void runAtomic(java.lang.Runnable)
meth public void runAtomicAsUser(java.lang.Runnable)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
meth public void setLogicalStyle(int,javax.swing.text.Style)
meth public void setNormalStyleName(java.lang.String)
meth public void setParagraphAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.BaseDocument
hfds LOG,atomicAsUser,breakGuarded,debugAtomic,debugAtomicStack,guardedBlockChain

CLSS public org.netbeans.editor.Syntax
cons public init()
fld protected boolean lastBuffer
fld protected char[] buffer
fld protected int offset
fld protected int state
fld protected int stopOffset
fld protected int stopPosition
fld protected int tokenLength
fld protected int tokenOffset
fld protected org.netbeans.editor.TokenContextPath tokenContextPath
fld protected org.netbeans.editor.TokenID supposedTokenID
fld public final static int DIFFERENT_STATE = 1
fld public final static int EQUAL_STATE = 0
fld public final static int INIT = -1
innr public abstract interface static StateInfo
innr public static BaseStateInfo
meth protected org.netbeans.editor.TokenID parseToken()
meth public char[] getBuffer()
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public int getOffset()
meth public int getPreScan()
meth public int getTokenLength()
meth public int getTokenOffset()
meth public java.lang.String getStateName(int)
meth public java.lang.String toString()
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenID getSupposedTokenID()
meth public org.netbeans.editor.TokenID nextToken()
meth public void load(org.netbeans.editor.Syntax$StateInfo,char[],int,int,boolean,int)
meth public void loadInitState()
meth public void loadState(org.netbeans.editor.Syntax$StateInfo)
meth public void relocate(char[],int,int,boolean,int)
meth public void reset()
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr java.lang.Object

CLSS public static org.netbeans.editor.Syntax$BaseStateInfo
 outer org.netbeans.editor.Syntax
cons public init()
intf org.netbeans.editor.Syntax$StateInfo
meth public int getPreScan()
meth public int getState()
meth public java.lang.String toString()
meth public java.lang.String toString(org.netbeans.editor.Syntax)
meth public void setPreScan(int)
meth public void setState(int)
supr java.lang.Object
hfds preScan,state

CLSS public abstract interface static org.netbeans.editor.Syntax$StateInfo
 outer org.netbeans.editor.Syntax
meth public abstract int getPreScan()
meth public abstract int getState()
meth public abstract void setPreScan(int)
meth public abstract void setState(int)

CLSS public org.netbeans.editor.SyntaxSupport
cons public init(org.netbeans.editor.BaseDocument)
fld protected boolean tokenNumericIDsValid
meth protected boolean isAbbrevDisabled(int)
meth protected org.netbeans.editor.SyntaxSupport createSyntaxSupport(java.lang.Class)
meth public boolean isIdentifier(java.lang.String)
meth public final org.netbeans.editor.BaseDocument getDocument()
meth public int findInsideBlocks(org.netbeans.editor.Finder,int,int,int[]) throws javax.swing.text.BadLocationException
meth public int findOutsideBlocks(org.netbeans.editor.Finder,int,int,int[]) throws javax.swing.text.BadLocationException
meth public int[] getTokenBlocks(int,int,org.netbeans.editor.TokenID[]) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.SyntaxSupport get(java.lang.Class)
meth public org.netbeans.editor.TokenItem getTokenChain(int) throws javax.swing.text.BadLocationException
meth public void initSyntax(org.netbeans.editor.Syntax,int,int,boolean,boolean) throws javax.swing.text.BadLocationException
meth public void tokenizeText(org.netbeans.editor.TokenProcessor,int,int,boolean) throws javax.swing.text.BadLocationException
meth public void tokenizeText(org.netbeans.editor.TokenProcessor,java.lang.String)
supr java.lang.Object
hfds EMPTY_INT_ARRAY,MATCH_ARRAY_CACHE_SIZE,doc,lastMatchArrays,lastTokenIDArrays,supMap,tokenBlocks

CLSS public org.netbeans.editor.TokenContext
cons public init(java.lang.String)
cons public init(java.lang.String,org.netbeans.editor.TokenContext[])
meth protected void addDeclaredTokenIDs() throws java.lang.IllegalAccessException
meth protected void addTokenID(org.netbeans.editor.TokenID)
meth public java.lang.String getNamePrefix()
meth public org.netbeans.editor.TokenCategory[] getTokenCategories()
meth public org.netbeans.editor.TokenContextPath getContextPath()
meth public org.netbeans.editor.TokenContextPath getContextPath(org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenContextPath[] getAllContextPaths()
meth public org.netbeans.editor.TokenContext[] getChildren()
meth public org.netbeans.editor.TokenID[] getTokenIDs()
supr java.lang.Object
hfds EMPTY_CHILDREN,allContextPaths,children,contextPath,lastContextPathPair,namePrefix,pathCache,tokenCategories,tokenCategoryList,tokenIDList,tokenIDs

CLSS public abstract org.netbeans.editor.ext.AbstractFormatLayer
cons public init(java.lang.String)
intf org.netbeans.editor.ext.FormatLayer
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth public java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public org.netbeans.editor.ext.ExtFormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
meth public boolean isComment(org.netbeans.editor.TokenItem,int)
meth public boolean isComment(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isImportant(org.netbeans.editor.TokenItem,int)
meth public boolean isImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineStart(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isNewLine(org.netbeans.editor.ext.FormatTokenPosition)
meth public char getChar(org.netbeans.editor.ext.FormatTokenPosition)
meth public int findLineDistance(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition)
meth public int getIndex(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID[])
meth public org.netbeans.editor.TokenItem findAnyToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID[],org.netbeans.editor.TokenContextPath,boolean)
meth public org.netbeans.editor.TokenItem findImportantToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem findMatchingToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.ImageTokenID,boolean)
meth public org.netbeans.editor.TokenItem findMatchingToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,java.lang.String,boolean)
meth public org.netbeans.editor.TokenItem findToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String,boolean)
meth public org.netbeans.editor.TokenItem insertImageToken(org.netbeans.editor.TokenItem,org.netbeans.editor.ImageTokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.ext.FormatTokenPosition findImportant(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition,boolean,boolean)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEndNonImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineFirstImportant(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition removeLineEndWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
supr org.netbeans.editor.ext.FormatSupport

CLSS public org.netbeans.editor.ext.ExtFormatter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
innr public static Simple
intf org.netbeans.editor.ext.FormatLayer
meth protected boolean acceptSyntax(org.netbeans.editor.Syntax)
meth protected boolean hasTextBefore(javax.swing.text.JTextComponent,java.lang.String)
meth protected int getEOLOffset(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected void initFormatLayers()
meth public boolean isSimple()
meth public boolean replaceFormatLayer(java.lang.String,org.netbeans.editor.ext.FormatLayer)
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public int reformat(org.netbeans.editor.BaseDocument,int,int) throws javax.swing.text.BadLocationException
meth public int[] getReformatBlock(javax.swing.text.JTextComponent,java.lang.String)
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.io.Writer reformat(org.netbeans.editor.BaseDocument,int,int,boolean) throws java.io.IOException,javax.swing.text.BadLocationException
meth public java.lang.Object getSettingValue(java.lang.String)
meth public java.lang.String getName()
meth public java.util.Iterator formatLayerIterator()
meth public void addFormatLayer(org.netbeans.editor.ext.FormatLayer)
meth public void format(org.netbeans.editor.ext.FormatWriter)
meth public void removeFormatLayer(java.lang.String)
meth public void setSettingValue(java.lang.String,java.lang.Object)
supr org.netbeans.editor.Formatter
hfds NULL_VALUE,formatLayerList,indentHotCharsAcceptor,mimeType,prefs,prefsListener,reindentWithTextBefore,settingsMap

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

CLSS public org.netbeans.editor.ext.ExtSyntaxSupport
cons public init(org.netbeans.editor.BaseDocument)
fld public final static int COMPLETION_CANCEL = 1
fld public final static int COMPLETION_HIDE = 4
fld public final static int COMPLETION_POPUP = 0
fld public final static int COMPLETION_POST_REFRESH = 3
fld public final static int COMPLETION_REFRESH = 2
innr public BracketFinder
innr public abstract interface static DeclarationTokenProcessor
innr public abstract interface static VariableMapTokenProcessor
meth protected int getMethodStartPosition(int)
meth protected java.util.Map buildGlobalVariableMap(int)
meth protected java.util.Map buildLocalVariableMap(int)
meth protected org.netbeans.editor.TokenID[] getBracketSkipTokens()
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$BracketFinder getMatchingBracketFinder(char)
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$DeclarationTokenProcessor createDeclarationTokenProcessor(java.lang.String,int,int)
meth protected org.netbeans.editor.ext.ExtSyntaxSupport$VariableMapTokenProcessor createVariableMapTokenProcessor(int,int)
meth protected void documentModified(javax.swing.event.DocumentEvent)
meth public boolean isCommentOrWhitespace(int,int) throws javax.swing.text.BadLocationException
meth public boolean isRowValid(int) throws javax.swing.text.BadLocationException
meth public boolean isWhitespaceToken(org.netbeans.editor.TokenID,char[],int,int)
meth public int checkCompletion(javax.swing.text.JTextComponent,java.lang.String,boolean)
meth public int findDeclarationPosition(java.lang.String,int)
meth public int findGlobalDeclarationPosition(java.lang.String,int)
meth public int findLocalDeclarationPosition(java.lang.String,int)
meth public int getRowLastValidChar(int) throws javax.swing.text.BadLocationException
meth public int[] findMatchingBlock(int,boolean) throws javax.swing.text.BadLocationException
meth public int[] getCommentBlocks(int,int) throws javax.swing.text.BadLocationException
meth public int[] getFunctionBlock(int) throws javax.swing.text.BadLocationException
meth public int[] getFunctionBlock(int[]) throws javax.swing.text.BadLocationException
meth public java.lang.Object findType(java.lang.String,int)
meth public java.util.Map getGlobalVariableMap(int)
meth public java.util.Map getLocalVariableMap(int)
meth public org.netbeans.editor.TokenID getTokenID(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.TokenID[] getCommentTokens()
meth public org.netbeans.editor.TokenItem getTokenChain(int,int) throws javax.swing.text.BadLocationException
supr org.netbeans.editor.SyntaxSupport
hfds EMPTY_TOKEN_ID_ARRAY,docL,globalVarMaps,localVarMaps
hcls CommentOrWhitespaceTP,FirstTokenTP,TokenItemTP

CLSS public abstract interface org.netbeans.editor.ext.FormatLayer
meth public abstract java.lang.String getName()
meth public abstract void format(org.netbeans.editor.ext.FormatWriter)

CLSS public org.netbeans.editor.ext.FormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
meth public boolean canInsertToken(org.netbeans.editor.TokenItem)
meth public boolean canModifyWhitespace(org.netbeans.editor.TokenItem)
meth public boolean canRemoveToken(org.netbeans.editor.TokenItem)
meth public boolean canReplaceToken(org.netbeans.editor.TokenItem)
meth public boolean expandTabs()
meth public boolean getSettingBoolean(java.lang.String,boolean)
meth public boolean getSettingBoolean(java.lang.String,java.lang.Boolean)
meth public boolean isAfter(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem)
meth public boolean isAfter(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isChainStartPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineEmpty(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isLineWhite(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean isRestartFormat()
meth public boolean isWhitespace(org.netbeans.editor.TokenItem,int)
meth public boolean isWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public boolean tokenEquals(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public final boolean isIndentOnly()
meth public int getIndentShift()
meth public int getLineIndent(org.netbeans.editor.ext.FormatTokenPosition,boolean)
meth public int getSettingInteger(java.lang.String,int)
meth public int getSettingInteger(java.lang.String,java.lang.Integer)
meth public int getShiftWidth()
meth public int getSpacesPerTab()
meth public int getTabSize()
meth public int getVisualColumnOffset(org.netbeans.editor.ext.FormatTokenPosition)
meth public java.lang.Object getSettingValue(java.lang.String)
meth public java.lang.Object getSettingValue(java.lang.String,java.lang.Object)
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem)
meth public java.lang.String chainToString(org.netbeans.editor.TokenItem,int)
meth public java.lang.String getIndentString(int)
meth public org.netbeans.editor.TokenContextPath getValidWhitespaceTokenContextPath()
meth public org.netbeans.editor.TokenContextPath getWhitespaceTokenContextPath()
meth public org.netbeans.editor.TokenID getValidWhitespaceTokenID()
meth public org.netbeans.editor.TokenID getWhitespaceTokenID()
meth public org.netbeans.editor.TokenItem findFirstToken(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findNonEmptyToken(org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem getLastToken()
meth public org.netbeans.editor.TokenItem getPreviousToken(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem insertToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public org.netbeans.editor.TokenItem splitEnd(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.TokenItem splitStart(org.netbeans.editor.TokenItem,int,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath)
meth public org.netbeans.editor.ext.FormatTokenPosition changeLineIndent(org.netbeans.editor.ext.FormatTokenPosition,int)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEnd(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineEndWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineFirstNonWhitespace(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineStart(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findNextEOL(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition findNonWhitespace(org.netbeans.editor.ext.FormatTokenPosition,org.netbeans.editor.ext.FormatTokenPosition,boolean,boolean)
meth public org.netbeans.editor.ext.FormatTokenPosition findPreviousEOL(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getFormatStartPosition()
meth public org.netbeans.editor.ext.FormatTokenPosition getLastPosition()
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getNextPosition(org.netbeans.editor.ext.FormatTokenPosition,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.TokenItem,int)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.TokenItem,int,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition getPreviousPosition(org.netbeans.editor.ext.FormatTokenPosition,javax.swing.text.Position$Bias)
meth public org.netbeans.editor.ext.FormatTokenPosition getTextStartPosition()
meth public org.netbeans.editor.ext.FormatWriter getFormatWriter()
meth public void insertSpaces(org.netbeans.editor.TokenItem,int)
meth public void insertString(org.netbeans.editor.TokenItem,int,java.lang.String)
meth public void insertString(org.netbeans.editor.ext.FormatTokenPosition,java.lang.String)
meth public void remove(org.netbeans.editor.TokenItem,int,int)
meth public void remove(org.netbeans.editor.ext.FormatTokenPosition,int)
meth public void removeToken(org.netbeans.editor.TokenItem)
meth public void removeTokenChain(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenItem)
meth public void replaceToken(org.netbeans.editor.TokenItem,org.netbeans.editor.TokenID,org.netbeans.editor.TokenContextPath,java.lang.String)
meth public void setIndentShift(int)
meth public void setRestartFormat(boolean)
supr java.lang.Object
hfds formatWriter

CLSS public abstract org.netbeans.modules.editor.FormatterIndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String EXPAND_TABS_PROP = "expandTabs"
fld public final static java.lang.String SPACES_PER_TAB_PROP = "spacesPerTab"
meth protected abstract org.netbeans.editor.ext.ExtFormatter createFormatter()
meth protected boolean acceptMimeType(java.lang.String)
meth public boolean isExpandTabs()
meth public int getSpacesPerTab()
meth public int indentLine(javax.swing.text.Document,int)
meth public int indentNewLine(javax.swing.text.Document,int)
meth public java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String[] getAcceptedMimeTypes()
meth public org.netbeans.editor.ext.ExtFormatter getFormatter()
meth public void setAcceptedMimeTypes(java.lang.String[])
meth public void setExpandTabs(boolean)
meth public void setSpacesPerTab(int)
meth public void setValue(java.lang.String,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public void setValue(java.lang.String,java.lang.Object,java.lang.String)
supr org.openide.text.IndentEngine
hfds acceptedMimeTypes,formatter,serialPersistentFields,serialVersionUID

CLSS public abstract org.netbeans.modules.editor.FormatterIndentEngineBeanInfo
cons public init()
cons public init(java.lang.String)
meth protected abstract java.lang.Class getBeanClass()
meth protected java.beans.PropertyDescriptor createPropertyDescriptor(java.lang.String)
meth protected java.beans.PropertyDescriptor getPropertyDescriptor(java.lang.String)
meth protected java.lang.String getString(java.lang.String)
meth protected java.lang.String[] createPropertyNames()
meth protected java.lang.String[] getPropertyNames()
meth protected void setExpert(java.lang.String[])
meth protected void setHidden(java.lang.String[])
meth protected void setPropertyEditor(java.lang.String,java.lang.Class)
meth protected void updatePropertyDescriptors()
meth public java.awt.Image getIcon(int)
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo
hfds icon,icon32,iconPrefix,propertyDescriptors,propertyNames

CLSS public org.netbeans.modules.editor.NbEditorDocument
cons public init(java.lang.Class)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
fld public final static java.lang.String INDENT_ENGINE = "indentEngine"
intf org.openide.text.NbDocument$Annotatable
intf org.openide.text.NbDocument$CustomEditor
intf org.openide.text.NbDocument$CustomToolbar
intf org.openide.text.NbDocument$PositionBiasable
intf org.openide.text.NbDocument$Printable
intf org.openide.text.NbDocument$WriteLockable
meth protected java.util.Dictionary createDocumentProperties(java.util.Dictionary)
meth public int getShiftWidth()
meth public int getTabSize()
meth public java.awt.Component createEditor(javax.swing.JEditorPane)
meth public java.text.AttributedCharacterIterator[] createPrintIterators()
meth public javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)
meth public void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public void removeAnnotation(org.openide.text.Annotation)
meth public void setCharacterAttributes(int,int,javax.swing.text.AttributeSet,boolean)
supr org.netbeans.editor.GuardedDocument
hfds RP,annoMap
hcls AnnotationDescDelegate,NbPrintContainer

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

CLSS public abstract interface org.netbeans.modules.editor.indent.spi.IndentTask
innr public abstract interface static Factory
meth public abstract org.netbeans.modules.editor.indent.spi.ExtraLock indentLock()
meth public abstract void reindent() throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.modules.editor.indent.spi.IndentTask$Factory
 outer org.netbeans.modules.editor.indent.spi.IndentTask
meth public abstract org.netbeans.modules.editor.indent.spi.IndentTask createTask(org.netbeans.modules.editor.indent.spi.Context)

CLSS public abstract org.netbeans.modules.editor.structure.formatting.TagBasedFormatter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class)
innr protected InitialIndentData
innr protected static TagIndentationData
meth protected abstract boolean areTagNamesEqual(java.lang.String,java.lang.String)
meth protected abstract boolean isClosingTag(org.netbeans.editor.TokenItem)
meth protected abstract boolean isClosingTagRequired(org.netbeans.editor.BaseDocument,java.lang.String)
meth protected abstract boolean isOpeningTag(org.netbeans.editor.TokenItem)
meth protected abstract boolean isUnformattableTag(java.lang.String)
meth protected abstract boolean isUnformattableToken(org.netbeans.editor.TokenItem)
meth protected abstract int getOpeningSymbolOffset(org.netbeans.editor.TokenItem)
meth protected abstract int getTagEndOffset(org.netbeans.editor.TokenItem)
meth protected abstract java.lang.String extractTagName(org.netbeans.editor.TokenItem)
meth protected abstract org.netbeans.editor.TokenItem getTagTokenEndingAtPosition(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected abstract org.netbeans.editor.ext.ExtSyntaxSupport getSyntaxSupport(org.netbeans.editor.BaseDocument)
meth protected boolean isJustBeforeClosingTag(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected boolean isWSTag(org.netbeans.editor.TokenItem)
meth protected int getIndentForTagParameter(org.netbeans.editor.BaseDocument,org.netbeans.editor.TokenItem) throws javax.swing.text.BadLocationException
meth protected int getInitialIndentFromPreviousLine(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected java.io.Writer extFormatterReformat(org.netbeans.editor.BaseDocument,int,int,boolean) throws java.io.IOException,javax.swing.text.BadLocationException
meth protected org.netbeans.editor.TokenItem getMatchingOpeningTag(org.netbeans.editor.TokenItem)
meth protected org.netbeans.editor.TokenItem getNextClosingTag(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected static int getNumberOfLines(org.netbeans.editor.BaseDocument) throws javax.swing.text.BadLocationException
meth protected void enterPressed(javax.swing.text.JTextComponent,int) throws javax.swing.text.BadLocationException
meth public int[] getReformatBlock(javax.swing.text.JTextComponent,java.lang.String)
meth public java.io.Writer reformat(org.netbeans.editor.BaseDocument,int,int,boolean) throws javax.swing.text.BadLocationException
supr org.netbeans.editor.ext.ExtFormatter

CLSS public abstract org.netbeans.modules.xml.spi.dom.AbstractNode
cons public init()
intf org.w3c.dom.Node
meth public abstract short getNodeType()
meth public boolean getSpecified()
meth public boolean getStrictErrorChecking()
meth public boolean getXmlStandalone()
meth public boolean hasAttribute(java.lang.String)
meth public boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isElementContentWhitespace()
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isId()
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public int getLength()
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public java.lang.String getBaseURI()
meth public java.lang.String getData()
meth public java.lang.String getDocumentURI()
meth public java.lang.String getInputEncoding()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.lang.String getTagName()
meth public java.lang.String getTextContent()
meth public java.lang.String getValue()
meth public java.lang.String getWholeText()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlVersion()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public java.lang.String substringData(int,int)
meth public org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public org.w3c.dom.DOMConfiguration getDomConfig()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.Element getOwnerElement()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public org.w3c.dom.Text splitText(int)
meth public org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public void appendData(java.lang.String)
meth public void deleteData(int,int)
meth public void insertData(int,java.lang.String)
meth public void normalize()
meth public void normalizeDocument()
meth public void removeAttribute(java.lang.String)
meth public void removeAttributeNS(java.lang.String,java.lang.String)
meth public void replaceData(int,int,java.lang.String)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public void setData(java.lang.String)
meth public void setDocumentURI(java.lang.String)
meth public void setIdAttribute(java.lang.String,boolean)
meth public void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public void setIdAttributeNode(org.w3c.dom.Attr,boolean)
meth public void setNodeValue(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setStrictErrorChecking(boolean)
meth public void setTextContent(java.lang.String)
meth public void setValue(java.lang.String)
meth public void setXmlStandalone(boolean)
meth public void setXmlVersion(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.api.XMLDefaultTokenContext
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.modules.xml.text.api.XMLDefaultTokenContext context
intf org.netbeans.modules.xml.text.syntax.XMLTokenIDs
supr org.netbeans.editor.TokenContext

CLSS public org.netbeans.modules.xml.text.api.XMLFormatUtil
cons public init()
meth public static void reformat(org.netbeans.editor.BaseDocument,int,int)
supr java.lang.Object

CLSS public final org.netbeans.modules.xml.text.api.XMLTextUtils
fld public final static java.lang.String XML_MIME = "text/xml"
meth public static java.lang.String actualAttributeValue(java.lang.String)
meth public static java.lang.String replaceCharsWithEntityStrings(java.lang.String)
meth public static java.lang.String replaceEntityStringsWithChars(java.lang.String)
meth public static org.netbeans.api.lexer.Token<org.netbeans.api.xml.lexer.XMLTokenId> skipAttributeValue(org.netbeans.api.lexer.TokenSequence,char)
meth public static void reformat(org.netbeans.api.editor.document.LineDocument,int,int)
supr java.lang.Object
hfds knownEntityChars,knownEntityStrings

CLSS public abstract interface org.netbeans.modules.xml.text.api.dom.SyntaxElement
fld public final static int NODE_ERROR = -1
meth public abstract <%0 extends org.w3c.dom.Node> {%%0} getNode()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract int getElementLength()
meth public abstract int getElementOffset()
meth public abstract int getType()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getNext()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getParentElement()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.xml.text.api.dom.SyntaxElement getPrevious()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public org.netbeans.modules.xml.text.indent.DTDFormatter
cons public init(java.lang.Class)
innr public StripEndWhitespaceLayer
meth protected boolean acceptSyntax(org.netbeans.editor.Syntax)
meth protected void initFormatLayers()
meth public int indentNewLine(javax.swing.text.Document,int)
meth public org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.ExtFormatter

CLSS public org.netbeans.modules.xml.text.indent.DTDFormatter$StripEndWhitespaceLayer
 outer org.netbeans.modules.xml.text.indent.DTDFormatter
cons public init(org.netbeans.modules.xml.text.indent.DTDFormatter)
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth public void format(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.AbstractFormatLayer

CLSS public org.netbeans.modules.xml.text.indent.DTDIndentEngine
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth protected org.netbeans.editor.ext.ExtFormatter createFormatter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.netbeans.modules.editor.FormatterIndentEngine

CLSS public org.netbeans.modules.xml.text.indent.DTDIndentEngineBeanInfo
cons public init()
meth protected java.lang.Class getBeanClass()
meth protected java.lang.String getString(java.lang.String)
meth protected java.lang.String[] createPropertyNames()
meth public java.beans.BeanDescriptor getBeanDescriptor()
supr org.netbeans.modules.editor.FormatterIndentEngineBeanInfo
hfds beanDescriptor

CLSS public org.netbeans.modules.xml.text.indent.LineBreakHook
cons public init()
innr public static F
intf org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public void afterInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public void cancelled(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context)
meth public void insert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds LOG

CLSS public static org.netbeans.modules.xml.text.indent.LineBreakHook$F
 outer org.netbeans.modules.xml.text.indent.LineBreakHook
cons public init()
intf org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Factory
meth public org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor createTypedBreakInterceptor(org.netbeans.api.editor.mimelookup.MimePath)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.indent.XMLFormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.ExtFormatSupport

CLSS public org.netbeans.modules.xml.text.indent.XMLFormatter
cons public init(java.lang.Class)
innr public StripEndWhitespaceLayer
meth protected boolean acceptSyntax(org.netbeans.editor.Syntax)
meth protected boolean areTagNamesEqual(java.lang.String,java.lang.String)
meth protected boolean isClosingTag(org.netbeans.editor.TokenItem)
meth protected boolean isClosingTagRequired(org.netbeans.editor.BaseDocument,java.lang.String)
meth protected boolean isOpeningTag(org.netbeans.editor.TokenItem)
meth protected boolean isUnformattableTag(java.lang.String)
meth protected boolean isUnformattableToken(org.netbeans.editor.TokenItem)
meth protected int getOpeningSymbolOffset(org.netbeans.editor.TokenItem)
meth protected int getTagEndOffset(org.netbeans.editor.TokenItem)
meth protected java.lang.String extractTagName(org.netbeans.editor.TokenItem)
meth protected org.netbeans.editor.TokenItem getTagTokenEndingAtPosition(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected org.netbeans.editor.ext.ExtSyntaxSupport getSyntaxSupport(org.netbeans.editor.BaseDocument)
meth protected void initFormatLayers()
meth public org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.modules.editor.structure.formatting.TagBasedFormatter
hfds TAG_CLOSED_SUFFIX,TAG_CLOSING_PREFIX,TAG_CLOSING_SUFFIX,TAG_OPENING_PREFIX,VALID_TAG_NAME,WORKUNITS_MAX

CLSS public org.netbeans.modules.xml.text.indent.XMLFormatter$StripEndWhitespaceLayer
 outer org.netbeans.modules.xml.text.indent.XMLFormatter
cons public init(org.netbeans.modules.xml.text.indent.XMLFormatter)
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth public void format(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.AbstractFormatLayer

CLSS public org.netbeans.modules.xml.text.indent.XMLIndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth protected org.netbeans.editor.ext.ExtFormatter createFormatter()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.netbeans.modules.editor.FormatterIndentEngine
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.indent.XMLIndentEngineBeanInfo
cons public init()
meth protected java.lang.Class getBeanClass()
meth protected java.lang.String getString(java.lang.String)
meth protected java.lang.String[] createPropertyNames()
meth public java.beans.BeanDescriptor getBeanDescriptor()
supr org.netbeans.modules.editor.FormatterIndentEngineBeanInfo
hfds beanDescriptor

CLSS public org.netbeans.modules.xml.text.indent.XMLIndentTask
intf org.netbeans.modules.editor.indent.spi.IndentTask
meth public org.netbeans.modules.editor.indent.spi.ExtraLock indentLock()
meth public void reindent() throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds context

CLSS public org.netbeans.modules.xml.text.indent.XMLIndentTaskFactory
cons public init()
intf org.netbeans.modules.editor.indent.spi.IndentTask$Factory
meth public org.netbeans.modules.editor.indent.spi.IndentTask createTask(org.netbeans.modules.editor.indent.spi.Context)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.indent.XMLLexerFormatter
cons public init(org.netbeans.api.lexer.LanguagePath)
meth protected org.netbeans.api.lexer.LanguagePath supportedLanguagePath()
meth public org.netbeans.api.editor.document.LineDocument doReformat(org.netbeans.api.editor.document.LineDocument,int,int)
meth public void reformat(org.netbeans.modules.editor.indent.spi.Context,int,int) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds SPACE_DEFAULT,SPACE_DEFAULT_LEN,SPACE_PRESERVE,SPACE_PRESERVE_LEN,XML_SPACE_ATTRIBUTE,XML_SPACE_ATTRIBUTE_LEN,basedoc,contentPresent,counter,currentTokensSize,endOffset,firstAttributeIndent,indentLevel,languagePath,lineCount,lineSizes,logger,onlyTags,preserveWhitespace,settingSpaceValue,spacesPerTab,stack,startOffset,tagIndent,tags,token,tokenInSelectionRange,tokenSequence,wasNewline
hcls TokenIndent

CLSS public org.netbeans.modules.xml.text.syntax.DTDKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "application/xml-dtd"
meth public java.lang.String getContentType()
supr org.netbeans.modules.xml.text.syntax.UniKit
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.syntax.DTDSyntaxTokenMapper
cons public init()
intf org.netbeans.modules.xml.text.syntax.javacc.DTDSyntaxConstants
intf org.netbeans.modules.xml.text.syntax.javacc.lib.JJConstants
intf org.netbeans.modules.xml.text.syntax.javacc.lib.JJMapperInterface
meth public final org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID guessToken(java.lang.String,int,boolean)
meth public org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID createToken(int)
meth public org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID supposedToken(java.lang.String,int,int)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.syntax.DTDTokenContext
fld public final static int COMMENT_ID = 1
fld public final static int EOL_ID = 9
fld public final static int ERROR_ID = 2
fld public final static int KW_ID = 3
fld public final static int PLAIN_ID = 4
fld public final static int REF_ID = 5
fld public final static int STRING_ID = 6
fld public final static int SYMBOL_ID = 7
fld public final static int TARGET_ID = 8
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.modules.xml.text.syntax.DTDTokenContext context
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID COMMENT
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID EOL
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID ERROR
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID KW
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID PLAIN
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID REF
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID STRING
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID SYMBOL
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID TARGET
supr org.netbeans.editor.TokenContext

CLSS public org.netbeans.modules.xml.text.syntax.ENTKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "text/xml-external-parsed-entity"
meth public java.lang.String getContentType()
supr org.netbeans.modules.xml.text.syntax.UniKit
hfds serialVersionUID

CLSS public abstract org.netbeans.modules.xml.text.syntax.SyntaxElement
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
fld protected int length
fld protected int offset
fld protected org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport support
innr public static Error
intf org.netbeans.modules.xml.text.api.dom.SyntaxElement
meth protected org.netbeans.editor.TokenItem first()
meth public boolean equals(java.lang.Object)
meth public int getElementLength()
meth public int getElementOffset()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.text.syntax.SyntaxElement getNext()
meth public org.netbeans.modules.xml.text.syntax.SyntaxElement getPrevious()
supr java.lang.Object
hfds first,next,previous

CLSS public static org.netbeans.modules.xml.text.syntax.SyntaxElement$Error
 outer org.netbeans.modules.xml.text.syntax.SyntaxElement
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
meth public int getType()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.text.syntax.SyntaxElement getParentElement()
meth public org.w3c.dom.Node getNode()
supr org.netbeans.modules.xml.text.syntax.SyntaxElement

CLSS public org.netbeans.modules.xml.text.syntax.UniKit
cons public init()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
meth public void read(java.io.InputStream,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void read(java.io.Reader,javax.swing.text.Document,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.OutputStream,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
meth public void write(java.io.Writer,javax.swing.text.Document,int,int) throws java.io.IOException,javax.swing.text.BadLocationException
supr org.netbeans.modules.editor.NbEditorKit
hfds serialVersionUID

CLSS public org.netbeans.modules.xml.text.syntax.UnicodeClasses
meth public static boolean isXMLBaseChar(int)
meth public static boolean isXMLChar(int)
meth public static boolean isXMLCombiningChar(int)
meth public static boolean isXMLDigit(int)
meth public static boolean isXMLExtender(int)
meth public static boolean isXMLIdeographic(int)
meth public static boolean isXMLLetter(int)
meth public static boolean isXMLNCNameChar(int)
meth public static boolean isXMLNCNameStartChar(int)
meth public static boolean isXMLNameChar(int)
meth public static boolean isXMLNameStartChar(int)
meth public static boolean isXMLPubidLiteral(char)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.syntax.XMLDefaultSyntax
cons public init()
fld protected boolean subInternalDTD
fld protected int subState
innr public static XMLStateInfo
meth protected org.netbeans.editor.TokenID parseToken()
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public java.lang.String getStateName(int)
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public void loadState(org.netbeans.editor.Syntax$StateInfo)
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr org.netbeans.editor.Syntax
hfds ISA_CDATA_BR,ISA_CDATA_BRBR,ISA_INIT_BR,ISA_LT,ISA_LTEXBR,ISA_LTEXBRC,ISA_LTEXBRCD,ISA_LTEXBRCDA,ISA_LTEXBRCDAT,ISA_LTEXBRCDATA,ISA_PI_CONTENT_QMARK,ISA_REF,ISA_REF_HASH,ISA_REF_X,ISA_SGML_DASH,ISA_SGML_DECL_DASH,ISA_SGML_ESCAPE,ISA_SLASH,ISA_XML_COMMENT_DASH,ISI_ARG,ISI_CDATA,ISI_DECL_CHARS,ISI_DECL_STRING,ISI_ENDTAG,ISI_ERROR,ISI_PI,ISI_PI_CONTENT,ISI_PI_TARGET,ISI_REF_DEC,ISI_REF_HEX,ISI_REF_NAME,ISI_SGML_DECL,ISI_TAG,ISI_TEXT,ISI_VAL_APOS,ISI_VAL_QUOT,ISI_XML_COMMENT,ISI_XML_COMMENT_WS,ISP_ARG_WS,ISP_ARG_X,ISP_DECL_CHARS,ISP_DECL_STRING,ISP_ENDTAG_WS,ISP_ENDTAG_X,ISP_EQ,ISP_EQ_WS,ISP_PI_CONTENT_QMARK,ISP_PI_TARGET_WS,ISP_TAG_WS,ISP_TAG_X

CLSS public static org.netbeans.modules.xml.text.syntax.XMLDefaultSyntax$XMLStateInfo
 outer org.netbeans.modules.xml.text.syntax.XMLDefaultSyntax
cons public init()
meth public boolean isInternalDTD()
meth public int getSubState()
meth public java.lang.String toString(org.netbeans.editor.Syntax)
meth public void setInternalDTD(boolean)
meth public void setSubState(int)
supr org.netbeans.editor.Syntax$BaseStateInfo
hfds subInternalDTD,subState

CLSS public org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
fld public final static java.lang.String MIME_TYPE = "text/xml"
fld public final static java.lang.String xmlCommentAction = "xml-comment"
fld public final static java.lang.String xmlTestAction = "xml-dump"
fld public final static java.lang.String xmlUncommentAction = "xml-uncomment"
fld public static java.util.Map settings
innr public XMLEditorDocument
innr public abstract static XMLEditorAction
innr public static TestAction
innr public static XMLCommentAction
innr public static XMLUncommentAction
intf org.openide.util.HelpCtx$Provider
meth protected javax.swing.Action[] createActions()
meth public java.lang.String getContentType()
meth public java.util.Map getMap()
meth public javax.swing.text.Document createDefaultDocument()
meth public org.netbeans.editor.Syntax createSyntax(javax.swing.text.Document)
meth public org.netbeans.editor.SyntaxSupport createSyntaxSupport(org.netbeans.editor.BaseDocument)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static void setMap(java.util.Map)
meth public void install(javax.swing.JEditorPane)
supr org.netbeans.modules.editor.NbEditorKit
hfds J2EE_LEXER_COLORING,serialVersionUID

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$TestAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds serialVersionUID

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$XMLCommentAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds commentEndString,commentStartString,serialVersionUID

CLSS public abstract static org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init(java.lang.String)
meth protected void problem(java.lang.String)
supr org.netbeans.editor.BaseAction

CLSS public org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorDocument
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init(org.netbeans.modules.xml.text.syntax.XMLKit,java.lang.Class)
cons public init(org.netbeans.modules.xml.text.syntax.XMLKit,java.lang.String)
supr org.netbeans.modules.editor.NbEditorDocument

CLSS public static org.netbeans.modules.xml.text.syntax.XMLKit$XMLUncommentAction
 outer org.netbeans.modules.xml.text.syntax.XMLKit
cons public init()
meth public void actionPerformed(java.awt.event.ActionEvent,javax.swing.text.JTextComponent)
supr org.netbeans.modules.xml.text.syntax.XMLKit$XMLEditorAction
hfds commentEnd,commentEndString,commentStart,commentStartString,serialVersionUID

CLSS public final org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport
cons public init(org.netbeans.editor.BaseDocument)
intf org.netbeans.modules.xml.text.syntax.XMLTokenIDs
meth public boolean isSingletonTag(org.netbeans.editor.TokenItem)
meth public boolean noCompletion(javax.swing.text.JTextComponent)
meth public boolean requestedAutoCompletion()
meth public final char lastTypedChar()
meth public int checkCompletion(javax.swing.text.JTextComponent,java.lang.String,boolean)
meth public int[] findMatch(int,boolean) throws javax.swing.text.BadLocationException
meth public int[] findMatchingBlock(int,boolean) throws javax.swing.text.BadLocationException
meth public java.lang.String getEndTag(int) throws javax.swing.text.BadLocationException
meth public java.util.List<java.lang.String> getFollowingLevelTags(int) throws javax.swing.text.BadLocationException
meth public java.util.List<java.lang.String> getPreviousLevelTags(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.editor.TokenItem getPreviousToken(int) throws javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.text.syntax.SyntaxElement createElement(org.netbeans.editor.TokenItem) throws javax.swing.text.BadLocationException
meth public org.netbeans.modules.xml.text.syntax.SyntaxElement getElementChain(int) throws javax.swing.text.BadLocationException
supr org.netbeans.editor.ext.ExtSyntaxSupport
hfds CDATA_END,CDATA_START,documentMonitor,lastInsertedChar,publicId,requestedAutoCompletion,systemId
hcls DocumentMonitor

CLSS public org.netbeans.modules.xml.text.syntax.XMLSyntaxTokenMapper
cons public init()
intf org.netbeans.modules.xml.text.syntax.javacc.XMLSyntaxConstants
intf org.netbeans.modules.xml.text.syntax.javacc.lib.JJConstants
intf org.netbeans.modules.xml.text.syntax.javacc.lib.JJMapperInterface
meth public final org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID guessToken(java.lang.String,int,boolean)
meth public org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID createToken(int)
meth public org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID supposedToken(java.lang.String,int,int)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.text.syntax.XMLTokenContext
cons protected init()
fld public final static int ATT_ID = 1
fld public final static int CDATA_ID = 2
fld public final static int CDATA_MARKUP_ID = 13
fld public final static int COMMENT_ID = 3
fld public final static int EOL_ID = 4
fld public final static int ERROR_ID = 5
fld public final static int KW_ID = 6
fld public final static int PLAIN_ID = 7
fld public final static int REF_ID = 8
fld public final static int STRING_ID = 9
fld public final static int SYMBOL_ID = 10
fld public final static int TAG_ID = 11
fld public final static int TARGET_ID = 12
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.modules.xml.text.syntax.XMLTokenContext context
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID ATT
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID CDATA
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID CDATA_MARKUP
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID COMMENT
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID EOL
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID ERROR
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID KW
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID PLAIN
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID REF
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID STRING
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID SYMBOL
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID TAG
fld public final static org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID TARGET
supr org.netbeans.editor.TokenContext

CLSS public abstract interface org.netbeans.modules.xml.text.syntax.XMLTokenIDs
 anno 0 java.lang.Deprecated()
fld public final static int ARGUMENT_ID = 5
fld public final static int BLOCK_COMMENT_ID = 8
fld public final static int CDATA_SECTION_ID = 17
fld public final static int CHARACTER_ID = 11
fld public final static int DECLARATION_ID = 10
fld public final static int EOL_ID = 12
fld public final static int ERROR_ID = 3
fld public final static int OPERATOR_ID = 6
fld public final static int PI_CONTENT_ID = 15
fld public final static int PI_END_ID = 16
fld public final static int PI_START_ID = 13
fld public final static int PI_TARGET_ID = 14
fld public final static int TAG_ID = 4
fld public final static int TEXT_ID = 1
fld public final static int VALUE_ID = 7
fld public final static int WS_ID = 2
fld public final static org.netbeans.editor.BaseTokenID ARGUMENT
fld public final static org.netbeans.editor.BaseTokenID BLOCK_COMMENT
fld public final static org.netbeans.editor.BaseTokenID CDATA_SECTION
fld public final static org.netbeans.editor.BaseTokenID CHARACTER
fld public final static org.netbeans.editor.BaseTokenID DECLARATION
fld public final static org.netbeans.editor.BaseTokenID EOL
fld public final static org.netbeans.editor.BaseTokenID ERROR
fld public final static org.netbeans.editor.BaseTokenID OPERATOR
fld public final static org.netbeans.editor.BaseTokenID PI_CONTENT
fld public final static org.netbeans.editor.BaseTokenID PI_END
fld public final static org.netbeans.editor.BaseTokenID PI_START
fld public final static org.netbeans.editor.BaseTokenID PI_TARGET
fld public final static org.netbeans.editor.BaseTokenID TAG
fld public final static org.netbeans.editor.BaseTokenID TEXT
fld public final static org.netbeans.editor.BaseTokenID VALUE
fld public final static org.netbeans.editor.BaseTokenID WS

CLSS public org.netbeans.modules.xml.text.syntax.dom.AttrImpl
intf org.netbeans.modules.xml.text.syntax.XMLTokenIDs
intf org.w3c.dom.Attr
meth public boolean getSpecified()
meth public java.lang.String getName()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.netbeans.editor.TokenItem getFirstToken()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.Element getOwnerElement()
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.NodeList getChildNodes()
meth public short getNodeType()
meth public void setNodeValue(java.lang.String)
meth public void setValue(java.lang.String)
supr org.netbeans.modules.xml.spi.dom.AbstractNode
hfds first,parent,syntax

CLSS public org.netbeans.modules.xml.text.syntax.dom.CDATASectionImpl
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
supr org.netbeans.modules.xml.text.syntax.dom.TextImpl

CLSS public org.netbeans.modules.xml.text.syntax.dom.CommentImpl
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
intf org.w3c.dom.Comment
meth public int getLength()
meth public java.lang.String getData()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String substringData(int,int)
meth public java.lang.String toString()
meth public org.w3c.dom.Text splitText(int)
meth public short getNodeType()
meth public void appendData(java.lang.String)
meth public void deleteData(int,int)
meth public void insertData(int,java.lang.String)
meth public void replaceData(int,int,java.lang.String)
meth public void setData(java.lang.String)
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode

CLSS public org.netbeans.modules.xml.text.syntax.dom.DocumentImpl
intf org.w3c.dom.Document
meth public boolean getStandalone()
meth public boolean getStrictErrorChecking()
meth public java.lang.String getEncoding()
meth public java.lang.String getVersion()
meth public org.w3c.dom.Attr createAttribute(java.lang.String)
meth public org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public org.w3c.dom.Comment createComment(java.lang.String)
meth public org.w3c.dom.DOMImplementation getImplementation()
meth public org.w3c.dom.DocumentFragment createDocumentFragment()
meth public org.w3c.dom.DocumentType getDoctype()
meth public org.w3c.dom.Element createElement(java.lang.String)
meth public org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Element getDocumentElement()
meth public org.w3c.dom.Element getElementById(java.lang.String)
meth public org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public org.w3c.dom.Text createTextNode(java.lang.String)
meth public short getNodeType()
meth public void setEncoding(java.lang.String)
meth public void setStandalone(boolean)
meth public void setStrictErrorChecking(boolean)
meth public void setVersion(java.lang.String)
supr org.netbeans.modules.xml.spi.dom.AbstractNode
hfds syntax

CLSS public org.netbeans.modules.xml.text.syntax.dom.DocumentTypeImpl
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
intf org.netbeans.modules.xml.text.syntax.XMLTokenIDs
intf org.w3c.dom.DocumentType
meth public java.lang.String getInternalSubset()
meth public java.lang.String getName()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public org.w3c.dom.NamedNodeMap getEntities()
meth public org.w3c.dom.NamedNodeMap getNotations()
meth public short getNodeType()
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode

CLSS public org.netbeans.modules.xml.text.syntax.dom.EmptyTag
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int,java.lang.String,java.util.Collection)
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getEndTag()
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getStartTag()
meth public boolean hasChildNodes()
meth public java.lang.String toString()
meth public org.w3c.dom.NodeList getChildNodes()
supr org.netbeans.modules.xml.text.syntax.dom.Tag

CLSS public org.netbeans.modules.xml.text.syntax.dom.EndTag
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int,java.lang.String)
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getEndTag()
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getStartTag()
meth public boolean hasChildNodes()
meth public java.lang.String toString()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.NodeList getChildNodes()
supr org.netbeans.modules.xml.text.syntax.dom.Tag

CLSS public final org.netbeans.modules.xml.text.syntax.dom.EntityReferenceImpl
intf org.w3c.dom.EntityReference
meth public java.lang.String getNodeName()
meth public java.lang.String toString()
meth public short getNodeType()
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode

CLSS public final org.netbeans.modules.xml.text.syntax.dom.ProcessingInstructionImpl
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
meth public java.lang.String getData()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getTarget()
meth public short getNodeType()
meth public void setData(java.lang.String)
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode

CLSS public org.netbeans.modules.xml.text.syntax.dom.StartTag
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int,java.lang.String,java.util.Collection)
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getEndTag()
meth protected org.netbeans.modules.xml.text.syntax.dom.Tag getStartTag()
meth public boolean hasChildNodes()
meth public java.lang.String toString()
meth public org.w3c.dom.NodeList getChildNodes()
supr org.netbeans.modules.xml.text.syntax.dom.Tag

CLSS public abstract org.netbeans.modules.xml.text.syntax.dom.SyntaxNode
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
intf org.w3c.dom.Node
meth public abstract short getNodeType()
meth public boolean hasAttributes()
meth public boolean hasChildNodes()
meth public boolean isDefaultNamespace(java.lang.String)
meth public boolean isElementContentWhitespace()
meth public boolean isEqualNode(org.w3c.dom.Node)
meth public boolean isSameNode(org.w3c.dom.Node)
meth public boolean isSupported(java.lang.String,java.lang.String)
meth public int getType()
meth public java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public java.lang.Object getUserData(java.lang.String)
meth public java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public java.lang.String getBaseURI()
meth public java.lang.String getLocalName()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPrefix()
meth public java.lang.String getTextContent()
meth public java.lang.String getWholeText()
meth public java.lang.String lookupNamespaceURI(java.lang.String)
meth public java.lang.String lookupPrefix(java.lang.String)
meth public org.netbeans.modules.xml.text.api.dom.SyntaxElement getParentElement()
meth public org.w3c.dom.Document getOwnerDocument()
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node cloneNode(boolean)
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getNode()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public org.w3c.dom.NodeList getChildNodes()
meth public org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public short compareDocumentPosition(org.w3c.dom.Node)
meth public void normalize()
meth public void setIdAttribute(java.lang.String,boolean)
meth public void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public void setIdAttributeNode(org.w3c.dom.Attr,boolean)
meth public void setNodeValue(java.lang.String)
meth public void setPrefix(java.lang.String)
meth public void setTextContent(java.lang.String)
supr org.netbeans.modules.xml.text.syntax.SyntaxElement

CLSS public abstract org.netbeans.modules.xml.text.syntax.dom.Tag
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int,java.lang.String,java.util.Collection)
fld protected java.lang.String name
fld protected org.w3c.dom.NamedNodeMap domAttributes
intf org.netbeans.modules.xml.text.syntax.XMLTokenIDs
intf org.w3c.dom.Element
meth protected abstract org.netbeans.modules.xml.text.syntax.dom.Tag getEndTag()
meth protected abstract org.netbeans.modules.xml.text.syntax.dom.Tag getStartTag()
meth public boolean hasAttribute(java.lang.String)
meth public boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String getNodeName()
meth public final java.lang.String getTagName()
meth public final org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public final org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public final short getNodeType()
meth public final void removeAttribute(java.lang.String)
meth public final void setAttribute(java.lang.String,java.lang.String)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public org.w3c.dom.NamedNodeMap getAttributes()
meth public org.w3c.dom.Node getFirstChild()
meth public org.w3c.dom.Node getLastChild()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public void removeAttributeNS(java.lang.String,java.lang.String)
meth public void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode

CLSS public org.netbeans.modules.xml.text.syntax.dom.TextImpl
cons public init(org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport,org.netbeans.editor.TokenItem,int)
intf org.w3c.dom.Text
meth public int getLength()
meth public java.lang.String getData()
meth public java.lang.String getNodeValue()
meth public java.lang.String substringData(int,int)
meth public java.lang.String toString()
meth public org.w3c.dom.Node getNextSibling()
meth public org.w3c.dom.Node getParentNode()
meth public org.w3c.dom.Node getPreviousSibling()
meth public org.w3c.dom.Text splitText(int)
meth public short getNodeType()
meth public void appendData(java.lang.String)
meth public void deleteData(int,int)
meth public void insertData(int,java.lang.String)
meth public void replaceData(int,int,java.lang.String)
meth public void setData(java.lang.String)
supr org.netbeans.modules.xml.text.syntax.dom.SyntaxNode
hfds parent

CLSS public org.netbeans.modules.xml.text.syntax.dom.Util
cons public init()
fld public static char[] knownEntityChars
fld public static java.lang.String[] knownEntityStrings
meth public static java.lang.String actualAttributeValue(java.lang.String)
meth public static java.lang.String replaceCharsWithEntityStrings(java.lang.String)
meth public static java.lang.String replaceEntityStringsWithChars(java.lang.String)
meth public static org.netbeans.editor.TokenItem skipAttributeValue(org.netbeans.editor.TokenItem,char)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.text.syntax.javacc.DTDSyntaxConstants
fld public final static int ATTLIST = 25
fld public final static int ATTLIST_END = 53
fld public final static int BR_IN_ENTITY = 35
fld public final static int BR_IN_NOTATION = 44
fld public final static int BR_IN_PI_CONTENT = 23
fld public final static int BR_IN_XML_DECL = 17
fld public final static int CHARS_END = 59
fld public final static int CHARS_START = 57
fld public final static int CHREF_END = 73
fld public final static int CHREF_START = 67
fld public final static int COMMENT_END = 66
fld public final static int COMMENT_START = 63
fld public final static int COND = 32
fld public final static int COND_END = 49
fld public final static int COND_END_IN_DEFAULT = 8
fld public final static int CONTENT = 3
fld public final static int CREF_END = 70
fld public final static int CREF_START = 68
fld public final static int CRLF = 1
fld public final static int DECL_END = 33
fld public final static int DECL_START = 5
fld public final static int DEFAULT = 17
fld public final static int DOCTYPE = 26
fld public final static int ELEMENT = 27
fld public final static int ELEMENT_END = 41
fld public final static int ENTITY = 24
fld public final static int ENTITY_END = 37
fld public final static int EOF = 0
fld public final static int ERR_IN_ATTLIST = 50
fld public final static int ERR_IN_CHREF = 74
fld public final static int ERR_IN_COMMENT = 65
fld public final static int ERR_IN_COND = 48
fld public final static int ERR_IN_CREF = 71
fld public final static int ERR_IN_DECL = 31
fld public final static int ERR_IN_DEFAULT = 9
fld public final static int ERR_IN_PI = 12
fld public final static int ERR_IN_PI_CONTENT = 21
fld public final static int IN_ATTLIST = 8
fld public final static int IN_CHARS = 4
fld public final static int IN_CHREF = 0
fld public final static int IN_COMMENT = 2
fld public final static int IN_COND = 9
fld public final static int IN_CREF = 1
fld public final static int IN_DECL = 13
fld public final static int IN_DOCTYPE = 5
fld public final static int IN_ELEMENT = 11
fld public final static int IN_ENTITY = 12
fld public final static int IN_NOTATION = 10
fld public final static int IN_PI = 16
fld public final static int IN_PI_CONTENT = 14
fld public final static int IN_PREF = 7
fld public final static int IN_STRING = 3
fld public final static int IN_TAG_ATTLIST = 6
fld public final static int IN_XML_DECL = 15
fld public final static int KW_IN_ATTLIST = 51
fld public final static int KW_IN_COND = 46
fld public final static int KW_IN_ELEMENT = 38
fld public final static int KW_IN_ENTITY = 34
fld public final static int KW_IN_NOTATION = 42
fld public final static int KW_IN_XML_DECL = 15
fld public final static int NOTATION = 28
fld public final static int NOTATION_END = 45
fld public final static int PI_CONTENT_END = 22
fld public final static int PI_CONTENT_START = 13
fld public final static int PI_END = 14
fld public final static int PI_START = 6
fld public final static int PI_TARGET = 11
fld public final static int PREF_END = 56
fld public final static int PREF_START = 54
fld public final static int Q_IN_XML_DECL = 19
fld public final static int STRING_END = 62
fld public final static int STRING_START = 60
fld public final static int SYMBOL_IN_ELEMENT = 39
fld public final static int TEXT = 2
fld public final static int TEXT_IN_ATTLIST = 52
fld public final static int TEXT_IN_CHARS = 58
fld public final static int TEXT_IN_CHREF = 72
fld public final static int TEXT_IN_COMMENT = 64
fld public final static int TEXT_IN_COND = 47
fld public final static int TEXT_IN_CREF = 69
fld public final static int TEXT_IN_DECL = 29
fld public final static int TEXT_IN_DEFAULT = 7
fld public final static int TEXT_IN_ELEMENT = 40
fld public final static int TEXT_IN_ENTITY = 36
fld public final static int TEXT_IN_NOTATION = 43
fld public final static int TEXT_IN_PI_CONTENT = 20
fld public final static int TEXT_IN_PREF = 55
fld public final static int TEXT_IN_STRING = 61
fld public final static int TEXT_IN_XML_DECL = 16
fld public final static int WS = 4
fld public final static int WS_IN_DECL = 30
fld public final static int XML_DECL_END = 18
fld public final static int XML_TARGET = 10
fld public final static java.lang.String[] tokenImage

CLSS public abstract interface org.netbeans.modules.xml.text.syntax.javacc.XMLSyntaxConstants
fld public final static int ANY = 54
fld public final static int ATTLIST = 40
fld public final static int ATTLIST_END = 80
fld public final static int ATT_NAME = 17
fld public final static int BR_IN_XML_DECL = 32
fld public final static int CDATA = 71
fld public final static int CDATA_CONTENT = 38
fld public final static int CDATA_END = 35
fld public final static int CDATA_START = 10
fld public final static int CHARS_END = 95
fld public final static int CHARS_START = 93
fld public final static int COMMENT_END = 108
fld public final static int COMMENT_START = 105
fld public final static int COND = 47
fld public final static int COND_END = 65
fld public final static int COND_END_IN_DEFAULT = 11
fld public final static int CONTENT = 2
fld public final static int DECL_END = 48
fld public final static int DECL_START = 8
fld public final static int DEFAULT = 20
fld public final static int DOCTYPE = 41
fld public final static int DOCTYPE_END = 85
fld public final static int DTD_END_IN_DEFAULT = 12
fld public final static int ELEMENT = 42
fld public final static int ELEMENT_END = 56
fld public final static int EMPTY = 52
fld public final static int ENTITIES = 75
fld public final static int ENTITY = 39
fld public final static int ENTITY_END = 51
fld public final static int ENTITY_IN_ATTLIST = 74
fld public final static int EOF = 0
fld public final static int EQ_IN_TAG_ATTLIST = 20
fld public final static int ERR_IN_ATTLIST = 66
fld public final static int ERR_IN_COMMENT = 107
fld public final static int ERR_IN_COND = 64
fld public final static int ERR_IN_DECL = 46
fld public final static int ERR_IN_DEFAULT = 14
fld public final static int ERR_IN_DOCTYPE = 84
fld public final static int ERR_IN_GREF = 91
fld public final static int ERR_IN_NOTATION = 59
fld public final static int ERR_IN_PI = 25
fld public final static int ERR_IN_PI_CONTENT = 29
fld public final static int ERR_IN_TAG = 16
fld public final static int ERR_IN_TAG_ATTLIST = 18
fld public final static int FIXED = 69
fld public final static int GREF_CHARS_END = 98
fld public final static int GREF_CHARS_START = 96
fld public final static int GREF_END = 92
fld public final static int GREF_START = 89
fld public final static int GREF_STRING_END = 104
fld public final static int GREF_STRING_START = 102
fld public final static int IDREF = 72
fld public final static int IDREFS = 73
fld public final static int ID_IN_ATTLIST = 70
fld public final static int IGNORE = 62
fld public final static int IMPLIED = 68
fld public final static int INCLUDE = 61
fld public final static int IN_ATTLIST_DECL = 8
fld public final static int IN_CDATA = 14
fld public final static int IN_CHARS = 2
fld public final static int IN_COMMENT = 0
fld public final static int IN_COND = 9
fld public final static int IN_DECL = 13
fld public final static int IN_DOCTYPE = 7
fld public final static int IN_ELEMENT = 11
fld public final static int IN_ENTITY_DECL = 12
fld public final static int IN_GREF = 3
fld public final static int IN_GREF_CHARS = 5
fld public final static int IN_GREF_STRING = 4
fld public final static int IN_NOTATION = 10
fld public final static int IN_PI = 17
fld public final static int IN_PI_CONTENT = 16
fld public final static int IN_PREF = 6
fld public final static int IN_STRING = 1
fld public final static int IN_TAG = 19
fld public final static int IN_TAG_ATTLIST = 18
fld public final static int IN_XML_DECL = 15
fld public final static int KW_IN_ENTITY = 49
fld public final static int KW_IN_XML_DECL = 30
fld public final static int MARKUP_IN_CDATA = 37
fld public final static int NAME = 5
fld public final static int NMTOKEN = 76
fld public final static int NMTOKENS = 77
fld public final static int NOTATION = 43
fld public final static int NOTATION_END = 60
fld public final static int NOTATION_IN_ATTLIST = 78
fld public final static int PCDATA = 53
fld public final static int PI_CONTENT_END = 27
fld public final static int PI_CONTENT_START = 23
fld public final static int PI_END = 24
fld public final static int PI_START = 9
fld public final static int PI_TARGET = 26
fld public final static int PREF_END = 88
fld public final static int PREF_START = 86
fld public final static int PUBLIC = 81
fld public final static int Q_IN_XML_DECL = 34
fld public final static int REQUIRED = 67
fld public final static int RSB = 6
fld public final static int S = 4
fld public final static int STRING_END = 101
fld public final static int STRING_START = 99
fld public final static int SYSTEM = 82
fld public final static int SYSTEM_IN_NOTATION = 57
fld public final static int TAG_END = 21
fld public final static int TAG_NAME = 15
fld public final static int TAG_START = 7
fld public final static int TEXT = 1
fld public final static int TEXT_IN_ATTLIST = 79
fld public final static int TEXT_IN_CDATA = 36
fld public final static int TEXT_IN_CHARS = 94
fld public final static int TEXT_IN_COMMENT = 106
fld public final static int TEXT_IN_COND = 63
fld public final static int TEXT_IN_DECL = 44
fld public final static int TEXT_IN_DEFAULT = 13
fld public final static int TEXT_IN_DOCTYPE = 83
fld public final static int TEXT_IN_ELEMENT = 55
fld public final static int TEXT_IN_ENTITY = 50
fld public final static int TEXT_IN_GREF = 90
fld public final static int TEXT_IN_GREF_CHARS = 97
fld public final static int TEXT_IN_GREF_STRING = 103
fld public final static int TEXT_IN_NOTATION = 58
fld public final static int TEXT_IN_PI_CONTENT = 28
fld public final static int TEXT_IN_PREF = 87
fld public final static int TEXT_IN_STRING = 100
fld public final static int TEXT_IN_XML_DECL = 31
fld public final static int WS = 3
fld public final static int WS_IN_DECL = 45
fld public final static int WS_IN_TAG_ATTLIST = 19
fld public final static int XML_DECL_END = 33
fld public final static int XML_TARGET = 22
fld public final static java.lang.String[] tokenImage

CLSS public abstract interface org.netbeans.modules.xml.text.syntax.javacc.lib.JJConstants
fld public final static int JJ_EOF = -10
fld public final static int JJ_EOL = -11
fld public final static int JJ_ERR = -13

CLSS public abstract interface org.netbeans.modules.xml.text.syntax.javacc.lib.JJMapperInterface
intf org.netbeans.modules.xml.text.syntax.javacc.lib.JJConstants
meth public abstract org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID createToken(int)
meth public abstract org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID guessToken(java.lang.String,int,boolean)
meth public abstract org.netbeans.modules.xml.text.syntax.javacc.lib.JJTokenID supposedToken(java.lang.String,int,int)

CLSS public abstract interface org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
innr public abstract interface static Factory
innr public final static MutableContext
innr public static Context
meth public abstract boolean beforeInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void afterInsert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context) throws javax.swing.text.BadLocationException
meth public abstract void cancelled(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Context)
meth public abstract void insert(org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$MutableContext) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor$Factory
 outer org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor
meth public abstract org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor createTypedBreakInterceptor(org.netbeans.api.editor.mimelookup.MimePath)

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public abstract org.openide.text.IndentEngine
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean acceptMimeType(java.lang.String)
meth public abstract int indentLine(javax.swing.text.Document,int)
meth public abstract int indentNewLine(javax.swing.text.Document,int)
meth public abstract java.io.Writer createWriter(javax.swing.text.Document,int,java.io.Writer)
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static java.util.Enumeration<? extends org.openide.text.IndentEngine> indentEngines()
meth public static org.openide.text.IndentEngine find(java.lang.String)
meth public static org.openide.text.IndentEngine find(javax.swing.text.Document)
meth public static org.openide.text.IndentEngine getDefault()
meth public static void register(java.lang.String,org.openide.text.IndentEngine)
 anno 0 java.lang.Deprecated()
supr org.openide.ServiceType
hfds INSTANCE,map,serialVersionUID
hcls Default

CLSS public final org.openide.text.NbDocument
fld public final static java.lang.Object GUARDED
fld public final static java.lang.String BREAKPOINT_STYLE_NAME = "NbBreakpointStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CURRENT_STYLE_NAME = "NbCurrentStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ERROR_STYLE_NAME = "NbErrorStyle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String NORMAL_STYLE_NAME = "NbNormalStyle"
 anno 0 java.lang.Deprecated()
innr public abstract interface static Annotatable
innr public abstract interface static CustomEditor
innr public abstract interface static CustomToolbar
innr public abstract interface static PositionBiasable
innr public abstract interface static Printable
innr public abstract interface static WriteLockable
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeRedoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static <%0 extends javax.swing.undo.UndoableEdit> {%%0} getEditToBeUndoneOfType(org.openide.cookies.EditorCookie,java.lang.Class<{%%0}>)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static boolean openDocument(org.openide.util.Lookup$Provider,int,org.openide.text.Line$ShowOpenType,org.openide.text.Line$ShowVisibilityType)
meth public static int findLineColumn(javax.swing.text.StyledDocument,int)
meth public static int findLineNumber(javax.swing.text.StyledDocument,int)
meth public static int findLineOffset(javax.swing.text.StyledDocument,int)
meth public static java.lang.Object findPageable(javax.swing.text.StyledDocument)
meth public static javax.swing.JEditorPane findRecentEditorPane(org.openide.cookies.EditorCookie)
meth public static javax.swing.text.Element findLineRootElement(javax.swing.text.StyledDocument)
meth public static javax.swing.text.Position createPosition(javax.swing.text.Document,int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException
meth public static javax.swing.text.StyledDocument getDocument(org.openide.util.Lookup$Provider)
meth public static void addAnnotation(javax.swing.text.StyledDocument,javax.swing.text.Position,int,org.openide.text.Annotation)
meth public static void insertGuarded(javax.swing.text.StyledDocument,int,java.lang.String) throws javax.swing.text.BadLocationException
meth public static void markBreakpoint(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markCurrent(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markError(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void markGuarded(javax.swing.text.StyledDocument,int,int)
meth public static void markNormal(javax.swing.text.StyledDocument,int)
 anno 0 java.lang.Deprecated()
meth public static void removeAnnotation(javax.swing.text.StyledDocument,org.openide.text.Annotation)
meth public static void runAtomic(javax.swing.text.StyledDocument,java.lang.Runnable)
meth public static void runAtomicAsUser(javax.swing.text.StyledDocument,java.lang.Runnable) throws javax.swing.text.BadLocationException
meth public static void unmarkGuarded(javax.swing.text.StyledDocument,int,int)
supr java.lang.Object
hfds ATTR_ADD,ATTR_REMOVE
hcls DocumentRenderer

CLSS public abstract interface static org.openide.text.NbDocument$Annotatable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void addAnnotation(javax.swing.text.Position,int,org.openide.text.Annotation)
meth public abstract void removeAnnotation(org.openide.text.Annotation)

CLSS public abstract interface static org.openide.text.NbDocument$CustomEditor
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.awt.Component createEditor(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$CustomToolbar
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.JToolBar createToolbar(javax.swing.JEditorPane)

CLSS public abstract interface static org.openide.text.NbDocument$PositionBiasable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public abstract interface static org.openide.text.NbDocument$Printable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract java.text.AttributedCharacterIterator[] createPrintIterators()

CLSS public abstract interface static org.openide.text.NbDocument$WriteLockable
 outer org.openide.text.NbDocument
intf javax.swing.text.Document
meth public abstract void runAtomic(java.lang.Runnable)
meth public abstract void runAtomicAsUser(java.lang.Runnable) throws javax.swing.text.BadLocationException

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

CLSS public abstract interface org.w3c.dom.Attr
intf org.w3c.dom.Node
meth public abstract boolean getSpecified()
meth public abstract boolean isId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract org.w3c.dom.Element getOwnerElement()
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.w3c.dom.CharacterData
intf org.w3c.dom.Node
meth public abstract int getLength()
meth public abstract java.lang.String getData()
meth public abstract java.lang.String substringData(int,int)
meth public abstract void appendData(java.lang.String)
meth public abstract void deleteData(int,int)
meth public abstract void insertData(int,java.lang.String)
meth public abstract void replaceData(int,int,java.lang.String)
meth public abstract void setData(java.lang.String)

CLSS public abstract interface org.w3c.dom.Comment
intf org.w3c.dom.CharacterData

CLSS public abstract interface org.w3c.dom.Document
intf org.w3c.dom.Node
meth public abstract boolean getStrictErrorChecking()
meth public abstract boolean getXmlStandalone()
meth public abstract java.lang.String getDocumentURI()
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getXmlEncoding()
meth public abstract java.lang.String getXmlVersion()
meth public abstract org.w3c.dom.Attr createAttribute(java.lang.String)
meth public abstract org.w3c.dom.Attr createAttributeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.CDATASection createCDATASection(java.lang.String)
meth public abstract org.w3c.dom.Comment createComment(java.lang.String)
meth public abstract org.w3c.dom.DOMConfiguration getDomConfig()
meth public abstract org.w3c.dom.DOMImplementation getImplementation()
meth public abstract org.w3c.dom.DocumentFragment createDocumentFragment()
meth public abstract org.w3c.dom.DocumentType getDoctype()
meth public abstract org.w3c.dom.Element createElement(java.lang.String)
meth public abstract org.w3c.dom.Element createElementNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Element getDocumentElement()
meth public abstract org.w3c.dom.Element getElementById(java.lang.String)
meth public abstract org.w3c.dom.EntityReference createEntityReference(java.lang.String)
meth public abstract org.w3c.dom.Node adoptNode(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node importNode(org.w3c.dom.Node,boolean)
meth public abstract org.w3c.dom.Node renameNode(org.w3c.dom.Node,java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Text createTextNode(java.lang.String)
meth public abstract void normalizeDocument()
meth public abstract void setDocumentURI(java.lang.String)
meth public abstract void setStrictErrorChecking(boolean)
meth public abstract void setXmlStandalone(boolean)
meth public abstract void setXmlVersion(java.lang.String)

CLSS public abstract interface org.w3c.dom.DocumentType
intf org.w3c.dom.Node
meth public abstract java.lang.String getInternalSubset()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getSystemId()
meth public abstract org.w3c.dom.NamedNodeMap getEntities()
meth public abstract org.w3c.dom.NamedNodeMap getNotations()

CLSS public abstract interface org.w3c.dom.Element
intf org.w3c.dom.Node
meth public abstract boolean hasAttribute(java.lang.String)
meth public abstract boolean hasAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttributeNS(java.lang.String,java.lang.String)
meth public abstract java.lang.String getTagName()
meth public abstract org.w3c.dom.Attr getAttributeNode(java.lang.String)
meth public abstract org.w3c.dom.Attr getAttributeNodeNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr)
meth public abstract org.w3c.dom.NodeList getElementsByTagName(java.lang.String)
meth public abstract org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String,java.lang.String)
meth public abstract org.w3c.dom.TypeInfo getSchemaTypeInfo()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void removeAttributeNS(java.lang.String,java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setAttributeNS(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setIdAttribute(java.lang.String,boolean)
meth public abstract void setIdAttributeNS(java.lang.String,java.lang.String,boolean)
meth public abstract void setIdAttributeNode(org.w3c.dom.Attr,boolean)

CLSS public abstract interface org.w3c.dom.EntityReference
intf org.w3c.dom.Node

CLSS public abstract interface org.w3c.dom.Node
fld public final static short ATTRIBUTE_NODE = 2
fld public final static short CDATA_SECTION_NODE = 4
fld public final static short COMMENT_NODE = 8
fld public final static short DOCUMENT_FRAGMENT_NODE = 11
fld public final static short DOCUMENT_NODE = 9
fld public final static short DOCUMENT_POSITION_CONTAINED_BY = 16
fld public final static short DOCUMENT_POSITION_CONTAINS = 8
fld public final static short DOCUMENT_POSITION_DISCONNECTED = 1
fld public final static short DOCUMENT_POSITION_FOLLOWING = 4
fld public final static short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 32
fld public final static short DOCUMENT_POSITION_PRECEDING = 2
fld public final static short DOCUMENT_TYPE_NODE = 10
fld public final static short ELEMENT_NODE = 1
fld public final static short ENTITY_NODE = 6
fld public final static short ENTITY_REFERENCE_NODE = 5
fld public final static short NOTATION_NODE = 12
fld public final static short PROCESSING_INSTRUCTION_NODE = 7
fld public final static short TEXT_NODE = 3
meth public abstract boolean hasAttributes()
meth public abstract boolean hasChildNodes()
meth public abstract boolean isDefaultNamespace(java.lang.String)
meth public abstract boolean isEqualNode(org.w3c.dom.Node)
meth public abstract boolean isSameNode(org.w3c.dom.Node)
meth public abstract boolean isSupported(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getFeature(java.lang.String,java.lang.String)
meth public abstract java.lang.Object getUserData(java.lang.String)
meth public abstract java.lang.Object setUserData(java.lang.String,java.lang.Object,org.w3c.dom.UserDataHandler)
meth public abstract java.lang.String getBaseURI()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract java.lang.String getNodeName()
meth public abstract java.lang.String getNodeValue()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getTextContent()
meth public abstract java.lang.String lookupNamespaceURI(java.lang.String)
meth public abstract java.lang.String lookupPrefix(java.lang.String)
meth public abstract org.w3c.dom.Document getOwnerDocument()
meth public abstract org.w3c.dom.NamedNodeMap getAttributes()
meth public abstract org.w3c.dom.Node appendChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node cloneNode(boolean)
meth public abstract org.w3c.dom.Node getFirstChild()
meth public abstract org.w3c.dom.Node getLastChild()
meth public abstract org.w3c.dom.Node getNextSibling()
meth public abstract org.w3c.dom.Node getParentNode()
meth public abstract org.w3c.dom.Node getPreviousSibling()
meth public abstract org.w3c.dom.Node insertBefore(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node removeChild(org.w3c.dom.Node)
meth public abstract org.w3c.dom.Node replaceChild(org.w3c.dom.Node,org.w3c.dom.Node)
meth public abstract org.w3c.dom.NodeList getChildNodes()
meth public abstract short compareDocumentPosition(org.w3c.dom.Node)
meth public abstract short getNodeType()
meth public abstract void normalize()
meth public abstract void setNodeValue(java.lang.String)
meth public abstract void setPrefix(java.lang.String)
meth public abstract void setTextContent(java.lang.String)

CLSS public abstract interface org.w3c.dom.Text
intf org.w3c.dom.CharacterData
meth public abstract boolean isElementContentWhitespace()
meth public abstract java.lang.String getWholeText()
meth public abstract org.w3c.dom.Text replaceWholeText(java.lang.String)
meth public abstract org.w3c.dom.Text splitText(int)

