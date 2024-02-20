#Signature file v4.1
#Version 1.23

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

CLSS public abstract interface com.oracle.truffle.api.Assumption
meth public abstract boolean isValid()
meth public abstract java.lang.String getName()
meth public abstract void check() throws com.oracle.truffle.api.nodes.InvalidAssumptionException
meth public abstract void invalidate()
meth public static boolean isValidAssumption(com.oracle.truffle.api.Assumption)
meth public static boolean isValidAssumption(com.oracle.truffle.api.Assumption[])
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
meth public static boolean inCompilationRoot()
meth public static boolean inCompiledCode()
meth public static boolean inInterpreter()
meth public static boolean injectBranchProbability(double,boolean)
meth public static boolean isCompilationConstant(java.lang.Object)
meth public static boolean isPartialEvaluationConstant(java.lang.Object)
meth public static java.lang.RuntimeException shouldNotReachHere()
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.String)
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.String,java.lang.Throwable)
meth public static java.lang.RuntimeException shouldNotReachHere(java.lang.Throwable)
meth public static void bailout(java.lang.String)
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

CLSS public abstract interface com.oracle.truffle.api.CompilerOptions
meth public abstract boolean supportsOption(java.lang.String)
meth public abstract void setOption(java.lang.String,java.lang.Object)

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
meth public static int multiplyHigh(int,int)
meth public static int multiplyHighUnsigned(int,int)
meth public static long multiplyHigh(long,long)
meth public static long multiplyHighUnsigned(long,long)
supr java.lang.Object

CLSS public final com.oracle.truffle.api.InstrumentInfo
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
supr java.lang.Object
hfds id,name,polyglotInstrument,version

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
meth public abstract !hasdefault org.graalvm.options.OptionStability stability()
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

CLSS public final com.oracle.truffle.api.Scope
 anno 0 java.lang.Deprecated()
