#Signature file v4.1
#Version 1.30

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
meth protected com.oracle.js.parser.ir.IdentNode createIdentNode(long,int,com.oracle.truffle.api.strings.TruffleString)
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
meth protected java.util.function.Function<java.lang.Number,com.oracle.truffle.api.strings.TruffleString> getNumberToStringConverter()
meth protected static java.lang.String message(java.lang.String,com.oracle.js.parser.ir.IdentNode)
meth protected void validateLexerToken(com.oracle.js.parser.Lexer$LexerToken)
supr java.lang.Object
hfds MSG_EXPECTED,MSG_EXPECTED_STMT,MSG_PARSER_ERROR,SOURCE_URL_PREFIX

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
intf com.oracle.js.parser.StringPool
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
meth public com.oracle.truffle.api.strings.TruffleString stringIntern(com.oracle.truffle.api.strings.TruffleString)
meth public com.oracle.truffle.api.strings.TruffleString stringIntern(java.lang.String)
meth public com.oracle.truffle.api.strings.TruffleString valueOfRawString(long)
meth public static boolean isJSEOL(char)
meth public static boolean isJSWhitespace(char)
meth public static boolean isStringLineTerminator(char)
meth public void lexify()
supr com.oracle.js.parser.Scanner
hfds JAVASCRIPT_WHITESPACE_HIGH_START,MSG_EDIT_STRING_MISSING_BRACE,MSG_HERE_MISSING_END_MARKER,MSG_HERE_NON_MATCHING_DELIMITER,MSG_ILLEGAL_IDENTIFIER_CHARACTER,MSG_INVALID_ESCAPE_CHAR,MSG_INVALID_HEX,MSG_LEXER_ERROR,MSG_MISSING_CLOSE_QUOTE,MSG_MISSING_SPACE_AFTER_NUMBER,MSG_NUMERIC_LITERAL_MULTIPLE_SEPARATORS,MSG_NUMERIC_LITERAL_TRAILING_SEPARATOR,MSG_STRICT_NO_NONOCTALDECIMAL,MSG_STRICT_NO_OCTAL,XML_LITERALS,allowBigInt,ecmaScriptVersion,internedStrings,isModule,last,linePosition,nested,pauseOnFunctionBody,pauseOnNextLeftBrace,pauseOnRightBrace,pendingLine,scripting,shebang,source,stream
hcls EditStringLexer,State

CLSS public abstract static com.oracle.js.parser.Lexer$LexerToken
 outer com.oracle.js.parser.Lexer
cons protected init(com.oracle.truffle.api.strings.TruffleString)
meth public com.oracle.truffle.api.strings.TruffleString getExpressionTS()
meth public java.lang.String getExpression()
supr java.lang.Object
hfds expression

CLSS protected abstract interface static com.oracle.js.parser.Lexer$LineInfoReceiver
 outer com.oracle.js.parser.Lexer
meth public abstract void lineInfo(int,int)

CLSS public static com.oracle.js.parser.Lexer$RegexToken
 outer com.oracle.js.parser.Lexer
cons public init(com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString)
meth public com.oracle.truffle.api.strings.TruffleString getOptionsTS()
meth public java.lang.String getOptions()
meth public java.lang.String toString()
supr com.oracle.js.parser.Lexer$LexerToken
hfds options

CLSS public static com.oracle.js.parser.Lexer$XMLToken
 outer com.oracle.js.parser.Lexer
cons public init(com.oracle.truffle.api.strings.TruffleString)
supr com.oracle.js.parser.Lexer$LexerToken

