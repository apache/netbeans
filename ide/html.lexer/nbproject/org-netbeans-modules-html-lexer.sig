#Signature file v4.1
#Version 1.61

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

CLSS public final !enum org.netbeans.api.html.lexer.HTMLTokenId
fld public final static java.lang.String SCRIPT_TYPE_TOKEN_PROPERTY = "type"
fld public final static java.lang.String VALUE_CSS_TOKEN_TYPE_CLASS = "class"
fld public final static java.lang.String VALUE_CSS_TOKEN_TYPE_ID = "id"
fld public final static java.lang.String VALUE_CSS_TOKEN_TYPE_PROPERTY = "valueCssType"
fld public final static org.netbeans.api.html.lexer.HTMLTokenId ARGUMENT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId BLOCK_COMMENT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId CHARACTER
fld public final static org.netbeans.api.html.lexer.HTMLTokenId DECLARATION
fld public final static org.netbeans.api.html.lexer.HTMLTokenId EL_CLOSE_DELIMITER
fld public final static org.netbeans.api.html.lexer.HTMLTokenId EL_CONTENT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId EL_OPEN_DELIMITER
fld public final static org.netbeans.api.html.lexer.HTMLTokenId EOL
fld public final static org.netbeans.api.html.lexer.HTMLTokenId ERROR
fld public final static org.netbeans.api.html.lexer.HTMLTokenId OPERATOR
fld public final static org.netbeans.api.html.lexer.HTMLTokenId SCRIPT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId SGML_COMMENT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId STYLE
fld public final static org.netbeans.api.html.lexer.HTMLTokenId TAG_CLOSE
fld public final static org.netbeans.api.html.lexer.HTMLTokenId TAG_CLOSE_SYMBOL
fld public final static org.netbeans.api.html.lexer.HTMLTokenId TAG_OPEN
fld public final static org.netbeans.api.html.lexer.HTMLTokenId TAG_OPEN_SYMBOL
fld public final static org.netbeans.api.html.lexer.HTMLTokenId TEXT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId VALUE
fld public final static org.netbeans.api.html.lexer.HTMLTokenId VALUE_CSS
fld public final static org.netbeans.api.html.lexer.HTMLTokenId VALUE_JAVASCRIPT
fld public final static org.netbeans.api.html.lexer.HTMLTokenId WS
fld public final static org.netbeans.api.html.lexer.HTMLTokenId XML_PI
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.html.lexer.HTMLTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.html.lexer.HTMLTokenId[] values()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.html.lexer.HTMLTokenId> language()
supr java.lang.Enum<org.netbeans.api.html.lexer.HTMLTokenId>
hfds BABEL_MIMETYPE,JAVASCRIPT_MIMETYPE,LOG,LOGGER,SCRIPT_TYPE_MODULE,STYLE_MIMETYPE,language,primaryCategory

CLSS public abstract org.netbeans.api.html.lexer.HtmlLexerPlugin
cons public init()
meth public java.lang.String createAttributeEmbedding(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getCloseDelimiter()
meth public java.lang.String getContentMimeType()
meth public java.lang.String getOpenDelimiter()
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