innr public final Builder
meth public com.oracle.truffle.api.nodes.Node getNode()
meth public java.lang.Object getArguments()
meth public java.lang.Object getReceiver()
meth public java.lang.Object getRootInstance()
meth public java.lang.Object getVariables()
meth public java.lang.String getName()
meth public java.lang.String getReceiverName()
meth public static com.oracle.truffle.api.Scope$Builder newBuilder(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds EMPTY,arguments,name,node,receiver,receiverName,rootInstance,variables

CLSS public final com.oracle.truffle.api.Scope$Builder
 outer com.oracle.truffle.api.Scope
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.Scope build()
meth public com.oracle.truffle.api.Scope$Builder arguments(java.lang.Object)
meth public com.oracle.truffle.api.Scope$Builder node(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.Scope$Builder receiver(java.lang.String,java.lang.Object)
meth public com.oracle.truffle.api.Scope$Builder rootInstance(java.lang.Object)
supr java.lang.Object
hfds arguments,name,node,receiver,receiverName,rootInstance,variables

CLSS public final com.oracle.truffle.api.Truffle
meth public static com.oracle.truffle.api.TruffleRuntime getRuntime()
supr java.lang.Object
hfds RUNTIME

CLSS public final com.oracle.truffle.api.TruffleContext
innr public final Builder
intf java.lang.AutoCloseable
meth public boolean equals(java.lang.Object)
meth public boolean isActive()
meth public boolean isClosed()
meth public boolean isEntered()
meth public com.oracle.truffle.api.TruffleContext getParent()
meth public int hashCode()
meth public java.lang.Object enter()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object enter(com.oracle.truffle.api.nodes.Node)
meth public void close()
meth public void closeCancelled(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public void closeResourceExhausted(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public void leave(com.oracle.truffle.api.nodes.Node,java.lang.Object)
meth public void leave(java.lang.Object)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds CONTEXT_ASSERT_STACK,EMPTY,closeable,polyglotContext

CLSS public final com.oracle.truffle.api.TruffleContext$Builder
 outer com.oracle.truffle.api.TruffleContext
meth public com.oracle.truffle.api.TruffleContext build()
meth public com.oracle.truffle.api.TruffleContext$Builder config(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds config,sourceEnvironment

CLSS public abstract interface com.oracle.truffle.api.TruffleException
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.nodes.Node getLocation()
 anno 0 java.lang.Deprecated()
meth public boolean isCancelled()
 anno 0 java.lang.Deprecated()
meth public boolean isExit()
 anno 0 java.lang.Deprecated()
meth public boolean isIncompleteSource()
 anno 0 java.lang.Deprecated()
meth public boolean isInternalError()
 anno 0 java.lang.Deprecated()
meth public boolean isSyntaxError()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.source.SourceSection getSourceLocation()
 anno 0 java.lang.Deprecated()
meth public int getExitStatus()
 anno 0 java.lang.Deprecated()
meth public int getStackTraceElementLimit()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getExceptionObject()
 anno 0 java.lang.Deprecated()

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
 anno 0 java.lang.Deprecated()
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
innr protected abstract interface static ContextLocalFactory
innr protected abstract interface static ContextThreadLocalFactory
innr public abstract interface static !annotation Registration
innr public abstract interface static Provider
innr public abstract static ContextReference
innr public abstract static LanguageReference
innr public final static !enum ContextPolicy
innr public final static Env
innr public final static InlineParsingRequest
innr public final static ParsingRequest
meth protected abstract {com.oracle.truffle.api.TruffleLanguage%0} createContext(com.oracle.truffle.api.TruffleLanguage$Env)
meth protected boolean areOptionsCompatible(org.graalvm.options.OptionValues,org.graalvm.options.OptionValues)
meth protected boolean initializeMultiContext()
 anno 0 java.lang.Deprecated()
meth protected boolean isObjectOfLanguage(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected boolean isThreadAccessAllowed(java.lang.Thread,boolean)
meth protected boolean isVisible({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected boolean patchContext({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.TruffleLanguage$Env)
meth protected com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.TruffleLanguage$ParsingRequest) throws java.lang.Exception
meth protected com.oracle.truffle.api.nodes.ExecutableNode parse(com.oracle.truffle.api.TruffleLanguage$InlineParsingRequest) throws java.lang.Exception
meth protected com.oracle.truffle.api.source.SourceSection findSourceLocation({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<{com.oracle.truffle.api.TruffleLanguage%0},{%%0}>)
meth protected final int getAsynchronousStackDepth()
meth protected final java.lang.String getLanguageHome()
meth protected java.lang.Iterable<com.oracle.truffle.api.Scope> findLocalScopes({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.Frame)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Iterable<com.oracle.truffle.api.Scope> findTopScopes({com.oracle.truffle.api.TruffleLanguage%0})
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object findExportedSymbol({com.oracle.truffle.api.TruffleLanguage%0},java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object findMetaObject({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getLanguageGlobal({com.oracle.truffle.api.TruffleLanguage%0})
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object getLanguageView({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
meth protected java.lang.Object getScope({com.oracle.truffle.api.TruffleLanguage%0})
meth protected java.lang.Object getScopedView({com.oracle.truffle.api.TruffleLanguage%0},com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.Frame,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected java.lang.String toString({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected static <%0 extends com.oracle.truffle.api.TruffleLanguage<?>> {%%0} getCurrentLanguage(java.lang.Class<{%%0}>)
meth protected static <%0 extends java.lang.Object, %1 extends com.oracle.truffle.api.TruffleLanguage<{%%0}>> {%%0} getCurrentContext(java.lang.Class<{%%1}>)
meth protected void disposeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void disposeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth protected void finalizeContext({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void initializeContext({com.oracle.truffle.api.TruffleLanguage%0}) throws java.lang.Exception
meth protected void initializeMultiThreading({com.oracle.truffle.api.TruffleLanguage%0})
meth protected void initializeMultipleContexts()
meth protected void initializeThread({com.oracle.truffle.api.TruffleLanguage%0},java.lang.Thread)
meth public final com.oracle.truffle.api.TruffleLanguage$ContextReference<{com.oracle.truffle.api.TruffleLanguage%0}> getContextReference()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds contextLocals,contextThreadLocals,languageInfo,polyglotLanguageInstance,reference

CLSS protected abstract interface static com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory%1} create({com.oracle.truffle.api.TruffleLanguage$ContextLocalFactory%0})

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
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextReference%0} get()
supr java.lang.Object

CLSS protected abstract interface static com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer com.oracle.truffle.api.TruffleLanguage
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory%1} create({com.oracle.truffle.api.TruffleLanguage$ContextThreadLocalFactory%0},java.lang.Thread)

CLSS public final static com.oracle.truffle.api.TruffleLanguage$Env
 outer com.oracle.truffle.api.TruffleLanguage
meth public !varargs com.oracle.truffle.api.CallTarget parse(com.oracle.truffle.api.source.Source,java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public !varargs com.oracle.truffle.api.CallTarget parseInternal(com.oracle.truffle.api.source.Source,java.lang.String[])
meth public !varargs com.oracle.truffle.api.CallTarget parsePublic(com.oracle.truffle.api.source.Source,java.lang.String[])
meth public !varargs com.oracle.truffle.api.TruffleFile createTempDirectory(com.oracle.truffle.api.TruffleFile,java.lang.String,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.TruffleFile createTempFile(com.oracle.truffle.api.TruffleFile,java.lang.String,java.lang.String,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs com.oracle.truffle.api.io.TruffleProcessBuilder newProcessBuilder(java.lang.String[])
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.InstrumentInfo,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public boolean initializeLanguage(com.oracle.truffle.api.nodes.LanguageInfo)
meth public boolean isCreateProcessAllowed()
meth public boolean isCreateThreadAllowed()
meth public boolean isHostException(java.lang.Throwable)
meth public boolean isHostFunction(java.lang.Object)
meth public boolean isHostLookupAllowed()
meth public boolean isHostObject(java.lang.Object)
meth public boolean isHostSymbol(java.lang.Object)
meth public boolean isMimeTypeSupported(java.lang.String)
meth public boolean isNativeAccessAllowed()
meth public boolean isPolyglotAccessAllowed()
 anno 0 java.lang.Deprecated()
meth public boolean isPolyglotBindingsAccessAllowed()
meth public boolean isPolyglotEvalAllowed()
meth public boolean isPreInitialization()
meth public com.oracle.truffle.api.TruffleContext getContext()
meth public com.oracle.truffle.api.TruffleContext$Builder newContextBuilder()
meth public com.oracle.truffle.api.TruffleFile getCurrentWorkingDirectory()
meth public com.oracle.truffle.api.TruffleFile getInternalTruffleFile(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getInternalTruffleFile(java.net.URI)
meth public com.oracle.truffle.api.TruffleFile getPublicTruffleFile(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getPublicTruffleFile(java.net.URI)
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.net.URI)
 anno 0 java.lang.Deprecated()
meth public java.io.InputStream in()
meth public java.io.OutputStream err()
meth public java.io.OutputStream out()
meth public java.lang.Object asBoxedGuestValue(java.lang.Object)
meth public java.lang.Object asGuestValue(java.lang.Object)
meth public java.lang.Object asHostObject(java.lang.Object)
meth public java.lang.Object asHostSymbol(java.lang.Class<?>)
meth public java.lang.Object createHostAdapterClass(java.lang.Class<?>[])
meth public java.lang.Object createHostAdapterClassWithStaticOverrides(java.lang.Class<?>[],java.lang.Object)
meth public java.lang.Object findMetaObject(java.lang.Object)
meth public java.lang.Object getPolyglotBindings()
meth public java.lang.Object importSymbol(java.lang.String)
meth public java.lang.Object lookupHostSymbol(java.lang.String)
meth public java.lang.String getFileNameSeparator()
meth public java.lang.String getPathSeparator()
meth public java.lang.String[] getApplicationArguments()
meth public java.lang.Thread createThread(java.lang.Runnable)
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext)
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext,java.lang.ThreadGroup)
meth public java.lang.Thread createThread(java.lang.Runnable,com.oracle.truffle.api.TruffleContext,java.lang.ThreadGroup,long)
meth public java.lang.Throwable asHostException(java.lang.Throwable)
meth public java.time.ZoneId getTimeZone()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.InstrumentInfo> getInstruments()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getInternalLanguages()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getLanguages()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getPublicLanguages()
meth public java.util.Map<java.lang.String,java.lang.Object> getConfig()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvironment()
meth public org.graalvm.options.OptionValues getOptions()
meth public void addToHostClassPath(com.oracle.truffle.api.TruffleFile)
meth public void exportSymbol(java.lang.String,java.lang.Object)
meth public void registerService(java.lang.Object)
meth public void setCurrentWorkingDirectory(com.oracle.truffle.api.TruffleFile)
supr java.lang.Object
hfds UNSET_CONTEXT,applicationArguments,config,context,contextUnchangedAssumption,err,in,initialized,initializedUnchangedAssumption,languageServicesCollector,options,out,polyglotLanguageContext,services,spi,valid

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
meth public abstract {com.oracle.truffle.api.TruffleLanguage$LanguageReference%0} get()
supr java.lang.Object

CLSS public final static com.oracle.truffle.api.TruffleLanguage$ParsingRequest
 outer com.oracle.truffle.api.TruffleLanguage
meth public com.oracle.truffle.api.source.Source getSource()
meth public java.util.List<java.lang.String> getArgumentNames()
supr java.lang.Object
hfds argumentNames,disposed,source

CLSS public abstract interface static com.oracle.truffle.api.TruffleLanguage$Provider
 outer com.oracle.truffle.api.TruffleLanguage
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
meth public abstract !hasdefault com.oracle.truffle.api.TruffleLanguage$ContextPolicy contextPolicy()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.TruffleFile$FileTypeDetector>[] fileTypeDetectors()
meth public abstract !hasdefault java.lang.Class<?>[] services()
meth public abstract !hasdefault java.lang.String defaultMimeType()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String implementationName()
meth public abstract !hasdefault java.lang.String version()
meth public abstract !hasdefault java.lang.String[] byteMimeTypes()
meth public abstract !hasdefault java.lang.String[] characterMimeTypes()
meth public abstract !hasdefault java.lang.String[] dependentLanguages()
meth public abstract !hasdefault java.lang.String[] mimeType()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String name()

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

CLSS public final com.oracle.truffle.api.TruffleOptions
fld public final static boolean AOT
fld public final static boolean DetailedRewriteReasons
fld public final static boolean TraceRewrites
fld public final static com.oracle.truffle.api.nodes.NodeCost TraceRewritesFilterFromCost
fld public final static com.oracle.truffle.api.nodes.NodeCost TraceRewritesFilterToCost
fld public final static java.lang.String TraceRewritesFilterClass
supr java.lang.Object

CLSS public abstract interface com.oracle.truffle.api.TruffleRuntime
meth public abstract <%0 extends java.lang.Object> {%%0} getCapability(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} iterateFrames(com.oracle.truffle.api.frame.FrameInstanceVisitor<{%%0}>)
meth public abstract boolean isProfilingEnabled()
meth public abstract com.oracle.truffle.api.Assumption createAssumption()
meth public abstract com.oracle.truffle.api.Assumption createAssumption(java.lang.String)
meth public abstract com.oracle.truffle.api.CompilerOptions createCompilerOptions()
meth public abstract com.oracle.truffle.api.RootCallTarget createCallTarget(com.oracle.truffle.api.nodes.RootNode)
meth public abstract com.oracle.truffle.api.frame.FrameInstance getCallerFrame()
meth public abstract com.oracle.truffle.api.frame.FrameInstance getCurrentFrame()
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

CLSS public final com.oracle.truffle.api.TruffleStackTrace
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public static com.oracle.truffle.api.TruffleStackTrace fillIn(java.lang.Throwable)
meth public static java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> getAsynchronousStackTrace(com.oracle.truffle.api.CallTarget,com.oracle.truffle.api.frame.Frame)
meth public static java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> getStackTrace(java.lang.Throwable)
supr java.lang.Exception
hfds EMPTY,UNSAFE,causeFieldIndex,frames,lazyFrames,materializedHostException
hcls LazyStackTrace,TracebackElement

CLSS public final com.oracle.truffle.api.TruffleStackTraceElement
meth public com.oracle.truffle.api.RootCallTarget getTarget()
meth public com.oracle.truffle.api.frame.Frame getFrame()
meth public com.oracle.truffle.api.nodes.Node getLocation()
meth public java.lang.Object getGuestObject()
meth public static com.oracle.truffle.api.TruffleStackTraceElement create(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.RootCallTarget,com.oracle.truffle.api.frame.Frame)
supr java.lang.Object
hfds frame,location,target

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
hfds BUILDER_INSTANCE,breakpointBinding,breakpointBindingAttaching,breakpointBindingReady,condition,conditionExistsUnchanged,conditionUnchanged,debugger,disposed,enabled,exceptionFilter,global,hitCount,ignoreCount,locationKey,oneShot,resolveListener,resolved,roWrapper,rootInstanceRef,sessions,sessionsUnchanged,sourceBinding,sourcePredicate,suspendAnchor
hcls AbstractBreakpointNode,BreakpointAfterNode,BreakpointAfterNodeException,BreakpointBeforeNode,BreakpointConditionFailure,BreakpointNodeFactory,ConditionalBreakNode,GlobalBreakpoint,SessionList

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
hfds CAUSE_CAPTION,catchLocation,debugAsyncStacks,debugStackTrace,exception,isCatchNodeComputed,javaLikeStackTrace,preferredLanguage,serialVersionUID,session,suspendedEvent,throwLocation

CLSS public final static com.oracle.truffle.api.debug.DebugException$CatchLocation
 outer com.oracle.truffle.api.debug.DebugException
meth public com.oracle.truffle.api.debug.DebugStackFrame getFrame()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
supr java.lang.Object
hfds depth,frame,frameInstance,section,session

CLSS public final com.oracle.truffle.api.debug.DebugScope
meth public boolean isFunctionScope()
meth public com.oracle.truffle.api.debug.DebugScope getParent()
meth public com.oracle.truffle.api.debug.DebugValue getDeclaredValue(java.lang.String)
meth public com.oracle.truffle.api.debug.DebugValue getReceiver()
meth public com.oracle.truffle.api.debug.DebugValue getRootInstance()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public java.lang.Iterable<com.oracle.truffle.api.debug.DebugValue> getArguments()
 anno 0 java.lang.Deprecated()
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
hfds currentFrame,depth,event,hostTraceElement

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
 anno 0 java.lang.Deprecated()
meth public abstract boolean hasReadSideEffects()
meth public abstract boolean hasWriteSideEffects()
meth public abstract boolean isInternal()
meth public abstract boolean isReadable()
meth public abstract boolean isWritable()
meth public abstract java.lang.String getName()
meth public abstract void set(com.oracle.truffle.api.debug.DebugValue)
meth public abstract void set(java.lang.Object)
meth public boolean asBoolean()
meth public boolean fitsInByte()
meth public boolean fitsInDouble()
meth public boolean fitsInFloat()
meth public boolean fitsInInt()
meth public boolean fitsInLong()
meth public boolean fitsInShort()
meth public boolean isBoolean()
meth public boolean isDate()
meth public boolean isDuration()
meth public boolean isInstant()
meth public boolean isMetaInstance(com.oracle.truffle.api.debug.DebugValue)
meth public boolean isMetaObject()
meth public boolean isNumber()
meth public boolean isString()
meth public boolean isTime()
meth public boolean isTimeZone()
meth public byte asByte()
meth public com.oracle.truffle.api.debug.DebugScope getScope()
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
meth public final java.util.List<com.oracle.truffle.api.debug.DebugValue> getArray()
meth public float asFloat()
meth public int asInt()
meth public java.lang.Object getRawValue(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public java.lang.String getMetaQualifiedName()
meth public java.lang.String getMetaSimpleName()
meth public java.lang.String toString()
meth public java.time.Duration asDuration()
meth public java.time.Instant asInstant()
meth public java.time.LocalDate asDate()
meth public java.time.LocalTime asTime()
meth public java.time.ZoneId asTimeZone()
meth public long asLong()
meth public short asShort()
supr java.lang.Object
hfds INTEROP,preferredLanguage
hcls AbstractDebugCachedValue,AbstractDebugValue,ArrayElementValue,HeapValue,ObjectMemberValue

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
meth public void removeBreakpointAddedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
meth public void removeBreakpointRemovedListener(java.util.function.Consumer<com.oracle.truffle.api.debug.Breakpoint>)
supr java.lang.Object
hfds ACCESSOR,TRACE,alwaysHaltBreakpoint,breakpointAddedListeners,breakpointRemovedListeners,breakpoints,env,propSupport,sessions
hcls AccessorDebug

CLSS public final com.oracle.truffle.api.debug.DebuggerSession
intf java.io.Closeable
meth public boolean isBreakpointsActive()
 anno 0 java.lang.Deprecated()
meth public boolean isBreakpointsActive(com.oracle.truffle.api.debug.Breakpoint$Kind)
meth public boolean suspendHere(com.oracle.truffle.api.nodes.Node)
meth public com.oracle.truffle.api.debug.Breakpoint install(com.oracle.truffle.api.debug.Breakpoint)
meth public com.oracle.truffle.api.debug.DebugScope getTopScope(java.lang.String)
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
 anno 0 java.lang.Deprecated()
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
hfds ANCHOR_SET_AFTER,ANCHOR_SET_ALL,ANCHOR_SET_BEFORE,SESSIONS,allBindings,alwaysHaltBreakpointsActive,breakpoints,breakpointsUnresolved,breakpointsUnresolvedEmpty,callback,closed,currentSuspendedEventMap,debugger,exceptionBreakpointsActive,executionLifecycle,hasExpressionElement,hasRootElement,ignoreLanguageContextInitialization,inEvalInContext,includeInternal,locationBreakpointsActive,sessionId,showHostStackFrames,sourceElements,sourceFilter,sources,stepping,strategyMap,suspendAll,suspendNext,suspensionFilterUnchanged,syntaxElementsBinding,threadSuspensions
hcls Caller,RootSteppingDepthNode,StableBoolean,SteppingNode,ThreadSuspension

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
meth public abstract !hasdefault boolean weak()
meth public abstract !hasdefault int dimensions()
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
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.CachedContext
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage> value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.CachedLanguage
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.CreateCast
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Executed
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] with()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Fallback
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateNodeFactory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GenerateUncached
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean inherit()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.GeneratedBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String methodName()
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.ImplicitCast
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.ImportStatic
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] value()

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.Introspectable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public final com.oracle.truffle.api.dsl.Introspection
innr public abstract interface static Provider
innr public final static SpecializationInfo
meth public static boolean isIntrospectable(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.dsl.Introspection$SpecializationInfo getSpecialization(com.oracle.truffle.api.nodes.Node,java.lang.String)
meth public static java.util.List<com.oracle.truffle.api.dsl.Introspection$SpecializationInfo> getSpecializations(com.oracle.truffle.api.nodes.Node)
supr java.lang.Object
hfds EMPTY_CACHED,NO_CACHED,data

CLSS public abstract interface static com.oracle.truffle.api.dsl.Introspection$Provider
 outer com.oracle.truffle.api.dsl.Introspection
meth public !varargs static com.oracle.truffle.api.dsl.Introspection create(java.lang.Object[])
meth public abstract com.oracle.truffle.api.dsl.Introspection getIntrospectionData()

CLSS public final static com.oracle.truffle.api.dsl.Introspection$SpecializationInfo
 outer com.oracle.truffle.api.dsl.Introspection
meth public boolean isActive()
meth public boolean isExcluded()
meth public int getInstances()
meth public java.lang.String getMethodName()
meth public java.util.List<java.lang.Object> getCachedData(int)
supr java.lang.Object
hfds cachedData,methodName,state

CLSS public abstract interface !annotation com.oracle.truffle.api.dsl.NodeChild
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.dsl.NodeChildren)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?> type()
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
intf com.oracle.truffle.api.TruffleException
intf com.oracle.truffle.api.interop.TruffleObject
meth public final boolean isCancelled()
 anno 0 java.lang.Deprecated()
meth public final boolean isExit()
 anno 0 java.lang.Deprecated()
meth public final boolean isIncompleteSource()
 anno 0 java.lang.Deprecated()
meth public final boolean isInternalError()
 anno 0 java.lang.Deprecated()
meth public final boolean isSyntaxError()
 anno 0 java.lang.Deprecated()
meth public final com.oracle.truffle.api.nodes.Node getLocation()
meth public final com.oracle.truffle.api.source.SourceSection getSourceLocation()
 anno 0 java.lang.Deprecated()
meth public final int getExitStatus()
 anno 0 java.lang.Deprecated()
meth public final int getStackTraceElementLimit()
meth public final java.lang.Object getExceptionObject()
 anno 0 java.lang.Deprecated()
meth public final java.lang.Throwable fillInStackTrace()
meth public final java.lang.Throwable getCause()
meth public final java.lang.Throwable initCause(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
supr java.lang.RuntimeException
hfds cause,lazyStackTrace,location,stackTraceElementLimit

CLSS public abstract interface com.oracle.truffle.api.frame.Frame
meth public abstract boolean getBoolean(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract boolean isBoolean(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isByte(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isDouble(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isFloat(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isInt(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isLong(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract boolean isObject(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract byte getByte(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract com.oracle.truffle.api.frame.FrameDescriptor getFrameDescriptor()
meth public abstract com.oracle.truffle.api.frame.MaterializedFrame materialize()
meth public abstract double getDouble(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract float getFloat(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract int getInt(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract java.lang.Object getObject(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract java.lang.Object getValue(com.oracle.truffle.api.frame.FrameSlot)
meth public abstract java.lang.Object[] getArguments()
meth public abstract long getLong(com.oracle.truffle.api.frame.FrameSlot) throws com.oracle.truffle.api.frame.FrameSlotTypeException
meth public abstract void setBoolean(com.oracle.truffle.api.frame.FrameSlot,boolean)
meth public abstract void setByte(com.oracle.truffle.api.frame.FrameSlot,byte)
meth public abstract void setDouble(com.oracle.truffle.api.frame.FrameSlot,double)
meth public abstract void setFloat(com.oracle.truffle.api.frame.FrameSlot,float)
meth public abstract void setInt(com.oracle.truffle.api.frame.FrameSlot,int)
meth public abstract void setLong(com.oracle.truffle.api.frame.FrameSlot,long)
meth public abstract void setObject(com.oracle.truffle.api.frame.FrameSlot,java.lang.Object)

CLSS public final com.oracle.truffle.api.frame.FrameDescriptor
cons public init()
cons public init(java.lang.Object)
intf java.lang.Cloneable
meth public com.oracle.truffle.api.Assumption getNotInFrameAssumption(java.lang.Object)
meth public com.oracle.truffle.api.Assumption getVersion()
meth public com.oracle.truffle.api.frame.FrameDescriptor copy()
meth public com.oracle.truffle.api.frame.FrameSlot addFrameSlot(java.lang.Object)
meth public com.oracle.truffle.api.frame.FrameSlot addFrameSlot(java.lang.Object,com.oracle.truffle.api.frame.FrameSlotKind)
meth public com.oracle.truffle.api.frame.FrameSlot addFrameSlot(java.lang.Object,java.lang.Object,com.oracle.truffle.api.frame.FrameSlotKind)
meth public com.oracle.truffle.api.frame.FrameSlot findFrameSlot(java.lang.Object)
meth public com.oracle.truffle.api.frame.FrameSlot findOrAddFrameSlot(java.lang.Object)
meth public com.oracle.truffle.api.frame.FrameSlot findOrAddFrameSlot(java.lang.Object,com.oracle.truffle.api.frame.FrameSlotKind)
meth public com.oracle.truffle.api.frame.FrameSlot findOrAddFrameSlot(java.lang.Object,java.lang.Object,com.oracle.truffle.api.frame.FrameSlotKind)
meth public com.oracle.truffle.api.frame.FrameSlotKind getFrameSlotKind(com.oracle.truffle.api.frame.FrameSlot)
meth public int getSize()
meth public java.lang.Object getDefaultValue()
meth public java.lang.String toString()
meth public java.util.List<? extends com.oracle.truffle.api.frame.FrameSlot> getSlots()
meth public java.util.Set<java.lang.Object> getIdentifiers()
meth public void removeFrameSlot(java.lang.Object)
meth public void setFrameSlotKind(com.oracle.truffle.api.frame.FrameSlot,com.oracle.truffle.api.frame.FrameSlotKind)
supr java.lang.Object
hfds NEVER_PART_OF_COMPILATION_MESSAGE,defaultValue,identifierToNotInFrameAssumptionMap,identifierToSlotMap,lock,materializeCalled,size,slots,version

CLSS public abstract interface com.oracle.truffle.api.frame.FrameInstance
innr public final static !enum FrameAccess
meth public abstract boolean isVirtualFrame()
meth public abstract com.oracle.truffle.api.CallTarget getCallTarget()
meth public abstract com.oracle.truffle.api.frame.Frame getFrame(com.oracle.truffle.api.frame.FrameInstance$FrameAccess)
meth public abstract com.oracle.truffle.api.nodes.Node getCallNode()

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

CLSS public final com.oracle.truffle.api.frame.FrameSlot
intf java.lang.Cloneable
meth public com.oracle.truffle.api.frame.FrameSlotKind getKind()
 anno 0 java.lang.Deprecated()
meth public int getIndex()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getIdentifier()
meth public java.lang.Object getInfo()
meth public java.lang.String toString()
meth public void setKind(com.oracle.truffle.api.frame.FrameSlotKind)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds descriptor,identifier,index,info,kind

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
meth public static com.oracle.truffle.api.frame.FrameSlotKind valueOf(java.lang.String)
meth public static com.oracle.truffle.api.frame.FrameSlotKind[] values()
supr java.lang.Enum<com.oracle.truffle.api.frame.FrameSlotKind>

CLSS public final com.oracle.truffle.api.frame.FrameSlotTypeException
cons public init()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds serialVersionUID

CLSS public final com.oracle.truffle.api.frame.FrameUtil
meth public static boolean getBooleanSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static byte getByteSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static double getDoubleSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static float getFloatSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static int getIntSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static java.lang.Object getObjectSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
meth public static long getLongSafe(com.oracle.truffle.api.frame.Frame,com.oracle.truffle.api.frame.FrameSlot)
supr java.lang.Object

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

CLSS public com.oracle.truffle.api.instrumentation.EventBinding<%0 extends java.lang.Object>
meth public boolean isDisposed()
meth public void dispose()
meth public {com.oracle.truffle.api.instrumentation.EventBinding%0} getElement()
supr java.lang.Object
hfds disposed,disposing,element,instrumenter
hcls Allocation,Source

CLSS public final com.oracle.truffle.api.instrumentation.EventContext
meth public boolean hasTag(java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>)
meth public boolean isLanguageContextInitialized()
meth public com.oracle.truffle.api.instrumentation.ExecutionEventNode lookupExecutionEventNode(com.oracle.truffle.api.instrumentation.EventBinding<? extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory>)
meth public com.oracle.truffle.api.nodes.Node getInstrumentedNode()
meth public com.oracle.truffle.api.source.SourceSection getInstrumentedSourceSection()
meth public java.lang.Object getNodeObject()
meth public java.lang.RuntimeException createError(java.lang.RuntimeException)
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
 anno 0 java.lang.Deprecated()

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
meth protected void onReturnExceptional(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Throwable)
meth protected void onReturnValue(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract interface com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory
meth public abstract com.oracle.truffle.api.instrumentation.ExecutionEventNode create(com.oracle.truffle.api.instrumentation.EventContext)

CLSS public abstract interface !annotation com.oracle.truffle.api.instrumentation.GenerateWrapper
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation IncomingConverter
innr public abstract interface static !annotation OutgoingConverter
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

CLSS public abstract interface com.oracle.truffle.api.instrumentation.InstrumentableNode
innr public abstract interface static WrapperNode
intf com.oracle.truffle.api.nodes.NodeInterface
meth public abstract boolean isInstrumentable()
meth public abstract com.oracle.truffle.api.instrumentation.InstrumentableNode$WrapperNode createWrapper(com.oracle.truffle.api.instrumentation.ProbeNode)
meth public boolean hasTag(java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>)
meth public com.oracle.truffle.api.instrumentation.InstrumentableNode materializeInstrumentableNodes(java.util.Set<java.lang.Class<? extends com.oracle.truffle.api.instrumentation.Tag>>)
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
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
 anno 0 java.lang.Deprecated()
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachExecutionEventFactory(com.oracle.truffle.api.instrumentation.SourceSectionFilter,com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0})
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceListener(com.oracle.truffle.api.instrumentation.SourceFilter,{%%0},boolean)
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
 anno 0 java.lang.Deprecated()
meth public abstract <%0 extends com.oracle.truffle.api.instrumentation.LoadSourceSectionListener> com.oracle.truffle.api.instrumentation.EventBinding<{%%0}> attachLoadSourceSectionListener(com.oracle.truffle.api.instrumentation.SourceSectionFilter,{%%0},boolean)
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

CLSS public final com.oracle.truffle.api.instrumentation.ProbeNode
fld public final static java.lang.Object UNWIND_ACTION_REENTER
meth public com.oracle.truffle.api.nodes.Node copy()
meth public com.oracle.truffle.api.nodes.NodeCost getCost()
meth public java.lang.Object onReturnExceptionalOrUnwind(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Throwable,boolean)
meth public void onEnter(com.oracle.truffle.api.frame.VirtualFrame)
meth public void onReturnValue(com.oracle.truffle.api.frame.VirtualFrame,java.lang.Object)
supr com.oracle.truffle.api.nodes.Node
hfds SEEN_REENTER,SEEN_RETURN,SEEN_UNWIND,SEEN_UNWIND_NEXT,UNWIND_ACTION_IGNORED,chain,context,handler,retiredNodeReference,seen,version
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
meth public java.lang.String toString()
meth public static com.oracle.truffle.api.instrumentation.SourceSectionFilter$Builder newBuilder()
supr java.lang.Object
hfds expressions
hcls EventFilterExpression,Not

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
innr protected abstract interface static ContextLocalFactory
innr protected abstract interface static ContextThreadLocalFactory
innr public abstract interface static !annotation Registration
innr public abstract interface static Provider
innr public final static Env
meth protected abstract void onCreate(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextLocal<{%%0}> createContextLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory<{%%0}>)
meth protected final <%0 extends java.lang.Object> com.oracle.truffle.api.ContextThreadLocal<{%%0}> createContextThreadLocal(com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextThreadLocalFactory<{%%0}>)
meth protected org.graalvm.options.OptionDescriptors getContextOptionDescriptors()
meth protected org.graalvm.options.OptionDescriptors getOptionDescriptors()
meth protected void onDispose(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
meth protected void onFinalize(com.oracle.truffle.api.instrumentation.TruffleInstrument$Env)
supr java.lang.Object
hfds contextLocals,contextThreadLocals

CLSS protected abstract interface static com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.FunctionalInterface()
meth public abstract {com.oracle.truffle.api.instrumentation.TruffleInstrument$ContextLocalFactory%0} create(com.oracle.truffle.api.TruffleContext)

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
meth public com.oracle.truffle.api.TruffleContext getEnteredContext()
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.lang.String)
meth public com.oracle.truffle.api.TruffleFile getTruffleFile(java.net.URI)
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.Class<?>)
meth public com.oracle.truffle.api.TruffleLogger getLogger(java.lang.String)
meth public com.oracle.truffle.api.instrumentation.Instrumenter getInstrumenter()
meth public com.oracle.truffle.api.nodes.ExecutableNode parseInline(com.oracle.truffle.api.source.Source,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.MaterializedFrame)
meth public com.oracle.truffle.api.nodes.LanguageInfo findLanguage(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.LanguageInfo getLanguageInfo(java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>>)
meth public com.oracle.truffle.api.source.SourceSection findSourceLocation(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.io.InputStream in()
meth public java.io.OutputStream err()
meth public java.io.OutputStream out()
meth public java.lang.Iterable<com.oracle.truffle.api.Scope> findLocalScopes(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.Frame)
 anno 0 java.lang.Deprecated()
meth public java.lang.Iterable<com.oracle.truffle.api.Scope> findTopScopes(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object findMetaObject(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getLanguageView(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Object)
meth public java.lang.Object getPolyglotBindings()
meth public java.lang.Object getScope(com.oracle.truffle.api.nodes.LanguageInfo)
meth public java.lang.Object getScopedView(com.oracle.truffle.api.nodes.LanguageInfo,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.frame.Frame,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString(com.oracle.truffle.api.nodes.LanguageInfo,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,?> getExportedSymbols()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.InstrumentInfo> getInstruments()
meth public java.util.Map<java.lang.String,com.oracle.truffle.api.nodes.LanguageInfo> getLanguages()
meth public org.graalvm.options.OptionValues getOptions()
meth public org.graalvm.options.OptionValues getOptions(com.oracle.truffle.api.TruffleContext)
meth public org.graalvm.polyglot.io.MessageEndpoint startServer(java.net.URI,org.graalvm.polyglot.io.MessageEndpoint) throws java.io.IOException,org.graalvm.polyglot.io.MessageTransport$VetoException
meth public void registerService(java.lang.Object)
meth public void setAsynchronousStackDepth(int)
supr java.lang.Object
hfds INTEROP,err,in,instrumenter,messageTransport,options,out,polyglotInstrument,services
hcls GuardedExecutableNode,MessageTransportProxy

CLSS public abstract interface static com.oracle.truffle.api.instrumentation.TruffleInstrument$Provider
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
meth public abstract com.oracle.truffle.api.instrumentation.TruffleInstrument create()
meth public abstract java.lang.String getInstrumentClassName()
meth public abstract java.util.Collection<java.lang.String> getServicesClassNames()

CLSS public abstract interface static !annotation com.oracle.truffle.api.instrumentation.TruffleInstrument$Registration
 outer com.oracle.truffle.api.instrumentation.TruffleInstrument
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean internal()
meth public abstract !hasdefault java.lang.Class<?>[] services()
meth public abstract !hasdefault java.lang.String id()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String version()

CLSS public final com.oracle.truffle.api.interop.ArityException
meth public int getActualArity()
meth public int getExpectedArity()
meth public java.lang.String getMessage()
meth public static com.oracle.truffle.api.interop.ArityException create(int,int)
meth public static com.oracle.truffle.api.interop.ArityException create(int,int,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds actualArity,expectedArity,serialVersionUID

CLSS public final !enum com.oracle.truffle.api.interop.ExceptionType
fld public final static com.oracle.truffle.api.interop.ExceptionType EXIT
fld public final static com.oracle.truffle.api.interop.ExceptionType INTERRUPT
fld public final static com.oracle.truffle.api.interop.ExceptionType PARSE_ERROR
fld public final static com.oracle.truffle.api.interop.ExceptionType RUNTIME_ERROR
intf com.oracle.truffle.api.interop.TruffleObject
meth public static com.oracle.truffle.api.interop.ExceptionType valueOf(java.lang.String)
meth public static com.oracle.truffle.api.interop.ExceptionType[] values()
supr java.lang.Enum<com.oracle.truffle.api.interop.ExceptionType>

CLSS public abstract com.oracle.truffle.api.interop.InteropException
meth public final java.lang.Throwable fillInStackTrace()
meth public final java.lang.Throwable getCause()
meth public final java.lang.Throwable initCause(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
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
meth public boolean fitsInByte(java.lang.Object)
meth public boolean fitsInDouble(java.lang.Object)
meth public boolean fitsInFloat(java.lang.Object)
meth public boolean fitsInInt(java.lang.Object)
meth public boolean fitsInLong(java.lang.Object)
meth public boolean fitsInShort(java.lang.Object)
meth public boolean hasArrayElements(java.lang.Object)
meth public boolean hasDeclaringMetaObject(java.lang.Object)
meth public boolean hasExceptionCause(java.lang.Object)
meth public boolean hasExceptionMessage(java.lang.Object)
meth public boolean hasExceptionStackTrace(java.lang.Object)
meth public boolean hasExecutableName(java.lang.Object)
meth public boolean hasLanguage(java.lang.Object)
meth public boolean hasMemberReadSideEffects(java.lang.Object,java.lang.String)
meth public boolean hasMemberWriteSideEffects(java.lang.Object,java.lang.String)
meth public boolean hasMembers(java.lang.Object)
meth public boolean hasMetaObject(java.lang.Object)
meth public boolean hasScopeParent(java.lang.Object)
meth public boolean hasSourceLocation(java.lang.Object)
meth public boolean isArrayElementInsertable(java.lang.Object,long)
meth public boolean isArrayElementModifiable(java.lang.Object,long)
meth public boolean isArrayElementReadable(java.lang.Object,long)
meth public boolean isArrayElementRemovable(java.lang.Object,long)
meth public boolean isBoolean(java.lang.Object)
meth public boolean isDate(java.lang.Object)
meth public boolean isDuration(java.lang.Object)
meth public boolean isException(java.lang.Object)
meth public boolean isExceptionIncompleteSource(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public boolean isExecutable(java.lang.Object)
meth public boolean isIdentical(java.lang.Object,java.lang.Object,com.oracle.truffle.api.interop.InteropLibrary)
meth public boolean isInstantiable(java.lang.Object)
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
meth public com.oracle.truffle.api.interop.ExceptionType getExceptionType(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public com.oracle.truffle.api.source.SourceSection getSourceLocation(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public double asDouble(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public final boolean hasIdentity(java.lang.Object)
meth public final boolean isArrayElementExisting(java.lang.Object,long)
meth public final boolean isArrayElementWritable(java.lang.Object,long)
meth public final boolean isInstant(java.lang.Object)
meth public final boolean isMemberExisting(java.lang.Object,java.lang.String)
meth public final boolean isMemberWritable(java.lang.Object,java.lang.String)
meth public final java.lang.Object getMembers(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public final java.lang.Object toDisplayString(java.lang.Object)
meth public float asFloat(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int asInt(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int getExceptionExitStatus(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public int identityHashCode(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Class<? extends com.oracle.truffle.api.TruffleLanguage<?>> getLanguage(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getDeclaringMetaObject(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionCause(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionMessage(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExceptionStackTrace(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getExecutableName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMembers(java.lang.Object,boolean) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaObject(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaQualifiedName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getMetaSimpleName(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object getScopeParent(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readArrayElement(java.lang.Object,long) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object readMember(java.lang.Object,java.lang.String) throws com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.Object toDisplayString(java.lang.Object,boolean)
meth public java.lang.RuntimeException throwException(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.lang.String asString(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.Duration asDuration(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.Instant asInstant(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.LocalDate asDate(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.LocalTime asTime(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public java.time.ZoneId asTimeZone(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long asLong(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long asPointer(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public long getArraySize(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public short asShort(java.lang.Object) throws com.oracle.truffle.api.interop.UnsupportedMessageException
meth public static com.oracle.truffle.api.interop.InteropLibrary getUncached()
meth public static com.oracle.truffle.api.interop.InteropLibrary getUncached(java.lang.Object)
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.interop.InteropLibrary> getFactory()
meth public void removeArrayElement(java.lang.Object,long) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void removeMember(java.lang.Object,java.lang.String) throws com.oracle.truffle.api.interop.UnknownIdentifierException,com.oracle.truffle.api.interop.UnsupportedMessageException
meth public void toNative(java.lang.Object)
meth public void writeArrayElement(java.lang.Object,long,java.lang.Object) throws com.oracle.truffle.api.interop.InvalidArrayIndexException,com.oracle.truffle.api.interop.UnsupportedMessageException,com.oracle.truffle.api.interop.UnsupportedTypeException
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

CLSS public abstract interface com.oracle.truffle.api.interop.TruffleObject

CLSS public final com.oracle.truffle.api.interop.UnknownIdentifierException
meth public java.lang.String getMessage()
meth public java.lang.String getUnknownIdentifier()
meth public static com.oracle.truffle.api.interop.UnknownIdentifierException create(java.lang.String)
meth public static com.oracle.truffle.api.interop.UnknownIdentifierException create(java.lang.String,java.lang.Throwable)
supr com.oracle.truffle.api.interop.InteropException
hfds serialVersionUID,unknownIdentifier

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

CLSS public abstract interface !annotation com.oracle.truffle.api.library.CachedLibrary
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String limit()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface com.oracle.truffle.api.library.DefaultExportProvider
meth public abstract int getPriority()
meth public abstract java.lang.Class<?> getDefaultExport()
meth public abstract java.lang.Class<?> getReceiverClass()
meth public abstract java.lang.String getLibraryClassName()

CLSS public abstract com.oracle.truffle.api.library.DynamicDispatchLibrary
cons protected init()
meth public abstract java.lang.Object cast(java.lang.Object)
meth public java.lang.Class<?> dispatch(java.lang.Object)
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.library.DynamicDispatchLibrary> getFactory()
supr com.oracle.truffle.api.library.Library
hfds FACTORY

CLSS public abstract interface !annotation com.oracle.truffle.api.library.ExportLibrary
 anno 0 java.lang.annotation.Repeatable(java.lang.Class<? extends java.lang.annotation.Annotation> value=class com.oracle.truffle.api.library.ExportLibrary$Repeat)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Repeat
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int priority()
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
innr protected abstract interface static DelegateExport
meth protected !varargs static com.oracle.truffle.api.utilities.FinalBitSet createMessageBitSet(com.oracle.truffle.api.library.LibraryFactory<?>,java.lang.String[])
meth protected abstract {com.oracle.truffle.api.library.LibraryExport%0} createCached(java.lang.Object)
meth protected abstract {com.oracle.truffle.api.library.LibraryExport%0} createUncached(java.lang.Object)
meth protected static <%0 extends com.oracle.truffle.api.library.Library> {%%0} createDelegate(com.oracle.truffle.api.library.LibraryFactory<{%%0}>,{%%0})
meth public !varargs static <%0 extends com.oracle.truffle.api.library.Library> void register(java.lang.Class<?>,com.oracle.truffle.api.library.LibraryExport<?>[])
meth public final java.lang.String toString()
supr java.lang.Object
hfds GENERATED_CLASS_SUFFIX,defaultExport,library,receiverClass,registerClass

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
meth protected static <%0 extends com.oracle.truffle.api.library.Library> void register(java.lang.Class<{%%0}>,com.oracle.truffle.api.library.LibraryFactory<{%%0}>)
meth protected static <%0 extends com.oracle.truffle.api.library.Library> {%%0} getDelegateLibrary({%%0},java.lang.Object)
meth protected static boolean isDelegated(com.oracle.truffle.api.library.Library,int)
meth protected static java.lang.Object readDelegate(com.oracle.truffle.api.library.Library,java.lang.Object)
meth protected {com.oracle.truffle.api.library.LibraryFactory%0} createAssertions({com.oracle.truffle.api.library.LibraryFactory%0})
meth protected {com.oracle.truffle.api.library.LibraryFactory%0} createDelegate({com.oracle.truffle.api.library.LibraryFactory%0})
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} create(java.lang.Object)
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} createDispatched(int)
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} getUncached()
meth public final {com.oracle.truffle.api.library.LibraryFactory%0} getUncached(java.lang.Object)
meth public java.lang.String toString()
meth public static <%0 extends com.oracle.truffle.api.library.Library> com.oracle.truffle.api.library.LibraryFactory<{%%0}> resolve(java.lang.Class<{%%0}>)
supr java.lang.Object
hfds EMPTY_DEFAULT_EXPORT_ARRAY,LIBRARIES,UNSAFE,afterBuiltinDefaultExports,beforeBuiltinDefaultExports,cachedCache,dispatchLibrary,exportCache,externalDefaultProviders,libraryClass,messages,nameToMessages,proxyExports,uncachedCache,uncachedDispatch
hcls ProxyExports,ResolvedDispatch

CLSS public abstract com.oracle.truffle.api.library.Message
cons protected !varargs init(java.lang.Class<? extends com.oracle.truffle.api.library.Library>,java.lang.String,java.lang.Class<?>,java.lang.Class<?>[])
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public final boolean equals(java.lang.Object)
meth public final com.oracle.truffle.api.library.LibraryFactory<?> getFactory()
meth public final int getParameterCount()
meth public final int hashCode()
meth public final java.lang.Class<? extends com.oracle.truffle.api.library.Library> getLibraryClass()
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
hfds hash,library,libraryClass,parameterCount,parameterTypes,qualifiedName,returnType,simpleName

CLSS public abstract com.oracle.truffle.api.library.ReflectionLibrary
cons protected init()
meth public !varargs java.lang.Object send(java.lang.Object,com.oracle.truffle.api.library.Message,java.lang.Object[]) throws java.lang.Exception
meth public static com.oracle.truffle.api.library.LibraryFactory<com.oracle.truffle.api.library.ReflectionLibrary> getFactory()
supr com.oracle.truffle.api.library.Library
hfds FACTORY

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

CLSS public com.oracle.truffle.api.nodes.ControlFlowException
cons public init()
meth public final java.lang.Throwable fillInStackTrace()
supr java.lang.RuntimeException
hfds serialVersionUID

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
hfds CURRENT,reference,thread

CLSS public abstract com.oracle.truffle.api.nodes.ExecutableNode
cons protected init(com.oracle.truffle.api.TruffleLanguage<?>)
meth public abstract java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public final <%0 extends com.oracle.truffle.api.TruffleLanguage> {%%0} getLanguage(java.lang.Class<{%%0}>)
meth public final com.oracle.truffle.api.nodes.LanguageInfo getLanguageInfo()
supr com.oracle.truffle.api.nodes.Node
hfds GENERIC,engineRef,referenceCache
hcls ReferenceCache

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

CLSS public com.oracle.truffle.api.nodes.GraphPrintVisitor
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.io.OutputStream)
fld public final static int GraphVisualizerPort = 4444
fld public final static java.lang.String GraphVisualizerAddress = "127.0.0.1"
innr public GraphPrintAdapter
innr public abstract interface static !annotation CustomGraphPrintHandler
innr public abstract interface static !annotation NullGraphPrintHandler
innr public abstract interface static GraphPrintHandler
intf java.io.Closeable
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor beginGraph(java.lang.String)
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor beginGroup(java.lang.String)
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor endGraph()
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor endGroup()
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor visit(java.lang.Object)
meth public com.oracle.truffle.api.nodes.GraphPrintVisitor visit(java.lang.Object,com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintHandler)
meth public java.lang.String toString()
meth public void close()
meth public void printToFile(java.io.File)
meth public void printToNetwork(boolean)
meth public void printToSysout()
supr java.lang.Object
hfds DEFAULT_GRAPH_NAME,currentGraphName,edgeList,id,nodeMap,openGraphCount,openGroupCount,outputStream,prevNodeMap,xmlstream
hcls DefaultGraphPrintHandler,EdgeElement,Impl,NodeElement,XMLImpl

CLSS public abstract interface static !annotation com.oracle.truffle.api.nodes.GraphPrintVisitor$CustomGraphPrintHandler
 outer com.oracle.truffle.api.nodes.GraphPrintVisitor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintHandler> handler()

CLSS public com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintAdapter
 outer com.oracle.truffle.api.nodes.GraphPrintVisitor
cons public init(com.oracle.truffle.api.nodes.GraphPrintVisitor)
meth public boolean visited(java.lang.Object)
meth public void connectNodes(java.lang.Object,java.lang.Object)
meth public void connectNodes(java.lang.Object,java.lang.Object,java.lang.String)
meth public void createElementForNode(java.lang.Object)
meth public void setNodeProperty(java.lang.Object,java.lang.String,java.lang.Object)
meth public void visit(java.lang.Object)
meth public void visit(java.lang.Object,com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintHandler)
supr java.lang.Object

CLSS public abstract interface static com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintHandler
 outer com.oracle.truffle.api.nodes.GraphPrintVisitor
meth public abstract void visit(java.lang.Object,com.oracle.truffle.api.nodes.GraphPrintVisitor$GraphPrintAdapter)

CLSS public abstract interface static !annotation com.oracle.truffle.api.nodes.GraphPrintVisitor$NullGraphPrintHandler
 outer com.oracle.truffle.api.nodes.GraphPrintVisitor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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
hfds defaultMimeType,id,interactive,internal,mimeTypes,name,polyglotLanguage,version

CLSS public abstract com.oracle.truffle.api.nodes.LoopNode
cons protected init()
meth public abstract com.oracle.truffle.api.nodes.RepeatingNode getRepeatingNode()
meth public abstract void executeLoop(com.oracle.truffle.api.frame.VirtualFrame)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public static void reportLoopCount(com.oracle.truffle.api.nodes.Node,int)
supr com.oracle.truffle.api.nodes.Node

CLSS public abstract com.oracle.truffle.api.nodes.Node
cons protected init()
innr public abstract interface static !annotation Child
innr public abstract interface static !annotation Children
intf com.oracle.truffle.api.nodes.NodeInterface
intf java.lang.Cloneable
meth protected final <%0 extends com.oracle.truffle.api.TruffleLanguage> com.oracle.truffle.api.TruffleLanguage$LanguageReference<{%%0}> lookupLanguageReference(java.lang.Class<{%%0}>)
meth protected final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} insert({%%0})
meth protected final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0}[] insert({%%0}[])
meth protected final <%0 extends java.lang.Object, %1 extends com.oracle.truffle.api.TruffleLanguage<{%%0}>> com.oracle.truffle.api.TruffleLanguage$ContextReference<{%%0}> lookupContextReference(java.lang.Class<{%%1}>)
meth protected final java.util.concurrent.locks.Lock getLock()
meth protected final void notifyInserted(com.oracle.truffle.api.nodes.Node)
meth protected final void reportPolymorphicSpecialize()
meth protected void onReplace(com.oracle.truffle.api.nodes.Node,java.lang.CharSequence)
meth public boolean isAdoptable()
meth public com.oracle.truffle.api.nodes.Node copy()
meth public com.oracle.truffle.api.nodes.Node deepCopy()
meth public com.oracle.truffle.api.nodes.NodeCost getCost()
meth public com.oracle.truffle.api.source.SourceSection getEncapsulatingSourceSection()
meth public com.oracle.truffle.api.source.SourceSection getSourceSection()
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} replace({%%0})
meth public final <%0 extends com.oracle.truffle.api.nodes.Node> {%%0} replace({%%0},java.lang.CharSequence)
meth public final <%0 extends java.lang.Object> {%%0} atomic(java.util.concurrent.Callable<{%%0}>)
meth public final boolean isSafelyReplaceableBy(com.oracle.truffle.api.nodes.Node)
meth public final com.oracle.truffle.api.nodes.Node getParent()
meth public final com.oracle.truffle.api.nodes.RootNode getRootNode()
meth public final java.lang.Iterable<com.oracle.truffle.api.nodes.Node> getChildren()
meth public final void accept(com.oracle.truffle.api.nodes.NodeVisitor)
meth public final void adoptChildren()
meth public final void atomic(java.lang.Runnable)
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.Object> getDebugProperties()
supr java.lang.Object
hfds GIL_LOCK,UNCACHED_CONTEXT_REFERENCES,UNCACHED_LANGUAGE_REFERENCES,parent

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
meth protected abstract java.lang.Class<?> getFieldType(java.lang.Object)
meth protected abstract java.lang.Iterable<?> getNodeFields()
 anno 0 java.lang.Deprecated()
meth protected abstract java.lang.Object getFieldObject(java.lang.Object,com.oracle.truffle.api.nodes.Node)
meth protected abstract java.lang.Object getFieldValue(java.lang.Object,com.oracle.truffle.api.nodes.Node)
meth protected abstract java.lang.Object[] getNodeFieldArray()
meth protected abstract java.lang.String getFieldName(java.lang.Object)
meth protected abstract void putFieldObject(java.lang.Object,com.oracle.truffle.api.nodes.Node,java.lang.Object)
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.nodes.Node> getType()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor getNodeClassField()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor getParentField()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor[] getChildFields()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor[] getChildrenFields()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor[] getCloneableFields()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor[] getFields()
 anno 0 java.lang.Deprecated()
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

CLSS public abstract com.oracle.truffle.api.nodes.NodeFieldAccessor
 anno 0 java.lang.Deprecated()
cons protected init(com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind,java.lang.Class<?>,java.lang.String,java.lang.Class<?>)
fld protected final java.lang.Class<?> type
innr public abstract static AbstractUnsafeNodeFieldAccessor
innr public final static !enum NodeFieldKind
meth protected static com.oracle.truffle.api.nodes.NodeFieldAccessor create(com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind,java.lang.reflect.Field)
meth public abstract java.lang.Object getObject(com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Object loadValue(com.oracle.truffle.api.nodes.Node)
meth public abstract void putObject(com.oracle.truffle.api.nodes.Node,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind getKind()
meth public java.lang.Class<?> getDeclaringClass()
meth public java.lang.Class<?> getType()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds USE_UNSAFE,declaringClass,kind,name
hcls ReflectionNodeField,UnsafeNodeField

CLSS public abstract static com.oracle.truffle.api.nodes.NodeFieldAccessor$AbstractUnsafeNodeFieldAccessor
 outer com.oracle.truffle.api.nodes.NodeFieldAccessor
 anno 0 java.lang.Deprecated()
cons protected init(com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind,java.lang.Class<?>,java.lang.String,java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
meth public abstract long getOffset()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getObject(com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object loadValue(com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public void putObject(com.oracle.truffle.api.nodes.Node,java.lang.Object)
 anno 0 java.lang.Deprecated()
supr com.oracle.truffle.api.nodes.NodeFieldAccessor
hfds unsafe

CLSS public final static !enum com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind
 outer com.oracle.truffle.api.nodes.NodeFieldAccessor
fld public final static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind CHILD
fld public final static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind CHILDREN
fld public final static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind DATA
fld public final static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind NODE_CLASS
fld public final static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind PARENT
meth public static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind valueOf(java.lang.String)
meth public static com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind[] values()
supr java.lang.Enum<com.oracle.truffle.api.nodes.NodeFieldAccessor$NodeFieldKind>

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
meth public static boolean forEachChild(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.NodeVisitor)
meth public static boolean isReplacementSafe(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static boolean replaceChild(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static boolean verify(com.oracle.truffle.api.nodes.Node)
meth public static com.oracle.truffle.api.nodes.Node getCurrentEncapsulatingNode()
 anno 0 java.lang.Deprecated()
meth public static com.oracle.truffle.api.nodes.Node getNthParent(com.oracle.truffle.api.nodes.Node,int)
meth public static com.oracle.truffle.api.nodes.Node pushEncapsulatingNode(com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public static com.oracle.truffle.api.nodes.NodeFieldAccessor findChildField(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public static int countNodes(com.oracle.truffle.api.nodes.Node)
meth public static int countNodes(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.NodeUtil$NodeCountFilter)
meth public static java.lang.String printCompactTreeToString(com.oracle.truffle.api.nodes.Node)
meth public static java.lang.String printSourceAttributionTree(com.oracle.truffle.api.nodes.Node)
meth public static java.lang.String printSyntaxTags(java.lang.Object)
meth public static java.lang.String printTreeToString(com.oracle.truffle.api.nodes.Node)
meth public static java.util.Iterator<com.oracle.truffle.api.nodes.Node> makeRecursiveIterator(com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<com.oracle.truffle.api.nodes.Node> collectNodes(com.oracle.truffle.api.nodes.Node,com.oracle.truffle.api.nodes.Node)
meth public static java.util.List<com.oracle.truffle.api.nodes.Node> findNodeChildren(com.oracle.truffle.api.nodes.Node)
meth public static void popEncapsulatingNode(com.oracle.truffle.api.nodes.Node)
 anno 0 java.lang.Deprecated()
meth public static void printCompactTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printSourceAttributionTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printSourceAttributionTree(java.io.PrintWriter,com.oracle.truffle.api.nodes.Node)
meth public static void printTree(java.io.OutputStream,com.oracle.truffle.api.nodes.Node)
meth public static void printTree(java.io.PrintWriter,com.oracle.truffle.api.nodes.Node)
supr java.lang.Object
hcls NodeCounter,RecursiveNodeIterator

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
meth protected boolean isCloneUninitializedSupported()
meth protected boolean isInstrumentable()
meth protected boolean isTrivial()
meth protected com.oracle.truffle.api.nodes.ExecutionSignature prepareForAOT()
meth protected com.oracle.truffle.api.nodes.RootNode cloneUninitialized()
meth protected final void setCallTarget(com.oracle.truffle.api.RootCallTarget)
meth protected java.lang.Object translateStackTraceElement(com.oracle.truffle.api.TruffleStackTraceElement)
meth protected java.util.List<com.oracle.truffle.api.TruffleStackTraceElement> findAsynchronousFrames(com.oracle.truffle.api.frame.Frame)
meth public abstract java.lang.Object execute(com.oracle.truffle.api.frame.VirtualFrame)
meth public boolean isCaptureFramesForTrace()
meth public boolean isCloningAllowed()
meth public boolean isInternal()
meth public com.oracle.truffle.api.CompilerOptions getCompilerOptions()
meth public com.oracle.truffle.api.nodes.Node copy()
meth public final <%0 extends java.lang.Object, %1 extends com.oracle.truffle.api.TruffleLanguage<{%%0}>> {%%0} getCurrentContext(java.lang.Class<{%%1}>)
 anno 0 java.lang.Deprecated()
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
 anno 0 java.lang.Deprecated()
supr java.lang.Exception
hfds serialVersionUID

CLSS public final com.oracle.truffle.api.nodes.UnexpectedResultException
cons public init(java.lang.Object)
meth public java.lang.Object getResult()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds result,serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.object.BooleanLocation
intf com.oracle.truffle.api.object.TypedLocation
meth public abstract boolean getBoolean(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract boolean getBoolean(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract java.lang.Class<java.lang.Boolean> getType()
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setBoolean(com.oracle.truffle.api.object.DynamicObject,boolean,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)

CLSS public abstract interface com.oracle.truffle.api.object.DoubleLocation
intf com.oracle.truffle.api.object.TypedLocation
meth public abstract double getDouble(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract double getDouble(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract java.lang.Class<java.lang.Double> getType()
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setDouble(com.oracle.truffle.api.object.DynamicObject,double,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)

CLSS public abstract com.oracle.truffle.api.object.DynamicObject
cons protected init()
 anno 0 java.lang.Deprecated()
cons protected init(com.oracle.truffle.api.object.Shape)
cons protected init(com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Layout$Access)
 anno 0 java.lang.Deprecated()
innr protected abstract interface static !annotation DynamicField
intf com.oracle.truffle.api.interop.TruffleObject
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean containsKey(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean delete(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean isEmpty()
 anno 0 java.lang.Deprecated()
meth public boolean set(java.lang.Object,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean updateShape()
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.object.DynamicObject copy(com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
meth public final com.oracle.truffle.api.object.Shape getShape()
meth public int size()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object get(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object get(java.lang.Object,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public void define(java.lang.Object,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public void define(java.lang.Object,java.lang.Object,int)
 anno 0 java.lang.Deprecated()
meth public void define(java.lang.Object,java.lang.Object,int,com.oracle.truffle.api.object.LocationFactory)
 anno 0 java.lang.Deprecated()
meth public void setShapeAndGrow(com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
meth public void setShapeAndResize(com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds SHAPE_OFFSET,UNSAFE,extRef,extVal,shape

CLSS protected abstract interface static !annotation com.oracle.truffle.api.object.DynamicObject$DynamicField
 outer com.oracle.truffle.api.object.DynamicObject
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface com.oracle.truffle.api.object.DynamicObjectFactory
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
cons public init()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds serialVersionUID

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
cons public init()
supr com.oracle.truffle.api.nodes.SlowPathException
hfds serialVersionUID

CLSS public abstract interface com.oracle.truffle.api.object.IntLocation
intf com.oracle.truffle.api.object.TypedLocation
meth public abstract int getInt(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract int getInt(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract java.lang.Class<java.lang.Integer> getType()
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setInt(com.oracle.truffle.api.object.DynamicObject,int,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)

CLSS public abstract com.oracle.truffle.api.object.Layout
cons protected init()
fld public final static java.lang.String OPTION_PREFIX = "truffle.object."
innr protected abstract static Access
innr public final static !enum ImplicitCast
innr public final static Builder
meth protected com.oracle.truffle.api.object.Shape buildShape(java.lang.Object,java.lang.Object,int,com.oracle.truffle.api.Assumption)
meth protected static boolean getPolymorphicUnboxing(com.oracle.truffle.api.object.Layout$Builder)
meth protected static com.oracle.truffle.api.object.LayoutFactory getFactory()
meth protected static java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject> getType(com.oracle.truffle.api.object.Layout$Builder)
meth protected static java.util.EnumSet<com.oracle.truffle.api.object.Layout$ImplicitCast> getAllowedImplicitCasts(com.oracle.truffle.api.object.Layout$Builder)
meth public abstract com.oracle.truffle.api.object.DynamicObject newInstance(com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape createShape(com.oracle.truffle.api.object.ObjectType)
meth public abstract com.oracle.truffle.api.object.Shape createShape(com.oracle.truffle.api.object.ObjectType,java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Shape createShape(com.oracle.truffle.api.object.ObjectType,java.lang.Object,int)
meth public abstract com.oracle.truffle.api.object.Shape$Allocator createAllocator()
meth public abstract java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject> getType()
meth public static com.oracle.truffle.api.object.Layout createLayout()
meth public static com.oracle.truffle.api.object.Layout$Builder newLayout()
supr java.lang.Object
hfds LAYOUT_FACTORY

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
meth public com.oracle.truffle.api.object.Layout build()
meth public com.oracle.truffle.api.object.Layout$Builder addAllowedImplicitCast(com.oracle.truffle.api.object.Layout$ImplicitCast)
meth public com.oracle.truffle.api.object.Layout$Builder setAllowedImplicitCasts(java.util.EnumSet<com.oracle.truffle.api.object.Layout$ImplicitCast>)
meth public com.oracle.truffle.api.object.Layout$Builder setPolymorphicUnboxing(boolean)
 anno 0 java.lang.Deprecated()
meth public com.oracle.truffle.api.object.Layout$Builder type(java.lang.Class<? extends com.oracle.truffle.api.object.DynamicObject>)
supr java.lang.Object
hfds allowedImplicitCasts,dynamicObjectClass,polymorphicUnboxing

CLSS public final static !enum com.oracle.truffle.api.object.Layout$ImplicitCast
 outer com.oracle.truffle.api.object.Layout
fld public final static com.oracle.truffle.api.object.Layout$ImplicitCast IntToDouble
fld public final static com.oracle.truffle.api.object.Layout$ImplicitCast IntToLong
meth public static com.oracle.truffle.api.object.Layout$ImplicitCast valueOf(java.lang.String)
meth public static com.oracle.truffle.api.object.Layout$ImplicitCast[] values()
supr java.lang.Enum<com.oracle.truffle.api.object.Layout$ImplicitCast>

CLSS public abstract interface com.oracle.truffle.api.object.LayoutFactory
meth public abstract com.oracle.truffle.api.object.Layout createLayout(com.oracle.truffle.api.object.Layout$Builder)
meth public abstract com.oracle.truffle.api.object.Property createProperty(java.lang.Object,com.oracle.truffle.api.object.Location)
meth public abstract com.oracle.truffle.api.object.Property createProperty(java.lang.Object,com.oracle.truffle.api.object.Location,int)
meth public abstract int getPriority()

CLSS public abstract com.oracle.truffle.api.object.Location
cons protected init()
meth protected abstract java.lang.Object getInternal(com.oracle.truffle.api.object.DynamicObject)
meth protected abstract void setInternal(com.oracle.truffle.api.object.DynamicObject,java.lang.Object) throws com.oracle.truffle.api.object.IncompatibleLocationException
meth protected static boolean checkShape(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth protected static com.oracle.truffle.api.object.FinalLocationException finalLocation() throws com.oracle.truffle.api.object.FinalLocationException
meth protected static com.oracle.truffle.api.object.IncompatibleLocationException incompatibleLocation() throws com.oracle.truffle.api.object.IncompatibleLocationException
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public boolean canSet(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public boolean canSet(java.lang.Object)
meth public boolean canStore(java.lang.Object)
meth public boolean isAssumedFinal()
meth public boolean isConstant()
meth public boolean isDeclared()
meth public boolean isFinal()
meth public boolean isValue()
meth public com.oracle.truffle.api.Assumption getFinalAssumption()
meth public final java.lang.Object get(com.oracle.truffle.api.object.DynamicObject)
meth public final java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public final void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
meth public java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
meth public void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.IncompatibleLocationException
supr java.lang.Object

CLSS public abstract interface com.oracle.truffle.api.object.LocationFactory
meth public abstract com.oracle.truffle.api.object.Location createLocation(com.oracle.truffle.api.object.Shape,java.lang.Object)

CLSS public final !enum com.oracle.truffle.api.object.LocationModifier
fld public final static com.oracle.truffle.api.object.LocationModifier Final
fld public final static com.oracle.truffle.api.object.LocationModifier NonNull
meth public static com.oracle.truffle.api.object.LocationModifier valueOf(java.lang.String)
meth public static com.oracle.truffle.api.object.LocationModifier[] values()
supr java.lang.Enum<com.oracle.truffle.api.object.LocationModifier>

CLSS public abstract interface com.oracle.truffle.api.object.LongLocation
intf com.oracle.truffle.api.object.TypedLocation
meth public abstract java.lang.Class<java.lang.Long> getType()
meth public abstract long getLong(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract long getLong(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException
meth public abstract void setLong(com.oracle.truffle.api.object.DynamicObject,long,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)

CLSS public abstract interface com.oracle.truffle.api.object.ObjectLocation
intf com.oracle.truffle.api.object.TypedLocation
meth public abstract boolean isNonNull()
meth public abstract java.lang.Class<?> getType()

CLSS public com.oracle.truffle.api.object.ObjectType
cons public init()
meth public boolean equals(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
meth public int hashCode(com.oracle.truffle.api.object.DynamicObject)
meth public java.lang.Class<?> dispatch()
meth public java.lang.String toString(com.oracle.truffle.api.object.DynamicObject)
supr java.lang.Object
hfds DEFAULT

CLSS public abstract com.oracle.truffle.api.object.Property
cons protected init()
meth public abstract boolean isHidden()
meth public abstract boolean isSame(com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Location getLocation()
meth public abstract com.oracle.truffle.api.object.Property copyWithFlags(int)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Property copyWithRelocatable(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Property relocate(com.oracle.truffle.api.object.Location)
 anno 0 java.lang.Deprecated()
meth public abstract int getFlags()
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract java.lang.Object getKey()
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.IncompatibleLocationException
 anno 0 java.lang.Deprecated()
meth public abstract void setGeneric(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape)
meth public abstract void setGeneric(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
meth public abstract void setInternal(com.oracle.truffle.api.object.DynamicObject,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public abstract void setSafe(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape)
meth public abstract void setSafe(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape)
meth public static com.oracle.truffle.api.object.Property create(java.lang.Object,com.oracle.truffle.api.object.Location,int)
supr java.lang.Object

CLSS public abstract com.oracle.truffle.api.object.Shape
cons protected init()
innr public abstract interface static Pred
innr public abstract static Allocator
innr public final static Builder
innr public final static DerivedBuilder
meth protected boolean hasInstanceProperties()
meth protected com.oracle.truffle.api.object.Shape setDynamicType(java.lang.Object)
meth protected com.oracle.truffle.api.object.Shape setFlags(int)
meth public abstract boolean check(com.oracle.truffle.api.object.DynamicObject)
meth public abstract boolean hasProperty(java.lang.Object)
meth public abstract boolean hasTransitionWithKey(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public abstract boolean isLeaf()
meth public abstract boolean isRelated(com.oracle.truffle.api.object.Shape)
 anno 0 java.lang.Deprecated()
meth public abstract boolean isValid()
meth public abstract com.oracle.truffle.api.Assumption getLeafAssumption()
meth public abstract com.oracle.truffle.api.Assumption getValidAssumption()
meth public abstract com.oracle.truffle.api.object.DynamicObject newInstance()
meth public abstract com.oracle.truffle.api.object.DynamicObjectFactory createFactory()
meth public abstract com.oracle.truffle.api.object.Layout getLayout()
meth public abstract com.oracle.truffle.api.object.ObjectType getObjectType()
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Property getLastProperty()
meth public abstract com.oracle.truffle.api.object.Property getProperty(java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Shape addProperty(com.oracle.truffle.api.object.Property)
meth public abstract com.oracle.truffle.api.object.Shape append(com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape changeType(com.oracle.truffle.api.object.ObjectType)
meth public abstract com.oracle.truffle.api.object.Shape createSeparateShape(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape defineProperty(java.lang.Object,java.lang.Object,int)
meth public abstract com.oracle.truffle.api.object.Shape defineProperty(java.lang.Object,java.lang.Object,int,com.oracle.truffle.api.object.LocationFactory)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape getParent()
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape getRoot()
meth public abstract com.oracle.truffle.api.object.Shape removeProperty(com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape replaceProperty(com.oracle.truffle.api.object.Property,com.oracle.truffle.api.object.Property)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape reservePrimitiveExtensionArray()
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Shape tryMerge(com.oracle.truffle.api.object.Shape)
meth public abstract com.oracle.truffle.api.object.Shape$Allocator allocator()
meth public abstract int getId()
 anno 0 java.lang.Deprecated()
meth public abstract int getPropertyCount()
meth public abstract java.lang.Iterable<com.oracle.truffle.api.object.Property> getProperties()
meth public abstract java.lang.Iterable<java.lang.Object> getKeys()
meth public abstract java.lang.Object getMutex()
meth public abstract java.lang.Object getSharedData()
meth public abstract java.util.List<com.oracle.truffle.api.object.Property> getPropertyList()
meth public abstract java.util.List<com.oracle.truffle.api.object.Property> getPropertyList(com.oracle.truffle.api.object.Shape$Pred<com.oracle.truffle.api.object.Property>)
 anno 0 java.lang.Deprecated()
meth public abstract java.util.List<com.oracle.truffle.api.object.Property> getPropertyListInternal(boolean)
meth public abstract java.util.List<java.lang.Object> getKeyList()
meth public abstract java.util.List<java.lang.Object> getKeyList(com.oracle.truffle.api.object.Shape$Pred<com.oracle.truffle.api.object.Property>)
 anno 0 java.lang.Deprecated()
meth public boolean allPropertiesMatch(java.util.function.Predicate<com.oracle.truffle.api.object.Property>)
meth public boolean isShared()
meth public com.oracle.truffle.api.Assumption getPropertyAssumption(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape makeSharedShape()
meth public int getFlags()
meth public java.lang.Object getDynamicType()
meth public static com.oracle.truffle.api.object.Shape$Builder newBuilder()
meth public static com.oracle.truffle.api.object.Shape$DerivedBuilder newBuilder(com.oracle.truffle.api.object.Shape)
supr java.lang.Object
hfds OBJECT_FLAGS_MASK,OBJECT_FLAGS_SHIFT,OBJECT_PROPERTY_ASSUMPTIONS,OBJECT_SHARED
hcls AbstractBuilder

CLSS public abstract static com.oracle.truffle.api.object.Shape$Allocator
 outer com.oracle.truffle.api.object.Shape
cons protected init()
meth protected abstract com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>,boolean,boolean)
meth protected abstract com.oracle.truffle.api.object.Location locationForValue(java.lang.Object,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public abstract com.oracle.truffle.api.object.Location constantLocation(java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Location declaredLocation(java.lang.Object)
meth public abstract com.oracle.truffle.api.object.Shape$Allocator addLocation(com.oracle.truffle.api.object.Location)
meth public abstract com.oracle.truffle.api.object.Shape$Allocator copy()
meth public final com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>)
meth public final com.oracle.truffle.api.object.Location locationForType(java.lang.Class<?>,java.util.EnumSet<com.oracle.truffle.api.object.LocationModifier>)
meth public final com.oracle.truffle.api.object.Location locationForValue(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public final com.oracle.truffle.api.object.Location locationForValue(java.lang.Object,java.util.EnumSet<com.oracle.truffle.api.object.LocationModifier>)
 anno 0 java.lang.Deprecated()
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
hfds allowedImplicitCasts,dynamicType,layoutClass,properties,propertyAssumptions,shapeFlags,shared,sharedData,singleContextAssumption

CLSS public final static com.oracle.truffle.api.object.Shape$DerivedBuilder
 outer com.oracle.truffle.api.object.Shape
meth public com.oracle.truffle.api.object.Shape build()
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder addConstantProperty(java.lang.Object,java.lang.Object,int)
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder dynamicType(java.lang.Object)
meth public com.oracle.truffle.api.object.Shape$DerivedBuilder shapeFlags(int)
supr java.lang.Object<com.oracle.truffle.api.object.Shape$DerivedBuilder>
hfds baseShape,dynamicType,properties,shapeFlags

CLSS public abstract interface static com.oracle.truffle.api.object.Shape$Pred<%0 extends java.lang.Object>
 outer com.oracle.truffle.api.object.Shape
 anno 0 java.lang.Deprecated()
meth public abstract boolean test({com.oracle.truffle.api.object.Shape$Pred%0})

CLSS public abstract interface com.oracle.truffle.api.object.ShapeListener
 anno 0 java.lang.Deprecated()
meth public abstract void onPropertyTransition(java.lang.Object)

CLSS public abstract interface com.oracle.truffle.api.object.TypedLocation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Class<?> getType()
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,boolean)
meth public abstract java.lang.Object get(com.oracle.truffle.api.object.DynamicObject,com.oracle.truffle.api.object.Shape)
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.FinalLocationException,com.oracle.truffle.api.object.IncompatibleLocationException
meth public abstract void set(com.oracle.truffle.api.object.DynamicObject,java.lang.Object,com.oracle.truffle.api.object.Shape,com.oracle.truffle.api.object.Shape) throws com.oracle.truffle.api.object.IncompatibleLocationException

CLSS public abstract interface !annotation com.oracle.truffle.api.object.dsl.Layout
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public final static DispatchDefaultValue
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean implicitCastIntToDouble()
 anno 0 java.lang.Deprecated()
meth public abstract !hasdefault boolean implicitCastIntToLong()
 anno 0 java.lang.Deprecated()
meth public abstract !hasdefault java.lang.Class<? extends com.oracle.truffle.api.object.ObjectType> objectTypeSuperclass()
 anno 0 java.lang.Deprecated()
meth public abstract !hasdefault java.lang.Class<?> dispatch()
 anno 0 java.lang.Deprecated()

CLSS public final static com.oracle.truffle.api.object.dsl.Layout$DispatchDefaultValue
 outer com.oracle.truffle.api.object.dsl.Layout
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public abstract interface !annotation com.oracle.truffle.api.object.dsl.Nullable
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation com.oracle.truffle.api.object.dsl.Volatile
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract com.oracle.truffle.api.profiles.BranchProfile
meth public abstract void enter()
meth public static com.oracle.truffle.api.profiles.BranchProfile create()
meth public static com.oracle.truffle.api.profiles.BranchProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.ByteValueProfile
meth public abstract byte profile(byte)
meth public static com.oracle.truffle.api.profiles.ByteValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.ByteValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.ConditionProfile
meth public abstract boolean profile(boolean)
meth public static com.oracle.truffle.api.profiles.ConditionProfile create()
meth public static com.oracle.truffle.api.profiles.ConditionProfile createBinaryProfile()
meth public static com.oracle.truffle.api.profiles.ConditionProfile createCountingProfile()
meth public static com.oracle.truffle.api.profiles.ConditionProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Binary,Counting,Disabled

CLSS public abstract com.oracle.truffle.api.profiles.DoubleValueProfile
meth public abstract double profile(double)
meth public static com.oracle.truffle.api.profiles.DoubleValueProfile createRawIdentityProfile()
meth public static com.oracle.truffle.api.profiles.DoubleValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.FloatValueProfile
meth public abstract float profile(float)
meth public static com.oracle.truffle.api.profiles.FloatValueProfile createRawIdentityProfile()
meth public static com.oracle.truffle.api.profiles.FloatValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.IntValueProfile
meth public abstract int profile(int)
meth public static com.oracle.truffle.api.profiles.IntValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.IntValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.LongValueProfile
meth public abstract long profile(long)
meth public static com.oracle.truffle.api.profiles.LongValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.LongValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.LoopConditionProfile
meth public abstract boolean inject(boolean)
meth public abstract boolean profile(boolean)
meth public abstract void profileCounted(long)
meth public static com.oracle.truffle.api.profiles.LoopConditionProfile createCountingProfile()
meth public static com.oracle.truffle.api.profiles.LoopConditionProfile getUncached()
supr com.oracle.truffle.api.profiles.ConditionProfile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.PrimitiveValueProfile
meth public abstract <%0 extends java.lang.Object> {%%0} profile({%%0})
meth public abstract boolean profile(boolean)
meth public abstract byte profile(byte)
meth public abstract char profile(char)
meth public abstract double profile(double)
meth public abstract float profile(float)
meth public abstract int profile(int)
meth public abstract long profile(long)
meth public abstract short profile(short)
meth public static com.oracle.truffle.api.profiles.PrimitiveValueProfile createEqualityProfile()
meth public static com.oracle.truffle.api.profiles.PrimitiveValueProfile getUncached()
supr com.oracle.truffle.api.profiles.ValueProfile
hcls Disabled,Enabled

CLSS public abstract com.oracle.truffle.api.profiles.Profile
supr com.oracle.truffle.api.nodes.NodeCloneable

CLSS public abstract com.oracle.truffle.api.profiles.ValueProfile
meth public abstract <%0 extends java.lang.Object> {%%0} profile({%%0})
meth public static com.oracle.truffle.api.profiles.ValueProfile createClassProfile()
meth public static com.oracle.truffle.api.profiles.ValueProfile createEqualityProfile()
meth public static com.oracle.truffle.api.profiles.ValueProfile createIdentityProfile()
meth public static com.oracle.truffle.api.profiles.ValueProfile getUncached()
supr com.oracle.truffle.api.profiles.Profile
hcls Disabled,Equality,ExactClass,Identity

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
hfds ALLOW_IO,BUFFER_SIZE,BYTE_SEQUENCE_CLASS,CONTENT_UNSET,EMPTY,MAX_BUFFER_SIZE,NO_FASTPATH_SUBSOURCE_CREATION_MESSAGE,SOURCES,URI_SCHEME,cachedPolyglotSource,computedURI,textMap

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
hfds cached,canonicalizePath,content,fileEncoding,fileSystemContext,interactive,internal,language,mimeType,name,origin,path,uri,url

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

CLSS public final com.oracle.truffle.api.utilities.AlwaysValidAssumption
fld public final static com.oracle.truffle.api.utilities.AlwaysValidAssumption INSTANCE
intf com.oracle.truffle.api.Assumption
meth public boolean isValid()
meth public java.lang.String getName()
meth public void check() throws com.oracle.truffle.api.nodes.InvalidAssumptionException
meth public void invalidate()
meth public void invalidate(java.lang.String)
supr java.lang.Object

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

CLSS public final com.oracle.truffle.api.utilities.NeverValidAssumption
fld public final static com.oracle.truffle.api.utilities.NeverValidAssumption INSTANCE
intf com.oracle.truffle.api.Assumption
meth public boolean isValid()
meth public java.lang.String getName()
meth public void check() throws com.oracle.truffle.api.nodes.InvalidAssumptionException
meth public void invalidate()
meth public void invalidate(java.lang.String)
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

CLSS public com.oracle.truffle.api.utilities.UnionAssumption
cons public init(com.oracle.truffle.api.Assumption,com.oracle.truffle.api.Assumption)
cons public init(java.lang.String,com.oracle.truffle.api.Assumption,com.oracle.truffle.api.Assumption)
intf com.oracle.truffle.api.Assumption
meth public boolean isValid()
meth public java.lang.String getName()
meth public void check() throws com.oracle.truffle.api.nodes.InvalidAssumptionException
meth public void invalidate()
meth public void invalidate(java.lang.String)
supr java.lang.Object
hfds first,name,second

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

CLSS public abstract java.lang.ref.Reference<%0 extends java.lang.Object>
meth public boolean enqueue()
meth public boolean isEnqueued()
meth public void clear()
meth public {java.lang.ref.Reference%0} get()
supr java.lang.Object

CLSS public java.lang.ref.WeakReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.WeakReference%0})
cons public init({java.lang.ref.WeakReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.WeakReference%0}>)
supr java.lang.ref.Reference<{java.lang.ref.WeakReference%0}>

CLSS public abstract interface java.util.function.Predicate<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.Predicate%0})
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> and(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> negate()
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> or(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Predicate<{%%0}> isEqual(java.lang.Object)