CLSS public com.oracle.js.parser.Parser
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager)
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean)
cons public init(com.oracle.js.parser.ScriptEnvironment,com.oracle.js.parser.Source,com.oracle.js.parser.ErrorManager,boolean,int)
fld protected final com.oracle.js.parser.Lexer$LineInfoReceiver lineInfoReceiver
meth public com.oracle.js.parser.ir.Expression parseExpression()
meth public com.oracle.js.parser.ir.FunctionNode parse()
meth public com.oracle.js.parser.ir.FunctionNode parse(com.oracle.truffle.api.strings.TruffleString,int,int,int,com.oracle.js.parser.ir.Scope,java.util.List<java.lang.String>)
meth public com.oracle.js.parser.ir.FunctionNode parseEval(boolean,com.oracle.js.parser.ir.Scope)
meth public com.oracle.js.parser.ir.FunctionNode parseFunctionBody(boolean,boolean)
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String)
meth public com.oracle.js.parser.ir.FunctionNode parseModule(java.lang.String,int,int)
meth public com.oracle.js.parser.ir.FunctionNode parseWithArguments(java.util.List<java.lang.String>)
meth public java.lang.String toString()
meth public java.util.List<com.oracle.js.parser.ir.Expression> decoratorList(boolean,boolean)
meth public void parseFormalParameterList()
meth public void setReparsedFunction(com.oracle.js.parser.RecompilableScriptFunctionData)
supr com.oracle.js.parser.AbstractParser
hfds ANONYMOUS_FUNCTION_NAME,APPLY_NAME,ARGS,ARGUMENTS_NAME,ARGUMENTS_NAME_TS,ARROW_FUNCTION_NAME,CONSTRUCTOR_NAME,CONSTRUCTOR_NAME_TS,CONTEXT_ASSIGNMENT_TARGET,CONTEXT_ASYNC_FUNCTION_DECLARATION,CONTEXT_CATCH_PARAMETER,CONTEXT_CLASS_DECLARATION,CONTEXT_CLASS_NAME,CONTEXT_CONST_DECLARATION,CONTEXT_FOR_IN_ITERATOR,CONTEXT_FOR_OF_ITERATOR,CONTEXT_FUNCTION_DECLARATION,CONTEXT_FUNCTION_NAME,CONTEXT_FUNCTION_PARAMETER,CONTEXT_GENERATOR_FUNCTION_DECLARATION,CONTEXT_IDENTIFIER_REFERENCE,CONTEXT_IMPORTED_BINDING,CONTEXT_IN,CONTEXT_LABEL_IDENTIFIER,CONTEXT_LET_DECLARATION,CONTEXT_OF,CONTEXT_OPERAND_FOR_DEC_OPERATOR,CONTEXT_OPERAND_FOR_INC_OPERATOR,CONTEXT_VARIABLE_NAME,ERROR_BINDING_NAME,ES2019_OPTIONAL_CATCH_BINDING,ES2020_CLASS_FIELDS,ES2022_TOP_LEVEL_AWAIT,ES6_ARROW_FUNCTION,ES6_CLASS,ES6_COMPUTED_PROPERTY_NAME,ES6_DEFAULT_PARAMETER,ES6_DESTRUCTURING,ES6_FOR_OF,ES6_GENERATOR_FUNCTION,ES6_NEW_TARGET,ES6_REST_PARAMETER,ES6_SPREAD_ARGUMENT,ES6_SPREAD_ARRAY,ES8_ASYNC_FUNCTION,ES8_FOR_AWAIT_OF,ES8_REST_SPREAD_PROPERTY,ES8_TRAILING_COMMA,EVAL_NAME,EXEC_NAME,GET_SPC,IMPORT_META_NAME,INITIALIZER_FUNCTION_NAME,META,MSG_ACCESSOR_CONSTRUCTOR,MSG_ARGUMENTS_IN_FIELD_INITIALIZER,MSG_ASYNC_CONSTRUCTOR,MSG_AUTO_ACCESSOR_NOT_FIELD,MSG_CONSTRUCTOR_FIELD,MSG_DECORATED_CONSTRUCTOR,MSG_DECORATED_STATIC_BLOCK,MSG_DUPLICATE_DEFAULT_IN_SWITCH,MSG_DUPLICATE_IMPORT_ATTRIBUTE,MSG_DUPLICATE_LABEL,MSG_ESCAPED_KEYWORD,MSG_EXPECTED_ARROW_PARAMETER,MSG_EXPECTED_BINDING,MSG_EXPECTED_BINDING_IDENTIFIER,MSG_EXPECTED_COMMA,MSG_EXPECTED_IMPORT,MSG_EXPECTED_NAMED_IMPORT,MSG_EXPECTED_OPERAND,MSG_EXPECTED_PROPERTY_ID,MSG_EXPECTED_STMT,MSG_EXPECTED_TARGET,MSG_FOR_EACH_WITHOUT_IN,MSG_FOR_IN_LOOP_INITIALIZER,MSG_GENERATOR_CONSTRUCTOR,MSG_ILLEGAL_BREAK_STMT,MSG_ILLEGAL_CONTINUE_STMT,MSG_INVALID_ARROW_PARAMETER,MSG_INVALID_EXPORT,MSG_INVALID_FOR_AWAIT_OF,MSG_INVALID_LVALUE,MSG_INVALID_PRIVATE_IDENT,MSG_INVALID_PROPERTY_INITIALIZER,MSG_INVALID_RETURN,MSG_INVALID_SUPER,MSG_LET_LEXICAL_BINDING,MSG_MANY_VARS_IN_FOR_IN_LOOP,MSG_MISSING_CATCH_OR_FINALLY,MSG_MISSING_CONST_ASSIGNMENT,MSG_MISSING_DESTRUCTURING_ASSIGNMENT,MSG_MULTIPLE_CONSTRUCTORS,MSG_MULTIPLE_PROTO_KEY,MSG_NEW_TARGET_IN_FUNCTION,MSG_NOT_LVALUE_FOR_IN_LOOP,MSG_NO_FUNC_DECL_HERE,MSG_NO_FUNC_DECL_HERE_WARN,MSG_OPTIONAL_CHAIN_TEMPLATE,MSG_PRIVATE_CONSTRUCTOR_METHOD,MSG_PROPERTY_REDEFINITON,MSG_STATIC_PROTOTYPE_FIELD,MSG_STATIC_PROTOTYPE_METHOD,MSG_STRICT_CANT_DELETE_IDENT,MSG_STRICT_CANT_DELETE_PRIVATE,MSG_STRICT_NAME,MSG_STRICT_NO_FUNC_DECL_HERE,MSG_STRICT_NO_NONOCTALDECIMAL,MSG_STRICT_NO_OCTAL,MSG_STRICT_NO_WITH,MSG_STRICT_PARAM_REDEFINITION,MSG_SYNTAX_ERROR_REDECLARE_VARIABLE,MSG_UNDEFINED_LABEL,MSG_UNEXPECTED_IDENT,MSG_UNEXPECTED_IMPORT_META,MSG_UNEXPECTED_TOKEN,MSG_UNTERMINATED_TEMPLATE_EXPRESSION,MSG_USE_STRICT_NON_SIMPLE_PARAM,NEW_TARGET_NAME,NEW_TARGET_NAME_TS,PARSE_EVAL,PARSE_FUNCTION_CONTEXT_EVAL,PRIVATE_CONSTRUCTOR_NAME,PROFILE_PARSING,PROGRAM_NAME,PROTOTYPE_NAME,PROTO_NAME,REPARSE_IS_METHOD,REPARSE_IS_PROPERTY_ACCESSOR,SET_SPC,SWITCH_BINDING_NAME,TARGET,USE_STRICT,allowBigInt,coverArrowFunction,defaultNames,env,functionDeclarations,isModule,lc,reparsedFunction,scripting,shebang
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
meth public java.lang.String getRawMessage()
meth public long getToken()
meth public void setColumnNumber(int)
meth public void setFileName(java.lang.String)
meth public void setLineNumber(int)
supr java.lang.RuntimeException
hfds column,errorType,fileName,line,source,token

