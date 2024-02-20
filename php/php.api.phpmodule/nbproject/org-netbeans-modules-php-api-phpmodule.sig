#Signature file v4.1
#Version 2.95

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

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

CLSS public final org.netbeans.modules.php.api.PhpConstants
fld public final static java.lang.String SOURCES_TYPE_PHP = "PHPSOURCE"
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.php.api.PhpOptions
meth public abstract java.lang.String getDebuggerSessionId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getPhpInterpreter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final !enum org.netbeans.modules.php.api.PhpVersion
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_5
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_53
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_54
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_55
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_56
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_70
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_71
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_72
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_73
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_74
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_80
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_81
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_82
fld public final static org.netbeans.modules.php.api.PhpVersion PHP_83
meth public boolean hasConstantsInTraits()
meth public boolean hasMixedType()
meth public boolean hasNamespaces()
meth public boolean hasNeverType()
meth public boolean hasNullAndFalseAndTrueTypes()
meth public boolean hasNullableTypes()
meth public boolean hasObjectType()
meth public boolean hasOverrideAttribute()
meth public boolean hasPropertyTypes()
meth public boolean hasScalarAndReturnTypes()
meth public boolean hasVoidReturnType()
meth public boolean isSupportedVersion()
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public static org.netbeans.modules.php.api.PhpVersion getDefault()
meth public static org.netbeans.modules.php.api.PhpVersion getLegacy()
meth public static org.netbeans.modules.php.api.PhpVersion valueOf(java.lang.String)
meth public static org.netbeans.modules.php.api.PhpVersion[] values()
supr java.lang.Enum<org.netbeans.modules.php.api.PhpVersion>
hfds displayName,namespaces
hcls Period

