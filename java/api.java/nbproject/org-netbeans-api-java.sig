#Signature file v4.1
#Version 1.89

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.java.classpath.ClassPath
fld public final static java.lang.String BOOT = "classpath/boot"
fld public final static java.lang.String COMPILE = "classpath/compile"
fld public final static java.lang.String DEBUG = "classpath/debug"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String EXECUTE = "classpath/execute"
fld public final static java.lang.String PROP_ENTRIES = "entries"
fld public final static java.lang.String PROP_FLAGS = "flags"
fld public final static java.lang.String PROP_INCLUDES = "includes"
fld public final static java.lang.String PROP_ROOTS = "roots"
fld public final static java.lang.String SOURCE = "classpath/source"
fld public final static org.netbeans.api.java.classpath.ClassPath EMPTY
innr public final Entry
innr public final static !enum Flag
innr public final static !enum PathConversionMode
innr public final static !enum PathEmbeddingMode
meth public boolean equals(java.lang.Object)
meth public final boolean contains(org.openide.filesystems.FileObject)
meth public final boolean isResourceVisible(org.openide.filesystems.FileObject)
meth public final java.lang.ClassLoader getClassLoader(boolean)
meth public final java.lang.String getResourceName(org.openide.filesystems.FileObject)
meth public final java.lang.String getResourceName(org.openide.filesystems.FileObject,char,boolean)
meth public final java.util.List<org.openide.filesystems.FileObject> findAllResources(java.lang.String)
meth public final org.openide.filesystems.FileObject findOwnerRoot(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public final org.openide.filesystems.FileObject findResource(java.lang.String)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String toString()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString(org.netbeans.api.java.classpath.ClassPath$PathConversionMode)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString(org.netbeans.api.java.classpath.ClassPath$PathConversionMode,org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<org.netbeans.api.java.classpath.ClassPath$Entry> entries()
meth public java.util.Set<org.netbeans.api.java.classpath.ClassPath$Flag> getFlags()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject[] getRoots()
meth public static org.netbeans.api.java.classpath.ClassPath getClassPath(org.openide.filesystems.FileObject,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EMPTY_REF,LOG,URL_EMBEDDING,caller,entriesCache,impl,implementations,invalidEntries,invalidRoots,pListener,propSupport,refClassLoader,root2Filter,rootsCache,rootsListener,weakPListeners
hcls RootsListener,SPIListener

CLSS public final org.netbeans.api.java.classpath.ClassPath$Entry
 outer org.netbeans.api.java.classpath.ClassPath
meth public boolean equals(java.lang.Object)
meth public boolean includes(java.lang.String)
meth public boolean includes(java.net.URL)
meth public boolean includes(org.openide.filesystems.FileObject)
meth public boolean isValid()
meth public int hashCode()
meth public java.io.IOException getError()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public org.netbeans.api.java.classpath.ClassPath getDefiningClassPath()
meth public org.openide.filesystems.FileObject getRoot()
supr java.lang.Object
hfds filter,isDataResult,lastError,root,url

CLSS public final static !enum org.netbeans.api.java.classpath.ClassPath$Flag
 outer org.netbeans.api.java.classpath.ClassPath
fld public final static org.netbeans.api.java.classpath.ClassPath$Flag INCOMPLETE
meth public static org.netbeans.api.java.classpath.ClassPath$Flag valueOf(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath$Flag[] values()
supr java.lang.Enum<org.netbeans.api.java.classpath.ClassPath$Flag>

CLSS public final static !enum org.netbeans.api.java.classpath.ClassPath$PathConversionMode
 outer org.netbeans.api.java.classpath.ClassPath
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode FAIL
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode PRINT
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode SKIP
fld public final static org.netbeans.api.java.classpath.ClassPath$PathConversionMode WARN
meth public static org.netbeans.api.java.classpath.ClassPath$PathConversionMode valueOf(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath$PathConversionMode[] values()
supr java.lang.Enum<org.netbeans.api.java.classpath.ClassPath$PathConversionMode>

CLSS public final static !enum org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode
 outer org.netbeans.api.java.classpath.ClassPath
fld public final static org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode EXCLUDE
fld public final static org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode FAIL
fld public final static org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode INCLUDE
meth public static org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode valueOf(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode[] values()
supr java.lang.Enum<org.netbeans.api.java.classpath.ClassPath$PathEmbeddingMode>

CLSS public final org.netbeans.api.java.classpath.GlobalPathRegistry
meth public java.util.Set<org.netbeans.api.java.classpath.ClassPath> getPaths(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.openide.filesystems.FileObject> getSourceRoots()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
meth public static org.netbeans.api.java.classpath.GlobalPathRegistry getDefault()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addGlobalPathRegistryListener(org.netbeans.api.java.classpath.GlobalPathRegistryListener)
meth public void register(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void removeGlobalPathRegistryListener(org.netbeans.api.java.classpath.GlobalPathRegistryListener)
meth public void unregister(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG,classpathListener,instances,listeners,resetCount,resultListener,results,sourceRoots,spi
hcls SFBQListener

CLSS public final org.netbeans.api.java.classpath.GlobalPathRegistryEvent
meth public java.lang.String getId()
meth public java.util.Set<org.netbeans.api.java.classpath.ClassPath> getChangedPaths()
meth public org.netbeans.api.java.classpath.GlobalPathRegistry getRegistry()
supr java.util.EventObject
hfds changed,id

CLSS public abstract interface org.netbeans.api.java.classpath.GlobalPathRegistryListener
intf java.util.EventListener
meth public abstract void pathsAdded(org.netbeans.api.java.classpath.GlobalPathRegistryEvent)
meth public abstract void pathsRemoved(org.netbeans.api.java.classpath.GlobalPathRegistryEvent)

CLSS public org.netbeans.api.java.classpath.JavaClassPathConstants
cons public init()
fld public final static java.lang.String COMPILE_ONLY = "classpath/compile_only"
fld public final static java.lang.String MODULE_BOOT_PATH = "modules/boot"
fld public final static java.lang.String MODULE_CLASS_PATH = "modules/classpath"
fld public final static java.lang.String MODULE_COMPILE_PATH = "modules/compile"
fld public final static java.lang.String MODULE_EXECUTE_CLASS_PATH = "modules/execute-classpath"
fld public final static java.lang.String MODULE_EXECUTE_PATH = "modules/execute"
fld public final static java.lang.String MODULE_PROCESSOR_PATH = "modules/processor"
fld public final static java.lang.String MODULE_SOURCE_PATH = "modules/source"
fld public final static java.lang.String PROCESSOR_PATH = "classpath/processor"
supr java.lang.Object

CLSS public org.netbeans.api.java.queries.AccessibilityQuery
innr public abstract static !enum Accessibility
innr public final static Result
meth public static java.lang.Boolean isPubliclyAccessible(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.queries.AccessibilityQuery$Result isPubliclyAccessible2(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds implementations,implementations2
hcls Adapter

CLSS public abstract static !enum org.netbeans.api.java.queries.AccessibilityQuery$Accessibility
 outer org.netbeans.api.java.queries.AccessibilityQuery
fld public final static org.netbeans.api.java.queries.AccessibilityQuery$Accessibility EXPORTED
fld public final static org.netbeans.api.java.queries.AccessibilityQuery$Accessibility PRIVATE
fld public final static org.netbeans.api.java.queries.AccessibilityQuery$Accessibility UNKNOWN
meth public static org.netbeans.api.java.queries.AccessibilityQuery$Accessibility valueOf(java.lang.String)
meth public static org.netbeans.api.java.queries.AccessibilityQuery$Accessibility[] values()
supr java.lang.Enum<org.netbeans.api.java.queries.AccessibilityQuery$Accessibility>

CLSS public final static org.netbeans.api.java.queries.AccessibilityQuery$Result
 outer org.netbeans.api.java.queries.AccessibilityQuery
meth public org.netbeans.api.java.queries.AccessibilityQuery$Accessibility getAccessibility()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate,listeners,spiListener

CLSS public org.netbeans.api.java.queries.AnnotationProcessingQuery
innr public abstract interface static Result
innr public final static !enum Trigger
meth public static org.netbeans.api.java.queries.AnnotationProcessingQuery$Result getAnnotationProcessingOptions(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EMPTY

CLSS public abstract interface static org.netbeans.api.java.queries.AnnotationProcessingQuery$Result
 outer org.netbeans.api.java.queries.AnnotationProcessingQuery
meth public abstract java.lang.Iterable<? extends java.lang.String> annotationProcessorsToRun()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.net.URL sourceOutputDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Map<? extends java.lang.String,? extends java.lang.String> processorOptions()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<? extends org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger> annotationProcessingEnabled()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static !enum org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger
 outer org.netbeans.api.java.queries.AnnotationProcessingQuery
fld public final static org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger IN_EDITOR
fld public final static org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger ON_SCAN
meth public static org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger valueOf(java.lang.String)
meth public static org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger[] values()
supr java.lang.Enum<org.netbeans.api.java.queries.AnnotationProcessingQuery$Trigger>

CLSS public final org.netbeans.api.java.queries.BinaryForSourceQuery
innr public abstract interface static Result
innr public abstract static Result2
meth public static org.netbeans.api.java.queries.BinaryForSourceQuery$Result findBinaryRoots(java.net.URL)
meth public static org.netbeans.api.java.queries.BinaryForSourceQuery$Result2 findBinaryRoots2(java.net.URL)
supr java.lang.Object
hfds CACHE,LOG
hcls DefaultResult,QueriesAccessorImpl,Result2Impl

CLSS public abstract interface static org.netbeans.api.java.queries.BinaryForSourceQuery$Result
 outer org.netbeans.api.java.queries.BinaryForSourceQuery
meth public abstract java.net.URL[] getRoots()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract static org.netbeans.api.java.queries.BinaryForSourceQuery$Result2
 outer org.netbeans.api.java.queries.BinaryForSourceQuery
intf org.netbeans.api.java.queries.BinaryForSourceQuery$Result
meth public abstract boolean preferBinaries()
supr java.lang.Object

CLSS public final org.netbeans.api.java.queries.CompilerOptionsQuery
innr public final static Result
meth public static org.netbeans.api.java.queries.CompilerOptionsQuery$Result getOptions(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds impls

CLSS public final static org.netbeans.api.java.queries.CompilerOptionsQuery$Result
 outer org.netbeans.api.java.queries.CompilerOptionsQuery
meth public java.util.List<? extends java.lang.String> getArguments()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EMPTY,changeListener,listeners,results

CLSS public org.netbeans.api.java.queries.JavadocForBinaryQuery
innr public abstract interface static Result
meth public static org.netbeans.api.java.queries.JavadocForBinaryQuery$Result findJavadoc(java.net.URL)
supr java.lang.Object
hfds EMPTY_RESULT,LOG,implementations
hcls EmptyResult

CLSS public abstract interface static org.netbeans.api.java.queries.JavadocForBinaryQuery$Result
 outer org.netbeans.api.java.queries.JavadocForBinaryQuery
meth public abstract java.net.URL[] getRoots()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.api.java.queries.SourceForBinaryQuery
innr public abstract interface static Result
innr public static Result2
meth public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result findSourceRoots(java.net.URL)
meth public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result2 findSourceRoots2(java.net.URL)
supr java.lang.Object
hfds EMPTY_RESULT,LOG,implementations
hcls EmptyResult

CLSS public abstract interface static org.netbeans.api.java.queries.SourceForBinaryQuery$Result
 outer org.netbeans.api.java.queries.SourceForBinaryQuery
meth public abstract org.openide.filesystems.FileObject[] getRoots()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public static org.netbeans.api.java.queries.SourceForBinaryQuery$Result2
 outer org.netbeans.api.java.queries.SourceForBinaryQuery
intf org.netbeans.api.java.queries.SourceForBinaryQuery$Result
meth public boolean preferSources()
meth public org.openide.filesystems.FileObject[] getRoots()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds changeSupport,delegate,spiListener

CLSS public final org.netbeans.api.java.queries.SourceJavadocAttacher
innr public abstract interface static AttachmentListener
meth public static void attachJavadoc(java.net.URL,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static void attachJavadoc(java.net.URL,org.openide.util.Lookup,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static void attachSources(java.net.URL,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static void attachSources(java.net.URL,org.openide.util.Lookup,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG
hcls AttacherExecution

CLSS public abstract interface static org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener
 outer org.netbeans.api.java.queries.SourceJavadocAttacher
meth public abstract void attachmentFailed()
meth public abstract void attachmentSucceeded()

CLSS public org.netbeans.api.java.queries.SourceLevelQuery
fld public final static org.openide.modules.SpecificationVersion MINIMAL_SOURCE_LEVEL
innr public final static Result
innr public static !enum Profile
meth public static java.lang.String getSourceLevel(org.openide.filesystems.FileObject)
meth public static org.netbeans.api.java.queries.SourceLevelQuery$Result getSourceLevel2(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds JDK8,LOGGER,ORIG_VERSION_SCHEME,VERONA_VERSION_SCHEME,implementations,implementations2

CLSS public static !enum org.netbeans.api.java.queries.SourceLevelQuery$Profile
 outer org.netbeans.api.java.queries.SourceLevelQuery
fld public final static org.netbeans.api.java.queries.SourceLevelQuery$Profile COMPACT1
fld public final static org.netbeans.api.java.queries.SourceLevelQuery$Profile COMPACT2
fld public final static org.netbeans.api.java.queries.SourceLevelQuery$Profile COMPACT3
fld public final static org.netbeans.api.java.queries.SourceLevelQuery$Profile DEFAULT
meth public boolean isSupportedIn(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.java.queries.SourceLevelQuery$Profile forName(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.java.queries.SourceLevelQuery$Profile valueOf(java.lang.String)
meth public static org.netbeans.api.java.queries.SourceLevelQuery$Profile[] values()
supr java.lang.Enum<org.netbeans.api.java.queries.SourceLevelQuery$Profile>
hfds displayName,name,profilesByName,supportedFrom

CLSS public final static org.netbeans.api.java.queries.SourceLevelQuery$Result
 outer org.netbeans.api.java.queries.SourceLevelQuery
meth public boolean supportsChanges()
meth public java.lang.String getSourceLevel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.java.queries.SourceLevelQuery$Profile getProfile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds cs,delegate,spiListener

CLSS public org.netbeans.api.java.queries.UnitTestForSourceQuery
meth public static java.net.URL findSource(org.openide.filesystems.FileObject)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL findUnitTest(org.openide.filesystems.FileObject)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL[] findSources(org.openide.filesystems.FileObject)
meth public static java.net.URL[] findUnitTests(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds implementations,mrImplementations

CLSS public final org.netbeans.spi.java.classpath.ClassPathFactory
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(org.netbeans.spi.java.classpath.ClassPathImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.java.classpath.ClassPathImplementation
fld public final static java.lang.String PROP_RESOURCES = "resources"
meth public abstract java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation> getResources()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.java.classpath.ClassPathProvider
meth public abstract org.netbeans.api.java.classpath.ClassPath findClassPath(org.openide.filesystems.FileObject,java.lang.String)

CLSS public abstract interface org.netbeans.spi.java.classpath.FilteringPathResourceImplementation
fld public final static java.lang.String PROP_INCLUDES = "includes"
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth public abstract boolean includes(java.net.URL,java.lang.String)

CLSS public abstract interface org.netbeans.spi.java.classpath.FlaggedClassPathImplementation
fld public final static java.lang.String PROP_FLAGS = "flags"
intf org.netbeans.spi.java.classpath.ClassPathImplementation
meth public abstract java.util.Set<org.netbeans.api.java.classpath.ClassPath$Flag> getFlags()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.spi.java.classpath.GlobalPathRegistryImplementation
cons public init()
meth protected abstract java.util.Set<org.netbeans.api.java.classpath.ClassPath> clear()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth protected abstract java.util.Set<org.netbeans.api.java.classpath.ClassPath> getPaths(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected abstract java.util.Set<org.netbeans.api.java.classpath.ClassPath> register(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth protected abstract java.util.Set<org.netbeans.api.java.classpath.ClassPath> unregister(java.lang.String,org.netbeans.api.java.classpath.ClassPath[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds owner
hcls AccessorImpl

CLSS public abstract interface org.netbeans.spi.java.classpath.PathResourceImplementation
fld public final static java.lang.String PROP_ROOTS = "roots"
meth public abstract java.net.URL[] getRoots()
meth public abstract org.netbeans.spi.java.classpath.ClassPathImplementation getContent()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.spi.java.classpath.support.ClassPathSupport
innr public abstract interface static Selector
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createClassPath(java.net.URL[])
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createClassPath(org.openide.filesystems.FileObject[])
meth public !varargs static org.netbeans.api.java.classpath.ClassPath createProxyClassPath(org.netbeans.api.java.classpath.ClassPath[])
meth public !varargs static org.netbeans.spi.java.classpath.ClassPathImplementation createProxyClassPathImplementation(org.netbeans.spi.java.classpath.ClassPathImplementation[])
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(java.lang.String)
meth public static org.netbeans.api.java.classpath.ClassPath createClassPath(java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation>)
meth public static org.netbeans.api.java.classpath.ClassPath createMultiplexClassPath(org.netbeans.spi.java.classpath.support.ClassPathSupport$Selector)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.java.classpath.ClassPathImplementation createClassPathImplementation(java.util.List<? extends org.netbeans.spi.java.classpath.PathResourceImplementation>)
meth public static org.netbeans.spi.java.classpath.PathResourceImplementation createResource(java.net.URL)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.java.classpath.support.ClassPathSupport$Selector
 outer org.netbeans.spi.java.classpath.support.ClassPathSupport
fld public final static java.lang.String PROP_ACTIVE_CLASS_PATH = "activeClassPath"
meth public abstract org.netbeans.api.java.classpath.ClassPath getActiveClassPath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.netbeans.spi.java.classpath.support.CompositePathResourceBase
cons public init()
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth protected abstract org.netbeans.spi.java.classpath.ClassPathImplementation createContent()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final java.net.URL[] getRoots()
meth public final org.netbeans.spi.java.classpath.ClassPathImplementation getContent()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds model,pListeners,roots

CLSS public abstract org.netbeans.spi.java.classpath.support.PathResourceBase
cons public init()
intf org.netbeans.spi.java.classpath.PathResourceImplementation
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pListeners

CLSS public abstract interface org.netbeans.spi.java.queries.AccessibilityQueryImplementation
meth public abstract java.lang.Boolean isPubliclyAccessible(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.java.queries.AccessibilityQueryImplementation2
innr public abstract interface static Result
meth public abstract org.netbeans.spi.java.queries.AccessibilityQueryImplementation2$Result isPubliclyAccessible(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.java.queries.AccessibilityQueryImplementation2$Result
 outer org.netbeans.spi.java.queries.AccessibilityQueryImplementation2
meth public abstract org.netbeans.api.java.queries.AccessibilityQuery$Accessibility getAccessibility()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation
meth public abstract org.netbeans.api.java.queries.AnnotationProcessingQuery$Result getAnnotationProcessingOptions(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation
meth public abstract org.netbeans.api.java.queries.BinaryForSourceQuery$Result findBinaryRoots(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2<%0 extends java.lang.Object>
intf org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation
meth public abstract boolean computePreferBinaries({org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2%0})
meth public abstract java.net.URL[] computeRoots({org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2%0})
meth public abstract void computeChangeListener({org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2%0},boolean,javax.swing.event.ChangeListener)
meth public abstract {org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2%0} findBinaryRoots2(java.net.URL)
meth public org.netbeans.api.java.queries.BinaryForSourceQuery$Result2 findBinaryRoots(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation
innr public abstract static Result
meth public abstract org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation$Result getOptions(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract static org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation$Result
 outer org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation
cons public init()
meth protected final java.util.List<java.lang.String> parseLine(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<? extends java.lang.String> getArguments()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation
meth public abstract org.netbeans.api.java.queries.JavadocForBinaryQuery$Result findJavadoc(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation
meth public abstract java.net.URL[] findSources(org.openide.filesystems.FileObject)
meth public abstract java.net.URL[] findUnitTests(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
meth public abstract org.netbeans.api.java.queries.SourceForBinaryQuery$Result findSourceRoots(java.net.URL)

CLSS public abstract interface org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2
innr public abstract interface static Result
intf org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation
meth public abstract org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result findSourceRoots2(java.net.URL)

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result
 outer org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2
intf org.netbeans.api.java.queries.SourceForBinaryQuery$Result
meth public abstract boolean preferSources()

CLSS public abstract interface org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation
innr public abstract interface static Definer
innr public abstract interface static Definer2
meth public abstract boolean attachJavadoc(java.net.URL,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean attachSources(java.net.URL,org.netbeans.api.java.queries.SourceJavadocAttacher$AttachmentListener) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation$Definer
 outer org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<? extends java.net.URL> getJavadoc(java.net.URL,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<? extends java.net.URL> getSources(java.net.URL,java.util.concurrent.Callable<java.lang.Boolean>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation$Definer2
 outer org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation
intf org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation$Definer
meth public abstract boolean accepts(java.net.URL)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.java.queries.SourceLevelQueryImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getSourceLevel(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.spi.java.queries.SourceLevelQueryImplementation2
innr public abstract interface static Result
innr public abstract interface static Result2
meth public abstract org.netbeans.spi.java.queries.SourceLevelQueryImplementation2$Result getSourceLevel(org.openide.filesystems.FileObject)

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceLevelQueryImplementation2$Result
 outer org.netbeans.spi.java.queries.SourceLevelQueryImplementation2
meth public abstract java.lang.String getSourceLevel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.spi.java.queries.SourceLevelQueryImplementation2$Result2
 outer org.netbeans.spi.java.queries.SourceLevelQueryImplementation2
intf org.netbeans.spi.java.queries.SourceLevelQueryImplementation2$Result
meth public abstract org.netbeans.api.java.queries.SourceLevelQuery$Profile getProfile()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.net.URL findSource(org.openide.filesystems.FileObject)
meth public abstract java.net.URL findUnitTest(org.openide.filesystems.FileObject)

CLSS public abstract org.netbeans.spi.java.queries.support.SourceForBinaryQueryImpl2Base
cons public init()
intf org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2
meth protected final org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2$Result asResult(org.netbeans.api.java.queries.SourceForBinaryQuery$Result)
supr java.lang.Object

