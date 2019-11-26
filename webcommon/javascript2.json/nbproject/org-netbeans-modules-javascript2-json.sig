#Signature file v4.1
#Version 1.9

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

CLSS public abstract org.antlr.v4.runtime.Lexer
cons public init()
cons public init(org.antlr.v4.runtime.CharStream)
fld protected org.antlr.v4.runtime.TokenFactory<?> _factory
fld protected org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream> _tokenFactorySourcePair
fld public boolean _hitEOF
fld public final org.antlr.v4.runtime.misc.IntegerStack _modeStack
fld public final static int DEFAULT_MODE = 0
fld public final static int DEFAULT_TOKEN_CHANNEL = 0
fld public final static int HIDDEN = 1
fld public final static int MAX_CHAR_VALUE = 65534
fld public final static int MIN_CHAR_VALUE = 0
fld public final static int MORE = -2
fld public final static int SKIP = -3
fld public int _channel
fld public int _mode
fld public int _tokenStartCharIndex
fld public int _tokenStartCharPositionInLine
fld public int _tokenStartLine
fld public int _type
fld public java.lang.String _text
fld public org.antlr.v4.runtime.CharStream _input
fld public org.antlr.v4.runtime.Token _token
intf org.antlr.v4.runtime.TokenSource
meth public int getChannel()
meth public int getCharIndex()
meth public int getCharPositionInLine()
meth public int getLine()
meth public int getType()
meth public int popMode()
meth public java.lang.String getCharErrorDisplay(int)
meth public java.lang.String getErrorDisplay(int)
meth public java.lang.String getErrorDisplay(java.lang.String)
meth public java.lang.String getSourceName()
meth public java.lang.String getText()
meth public java.lang.String[] getModeNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public java.util.List<? extends org.antlr.v4.runtime.Token> getAllTokens()
meth public org.antlr.v4.runtime.CharStream getInputStream()
meth public org.antlr.v4.runtime.Token emit()
meth public org.antlr.v4.runtime.Token emitEOF()
meth public org.antlr.v4.runtime.Token getToken()
meth public org.antlr.v4.runtime.Token nextToken()
meth public org.antlr.v4.runtime.TokenFactory<? extends org.antlr.v4.runtime.Token> getTokenFactory()
meth public void emit(org.antlr.v4.runtime.Token)
meth public void mode(int)
meth public void more()
meth public void notifyListeners(org.antlr.v4.runtime.LexerNoViableAltException)
meth public void pushMode(int)
meth public void recover(org.antlr.v4.runtime.LexerNoViableAltException)
meth public void recover(org.antlr.v4.runtime.RecognitionException)
meth public void reset()
meth public void setChannel(int)
meth public void setCharPositionInLine(int)
meth public void setInputStream(org.antlr.v4.runtime.IntStream)
meth public void setLine(int)
meth public void setText(java.lang.String)
meth public void setToken(org.antlr.v4.runtime.Token)
meth public void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public void setType(int)
meth public void skip()
supr org.antlr.v4.runtime.Recognizer<java.lang.Integer,org.antlr.v4.runtime.atn.LexerATNSimulator>

