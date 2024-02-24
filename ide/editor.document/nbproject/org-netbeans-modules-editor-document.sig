#Signature file v4.1
#Version 1.32.0

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface org.netbeans.api.editor.document.AtomicLockDocument
meth public abstract javax.swing.text.Document getDocument()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void atomicUndo()
meth public abstract void removeAtomicLockListener(org.netbeans.api.editor.document.AtomicLockListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runAtomic(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runAtomicAsUser(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.api.editor.document.AtomicLockEvent
cons public init(javax.swing.text.Document)
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.api.editor.document.AtomicLockDocument getAtomicLock()
supr java.util.EventObject

CLSS public abstract interface org.netbeans.api.editor.document.AtomicLockListener
intf java.util.EventListener
meth public abstract void atomicLock(org.netbeans.api.editor.document.AtomicLockEvent)
meth public abstract void atomicUnlock(org.netbeans.api.editor.document.AtomicLockEvent)

CLSS public final org.netbeans.api.editor.document.ComplexPositions
meth public static int compare(int,int,int,int)
meth public static int compare(javax.swing.text.Position,javax.swing.text.Position)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static int getSplitOffset(javax.swing.text.Position)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.text.Position create(javax.swing.text.Position,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.editor.document.CustomUndoDocument
meth public abstract void addUndoableEdit(javax.swing.undo.UndoableEdit)

CLSS public abstract interface org.netbeans.api.editor.document.DocumentLockState
meth public abstract boolean isReadLocked()
meth public abstract boolean isWriteLocked()

CLSS public final org.netbeans.api.editor.document.EditorDocumentUtils
meth public static org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void runExclusive(javax.swing.text.Document,java.lang.Runnable)
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.api.editor.document.EditorMimeTypes
fld public final static java.lang.String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes"
meth public java.util.Set<java.lang.String> getSupportedMimeTypes()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.editor.document.EditorMimeTypes getDefault()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impl,instance,listener,listeners

CLSS public abstract interface org.netbeans.api.editor.document.LineDocument
intf javax.swing.text.Document
meth public abstract javax.swing.text.Element getParagraphElement(int)
meth public abstract javax.swing.text.Position createPosition(int,javax.swing.text.Position$Bias) throws javax.swing.text.BadLocationException

CLSS public final org.netbeans.api.editor.document.LineDocumentUtils
meth public static <%0 extends java.lang.Object> {%%0} as(javax.swing.text.Document,java.lang.Class<{%%0}>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static <%0 extends java.lang.Object> {%%0} asRequired(javax.swing.text.Document,java.lang.Class<{%%0}>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isLineEmpty(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isLineWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineCount(org.netbeans.api.editor.document.LineDocument)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineCount(org.netbeans.api.editor.document.LineDocument,int,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineFirstNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineIndex(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineLastNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineStart(org.netbeans.api.editor.document.LineDocument,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getLineStartFromIndex(org.netbeans.api.editor.document.LineDocument,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonNewline(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextNonWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getNextWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonNewline(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousNonWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWhitespace(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWhitespace(org.netbeans.api.editor.document.LineDocument,int,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWordEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getPreviousWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getWordEnd(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static int getWordStart(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getWord(org.netbeans.api.editor.document.LineDocument,int) throws javax.swing.text.BadLocationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.editor.document.LineDocument createDocument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds NOT_FOUND,WRONG_POSITION_LOCALE
hcls V

CLSS public abstract interface org.netbeans.spi.editor.document.DocumentFactory
meth public abstract javax.swing.text.Document createDocument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.swing.text.Document getDocument(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject(javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.document.EditorMimeTypesImplementation
fld public final static java.lang.String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes"
meth public abstract java.util.Set<java.lang.String> getSupportedMimeTypes()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.editor.document.UndoableEditWrapper
meth public abstract javax.swing.undo.UndoableEdit wrap(javax.swing.undo.UndoableEdit,javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

