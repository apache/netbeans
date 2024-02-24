#Signature file v4.1
#Version 3.4

CLSS public abstract com.oracle.js.parser.AbstractParser
cons protected init(com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean,int)
fld protected boolean isModule
fld protected boolean isStrictMode
fld protected boolean isStrongMode
fld protected com.oracle.js.parser.Lexer lexer
fld protected com.oracle.js.parser.TokenStream stream
fld protected com.oracle.js.parser.TokenType last
fld protected com.oracle.js.parser.TokenType type
fld protected final com.oracle.js.parser.ErrorManager errors
fld protected final com.oracle.js.parser.Source source
fld protected final int lineOffset
fld protected int finish
fld protected int k
fld protected int line
fld protected int linePosition
fld protected int start
fld protected long previousToken
fld protected long token
meth protected !varargs static java.lang.String message(java.lang.String,java.lang.String[])
meth protected com.oracle.js.parser.ir.IdentNode createIdentNode(long,int,java.lang.String)
meth protected final boolean isIdentifierName(long)
meth protected final boolean isNonStrictModeIdent()
meth protected final com.oracle.js.parser.ParserException error(com.oracle.js.parser.JSErrorType,java.lang.String)
meth protected final com.oracle.js.parser.ParserException error(com.oracle.js.parser.JSErrorType,java.lang.String,long)
meth protected final com.oracle.js.parser.ParserException error(java.lang.String)
meth protected final com.oracle.js.parser.ParserException error(java.lang.String,long)
meth protected final com.oracle.js.parser.TokenType T(int)
meth protected final com.oracle.js.parser.TokenType next()
meth protected final com.oracle.js.parser.TokenType nextOrEOL()
meth protected final com.oracle.js.parser.ir.IdentNode getIdent()
meth protected final com.oracle.js.parser.ir.IdentNode getIdentifierName()
meth protected final com.oracle.js.parser.ir.LiteralNode<?> getLiteral()
meth protected final java.lang.Object expectValue(com.oracle.js.parser.TokenType)
meth protected final java.lang.Object getValue()
meth protected final java.lang.Object getValue(long)
meth protected final java.lang.String expectMessage(com.oracle.js.parser.TokenType)
meth protected final long getToken(int)
meth protected final void expect(com.oracle.js.parser.TokenType)
meth protected final void expectDontAdvance(com.oracle.js.parser.TokenType)
meth protected final void warning(com.oracle.js.parser.JSErrorType,java.lang.String,long)
meth protected java.util.function.Function<java.lang.Number,java.lang.String> getNumberToStringConverter()
meth protected void validateLexerToken(com.oracle.js.parser.Lexer$LexerToken)
supr java.lang.Object
hfds SOURCE_URL_PREFIX,canonicalNames

CLSS public final com.oracle.js.parser.ECMAErrors
meth public !varargs static java.lang.String getMessage(java.lang.String,java.lang.String[])
supr java.lang.Object
hfds MESSAGES_BUNDLE,MESSAGES_RESOURCE

CLSS public abstract com.oracle.js.parser.ErrorManager
cons protected init()
innr public static PrintWriterErrorManager
innr public static StringBuilderErrorManager
innr public static ThrowErrorManager
meth protected void message(java.lang.String)
meth public boolean hasErrors()
meth public boolean isWarningsAsErrors()
meth public com.oracle.js.parser.ParserException getParserException()
meth public int getLimit()
meth public int getNumberOfErrors()
meth public int getNumberOfWarnings()
meth public static java.lang.String format(java.lang.String,com.oracle.js.parser.Source,int,int,long)
meth public void error(com.oracle.js.parser.ParserException)
meth public void error(java.lang.String)
meth public void setLimit(int)
meth public void setWarningsAsErrors(boolean)
meth public void warning(com.oracle.js.parser.ParserException)
meth public void warning(java.lang.String)
supr java.lang.Object
hfds errors,limit,parserException,warnings,warningsAsErrors