CLSS public abstract interface org.netbeans.modules.php.api.phpmodule.PhpModule
fld public final static java.lang.String PROPERTY_FRAMEWORKS = "PROPERTY_FRAMEWORKS"
innr public final static Factory
intf org.openide.util.Lookup$Provider
meth public abstract boolean isBroken()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<org.openide.filesystems.FileObject> getTestDirectories()
meth public abstract java.util.prefs.Preferences getPreferences(java.lang.Class<?>,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getProjectDirectory()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getSourceDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.filesystems.FileObject getTestDirectory(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void notifyPropertyChanged(java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.php.api.phpmodule.PhpModule$Factory
 outer org.netbeans.modules.php.api.phpmodule.PhpModule
cons public init()
meth public static org.netbeans.modules.php.api.phpmodule.PhpModule forFileObject(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.api.phpmodule.PhpModule inferPhpModule()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.api.phpmodule.PhpModule lookupPhpModule(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.api.phpmodule.PhpModule lookupPhpModule(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator
innr public final static CreateProperties
innr public final static CreatePropertiesValidator
meth public abstract org.netbeans.modules.php.api.phpmodule.PhpModule createModule(org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties
 outer org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator
cons public init()
meth public boolean isAutoconfigured()
meth public java.io.File getProjectDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getSourcesDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.nio.charset.Charset getCharset()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.api.PhpVersion getPhpVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setAutoconfigured(boolean)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setCharset(java.nio.charset.Charset)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setPhpVersion(org.netbeans.modules.php.api.PhpVersion)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setProjectDirectory(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties setSourcesDirectory(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds autoconfigured,charset,name,phpVersion,projectDirectory,sourcesDirectory

CLSS public final static org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreatePropertiesValidator
 outer org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator
cons public init()
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreatePropertiesValidator validate(org.netbeans.modules.php.api.phpmodule.PhpModuleGenerator$CreateProperties)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.validation.ValidationResult getResult()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds result

CLSS public final org.netbeans.modules.php.api.phpmodule.PhpModuleProperties
cons public init()
innr public abstract interface static Factory
meth public java.lang.String getEncoding()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<java.lang.String> getIncludePath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setEncoding(java.lang.String)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setIncludePath(java.util.List<java.lang.String>)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setIndexFile(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setTests(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setUrl(java.lang.String)
meth public org.netbeans.modules.php.api.phpmodule.PhpModuleProperties setWebRoot(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject getIndexFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.filesystems.FileObject getTests()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.filesystems.FileObject getWebRoot()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds encoding,includePath,indexFile,tests,url,webRoot
hcls PhpModulePropertiesData

CLSS public abstract interface static org.netbeans.modules.php.api.phpmodule.PhpModuleProperties$Factory
 outer org.netbeans.modules.php.api.phpmodule.PhpModuleProperties
meth public abstract org.netbeans.modules.php.api.phpmodule.PhpModuleProperties getProperties()

CLSS public abstract interface org.netbeans.modules.php.api.queries.PhpVisibilityQuery
meth public abstract boolean isVisible(java.io.File)
meth public abstract boolean isVisible(org.openide.filesystems.FileObject)
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> getCodeAnalysisExcludeFiles()
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> getIgnoredFiles()

CLSS public final org.netbeans.modules.php.api.queries.Queries
meth public static org.netbeans.modules.php.api.queries.PhpVisibilityQuery getVisibilityQuery(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds DEFAULT_PHP_VISIBILITY_QUERY
hcls DefaultPhpVisibilityQuery

CLSS public final org.netbeans.modules.php.api.util.FileUtils
fld public final static java.lang.String PHP_MIME_TYPE = "text/x-php5"
innr public abstract interface static ZipEntryFilter
meth public !varargs static java.util.List<java.lang.String> findFileOnUsersPath(java.lang.String[])
meth public static boolean isDirectoryLink(java.io.File)
meth public static boolean isDirectoryWritable(java.io.File)
meth public static boolean isPhpFile(org.openide.filesystems.FileObject)
meth public static java.lang.String getScriptExtension(boolean)
meth public static java.lang.String validateDirectory(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String validateDirectory(java.lang.String,java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String validateFile(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String validateFile(java.lang.String,java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.util.List<java.lang.String> findFileOnUsersPath(java.lang.String)
meth public static org.openide.filesystems.FileObject getCommonRoot(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.filesystems.FileObject getFileObject(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.xml.sax.XMLReader createXmlReader() throws org.xml.sax.SAXException
meth public static void openFile(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void openFile(java.io.File,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void reformatFile(java.io.File) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void reformatFile(org.openide.loaders.DataObject) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void saveFile(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void saveFile(org.openide.loaders.DataObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void unzip(java.lang.String,java.io.File,org.netbeans.modules.php.api.util.FileUtils$ZipEntryFilter) throws java.io.IOException
supr java.lang.Object
hfds DUMMY_ZIP_ENTRY_FILTER,IS_MAC,IS_UNIX,IS_WINDOWS,LOGGER

CLSS public abstract interface static org.netbeans.modules.php.api.util.FileUtils$ZipEntryFilter
 outer org.netbeans.modules.php.api.util.FileUtils
meth public abstract boolean accept(java.util.zip.ZipEntry)
meth public abstract java.lang.String getName(java.util.zip.ZipEntry)

CLSS public final org.netbeans.modules.php.api.util.StringUtils
meth public static boolean hasText(java.lang.String)
meth public static boolean isEmpty(java.lang.String)
meth public static java.lang.String capitalize(java.lang.String)
meth public static java.lang.String decapitalize(java.lang.String)
meth public static java.lang.String implode(java.util.Collection<java.lang.String>,java.lang.String)
meth public static java.lang.String truncate(java.lang.String,int,int,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String webalize(java.lang.String)
meth public static java.util.List<java.lang.String> explode(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.regex.Pattern getExactPattern(java.lang.String)
meth public static java.util.regex.Pattern getPattern(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.php.api.util.UiUtils
fld public final static java.lang.String CUSTOMIZER_PATH = "org-netbeans-modules-php-project"
fld public final static java.lang.String FRAMEWORKS_AND_TOOLS_OPTIONS_PATH = "org-netbeans-modules-php-project-ui-options-PHPOptionsCategory/FrameworksAndTools"
fld public final static java.lang.String FRAMEWORKS_AND_TOOLS_SUB_PATH = "FrameworksAndTools"
fld public final static java.lang.String GENERAL_OPTIONS_SUBCATEGORY = "General"
fld public final static java.lang.String OPTIONS_PATH = "org-netbeans-modules-php-project-ui-options-PHPOptionsCategory"
innr public abstract interface static !annotation PhpOptionsPanelRegistration
innr public final static SearchWindow
meth public static java.awt.Image getTreeFolderIcon(boolean)
meth public static void invalidScriptProvided(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void invalidScriptProvided(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static void invalidScriptProvided(org.netbeans.modules.php.api.phpmodule.PhpModule,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static void processExecutionException(java.util.concurrent.ExecutionException)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void processExecutionException(java.util.concurrent.ExecutionException,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static void processExecutionException(java.util.concurrent.ExecutionException,org.netbeans.modules.php.api.phpmodule.PhpModule,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static void showGeneralOptions()
meth public static void showOptions(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static void warnBrokenProject(org.netbeans.modules.php.api.phpmodule.PhpModule)
supr java.lang.Object
hfds ICON_KEY_UIMANAGER,ICON_KEY_UIMANAGER_NB,ICON_PATH,OPENED_ICON_KEY_UIMANAGER,OPENED_ICON_KEY_UIMANAGER_NB,OPENED_ICON_PATH

CLSS public abstract interface static !annotation org.netbeans.modules.php.api.util.UiUtils$PhpOptionsPanelRegistration
 outer org.netbeans.modules.php.api.util.UiUtils
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String keywords()
meth public abstract !hasdefault java.lang.String keywordsCategory()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String id()

CLSS public final static org.netbeans.modules.php.api.util.UiUtils$SearchWindow
 outer org.netbeans.modules.php.api.util.UiUtils
innr public abstract interface static SearchWindowSupport
meth public static java.lang.String search(org.netbeans.modules.php.api.util.UiUtils$SearchWindow$SearchWindowSupport)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.php.api.util.UiUtils$SearchWindow$SearchWindowSupport
 outer org.netbeans.modules.php.api.util.UiUtils$SearchWindow
meth public abstract java.lang.String getListTitle()
meth public abstract java.lang.String getNoItemsFound()
meth public abstract java.lang.String getPleaseWaitPart()
meth public abstract java.lang.String getWindowTitle()
meth public abstract java.util.List<java.lang.String> detect()

CLSS public final org.netbeans.modules.php.api.validation.ValidationResult
cons public init()
cons public init(org.netbeans.modules.php.api.validation.ValidationResult)
innr public final static Message
meth public boolean hasErrors()
meth public boolean hasWarnings()
meth public boolean isFaultless()
meth public java.util.List<org.netbeans.modules.php.api.validation.ValidationResult$Message> getErrors()
meth public java.util.List<org.netbeans.modules.php.api.validation.ValidationResult$Message> getWarnings()
meth public org.netbeans.modules.php.api.validation.ValidationResult$Message getFirstError()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.php.api.validation.ValidationResult$Message getFirstWarning()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void addError(org.netbeans.modules.php.api.validation.ValidationResult$Message)
meth public void addWarning(org.netbeans.modules.php.api.validation.ValidationResult$Message)
meth public void merge(org.netbeans.modules.php.api.validation.ValidationResult)
supr java.lang.Object
hfds errors,warnings

CLSS public final static org.netbeans.modules.php.api.validation.ValidationResult$Message
 outer org.netbeans.modules.php.api.validation.ValidationResult
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,java.lang.String)
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean isType(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Object getSource()
meth public java.lang.String getMessage()
meth public java.lang.String getType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
supr java.lang.Object
hfds message,source,type

CLSS public abstract interface org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation
innr public final static FileInfo
meth public abstract java.util.Collection<org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation$FileInfo> getFiles()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public final static org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation$FileInfo
 outer org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation
cons public init(org.openide.filesystems.FileObject)
cons public init(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
fld public final static java.util.Comparator<org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation$FileInfo> COMPARATOR
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds description,displayName,file

CLSS public final org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport
innr public abstract interface static FileInfoCreator
meth public !varargs static org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport create(org.openide.filesystems.FileObject,java.lang.String[])
meth public java.util.Collection<org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation$FileInfo> getFiles(org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport$FileInfoCreator)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds changeSupport,directory,fileChangeListener,fileNames
hcls FilesListener

CLSS public abstract interface static org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport$FileInfoCreator
 outer org.netbeans.modules.php.spi.phpmodule.ImportantFilesSupport
meth public abstract org.netbeans.modules.php.spi.phpmodule.ImportantFilesImplementation$FileInfo create(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizer
meth public abstract boolean isValid()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract javax.swing.JComponent getComponent()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void close()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void save()

CLSS public abstract interface org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender
innr public abstract interface static Factory
innr public final static ExtendingException
meth public abstract boolean isValid()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getIdentifier()
meth public abstract java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<org.openide.filesystems.FileObject> extend(org.netbeans.modules.php.api.phpmodule.PhpModule) throws org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender$ExtendingException
meth public abstract javax.swing.JComponent getComponent()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.util.HelpCtx getHelp()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender$ExtendingException
 outer org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
cons public init(java.lang.String,java.lang.Throwable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String getFailureMessage()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface static org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender$Factory
 outer org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender
fld public final static java.lang.String EXTENDERS_PATH = "PHP/Extenders"
meth public abstract org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender create()

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

