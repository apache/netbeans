#Signature file v4.1
#Version 1.30

CLSS public final com.oracle.truffle.api.ArrayUtils
meth public !varargs static int indexOf(byte[],int,int,byte[])
meth public !varargs static int indexOf(char[],int,int,char[])
meth public !varargs static int indexOf(java.lang.String,int,int,char[])
meth public static boolean regionEqualsWithOrMask(byte[],int,byte[],int,int,byte[])
meth public static boolean regionEqualsWithOrMask(char[],int,char[],int,int,char[])
meth public static boolean regionEqualsWithOrMask(java.lang.String,int,java.lang.String,int,int,java.lang.String)
meth public static int indexOfWithOrMask(byte[],int,int,byte[],byte[])
meth public static int indexOfWithOrMask(char[],int,int,char[],char[])
meth public static int indexOfWithOrMask(java.lang.String,int,int,java.lang.String,java.lang.String)
supr java.lang.Object
hfds UNSAFE,javaStringCoderFieldOffset,javaStringValueFieldOffset

CLSS public abstract interface com.oracle.truffle.api.Assumption
fld public final static com.oracle.truffle.api.Assumption ALWAYS_VALID
fld public final static com.oracle.truffle.api.Assumption NEVER_VALID
meth public abstract boolean isValid()
meth public abstract java.lang.String getName()
meth public abstract void check() throws com.oracle.truffle.api.nodes.InvalidAssumptionException
meth public abstract void invalidate()
meth public static boolean isValidAssumption(com.oracle.truffle.api.Assumption)
meth public static boolean isValidAssumption(com.oracle.truffle.api.Assumption[])
meth public static com.oracle.truffle.api.Assumption create()
meth public static com.oracle.truffle.api.Assumption create(java.lang.String)
meth public void invalidate(java.lang.String)

CLSS public abstract interface com.oracle.truffle.api.CallTarget
meth public abstract !varargs java.lang.Object call(java.lang.Object[])

CLSS public final com.oracle.truffle.api.CompilerAsserts
meth public static <%0 extends java.lang.Object> void compilationConstant(java.lang.Object)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(boolean)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(double)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(float)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(int)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(java.lang.Object)
meth public static <%0 extends java.lang.Object> void partialEvaluationConstant(long)
meth public static void neverPartOfCompilation()
meth public static void neverPartOfCompilation(java.lang.String)
supr java.lang.Object

CLSS public final com.oracle.truffle.api.CompilerDirectives
fld public final static double FASTPATH_PROBABILITY = 0.9999
fld public final static double LIKELY_PROBABILITY = 0.75
fld public final static double SLOWPATH_PROBABILITY = 1.0E-4
fld public final static double UNLIKELY_PROBABILITY = 0.25
innr public abstract interface static !annotation CompilationFinal
innr public abstract interface static !annotation TruffleBoundary
innr public abstract interface static !annotation ValueType
meth public static <%0 extends java.lang.Object> {%%0} castExact(java.lang.Object,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} interpreterOnly(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public static boolean hasNextTier()
meth public static boolean inCompilationRoot()
meth public static boolean inCompiledCode()
meth public static boolean inInterpreter()
meth public static boolean injectBranchProbability(double,boolean)
meth public static boolean isCompilationConstant(java.lang.Object)
meth public static boolean isExact(java.lang.Object,java.lang.Class<?>)
meth public static boolean isPartialEvaluationConstant(java.lang.Object)
meth public static java.lang.RuntimeException shouldNotReachHere()
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.String)
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.String,java.lang.Throwable)
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.Throwable)
meth public static void bailout(java.lang.String)
meth public static void blackhole(boolean)
meth public static void blackhole(byte)
meth public static void blackhole(char)
meth public static void blackhole(double)
meth public static void blackhole(float)
meth public static void blackhole(int)
meth public static void blackhole(java.lang.Object)
meth public static void blackhole(long)
meth public static void blackhole(short)
meth public static void ensureVirtualized(java.lang.Object)
meth public static void ensureVirtualizedHere(java.lang.Object)
meth public static void interpreterOnly(java.lang.Runnable)
meth public static void materialize(java.lang.Object)
meth public static void transferToInterpreter()
meth public static void transferToInterpreterAndInvalidate()
supr java.lang.Object
hcls ShouldNotReachHere

CLSS public abstract interface static !annotation com.oracle.truffle.api.CompilerDirectives$CompilationFinal
 outer com.oracle.truffle.api.CompilerDirectives
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int dimensions()

CLSS public abstract interface static !annotation com.oracle.truffle.api.CompilerDirectives$TruffleBoundary
 outer com.oracle.truffle.api.CompilerDirectives
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean allowInlining()
meth public abstract !hasdefault boolean transferToInterpreterOnException()

CLSS public abstract interface static !annotation com.oracle.truffle.api.CompilerDirectives$ValueType
 outer com.oracle.truffle.api.CompilerDirectives
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract com.oracle.truffle.api.ContextLocal<%0 extends java.lang.Object>
cons protected init(java.lang.Object)
meth public abstract {com.oracle.truffle.api.ContextLocal%0} get()
meth public abstract {com.oracle.truffle.api.ContextLocal%0} get(com.oracle.truffle.api.TruffleContext)
supr java.lang.Object

CLSS public abstract com.oracle.truffle.api.ContextThreadLocal<%0 extends java.lang.Object>
cons protected init(java.lang.Object)
meth public abstract {com.oracle.truffle.api.ContextThreadLocal%0} get()
meth public abstract {com.oracle.truffle.api.ContextThreadLocal%0} get(com.oracle.truffle.api.TruffleContext)
meth public abstract {com.oracle.truffle.api.ContextThreadLocal%0} get(com.oracle.truffle.api.TruffleContext,java.lang.Thread)
meth public abstract {com.oracle.truffle.api.ContextThreadLocal%0} get(java.lang.Thread)
supr java.lang.Object

CLSS public final com.oracle.truffle.api.ExactMath
meth public static double truncate(double)
meth public static float truncate(float)
meth public static int multiplyHigh(int,int)
meth public static int multiplyHighUnsigned(int,int)
meth public static long multiplyHigh(long,long)
meth public static long multiplyHighUnsigned(long,long)
supr java.lang.Object

CLSS public final com.oracle.truffle.api.HostCompilerDirectives
innr public abstract interface static !annotation BytecodeInterpreterSwitch
innr public abstract interface static !annotation BytecodeInterpreterSwitchBoundary
innr public abstract interface static !annotation InliningCutoff
meth public static boolean inInterpreterFastPath()
supr java.lang.Object

CLSS public abstract interface static !annotation com.oracle.truffle.api.HostCompilerDirectives$BytecodeInterpreterSwitch
 outer com.oracle.truffle.api.HostCompilerDirectives
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.HostCompilerDirectives$BytecodeInterpreterSwitchBoundary
 outer com.oracle.truffle.api.HostCompilerDirectives
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.HostCompilerDirectives$InliningCutoff
 outer com.oracle.truffle.api.HostCompilerDirectives
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation

CLSS public final com.oracle.truffle.api.InstrumentInfo
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
supr java.lang.Object
hfds id,name,polyglotInstrument,version

CLSS public abstract interface com.oracle.truffle.api.InternalResource
innr public abstract interface static !annotation Id
innr public final static !enum CPUArchitecture
innr public final static !enum OS
innr public final static Env
meth public abstract java.lang.String versionHash(com.oracle.truffle.api.InternalResource$Env)
meth public abstract void unpackFiles(com.oracle.truffle.api.InternalResource$Env,java.nio.file.Path) throws java.io.IOException

CLSS public final static !enum com.oracle.truffle.api.InternalResource$CPUArchitecture
 outer com.oracle.truffle.api.InternalResource
fld public final static com.oracle.truffle.api.InternalResource$CPUArchitecture AARCH64
fld public final static com.oracle.truffle.api.InternalResource$CPUArchitecture AMD64
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.InternalResource$CPUArchitecture getCurrent()
meth public static com.oracle.truffle.api.InternalResource$CPUArchitecture valueOf(java.lang.String)
meth public static com.oracle.truffle.api.InternalResource$CPUArchitecture[] values()
supr java.lang.Enum<com.oracle.truffle.api.InternalResource$CPUArchitecture>
hfds id

CLSS public final static com.oracle.truffle.api.InternalResource$Env
 outer com.oracle.truffle.api.InternalResource
