#Signature file v4.1
#Version 2.36

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

CLSS public final org.netbeans.modules.hudson.api.ConnectionBuilder
cons public init()
meth public java.net.HttpURLConnection httpConnection() throws java.io.IOException
meth public java.net.URLConnection connection() throws java.io.IOException
meth public org.netbeans.modules.hudson.api.ConnectionBuilder authentication(boolean)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder followRedirects(boolean)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder header(java.lang.String,java.lang.String)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder homeURL(java.lang.String) throws java.net.MalformedURLException
meth public org.netbeans.modules.hudson.api.ConnectionBuilder homeURL(java.net.URL)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder instance(org.netbeans.modules.hudson.api.HudsonInstance)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder job(org.netbeans.modules.hudson.api.HudsonJob)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder postData(byte[])
meth public org.netbeans.modules.hudson.api.ConnectionBuilder timeout(int)
meth public org.netbeans.modules.hudson.api.ConnectionBuilder url(java.lang.String) throws java.net.MalformedURLException
meth public org.netbeans.modules.hudson.api.ConnectionBuilder url(java.net.URL)
meth public org.w3c.dom.Document parseXML() throws java.io.IOException
meth public static void clearRejectedAuthentication()
supr java.lang.Object
hfds COOKIES,LOG,TIMER,auth,authenticationRejected,crumbs,followRedirects,home,postData,requestHeaders,timeout,url

