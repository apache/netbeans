#Signature file v4.1
#Version 1.43

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.modules.web.indent.api.LexUtilities
meth public static <%0 extends org.netbeans.api.lexer.TokenId> java.util.List<org.netbeans.api.lexer.TokenSequence<{%%0}>> getEmbeddedTokenSequences(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.Language<{%%0}>,int,int)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$CodeBlock<{%%0}>> createCodeBlocks(org.netbeans.editor.BaseDocument,org.netbeans.api.lexer.Language<{%%0}>,org.netbeans.modules.web.indent.api.embedding.VirtualSource) throws javax.swing.text.BadLocationException
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.Token<{%%0}> findNext(org.netbeans.api.lexer.TokenSequence<{%%0}>,java.util.List<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.Token<{%%0}> findNext(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{%%0}>,java.util.List<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.Token<{%%0}> findPrevious(org.netbeans.api.lexer.TokenSequence<{%%0}>,java.util.List<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.Token<{%%0}> findPrevious(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{%%0}>,java.util.List<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.Token<{%%0}> getTokenAtOffset(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{%%0}>,int)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> getPositionedSequence(org.netbeans.editor.BaseDocument,int,boolean,org.netbeans.api.lexer.Language<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> getPositionedSequence(org.netbeans.editor.BaseDocument,int,org.netbeans.api.lexer.Language<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> getTokenSequence(org.netbeans.api.lexer.TokenHierarchy<javax.swing.text.Document>,int,org.netbeans.api.lexer.Language<{%%0}>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.api.lexer.TokenSequence<{%%0}> getTokenSequence(org.netbeans.editor.BaseDocument,int,org.netbeans.api.lexer.Language<{%%0}>)
meth public static int getTokenSequenceEndOffset(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.api.lexer.TokenId>)
meth public static int getTokenSequenceEndOffset(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<? extends org.netbeans.api.lexer.TokenId>)
meth public static int getTokenSequenceStartOffset(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.api.lexer.TokenId>)
meth public static int getTokenSequenceStartOffset(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<? extends org.netbeans.api.lexer.TokenId>)
meth public static org.netbeans.api.lexer.Language<? extends org.netbeans.api.lexer.TokenId> getLanguage(org.netbeans.api.lexer.TokenHierarchy<org.netbeans.editor.BaseDocument>,int)
meth public static org.netbeans.api.lexer.Language<? extends org.netbeans.api.lexer.TokenId> getLanguage(org.netbeans.editor.BaseDocument,int)
supr java.lang.Object

CLSS public final org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<%0 extends org.netbeans.api.lexer.TokenId>
innr public final static CodeBlock
innr public final static TokenSequenceWrapper
meth public boolean isCurrentTokenSequenceVirtual()
meth public boolean move(int,boolean)
meth public boolean moveNext()
meth public boolean movePrevious()
meth public int move(int)
meth public int offset()
meth public int[] index()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence%0}>> getContextDataTokenSequences()
meth public org.netbeans.api.lexer.Language<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence%0}> language()
meth public org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence%0}> token()
meth public org.netbeans.api.lexer.TokenSequence<?> embedded()
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence%0}> currentTokenSequence()
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{%%0}> createFromCodeBlocks(java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$CodeBlock<{%%0}>>)
meth public static <%0 extends org.netbeans.api.lexer.TokenId> org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{%%0}> createFromTokenSequenceWrappers(java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper<{%%0}>>)
meth public void moveEnd()
meth public void moveIndex(int[])
meth public void moveStart()
supr java.lang.Object
hfds currentTSW,currentTokenSequence,tss

CLSS public final static org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$CodeBlock<%0 extends org.netbeans.api.lexer.TokenId>
 outer org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence
cons public init(java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$CodeBlock%0}>>)
fld public java.util.List<org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$CodeBlock%0}>> tss
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper<%0 extends org.netbeans.api.lexer.TokenId>
 outer org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence
cons public init(org.netbeans.api.lexer.TokenSequence<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper%0}>,boolean)
meth public boolean isVirtual()
meth public int getEnd()
meth public int getStart()
meth public java.lang.String toString()
meth public org.netbeans.api.lexer.TokenSequence<{org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence$TokenSequenceWrapper%0}> getTokenSequence()
supr java.lang.Object
hfds end,start,ts,virtual

