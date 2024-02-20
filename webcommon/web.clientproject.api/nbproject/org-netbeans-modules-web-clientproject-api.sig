#Signature file v4.1
#Version 1.127

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.modules.gsf.codecoverage.api.CoverageProvider
meth public abstract boolean isAggregating()
meth public abstract boolean isEnabled()
meth public abstract boolean supportsAggregation()
meth public abstract boolean supportsHitCounts()
meth public abstract java.lang.String getTestAllAction()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.List<org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary> getResults()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails getDetails(org.openide.filesystems.FileObject,javax.swing.text.Document)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void clear()
meth public abstract void setAggregating(boolean)
meth public abstract void setEnabled(boolean)

CLSS public final org.netbeans.modules.web.clientproject.api.BadgeIcon
cons public init(java.awt.Image,java.net.URL)
meth public java.awt.Image getImage()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.net.URL getUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds image,url

CLSS public abstract org.netbeans.modules.web.clientproject.api.ClientProjectWizardProvider
cons public init()
meth public static org.openide.WizardDescriptor$InstantiatingIterator existingHtml5Project()
meth public static org.openide.WizardDescriptor$InstantiatingIterator newProjectWithExtender()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.clientproject.api.ClientSideModule
innr public abstract interface static Properties
meth public abstract org.netbeans.modules.web.clientproject.api.ClientSideModule$Properties getProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.web.clientproject.api.ClientSideModule$Properties
 outer org.netbeans.modules.web.clientproject.api.ClientSideModule
meth public abstract java.lang.String getWebContextRoot()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getStartFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.modules.web.clientproject.api.CustomizerPanel
meth public boolean isValid()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.swing.JComponent getComponent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void save()
supr java.lang.Object
hfds delegate

