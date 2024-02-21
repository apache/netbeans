#Signature file v4.1
#Version 1.33

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

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

CLSS public abstract interface java.util.EventListener

CLSS public org.netbeans.api.debugger.ActionsManagerAdapter
cons public init()
intf org.netbeans.api.debugger.ActionsManagerListener
meth public void actionPerformed(java.lang.Object)
meth public void actionStateChanged(java.lang.Object,boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.api.debugger.ActionsManagerListener
fld public final static java.lang.String PROP_ACTION_PERFORMED = "actionPerformed"
fld public final static java.lang.String PROP_ACTION_STATE_CHANGED = "actionStateChanged"
intf java.util.EventListener
meth public abstract void actionPerformed(java.lang.Object)
meth public abstract void actionStateChanged(java.lang.Object,boolean)

CLSS public abstract org.netbeans.api.debugger.LazyActionsManagerListener
cons public init()
innr public abstract interface static !annotation Registration
meth protected abstract void destroy()
meth public abstract java.lang.String[] getProperties()
supr org.netbeans.api.debugger.ActionsManagerAdapter
hcls ContextAware

CLSS public org.netbeans.modules.debugger.jpda.js.FirstSourceURLProvider
cons public init(org.netbeans.spi.debugger.ContextProvider)
meth public java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public java.lang.String getURL(java.lang.String,boolean)
meth public java.lang.String getURL(org.netbeans.api.debugger.jpda.JPDAClassType,java.lang.String)
meth public java.lang.String[] getOriginalSourceRoots()
meth public java.lang.String[] getSourceRoots()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setSourceRoots(java.lang.String[])
supr org.netbeans.spi.debugger.jpda.SourcePathProvider
hfds NO_SOURCE_ROOTS,contextProvider,pathPrefix,rootDirs,rootDirsLock,sourcePath
hcls SourcePathListener

CLSS public org.netbeans.modules.debugger.jpda.js.JSUtils
cons public init()
fld public final static java.lang.String JS_MIME_TYPE = "text/javascript"
fld public final static java.lang.String JS_STRATUM = "JS"
fld public final static java.lang.String NASHORN_SCRIPT = "jdk.nashorn.internal.scripts.Script$"
fld public final static java.lang.String VAR_CALLEE = ":callee"
fld public final static java.lang.String VAR_SCOPE = ":scope"
fld public final static java.lang.String VAR_THIS = ":this"
supr java.lang.Object

CLSS public org.netbeans.modules.debugger.jpda.js.StepIntoJSHandler
cons public init(org.netbeans.spi.debugger.ContextProvider)
intf java.beans.PropertyChangeListener
meth protected void destroy()
meth public java.lang.String[] getProperties()
meth public void actionPerformed(java.lang.Object)
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.api.debugger.LazyActionsManagerListener
hfds SCRIPT_ACCESS_CLASS,SCRIPT_ACCESS_METHODS,SCRIPT_NOTIFY_INVOKE_METHOD,SCRIPT_NOTIFY_INVOKE_METHOD_ARG,SCRIPT_NOTIFY_INVOKE_METHOD_SIG,debugger,isNotifyInvoke,logger,notifyInvokeBP,scriptAccessBPs
hcls CurrentSFTracker,InScriptBPListener,ScriptBPListener,ScriptInvokeBPListener

CLSS public org.netbeans.modules.debugger.jpda.js.StepThroughFiltersCheck
cons public init(org.netbeans.spi.debugger.ContextProvider)
intf java.beans.PropertyChangeListener
meth protected void destroy()
meth public java.lang.String[] getProperties()
meth public void propertyChange(java.beans.PropertyChangeEvent)
supr org.netbeans.api.debugger.LazyActionsManagerListener
hfds STEP_THROUGH_FILTERS_PROP,debugger,p,stepThroughFiltersTurnedOn,stepThroughFiltersWasNotSet

CLSS public final org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame
meth public org.netbeans.api.debugger.jpda.CallStackFrame getJavaFrame()
meth public static org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame get(org.netbeans.api.debugger.jpda.CallStackFrame)
meth public static org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame getExisting(org.netbeans.api.debugger.jpda.CallStackFrame)
supr java.lang.Object
hfds csf,framesCache

CLSS public final org.netbeans.modules.debugger.jpda.js.vars.DebuggerSupport
fld public final static java.lang.String DEBUGGER_SUPPORT_CLASS = "jdk.nashorn.internal.runtime.DebuggerSupport"
meth public static boolean hasSourceInfo(org.netbeans.api.debugger.jpda.JPDADebugger)
meth public static java.lang.String getVarValue(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.Variable)
meth public static org.netbeans.api.debugger.jpda.Variable evaluate(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public static org.netbeans.api.debugger.jpda.Variable evaluate(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.CallStackFrame,java.lang.String,org.netbeans.api.debugger.jpda.ObjectVariable) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public static org.netbeans.api.debugger.jpda.Variable getSourceInfo(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAClassType)
meth public static org.netbeans.api.debugger.jpda.Variable getVarStringValueAsVar(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.ObjectVariable)
supr java.lang.Object
hfds CONTEXT_CLASS,DEBUGGER_SUPPORT_VALUE_DESC_CLASS,FIELD_DESC_EXPANDABLE,FIELD_DESC_KEY,FIELD_DESC_VALUE_AS_OBJECT,FIELD_DESC_VALUE_AS_STRING,METHOD_CONTEXT_EVAL,METHOD_EVAL,METHOD_FROM_CLASS,METHOD_SOURCE_INFO,METHOD_VALUE_AS_STRING,METHOD_VALUE_INFO,METHOD_VALUE_INFOS,SIGNAT_CONTEXT_EVAL,SIGNAT_CONTEXT_EVAL_OLD,SIGNAT_EVAL,SIGNAT_FROM_CLASS,SIGNAT_SOURCE_INFO,SIGNAT_VALUE_AS_STRING,SIGNAT_VALUE_INFO,SIGNAT_VALUE_INFOS,hasOldEval

CLSS public org.netbeans.modules.debugger.jpda.js.vars.JSEvaluator
cons public init(org.netbeans.spi.debugger.ContextProvider)
intf org.netbeans.spi.debugger.jpda.Evaluator<org.netbeans.modules.debugger.jpda.js.vars.JSExpression>
meth public org.netbeans.spi.debugger.jpda.Evaluator$Result evaluate(org.netbeans.spi.debugger.jpda.Evaluator$Expression<org.netbeans.modules.debugger.jpda.js.vars.JSExpression>,org.netbeans.spi.debugger.jpda.Evaluator$Context) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
supr java.lang.Object
hfds debugger

CLSS public final org.netbeans.modules.debugger.jpda.js.vars.JSExpression
meth public java.lang.String getExpression()
meth public static org.netbeans.modules.debugger.jpda.js.vars.JSExpression parse(java.lang.String)
supr java.lang.Object
hfds expr

CLSS public org.netbeans.modules.debugger.jpda.js.vars.JSThis
cons protected init(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.Variable)
meth public static org.netbeans.modules.debugger.jpda.js.vars.JSVariable create(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.LocalVariable)
supr org.netbeans.modules.debugger.jpda.js.vars.JSVariable

CLSS public org.netbeans.modules.debugger.jpda.js.vars.JSVariable
cons protected init(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.Variable)
meth public boolean isExpandable()
meth public java.lang.String getKey()
meth public java.lang.String getValue()
meth public org.netbeans.api.debugger.jpda.ObjectVariable getValueObject()
meth public org.netbeans.modules.debugger.jpda.js.vars.JSVariable[] getChildren()
meth public static org.netbeans.modules.debugger.jpda.js.vars.JSVariable create(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.LocalVariable)
meth public static org.netbeans.modules.debugger.jpda.js.vars.JSVariable createIfScriptObject(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.ObjectVariable,java.lang.String)
meth public static org.netbeans.modules.debugger.jpda.js.vars.JSVariable[] createScopeVars(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.Variable)
supr java.lang.Object
hfds debugger,expandable,key,value,valueInfoDesc,valueObject

CLSS public final org.netbeans.modules.debugger.jpda.js.vars.ScopeVariable
meth public java.lang.String getName()
meth public org.netbeans.modules.debugger.jpda.js.vars.JSVariable[] getScopeVars()
meth public static org.netbeans.modules.debugger.jpda.js.vars.ScopeVariable create(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.LocalVariable)
supr java.lang.Object
hfds debugger,lv,name,scopeVars

CLSS public abstract interface org.netbeans.spi.debugger.jpda.Evaluator<%0 extends java.lang.Object>
innr public abstract interface static !annotation Registration
innr public final static Context
innr public final static Expression
innr public final static Result
meth public abstract org.netbeans.spi.debugger.jpda.Evaluator$Result evaluate(org.netbeans.spi.debugger.jpda.Evaluator$Expression<{org.netbeans.spi.debugger.jpda.Evaluator%0}>,org.netbeans.spi.debugger.jpda.Evaluator$Context) throws org.netbeans.api.debugger.jpda.InvalidExpressionException

CLSS public abstract org.netbeans.spi.debugger.jpda.SourcePathProvider
cons public init()
fld public final static java.lang.String PROP_SOURCE_ROOTS = "sourceRoots"
innr public abstract interface static !annotation Registration
meth public abstract java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public abstract java.lang.String getURL(java.lang.String,boolean)
meth public abstract java.lang.String[] getOriginalSourceRoots()
meth public abstract java.lang.String[] getSourceRoots()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setSourceRoots(java.lang.String[])
meth public java.lang.String getSourceRoot(java.lang.String)
supr java.lang.Object
hcls ContextAware

