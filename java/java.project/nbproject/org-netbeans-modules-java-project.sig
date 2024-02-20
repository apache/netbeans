#Signature file v4.1
#Version 1.95

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public org.netbeans.api.java.project.JavaProjectConstants
fld public final static java.lang.String ARTIFACT_TYPE_FOLDER = "folder"
fld public final static java.lang.String ARTIFACT_TYPE_JAR = "jar"
fld public final static java.lang.String COMMAND_DEBUG_FIX = "debug.fix"
fld public final static java.lang.String COMMAND_JAVADOC = "javadoc"
fld public final static java.lang.String SOURCES_HINT_MAIN = "main"
fld public final static java.lang.String SOURCES_HINT_TEST = "test"
fld public final static java.lang.String SOURCES_TYPE_JAVA = "java"
fld public final static java.lang.String SOURCES_TYPE_MODULES = "modules"
fld public final static java.lang.String SOURCES_TYPE_RESOURCES = "resources"
supr java.lang.Object

CLSS public org.netbeans.api.java.project.classpath.ProjectClassPathModifier
meth public static boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addProjects(org.netbeans.api.project.Project[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addRoots(java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean addRoots(java.net.URL[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeRoots(java.net.URI[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static boolean removeRoots(java.net.URL[],org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.netbeans.spi.java.project.classpath.ProjectClassPathExtender extenderForModifier(org.netbeans.api.project.Project)
meth public static org.netbeans.spi.java.project.classpath.ProjectClassPathExtender extenderForModifier(org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation)
supr java.lang.Object
hfds LOG
hcls Extensible

CLSS public abstract interface org.netbeans.spi.java.project.classpath.ProjectClassPathExtender
 anno 0 java.lang.Deprecated()
meth public abstract boolean addAntArtifact(org.netbeans.api.project.ant.AntArtifact,java.net.URI) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addArchiveFile(org.openide.filesystems.FileObject) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public abstract boolean addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
 anno 0 java.lang.Deprecated()

CLSS public abstract org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation
cons protected init()
meth protected abstract boolean addAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean addRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeAntArtifacts(org.netbeans.api.project.ant.AntArtifact[],java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeLibraries(org.netbeans.api.project.libraries.Library[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract boolean removeRoots(java.net.URL[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected abstract java.lang.String[] getExtensibleClassPathTypes(org.netbeans.api.project.SourceGroup)
meth protected abstract org.netbeans.api.project.SourceGroup[] getExtensibleSourceGroups()
meth protected boolean addProjects(org.netbeans.api.project.Project[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected boolean addRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected boolean removeRoots(java.net.URI[],org.netbeans.api.project.SourceGroup,java.lang.String) throws java.io.IOException
meth protected final java.lang.String performSharabilityHeuristics(java.net.URI,org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException,java.net.URISyntaxException
meth protected static java.net.URI[] convertURLsToURIs(java.net.URL[])
supr java.lang.Object
hcls Accessor

CLSS public abstract interface org.netbeans.spi.java.project.classpath.ProjectModulesModifier
meth public abstract boolean addRequiredModules(java.lang.String,org.openide.filesystems.FileObject,java.util.Collection<java.net.URL>) throws java.io.IOException
meth public abstract boolean removeRequiredModules(java.lang.String,org.openide.filesystems.FileObject,java.util.Collection<java.net.URL>) throws java.io.IOException
meth public abstract java.lang.String provideModularClasspath(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract java.util.Map<java.net.URL,java.util.Collection<org.netbeans.api.java.classpath.ClassPath>> findModuleUsages(org.openide.filesystems.FileObject,java.util.Collection<java.net.URL>)
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createPropertyBasedClassPathImplementation(java.io.File,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[])
supr java.lang.Object

CLSS public org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport
cons public init()
meth public static org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation createExtraJavadocQueryImplementation(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
meth public static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation createExtraSourceQueryImplementation(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
supr java.lang.Object

CLSS public org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection
meth public static java.util.Set<? extends org.openide.filesystems.FileObject> findJavadocRoots(org.openide.filesystems.FileObject,java.util.concurrent.atomic.AtomicBoolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Set<? extends org.openide.filesystems.FileObject> findSourceRoots(org.openide.filesystems.FileObject,java.util.concurrent.atomic.AtomicBoolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.filesystems.FileObject findJavadocRoot(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject findPackageRoot(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject findSourceRoot(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds JAVADOC_TRAVERSE_DEEPTH,JAVA_FILE,LOG,PACKAGE_INFO,SRC_TRAVERSE_DEEPTH

CLSS public final org.netbeans.spi.java.project.support.LookupMergerSupport
cons public init()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.java.classpath.ClassPathProvider> createClassPathProviderMerger(org.netbeans.spi.java.classpath.ClassPathProvider)
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation> createClassPathModifierMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation> createCompilerOptionsQueryMerger()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation> createJFBLookupMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation> createSFBLookupMerger()
supr java.lang.Object
hcls JFBIMerged,JFBLookupMerger,SFBIMerged,SFBLookupMerger

CLSS public final org.netbeans.spi.java.project.support.PreferredProjectPlatform
meth public static org.netbeans.api.java.platform.JavaPlatform getPreferredPlatform(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void setPreferredPlatform(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds PLATFORM_ANT_NAME,PREFERRED_PLATFORM

CLSS public final org.netbeans.spi.java.project.support.ProjectPlatform
meth public static org.netbeans.api.java.platform.JavaPlatform forProject(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.platform.JavaPlatform forProject(org.netbeans.api.project.Project,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds PLATFORM_ACTIVE,homesByProject,platformsByHome,platformsByProject

