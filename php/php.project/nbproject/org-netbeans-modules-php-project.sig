#Signature file v4.1
#Version 2.166

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

CLSS public final org.netbeans.modules.php.project.api.PhpAnnotations
fld public final static java.lang.String PROP_RESOLVE_DEPRECATED_ELEMENTS = "PROP_RESOLVE_DEPRECATED_ELEMENTS"
fld public final static java.lang.String PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS = "PROP_UNKNOWN_ANNOTATIONS_AS_TYPE_ANNOTATIONS"
intf java.beans.PropertyChangeListener
meth public boolean isResolveDeprecatedElements()
meth public boolean isUnknownAnnotationsAsTypeAnnotations()
meth public java.util.List<org.netbeans.modules.php.spi.annotation.AnnotationCompletionTagProvider> getCompletionTagProviders(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.php.project.api.PhpAnnotations getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds INSTANCE,cache,propertyChangeSupport

CLSS public final org.netbeans.modules.php.project.api.PhpEditorExtender
meth public static org.netbeans.modules.php.spi.editor.EditorExtender forFileObject(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds EMPTY_EDITOR_EXTENDER
hcls EmptyEditorExtender

CLSS public final org.netbeans.modules.php.project.api.PhpLanguageProperties
fld public final static boolean ASP_TAGS_ENABLED
fld public final static boolean SHORT_TAGS_ENABLED = false
fld public final static java.lang.String PROP_ASP_TAGS
fld public final static java.lang.String PROP_PHP_VERSION
fld public final static java.lang.String PROP_SHORT_TAGS
meth public boolean areAspTagsEnabled()
meth public boolean areShortTagsEnabled()
meth public java.lang.String toString()
meth public org.netbeans.modules.php.api.PhpVersion getPhpVersion()
meth public static org.netbeans.modules.php.project.api.PhpLanguageProperties forFileObject(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.php.project.api.PhpLanguageProperties getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds INSTANCE,impl
hcls DefaultOptions,PhpLanguageOptionsImpl,ProjectOptions

CLSS public final org.netbeans.modules.php.project.api.PhpOptions
fld public final static java.lang.String PROP_PHP_DEBUGGER_MAX_CHILDREN = "propPhpDebuggerMaxChildren"
fld public final static java.lang.String PROP_PHP_DEBUGGER_MAX_STRUCTURES_DEPTH = "propPhpDebuggerMaxStructuresDepth"
fld public final static java.lang.String PROP_PHP_DEBUGGER_PORT = "propPhpDebuggerPort"
fld public final static java.lang.String PROP_PHP_DEBUGGER_RESOLVE_BREAKPOINTS = "propPhpDebuggerResolveBreakpoints"
fld public final static java.lang.String PROP_PHP_DEBUGGER_SESSION_ID = "propPhpDebuggerSessionId"
fld public final static java.lang.String PROP_PHP_DEBUGGER_SHOW_CONSOLE = "propPhpDebuggerShowConsole"
fld public final static java.lang.String PROP_PHP_DEBUGGER_SHOW_URLS = "propPhpDebuggerShowUrls"
fld public final static java.lang.String PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE = "propPhpDebuggerStopAtFirstLine"
fld public final static java.lang.String PROP_PHP_DEBUGGER_WATCHES_AND_EVAL = "propPhpDebuggerWatchesAndEval"
fld public final static java.lang.String PROP_PHP_GLOBAL_INCLUDE_PATH = "propPhpGlobalIncludePath"
fld public final static java.lang.String PROP_PHP_INTERPRETER = "propPhpInterpreter"
meth public boolean isDebuggerResolveBreakpoints()
meth public boolean isDebuggerShowDebuggerConsole()
meth public boolean isDebuggerShowRequestedUrls()
meth public boolean isDebuggerStoppedAtTheFirstLine()
meth public boolean isDebuggerWatchesAndEval()
meth public int getDebuggerMaxChildren()
meth public int getDebuggerMaxDataLength()
meth public int getDebuggerMaxStructuresDepth()
meth public int getDebuggerPort()
meth public java.lang.String getDebuggerSessionId()
meth public java.lang.String getPhpGlobalIncludePath()
meth public java.lang.String getPhpInterpreter()
meth public java.lang.String[] getPhpGlobalIncludePathAsArray()
meth public static org.netbeans.modules.php.project.api.PhpOptions getInstance()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds INSTANCE,propertyChangeSupport

CLSS public final org.netbeans.modules.php.project.api.PhpProjectUtils
meth public static boolean isPhpProject(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.php.project.api.PhpSeleniumProvider
meth public abstract boolean isSupportEnabled(org.openide.filesystems.FileObject[])
meth public abstract java.util.List<java.lang.Object> getTestSourceRoots(java.util.Collection<org.netbeans.api.project.SourceGroup>,org.openide.filesystems.FileObject)
meth public abstract org.openide.filesystems.FileObject getTestDirectory(boolean)
meth public abstract void runAllTests()

CLSS public final org.netbeans.modules.php.project.api.PhpSourcePath
fld public final static java.lang.String BOOT_CP = "classpath/php-boot"
fld public final static java.lang.String PROJECT_BOOT_CP = "classpath/php-project-boot"
fld public final static java.lang.String SOURCE_CP = "classpath/php-source"
innr public final static !enum FileType
meth public static java.util.List<org.openide.filesystems.FileObject> getIncludePath(org.openide.filesystems.FileObject)
meth public static java.util.List<org.openide.filesystems.FileObject> getPreindexedFolders()
meth public static org.netbeans.modules.php.project.api.PhpSourcePath$FileType getFileType(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject resolveFile(org.openide.filesystems.FileObject,java.lang.String)
supr java.lang.Object
hfds DEFAULT_PHP_SOURCE_PATH,phpStubsFolder
hcls DefaultPhpSourcePath

CLSS public final static !enum org.netbeans.modules.php.project.api.PhpSourcePath$FileType
 outer org.netbeans.modules.php.project.api.PhpSourcePath
fld public final static org.netbeans.modules.php.project.api.PhpSourcePath$FileType INCLUDE
fld public final static org.netbeans.modules.php.project.api.PhpSourcePath$FileType INTERNAL
fld public final static org.netbeans.modules.php.project.api.PhpSourcePath$FileType SOURCE
fld public final static org.netbeans.modules.php.project.api.PhpSourcePath$FileType TEST
fld public final static org.netbeans.modules.php.project.api.PhpSourcePath$FileType UNKNOWN
meth public static org.netbeans.modules.php.project.api.PhpSourcePath$FileType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.project.api.PhpSourcePath$FileType[] values()
supr java.lang.Enum<org.netbeans.modules.php.project.api.PhpSourcePath$FileType>

