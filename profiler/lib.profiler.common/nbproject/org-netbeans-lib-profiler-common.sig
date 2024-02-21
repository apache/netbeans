#Signature file v4.1
#Version 1.70

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

CLSS public final org.netbeans.lib.profiler.common.AttachSettings
cons public init()
fld public final static java.lang.String PROP_ATTACH_DIRECT = "profiler.attach.direct"
fld public final static java.lang.String PROP_ATTACH_DYNAMIC_AUTO = "profiler.attach.dynamic.auto"
fld public final static java.lang.String PROP_ATTACH_DYNAMIC_JDK16 = "profiler.attach.dynamic.jdk16"
fld public final static java.lang.String PROP_ATTACH_DYNAMIC_PID = "profiler.attach.dynamic.pid"
fld public final static java.lang.String PROP_ATTACH_DYNAMIC_PROCESS_NAME = "profiler.attach.dynamic.processName"
fld public final static java.lang.String PROP_ATTACH_HOST = "profiler.attach.host"
fld public final static java.lang.String PROP_ATTACH_HOST_OS = "profiler.attach.host.os"
fld public final static java.lang.String PROP_ATTACH_PORT = "profiler.attach.port"
fld public final static java.lang.String PROP_ATTACH_REMOTE = "profiler.attach.remote"
fld public final static java.lang.String PROP_ATTACH_SERVER_TYPE = "profiler.attach.server.type"
fld public final static java.lang.String PROP_ATTACH_TARGET_TYPE = "profiler.attach.target.type"
meth public boolean isAutoSelectProcess()
meth public boolean isDirect()
meth public boolean isDynamic16()
meth public boolean isRemote()
meth public int getPid()
meth public int getPort()
meth public java.lang.String debug()
meth public java.lang.String getHost()
meth public java.lang.String getHostOS()
meth public java.lang.String getProcessName()
meth public java.lang.String getServerType()
meth public java.lang.String getTargetType()
meth public java.lang.String toString()
meth public void applySettings(org.netbeans.lib.profiler.ProfilerEngineSettings)
meth public void copyInto(org.netbeans.lib.profiler.common.AttachSettings)
meth public void load(java.util.Map)
meth public void setAutoSelectProcess(boolean)
meth public void setDirect(boolean)
meth public void setDynamic16(boolean)
meth public void setHost(java.lang.String)
meth public void setHostOS(java.lang.String)
meth public void setPid(int)
meth public void setPort(int)
meth public void setProcessName(java.lang.String)
meth public void setRemote(boolean)
meth public void setServerType(java.lang.String)
meth public void setTargetType(java.lang.String)
meth public void store(java.util.Map)
supr java.lang.Object
hfds autoSelect,direct,dynamic16,host,hostOS,pid,processName,remote,serverType,targetType,transientPort

CLSS public final org.netbeans.lib.profiler.common.CommonUtils
cons public init()
meth public static void runInEventDispatchThread(java.lang.Runnable)
meth public static void runInEventDispatchThreadAndWait(java.lang.Runnable)
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.profiler.common.GlobalProfilingSettings
meth public abstract int getCalibrationPortNo()
meth public abstract int getPortNo()
meth public abstract java.lang.String getJavaPlatformForProfiling()
meth public abstract void setCalibrationPortNo(int)
meth public abstract void setJavaPlatformForProfiling(java.lang.String)
meth public abstract void setPortNo(int)

