#Signature file v4.1
#Version 0.1

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

CLSS public org.netbeans.modules.nativeimage.api.debug.EvaluateException
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public final org.netbeans.modules.nativeimage.api.debug.NIDebugger
innr public final static Builder
meth public java.lang.String getVersion()
meth public java.lang.String readMemory(java.lang.String,long,int)
meth public java.util.concurrent.CompletableFuture<java.lang.Void> start(java.util.List<java.lang.String>,java.io.File,java.lang.String,java.lang.String,org.netbeans.api.extexecution.ExecutionDescriptor,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
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

CLSS public abstract interface org.netbeans.modules.nativeimage.spi.debug.NIDebuggerProvider
meth public abstract java.lang.String getVersion()
meth public abstract java.lang.String readMemory(java.lang.String,long,int)
meth public abstract java.util.concurrent.CompletableFuture<java.lang.Void> start(java.util.List<java.lang.String>,java.io.File,java.lang.String,java.lang.String,org.netbeans.api.extexecution.ExecutionDescriptor,java.util.function.Consumer<org.netbeans.api.debugger.DebuggerEngine>)
meth public abstract java.util.concurrent.CompletableFuture<org.netbeans.modules.nativeimage.api.debug.NIVariable> evaluateAsync(java.lang.String,java.lang.String,org.netbeans.modules.nativeimage.api.debug.NIFrame)
meth public abstract org.netbeans.api.debugger.Breakpoint addLineBreakpoint(java.lang.Object,org.netbeans.modules.nativeimage.api.debug.NILineBreakpointDescriptor)
meth public abstract void removeBreakpoint(java.lang.Object)
meth public abstract void setFrameDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.FrameDisplayer)
meth public abstract void setVariablesDisplayer(org.netbeans.modules.nativeimage.spi.debug.filters.VariableDisplayer)

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

