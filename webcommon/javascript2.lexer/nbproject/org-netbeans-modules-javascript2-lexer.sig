#Signature file v4.1
#Version 1.29

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

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public final !enum org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId
fld protected final static org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId> LANGUAGE
fld public final static java.lang.String MIME_TYPE = "text/javascript-doc"
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId ASTERISK
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId AT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId BRACKET_LEFT_BRACKET
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId BRACKET_LEFT_CURLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId BRACKET_RIGHT_BRACKET
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId BRACKET_RIGHT_CURLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId COMMA
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId COMMENT_BLOCK_START
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId COMMENT_DOC_START
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId COMMENT_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId EOL
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId HTML
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId KEYWORD
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId OTHER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId STRING
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId STRING_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId STRING_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId UNKNOWN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId WHITESPACE
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId> language()
meth public static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId>
hfds fixedText,primaryCategory

CLSS public !enum org.netbeans.modules.javascript2.lexer.api.JsTokenId
fld public final static java.lang.String BOWERRC_JSON_MIME_TYPE = "text/bowerrc+x-json"
fld public final static java.lang.String BOWER_JSON_MIME_TYPE = "text/bower+x-json"
fld public final static java.lang.String GRUNT_MIME_TYPE = "text/grunt+javascript"
fld public final static java.lang.String GULP_MIME_TYPE = "text/gulp+javascript"
fld public final static java.lang.String JAVASCRIPT_MIME_TYPE = "text/javascript"
fld public final static java.lang.String JSHINTRC_JSON_MIME_TYPE = "text/jshintrc+x-json"
fld public final static java.lang.String JSON_MIME_TYPE = "text/x-json"
fld public final static java.lang.String KARMACONF_MIME_TYPE = "text/karmaconf+javascript"
fld public final static java.lang.String PACKAGE_JSON_MIME_TYPE = "text/package+x-json"
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BLOCK_COMMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_LEFT_BRACKET
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_LEFT_CURLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_LEFT_PAREN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_RIGHT_BRACKET
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_RIGHT_CURLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId BRACKET_RIGHT_PAREN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId DOC_COMMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId EOL
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId ERROR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId IDENTIFIER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId JSX_EXP_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId JSX_EXP_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId JSX_TEXT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_BREAK
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_CASE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_CATCH
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_CLASS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_CONST
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_CONTINUE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_DEBUGGER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_DEFAULT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_DELETE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_DO
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_ELSE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_EXPORT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_EXTENDS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_FALSE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_FINALLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_FOR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_FUNCTION
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_IF
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_IMPORT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_IN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_INSTANCEOF
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_NEW
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_NULL
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_RETURN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_SUPER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_SWITCH
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_THIS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_THROW
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_TRUE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_TRY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_TYPEOF
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_VAR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_VOID
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_WHILE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_WITH
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId KEYWORD_YIELD
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId LINE_COMMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId NUMBER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_AND
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_ARROW
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_ASSIGN_LOG_AND
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_ASSIGN_LOG_OR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_ASSIGN_NULLISH
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_AT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_AND
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_AND_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_NOT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_OR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_OR_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_XOR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_BITWISE_XOR_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_COLON
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_COMMA
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_DECREMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_DIVISION
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_DIVISION_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_DOT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_EQUALS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_EQUALS_EXACTLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_EXPONENTIATION
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_EXPONENTIATION_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_GREATER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_GREATER_EQUALS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_INCREMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_LEFT_SHIFT_ARITHMETIC
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_LOWER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_LOWER_EQUALS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MINUS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MINUS_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MODULUS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MODULUS_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MULTIPLICATION
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_MULTIPLICATION_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_NOT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_NOT_EQUALS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_NOT_EQUALS_EXACTLY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_NULLISH
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_OPTIONAL_ACCESS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_OR
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_PLUS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_PLUS_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_REST
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_RIGHT_SHIFT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_RIGHT_SHIFT_ARITHMETIC
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_RIGHT_SHIFT_ASSIGNMENT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_SEMICOLON
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId OPERATOR_TERNARY
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId PRIVATE_IDENTIFIER
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId REGEXP
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId REGEXP_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId REGEXP_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_AWAIT
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_ENUM
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_IMPLEMENTS
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_INTERFACE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_LET
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_PACKAGE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_PRIVATE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_PROTECTED
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_PUBLIC
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId RESERVED_STATIC
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId STRING
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId STRING_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId STRING_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId TEMPLATE
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId TEMPLATE_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId TEMPLATE_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId TEMPLATE_EXP_BEGIN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId TEMPLATE_EXP_END
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId UNKNOWN
fld public final static org.netbeans.modules.javascript2.lexer.api.JsTokenId WHITESPACE
intf org.netbeans.api.lexer.TokenId
meth public boolean isError()
meth public boolean isKeyword()
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static boolean isJSONBasedMimeType(java.lang.String)
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId> javascriptLanguage()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId> jsonLanguage()
meth public static org.netbeans.modules.javascript2.lexer.api.JsTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.javascript2.lexer.api.JsTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.javascript2.lexer.api.JsTokenId>
hfds JAVASCRIPT_LANGUAGE,JSON_LANGUAGE,JSON_MIME_TYPE_END,fixedText,primaryCategory

