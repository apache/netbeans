#Signature file v4.1
#Version 2.2

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.Attribute
innr public static DefaultAttribute
meth public abstract boolean isRequired()
meth public abstract java.lang.String getDefaultValue()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getMethodSignature()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()

CLSS public static org.netbeans.modules.web.jsfapi.api.Attribute$DefaultAttribute
 outer org.netbeans.modules.web.jsfapi.api.Attribute
cons public init(java.lang.String,java.lang.String,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String)
intf org.netbeans.modules.web.jsfapi.api.Attribute
meth public boolean equals(java.lang.Object)
meth public boolean isRequired()
meth public int hashCode()
meth public java.lang.String getDefaultValue()
meth public java.lang.String getDescription()
meth public java.lang.String getMethodSignature()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds defaultValue,description,methodSignature,name,required,type

CLSS public final !enum org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo COMPOSITE
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo FACELETS
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo HTML
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo JSF
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo JSF_CORE
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo JSTL_CORE
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo JSTL_CORE_FUNCTIONS
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo PASSTHROUGH
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo PRIMEFACES
fld public final static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo PRIMEFACES_MOBILE
intf org.netbeans.modules.web.jsfapi.api.LibraryInfo
meth public java.lang.String getDefaultPrefix()
meth public java.lang.String getDisplayName()
meth public java.lang.String getNamespace()
meth public java.util.Set<java.lang.String> getValidNamespaces()
meth public static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo[] values()
meth public static org.netbeans.modules.web.jsfapi.api.LibraryInfo forNamespace(java.lang.String)
supr java.lang.Enum<org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo>
hfds allValidNamespaces,defaultPrefix,displayName

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.Function
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getSignature()

CLSS public final !enum org.netbeans.modules.web.jsfapi.api.JsfNamespaces
fld public final static org.netbeans.modules.web.jsfapi.api.JsfNamespaces JAKARTA_EE_NS
fld public final static org.netbeans.modules.web.jsfapi.api.JsfNamespaces JAVA_SUN_COM_NS
fld public final static org.netbeans.modules.web.jsfapi.api.JsfNamespaces XMLNS_JCP_ORG_NS
innr public final static !enum Type
meth public java.lang.String getNamespace(org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type)
meth public static org.netbeans.modules.web.jsfapi.api.JsfNamespaces valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsfapi.api.JsfNamespaces[] values()
supr java.lang.Enum<org.netbeans.modules.web.jsfapi.api.JsfNamespaces>
hfds namespaces

CLSS public final static !enum org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type
 outer org.netbeans.modules.web.jsfapi.api.JsfNamespaces
fld public final static org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type TAGLIB
meth public static org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type[] values()
supr java.lang.Enum<org.netbeans.modules.web.jsfapi.api.JsfNamespaces$Type>

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.JsfSupport
meth public abstract java.util.Map<java.lang.String,? extends org.netbeans.modules.web.jsfapi.api.Library> getLibraries()
meth public abstract org.netbeans.api.java.classpath.ClassPath getClassPath()
meth public abstract org.netbeans.api.project.Project getProject()
meth public abstract org.netbeans.modules.web.api.webmodule.WebModule getWebModule()
meth public abstract org.netbeans.modules.web.jsfapi.api.JsfVersion getJsfVersion()
meth public abstract org.netbeans.modules.web.jsfapi.api.Library getLibrary(java.lang.String)
meth public abstract org.openide.util.Lookup getLookup()

