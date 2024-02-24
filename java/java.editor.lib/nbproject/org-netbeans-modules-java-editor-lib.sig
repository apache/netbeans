#Signature file v4.1
#Version 1.53.0

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
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

CLSS public org.netbeans.editor.ext.MultiSyntax
cons public init()
innr public static MultiStateInfo
meth protected void registerSyntax(org.netbeans.editor.Syntax)
meth public int compareState(org.netbeans.editor.Syntax$StateInfo)
meth public org.netbeans.editor.Syntax$StateInfo createStateInfo()
meth public void load(org.netbeans.editor.Syntax$StateInfo,char[],int,int,boolean,int)
meth public void loadInitState()
meth public void storeState(org.netbeans.editor.Syntax$StateInfo)
supr org.netbeans.editor.Syntax
hfds slaveSyntaxChain,slaveSyntaxChainEnd
hcls SyntaxInfo

CLSS public org.netbeans.editor.ext.java.JavaDocSyntax
cons public init()
meth protected org.netbeans.editor.TokenID parseToken()
supr org.netbeans.editor.ext.MultiSyntax
hfds HTML_ON,ISI_ERROR

CLSS public abstract org.netbeans.editor.ext.java.JavaFoldManager
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static org.netbeans.api.editor.fold.FoldType CODE_BLOCK_FOLD_TYPE
fld public final static org.netbeans.api.editor.fold.FoldType IMPORTS_FOLD_TYPE
fld public final static org.netbeans.api.editor.fold.FoldType INITIAL_COMMENT_FOLD_TYPE
fld public final static org.netbeans.api.editor.fold.FoldType INNERCLASS_TYPE
fld public final static org.netbeans.api.editor.fold.FoldType JAVADOC_FOLD_TYPE
fld public final static org.netbeans.api.editor.fold.FoldType METHOD_BLOCK_FOLD_TYPE
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate CODE_BLOCK_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate IMPORTS_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate INITIAL_COMMENT_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate INNER_CLASS_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate JAVADOC_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate METHOD_BLOCK_FOLD_TEMPLATE
 anno 0 java.lang.Deprecated()
innr protected final static FoldTemplate
intf org.netbeans.spi.editor.fold.FoldManager
supr java.lang.Object
hfds CODE_BLOCK_FOLD_DESCRIPTION,COMMENT_FOLD_DESCRIPTION,IMPORTS_FOLD_DESCRIPTION,JAVADOC_FOLD_DESCRIPTION

CLSS protected final static org.netbeans.editor.ext.java.JavaFoldManager$FoldTemplate
 outer org.netbeans.editor.ext.java.JavaFoldManager
cons protected init(org.netbeans.api.editor.fold.FoldType,java.lang.String,int,int)
meth public int getEndGuardedLength()
meth public int getStartGuardedLength()
meth public java.lang.String getDescription()
meth public org.netbeans.api.editor.fold.FoldType getType()
supr java.lang.Object
hfds description,endGuardedLength,startGuardedLength,type

