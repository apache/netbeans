#Signature file v4.1
#Version 1.110.0

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

CLSS public abstract interface org.netbeans.modules.java.api.common.project.ProjectPlatformProvider
fld public final static java.lang.String PROP_PROJECT_PLATFORM = "projectPlatform"
meth public abstract org.netbeans.api.java.platform.JavaPlatform getProjectPlatform()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setProjectPlatform(org.netbeans.api.java.platform.JavaPlatform) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.api.common.project.PropertyEvaluatorProvider
meth public abstract org.netbeans.spi.project.support.ant.PropertyEvaluator getPropertyEvaluator()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SEBuildPropertiesProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> createAdditionalProperties(java.lang.String,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<java.lang.String> createConcealedProperties(java.lang.String,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider
innr public abstract interface static ConfigChangeListener
innr public final static !enum ExtensibleCategory
meth public abstract javax.swing.JComponent createComponent(org.netbeans.api.project.Project,org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ConfigChangeListener)
meth public abstract org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory getCategory()
meth public abstract void configUpdated(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ConfigChangeListener
 outer org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider
meth public abstract void propertiesChanged(java.util.Map<java.lang.String,java.lang.String>)

CLSS public final static !enum org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory
 outer org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider
fld public final static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory APPLICATION
fld public final static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory DEPLOYMENT
fld public final static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory PACKAGING
fld public final static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory RUN
meth public static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory valueOf(java.lang.String)
meth public static org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory[] values()
supr java.lang.Enum<org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider$ExtensibleCategory>

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SECustomPropertySaver
meth public abstract void save(org.netbeans.api.project.Project)

CLSS public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder
cons public init(java.io.File,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addCompileLibraries(org.netbeans.api.project.libraries.Library[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addRuntimeLibraries(org.netbeans.api.project.libraries.Library[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addSourceRoots(java.io.File[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addTestRoots(java.io.File[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addDefaultSourceRoots()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder addJVMArguments(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setBuildXmlName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setDistFolder(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setJavaPlatform(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setLibrariesDefinitionFile(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setMainClass(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setMainClassTemplate(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setManifest(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder setSourceLevel(org.openide.modules.SpecificationVersion)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder skipTests(boolean)
meth public org.netbeans.spi.project.support.ant.AntProjectHelper build() throws java.io.IOException
meth public static void createDefaultModuleProperties(org.netbeans.spi.project.support.ant.EditableProperties,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT_PLATFORM_ID,LOG,buildXmlName,compileLibraries,defaultSourceLevel,distFolder,hasDefaultRoots,jvmArgs,librariesDefinition,loggerKey,loggerName,mainClass,mainClassTemplate,manifest,name,platform,projectDirectory,runtimeLibraries,skipTests,sourceRoots,testRoots

CLSS public final org.netbeans.modules.java.j2seproject.api.J2SEProjectConfigurations
meth public static void createConfigurationFiles(org.netbeans.api.project.Project,java.lang.String,java.util.Properties,java.util.Properties) throws java.io.IOException
meth public static void createConfigurationFiles(org.netbeans.api.project.Project,java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties,org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SEProjectPlatform
intf org.netbeans.modules.java.api.common.project.ProjectPlatformProvider

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator
intf org.netbeans.modules.java.api.common.project.PropertyEvaluatorProvider
meth public abstract org.netbeans.spi.project.support.ant.PropertyEvaluator evaluator()
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getPropertyEvaluator()

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider
 anno 0 java.lang.Deprecated()
innr public abstract interface static ConfigChangeListener
meth public abstract javax.swing.JComponent createComponent(org.netbeans.api.project.Project,org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider$ConfigChangeListener)
meth public abstract void configUpdated(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface static org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider$ConfigChangeListener
 outer org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider
meth public abstract void propertiesChanged(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface org.netbeans.modules.java.j2seproject.api.J2SERuntimePlatformProvider
meth public abstract java.util.Collection<? extends org.netbeans.api.java.platform.JavaPlatform> getPlatformType(org.openide.modules.SpecificationVersion,org.netbeans.api.java.queries.SourceLevelQuery$Profile)
 anno 0 org.netbeans.api.annotations.common.NonNull()