CLSS public org.netbeans.modules.web.jsfapi.api.JsfUtils
cons public init()
fld public final static java.lang.String JSF_XHTML_FILE_MIMETYPE = "text/facelets"
meth public static boolean isFaceletsFile(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public final !enum org.netbeans.modules.web.jsfapi.api.JsfVersion
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_1_0
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_1_1
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_1_2
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_2_0
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_2_1
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_2_2
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_2_3
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_3_0
fld public final static org.netbeans.modules.web.jsfapi.api.JsfVersion JSF_4_0
meth public boolean isAtLeast(org.netbeans.modules.web.jsfapi.api.JsfVersion)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getShortName()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion latest()
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsfapi.api.JsfVersion[] values()
supr java.lang.Enum<org.netbeans.modules.web.jsfapi.api.JsfVersion>
hfds version

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.Library
intf org.netbeans.modules.web.jsfapi.api.LibraryInfo
meth public abstract java.lang.String getDefaultNamespace()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends org.netbeans.modules.web.jsfapi.api.LibraryComponent> getComponents()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.LibraryComponent getComponent(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.LibraryType getType()
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.LibraryComponent
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[][] getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.Library getLibrary()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.jsfapi.api.Tag getTag()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.LibraryInfo
meth public abstract java.lang.String getDefaultPrefix()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.util.Set<java.lang.String> getValidNamespaces()

CLSS public final !enum org.netbeans.modules.web.jsfapi.api.LibraryType
 anno 0 java.lang.Deprecated()
fld public final static org.netbeans.modules.web.jsfapi.api.LibraryType CLASS
fld public final static org.netbeans.modules.web.jsfapi.api.LibraryType COMPONENT
fld public final static org.netbeans.modules.web.jsfapi.api.LibraryType COMPOSITE
meth public static org.netbeans.modules.web.jsfapi.api.LibraryType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.jsfapi.api.LibraryType[] values()
supr java.lang.Enum<org.netbeans.modules.web.jsfapi.api.LibraryType>

CLSS public final org.netbeans.modules.web.jsfapi.api.NamespaceUtils
cons public init()
fld public final static java.lang.String JCP_ORG_LOCATION = "http://xmlns.jcp.org"
fld public final static java.lang.String SUN_COM_LOCATION = "http://java.sun.com"
fld public final static java.util.Map<java.lang.String,java.lang.String> NS_MAPPING
meth public static <%0 extends java.lang.Object> {%%0} getForNs(java.util.Map<java.lang.String,{%%0}>,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean containsNsOf(java.util.Collection<java.lang.String>,org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.Tag
meth public abstract boolean hasNonGenenericAttributes()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection<org.netbeans.modules.web.jsfapi.api.Attribute> getAttributes()
meth public abstract org.netbeans.modules.web.jsfapi.api.Attribute getAttribute(java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.jsfapi.api.TagFeature
innr public abstract interface static IterableTagPattern

CLSS public abstract interface static org.netbeans.modules.web.jsfapi.api.TagFeature$IterableTagPattern
 outer org.netbeans.modules.web.jsfapi.api.TagFeature
intf org.netbeans.modules.web.jsfapi.api.TagFeature
meth public abstract org.netbeans.modules.web.jsfapi.api.Attribute getItems()
meth public abstract org.netbeans.modules.web.jsfapi.api.Attribute getVariable()

CLSS public abstract interface org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider
innr public static Query
meth public abstract java.util.Map<java.lang.String,java.lang.String> getInputTextValuesMap(org.openide.filesystems.FileObject)

CLSS public static org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider$Query
 outer org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider
cons public init()
meth public static java.util.Map<java.lang.String,java.lang.String> getInputTextValuesMap(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.jsfapi.spi.JsfReferenceImplementationProvider
meth public abstract java.nio.file.Path artifactPathFor(org.netbeans.modules.web.jsfapi.api.JsfVersion)

CLSS public org.netbeans.modules.web.jsfapi.spi.JsfSupportHandle
 anno 0 java.lang.Deprecated()
cons public init()
meth protected boolean isEnabled()
supr java.lang.Object
hfds caller,instance

CLSS public abstract org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider
cons public init()
meth public abstract org.netbeans.modules.web.jsfapi.api.JsfSupport getSupport(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.web.jsfapi.api.JsfSupport get(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.web.jsfapi.api.JsfSupport get(org.netbeans.modules.parsing.api.Source)
meth public static org.netbeans.modules.web.jsfapi.api.JsfSupport get(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds CACHE,LOGGER

CLSS public org.netbeans.modules.web.jsfapi.spi.LibraryUtils
cons public init()
fld public final static java.lang.String COMPOSITE_LIBRARY_JAKARTA_NS = "jakarta.faces.composite"
fld public final static java.lang.String COMPOSITE_LIBRARY_JCP_NS = "http://xmlns.jcp.org/jsf/composite"
fld public final static java.lang.String COMPOSITE_LIBRARY_SUN_NS = "http://java.sun.com/jsf/composite"
fld public final static java.lang.String XHTML_NS = "http://www.w3.org/1999/xhtml"
meth public static boolean importLibrary(javax.swing.text.Document,org.netbeans.modules.web.jsfapi.api.Library,java.lang.String)
meth public static java.lang.String generateDefaultPrefix(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.String getCompositeLibraryURL(java.lang.String,org.netbeans.modules.web.jsfapi.api.JsfVersion)
meth public static java.util.Map<java.lang.String,org.netbeans.modules.web.jsfapi.api.Library> getDeclaredLibraries(org.netbeans.modules.html.editor.lib.api.HtmlParsingResult)
meth public static java.util.Map<org.netbeans.modules.web.jsfapi.api.Library,java.lang.String> importLibrary(javax.swing.text.Document,java.util.Map<org.netbeans.modules.web.jsfapi.api.Library,java.lang.String>)
meth public static java.util.Set<java.lang.String> getAllCompositeLibraryNamespaces(java.lang.String,org.netbeans.modules.web.jsfapi.api.JsfVersion)
meth public static org.netbeans.api.project.Project[] getOpenedJSFProjects()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider
innr public static Query
meth public abstract <%0 extends org.netbeans.modules.web.jsfapi.api.TagFeature> java.util.Collection<{%%0}> getFeatures(org.netbeans.modules.web.jsfapi.api.Tag,org.netbeans.modules.web.jsfapi.api.Library,java.lang.Class<{%%0}>)

CLSS public static org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider$Query
 outer org.netbeans.modules.web.jsfapi.spi.TagFeatureProvider
cons public init()
meth public static <%0 extends org.netbeans.modules.web.jsfapi.api.TagFeature> java.util.Collection<{%%0}> getFeatures(org.netbeans.modules.web.jsfapi.api.Tag,org.netbeans.modules.web.jsfapi.api.Library,java.lang.Class<{%%0}>)
supr java.lang.Object

