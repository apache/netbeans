#Signature file v4.1
#Version 0.15

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

CLSS public final org.netbeans.modules.nativeimage.api.Location
innr public final static Builder
meth public int getColumn()
meth public int getLine()
meth public java.lang.String toString()
meth public long getPC()
meth public static org.netbeans.modules.nativeimage.api.Location$Builder newBuilder()
supr java.lang.Object
hfds column,line,pc

CLSS public final static org.netbeans.modules.nativeimage.api.Location$Builder
 outer org.netbeans.modules.nativeimage.api.Location
meth public org.netbeans.modules.nativeimage.api.Location build()
meth public void column(int)
meth public void line(int)
meth public void pc(long)
supr java.lang.Object
hfds column,line,pc

CLSS public final org.netbeans.modules.nativeimage.api.SourceInfo
innr public final static Builder
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getFileName()
meth public java.lang.String getFullName()
meth public java.lang.String toString()
meth public static org.netbeans.modules.nativeimage.api.SourceInfo$Builder newBuilder()
supr java.lang.Object
hfds fileName,fullName

CLSS public final static org.netbeans.modules.nativeimage.api.SourceInfo$Builder
 outer org.netbeans.modules.nativeimage.api.SourceInfo
meth public org.netbeans.modules.nativeimage.api.SourceInfo build()
meth public void fileName(java.lang.String)
meth public void fullName(java.lang.String)
supr java.lang.Object
hfds fileName,fullName

CLSS public final org.netbeans.modules.nativeimage.api.Symbol
innr public final static Builder
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public static org.netbeans.modules.nativeimage.api.Symbol$Builder newBuilder()
supr java.lang.Object
hfds description,name,type

CLSS public final static org.netbeans.modules.nativeimage.api.Symbol$Builder
 outer org.netbeans.modules.nativeimage.api.Symbol
meth public org.netbeans.modules.nativeimage.api.Symbol build()
meth public void description(java.lang.String)
meth public void name(java.lang.String)
meth public void type(java.lang.String)
supr java.lang.Object
hfds description,name,type

