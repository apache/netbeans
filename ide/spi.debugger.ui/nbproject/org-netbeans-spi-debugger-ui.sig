#Signature file v4.1
#Version 2.81

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public final org.netbeans.api.debugger.Watch
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_EXPRESSION = "expression"
fld public final static java.lang.String PROP_VALUE = "value"
innr public abstract interface static Pin
meth public boolean isEnabled()
meth public java.lang.String getExpression()
meth public org.netbeans.api.debugger.Watch$Pin getPin()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void remove()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
meth public void setExpression(java.lang.String)
supr java.lang.Object
hfds enabled,expression,pcs,pin

CLSS public abstract interface static org.netbeans.api.debugger.Watch$Pin
 outer org.netbeans.api.debugger.Watch

CLSS public abstract org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction
cons protected init()
meth protected abstract void openTooltipView()
meth protected final org.netbeans.editor.ext.ToolTipSupport openTooltipView(java.lang.String,java.lang.Object)
meth public final void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds expanded,toCollapsIcon,toExpandIcon

CLSS public abstract org.netbeans.spi.debugger.ui.AttachType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.AttachType$Registration
 outer org.netbeans.spi.debugger.ui.AttachType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String displayName()

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointAnnotation
cons public init()
meth public abstract org.netbeans.api.debugger.Breakpoint getBreakpoint()
supr org.openide.text.Annotation

CLSS public abstract org.netbeans.spi.debugger.ui.BreakpointType
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract boolean isDefault()
meth public abstract java.lang.String getCategoryDisplayName()
meth public abstract javax.swing.JComponent getCustomizer()
meth public java.lang.String getTypeDisplayName()
meth public org.netbeans.spi.debugger.ui.Controller getController()
supr java.lang.Object
hcls ContextAware

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.BreakpointType$Registration
 outer org.netbeans.spi.debugger.ui.BreakpointType
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String path()
meth public abstract java.lang.String displayName()

CLSS public final org.netbeans.spi.debugger.ui.CodeEvaluator
innr public abstract static EvaluatorService
innr public final static DefaultExpressionsHistoryPersistence
innr public final static Result
meth public static org.netbeans.spi.debugger.ui.CodeEvaluator getDefault()
meth public void open()
meth public void requestFocus()
meth public void setExpression(java.lang.String)
supr java.lang.Object
hfds INSTANCE

CLSS public final static org.netbeans.spi.debugger.ui.CodeEvaluator$DefaultExpressionsHistoryPersistence
 outer org.netbeans.spi.debugger.ui.CodeEvaluator
meth public java.util.List<java.lang.String> getExpressions()
meth public static org.netbeans.spi.debugger.ui.CodeEvaluator$DefaultExpressionsHistoryPersistence create(java.lang.String)
meth public void addExpression(java.lang.String)
supr java.lang.Object
hfds NUM_HISTORY_ITEMS,editItemsList,editItemsSet,engineName

CLSS public abstract static org.netbeans.spi.debugger.ui.CodeEvaluator$EvaluatorService
 outer org.netbeans.spi.debugger.ui.CodeEvaluator
cons public init()
fld public final static java.lang.String PROP_CAN_EVALUATE = "canEvaluate"
fld public final static java.lang.String PROP_EXPRESSIONS_HISTORY = "expressionsHistory"
innr public abstract interface static !annotation Registration
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean canEvaluate()
meth public abstract java.util.List<java.lang.String> getExpressionsHistory()
meth public abstract void evaluate(java.lang.String)
meth public abstract void setupContext(javax.swing.JEditorPane,java.lang.Runnable)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds pchs

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.CodeEvaluator$EvaluatorService$Registration
 outer org.netbeans.spi.debugger.ui.CodeEvaluator$EvaluatorService
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String path()

