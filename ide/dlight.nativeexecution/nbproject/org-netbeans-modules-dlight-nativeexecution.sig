#Signature file v4.1
#Version 1.62.0

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract java.lang.Process
cons public init()
meth public abstract int exitValue()
meth public abstract int waitFor() throws java.lang.InterruptedException
meth public abstract java.io.InputStream getErrorStream()
meth public abstract java.io.InputStream getInputStream()
meth public abstract java.io.OutputStream getOutputStream()
meth public abstract void destroy()
meth public boolean isAlive()
meth public boolean waitFor(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public java.lang.Process destroyForcibly()
supr java.lang.Object

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public javax.swing.event.ChangeEvent
cons public init(java.lang.Object)
supr java.util.EventObject

CLSS public abstract interface org.netbeans.api.extexecution.input.LineProcessor
 anno 0 java.lang.Deprecated()
intf java.io.Closeable
meth public abstract void close()
meth public abstract void processLine(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void reset()

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.ExecutionEnvironment
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isLocal()
meth public abstract boolean isRemote()
meth public abstract int getSSHPort()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getHostAddress()
meth public abstract java.lang.String getUser()
meth public abstract java.lang.String toString()
meth public abstract void prepareForConnection() throws java.io.IOException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException

CLSS public org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory
meth public static java.lang.String toUniqueID(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static org.netbeans.modules.nativeexecution.api.ExecutionEnvironment createNew(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.ExecutionEnvironment createNew(java.lang.String,java.lang.String,int)
meth public static org.netbeans.modules.nativeexecution.api.ExecutionEnvironment fromUniqueID(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.ExecutionEnvironment getLocal()
meth public static org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService getDefault()
supr java.lang.Object
hfds allFactories,defaultFactory,ll,lookupResult

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.ExecutionListener
fld public final static int UNKNOWN_PID = -1
intf java.util.EventListener
meth public abstract void executionFinished(int)
meth public abstract void executionStarted(int)

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.HostInfo
innr public abstract interface static OS
innr public final static !enum Bitness
innr public final static !enum CpuFamily
innr public final static !enum OSFamily
meth public abstract int getCpuNum()
meth public abstract int getGroupId()
meth public abstract int getUserId()
meth public abstract int[] getAllGroupIDs()
meth public abstract java.io.File getTempDirFile()
meth public abstract java.io.File getUserDirFile()
meth public abstract java.lang.String getEnvironmentFile()
meth public abstract java.lang.String getGroup()
meth public abstract java.lang.String getHostname()
meth public abstract java.lang.String getLoginShell()
meth public abstract java.lang.String getShell()
meth public abstract java.lang.String getTempDir()
meth public abstract java.lang.String getUserDir()
meth public abstract java.lang.String[] getAllGroups()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getEnvironment()
meth public abstract long getClockSkew()
meth public abstract org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily getCpuFamily()
meth public abstract org.netbeans.modules.nativeexecution.api.HostInfo$OS getOS()
meth public abstract org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily getOSFamily()

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.HostInfo$Bitness
 outer org.netbeans.modules.nativeexecution.api.HostInfo
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$Bitness _32
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$Bitness _64
meth public java.lang.String toString()
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$Bitness valueOf(int)
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$Bitness valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$Bitness[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.HostInfo$Bitness>

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily
 outer org.netbeans.modules.nativeexecution.api.HostInfo
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily AARCH64
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily ARM
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily SPARC
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily UNKNOWN
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily X86
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.HostInfo$CpuFamily>

CLSS public abstract interface static org.netbeans.modules.nativeexecution.api.HostInfo$OS
 outer org.netbeans.modules.nativeexecution.api.HostInfo
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()
meth public abstract org.netbeans.modules.nativeexecution.api.HostInfo$Bitness getBitness()
meth public abstract org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily getFamily()

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily
 outer org.netbeans.modules.nativeexecution.api.HostInfo
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily FREEBSD
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily LINUX
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily MACOSX
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily SUNOS
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily UNKNOWN
fld public final static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily WINDOWS
meth public boolean isUnix()
meth public java.lang.String cname()
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.HostInfo$OSFamily>

CLSS public abstract org.netbeans.modules.nativeexecution.api.NativeProcess
cons public init()
innr public final static !enum State
meth public abstract int getPID() throws java.io.IOException
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment getExecutionEnvironment()
meth public abstract org.netbeans.modules.nativeexecution.api.NativeProcess$State getState()
meth public abstract org.netbeans.modules.nativeexecution.api.ProcessInfo getProcessInfo()
meth public abstract org.netbeans.modules.nativeexecution.api.ProcessStatusEx getExitStatusEx()
supr java.lang.Process

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.NativeProcess$State
 outer org.netbeans.modules.nativeexecution.api.NativeProcess
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State CANCELLED
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State ERROR
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State FINISHED
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State FINISHING
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State INITIAL
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State RUNNING
fld public final static org.netbeans.modules.nativeexecution.api.NativeProcess$State STARTING
meth public static org.netbeans.modules.nativeexecution.api.NativeProcess$State valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.NativeProcess$State[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.NativeProcess$State>

CLSS public final org.netbeans.modules.nativeexecution.api.NativeProcessBuilder
intf java.util.concurrent.Callable<java.lang.Process>
meth public !varargs org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setArguments(java.lang.String[])
meth public boolean redirectErrorStream()
meth public org.netbeans.modules.nativeexecution.api.NativeProcess call() throws java.io.IOException
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder addNativeProcessListener(javax.swing.event.ChangeListener)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder redirectError()
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder removeNativeProcessListener(javax.swing.event.ChangeListener)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setCharset(java.nio.charset.Charset)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setCommandLine(java.lang.String)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setExecutable(java.lang.String)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setInitialSuspend(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setMacroExpansion(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setStatusEx(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setUsePty(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setWorkingDirectory(java.lang.String)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder setX11Forwarding(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder unbufferOutput(boolean)
meth public org.netbeans.modules.nativeexecution.api.NativeProcessBuilder useExternalTerminal(org.netbeans.modules.nativeexecution.api.util.ExternalTerminal)
meth public org.netbeans.modules.nativeexecution.api.util.MacroMap getEnvironment()
meth public static org.netbeans.modules.nativeexecution.api.NativeProcessBuilder newLocalProcessBuilder()
meth public static org.netbeans.modules.nativeexecution.api.NativeProcessBuilder newProcessBuilder(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object
hfds externalTerminal,info

CLSS public org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent
cons public init(org.netbeans.modules.nativeexecution.api.NativeProcess,org.netbeans.modules.nativeexecution.api.NativeProcess$State,int)
fld public final int pid
fld public final org.netbeans.modules.nativeexecution.api.NativeProcess$State state
meth public java.lang.String toString()
supr javax.swing.event.ChangeEvent

CLSS public final org.netbeans.modules.nativeexecution.api.NativeProcessExecutionService
meth public java.util.concurrent.Future<java.lang.Integer> start()
meth public org.netbeans.modules.nativeexecution.api.ProcessInfo getProcessInfo()
meth public static org.netbeans.modules.nativeexecution.api.NativeProcessExecutionService newService(org.netbeans.modules.nativeexecution.api.NativeProcessBuilder,org.netbeans.api.extexecution.input.LineProcessor,org.netbeans.api.extexecution.input.LineProcessor,java.lang.String)
supr java.lang.Object
hfds descr,task
hcls ExecutionTask

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.ProcessInfo
meth public abstract long getCreationTimestamp(java.util.concurrent.TimeUnit)

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.ProcessInfoProvider
meth public abstract org.netbeans.modules.nativeexecution.api.ProcessInfo getProcessInfo()

CLSS public final org.netbeans.modules.nativeexecution.api.ProcessStatusEx
meth public boolean ifCoreDump()
meth public boolean ifExited()
meth public boolean ifSignalled()
meth public int getExitCode()
meth public java.lang.String termSignal()
meth public long maxRSS()
meth public long realTime(java.util.concurrent.TimeUnit)
meth public long sysTime(java.util.concurrent.TimeUnit)
meth public long usrTime(java.util.concurrent.TimeUnit)
supr java.lang.Object
hfds exitCode,ifCoreDump,maxRSS,rtime,stime,termSignal,utime
hcls Accessor

CLSS public final org.netbeans.modules.nativeexecution.api.execution.IOTabsController
cons public init()
innr public abstract interface static IOTabFactory
innr public final static InputOutputTab
innr public final static TabsGroup
meth public org.netbeans.modules.nativeexecution.api.execution.IOTabsController$TabsGroup openTabsGroup(java.lang.String,boolean)
meth public static org.netbeans.modules.nativeexecution.api.execution.IOTabsController getDefault()
meth public static org.openide.windows.InputOutput getInputOutput(org.netbeans.modules.nativeexecution.api.execution.IOTabsController$InputOutputTab)
supr java.lang.Object
hfds comparator,groups,instance
hcls TabsGroupGroupsComparator

CLSS public abstract interface static org.netbeans.modules.nativeexecution.api.execution.IOTabsController$IOTabFactory
 outer org.netbeans.modules.nativeexecution.api.execution.IOTabsController
meth public abstract org.openide.windows.InputOutput createNewTab(java.lang.String)

CLSS public final static org.netbeans.modules.nativeexecution.api.execution.IOTabsController$InputOutputTab
 outer org.netbeans.modules.nativeexecution.api.execution.IOTabsController
meth public java.lang.String getName()
meth public void closeOutput()
meth public void select()
supr java.lang.Object
hfds inputOutputRef,name

CLSS public final static org.netbeans.modules.nativeexecution.api.execution.IOTabsController$TabsGroup
 outer org.netbeans.modules.nativeexecution.api.execution.IOTabsController
meth public java.lang.String toString()
meth public org.netbeans.modules.nativeexecution.api.execution.IOTabsController$InputOutputTab getTab(java.lang.String,org.netbeans.modules.nativeexecution.api.execution.IOTabsController$IOTabFactory)
meth public void lockAndReset()
meth public void unlockAndCloseOutput()
supr java.lang.Object
hfds groupName,locked,seqID,tabs

CLSS public final org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor
cons public init()
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor charset(java.nio.charset.Charset)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor controllable(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor errConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor frontWindow(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor inputOutput(org.openide.windows.InputOutput)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor inputVisible(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor keepInputOutputOnFinish()
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor noReset(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor outConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor outLineBased(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor postExecution(java.lang.Runnable)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor postMessageDisplayer(org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor requestFocus(boolean)
meth public org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor showProgress(boolean)
supr java.lang.Object
hfds charset,closeInputOutputOnFinish,controllable,errConvertorFactory,frontWindow,inputOutput,inputVisible,outConvertorFactory,outLineBased,postExecution,postMessageDisplayer,requestFocus,resetInputOutputOnFinish,showProgress

CLSS public final org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService
meth public java.util.concurrent.Future<java.lang.Integer> run()
meth public static org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService newService(org.netbeans.modules.nativeexecution.api.NativeProcessBuilder,org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor,java.lang.String)
supr java.lang.Object
hfds descriptor,displayName,execCharset,listener,postExecutable,processBuilder,processRef,startTimeMillis,started
hcls PostRunnable,PreExecution,ProcessChangeListener

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer
innr public abstract static AbstractDisplayer
innr public final static Default
meth public abstract java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess$State,int,long)
meth public abstract java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess,long)
meth public abstract java.lang.String getPostStatusString(org.netbeans.modules.nativeexecution.api.NativeProcess)

CLSS public abstract static org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer$AbstractDisplayer
 outer org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer
cons protected init(java.lang.String)
innr protected static Colors
intf org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer2
meth protected final java.lang.String formatTime(long)
meth public java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess$State,int,long)
meth public java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess,long)
meth public java.lang.String getPostStatusString(org.netbeans.modules.nativeexecution.api.NativeProcess)
meth public void outPostMessage(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.NativeProcess,long)
supr java.lang.Object
hfds defaultImpl

CLSS protected static org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer$AbstractDisplayer$Colors
 outer org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer$AbstractDisplayer
cons protected init()
meth protected static java.awt.Color getColorError(org.openide.windows.InputOutput)
meth protected static java.awt.Color getColorFailure(org.openide.windows.InputOutput)
meth protected static java.awt.Color getColorSuccess(org.openide.windows.InputOutput)
meth protected static java.awt.Color getDefaultColorBackground()
supr java.lang.Object

CLSS public final static org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer$Default
 outer org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer
cons public init(java.lang.String)
intf org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer2
meth public java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess$State,int,long)
meth public java.lang.String getPostMessage(org.netbeans.modules.nativeexecution.api.NativeProcess,long)
meth public java.lang.String getPostStatusString(org.netbeans.modules.nativeexecution.api.NativeProcess)
meth public void outPostMessage(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.NativeProcess,long)
supr java.lang.Object
hfds actionName

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer2
intf org.netbeans.modules.nativeexecution.api.execution.PostMessageDisplayer
meth public abstract void outPostMessage(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.NativeProcess,long)

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.pty.Pty
meth public abstract java.io.InputStream getErrorStream()
meth public abstract java.io.InputStream getInputStream()
meth public abstract java.io.OutputStream getOutputStream()
meth public abstract java.lang.String getSlaveName()
meth public abstract java.lang.String toString()
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment getEnv()
meth public abstract void close() throws java.io.IOException

CLSS public final org.netbeans.modules.nativeexecution.api.pty.PtySupport
meth public static boolean connect(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.NativeProcess)
meth public static boolean connect(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.NativeProcess,java.lang.Runnable)
meth public static boolean connect(org.openide.windows.InputOutput,org.netbeans.modules.nativeexecution.api.pty.Pty)
meth public static boolean isSupportedFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static java.lang.String getTTY(org.netbeans.modules.nativeexecution.api.NativeProcess)
meth public static org.netbeans.modules.nativeexecution.api.pty.Pty allocate(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException
meth public static void deallocate(org.netbeans.modules.nativeexecution.api.pty.Pty) throws java.io.IOException
meth public static void disableEcho(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
meth public static void setBackspaceAsEraseChar(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds log

CLSS abstract interface org.netbeans.modules.nativeexecution.api.pty.package-info

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.util.AsynchronousAction
intf javax.swing.Action
meth public abstract void invoke() throws java.lang.Exception

CLSS public final org.netbeans.modules.nativeexecution.api.util.Authentication
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList DEFAULT_METHODS
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList PASSWORD_METHODS
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList SSH_KEY_METHODS
innr public final static !enum Method
innr public final static !enum Type
innr public static MethodList
meth public boolean isDefined()
meth public int getTimeout()
meth public java.lang.String getKey()
meth public java.lang.String getKnownHostsFile()
meth public java.lang.String getSSHKeyFile()
meth public org.netbeans.modules.nativeexecution.api.ExecutionEnvironment getEnv()
meth public org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList getAuthenticationMethods()
meth public org.netbeans.modules.nativeexecution.api.util.Authentication$Type getType()
meth public static boolean isValidSSHKeyFile(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.Authentication getFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void apply()
meth public void remove()
meth public void setAuthenticationMethods(org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList)
meth public void setPassword()
meth public void setSSHKeyFile(java.lang.String)
meth public void setTimeout(int)
meth public void store()
supr java.lang.Object
hfds METHODS_SUFFIX,TIMEOUT_SUFFIX,authenticationMethods,env,isUnitTest,knownHostsFile,lastSSHKeyFile,pref_key,prefs,sshKeyFile,timeout,type

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.Authentication$Method
 outer org.netbeans.modules.nativeexecution.api.util.Authentication
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Method GssapiWithMic
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Method KeyboardInteractive
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Method Password
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Method PublicKey
meth public boolean hasKeyFile()
meth public java.lang.String getDisplayName()
meth public java.lang.String getID()
meth public static org.netbeans.modules.nativeexecution.api.util.Authentication$Method valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.Authentication$Method[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.Authentication$Method>
hfds hasKeyFile,id

CLSS public static org.netbeans.modules.nativeexecution.api.util.Authentication$MethodList
 outer org.netbeans.modules.nativeexecution.api.util.Authentication
cons public !varargs init(org.openide.util.Pair<org.netbeans.modules.nativeexecution.api.util.Authentication$Method,java.lang.Boolean>[])
cons public init(java.util.List<org.openide.util.Pair<org.netbeans.modules.nativeexecution.api.util.Authentication$Method,java.lang.Boolean>>)
meth public boolean equals(java.lang.Object)
meth public boolean hasKeyFile()
meth public boolean isEmpty()
meth public boolean isEnabled(org.netbeans.modules.nativeexecution.api.util.Authentication$Method)
meth public int hashCode()
meth public java.lang.String toJschString()
meth public java.lang.String toStorageString()
meth public java.lang.String toString()
meth public org.netbeans.modules.nativeexecution.api.util.Authentication$Method[] getMethods()
supr java.lang.Object
hfds enabled,methods

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.Authentication$Type
 outer org.netbeans.modules.nativeexecution.api.util.Authentication
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Type PASSWORD
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Type SSH_KEY
fld public final static org.netbeans.modules.nativeexecution.api.util.Authentication$Type UNDEFINED
meth public static org.netbeans.modules.nativeexecution.api.util.Authentication$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.Authentication$Type[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.Authentication$Type>

CLSS public final org.netbeans.modules.nativeexecution.api.util.AuthenticationUtils
meth public static java.lang.String getSSHKeyFileFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static void changeAuth(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.netbeans.modules.nativeexecution.api.util.Authentication)
meth public static void usePasswordAuthenticationFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static void useSSHKeyAuthenticationFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport
innr public final static UploadParameters
innr public static UploadStatus
meth public static byte[] readFile(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,long,int,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> chmod(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> downloadFile(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.io.File,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> downloadFile(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> mkDir(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> rmDir(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,boolean,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> rmFile(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> sendSignal(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int,org.netbeans.modules.nativeexecution.api.util.Signal,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> sendSignalGrp(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int,org.netbeans.modules.nativeexecution.api.util.Signal,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> sendSignalSession(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int,org.netbeans.modules.nativeexecution.api.util.Signal,java.io.Writer)
meth public static java.util.concurrent.Future<java.lang.Integer> sigqueue(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int,int,int,java.io.Writer)
 anno 0 java.lang.Deprecated()
meth public static java.util.concurrent.Future<java.lang.Integer> sigqueue(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int,org.netbeans.modules.nativeexecution.api.util.Signal,int,java.io.Writer)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus> uploadFile(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus> uploadFile(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int,boolean)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus> uploadFile(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus> uploadFile(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int,boolean)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus> uploadFile(org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadParameters)
supr java.lang.Object
hcls CommandRunner

CLSS public final static org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadParameters
 outer org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport
cons public init(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
cons public init(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,int)
cons public init(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String,int,boolean,javax.swing.event.ChangeListener)
cons public init(java.io.File,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String,int,boolean,javax.swing.event.ChangeListener,boolean)
fld public final boolean checkMd5
fld public final boolean returnStat
fld public final int mask
fld public final java.io.File srcFile
fld public final java.lang.String dstFileName
fld public final java.lang.String dstFileToRename
fld public final javax.swing.event.ChangeListener callback
fld public final org.netbeans.modules.nativeexecution.api.ExecutionEnvironment dstExecEnv
meth public java.lang.String toString()
supr java.lang.Object

CLSS public static org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport$UploadStatus
 outer org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport
meth public boolean isOK()
meth public int getExitCode()
meth public java.lang.String getError()
meth public org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo getStatInfo()
supr java.lang.Object
hfds error,exitCode,statInfo

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.util.ConnectionListener
intf java.util.EventListener
meth public abstract void connected(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public abstract void disconnected(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)

CLSS public final org.netbeans.modules.nativeexecution.api.util.ConnectionManager
innr public static CancellationException
meth public boolean connect(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public boolean isConnectedTo(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public java.util.List<org.netbeans.modules.nativeexecution.api.ExecutionEnvironment> getRecentConnections()
meth public org.netbeans.modules.nativeexecution.api.util.AsynchronousAction getConnectToAction(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.Runnable)
meth public static org.netbeans.modules.nativeexecution.api.util.ConnectionManager getInstance()
meth public void addConnectionListener(org.netbeans.modules.nativeexecution.api.util.ConnectionListener)
meth public void connectTo(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
meth public void disconnect(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void forget(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void removeConnectionListener(org.netbeans.modules.nativeexecution.api.util.ConnectionListener)
supr java.lang.Object
hfds DEFAULT_CC,RETRY_MAX,UNIT_TEST_MODE,channelsSupport,channelsSupportLock,connectionActions,connectionListeners,connectionTasks,connectionWatcher,connectionWatcherInterval,instance,jschPool,log,recentConnections,slowConnectionListenerDetector
hcls ConnectToAction,ConnectionContinuation,ConnectionManagerAccessorImpl,ConnectionWatcher,JSchAccessImpl

CLSS public static org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
 outer org.netbeans.modules.nativeexecution.api.util.ConnectionManager
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.netbeans.modules.nativeexecution.api.util.EnvUtils
meth public static java.lang.String getKey(java.lang.String)
meth public static java.lang.String getValue(java.lang.String)
meth public static java.lang.String toHostID(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object

CLSS public final org.netbeans.modules.nativeexecution.api.util.ExternalTerminal
meth public boolean isAvailable(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public java.lang.String getID()
meth public org.netbeans.modules.nativeexecution.api.util.ExternalTerminal setPrompt(java.lang.String)
meth public org.netbeans.modules.nativeexecution.api.util.ExternalTerminal setTitle(java.lang.String)
meth public org.netbeans.modules.nativeexecution.api.util.ExternalTerminal setWorkdir(java.lang.String)
supr java.lang.Object
hfds CLOSE_TERMINAL,execCache,profile,prompt,title,workdir
hcls ExternalTerminalAccessorImpl,TermEnvPair

CLSS public final org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider
meth public static java.util.Collection<java.lang.String> getSupportedTerminalIDs()
meth public static org.netbeans.modules.nativeexecution.api.util.ExternalTerminal getTerminal(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
supr java.lang.Object
hfds hash,log,profiles
hcls Context,SAXHandler

CLSS public org.netbeans.modules.nativeexecution.api.util.FileInfoProvider
cons public init()
innr public final static SftpIOException
innr public final static StatInfo
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo> lstat(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo> move(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo> stat(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
meth public static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo[]> ls(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
supr java.lang.Object
hfds ACCESS_MASK,ALL_R,ALL_W,ALL_X,GRP_R,GRP_W,GRP_X,USR_R,USR_W,USR_X

CLSS public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$SftpIOException
 outer org.netbeans.modules.nativeexecution.api.util.FileInfoProvider
fld public final static int SSH_FX_BAD_MESSAGE = 5
fld public final static int SSH_FX_CONNECTION_LOST = 7
fld public final static int SSH_FX_EOF = 1
fld public final static int SSH_FX_FAILURE = 4
fld public final static int SSH_FX_NO_CONNECTION = 6
fld public final static int SSH_FX_NO_SUCH_FILE = 2
fld public final static int SSH_FX_OP_UNSUPPORTED = 8
fld public final static int SSH_FX_PERMISSION_DENIED = 3
meth public int getId()
meth public java.lang.String getPath()
supr java.io.IOException
hfds id,path

CLSS public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo
 outer org.netbeans.modules.nativeexecution.api.util.FileInfoProvider
cons public init(java.lang.String,int,int,long,boolean,boolean,java.lang.String,int,java.util.Date)
cons public init(java.lang.String,int,int,long,java.lang.String,int,java.util.Date)
innr public final static !enum FileType
meth public boolean canExecute(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public boolean canRead(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public boolean canWrite(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public boolean isDirectory()
meth public boolean isLink()
meth public boolean isPlainFile()
meth public int getAccess()
meth public int getGropupId()
meth public int getUserId()
meth public java.lang.String getAccessAsString()
meth public java.lang.String getLinkTarget()
meth public java.lang.String getName()
meth public java.lang.String toExternalForm()
meth public java.lang.String toString()
meth public java.util.Date getLastModified()
meth public long getSize()
meth public org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType getFileType()
meth public static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo fromExternalForm(java.lang.String)
supr java.lang.Object
hfds S_IFBLK,S_IFBLK_C,S_IFCHR,S_IFCHR_C,S_IFCMP,S_IFCMP_C,S_IFDIR,S_IFDIR_C,S_IFDOOR,S_IFDOOR_C,S_IFIFO,S_IFIFO_C,S_IFLNK,S_IFLNK_C,S_IFMPB,S_IFMPB_C,S_IFMPC,S_IFMPC_C,S_IFMT,S_IFNAM,S_IFNAM_C,S_IFPORT,S_IFPORT_C,S_IFREG,S_IFREG_C,S_IFSHAD,S_IFSHAD_C,S_IFSOCK,S_IFSOCK_C,S_UNDEF_C,access,gid,lastModified,linkTarget,name,size,uid

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType
 outer org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType BlockSpecial
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType CharacterSpecial
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Directory
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Door
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType EventPort
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType MultiplexedBlockSpecial
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType MultiplexedCharacterSpecial
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType NamedPipe
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType NetworkSpecial
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Regular
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Shadow
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Socket
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType SpecialNamed
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType SymbolicLink
fld public final static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType Undefined
meth public char toChar()
meth public int toInt()
meth public static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType fromChar(char)
meth public static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType fromInt(int)
meth public static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.FileInfoProvider$StatInfo$FileType>
hfds fileType,letter

CLSS public org.netbeans.modules.nativeexecution.api.util.HelperLibraryUtility
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public final java.lang.String getLDPaths(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException
meth public final java.lang.String getLibraryName(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException
meth public final java.util.List<java.lang.String> getPaths(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException
meth public static boolean isMac(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static java.lang.String getLDPathEnvName(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static java.lang.String getLDPreloadEnvName(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object
hfds cache,codeNameBase,pattern

CLSS public org.netbeans.modules.nativeexecution.api.util.HelperUtility
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected final java.lang.String codeNameBase
fld protected final static java.util.logging.Logger log
meth protected java.io.File getLocalFile(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.text.ParseException
meth protected java.io.File getLocalFile(org.netbeans.modules.nativeexecution.api.HostInfo)
meth public final java.lang.String getPath(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException
meth public final java.lang.String getPath(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.netbeans.modules.nativeexecution.api.HostInfo) throws java.io.IOException
supr java.lang.Object
hfds cache,pattern

CLSS public final org.netbeans.modules.nativeexecution.api.util.HostInfoUtils
fld public final static java.lang.String LOCALHOST = "localhost"
meth protected static void resetHostsData()
meth public static boolean directoryExists(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String) throws java.io.IOException,java.lang.InterruptedException
meth public static boolean fileExists(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String) throws java.io.IOException,java.lang.InterruptedException
meth public static boolean isHostInfoAvailable(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static boolean isLocalhost(java.lang.String)
meth public static java.lang.String searchFile(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.util.List<java.lang.String>,java.lang.String,boolean)
meth public static org.netbeans.modules.nativeexecution.api.HostInfo getHostInfo(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
meth public static void dumpInfo(org.netbeans.modules.nativeexecution.api.HostInfo,java.io.PrintStream)
meth public static void updateHostInfo(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException,java.lang.InterruptedException
supr java.lang.Object
hfds cache,myAddresses

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.util.HostPropertyValidator

CLSS public org.netbeans.modules.nativeexecution.api.util.LinkSupport
meth public static boolean isLinkFile(java.lang.String)
meth public static java.lang.String getOriginalFile(java.lang.String)
meth public static java.lang.String resolveWindowsLink(java.lang.String)
supr java.lang.Object
hcls LinkReader

CLSS public final org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory
innr public abstract interface static MacroExpander
innr public final static !enum ExpanderStyle
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$MacroExpander getExpander(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$MacroExpander getExpander(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,boolean)
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$MacroExpander getExpander(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle)
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$MacroExpander getExpander(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle,boolean)
supr java.lang.Object
hfds expanderCache
hcls MacroExpanderImpl

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle
 outer org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory
fld public final static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle DEFAULT_STYLE
fld public final static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle SUNSTUDIO_STYLE
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$ExpanderStyle>

CLSS public abstract interface static org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory$MacroExpander
 outer org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory
meth public abstract java.lang.String expandMacros(java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws java.text.ParseException
meth public abstract java.lang.String expandPredefinedMacros(java.lang.String) throws java.text.ParseException

CLSS public final org.netbeans.modules.nativeexecution.api.util.MacroMap
intf java.lang.Cloneable
meth public final boolean isEmpty()
meth public final java.lang.String toString()
meth public final java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public final void putAll(java.lang.String[])
meth public final void putAll(java.util.Map<java.lang.String,java.lang.String>)
meth public final void putAll(org.netbeans.modules.nativeexecution.api.util.MacroMap)
meth public java.lang.String get(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String remove(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getUserDefinedMap()
meth public java.util.Map<java.lang.String,java.lang.String> toMap()
meth public java.util.Set<java.lang.String> getExportVariablesSet()
meth public org.netbeans.modules.nativeexecution.api.util.MacroMap clone()
meth public static org.netbeans.modules.nativeexecution.api.util.MacroMap createEmpty(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static org.netbeans.modules.nativeexecution.api.util.MacroMap forExecEnv(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void appendPathVariable(java.lang.String,java.lang.String)
meth public void clear()
meth public void dump(java.io.PrintStream)
meth public void prependPathVariable(java.lang.String,java.lang.String)
supr java.lang.Object
hfds execEnv,hostEnv,isWindows,lock,log,macroExpander,map,varsForExport
hcls CaseInsensitiveComparator

CLSS public final org.netbeans.modules.nativeexecution.api.util.PasswordManager
meth public boolean isRememberPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public char[] getPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static org.netbeans.modules.nativeexecution.api.util.PasswordManager getInstance()
meth public void clearPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void forceClearPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public void setRememberPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,boolean)
meth public void setServerList(java.util.List<org.netbeans.modules.nativeexecution.api.ExecutionEnvironment>)
meth public void storePassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,char[],boolean)
supr java.lang.Object
hfds KEY_PREFIX,STORE_PREFIX,cache,instance,keepPasswordsInMemory,keyringIsActivated

CLSS public final org.netbeans.modules.nativeexecution.api.util.Path
meth public static java.lang.String findCommand(java.lang.String)
meth public static java.lang.String getPathAsString()
meth public static java.lang.String getPathName()
meth public static java.util.ArrayList<java.lang.String> getPath()
supr java.lang.Object
hfds list,pathName

CLSS public org.netbeans.modules.nativeexecution.api.util.PathUtils
meth public static java.lang.String expandPath(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment) throws java.io.IOException,java.text.ParseException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
meth public static java.lang.String getCwdPath(long,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static java.lang.String getExePath(long,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public static java.lang.String getPathFromSymlink(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object

CLSS public final org.netbeans.modules.nativeexecution.api.util.ProcessUtils
innr public abstract interface static PostExecutor
innr public final static ExitStatus
meth public !varargs static java.util.concurrent.Future<org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus> execute(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.openide.util.RequestProcessor,org.netbeans.modules.nativeexecution.api.util.ProcessUtils$PostExecutor,java.lang.String,java.lang.String[])
meth public !varargs static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus execute(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String[])
meth public !varargs static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus executeInDir(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String[])
meth public !varargs static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus executeWithoutMacroExpansion(java.lang.String,org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,java.lang.String[])
meth public static <%0 extends java.lang.Process> {%%0} ignoreProcessError({%%0})
meth public static <%0 extends java.lang.Process> {%%0} ignoreProcessOutput({%%0})
meth public static <%0 extends java.lang.Process> {%%0} ignoreProcessOutputAndError({%%0})
meth public static boolean isAlive(java.lang.Process)
meth public static java.io.BufferedReader getReader(java.io.InputStream,boolean)
meth public static java.io.PrintWriter getWriter(java.io.OutputStream,boolean)
meth public static java.lang.String getRemoteCharSet()
meth public static java.lang.String readProcessErrorLine(java.lang.Process) throws java.io.IOException
meth public static java.lang.String readProcessOutputLine(java.lang.Process) throws java.io.IOException
meth public static java.util.List<java.lang.String> readProcessError(java.lang.Process) throws java.io.IOException
meth public static java.util.List<java.lang.String> readProcessOutput(java.lang.Process) throws java.io.IOException
meth public static java.util.concurrent.Future<java.util.List<java.lang.String>> readProcessErrorAsync(java.lang.Process)
meth public static java.util.concurrent.Future<java.util.List<java.lang.String>> readProcessOutputAsync(java.lang.Process)
meth public static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus execute(java.lang.ProcessBuilder)
meth public static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus execute(java.lang.ProcessBuilder,byte[])
meth public static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus execute(org.netbeans.modules.nativeexecution.api.NativeProcessBuilder)
meth public static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus execute(org.netbeans.modules.nativeexecution.api.NativeProcessBuilder,byte[])
meth public static void destroy(java.lang.Process)
meth public static void logError(java.util.logging.Level,java.util.logging.Logger,java.lang.Process) throws java.io.IOException
meth public static void logError(java.util.logging.Level,java.util.logging.Logger,org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus) throws java.io.IOException
meth public static void readProcessErrorAsync(java.lang.Process,org.netbeans.api.extexecution.input.LineProcessor)
meth public static void readProcessOutputAsync(java.lang.Process,org.netbeans.api.extexecution.input.LineProcessor)
meth public static void writeError(java.io.Writer,java.lang.Process) throws java.io.IOException
supr java.lang.Object
hfds RP,remoteCharSet

CLSS public final static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus
 outer org.netbeans.modules.nativeexecution.api.util.ProcessUtils
cons public init(int,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
fld public final int exitCode
meth public boolean isOK()
meth public java.lang.String getErrorString()
meth public java.lang.String getOutputString()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getErrorLines()
meth public java.util.List<java.lang.String> getOutputLines()
supr java.lang.Object
hfds error,errorLines,output,outputLines

CLSS public abstract interface static org.netbeans.modules.nativeexecution.api.util.ProcessUtils$PostExecutor
 outer org.netbeans.modules.nativeexecution.api.util.ProcessUtils
meth public abstract void processFinished(org.netbeans.modules.nativeexecution.api.util.ProcessUtils$ExitStatus)

CLSS public final org.netbeans.modules.nativeexecution.api.util.RemoteStatistics
cons public init()
fld public final static boolean COLLECT_STACKS
fld public final static boolean COLLECT_STATISTICS
fld public final static boolean COLLECT_TRAFFIC
innr public abstract static ActivityID
intf java.util.concurrent.Callable<java.lang.Boolean>
meth public !varargs static org.netbeans.modules.nativeexecution.api.util.RemoteStatistics$ActivityID startChannelActivity(java.lang.CharSequence,java.lang.CharSequence[])
meth public java.lang.Boolean call() throws java.lang.Exception
meth public static void startTest(java.lang.String,java.lang.Runnable,int,int)
meth public static void stopChannelActivity(org.netbeans.modules.nativeexecution.api.util.RemoteStatistics$ActivityID)
meth public static void stopChannelActivity(org.netbeans.modules.nativeexecution.api.util.RemoteStatistics$ActivityID,long)
supr java.lang.Object
hfds BREAK_UPLOADS_FLAG_FILE,currentStatRef,listener,queue,trafficCounters,trafficDetected,unnamed
hcls RemoteIOListener,RemoteMeasurementsRef,TrafficCounters

CLSS public abstract static org.netbeans.modules.nativeexecution.api.util.RemoteStatistics$ActivityID
 outer org.netbeans.modules.nativeexecution.api.util.RemoteStatistics
supr java.lang.Object

CLSS public org.netbeans.modules.nativeexecution.api.util.Shell
cons public init(org.netbeans.modules.nativeexecution.api.util.Shell$ShellType,java.lang.String,java.io.File)
fld public final java.io.File bindir
fld public final java.lang.String shell
fld public final org.netbeans.modules.nativeexecution.api.util.Shell$ShellType type
innr public final static !enum ShellType
meth public java.lang.String toString()
meth public org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus getValidationStatus()
supr java.lang.Object
hfds validationStatus

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.Shell$ShellType
 outer org.netbeans.modules.nativeexecution.api.util.Shell
fld public final static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType CYGWIN
fld public final static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType MSYS
fld public final static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType NO_SHELL
fld public final static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType UNKNOWN
meth public org.netbeans.modules.nativeexecution.support.windows.PathConverter$PathType toPathType()
meth public static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.Shell$ShellType[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.Shell$ShellType>

CLSS public final org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner
cons public init(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String,org.netbeans.api.extexecution.input.LineProcessor)
cons public init(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.net.URI,org.netbeans.api.extexecution.input.LineProcessor)
innr public final static BufferedLineProcessor
innr public final static LoggerLineProcessor
meth public !varargs void setArguments(java.lang.String[])
meth public int execute() throws java.io.IOException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
meth public void setErrorProcessor(org.netbeans.api.extexecution.input.LineProcessor)
meth public void setOutputCharset(java.nio.charset.Charset)
meth public void setScriptCharset(java.nio.charset.Charset)
supr java.lang.Object
hfds args,countdown,env,errorProcessor,log,outputCS,outputProcessor,script,scriptCS,scriptURI,shellProcess
hcls ProcessOutputReader

CLSS public final static org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner$BufferedLineProcessor
 outer org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner
cons public init()
intf org.netbeans.api.extexecution.input.LineProcessor
meth public java.lang.String getAsString()
meth public java.util.List<java.lang.String> getBuffer()
meth public void close()
meth public void processLine(java.lang.String)
meth public void reset()
supr java.lang.Object
hfds buffer

CLSS public final static org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner$LoggerLineProcessor
 outer org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner
cons public init(java.lang.String)
intf org.netbeans.api.extexecution.input.LineProcessor
meth public void close()
meth public void processLine(java.lang.String)
meth public void reset()
supr java.lang.Object
hfds prefix

CLSS public final org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport
fld protected final static org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus NOSHELL
fld protected final static org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus VALID
innr public static ShellValidationStatus
meth public static boolean confirm(java.lang.String,java.lang.String,org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus)
meth public static boolean confirm(org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus)
meth public static org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus getValidationStatus(org.netbeans.modules.nativeexecution.api.util.Shell)
supr java.lang.Object

CLSS public static org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport$ShellValidationStatus
 outer org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport
meth public boolean hasWarnings()
meth public boolean isValid()
meth public java.util.List<java.lang.String> getErrors()
meth public java.util.List<java.lang.String> getWarnings()
supr java.lang.Object
hfds errors,shell,warnings

CLSS public final !enum org.netbeans.modules.nativeexecution.api.util.Signal
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal NULL
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGABRT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGALRM
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGBUS
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGCANCEL
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGCHLD
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGCLD
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGCONT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGEMT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGFPE
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGFREEZE
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGHUP
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGILL
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGINT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGIO
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGIOT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGJVM1
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGJVM2
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGKILL
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGLOST
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGLWP
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGPIPE
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGPOLL
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGPROF
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGPWR
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGQUIT
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGSEGV
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGSTOP
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGSYS
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTERM
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTHAW
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTRAP
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTSTP
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTTIN
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGTTOU
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGURG
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGUSR1
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGUSR2
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGVTALRM
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGWAITING
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGWINCH
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGXCPU
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGXFSZ
fld public final static org.netbeans.modules.nativeexecution.api.util.Signal SIGXRES
meth public static org.netbeans.modules.nativeexecution.api.util.Signal valueOf(int)
meth public static org.netbeans.modules.nativeexecution.api.util.Signal valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.Signal[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.Signal>
hfds id

CLSS public abstract interface org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport
meth public abstract boolean hasPrivileges(java.util.Collection<java.lang.String>)
meth public abstract boolean requestPrivileges(java.util.Collection<java.lang.String>,java.lang.String,char[]) throws java.lang.InterruptedException,java.security.acl.NotOwnerException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException
meth public abstract java.util.List<java.lang.String> getExecutionPrivileges()
meth public abstract org.netbeans.modules.nativeexecution.api.util.AsynchronousAction getRequestPrivilegesAction(java.util.Collection<java.lang.String>,java.lang.Runnable)
meth public abstract void invalidate()
meth public abstract void requestPrivileges(java.util.Collection<java.lang.String>,boolean) throws java.lang.InterruptedException,java.security.acl.NotOwnerException,org.netbeans.modules.nativeexecution.api.util.ConnectionManager$CancellationException

CLSS public final org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupportProvider
meth public static org.netbeans.modules.nativeexecution.api.util.SolarisPrivilegesSupport getSupportFor(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object
hfds instances

CLSS public org.netbeans.modules.nativeexecution.api.util.UnbufferSupport
meth public static void initUnbuffer(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,org.netbeans.modules.nativeexecution.api.util.MacroMap) throws java.io.IOException
supr java.lang.Object
hfds UNBUFFER_DISABLED,cache,log

CLSS public final org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator
innr public final static !enum RootKey
intf java.util.Iterator<java.lang.String[]>
meth public boolean hasNext()
meth public java.lang.String[] next()
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator get(java.lang.String,java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator get(java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator get(java.lang.String,org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey[],java.lang.String,boolean)
meth public void remove()
supr java.lang.Object
hfds idx,recursively,rootKeys,subKey,valueName

CLSS public final static !enum org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey
 outer org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator
fld public final static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey HKCU
fld public final static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey HKLM
fld public final static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey NHKCU
fld public final static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey NHKLM
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.api.util.WindowsRegistryIterator$RootKey>

CLSS public final org.netbeans.modules.nativeexecution.api.util.WindowsSupport
meth public int getWinPID(int)
meth public java.lang.String convertFromCygwinPath(java.lang.String)
meth public java.lang.String convertFromMSysPath(java.lang.String)
meth public java.lang.String convertToAllShellPaths(java.lang.String)
meth public java.lang.String convertToCygwinPath(java.lang.String)
meth public java.lang.String convertToMSysPath(java.lang.String)
meth public java.lang.String convertToShellPath(java.lang.String)
meth public java.lang.String convertToWindowsPath(java.lang.String)
meth public java.lang.String getPathKey()
meth public java.lang.String getShell()
meth public java.nio.charset.Charset getShellCharset()
meth public org.netbeans.modules.nativeexecution.api.util.Shell getActiveShell()
meth public static org.netbeans.modules.nativeexecution.api.util.WindowsSupport getInstance()
meth public void init()
meth public void init(java.lang.String)
supr java.lang.Object
hfds activeShell,charset,initLock,initialized,instance,log,pathConverter,pathKeyRef

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService
meth public abstract java.lang.String toUniqueID(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment createNew(java.lang.String)
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment createNew(java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment createNew(java.lang.String,java.lang.String,int)
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment fromUniqueID(java.lang.String)
meth public abstract org.netbeans.modules.nativeexecution.api.ExecutionEnvironment getLocal()

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentServiceProvider
meth public abstract boolean isApplicable(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)

CLSS public abstract org.netbeans.modules.nativeexecution.spi.JSchAuthenticationSelection
cons public init()
meth public abstract boolean initAuthentication(org.netbeans.modules.nativeexecution.api.util.Authentication)
meth public static org.netbeans.modules.nativeexecution.spi.JSchAuthenticationSelection find()
supr java.lang.Object
hfds INSTANCE
hcls JSchAuthenticationSelectionImpl

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.ProcessInfoProviderFactory
meth public abstract org.netbeans.modules.nativeexecution.api.ProcessInfoProvider getProvider(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,int)

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.support.GrantPrivilegesProvider
meth public abstract boolean askPassword()
meth public abstract char[] getPassword()
meth public abstract java.lang.String getUser()
meth public abstract void clearPassword()

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.support.GrantPrivilegesProviderFactory
meth public abstract org.netbeans.modules.nativeexecution.spi.support.GrantPrivilegesProvider create()

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.support.JSchAccess
meth public abstract com.jcraft.jsch.Channel openChannel(java.lang.String) throws com.jcraft.jsch.JSchException,java.lang.InterruptedException
meth public abstract int setPortForwardingL(int,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public abstract java.lang.String getConfig(java.lang.String)
meth public abstract java.lang.String getServerVersion() throws com.jcraft.jsch.JSchException
meth public abstract void delPortForwardingL(int) throws com.jcraft.jsch.JSchException
meth public abstract void delPortForwardingR(int) throws com.jcraft.jsch.JSchException
meth public abstract void releaseChannel(com.jcraft.jsch.Channel) throws com.jcraft.jsch.JSchException
meth public abstract void setPortForwardingR(java.lang.String,int,java.lang.String,int) throws com.jcraft.jsch.JSchException

CLSS public final org.netbeans.modules.nativeexecution.spi.support.JSchAccessor
cons public init()
meth public static org.netbeans.modules.nativeexecution.spi.support.JSchAccess get(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification
cons public init()
innr public final static !enum Descriptor
meth public abstract boolean confirmShellStatusValiation(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.nativeexecution.api.util.Shell)
meth public abstract boolean showYesNoQuestion(java.lang.String,java.lang.String)
meth public abstract void notify(java.lang.String)
meth public abstract void notify(java.lang.String,org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor)
meth public abstract void notifyStatus(java.lang.String)
meth public abstract void showErrorNotification(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void showInfoNotification(java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification getDefault()
supr java.lang.Object
hfds INSTANCE
hcls Trivial

CLSS public final static !enum org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor
 outer org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification
fld public final static org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor ERROR
fld public final static org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor WARNING
meth public static org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification$Descriptor>

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.support.PasswordProvider
innr public final static !enum SecretType
meth public abstract boolean askPassword(org.netbeans.modules.nativeexecution.api.ExecutionEnvironment,java.lang.String)
meth public abstract boolean isRememberPassword()
meth public abstract char[] getPassword()
meth public abstract void clearPassword()

CLSS public final static !enum org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType
 outer org.netbeans.modules.nativeexecution.spi.support.PasswordProvider
fld public final static org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType PASSPHRASE
fld public final static org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType PASSWORD
meth public static org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType valueOf(java.lang.String)
meth public static org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType[] values()
supr java.lang.Enum<org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType>

CLSS public abstract interface org.netbeans.modules.nativeexecution.spi.support.PasswordProviderFactory
meth public abstract org.netbeans.modules.nativeexecution.spi.support.PasswordProvider create(org.netbeans.modules.nativeexecution.spi.support.PasswordProvider$SecretType)