CLSS public abstract org.antlr.v4.runtime.Parser
cons public init(org.antlr.v4.runtime.TokenStream)
fld protected boolean _buildParseTrees
fld protected boolean matchedEOF
fld protected final org.antlr.v4.runtime.misc.IntegerStack _precedenceStack
fld protected int _syntaxErrors
fld protected java.util.List<org.antlr.v4.runtime.tree.ParseTreeListener> _parseListeners
fld protected org.antlr.v4.runtime.ANTLRErrorStrategy _errHandler
fld protected org.antlr.v4.runtime.ParserRuleContext _ctx
fld protected org.antlr.v4.runtime.TokenStream _input
innr public TraceListener
innr public static TrimToSizeListener
meth protected void addContextToParseTree()
meth protected void triggerEnterRuleEvent()
meth protected void triggerExitRuleEvent()
meth public boolean getBuildParseTree()
meth public boolean getTrimParseTree()
meth public boolean inContext(java.lang.String)
meth public boolean isExpectedToken(int)
meth public boolean isMatchedEOF()
meth public boolean isTrace()
meth public boolean precpred(org.antlr.v4.runtime.RuleContext,int)
meth public final int getPrecedence()
meth public final void notifyErrorListeners(java.lang.String)
meth public final void setInputStream(org.antlr.v4.runtime.IntStream)
meth public int getNumberOfSyntaxErrors()
meth public int getRuleIndex(java.lang.String)
meth public java.lang.String getSourceName()
meth public java.util.List<java.lang.String> getDFAStrings()
meth public java.util.List<java.lang.String> getRuleInvocationStack()
meth public java.util.List<java.lang.String> getRuleInvocationStack(org.antlr.v4.runtime.RuleContext)
meth public java.util.List<org.antlr.v4.runtime.tree.ParseTreeListener> getParseListeners()
meth public org.antlr.v4.runtime.ANTLRErrorStrategy getErrorHandler()
meth public org.antlr.v4.runtime.ParserRuleContext getContext()
meth public org.antlr.v4.runtime.ParserRuleContext getInvokingContext(int)
meth public org.antlr.v4.runtime.ParserRuleContext getRuleContext()
meth public org.antlr.v4.runtime.Token consume()
meth public org.antlr.v4.runtime.Token getCurrentToken()
meth public org.antlr.v4.runtime.Token match(int)
meth public org.antlr.v4.runtime.Token matchWildcard()
meth public org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public org.antlr.v4.runtime.TokenStream getInputStream()
meth public org.antlr.v4.runtime.TokenStream getTokenStream()
meth public org.antlr.v4.runtime.atn.ATN getATNWithBypassAlts()
meth public org.antlr.v4.runtime.atn.ParseInfo getParseInfo()
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokens()
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokensWithinCurrentRule()
meth public org.antlr.v4.runtime.tree.pattern.ParseTreePattern compileParseTreePattern(java.lang.String,int)
meth public org.antlr.v4.runtime.tree.pattern.ParseTreePattern compileParseTreePattern(java.lang.String,int,org.antlr.v4.runtime.Lexer)
meth public void addParseListener(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void dumpDFA()
meth public void enterOuterAlt(org.antlr.v4.runtime.ParserRuleContext,int)
meth public void enterRecursionRule(org.antlr.v4.runtime.ParserRuleContext,int)
 anno 0 java.lang.Deprecated()
meth public void enterRecursionRule(org.antlr.v4.runtime.ParserRuleContext,int,int,int)
meth public void enterRule(org.antlr.v4.runtime.ParserRuleContext,int,int)
meth public void exitRule()
meth public void notifyErrorListeners(org.antlr.v4.runtime.Token,java.lang.String,org.antlr.v4.runtime.RecognitionException)
meth public void pushNewRecursionContext(org.antlr.v4.runtime.ParserRuleContext,int,int)
meth public void removeParseListener(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void removeParseListeners()
meth public void reset()
meth public void setBuildParseTree(boolean)
meth public void setContext(org.antlr.v4.runtime.ParserRuleContext)
meth public void setErrorHandler(org.antlr.v4.runtime.ANTLRErrorStrategy)
meth public void setProfile(boolean)
meth public void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public void setTokenStream(org.antlr.v4.runtime.TokenStream)
meth public void setTrace(boolean)
meth public void setTrimParseTree(boolean)
meth public void unrollRecursionContexts(org.antlr.v4.runtime.ParserRuleContext)
supr org.antlr.v4.runtime.Recognizer<org.antlr.v4.runtime.Token,org.antlr.v4.runtime.atn.ParserATNSimulator>
hfds _tracer,bypassAltsAtnCache

CLSS public org.antlr.v4.runtime.ParserRuleContext
cons public init()
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
fld public java.util.List<org.antlr.v4.runtime.tree.ParseTree> children
fld public org.antlr.v4.runtime.RecognitionException exception
fld public org.antlr.v4.runtime.Token start
fld public org.antlr.v4.runtime.Token stop
meth public <%0 extends org.antlr.v4.runtime.ParserRuleContext> java.util.List<{%%0}> getRuleContexts(java.lang.Class<? extends {%%0}>)
meth public <%0 extends org.antlr.v4.runtime.ParserRuleContext> {%%0} getRuleContext(java.lang.Class<? extends {%%0}>,int)
meth public <%0 extends org.antlr.v4.runtime.tree.ParseTree> {%%0} getChild(java.lang.Class<? extends {%%0}>,int)
meth public int getChildCount()
meth public java.lang.String toInfoString(org.antlr.v4.runtime.Parser)
meth public java.util.List<org.antlr.v4.runtime.tree.TerminalNode> getTokens(int)
meth public org.antlr.v4.runtime.ParserRuleContext getParent()
meth public org.antlr.v4.runtime.RuleContext addChild(org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.Token getStart()
meth public org.antlr.v4.runtime.Token getStop()
meth public org.antlr.v4.runtime.misc.Interval getSourceInterval()
meth public org.antlr.v4.runtime.tree.ErrorNode addErrorNode(org.antlr.v4.runtime.Token)
meth public org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public org.antlr.v4.runtime.tree.TerminalNode addChild(org.antlr.v4.runtime.Token)
meth public org.antlr.v4.runtime.tree.TerminalNode addChild(org.antlr.v4.runtime.tree.TerminalNode)
meth public org.antlr.v4.runtime.tree.TerminalNode getToken(int,int)
meth public void copyFrom(org.antlr.v4.runtime.ParserRuleContext)
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void removeLastChild()
supr org.antlr.v4.runtime.RuleContext

CLSS public abstract org.antlr.v4.runtime.Recognizer<%0 extends java.lang.Object, %1 extends org.antlr.v4.runtime.atn.ATNSimulator>
cons public init()
fld protected {org.antlr.v4.runtime.Recognizer%1} _interp
fld public final static int EOF = -1
meth public abstract java.lang.String getGrammarFileName()
meth public abstract java.lang.String[] getRuleNames()
meth public abstract java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public abstract org.antlr.v4.runtime.IntStream getInputStream()
meth public abstract org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public abstract org.antlr.v4.runtime.atn.ATN getATN()
meth public abstract void setInputStream(org.antlr.v4.runtime.IntStream)
meth public abstract void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public boolean precpred(org.antlr.v4.runtime.RuleContext,int)
meth public boolean sempred(org.antlr.v4.runtime.RuleContext,int,int)
meth public final int getState()
meth public final void setState(int)
meth public int getTokenType(java.lang.String)
meth public java.lang.String getErrorHeader(org.antlr.v4.runtime.RecognitionException)
meth public java.lang.String getSerializedATN()
meth public java.lang.String getTokenErrorDisplay(org.antlr.v4.runtime.Token)
 anno 0 java.lang.Deprecated()
meth public java.util.List<? extends org.antlr.v4.runtime.ANTLRErrorListener> getErrorListeners()
meth public java.util.Map<java.lang.String,java.lang.Integer> getRuleIndexMap()
meth public java.util.Map<java.lang.String,java.lang.Integer> getTokenTypeMap()
meth public org.antlr.v4.runtime.ANTLRErrorListener getErrorListenerDispatch()
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ParseInfo getParseInfo()
meth public void action(org.antlr.v4.runtime.RuleContext,int,int)
meth public void addErrorListener(org.antlr.v4.runtime.ANTLRErrorListener)
meth public void removeErrorListener(org.antlr.v4.runtime.ANTLRErrorListener)
meth public void removeErrorListeners()
meth public void setInterpreter({org.antlr.v4.runtime.Recognizer%1})
meth public {org.antlr.v4.runtime.Recognizer%1} getInterpreter()
supr java.lang.Object
hfds _listeners,_stateNumber,ruleIndexMapCache,tokenTypeMapCache

CLSS public org.antlr.v4.runtime.RuleContext
cons public init()
cons public init(org.antlr.v4.runtime.RuleContext,int)
fld public final static org.antlr.v4.runtime.ParserRuleContext EMPTY
fld public int invokingState
fld public org.antlr.v4.runtime.RuleContext parent
intf org.antlr.v4.runtime.tree.RuleNode
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public boolean isEmpty()
meth public final java.lang.String toString(java.util.List<java.lang.String>)
meth public final java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>)
meth public int depth()
meth public int getAltNumber()
meth public int getChildCount()
meth public int getRuleIndex()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toString(java.util.List<java.lang.String>,org.antlr.v4.runtime.RuleContext)
meth public java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public java.lang.String toStringTree()
meth public java.lang.String toStringTree(java.util.List<java.lang.String>)
meth public java.lang.String toStringTree(org.antlr.v4.runtime.Parser)
meth public org.antlr.v4.runtime.RuleContext getParent()
meth public org.antlr.v4.runtime.RuleContext getPayload()
meth public org.antlr.v4.runtime.RuleContext getRuleContext()
meth public org.antlr.v4.runtime.misc.Interval getSourceInterval()
meth public org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public void setAltNumber(int)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.TokenSource
meth public abstract int getCharPositionInLine()
meth public abstract int getLine()
meth public abstract java.lang.String getSourceName()
meth public abstract org.antlr.v4.runtime.CharStream getInputStream()
meth public abstract org.antlr.v4.runtime.Token nextToken()
meth public abstract org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public abstract void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)

CLSS public abstract org.antlr.v4.runtime.tree.AbstractParseTreeVisitor<%0 extends java.lang.Object>
cons public init()
intf org.antlr.v4.runtime.tree.ParseTreeVisitor<{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0}>
meth protected boolean shouldVisitNextChild(org.antlr.v4.runtime.tree.RuleNode,{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0})
meth protected {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} aggregateResult({org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0},{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0})
meth protected {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} defaultResult()
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visit(org.antlr.v4.runtime.tree.ParseTree)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitChildren(org.antlr.v4.runtime.tree.RuleNode)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTree
intf org.antlr.v4.runtime.tree.SyntaxTree
meth public abstract <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public abstract java.lang.String getText()
meth public abstract java.lang.String toStringTree(org.antlr.v4.runtime.Parser)
meth public abstract org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public abstract org.antlr.v4.runtime.tree.ParseTree getParent()

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTreeListener
meth public abstract void enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public abstract void exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public abstract void visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public abstract void visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTreeVisitor<%0 extends java.lang.Object>
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visit(org.antlr.v4.runtime.tree.ParseTree)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitChildren(org.antlr.v4.runtime.tree.RuleNode)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)