CLSS public org.netbeans.modules.nativeimage.api.debug.EvaluateException
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public final org.netbeans.modules.nativeimage.api.debug.NIDebugger
innr public final static Builder
meth public java.lang.String getVersion()
meth public java.lang.String readMemory(java.lang.String,long,int)
meth public java.util.List<org.netbeans.modules.nativeimage.api.Location> listLocations(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.Map<org.netbeans.modules.nativeimage.api.SourceInfo,java.util.List<org.netbeans.modules.nativeimage.api.Symbol>> listFunctions(java.lang.String,boolean,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.Map<org.netbeans.modules.nativeimage.api.SourceInfo,java.util.List<org.netbeans.modules.nativeimage.api.Symbol>> listVariables(java.lang.String,boolean,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.concurrent.CompletableFuture<java.lang.Void> attach(java.lang.String,long,java.lang.String,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
 anno 0 java.lang.Deprecated()
meth public java.util.concurrent.CompletableFuture<java.lang.Void> start(java.util.List<java.lang.String>,java.io.File,java.lang.String,java.lang.String,org.netbeans.api.extexecution.ExecutionDescriptor,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
 anno 0 java.lang.Deprecated()
meth public java.util.concurrent.CompletableFuture<java.lang.Void> start(org.netbeans.modules.nativeimage.api.debug.StartDebugParameters,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
meth public java.util.concurrent.CompletableFuture<org.netbeans.modules.nativeimage.api.debug.NIVariable> evaluateAsync(java.lang.String,java.lang.String,org.netbeans.modules.nativeimage.api.debug.NIFrame)
meth public org.netbeans.api.debugger.Breakpoint addLineBreakpoint(java.lang.Object,org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor)
meth public org.netbeans.modules.nativeimage.api.debug.NIVariable evaluate(java.lang.String,java.lang.String,org.netbeans.modules.nativeimage.api.debug.NIFrame) throws org.netbeans.modules.nativeimage.api.debug.EvaluateException
meth public static org.netbeans.modules.nativeimage.api.debug.NIDebugger$Builder newBuilder()
meth public void removeBreakpoint(java.lang.Object)
supr java.lang.Object
hfds provider

CLSS public final static org.netbeans.modules.nativeimage.api.debug.NIDebugger$Builder
 outer org.netbeans.modules.nativeimage.api.debug.NIDebugger
meth public org.netbeans.modules.nativeimage.api.debug.NIDebugger build()
meth public org.netbeans.modules.nativeimage.api.debug.NIDebugger$Builder frameDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer)
meth public org.netbeans.modules.nativeimage.api.debug.NIDebugger$Builder variablesDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer)
supr java.lang.Object
hfds debuggerProvider

CLSS public abstract interface org.netbeans.modules.nativeimage.api.debug.NIFrame
meth public abstract int getLevel()
meth public abstract int getLine()
meth public abstract java.lang.String getAddress()
meth public abstract java.lang.String getFullFileName()
meth public abstract java.lang.String getFunctionName()
meth public abstract java.lang.String getShortFileName()
meth public abstract java.lang.String getThreadId()

CLSS public final org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor
innr public final static Builder
meth public boolean isEnabled()
meth public boolean isHidden()
meth public int getLine()
meth public java.lang.String getCondition()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getFilePath()
meth public static org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder newBuilder(java.lang.String,int)
supr java.lang.Object
hfds condition,enabled,filePath,hidden,line

CLSS public final static org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder
 outer org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor build()
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder condition(java.lang.String)
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder enabled(boolean)
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder filePath(java.lang.String)
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder hidden(boolean)
meth public org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor$Builder line(int)
supr java.lang.Object
hfds condition,enabled,filePath,hidden,line

CLSS public abstract interface org.netbeans.modules.nativeimage.api.debug.NIVariable
meth public abstract int getNumChildren()
meth public abstract java.lang.String getExpressionPath()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getValue()
meth public abstract org.netbeans.modules.nativeimage.api.debug.NIFrame getFrame()
meth public abstract org.netbeans.modules.nativeimage.api.debug.NIVariable getParent()
meth public abstract org.netbeans.modules.nativeimage.api.debug.NIVariable[] getChildren(int,int)
meth public org.netbeans.modules.nativeimage.api.debug.NIVariable[] getChildren()

CLSS public final org.netbeans.modules.nativeimage.api.debug.StartDebugParameters
innr public final static Builder
meth public boolean isDebuggerDisplayObjects()
meth public java.io.File getWorkingDirectory()
meth public java.lang.Long getProcessId()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDebugger()
meth public java.lang.String getDisplayName()
meth public java.util.List<java.lang.String> getCommand()
meth public org.netbeans.api.extexecution.ExecutionDescriptor getExecutionDescriptor()
meth public org.openide.util.Lookup getContextLookup()
meth public static org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder newBuilder(java.util.List<java.lang.String>)
supr java.lang.Object
hfds command,contextLookup,debugger,displayName,displayObjects,executionDescriptor,processId,workingDirectory

CLSS public final static org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder
 outer org.netbeans.modules.nativeimage.api.debug.StartDebugParameters
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters build()
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder debugger(java.lang.String)
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder debuggerDisplayObjects(boolean)
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder displayName(java.lang.String)
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder executionDescriptor(org.netbeans.api.extexecution.ExecutionDescriptor)
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder lookup(org.openide.util.Lookup)
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder processID(java.lang.Long)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.nativeimage.api.debug.StartDebugParameters$Builder workingDirectory(java.io.File)
supr java.lang.Object
hfds command,contextLookup,debugger,displayName,displayObjects,executionDescriptor,processId,workingDirectory

CLSS public abstract interface org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider
meth public abstract java.lang.String getVersion()
meth public abstract java.lang.String readMemory(java.lang.String,long,int)
meth public abstract java.util.concurrent.CompletableFuture<org.netbeans.modules.nativeimage.api.debug.NIVariable> evaluateAsync(java.lang.String,java.lang.String,org.netbeans.modules.nativeimage.api.debug.NIFrame)
meth public abstract org.netbeans.api.debugger.Breakpoint addLineBreakpoint(java.lang.Object,org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor)
meth public abstract void removeBreakpoint(java.lang.Object)
meth public abstract void setFrameDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer)
meth public abstract void setVariablesDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer)
meth public java.util.List<org.netbeans.modules.nativeimage.api.Location> listLocations(java.lang.String)
meth public java.util.Map<org.netbeans.modules.nativeimage.api.SourceInfo,java.util.List<org.netbeans.modules.nativeimage.api.Symbol>> listFunctions(java.lang.String,boolean,int)
meth public java.util.Map<org.netbeans.modules.nativeimage.api.SourceInfo,java.util.List<org.netbeans.modules.nativeimage.api.Symbol>> listVariables(java.lang.String,boolean,int)
meth public java.util.concurrent.CompletableFuture<java.lang.Void> attach(java.lang.String,long,java.lang.String,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
 anno 0 java.lang.Deprecated()
meth public java.util.concurrent.CompletableFuture<java.lang.Void> start(java.util.List<java.lang.String>,java.io.File,java.lang.String,java.lang.String,org.netbeans.api.extexecution.ExecutionDescriptor,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
 anno 0 java.lang.Deprecated()
meth public java.util.concurrent.CompletableFuture<java.lang.Void> start(org.netbeans.modules.nativeimage.api.debug.StartDebugParameters,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)

CLSS public abstract interface org.netbeans.modules.nativeimage.spi.debug.NIDebuggerServiceProvider
meth public abstract org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider create()

CLSS public abstract interface org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer
innr public final static DisplayedFrame
meth public abstract org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame displayed(org.netbeans.modules.nativeimage.api.debug.NIFrame)

CLSS public final static org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame
 outer org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer
innr public final static Builder
meth public int getLine()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.net.URI getSourceURI()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame$Builder newBuilder(java.lang.String)
supr java.lang.Object
hfds description,displayName,line,uriSupplier

CLSS public final static org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame$Builder
 outer org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame
meth public org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame build()
meth public org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame$Builder description(java.lang.String)
meth public org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame$Builder line(int)
meth public org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer$DisplayedFrame$Builder sourceURISupplier(java.util.function.Supplier<java.net.URI>)
supr java.lang.Object
hfds description,displayName,line,uriSupplier

CLSS public abstract interface org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer
meth public abstract !varargs org.netbeans.modules.nativeimage.api.debug.NIVariable[] displayed(org.netbeans.modules.nativeimage.api.debug.NIVariable[])

