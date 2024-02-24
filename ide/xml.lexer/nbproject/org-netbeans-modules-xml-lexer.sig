#Signature file v4.1
#Version 1.53

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

CLSS public final !enum org.netbeans.api.xml.lexer.DTDTokenId
fld public final static org.netbeans.api.xml.lexer.DTDTokenId CHARACTER
fld public final static org.netbeans.api.xml.lexer.DTDTokenId COMMENT
fld public final static org.netbeans.api.xml.lexer.DTDTokenId DECLARATION
fld public final static org.netbeans.api.xml.lexer.DTDTokenId ERROR
fld public final static org.netbeans.api.xml.lexer.DTDTokenId KEYWORD
fld public final static org.netbeans.api.xml.lexer.DTDTokenId NAME
fld public final static org.netbeans.api.xml.lexer.DTDTokenId OPERATOR
fld public final static org.netbeans.api.xml.lexer.DTDTokenId PI_CONTENT
fld public final static org.netbeans.api.xml.lexer.DTDTokenId PLAIN
fld public final static org.netbeans.api.xml.lexer.DTDTokenId REFERENCE
fld public final static org.netbeans.api.xml.lexer.DTDTokenId STRING
fld public final static org.netbeans.api.xml.lexer.DTDTokenId SYMBOL
fld public final static org.netbeans.api.xml.lexer.DTDTokenId TARGET
fld public final static org.netbeans.api.xml.lexer.DTDTokenId WS
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.xml.lexer.DTDTokenId> language()
meth public static org.netbeans.api.xml.lexer.DTDTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.xml.lexer.DTDTokenId[] values()
supr java.lang.Enum<org.netbeans.api.xml.lexer.DTDTokenId>
hfds language,primaryCategory

CLSS public final !enum org.netbeans.api.xml.lexer.XMLTokenId
fld public final static org.netbeans.api.xml.lexer.XMLTokenId ARGUMENT
fld public final static org.netbeans.api.xml.lexer.XMLTokenId BLOCK_COMMENT
fld public final static org.netbeans.api.xml.lexer.XMLTokenId CDATA_SECTION
fld public final static org.netbeans.api.xml.lexer.XMLTokenId CHARACTER
fld public final static org.netbeans.api.xml.lexer.XMLTokenId DECLARATION
fld public final static org.netbeans.api.xml.lexer.XMLTokenId EOL
fld public final static org.netbeans.api.xml.lexer.XMLTokenId ERROR
fld public final static org.netbeans.api.xml.lexer.XMLTokenId OPERATOR
fld public final static org.netbeans.api.xml.lexer.XMLTokenId PI_CONTENT
fld public final static org.netbeans.api.xml.lexer.XMLTokenId PI_END
fld public final static org.netbeans.api.xml.lexer.XMLTokenId PI_START
fld public final static org.netbeans.api.xml.lexer.XMLTokenId PI_TARGET
fld public final static org.netbeans.api.xml.lexer.XMLTokenId TAG
fld public final static org.netbeans.api.xml.lexer.XMLTokenId TEXT
fld public final static org.netbeans.api.xml.lexer.XMLTokenId VALUE
fld public final static org.netbeans.api.xml.lexer.XMLTokenId WS
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.api.xml.lexer.XMLTokenId> language()
meth public static org.netbeans.api.xml.lexer.XMLTokenId valueOf(java.lang.String)
meth public static org.netbeans.api.xml.lexer.XMLTokenId[] values()
supr java.lang.Enum<org.netbeans.api.xml.lexer.XMLTokenId>
hfds language,primaryCategory

