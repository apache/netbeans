#Signature file v4.1
#Version 1.29

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public final org.netbeans.api.extexecution.base.BaseExecutionDescriptor
cons public init()
innr public abstract interface static InputProcessorFactory
innr public abstract interface static ReaderFactory
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor charset(java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor errProcessorFactory(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor inReaderFactory(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$ReaderFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor outProcessorFactory(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor postExecution(org.netbeans.api.extexecution.base.ParametrizedRunnable<java.lang.Integer>)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.BaseExecutionDescriptor preExecution(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds charset,errProcessorFactory,inReaderFactory,outProcessorFactory,postExecution,preExecution

CLSS public abstract interface static org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory
 outer org.netbeans.api.extexecution.base.BaseExecutionDescriptor
meth public abstract org.netbeans.api.extexecution.base.input.InputProcessor newInputProcessor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static org.netbeans.api.extexecution.base.BaseExecutionDescriptor$ReaderFactory
 outer org.netbeans.api.extexecution.base.BaseExecutionDescriptor
meth public abstract java.io.Reader newReader()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public final org.netbeans.api.extexecution.base.BaseExecutionService
meth public java.util.concurrent.Future<java.lang.Integer> run()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.BaseExecutionService newService(java.util.concurrent.Callable<? extends java.lang.Process>,org.netbeans.api.extexecution.base.BaseExecutionDescriptor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds EXECUTOR_SERVICE,EXECUTOR_SHUTDOWN_SLICE,LOGGER,RUNNING_PROCESSES,descriptor,processCreator
hcls WrappedException

CLSS public final org.netbeans.api.extexecution.base.Environment
meth public java.lang.String getVariable(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Map<java.lang.String,java.lang.String> values()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void appendPath(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void prependPath(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void removeVariable(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setVariable(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds implementation

CLSS public final org.netbeans.api.extexecution.base.ExplicitProcessParameters
innr public final static Builder
meth public !varargs java.util.List<java.lang.String> getAllArguments(java.lang.String[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public boolean isArgReplacement()
meth public boolean isEmpty()
meth public boolean isLauncherArgReplacement()
meth public java.io.File getWorkingDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<java.lang.String> getAllArguments(java.util.List<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<java.lang.String> getArguments()
meth public java.util.List<java.lang.String> getLauncherArguments()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvironmentVariables()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.ExplicitProcessParameters buildExplicitParameters(java.util.Collection<? extends org.netbeans.api.extexecution.base.ExplicitProcessParameters>)
meth public static org.netbeans.api.extexecution.base.ExplicitProcessParameters buildExplicitParameters(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.ExplicitProcessParameters empty()
meth public static org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder builder()
supr java.lang.Object
hfds EMPTY,arguments,environmentVars,launcherArguments,position,replaceArgs,replaceLauncherArgs,workingDirectory

CLSS public final static org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder
 outer org.netbeans.api.extexecution.base.ExplicitProcessParameters
cons public init()
meth public !varargs org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder args(java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public !varargs org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder launcherArgs(java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters build()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder arg(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder args(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder combine(org.netbeans.api.extexecution.base.ExplicitProcessParameters)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder environmentVariable(java.lang.String,java.lang.String)
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder environmentVariables(java.util.Map<java.lang.String,java.lang.String>)
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder launcherArg(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder launcherArgs(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder position(int)
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder replaceArgs(boolean)
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder replaceLauncherArgs(boolean)
meth public org.netbeans.api.extexecution.base.ExplicitProcessParameters$Builder workingDirectory(java.io.File)
supr java.lang.Object
hfds arguments,environmentVars,launcherArguments,position,replaceArgs,replaceLauncherArgs,workingDirectory

CLSS public abstract interface org.netbeans.api.extexecution.base.ParametrizedRunnable<%0 extends java.lang.Object>
meth public abstract void run({org.netbeans.api.extexecution.base.ParametrizedRunnable%0})

CLSS public final org.netbeans.api.extexecution.base.ProcessBuilder
intf java.util.concurrent.Callable<java.lang.Process>
intf org.openide.util.Lookup$Provider
meth public java.lang.Process call() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.base.Environment getEnvironment()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getLookup()
meth public static org.netbeans.api.extexecution.base.ProcessBuilder getLocal()
meth public void setArguments(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setExecutable(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setRedirectErrorStream(boolean)
meth public void setWorkingDirectory(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds arguments,description,executable,implementation,lock,redirectErrorStream,workingDirectory
hcls LocalEnvironment,LocalProcessBuilder

CLSS public final org.netbeans.api.extexecution.base.Processes
meth public static void killTree(java.lang.Process,java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface org.netbeans.api.extexecution.base.input.InputProcessor
intf java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException
meth public abstract void processInput(char[]) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void reset() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.base.input.InputProcessors
meth public !varargs static org.netbeans.api.extexecution.base.input.InputProcessor proxy(org.netbeans.api.extexecution.base.input.InputProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputProcessor ansiStripping(org.netbeans.api.extexecution.base.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputProcessor bridge(org.netbeans.api.extexecution.base.input.LineProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputProcessor copying(java.io.Writer)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputProcessor printing(java.io.PrintWriter)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls AnsiStrippingInputProcessor,Bridge,CopyingInputProcessor,PrintingInputProcessor,ProxyInputProcessor

CLSS public abstract interface org.netbeans.api.extexecution.base.input.InputReader
intf java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract int readInput(org.netbeans.api.extexecution.base.input.InputProcessor) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void close() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.base.input.InputReaderTask
intf java.lang.Runnable
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public static org.netbeans.api.extexecution.base.input.InputReaderTask newDrainingTask(org.netbeans.api.extexecution.base.input.InputReader,org.netbeans.api.extexecution.base.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.extexecution.base.input.InputReaderTask newTask(org.netbeans.api.extexecution.base.input.InputReader,org.netbeans.api.extexecution.base.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public void run()
supr java.lang.Object
hfds DELAY_INCREMENT,LOGGER,MAX_DELAY,MIN_DELAY,cancelled,draining,inputProcessor,inputReader,running

CLSS public final org.netbeans.api.extexecution.base.input.InputReaders
innr public final static FileInput
meth public static org.netbeans.api.extexecution.base.input.InputReader forFile(java.io.File,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputReader forFileInputProvider(org.netbeans.api.extexecution.base.input.InputReaders$FileInput$Provider)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputReader forReader(java.io.Reader)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputReader forStream(java.io.InputStream,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.api.extexecution.base.input.InputReaders$FileInput
 outer org.netbeans.api.extexecution.base.input.InputReaders
cons public init(java.io.File,java.nio.charset.Charset)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
innr public abstract interface static Provider
meth public java.io.File getFile()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.nio.charset.Charset getCharset()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds charset,file

CLSS public abstract interface static org.netbeans.api.extexecution.base.input.InputReaders$FileInput$Provider
 outer org.netbeans.api.extexecution.base.input.InputReaders$FileInput
meth public abstract org.netbeans.api.extexecution.base.input.InputReaders$FileInput getFileInput()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.api.extexecution.base.input.LineProcessor
intf java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close()
meth public abstract void processLine(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void reset()

CLSS public final org.netbeans.api.extexecution.base.input.LineProcessors
meth public !varargs static org.netbeans.api.extexecution.base.input.LineProcessor proxy(org.netbeans.api.extexecution.base.input.LineProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.LineProcessor patternWaiting(java.util.regex.Pattern,java.util.concurrent.CountDownLatch)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.LineProcessor printing(java.io.PrintWriter)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER
hcls PrintingLineProcessor,ProxyLineProcessor,WaitingLineProcessor

CLSS abstract interface org.netbeans.api.extexecution.base.input.package-info

CLSS abstract interface org.netbeans.api.extexecution.base.package-info

CLSS public final org.netbeans.spi.extexecution.base.EnvironmentFactory
meth public static org.netbeans.api.extexecution.base.Environment createEnvironment(org.netbeans.spi.extexecution.base.EnvironmentImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.extexecution.base.EnvironmentImplementation
meth public abstract java.lang.String getVariable(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<java.lang.String,java.lang.String> values()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void appendPath(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void prependPath(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeVariable(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setVariable(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.spi.extexecution.base.ProcessBuilderFactory
meth public static org.netbeans.api.extexecution.base.ProcessBuilder createProcessBuilder(org.netbeans.spi.extexecution.base.ProcessBuilderImplementation,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.extexecution.base.ProcessBuilderImplementation
intf org.openide.util.Lookup$Provider
meth public abstract java.lang.Process createProcess(org.netbeans.spi.extexecution.base.ProcessParameters) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.extexecution.base.Environment getEnvironment()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.util.Lookup getLookup()

CLSS public final org.netbeans.spi.extexecution.base.ProcessParameters
meth public boolean isRedirectErrorStream()
meth public java.lang.String getExecutable()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getWorkingDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<java.lang.String> getArguments()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvironmentVariables()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds arguments,environmentVariables,executable,redirectErrorStream,workingDirectory

CLSS public abstract interface org.netbeans.spi.extexecution.base.ProcessesImplementation
meth public abstract void killTree(java.lang.Process,java.util.Map<java.lang.String,java.lang.String>)

CLSS abstract interface org.netbeans.spi.extexecution.base.package-info

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