CLSS public abstract interface org.netbeans.modules.web.clientproject.api.ProjectDirectoriesProvider
meth public abstract org.openide.filesystems.FileObject getTestDirectory(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.filesystems.FileObject getTestSeleniumDirectory(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.modules.web.clientproject.api.WebClientProjectConstants
fld public final static java.lang.String CUSTOMIZER_RUN_IDENT = "RUN"
fld public final static java.lang.String CUSTOMIZER_SOURCES_IDENT = "SOURCES"
fld public final static java.lang.String SOURCES_TYPE_HTML5 = "HTML5-Sources"
fld public final static java.lang.String SOURCES_TYPE_HTML5_SITE_ROOT = "HTML5-SiteRoot"
fld public final static java.lang.String SOURCES_TYPE_HTML5_TEST = "HTML5-Tests"
fld public final static java.lang.String SOURCES_TYPE_HTML5_TEST_SELENIUM = "HTML5-Tests-Selenium"
supr java.lang.Object

CLSS public final org.netbeans.modules.web.clientproject.api.build.BuildTools
innr public abstract interface static BuildToolSupport
innr public abstract interface static CustomizerSupport
innr public abstract interface static NavigatorPanelSupport
innr public abstract interface static TasksMenuSupport
meth public boolean hasBuildTools(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public boolean run(org.netbeans.api.project.Project,java.lang.String,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public javax.swing.JComponent createCustomizerComponent(org.netbeans.modules.web.clientproject.api.build.BuildTools$CustomizerSupport)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public javax.swing.JMenu createTasksMenu(org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.navigator.NavigatorPanel createNavigatorPanel(org.netbeans.modules.web.clientproject.api.build.BuildTools$NavigatorPanelSupport)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.clientproject.api.build.BuildTools getDefault()
supr java.lang.Object
hfds INSTANCE

CLSS public abstract interface static org.netbeans.modules.web.clientproject.api.build.BuildTools$BuildToolSupport
 outer org.netbeans.modules.web.clientproject.api.build.BuildTools
meth public abstract !varargs void runTask(java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.lang.String getBuildToolExecName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.concurrent.Future<java.util.List<java.lang.String>> getTasks()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.project.Project getProject()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getWorkDir()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.web.clientproject.api.build.BuildTools$CustomizerSupport
 outer org.netbeans.modules.web.clientproject.api.build.BuildTools
meth public abstract java.lang.String getHeader()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getTask(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation getCustomizerPanel()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category getCategory()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setTask(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface static org.netbeans.modules.web.clientproject.api.build.BuildTools$NavigatorPanelSupport
 outer org.netbeans.modules.web.clientproject.api.build.BuildTools
meth public abstract java.lang.String getDisplayHint()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.clientproject.api.build.BuildTools$BuildToolSupport getBuildToolSupport(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport
 outer org.netbeans.modules.web.clientproject.api.build.BuildTools
innr public final static !enum Title
intf org.netbeans.modules.web.clientproject.api.build.BuildTools$BuildToolSupport
meth public abstract java.lang.String getDefaultTaskName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getTitle(org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void configure()
meth public abstract void reloadTasks()

CLSS public final static !enum org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title
 outer org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport
fld public final static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title CONFIGURE_TOOL
fld public final static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title LOADING_TASKS
fld public final static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title MANAGE_ADVANCED
fld public final static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title MENU
fld public final static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title TASKS_LABEL
meth public static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title valueOf(java.lang.String)
meth public static org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title[] values()
supr java.lang.Enum<org.netbeans.modules.web.clientproject.api.build.BuildTools$TasksMenuSupport$Title>

CLSS public final org.netbeans.modules.web.clientproject.api.json.JsonFile
cons public init(java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.web.clientproject.api.json.JsonFile$WatchedFields)
innr public final static WatchedFields
meth protected !varargs <%0 extends java.lang.Object> {%%0} getContentValue(java.util.Map<java.lang.String,java.lang.Object>,java.lang.Class<{%%0}>,java.lang.String[])
meth public !varargs <%0 extends java.lang.Object> {%%0} getContentValue(java.lang.Class<{%%0}>,java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public boolean exists()
meth public java.io.File getFile()
meth public java.lang.String getPath()
meth public java.util.Map<java.lang.String,java.lang.Object> getContent()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void cleanup()
meth public void refresh()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setContent(java.util.List<java.lang.String>,java.lang.Object) throws java.io.IOException
supr java.lang.Object
hfds CONTAINER_FACTORY,LOGGER,content,contentInited,propertyChangeSupport,watchedFields,watchedFile,watchedFileChangeListener
hcls WatchedFileChangeListener

CLSS public final static org.netbeans.modules.web.clientproject.api.json.JsonFile$WatchedFields
 outer org.netbeans.modules.web.clientproject.api.json.JsonFile
meth public !varargs org.netbeans.modules.web.clientproject.api.json.JsonFile$WatchedFields add(java.lang.String,java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.clientproject.api.json.JsonFile$WatchedFields all()
meth public static org.netbeans.modules.web.clientproject.api.json.JsonFile$WatchedFields create()
supr java.lang.Object
hfds ALL,data,frozen

CLSS public final org.netbeans.modules.web.clientproject.api.jstesting.Coverage
fld public final static java.lang.String PROP_ENABLED = "ENABLED"
innr public final static File
innr public final static FileMetrics
innr public final static Line
meth public boolean isEnabled()
meth public static org.netbeans.modules.web.clientproject.api.jstesting.Coverage forProject(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setFiles(java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.Coverage$File>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate

CLSS public final static org.netbeans.modules.web.clientproject.api.jstesting.Coverage$File
 outer org.netbeans.modules.web.clientproject.api.jstesting.Coverage
cons public init(java.lang.String,org.netbeans.modules.web.clientproject.api.jstesting.Coverage$FileMetrics,java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.Coverage$Line>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.Coverage$Line> getLines()
meth public org.netbeans.modules.web.clientproject.api.jstesting.Coverage$FileMetrics getMetrics()
supr java.lang.Object
hfds fileMetrics,lines,path

CLSS public final static org.netbeans.modules.web.clientproject.api.jstesting.Coverage$FileMetrics
 outer org.netbeans.modules.web.clientproject.api.jstesting.Coverage
cons public init(int,int,int)
meth public int getCoveredStatements()
meth public int getLineCount()
meth public int getStatements()
meth public java.lang.String toString()
supr java.lang.Object
hfds coveredStatements,lineCount,statements

CLSS public final static org.netbeans.modules.web.clientproject.api.jstesting.Coverage$Line
 outer org.netbeans.modules.web.clientproject.api.jstesting.Coverage
cons public init(int,int)
meth public int getHitCount()
meth public int getNumber()
meth public java.lang.String toString()
supr java.lang.Object
hfds hitCount,number

CLSS public final org.netbeans.modules.web.clientproject.api.jstesting.CoverageProviderImpl
cons public init(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
intf org.netbeans.modules.gsf.codecoverage.api.CoverageProvider
intf org.netbeans.modules.web.clientproject.spi.jstesting.CoverageImplementation
meth public boolean isAggregating()
meth public boolean isEnabled()
meth public boolean supportsAggregation()
meth public boolean supportsHitCounts()
meth public java.lang.String getTestAllAction()
meth public java.util.List<org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary> getResults()
meth public java.util.Set<java.lang.String> getMimeTypes()
meth public org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails getDetails(org.openide.filesystems.FileObject,javax.swing.text.Document)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void clear()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAggregating(boolean)
meth public void setEnabled(boolean)
meth public void setFiles(java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.Coverage$File>)
supr java.lang.Object
hfds LOGGER,MIME_TYPES,enabled,files,project,propertyChangeSupport

CLSS public final org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider
meth public boolean isCoverageSupported(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.net.URL toServer(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject fromServer(org.netbeans.api.project.Project,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void runTests(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders
fld public final static java.lang.String CUSTOMIZER_IDENT = "JS_TESTING"
fld public final static java.lang.String JS_TESTING_PATH = "JS/Testing"
meth public java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider> getJsTestingProviders()
meth public org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider findJsTestingProvider(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider getJsTestingProvider(org.netbeans.api.project.Project,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.ui.support.NodeFactory createJsTestingProvidersNodeFactory()
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizer()
meth public static org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders getDefault()
meth public void setJsTestingProvider(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INSTANCE,JS_TESTING_PROVIDERS,jsTestingProviders
hcls ProxyNodeList

CLSS public final org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo
innr public final static !enum SessionType
innr public final static Builder
meth public java.lang.String getTestFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType getSessionType()
supr java.lang.Object
hfds sessionType,testFile

CLSS public final static org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$Builder
 outer org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo
cons public init()
meth public org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo build()
meth public org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$Builder setSessionType(org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$Builder setTestFile(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds sessionType,testFile

CLSS public final static !enum org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType
 outer org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo
fld public final static org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType DEBUG
fld public final static org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType TEST
meth public static org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType[] values()
supr java.lang.Enum<org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo$SessionType>

CLSS public org.netbeans.modules.web.clientproject.api.network.NetworkException
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.util.List<java.lang.String>,java.lang.Throwable)
meth public java.util.List<java.lang.String> getFailedRequests()
supr java.io.IOException
hfds failedRequests,serialVersionUID

CLSS public final org.netbeans.modules.web.clientproject.api.network.NetworkSupport
meth public static boolean showNetworkErrorDialog(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public static boolean showNetworkErrorDialog(java.util.List<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public static void download(java.lang.String,java.io.File) throws java.io.IOException,java.lang.InterruptedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void downloadWithProgress(java.lang.String,java.io.File,java.lang.String) throws java.io.IOException,java.lang.InterruptedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static void downloadWithProgress(java.lang.String,java.io.File,org.netbeans.api.progress.ProgressHandle) throws java.io.IOException,java.lang.InterruptedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER

CLSS public final org.netbeans.modules.web.clientproject.api.platform.PlatformProvider
fld public final static java.lang.String PROP_ENABLED = "ENABLED"
fld public final static java.lang.String PROP_PROJECT_NAME = "PROJECT_NAME"
fld public final static java.lang.String PROP_RUN_CONFIGURATION = "RUN_CONFIGURATION"
fld public final static java.lang.String PROP_SOURCE_ROOTS = "SOURCE_ROOTS"
meth public boolean isEnabled(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<java.net.URL> getSourceRoots(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<org.netbeans.modules.web.clientproject.api.CustomizerPanel> getRunCustomizerPanels(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.web.clientproject.api.BadgeIcon getBadgeIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.spi.project.ActionProvider getActionProvider(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.web.clientproject.api.platform.PlatformProviders
fld public final static java.lang.String PLATFORM_PATH = "HTML5/Platform"
meth public java.util.List<org.netbeans.modules.web.clientproject.api.platform.PlatformProvider> getPlatformProviders()
meth public org.netbeans.modules.web.clientproject.api.platform.PlatformProvider findPlatformProvider(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.web.clientproject.api.platform.PlatformProviders getDefault()
meth public void addPlatformProvidersListener(org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void notifyPropertyChanged(org.netbeans.api.project.Project,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void notifyPropertyChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.platform.PlatformProvider,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePlatformProvidersListener(org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setPlatformProvider(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.platform.PlatformProvider)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INSTANCE,PLATFORM_PROVIDERS,delegatingPlatformProvidersListener,listenersSupport,platformProviders
hcls DelegatingPlatformProviderListener

CLSS public abstract interface org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener
innr public final static Support
intf java.util.EventListener
meth public abstract void platformProvidersChanged()
meth public abstract void propertyChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.platform.PlatformProvider,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener$Support
 outer org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener
cons public init()
meth public boolean hasListeners()
meth public void addPlatformProvidersListener(org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void firePlatformProvidersChanged()
meth public void firePropertyChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.platform.PlatformProvider,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void removePlatformProvidersListener(org.netbeans.modules.web.clientproject.api.platform.PlatformProvidersListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds listeners

CLSS public final org.netbeans.modules.web.clientproject.api.remotefiles.RemoteFilesNodeFactory
meth public static org.netbeans.spi.project.ui.support.NodeFactory createRemoteFilesNodeFactory()
supr java.lang.Object

CLSS public final org.netbeans.modules.web.clientproject.api.sites.SiteHelper
meth public !varargs static void unzipProjectTemplate(org.openide.filesystems.FileObject,java.io.File,org.netbeans.api.progress.ProgressHandle,java.lang.String[]) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.io.File getJsLibsDirectory()
meth public static java.util.List<java.lang.String> stripRootFolder(java.util.List<java.lang.String>)
meth public static void download(java.lang.String,java.io.File,org.netbeans.api.progress.ProgressHandle) throws java.io.IOException
 anno 0 java.lang.Deprecated()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds JS_LIBS_DIR,LOGGER

CLSS public final org.netbeans.modules.web.clientproject.api.util.StringUtilities
meth public static boolean hasText(java.lang.String)
meth public static boolean isEmpty(java.lang.String)
meth public static java.lang.String implode(java.util.List<java.lang.String>,java.lang.String)
meth public static java.util.List<java.lang.String> explode(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.web.clientproject.api.util.ValidationUtilities
meth public static boolean isValidFilename(java.io.File)
meth public static boolean isValidFilename(java.lang.String)
supr java.lang.Object
hfds INVALID_FILENAME_CHARS

CLSS public final org.netbeans.modules.web.clientproject.api.util.WatchedFile
meth public boolean exists()
meth public java.io.File getFile()
meth public java.lang.String toString()
meth public static org.netbeans.modules.web.clientproject.api.util.WatchedFile create(java.lang.String,org.openide.filesystems.FileObject)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LOGGER,changeSupport,directory,directoryListener,file,fileListener,filename
hcls DirectoryListener,FileListener

CLSS public final org.netbeans.modules.web.clientproject.api.validation.FolderValidator
cons public init()
fld public final static java.lang.String FOLDER = "folder"
meth public org.netbeans.modules.web.clientproject.api.validation.FolderValidator validateFolder(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.api.validation.FolderValidator validateFolder(java.io.File,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.common.api.ValidationResult getResult()
supr java.lang.Object
hfds result

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.ClientProjectExtender
meth public abstract org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[] createInitPanels()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[] createWizardPanels()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void apply(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String)
meth public abstract void initialize(org.openide.WizardDescriptor)

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.ClientProjectWizardProviderImpl
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator existingHtml5Project()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator newClientProjectWithExtender()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation
meth public abstract boolean isValid()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract javax.swing.JComponent getComponent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void save()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation
innr public final static ProjectProperties
meth public abstract boolean isPrepared()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void apply(org.openide.filesystems.FileObject,org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties,org.netbeans.api.progress.ProgressHandle) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void cleanup() throws java.io.IOException
meth public abstract void configure(org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void prepare() throws java.io.IOException

CLSS public final static org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties
 outer org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation
cons public init()
meth public java.lang.String getJsTestingProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSeleniumTestingProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSiteRootFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSourceFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getTestFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getTestSeleniumFolder()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setJsTestingProvider(java.lang.String)
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setSeleniumTestingProvider(java.lang.String)
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setSiteRootFolder(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setSourceFolder(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setTestFolder(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation$ProjectProperties setTestSeleniumFolder(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds jsTestingProvider,seleniumTestingProvider,siteRootFolder,sourceFolder,testFolder,testSeleniumFolder

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.build.BuildToolImplementation
meth public abstract boolean isEnabled()
meth public abstract boolean run(java.lang.String,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation
meth public abstract boolean isValid()
meth public abstract java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getWarningMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract javax.swing.JComponent getComponent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void save()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.jstesting.CoverageImplementation
fld public final static java.lang.String PROP_ENABLED = "ENABLED"
meth public abstract boolean isEnabled()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setFiles(java.util.List<org.netbeans.modules.web.clientproject.api.jstesting.Coverage$File>)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.jstesting.JsTestingProviderImplementation
meth public abstract boolean isCoverageSupported(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean isEnabled(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.net.URL toServer(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation createCustomizerPanel(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.spi.project.ui.support.NodeList<org.openide.nodes.Node> createNodeList(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject fromServer(org.netbeans.api.project.Project,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void notifyEnabled(org.netbeans.api.project.Project,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runTests(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.api.jstesting.TestRunInfo)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation
meth public abstract boolean isAutoRefresh()
meth public abstract boolean isHighlightSelectionEnabled()
meth public abstract org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer getProjectConfigurationCustomizer()
meth public abstract org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener getRefreshOnSaveListener()
meth public abstract org.netbeans.spi.project.ActionProvider getActionProvider()
meth public abstract void close()
meth public abstract void deactivate()
meth public abstract void save()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserProvider
meth public abstract org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation getEnhancedBrowser(org.netbeans.modules.web.browser.api.WebBrowser)

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation
fld public final static java.lang.String PROP_ENABLED = "ENABLED"
fld public final static java.lang.String PROP_PROJECT_NAME = "PROJECT_NAME"
fld public final static java.lang.String PROP_RUN_CONFIGURATION = "RUN_CONFIGURATION"
fld public final static java.lang.String PROP_SOURCE_ROOTS = "SOURCE_ROOTS"
meth public abstract boolean isEnabled(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<java.net.URL> getSourceRoots(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation> getRunCustomizerPanels(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.clientproject.api.BadgeIcon getBadgeIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.spi.project.ActionProvider getActionProvider(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addPlatformProviderImplementationListener(org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void notifyPropertyChanged(org.netbeans.api.project.Project,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePlatformProviderImplementationListener(org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener
innr public final static Support
intf java.util.EventListener
meth public abstract void propertyChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener$Support
 outer org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener
cons public init()
meth public boolean hasListeners()
meth public void addPlatformProviderImplementationsListener(org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void firePropertyChanged(org.netbeans.api.project.Project,org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementation,java.beans.PropertyChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void removePlatformProviderImplementationsListener(org.netbeans.modules.web.clientproject.spi.platform.PlatformProviderImplementationListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer
innr public final static !enum HiddenProperties
meth public abstract java.util.EnumSet<org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties> getHiddenProperties()
meth public abstract javax.swing.JPanel createPanel()

CLSS public final static !enum org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties
 outer org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer
fld public final static org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties WEB_SERVER
meth public static org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties valueOf(java.lang.String)
meth public static org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties[] values()
supr java.lang.Enum<org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer$HiddenProperties>

CLSS public abstract interface org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener
meth public abstract void fileChanged(org.openide.filesystems.FileObject)
meth public abstract void fileDeleted(org.openide.filesystems.FileObject)