CLSS public abstract org.netbeans.lib.profiler.common.Profiler
cons public init()
fld public final static int ERROR = 16
fld public final static int EXCEPTION = 8
fld public final static int INFORMATIONAL = 1
fld public final static int MODE_ATTACH = 0
fld public final static int MODE_PROFILE = 1
fld public final static int PROFILING_INACTIVE = 1
fld public final static int PROFILING_IN_TRANSITION = 128
fld public final static int PROFILING_PAUSED = 8
fld public final static int PROFILING_RUNNING = 4
fld public final static int PROFILING_STARTED = 2
fld public final static int PROFILING_STOPPED = 16
fld public final static int USER = 4
fld public final static int WARNING = 2
meth protected final void fireInstrumentationChanged(int,int)
meth protected final void fireLockContentionMonitoringChange()
meth protected final void fireProfilingStateChange(int,int)
meth protected final void fireServerStateChanged(int,int)
meth protected final void fireThreadsMonitoringChange()
meth public abstract boolean attachToApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.AttachSettings)
meth public abstract boolean connectToStartedApp(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public abstract boolean getLockContentionMonitoringEnabled()
meth public abstract boolean getThreadsMonitoringEnabled()
meth public abstract boolean modifyAvailable()
meth public abstract boolean profileClass(org.netbeans.lib.profiler.common.ProfilingSettings,org.netbeans.lib.profiler.common.SessionSettings)
meth public abstract boolean rerunAvailable()
meth public abstract boolean runCalibration(boolean,java.lang.String,java.lang.String,int)
meth public abstract boolean shutdownBlockedAgent(java.lang.String,int,int)
meth public abstract int getAgentState(java.lang.String,int,int)
meth public abstract int getPlatformArchitecture(java.lang.String)
meth public abstract int getProfilingMode()
meth public abstract int getProfilingState()
meth public abstract int getServerProgress()
meth public abstract int getServerState()
meth public abstract java.lang.String getLibsDir()
meth public abstract java.lang.String getPlatformJDKVersion(java.lang.String)
meth public abstract java.lang.String getPlatformJavaFile(java.lang.String)
meth public abstract org.netbeans.lib.profiler.TargetAppRunner getTargetAppRunner()
meth public abstract org.netbeans.lib.profiler.common.GlobalProfilingSettings getGlobalProfilingSettings()
meth public abstract org.netbeans.lib.profiler.common.ProfilingSettings getLastProfilingSettings()
meth public abstract org.netbeans.lib.profiler.common.SessionSettings getCurrentSessionSettings()
meth public abstract org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager getVMTelemetryManager()
meth public abstract org.netbeans.lib.profiler.results.threads.ThreadsDataManager getThreadsManager()
meth public abstract void detachFromApp()
meth public abstract void instrumentSelectedRoots(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public abstract void log(int,java.lang.String)
meth public abstract void modifyCurrentProfiling(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public abstract void notifyException(int,java.lang.Exception)
meth public abstract void openJavaSource(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void rerunLastProfiling()
meth public abstract void setLockContentionMonitoringEnabled(boolean)
meth public abstract void setThreadsMonitoringEnabled(boolean)
meth public abstract void stopApp()
meth public boolean prepareInstrumentation(org.netbeans.lib.profiler.common.ProfilingSettings) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public final boolean profilingInProgress()
meth public final void addProfilingStateListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void removeProfilingStateListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public static org.netbeans.lib.profiler.common.Profiler getDefault()
meth public static void debug(java.lang.Exception)
meth public static void debug(java.lang.String)
supr java.lang.Object
hfds DEBUG,currentProfilingState,defaultProfiler,profilingStateListeners

CLSS public org.netbeans.lib.profiler.common.ProfilingSettings
cons public init()
cons public init(java.lang.String)
fld public final static boolean QUICK_FILTER_EXCLUSIVE = false
fld public final static boolean QUICK_FILTER_INCLUSIVE = true
fld public final static int PROFILE_CPU_ENTIRE = 8
fld public final static int PROFILE_CPU_JDBC = 256
fld public final static int PROFILE_CPU_PART = 16
fld public final static int PROFILE_CPU_SAMPLING = 64
fld public final static int PROFILE_CPU_STOPWATCH = 32
fld public final static int PROFILE_MEMORY_ALLOCATIONS = 2
fld public final static int PROFILE_MEMORY_LIVENESS = 4
fld public final static int PROFILE_MEMORY_SAMPLING = 128
fld public final static int PROFILE_MONITOR = 1
fld public final static java.lang.String LINES_PREFIX = "[lines]"
fld public final static java.lang.String PROP_CODE_REGION_CPU_RES_BUF_SIZE = "profiler.settings.code.region.cpu.res.buf.size"
fld public final static java.lang.String PROP_CPU_PROFILING_TYPE = "profiler.settings.cpu.profiling.type"
fld public final static java.lang.String PROP_EXCLUDE_WAIT_TIME = "profiler.settings.cpu.exclude.wait.time"
fld public final static java.lang.String PROP_FRAGMENT_SELECTION = "profiler.settings.fragment.selection"
fld public final static java.lang.String PROP_INSTRUMENTATION_MARKER_METHODS_PREFIX = "profiler.settings.istrumentation.marker.methods-"
fld public final static java.lang.String PROP_INSTRUMENTATION_MARKER_METHODS_SIZE = "profiler.settings.instrumentation.marker.methods.size"
fld public final static java.lang.String PROP_INSTRUMENTATION_ROOT_METHODS_PREFIX = "profiler.settings.istrumentation.root.methods-"
fld public final static java.lang.String PROP_INSTRUMENTATION_ROOT_METHODS_SIZE = "profiler.settings.instrumentation.root.methods.size"
fld public final static java.lang.String PROP_INSTRUMENT_EMPTY_METHODS = "profiler.settings.instrument.empty.methods"
fld public final static java.lang.String PROP_INSTRUMENT_GETTER_SETTER_METHODS = "profiler.settings.istrument.getter.setter.methods"
fld public final static java.lang.String PROP_INSTRUMENT_METHOD_INVOKE = "profiler.settings.instrument.method.invoke"
fld public final static java.lang.String PROP_INSTRUMENT_SPAWNED_THREADS = "profiler.settings.instrument.spawned.threads"
fld public final static java.lang.String PROP_INSTR_FILTER = "profiler.settings.instrumentation.filter."
fld public final static java.lang.String PROP_INSTR_SCHEME = "profiler.settings.instr.scheme"
fld public final static java.lang.String PROP_IS_PRESET = "profiler.settigns.ispreset"
fld public final static java.lang.String PROP_JAVA_PLATFORM = "profiler.settings.override.java.platform"
fld public final static java.lang.String PROP_JVM_ARGS = "profiler.settings.override.jvm.args"
fld public final static java.lang.String PROP_LOCKCONTENTION_MONITORING_ENABLED = "profiler.settings.lockcontention.monitoring.enabled"
fld public final static java.lang.String PROP_N_PROFILED_THREADS_LIMIT = "profiler.settings.n.profiled.threads.limit"
fld public final static java.lang.String PROP_OBJ_ALLOC_STACK_SAMPLING_DEPTH = "profiler.settings.obj.alloc.stack.sampling.depth"
fld public final static java.lang.String PROP_OBJ_ALLOC_STACK_SAMPLING_INTERVAL = "profiler.settings.obj.alloc.stack.sampling.interval"
fld public final static java.lang.String PROP_OVERRIDE_GLOBAL_SETTINGS = "profiler.settings.override"
fld public final static java.lang.String PROP_PROFILE_UNDERLYING_FRAMEWORK = "profiler.settings.profile.underlying.framework"
fld public final static java.lang.String PROP_PROFILING_POINTS_ENABLED = "profiler.settings.profilingpoints.enabled"
fld public final static java.lang.String PROP_PROFILING_TYPE = "profiler.settings.profiling.type"
fld public final static java.lang.String PROP_QUICK_FILTER = "profiler.settings.cpu.quick.filter"
fld public final static java.lang.String PROP_RUN_GC_ON_GET_RESULTS_IN_MEMORY_PROFILING = "profiler.settings.run.gc.on.get.results.in.memory.profiling"
fld public final static java.lang.String PROP_SAMPLING_FREQUENCY = "profiler.settings.cpu.sampling.frequency"
fld public final static java.lang.String PROP_SAMPLING_INTERVAL = "profiler.settings.sampling.interval"
fld public final static java.lang.String PROP_SETTINGS_NAME = "profiler.settings.settings.name"
fld public final static java.lang.String PROP_SORT_RESULTS_BY_THREAD_CPU_TIME = "profiler.settings.sort.results.by.thread.cpu.time"
fld public final static java.lang.String PROP_STACK_DEPTH_LIMIT = "profiler.settings.stack.depth.limit"
fld public final static java.lang.String PROP_THREADS_MONITORING_ENABLED = "profiler.settings.threads.monitoring.enabled"
fld public final static java.lang.String PROP_THREADS_SAMPLING_ENABLED = "profiler.settings.threads.sampling.enabled"
fld public final static java.lang.String PROP_THREAD_CPU_TIMER_ON = "profiler.settings.thread.cpu.timer.on"
fld public final static java.lang.String PROP_WORKING_DIR = "profiler.settings.override.working.dir"
meth public boolean getExcludeWaitTime()
meth public boolean getInstrumentEmptyMethods()
meth public boolean getInstrumentGetterSetterMethods()
meth public boolean getInstrumentMethodInvoke()
meth public boolean getInstrumentSpawnedThreads()
meth public boolean getLockContentionMonitoringEnabled()
meth public boolean getOverrideGlobalSettings()
meth public boolean getProfileUnderlyingFramework()
meth public boolean getRunGCOnGetResultsInMemoryProfiling()
meth public boolean getSortResultsByThreadCPUTime()
meth public boolean getThreadCPUTimerOn()
meth public boolean getThreadsMonitoringEnabled()
meth public boolean getThreadsSamplingEnabled()
meth public boolean isPreset()
meth public boolean useProfilingPoints()
meth public int getAllocStackTraceLimit()
meth public int getAllocTrackEvery()
meth public int getCPUProfilingType()
meth public int getCodeRegionCPUResBufSize()
meth public int getInstrScheme()
meth public int getNProfiledThreadsLimit()
meth public int getProfilingType()
meth public int getSamplingFrequency()
meth public int getSamplingInterval()
meth public int getStackDepthLimit()
meth public java.lang.String debug()
meth public java.lang.String getJVMArgs()
meth public java.lang.String getJavaPlatformName()
meth public java.lang.String getSettingsName()
meth public java.lang.String getWorkingDir()
meth public java.lang.String toString()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getCodeFragmentSelection()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getInstrumentationMarkerMethods()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getInstrumentationMethods()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getInstrumentationRootMethods()
meth public org.netbeans.lib.profiler.filters.GenericFilter getInstrumentationFilter()
meth public static boolean isCPUSettings(int)
meth public static boolean isCPUSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public static boolean isJDBCSettings(int)
meth public static boolean isJDBCSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public static boolean isMemorySettings(int)
meth public static boolean isMemorySettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public static boolean isMonitorSettings(int)
meth public static boolean isMonitorSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public static void saveRootMethods(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[],java.util.Map)
meth public void addRootMethod(java.lang.String,java.lang.String,java.lang.String)
meth public void addRootMethods(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[])
meth public void applySettings(org.netbeans.lib.profiler.ProfilerEngineSettings)
meth public void copySettingsInto(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public void load(java.util.Map)
meth public void load(java.util.Map,java.lang.String)
meth public void setAllocStackTraceLimit(int)
meth public void setAllocTrackEvery(int)
meth public void setCPUProfilingType(int)
meth public void setCodeFragmentSelection(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public void setCodeRegionCPUResBufSize(int)
meth public void setExcludeWaitTime(boolean)
meth public void setInstrScheme(int)
meth public void setInstrumentEmptyMethods(boolean)
meth public void setInstrumentGetterSetterMethods(boolean)
meth public void setInstrumentMethodInvoke(boolean)
meth public void setInstrumentSpawnedThreads(boolean)
meth public void setInstrumentationFilter(org.netbeans.lib.profiler.filters.GenericFilter)
meth public void setInstrumentationMarkerMethods(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[])
meth public void setInstrumentationRootMethods(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[])
meth public void setIsPreset(boolean)
meth public void setJVMArgs(java.lang.String)
meth public void setJavaPlatformName(java.lang.String)
meth public void setLockContentionMonitoringEnabled(boolean)
meth public void setNProfiledThreadsLimit(int)
meth public void setOverrideGlobalSettings(boolean)
meth public void setProfileUnderlyingFramework(boolean)
meth public void setProfilingType(int)
meth public void setRunGCOnGetResultsInMemoryProfiling(boolean)
meth public void setSamplingFrequency(int)
meth public void setSamplingInterval(int)
meth public void setSettingsName(java.lang.String)
meth public void setSortResultsByThreadCPUTime(boolean)
meth public void setStackDepthLimit(int)
meth public void setThreadCPUTimerOn(boolean)
meth public void setThreadsMonitoringEnabled(boolean)
meth public void setThreadsSamplingEnabled(boolean)
meth public void setUseProfilingPoints(boolean)
meth public void setWorkingDir(java.lang.String)
meth public void store(java.util.Map)
meth public void store(java.util.Map,java.lang.String)
supr java.lang.Object
hfds DEFAULT_PROFILING_SETTINGS_NAME,UNKNOWN_PROFILING_SETTINGS_NAME,allocStackTraceLimit,allocTrackEvery,bundle,codeRegionCPUResBufSize,cpuProfilingType,excludeWaitTime,fragmentSelection,instrScheme,instrumentEmptyMethods,instrumentGetterSetterMethods,instrumentMethodInvoke,instrumentSpawnedThreads,instrumentationFilter,instrumentationMarkerMethods,instrumentationRootMethods,isPreset,jvmArgs,lockContentionMonitoringEnabled,nProfiledThreadsLimit,overrideGlobalSettings,platformName,profileUnderlyingFramework,profilingType,runGCOnGetResultsInMemoryProfiling,samplingFrequency,samplingInterval,settingsName,sortResultsByThreadCPUTime,stackDepthLimit,threadCPUTimerOn,threadsMonitoringEnabled,threadsSamplingEnabled,useProfilingPoints,workingDir

CLSS public org.netbeans.lib.profiler.common.ProfilingSettingsPresets
cons public init()
meth public static org.netbeans.lib.profiler.common.ProfilingSettings createCPUPreset()
meth public static org.netbeans.lib.profiler.common.ProfilingSettings createCPUPreset(int)
meth public static org.netbeans.lib.profiler.common.ProfilingSettings createMemoryPreset()
meth public static org.netbeans.lib.profiler.common.ProfilingSettings createMemoryPreset(int)
meth public static org.netbeans.lib.profiler.common.ProfilingSettings createMonitorPreset()
supr java.lang.Object
hfds CPU_PRESET_NAME,MEMORY_PRESET_NAME,MONITOR_PRESET_NAME,bundle
hcls CPUPreset,MemoryPreset,MonitorPreset

CLSS public final org.netbeans.lib.profiler.common.SessionSettings
cons public init()
fld public final static java.lang.String PROP_ARCHITECTURE = "profiler.session.java.architecture"
fld public final static java.lang.String PROP_ARGS = "profiler.session.args"
fld public final static java.lang.String PROP_CLASS_NAME = "profiler.session.class.name"
fld public final static java.lang.String PROP_CLASS_PATH = "profiler.session.class.path"
fld public final static java.lang.String PROP_JAVA_EXECUTABLE = "profiler.session.java.executable"
fld public final static java.lang.String PROP_JAVA_VERSION = "profiler.session.java.version"
fld public final static java.lang.String PROP_JVM_ARGS = "profiler.session.jvm.args"
fld public final static java.lang.String PROP_PORT_NO = "profiler.session.port.no"
fld public final static java.lang.String PROP_REMOTE_HOST = "profiler.session.remote.host"
fld public final static java.lang.String PROP_WORKING_DIR = "profiler.session.working.dir"
meth public int getPortNo()
meth public int getSystemArchitecture()
meth public java.lang.String debug()
meth public java.lang.String getJVMArgs()
meth public java.lang.String getJavaExecutable()
meth public java.lang.String getJavaVersionString()
meth public java.lang.String getMainArgs()
meth public java.lang.String getMainClass()
meth public java.lang.String getMainClassPath()
meth public java.lang.String getRemoteHost()
meth public java.lang.String getWorkingDir()
meth public void applySettings(org.netbeans.lib.profiler.ProfilerEngineSettings)
meth public void load(java.util.Map)
meth public void setJVMArgs(java.lang.String)
meth public void setJavaExecutable(java.lang.String)
meth public void setJavaVersionString(java.lang.String)
meth public void setMainArgs(java.lang.String)
meth public void setMainClass(java.lang.String)
meth public void setMainClassPath(java.lang.String)
meth public void setPortNo(int)
meth public void setRemoteHost(java.lang.String)
meth public void setSystemArchitecture(int)
meth public void setWorkingDir(java.lang.String)
meth public void store(java.util.Map)
supr java.lang.Object
hfds INCORRECT_ARCH_MSG,INCORRECT_PORT_MSG,architecture,bundle,javaExecutable,javaVersionString,jvmArgs,mainArgs,mainClass,mainClassPath,portNo,remoteHost,workingDir

CLSS public org.netbeans.lib.profiler.common.event.ProfilingStateAdapter
cons public init()
intf org.netbeans.lib.profiler.common.event.ProfilingStateListener
meth public void instrumentationChanged(int,int)
meth public void lockContentionMonitoringChanged()
meth public void profilingStateChanged(org.netbeans.lib.profiler.common.event.ProfilingStateEvent)
meth public void serverStateChanged(int,int)
meth public void threadsMonitoringChanged()
supr java.lang.Object

CLSS public final org.netbeans.lib.profiler.common.event.ProfilingStateEvent
cons public init(int,int,org.netbeans.lib.profiler.common.Profiler)
meth public int getNewState()
meth public int getOldState()
meth public org.netbeans.lib.profiler.common.Profiler getSource()
supr java.lang.Object
hfds newState,oldState,source

CLSS public abstract interface org.netbeans.lib.profiler.common.event.ProfilingStateListener
meth public abstract void instrumentationChanged(int,int)
meth public abstract void lockContentionMonitoringChanged()
meth public abstract void profilingStateChanged(org.netbeans.lib.profiler.common.event.ProfilingStateEvent)
meth public abstract void serverStateChanged(int,int)
meth public abstract void threadsMonitoringChanged()

CLSS public abstract org.netbeans.lib.profiler.common.event.SimpleProfilingStateAdapter
cons public init()
intf org.netbeans.lib.profiler.common.event.ProfilingStateListener
meth protected abstract void update()
meth public void instrumentationChanged(int,int)
meth public void lockContentionMonitoringChanged()
meth public void profilingStateChanged(org.netbeans.lib.profiler.common.event.ProfilingStateEvent)
meth public void serverStateChanged(int,int)
meth public void threadsMonitoringChanged()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.common.integration.IntegrationUtils
cons public init()
fld public final static java.lang.String FILE_BACKUP_EXTENSION = ".backup"
fld public final static java.lang.String MODIFIED_FOR_PROFILER_STRING
fld public final static java.lang.String ORIGINAL_BACKUP_LOCATION_STRING
fld public final static java.lang.String PLATFORM_JAVA_110_BEYOND
fld public final static java.lang.String PLATFORM_JAVA_50
fld public final static java.lang.String PLATFORM_JAVA_60
fld public final static java.lang.String PLATFORM_JAVA_70
fld public final static java.lang.String PLATFORM_JAVA_80
fld public final static java.lang.String PLATFORM_JAVA_90
fld public final static java.lang.String PLATFORM_JAVA_CVM
fld public final static java.lang.String PLATFORM_LINUX_AMD64_OS
fld public final static java.lang.String PLATFORM_LINUX_ARM_OS
fld public final static java.lang.String PLATFORM_LINUX_ARM_VFP_HFLT_OS
fld public final static java.lang.String PLATFORM_LINUX_CVM
fld public final static java.lang.String PLATFORM_LINUX_OS
fld public final static java.lang.String PLATFORM_MAC_OS
fld public final static java.lang.String PLATFORM_SOLARIS_AMD64_OS
fld public final static java.lang.String PLATFORM_SOLARIS_INTEL_OS
fld public final static java.lang.String PLATFORM_SOLARIS_SPARC64_OS
fld public final static java.lang.String PLATFORM_SOLARIS_SPARC_OS
fld public final static java.lang.String PLATFORM_WINDOWS_AMD64_OS
fld public final static java.lang.String PLATFORM_WINDOWS_CVM
fld public final static java.lang.String PLATFORM_WINDOWS_OS
meth public static boolean backupFile(java.io.File)
meth public static boolean copyFile(java.io.File,java.io.File)
meth public static boolean fileBackupExists(java.io.File)
meth public static boolean isFileModifiedForProfiler(java.io.File)
meth public static boolean isLinuxPlatform(java.lang.String)
meth public static boolean isMacPlatform(java.lang.String)
meth public static boolean isSolarisPlatform(java.lang.String)
meth public static boolean isWindowsPlatform(java.lang.String)
meth public static boolean restoreFile(java.io.File)
meth public static java.lang.String fixLibsDirPath(java.lang.String,java.lang.String)
meth public static java.lang.String fixLibsDirPath(java.lang.String,java.lang.String,boolean)
meth public static java.lang.String getAddProfilerLibrariesToPathString(java.lang.String,java.lang.String,boolean,boolean)
meth public static java.lang.String getAssignEnvVariableValueString(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getBatchExtensionString(java.lang.String)
meth public static java.lang.String getBatchExtensionString(java.lang.String,java.lang.String)
meth public static java.lang.String getCPUReduceOverheadHint()
meth public static java.lang.String getClassPathSeparator(java.lang.String)
meth public static java.lang.String getDirectorySeparator(java.lang.String)
meth public static java.lang.String getEnvVariableReference(java.lang.String,java.lang.String)
meth public static java.lang.String getExportCommandString(java.lang.String)
meth public static java.lang.String getExportEnvVariableValueString(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static java.lang.String getExportVSSetenvNote()
meth public static java.lang.String getJavaPlatformFromJavaVersionString(java.lang.String)
meth public static java.lang.String getJavaPlatformName(java.lang.String)
meth public static java.lang.String getJavaPlatformNativeLibrariesDirectoryName(java.lang.String)
meth public static java.lang.String getLibsDir(java.lang.String,boolean)
meth public static java.lang.String getLineBreak(java.lang.String)
meth public static java.lang.String getLocalJavaPlatform()
meth public static java.lang.String getLocalPlatform(int)
meth public static java.lang.String getManualRemoteStep1(java.lang.String,java.lang.String)
meth public static java.lang.String getManualRemoteStep2(java.lang.String,java.lang.String)
meth public static java.lang.String getNativeLibrariesPath(java.lang.String,java.lang.String,boolean)
meth public static java.lang.String getNativePathEnvVariableString(java.lang.String)
meth public static java.lang.String getOSPlatformNativeLibrariesDirectoryName(java.lang.String,boolean)
meth public static java.lang.String getPlatformByOSAndArch(int,int,java.lang.String,java.lang.String)
meth public static java.lang.String getProfilerAgentCommandLineArgs(java.lang.String,java.lang.String,boolean,int)
meth public static java.lang.String getProfilerAgentCommandLineArgs(java.lang.String,java.lang.String,boolean,int,boolean)
meth public static java.lang.String getProfilerAgentCommandLineArgsWithoutQuotes(java.lang.String,java.lang.String,boolean,int)
meth public static java.lang.String getProfilerAgentCommandLineArgsWithoutQuotes(java.lang.String,java.lang.String,boolean,int,java.lang.String)
meth public static java.lang.String getProfilerAgentLibraryFile(java.lang.String)
meth public static java.lang.String getProfilerModifiedFileHeader(java.lang.String)
meth public static java.lang.String getProfilerModifiedReplaceFileHeader(java.lang.String)
meth public static java.lang.String getRemoteAbsolutePathHint()
meth public static java.lang.String getRemoteCalibrateCommandString(java.lang.String,java.lang.String)
meth public static java.lang.String getRemoteProfileCommandString(java.lang.String,java.lang.String)
meth public static java.lang.String getRemoteProfilerAgentCommandLineArgsWithoutQuotes(java.lang.String,java.lang.String,java.lang.String,int)
meth public static java.lang.String getScriptCommentSign(java.lang.String)
meth public static java.lang.String getSilentScriptCommentSign(java.lang.String)
meth public static java.lang.String getSpacesInPathWarning()
meth public static java.lang.String getTemporaryBinariesLink(java.lang.String)
meth public static java.lang.String getXMLCommendEndSign()
meth public static java.lang.String getXMLCommentStartSign()
supr java.lang.Object
hfds APPLET_STRING,APPLICATION_STRING,BACKUP_CANNOT_DELETE_FILE_MESSAGE,BACKUP_ERROR_COPY_FILE_MESSAGE,BACKUP_ERROR_MESSAGE,BACKUP_FILE_NOT_FOUND_MESSAGE,BINARIES_TMP_EXT,BINARIES_TMP_PREFIX,COPY_CANNOT_DELETE_FILE_MESSAGE,COPY_ERROR_MESSAGE,COPY_FILE_NOT_FOUND_MESSAGE,DATABASE_STRING,EXPORT_SETENV_MESSAGE,HTML_REMOTE_STRING,JDK_110_BEYOND_NAME,JDK_50_NAME,JDK_60_NAME,JDK_70_NAME,JDK_80_NAME,JDK_90_NAME,JDK_CVM_NAME,MANUAL_REMOTE_STEP1_MESSAGE,MANUAL_REMOTE_STEP2_MESSAGE,REDUCE_OVERHEAD_MESSAGE,REMOTE_ABSOLUTE_PATH_HINT,RESTORE_CANNOT_DELETE_FILE_MESSAGE,RESTORE_ERROR_MESSAGE,RESTORE_FILE_NOT_FOUND_MESSAGE,SERVER_STRING,SPACES_IN_PATH_WARNING_MSG,TARGET_STRING,messages