CLSS public abstract interface org.antlr.v4.runtime.tree.RuleNode
intf org.antlr.v4.runtime.tree.ParseTree
meth public abstract org.antlr.v4.runtime.RuleContext getRuleContext()

CLSS public abstract interface org.antlr.v4.runtime.tree.SyntaxTree
intf org.antlr.v4.runtime.tree.Tree
meth public abstract org.antlr.v4.runtime.misc.Interval getSourceInterval()

CLSS public abstract interface org.antlr.v4.runtime.tree.Tree
meth public abstract int getChildCount()
meth public abstract java.lang.Object getPayload()
meth public abstract java.lang.String toStringTree()
meth public abstract org.antlr.v4.runtime.tree.Tree getChild(int)
meth public abstract org.antlr.v4.runtime.tree.Tree getParent()

CLSS public org.netbeans.modules.javascript2.json.api.JsonOptionsQuery
innr public final static Result
meth public static org.netbeans.modules.javascript2.json.api.JsonOptionsQuery$Result getOptions(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.modules.javascript2.json.api.JsonOptionsQuery$Result
 outer org.netbeans.modules.javascript2.json.api.JsonOptionsQuery
fld public final static java.lang.String PROP_COMMENT_SUPPORTED = "commentSupported"
meth public boolean isCommentSupported()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegates,listeners,listens,pcl

CLSS public org.netbeans.modules.javascript2.json.parser.JsonBaseListener
cons public init()
intf org.netbeans.modules.javascript2.json.parser.JsonListener
meth public void enterArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public void enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void enterJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public void enterKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public void enterObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public void enterPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public void enterValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)
meth public void exitArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public void exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void exitJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public void exitKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public void exitObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public void exitPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public void exitValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)
meth public void visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public void visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
supr java.lang.Object

CLSS public org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor<%0 extends java.lang.Object>
cons public init()
intf org.netbeans.modules.javascript2.json.parser.JsonVisitor<{org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0}>
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public {org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0} visitValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)
supr org.antlr.v4.runtime.tree.AbstractParseTreeVisitor<{org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor%0}>

