#Signature file v4.1
#Version 1.72

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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

CLSS public final org.netbeans.api.extexecution.ExecutionDescriptor
cons public init()
innr public abstract interface static InputProcessorFactory
innr public abstract interface static InputProcessorFactory2
innr public abstract interface static LineConvertorFactory
innr public abstract interface static RerunCallback
innr public abstract interface static RerunCondition
meth public org.netbeans.api.extexecution.ExecutionDescriptor charset(java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor controllable(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errLineBased(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor errProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor frontWindow(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor frontWindowOnError(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor inputOutput(org.openide.windows.InputOutput)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor inputVisible(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor noReset(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor optionsPath(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outConvertorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outLineBased(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory)
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor outProcessorFactory(org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor postExecution(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor postExecution(java.util.function.Consumer<java.lang.Integer>)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor preExecution(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor rerunCallback(org.netbeans.api.extexecution.ExecutionDescriptor$RerunCallback)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor rerunCondition(org.netbeans.api.extexecution.ExecutionDescriptor$RerunCondition)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.extexecution.ExecutionDescriptor showProgress(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExecutionDescriptor showSuspended(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER,charset,controllable,errConvertorFactory,errLineBased,errProcessorFactory,errProcessorFactory2,front,frontWindowOnError,input,inputOutput,noReset,optionsPath,outConvertorFactory,outLineBased,outProcessorFactory,outProcessorFactory2,postExecution,preExecution,progress,rerunCallback,rerunCondition,suspend
hcls DescriptorData

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory
 outer org.netbeans.api.extexecution.ExecutionDescriptor
 anno 0 java.lang.Deprecated()
meth public abstract org.netbeans.api.extexecution.input.InputProcessor newInputProcessor(org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2
 outer org.netbeans.api.extexecution.ExecutionDescriptor
meth public abstract org.netbeans.api.extexecution.base.input.InputProcessor newInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$LineConvertorFactory
 outer org.netbeans.api.extexecution.ExecutionDescriptor
meth public abstract org.netbeans.api.extexecution.print.LineConvertor newLineConvertor()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$RerunCallback
 outer org.netbeans.api.extexecution.ExecutionDescriptor
meth public abstract void performed(java.util.concurrent.Future<java.lang.Integer>)

CLSS public abstract interface static org.netbeans.api.extexecution.ExecutionDescriptor$RerunCondition
 outer org.netbeans.api.extexecution.ExecutionDescriptor
meth public abstract boolean isRerunPossible()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.api.extexecution.ExecutionService
meth public java.util.concurrent.Future<java.lang.Integer> run()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.ExecutionService newService(java.util.concurrent.Callable<java.lang.Process>,org.netbeans.api.extexecution.ExecutionDescriptor,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER,descriptor,originalDisplayName,processCreator
hcls ProgressAction,ProgressCancellable

CLSS public final org.netbeans.api.extexecution.ExternalProcessBuilder
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
intf java.util.concurrent.Callable<java.lang.Process>
meth public java.lang.Process call() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder addArgument(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder addEnvironmentVariable(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder prependPath(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder redirectErrorStream(boolean)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.extexecution.ExternalProcessBuilder workingDirectory(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds ESCAPED_PATTERN,LOGGER,PROXY_AUTHENTICATION_PASSWORD,PROXY_AUTHENTICATION_USERNAME,USE_PROXY_AUTHENTICATION,arguments,envVariables,executable,paths,redirectErrorStream,workingDirectory
hcls BuilderData

CLSS public final org.netbeans.api.extexecution.ExternalProcessSupport
 anno 0 java.lang.Deprecated()
meth public static void destroy(java.lang.Process,java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.api.extexecution.ProcessBuilder
 anno 0 java.lang.Deprecated()
intf java.util.concurrent.Callable<java.lang.Process>
meth public java.lang.Process call() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.ProcessBuilder getLocal()
meth public void setArguments(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setEnvironmentVariables(java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setExecutable(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setPaths(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setRedirectErrorStream(boolean)
meth public void setWorkingDirectory(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds arguments,description,envVariables,executable,implementation,paths,redirectErrorStream,workingDirectory
hcls LocalProcessFactory

CLSS public abstract interface org.netbeans.api.extexecution.input.InputProcessor
 anno 0 java.lang.Deprecated()
intf java.io.Closeable
meth public abstract void close() throws java.io.IOException
meth public abstract void processInput(char[]) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void reset() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.input.InputProcessors
 anno 0 java.lang.Deprecated()
meth public !varargs static org.netbeans.api.extexecution.input.InputProcessor proxy(org.netbeans.api.extexecution.input.InputProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor ansiStripping(org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor bridge(org.netbeans.api.extexecution.input.LineProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor copying(java.io.Writer)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.extexecution.input.InputReader
 anno 0 java.lang.Deprecated()
intf java.io.Closeable
meth public abstract int readInput(org.netbeans.api.extexecution.input.InputProcessor) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void close() throws java.io.IOException

CLSS public final org.netbeans.api.extexecution.input.InputReaderTask
 anno 0 java.lang.Deprecated()
intf java.lang.Runnable
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public static org.netbeans.api.extexecution.input.InputReaderTask newDrainingTask(org.netbeans.api.extexecution.input.InputReader,org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.extexecution.input.InputReaderTask newTask(org.netbeans.api.extexecution.input.InputReader,org.netbeans.api.extexecution.input.InputProcessor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public void run()
supr java.lang.Object
hfds delegate
hcls BaseInputReader

CLSS public final org.netbeans.api.extexecution.input.InputReaders
 anno 0 java.lang.Deprecated()
innr public final static FileInput
meth public static org.netbeans.api.extexecution.input.InputReader forFile(java.io.File,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forFileInputProvider(org.netbeans.api.extexecution.input.InputReaders$FileInput$Provider)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forReader(java.io.Reader)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.InputReader forStream(java.io.InputStream,java.nio.charset.Charset)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final static org.netbeans.api.extexecution.input.InputReaders$FileInput
 outer org.netbeans.api.extexecution.input.InputReaders
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

CLSS public abstract interface static org.netbeans.api.extexecution.input.InputReaders$FileInput$Provider
 outer org.netbeans.api.extexecution.input.InputReaders$FileInput
meth public abstract org.netbeans.api.extexecution.input.InputReaders$FileInput getFileInput()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.api.extexecution.input.LineProcessor
 anno 0 java.lang.Deprecated()
intf java.io.Closeable
meth public abstract void close()
meth public abstract void processLine(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void reset()

CLSS public final org.netbeans.api.extexecution.input.LineProcessors
 anno 0 java.lang.Deprecated()
meth public !varargs static org.netbeans.api.extexecution.input.LineProcessor proxy(org.netbeans.api.extexecution.input.LineProcessor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor patternWaiting(java.util.regex.Pattern,java.util.concurrent.CountDownLatch)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.input.LineProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls BaseLineProcessor,DelegatingLineProcessor

CLSS abstract interface org.netbeans.api.extexecution.input.package-info

CLSS abstract interface org.netbeans.api.extexecution.package-info

CLSS public final org.netbeans.api.extexecution.print.ConvertedLine
meth public java.lang.String getText()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.windows.OutputListener getListener()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.api.extexecution.print.ConvertedLine forText(java.lang.String,org.openide.windows.OutputListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds listener,text

CLSS public final org.netbeans.api.extexecution.print.InputProcessors
meth public static org.netbeans.api.extexecution.base.input.InputProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.InputProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOGGER
hcls PrintingInputProcessor

CLSS public abstract interface org.netbeans.api.extexecution.print.LineConvertor
meth public abstract java.util.List<org.netbeans.api.extexecution.print.ConvertedLine> convert(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.api.extexecution.print.LineConvertors
innr public abstract interface static FileLocator
meth public !varargs static org.netbeans.api.extexecution.print.LineConvertor proxy(org.netbeans.api.extexecution.print.LineConvertor[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.print.LineConvertor filePattern(org.netbeans.api.extexecution.print.LineConvertors$FileLocator,java.util.regex.Pattern,java.util.regex.Pattern,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.api.extexecution.print.LineConvertor httpUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT_FILE_HANDLER,DEFAULT_HTTP_HANDLER,LOGGER
hcls FilePatternConvertor,HttpUrlConvertor,ProxyLineConvertor

CLSS public abstract interface static org.netbeans.api.extexecution.print.LineConvertors$FileLocator
 outer org.netbeans.api.extexecution.print.LineConvertors
meth public abstract org.openide.filesystems.FileObject find(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.api.extexecution.print.LineProcessors
meth public static org.netbeans.api.extexecution.base.input.LineProcessor printing(org.openide.windows.OutputWriter,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.extexecution.base.input.LineProcessor printing(org.openide.windows.OutputWriter,org.netbeans.api.extexecution.print.LineConvertor,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOGGER
hcls PrintingLineProcessor

CLSS abstract interface org.netbeans.api.extexecution.print.package-info

CLSS public final org.netbeans.api.extexecution.startup.StartupExtender
innr public final static !enum StartMode
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<java.lang.String> getArguments()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<java.lang.String> getRawArguments()
meth public static java.util.List<org.netbeans.api.extexecution.startup.StartupExtender> getExtenders(org.openide.util.Lookup,org.netbeans.api.extexecution.startup.StartupExtender$StartMode)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOG,arguments,description,rawArguments

CLSS public final static !enum org.netbeans.api.extexecution.startup.StartupExtender$StartMode
 outer org.netbeans.api.extexecution.startup.StartupExtender
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode DEBUG
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode NORMAL
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode PROFILE
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode TEST_DEBUG
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode TEST_NORMAL
fld public final static org.netbeans.api.extexecution.startup.StartupExtender$StartMode TEST_PROFILE
meth public java.lang.String toString()
meth public static org.netbeans.api.extexecution.startup.StartupExtender$StartMode valueOf(java.lang.String)
meth public static org.netbeans.api.extexecution.startup.StartupExtender$StartMode[] values()
supr java.lang.Enum<org.netbeans.api.extexecution.startup.StartupExtender$StartMode>
hfds mode

CLSS abstract interface org.netbeans.api.extexecution.startup.package-info

CLSS public org.netbeans.spi.extexecution.ProcessBuilderFactory
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(org.netbeans.spi.extexecution.ProcessBuilderImplementation,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.extexecution.ProcessBuilderImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Process createProcess(java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,boolean) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.extexecution.destroy.ProcessDestroyPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void destroy(java.lang.Process,java.util.Map<java.lang.String,java.lang.String>)

CLSS abstract interface org.netbeans.spi.extexecution.destroy.package-info

CLSS public abstract interface org.netbeans.spi.extexecution.open.FileOpenHandler
meth public abstract void open(org.openide.filesystems.FileObject,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.extexecution.open.HttpOpenHandler
meth public abstract void open(java.net.URL)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.extexecution.open.OptionOpenHandler
meth public abstract void open(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS abstract interface org.netbeans.spi.extexecution.open.package-info

CLSS abstract interface org.netbeans.spi.extexecution.package-info

CLSS public abstract interface org.netbeans.spi.extexecution.startup.StartupExtenderImplementation
innr public abstract interface static !annotation Registration
meth public abstract java.util.List<java.lang.String> getArguments(org.openide.util.Lookup,org.netbeans.api.extexecution.startup.StartupExtender$StartMode)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static !annotation org.netbeans.spi.extexecution.startup.StartupExtenderImplementation$Registration
 outer org.netbeans.spi.extexecution.startup.StartupExtenderImplementation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean argumentsQuoted()
meth public abstract !hasdefault int position()
meth public abstract java.lang.String displayName()
meth public abstract org.netbeans.api.extexecution.startup.StartupExtender$StartMode[] startMode()

CLSS abstract interface org.netbeans.spi.extexecution.startup.package-info

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

