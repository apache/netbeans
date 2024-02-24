#Signature file v4.1
#Version 0.53

CLSS public abstract interface java.io.Serializable

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

CLSS public org.netbeans.modules.php.api.executable.InvalidPhpExecutableException
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.netbeans.modules.php.api.executable.PhpExecutable
cons public init(java.lang.String)
fld public final static org.netbeans.api.extexecution.ExecutionDescriptor DEFAULT_EXECUTION_DESCRIPTOR
fld public final static org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2 ANSI_STRIPPING_FACTORY
meth public java.lang.Integer debug(org.openide.filesystems.FileObject) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Integer debug(org.openide.filesystems.FileObject,org.netbeans.api.extexecution.ExecutionDescriptor) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Integer debug(org.openide.filesystems.FileObject,org.netbeans.api.extexecution.ExecutionDescriptor,org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.Integer runAndWait(java.lang.String) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Integer runAndWait(org.netbeans.api.extexecution.ExecutionDescriptor,java.lang.String) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Integer runAndWait(org.netbeans.api.extexecution.ExecutionDescriptor,org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2,java.lang.String) throws java.util.concurrent.ExecutionException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getCommand()
meth public java.lang.String getExecutable()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getParameters()
meth public java.util.concurrent.Future<java.lang.Integer> run()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.concurrent.Future<java.lang.Integer> run(org.netbeans.api.extexecution.ExecutionDescriptor)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.concurrent.Future<java.lang.Integer> run(org.netbeans.api.extexecution.ExecutionDescriptor,org.netbeans.api.extexecution.ExecutionDescriptor$InputProcessorFactory2)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.php.api.executable.PhpExecutable additionalParameters(java.util.List<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.executable.PhpExecutable displayName(java.lang.String)
meth public org.netbeans.modules.php.api.executable.PhpExecutable environmentVariables(java.util.Map<java.lang.String,java.lang.String>)
meth public org.netbeans.modules.php.api.executable.PhpExecutable executableName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.executable.PhpExecutable fileOutput(java.io.File,java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.executable.PhpExecutable noDebugConfig(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable noInfo(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable optionsSubcategory(java.lang.String)
meth public org.netbeans.modules.php.api.executable.PhpExecutable redirectErrorStream(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable validationHandler(org.netbeans.modules.php.api.executable.PhpExecutableValidator$ValidationHandler)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.executable.PhpExecutable viaAutodetection(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable viaPhpInterpreter(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable warnUser(boolean)
meth public org.netbeans.modules.php.api.executable.PhpExecutable workDir(java.io.File)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DUMMY_PROJECT,LOGGER,additionalParameters,command,displayName,environmentVariables,executable,executableName,fileOutput,fileOutputOnly,fullCommand,noDebugConfig,noInfo,optionsSubcategory,outputCharset,parameters,redirectErrorStream,validationHandler,viaAutodetection,viaPhpInterpreter,warnUser,workDir
hcls DummyProject,InfoInputProcessor,RedirectOutputProcessor

CLSS public final org.netbeans.modules.php.api.executable.PhpExecutableValidator
innr public abstract interface static ValidationHandler
meth public static boolean isValidCommand(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String validateCommand(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String validateCommand(java.lang.String,org.netbeans.modules.php.api.executable.PhpExecutableValidator$ValidationHandler)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.php.api.executable.PhpExecutableValidator$ValidationHandler
 outer org.netbeans.modules.php.api.executable.PhpExecutableValidator
meth public abstract java.lang.String validate(java.lang.String)

CLSS public final org.netbeans.modules.php.api.executable.PhpInterpreter
fld public final static java.util.regex.Pattern[] LINE_PATTERNS
meth public java.lang.String getInterpreter()
meth public java.util.List<java.lang.String> getParameters()
meth public static org.netbeans.modules.php.api.executable.PhpInterpreter getCustom(java.lang.String) throws org.netbeans.modules.php.api.executable.InvalidPhpExecutableException
meth public static org.netbeans.modules.php.api.executable.PhpInterpreter getDefault() throws org.netbeans.modules.php.api.executable.InvalidPhpExecutableException
supr java.lang.Object
hfds executable

CLSS public abstract interface org.netbeans.modules.php.spi.executable.DebugStarter
innr public final static Properties
meth public abstract boolean isAlreadyRunning()
meth public abstract void start(org.netbeans.api.project.Project,java.util.concurrent.Callable<org.openide.util.Cancellable>,org.netbeans.modules.php.spi.executable.DebugStarter$Properties)
meth public abstract void stop()

CLSS public final static org.netbeans.modules.php.spi.executable.DebugStarter$Properties
 outer org.netbeans.modules.php.spi.executable.DebugStarter
innr public final static Builder
meth public boolean isCloseSession()
meth public java.lang.String getEncoding()
meth public java.util.List<org.openide.util.Pair<java.lang.String,java.lang.String>> getPathMapping()
meth public org.openide.filesystems.FileObject getStartFile()
meth public org.openide.util.Pair<java.lang.String,java.lang.Integer> getDebugProxy()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds closeSession,debugProxy,encoding,pathMapping,startFile

CLSS public final static org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder
 outer org.netbeans.modules.php.spi.executable.DebugStarter$Properties
cons public init()
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties build()
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder setCloseSession(boolean)
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder setDebugProxy(org.openide.util.Pair<java.lang.String,java.lang.Integer>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder setEncoding(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder setPathMapping(java.util.List<org.openide.util.Pair<java.lang.String,java.lang.String>>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.spi.executable.DebugStarter$Properties$Builder setStartFile(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds closeSession,debugProxy,encoding,pathMapping,startFile