CLSS public org.netbeans.modules.javascript2.json.parser.JsonLexer
cons public init(org.antlr.v4.runtime.CharStream)
cons public init(org.antlr.v4.runtime.CharStream,boolean)
cons public init(org.antlr.v4.runtime.CharStream,boolean,boolean)
fld protected final static org.antlr.v4.runtime.atn.PredictionContextCache _sharedContextCache
fld protected final static org.antlr.v4.runtime.dfa.DFA[] _decisionToDFA
fld public final static int COLON = 1
fld public final static int COMMA = 2
fld public final static int COMMENT = 16
fld public final static int COMMENTS = 2
fld public final static int DOT = 3
fld public final static int ERROR = 19
fld public final static int ERRORS = 3
fld public final static int ERROR_COMMENT = 18
fld public final static int FALSE = 11
fld public final static int LBRACE = 6
fld public final static int LBRACKET = 8
fld public final static int LINE_COMMENT = 15
fld public final static int MINUS = 5
fld public final static int NULL = 12
fld public final static int NUMBER = 13
fld public final static int PLUS = 4
fld public final static int RBRACE = 7
fld public final static int RBRACKET = 9
fld public final static int STRING = 14
fld public final static int TRUE = 10
fld public final static int WHITESPACES = 1
fld public final static int WS = 17
fld public final static java.lang.String _serializedATN = "\u0003\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\u0002\u0015\u00e9\u0008\u0001\u0004\u0002\u0009\u0002\u0004\u0003\u0009\u0003\u0004\u0004\u0009\u0004\u0004\u0005\u0009\u0005\u0004\u0006\u0009\u0006\u0004\u0007\u0009\u0007\u0004\u0008\u0009\u0008\u0004\u0009\u0009\u0009\u0004\n\u0009\n\u0004\u000b\u0009\u000b\u0004\u000c\u0009\u000c\u0004\r\u0009\r\u0004\u000e\u0009\u000e\u0004\u000f\u0009\u000f\u0004\u0010\u0009\u0010\u0004\u0011\u0009\u0011\u0004\u0012\u0009\u0012\u0004\u0013\u0009\u0013\u0004\u0014\u0009\u0014\u0004\u0015\u0009\u0015\u0004\u0016\u0009\u0016\u0004\u0017\u0009\u0017\u0004\u0018\u0009\u0018\u0004\u0019\u0009\u0019\u0004\u001a\u0009\u001a\u0004\u001b\u0009\u001b\u0004\u001c\u0009\u001c\u0004\u001d\u0009\u001d\u0004\u001e\u0009\u001e\u0004\u001f\u0009\u001f\u0003\u0002\u0003\u0002\u0003\u0003\u0003\u0003\u0003\u0004\u0003\u0004\u0003\u0005\u0003\u0005\u0003\u0006\u0003\u0006\u0003\u0007\u0003\u0007\u0003\u0008\u0003\u0008\u0003\u0009\u0003\u0009\u0003\n\u0003\n\u0003\u000b\u0003\u000b\u0003\u000b\u0003\u000b\u0003\u000b\u0003\u000c\u0003\u000c\u0003\u000c\u0003\u000c\u0003\u000c\u0003\u000c\u0003\r\u0003\r\u0003\r\u0003\r\u0003\r\u0003\u000e\u0003\u000e\u0005\u000ed\n\u000e\u0003\u000e\u0005\u000eg\n\u000e\u0003\u000f\u0005\u000fj\n\u000f\u0003\u000f\u0003\u000f\u0003\u000f\u0007\u000fo\n\u000f\u000c\u000f\u000e\u000fr\u000b\u000f\u0005\u000ft\n\u000f\u0003\u0010\u0003\u0010\u0003\u0011\u0003\u0011\u0003\u0012\u0003\u0012\u0005\u0012|\n\u0012\u0003\u0013\u0003\u0013\u0006\u0013\u0080\n\u0013\r\u0013\u000e\u0013\u0081\u0003\u0014\u0003\u0014\u0003\u0014\u0005\u0014\u0087\n\u0014\u0003\u0014\u0006\u0014\u008a\n\u0014\r\u0014\u000e\u0014\u008b\u0003\u0015\u0003\u0015\u0007\u0015\u0090\n\u0015\u000c\u0015\u000e\u0015\u0093\u000b\u0015\u0003\u0015\u0003\u0015\u0003\u0016\u0003\u0016\u0003\u0017\u0003\u0017\u0005\u0017\u009b\n\u0017\u0003\u0018\u0003\u0018\u0003\u0018\u0005\u0018\u00a0\n\u0018\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u001a\u0003\u001a\u0003\u001b\u0003\u001b\u0003\u001b\u0003\u001b\u0007\u001b\u00ae\n\u001b\u000c\u001b\u000e\u001b\u00b1\u000b\u001b\u0003\u001b\u0005\u001b\u00b4\n\u001b\u0003\u001b\u0003\u001b\u0003\u001b\u0003\u001b\u0003\u001b\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0007\u001c\u00bf\n\u001c\u000c\u001c\u000e\u001c\u00c2\u000b\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001c\u0003\u001d\u0006\u001d\u00cc\n\u001d\r\u001d\u000e\u001d\u00cd\u0003\u001d\u0003\u001d\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001e\u0006\u001e\u00d7\n\u001e\r\u001e\u000e\u001e\u00d8\u0003\u001e\u0007\u001e\u00dc\n\u001e\u000c\u001e\u000e\u001e\u00df\u000b\u001e\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001f\u0003\u001f\u0003\u001f\u0003\u001f\u0003\u001f\u0004\u00af\u00c0\u0002 \u0003\u0003\u0005\u0004\u0007\u0005\u0009\u0006\u000b\u0007\r\u0008\u000f\u0009\u0011\n\u0013\u000b\u0015\u000c\u0017\r\u0019\u000e\u001b\u000f\u001d\u0002\u001f\u0002!\u0002#\u0002%\u0002'\u0002)\u0010+\u0002-\u0002/\u00021\u00023\u00025\u00117\u00129\u0013;\u0014=\u0015\u0003\u0002\n\u0003\u00023;\u0004\u0002GGgg\u0005\u0002\u0002!$$^^\n\u0002$$11^^ddhhppttvv\u0005\u00022;CHch\u0005\u0002\u000b\u000c\u000f\u000f\u0022\u0022\u0003\u0002,,\u0003\u000211\u00f1\u0002\u0003\u0003\u0002\u0002\u0002\u0002\u0005\u0003\u0002\u0002\u0002\u0002\u0007\u0003\u0002\u0002\u0002\u0002\u0009\u0003\u0002\u0002\u0002\u0002\u000b\u0003\u0002\u0002\u0002\u0002\r\u0003\u0002\u0002\u0002\u0002\u000f\u0003\u0002\u0002\u0002\u0002\u0011\u0003\u0002\u0002\u0002\u0002\u0013\u0003\u0002\u0002\u0002\u0002\u0015\u0003\u0002\u0002\u0002\u0002\u0017\u0003\u0002\u0002\u0002\u0002\u0019\u0003\u0002\u0002\u0002\u0002\u001b\u0003\u0002\u0002\u0002\u0002)\u0003\u0002\u0002\u0002\u00025\u0003\u0002\u0002\u0002\u00027\u0003\u0002\u0002\u0002\u00029\u0003\u0002\u0002\u0002\u0002;\u0003\u0002\u0002\u0002\u0002=\u0003\u0002\u0002\u0002\u0003?\u0003\u0002\u0002\u0002\u0005A\u0003\u0002\u0002\u0002\u0007C\u0003\u0002\u0002\u0002\u0009E\u0003\u0002\u0002\u0002\u000bG\u0003\u0002\u0002\u0002\rI\u0003\u0002\u0002\u0002\u000fK\u0003\u0002\u0002\u0002\u0011M\u0003\u0002\u0002\u0002\u0013O\u0003\u0002\u0002\u0002\u0015Q\u0003\u0002\u0002\u0002\u0017V\u0003\u0002\u0002\u0002\u0019\u005c\u0003\u0002\u0002\u0002\u001ba\u0003\u0002\u0002\u0002\u001di\u0003\u0002\u0002\u0002\u001fu\u0003\u0002\u0002\u0002!w\u0003\u0002\u0002\u0002#{\u0003\u0002\u0002\u0002%}\u0003\u0002\u0002\u0002'\u0083\u0003\u0002\u0002\u0002)\u008d\u0003\u0002\u0002\u0002+\u0096\u0003\u0002\u0002\u0002-\u009a\u0003\u0002\u0002\u0002/\u009c\u0003\u0002\u0002\u00021\u00a1\u0003\u0002\u0002\u00023\u00a7\u0003\u0002\u0002\u00025\u00a9\u0003\u0002\u0002\u00027\u00ba\u0003\u0002\u0002\u00029\u00cb\u0003\u0002\u0002\u0002;\u00d1\u0003\u0002\u0002\u0002=\u00e4\u0003\u0002\u0002\u0002?@\u0007<\u0002\u0002@\u0004\u0003\u0002\u0002\u0002AB\u0007.\u0002\u0002B\u0006\u0003\u0002\u0002\u0002CD\u00070\u0002\u0002D\u0008\u0003\u0002\u0002\u0002EF\u0007-\u0002\u0002F\n\u0003\u0002\u0002\u0002GH\u0007/\u0002\u0002H\u000c\u0003\u0002\u0002\u0002IJ\u0007}\u0002\u0002J\u000e\u0003\u0002\u0002\u0002KL\u0007\u007f\u0002\u0002L\u0010\u0003\u0002\u0002\u0002MN\u0007]\u0002\u0002N\u0012\u0003\u0002\u0002\u0002OP\u0007_\u0002\u0002P\u0014\u0003\u0002\u0002\u0002QR\u0007v\u0002\u0002RS\u0007t\u0002\u0002ST\u0007w\u0002\u0002TU\u0007g\u0002\u0002U\u0016\u0003\u0002\u0002\u0002VW\u0007h\u0002\u0002WX\u0007c\u0002\u0002XY\u0007n\u0002\u0002YZ\u0007u\u0002\u0002Z[\u0007g\u0002\u0002[\u0018\u0003\u0002\u0002\u0002\u005c]\u0007p\u0002\u0002]^\u0007w\u0002\u0002^_\u0007n\u0002\u0002_`\u0007n\u0002\u0002`\u001a\u0003\u0002\u0002\u0002ac\u0005\u001d\u000f\u0002bd\u0005%\u0013\u0002cb\u0003\u0002\u0002\u0002cd\u0003\u0002\u0002\u0002df\u0003\u0002\u0002\u0002eg\u0005'\u0014\u0002fe\u0003\u0002\u0002\u0002fg\u0003\u0002\u0002\u0002g\u001c\u0003\u0002\u0002\u0002hj\u0005\u000b\u0006\u0002ih\u0003\u0002\u0002\u0002ij\u0003\u0002\u0002\u0002js\u0003\u0002\u0002\u0002kt\u0005\u001f\u0010\u0002lp\u0005!\u0011\u0002mo\u0005#\u0012\u0002nm\u0003\u0002\u0002\u0002or\u0003\u0002\u0002\u0002pn\u0003\u0002\u0002\u0002pq\u0003\u0002\u0002\u0002qt\u0003\u0002\u0002\u0002rp\u0003\u0002\u0002\u0002sk\u0003\u0002\u0002\u0002sl\u0003\u0002\u0002\u0002t\u001e\u0003\u0002\u0002\u0002uv\u00072\u0002\u0002v \u0003\u0002\u0002\u0002wx\u0009\u0002\u0002\u0002x\u0022\u0003\u0002\u0002\u0002y|\u0005\u001f\u0010\u0002z|\u0005!\u0011\u0002{y\u0003\u0002\u0002\u0002{z\u0003\u0002\u0002\u0002|$\u0003\u0002\u0002\u0002}\u007f\u0005\u0007\u0004\u0002~\u0080\u0005#\u0012\u0002\u007f~\u0003\u0002\u0002\u0002\u0080\u0081\u0003\u0002\u0002\u0002\u0081\u007f\u0003\u0002\u0002\u0002\u0081\u0082\u0003\u0002\u0002\u0002\u0082&\u0003\u0002\u0002\u0002\u0083\u0086\u0009\u0003\u0002\u0002\u0084\u0087\u0005\u0009\u0005\u0002\u0085\u0087\u0005\u000b\u0006\u0002\u0086\u0084\u0003\u0002\u0002\u0002\u0086\u0085\u0003\u0002\u0002\u0002\u0086\u0087\u0003\u0002\u0002\u0002\u0087\u0089\u0003\u0002\u0002\u0002\u0088\u008a\u0005#\u0012\u0002\u0089\u0088\u0003\u0002\u0002\u0002\u008a\u008b\u0003\u0002\u0002\u0002\u008b\u0089\u0003\u0002\u0002\u0002\u008b\u008c\u0003\u0002\u0002\u0002\u008c(\u0003\u0002\u0002\u0002\u008d\u0091\u0005+\u0016\u0002\u008e\u0090\u0005-\u0017\u0002\u008f\u008e\u0003\u0002\u0002\u0002\u0090\u0093\u0003\u0002\u0002\u0002\u0091\u008f\u0003\u0002\u0002\u0002\u0091\u0092\u0003\u0002\u0002\u0002\u0092\u0094\u0003\u0002\u0002\u0002\u0093\u0091\u0003\u0002\u0002\u0002\u0094\u0095\u0005+\u0016\u0002\u0095*\u0003\u0002\u0002\u0002\u0096\u0097\u0007$\u0002\u0002\u0097,\u0003\u0002\u0002\u0002\u0098\u009b\n\u0004\u0002\u0002\u0099\u009b\u0005/\u0018\u0002\u009a\u0098\u0003\u0002\u0002\u0002\u009a\u0099\u0003\u0002\u0002\u0002\u009b.\u0003\u0002\u0002\u0002\u009c\u009f\u0007^\u0002\u0002\u009d\u00a0\u0009\u0005\u0002\u0002\u009e\u00a0\u00051\u0019\u0002\u009f\u009d\u0003\u0002\u0002\u0002\u009f\u009e\u0003\u0002\u0002\u0002\u00a00\u0003\u0002\u0002\u0002\u00a1\u00a2\u0007w\u0002\u0002\u00a2\u00a3\u00053\u001a\u0002\u00a3\u00a4\u00053\u001a\u0002\u00a4\u00a5\u00053\u001a\u0002\u00a5\u00a6\u00053\u001a\u0002\u00a62\u0003\u0002\u0002\u0002\u00a7\u00a8\u0009\u0006\u0002\u0002\u00a84\u0003\u0002\u0002\u0002\u00a9\u00aa\u00071\u0002\u0002\u00aa\u00ab\u00071\u0002\u0002\u00ab\u00af\u0003\u0002\u0002\u0002\u00ac\u00ae\u000b\u0002\u0002\u0002\u00ad\u00ac\u0003\u0002\u0002\u0002\u00ae\u00b1\u0003\u0002\u0002\u0002\u00af\u00b0\u0003\u0002\u0002\u0002\u00af\u00ad\u0003\u0002\u0002\u0002\u00b0\u00b3\u0003\u0002\u0002\u0002\u00b1\u00af\u0003\u0002\u0002\u0002\u00b2\u00b4\u0007\u000f\u0002\u0002\u00b3\u00b2\u0003\u0002\u0002\u0002\u00b3\u00b4\u0003\u0002\u0002\u0002\u00b4\u00b5\u0003\u0002\u0002\u0002\u00b5\u00b6\u0007\u000c\u0002\u0002\u00b6\u00b7\u0006\u001b\u0002\u0002\u00b7\u00b8\u0003\u0002\u0002\u0002\u00b8\u00b9\u0008\u001b\u0002\u0002\u00b96\u0003\u0002\u0002\u0002\u00ba\u00bb\u00071\u0002\u0002\u00bb\u00bc\u0007,\u0002\u0002\u00bc\u00c0\u0003\u0002\u0002\u0002\u00bd\u00bf\u000b\u0002\u0002\u0002\u00be\u00bd\u0003\u0002\u0002\u0002\u00bf\u00c2\u0003\u0002\u0002\u0002\u00c0\u00c1\u0003\u0002\u0002\u0002\u00c0\u00be\u0003\u0002\u0002\u0002\u00c1\u00c3\u0003\u0002\u0002\u0002\u00c2\u00c0\u0003\u0002\u0002\u0002\u00c3\u00c4\u0007,\u0002\u0002\u00c4\u00c5\u00071\u0002\u0002\u00c5\u00c6\u0003\u0002\u0002\u0002\u00c6\u00c7\u0006\u001c\u0003\u0002\u00c7\u00c8\u0003\u0002\u0002\u0002\u00c8\u00c9\u0008\u001c\u0003\u0002\u00c98\u0003\u0002\u0002\u0002\u00ca\u00cc\u0009\u0007\u0002\u0002\u00cb\u00ca\u0003\u0002\u0002\u0002\u00cc\u00cd\u0003\u0002\u0002\u0002\u00cd\u00cb\u0003\u0002\u0002\u0002\u00cd\u00ce\u0003\u0002\u0002\u0002\u00ce\u00cf\u0003\u0002\u0002\u0002\u00cf\u00d0\u0008\u001d\u0004\u0002\u00d0:\u0003\u0002\u0002\u0002\u00d1\u00d2\u00071\u0002\u0002\u00d2\u00d3\u0007,\u0002\u0002\u00d3\u00dd\u0003\u0002\u0002\u0002\u00d4\u00dc\n\u0008\u0002\u0002\u00d5\u00d7\u0007,\u0002\u0002\u00d6\u00d5\u0003\u0002\u0002\u0002\u00d7\u00d8\u0003\u0002\u0002\u0002\u00d8\u00d6\u0003\u0002\u0002\u0002\u00d8\u00d9\u0003\u0002\u0002\u0002\u00d9\u00da\u0003\u0002\u0002\u0002\u00da\u00dc\n\u0009\u0002\u0002\u00db\u00d4\u0003\u0002\u0002\u0002\u00db\u00d6\u0003\u0002\u0002\u0002\u00dc\u00df\u0003\u0002\u0002\u0002\u00dd\u00db\u0003\u0002\u0002\u0002\u00dd\u00de\u0003\u0002\u0002\u0002\u00de\u00e0\u0003\u0002\u0002\u0002\u00df\u00dd\u0003\u0002\u0002\u0002\u00e0\u00e1\u0006\u001e\u0004\u0002\u00e1\u00e2\u0003\u0002\u0002\u0002\u00e2\u00e3\u0008\u001e\u0005\u0002\u00e3<\u0003\u0002\u0002\u0002\u00e4\u00e5\u000b\u0002\u0002\u0002\u00e5\u00e6\u0006\u001f\u0005\u0002\u00e6\u00e7\u0003\u0002\u0002\u0002\u00e7\u00e8\u0008\u001f\u0006\u0002\u00e8>\u0003\u0002\u0002\u0002\u0016\u0002cfips{\u0081\u0086\u008b\u0091\u009a\u009f\u00af\u00b3\u00c0\u00cd\u00d8\u00db\u00dd\u0007\u0003\u001b\u0002\u0003\u001c\u0003\u0003\u001d\u0004\u0003\u001e\u0005\u0003\u001f\u0006"
fld public final static java.lang.String[] ruleNames
fld public final static java.lang.String[] tokenNames
 anno 0 java.lang.Deprecated()
