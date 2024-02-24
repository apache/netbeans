#Signature file v4.1
#Version 1.73

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

CLSS public abstract interface org.netbeans.lib.profiler.common.GlobalProfilingSettings
meth public abstract int getCalibrationPortNo()
meth public abstract int getPortNo()
meth public abstract java.lang.String getJavaPlatformForProfiling()
meth public abstract void setCalibrationPortNo(int)
meth public abstract void setJavaPlatformForProfiling(java.lang.String)
meth public abstract void setPortNo(int)

CLSS public final org.netbeans.modules.profiler.api.ActionsSupport
cons public init()
fld public final static javax.swing.KeyStroke NO_KEYSTROKE
meth public static java.lang.String keyAcceleratorString(javax.swing.KeyStroke)
meth public static javax.swing.KeyStroke registerAction(java.lang.String,javax.swing.Action,javax.swing.ActionMap,javax.swing.InputMap)
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.api.EditorContext
cons public init(javax.swing.text.JTextComponent,javax.swing.text.Document,org.openide.filesystems.FileObject)
meth public javax.swing.text.Document getDocument()
meth public javax.swing.text.JTextComponent getTextComponent()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds document,fileObject,textComponent

CLSS public final org.netbeans.modules.profiler.api.EditorSupport
cons public init()
meth public static boolean currentlyInJavaEditor()
meth public static boolean isCurrentOffsetValid()
meth public static boolean isOffsetValid(org.openide.filesystems.FileObject,int)
meth public static int getCurrentLine()
meth public static int getCurrentOffset()
meth public static int getLineForOffset(org.openide.filesystems.FileObject,int)
meth public static int getOffsetForLine(org.openide.filesystems.FileObject,int)
meth public static int[] getSelectionOffsets()
meth public static org.netbeans.modules.profiler.api.EditorContext getMostActiveJavaEditorContext()
meth public static org.openide.filesystems.FileObject getCurrentFile()
meth public static org.openide.util.Lookup$Provider getCurrentProject()
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.api.GestureSubmitter
cons public init()
meth public static void logAttachApp(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.AttachSettings)
meth public static void logAttachExternal(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.AttachSettings)
meth public static void logProfileApp(org.openide.util.Lookup$Provider,org.netbeans.lib.profiler.common.ProfilingSettings)
supr java.lang.Object
hfds USG_LOGGER