CLSS public abstract interface org.netbeans.modules.web.indent.api.embedding.VirtualSource
innr public abstract interface static Factory
meth public abstract java.lang.String getSource(int,int)

CLSS public abstract interface static org.netbeans.modules.web.indent.api.embedding.VirtualSource$Factory
 outer org.netbeans.modules.web.indent.api.embedding.VirtualSource
meth public abstract org.netbeans.modules.web.indent.api.embedding.VirtualSource createVirtualSource(javax.swing.text.Document,java.lang.String)

CLSS public abstract org.netbeans.modules.web.indent.api.support.AbstractIndenter<%0 extends org.netbeans.api.lexer.TokenId>
cons public init(org.netbeans.api.lexer.Language<{org.netbeans.modules.web.indent.api.support.AbstractIndenter%0}>,org.netbeans.modules.editor.indent.spi.Context)
fld protected final static boolean DEBUG
fld protected final static boolean DEBUG_PERFORMANCE
fld public static boolean inUnitTestRun
innr public final static OffsetRanges
meth protected abstract boolean isWhiteSpaceToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.AbstractIndenter%0}>)
meth protected abstract int getFormatStableStart(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.AbstractIndenter%0}>,int,int,org.netbeans.modules.web.indent.api.support.AbstractIndenter$OffsetRanges) throws javax.swing.text.BadLocationException
meth protected abstract java.util.List<org.netbeans.modules.web.indent.api.support.IndentCommand> getLineIndent(org.netbeans.modules.web.indent.api.support.IndenterContextData<{org.netbeans.modules.web.indent.api.support.AbstractIndenter%0}>,java.util.List<org.netbeans.modules.web.indent.api.support.IndentCommand>) throws javax.swing.text.BadLocationException
meth protected abstract void reset()
meth protected final int getIndentationSize()
meth protected final org.netbeans.api.lexer.Language<{org.netbeans.modules.web.indent.api.support.AbstractIndenter%0}> getLanguage()
meth protected final org.netbeans.editor.BaseDocument getDocument()
meth protected final org.netbeans.modules.editor.indent.spi.Context getContext()
meth public final org.netbeans.modules.web.indent.api.support.IndenterFormattingContext createFormattingContext()
meth public final void beforeReindent(java.util.Collection<? extends org.netbeans.modules.web.indent.api.support.IndenterFormattingContext>)
meth public final void reindent()
supr java.lang.Object
hfds LOG,MAX_INDENT,context,formattingContext,indentationSize,language,startTime1,startTimeTotal,used
hcls ForeignLanguageBlock,Line,LineCommandsPair,LinePair,OffsetRange,UnmodifiableButExtendableList

CLSS public final static org.netbeans.modules.web.indent.api.support.AbstractIndenter$OffsetRanges
 outer org.netbeans.modules.web.indent.api.support.AbstractIndenter
cons public init()
meth public boolean isEmpty()
meth public java.lang.String dump()
meth public java.lang.String toString()
meth public void add(int,int)
supr java.lang.Object
hfds ranges

CLSS public final org.netbeans.modules.web.indent.api.support.IndentCommand
cons public init(org.netbeans.modules.web.indent.api.support.IndentCommand$Type,int)
cons public init(org.netbeans.modules.web.indent.api.support.IndentCommand$Type,int,int)
innr public final static !enum Type
meth public int getFixedIndentSize()
meth public int getIndentationSize()
meth public int getLineOffset()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.indent.api.support.IndentCommand$Type getType()
meth public void setFixedIndentSize(int)
supr java.lang.Object
hfds fixedIndentSize,indentation,indentationSize,lineOffset,type,wasContinue

