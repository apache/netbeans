#Signature file v4.1
#Version 1.12.0

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object
hfds MAX_SKIP_BUFFER_SIZE

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

CLSS public jdk.internal.jshell.debug.InternalDebugControl
fld public final static int DBG_COMPA = 4
fld public final static int DBG_DEP = 8
fld public final static int DBG_EVNT = 16
fld public final static int DBG_FMGR = 2
fld public final static int DBG_GEN = 1
fld public final static int DBG_WRAP = 32
meth public !varargs static void debug(jdk.jshell.JShell,java.io.PrintStream,int,java.lang.String,java.lang.Object[])
meth public static boolean isDebugEnabled(jdk.jshell.JShell,int)
meth public static void debug(jdk.jshell.JShell,java.io.PrintStream,java.lang.Exception,java.lang.String)
meth public static void release(jdk.jshell.JShell)
meth public static void setDebugFlags(jdk.jshell.JShell,int)
supr java.lang.Object
hfds debugMap

CLSS public abstract interface jdk.internal.jshell.tool.MessageHandler
meth public abstract !varargs void errormsg(java.lang.String,java.lang.Object[])
meth public abstract !varargs void fluff(java.lang.String,java.lang.Object[])
meth public abstract !varargs void fluffmsg(java.lang.String,java.lang.Object[])
meth public abstract !varargs void hard(java.lang.String,java.lang.Object[])
meth public abstract !varargs void hardmsg(java.lang.String,java.lang.Object[])
meth public abstract boolean showFluff()

CLSS public final jdk.internal.jshell.tool.StopDetectingInputStream
cons public init(java.lang.Runnable,java.util.function.Consumer<java.lang.Exception>)
fld public final static int INITIAL_SIZE = 128
innr public final static !enum State
meth public int read()
meth public java.io.InputStream setInputStream(java.io.InputStream)
meth public void setState(jdk.internal.jshell.tool.StopDetectingInputStream$State)
meth public void shutdown()
meth public void write(int)
supr java.io.InputStream
hfds buffer,end,errorHandler,initialized,start,state,stop

CLSS public final static !enum jdk.internal.jshell.tool.StopDetectingInputStream$State
 outer jdk.internal.jshell.tool.StopDetectingInputStream
fld public final static jdk.internal.jshell.tool.StopDetectingInputStream$State BUFFER
fld public final static jdk.internal.jshell.tool.StopDetectingInputStream$State CLOSED
fld public final static jdk.internal.jshell.tool.StopDetectingInputStream$State READ
fld public final static jdk.internal.jshell.tool.StopDetectingInputStream$State WAIT
meth public static jdk.internal.jshell.tool.StopDetectingInputStream$State valueOf(java.lang.String)
meth public static jdk.internal.jshell.tool.StopDetectingInputStream$State[] values()
supr java.lang.Enum<jdk.internal.jshell.tool.StopDetectingInputStream$State>

CLSS public abstract jdk.jshell.DeclarationSnippet
supr jdk.jshell.PersistentSnippet
hfds bodyReferences,corralled,declareReferences

CLSS public abstract jdk.jshell.Diag
fld public final static long NOPOS = -1
meth public abstract boolean isError()
meth public abstract java.lang.String getCode()
meth public abstract java.lang.String getMessage(java.util.Locale)
meth public abstract long getEndPosition()
meth public abstract long getPosition()
meth public abstract long getStartPosition()
supr java.lang.Object

CLSS public jdk.jshell.ErroneousSnippet
meth public jdk.jshell.Snippet$Kind probableKind()
supr jdk.jshell.Snippet
hfds probableKind

CLSS public jdk.jshell.EvalException
meth public java.lang.String getExceptionClassName()
supr jdk.jshell.JShellException
hfds exceptionClass

CLSS public jdk.jshell.ExpressionSnippet
meth public java.lang.String name()
meth public java.lang.String typeName()
supr jdk.jshell.Snippet

