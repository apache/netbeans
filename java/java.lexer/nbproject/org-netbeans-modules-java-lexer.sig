#Signature file v4.1
#Version 1.59

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

CLSS public final !enum org.netbeans.api.java.lexer.JavaStringTokenId
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId BACKSLASH
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId BACKSPACE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId CR
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId DOUBLE_QUOTE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId ESCAPE_SEQUENCE_INVALID
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId FORM_FEED
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId NEWLINE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId OCTAL_ESCAPE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId OCTAL_ESCAPE_INVALID
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId SINGLE_QUOTE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId TAB
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId TEMPLATE_START
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId TEXT
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId UNICODE_ESCAPE
fld public final static org.netbeans.api.java.lexer.JavaStringTokenId UNICODE_ESCAPE_INVALID
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.java.lexer.JavaStringTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.java.lexer.JavaStringTokenId[] values()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.java.lexer.JavaStringTokenId> language()
supr java.lang.Enum<org.netbeans.api.java.lexer.JavaStringTokenId>
hfds language,primaryCategory

CLSS public final !enum org.netbeans.api.java.lexer.JavaTokenId
fld public final static org.netbeans.api.java.lexer.JavaTokenId ABSTRACT
fld public final static org.netbeans.api.java.lexer.JavaTokenId AMP
fld public final static org.netbeans.api.java.lexer.JavaTokenId AMPAMP
fld public final static org.netbeans.api.java.lexer.JavaTokenId AMPEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId ARROW
fld public final static org.netbeans.api.java.lexer.JavaTokenId ASSERT
fld public final static org.netbeans.api.java.lexer.JavaTokenId AT
fld public final static org.netbeans.api.java.lexer.JavaTokenId BANG
fld public final static org.netbeans.api.java.lexer.JavaTokenId BANGEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId BAR
fld public final static org.netbeans.api.java.lexer.JavaTokenId BARBAR
fld public final static org.netbeans.api.java.lexer.JavaTokenId BAREQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId BLOCK_COMMENT
fld public final static org.netbeans.api.java.lexer.JavaTokenId BOOLEAN
fld public final static org.netbeans.api.java.lexer.JavaTokenId BREAK
fld public final static org.netbeans.api.java.lexer.JavaTokenId BYTE
fld public final static org.netbeans.api.java.lexer.JavaTokenId CARET
fld public final static org.netbeans.api.java.lexer.JavaTokenId CARETEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId CASE
fld public final static org.netbeans.api.java.lexer.JavaTokenId CATCH
fld public final static org.netbeans.api.java.lexer.JavaTokenId CHAR
fld public final static org.netbeans.api.java.lexer.JavaTokenId CHAR_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId CLASS
fld public final static org.netbeans.api.java.lexer.JavaTokenId COLON
fld public final static org.netbeans.api.java.lexer.JavaTokenId COLONCOLON
fld public final static org.netbeans.api.java.lexer.JavaTokenId COMMA
fld public final static org.netbeans.api.java.lexer.JavaTokenId CONST
fld public final static org.netbeans.api.java.lexer.JavaTokenId CONTINUE
fld public final static org.netbeans.api.java.lexer.JavaTokenId DEFAULT
fld public final static org.netbeans.api.java.lexer.JavaTokenId DO
fld public final static org.netbeans.api.java.lexer.JavaTokenId DOT
fld public final static org.netbeans.api.java.lexer.JavaTokenId DOUBLE
fld public final static org.netbeans.api.java.lexer.JavaTokenId DOUBLE_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId ELLIPSIS
fld public final static org.netbeans.api.java.lexer.JavaTokenId ELSE
fld public final static org.netbeans.api.java.lexer.JavaTokenId ENUM
fld public final static org.netbeans.api.java.lexer.JavaTokenId EQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId EQEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId ERROR
fld public final static org.netbeans.api.java.lexer.JavaTokenId EXPORTS
fld public final static org.netbeans.api.java.lexer.JavaTokenId EXTENDS
fld public final static org.netbeans.api.java.lexer.JavaTokenId FALSE
fld public final static org.netbeans.api.java.lexer.JavaTokenId FINAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId FINALLY
fld public final static org.netbeans.api.java.lexer.JavaTokenId FLOAT
fld public final static org.netbeans.api.java.lexer.JavaTokenId FLOAT_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId FLOAT_LITERAL_INVALID
fld public final static org.netbeans.api.java.lexer.JavaTokenId FOR
fld public final static org.netbeans.api.java.lexer.JavaTokenId GOTO
fld public final static org.netbeans.api.java.lexer.JavaTokenId GT
fld public final static org.netbeans.api.java.lexer.JavaTokenId GTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId GTGT
fld public final static org.netbeans.api.java.lexer.JavaTokenId GTGTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId GTGTGT
fld public final static org.netbeans.api.java.lexer.JavaTokenId GTGTGTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId IDENTIFIER
fld public final static org.netbeans.api.java.lexer.JavaTokenId IF
fld public final static org.netbeans.api.java.lexer.JavaTokenId IMPLEMENTS
fld public final static org.netbeans.api.java.lexer.JavaTokenId IMPORT
fld public final static org.netbeans.api.java.lexer.JavaTokenId INSTANCEOF
fld public final static org.netbeans.api.java.lexer.JavaTokenId INT
fld public final static org.netbeans.api.java.lexer.JavaTokenId INTERFACE
fld public final static org.netbeans.api.java.lexer.JavaTokenId INT_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId INVALID_COMMENT_END
fld public final static org.netbeans.api.java.lexer.JavaTokenId JAVADOC_COMMENT
fld public final static org.netbeans.api.java.lexer.JavaTokenId LBRACE
fld public final static org.netbeans.api.java.lexer.JavaTokenId LBRACKET
fld public final static org.netbeans.api.java.lexer.JavaTokenId LINE_COMMENT
fld public final static org.netbeans.api.java.lexer.JavaTokenId LONG
fld public final static org.netbeans.api.java.lexer.JavaTokenId LONG_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId LPAREN
fld public final static org.netbeans.api.java.lexer.JavaTokenId LT
fld public final static org.netbeans.api.java.lexer.JavaTokenId LTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId LTLT
fld public final static org.netbeans.api.java.lexer.JavaTokenId LTLTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId MINUS
fld public final static org.netbeans.api.java.lexer.JavaTokenId MINUSEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId MINUSMINUS
fld public final static org.netbeans.api.java.lexer.JavaTokenId MODULE
fld public final static org.netbeans.api.java.lexer.JavaTokenId MULTILINE_STRING_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId NATIVE
fld public final static org.netbeans.api.java.lexer.JavaTokenId NEW
fld public final static org.netbeans.api.java.lexer.JavaTokenId NULL
fld public final static org.netbeans.api.java.lexer.JavaTokenId OPEN
fld public final static org.netbeans.api.java.lexer.JavaTokenId OPENS
fld public final static org.netbeans.api.java.lexer.JavaTokenId PACKAGE
fld public final static org.netbeans.api.java.lexer.JavaTokenId PERCENT
fld public final static org.netbeans.api.java.lexer.JavaTokenId PERCENTEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId PLUS
fld public final static org.netbeans.api.java.lexer.JavaTokenId PLUSEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId PLUSPLUS
fld public final static org.netbeans.api.java.lexer.JavaTokenId PRIVATE
fld public final static org.netbeans.api.java.lexer.JavaTokenId PROTECTED
fld public final static org.netbeans.api.java.lexer.JavaTokenId PROVIDES
fld public final static org.netbeans.api.java.lexer.JavaTokenId PUBLIC
fld public final static org.netbeans.api.java.lexer.JavaTokenId QUESTION
fld public final static org.netbeans.api.java.lexer.JavaTokenId RBRACE
fld public final static org.netbeans.api.java.lexer.JavaTokenId RBRACKET
fld public final static org.netbeans.api.java.lexer.JavaTokenId REQUIRES
fld public final static org.netbeans.api.java.lexer.JavaTokenId RETURN
fld public final static org.netbeans.api.java.lexer.JavaTokenId RPAREN
fld public final static org.netbeans.api.java.lexer.JavaTokenId SEMICOLON
fld public final static org.netbeans.api.java.lexer.JavaTokenId SHORT
fld public final static org.netbeans.api.java.lexer.JavaTokenId SLASH
fld public final static org.netbeans.api.java.lexer.JavaTokenId SLASHEQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId STAR
fld public final static org.netbeans.api.java.lexer.JavaTokenId STAREQ
fld public final static org.netbeans.api.java.lexer.JavaTokenId STATIC
fld public final static org.netbeans.api.java.lexer.JavaTokenId STRICTFP
fld public final static org.netbeans.api.java.lexer.JavaTokenId STRING_LITERAL
fld public final static org.netbeans.api.java.lexer.JavaTokenId SUPER
fld public final static org.netbeans.api.java.lexer.JavaTokenId SWITCH
fld public final static org.netbeans.api.java.lexer.JavaTokenId SYNCHRONIZED
fld public final static org.netbeans.api.java.lexer.JavaTokenId THIS
fld public final static org.netbeans.api.java.lexer.JavaTokenId THROW
fld public final static org.netbeans.api.java.lexer.JavaTokenId THROWS
fld public final static org.netbeans.api.java.lexer.JavaTokenId TILDE
fld public final static org.netbeans.api.java.lexer.JavaTokenId TO
fld public final static org.netbeans.api.java.lexer.JavaTokenId TRANSIENT
fld public final static org.netbeans.api.java.lexer.JavaTokenId TRANSITIVE
fld public final static org.netbeans.api.java.lexer.JavaTokenId TRUE
fld public final static org.netbeans.api.java.lexer.JavaTokenId TRY
fld public final static org.netbeans.api.java.lexer.JavaTokenId UNDERSCORE
fld public final static org.netbeans.api.java.lexer.JavaTokenId USES
fld public final static org.netbeans.api.java.lexer.JavaTokenId VAR
fld public final static org.netbeans.api.java.lexer.JavaTokenId VOID
fld public final static org.netbeans.api.java.lexer.JavaTokenId VOLATILE
fld public final static org.netbeans.api.java.lexer.JavaTokenId WHILE
fld public final static org.netbeans.api.java.lexer.JavaTokenId WHITESPACE
fld public final static org.netbeans.api.java.lexer.JavaTokenId WITH
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String fixedText()
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.java.lexer.JavaTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.java.lexer.JavaTokenId[] values()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.java.lexer.JavaTokenId> language()
supr java.lang.Enum<org.netbeans.api.java.lexer.JavaTokenId>
hfds fixedText,language,primaryCategory

CLSS public final !enum org.netbeans.api.java.lexer.JavadocTokenId
fld public final static org.netbeans.api.java.lexer.JavadocTokenId DOT
fld public final static org.netbeans.api.java.lexer.JavadocTokenId HASH
fld public final static org.netbeans.api.java.lexer.JavadocTokenId HTML_TAG
fld public final static org.netbeans.api.java.lexer.JavadocTokenId IDENT
fld public final static org.netbeans.api.java.lexer.JavadocTokenId OTHER_TEXT
fld public final static org.netbeans.api.java.lexer.JavadocTokenId TAG
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.java.lexer.JavadocTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.java.lexer.JavadocTokenId[] values()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.java.lexer.JavadocTokenId> language()
supr java.lang.Enum<org.netbeans.api.java.lexer.JavadocTokenId>
hfds language,primaryCategory

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

