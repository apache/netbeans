#Signature file v4.1
#Version 0.98

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

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.SuppressWarnings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String justification()
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface org.netbeans.modules.csl.api.DeclarationFinder
innr public abstract interface static AlternativeLocation
innr public final static DeclarationLocation
meth public abstract org.netbeans.modules.csl.api.DeclarationFinder$DeclarationLocation findDeclaration(org.netbeans.modules.csl.spi.ParserResult,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getReferenceSpan(javax.swing.text.Document,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.modules.javascript2.editor.api.FrameworksUtils
cons public init()
fld public final static java.lang.String CATEGORY = "jsframeworks"
fld public final static java.lang.String GRADLE_PROJECT = "org-netbeans-modules-gradle"
fld public final static java.lang.String HTML5_CLIENT_PROJECT = "org.netbeans.modules.web.clientproject"
fld public final static java.lang.String MAVEN_PROJECT = "org-netbeans-modules-maven"
fld public final static java.lang.String PHP_PROJECT = "org-netbeans-modules-php-project"
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.javascript2.editor.spi.CompletionContext
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext CALL_ARGUMENT
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext DOCUMENTATION
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext EXPRESSION
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext GLOBAL
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext IMPORT_EXPORT_MODULE
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext IMPORT_EXPORT_SPECIAL_TOKENS
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext IN_STRING
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext NONE
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext NUMBER
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext OBJECT_MEMBERS
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext OBJECT_PROPERTY
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext OBJECT_PROPERTY_NAME
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext REGEXP
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext STRING
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext STRING_ELEMENTS_BY_CLASS_NAME
fld public final static org.netbeans.modules.javascript2.editor.spi.CompletionContext STRING_ELEMENTS_BY_ID
meth public static org.netbeans.modules.javascript2.editor.spi.CompletionContext valueOf(java.lang.String)
meth public static org.netbeans.modules.javascript2.editor.spi.CompletionContext[] values()
supr java.lang.Enum<org.netbeans.modules.javascript2.editor.spi.CompletionContext>

CLSS public abstract interface org.netbeans.modules.javascript2.editor.spi.CompletionProvider
innr public abstract interface static !annotation Registration
meth public abstract java.lang.String getHelpDocumentation(org.netbeans.modules.csl.spi.ParserResult,org.netbeans.modules.csl.api.ElementHandle)
meth public abstract java.util.List<org.netbeans.modules.csl.api.CompletionProposal> complete(org.netbeans.modules.csl.api.CodeCompletionContext,org.netbeans.modules.javascript2.editor.spi.CompletionContext,java.lang.String)

CLSS public abstract interface static !annotation org.netbeans.modules.javascript2.editor.spi.CompletionProvider$Registration
 outer org.netbeans.modules.javascript2.editor.spi.CompletionProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int priority()

CLSS public abstract interface org.netbeans.modules.javascript2.editor.spi.CompletionProviderEx
intf org.netbeans.modules.javascript2.editor.spi.CompletionProvider
meth public abstract java.util.List<org.netbeans.modules.csl.api.CompletionProposal> complete(org.netbeans.modules.javascript2.editor.spi.ProposalRequest)
meth public java.util.List<org.netbeans.modules.csl.api.CompletionProposal> complete(org.netbeans.modules.csl.api.CodeCompletionContext,org.netbeans.modules.javascript2.editor.spi.CompletionContext,java.lang.String)

CLSS public abstract interface org.netbeans.modules.javascript2.editor.spi.DeclarationFinder
innr public abstract interface static !annotation Registration
intf org.netbeans.modules.csl.api.DeclarationFinder

CLSS public abstract interface static !annotation org.netbeans.modules.javascript2.editor.spi.DeclarationFinder$Registration
 outer org.netbeans.modules.javascript2.editor.spi.DeclarationFinder
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int priority()

CLSS public abstract interface org.netbeans.modules.javascript2.editor.spi.ElementDocumentation
meth public abstract org.netbeans.modules.csl.api.Documentation getDocumentation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.javascript2.editor.spi.PostScanProvider
meth public abstract void addPostScanTask(java.lang.Runnable)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.javascript2.editor.spi.ProposalRequest
cons public init(org.netbeans.modules.csl.api.CodeCompletionContext,org.netbeans.modules.javascript2.editor.spi.CompletionContext,java.util.Collection<java.lang.String>,int)
meth public int getAnchor()
meth public java.lang.String getPrefix()
meth public java.util.Collection<java.lang.String> getSelectors()
meth public org.netbeans.modules.csl.api.CodeCompletionContext getContext()
meth public org.netbeans.modules.csl.spi.ParserResult getInfo()
meth public org.netbeans.modules.javascript2.editor.spi.CompletionContext getType()
supr java.lang.Object
hfds context,offset,selectors,type

