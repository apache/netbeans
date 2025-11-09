#Signature file v4.1
#Version 2.2

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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

CLSS public abstract org.netbeans.lib.chrome_devtools_protocol.CDTUtil
meth public static java.lang.String toNodeUrl(java.lang.String)
meth public static java.net.URI toNodeUrl(java.net.URI)
supr java.lang.Object

CLSS public org.netbeans.lib.chrome_devtools_protocol.ChromeDevToolsClient
cons public init(java.net.URI)
intf java.io.Closeable
meth public boolean connected()
meth public org.netbeans.lib.chrome_devtools_protocol.DebuggerDomain getDebugger()
meth public org.netbeans.lib.chrome_devtools_protocol.RuntimeDomain getRuntime()
meth public static org.netbeans.lib.chrome_devtools_protocol.json.Endpoint[] listEndpoints(java.lang.String,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void connect()
supr java.lang.Object
hfds LOG,callbacks,client,debuggerDomain,eventHandler,gson,idSupplier,runtimeDomain,webSocket,websocketUri

CLSS public final org.netbeans.lib.chrome_devtools_protocol.DebuggerDomain
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationResponse> continueToLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.DisableResponse> disable(org.netbeans.lib.chrome_devtools_protocol.debugger.DisableRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.EnableResponse> enable(org.netbeans.lib.chrome_devtools_protocol.debugger.EnableRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameResponse> evaluateOnCallFrame(org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsResponse> getPossibleBreakpoints(org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceResponse> getScriptSource(org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.PauseResponse> pause(org.netbeans.lib.chrome_devtools_protocol.debugger.PauseRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointResponse> removeBreakpoint(org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeResponse> resume(org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentRequest> searchInContent(org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentResponse)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthResponse> setAsyncCallStackDepth(org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlResponse> setBreakpointByUrl(org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointResponse> setBreakpoint(org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveResponse> setBreakpointsActive(org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointResponse> setInstrumentationBreakpoint(org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsResponse> setPauseOnExceptions(org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceResponse> setScriptSource(org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesResponse> setSkipAllPauses(org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueResponse> setVariableValue(org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoResponse> stepInto(org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutResponse> stepOut(org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutRequest)
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverResponse> stepOver(org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverRequest)
meth public org.netbeans.lib.chrome_devtools_protocol.Unregisterer onBreakpointResolved(java.util.function.Consumer<org.netbeans.lib.chrome_devtools_protocol.debugger.BreakpointResolved>)
meth public org.netbeans.lib.chrome_devtools_protocol.Unregisterer onPaused(java.util.function.Consumer<org.netbeans.lib.chrome_devtools_protocol.debugger.Paused>)
meth public org.netbeans.lib.chrome_devtools_protocol.Unregisterer onResumed(java.util.function.Consumer<org.netbeans.lib.chrome_devtools_protocol.debugger.Resumed>)
meth public org.netbeans.lib.chrome_devtools_protocol.Unregisterer onScriptFailedToParse(java.util.function.Consumer<org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptFailedToParse>)
meth public org.netbeans.lib.chrome_devtools_protocol.Unregisterer onScriptParsed(java.util.function.Consumer<org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptParsed>)
supr java.lang.Object
hfds cdtc

CLSS public org.netbeans.lib.chrome_devtools_protocol.DebuggerException
cons public init(int,java.lang.String,java.lang.String)
cons public init(java.lang.String)
meth public int getCode()
meth public java.lang.String getData()
meth public java.lang.String getMessage()
meth public java.lang.String getRawMessage()
supr java.lang.RuntimeException
hfds code,data,rawMessage

CLSS public final org.netbeans.lib.chrome_devtools_protocol.RuntimeDomain
meth public java.util.concurrent.CompletionStage<java.lang.Void> runIfWaitingForDebugger()
meth public java.util.concurrent.CompletionStage<org.netbeans.lib.chrome_devtools_protocol.runtime.GetPropertiesResponse> getProperties(org.netbeans.lib.chrome_devtools_protocol.runtime.GetPropertiesRequest)
supr java.lang.Object
hfds cdtc

CLSS public abstract interface org.netbeans.lib.chrome_devtools_protocol.Unregisterer
intf java.lang.AutoCloseable
meth public abstract void close()

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.BreakLocation
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String getScriptId()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public void setColumnNumber(int)
meth public void setLineNumber(int)
meth public void setScriptId(java.lang.String)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds columnNumber,lineNumber,scriptId,type

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.BreakpointResolved
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBreakpointId()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getLocation()
meth public void setBreakpointId(java.lang.String)
meth public void setLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
supr java.lang.Object
hfds breakpointId,location

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getCanBeRestarted()
meth public java.lang.String getCallFrameId()
meth public java.lang.String getFunctionName()
meth public java.lang.String getUrl()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.Scope> getScopeChain()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getFunctionLocation()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getLocation()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getReturnValue()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getThisObject()
meth public void setCallFrameId(java.lang.String)
meth public void setCanBeRestarted(java.lang.Boolean)
meth public void setFunctionLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setFunctionName(java.lang.String)
meth public void setLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setReturnValue(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setScopeChain(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.Scope>)
meth public void setThisObject(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setUrl(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr java.lang.Object
hfds callFrameId,canBeRestarted,functionLocation,functionName,location,returnValue,scopeChain,thisObject,url

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getTargetCallFrames()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getLocation()
meth public void setLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setTargetCallFrames(java.lang.String)
supr java.lang.Object
hfds location,targetCallFrames

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ContinueToLocationResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.DebugSymbols
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.net.URI getExternalURL()
meth public void setExternalURL(java.net.URI)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds externalURL,type

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.DisableRequest
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.DisableResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.EnableRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Integer getMaxScriptsCache()
meth public java.lang.String toString()
meth public void setMaxScriptsCache(java.lang.Integer)
supr java.lang.Object
hfds maxScriptsCache

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.EnableResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDebuggerId()
meth public java.lang.String toString()
meth public void setDebuggerId(java.lang.String)
supr java.lang.Object
hfds debuggerId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameRequest
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getGeneratePreview()
meth public java.lang.Boolean getIncludeCommandLineAPI()
meth public java.lang.Boolean getReturnByValue()
meth public java.lang.Boolean getSilent()
meth public java.lang.Boolean getThrowOnSideEffect()
meth public java.lang.Integer getTimeout()
meth public java.lang.String getCallFrameId()
meth public java.lang.String getExpression()
meth public java.lang.String getObjectGroup()
meth public java.lang.String toString()
meth public void setCallFrameId(java.lang.String)
meth public void setExpression(java.lang.String)
meth public void setGeneratePreview(java.lang.Boolean)
meth public void setIncludeCommandLineAPI(java.lang.Boolean)
meth public void setObjectGroup(java.lang.String)
meth public void setReturnByValue(java.lang.Boolean)
meth public void setSilent(java.lang.Boolean)
meth public void setThrowOnSideEffect(java.lang.Boolean)
meth public void setTimeout(java.lang.Integer)
supr java.lang.Object
hfds callFrameId,expression,generatePreview,includeCommandLineAPI,objectGroup,returnByValue,silent,throwOnSideEffect,timeout

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.EvaluateOnCallFrameResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails getExceptionDetails()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getResult()
meth public void setExceptionDetails(org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails)
meth public void setResult(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
supr java.lang.Object
hfds exceptionDetails,result

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getRestrictToFunction()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getEnd()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getStart()
meth public void setEnd(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setRestrictToFunction(java.lang.Boolean)
meth public void setStart(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
supr java.lang.Object
hfds end,restrictToFunction,start

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.GetPossibleBreakpointsResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.BreakLocation> getLocations()
meth public void setLocations(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.BreakLocation>)
supr java.lang.Object
hfds locations

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getScriptId()
meth public java.lang.String toString()
meth public void setScriptId(java.lang.String)
supr java.lang.Object
hfds scriptId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.GetScriptSourceResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBytecode()
meth public java.lang.String getScriptSource()
meth public java.lang.String toString()
meth public void setBytecode(java.lang.String)
meth public void setScriptSource(java.lang.String)
supr java.lang.Object
hfds bytecode,scriptSource

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.Location
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.Integer getColumnNumber()
meth public java.lang.String getScriptId()
meth public java.lang.String toString()
meth public void setColumnNumber(java.lang.Integer)
meth public void setLineNumber(int)
meth public void setScriptId(java.lang.String)
supr java.lang.Object
hfds columnNumber,lineNumber,scriptId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.LocationRange
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getScriptId()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptPosition getEnd()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptPosition getStart()
meth public void setEnd(org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptPosition)
meth public void setScriptId(java.lang.String)
meth public void setStart(org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptPosition)
supr java.lang.Object
hfds end,scriptId,start

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.PauseRequest
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.PauseResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.Paused
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getData()
meth public java.lang.String getReason()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getHitBreakpoints()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame> getCallFrames()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getAsyncStackTrace()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId getAsyncStackTraceId()
meth public void setAsyncStackTrace(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setAsyncStackTraceId(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId)
meth public void setCallFrames(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame>)
meth public void setData(java.lang.Object)
meth public void setHitBreakpoints(java.util.List<java.lang.String>)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds asyncStackTrace,asyncStackTraceId,callFrames,data,hitBreakpoints,reason

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointRequest
cons public init()
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBreakpointId()
meth public java.lang.String toString()
meth public void setBreakpointId(java.lang.String)
supr java.lang.Object
hfds breakpointId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.RemoveBreakpointResponse
cons public init()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getTerminateOnResume()
meth public java.lang.String toString()
meth public void setTerminateOnResume(java.lang.Boolean)
supr java.lang.Object
hfds terminateOnResume

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ResumeResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.Resumed
cons public init()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.Scope
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getEndLocation()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getStartLocation()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getObject()
meth public void setEndLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setName(java.lang.String)
meth public void setObject(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setStartLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds endLocation,name,object,startLocation,type

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptFailedToParse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getEndColumn()
meth public int getEndLine()
meth public int getExecutionContextId()
meth public int getStartColumn()
meth public int getStartLine()
meth public int hashCode()
meth public java.lang.Boolean getHasSourceURL()
meth public java.lang.Boolean getIsModule()
meth public java.lang.Integer getCodeOffset()
meth public java.lang.Integer getLength()
meth public java.lang.Object getExecutionContextAuxData()
meth public java.lang.String getEmbedderName()
meth public java.lang.String getHash()
meth public java.lang.String getScriptId()
meth public java.lang.String getScriptLanguage()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public java.net.URI getSourceMapURL()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getStackTrace()
meth public void setCodeOffset(java.lang.Integer)
meth public void setEmbedderName(java.lang.String)
meth public void setEndColumn(int)
meth public void setEndLine(int)
meth public void setExecutionContextAuxData(java.lang.Object)
meth public void setExecutionContextId(int)
meth public void setHasSourceURL(java.lang.Boolean)
meth public void setHash(java.lang.String)
meth public void setIsModule(java.lang.Boolean)
meth public void setLength(java.lang.Integer)
meth public void setScriptId(java.lang.String)
meth public void setScriptLanguage(java.lang.String)
meth public void setSourceMapURL(java.net.URI)
meth public void setStackTrace(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setStartColumn(int)
meth public void setStartLine(int)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds codeOffset,embedderName,endColumn,endLine,executionContextAuxData,executionContextId,hasSourceURL,hash,isModule,length,scriptId,scriptLanguage,sourceMapURL,stackTrace,startColumn,startLine,url

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptParsed
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getEndColumn()
meth public int getEndLine()
meth public int getExecutionContextId()
meth public int getStartColumn()
meth public int getStartLine()
meth public int hashCode()
meth public java.lang.Boolean getHasSourceURL()
meth public java.lang.Boolean getIsLiveEdit()
meth public java.lang.Boolean getIsModule()
meth public java.lang.Integer getCodeOffset()
meth public java.lang.Integer getLength()
meth public java.lang.Object getExecutionContextAuxData()
meth public java.lang.String getEmbedderName()
meth public java.lang.String getHash()
meth public java.lang.String getScriptId()
meth public java.lang.String getScriptLanguage()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public java.net.URI getSourceMapURL()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.DebugSymbols getDebugSymbols()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getStackTrace()
meth public void setCodeOffset(java.lang.Integer)
meth public void setDebugSymbols(org.netbeans.lib.chrome_devtools_protocol.debugger.DebugSymbols)
meth public void setEmbedderName(java.lang.String)
meth public void setEndColumn(int)
meth public void setEndLine(int)
meth public void setExecutionContextAuxData(java.lang.Object)
meth public void setExecutionContextId(int)
meth public void setHasSourceURL(java.lang.Boolean)
meth public void setHash(java.lang.String)
meth public void setIsLiveEdit(java.lang.Boolean)
meth public void setIsModule(java.lang.Boolean)
meth public void setLength(java.lang.Integer)
meth public void setScriptId(java.lang.String)
meth public void setScriptLanguage(java.lang.String)
meth public void setSourceMapURL(java.net.URI)
meth public void setStackTrace(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setStartColumn(int)
meth public void setStartLine(int)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds codeOffset,debugSymbols,embedderName,endColumn,endLine,executionContextAuxData,executionContextId,hasSourceURL,hash,isLiveEdit,isModule,length,scriptId,scriptLanguage,sourceMapURL,stackTrace,startColumn,startLine,url

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.ScriptPosition
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setColumnNumber(int)
meth public void setLineNumber(int)
supr java.lang.Object
hfds columnNumber,lineNumber

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getCaseSensitive()
meth public java.lang.Boolean getIsRegex()
meth public java.lang.String getQuery()
meth public java.lang.String getScriptId()
meth public java.lang.String toString()
meth public void setCaseSensitive(java.lang.Boolean)
meth public void setIsRegex(java.lang.Boolean)
meth public void setQuery(java.lang.String)
meth public void setScriptId(java.lang.String)
supr java.lang.Object
hfds caseSensitive,isRegex,query,scriptId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SearchInContentResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.SearchMatch> getResult()
meth public void setResult(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.SearchMatch>)
supr java.lang.Object
hfds result

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SearchMatch
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String getLineContent()
meth public java.lang.String toString()
meth public void setLineContent(java.lang.String)
meth public void setLineNumber(int)
supr java.lang.Object
hfds lineContent,lineNumber

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getMaxDepth()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setMaxDepth(int)
supr java.lang.Object
hfds maxDepth

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetAsyncCallStackDepthResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.Integer getColumnNumber()
meth public java.lang.String getCondition()
meth public java.lang.String getScriptHash()
meth public java.lang.String getUrl()
meth public java.lang.String getUrlRegex()
meth public java.lang.String toString()
meth public void setColumnNumber(java.lang.Integer)
meth public void setCondition(java.lang.String)
meth public void setLineNumber(int)
meth public void setScriptHash(java.lang.String)
meth public void setUrl(java.lang.String)
meth public void setUrlRegex(java.lang.String)
supr java.lang.Object
hfds columnNumber,condition,lineNumber,scriptHash,url,urlRegex

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointByUrlResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBreakpointId()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.Location> getLocations()
meth public void setBreakpointId(java.lang.String)
meth public void setLocations(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.Location>)
supr java.lang.Object
hfds breakpointId,locations

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getCondition()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getLocation()
meth public void setCondition(java.lang.String)
meth public void setLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
supr java.lang.Object
hfds condition,location

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBreakpointId()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.debugger.Location getActualLocation()
meth public void setActualLocation(org.netbeans.lib.chrome_devtools_protocol.debugger.Location)
meth public void setBreakpointId(java.lang.String)
supr java.lang.Object
hfds actualLocation,breakpointId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public boolean isActive()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setActive(boolean)
supr java.lang.Object
hfds active

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetBreakpointsActiveResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointRequest
cons public init()
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getInstrumentation()
meth public java.lang.String toString()
meth public void setInstrumentation(java.lang.String)
supr java.lang.Object
hfds instrumentation

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetInstrumentationBreakpointResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBreakpointId()
meth public java.lang.String toString()
meth public void setBreakpointId(java.lang.String)
supr java.lang.Object
hfds breakpointId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getState()
meth public java.lang.String toString()
meth public void setState(java.lang.String)
supr java.lang.Object
hfds state

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetPauseOnExceptionsResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getDryRun()
meth public java.lang.String getScriptId()
meth public java.lang.String getScriptSource()
meth public java.lang.String toString()
meth public void setDryRun(java.lang.Boolean)
meth public void setScriptId(java.lang.String)
meth public void setScriptSource(java.lang.String)
supr java.lang.Object
hfds dryRun,scriptId,scriptSource

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetScriptSourceResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getStackChanged()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame> getCallFrames()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails getExceptionDetails()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getAsyncStackTrace()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId getAsyncStackTraceId()
meth public void setAsyncStackTrace(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setAsyncStackTraceId(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId)
meth public void setCallFrames(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.CallFrame>)
meth public void setExceptionDetails(org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails)
meth public void setStackChanged(java.lang.Boolean)
supr java.lang.Object
hfds asyncStackTrace,asyncStackTraceId,callFrames,exceptionDetails,stackChanged

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public boolean isSkip()
meth public int hashCode()
meth public java.lang.String toString()
meth public void setSkip(boolean)
supr java.lang.Object
hfds skip

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetSkipAllPausesResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getScopeNumber()
meth public int hashCode()
meth public java.lang.String getCallFrameId()
meth public java.lang.String getVariableName()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.CallArgument getNewValue()
meth public void setCallFrameId(java.lang.String)
meth public void setNewValue(org.netbeans.lib.chrome_devtools_protocol.runtime.CallArgument)
meth public void setScopeNumber(int)
meth public void setVariableName(java.lang.String)
supr java.lang.Object
hfds callFrameId,newValue,scopeNumber,variableName

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.SetVariableValueResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getBreakOnAsyncCall()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.LocationRange> getSkipList()
meth public void setBreakOnAsyncCall(java.lang.Boolean)
meth public void setSkipList(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.LocationRange>)
supr java.lang.Object
hfds breakOnAsyncCall,skipList

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepIntoResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutRequest
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepOutResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverRequest
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.LocationRange> getSkipList()
meth public void setSkipList(java.util.List<org.netbeans.lib.chrome_devtools_protocol.debugger.LocationRange>)
supr java.lang.Object
hfds skipList

CLSS public final org.netbeans.lib.chrome_devtools_protocol.debugger.StepOverResponse
cons public init()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.netbeans.lib.chrome_devtools_protocol.json.Endpoint
cons public init()
fld public final static java.lang.String TYPE_IFRAME = "iframe"
fld public final static java.lang.String TYPE_NODE = "node"
fld public final static java.lang.String TYPE_PAGE = "page"
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getId()
meth public java.lang.String getTitle()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.net.URI getFaviconUrl()
meth public java.net.URI getUrl()
meth public java.net.URI getWebSocketDebuggerUrl()
meth public void setDescription(java.lang.String)
meth public void setFaviconUrl(java.net.URI)
meth public void setId(java.lang.String)
meth public void setTitle(java.lang.String)
meth public void setType(java.lang.String)
meth public void setUrl(java.net.URI)
meth public void setWebSocketDebuggerUrl(java.net.URI)
supr java.lang.Object
hfds description,faviconUrl,id,title,type,url,webSocketDebuggerUrl

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.CallArgument
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getValue()
meth public java.lang.String getObjectId()
meth public java.lang.String getUnserializableValue()
meth public java.lang.String toString()
meth public void setObjectId(java.lang.String)
meth public void setUnserializableValue(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds objectId,unserializableValue,value

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.CallFrame
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.String getFunctionName()
meth public java.lang.String getScriptId()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public void setColumnNumber(int)
meth public void setFunctionName(java.lang.String)
meth public void setLineNumber(int)
meth public void setScriptId(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds columnNumber,functionName,lineNumber,scriptId,url

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.CustomPreview
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBodyGetterId()
meth public java.lang.String getHeader()
meth public java.lang.String toString()
meth public void setBodyGetterId(java.lang.String)
meth public void setHeader(java.lang.String)
supr java.lang.Object
hfds bodyGetterId,header

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.EntryPreview
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview getKey()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview getValue()
meth public void setKey(org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview)
meth public void setValue(org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview)
supr java.lang.Object
hfds key,value

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getExceptionId()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.Integer getExecutionContextId()
meth public java.lang.Object getExceptionMetaData()
meth public java.lang.String getScriptId()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.net.URI getUrl()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getException()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getStackTrace()
meth public void setColumnNumber(int)
meth public void setException(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setExceptionId(int)
meth public void setExceptionMetaData(java.lang.Object)
meth public void setExecutionContextId(java.lang.Integer)
meth public void setLineNumber(int)
meth public void setScriptId(java.lang.String)
meth public void setStackTrace(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setText(java.lang.String)
meth public void setUrl(java.net.URI)
supr java.lang.Object
hfds columnNumber,exception,exceptionId,exceptionMetaData,executionContextId,lineNumber,scriptId,stackTrace,text,url

CLSS public org.netbeans.lib.chrome_devtools_protocol.runtime.GetPropertiesRequest
cons public init()
cons public init(java.lang.String,java.lang.Boolean)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Boolean getAccessorPropertiesOnly()
meth public java.lang.Boolean getGeneratPreview()
meth public java.lang.Boolean getNonIndexedPropertiesOnly()
meth public java.lang.Boolean getOwnProperties()
meth public java.lang.String getObjectId()
meth public java.lang.String toString()
meth public void setAccessorPropertiesOnly(java.lang.Boolean)
meth public void setGeneratPreview(java.lang.Boolean)
meth public void setNonIndexedPropertiesOnly(java.lang.Boolean)
meth public void setObjectId(java.lang.String)
meth public void setOwnProperties(java.lang.Boolean)
supr java.lang.Object
hfds accessorPropertiesOnly,generatPreview,nonIndexedPropertiesOnly,objectId,ownProperties

CLSS public org.netbeans.lib.chrome_devtools_protocol.runtime.GetPropertiesResponse
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.InternalPropertyDescriptor> getInternalProperties()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PrivatePropertyDescriptor> getPrivateProperties()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyDescriptor> getResult()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails getExceptionDetails()
meth public void setExceptionDetails(org.netbeans.lib.chrome_devtools_protocol.runtime.ExceptionDetails)
meth public void setInternalProperties(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.InternalPropertyDescriptor>)
meth public void setPrivateProperties(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PrivatePropertyDescriptor>)
meth public void setResult(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyDescriptor>)
supr java.lang.Object
hfds exceptionDetails,internalProperties,privateProperties,result

CLSS public org.netbeans.lib.chrome_devtools_protocol.runtime.InternalPropertyDescriptor
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getValue()
meth public void setName(java.lang.String)
meth public void setValue(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
supr java.lang.Object
hfds name,value

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview
cons public init()
meth public boolean equals(java.lang.Object)
meth public boolean isOverflow()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getSubtype()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.EntryPreview> getEntries()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyPreview> getProperties()
meth public void setDescription(java.lang.String)
meth public void setEntries(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.EntryPreview>)
meth public void setOverflow(boolean)
meth public void setProperties(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyPreview>)
meth public void setSubtype(java.lang.String)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds description,entries,overflow,properties,subtype,type

CLSS public org.netbeans.lib.chrome_devtools_protocol.runtime.PrivatePropertyDescriptor
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getGet()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getSet()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getValue()
meth public void setGet(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setName(java.lang.String)
meth public void setSet(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setValue(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
supr java.lang.Object
hfds get,name,set,value

CLSS public org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyDescriptor
cons public init()
meth public boolean equals(java.lang.Object)
meth public boolean isConfigurable()
meth public boolean isEnumerable()
meth public int hashCode()
meth public java.lang.Boolean getIsOwn()
meth public java.lang.Boolean getWasThrown()
meth public java.lang.Boolean getWriteable()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getGet()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getSet()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getSymbol()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject getValue()
meth public void setConfigurable(boolean)
meth public void setEnumerable(boolean)
meth public void setGet(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setIsOwn(java.lang.Boolean)
meth public void setName(java.lang.String)
meth public void setSet(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setSymbol(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setValue(org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject)
meth public void setWasThrown(java.lang.Boolean)
meth public void setWriteable(java.lang.Boolean)
supr java.lang.Object
hfds configurable,enumerable,get,isOwn,name,set,symbol,value,wasThrown,writeable

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.PropertyPreview
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getSubtype()
meth public java.lang.String getType()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview getValuePreview()
meth public void setName(java.lang.String)
meth public void setSubtype(java.lang.String)
meth public void setType(java.lang.String)
meth public void setValue(java.lang.String)
meth public void setValuePreview(org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview)
supr java.lang.Object
hfds name,subtype,type,value,valuePreview

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getValue()
meth public java.lang.String getClassName()
meth public java.lang.String getDescription()
meth public java.lang.String getObjectId()
meth public java.lang.String getSubtype()
meth public java.lang.String getType()
meth public java.lang.String getUnserializableValue()
meth public java.lang.String toString()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.CustomPreview getCustomPreview()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview getPreview()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.WebDriverValue getWebDriverValue()
meth public void setClassName(java.lang.String)
meth public void setCustomPreview(org.netbeans.lib.chrome_devtools_protocol.runtime.CustomPreview)
meth public void setDescription(java.lang.String)
meth public void setObjectId(java.lang.String)
meth public void setPreview(org.netbeans.lib.chrome_devtools_protocol.runtime.ObjectPreview)
meth public void setSubtype(java.lang.String)
meth public void setType(java.lang.String)
meth public void setUnserializableValue(java.lang.String)
meth public void setValue(java.lang.Object)
meth public void setWebDriverValue(org.netbeans.lib.chrome_devtools_protocol.runtime.WebDriverValue)
supr java.lang.Object
hfds className,customPreview,description,objectId,preview,subtype,type,unserializableValue,value,webDriverValue

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.CallFrame> getCallFrames()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace getParent()
meth public org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId getParentId()
meth public void setCallFrames(java.util.List<org.netbeans.lib.chrome_devtools_protocol.runtime.CallFrame>)
meth public void setDescription(java.lang.String)
meth public void setParent(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTrace)
meth public void setParentId(org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId)
supr java.lang.Object
hfds callFrames,description,parent,parentId

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.StackTraceId
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDebuggerId()
meth public java.lang.String getId()
meth public void setDebuggerId(java.lang.String)
meth public void setId(java.lang.String)
supr java.lang.Object
hfds debuggerId,id

CLSS public final org.netbeans.lib.chrome_devtools_protocol.runtime.WebDriverValue
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getValue()
meth public java.lang.String getObjectId()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public void setObjectId(java.lang.String)
meth public void setType(java.lang.String)
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds objectId,type,value