CLSS public final static !enum org.netbeans.modules.web.indent.api.support.IndentCommand$Type
 outer org.netbeans.modules.web.indent.api.support.IndentCommand
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type BLOCK_END
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type BLOCK_START
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type CONTINUE
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type DO_NOT_INDENT_THIS_LINE
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type INDENT
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type NO_CHANGE
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type PRESERVE_INDENTATION
fld public final static org.netbeans.modules.web.indent.api.support.IndentCommand$Type RETURN
meth public static org.netbeans.modules.web.indent.api.support.IndentCommand$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.web.indent.api.support.IndentCommand$Type[] values()
supr java.lang.Enum<org.netbeans.modules.web.indent.api.support.IndentCommand$Type>

CLSS public final org.netbeans.modules.web.indent.api.support.IndenterContextData<%0 extends org.netbeans.api.lexer.TokenId>
cons public init(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.IndenterContextData%0}>,int,int,int,int,boolean,boolean)
meth public boolean isBlankLine()
meth public boolean isIndentThisLine()
meth public boolean isLanguageBlockEnd()
meth public boolean isLanguageBlockStart()
meth public int getLineEndOffset()
meth public int getLineNonWhiteStartOffset()
meth public int getLineStartOffset()
meth public int getNextLineStartOffset()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.IndenterContextData%0}> getJoinedTokenSequences()
supr java.lang.Object
hfds blankLine,indentThisLine,joinedTS,languageBlockEnd,languageBlockStart,lineEndOffset,lineNonWhiteStartOffset,lineStartOffset,nextLineStartOffset

CLSS public final org.netbeans.modules.web.indent.api.support.IndenterFormattingContext
cons public init(org.netbeans.editor.BaseDocument)
meth public boolean isFirstIndenter()
meth public boolean isLastIndenter()
supr java.lang.Object
hfds changes,delegate,doc,firstIndenter,indentedLines,initialized,lastIndenter,listener
hcls Change

CLSS public abstract org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter<%0 extends org.netbeans.api.lexer.TokenId>
cons public init(org.netbeans.api.lexer.Language<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,org.netbeans.modules.editor.indent.spi.Context)
meth protected abstract boolean isBlockCommentToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isCloseTagNameToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isClosingTagOptional(java.lang.CharSequence)
meth protected abstract boolean isEndTagClosingSymbol(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isEndTagSymbol(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isForeignLanguageEndToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isForeignLanguageStartToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isOpenTagNameToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isOpeningTagOptional(java.lang.CharSequence)
meth protected abstract boolean isPreservedLine(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,org.netbeans.modules.web.indent.api.support.IndenterContextData<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isStartTagClosingSymbol(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isStartTagSymbol(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isTagArgumentToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isTagContentToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected abstract boolean isTagContentUnformattable(java.lang.CharSequence)
meth protected abstract int getPreservedLineInitialIndentation(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>) throws javax.swing.text.BadLocationException
meth protected abstract java.lang.Boolean isEmptyTag(java.lang.CharSequence)
meth protected abstract java.util.Set<java.lang.String> getTagChildren(java.lang.CharSequence)
meth protected boolean isStableFormattingStartToken(org.netbeans.api.lexer.Token<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>)
meth protected int getFormatStableStart(org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,int,int,org.netbeans.modules.web.indent.api.support.AbstractIndenter$OffsetRanges) throws javax.swing.text.BadLocationException
meth protected java.util.List<org.netbeans.modules.web.indent.api.support.IndentCommand> getLineIndent(org.netbeans.modules.web.indent.api.support.IndenterContextData<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>,java.util.List<org.netbeans.modules.web.indent.api.support.IndentCommand>) throws javax.swing.text.BadLocationException
meth protected void reset()
supr org.netbeans.modules.web.indent.api.support.AbstractIndenter<{org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter%0}>
hfds attributesIndent,eliminatedTags,firstPreservedLineIndent,inOpeningTagAttributes,inUnformattableTagContent,stack,unformattableTagName
hcls EliminatedTag,MarkupItem