CLSS public static com.oracle.js.parser.ErrorManager$PrintWriterErrorManager
 outer com.oracle.js.parser.ErrorManager
cons public init()
cons public init(java.io.PrintWriter)
meth protected void message(java.lang.String)
supr com.oracle.js.parser.ErrorManager
hfds writer

CLSS public static com.oracle.js.parser.ErrorManager$StringBuilderErrorManager
 outer com.oracle.js.parser.ErrorManager
cons public init()
meth protected void message(java.lang.String)
meth public java.lang.String getOutput()
supr com.oracle.js.parser.ErrorManager
hfds buffer

CLSS public static com.oracle.js.parser.ErrorManager$ThrowErrorManager
 outer com.oracle.js.parser.ErrorManager
cons public init()
meth protected void message(java.lang.String)
meth public void error(com.oracle.js.parser.ParserException)
meth public void error(java.lang.String)
supr com.oracle.js.parser.ErrorManager

CLSS public final !enum com.oracle.js.parser.JSErrorType
fld public final static com.oracle.js.parser.JSErrorType Error
fld public final static com.oracle.js.parser.JSErrorType EvalError
fld public final static com.oracle.js.parser.JSErrorType RangeError
fld public final static com.oracle.js.parser.JSErrorType ReferenceError
fld public final static com.oracle.js.parser.JSErrorType SyntaxError
fld public final static com.oracle.js.parser.JSErrorType TypeError
fld public final static com.oracle.js.parser.JSErrorType URIError
meth public static com.oracle.js.parser.JSErrorType valueOf(java.lang.String)
meth public static com.oracle.js.parser.JSErrorType[] values()
supr java.lang.Enum<com.oracle.js.parser.JSErrorType>

CLSS public final com.oracle.js.parser.JSType
meth public static boolean isFinite(double)
meth public static boolean isNegativeZero(double)
meth public static boolean isRepresentableAsInt(double)
meth public static boolean isRepresentableAsInt(java.lang.Object)
meth public static boolean isRepresentableAsInt(long)
meth public static boolean isRepresentableAsLong(double)
meth public static boolean isRepresentableAsLong(java.lang.Object)
meth public static boolean isStrictlyRepresentableAsInt(double)
meth public static boolean isStrictlyRepresentableAsLong(double)
meth public static boolean toBoolean(double)
meth public static boolean toBoolean(java.lang.Object)
meth public static double toNumber(java.lang.Object)
meth public static double toNumber(java.lang.String)
meth public static int digit(char,int)
meth public static int digit(char,int,boolean)
meth public static int toInt32(double)
meth public static int toInt32(java.lang.Object)
meth public static int toInt32(long)
meth public static int toUint16(double)
meth public static int toUint16(int)
meth public static int toUint16(java.lang.Object)
meth public static int toUint16(long)
meth public static java.lang.String toString(double,int)
meth public static long toLong(double)
meth public static long toLong(java.lang.Object)
meth public static long toUint32(double)
meth public static long toUint32(java.lang.Object)
supr java.lang.Object
hfds INT32_LIMIT,MAX_PRECISE_DOUBLE,MAX_UINT,MIN_PRECISE_DOUBLE