CLSS public final org.netbeans.modules.profiler.api.GoToSource
cons public init()
meth public static boolean isAvailable()
meth public static void openFile(org.openide.filesystems.FileObject,int)
meth public static void openSource(org.openide.util.Lookup$Provider,java.lang.String,java.lang.String,int)
meth public static void openSource(org.openide.util.Lookup$Provider,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds srcOpenerRP

CLSS public final org.netbeans.modules.profiler.api.JavaPlatform
meth public boolean equals(java.lang.Object)
meth public int getPlatformArchitecture()
meth public int getPlatformJDKMinor()
meth public int hashCode()
meth public java.lang.String getDisplayName()
meth public java.lang.String getPlatformId()
meth public java.lang.String getPlatformJDKVersion()
meth public java.lang.String getPlatformJavaFile()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public static java.util.List<org.netbeans.modules.profiler.api.JavaPlatform> getPlatforms()
meth public static org.netbeans.modules.profiler.api.JavaPlatform getDefaultPlatform()
meth public static org.netbeans.modules.profiler.api.JavaPlatform getJavaPlatformById(java.lang.String)
meth public static void showCustomizer()
supr java.lang.Object
hfds provider

CLSS public final org.netbeans.modules.profiler.api.ProfilerDialogs
cons public init()
meth public static boolean displayConfirmation(java.lang.String)
meth public static boolean displayConfirmation(java.lang.String,java.lang.String)
meth public static boolean displayConfirmationDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static java.lang.Boolean displayCancellableConfirmation(java.lang.String,java.lang.String)
meth public static java.lang.Boolean displayCancellableConfirmationDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static void displayError(java.lang.String)
meth public static void displayError(java.lang.String,java.lang.String,java.lang.String)
meth public static void displayInfo(java.lang.String)
meth public static void displayInfo(java.lang.String,java.lang.String,java.lang.String)
meth public static void displayInfoDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static void displayWarning(java.lang.String)
meth public static void displayWarning(java.lang.String,java.lang.String,java.lang.String)
meth public static void displayWarningDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
supr java.lang.Object

CLSS public final org.netbeans.modules.profiler.api.ProfilerIDESettings
fld public final static int CPU_ENTIRE_APP = 0
fld public final static int CPU_PART_APP = 1
fld public final static int CPU_PROFILING_POINTS = 3
fld public final static int CPU_STARTUP = 2
fld public final static int OOME_DETECTION_CUSTOMDIR = 3
fld public final static int OOME_DETECTION_NONE = 0
fld public final static int OOME_DETECTION_PROJECTDIR = 1
fld public final static int OOME_DETECTION_TEMPDIR = 2
fld public final static int OPEN_ALWAYS = 1
fld public final static int OPEN_MONITORING = 2
fld public final static int OPEN_NEVER = 3
fld public final static int SNAPSHOT_WINDOW_CLOSE_NEVER = 0
fld public final static int SNAPSHOT_WINDOW_CLOSE_PROFILER = 1
fld public final static int SNAPSHOT_WINDOW_HIDE_PROFILER = 2
fld public final static int SNAPSHOT_WINDOW_OPEN_EACH = 4
fld public final static int SNAPSHOT_WINDOW_OPEN_FIRST = 3
fld public final static int SNAPSHOT_WINDOW_OPEN_NEVER = 0
fld public final static int SNAPSHOT_WINDOW_OPEN_PROFILER = 1
fld public final static int SNAPSHOT_WINDOW_SHOW_PROFILER = 2
fld public final static java.lang.String DO_NOT_SHOW_ATTACH_SETTINGS = "dns-attach-settings"
fld public final static java.lang.String DO_NOT_SHOW_JDK_DIALOG = "dns-jdk-dialog"
fld public final static java.lang.String DO_NOT_SHOW_PID_WINDOWS = "dns-pid-windows4"
intf org.netbeans.lib.profiler.common.GlobalProfilingSettings
meth public boolean getAutoOpenSnapshot()
meth public boolean getAutoSaveSnapshot()
meth public boolean getDisplayLiveResultsCPU()
meth public boolean getDisplayLiveResultsFragment()
meth public boolean getDisplayLiveResultsMemory()
meth public boolean getEnableExpertSettings()
meth public boolean getHeapWalkerAnalysisEnabled()
meth public boolean getIncludeProfilingPointsDependencies()
meth public boolean getLogProfilerStatus()
meth public boolean getMemoryTaskAllocationsDefault()
meth public boolean getRecordStackTracesDefault()
meth public boolean getReopenHeapDumps()
meth public boolean getReopenSnapshots()
meth public boolean getShowNoDataHint()
meth public boolean getThreadsMonitoringDefault()
meth public boolean isOOMDetectionEnabled()
meth public boolean isSourcesColoringEnabled()
meth public int getCalibrationPortNo()
meth public int getCpuTaskDefault()
meth public int getLockContentionViewBehavior()
meth public int getOOMDetectionMode()
meth public int getPortNo()
meth public int getSnapshotWindowClosePolicy()
meth public int getSnapshotWindowOpenPolicy()
meth public int getTelemetryOverviewBehavior()
meth public int getThreadsViewBehavior()
meth public int getTrackEveryDefault()
meth public java.lang.String displayName()
meth public java.lang.String getCustomHeapdumpPath()
meth public java.lang.String getDoNotShowAgain(java.lang.String)
meth public java.lang.String getInstrFilterDefault()
meth public java.lang.String getJavaPlatformForProfiling()
meth public org.netbeans.lib.profiler.common.ProfilingSettings createDefaultProfilingSettings()
meth public org.netbeans.lib.profiler.common.ProfilingSettings getDefaultProfilingSettings()
meth public static org.netbeans.modules.profiler.api.ProfilerIDESettings getInstance()
meth public void clearDoNotShowAgainMap()
meth public void saveDefaultProfilingSettings()
meth public void setAutoOpenSnapshot(boolean)
meth public void setAutoSaveSnapshot(boolean)
meth public void setCalibrationPortNo(int)
meth public void setCpuTaskDefault(int)
meth public void setCustomHeapdumpPath(java.lang.String)
meth public void setDisplayLiveResultsCPU(boolean)
meth public void setDisplayLiveResultsFragment(boolean)
meth public void setDisplayLiveResultsMemory(boolean)
meth public void setDoNotShowAgain(java.lang.String,java.lang.String)
meth public void setEnableExpertSettings(boolean)
meth public void setHeapWalkerAnalysisEnabled(boolean)
meth public void setIncludeProfilingPointsDependencies(boolean)
meth public void setInstrFilterDefault(java.lang.String)
meth public void setJavaPlatformForProfiling(java.lang.String)
meth public void setLockContentionViewBehavior(int)
meth public void setLogProfilerStatus(boolean)
meth public void setMemoryTaskAllocationsDefault(boolean)
meth public void setOOMDetectionMode(int)
meth public void setPortNo(int)
meth public void setRecordStackTracesDefault(boolean)
meth public void setReopenHeapDumps(boolean)
meth public void setReopenSnapshots(boolean)
meth public void setShowNoDataHint(boolean)
meth public void setSnapshotWindowClosePolicy(int)
meth public void setSnapshotWindowOpenPolicy(int)
meth public void setSourcesColoringEnabled(boolean)
meth public void setTelemetryOverviewBehavior(int)
meth public void setThreadsMonitoringDefault(boolean)
meth public void setThreadsViewBehavior(int)
meth public void setTrackEveryDefault(int)
supr java.lang.Object
hfds AUTO_OPEN_SNAPSHOT_DEFAULT,AUTO_OPEN_SNAPSHOT_KEY,AUTO_OPEN_SNAPSHOT_KEY_55,AUTO_SAVE_SNAPSHOT_DEFAULT,AUTO_SAVE_SNAPSHOT_KEY,AUTO_SAVE_SNAPSHOT_KEY_55,CALIBRATION_PORT_NO_DEFAULT,CALIBRATION_PORT_NO_KEY,CALIBRATION_PORT_NO_KEY_55,CPU_TASK_DEFAULT,CPU_TASK_KEY,CPU_TASK_KEY_55,CUSTOM_HEAPDUMP_PATH_DEFAULT,CUSTOM_HEAPDUMP_PATH_KEY,DNSA_SETTINGS_KEY,ENABLE_EXPERT_SETTINGS_DEFAULT,ENABLE_EXPERT_SETTINGS_KEY,HEAPWALKER_ANALYSIS_ENABLED_DEFAULT,HEAPWALKER_ANALYSIS_ENABLED_KEY,INSTR_FILTER_DEFAULT,INSTR_FILTER_KEY,LCV_BEHAVIOR_DEFAULT,LCV_BEHAVIOR_KEY,LIVE_CPU_DEFAULT,LIVE_CPU_KEY,LIVE_CPU_KEY_55,LIVE_FRAGMENT_DEFAULT,LIVE_FRAGMENT_KEY,LIVE_FRAGMENT_KEY_55,LIVE_MEMORY_DEFAULT,LIVE_MEMORY_KEY,LIVE_MEMORY_KEY_55,LOG_PROFILER_STATUS_DEFAULT,LOG_PROFILER_STATUS_KEY,MEMORY_TASK_ALLOCATIONS_DEFAULT,MEMORY_TASK_ALLOCATIONS_KEY,MEMORY_TASK_ALLOCATIONS_KEY_55,NO_DATA_HINT_DEFAULT,NO_DATA_HINT_KEY,OOME_DETECTION_MODE_DEFAULT,OOME_DETECTION_MODE_KEY,PLATFORM_NAME_DEFAULT,PLATFORM_NAME_KEY,PLATFORM_NAME_KEY_55,PORT_NO_DEFAULT,PORT_NO_KEY,PORT_NO_KEY_55,PPOINTS_DEPENDENCIES_INCLUDE_DEFAULT,PPOINTS_DEPENDENCIES_INCLUDE_KEY,RECORD_STACK_TRACES_DEFAULT,RECORD_STACK_TRACES_KEY,RECORD_STACK_TRACES_KEY_55,REOPEN_HDUMPS_DEFAULT,REOPEN_HDUMPS_KEY,REOPEN_SNAPSHOTS_DEFAULT,REOPEN_SNAPSHOTS_KEY,SNAPSHOT_WINDOW_CLOSE_DEFAULT,SNAPSHOT_WINDOW_CLOSE_POLICY_KEY,SNAPSHOT_WINDOW_OPEN_DEFAULT,SNAPSHOT_WINDOW_OPEN_POLICY_KEY,SOURCES_COLORING_DEFAULT,SOURCES_COLORING_KEY,THREADS_MONITORING_DEFAULT,THREADS_MONITORING_KEY,THREADS_MONITORING_KEY_55,TO_BEHAVIOR_DEFAULT,TO_BEHAVIOR_KEY,TO_BEHAVIOR_KEY_55,TRACK_EVERY_DEFAULT,TRACK_EVERY_KEY,TRACK_EVERY_KEY_55,TV_BEHAVIOR_DEFAULT,TV_BEHAVIOR_KEY,TV_BEHAVIOR_KEY_55,defaultInstance,dnsaMap,pSettings,sourcesColoringEnabled

CLSS public abstract org.netbeans.modules.profiler.api.ProfilerProject
cons protected init(org.openide.util.Lookup$Provider)
intf org.openide.util.Lookup$Provider
meth protected abstract org.openide.util.Lookup additionalLookup()
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds lkp,lkpLock,provider

CLSS public abstract org.netbeans.modules.profiler.api.ProfilerSource
cons protected init(org.openide.filesystems.FileObject)
intf org.openide.util.Lookup$Provider
meth public abstract boolean isRunnable()
meth public final org.openide.filesystems.FileObject getFile()
meth public final org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds file

CLSS public final org.netbeans.modules.profiler.api.ProfilerStorage
cons public init()
meth public static org.openide.filesystems.FileObject getGlobalFolder(boolean) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getProjectFolder(org.openide.util.Lookup$Provider,boolean) throws java.io.IOException
meth public static org.openide.util.Lookup$Provider getProjectFromFolder(org.openide.filesystems.FileObject)
meth public static void deleteGlobalProperties(java.lang.String) throws java.io.IOException
meth public static void deleteProjectProperties(org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public static void loadGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public static void loadProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public static void saveGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public static void saveProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.profiler.api.ProgressDisplayer
fld public final static org.netbeans.modules.profiler.api.ProgressDisplayer DEFAULT
innr public abstract interface static ProgressController
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String)
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public abstract org.netbeans.modules.profiler.api.ProgressDisplayer showProgress(java.lang.String,org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController)
meth public abstract void close()

CLSS public abstract interface static org.netbeans.modules.profiler.api.ProgressDisplayer$ProgressController
 outer org.netbeans.modules.profiler.api.ProgressDisplayer
intf org.openide.util.Cancellable

CLSS public final org.netbeans.modules.profiler.api.ProjectUtilities
cons public init()
meth public static boolean hasSubprojects(org.openide.util.Lookup$Provider)
meth public static java.lang.String getDisplayName(org.openide.util.Lookup$Provider)
meth public static javax.swing.Icon getIcon(org.openide.util.Lookup$Provider)
meth public static org.openide.filesystems.FileObject getProjectDirectory(org.openide.util.Lookup$Provider)
meth public static org.openide.util.Lookup$Provider getMainProject()
meth public static org.openide.util.Lookup$Provider getProject(org.openide.filesystems.FileObject)
meth public static org.openide.util.Lookup$Provider[] getOpenedProjects()
meth public static org.openide.util.Lookup$Provider[] getSortedProjects(org.openide.util.Lookup$Provider[])
meth public static void addOpenProjectsListener(javax.swing.event.ChangeListener)
meth public static void fetchSubprojects(org.openide.util.Lookup$Provider,java.util.Set<org.openide.util.Lookup$Provider>)
meth public static void removeOpenProjectsListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.profiler.api.icons.GeneralIcons
fld public final static java.lang.String ADD = "GeneralIcons.Add"
fld public final static java.lang.String BACK = "GeneralIcons.Back"
fld public final static java.lang.String BADGE_ADD = "GeneralIcons.BadgeAdd"
fld public final static java.lang.String BADGE_REMOVE = "GeneralIcons.BadgeRemove"
fld public final static java.lang.String BUTTON_ATTACH = "GeneralIcons.ButtonAttach"
fld public final static java.lang.String BUTTON_RUN = "GeneralIcons.ButtonRun"
fld public final static java.lang.String CLEAR_FILTER = "GeneralIcons.ClearFilter"
fld public final static java.lang.String CLEAR_FILTER_HIGHL = "GeneralIcons.ClearFilterHighl"
fld public final static java.lang.String CLOSE_PANEL = "GeneralIcons.ClosePanel"
fld public final static java.lang.String COLLAPSED_SNIPPET = "GeneralIcons.CollapsedSnippet"
fld public final static java.lang.String DETACH = "GeneralIcons.Detach"
fld public final static java.lang.String DOWN = "GeneralIcons.Down"
fld public final static java.lang.String EDIT = "GeneralIcons.Edit"
fld public final static java.lang.String EMPTY = "GeneralIcons.Empty"
fld public final static java.lang.String ERROR = "GeneralIcons.Error"
fld public final static java.lang.String EXPANDED_SNIPPET = "GeneralIcons.ExpandedSnippet"
fld public final static java.lang.String EXPORT = "GeneralIcons.Export"
fld public final static java.lang.String FILTER = "GeneralIcons.Filter"
fld public final static java.lang.String FILTER_CONTAINS = "GeneralIcons.FilterContains"
fld public final static java.lang.String FILTER_ENDS_WITH = "GeneralIcons.FilterEndsWith"
fld public final static java.lang.String FILTER_HIGHL = "GeneralIcons.FilterHighl"
fld public final static java.lang.String FILTER_NOT_CONTAINS = "GeneralIcons.FilterNotContains"
fld public final static java.lang.String FILTER_REG_EXP = "GeneralIcons.FilterRegExp"
fld public final static java.lang.String FILTER_STARTS_WITH = "GeneralIcons.FilterStartsWith"
fld public final static java.lang.String FIND = "GeneralIcons.Find"
fld public final static java.lang.String FIND_NEXT = "GeneralIcons.FindNext"
fld public final static java.lang.String FIND_PREVIOUS = "GeneralIcons.FindPrevious"
fld public final static java.lang.String FOLDER = "GeneralIcons.Folder"
fld public final static java.lang.String FORWARD = "GeneralIcons.Forward"
fld public final static java.lang.String HIDE_COLUMN = "GeneralIcons.HideColumn"
fld public final static java.lang.String INFO = "GeneralIcons.Info"
fld public final static java.lang.String JAVA_PROCESS = "GeneralIcons.JavaProcess"
fld public final static java.lang.String MATCH_CASE = "GeneralIcons.MatchCase"
fld public final static java.lang.String MAXIMIZE_PANEL = "GeneralIcons.MaximizePanel"
fld public final static java.lang.String MINIMIZE_PANEL = "GeneralIcons.MinimizePanel"
fld public final static java.lang.String PAUSE = "GeneralIcons.Pause"
fld public final static java.lang.String PIE = "GeneralIcons.Pie"
fld public final static java.lang.String POPUP_ARROW = "GeneralIcons.PopupArrow"
fld public final static java.lang.String REMOVE = "GeneralIcons.Remove"
fld public final static java.lang.String RENAME = "GeneralIcons.Rename"
fld public final static java.lang.String RERUN = "GeneralIcons.Rerun"
fld public final static java.lang.String RESTORE_PANEL = "GeneralIcons.RestorePanel"
fld public final static java.lang.String RESUME = "GeneralIcons.Resume"
fld public final static java.lang.String SAVE = "GeneralIcons.Save"
fld public final static java.lang.String SAVE_AS = "GeneralIcons.SaveAs"
fld public final static java.lang.String SAVE_VIEW = "GeneralIcons.SaveView"
fld public final static java.lang.String SCALE_TO_FIT = "GeneralIcons.ScaleToFit"
fld public final static java.lang.String SETTINGS = "GeneralIcons.Settings"
fld public final static java.lang.String SET_FILTER = "GeneralIcons.SetFilter"
fld public final static java.lang.String SET_FILTER_HIGHL = "GeneralIcons.SetFilterHighl"
fld public final static java.lang.String SLAVE_DOWN = "GeneralIcons.SlaveDown"
fld public final static java.lang.String SLAVE_UP = "GeneralIcons.SlaveUp"
fld public final static java.lang.String SORT_ASCENDING = "GeneralIcons.SortAscending"
fld public final static java.lang.String SORT_DESCENDING = "GeneralIcons.SortDescending"
fld public final static java.lang.String START = "GeneralIcons.Start"
fld public final static java.lang.String STOP = "GeneralIcons.Stop"
fld public final static java.lang.String UP = "GeneralIcons.Up"
fld public final static java.lang.String UPDATE_AUTO = "GeneralIcons.UpdateAuto"
fld public final static java.lang.String UPDATE_NOW = "GeneralIcons.UpdateNow"
fld public final static java.lang.String ZOOM = "GeneralIcons.Zoom"
fld public final static java.lang.String ZOOM_IN = "GeneralIcons.ZoomIn"
fld public final static java.lang.String ZOOM_OUT = "GeneralIcons.ZoomOut"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

CLSS public final org.netbeans.modules.profiler.api.icons.Icons
cons public init()
innr public abstract interface static Keys
meth public static java.awt.Image getImage(java.lang.String)
meth public static java.lang.String getResource(java.lang.String)
meth public static javax.swing.Icon getIcon(java.lang.String)
meth public static javax.swing.ImageIcon getImageIcon(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.api.icons.Icons$Keys
 outer org.netbeans.modules.profiler.api.icons.Icons

CLSS public abstract interface org.netbeans.modules.profiler.api.icons.LanguageIcons
fld public final static java.lang.String ARRAY = "LanguageIcons.Array"
fld public final static java.lang.String CLASS = "LanguageIcons.Class"
fld public final static java.lang.String CLASS_ANONYMOUS = "LanguageIcons.ClassAnonymous"
fld public final static java.lang.String CONSTRUCTORS = "LanguageIcons.Constructors"
fld public final static java.lang.String CONSTRUCTOR_PACKAGE = "LanguageIcons.ConstructorPackage"
fld public final static java.lang.String CONSTRUCTOR_PRIVATE = "LanguageIcons.ConstructorPrivate"
fld public final static java.lang.String CONSTRUCTOR_PROTECTED = "LanguageIcons.ConstructorProtected"
fld public final static java.lang.String CONSTRUCTOR_PUBLIC = "LanguageIcons.ConstructorPublic"
fld public final static java.lang.String INITIALIZER = "LanguageIcons.Initializer"
fld public final static java.lang.String INITIALIZER_STATIC = "LanguageIcons.InitializerStatic"
fld public final static java.lang.String INSTANCE = "LanguageIcons.Instance"
fld public final static java.lang.String INTERFACE = "LanguageIcons.Interface"
fld public final static java.lang.String JAR = "LanguangeIcons.Jar"
fld public final static java.lang.String LIBRARIES = "LanguageIcons.Libraries"
fld public final static java.lang.String METHOD = "LanguageIcons.Method"
fld public final static java.lang.String METHODS = "LanguageIcons.Methods"
fld public final static java.lang.String METHOD_INHERITED = "LanguageIcons.MethodInheritedStatic"
fld public final static java.lang.String METHOD_PACKAGE = "LanguageIcons.MethodPackage"
fld public final static java.lang.String METHOD_PACKAGE_STATIC = "LanguageIcons.MethodPackageStatic"
fld public final static java.lang.String METHOD_PRIVATE = "LanguageIcons.MethodPrivate"
fld public final static java.lang.String METHOD_PRIVATE_STATIC = "LanguageIcons.MethodPrivateStatic"
fld public final static java.lang.String METHOD_PROTECTED = "LanguageIcons.MethodProtected"
fld public final static java.lang.String METHOD_PROTECTED_STATIC = "LanguageIcons.MethodProtectedStatic"
fld public final static java.lang.String METHOD_PUBLIC = "LanguageIcons.MethodPublic"
fld public final static java.lang.String METHOD_PUBLIC_STATIC = "LanguageIcons.MethodPublicStatic"
fld public final static java.lang.String PACKAGE = "LanguageIcons.Package"
fld public final static java.lang.String PRIMITIVE = "LanguageIcons.Primitive"
fld public final static java.lang.String VARIABLES = "LanguageIcons.Variables"
fld public final static java.lang.String VARIABLE_PACKAGE = "LanguageIcons.VariablePackage"
fld public final static java.lang.String VARIABLE_PACKAGE_STATIC = "LanguageIcons.VariablePackageStatic"
fld public final static java.lang.String VARIABLE_PRIVATE = "LanguageIcons.VariablePrivate"
fld public final static java.lang.String VARIABLE_PRIVATE_STATIC = "LanguageIcons.VariablePrivateStatic"
fld public final static java.lang.String VARIABLE_PROTECTED = "LanguageIcons.VariableProtected"
fld public final static java.lang.String VARIABLE_PROTECTED_STATIC = "LanguageIcons.VariableProtectedStatic"
fld public final static java.lang.String VARIABLE_PUBLIC = "LanguageIcons.VariablePublic"
fld public final static java.lang.String VARIABLE_PUBLIC_STATIC = "LanguageIcons.VariablePublicStatic"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

CLSS public abstract interface org.netbeans.modules.profiler.api.icons.ProfilerIcons
fld public final static java.lang.String ALL_THREADS = "ProfilerIcons.AllThreads"
fld public final static java.lang.String ATTACH = "ProfilerIcons.Attach"
fld public final static java.lang.String ATTACH_24 = "ProfilerIcons.Attach24"
fld public final static java.lang.String CONTROL_PANEL = "ProfilerIcons.ControlPanel"
fld public final static java.lang.String CPU = "ProfilerIcons.Cpu"
fld public final static java.lang.String CPU_32 = "ProfilerIcons.Cpu32"
fld public final static java.lang.String CUSTOM_32 = "ProfilerIcons.Custom32"
fld public final static java.lang.String DELTA_RESULTS = "ProfilerIcons.DeltaResults"
fld public final static java.lang.String FRAGMENT = "ProfilerIcons.Fragment"
fld public final static java.lang.String HEAP_DUMP = "ProfilerIcons.HeapDump"
fld public final static java.lang.String LIVE_RESULTS = "ProfilerIcons.LiveResults"
fld public final static java.lang.String MEMORY = "ProfilerIcons.Memory"
fld public final static java.lang.String MEMORY_32 = "ProfilerIcons.Memory32"
fld public final static java.lang.String MODIFY_PROFILING = "ProfilerIcons.ModifyProfiling"
fld public final static java.lang.String MONITORING = "ProfilerIcons.Monitoring"
fld public final static java.lang.String MONITORING_32 = "ProfilerIcons.Monitoring32"
fld public final static java.lang.String NODE_FORWARD = "ProfilerIcons.NodeForward"
fld public final static java.lang.String NODE_LEAF = "ProfilerIcons.NodeLeaf"
fld public final static java.lang.String NODE_REVERSE = "ProfilerIcons.NodeReverse"
fld public final static java.lang.String PROFILE = "ProfilerIcons.Profile"
fld public final static java.lang.String PROFILE_24 = "ProfilerIcons.Profile24"
fld public final static java.lang.String PROFILE_INACTIVE = "ProfilerIcons.ProfileInactive"
fld public final static java.lang.String PROFILE_RUNNING = "ProfilerIcons.ProfileRunning"
fld public final static java.lang.String RESET_RESULTS = "ProfilerIcons.ResetResults"
fld public final static java.lang.String RUN_GC = "ProfilerIcons.RunGC"
fld public final static java.lang.String SHOW_GRAPHS = "ProfilerIcons.ShowGraphs"
fld public final static java.lang.String SNAPSHOTS_COMPARE = "ProfilerIcons.SnapshotsCompare"
fld public final static java.lang.String SNAPSHOT_CPU_DO = "ProfilerIcons.SnapshotCpuDO"
fld public final static java.lang.String SNAPSHOT_CPU_DO_32 = "ProfilerIcons.SnapshotCpuDO32"
fld public final static java.lang.String SNAPSHOT_DO = "ProfilerIcons.SnapshotDO"
fld public final static java.lang.String SNAPSHOT_DO_32 = "ProfilerIcons.SnapshotDO32"
fld public final static java.lang.String SNAPSHOT_FRAGMENT_DO = "ProfilerIcons.SnapshotFragmentDO"
fld public final static java.lang.String SNAPSHOT_FRAGMENT_DO_32 = "ProfilerIcons.SnapshotFragmentDO32"
fld public final static java.lang.String SNAPSHOT_HEAP = "ProfilerIcons.SnapshotHeap"
fld public final static java.lang.String SNAPSHOT_MEMORY_32 = "ProfilerIcons.SnapshotMemory32"
fld public final static java.lang.String SNAPSHOT_MEMORY_DO = "ProfilerIcons.SnapshotMemoryDO"
fld public final static java.lang.String SNAPSHOT_MEMORY_DO_32 = "ProfilerIcons.SnapshotMemoryDO32"
fld public final static java.lang.String SNAPSHOT_OPEN = "ProfilerIcons.SnapshotOpen"
fld public final static java.lang.String SNAPSHOT_TAKE = "ProfilerIcons.SnapshotTake"
fld public final static java.lang.String SNAPSHOT_THREADS = "ProfilerIcons.SnapshotThreads"
fld public final static java.lang.String SQL_QUERY = "ProfilerIcons.SqlQuery"
fld public final static java.lang.String STARTUP_32 = "ProfilerIcons.Startup32"
fld public final static java.lang.String TAB_BACK_TRACES = "ProfilerIcons.TabBackTraces"
fld public final static java.lang.String TAB_CALL_TREE = "ProfilerIcons.TabCallTree"
fld public final static java.lang.String TAB_COMBINED = "ProfilerIcons.TabCombined"
fld public final static java.lang.String TAB_HOTSPOTS = "ProfilerIcons.TabHotSpots"
fld public final static java.lang.String TAB_INFO = "ProfilerIcons.TabInfo"
fld public final static java.lang.String TAB_MEMORY_RESULTS = "ProfilerIcons.TabMemoryResults"
fld public final static java.lang.String TAB_STACK_TRACES = "ProfilerIcons.TabStackTraces"
fld public final static java.lang.String TAB_SUBTREE = "ProfilerIcons.TabSubtree"
fld public final static java.lang.String TAKE_HEAP_DUMP_32 = "ProfilerIcons.TakeHeapDump32"
fld public final static java.lang.String TAKE_SNAPSHOT_CPU_32 = "ProfilerIcons.TakeSnapshotCpu32"
fld public final static java.lang.String TAKE_SNAPSHOT_FRAGMENT_32 = "ProfilerIcons.TakeSnapshotFragment32"
fld public final static java.lang.String TAKE_SNAPSHOT_MEMORY_32 = "ProfilerIcons.TakeSnapshotMemory32"
fld public final static java.lang.String THREAD = "ProfilerIcons.Thread"
fld public final static java.lang.String VIEW_LIVE_RESULTS_CPU_32 = "ProfilerIcons.ViewLiveResultsCpu32"
fld public final static java.lang.String VIEW_LIVE_RESULTS_FRAGMENT_32 = "ProfilerIcons.ViewLiveResultsFragment32"
fld public final static java.lang.String VIEW_LIVE_RESULTS_MEMORY_32 = "ProfilerIcons.ViewLiveResultsMemory32"
fld public final static java.lang.String VIEW_LOCKS_32 = "ProfilerIcons.ViewLocks32"
fld public final static java.lang.String VIEW_TELEMETRY_32 = "ProfilerIcons.ViewTelemetry32"
fld public final static java.lang.String VIEW_THREADS_32 = "ProfilerIcons.ViewThreads32"
fld public final static java.lang.String WINDOW_CONTROL_PANEL = "ProfilerIcons.WindowControlPanel"
fld public final static java.lang.String WINDOW_LIVE_RESULTS = "ProfilerIcons.WindowLiveResults"
fld public final static java.lang.String WINDOW_LOCKS = "ProfilerIcons.WindowLocks"
fld public final static java.lang.String WINDOW_SQL = "ProfilerIcons.WindowSql"
fld public final static java.lang.String WINDOW_TELEMETRY = "ProfilerIcons.WindowTelemetry"
fld public final static java.lang.String WINDOW_TELEMETRY_OVERVIEW = "ProfilerIcons.WindowTelemetryOverview"
fld public final static java.lang.String WINDOW_THREADS = "ProfilerIcons.WindowThreads"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

CLSS public final org.netbeans.modules.profiler.api.java.ExternalPackages
cons public init()
meth public static java.util.List<org.netbeans.modules.profiler.api.java.SourcePackageInfo> forPath(org.openide.filesystems.FileObject,boolean)
supr java.lang.Object
hfds pathComparator
hcls FileClassInfo,FileMethodInfo,FilePackageInfo,PlainClassInfo

CLSS public final org.netbeans.modules.profiler.api.java.JavaProfilerSource
meth public boolean hasAnnotation(java.lang.String)
meth public boolean hasAnnotation(java.lang.String[],boolean)
meth public boolean isApplet()
meth public boolean isInstanceOf(java.lang.String)
meth public boolean isInstanceOf(java.lang.String[],boolean)
meth public boolean isOffsetValid(int)
meth public boolean isRunnable()
meth public boolean isTest()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getClasses()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getMainClasses()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getConstructors()
meth public org.netbeans.modules.profiler.api.java.SourceClassInfo getEnclosingClass(int)
meth public org.netbeans.modules.profiler.api.java.SourceClassInfo getTopLevelClass()
meth public org.netbeans.modules.profiler.api.java.SourceClassInfo resolveClassAtPosition(int,boolean)
meth public org.netbeans.modules.profiler.api.java.SourceMethodInfo getEnclosingMethod(int)
meth public org.netbeans.modules.profiler.api.java.SourceMethodInfo resolveMethodAtPosition(int)
meth public static org.netbeans.modules.profiler.api.java.JavaProfilerSource createFrom(org.openide.filesystems.FileObject)
supr org.netbeans.modules.profiler.api.ProfilerSource
hfds impl

CLSS public final org.netbeans.modules.profiler.api.java.ProfilerTypeUtils
cons public init()
meth public static java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> findClasses(java.lang.String,java.util.Set<org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope>,org.openide.util.Lookup$Provider)
meth public static java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> getMainClasses(org.openide.util.Lookup$Provider)
meth public static java.util.Collection<org.netbeans.modules.profiler.api.java.SourcePackageInfo> getPackages(boolean,org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope,org.openide.util.Lookup$Provider)
meth public static org.netbeans.modules.profiler.api.java.SourceClassInfo resolveClass(java.lang.String,org.openide.util.Lookup$Provider)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo
cons public init(java.lang.String,java.lang.String,java.lang.String)
fld public final static java.util.Comparator<org.netbeans.modules.profiler.api.java.SourceClassInfo> COMPARATOR
meth protected final boolean isAnonymous(java.lang.String)
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInnerClases()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInterfaces()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getSubclasses()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getConstructors()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getMethods(boolean)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo getSuperType()
meth public abstract org.openide.filesystems.FileObject getFile()
meth public boolean equals(java.lang.Object)
meth public boolean isAnonymous()
meth public final java.lang.String getQualifiedName()
meth public final java.lang.String getSimpleName()
meth public final java.lang.String getVMName()
meth public int hashCode()
supr java.lang.Object
hfds anonymousInnerClassPattern,qualName,simpleName,vmName

CLSS public org.netbeans.modules.profiler.api.java.SourceMethodInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,int)
meth public boolean equals(java.lang.Object)
meth public final boolean isExecutable()
meth public final int getModifiers()
meth public final java.lang.String getClassName()
meth public final java.lang.String getName()
meth public final java.lang.String getSignature()
meth public final java.lang.String getVMName()
meth public int hashCode()
supr java.lang.Object
hfds className,execFlag,modifiers,name,signature,vmName

CLSS public abstract org.netbeans.modules.profiler.api.java.SourcePackageInfo
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope)
innr public final static !enum Scope
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> getClasses()
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourcePackageInfo> getSubpackages()
meth public java.lang.String getBinaryName()
meth public java.lang.String getSimpleName()
meth public org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope getScope()
supr java.lang.Object
hfds fqn,scope,simpleName