fld public final static org.antlr.v4.runtime.Vocabulary VOCABULARY
fld public final static org.antlr.v4.runtime.atn.ATN _ATN
fld public static java.lang.String[] modeNames
innr public final static LexerState
meth public boolean sempred(org.antlr.v4.runtime.RuleContext,int,int)
meth public java.lang.String getGrammarFileName()
meth public java.lang.String getSerializedATN()
meth public java.lang.String[] getModeNames()
meth public java.lang.String[] getRuleNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ATN getATN()
meth public org.netbeans.modules.javascript2.json.parser.JsonLexer$LexerState getLexerState()
meth public void action(org.antlr.v4.runtime.RuleContext,int,int)
meth public void recover(org.antlr.v4.runtime.LexerNoViableAltException)
meth public void setLexerState(org.netbeans.modules.javascript2.json.parser.JsonLexer$LexerState)
supr org.antlr.v4.runtime.Lexer
hfds RECOVERIES,_LITERAL_NAMES,_SYMBOLIC_NAMES,hasErrorToken,isCommentSupported

CLSS public final static org.netbeans.modules.javascript2.json.parser.JsonLexer$LexerState
 outer org.netbeans.modules.javascript2.json.parser.JsonLexer
cons public init(int)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object
hfds atnState

CLSS public abstract interface org.netbeans.modules.javascript2.json.parser.JsonListener
intf org.antlr.v4.runtime.tree.ParseTreeListener
meth public abstract void enterArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public abstract void enterJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public abstract void enterKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public abstract void enterObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public abstract void enterPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public abstract void enterValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)
meth public abstract void exitArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public abstract void exitJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public abstract void exitKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public abstract void exitObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public abstract void exitPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public abstract void exitValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)