meth public boolean inContextPreinitialization()
meth public boolean inNativeImageBuild()
meth public com.oracle.truffle.api.InternalResource$CPUArchitecture getCPUArchitecture()
meth public com.oracle.truffle.api.InternalResource$OS getOS()
meth public java.util.List<java.lang.String> readResourceLines(java.nio.file.Path) throws java.io.IOException
meth public void unpackResourceFiles(java.nio.file.Path,java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
supr java.lang.Object
hfds contextPreinitializationCheck,owner,resourceClass

CLSS public abstract interface static !annotation com.oracle.truffle.api.InternalResource$Id
 outer com.oracle.truffle.api.InternalResource
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()
meth public abstract !hasdefault java.lang.String componentId()
meth public abstract java.lang.String value()

CLSS public final static !enum com.oracle.truffle.api.InternalResource$OS
 outer com.oracle.truffle.api.InternalResource
fld public final static com.oracle.truffle.api.InternalResource$OS DARWIN
fld public final static com.oracle.truffle.api.InternalResource$OS LINUX
fld public final static com.oracle.truffle.api.InternalResource$OS WINDOWS
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.InternalResource$OS getCurrent()
meth public static com.oracle.truffle.api.InternalResource$OS valueOf(java.lang.String)
meth public static com.oracle.truffle.api.InternalResource$OS[] values()
supr java.lang.Enum<com.oracle.truffle.api.InternalResource$OS>
hfds id

CLSS public com.oracle.truffle.api.OptimizationFailedException
cons public init(java.lang.Throwable,com.oracle.truffle.api.RootCallTarget)
meth public com.oracle.truffle.api.RootCallTarget getCallTarget()
supr java.lang.RuntimeException
hfds callTarget,serialVersionUID

CLSS public abstract interface !annotation com.oracle.truffle.api.Option
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
innr public abstract interface static !annotation Group
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean deprecated()
meth public abstract !hasdefault java.lang.String deprecationMessage()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String usageSyntax()
meth public abstract !hasdefault org.graalvm.options.OptionStability stability()
meth public abstract !hasdefault org.graalvm.polyglot.SandboxPolicy sandbox()
meth public abstract java.lang.String help()
meth public abstract org.graalvm.options.OptionCategory category()

CLSS public abstract interface static !annotation com.oracle.truffle.api.Option$Group
 outer com.oracle.truffle.api.Option
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface com.oracle.truffle.api.ReplaceObserver
meth public abstract boolean nodeReplaced(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,java.lang.CharSequence)

CLSS public abstract interface com.oracle.truffle.api.RootCallTarget
intf com.oracle.truffle.api.CallTarget
meth public abstract com.oracle.truffle.api.nodes.RootNode getRootNode()

CLSS public abstract com.oracle.truffle.api.ThreadLocalAction
cons protected init(boolean,boolean)
cons protected init(boolean,boolean,boolean)
innr public abstract static Access
meth protected abstract void perform(com.oracle.truffle.api.ThreadLocalAction$Access)
supr java.lang.Object
hfds hasSideEffects,recurring,synchronous

CLSS public abstract static com.oracle.truffle.api.ThreadLocalAction$Access
 outer com.oracle.truffle.api.ThreadLocalAction
cons protected init(java.lang.Object)
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract com.oracle.truffle.api.nodes.Node getLocation()
meth public abstract java.lang.Thread getThread()
supr java.lang.Object

CLSS public final com.oracle.truffle.api.Truffle
meth public static com.oracle.truffle.api.TruffleRuntime getRuntime()
supr java.lang.Object
hfds RUNTIME

CLSS public final com.oracle.truffle.api.TruffleContext
innr public final Builder
intf java.lang.AutoCloseable
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} leaveAndEnter(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$Interrupter,com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction<{%%0},{%%1}>,{%%0})
meth public <%0 extends java.lang.Object> {%%0} leaveAndEnter(com.oracle.truffle.api.nodes.Node,java.util.function.Supplier<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean equals(java.lang.Object)
meth public boolean initializeInternal(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public boolean initializePublic(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public boolean isActive()
meth public boolean isCancelling()
meth public boolean isClosed()
meth public boolean isEntered()
meth public boolean isExiting()
meth public com.oracle.truffle.api.TruffleContext getParent()
meth public int hashCode()
meth public java.lang.Object enter(com.oracle.truffle.api.nodes.Node)
meth public java.lang.Object evalInternal(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.source.Source)
meth public java.lang.Object evalPublic(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.source.Source)
meth public java.util.concurrent.Future<java.lang.Void> pause()
meth public void close()
meth public void closeCancelled(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public void closeExited(com.oracle.truffle.api.nodes.Node,int)
meth public void closeResourceExhausted(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public void leave(com.oracle.truffle.api.nodes.Node,java.lang.Object)
meth public void resume(java.util.concurrent.Future<java.lang.Void>)
supr java.lang.Object
hfds CONTEXT_ASSERT_STACK,EMPTY,creator,polyglotContext

CLSS public final com.oracle.truffle.api.TruffleContext$Builder
 outer com.oracle.truffle.api.TruffleContext
meth public com.oracle.truffle.api.TruffleContext build()
meth public com.oracle.truffle.api.TruffleContext$Builder allowCreateProcess(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowCreateThread(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowHostClassLoading(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowHostClassLookup(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowIO(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowInheritEnvironmentAccess(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowInnerContextOptions(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowNativeAccess(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder allowPolyglotAccess(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder arguments(java.lang.String,java.lang.String[])
meth public com.oracle.truffle.api.TruffleContext$Builder config(java.lang.String,java.lang.Object)
meth public com.oracle.truffle.api.TruffleContext$Builder environment(java.lang.String,java.lang.String)
meth public com.oracle.truffle.api.TruffleContext$Builder environment(java.util.Map<java.lang.String,java.lang.String>)
meth public com.oracle.truffle.api.TruffleContext$Builder err(java.io.OutputStream)
meth public com.oracle.truffle.api.TruffleContext$Builder forceSharing(java.lang.Boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder in(java.io.InputStream)
meth public com.oracle.truffle.api.TruffleContext$Builder inheritAllAccess(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder initializeCreatorContext(boolean)
meth public com.oracle.truffle.api.TruffleContext$Builder onCancelled(java.lang.Runnable)
meth public com.oracle.truffle.api.TruffleContext$Builder onClosed(java.lang.Runnable)
meth public com.oracle.truffle.api.TruffleContext$Builder onExited(java.util.function.Consumer<java.lang.Integer>)
meth public com.oracle.truffle.api.TruffleContext$Builder option(java.lang.String,java.lang.String)
meth public com.oracle.truffle.api.TruffleContext$Builder options(java.util.Map<java.lang.String,java.lang.String>)
meth public com.oracle.truffle.api.TruffleContext$Builder out(java.io.OutputStream)
meth public com.oracle.truffle.api.TruffleContext$Builder timeZone(java.time.ZoneId)
supr java.lang.Object
hfds allowCreateProcess,allowCreateThread,allowEnvironmentAccess,allowHostClassLoading,allowHostLookup,allowIO,allowInnerContextOptions,allowNativeAccess,allowPolyglotAccess,arguments,config,environment,err,in,inheritAccess,initializeCreatorContext,onCancelled,onClosed,onExited,options,out,permittedLanguages,sharingEnabled,sourceEnvironment,timeZone

CLSS public final com.oracle.truffle.api.TruffleFile
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_DIRECTORY
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_OTHER
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_REGULAR_FILE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Boolean> IS_SYMBOLIC_LINK
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_GID
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_MODE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_NLINK
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Integer> UNIX_UID
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> SIZE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_DEV
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_INODE
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.lang.Long> UNIX_RDEV
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> CREATION_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> LAST_ACCESS_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> LAST_MODIFIED_TIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.FileTime> UNIX_CTIME
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.GroupPrincipal> UNIX_GROUP
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.nio.file.attribute.UserPrincipal> UNIX_OWNER
fld public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<java.util.Set<java.nio.file.attribute.PosixFilePermission>> UNIX_PERMISSIONS
innr public abstract interface static FileTypeDetector
innr public final static AttributeDescriptor
innr public final static Attributes
meth public !varargs <%0 extends java.lang.Object> void setAttribute(com.oracle.truffle.api.TruffleFile$AttributeDescriptor<{%%0}>,{%%0},java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs <%0 extends java.lang.Object> {%%0} getAttribute(com.oracle.truffle.api.TruffleFile$AttributeDescriptor<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs boolean exists(java.nio.file.LinkOption[])
meth public !varargs boolean isDirectory(java.nio.file.LinkOption[])
meth public !varargs boolean isRegularFile(java.nio.file.LinkOption[])
meth public !varargs boolean isSameFile(com.oracle.truffle.api.TruffleFile,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile getCanonicalFile(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile$Attributes getAttributes(java.util.Collection<? extends com.oracle.truffle.api.TruffleFile$AttributeDescriptor<?>>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.io.BufferedWriter newBufferedWriter(java.nio.charset.Charset,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.BufferedWriter newBufferedWriter(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.InputStream newInputStream(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.OutputStream newOutputStream(java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getCreationTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getLastAccessTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.FileTime getLastModifiedTime(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.GroupPrincipal getGroup(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.attribute.UserPrincipal getOwner(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.util.Set<java.nio.file.attribute.PosixFilePermission> getPosixPermissions(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs long size(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void copy(com.oracle.truffle.api.TruffleFile,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createDirectories(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createDirectory(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createFile(java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(com.oracle.truffle.api.TruffleFile,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(com.oracle.truffle.api.TruffleFile,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setCreationTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setLastAccessTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setLastModifiedTime(java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setPosixPermissions(java.util.Set<? extends java.nio.file.attribute.PosixFilePermission>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void visit(java.nio.file.FileVisitor<com.oracle.truffle.api.TruffleFile>,int,java.nio.file.FileVisitOption[]) throws java.io.IOException
meth public boolean endsWith(com.oracle.truffle.api.TruffleFile)
meth public boolean endsWith(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean isAbsolute()
meth public boolean isExecutable()
meth public boolean isReadable()
meth public boolean isSymbolicLink()
meth public boolean isWritable()
meth public boolean startsWith(com.oracle.truffle.api.TruffleFile)
meth public boolean startsWith(java.lang.String)
meth public byte[] readAllBytes() throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getAbsoluteFile()
meth public com.oracle.truffle.api.TruffleFile getParent()
meth public com.oracle.truffle.api.TruffleFile normalize()
meth public com.oracle.truffle.api.TruffleFile readSymbolicLink() throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile relativize(com.oracle.truffle.api.TruffleFile)
meth public com.oracle.truffle.api.TruffleFile resolve(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile resolveSibling(java.lang.String)
meth public int hashCode()
meth public java.io.BufferedReader newBufferedReader() throws java.io.IOException
meth public java.io.BufferedReader newBufferedReader(java.nio.charset.Charset) throws java.io.IOException
meth public java.lang.String detectMimeType()
meth public java.lang.String getMimeType() throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.net.URI toRelativeUri()
meth public java.net.URI toUri()
meth public java.nio.file.DirectoryStream<com.oracle.truffle.api.TruffleFile> newDirectoryStream() throws java.io.IOException
meth public java.util.Collection<com.oracle.truffle.api.TruffleFile> list() throws java.io.IOException
meth public void createLink(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public void delete() throws java.io.IOException
supr java.lang.Object
hfds BUFFER_SIZE,MAX_BUFFER_SIZE,fileSystemContext,isEmptyPath,normalizedPath,path
hcls AllFiles,AttributeGroup,ByteChannelDecorator,FileSystemContext,TempFileRandomHolder,TruffleFileDirectoryStream,Walker

CLSS public final static com.oracle.truffle.api.TruffleFile$AttributeDescriptor<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleFile
meth public java.lang.String toString()
supr java.lang.Object
hfds clazz,group,name

CLSS public final static com.oracle.truffle.api.TruffleFile$Attributes
 outer com.oracle.truffle.api.TruffleFile
meth public <%0 extends java.lang.Object> {%%0} get(com.oracle.truffle.api.TruffleFile$AttributeDescriptor<{%%0}>)
supr java.lang.Object
hfds delegate,queriedAttributes

CLSS public abstract interface static com.oracle.truffle.api.TruffleFile$FileTypeDetector
 outer com.oracle.truffle.api.TruffleFile
meth public abstract java.lang.String findMimeType(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public abstract java.nio.charset.Charset findEncoding(com.oracle.truffle.api.TruffleFile) throws java.io.IOException

CLSS public abstract com.oracle.truffle.api.TruffleLanguage<%0 extends java.lang.Object>
cons protected init()
fld protected final com.oracle.truffle.api.TruffleLanguage$ContextLocalProvider<{com.oracle.truffle.api.TruffleLanguage%0}> locals
innr protected abstract interface static ContextLocalFactory
innr protected abstract interface static ContextThreadLocalFactory
innr protected final static ContextLocalProvider
innr public abstract interface static !annotation Registration
innr public abstract interface static Provider
innr public abstract static ContextReference
innr public abstract static LanguageReference
innr public final static !enum ContextPolicy
innr public final static !enum ExitMode
innr public final static Env
innr public final static InlineParsingRequest
innr public final static ParsingRequest
meth protected abstract {com.oracle.truffle.api.TruffleLanguage%0} createContext(com.oracle.truffle.api.TruffleLanguage$Env)
meth protected boolean areOptionsCompatible(org.graalvm.options.OptionValues,org.graalvm.options.OptionValues)
meth protected boolean isThreadAccessAllowed(java.lang.Thread,boolean)
meth protected boolean isVisible({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected boolean patchContext({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.TruffleLanguage$Env)
meth protected com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.TruffleLanguage$ParsingRequest) throws java.lang.Exception
meth protected com.oracle.truffle.api.nodes.ExecutableNode parse(com.oracle.truffle.api.TruffleLanguage$InlineParsingRequest) throws java.lang.Exception
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected final int getAsynchronousStackDepth()
meth protected final java.lang.String getLanguageHome()
meth protected java.lang.Object getLanguageView({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected java.lang.Object getScope({com.oracle.truffle.api.TruffleLanguage%0})
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected static <%0 extends com.oracle.truffle.api.TruffleLanguage<?>> {%%0} getCurrentLanguage(java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.3")
meth protected static <%0 extends java.lang.Object, %1 extends com.oracle.truffle.api.TruffleLanguage<{%%0}>> {%%0} getCurrentContext(java.lang.Class<{%%1}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.3")
meth protected void disposeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void disposeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth protected void exitContext({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.TruffleLanguage$ExitMode,int)
meth protected void finalizeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void finalizeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth protected void initializeContext({com.oracle.truffle.api.TruffleLanguage%0}) throws java.lang.Exception
meth protected void initializeMultiThreading({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void initializeMultipleContexts()
meth protected void initializeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
supr java.lang.Object
hfds languageInfo,polyglotLanguageInstance

CLSS protected abstract interface static com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory%1} create({com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory%0})

CLSS protected final static com.oracle.truffle.api.TruffleLanguage$ContextLocalProvider<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<{com.oracle.truffle.api.TruffleLanguage$ContextLocalProvider%0},{%%0}>)
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<{com.oracle.truffle.api.TruffleLanguage$ContextLocalProvider%0},{%%0}>)
supr java.lang.Object
hfds contextLocals,contextThreadLocals

CLSS public final static !enum com.oracle.truffle.api.TruffleLanguage$ContextPolicy
 outer com.oracle.truffle.api.TruffleLanguage
fld public final static com.oracle.truffle.api.TruffleLanguage$ContextPolicy EXCLUSIVE
fld public final static com.oracle.truffle.api.TruffleLanguage$ContextPolicy REUSE
fld public final static com.oracle.truffle.api.TruffleLanguage$ContextPolicy SHARED
meth public static com.oracle.truffle.api.TruffleLanguage$ContextPolicy valueOf(java.lang.String)
meth public static com.oracle.truffle.api.TruffleLanguage$ContextPolicy[] values()
supr java.lang.Enum<com.oracle.truffle.api.TruffleLanguage$ContextPolicy>

CLSS public abstract static com.oracle.truffle.api.TruffleLanguage$ContextReference<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
cons protected init()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextReference%0} get(com.oracle.truffle.api.nodes.Node)
meth public static <%0 extends com.oracle.truffle.api.TruffleLanguage<{%%1}>, %1 extends java.lang.Object> com.oracle.truffle.api.TruffleLanguage$ContextReference<{%%1}> create(java.lang.Class<{%%0}>)
supr java.lang.Object

CLSS protected abstract interface static com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory%1} create({com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory%0},java.lang.Thread)

CLSS public final static com.oracle.truffle.api.TruffleLanguage$Env
 outer com.oracle.truffle.api.TruffleLanguage
meth public !varargs com.oracle.truffle.api.CallTarget parseInternal(com.oracle.truffle.api.source.Source,java.lang.String[])
meth public !varargs com.oracle.truffle.api.CallTarget parsePublic(com.oracle.truffle.api.source.Source,java.lang.String[])
meth public !varargs com.oracle.truffle.api.TruffleContext$Builder newInnerContextBuilder(java.lang.String[])
meth public !varargs com.oracle.truffle.api.TruffleFile createTempDirectory(com.oracle.truffle.api.TruffleFile,java.lang.String,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile createTempFile(com.oracle.truffle.api.TruffleFile,java.lang.String,java.lang.String,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.io.TruffleProcessBuilder newProcessBuilder(java.lang.String[])
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.InstrumentInfo,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public boolean initializeLanguage(com.oracle.truffle.api.nodes.LanguageInfo)
meth public boolean isCreateProcessAllowed()
meth public boolean isCreateThreadAllowed()
meth public boolean isFileIOAllowed()
meth public boolean isHostException(java.lang.Throwable)
meth public boolean isHostFunction(java.lang.Object)
meth public boolean isHostLookupAllowed()
meth public boolean isHostObject(java.lang.Object)
meth public boolean isHostSymbol(java.lang.Object)
meth public boolean isIOAllowed()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.0")
meth public boolean isInnerContextOptionsAllowed()
meth public boolean isMimeTypeSupported(java.lang.String)
meth public boolean isNativeAccessAllowed()
meth public boolean isPolyglotBindingsAccessAllowed()
meth public boolean isPolyglotEvalAllowed()
meth public boolean isPreInitialization()
meth public boolean isSocketIOAllowed()
meth public com.oracle.truffle.api.TruffleContext getContext()
meth public com.oracle.truffle.api.TruffleContext$Builder newContextBuilder()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public com.oracle.truffle.api.TruffleFile getCurrentWorkingDirectory()
meth public com.oracle.truffle.api.TruffleFile getInternalResource(java.lang.Class<? extends com.oracle.truffle.api.InternalResource>) throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getInternalResource(java.lang.String) throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getInternalTruffleFile(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getInternalTruffleFile(java.net.URI)
meth public com.oracle.truffle.api.TruffleFile getPublicTruffleFile(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getPublicTruffleFile(java.net.URI)
meth public com.oracle.truffle.api.TruffleFile getTruffleFileInternal(java.lang.String,java.util.function.Predicate<com.oracle.truffle.api.TruffleFile>)
meth public com.oracle.truffle.api.TruffleFile getTruffleFileInternal(java.net.URI,java.util.function.Predicate<com.oracle.truffle.api.TruffleFile>)
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.Class<?>)
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String)
meth public com.oracle.truffle.api.TruffleThreadBuilder newTruffleThreadBuilder(java.lang.Runnable)
meth public java.io.InputStream in()
meth public java.io.OutputStream err()
meth public java.io.OutputStream out()
meth public java.lang.Object asBoxedGuestValue(java.lang.Object)
meth public java.lang.Object asGuestValue(java.lang.Object)
meth public java.lang.Object asHostObject(java.lang.Object)
meth public java.lang.Object asHostSymbol(java.lang.Class<?>)
meth public java.lang.Object createHostAdapter(java.lang.Object[])
meth public java.lang.Object createHostAdapterClass(java.lang.Class<?>[])
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.1")
meth public java.lang.Object createHostAdapterClassWithStaticOverrides(java.lang.Class<?>[],java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.1")
meth public java.lang.Object createHostAdapterWithClassOverrides(java.lang.Object[],java.lang.Object)
meth public java.lang.Object findMetaObject(java.lang.Object)
meth public java.lang.Object getPolyglotBindings()
meth public java.lang.Object importSymbol(java.lang.String)
meth public java.lang.Object lookupHostSymbol(java.lang.String)
meth public java.lang.String getFileNameSeparator()
meth public java.lang.String getPathSeparator()
meth public java.lang.String[] getApplicationArguments()
meth public java.lang.Thread createSystemThread(java.lang.Runnable)
meth public java.lang.Thread createSystemThread(java.lang.Runnable,java.lang.ThreadGroup)
meth public java.lang.Thread createThread(java.lang.Runnable)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext,java.lang.ThreadGroup)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext,java.lang.ThreadGroup,long)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.Throwable asHostException(java.lang.Throwable)
meth public java.time.ZoneId getTimeZone()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.InstrumentInfo> getInstruments()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getInternalLanguages()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getPublicLanguages()
meth public java.util.Map<java.lang.String,java.lang.Object> getConfig()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvironment()
meth public java.util.concurrent.Future<java.lang.Void> submitThreadLocal(java.lang.Thread[],com.oracle.truffle.api.ThreadLocalAction)
meth public org.graalvm.options.OptionValues getOptions()
meth public org.graalvm.polyglot.SandboxPolicy getSandboxPolicy()
meth public void addToHostClassPath(com.oracle.truffle.api.TruffleFile)
meth public void exportSymbol(java.lang.String,java.lang.Object)
meth public void registerOnDispose(java.io.Closeable)
meth public void registerService(java.lang.Object)
meth public void setCurrentWorkingDirectory(com.oracle.truffle.api.TruffleFile)
supr java.lang.Object
hfds UNSET_CONTEXT,applicationArguments,config,context,contextUnchangedAssumption,err,in,initialized,initializedUnchangedAssumption,languageServicesCollector,options,out,polyglotLanguageContext,services,spi,valid
hcls TruffleFileFactory

CLSS public final static !enum com.oracle.truffle.api.TruffleLanguage$ExitMode
 outer com.oracle.truffle.api.TruffleLanguage
fld public final static com.oracle.truffle.api.TruffleLanguage$ExitMode HARD
fld public final static com.oracle.truffle.api.TruffleLanguage$ExitMode NATURAL
meth public static com.oracle.truffle.api.TruffleLanguage$ExitMode valueOf(java.lang.String)
meth public static com.oracle.truffle.api.TruffleLanguage$ExitMode[] values()
supr java.lang.Enum<com.oracle.truffle.api.TruffleLanguage$ExitMode>

CLSS public final static com.oracle.truffle.api.TruffleLanguage$InlineParsingRequest
 outer com.oracle.truffle.api.TruffleLanguage
meth public com.oracle.truffle.api.frame.MaterializedFrame getFrame()
meth public com.oracle.truffle.api.nodes.Node getLocation()
meth public com.oracle.truffle.api.source.Source getSource()
supr java.lang.Object
hfds disposed,frame,node,source

CLSS public abstract static com.oracle.truffle.api.TruffleLanguage$LanguageReference<%0 extends com.oracle.truffle.api.TruffleLanguage>
 outer com.oracle.truffle.api.TruffleLanguage
cons protected init()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$LanguageReference%0} get(com.oracle.truffle.api.nodes.Node)
meth public static <%0 extends com.oracle.truffle.api.TruffleLanguage<?>> com.oracle.truffle.api.TruffleLanguage$LanguageReference<{%%0}> create(java.lang.Class<{%%0}>)
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.TruffleLanguage$ParsingRequest
 outer com.oracle.truffle.api.TruffleLanguage
meth public com.oracle.truffle.api.source.Source getSource()
meth public java.util.List<java.lang.String> getArgumentNames()
supr java.lang.Object
hfds argumentNames,disposed,source

CLSS public abstract interface static com.oracle.truffle.api.TruffleLanguage$Provider
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.1")
meth public abstract com.oracle.truffle.api.TruffleLanguage<?> create()
meth public abstract java.lang.String getLanguageClassName()
meth public abstract java.util.Collection<java.lang.String> getServicesClassNames()
meth public abstract java.util.List<com.oracle.truffle.api.TruffleFile$FileTypeDetector> createFileTypeDetectors()

CLSS public abstract interface static !annotation com.oracle.truffle.api.TruffleLanguage$Registration
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean interactive()
meth public abstract !hasdefault boolean internal()
meth public abstract !hasdefault boolean needsAllEncodings()
meth public abstract !hasdefault com.oracle.truffle.api.TruffleLanguage$ContextPolicy contextPolicy()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.InternalResource>[] internalResources()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.TruffleFile$FileTypeDetector>[] fileTypeDetectors()
meth public abstract !hasdefault java.lang.Class<?>[] services()
meth public abstract !hasdefault java.lang.String defaultMimeType()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String implementationName()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String version()
meth public abstract !hasdefault java.lang.String website()
meth public abstract !hasdefault java.lang.String[] byteMimeTypes()
meth public abstract !hasdefault java.lang.String[] characterMimeTypes()
meth public abstract !hasdefault java.lang.String[] dependentLanguages()
meth public abstract !hasdefault org.graalvm.polyglot.SandboxPolicy sandbox()

CLSS public final com.oracle.truffle.api.TruffleLogger
meth public <%0 extends java.lang.Throwable> {%%0} throwing(java.lang.String,java.lang.String,{%%0})
meth public boolean isLoggable(java.util.logging.Level)
meth public com.oracle.truffle.api.TruffleLogger getParent()
meth public java.lang.String getName()
meth public static com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String)
meth public static com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String,java.lang.Class<?>)
meth public static com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String,java.lang.String)
meth public void config(java.lang.String)
meth public void config(java.util.function.Supplier<java.lang.String>)
meth public void entering(java.lang.String,java.lang.String)
meth public void entering(java.lang.String,java.lang.String,java.lang.Object)
meth public void entering(java.lang.String,java.lang.String,java.lang.Object[])
meth public void exiting(java.lang.String,java.lang.String)
meth public void exiting(java.lang.String,java.lang.String,java.lang.Object)
meth public void fine(java.lang.String)
meth public void fine(java.util.function.Supplier<java.lang.String>)
meth public void finer(java.lang.String)
meth public void finer(java.util.function.Supplier<java.lang.String>)
meth public void finest(java.lang.String)
meth public void finest(java.util.function.Supplier<java.lang.String>)
meth public void info(java.lang.String)
meth public void info(java.util.function.Supplier<java.lang.String>)
meth public void log(java.util.logging.Level,java.lang.String)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object)
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Object[])
meth public void log(java.util.logging.Level,java.lang.String,java.lang.Throwable)
meth public void log(java.util.logging.Level,java.lang.Throwable,java.util.function.Supplier<java.lang.String>)
meth public void log(java.util.logging.Level,java.util.function.Supplier<java.lang.String>)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Object[])
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.lang.Throwable,java.util.function.Supplier<java.lang.String>)
meth public void logp(java.util.logging.Level,java.lang.String,java.lang.String,java.util.function.Supplier<java.lang.String>)
meth public void severe(java.lang.String)
meth public void severe(java.util.function.Supplier<java.lang.String>)
meth public void warning(java.lang.String)
meth public void warning(java.util.function.Supplier<java.lang.String>)
supr java.lang.Object
hfds DEFAULT_VALUE,MAX_CLEANED_REFS,OFF_VALUE,ROOT_NAME,children,childrenLock,levelNum,levelNumStable,levelObj,loggerCache,loggersRefQueue,name,parent
hcls AbstractLoggerRef,ChildLoggerRef,LoggerCache

CLSS public abstract interface com.oracle.truffle.api.TruffleOptionDescriptors
intf org.graalvm.options.OptionDescriptors
meth public abstract org.graalvm.polyglot.SandboxPolicy getSandboxPolicy(java.lang.String)

CLSS public final com.oracle.truffle.api.TruffleOptions
fld public final static boolean AOT
fld public final static boolean DetailedRewriteReasons
fld public final static boolean TraceRewrites
fld public final static com.oracle.truffle.api.nodes.NodeCost TraceRewritesFilterFromCost
fld public final static com.oracle.truffle.api.nodes.NodeCost TraceRewritesFilterToCost
fld public final static java.lang.String TraceRewritesFilterClass
supr java.lang.Object

CLSS public abstract interface com.oracle.truffle.api.TruffleRuntime
meth public <%0 extends java.lang.Object> {%%0} iterateFrames(com.oracle.truffle.api.frame.FrameInstanceVisitor<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} iterateFrames(com.oracle.truffle.api.frame.FrameInstanceVisitor<{%%0}>,int)
meth public abstract <%0 extends java.lang.Object> {%%0} getCapability(java.lang.Class<{%%0}>)
meth public abstract boolean isProfilingEnabled()
meth public abstract com.oracle.truffle.api.Assumption createAssumption()
meth public abstract com.oracle.truffle.api.Assumption createAssumption(java.lang.String)
meth public abstract com.oracle.truffle.api.frame.MaterializedFrame createMaterializedFrame(java.lang.Object[])
meth public abstract com.oracle.truffle.api.frame.MaterializedFrame createMaterializedFrame(java.lang.Object[],com.oracle.truffle.api.frame.FrameDescriptor)
meth public abstract com.oracle.truffle.api.frame.VirtualFrame createVirtualFrame(java.lang.Object[],com.oracle.truffle.api.frame.FrameDescriptor)
meth public abstract com.oracle.truffle.api.nodes.DirectCallNode createDirectCallNode(com.oracle.truffle.api.CallTarget)
meth public abstract com.oracle.truffle.api.nodes.IndirectCallNode createIndirectCallNode()
meth public abstract com.oracle.truffle.api.nodes.LoopNode createLoopNode(com.oracle.truffle.api.nodes.RepeatingNode)
meth public abstract java.lang.String getName()
meth public abstract void notifyTransferToInterpreter()

CLSS public abstract interface com.oracle.truffle.api.TruffleRuntimeAccess
meth public abstract com.oracle.truffle.api.TruffleRuntime getRuntime()
meth public int getPriority()

CLSS public abstract com.oracle.truffle.api.TruffleSafepoint
cons protected init(com.oracle.truffle.api.impl.Accessor$EngineSupport)
innr public abstract interface static CompiledInterruptible
innr public abstract interface static CompiledInterruptibleFunction
innr public abstract interface static Interrupter
innr public abstract interface static Interruptible
innr public abstract interface static InterruptibleFunction
meth public <%0 extends java.lang.Object> void setBlockedWithException(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$Interrupter,com.oracle.truffle.api.TruffleSafepoint$Interruptible<{%%0}>,{%%0},java.lang.Runnable,java.util.function.Consumer<java.lang.Throwable>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} setBlockedFunction(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$Interrupter,com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction<{%%0},{%%1}>,{%%0},java.lang.Runnable,java.util.function.Consumer<java.lang.Throwable>)
meth public abstract <%0 extends java.lang.Object> void setBlocked(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$Interrupter,com.oracle.truffle.api.TruffleSafepoint$Interruptible<{%%0}>,{%%0},java.lang.Runnable,java.util.function.Consumer<java.lang.Throwable>)
meth public abstract boolean hasPendingSideEffectingActions()
meth public abstract boolean setAllowActions(boolean)
meth public abstract boolean setAllowSideEffects(boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} setBlockedThreadInterruptibleFunction(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction<{%%0},{%%1}>,{%%0})
meth public static <%0 extends java.lang.Object> void setBlockedThreadInterruptible(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.TruffleSafepoint$Interruptible<{%%0}>,{%%0})
meth public static com.oracle.truffle.api.TruffleSafepoint getCurrent()
meth public static void poll(com.oracle.truffle.api.nodes.Node)
meth public static void pollHere(com.oracle.truffle.api.nodes.Node)
supr java.lang.Object
hfds HANDSHAKE

CLSS public abstract interface static com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptible<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleSafepoint
 anno 0 java.lang.FunctionalInterface()
intf com.oracle.truffle.api.TruffleSafepoint$Interruptible<{com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptible%0}>
meth public abstract void apply({com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptible%0}) throws java.lang.InterruptedException

CLSS public abstract interface static com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptibleFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleSafepoint
 anno 0 java.lang.FunctionalInterface()
intf com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction<{com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptibleFunction%0},{com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptibleFunction%1}>
meth public abstract {com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptibleFunction%1} apply({com.oracle.truffle.api.TruffleSafepoint$CompiledInterruptibleFunction%0}) throws java.lang.InterruptedException

CLSS public abstract interface static com.oracle.truffle.api.TruffleSafepoint$Interrupter
 outer com.oracle.truffle.api.TruffleSafepoint
fld public final static com.oracle.truffle.api.TruffleSafepoint$Interrupter THREAD_INTERRUPT
meth public abstract void interrupt(java.lang.Thread)
meth public abstract void resetInterrupted()

CLSS public abstract interface static com.oracle.truffle.api.TruffleSafepoint$Interruptible<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleSafepoint
 anno 0 java.lang.FunctionalInterface()
meth public abstract void apply({com.oracle.truffle.api.TruffleSafepoint$Interruptible%0}) throws java.lang.InterruptedException

CLSS public abstract interface static com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleSafepoint
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction%1} apply({com.oracle.truffle.api.TruffleSafepoint$InterruptibleFunction%0}) throws java.lang.InterruptedException

CLSS public final com.oracle.truffle.api.TruffleStackTrace
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public static com.oracle.truffle.api.TruffleStackTrace fillIn(java.lang.Throwable)
meth public static java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> getAsynchronousStackTrace(com.oracle.truffle.api.CallTarget,com.oracle.truffle.api.frame.Frame)
meth public static java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> getStackTrace(java.lang.Throwable)
supr java.lang.Exception
hfds EMPTY,frames,lazyFrames,materializedHostException
hcls LazyStackTrace,TracebackElement

CLSS public final com.oracle.truffle.api.TruffleStackTraceElement
meth public com.oracle.truffle.api.RootCallTarget getTarget()
meth public com.oracle.truffle.api.frame.Frame getFrame()
meth public com.oracle.truffle.api.nodes.Node getLocation()
meth public java.lang.Object getGuestObject()
meth public static com.oracle.truffle.api.TruffleStackTraceElement create(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.RootCallTarget,com.oracle.truffle.api.frame.Frame)
supr java.lang.Object
hfds frame,location,target

CLSS public final com.oracle.truffle.api.TruffleThreadBuilder
meth public com.oracle.truffle.api.TruffleThreadBuilder afterLeave(java.lang.Runnable)
meth public com.oracle.truffle.api.TruffleThreadBuilder beforeEnter(java.lang.Runnable)
meth public com.oracle.truffle.api.TruffleThreadBuilder context(com.oracle.truffle.api.TruffleContext)
meth public com.oracle.truffle.api.TruffleThreadBuilder stackSize(long)
meth public com.oracle.truffle.api.TruffleThreadBuilder threadGroup(java.lang.ThreadGroup)
meth public java.lang.Thread build()
supr java.lang.Object
hfds afterLeave,beforeEnter,polyglotLanguageContext,runnable,stackSize,threadGroup,truffleContext

CLSS public com.oracle.truffle.api.debug.Breakpoint
innr public abstract interface static ResolveListener
innr public final Builder
innr public final ExceptionBuilder
innr public final static !enum Kind
meth public boolean isDisposed()
meth public boolean isEnabled()
meth public boolean isModifiable()
meth public boolean isOneShot()
meth public boolean isResolved()
meth public com.oracle.truffle.api.debug.Breakpoint$Kind getKind()
meth public com.oracle.truffle.api.debug.SuspendAnchor getSuspendAnchor()
meth public int getHitCount()
meth public int getIgnoreCount()
meth public java.lang.String getCondition()
meth public java.lang.String getLocationDescription()
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.debug.Breakpoint$Builder newBuilder(com.oracle.truffle.api.source.Source)
meth public static com.oracle.truffle.api.debug.Breakpoint$Builder newBuilder(com.oracle.truffle.api.source.SourceSection)
meth public static com.oracle.truffle.api.debug.Breakpoint$Builder newBuilder(java.net.URI)
meth public static com.oracle.truffle.api.debug.Breakpoint$ExceptionBuilder newExceptionBuilder(boolean,boolean)
meth public void dispose()
meth public void setCondition(java.lang.String)
meth public void setEnabled(boolean)
meth public void setIgnoreCount(int)
supr java.lang.Object
hfds BUILDER_INSTANCE,breakpointBindingReady,condition,conditionExistsUnchanged,conditionUnchanged,debugger,disposed,enabled,exceptionFilter,execBindings,global,hitCount,ignoreCount,locationKey,locationsInExecutedSources,oneShot,resolveListener,roWrapper,rootInstanceRef,sessions,sessionsUnchanged,sourceBinding,suspendAnchor
hcls AbstractBreakpointNode,BreakpointAfterNode,BreakpointAfterNodeException,BreakpointBeforeNode,BreakpointConditionFailure,BreakpointNodeFactory,ConditionalBreakNode,GlobalBreakpoint,LocationsInExecutedSources,SessionList

CLSS public final com.oracle.truffle.api.debug.Breakpoint$Builder
 outer com.oracle.truffle.api.debug.Breakpoint
meth public !varargs com.oracle.truffle.api.debug.Breakpoint$Builder sourceElements(com.oracle.truffle.api.debug.SourceElement[])
meth public com.oracle.truffle.api.debug.Breakpoint build()
meth public com.oracle.truffle.api.debug.Breakpoint$Builder columnIs(int)
meth public com.oracle.truffle.api.debug.Breakpoint$Builder ignoreCount(int)
meth public com.oracle.truffle.api.debug.Breakpoint$Builder lineIs(int)
meth public com.oracle.truffle.api.debug.Breakpoint$Builder oneShot()
meth public com.oracle.truffle.api.debug.Breakpoint$Builder resolveListener(com.oracle.truffle.api.debug.Breakpoint$ResolveListener)
meth public com.oracle.truffle.api.debug.Breakpoint$Builder rootInstance(com.oracle.truffle.api.debug.DebugValue)
meth public com.oracle.truffle.api.debug.Breakpoint$Builder suspendAnchor(com.oracle.truffle.api.debug.SuspendAnchor)
supr java.lang.Object
hfds anchor,column,ignoreCount,key,line,oneShot,resolveListener,rootInstance,sourceElements,sourceSection

CLSS public final com.oracle.truffle.api.debug.Breakpoint$ExceptionBuilder
 outer com.oracle.truffle.api.debug.Breakpoint
meth public !varargs com.oracle.truffle.api.debug.Breakpoint$ExceptionBuilder sourceElements(com.oracle.truffle.api.debug.SourceElement[])
meth public com.oracle.truffle.api.debug.Breakpoint build()
meth public com.oracle.truffle.api.debug.Breakpoint$ExceptionBuilder suspensionFilter(com.oracle.truffle.api.debug.SuspensionFilter)
supr java.lang.Object
hfds caught,sourceElements,suspensionFilter,uncaught

CLSS public final static !enum com.oracle.truffle.api.debug.Breakpoint$Kind
 outer com.oracle.truffle.api.debug.Breakpoint
fld public final static com.oracle.truffle.api.debug.Breakpoint$Kind EXCEPTION
fld public final static com.oracle.truffle.api.debug.Breakpoint$Kind HALT_INSTRUCTION
fld public final static com.oracle.truffle.api.debug.Breakpoint$Kind SOURCE_LOCATION
meth public static com.oracle.truffle.api.debug.Breakpoint$Kind valueOf(java.lang.String)
meth public static com.oracle.truffle.api.debug.Breakpoint$Kind[] values()
supr java.lang.Enum<com.oracle.truffle.api.debug.Breakpoint$Kind>
hfds VALUES

CLSS public abstract interface static com.oracle.truffle.api.debug.Breakpoint$ResolveListener
 outer com.oracle.truffle.api.debug.Breakpoint
meth public abstract void breakpointResolved(com.oracle.truffle.api.debug.Breakpoint,com.oracle.truffle.api.source.SourceSection)

CLSS public final com.oracle.truffle.api.debug.DebugContext
meth public <%0 extends java.lang.Object> {%%0} runInContext(java.util.function.Supplier<{%%0}>)
meth public com.oracle.truffle.api.debug.DebugContext getParent()
meth public com.oracle.truffle.api.debug.DebugValue evaluate(java.lang.String,java.lang.String)
supr java.lang.Object
hfds context,executionLifecycle

CLSS public abstract interface com.oracle.truffle.api.debug.DebugContextsListener
meth public abstract void contextClosed(com.oracle.truffle.api.debug.DebugContext)
meth public abstract void contextCreated(com.oracle.truffle.api.debug.DebugContext)
meth public abstract void languageContextCreated(com.oracle.truffle.api.debug.DebugContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void languageContextDisposed(com.oracle.truffle.api.debug.DebugContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void languageContextFinalized(com.oracle.truffle.api.debug.DebugContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void languageContextInitialized(com.oracle.truffle.api.debug.DebugContext,com.oracle.truffle.api.nodes.LanguageInfo)

CLSS public final com.oracle.truffle.api.debug.DebugException
innr public final static CatchLocation
meth public boolean isInternalError()
meth public com.oracle.truffle.api.debug.DebugException$CatchLocation getCatchLocation()
meth public com.oracle.truffle.api.debug.DebugValue getExceptionObject()
meth public com.oracle.truffle.api.source.SourceSection getThrowLocation()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getRawException(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public java.util.List<com.oracle.truffle.api.debug.DebugStackTraceElement> getDebugStackTrace()
meth public java.util.List<java.util.List<com.oracle.truffle.api.debug.DebugStackTraceElement>> getDebugAsynchronousStacks()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.RuntimeException
hfds CAUSE_CAPTION,catchLocation,debugAsyncStacks,debugStackTrace,exception,isCatchNodeComputed,javaLikeStackTrace,preferredLanguage,rawStackTrace,serialVersionUID,session,suspendedEvent,throwLocation

CLSS public final static com.oracle.truffle.api.debug.DebugException$CatchLocation
 outer com.oracle.truffle.api.debug.DebugException
meth public com.oracle.truffle.api.debug.DebugStackFrame getFrame()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
supr java.lang.Object
hfds depth,frame,frameInstance,section,session

CLSS public final com.oracle.truffle.api.debug.DebugScope
meth public boolean isFunctionScope()
meth public com.oracle.truffle.api.debug.DebugScope getParent()
meth public com.oracle.truffle.api.debug.DebugValue convertRawValue(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>,java.lang.Object)
meth public com.oracle.truffle.api.debug.DebugValue getDeclaredValue(java.lang.String)
meth public com.oracle.truffle.api.debug.DebugValue getReceiver()
meth public com.oracle.truffle.api.debug.DebugValue getRootInstance()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public java.lang.Iterable<com.oracle.truffle.api.debug.DebugValue> getArguments()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.3")
meth public java.lang.Iterable<com.oracle.truffle.api.debug.DebugValue> getDeclaredValues()
meth public java.lang.String getName()
supr java.lang.Object
hfds INTEROP,NODE,event,frame,language,node,parent,root,scope,session,variables
hcls SubtractedKeys,SubtractedVariables

CLSS public final com.oracle.truffle.api.debug.DebugStackFrame
meth public boolean equals(java.lang.Object)
meth public boolean isHost()
meth public boolean isInternal()
meth public com.oracle.truffle.api.debug.DebugScope getScope()
meth public com.oracle.truffle.api.debug.DebugValue eval(java.lang.String)
meth public com.oracle.truffle.api.frame.Frame getRawFrame(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>,com.oracle.truffle.api.frame.FrameInstance$FrameAccess)
meth public com.oracle.truffle.api.nodes.LanguageInfo getLanguage()
meth public com.oracle.truffle.api.nodes.Node getRawNode(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public int hashCode()
meth public java.lang.StackTraceElement getHostTraceElement()
meth public java.lang.String getName()
supr java.lang.Object
hfds currentFrame,depth,event,hostTraceElement,name,nameEx

CLSS public final com.oracle.truffle.api.debug.DebugStackTraceElement
meth public boolean isHost()
meth public boolean isInternal()
meth public com.oracle.truffle.api.debug.DebugScope getScope()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public java.lang.StackTraceElement getHostTraceElement()
meth public java.lang.String getName()
supr java.lang.Object
hfds hostTraceElement,session,stackTraceElement,traceElement

CLSS public abstract interface com.oracle.truffle.api.debug.DebugThreadsListener
meth public abstract void threadDisposed(com.oracle.truffle.api.debug.DebugContext,java.lang.Thread)
meth public abstract void threadInitialized(com.oracle.truffle.api.debug.DebugContext,java.lang.Thread)

CLSS public abstract com.oracle.truffle.api.debug.DebugValue
meth public !varargs final com.oracle.truffle.api.debug.DebugValue execute(com.oracle.truffle.api.debug.DebugValue[])
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.1")
meth public abstract boolean hasReadSideEffects()
meth public abstract boolean hasWriteSideEffects()
meth public abstract boolean isInternal()
meth public abstract boolean isReadable()
meth public abstract boolean isWritable()
meth public abstract com.oracle.truffle.api.debug.DebuggerSession getSession()
meth public abstract java.lang.String getName()
meth public abstract void set(com.oracle.truffle.api.debug.DebugValue)
meth public abstract void set(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.2")
meth public boolean asBoolean()
meth public boolean equals(java.lang.Object)
meth public boolean fitsInBigInteger()
meth public boolean fitsInByte()
meth public boolean fitsInDouble()
meth public boolean fitsInFloat()
meth public boolean fitsInInt()
meth public boolean fitsInLong()
meth public boolean fitsInShort()
meth public boolean hasHashEntries()
meth public boolean hasIterator()
meth public boolean hasIteratorNextElement()
meth public boolean isBoolean()
meth public boolean isDate()
meth public boolean isDuration()
meth public boolean isHashEntryExisting(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isHashEntryInsertable(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isHashEntryModifiable(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isHashEntryReadable(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isHashEntryRemovable(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isHashEntryWritable(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isInstant()
meth public boolean isIterator()
meth public boolean isMetaInstance(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isMetaObject()
meth public boolean isNumber()
meth public boolean isString()
meth public boolean isTime()
meth public boolean isTimeZone()
meth public boolean removeHashEntry(com.oracle.truffle.api.debug.DebugValue)
meth public byte asByte()
meth public com.oracle.truffle.api.debug.DebugScope getScope()
meth public com.oracle.truffle.api.debug.DebugValue getHashEntriesIterator()
meth public com.oracle.truffle.api.debug.DebugValue getHashKeysIterator()
meth public com.oracle.truffle.api.debug.DebugValue getHashValue(com.oracle.truffle.api.debug.DebugValue)
meth public com.oracle.truffle.api.debug.DebugValue getHashValueOrDefault(com.oracle.truffle.api.debug.DebugValue,com.oracle.truffle.api.debug.DebugValue)
meth public com.oracle.truffle.api.debug.DebugValue getHashValuesIterator()
meth public com.oracle.truffle.api.debug.DebugValue getIterator()
meth public com.oracle.truffle.api.debug.DebugValue getIteratorNextElement()
meth public double asDouble()
meth public final boolean canExecute()
meth public final boolean isArray()
meth public final boolean isNull()
meth public final com.oracle.truffle.api.debug.DebugValue asInLanguage(com.oracle.truffle.api.nodes.LanguageInfo)
meth public final com.oracle.truffle.api.debug.DebugValue getMetaObject()
meth public final com.oracle.truffle.api.debug.DebugValue getProperty(java.lang.String)
meth public final com.oracle.truffle.api.nodes.LanguageInfo getOriginalLanguage()
meth public final com.oracle.truffle.api.source.SourceSection getSourceLocation()
meth public final java.lang.String asString()
meth public final java.lang.String toDisplayString()
meth public final java.lang.String toDisplayString(boolean)
meth public final java.util.Collection<com.oracle.truffle.api.debug.DebugValue> getProperties()
meth public final java.util.List<com.oracle.truffle.api.debug.Breakpoint> getRootInstanceBreakpoints()
meth public float asFloat()
meth public int asInt()
meth public int hashCode()
meth public java.lang.Object getRawValue(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public java.lang.String getMetaQualifiedName()
meth public java.lang.String getMetaSimpleName()
meth public java.lang.String toString()
meth public java.math.BigInteger asBigInteger()
meth public java.time.Duration asDuration()
meth public java.time.Instant asInstant()
meth public java.time.LocalDate asDate()
meth public java.time.LocalTime asTime()
meth public java.time.ZoneId asTimeZone()
meth public java.util.List<com.oracle.truffle.api.debug.DebugValue> getArray()
meth public long asLong()
meth public long getHashSize()
meth public short asShort()
meth public void putHashEntry(com.oracle.truffle.api.debug.DebugValue,com.oracle.truffle.api.debug.DebugValue)
supr java.lang.Object
hfds INTEROP,preferredLanguage
hcls AbstractDebugCachedValue,AbstractDebugValue,ArrayElementValue,HashEntriesIteratorValue,HashEntryArrayValue,HashEntryValue,HeapValue,ObjectMemberValue

CLSS public final com.oracle.truffle.api.debug.Debugger
meth public !varargs com.oracle.truffle.api.debug.DebuggerSession startSession(com.oracle.truffle.api.debug.SuspendedCallback,com.oracle.truffle.api.debug.SourceElement[])
meth public com.oracle.truffle.api.debug.Breakpoint install(com.oracle.truffle.api.debug.Breakpoint)
meth public com.oracle.truffle.api.debug.DebuggerSession startSession(com.oracle.truffle.api.debug.SuspendedCallback)
meth public int getSessionCount()
meth public java.util.List<com.oracle.truffle.api.debug.Breakpoint> getBreakpoints()
meth public static com.oracle.truffle.api.debug.Debugger find(com.oracle.truffle.api.TruffleLanguage$Env)
meth public static com.oracle.truffle.api.debug.Debugger find(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
meth public static com.oracle.truffle.api.debug.Debugger find(org.graalvm.polyglot.Engine)
meth public void addBreakpointAddedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
meth public void addBreakpointRemovedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
meth public void disableStepping()
meth public void removeBreakpointAddedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
meth public void removeBreakpointRemovedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
meth public void restoreStepping()
supr java.lang.Object
hfds ACCESSOR,TRACE,alwaysHaltBreakpoint,breakpointAddedListeners,breakpointRemovedListeners,breakpoints,disabledSteppingCount,env,propSupport,sessions
hcls AccessorDebug

CLSS public final com.oracle.truffle.api.debug.DebuggerSession
intf java.io.Closeable
meth public boolean isBreakpointsActive()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.0")
meth public boolean isBreakpointsActive(com.oracle.truffle.api.debug.Breakpoint$Kind)
meth public boolean suspendHere(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.debug.Breakpoint install(com.oracle.truffle.api.debug.Breakpoint)
meth public com.oracle.truffle.api.debug.DebugScope getTopScope(java.lang.String)
meth public com.oracle.truffle.api.debug.DebugValue createPrimitiveValue(java.lang.Object,com.oracle.truffle.api.nodes.LanguageInfo)
meth public com.oracle.truffle.api.debug.Debugger getDebugger()
meth public com.oracle.truffle.api.source.Source resolveSource(com.oracle.truffle.api.source.Source)
meth public java.lang.String toString()
meth public java.util.List<com.oracle.truffle.api.debug.Breakpoint> getBreakpoints()
meth public java.util.Map<java.lang.String,? extends com.oracle.truffle.api.debug.DebugValue> getExportedSymbols()
meth public void close()
meth public void resume(java.lang.Thread)
meth public void resumeAll()
meth public void setAsynchronousStackDepth(int)
meth public void setBreakpointsActive(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.0")
meth public void setBreakpointsActive(com.oracle.truffle.api.debug.Breakpoint$Kind,boolean)
meth public void setContextsListener(com.oracle.truffle.api.debug.DebugContextsListener,boolean)
meth public void setShowHostStackFrames(boolean)
meth public void setSourcePath(java.lang.Iterable<java.net.URI>)
meth public void setSteppingFilter(com.oracle.truffle.api.debug.SuspensionFilter)
meth public void setThreadsListener(com.oracle.truffle.api.debug.DebugThreadsListener,boolean)
meth public void suspend(java.lang.Thread)
meth public void suspendAll()
meth public void suspendNextExecution()
supr java.lang.Object
hfds ANCHOR_SET_AFTER,ANCHOR_SET_ALL,ANCHOR_SET_BEFORE,SESSIONS,allBindings,alwaysHaltBreakpointsActive,breakpoints,callback,closed,currentSuspendedEventMap,debugger,exceptionBreakpointsActive,executionLifecycle,hasExpressionElement,hasRootElement,ignoreLanguageContextInitialization,inEvalInContext,includeInternal,locationBreakpointsActive,sessionId,showHostStackFrames,sourceElements,sourceFilter,sources,stepping,steppingEnabledSlots,strategyMap,suspendAll,suspendNext,suspensionFilterUnchanged,syntaxElementsBinding,threadSuspensions
hcls Caller,RootSteppingDepthNode,StableBoolean,SteppingNode,SuspendContextAndFrame,ThreadSuspension

CLSS public final com.oracle.truffle.api.debug.DebuggerTags
innr public final AlwaysHalt
supr java.lang.Object

CLSS public final com.oracle.truffle.api.debug.DebuggerTags$AlwaysHalt
 outer com.oracle.truffle.api.debug.DebuggerTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final !enum com.oracle.truffle.api.debug.SourceElement
fld public final static com.oracle.truffle.api.debug.SourceElement EXPRESSION
fld public final static com.oracle.truffle.api.debug.SourceElement ROOT
fld public final static com.oracle.truffle.api.debug.SourceElement STATEMENT
meth public static com.oracle.truffle.api.debug.SourceElement valueOf(java.lang.String)
meth public static com.oracle.truffle.api.debug.SourceElement[] values()
supr java.lang.Enum<com.oracle.truffle.api.debug.SourceElement>
hfds tag

CLSS public final com.oracle.truffle.api.debug.StepConfig
innr public final Builder
meth public static com.oracle.truffle.api.debug.StepConfig$Builder newBuilder()
supr java.lang.Object
hfds EMPTY,allElements,defaultAnchors,preferredAnchors,sourceElements,stepCount

CLSS public final com.oracle.truffle.api.debug.StepConfig$Builder
 outer com.oracle.truffle.api.debug.StepConfig
meth public !varargs com.oracle.truffle.api.debug.StepConfig$Builder sourceElements(com.oracle.truffle.api.debug.SourceElement[])
meth public !varargs com.oracle.truffle.api.debug.StepConfig$Builder suspendAnchors(com.oracle.truffle.api.debug.SourceElement,com.oracle.truffle.api.debug.SuspendAnchor[])
meth public com.oracle.truffle.api.debug.StepConfig build()
meth public com.oracle.truffle.api.debug.StepConfig$Builder count(int)
supr java.lang.Object
hfds preferredAnchors,stepCount,stepElements

CLSS public final !enum com.oracle.truffle.api.debug.SuspendAnchor
fld public final static com.oracle.truffle.api.debug.SuspendAnchor AFTER
fld public final static com.oracle.truffle.api.debug.SuspendAnchor BEFORE
meth public static com.oracle.truffle.api.debug.SuspendAnchor valueOf(java.lang.String)
meth public static com.oracle.truffle.api.debug.SuspendAnchor[] values()
supr java.lang.Enum<com.oracle.truffle.api.debug.SuspendAnchor>

CLSS public abstract interface com.oracle.truffle.api.debug.SuspendedCallback
meth public abstract void onSuspend(com.oracle.truffle.api.debug.SuspendedEvent)

CLSS public final com.oracle.truffle.api.debug.SuspendedEvent
meth public boolean hasSourceElement(com.oracle.truffle.api.debug.SourceElement)
meth public boolean isLanguageContextInitialized()
meth public com.oracle.truffle.api.debug.DebugException getException()
meth public com.oracle.truffle.api.debug.DebugStackFrame getTopStackFrame()
meth public com.oracle.truffle.api.debug.DebugValue getReturnValue()
meth public com.oracle.truffle.api.debug.DebugValue[] getInputValues()
meth public com.oracle.truffle.api.debug.DebuggerSession getSession()
meth public com.oracle.truffle.api.debug.SuspendAnchor getSuspendAnchor()
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepInto(com.oracle.truffle.api.debug.StepConfig)
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepInto(int)
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepOut(com.oracle.truffle.api.debug.StepConfig)
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepOut(int)
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepOver(com.oracle.truffle.api.debug.StepConfig)
meth public com.oracle.truffle.api.debug.SuspendedEvent prepareStepOver(int)
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public java.lang.Iterable<com.oracle.truffle.api.debug.DebugStackFrame> getStackFrames()
meth public java.lang.String toString()
meth public java.lang.Throwable getBreakpointConditionException(com.oracle.truffle.api.debug.Breakpoint)
meth public java.util.List<com.oracle.truffle.api.debug.Breakpoint> getBreakpoints()
meth public java.util.List<java.util.List<com.oracle.truffle.api.debug.DebugStackTraceElement>> getAsynchronousStacks()
meth public void prepareContinue()
meth public void prepareKill()
meth public void prepareUnwindFrame(com.oracle.truffle.api.debug.DebugStackFrame)
meth public void prepareUnwindFrame(com.oracle.truffle.api.debug.DebugStackFrame,com.oracle.truffle.api.debug.DebugValue)
meth public void setReturnValue(com.oracle.truffle.api.debug.DebugValue)
supr java.lang.Object
hfds HOST_INTEROP_NODE_NAME,breakpoints,cachedAsyncFrames,cachedFrames,conditionFailures,context,disposed,exception,inputValuesProvider,insertableNode,materializedFrame,nextStrategy,returnValue,session,sourceSection,suspendAnchor,thread
hcls DebugAsyncStackFrameLists,DebugStackFrameIterable

CLSS public final com.oracle.truffle.api.debug.SuspensionFilter
innr public final Builder
meth public boolean isIgnoreLanguageContextInitialization()
meth public static com.oracle.truffle.api.debug.SuspensionFilter$Builder newBuilder()
supr java.lang.Object
hfds ignoreLanguageContextInitialization,includeInternal,sourcePredicate

CLSS public final com.oracle.truffle.api.debug.SuspensionFilter$Builder
 outer com.oracle.truffle.api.debug.SuspensionFilter
meth public com.oracle.truffle.api.debug.SuspensionFilter build()
meth public com.oracle.truffle.api.debug.SuspensionFilter$Builder ignoreLanguageContextInitialization(boolean)
meth public com.oracle.truffle.api.debug.SuspensionFilter$Builder includeInternal(boolean)
meth public com.oracle.truffle.api.debug.SuspensionFilter$Builder sourceIs(java.util.function.Predicate<com.oracle.truffle.api.source.Source>)
supr java.lang.Object
hfds ignoreLanguageContextInitialization,includeInternal,sourcePredicate

CLSS public final com.oracle.truffle.api.dsl.AOTSupport
meth public static void prepareForAOT(com.oracle.truffle.api.nodes.RootNode)
supr java.lang.Object

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Bind
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Cached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
innr public abstract interface static !annotation Exclusive
innr public abstract interface static !annotation Shared
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean adopt()
meth public abstract !hasdefault boolean allowUncached()
meth public abstract !hasdefault boolean inline()
meth public abstract !hasdefault boolean neverDefault()
meth public abstract !hasdefault boolean weak()
meth public abstract !hasdefault int dimensions()
meth public abstract !hasdefault java.lang.String inlineMethod()
meth public abstract !hasdefault java.lang.String uncached()
meth public abstract !hasdefault java.lang.String value()
meth public abstract !hasdefault java.lang.String[] parameters()

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.Cached$Exclusive
 outer com.oracle.truffle.api.dsl.Cached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.Cached$Shared
 outer com.oracle.truffle.api.dsl.Cached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.CreateCast
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract com.oracle.truffle.api.dsl.DSLSupport
innr public abstract interface static SpecializationDataNode
meth public static <%0 extends com.oracle.truffle.api.nodes.NodeInterface> {%%0} maybeInsert(com.oracle.truffle.api.nodes.Node,{%%0})
meth public static <%0 extends com.oracle.truffle.api.nodes.NodeInterface> {%%0}[] maybeInsert(com.oracle.truffle.api.nodes.Node,{%%0}[])
meth public static <%0 extends java.lang.Enum<?>> {%%0}[] lookupEnumConstants(java.lang.Class<{%%0}>)
meth public static boolean assertIdempotence(boolean)
supr java.lang.Object
hfds ENUM_CONSTANTS

CLSS public abstract interface static com.oracle.truffle.api.dsl.DSLSupport$SpecializationDataNode
 outer com.oracle.truffle.api.dsl.DSLSupport

CLSS public abstract interface com.oracle.truffle.api.dsl.ExecuteTracingSupport
meth public abstract boolean isTracingEnabled()
meth public void traceOnEnter(java.lang.Object[])
meth public void traceOnException(java.lang.Throwable)
meth public void traceOnReturn(java.lang.Object)

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Executed
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] with()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Fallback
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateAOT
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Exclude
innr public abstract interface static Provider
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.GenerateAOT$Exclude
 outer com.oracle.truffle.api.dsl.GenerateAOT
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static com.oracle.truffle.api.dsl.GenerateAOT$Provider
 outer com.oracle.truffle.api.dsl.GenerateAOT
meth public void prepareForAOT(com.oracle.truffle.api.TruffleLanguage<?>,com.oracle.truffle.api.nodes.RootNode)
meth public void prepareForAOT(com.oracle.truffle.api.TruffleLanguage<?>,com.oracle.truffle.api.nodes.RootNode,com.oracle.truffle.api.nodes.Node)

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateCached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysInlineCached()
meth public abstract !hasdefault boolean inherit()
meth public abstract !hasdefault boolean value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateInline
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean inherit()
meth public abstract !hasdefault boolean inlineByDefault()
meth public abstract !hasdefault boolean value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateNodeFactory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean inherit()
meth public abstract !hasdefault boolean value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GeneratePackagePrivate
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateUncached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean inherit()
meth public abstract !hasdefault boolean value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GeneratedBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String methodName()
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Idempotent
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.ImplicitCast
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.ImportStatic
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] value()

CLSS public final com.oracle.truffle.api.dsl.InlineSupport
innr public abstract interface static !annotation RequiredField
innr public abstract interface static !annotation RequiredFields
innr public abstract interface static !annotation UnsafeAccessedField
innr public abstract static InlinableField
innr public final static BooleanField
innr public final static ByteField
innr public final static CharField
innr public final static DoubleField
innr public final static FloatField
innr public final static InlineTarget
innr public final static IntField
innr public final static LongField
innr public final static ReferenceField
innr public final static ShortField
innr public final static StateField
meth public !varargs static boolean validate(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.dsl.InlineSupport$InlinableField,com.oracle.truffle.api.dsl.InlineSupport$InlinableField,com.oracle.truffle.api.dsl.InlineSupport$InlinableField[])
meth public static boolean validate(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.dsl.InlineSupport$InlinableField)
meth public static boolean validate(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.dsl.InlineSupport$InlinableField,com.oracle.truffle.api.dsl.InlineSupport$InlinableField)
supr java.lang.Object
hfds PARENT
hcls UnsafeField,VarHandleField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$BooleanField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public boolean get(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.dsl.InlineSupport$BooleanField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.dsl.InlineSupport$BooleanField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,boolean)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$ByteField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public byte get(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.dsl.InlineSupport$ByteField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.dsl.InlineSupport$ByteField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,byte)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$CharField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public char get(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.dsl.InlineSupport$CharField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.dsl.InlineSupport$CharField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,char)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$DoubleField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$DoubleField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public double get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$DoubleField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,double)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$FloatField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$FloatField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public float get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$FloatField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,float)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public abstract static com.oracle.truffle.api.dsl.InlineSupport$InlinableField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public final boolean validate(com.oracle.truffle.api.nodes.Node)
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$InlineTarget
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public !varargs static com.oracle.truffle.api.dsl.InlineSupport$InlineTarget create(java.lang.Class<?>,com.oracle.truffle.api.dsl.InlineSupport$InlinableField[])
meth public <%0 extends com.oracle.truffle.api.dsl.InlineSupport$InlinableField> {%%0} getPrimitive(int,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.dsl.InlineSupport$ReferenceField<{%%0}> getReference(int,java.lang.Class<?>)
meth public com.oracle.truffle.api.dsl.InlineSupport$StateField getState(int,int)
meth public java.lang.Class<?> getTargetClass()
supr java.lang.Object
hfds targetClass,updaters

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$IntField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$IntField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public int get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$IntField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,int)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$LongField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$LongField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public long get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$LongField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,long)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$ReferenceField<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public boolean compareAndSet(com.oracle.truffle.api.nodes.Node,{com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0},{com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0})
meth public com.oracle.truffle.api.dsl.InlineSupport$ReferenceField<{com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0}> createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static <%0 extends java.lang.Object> com.oracle.truffle.api.dsl.InlineSupport$ReferenceField<{%%0}> create(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.Class<{%%0}>)
meth public void set(com.oracle.truffle.api.nodes.Node,{com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0})
meth public {com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0} get(com.oracle.truffle.api.nodes.Node)
meth public {com.oracle.truffle.api.dsl.InlineSupport$ReferenceField%0} getVolatile(com.oracle.truffle.api.nodes.Node)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField
hfds valueClass

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.InlineSupport$RequiredField
 outer com.oracle.truffle.api.dsl.InlineSupport
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.dsl.InlineSupport$RequiredFields)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int bits()
meth public abstract !hasdefault int dimensions()
meth public abstract !hasdefault java.lang.Class<?> type()
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.dsl.InlineSupport$InlinableField> value()

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.InlineSupport$RequiredFields
 outer com.oracle.truffle.api.dsl.InlineSupport
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract com.oracle.truffle.api.dsl.InlineSupport$RequiredField[] value()

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$ShortField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$ShortField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public short get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$ShortField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,short)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField

CLSS public final static com.oracle.truffle.api.dsl.InlineSupport$StateField
 outer com.oracle.truffle.api.dsl.InlineSupport
meth public com.oracle.truffle.api.dsl.InlineSupport$StateField createParentAccessor(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public com.oracle.truffle.api.dsl.InlineSupport$StateField subUpdater(int,int)
meth public int get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.InlineSupport$StateField create(java.lang.invoke.MethodHandles$Lookup,java.lang.String)
meth public void set(com.oracle.truffle.api.nodes.Node,int)
supr com.oracle.truffle.api.dsl.InlineSupport$InlinableField
hfds bitLength,bitMask,bitOffset

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.InlineSupport$UnsafeAccessedField
 outer com.oracle.truffle.api.dsl.InlineSupport
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Introspectable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public final com.oracle.truffle.api.dsl.Introspection
innr public abstract interface static Provider
innr public final static SpecializationInfo
meth public static boolean isIntrospectable(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.Introspection$SpecializationInfo getSpecialization(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public static com.oracle.truffle.api.dsl.Introspection$SpecializationInfo getSpecialization(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public static java.util.List<com.oracle.truffle.api.dsl.Introspection$SpecializationInfo> getSpecializations(com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<com.oracle.truffle.api.dsl.Introspection$SpecializationInfo> getSpecializations(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
supr java.lang.Object
hfds EMPTY_CACHED,NO_CACHED,data

CLSS public abstract interface static com.oracle.truffle.api.dsl.Introspection$Provider
 outer com.oracle.truffle.api.dsl.Introspection
meth public !varargs static com.oracle.truffle.api.dsl.Introspection create(java.lang.Object[])
meth public com.oracle.truffle.api.dsl.Introspection getIntrospectionData()
meth public com.oracle.truffle.api.dsl.Introspection getIntrospectionData(com.oracle.truffle.api.nodes.Node)

CLSS public final static com.oracle.truffle.api.dsl.Introspection$SpecializationInfo
 outer com.oracle.truffle.api.dsl.Introspection
meth public boolean isActive()
meth public boolean isExcluded()
meth public int getInstances()
meth public java.lang.String getMethodName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Object> getCachedData(int)
supr java.lang.Object
hfds cachedData,methodName,state

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NeverDefault
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, FIELD, PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NodeChild
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.dsl.NodeChildren)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean allowUncached()
meth public abstract !hasdefault boolean implicit()
meth public abstract !hasdefault java.lang.Class<?> type()
meth public abstract !hasdefault java.lang.String implicitCreate()
meth public abstract !hasdefault java.lang.String uncached()
meth public abstract !hasdefault java.lang.String value()
meth public abstract !hasdefault java.lang.String[] executeWith()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NodeChildren
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault com.oracle.truffle.api.dsl.NodeChild[] value()

CLSS public abstract interface com.oracle.truffle.api.dsl.NodeFactory<%0 extends java.lang.Object>
meth public abstract !varargs {com.oracle.truffle.api.dsl.NodeFactory%0} createNode(java.lang.Object[])
meth public abstract java.lang.Class<{com.oracle.truffle.api.dsl.NodeFactory%0}> getNodeClass()
meth public abstract java.util.List<java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>> getExecutionSignature()
meth public abstract java.util.List<java.util.List<java.lang.Class<?>>> getNodeSignatures()
meth public {com.oracle.truffle.api.dsl.NodeFactory%0} getUncachedInstance()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NodeField
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.dsl.NodeFields)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> type()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NodeFields
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault com.oracle.truffle.api.dsl.NodeField[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NonIdempotent
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.ReportPolymorphism
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Exclude
innr public abstract interface static !annotation Megamorphic
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.ReportPolymorphism$Exclude
 outer com.oracle.truffle.api.dsl.ReportPolymorphism
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.ReportPolymorphism$Megamorphic
 outer com.oracle.truffle.api.dsl.ReportPolymorphism
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Specialization
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int unroll()
meth public abstract !hasdefault java.lang.Class<? extends java.lang.Throwable>[] rewriteOn()
meth public abstract !hasdefault java.lang.String insertBefore()
meth public abstract !hasdefault java.lang.String limit()
meth public abstract !hasdefault java.lang.String[] assumptions()
meth public abstract !hasdefault java.lang.String[] guards()
meth public abstract !hasdefault java.lang.String[] replaces()

CLSS public final com.oracle.truffle.api.dsl.SpecializationStatistics
innr public abstract interface static !annotation AlwaysEnabled
innr public abstract static NodeStatistics
meth public boolean hasData()
meth public com.oracle.truffle.api.dsl.SpecializationStatistics enter()
meth public static com.oracle.truffle.api.dsl.SpecializationStatistics create()
meth public void leave(com.oracle.truffle.api.dsl.SpecializationStatistics)
meth public void printHistogram(java.io.PrintStream)
meth public void printHistogram(java.io.PrintWriter)
supr java.lang.Object
hfds STATISTICS,classStatistics,uncachedStatistics
hcls DisabledNodeStatistics,EnabledNodeStatistics,IntStatistics,NodeClassHistogram,NodeClassStatistics,TypeCombination,UncachedNodeStatistics

CLSS public abstract interface static !annotation com.oracle.truffle.api.dsl.SpecializationStatistics$AlwaysEnabled
 outer com.oracle.truffle.api.dsl.SpecializationStatistics
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract static com.oracle.truffle.api.dsl.SpecializationStatistics$NodeStatistics
 outer com.oracle.truffle.api.dsl.SpecializationStatistics
meth public abstract !varargs void acceptExecute(int,java.lang.Class<?>[])
meth public abstract java.lang.Class<?> resolveValueClass(java.lang.Object)
meth public abstract void acceptExecute(int,java.lang.Class<?>)
meth public abstract void acceptExecute(int,java.lang.Class<?>,java.lang.Class<?>)
meth public static com.oracle.truffle.api.dsl.SpecializationStatistics$NodeStatistics create(com.oracle.truffle.api.nodes.Node,java.lang.String[])
supr java.lang.Object

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.SuppressPackageWarnings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.TypeCast
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.TypeCheck
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.TypeSystem
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?>[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.TypeSystemReference
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public final com.oracle.truffle.api.dsl.UnsupportedSpecializationException
cons public !varargs init(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node[],java.lang.Object[])
meth public com.oracle.truffle.api.nodes.Node getNode()
meth public com.oracle.truffle.api.nodes.Node[] getSuppliedNodes()
meth public java.lang.Object[] getSuppliedValues()
meth public java.lang.String getMessage()
supr java.lang.RuntimeException
hfds node,serialVersionUID,suppliedNodes,suppliedValues

CLSS public abstract com.oracle.truffle.api.exception.AbstractTruffleException
cons protected init()
cons protected init(com.oracle.truffle.api.exception.AbstractTruffleException)
cons protected init(com.oracle.truffle.api.nodes.Node)
cons protected init(java.lang.String)
cons protected init(java.lang.String,com.oracle.truffle.api.nodes.Node)
cons protected init(java.lang.String,java.lang.Throwable,int,com.oracle.truffle.api.nodes.Node)
fld public final static int UNLIMITED_STACK_TRACE = -1
intf com.oracle.truffle.api.interop.TruffleObject
meth public final com.oracle.truffle.api.nodes.Node getLocation()
meth public final int getStackTraceElementLimit()
meth public final java.lang.Throwable fillInStackTrace()
meth public final java.lang.Throwable getCause()
supr java.lang.RuntimeException
hfds cause,lazyStackTrace,location,stackTraceElementLimit

CLSS public abstract interface com.oracle.truffle.api.frame.Frame
meth public abstract com.oracle.truffle.api.frame.FrameDescriptor getFrameDescriptor()
meth public abstract com.oracle.truffle.api.frame.MaterializedFrame materialize()
meth public abstract java.lang.Object[] getArguments()
meth public boolean getBoolean(int)
meth public boolean getBooleanStatic(int)
meth public boolean isBoolean(int)
meth public boolean isByte(int)
meth public boolean isDouble(int)
meth public boolean isFloat(int)
meth public boolean isInt(int)
meth public boolean isLong(int)
meth public boolean isObject(int)
meth public boolean isStatic(int)
meth public byte getByte(int)
meth public byte getByteStatic(int)
meth public byte getTag(int)
meth public double getDouble(int)
meth public double getDoubleStatic(int)
meth public float getFloat(int)
meth public float getFloatStatic(int)
meth public int getInt(int)
meth public int getIntStatic(int)
meth public java.lang.Object getAuxiliarySlot(int)
meth public java.lang.Object getObject(int)
meth public java.lang.Object getObjectStatic(int)
meth public java.lang.Object getValue(int)
meth public long getLong(int)
meth public long getLongStatic(int)
meth public void clear(int)
meth public void clearObjectStatic(int)
meth public void clearPrimitiveStatic(int)
meth public void clearStatic(int)
meth public void copy(int,int)
meth public void copyObjectStatic(int,int)
meth public void copyPrimitiveStatic(int,int)
meth public void copyStatic(int,int)
meth public void setAuxiliarySlot(int,java.lang.Object)
meth public void setBoolean(int,boolean)
meth public void setBooleanStatic(int,boolean)
meth public void setByte(int,byte)
meth public void setByteStatic(int,byte)
meth public void setDouble(int,double)
meth public void setDoubleStatic(int,double)
meth public void setFloat(int,float)
meth public void setFloatStatic(int,float)
meth public void setInt(int,int)
meth public void setIntStatic(int,int)
meth public void setLong(int,long)
meth public void setLongStatic(int,long)
meth public void setObject(int,java.lang.Object)
meth public void setObjectStatic(int,java.lang.Object)
meth public void swap(int,int)
meth public void swapObjectStatic(int,int)
meth public void swapPrimitiveStatic(int,int)
meth public void swapStatic(int,int)

CLSS public final com.oracle.truffle.api.frame.FrameDescriptor
cons public init()
cons public init(java.lang.Object)
innr public final static Builder
intf java.lang.Cloneable
meth public com.oracle.truffle.api.frame.FrameDescriptor copy()
meth public com.oracle.truffle.api.frame.FrameSlotKind getSlotKind(int)
meth public int findOrAddAuxiliarySlot(java.lang.Object)
meth public int getNumberOfAuxiliarySlots()
meth public int getNumberOfSlots()
meth public java.lang.Object getDefaultValue()
meth public java.lang.Object getInfo()
meth public java.lang.Object getSlotInfo(int)
meth public java.lang.Object getSlotName(int)
meth public java.lang.String toString()
meth public java.util.Map<java.lang.Object,java.lang.Integer> getAuxiliarySlots()
meth public static com.oracle.truffle.api.frame.FrameDescriptor$Builder newBuilder()
meth public static com.oracle.truffle.api.frame.FrameDescriptor$Builder newBuilder(int)
meth public void disableAuxiliarySlot(java.lang.Object)
meth public void setSlotKind(int,com.oracle.truffle.api.frame.FrameSlotKind)
supr java.lang.Object
hfds ALL_STATIC_MODE,EMPTY_BYTE_ARRAY,MIXED_STATIC_MODE,NEVER_PART_OF_COMPILATION_MESSAGE,NO_STATIC_MODE,activeAuxiliarySlotCount,auxiliarySlotCount,auxiliarySlotMap,defaultValue,descriptorInfo,disabledAuxiliarySlots,indexedSlotInfos,indexedSlotNames,indexedSlotTags,materializeCalled,staticMode

CLSS public final static com.oracle.truffle.api.frame.FrameDescriptor$Builder
 outer com.oracle.truffle.api.frame.FrameDescriptor
meth public com.oracle.truffle.api.frame.FrameDescriptor build()
meth public com.oracle.truffle.api.frame.FrameDescriptor$Builder defaultValue(java.lang.Object)
meth public com.oracle.truffle.api.frame.FrameDescriptor$Builder info(java.lang.Object)
meth public int addSlot(com.oracle.truffle.api.frame.FrameSlotKind,java.lang.Object,java.lang.Object)
meth public int addSlots(int,com.oracle.truffle.api.frame.FrameSlotKind)
supr java.lang.Object
hfds DEFAULT_CAPACITY,defaultValue,descriptorInfo,infos,names,size,staticMode,tags

CLSS public abstract interface com.oracle.truffle.api.frame.FrameInstance
innr public final static !enum FrameAccess
meth public abstract boolean isVirtualFrame()
meth public abstract com.oracle.truffle.api.CallTarget getCallTarget()
meth public abstract com.oracle.truffle.api.frame.Frame getFrame(com.oracle.truffle.api.frame.FrameInstance$FrameAccess)
meth public abstract com.oracle.truffle.api.nodes.Node getCallNode()
meth public boolean isCompilationRoot()
meth public int getCompilationTier()

CLSS public final static !enum com.oracle.truffle.api.frame.FrameInstance$FrameAccess
 outer com.oracle.truffle.api.frame.FrameInstance
fld public final static com.oracle.truffle.api.frame.FrameInstance$FrameAccess MATERIALIZE
fld public final static com.oracle.truffle.api.frame.FrameInstance$FrameAccess READ_ONLY
fld public final static com.oracle.truffle.api.frame.FrameInstance$FrameAccess READ_WRITE
meth public static com.oracle.truffle.api.frame.FrameInstance$FrameAccess valueOf(java.lang.String)
meth public static com.oracle.truffle.api.frame.FrameInstance$FrameAccess[] values()
supr java.lang.Enum<com.oracle.truffle.api.frame.FrameInstance$FrameAccess>

CLSS public abstract interface com.oracle.truffle.api.frame.FrameInstanceVisitor<%0 extends java.lang.Object>
meth public abstract {com.oracle.truffle.api.frame.FrameInstanceVisitor%0} visitFrame(com.oracle.truffle.api.frame.FrameInstance)

CLSS public final !enum com.oracle.truffle.api.frame.FrameSlotKind
fld public final byte tag
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Boolean
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Byte
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Double
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Float
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Illegal
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Int
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Long
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Object
fld public final static com.oracle.truffle.api.frame.FrameSlotKind Static
meth public static com.oracle.truffle.api.frame.FrameSlotKind fromTag(byte)
meth public static com.oracle.truffle.api.frame.FrameSlotKind valueOf(java.lang.String)
meth public static com.oracle.truffle.api.frame.FrameSlotKind[] values()
supr java.lang.Enum<com.oracle.truffle.api.frame.FrameSlotKind>
hfds VALUES

CLSS public final com.oracle.truffle.api.frame.FrameSlotTypeException
cons public init()
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.frame.MaterializedFrame
intf com.oracle.truffle.api.frame.VirtualFrame

CLSS public abstract interface com.oracle.truffle.api.frame.VirtualFrame
intf com.oracle.truffle.api.frame.Frame

CLSS public final com.oracle.truffle.api.instrumentation.AllocationEvent
meth public com.oracle.truffle.api.nodes.LanguageInfo getLanguage()
meth public java.lang.Object getValue()
meth public long getNewSize()
meth public long getOldSize()
supr java.lang.Object
hfds language,newSize,oldSize,value

CLSS public final com.oracle.truffle.api.instrumentation.AllocationEventFilter
fld public final static com.oracle.truffle.api.instrumentation.AllocationEventFilter ANY
innr public Builder
meth public static com.oracle.truffle.api.instrumentation.AllocationEventFilter$Builder newBuilder()
supr java.lang.Object
hfds languageSet

CLSS public com.oracle.truffle.api.instrumentation.AllocationEventFilter$Builder
 outer com.oracle.truffle.api.instrumentation.AllocationEventFilter
meth public !varargs com.oracle.truffle.api.instrumentation.AllocationEventFilter$Builder languages(com.oracle.truffle.api.nodes.LanguageInfo[])
meth public com.oracle.truffle.api.instrumentation.AllocationEventFilter build()
supr java.lang.Object
hfds langs

CLSS public abstract interface com.oracle.truffle.api.instrumentation.AllocationListener
meth public abstract void onEnter(com.oracle.truffle.api.instrumentation.AllocationEvent)
meth public abstract void onReturnValue(com.oracle.truffle.api.instrumentation.AllocationEvent)

CLSS public final com.oracle.truffle.api.instrumentation.AllocationReporter
fld public final static long SIZE_UNKNOWN = -9223372036854775808
meth public boolean isActive()
meth public void addActiveListener(java.util.function.Consumer<java.lang.Boolean>)
meth public void onEnter(java.lang.Object,long,long)
meth public void onReturnValue(java.lang.Object,long,long)
meth public void removeActiveListener(java.util.function.Consumer<java.lang.Boolean>)
supr java.lang.Object
hfds activeListeners,language,listeners,listenersNotChangedAssumption,valueCheck

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ContextsListener
meth public abstract void onContextClosed(com.oracle.truffle.api.TruffleContext)
meth public abstract void onContextCreated(com.oracle.truffle.api.TruffleContext)
meth public abstract void onLanguageContextCreated(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void onLanguageContextDisposed(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void onLanguageContextFinalized(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public abstract void onLanguageContextInitialized(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public void onContextResetLimits(com.oracle.truffle.api.TruffleContext)
meth public void onLanguageContextCreate(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public void onLanguageContextCreateFailed(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public void onLanguageContextInitialize(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)
meth public void onLanguageContextInitializeFailed(com.oracle.truffle.api.TruffleContext,com.oracle.truffle.api.nodes.LanguageInfo)

CLSS public com.oracle.truffle.api.instrumentation.EventBinding<%0 extends java.lang.Object>
meth public boolean isDisposed()
meth public final boolean isAttached()
meth public final boolean tryAttach()
meth public final void attach()
meth public void dispose()
meth public {com.oracle.truffle.api.instrumentation.EventBinding%0} getElement()
supr java.lang.Object
hfds attached,attachedSemaphore,disposed,disposing,element,instrumenter
hcls Allocation,Execution,LoadNearestSection,LoadSource,LoadedNotifier,NearestExecution,NearestSourceSection,Source,SourceExecuted,SourceLoaded,SourceSectionLoaded

CLSS public final com.oracle.truffle.api.instrumentation.EventContext
meth public boolean hasTag(java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>)
meth public boolean isLanguageContextInitialized()
meth public com.oracle.truffle.api.instrumentation.ExecutionEventNode lookupExecutionEventNode(com.oracle.truffle.api.instrumentation.EventBinding<? extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory>)
meth public com.oracle.truffle.api.nodes.Node getInstrumentedNode()
meth public com.oracle.truffle.api.source.SourceSection getInstrumentedSourceSection()
meth public java.lang.Object getNodeObject()
meth public java.lang.RuntimeException createError(java.lang.RuntimeException)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.3")
meth public java.lang.String toString()
meth public java.lang.ThreadDeath createUnwind(java.lang.Object)
meth public java.lang.ThreadDeath createUnwind(java.lang.Object,com.oracle.truffle.api.instrumentation.EventBinding<?>)
meth public java.util.Iterator<com.oracle.truffle.api.instrumentation.ExecutionEventNode> lookupExecutionEventNodes(java.util.Collection<com.oracle.truffle.api.instrumentation.EventBinding<? extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory>>)
supr java.lang.Object
hfds nodeObject,probeNode,sourceSection

CLSS public final com.oracle.truffle.api.instrumentation.ExecuteSourceEvent
meth public com.oracle.truffle.api.source.Source getSource()
supr java.lang.Object
hfds source

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ExecuteSourceListener
meth public abstract void onExecute(com.oracle.truffle.api.instrumentation.ExecuteSourceEvent)

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ExecutionEventListener
meth public abstract void onEnter(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame)
meth public abstract void onReturnExceptional(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame,java.lang.Throwable)
meth public abstract void onReturnValue(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
meth public java.lang.Object onUnwind(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
meth public void onInputValue(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame,com.oracle.truffle.api.instrumentation.EventContext,int,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.0")
meth public void onResume(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame)
meth public void onYield(com.oracle.truffle.api.instrumentation.EventContext,com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)

CLSS public abstract com.oracle.truffle.api.instrumentation.ExecutionEventNode
cons protected init()
meth protected final com.oracle.truffle.api.instrumentation.EventContext getInputContext(int)
meth protected final int getInputCount()
meth protected final java.lang.Object[] getSavedInputValues(com.oracle.truffle.api.frame.VirtualFrame)
meth protected final void saveInputValue(com.oracle.truffle.api.frame.VirtualFrame,int,java.lang.Object)
meth protected java.lang.Object onUnwind(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
meth protected void onDispose(com.oracle.truffle.api.frame.VirtualFrame)
meth protected void onEnter(com.oracle.truffle.api.frame.VirtualFrame)
meth protected void onInputValue(com.oracle.truffle.api.frame.VirtualFrame,com.oracle.truffle.api.instrumentation.EventContext,int,java.lang.Object)
meth protected void onResume(com.oracle.truffle.api.frame.VirtualFrame)
meth protected void onReturnExceptional(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Throwable)
meth protected void onReturnValue(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
meth protected void onYield(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory
meth public abstract com.oracle.truffle.api.instrumentation.ExecutionEventNode create(com.oracle.truffle.api.instrumentation.EventContext)

CLSS public abstract interface !annotation com.oracle.truffle.api.instrumentation.GenerateWrapper
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Ignore
innr public abstract interface static !annotation IncomingConverter
innr public abstract interface static !annotation OutgoingConverter
innr public abstract interface static YieldException
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?>[] yieldExceptions()
meth public abstract !hasdefault java.lang.String resumeMethodPrefix()

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.GenerateWrapper$Ignore
 outer com.oracle.truffle.api.instrumentation.GenerateWrapper
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.GenerateWrapper$IncomingConverter
 outer com.oracle.truffle.api.instrumentation.GenerateWrapper
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.GenerateWrapper$OutgoingConverter
 outer com.oracle.truffle.api.instrumentation.GenerateWrapper
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static com.oracle.truffle.api.instrumentation.GenerateWrapper$YieldException
 outer com.oracle.truffle.api.instrumentation.GenerateWrapper
meth public abstract java.lang.Object getYieldValue()

CLSS public abstract interface com.oracle.truffle.api.instrumentation.InstrumentableNode
innr public abstract interface static WrapperNode
intf com.oracle.truffle.api.nodes.NodeInterface
meth public abstract boolean isInstrumentable()
meth public abstract com.oracle.truffle.api.instrumentation.InstrumentableNode$WrapperNode createWrapper(com.oracle.truffle.api.instrumentation.ProbeNode)
meth public boolean hasTag(java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>)
meth public com.oracle.truffle.api.instrumentation.InstrumentableNode materializeInstrumentableNodes(java.util.Set<java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>>)
meth public com.oracle.truffle.api.nodes.Node findNearestNodeAt(int,int,java.util.Set<java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>>)
meth public com.oracle.truffle.api.nodes.Node findNearestNodeAt(int,java.util.Set<java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>>)
meth public java.lang.Object getNodeObject()
meth public static com.oracle.truffle.api.nodes.Node findInstrumentableParent(com.oracle.truffle.api.nodes.Node)

CLSS public abstract interface static com.oracle.truffle.api.instrumentation.InstrumentableNode$WrapperNode
 outer com.oracle.truffle.api.instrumentation.InstrumentableNode
intf com.oracle.truffle.api.nodes.NodeInterface
meth public abstract com.oracle.truffle.api.instrumentation.ProbeNode getProbeNode()
meth public abstract com.oracle.truffle.api.nodes.Node getDelegateNode()

CLSS public abstract com.oracle.truffle.api.instrumentation.Instrumenter
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.AllocationListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachAllocationListener(com.oracle.truffle.api.instrumentation.AllocationEventFilter,{%%0})
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ContextsListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachContextsListener({%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecuteSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecuteSourceListener(com.oracle.truffle.api.instrumentation.SourceFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecuteSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> createExecuteSourceBinding(com.oracle.truffle.api.instrumentation.SourceFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.0")
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventFactory(com.oracle.truffle.api.instrumentation.NearestSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventFactory(com.oracle.truffle.api.instrumentation.SourceSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceListener(com.oracle.truffle.api.instrumentation.SourceFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.0")
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> createLoadSourceBinding(com.oracle.truffle.api.instrumentation.SourceFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceSectionListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceSectionListener(com.oracle.truffle.api.instrumentation.NearestSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceSectionListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceSectionListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceSectionListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> createLoadSourceSectionBinding(com.oracle.truffle.api.instrumentation.NearestSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceSectionListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> createLoadSourceSectionBinding(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ThreadsListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachThreadsListener({%%0},boolean)
meth public abstract <%0 extends java.io.OutputStream> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachErrConsumer({%%0})
meth public abstract <%0 extends java.io.OutputStream> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachOutConsumer({%%0})
meth public abstract com.oracle.truffle.api.instrumentation.EventBinding<? extends com.oracle.truffle.api.instrumentation.ThreadsActivationListener> attachThreadsActivationListener(com.oracle.truffle.api.instrumentation.ThreadsActivationListener)
meth public abstract com.oracle.truffle.api.instrumentation.ExecutionEventNode lookupExecutionEventNode(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.instrumentation.EventBinding<?>)
meth public abstract java.util.Set<java.lang.Class<?>> queryTags(com.oracle.truffle.api.nodes.Node)
meth public abstract void visitLoadedSourceSections(com.oracle.truffle.api.instrumentation.SourceSectionFilter,com.oracle.truffle.api.instrumentation.LoadSourceSectionListener)
meth public final <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
meth public final <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventFactory(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
meth public final java.util.List<com.oracle.truffle.api.source.SourceSection> querySourceSections(com.oracle.truffle.api.instrumentation.SourceSectionFilter)
supr java.lang.Object

CLSS public final com.oracle.truffle.api.instrumentation.LoadSourceEvent
meth public com.oracle.truffle.api.source.Source getSource()
supr java.lang.Object
hfds source

CLSS public abstract interface com.oracle.truffle.api.instrumentation.LoadSourceListener
meth public abstract void onLoad(com.oracle.truffle.api.instrumentation.LoadSourceEvent)

CLSS public final com.oracle.truffle.api.instrumentation.LoadSourceSectionEvent
meth public com.oracle.truffle.api.nodes.Node getNode()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
supr java.lang.Object
hfds node,sourceSection

CLSS public abstract interface com.oracle.truffle.api.instrumentation.LoadSourceSectionListener
meth public abstract void onLoad(com.oracle.truffle.api.instrumentation.LoadSourceSectionEvent)

CLSS public final com.oracle.truffle.api.instrumentation.NearestSectionFilter
innr public final static Builder
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.instrumentation.NearestSectionFilter$Builder newBuilder(int,int)
supr java.lang.Object
hfds anchorStart,position,tagClasses,tags

CLSS public final static com.oracle.truffle.api.instrumentation.NearestSectionFilter$Builder
 outer com.oracle.truffle.api.instrumentation.NearestSectionFilter
meth public !varargs com.oracle.truffle.api.instrumentation.NearestSectionFilter$Builder tagIs(java.lang.Class<?>[])
meth public com.oracle.truffle.api.instrumentation.NearestSectionFilter build()
meth public com.oracle.truffle.api.instrumentation.NearestSectionFilter$Builder anchorStart(boolean)
supr java.lang.Object
hfds anchorStart,column,line,theTags

CLSS public final com.oracle.truffle.api.instrumentation.ProbeNode
fld public final static java.lang.Object UNWIND_ACTION_REENTER
meth public com.oracle.truffle.api.nodes.Node copy()
meth public com.oracle.truffle.api.nodes.NodeCost getCost()
meth public java.lang.Object onReturnExceptionalOrUnwind(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Throwable,boolean)
meth public void onEnter(com.oracle.truffle.api.frame.VirtualFrame)
meth public void onResume(com.oracle.truffle.api.frame.VirtualFrame)
meth public void onReturnValue(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
meth public void onYield(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
supr com.oracle.truffle.api.nodes.Node
hfds ASSERT_ENTER_RETURN_PARITY,SEEN_REENTER,SEEN_RETURN,SEEN_UNWIND,SEEN_UNWIND_NEXT,UNWIND_ACTION_IGNORED,chain,context,handler,retiredNodeReference,seen,version
hcls EventChainNode,EventFilterChainNode,EventProviderChainNode,EventProviderWithInputChainNode,InputChildContextLookup,InputChildIndexLookup,InputValueChainNode,InstrumentableChildVisitor,RetiredNodeReference

CLSS public abstract interface !annotation com.oracle.truffle.api.instrumentation.ProvidedTags
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] value()

CLSS public final com.oracle.truffle.api.instrumentation.SourceFilter
fld public final static com.oracle.truffle.api.instrumentation.SourceFilter ANY
innr public final Builder
meth public static com.oracle.truffle.api.instrumentation.SourceFilter$Builder newBuilder()
supr java.lang.Object
hfds expressions

CLSS public final com.oracle.truffle.api.instrumentation.SourceFilter$Builder
 outer com.oracle.truffle.api.instrumentation.SourceFilter
meth public !varargs com.oracle.truffle.api.instrumentation.SourceFilter$Builder languageIs(java.lang.String[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceFilter$Builder sourceIs(com.oracle.truffle.api.source.Source[])
meth public com.oracle.truffle.api.instrumentation.SourceFilter build()
meth public com.oracle.truffle.api.instrumentation.SourceFilter$Builder includeInternal(boolean)
meth public com.oracle.truffle.api.instrumentation.SourceFilter$Builder sourceIs(java.util.function.Predicate<com.oracle.truffle.api.source.Source>)
supr java.lang.Object
hfds expressions,includeInternal

CLSS public final com.oracle.truffle.api.instrumentation.SourceSectionFilter
fld public final static com.oracle.truffle.api.instrumentation.SourceSectionFilter ANY
innr public abstract interface static SourcePredicate
innr public final Builder
innr public final static IndexRange
meth public boolean includes(com.oracle.truffle.api.nodes.Node)
meth public boolean includes(com.oracle.truffle.api.nodes.RootNode,com.oracle.truffle.api.source.SourceSection,java.util.Set<java.lang.Class<?>>)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder newBuilder()
supr java.lang.Object
hfds TAGGED_NODE_CACHE,expressions
hcls EventFilterExpression,Not,TaggedNode

CLSS public final com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder
 outer com.oracle.truffle.api.instrumentation.SourceSectionFilter
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder columnEndsIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder columnIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder columnNotIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder columnStartsIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder indexIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder indexNotIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineEndsIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineNotIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineStartsIn(com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder mimeTypeIs(java.lang.String[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder rootSourceSectionEquals(com.oracle.truffle.api.source.SourceSection[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder sourceIs(com.oracle.truffle.api.source.Source[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder sourceSectionEquals(com.oracle.truffle.api.source.SourceSection[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder tagIs(java.lang.Class<?>[])
meth public !varargs com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder tagIsNot(java.lang.Class<?>[])
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter build()
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder and(com.oracle.truffle.api.instrumentation.SourceSectionFilter)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder columnIn(int,int)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder includeInternal(boolean)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder indexIn(int,int)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineIn(int,int)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder lineIs(int)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder rootNameIs(java.util.function.Predicate<java.lang.String>)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder sourceFilter(com.oracle.truffle.api.instrumentation.SourceFilter)
meth public com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder sourceIs(com.oracle.truffle.api.instrumentation.SourceSectionFilter$SourcePredicate)
supr java.lang.Object
hfds expressions,includeInternal

CLSS public final static com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange
 outer com.oracle.truffle.api.instrumentation.SourceSectionFilter
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange between(int,int)
meth public static com.oracle.truffle.api.instrumentation.SourceSectionFilter$IndexRange byLength(int,int)
supr java.lang.Object
hfds endIndex,startIndex

CLSS public abstract interface static com.oracle.truffle.api.instrumentation.SourceSectionFilter$SourcePredicate
 outer com.oracle.truffle.api.instrumentation.SourceSectionFilter
intf java.util.function.Predicate<com.oracle.truffle.api.source.Source>
meth public abstract boolean test(com.oracle.truffle.api.source.Source)

CLSS public final com.oracle.truffle.api.instrumentation.StandardTags
innr public final static CallTag
innr public final static ExpressionTag
innr public final static ReadVariableTag
innr public final static RootBodyTag
innr public final static RootTag
innr public final static StatementTag
innr public final static TryBlockTag
innr public final static WriteVariableTag
supr java.lang.Object
hfds ALL_TAGS

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$CallTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$ExpressionTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$ReadVariableTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
fld public final static java.lang.String NAME = "readVariableName"
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$RootBodyTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$RootTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$StatementTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$TryBlockTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
fld public final static java.lang.String CATCHES = "catches"
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public final static com.oracle.truffle.api.instrumentation.StandardTags$WriteVariableTag
 outer com.oracle.truffle.api.instrumentation.StandardTags
fld public final static java.lang.String NAME = "writeVariableName"
supr com.oracle.truffle.api.instrumentation.Tag

CLSS public abstract com.oracle.truffle.api.instrumentation.Tag
cons protected init()
innr public abstract interface static !annotation Identifier
meth public static java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag> findProvidedTag(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.String)
meth public static java.lang.String getIdentifier(java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>)
supr java.lang.Object

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.Tag$Identifier
 outer com.oracle.truffle.api.instrumentation.Tag
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ThreadsActivationListener
meth public abstract void onEnterThread(com.oracle.truffle.api.TruffleContext)
meth public abstract void onLeaveThread(com.oracle.truffle.api.TruffleContext)

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ThreadsListener
meth public abstract void onThreadDisposed(com.oracle.truffle.api.TruffleContext,java.lang.Thread)
meth public abstract void onThreadInitialized(com.oracle.truffle.api.TruffleContext,java.lang.Thread)

CLSS public abstract com.oracle.truffle.api.instrumentation.TruffleInstrument
cons protected init()
fld protected final com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalProvider locals
innr protected abstract interface static ContextLocalFactory
innr protected abstract interface static ContextThreadLocalFactory
innr protected final static ContextLocalProvider
innr public abstract interface static !annotation Registration
innr public abstract interface static Provider
innr public final static Env
meth protected abstract void onCreate(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextThreadLocalFactory<{%%0}>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected org.graalvm.options.OptionDescriptors getContextOptionDescriptors()
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected void onDispose(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
meth protected void onFinalize(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
supr java.lang.Object

CLSS protected abstract interface static com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory%0} create(com.oracle.truffle.api.TruffleContext)

CLSS protected final static com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalProvider
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory<{%%0}>)
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextThreadLocalFactory<{%%0}>)
supr java.lang.Object
hfds contextLocals,contextThreadLocals

CLSS protected abstract interface static com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextThreadLocalFactory<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextThreadLocalFactory%0} create(com.oracle.truffle.api.TruffleContext,java.lang.Thread)

CLSS public final static com.oracle.truffle.api.instrumentation.TruffleInstrument$Env
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
meth public !varargs com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.source.Source,java.lang.String[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.InstrumentInfo,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Class<{%%0}>)
meth public boolean isEngineRoot(com.oracle.truffle.api.nodes.RootNode)
meth public boolean isSameFrame(com.oracle.truffle.api.nodes.RootNode,com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.Frame)
meth public com.oracle.truffle.api.TruffleContext getEnteredContext()
meth public com.oracle.truffle.api.TruffleFile getInternalResource(java.lang.Class<? extends com.oracle.truffle.api.InternalResource>) throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getInternalResource(java.lang.String) throws java.io.IOException
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(com.oracle.truffle.api.TruffleContext,java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(com.oracle.truffle.api.TruffleContext,java.net.URI)
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.net.URI)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.Class<?>)
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String)
meth public com.oracle.truffle.api.instrumentation.Instrumenter getInstrumenter()
meth public com.oracle.truffle.api.nodes.ExecutableNode parseInline(com.oracle.truffle.api.source.Source,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.MaterializedFrame)
meth public com.oracle.truffle.api.nodes.LanguageInfo getLanguageInfo(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public java.io.InputStream in()
meth public java.io.OutputStream err()
meth public java.io.OutputStream out()
meth public java.lang.Object getLanguageView(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Object)
meth public java.lang.Object getPolyglotBindings()
meth public java.lang.Object getScope(com.oracle.truffle.api.nodes.LanguageInfo)
meth public java.lang.Thread createSystemThread(java.lang.Runnable)
meth public java.lang.Thread createSystemThread(java.lang.Runnable,java.lang.ThreadGroup)
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.InstrumentInfo> getInstruments()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getLanguages()
meth public java.util.concurrent.Future<java.lang.Void> submitThreadLocal(com.oracle.truffle.api.TruffleContext,java.lang.Thread[],com.oracle.truffle.api.ThreadLocalAction)
meth public long calculateContextHeapSize(com.oracle.truffle.api.TruffleContext,long,java.util.concurrent.atomic.AtomicBoolean)
meth public org.graalvm.options.OptionValues getOptions()
meth public org.graalvm.options.OptionValues getOptions(com.oracle.truffle.api.TruffleContext)
meth public org.graalvm.polyglot.SandboxPolicy getSandboxPolicy()
meth public org.graalvm.polyglot.io.MessageEndpoint startServer(java.net.URI,org.graalvm.polyglot.io.MessageEndpoint) throws java.io.IOException,org.graalvm.polyglot.io.MessageTransport$VetoException
meth public void registerService(java.lang.Object)
meth public void setAsynchronousStackDepth(int)
supr java.lang.Object
hfds err,in,instrumenter,messageTransport,options,out,polyglotInstrument,services
hcls GuardedExecutableNode,MessageTransportProxy

CLSS public abstract interface static com.oracle.truffle.api.instrumentation.TruffleInstrument$Provider
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.1")
meth public abstract com.oracle.truffle.api.instrumentation.TruffleInstrument create()
meth public abstract java.lang.String getInstrumentClassName()
meth public abstract java.util.Collection<java.lang.String> getServicesClassNames()

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.TruffleInstrument$Registration
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean internal()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.InternalResource>[] internalResources()
meth public abstract !hasdefault java.lang.Class<?>[] services()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String version()
meth public abstract !hasdefault java.lang.String website()
meth public abstract !hasdefault org.graalvm.polyglot.SandboxPolicy sandbox()

CLSS public final com.oracle.truffle.api.interop.ArityException
meth public int getActualArity()
meth public int getExpectedMaxArity()
meth public int getExpectedMinArity()
meth public java.lang.String getMessage()
meth public static com.oracle.truffle.api.interop.ArityException create(int,int,int)
meth public static com.oracle.truffle.api.interop.ArityException create(int,int,int,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds actualArity,expectedMaxArity,expectedMinArity,serialVersionUID

CLSS public final !enum com.oracle.truffle.api.interop.ExceptionType
fld public final static com.oracle.truffle.api.interop.ExceptionType EXIT
fld public final static com.oracle.truffle.api.interop.ExceptionType INTERRUPT
fld public final static com.oracle.truffle.api.interop.ExceptionType PARSE_ERROR
fld public final static com.oracle.truffle.api.interop.ExceptionType RUNTIME_ERROR
meth public static com.oracle.truffle.api.interop.ExceptionType valueOf(java.lang.String)
meth public static com.oracle.truffle.api.interop.ExceptionType[] values()
supr java.lang.Enum<com.oracle.truffle.api.interop.ExceptionType>

CLSS public abstract com.oracle.truffle.api.interop.InteropException
meth public final java.lang.Throwable fillInStackTrace()
meth public final java.lang.Throwable getCause()
meth public final java.lang.Throwable initCause(java.lang.Throwable)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract com.oracle.truffle.api.interop.InteropLibrary
cons protected init()
meth protected com.oracle.truffle.api.utilities.TriState isIdenticalOrUndefined(java.lang.Object,java.lang.Object)
meth protected final boolean assertAdopted()
meth public !varargs java.lang.Object execute(java.lang.Object,java.lang.Object[]) throws com.oracle.truffle.api.interop.ArityException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
meth public !varargs java.lang.Object instantiate(java.lang.Object,java.lang.Object[]) throws com.oracle.truffle.api.interop.ArityException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
meth public !varargs java.lang.Object invokeMember(java.lang.Object,java.lang.String,java.lang.Object[]) throws com.oracle.truffle.api.interop.ArityException,com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
meth public boolean asBoolean(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean fitsInBigInteger(java.lang.Object)
meth public boolean fitsInByte(java.lang.Object)
meth public boolean fitsInDouble(java.lang.Object)
meth public boolean fitsInFloat(java.lang.Object)
meth public boolean fitsInInt(java.lang.Object)
meth public boolean fitsInLong(java.lang.Object)
meth public boolean fitsInShort(java.lang.Object)
meth public boolean hasArrayElements(java.lang.Object)
meth public boolean hasBufferElements(java.lang.Object)
meth public boolean hasDeclaringMetaObject(java.lang.Object)
meth public boolean hasExceptionCause(java.lang.Object)
meth public boolean hasExceptionMessage(java.lang.Object)
meth public boolean hasExceptionStackTrace(java.lang.Object)
meth public boolean hasExecutableName(java.lang.Object)
meth public boolean hasHashEntries(java.lang.Object)
meth public boolean hasIterator(java.lang.Object)
meth public boolean hasIteratorNextElement(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean hasLanguage(java.lang.Object)
meth public boolean hasMemberReadSideEffects(java.lang.Object,java.lang.String)
meth public boolean hasMemberWriteSideEffects(java.lang.Object,java.lang.String)
meth public boolean hasMembers(java.lang.Object)
meth public boolean hasMetaObject(java.lang.Object)
meth public boolean hasMetaParents(java.lang.Object)
meth public boolean hasScopeParent(java.lang.Object)
meth public boolean hasSourceLocation(java.lang.Object)
meth public boolean isArrayElementInsertable(java.lang.Object,long)
meth public boolean isArrayElementModifiable(java.lang.Object,long)
meth public boolean isArrayElementReadable(java.lang.Object,long)
meth public boolean isArrayElementRemovable(java.lang.Object,long)
meth public boolean isBoolean(java.lang.Object)
meth public boolean isBufferWritable(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean isDate(java.lang.Object)
meth public boolean isDuration(java.lang.Object)
meth public boolean isException(java.lang.Object)
meth public boolean isExceptionIncompleteSource(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean isExecutable(java.lang.Object)
meth public boolean isHashEntryExisting(java.lang.Object,java.lang.Object)
meth public boolean isHashEntryInsertable(java.lang.Object,java.lang.Object)
meth public boolean isHashEntryModifiable(java.lang.Object,java.lang.Object)
meth public boolean isHashEntryReadable(java.lang.Object,java.lang.Object)
meth public boolean isHashEntryRemovable(java.lang.Object,java.lang.Object)
meth public boolean isHashEntryWritable(java.lang.Object,java.lang.Object)
meth public boolean isIdentical(java.lang.Object,java.lang.Object,com.oracle.truffle.api.interop.InteropLibrary)
meth public boolean isInstantiable(java.lang.Object)
meth public boolean isIterator(java.lang.Object)
meth public boolean isMemberInsertable(java.lang.Object,java.lang.String)
meth public boolean isMemberInternal(java.lang.Object,java.lang.String)
meth public boolean isMemberInvocable(java.lang.Object,java.lang.String)
meth public boolean isMemberModifiable(java.lang.Object,java.lang.String)
meth public boolean isMemberReadable(java.lang.Object,java.lang.String)
meth public boolean isMemberRemovable(java.lang.Object,java.lang.String)
meth public boolean isMetaInstance(java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean isMetaObject(java.lang.Object)
meth public boolean isNull(java.lang.Object)
meth public boolean isNumber(java.lang.Object)
meth public boolean isPointer(java.lang.Object)
meth public boolean isScope(java.lang.Object)
meth public boolean isString(java.lang.Object)
meth public boolean isTime(java.lang.Object)
meth public boolean isTimeZone(java.lang.Object)
meth public byte asByte(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public byte readBufferByte(java.lang.Object,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public com.oracle.truffle.api.interop.ExceptionType getExceptionType(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public com.oracle.truffle.api.source.SourceSection getSourceLocation(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public com.oracle.truffle.api.strings.TruffleString asTruffleString(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public double asDouble(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public double readBufferDouble(java.lang.Object,java.nio.ByteOrder,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public final boolean hasIdentity(java.lang.Object)
meth public final boolean isArrayElementExisting(java.lang.Object,long)
meth public final boolean isArrayElementWritable(java.lang.Object,long)
meth public final boolean isInstant(java.lang.Object)
meth public final boolean isMemberExisting(java.lang.Object,java.lang.String)
meth public final boolean isMemberWritable(java.lang.Object,java.lang.String)
meth public final java.lang.Object getMembers(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public final java.lang.Object toDisplayString(java.lang.Object)
meth public float asFloat(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public float readBufferFloat(java.lang.Object,java.nio.ByteOrder,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int asInt(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int getExceptionExitStatus(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int identityHashCode(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int readBufferInt(java.lang.Object,java.nio.ByteOrder,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>> getLanguage(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getDeclaringMetaObject(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionCause(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionMessage(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionStackTrace(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExecutableName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getHashEntriesIterator(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getHashKeysIterator(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getHashValuesIterator(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getIterator(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getIteratorNextElement(java.lang.Object) throws com.oracle.truffle.api.interop.StopIterationException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMembers(java.lang.Object,boolean) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaObject(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaParents(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaQualifiedName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaSimpleName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getScopeParent(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readArrayElement(java.lang.Object,long) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readHashValue(java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.interop.UnknownKeyException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readHashValueOrDefault(java.lang.Object,java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readMember(java.lang.Object,java.lang.String) throws com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object toDisplayString(java.lang.Object,boolean)
meth public java.lang.RuntimeException throwException(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.String asString(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.math.BigInteger asBigInteger(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.Duration asDuration(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.Instant asInstant(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.LocalDate asDate(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.LocalTime asTime(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.ZoneId asTimeZone(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long asLong(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long asPointer(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long getArraySize(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long getBufferSize(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long getHashSize(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long readBufferLong(java.lang.Object,java.nio.ByteOrder,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public short asShort(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public short readBufferShort(java.lang.Object,java.nio.ByteOrder,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public static boolean isValidProtocolValue(java.lang.Object)
meth public static boolean isValidValue(java.lang.Object)
meth public static com.oracle.truffle.api.interop.InteropLibrary getUncached()
meth public static com.oracle.truffle.api.interop.InteropLibrary getUncached(java.lang.Object)
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.interop.InteropLibrary> getFactory()
meth public void readBuffer(java.lang.Object,long,byte[],int,int) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void removeArrayElement(java.lang.Object,long) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void removeHashEntry(java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.interop.UnknownKeyException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void removeMember(java.lang.Object,java.lang.String) throws com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void toNative(java.lang.Object)
meth public void writeArrayElement(java.lang.Object,long,java.lang.Object) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
meth public void writeBufferByte(java.lang.Object,long,byte) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeBufferDouble(java.lang.Object,java.nio.ByteOrder,long,double) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeBufferFloat(java.lang.Object,java.nio.ByteOrder,long,float) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeBufferInt(java.lang.Object,java.nio.ByteOrder,long,int) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeBufferLong(java.lang.Object,java.nio.ByteOrder,long,long) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeBufferShort(java.lang.Object,java.nio.ByteOrder,long,short) throws com.oracle.truffle.api.interop.InvalidBufferOffsetException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void writeHashEntry(java.lang.Object,java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.interop.UnknownKeyException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
meth public void writeMember(java.lang.Object,java.lang.String,java.lang.Object) throws com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
supr com.oracle.truffle.api.library.Library
hfds FACTORY,UNCACHED
hcls Asserts

CLSS public final com.oracle.truffle.api.interop.InvalidArrayIndexException
meth public java.lang.String getMessage()
meth public long getInvalidIndex()
meth public static com.oracle.truffle.api.interop.InvalidArrayIndexException create(long)
meth public static com.oracle.truffle.api.interop.InvalidArrayIndexException create(long,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds invalidIndex,serialVersionUID

CLSS public final com.oracle.truffle.api.interop.InvalidBufferOffsetException
meth public java.lang.String getMessage()
meth public long getByteOffset()
meth public long getLength()
meth public static com.oracle.truffle.api.interop.InvalidBufferOffsetException create(long,long)
meth public static com.oracle.truffle.api.interop.InvalidBufferOffsetException create(long,long,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds byteOffset,length,serialVersionUID

CLSS public abstract com.oracle.truffle.api.interop.NodeLibrary
cons protected init()
meth public boolean hasReceiverMember(java.lang.Object,com.oracle.truffle.api.frame.Frame)
meth public boolean hasRootInstance(java.lang.Object,com.oracle.truffle.api.frame.Frame)
meth public boolean hasScope(java.lang.Object,com.oracle.truffle.api.frame.Frame)
meth public java.lang.Object getReceiverMember(java.lang.Object,com.oracle.truffle.api.frame.Frame) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getRootInstance(java.lang.Object,com.oracle.truffle.api.frame.Frame) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getScope(java.lang.Object,com.oracle.truffle.api.frame.Frame,boolean) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getView(java.lang.Object,com.oracle.truffle.api.frame.Frame,java.lang.Object)
meth public static com.oracle.truffle.api.interop.NodeLibrary getUncached()
meth public static com.oracle.truffle.api.interop.NodeLibrary getUncached(java.lang.Object)
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.interop.NodeLibrary> getFactory()
supr com.oracle.truffle.api.library.Library
hfds FACTORY
hcls Asserts

CLSS public final com.oracle.truffle.api.interop.StopIterationException
meth public java.lang.String getMessage()
meth public static com.oracle.truffle.api.interop.StopIterationException create()
meth public static com.oracle.truffle.api.interop.StopIterationException create(java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds INSTANCE,serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.interop.TruffleObject

CLSS public final com.oracle.truffle.api.interop.UnknownIdentifierException
meth public java.lang.String getMessage()
meth public java.lang.String getUnknownIdentifier()
meth public static com.oracle.truffle.api.interop.UnknownIdentifierException create(java.lang.String)
meth public static com.oracle.truffle.api.interop.UnknownIdentifierException create(java.lang.String,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds serialVersionUID,unknownIdentifier

CLSS public final com.oracle.truffle.api.interop.UnknownKeyException
meth public java.lang.Object getUnknownKey()
meth public java.lang.String getMessage()
meth public static com.oracle.truffle.api.interop.UnknownKeyException create(java.lang.Object)
meth public static com.oracle.truffle.api.interop.UnknownKeyException create(java.lang.Object,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds serialVersionUID,unknownKey

CLSS public final com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.String getMessage()
meth public static com.oracle.truffle.api.interop.UnsupportedMessageException create()
meth public static com.oracle.truffle.api.interop.UnsupportedMessageException create(java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds serialVersionUID

CLSS public final com.oracle.truffle.api.interop.UnsupportedTypeException
meth public java.lang.Object[] getSuppliedValues()
meth public static com.oracle.truffle.api.interop.UnsupportedTypeException create(java.lang.Object[])
meth public static com.oracle.truffle.api.interop.UnsupportedTypeException create(java.lang.Object[],java.lang.String)
meth public static com.oracle.truffle.api.interop.UnsupportedTypeException create(java.lang.Object[],java.lang.String,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds serialVersionUID,suppliedValues

CLSS public final com.oracle.truffle.api.io.TruffleProcessBuilder
meth public !varargs com.oracle.truffle.api.io.TruffleProcessBuilder command(java.lang.String[])
meth public com.oracle.truffle.api.io.TruffleProcessBuilder clearEnvironment(boolean)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder command(java.util.List<java.lang.String>)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder directory(com.oracle.truffle.api.TruffleFile)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder environment(java.lang.String,java.lang.String)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder environment(java.util.Map<java.lang.String,java.lang.String>)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder inheritIO(boolean)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder redirectError(org.graalvm.polyglot.io.ProcessHandler$Redirect)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder redirectErrorStream(boolean)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder redirectInput(org.graalvm.polyglot.io.ProcessHandler$Redirect)
meth public com.oracle.truffle.api.io.TruffleProcessBuilder redirectOutput(org.graalvm.polyglot.io.ProcessHandler$Redirect)
meth public java.lang.Process start() throws java.io.IOException
meth public org.graalvm.polyglot.io.ProcessHandler$Redirect createRedirectToStream(java.io.OutputStream)
supr java.lang.Object
hfds clearEnvironment,cmd,cwd,env,errorRedirect,fileSystem,inheritIO,inputRedirect,outputRedirect,polyglotLanguageContext,redirectErrorStream

CLSS public abstract interface !annotation com.oracle.truffle.api.library.CachedLibrary
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String limit()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface com.oracle.truffle.api.library.DefaultExportProvider
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.1")
intf com.oracle.truffle.api.library.provider.DefaultExportProvider

CLSS public abstract com.oracle.truffle.api.library.DynamicDispatchLibrary
cons protected init()
meth public abstract java.lang.Object cast(java.lang.Object)
meth public java.lang.Class<?> dispatch(java.lang.Object)
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.library.DynamicDispatchLibrary> getFactory()
supr com.oracle.truffle.api.library.Library
hfds FACTORY

CLSS public abstract interface com.oracle.truffle.api.library.EagerExportProvider
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.1")
intf com.oracle.truffle.api.library.provider.EagerExportProvider

CLSS public abstract interface !annotation com.oracle.truffle.api.library.ExportLibrary
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.library.ExportLibrary$Repeat)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Repeat
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean useForAOT()
meth public abstract !hasdefault int priority()
meth public abstract !hasdefault int useForAOTPriority()
meth public abstract !hasdefault java.lang.Class<?> receiverType()
meth public abstract !hasdefault java.lang.String delegateTo()
meth public abstract !hasdefault java.lang.String transitionLimit()
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.library.Library> value()

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.ExportLibrary$Repeat
 outer com.oracle.truffle.api.library.ExportLibrary
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract com.oracle.truffle.api.library.ExportLibrary[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.library.ExportMessage
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.library.ExportMessage$Repeat)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
innr public abstract interface static !annotation Ignore
innr public abstract interface static !annotation Repeat
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.library.Library> library()
meth public abstract !hasdefault java.lang.String limit()
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.ExportMessage$Ignore
 outer com.oracle.truffle.api.library.ExportMessage
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.ExportMessage$Repeat
 outer com.oracle.truffle.api.library.ExportMessage
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract com.oracle.truffle.api.library.ExportMessage[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.library.GenerateLibrary
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Abstract
innr public abstract interface static !annotation DefaultExport
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean defaultExportLookupEnabled()
meth public abstract !hasdefault boolean dynamicDispatchEnabled()
meth public abstract !hasdefault boolean pushEncapsulatingNode()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.library.Library> assertions()
meth public abstract !hasdefault java.lang.Class<?> receiverType()

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.GenerateLibrary$Abstract
 outer com.oracle.truffle.api.library.GenerateLibrary
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] ifExported()
meth public abstract !hasdefault java.lang.String[] ifExportedAsWarning()

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.GenerateLibrary$DefaultExport
 outer com.oracle.truffle.api.library.GenerateLibrary
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.library.GenerateLibrary$DefaultExport$Repeat)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Repeat
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface static !annotation com.oracle.truffle.api.library.GenerateLibrary$DefaultExport$Repeat
 outer com.oracle.truffle.api.library.GenerateLibrary$DefaultExport
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract com.oracle.truffle.api.library.GenerateLibrary$DefaultExport[] value()

CLSS public abstract com.oracle.truffle.api.library.Library
cons protected init()
meth public abstract boolean accepts(java.lang.Object)
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract com.oracle.truffle.api.library.LibraryExport<%0 extends com.oracle.truffle.api.library.Library>
cons protected init(java.lang.Class<? extends {com.oracle.truffle.api.library.LibraryExport%0}>,java.lang.Class<?>,boolean)
cons protected init(java.lang.Class<? extends {com.oracle.truffle.api.library.LibraryExport%0}>,java.lang.Class<?>,boolean,boolean,int)
innr protected abstract interface static DelegateExport
meth protected !varargs static com.oracle.truffle.api.utilities.FinalBitSet createMessageBitSet(com.oracle.truffle.api.library.LibraryFactory<?>,java.lang.String[])
meth protected abstract {com.oracle.truffle.api.library.LibraryExport%0} createCached(java.lang.Object)
meth protected abstract {com.oracle.truffle.api.library.LibraryExport%0} createUncached(java.lang.Object)
meth protected static <%0 extends com.oracle.truffle.api.library.Library> {%%0} createDelegate(com.oracle.truffle.api.library.LibraryFactory<{%%0}>,{%%0})
meth protected static boolean assertAdopted(com.oracle.truffle.api.nodes.Node)
meth public !varargs static <%0 extends com.oracle.truffle.api.library.Library> void register(java.lang.Class<?>,com.oracle.truffle.api.library.LibraryExport<?>[])
meth public final java.lang.String toString()
supr java.lang.Object
hfds GENERATED_CLASS_SUFFIX,aot,aotPriority,defaultExport,library,receiverClass,registerClass

CLSS protected abstract interface static com.oracle.truffle.api.library.LibraryExport$DelegateExport
 outer com.oracle.truffle.api.library.LibraryExport
meth public abstract com.oracle.truffle.api.library.Library getDelegateExportLibrary(java.lang.Object)
meth public abstract com.oracle.truffle.api.utilities.FinalBitSet getDelegateExportMessages()
meth public abstract java.lang.Object readDelegateExport(java.lang.Object)

CLSS public abstract com.oracle.truffle.api.library.LibraryFactory<%0 extends com.oracle.truffle.api.library.Library>
cons protected init(java.lang.Class<{com.oracle.truffle.api.library.LibraryFactory%0}>,java.util.List<com.oracle.truffle.api.library.Message>)
meth protected !varargs com.oracle.truffle.api.utilities.FinalBitSet createMessageBitSet(com.oracle.truffle.api.library.Message[])
meth protected abstract java.lang.Class<?> getDefaultClass(java.lang.Object)
meth protected abstract java.lang.Object genericDispatch(com.oracle.truffle.api.library.Library,java.lang.Object,com.oracle.truffle.api.library.Message,java.lang.Object[],int) throws java.lang.Exception
meth protected abstract {com.oracle.truffle.api.library.LibraryFactory%0} createDispatchImpl(int)
meth protected abstract {com.oracle.truffle.api.library.LibraryFactory%0} createProxy(com.oracle.truffle.api.library.ReflectionLibrary)
meth protected abstract {com.oracle.truffle.api.library.LibraryFactory%0} createUncachedDispatch()
meth protected final java.util.List<com.oracle.truffle.api.library.LibraryExport<{com.oracle.truffle.api.library.LibraryFactory%0}>> getAOTExports()
meth protected final {com.oracle.truffle.api.library.LibraryFactory%0} createAOT(com.oracle.truffle.api.library.LibraryExport<{com.oracle.truffle.api.library.LibraryFactory%0}>)
meth protected java.lang.invoke.MethodHandles$Lookup getLookup()
meth protected static <%0 extends com.oracle.truffle.api.library.Library> void register(java.lang.Class<{%%0}>,com.oracle.truffle.api.library.LibraryFactory<{%%0}>)
meth protected static <%0 extends com.oracle.truffle.api.library.Library> {%%0} getDelegateLibrary({%%0},java.lang.Object)
meth protected static boolean assertAdopted(com.oracle.truffle.api.nodes.Node)
meth protected static boolean isDelegated(com.oracle.truffle.api.library.Library,int)
meth protected static java.lang.Object readDelegate(com.oracle.truffle.api.library.Library,java.lang.Object)
meth protected {com.oracle.truffle.api.library.LibraryFactory%0} createAssertions({com.oracle.truffle.api.library.LibraryFactory%0})
meth protected {com.oracle.truffle.api.library.LibraryFactory%0} createDelegate({com.oracle.truffle.api.library.LibraryFactory%0})
meth public final java.util.List<com.oracle.truffle.api.library.Message> getMessages()
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} create(java.lang.Object)
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} createDispatched(int)
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} getUncached()
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} getUncached(java.lang.Object)
meth public java.lang.String toString()
meth public static <%0 extends com.oracle.truffle.api.library.Library> com.oracle.truffle.api.library.LibraryFactory<{%%0}> resolve(java.lang.Class<{%%0}>)
supr java.lang.Object
hfds EMPTY_DEFAULT_EXPORT_ARRAY,LIBRARIES,afterBuiltinDefaultExports,aot,beforeBuiltinDefaultExports,cachedCache,dispatchLibrary,eagerExportProviders,exportCache,externalDefaultProviders,libraryClass,messages,nameToMessages,proxyExports,uncachedCache,uncachedDispatch
hcls CachedAOTExports,ProxyExports,ResolvedDispatch

CLSS public abstract com.oracle.truffle.api.library.Message
cons protected !varargs init(java.lang.Class<? extends com.oracle.truffle.api.library.Library>,java.lang.String,int,boolean,java.lang.Class<?>,java.lang.Class<?>[])
cons protected !varargs init(java.lang.Class<? extends com.oracle.truffle.api.library.Library>,java.lang.String,int,java.lang.Class<?>,java.lang.Class<?>[])
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public final boolean equals(java.lang.Object)
meth public final boolean isDeprecated()
meth public final com.oracle.truffle.api.library.LibraryFactory<?> getFactory()
meth public final int getId()
meth public final int getParameterCount()
meth public final int hashCode()
meth public final java.lang.Class<? extends com.oracle.truffle.api.library.Library> getLibraryClass()
meth public final java.lang.Class<?> getParameterType(int)
meth public final java.lang.Class<?> getReceiverType()
meth public final java.lang.Class<?> getReturnType()
meth public final java.lang.String getLibraryName()
meth public final java.lang.String getQualifiedName()
meth public final java.lang.String getSimpleName()
meth public final java.lang.String toString()
meth public final java.util.List<java.lang.Class<?>> getParameterTypes()
meth public static com.oracle.truffle.api.library.Message resolve(java.lang.Class<? extends com.oracle.truffle.api.library.Library>,java.lang.String)
meth public static com.oracle.truffle.api.library.Message resolve(java.lang.Class<? extends com.oracle.truffle.api.library.Library>,java.lang.String,boolean)
supr java.lang.Object
hfds deprecated,hash,id,library,libraryClass,parameterCount,parameterTypes,parameterTypesArray,qualifiedName,returnType,simpleName

CLSS public abstract com.oracle.truffle.api.library.ReflectionLibrary
cons protected init()
meth public !varargs java.lang.Object send(java.lang.Object,com.oracle.truffle.api.library.Message,java.lang.Object[]) throws java.lang.Exception
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.library.ReflectionLibrary> getFactory()
meth public static com.oracle.truffle.api.library.ReflectionLibrary getUncached()
meth public static com.oracle.truffle.api.library.ReflectionLibrary getUncached(java.lang.Object)
supr com.oracle.truffle.api.library.Library
hfds FACTORY,UNCACHED

CLSS public abstract interface com.oracle.truffle.api.library.provider.DefaultExportProvider
meth public abstract int getPriority()
meth public abstract java.lang.Class<?> getDefaultExport()
meth public abstract java.lang.Class<?> getReceiverClass()
meth public abstract java.lang.String getLibraryClassName()

CLSS public abstract interface com.oracle.truffle.api.library.provider.EagerExportProvider
meth public abstract java.lang.String getLibraryClassName()
meth public abstract void ensureRegistered()

CLSS public abstract com.oracle.truffle.api.memory.ByteArraySupport
meth public abstract byte compareAndExchangeByte(byte[],long,byte,byte)
meth public abstract byte getAndAddByte(byte[],long,byte)
meth public abstract byte getAndBitwiseAndByte(byte[],long,byte)
meth public abstract byte getAndBitwiseOrByte(byte[],long,byte)
meth public abstract byte getAndBitwiseXorByte(byte[],long,byte)
meth public abstract byte getAndSetByte(byte[],long,byte)
meth public abstract byte getByte(byte[],int)
meth public abstract byte getByte(byte[],long)
meth public abstract byte getByteVolatile(byte[],long)
meth public abstract double getDouble(byte[],int)
meth public abstract double getDouble(byte[],long)
meth public abstract float getFloat(byte[],int)
meth public abstract float getFloat(byte[],long)
meth public abstract int compareAndExchangeInt(byte[],long,int,int)
meth public abstract int getAndAddInt(byte[],long,int)
meth public abstract int getAndBitwiseAndInt(byte[],long,int)
meth public abstract int getAndBitwiseOrInt(byte[],long,int)
meth public abstract int getAndBitwiseXorInt(byte[],long,int)
meth public abstract int getAndSetInt(byte[],long,int)
meth public abstract int getInt(byte[],int)
meth public abstract int getInt(byte[],long)
meth public abstract int getIntVolatile(byte[],long)
meth public abstract long compareAndExchangeLong(byte[],long,long,long)
meth public abstract long getAndAddLong(byte[],long,long)
meth public abstract long getAndBitwiseAndLong(byte[],long,long)
meth public abstract long getAndBitwiseOrLong(byte[],long,long)
meth public abstract long getAndBitwiseXorLong(byte[],long,long)
meth public abstract long getAndSetLong(byte[],long,long)
meth public abstract long getLong(byte[],int)
meth public abstract long getLong(byte[],long)
meth public abstract long getLongVolatile(byte[],long)
meth public abstract short compareAndExchangeShort(byte[],long,short,short)
meth public abstract short getAndAddShort(byte[],long,short)
meth public abstract short getAndBitwiseAndShort(byte[],long,short)
meth public abstract short getAndBitwiseOrShort(byte[],long,short)
meth public abstract short getAndBitwiseXorShort(byte[],long,short)
meth public abstract short getAndSetShort(byte[],long,short)
meth public abstract short getShort(byte[],int)
meth public abstract short getShort(byte[],long)
meth public abstract short getShortVolatile(byte[],long)
meth public abstract void putByte(byte[],int,byte)
meth public abstract void putByte(byte[],long,byte)
meth public abstract void putByteVolatile(byte[],long,byte)
meth public abstract void putDouble(byte[],int,double)
meth public abstract void putDouble(byte[],long,double)
meth public abstract void putFloat(byte[],int,float)
meth public abstract void putFloat(byte[],long,float)
meth public abstract void putInt(byte[],int,int)
meth public abstract void putInt(byte[],long,int)
meth public abstract void putIntVolatile(byte[],long,int)
meth public abstract void putLong(byte[],int,long)
meth public abstract void putLong(byte[],long,long)
meth public abstract void putLongVolatile(byte[],long,long)
meth public abstract void putShort(byte[],int,short)
meth public abstract void putShort(byte[],long,short)
meth public abstract void putShortVolatile(byte[],long,short)
meth public final boolean inBounds(byte[],int,int)
meth public final boolean inBounds(byte[],long,long)
meth public static com.oracle.truffle.api.memory.ByteArraySupport bigEndian()
meth public static com.oracle.truffle.api.memory.ByteArraySupport littleEndian()
supr java.lang.Object

CLSS public abstract com.oracle.truffle.api.nodes.BlockNode<%0 extends com.oracle.truffle.api.nodes.Node>
cons protected init({com.oracle.truffle.api.nodes.BlockNode%0}[])
fld public final static int NO_ARGUMENT = 0
innr public abstract interface static ElementExecutor
meth public abstract boolean executeBoolean(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract byte executeByte(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract char executeChar(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract double executeDouble(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract float executeFloat(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract int executeInt(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract java.lang.Object executeGeneric(com.oracle.truffle.api.frame.VirtualFrame,int)
meth public abstract long executeLong(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract short executeShort(com.oracle.truffle.api.frame.VirtualFrame,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public abstract void executeVoid(com.oracle.truffle.api.frame.VirtualFrame,int)
meth public final com.oracle.truffle.api.nodes.NodeCost getCost()
meth public final {com.oracle.truffle.api.nodes.BlockNode%0}[] getElements()
meth public static <%0 extends com.oracle.truffle.api.nodes.Node> com.oracle.truffle.api.nodes.BlockNode<{%%0}> create({%%0}[],com.oracle.truffle.api.nodes.BlockNode$ElementExecutor<{%%0}>)
supr com.oracle.truffle.api.nodes.Node
hfds elements

CLSS public abstract interface static com.oracle.truffle.api.nodes.BlockNode$ElementExecutor<%0 extends com.oracle.truffle.api.nodes.Node>
 outer com.oracle.truffle.api.nodes.BlockNode
meth public abstract void executeVoid(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int)
meth public boolean executeBoolean(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public byte executeByte(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public char executeChar(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public double executeDouble(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public float executeFloat(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public int executeInt(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public java.lang.Object executeGeneric(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int)
meth public long executeLong(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public short executeShort(com.oracle.truffle.api.frame.VirtualFrame,{com.oracle.truffle.api.nodes.BlockNode$ElementExecutor%0},int,int) throws com.oracle.truffle.api.nodes.UnexpectedResultException

CLSS public abstract interface com.oracle.truffle.api.nodes.BytecodeOSRNode
intf com.oracle.truffle.api.nodes.NodeInterface
meth public abstract java.lang.Object executeOSR(com.oracle.truffle.api.frame.VirtualFrame,int,java.lang.Object)
meth public abstract java.lang.Object getOSRMetadata()
meth public abstract void setOSRMetadata(java.lang.Object)
meth public com.oracle.truffle.api.frame.Frame restoreParentFrameFromArguments(java.lang.Object[])
meth public java.lang.Object[] storeParentFrameInArguments(com.oracle.truffle.api.frame.VirtualFrame)
meth public static boolean pollOSRBackEdge(com.oracle.truffle.api.nodes.BytecodeOSRNode)
meth public static java.lang.Object tryOSR(com.oracle.truffle.api.nodes.BytecodeOSRNode,int,java.lang.Object,java.lang.Runnable,com.oracle.truffle.api.frame.VirtualFrame)
meth public void copyIntoOSRFrame(com.oracle.truffle.api.frame.VirtualFrame,com.oracle.truffle.api.frame.VirtualFrame,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public void copyIntoOSRFrame(com.oracle.truffle.api.frame.VirtualFrame,com.oracle.truffle.api.frame.VirtualFrame,int,java.lang.Object)
meth public void prepareOSR(int)
meth public void restoreParentFrame(com.oracle.truffle.api.frame.VirtualFrame,com.oracle.truffle.api.frame.VirtualFrame)

CLSS public com.oracle.truffle.api.nodes.ControlFlowException
cons public init()
meth public final java.lang.Throwable fillInStackTrace()
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface !annotation com.oracle.truffle.api.nodes.DenyReplace
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract com.oracle.truffle.api.nodes.DirectCallNode
cons protected init(com.oracle.truffle.api.CallTarget)
fld protected final com.oracle.truffle.api.CallTarget callTarget
meth public abstract !varargs java.lang.Object call(java.lang.Object[])
meth public abstract boolean cloneCallTarget()
meth public abstract boolean isCallTargetCloningAllowed()
meth public abstract boolean isInlinable()
meth public abstract boolean isInliningForced()
meth public abstract com.oracle.truffle.api.CallTarget getClonedCallTarget()
meth public abstract void forceInlining()
meth public com.oracle.truffle.api.CallTarget getCallTarget()
meth public com.oracle.truffle.api.CallTarget getCurrentCallTarget()
meth public final boolean isCallTargetCloned()
meth public final com.oracle.truffle.api.nodes.RootNode getCurrentRootNode()
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.nodes.DirectCallNode create(com.oracle.truffle.api.CallTarget)
supr com.oracle.truffle.api.nodes.Node

CLSS public final com.oracle.truffle.api.nodes.EncapsulatingNodeReference
meth public com.oracle.truffle.api.nodes.Node get()
meth public com.oracle.truffle.api.nodes.Node set(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.nodes.EncapsulatingNodeReference getCurrent()
supr java.lang.Object
hfds CURRENT,reference,seenNullContext,thread

CLSS public abstract com.oracle.truffle.api.nodes.ExecutableNode
cons protected init(com.oracle.truffle.api.TruffleLanguage<?>)
meth public abstract java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public final <%0 extends com.oracle.truffle.api.TruffleLanguage> {%%0} getLanguage(java.lang.Class<{%%0}>)
meth public final com.oracle.truffle.api.nodes.LanguageInfo getLanguageInfo()
supr com.oracle.truffle.api.nodes.Node
hfds polyglotRef

CLSS public final com.oracle.truffle.api.nodes.ExecutionSignature
fld public final static com.oracle.truffle.api.nodes.ExecutionSignature GENERIC
meth public java.lang.Class<?> getReturnType()
meth public java.lang.Class<?>[] getArgumentTypes()
meth public static com.oracle.truffle.api.nodes.ExecutionSignature create(java.lang.Class<?>,java.lang.Class<?>[])
supr java.lang.Object
hfds argumentTypes,returnType

CLSS public abstract interface !annotation com.oracle.truffle.api.nodes.ExplodeLoop
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
innr public final static !enum LoopExplosionKind
intf java.lang.annotation.Annotation
meth public abstract !hasdefault com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind kind()

CLSS public final static !enum com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind
 outer com.oracle.truffle.api.nodes.ExplodeLoop
fld public final static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind FULL_EXPLODE
fld public final static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind FULL_EXPLODE_UNTIL_RETURN
fld public final static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind FULL_UNROLL
fld public final static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind FULL_UNROLL_UNTIL_RETURN
fld public final static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind MERGE_EXPLODE
meth public static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind valueOf(java.lang.String)
meth public static com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind[] values()
supr java.lang.Enum<com.oracle.truffle.api.nodes.ExplodeLoop$LoopExplosionKind>

CLSS public abstract com.oracle.truffle.api.nodes.IndirectCallNode
cons protected init()
meth public abstract !varargs java.lang.Object call(com.oracle.truffle.api.CallTarget,java.lang.Object[])
meth public static com.oracle.truffle.api.nodes.IndirectCallNode create()
meth public static com.oracle.truffle.api.nodes.IndirectCallNode getUncached()
supr com.oracle.truffle.api.nodes.Node
hfds UNCACHED

CLSS public final com.oracle.truffle.api.nodes.InvalidAssumptionException
cons public init()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds serialVersionUID

CLSS public final com.oracle.truffle.api.nodes.LanguageInfo
meth public boolean isInteractive()
meth public boolean isInternal()
meth public java.lang.String getDefaultMimeType()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.util.Set<java.lang.String> getMimeTypes()
supr java.lang.Object
hfds defaultMimeType,id,interactive,internal,languageCache,mimeTypes,name,version

CLSS public abstract com.oracle.truffle.api.nodes.LoopNode
cons protected init()
meth public abstract com.oracle.truffle.api.nodes.RepeatingNode getRepeatingNode()
meth public java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public static void reportLoopCount(com.oracle.truffle.api.nodes.Node,int)
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract com.oracle.truffle.api.nodes.Node
cons protected init()
innr public abstract interface static !annotation Child
innr public abstract interface static !annotation Children
intf com.oracle.truffle.api.nodes.NodeInterface
intf java.lang.Cloneable
meth protected final java.util.concurrent.locks.Lock getLock()
meth protected final void notifyInserted(com.oracle.truffle.api.nodes.Node)
meth protected void onReplace(com.oracle.truffle.api.nodes.Node,java.lang.CharSequence)
meth public boolean isAdoptable()
meth public com.oracle.truffle.api.nodes.Node copy()
meth public com.oracle.truffle.api.nodes.Node deepCopy()
meth public com.oracle.truffle.api.nodes.NodeCost getCost()
meth public com.oracle.truffle.api.source.SourceSection getEncapsulatingSourceSection()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} insert({%%0})
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} replace({%%0})
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} replace({%%0},java.lang.CharSequence)
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0}[] insert({%%0}[])
meth public final <%0 extends java.lang.Object> {%%0} atomic(java.util.concurrent.Callable<{%%0}>)
meth public final boolean isSafelyReplaceableBy(com.oracle.truffle.api.nodes.Node)
meth public final com.oracle.truffle.api.nodes.Node getParent()
meth public final com.oracle.truffle.api.nodes.RootNode getRootNode()
meth public final java.lang.Iterable<com.oracle.truffle.api.nodes.Node> getChildren()
meth public final void accept(com.oracle.truffle.api.nodes.NodeVisitor)
meth public final void adoptChildren()
meth public final void atomic(java.lang.Runnable)
meth public final void reportPolymorphicSpecialize()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.Object> getDebugProperties()
supr java.lang.Object
hfds GIL_LOCK,PARENT_LIMIT,SAME_LANGUAGE_CHECK_VISITOR,parent

CLSS public abstract interface static !annotation com.oracle.truffle.api.nodes.Node$Child
 outer com.oracle.truffle.api.nodes.Node
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation com.oracle.truffle.api.nodes.Node$Children
 outer com.oracle.truffle.api.nodes.Node
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract com.oracle.truffle.api.nodes.NodeClass
cons public init(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
meth protected abstract boolean isChildField(java.lang.Object)
meth protected abstract boolean isChildrenField(java.lang.Object)
meth protected abstract boolean isCloneableField(java.lang.Object)
meth protected abstract boolean isReplaceAllowed()
meth protected abstract java.lang.Class<?> getFieldType(java.lang.Object)
meth protected abstract java.lang.Object getFieldObject(java.lang.Object,com.oracle.truffle.api.nodes.Node)
meth protected abstract java.lang.Object getFieldValue(java.lang.Object,com.oracle.truffle.api.nodes.Node)
meth protected abstract java.lang.Object[] getNodeFieldArray()
meth protected abstract java.lang.String getFieldName(java.lang.Object)
meth protected abstract void putFieldObject(java.lang.Object,com.oracle.truffle.api.nodes.Node,java.lang.Object)
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.nodes.Node> getType()
meth public java.util.Iterator<com.oracle.truffle.api.nodes.Node> makeIterator(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.nodes.NodeClass get(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.nodes.NodeClass get(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
supr java.lang.Object
hfds nodeClasses

CLSS public abstract com.oracle.truffle.api.nodes.NodeCloneable
cons protected init()
intf java.lang.Cloneable
meth protected java.lang.Object clone()
supr java.lang.Object

CLSS public final !enum com.oracle.truffle.api.nodes.NodeCost
fld public final static com.oracle.truffle.api.nodes.NodeCost MEGAMORPHIC
fld public final static com.oracle.truffle.api.nodes.NodeCost MONOMORPHIC
fld public final static com.oracle.truffle.api.nodes.NodeCost NONE
fld public final static com.oracle.truffle.api.nodes.NodeCost POLYMORPHIC
fld public final static com.oracle.truffle.api.nodes.NodeCost UNINITIALIZED
meth public boolean isTrivial()
meth public static com.oracle.truffle.api.nodes.NodeCost fromCount(int)
meth public static com.oracle.truffle.api.nodes.NodeCost valueOf(java.lang.String)
meth public static com.oracle.truffle.api.nodes.NodeCost[] values()
supr java.lang.Enum<com.oracle.truffle.api.nodes.NodeCost>

CLSS public abstract interface !annotation com.oracle.truffle.api.nodes.NodeInfo
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault com.oracle.truffle.api.nodes.NodeCost cost()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String language()
meth public abstract !hasdefault java.lang.String shortName()

CLSS public abstract interface com.oracle.truffle.api.nodes.NodeInterface

CLSS public final com.oracle.truffle.api.nodes.NodeUtil
innr public abstract interface static NodeCountFilter
meth public static <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} cloneNode({%%0})
meth public static <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} nonAtomicReplace(com.oracle.truffle.api.nodes.Node,{%%0},java.lang.CharSequence)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> findAllNodeInstances(com.oracle.truffle.api.nodes.Node,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> findAllParents(com.oracle.truffle.api.nodes.Node,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} findFirstNodeInstance(com.oracle.truffle.api.nodes.Node,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} findParent(com.oracle.truffle.api.nodes.Node,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0}[] concat({%%0}[],{%%0}[])
meth public static <%0 extends java.lang.annotation.Annotation> {%%0} findAnnotation(java.lang.Class<?>,java.lang.Class<{%%0}>)
meth public static boolean assertAdopted(com.oracle.truffle.api.nodes.Node)
meth public static boolean assertRecursion(com.oracle.truffle.api.nodes.Node,int)
meth public static boolean forEachChild(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.NodeVisitor)
meth public static boolean isReplacementSafe(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static boolean replaceChild(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static boolean verify(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.nodes.Node getNthParent(com.oracle.truffle.api.nodes.Node,int)
meth public static int countNodes(com.oracle.truffle.api.nodes.Node)
meth public static int countNodes(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.NodeUtil$NodeCountFilter)
meth public static java.lang.String findChildFieldName(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static java.lang.String printCompactTreeToString(com.oracle.truffle.api.nodes.Node)
meth public static java.lang.String printSourceAttributionTree(com.oracle.truffle.api.nodes.Node)
meth public static java.lang.String printSyntaxTags(java.lang.Object)
meth public static java.lang.String printTreeToString(com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<com.oracle.truffle.api.nodes.Node> collectNodes(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<com.oracle.truffle.api.nodes.Node> findNodeChildren(com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<java.lang.String> collectFieldNames(java.lang.Class<? extends com.oracle.truffle.api.nodes.Node>)
meth public static java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.Node> collectNodeChildren(com.oracle.truffle.api.nodes.Node)
meth public static java.util.Map<java.lang.String,java.lang.Object> collectNodeProperties(com.oracle.truffle.api.nodes.Node)
meth public static void printCompactTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printSourceAttributionTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printSourceAttributionTree(java.io.PrintWriter,com.oracle.truffle.api.nodes.Node)
meth public static void printTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printTree(java.io.PrintWriter,com.oracle.truffle.api.nodes.Node)
supr java.lang.Object
hcls NodeCounter

CLSS public abstract interface static com.oracle.truffle.api.nodes.NodeUtil$NodeCountFilter
 outer com.oracle.truffle.api.nodes.NodeUtil
fld public final static com.oracle.truffle.api.nodes.NodeUtil$NodeCountFilter NO_FILTER
meth public abstract boolean isCounted(com.oracle.truffle.api.nodes.Node)

CLSS public abstract interface com.oracle.truffle.api.nodes.NodeVisitor
meth public abstract boolean visit(com.oracle.truffle.api.nodes.Node)

CLSS public abstract interface com.oracle.truffle.api.nodes.RepeatingNode
fld public final static java.lang.Object BREAK_LOOP_STATUS
fld public final static java.lang.Object CONTINUE_LOOP_STATUS
intf com.oracle.truffle.api.nodes.NodeInterface
meth public abstract boolean executeRepeating(com.oracle.truffle.api.frame.VirtualFrame)
meth public boolean shouldContinue(java.lang.Object)
meth public java.lang.Object executeRepeatingWithValue(com.oracle.truffle.api.frame.VirtualFrame)
meth public java.lang.Object initialLoopStatus()

CLSS public abstract com.oracle.truffle.api.nodes.RootNode
cons protected init(com.oracle.truffle.api.TruffleLanguage<?>)
cons protected init(com.oracle.truffle.api.TruffleLanguage<?>,com.oracle.truffle.api.frame.FrameDescriptor)
meth protected boolean countsTowardsStackTraceLimit()
meth protected boolean isCloneUninitializedSupported()
meth protected boolean isInstrumentable()
meth protected boolean isSameFrame(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.Frame)
meth protected boolean isTrivial()
meth protected com.oracle.truffle.api.frame.FrameDescriptor getParentFrameDescriptor()
meth protected com.oracle.truffle.api.nodes.ExecutionSignature prepareForAOT()
meth protected com.oracle.truffle.api.nodes.RootNode cloneUninitialized()
meth protected int computeSize()
meth protected java.lang.Object translateStackTraceElement(com.oracle.truffle.api.TruffleStackTraceElement)
meth protected java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> findAsynchronousFrames(com.oracle.truffle.api.frame.Frame)
meth public abstract java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public boolean isCaptureFramesForTrace()
meth public boolean isCloningAllowed()
meth public boolean isInternal()
meth public com.oracle.truffle.api.nodes.Node copy()
meth public final com.oracle.truffle.api.RootCallTarget getCallTarget()
meth public final com.oracle.truffle.api.frame.FrameDescriptor getFrameDescriptor()
meth public java.lang.String getName()
meth public java.lang.String getQualifiedName()
meth public static com.oracle.truffle.api.nodes.RootNode createConstantNode(java.lang.Object)
supr com.oracle.truffle.api.nodes.ExecutableNode
hfds LOCK_UPDATER,callTarget,frameDescriptor,instrumentationBits,lock
hcls Constant

CLSS public com.oracle.truffle.api.nodes.SlowPathException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public final java.lang.Throwable fillInStackTrace()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.0")
supr java.lang.Exception
hfds serialVersionUID

CLSS public final com.oracle.truffle.api.nodes.UnexpectedResultException
cons public init(java.lang.Object)
meth public java.lang.Object getResult()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds result,serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.object.BooleanLocation
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract boolean getBoolean(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract boolean getBoolean(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Class<java.lang.Boolean> getType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public abstract interface com.oracle.truffle.api.object.DoubleLocation
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract double getDouble(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract double getDouble(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Class<java.lang.Double> getType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public abstract com.oracle.truffle.api.object.DynamicObject
cons protected init(com.oracle.truffle.api.object.Shape)
innr protected abstract interface static !annotation DynamicField
intf com.oracle.truffle.api.interop.TruffleObject
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public final com.oracle.truffle.api.object.Shape getShape()
supr java.lang.Object
hfds SHAPE_OFFSET,UNSAFE,extRef,extVal,shape

CLSS protected abstract interface static !annotation com.oracle.truffle.api.object.DynamicObject$DynamicField
 outer com.oracle.truffle.api.object.DynamicObject
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface com.oracle.truffle.api.object.DynamicObjectFactory
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract !varargs com.oracle.truffle.api.object.DynamicObject newInstance(java.lang.Object[])
meth public abstract com.oracle.truffle.api.object.Shape getShape()

CLSS public abstract com.oracle.truffle.api.object.DynamicObjectLibrary
cons protected init()
meth public abstract boolean containsKey(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public abstract boolean isShared(com.oracle.truffle.api.object.DynamicObject)
meth public abstract boolean putIfPresent(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object)
meth public abstract boolean removeKey(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public abstract boolean resetShape(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract boolean setDynamicType(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public abstract boolean setPropertyFlags(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,int)
meth public abstract boolean setShapeFlags(com.oracle.truffle.api.object.DynamicObject,int)
meth public abstract boolean updateShape(com.oracle.truffle.api.object.DynamicObject)
meth public abstract com.oracle.truffle.api.object.Property getProperty(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Property[] getPropertyArray(com.oracle.truffle.api.object.DynamicObject)
meth public abstract com.oracle.truffle.api.object.Shape getShape(com.oracle.truffle.api.object.DynamicObject)
meth public abstract int getShapeFlags(com.oracle.truffle.api.object.DynamicObject)
meth public abstract java.lang.Object getDynamicType(com.oracle.truffle.api.object.DynamicObject)
meth public abstract java.lang.Object getOrDefault(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object[] getKeyArray(com.oracle.truffle.api.object.DynamicObject)
meth public abstract void markShared(com.oracle.truffle.api.object.DynamicObject)
meth public abstract void put(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object)
meth public abstract void putConstant(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object,int)
meth public abstract void putWithFlags(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object,int)
meth public double getDoubleOrDefault(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public final int getPropertyFlagsOrDefault(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,int)
meth public int getIntOrDefault(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public long getLongOrDefault(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,java.lang.Object) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.object.DynamicObjectLibrary> getFactory()
meth public static com.oracle.truffle.api.object.DynamicObjectLibrary getUncached()
meth public void putDouble(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,double)
meth public void putInt(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,int)
meth public void putLong(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,long)
supr com.oracle.truffle.api.library.Library
hfds FACTORY,UNCACHED

CLSS public final com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
supr com.oracle.truffle.api.nodes.SlowPathException
hfds INSTANCE,serialVersionUID

CLSS public final com.oracle.truffle.api.object.HiddenKey
cons public init(java.lang.String)
intf com.oracle.truffle.api.interop.TruffleObject
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public final com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
supr com.oracle.truffle.api.nodes.SlowPathException
hfds INSTANCE,serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.object.IntLocation
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract int getInt(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract int getInt(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Class<java.lang.Integer> getType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public abstract com.oracle.truffle.api.object.Layout
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.1")
cons protected init()
fld public final static java.lang.String OPTION_PREFIX = "truffle.object."
innr protected abstract static Access
innr public final static !enum ImplicitCast
innr public final static Builder
meth protected com.oracle.truffle.api.object.Shape buildShape(java.lang.Object,java.lang.Object,int,com.oracle.truffle.api.Assumption)
meth protected static com.oracle.truffle.api.object.LayoutFactory getFactory()
meth public abstract com.oracle.truffle.api.object.Shape$Allocator createAllocator()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.1")
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject> getType()
supr java.lang.Object
hfds INT_TO_DOUBLE_FLAG,INT_TO_LONG_FLAG,LAYOUT_FACTORY

CLSS protected abstract static com.oracle.truffle.api.object.Layout$Access
 outer com.oracle.truffle.api.object.Layout
cons protected init()
meth public final com.oracle.truffle.api.object.DynamicObject objectClone(com.oracle.truffle.api.object.DynamicObject)
meth public final com.oracle.truffle.api.object.Shape getShape(com.oracle.truffle.api.object.DynamicObject)
meth public final int[] getPrimitiveArray(com.oracle.truffle.api.object.DynamicObject)
meth public final java.lang.Class<? extends java.lang.annotation.Annotation> getDynamicFieldAnnotation()
meth public final java.lang.Object[] getObjectArray(com.oracle.truffle.api.object.DynamicObject)
meth public final void setObjectArray(com.oracle.truffle.api.object.DynamicObject,java.lang.Object[])
meth public final void setPrimitiveArray(com.oracle.truffle.api.object.DynamicObject,int[])
meth public final void setShape(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.object.Layout$Builder
 outer com.oracle.truffle.api.object.Layout
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.1")
meth public com.oracle.truffle.api.object.Layout build()
meth public com.oracle.truffle.api.object.Layout$Builder addAllowedImplicitCast(com.oracle.truffle.api.object.Layout$ImplicitCast)
meth public com.oracle.truffle.api.object.Layout$Builder setAllowedImplicitCasts(java.util.EnumSet<com.oracle.truffle.api.object.Layout$ImplicitCast>)
meth public com.oracle.truffle.api.object.Layout$Builder type(java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject>)
supr java.lang.Object
hfds allowedImplicitCasts,dynamicObjectClass

CLSS public final static !enum com.oracle.truffle.api.object.Layout$ImplicitCast
 outer com.oracle.truffle.api.object.Layout
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.1")
fld public final static com.oracle.truffle.api.object.Layout$ImplicitCast IntToDouble
fld public final static com.oracle.truffle.api.object.Layout$ImplicitCast IntToLong
meth public static com.oracle.truffle.api.object.Layout$ImplicitCast valueOf(java.lang.String)
meth public static com.oracle.truffle.api.object.Layout$ImplicitCast[] values()
supr java.lang.Enum<com.oracle.truffle.api.object.Layout$ImplicitCast>

CLSS public abstract interface com.oracle.truffle.api.object.LayoutFactory
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Property createProperty(java.lang.Object,com.oracle.truffle.api.object.Location)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Property createProperty(java.lang.Object,com.oracle.truffle.api.object.Location,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract int getPriority()
meth public com.oracle.truffle.api.object.Layout createLayout(com.oracle.truffle.api.object.Layout$Builder)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public com.oracle.truffle.api.object.Shape createShape(java.lang.Object)

CLSS public abstract com.oracle.truffle.api.object.Location
cons protected init()
meth protected abstract java.lang.Object getInternal(com.oracle.truffle.api.object.DynamicObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected abstract void setInternal(com.oracle.truffle.api.object.DynamicObject,java.lang.Object) throws com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected double getDouble(com.oracle.truffle.api.object.DynamicObject,boolean) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth protected int getInt(com.oracle.truffle.api.object.DynamicObject,boolean) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth protected long getLong(com.oracle.truffle.api.object.DynamicObject,boolean) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth protected static boolean checkShape(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected static com.oracle.truffle.api.object.FinalLocationException finalLocation() throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected static com.oracle.truffle.api.object.IncompatibleLocationException incompatibleLocation() throws com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public boolean canSet(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public boolean canSet(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public boolean canStore(java.lang.Object)
meth public boolean isAssumedFinal()
meth public boolean isConstant()
meth public boolean isDeclared()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public boolean isFinal()
meth public boolean isPrimitive()
meth public boolean isValue()
meth public com.oracle.truffle.api.Assumption getFinalAssumption()
meth public final java.lang.Object get(com.oracle.truffle.api.object.DynamicObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public final java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public final void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public java.lang.Object getConstantValue()
meth public void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
supr java.lang.Object

CLSS public abstract interface com.oracle.truffle.api.object.LocationFactory
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Location createLocation(com.oracle.truffle.api.object.Shape,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public final !enum com.oracle.truffle.api.object.LocationModifier
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
fld public final static com.oracle.truffle.api.object.LocationModifier Final
fld public final static com.oracle.truffle.api.object.LocationModifier NonNull
meth public static com.oracle.truffle.api.object.LocationModifier valueOf(java.lang.String)
meth public static com.oracle.truffle.api.object.LocationModifier[] values()
supr java.lang.Enum<com.oracle.truffle.api.object.LocationModifier>

CLSS public abstract interface com.oracle.truffle.api.object.LongLocation
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Class<java.lang.Long> getType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract long getLong(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract long getLong(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public abstract interface com.oracle.truffle.api.object.ObjectLocation
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract boolean isNonNull()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Class<?> getType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")

CLSS public com.oracle.truffle.api.object.ObjectType
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
cons public init()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public boolean equals(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public int hashCode(com.oracle.truffle.api.object.DynamicObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public java.lang.Class<?> dispatch()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public java.lang.String toString(com.oracle.truffle.api.object.DynamicObject)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
supr java.lang.Object
hfds DEFAULT

CLSS public abstract com.oracle.truffle.api.object.Property
cons protected init()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract boolean isHidden()
meth public abstract com.oracle.truffle.api.object.Location getLocation()
meth public abstract int getFlags()
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Object getKey()
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setGeneric(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setSafe(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract void setSafe(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public static com.oracle.truffle.api.object.Property create(java.lang.Object,com.oracle.truffle.api.object.Location,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
supr java.lang.Object

CLSS public final com.oracle.truffle.api.object.PropertyGetter
meth public boolean accepts(com.oracle.truffle.api.object.DynamicObject)
meth public double getDouble(com.oracle.truffle.api.object.DynamicObject) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public int getFlags()
meth public int getInt(com.oracle.truffle.api.object.DynamicObject) throws com.oracle.truffle.api.nodes.UnexpectedResultException
meth public java.lang.Object get(com.oracle.truffle.api.object.DynamicObject)
meth public java.lang.Object getKey()
meth public long getLong(com.oracle.truffle.api.object.DynamicObject) throws com.oracle.truffle.api.nodes.UnexpectedResultException
supr java.lang.Object
hfds expectedShape,location,property

CLSS public abstract com.oracle.truffle.api.object.Shape
cons protected init()
innr public abstract static Allocator
innr public final static Builder
innr public final static DerivedBuilder
meth protected boolean hasInstanceProperties()
meth protected com.oracle.truffle.api.object.Shape setDynamicType(java.lang.Object)
meth protected com.oracle.truffle.api.object.Shape setFlags(int)
meth public abstract boolean check(com.oracle.truffle.api.object.DynamicObject)
meth public abstract boolean hasProperty(java.lang.Object)
meth public abstract boolean isLeaf()
meth public abstract boolean isValid()
meth public abstract com.oracle.truffle.api.Assumption getLeafAssumption()
meth public abstract com.oracle.truffle.api.Assumption getValidAssumption()
meth public abstract com.oracle.truffle.api.object.DynamicObject newInstance()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.DynamicObjectFactory createFactory()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Layout getLayout()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="21.1")
meth public abstract com.oracle.truffle.api.object.ObjectType getObjectType()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.3")
meth public abstract com.oracle.truffle.api.object.Property getLastProperty()
meth public abstract com.oracle.truffle.api.object.Property getProperty(java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Shape addProperty(com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Shape changeType(com.oracle.truffle.api.object.ObjectType)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Shape defineProperty(java.lang.Object,java.lang.Object,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Shape defineProperty(java.lang.Object,java.lang.Object,int,com.oracle.truffle.api.object.LocationFactory)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public abstract com.oracle.truffle.api.object.Shape getParent()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public abstract com.oracle.truffle.api.object.Shape getRoot()
meth public abstract com.oracle.truffle.api.object.Shape removeProperty(com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public abstract com.oracle.truffle.api.object.Shape replaceProperty(com.oracle.truffle.api.object.Property,com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public abstract com.oracle.truffle.api.object.Shape tryMerge(com.oracle.truffle.api.object.Shape)
meth public abstract com.oracle.truffle.api.object.Shape$Allocator allocator()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract int getPropertyCount()
meth public abstract java.lang.Iterable<com.oracle.truffle.api.object.Property> getProperties()
meth public abstract java.lang.Iterable<java.lang.Object> getKeys()
meth public abstract java.lang.Object getMutex()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract java.lang.Object getSharedData()
meth public abstract java.util.List<com.oracle.truffle.api.object.Property> getPropertyList()
meth public abstract java.util.List<com.oracle.truffle.api.object.Property> getPropertyListInternal(boolean)
meth public abstract java.util.List<java.lang.Object> getKeyList()
meth public boolean allPropertiesMatch(java.util.function.Predicate<com.oracle.truffle.api.object.Property>)
meth public boolean isShared()
meth public com.oracle.truffle.api.Assumption getPropertyAssumption(java.lang.Object)
meth public com.oracle.truffle.api.object.PropertyGetter makePropertyGetter(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape makeSharedShape()
meth public int getFlags()
meth public java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject> getLayoutClass()
meth public java.lang.Object getDynamicType()
meth public static com.oracle.truffle.api.object.Shape$Builder newBuilder()
meth public static com.oracle.truffle.api.object.Shape$DerivedBuilder newBuilder(com.oracle.truffle.api.object.Shape)
supr java.lang.Object
hfds OBJECT_FLAGS_MASK,OBJECT_FLAGS_SHIFT,OBJECT_PROPERTY_ASSUMPTIONS,OBJECT_SHARED
hcls AbstractBuilder

CLSS public abstract static com.oracle.truffle.api.object.Shape$Allocator
 outer com.oracle.truffle.api.object.Shape
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
cons protected init()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected abstract com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>,boolean,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth protected abstract com.oracle.truffle.api.object.Location locationForValue(java.lang.Object,boolean,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.3")
meth public abstract com.oracle.truffle.api.object.Location constantLocation(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Location declaredLocation(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Shape$Allocator addLocation(com.oracle.truffle.api.object.Location)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public abstract com.oracle.truffle.api.object.Shape$Allocator copy()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public final com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public final com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>,java.util.EnumSet<com.oracle.truffle.api.object.LocationModifier>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.2")
meth public final com.oracle.truffle.api.object.Location locationForValue(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="20.2")
meth public final com.oracle.truffle.api.object.Location locationForValue(java.lang.Object,java.util.EnumSet<com.oracle.truffle.api.object.LocationModifier>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="19.3")
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.object.Shape$Builder
 outer com.oracle.truffle.api.object.Shape
meth public com.oracle.truffle.api.object.Shape build()
meth public com.oracle.truffle.api.object.Shape$Builder addConstantProperty(java.lang.Object,java.lang.Object,int)
meth public com.oracle.truffle.api.object.Shape$Builder allowImplicitCastIntToDouble(boolean)
meth public com.oracle.truffle.api.object.Shape$Builder allowImplicitCastIntToLong(boolean)
meth public com.oracle.truffle.api.object.Shape$Builder dynamicType(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape$Builder layout(java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject>)
meth public com.oracle.truffle.api.object.Shape$Builder propertyAssumptions(boolean)
meth public com.oracle.truffle.api.object.Shape$Builder shapeFlags(int)
meth public com.oracle.truffle.api.object.Shape$Builder shared(boolean)
meth public com.oracle.truffle.api.object.Shape$Builder sharedData(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape$Builder singleContextAssumption(com.oracle.truffle.api.Assumption)
supr java.lang.Object<com.oracle.truffle.api.object.Shape$Builder>
hfds allowImplicitCastIntToDouble,allowImplicitCastIntToLong,dynamicType,layoutClass,properties,propertyAssumptions,shapeFlags,shared,sharedData,singleContextAssumption

CLSS public final static com.oracle.truffle.api.object.Shape$DerivedBuilder
 outer com.oracle.truffle.api.object.Shape
meth public com.oracle.truffle.api.object.Shape build()
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder addConstantProperty(java.lang.Object,java.lang.Object,int)
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder dynamicType(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder shapeFlags(int)
supr java.lang.Object<com.oracle.truffle.api.object.Shape$DerivedBuilder>
hfds baseShape,dynamicType,properties,shapeFlags

CLSS public final com.oracle.truffle.api.profiles.BranchProfile
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.BranchProfile create()
meth public static com.oracle.truffle.api.profiles.BranchProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedBranchProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void enter()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,visited

CLSS public final com.oracle.truffle.api.profiles.ByteValueProfile
meth public byte profile(byte)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.ByteValueProfile create()
meth public static com.oracle.truffle.api.profiles.ByteValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.ByteValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedByteValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,GENERIC,SPECIALIZED,UNINITIALIZED,cachedValue,state

CLSS public com.oracle.truffle.api.profiles.ConditionProfile
meth public boolean profile(boolean)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.ConditionProfile create()
meth public static com.oracle.truffle.api.profiles.ConditionProfile createBinaryProfile()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.profiles.ConditionProfile createCountingProfile()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.profiles.ConditionProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedConditionProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,wasFalse,wasTrue
hcls Counting,Disabled

CLSS public final com.oracle.truffle.api.profiles.CountingConditionProfile
meth public boolean profile(boolean)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.CountingConditionProfile create()
meth public static com.oracle.truffle.api.profiles.CountingConditionProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedCountingConditionProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,MAX_VALUE,falseCount,trueCount

CLSS public final com.oracle.truffle.api.profiles.DoubleValueProfile
meth public double profile(double)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.DoubleValueProfile create()
meth public static com.oracle.truffle.api.profiles.DoubleValueProfile createRawIdentityProfile()
meth public static com.oracle.truffle.api.profiles.DoubleValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedDoubleValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,GENERIC,SPECIALIZED,UNINITIALIZED,cachedRawValue,cachedValue,state

CLSS public final com.oracle.truffle.api.profiles.FloatValueProfile
meth public float profile(float)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.FloatValueProfile create()
meth public static com.oracle.truffle.api.profiles.FloatValueProfile createRawIdentityProfile()
meth public static com.oracle.truffle.api.profiles.FloatValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedFloatValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,GENERIC,SPECIALIZED,UNINITIALIZED,cachedRawValue,cachedValue,state

CLSS public final com.oracle.truffle.api.profiles.InlinedBranchProfile
meth public boolean wasEntered(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedBranchProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedBranchProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable(com.oracle.truffle.api.nodes.Node)
meth public void enter(com.oracle.truffle.api.nodes.Node)
meth public void reset(com.oracle.truffle.api.nodes.Node)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,REQUIRED_STATE_BITS,state

CLSS public final com.oracle.truffle.api.profiles.InlinedByteValueProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public byte profile(com.oracle.truffle.api.nodes.Node,byte)
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedByteValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedByteValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue

CLSS public final com.oracle.truffle.api.profiles.InlinedConditionProfile
meth public boolean profile(com.oracle.truffle.api.nodes.Node,boolean)
meth public boolean wasFalse(com.oracle.truffle.api.nodes.Node)
meth public boolean wasTrue(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedConditionProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedConditionProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable(com.oracle.truffle.api.nodes.Node)
meth public void reset(com.oracle.truffle.api.nodes.Node)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,REQUIRED_STATE_BITS,state

CLSS public final com.oracle.truffle.api.profiles.InlinedCountingConditionProfile
meth public boolean profile(com.oracle.truffle.api.nodes.Node,boolean)
meth public boolean wasFalse(com.oracle.truffle.api.nodes.Node)
meth public boolean wasTrue(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedCountingConditionProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedCountingConditionProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable(com.oracle.truffle.api.nodes.Node)
meth public void reset(com.oracle.truffle.api.nodes.Node)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,MAX_VALUE,falseCount,trueCount

CLSS public final com.oracle.truffle.api.profiles.InlinedDoubleValueProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public double profile(com.oracle.truffle.api.nodes.Node,double)
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedDoubleValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedDoubleValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue0

CLSS public final com.oracle.truffle.api.profiles.InlinedExactClassProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public <%0 extends java.lang.Object> {%%0} profile(com.oracle.truffle.api.nodes.Node,{%%0})
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedExactClassProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedExactClassProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue

CLSS public final com.oracle.truffle.api.profiles.InlinedFloatValueProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public float profile(com.oracle.truffle.api.nodes.Node,float)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedFloatValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedFloatValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue

CLSS public final com.oracle.truffle.api.profiles.InlinedIntValueProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public int profile(com.oracle.truffle.api.nodes.Node,int)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedIntValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedIntValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue

CLSS public final com.oracle.truffle.api.profiles.InlinedLongValueProfile
fld protected final com.oracle.truffle.api.dsl.InlineSupport$StateField state
fld protected final static int GENERIC = 2
fld protected final static int REQUIRED_STATE_BITS = 2
fld protected final static int SPECIALIZED = 1
fld protected final static int UNINITIALIZED = 0
meth public final void disable(com.oracle.truffle.api.nodes.Node)
meth public final void reset(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public long profile(com.oracle.truffle.api.nodes.Node,long)
meth public static com.oracle.truffle.api.profiles.InlinedLongValueProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedLongValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,cachedValue

CLSS public final com.oracle.truffle.api.profiles.InlinedLoopConditionProfile
meth public boolean inject(com.oracle.truffle.api.nodes.Node,boolean)
meth public boolean profile(com.oracle.truffle.api.nodes.Node,boolean)
meth public boolean wasFalse(com.oracle.truffle.api.nodes.Node)
meth public boolean wasTrue(com.oracle.truffle.api.nodes.Node)
meth public java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.profiles.InlinedLoopConditionProfile getUncached()
meth public static com.oracle.truffle.api.profiles.InlinedLoopConditionProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public void disable(com.oracle.truffle.api.nodes.Node)
meth public void profileCounted(com.oracle.truffle.api.nodes.Node,long)
meth public void reset(com.oracle.truffle.api.nodes.Node)
supr com.oracle.truffle.api.profiles.InlinedProfile
hfds DISABLED,MAX_VALUE,falseCount,trueCount

CLSS public abstract com.oracle.truffle.api.profiles.InlinedProfile
meth public abstract java.lang.String toString(com.oracle.truffle.api.nodes.Node)
meth public abstract void disable(com.oracle.truffle.api.nodes.Node)
meth public abstract void reset(com.oracle.truffle.api.nodes.Node)
meth public final java.lang.String toString()
supr java.lang.Object

CLSS public final com.oracle.truffle.api.profiles.IntValueProfile
meth public int profile(int)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.InlinedIntValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public static com.oracle.truffle.api.profiles.IntValueProfile create()
meth public static com.oracle.truffle.api.profiles.IntValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.IntValueProfile getUncached()
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,GENERIC,SPECIALIZED,UNINITIALIZED,cachedValue,state

CLSS public final com.oracle.truffle.api.profiles.LongValueProfile
meth public java.lang.String toString()
meth public long profile(long)
meth public static com.oracle.truffle.api.profiles.InlinedLongValueProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public static com.oracle.truffle.api.profiles.LongValueProfile create()
meth public static com.oracle.truffle.api.profiles.LongValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.LongValueProfile getUncached()
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.Profile
hfds DISABLED,GENERIC,SPECIALIZED,UNINITIALIZED,cachedValue,state

CLSS public final com.oracle.truffle.api.profiles.LoopConditionProfile
meth public boolean inject(boolean)
meth public boolean profile(boolean)
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.profiles.LoopConditionProfile create()
meth public static com.oracle.truffle.api.profiles.LoopConditionProfile createCountingProfile()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static com.oracle.truffle.api.profiles.LoopConditionProfile getUncached()
meth public void disable()
meth public void profileCounted(long)
meth public void reset()
supr com.oracle.truffle.api.profiles.ConditionProfile
hfds DISABLED,falseCount,trueCount

CLSS public final com.oracle.truffle.api.profiles.PrimitiveValueProfile
meth public <%0 extends java.lang.Object> {%%0} profile({%%0})
meth public boolean profile(boolean)
meth public byte profile(byte)
meth public char profile(char)
meth public double profile(double)
meth public float profile(float)
meth public int profile(int)
meth public java.lang.String toString()
meth public long profile(long)
meth public short profile(short)
meth public static com.oracle.truffle.api.profiles.PrimitiveValueProfile create()
meth public static com.oracle.truffle.api.profiles.PrimitiveValueProfile createEqualityProfile()
meth public static com.oracle.truffle.api.profiles.PrimitiveValueProfile getUncached()
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.profiles.ValueProfile
hfds DISABLED,GENERIC,UNINITIALIZED,cachedValue

CLSS public abstract com.oracle.truffle.api.profiles.Profile
meth public void disable()
meth public void reset()
supr com.oracle.truffle.api.nodes.NodeCloneable

CLSS public abstract com.oracle.truffle.api.profiles.ValueProfile
meth public abstract <%0 extends java.lang.Object> {%%0} profile({%%0})
meth public static com.oracle.truffle.api.profiles.InlinedExactClassProfile inline(com.oracle.truffle.api.dsl.InlineSupport$InlineTarget)
meth public static com.oracle.truffle.api.profiles.ValueProfile create()
meth public static com.oracle.truffle.api.profiles.ValueProfile createClassProfile()
meth public static com.oracle.truffle.api.profiles.ValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.ValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,ExactClass,Identity

CLSS public abstract com.oracle.truffle.api.provider.InternalResourceProvider
cons public init()
meth protected abstract java.lang.Object createInternalResource()
meth protected abstract java.lang.String getComponentId()
meth protected abstract java.lang.String getResourceId()
supr java.lang.Object

CLSS public abstract com.oracle.truffle.api.provider.TruffleLanguageProvider
cons protected init()
meth protected abstract java.lang.Object create()
meth protected abstract java.lang.String getLanguageClassName()
meth protected abstract java.util.Collection<java.lang.String> getServicesClassNames()
meth protected abstract java.util.List<?> createFileTypeDetectors()
meth protected java.lang.Object createInternalResource(java.lang.String)
meth protected java.util.List<java.lang.String> getInternalResourceIds()
supr java.lang.Object

CLSS public abstract com.oracle.truffle.api.source.Source
fld public final static java.lang.CharSequence CONTENT_NONE
innr public SourceBuilder
innr public final LiteralBuilder
meth public abstract boolean hasBytes()
meth public abstract boolean hasCharacters()
meth public abstract boolean isCached()
meth public abstract boolean isInteractive()
meth public abstract boolean isInternal()
meth public abstract java.lang.CharSequence getCharacters()
meth public abstract java.lang.String getLanguage()
meth public abstract java.lang.String getMimeType()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPath()
meth public abstract java.net.URL getURL()
meth public abstract org.graalvm.polyglot.io.ByteSequence getBytes()
meth public com.oracle.truffle.api.source.Source subSource(int,int)
meth public final boolean equals(java.lang.Object)
meth public final com.oracle.truffle.api.source.SourceSection createSection(int)
meth public final com.oracle.truffle.api.source.SourceSection createSection(int,int)
meth public final com.oracle.truffle.api.source.SourceSection createSection(int,int,int)
meth public final com.oracle.truffle.api.source.SourceSection createSection(int,int,int,int)
meth public final com.oracle.truffle.api.source.SourceSection createUnavailableSection()
meth public final int getColumnNumber(int)
meth public final int getLength()
meth public final int getLineCount()
meth public final int getLineLength(int)
meth public final int getLineNumber(int)
meth public final int getLineStartOffset(int)
meth public final int hashCode()
meth public final java.io.Reader getReader()
meth public final java.lang.CharSequence getCharacters(int)
meth public final java.net.URI getURI()
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.source.Source$LiteralBuilder newBuilder(com.oracle.truffle.api.source.Source)
meth public static com.oracle.truffle.api.source.Source$LiteralBuilder newBuilder(java.lang.String,java.lang.CharSequence,java.lang.String)
meth public static com.oracle.truffle.api.source.Source$LiteralBuilder newBuilder(java.lang.String,org.graalvm.polyglot.io.ByteSequence,java.lang.String)
meth public static com.oracle.truffle.api.source.Source$SourceBuilder newBuilder(java.lang.String,com.oracle.truffle.api.TruffleFile)
meth public static com.oracle.truffle.api.source.Source$SourceBuilder newBuilder(java.lang.String,java.io.Reader,java.lang.String)
meth public static com.oracle.truffle.api.source.Source$SourceBuilder newBuilder(java.lang.String,java.net.URL)
meth public static java.lang.String findLanguage(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public static java.lang.String findLanguage(java.lang.String)
meth public static java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public static java.lang.String findMimeType(com.oracle.truffle.api.TruffleFile) throws java.io.IOException
meth public static java.lang.String findMimeType(java.net.URL) throws java.io.IOException
supr java.lang.Object
hfds ALLOW_IO,BUFFER_SIZE,BYTE_SEQUENCE_CLASS,CONTENT_EMPTY,CONTENT_UNSET,EMPTY,MAX_BUFFER_SIZE,NO_FASTPATH_SUBSOURCE_CREATION_MESSAGE,SOURCES,URI_SCHEME,cachedPolyglotSource,computedURI,textMap

CLSS public final com.oracle.truffle.api.source.Source$LiteralBuilder
 outer com.oracle.truffle.api.source.Source
meth public com.oracle.truffle.api.source.Source build()
meth public com.oracle.truffle.api.source.Source$LiteralBuilder cached(boolean)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder canonicalizePath(boolean)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder content(java.lang.CharSequence)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder content(org.graalvm.polyglot.io.ByteSequence)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder encoding(java.nio.charset.Charset)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder interactive(boolean)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder internal(boolean)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder mimeType(java.lang.String)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder name(java.lang.String)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder uri(java.net.URI)
supr com.oracle.truffle.api.source.Source$SourceBuilder
hfds buildThrowsIOException

CLSS public com.oracle.truffle.api.source.Source$SourceBuilder
 outer com.oracle.truffle.api.source.Source
meth public com.oracle.truffle.api.source.Source build() throws java.io.IOException
meth public com.oracle.truffle.api.source.Source$LiteralBuilder content(java.lang.CharSequence)
meth public com.oracle.truffle.api.source.Source$LiteralBuilder content(org.graalvm.polyglot.io.ByteSequence)
meth public com.oracle.truffle.api.source.Source$SourceBuilder cached(boolean)
meth public com.oracle.truffle.api.source.Source$SourceBuilder canonicalizePath(boolean)
meth public com.oracle.truffle.api.source.Source$SourceBuilder encoding(java.nio.charset.Charset)
meth public com.oracle.truffle.api.source.Source$SourceBuilder interactive(boolean)
meth public com.oracle.truffle.api.source.Source$SourceBuilder internal(boolean)
meth public com.oracle.truffle.api.source.Source$SourceBuilder mimeType(java.lang.String)
meth public com.oracle.truffle.api.source.Source$SourceBuilder name(java.lang.String)
meth public com.oracle.truffle.api.source.Source$SourceBuilder uri(java.net.URI)
supr java.lang.Object
hfds cached,canonicalizePath,content,embedderSource,fileEncoding,fileSystemContext,interactive,internal,language,mimeType,name,origin,path,uri,url

CLSS public abstract com.oracle.truffle.api.source.SourceSection
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean hasCharIndex()
meth public abstract boolean hasColumns()
meth public abstract boolean hasLines()
meth public abstract boolean isAvailable()
meth public abstract int getCharEndIndex()
meth public abstract int getCharIndex()
meth public abstract int getCharLength()
meth public abstract int getEndColumn()
meth public abstract int getEndLine()
meth public abstract int getStartColumn()
meth public abstract int getStartLine()
meth public abstract int hashCode()
meth public abstract java.lang.CharSequence getCharacters()
meth public final com.oracle.truffle.api.source.Source getSource()
meth public final java.lang.String toString()
supr java.lang.Object
hfds source

CLSS public abstract interface com.oracle.truffle.api.staticobject.DefaultStaticObjectFactory
meth public abstract java.lang.Object create()

CLSS public final com.oracle.truffle.api.staticobject.DefaultStaticProperty
cons public init(java.lang.String)
meth public java.lang.String getId()
supr com.oracle.truffle.api.staticobject.StaticProperty
hfds id

CLSS public abstract com.oracle.truffle.api.staticobject.StaticProperty
cons protected init()
meth protected abstract java.lang.String getId()
meth public final boolean compareAndExchangeBoolean(java.lang.Object,boolean,boolean)
meth public final boolean compareAndSwapBoolean(java.lang.Object,boolean,boolean)
meth public final boolean compareAndSwapByte(java.lang.Object,byte,byte)
meth public final boolean compareAndSwapChar(java.lang.Object,char,char)
meth public final boolean compareAndSwapDouble(java.lang.Object,double,double)
meth public final boolean compareAndSwapFloat(java.lang.Object,float,float)
meth public final boolean compareAndSwapInt(java.lang.Object,int,int)
meth public final boolean compareAndSwapLong(java.lang.Object,long,long)
meth public final boolean compareAndSwapObject(java.lang.Object,java.lang.Object,java.lang.Object)
meth public final boolean compareAndSwapShort(java.lang.Object,short,short)
meth public final boolean getBoolean(java.lang.Object)
meth public final boolean getBooleanVolatile(java.lang.Object)
meth public final byte compareAndExchangeByte(java.lang.Object,byte,byte)
meth public final byte getByte(java.lang.Object)
meth public final byte getByteVolatile(java.lang.Object)
meth public final char compareAndExchangeChar(java.lang.Object,char,char)
meth public final char getChar(java.lang.Object)
meth public final char getCharVolatile(java.lang.Object)
meth public final double compareAndExchangeDouble(java.lang.Object,double,double)
meth public final double getDouble(java.lang.Object)
meth public final double getDoubleVolatile(java.lang.Object)
meth public final float compareAndExchangeFloat(java.lang.Object,float,float)
meth public final float getFloat(java.lang.Object)
meth public final float getFloatVolatile(java.lang.Object)
meth public final int compareAndExchangeInt(java.lang.Object,int,int)
meth public final int getAndAddInt(java.lang.Object,int)
meth public final int getAndSetInt(java.lang.Object,int)
meth public final int getInt(java.lang.Object)
meth public final int getIntVolatile(java.lang.Object)
meth public final java.lang.Object compareAndExchangeObject(java.lang.Object,java.lang.Object,java.lang.Object)
meth public final java.lang.Object getAndSetObject(java.lang.Object,java.lang.Object)
meth public final java.lang.Object getObject(java.lang.Object)
meth public final java.lang.Object getObjectVolatile(java.lang.Object)
meth public final long compareAndExchangeLong(java.lang.Object,long,long)
meth public final long getAndAddLong(java.lang.Object,long)
meth public final long getAndSetLong(java.lang.Object,long)
meth public final long getLong(java.lang.Object)
meth public final long getLongVolatile(java.lang.Object)
meth public final short compareAndExchangeShort(java.lang.Object,short,short)
meth public final short getShort(java.lang.Object)
meth public final short getShortVolatile(java.lang.Object)
meth public final void setBoolean(java.lang.Object,boolean)
meth public final void setBooleanVolatile(java.lang.Object,boolean)
meth public final void setByte(java.lang.Object,byte)
meth public final void setByteVolatile(java.lang.Object,byte)
meth public final void setChar(java.lang.Object,char)
meth public final void setCharVolatile(java.lang.Object,char)
meth public final void setDouble(java.lang.Object,double)
meth public final void setDoubleVolatile(java.lang.Object,double)
meth public final void setFloat(java.lang.Object,float)
meth public final void setFloatVolatile(java.lang.Object,float)
meth public final void setInt(java.lang.Object,int)
meth public final void setIntVolatile(java.lang.Object,int)
meth public final void setLong(java.lang.Object,long)
meth public final void setLongVolatile(java.lang.Object,long)
meth public final void setObject(java.lang.Object,java.lang.Object)
meth public final void setObjectVolatile(java.lang.Object,java.lang.Object)
meth public final void setShort(java.lang.Object,short)
meth public final void setShortVolatile(java.lang.Object,short)
supr java.lang.Object
hfds UNSAFE,offset,shape,storeAsFinal,type
hcls CASSupport

CLSS public abstract com.oracle.truffle.api.staticobject.StaticShape<%0 extends java.lang.Object>
innr public final static Builder
meth public final {com.oracle.truffle.api.staticobject.StaticShape%0} getFactory()
meth public static com.oracle.truffle.api.staticobject.StaticShape$Builder newBuilder(com.oracle.truffle.api.TruffleLanguage<?>)
supr java.lang.Object
hfds UNSAFE,factory,safetyChecks,storageClass
hcls StorageStrategy

CLSS public final static com.oracle.truffle.api.staticobject.StaticShape$Builder
 outer com.oracle.truffle.api.staticobject.StaticShape
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.staticobject.StaticShape<{%%0}> build(com.oracle.truffle.api.staticobject.StaticShape<{%%0}>)
meth public <%0 extends java.lang.Object> com.oracle.truffle.api.staticobject.StaticShape<{%%0}> build(java.lang.Class<?>,java.lang.Class<{%%0}>)
meth public com.oracle.truffle.api.staticobject.StaticShape$Builder property(com.oracle.truffle.api.staticobject.StaticProperty,java.lang.Class<?>,boolean)
meth public com.oracle.truffle.api.staticobject.StaticShape<com.oracle.truffle.api.staticobject.DefaultStaticObjectFactory> build()
supr java.lang.Object
hfds DELIMITER,MAX_NUMBER_OF_PROPERTIES,MAX_PROPERTY_ID_BYTE_LENGTH,counter,hasLongPropertyId,isActive,language,staticProperties,storageClassName

CLSS public abstract com.oracle.truffle.api.strings.AbstractTruffleString
meth public com.oracle.truffle.api.strings.TruffleString toValidStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean codeRangeEqualsUncached(com.oracle.truffle.api.strings.TruffleString$CodeRange)
meth public final boolean equals(java.lang.Object)
meth public final boolean equalsUncached(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean isCompatibleTo(com.oracle.truffle.api.strings.TruffleString$Encoding)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="23.0")
meth public final boolean isCompatibleToUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean isEmpty()
meth public final boolean isImmutable()
meth public final boolean isManaged()
meth public final boolean isMutable()
meth public final boolean isNative()
meth public final boolean isValidUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean regionEqualByteIndexUncached(int,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean regionEqualByteIndexUncached(int,com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean regionEqualsUncached(int,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final byte[] copyToByteArrayUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.InternalByteArray getInternalByteArrayUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.MutableTruffleString asManagedMutableTruffleStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.MutableTruffleString asMutableTruffleStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString asManagedTruffleStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString asManagedTruffleStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString asTruffleStringUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString concatUncached(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString forceEncodingUncached(com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString repeatUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString substringByteIndexUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString substringUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString switchEncodingUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString switchEncodingUncached(com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TranscodingErrorHandler)
meth public final com.oracle.truffle.api.strings.TruffleString$CodeRange getByteCodeRangeUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString$CodeRange getCodeRangeImpreciseUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString$CodeRange getCodeRangeUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString$CompactionLevel getStringCompactionLevelUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleStringIterator createBackwardCodePointIteratorUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleStringIterator createCodePointIteratorUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final double parseDoubleUncached() throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public final int byteIndexOfAnyByteUncached(int,int,byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteIndexOfCodePointUncached(int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteIndexOfStringUncached(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteIndexOfStringUncached(com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteIndexToCodePointIndexUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteLength(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteLengthOfCodePointUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int byteLengthOfCodePointUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int charIndexOfAnyCharUTF16Uncached(int,int,char[])
meth public final int codePointAtByteIndexUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int codePointAtByteIndexUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int codePointAtIndexUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int codePointAtIndexUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int codePointIndexToByteIndexUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int codePointLengthUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int compareBytesUncached(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int compareCharsUTF16Uncached(com.oracle.truffle.api.strings.AbstractTruffleString)
meth public final int compareIntsUTF32Uncached(com.oracle.truffle.api.strings.AbstractTruffleString)
meth public final int hashCode()
meth public final int hashCodeUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int indexOfCodePointUncached(int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int indexOfStringUncached(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int intIndexOfAnyIntUTF32Uncached(int,int,int[])
meth public final int lastByteIndexOfCodePointUncached(int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int lastByteIndexOfStringUncached(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int lastByteIndexOfStringUncached(com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int lastIndexOfCodePointUncached(int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int lastIndexOfStringUncached(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int parseIntUncached() throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public final int parseIntUncached(int) throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public final int readByteUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int readCharUTF16Uncached(int)
meth public final java.lang.Object getInternalNativePointerUncached(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final java.lang.String toJavaStringUncached()
meth public final java.lang.String toString()
meth public final java.lang.String toStringDebug()
meth public final long parseLongUncached() throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public final long parseLongUncached(int) throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public final void copyToByteArrayNodeUncached(int,byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.3")
meth public final void copyToByteArrayUncached(int,byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final void copyToNativeMemoryNodeUncached(int,java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="22.3")
meth public final void copyToNativeMemoryUncached(int,java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public void materializeUncached(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
supr java.lang.Object
hfds DEBUG_ALWAYS_CREATE_JAVA_STRING,DEBUG_NON_ZERO_OFFSET,DEBUG_STRICT_ENCODING_CHECKS,codePointLength,codeRange,data,encoding,flags,hashCode,length,offset,stride
hcls LazyConcat,LazyLong,NativePointer

CLSS public final com.oracle.truffle.api.strings.InternalByteArray
meth public byte get(int)
meth public byte[] getArray()
meth public int getEnd()
meth public int getLength()
meth public int getOffset()
supr java.lang.Object
hfds EMPTY,array,length,offset

CLSS public final com.oracle.truffle.api.strings.MutableTruffleString
innr public abstract static AsManagedNode
innr public abstract static AsMutableTruffleStringNode
innr public abstract static ConcatNode
innr public abstract static ForceEncodingNode
innr public abstract static FromByteArrayNode
innr public abstract static FromNativePointerNode
innr public abstract static SubstringByteIndexNode
innr public abstract static SubstringNode
innr public abstract static SwitchEncodingNode
innr public abstract static WriteByteNode
meth public com.oracle.truffle.api.strings.MutableTruffleString concatUncached(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public com.oracle.truffle.api.strings.MutableTruffleString substringByteIndexUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public com.oracle.truffle.api.strings.MutableTruffleString substringUncached(int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString fromByteArrayUncached(byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.MutableTruffleString fromNativePointerUncached(java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public void notifyExternalMutation()
meth public void writeByteUncached(int,byte,com.oracle.truffle.api.strings.TruffleString$Encoding)
supr com.oracle.truffle.api.strings.AbstractTruffleString
hcls CalcLazyAttributesNode,DataClassProfile

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$AsManagedNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$AsManagedNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$AsManagedNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$AsMutableTruffleStringNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$AsMutableTruffleStringNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$AsMutableTruffleStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$ConcatNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$ConcatNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$ConcatNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$ForceEncodingNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$ForceEncodingNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$ForceEncodingNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$FromByteArrayNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$FromByteArrayNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$FromByteArrayNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$FromNativePointerNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$FromNativePointerNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$FromNativePointerNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$SubstringByteIndexNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SubstringByteIndexNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SubstringByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$SubstringNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SubstringNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SubstringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$SwitchEncodingNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TranscodingErrorHandler)
meth public final com.oracle.truffle.api.strings.MutableTruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SwitchEncodingNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$SwitchEncodingNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.MutableTruffleString$WriteByteNode
 outer com.oracle.truffle.api.strings.MutableTruffleString
meth public abstract void execute(com.oracle.truffle.api.strings.MutableTruffleString,int,byte,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.MutableTruffleString$WriteByteNode create()
meth public static com.oracle.truffle.api.strings.MutableTruffleString$WriteByteNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final com.oracle.truffle.api.strings.MutableTruffleStringFactory
cons public init()
supr java.lang.Object
hcls AsManagedNodeGen,AsMutableTruffleStringNodeGen,CalcLazyAttributesNodeGen,ConcatNodeGen,DataClassProfileNodeGen,ForceEncodingNodeGen,FromByteArrayNodeGen,FromNativePointerNodeGen,SubstringByteIndexNodeGen,SubstringNodeGen,SwitchEncodingNodeGen,WriteByteNodeGen

CLSS public abstract interface com.oracle.truffle.api.strings.NativeAllocator
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.Object allocate(int)

CLSS public abstract interface com.oracle.truffle.api.strings.TranscodingErrorHandler
 anno 0 java.lang.FunctionalInterface()
fld public final static com.oracle.truffle.api.strings.TranscodingErrorHandler DEFAULT
fld public final static com.oracle.truffle.api.strings.TranscodingErrorHandler DEFAULT_KEEP_SURROGATES_IN_UTF8
innr public final static ReplacementString
meth public abstract com.oracle.truffle.api.strings.TranscodingErrorHandler$ReplacementString apply(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$Encoding)

CLSS public final static com.oracle.truffle.api.strings.TranscodingErrorHandler$ReplacementString
 outer com.oracle.truffle.api.strings.TranscodingErrorHandler
cons public init(com.oracle.truffle.api.strings.TruffleString,int)
meth public com.oracle.truffle.api.strings.TruffleString replacement()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String toString()
meth public int byteLength()
supr java.lang.Record
hfds byteLength,replacement

CLSS public final com.oracle.truffle.api.strings.TruffleString
innr public abstract static AsManagedNode
innr public abstract static AsNativeNode
innr public abstract static AsTruffleStringNode
innr public abstract static ByteIndexOfAnyByteNode
innr public abstract static ByteIndexOfCodePointNode
innr public abstract static ByteIndexOfCodePointSetNode
innr public abstract static ByteIndexOfStringNode
innr public abstract static ByteIndexToCodePointIndexNode
innr public abstract static ByteLengthOfCodePointNode
innr public abstract static CharIndexOfAnyCharUTF16Node
innr public abstract static CodePointAtByteIndexNode
innr public abstract static CodePointAtIndexNode
innr public abstract static CodePointIndexToByteIndexNode
innr public abstract static CodePointLengthNode
innr public abstract static CodeRangeEqualsNode
innr public abstract static CompareBytesNode
innr public abstract static CompareCharsUTF16Node
innr public abstract static CompareIntsUTF32Node
innr public abstract static ConcatNode
innr public abstract static CopyToByteArrayNode
innr public abstract static CopyToNativeMemoryNode
innr public abstract static CreateBackwardCodePointIteratorNode
innr public abstract static CreateCodePointIteratorNode
innr public abstract static EqualNode
innr public abstract static ForceEncodingNode
innr public abstract static FromByteArrayNode
innr public abstract static FromCharArrayUTF16Node
innr public abstract static FromCodePointNode
innr public abstract static FromIntArrayUTF32Node
innr public abstract static FromJavaStringNode
innr public abstract static FromLongNode
innr public abstract static FromNativePointerNode
innr public abstract static GetByteCodeRangeNode
innr public abstract static GetCodeRangeImpreciseNode
innr public abstract static GetCodeRangeNode
innr public abstract static GetInternalByteArrayNode
innr public abstract static GetInternalNativePointerNode
innr public abstract static GetStringCompactionLevelNode
innr public abstract static HashCodeNode
innr public abstract static IndexOfCodePointNode
innr public abstract static IndexOfStringNode
innr public abstract static IntIndexOfAnyIntUTF32Node
innr public abstract static IsValidNode
innr public abstract static LastByteIndexOfCodePointNode
innr public abstract static LastByteIndexOfStringNode
innr public abstract static LastIndexOfCodePointNode
innr public abstract static LastIndexOfStringNode
innr public abstract static MaterializeNode
innr public abstract static ParseDoubleNode
innr public abstract static ParseIntNode
innr public abstract static ParseLongNode
innr public abstract static ReadByteNode
innr public abstract static ReadCharUTF16Node
innr public abstract static RegionEqualByteIndexNode
innr public abstract static RegionEqualNode
innr public abstract static RepeatNode
innr public abstract static SubstringByteIndexNode
innr public abstract static SubstringNode
innr public abstract static SwitchEncodingNode
innr public abstract static ToJavaStringNode
innr public abstract static ToValidStringNode
innr public final static !enum CodeRange
innr public final static !enum CompactionLevel
innr public final static !enum Encoding
innr public final static !enum ErrorHandling
innr public final static CodePointSet
innr public final static IllegalByteArrayLengthException
innr public final static NumberFormatException
innr public final static WithMask
meth public com.oracle.truffle.api.strings.TruffleString asNativeUncached(com.oracle.truffle.api.strings.NativeAllocator,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromByteArrayUncached(byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString fromByteArrayUncached(byte[],com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromByteArrayUncached(byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromCharArrayUTF16Uncached(char[])
meth public static com.oracle.truffle.api.strings.TruffleString fromCharArrayUTF16Uncached(char[],int,int)
meth public static com.oracle.truffle.api.strings.TruffleString fromCodePointUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString fromCodePointUncached(int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromConstant(java.lang.String,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString fromIntArrayUTF32Uncached(int[])
meth public static com.oracle.truffle.api.strings.TruffleString fromIntArrayUTF32Uncached(int[],int,int)
meth public static com.oracle.truffle.api.strings.TruffleString fromJavaStringUncached(java.lang.String,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString fromJavaStringUncached(java.lang.String,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromLongUncached(long,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString fromNativePointerUncached(java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
supr com.oracle.truffle.api.strings.AbstractTruffleString
hfds FLAG_CACHE_HEAD,NEXT_UPDATER,next
hcls InternalAsTruffleStringNode,InternalCopyToByteArrayNode,InternalSwitchEncodingNode,ToIndexableNode

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$AsManagedNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$AsManagedNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$AsManagedNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$AsNativeNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.TruffleString,com.oracle.truffle.api.strings.NativeAllocator,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$AsNativeNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$AsNativeNode getUncached()
supr com.oracle.truffle.api.nodes.Node
hfds NULL_TERMINATION_BYTES

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$AsTruffleStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$AsTruffleStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$AsTruffleStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfAnyByteNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfAnyByteNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfAnyByteNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointSetNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$CodePointSet)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointSetNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfCodePointSetNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexOfStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteIndexToCodePointIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexToCodePointIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteIndexToCodePointIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ByteLengthOfCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ByteLengthOfCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ByteLengthOfCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CharIndexOfAnyCharUTF16Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,char[])
meth public static com.oracle.truffle.api.strings.TruffleString$CharIndexOfAnyCharUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$CharIndexOfAnyCharUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CodePointAtByteIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointAtByteIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointAtByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CodePointAtIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointAtIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointAtIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CodePointIndexToByteIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointIndexToByteIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointIndexToByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CodePointLengthNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointLengthNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointLengthNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static com.oracle.truffle.api.strings.TruffleString$CodePointSet
 outer com.oracle.truffle.api.strings.TruffleString
meth public boolean isIntrinsicCandidate(com.oracle.truffle.api.strings.TruffleString$CodeRange)
meth public static com.oracle.truffle.api.strings.TruffleString$CodePointSet fromRanges(int[],com.oracle.truffle.api.strings.TruffleString$Encoding)
supr java.lang.Object
hfds encoding,indexOfNodes,ranges

CLSS public final static !enum com.oracle.truffle.api.strings.TruffleString$CodeRange
 outer com.oracle.truffle.api.strings.TruffleString
fld public final static com.oracle.truffle.api.strings.TruffleString$CodeRange ASCII
fld public final static com.oracle.truffle.api.strings.TruffleString$CodeRange BMP
fld public final static com.oracle.truffle.api.strings.TruffleString$CodeRange BROKEN
fld public final static com.oracle.truffle.api.strings.TruffleString$CodeRange LATIN_1
fld public final static com.oracle.truffle.api.strings.TruffleString$CodeRange VALID
meth public boolean isSubsetOf(com.oracle.truffle.api.strings.TruffleString$CodeRange)
meth public boolean isSupersetOf(com.oracle.truffle.api.strings.TruffleString$CodeRange)
meth public static com.oracle.truffle.api.strings.TruffleString$CodeRange valueOf(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString$CodeRange[] values()
supr java.lang.Enum<com.oracle.truffle.api.strings.TruffleString$CodeRange>
hfds BYTE_CODE_RANGES

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CodeRangeEqualsNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$CodeRange)
meth public static com.oracle.truffle.api.strings.TruffleString$CodeRangeEqualsNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CodeRangeEqualsNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static !enum com.oracle.truffle.api.strings.TruffleString$CompactionLevel
 outer com.oracle.truffle.api.strings.TruffleString
fld public final static com.oracle.truffle.api.strings.TruffleString$CompactionLevel S1
fld public final static com.oracle.truffle.api.strings.TruffleString$CompactionLevel S2
fld public final static com.oracle.truffle.api.strings.TruffleString$CompactionLevel S4
meth public final int getBytes()
meth public final int getLog2()
meth public static com.oracle.truffle.api.strings.TruffleString$CompactionLevel valueOf(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString$CompactionLevel[] values()
supr java.lang.Enum<com.oracle.truffle.api.strings.TruffleString$CompactionLevel>
hfds bytes,log2

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CompareBytesNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CompareBytesNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CompareBytesNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CompareCharsUTF16Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString)
meth public static com.oracle.truffle.api.strings.TruffleString$CompareCharsUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$CompareCharsUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CompareIntsUTF32Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString)
meth public static com.oracle.truffle.api.strings.TruffleString$CompareIntsUTF32Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$CompareIntsUTF32Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ConcatNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$ConcatNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ConcatNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CopyToByteArrayNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract void execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final byte[] execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CopyToByteArrayNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CopyToByteArrayNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CopyToNativeMemoryNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract void execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CopyToNativeMemoryNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CopyToNativeMemoryNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CreateBackwardCodePointIteratorNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleStringIterator execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final com.oracle.truffle.api.strings.TruffleStringIterator execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CreateBackwardCodePointIteratorNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CreateBackwardCodePointIteratorNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$CreateCodePointIteratorNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleStringIterator execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$ErrorHandling)
meth public final com.oracle.truffle.api.strings.TruffleStringIterator execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$CreateCodePointIteratorNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$CreateCodePointIteratorNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static !enum com.oracle.truffle.api.strings.TruffleString$Encoding
 outer com.oracle.truffle.api.strings.TruffleString
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding BYTES
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Big5
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Big5_HKSCS
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Big5_UAO
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CESU_8
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP50220
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP50221
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP51932
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP850
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP852
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP855
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP949
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP950
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding CP951
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding EUC_JIS_2004
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding EUC_JP
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding EUC_KR
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding EUC_TW
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Emacs_Mule
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding EucJP_ms
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding GB12345
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding GB18030
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding GB1988
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding GB2312
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding GBK
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM037
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM437
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM720
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM737
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM775
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM852
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM855
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM857
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM860
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM861
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM862
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM863
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM864
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM865
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM866
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding IBM869
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_2022_JP
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_2022_JP_2
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_2022_JP_KDDI
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_1
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_10
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_11
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_13
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_14
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_15
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_16
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_2
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_3
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_4
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_5
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_6
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_7
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_8
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding ISO_8859_9
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding KOI8_R
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding KOI8_U
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacCentEuro
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacCroatian
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacCyrillic
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacGreek
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacIceland
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacJapanese
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacRoman
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacRomania
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacThai
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacTurkish
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding MacUkraine
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding SJIS_DoCoMo
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding SJIS_KDDI
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding SJIS_SoftBank
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Shift_JIS
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Stateless_ISO_2022_JP
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Stateless_ISO_2022_JP_KDDI
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding TIS_620
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding US_ASCII
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF8_DoCoMo
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF8_KDDI
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF8_MAC
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF8_SoftBank
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_16
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_16BE
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_16LE
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_32
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_32BE
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_32LE
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_7
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding UTF_8
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1250
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1251
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1252
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1253
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1254
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1255
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1256
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1257
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_1258
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_31J
fld public final static com.oracle.truffle.api.strings.TruffleString$Encoding Windows_874
meth public com.oracle.truffle.api.strings.TruffleString getEmpty()
meth public static com.oracle.truffle.api.strings.TruffleString$Encoding fromJCodingName(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString$Encoding valueOf(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString$Encoding[] values()
supr java.lang.Enum<com.oracle.truffle.api.strings.TruffleString$Encoding>
hfds EMPTY_STRINGS,ENCODINGS_TABLE,J_CODINGS_NAME_MAP,J_CODINGS_TABLE,MAX_COMPATIBLE_CODE_RANGE,fixedWidth,id,jCoding,maxCompatibleCodeRange,name,naturalStride

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$EqualNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$EqualNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$EqualNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static !enum com.oracle.truffle.api.strings.TruffleString$ErrorHandling
 outer com.oracle.truffle.api.strings.TruffleString
fld public final static com.oracle.truffle.api.strings.TruffleString$ErrorHandling BEST_EFFORT
fld public final static com.oracle.truffle.api.strings.TruffleString$ErrorHandling RETURN_NEGATIVE
meth public static com.oracle.truffle.api.strings.TruffleString$ErrorHandling valueOf(java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleString$ErrorHandling[] values()
supr java.lang.Enum<com.oracle.truffle.api.strings.TruffleString$ErrorHandling>
hfds errorHandler

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ForceEncodingNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ForceEncodingNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ForceEncodingNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromByteArrayNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(byte[],int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString execute(byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final com.oracle.truffle.api.strings.TruffleString execute(byte[],com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$FromByteArrayNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromByteArrayNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromCharArrayUTF16Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(char[],int,int)
meth public final com.oracle.truffle.api.strings.TruffleString execute(char[])
meth public static com.oracle.truffle.api.strings.TruffleString$FromCharArrayUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromCharArrayUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString execute(int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$FromCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromIntArrayUTF32Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(int[],int,int)
meth public final com.oracle.truffle.api.strings.TruffleString execute(int[])
meth public static com.oracle.truffle.api.strings.TruffleString$FromIntArrayUTF32Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromIntArrayUTF32Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromJavaStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(java.lang.String,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString execute(java.lang.String,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$FromJavaStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromJavaStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromLongNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(long,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$FromLongNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromLongNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$FromNativePointerNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(java.lang.Object,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$FromNativePointerNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$FromNativePointerNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetByteCodeRangeNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString$CodeRange execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetByteCodeRangeNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetByteCodeRangeNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeImpreciseNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString$CodeRange execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeImpreciseNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeImpreciseNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString$CodeRange execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetCodeRangeNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetInternalByteArrayNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.InternalByteArray execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetInternalByteArrayNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetInternalByteArrayNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetInternalNativePointerNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract java.lang.Object execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetInternalNativePointerNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetInternalNativePointerNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$GetStringCompactionLevelNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString$CompactionLevel execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$GetStringCompactionLevelNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$GetStringCompactionLevelNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$HashCodeNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$HashCodeNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$HashCodeNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static com.oracle.truffle.api.strings.TruffleString$IllegalByteArrayLengthException
 outer com.oracle.truffle.api.strings.TruffleString
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$IndexOfCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$IndexOfCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$IndexOfCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$IndexOfStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$IndexOfStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$IndexOfStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$IntIndexOfAnyIntUTF32Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,int[])
meth public static com.oracle.truffle.api.strings.TruffleString$IntIndexOfAnyIntUTF32Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$IntIndexOfAnyIntUTF32Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$IsValidNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$IsValidNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$IsValidNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$LastByteIndexOfStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$LastIndexOfCodePointNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$LastIndexOfCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$LastIndexOfCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$LastIndexOfStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$LastIndexOfStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$LastIndexOfStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$MaterializeNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract void execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$MaterializeNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$MaterializeNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static com.oracle.truffle.api.strings.TruffleString$NumberFormatException
 outer com.oracle.truffle.api.strings.TruffleString
meth public java.lang.String getMessage()
meth public java.lang.Throwable fillInStackTrace()
supr java.lang.Exception
hfds reason,regionLength,regionOffset,serialVersionUID,string
hcls Reason

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ParseDoubleNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract double execute(com.oracle.truffle.api.strings.AbstractTruffleString) throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public static com.oracle.truffle.api.strings.TruffleString$ParseDoubleNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ParseDoubleNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ParseIntNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int) throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public static com.oracle.truffle.api.strings.TruffleString$ParseIntNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ParseIntNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ParseLongNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract long execute(com.oracle.truffle.api.strings.AbstractTruffleString,int) throws com.oracle.truffle.api.strings.TruffleString$NumberFormatException
meth public static com.oracle.truffle.api.strings.TruffleString$ParseLongNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ParseLongNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ReadByteNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract int execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ReadByteNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ReadByteNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ReadCharUTF16Node
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract char execute(com.oracle.truffle.api.strings.AbstractTruffleString,int)
meth public static com.oracle.truffle.api.strings.TruffleString$ReadCharUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$ReadCharUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$RegionEqualByteIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public final boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public final boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$WithMask,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$RegionEqualByteIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$RegionEqualByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$RegionEqualNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract boolean execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$RegionEqualNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$RegionEqualNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$RepeatNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$RepeatNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$RepeatNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$SubstringByteIndexNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$SubstringByteIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$SubstringByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$SubstringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,int,int,com.oracle.truffle.api.strings.TruffleString$Encoding,boolean)
meth public static com.oracle.truffle.api.strings.TruffleString$SubstringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$SubstringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$SwitchEncodingNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding,com.oracle.truffle.api.strings.TranscodingErrorHandler)
meth public final com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$SwitchEncodingNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$SwitchEncodingNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ToJavaStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract java.lang.String execute(com.oracle.truffle.api.strings.AbstractTruffleString)
meth public static com.oracle.truffle.api.strings.TruffleString$ToJavaStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ToJavaStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$ToValidStringNode
 outer com.oracle.truffle.api.strings.TruffleString
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.AbstractTruffleString,com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$ToValidStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$ToValidStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final static com.oracle.truffle.api.strings.TruffleString$WithMask
 outer com.oracle.truffle.api.strings.TruffleString
innr public abstract static CreateNode
innr public abstract static CreateUTF16Node
innr public abstract static CreateUTF32Node
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask createUTF16Uncached(com.oracle.truffle.api.strings.AbstractTruffleString,char[])
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask createUTF32Uncached(com.oracle.truffle.api.strings.AbstractTruffleString,int[])
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask createUncached(com.oracle.truffle.api.strings.AbstractTruffleString,byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
supr java.lang.Object
hfds mask,string

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateNode
 outer com.oracle.truffle.api.strings.TruffleString$WithMask
meth public abstract com.oracle.truffle.api.strings.TruffleString$WithMask execute(com.oracle.truffle.api.strings.AbstractTruffleString,byte[],com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateNode create()
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF16Node
 outer com.oracle.truffle.api.strings.TruffleString$WithMask
meth public abstract com.oracle.truffle.api.strings.TruffleString$WithMask execute(com.oracle.truffle.api.strings.AbstractTruffleString,char[])
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF32Node
 outer com.oracle.truffle.api.strings.TruffleString$WithMask
meth public abstract com.oracle.truffle.api.strings.TruffleString$WithMask execute(com.oracle.truffle.api.strings.AbstractTruffleString,int[])
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF32Node create()
meth public static com.oracle.truffle.api.strings.TruffleString$WithMask$CreateUTF32Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract com.oracle.truffle.api.strings.TruffleStringBuilder
innr public abstract static AppendByteNode
innr public abstract static AppendCharUTF16Node
innr public abstract static AppendCodePointNode
innr public abstract static AppendIntNumberNode
innr public abstract static AppendJavaStringUTF16Node
innr public abstract static AppendLongNumberNode
innr public abstract static AppendStringNode
innr public abstract static AppendSubstringByteIndexNode
innr public abstract static ToStringNode
meth public final boolean isEmpty()
meth public final com.oracle.truffle.api.strings.TruffleString toStringUncached()
meth public final int byteLength()
meth public final java.lang.String toString()
meth public final void appendByteUncached(byte)
meth public final void appendCharUTF16Uncached(char)
meth public final void appendCodePointUncached(int)
meth public final void appendCodePointUncached(int,int)
meth public final void appendCodePointUncached(int,int,boolean)
meth public final void appendIntNumberUncached(int)
meth public final void appendJavaStringUTF16Uncached(java.lang.String)
meth public final void appendJavaStringUTF16Uncached(java.lang.String,int,int)
meth public final void appendLongNumberUncached(long)
meth public final void appendStringUncached(com.oracle.truffle.api.strings.TruffleString)
meth public final void appendSubstringByteIndexUncached(com.oracle.truffle.api.strings.TruffleString,int,int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder create(com.oracle.truffle.api.strings.TruffleString$Encoding)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder create(com.oracle.truffle.api.strings.TruffleString$Encoding,int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF16 createUTF16()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF16 createUTF16(int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF32 createUTF32()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF32 createUTF32(int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF8 createUTF8()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilderUTF8 createUTF8(int)
supr java.lang.Object
hfds DEFAULT_INITIAL_CAPACITY,buf,codePointLength,codeRange,encoding,length,stride
hcls AppendCodePointIntlNode,AppendStringIntlNode,ToStringIntlNode

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendByteNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,byte)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendByteNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendByteNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCharUTF16Node
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,char)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCharUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCharUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCodePointNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,int,int,boolean)
meth public final void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,int)
meth public final void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,int,int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCodePointNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendCodePointNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendIntNumberNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendIntNumberNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendIntNumberNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendJavaStringUTF16Node
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,java.lang.String,int,int)
meth public final void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,java.lang.String)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendJavaStringUTF16Node create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendJavaStringUTF16Node getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendLongNumberNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,long)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendLongNumberNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendLongNumberNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendStringNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,com.oracle.truffle.api.strings.AbstractTruffleString)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendSubstringByteIndexNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract void execute(com.oracle.truffle.api.strings.TruffleStringBuilder,com.oracle.truffle.api.strings.AbstractTruffleString,int,int)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendSubstringByteIndexNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$AppendSubstringByteIndexNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringBuilder$ToStringNode
 outer com.oracle.truffle.api.strings.TruffleStringBuilder
meth public abstract com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.TruffleStringBuilder,boolean)
meth public final com.oracle.truffle.api.strings.TruffleString execute(com.oracle.truffle.api.strings.TruffleStringBuilder)
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$ToStringNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringBuilder$ToStringNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final com.oracle.truffle.api.strings.TruffleStringBuilderFactory
cons public init()
supr java.lang.Object
hcls AppendByteNodeGen,AppendCharUTF16NodeGen,AppendCodePointIntlNodeGen,AppendCodePointNodeGen,AppendIntNumberNodeGen,AppendJavaStringUTF16NodeGen,AppendLongNumberNodeGen,AppendStringIntlNodeGen,AppendStringNodeGen,AppendSubstringByteIndexNodeGen,ToStringIntlNodeGen,ToStringNodeGen

CLSS public final com.oracle.truffle.api.strings.TruffleStringBuilderUTF16
supr com.oracle.truffle.api.strings.TruffleStringBuilder

CLSS public final com.oracle.truffle.api.strings.TruffleStringBuilderUTF32
supr com.oracle.truffle.api.strings.TruffleStringBuilder

CLSS public final com.oracle.truffle.api.strings.TruffleStringBuilderUTF8
supr com.oracle.truffle.api.strings.TruffleStringBuilder

CLSS public final com.oracle.truffle.api.strings.TruffleStringFactory
cons public init()
innr public final static WithMaskFactory
supr java.lang.Object
hcls AsManagedNodeGen,AsNativeNodeGen,AsTruffleStringNodeGen,ByteIndexOfAnyByteNodeGen,ByteIndexOfCodePointNodeGen,ByteIndexOfCodePointSetNodeGen,ByteIndexOfStringNodeGen,ByteIndexToCodePointIndexNodeGen,ByteLengthOfCodePointNodeGen,CharIndexOfAnyCharUTF16NodeGen,CodePointAtByteIndexNodeGen,CodePointAtIndexNodeGen,CodePointIndexToByteIndexNodeGen,CodePointLengthNodeGen,CodeRangeEqualsNodeGen,CompareBytesNodeGen,CompareCharsUTF16NodeGen,CompareIntsUTF32NodeGen,ConcatNodeGen,CopyToByteArrayNodeGen,CopyToNativeMemoryNodeGen,CreateBackwardCodePointIteratorNodeGen,CreateCodePointIteratorNodeGen,EqualNodeGen,ForceEncodingNodeGen,FromByteArrayNodeGen,FromCharArrayUTF16NodeGen,FromCodePointNodeGen,FromIntArrayUTF32NodeGen,FromJavaStringNodeGen,FromLongNodeGen,FromNativePointerNodeGen,GetByteCodeRangeNodeGen,GetCodeRangeImpreciseNodeGen,GetCodeRangeNodeGen,GetInternalByteArrayNodeGen,GetInternalNativePointerNodeGen,GetStringCompactionLevelNodeGen,HashCodeNodeGen,IndexOfCodePointNodeGen,IndexOfStringNodeGen,IntIndexOfAnyIntUTF32NodeGen,InternalAsTruffleStringNodeGen,InternalCopyToByteArrayNodeGen,InternalSwitchEncodingNodeGen,IsValidNodeGen,LastByteIndexOfCodePointNodeGen,LastByteIndexOfStringNodeGen,LastIndexOfCodePointNodeGen,LastIndexOfStringNodeGen,MaterializeNodeGen,ParseDoubleNodeGen,ParseIntNodeGen,ParseLongNodeGen,ReadByteNodeGen,ReadCharUTF16NodeGen,RegionEqualByteIndexNodeGen,RegionEqualNodeGen,RepeatNodeGen,SubstringByteIndexNodeGen,SubstringNodeGen,SwitchEncodingNodeGen,ToIndexableNodeGen,ToJavaStringNodeGen,ToValidStringNodeGen

CLSS public final static com.oracle.truffle.api.strings.TruffleStringFactory$WithMaskFactory
 outer com.oracle.truffle.api.strings.TruffleStringFactory
cons public init()
supr java.lang.Object
hcls CreateNodeGen,CreateUTF16NodeGen,CreateUTF32NodeGen

CLSS public final com.oracle.truffle.api.strings.TruffleStringIterator
innr public abstract static NextNode
innr public abstract static PreviousNode
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public int getByteIndex()
meth public int nextUncached()
meth public int previousUncached()
supr java.lang.Object
hfds a,arrayA,codeRangeA,encoding,errorHandling,rawIndex
hcls InternalNextNode,InternalPreviousNode

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringIterator$NextNode
 outer com.oracle.truffle.api.strings.TruffleStringIterator
meth public abstract int execute(com.oracle.truffle.api.strings.TruffleStringIterator)
meth public static com.oracle.truffle.api.strings.TruffleStringIterator$NextNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringIterator$NextNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract static com.oracle.truffle.api.strings.TruffleStringIterator$PreviousNode
 outer com.oracle.truffle.api.strings.TruffleStringIterator
meth public abstract int execute(com.oracle.truffle.api.strings.TruffleStringIterator)
meth public static com.oracle.truffle.api.strings.TruffleStringIterator$PreviousNode create()
meth public static com.oracle.truffle.api.strings.TruffleStringIterator$PreviousNode getUncached()
supr com.oracle.truffle.api.nodes.Node

CLSS public final com.oracle.truffle.api.strings.TruffleStringIteratorFactory
cons public init()
supr java.lang.Object
hcls InternalNextNodeGen,InternalPreviousNodeGen,NextNodeGen,PreviousNodeGen

CLSS public com.oracle.truffle.api.utilities.AssumedValue<%0 extends java.lang.Object>
cons public init(java.lang.String,{com.oracle.truffle.api.utilities.AssumedValue%0})
cons public init({com.oracle.truffle.api.utilities.AssumedValue%0})
meth public void set({com.oracle.truffle.api.utilities.AssumedValue%0})
meth public {com.oracle.truffle.api.utilities.AssumedValue%0} get()
supr java.lang.Object
hfds ASSUMPTION_UPDATER,assumption,name,value

CLSS public com.oracle.truffle.api.utilities.CyclicAssumption
cons public init(java.lang.String)
meth public com.oracle.truffle.api.Assumption getAssumption()
meth public void invalidate()
meth public void invalidate(java.lang.String)
supr java.lang.Object
hfds ASSUMPTION_UPDATER,assumption,name

CLSS public final com.oracle.truffle.api.utilities.FinalBitSet
fld public final static com.oracle.truffle.api.utilities.FinalBitSet EMPTY
meth public boolean equals(java.lang.Object)
meth public boolean get(int)
meth public boolean isEmpty()
meth public int cardinality()
meth public int hashCode()
meth public int length()
meth public int nextClearBit(int)
meth public int nextSetBit(int)
meth public int size()
meth public java.lang.String toString()
meth public long[] toLongArray()
meth public static com.oracle.truffle.api.utilities.FinalBitSet valueOf(java.util.BitSet)
meth public static com.oracle.truffle.api.utilities.FinalBitSet valueOf(long[])
supr java.lang.Object
hfds ADDRESS_BITS_PER_WORD,BITS_PER_WORD,WORD_MASK,words

CLSS public final com.oracle.truffle.api.utilities.JSONHelper
innr public abstract static JSONStringBuilder
innr public final static JSONArrayBuilder
innr public final static JSONObjectBuilder
meth public static com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder array()
meth public static com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder object()
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder
 outer com.oracle.truffle.api.utilities.JSONHelper
meth protected void appendTo(java.lang.StringBuilder)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder add(com.oracle.truffle.api.utilities.JSONHelper$JSONStringBuilder)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder add(java.lang.Boolean)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder add(java.lang.Number)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONArrayBuilder add(java.lang.String)
supr com.oracle.truffle.api.utilities.JSONHelper$JSONStringBuilder
hfds contents

CLSS public final static com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder
 outer com.oracle.truffle.api.utilities.JSONHelper
meth protected void appendTo(java.lang.StringBuilder)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder add(java.lang.String,com.oracle.truffle.api.utilities.JSONHelper$JSONStringBuilder)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder add(java.lang.String,java.lang.Boolean)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder add(java.lang.String,java.lang.Number)
meth public com.oracle.truffle.api.utilities.JSONHelper$JSONObjectBuilder add(java.lang.String,java.lang.String)
supr com.oracle.truffle.api.utilities.JSONHelper$JSONStringBuilder
hfds contents

CLSS public abstract static com.oracle.truffle.api.utilities.JSONHelper$JSONStringBuilder
 outer com.oracle.truffle.api.utilities.JSONHelper
meth protected abstract void appendTo(java.lang.StringBuilder)
meth protected static void appendValue(java.lang.StringBuilder,java.lang.Object)
meth public final java.lang.String toString()
supr java.lang.Object

CLSS public final !enum com.oracle.truffle.api.utilities.TriState
fld public final static com.oracle.truffle.api.utilities.TriState FALSE
fld public final static com.oracle.truffle.api.utilities.TriState TRUE
fld public final static com.oracle.truffle.api.utilities.TriState UNDEFINED
meth public static com.oracle.truffle.api.utilities.TriState valueOf(boolean)
meth public static com.oracle.truffle.api.utilities.TriState valueOf(java.lang.Boolean)
meth public static com.oracle.truffle.api.utilities.TriState valueOf(java.lang.String)
meth public static com.oracle.truffle.api.utilities.TriState[] values()
supr java.lang.Enum<com.oracle.truffle.api.utilities.TriState>

CLSS public final com.oracle.truffle.api.utilities.TruffleWeakReference<%0 extends java.lang.Object>
cons public init({com.oracle.truffle.api.utilities.TruffleWeakReference%0})
cons public init({com.oracle.truffle.api.utilities.TruffleWeakReference%0},java.lang.ref.ReferenceQueue<? super {com.oracle.truffle.api.utilities.TruffleWeakReference%0}>)
supr java.lang.ref.WeakReference<{com.oracle.truffle.api.utilities.TruffleWeakReference%0}>

CLSS public final com.oracle.truffle.polyglot.PolyglotImpl
cons public init()
meth protected org.graalvm.options.OptionDescriptors createEngineOptionDescriptors()
meth public !varargs boolean copyResources(java.nio.file.Path,java.lang.String[]) throws java.io.IOException
meth public !varargs org.graalvm.options.OptionDescriptors createUnionOptionDescriptors(org.graalvm.options.OptionDescriptors[])
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> java.lang.Object newTargetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>,org.graalvm.polyglot.HostAccess$TargetMappingPrecedence)
meth public boolean isDefaultProcessHandler(org.graalvm.polyglot.io.ProcessHandler)
meth public boolean isHostFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public boolean isInCurrentEngineHostCallback(java.lang.Object)
meth public boolean isInternalFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public com.oracle.truffle.api.TruffleLanguage<java.lang.Object> createHostLanguage(java.lang.Object)
meth public int getPriority()
meth public java.lang.Class<?> loadLanguageClass(java.lang.String)
meth public java.lang.Object asValue(java.lang.Object)
meth public java.lang.Object buildEngine(java.lang.String[],org.graalvm.polyglot.SandboxPolicy,java.io.OutputStream,java.io.OutputStream,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>,boolean,boolean,org.graalvm.polyglot.io.MessageTransport,java.lang.Object,java.lang.Object,boolean,boolean,java.lang.Object)
meth public java.lang.Object buildLimits(long,java.util.function.Predicate<java.lang.Object>,java.util.function.Consumer<java.lang.Object>)
meth public java.lang.Object buildSource(java.lang.String,java.lang.Object,java.net.URI,java.lang.String,java.lang.String,java.lang.Object,boolean,boolean,boolean,java.nio.charset.Charset,java.net.URL,java.lang.String) throws java.io.IOException
meth public java.lang.Object getCurrentContext()
meth public java.lang.Object initializeModuleToUnnamedAccess(java.lang.invoke.MethodHandles$Lookup,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object)
meth public java.lang.Object newIOAccess(java.lang.String,boolean,boolean,org.graalvm.polyglot.io.FileSystem)
meth public java.lang.String findLanguage(java.io.File) throws java.io.IOException
meth public java.lang.String findLanguage(java.lang.String)
meth public java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public java.lang.String findMimeType(java.io.File) throws java.io.IOException
meth public java.lang.String findMimeType(java.net.URL) throws java.io.IOException
meth public java.lang.String getTruffleVersion()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostAccess createHostAccess()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$LogHandler newLogHandler(java.lang.Object)
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$ThreadScope createThreadScope()
meth public org.graalvm.polyglot.io.ByteSequence asByteSequence(java.lang.Object)
meth public org.graalvm.polyglot.io.FileSystem allowInternalResourceAccess(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newDefaultFileSystem(java.lang.String)
meth public org.graalvm.polyglot.io.FileSystem newFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newNIOFileSystem(java.nio.file.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newReadOnlyFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.ProcessHandler newDefaultProcessHandler()
meth public void initialize()
meth public void preInitializeEngine()
meth public void resetPreInitializedEngine()
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl
hfds EMPTY_ARGS,SECRET,TRUFFLE_VERSION,contextDispatch,defaultFileSystemContext,disconnectedBigIntegerHostValue,disconnectedHostValue,engineDispatch,exceptionDispatch,executionEventDispatch,executionListenerDispatch,hostNull,instrumentDispatch,isolatePolyglot,languageDispatch,preInitializedEngineRef,primitiveValues,sourceDispatch,sourceSectionDispatch
hcls EmbedderFileSystemContext,VMObject

CLSS public com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map$Entry<{com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%0},{com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%1}>
intf java.util.function.Function<java.lang.Object,java.lang.Object>
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.Object getContext()
meth public final java.lang.Object getGuestObject()
meth public final java.lang.Object getLanguageContext()
meth public final java.lang.String toString()
meth public final {com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%0} getKey()
meth public final {com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%1} getValue()
meth public final {com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%1} setValue({com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%1})
meth public java.lang.Object apply(java.lang.Object)
meth public static boolean equals(java.lang.Object,java.lang.Object,java.lang.Object)
meth public static boolean equalsProxy(com.oracle.truffle.polyglot.PolyglotWrapper,java.lang.Object)
meth public static boolean isHostProxy(java.lang.Object)
meth public static boolean isInstance(java.lang.Object)
meth public static int hashCode(java.lang.Object,java.lang.Object)
meth public static java.lang.Object asInstance(java.lang.Object)
meth public static java.lang.Object getHostProxy(java.lang.Object)
meth public static java.lang.String toString(com.oracle.truffle.polyglot.PolyglotWrapper)
meth public static java.lang.String toString(java.lang.Object,java.lang.Object)
meth public static java.lang.String toStringImpl(java.lang.Object,java.lang.Object)
supr java.lang.Object<{com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%0},{com.oracle.truffle.polyglot.PolyglotMapEntryAndFunction%1}>

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean forRemoval()
meth public abstract !hasdefault java.lang.String since()

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
innr public final static EnumDesc
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
intf java.lang.constant.Constable
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public final java.util.Optional<java.lang.Enum$EnumDesc<{java.lang.Enum%0}>> describeConstable()
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

CLSS public java.lang.IllegalArgumentException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public java.lang.IllegalStateException
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
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract java.lang.Record
cons protected init()
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Repeatable
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> value()

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

CLSS public abstract interface java.lang.constant.Constable
meth public abstract java.util.Optional<? extends java.lang.constant.ConstantDesc> describeConstable()

CLSS public abstract java.lang.ref.Reference<%0 extends java.lang.Object>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean enqueue()
meth public boolean isEnqueued()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="16")
meth public final boolean refersTo({java.lang.ref.Reference%0})
meth public static void reachabilityFence(java.lang.Object)
meth public void clear()
meth public {java.lang.ref.Reference%0} get()
supr java.lang.Object

CLSS public java.lang.ref.WeakReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.WeakReference%0})
cons public init({java.lang.ref.WeakReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.WeakReference%0}>)
supr java.lang.ref.Reference<{java.lang.ref.WeakReference%0}>

CLSS public abstract interface java.util.function.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Object> java.util.function.Function<{%%0},{java.util.function.Function%1}> compose(java.util.function.Function<? super {%%0},? extends {java.util.function.Function%0}>)
meth public <%0 extends java.lang.Object> java.util.function.Function<{java.util.function.Function%0},{%%0}> andThen(java.util.function.Function<? super {java.util.function.Function%1},? extends {%%0}>)
meth public abstract {java.util.function.Function%1} apply({java.util.function.Function%0})
meth public static <%0 extends java.lang.Object> java.util.function.Function<{%%0},{%%0}> identity()

CLSS public abstract interface java.util.function.Predicate<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.Predicate%0})
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> and(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> negate()
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> or(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Predicate<{%%0}> isEqual(java.lang.Object)
meth public static <%0 extends java.lang.Object> java.util.function.Predicate<{%%0}> not(java.util.function.Predicate<? super {%%0}>)

CLSS public abstract interface org.graalvm.options.OptionDescriptors
fld public final static org.graalvm.options.OptionDescriptors EMPTY
intf java.lang.Iterable<org.graalvm.options.OptionDescriptor>
meth public !varargs static org.graalvm.options.OptionDescriptors createUnion(org.graalvm.options.OptionDescriptors[])
meth public abstract java.util.Iterator<org.graalvm.options.OptionDescriptor> iterator()
meth public abstract org.graalvm.options.OptionDescriptor get(java.lang.String)
meth public static org.graalvm.options.OptionDescriptors create(java.util.List<org.graalvm.options.OptionDescriptor>)

CLSS public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
innr public abstract static APIAccess
innr public abstract static AbstractContextDispatch
innr public abstract static AbstractDispatchClass
innr public abstract static AbstractEngineDispatch
innr public abstract static AbstractExceptionDispatch
innr public abstract static AbstractExecutionEventDispatch
innr public abstract static AbstractExecutionListenerDispatch
innr public abstract static AbstractHostAccess
innr public abstract static AbstractHostLanguageService
innr public abstract static AbstractInstrumentDispatch
innr public abstract static AbstractLanguageDispatch
innr public abstract static AbstractPolyglotHostService
innr public abstract static AbstractSourceDispatch
innr public abstract static AbstractSourceSectionDispatch
innr public abstract static AbstractStackFrameImpl
innr public abstract static AbstractValueDispatch
innr public abstract static IOAccessor
innr public abstract static LogHandler
innr public abstract static ManagementAccess
innr public abstract static ThreadScope
meth protected final org.graalvm.options.OptionDescriptors createAllEngineOptionDescriptors()
meth protected org.graalvm.options.OptionDescriptors createEngineOptionDescriptors()
meth public !varargs boolean copyResources(java.nio.file.Path,java.lang.String[]) throws java.io.IOException
meth public !varargs org.graalvm.options.OptionDescriptors createUnionOptionDescriptors(org.graalvm.options.OptionDescriptors[])
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> java.lang.Object newTargetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>,org.graalvm.polyglot.HostAccess$TargetMappingPrecedence)
meth public abstract int getPriority()
meth public boolean isDefaultProcessHandler(org.graalvm.polyglot.io.ProcessHandler)
meth public boolean isHostFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public boolean isInCurrentEngineHostCallback(java.lang.Object)
meth public boolean isInternalFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl getNext()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl getNextOrNull()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl getRootImpl()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess getAPIAccess()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$IOAccessor getIO()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$ManagementAccess getManagement()
meth public final void setConstructors(org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess)
meth public final void setIO(org.graalvm.polyglot.impl.AbstractPolyglotImpl$IOAccessor)
meth public final void setMonitoring(org.graalvm.polyglot.impl.AbstractPolyglotImpl$ManagementAccess)
meth public final void setNext(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public java.lang.Class<?> loadLanguageClass(java.lang.String)
meth public java.lang.Object asValue(java.lang.Object)
meth public java.lang.Object buildEngine(java.lang.String[],org.graalvm.polyglot.SandboxPolicy,java.io.OutputStream,java.io.OutputStream,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>,boolean,boolean,org.graalvm.polyglot.io.MessageTransport,java.lang.Object,java.lang.Object,boolean,boolean,java.lang.Object)
meth public java.lang.Object buildLimits(long,java.util.function.Predicate<java.lang.Object>,java.util.function.Consumer<java.lang.Object>)
meth public java.lang.Object buildSource(java.lang.String,java.lang.Object,java.net.URI,java.lang.String,java.lang.String,java.lang.Object,boolean,boolean,boolean,java.nio.charset.Charset,java.net.URL,java.lang.String) throws java.io.IOException
meth public java.lang.Object createHostAccess()
meth public java.lang.Object createHostLanguage(java.lang.Object)
meth public java.lang.Object getCurrentContext()
meth public java.lang.Object initializeModuleToUnnamedAccess(java.lang.invoke.MethodHandles$Lookup,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object)
meth public java.lang.Object newFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public java.lang.Object newIOAccess(java.lang.String,boolean,boolean,org.graalvm.polyglot.io.FileSystem)
meth public java.lang.Object newLogHandler(java.lang.Object)
meth public java.lang.String findLanguage(java.io.File) throws java.io.IOException
meth public java.lang.String findLanguage(java.lang.String)
meth public java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public java.lang.String findMimeType(java.io.File) throws java.io.IOException
meth public java.lang.String findMimeType(java.net.URL) throws java.io.IOException
meth public java.lang.String getTruffleVersion()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$ThreadScope createThreadScope()
meth public org.graalvm.polyglot.io.ByteSequence asByteSequence(java.lang.Object)
meth public org.graalvm.polyglot.io.FileSystem allowInternalResourceAccess(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newDefaultFileSystem(java.lang.String)
meth public org.graalvm.polyglot.io.FileSystem newNIOFileSystem(java.nio.file.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newReadOnlyFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.ProcessHandler newDefaultProcessHandler()
meth public void initialize()
meth public void preInitializeEngine()
meth public void resetPreInitializedEngine()
supr java.lang.Object
hfds api,io,management,next,prev

CLSS public abstract interface org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction
meth public abstract void apply(int,int[],int,java.lang.Object)

CLSS public abstract org.graalvm.shadowed.org.jcodings.CanBeTrailTableEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[],boolean[])
fld protected final boolean[] CanBeTrailTable
meth public boolean isReverseMatchAllowed(byte[],int,int)
meth public int leftAdjustCharHead(byte[],int,int,int)
supr org.graalvm.shadowed.org.jcodings.MultiByteEncoding

CLSS public final org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem
fld public final int byteLen
fld public final int[] code
fld public final static org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] EMPTY_FOLD_CODES
meth public static org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem create(int,int)
meth public static org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem create(int,int,int)
meth public static org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem create(int,int,int,int)
supr java.lang.Object

CLSS public abstract org.graalvm.shadowed.org.jcodings.CaseFoldMapEncoding
cons protected init(java.lang.String,short[],byte[],int[][])
cons protected init(java.lang.String,short[],byte[],int[][],boolean)
fld protected final boolean foldFlag
fld protected final int[][] CaseFoldMap
meth protected final int applyAllCaseFoldWithMap(int,int[][],boolean,int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth protected final org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] getCaseFoldCodesByStringWithMap(int,int[][],boolean,int,byte[],int,int)
meth public boolean isCodeCType(int,int)
meth public org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.graalvm.shadowed.org.jcodings.SingleByteEncoding
hfds SS

CLSS public final org.graalvm.shadowed.org.jcodings.CodeRange
cons public init()
meth public static boolean isInCodeRange(int[],int)
meth public static boolean isInCodeRange(int[],int,int)
supr java.lang.Object

CLSS public abstract interface org.graalvm.shadowed.org.jcodings.Config
fld public final static boolean USE_CRNL_AS_LINE_TERMINATOR = false
fld public final static boolean USE_UNICODE_ALL_LINE_TERMINATORS = false
fld public final static boolean USE_UNICODE_CASE_FOLD_TURKISH_AZERI = false
fld public final static boolean USE_UNICODE_PROPERTIES = true
fld public final static int CASE_ASCII_ONLY = 4194304
fld public final static int CASE_DOWNCASE = 16384
fld public final static int CASE_DOWN_SPECIAL = 131072
fld public final static int CASE_FOLD = 524288
fld public final static int CASE_FOLD_LITHUANIAN = 2097152
fld public final static int CASE_FOLD_TURKISH_AZERI = 1048576
fld public final static int CASE_IS_TITLECASE = 8388608
fld public final static int CASE_MODIFIED = 262144
fld public final static int CASE_SPECIALS = 8617984
fld public final static int CASE_SPECIAL_OFFSET = 3
fld public final static int CASE_TITLECASE = 32768
fld public final static int CASE_UPCASE = 8192
fld public final static int CASE_UP_SPECIAL = 65536
fld public final static int CodePointMask = 7
fld public final static int CodePointMaskWidth = 3
fld public final static int ENC_CASE_FOLD_DEFAULT = 1073741824
fld public final static int ENC_CASE_FOLD_MIN = 1073741824
fld public final static int ENC_CODE_TO_MBC_MAXLEN = 7
fld public final static int ENC_GET_CASE_FOLD_CODES_MAX_NUM = 13
fld public final static int ENC_MAX_COMP_CASE_FOLD_CODE_LEN = 3
fld public final static int ENC_MBC_CASE_FOLD_MAXLEN = 18
fld public final static int INTERNAL_ENC_CASE_FOLD_MULTI_CHAR = 1073741824
fld public final static int SpecialIndexMask = 8184
fld public final static int SpecialIndexShift = 3
fld public final static int SpecialIndexWidth = 10
fld public final static int SpecialsLengthOffset = 25
fld public final static int UNICODE_EMOJI_VERSION_MAJOR = 13
fld public final static int UNICODE_EMOJI_VERSION_MINOR = 1
fld public final static int UNICODE_VERSION_MAJOR = 13
fld public final static int UNICODE_VERSION_MINOR = 0
fld public final static int UNICODE_VERSION_TEENY = 0
fld public final static java.lang.String UNICODE_EMOJI_VERSION_STRING = "13.1"
fld public final static java.lang.String UNICODE_VERSION_STRING = "13.0.0"

CLSS public abstract org.graalvm.shadowed.org.jcodings.Encoding
cons protected init(java.lang.String,int,int)
fld protected boolean isUTF8
fld protected boolean isUnicode
fld protected final int maxLength
fld protected final int minLength
fld public final static byte NEW_LINE = 10
fld public final static int CHAR_INVALID = -1
intf java.lang.Cloneable
meth protected final void setDummy()
meth protected final void setName(byte[])
meth protected final void setName(java.lang.String)
meth public abstract boolean isCodeCType(int,int)
meth public abstract boolean isNewLine(byte[],int,int)
meth public abstract boolean isReverseMatchAllowed(byte[],int,int)
meth public abstract int caseMap(org.graalvm.shadowed.org.jcodings.IntHolder,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[],int,int)
meth public abstract int codeToMbc(int,byte[],int)
meth public abstract int codeToMbcLength(int)
meth public abstract int leftAdjustCharHead(byte[],int,int,int)
meth public abstract int length(byte)
meth public abstract int length(byte[],int,int)
meth public abstract int mbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth public abstract int mbcToCode(byte[],int,int)
meth public abstract int propertyNameToCType(byte[],int,int)
meth public abstract int strCodeAt(byte[],int,int,int)
meth public abstract int strLength(byte[],int,int)
meth public abstract int[] ctypeCodeRange(int,org.graalvm.shadowed.org.jcodings.IntHolder)
meth public abstract org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public abstract void applyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isMbcCrnl(byte[],int,int)
meth public byte[] toLowerCaseTable()
meth public final boolean equals(java.lang.Object)
meth public final boolean isAlnum(int)
meth public final boolean isAlpha(int)
meth public final boolean isAsciiCompatible()
meth public final boolean isBlank(int)
meth public final boolean isCntrl(int)
meth public final boolean isDigit(int)
meth public final boolean isDummy()
meth public final boolean isFixedWidth()
meth public final boolean isGraph(int)
meth public final boolean isLower(int)
meth public final boolean isMbcHead(byte[],int,int)
meth public final boolean isMbcWord(byte[],int,int)
meth public final boolean isNewLine(int)
meth public final boolean isPrint(int)
meth public final boolean isPunct(int)
meth public final boolean isSbWord(int)
meth public final boolean isSingleByte()
meth public final boolean isSpace(int)
meth public final boolean isUTF8()
meth public final boolean isUnicode()
meth public final boolean isUpper(int)
meth public final boolean isWord(int)
meth public final boolean isXDigit(int)
meth public final byte[] getName()
meth public final int getIndex()
meth public final int hashCode()
meth public final int maxLength()
meth public final int maxLengthDistance()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final int mbcodeStartPosition()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final int minLength()
meth public final int prevCharHead(byte[],int,int,int)
meth public final int rightAdjustCharHead(byte[],int,int,int)
meth public final int rightAdjustCharHeadWithPrev(byte[],int,int,int,org.graalvm.shadowed.org.jcodings.IntHolder)
meth public final int step(byte[],int,int,int)
meth public final int stepBack(byte[],int,int,int,int)
meth public final int strByteLengthNull(byte[],int,int)
meth public final int strLengthNull(byte[],int,int)
meth public final int strNCmp(byte[],int,int,byte[],int,int)
meth public final int xdigitVal(int)
meth public final java.lang.String toString()
meth public java.lang.String getCharsetName()
meth public java.nio.charset.Charset getCharset()
meth public static boolean isAscii(byte)
meth public static boolean isAscii(int)
meth public static boolean isMbcAscii(byte)
meth public static boolean isWordGraphPrint(int)
meth public static byte asciiToLower(int)
meth public static byte asciiToUpper(int)
meth public static int digitVal(int)
meth public static int odigitVal(int)
meth public static org.graalvm.shadowed.org.jcodings.Encoding load(java.lang.String)
supr java.lang.Object
hfds charset,count,hashCode,index,isAsciiCompatible,isDummy,isFixedWidth,isSingleByte,name,stringName

CLSS public final org.graalvm.shadowed.org.jcodings.EncodingDB
cons public init()
innr public final static Entry
meth public final static org.graalvm.shadowed.org.jcodings.util.CaseInsensitiveBytesHash<org.graalvm.shadowed.org.jcodings.EncodingDB$Entry> getAliases()
meth public final static org.graalvm.shadowed.org.jcodings.util.CaseInsensitiveBytesHash<org.graalvm.shadowed.org.jcodings.EncodingDB$Entry> getEncodings()
meth public static org.graalvm.shadowed.org.jcodings.EncodingDB$Entry dummy(byte[])
meth public static void alias(java.lang.String,java.lang.String)
meth public static void declare(java.lang.String,java.lang.String)
meth public static void dummy(java.lang.String)
meth public static void dummy_unicode(java.lang.String)
meth public static void replicate(java.lang.String,java.lang.String)
meth public static void set_base(java.lang.String,java.lang.String)
supr java.lang.Object
hfds aliases,ascii,encodings

CLSS public final static org.graalvm.shadowed.org.jcodings.EncodingDB$Entry
 outer org.graalvm.shadowed.org.jcodings.EncodingDB
meth public boolean isDummy()
meth public int getIndex()
meth public int hashCode()
meth public java.lang.String getEncodingClass()
meth public org.graalvm.shadowed.org.jcodings.Encoding getEncoding()
meth public org.graalvm.shadowed.org.jcodings.EncodingDB$Entry getBase()
supr java.lang.Object
hfds base,count,encoding,encodingClass,index,isDummy,name

CLSS public abstract org.graalvm.shadowed.org.jcodings.EucEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[])
meth protected abstract boolean isLead(int)
meth public int leftAdjustCharHead(byte[],int,int,int)
supr org.graalvm.shadowed.org.jcodings.MultiByteEncoding

CLSS public abstract org.graalvm.shadowed.org.jcodings.ISOEncoding
cons protected init(java.lang.String,short[],byte[],int[][])
cons protected init(java.lang.String,short[],byte[],int[][],boolean)
fld public static int SHARP_s
meth public boolean isCodeCType(int,int)
meth public int mbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth public java.lang.String getCharsetName()
supr org.graalvm.shadowed.org.jcodings.CaseFoldMapEncoding

CLSS public org.graalvm.shadowed.org.jcodings.IntHolder
cons public init()
fld public int value
supr java.lang.Object

CLSS public abstract org.graalvm.shadowed.org.jcodings.MultiByteEncoding
cons protected init(java.lang.String,int,int,int[],int[][],short[])
fld protected final int[] EncLen
fld protected final int[] TransZero
fld protected final int[][] Trans
fld protected final static int A = -1
fld protected final static int F = -2
meth protected final boolean isCodeCTypeInternal(int,int)
meth protected final boolean mb2IsCodeCType(int,int)
meth protected final boolean mb4IsCodeCType(int,int)
meth protected final int asciiMbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth protected final int lengthForTwoUptoFour(byte[],int,int,int,int)
meth protected final int mb2CodeToMbc(int,byte[],int)
meth protected final int mb2CodeToMbcLength(int)
meth protected final int mb4CodeToMbc(int,byte[],int)
meth protected final int mb4CodeToMbcLength(int)
meth protected final int mbnMbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth protected final int mbnMbcToCode(byte[],int,int)
meth protected final int missing(int)
meth protected final int missing(int,int)
meth protected final int safeLengthForUptoFour(byte[],int,int)
meth protected final int safeLengthForUptoThree(byte[],int,int)
meth protected final int safeLengthForUptoTwo(byte[],int,int)
meth protected final org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] asciiCaseFoldCodesByString(int,byte[],int,int)
meth protected final void asciiApplyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isNewLine(byte[],int,int)
meth public int caseMap(org.graalvm.shadowed.org.jcodings.IntHolder,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[],int,int)
meth public int length(byte)
meth public int mbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth public int propertyNameToCType(byte[],int,int)
meth public int strCodeAt(byte[],int,int,int)
meth public int strLength(byte[],int,int)
meth public org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public static boolean isInRange(int,int,int)
meth public void applyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.graalvm.shadowed.org.jcodings.Encoding

CLSS public final org.graalvm.shadowed.org.jcodings.ObjPtr<%0 extends java.lang.Object>
cons public init()
cons public init({org.graalvm.shadowed.org.jcodings.ObjPtr%0})
fld public {org.graalvm.shadowed.org.jcodings.ObjPtr%0} p
supr java.lang.Object
hfds NULL

CLSS public final org.graalvm.shadowed.org.jcodings.Ptr
cons public init()
cons public init(int)
fld public final static org.graalvm.shadowed.org.jcodings.Ptr NULL
fld public int p
supr java.lang.Object

CLSS public abstract org.graalvm.shadowed.org.jcodings.SingleByteEncoding
cons protected init(java.lang.String,short[],byte[])
fld protected final byte[] LowerCaseTable
fld public final static int MAX_BYTE = 255
meth protected final boolean isCodeCTypeInternal(int,int)
meth protected final int asciiMbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth protected final org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] asciiCaseFoldCodesByString(int,byte[],int,int)
meth protected final void asciiApplyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
meth public boolean isNewLine(byte[],int,int)
meth public final boolean isReverseMatchAllowed(byte[],int,int)
meth public final int codeToMbc(int,byte[],int)
meth public final int leftAdjustCharHead(byte[],int,int,int)
meth public final int strLength(byte[],int,int)
meth public final int[] ctypeCodeRange(int,org.graalvm.shadowed.org.jcodings.IntHolder)
meth public int caseMap(org.graalvm.shadowed.org.jcodings.IntHolder,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[],int,int)
meth public int codeToMbcLength(int)
meth public int length(byte)
meth public int length(byte[],int,int)
meth public int mbcCaseFold(int,byte[],org.graalvm.shadowed.org.jcodings.IntHolder,int,byte[])
meth public int mbcToCode(byte[],int,int)
meth public int propertyNameToCType(byte[],int,int)
meth public int strCodeAt(byte[],int,int,int)
meth public org.graalvm.shadowed.org.jcodings.CaseFoldCodeItem[] caseFoldCodesByString(int,byte[],int,int)
meth public void applyAllCaseFold(int,org.graalvm.shadowed.org.jcodings.ApplyAllCaseFoldFunction,java.lang.Object)
supr org.graalvm.shadowed.org.jcodings.Encoding

