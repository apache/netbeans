#Signature file v4.1
#Version 1.3

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

CLSS public java.lang.UnsupportedOperationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

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

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()
meth public java.util.Spliterator<{java.util.Set%0}> spliterator()

CLSS public abstract interface !annotation org.gradle.api.Incubating
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation

CLSS public abstract interface org.gradle.tooling.BuildAction<%0 extends java.lang.Object>
intf java.io.Serializable
meth public abstract {org.gradle.tooling.BuildAction%0} execute(org.gradle.tooling.BuildController)

CLSS public abstract interface org.gradle.tooling.BuildActionExecuter<%0 extends java.lang.Object>
innr public abstract interface static Builder
intf org.gradle.tooling.ConfigurableLauncher<org.gradle.tooling.BuildActionExecuter<{org.gradle.tooling.BuildActionExecuter%0}>>
meth public abstract !varargs org.gradle.tooling.BuildActionExecuter<{org.gradle.tooling.BuildActionExecuter%0}> forTasks(java.lang.String[])
meth public abstract org.gradle.tooling.BuildActionExecuter<{org.gradle.tooling.BuildActionExecuter%0}> forTasks(java.lang.Iterable<java.lang.String>)
meth public abstract void run(org.gradle.tooling.ResultHandler<? super {org.gradle.tooling.BuildActionExecuter%0}>)
meth public abstract {org.gradle.tooling.BuildActionExecuter%0} run()

CLSS public abstract interface static org.gradle.tooling.BuildActionExecuter$Builder
 outer org.gradle.tooling.BuildActionExecuter
 anno 0 org.gradle.api.Incubating()
meth public abstract <%0 extends java.lang.Object> org.gradle.tooling.BuildActionExecuter$Builder buildFinished(org.gradle.tooling.BuildAction<{%%0}>,org.gradle.tooling.IntermediateResultHandler<? super {%%0}>)
meth public abstract <%0 extends java.lang.Object> org.gradle.tooling.BuildActionExecuter$Builder projectsLoaded(org.gradle.tooling.BuildAction<{%%0}>,org.gradle.tooling.IntermediateResultHandler<? super {%%0}>)
meth public abstract org.gradle.tooling.BuildActionExecuter<java.lang.Void> build()

CLSS public org.gradle.tooling.BuildActionFailureException
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public org.gradle.tooling.BuildCancelledException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public abstract interface org.gradle.tooling.BuildController
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} findModel(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,org.gradle.api.Action<? super {%%1}>)
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} findModel(org.gradle.tooling.model.Model,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,org.gradle.api.Action<? super {%%1}>)
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} getModel(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,org.gradle.api.Action<? super {%%1}>)
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} getModel(org.gradle.tooling.model.Model,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,org.gradle.api.Action<? super {%%1}>)
meth public abstract <%0 extends java.lang.Object> {%%0} findModel(java.lang.Class<{%%0}>)
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract <%0 extends java.lang.Object> {%%0} findModel(org.gradle.tooling.model.Model,java.lang.Class<{%%0}>)
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract <%0 extends java.lang.Object> {%%0} getModel(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getModel(org.gradle.tooling.model.Model,java.lang.Class<{%%0}>)
meth public abstract org.gradle.tooling.model.gradle.GradleBuild getBuildModel()

CLSS public org.gradle.tooling.BuildException
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public abstract interface org.gradle.tooling.BuildLauncher
intf org.gradle.tooling.ConfigurableLauncher<org.gradle.tooling.BuildLauncher>
meth public abstract !varargs org.gradle.tooling.BuildLauncher forLaunchables(org.gradle.tooling.model.Launchable[])
meth public abstract !varargs org.gradle.tooling.BuildLauncher forTasks(java.lang.String[])
meth public abstract !varargs org.gradle.tooling.BuildLauncher forTasks(org.gradle.tooling.model.Task[])
meth public abstract org.gradle.tooling.BuildLauncher forLaunchables(java.lang.Iterable<? extends org.gradle.tooling.model.Launchable>)
meth public abstract org.gradle.tooling.BuildLauncher forTasks(java.lang.Iterable<? extends org.gradle.tooling.model.Task>)
meth public abstract void run()
meth public abstract void run(org.gradle.tooling.ResultHandler<? super java.lang.Void>)

CLSS public abstract interface org.gradle.tooling.CancellationToken
meth public abstract boolean isCancellationRequested()

CLSS public abstract interface org.gradle.tooling.CancellationTokenSource
meth public abstract org.gradle.tooling.CancellationToken token()
meth public abstract void cancel()

CLSS public abstract interface org.gradle.tooling.ConfigurableLauncher<%0 extends org.gradle.tooling.ConfigurableLauncher>
intf org.gradle.tooling.LongRunningOperation
meth public abstract !varargs {org.gradle.tooling.ConfigurableLauncher%0} addProgressListener(org.gradle.tooling.events.ProgressListener,org.gradle.tooling.events.OperationType[])
meth public abstract !varargs {org.gradle.tooling.ConfigurableLauncher%0} setJvmArguments(java.lang.String[])
meth public abstract !varargs {org.gradle.tooling.ConfigurableLauncher%0} withArguments(java.lang.String[])
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} addProgressListener(org.gradle.tooling.ProgressListener)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} addProgressListener(org.gradle.tooling.events.ProgressListener)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} addProgressListener(org.gradle.tooling.events.ProgressListener,java.util.Set<org.gradle.tooling.events.OperationType>)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setColorOutput(boolean)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setEnvironmentVariables(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setJavaHome(java.io.File)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setJvmArguments(java.lang.Iterable<java.lang.String>)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setStandardError(java.io.OutputStream)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setStandardInput(java.io.InputStream)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} setStandardOutput(java.io.OutputStream)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} withArguments(java.lang.Iterable<java.lang.String>)
meth public abstract {org.gradle.tooling.ConfigurableLauncher%0} withCancellationToken(org.gradle.tooling.CancellationToken)

CLSS public abstract interface org.gradle.tooling.Failure
meth public abstract java.lang.String getDescription()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getMessage()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.util.List<? extends org.gradle.tooling.Failure> getCauses()

CLSS public org.gradle.tooling.GradleConnectionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract org.gradle.tooling.GradleConnector
cons public init()
meth public abstract org.gradle.tooling.GradleConnector forProjectDirectory(java.io.File)
meth public abstract org.gradle.tooling.GradleConnector useBuildDistribution()
meth public abstract org.gradle.tooling.GradleConnector useDistribution(java.net.URI)
meth public abstract org.gradle.tooling.GradleConnector useGradleUserHomeDir(java.io.File)
meth public abstract org.gradle.tooling.GradleConnector useGradleVersion(java.lang.String)
meth public abstract org.gradle.tooling.GradleConnector useInstallation(java.io.File)
meth public abstract org.gradle.tooling.ProjectConnection connect()
meth public static org.gradle.tooling.CancellationTokenSource newCancellationTokenSource()
meth public static org.gradle.tooling.GradleConnector newConnector()
supr java.lang.Object

CLSS public abstract interface org.gradle.tooling.IntermediateResultHandler<%0 extends java.lang.Object>
 anno 0 org.gradle.api.Incubating()
meth public abstract void onComplete({org.gradle.tooling.IntermediateResultHandler%0})

CLSS public org.gradle.tooling.ListenerFailedException
cons public init(java.lang.String,java.util.List<? extends java.lang.Throwable>)
meth public java.util.List<? extends java.lang.Throwable> getCauses()
supr org.gradle.tooling.GradleConnectionException
hfds listenerFailures