CLSS public org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.TokenStream)
fld protected final static org.antlr.v4.runtime.atn.PredictionContextCache _sharedContextCache
fld protected final static org.antlr.v4.runtime.dfa.DFA[] _decisionToDFA
fld public final static int COLON = 1
fld public final static int COMMA = 2
fld public final static int COMMENT = 16
fld public final static int DOT = 3
fld public final static int ERROR = 19
fld public final static int ERROR_COMMENT = 18
fld public final static int FALSE = 11
fld public final static int LBRACE = 6
fld public final static int LBRACKET = 8
fld public final static int LINE_COMMENT = 15
fld public final static int MINUS = 5
fld public final static int NULL = 12
fld public final static int NUMBER = 13
fld public final static int PLUS = 4
fld public final static int RBRACE = 7
fld public final static int RBRACKET = 9
fld public final static int RULE_array = 5
fld public final static int RULE_json = 0
fld public final static int RULE_key = 4
fld public final static int RULE_object = 2
fld public final static int RULE_pair = 3
fld public final static int RULE_value = 1
fld public final static int STRING = 14
fld public final static int TRUE = 10
fld public final static int WS = 17
fld public final static java.lang.String _serializedATN = "\u0003\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\u0003\u0015=\u0004\u0002\u0009\u0002\u0004\u0003\u0009\u0003\u0004\u0004\u0009\u0004\u0004\u0005\u0009\u0005\u0004\u0006\u0009\u0006\u0004\u0007\u0009\u0007\u0003\u0002\u0005\u0002\u0010\n\u0002\u0003\u0002\u0003\u0002\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0003\u0005\u0003\u001b\n\u0003\u0003\u0004\u0003\u0004\u0003\u0004\u0003\u0004\u0007\u0004!\n\u0004\u000c\u0004\u000e\u0004$\u000b\u0004\u0005\u0004&\n\u0004\u0003\u0004\u0003\u0004\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0005\u0003\u0006\u0003\u0006\u0003\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0007\u00074\n\u0007\u000c\u0007\u000e\u00077\u000b\u0007\u0005\u00079\n\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0002\u0002\u0008\u0002\u0004\u0006\u0008\n\u000c\u0002\u0002A\u0002\u000f\u0003\u0002\u0002\u0002\u0004\u001a\u0003\u0002\u0002\u0002\u0006\u001c\u0003\u0002\u0002\u0002\u0008)\u0003\u0002\u0002\u0002\n-\u0003\u0002\u0002\u0002\u000c/\u0003\u0002\u0002\u0002\u000e\u0010\u0005\u0004\u0003\u0002\u000f\u000e\u0003\u0002\u0002\u0002\u000f\u0010\u0003\u0002\u0002\u0002\u0010\u0011\u0003\u0002\u0002\u0002\u0011\u0012\u0007\u0002\u0002\u0003\u0012\u0003\u0003\u0002\u0002\u0002\u0013\u001b\u0007\u0010\u0002\u0002\u0014\u001b\u0007\u000f\u0002\u0002\u0015\u001b\u0007\u000c\u0002\u0002\u0016\u001b\u0007\r\u0002\u0002\u0017\u001b\u0007\u000e\u0002\u0002\u0018\u001b\u0005\u000c\u0007\u0002\u0019\u001b\u0005\u0006\u0004\u0002\u001a\u0013\u0003\u0002\u0002\u0002\u001a\u0014\u0003\u0002\u0002\u0002\u001a\u0015\u0003\u0002\u0002\u0002\u001a\u0016\u0003\u0002\u0002\u0002\u001a\u0017\u0003\u0002\u0002\u0002\u001a\u0018\u0003\u0002\u0002\u0002\u001a\u0019\u0003\u0002\u0002\u0002\u001b\u0005\u0003\u0002\u0002\u0002\u001c%\u0007\u0008\u0002\u0002\u001d\u0022\u0005\u0008\u0005\u0002\u001e\u001f\u0007\u0004\u0002\u0002\u001f!\u0005\u0008\u0005\u0002 \u001e\u0003\u0002\u0002\u0002!$\u0003\u0002\u0002\u0002\u0022 \u0003\u0002\u0002\u0002\u0022#\u0003\u0002\u0002\u0002#&\u0003\u0002\u0002\u0002$\u0022\u0003\u0002\u0002\u0002%\u001d\u0003\u0002\u0002\u0002%&\u0003\u0002\u0002\u0002&'\u0003\u0002\u0002\u0002'(\u0007\u0009\u0002\u0002(\u0007\u0003\u0002\u0002\u0002)*\u0005\n\u0006\u0002*+\u0007\u0003\u0002\u0002+,\u0005\u0004\u0003\u0002,\u0009\u0003\u0002\u0002\u0002-.\u0007\u0010\u0002\u0002.\u000b\u0003\u0002\u0002\u0002/8\u0007\n\u0002\u000205\u0005\u0004\u0003\u000212\u0007\u0004\u0002\u000224\u0005\u0004\u0003\u000231\u0003\u0002\u0002\u000247\u0003\u0002\u0002\u000253\u0003\u0002\u0002\u000256\u0003\u0002\u0002\u000269\u0003\u0002\u0002\u000275\u0003\u0002\u0002\u000280\u0003\u0002\u0002\u000289\u0003\u0002\u0002\u00029:\u0003\u0002\u0002\u0002:;\u0007\u000b\u0002\u0002;\r\u0003\u0002\u0002\u0002\u0008\u000f\u001a\u0022%58"
fld public final static java.lang.String[] ruleNames
fld public final static java.lang.String[] tokenNames
 anno 0 java.lang.Deprecated()