CLSS public com.oracle.js.parser.Lexer
cons public init(com.oracle.js.parser.Source,com.oracle.js.parser.TokenStream)
cons public init(com.oracle.js.parser.Source,com.oracle.js.parser.TokenStream,boolean,int,boolean,boolean)
cons public init(com.oracle.js.parser.Source,int,int,com.oracle.js.parser.TokenStream,boolean,int,boolean,boolean,boolean)
innr protected abstract interface static LineInfoReceiver
innr public abstract interface static InnerState
innr public abstract static LexerToken
innr public static JsxState
innr public static RegexToken
innr public static TemplateState
innr public static XMLToken
meth protected !varargs static java.lang.String message(java.lang.String,java.lang.String[])
meth protected boolean isEOL(char)
meth protected boolean isEscapeCharacter(char)
meth protected boolean isStringDelimiter(char)
meth protected boolean isWhitespace(char)
meth protected boolean scanJsx(long,com.oracle.js.parser.TokenType)
meth protected boolean scanLiteral(long,com.oracle.js.parser.TokenType,com.oracle.js.parser.Lexer$LineInfoReceiver)
meth protected boolean skipComments()
meth protected static int convertDigit(char,int)
meth protected void add(com.oracle.js.parser.TokenType,int)
meth protected void add(com.oracle.js.parser.TokenType,int,int)
meth protected void error(java.lang.String,com.oracle.js.parser.TokenType,int,int)
meth protected void scanNumber()
meth protected void scanString(boolean)
meth public boolean canStartLiteral(com.oracle.js.parser.TokenType)
meth public com.oracle.js.parser.Lexer$RegexToken valueOfPattern(int,int)
meth public java.lang.String valueOfRawString(long)
meth public static boolean isJSEOL(char)
meth public static boolean isJSWhitespace(char)
meth public static boolean isJsonEOL(char)
meth public static boolean isJsonWhitespace(char)
meth public static java.lang.String getWhitespaceRegExp()
meth public static java.lang.String unicodeEscape(char)
meth public void lexify()
supr com.oracle.js.parser.Scanner
hfds JAVASCRIPT_WHITESPACE,JAVASCRIPT_WHITESPACE_EOL,JAVASCRIPT_WHITESPACE_IN_REGEXP,JSON_WHITESPACE,JSON_WHITESPACE_EOL,LFCR,MAX_INT_L,MIN_INT_L,SPACETAB,XML_LITERALS,ecmascriptEdition,innerStates,jsx,jsxClosing,jsxTag,jsxTagCount,last,linePosition,nested,nextStateChange,openExpressionBraces,pauseOnFunctionBody,pauseOnNextLeftBrace,pendingLine,scripting,shebang,source,stream,template,templateExpression
hcls EditStringLexer,State

CLSS public abstract interface static com.oracle.js.parser.Lexer$InnerState
 outer com.oracle.js.parser.Lexer
meth public abstract boolean emitRightCurly()
meth public abstract int nextStateChange()
meth public abstract void restore(com.oracle.js.parser.Lexer)

CLSS public static com.oracle.js.parser.Lexer$JsxState
 outer com.oracle.js.parser.Lexer
cons public init(int,boolean,boolean,int)
intf com.oracle.js.parser.Lexer$InnerState
meth public boolean emitRightCurly()
meth public int nextStateChange()
meth public void restore(com.oracle.js.parser.Lexer)
supr java.lang.Object
hfds expressionBraces,jsxClosing,jsxTag,jsxTagCount

CLSS public abstract static com.oracle.js.parser.Lexer$LexerToken
 outer com.oracle.js.parser.Lexer
cons protected init(java.lang.String)
meth public java.lang.String getExpression()
supr java.lang.Object
hfds expression

CLSS protected abstract interface static com.oracle.js.parser.Lexer$LineInfoReceiver
 outer com.oracle.js.parser.Lexer
meth public abstract void lineInfo(int,int)

CLSS public static com.oracle.js.parser.Lexer$RegexToken
 outer com.oracle.js.parser.Lexer
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getOptions()
meth public java.lang.String toString()
supr com.oracle.js.parser.Lexer$LexerToken
hfds options

CLSS public static com.oracle.js.parser.Lexer$TemplateState
 outer com.oracle.js.parser.Lexer
cons public init(boolean,boolean,int)
intf com.oracle.js.parser.Lexer$InnerState
meth public boolean emitRightCurly()
meth public int nextStateChange()
meth public void restore(com.oracle.js.parser.Lexer)
supr java.lang.Object
hfds expressionBraces,template,templateExpression

CLSS public static com.oracle.js.parser.Lexer$XMLToken
 outer com.oracle.js.parser.Lexer