CLSS public org.netbeans.editor.ext.java.JavaFormatSupport
cons public init(org.netbeans.editor.ext.FormatWriter)
cons public init(org.netbeans.editor.ext.FormatWriter,org.netbeans.editor.TokenContextPath)
meth public boolean canModifyWhitespace(org.netbeans.editor.TokenItem)
meth public boolean getFormatLeadingSpaceInComment()
meth public boolean getFormatLeadingStarInComment()
meth public boolean getFormatNewlineBeforeBrace()
meth public boolean getFormatSpaceAfterComma()
meth public boolean getFormatSpaceBeforeParenthesis()
meth public boolean isComment(org.netbeans.editor.TokenItem,int)
meth public boolean isEnumComma(org.netbeans.editor.TokenItem)
meth public boolean isForLoopSemicolon(org.netbeans.editor.TokenItem)
meth public boolean isJavaDocComment(org.netbeans.editor.TokenItem)
meth public boolean isMultiLineComment(org.netbeans.editor.TokenItem)
meth public boolean isMultiLineComment(org.netbeans.editor.ext.FormatTokenPosition)
meth public int findIndent(org.netbeans.editor.TokenItem)
meth public int getTokenIndent(org.netbeans.editor.TokenItem)
meth public int getTokenIndent(org.netbeans.editor.TokenItem,boolean)
meth public java.lang.String getIndentString(int)
meth public org.netbeans.editor.TokenContextPath getTokenContextPath()
meth public org.netbeans.editor.TokenContextPath getWhitespaceTokenContextPath()
meth public org.netbeans.editor.TokenID getWhitespaceTokenID()
meth public org.netbeans.editor.TokenItem findIf(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findStatement(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findStatementStart(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findStatementStart(org.netbeans.editor.TokenItem,boolean)
meth public org.netbeans.editor.TokenItem findSwitch(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.TokenItem findTry(org.netbeans.editor.TokenItem)
meth public org.netbeans.editor.ext.FormatTokenPosition findLineFirstNonWhitespaceAndNonLeftBrace(org.netbeans.editor.ext.FormatTokenPosition)
meth public org.netbeans.editor.ext.FormatTokenPosition indentLine(org.netbeans.editor.ext.FormatTokenPosition)
supr org.netbeans.editor.ext.ExtFormatSupport
hfds tokenContextPath

CLSS public org.netbeans.editor.ext.java.JavaFormatter
cons public init(java.lang.Class)
innr public JavaLayer
innr public StripEndWhitespaceLayer
meth protected boolean acceptSyntax(org.netbeans.editor.Syntax)
meth protected void initFormatLayers()
meth public int[] getReformatBlock(javax.swing.text.JTextComponent,java.lang.String)
meth public org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.ExtFormatter

CLSS public org.netbeans.editor.ext.java.JavaFormatter$JavaLayer
 outer org.netbeans.editor.ext.java.JavaFormatter
cons public init(org.netbeans.editor.ext.java.JavaFormatter)
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth protected void formatLine(org.netbeans.editor.ext.java.JavaFormatSupport,org.netbeans.editor.ext.FormatTokenPosition)
meth public void format(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.AbstractFormatLayer

CLSS public org.netbeans.editor.ext.java.JavaFormatter$StripEndWhitespaceLayer
 outer org.netbeans.editor.ext.java.JavaFormatter
cons public init(org.netbeans.editor.ext.java.JavaFormatter)
meth protected org.netbeans.editor.ext.FormatSupport createFormatSupport(org.netbeans.editor.ext.FormatWriter)
meth public void format(org.netbeans.editor.ext.FormatWriter)
supr org.netbeans.editor.ext.AbstractFormatLayer

CLSS public org.netbeans.editor.ext.java.JavaLayerTokenContext
fld public final static int METHOD_ID = 1
fld public final static org.netbeans.editor.BaseTokenID METHOD
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.editor.ext.java.JavaLayerTokenContext context
supr org.netbeans.editor.TokenContext

CLSS public org.netbeans.editor.ext.java.JavaSyntax
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth protected org.netbeans.editor.TokenID parseToken()
meth public java.lang.String getStateName(int)
meth public org.netbeans.editor.TokenID matchKeyword(char[],int,int)
supr org.netbeans.editor.Syntax
hfds ISA_AND,ISA_DOT,ISA_EQ,ISA_EXCLAMATION,ISA_GT,ISA_GTGT,ISA_GTGTGT,ISA_LT,ISA_LTLT,ISA_MINUS,ISA_PERCENT,ISA_PIPE,ISA_PLUS,ISA_SLASH,ISA_STAR,ISA_STAR_I_BLOCK_COMMENT,ISA_XOR,ISA_ZERO,ISI_BLOCK_COMMENT,ISI_CHAR,ISI_CHAR_A_BSLASH,ISI_DOUBLE,ISI_DOUBLE_EXP,ISI_HEX,ISI_IDENTIFIER,ISI_INT,ISI_LINE_COMMENT,ISI_OCTAL,ISI_STRING,ISI_STRING_A_BSLASH,ISI_WHITESPACE,isJava15,useInJsp

CLSS public org.netbeans.editor.ext.java.JavaTokenContext
fld public final static int ABSTRACT_ID = 72
fld public final static int AND_AND_ID = 61
fld public final static int AND_EQ_ID = 43
fld public final static int AND_ID = 27
fld public final static int ANNOTATION_ID = 123
fld public final static int ASSERT_ID = 73
fld public final static int BLOCK_COMMENT_ID = 8
fld public final static int BOOLEAN_ID = 63
fld public final static int BREAK_ID = 74
fld public final static int BYTE_ID = 64
fld public final static int CASE_ID = 75
fld public final static int CATCH_ID = 76
fld public final static int CHAR_ID = 65
fld public final static int CHAR_LITERAL_ID = 9
fld public final static int CLASS_ID = 77
fld public final static int COLON_ID = 50
fld public final static int COMMA_ID = 49
fld public final static int CONST_ID = 78
fld public final static int CONTINUE_ID = 79
fld public final static int DEFAULT_ID = 80
fld public final static int DIV_EQ_ID = 42
fld public final static int DIV_ID = 26
fld public final static int DOT_ID = 48
fld public final static int DOUBLE_ID = 66
fld public final static int DOUBLE_LITERAL_ID = 16
fld public final static int DO_ID = 81
fld public final static int ELLIPSIS_ID = 124
fld public final static int ELSE_ID = 82
fld public final static int ENUM_ID = 83
fld public final static int EQ_EQ_ID = 33
fld public final static int EQ_ID = 17
fld public final static int ERRORS_ID = 4
fld public final static int EXTENDS_ID = 84
fld public final static int FALSE_ID = 85
fld public final static int FINALLY_ID = 87
fld public final static int FINAL_ID = 86
fld public final static int FLOAT_ID = 67
fld public final static int FLOAT_LITERAL_ID = 15
fld public final static int FOR_ID = 88
fld public final static int GOTO_ID = 89
fld public final static int GT_EQ_ID = 35
fld public final static int GT_ID = 19
fld public final static int HEX_LITERAL_ID = 13
fld public final static int IDENTIFIER_ID = 6
fld public final static int IF_ID = 90
fld public final static int IMPLEMENTS_ID = 91
fld public final static int IMPORT_ID = 92
fld public final static int INCOMPLETE_CHAR_LITERAL_ID = 117
fld public final static int INCOMPLETE_HEX_LITERAL_ID = 118
fld public final static int INCOMPLETE_STRING_LITERAL_ID = 116
fld public final static int INSTANCEOF_ID = 93
fld public final static int INTERFACE_ID = 94
fld public final static int INT_ID = 68
fld public final static int INT_LITERAL_ID = 11
fld public final static int INVALID_CHAR_ID = 119
fld public final static int INVALID_COMMENT_END_ID = 122
fld public final static int INVALID_OCTAL_LITERAL_ID = 121
fld public final static int INVALID_OPERATOR_ID = 120
fld public final static int KEYWORDS_ID = 1
fld public final static int LBRACE_ID = 57
fld public final static int LBRACKET_ID = 55
fld public final static int LINE_COMMENT_ID = 7
fld public final static int LONG_ID = 69
fld public final static int LONG_LITERAL_ID = 12
fld public final static int LPAREN_ID = 53
fld public final static int LSHIFT_EQ_ID = 36
fld public final static int LSHIFT_ID = 20
fld public final static int LT_EQ_ID = 34
fld public final static int LT_ID = 18
fld public final static int MINUS_EQ_ID = 40
fld public final static int MINUS_ID = 24
fld public final static int MINUS_MINUS_ID = 60
fld public final static int MOD_EQ_ID = 46
fld public final static int MOD_ID = 30
fld public final static int MUL_EQ_ID = 41
fld public final static int MUL_ID = 25
fld public final static int NATIVE_ID = 95
fld public final static int NEG_ID = 32
fld public final static int NEW_ID = 96
fld public final static int NOT_EQ_ID = 47
fld public final static int NOT_ID = 31
fld public final static int NULL_ID = 97
fld public final static int NUMERIC_LITERALS_ID = 3
fld public final static int OCTAL_LITERAL_ID = 14
fld public final static int OPERATORS_ID = 2
fld public final static int OR_EQ_ID = 44
fld public final static int OR_ID = 28
fld public final static int OR_OR_ID = 62
fld public final static int PACKAGE_ID = 98
fld public final static int PLUS_EQ_ID = 39
fld public final static int PLUS_ID = 23
fld public final static int PLUS_PLUS_ID = 59
fld public final static int PRIVATE_ID = 99
fld public final static int PROTECTED_ID = 100
fld public final static int PUBLIC_ID = 101
fld public final static int QUESTION_ID = 52
fld public final static int RBRACE_ID = 58
fld public final static int RBRACKET_ID = 56
fld public final static int RETURN_ID = 102
fld public final static int RPAREN_ID = 54
fld public final static int RSSHIFT_EQ_ID = 37
fld public final static int RSSHIFT_ID = 21
fld public final static int RUSHIFT_EQ_ID = 38
fld public final static int RUSHIFT_ID = 22
fld public final static int SEMICOLON_ID = 51
fld public final static int SHORT_ID = 70
fld public final static int STATIC_ID = 103
fld public final static int STRICTFP_ID = 104
fld public final static int STRING_LITERAL_ID = 10
fld public final static int SUPER_ID = 105
fld public final static int SWITCH_ID = 106
fld public final static int SYNCHRONIZED_ID = 107
fld public final static int THIS_ID = 108
fld public final static int THROWS_ID = 110
fld public final static int THROW_ID = 109
fld public final static int TRANSIENT_ID = 111
fld public final static int TRUE_ID = 112
fld public final static int TRY_ID = 113
fld public final static int VOID_ID = 71
fld public final static int VOLATILE_ID = 114
fld public final static int WHILE_ID = 115
fld public final static int WHITESPACE_ID = 5
fld public final static int XOR_EQ_ID = 45
fld public final static int XOR_ID = 29
fld public final static org.netbeans.editor.BaseImageTokenID ABSTRACT
fld public final static org.netbeans.editor.BaseImageTokenID AND
fld public final static org.netbeans.editor.BaseImageTokenID AND_AND
fld public final static org.netbeans.editor.BaseImageTokenID AND_EQ
fld public final static org.netbeans.editor.BaseImageTokenID ASSERT
fld public final static org.netbeans.editor.BaseImageTokenID BOOLEAN
fld public final static org.netbeans.editor.BaseImageTokenID BREAK
fld public final static org.netbeans.editor.BaseImageTokenID BYTE
fld public final static org.netbeans.editor.BaseImageTokenID CASE
fld public final static org.netbeans.editor.BaseImageTokenID CATCH
fld public final static org.netbeans.editor.BaseImageTokenID CHAR
fld public final static org.netbeans.editor.BaseImageTokenID CLASS
fld public final static org.netbeans.editor.BaseImageTokenID COLON
fld public final static org.netbeans.editor.BaseImageTokenID COMMA
fld public final static org.netbeans.editor.BaseImageTokenID CONST
fld public final static org.netbeans.editor.BaseImageTokenID CONTINUE
fld public final static org.netbeans.editor.BaseImageTokenID DEFAULT
fld public final static org.netbeans.editor.BaseImageTokenID DIV
fld public final static org.netbeans.editor.BaseImageTokenID DIV_EQ
fld public final static org.netbeans.editor.BaseImageTokenID DO
fld public final static org.netbeans.editor.BaseImageTokenID DOT
fld public final static org.netbeans.editor.BaseImageTokenID DOUBLE
fld public final static org.netbeans.editor.BaseImageTokenID ELLIPSIS
fld public final static org.netbeans.editor.BaseImageTokenID ELSE
fld public final static org.netbeans.editor.BaseImageTokenID ENUM
fld public final static org.netbeans.editor.BaseImageTokenID EQ
fld public final static org.netbeans.editor.BaseImageTokenID EQ_EQ
fld public final static org.netbeans.editor.BaseImageTokenID EXTENDS
fld public final static org.netbeans.editor.BaseImageTokenID FALSE
fld public final static org.netbeans.editor.BaseImageTokenID FINAL
fld public final static org.netbeans.editor.BaseImageTokenID FINALLY
fld public final static org.netbeans.editor.BaseImageTokenID FLOAT
fld public final static org.netbeans.editor.BaseImageTokenID FOR
fld public final static org.netbeans.editor.BaseImageTokenID GOTO
fld public final static org.netbeans.editor.BaseImageTokenID GT
fld public final static org.netbeans.editor.BaseImageTokenID GT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID IF
fld public final static org.netbeans.editor.BaseImageTokenID IMPLEMENTS
fld public final static org.netbeans.editor.BaseImageTokenID IMPORT
fld public final static org.netbeans.editor.BaseImageTokenID INSTANCEOF
fld public final static org.netbeans.editor.BaseImageTokenID INT
fld public final static org.netbeans.editor.BaseImageTokenID INTERFACE
fld public final static org.netbeans.editor.BaseImageTokenID LBRACE
fld public final static org.netbeans.editor.BaseImageTokenID LBRACKET
fld public final static org.netbeans.editor.BaseImageTokenID LONG
fld public final static org.netbeans.editor.BaseImageTokenID LPAREN
fld public final static org.netbeans.editor.BaseImageTokenID LSHIFT
fld public final static org.netbeans.editor.BaseImageTokenID LSHIFT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID LT
fld public final static org.netbeans.editor.BaseImageTokenID LT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID MINUS
fld public final static org.netbeans.editor.BaseImageTokenID MINUS_EQ
fld public final static org.netbeans.editor.BaseImageTokenID MINUS_MINUS
fld public final static org.netbeans.editor.BaseImageTokenID MOD
fld public final static org.netbeans.editor.BaseImageTokenID MOD_EQ
fld public final static org.netbeans.editor.BaseImageTokenID MUL
fld public final static org.netbeans.editor.BaseImageTokenID MUL_EQ
fld public final static org.netbeans.editor.BaseImageTokenID NATIVE
fld public final static org.netbeans.editor.BaseImageTokenID NEG
fld public final static org.netbeans.editor.BaseImageTokenID NEW
fld public final static org.netbeans.editor.BaseImageTokenID NOT
fld public final static org.netbeans.editor.BaseImageTokenID NOT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID NULL
fld public final static org.netbeans.editor.BaseImageTokenID OR
fld public final static org.netbeans.editor.BaseImageTokenID OR_EQ
fld public final static org.netbeans.editor.BaseImageTokenID OR_OR
fld public final static org.netbeans.editor.BaseImageTokenID PACKAGE
fld public final static org.netbeans.editor.BaseImageTokenID PLUS
fld public final static org.netbeans.editor.BaseImageTokenID PLUS_EQ
fld public final static org.netbeans.editor.BaseImageTokenID PLUS_PLUS
fld public final static org.netbeans.editor.BaseImageTokenID PRIVATE
fld public final static org.netbeans.editor.BaseImageTokenID PROTECTED
fld public final static org.netbeans.editor.BaseImageTokenID PUBLIC
fld public final static org.netbeans.editor.BaseImageTokenID QUESTION
fld public final static org.netbeans.editor.BaseImageTokenID RBRACE
fld public final static org.netbeans.editor.BaseImageTokenID RBRACKET
fld public final static org.netbeans.editor.BaseImageTokenID RETURN
fld public final static org.netbeans.editor.BaseImageTokenID RPAREN
fld public final static org.netbeans.editor.BaseImageTokenID RSSHIFT
fld public final static org.netbeans.editor.BaseImageTokenID RSSHIFT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID RUSHIFT
fld public final static org.netbeans.editor.BaseImageTokenID RUSHIFT_EQ
fld public final static org.netbeans.editor.BaseImageTokenID SEMICOLON
fld public final static org.netbeans.editor.BaseImageTokenID SHORT
fld public final static org.netbeans.editor.BaseImageTokenID STATIC
fld public final static org.netbeans.editor.BaseImageTokenID STRICTFP
fld public final static org.netbeans.editor.BaseImageTokenID SUPER
fld public final static org.netbeans.editor.BaseImageTokenID SWITCH
fld public final static org.netbeans.editor.BaseImageTokenID SYNCHRONIZED
fld public final static org.netbeans.editor.BaseImageTokenID THIS
fld public final static org.netbeans.editor.BaseImageTokenID THROW
fld public final static org.netbeans.editor.BaseImageTokenID THROWS
fld public final static org.netbeans.editor.BaseImageTokenID TRANSIENT
fld public final static org.netbeans.editor.BaseImageTokenID TRUE
fld public final static org.netbeans.editor.BaseImageTokenID TRY
fld public final static org.netbeans.editor.BaseImageTokenID VOID
fld public final static org.netbeans.editor.BaseImageTokenID VOLATILE
fld public final static org.netbeans.editor.BaseImageTokenID WHILE
fld public final static org.netbeans.editor.BaseImageTokenID XOR
fld public final static org.netbeans.editor.BaseImageTokenID XOR_EQ
fld public final static org.netbeans.editor.BaseTokenCategory ERRORS
fld public final static org.netbeans.editor.BaseTokenCategory KEYWORDS
fld public final static org.netbeans.editor.BaseTokenCategory NUMERIC_LITERALS
fld public final static org.netbeans.editor.BaseTokenCategory OPERATORS
fld public final static org.netbeans.editor.BaseTokenID ANNOTATION
fld public final static org.netbeans.editor.BaseTokenID BLOCK_COMMENT
fld public final static org.netbeans.editor.BaseTokenID CHAR_LITERAL
fld public final static org.netbeans.editor.BaseTokenID DOUBLE_LITERAL
fld public final static org.netbeans.editor.BaseTokenID FLOAT_LITERAL
fld public final static org.netbeans.editor.BaseTokenID HEX_LITERAL
fld public final static org.netbeans.editor.BaseTokenID IDENTIFIER
fld public final static org.netbeans.editor.BaseTokenID INCOMPLETE_CHAR_LITERAL
fld public final static org.netbeans.editor.BaseTokenID INCOMPLETE_HEX_LITERAL
fld public final static org.netbeans.editor.BaseTokenID INCOMPLETE_STRING_LITERAL
fld public final static org.netbeans.editor.BaseTokenID INT_LITERAL
fld public final static org.netbeans.editor.BaseTokenID INVALID_CHAR
fld public final static org.netbeans.editor.BaseTokenID INVALID_COMMENT_END
fld public final static org.netbeans.editor.BaseTokenID INVALID_OCTAL_LITERAL
fld public final static org.netbeans.editor.BaseTokenID INVALID_OPERATOR
fld public final static org.netbeans.editor.BaseTokenID LINE_COMMENT
fld public final static org.netbeans.editor.BaseTokenID LONG_LITERAL
fld public final static org.netbeans.editor.BaseTokenID OCTAL_LITERAL
fld public final static org.netbeans.editor.BaseTokenID STRING_LITERAL
fld public final static org.netbeans.editor.BaseTokenID WHITESPACE
fld public final static org.netbeans.editor.TokenContextPath contextPath
fld public final static org.netbeans.editor.ext.java.JavaTokenContext context
meth public static boolean isType(java.lang.String)
meth public static boolean isType(org.netbeans.editor.TokenID)
meth public static boolean isTypeOrVoid(java.lang.String)
meth public static boolean isTypeOrVoid(org.netbeans.editor.TokenID)
meth public static org.netbeans.editor.TokenID getKeyword(java.lang.String)
supr org.netbeans.editor.TokenContext
hfds str2kwd

CLSS public abstract interface org.netbeans.spi.editor.fold.FoldManager
meth public abstract void changedUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void expandNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void init(org.netbeans.spi.editor.fold.FoldOperation)
meth public abstract void initFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void insertUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)
meth public abstract void release()
meth public abstract void removeDamagedNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeEmptyNotify(org.netbeans.api.editor.fold.Fold)
meth public abstract void removeUpdate(javax.swing.event.DocumentEvent,org.netbeans.spi.editor.fold.FoldHierarchyTransaction)