fld public final static org.antlr.v4.runtime.Vocabulary VOCABULARY
fld public final static org.antlr.v4.runtime.atn.ATN _ATN
innr public static ArrayContext
innr public static JsonContext
innr public static KeyContext
innr public static ObjectContext
innr public static PairContext
innr public static ValueContext
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext array()
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext json()
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext key()
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext object()
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext pair()
meth public final org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext value()
meth public java.lang.String getGrammarFileName()
meth public java.lang.String getSerializedATN()
meth public java.lang.String[] getRuleNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ATN getATN()
supr org.antlr.v4.runtime.Parser
hfds _LITERAL_NAMES,_SYMBOLIC_NAMES

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public java.util.List<org.antlr.v4.runtime.tree.TerminalNode> COMMA()
meth public java.util.List<org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext> value()
meth public org.antlr.v4.runtime.tree.TerminalNode COMMA(int)
meth public org.antlr.v4.runtime.tree.TerminalNode LBRACKET()
meth public org.antlr.v4.runtime.tree.TerminalNode RBRACKET()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext value(int)
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public org.antlr.v4.runtime.tree.TerminalNode EOF()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext value()
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public org.antlr.v4.runtime.tree.TerminalNode STRING()
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public java.util.List<org.antlr.v4.runtime.tree.TerminalNode> COMMA()
meth public java.util.List<org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext> pair()
meth public org.antlr.v4.runtime.tree.TerminalNode COMMA(int)
meth public org.antlr.v4.runtime.tree.TerminalNode LBRACE()
meth public org.antlr.v4.runtime.tree.TerminalNode RBRACE()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext pair(int)
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public org.antlr.v4.runtime.tree.TerminalNode COLON()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext key()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext value()
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public static org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext
 outer org.netbeans.modules.javascript2.json.parser.JsonParser
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getRuleIndex()
meth public org.antlr.v4.runtime.tree.TerminalNode FALSE()
meth public org.antlr.v4.runtime.tree.TerminalNode NULL()
meth public org.antlr.v4.runtime.tree.TerminalNode NUMBER()
meth public org.antlr.v4.runtime.tree.TerminalNode STRING()
meth public org.antlr.v4.runtime.tree.TerminalNode TRUE()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext array()
meth public org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext object()
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public abstract interface org.netbeans.modules.javascript2.json.parser.JsonVisitor<%0 extends java.lang.Object>
intf org.antlr.v4.runtime.tree.ParseTreeVisitor<{org.netbeans.modules.javascript2.json.parser.JsonVisitor%0}>
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public abstract {org.netbeans.modules.javascript2.json.parser.JsonVisitor%0} visitValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)

