#Signature file v4.1
#Version 1.50

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

CLSS public final !enum org.netbeans.modules.el.lexer.api.ELTokenId
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId AND_AND
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId AND_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId CHAR_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId COLON
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId COMMA
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId CONCAT
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId DIV
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId DIV_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId DOT
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId DOUBLE_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId EMPTY_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId EOL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId EQ
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId EQ_EQ
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId EQ_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId FALSE_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId FLOAT_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId GE_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId GT
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId GT_EQ
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId GT_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId HEX_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId IDENTIFIER
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId INSTANCEOF_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId INT_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId INVALID_CHAR
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId INVALID_OCTAL_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LAMBDA
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LBRACKET
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LE_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LONG_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LPAREN
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LT
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LT_EQ
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId LT_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId MINUS
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId MOD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId MOD_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId MUL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId NE_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId NOT
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId NOT_EQ
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId NOT_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId NULL_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId OCTAL_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId OR_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId OR_OR
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId PLUS
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId QUESTION
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId RBRACKET
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId RPAREN
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId SEMICOLON
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId STRING_LITERAL
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId TAG_LIB_PREFIX
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId TRUE_KEYWORD
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId WHITESPACE
innr public final static !enum ELTokenCategories
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.el.lexer.api.ELTokenId> language()
meth public static org.netbeans.modules.el.lexer.api.ELTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.el.lexer.api.ELTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.el.lexer.api.ELTokenId>
hfds fixedText,language,primaryCategory

CLSS public final static !enum org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories
 outer org.netbeans.modules.el.lexer.api.ELTokenId
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories ERRORS
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories KEYWORDS
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories NUMERIC_LITERALS
fld public final static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories OPERATORS
meth public boolean hasCategory(org.netbeans.api.lexer.TokenId)
meth public static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories valueOf(java.lang.String)
meth public static org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories[] values()
supr java.lang.Enum<org.netbeans.modules.el.lexer.api.ELTokenId$ELTokenCategories>
hfds categoryName