CLSS public final org.netbeans.modules.javascript2.lexer.api.LexUtilities
meth public static <%0 extends java.lang.Object> org.netbeans.api.lexer.TokenSequence<? extends {%%0}> getTokenSequence(org.netbeans.api.lexer.TokenHierarchy<?>,int,org.netbeans.api.lexer.Language<? extends {%%0}>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isBinaryOperator(org.netbeans.modules.javascript2.lexer.api.JsTokenId,org.netbeans.modules.javascript2.lexer.api.JsTokenId)
meth public static boolean skipParenthesis(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,boolean)
meth public static char getTokenChar(javax.swing.text.Document,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static int getLexerOffset(org.netbeans.modules.csl.spi.ParserResult,int)
meth public static int getTokenBalance(javax.swing.text.Document,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>) throws javax.swing.text.BadLocationException
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findNext(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findNextIncluding(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findNextNonWsNonComment(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findNextToken(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findPrevious(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findPreviousIncluding(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findPreviousNonWsNonComment(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> findPreviousToken(org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,java.util.List<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getToken(javax.swing.text.Document,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId> getJsDocumentationTokenSequence(org.netbeans.api.lexer.TokenHierarchy<?>,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId> getJsDocumentationTokenSequence(org.netbeans.modules.parsing.api.Snapshot,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getJsPositionedSequence(javax.swing.text.Document,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getJsPositionedSequence(org.netbeans.modules.parsing.api.Snapshot,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getJsTokenSequence(javax.swing.text.Document,int)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getJsTokenSequence(org.netbeans.api.lexer.TokenHierarchy<?>,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getJsTokenSequence(org.netbeans.modules.parsing.api.Snapshot,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getNextJsTokenSequence(javax.swing.text.Document,int,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getNextJsTokenSequence(org.netbeans.api.lexer.TokenHierarchy<?>,int,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getNextJsTokenSequence(org.netbeans.modules.parsing.api.Snapshot,int,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getPositionedSequence(javax.swing.text.Document,int,boolean,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getPositionedSequence(javax.swing.text.Document,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getPositionedSequence(org.netbeans.modules.parsing.api.Snapshot,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getTokenSequence(javax.swing.text.Document,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId> getTokenSequence(org.netbeans.modules.parsing.api.Snapshot,int,org.netbeans.api.lexer.Language<org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.csl.api.OffsetRange findBwd(javax.swing.text.Document,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId)
meth public static org.netbeans.modules.csl.api.OffsetRange findFwd(javax.swing.text.Document,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>,org.netbeans.api.lexer.TokenId,org.netbeans.api.lexer.TokenId)
meth public static org.netbeans.modules.csl.api.OffsetRange getLexerOffsets(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.OffsetRange)
meth public static org.netbeans.modules.csl.api.OffsetRange getMultilineRange(javax.swing.text.Document,org.netbeans.api.lexer.TokenSequence<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
supr java.lang.Object

