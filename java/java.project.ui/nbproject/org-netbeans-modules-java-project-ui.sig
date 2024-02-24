#Signature file v4.1
#Version 1.98

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

CLSS public final org.netbeans.api.java.project.runner.JavaRunner
cons public init()
fld public final static java.lang.String PROP_APPLICATION_ARGS = "application.args"
fld public final static java.lang.String PROP_CLASSNAME = "classname"
fld public final static java.lang.String PROP_EXECUTE_CLASSPATH = "execute.classpath"
fld public final static java.lang.String PROP_EXECUTE_FILE = "execute.file"
fld public final static java.lang.String PROP_EXECUTE_MODULEPATH = "execute.modulepath"
fld public final static java.lang.String PROP_PLATFORM = "platform"
fld public final static java.lang.String PROP_PLATFORM_JAVA = "platform.java"
fld public final static java.lang.String PROP_PROJECT_NAME = "project.name"
fld public final static java.lang.String PROP_RUNTIME_ENCODING = "runtime.encoding"
fld public final static java.lang.String PROP_RUN_JVMARGS = "run.jvmargs"
fld public final static java.lang.String PROP_WORK_DIR = "work.dir"
fld public final static java.lang.String QUICK_CLEAN = "clean"
fld public final static java.lang.String QUICK_DEBUG = "debug"
fld public final static java.lang.String QUICK_DEBUG_APPLET = "debug-applet"
fld public final static java.lang.String QUICK_PROFILE = "profile"
fld public final static java.lang.String QUICK_PROFILE_APPLET = "profile-applet"
fld public final static java.lang.String QUICK_RUN = "run"
fld public final static java.lang.String QUICK_RUN_APPLET = "run-applet"
fld public final static java.lang.String QUICK_TEST = "junit"
fld public final static java.lang.String QUICK_TEST_DEBUG = "junit-debug"
fld public final static java.lang.String QUICK_TEST_PROFILE = "junit-profile"
meth public static boolean isSupported(java.lang.String,java.util.Map<java.lang.String,?>)
meth public static org.openide.execution.ExecutorTask execute(java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
supr java.lang.Object
hfds LOG

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

CLSS public abstract interface org.netbeans.spi.java.project.runner.JavaRunnerImplementation
meth public abstract boolean isSupported(java.lang.String,java.util.Map<java.lang.String,?>)
meth public abstract org.openide.execution.ExecutorTask execute(java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException

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

CLSS public org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
innr public abstract interface static LibraryDefiner
innr public abstract interface static PlatformUpdatedCallBack
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createPlatformVersionProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createPlatformVersionProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String,org.openide.modules.SpecificationVersion,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NonNull()
meth public !varargs static org.netbeans.spi.project.ui.ProjectProblemsProvider createProfileProblemProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isBroken(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider createReferenceProblemsProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider createReferenceProblemsProvider(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack,java.lang.String[],java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public static void showAlert()
 anno 0 java.lang.Deprecated()
meth public static void showAlert(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static void showCustomizer(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hcls ProjectDecorator

CLSS public abstract interface static org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$LibraryDefiner
 outer org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
meth public abstract java.util.concurrent.Callable<org.netbeans.api.project.libraries.Library> missingLibrary(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport$PlatformUpdatedCallBack
 outer org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport
meth public abstract void platformPropertyUpdated(org.netbeans.api.java.platform.JavaPlatform)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.java.project.support.ui.CreateJavaClassFileFromClipboard
cons public init(org.openide.loaders.DataFolder,java.awt.datatransfer.Transferable)
meth public java.awt.datatransfer.Transferable paste() throws java.io.IOException
supr org.openide.util.datatransfer.PasteType
hfds PUBLIC_MODIFIER,context,t
hcls ClassContent,MyFileObject

CLSS public final org.netbeans.spi.java.project.support.ui.EditJarSupport
cons public init()
innr public final static Item
meth public static org.netbeans.spi.java.project.support.ui.EditJarSupport$Item showEditDialog(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.java.project.support.ui.EditJarSupport$Item)
supr java.lang.Object

CLSS public final static org.netbeans.spi.java.project.support.ui.EditJarSupport$Item
 outer org.netbeans.spi.java.project.support.ui.EditJarSupport
cons public init()
meth public java.lang.String getJarFile()
meth public java.lang.String getJavadocFile()
meth public java.lang.String getSourceFile()
meth public void setJarFile(java.lang.String)
meth public void setJavadocFile(java.lang.String)
meth public void setSourceFile(java.lang.String)
supr java.lang.Object
hfds jarFile,javadocFile,sourceFile

CLSS public org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer
cons public init()
meth public java.lang.String getExcludePattern()
meth public java.lang.String getIncludePattern()
meth public javax.swing.JComponent getVisualizerPanel()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void setExcludePattern(java.lang.String)
meth public void setIncludePattern(java.lang.String)
meth public void setRoots(java.io.File[])
supr java.lang.Object
hfds DELAY,GRANULARITY,RP,busy,excluded,excludes,included,includes,interrupted,listeners,panel,roots,scanCounter,task
hcls RecalculateTask

CLSS public abstract interface org.netbeans.spi.java.project.support.ui.PackageRenameHandler
meth public abstract void handleRename(org.openide.nodes.Node,java.lang.String)

CLSS public org.netbeans.spi.java.project.support.ui.PackageView
meth public static javax.swing.ComboBoxModel createListView(org.netbeans.api.project.SourceGroup)
meth public static javax.swing.ListCellRenderer listRenderer()
meth public static org.openide.nodes.Node createPackageView(org.netbeans.api.project.SourceGroup)
meth public static org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)
supr java.lang.Object
hfds LOG
hcls PackageItem,PackageListCellRenderer,RootNode

CLSS public final org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils
cons public init()
fld public final static java.lang.String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties"
meth public static boolean isLastProjectSharable()
meth public static boolean showMakeSharableWizard(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.ReferenceHelper,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static java.lang.String browseForLibraryLocation(java.lang.String,java.awt.Component,java.io.File)
meth public static void setLastProjectSharable(boolean)
supr java.lang.Object
hfds PROP_ACTIONS,PROP_HELPER,PROP_JAR_REFS,PROP_LAST_SHARABLE,PROP_LIBRARIES,PROP_LOCATION,PROP_REFERENCE_HELPER
hcls CopyIterator,CopyJars,CopyLibraryJars,ErrorProvider,KeepJarAtLocation,KeepLibraryAtLocation

CLSS public abstract interface org.netbeans.spi.java.project.support.ui.templates.JavaFileWizardIteratorFactory
meth public abstract org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor> createIterator(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.java.project.support.ui.templates.JavaTemplates
fld public final static java.lang.String ANNOTATION_TYPE_ICON = "org/netbeans/spi/java/project/support/ui/templates/annotation.png"
fld public final static java.lang.String ENUM_ICON = "org/netbeans/spi/java/project/support/ui/templates/enum.png"
fld public final static java.lang.String INTERFACE_ICON = "org/netbeans/spi/java/project/support/ui/templates/interface.png"
fld public final static java.lang.String JAVA_ICON = "org/netbeans/spi/java/project/support/ui/templates/class.png"
meth public static org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createJavaTemplateIterator()
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[])
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>)
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createPackageChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>,boolean)
supr java.lang.Object

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract org.openide.util.datatransfer.PasteType
cons public init()
intf org.openide.util.HelpCtx$Provider
meth public abstract java.awt.datatransfer.Transferable paste() throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object