CLSS public jdk.jshell.ImportSnippet
meth public boolean isStatic()
meth public java.lang.String fullname()
meth public java.lang.String name()
supr jdk.jshell.PersistentSnippet
hfds fullkey,fullname,isStar,isStatic

CLSS public jdk.jshell.JShell
innr public Subscription
innr public static Builder
intf java.lang.AutoCloseable
meth public java.lang.String varValue(jdk.jshell.VarSnippet)
meth public java.util.List<jdk.jshell.SnippetEvent> drop(jdk.jshell.Snippet)
meth public java.util.List<jdk.jshell.SnippetEvent> eval(java.lang.String)
meth public java.util.stream.Stream<java.lang.String> unresolvedDependencies(jdk.jshell.DeclarationSnippet)
meth public java.util.stream.Stream<jdk.jshell.Diag> diagnostics(jdk.jshell.Snippet)
meth public java.util.stream.Stream<jdk.jshell.ImportSnippet> imports()
meth public java.util.stream.Stream<jdk.jshell.MethodSnippet> methods()
meth public java.util.stream.Stream<jdk.jshell.Snippet> snippets()
meth public java.util.stream.Stream<jdk.jshell.TypeDeclSnippet> types()
meth public java.util.stream.Stream<jdk.jshell.VarSnippet> variables()
meth public jdk.jshell.JShell$Subscription onShutdown(java.util.function.Consumer<jdk.jshell.JShell>)
meth public jdk.jshell.JShell$Subscription onSnippetEvent(java.util.function.Consumer<jdk.jshell.SnippetEvent>)
meth public jdk.jshell.Snippet$Status status(jdk.jshell.Snippet)
meth public jdk.jshell.SourceCodeAnalysis sourceCodeAnalysis()
meth public static jdk.jshell.JShell create()
meth public static jdk.jshell.JShell$Builder builder()
meth public void addToClasspath(java.lang.String)
meth public void close()
meth public void stop()
meth public void unsubscribe(jdk.jshell.JShell$Subscription)
supr java.lang.Object
hfds L10N_RB_NAME,classTracker,closed,err,eval,executionControl,extraCompilerOptions,extraRemoteVMOptions,fileManagerMapping,idGenerator,in,keyMap,keyStatusListeners,maps,nextKeyIndex,out,outerMap,outputRB,shutdownListeners,sourceCodeAnalysis,taskFactory,tempVariableNameGenerator
hcls ExecutionEnvImpl

CLSS public static jdk.jshell.JShell$Builder
 outer jdk.jshell.JShell
meth public !varargs jdk.jshell.JShell$Builder compilerOptions(java.lang.String[])
meth public !varargs jdk.jshell.JShell$Builder remoteVMOptions(java.lang.String[])
meth public jdk.jshell.JShell build()
meth public jdk.jshell.JShell$Builder err(java.io.PrintStream)
meth public jdk.jshell.JShell$Builder executionEngine(java.lang.String)
meth public jdk.jshell.JShell$Builder executionEngine(jdk.jshell.spi.ExecutionControlProvider,java.util.Map<java.lang.String,java.lang.String>)
meth public jdk.jshell.JShell$Builder fileManager(java.util.function.Function<javax.tools.StandardJavaFileManager,javax.tools.StandardJavaFileManager>)
meth public jdk.jshell.JShell$Builder fileManager(javax.tools.StandardJavaFileManager)
meth public jdk.jshell.JShell$Builder idGenerator(java.util.function.BiFunction<jdk.jshell.Snippet,java.lang.Integer,java.lang.String>)
meth public jdk.jshell.JShell$Builder in(java.io.InputStream)
meth public jdk.jshell.JShell$Builder out(java.io.PrintStream)
meth public jdk.jshell.JShell$Builder tempVariableNameGenerator(java.util.function.Supplier<java.lang.String>)
supr java.lang.Object
hfds err,executionControlParameters,executionControlProvider,executionControlSpec,extraCompilerOptions,extraRemoteVMOptions,fileManagerMapping,idGenerator,in,jfm,out,tempVariableNameGenerator

