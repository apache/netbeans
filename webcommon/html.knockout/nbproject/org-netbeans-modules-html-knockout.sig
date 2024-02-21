#Signature file v4.1
#Version 1.33

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

CLSS public final !enum org.netbeans.modules.html.knockout.api.KODataBindTokenId
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId COLON
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId COMMA
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId ERROR
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId KEY
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId VALUE
fld public final static org.netbeans.modules.html.knockout.api.KODataBindTokenId WS
intf org.netbeans.api.lexer.TokenId
meth public java.lang.String primaryCategory()
meth public static org.netbeans.api.lexer.Language<org.netbeans.modules.html.knockout.api.KODataBindTokenId> language()
meth public static org.netbeans.modules.html.knockout.api.KODataBindTokenId valueOf(java.lang.String)
meth public static org.netbeans.modules.html.knockout.api.KODataBindTokenId[] values()
supr java.lang.Enum<org.netbeans.modules.html.knockout.api.KODataBindTokenId>
hfds category,language