CLSS public final com.oracle.js.parser.ParserStrings
cons public init()
meth public static com.oracle.truffle.api.strings.TruffleString constant(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString fromJavaString(java.lang.String)
supr java.lang.Object

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
hfds allowBigInt,annexB,classFields,constAsVar,dumpOnError,ecmaScriptVersion,emptyStatements,err,functionStatement,importAttributes,privateFieldsIn,scripting,shebang,strict,syntaxExtensions,topLevelAwait,v8Intrinsics

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
meth public com.oracle.js.parser.ScriptEnvironment$Builder importAttributes(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder privateFieldsIn(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder scripting(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder shebang(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder strict(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder syntaxExtensions(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder topLevelAwait(boolean)
meth public com.oracle.js.parser.ScriptEnvironment$Builder v8Intrinsics(boolean)
supr java.lang.Object
hfds allowBigInt,annexB,classFields,constAsVar,dumpOnError,ecmaScriptVersion,emptyStatements,functionStatementBehavior,importAttributes,privateFieldsIn,scripting,shebang,strict,syntaxExtensions,topLevelAwait,v8Intrinsics

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

CLSS public abstract interface com.oracle.js.parser.StringPool
meth public abstract com.oracle.truffle.api.strings.TruffleString stringIntern(com.oracle.truffle.api.strings.TruffleString)

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
meth public static com.oracle.js.parser.TokenType lookupKeyword(java.lang.String,int,int)
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
fld public final static com.oracle.js.parser.TokenType ACCESSOR
fld public final static com.oracle.js.parser.TokenType ADD
fld public final static com.oracle.js.parser.TokenType AND
fld public final static com.oracle.js.parser.TokenType ARRAY
fld public final static com.oracle.js.parser.TokenType ARROW
fld public final static com.oracle.js.parser.TokenType AS
fld public final static com.oracle.js.parser.TokenType ASSERT
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
fld public final static com.oracle.js.parser.TokenType NAMEDEVALUATION
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
meth public com.oracle.truffle.api.strings.TruffleString getNameTS()
meth public int getECMAScriptVersion()
meth public int getLength()
meth public int getPrecedence()
meth public java.lang.String getName()
meth public java.lang.String getNameOrType()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.TokenType valueOf(java.lang.String)
meth public static com.oracle.js.parser.TokenType[] values()
supr java.lang.Enum<com.oracle.js.parser.TokenType>
hfds ecmaScriptVersion,isLeftAssociative,kind,name,nameTS,next,precedence,tokenValues

CLSS public final com.oracle.js.parser.ir.AccessNode
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.truffle.api.strings.TruffleString)
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.truffle.api.strings.TruffleString,boolean,boolean,boolean,boolean)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isPrivate()
meth public com.oracle.js.parser.ir.AccessNode setIsSuper()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.truffle.api.strings.TruffleString getPrivateNameTS()
meth public com.oracle.truffle.api.strings.TruffleString getPropertyTS()
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
meth public boolean isDefaultDerivedConstructorSuperCall()
meth public boolean isEval()
meth public boolean isImport()
meth public boolean isNew()
meth public boolean isOptional()
meth public boolean isOptionalChain()
meth public boolean isTaggedTemplateLiteral()
meth public com.oracle.js.parser.ir.CallNode setArgs(java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.CallNode setFunction(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Expression getFunction()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getLineNumber()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getArgs()
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean)
meth public static com.oracle.js.parser.ir.Expression forCall(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.Expression forImport(int,long,int,int,com.oracle.js.parser.ir.IdentNode,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.Expression forNew(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.Expression forTaggedTemplateLiteral(int,long,int,int,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.OptionalExpression
hfds IS_APPLY_ARGUMENTS,IS_DEFAULT_DERIVED_CONSTRUCTOR_SUPER_CALL,IS_EVAL,IS_IMPORT,IS_NEW,IS_OPTIONAL,IS_OPTIONAL_CHAIN,IS_TAGGED_TEMPLATE_LITERAL,args,flags,function,lineNumber

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

CLSS public final com.oracle.js.parser.ir.ClassElement
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAccessor()
meth public boolean isAutoAccessor()
meth public boolean isClassField()
meth public boolean isClassFieldOrAutoAccessor()
meth public boolean isClassStaticBlock()
meth public boolean isMethod()
meth public boolean isMethodOrAccessor()
meth public boolean isPrivate()
meth public boolean isStatic()
meth public com.oracle.js.parser.ir.ClassElement setDecorators(java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.ClassElement setGetter(com.oracle.js.parser.ir.FunctionNode)
meth public com.oracle.js.parser.ir.ClassElement setKey(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.ClassElement setSetter(com.oracle.js.parser.ir.FunctionNode)
meth public com.oracle.js.parser.ir.ClassElement setValue(com.oracle.js.parser.ir.Expression)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Expression> getDecorators()
meth public static com.oracle.js.parser.ir.ClassElement createAccessor(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.ClassElement createAutoAccessor(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.ClassElement createDefaultConstructor(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression)
meth public static com.oracle.js.parser.ir.ClassElement createField(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean,boolean)
meth public static com.oracle.js.parser.ir.ClassElement createMethod(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean)
meth public static com.oracle.js.parser.ir.ClassElement createStaticInitializer(long,int,com.oracle.js.parser.ir.FunctionNode)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.PropertyNode
hfds KIND_ACCESSOR,KIND_AUTO_ACCESSOR,KIND_FIELD,KIND_METHOD,KIND_STATIC_INIT,decorators,kind

CLSS public com.oracle.js.parser.ir.ClassNode
cons public init(long,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.ClassElement,java.util.List<com.oracle.js.parser.ir.ClassElement>,java.util.List<com.oracle.js.parser.ir.Expression>,com.oracle.js.parser.ir.Scope,int,boolean,boolean,boolean,boolean)
fld public final static com.oracle.truffle.api.strings.TruffleString PRIVATE_CONSTRUCTOR_BINDING_NAME
intf com.oracle.js.parser.ir.LexicalContextNode
intf com.oracle.js.parser.ir.LexicalContextScope
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasClassElementDecorators()
meth public boolean hasInstanceFieldsOrAccessors()
meth public boolean hasPrivateInstanceMethods()
meth public boolean hasPrivateMethods()
meth public boolean hasStaticElements()
meth public boolean isAnonymous()
meth public boolean needsInitializeInstanceElements()
meth public com.oracle.js.parser.ir.ClassElement getConstructor()
meth public com.oracle.js.parser.ir.ClassNode setClassElements(java.util.List<com.oracle.js.parser.ir.ClassElement>)
meth public com.oracle.js.parser.ir.ClassNode setConstructor(com.oracle.js.parser.ir.ClassElement)
meth public com.oracle.js.parser.ir.ClassNode setDecorators(java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.Expression getClassHeritage()
meth public com.oracle.js.parser.ir.IdentNode getIdent()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Scope getClassHeadScope()
meth public com.oracle.js.parser.ir.Scope getScope()
meth public final <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public final com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public int getInstanceElementCount()
meth public int getStaticElementCount()
meth public java.util.List<com.oracle.js.parser.ir.ClassElement> getClassElements()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getDecorators()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds classDecorators,classElements,classHeritage,constructor,hasClassElementDecorators,hasInstanceFieldsOrAccessors,hasPrivateInstanceMethods,hasPrivateMethods,ident,scope,staticElementCount

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
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.FromNode,java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString>)
cons public init(long,int,int,com.oracle.js.parser.ir.IdentNode,com.oracle.js.parser.ir.VarNode)
cons public init(long,int,int,com.oracle.js.parser.ir.NamedExportsNode,com.oracle.js.parser.ir.FromNode,java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString>)
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
meth public java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString> getAssertions()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds assertions,exportIdent,expression,from,isDefault,namedExports,var

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
cons public init(long,int,int,com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.FromNode setModuleSpecifier(com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString>)
meth public com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString> getModuleSpecifier()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds moduleSpecifier

CLSS public abstract interface com.oracle.js.parser.ir.FunctionCall
meth public abstract boolean isFunction()

CLSS public final com.oracle.js.parser.ir.FunctionNode
cons public init(com.oracle.js.parser.Source,int,long,int,long,long,com.oracle.js.parser.ir.IdentNode,com.oracle.truffle.api.strings.TruffleString,int,int,java.util.List<com.oracle.js.parser.ir.IdentNode>,int,com.oracle.js.parser.ir.Block,java.lang.Object,com.oracle.js.parser.ir.Module,com.oracle.truffle.api.strings.TruffleString)
fld public final static int ARROW_HEAD_FLAGS = 134791400
fld public final static int DEFINES_ARGUMENTS = 256
fld public final static int HAS_APPLY_ARGUMENTS_CALL = 536870912
fld public final static int HAS_ARROW_EVAL = 134217728
fld public final static int HAS_CLOSURES = 16384
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
fld public final static int NO_FUNCTION_SELF = 1056771
fld public final static int USES_ANCESTOR_SCOPE = 512
fld public final static int USES_ARGUMENTS = 8
fld public final static int USES_NEW_TARGET = 8388608
fld public final static int USES_SUPER = 524288
fld public final static int USES_THIS = 32768
intf com.oracle.js.parser.ir.Flags<com.oracle.js.parser.ir.FunctionNode>
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean getFlag(int)
meth public boolean hasApplyArgumentsCall()
meth public boolean hasArrowEval()
meth public boolean hasClosures()
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
meth public com.oracle.js.parser.ir.FunctionNode setName(com.oracle.js.parser.ir.LexicalContext,com.oracle.truffle.api.strings.TruffleString)
meth public com.oracle.js.parser.ir.IdentNode getIdent()
meth public com.oracle.js.parser.ir.Module getModule()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.truffle.api.strings.TruffleString getInternalNameTS()
meth public com.oracle.truffle.api.strings.TruffleString getNameTS()
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
cons public init(long,int,com.oracle.truffle.api.strings.TruffleString)
intf com.oracle.js.parser.ir.FunctionCall
intf com.oracle.js.parser.ir.PropertyKey
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isApplyArguments()
meth public boolean isArguments()
meth public boolean isCatchParameter()
meth public boolean isDeclaredHere()
meth public boolean isDirectSuper()
meth public boolean isFunction()
meth public boolean isIgnoredParameter()
meth public boolean isImportMeta()
meth public boolean isInitializedHere()
meth public boolean isInternal()
meth public boolean isMetaProperty()
meth public boolean isNewTarget()
meth public boolean isPrivate()
meth public boolean isPrivateInCheck()
meth public boolean isPropertyName()
meth public boolean isRestParameter()
meth public boolean isSuper()
meth public boolean isThis()
meth public com.oracle.js.parser.ir.IdentNode setIsApplyArguments()
meth public com.oracle.js.parser.ir.IdentNode setIsArguments()
meth public com.oracle.js.parser.ir.IdentNode setIsCatchParameter()
meth public com.oracle.js.parser.ir.IdentNode setIsDeclaredHere()
meth public com.oracle.js.parser.ir.IdentNode setIsDirectSuper()
meth public com.oracle.js.parser.ir.IdentNode setIsIgnoredParameter()
meth public com.oracle.js.parser.ir.IdentNode setIsImportMeta()
meth public com.oracle.js.parser.ir.IdentNode setIsInitializedHere()
meth public com.oracle.js.parser.ir.IdentNode setIsNewTarget()
meth public com.oracle.js.parser.ir.IdentNode setIsPrivate()
meth public com.oracle.js.parser.ir.IdentNode setIsPrivateInCheck()
meth public com.oracle.js.parser.ir.IdentNode setIsPropertyName()
meth public com.oracle.js.parser.ir.IdentNode setIsRestParameter()
meth public com.oracle.js.parser.ir.IdentNode setIsSuper()
meth public com.oracle.js.parser.ir.IdentNode setIsThis()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Symbol getSymbol()
meth public com.oracle.truffle.api.strings.TruffleString getNameTS()
meth public com.oracle.truffle.api.strings.TruffleString getPropertyNameTS()
meth public java.lang.String getName()
meth public java.lang.String getPropertyName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds APPLY_ARGUMENTS,ARGUMENTS,CATCH_PARAMETER,DIRECT_SUPER,FUNCTION,IGNORED_PARAMETER,IMPORT_META,INITIALIZED_HERE,IS_DECLARED_HERE,NEW_TARGET,PRIVATE_IDENT,PRIVATE_IN_CHECK,PROPERTY_NAME,REST_PARAMETER,SUPER,THIS,flags,name,nameTS,symbol

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
cons public init(long,int,int,com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.FromNode getFrom()
meth public com.oracle.js.parser.ir.ImportClauseNode getImportClause()
meth public com.oracle.js.parser.ir.ImportNode setFrom(com.oracle.js.parser.ir.FromNode)
meth public com.oracle.js.parser.ir.ImportNode setImportClause(com.oracle.js.parser.ir.ImportClauseNode)
meth public com.oracle.js.parser.ir.ImportNode setModuleSpecifier(com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString>)
meth public com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString> getModuleSpecifier()
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
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]> newInstance(long,int,java.util.List<com.oracle.js.parser.ir.Expression>,boolean,boolean)
meth public static com.oracle.js.parser.ir.LiteralNode<com.oracle.truffle.api.strings.TruffleString> newInstance(long,com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Boolean> newInstance(long,int,boolean)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Number> newInstance(long,int,java.lang.Number)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Number> newInstance(long,int,java.lang.Number,java.util.function.Function<java.lang.Number,com.oracle.truffle.api.strings.TruffleString>)
meth public static com.oracle.js.parser.ir.LiteralNode<java.lang.Object> newInstance(long,int)
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hcls BooleanLiteralNode,LexerTokenLiteralNode,NullLiteralNode,NumberLiteralNode,StringLiteralNode

CLSS public final static com.oracle.js.parser.ir.LiteralNode$ArrayLiteralNode
 outer com.oracle.js.parser.ir.LiteralNode
cons protected init(long,int,com.oracle.js.parser.ir.Expression[])
cons protected init(long,int,com.oracle.js.parser.ir.Expression[],boolean,boolean)
intf com.oracle.js.parser.ir.LexicalContextNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean hasSpread()
meth public boolean hasTrailingComma()
meth public boolean isArray()
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.LexicalContext,com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Expression> getElementExpressions()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.LiteralNode<com.oracle.js.parser.ir.Expression[]>
hfds hasSpread,hasTrailingComma

CLSS public static com.oracle.js.parser.ir.LiteralNode$PrimitiveLiteralNode<%0 extends java.lang.Object>
 outer com.oracle.js.parser.ir.LiteralNode
intf com.oracle.js.parser.ir.PropertyKey
meth public com.oracle.truffle.api.strings.TruffleString getPropertyNameTS()
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
cons public init(java.util.List<com.oracle.js.parser.ir.Module$ModuleRequest>,java.util.List<com.oracle.js.parser.ir.Module$ImportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.Module$ExportEntry>,java.util.List<com.oracle.js.parser.ir.ImportNode>,java.util.List<com.oracle.js.parser.ir.ExportNode>)
fld public final static com.oracle.truffle.api.strings.TruffleString DEFAULT_EXPORT_BINDING_NAME
fld public final static com.oracle.truffle.api.strings.TruffleString DEFAULT_NAME
fld public final static com.oracle.truffle.api.strings.TruffleString NAMESPACE_EXPORT_BINDING_NAME
fld public final static com.oracle.truffle.api.strings.TruffleString STAR_NAME
innr public final static ExportEntry
innr public final static ImportEntry
innr public final static ModuleRequest
meth public java.lang.String toString()
meth public java.util.List<com.oracle.js.parser.ir.ExportNode> getExports()
meth public java.util.List<com.oracle.js.parser.ir.ImportNode> getImports()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getIndirectExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getLocalExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ExportEntry> getStarExportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ImportEntry> getImportEntries()
meth public java.util.List<com.oracle.js.parser.ir.Module$ModuleRequest> getRequestedModules()
supr java.lang.Object
hfds exports,importEntries,imports,indirectExportEntries,localExportEntries,requestedModules,starExportEntries

CLSS public final static com.oracle.js.parser.ir.Module$ExportEntry
 outer com.oracle.js.parser.ir.Module
meth public com.oracle.js.parser.ir.Module$ExportEntry withFrom(com.oracle.js.parser.ir.Module$ModuleRequest)
meth public com.oracle.js.parser.ir.Module$ModuleRequest getModuleRequest()
meth public com.oracle.truffle.api.strings.TruffleString getExportName()
meth public com.oracle.truffle.api.strings.TruffleString getImportName()
meth public com.oracle.truffle.api.strings.TruffleString getLocalName()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportDefault(com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportIndirect(com.oracle.truffle.api.strings.TruffleString,com.oracle.js.parser.ir.Module$ModuleRequest,com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportSpecifier(com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportSpecifier(com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportStarAsNamespaceFrom(com.oracle.truffle.api.strings.TruffleString,com.oracle.js.parser.ir.Module$ModuleRequest)
meth public static com.oracle.js.parser.ir.Module$ExportEntry exportStarFrom(com.oracle.js.parser.ir.Module$ModuleRequest)
supr java.lang.Object
hfds exportName,importName,localName,moduleRequest

CLSS public final static com.oracle.js.parser.ir.Module$ImportEntry
 outer com.oracle.js.parser.ir.Module
meth public com.oracle.js.parser.ir.Module$ImportEntry withFrom(com.oracle.js.parser.ir.Module$ModuleRequest)
meth public com.oracle.js.parser.ir.Module$ModuleRequest getModuleRequest()
meth public com.oracle.truffle.api.strings.TruffleString getImportName()
meth public com.oracle.truffle.api.strings.TruffleString getLocalName()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Module$ImportEntry importDefault(com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importSpecifier(com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importSpecifier(com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ImportEntry importStarAsNameSpaceFrom(com.oracle.truffle.api.strings.TruffleString)
supr java.lang.Object
hfds importName,localName,moduleRequest

CLSS public final static com.oracle.js.parser.ir.Module$ModuleRequest
 outer com.oracle.js.parser.ir.Module
meth public com.oracle.truffle.api.strings.TruffleString getSpecifier()
meth public java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString> getAttributes()
meth public static com.oracle.js.parser.ir.Module$ModuleRequest create(com.oracle.truffle.api.strings.TruffleString)
meth public static com.oracle.js.parser.ir.Module$ModuleRequest create(com.oracle.truffle.api.strings.TruffleString,java.util.Map$Entry<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString>[])
meth public static com.oracle.js.parser.ir.Module$ModuleRequest create(com.oracle.truffle.api.strings.TruffleString,java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString>)
meth public void setAttributes(java.util.Map<com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.TruffleString>)
supr java.lang.Object
hfds attributes,specifier

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
cons public init(long,int,java.util.List<com.oracle.js.parser.ir.PropertyNode>)
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.PropertyNode> getElements()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Expression
hfds elements

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
meth public abstract com.oracle.truffle.api.strings.TruffleString getPropertyNameTS()
meth public abstract java.lang.String getPropertyName()

CLSS public com.oracle.js.parser.ir.PropertyNode
cons protected init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,boolean,boolean,boolean)
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,boolean,boolean,boolean,boolean)
cons public init(long,int,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.Expression,com.oracle.js.parser.ir.FunctionNode,com.oracle.js.parser.ir.FunctionNode,boolean,boolean,boolean,boolean,boolean,boolean)
fld protected final boolean computed
fld protected final boolean isAnonymousFunctionDefinition
fld protected final boolean isStatic
fld protected final com.oracle.js.parser.ir.Expression key
fld protected final com.oracle.js.parser.ir.Expression value
fld protected final com.oracle.js.parser.ir.FunctionNode getter
fld protected final com.oracle.js.parser.ir.FunctionNode setter
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public boolean isAccessor()
meth public boolean isAnonymousFunctionDefinition()
meth public boolean isClassField()
meth public boolean isClassStaticBlock()
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
meth public com.oracle.truffle.api.strings.TruffleString getKeyNameTS()
meth public com.oracle.truffle.api.strings.TruffleString getPrivateNameTS()
meth public java.lang.String getKeyName()
meth public java.lang.String getPrivateName()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.Node
hfds classField,coverInitializedName,proto

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

CLSS public final com.oracle.js.parser.ir.Scope
fld protected final org.graalvm.collections.EconomicMap<java.lang.String,com.oracle.js.parser.ir.Symbol> symbols
fld protected org.graalvm.collections.EconomicMap<java.lang.String,com.oracle.js.parser.ir.Scope$UseInfo> uses
meth public boolean addPrivateName(com.oracle.truffle.api.strings.TruffleString,int)
meth public boolean findPrivateName(java.lang.String)
meth public boolean hasBlockScopedOrRedeclaredSymbols()
meth public boolean hasClosures()
meth public boolean hasDeclarations()
meth public boolean hasEval()
meth public boolean hasNestedEval()
meth public boolean hasPrivateNames()
meth public boolean hasSymbol(java.lang.String)
meth public boolean inClassFieldInitializer()
meth public boolean inDerivedConstructor()
meth public boolean inFunction()
meth public boolean inMethod()
meth public boolean isArrowFunctionParameterScope()
meth public boolean isBlockScope()
meth public boolean isCatchParameterScope()
meth public boolean isClassBodyScope()
meth public boolean isClassHeadScope()
meth public boolean isClosed()
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
meth public int getSymbolCount()
meth public java.lang.Iterable<com.oracle.js.parser.ir.Symbol> getSymbols()
meth public java.lang.String toString()
meth public static com.oracle.js.parser.ir.Scope createBlock(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createCatchParameter(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createClassBody(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createClassHead(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createEval(com.oracle.js.parser.ir.Scope,boolean)
meth public static com.oracle.js.parser.ir.Scope createFunctionBody(com.oracle.js.parser.ir.Scope)
meth public static com.oracle.js.parser.ir.Scope createFunctionBody(com.oracle.js.parser.ir.Scope,int,boolean)
meth public static com.oracle.js.parser.ir.Scope createFunctionParameter(com.oracle.js.parser.ir.Scope,int)
meth public static com.oracle.js.parser.ir.Scope createGlobal()
meth public static com.oracle.js.parser.ir.Scope createModule()
meth public static com.oracle.js.parser.ir.Scope createSwitchBlock(com.oracle.js.parser.ir.Scope)
meth public void addIdentifierReference(java.lang.String)
meth public void close()
meth public void kill()
meth public void resolveUses()
meth public void setHasEval()
meth public void setHasNestedEval()
supr java.lang.Object
hfds ARROW_FUNCTION_PARAMETER_SCOPE,BLOCK_SCOPE,CATCH_PARAMETER_SCOPE,CLASS_BODY_SCOPE,CLASS_HEAD_SCOPE,EVAL_SCOPE,FUNCTION_BODY_SCOPE,FUNCTION_PARAMETER_SCOPE,FUNCTION_TOP_SCOPE,GLOBAL_SCOPE,IN_DERIVED_CONSTRUCTOR,IN_FUNCTION,IN_METHOD,IS_CLASS_FIELD_INITIALIZER,MODULE_SCOPE,SWITCH_BLOCK_SCOPE,closed,flags,hasBlockScopedOrRedeclaredSymbols,hasClosures,hasEval,hasNestedEval,hasPrivateNames,parent,type
hcls UseInfo

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
cons public init(com.oracle.truffle.api.strings.TruffleString,int)
fld public final static int HAS_BEEN_DECLARED = 1024
fld public final static int IS_ARGUMENTS = 2097152
fld public final static int IS_BLOCK_FUNCTION_DECLARATION = 65536
fld public final static int IS_CATCH_PARAMETER = 32768
fld public final static int IS_CLOSED_OVER = 8388608
fld public final static int IS_CONST = 2
fld public final static int IS_DECLARED_IN_SWITCH_BLOCK = 8192
fld public final static int IS_FUNCTION_SELF = 128
fld public final static int IS_GLOBAL = 8
fld public final static int IS_HOISTABLE_DECLARATION = 256
fld public final static int IS_HOISTED_BLOCK_FUNCTION = 2048
fld public final static int IS_IMPORT_BINDING = 16384
fld public final static int IS_INTERNAL = 64
fld public final static int IS_LET = 1
fld public final static int IS_NEW_TARGET = 67108864
fld public final static int IS_PARAM = 16
fld public final static int IS_PRIVATE_NAME = 131072
fld public final static int IS_PRIVATE_NAME_ACCESSOR = 1048576
fld public final static int IS_PRIVATE_NAME_METHOD = 524288
fld public final static int IS_PRIVATE_NAME_STATIC = 262144
fld public final static int IS_PROGRAM_LEVEL = 512
fld public final static int IS_SUPER = 33554432
fld public final static int IS_THIS = 32
fld public final static int IS_USED = 4194304
fld public final static int IS_USED_IN_INNER_SCOPE = 16777216
fld public final static int IS_VAR = 4
fld public final static int IS_VAR_REDECLARED_HERE = 4096
fld public final static int KINDMASK = 7
intf java.lang.Comparable<com.oracle.js.parser.ir.Symbol>
meth public boolean hasBeenDeclared()
meth public boolean isArguments()
meth public boolean isBlockFunctionDeclaration()
meth public boolean isBlockScoped()
meth public boolean isCatchParameter()
meth public boolean isClosedOver()
meth public boolean isConst()
meth public boolean isDeclaredInSwitchBlock()
meth public boolean isFunctionSelf()
meth public boolean isGlobal()
meth public boolean isHoistableDeclaration()
meth public boolean isHoistedBlockFunctionDeclaration()
meth public boolean isImportBinding()
meth public boolean isInternal()
meth public boolean isLet()
meth public boolean isNewTarget()
meth public boolean isParam()
meth public boolean isPrivateAccessor()
meth public boolean isPrivateField()
meth public boolean isPrivateMethod()
meth public boolean isPrivateName()
meth public boolean isPrivateNameStatic()
meth public boolean isProgramLevel()
meth public boolean isSuper()
meth public boolean isThis()
meth public boolean isUsed()
meth public boolean isUsedInInnerScope()
meth public boolean isVar()
meth public boolean isVarRedeclaredHere()
meth public com.oracle.truffle.api.strings.TruffleString getNameTS()
meth public int compareTo(com.oracle.js.parser.ir.Symbol)
meth public int getFlags()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setClosedOver()
meth public void setHoistedBlockFunctionDeclaration()
meth public void setUsed()
meth public void setUsedInInnerScope()
supr java.lang.Object
hfds flags,name,nameTS

CLSS public abstract com.oracle.js.parser.ir.TemplateLiteralNode
cons protected init(com.oracle.js.parser.ir.TemplateLiteralNode)
cons protected init(long,int)
innr public static TaggedTemplateLiteralNode
innr public static UntaggedTemplateLiteralNode
meth public <%0 extends java.lang.Object> {%%0} accept(com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext,{%%0}>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public static com.oracle.js.parser.ir.TemplateLiteralNode newTagged(long,int,java.util.List<com.oracle.js.parser.ir.Expression>,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public static com.oracle.js.parser.ir.TemplateLiteralNode newUntagged(long,int,java.util.List<com.oracle.js.parser.ir.Expression>)
supr com.oracle.js.parser.ir.Expression

CLSS public static com.oracle.js.parser.ir.TemplateLiteralNode$TaggedTemplateLiteralNode
 outer com.oracle.js.parser.ir.TemplateLiteralNode
cons protected init(long,int,java.util.List<com.oracle.js.parser.ir.Expression>,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Expression> getCookedStrings()
meth public java.util.List<com.oracle.js.parser.ir.Expression> getRawStrings()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.TemplateLiteralNode
hfds cookedStrings,rawStrings

CLSS public static com.oracle.js.parser.ir.TemplateLiteralNode$UntaggedTemplateLiteralNode
 outer com.oracle.js.parser.ir.TemplateLiteralNode
cons protected init(long,int,java.util.List<com.oracle.js.parser.ir.Expression>)
cons public init(com.oracle.js.parser.ir.TemplateLiteralNode$UntaggedTemplateLiteralNode,java.util.List<com.oracle.js.parser.ir.Expression>)
meth public com.oracle.js.parser.ir.Node accept(com.oracle.js.parser.ir.visitor.NodeVisitor<? extends com.oracle.js.parser.ir.LexicalContext>)
meth public java.util.List<com.oracle.js.parser.ir.Expression> getExpressions()
meth public void toString(java.lang.StringBuilder,boolean)
supr com.oracle.js.parser.ir.TemplateLiteralNode
hfds expressions

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
fld public final static int IS_ANNEXB_BLOCK_TO_FUNCTION_TRANSFER = 32
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

CLSS public final com.oracle.truffle.api.TruffleFile
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_DIRECTORY
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_OTHER
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_REGULAR_FILE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_SYMBOLIC_LINK
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_GID
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_MODE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_NLINK
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_UID
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> SIZE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_DEV
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_INODE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_RDEV
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> CREATION_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> LAST_ACCESS_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> LAST_MODIFIED_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> UNIX_CTIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.GroupPrincipal> UNIX_GROUP
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.UserPrincipal> UNIX_OWNER
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.util.Set<java.nio.file.attribute.PosixFilePermission>> UNIX_PERMISSIONS
innr public abstract interface static FileTypeDetector
innr public final static AttributeDescriptor
innr public final static Attributes
meth public !varargs <%0 extends java.lang.Object> void setAttribute(com.oracle.truffle.api.TruffleFile$AttributeDescriptor<{%%0}>,{%%0},java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs <%0 extends java.lang.Object> {%%0} getAttribute(com.oracle.truffle.api.TruffleFile$AttributeDescriptor<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs boolean exists(java.nio.file.LinkOption[])
meth public !varargs boolean isDirectory(java.nio.file.LinkOption[])
meth public !varargs boolean isRegularFile(java.nio.file.LinkOption[])
meth public !varargs boolean isSameFile(com.oracle.truffle.api.TruffleFile,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile getCanonicalFile(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile$Attributes getAttributes(java.util.Collection<? extends com.oracle.truffle.api.TruffleFile$AttributeDescriptor<?>>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.io.BufferedWriter newBufferedWriter(java.nio.charset.Charset,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.BufferedWriter newBufferedWriter(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.InputStream newInputStream(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.OutputStream newOutputStream(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getCreationTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getLastAccessTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getLastModifiedTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.GroupPrincipal getGroup(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.UserPrincipal getOwner(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.util.Set<java.nio.file.attribute.PosixFilePermission> getPosixPermissions(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs long size(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void copy(com.oracle.truffle.api.TruffleFile,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createDirectories(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createDirectory(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createFile(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(com.oracle.truffle.api.TruffleFile,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(com.oracle.truffle.api.TruffleFile,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setCreationTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setLastAccessTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setLastModifiedTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setPosixPermissions(java.util.Set<? extends java.nio.file.attribute.PosixFilePermission>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void visit(java.nio.file.FileVisitor<com.oracle.truffle.api.TruffleFile>,int,java.nio.file.FileVisitOption[]) throws java.io.IOException
meth public boolean endsWith(com.oracle.truffle.api.TruffleFile)
meth public boolean endsWith(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean isAbsolute()
meth public boolean isExecutable()
meth public boolean isReadable()
meth public boolean isSymbolicLink()
meth public boolean isWritable()
meth public boolean startsWith(com.oracle.truffle.api.TruffleFile)
meth public boolean startsWith(java.lang.String)
meth public byte[] readAllBytes() throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getAbsoluteFile()
meth public com.oracle.truffle.api.TruffleFile getParent()
meth public com.oracle.truffle.api.TruffleFile normalize()
meth public com.oracle.truffle.api.TruffleFile readSymbolicLink() throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile relativize(com.oracle.truffle.api.TruffleFile)
meth public com.oracle.truffle.api.TruffleFile resolve(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile resolveSibling(java.lang.String)
meth public int hashCode()
meth public java.io.BufferedReader newBufferedReader() throws java.io.IOException
meth public java.io.BufferedReader newBufferedReader(java.nio.charset.Charset) throws java.io.IOException
meth public java.lang.String detectMimeType()
meth public java.lang.String getMimeType() throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.net.URI toRelativeUri()
meth public java.net.URI toUri()
meth public java.nio.file.DirectoryStream<com.oracle.truffle.api.TruffleFile> newDirectoryStream() throws java.io.IOException
meth public java.util.Collection<com.oracle.truffle.api.TruffleFile> list() throws java.io.IOException
meth public void createLink(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr java.lang.Object
hfds BUFFER_SIZE,MAX_BUFFER_SIZE,fileSystemContext,isEmptyPath,normalizedPath,path
hcls AllFiles,AttributeGroup,ByteChannelDecorator,FileSystemContext,TempFileRandomHolder,TruffleFileDirectoryStream,Walker

CLSS public abstract interface static com.oracle.truffle.api.TruffleFile$FileTypeDetector
 outer com.oracle.truffle.api.TruffleFile
meth public abstract java.lang.String findMimeType(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public abstract java.nio.charset.Charset findEncoding(com.oracle.truffle.api.TruffleFile) throws java.io.IOException

CLSS public abstract com.oracle.truffle.api.TruffleLanguage<%0 extends java.lang.Object>
cons protected init()
fld protected final com.oracle.truffle.api.TruffleLanguage$ContextLocalProvider<{com.oracle.truffle.api.TruffleLanguage%0}> locals
innr protected abstract interface static ContextLocalFactory
innr protected abstract interface static ContextThreadLocalFactory
innr protected final static ContextLocalProvider
innr public abstract interface static !annotation Registration
innr public abstract interface static Provider
innr public abstract static ContextReference
innr public abstract static LanguageReference
innr public final static !enum ContextPolicy
innr public final static !enum ExitMode
innr public final static Env
innr public final static InlineParsingRequest
innr public final static ParsingRequest
meth protected abstract {com.oracle.truffle.api.TruffleLanguage%0} createContext(com.oracle.truffle.api.TruffleLanguage$Env)
meth protected boolean areOptionsCompatible(org.graalvm.options.OptionValues,org.graalvm.options.OptionValues)
meth protected boolean isThreadAccessAllowed(java.lang.Thread,boolean)
meth protected boolean isVisible({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected boolean patchContext({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.TruffleLanguage$Env)
meth protected com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.TruffleLanguage$ParsingRequest) throws java.lang.Exception
meth protected com.oracle.truffle.api.nodes.ExecutableNode parse(com.oracle.truffle.api.TruffleLanguage$InlineParsingRequest) throws java.lang.Exception
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected final int getAsynchronousStackDepth()
meth protected final java.lang.String getLanguageHome()
meth protected java.lang.Object getLanguageView({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected java.lang.Object getScope({com.oracle.truffle.api.TruffleLanguage%0})
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected static <%0 extends com.oracle.truffle.api.TruffleLanguage<?>> {%%0} getCurrentLanguage(java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.3")
meth protected static <%0 extends java.lang.Object, %1 extends com.oracle.truffle.api.TruffleLanguage<{%%0}>> {%%0} getCurrentContext(java.lang.Class<{%%1}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.3")
meth protected void disposeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void disposeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth protected void exitContext({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.TruffleLanguage$ExitMode,int)
meth protected void finalizeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void finalizeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth protected void initializeContext({com.oracle.truffle.api.TruffleLanguage%0}) throws java.lang.Exception
meth protected void initializeMultiThreading({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void initializeMultipleContexts()
meth protected void initializeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
supr java.lang.Object
hfds languageInfo,polyglotLanguageInstance

CLSS public abstract interface static !annotation com.oracle.truffle.api.TruffleLanguage$Registration
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean interactive()
meth public abstract !hasdefault boolean internal()
meth public abstract !hasdefault boolean needsAllEncodings()
meth public abstract !hasdefault com.oracle.truffle.api.TruffleLanguage$ContextPolicy contextPolicy()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.InternalResource>[] internalResources()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.TruffleFile$FileTypeDetector>[] fileTypeDetectors()
meth public abstract !hasdefault java.lang.Class<?>[] services()
meth public abstract !hasdefault java.lang.String defaultMimeType()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String implementationName()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String version()
meth public abstract !hasdefault java.lang.String website()
meth public abstract !hasdefault java.lang.String[] byteMimeTypes()
meth public abstract !hasdefault java.lang.String[] characterMimeTypes()
meth public abstract !hasdefault java.lang.String[] dependentLanguages()
meth public abstract !hasdefault org.graalvm.polyglot.SandboxPolicy sandbox()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GeneratedBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String methodName()
meth public abstract java.lang.Class<?> value()

CLSS public abstract com.oracle.truffle.api.exception.AbstractTruffleException
cons protected init()
cons protected init(com.oracle.truffle.api.exception.AbstractTruffleException)
cons protected init(com.oracle.truffle.api.nodes.Node)
cons protected init(java.lang.String)
cons protected init(java.lang.String,com.oracle.truffle.api.nodes.Node)
cons protected init(java.lang.String,java.lang.Throwable,int,com.oracle.truffle.api.nodes.Node)
fld public final static int UNLIMITED_STACK_TRACE = -1
intf com.oracle.truffle.api.interop.TruffleObject
meth public final com.oracle.truffle.api.nodes.Node getLocation()
meth public final int getStackTraceElementLimit()
meth public final java.lang.Throwable fillInStackTrace()
meth public final java.lang.Throwable getCause()
supr java.lang.RuntimeException
hfds cause,lazyStackTrace,location,stackTraceElementLimit

CLSS public abstract interface !annotation com.oracle.truffle.api.instrumentation.ProvidedTags
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] value()

CLSS public abstract interface com.oracle.truffle.api.interop.TruffleObject

CLSS public abstract interface !annotation com.oracle.truffle.api.library.ExportLibrary
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.library.ExportLibrary$Repeat)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Repeat
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean useForAOT()
meth public abstract !hasdefault int priority()
meth public abstract !hasdefault int useForAOTPriority()
meth public abstract !hasdefault java.lang.Class<?> receiverType()
meth public abstract !hasdefault java.lang.String delegateTo()
meth public abstract !hasdefault java.lang.String transitionLimit()
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.library.Library> value()

CLSS public abstract com.oracle.truffle.api.provider.TruffleLanguageProvider
cons protected init()
meth protected abstract java.lang.Object create()
meth protected abstract java.lang.String getLanguageClassName()
meth protected abstract java.util.Collection<java.lang.String> getServicesClassNames()
meth protected abstract java.util.List<?> createFileTypeDetectors()
meth protected java.lang.Object createInternalResource(java.lang.String)
meth protected java.util.List<java.lang.String> getInternalResourceIds()
supr java.lang.Object

CLSS public final com.oracle.truffle.js.lang.JSFileTypeDetector
cons public init()
intf com.oracle.truffle.api.TruffleFile$FileTypeDetector
meth public java.lang.String findMimeType(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public java.nio.charset.Charset findEncoding(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
supr java.lang.Object

CLSS public final com.oracle.truffle.js.lang.JavaScriptLanguage
cons public init()
fld public final static java.lang.String APPLICATION_MIME_TYPE = "application/javascript"
fld public final static java.lang.String ID = "js"
fld public final static java.lang.String IMPLEMENTATION_NAME = "GraalVM JavaScript"
fld public final static java.lang.String INTERNAL_SOURCE_URL_PREFIX = "internal:"
fld public final static java.lang.String JSON_MIME_TYPE = "application/json"
fld public final static java.lang.String JSON_SOURCE_NAME_SUFFIX = ".json"
fld public final static java.lang.String MODULE_MIME_TYPE = "application/javascript+module"
fld public final static java.lang.String MODULE_SOURCE_NAME_SUFFIX = ".mjs"
fld public final static java.lang.String NAME = "JavaScript"
fld public final static java.lang.String NODE_ENV_PARSE_TOKEN = "%NODE_ENV_PARSE_TOKEN%"
fld public final static java.lang.String SCRIPT_SOURCE_NAME_SUFFIX = ".js"
fld public final static java.lang.String TEXT_MIME_TYPE = "text/javascript"
fld public final static org.graalvm.options.OptionDescriptors OPTION_DESCRIPTORS
meth protected boolean areOptionsCompatible(org.graalvm.options.OptionValues,org.graalvm.options.OptionValues)
meth protected boolean isVisible(com.oracle.truffle.js.runtime.JSRealm,java.lang.Object)
meth protected boolean patchContext(com.oracle.truffle.js.runtime.JSRealm,com.oracle.truffle.api.TruffleLanguage$Env)
meth protected com.oracle.truffle.api.nodes.ExecutableNode parse(com.oracle.truffle.api.TruffleLanguage$InlineParsingRequest) throws java.lang.Exception
meth protected com.oracle.truffle.js.runtime.JSRealm createContext(com.oracle.truffle.api.TruffleLanguage$Env)
meth protected java.lang.Object getLanguageView(com.oracle.truffle.js.runtime.JSRealm,java.lang.Object)
meth protected java.lang.Object getScope(com.oracle.truffle.js.runtime.JSRealm)
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected static com.oracle.truffle.js.nodes.JavaScriptNode parseInlineScript(com.oracle.truffle.js.runtime.JSContext,com.oracle.truffle.api.source.Source,com.oracle.truffle.api.frame.MaterializedFrame,boolean,com.oracle.truffle.api.nodes.Node)
meth protected void disposeContext(com.oracle.truffle.js.runtime.JSRealm)
meth protected void finalizeContext(com.oracle.truffle.js.runtime.JSRealm)
meth protected void initializeContext(com.oracle.truffle.js.runtime.JSRealm)
meth protected void initializeMultipleContexts()
meth public boolean bindMemberFunctions()
meth public boolean isMultiContext()
meth public com.oracle.truffle.api.Assumption getPromiseJobsQueueEmptyAssumption()
meth public com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.TruffleLanguage$ParsingRequest)
meth public com.oracle.truffle.js.runtime.JSContext getJSContext()
meth public int getAsyncStackDepth()
meth public java.lang.String getTruffleLanguageHome()
meth public static com.oracle.truffle.api.CallTarget getParsedProgramCallTarget(com.oracle.truffle.api.nodes.RootNode)
meth public static com.oracle.truffle.api.TruffleLanguage$Env getCurrentEnv()
meth public static com.oracle.truffle.js.lang.JavaScriptLanguage get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.js.lang.JavaScriptLanguage getCurrentLanguage()
meth public static com.oracle.truffle.js.runtime.JSContext getJSContext(org.graalvm.polyglot.Context)
meth public static com.oracle.truffle.js.runtime.JSRealm getCurrentJSRealm()
meth public static com.oracle.truffle.js.runtime.JSRealm getJSRealm(org.graalvm.polyglot.Context)
meth public void interopBoundaryEnter(com.oracle.truffle.js.runtime.JSRealm)
meth public void interopBoundaryExit(com.oracle.truffle.js.runtime.JSRealm)
supr com.oracle.truffle.api.TruffleLanguage<com.oracle.truffle.js.runtime.JSRealm>
hfds PREINIT_CONTEXT_PATCHABLE_OPTIONS,REFERENCE,languageContext,multiContext,promiseJobsQueueEmptyAssumption
hcls ParsedProgramRoot

CLSS public final com.oracle.truffle.js.lang.JavaScriptLanguageProvider
cons public init()
meth protected java.lang.Object create()
meth protected java.lang.Object createInternalResource(java.lang.String)
meth protected java.lang.String getLanguageClassName()
meth protected java.util.Collection<java.lang.String> getServicesClassNames()
meth protected java.util.List<?> createFileTypeDetectors()
meth protected java.util.List<java.lang.String> getInternalResourceIds()
supr com.oracle.truffle.api.provider.TruffleLanguageProvider

CLSS public final com.oracle.truffle.js.lang.SandboxValidationError
cons public init(java.lang.String)
meth public boolean hasLanguage()
meth public java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>> getLanguage()
supr com.oracle.truffle.api.exception.AbstractTruffleException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
innr public final static EnumDesc
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
intf java.lang.constant.Constable
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public final java.util.Optional<java.lang.Enum$EnumDesc<{java.lang.Enum%0}>> describeConstable()
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
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

CLSS public abstract interface !annotation java.lang.annotation.Repeatable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> value()

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

CLSS public abstract interface java.lang.constant.Constable
meth public abstract java.util.Optional<? extends java.lang.constant.ConstantDesc> describeConstable()

