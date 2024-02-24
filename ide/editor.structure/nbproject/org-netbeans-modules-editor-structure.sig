#Signature file v4.1
#Version 1.70.0

CLSS public abstract interface java.io.Serializable

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface org.netbeans.editor.ext.FormatLayer
meth public abstract java.lang.String getName()
meth public abstract void format(org.netbeans.editor.ext.FormatWriter)

CLSS public final org.netbeans.modules.editor.structure.api.DocumentElement
meth public boolean equals(java.lang.Object)
meth public boolean isLeaf()
meth public int getElementCount()
meth public int getElementIndex(int)
meth public int getEndOffset()
meth public int getStartOffset()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.editor.structure.api.DocumentElement> getChildren()
meth public javax.swing.text.AttributeSet getAttributes()
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.modules.editor.structure.api.DocumentElement getElement(int)
meth public org.netbeans.modules.editor.structure.api.DocumentElement getParentElement()
meth public org.netbeans.modules.editor.structure.api.DocumentModel getDocumentModel()
meth public void addDocumentElementListener(org.netbeans.modules.editor.structure.api.DocumentElementListener)
meth public void removeDocumentElementListener(org.netbeans.modules.editor.structure.api.DocumentElementListener)
supr java.lang.Object
hfds EMPTY_ATTRIBUTES,PRINT_MAX_CHARS,attributes,deListener,deListeners,endPos,model,name,startPos,type
hcls Attributes

CLSS public final org.netbeans.modules.editor.structure.api.DocumentElementEvent
fld public final static int ATTRIBUTES_CHANGED = 5
fld public final static int CHILDREN_REORDERED = 4
fld public final static int CHILD_ADDED = 2
fld public final static int CHILD_REMOVED = 3
fld public final static int CONTENT_CHANGED = 1
meth public int getType()
meth public org.netbeans.modules.editor.structure.api.DocumentElement getChangedChild()
meth public org.netbeans.modules.editor.structure.api.DocumentElement getSourceDocumentElement()
supr java.util.EventObject
hfds changedChild,type

CLSS public abstract interface org.netbeans.modules.editor.structure.api.DocumentElementListener
intf java.util.EventListener
meth public abstract void attributesChanged(org.netbeans.modules.editor.structure.api.DocumentElementEvent)
meth public abstract void childrenReordered(org.netbeans.modules.editor.structure.api.DocumentElementEvent)
meth public abstract void contentChanged(org.netbeans.modules.editor.structure.api.DocumentElementEvent)
meth public abstract void elementAdded(org.netbeans.modules.editor.structure.api.DocumentElementEvent)
meth public abstract void elementRemoved(org.netbeans.modules.editor.structure.api.DocumentElementEvent)

CLSS public final org.netbeans.modules.editor.structure.api.DocumentModel
fld public final static java.util.Comparator<org.netbeans.modules.editor.structure.api.DocumentElement> ELEMENTS_COMPARATOR
innr public DocumentChange
innr public final DocumentModelModificationTransaction
innr public final DocumentModelTransactionCancelledException
meth public boolean isDescendantOf(org.netbeans.modules.editor.structure.api.DocumentElement,org.netbeans.modules.editor.structure.api.DocumentElement)
meth public final void readLock()
meth public final void readUnlock()
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.modules.editor.structure.api.DocumentElement getLeafElementForOffset(int)
meth public org.netbeans.modules.editor.structure.api.DocumentElement getRootElement()
meth public static org.netbeans.modules.editor.structure.api.DocumentModel getDocumentModel(javax.swing.text.Document) throws org.netbeans.modules.editor.structure.api.DocumentModelException
meth public void addDocumentModelListener(org.netbeans.modules.editor.structure.api.DocumentModelListener)
meth public void addDocumentModelStateListener(org.netbeans.modules.editor.structure.api.DocumentModelStateListener)
meth public void forceUpdate()
meth public void removeDocumentModelListener(org.netbeans.modules.editor.structure.api.DocumentModelListener)
meth public void removeDocumentModelStateListener(org.netbeans.modules.editor.structure.api.DocumentModelStateListener)
supr java.lang.Object
hfds DOCUMENT_ROOT_ELEMENT_TYPE,ELEMENT_ADDED,ELEMENT_ATTRS_CHANGED,ELEMENT_CHANGED,ELEMENT_REMOVED,EMPTY_ATTRS_MAP,EMPTY_ELEMENTS_LIST,GENERATING_MODEL_PROPERTY,MODEL_UPDATE_TIMEOUT,changesWatcher,currReader,currWriter,debug,dmListeners,dmsListeners,doc,documentDirty,elements,elementsAttrNamesCache,elementsAttrValueCache,elementsNamesCache,elementsTypesCache,last_empty_element_start_offset,last_parent,locks,measure,modelUpdateTransaction,numReaders,numWriters,parent,parent_count,provider,requestProcessor,rootElement,task
hcls DocumentChangesWatcher,ElementsArray

