#Signature file v4.1
#Version 1.132

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract interface javax.swing.tree.TreeNode
meth public abstract boolean getAllowsChildren()
meth public abstract boolean isLeaf()
meth public abstract int getChildCount()
meth public abstract int getIndex(javax.swing.tree.TreeNode)
meth public abstract java.util.Enumeration children()
meth public abstract javax.swing.tree.TreeNode getChildAt(int)
meth public abstract javax.swing.tree.TreeNode getParent()

CLSS public org.netbeans.lib.profiler.ProfilerClient
cons public init(org.netbeans.lib.profiler.ProfilerEngineSettings,org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.client.AppStatusHandler,org.netbeans.lib.profiler.client.AppStatusHandler$ServerCommandHandler)
intf org.netbeans.lib.profiler.global.CommonConstants
meth public boolean cpuResultsExist() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean currentInstrTypeIsMemoryProfiling()
meth public boolean currentInstrTypeIsRecursiveCPUProfiling()
meth public boolean establishConnectionWithServer(int,boolean,java.util.concurrent.atomic.AtomicBoolean)
meth public boolean forceObtainedResultsDump() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean forceObtainedResultsDump(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean forceObtainedResultsDump(boolean,int) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean memoryResultsExist()
meth public boolean startTargetApp(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppFailedToStart,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean takeHeapDump(java.lang.String) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean targetAppIsRunning()
meth public boolean targetJVMIsAlive()
meth public byte[] getCurrentThreadsLivenessStatus()
meth public byte[][] getCachedClassFileBytes(java.lang.String[],int[]) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public int getCurrentAgentId()
meth public int getCurrentInstrType()
meth public int getDefiningClassLoaderId(java.lang.String,int) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public int[] getAllocatedObjectsCountResults() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public java.io.ObjectInputStream getSocketInputStream()
meth public java.lang.String[][] getMethodNamesForJMethodIds(int[]) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public long getInstrProcessingTime()
meth public org.netbeans.lib.profiler.ProfilerEngineSettings getSettings()
meth public org.netbeans.lib.profiler.client.MonitoredData getMonitoredData()
meth public org.netbeans.lib.profiler.global.ProfilingSessionStatus getStatus()
meth public org.netbeans.lib.profiler.marker.Marker getMethodMarker()
meth public org.netbeans.lib.profiler.results.coderegion.CodeRegionResultsSnapshot getCodeRegionProfilingResultsSnapshot() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getCPUProfilingResultsSnapshot() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot$NoDataAvailableException
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getCPUProfilingResultsSnapshot(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot$NoDataAvailableException
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileProvider getFlatProfileProvider()
meth public org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot getJdbcProfilingResultsSnapshot() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot getJdbcProfilingResultsSnapshot(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.memory.HeapHistogram getHeapHistogram() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.memory.MemoryCCTProvider getMemoryCCTProvider()
meth public org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot getMemoryProfilingResultsSnapshot() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot getMemoryProfilingResultsSnapshot(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.threads.ThreadDump takeThreadDump() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.wireprotocol.InternalStatsResponse getInternalStats() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void deinstrumentMemoryProfiledClasses(boolean[]) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void detachFromTargetJVM() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void initiateCPUSampling() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void initiateCodeRegionInstrumentation(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void initiateMemoryProfInstrumentation(int) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void initiateMonitoring() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void initiateRecursiveCPUProfInstrumentation(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated,org.netbeans.lib.profiler.instrumentation.BadLocationException,org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void prepareDetachFromTargetJVM() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void registerCPUCCTProvider(org.netbeans.lib.profiler.results.cpu.CPUCCTProvider)
meth public void registerFlatProfileProvider(org.netbeans.lib.profiler.results.cpu.FlatProfileProvider)
meth public void registerJdbcCCTProvider(org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider)
meth public void registerMemoryCCTProvider(org.netbeans.lib.profiler.results.memory.MemoryCCTProvider)
meth public void removeAllInstrumentation() throws org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void removeAllInstrumentation(boolean) throws org.netbeans.lib.profiler.instrumentation.InstrumentationException
meth public void resetClientData()
meth public void resetProfilerCollectors() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void resumeTargetAppThreads() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void runGC() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void sendSetInstrumentationParamsCmd(boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void setCurrentInstrType(int)
meth public void suspendTargetAppThreads() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void terminateTargetJVM() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
supr java.lang.Object
hfds CANNOT_OPEN_SERVER_TEMPFILE_MSG,CLASS_NOT_FOUND_MSG,CONNECT_VM_MSG,CORRUPTED_TARGET_CALIBRATION_DATA_MSG,ERROR_GETTING_CALIBRATION_DATA_MSG,INCORRECT_AGENT_VERSION_MSG,INSTRUMENTATION_LIMIT_REACHED_MSG,INVALID_CODE_REGION_MSG,MUST_CALIBRATE_FIRST_MSG,MUST_CALIBRATE_FIRST_SHORT_MSG,OUT_OF_MEMORY_MSG,PERFORMING_INSTRUMENTATION_STRING,TARGET_JVM_ERROR_MSG,UNSUPPORTED_JVM_MSG,appStatusHandler,clientSocket,commandOnStartup,cpuCctProvider,currentAgentId,execInSeparateThreadCmd,execInSeparateThreadLock,flatProvider,forceObtainedResultsDumpCalled,forceObtainedResultsDumpLock,handlingEventBufferDump,histogramManager,instrMethodsLimitReported,instrProcessingTime,instrumentationLock,instrumentor,jdbcCctProvider,lastResponse,memCctProvider,responseLock,resultsStart,savedAllocatedObjectsCountResults,separateCmdExecThread,serverClassesInitialized,serverCommandHandler,serverListener,settings,socketIn,socketOut,status,targetVMAlive,terminateOrDetachCommandIssued,wireIO
hcls SeparateCmdExecutionThread,ServerListener

CLSS public abstract interface org.netbeans.lib.profiler.ProfilerClientListener
meth public abstract void instrumentationChanged(int,int)

CLSS public final org.netbeans.lib.profiler.ProfilerEngineSettings
cons public init()
intf java.lang.Cloneable
intf org.netbeans.lib.profiler.global.CommonConstants
meth public boolean getAbsoluteTimerOn()
meth public boolean getDontShowZeroLiveObjAllocPaths()
meth public boolean getExcludeWaitTime()
meth public boolean getInstrumentEmptyMethods()
meth public boolean getInstrumentGetterSetterMethods()
meth public boolean getInstrumentMethodInvoke()
meth public boolean getInstrumentSpawnedThreads()
meth public boolean getRunGCOnGetResultsInMemoryProfiling()
meth public boolean getSeparateConsole()
meth public boolean getSortResultsByThreadCPUTime()
meth public boolean getSuspendTargetApp()
meth public boolean getTargetWindowRemains()
meth public boolean getThreadCPUTimerOn()
meth public boolean isInstrumentArrayAllocation()
meth public boolean isInstrumentObjectInit()
meth public boolean isLockContentionMonitoringEnabled()
meth public boolean isThreadsMonitoringEnabled()
meth public boolean isThreadsSamplingEnabled()
meth public int getAllocStackTraceLimit()
meth public int getAllocTrackEvery()
meth public int getCPUProfilingType()
meth public int getCodeRegionCPUResBufSize()
meth public int getInstrScheme()
meth public int getNProfiledThreadsLimit()
meth public int getPortNo()
meth public int getSamplingFrequency()
meth public int getSamplingInterval()
meth public int getStackDepthLimit()
meth public int getSystemArchitecture()
meth public java.lang.Object clone()
meth public java.lang.String getJFluidRootDirName()
meth public java.lang.String getJVMArgsAsSingleString()
meth public java.lang.String getMainArgsAsSingleString()
meth public java.lang.String getMainClassName()
meth public java.lang.String getMainClassPath()
meth public java.lang.String getRemoteHost()
meth public java.lang.String getTargetJDKVersionString()
meth public java.lang.String getTargetJVMExeFile()
meth public java.lang.String getTargetJVMStartupDirName()
meth public java.lang.String getWorkingDir()
meth public java.lang.String[] getJVMArgs()
meth public java.lang.String[] getMainArgs()
meth public java.lang.String[] getVMClassPaths()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getInstrumentationRootMethods()
meth public org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getRuntimeProfilingPoints()
meth public org.netbeans.lib.profiler.filters.InstrumentationFilter getInstrumentationFilter()
meth public org.netbeans.lib.profiler.marker.Marker getMethodMarker()
meth public void initialize(java.lang.String) throws java.io.IOException
meth public void setAbsoluteTimerOn(boolean)
meth public void setAllocStackTraceLimit(int)
meth public void setAllocTrackEvery(int)
meth public void setCPUProfilingType(int)
meth public void setCodeRegionCPUResBufSize(int)
meth public void setDontShowZeroLiveObjAllocPaths(boolean)
meth public void setExcludeWaitTime(boolean)
meth public void setInstrScheme(int)
meth public void setInstrumentEmptyMethods(boolean)
meth public void setInstrumentGetterSetterMethods(boolean)
meth public void setInstrumentMethodInvoke(boolean)
meth public void setInstrumentObjectInit(boolean)
meth public void setInstrumentSpawnedThreads(boolean)
meth public void setInstrumentationFilter(org.netbeans.lib.profiler.filters.GenericFilter)
meth public void setInstrumentationRootMethods(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[])
meth public void setJVMArgs(java.lang.String)
meth public void setLockContentionMonitoringEnabled(boolean)
meth public void setMainArgs(java.lang.String)
meth public void setMainClass(java.lang.String)
meth public void setMainClassPath(java.lang.String)
meth public void setMethodMarker(org.netbeans.lib.profiler.marker.Marker)
meth public void setNProfiledThreadsLimit(int)
meth public void setPortNo(int)
meth public void setRemoteHost(java.lang.String)
meth public void setRunGCOnGetResultsInMemoryProfiling(boolean)
meth public void setRuntimeProfilingPoints(org.netbeans.lib.profiler.client.RuntimeProfilingPoint[])
meth public void setSamplingFrequency(int)
meth public void setSamplingInterval(int)
meth public void setSeparateConsole(boolean)
meth public void setSortResultsByThreadCPUTime(boolean)
meth public void setStackDepthLimit(int)
meth public void setSuspendTargetApp(boolean)
meth public void setSystemArchitecture(int)
meth public void setTargetJDKVersionString(java.lang.String)
meth public void setTargetJVMExeFile(java.lang.String)
meth public void setTargetJVMStartupDirName(java.lang.String)
meth public void setTargetWindowRemains(boolean)
meth public void setThreadCPUTimerOn(boolean)
meth public void setThreadsMonitoringEnabled(boolean)
meth public void setThreadsSamplingEnabled(boolean)
meth public void setVMClassPaths(java.lang.String,java.lang.String,java.lang.String)
meth public void setWorkingDir(java.lang.String)
supr java.lang.Object
hfds absoluteTimerOn,allocStackTraceLimit,allocTrackEvery,architecture,codeRegionCPUResBufSize,cpuProfilingType,dontShowZeroLiveObjAllocPaths,excludeWaitTime,instrScheme,instrumentEmptyMethods,instrumentGetterSetterMethods,instrumentMethodInvoke,instrumentObjectInit,instrumentSpawnedThreads,instrumentationFilter,instrumentationRootMethods,jFluidRootDirName,jvmArgs,lockContentionMonitoringEnabled,mainArgs,mainClassName,mainClassPath,methodMarker,nProfiledThreadsLimit,portNo,profilingPoints,remoteHost,runGCOnGetResultsInMemoryProfiling,samplingFrequency,samplingInterval,separateConsole,sortResultsByThreadCPUTime,stackDepthLimit,suspendTargetApp,targetJDKVersion,targetJVMExeFile,targetJVMStartupDirName,targetWindowRemains,threadCPUTimerOn,threadsMonitoringEnabled,threadsSamplingEnabled,vmClassPaths,workingDir

CLSS public org.netbeans.lib.profiler.ProfilerLogger
cons public init()
meth public static boolean isDebug()
meth public static java.util.logging.Level getLevel()
meth public static void debug(java.lang.String)
meth public static void info(java.lang.String)
meth public static void log(java.lang.Exception)
meth public static void log(java.lang.String)
meth public static void setLevel(java.util.logging.Level)
meth public static void severe(java.lang.String)
meth public static void warning(java.lang.String)
supr java.lang.Object
hfds DEFAULT_LEVEL,EXCEPTION_LEVEL,INSTANCE,debugFlag

CLSS public abstract interface org.netbeans.lib.profiler.ProfilingEventListener
meth public abstract void attachedToTarget()
meth public abstract void detachedFromTarget()
meth public abstract void targetAppResumed()
meth public abstract void targetAppStarted()
meth public abstract void targetAppStopped()
meth public abstract void targetAppSuspended()
meth public abstract void targetVMTerminated()

CLSS public org.netbeans.lib.profiler.TargetAppRunner
cons public init(org.netbeans.lib.profiler.ProfilerEngineSettings,org.netbeans.lib.profiler.client.AppStatusHandler,org.netbeans.lib.profiler.client.ProfilingPointsProcessor)
intf org.netbeans.lib.profiler.global.CommonConstants
meth public boolean attachToTargetVM()
meth public boolean attachToTargetVMOnStartup()
meth public boolean calibrateInstrumentationCode()
meth public boolean connectToStartedVMAndStartTA()
meth public boolean hasSupportedJDKForHeapDump()
meth public boolean initiateSession(int,boolean)
meth public boolean initiateSession(int,boolean,java.util.concurrent.atomic.AtomicBoolean)
meth public boolean readSavedCalibrationData()
meth public boolean startTargetVM()
meth public boolean targetAppIsRunning()
meth public boolean targetAppSuspended()
meth public boolean targetJVMIsAlive()
meth public java.lang.Process getRunningAppProcess()
meth public java.lang.String getInternalStats() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.ProfilerClient getProfilerClient()
meth public org.netbeans.lib.profiler.ProfilerEngineSettings getProfilerEngineSettings()
meth public org.netbeans.lib.profiler.client.AppStatusHandler getAppStatusHandler()
meth public org.netbeans.lib.profiler.client.ProfilingPointsProcessor getProfilingPointsProcessor()
meth public org.netbeans.lib.profiler.global.ProfilingSessionStatus getProfilingSessionStatus()
meth public static org.netbeans.lib.profiler.TargetAppRunner getDefault()
meth public void addProfilingEventListener(org.netbeans.lib.profiler.ProfilingEventListener)
meth public void detachFromTargetJVM()
meth public void prepareDetachFromTargetJVM()
meth public void removeProfilingEventListener(org.netbeans.lib.profiler.ProfilingEventListener)
meth public void resetTimers() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void resumeTargetAppIfSuspended() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void runGC() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void suspendTargetAppIfRunning() throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void terminateTargetJVM()
supr java.lang.Object
hfds AVG_METHOD_TIME_MSG,BYTECODE_COMM_TIME_MSG,CALIBRATION_ERROR_MSG,CALIBRATION_RESULTS_MSG,CALIBRATION_SUMMARY_DETAILS_MSG,CALIBRATION_SUMMARY_SHORT_MSG,CLASSLOAD_FIRSTINV_COUNT_MSG,CLASSPATH_SETTINGS_IGNORED_MSG,CLIENT_BYTECODE_TIME_MSG,CLIENT_DISK_PROCESS_MSG,CLIENT_RESULTS_PROCESS_MSG,DEBUG,EMPTY_IMG_COUNT_MSG,ERROR_STARTING_JVM_MSG,EVENT_ATTACHED,EVENT_DETACHED,EVENT_RESUMED,EVENT_STARTED,EVENT_STOPPED,EVENT_SUSPENDED,EVENT_TERMINATED,FAILED_ESTABLISH_CONN_MSG,FAILED_START_APP_CAUSE_MSG,INJ_INSTR_TIME_MSG,INSTR_METHODS_COUNT_MSG,INTERNAL_PROBLEM_STRING,INTERNAL_STATISTICS_ONLY_MSG,JVM_TERMINATED_NOTRESPOND_STRING,MAX_METHOD_TIME_MSG,MIN_METHOD_TIME_MSG,NON_EMPTY_IMG_COUNT_MSG,PERFORMING_CALIBRATION_MSG,SINGLE_IMG_COUNT_MSG,TOTAL_INSTR_HOTSWAP_TIME_MSG,TOTAL_RUN_TIME_MSG,UNEXPECTED_PROBLEM_STARTING_APP_MSG,appStatusHandler,defaultTAR,listeners,profilerClient,profilingPointProcessor,runningAppProcess,settings,status,targetAppIsSuspended

CLSS public org.netbeans.lib.profiler.classfile.BaseClassInfo
cons public init(java.lang.String,int)
fld protected int classLoaderId
fld protected java.lang.String name
fld protected java.lang.String nameAndLoader
meth public int getInstrClassId()
meth public int getLoaderId()
meth public java.lang.String getName()
meth public java.lang.String getNameAndLoader()
meth public java.lang.String toString()
meth public void setInstrClassId(int)
meth public void setLoaderId(int)
supr java.lang.Object
hfds instrClassId

CLSS public org.netbeans.lib.profiler.classfile.ClassFileCache
supr java.lang.Object
hfds capacity,classFileBytes,classNameAndLocation,classPath,defaultClassFileCache,lastTimeUsed,preloadLoaderIds,preloadNames,size,sizeLimit,timeCounter,vmSuppliedClassCache

CLSS public org.netbeans.lib.profiler.classfile.ClassFileParser
cons public init()
innr public static ClassFileReadException
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth public void parseClassFile(byte[],org.netbeans.lib.profiler.classfile.ClassInfo) throws org.netbeans.lib.profiler.classfile.ClassFileParser$ClassFileReadException
supr java.lang.Object
hfds classBuf,classInfo,cpObjectCache,cpOffsets,cpTags,curBufPos
hcls ClassFileReadRuntimeException

CLSS public static org.netbeans.lib.profiler.classfile.ClassFileParser$ClassFileReadException
 outer org.netbeans.lib.profiler.classfile.ClassFileParser
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds e

CLSS public abstract org.netbeans.lib.profiler.classfile.ClassInfo
cons protected init(java.lang.String,int)
innr public StackMapTables
innr public static LineNumberTables
innr public static LocalVariableTables
innr public static LocalVariableTypeTables
intf org.netbeans.lib.profiler.global.CommonConstants
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth protected abstract byte[] getClassFileBytes() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected static java.lang.String getPackageName(java.lang.String)
meth public boolean containsMethod(java.lang.String,java.lang.String)
meth public boolean isAbstract()
meth public boolean isInterface()
meth public boolean isMethodAbstract(int)
meth public boolean isMethodFinal(int)
meth public boolean isMethodNative(int)
meth public boolean isMethodPrivate(int)
meth public boolean isMethodProtected(int)
meth public boolean isMethodPublic(int)
meth public boolean isMethodStatic(int)
meth public byte[] getMethodBytecode(int)
meth public byte[] getMethodInfo(int)
meth public int bciForMethodAndLineNo(int,int)
meth public int checkIfAtGoTo(int,int)
meth public int getCPIndexOfClass(java.lang.String)
meth public int getExceptionTableCount(int)
meth public int getExceptionTableStartOffsetInMethodInfo(int)
meth public int getLocalVariableTableStartOffsetInMethodInfo(int)
meth public int getLocalVariableTypeTableStartOffsetInMethodInfo(int)
meth public int getMajorVersion()
meth public int getMethodBytecodeOffsetInMethodInfo(int)
meth public int getMethodBytecodesLength(int)
meth public int getMethodIndex(java.lang.String,java.lang.String)
meth public int getMethodInfoLength(int)
meth public int getOrigAttrsStartOfs()
meth public int getOrigCPoolCount()
meth public int getOrigCPoolStartOfs()
meth public int getOrigFieldsStartOfs()
meth public int getOrigIntermediateDataStartOfs()
meth public int getOrigMethodsStartOfs()
meth public int getStackMapTableStartOffsetInMethodInfo(int)
meth public int lineNoForMethodAndBci(int,int)
meth public int overridesVirtualMethod(org.netbeans.lib.profiler.classfile.ClassInfo,int)
meth public int[] getMinAndMaxLinesForMethod(int)
meth public int[] methodIdxAndBestBCIForLineNo(int)
meth public java.lang.String getMethodName(int)
meth public java.lang.String getMethodSignature(int)
meth public java.lang.String getRefClassName(int)
meth public java.lang.String getSuperclassName()
meth public java.lang.String[] getInterfaceNames()
meth public java.lang.String[] getMethodNames()
meth public java.lang.String[] getMethodSignatures()
meth public java.lang.String[] getNestedClassNames()
meth public java.lang.String[] getRefMethodsClassNameAndSig(int)
meth public org.netbeans.lib.profiler.classfile.ClassInfo$LineNumberTables getLineNumberTables()
meth public org.netbeans.lib.profiler.classfile.ClassInfo$LocalVariableTables getLocalVariableTables()
meth public org.netbeans.lib.profiler.classfile.ClassInfo$LocalVariableTypeTables getLocalVariableTypeTables()
meth public org.netbeans.lib.profiler.classfile.ClassInfo$StackMapTables getStackMapTables()
meth public static int findPreviousBCI(byte[],int)
meth public void resetTables()
supr org.netbeans.lib.profiler.classfile.BaseClassInfo
hfds accessFlags,attrsStartOfs,classIndex,cpoolRefsToClassIdx,cpoolRefsToClassName,cpoolRefsToMethodClassNameAndSig,cpoolRefsToMethodIdx,cpoolStartOfs,exceptionTableStartOffsets,fieldsStartOfs,interfaces,intermediateDataStartOfs,lineNumberTables,lineNumberTablesLengths,lineNumberTablesOffsets,localVariableTables,localVariableTablesLengths,localVariableTablesOffsets,localVariableTypeTables,localVariableTypeTablesLengths,localVariableTypeTablesOffsets,localVaribaleTableCPindex,localVaribaleTypeTableCPindex,majorVersion,methodAccessFlags,methodBytecodesLengths,methodBytecodesOffsets,methodInfoLengths,methodInfoOffsets,methodNames,methodSignatures,methodsStartOfs,nestedClassNames,origCPoolCount,packageName,stackMapTableCPindex,stackMapTables,stackMapTablesLengths,stackMapTablesOffsets,superName
hcls FullStackMapFrame,StackMapFrame

CLSS public static org.netbeans.lib.profiler.classfile.ClassInfo$LineNumberTables
 outer org.netbeans.lib.profiler.classfile.ClassInfo
meth public char[][] getStartPCs()
supr java.lang.Object
hfds hasTable,lineNumbers,startPCs

CLSS public static org.netbeans.lib.profiler.classfile.ClassInfo$LocalVariableTables
 outer org.netbeans.lib.profiler.classfile.ClassInfo
fld public final static int ATTR_SIZE = 10
meth public boolean hasTable()
meth public void updateTable(int,int,int)
meth public void writeTable(byte[],int,int)
supr java.lang.Object
hfds hasTable,lengths,startPCs

CLSS public static org.netbeans.lib.profiler.classfile.ClassInfo$LocalVariableTypeTables
 outer org.netbeans.lib.profiler.classfile.ClassInfo
supr org.netbeans.lib.profiler.classfile.ClassInfo$LocalVariableTables

CLSS public org.netbeans.lib.profiler.classfile.ClassInfo$StackMapTables
 outer org.netbeans.lib.profiler.classfile.ClassInfo
meth public boolean hasTable()
meth public byte[] getAttributeHeader(int)
meth public byte[] writeTable(int)
meth public int getNumberOfFrames(int)
meth public void updateTable(int,int,int,boolean,boolean)
supr java.lang.Object
hfds frames,framesBytes,hasTable

CLSS public org.netbeans.lib.profiler.classfile.ClassLoaderTable
cons public init()
meth public static int getParentLoader(int)
meth public static void addChildAndParent(int[])
meth public static void initTable(int[])
supr java.lang.Object
hfds DEBUG,parentLoaderIds

CLSS public org.netbeans.lib.profiler.classfile.ClassPath
cons public init(java.lang.String,boolean)
meth public java.lang.String getLocationForClass(java.lang.String)
meth public java.lang.String toString()
meth public java.util.zip.ZipFile getZipFileForName(java.lang.String) throws java.io.IOException
meth public org.netbeans.lib.profiler.classfile.DynamicClassInfo getClassInfoForClass(java.lang.String,int) throws java.io.IOException
meth public void close()
supr java.lang.Object
hfds isCP,paths,zipFileNameToFile
hcls Dir,JarLRUCache,PathEntry,Zip

CLSS public abstract org.netbeans.lib.profiler.classfile.ClassRepository
cons public init()
innr public static CodeRegionBCI
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static java.util.Enumeration getClassEnumerationWithAllVersions()
meth public static java.util.List getAllClassVersions(java.lang.String)
meth public static java.util.List getClassesOnClasspath(java.util.List)
meth public static org.netbeans.lib.profiler.classfile.BaseClassInfo lookupClassOrCreatePlaceholder(java.lang.String,int)
meth public static org.netbeans.lib.profiler.classfile.BaseClassInfo lookupLoadedClass(java.lang.String,int,boolean)
meth public static org.netbeans.lib.profiler.classfile.BaseClassInfo lookupSpecialClass(java.lang.String)
meth public static org.netbeans.lib.profiler.classfile.ClassPath getClassPath()
meth public static org.netbeans.lib.profiler.classfile.ClassRepository$CodeRegionBCI getMethodForSourceRegion(org.netbeans.lib.profiler.classfile.ClassInfo,int,int) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.instrumentation.BadLocationException
meth public static org.netbeans.lib.profiler.classfile.ClassRepository$CodeRegionBCI getMethodMinAndMaxBCI(org.netbeans.lib.profiler.classfile.ClassInfo,java.lang.String,java.lang.String)
meth public static org.netbeans.lib.profiler.classfile.DynamicClassInfo lookupClass(java.lang.String,int) throws java.io.IOException
meth public static void addClassInfo(org.netbeans.lib.profiler.classfile.BaseClassInfo)
meth public static void addVMSuppliedClassFile(java.lang.String,int,byte[])
meth public static void addVMSuppliedClassFile(java.lang.String,int,byte[],java.lang.String,java.lang.String[])
meth public static void cleanup()
meth public static void clearCache()
meth public static void initClassPaths(java.lang.String,java.lang.String[])
supr java.lang.Object
hfds LOCATION_VMSUPPLIED,classPath,classes,definingClassLoaderMap,notFoundClasses

CLSS public static org.netbeans.lib.profiler.classfile.ClassRepository$CodeRegionBCI
 outer org.netbeans.lib.profiler.classfile.ClassRepository
cons public init(java.lang.String,java.lang.String,java.lang.String,int,int)
fld public int bci0
fld public int bci1
fld public java.lang.String className
fld public java.lang.String methodName
fld public java.lang.String methodSignature
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.classfile.DynamicClassInfo
cons public init(java.lang.String,int,java.lang.String) throws java.io.IOException
meth public boolean getAllMethodsMarkers()
meth public boolean getAllMethodsRoots()
meth public boolean hasInstrumentedMethods()
meth public boolean hasMethodReachable()
meth public boolean hasUninstrumentedMarkerMethods()
meth public boolean hasUninstrumentedRootMethods()
meth public boolean implementsInterface(java.lang.String)
meth public boolean isLoaded()
meth public boolean isMethodInstrumented(int)
meth public boolean isMethodLeaf(int)
meth public boolean isMethodMarker(int)
meth public boolean isMethodReachable(int)
meth public boolean isMethodRoot(int)
meth public boolean isMethodScanned(int)
meth public boolean isMethodSpecial(int)
meth public boolean isMethodUnscannable(int)
meth public boolean isMethodVirtual(int)
meth public boolean isServletDoMethodScanned()
meth public boolean isSubclassOf(java.lang.String)
meth public byte[] getClassFileBytes() throws java.io.IOException
meth public byte[] getMethodBytecode(int)
meth public byte[] getMethodInfo(int)
meth public byte[] getOrigMethodInfo(int)
meth public char getInstrMethodId(int)
meth public int getBaseCPoolCount(int)
meth public int getBaseCPoolCountLen()
meth public int getCurrentCPoolCount()
meth public int getExceptionTableStartOffsetInMethodInfo(int)
meth public int getLocalVariableTableStartOffsetInMethodInfo(int)
meth public int getLocalVariableTypeTableStartOffsetInMethodInfo(int)
meth public int getMethodBytecodesLength(int)
meth public int getMethodInfoLength(int)
meth public int getOrigMethodInfoLength(int)
meth public int getStackMapTableStartOffsetInMethodInfo(int)
meth public java.lang.String getClassFileLocation()
meth public java.util.ArrayList getSubclasses()
meth public org.netbeans.lib.profiler.classfile.DynamicClassInfo getSuperClass()
meth public org.netbeans.lib.profiler.classfile.DynamicClassInfo[] getSuperInterfaces()
meth public void addGlobalCatchStackMapTableEntry(int,int)
meth public void addSubclass(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth public void preloadBytecode()
meth public void resetTables()
meth public void saveMethodInfo(int,byte[])
meth public void setAllMethodsMarkers()
meth public void setAllMethodsRoots()
meth public void setBaseCPoolCount(int,int)
meth public void setCurrentCPoolCount(int)
meth public void setHasUninstrumentedMarkerMethods(boolean)
meth public void setHasUninstrumentedRootMethods(boolean)
meth public void setInstrMethodId(int,int)
meth public void setInterface()
meth public void setLoaded(boolean)
meth public void setMethodInstrumented(int)
meth public void setMethodLeaf(int)
meth public void setMethodMarker(int)
meth public void setMethodReachable(int)
meth public void setMethodRoot(int)
meth public void setMethodScanned(int)
meth public void setMethodSpecial(int)
meth public void setMethodUnscannable(int)
meth public void setMethodVirtual(int)
meth public void setServletDoMethodScanned()
meth public void setSuperClass(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth public void setSuperInterface(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth public void unsetMethodInstrumented(int)
meth public void unsetMethodSpecial(int)
supr org.netbeans.lib.profiler.classfile.ClassInfo
hfds allMethodsMarkers,allMethodsRoots,baseCPoolCount,classFileLocation,currentCPoolCount,hasMethodReachable,hasUninstrumentedMarkerMethods,hasUninstrumentedRootMethods,instrMethodIds,interfacesDCI,isLoaded,java_lang_ThowableCPIndex,methodScanStatus,modifiedAndSavedMethodInfos,modifiedMethodBytecodesLength,modifiledLocalVariableTableOffsets,modifiledLocalVariableTypeTableOffsets,modifiledStackMapTableOffsets,nInstrumentedMethods,servletDoMethodScanned,subclasses,superClass

CLSS public org.netbeans.lib.profiler.classfile.LazyDynamicClassInfo
cons public init(java.lang.String,int,java.lang.String,java.lang.String,java.lang.String[]) throws java.io.IOException
meth public boolean isInterface()
meth public int getMethodIndex(java.lang.String,java.lang.String)
meth public java.lang.String[] getMethodNames()
meth public void preloadBytecode()
meth public void setInterface()
supr org.netbeans.lib.profiler.classfile.DynamicClassInfo
hfds isInitilaized,isInterface

CLSS public org.netbeans.lib.profiler.classfile.PlaceholderClassInfo
cons public init(java.lang.String,int)
meth public void transferDataIntoRealClass(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
supr org.netbeans.lib.profiler.classfile.BaseClassInfo

CLSS public org.netbeans.lib.profiler.classfile.SameNameClassGroup
cons public init()
meth public java.util.List getAll()
meth public org.netbeans.lib.profiler.classfile.BaseClassInfo findCompatibleClass(int)
meth public static org.netbeans.lib.profiler.classfile.BaseClassInfo checkForCompatibility(org.netbeans.lib.profiler.classfile.BaseClassInfo,int)
meth public void add(org.netbeans.lib.profiler.classfile.BaseClassInfo)
meth public void replace(org.netbeans.lib.profiler.classfile.BaseClassInfo,org.netbeans.lib.profiler.classfile.BaseClassInfo)
supr java.lang.Object
hfds classes

CLSS public abstract interface org.netbeans.lib.profiler.client.AppStatusHandler
innr public abstract interface static AsyncDialog
innr public abstract interface static ServerCommandHandler
meth public abstract boolean confirmWaitForConnectionReply()
meth public abstract org.netbeans.lib.profiler.client.AppStatusHandler$AsyncDialog getAsyncDialogInstance(java.lang.String,boolean,java.lang.Runnable)
meth public abstract void displayError(java.lang.String)
meth public abstract void displayErrorAndWaitForConfirm(java.lang.String)
meth public abstract void displayErrorWithDetailsAndWaitForConfirm(java.lang.String,java.lang.String)
meth public abstract void displayNotification(java.lang.String)
meth public abstract void displayNotificationAndWaitForConfirm(java.lang.String)
meth public abstract void displayNotificationWithDetailsAndWaitForConfirm(java.lang.String,java.lang.String)
meth public abstract void displayWarning(java.lang.String)
meth public abstract void displayWarningAndWaitForConfirm(java.lang.String)
meth public abstract void handleShutdown()
meth public abstract void pauseLiveUpdates()
meth public abstract void resultsAvailable()
meth public abstract void resumeLiveUpdates()
meth public abstract void takeSnapshot()

CLSS public abstract interface static org.netbeans.lib.profiler.client.AppStatusHandler$AsyncDialog
 outer org.netbeans.lib.profiler.client.AppStatusHandler
meth public abstract void close()
meth public abstract void display()

CLSS public abstract interface static org.netbeans.lib.profiler.client.AppStatusHandler$ServerCommandHandler
 outer org.netbeans.lib.profiler.client.AppStatusHandler
meth public abstract void handleServerCommand(org.netbeans.lib.profiler.wireprotocol.Command)

CLSS public org.netbeans.lib.profiler.client.ClientUtils
cons public init()
fld public final static java.lang.String LINES_PREFIX = "[lines]"
innr public static SourceCodeSelection
innr public static TargetAppFailedToStart
innr public static TargetAppOrVMTerminated
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static java.lang.String formatClassName(java.lang.String)
meth public static java.lang.String parseClassName(java.lang.String,boolean)
meth public static java.lang.String selectionToString(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public static org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection stringToSelection(java.lang.String)
supr java.lang.Object
hfds classNameFormatter

CLSS public static org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection
 outer org.netbeans.lib.profiler.client.ClientUtils
cons public init(int)
cons public init(java.lang.String,int,int)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.lang.Cloneable
meth public boolean contains(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth public boolean definedViaMethodName()
meth public boolean definedViaSourceLines()
meth public boolean equals(java.lang.Object)
meth public boolean isDefaultPackage()
meth public boolean isInDefaultPackage()
meth public boolean isMarkerMethod()
meth public int getEndLine()
meth public int getStartLine()
meth public int hashCode()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String getClassName()
meth public java.lang.String getMethodName()
meth public java.lang.String getMethodSignature()
meth public java.lang.String toFlattened()
meth public java.lang.String toString()
meth public void setMarkerMethod(boolean)
supr java.lang.Object
hfds P1,P2,P3,className,endLine,isMarkerMethod,methodName,methodSignature,normalizedClassName,startLine

CLSS public static org.netbeans.lib.profiler.client.ClientUtils$TargetAppFailedToStart
 outer org.netbeans.lib.profiler.client.ClientUtils
cons public init(java.lang.String)
meth public java.lang.String getOrigCause()
supr java.lang.Exception
hfds origCause

CLSS public static org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
 outer org.netbeans.lib.profiler.client.ClientUtils
cons public init(int)
cons public init(int,java.lang.String)
fld public final static int APP = 2
fld public final static int VM = 1
meth public boolean isAppTerminated()
meth public boolean isVMTerminated()
meth public java.lang.String getMessage()
supr java.lang.Exception
hfds code

CLSS public org.netbeans.lib.profiler.client.MonitoredData
meth public byte[] getExplicitThreadStates()
meth public byte[][] getThreadStates()
meth public int getNNewThreads()
meth public int getNThreadStates()
meth public int getNThreads()
meth public int getServerProgress()
meth public int getServerState()
meth public int getThreadsDataMode()
meth public int[] getExplicitThreadIds()
meth public int[] getNewThreadIds()
meth public int[] getThreadIds()
meth public java.lang.String[] getNewThreadClassNames()
meth public java.lang.String[] getNewThreadNames()
meth public long getFreeMemory()
meth public long getLastGCPauseInMS()
meth public long getLoadedClassesCount()
meth public long getNSurvivingGenerations()
meth public long getNSystemThreads()
meth public long getNUserThreads()
meth public long getProcessCpuTime()
meth public long getRelativeGCTimeInPerMil()
meth public long getTimestamp()
meth public long getTotalMemory()
meth public long[] getExplicitStateTimestamps()
meth public long[] getGCFinishs()
meth public long[] getGCStarts()
meth public long[] getStateTimestamps()
meth public static org.netbeans.lib.profiler.client.MonitoredData getMonitoredData(org.netbeans.lib.profiler.wireprotocol.MonitoredNumbersResponse)
supr java.lang.Object
hfds exStateTimestamps,exThreadIds,exThreadStates,gcFinishs,gcStarts,generalMNumbers,mode,nNewThreads,nThreadStates,nThreads,newThreadClassNames,newThreadIds,newThreadNames,serverProgress,serverState,stateTimestamps,threadIds,threadStates

CLSS public abstract org.netbeans.lib.profiler.client.ProfilingPointsProcessor
cons public init()
meth public abstract org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getSupportedProfilingPoints()
meth public abstract void init(java.lang.Object)
meth public abstract void profilingPointHit(org.netbeans.lib.profiler.client.RuntimeProfilingPoint$HitEvent)
meth public abstract void timeAdjust(int,long,long)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.client.RuntimeProfilingPoint
cons public init(int,java.lang.String,int,int,java.lang.String,java.lang.String)
cons public init(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
innr public static HitEvent
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public boolean resolve(org.netbeans.lib.profiler.classfile.ClassInfo)
meth public int compareTo(java.lang.Object)
meth public int getBci()
meth public int getId()
meth public int getMethodIdx()
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getServerHandlerClass()
meth public java.lang.String getServerInfo()
meth public java.lang.String toString()
supr java.lang.Object
hfds bci,className,id,line,methodIdx,methodName,methodSignature,offset,serverHandlerClass,serverInfo

CLSS public static org.netbeans.lib.profiler.client.RuntimeProfilingPoint$HitEvent
 outer org.netbeans.lib.profiler.client.RuntimeProfilingPoint
cons public init(int,long,int)
meth public int getId()
meth public int getThreadId()
meth public java.lang.String toString()
meth public long getTimestamp()
supr java.lang.Object
hfds id,threadId,timestamp

CLSS public org.netbeans.lib.profiler.filters.GenericFilter
cons public init()
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.util.Properties,java.lang.String)
cons public init(org.netbeans.lib.profiler.filters.GenericFilter)
fld protected final static int MODE_CONTAINS = 1010
fld protected final static int MODE_ENDS_WITH = 1030
fld protected final static int MODE_EQUALS = 1000
fld protected final static int MODE_STARTS_WITH = 1020
fld public final static int TYPE_EXCLUSIVE = 20
fld public final static int TYPE_INCLUSIVE = 10
fld public final static int TYPE_NONE = 0
innr public final static InvalidFilterIdException
meth protected boolean matches(java.lang.String,java.lang.String,int)
meth protected boolean simplePasses(java.lang.String)
meth protected boolean valuesEquals(java.lang.Object)
meth protected int valuesHashCode(int)
meth protected int[] computeModes(java.lang.String[])
meth protected java.lang.String[] computeValues(java.lang.String)
meth protected void valueChanged()
meth public boolean equals(java.lang.Object)
meth public boolean isAll()
meth public boolean passes(java.lang.String)
meth public final boolean isEmpty()
meth public final int getType()
meth public final int[] getModes()
meth public final java.lang.String getName()
meth public final java.lang.String getValue()
meth public final java.lang.String[] getValues()
meth public final void setName(java.lang.String)
meth public final void setType(int)
meth public final void setValue(java.lang.String)
meth public int hashCode()
meth public java.lang.String toString()
meth public static java.lang.String[] values(java.lang.String)
meth public void copyFrom(org.netbeans.lib.profiler.filters.GenericFilter)
meth public void store(java.util.Properties,java.lang.String)
supr java.lang.Object
hfds PROP_NAME,PROP_TYPE,PROP_VALUE,isAll,isEmpty,modes,name,type,value,values

CLSS public final static org.netbeans.lib.profiler.filters.GenericFilter$InvalidFilterIdException
 outer org.netbeans.lib.profiler.filters.GenericFilter
cons public init(java.lang.String,java.lang.String)
supr java.lang.IllegalArgumentException

CLSS public org.netbeans.lib.profiler.filters.InstrumentationFilter
cons public init()
cons public init(org.netbeans.lib.profiler.filters.GenericFilter)
meth public boolean passes(java.lang.String)
supr org.netbeans.lib.profiler.filters.JavaTypeFilter
hfds fake

CLSS public org.netbeans.lib.profiler.filters.JavaTypeFilter
cons public init()
cons public init(java.lang.String,int)
cons public init(java.util.Properties,java.lang.String)
cons public init(org.netbeans.lib.profiler.filters.GenericFilter)
fld protected final static int MODE_STARTS_WITH_EX = 1025
meth protected boolean matches(java.lang.String,java.lang.String,int)
meth protected int[] computeModes(java.lang.String[])
meth protected java.lang.String[] computeValues(java.lang.String)
meth protected void valueChanged()
meth public final boolean hasArray()
meth public final boolean isAll()
meth public final boolean isExact()
meth public void copyFrom(org.netbeans.lib.profiler.filters.JavaTypeFilter)
supr org.netbeans.lib.profiler.filters.GenericFilter
hfds hasArray,isAll,isExact

CLSS public org.netbeans.lib.profiler.filters.TextFilter
cons public init()
cons public init(java.lang.String,int,boolean)
cons public init(java.util.Properties,java.lang.String)
fld public final static int TYPE_REGEXP = 30
meth protected boolean valuesEquals(java.lang.Object)
meth protected int valuesHashCode(int)
meth protected void handleInvalidFilter(java.lang.String,java.lang.RuntimeException)
meth protected void valueChanged()
meth public boolean isAll()
meth public boolean passes(java.lang.String)
meth public final boolean isCaseSensitive()
meth public final void setCaseSensitive(boolean)
meth public void copyFrom(org.netbeans.lib.profiler.filters.TextFilter)
supr org.netbeans.lib.profiler.filters.GenericFilter
hfds NORMALIZED_NOT_READY,normalizedValues,regexpPatterns

CLSS public org.netbeans.lib.profiler.global.CalibrationDataFileIO
cons public init()
meth public static boolean saveCalibrationData(org.netbeans.lib.profiler.global.ProfilingSessionStatus)
meth public static boolean validateCalibrationInput(java.lang.String,java.lang.String)
meth public static int readSavedCalibrationData(org.netbeans.lib.profiler.global.ProfilingSessionStatus)
meth public static java.lang.String getCalibrationDataFileName(java.lang.String) throws java.io.IOException
meth public static java.lang.String getErrorMessage()
supr java.lang.Object
hfds CALIBRATION_DATA_CORRUPTED_PREFIX,CALIBRATION_FILE_NOT_EXIST_MSG,CALIBRATION_FILE_NOT_READABLE_MSG,ERROR_WRITING_CALIBRATION_FILE_PREFIX,ORIGINAL_MESSAGE_STRING,REEXECUTE_CALIBRATION_MSG,RERUN_CALIBRATION_MSG,SHORTER_THAN_EXPECTED_STRING,errorMessage

CLSS public abstract interface org.netbeans.lib.profiler.global.CommonConstants
fld public final static byte ADJUST_TIME = 5
fld public final static byte BUFFEREVENT_PROFILEPOINT_HIT = 29
fld public final static byte COMPACT_EVENT_FORMAT_BYTE_MASK = -128
fld public final static byte MARKER_ENTRY = 3
fld public final static byte MARKER_ENTRY_PARAMETERS = 35
fld public final static byte MARKER_ENTRY_UNSTAMPED = 18
fld public final static byte MARKER_EXIT = 4
fld public final static byte MARKER_EXIT_UNSTAMPED = 19
fld public final static byte METHOD_ENTRY = 6
fld public final static byte METHOD_ENTRY_COMPACT_BYTE_MASK = -128
fld public final static byte METHOD_ENTRY_MONITOR = 22
fld public final static byte METHOD_ENTRY_PARK = 26
fld public final static byte METHOD_ENTRY_SLEEP = 24
fld public final static byte METHOD_ENTRY_UNSTAMPED = 16
fld public final static byte METHOD_ENTRY_WAIT = 20
fld public final static byte METHOD_EXIT = 7
fld public final static byte METHOD_EXIT_COMPACT_BYTE_MASK = -64
fld public final static byte METHOD_EXIT_MONITOR = 23
fld public final static byte METHOD_EXIT_PARK = 27
fld public final static byte METHOD_EXIT_SLEEP = 25
fld public final static byte METHOD_EXIT_UNSTAMPED = 17
fld public final static byte METHOD_EXIT_WAIT = 21
fld public final static byte NEW_MONITOR = 28
fld public final static byte NEW_THREAD = 11
fld public final static byte OBJ_ALLOC_STACK_TRACE = 12
fld public final static byte OBJ_GC_HAPPENED = 15
fld public final static byte OBJ_LIVENESS_STACK_TRACE = 14
fld public final static byte RESET_COLLECTORS = 10
fld public final static byte ROOT_ENTRY = 1
fld public final static byte ROOT_EXIT = 2
fld public final static byte SERVLET_DO_METHOD = 30
fld public final static byte SET_FOLLOWING_EVENTS_THREAD = 13
fld public final static byte THREADS_RESUMED = 9
fld public final static byte THREADS_SUSPENDED = 8
fld public final static byte THREAD_DUMP_END = 32
fld public final static byte THREAD_DUMP_START = 31
fld public final static byte THREAD_INFO = 34
fld public final static byte THREAD_INFO_IDENTICAL = 33
fld public final static byte THREAD_STATUS_MONITOR = 3
fld public final static byte THREAD_STATUS_PARK = 5
fld public final static byte THREAD_STATUS_RUNNING = 1
fld public final static byte THREAD_STATUS_SLEEPING = 2
fld public final static byte THREAD_STATUS_UNKNOWN = -1
fld public final static byte THREAD_STATUS_WAIT = 4
fld public final static byte THREAD_STATUS_ZOMBIE = 0
fld public final static char COMPACT_EVENT_METHOD_ID_MASK = '\u3fff'
fld public final static char MAX_METHOD_ID_FOR_COMPACT_FORMAT = '\u3fff'
fld public final static char METHOD_ENTRY_COMPACT_MASK = '\u8000'
fld public final static char METHOD_EXIT_COMPACT_MASK = '\uc000'
fld public final static int AGENT_ID_ANY = -1
fld public final static int AGENT_STATE_CONNECTED = 3
fld public final static int AGENT_STATE_DIFFERENT_ID = 4
fld public final static int AGENT_STATE_NOT_RUNNING = 0
fld public final static int AGENT_STATE_OTHER_SESSION_IN_PROGRESS = 5
fld public final static int AGENT_STATE_READY_DIRECT = 2
fld public final static int AGENT_STATE_READY_DYNAMIC = 1
fld public final static int AGENT_VERSION_10_M10 = 2
fld public final static int AGENT_VERSION_10_M9 = 1
fld public final static int AGENT_VERSION_60_BETA1 = 8
fld public final static int AGENT_VERSION_60_M10 = 7
fld public final static int AGENT_VERSION_60_M5 = 3
fld public final static int AGENT_VERSION_60_M6 = 4
fld public final static int AGENT_VERSION_60_M7 = 5
fld public final static int AGENT_VERSION_60_M8 = 6
fld public final static int AGENT_VERSION_610_M2 = 11
fld public final static int AGENT_VERSION_67_BETA = 9
fld public final static int AGENT_VERSION_69 = 10
fld public final static int AGENT_VERSION_71 = 12
fld public final static int AGENT_VERSION_73 = 13
fld public final static int AGENT_VERSION_74 = 14
fld public final static int AGENT_VERSION_80 = 15
fld public final static int AGENT_VERSION_81 = 16
fld public final static int AGENT_VERSION_82 = 17
fld public final static int AGENT_VERSION_90 = 18
fld public final static int ARCH_32 = 32
fld public final static int ARCH_64 = 64
fld public final static int CPU_INSTR_FULL = 0
fld public final static int CPU_INSTR_SAMPLED = 1
fld public final static int CPU_SAMPLED = 2
fld public final static int CURRENT_AGENT_VERSION = 18
fld public final static int EVENT_BUFFER_SIZE_IN_BYTES = 1200000
fld public final static int FILTER_CONTAINS = 20
fld public final static int FILTER_ENDS_WITH = 30
fld public final static int FILTER_EQUALS = 40
fld public final static int FILTER_NONE = 0
fld public final static int FILTER_NOT_CONTAINS = 25
fld public final static int FILTER_REGEXP = 50
fld public final static int FILTER_STARTS_WITH = 10
fld public final static int INJ_CODE_REGION = 8
fld public final static int INJ_MAXNUMBER = 13
fld public final static int INJ_OBJECT_ALLOCATIONS = 9
fld public final static int INJ_OBJECT_LIVENESS = 10
fld public final static int INJ_RECURSIVE_MARKER_METHOD = 2
fld public final static int INJ_RECURSIVE_NORMAL_METHOD = 0
fld public final static int INJ_RECURSIVE_ROOT_METHOD = 1
fld public final static int INJ_RECURSIVE_SAMPLED_MARKER_METHOD = 5
fld public final static int INJ_RECURSIVE_SAMPLED_NORMAL_METHOD = 3
fld public final static int INJ_RECURSIVE_SAMPLED_ROOT_METHOD = 4
fld public final static int INJ_REFLECT_METHOD_INVOKE = 6
fld public final static int INJ_SERVLET_DO_METHOD = 7
fld public final static int INJ_STACKMAP = 11
fld public final static int INJ_THROWABLE = 12
fld public final static int INSTRSCHEME_EAGER = 2
fld public final static int INSTRSCHEME_LAZY = 1
fld public final static int INSTRSCHEME_TOTAL = 3
fld public final static int INSTR_CODE_REGION = 1
fld public final static int INSTR_MAXNUMBER = 7
fld public final static int INSTR_MEMORY_BASE = 5
fld public final static int INSTR_NONE = 0
fld public final static int INSTR_NONE_MEMORY_SAMPLING = 7
fld public final static int INSTR_NONE_SAMPLING = 2
fld public final static int INSTR_OBJECT_ALLOCATIONS = 5
fld public final static int INSTR_OBJECT_LIVENESS = 6
fld public final static int INSTR_RECURSIVE_FULL = 3
fld public final static int INSTR_RECURSIVE_SAMPLED = 4
fld public final static int JDK_110_BEYOND = 8
fld public final static int JDK_15 = 2
fld public final static int JDK_16 = 3
fld public final static int JDK_17 = 4
fld public final static int JDK_18 = 6
fld public final static int JDK_19 = 7
fld public final static int JDK_CVM = 5
fld public final static int JDK_UNSUPPORTED = -1
fld public final static int MODE_THREADS_EXACT = 2
fld public final static int MODE_THREADS_NONE = 0
fld public final static int MODE_THREADS_SAMPLING = 1
fld public final static int SERVER_INITIALIZING = 1
fld public final static int SERVER_INSTRUMENTING = 3
fld public final static int SERVER_PREPARING = 2
fld public final static int SERVER_PROGRESS_INDETERMINATE = -1
fld public final static int SERVER_PROGRESS_WORKUNITS = 100
fld public final static int SERVER_RUNNING = 0
fld public final static int SORTING_COLUMN_DEFAULT = -1
fld public final static java.awt.Color THREAD_STATUS_MONITOR_COLOR
fld public final static java.awt.Color THREAD_STATUS_PARK_COLOR
fld public final static java.awt.Color THREAD_STATUS_RUNNING_COLOR
fld public final static java.awt.Color THREAD_STATUS_SLEEPING_COLOR
fld public final static java.awt.Color THREAD_STATUS_UNKNOWN_COLOR
fld public final static java.awt.Color THREAD_STATUS_WAIT_COLOR
fld public final static java.awt.Color THREAD_STATUS_ZOMBIE_COLOR
fld public final static java.lang.String CALIBRATION_PSEUDO_CLASS_NAME = "____Profiler+Calibration+Run____"
fld public final static java.lang.String ENGINE_WARNING = "*** Profiler engine warning: "
fld public final static java.lang.String INVOKE_METHOD_NAME = "invoke"
fld public final static java.lang.String INVOKE_METHOD_SIGNATURE = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"
fld public final static java.lang.String JAVA_LANG_REFLECT_METHOD_DOTTED_CLASS_NAME = "java.lang.reflect.Method"
fld public final static java.lang.String JAVA_LANG_REFLECT_METHOD_SLASHED_CLASS_NAME = "java/lang/reflect/Method"
fld public final static java.lang.String JDK_110_BEYOND_STRING = "jdk110"
fld public final static java.lang.String JDK_15_STRING = "jdk15"
fld public final static java.lang.String JDK_16_STRING = "jdk16"
fld public final static java.lang.String JDK_17_STRING = "jdk17"
fld public final static java.lang.String JDK_18_STRING = "jdk18"
fld public final static java.lang.String JDK_19_STRING = "jdk19"
fld public final static java.lang.String JDK_CVM_STRING = "cvm"
fld public final static java.lang.String JDK_UNSUPPORTED_STRING = "UNSUPPORTED_JDK"
fld public final static java.lang.String NO_CLASS_NAME = "*NO_CLASS_NAME*"
fld public final static java.lang.String NO_METHOD_NAME = "*NO_METHOD_NAME*"
fld public final static java.lang.String NO_METHOD_SIGNATURE = "*NO_METHOD_SIGNATURE*"
fld public final static java.lang.String OBJECT_SLASHED_CLASS_NAME = "java/lang/Object"
fld public final static java.lang.String PLEASE_REPORT_PROBLEM = "*** Please report this problem to feedback@profiler.netbeans.org"
fld public final static java.lang.String PROFILER_DOTTED_CLASS_PREFIX = "org.netbeans.lib.profiler."
fld public final static java.lang.String PROFILER_SEPARATE_EXEC_THREAD_NAME = "*** JFluid Separate Command Execution Thread"
fld public final static java.lang.String PROFILER_SERVER_SLASHED_CLASS_PREFIX = "org/netbeans/lib/profiler/server/"
fld public final static java.lang.String PROFILER_SERVER_THREAD_NAME = "*** Profiler Agent Communication Thread"
fld public final static java.lang.String PROFILER_SPECIAL_EXEC_THREAD_NAME = "*** Profiler Agent Special Execution Thread"
fld public final static java.lang.String THREAD_STATUS_MONITOR_STRING
fld public final static java.lang.String THREAD_STATUS_PARK_STRING
fld public final static java.lang.String THREAD_STATUS_RUNNING_STRING
fld public final static java.lang.String THREAD_STATUS_SLEEPING_STRING
fld public final static java.lang.String THREAD_STATUS_UNKNOWN_STRING
fld public final static java.lang.String THREAD_STATUS_WAIT_STRING
fld public final static java.lang.String THREAD_STATUS_ZOMBIE_STRING

CLSS public org.netbeans.lib.profiler.global.Platform
cons public init()
fld public final static int OS_AIX = 64
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 2048
fld public final static int OS_OS2 = 1024
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 3064
fld public final static int OS_VMS = 8192
fld public final static int OS_WIN2000 = 4096
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 20487
fld public final static int OS_WINNT = 1
fld public final static int OS_WIN_OTHER = 16384
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static boolean is32bitArchitecture()
meth public static boolean is64bitArchitecture()
meth public static boolean isHpux()
meth public static boolean isLinux()
meth public static boolean isLinux(java.lang.String)
meth public static boolean isLinuxArm()
meth public static boolean isLinuxArmVfpHflt()
meth public static boolean isMac()
meth public static boolean isSolaris()
meth public static boolean isSolarisIntel()
meth public static boolean isSolarisSparc()
meth public static boolean isUnix()
meth public static boolean isWindows()
meth public static boolean isWindows(java.lang.String)
meth public static boolean supportsDynamicAttach(java.lang.String)
meth public static boolean supportsThreadSleepingStateMonitoring(java.lang.String)
meth public static boolean thisVMSupportsThreadSleepingStateMonitoring()
meth public static int getJDKMinorNumber(java.lang.String)
meth public static int getJDKVersionNumber()
meth public static int getOperatingSystem()
meth public static int getOperatingSystem(java.lang.String)
meth public static int getSystemArchitecture()
meth public static int getSystemArchitecture(java.lang.String)
meth public static java.lang.String getAgentNativeLibFullName(java.lang.String,boolean,java.lang.String,int)
meth public static java.lang.String getJDKVersionString()
meth public static java.lang.String getJDKVersionString(java.lang.String)
meth public static java.lang.String getJFluidNativeLibDirName(java.lang.String,java.lang.String,int)
meth public static java.lang.String getJavaVersionString()
meth public static java.lang.String getProfilerUserDir() throws java.io.IOException
supr java.lang.Object
hfds jdkDenoteString,jdkVersion,operatingSystem,sysArch

CLSS public org.netbeans.lib.profiler.global.ProfilingSessionStatus
cons public init()
fld public boolean absoluteTimerOn
fld public boolean canInstrumentConstructor
fld public boolean remoteProfiling
fld public boolean runningInAttachedMode
fld public boolean startProfilingPointsActive
fld public boolean threadCPUTimerOn
fld public double[] methodEntryExitCallTime
fld public double[] methodEntryExitInnerTime
fld public double[] methodEntryExitOuterTime
fld public final static int CODE_REGION_CLASS_IDX = 0
fld public final static int N_TIMER_CONSTANTS = 5
fld public int currentInstrType
fld public int instrEndLine
fld public int instrScheme
fld public int instrStartLine
fld public java.lang.String fullTargetJDKVersionString
fld public java.lang.String instrClassLoaderName
fld public java.lang.String javaCommand
fld public java.lang.String jvmArguments
fld public java.lang.String targetJDKVersionString
fld public java.lang.String targetMachineOSName
fld public long dumpAbsTimeStamp
fld public long maxHeapSize
fld public long startupTimeInCounts
fld public long startupTimeMillis
fld public long[] timerCountsInSecond
fld public org.netbeans.lib.profiler.wireprotocol.InternalStatsResponse savedInternalStats
fld public volatile boolean targetAppRunning
meth public boolean collectingTwoTimeStamps()
meth public boolean[] getInstrMethodInvoked()
meth public int getNInstrClasses()
meth public int getNInstrMethods()
meth public int getStartingMethodId()
meth public int[] getAllocatedInstancesCount()
meth public int[] getClassLoaderIds()
meth public java.lang.String[] getClassNames()
meth public java.lang.String[] getInstrMethodClasses()
meth public java.lang.String[] getInstrMethodNames()
meth public java.lang.String[] getInstrMethodSignatures()
meth public void beginTrans(boolean)
meth public void endTrans()
meth public void resetInstrClassAndMethodInfo()
meth public void setInstrMethodNames(java.lang.String[])
meth public void setInstrMethodSignatures(java.lang.String[])
meth public void setTimerTypes(boolean,boolean)
meth public void updateAllocatedInstancesCountInfoInClient(java.lang.String)
meth public void updateAllocatedInstancesCountInfoInServer(int)
meth public void updateInstrMethodsInfo(int,int,java.lang.String[],int[],int[],java.lang.String[],java.lang.String[],boolean[])
meth public void updateInstrMethodsInfo(java.lang.String,int,java.lang.String,java.lang.String)
supr java.lang.Object
hfds allocatedInstancesCount,classLoaderIds,classNames,instrMethodClasses,instrMethodInvoked,instrMethodNames,instrMethodSignatures,nInstrClasses,nInstrMethods,transaction

CLSS public org.netbeans.lib.profiler.global.TransactionalSupport
cons public init()
meth public boolean beginTrans(boolean,boolean)
meth public void beginTrans(boolean)
meth public void endTrans()
supr java.lang.Object
hfds DEBUG,interruptedFlag,lockRead,lockWrite,lockedExclusively,lockedShared,sharedLockCount,transactionLock

CLSS public abstract interface org.netbeans.lib.profiler.heap.ArrayItemValue
intf org.netbeans.lib.profiler.heap.Value
meth public abstract int getIndex()
meth public abstract org.netbeans.lib.profiler.heap.Instance getInstance()

CLSS public abstract interface org.netbeans.lib.profiler.heap.Field
meth public abstract boolean isStatic()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getDeclaringClass()
meth public abstract org.netbeans.lib.profiler.heap.Type getType()

CLSS public abstract interface org.netbeans.lib.profiler.heap.FieldValue
intf org.netbeans.lib.profiler.heap.Value
meth public abstract java.lang.String getValue()
meth public abstract org.netbeans.lib.profiler.heap.Field getField()

CLSS public abstract interface org.netbeans.lib.profiler.heap.GCRoot
fld public final static java.lang.String DEBUGGER = "debugger"
fld public final static java.lang.String FINALIZING = "finalizing"
fld public final static java.lang.String INTERNED_STRING = "interned string"
fld public final static java.lang.String JAVA_FRAME = "Java frame"
fld public final static java.lang.String JNI_GLOBAL = "JNI global"
fld public final static java.lang.String JNI_LOCAL = "JNI local"
fld public final static java.lang.String JNI_MONITOR = "JNI monitor"
fld public final static java.lang.String MONITOR_USED = "monitor used"
fld public final static java.lang.String NATIVE_STACK = "native stack"
fld public final static java.lang.String REFERENCE_CLEANUP = "reference cleanup"
fld public final static java.lang.String STICKY_CLASS = "sticky class"
fld public final static java.lang.String THREAD_BLOCK = "thread block"
fld public final static java.lang.String THREAD_OBJECT = "thread object"
fld public final static java.lang.String UNKNOWN = "unknown"
fld public final static java.lang.String VM_INTERNAL = "VM internal"
meth public abstract java.lang.String getKind()
meth public abstract org.netbeans.lib.profiler.heap.Instance getInstance()

CLSS public abstract interface org.netbeans.lib.profiler.heap.Heap
meth public abstract boolean isRetainedSizeByClassComputed()
meth public abstract boolean isRetainedSizeComputed()
meth public abstract java.util.Collection getGCRoots()
meth public abstract java.util.Collection getJavaClassesByRegExp(java.lang.String)
meth public abstract java.util.Iterator getAllInstancesIterator()
meth public abstract java.util.List getAllClasses()
meth public abstract java.util.List getBiggestObjectsByRetainedSize(int)
meth public abstract java.util.Properties getSystemProperties()
meth public abstract org.netbeans.lib.profiler.heap.GCRoot getGCRoot(org.netbeans.lib.profiler.heap.Instance)
meth public abstract org.netbeans.lib.profiler.heap.HeapSummary getSummary()
meth public abstract org.netbeans.lib.profiler.heap.Instance getInstanceByID(long)
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getJavaClassByID(long)
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getJavaClassByName(java.lang.String)

CLSS public org.netbeans.lib.profiler.heap.HeapFactory
cons public init()
meth public static org.netbeans.lib.profiler.heap.Heap createHeap(java.io.File) throws java.io.IOException
meth public static org.netbeans.lib.profiler.heap.Heap createHeap(java.io.File,int) throws java.io.IOException
meth public static org.netbeans.lib.profiler.heap.Heap createHeap(java.nio.ByteBuffer,int) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.lib.profiler.heap.HeapProgress
fld public final static int PROGRESS_MAX = 1000
meth public static javax.swing.BoundedRangeModel getProgress()
supr java.lang.Object
hfds listener,progressThreadLocal
hcls ModelInfo

CLSS public abstract interface org.netbeans.lib.profiler.heap.HeapSummary
meth public abstract long getTime()
meth public abstract long getTotalAllocatedBytes()
meth public abstract long getTotalAllocatedInstances()
meth public abstract long getTotalLiveBytes()
meth public abstract long getTotalLiveInstances()

CLSS public abstract interface org.netbeans.lib.profiler.heap.Instance
meth public abstract boolean isGCRoot()
meth public abstract int getInstanceNumber()
meth public abstract java.lang.Object getValueOfField(java.lang.String)
meth public abstract java.util.List getFieldValues()
meth public abstract java.util.List getReferences()
meth public abstract java.util.List getStaticFieldValues()
meth public abstract long getInstanceId()
meth public abstract long getReachableSize()
meth public abstract long getRetainedSize()
meth public abstract long getSize()
meth public abstract org.netbeans.lib.profiler.heap.Instance getNearestGCRootPointer()
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getJavaClass()

CLSS public abstract interface org.netbeans.lib.profiler.heap.JavaClass
intf org.netbeans.lib.profiler.heap.Type
meth public abstract boolean isArray()
meth public abstract int getInstanceSize()
meth public abstract int getInstancesCount()
meth public abstract java.lang.Object getValueOfStaticField(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.util.Collection getSubClasses()
meth public abstract java.util.Iterator getInstancesIterator()
meth public abstract java.util.List getFields()
meth public abstract java.util.List getInstances()
meth public abstract java.util.List getStaticFieldValues()
meth public abstract long getAllInstancesSize()
meth public abstract long getJavaClassId()
meth public abstract long getRetainedSizeByClass()
meth public abstract org.netbeans.lib.profiler.heap.Instance getClassLoader()
meth public abstract org.netbeans.lib.profiler.heap.JavaClass getSuperClass()

CLSS public abstract interface org.netbeans.lib.profiler.heap.JavaFrameGCRoot
intf org.netbeans.lib.profiler.heap.GCRoot
meth public abstract int getFrameNumber()
meth public abstract org.netbeans.lib.profiler.heap.ThreadObjectGCRoot getThreadGCRoot()

CLSS public abstract interface org.netbeans.lib.profiler.heap.ObjectArrayInstance
intf org.netbeans.lib.profiler.heap.Instance
meth public abstract int getLength()
meth public abstract java.util.List getItems()
meth public abstract java.util.List getValues()

CLSS public abstract interface org.netbeans.lib.profiler.heap.ObjectFieldValue
intf org.netbeans.lib.profiler.heap.FieldValue
meth public abstract org.netbeans.lib.profiler.heap.Instance getInstance()

CLSS public abstract interface org.netbeans.lib.profiler.heap.PrimitiveArrayInstance
intf org.netbeans.lib.profiler.heap.Instance
meth public abstract int getLength()
meth public abstract java.util.List getValues()

CLSS public abstract interface org.netbeans.lib.profiler.heap.PrimitiveType
intf org.netbeans.lib.profiler.heap.Type

CLSS public abstract interface org.netbeans.lib.profiler.heap.ThreadObjectGCRoot
intf org.netbeans.lib.profiler.heap.GCRoot
meth public abstract java.lang.StackTraceElement[] getStackTrace()

CLSS public abstract interface org.netbeans.lib.profiler.heap.Type
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.lib.profiler.heap.Value
meth public abstract org.netbeans.lib.profiler.heap.Instance getDefiningInstance()

CLSS public org.netbeans.lib.profiler.instrumentation.BadLocationException
cons public init()
cons public init(int)
cons public init(java.lang.String)
supr java.lang.Exception
hfds CANNOT_FIND_METHOD_CURSOR_MSG,CANNOT_FIND_METHOD_SELECTION_MSG

CLSS public org.netbeans.lib.profiler.instrumentation.CPExtensionsRepository
cons public init()
fld public static int codeRegionContents_CodeRegionEntryMethodIdx
fld public static int codeRegionContents_CodeRegionExitMethodIdx
fld public static int memoryProfContents_ProfilePointHitMethodIdx
fld public static int memoryProfContents_TraceObjAllocMethodIdx
fld public static int miContents_AddParBooleanMethodIdx
fld public static int miContents_AddParByteMethodIdx
fld public static int miContents_AddParCharMethodIdx
fld public static int miContents_AddParDoubleMethodIdx
fld public static int miContents_AddParFloatMethodIdx
fld public static int miContents_AddParIntMethodIdx
fld public static int miContents_AddParLongMethodIdx
fld public static int miContents_AddParObjectMethodIdx
fld public static int miContents_AddParShortMethodIdx
fld public static int miContents_HandleReflectInvokeMethodIdx
fld public static int miContents_HandleServletDoMethodIdx
fld public static int normalContents_MethodEntryMethodIdx
fld public static int normalContents_MethodExitMethodIdx
fld public static int normalContents_ProfilePointHitMethodIdx
fld public static int rootContents_MarkerEntryMethodIdx
fld public static int rootContents_MarkerExitMethodIdx
fld public static int rootContents_MarkerExitParMethodIdx
fld public static int rootContents_RootEntryMethodIdx
intf org.netbeans.lib.profiler.global.CommonConstants
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth public static org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension$PackedCPFragment getStandardCPFragment(int)
supr java.lang.Object
hfds ADD_PARAMETER,BOOLEAN_VOID_SIGNATURE,BYTE_VOID_SIGNATURE,CHAR_VOID_SIGNATURE,CODE_REGION_ENTRY_METHOD_NAME,CODE_REGION_EXIT_METHOD_NAME,DOUBLE_VOID_SIGNATURE,FLOAT_VOID_SIGNATURE,HANDLE_REFLECT_INVOKE_METHOD_NAME,HANDLE_SERVLET_DO_METHOD_NAME,INT_VOID_SIGNATURE,JAVA_LANG_THROWABLE_NAME,LONG_VOID_SIGNATURE,MARKER_ENTRY_METHOD_NAME,MARKER_EXIT_METHOD_NAME,METHOD_ENTRY_METHOD_NAME,METHOD_EXIT_METHOD_NAME,OBJECT_CHAR_VOID_SIGNATURE,OBJECT_VOID_SIGNATURE,PROFILE_POINT_HIT,PROFRUNTIME_CPUCODEREGION_CLASS_NAME,PROFRUNTIME_CPUFULL_CLASS_NAME,PROFRUNTIME_CPUSAMPLED_CLASS_NAME,PROFRUNTIME_CPU_CLASS_NAME,PROFRUNTIME_OBJALLOC_CLASS_NAME,PROFRUNTIME_OBJLIVENESS_CLASS_NAME,REFLECT_METHOD_VOID_SIGNATURE,ROOT_ENTRY_METHOD_NAME,SHORT_VOID_SIGNATURE,STACK_MAP_TABLE_ATTRIBUTE,TRACE_OBJ_ALLOC_METHOD_NAME,VOID_VOID_SIGNATURE,standardCPFragments

CLSS public org.netbeans.lib.profiler.instrumentation.ClassManager
cons protected init(org.netbeans.lib.profiler.global.ProfilingSessionStatus)
fld protected org.netbeans.lib.profiler.global.ProfilingSessionStatus status
intf org.netbeans.lib.profiler.global.CommonConstants
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth protected static org.netbeans.lib.profiler.classfile.BaseClassInfo javaClassForObjectArrayType(java.lang.String)
meth protected static org.netbeans.lib.profiler.classfile.BaseClassInfo javaClassForPrimitiveArrayType(int)
meth protected static org.netbeans.lib.profiler.classfile.BaseClassInfo javaClassOrPlaceholderForName(java.lang.String,int)
meth protected static org.netbeans.lib.profiler.classfile.BaseClassInfo loadedJavaClassOrExistingPlaceholderForName(java.lang.String,int)
meth protected static org.netbeans.lib.profiler.classfile.DynamicClassInfo javaClassForName(java.lang.String,int)
meth protected static org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getRuntimeProfilingPoints(org.netbeans.lib.profiler.client.RuntimeProfilingPoint[],int)
meth protected static org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getRuntimeProfilingPoints(org.netbeans.lib.profiler.client.RuntimeProfilingPoint[],org.netbeans.lib.profiler.classfile.ClassInfo)
meth protected static org.netbeans.lib.profiler.client.RuntimeProfilingPoint[] getRuntimeProfilingPoints(org.netbeans.lib.profiler.client.RuntimeProfilingPoint[],org.netbeans.lib.profiler.classfile.ClassInfo,int)
meth protected static void registerPlaceholder(org.netbeans.lib.profiler.classfile.PlaceholderClassInfo)
meth protected static void resetLoadedClassData()
meth protected static void storeClassFileBytesForCustomLoaderClasses(org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand)
supr java.lang.Object
hfds ByBciComparator,EMPTY_PROFILEPOINT_ARRAY

CLSS public org.netbeans.lib.profiler.instrumentation.ClassRewriter
cons public init()
meth public static byte[] rewriteClassFile(org.netbeans.lib.profiler.classfile.DynamicClassInfo,byte[][],int,byte[])
meth public static void saveToDisk(java.lang.String,byte[])
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.instrumentation.CodeRegionMethodInstrumentor
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
meth protected java.lang.Object[] createInstrumentedMethodPack()
meth public java.lang.Object[] getFollowUpInstrumentCodeRegionResponse(int)
supr org.netbeans.lib.profiler.instrumentation.ClassManager
hfds className,instrClasses,nInstrClasses,sourceCodeSelection

CLSS public org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension
cons protected init()
cons protected init(org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension$PackedCPFragment,int,int)
fld protected byte[] addedCPContents
fld protected int nAddedEntries
innr public static CPEntry
innr public static PackedCPFragment
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth public byte[] getConcatenatedContents(org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension)
meth public byte[] getContents()
meth public int getNEntries()
supr java.lang.Object

CLSS public static org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension$CPEntry
 outer org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension
cons public init(int)
cons public init(java.lang.String)
meth public void setIndex1(int)
meth public void setIndex2(int)
supr java.lang.Object
hfds index1,index2,tag,utf8

CLSS public static org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension$PackedCPFragment
 outer org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension
cons public init(org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension$CPEntry[])
meth public byte[] getRelocatedCPBytes(int,int)
supr java.lang.Object
hfds cpoolBytes,externalIndices,internalIndices,nEntries,tmpBytes

CLSS public org.netbeans.lib.profiler.instrumentation.DynamicConstantPoolExtension
cons protected init()
cons protected init(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,int,int)
fld protected org.netbeans.lib.profiler.classfile.DynamicClassInfo clazz
intf org.netbeans.lib.profiler.global.CommonConstants
meth protected static org.netbeans.lib.profiler.instrumentation.DynamicConstantPoolExtension newDynamicCPExtension(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,int)
meth public static org.netbeans.lib.profiler.instrumentation.DynamicConstantPoolExtension getAllAddedCPFragments(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth public static org.netbeans.lib.profiler.instrumentation.DynamicConstantPoolExtension getCPFragment(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth public static org.netbeans.lib.profiler.instrumentation.DynamicConstantPoolExtension getEmptyCPFragment()
supr org.netbeans.lib.profiler.instrumentation.ConstantPoolExtension
hfds emptyECP

CLSS public abstract org.netbeans.lib.profiler.instrumentation.Injector
cons protected init()
cons protected init(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
fld protected byte[] exceptionTable
fld protected int baseCPoolCount
fld protected int excTableEntryCount
fld protected int maxLocals
fld protected int maxStack
fld protected int origBytecodesLength
fld protected int origExcTableEntryCount
meth protected byte[] createPackedMethodInfo()
meth protected void addExceptionTableEntry(int,int,int,int)
meth protected void addGlobalCatchStackMapTableEntry(int)
meth protected void appendCode(byte[],int)
meth protected void injectCodeAndRewrite(byte[],int,int,boolean)
meth protected void insertProfilingPoints(org.netbeans.lib.profiler.client.RuntimeProfilingPoint[],int)
meth public abstract byte[] instrumentMethod()
supr org.netbeans.lib.profiler.instrumentation.SingleMethodScaner
hfds MAX_SHORT,MIN_SHORT,STACK_INCREMENT,_overwrite,changeTypeIsInjectNewInstr,changes,classChecked,injProfilePointHitCode,injProfilePointHitIDCodeIdx,injProfilePointHitMethodIdx,injectionBindsToFollowingInstruction,reusableExcTable
hcls ChangeItem,ChangeJumpWiden,ChangeSwitchPadding

CLSS public org.netbeans.lib.profiler.instrumentation.InstrumentationException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.netbeans.lib.profiler.instrumentation.InstrumentationFactory
cons public init()
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static byte[] instrumentAsProiflePointHitMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,int,org.netbeans.lib.profiler.client.RuntimeProfilingPoint[])
meth public static byte[] instrumentAsReflectInvokeMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth public static byte[] instrumentAsServletDoMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth public static byte[] instrumentCodeRegion(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,int,int)
meth public static byte[] instrumentForMemoryProfiling(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,boolean[],int,org.netbeans.lib.profiler.client.RuntimeProfilingPoint[],org.netbeans.lib.profiler.filters.InstrumentationFilter,boolean,boolean)
meth public static byte[] instrumentMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int,int,int,int,int,org.netbeans.lib.profiler.client.RuntimeProfilingPoint[])
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.instrumentation.Instrumentor
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ProfilerEngineSettings)
intf org.netbeans.lib.profiler.global.CommonConstants
meth public int getClassId(java.lang.String,int)
meth public java.lang.String[] getRootClassNames()
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupCommand createClearAllInstrumentationCommand()
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupCommand getCommandToUnprofileClasses(boolean[])
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupResponse createFollowUpInstrumentMethodGroupResponse(org.netbeans.lib.profiler.wireprotocol.Command)
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupResponse createInitialInstrumentMethodGroupResponse(org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand) throws java.lang.ClassNotFoundException,org.netbeans.lib.profiler.instrumentation.BadLocationException
meth public void resetPerVMInstanceData()
meth public void setSavedSourceCodeSelection(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[])
meth public void setStatusInfoFromSourceCodeSelection(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[]) throws java.io.IOException,java.lang.ClassNotFoundException,org.netbeans.lib.profiler.instrumentation.BadLocationException
supr java.lang.Object
hfds DEBUG,crms,ms,oms,rootMethods,savedSourceCodeSelection,settings,status

CLSS public abstract interface org.netbeans.lib.profiler.instrumentation.JavaClassConstants
fld public final static int CONSTANT_Class = 7
fld public final static int CONSTANT_ConstantDynamic = 17
fld public final static int CONSTANT_Double = 6
fld public final static int CONSTANT_Fieldref = 9
fld public final static int CONSTANT_Float = 4
fld public final static int CONSTANT_Integer = 3
fld public final static int CONSTANT_InterfaceMethodref = 11
fld public final static int CONSTANT_InvokeDynamic = 18
fld public final static int CONSTANT_Long = 5
fld public final static int CONSTANT_MethodHandle = 15
fld public final static int CONSTANT_MethodType = 16
fld public final static int CONSTANT_Methodref = 10
fld public final static int CONSTANT_Module = 19
fld public final static int CONSTANT_NameAndType = 12
fld public final static int CONSTANT_Package = 20
fld public final static int CONSTANT_String = 8
fld public final static int CONSTANT_Unicode = 2
fld public final static int CONSTANT_Utf8 = 1
fld public final static int CONSTANT_unused13 = 13
fld public final static int CONSTANT_unused14 = 14
fld public final static int JAVA_MAGIC = -889275714
fld public final static int JAVA_MAJOR_VERSION = 55
fld public final static int JAVA_MINOR_VERSION = 0
fld public final static int JAVA_MIN_MAJOR_VERSION = 45
fld public final static int JAVA_MIN_MINOR_VERSION = 3
fld public final static int T_BOOLEAN = 4
fld public final static int T_BYTE = 8
fld public final static int T_CHAR = 5
fld public final static int T_DOUBLE = 7
fld public final static int T_FLOAT = 6
fld public final static int T_INT = 10
fld public final static int T_LONG = 11
fld public final static int T_SHORT = 9
fld public final static int opc_aaload = 50
fld public final static int opc_aastore = 83
fld public final static int opc_aconst_null = 1
fld public final static int opc_aload = 25
fld public final static int opc_aload_0 = 42
fld public final static int opc_aload_1 = 43
fld public final static int opc_aload_2 = 44
fld public final static int opc_aload_3 = 45
fld public final static int opc_anewarray = 189
fld public final static int opc_areturn = 176
fld public final static int opc_arraylength = 190
fld public final static int opc_astore = 58
fld public final static int opc_astore_0 = 75
fld public final static int opc_astore_1 = 76
fld public final static int opc_astore_2 = 77
fld public final static int opc_astore_3 = 78
fld public final static int opc_athrow = 191
fld public final static int opc_baload = 51
fld public final static int opc_bastore = 84
fld public final static int opc_bipush = 16
fld public final static int opc_breakpoint = 202
fld public final static int opc_caload = 52
fld public final static int opc_castore = 85
fld public final static int opc_checkcast = 192
fld public final static int opc_d2f = 144
fld public final static int opc_d2i = 142
fld public final static int opc_d2l = 143
fld public final static int opc_dadd = 99
fld public final static int opc_daload = 49
fld public final static int opc_dastore = 82
fld public final static int opc_dcmpg = 152
fld public final static int opc_dcmpl = 151
fld public final static int opc_dconst_0 = 14
fld public final static int opc_dconst_1 = 15
fld public final static int opc_ddiv = 111
fld public final static int opc_dead = -2
fld public final static int opc_dload = 24
fld public final static int opc_dload_0 = 38
fld public final static int opc_dload_1 = 39
fld public final static int opc_dload_2 = 40
fld public final static int opc_dload_3 = 41
fld public final static int opc_dmul = 107
fld public final static int opc_dneg = 119
fld public final static int opc_drem = 115
fld public final static int opc_dreturn = 175
fld public final static int opc_dstore = 57
fld public final static int opc_dstore_0 = 71
fld public final static int opc_dstore_1 = 72
fld public final static int opc_dstore_2 = 73
fld public final static int opc_dstore_3 = 74
fld public final static int opc_dsub = 103
fld public final static int opc_dup = 89
fld public final static int opc_dup2 = 92
fld public final static int opc_dup2_x1 = 93
fld public final static int opc_dup2_x2 = 94
fld public final static int opc_dup_x1 = 90
fld public final static int opc_dup_x2 = 91
fld public final static int opc_f2d = 141
fld public final static int opc_f2i = 139
fld public final static int opc_f2l = 140
fld public final static int opc_fadd = 98
fld public final static int opc_faload = 48
fld public final static int opc_fastore = 81
fld public final static int opc_fcmpg = 150
fld public final static int opc_fcmpl = 149
fld public final static int opc_fconst_0 = 11
fld public final static int opc_fconst_1 = 12
fld public final static int opc_fconst_2 = 13
fld public final static int opc_fdiv = 110
fld public final static int opc_fload = 23
fld public final static int opc_fload_0 = 34
fld public final static int opc_fload_1 = 35
fld public final static int opc_fload_2 = 36
fld public final static int opc_fload_3 = 37
fld public final static int opc_fmul = 106
fld public final static int opc_fneg = 118
fld public final static int opc_frem = 114
fld public final static int opc_freturn = 174
fld public final static int opc_fstore = 56
fld public final static int opc_fstore_0 = 67
fld public final static int opc_fstore_1 = 68
fld public final static int opc_fstore_2 = 69
fld public final static int opc_fstore_3 = 70
fld public final static int opc_fsub = 102
fld public final static int opc_getfield = 180
fld public final static int opc_getstatic = 178
fld public final static int opc_goto = 167
fld public final static int opc_goto_w = 200
fld public final static int opc_i2b = 145
fld public final static int opc_i2c = 146
fld public final static int opc_i2d = 135
fld public final static int opc_i2f = 134
fld public final static int opc_i2l = 133
fld public final static int opc_i2s = 147
fld public final static int opc_iadd = 96
fld public final static int opc_iaload = 46
fld public final static int opc_iand = 126
fld public final static int opc_iastore = 79
fld public final static int opc_iconst_0 = 3
fld public final static int opc_iconst_1 = 4
fld public final static int opc_iconst_2 = 5
fld public final static int opc_iconst_3 = 6
fld public final static int opc_iconst_4 = 7
fld public final static int opc_iconst_5 = 8
fld public final static int opc_iconst_m1 = 2
fld public final static int opc_idiv = 108
fld public final static int opc_if_acmpeq = 165
fld public final static int opc_if_acmpne = 166
fld public final static int opc_if_icmpeq = 159
fld public final static int opc_if_icmpge = 162
fld public final static int opc_if_icmpgt = 163
fld public final static int opc_if_icmple = 164
fld public final static int opc_if_icmplt = 161
fld public final static int opc_if_icmpne = 160
fld public final static int opc_ifeq = 153
fld public final static int opc_ifge = 156
fld public final static int opc_ifgt = 157
fld public final static int opc_ifle = 158
fld public final static int opc_iflt = 155
fld public final static int opc_ifne = 154
fld public final static int opc_ifnonnull = 199
fld public final static int opc_ifnull = 198
fld public final static int opc_iinc = 132
fld public final static int opc_iload = 21
fld public final static int opc_iload_0 = 26
fld public final static int opc_iload_1 = 27
fld public final static int opc_iload_2 = 28
fld public final static int opc_iload_3 = 29
fld public final static int opc_imul = 104
fld public final static int opc_ineg = 116
fld public final static int opc_instanceof = 193
fld public final static int opc_invokedynamic = 186
fld public final static int opc_invokeinterface = 185
fld public final static int opc_invokespecial = 183
fld public final static int opc_invokestatic = 184
fld public final static int opc_invokevirtual = 182
fld public final static int opc_ior = 128
fld public final static int opc_irem = 112
fld public final static int opc_ireturn = 172
fld public final static int opc_ishl = 120
fld public final static int opc_ishr = 122
fld public final static int opc_istore = 54
fld public final static int opc_istore_0 = 59
fld public final static int opc_istore_1 = 60
fld public final static int opc_istore_2 = 61
fld public final static int opc_istore_3 = 62
fld public final static int opc_isub = 100
fld public final static int opc_iushr = 124
fld public final static int opc_ixor = 130
fld public final static int opc_jsr = 168
fld public final static int opc_jsr_w = 201
fld public final static int opc_l2d = 138
fld public final static int opc_l2f = 137
fld public final static int opc_l2i = 136
fld public final static int opc_label = -1
fld public final static int opc_ladd = 97
fld public final static int opc_laload = 47
fld public final static int opc_land = 127
fld public final static int opc_lastore = 80
fld public final static int opc_lcmp = 148
fld public final static int opc_lconst_0 = 9
fld public final static int opc_lconst_1 = 10
fld public final static int opc_ldc = 18
fld public final static int opc_ldc2_w = 20
fld public final static int opc_ldc_w = 19
fld public final static int opc_ldiv = 109
fld public final static int opc_lload = 22
fld public final static int opc_lload_0 = 30
fld public final static int opc_lload_1 = 31
fld public final static int opc_lload_2 = 32
fld public final static int opc_lload_3 = 33
fld public final static int opc_lmul = 105
fld public final static int opc_lneg = 117
fld public final static int opc_lookupswitch = 171
fld public final static int opc_lor = 129
fld public final static int opc_lrem = 113
fld public final static int opc_lreturn = 173
fld public final static int opc_lshl = 121
fld public final static int opc_lshr = 123
fld public final static int opc_lstore = 55
fld public final static int opc_lstore_0 = 63
fld public final static int opc_lstore_1 = 64
fld public final static int opc_lstore_2 = 65
fld public final static int opc_lstore_3 = 66
fld public final static int opc_lsub = 101
fld public final static int opc_lushr = 125
fld public final static int opc_lxor = 131
fld public final static int opc_monitorenter = 194
fld public final static int opc_monitorexit = 195
fld public final static int opc_multianewarray = 197
fld public final static int opc_new = 187
fld public final static int opc_newarray = 188
fld public final static int opc_nop = 0
fld public final static int opc_pop = 87
fld public final static int opc_pop2 = 88
fld public final static int opc_putfield = 181
fld public final static int opc_putstatic = 179
fld public final static int opc_ret = 169
fld public final static int opc_return = 177
fld public final static int opc_saload = 53
fld public final static int opc_sastore = 86
fld public final static int opc_sipush = 17
fld public final static int opc_swap = 95
fld public final static int opc_tableswitch = 170
fld public final static int opc_try = -3
fld public final static int opc_wide = 196
fld public final static int[] opc_length
fld public final static java.lang.String[] PRIMITIVE_ARRAY_TYPE_NAMES

CLSS public abstract org.netbeans.lib.profiler.instrumentation.MemoryProfMethodInstrumentor
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,int)
fld protected int injType
fld protected int instrClassId
fld protected int nInstantiatableClasses
fld protected int nInstrClasses
fld protected int nInstrMethods
fld protected java.lang.String[] instantiatableClasses
fld protected java.util.ArrayList instrClasses
meth protected abstract boolean classNeedsInstrumentation(org.netbeans.lib.profiler.classfile.ClassInfo)
meth protected abstract boolean methodNeedsInstrumentation(org.netbeans.lib.profiler.classfile.ClassInfo,int)
meth protected abstract byte[] instrumentMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected boolean hasNewOpcodes(org.netbeans.lib.profiler.classfile.ClassInfo,int,boolean,boolean,org.netbeans.lib.profiler.filters.InstrumentationFilter)
meth protected boolean methodNeedsRewriting(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected java.lang.Object[] createInstrumentedMethodPack()
meth protected void findAndMarkMethodsToInstrumentInClass(java.lang.String,int)
meth protected void findAndMarkMethodsToInstrumentInClass(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void initInstrumentationPackData()
meth public int getNInstantiatableClasses()
meth public java.lang.Object[] getInitialMethodsToInstrument(org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand)
meth public java.lang.Object[] getMethodsToInstrumentUponClassLoad(java.lang.String,int)
meth public java.lang.String[] getInstantiatableClasses()
supr org.netbeans.lib.profiler.instrumentation.ClassManager
hcls MethodScanerForNewOpcodes

CLSS public org.netbeans.lib.profiler.instrumentation.MiscInstrumentationOps
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus)
meth protected java.lang.Object[] createInstrumentedMethodPack()
meth public java.lang.Object[] getOrigCodeForAllInstrumentedMethods()
supr org.netbeans.lib.profiler.instrumentation.ClassManager
hfds instrClasses,nInstrClasses,nInstrMethods

CLSS public org.netbeans.lib.profiler.instrumentation.ObjLivenessMethodInstrumentor
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ProfilerEngineSettings,boolean)
fld protected boolean[] allUnprofiledClassStatusArray
fld protected final static int SELECTIVE_INSTR_REMOVAL = 2
fld protected final static int STANDARD_INSTRUMENTATION = 1
fld protected int operationCode
meth protected boolean classNeedsInstrumentation(org.netbeans.lib.profiler.classfile.ClassInfo)
meth protected boolean methodNeedsInstrumentation(org.netbeans.lib.profiler.classfile.ClassInfo,int)
meth protected boolean methodNeedsRewriting(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected byte[] instrumentMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected void setAllUnprofiledClassStatusArray(boolean[])
meth public java.lang.Object[] getMethodsToInstrumentUponClassUnprofiling(boolean[])
supr org.netbeans.lib.profiler.instrumentation.MemoryProfMethodInstrumentor
hfds engineSettings,instrArr,instrFilter,instrObjectInit
hcls MethodScanerForBannedInstantiations

CLSS public abstract org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor
fld protected boolean dontInstrumentEmptyMethods
fld protected boolean dontScanGetterSetterMethods
fld protected boolean instrumentSpawnedThreads
fld protected boolean reflectInvokeInstrumented
fld protected byte[] codeBytes
fld protected int markerInjectionType
fld protected int nInstrClasses
fld protected int nInstrMethods
fld protected int normalInjectionType
fld protected int offset
fld protected int rootInjectionType
fld protected java.util.Map instrClasses
fld protected org.netbeans.lib.profiler.filters.InstrumentationFilter instrFilter
innr protected static ReachableMethodPlaceholder
meth protected abstract boolean tryInstrumentSpawnedThreads(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected abstract void findAndMarkOverridingMethodsReachable(org.netbeans.lib.profiler.classfile.DynamicClassInfo,org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected abstract void processInvoke(org.netbeans.lib.profiler.classfile.DynamicClassInfo,boolean,int)
meth protected boolean isLeafMethod(byte[])
meth protected boolean markMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected boolean markMethodMarker(org.netbeans.lib.profiler.classfile.DynamicClassInfo,java.lang.String,java.lang.String)
meth protected boolean markMethodRoot(org.netbeans.lib.profiler.classfile.DynamicClassInfo,java.lang.String,java.lang.String)
meth protected boolean scanBytecode(org.netbeans.lib.profiler.classfile.DynamicClassInfo,byte[])
meth protected final int at(int)
meth protected final int shortAt(int)
meth protected final long intAt(int,int)
meth protected java.lang.Object[] createInstrumentedMethodPack()
meth protected static boolean isEmptyMethod(byte[])
meth protected static boolean isGetterSetterMethod(byte[])
meth protected static boolean rootClassNameIsReal(java.lang.String)
meth protected void addToSubclassList(org.netbeans.lib.profiler.classfile.DynamicClassInfo,org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void initInstrMethodData()
meth protected void markAllMethodsMarker(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void markAllMethodsRoot(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void markClassAndMethodForInstrumentation(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth protected void markProfilingPointForInstrumentation(java.lang.String,java.lang.String,int)
meth protected void markProfilingPonitForInstrumentation(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void scanMethod(org.netbeans.lib.profiler.classfile.DynamicClassInfo,int)
meth public abstract java.lang.Object[] getMethodsToInstrumentUponClassLoad(java.lang.String,int,boolean)
meth public abstract java.lang.Object[] getMethodsToInstrumentUponMethodInvocation(java.lang.String,int,java.lang.String,java.lang.String)
meth public abstract java.lang.Object[] getMethodsToInstrumentUponReflectInvoke(java.lang.String,int,java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.instrumentation.ClassManager
hfds engineSettings,rootMethods

CLSS protected static org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor$ReachableMethodPlaceholder
 outer org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor
fld protected java.util.ArrayList methodNamesAndSigs
meth public void registerReachableMethod(java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.classfile.PlaceholderClassInfo

CLSS public org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor1
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ProfilerEngineSettings)
innr protected ReachableMethodPlaceholder1
meth protected boolean tryInstrumentSpawnedThreads(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void findAndMarkOverridingMethodsReachable(org.netbeans.lib.profiler.classfile.DynamicClassInfo,org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void processInvoke(org.netbeans.lib.profiler.classfile.DynamicClassInfo,boolean,int)
meth public java.lang.Object[] getMethodsToInstrumentUponClassLoad(java.lang.String,int,boolean)
meth public java.lang.Object[] getMethodsToInstrumentUponMethodInvocation(java.lang.String,int,java.lang.String,java.lang.String)
meth public java.lang.Object[] getMethodsToInstrumentUponReflectInvoke(java.lang.String,int,java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor

CLSS protected org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor1$ReachableMethodPlaceholder1
 outer org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor1
cons public init(org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor1,java.lang.String,int)
meth public void processReachableMethods(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
supr org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor$ReachableMethodPlaceholder

CLSS public org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor2
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ProfilerEngineSettings)
innr protected ReachableMethodPlaceholder2
meth protected boolean tryInstrumentSpawnedThreads(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void findAndMarkOverridingMethodsReachable(org.netbeans.lib.profiler.classfile.DynamicClassInfo,org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void processInvoke(org.netbeans.lib.profiler.classfile.DynamicClassInfo,boolean,int)
meth public java.lang.Object[] getMethodsToInstrumentUponClassLoad(java.lang.String,int,boolean)
meth public java.lang.Object[] getMethodsToInstrumentUponMethodInvocation(java.lang.String,int,java.lang.String,java.lang.String)
meth public java.lang.Object[] getMethodsToInstrumentUponReflectInvoke(java.lang.String,int,java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor

CLSS protected org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor2$ReachableMethodPlaceholder2
 outer org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor2
cons public init(org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor2,java.lang.String,int)
meth public void transferDataIntoRealClass(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
supr org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor$ReachableMethodPlaceholder

CLSS public org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor3
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,org.netbeans.lib.profiler.ProfilerEngineSettings)
meth protected boolean tryInstrumentSpawnedThreads(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected boolean tryMainMethodInstrumentation(org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void findAndMarkOverridingMethodsReachable(org.netbeans.lib.profiler.classfile.DynamicClassInfo,org.netbeans.lib.profiler.classfile.DynamicClassInfo)
meth protected void processInvoke(org.netbeans.lib.profiler.classfile.DynamicClassInfo,boolean,int)
meth public java.lang.Object[] getMethodsToInstrumentUponClassLoad(java.lang.String,int,boolean)
meth public java.lang.Object[] getMethodsToInstrumentUponMethodInvocation(java.lang.String,int,java.lang.String,java.lang.String)
meth public java.lang.Object[] getMethodsToInstrumentUponReflectInvoke(java.lang.String,int,java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.instrumentation.RecursiveMethodInstrumentor
hfds mainMethodInstrumented,noExplicitRootsSpecified

CLSS public org.netbeans.lib.profiler.instrumentation.SingleMethodScaner
cons public init()
cons public init(org.netbeans.lib.profiler.classfile.ClassInfo,int)
fld protected byte[] bytecodes
fld protected byte[] origMethodInfo
fld protected int bytecodesLength
fld protected int bytecodesStartIdx
fld protected int methodIdx
fld protected org.netbeans.lib.profiler.classfile.ClassInfo clazz
intf org.netbeans.lib.profiler.instrumentation.JavaClassConstants
meth protected int getByte(int)
meth protected int getInt(int)
meth protected int getU2(int)
meth protected int getU4(int)
meth protected int opcodeLength(int)
meth protected short getShort(int)
meth protected static int align(int)
meth protected static int getU2(byte[],int)
meth protected static int getU4(byte[],int)
meth protected static void putByte(byte[],int,int)
meth protected static void putU2(byte[],int,int)
meth protected static void putU4(byte[],int,int)
meth protected void initBytecodesArray()
meth protected void putInt(int,int)
meth protected void putShort(int,short)
meth protected void putU4(int,int)
meth public void setClassAndMethod(org.netbeans.lib.profiler.classfile.ClassInfo,int)
supr java.lang.Object
hfds reusableBytecodes

CLSS public org.netbeans.lib.profiler.jps.JpsProxy
cons public init()
meth public static org.netbeans.lib.profiler.jps.RunningVM[] getRunningVMs()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.jps.RunningVM
cons public init(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int getPid()
meth public int hashCode()
meth public java.lang.String getMainArgs()
meth public java.lang.String getMainClass()
meth public java.lang.String getVMArgs()
meth public java.lang.String getVMFlags()
meth public java.lang.String toString()
supr java.lang.Object
hfds mainArgs,mainClass,pid,vmArgs,vmFlags

CLSS public org.netbeans.lib.profiler.marker.ClassMarker
cons public init()
intf org.netbeans.lib.profiler.marker.Marker
meth public org.netbeans.lib.profiler.marker.Mark[] getMarks()
meth public org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[] getMappings()
meth public void addClassMark(java.lang.String,org.netbeans.lib.profiler.marker.Mark)
meth public void removeClassMark(java.lang.String)
meth public void resetClassMarks()
supr java.lang.Object
hfds LOGGER,markMap

CLSS public org.netbeans.lib.profiler.marker.CompositeMarker
cons public init()
cons public init(java.util.Set)
intf org.netbeans.lib.profiler.marker.Marker
meth public org.netbeans.lib.profiler.marker.Mark[] getMarks()
meth public org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[] getMappings()
meth public void addMarker(org.netbeans.lib.profiler.marker.Marker)
meth public void addMarkers(java.util.Collection)
meth public void removeMarker(org.netbeans.lib.profiler.marker.Marker)
meth public void removeMarkers(java.util.Collection)
supr java.lang.Object
hfds delegates

CLSS public org.netbeans.lib.profiler.marker.Mark
cons public init()
cons public init(short)
fld public final short id
fld public final static char ID_NONE = '\u0000'
fld public final static org.netbeans.lib.profiler.marker.Mark DEFAULT
fld public final static short DEFAULT_ID = 0
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isDefault()
meth public int hashCode()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public short getId()
supr java.lang.Object
hfds counter

CLSS public abstract interface org.netbeans.lib.profiler.marker.Marker
fld public final static org.netbeans.lib.profiler.marker.Marker DEFAULT
meth public abstract org.netbeans.lib.profiler.marker.Mark[] getMarks()
meth public abstract org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[] getMappings()

CLSS public org.netbeans.lib.profiler.marker.MethodMarker
cons public init()
intf org.netbeans.lib.profiler.marker.Marker
meth public org.netbeans.lib.profiler.marker.Mark[] getMarks()
meth public org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[] getMappings()
meth public void addMethodMark(java.lang.String,java.lang.String,java.lang.String,org.netbeans.lib.profiler.marker.Mark)
meth public void removeMethodMark(java.lang.String,java.lang.String,java.lang.String)
meth public void resetMethodMarks()
supr java.lang.Object
hfds LOGGER,markMap

CLSS public org.netbeans.lib.profiler.marker.PackageMarker
cons public init()
intf org.netbeans.lib.profiler.marker.Marker
meth public org.netbeans.lib.profiler.marker.Mark[] getMarks()
meth public org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[] getMappings()
meth public void addPackageMark(java.lang.String,org.netbeans.lib.profiler.marker.Mark,boolean)
meth public void removePackageMark(java.lang.String)
meth public void resetPackageMarks()
supr java.lang.Object
hfds LOGGER,markMap

CLSS public abstract org.netbeans.lib.profiler.results.AbstractDataFrameProcessor
cons public init()
fld protected final static java.util.logging.Logger LOGGER
fld protected volatile boolean collectingTwoTimeStamps
fld protected volatile org.netbeans.lib.profiler.ProfilerClient client
innr protected abstract interface static ListenerFunctor
intf org.netbeans.lib.profiler.results.DataFrameProcessor
meth protected abstract void doProcessDataFrame(java.nio.ByteBuffer)
meth protected static java.lang.String getString(java.nio.ByteBuffer)
meth protected static long getTimeStamp(java.nio.ByteBuffer)
meth protected void addListener(org.netbeans.lib.profiler.results.ProfilingResultListener)
meth protected void fireProfilingPoint(int,int,long)
meth protected void fireReset()
meth protected void foreachListener(org.netbeans.lib.profiler.results.AbstractDataFrameProcessor$ListenerFunctor)
meth protected void removeListener(org.netbeans.lib.profiler.results.ProfilingResultListener)
meth public boolean hasListeners()
meth public void processDataFrame(byte[])
meth public void removeAllListeners()
meth public void reset()
meth public void shutdown()
meth public void startup(org.netbeans.lib.profiler.ProfilerClient)
supr java.lang.Object
hfds listeners,processorLives

CLSS protected abstract interface static org.netbeans.lib.profiler.results.AbstractDataFrameProcessor$ListenerFunctor
 outer org.netbeans.lib.profiler.results.AbstractDataFrameProcessor
meth public abstract void execute(org.netbeans.lib.profiler.results.ProfilingResultListener)

CLSS public abstract org.netbeans.lib.profiler.results.BaseCallGraphBuilder
cons public init()
fld protected boolean batchNotEmpty
fld protected final java.util.Set cctListeners
fld protected final static java.util.logging.Logger LOGGER
fld protected java.lang.ref.WeakReference clientRef
fld protected java.util.List afterBatchCommands
fld protected org.netbeans.lib.profiler.global.ProfilingSessionStatus status
intf org.netbeans.lib.profiler.results.CCTProvider
intf org.netbeans.lib.profiler.results.ProfilingResultListener
meth protected abstract org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth protected abstract void doBatchStart()
meth protected abstract void doBatchStop()
meth protected abstract void doReset()
meth protected abstract void doShutdown()
meth protected abstract void doStartup(org.netbeans.lib.profiler.ProfilerClient)
meth protected org.netbeans.lib.profiler.ProfilerClient getClient()
meth public void addListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)
meth public void onBatchStart()
meth public void onBatchStop()
meth public void removeAllListeners()
meth public void removeListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)
meth public void reset()
meth public void shutdown()
meth public void startup(org.netbeans.lib.profiler.ProfilerClient)
supr java.lang.Object

CLSS public abstract org.netbeans.lib.profiler.results.CCTNode
cons public init()
innr public abstract interface static AlwaysFirst
innr public abstract interface static AlwaysLast
innr public abstract interface static FixedPosition
intf javax.swing.tree.TreeNode
meth protected void setFilteredNode()
meth public abstract int getIndexOfChild(java.lang.Object)
meth public abstract int getNChildren()
meth public abstract org.netbeans.lib.profiler.results.CCTNode getChild(int)
meth public abstract org.netbeans.lib.profiler.results.CCTNode getParent()
meth public abstract org.netbeans.lib.profiler.results.CCTNode[] getChildren()
meth public boolean getAllowsChildren()
meth public boolean isFiltered()
meth public boolean isLeaf()
meth public int getChildCount()
meth public int getIndex(javax.swing.tree.TreeNode)
meth public java.util.Enumeration<org.netbeans.lib.profiler.results.CCTNode> children()
meth public javax.swing.tree.TreeNode getChildAt(int)
meth public org.netbeans.lib.profiler.results.CCTNode createFilteredNode()
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
supr java.lang.Object
hfds filtered

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTNode$AlwaysFirst
 outer org.netbeans.lib.profiler.results.CCTNode
intf org.netbeans.lib.profiler.results.CCTNode$FixedPosition

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTNode$AlwaysLast
 outer org.netbeans.lib.profiler.results.CCTNode
intf org.netbeans.lib.profiler.results.CCTNode$FixedPosition

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTNode$FixedPosition
 outer org.netbeans.lib.profiler.results.CCTNode

CLSS public abstract interface org.netbeans.lib.profiler.results.CCTProvider
innr public abstract interface static Listener
meth public abstract void addListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)
meth public abstract void removeAllListeners()
meth public abstract void removeListener(org.netbeans.lib.profiler.results.CCTProvider$Listener)

CLSS public abstract interface static org.netbeans.lib.profiler.results.CCTProvider$Listener
 outer org.netbeans.lib.profiler.results.CCTProvider
meth public abstract void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public abstract void cctReset()

CLSS public abstract interface org.netbeans.lib.profiler.results.DataFrameProcessor
meth public abstract boolean hasListeners()
meth public abstract void processDataFrame(byte[])
meth public abstract void reset()
meth public abstract void shutdown()
meth public abstract void startup(org.netbeans.lib.profiler.ProfilerClient)

CLSS public abstract org.netbeans.lib.profiler.results.DataManager
cons public init()
meth protected void fireDataChanged()
meth protected void fireDataReset()
meth public void addDataListener(org.netbeans.lib.profiler.results.DataManagerListener)
meth public void removeDataListener(org.netbeans.lib.profiler.results.DataManagerListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.netbeans.lib.profiler.results.DataManagerListener
meth public abstract void dataChanged()
meth public abstract void dataReset()

CLSS public org.netbeans.lib.profiler.results.EventBufferProcessor
cons public init()
fld protected static boolean bufFileExists
fld protected static java.io.File bufFile
fld protected static java.io.RandomAccessFile raFile
fld protected static java.nio.MappedByteBuffer mapByteBuf
fld protected static java.nio.channels.FileChannel bufFileChannel
fld protected static long dataProcessingTime
fld protected static long startDataProcessingTime
fld protected static org.netbeans.lib.profiler.ProfilerClient profilerClient
fld protected static org.netbeans.lib.profiler.global.ProfilingSessionStatus status
intf org.netbeans.lib.profiler.global.CommonConstants
meth protected static void completeDataProcessing()
meth public static boolean bufFileExists()
meth public static boolean setEventBufferFile(java.lang.String)
meth public static byte[] readDataAndPrepareForProcessing(org.netbeans.lib.profiler.wireprotocol.EventBufferDumpedCommand)
meth public static long getDataProcessingTime()
meth public static void initialize(org.netbeans.lib.profiler.ProfilerClient)
meth public static void removeEventBufferFile()
meth public static void reset()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.EventBufferResultsProvider
intf org.netbeans.lib.profiler.results.ProfilingResultsProvider
meth public static org.netbeans.lib.profiler.results.EventBufferResultsProvider getDefault()
meth public void addDispatcher(org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher)
meth public void dataReady(byte[],int)
meth public void removeDispatcher(org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher)
meth public void shutdown()
meth public void startup(org.netbeans.lib.profiler.ProfilerClient)
supr java.lang.Object
hfds LOGGER,instance,listeners

CLSS public org.netbeans.lib.profiler.results.ExportDataDumper
cons public init(java.io.FileOutputStream)
fld public final static int BUFFER_SIZE = 32000
meth public int getNumExceptions()
meth public java.io.BufferedOutputStream getOutputStream()
meth public java.io.IOException getCaughtException()
meth public void close()
meth public void dumpByte(byte)
meth public void dumpData(java.lang.CharSequence)
meth public void dumpDataAndClose(java.lang.StringBuffer)
supr java.lang.Object
hfds bos,caughtEx,numExceptions,osw

CLSS public final org.netbeans.lib.profiler.results.FilterSortSupport
cons public init()
fld public final static java.lang.String FILTERED_OUT_LBL
innr public final static Configuration
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static boolean passesFilter(java.lang.String,int,java.lang.String)
meth public static boolean passesFilter(org.netbeans.lib.profiler.results.FilterSortSupport$Configuration,java.lang.String)
supr java.lang.Object

CLSS public final static org.netbeans.lib.profiler.results.FilterSortSupport$Configuration
 outer org.netbeans.lib.profiler.results.FilterSortSupport
cons public init()
meth public boolean getSortOrder()
meth public int getFilterType()
meth public int getSortBy()
meth public java.lang.String getFilterString()
meth public void setFilterInfo(java.lang.String,int)
meth public void setSortInfo(int,boolean)
supr java.lang.Object
hfds filterString,filterType,sortBy,sortOrder

CLSS public abstract interface org.netbeans.lib.profiler.results.ProfilingResultListener
meth public abstract void onBatchStart()
meth public abstract void onBatchStop()
meth public abstract void profilingPoint(int,int,long)
meth public abstract void reset()
meth public abstract void shutdown()
meth public abstract void startup(org.netbeans.lib.profiler.ProfilerClient)

CLSS public final org.netbeans.lib.profiler.results.ProfilingResultsDispatcher
cons public init()
intf org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher
meth public static org.netbeans.lib.profiler.results.ProfilingResultsDispatcher getDefault()
meth public void addListener(org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener)
meth public void addListener(org.netbeans.lib.profiler.results.locks.LockProfilingResultListener)
meth public void addListener(org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener)
meth public void dataFrameReceived(byte[],int)
meth public void pause(boolean)
meth public void removeAllListeners()
meth public void removeListener(org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener)
meth public void removeListener(org.netbeans.lib.profiler.results.locks.LockProfilingResultListener)
meth public void removeListener(org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener)
meth public void reset()
meth public void resume()
meth public void shutdown()
meth public void startup(org.netbeans.lib.profiler.ProfilerClient)
supr java.lang.Object
hfds LOGGER,QLengthLowerBound,QLengthUpperBound,cpuDataProcessor,cpuDataProcessorQLength,cpuDataProcessorQLengthLock,cpuSamplingDataProcessor,instance,lockDataProcessor,lockDataProcessorQLength,lockDataProcessorQLengthLock,memDataProcessorQLength,memDataProcessorQLengthLock,memoryDataProcessor,pauseFlag,queueProcessor

CLSS public abstract interface org.netbeans.lib.profiler.results.ProfilingResultsProvider
innr public abstract interface static Dispatcher
meth public abstract void addDispatcher(org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher)
meth public abstract void dataReady(byte[],int)
meth public abstract void removeDispatcher(org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher)
meth public abstract void shutdown()
meth public abstract void startup(org.netbeans.lib.profiler.ProfilerClient)

CLSS public abstract interface static org.netbeans.lib.profiler.results.ProfilingResultsProvider$Dispatcher
 outer org.netbeans.lib.profiler.results.ProfilingResultsProvider
meth public abstract void dataFrameReceived(byte[],int)
meth public abstract void pause(boolean)
meth public abstract void reset()
meth public abstract void resume()
meth public abstract void shutdown()
meth public abstract void startup(org.netbeans.lib.profiler.ProfilerClient)

CLSS public org.netbeans.lib.profiler.results.ResultsSnapshot
cons protected init(long,long)
cons public init()
fld protected final static java.util.logging.Logger LOGGER
fld protected long beginTime
fld protected long timeTaken
meth protected java.lang.String debugLength(java.lang.Object)
meth public java.lang.String toString()
meth public long getBeginTime()
meth public long getTimeTaken()
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr java.lang.Object
hfds SNAPSHOT_VERSION

CLSS public abstract interface org.netbeans.lib.profiler.results.RuntimeCCTNode
meth public abstract org.netbeans.lib.profiler.results.RuntimeCCTNode[] getChildren()

CLSS public final org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor
innr public abstract interface static Plugin
innr public abstract static PluginAdapter
meth public !varargs static void process(org.netbeans.lib.profiler.results.RuntimeCCTNode,org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$Plugin[])
supr java.lang.Object
hfds LOGGER
hcls BackoutItem,Item,SimpleItem

CLSS public abstract interface static org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$Plugin
 outer org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor
meth public abstract void onBackout(org.netbeans.lib.profiler.results.RuntimeCCTNode)
meth public abstract void onNode(org.netbeans.lib.profiler.results.RuntimeCCTNode)
meth public abstract void onStart()
meth public abstract void onStop()

CLSS public abstract static org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$PluginAdapter
 outer org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor
cons public init()
intf org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$Plugin
meth protected void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth protected void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode)
meth protected void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode)
meth protected void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode)
meth protected void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode)
meth protected void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth protected void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode)
meth protected void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode)
meth protected void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode)
meth protected void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode)
meth public final void onBackout(org.netbeans.lib.profiler.results.RuntimeCCTNode)
meth public final void onNode(org.netbeans.lib.profiler.results.RuntimeCCTNode)
meth public void onStart()
meth public void onStop()
supr java.lang.Object

CLSS public final org.netbeans.lib.profiler.results.coderegion.CodeRegionResultsSnapshot
cons public init()
cons public init(long,long,long[],long)
meth public int getInvocations()
meth public java.lang.String toString()
meth public long getTimerCountsInSecond()
meth public long[] getTimes()
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.ResultsSnapshot
hfds CODE_FRAGMENT_MSG,rawData,timerCountsInSecond

CLSS public org.netbeans.lib.profiler.results.cpu.AllThreadsMergedCPUCCTContainer
cons public init(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode[],int)
fld protected int view
meth protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer generateFlatProfile()
meth protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree generateReverseCCT(int)
meth public java.lang.String[] getMethodClassNameAndSig(int)
supr org.netbeans.lib.profiler.results.cpu.CPUCCTContainer
hfds ALL_THREADS_STRING

CLSS public org.netbeans.lib.profiler.results.cpu.CPUCCTClassContainer
cons public init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,org.netbeans.lib.profiler.results.cpu.MethodIdMap,int)
fld protected int view
meth protected int generateClassNodeFromMethodNodes(org.netbeans.lib.profiler.utils.IntVector,int)
meth protected void processChildren(int,int,int,org.netbeans.lib.profiler.utils.IntVector,java.util.Hashtable)
meth public java.lang.String[] getMethodClassNameAndSig(int)
supr org.netbeans.lib.profiler.results.cpu.CPUCCTContainer
hfds childTotalTime0,childTotalTime1,methodIdMap,sourceContainer

CLSS public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer
cons protected init(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)
cons public init(org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode,org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.cpu.MethodInfoMapper,org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld,org.netbeans.lib.profiler.filters.InstrumentationFilter,int,double[],int,java.lang.String)
fld protected boolean collectingTwoTimeStamps
fld protected boolean displayWholeThreadCPUTime
fld protected byte[] compactData
fld protected double timeInInjectedCodeInAbsCounts
fld protected double timeInInjectedCodeInThreadCPUCounts
fld protected final static int CHILD_OFS_SIZE_3 = 3
fld protected final static int CHILD_OFS_SIZE_4 = 4
fld protected final static int OFS_METHODID = 0
fld protected final static int OFS_NCALLS = 2
fld protected final static int OFS_NSUBNODES1 = 16
fld protected final static int OFS_NSUBNODES2 = 26
fld protected final static int OFS_SELFTIME0 = 11
fld protected final static int OFS_SELFTIME1 = 21
fld protected final static int OFS_SUBNODE01 = 18
fld protected final static int OFS_SUBNODE02 = 28
fld protected final static int OFS_TIME0 = 6
fld protected final static int OFS_TIME1 = 16
fld protected int childOfsSize
fld protected int nodeSize
fld protected int threadId
fld protected int[] invPerMethodId
fld protected java.lang.String threadName
fld protected java.util.Set methodsOnStack
fld protected long wholeGraphGrossTimeAbs
fld protected long wholeGraphGrossTimeThreadCPU
fld protected long wholeGraphNetTime0
fld protected long wholeGraphNetTime1
fld protected long wholeGraphPureTimeAbs
fld protected long wholeGraphPureTimeThreadCPU
fld protected long[] timePerMethodId0
fld protected long[] timePerMethodId1
fld protected long[] totalTimePerMethodId0
fld protected long[] totalTimePerMethodId1
fld protected org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot cpuResSnapshot
fld protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer cachedFlatProfile
fld public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode rootNode
meth protected int get2Bytes(int)
meth protected int get3Bytes(int)
meth protected int get4Bytes(int)
meth protected long get5Bytes(int)
meth protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer generateFlatProfile()
meth protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer postGenerateFlatProfile()
meth protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree generateReverseCCT(int)
meth protected void addFlatProfTimeForNode(int)
meth protected void addToReverseCCT(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree,int)
meth protected void checkStraightGraphNode(int)
meth protected void preGenerateFlatProfile()
meth protected void setChildOfsForNodeOfs(int,int,int)
meth protected void setMethodIdForNodeOfs(int,int)
meth protected void setNCallsForNodeOfs(int,int)
meth protected void setNChildrenForNodeOfs(int,int)
meth protected void setSelfTime0ForNodeOfs(int,long)
meth protected void setSelfTime1ForNodeOfs(int,long)
meth protected void setSleepTime0ForNodeOfs(int,long)
meth protected void setTotalTime0ForNodeOfs(int,long)
meth protected void setTotalTime1ForNodeOfs(int,long)
meth protected void setWaitTime0ForNodeOfs(int,long)
meth protected void store2Bytes(int,int)
meth protected void store3Bytes(int,int)
meth protected void store4Bytes(int,int)
meth protected void store5Bytes(int,long)
meth public boolean canDisplayWholeGraphCPUTime()
meth public boolean isCollectingTwoTimeStamps()
meth public int getChildOfsForNodeOfs(int,int)
meth public int getMethodIdForNodeOfs(int)
meth public int getNCallsForNodeOfs(int)
meth public int getNChildrenForNodeOfs(int)
meth public int getThreadId()
meth public java.lang.String getThreadName()
meth public java.lang.String[] getMethodClassNameAndSig(int)
meth public long getSelfTime0ForNodeOfs(int)
meth public long getSelfTime1ForNodeOfs(int)
meth public long getSleepTime0ForNodeOfs(int)
meth public long getTotalTime0ForNodeOfs(int)
meth public long getTotalTime1ForNodeOfs(int)
meth public long getWaitTime0ForNodeOfs(int)
meth public long getWholeGraphNetTime0()
meth public long getWholeGraphNetTime1()
meth public long getWholeGraphPureTimeAbs()
meth public long getWholeGraphPureTimeThreadCPU()
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot getCPUResSnapshot()
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer getFlatProfile()
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getReverseCCT(int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getRootNode()
meth public static double getTimeInInjectedCodeForDisplayedThread()
meth public static double getWholeGraphGrossTimeAbsForDisplayedThread()
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr java.lang.Object
hfds LOGGER,childTotalNCalls,childTotalTime0InTimerUnits,childTotalTime1InTimerUnits,currentNodeStackSize,filter,methodInfoMapper,nodeStack,nodeStackPtr,reverseCCTRootNode,selectedMethodId,timeInInjectedCodeInMS,timingAdjuster,totalInvNo,wholeGraphGrossTimeAbsInMS
hcls AddChildLocalVars,GenerateMirrorNodeLocalVars

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[] createPresentationCCTs(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public org.netbeans.lib.profiler.results.cpu.CPUCallGraphBuilder
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUCCTProvider
intf org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener
meth protected boolean isCollectingTwoTimeStamps()
meth protected boolean isReady()
meth protected long getDumpAbsTimeStamp()
meth protected long[][] getAllThreadsActiveTimes()
meth protected org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth protected void doBatchStart()
meth protected void doBatchStop()
meth protected void doReset()
meth protected void doShutdown()
meth protected void doStartup(org.netbeans.lib.profiler.ProfilerClient)
meth protected void setFilter(org.netbeans.lib.profiler.filters.InstrumentationFilter)
meth public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[] createPresentationCCTs(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)
meth public void methodEntry(int,int,int,long,long,java.util.List,int[])
meth public void methodEntryUnstamped(int,int,int,java.util.List,int[])
meth public void methodExit(int,int,int,long,long,java.lang.Object)
meth public void methodExitUnstamped(int,int,int)
meth public void monitorEntry(int,long,long,int,int)
meth public void monitorExit(int,long,long,int)
meth public void newMonitor(int,java.lang.String)
meth public void newThread(int,java.lang.String,java.lang.String)
meth public void parkEntry(int,long,long)
meth public void parkExit(int,long,long)
meth public void profilingPoint(int,int,long)
meth public void servletRequest(int,int,java.lang.String,int)
meth public void setMethodInfoMapper(org.netbeans.lib.profiler.results.cpu.MethodInfoMapper)
meth public void sleepEntry(int,long,long)
meth public void sleepExit(int,long,long)
meth public void threadsResume(long,long)
meth public void threadsSuspend(long,long)
meth public void timeAdjust(int,long,long)
meth public void waitEntry(int,long,long)
meth public void waitExit(int,long,long)
supr org.netbeans.lib.profiler.results.BaseCallGraphBuilder
hfds debugCollector,delta,instrFilter,methodInfoMapper,stackIntegrityViolationReported,threadInfos,timingAdjuster
hcls DebugInfoCollector

CLSS public org.netbeans.lib.profiler.results.cpu.CPUDataFrameProcessor
cons public init()
meth public void doProcessDataFrame(java.nio.ByteBuffer)
supr org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor
hfds hasMonitorInfo,methodParameters

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener
fld public final static int METHODTYPE_MARKER = 3
fld public final static int METHODTYPE_NORMAL = 1
fld public final static int METHODTYPE_ROOT = 2
intf org.netbeans.lib.profiler.results.locks.LockProfilingResultListener
meth public abstract void methodEntry(int,int,int,long,long,java.util.List,int[])
meth public abstract void methodEntryUnstamped(int,int,int,java.util.List,int[])
meth public abstract void methodExit(int,int,int,long,long,java.lang.Object)
meth public abstract void methodExitUnstamped(int,int,int)
meth public abstract void parkEntry(int,long,long)
meth public abstract void parkExit(int,long,long)
meth public abstract void servletRequest(int,int,java.lang.String,int)
meth public abstract void sleepEntry(int,long,long)
meth public abstract void sleepExit(int,long,long)
meth public abstract void threadsResume(long,long)
meth public abstract void threadsSuspend(long,long)
meth public abstract void waitEntry(int,long,long)
meth public abstract void waitExit(int,long,long)

CLSS public org.netbeans.lib.profiler.results.cpu.CPUResultsDiff
cons public init(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot,org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)
meth public boolean isCollectingTwoTimeStamps()
meth public int getNInstrMethods()
meth public int getNThreads()
meth public int[] getThreadIds()
meth public java.lang.String getThreadNameForId(int)
meth public java.lang.String[] getInstrMethodClasses(int)
meth public java.lang.String[] getInstrMethodNames()
meth public java.lang.String[] getInstrMethodSignatures()
meth public java.lang.String[] getMethodClassNameAndSig(int,int)
meth public java.lang.String[] getThreadNames()
meth public java.util.Map<java.lang.Integer,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection> getMethodIDMap(int)
meth public long getBound(int)
meth public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer getContainerForThread(int,int)
meth public org.netbeans.lib.profiler.results.cpu.DiffFlatProfileContainer getFlatProfile(int,int)
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer getFlatProfile(java.util.Collection<java.lang.Integer>,int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getReverseCCT(int,int,int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getReverseRootNode(int,java.util.Collection<java.lang.Integer>,boolean)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getRootNode(int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getRootNode(int,java.util.Collection<java.lang.Integer>,boolean)
meth public void filterForward(java.lang.String,int,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked)
meth public void saveSortParams(int,boolean,org.netbeans.lib.profiler.results.CCTNode)
supr org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot
hfds snapshot1,snapshot2

CLSS public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.results.cpu.CPUCCTProvider,boolean,java.lang.String[],java.lang.String[],java.lang.String[],int) throws org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot$NoDataAvailableException
fld protected boolean collectingTwoTimeStamps
fld protected int nInstrMethods
fld protected java.lang.String[] instrMethodNames
fld protected java.lang.String[] instrMethodSignatures
fld protected java.lang.String[][] instrMethodClassesViews
fld protected java.util.Map threadIdMap
fld protected org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[] allThreadsMergedCCTContainers
fld protected org.netbeans.lib.profiler.results.cpu.CPUCCTContainer[][] threadCCTContainers
fld protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode[] rootNode
fld public final static int CLASS_LEVEL_VIEW = 1
fld public final static int METHOD_LEVEL_VIEW = 0
fld public final static int PACKAGE_LEVEL_VIEW = 2
innr public static NoDataAvailableException
meth protected org.netbeans.lib.profiler.results.cpu.CPUCCTContainer createContainerForThreads(java.util.Collection<java.lang.Integer>,int)
meth protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode createRootNodeForAllThreads(int)
meth public boolean isCollectingTwoTimeStamps()
meth public int getNInstrMethods()
meth public int getNThreads()
meth public int[] getThreadIds()
meth public java.lang.String getThreadNameForId(int)
meth public java.lang.String toString()
meth public java.lang.String[] getInstrMethodClasses(int)
meth public java.lang.String[] getInstrMethodNames()
meth public java.lang.String[] getInstrMethodSignatures()
meth public java.lang.String[] getMethodClassNameAndSig(int,int)
meth public java.lang.String[] getThreadNames()
meth public java.util.Map<java.lang.Integer,org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection> getMethodIDMap(int)
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getSourceCodeSelection(int,int)
meth public org.netbeans.lib.profiler.results.FilterSortSupport$Configuration getFilterSortInfo(org.netbeans.lib.profiler.results.CCTNode)
meth public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer getContainerForThread(int,int)
meth public org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot createDiff(org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot)
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer getFlatProfile(int,int)
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer getFlatProfile(java.util.Collection<java.lang.Integer>,int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getReverseCCT(int,int,int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getReverseRootNode(int,java.util.Collection<java.lang.Integer>,boolean)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getRootNode(int)
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode getRootNode(int,java.util.Collection<java.lang.Integer>,boolean)
meth public void filterForward(java.lang.String,int,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked)
meth public void filterReverse(java.lang.String,int,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree,int)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void saveSortParams(int,boolean,org.netbeans.lib.profiler.results.CCTNode)
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.ResultsSnapshot
hfds CPU_MSG,sortInfos

CLSS public static org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot$NoDataAvailableException
 outer org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot
cons public init()
supr java.lang.Exception

CLSS public org.netbeans.lib.profiler.results.cpu.CPUSamplingDataFrameProcessor
cons public init()
meth public void doProcessDataFrame(java.nio.ByteBuffer)
meth public void shutdown()
meth public void startup(org.netbeans.lib.profiler.ProfilerClient)
supr org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor
hfds builder,currentThreadClassName,currentThreadName,currentThreadsDump,currentTimestamp,formatter,lastThreadsDump,threadDumps
hcls ThreadDump,ThreadInfo

CLSS public org.netbeans.lib.profiler.results.cpu.DiffFlatProfileContainer
meth protected void swap(int,int)
meth public double getWholeGraphNetTime0()
meth public double getWholeGraphNetTime1()
meth public java.lang.String getMethodNameAtRow(int)
meth public long getMaxTime()
meth public long getMinTime()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getSourceCodeSelectionAtRow(int)
supr org.netbeans.lib.profiler.results.cpu.FlatProfileContainer
hfds maxTime,minTime,sels,wholeGraphNetTime0,wholeGraphNetTime1

CLSS public org.netbeans.lib.profiler.results.cpu.FlatProfileBuilder
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUCCTProvider$Listener
intf org.netbeans.lib.profiler.results.cpu.FlatProfileProvider
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer createFlatProfile()
meth public void cctEstablished(org.netbeans.lib.profiler.results.RuntimeCCTNode,boolean)
meth public void cctReset()
meth public void setContext(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.cpu.cct.TimeCollector,org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter)
supr java.lang.Object
hfds LOGGER,appNode,client,collector,filter,flattener,lastFlatProfile

CLSS public abstract org.netbeans.lib.profiler.results.cpu.FlatProfileContainer
cons public init(long[],long[],long[],long[],int[],char[],int)
fld protected boolean collectingTwoTimeStamps
fld protected final char[] methodMarks
fld protected float[] percent
fld protected int nRows
fld protected int[] methodIds
fld protected int[] nInvocations
fld protected long nTotalInvocations
fld protected long[] timeInMcs0
fld protected long[] timeInMcs1
fld protected long[] totalTimeInMcs0
fld protected long[] totalTimeInMcs1
fld protected static boolean staticUsePrimaryTime
fld public final static int SORT_BY_INV_NUMBER = 4
fld public final static int SORT_BY_NAME = 1
fld public final static int SORT_BY_SECONDARY_TIME = 3
fld public final static int SORT_BY_SECONDARY_TOTAL_TIME = 6
fld public final static int SORT_BY_TIME = 2
fld public final static int SORT_BY_TOTAL_TIME = 5
meth protected void removeZeroInvocationEntries()
meth protected void swap(int,int)
meth public abstract double getWholeGraphNetTime0()
meth public abstract double getWholeGraphNetTime1()
meth public abstract java.lang.String getMethodNameAtRow(int)
meth public abstract org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getSourceCodeSelectionAtRow(int)
meth public boolean isCollectingTwoTimeStamps()
meth public float getPercentAtRow(int)
meth public int getMethodIdAtRow(int)
meth public int getNInvocationsAtRow(int)
meth public int getNRows()
meth public long getNTotalInvocations()
meth public long getTimeInMcs0AtRow(int)
meth public long getTimeInMcs1AtRow(int)
meth public long getTotalTimeInMcs0AtRow(int)
meth public long getTotalTimeInMcs1AtRow(int)
meth public void filterOriginalData(java.lang.String[],int,double)
meth public void sortBy(int,boolean)
supr java.lang.Object
hfds totalMethods

CLSS public org.netbeans.lib.profiler.results.cpu.FlatProfileContainerBacked
cons public init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,long[],long[],long[],long[],int[],int)
fld protected org.netbeans.lib.profiler.results.cpu.CPUCCTContainer cctContainer
meth public double getWholeGraphNetTime0()
meth public double getWholeGraphNetTime1()
meth public java.lang.String getMethodNameAtRow(int)
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getSourceCodeSelectionAtRow(int)
meth public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer getCCTContainer()
supr org.netbeans.lib.profiler.results.cpu.FlatProfileContainer

CLSS public org.netbeans.lib.profiler.results.cpu.FlatProfileContainerFree
cons public init(org.netbeans.lib.profiler.global.ProfilingSessionStatus,long[],long[],long[],long[],int[],char[],double,double,int)
fld protected double wholeGraphNetTime0
fld protected double wholeGraphNetTime1
fld protected org.netbeans.lib.profiler.global.ProfilingSessionStatus status
meth public double getWholeGraphNetTime0()
meth public double getWholeGraphNetTime1()
meth public java.lang.String getMethodNameAtRow(int)
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection getSourceCodeSelectionAtRow(int)
meth public org.netbeans.lib.profiler.global.ProfilingSessionStatus getStatus()
supr org.netbeans.lib.profiler.results.cpu.FlatProfileContainer

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.FlatProfileProvider
meth public abstract org.netbeans.lib.profiler.results.cpu.FlatProfileContainer createFlatProfile()

CLSS public org.netbeans.lib.profiler.results.cpu.InstrTimingData
cons public init()
fld public final static org.netbeans.lib.profiler.results.cpu.InstrTimingData DEFAULT
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String toString()
supr java.lang.Object
hfds methodEntryExitCallTime0,methodEntryExitCallTime1,methodEntryExitInnerTime0,methodEntryExitInnerTime1,methodEntryExitOuterTime0,methodEntryExitOuterTime1,timerCountsInSecond0,timerCountsInSecond1

CLSS public org.netbeans.lib.profiler.results.cpu.MethodIdMap
cons public init(java.lang.String[],int,int)
meth public int getClassOrPackageIdForMethodId(int)
meth public int getNInstrClassesOrPackages()
meth public java.lang.String[] getInstrClassesOrPackages()
supr java.lang.Object
hfds ANONYMOUS_PACKAGE_STRING,classIdCache,classIds,classOrPackageNames,curClassId,newView

CLSS public abstract org.netbeans.lib.profiler.results.cpu.MethodInfoMapper
cons public init()
fld protected final static java.util.logging.Logger LOGGER
fld public final static org.netbeans.lib.profiler.results.cpu.MethodInfoMapper DEFAULT
meth public abstract int getMaxMethodId()
meth public abstract int getMinMethodId()
meth public abstract java.lang.String getInstrMethodClass(int)
meth public abstract java.lang.String getInstrMethodName(int)
meth public abstract java.lang.String getInstrMethodSignature(int)
meth public void lock(boolean)
meth public void unlock()
supr java.lang.Object

CLSS public abstract org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode
cons protected init()
cons protected init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode,int)
fld protected char flags
fld protected final static char MASK_CONTEXT_CALLS_NODE = '\u0002'
fld protected final static char MASK_SELF_TIME_NODE = '\u0001'
fld protected final static char MASK_THREAD_NODE = '\u0004'
fld protected int methodId
fld protected int nCalls
fld protected long sleepTime0
fld protected long totalTime0
fld protected long totalTime1
fld protected long waitTime0
fld protected org.netbeans.lib.profiler.results.cpu.CPUCCTContainer container
fld protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode parent
fld protected org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode[] children
fld public final static int SORT_BY_INVOCATIONS = 4
fld public final static int SORT_BY_NAME = 1
fld public final static int SORT_BY_TIME_0 = 2
fld public final static int SORT_BY_TIME_1 = 3
fld public final static org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode EMPTY
intf java.lang.Cloneable
meth protected java.lang.String computeNodeName()
meth protected static java.util.Collection<org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode> resolveChildren(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode)
meth protected void resetChildren()
meth protected void setupFilteredNode(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode)
meth public abstract float getTotalTime0InPerCent()
meth public abstract float getTotalTime1InPerCent()
meth public boolean equals(java.lang.Object)
meth public boolean isContextCallsNode()
meth public boolean isSelfTimeNode()
meth public boolean isThreadNode()
meth public int getIndexOfChild(java.lang.Object)
meth public int getMethodId()
meth public int getNCalls()
meth public int getNChildren()
meth public int getThreadId()
meth public int hashCode()
meth public java.lang.String getNodeName()
meth public java.lang.String toString()
meth public java.lang.String[] getMethodClassNameAndSig()
meth public long getSleepTime0()
meth public long getTotalTime0()
meth public long getTotalTime1()
meth public long getWaitTime0()
meth public org.netbeans.lib.profiler.results.CCTNode getChild(int)
meth public org.netbeans.lib.profiler.results.CCTNode getParent()
meth public org.netbeans.lib.profiler.results.CCTNode[] getChildren()
meth public org.netbeans.lib.profiler.results.cpu.CPUCCTContainer getContainer()
meth public void addNCalls(int)
meth public void addSleepTime0(long)
meth public void addTotalTime0(long)
meth public void addTotalTime1(long)
meth public void addWaitTime0(long)
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
meth public void setContextCallsNode()
meth public void setSelfTimeNode()
meth public void setThreadNode()
meth public void sortChildren(int,boolean)
supr org.netbeans.lib.profiler.results.CCTNode
hfds SELF_TIME_STRING,nodeName

CLSS public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked
cons protected init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode[])
cons public init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode,int)
fld protected int nChildren
fld protected int selfCompactDataOfs
fld protected java.util.Set<java.lang.Integer> compactDataOfs
meth protected void merge(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked)
meth protected void resetChildren()
meth protected void setupFilteredNode(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked)
meth public boolean isLeaf()
meth public float getTotalTime0InPerCent()
meth public float getTotalTime1InPerCent()
meth public int getNChildren()
meth public org.netbeans.lib.profiler.results.CCTNode createFilteredNode()
meth public org.netbeans.lib.profiler.results.CCTNode getChild(int)
meth public org.netbeans.lib.profiler.results.CCTNode[] getChildren()
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeBacked createRootCopy()
meth public static void setPercentFormat(java.text.NumberFormat)
meth public void exportCSVData(java.lang.String,int,org.netbeans.lib.profiler.results.ExportDataDumper)
meth public void exportHTMLData(org.netbeans.lib.profiler.results.ExportDataDumper,int)
meth public void exportXMLData(org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
meth public void setSelfTimeNode()
supr org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode
hfds percentFormat

CLSS public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree
cons protected init(org.netbeans.lib.profiler.results.cpu.CPUCCTContainer,org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode,int)
meth public float getTotalTime0InPerCent()
meth public float getTotalTime1InPerCent()
meth public org.netbeans.lib.profiler.results.CCTNode createFilteredNode()
meth public org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree createChildlessCopy()
meth public static void setPercentFormat(java.text.NumberFormat)
meth public void addChild(org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNodeFree)
meth public void exportCSVData(java.lang.String,int,org.netbeans.lib.profiler.results.ExportDataDumper)
meth public void exportHTMLData(org.netbeans.lib.profiler.results.ExportDataDumper,int)
meth public void exportXMLData(org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void setMethodId(int)
supr org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode
hfds percentFormat

CLSS public org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder
cons public init()
cons public init(int,org.netbeans.lib.profiler.filters.InstrumentationFilter)
meth public boolean collectionTwoTimeStamps()
meth public final org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot createSnapshot(long) throws org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot$NoDataAvailableException
meth public final void addStacktrace(java.lang.management.ThreadInfo[],long)
meth public final void reset()
meth public final void setIgnoredThreads(java.util.Set<java.lang.String>)
meth public org.netbeans.lib.profiler.filters.InstrumentationFilter getFilter()
meth public org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth public org.netbeans.lib.profiler.results.cpu.MethodInfoMapper getMapper()
supr java.lang.Object
hfds COLLECT_TWO_TIMESTAMPS,NAME_SIG_SPLITTER,NO_STACK_TRACE,ccgb,currentDumpTimeStamp,filter,ignoredThreadNames,knownBLockingMethods,lastStackTrace,lock,mapper,methodInfoMap,methodInfos,stackTraceCount,stampLock,status,threadCompactData,threadIds,threadNames,threadtimes
hcls MethodInfo,SampledThreadInfo,StackTraceCallGraphBuilder

CLSS public org.netbeans.lib.profiler.results.cpu.ThreadInfo
fld public final int threadId
fld public int totalNNodes
fld public long rootMethodEntryTimeAbs
fld public long rootMethodEntryTimeThreadCPU
fld public long topMethodEntryTime0
fld public long topMethodEntryTime1
fld public long totalNInv
fld public org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode[] stack
meth public boolean isInRoot()
meth public org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode peek()
meth public org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode pop()
meth public void push(org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode)
supr java.lang.Object
hfds inRoot,rootGrossTimeAbs,rootGrossTimeThreadCPU,stackLock,stackTopIdx

CLSS public org.netbeans.lib.profiler.results.cpu.ThreadInfos
cons public init()
fld public org.netbeans.lib.profiler.results.cpu.ThreadInfo[] threadInfos
meth public boolean isEmpty()
meth public java.lang.String[] getThreadNames()
meth public void beginTrans(boolean)
meth public void endTrans()
meth public void newThreadInfo(int,java.lang.String,java.lang.String)
meth public void reset()
supr java.lang.Object
hfds threadClassNames,threadInfosLastIdx,threadNames,transaction

CLSS public org.netbeans.lib.profiler.results.cpu.TimingAdjuster
cons public init()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld
meth public final double adjustTime(long,int,int,boolean)
meth public final double delta(int,int,boolean)
meth public org.netbeans.lib.profiler.results.cpu.InstrTimingData getInstrTimingData()
meth public static org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld getDefault()
meth public static org.netbeans.lib.profiler.results.cpu.TimingAdjusterOld getInstance(org.netbeans.lib.profiler.global.ProfilingSessionStatus)
supr java.lang.Object
hfds instance,lastStatusRef,timingData

CLSS public org.netbeans.lib.profiler.results.cpu.cct.CCTFlattener
cons public init(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter)
meth protected int getMaxMethodId()
meth protected java.lang.String getInstrMethodClass(int)
meth protected java.lang.String getInstrMethodName(int)
meth protected org.netbeans.lib.profiler.results.cpu.FlatProfileContainer createContainer(long[],long[],long[],long[],int[],double,double)
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer getFlatProfile()
meth public void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode)
meth public void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode)
meth public void onStart()
meth public void onStop()
supr org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$PluginAdapter
hfds LOGGER,client,container,containerGuard,cpuProfilingType,currentFilter,instrFilter,invDiff,invPM,methodsOnStack,nCalleeInvocations,nMethods,parentStack,timePM0,timePM1,timingAdjuster,totalTimePM0,totalTimePM1,twoTimestamps
hcls TotalTime

CLSS public final org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter
cons public init()
innr public abstract interface static Evaluator
innr public abstract interface static EvaluatorProvider
meth public boolean passesFilter()
meth public void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth public void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode)
meth public void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth public void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode)
meth public void onStart()
meth public void onStop()
meth public void reset()
meth public void setEvaluators(java.util.Collection)
supr org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$PluginAdapter
hfds LOGGER,evaluatorProviders,evaluators,passFlagStack,passingFilter

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter$Evaluator
 outer org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter
meth public abstract boolean evaluate(org.netbeans.lib.profiler.marker.Mark)

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter$EvaluatorProvider
 outer org.netbeans.lib.profiler.results.cpu.cct.CCTResultsFilter
meth public abstract java.util.Set getEvaluators()

CLSS public org.netbeans.lib.profiler.results.cpu.cct.TimeCollector
cons public init()
meth public long getNetTime0(org.netbeans.lib.profiler.marker.Mark)
meth public long getNetTime1(org.netbeans.lib.profiler.marker.Mark)
meth public void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode)
meth public void onStart()
meth public void onStop()
supr org.netbeans.lib.profiler.results.cpu.marking.MarkAwareNodeProcessorPlugin
hfds timing
hcls TimingData

CLSS public abstract org.netbeans.lib.profiler.results.cpu.cct.nodes.BaseCPUCCTNode
cons public init()
intf org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode
meth public org.netbeans.lib.profiler.results.RuntimeCCTNode[] getChildren()
meth public void attachNodeAsChild(org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode)
supr java.lang.Object
hfds EMPTY_CHILDREN,children

CLSS public org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode
cons public init(org.netbeans.lib.profiler.marker.Mark)
innr public static Locator
meth protected org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode createSelfInstance()
meth public boolean equals(java.lang.Object)
meth public boolean isRoot()
meth public int hashCode()
meth public org.netbeans.lib.profiler.marker.Mark getMark()
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode
hfds mark

CLSS public static org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode$Locator
 outer org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode
meth public static org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode locate(org.netbeans.lib.profiler.marker.Mark,org.netbeans.lib.profiler.results.RuntimeCCTNode[])
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode
cons public init(int)
innr public static Locator
meth protected org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode createSelfInstance()
meth public boolean isRoot()
meth public int getMethodId()
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode
hfds methodId

CLSS public static org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode$Locator
 outer org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode
meth public static org.netbeans.lib.profiler.results.cpu.cct.nodes.MethodCPUCCTNode locate(int,org.netbeans.lib.profiler.results.RuntimeCCTNode[])
supr java.lang.Object

CLSS public abstract interface org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode
intf org.netbeans.lib.profiler.results.RuntimeCCTNode
meth public abstract boolean isRoot()
meth public abstract void attachNodeAsChild(org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode)

CLSS public org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode
cons public init(int,java.lang.String)
innr public static Locator
meth protected org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode createSelfInstance()
meth public boolean equals(java.lang.Object)
meth public boolean isRoot()
meth public int getRequestType()
meth public int hashCode()
meth public java.lang.String getServletPath()
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode
hfds hashCode,requestType,servletPath

CLSS public static org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode$Locator
 outer org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode
meth public static org.netbeans.lib.profiler.results.cpu.cct.nodes.ServletRequestCPUCCTNode locate(int,java.lang.String,org.netbeans.lib.profiler.results.RuntimeCCTNode[])
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.cpu.cct.nodes.SimpleCPUCCTNode
cons public init(boolean)
cons public init(int)
meth public boolean isRoot()
meth public int getMaxMethodId()
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.BaseCPUCCTNode
hfds maxMethodId,root

CLSS public org.netbeans.lib.profiler.results.cpu.cct.nodes.ThreadCPUCCTNode
cons public init(int)
meth protected org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode createSelfInstance()
meth public boolean isRoot()
meth public int getThreadId()
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode
hfds threadId

CLSS public abstract org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode
cons public init()
fld public final static int FILTERED_MAYBE = 1
fld public final static int FILTERED_NO = 0
fld public final static int FILTERED_YES = 2
intf java.lang.Cloneable
intf org.netbeans.lib.profiler.results.cpu.cct.nodes.RuntimeCPUCCTNode
meth protected abstract org.netbeans.lib.profiler.results.cpu.cct.nodes.TimedCPUCCTNode createSelfInstance()
meth public int addNCalls(int)
meth public int addNCallsDiff(int)
meth public int getFilteredStatus()
meth public int getNCalls()
meth public int getNCallsDiff()
meth public java.lang.Object clone()
meth public long addNetTime0(long)
meth public long addNetTime1(long)
meth public long addSleepTime0(long)
meth public long addWaitTime0(long)
meth public long getLastWaitOrSleepStamp()
meth public long getNetTime0()
meth public long getNetTime1()
meth public long getSleepTime0()
meth public long getWaitTime0()
meth public void setFilteredStatus(int)
meth public void setLastWaitOrSleepStamp(long)
meth public void setNCalls(int)
meth public void setNCallsDiff(int)
meth public void setNetTime0(long)
meth public void setNetTime1(long)
meth public void setSleepTime0(long)
meth public void setWaitTime0(long)
supr org.netbeans.lib.profiler.results.cpu.cct.nodes.BaseCPUCCTNode
hfds filteredStatus,lastWaitOrSleepStamp,nCalls,nCallsDiff,netTime0,netTime1,sleepTime0,waitTime0

CLSS public org.netbeans.lib.profiler.results.cpu.marking.CharStack
cons public init()
meth public boolean isEmpty()
meth public char peek()
meth public char pop()
meth public void clear()
meth public void push(char)
supr java.lang.Object
hfds data,loadFactor,maxCapacity,stackPointer

CLSS public org.netbeans.lib.profiler.results.cpu.marking.MarkAwareNodeProcessorPlugin
cons public init()
intf org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine$StateObserver
meth protected boolean isReset()
meth protected final org.netbeans.lib.profiler.marker.Mark getCurrentMark()
meth protected final org.netbeans.lib.profiler.marker.Mark getParentMark()
meth public void beginTrans(boolean)
meth public void endTrans()
meth public void onBackout(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth public void onNode(org.netbeans.lib.profiler.results.cpu.cct.nodes.MarkedCPUCCTNode)
meth public void onReset()
meth public void onStart()
meth public void onStop()
meth public void stateChanged(org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine)
supr org.netbeans.lib.profiler.results.RuntimeCCTNodeProcessor$PluginAdapter
hfds markStack,parentMark,resetFlag,transaction

CLSS public org.netbeans.lib.profiler.results.cpu.marking.MarkMapping
cons public init(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection,org.netbeans.lib.profiler.marker.Mark)
fld public final java.lang.String markSig
fld public final org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection markMask
fld public final org.netbeans.lib.profiler.marker.Mark mark
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine
innr public abstract interface static StateObserver
meth public int getNMarks()
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getMarkerMethods()
meth public org.netbeans.lib.profiler.marker.Mark markMethod(int,org.netbeans.lib.profiler.global.ProfilingSessionStatus)
meth public static org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine getDefault()
meth public void configure(org.netbeans.lib.profiler.results.cpu.marking.MarkMapping[],java.util.Collection)
meth public void deconfigure()
supr java.lang.Object
hfds INVALID_MID,LOGGER,instance,mapper,markGuard,marks,observers

CLSS public abstract interface static org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine$StateObserver
 outer org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine
meth public abstract void stateChanged(org.netbeans.lib.profiler.results.cpu.marking.MarkingEngine)

CLSS public abstract interface org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider
fld public final static int SQL_CALLABLE_STATEMENT = 2
fld public final static int SQL_COMMAND_ALTER = 0
fld public final static int SQL_COMMAND_BATCH = -2
fld public final static int SQL_COMMAND_CREATE = 1
fld public final static int SQL_COMMAND_DELETE = 2
fld public final static int SQL_COMMAND_DESCRIBE = 3
fld public final static int SQL_COMMAND_INSERT = 4
fld public final static int SQL_COMMAND_OTHER = -1
fld public final static int SQL_COMMAND_SELECT = 5
fld public final static int SQL_COMMAND_SET = 6
fld public final static int SQL_COMMAND_UPDATE = 7
fld public final static int SQL_PREPARED_STATEMENT = 1
fld public final static int SQL_STATEMENT = 0
fld public final static int SQL_STATEMENT_UNKNOWN = -1
fld public final static java.lang.String CALLABLE_STATEMENT_INTERFACE
fld public final static java.lang.String CONNECTION_INTERFACE
fld public final static java.lang.String DRIVER_INTERFACE
fld public final static java.lang.String PREPARED_STATEMENT_INTERFACE
fld public final static java.lang.String STATEMENT_INTERFACE
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider
intf org.netbeans.lib.profiler.results.cpu.FlatProfileProvider
meth public abstract int getCommandType(int)
meth public abstract int getSQLCommand(int)
meth public abstract java.lang.String[] getTables(int)
meth public abstract org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForSelects()
meth public abstract void beginTrans(boolean)
meth public abstract void endTrans()
meth public abstract void updateInternals()

CLSS public abstract interface static org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public org.netbeans.lib.profiler.results.jdbc.JdbcGraphBuilder
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener
intf org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider
meth protected org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth protected void doBatchStart()
meth protected void doBatchStop()
meth protected void doReset()
meth protected void doShutdown()
meth protected void doStartup(org.netbeans.lib.profiler.ProfilerClient)
meth public int getCommandType(int)
meth public int getSQLCommand(int)
meth public java.lang.String[] getTables(int)
meth public org.netbeans.lib.profiler.results.cpu.FlatProfileContainer createFlatProfile()
meth public org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForSelects()
meth public void beginTrans(boolean)
meth public void endTrans()
meth public void methodEntry(int,int,int,long,long,java.util.List,int[])
meth public void methodEntryUnstamped(int,int,int,java.util.List,int[])
meth public void methodExit(int,int,int,long,long,java.lang.Object)
meth public void methodExitUnstamped(int,int,int)
meth public void monitorEntry(int,long,long,int,int)
meth public void monitorExit(int,long,long,int)
meth public void newMonitor(int,java.lang.String)
meth public void newThread(int,java.lang.String,java.lang.String)
meth public void parkEntry(int,long,long)
meth public void parkExit(int,long,long)
meth public void profilingPoint(int,int,long)
meth public void servletRequest(int,int,java.lang.String,int)
meth public void sleepEntry(int,long,long)
meth public void sleepExit(int,long,long)
meth public void threadsResume(long,long)
meth public void threadsSuspend(long,long)
meth public void timeAdjust(int,long,long)
meth public void updateInternals()
meth public void waitEntry(int,long,long)
meth public void waitExit(int,long,long)
supr org.netbeans.lib.profiler.results.BaseCallGraphBuilder
hfds JDBC_LOGGER,connections,currentObject,currentSqlLevel,filter,idsToSelect,lastSelectId,selectsToId,sqlParser,stacksForSelects,statements,threadInfos
hcls JdbcCCTFlattener,JdbcFlatProfileContainer,Select

CLSS public org.netbeans.lib.profiler.results.jdbc.JdbcResultsDiff
cons public init(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot,org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot)
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public boolean containsStacks()
meth public long getBeginTime()
meth public long getTimeTaken()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot
hfds selectIdToSnapshot1,selectIdToSnapshot2,snapshot1,snapshot2

CLSS public org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider,org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth protected void performInit(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.jdbc.JdbcCCTProvider) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public boolean containsStacks()
meth public int getNProfiledSelects()
meth public int[] getCommandTypeForSelectId()
meth public int[] getTypeForSelectId()
meth public java.lang.String getSelectName(int)
meth public java.lang.String[] getSelectNames()
meth public java.lang.String[][] getTablesForSelectId()
meth public long[] getInvocationsPerSelectId()
meth public long[] getTimePerSelectId()
meth public org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot createDiff(org.netbeans.lib.profiler.results.jdbc.JdbcResultsSnapshot)
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean)
meth public void filterReverse(java.lang.String,int,int,boolean,org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode,int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.ResultsSnapshot
hfds commandTypeForSelectId,invocationsPerSelectId,nProfiledSelects,selectNames,stacksForSelects,table,tablesForSelectId,timePerSelectId,typeForSelectId

CLSS public abstract org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor
cons public init()
fld protected volatile int currentThreadId
meth protected void fireAdjustTime(int,long,long)
meth protected void fireMonitorEntry(int,long,long,int,int)
meth protected void fireMonitorExit(int,long,long,int)
meth protected void fireNewMonitor(int,java.lang.String)
meth protected void fireNewThread(int,java.lang.String,java.lang.String)
supr org.netbeans.lib.profiler.results.AbstractDataFrameProcessor

CLSS public abstract org.netbeans.lib.profiler.results.locks.LockCCTNode
fld public final static org.netbeans.lib.profiler.results.locks.LockCCTNode EMPTY
meth public abstract java.lang.String getNodeName()
meth public abstract long getTime()
meth public abstract long getWaits()
meth public boolean isMonitorNode()
meth public boolean isThreadLockNode()
meth public double getTimeInPerCent()
meth public int getIndexOfChild(java.lang.Object)
meth public int getNChildren()
meth public java.lang.String toString()
meth public org.netbeans.lib.profiler.results.locks.LockCCTNode getChild(int)
meth public org.netbeans.lib.profiler.results.locks.LockCCTNode getParent()
meth public org.netbeans.lib.profiler.results.locks.LockCCTNode[] getChildren()
meth public void debug()
supr org.netbeans.lib.profiler.results.CCTNode
hfds children,parent

CLSS public abstract interface org.netbeans.lib.profiler.results.locks.LockCCTProvider
innr public abstract interface static Listener
intf org.netbeans.lib.profiler.results.CCTProvider

CLSS public abstract interface static org.netbeans.lib.profiler.results.locks.LockCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.locks.LockCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public org.netbeans.lib.profiler.results.locks.LockDataFrameProcessor
cons public init()
meth public void doProcessDataFrame(java.nio.ByteBuffer)
supr org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor

CLSS public org.netbeans.lib.profiler.results.locks.LockGraphBuilder
cons public init()
innr public final static CPULockGraphBuilder
innr public final static MemoryLockGraphBuilder
intf org.netbeans.lib.profiler.results.locks.LockCCTProvider
intf org.netbeans.lib.profiler.results.locks.LockProfilingResultListener
meth protected org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth protected void doBatchStart()
meth protected void doBatchStop()
meth protected void doReset()
meth protected void doShutdown()
meth protected void doStartup(org.netbeans.lib.profiler.ProfilerClient)
meth public void monitorEntry(int,long,long,int,int)
meth public void monitorExit(int,long,long,int)
meth public void newMonitor(int,java.lang.String)
meth public void newThread(int,java.lang.String,java.lang.String)
meth public void profilingPoint(int,int,long)
meth public void timeAdjust(int,long,long)
supr org.netbeans.lib.profiler.results.BaseCallGraphBuilder
hfds LOG,monitorInfos,threadInfos,transaction

CLSS public final static org.netbeans.lib.profiler.results.locks.LockGraphBuilder$CPULockGraphBuilder
 outer org.netbeans.lib.profiler.results.locks.LockGraphBuilder
cons public init()
intf org.netbeans.lib.profiler.results.cpu.CPUProfilingResultListener
meth public void methodEntry(int,int,int,long,long,java.util.List,int[])
meth public void methodEntryUnstamped(int,int,int,java.util.List,int[])
meth public void methodExit(int,int,int,long,long,java.lang.Object)
meth public void methodExitUnstamped(int,int,int)
meth public void parkEntry(int,long,long)
meth public void parkExit(int,long,long)
meth public void servletRequest(int,int,java.lang.String,int)
meth public void sleepEntry(int,long,long)
meth public void sleepExit(int,long,long)
meth public void threadsResume(long,long)
meth public void threadsSuspend(long,long)
meth public void waitEntry(int,long,long)
meth public void waitExit(int,long,long)
supr org.netbeans.lib.profiler.results.locks.LockGraphBuilder

CLSS public final static org.netbeans.lib.profiler.results.locks.LockGraphBuilder$MemoryLockGraphBuilder
 outer org.netbeans.lib.profiler.results.locks.LockGraphBuilder
cons public init()
intf org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener
meth public void onAllocStackTrace(char,long,int[])
meth public void onGcPerformed(char,long,int)
meth public void onLivenessStackTrace(char,long,int,long,int[])
supr org.netbeans.lib.profiler.results.locks.LockGraphBuilder

CLSS public abstract interface org.netbeans.lib.profiler.results.locks.LockProfilingResultListener
intf org.netbeans.lib.profiler.results.ProfilingResultListener
meth public abstract void monitorEntry(int,long,long,int,int)
meth public abstract void monitorExit(int,long,long,int)
meth public abstract void newMonitor(int,java.lang.String)
meth public abstract void newThread(int,java.lang.String,java.lang.String)
meth public abstract void timeAdjust(int,long,long)

CLSS public org.netbeans.lib.profiler.results.locks.LockRuntimeCCTNode
intf org.netbeans.lib.profiler.results.RuntimeCCTNode
meth public org.netbeans.lib.profiler.results.RuntimeCCTNode[] getChildren()
meth public org.netbeans.lib.profiler.results.locks.LockCCTNode getMonitors()
meth public org.netbeans.lib.profiler.results.locks.LockCCTNode getThreads()
supr java.lang.Object
hfds monitors,threads

CLSS public org.netbeans.lib.profiler.results.memory.AllocMemoryResultsDiff
cons public init(org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot,org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot)
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public boolean containsStacks()
meth public int getNProfiledClasses()
meth public int[] getObjectsCounts()
meth public java.lang.String getClassName(int)
meth public java.lang.String[] getClassNames()
meth public long getBeginTime()
meth public long getMaxObjectsSizePerClassDiff()
meth public long getMinObjectsSizePerClassDiff()
meth public long getTimeTaken()
meth public long[] getObjectsSizePerClass()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot
hfds classNames,maxObjectsSizePerClassDiff,minObjectsSizePerClassDiff,nClasses,objectsCounts,objectsSizePerClass,snapshot1,snapshot2

CLSS public org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider,org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public int[] getObjectsCounts()
meth public java.lang.String toString()
meth public org.netbeans.lib.profiler.results.memory.AllocMemoryResultsSnapshot createDiff(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot)
meth public void performInit(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot
hfds MEMORY_ALLOC_MSG,objectsCounts

CLSS public org.netbeans.lib.profiler.results.memory.ClassHistoryDataManager
cons public init()
cons public init(int)
fld public int[] nTotalAllocObjects
fld public int[] nTrackedLiveObjects
fld public long[] timeStamps
fld public long[] totalAllocObjectsSize
fld public long[] trackedLiveObjectsSize
meth public boolean isTrackingClass()
meth public int getArrayBufferSize()
meth public int getItemCount()
meth public int getTrackedClassID()
meth public java.lang.String getTrackedClassName()
meth public void processData(int[],int[],long[])
meth public void processData(int[],long[])
meth public void resetClass()
meth public void setArrayBufferSize(int)
meth public void setupClass(int,java.lang.String)
supr org.netbeans.lib.profiler.results.DataManager
hfds arrayBufferSize,currentArraysSize,itemCount,trackedClassID,trackedClassName

CLSS public org.netbeans.lib.profiler.results.memory.DiffObjAllocCCTNode
cons public init(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode,org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode)
meth public boolean equals(java.lang.Object)
meth public boolean isLeaf()
meth public int hashCode()
meth public java.lang.String getNodeName()
meth public java.lang.String[] getMethodClassNameAndSig()
meth public org.netbeans.lib.profiler.results.memory.DiffObjAllocCCTNode createFilteredNode()
supr org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode
hfds node1,node2

CLSS public abstract org.netbeans.lib.profiler.results.memory.HeapHistogram
cons public init()
innr public abstract static ClassInfo
meth public abstract java.util.Date getTime()
meth public abstract java.util.Set<org.netbeans.lib.profiler.results.memory.HeapHistogram$ClassInfo> getHeapHistogram()
meth public abstract java.util.Set<org.netbeans.lib.profiler.results.memory.HeapHistogram$ClassInfo> getPermGenHistogram()
meth public abstract long getTotalBytes()
meth public abstract long getTotalHeapBytes()
meth public abstract long getTotalHeapInstances()
meth public abstract long getTotalInstances()
meth public abstract long getTotalPerGenInstances()
meth public abstract long getTotalPermGenHeapBytes()
supr java.lang.Object

CLSS public abstract static org.netbeans.lib.profiler.results.memory.HeapHistogram$ClassInfo
 outer org.netbeans.lib.profiler.results.memory.HeapHistogram
cons public init()
meth public abstract java.lang.String getName()
meth public abstract long getBytes()
meth public abstract long getInstancesCount()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.memory.HeapHistogramManager
cons public init(org.netbeans.lib.profiler.ProfilerEngineSettings)
meth public org.netbeans.lib.profiler.results.memory.HeapHistogram getHistogram(org.netbeans.lib.profiler.wireprotocol.HeapHistogramResponse)
supr java.lang.Object
hfds classesIdMap,settings
hcls ClassInfoImpl,HeapHistogramImpl

CLSS public org.netbeans.lib.profiler.results.memory.JMethodIdTable
cons public init()
cons public init(org.netbeans.lib.profiler.results.memory.JMethodIdTable)
innr public static JMethodIdTableEntry
meth public java.lang.String debug()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable$JMethodIdTableEntry getEntry(int)
meth public static org.netbeans.lib.profiler.results.memory.JMethodIdTable getDefault()
meth public static void reset()
meth public void checkMethodId(int)
meth public void getNamesForMethodIds(org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr java.lang.Object
hfds NATIVE_SUFFIX,defaultTable,entries,incompleteEntries,nElements,size,staticTable,threshold

CLSS public static org.netbeans.lib.profiler.results.memory.JMethodIdTable$JMethodIdTableEntry
 outer org.netbeans.lib.profiler.results.memory.JMethodIdTable
fld public boolean isNative
fld public java.lang.String className
fld public java.lang.String methodName
fld public java.lang.String methodSig
supr java.lang.Object
hfds methodId

CLSS public org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsDiff
cons public init(org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot,org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot)
meth protected org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public boolean containsStacks()
meth public float[] getAvgObjectAge()
meth public int getNAlloc()
meth public int getNInstrClasses()
meth public int getNProfiledClasses()
meth public int getNTrackedItems()
meth public int[] getMaxSurvGen()
meth public int[] getNTrackedLiveObjects()
meth public int[] getnTotalAllocObjects()
meth public java.lang.String getClassName(int)
meth public java.lang.String[] getClassNames()
meth public long getBeginTime()
meth public long getMaxTrackedLiveObjectsSizeDiff()
meth public long getMaxValue()
meth public long getMinTrackedLiveObjectsSizeDiff()
meth public long getNTotalTracked()
meth public long getNTotalTrackedBytes()
meth public long getTimeTaken()
meth public long[] getNTrackedAllocObjects()
meth public long[] getObjectsSizePerClass()
meth public long[] getTrackedLiveObjectsSize()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createPresentationCCT(int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot
hfds avgObjectAge,classNames,maxSurvGen,maxTrackedLiveObjectsSizeDiff,minTrackedLiveObjectsSizeDiff,nClasses,nTotalAllocObjects,nTrackedAllocObjects,nTrackedLiveObjects,objectsSizePerClass,snapshot1,snapshot2,trackedLiveObjectsSize

CLSS public org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider,org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public float[] getAvgObjectAge()
meth public int getNAlloc()
meth public int getNInstrClasses()
meth public int getNTrackedItems()
meth public int[] getMaxSurvGen()
meth public int[] getNTrackedLiveObjects()
meth public int[] getnTotalAllocObjects()
meth public java.lang.String toString()
meth public long getMaxValue()
meth public long getNTotalTracked()
meth public long getNTotalTrackedBytes()
meth public long[] getNTrackedAllocObjects()
meth public long[] getTrackedLiveObjectsSize()
meth public org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot createDiff(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot)
meth public org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createPresentationCCT(int,boolean)
meth public void performInit(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot
hfds MEMORY_LIVENESS_MSG,avgObjectAge,currentEpoch,maxSurvGen,maxValue,nInstrClasses,nTotalAllocObjects,nTotalTracked,nTotalTrackedBytes,nTrackedAllocObjects,nTrackedItems,nTrackedLiveObjects,trackedLiveObjectsSize

CLSS public org.netbeans.lib.profiler.results.memory.MemoryCCTManager
cons public init(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot,int,boolean)
meth public boolean isEmpty()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode getRootNode()
supr java.lang.Object
hfds rootNode

CLSS public abstract interface org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
innr public abstract interface static Listener
innr public static ObjectNumbersContainer
intf org.netbeans.lib.profiler.results.CCTProvider
meth public abstract boolean classMarkedUnprofiled(int)
meth public abstract int getCurrentEpoch()
meth public abstract int getNProfiledClasses()
meth public abstract long[] getAllocObjectNumbers()
meth public abstract long[] getObjectsSizePerClass()
meth public abstract org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$ObjectNumbersContainer getLivenessObjectNumbers()
meth public abstract org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public abstract org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForClasses()
meth public abstract void beginTrans(boolean)
meth public abstract void endTrans()
meth public abstract void markClassUnprofiled(int)
meth public abstract void updateInternals()

CLSS public abstract interface static org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$Listener
 outer org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
intf org.netbeans.lib.profiler.results.CCTProvider$Listener

CLSS public static org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$ObjectNumbersContainer
 outer org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
fld public float[] avgObjectAge
fld public int nInstrClasses
fld public int[] maxSurvGen
fld public int[] nTrackedLiveObjects
fld public long[] nTrackedAllocObjects
fld public long[] trackedLiveObjectsSize
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.results.memory.MemoryCallGraphBuilder
cons public init()
intf org.netbeans.lib.profiler.results.memory.MemoryCCTProvider
intf org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener
meth protected org.netbeans.lib.profiler.results.RuntimeCCTNode getAppRootNode()
meth protected void doBatchStart()
meth protected void doBatchStop()
meth protected void doReset()
meth protected void doShutdown()
meth protected void doStartup(org.netbeans.lib.profiler.ProfilerClient)
meth public boolean classMarkedUnprofiled(int)
meth public int getCurrentEpoch()
meth public int getNProfiledClasses()
meth public long[] getAllocObjectNumbers()
meth public long[] getObjectsSizePerClass()
meth public org.netbeans.lib.profiler.results.memory.MemoryCCTProvider$ObjectNumbersContainer getLivenessObjectNumbers()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[] getStacksForClasses()
meth public void beginTrans(boolean)
meth public void endTrans()
meth public void markClassUnprofiled(int)
meth public void monitorEntry(int,long,long,int,int)
meth public void monitorExit(int,long,long,int)
meth public void newMonitor(int,java.lang.String)
meth public void newThread(int,java.lang.String,java.lang.String)
meth public void onAllocStackTrace(char,long,int[])
meth public void onGcPerformed(char,long,int)
meth public void onLivenessStackTrace(char,long,int,long,int[])
meth public void profilingPoint(int,int,long)
meth public void timeAdjust(int,long,long)
meth public void updateInternals()
supr org.netbeans.lib.profiler.results.BaseCallGraphBuilder
hfds avgObjectAge,currentEpoch,maxSurvGen,nProfiledClasses,nTrackedAllocObjects,nTrackedLiveObjects,objMap,objectsSizePerClass,stacksForClasses,transaction,unprofiledClass
hcls ObjIdToCCTNodeMap

CLSS public org.netbeans.lib.profiler.results.memory.MemoryDataFrameProcessor
cons public init()
meth public void doProcessDataFrame(java.nio.ByteBuffer)
supr org.netbeans.lib.profiler.results.locks.AbstractLockDataFrameProcessor

CLSS public abstract interface org.netbeans.lib.profiler.results.memory.MemoryProfilingResultsListener
intf org.netbeans.lib.profiler.results.locks.LockProfilingResultListener
meth public abstract void onAllocStackTrace(char,long,int[])
meth public abstract void onGcPerformed(char,long,int)
meth public abstract void onLivenessStackTrace(char,long,int,long,int[])

CLSS public abstract org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider,org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected abstract org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth protected void performInit(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public abstract org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot createDiff(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot)
meth public boolean containsStacks()
meth public int getNProfiledClasses()
meth public java.lang.String getClassName(int)
meth public java.lang.String[] getClassNames()
meth public long[] getObjectsSizePerClass()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean)
meth public void filterReverse(java.lang.String,int,int,boolean,org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode,int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.ResultsSnapshot
hfds classNames,nProfiledClasses,objectsSizePerClass,stacksForClasses,table

CLSS public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode
cons protected init(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
cons public init(java.lang.String,long,long)
fld protected char flags
fld public final static int SORT_BY_ALLOC_OBJ_NUMBER = 3
fld public final static int SORT_BY_ALLOC_OBJ_SIZE = 2
fld public final static int SORT_BY_NAME = 1
fld public final static java.lang.String VM_ALLOC_CLASS = "org.netbeans.lib.profiler.server.ProfilerRuntimeMemory"
fld public final static java.lang.String VM_ALLOC_METHOD = "traceVMObjectAlloc"
fld public long nCalls
fld public long totalObjSize
fld public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode parent
fld public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode[] children
meth protected boolean setFullClassAndMethodInfo(org.netbeans.lib.profiler.results.memory.JMethodIdTable)
meth protected final void setChildren(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode[])
meth protected static java.util.Collection<org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode> resolveChildren(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode)
meth protected static org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode generateMirrorNode(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
meth protected static void assignNamesToNodesFromSnapshot(org.netbeans.lib.profiler.results.memory.JMethodIdTable,org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode,java.lang.String)
meth protected static void assignNamesToNodesFromVM(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode,java.lang.String) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected static void checkMethodIdForNodeFromVM(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
meth protected void setupFilteredNode(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode)
meth public boolean equals(java.lang.Object)
meth public int getIndexOfChild(java.lang.Object)
meth public int getNChildren()
meth public int hashCode()
meth public java.lang.String getNodeName()
meth public java.lang.String toString()
meth public java.lang.String[] getMethodClassNameAndSig()
meth public org.netbeans.lib.profiler.results.CCTNode createFilteredNode()
meth public org.netbeans.lib.profiler.results.CCTNode getChild(int)
meth public org.netbeans.lib.profiler.results.CCTNode getParent()
meth public org.netbeans.lib.profiler.results.CCTNode[] getChildren()
meth public static org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCTFromSnapshot(org.netbeans.lib.profiler.results.memory.JMethodIdTable,org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,java.lang.String)
meth public static org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCTFromVM(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,java.lang.String) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public static org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode rootNode(org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode[])
meth public static void getNamesForMethodIdsFromVM(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode[]) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void exportCSVData(java.lang.String,int,org.netbeans.lib.profiler.results.ExportDataDumper)
meth public void exportHTMLData(org.netbeans.lib.profiler.results.ExportDataDumper,int)
meth public void exportXMLData(org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
meth public void sortChildren(int,boolean)
supr org.netbeans.lib.profiler.results.CCTNode
hfds UKNOWN_NODENAME,VM_ALLOC_TEXT,className,entry,methodId,methodName,nodeName
hcls Handle

CLSS public org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode
cons protected init(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
cons public init(java.lang.String,long,long,int,int,float,int)
fld public final static int SORT_BY_ALLOC_OBJ = 3
fld public final static int SORT_BY_AVG_AGE = 4
fld public final static int SORT_BY_LIVE_OBJ_NUMBER = 2
fld public final static int SORT_BY_LIVE_OBJ_SIZE = 1
fld public final static int SORT_BY_NAME = 6
fld public final static int SORT_BY_SURV_GEN = 5
fld public final static int SORT_BY_TOTAL_ALLOC_OBJ = 7
fld public float avgObjectAge
fld public int nLiveObjects
fld public int nTotalAllocObjects
fld public int survGen
meth protected static org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode generateMirrorNode(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,org.netbeans.lib.profiler.results.memory.SurvGenSet)
meth protected void setupFilteredNode(org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode)
meth public org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createFilteredNode()
meth public static org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCTFromVM(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,java.lang.String,int,boolean) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public static org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode createPresentationCCTFromSnapshot(org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot,org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,java.lang.String,int,boolean)
meth public static org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode rootNode(org.netbeans.lib.profiler.results.memory.PresoObjLivenessCCTNode[])
meth public void exportCSVData(java.lang.String,int,org.netbeans.lib.profiler.results.ExportDataDumper)
meth public void exportHTMLData(org.netbeans.lib.profiler.results.ExportDataDumper,int)
meth public void exportXMLData(org.netbeans.lib.profiler.results.ExportDataDumper,java.lang.String)
meth public void merge(org.netbeans.lib.profiler.results.CCTNode)
meth public void setDecimalFormat()
meth public void sortChildren(int,boolean)
supr org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode
hfds currentEpoch,dontShowZeroLiveObjNodes

CLSS public org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode
cons protected init()
cons public init(int)
fld protected final static int TYPE_RuntimeMemoryCCTNode = 1
fld protected final static int TYPE_RuntimeObjAllocTermCCTNode = 2
fld protected final static int TYPE_RuntimeObjLivenessTermCCTNode = 3
fld public int methodId
fld public java.lang.Object children
intf java.lang.Cloneable
intf org.netbeans.lib.profiler.results.RuntimeCCTNode
meth public int getType()
meth public java.lang.Object clone()
meth public org.netbeans.lib.profiler.results.RuntimeCCTNode[] getChildren()
meth public org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode addNewChild(int)
meth public static org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode create(int)
meth public void attachNodeAsChild(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr java.lang.Object
hfds EMPTY_CHILDREN

CLSS public org.netbeans.lib.profiler.results.memory.RuntimeObjAllocTermCCTNode
cons protected init()
cons public init(int)
fld public long nCalls
fld public long totalObjSize
meth public int getType()
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void updateForNewObject(long)
meth public void updateForRemovedObject(long)
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode

CLSS public org.netbeans.lib.profiler.results.memory.RuntimeObjLivenessTermCCTNode
cons protected init()
cons public init(int)
meth protected static void calculateNObjAndAge(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,int[])
meth protected static void calculateTotalNumberOfSurvGens(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,org.netbeans.lib.profiler.results.memory.SurvGenSet)
meth protected void dumpEpochs()
meth public int calculateTotalNLiveObjects()
meth public int getType()
meth public java.lang.Object clone()
meth public static float calculateAvgObjectAgeForAllPaths(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int)
meth public static int calculateTotalNumberOfSurvGensForAllPaths(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode)
meth public void addLiveObjectForEpoch(int)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void removeLiveObjectForEpoch(int)
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.RuntimeObjAllocTermCCTNode
hfds epochAndNLiveObjects

CLSS public org.netbeans.lib.profiler.results.memory.SampledMemoryResultsDiff
cons public init(org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot,org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot)
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public boolean containsStacks()
meth public int getNProfiledClasses()
meth public int[] getObjectsCounts()
meth public java.lang.String getClassName(int)
meth public java.lang.String[] getClassNames()
meth public long getBeginTime()
meth public long getMaxObjectsSizePerClassDiff()
meth public long getMinObjectsSizePerClassDiff()
meth public long getTimeTaken()
meth public long[] getObjectsSizePerClass()
meth public org.netbeans.lib.profiler.results.memory.JMethodIdTable getJMethodIdTable()
meth public org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(int,boolean)
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot
hfds maxObjectsSizePerClassDiff,minObjectsSizePerClassDiff,nClasses,objectsCounts

CLSS public org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot
cons public init()
cons public init(long,long,org.netbeans.lib.profiler.ProfilerClient) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth protected org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode createPresentationCCT(org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode,int,boolean)
meth public int[] getObjectsCounts()
meth public java.lang.String toString()
meth public org.netbeans.lib.profiler.results.memory.SampledMemoryResultsSnapshot createDiff(org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot)
meth public void performInit(org.netbeans.lib.profiler.ProfilerClient,org.netbeans.lib.profiler.results.memory.MemoryCCTProvider) throws org.netbeans.lib.profiler.client.ClientUtils$TargetAppOrVMTerminated
meth public void readFromStream(java.io.DataInputStream) throws java.io.IOException
meth public void writeToStream(java.io.DataOutputStream) throws java.io.IOException
supr org.netbeans.lib.profiler.results.memory.MemoryResultsSnapshot
hfds MEMORY_SAMPLED_MSG,liveObjectsCounts

CLSS public org.netbeans.lib.profiler.results.memory.SurvGenSet
cons public init()
meth public int getTotalNoOfAges()
meth public void addAge(int)
meth public void mergeWith(org.netbeans.lib.profiler.results.memory.SurvGenSet)
supr java.lang.Object
hfds age,limit,nEls,nSlots

CLSS public org.netbeans.lib.profiler.results.monitor.VMTelemetryDataManager
cons public init()
cons public init(int)
fld public long maxHeapSize
fld public long[] freeMemory
fld public long[] lastGCPauseInMS
fld public long[] loadedClassesCount
fld public long[] nSurvivingGenerations
fld public long[] nSystemThreads
fld public long[] nTotalThreads
fld public long[] nUserThreads
fld public long[] processCPUTimeInPromile
fld public long[] relativeGCTimeInPerMil
fld public long[] timeStamps
fld public long[] totalMemory
fld public long[] usedMemory
fld public long[][] gcFinishs
fld public long[][] gcStarts
meth public int getArrayBufferSize()
meth public int getItemCount()
meth public org.netbeans.lib.profiler.client.MonitoredData getLastData()
meth public void processData(org.netbeans.lib.profiler.client.MonitoredData)
meth public void reset()
meth public void setArrayBufferSize(int)
supr org.netbeans.lib.profiler.results.DataManager
hfds arrayBufferSize,currentArraysSize,firstStart,itemCount,lastData,lastUnpairedStart

CLSS public org.netbeans.lib.profiler.results.threads.ThreadData
cons public init(java.lang.String,java.lang.String)
meth public byte getFirstState()
meth public byte getLastState()
meth public byte getStateAt(int)
meth public int size()
meth public java.awt.Color getThreadStateColorAt(int)
meth public java.lang.String getClassName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public long getFirstTimeStamp()
meth public long getLastTimeStamp()
meth public long getMonitorTime(long)
meth public long getParkTime(long)
meth public long getRunningTime(long)
meth public long getSleepingTime(long)
meth public long getTimeStampAt(int)
meth public long getTotalTime(long)
meth public long getWaitTime(long)
meth public static boolean isAliveState(int)
meth public static java.awt.Color getThreadStateColor(int)
meth public void add(long,byte)
meth public void clearStates()
supr java.lang.Object
hfds NO_STATE,capacity,className,curSize,dataLock,name,threadStates,timeStamps,times

CLSS public org.netbeans.lib.profiler.results.threads.ThreadDump
cons public init(boolean,java.util.Date,java.lang.Object[])
meth public boolean isJDK15()
meth public java.lang.String toString()
meth public java.lang.management.ThreadInfo[] getThreads()
meth public java.util.Date getTime()
supr java.lang.Object
hfds cdThreads,jdk15,time,tinfoLock,tinfos

CLSS public org.netbeans.lib.profiler.results.threads.ThreadsDataManager
cons public init()
meth public boolean hasData()
meth public boolean supportsSleepingStateMonitoring()
meth public int getThreadsCount()
meth public java.lang.String getThreadClassName(int)
meth public java.lang.String getThreadName(int)
meth public long getEndTime()
meth public long getStartTime()
meth public org.netbeans.lib.profiler.results.threads.ThreadData getThreadData(int)
meth public void processData(org.netbeans.lib.profiler.client.MonitoredData)
meth public void reset()
meth public void resetStates()
meth public void setSupportsSleepingStateMonitoring(boolean)
meth public void setThreadsMonitoringEnabled(boolean)
supr org.netbeans.lib.profiler.results.DataManager
hfds endTime,idToIndex,startTime,supportsSleepingState,threadData,threadsMonitoringEnabled

CLSS public org.netbeans.lib.profiler.utils.CharStack
cons public init()
meth public boolean isEmpty()
meth public char peek()
meth public char pop()
meth public void clear()
meth public void push(char)
supr java.lang.Object
hfds data,loadFactor,maxCapacity,stackPointer

CLSS public org.netbeans.lib.profiler.utils.FileOrZipEntry
cons public init(java.io.File)
cons public init(java.lang.String,java.lang.String)
meth public boolean isFile()
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String getFullName()
meth public java.lang.String getName()
meth public long getLength() throws java.io.IOException
supr java.lang.Object
hfds dirOrJar,file,fileName,isZipEntry,len

CLSS public org.netbeans.lib.profiler.utils.FloatSorter
cons public init(float[],int,int)
meth protected void swap(int,int)
meth public void sort(boolean)
supr java.lang.Object
hfds len,off,x

CLSS public org.netbeans.lib.profiler.utils.ImmutableList
cons public init()
intf java.lang.Iterable
meth public int size()
meth public java.lang.Object get(int)
meth public java.lang.Object get(java.lang.Object)
meth public java.util.Iterator iterator()
meth public static void main(java.lang.String[])
meth public void add(java.lang.Object)
meth public void clear()
supr java.lang.Object
hfds availableSize,currentIndex,currentSlot,distributionMap,initialSize,loadFactor,size,slotCount,slotInitialSize,slotLimits,slotsGuard,storageSlots
hcls InnerIterator,LoadFactor

CLSS public org.netbeans.lib.profiler.utils.IntSorter
cons public init(int[],int,int)
meth protected void swap(int,int)
meth public void sort(boolean)
supr java.lang.Object
hfds len,off,x

CLSS public org.netbeans.lib.profiler.utils.IntVector
cons public init()
cons public init(int)
meth public int get(int)
meth public int size()
meth public void add(int)
meth public void clear()
supr java.lang.Object
hfds size,vec

CLSS public org.netbeans.lib.profiler.utils.LongSorter
cons public init(long[],int,int)
meth protected void swap(int,int)
meth public void sort(boolean)
supr java.lang.Object
hfds len,off,x

CLSS public org.netbeans.lib.profiler.utils.MiscUtils
cons public init()
intf org.netbeans.lib.profiler.global.CommonConstants
meth public static boolean containsDirectoryOnPath(java.lang.String,java.lang.String)
meth public static boolean fileForNameOk(java.lang.String)
meth public static boolean inSamePackage(java.lang.String,java.lang.String)
meth public static boolean isSlashedJavaCoreClassName(java.lang.String)
meth public static boolean isSupportedJVM(java.util.Map)
meth public static boolean isSupportedRunningJVMVersion(java.lang.String)
meth public static byte[] readFileIntoBuffer(org.netbeans.lib.profiler.utils.FileOrZipEntry) throws java.io.IOException
meth public static java.io.File checkDirForName(java.lang.String) throws java.io.IOException
meth public static java.io.File checkFile(java.io.File,boolean) throws java.io.IOException
meth public static java.io.File checkFileForName(java.lang.String) throws java.io.IOException
meth public static java.lang.String getAbsoluteFilePath(java.lang.String,java.lang.String)
meth public static java.lang.String getCanonicalPath(java.io.File)
meth public static java.lang.String getClassNameForSource(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getFirstPathComponent(java.lang.String)
meth public static java.lang.String getJDKVersionForJVMExeFile(java.io.File) throws java.io.IOException
meth public static java.lang.String getLiveClassPathSubset(java.lang.String,java.lang.String)
meth public static java.util.List getPathComponents(java.lang.String,boolean,java.lang.String)
meth public static void deleteHeapTempFiles()
meth public static void getAllClassesInDir(java.lang.String,java.lang.String,boolean,java.util.Collection)
meth public static void getAllClassesInJar(java.lang.String,boolean,java.util.Collection)
meth public static void internalError(java.lang.String)
meth public static void printErrorMessage(java.lang.String)
meth public static void printInfoMessage(java.lang.String)
meth public static void printVerboseInfoMessage(java.lang.String)
meth public static void printVerboseInfoMessage(java.lang.String[])
meth public static void printWarningMessage(java.lang.String)
meth public static void setSilent(boolean)
meth public static void setVerbosePrint()
supr java.lang.Object
hfds FILE_NOT_EXIST_MSG,FILE_NOT_READABLE_MSG,NOT_DIRECTORY_MSG,NOT_FILE_MSG,VM_INCOMPATIBLE_MSG,VM_UNKNOWN_MSG,VM_VERSION_MSG,printInfo,verbosePrint

CLSS public org.netbeans.lib.profiler.utils.StringSorter
cons public init(java.lang.String[],int,int)
meth protected void swap(int,int)
meth public void sort(boolean)
supr java.lang.Object
hfds len,off,x

CLSS public org.netbeans.lib.profiler.utils.StringUtils
cons public init()
meth public static java.lang.String floatPerCentToString(float)
meth public static java.lang.String formatFullDate(java.util.Date)
meth public static java.lang.String formatUserDate(java.util.Date)
meth public static java.lang.String mcsTimeToString(long)
meth public static java.lang.String nBytesToString(long)
meth public static java.lang.String userFormClassName(java.lang.String)
meth public static java.lang.String utf8ToString(byte[],int,int)
meth public static java.lang.String[] parseArgsString(java.lang.String)
meth public static java.lang.String[][] convertPackedStringsIntoStringArrays(byte[],int[],int)
meth public static void appendSplittedLongString(java.lang.StringBuffer,java.lang.String,int)
supr java.lang.Object
hfds LAST_WEEK_FORMAT,MCS_ZERO,SEPARATOR,THIS_WEEK_FORMAT,YESTERDAY_FORMAT,fullFormat,intFormat,lastWeekFormat,otherFormat,percentage,thisWeekFormat,todayFormat,yesterdayFormat

CLSS public org.netbeans.lib.profiler.utils.VMUtils
cons public init()
fld public final static char BOOLEAN = 'Z'
fld public final static char BYTE = 'B'
fld public final static char CHAR = 'C'
fld public final static char DOUBLE = 'D'
fld public final static char FLOAT = 'F'
fld public final static char INT = 'I'
fld public final static char LONG = 'J'
fld public final static char REFERENCE = 'L'
fld public final static char SHORT = 'S'
fld public final static char VOID = 'V'
fld public final static java.lang.String BOOLEAN_CODE = "Z"
fld public final static java.lang.String BOOLEAN_STRING = "boolean"
fld public final static java.lang.String BYTE_CODE = "B"
fld public final static java.lang.String BYTE_STRING = "byte"
fld public final static java.lang.String CHAR_CODE = "C"
fld public final static java.lang.String CHAR_STRING = "char"
fld public final static java.lang.String DOUBLE_CODE = "D"
fld public final static java.lang.String DOUBLE_STRING = "double"
fld public final static java.lang.String FLOAT_CODE = "F"
fld public final static java.lang.String FLOAT_STRING = "float"
fld public final static java.lang.String INT_CODE = "I"
fld public final static java.lang.String INT_STRING = "int"
fld public final static java.lang.String LONG_CODE = "J"
fld public final static java.lang.String LONG_STRING = "long"
fld public final static java.lang.String SHORT_CODE = "S"
fld public final static java.lang.String SHORT_STRING = "short"
fld public final static java.lang.String VOID_CODE = "V"
fld public final static java.lang.String VOID_STRING = "void"
meth public static boolean isPrimitiveType(java.lang.String)
meth public static boolean isVMPrimitiveType(java.lang.String)
meth public static java.lang.String typeToVMSignature(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.utils.Wildcards
cons public init()
fld public final static java.lang.String ALLWILDCARD = "*"
meth public static boolean isMethodWildcard(java.lang.String)
meth public static boolean isPackageWildcard(java.lang.String)
meth public static boolean matchesWildcard(java.lang.String,java.lang.String)
meth public static java.lang.String unwildPackage(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.utils.formatting.DefaultMethodNameFormatter
cons public init()
cons public init(int)
fld public final static int VERBOSITY_CLASS = 1
fld public final static int VERBOSITY_CLASSMETHOD = 3
fld public final static int VERBOSITY_FULLCLASSMETHOD = 5
fld public final static int VERBOSITY_FULLMETHOD = 4
fld public final static int VERBOSITY_METHOD = 2
intf org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter
meth public org.netbeans.lib.profiler.utils.formatting.Formattable formatMethodName(java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.lib.profiler.utils.formatting.Formattable formatMethodName(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)
supr java.lang.Object
hfds VERBOSITY_DEFAULT,VERBOSITY_MAX,VERBOSITY_MIN,verbosity

CLSS public abstract interface org.netbeans.lib.profiler.utils.formatting.Formattable
meth public abstract java.lang.String toFormatted()

CLSS public abstract interface org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter
meth public abstract org.netbeans.lib.profiler.utils.formatting.Formattable formatMethodName(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.netbeans.lib.profiler.utils.formatting.Formattable formatMethodName(org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection)

CLSS public org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory
meth public org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter getFormatter()
meth public org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter getFormatter(org.netbeans.lib.profiler.marker.Mark)
meth public static org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory getDefault()
meth public static org.netbeans.lib.profiler.utils.formatting.MethodNameFormatterFactory getDefault(org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter)
meth public void registerFormatter(org.netbeans.lib.profiler.marker.Mark,org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter)
supr java.lang.Object
hfds defaultFormatter,formatterMap,instance

CLSS public org.netbeans.lib.profiler.utils.formatting.PlainFormattableMethodName
intf org.netbeans.lib.profiler.utils.formatting.Formattable
meth public java.lang.String getFormattedClass()
meth public java.lang.String getFormattedClassAndMethod()
meth public java.lang.String getFormattedMethod()
meth public java.lang.String getFullFormattedClassAndMethod()
meth public java.lang.String getFullFormattedMethod()
meth public java.lang.String getParamsString()
meth public java.lang.String getReturnTypeX()
meth public java.lang.String toFormatted()
meth public java.lang.String toString()
supr java.lang.Object
hfds LOGGER,className,methodName,params,returnType,verbosity

CLSS public org.netbeans.lib.profiler.wireprotocol.AsyncMessageCommand
cons public init(boolean,java.lang.String)
meth public boolean isPositive()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds message,positive

CLSS public org.netbeans.lib.profiler.wireprotocol.CalibrationDataResponse
cons public init(double[],double[],double[],long[])
meth public double[] getMethodEntryExitCallTime()
meth public double[] getMethodEntryExitInnerTime()
meth public double[] getMethodEntryExitOuterTime()
meth public java.lang.String toString()
meth public long[] getTimerCountsInSecond()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds methodEntryExitCallTime,methodEntryExitInnerTime,methodEntryExitOuterTime,timerCountsInSecond

CLSS public org.netbeans.lib.profiler.wireprotocol.ClassLoadedCommand
cons public init(java.lang.String,int[],byte[],boolean)
meth public boolean getThreadInCallGraph()
meth public byte[] getClassFileBytes()
meth public int[] getThisAndParentLoaderData()
meth public java.lang.String getClassName()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classFileBytes,className,thisAndParentLoaderData,threadInCallGraph

CLSS public org.netbeans.lib.profiler.wireprotocol.CodeRegionCPUResultsResponse
cons public init(long[])
meth public java.lang.String toString()
meth public long[] getResults()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds results

CLSS public org.netbeans.lib.profiler.wireprotocol.Command
cons public init(int)
fld public final static int CHECK_CONNECTION = 1
fld public final static int CLASS_LOADED = 3
fld public final static int CLASS_LOADER_UNLOADING = 36
fld public final static int CPU_RESULTS_EXIST = 6
fld public final static int DEACTIVATE_INJECTED_CODE = 9
fld public final static int DEINSTRUMENT_REFLECTION = 21
fld public final static int DETACH = 25
fld public final static int DUMP_EXISTING_RESULTS = 27
fld public final static int DUMP_EXISTING_RESULTS_LIVE = 40
fld public final static int EVENT_BUFFER_DUMPED = 26
fld public final static int GET_CLASSID = 42
fld public final static int GET_CLASS_FILE_BYTES = 47
fld public final static int GET_CODE_REGION_CPU_RESULTS = 8
fld public final static int GET_DEFINING_CLASS_LOADER = 35
fld public final static int GET_HEAP_HISTOGRAM = 45
fld public final static int GET_INTERNAL_STATS = 24
fld public final static int GET_METHOD_NAMES_FOR_JMETHOD_IDS = 31
fld public final static int GET_MONITORED_NUMBERS = 32
fld public final static int GET_OBJECT_ALLOCATION_RESULTS = 30
fld public final static int GET_STORED_CALIBRATION_DATA = 37
fld public final static int GET_THREAD_LIVENESS_STATUS = 16
fld public final static int GET_VM_PROPERTIES = 28
fld public final static int INITIATE_PROFILING = 13
fld public final static int INSTRUMENT_METHOD_GROUP = 7
fld public final static int INSTRUMENT_REFLECTION = 20
fld public final static int MESSAGE = 14
fld public final static int METHOD_INVOKED_FIRST_TIME = 23
fld public final static int METHOD_LOADED = 22
fld public final static int PREPARE_DETACH = 44
fld public final static int RESET_PROFILER_COLLECTORS = 29
fld public final static int RESULTS_AVAILABLE = 38
fld public final static int RESUME_TARGET_APP = 11
fld public final static int ROOT_CLASS_LOADED = 17
fld public final static int RUN_CALIBRATION_AND_GET_DATA = 34
fld public final static int RUN_GC = 33
fld public final static int SET_CHANGEABLE_INSTR_PARAMS = 4
fld public final static int SET_UNCHANGEABLE_INSTR_PARAMS = 5
fld public final static int SHUTDOWN_COMPLETED = 19
fld public final static int SHUTDOWN_INITIATED = 18
fld public final static int SHUTDOWN_OK = 15
fld public final static int START_TARGET_APP = 2
fld public final static int STILL_ALIVE = 43
fld public final static int SUSPEND_TARGET_APP = 10
fld public final static int TAKE_HEAP_DUMP = 41
fld public final static int TAKE_SNAPSHOT = 39
fld public final static int TAKE_THREAD_DUMP = 46
fld public final static int TERMINATE_TARGET_JVM = 12
meth public int getType()
meth public java.lang.String toString()
meth public static java.lang.String cmdTypeToString(int)
supr java.lang.Object
hfds type

CLSS public org.netbeans.lib.profiler.wireprotocol.DefiningLoaderResponse
cons public init(int)
meth public int getLoaderId()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds loaderId

CLSS public org.netbeans.lib.profiler.wireprotocol.DumpResultsResponse
cons public init(boolean,long)
meth public java.lang.String toString()
meth public long getDumpAbsTimeStamp()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds dumpAbsTimeStamp

CLSS public org.netbeans.lib.profiler.wireprotocol.EventBufferDumpedCommand
cons public init(int,byte[],int)
cons public init(int,java.lang.String)
meth public byte[] getBuffer()
meth public int getBufSize()
meth public java.lang.String getEventBufferFileName()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds bufSize,buffer,eventBufferFileName,startPos

CLSS public org.netbeans.lib.profiler.wireprotocol.GetClassFileBytesCommand
cons public init(java.lang.String[],int[])
meth public int[] getClassLoaderIds()
meth public java.lang.String toString()
meth public java.lang.String[] getClasses()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classLoaderIds,classes

CLSS public org.netbeans.lib.profiler.wireprotocol.GetClassFileBytesResponse
cons public init(byte[][])
meth public byte[][] getClassBytes()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds classBytes

CLSS public org.netbeans.lib.profiler.wireprotocol.GetClassIdCommand
cons public init(java.lang.String,int)
meth public int getClassLoaderId()
meth public java.lang.String getClassName()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classLoaderId,className

CLSS public org.netbeans.lib.profiler.wireprotocol.GetClassIdResponse
cons public init(boolean,int)
meth public int getClassId()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds classId

CLSS public org.netbeans.lib.profiler.wireprotocol.GetDefiningClassLoaderCommand
cons public init(java.lang.String,int)
meth public int getClassLoaderId()
meth public java.lang.String getClassName()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classLoaderId,className

CLSS public org.netbeans.lib.profiler.wireprotocol.GetMethodNamesForJMethodIdsCommand
cons public init(int[])
meth public int[] getMethodIds()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds methodIds

CLSS public org.netbeans.lib.profiler.wireprotocol.HeapHistogramResponse
cons public init(java.util.Date,java.lang.String[],int[],int[],long[],long[])
meth public int[] getIds()
meth public int[] getNewids()
meth public java.lang.String[] getNewNames()
meth public java.util.Date getTime()
meth public long[] getBytes()
meth public long[] getInstances()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds bytes,ids,instances,newNames,newids,time

CLSS public org.netbeans.lib.profiler.wireprotocol.InitiateProfilingCommand
cons public init(int)
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,boolean,boolean)
cons public init(int,java.lang.String[],int[],java.lang.String[],java.lang.String[],boolean,boolean)
meth public boolean getInstrSpawnedThreads()
meth public boolean isStartProfilingPointsActive()
meth public int getInstrType()
meth public int[] getProfilingPointIDs()
meth public java.lang.String getRootClassName()
meth public java.lang.String toString()
meth public java.lang.String[] getProfilingPointHandlers()
meth public java.lang.String[] getProfilingPointInfos()
meth public java.lang.String[] getRootClassNames()
meth public void setInstrType(int)
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classNames,instrSpawnedThreads,instrType,profilingPointHandlers,profilingPointIDs,profilingPointInfos,startProfilingPointsActive

CLSS public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupCommand
cons public init(int,java.lang.String[],int[],byte[][],boolean[],int)
cons public init(java.lang.Object)
meth public boolean isEmpty()
meth public boolean[] getInstrMethodLeaf()
meth public byte[][] getReplacementClassFileBytes()
meth public int getInstrType()
meth public int[] getClassLoaderIds()
meth public java.lang.String toString()
meth public java.lang.String[] getMethodClasses()
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupData getBase()
meth public void dump()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds b,instrType

CLSS public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupData
cons public init(java.lang.String[],int[],byte[][],boolean[],int)
fld protected boolean[] instrMethodLeaf
fld protected byte[][] replacementClassFileBytes
fld protected int addInfo
fld protected int nClasses
fld protected int nMethods
fld protected int[] instrMethodClassLoaderIds
fld protected java.lang.String[] instrMethodClasses
meth public boolean[] getInstrMethodLeaf()
meth public byte[][] getReplacementClassFileBytes()
meth public int getAddInfo()
meth public int getNClasses()
meth public int getNMethods()
meth public int[] getClassLoaderIds()
meth public java.lang.String toString()
meth public java.lang.String[] getMethodClasses()
meth public void dump()
supr java.lang.Object

CLSS public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupResponse
cons public init(java.lang.Object)
cons public init(java.lang.String[],int[],byte[][],boolean[],int)
meth public boolean isEmpty()
meth public boolean[] getInstrMethodLeaf()
meth public byte[][] getReplacementClassFileBytes()
meth public int[] getClassLoaderIds()
meth public java.lang.String toString()
meth public java.lang.String[] getMethodClasses()
meth public org.netbeans.lib.profiler.wireprotocol.InstrumentMethodGroupData getBase()
meth public void dump()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds b

CLSS public org.netbeans.lib.profiler.wireprotocol.InternalStatsResponse
cons public init()
fld public double averageHotswappingTime
fld public double clientDataProcTime
fld public double clientInstrTime
fld public double maxHotswappingTime
fld public double methodEntryExitCallTime0
fld public double methodEntryExitCallTime1
fld public double methodEntryExitCallTime2
fld public double minHotswappingTime
fld public double totalHotswappingTime
fld public int nClassLoads
fld public int nEmptyInstrMethodGroupResponses
fld public int nFirstMethodInvocations
fld public int nNonEmptyInstrMethodGroupResponses
fld public int nSingleMethodInstrMethodGroupResponses
fld public int nTotalInstrMethods
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response

CLSS public org.netbeans.lib.profiler.wireprotocol.MethodInvokedFirstTimeCommand
cons public init(char)
meth public int getMethodId()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds methodId

CLSS public org.netbeans.lib.profiler.wireprotocol.MethodLoadedCommand
cons public init(java.lang.String,int,java.lang.String,java.lang.String)
meth public int getClassLoaderId()
meth public java.lang.String getClassName()
meth public java.lang.String getMethodName()
meth public java.lang.String getMethodSignature()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds classLoaderId,className,methodName,methodSignature

CLSS public org.netbeans.lib.profiler.wireprotocol.MethodNamesResponse
cons public init(byte[],int[])
meth public byte[] getPackedData()
meth public int[] getPackedArrayOffsets()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds packedArrayOffsets,packedData

CLSS public org.netbeans.lib.profiler.wireprotocol.MonitoredNumbersResponse
cons public init(long[],int,int)
fld public final static int CPU_TIME_IDX = 8
fld public final static int FREE_MEMORY_IDX = 0
fld public final static int GC_PAUSE_IDX = 6
fld public final static int GC_TIME_IDX = 5
fld public final static int GENERAL_NUMBERS_SIZE = 10
fld public final static int LOADED_CLASSES_IDX = 7
fld public final static int SURVIVING_GENERATIONS_IDX = 4
fld public final static int SYSTEM_THREADS_IDX = 3
fld public final static int TIMESTAMP_IDX = 9
fld public final static int TOTAL_MEMORY_IDX = 1
fld public final static int USER_THREADS_IDX = 2
meth public byte[] getExactThreadStates()
meth public byte[] getThreadStates()
meth public int getNNewThreads()
meth public int getNThreadStates()
meth public int getNThreads()
meth public int getServerProgress()
meth public int getServerState()
meth public int getThreadsDataMode()
meth public int[] getExactThreadIds()
meth public int[] getNewThreadIds()
meth public int[] getThreadIds()
meth public java.lang.String toString()
meth public java.lang.String[] getNewThreadClassNames()
meth public java.lang.String[] getNewThreadNames()
meth public long[] getExactStateTimestamps()
meth public long[] getGCFinishs()
meth public long[] getGCStarts()
meth public long[] getGeneralMonitoredNumbers()
meth public long[] getStateTimestamps()
meth public void setDataOnNewThreads(int,int[],java.lang.String[],java.lang.String[])
meth public void setDataOnThreads(int,int,int[],long[],byte[])
meth public void setExplicitDataOnThreads(int[],byte[],long[])
meth public void setGCstartFinishData(long[],long[])
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds exactThreadIds,exactThreadStates,exactTimeStamps,gcFinishs,gcStarts,generalNumbers,mode,nNewThreads,nThreadStates,nThreads,newThreadClassNames,newThreadIds,newThreadNames,serverProgress,serverState,stateTimestamps,threadIds,threadStates

CLSS public org.netbeans.lib.profiler.wireprotocol.ObjectAllocationResultsResponse
cons public init(int[],int)
meth public int[] getResults()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds nEntries,results

CLSS public org.netbeans.lib.profiler.wireprotocol.Response
cons protected init(boolean,int)
cons public init(boolean)
cons public init(java.lang.String)
fld protected boolean yes
fld protected java.lang.String errorMessage
fld public final static int CALIBRATION_DATA = 11
fld public final static int CLASSID_RESPONSE = 12
fld public final static int CODE_REGION_CPU_RESULTS = 1
fld public final static int DEFINING_LOADER = 10
fld public final static int DUMP_RESULTS = 5
fld public final static int GET_CLASS_FILE_BYTES_RESPONSE = 15
fld public final static int HEAP_HISTOGRAM = 13
fld public final static int INSTRUMENT_METHOD_GROUP = 2
fld public final static int INTERNAL_STATS = 3
fld public final static int METHOD_NAMES = 7
fld public final static int MONITORED_NUMBERS = 9
fld public final static int NO_TYPE = 0
fld public final static int OBJECT_ALLOCATION_RESULTS = 6
fld public final static int THREAD_DUMP = 14
fld public final static int THREAD_LIVENESS_STATUS = 8
fld public final static int VM_PROPERTIES = 4
meth public boolean isOK()
meth public boolean yes()
meth public int getType()
meth public java.lang.String getErrorMessage()
meth public java.lang.String toString()
supr java.lang.Object
hfds type

CLSS public org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand
cons public init(java.lang.String[],int[],byte[][],int[],int[][],int,int[])
meth public byte[][] getCachedClassFileBytes()
meth public int[] getAllLoadedClassLoaderIds()
meth public int[] getAllLoaderSuperClassIds()
meth public int[] getParentLoaderIds()
meth public int[][] getAllLoadedInterfaceIds()
meth public java.lang.String toString()
meth public java.lang.String[] getAllLoadedClassNames()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds allLoadedClassLoaderIds,allLoadedClassNames,allLoadedClassesInterfaces,allLoadedClassesSuper,cachedClassFileBytes,classCount,parentLoaderIds

CLSS public org.netbeans.lib.profiler.wireprotocol.SetChangeableInstrParamsCommand
cons public init(boolean,int,int,int,int,int,boolean,boolean,boolean,boolean,int)
meth public boolean getRunGCOnGetResultsInMemoryProfiling()
meth public boolean isLockContentionMonitoringEnabled()
meth public boolean isSleepTrackingEnabled()
meth public boolean isThreadsSamplingEnabled()
meth public boolean isWaitTrackingEnabled()
meth public int getNProfiledThreadsLimit()
meth public int getObjAllocStackSamplingDepth()
meth public int getObjAllocStackSamplingInterval()
meth public int getSamplingInterval()
meth public int getStackDepthLimit()
meth public int getThreadsSamplingFrequency()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds lockContentionMonitoringEnabled,nProfiledThreadsLimit,objAllocStackSamplingDepth,objAllocStackSamplingInterval,runGCOnGetResultsInMemoryProfiling,samplingInterval,sleepTrackingEnabled,stackDepthLimit,threadsSamplingEnabled,threadsSamplingFrequency,waitTrackingEnabled

CLSS public org.netbeans.lib.profiler.wireprotocol.SetUnchangeableInstrParamsCommand
cons public init(boolean,boolean,boolean,int,int)
meth public boolean getAbsoluteTimerOn()
meth public boolean getRemoteProfiling()
meth public boolean getThreadCPUTimerOn()
meth public int getCodeRegionCPUResBufSize()
meth public int getInstrScheme()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds absoluteTimerOn,codeRegionCPUResBufSize,instrScheme,remoteProfiling,threadCPUTimerOn

CLSS public org.netbeans.lib.profiler.wireprotocol.TakeHeapDumpCommand
cons public init(java.lang.String)
meth public java.lang.String getOutputFile()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Command
hfds outputFile

CLSS public org.netbeans.lib.profiler.wireprotocol.ThreadDumpResponse
cons public init(boolean,java.util.Date,java.lang.Object[])
meth public boolean isJDK15()
meth public java.lang.Object[] getThreads()
meth public java.util.Date getTime()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds cdThreads,jdk15,time

CLSS public org.netbeans.lib.profiler.wireprotocol.ThreadLivenessStatusResponse
cons public init(byte[])
meth public byte[] getStatus()
meth public java.lang.String toString()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds status

CLSS public org.netbeans.lib.profiler.wireprotocol.VMPropertiesResponse
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,long,long,long,int)
meth public boolean canInstrumentConstructor()
meth public int getAgentId()
meth public int getAgentVersion()
meth public java.lang.String getBootClassPath()
meth public java.lang.String getJDKVersionString()
meth public java.lang.String getJVMArguments()
meth public java.lang.String getJavaClassPath()
meth public java.lang.String getJavaCommand()
meth public java.lang.String getJavaExtDirs()
meth public java.lang.String getTargetMachineOSName()
meth public java.lang.String getWorkingDir()
meth public java.lang.String toString()
meth public long getMaxHeapSize()
meth public long getStartupTimeInCounts()
meth public long getStartupTimeMillis()
supr org.netbeans.lib.profiler.wireprotocol.Response
hfds agentId,agentVersion,bootClassPath,canInstrumentConstructor,javaClassPath,javaCommand,javaExtDirs,jdkVersionString,jvmArguments,maxHeapSize,startupTimeInCounts,startupTimeMillis,targetMachineOSName,workingDir

CLSS public org.netbeans.lib.profiler.wireprotocol.WireIO
cons public init(java.io.ObjectOutputStream,java.io.ObjectInputStream)
meth public java.lang.Object receiveCommandOrResponse() throws java.io.IOException
meth public long wasAlive()
meth public void sendComplexCommand(org.netbeans.lib.profiler.wireprotocol.Command) throws java.io.IOException
meth public void sendComplexResponse(org.netbeans.lib.profiler.wireprotocol.Response) throws java.io.IOException
meth public void sendSimpleCommand(int) throws java.io.IOException
meth public void sendSimpleResponse(boolean,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds DEBUG,IS_COMPLEX_COMMAND,IS_COMPLEX_RESPONSE,IS_SIMPLE_COMMAND,IS_SIMPLE_RESPONSE,in,out,wasAlive