CLSS public abstract interface org.gradle.tooling.LongRunningOperation
meth public abstract !varargs org.gradle.tooling.LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener,org.gradle.tooling.events.OperationType[])
meth public abstract !varargs org.gradle.tooling.LongRunningOperation setJvmArguments(java.lang.String[])
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract !varargs org.gradle.tooling.LongRunningOperation withArguments(java.lang.String[])
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.LongRunningOperation addProgressListener(org.gradle.tooling.ProgressListener)
meth public abstract org.gradle.tooling.LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener)
meth public abstract org.gradle.tooling.LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener,java.util.Set<org.gradle.tooling.events.OperationType>)
meth public abstract org.gradle.tooling.LongRunningOperation setColorOutput(boolean)
meth public abstract org.gradle.tooling.LongRunningOperation setEnvironmentVariables(java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.LongRunningOperation setJavaHome(java.io.File)
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.LongRunningOperation setJvmArguments(java.lang.Iterable<java.lang.String>)
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.LongRunningOperation setStandardError(java.io.OutputStream)
meth public abstract org.gradle.tooling.LongRunningOperation setStandardInput(java.io.InputStream)
meth public abstract org.gradle.tooling.LongRunningOperation setStandardOutput(java.io.OutputStream)
meth public abstract org.gradle.tooling.LongRunningOperation withArguments(java.lang.Iterable<java.lang.String>)
 anno 1 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.LongRunningOperation withCancellationToken(org.gradle.tooling.CancellationToken)

CLSS public abstract interface org.gradle.tooling.ModelBuilder<%0 extends java.lang.Object>
intf org.gradle.tooling.ConfigurableLauncher<org.gradle.tooling.ModelBuilder<{org.gradle.tooling.ModelBuilder%0}>>
meth public abstract !varargs org.gradle.tooling.ModelBuilder<{org.gradle.tooling.ModelBuilder%0}> forTasks(java.lang.String[])
meth public abstract org.gradle.tooling.ModelBuilder<{org.gradle.tooling.ModelBuilder%0}> forTasks(java.lang.Iterable<java.lang.String>)
meth public abstract void get(org.gradle.tooling.ResultHandler<? super {org.gradle.tooling.ModelBuilder%0}>)
meth public abstract {org.gradle.tooling.ModelBuilder%0} get()

CLSS public abstract interface org.gradle.tooling.ProgressEvent
meth public abstract java.lang.String getDescription()

CLSS public abstract interface org.gradle.tooling.ProgressListener
meth public abstract void statusChanged(org.gradle.tooling.ProgressEvent)

CLSS public abstract interface org.gradle.tooling.ProjectConnection
intf java.io.Closeable
meth public abstract <%0 extends java.lang.Object> org.gradle.tooling.BuildActionExecuter<{%%0}> action(org.gradle.tooling.BuildAction<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.gradle.tooling.ModelBuilder<{%%0}> model(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> void getModel(java.lang.Class<{%%0}>,org.gradle.tooling.ResultHandler<? super {%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getModel(java.lang.Class<{%%0}>)
meth public abstract org.gradle.tooling.BuildActionExecuter$Builder action()
 anno 0 org.gradle.api.Incubating()
meth public abstract org.gradle.tooling.BuildLauncher newBuild()
meth public abstract org.gradle.tooling.TestLauncher newTestLauncher()
meth public abstract void close()

CLSS public abstract interface org.gradle.tooling.ResultHandler<%0 extends java.lang.Object>
meth public abstract void onComplete({org.gradle.tooling.ResultHandler%0})
meth public abstract void onFailure(org.gradle.tooling.GradleConnectionException)

CLSS public org.gradle.tooling.TestExecutionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public abstract interface org.gradle.tooling.TestLauncher
intf org.gradle.tooling.ConfigurableLauncher<org.gradle.tooling.TestLauncher>
meth public abstract !varargs org.gradle.tooling.TestLauncher withJvmTestClasses(java.lang.String[])
meth public abstract !varargs org.gradle.tooling.TestLauncher withJvmTestMethods(java.lang.String,java.lang.String[])
meth public abstract !varargs org.gradle.tooling.TestLauncher withTests(org.gradle.tooling.events.test.TestOperationDescriptor[])
meth public abstract org.gradle.tooling.TestLauncher withJvmTestClasses(java.lang.Iterable<java.lang.String>)
meth public abstract org.gradle.tooling.TestLauncher withJvmTestMethods(java.lang.String,java.lang.Iterable<java.lang.String>)
meth public abstract org.gradle.tooling.TestLauncher withTests(java.lang.Iterable<? extends org.gradle.tooling.events.test.TestOperationDescriptor>)
meth public abstract void run()
meth public abstract void run(org.gradle.tooling.ResultHandler<? super java.lang.Void>)

CLSS public org.gradle.tooling.UnknownModelException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.UnsupportedVersionException

CLSS public org.gradle.tooling.UnsupportedVersionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public abstract interface org.gradle.tooling.events.FailureResult
intf org.gradle.tooling.events.OperationResult
meth public abstract java.util.List<? extends org.gradle.tooling.Failure> getFailures()

CLSS public abstract interface org.gradle.tooling.events.FinishEvent
intf org.gradle.tooling.events.ProgressEvent
meth public abstract org.gradle.tooling.events.OperationResult getResult()

CLSS public abstract interface org.gradle.tooling.events.OperationDescriptor
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract org.gradle.tooling.events.OperationDescriptor getParent()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.events.OperationResult
meth public abstract long getEndTime()
meth public abstract long getStartTime()

CLSS public final !enum org.gradle.tooling.events.OperationType
fld public final static org.gradle.tooling.events.OperationType GENERIC
fld public final static org.gradle.tooling.events.OperationType TASK
fld public final static org.gradle.tooling.events.OperationType TEST
meth public static org.gradle.tooling.events.OperationType valueOf(java.lang.String)
meth public static org.gradle.tooling.events.OperationType[] values()
supr java.lang.Enum<org.gradle.tooling.events.OperationType>

CLSS public abstract interface org.gradle.tooling.events.ProgressEvent
meth public abstract java.lang.String getDisplayName()
meth public abstract long getEventTime()
meth public abstract org.gradle.tooling.events.OperationDescriptor getDescriptor()

CLSS public abstract interface org.gradle.tooling.events.ProgressListener
meth public abstract void statusChanged(org.gradle.tooling.events.ProgressEvent)

CLSS public abstract interface org.gradle.tooling.events.SkippedResult
intf org.gradle.tooling.events.OperationResult

CLSS public abstract interface org.gradle.tooling.events.StartEvent
intf org.gradle.tooling.events.ProgressEvent

CLSS public abstract interface org.gradle.tooling.events.StatusEvent
intf org.gradle.tooling.events.ProgressEvent
meth public abstract java.lang.String getUnit()
meth public abstract long getProgress()
meth public abstract long getTotal()

CLSS public abstract interface org.gradle.tooling.events.SuccessResult
intf org.gradle.tooling.events.OperationResult

CLSS public final !enum org.gradle.tooling.events.test.JvmTestKind
fld public final static org.gradle.tooling.events.test.JvmTestKind ATOMIC
fld public final static org.gradle.tooling.events.test.JvmTestKind SUITE
fld public final static org.gradle.tooling.events.test.JvmTestKind UNKNOWN
meth public java.lang.String getLabel()
meth public static org.gradle.tooling.events.test.JvmTestKind valueOf(java.lang.String)
meth public static org.gradle.tooling.events.test.JvmTestKind[] values()
supr java.lang.Enum<org.gradle.tooling.events.test.JvmTestKind>
hfds label

CLSS public abstract interface org.gradle.tooling.events.test.JvmTestOperationDescriptor
intf org.gradle.tooling.events.test.TestOperationDescriptor
meth public abstract java.lang.String getClassName()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getMethodName()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getSuiteName()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.events.test.JvmTestKind getJvmTestKind()

CLSS public abstract interface org.gradle.tooling.events.test.TestFailureResult
intf org.gradle.tooling.events.FailureResult
intf org.gradle.tooling.events.test.TestOperationResult

CLSS public abstract interface org.gradle.tooling.events.test.TestFinishEvent
intf org.gradle.tooling.events.FinishEvent
intf org.gradle.tooling.events.test.TestProgressEvent
meth public abstract org.gradle.tooling.events.test.TestOperationResult getResult()

CLSS public abstract interface org.gradle.tooling.events.test.TestOperationDescriptor
intf org.gradle.tooling.events.OperationDescriptor

CLSS public abstract interface org.gradle.tooling.events.test.TestOperationResult
intf org.gradle.tooling.events.OperationResult

CLSS public abstract interface org.gradle.tooling.events.test.TestProgressEvent
intf org.gradle.tooling.events.ProgressEvent
meth public abstract org.gradle.tooling.events.test.TestOperationDescriptor getDescriptor()

CLSS public abstract interface org.gradle.tooling.events.test.TestSkippedResult
intf org.gradle.tooling.events.SkippedResult
intf org.gradle.tooling.events.test.TestOperationResult

CLSS public abstract interface org.gradle.tooling.events.test.TestStartEvent
intf org.gradle.tooling.events.StartEvent
intf org.gradle.tooling.events.test.TestProgressEvent

CLSS public abstract interface org.gradle.tooling.events.test.TestSuccessResult
intf org.gradle.tooling.events.SuccessResult
intf org.gradle.tooling.events.test.TestOperationResult

CLSS public org.gradle.tooling.exceptions.UnsupportedBuildArgumentException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.GradleConnectionException

CLSS public org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.gradle.tooling.UnsupportedVersionException

CLSS public abstract interface org.gradle.tooling.model.BuildIdentifier
intf org.gradle.tooling.model.Model
meth public abstract java.io.File getRootDir()

CLSS public abstract interface org.gradle.tooling.model.BuildModel
meth public abstract org.gradle.tooling.model.BuildIdentifier getBuildIdentifier()

CLSS public abstract interface org.gradle.tooling.model.BuildableElement
intf org.gradle.tooling.model.Element
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.Task> getTasks()

CLSS public abstract interface org.gradle.tooling.model.Dependency

CLSS public abstract interface org.gradle.tooling.model.DomainObjectSet<%0 extends java.lang.Object>
intf java.util.Set<{org.gradle.tooling.model.DomainObjectSet%0}>
meth public abstract java.util.List<{org.gradle.tooling.model.DomainObjectSet%0}> getAll()
meth public abstract {org.gradle.tooling.model.DomainObjectSet%0} getAt(int)

CLSS public abstract interface org.gradle.tooling.model.Element
intf org.gradle.tooling.model.Model
meth public abstract java.lang.String getDescription()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getName()

CLSS public abstract interface org.gradle.tooling.model.ExternalDependency
intf org.gradle.tooling.model.Dependency
meth public abstract boolean isExported()
meth public abstract java.io.File getFile()
meth public abstract java.io.File getJavadoc()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.io.File getSource()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.model.GradleModuleVersion getGradleModuleVersion()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.GradleModuleVersion
meth public abstract java.lang.String getGroup()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()

CLSS public abstract interface org.gradle.tooling.model.GradleProject
intf org.gradle.tooling.model.BuildableElement
intf org.gradle.tooling.model.HierarchicalElement
intf org.gradle.tooling.model.ProjectModel
meth public abstract java.io.File getBuildDirectory()
meth public abstract java.io.File getProjectDirectory()
meth public abstract java.lang.String getPath()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.GradleProject> getChildren()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.GradleTask> getTasks()
meth public abstract org.gradle.tooling.model.GradleProject findByPath(java.lang.String)
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.model.GradleProject getParent()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()
meth public abstract org.gradle.tooling.model.gradle.GradleScript getBuildScript()

CLSS public abstract interface org.gradle.tooling.model.GradleTask
intf org.gradle.tooling.model.Task
meth public abstract org.gradle.tooling.model.GradleProject getProject()

CLSS public abstract interface org.gradle.tooling.model.HasGradleProject
intf org.gradle.tooling.model.ProjectModel
meth public abstract org.gradle.tooling.model.GradleProject getGradleProject()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.HierarchicalElement
intf org.gradle.tooling.model.Element
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.HierarchicalElement> getChildren()
meth public abstract org.gradle.tooling.model.HierarchicalElement getParent()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.Launchable
intf org.gradle.tooling.model.ProjectModel
meth public abstract boolean isPublic()
meth public abstract java.lang.String getDescription()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getDisplayName()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.Model

CLSS public abstract interface org.gradle.tooling.model.ProjectDependency
intf org.gradle.tooling.model.Dependency

CLSS public abstract interface org.gradle.tooling.model.ProjectIdentifier
intf org.gradle.tooling.model.Model
meth public abstract java.lang.String getProjectPath()
meth public abstract org.gradle.tooling.model.BuildIdentifier getBuildIdentifier()

CLSS public abstract interface org.gradle.tooling.model.ProjectModel
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.SourceDirectory
meth public abstract java.io.File getDirectory()

CLSS public abstract interface org.gradle.tooling.model.Task
intf org.gradle.tooling.model.Launchable
meth public abstract java.lang.String getDescription()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getGroup()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPath()

CLSS public abstract interface org.gradle.tooling.model.TaskSelector
intf org.gradle.tooling.model.Launchable
meth public abstract java.lang.String getName()

CLSS public org.gradle.tooling.model.UnsupportedMethodException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.UnsupportedOperationException

CLSS public abstract interface org.gradle.tooling.model.build.BuildEnvironment
intf org.gradle.tooling.model.BuildModel
intf org.gradle.tooling.model.Model
meth public abstract org.gradle.tooling.model.BuildIdentifier getBuildIdentifier()
meth public abstract org.gradle.tooling.model.build.GradleEnvironment getGradle()
meth public abstract org.gradle.tooling.model.build.JavaEnvironment getJava()

CLSS public abstract interface org.gradle.tooling.model.build.GradleEnvironment
meth public abstract java.io.File getGradleUserHome()
meth public abstract java.lang.String getGradleVersion()

CLSS public abstract interface org.gradle.tooling.model.build.JavaEnvironment
meth public abstract java.io.File getJavaHome()
meth public abstract java.util.List<java.lang.String> getJvmArguments()

CLSS public abstract interface org.gradle.tooling.model.eclipse.AccessRule
meth public abstract int getKind()
meth public abstract java.lang.String getPattern()

CLSS public abstract interface org.gradle.tooling.model.eclipse.ClasspathAttribute
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseBuildCommand
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getArguments()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseClasspathContainer
intf org.gradle.tooling.model.eclipse.EclipseClasspathEntry
meth public abstract boolean isExported()
meth public abstract java.lang.String getPath()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseClasspathEntry
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.AccessRule> getAccessRules()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.ClasspathAttribute> getClasspathAttributes()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseExternalDependency
intf org.gradle.tooling.model.ExternalDependency
intf org.gradle.tooling.model.eclipse.EclipseClasspathEntry

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings
meth public abstract org.gradle.api.JavaVersion getSourceLanguageLevel()
meth public abstract org.gradle.api.JavaVersion getTargetBytecodeVersion()
meth public abstract org.gradle.tooling.model.java.InstalledJdk getJdk()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseLinkedResource
meth public abstract java.lang.String getLocation()
meth public abstract java.lang.String getLocationUri()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseOutputLocation
meth public abstract java.lang.String getPath()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseProject
intf org.gradle.tooling.model.eclipse.HierarchicalEclipseProject
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseBuildCommand> getBuildCommands()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseClasspathContainer> getClasspathContainers()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseExternalDependency> getClasspath()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseProject> getChildren()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseProjectNature> getProjectNatures()
meth public abstract org.gradle.tooling.model.GradleProject getGradleProject()
meth public abstract org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings getJavaSourceSettings()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.model.eclipse.EclipseOutputLocation getOutputLocation()
meth public abstract org.gradle.tooling.model.eclipse.EclipseProject getParent()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseProjectDependency
intf org.gradle.tooling.model.ProjectDependency
intf org.gradle.tooling.model.eclipse.EclipseClasspathEntry
meth public abstract boolean isExported()
meth public abstract java.lang.String getPath()
meth public abstract org.gradle.tooling.model.eclipse.HierarchicalEclipseProject getTargetProject()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseProjectNature
meth public abstract java.lang.String getId()

CLSS public abstract interface org.gradle.tooling.model.eclipse.EclipseSourceDirectory
intf org.gradle.tooling.model.SourceDirectory
intf org.gradle.tooling.model.eclipse.EclipseClasspathEntry
meth public abstract java.lang.String getOutput()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.lang.String getPath()
meth public abstract java.util.List<java.lang.String> getExcludes()
meth public abstract java.util.List<java.lang.String> getIncludes()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.ClasspathAttribute> getClasspathAttributes()

CLSS public abstract interface org.gradle.tooling.model.eclipse.HierarchicalEclipseProject
intf org.gradle.tooling.model.HasGradleProject
intf org.gradle.tooling.model.HierarchicalElement
meth public abstract java.io.File getProjectDirectory()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseLinkedResource> getLinkedResources()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseProjectDependency> getProjectDependencies()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.EclipseSourceDirectory> getSourceDirectories()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.eclipse.HierarchicalEclipseProject> getChildren()
meth public abstract org.gradle.tooling.model.eclipse.HierarchicalEclipseProject getParent()

CLSS public abstract interface org.gradle.tooling.model.gradle.BasicGradleProject
intf org.gradle.tooling.model.Model
intf org.gradle.tooling.model.ProjectModel
meth public abstract java.io.File getProjectDirectory()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPath()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.gradle.BasicGradleProject> getChildren()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()
meth public abstract org.gradle.tooling.model.gradle.BasicGradleProject getParent()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.gradle.BuildInvocations
intf org.gradle.tooling.model.Model
intf org.gradle.tooling.model.ProjectModel
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.Task> getTasks()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.TaskSelector> getTaskSelectors()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.gradle.GradleBuild
intf org.gradle.tooling.model.BuildModel
intf org.gradle.tooling.model.Model
meth public abstract org.gradle.tooling.model.BuildIdentifier getBuildIdentifier()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.gradle.BasicGradleProject> getProjects()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.gradle.GradleBuild> getEditableBuilds()
 anno 0 org.gradle.api.Incubating()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.gradle.GradleBuild> getIncludedBuilds()
meth public abstract org.gradle.tooling.model.gradle.BasicGradleProject getRootProject()

CLSS public abstract interface org.gradle.tooling.model.gradle.GradlePublication
intf org.gradle.tooling.model.ProjectModel
meth public abstract org.gradle.tooling.model.GradleModuleVersion getId()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.gradle.GradleScript
meth public abstract java.io.File getSourceFile()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.gradle.ProjectPublications
intf org.gradle.tooling.model.Model
intf org.gradle.tooling.model.ProjectModel
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.gradle.GradlePublication> getPublications()
meth public abstract org.gradle.tooling.model.ProjectIdentifier getProjectIdentifier()

CLSS public abstract interface org.gradle.tooling.model.idea.BasicIdeaProject
intf org.gradle.tooling.model.idea.IdeaProject

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaCompilerOutput
meth public abstract boolean getInheritOutputDirs()
meth public abstract java.io.File getOutputDir()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.io.File getTestOutputDir()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaContentRoot
meth public abstract java.io.File getRootDirectory()
meth public abstract java.util.Set<java.io.File> getExcludeDirectories()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getGeneratedSourceDirectories()
 anno 0 java.lang.Deprecated()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getGeneratedTestDirectories()
 anno 0 java.lang.Deprecated()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getResourceDirectories()
 anno 0 org.gradle.api.Incubating()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getSourceDirectories()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getTestDirectories()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaSourceDirectory> getTestResourceDirectories()
 anno 0 org.gradle.api.Incubating()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaDependency
intf org.gradle.tooling.model.Dependency
meth public abstract boolean getExported()
meth public abstract org.gradle.tooling.model.idea.IdeaDependencyScope getScope()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaDependencyScope
meth public abstract java.lang.String getScope()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaJavaLanguageSettings
meth public abstract org.gradle.api.JavaVersion getLanguageLevel()
meth public abstract org.gradle.api.JavaVersion getTargetBytecodeVersion()
meth public abstract org.gradle.tooling.model.java.InstalledJdk getJdk()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaLanguageLevel
meth public abstract java.lang.String getLevel()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaModule
intf org.gradle.tooling.model.HasGradleProject
intf org.gradle.tooling.model.HierarchicalElement
meth public abstract java.lang.String getJdkName()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaContentRoot> getContentRoots()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaDependency> getDependencies()
meth public abstract org.gradle.tooling.model.GradleProject getGradleProject()
meth public abstract org.gradle.tooling.model.idea.IdeaCompilerOutput getCompilerOutput()
meth public abstract org.gradle.tooling.model.idea.IdeaJavaLanguageSettings getJavaLanguageSettings()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract org.gradle.tooling.model.idea.IdeaProject getParent()
meth public abstract org.gradle.tooling.model.idea.IdeaProject getProject()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaModuleDependency
intf org.gradle.tooling.model.idea.IdeaDependency
meth public abstract java.lang.String getTargetModuleName()
meth public abstract org.gradle.tooling.model.idea.IdeaModule getDependencyModule()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaModuleIdentifier

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaProject
intf org.gradle.tooling.model.HierarchicalElement
meth public abstract java.lang.String getJdkName()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaModule> getChildren()
meth public abstract org.gradle.tooling.model.DomainObjectSet<? extends org.gradle.tooling.model.idea.IdeaModule> getModules()
meth public abstract org.gradle.tooling.model.idea.IdeaJavaLanguageSettings getJavaLanguageSettings()
meth public abstract org.gradle.tooling.model.idea.IdeaLanguageLevel getLanguageLevel()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaSingleEntryLibraryDependency
intf org.gradle.tooling.model.ExternalDependency
intf org.gradle.tooling.model.idea.IdeaDependency
meth public abstract java.io.File getFile()
meth public abstract java.io.File getJavadoc()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()
meth public abstract java.io.File getSource()
 anno 0 org.gradle.internal.impldep.javax.annotation.Nullable()

CLSS public abstract interface org.gradle.tooling.model.idea.IdeaSourceDirectory
intf org.gradle.tooling.model.SourceDirectory
meth public abstract boolean isGenerated()

CLSS public final org.netbeans.modules.gradle.api.GradleBaseProject
fld public final static java.lang.String PRIVATE_TASK_GROUP = "<private>"
intf java.io.Serializable
intf org.netbeans.modules.gradle.api.ModuleSearchSupport
meth public !varargs boolean hasPlugins(java.lang.String[])
meth public boolean isResolved()
meth public boolean isRoot()
meth public boolean isRootOf(org.netbeans.modules.gradle.api.GradleBaseProject)
meth public boolean isSibling(org.netbeans.modules.gradle.api.GradleBaseProject)
meth public java.io.File getBuildDir()
meth public java.io.File getProjectDir()
meth public java.io.File getRootDir()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getGroup()
meth public java.lang.String getLicense()
meth public java.lang.String getName()
meth public java.lang.String getNetBeansProperty(java.lang.String)
meth public java.lang.String getParentName()
meth public java.lang.String getPath()
meth public java.lang.String getStatus()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.gradle.api.GradleTask> getTasks()
meth public java.util.List<org.netbeans.modules.gradle.api.GradleTask> getTasks(java.lang.String)
meth public java.util.Map<java.lang.String,java.io.File> getIncludedBuilds()
meth public java.util.Map<java.lang.String,java.io.File> getSubProjects()
meth public java.util.Map<java.lang.String,org.netbeans.modules.gradle.api.GradleConfiguration> getConfigurations()
meth public java.util.Set<java.io.File> getBuildClassPath()
meth public java.util.Set<java.io.File> getGradleClassPath()
meth public java.util.Set<java.io.File> getOutputPaths()
meth public java.util.Set<java.lang.String> getPlugins()
meth public java.util.Set<java.lang.String> getTaskGroups()
meth public java.util.Set<java.lang.String> getTaskNames()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String)
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String,java.lang.String,java.lang.String)
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ProjectDependency> getProjectDependencies()
meth public org.netbeans.modules.gradle.api.GradleTask getTaskByName(java.lang.String)
meth public static org.netbeans.modules.gradle.api.GradleBaseProject get(org.netbeans.api.project.Project)
supr java.lang.Object
hfds buildClassPath,buildDir,componentsByFile,configurations,description,displayName,gradleClassPath,group,includedBuilds,license,name,netBeansProperties,outputPaths,parentName,path,plugins,projectDir,resolved,rootDir,status,subProjects,tasksByGroup,tasksByName,version

CLSS public final org.netbeans.modules.gradle.api.GradleConfiguration
intf java.io.Serializable
intf java.lang.Comparable<org.netbeans.modules.gradle.api.GradleConfiguration>
intf org.netbeans.modules.gradle.api.ModuleSearchSupport
meth public boolean isCanBeResolved()
meth public boolean isEmpty()
meth public boolean isResolved()
meth public boolean isTransitive()
meth public int compareTo(org.netbeans.modules.gradle.api.GradleConfiguration)
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleConfiguration> getAllParents()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleConfiguration> getExtendsFrom()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String)
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String,java.lang.String,java.lang.String)
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> getModules()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ProjectDependency> getProjects()
meth public java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$UnresolvedDependency> getUnresolved()
meth public org.netbeans.modules.gradle.api.GradleDependency$FileCollectionDependency getFiles()
supr java.lang.Object
hfds canBeResolved,description,extendsFrom,files,modules,name,projects,transitive,unresolved

CLSS public abstract org.netbeans.modules.gradle.api.GradleDependency
innr public final static !enum Type
innr public final static FileCollectionDependency
innr public final static ModuleDependency
innr public final static ProjectDependency
innr public final static UnresolvedDependency
intf java.io.Serializable
intf java.lang.Comparable<org.netbeans.modules.gradle.api.GradleDependency>
meth public abstract org.netbeans.modules.gradle.api.GradleDependency$Type getType()
meth public final java.lang.String getId()
meth public int compareTo(org.netbeans.modules.gradle.api.GradleDependency)
supr java.lang.Object
hfds id

CLSS public final static org.netbeans.modules.gradle.api.GradleDependency$FileCollectionDependency
 outer org.netbeans.modules.gradle.api.GradleDependency
meth public java.util.Set<java.io.File> getFiles()
meth public org.netbeans.modules.gradle.api.GradleDependency$Type getType()
supr org.netbeans.modules.gradle.api.GradleDependency
hfds files

CLSS public final static org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency
 outer org.netbeans.modules.gradle.api.GradleDependency
meth public boolean equals(java.lang.Object)
meth public boolean hasJavadocs()
meth public boolean hasSources()
meth public int compareTo(org.netbeans.modules.gradle.api.GradleDependency)
meth public int hashCode()
meth public java.lang.String getGroup()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.Set<java.io.File> getArtifacts()
meth public java.util.Set<java.io.File> getJavadoc()
meth public java.util.Set<java.io.File> getSources()
meth public org.netbeans.modules.gradle.api.GradleDependency$Type getType()
supr org.netbeans.modules.gradle.api.GradleDependency
hfds artifacts,group,javadoc,name,sources,version

CLSS public final static org.netbeans.modules.gradle.api.GradleDependency$ProjectDependency
 outer org.netbeans.modules.gradle.api.GradleDependency
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getPath()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public org.netbeans.modules.gradle.api.GradleDependency$Type getType()
supr org.netbeans.modules.gradle.api.GradleDependency
hfds description,path

CLSS public final static !enum org.netbeans.modules.gradle.api.GradleDependency$Type
 outer org.netbeans.modules.gradle.api.GradleDependency
fld public final static org.netbeans.modules.gradle.api.GradleDependency$Type FILE
fld public final static org.netbeans.modules.gradle.api.GradleDependency$Type MODULE
fld public final static org.netbeans.modules.gradle.api.GradleDependency$Type PROJECT
fld public final static org.netbeans.modules.gradle.api.GradleDependency$Type UNRESOLVED
meth public static org.netbeans.modules.gradle.api.GradleDependency$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.GradleDependency$Type[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.GradleDependency$Type>

CLSS public final static org.netbeans.modules.gradle.api.GradleDependency$UnresolvedDependency
 outer org.netbeans.modules.gradle.api.GradleDependency
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getProblem()
meth public java.lang.String toString()
meth public org.netbeans.modules.gradle.api.GradleDependency$Type getType()
supr org.netbeans.modules.gradle.api.GradleDependency
hfds problem

CLSS public final org.netbeans.modules.gradle.api.GradleProjects
meth public static boolean testForProject(java.io.File)
meth public static boolean testForRootProject(java.io.File)
meth public static java.io.File getJavadoc(java.io.File)
meth public static java.io.File getSources(java.io.File)
meth public static java.util.Map<java.lang.String,org.netbeans.api.project.Project> openedProjectDependencies(org.netbeans.api.project.Project)
meth public static java.util.Map<java.lang.String,org.netbeans.api.project.Project> openedSiblings(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public final org.netbeans.modules.gradle.api.GradleTask
intf java.io.Serializable
meth public boolean isPrivate()
meth public boolean matches(java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getGroup()
meth public java.lang.String getName()
meth public java.lang.String getPath()
supr java.lang.Object
hfds CAMLE_CASE_SPLITTER,description,group,name,path

CLSS public abstract interface org.netbeans.modules.gradle.api.ModuleSearchSupport
meth public abstract java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String)
meth public abstract java.util.Set<org.netbeans.modules.gradle.api.GradleDependency$ModuleDependency> findModules(java.lang.String,java.lang.String,java.lang.String)

CLSS public final org.netbeans.modules.gradle.api.NbGradleProject
fld public final static java.lang.String CODENAME_BASE = "org.netbeans.modules.gradle"
fld public final static java.lang.String GRADLE_PLUGIN_TYPE = "org-netbeans-modules-gradle/Plugins"
fld public final static java.lang.String GRADLE_PROJECT_TYPE = "org-netbeans-modules-gradle"
fld public final static java.lang.String PROP_PROJECT_INFO = "ProjectInfo"
fld public final static java.lang.String PROP_RESOURCES = "resources"
innr public final static !enum Quality
meth public <%0 extends java.lang.Object> {%%0} projectLookup(java.lang.Class<{%%0}>)
meth public boolean isGradleProjectLoaded()
meth public boolean isUnloadable()
meth public final static javax.swing.Icon getWarningIcon()
meth public java.lang.String toString()
meth public java.util.prefs.Preferences getPreferences(boolean)
meth public org.netbeans.modules.gradle.api.NbGradleProject$Quality getAimedQuality()
meth public org.netbeans.modules.gradle.api.NbGradleProject$Quality getQuality()
meth public static java.util.prefs.Preferences getPreferences(org.netbeans.api.project.Project,boolean)
meth public static javax.swing.ImageIcon getIcon()
meth public static org.netbeans.modules.gradle.api.NbGradleProject get(org.netbeans.api.project.Project)
meth public static void addPropertyChangeListener(org.netbeans.api.project.Project,java.beans.PropertyChangeListener)
meth public static void fireGradleProjectReload(org.netbeans.api.project.Project)
meth public static void removePropertyChangeListener(org.netbeans.api.project.Project,java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds FCHSL,GRADLE_ICON,WARNING_BADGE,privatePrefs,project,resources,sharedPrefs,support,warningIcon
hcls AccessorImpl

CLSS public final static !enum org.netbeans.modules.gradle.api.NbGradleProject$Quality
 outer org.netbeans.modules.gradle.api.NbGradleProject
fld public final static org.netbeans.modules.gradle.api.NbGradleProject$Quality EVALUATED
fld public final static org.netbeans.modules.gradle.api.NbGradleProject$Quality FALLBACK
fld public final static org.netbeans.modules.gradle.api.NbGradleProject$Quality FULL
fld public final static org.netbeans.modules.gradle.api.NbGradleProject$Quality FULL_ONLINE
fld public final static org.netbeans.modules.gradle.api.NbGradleProject$Quality SIMPLE
meth public boolean atLeast(org.netbeans.modules.gradle.api.NbGradleProject$Quality)
meth public boolean betterThan(org.netbeans.modules.gradle.api.NbGradleProject$Quality)
meth public boolean notBetterThan(org.netbeans.modules.gradle.api.NbGradleProject$Quality)
meth public boolean worseThan(org.netbeans.modules.gradle.api.NbGradleProject$Quality)
meth public static org.netbeans.modules.gradle.api.NbGradleProject$Quality valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.NbGradleProject$Quality[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.NbGradleProject$Quality>

CLSS public abstract interface org.netbeans.modules.gradle.api.NbProjectInfo
intf org.netbeans.modules.gradle.tooling.Model
meth public abstract boolean getMiscOnly()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getExt()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getInfo()
meth public abstract java.util.Set<java.lang.String> getProblems()

CLSS public abstract interface org.netbeans.modules.gradle.api.execute.ActionMapping
fld public final static java.lang.String CUSTOM_PREFIX = "custom-"
innr public final static !enum ReloadRule
intf java.io.Serializable
intf java.lang.Comparable<org.netbeans.modules.gradle.api.execute.ActionMapping>
meth public abstract boolean isApplicable(java.util.Set<java.lang.String>)
meth public abstract boolean isRepeatable()
meth public abstract java.lang.String getArgs()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getReloadArgs()
meth public abstract org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule getReloadRule()

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule
 outer org.netbeans.modules.gradle.api.execute.ActionMapping
fld public final static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule ALWAYS
fld public final static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule ALWAYS_ONLINE
fld public final static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule DEFAULT
fld public final static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule NEVER
meth public static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.ActionMapping$ReloadRule>

CLSS public final org.netbeans.modules.gradle.api.execute.GradleCommandLine
cons public !varargs init(java.lang.String[])
cons public init(java.lang.CharSequence)
cons public init(org.netbeans.modules.gradle.api.execute.GradleCommandLine)
fld public final static java.lang.String CHECK_TASK = "check"
fld public final static java.lang.String TEST_TASK = "test"
innr public final static !enum Flag
innr public final static !enum LogLevel
innr public final static !enum Parameter
innr public final static !enum Property
innr public final static !enum StackTrace
intf java.io.Serializable
meth public !varargs static org.netbeans.modules.gradle.api.execute.GradleCommandLine combine(org.netbeans.modules.gradle.api.execute.GradleCommandLine,org.netbeans.modules.gradle.api.execute.GradleCommandLine[])
meth public boolean canAdd(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag)
meth public boolean hasFlag(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag)
meth public boolean hasParameter(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter)
meth public boolean hasTask(java.lang.String)
meth public java.lang.String getFirstParameter(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter)
meth public java.lang.String getProperty(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property,java.lang.String)
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> getParameters(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter)
meth public java.util.List<java.lang.String> getFullCommandLine()
meth public java.util.List<java.lang.String> getSupportedCommandLine()
meth public java.util.Set<java.lang.String> getExcludedTasks()
meth public java.util.Set<java.lang.String> getTasks()
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine remove(org.netbeans.modules.gradle.api.execute.GradleCommandLine)
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel getLoglevel()
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace getStackTrace()
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine getDefaultCommandLine()
meth public void addFlag(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag)
meth public void addParameter(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter,java.lang.String)
meth public void addProjectProperty(java.lang.String,java.lang.String)
meth public void addSystemProperty(java.lang.String,java.lang.String)
meth public void addTask(java.lang.String)
meth public void configure(org.gradle.tooling.ConfigurableLauncher)
meth public void configure(org.gradle.tooling.ConfigurableLauncher,java.io.File)
meth public void removeFlag(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag)
meth public void removeParameter(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter,java.lang.String)
meth public void removeParameters(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter)
meth public void removeProperty(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property,java.lang.String)
meth public void removeTask(java.lang.String)
meth public void setExcludedTasks(java.util.Collection<java.lang.String>)
meth public void setFlag(org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag,boolean)
meth public void setLogLevel(org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel)
meth public void setStackTrace(org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace)
meth public void setTasks(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds LOGGER,PARSERS,PROP_JVMARGS,arguments,tasks
hcls Argument,ArgumentParser,FlagArgument,ParameterParser,ParametricArgument,PropertyArgument,PropertyParser

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag
 outer org.netbeans.modules.gradle.api.execute.GradleCommandLine
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag BUILD_CACHE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag CONFIGURE_ON_DEMAND
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag CONTINUE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag CONTINUOUS
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag DAEMON
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag DRY_RUN
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag FOREGROUND
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag GUI
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag HELP
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag LOG_DEBUG
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag LOG_INFO
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag LOG_QUIET
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag LOG_WARN
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_BUILD_CACHE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_CONFIGURE_ON_DEMAND
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_DAEMON
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_PARALLEL
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_REBUILD
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_SCAN
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag NO_SEARCH_UPWARD
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag OFFLINE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag PARALLEL
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag PROFILE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag RECOMPILE_SCRIPTS
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag REFRESH_DEPENDENCIES
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag RERUN_TASKS
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag SCAN
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag STACKTRACE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag STACKTRACE_FULL
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag STATUS
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag STOP
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag VERSION
meth public boolean isSupported()
meth public final java.lang.String getDescription()
meth public java.util.List<java.lang.String> getFlags()
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.GradleCommandLine$Flag>
hfds flags,incompatible,kind

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel
 outer org.netbeans.modules.gradle.api.execute.GradleCommandLine
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel DEBUG
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel INFO
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel LIFECYCLE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel QUIET
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel WARN
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel>

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter
 outer org.netbeans.modules.gradle.api.execute.GradleCommandLine
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter CONSOLE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter EXCLUDE_TASK
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter GRADLE_USER_HOME
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter IMPORT_BUILD
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter INIT_SCRIPT
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter MAX_WORKER
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter PROJECT_CACHE_DIR
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter PROJECT_DIR
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter SETTINGS_FILE
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.GradleCommandLine$Parameter>
hfds flags,kind

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property
 outer org.netbeans.modules.gradle.api.execute.GradleCommandLine
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property PROJECT
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property SYSTEM
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.GradleCommandLine$Property>
hfds flag,kind,prefix

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace
 outer org.netbeans.modules.gradle.api.execute.GradleCommandLine
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace FULL
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace NONE
fld public final static org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace SHORT
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace>

CLSS public final org.netbeans.modules.gradle.api.execute.RunConfig
cons public init(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.util.Set<org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag>,org.netbeans.modules.gradle.api.execute.GradleCommandLine)
innr public final static !enum ExecFlag
meth public java.lang.String getActionName()
meth public java.lang.String getTaskDisplayName()
meth public java.util.Set<org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag> getExecFlags()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine getCommandLine()
meth public org.netbeans.modules.gradle.api.execute.RunConfig withCommandLine(org.netbeans.modules.gradle.api.execute.GradleCommandLine)
supr java.lang.Object
hfds action,commandLine,displayName,execFlags,project

CLSS public final static !enum org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag
 outer org.netbeans.modules.gradle.api.execute.RunConfig
fld public final static org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag REPEATABLE
meth public static org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.api.execute.RunConfig$ExecFlag>

CLSS public final org.netbeans.modules.gradle.api.execute.RunUtils
fld public final static java.lang.String PROP_AUGMENTED_BUILD = "augmented.build"
fld public final static java.lang.String PROP_COMPILE_ON_SAVE = "compile.on.save"
fld public final static java.lang.String PROP_DEFAULT_CLI = "gradle.cli"
fld public final static java.lang.String PROP_JDK_PLATFORM = "jdkPlatform"
meth public static boolean isAugmentedBuildEnabled(org.netbeans.api.project.Project)
meth public static boolean isCompileOnSaveEnabled(org.netbeans.api.project.Project)
meth public static java.io.File evaluateGradleDistribution(org.netbeans.api.project.Project,boolean)
meth public static org.netbeans.modules.gradle.api.execute.GradleCommandLine getDefaultCommandLine(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.gradle.api.execute.RunConfig createRunConfig(org.netbeans.api.project.Project,java.lang.String,java.lang.String,java.lang.String[])
meth public static org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider simpleReplaceTokenProvider(java.lang.String,java.lang.String)
meth public static org.openide.execution.ExecutorTask executeGradle(org.netbeans.modules.gradle.api.execute.RunConfig,java.lang.String)
meth public static org.openide.filesystems.FileObject extractFileObjectfromLookup(org.openide.util.Lookup)
meth public static org.openide.filesystems.FileObject[] extractFileObjectsfromLookup(org.openide.util.Lookup)
meth public static org.openide.util.Pair<java.lang.String,org.netbeans.api.java.platform.JavaPlatform> getActivePlatform(java.lang.String)
meth public static org.openide.util.Pair<java.lang.String,org.netbeans.api.java.platform.JavaPlatform> getActivePlatform(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOG

CLSS abstract interface org.netbeans.modules.gradle.api.execute.package-info

CLSS public abstract org.netbeans.modules.gradle.api.output.OutputDisplayer
cons public init()
meth protected abstract void doPrint(java.lang.CharSequence,java.lang.Runnable,org.openide.windows.IOColors$OutputType)
meth public final org.netbeans.modules.gradle.api.output.OutputDisplayer print(java.lang.String)
meth public final org.netbeans.modules.gradle.api.output.OutputDisplayer print(java.lang.String,java.lang.Runnable)
meth public final org.netbeans.modules.gradle.api.output.OutputDisplayer print(java.lang.String,java.lang.Runnable,org.openide.windows.IOColors$OutputType)
supr java.lang.Object

CLSS public final org.netbeans.modules.gradle.api.output.OutputListeners
meth public static java.lang.Runnable displayStatusText(java.lang.String)
meth public static java.lang.Runnable openFileAt(org.openide.filesystems.FileObject,int,int)
meth public static java.lang.Runnable openURL(java.net.URL)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.gradle.api.output.OutputProcessor
meth public abstract boolean processLine(org.netbeans.modules.gradle.api.output.OutputDisplayer,java.lang.String)

CLSS public abstract interface org.netbeans.modules.gradle.api.output.OutputProcessorFactory
meth public abstract java.util.Set<? extends org.netbeans.modules.gradle.api.output.OutputProcessor> createOutputProcessors(org.netbeans.modules.gradle.api.execute.RunConfig)

CLSS abstract interface org.netbeans.modules.gradle.api.package-info

CLSS public final org.netbeans.modules.gradle.spi.GradleFiles
cons public init(java.io.File)
cons public init(java.io.File,boolean)
fld public final static java.lang.String BUILD_FILE_NAME = "build.gradle"
fld public final static java.lang.String GRADLE_PROPERTIES_NAME = "gradle.properties"
fld public final static java.lang.String SETTINGS_FILE_NAME = "settings.gradle"
fld public final static java.lang.String WRAPPER_PROPERTIES = "gradle/wrapper/gradle-wrapper.properties"
innr public final static !enum Kind
innr public static SettingsFile
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean hasWrapper()
meth public boolean isProject()
meth public boolean isRootProject()
meth public boolean isScriptlessSubProject()
meth public boolean isSubProject()
meth public int hashCode()
meth public java.io.File getBuildScript()
meth public java.io.File getFile(org.netbeans.modules.gradle.spi.GradleFiles$Kind)
meth public java.io.File getGradlew()
meth public java.io.File getParentScript()
meth public java.io.File getProjectDir()
meth public java.io.File getRootDir()
meth public java.io.File getSettingsScript()
meth public java.io.File getWrapperProperties()
meth public java.lang.String toString()
meth public java.util.List<java.io.File> getPropertyFiles()
meth public java.util.Set<java.io.File> getProjectFiles()
meth public long lastChanged()
supr java.lang.Object
hfds buildScript,gradlew,knownProject,parentScript,projectDir,rootDir,settingsScript,wrapperProperties

CLSS public final static !enum org.netbeans.modules.gradle.spi.GradleFiles$Kind
 outer org.netbeans.modules.gradle.spi.GradleFiles
fld public final static java.util.Set<org.netbeans.modules.gradle.spi.GradleFiles$Kind> PROJECT_FILES
fld public final static java.util.Set<org.netbeans.modules.gradle.spi.GradleFiles$Kind> PROPERTIES
fld public final static java.util.Set<org.netbeans.modules.gradle.spi.GradleFiles$Kind> SCRIPTS
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind BUILD_SCRIPT
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind PROJECT_PROPERTIES
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind ROOT_PROPERTIES
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind ROOT_SCRIPT
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind SETTINGS_SCRIPT
fld public final static org.netbeans.modules.gradle.spi.GradleFiles$Kind USER_PROPERTIES
meth public static org.netbeans.modules.gradle.spi.GradleFiles$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.spi.GradleFiles$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.spi.GradleFiles$Kind>

CLSS public static org.netbeans.modules.gradle.spi.GradleFiles$SettingsFile
 outer org.netbeans.modules.gradle.spi.GradleFiles
cons public init(java.io.File)
meth public static java.util.Set<java.io.File> getSubProjects(java.io.File)
supr java.lang.Object
hfds CACHE,SET_PROJECTDIR_PATTERN,subProjects,time

CLSS public abstract interface org.netbeans.modules.gradle.spi.GradleProgressListenerProvider
meth public abstract java.util.Set<org.gradle.tooling.events.OperationType> getSupportedOperationTypes()
meth public abstract org.gradle.tooling.events.ProgressListener getProgressListener()

CLSS public final org.netbeans.modules.gradle.spi.GradleSettings
cons public init()
fld public final static java.lang.String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput"
fld public final static java.lang.String PROP_DISABLE_CACHE = "disableCache"
fld public final static java.lang.String PROP_DISPLAY_DESCRIPTION = "displayDescription"
fld public final static java.lang.String PROP_DOWNLOAD_JAVADOC = "downloadJavaDoc"
fld public final static java.lang.String PROP_DOWNLOAD_LIBS = "downloadLibs"
fld public final static java.lang.String PROP_DOWNLOAD_SOURCES = "downloadSources"
fld public final static java.lang.String PROP_GRADLE_DISTRIBUTION = "gradleHome"
fld public final static java.lang.String PROP_GRADLE_USER_HOME = "gradleUserHome"
fld public final static java.lang.String PROP_GRADLE_VERSION = "gradleVersion"
fld public final static java.lang.String PROP_HIDE_EMPTY_CONF = "hideEmptyConfiguration"
fld public final static java.lang.String PROP_LAZY_OPEN_GROUPS = "lazyOpen"
fld public final static java.lang.String PROP_LOG_LEVEL = "logLevel"
fld public final static java.lang.String PROP_OPT_CONFIGURE_ON_DEMAND = "configureOnDemand"
fld public final static java.lang.String PROP_OPT_NO_REBUILD = "noRebuild"
fld public final static java.lang.String PROP_OPT_OFFLINE = "offline"
fld public final static java.lang.String PROP_PREFER_MAVEN = "preferMaven"
fld public final static java.lang.String PROP_PREFER_WRAPPER = "preferWrapper"
fld public final static java.lang.String PROP_REUSE_EDITOR_ON_STACKTRACE = "reuseEditorOnStackTace"
fld public final static java.lang.String PROP_REUSE_OUTPUT_TABS = "reuseOutputTabs"
fld public final static java.lang.String PROP_SILENT_INSTALL = "silentInstall"
fld public final static java.lang.String PROP_SKIP_CHECK = "skipCheck"
fld public final static java.lang.String PROP_SKIP_TEST = "skipTest"
fld public final static java.lang.String PROP_STACKTRACE = "stacktrace"
fld public final static java.lang.String PROP_START_DAEMON_ON_START = "startDaemonOnStart"
fld public final static java.lang.String PROP_USE_CUSTOM_GRADLE = "useCustomGradle"
innr public final static !enum DownloadLibsRule
innr public final static !enum DownloadMiscRule
meth public boolean getNoRebuild()
meth public boolean isAlwaysShowOutput()
meth public boolean isCacheDisabled()
meth public boolean isConfigureOnDemand()
meth public boolean isDisplayDesctiption()
meth public boolean isHideEmptyConfigurations()
meth public boolean isOffline()
meth public boolean isOpenLazy()
meth public boolean isPreferMaven()
meth public boolean isReuseEditorOnStackTace()
meth public boolean isReuseOutputTabs()
meth public boolean isSilentInstall()
meth public boolean isStartDaemonOnStart()
meth public boolean isWrapperPreferred()
meth public boolean skipCheck()
meth public boolean skipTest()
meth public boolean useCustomGradle()
meth public java.io.File getGradleUserHome()
meth public java.lang.String getDistributionHome()
meth public java.lang.String getGradleVersion()
meth public java.util.prefs.Preferences getPreferences()
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel getDefaultLogLevel()
meth public org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace getDefaultStackTrace()
meth public org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule getDownloadLibs()
meth public org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule getDownloadJavadoc()
meth public org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule getDownloadSources()
meth public static org.netbeans.modules.gradle.spi.GradleSettings getDefault()
meth public void setAlwaysShowOutput(boolean)
meth public void setCacheDisabled(boolean)
meth public void setConfigureOnDemand(boolean)
meth public void setDefaultLogLevel(org.netbeans.modules.gradle.api.execute.GradleCommandLine$LogLevel)
meth public void setDefaultStackTrace(org.netbeans.modules.gradle.api.execute.GradleCommandLine$StackTrace)
meth public void setDisplayDescription(boolean)
meth public void setDistributionHome(java.lang.String)
meth public void setDownloadJavadoc(org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule)
meth public void setDownloadLibs(org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule)
meth public void setDownloadSources(org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule)
meth public void setGradleUserHome(java.io.File)
meth public void setGradleVersion(java.lang.String)
meth public void setHideEmptyConfigurations(boolean)
meth public void setNoRebuild(boolean)
meth public void setOffline(boolean)
meth public void setOpenLazy(boolean)
meth public void setPreferMaven(boolean)
meth public void setReuseEditorOnStackTrace(boolean)
meth public void setReuseOutputTabs(boolean)
meth public void setSilentInstall(boolean)
meth public void setSkipCheck(boolean)
meth public void setSkipTest(boolean)
meth public void setStartDaemonOnStart(boolean)
meth public void setUseCustomGradle(boolean)
meth public void setWrapperPreferred(boolean)
supr java.lang.Object
hfds INSTANCE

CLSS public final static !enum org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule
 outer org.netbeans.modules.gradle.spi.GradleSettings
fld public final static org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule ALWAYS
fld public final static org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule AS_NEEDED
fld public final static org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule NEVER
meth public java.lang.String toString()
meth public static org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.spi.GradleSettings$DownloadLibsRule>

CLSS public final static !enum org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule
 outer org.netbeans.modules.gradle.spi.GradleSettings
fld public final static org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule ALWAYS
fld public final static org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule NEVER
meth public java.lang.String toString()
meth public static org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule valueOf(java.lang.String)
meth public static org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule[] values()
supr java.lang.Enum<org.netbeans.modules.gradle.spi.GradleSettings$DownloadMiscRule>

CLSS public abstract interface org.netbeans.modules.gradle.spi.ProjectIconProvider
meth public abstract boolean isGradleBadgeRequested()
meth public abstract java.awt.Image getIcon()

CLSS public abstract interface org.netbeans.modules.gradle.spi.ProjectInfoExtractor
innr public abstract interface static Result
innr public static DefaultResult
meth public abstract org.netbeans.modules.gradle.spi.ProjectInfoExtractor$Result extract(java.util.Map<java.lang.String,java.lang.Object>,java.util.Map<java.lang.Class,java.lang.Object>)
meth public abstract org.netbeans.modules.gradle.spi.ProjectInfoExtractor$Result fallback(org.netbeans.modules.gradle.spi.GradleFiles)

CLSS public static org.netbeans.modules.gradle.spi.ProjectInfoExtractor$DefaultResult
 outer org.netbeans.modules.gradle.spi.ProjectInfoExtractor
cons public !varargs init(java.lang.Object,java.lang.String[])
cons public init(java.lang.Object,java.util.Set<java.lang.String>)
intf org.netbeans.modules.gradle.spi.ProjectInfoExtractor$Result
meth public java.util.Set getExtract()
meth public java.util.Set<java.lang.String> getProblems()
supr java.lang.Object
hfds extract,problems

CLSS public abstract interface static org.netbeans.modules.gradle.spi.ProjectInfoExtractor$Result
 outer org.netbeans.modules.gradle.spi.ProjectInfoExtractor
fld public final static org.netbeans.modules.gradle.spi.ProjectInfoExtractor$Result NONE
meth public abstract java.util.Set getExtract()
meth public abstract java.util.Set<java.lang.String> getProblems()

CLSS public final org.netbeans.modules.gradle.spi.Utils
meth public static java.lang.String camelCaseToTitle(java.lang.String)
meth public static java.lang.String capitalize(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.gradle.spi.WatchedResourceProvider
meth public abstract java.util.Set<java.io.File> getWatchedResources()

CLSS public abstract interface org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook
meth public abstract void afterAction(java.lang.String,org.openide.util.Lookup,int,java.io.PrintWriter)

CLSS public abstract interface org.netbeans.modules.gradle.spi.actions.BeforeBuildActionHook
meth public abstract org.openide.util.Lookup beforeAction(java.lang.String,org.openide.util.Lookup,java.io.PrintWriter)

CLSS public abstract interface org.netbeans.modules.gradle.spi.actions.BeforeReloadActionHook
meth public abstract boolean beforeReload(java.lang.String,org.openide.util.Lookup,int,java.io.PrintWriter)

CLSS public abstract org.netbeans.modules.gradle.spi.actions.DefaultGradleActionsProvider
cons public !varargs init(java.lang.String[])
intf org.netbeans.modules.gradle.spi.actions.GradleActionsProvider
meth public boolean isActionEnabled(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public final java.io.InputStream defaultActionMapConfig()
meth public final java.util.Set<java.lang.String> getSupportedActions()
supr java.lang.Object
hfds supportedActions

CLSS public abstract interface org.netbeans.modules.gradle.spi.actions.GradleActionsProvider
meth public abstract boolean isActionEnabled(java.lang.String,org.netbeans.api.project.Project,org.openide.util.Lookup)
meth public abstract java.io.InputStream defaultActionMapConfig()
meth public abstract java.util.Set<java.lang.String> getSupportedActions()

CLSS public abstract interface org.netbeans.modules.gradle.spi.actions.ReplaceTokenProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> createReplacements(java.lang.String,org.openide.util.Lookup)
meth public abstract java.util.Set<java.lang.String> getSupportedTokens()

CLSS public final org.netbeans.modules.gradle.spi.customizer.support.FilterPanelProvider
cons public init(org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider,java.lang.String)
intf org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
meth public javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)
supr java.lang.Object
hfds original,plugin

CLSS public abstract org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator
cons public init()
fld public final static java.lang.String PROP_DESCRIPTION = "description"
fld public final static java.lang.String PROP_GROUP = "group"
fld public final static java.lang.String PROP_INIT_WRAPPER = "initWrapper"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PACKAGE_BASE = "packageBase"
fld public final static java.lang.String PROP_VERSION = "version"
intf org.openide.WizardDescriptor$ProgressInstantiatingIterator<org.openide.WizardDescriptor>
meth protected abstract java.lang.String getTitle()
meth protected abstract java.util.List<? extends org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>> createPanels()
meth protected abstract void collectOperations(org.netbeans.modules.gradle.spi.newproject.TemplateOperation,java.util.Map<java.lang.String,java.lang.Object>)
meth protected final java.io.File assumedRoot()
meth protected final org.openide.WizardDescriptor getData()
meth protected final static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createProjectAttributesPanel(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>)
meth public final boolean hasNext()
meth public final boolean hasPrevious()
meth public final java.util.Set instantiate() throws java.io.IOException
meth public final java.util.Set instantiate(org.netbeans.api.progress.ProgressHandle) throws java.io.IOException
meth public final org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> current()
meth public final void initialize(org.openide.WizardDescriptor)
meth public final void nextPanel()
meth public final void previousPanel()
meth public final void uninitialize(org.openide.WizardDescriptor)
meth public java.lang.String name()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds data,index,panels

CLSS public org.netbeans.modules.gradle.spi.newproject.SimpleGradleWizardIterator
cons public init(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
cons public init(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
fld public final static java.lang.String PROP_DEPENDENCIES = "dependencies"
fld public final static java.lang.String PROP_JAVA_VERSION = "javaVersion"
fld public final static java.lang.String PROP_MAIN_JAVA_DIR = "projectMainJavaDir"
fld public final static java.lang.String PROP_PLUGINS = "plugins"
fld public final static java.lang.String PROP_PROJECT_ROOT = "projectDir"
meth protected final java.lang.String getTitle()
meth protected java.util.List<? extends org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>> createPanels()
meth protected void collectOperations(org.netbeans.modules.gradle.spi.newproject.TemplateOperation,java.util.Map<java.lang.String,java.lang.Object>)
supr org.netbeans.modules.gradle.spi.newproject.BaseGradleWizardIterator
hfds TEMPLATE_BUILD,TEMPLATE_PROPS,TEMPLATE_SETTINGS,buildTemplate,templateParams,title

CLSS public final org.netbeans.modules.gradle.spi.newproject.TemplateOperation
cons public init()
cons public init(org.netbeans.api.progress.ProgressHandle)
innr public abstract interface static ProjectConfigurator
intf java.lang.Runnable
meth public java.util.Set<org.openide.filesystems.FileObject> getImportantFiles()
meth public void addConfigureProject(java.io.File,org.netbeans.modules.gradle.spi.newproject.TemplateOperation$ProjectConfigurator)
meth public void addProjectPreload(java.io.File)
meth public void addWrapperInit(java.io.File)
meth public void copyFromFile(java.lang.String,java.io.File,java.util.Map<java.lang.String,?>)
meth public void copyFromTemplate(java.lang.String,java.io.File,java.util.Map<java.lang.String,?>)
meth public void createFolder(java.io.File)
meth public void createPackage(java.io.File,java.lang.String)
meth public void openFromFile(java.lang.String,java.io.File,java.util.Map<java.lang.String,?>)
meth public void openFromTemplate(java.lang.String,java.io.File,java.util.Map<java.lang.String,?>)
meth public void run()
supr java.lang.Object
hfds handle,importantFiles,steps
hcls ConfigureProjectStep,CopyFromFileTemplate,CopyFromTemplate,CreateDirStep,InitGradleWrapper,OperationStep,PreloadProject

CLSS public abstract interface static org.netbeans.modules.gradle.spi.newproject.TemplateOperation$ProjectConfigurator
 outer org.netbeans.modules.gradle.spi.newproject.TemplateOperation
meth public abstract void configure(org.netbeans.api.project.Project)

CLSS public abstract org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList<%0 extends java.lang.Object>
cons public init()
intf org.netbeans.spi.project.ui.support.NodeList<{org.netbeans.modules.gradle.spi.nodes.AbstractGradleNodeList%0}>
meth protected void fireChange()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addNotify()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeNotify()
supr java.lang.Object
hfds cs

CLSS public org.netbeans.modules.gradle.spi.nodes.NodeUtils
meth public static java.awt.Image getTreeFolderIcon(boolean)
supr java.lang.Object
hfds ICON_KEY_UIMANAGER,ICON_KEY_UIMANAGER_NB,ICON_PATH,OPENED_ICON_KEY_UIMANAGER,OPENED_ICON_KEY_UIMANAGER_NB,OPENED_ICON_PATH

CLSS public abstract interface org.netbeans.modules.gradle.tooling.Model
intf java.io.Serializable
meth public abstract boolean hasException()
meth public abstract java.lang.String getGradleException()

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeList<%0 extends java.lang.Object>
meth public abstract java.util.List<{org.netbeans.spi.project.ui.support.NodeList%0}> keys()
meth public abstract org.openide.nodes.Node node({org.netbeans.spi.project.ui.support.NodeList%0})
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addNotify()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeNotify()

CLSS public final org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static CategoryComponentProvider
innr public abstract interface static CompositeCategoryProvider
innr public final static Category
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.ComboBoxModel encodingModel(java.lang.String)
meth public static javax.swing.ListCellRenderer encodingRenderer()
supr java.lang.Object
hfds LOG
hcls DelegateCategoryProvider,EncodingModel,EncodingRenderer

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
meth public abstract javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)

CLSS public org.openide.DialogDescriptor
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,int,java.lang.Object,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener)
cons public init(java.lang.Object,java.lang.String,boolean,java.lang.Object[],java.lang.Object,int,org.openide.util.HelpCtx,java.awt.event.ActionListener,boolean)
fld public final static int BOTTOM_ALIGN = 0
fld public final static int DEFAULT_ALIGN = 0
fld public final static int RIGHT_ALIGN = 1
fld public final static java.lang.String PROP_BUTTON_LISTENER = "buttonListener"
fld public final static java.lang.String PROP_CLOSING_OPTIONS = "closingOptions"
fld public final static java.lang.String PROP_HELP_CTX = "helpCtx"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_MODAL = "modal"
fld public final static java.lang.String PROP_OPTIONS_ALIGN = "optionsAlign"
intf org.openide.util.HelpCtx$Provider
meth public boolean isLeaf()
meth public boolean isModal()
meth public int getOptionsAlign()
meth public java.awt.event.ActionListener getButtonListener()
meth public java.lang.Object[] getClosingOptions()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public void setButtonListener(java.awt.event.ActionListener)
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setLeaf(boolean)
meth public void setModal(boolean)
meth public void setOptionsAlign(int)
supr org.openide.NotifyDescriptor
hfds DEFAULT_CLOSING_OPTIONS,buttonListener,closingOptions,helpCtx,leaf,modal,optionsAlign

CLSS public org.openide.NotifyDescriptor
cons public init(java.lang.Object,java.lang.String,int,int,java.lang.Object[],java.lang.Object)
fld public final static int DEFAULT_OPTION = -1
fld public final static int ERROR_MESSAGE = 0
fld public final static int INFORMATION_MESSAGE = 1
fld public final static int OK_CANCEL_OPTION = 2
fld public final static int PLAIN_MESSAGE = -1
fld public final static int QUESTION_MESSAGE = 3
fld public final static int WARNING_MESSAGE = 2
fld public final static int YES_NO_CANCEL_OPTION = 1
fld public final static int YES_NO_OPTION = 0
fld public final static java.lang.Object CANCEL_OPTION
fld public final static java.lang.Object CLOSED_OPTION
fld public final static java.lang.Object NO_OPTION
fld public final static java.lang.Object OK_OPTION
fld public final static java.lang.Object YES_OPTION
fld public final static java.lang.String PROP_DETAIL = "detail"
fld public final static java.lang.String PROP_ERROR_NOTIFICATION = "errorNotification"
fld public final static java.lang.String PROP_INFO_NOTIFICATION = "infoNotification"
fld public final static java.lang.String PROP_MESSAGE = "message"
fld public final static java.lang.String PROP_MESSAGE_TYPE = "messageType"
fld public final static java.lang.String PROP_NO_DEFAULT_CLOSE = "noDefaultClose"
fld public final static java.lang.String PROP_OPTIONS = "options"
fld public final static java.lang.String PROP_OPTION_TYPE = "optionType"
fld public final static java.lang.String PROP_TITLE = "title"
fld public final static java.lang.String PROP_VALID = "valid"
fld public final static java.lang.String PROP_VALUE = "value"
fld public final static java.lang.String PROP_WARNING_NOTIFICATION = "warningNotification"
innr public final static Exception
innr public static Confirmation
innr public static InputLine
innr public static Message
meth protected static java.lang.String getTitleForType(int)
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth public boolean isNoDefaultClose()
meth public final boolean isValid()
meth public final org.openide.NotificationLineSupport createNotificationLineSupport()
meth public final org.openide.NotificationLineSupport getNotificationLineSupport()
meth public final void setValid(boolean)
meth public int getMessageType()
meth public int getOptionType()
meth public java.lang.Object getDefaultValue()
meth public java.lang.Object getMessage()
meth public java.lang.Object getValue()
meth public java.lang.Object[] getAdditionalOptions()
meth public java.lang.Object[] getOptions()
meth public java.lang.String getTitle()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setMessage(java.lang.Object)
meth public void setMessageType(int)
meth public void setNoDefaultClose(boolean)
meth public void setOptionType(int)
meth public void setOptions(java.lang.Object[])
meth public void setTitle(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds MAXIMUM_TEXT_WIDTH,SIZE_PREFERRED_HEIGHT,SIZE_PREFERRED_WIDTH,adOptions,changeSupport,defaultValue,errMsg,infoMsg,message,messageType,noDefaultClose,notificationLineSupport,optionType,options,title,valid,value,warnMsg

CLSS public org.openide.WizardDescriptor
cons protected init()
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
cons public <%0 extends java.lang.Object> init(org.openide.WizardDescriptor$Panel<{%%0}>[],{%%0})
cons public init(org.openide.WizardDescriptor$Iterator<org.openide.WizardDescriptor>)
cons public init(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>[])
fld public final static java.lang.Object FINISH_OPTION
fld public final static java.lang.Object NEXT_OPTION
fld public final static java.lang.Object PREVIOUS_OPTION
fld public final static java.lang.String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"
fld public final static java.lang.String PROP_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor"
fld public final static java.lang.String PROP_CONTENT_DATA = "WizardPanel_contentData"
fld public final static java.lang.String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"
fld public final static java.lang.String PROP_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor"
fld public final static java.lang.String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"
fld public final static java.lang.String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"
fld public final static java.lang.String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"
fld public final static java.lang.String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"
fld public final static java.lang.String PROP_HELP_URL = "WizardPanel_helpURL"
fld public final static java.lang.String PROP_IMAGE = "WizardPanel_image"
fld public final static java.lang.String PROP_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment"
fld public final static java.lang.String PROP_INFO_MESSAGE = "WizardPanel_infoMessage"
fld public final static java.lang.String PROP_LEFT_DIMENSION = "WizardPanel_leftDimension"
fld public final static java.lang.String PROP_WARNING_MESSAGE = "WizardPanel_warningMessage"
innr public abstract interface static AsynchronousInstantiatingIterator
innr public abstract interface static AsynchronousValidatingPanel
innr public abstract interface static BackgroundInstantiatingIterator
innr public abstract interface static ExtendedAsynchronousValidatingPanel
innr public abstract interface static FinishPanel
innr public abstract interface static FinishablePanel
innr public abstract interface static InstantiatingIterator
innr public abstract interface static Iterator
innr public abstract interface static Panel
innr public abstract interface static ProgressInstantiatingIterator
innr public abstract interface static ValidatingPanel
innr public static ArrayIterator
meth protected void initialize()
meth protected void updateState()
meth public final <%0 extends java.lang.Object> void setPanelsAndSettings(org.openide.WizardDescriptor$Iterator<{%%0}>,{%%0})
meth public final void doCancelClick()
meth public final void doFinishClick()
meth public final void doNextClick()
meth public final void doPreviousClick()
meth public final void setPanels(org.openide.WizardDescriptor$Iterator)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getValue()
meth public java.text.MessageFormat getTitleFormat()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set getInstantiatedObjects()
meth public void putProperty(java.lang.String,java.lang.Object)
meth public void setAdditionalOptions(java.lang.Object[])
meth public void setClosingOptions(java.lang.Object[])
meth public void setHelpCtx(org.openide.util.HelpCtx)
meth public void setOptions(java.lang.Object[])
meth public void setTitleFormat(java.text.MessageFormat)
meth public void setValue(java.lang.Object)
supr org.openide.DialogDescriptor
hfds ASYNCHRONOUS_JOBS_RP,CLOSE_PREVENTER,PROGRESS_BAR_DISPLAY_NAME,addedWindowListener,autoWizardStyle,backgroundValidationTask,baseListener,bundle,cancelButton,changeStateInProgress,contentBackColor,contentData,contentForegroundColor,contentSelectedIndex,currentPanelWasChangedWhileStoreSettings,data,err,escapeActionListener,finishButton,finishOption,handle,helpURL,image,imageAlignment,init,initialized,isWizardWideHelpSet,logged,newObjects,nextButton,previousButton,propListener,properties,titleFormat,validationRuns,waitingComponent,weakCancelButtonListener,weakChangeListener,weakFinishButtonListener,weakNextButtonListener,weakPreviousButtonListener,weakPropertyChangeListener,wizardPanel
hcls BoundedHtmlBrowser,EmptyPanel,FinishAction,FixedHeightLabel,FixedHeightPane,ImagedPanel,Listener,PropL,SettingsAndIterator,WizardPanel,WrappedCellRenderer

CLSS public abstract interface static org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$InstantiatingIterator<{org.openide.WizardDescriptor$AsynchronousInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException

CLSS public abstract interface static org.openide.WizardDescriptor$InstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$Iterator<{org.openide.WizardDescriptor$InstantiatingIterator%0}>
meth public abstract java.util.Set instantiate() throws java.io.IOException
meth public abstract void initialize(org.openide.WizardDescriptor)
meth public abstract void uninitialize(org.openide.WizardDescriptor)

CLSS public abstract interface static org.openide.WizardDescriptor$Iterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
meth public abstract boolean hasNext()
meth public abstract boolean hasPrevious()
meth public abstract java.lang.String name()
meth public abstract org.openide.WizardDescriptor$Panel<{org.openide.WizardDescriptor$Iterator%0}> current()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void nextPanel()
meth public abstract void previousPanel()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface static org.openide.WizardDescriptor$ProgressInstantiatingIterator<%0 extends java.lang.Object>
 outer org.openide.WizardDescriptor
intf org.openide.WizardDescriptor$AsynchronousInstantiatingIterator<{org.openide.WizardDescriptor$ProgressInstantiatingIterator%0}>
meth public abstract java.util.Set instantiate(org.netbeans.api.progress.ProgressHandle) throws java.io.IOException

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

