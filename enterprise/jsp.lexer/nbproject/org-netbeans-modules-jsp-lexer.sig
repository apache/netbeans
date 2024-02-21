#Signature file v4.1
#Version 1.48

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

CLSS public final !enum org.netbeans.api.jsp.lexer.JspTokenId
fld public final static java.lang.String SCRIPTLET_TOKEN_TYPE_PROPERTY = "JAVA_CODE_TYPE"
fld public final static org.netbeans.api.jsp.lexer.JspTokenId ATTRIBUTE
fld public final static org.netbeans.api.jsp.lexer.JspTokenId ATTR_VALUE
fld public final static org.netbeans.api.jsp.lexer.JspTokenId COMMENT
fld public final static org.netbeans.api.jsp.lexer.JspTokenId EL
fld public final static org.netbeans.api.jsp.lexer.JspTokenId ENDTAG
fld public final static org.netbeans.api.jsp.lexer.JspTokenId EOL
fld public final static org.netbeans.api.jsp.lexer.JspTokenId ERROR
fld public final static org.netbeans.api.jsp.lexer.JspTokenId SCRIPTLET
fld public final static org.netbeans.api.jsp.lexer.JspTokenId SYMBOL
fld public final static org.netbeans.api.jsp.lexer.JspTokenId SYMBOL2
fld public final static org.netbeans.api.jsp.lexer.JspTokenId TAG
fld public final static org.netbeans.api.jsp.lexer.JspTokenId TEXT
fld public final static org.netbeans.api.jsp.lexer.JspTokenId WHITESPACE
innr public final static !enum JavaCodeType
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.jsp.lexer.JspTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.jsp.lexer.JspTokenId[] values()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.jsp.lexer.JspTokenId> language()
supr java.lang.Enum<org.netbeans.api.jsp.lexer.JspTokenId>
hfds language,primaryCategory

CLSS public final static !enum org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType
 outer org.netbeans.api.jsp.lexer.JspTokenId
fld public final static org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType DECLARATION
fld public final static org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType EXPRESSION
fld public final static org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType SCRIPTLET
meth public static org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType valueOf(java.lang.String)
meth public static org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType[] values()
supr java.lang.Enum<org.netbeans.api.jsp.lexer.JspTokenId$JavaCodeType>
hfds type

CLSS public abstract interface org.netbeans.api.lexer.TokenId
meth public abstract int ordinal()
meth public abstract java.lang.String name()
meth public abstract java.lang.String primaryCategory()

CLSS public final org.netbeans.spi.jsp.lexer.JspParseData
cons public init(java.util.Map<java.lang.String,java.lang.String>,boolean,boolean,boolean)
meth public boolean initialized()
meth public boolean isELIgnored()
meth public boolean isInitialized()
meth public boolean isTagLibRegistered(java.lang.CharSequence)
meth public boolean isXMLSyntax()
meth public java.lang.String toString()
meth public void updateParseData(java.util.Map<java.lang.String,java.lang.String>,boolean,boolean)
supr java.lang.Object
hfds initialized,isELIgnored,isXMLSyntax,prefixMap