CLSS public final static org.netbeans.spi.debugger.ui.CodeEvaluator$Result<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.netbeans.spi.debugger.ui.CodeEvaluator
innr public abstract interface static Listener
innr public final static DefaultHistoryItem
meth public java.lang.String getExpression()
meth public java.util.List<{org.netbeans.spi.debugger.ui.CodeEvaluator$Result%1}> getHistoryItems()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.spi.debugger.ui.CodeEvaluator$Result<{%%0},{%%1}> get(org.netbeans.api.debugger.DebuggerEngine)
meth public void addListener(org.netbeans.spi.debugger.ui.CodeEvaluator$Result$Listener<{org.netbeans.spi.debugger.ui.CodeEvaluator$Result%0}>)
meth public void removeListener(org.netbeans.spi.debugger.ui.CodeEvaluator$Result$Listener<{org.netbeans.spi.debugger.ui.CodeEvaluator$Result%0}>)
meth public void setAndOpen(java.lang.String,{org.netbeans.spi.debugger.ui.CodeEvaluator$Result%0},{org.netbeans.spi.debugger.ui.CodeEvaluator$Result%1})
meth public void setMaxHistoryItems(int)
meth public {org.netbeans.spi.debugger.ui.CodeEvaluator$Result%0} getResult()
supr java.lang.Object
hfds ENGINE_HASH_MAP,engine,expression,historyItems,historyItemsRO,lastHistoryItem,listeners,maxHistoryItems,preferences,result,resultView

CLSS public final static org.netbeans.spi.debugger.ui.CodeEvaluator$Result$DefaultHistoryItem
 outer org.netbeans.spi.debugger.ui.CodeEvaluator$Result
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getExpression()
meth public java.lang.String getToStringValue()
meth public java.lang.String getTooltip()
meth public java.lang.String getType()
meth public java.lang.String getValue()
supr java.lang.Object
hfds expression,toStringValue,tooltip,type,value

CLSS public abstract interface static org.netbeans.spi.debugger.ui.CodeEvaluator$Result$Listener<%0 extends java.lang.Object>
 outer org.netbeans.spi.debugger.ui.CodeEvaluator$Result
meth public abstract void resultChanged({org.netbeans.spi.debugger.ui.CodeEvaluator$Result$Listener%0})

CLSS public abstract interface !annotation org.netbeans.spi.debugger.ui.ColumnModelRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String path()

CLSS public abstract interface !annotation org.netbeans.spi.debugger.ui.ColumnModelRegistrations
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.debugger.ui.ColumnModelRegistration[] value()

CLSS public abstract interface org.netbeans.spi.debugger.ui.Constants
fld public final static java.lang.String BREAKPOINT_ENABLED_COLUMN_ID = "BreakpointEnabled"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String CALL_STACK_FRAME_LOCATION_COLUMN_ID = "CallStackFrameLocation"
fld public final static java.lang.String LOCALS_TO_STRING_COLUMN_ID = "LocalsToString"
fld public final static java.lang.String LOCALS_TYPE_COLUMN_ID = "LocalsType"
fld public final static java.lang.String LOCALS_VALUE_COLUMN_ID = "LocalsValue"
fld public final static java.lang.String SESSION_HOST_NAME_COLUMN_ID = "SessionHostName"
fld public final static java.lang.String SESSION_LANGUAGE_COLUMN_ID = "SessionLanguage"
fld public final static java.lang.String SESSION_STATE_COLUMN_ID = "SessionState"
fld public final static java.lang.String THREAD_STATE_COLUMN_ID = "ThreadState"
fld public final static java.lang.String THREAD_SUSPENDED_COLUMN_ID = "ThreadSuspended"
fld public final static java.lang.String WATCH_TO_STRING_COLUMN_ID = "WatchToString"
fld public final static java.lang.String WATCH_TYPE_COLUMN_ID = "WatchType"
fld public final static java.lang.String WATCH_VALUE_COLUMN_ID = "WatchValue"

CLSS public abstract interface org.netbeans.spi.debugger.ui.Controller
fld public final static java.lang.String PROP_VALID = "valid"
meth public abstract boolean cancel()
meth public abstract boolean isValid()
meth public abstract boolean ok()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.spi.debugger.ui.DebuggingView
innr public abstract interface static DVFrame
innr public abstract interface static DVThread
innr public abstract interface static DVThreadGroup
innr public abstract static DVSupport
innr public final static DVFilter
innr public final static Deadlock
innr public final static PopException
meth public org.openide.windows.TopComponent getViewTC()
meth public static org.netbeans.spi.debugger.ui.DebuggingView getDefault()
supr java.lang.Object
hfds INSTANCE,dvcRef