cons public init(java.lang.String)
supr com.oracle.js.parser.Lexer$LexerToken

CLSS public com.oracle.js.parser.Namespace
cons public init()
cons public init(com.oracle.js.parser.Namespace)
meth public com.oracle.js.parser.Namespace getParent()
meth public java.lang.String toString()
meth public java.lang.String uniqueName(java.lang.String)
supr java.lang.Object
hfds directory,parent

CLSS public com.oracle.js.parser.Parser
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager)
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean)
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean,int)
fld protected final com.oracle.js.parser.Lexer$LineInfoReceiver lineInfoReceiver
meth protected com.oracle.js.parser.ir.Expression assignmentExpression(boolean)
meth protected com.oracle.js.parser.ir.Expression expression()
meth public com.oracle.js.parser.ir.FunctionNode parse()
meth public com.oracle.js.parser.ir.FunctionNode parse(java.lang.String,int,int,boolean)
meth public com.oracle.js.parser.ir.FunctionNode parseFunctionBody()
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String)
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String,int,int)
meth public java.lang.String toString()
meth public java.util.List<com.oracle.js.parser.ir.IdentNode> parseFormalParameterList()
meth public void setFunctionName(java.lang.String)
meth public void setReparsedFunction(com.oracle.js.parser.RecompilableScriptFunctionData)
supr com.oracle.js.parser.AbstractParser
hfds ANON_FUNCTION_PREFIX,ARGUMENTS_NAME,ARROW_FUNCTION_PREFIX,ASYNC_IDENT,EVAL_NAME,EXEC_NAME,NESTED_FUNCTION_SEPARATOR,PROGRAM_NAME,defaultNames,env,functionDeclarations,lc,namespace,reparsedFunction,scripting,shebang
hcls ClassElementKey,ForVariableDeclarationListResult,ParserState,PropertyFunction,VerifyDestructuringPatternNodeVisitor

CLSS public final com.oracle.js.parser.ParserException
cons public init(com.oracle.js.parser.JSErrorType,java.lang.String,com.oracle.js.parser.Source,int,int,long)
cons public init(java.lang.String)
meth public com.oracle.js.parser.JSErrorType getErrorType()
meth public com.oracle.js.parser.Source getSource()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int getPosition()
meth public java.lang.String getFileName()
meth public long getToken()
meth public void setColumnNumber(int)
meth public void setFileName(java.lang.String)
meth public void setLineNumber(int)
supr java.lang.RuntimeException
hfds column,errorType,fileName,line,source,token

CLSS public abstract interface com.oracle.js.parser.RecompilableScriptFunctionData
meth public abstract com.oracle.js.parser.RecompilableScriptFunctionData getScriptFunctionData(int)
meth public abstract int getFunctionFlags()
meth public abstract int getFunctionNodeId()
meth public abstract java.lang.Object getEndParserState()

CLSS public com.oracle.js.parser.Scanner
cons protected init(java.lang.String,int,int,int)
fld protected char ch0
fld protected char ch1
fld protected char ch2
fld protected char ch3
fld protected final int limit
fld protected final java.lang.String content
fld protected int line
fld protected int position
meth protected final boolean atEOF()
meth protected final char charAt(int)
meth protected final void reset(int)
meth protected final void skip(int)
supr java.lang.Object
hcls State

CLSS public final com.oracle.js.parser.ScriptEnvironment
innr public final static !enum FunctionStatementBehavior
innr public final static Builder
meth public boolean isStrict()
meth public static com.oracle.js.parser.ScriptEnvironment$Builder builder()
supr java.lang.Object
hfds constAsVar,dumpOnError,earlyLvalueError,ecmascriptEdition,emptyStatements,err,functionDeclarationHoisting,functionStatement,jsx,namespace,scripting,shebang,strict,syntaxExtensions

CLSS public final static com.oracle.js.parser.ScriptEnvironment$Builder
 outer com.oracle.js.parser.ScriptEnvironment