CLSS public org.netbeans.modules.editor.structure.api.DocumentModel$DocumentChange
 outer org.netbeans.modules.editor.structure.api.DocumentModel
fld public final static int INSERT = 0
fld public final static int REMOVE = 1
meth public int getChangeLength()
meth public int getChangeType()
meth public java.lang.String toString()
meth public javax.swing.text.Position getChangeStart()
supr java.lang.Object
hfds changeLength,changeStart,type

CLSS public final org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelModificationTransaction
 outer org.netbeans.modules.editor.structure.api.DocumentModel
meth public org.netbeans.modules.editor.structure.api.DocumentElement addDocumentElement(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,int,int) throws javax.swing.text.BadLocationException,org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException
meth public void removeDocumentElement(org.netbeans.modules.editor.structure.api.DocumentElement,boolean) throws org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException
meth public void updateDocumentElementAttribs(org.netbeans.modules.editor.structure.api.DocumentElement,java.util.Map<java.lang.String,java.lang.String>) throws org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException
meth public void updateDocumentElementText(org.netbeans.modules.editor.structure.api.DocumentElement) throws org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException
supr java.lang.Object
hfds init,modifications,transactionCancelled
hcls DocumentModelModification

CLSS public final org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException
 outer org.netbeans.modules.editor.structure.api.DocumentModel
cons public init(org.netbeans.modules.editor.structure.api.DocumentModel)
supr java.lang.Exception

CLSS public org.netbeans.modules.editor.structure.api.DocumentModelException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.netbeans.modules.editor.structure.api.DocumentModelListener
intf java.util.EventListener
meth public abstract void documentElementAdded(org.netbeans.modules.editor.structure.api.DocumentElement)
meth public abstract void documentElementAttributesChanged(org.netbeans.modules.editor.structure.api.DocumentElement)
meth public abstract void documentElementChanged(org.netbeans.modules.editor.structure.api.DocumentElement)
meth public abstract void documentElementRemoved(org.netbeans.modules.editor.structure.api.DocumentElement)

CLSS public abstract interface org.netbeans.modules.editor.structure.api.DocumentModelStateListener
meth public abstract void scanningStarted()
meth public abstract void sourceChanged()
meth public abstract void updateFinished()
meth public abstract void updateStarted()

CLSS public final org.netbeans.modules.editor.structure.api.DocumentModelUtils
cons public init()
meth public static java.util.List<org.netbeans.modules.editor.structure.api.DocumentElement> getDescendants(org.netbeans.modules.editor.structure.api.DocumentElement)
meth public static org.netbeans.modules.editor.structure.api.DocumentElement findElement(org.netbeans.modules.editor.structure.api.DocumentModel,int,java.lang.String,java.lang.String) throws javax.swing.text.BadLocationException
meth public static org.netbeans.modules.editor.structure.api.DocumentElement[] elements(org.netbeans.modules.editor.structure.api.DocumentModel)
meth public static void dumpElementStructure(org.netbeans.modules.editor.structure.api.DocumentElement)
meth public static void dumpModelElements(org.netbeans.modules.editor.structure.api.DocumentModel)
supr java.lang.Object