CLSS public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter
 outer org.netbeans.spi.debugger.ui.DebuggingView
innr public final static !enum DefaultFilter
innr public final static Group
meth public boolean isSelected()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getPrefKey()
meth public java.lang.String getTooltip()
meth public java.util.prefs.Preferences getPreferences()
meth public javax.swing.Icon getIcon()
meth public org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$Group getGroup()
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter create(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,java.util.prefs.Preferences,java.lang.String,boolean,org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$Group)
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter getDefault(org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter)
meth public void setSelected(boolean)
supr java.lang.Object
hfds fimpl,group,sortGroupRef

CLSS public final static !enum org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter
 outer org.netbeans.spi.debugger.ui.DebuggingView$DVFilter
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showMonitors
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showQualifiedNames
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showSuspendTable
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showSuspendedThreadsOnly
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showSystemThreads
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter showThreadGroups
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter sortAlphabetic
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter sortNatural
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter sortSuspend
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter valueOf(java.lang.String)
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter[] values()
supr java.lang.Enum<org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$DefaultFilter>

CLSS public final static org.netbeans.spi.debugger.ui.DebuggingView$DVFilter$Group
 outer org.netbeans.spi.debugger.ui.DebuggingView$DVFilter
cons public init()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFilter> getItems()
supr java.lang.Object
hfds items

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVFrame
 outer org.netbeans.spi.debugger.ui.DebuggingView
meth public abstract int getColumn()
meth public abstract int getLine()
meth public abstract java.lang.String getName()
meth public abstract java.net.URI getSourceURI()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread getThread()
meth public abstract void makeCurrent()
meth public java.lang.String getSourceMimeType()
meth public void popOff() throws org.netbeans.spi.debugger.ui.DebuggingView$PopException

CLSS public abstract static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport
 outer org.netbeans.spi.debugger.ui.DebuggingView
cons protected init()
fld public final static java.lang.String PROP_CURRENT_THREAD = "currentThread"
fld public final static java.lang.String PROP_DEADLOCK = "deadlock"
fld public final static java.lang.String PROP_STATE = "state"
fld public final static java.lang.String PROP_THREAD_DIED = "threadDied"
fld public final static java.lang.String PROP_THREAD_GROUP_ADDED = "threadGroupAdded"
fld public final static java.lang.String PROP_THREAD_RESUMED = "threadResumed"
fld public final static java.lang.String PROP_THREAD_STARTED = "threadStarted"
fld public final static java.lang.String PROP_THREAD_SUSPENDED = "threadSuspended"
innr public abstract interface static !annotation Registration
innr public final static !enum STATE
meth protected abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFilter> getFilters()
meth protected final org.netbeans.spi.debugger.ui.DebuggingView$Deadlock createDeadlock(java.util.Collection<org.netbeans.spi.debugger.ui.DebuggingView$DVThread>)
meth protected final void firePropertyChange(java.beans.PropertyChangeEvent)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract java.awt.Image getIcon(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public abstract java.lang.String getDisplayName(org.netbeans.spi.debugger.ui.DebuggingView$DVThread)
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getAllThreads()
meth public abstract java.util.Set<org.netbeans.spi.debugger.ui.DebuggingView$Deadlock> getDeadlocks()
meth public abstract org.netbeans.api.debugger.Session getSession()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE getState()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread getCurrentThread()
meth public abstract void resume()
meth public final javax.swing.Action[] getFilterActions()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getDisplayName(org.netbeans.spi.debugger.ui.DebuggingView$DVFrame)
supr java.lang.Object
hfds pcs

CLSS public abstract interface static !annotation org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$Registration
 outer org.netbeans.spi.debugger.ui.DebuggingView$DVSupport
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String path()

CLSS public final static !enum org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE
 outer org.netbeans.spi.debugger.ui.DebuggingView$DVSupport
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE DISCONNECTED
fld public final static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE RUNNING
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE valueOf(java.lang.String)
meth public static org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE[] values()
supr java.lang.Enum<org.netbeans.spi.debugger.ui.DebuggingView$DVSupport$STATE>

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVThread
 outer org.netbeans.spi.debugger.ui.DebuggingView
fld public final static java.lang.String PROP_BREAKPOINT = "currentBreakpoint"
fld public final static java.lang.String PROP_LOCKER_THREADS = "lockerThreads"
fld public final static java.lang.String PROP_SUSPENDED = "suspended"
meth public abstract boolean isInStep()
meth public abstract boolean isSuspended()
meth public abstract java.lang.String getName()
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getLockerThreads()
meth public abstract org.netbeans.api.debugger.Breakpoint getCurrentBreakpoint()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVSupport getDVSupport()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void makeCurrent()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void resume()
meth public abstract void resumeBlockingThreads()
meth public abstract void suspend()
meth public int getFrameCount()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames()
meth public java.util.List<org.netbeans.spi.debugger.ui.DebuggingView$DVFrame> getFrames(int,int)

CLSS public abstract interface static org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup
 outer org.netbeans.spi.debugger.ui.DebuggingView
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup getParentThreadGroup()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThreadGroup[] getThreadGroups()
meth public abstract org.netbeans.spi.debugger.ui.DebuggingView$DVThread[] getThreads()

CLSS public final static org.netbeans.spi.debugger.ui.DebuggingView$Deadlock
 outer org.netbeans.spi.debugger.ui.DebuggingView
meth public java.util.Collection<org.netbeans.spi.debugger.ui.DebuggingView$DVThread> getThreads()
supr java.lang.Object
hfds threads

CLSS public final static org.netbeans.spi.debugger.ui.DebuggingView$PopException
 outer org.netbeans.spi.debugger.ui.DebuggingView
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public final org.netbeans.spi.debugger.ui.EditorContextDispatcher
fld public final static java.lang.String PROP_EDITOR = "editor"
fld public final static java.lang.String PROP_FILE = "file"
meth public int getCurrentLineNumber()
meth public int getMostRecentLineNumber()
meth public java.lang.String getCurrentURLAsString()
meth public java.lang.String getMostRecentURLAsString()
meth public java.util.Set<java.lang.String> getMIMETypesOnCurrentLine()
meth public java.util.Set<java.lang.String> getMIMETypesOnLine(org.openide.text.Line)
meth public javax.swing.JEditorPane getCurrentEditor()
meth public javax.swing.JEditorPane getMostRecentEditor()
meth public org.openide.filesystems.FileObject getCurrentFile()
meth public org.openide.filesystems.FileObject getMostRecentFile()
meth public org.openide.text.Line getCurrentLine()
meth public org.openide.text.Line getMostRecentLine()
meth public static org.netbeans.spi.debugger.ui.EditorContextDispatcher getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds NO_FILE,NO_FILE_CHANGE,NO_TEXT_COMPONENT,ccrp,context,currentFile,currentFileChangeListener,currentFileChangeListenerWeak,currentTextComponent,currentURL,erListener,lastFiredMIMEType,lastMIMETypeEvents,logger,lookupCoalescedChange,mostRecentFileChangeListener,mostRecentFileChangeListenerWeak,mostRecentFileRef,pcs,pcsByMIMEType,refreshProcessor,resFileObject
hcls AddRemoveFileListenerInEQThread,CoalescedChange,EditorLookupListener,EditorRegistryListener,EventFirer,FileRenameListener

CLSS public final org.netbeans.spi.debugger.ui.EditorPin
cons public init(org.openide.filesystems.FileObject,int,java.awt.Point)
fld public final static java.lang.String PROP_COMMENT = "comment"
fld public final static java.lang.String PROP_LINE = "line"
fld public final static java.lang.String PROP_LOCATION = "location"
intf org.netbeans.api.debugger.Watch$Pin
meth public int getLine()
meth public java.awt.Point getLocation()
meth public java.lang.String getComment()
meth public org.openide.filesystems.FileObject getFile()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void move(int,java.awt.Point)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setComment(java.lang.String)
supr java.lang.Object
hfds comment,file,line,location,pchs,vpId

CLSS public abstract interface org.netbeans.spi.debugger.ui.EngineComponentsProvider
innr public abstract interface static ComponentProvider
innr public final static ComponentInfo
innr public static TopComponentProvider
meth public abstract java.util.List<org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo> getComponents()
meth public abstract void willCloseNotify(java.util.List<org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo>)

CLSS public final static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
meth public boolean isMinimized()
meth public boolean isOpened()
meth public java.awt.Component getComponent()
meth public java.lang.String toString()
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String)
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String,boolean)
meth public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentInfo create(java.lang.String,boolean,boolean)
supr java.lang.Object
hfds minimized,opened,provider

CLSS public abstract interface static org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentProvider
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
meth public abstract java.awt.Component getComponent()

CLSS public static org.netbeans.spi.debugger.ui.EngineComponentsProvider$TopComponentProvider
 outer org.netbeans.spi.debugger.ui.EngineComponentsProvider
intf org.netbeans.spi.debugger.ui.EngineComponentsProvider$ComponentProvider
meth public java.awt.Component getComponent()
meth public java.lang.String toString()
supr java.lang.Object
hfds tcId

CLSS public org.netbeans.spi.debugger.ui.MethodChooser
cons public init(java.lang.String,org.netbeans.spi.debugger.ui.MethodChooser$Segment[],int)
cons public init(java.lang.String,org.netbeans.spi.debugger.ui.MethodChooser$Segment[],int,java.lang.String,javax.swing.KeyStroke[],javax.swing.KeyStroke[])
innr public abstract interface static ReleaseListener
innr public static Segment
meth public boolean isUIActive()
meth public boolean showUI()
meth public int getSelectedIndex()
meth public static org.netbeans.spi.editor.highlighting.HighlightsLayerFactory createHighlihgtsLayerFactory()
meth public void addReleaseListener(org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener)
meth public void releaseUI(boolean)
meth public void removeReleaseListener(org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener)
supr java.lang.Object
hfds arrowCursor,attribsAll,attribsAllUnc,attribsArea,attribsHyperlink,attribsLeft,attribsLeftUnc,attribsMethod,attribsMethodUnc,attribsMiddle,attribsMiddleUnc,attribsRight,attribsRightUnc,confirmEvents,defaultHyperlinkHighlight,doc,editorPane,endLine,handCursor,hintText,isInSelectMode,mainListener,mousedIndex,originalCursor,releaseListeners,segments,selectedIndex,startLine,stopEvents,url
hcls CentralListener,MethodChooserHighlightsLayerFactory,TooltipResolver

CLSS public abstract interface static org.netbeans.spi.debugger.ui.MethodChooser$ReleaseListener
 outer org.netbeans.spi.debugger.ui.MethodChooser
meth public abstract void released(boolean)

CLSS public static org.netbeans.spi.debugger.ui.MethodChooser$Segment
 outer org.netbeans.spi.debugger.ui.MethodChooser
cons public init(int,int)
meth public int getEndOffset()
meth public int getStartOffset()
supr java.lang.Object
hfds endOffset,startOffset

CLSS public abstract interface org.netbeans.spi.debugger.ui.PersistentController
intf org.netbeans.spi.debugger.ui.Controller
meth public abstract boolean load(org.netbeans.api.debugger.Properties)
meth public abstract java.lang.String getDisplayName()
meth public abstract void save(org.netbeans.api.debugger.Properties)

CLSS public final org.netbeans.spi.debugger.ui.PinWatchUISupport
innr public abstract interface static ValueProvider
meth public static org.netbeans.spi.debugger.ui.PinWatchUISupport getDefault()
meth public void pin(org.netbeans.api.debugger.Watch,java.lang.String)
supr java.lang.Object
hfds INSTANCE,valueProviders,valueProvidersLock
hcls DelegatingValueProvider

CLSS public abstract interface static org.netbeans.spi.debugger.ui.PinWatchUISupport$ValueProvider
 outer org.netbeans.spi.debugger.ui.PinWatchUISupport
innr public abstract interface static ValueChangeListener
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getValue(org.netbeans.api.debugger.Watch)
meth public abstract void setChangeListener(org.netbeans.api.debugger.Watch,org.netbeans.spi.debugger.ui.PinWatchUISupport$ValueProvider$ValueChangeListener)
meth public abstract void unsetChangeListener(org.netbeans.api.debugger.Watch)
meth public boolean setValue(org.netbeans.api.debugger.Watch,java.lang.String)
meth public java.lang.String getEditableValue(org.netbeans.api.debugger.Watch)
meth public java.lang.String getEvaluatingText()
meth public javax.swing.Action[] getHeadActions(org.netbeans.api.debugger.Watch)
meth public javax.swing.Action[] getTailActions(org.netbeans.api.debugger.Watch)

CLSS public abstract interface static org.netbeans.spi.debugger.ui.PinWatchUISupport$ValueProvider$ValueChangeListener
 outer org.netbeans.spi.debugger.ui.PinWatchUISupport$ValueProvider
meth public abstract void valueChanged(org.netbeans.api.debugger.Watch)

CLSS public final org.netbeans.spi.debugger.ui.ToolTipUI
innr public final static Expandable
innr public final static Pinnable
meth public org.netbeans.editor.ext.ToolTipSupport show(javax.swing.JEditorPane)
supr java.lang.Object
hfds editorPane,et
hcls ExpansionListener,PinListener

CLSS public final static org.netbeans.spi.debugger.ui.ToolTipUI$Expandable
 outer org.netbeans.spi.debugger.ui.ToolTipUI
cons public init(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds expression,variable

CLSS public final static org.netbeans.spi.debugger.ui.ToolTipUI$Pinnable
 outer org.netbeans.spi.debugger.ui.ToolTipUI
cons public init(java.lang.String,int,java.lang.String)
supr java.lang.Object
hfds expression,line,valueProviderId

CLSS public org.netbeans.spi.debugger.ui.ViewFactory
meth public javax.swing.JComponent createViewComponent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.spi.debugger.ui.ToolTipUI createToolTip(java.lang.String,org.netbeans.spi.debugger.ui.ToolTipUI$Expandable,org.netbeans.spi.debugger.ui.ToolTipUI$Pinnable)
meth public org.netbeans.spi.debugger.ui.ViewLifecycle createViewLifecycle(java.lang.String,java.lang.String)
meth public org.openide.windows.TopComponent createViewTC(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.debugger.ui.ViewFactory getDefault()
supr java.lang.Object
hfds vf

CLSS public final org.netbeans.spi.debugger.ui.ViewLifecycle
innr public abstract interface static ModelUpdateListener
meth public org.netbeans.spi.viewmodel.Models$CompoundModel getModel()
meth public void addModelUpdateListener(org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener)
meth public void destroy()
meth public void removeModelUpdateListener(org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener)
supr java.lang.Object
hfds cmul,vml
hcls CompoundModelUpdateListener

CLSS public abstract interface static org.netbeans.spi.debugger.ui.ViewLifecycle$ModelUpdateListener
 outer org.netbeans.spi.debugger.ui.ViewLifecycle
meth public abstract void modelUpdated(org.netbeans.spi.viewmodel.Models$CompoundModel,org.netbeans.api.debugger.DebuggerEngine)

CLSS public abstract org.openide.text.Annotation
cons public init()
fld public final static java.lang.String PROP_ANNOTATION_TYPE = "annotationType"
fld public final static java.lang.String PROP_MOVE_TO_FRONT = "moveToFront"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void notifyAttached(org.openide.text.Annotatable)
meth protected void notifyDetached(org.openide.text.Annotatable)
meth public abstract java.lang.String getAnnotationType()
meth public abstract java.lang.String getShortDescription()
meth public final org.openide.text.Annotatable getAttachedAnnotatable()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void attach(org.openide.text.Annotatable)
meth public final void detach()
meth public final void moveToFront()
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds attached,inDocument,support

