#Signature file v4.1
#Version 1.23

CLSS public abstract com.oracle.js.parser.AbstractParser
cons protected init(com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean,int)
fld protected boolean isStrictMode
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
meth protected final boolean isIdentifierName()
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
meth protected final java.lang.Object getValue()
meth protected final java.lang.Object getValue(long)
meth protected final java.lang.Object getValueNoEscape()
meth protected final java.lang.String expectMessage(com.oracle.js.parser.TokenType)
meth protected final java.lang.String expectMessage(com.oracle.js.parser.TokenType,long)
meth protected final long getToken(int)
meth protected final void expect(com.oracle.js.parser.TokenType)
meth protected final void expectDontAdvance(com.oracle.js.parser.TokenType)
meth protected final void warning(com.oracle.js.parser.JSErrorType,java.lang.String,long)
meth protected java.util.function.Function<java.lang.Number,java.lang.String> getNumberToStringConverter()
meth protected void validateLexerToken(com.oracle.js.parser.Lexer$LexerToken)
supr java.lang.Object
hfds SOURCE_URL_PREFIX

CLSS public final com.oracle.js.parser.ECMAErrors
meth public !varargs static java.lang.String getMessage(java.lang.String,java.lang.String[])
supr java.lang.Object
hfds MESSAGES_BUNDLE,MESSAGES_RESOURCE

CLSS public abstract com.oracle.js.parser.ErrorManager
cons protected init()
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

CLSS public final com.oracle.js.parser.IdentUtils
meth public static boolean isIdentifierPart(int)
meth public static boolean isIdentifierStart(int)
supr java.lang.Object
hfds ID_PART_ARRAY,ID_PART_RANGES,ID_START_ARRAY,ID_START_RANGES,PRECOMPUTED_ARRAY_SIZE

CLSS public final !enum com.oracle.js.parser.JSErrorType
fld public final static com.oracle.js.parser.JSErrorType AggregateError
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
meth public static boolean isRepresentableAsInt(double)
meth public static boolean isRepresentableAsLong(double)
meth public static boolean isStrictlyRepresentableAsInt(double)
meth public static boolean isStrictlyRepresentableAsLong(double)
supr java.lang.Object

CLSS public com.oracle.js.parser.Lexer
cons public init(com.oracle.js.parser.Source,com.oracle.js.parser.TokenStream,boolean,int,boolean,boolean,boolean)
cons public init(com.oracle.js.parser.Source,int,int,com.oracle.js.parser.TokenStream,boolean,int,boolean,boolean,boolean,boolean)
innr protected abstract interface static LineInfoReceiver
innr public abstract static LexerToken
innr public static RegexToken
innr public static XMLToken
meth protected !varargs static java.lang.String message(java.lang.String,java.lang.String[])
meth protected boolean isEOL(char)
meth protected boolean isEscapeCharacter(char)
meth protected boolean isStringDelimiter(char)
meth protected boolean isWhitespace(char)
meth protected boolean scanLiteral(long,com.oracle.js.parser.TokenType,com.oracle.js.parser.Lexer$LineInfoReceiver)
meth protected final void scanTemplateSpan()
meth protected static int convertDigit(char,int)
meth protected void add(com.oracle.js.parser.TokenType,int)
meth protected void add(com.oracle.js.parser.TokenType,int,int)
meth protected void error(java.lang.String,com.oracle.js.parser.TokenType,int,int)
meth protected void scanNumber()
meth protected void scanString(boolean)
meth public boolean canStartLiteral(com.oracle.js.parser.TokenType)
meth public boolean checkIdentForKeyword(long,java.lang.String)
meth public com.oracle.js.parser.Lexer$RegexToken valueOfPattern(int,int)
meth public java.lang.String stringIntern(java.lang.String)
meth public java.lang.String valueOfRawString(long)
meth public static boolean isJSEOL(char)
meth public static boolean isJSWhitespace(char)
meth public static boolean isStringLineTerminator(char)
meth public void lexify()
supr com.oracle.js.parser.Scanner
hfds JAVASCRIPT_WHITESPACE_HIGH,JAVASCRIPT_WHITESPACE_HIGH_START,MESSAGE_INVALID_HEX,XML_LITERALS,allowBigInt,ecmaScriptVersion,internedStrings,isModule,last,linePosition,nested,pauseOnFunctionBody,pauseOnNextLeftBrace,pauseOnRightBrace,pendingLine,scripting,shebang,source,stream
hcls EditStringLexer,State

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
fld public final static boolean PROFILE_PARSING
fld public final static boolean PROFILE_PARSING_PRINT
meth public com.oracle.js.parser.ir.Expression parseExpression()
meth public com.oracle.js.parser.ir.FunctionNode parse()
meth public com.oracle.js.parser.ir.FunctionNode parse(java.lang.String,int,int,int,com.oracle.js.parser.ir.Scope,java.lang.String[])
meth public com.oracle.js.parser.ir.FunctionNode parseEval(boolean,com.oracle.js.parser.ir.Scope)
meth public com.oracle.js.parser.ir.FunctionNode parseFunctionBody(boolean,boolean)
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String)
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String,int,int)
meth public com.oracle.js.parser.ir.FunctionNode parseWithArguments(java.lang.String[])
meth public java.lang.String toString()
meth public void parseFormalParameterList()
meth public void setReparsedFunction(com.oracle.js.parser.RecompilableScriptFunctionData)
supr com.oracle.js.parser.AbstractParser
hfds ANONYMOUS_FUNCTION_NAME,APPLY_NAME,ARGUMENTS_NAME,ARROW_FUNCTION_NAME,ASSIGNMENT_TARGET_CONTEXT,CATCH_PARAMETER_CONTEXT,CLASS_NAME_CONTEXT,CONSTRUCTOR_NAME,ERROR_BINDING_NAME,ES2019_OPTIONAL_CATCH_BINDING,ES2020_CLASS_FIELDS,ES2021_TOP_LEVEL_AWAIT,ES6_ARROW_FUNCTION,ES6_CLASS,ES6_COMPUTED_PROPERTY_NAME,ES6_DEFAULT_PARAMETER,ES6_DESTRUCTURING,ES6_FOR_OF,ES6_GENERATOR_FUNCTION,ES6_NEW_TARGET,ES6_REST_PARAMETER,ES6_SPREAD_ARGUMENT,ES6_SPREAD_ARRAY,ES8_ASYNC_FUNCTION,ES8_FOR_AWAIT_OF,ES8_REST_SPREAD_PROPERTY,ES8_TRAILING_COMMA,EVAL_NAME,EXEC_NAME,FUNCTION_PARAMETER_CONTEXT,IMPORTED_BINDING_CONTEXT,IMPORT_META_NAME,INITIALIZER_FUNCTION_NAME,MESSAGE_ESCAPED_KEYWORD,MESSAGE_EXPECTED_OPERAND,MESSAGE_EXPECTED_STMT,MESSAGE_INVALID_ARROW_PARAMETER,MESSAGE_INVALID_LVALUE,MESSAGE_INVALID_PROPERTY_INITIALIZER,MESSAGE_PROPERTY_REDEFINITON,NEW_TARGET_NAME,PARSE_EVAL,PARSE_FUNCTION_CONTEXT_EVAL,PRIVATE_CONSTRUCTOR_NAME,PROGRAM_NAME,PROTOTYPE_NAME,PROTO_NAME,REPARSE_IS_METHOD,REPARSE_IS_PROPERTY_ACCESSOR,SWITCH_BINDING_NAME,VARIABLE_NAME_CONTEXT,allowBigInt,defaultNames,env,functionDeclarations,isModule,lc,namespace,reparsedFunction,scripting,shebang
hcls ForVariableDeclarationListResult,ParserState,PropertyFunction,VerifyDestructuringPatternNodeVisitor

CLSS public final com.oracle.js.parser.ParserException
cons public init(com.oracle.js.parser.JSErrorType,java.lang.String,com.oracle.js.parser.Source,int,int,long)
cons public init(java.lang.String)
meth public boolean isIncompleteSource()
meth public com.oracle.js.parser.JSErrorType getErrorType()
meth public com.oracle.js.parser.Source getSource()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int getPosition()
meth public java.lang.String getFileName()
meth public java.lang.String getMessage()
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
cons protected init(char[],int,int,int)
fld protected char ch0
fld protected char ch1
fld protected char ch2
fld protected char ch3
fld protected final char[] content
fld protected final int limit
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
hfds allowBigInt,annexB,classFields,constAsVar,dumpOnError,ecmaScriptVersion,emptyStatements,err,functionStatement,namespace,scripting,shebang,strict,syntaxExtensions