CLSS public org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence
cons public init(org.netbeans.api.lexer.TokenSequence[],org.netbeans.modules.editor.structure.formatting.TextBounds[])
meth public boolean moveNext()
meth public boolean movePrevious()
meth public int move(int)
meth public int offset()
meth public org.netbeans.api.lexer.Token token()
meth public org.netbeans.api.lexer.TokenSequence currentTokenSequence()
meth public org.netbeans.api.lexer.TokenSequence embedded()
meth public void moveStart()
supr java.lang.Object
hfds currentTokenSequence,tokenSequenceBounds,tokenSequences

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

CLSS protected org.netbeans.modules.editor.structure.formatting.TagBasedFormatter$InitialIndentData
 outer org.netbeans.modules.editor.structure.formatting.TagBasedFormatter
cons public init(org.netbeans.modules.editor.structure.formatting.TagBasedFormatter,org.netbeans.editor.BaseDocument,int[],int[],int,int) throws javax.swing.text.BadLocationException
meth public boolean isEligibleToIndent(int)
meth public int getIndent(int)
supr java.lang.Object
hfds doc,indentBias,indentLevelBias,indentLevels,indentsWithinTags

CLSS protected static org.netbeans.modules.editor.structure.formatting.TagBasedFormatter$TagIndentationData
 outer org.netbeans.modules.editor.structure.formatting.TagBasedFormatter
cons public init(java.lang.String,int)
meth public int getClosedOnLine()
meth public int getLine()
meth public java.lang.String getTagName()
meth public void setClosedOnLine(int)
supr java.lang.Object
hfds closedOnLine,line,tagName