meth public com.oracle.js.parser.ScriptEnvironment build()
meth public com.oracle.js.parser.ScriptEnvironment$Builder constAsVar(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder dumpOnError(java.io.PrintWriter)
meth public com.oracle.js.parser.ScriptEnvironment$Builder earlyLvalueError(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder ecmacriptEdition(int)
meth public com.oracle.js.parser.ScriptEnvironment$Builder emptyStatements(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder functionDeclarationHoisting(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder functionStatementBehavior(com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior)
meth public com.oracle.js.parser.ScriptEnvironment$Builder jsx(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder scripting(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder shebang(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder strict(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder syntaxExtensions(boolean)
supr java.lang.Object
hfds constAsVar,dumpOnError,earlyLvalueError,ecmacriptEdition,emptyStatements,functionDeclarationHoisting,functionStatementBehavior,jsx,scripting,shebang,strict,syntaxExtensions

CLSS public final static !enum com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior
 outer com.oracle.js.parser.ScriptEnvironment
fld public final static com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior ACCEPT
fld public final static com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior ERROR
fld public final static com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior WARNING
meth public static com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior valueOf(java.lang.String)
meth public static com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior[] values()
supr java.lang.Enum<com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior>

CLSS public final com.oracle.js.parser.Source
meth public boolean equals(java.lang.Object)
meth public boolean isEvalCode()
meth public int getColumn(int)
meth public int getLength()
meth public int getLine(int)
meth public int hashCode()
meth public java.lang.String getBase()
meth public java.lang.String getContent()
meth public java.lang.String getDigest()
meth public java.lang.String getExplicitURL()
meth public java.lang.String getName()
meth public java.lang.String getSourceLine(int)
meth public java.lang.String getString()
meth public java.lang.String getString(int,int)
meth public java.lang.String getString(long)
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public long getLastModified()
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.io.File) throws java.io.IOException
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.io.File,java.nio.charset.Charset) throws java.io.IOException
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.io.Reader) throws java.io.IOException
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.lang.String)
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.lang.String,boolean)
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.net.URL) throws java.io.IOException
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.net.URL,java.nio.charset.Charset) throws java.io.IOException
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.nio.file.Path) throws java.io.IOException
meth public static java.lang.String baseURL(java.net.URL)
meth public static java.lang.String readFully(java.io.File) throws java.io.IOException
meth public static java.lang.String readFully(java.io.File,java.nio.charset.Charset) throws java.io.IOException
meth public static java.lang.String readFully(java.io.InputStream) throws java.io.IOException
meth public static java.lang.String readFully(java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException
meth public static java.lang.String readFully(java.io.Reader) throws java.io.IOException
meth public static java.lang.String readFully(java.net.URL) throws java.io.IOException
meth public static java.lang.String readFully(java.net.URL,java.nio.charset.Charset) throws java.io.IOException
meth public void setExplicitURL(java.lang.String)
supr java.lang.Object
hfds BUF_SIZE,base,data,digest,explicitURL,hash,name
hcls Data,FileData,RawData,URLData

CLSS public final com.oracle.js.parser.Token
fld public final static int LENGTH_MASK = 268435455
meth public static com.oracle.js.parser.TokenType descType(long)
meth public static int descLength(long)
meth public static int descPosition(long)
meth public static java.lang.String toString(com.oracle.js.parser.Source,long)
meth public static java.lang.String toString(com.oracle.js.parser.Source,long,boolean)
meth public static java.lang.String toString(long)
meth public static long recast(long,com.oracle.js.parser.TokenType)
meth public static long toDesc(com.oracle.js.parser.TokenType,int,int)
meth public static long withDelimiter(long)
supr java.lang.Object
hfds LENGTH_SHIFT,POSITION_SHIFT

CLSS public final !enum com.oracle.js.parser.TokenKind
fld public final static com.oracle.js.parser.TokenKind BINARY
fld public final static com.oracle.js.parser.TokenKind BRACKET
fld public final static com.oracle.js.parser.TokenKind FUTURE
fld public final static com.oracle.js.parser.TokenKind FUTURESTRICT
fld public final static com.oracle.js.parser.TokenKind IR
fld public final static com.oracle.js.parser.TokenKind JSX
fld public final static com.oracle.js.parser.TokenKind KEYWORD
fld public final static com.oracle.js.parser.TokenKind LITERAL
fld public final static com.oracle.js.parser.TokenKind SPECIAL
fld public final static com.oracle.js.parser.TokenKind UNARY
meth public static com.oracle.js.parser.TokenKind valueOf(java.lang.String)
meth public static com.oracle.js.parser.TokenKind[] values()
supr java.lang.Enum<com.oracle.js.parser.TokenKind>

CLSS public final com.oracle.js.parser.TokenLookup
meth public static com.oracle.js.parser.TokenType lookupKeyword(java.lang.String,int,int)
meth public static com.oracle.js.parser.TokenType lookupOperator(char,char,char,char)
supr java.lang.Object
hfds table,tableBase,tableLength,tableLimit

CLSS public com.oracle.js.parser.TokenStream
cons public init()
meth public boolean isEmpty()
meth public boolean isFull()
meth public int count()
meth public int first()
meth public int last()
meth public long get(int)
meth public void commit(int)
meth public void grow()
meth public void put(long)
meth public void removeLast()
supr java.lang.Object
hfds INITIAL_SIZE,base,buffer,count,in,out

CLSS public final !enum com.oracle.js.parser.TokenType
fld public final static com.oracle.js.parser.TokenType ADD
fld public final static com.oracle.js.parser.TokenType AND
fld public final static com.oracle.js.parser.TokenType ARRAY
fld public final static com.oracle.js.parser.TokenType ARROW
fld public final static com.oracle.js.parser.TokenType ASSIGN
fld public final static com.oracle.js.parser.TokenType ASSIGN_ADD
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_AND
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_OR
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_XOR
fld public final static com.oracle.js.parser.TokenType ASSIGN_DIV
fld public final static com.oracle.js.parser.TokenType ASSIGN_EXP
fld public final static com.oracle.js.parser.TokenType ASSIGN_LOG_AND
fld public final static com.oracle.js.parser.TokenType ASSIGN_LOG_OR
fld public final static com.oracle.js.parser.TokenType ASSIGN_MOD
fld public final static com.oracle.js.parser.TokenType ASSIGN_MUL
fld public final static com.oracle.js.parser.TokenType ASSIGN_NULLISH
fld public final static com.oracle.js.parser.TokenType ASSIGN_SAR
fld public final static com.oracle.js.parser.TokenType ASSIGN_SHL
fld public final static com.oracle.js.parser.TokenType ASSIGN_SHR
fld public final static com.oracle.js.parser.TokenType ASSIGN_SUB
fld public final static com.oracle.js.parser.TokenType AT
fld public final static com.oracle.js.parser.TokenType AWAIT
fld public final static com.oracle.js.parser.TokenType BIGINT
fld public final static com.oracle.js.parser.TokenType BINARY_NUMBER
fld public final static com.oracle.js.parser.TokenType BIT_AND
fld public final static com.oracle.js.parser.TokenType BIT_NOT
fld public final static com.oracle.js.parser.TokenType BIT_OR
fld public final static com.oracle.js.parser.TokenType BIT_XOR
fld public final static com.oracle.js.parser.TokenType BREAK
fld public final static com.oracle.js.parser.TokenType CASE
fld public final static com.oracle.js.parser.TokenType CATCH
fld public final static com.oracle.js.parser.TokenType CLASS
fld public final static com.oracle.js.parser.TokenType COLON
fld public final static com.oracle.js.parser.TokenType COMMALEFT
fld public final static com.oracle.js.parser.TokenType COMMARIGHT
fld public final static com.oracle.js.parser.TokenType COMMENT
fld public final static com.oracle.js.parser.TokenType CONST
fld public final static com.oracle.js.parser.TokenType CONTINUE
fld public final static com.oracle.js.parser.TokenType DEBUGGER
fld public final static com.oracle.js.parser.TokenType DECIMAL
fld public final static com.oracle.js.parser.TokenType DECPOSTFIX
fld public final static com.oracle.js.parser.TokenType DECPREFIX
fld public final static com.oracle.js.parser.TokenType DEFAULT
fld public final static com.oracle.js.parser.TokenType DELETE
fld public final static com.oracle.js.parser.TokenType DIRECTIVE_COMMENT
fld public final static com.oracle.js.parser.TokenType DIV
fld public final static com.oracle.js.parser.TokenType DO
fld public final static com.oracle.js.parser.TokenType ELLIPSIS
fld public final static com.oracle.js.parser.TokenType ELSE
fld public final static com.oracle.js.parser.TokenType ENUM
fld public final static com.oracle.js.parser.TokenType EOF
fld public final static com.oracle.js.parser.TokenType EOL
fld public final static com.oracle.js.parser.TokenType EQ
fld public final static com.oracle.js.parser.TokenType EQ_STRICT
fld public final static com.oracle.js.parser.TokenType ERROR
fld public final static com.oracle.js.parser.TokenType ESCSTRING
fld public final static com.oracle.js.parser.TokenType EXECSTRING
fld public final static com.oracle.js.parser.TokenType EXP
fld public final static com.oracle.js.parser.TokenType EXPORT
fld public final static com.oracle.js.parser.TokenType EXTENDS
fld public final static com.oracle.js.parser.TokenType FALSE
fld public final static com.oracle.js.parser.TokenType FINALLY
fld public final static com.oracle.js.parser.TokenType FLOATING
fld public final static com.oracle.js.parser.TokenType FOR
fld public final static com.oracle.js.parser.TokenType FUNCTION
fld public final static com.oracle.js.parser.TokenType GE
fld public final static com.oracle.js.parser.TokenType GT
fld public final static com.oracle.js.parser.TokenType HEXADECIMAL
fld public final static com.oracle.js.parser.TokenType IDENT
fld public final static com.oracle.js.parser.TokenType IF
fld public final static com.oracle.js.parser.TokenType IMPLEMENTS
fld public final static com.oracle.js.parser.TokenType IMPORT
fld public final static com.oracle.js.parser.TokenType IN
fld public final static com.oracle.js.parser.TokenType INCPOSTFIX
fld public final static com.oracle.js.parser.TokenType INCPREFIX
fld public final static com.oracle.js.parser.TokenType INSTANCEOF
fld public final static com.oracle.js.parser.TokenType INTERFACE
fld public final static com.oracle.js.parser.TokenType JSX_ELEM_CLOSE
fld public final static com.oracle.js.parser.TokenType JSX_ELEM_END
fld public final static com.oracle.js.parser.TokenType JSX_ELEM_START
fld public final static com.oracle.js.parser.TokenType JSX_IDENTIFIER
fld public final static com.oracle.js.parser.TokenType JSX_STRING
fld public final static com.oracle.js.parser.TokenType JSX_TEXT
fld public final static com.oracle.js.parser.TokenType LBRACE
fld public final static com.oracle.js.parser.TokenType LBRACKET
fld public final static com.oracle.js.parser.TokenType LE
fld public final static com.oracle.js.parser.TokenType LET
fld public final static com.oracle.js.parser.TokenType LPAREN
fld public final static com.oracle.js.parser.TokenType LT
fld public final static com.oracle.js.parser.TokenType MOD
fld public final static com.oracle.js.parser.TokenType MUL
fld public final static com.oracle.js.parser.TokenType NE
fld public final static com.oracle.js.parser.TokenType NEW
fld public final static com.oracle.js.parser.TokenType NE_STRICT
fld public final static com.oracle.js.parser.TokenType NOT
fld public final static com.oracle.js.parser.TokenType NULL
fld public final static com.oracle.js.parser.TokenType NULLISH
fld public final static com.oracle.js.parser.TokenType OBJECT
fld public final static com.oracle.js.parser.TokenType OCTAL
fld public final static com.oracle.js.parser.TokenType OCTAL_LEGACY
fld public final static com.oracle.js.parser.TokenType OPTIONAL_ACCESS
fld public final static com.oracle.js.parser.TokenType OR
fld public final static com.oracle.js.parser.TokenType PACKAGE
fld public final static com.oracle.js.parser.TokenType PERIOD
fld public final static com.oracle.js.parser.TokenType PRIVATE
fld public final static com.oracle.js.parser.TokenType PROTECTED
fld public final static com.oracle.js.parser.TokenType PUBLIC
fld public final static com.oracle.js.parser.TokenType RBRACE
fld public final static com.oracle.js.parser.TokenType RBRACKET
fld public final static com.oracle.js.parser.TokenType REGEX
fld public final static com.oracle.js.parser.TokenType RETURN
fld public final static com.oracle.js.parser.TokenType RPAREN
fld public final static com.oracle.js.parser.TokenType SAR
fld public final static com.oracle.js.parser.TokenType SEMICOLON
fld public final static com.oracle.js.parser.TokenType SHL
fld public final static com.oracle.js.parser.TokenType SHR
fld public final static com.oracle.js.parser.TokenType SPREAD_ARGUMENT
fld public final static com.oracle.js.parser.TokenType SPREAD_ARRAY
fld public final static com.oracle.js.parser.TokenType SPREAD_OBJECT
fld public final static com.oracle.js.parser.TokenType STATIC
fld public final static com.oracle.js.parser.TokenType STRING
fld public final static com.oracle.js.parser.TokenType SUB
fld public final static com.oracle.js.parser.TokenType SUPER
fld public final static com.oracle.js.parser.TokenType SWITCH
fld public final static com.oracle.js.parser.TokenType TEMPLATE
fld public final static com.oracle.js.parser.TokenType TEMPLATE_HEAD
fld public final static com.oracle.js.parser.TokenType TEMPLATE_MIDDLE
fld public final static com.oracle.js.parser.TokenType TEMPLATE_TAIL
fld public final static com.oracle.js.parser.TokenType TERNARY
fld public final static com.oracle.js.parser.TokenType THIS
fld public final static com.oracle.js.parser.TokenType THROW
fld public final static com.oracle.js.parser.TokenType TRUE
fld public final static com.oracle.js.parser.TokenType TRY
fld public final static com.oracle.js.parser.TokenType TYPEOF
fld public final static com.oracle.js.parser.TokenType VAR
fld public final static com.oracle.js.parser.TokenType VOID
fld public final static com.oracle.js.parser.TokenType WHILE
fld public final static com.oracle.js.parser.TokenType WITH
fld public final static com.oracle.js.parser.TokenType XML
fld public final static com.oracle.js.parser.TokenType YIELD
fld public final static com.oracle.js.parser.TokenType YIELD_STAR
meth public boolean isAssignment()
meth public boolean isLeftAssociative()
meth public boolean isOperator(boolean)
meth public boolean isSupported(int)
meth public boolean needsParens(com.oracle.js.parser.TokenType,boolean)
meth public com.oracle.js.parser.TokenKind getKind()
meth public com.oracle.js.parser.TokenType getNext()
meth public int getEcmascriptEdition()
meth public int getLength()
meth public int getPrecedence()
meth public java.lang.String getName()
meth public java.lang.String getNameOrType()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.TokenType valueOf(java.lang.String)
meth public static com.oracle.js.parser.TokenType[] values()
meth public void setNext(com.oracle.js.parser.TokenType)
supr java.lang.Enum<com.oracle.js.parser.TokenType>
hfds ecmascriptEdition,isLeftAssociative,kind,name,next,precedence,values

CLSS abstract interface com.oracle.js.parser.package-info

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