CLSS public final static com.oracle.js.parser.ScriptEnvironment$Builder
 outer com.oracle.js.parser.ScriptEnvironment
meth public com.oracle.js.parser.ScriptEnvironment build()
meth public com.oracle.js.parser.ScriptEnvironment$Builder allowBigInt(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder annexB(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder classFields(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder constAsVar(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder dumpOnError(java.io.PrintWriter)
meth public com.oracle.js.parser.ScriptEnvironment$Builder ecmaScriptVersion(int)
meth public com.oracle.js.parser.ScriptEnvironment$Builder emptyStatements(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder functionStatementBehavior(com.oracle.js.parser.ScriptEnvironment$FunctionStatementBehavior)
meth public com.oracle.js.parser.ScriptEnvironment$Builder scripting(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder shebang(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder strict(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder syntaxExtensions(boolean)
supr java.lang.Object
hfds allowBigInt,annexB,classFields,constAsVar,dumpOnError,ecmaScriptVersion,emptyStatements,functionStatementBehavior,scripting,shebang,strict,syntaxExtensions

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
meth public java.lang.CharSequence getContent()
meth public java.lang.CharSequence getSourceLine(int)
meth public java.lang.String getBase()
meth public java.lang.String getDigest()
meth public java.lang.String getExplicitURL()
meth public java.lang.String getName()
meth public java.lang.String getString()
meth public java.lang.String getString(int,int)
meth public java.lang.String getString(long)
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public long getLastModified()
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.lang.CharSequence,boolean)
meth public static com.oracle.js.parser.Source sourceFor(java.lang.String,java.lang.String)
meth public static java.lang.String readFully(java.io.Reader) throws java.io.IOException
meth public void setExplicitURL(java.lang.String)
supr java.lang.Object
hfds BUF_SIZE,base,data,digest,explicitURL,hash,name
hcls Data,RawData

CLSS public final com.oracle.js.parser.Token
fld public final static int LENGTH_MASK = 268435455
meth public static com.oracle.js.parser.TokenType descType(long)
meth public static int descLength(long)
meth public static int descPosition(long)
meth public static java.lang.String toString(com.oracle.js.parser.Source,long)
meth public static java.lang.String toString(com.oracle.js.parser.Source,long,boolean)
meth public static long recast(long,com.oracle.js.parser.TokenType)
meth public static long toDesc(com.oracle.js.parser.TokenType,int,int)
meth public static long withDelimiter(long)
supr java.lang.Object
hfds LENGTH_SHIFT,POSITION_SHIFT

CLSS public final !enum com.oracle.js.parser.TokenKind
fld public final static com.oracle.js.parser.TokenKind BINARY
fld public final static com.oracle.js.parser.TokenKind BRACKET
fld public final static com.oracle.js.parser.TokenKind CONTEXTUAL
fld public final static com.oracle.js.parser.TokenKind FUTURE
fld public final static com.oracle.js.parser.TokenKind FUTURESTRICT
fld public final static com.oracle.js.parser.TokenKind IR
fld public final static com.oracle.js.parser.TokenKind KEYWORD
fld public final static com.oracle.js.parser.TokenKind LITERAL
fld public final static com.oracle.js.parser.TokenKind SPECIAL
fld public final static com.oracle.js.parser.TokenKind UNARY
meth public static com.oracle.js.parser.TokenKind valueOf(java.lang.String)
meth public static com.oracle.js.parser.TokenKind[] values()
supr java.lang.Enum<com.oracle.js.parser.TokenKind>

CLSS public final com.oracle.js.parser.TokenLookup
meth public static com.oracle.js.parser.TokenType lookupKeyword(char[],int,int)
meth public static com.oracle.js.parser.TokenType lookupOperator(char,char,char,char,int)
supr java.lang.Object
hfds table,tableBase,tableLength,tableLimit

CLSS public com.oracle.js.parser.TokenStream
cons public init()
meth public boolean isEmpty()
meth public boolean isFull()
meth public int last()
meth public long get(int)
meth public void commit(int)
meth public void grow()
meth public void put(long)
supr java.lang.Object
hfds INITIAL_SIZE,base,buffer,count,in,out

CLSS public final !enum com.oracle.js.parser.TokenType
fld public final static com.oracle.js.parser.TokenType ADD
fld public final static com.oracle.js.parser.TokenType AND
fld public final static com.oracle.js.parser.TokenType ARRAY
fld public final static com.oracle.js.parser.TokenType ARROW
fld public final static com.oracle.js.parser.TokenType AS
fld public final static com.oracle.js.parser.TokenType ASSIGN
fld public final static com.oracle.js.parser.TokenType ASSIGN_ADD
fld public final static com.oracle.js.parser.TokenType ASSIGN_AND
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_AND
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_OR
fld public final static com.oracle.js.parser.TokenType ASSIGN_BIT_XOR
fld public final static com.oracle.js.parser.TokenType ASSIGN_DIV
fld public final static com.oracle.js.parser.TokenType ASSIGN_EXP
fld public final static com.oracle.js.parser.TokenType ASSIGN_INIT
fld public final static com.oracle.js.parser.TokenType ASSIGN_MOD
fld public final static com.oracle.js.parser.TokenType ASSIGN_MUL
fld public final static com.oracle.js.parser.TokenType ASSIGN_NULLCOAL
fld public final static com.oracle.js.parser.TokenType ASSIGN_OR
fld public final static com.oracle.js.parser.TokenType ASSIGN_SAR
fld public final static com.oracle.js.parser.TokenType ASSIGN_SHL
fld public final static com.oracle.js.parser.TokenType ASSIGN_SHR
fld public final static com.oracle.js.parser.TokenType ASSIGN_SUB
fld public final static com.oracle.js.parser.TokenType ASYNC
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
fld public final static com.oracle.js.parser.TokenType FROM
fld public final static com.oracle.js.parser.TokenType FUNCTION
fld public final static com.oracle.js.parser.TokenType GE
fld public final static com.oracle.js.parser.TokenType GET
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
fld public final static com.oracle.js.parser.TokenType NON_OCTAL_DECIMAL
fld public final static com.oracle.js.parser.TokenType NOT
fld public final static com.oracle.js.parser.TokenType NULL
fld public final static com.oracle.js.parser.TokenType NULLISHCOALESC
fld public final static com.oracle.js.parser.TokenType OBJECT
fld public final static com.oracle.js.parser.TokenType OCTAL
fld public final static com.oracle.js.parser.TokenType OCTAL_LEGACY
fld public final static com.oracle.js.parser.TokenType OF
fld public final static com.oracle.js.parser.TokenType OPTIONAL_CHAIN
fld public final static com.oracle.js.parser.TokenType OR
fld public final static com.oracle.js.parser.TokenType PACKAGE
fld public final static com.oracle.js.parser.TokenType PERIOD
fld public final static com.oracle.js.parser.TokenType PRIVATE
fld public final static com.oracle.js.parser.TokenType PRIVATE_IDENT
fld public final static com.oracle.js.parser.TokenType PROTECTED
fld public final static com.oracle.js.parser.TokenType PUBLIC
fld public final static com.oracle.js.parser.TokenType RBRACE
fld public final static com.oracle.js.parser.TokenType RBRACKET
fld public final static com.oracle.js.parser.TokenType REGEX
fld public final static com.oracle.js.parser.TokenType RETURN
fld public final static com.oracle.js.parser.TokenType RPAREN
fld public final static com.oracle.js.parser.TokenType SAR
fld public final static com.oracle.js.parser.TokenType SEMICOLON
fld public final static com.oracle.js.parser.TokenType SET
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
meth public boolean isContextualKeyword()
meth public boolean isFutureStrict()
meth public boolean isLeftAssociative()
meth public boolean isOperator(boolean)
meth public boolean needsParens(com.oracle.js.parser.TokenType,boolean)
meth public com.oracle.js.parser.TokenKind getKind()
meth public com.oracle.js.parser.TokenType getNext()
meth public int getECMAScriptVersion()
meth public int getLength()
meth public int getPrecedence()
meth public java.lang.String getName()
meth public java.lang.String getNameOrType()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.TokenType valueOf(java.lang.String)
meth public static com.oracle.js.parser.TokenType[] values()
supr java.lang.Enum<com.oracle.js.parser.TokenType>
hfds ecmaScriptVersion,isLeftAssociative,kind,name,next,precedence,tokenValues

CLSS public final com.oracle.js.parser.ir.AccessNode
cons public init(long,int,com.oracle.js.parser.ir.Expression,java.lang.String)
cons public init(long,int,com.oracle.js.parser.ir.Expression,java.lang.String,boolean,boolean,boolean,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isPrivate()
meth public com.oracle.js.parser.ir.AccessNode setIsSuper()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.lang.String getPrivateName()
meth public java.lang.String getProperty()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.BaseNode
hfds isPrivate,property

CLSS public abstract interface com.oracle.js.parser.ir.Assignment<%0 extends com.oracle.js.parser.ir.Expression>
meth public abstract com.oracle.js.parser.ir.Expression getAssignmentSource()
meth public abstract {com.oracle.js.parser.ir.Assignment%0} getAssignmentDest()

CLSS public abstract com.oracle.js.parser.ir.BaseNode
cons protected init(com.oracle.js.parser.ir.BaseNode,com.oracle.js.parser.ir.Expression,boolean,boolean,boolean)
cons public init(long,int,com.oracle.js.parser.ir.Expression,boolean,boolean,boolean)
fld protected final com.oracle.js.parser.ir.Expression base
intf com.oracle.js.parser.ir.FunctionCall
meth public abstract com.oracle.js.parser.ir.BaseNode setIsSuper()
meth public boolean isFunction()
meth public boolean isIndex()
meth public boolean isSuper()
meth public com.oracle.js.parser.ir.Expression getBase()
meth public final boolean isOptional()
meth public final boolean isOptionalChain()
supr com.oracle.js.parser.ir.OptionalExpression
hfds isFunction,isSuper,optional,optionalChain

CLSS public final com.oracle.js.parser.ir.BinaryNode
cons public init(long,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression)
intf com.oracle.js.parser.ir.Assignment<com.oracle.js.parser.ir.Expression>
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAlwaysFalse()
meth public boolean isAlwaysTrue()
meth public boolean isAssignment()
meth public boolean isComparison()
meth public boolean isLogical()
meth public boolean isRelational()
meth public boolean isSelfModifying()
meth public com.oracle.js.parser.ir.BinaryNode setLHS(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.BinaryNode setRHS(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Expression getAssignmentDest()
meth public com.oracle.js.parser.ir.Expression getAssignmentSource()
meth public com.oracle.js.parser.ir.Expression getLhs()
meth public com.oracle.js.parser.ir.Expression getRhs()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public static boolean isLogical(com.oracle.js.parser.TokenType)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds lhs,rhs

CLSS public com.oracle.js.parser.ir.Block
cons public !varargs init(long,int,int,com.oracle.js.parser.ir.Scope,com.oracle.js.parser.ir.Statement[])
cons public init(long,int,int,com.oracle.js.parser.ir.Scope,java.util.List<com.oracle.js.parser.ir.Statement>)
fld protected final com.oracle.js.parser.ir.Scope scope
fld protected final int flags
fld protected final java.util.List<com.oracle.js.parser.ir.Statement> statements
fld public final static int IS_BODY = 32
fld public final static int IS_EXPRESSION_BLOCK = 256
fld public final static int IS_GLOBAL_SCOPE = 8
fld public final static int IS_MODULE_BODY = 512
fld public final static int IS_PARAMETER_BLOCK = 64
fld public final static int IS_SWITCH_BLOCK = 128
fld public final static int IS_SYNTHETIC = 16
fld public final static int IS_TERMINAL = 4
fld public final static int NEEDS_SCOPE = 1
intf com.oracle.js.parser.ir.BreakableNode
intf com.oracle.js.parser.ir.Flags<com.oracle.js.parser.ir.Block>
intf com.oracle.js.parser.ir.LexicalContextScope
intf com.oracle.js.parser.ir.Terminal
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean getFlag(int)
meth public boolean hasSymbol(java.lang.String)
meth public boolean isBreakableWithoutLabel()
meth public boolean isCatchBlock()
meth public boolean isExpressionBlock()
meth public boolean isFunctionBody()
meth public boolean isGlobalScope()
meth public boolean isModuleBody()
meth public boolean isParameterBlock()
meth public boolean isSwitchBlock()
meth public boolean isSynthetic()
meth public boolean isTerminal()
meth public boolean needsScope()
meth public com.oracle.js.parser.ir.Block setFlag(com.oracle.js.parser.ir.LexicalContext,int)
meth public com.oracle.js.parser.ir.Block setFlags(com.oracle.js.parser.ir.LexicalContext,int)
meth public com.oracle.js.parser.ir.Block setStatements(com.oracle.js.parser.ir.LexicalContext,java.util.List<com.oracle.js.parser.ir.Statement>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Scope getScope()
meth public com.oracle.js.parser.ir.Statement getFirstStatement()
meth public com.oracle.js.parser.ir.Statement getLastStatement()
meth public com.oracle.js.parser.ir.Symbol getExistingSymbol(java.lang.String)
meth public int getFirstStatementLineNumber()
meth public int getFlags()
meth public int getStatementCount()
meth public int getSymbolCount()
meth public java.lang.Iterable<com.oracle.js.parser.ir.Symbol> getSymbols()
meth public java.util.List<com.oracle.js.parser.ir.Statement> getStatements()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node

CLSS public com.oracle.js.parser.ir.BlockExpression
cons public init(long,int,com.oracle.js.parser.ir.Block)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Block getBlock()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds block

CLSS public com.oracle.js.parser.ir.BlockStatement
cons public init(int,com.oracle.js.parser.ir.Block)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isSynthetic()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getBlock()
meth public com.oracle.js.parser.ir.BlockStatement setBlock(com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds block

CLSS public final com.oracle.js.parser.ir.BreakNode
cons public init(int,long,int,java.lang.String)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
supr com.oracle.js.parser.ir.JumpStatement

CLSS public abstract interface com.oracle.js.parser.ir.BreakableNode
intf com.oracle.js.parser.ir.LexicalContextNode
meth public abstract boolean isBreakableWithoutLabel()

CLSS public final com.oracle.js.parser.ir.CallNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isApplyArguments()
meth public boolean isEval()
meth public boolean isImport()
meth public boolean isNew()
meth public boolean isOptional()
meth public boolean isOptionalChain()
meth public com.oracle.js.parser.ir.CallNode setArgs(java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.CallNode setFunction(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Expression getFunction()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getLineNumber()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getArgs()
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean)
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.Expression forImport(int,long,int,int,com.oracle.js.parser.ir.IdentNode,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.Expression forNew(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.OptionalExpression
hfds IS_APPLY_ARGUMENTS,IS_EVAL,IS_IMPORT,IS_NEW,IS_OPTIONAL,IS_OPTIONAL_CHAIN,args,flags,function,lineNumber

CLSS public final com.oracle.js.parser.ir.CaseNode
cons public init(long,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Statement>)
fld protected final java.util.List<com.oracle.js.parser.ir.Statement> statements
intf com.oracle.js.parser.ir.Terminal
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.CaseNode setStatements(java.util.List<com.oracle.js.parser.ir.Statement>)
meth public com.oracle.js.parser.ir.CaseNode setTest(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Expression getTest()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Statement> getStatements()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds terminal,test

CLSS public final com.oracle.js.parser.ir.CatchNode
cons public init(int,long,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Block,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isSyntheticRethrow()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.CatchNode setDestructuringPattern(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.CatchNode setException(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.CatchNode setExceptionCondition(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Expression getDestructuringPattern()
meth public com.oracle.js.parser.ir.Expression getException()
meth public com.oracle.js.parser.ir.Expression getExceptionCondition()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds body,exception,exceptionCondition,isSyntheticRethrow,pattern

CLSS public com.oracle.js.parser.ir.ClassNode
cons public init(long,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.PropertyNode,java.util.List<com.oracle.js.parser.ir.PropertyNode>,com.oracle.js.parser.ir.Scope,int,int,boolean,boolean)
fld public final static java.lang.String PRIVATE_CONSTRUCTOR_BINDING_NAME = "#constructor"
intf com.oracle.js.parser.ir.LexicalContextNode
intf com.oracle.js.parser.ir.LexicalContextScope
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasInstanceFields()
meth public boolean hasPrivateInstanceMethods()
meth public boolean hasPrivateMethods()
meth public boolean hasStaticFields()
meth public boolean isAnonymous()
meth public com.oracle.js.parser.ir.ClassNode setClassElements(java.util.List<com.oracle.js.parser.ir.PropertyNode>)
meth public com.oracle.js.parser.ir.ClassNode setConstructor(com.oracle.js.parser.ir.PropertyNode)
meth public com.oracle.js.parser.ir.Expression getClassHeritage()
meth public com.oracle.js.parser.ir.IdentNode getIdent()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.PropertyNode getConstructor()
meth public com.oracle.js.parser.ir.Scope getScope()
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getInstanceFieldCount()
meth public int getStaticFieldCount()
meth public java.util.List<com.oracle.js.parser.ir.PropertyNode> getClassElements()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds classElements,classHeritage,constructor,hasPrivateInstanceMethods,hasPrivateMethods,ident,instanceFieldCount,scope,staticFieldCount

CLSS public com.oracle.js.parser.ir.ContinueNode
cons public init(int,long,int,java.lang.String)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
supr com.oracle.js.parser.ir.JumpStatement

CLSS public final com.oracle.js.parser.ir.DebuggerNode
cons public init(int,long,int)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement

CLSS public final com.oracle.js.parser.ir.EmptyNode
cons public init(int,long,int)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement

CLSS public com.oracle.js.parser.ir.ErrorNode
cons public init(long,int)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression

CLSS public com.oracle.js.parser.ir.ExportNode
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,boolean)
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.FromNode)
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.VarNode)
cons public init(long,int,int,com.oracle.js.parser.ir.NamedExportsNode,com.oracle.js.parser.ir.FromNode)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isDefault()
meth public com.oracle.js.parser.ir.ExportNode setExportClause(com.oracle.js.parser.ir.NamedExportsNode)
meth public com.oracle.js.parser.ir.ExportNode setFrom(com.oracle.js.parser.ir.FromNode)
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.FromNode getFrom()
meth public com.oracle.js.parser.ir.IdentNode getExportIdentifier()
meth public com.oracle.js.parser.ir.NamedExportsNode getNamedExports()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.VarNode getVar()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds exportIdent,expression,from,isDefault,namedExports,var

CLSS public com.oracle.js.parser.ir.ExportSpecifierNode
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.IdentNode)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.ExportSpecifierNode setExportIdentifier(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.ExportSpecifierNode setIdentifier(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.IdentNode getExportIdentifier()
meth public com.oracle.js.parser.ir.IdentNode getIdentifier()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds exportIdentifier,identifier

CLSS public abstract com.oracle.js.parser.ir.Expression
meth public boolean isAlwaysFalse()
meth public boolean isAlwaysTrue()
meth public boolean isSelfModifying()
meth public final boolean isParenthesized()
meth public final int getFinishWithoutParens()
meth public final int getStartWithoutParens()
meth public final void makeParenthesized(int,int)
meth public int getFinish()
meth public int getStart()
supr com.oracle.js.parser.ir.Node
hfds parensFinish,parensStart,parenthesized

CLSS public com.oracle.js.parser.ir.ExpressionList
cons public init(long,int,java.util.List<? extends com.oracle.js.parser.ir.Expression>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<? extends com.oracle.js.parser.ir.Expression> getExpressions()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds expressions

CLSS public final com.oracle.js.parser.ir.ExpressionStatement
cons public init(int,long,int,com.oracle.js.parser.ir.Expression)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.ExpressionStatement setExpression(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds expression

CLSS public abstract interface com.oracle.js.parser.ir.Flags<%0 extends com.oracle.js.parser.ir.LexicalContextNode>
meth public abstract boolean getFlag(int)
meth public abstract int getFlags()
meth public abstract {com.oracle.js.parser.ir.Flags%0} setFlag(com.oracle.js.parser.ir.LexicalContext,int)
meth public abstract {com.oracle.js.parser.ir.Flags%0} setFlags(com.oracle.js.parser.ir.LexicalContext,int)

CLSS public final com.oracle.js.parser.ir.ForNode
cons public init(int,long,int,com.oracle.js.parser.ir.Block,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.JoinPredecessorExpression,com.oracle.js.parser.ir.JoinPredecessorExpression)
fld public final static int IS_FOR_AWAIT_OF = 16
fld public final static int IS_FOR_EACH = 2
fld public final static int IS_FOR_IN = 1
fld public final static int IS_FOR_OF = 8
fld public final static int PER_ITERATION_SCOPE = 4
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasGoto()
meth public boolean hasPerIterationScope()
meth public boolean isForAwaitOf()
meth public boolean isForEach()
meth public boolean isForIn()
meth public boolean isForInOrOf()
meth public boolean isForOf()
meth public boolean mustEnter()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.Expression getInit()
meth public com.oracle.js.parser.ir.ForNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.ForNode setControlFlowEscapes(com.oracle.js.parser.ir.LexicalContext,boolean)
meth public com.oracle.js.parser.ir.ForNode setInit(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.ForNode setModify(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public com.oracle.js.parser.ir.ForNode setTest(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public com.oracle.js.parser.ir.JoinPredecessorExpression getModify()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Symbol getIterator()
meth public void setIterator(com.oracle.js.parser.ir.Symbol)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.LoopNode
hfds flags,init,iterator,modify

CLSS public com.oracle.js.parser.ir.FromNode
cons public init(long,int,int,com.oracle.js.parser.ir.LiteralNode<java.lang.String>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.FromNode setModuleSpecifier(com.oracle.js.parser.ir.LiteralNode<java.lang.String>)
meth public com.oracle.js.parser.ir.LiteralNode<java.lang.String> getModuleSpecifier()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds moduleSpecifier

CLSS public abstract interface com.oracle.js.parser.ir.FunctionCall
meth public abstract boolean isFunction()

CLSS public final com.oracle.js.parser.ir.FunctionNode
cons public init(com.oracle.js.parser.Source,int,long,int,long,long,com.oracle.js.parser.ir.IdentNode,java.lang.String,int,int,java.util.List<com.oracle.js.parser.ir.IdentNode>,int,com.oracle.js.parser.ir.Block,java.lang.Object,com.oracle.js.parser.ir.Module,java.lang.String)
fld public final static int DEFINES_ARGUMENTS = 256
fld public final static int HAS_APPLY_ARGUMENTS_CALL = 536870912
fld public final static int HAS_ARROW_EVAL = 134217728
fld public final static int HAS_DIRECT_SUPER = 262144
fld public final static int HAS_EVAL = 32
fld public final static int HAS_FUNCTION_DECLARATIONS = 268435456
fld public final static int HAS_NESTED_EVAL = 64
fld public final static int HAS_NON_SIMPLE_PARAMETER_LIST = 67108864
fld public final static int HAS_SCOPE_BLOCK = 128
fld public final static int IS_ANONYMOUS = 1
fld public final static int IS_ARROW = 65536
fld public final static int IS_ASYNC = 33554432
fld public final static int IS_CLASS_CONSTRUCTOR = 2097152
fld public final static int IS_CLASS_FIELD_INITIALIZER = 1073741824
fld public final static int IS_DECLARED = 2
fld public final static int IS_DERIVED_CONSTRUCTOR = 4194304
fld public final static int IS_GENERATOR = 16777216
fld public final static int IS_GETTER = 2048
fld public final static int IS_METHOD = 1048576
fld public final static int IS_MODULE = 131072
fld public final static int IS_PROGRAM = 8192
fld public final static int IS_SCRIPT = 1024
fld public final static int IS_SETTER = 4096
fld public final static int IS_STATEMENT = 16
fld public final static int IS_STRICT = 4
fld public final static int NEEDS_PARENT_SCOPE = 8800
fld public final static int USES_ANCESTOR_SCOPE = 512
fld public final static int USES_ARGUMENTS = 8
fld public final static int USES_NEW_TARGET = 8388608
fld public final static int USES_SELF_SYMBOL = 16384
fld public final static int USES_SUPER = 524288
fld public final static int USES_THIS = 32768
intf com.oracle.js.parser.ir.Flags<com.oracle.js.parser.ir.FunctionNode>
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean getFlag(int)
meth public boolean hasApplyArgumentsCall()
meth public boolean hasArrowEval()
meth public boolean hasDirectSuper()
meth public boolean hasEval()
meth public boolean hasSimpleParameterList()
meth public boolean isAnonymous()
meth public boolean isArrow()
meth public boolean isAsync()
meth public boolean isClassConstructor()
meth public boolean isClassFieldInitializer()
meth public boolean isDeclared()
meth public boolean isDerivedConstructor()
meth public boolean isGenerator()
meth public boolean isGetter()
meth public boolean isMethod()
meth public boolean isModule()
meth public boolean isNamedFunctionExpression()
meth public boolean isNormal()
meth public boolean isProgram()
meth public boolean isScript()
meth public boolean isSetter()
meth public boolean isStatement()
meth public boolean isStrict()
meth public boolean needsArguments()
meth public boolean needsDynamicScope()
meth public boolean needsNewTarget()
meth public boolean needsSuper()
meth public boolean needsThis()
meth public boolean usesAncestorScope()
meth public boolean usesNewTarget()
meth public boolean usesSuper()
meth public boolean usesThis()
meth public com.oracle.js.parser.Source getSource()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.Block getVarDeclarationBlock()
meth public com.oracle.js.parser.ir.FunctionNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.FunctionNode setFlag(com.oracle.js.parser.ir.LexicalContext,int)
meth public com.oracle.js.parser.ir.FunctionNode setFlags(com.oracle.js.parser.ir.LexicalContext,int)
meth public com.oracle.js.parser.ir.FunctionNode setName(com.oracle.js.parser.ir.LexicalContext,java.lang.String)
meth public com.oracle.js.parser.ir.IdentNode getIdent()
meth public com.oracle.js.parser.ir.Module getModule()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getFlags()
meth public int getId()
meth public int getLength()
meth public int getLineNumber()
meth public int getNumOfParams()
meth public java.lang.Object getEndParserState()
meth public java.lang.String getInternalName()
meth public java.lang.String getName()
meth public java.lang.String getSourceName()
meth public java.util.List<com.oracle.js.parser.ir.IdentNode> getParameters()
meth public long getFirstToken()
meth public long getLastToken()
meth public static java.lang.String getSourceName(com.oracle.js.parser.Source)
meth public void setUsesAncestorScope(boolean)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds HAS_DEEP_EVAL,MAYBE_NEEDS_ARGUMENTS,body,endParserState,firstToken,flags,ident,internalName,lastToken,length,lineNumber,module,name,numOfParams,parameters,source,usesAncestorScope

CLSS public final com.oracle.js.parser.ir.IdentNode
cons public init(long,int,java.lang.String)
intf com.oracle.js.parser.ir.FunctionCall
intf com.oracle.js.parser.ir.PropertyKey
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isApplyArguments()
meth public boolean isArguments()
meth public boolean isCatchParameter()
meth public boolean isDeclaredHere()
meth public boolean isDirectSuper()
meth public boolean isFunction()
meth public boolean isImportMeta()
meth public boolean isInitializedHere()
meth public boolean isInternal()
meth public boolean isMetaProperty()
meth public boolean isNewTarget()
meth public boolean isPrivate()
meth public boolean isPropertyName()
meth public boolean isRestParameter()
meth public boolean isSuper()
meth public boolean isThis()
meth public com.oracle.js.parser.ir.IdentNode setIsApplyArguments()
meth public com.oracle.js.parser.ir.IdentNode setIsArguments()
meth public com.oracle.js.parser.ir.IdentNode setIsCatchParameter()
meth public com.oracle.js.parser.ir.IdentNode setIsDeclaredHere()
meth public com.oracle.js.parser.ir.IdentNode setIsDirectSuper()
meth public com.oracle.js.parser.ir.IdentNode setIsImportMeta()
meth public com.oracle.js.parser.ir.IdentNode setIsInitializedHere()
meth public com.oracle.js.parser.ir.IdentNode setIsNewTarget()
meth public com.oracle.js.parser.ir.IdentNode setIsPrivate()
meth public com.oracle.js.parser.ir.IdentNode setIsPropertyName()
meth public com.oracle.js.parser.ir.IdentNode setIsRestParameter()
meth public com.oracle.js.parser.ir.IdentNode setIsSuper()
meth public com.oracle.js.parser.ir.IdentNode setIsThis()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Symbol getSymbol()
meth public java.lang.String getName()
meth public java.lang.String getPropertyName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds APPLY_ARGUMENTS,ARGUMENTS,CATCH_PARAMETER,DIRECT_SUPER,FUNCTION,IMPORT_META,INITIALIZED_HERE,IS_DECLARED_HERE,NEW_TARGET,PRIVATE_IDENT,PROPERTY_NAME,REST_PARAMETER,SUPER,THIS,flags,name,symbol

CLSS public final com.oracle.js.parser.ir.IfNode
cons public init(int,long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Block,com.oracle.js.parser.ir.Block)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getFail()
meth public com.oracle.js.parser.ir.Block getPass()
meth public com.oracle.js.parser.ir.Expression getTest()
meth public com.oracle.js.parser.ir.IfNode setTest(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds fail,pass,test

CLSS public com.oracle.js.parser.ir.ImportClauseNode
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode)
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.NameSpaceImportNode)
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.NamedImportsNode)
cons public init(long,int,int,com.oracle.js.parser.ir.NameSpaceImportNode)
cons public init(long,int,int,com.oracle.js.parser.ir.NamedImportsNode)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.IdentNode getDefaultBinding()
meth public com.oracle.js.parser.ir.ImportClauseNode setDefaultBinding(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.ImportClauseNode setNameSpaceImport(com.oracle.js.parser.ir.NameSpaceImportNode)
meth public com.oracle.js.parser.ir.ImportClauseNode setNamedImports(com.oracle.js.parser.ir.NamedImportsNode)
meth public com.oracle.js.parser.ir.NameSpaceImportNode getNameSpaceImport()
meth public com.oracle.js.parser.ir.NamedImportsNode getNamedImports()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds defaultBinding,nameSpaceImport,namedImports

CLSS public com.oracle.js.parser.ir.ImportNode
cons public init(long,int,int,com.oracle.js.parser.ir.ImportClauseNode,com.oracle.js.parser.ir.FromNode)
cons public init(long,int,int,com.oracle.js.parser.ir.LiteralNode<java.lang.String>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.FromNode getFrom()
meth public com.oracle.js.parser.ir.ImportClauseNode getImportClause()
meth public com.oracle.js.parser.ir.ImportNode setFrom(com.oracle.js.parser.ir.FromNode)
meth public com.oracle.js.parser.ir.ImportNode setImportClause(com.oracle.js.parser.ir.ImportClauseNode)
meth public com.oracle.js.parser.ir.ImportNode setModuleSpecifier(com.oracle.js.parser.ir.LiteralNode<java.lang.String>)
meth public com.oracle.js.parser.ir.LiteralNode<java.lang.String> getModuleSpecifier()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds from,importClause,moduleSpecifier

CLSS public com.oracle.js.parser.ir.ImportSpecifierNode
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.IdentNode)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.IdentNode getBindingIdentifier()
meth public com.oracle.js.parser.ir.IdentNode getIdentifier()
meth public com.oracle.js.parser.ir.ImportSpecifierNode setBindingIdentifier(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.ImportSpecifierNode setIdentifier(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds bindingIdentifier,identifier

CLSS public final com.oracle.js.parser.ir.IndexNode
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression)
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,boolean,boolean,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Expression getIndex()
meth public com.oracle.js.parser.ir.IndexNode setIndex(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.IndexNode setIsSuper()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.BaseNode
hfds index

CLSS public com.oracle.js.parser.ir.JoinPredecessorExpression
cons public init()
cons public init(com.oracle.js.parser.ir.Expression)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAlwaysFalse()
meth public boolean isAlwaysTrue()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.JoinPredecessorExpression setExpression(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds expression

CLSS public abstract com.oracle.js.parser.ir.JumpStatement
meth public boolean hasGoto()
meth public java.lang.String getLabelName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds labelName

CLSS public final com.oracle.js.parser.ir.LabelNode
cons public init(int,long,int,java.lang.String,com.oracle.js.parser.ir.Block)
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.LabelNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.lang.String getLabelName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds body,labelName

CLSS public com.oracle.js.parser.ir.LexicalContext
cons public init()
meth public <%0 extends com.oracle.js.parser.ir.LexicalContextNode> {%%0} pop({%%0})
meth public <%0 extends com.oracle.js.parser.ir.LexicalContextNode> {%%0} push({%%0})
meth public boolean contains(com.oracle.js.parser.ir.LexicalContextNode)
meth public boolean inModule()
meth public boolean isEmpty()
meth public com.oracle.js.parser.ir.Block getCurrentBlock()
meth public com.oracle.js.parser.ir.ClassNode getCurrentClass()
meth public com.oracle.js.parser.ir.FunctionNode getCurrentFunction()
meth public com.oracle.js.parser.ir.FunctionNode getCurrentNonArrowFunction()
meth public com.oracle.js.parser.ir.LexicalContext copy()
meth public com.oracle.js.parser.ir.LexicalContextNode replace(com.oracle.js.parser.ir.LexicalContextNode,com.oracle.js.parser.ir.LexicalContextNode)
meth public com.oracle.js.parser.ir.Scope getCurrentScope()
meth public java.lang.String toString()
meth public java.util.Iterator<com.oracle.js.parser.ir.Block> getBlocks()
meth public java.util.Iterator<com.oracle.js.parser.ir.FunctionNode> getFunctions()
meth public java.util.Iterator<com.oracle.js.parser.ir.LexicalContextNode> getAllNodes()
supr java.lang.Object
hfds sp,stack
hcls NodeIterator

CLSS public abstract interface com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public abstract com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)

CLSS public abstract interface com.oracle.js.parser.ir.LexicalContextScope
intf com.oracle.js.parser.ir.LexicalContextNode
meth public abstract com.oracle.js.parser.ir.Scope getScope()

CLSS public abstract com.oracle.js.parser.ir.LiteralNode<%0 extends java.lang.Object>
cons protected init(com.oracle.js.parser.ir.LiteralNode<{com.oracle.js.parser.ir.LiteralNode%0}>)
cons protected init(com.oracle.js.parser.ir.LiteralNode<{com.oracle.js.parser.ir.LiteralNode%0}>,{com.oracle.js.parser.ir.LiteralNode%0})
cons protected init(long,int,{com.oracle.js.parser.ir.LiteralNode%0})
fld protected final {com.oracle.js.parser.ir.LiteralNode%0} value
innr public final static ArrayLiteralNode
innr public static PrimitiveLiteralNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isArray()
meth public boolean isString()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public final {com.oracle.js.parser.ir.LiteralNode%0} getValue()
meth public java.lang.Object getObject()
meth public java.lang.String getString()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getElementExpressions()
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.Lexer$LexerToken> newInstance(long,int,com.oracle.js.parser.Lexer$LexerToken)
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]> newInstance(long,int,com.oracle.js.parser.ir.Expression[])
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]> newInstance(long,int,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]> newInstance(long,int,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Boolean> newInstance(long,int,boolean)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Number> newInstance(long,int,java.lang.Number)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Number> newInstance(long,int,java.lang.Number,java.util.function.Function<java.lang.Number,java.lang.String>)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Object> newInstance(long,int)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.String> newInstance(long,java.lang.String)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hcls BooleanLiteralNode,LexerTokenLiteralNode,NullLiteralNode,NumberLiteralNode,StringLiteralNode

CLSS public final static com.oracle.js.parser.ir.LiteralNode$ArrayLiteralNode
 outer com.oracle.js.parser.ir.LiteralNode
cons protected init(long,int,com.oracle.js.parser.ir.Expression[])
cons protected init(long,int,com.oracle.js.parser.ir.Expression[],boolean,boolean,boolean)
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasCoverInitializedName()
meth public boolean hasSpread()
meth public boolean hasTrailingComma()
meth public boolean isArray()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Expression> getElementExpressions()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]>
hfds hasCoverInitializedName,hasSpread,hasTrailingComma

CLSS public static com.oracle.js.parser.ir.LiteralNode$PrimitiveLiteralNode<%0 extends java.lang.Object>
 outer com.oracle.js.parser.ir.LiteralNode
intf com.oracle.js.parser.ir.PropertyKey
meth public java.lang.String getPropertyName()
supr com.oracle.js.parser.ir.LiteralNode<{com.oracle.js.parser.ir.LiteralNode$PrimitiveLiteralNode%0}>

CLSS public abstract com.oracle.js.parser.ir.LoopNode
cons protected init(com.oracle.js.parser.ir.LoopNode,com.oracle.js.parser.ir.JoinPredecessorExpression,com.oracle.js.parser.ir.Block,boolean)
cons protected init(int,long,int,com.oracle.js.parser.ir.Block,com.oracle.js.parser.ir.JoinPredecessorExpression,boolean)
fld protected final boolean controlFlowEscapes
fld protected final com.oracle.js.parser.ir.Block body
fld protected final com.oracle.js.parser.ir.JoinPredecessorExpression test
intf com.oracle.js.parser.ir.BreakableNode
meth public abstract boolean hasPerIterationScope()
meth public abstract boolean mustEnter()
meth public abstract com.oracle.js.parser.ir.Block getBody()
meth public abstract com.oracle.js.parser.ir.LoopNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public abstract com.oracle.js.parser.ir.LoopNode setControlFlowEscapes(com.oracle.js.parser.ir.LexicalContext,boolean)
meth public abstract com.oracle.js.parser.ir.LoopNode setTest(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public boolean isBreakableWithoutLabel()
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isLoop()
meth public boolean isTerminal()
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.JoinPredecessorExpression getTest()
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
supr com.oracle.js.parser.ir.Statement

CLSS public final com.oracle.js.parser.ir.Module
cons public init(java.util.List<java.lang.String>,java.util.List<com.oracle.js.parser.ir.Module$ImportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.ImportNode>,java.util.List<com.oracle.js.parser.ir.ExportNode>)
fld public final static java.lang.String DEFAULT_EXPORT_BINDING_NAME = "*default*"
fld public final static java.lang.String DEFAULT_NAME = "default"
fld public final static java.lang.String NAMESPACE_EXPORT_BINDING_NAME = "*namespace*"
fld public final static java.lang.String STAR_NAME = "*"
innr public final static ExportEntry
innr public final static ImportEntry
meth public java.lang.String toString()
meth public java.util.List<com.oracle.js.parser.ir.ExportNode> getExports()
meth public java.util.List<com.oracle.js.parser.ir.ImportNode> getImports()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getIndirectExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getLocalExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getStarExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ImportEntry> getImportEntries()
meth public java.util.List<java.lang.String> getRequestedModules()
supr java.lang.Object
hfds exports,importEntries,imports,indirectExportEntries,localExportEntries,requestedModules,starExportEntries

CLSS public final static com.oracle.js.parser.ir.Module$ExportEntry
 outer com.oracle.js.parser.ir.Module
meth public com.oracle.js.parser.ir.Module$ExportEntry withFrom(java.lang.String)
meth public java.lang.String getExportName()
meth public java.lang.String getImportName()
meth public java.lang.String getLocalName()
meth public java.lang.String getModuleRequest()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportDefault()
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportDefault(java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportIndirect(java.lang.String,java.lang.String,java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportSpecifier(java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportSpecifier(java.lang.String,java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportStarAsNamespaceFrom(java.lang.String,java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportStarFrom(java.lang.String)
supr java.lang.Object
hfds exportName,importName,localName,moduleRequest

CLSS public final static com.oracle.js.parser.ir.Module$ImportEntry
 outer com.oracle.js.parser.ir.Module
meth public com.oracle.js.parser.ir.Module$ImportEntry withFrom(java.lang.String)
meth public java.lang.String getImportName()
meth public java.lang.String getLocalName()
meth public java.lang.String getModuleRequest()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Module$ImportEntry importDefault(java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importSpecifier(java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importSpecifier(java.lang.String,java.lang.String)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importStarAsNameSpaceFrom(java.lang.String)
supr java.lang.Object
hfds importName,localName,moduleRequest

CLSS public com.oracle.js.parser.ir.NameSpaceImportNode
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.IdentNode getBindingIdentifier()
meth public com.oracle.js.parser.ir.NameSpaceImportNode setBindingIdentifier(com.oracle.js.parser.ir.IdentNode)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds bindingIdentifier

CLSS public com.oracle.js.parser.ir.NamedExportsNode
cons public init(long,int,int,java.util.List<com.oracle.js.parser.ir.ExportSpecifierNode>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.NamedExportsNode setExportSpecifiers(java.util.List<com.oracle.js.parser.ir.ExportSpecifierNode>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.ExportSpecifierNode> getExportSpecifiers()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds exportSpecifiers

CLSS public com.oracle.js.parser.ir.NamedImportsNode
cons public init(long,int,int,java.util.List<com.oracle.js.parser.ir.ImportSpecifierNode>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.NamedImportsNode setImportSpecifiers(java.util.List<com.oracle.js.parser.ir.ImportSpecifierNode>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.ImportSpecifierNode> getImportSpecifiers()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds importSpecifiers

CLSS public abstract com.oracle.js.parser.ir.Node
cons protected init(com.oracle.js.parser.ir.Node)
cons protected init(com.oracle.js.parser.ir.Node,int)
cons protected init(long,int,int)
cons public init(long,int)
fld protected final int finish
fld protected final int start
intf java.lang.Cloneable
meth protected java.lang.Object clone()
meth public abstract <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public abstract com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public abstract void toString(java.lang.StringBuilder,boolean)
meth public boolean isAssignment()
meth public boolean isLoop()
meth public boolean isTokenType(com.oracle.js.parser.TokenType)
meth public com.oracle.js.parser.TokenType tokenType()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String toString()
meth public final java.lang.String toString(boolean)
meth public int getFinish()
meth public int getSourceOrder()
meth public int getStart()
meth public long getToken()
supr java.lang.Object
hfds token

CLSS public final com.oracle.js.parser.ir.ObjectNode
cons public init(long,int,java.util.List<com.oracle.js.parser.ir.PropertyNode>,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasCoverInitializedName()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.PropertyNode> getElements()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds elements,hasCoverInitializedName

CLSS public abstract com.oracle.js.parser.ir.OptionalExpression
cons protected init(com.oracle.js.parser.ir.OptionalExpression)
cons public init(long,int)
cons public init(long,int,int)
meth public abstract boolean isOptional()
meth public abstract boolean isOptionalChain()
supr com.oracle.js.parser.ir.Expression

CLSS public final com.oracle.js.parser.ir.ParameterNode
cons public init(long,int,int)
cons public init(long,int,int,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isRestParameter()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getIndex()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds index,rest

CLSS public abstract interface com.oracle.js.parser.ir.PropertyKey
meth public abstract java.lang.String getPropertyName()

CLSS public final com.oracle.js.parser.ir.PropertyNode
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,boolean,boolean,boolean,boolean)
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,boolean,boolean,boolean,boolean,boolean,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAccessor()
meth public boolean isAnonymousFunctionDefinition()
meth public boolean isClassField()
meth public boolean isComputed()
meth public boolean isCoverInitializedName()
meth public boolean isPrivate()
meth public boolean isProto()
meth public boolean isRest()
meth public boolean isStatic()
meth public com.oracle.js.parser.ir.Expression getKey()
meth public com.oracle.js.parser.ir.Expression getValue()
meth public com.oracle.js.parser.ir.FunctionNode getGetter()
meth public com.oracle.js.parser.ir.FunctionNode getSetter()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.PropertyNode setGetter(com.oracle.js.parser.ir.FunctionNode)
meth public com.oracle.js.parser.ir.PropertyNode setSetter(com.oracle.js.parser.ir.FunctionNode)
meth public com.oracle.js.parser.ir.PropertyNode setValue(com.oracle.js.parser.ir.Expression)
meth public java.lang.String getKeyName()
meth public java.lang.String getPrivateName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds classField,computed,coverInitializedName,getter,isAnonymousFunctionDefinition,isStatic,key,proto,setter,value

CLSS public com.oracle.js.parser.ir.ReturnNode
cons public init(int,long,int,com.oracle.js.parser.ir.Expression)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isInTerminalPosition()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.ReturnNode setExpression(com.oracle.js.parser.ir.Expression)
meth public void setInTerminalPosition(boolean)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds expression,inTerminalPosition

CLSS public com.oracle.js.parser.ir.RuntimeNode
cons public !varargs init(long,int,com.oracle.js.parser.ir.RuntimeNode$Request,com.oracle.js.parser.ir.Expression[])
cons public init(long,int,com.oracle.js.parser.ir.RuntimeNode$Request,java.util.List<com.oracle.js.parser.ir.Expression>)
innr public final static !enum Request
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.RuntimeNode setArgs(java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.RuntimeNode$Request getRequest()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getArgs()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds args,request

CLSS public final static !enum com.oracle.js.parser.ir.RuntimeNode$Request
 outer com.oracle.js.parser.ir.RuntimeNode
fld public final static com.oracle.js.parser.ir.RuntimeNode$Request GET_TEMPLATE_OBJECT
fld public final static com.oracle.js.parser.ir.RuntimeNode$Request REFERENCE_ERROR
fld public final static com.oracle.js.parser.ir.RuntimeNode$Request TO_STRING
meth public com.oracle.js.parser.TokenType getTokenType()
meth public int getArity()
meth public java.lang.Class<?> getReturnType()
meth public static com.oracle.js.parser.ir.RuntimeNode$Request valueOf(java.lang.String)
meth public static com.oracle.js.parser.ir.RuntimeNode$Request[] values()
supr java.lang.Enum<com.oracle.js.parser.ir.RuntimeNode$Request>
hfds arity,returnType,tokenType

CLSS public final com.oracle.js.parser.ir.Scope
fld protected final org.graalvm.collections.EconomicMap<java.lang.String,com.oracle.js.parser.ir.Symbol> symbols
fld protected java.util.List<java.util.Map$Entry<com.oracle.js.parser.ir.VarNode,com.oracle.js.parser.ir.Scope>> hoistableBlockFunctionDeclarations
fld protected java.util.List<java.util.Map$Entry<com.oracle.js.parser.ir.VarNode,com.oracle.js.parser.ir.Scope>> hoistedVarDeclarations
meth public boolean addPrivateName(java.lang.String,int)
meth public boolean findPrivateName(java.lang.String)
meth public boolean hasBlockScopedOrRedeclaredSymbols()
meth public boolean hasDeclarations()
meth public boolean hasHoistedVarDeclarations()
meth public boolean hasSymbol(java.lang.String)
meth public boolean inClassFieldInitializer()
meth public boolean inDerivedConstructor()
meth public boolean inFunction()
meth public boolean inMethod()
meth public boolean isBlockScope()
meth public boolean isCatchParameterScope()
meth public boolean isClassScope()
meth public boolean isEvalScope()
meth public boolean isFunctionBodyScope()
meth public boolean isFunctionParameterScope()
meth public boolean isFunctionTopScope()
meth public boolean isGlobalScope()
meth public boolean isLexicallyDeclaredName(java.lang.String,boolean,boolean)
meth public boolean isModuleScope()
meth public boolean isSwitchBlockScope()
meth public com.oracle.js.parser.ir.Scope getParent()
meth public com.oracle.js.parser.ir.Symbol findBlockScopedSymbolInFunction(java.lang.String)
meth public com.oracle.js.parser.ir.Symbol getExistingSymbol(java.lang.String)
meth public com.oracle.js.parser.ir.Symbol putSymbol(com.oracle.js.parser.ir.Symbol)
meth public com.oracle.js.parser.ir.VarNode verifyHoistedVarDeclarations()
meth public int getSymbolCount()
meth public java.lang.Iterable<com.oracle.js.parser.ir.Symbol> getSymbols()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Scope createBlock(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createCatch(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createClass(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createEval(com.oracle.js.parser.ir.Scope,boolean)
meth public static com.oracle.js.parser.ir.Scope createFunctionBody(com.oracle.js.parser.ir.Scope,int)
meth public static com.oracle.js.parser.ir.Scope createGlobal()
meth public static com.oracle.js.parser.ir.Scope createModule()
meth public static com.oracle.js.parser.ir.Scope createParameter(com.oracle.js.parser.ir.Scope,int)
meth public static com.oracle.js.parser.ir.Scope createSwitchBlock(com.oracle.js.parser.ir.Scope)
meth public void close()
meth public void declareHoistedBlockFunctionDeclarations()
meth public void recordHoistableBlockFunctionDeclaration(com.oracle.js.parser.ir.VarNode,com.oracle.js.parser.ir.Scope)
meth public void recordHoistedVarDeclaration(com.oracle.js.parser.ir.VarNode,com.oracle.js.parser.ir.Scope)
supr java.lang.Object
hfds BLOCK_SCOPE,CATCH_PARAMETER_SCOPE,CLASS_SCOPE,EVAL_SCOPE,FUNCTION_BODY_SCOPE,FUNCTION_PARAMETER_SCOPE,FUNCTION_TOP_SCOPE,GLOBAL_SCOPE,IN_DERIVED_CONSTRUCTOR,IN_FUNCTION,IN_METHOD,IS_CLASS_FIELD_INITIALIZER,MODULE_SCOPE,SWITCH_BLOCK_SCOPE,blockScopedOrRedeclaredSymbols,closed,declaredNames,flags,parent,type

CLSS public abstract com.oracle.js.parser.ir.Statement
cons protected init(com.oracle.js.parser.ir.Statement)
cons public init(int,long,int)
cons public init(int,long,int,int)
intf com.oracle.js.parser.ir.Terminal
meth public boolean hasGoto()
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isTerminal()
meth public final boolean hasTerminalFlags()
meth public int getLineNumber()
supr com.oracle.js.parser.ir.Node
hfds lineNumber

CLSS public final com.oracle.js.parser.ir.SwitchNode
cons public init(int,long,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.CaseNode>,int)
intf com.oracle.js.parser.ir.BreakableNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasDefaultCase()
meth public boolean isBreakableWithoutLabel()
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.CaseNode getDefaultCase()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.SwitchNode setExpression(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Symbol getTag()
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.CaseNode> getCases()
meth public void setTag(com.oracle.js.parser.ir.Symbol)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds cases,defaultCaseIndex,expression,tag

CLSS public final com.oracle.js.parser.ir.Symbol
cons public init(java.lang.String,int)
fld public final static int HAS_BEEN_DECLARED = 1024
fld public final static int IS_BLOCK_FUNCTION_DECLARATION = 65536
fld public final static int IS_CATCH_PARAMETER = 32768
fld public final static int IS_CONST = 2
fld public final static int IS_DECLARED_IN_SWITCH_BLOCK = 8192
fld public final static int IS_FUNCTION_SELF = 128
fld public final static int IS_GLOBAL = 8
fld public final static int IS_HOISTABLE_DECLARATION = 256
fld public final static int IS_HOISTED_BLOCK_FUNCTION = 2048
fld public final static int IS_IMPORT_BINDING = 16384
fld public final static int IS_INTERNAL = 64
fld public final static int IS_LET = 1
fld public final static int IS_PARAM = 16
fld public final static int IS_PRIVATE_NAME = 131072
fld public final static int IS_PRIVATE_NAME_ACCESSOR = 1048576
fld public final static int IS_PRIVATE_NAME_METHOD = 524288
fld public final static int IS_PRIVATE_NAME_STATIC = 262144
fld public final static int IS_PROGRAM_LEVEL = 512
fld public final static int IS_THIS = 32
fld public final static int IS_VAR = 4
fld public final static int IS_VAR_REDECLARED_HERE = 4096
fld public final static int KINDMASK = 7
intf java.lang.Comparable<com.oracle.js.parser.ir.Symbol>
meth public boolean hasBeenDeclared()
meth public boolean isBlockFunctionDeclaration()
meth public boolean isBlockScoped()
meth public boolean isCatchParameter()
meth public boolean isConst()
meth public boolean isDeclaredInSwitchBlock()
meth public boolean isFunctionSelf()
meth public boolean isGlobal()
meth public boolean isHoistableDeclaration()
meth public boolean isHoistedBlockFunctionDeclaration()
meth public boolean isImportBinding()
meth public boolean isInternal()
meth public boolean isLet()
meth public boolean isParam()
meth public boolean isPrivateAccessor()
meth public boolean isPrivateField()
meth public boolean isPrivateMethod()
meth public boolean isPrivateName()
meth public boolean isPrivateNameStatic()
meth public boolean isProgramLevel()
meth public boolean isThis()
meth public boolean isVar()
meth public boolean isVarRedeclaredHere()
meth public int compareTo(com.oracle.js.parser.ir.Symbol)
meth public int getFlags()
meth public int getUseCount()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setHasBeenDeclared()
meth public void setHasBeenDeclared(boolean)
meth public void setHoistedBlockFunctionDeclaration()
supr java.lang.Object
hfds flags,name,useCount

CLSS public abstract interface com.oracle.js.parser.ir.Terminal
meth public abstract boolean isTerminal()

CLSS public final com.oracle.js.parser.ir.TernaryNode
cons public init(long,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.JoinPredecessorExpression,com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Expression getTest()
meth public com.oracle.js.parser.ir.JoinPredecessorExpression getFalseExpression()
meth public com.oracle.js.parser.ir.JoinPredecessorExpression getTrueExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.TernaryNode setFalseExpression(com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public com.oracle.js.parser.ir.TernaryNode setTest(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.TernaryNode setTrueExpression(com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds falseExpr,test,trueExpr

CLSS public final com.oracle.js.parser.ir.ThrowNode
cons public init(int,long,int,com.oracle.js.parser.ir.Expression,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isSyntheticRethrow()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.ThrowNode setExpression(com.oracle.js.parser.ir.Expression)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds expression,isSyntheticRethrow

CLSS public final com.oracle.js.parser.ir.TryNode
cons public init(int,long,int,com.oracle.js.parser.ir.Block,java.util.List<com.oracle.js.parser.ir.Block>,com.oracle.js.parser.ir.Block)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.Block getFinallyBody()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Symbol getException()
meth public com.oracle.js.parser.ir.TryNode setBody(com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.TryNode setCatchBlocks(java.util.List<com.oracle.js.parser.ir.Block>)
meth public com.oracle.js.parser.ir.TryNode setFinallyBody(com.oracle.js.parser.ir.Block)
meth public java.util.List<com.oracle.js.parser.ir.Block> getCatchBlocks()
meth public java.util.List<com.oracle.js.parser.ir.CatchNode> getCatches()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds body,catchBlocks,exception,finallyBody

CLSS public final com.oracle.js.parser.ir.UnaryNode
cons public init(long,com.oracle.js.parser.ir.Expression)
cons public init(long,int,int,com.oracle.js.parser.ir.Expression)
intf com.oracle.js.parser.ir.Assignment<com.oracle.js.parser.ir.Expression>
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAssignment()
meth public boolean isSelfModifying()
meth public com.oracle.js.parser.ir.Expression getAssignmentDest()
meth public com.oracle.js.parser.ir.Expression getAssignmentSource()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.UnaryNode setExpression(com.oracle.js.parser.ir.Expression)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds expression

CLSS public final com.oracle.js.parser.ir.VarNode
cons public init(int,long,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression)
cons public init(int,long,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,int)
cons public init(int,long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,int)
cons public init(int,long,int,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,int)
fld public final static int IS_CONST = 2
fld public final static int IS_DESTRUCTURING = 16
fld public final static int IS_EXPORT = 8
fld public final static int IS_LAST_FUNCTION_DECLARATION = 4
fld public final static int IS_LET = 1
intf com.oracle.js.parser.ir.Assignment<com.oracle.js.parser.ir.IdentNode>
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean getFlag(int)
meth public boolean hasInit()
meth public boolean isAssignment()
meth public boolean isBlockScoped()
meth public boolean isClassDeclaration()
meth public boolean isConst()
meth public boolean isDestructuring()
meth public boolean isExport()
meth public boolean isFunctionDeclaration()
meth public boolean isHoistableDeclaration()
meth public boolean isLet()
meth public com.oracle.js.parser.ir.Expression getAssignmentSource()
meth public com.oracle.js.parser.ir.Expression getInit()
meth public com.oracle.js.parser.ir.IdentNode getAssignmentDest()
meth public com.oracle.js.parser.ir.IdentNode getName()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.VarNode setFlag(int)
meth public int getSourceOrder()
meth public int getSymbolFlags()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds flags,init,name,sourceOrder

CLSS public final com.oracle.js.parser.ir.WhileNode
cons public init(int,long,int,boolean,com.oracle.js.parser.ir.JoinPredecessorExpression,com.oracle.js.parser.ir.Block)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasGoto()
meth public boolean hasPerIterationScope()
meth public boolean isDoWhile()
meth public boolean mustEnter()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.WhileNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.WhileNode setControlFlowEscapes(com.oracle.js.parser.ir.LexicalContext,boolean)
meth public com.oracle.js.parser.ir.WhileNode setTest(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.JoinPredecessorExpression)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.LoopNode
hfds isDoWhile

CLSS public final com.oracle.js.parser.ir.WithNode
cons public init(int,long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Block)
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isCompletionValueNeverEmpty()
meth public boolean isTerminal()
meth public com.oracle.js.parser.ir.Block getBody()
meth public com.oracle.js.parser.ir.Expression getExpression()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.WithNode setBody(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Block)
meth public com.oracle.js.parser.ir.WithNode setExpression(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.Expression)
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Statement
hfds body,expression

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