CLSS public final static !enum org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope
 outer org.netbeans.modules.profiler.api.java.SourcePackageInfo
fld public final static org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope DEPENDENCIES
fld public final static org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope SOURCE
meth public static org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope valueOf(java.lang.String)
meth public static org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope[] values()
supr java.lang.Enum<org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope>

CLSS public final org.netbeans.modules.profiler.api.project.ProjectContentsSupport
meth public java.lang.String getInstrumentationFilter(boolean)
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProfilingRoots(org.openide.filesystems.FileObject,boolean)
meth public static org.netbeans.modules.profiler.api.project.ProjectContentsSupport get(org.openide.util.Lookup$Provider)
meth public void reset()
supr java.lang.Object
hfds DEFAULT,EMPTY_SELECTION,providers

CLSS public final org.netbeans.modules.profiler.api.project.ProjectProfilingSupport
meth public boolean areProfilingPointsSupported()
meth public boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public boolean isAttachSupported()
meth public boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public boolean isProfilingSupported()
meth public boolean startProfilingSession(org.openide.filesystems.FileObject,boolean)
meth public org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public static org.netbeans.modules.profiler.api.project.ProjectProfilingSupport get(org.openide.util.Lookup$Provider)
meth public void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr java.lang.Object
hfds DEFAULT,provider