CLSS public org.netbeans.modules.hudson.api.HudsonChangeAdapter
cons public init()
intf org.netbeans.modules.hudson.api.HudsonChangeListener
meth public void contentChanged()
meth public void stateChanged()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonChangeListener
intf java.util.EventListener
meth public abstract void contentChanged()
meth public abstract void stateChanged()

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonFolder
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonFolder> getFolders()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonJob> getJobs()
meth public abstract org.netbeans.modules.hudson.api.HudsonInstance getInstance()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonInstance
fld public final static java.lang.String ACTION_PATH = "org-netbeans-modules-hudson/Actions/instance"
innr public final static Persistence
intf java.lang.Comparable<org.netbeans.modules.hudson.api.HudsonInstance>
meth public abstract boolean isConnected()
meth public abstract boolean isForbidden()
meth public abstract boolean isPersisted()
meth public abstract int getSyncInterval()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonFolder> getFolders()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonJob> getJobs()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonView> getViews()
meth public abstract java.util.List<java.lang.String> getPreferredJobs()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.util.prefs.Preferences prefs()
meth public abstract org.netbeans.modules.hudson.api.HudsonInstance$Persistence getPersistence()
meth public abstract org.netbeans.modules.hudson.api.HudsonVersion getVersion()
meth public abstract org.netbeans.modules.hudson.api.HudsonView getPrimaryView()
meth public abstract void addHudsonChangeListener(org.netbeans.modules.hudson.api.HudsonChangeListener)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeHudsonChangeListener(org.netbeans.modules.hudson.api.HudsonChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setPreferredJobs(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setSyncInterval(int)
meth public abstract void synchronize(boolean)

CLSS public final static org.netbeans.modules.hudson.api.HudsonInstance$Persistence
 outer org.netbeans.modules.hudson.api.HudsonInstance
cons public init(boolean,java.lang.String,javax.swing.Action)
meth public boolean isPersistent()
meth public java.lang.String getInfo(java.lang.String)
meth public javax.swing.Action getNewJobAction()
meth public static org.netbeans.modules.hudson.api.HudsonInstance$Persistence instance(boolean)
meth public static org.netbeans.modules.hudson.api.HudsonInstance$Persistence persistent()
meth public static org.netbeans.modules.hudson.api.HudsonInstance$Persistence tranzient()
meth public static org.netbeans.modules.hudson.api.HudsonInstance$Persistence tranzient(java.lang.String)
meth public static org.netbeans.modules.hudson.api.HudsonInstance$Persistence tranzient(java.lang.String,javax.swing.Action)
supr java.lang.Object
hfds PERSISTENT_INSTANCE,TRANSIENT_INSTANCE,info,isPersistent,newJobAction

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonJob
innr public static !enum Color
intf java.lang.Comparable<org.netbeans.modules.hudson.api.HudsonJob>
meth public abstract boolean isBuildable()
meth public abstract boolean isInQueue()
meth public abstract boolean isSalient()
meth public abstract int getLastBuild()
meth public abstract int getLastCompletedBuild()
meth public abstract int getLastFailedBuild()
meth public abstract int getLastStableBuild()
meth public abstract int getLastSuccessfulBuild()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Collection<? extends org.netbeans.modules.hudson.api.HudsonJobBuild> getBuilds()
meth public abstract java.util.Collection<org.netbeans.modules.hudson.api.HudsonView> getViews()
meth public abstract org.netbeans.modules.hudson.api.HudsonInstance getInstance()
meth public abstract org.netbeans.modules.hudson.api.HudsonJob$Color getColor()
meth public abstract org.openide.filesystems.FileSystem getRemoteWorkspace()
meth public abstract void setSalient(boolean)
meth public abstract void start()

CLSS public static !enum org.netbeans.modules.hudson.api.HudsonJob$Color
 outer org.netbeans.modules.hudson.api.HudsonJob
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color aborted
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color aborted_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color blue
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color blue_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color disabled
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color disabled_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color grey
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color grey_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color notbuilt
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color notbuilt_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color red
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color red_anime
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color secured
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color yellow
fld public final static org.netbeans.modules.hudson.api.HudsonJob$Color yellow_anime
meth public boolean isRunning()
meth public java.lang.String colorizeDisplayName(java.lang.String)
meth public java.lang.String iconBase()
meth public static org.netbeans.modules.hudson.api.HudsonJob$Color find(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.hudson.api.HudsonJob$Color valueOf(java.lang.String)
meth public static org.netbeans.modules.hudson.api.HudsonJob$Color[] values()
supr java.lang.Enum<org.netbeans.modules.hudson.api.HudsonJob$Color>
hfds iconBaseName

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonJobBuild
innr public final static !enum Result
meth public abstract boolean canShowConsole()
meth public abstract boolean canShowFailures()
meth public abstract boolean isBuilding()
meth public abstract int getNumber()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Collection<? extends org.netbeans.modules.hudson.api.HudsonMavenModuleBuild> getMavenModules()
meth public abstract java.util.Collection<? extends org.netbeans.modules.hudson.spi.HudsonJobChangeItem> getChanges()
meth public abstract org.netbeans.modules.hudson.api.HudsonJob getJob()
meth public abstract org.netbeans.modules.hudson.api.HudsonJobBuild$Result getResult()
meth public abstract org.openide.filesystems.FileSystem getArtifacts()
meth public abstract void showConsole(org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl)
meth public abstract void showFailures(org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl)

CLSS public final static !enum org.netbeans.modules.hudson.api.HudsonJobBuild$Result
 outer org.netbeans.modules.hudson.api.HudsonJobBuild
fld public final static org.netbeans.modules.hudson.api.HudsonJobBuild$Result ABORTED
fld public final static org.netbeans.modules.hudson.api.HudsonJobBuild$Result FAILURE
fld public final static org.netbeans.modules.hudson.api.HudsonJobBuild$Result NOT_BUILT
fld public final static org.netbeans.modules.hudson.api.HudsonJobBuild$Result SUCCESS
fld public final static org.netbeans.modules.hudson.api.HudsonJobBuild$Result UNSTABLE
meth public static org.netbeans.modules.hudson.api.HudsonJobBuild$Result valueOf(java.lang.String)
meth public static org.netbeans.modules.hudson.api.HudsonJobBuild$Result[] values()
supr java.lang.Enum<org.netbeans.modules.hudson.api.HudsonJobBuild$Result>

CLSS public org.netbeans.modules.hudson.api.HudsonManager
meth public static java.lang.String simplifyServerLocation(java.lang.String,boolean)
meth public static java.util.Collection<? extends org.netbeans.modules.hudson.api.HudsonInstance> getAllInstances()
meth public static org.netbeans.modules.hudson.api.HudsonInstance addInstance(java.lang.String,java.lang.String,int,boolean)
meth public static org.netbeans.modules.hudson.api.HudsonInstance addInstance(java.lang.String,java.lang.String,int,org.netbeans.modules.hudson.api.HudsonInstance$Persistence)
meth public static org.netbeans.modules.hudson.api.HudsonInstance addInstance(java.lang.String,java.lang.String,int,org.netbeans.modules.hudson.spi.BuilderConnector)
meth public static org.netbeans.modules.hudson.api.HudsonInstance getInstance(java.lang.String)
meth public static org.netbeans.modules.hudson.api.HudsonInstance getInstanceByName(java.lang.String)
meth public static void addHudsonChangeListener(org.netbeans.modules.hudson.api.HudsonChangeListener)
meth public static void removeHudsonChangeListener(org.netbeans.modules.hudson.api.HudsonChangeListener)
meth public static void removeInstance(org.netbeans.modules.hudson.api.HudsonInstance)
meth public static void synchronizeInstance(org.netbeans.modules.hudson.api.HudsonInstance)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonMavenModuleBuild
meth public abstract boolean canShowConsole()
meth public abstract boolean canShowFailures()
meth public abstract java.lang.String getBuildDisplayName()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract org.netbeans.modules.hudson.api.HudsonJob$Color getColor()
meth public abstract org.netbeans.modules.hudson.api.HudsonJobBuild getBuild()
meth public abstract org.openide.filesystems.FileSystem getArtifacts()
meth public abstract void showConsole(org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl)
meth public abstract void showFailures(org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl)

CLSS public final org.netbeans.modules.hudson.api.HudsonVersion
cons public init(java.lang.String)
fld public final static org.netbeans.modules.hudson.api.HudsonVersion SUPPORTED_VERSION
intf java.lang.Comparable<org.netbeans.modules.hudson.api.HudsonVersion>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.netbeans.modules.hudson.api.HudsonVersion)
meth public int getMajorVersion()
meth public int getMinorVersion()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds VERSION_PATTERN,major,minor

CLSS public abstract interface org.netbeans.modules.hudson.api.HudsonView
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUrl()
meth public abstract org.netbeans.modules.hudson.api.HudsonInstance getInstance()

CLSS public org.netbeans.modules.hudson.api.Utilities
innr public final static !enum HudsonURLCheckResult
meth public static boolean isHudsonSupportActive()
meth public static boolean isSupportedVersion(org.netbeans.modules.hudson.api.HudsonVersion)
meth public static java.lang.String uriDecode(java.lang.String)
meth public static java.lang.String uriEncode(java.lang.String)
meth public static java.lang.String xpath(java.lang.String,org.w3c.dom.Element)
meth public static javax.swing.Icon getIcon(org.netbeans.modules.hudson.api.HudsonJob)
meth public static javax.swing.Icon getIcon(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public static org.netbeans.modules.hudson.api.HudsonJob$Color getColorForBuild(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult checkHudsonURL(java.lang.String)
meth public static void persistInstance(org.netbeans.modules.hudson.api.HudsonInstance)
meth public static void showInUI(org.netbeans.modules.hudson.api.HudsonJob)
meth public static void showInUI(org.netbeans.modules.hudson.api.HudsonJobBuild)
supr java.lang.Object
hfds xpath

CLSS public final static !enum org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult
 outer org.netbeans.modules.hudson.api.Utilities
fld public final static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult INCORRECT_REDIRECTS
fld public final static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult OK
fld public final static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult OTHER_ERROR
fld public final static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult WRONG_VERSION
meth public boolean isOK()
meth public static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult valueOf(java.lang.String)
meth public static org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult[] values()
supr java.lang.Enum<org.netbeans.modules.hudson.api.Utilities$HudsonURLCheckResult>

CLSS public abstract interface org.netbeans.modules.hudson.api.ui.ConsoleDataDisplayer
meth public abstract boolean writeLine(java.lang.String)
meth public abstract void close()
meth public abstract void open()

CLSS public abstract interface org.netbeans.modules.hudson.api.ui.FailureDataDisplayer
innr public final static Case
innr public final static Suite
meth public abstract void close()
meth public abstract void open()
meth public abstract void showSuite(org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Suite)

CLSS public final static org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Case
 outer org.netbeans.modules.hudson.api.ui.FailureDataDisplayer
cons public init()
meth public java.lang.String getClassName()
meth public java.lang.String getErrorStackTrace()
meth public java.lang.String getName()
meth public long getDuration()
meth public void setClassName(java.lang.String)
meth public void setDuration(long)
meth public void setErrorStackTrace(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds className,duration,errorStackTrace,name

CLSS public final static org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Suite
 outer org.netbeans.modules.hudson.api.ui.FailureDataDisplayer
cons public init()
meth public java.lang.String getName()
meth public java.lang.String getStderr()
meth public java.lang.String getStdout()
meth public java.util.List<org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Case> getCases()
meth public long getDuration()
meth public void addCase(org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Case)
meth public void setDuration(long)
meth public void setName(java.lang.String)
meth public void setStderr(java.lang.String)
meth public void setStdout(java.lang.String)
supr java.lang.Object
hfds cases,duration,name,stderr,stdout

CLSS public abstract interface org.netbeans.modules.hudson.api.ui.OpenableInBrowser
meth public abstract java.lang.String getUrl()

CLSS public org.netbeans.modules.hudson.constants.HudsonInstanceConstants
fld public final static java.lang.String FALSE = "false"
fld public final static java.lang.String INSTANCE_NAME = "name"
fld public final static java.lang.String INSTANCE_PERSISTED = "persisted"
fld public final static java.lang.String INSTANCE_PREF_JOBS = "pref_jobs"
fld public final static java.lang.String INSTANCE_SUPPRESSED_JOBS = "suppressed_jobs"
fld public final static java.lang.String INSTANCE_SYNC = "sync_time"
fld public final static java.lang.String INSTANCE_URL = "url"
fld public final static java.lang.String TRUE = "true"
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.constants.HudsonJobChangeFileConstants
fld public final static java.lang.String JOB_CHANGE_FILE_EDIT_TYPE = "job_change_edit_type"
fld public final static java.lang.String JOB_CHANGE_FILE_NAME = "job_change_file_name"
fld public final static java.lang.String JOB_CHANGE_FILE_PREVIOUS_REVISION = "job_change_previous_revision"
fld public final static java.lang.String JOB_CHANGE_FILE_REVISION = "job_change_revision"
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.constants.HudsonJobChangeItemConstants
fld public final static java.lang.String JOB_CHANGE_ITEM_MESSAGE = "job_change_item_message"
fld public final static java.lang.String JOB_CHANGE_ITEM_USER = "job_change_item_user"
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.constants.HudsonJobConstants
fld public final static java.lang.String JOB_BUILDABLE = "job_buildable"
fld public final static java.lang.String JOB_COLOR = "job_color"
fld public final static java.lang.String JOB_DISPLAY_NAME = "job_display_name"
fld public final static java.lang.String JOB_IN_QUEUE = "job_in_queue"
fld public final static java.lang.String JOB_LAST_BUILD = "job_last_build"
fld public final static java.lang.String JOB_LAST_COMPLETED_BUILD = "job_last_completed_build"
fld public final static java.lang.String JOB_LAST_FAILED_BUILD = "job_last_failed_build"
fld public final static java.lang.String JOB_LAST_STABLE_BUILD = "job_last_stable_build"
fld public final static java.lang.String JOB_LAST_SUCCESSFUL_BUILD = "job_last_successful_build"
fld public final static java.lang.String JOB_NAME = "job_name"
fld public final static java.lang.String JOB_URL = "job_url"
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.constants.HudsonViewConstants
fld public final static java.lang.String VIEW_NAME = "view_name"
fld public final static java.lang.String VIEW_URL = "view_url"
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.constants.HudsonXmlApiConstants
fld public final static java.lang.String XML_API_BUILDABLE_ELEMENT = "buildable"
fld public final static java.lang.String XML_API_COLOR_ELEMENT = "color"
fld public final static java.lang.String XML_API_DISPLAY_NAME_ELEMENT = "displayName"
fld public final static java.lang.String XML_API_INQUEUE_ELEMENT = "inQueue"
fld public final static java.lang.String XML_API_JOB_ELEMENT = "job"
fld public final static java.lang.String XML_API_LAST_BUILD_ELEMENT = "lastBuild"
fld public final static java.lang.String XML_API_LAST_COMPLETED_BUILD_ELEMENT = "lastCompletedBuild"
fld public final static java.lang.String XML_API_LAST_FAILED_BUILD_ELEMENT = "lastFailedBuild"
fld public final static java.lang.String XML_API_LAST_STABLE_BUILD_ELEMENT = "lastStableBuild"
fld public final static java.lang.String XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT = "lastSuccessfulBuild"
fld public final static java.lang.String XML_API_NAME_ELEMENT = "name"
fld public final static java.lang.String XML_API_SECURED_JOB_ELEMENT = "securedJob"
fld public final static java.lang.String XML_API_URL = "api/xml"
fld public final static java.lang.String XML_API_URL_ELEMENT = "url"
fld public final static java.lang.String XML_API_VIEW_ELEMENT = "view"
supr java.lang.Object

CLSS public abstract org.netbeans.modules.hudson.spi.BuilderConnector
cons public init()
innr public abstract static ConsoleDataProvider
innr public abstract static FailureDataProvider
innr public final static BuildData
innr public final static FolderData
innr public final static InstanceData
innr public final static JobData
innr public final static ModuleData
innr public final static ViewData
meth public abstract boolean isConnected()
meth public abstract boolean isForbidden()
meth public abstract java.util.Collection<? extends org.netbeans.modules.hudson.spi.HudsonJobChangeItem> getJobBuildChanges(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public abstract java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$BuildData> getJobBuildsData(org.netbeans.modules.hudson.api.HudsonJob)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.hudson.api.HudsonVersion getHudsonVersion(boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.hudson.spi.BuilderConnector$ConsoleDataProvider getConsoleDataProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.hudson.spi.BuilderConnector$FailureDataProvider getFailureDataProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.hudson.spi.BuilderConnector$InstanceData getInstanceData(boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.hudson.spi.RemoteFileSystem getArtifacts(org.netbeans.modules.hudson.api.HudsonJobBuild)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.hudson.spi.RemoteFileSystem getArtifacts(org.netbeans.modules.hudson.api.HudsonMavenModuleBuild)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.hudson.spi.RemoteFileSystem getWorkspace(org.netbeans.modules.hudson.api.HudsonJob)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void getJobBuildResult(org.netbeans.modules.hudson.api.HudsonJobBuild,java.util.concurrent.atomic.AtomicBoolean,java.util.concurrent.atomic.AtomicReference<org.netbeans.modules.hudson.api.HudsonJobBuild$Result>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void startJob(org.netbeans.modules.hudson.api.HudsonJob)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.hudson.spi.BuilderConnector$InstanceData getInstanceData(org.netbeans.modules.hudson.api.HudsonFolder,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$BuildData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init(int,org.netbeans.modules.hudson.api.HudsonJobBuild$Result,boolean)
meth public boolean isBuilding()
meth public int getNumber()
meth public org.netbeans.modules.hudson.api.HudsonJobBuild$Result getResult()
supr java.lang.Object
hfds building,number,result

CLSS public abstract static org.netbeans.modules.hudson.spi.BuilderConnector$ConsoleDataProvider
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init()
meth public abstract void showConsole(org.netbeans.modules.hudson.api.HudsonJobBuild,org.netbeans.modules.hudson.api.ui.ConsoleDataDisplayer)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void showConsole(org.netbeans.modules.hudson.api.HudsonMavenModuleBuild,org.netbeans.modules.hudson.api.ui.ConsoleDataDisplayer)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.hudson.spi.BuilderConnector$FailureDataProvider
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init()
meth public abstract void showFailures(org.netbeans.modules.hudson.api.HudsonJobBuild,org.netbeans.modules.hudson.api.ui.FailureDataDisplayer)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void showFailures(org.netbeans.modules.hudson.api.HudsonMavenModuleBuild,org.netbeans.modules.hudson.api.ui.FailureDataDisplayer)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$FolderData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public void setName(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds name,url

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$InstanceData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init(java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$JobData>,java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$ViewData>)
 anno 0 java.lang.Deprecated()
cons public init(java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$JobData>,java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$ViewData>,java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$FolderData>)
meth public java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$FolderData> getFoldersData()
meth public java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$JobData> getJobsData()
meth public java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$ViewData> getViewsData()
supr java.lang.Object
hfds foldersData,jobsData,viewsData

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$JobData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init()
meth public boolean isBuildable()
meth public boolean isInQueue()
meth public boolean isSecured()
meth public int getLastBuild()
meth public int getLastCompletedBuild()
meth public int getLastFailedBuild()
meth public int getLastStableBuild()
meth public int getLastSuccessfulBuild()
meth public java.lang.String getDisplayName()
meth public java.lang.String getJobName()
meth public java.lang.String getJobUrl()
meth public java.util.Collection<java.lang.String> getViews()
meth public java.util.Collection<org.netbeans.modules.hudson.spi.BuilderConnector$ModuleData> getModules()
meth public org.netbeans.modules.hudson.api.HudsonJob$Color getColor()
meth public void addModule(java.lang.String,java.lang.String,org.netbeans.modules.hudson.api.HudsonJob$Color,java.lang.String)
meth public void addView(java.lang.String)
meth public void setBuildable(boolean)
meth public void setColor(org.netbeans.modules.hudson.api.HudsonJob$Color)
meth public void setDisplayName(java.lang.String)
meth public void setInQueue(boolean)
meth public void setJobName(java.lang.String)
meth public void setJobUrl(java.lang.String)
meth public void setLastBuild(int)
meth public void setLastCompletedBuild(int)
meth public void setLastFailedBuild(int)
meth public void setLastStableBuild(int)
meth public void setLastSuccessfulBuild(int)
meth public void setSecured(boolean)
supr java.lang.Object
hfds buildable,color,displayName,inQueue,jobName,jobUrl,lastBuild,lastCompletedBuild,lastFailedBuild,lastStableBuild,lastSuccessfulBuild,modules,secured,views

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$ModuleData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.hudson.api.HudsonJob$Color,java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public org.netbeans.modules.hudson.api.HudsonJob$Color getColor()
supr java.lang.Object
hfds color,displayName,name,url

CLSS public final static org.netbeans.modules.hudson.spi.BuilderConnector$ViewData
 outer org.netbeans.modules.hudson.spi.BuilderConnector
cons public init(java.lang.String,java.lang.String,boolean)
meth public boolean isPrimary()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
supr java.lang.Object
hfds name,primary,url

CLSS public abstract interface org.netbeans.modules.hudson.spi.ConnectionAuthenticator
meth public abstract java.net.URLConnection forbidden(java.net.URLConnection,java.net.URL)
meth public abstract void prepareRequest(java.net.URLConnection,java.net.URL)

CLSS public abstract org.netbeans.modules.hudson.spi.ConsoleDataDisplayerImpl
cons public init()
meth public abstract boolean writeLine(java.lang.String)
meth public abstract void close()
meth public abstract void open()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.hudson.spi.FailureDataDisplayerImpl
cons public init()
meth public abstract void close()
meth public abstract void open()
meth public abstract void showSuite(org.netbeans.modules.hudson.api.ui.FailureDataDisplayer$Suite)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.hudson.spi.HudsonJobChangeItem
innr public abstract interface static HudsonJobChangeFile
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getUser()
meth public abstract java.util.Collection<? extends org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile> getFiles()

CLSS public abstract interface static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile
 outer org.netbeans.modules.hudson.spi.HudsonJobChangeItem
innr public final static !enum EditType
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType getEditType()
meth public abstract org.openide.windows.OutputListener hyperlink()

CLSS public final static !enum org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType
 outer org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile
fld public final static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType add
fld public final static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType delete
fld public final static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType edit
meth public static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType valueOf(java.lang.String)
meth public static org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType[] values()
supr java.lang.Enum<org.netbeans.modules.hudson.spi.HudsonJobChangeItem$HudsonJobChangeFile$EditType>

CLSS public abstract interface org.netbeans.modules.hudson.spi.HudsonLogger
innr public abstract interface static HudsonLogSession
meth public abstract org.netbeans.modules.hudson.spi.HudsonLogger$HudsonLogSession createSession(org.netbeans.modules.hudson.api.HudsonJob)

CLSS public abstract interface static org.netbeans.modules.hudson.spi.HudsonLogger$HudsonLogSession
 outer org.netbeans.modules.hudson.spi.HudsonLogger
meth public abstract boolean handle(java.lang.String,org.openide.windows.OutputWriter)

CLSS public abstract org.netbeans.modules.hudson.spi.HudsonManagerAgent
cons public init()
meth public abstract void instanceAdded(org.netbeans.modules.hudson.api.HudsonInstance)
meth public abstract void instanceRemoved(org.netbeans.modules.hudson.api.HudsonInstance)
meth public abstract void start()
meth public abstract void terminate()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.hudson.spi.HudsonSCM
innr public abstract interface static Configuration
innr public final static ConfigurationStatus
meth public abstract java.lang.String translateWorkspacePath(org.netbeans.modules.hudson.api.HudsonJob,java.lang.String,java.io.File)
meth public abstract java.util.List<? extends org.netbeans.modules.hudson.spi.HudsonJobChangeItem> parseChangeSet(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public abstract org.netbeans.modules.hudson.spi.HudsonSCM$Configuration forFolder(java.io.File)

CLSS public abstract interface static org.netbeans.modules.hudson.spi.HudsonSCM$Configuration
 outer org.netbeans.modules.hudson.spi.HudsonSCM
meth public abstract org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus problems()
meth public abstract void configure(org.w3c.dom.Document)

CLSS public final static org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus
 outer org.netbeans.modules.hudson.spi.HudsonSCM
meth public java.lang.String getErrorMessage()
meth public java.lang.String getWarningMessage()
meth public javax.swing.JButton getExtraButton()
meth public org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus withExtraButton(javax.swing.JButton)
meth public static org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus valid()
meth public static org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus withError(java.lang.String)
meth public static org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus withWarning(java.lang.String)
supr java.lang.Object
hfds errorMessage,extraButton,warningMessage

CLSS public abstract interface org.netbeans.modules.hudson.spi.PasswordAuthorizer
meth public abstract java.lang.String[] authorize(java.net.URL)

CLSS public abstract org.netbeans.modules.hudson.spi.RemoteFileSystem
cons public init()
meth public abstract void refreshAll()
supr org.openide.filesystems.AbstractFileSystem

CLSS public abstract org.netbeans.modules.hudson.spi.UIExtension
cons public init()
meth public abstract void showInUI(org.netbeans.modules.hudson.api.HudsonJob)
meth public abstract void showInUI(org.netbeans.modules.hudson.api.HudsonJobBuild)
supr java.lang.Object

CLSS public abstract org.openide.filesystems.AbstractFileSystem
cons public init()
fld protected org.openide.filesystems.AbstractFileSystem$Attr attr
fld protected org.openide.filesystems.AbstractFileSystem$Change change
fld protected org.openide.filesystems.AbstractFileSystem$Info info
fld protected org.openide.filesystems.AbstractFileSystem$List list
fld protected org.openide.filesystems.AbstractFileSystem$Transfer transfer
innr public abstract interface static Attr
innr public abstract interface static Change
innr public abstract interface static Info
innr public abstract interface static List
innr public abstract interface static SymlinkInfo
innr public abstract interface static Transfer
meth protected <%0 extends org.openide.filesystems.FileObject> java.lang.ref.Reference<{%%0}> createReference({%%0})
meth protected boolean canRead(java.lang.String)
meth protected boolean canWrite(java.lang.String)
meth protected boolean checkVirtual(java.lang.String)
meth protected final int getRefreshTime()
meth protected final java.lang.ref.Reference<? extends org.openide.filesystems.FileObject> findReference(java.lang.String)
meth protected final java.util.Enumeration<? extends org.openide.filesystems.FileObject> existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject refreshRoot()
meth protected final void refreshResource(java.lang.String,boolean)
meth protected final void setRefreshTime(int)
meth protected void markImportant(java.lang.String,boolean)
meth public abstract java.lang.String getDisplayName()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
meth public org.openide.filesystems.FileObject getRoot()
meth public void refresh(boolean)
supr org.openide.filesystems.FileSystem
hfds lastEnum,refresher,root,serialVersionUID

CLSS public abstract org.openide.filesystems.FileSystem
cons public init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_HIDDEN = "hidden"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_READ_ONLY = "readOnly"
fld public final static java.lang.String PROP_ROOT = "root"
fld public final static java.lang.String PROP_SYSTEM_NAME = "systemName"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static AtomicAction
intf java.io.Serializable
meth protected final void fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void setSystemName(java.lang.String) throws java.beans.PropertyVetoException
 anno 0 java.lang.Deprecated()
meth public abstract boolean isReadOnly()
meth public abstract java.lang.String getDisplayName()
meth public abstract org.openide.filesystems.FileObject findResource(java.lang.String)
meth public abstract org.openide.filesystems.FileObject getRoot()
meth public final boolean isDefault()
meth public final boolean isValid()
meth public final java.lang.String getSystemName()
 anno 0 java.lang.Deprecated()
meth public final void addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject createTempFile(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.openide.filesystems.FileObject getTempFolder() throws java.io.IOException
meth public org.openide.filesystems.StatusDecorator getDecorator()
meth public org.openide.util.Lookup findExtrasFor(java.util.Set<org.openide.filesystems.FileObject>)
meth public void addNotify()
meth public void refresh(boolean)
meth public void removeNotify()
supr java.lang.Object
hfds LOG,SFS_STATUS,STATUS_NONE,assigned,changeSupport,defFS,fclSupport,fileStatusList,internLock,repository,serialVersionUID,statusResult,systemName,thrLocal,valid,vetoableChangeList
hcls AsyncAtomicAction,EventDispatcher,FileStatusDispatcher

