#Signature file v4.1
#Version 1.54

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

CLSS public abstract org.netbeans.modules.xml.schema.completion.spi.CompletionContext
cons public init()
innr public final static !enum CompletionType
meth public abstract java.lang.String getDefaultNamespace()
meth public abstract java.lang.String getTypedChars()
meth public abstract java.util.HashMap<java.lang.String,java.lang.String> getDeclaredNamespaces()
meth public abstract java.util.List<javax.xml.namespace.QName> getPathFromRoot()
meth public abstract org.netbeans.editor.BaseDocument getBaseDocument()
meth public abstract org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType getCompletionType()
meth public abstract org.openide.filesystems.FileObject getPrimaryFile()
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType
 outer org.netbeans.modules.xml.schema.completion.spi.CompletionContext
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_ATTRIBUTE
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_ATTRIBUTE_VALUE
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_DTD
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_ELEMENT
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_ELEMENT_VALUE
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_ENTITY
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_NOTATION
fld public final static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType COMPLETION_TYPE_UNKNOWN
meth public static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType[] values()
supr java.lang.Enum<org.netbeans.modules.xml.schema.completion.spi.CompletionContext$CompletionType>

CLSS public abstract org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider
cons public init()
innr public abstract static CompletionModel
meth public abstract java.util.List<org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider$CompletionModel> getModels(org.netbeans.modules.xml.schema.completion.spi.CompletionContext)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider$CompletionModel
 outer org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider
cons public init()
meth public abstract java.lang.String getSuggestedPrefix()
meth public abstract java.lang.String getTargetNamespace()
meth public abstract org.netbeans.modules.xml.schema.model.SchemaModel getSchemaModel()
supr java.lang.Object