CLSS public org.netbeans.modules.javascript2.json.parser.ParseTreeToXml
cons public init(org.netbeans.modules.javascript2.json.parser.JsonLexer,org.netbeans.modules.javascript2.json.parser.JsonParser)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.w3c.dom.Document visitArray(org.netbeans.modules.javascript2.json.parser.JsonParser$ArrayContext)
meth public org.w3c.dom.Document visitJson(org.netbeans.modules.javascript2.json.parser.JsonParser$JsonContext)
meth public org.w3c.dom.Document visitKey(org.netbeans.modules.javascript2.json.parser.JsonParser$KeyContext)
meth public org.w3c.dom.Document visitObject(org.netbeans.modules.javascript2.json.parser.JsonParser$ObjectContext)
meth public org.w3c.dom.Document visitPair(org.netbeans.modules.javascript2.json.parser.JsonParser$PairContext)
meth public org.w3c.dom.Document visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
meth public org.w3c.dom.Document visitValue(org.netbeans.modules.javascript2.json.parser.JsonParser$ValueContext)
meth public static java.lang.String stringify(org.w3c.dom.Document) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.javascript2.json.parser.JsonBaseVisitor<org.w3c.dom.Document>
hfds currentNode,doc,lexer,parser

CLSS public abstract interface org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation
innr public abstract interface static Result
meth public abstract org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation$Result getOptions(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation$Result
 outer org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation
fld public final static java.lang.String PROP_COMMENT_SUPPORTED = "commentSupported"
meth public abstract java.lang.Boolean isCommentSupported()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.javascript2.json.spi.support.JsonPreferences
fld public final static java.lang.String PROP_COMMENT_SUPPORTED = "commentSupported"
meth public boolean isCommentSupported()
meth public static org.netbeans.modules.javascript2.json.spi.support.JsonPreferences forProject(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setCommentSupported(boolean)
supr java.lang.Object
hfds PREF_JSON_COMMENTS,listeners,listens,pcl,prefsRef,project

