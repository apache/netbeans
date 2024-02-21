#Signature file v4.1
#Version 1.23

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

CLSS public final org.netbeans.modules.javascript2.doc.api.JsDocumentationPrinter
meth public static java.lang.String printDocumentation(org.netbeans.modules.javascript2.doc.spi.JsComment)
meth public static java.lang.String printParameterDocumentation(org.netbeans.modules.javascript2.doc.spi.DocParameter)
supr java.lang.Object
hfds OPTIONAL_PARAMETER,OPTIONAL_PARAMETER_DEFAULT,PARAGRAPH_BEGIN,SYNTAX_HEADER_BACKGROUNDCOLOR,SYNTAX_HEADER_COLOR,TABLE_BEGIN,WRAPPER_HEADER,WRAPPER_SUBHEADER

CLSS public final org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport
fld public final static java.lang.String DOCUMENTATION_PROVIDER_PATH = "javascript/doc/providers"
meth public static org.netbeans.modules.javascript2.doc.spi.JsComment getCommentForOffset(org.netbeans.modules.javascript2.types.spi.ParserResult,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder getDocumentationHolder(org.netbeans.modules.javascript2.types.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider getDocumentationProvider(org.netbeans.modules.javascript2.types.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.javascript2.doc.spi.SyntaxProvider getSyntaxProvider(org.netbeans.modules.javascript2.types.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag
cons public init(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean equals(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public final java.lang.String getDocumentation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final java.lang.String getInsertTemplate()
meth public final java.lang.String getName()
meth public int hashCode()
meth public java.util.List<org.netbeans.modules.javascript2.doc.spi.ParameterFormat> getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds documentation,insertTemplate,name

CLSS public abstract org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag> getAnnotations()
meth public final java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public abstract interface org.netbeans.modules.javascript2.doc.spi.DocParameter
meth public abstract boolean isOptional()
meth public abstract java.lang.String getDefaultValue()
meth public abstract java.lang.String getParamDescription()
meth public abstract java.util.List<org.netbeans.modules.javascript2.types.api.Type> getParamTypes()
meth public abstract org.netbeans.modules.javascript2.types.api.Identifier getParamName()

CLSS public final org.netbeans.modules.javascript2.doc.spi.DocumentationContainer
cons public init()
meth public org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder getHolder(org.netbeans.modules.javascript2.types.spi.ParserResult)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds holder

CLSS public abstract org.netbeans.modules.javascript2.doc.spi.JsComment
cons public init(org.netbeans.modules.csl.api.OffsetRange)
meth public abstract boolean isClass()
meth public abstract boolean isConstant()
meth public abstract java.lang.String getDeprecated()
meth public abstract java.lang.String getSince()
meth public abstract java.util.List<java.lang.String> getExamples()
meth public abstract java.util.List<java.lang.String> getSee()
meth public abstract java.util.List<java.lang.String> getSummary()
meth public abstract java.util.List<java.lang.String> getSyntax()
meth public abstract java.util.List<org.netbeans.modules.javascript2.doc.spi.DocParameter> getParameters()
meth public abstract java.util.List<org.netbeans.modules.javascript2.doc.spi.DocParameter> getProperties()
meth public abstract java.util.List<org.netbeans.modules.javascript2.doc.spi.DocParameter> getThrows()
meth public abstract java.util.List<org.netbeans.modules.javascript2.types.api.Type> getExtends()
meth public abstract java.util.List<org.netbeans.modules.javascript2.types.api.Type> getTypes()
meth public abstract java.util.Set<org.netbeans.modules.javascript2.doc.spi.JsModifier> getModifiers()
meth public abstract org.netbeans.modules.javascript2.doc.spi.DocParameter getDefinedType()
meth public abstract org.netbeans.modules.javascript2.doc.spi.DocParameter getReturnType()
meth public abstract org.netbeans.modules.javascript2.types.api.Type getCallBack()
meth public final java.lang.String getDocumentation()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange()
supr java.lang.Object
hfds offsetRange

CLSS public abstract org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder
cons public init(org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider,org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.Map<java.lang.Integer,? extends org.netbeans.modules.javascript2.doc.spi.JsComment> getCommentBlocks()
meth public boolean isClass(com.oracle.js.parser.ir.Node)
meth public boolean isConstant(com.oracle.js.parser.ir.Node)
meth public boolean isDeprecated(com.oracle.js.parser.ir.Node)
meth public boolean isWhitespaceToken(org.netbeans.api.lexer.Token<? extends org.netbeans.modules.javascript2.lexer.api.JsTokenId>)
meth public final java.util.Map<java.lang.String,java.util.List<org.netbeans.modules.csl.api.OffsetRange>> getOccurencesMap()
meth public java.util.List<org.netbeans.modules.javascript2.doc.spi.DocParameter> getParameters(com.oracle.js.parser.ir.Node)
meth public java.util.List<org.netbeans.modules.javascript2.doc.spi.DocParameter> getProperties(com.oracle.js.parser.ir.Node)
meth public java.util.List<org.netbeans.modules.javascript2.types.api.Type> getExtends(com.oracle.js.parser.ir.Node)
meth public java.util.List<org.netbeans.modules.javascript2.types.api.Type> getReturnType(com.oracle.js.parser.ir.Node)
meth public java.util.Set<org.netbeans.modules.javascript2.doc.spi.JsModifier> getModifiers(com.oracle.js.parser.ir.Node)
meth public org.netbeans.modules.csl.api.Documentation getDocumentation(com.oracle.js.parser.ir.Node)
meth public org.netbeans.modules.javascript2.doc.spi.JsComment getCommentForOffset(int,java.util.Map<java.lang.Integer,? extends org.netbeans.modules.javascript2.doc.spi.JsComment>)
meth public org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider getProvider()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds occurencesMap,provider,snapshot

CLSS public abstract interface org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider
meth public abstract java.util.List<? extends org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider> getAnnotationsProvider()
meth public abstract java.util.Set<java.lang.String> getSupportedTags()
meth public abstract org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder createDocumentationHolder(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract org.netbeans.modules.javascript2.doc.spi.SyntaxProvider getSyntaxProvider()

CLSS public final !enum org.netbeans.modules.javascript2.doc.spi.JsModifier
fld public final static org.netbeans.modules.javascript2.doc.spi.JsModifier PRIVATE
fld public final static org.netbeans.modules.javascript2.doc.spi.JsModifier PUBLIC
fld public final static org.netbeans.modules.javascript2.doc.spi.JsModifier STATIC
meth public java.lang.String toString()
meth public static org.netbeans.modules.javascript2.doc.spi.JsModifier fromString(java.lang.String)
meth public static org.netbeans.modules.javascript2.doc.spi.JsModifier valueOf(java.lang.String)
meth public static org.netbeans.modules.javascript2.doc.spi.JsModifier[] values()
supr java.lang.Enum<org.netbeans.modules.javascript2.doc.spi.JsModifier>
hfds value

CLSS public final org.netbeans.modules.javascript2.doc.spi.ParameterFormat
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getParam()
meth public java.lang.String getPost()
meth public java.lang.String getPre()
supr java.lang.Object
hfds param,post,pre

CLSS public abstract interface org.netbeans.modules.javascript2.doc.spi.SyntaxProvider
fld public final static java.lang.String NAME_PLACEHOLDER = "[name]"
fld public final static java.lang.String TYPE_PLACEHOLDER = "[type]"
meth public abstract java.lang.String paramTagTemplate()
meth public abstract java.lang.String returnTagTemplate()
meth public abstract java.lang.String typeTagTemplate()
meth public abstract java.lang.String typesSeparator()