CLSS public jdk.jshell.JShell$Subscription
 outer jdk.jshell.JShell
supr java.lang.Object
hfds remover

CLSS public jdk.jshell.JShellException
supr java.lang.Exception

CLSS public jdk.jshell.MethodSnippet
meth public java.lang.String parameterTypes()
meth public java.lang.String signature()
meth public java.lang.String toString()
supr jdk.jshell.DeclarationSnippet
hfds qualifiedParamaterTypes,signature

CLSS public abstract jdk.jshell.PersistentSnippet
meth public java.lang.String name()
supr jdk.jshell.Snippet

CLSS public abstract jdk.jshell.Snippet
innr public final static !enum Kind
innr public final static !enum Status
innr public final static !enum SubKind
meth public java.lang.String id()
meth public java.lang.String source()
meth public java.lang.String toString()
meth public jdk.jshell.Snippet$Kind kind()
meth public jdk.jshell.Snippet$SubKind subKind()
supr java.lang.Object
hfds diagnostics,guts,id,key,outer,seq,source,status,subkind,syntheticDiags,unitName,unresolved

CLSS public final static !enum jdk.jshell.Snippet$Kind
 outer jdk.jshell.Snippet
fld public final static jdk.jshell.Snippet$Kind ERRONEOUS
fld public final static jdk.jshell.Snippet$Kind EXPRESSION
fld public final static jdk.jshell.Snippet$Kind IMPORT
fld public final static jdk.jshell.Snippet$Kind METHOD
fld public final static jdk.jshell.Snippet$Kind STATEMENT
fld public final static jdk.jshell.Snippet$Kind TYPE_DECL
fld public final static jdk.jshell.Snippet$Kind VAR
meth public boolean isPersistent()
meth public static jdk.jshell.Snippet$Kind valueOf(java.lang.String)
meth public static jdk.jshell.Snippet$Kind[] values()
supr java.lang.Enum<jdk.jshell.Snippet$Kind>
hfds isPersistent

CLSS public final static !enum jdk.jshell.Snippet$Status
 outer jdk.jshell.Snippet
fld public final static jdk.jshell.Snippet$Status DROPPED
fld public final static jdk.jshell.Snippet$Status NONEXISTENT
fld public final static jdk.jshell.Snippet$Status OVERWRITTEN
fld public final static jdk.jshell.Snippet$Status RECOVERABLE_DEFINED
fld public final static jdk.jshell.Snippet$Status RECOVERABLE_NOT_DEFINED
fld public final static jdk.jshell.Snippet$Status REJECTED
fld public final static jdk.jshell.Snippet$Status VALID
meth public boolean isActive()
meth public boolean isDefined()
meth public static jdk.jshell.Snippet$Status valueOf(java.lang.String)
meth public static jdk.jshell.Snippet$Status[] values()
supr java.lang.Enum<jdk.jshell.Snippet$Status>
hfds isActive,isDefined

CLSS public final static !enum jdk.jshell.Snippet$SubKind
 outer jdk.jshell.Snippet
fld public final static jdk.jshell.Snippet$SubKind ANNOTATION_TYPE_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind ASSIGNMENT_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind CLASS_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind ENUM_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind INTERFACE_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind METHOD_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind OTHER_EXPRESSION_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind SINGLE_STATIC_IMPORT_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind SINGLE_TYPE_IMPORT_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind STATEMENT_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind STATIC_IMPORT_ON_DEMAND_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind TEMP_VAR_EXPRESSION_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind TYPE_IMPORT_ON_DEMAND_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind UNKNOWN_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind VAR_DECLARATION_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind VAR_DECLARATION_WITH_INITIALIZER_SUBKIND
fld public final static jdk.jshell.Snippet$SubKind VAR_VALUE_SUBKIND
meth public boolean hasValue()
meth public boolean isExecutable()
meth public jdk.jshell.Snippet$Kind kind()
meth public static jdk.jshell.Snippet$SubKind valueOf(java.lang.String)
meth public static jdk.jshell.Snippet$SubKind[] values()
supr java.lang.Enum<jdk.jshell.Snippet$SubKind>
hfds hasValue,isExecutable,kind