CLSS public abstract org.netbeans.modules.profiler.spi.ActionsSupportProvider
cons public init()
meth public abstract javax.swing.KeyStroke registerAction(java.lang.String,javax.swing.Action,javax.swing.ActionMap,javax.swing.InputMap)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.EditorSupportProvider
cons public init()
fld public static org.netbeans.modules.profiler.spi.EditorSupportProvider NULL
meth public abstract boolean currentlyInJavaEditor()
meth public abstract boolean isOffsetValid(org.openide.filesystems.FileObject,int)
meth public abstract int getCurrentOffset()
meth public abstract int getLineForOffset(org.openide.filesystems.FileObject,int)
meth public abstract int getOffsetForLine(org.openide.filesystems.FileObject,int)
meth public abstract int[] getSelectionOffsets()
meth public abstract org.netbeans.modules.profiler.api.EditorContext getMostActiveJavaEditorContext()
meth public abstract org.openide.filesystems.FileObject getCurrentFile()
meth public abstract org.openide.util.Lookup$Provider getCurrentProject()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.IconsProvider
cons public init()
innr public abstract static Basic
meth public abstract java.awt.Image getImage(java.lang.String)
meth public abstract java.lang.String getResource(java.lang.String)
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.profiler.spi.IconsProvider$Basic
 outer org.netbeans.modules.profiler.spi.IconsProvider