CLSS public abstract org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter
cons public init()
innr protected static TagIndentationData
innr public final static !enum EmbeddingType
meth protected abstract boolean areTagNamesEqual(java.lang.String,java.lang.String)
meth protected abstract boolean isClosingTag(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract boolean isClosingTagRequired(org.netbeans.editor.BaseDocument,java.lang.String)
meth protected abstract boolean isOpeningTag(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract boolean isUnformattableTag(java.lang.String)
meth protected abstract boolean isUnformattableToken(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract int getOpeningSymbolOffset(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract int getTagEndOffset(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract int getTagEndingAtPosition(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int) throws javax.swing.text.BadLocationException
meth protected abstract java.lang.String extractTagName(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected abstract org.netbeans.api.lexer.LanguagePath supportedLanguagePath()
meth protected boolean isJustBeforeClosingTag(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int) throws javax.swing.text.BadLocationException
meth protected boolean isOnlyWhiteSpaces(java.lang.CharSequence)
meth protected boolean isTopLevelLanguage(org.netbeans.editor.BaseDocument)
meth protected boolean isWSToken(org.netbeans.api.lexer.Token)
meth protected int getIndentForTagParameter(org.netbeans.editor.BaseDocument,org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int) throws javax.swing.text.BadLocationException
meth protected int getInitialIndentFromPreviousLine(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected int getMatchingOpeningTagStart(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected int getNextClosingTagOffset(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int) throws javax.swing.text.BadLocationException
meth protected org.netbeans.api.lexer.Token getTokenAtOffset(org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence,int)
meth protected static int getExistingIndent(org.netbeans.editor.BaseDocument,int) throws javax.swing.text.BadLocationException
meth protected static int getNumberOfLines(org.netbeans.editor.BaseDocument) throws javax.swing.text.BadLocationException
meth public boolean handleSmartEnter(org.netbeans.modules.editor.indent.spi.Context) throws javax.swing.text.BadLocationException
meth public boolean isJustAfterClosingTag(org.netbeans.editor.BaseDocument,int)
meth public boolean isSmartEnter(org.netbeans.editor.BaseDocument,int)
meth public void enterPressed(org.netbeans.modules.editor.indent.spi.Context)
meth public void process(org.netbeans.modules.editor.indent.spi.Context) throws javax.swing.text.BadLocationException
meth public void reformat(org.netbeans.modules.editor.indent.spi.Context) throws javax.swing.text.BadLocationException
meth public void reformat(org.netbeans.modules.editor.indent.spi.Context,int,int) throws javax.swing.text.BadLocationException
supr java.lang.Object
hfds logger
hcls EnterPressedTask,FormattingTask,IsJustAfterClosingTagTask

CLSS public final static !enum org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType
 outer org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter
fld public final static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType CURRENT_LANG
fld public final static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType INNER
fld public final static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType OUTER
meth public static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType valueOf(java.lang.String)
meth public static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType[] values()
supr java.lang.Enum<org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$EmbeddingType>

CLSS protected static org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter$TagIndentationData
 outer org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter
cons public init(java.lang.String,int)
meth public int getClosedOnLine()
meth public int getLine()
meth public java.lang.String getTagName()
meth public void setClosedOnLine(int)
supr java.lang.Object
hfds closedOnLine,line,tagName

CLSS public org.netbeans.modules.editor.structure.formatting.TextBounds
cons public init(int,int)
cons public init(int,int,int,int,int,int)
meth public int getAbsoluteEnd()
meth public int getAbsoluteStart()
meth public int getEndLine()
meth public int getEndPos()
meth public int getStartLine()
meth public int getStartPos()
meth public java.lang.String toString()
supr java.lang.Object
hfds absoluteEnd,absoluteStart,endLine,endPos,startLine,startPos

CLSS public org.netbeans.modules.editor.structure.formatting.TransferData
cons public init()
fld public final static java.lang.String ORG_CARET_OFFSET_DOCPROPERTY = "TagBasedFormatter.org_caret_offset"
fld public final static java.lang.String TRANSFER_DATA_DOC_PROPERTY = "TagBasedFormatter.TransferData"
meth public boolean isFormattable(int)
meth public boolean wasProcessedByNativeFormatter(int)
meth public int getNumberOfLines()
meth public int getOriginalIndent(int)
meth public int[] getTransformedOffsets()
meth public static org.netbeans.modules.editor.structure.formatting.TransferData readFromDocument(org.netbeans.editor.BaseDocument)
meth public void init(org.netbeans.editor.BaseDocument) throws javax.swing.text.BadLocationException
meth public void setNonFormattable(int)
meth public void setProcessedByNativeFormatter(int)
meth public void setTransformedOffsets(int[])
supr java.lang.Object
hfds alreadyProcessedByNativeFormatter,formattableLines,numberOfLines,originalIndents,transformedOffsets

CLSS public abstract interface org.netbeans.modules.editor.structure.spi.DocumentModelProvider
 anno 0 org.netbeans.spi.editor.mimelookup.MimeLocation(java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass=class org.netbeans.spi.editor.mimelookup.InstanceProvider, java.lang.String subfolderName="DocumentModel")
meth public abstract void updateModel(org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelModificationTransaction,org.netbeans.modules.editor.structure.api.DocumentModel,org.netbeans.modules.editor.structure.api.DocumentModel$DocumentChange[]) throws org.netbeans.modules.editor.structure.api.DocumentModel$DocumentModelTransactionCancelledException,org.netbeans.modules.editor.structure.api.DocumentModelException

CLSS public abstract interface !annotation org.netbeans.spi.editor.mimelookup.MimeLocation
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.netbeans.spi.editor.mimelookup.InstanceProvider> instanceProviderClass()
meth public abstract java.lang.String subfolderName()

