#Signature file v4.1
#Version 1.25.0

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

CLSS public final org.netbeans.modules.gradle.java.api.GradleJavaProject
fld public final static java.lang.String CLASSIFIER_JAVADOC = "javadoc"
fld public final static java.lang.String CLASSIFIER_NONE = ""
fld public final static java.lang.String CLASSIFIER_SOURCES = "sources"
fld public final static java.lang.String CLASSIFIER_TESTS = "tests"
intf java.io.Serializable
meth protected org.netbeans.modules.gradle.java.api.GradleJavaSourceSet createSourceSet(java.lang.String)
meth public java.io.File getArchive(java.lang.String)
meth public java.io.File getMainJar()
meth public java.util.Map<java.lang.String,org.netbeans.modules.gradle.java.api.GradleJavaSourceSet> getSourceSets()
meth public java.util.Set<java.io.File> getCoverageData()
meth public java.util.Set<java.io.File> getTestClassesRoots()
meth public org.netbeans.modules.gradle.java.api.GradleJavaSourceSet containingSourceSet(java.io.File)
meth public org.netbeans.modules.gradle.java.api.GradleJavaSourceSet getMainSourceSet()
meth public static org.netbeans.modules.gradle.java.api.GradleJavaProject get(org.netbeans.api.project.Project)
supr java.lang.Object
hfds archives,coverageData,fileToSourceSetCache,mainJar,sourceSets,testClassesRoots

CLSS public final org.netbeans.modules.gradle.java.api.GradleJavaSourceSet
cons public init(java.lang.String)
fld public final static java.io.File UNKNOWN
fld public final static java.lang.String MAIN_SOURCESET_NAME = "main"
fld public final static java.lang.String TEST_SOURCESET_NAME = "test"
innr public final static !enum ClassPathType
innr public final static !enum SourceType
intf java.io.Serializable
meth public !varargs java.io.File findResource(java.lang.String,boolean,org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType[])
meth public boolean contains(java.io.File)
meth public boolean equals(java.lang.Object)
meth public boolean hasOverlappingSourceDirs()
meth public boolean isTestSourceSet()
meth public boolean outputContains(java.io.File)
meth public final java.util.Collection<java.io.File> getAllDirs()
meth public final java.util.Collection<java.io.File> getAllDirs(boolean)
meth public final java.util.Collection<java.io.File> getAvailableDirs()
meth public final java.util.Collection<java.io.File> getAvailableDirs(boolean)
meth public final java.util.Set<java.io.File> getGroovyDirs()
meth public final java.util.Set<java.io.File> getJavaDirs()
meth public final java.util.Set<java.io.File> getKotlinDirs()
meth public final java.util.Set<java.io.File> getResourcesDirs()
meth public final java.util.Set<java.io.File> getScalaDirs()
meth public int hashCode()
meth public java.io.File findResource(java.lang.String)
meth public java.io.File getOutputClassDir(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.io.File getOutputResources()
meth public java.lang.String getAnnotationProcessorConfigurationName()
meth public java.lang.String getBuildTaskName(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.lang.String getClassesTaskName()
meth public java.lang.String getCompileConfigurationName()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getCompileTaskName(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getProcessResourcesTaskName()
meth public java.lang.String getRuntimeConfigurationName()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getSourcesCompatibility()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getSourcesCompatibility(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.lang.String getTargetCompatibility()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getTargetCompatibility(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.lang.String getTaskName(java.lang.String,java.lang.String)
meth public java.lang.String relativePath(java.io.File)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getCompilerArgs(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.util.Set<java.io.File> getAnnotationProcessorPath()
meth public java.util.Set<java.io.File> getCompileClassPath()
meth public java.util.Set<java.io.File> getGeneratedSourcesDirs()
meth public java.util.Set<java.io.File> getOutputClassDirs()
meth public java.util.Set<java.io.File> getRuntimeClassPath()
meth public java.util.Set<java.io.File> getSourceDirs(org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType)
meth public java.util.Set<org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType> getSourceTypes(java.io.File)
meth public java.util.Set<org.netbeans.modules.gradle.java.api.GradleJavaSourceSet> getSourceDependencies()
meth public org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType getSourceType(java.io.File)
supr java.lang.Object
hfds DEFAULT_SOURCE_COMPATIBILITY,annotationProcessorConfigurationName,annotationProcessorPath,compileClassPath,compileConfigurationName,compilerArgs,name,outputClassDirs,outputResources,outputs,runtimeClassPath,runtimeConfigurationName,sourceDependencies,sources,sourcesCompatibility,targetCompatibility,testSourceSet,webApp

CLSS public final static !enum org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType
 outer org.netbeans.modules.gradle.java.api.GradleJavaSourceSet
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType COMPILE
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType RUNTIME
meth public static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$ClassPathType>

CLSS public final static !enum org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType
 outer org.netbeans.modules.gradle.java.api.GradleJavaSourceSet
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType GENERATED
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType GROOVY
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType JAVA
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType KOTLIN
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType RESOURCES
fld public final static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType SCALA
meth public java.lang.String toString()
meth public static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.java.api.GradleJavaSourceSet$SourceType>

CLSS public final org.netbeans.modules.gradle.java.api.ProjectActions
fld public static java.lang.String TOKEN_JAVAEXEC_ARGS
fld public static java.lang.String TOKEN_JAVAEXEC_CWD
fld public static java.lang.String TOKEN_JAVAEXEC_ENV
fld public static java.lang.String TOKEN_JAVAEXEC_JVMARGS
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath getProjectSourcesClassPath(java.lang.String)
meth public abstract org.netbeans.api.java.classpath.ClassPath[] getProjectClassPath(java.lang.String)

CLSS public final org.netbeans.modules.gradle.java.api.output.Location
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
innr public abstract interface static Finder
meth public boolean isLine()
meth public boolean isMethod()
meth public final static org.netbeans.modules.gradle.java.api.output.Location locationFromCallStackItem(java.lang.String)
meth public java.lang.Integer getLineNum()
meth public java.lang.String getFileName()
meth public java.lang.String getTarget()
meth public java.lang.String toString()
meth public org.netbeans.modules.gradle.java.api.output.Location withNoTarget()
meth public static org.netbeans.modules.gradle.java.api.output.Location parseLocation(java.lang.String)
supr java.lang.Object
hfds CALLSTACK_ITEM_PARSER,classNames,fileName,lineNum,target

CLSS public abstract interface static org.netbeans.modules.gradle.java.api.output.Location$Finder
 outer org.netbeans.modules.gradle.java.api.output.Location
meth public abstract org.openide.filesystems.FileObject findFileObject(org.netbeans.modules.gradle.java.api.output.Location)

CLSS public final org.netbeans.modules.gradle.java.api.output.LocationOpener
cons public init(org.netbeans.modules.gradle.java.api.output.Location)
cons public init(org.netbeans.modules.gradle.java.api.output.Location,org.netbeans.modules.gradle.java.api.output.Location$Finder)
fld public final static org.netbeans.modules.gradle.java.api.output.Location$Finder GLOBAL_FINDER
meth public final void open()
meth public static void openAtLine(org.openide.filesystems.FileObject,int)
meth public static void openAtLine(org.openide.filesystems.FileObject,int,boolean)
supr java.lang.Object
hfds finder,location

CLSS public abstract interface org.netbeans.modules.gradle.java.spi.debug.GradleJavaDebugger
meth public abstract void attachDebugger(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception

