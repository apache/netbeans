#Signature file v4.1
#Version 1.70

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

CLSS public org.netbeans.modules.progress.spi.Controller
cons public init(org.netbeans.modules.progress.spi.ProgressUIWorker)
fld public final static int INITIAL_DELAY = 500
fld public static org.netbeans.modules.progress.spi.Controller defaultInstance
meth protected final org.netbeans.modules.progress.spi.ProgressUIWorker getProgressUIWorker()
meth protected java.util.concurrent.Executor getEventExecutor()
meth protected org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel createWorker()
meth protected void resetTimer(int,boolean)
meth protected void runEvents()
meth public org.netbeans.modules.progress.spi.TaskModel getModel()
meth public static org.netbeans.modules.progress.spi.Controller getDefault()
meth public void runNow()
supr java.lang.Object
hfds LOG,RQ,TIMER_QUANTUM,component,dispatchRunning,eventQueue,model,task,taskDelay,timerStart

CLSS public org.netbeans.modules.progress.spi.ControllerCompat
cons public init()
fld protected javax.swing.Timer timer
meth protected void compatPostInit(javax.swing.Timer)
meth public java.awt.Component getVisualComponent()
supr java.lang.Object

CLSS public org.netbeans.modules.progress.spi.InternalHandle
cons protected init(java.lang.String,org.openide.util.Cancellable,boolean)
fld public final static int NO_INCREASE = -2
fld public final static int STATE_FINISHED = 2
fld public final static int STATE_INITIALIZED = 0
fld public final static int STATE_REQUEST_STOP = 3
fld public final static int STATE_RUNNING = 1
meth protected final void markCustomPlaced()
meth protected final void setController(org.netbeans.modules.progress.spi.Controller)
meth public boolean isAllowCancel()
meth public boolean isAllowView()
meth public boolean isCustomPlaced()
meth public boolean isInSleepMode()
meth public boolean requestAction(java.lang.String,javax.swing.Action)
meth public double getPercentageDone()
meth public final boolean isUserInitialized()
meth public final org.netbeans.api.progress.ProgressHandle createProgressHandle()
meth public int getInitialDelay()
meth public int getState()
meth public int getTotalUnits()
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public long getLastPingTime()
meth public long getTimeStampStarted()
meth public org.netbeans.modules.progress.spi.ProgressEvent requestStateSnapshot()
meth public void finish()
meth public void progress(java.lang.String,int)
meth public void requestCancel()
meth public void requestDisplayNameChange(java.lang.String)
meth public void requestExplicitSelection()
meth public void requestView()
meth public void setInitialDelay(int)
meth public void start(java.lang.String,int,long)
meth public void toDeterminate(int,long)
meth public void toIndeterminate()
meth public void toSilent(java.lang.String)
supr java.lang.Object
hfds LOG,cancelable,compatInit,controller,currentUnit,customPlaced,del,displayName,handle,initialDelay,initialEstimate,lastMessage,state,timeLastProgress,timeSleepy,timeStarted,totalUnits,userInitiated

CLSS public org.netbeans.modules.progress.spi.InternalHandleCompat
cons public init()
meth protected void compatInit(java.lang.String,org.openide.util.Cancellable,boolean)
meth public javax.swing.JComponent extractComponent()
meth public javax.swing.JLabel extractDetailLabel()
meth public javax.swing.JLabel extractMainLabel()
meth public static void create(org.netbeans.modules.progress.spi.InternalHandleCompat,java.lang.String,org.openide.util.Cancellable,boolean,javax.swing.Action)
supr java.lang.Object
hfds component,ctor,delegate,detailLabel,mainLabel,uiClazz

CLSS public abstract interface org.netbeans.modules.progress.spi.ProgressEnvironment
meth public abstract org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,org.openide.util.Cancellable,boolean)
meth public abstract org.netbeans.modules.progress.spi.Controller getController()

CLSS public final org.netbeans.modules.progress.spi.ProgressEvent
cons public init(org.netbeans.modules.progress.spi.InternalHandle,int,boolean)
cons public init(org.netbeans.modules.progress.spi.InternalHandle,int,boolean,java.lang.String)
cons public init(org.netbeans.modules.progress.spi.InternalHandle,java.lang.String,int,double,long,boolean)
cons public init(org.netbeans.modules.progress.spi.InternalHandle,java.lang.String,int,double,long,boolean,java.lang.String)
fld public final static int TYPE_FINISH = 4
fld public final static int TYPE_PROGRESS = 1
fld public final static int TYPE_REQUEST_STOP = 3
fld public final static int TYPE_SILENT = 6
fld public final static int TYPE_START = 0
fld public final static int TYPE_SWITCH = 5
meth public boolean isSwitched()
meth public boolean isWatched()
meth public double getPercentageDone()
meth public int getType()
meth public int getWorkunitsDone()
meth public java.lang.String getDisplayName()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public long getEstimatedCompletion()
meth public org.netbeans.modules.progress.spi.InternalHandle getSource()
meth public void copyMessageFromEarlier(org.netbeans.modules.progress.spi.ProgressEvent)
meth public void markAsSwitched()
supr java.lang.Object
hfds displayName,estimatedCompletion,message,percentageDone,source,switched,type,watched,workunitsDone

CLSS public abstract interface org.netbeans.modules.progress.spi.ProgressUIWorker
meth public abstract void processProgressEvent(org.netbeans.modules.progress.spi.ProgressEvent)
meth public abstract void processSelectedProgressEvent(org.netbeans.modules.progress.spi.ProgressEvent)

CLSS public abstract interface org.netbeans.modules.progress.spi.ProgressUIWorkerWithModel
intf org.netbeans.modules.progress.spi.ProgressUIWorker
meth public abstract void setModel(org.netbeans.modules.progress.spi.TaskModel)
meth public abstract void showPopup()

CLSS public abstract interface org.netbeans.modules.progress.spi.RunOffEDTProvider
innr public abstract interface static Progress
innr public abstract interface static Progress2
meth public abstract void runOffEventDispatchThread(java.lang.Runnable,java.lang.String,java.util.concurrent.atomic.AtomicBoolean,boolean,int,int)

CLSS public abstract interface static org.netbeans.modules.progress.spi.RunOffEDTProvider$Progress
 outer org.netbeans.modules.progress.spi.RunOffEDTProvider
intf org.netbeans.modules.progress.spi.RunOffEDTProvider
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> showProgressDialogAndRunLater(org.netbeans.api.progress.ProgressRunnable<{%%0}>,org.netbeans.api.progress.ProgressHandle,boolean)
meth public abstract <%0 extends java.lang.Object> {%%0} showProgressDialogAndRun(org.netbeans.api.progress.ProgressRunnable<{%%0}>,java.lang.String,boolean)
meth public abstract void showProgressDialogAndRun(java.lang.Runnable,org.netbeans.api.progress.ProgressHandle,boolean)

CLSS public abstract interface static org.netbeans.modules.progress.spi.RunOffEDTProvider$Progress2
 outer org.netbeans.modules.progress.spi.RunOffEDTProvider
intf org.netbeans.modules.progress.spi.RunOffEDTProvider$Progress
meth public abstract void runOffEventThreadWithCustomDialogContent(java.lang.Runnable,java.lang.String,javax.swing.JPanel,int,int)
 anno 0 java.lang.Deprecated()
meth public abstract void runOffEventThreadWithProgressDialog(java.lang.Runnable,java.lang.String,org.netbeans.api.progress.ProgressHandle,boolean,int,int)

CLSS public final org.netbeans.modules.progress.spi.TaskModel
cons public init()
meth public int getSize()
meth public org.netbeans.modules.progress.spi.InternalHandle getExplicitSelection()
meth public org.netbeans.modules.progress.spi.InternalHandle getSelectedHandle()
meth public org.netbeans.modules.progress.spi.InternalHandle[] getHandles()
meth public void addHandle(org.netbeans.modules.progress.spi.InternalHandle)
meth public void addListDataListener(javax.swing.event.ListDataListener)
meth public void addListSelectionListener(javax.swing.event.ListSelectionListener)
meth public void explicitlySelect(org.netbeans.modules.progress.spi.InternalHandle)
meth public void removeHandle(org.netbeans.modules.progress.spi.InternalHandle)
meth public void removeListDataListener(javax.swing.event.ListDataListener)
meth public void removeListSelectionListener(javax.swing.event.ListSelectionListener)
supr java.lang.Object
hfds dataListeners,eventExecutor,explicit,model,selectionListeners,selectionModel
hcls TaskListener

CLSS abstract interface org.netbeans.modules.progress.spi.package-info

CLSS public abstract interface !annotation org.openide.modules.PatchFor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