cons public init()
meth protected java.awt.Image getDynamicImage(java.lang.String)
meth protected java.lang.String getImagePath(java.lang.String)
meth protected void initStaticImages(java.util.Map<java.lang.String,java.lang.String>)
meth public final java.awt.Image getImage(java.lang.String)
meth public final java.lang.String getResource(java.lang.String)
supr org.netbeans.modules.profiler.spi.IconsProvider
hfds images

CLSS public abstract org.netbeans.modules.profiler.spi.JavaPlatformManagerProvider
cons public init()
meth public abstract java.util.List<org.netbeans.modules.profiler.spi.JavaPlatformProvider> getPlatforms()
meth public abstract org.netbeans.modules.profiler.spi.JavaPlatformProvider getDefaultPlatform()
meth public abstract void showCustomizer()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.JavaPlatformProvider
cons public init()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getPlatformId()
meth public abstract java.lang.String getPlatformJavaFile()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.profiler.spi.LoadGenPlugin
innr public abstract interface static Callback
innr public final static !enum Result
meth public abstract boolean isRunning()
meth public abstract java.util.Collection<org.openide.filesystems.FileObject> listScripts(org.openide.util.Lookup$Provider)
meth public abstract java.util.Set<java.lang.String> getSupportedExtensions()
meth public abstract void start(java.lang.String,org.netbeans.modules.profiler.spi.LoadGenPlugin$Callback)
meth public abstract void stop()
meth public abstract void stop(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.profiler.spi.LoadGenPlugin$Callback
 outer org.netbeans.modules.profiler.spi.LoadGenPlugin
fld public final static org.netbeans.modules.profiler.spi.LoadGenPlugin$Callback NULL
meth public abstract void afterStart(org.netbeans.modules.profiler.spi.LoadGenPlugin$Result)
meth public abstract void afterStop(org.netbeans.modules.profiler.spi.LoadGenPlugin$Result)

CLSS public final static !enum org.netbeans.modules.profiler.spi.LoadGenPlugin$Result
 outer org.netbeans.modules.profiler.spi.LoadGenPlugin
fld public final static org.netbeans.modules.profiler.spi.LoadGenPlugin$Result FAIL
fld public final static org.netbeans.modules.profiler.spi.LoadGenPlugin$Result SUCCESS
fld public final static org.netbeans.modules.profiler.spi.LoadGenPlugin$Result TIMEOUT
meth public static org.netbeans.modules.profiler.spi.LoadGenPlugin$Result valueOf(java.lang.String)
meth public static org.netbeans.modules.profiler.spi.LoadGenPlugin$Result[] values()
supr java.lang.Enum<org.netbeans.modules.profiler.spi.LoadGenPlugin$Result>

CLSS public abstract org.netbeans.modules.profiler.spi.ProfilerDialogsProvider
cons public init()
meth public abstract java.lang.Boolean displayConfirmation(java.lang.String,java.lang.String,boolean)
meth public abstract java.lang.Boolean displayConfirmationDNSA(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,boolean)
meth public abstract void displayError(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void displayInfo(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void displayInfoDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract void displayWarning(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void displayWarningDNSA(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.ProfilerStorageProvider
cons public init()
innr public abstract static Abstract
meth public abstract org.openide.filesystems.FileObject getGlobalFolder(boolean) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getProjectFolder(org.openide.util.Lookup$Provider,boolean) throws java.io.IOException
meth public abstract org.openide.util.Lookup$Provider getProjectFromFolder(org.openide.filesystems.FileObject)
meth public abstract void deleteGlobalProperties(java.lang.String) throws java.io.IOException
meth public abstract void deleteProjectProperties(org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public abstract void loadGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public abstract void loadProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public abstract void saveGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public abstract void saveProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract static org.netbeans.modules.profiler.spi.ProfilerStorageProvider$Abstract
 outer org.netbeans.modules.profiler.spi.ProfilerStorageProvider
cons public init()
fld protected java.lang.String EXT
meth protected void deleteProperties(org.openide.filesystems.FileObject) throws java.io.IOException
meth protected void loadProperties(java.util.Properties,org.openide.filesystems.FileObject) throws java.io.IOException
meth protected void saveProperties(java.util.Properties,org.openide.filesystems.FileObject) throws java.io.IOException
meth public void deleteGlobalProperties(java.lang.String) throws java.io.IOException
meth public void deleteProjectProperties(org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public void loadGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public void loadProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
meth public void saveGlobalProperties(java.util.Properties,java.lang.String) throws java.io.IOException
meth public void saveProjectProperties(java.util.Properties,org.openide.util.Lookup$Provider,java.lang.String) throws java.io.IOException
supr org.netbeans.modules.profiler.spi.ProfilerStorageProvider

CLSS public abstract org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider
cons public init()
meth public abstract boolean hasSubprojects(org.openide.util.Lookup$Provider)
meth public abstract java.lang.String getDisplayName(org.openide.util.Lookup$Provider)
meth public abstract javax.swing.Icon getIcon(org.openide.util.Lookup$Provider)
meth public abstract org.openide.filesystems.FileObject getProjectDirectory(org.openide.util.Lookup$Provider)
meth public abstract org.openide.util.Lookup$Provider getMainProject()
meth public abstract org.openide.util.Lookup$Provider getProject(org.openide.filesystems.FileObject)
meth public abstract org.openide.util.Lookup$Provider[] getOpenedProjects()
meth public abstract void addOpenProjectsListener(javax.swing.event.ChangeListener)
meth public abstract void fetchSubprojects(org.openide.util.Lookup$Provider,java.util.Set<org.openide.util.Lookup$Provider>)
meth public abstract void removeOpenProjectsListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.profiler.spi.SessionListener
innr public abstract static Adapter
meth public abstract void onShutdown()
meth public abstract void onStartup(org.netbeans.lib.profiler.common.ProfilingSettings,org.openide.util.Lookup$Provider)

CLSS public abstract static org.netbeans.modules.profiler.spi.SessionListener$Adapter
 outer org.netbeans.modules.profiler.spi.SessionListener
cons public init()
intf org.netbeans.modules.profiler.spi.SessionListener
meth public void onShutdown()
meth public void onStartup(org.netbeans.lib.profiler.common.ProfilingSettings,org.openide.util.Lookup$Provider)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.profiler.spi.java.AbstractJavaProfilerSource
fld public final static org.netbeans.modules.profiler.spi.java.AbstractJavaProfilerSource NULL
meth public abstract boolean hasAnnotation(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract boolean hasAnnotation(org.openide.filesystems.FileObject,java.lang.String[],boolean)
meth public abstract boolean isApplet(org.openide.filesystems.FileObject)
meth public abstract boolean isInstanceOf(org.openide.filesystems.FileObject,java.lang.String)
meth public abstract boolean isInstanceOf(org.openide.filesystems.FileObject,java.lang.String[],boolean)
meth public abstract boolean isOffsetValid(org.openide.filesystems.FileObject,int)
meth public abstract boolean isTest(org.openide.filesystems.FileObject)
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getClasses(org.openide.filesystems.FileObject)
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getMainClasses(org.openide.filesystems.FileObject)
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getConstructors(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo getEnclosingClass(org.openide.filesystems.FileObject,int)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo getTopLevelClass(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo resolveClassAtPosition(org.openide.filesystems.FileObject,int,boolean)
meth public abstract org.netbeans.modules.profiler.api.java.SourceMethodInfo getEnclosingMethod(org.openide.filesystems.FileObject,int)
meth public abstract org.netbeans.modules.profiler.api.java.SourceMethodInfo resolveMethodAtPosition(org.openide.filesystems.FileObject,int)

CLSS public abstract org.netbeans.modules.profiler.spi.java.GoToSourceProvider
cons public init()
meth public abstract boolean openFile(org.openide.filesystems.FileObject,int)
meth public abstract boolean openSource(org.openide.util.Lookup$Provider,java.lang.String,java.lang.String,java.lang.String,int)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider
cons public init()
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> getMainClasses()
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourcePackageInfo> getPackages(boolean,org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo resolveClass(java.lang.String)
meth public java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> findClasses(java.lang.String,java.util.Set<org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope>)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider
cons public init()
meth public abstract java.lang.String getInstrumentationFilter(boolean)
meth public abstract org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProfilingRoots(org.openide.filesystems.FileObject,boolean)
meth public abstract void reset()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider
cons public init()
innr public static Basic
meth public abstract boolean areProfilingPointsSupported()
meth public abstract boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public abstract boolean isAttachSupported()
meth public abstract boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public abstract boolean isProfilingSupported()
meth public abstract boolean startProfilingSession(org.openide.filesystems.FileObject,boolean)
meth public abstract org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public abstract void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr java.lang.Object

CLSS public static org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider$Basic
 outer org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider
cons public init()
meth public boolean areProfilingPointsSupported()
meth public boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public boolean isAttachSupported()
meth public boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public boolean isProfilingSupported()
meth public boolean startProfilingSession(org.openide.filesystems.FileObject,boolean)
meth public org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

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

