#Signature file v4.1
#Version 1.76

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface java.util.EventListener

CLSS public org.netbeans.modules.web.webkit.debugging.api.BreakpointException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public final org.netbeans.modules.web.webkit.debugging.api.Debugger
fld public final static java.lang.String DOM_BREAKPOINT_ATTRIBUTE = "attribute-modified"
fld public final static java.lang.String DOM_BREAKPOINT_NODE = "node-removed"
fld public final static java.lang.String DOM_BREAKPOINT_SUBTREE = "subtree-modified"
fld public final static java.lang.String PROP_BREAKPOINTS_ACTIVE = "breakpointsActive"
fld public final static java.lang.String PROP_CURRENT_FRAME = "currentFrame"
innr public abstract interface static Listener
innr public abstract interface static ScriptsListener
meth public boolean areBreakpointsActive()
meth public boolean enable()
meth public boolean isEnabled()
meth public boolean isInLiveHTMLMode()
meth public boolean isSuspended()
meth public java.net.URL getConnectionURL()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame> getCurrentCallStack()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint addDOMBreakpoint(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint addEventBreakpoint(java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint addInstrumentationBreakpoint(java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint addLineBreakpoint(java.lang.String,int,java.lang.Integer,java.lang.String) throws org.netbeans.modules.web.webkit.debugging.api.BreakpointException
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint addXHRBreakpoint(java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame getCurrentCallFrame()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Script getScript(java.lang.String)
meth public void addListener(org.netbeans.modules.web.webkit.debugging.api.Debugger$Listener)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addScriptsListener(org.netbeans.modules.web.webkit.debugging.api.Debugger$ScriptsListener)
meth public void disable()
meth public void enableDebuggerInLiveHTMLMode()
meth public void pause()
meth public void removeDOMBreakpoint(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public void removeEventBreakpoint(java.lang.String)
meth public void removeInstrumentationBreakpoint(java.lang.String)
meth public void removeLineBreakpoint(org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint)
meth public void removeListener(org.netbeans.modules.web.webkit.debugging.api.Debugger$Listener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeScriptsListener(org.netbeans.modules.web.webkit.debugging.api.Debugger$ScriptsListener)
meth public void removeXHRBreakpoint(java.lang.String)
meth public void resume()
meth public void setBreakpointsActive(boolean)
meth public void setCurrentCallFrame(org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame)
meth public void stepInto()
meth public void stepOut()
meth public void stepOver()
supr java.lang.Object
hfds COMMAND_DISABLE,COMMAND_ENABLE,COMMAND_PAUSE,COMMAND_REMOVE_BRKP,COMMAND_REMOVE_BRKP_DOM,COMMAND_REMOVE_BRKP_EVENT,COMMAND_REMOVE_BRKP_INSTR,COMMAND_REMOVE_BRKP_XHR,COMMAND_RESUME,COMMAND_SET_BRKPS_ACTIVE,COMMAND_SET_BRKP_BY_URL,COMMAND_SET_BRKP_DOM,COMMAND_SET_BRKP_EVENT,COMMAND_SET_BRKP_INSTR,COMMAND_SET_BRKP_XHR,COMMAND_STEP_INTO,COMMAND_STEP_OUT,COMMAND_STEP_OVER,ENABLED_LOCK,LOG,RESPONSE_BRKP_RESOLVED,RESPONSE_GLOB_OBJECT_CLEARED,RESPONSE_PAUSED,RESPONSE_RESUMED,RESPONSE_SCRIPT_PARSED,breakpointsActive,breakpointsActiveLock,breakpointsById,callback,currentCallFrame,currentCallStack,enabled,inLiveHTMLMode,initDOMLister,lastBreakpointsActive,latestSnapshotTask,listeners,pchs,scripts,scriptsListeners,suspended,transport,webkit
hcls Callback

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.Debugger$Listener
 outer org.netbeans.modules.web.webkit.debugging.api.Debugger
intf java.util.EventListener
meth public abstract void enabled(boolean)
meth public abstract void paused(java.util.List<org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame>,java.lang.String)
meth public abstract void reset()
meth public abstract void resumed()

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.Debugger$ScriptsListener
 outer org.netbeans.modules.web.webkit.debugging.api.Debugger
intf java.util.EventListener
meth public abstract void scriptParsed(org.netbeans.modules.web.webkit.debugging.api.debugger.Script)

CLSS public org.netbeans.modules.web.webkit.debugging.api.Runtime
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor> getRemoteObjectProperties(org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject,boolean)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject callFunctionOn(org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject evaluate(java.lang.String)
meth public void callProcedureOn(org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject,java.lang.String)
meth public void execute(java.lang.String)
meth public void releaseObject(org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject)
supr java.lang.Object
hfds LOG,transport,webkit

CLSS public org.netbeans.modules.web.webkit.debugging.api.TransportStateException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging
meth public java.lang.String getConnectionName()
meth public org.netbeans.modules.web.webkit.debugging.api.Debugger getDebugger()
meth public org.netbeans.modules.web.webkit.debugging.api.Runtime getRuntime()
meth public org.netbeans.modules.web.webkit.debugging.api.console.Console getConsole()
meth public org.netbeans.modules.web.webkit.debugging.api.css.CSS getCSS()
meth public org.netbeans.modules.web.webkit.debugging.api.dom.DOM getDOM()
meth public org.netbeans.modules.web.webkit.debugging.api.network.Network getNetwork()
meth public org.netbeans.modules.web.webkit.debugging.api.page.Page getPage()
meth public void reset()
supr java.lang.Object
hfds console,css,debugger,dom,network,page,runtime,transport

CLSS public final org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager
meth public org.netbeans.api.debugger.Session createDebuggingSession(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public org.openide.util.Lookup createBrowserConsoleLogger(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public org.openide.util.Lookup createNetworkMonitor(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public static org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager getDefault()
meth public void stopBrowserConsoleLogger(org.openide.util.Lookup)
meth public void stopDebuggingSession(org.netbeans.api.debugger.Session)
meth public void stopNetworkMonitor(org.openide.util.Lookup)
supr java.lang.Object
hfds DEFAULT,browserLoggerFactory,debuggerFactory,networkMonitorFactory

CLSS public final org.netbeans.modules.web.webkit.debugging.api.console.Console
cons public init(org.netbeans.modules.web.webkit.debugging.TransportHelper,org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging)
innr public abstract interface static Listener
innr public final InputCallback
meth public boolean isEnabled()
meth public org.netbeans.modules.web.webkit.debugging.api.console.Console$InputCallback getInput()
meth public void addInspectedHeapObject(int)
meth public void addListener(org.netbeans.modules.web.webkit.debugging.api.console.Console$Listener)
meth public void clearMessages()
meth public void disable()
meth public void enable()
meth public void removeListener(org.netbeans.modules.web.webkit.debugging.api.console.Console$Listener)
meth public void reset()
supr java.lang.Object
hfds LOGGER,callback,enabled,input,listeners,numberOfClients,transport,webKit
hcls Callback

CLSS public final org.netbeans.modules.web.webkit.debugging.api.console.Console$InputCallback
 outer org.netbeans.modules.web.webkit.debugging.api.console.Console
cons public init(org.netbeans.modules.web.webkit.debugging.api.console.Console)
meth public void line(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.console.Console$Listener
 outer org.netbeans.modules.web.webkit.debugging.api.console.Console
intf java.util.EventListener
meth public abstract void messageAdded(org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage)
meth public abstract void messageRepeatCountUpdated(int)
meth public abstract void messagesCleared()

CLSS public org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage
innr public final static StackFrame
meth public int getLine()
meth public java.lang.String getLevel()
meth public java.lang.String getSource()
meth public java.lang.String getText()
meth public java.lang.String getType()
meth public java.lang.String getURLString()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage$StackFrame> getStackTrace()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage> getSubMessages()
supr java.lang.Object
hfds msg,stackTrace,stackTraceLoaded

CLSS public final static org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage$StackFrame
 outer org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage
cons public init(org.json.simple.JSONObject)
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getFunctionName()
meth public java.lang.String getURLString()
supr java.lang.Object
hfds stack

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.CSS
cons public init(org.netbeans.modules.web.webkit.debugging.TransportHelper)
innr public abstract interface static Listener
innr public final static !enum PseudoClass
meth public java.lang.String getStyleSheetText(java.lang.String)
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.ComputedStyleProperty> getComputedStyle(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader> getAllStyleSheets()
meth public java.util.Map<java.lang.String,org.netbeans.modules.web.webkit.debugging.api.css.PropertyInfo> getSupportedCSSProperties()
meth public org.netbeans.modules.web.webkit.debugging.api.css.InlineStyles getInlineStyles(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles getMatchedStyles(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass[],boolean,boolean)
meth public org.netbeans.modules.web.webkit.debugging.api.css.Rule setRuleSelector(org.netbeans.modules.web.webkit.debugging.api.css.RuleId,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style setPropertyText(org.netbeans.modules.web.webkit.debugging.api.css.StyleId,int,java.lang.String,boolean)
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style toggleProperty(org.netbeans.modules.web.webkit.debugging.api.css.StyleId,int,boolean)
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody getStyleSheet(java.lang.String)
meth public void addListener(org.netbeans.modules.web.webkit.debugging.api.css.CSS$Listener)
meth public void disable()
meth public void enable()
meth public void forcePseudoState(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass[])
meth public void removeListener(org.netbeans.modules.web.webkit.debugging.api.css.CSS$Listener)
meth public void reset()
meth public void setClassForHover(java.lang.String)
meth public void setStyleSheetText(java.lang.String,java.lang.String)
supr java.lang.Object
hfds LOG,callback,classForHover,listeners,styleSheetChanged,styleSheetHeaders,styleSheets,supportedProperties,transport
hcls Callback

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.css.CSS$Listener
 outer org.netbeans.modules.web.webkit.debugging.api.css.CSS
meth public abstract void mediaQueryResultChanged()
meth public abstract void styleSheetAdded(org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader)
meth public abstract void styleSheetChanged(java.lang.String)
meth public abstract void styleSheetRemoved(java.lang.String)

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass
 outer org.netbeans.modules.web.webkit.debugging.api.css.CSS
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass ACTIVE
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass FOCUS
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass HOVER
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass VISITED
meth public static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.css.CSS$PseudoClass>
hfds code

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.ComputedStyleProperty
meth public java.lang.String getName()
meth public java.lang.String getValue()
supr java.lang.Object
hfds name,value

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.InheritedStyleEntry
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.Rule> getMatchedRules()
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style getInlineStyle()
supr java.lang.Object
hfds inlineStyle,matchedRules

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.InlineStyles
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style getAttributesStyle()
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style getInlineStyle()
supr java.lang.Object
hfds attributesStyle,inlineStyle

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.MatchedStyles
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.InheritedStyleEntry> getInheritedRules()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.Rule> getMatchedRules()
supr java.lang.Object
hfds inheritedRules,matchedRules

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.Media
innr public final static !enum Source
meth public java.lang.String getText()
meth public org.netbeans.modules.web.webkit.debugging.api.css.Media$Source getSource()
supr java.lang.Object
hfds source,text

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.css.Media$Source
 outer org.netbeans.modules.web.webkit.debugging.api.css.Media
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source IMPORT_RULE
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source INLINE_SHEET
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source LINKED_SHEET
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source MEDIA_RULE
meth public static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.css.Media$Source[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.css.Media$Source>

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.Property
innr public final static !enum Status
meth public boolean isImplicit()
meth public boolean isParsedOk()
meth public java.lang.String getName()
meth public java.lang.String getPriority()
meth public java.lang.String getShorthandName()
meth public java.lang.String getText()
meth public java.lang.String getValue()
meth public org.netbeans.modules.web.webkit.debugging.api.css.Property$Status getStatus()
meth public org.netbeans.modules.web.webkit.debugging.api.css.SourceRange getRange()
supr java.lang.Object
hfds implicit,name,parsedOk,priority,range,shorthandName,status,text,value

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.css.Property$Status
 outer org.netbeans.modules.web.webkit.debugging.api.css.Property
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status ACTIVE
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status DISABLED
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status INACTIVE
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status STYLE
meth public static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.css.Property$Status[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.css.Property$Status>

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.PropertyInfo
meth public java.lang.String getName()
meth public java.util.List<java.lang.String> getLonghands()
supr java.lang.Object
hfds longhands,name

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.Rule
meth public boolean equals(java.lang.Object)
meth public int getSourceLine()
meth public int hashCode()
meth public java.lang.String getSelector()
meth public java.lang.String getSourceURL()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.Media> getMedia()
meth public org.netbeans.modules.web.webkit.debugging.api.css.RuleId getId()
meth public org.netbeans.modules.web.webkit.debugging.api.css.SourceRange getSelectorRange()
meth public org.netbeans.modules.web.webkit.debugging.api.css.Style getStyle()
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody getParentStyleSheet()
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin getOrigin()
supr java.lang.Object
hfds id,json,media,origin,parentStyleSheet,selector,selectorRange,selectors,sourceLine,sourceURL,style

CLSS public final org.netbeans.modules.web.webkit.debugging.api.css.RuleId
meth public boolean equals(java.lang.Object)
meth public int getOrdinal()
meth public int hashCode()
meth public java.lang.String getStyleSheetId()
supr java.lang.Object
hfds ordinal,styleSheetId

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.Selector
meth public java.lang.String getText()
meth public org.netbeans.modules.web.webkit.debugging.api.css.SourceRange getRange()
supr java.lang.Object
hfds range,text

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.SourceRange
meth public int getEnd()
meth public int getEndColumn()
meth public int getEndLine()
meth public int getStart()
meth public int getStartColumn()
meth public int getStartLine()
supr java.lang.Object
hfds end,endColumn,endLine,start,startColumn,startLine

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.Style
meth public java.lang.String getText()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.Property> getProperties()
meth public org.netbeans.modules.web.webkit.debugging.api.css.SourceRange getRange()
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleId getId()
supr java.lang.Object
hfds id,properties,range,text

CLSS public final org.netbeans.modules.web.webkit.debugging.api.css.StyleId
meth public boolean equals(java.lang.Object)
meth public int getOrdinal()
meth public int hashCode()
meth public java.lang.String getStyleSheetId()
supr java.lang.Object
hfds ordinal,styleSheetId

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody
meth public java.lang.String getStyleSheetId()
meth public java.lang.String getText()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.css.Rule> getRules()
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader getHeader()
supr java.lang.Object
hfds rules,styleSheetHeader,styleSheetId,text

CLSS public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader
meth public boolean isDisabled()
meth public java.lang.String getFrameId()
meth public java.lang.String getSourceURL()
meth public java.lang.String getStyleSheetId()
meth public java.lang.String getTitle()
meth public org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin getOrigin()
supr java.lang.Object
hfds disabled,frameId,origin,sourceURL,styleSheetId,title

CLSS public final !enum org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin INSPECTOR
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin REGULAR
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin USER
fld public final static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin USER_AGENT
meth public static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin>

CLSS public abstract org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
meth protected org.json.simple.JSONObject getObject()
meth protected org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging getWebkit()
meth public java.lang.String toString()
supr java.lang.Object
hfds object,webkit

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint
fld public final static java.lang.String PROP_LOCATION = "location"
meth public java.lang.String getBreakpointID()
meth public long getColumnNumber()
meth public long getLineNumber()
meth public org.json.simple.JSONObject getBreakpointLocation()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
hfds location,pcs

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getCallFrameID()
meth public java.lang.String getFunctionName()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.debugger.Scope> getScopes()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject evaluate(java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.Script getScript()
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
hfds transport

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor
meth public boolean isMutable()
meth public java.lang.String getName()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject getValue()
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
hfds value

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject
cons public init(org.json.simple.JSONObject,org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging)
innr public final static !enum SubType
innr public final static !enum Type
meth public boolean hasFetchedProperties()
meth public boolean isMutable()
meth public java.lang.String getClassName()
meth public java.lang.String getDescription()
meth public java.lang.String getObjectID()
meth public java.lang.String getValueAsString()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor> getProperties()
meth public org.json.simple.JSONObject getOwningProperty()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject apply(java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType getSubType()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type getType()
meth public void release()
meth public void resetProperties()
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
hfds PROP_CLASS_NAME,PROP_DESCRIPTION,PROP_OBJECT_ID,PROP_SUBTYPE,PROP_TYPE,PROP_VALUE,properties,property

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType
 outer org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType ARRAY
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType DATE
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType ERROR
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType NODE
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType NULL
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType REGEXP
meth public static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$SubType>

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type
 outer org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type BOOLEAN
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type FUNCTION
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type NUMBER
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type OBJECT
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type STRING
fld public final static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type UNDEFINED
meth public java.lang.String getName()
meth public static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject$Type>
hfds name

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.Scope
meth public boolean isCatchScope()
meth public boolean isClosureScope()
meth public boolean isGlobalScope()
meth public boolean isLocalScope()
meth public boolean isWithScope()
meth public java.lang.String getType()
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject getScopeObject()
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject
hfds scopeObject

CLSS public org.netbeans.modules.web.webkit.debugging.api.debugger.Script
meth public java.lang.String getID()
meth public java.lang.String getSourceMapURL()
meth public java.lang.String getURL()
supr org.netbeans.modules.web.webkit.debugging.api.debugger.AbstractObject

CLSS public org.netbeans.modules.web.webkit.debugging.api.dom.DOM
cons public init(org.netbeans.modules.web.webkit.debugging.TransportHelper,org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging)
innr public abstract interface static Listener
meth public java.lang.String getOuterHTML(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> querySelectorAll(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject resolveNode(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node getDocument()
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node querySelector(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node requestNode(org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject)
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node setNodeName(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public void addListener(org.netbeans.modules.web.webkit.debugging.api.dom.DOM$Listener)
meth public void disable()
meth public void enable()
meth public void hideHighlight()
meth public void highlightNode(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.dom.HighlightConfig)
meth public void highlightRect(java.awt.Rectangle,java.awt.Color,java.awt.Color)
meth public void removeAttribute(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public void removeListener(org.netbeans.modules.web.webkit.debugging.api.dom.DOM$Listener)
meth public void removeNode(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public void requestChildNodes(int)
meth public void reset()
meth public void setAttributeValue(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String,java.lang.String)
meth public void setClassForHover(java.lang.String)
meth public void setNodeValue(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public void setOuterHTML(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
supr java.lang.Object
hfds DOCUMENT_LOCK,callback,classForHover,documentCounter,documentNode,listeners,nodes,transport,webKit
hcls Callback

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.dom.DOM$Listener
 outer org.netbeans.modules.web.webkit.debugging.api.dom.DOM
meth public abstract void attributeModified(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String,java.lang.String)
meth public abstract void attributeRemoved(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.lang.String)
meth public abstract void characterDataModified(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public abstract void childNodeInserted(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public abstract void childNodeRemoved(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public abstract void childNodesSet(org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public abstract void documentUpdated()
meth public abstract void shadowRootPopped(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.dom.Node)
meth public abstract void shadowRootPushed(org.netbeans.modules.web.webkit.debugging.api.dom.Node,org.netbeans.modules.web.webkit.debugging.api.dom.Node)

CLSS public org.netbeans.modules.web.webkit.debugging.api.dom.HighlightConfig
cons public init()
fld public boolean showInfo
fld public java.awt.Color borderColor
fld public java.awt.Color contentColor
fld public java.awt.Color marginColor
fld public java.awt.Color paddingColor
supr java.lang.Object

CLSS public org.netbeans.modules.web.webkit.debugging.api.dom.Node
innr public static Attribute
meth public boolean equals(java.lang.Object)
meth public boolean isInjectedByNetBeans()
meth public final java.util.List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> getShadowRoots()
meth public int getChildrenCount()
meth public int getNodeId()
meth public int getNodeType()
meth public int hashCode()
meth public java.lang.String getDocumentURL()
meth public java.lang.String getInternalSubset()
meth public java.lang.String getLocalName()
meth public java.lang.String getName()
meth public java.lang.String getNodeName()
meth public java.lang.String getNodeValue()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public java.lang.String getValue()
meth public java.lang.String getXmlVersion()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.dom.Node$Attribute> getAttributes()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.dom.Node> getChildren()
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node getContentDocument()
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node getParent()
meth public org.netbeans.modules.web.webkit.debugging.api.dom.Node$Attribute getAttribute(java.lang.String)
supr java.lang.Object
hfds attributes,children,contentDocument,parent,properties,shadowRoots

CLSS public static org.netbeans.modules.web.webkit.debugging.api.dom.Node$Attribute
 outer org.netbeans.modules.web.webkit.debugging.api.dom.Node
meth public java.lang.String getName()
meth public java.lang.String getValue()
supr java.lang.Object
hfds name,value

CLSS public org.netbeans.modules.web.webkit.debugging.api.dom.NodeAnnotator
innr public abstract interface static Impl
meth public static org.netbeans.modules.web.webkit.debugging.api.dom.NodeAnnotator getDefault()
meth public void annotate(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.awt.Image)
supr java.lang.Object
hfds INSTANCE

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.dom.NodeAnnotator$Impl
 outer org.netbeans.modules.web.webkit.debugging.api.dom.NodeAnnotator
meth public abstract void annotate(org.netbeans.modules.web.webkit.debugging.api.dom.Node,java.awt.Image)

CLSS public org.netbeans.modules.web.webkit.debugging.api.network.Network
cons public init(org.netbeans.modules.web.webkit.debugging.TransportHelper,org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging)
innr public abstract interface static Listener
innr public final static !enum Direction
innr public final static Request
innr public final static WebSocketFrame
innr public final static WebSocketRequest
meth public boolean isEnabled()
meth public void addListener(org.netbeans.modules.web.webkit.debugging.api.network.Network$Listener)
meth public void disable()
meth public void enable()
meth public void removeListener(org.netbeans.modules.web.webkit.debugging.api.network.Network$Listener)
supr java.lang.Object
hfds activeRequests,activeWebSocketRequests,callback,enabled,inLiveHTMLMode,listeners,numberOfClients,transport,webKit
hcls Callback

CLSS public final static !enum org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction
 outer org.netbeans.modules.web.webkit.debugging.api.network.Network
fld public final static org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction RECEIVED
fld public final static org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction SEND
meth public static org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction valueOf(java.lang.String)
meth public static org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction[] values()
supr java.lang.Enum<org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction>

CLSS public abstract interface static org.netbeans.modules.web.webkit.debugging.api.network.Network$Listener
 outer org.netbeans.modules.web.webkit.debugging.api.network.Network
meth public abstract void networkRequest(org.netbeans.modules.web.webkit.debugging.api.network.Network$Request)
meth public abstract void webSocketRequest(org.netbeans.modules.web.webkit.debugging.api.network.Network$WebSocketRequest)

CLSS public final static org.netbeans.modules.web.webkit.debugging.api.network.Network$Request
 outer org.netbeans.modules.web.webkit.debugging.api.network.Network
fld public final static java.lang.String PROP_RESPONSE = "Network.Request.Response"
fld public final static java.lang.String PROP_RESPONSE_DATA = "Network.Request.Response.Data"
meth public boolean hasData()
meth public boolean isFailed()
meth public int getResponseCode()
meth public java.lang.String getDocumentUrl()
meth public java.lang.String getInitiatorType()
meth public java.lang.String getResponseData()
meth public java.lang.String getResponseType()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage$StackFrame> getInitiatorCallStack()
meth public org.json.simple.JSONObject getInitiator()
meth public org.json.simple.JSONObject getRequest()
meth public org.json.simple.JSONObject getResponse()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds dataReady,documentUrl,failed,hasData,initiator,network,request,requestId,response,responseType,support

CLSS public final static org.netbeans.modules.web.webkit.debugging.api.network.Network$WebSocketFrame
 outer org.netbeans.modules.web.webkit.debugging.api.network.Network
meth public int getOpcode()
meth public java.lang.String getPayload()
meth public java.util.Date getTimestamp()
meth public org.netbeans.modules.web.webkit.debugging.api.network.Network$Direction getDirection()
supr java.lang.Object
hfds data,direction,opcode,timestamp

CLSS public final static org.netbeans.modules.web.webkit.debugging.api.network.Network$WebSocketRequest
 outer org.netbeans.modules.web.webkit.debugging.api.network.Network
fld public final static java.lang.String PROP_CLOSED = "Network.WebSocketRequest.Closed"
fld public final static java.lang.String PROP_FRAMES = "Network.WebSocketRequest.Frame"
fld public final static java.lang.String PROP_HANDSHAKE_REQUEST = "Network.WebSocketRequest.Handshake.Request"
fld public final static java.lang.String PROP_HANDSHAKE_RESPONSE = "Network.WebSocketRequest.Handshake.Response"
meth public boolean isClosed()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getURL()
meth public java.util.List<org.netbeans.modules.web.webkit.debugging.api.network.Network$WebSocketFrame> getFrames()
meth public org.json.simple.JSONObject getHandshakeRequest()
meth public org.json.simple.JSONObject getHandshakeResponse()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds closed,errorMessage,frames,handshakeRequest,handshakeResponse,requestId,support,url

CLSS public org.netbeans.modules.web.webkit.debugging.api.page.Page
cons public init(org.netbeans.modules.web.webkit.debugging.TransportHelper,org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging)
meth public boolean isEnabled()
meth public void disable()
meth public void enable()
meth public void navigate(java.lang.String)
meth public void reload(boolean,java.lang.String)
meth public void reload(boolean,java.lang.String,java.lang.String)
supr java.lang.Object
hfds callback,enabled,numberOfClients,transport
hcls Callback

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.BrowserConsoleLoggerFactory
meth public abstract org.openide.util.Lookup createBrowserConsoleLogger(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public abstract void stopBrowserConsoleLogger(org.openide.util.Lookup)

CLSS public final org.netbeans.modules.web.webkit.debugging.spi.Command
cons public init(java.lang.String)
cons public init(java.lang.String,org.json.simple.JSONObject)
fld public final static java.lang.String COMMAND_ID = "id"
fld public final static java.lang.String COMMAND_METHOD = "method"
fld public final static java.lang.String COMMAND_PARAMS = "params"
fld public final static java.lang.String COMMAND_RESULT = "result"
meth public int getID()
meth public java.lang.String toString()
meth public org.json.simple.JSONObject getCommand()
supr java.lang.Object
hfds command,uniqueCommandID

CLSS public final org.netbeans.modules.web.webkit.debugging.spi.Factory
meth public static org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging createWebKitDebugging(org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.JavaScriptDebuggerFactory
meth public abstract org.netbeans.api.debugger.Session createDebuggingSession(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public abstract void stopDebuggingSession(org.netbeans.api.debugger.Session)

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.LiveHTMLImplementation
meth public abstract void storeDataEvent(java.net.URL,long,java.lang.String,java.lang.String,java.lang.String)
meth public abstract void storeDocumentVersionAfterChange(java.net.URL,long,java.lang.String)
meth public abstract void storeDocumentVersionBeforeChange(java.net.URL,long,java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.NetworkMonitorFactory
meth public abstract org.openide.util.Lookup createNetworkMonitor(org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging,org.openide.util.Lookup)
meth public abstract void stopNetworkMonitor(org.openide.util.Lookup)

CLSS public final org.netbeans.modules.web.webkit.debugging.spi.Response
cons public init(org.json.simple.JSONObject)
cons public init(org.json.simple.JSONObject,org.netbeans.modules.web.webkit.debugging.api.TransportStateException)
cons public init(org.netbeans.modules.web.webkit.debugging.api.TransportStateException)
meth public int getID()
meth public java.lang.String getMethod()
meth public java.lang.String toString()
meth public org.json.simple.JSONObject getParams()
meth public org.json.simple.JSONObject getResponse()
meth public org.json.simple.JSONObject getResult()
meth public org.netbeans.modules.web.webkit.debugging.api.TransportStateException getException()
supr java.lang.Object
hfds response,transportEx

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback
meth public abstract void handleResponse(org.netbeans.modules.web.webkit.debugging.spi.Response)

CLSS public abstract interface org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation
fld public final static java.lang.String VERSION_1 = "version 1.0"
fld public final static java.lang.String VERSION_UNKNOWN_BEFORE_requestChildNodes = "version without requestChildNodes"
meth public abstract boolean attach()
meth public abstract boolean detach()
meth public abstract java.lang.String getConnectionName()
meth public abstract java.lang.String getVersion()
meth public abstract java.net.URL getConnectionURL()
meth public abstract void registerResponseCallback(org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback)
meth public abstract void sendCommand(org.netbeans.modules.web.webkit.debugging.spi.Command) throws org.netbeans.modules.web.webkit.debugging.api.TransportStateException