CLSS public jdk.jshell.SnippetEvent
meth public boolean isSignatureChange()
meth public java.lang.String toString()
meth public java.lang.String value()
meth public jdk.jshell.JShellException exception()
meth public jdk.jshell.Snippet causeSnippet()
meth public jdk.jshell.Snippet snippet()
meth public jdk.jshell.Snippet$Status previousStatus()
meth public jdk.jshell.Snippet$Status status()
supr java.lang.Object
hfds causeSnippet,exception,isSignatureChange,previousStatus,snippet,status,value

CLSS public abstract jdk.jshell.SourceCodeAnalysis
innr public abstract interface static CompletionInfo
innr public abstract interface static Documentation
innr public abstract interface static SnippetWrapper
innr public abstract interface static Suggestion
innr public final static !enum Completeness
innr public final static QualifiedNames
meth public abstract java.lang.String analyzeType(java.lang.String,int)
meth public abstract java.util.Collection<jdk.jshell.Snippet> dependents(jdk.jshell.Snippet)
meth public abstract java.util.List<jdk.jshell.SourceCodeAnalysis$Documentation> documentation(java.lang.String,int,boolean)
meth public abstract java.util.List<jdk.jshell.SourceCodeAnalysis$SnippetWrapper> wrappers(java.lang.String)
meth public abstract java.util.List<jdk.jshell.SourceCodeAnalysis$Suggestion> completionSuggestions(java.lang.String,int,int[])
meth public abstract jdk.jshell.SourceCodeAnalysis$CompletionInfo analyzeCompletion(java.lang.String)
meth public abstract jdk.jshell.SourceCodeAnalysis$QualifiedNames listQualifiedNames(java.lang.String,int)
meth public abstract jdk.jshell.SourceCodeAnalysis$SnippetWrapper wrapper(jdk.jshell.Snippet)
supr java.lang.Object

CLSS public final static !enum jdk.jshell.SourceCodeAnalysis$Completeness
 outer jdk.jshell.SourceCodeAnalysis
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness COMPLETE
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness COMPLETE_WITH_SEMI
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness CONSIDERED_INCOMPLETE
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness DEFINITELY_INCOMPLETE
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness EMPTY
fld public final static jdk.jshell.SourceCodeAnalysis$Completeness UNKNOWN
meth public boolean isComplete()
meth public static jdk.jshell.SourceCodeAnalysis$Completeness valueOf(java.lang.String)
meth public static jdk.jshell.SourceCodeAnalysis$Completeness[] values()
supr java.lang.Enum<jdk.jshell.SourceCodeAnalysis$Completeness>
hfds isComplete

CLSS public abstract interface static jdk.jshell.SourceCodeAnalysis$CompletionInfo
 outer jdk.jshell.SourceCodeAnalysis
meth public abstract java.lang.String remaining()
meth public abstract java.lang.String source()
meth public abstract jdk.jshell.SourceCodeAnalysis$Completeness completeness()

CLSS public abstract interface static jdk.jshell.SourceCodeAnalysis$Documentation
 outer jdk.jshell.SourceCodeAnalysis
meth public abstract java.lang.String javadoc()
meth public abstract java.lang.String signature()

CLSS public final static jdk.jshell.SourceCodeAnalysis$QualifiedNames
 outer jdk.jshell.SourceCodeAnalysis
meth public boolean isResolvable()
meth public boolean isUpToDate()
meth public int getSimpleNameLength()
meth public java.util.List<java.lang.String> getNames()
supr java.lang.Object
hfds names,resolvable,simpleNameLength,upToDate

CLSS public abstract interface static jdk.jshell.SourceCodeAnalysis$SnippetWrapper
 outer jdk.jshell.SourceCodeAnalysis
meth public abstract int sourceToWrappedPosition(int)
meth public abstract int wrappedToSourcePosition(int)
meth public abstract java.lang.String fullClassName()
meth public abstract java.lang.String source()
meth public abstract java.lang.String wrapped()
meth public abstract jdk.jshell.Snippet$Kind kind()

CLSS public abstract interface static jdk.jshell.SourceCodeAnalysis$Suggestion
 outer jdk.jshell.SourceCodeAnalysis
meth public abstract boolean matchesType()
meth public abstract java.lang.String continuation()

CLSS public jdk.jshell.StatementSnippet
supr jdk.jshell.Snippet

CLSS public jdk.jshell.TypeDeclSnippet
supr jdk.jshell.DeclarationSnippet

CLSS public jdk.jshell.UnresolvedReferenceException
meth public jdk.jshell.DeclarationSnippet getSnippet()
supr jdk.jshell.JShellException
hfds snippet

CLSS public jdk.jshell.VarSnippet
meth public java.lang.String typeName()
supr jdk.jshell.DeclarationSnippet
hfds typeName

CLSS public jdk.jshell.execution.DirectExecutionControl
cons public init()
cons public init(jdk.jshell.execution.LoaderDelegate)
intf jdk.jshell.spi.ExecutionControl
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.String invoke(java.lang.reflect.Method) throws java.lang.Exception
meth protected java.lang.String throwConvertedInvocationException(java.lang.Throwable) throws jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth protected java.lang.String throwConvertedOtherException(java.lang.Throwable) throws jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth protected static java.lang.String valueString(java.lang.Object)
meth protected void clientCodeEnter() throws jdk.jshell.spi.ExecutionControl$InternalException
meth protected void clientCodeLeave() throws jdk.jshell.spi.ExecutionControl$InternalException
meth public java.lang.Object extensionCommand(java.lang.String,java.lang.Object) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public java.lang.String invoke(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public java.lang.String varValue(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public void addToClasspath(java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
meth public void close()
meth public void load(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public void redefine(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
supr java.lang.Object
hfds charRep,loaderDelegate

CLSS public jdk.jshell.execution.FailOverExecutionControlProvider
cons public init()
intf jdk.jshell.spi.ExecutionControlProvider
meth public java.lang.String name()
meth public java.util.Map<java.lang.String,java.lang.String> defaultParameters()
meth public jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.util.Map<java.lang.String,java.lang.String>) throws java.lang.Throwable
supr java.lang.Object
hfds logger

CLSS public jdk.jshell.execution.JdiDefaultExecutionControl
meth protected com.sun.jdi.VirtualMachine vm() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException
meth public java.lang.String invoke(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public void close()
meth public void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
supr jdk.jshell.execution.JdiExecutionControl
hfds STOP_LOCK,process,remoteAgent,userCodeRunning,vm

CLSS public abstract jdk.jshell.execution.JdiExecutionControl
cons protected init(java.io.ObjectOutput,java.io.ObjectInput)
intf jdk.jshell.spi.ExecutionControl
meth protected abstract com.sun.jdi.VirtualMachine vm() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException
meth protected com.sun.jdi.ReferenceType referenceType(com.sun.jdi.VirtualMachine,java.lang.String)
meth public void redefine(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException
supr jdk.jshell.execution.StreamingExecutionControl
hfds toReferenceType

CLSS public jdk.jshell.execution.JdiExecutionControlProvider
cons public init()
fld public final static java.lang.String PARAM_HOST_NAME = "hostname"
fld public final static java.lang.String PARAM_LAUNCH = "launch"
fld public final static java.lang.String PARAM_REMOTE_AGENT = "remoteAgent"
fld public final static java.lang.String PARAM_TIMEOUT = "timeout"
intf jdk.jshell.spi.ExecutionControlProvider
meth public java.lang.String name()
meth public java.util.Map<java.lang.String,java.lang.String> defaultParameters()
meth public jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT_TIMEOUT

CLSS public jdk.jshell.execution.JdiInitiator
cons public init(int,java.util.List<java.lang.String>,java.lang.String,boolean,java.lang.String,int,java.util.Map<java.lang.String,java.lang.String>)
meth public com.sun.jdi.VirtualMachine vm()
meth public java.lang.Process process()
supr java.lang.Object
hfds CONNECT_TIMEOUT_FACTOR,connectTimeout,connector,connectorArgs,process,remoteAgent,vm

CLSS public abstract interface jdk.jshell.execution.LoaderDelegate
meth public abstract java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public abstract void addToClasspath(java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
meth public abstract void load(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException

CLSS public jdk.jshell.execution.LocalExecutionControl
cons public init()
cons public init(jdk.jshell.execution.LoaderDelegate)
meth protected java.lang.String invoke(java.lang.reflect.Method) throws java.lang.Exception
meth protected void clientCodeEnter()
meth protected void clientCodeLeave()
meth public void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
supr jdk.jshell.execution.DirectExecutionControl
hfds STOP_LOCK,execThreadGroup,userCodeRunning

CLSS public jdk.jshell.execution.LocalExecutionControlProvider
cons public init()
intf jdk.jshell.spi.ExecutionControlProvider
meth public java.lang.String name()
meth public java.util.Map<java.lang.String,java.lang.String> defaultParameters()
meth public jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object

CLSS public jdk.jshell.execution.RemoteExecutionControl
cons public init()
cons public init(jdk.jshell.execution.LoaderDelegate)
intf jdk.jshell.spi.ExecutionControl
meth protected java.lang.String invoke(java.lang.reflect.Method) throws java.lang.Exception
meth protected java.lang.String throwConvertedInvocationException(java.lang.Throwable) throws jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth protected java.lang.String throwConvertedOtherException(java.lang.Throwable) throws jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth protected void clientCodeEnter()
meth protected void clientCodeLeave() throws jdk.jshell.spi.ExecutionControl$InternalException
meth public java.lang.String varValue(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
supr jdk.jshell.execution.DirectExecutionControl
hfds expectingStop,inClientCode,stopException
hcls StopExecutionException

CLSS public jdk.jshell.execution.StreamingExecutionControl
cons public init(java.io.ObjectOutput,java.io.ObjectInput)
intf jdk.jshell.spi.ExecutionControl
meth public java.lang.Object extensionCommand(java.lang.String,java.lang.Object) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public java.lang.String invoke(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public java.lang.String varValue(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public void addToClasspath(java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
meth public void close()
meth public void load(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public void redefine(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
supr java.lang.Object
hfds in,out

CLSS public jdk.jshell.execution.Util
meth public static jdk.jshell.spi.ExecutionControl remoteInputOutput(java.io.InputStream,java.io.OutputStream,java.util.Map<java.lang.String,java.io.OutputStream>,java.util.Map<java.lang.String,java.io.InputStream>,java.util.function.BiFunction<java.io.ObjectInput,java.io.ObjectOutput,jdk.jshell.spi.ExecutionControl>) throws java.io.IOException
meth public static void detectJdiExitEvent(com.sun.jdi.VirtualMachine,java.util.function.Consumer<java.lang.String>)
meth public static void forwardExecutionControl(jdk.jshell.spi.ExecutionControl,java.io.ObjectInput,java.io.ObjectOutput)
meth public static void forwardExecutionControlAndIO(jdk.jshell.spi.ExecutionControl,java.io.InputStream,java.io.OutputStream,java.util.Map<java.lang.String,java.util.function.Consumer<java.io.OutputStream>>,java.util.Map<java.lang.String,java.util.function.Consumer<java.io.InputStream>>) throws java.io.IOException
supr java.lang.Object
hfds TAG_CLOSED,TAG_DATA,TAG_EXCEPTION

CLSS abstract interface jdk.jshell.execution.package-info

CLSS abstract interface jdk.jshell.package-info

CLSS public abstract interface jdk.jshell.spi.ExecutionControl
innr public abstract static ExecutionControlException
innr public abstract static RunException
innr public final static ClassBytecodes
innr public static ClassInstallException
innr public static EngineTerminationException
innr public static InternalException
innr public static NotImplementedException
innr public static ResolutionException
innr public static StoppedException
innr public static UserException
intf java.lang.AutoCloseable
meth public abstract java.lang.Object extensionCommand(java.lang.String,java.lang.Object) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public abstract java.lang.String invoke(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public abstract java.lang.String varValue(java.lang.String,java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException,jdk.jshell.spi.ExecutionControl$RunException
meth public abstract void addToClasspath(java.lang.String) throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
meth public abstract void close()
meth public abstract void load(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public abstract void redefine(jdk.jshell.spi.ExecutionControl$ClassBytecodes[]) throws jdk.jshell.spi.ExecutionControl$ClassInstallException,jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$NotImplementedException
meth public abstract void stop() throws jdk.jshell.spi.ExecutionControl$EngineTerminationException,jdk.jshell.spi.ExecutionControl$InternalException
meth public static jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.lang.String) throws java.lang.Throwable
meth public static jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws java.lang.Throwable

CLSS public final static jdk.jshell.spi.ExecutionControl$ClassBytecodes
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String,byte[])
intf java.io.Serializable
meth public byte[] bytecodes()
meth public java.lang.String name()
supr java.lang.Object
hfds bytecodes,name,serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$ClassInstallException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String,boolean[])
meth public boolean[] installed()
supr jdk.jshell.spi.ExecutionControl$ExecutionControlException
hfds installed,serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$EngineTerminationException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String)
supr jdk.jshell.spi.ExecutionControl$ExecutionControlException
hfds serialVersionUID

CLSS public abstract static jdk.jshell.spi.ExecutionControl$ExecutionControlException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$InternalException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String)
supr jdk.jshell.spi.ExecutionControl$ExecutionControlException
hfds serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$NotImplementedException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String)
supr jdk.jshell.spi.ExecutionControl$InternalException
hfds serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$ResolutionException
 outer jdk.jshell.spi.ExecutionControl
cons public init(int,java.lang.StackTraceElement[])
meth public int id()
supr jdk.jshell.spi.ExecutionControl$RunException
hfds id,serialVersionUID

CLSS public abstract static jdk.jshell.spi.ExecutionControl$RunException
 outer jdk.jshell.spi.ExecutionControl
supr jdk.jshell.spi.ExecutionControl$ExecutionControlException
hfds serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$StoppedException
 outer jdk.jshell.spi.ExecutionControl
cons public init()
supr jdk.jshell.spi.ExecutionControl$RunException
hfds serialVersionUID

CLSS public static jdk.jshell.spi.ExecutionControl$UserException
 outer jdk.jshell.spi.ExecutionControl
cons public init(java.lang.String,java.lang.String,java.lang.StackTraceElement[])
meth public java.lang.String causeExceptionClass()
supr jdk.jshell.spi.ExecutionControl$RunException
hfds causeExceptionClass,serialVersionUID

CLSS public abstract interface jdk.jshell.spi.ExecutionControlProvider
meth public abstract java.lang.String name()
meth public abstract jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv,java.util.Map<java.lang.String,java.lang.String>) throws java.lang.Throwable
meth public java.util.Map<java.lang.String,java.lang.String> defaultParameters()

CLSS public abstract interface jdk.jshell.spi.ExecutionEnv
meth public abstract java.io.InputStream userIn()
meth public abstract java.io.PrintStream userErr()
meth public abstract java.io.PrintStream userOut()
meth public abstract java.util.List<java.lang.String> extraRemoteVMOptions()
meth public abstract void closeDown()

CLSS public jdk.jshell.spi.SPIResolutionException
cons public init(int)
meth public int id()
supr java.lang.RuntimeException
hfds id

CLSS abstract interface jdk.jshell.spi.package-info

