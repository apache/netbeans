#Signature file v4.1
#Version 1.55

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface javax.enterprise.deploy.model.DDBean
meth public abstract java.lang.String getAttributeValue(java.lang.String)
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getText()
meth public abstract java.lang.String getXpath()
meth public abstract java.lang.String[] getAttributeNames()
meth public abstract java.lang.String[] getText(java.lang.String)
meth public abstract javax.enterprise.deploy.model.DDBeanRoot getRoot()
meth public abstract javax.enterprise.deploy.model.DDBean[] getChildBean(java.lang.String)
meth public abstract void addXpathListener(java.lang.String,javax.enterprise.deploy.model.XpathListener)
meth public abstract void removeXpathListener(java.lang.String,javax.enterprise.deploy.model.XpathListener)

CLSS public abstract interface javax.enterprise.deploy.model.DDBeanRoot
intf javax.enterprise.deploy.model.DDBean
meth public abstract java.lang.String getDDBeanRootVersion()
meth public abstract java.lang.String getFilename()
meth public abstract java.lang.String getModuleDTDVersion()
meth public abstract java.lang.String getXpath()
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public abstract javax.enterprise.deploy.shared.ModuleType getType()

CLSS public abstract interface javax.enterprise.deploy.model.DeployableObject
meth public abstract java.io.InputStream getEntry(java.lang.String)
meth public abstract java.lang.Class getClassFromScope(java.lang.String)
meth public abstract java.lang.String getModuleDTDVersion()
meth public abstract java.lang.String[] getText(java.lang.String)
meth public abstract java.util.Enumeration entries()
meth public abstract javax.enterprise.deploy.model.DDBeanRoot getDDBeanRoot()
meth public abstract javax.enterprise.deploy.model.DDBeanRoot getDDBeanRoot(java.lang.String) throws java.io.FileNotFoundException,javax.enterprise.deploy.model.exceptions.DDBeanCreateException
meth public abstract javax.enterprise.deploy.model.DDBean[] getChildBean(java.lang.String)
meth public abstract javax.enterprise.deploy.shared.ModuleType getType()

CLSS public abstract interface javax.enterprise.deploy.model.J2eeApplicationObject
intf javax.enterprise.deploy.model.DeployableObject
meth public abstract java.lang.String[] getModuleUris()
meth public abstract java.lang.String[] getModuleUris(javax.enterprise.deploy.shared.ModuleType)
meth public abstract java.lang.String[] getText(javax.enterprise.deploy.shared.ModuleType,java.lang.String)
meth public abstract javax.enterprise.deploy.model.DDBean[] getChildBean(javax.enterprise.deploy.shared.ModuleType,java.lang.String)
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject(java.lang.String)
meth public abstract javax.enterprise.deploy.model.DeployableObject[] getDeployableObjects()
meth public abstract javax.enterprise.deploy.model.DeployableObject[] getDeployableObjects(javax.enterprise.deploy.shared.ModuleType)
meth public abstract void addXpathListener(javax.enterprise.deploy.shared.ModuleType,java.lang.String,javax.enterprise.deploy.model.XpathListener)
meth public abstract void removeXpathListener(javax.enterprise.deploy.shared.ModuleType,java.lang.String,javax.enterprise.deploy.model.XpathListener)

CLSS public final javax.enterprise.deploy.model.XpathEvent
cons public init(javax.enterprise.deploy.model.DDBean,java.lang.Object)
fld public final static java.lang.Object BEAN_ADDED
fld public final static java.lang.Object BEAN_CHANGED
fld public final static java.lang.Object BEAN_REMOVED
meth public boolean isAddEvent()
meth public boolean isChangeEvent()
meth public boolean isRemoveEvent()
meth public java.beans.PropertyChangeEvent getChangeEvent()
meth public javax.enterprise.deploy.model.DDBean getBean()
meth public void setChangeEvent(java.beans.PropertyChangeEvent)
supr java.lang.Object
hfds bean,changeEvent,typ

CLSS public abstract interface javax.enterprise.deploy.model.XpathListener
meth public abstract void fireXpathEvent(javax.enterprise.deploy.model.XpathEvent)

CLSS public javax.enterprise.deploy.model.exceptions.DDBeanCreateException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.shared.ActionType
cons protected init(int)
fld public final static javax.enterprise.deploy.shared.ActionType CANCEL
fld public final static javax.enterprise.deploy.shared.ActionType EXECUTE
fld public final static javax.enterprise.deploy.shared.ActionType STOP
meth protected int getOffset()
meth protected java.lang.String[] getStringTable()
meth protected javax.enterprise.deploy.shared.ActionType[] getEnumValueTable()
meth public int getValue()
meth public java.lang.String toString()
meth public static javax.enterprise.deploy.shared.ActionType getActionType(int)
supr java.lang.Object
hfds enumValueTable,stringTable,value

CLSS public javax.enterprise.deploy.shared.CommandType
cons protected init(int)
fld public final static javax.enterprise.deploy.shared.CommandType DISTRIBUTE
fld public final static javax.enterprise.deploy.shared.CommandType REDEPLOY
fld public final static javax.enterprise.deploy.shared.CommandType START
fld public final static javax.enterprise.deploy.shared.CommandType STOP
fld public final static javax.enterprise.deploy.shared.CommandType UNDEPLOY
meth protected int getOffset()
meth protected java.lang.String[] getStringTable()
meth protected javax.enterprise.deploy.shared.CommandType[] getEnumValueTable()
meth public int getValue()
meth public java.lang.String toString()
meth public static javax.enterprise.deploy.shared.CommandType getCommandType(int)
supr java.lang.Object
hfds enumValueTable,stringTable,value

CLSS public javax.enterprise.deploy.shared.DConfigBeanVersionType
cons protected init(int)
fld public final static javax.enterprise.deploy.shared.DConfigBeanVersionType V1_3
fld public final static javax.enterprise.deploy.shared.DConfigBeanVersionType V1_3_1
fld public final static javax.enterprise.deploy.shared.DConfigBeanVersionType V1_4
fld public final static javax.enterprise.deploy.shared.DConfigBeanVersionType V5
meth protected int getOffset()
meth protected java.lang.String[] getStringTable()
meth protected javax.enterprise.deploy.shared.DConfigBeanVersionType[] getEnumValueTable()
meth public int getValue()
meth public java.lang.String toString()
meth public static javax.enterprise.deploy.shared.DConfigBeanVersionType getDConfigBeanVersionType(int)
supr java.lang.Object
hfds enumValueTable,stringTable,value

CLSS public javax.enterprise.deploy.shared.ModuleType
cons protected init(int)
fld public final static javax.enterprise.deploy.shared.ModuleType CAR
fld public final static javax.enterprise.deploy.shared.ModuleType EAR
fld public final static javax.enterprise.deploy.shared.ModuleType EJB
fld public final static javax.enterprise.deploy.shared.ModuleType RAR
fld public final static javax.enterprise.deploy.shared.ModuleType WAR
meth protected int getOffset()
meth protected java.lang.String[] getStringTable()
meth protected javax.enterprise.deploy.shared.ModuleType[] getEnumValueTable()
meth public int getValue()
meth public java.lang.String getModuleExtension()
meth public java.lang.String toString()
meth public static javax.enterprise.deploy.shared.ModuleType getModuleType(int)
supr java.lang.Object
hfds enumValueTable,moduleExtension,stringTable,value

CLSS public javax.enterprise.deploy.shared.StateType
cons protected init(int)
fld public final static javax.enterprise.deploy.shared.StateType COMPLETED
fld public final static javax.enterprise.deploy.shared.StateType FAILED
fld public final static javax.enterprise.deploy.shared.StateType RELEASED
fld public final static javax.enterprise.deploy.shared.StateType RUNNING
meth protected int getOffset()
meth protected java.lang.String[] getStringTable()
meth protected javax.enterprise.deploy.shared.StateType[] getEnumValueTable()
meth public int getValue()
meth public java.lang.String toString()
meth public static javax.enterprise.deploy.shared.StateType getStateType(int)
supr java.lang.Object
hfds enumValueTable,stringTable,value

CLSS public final javax.enterprise.deploy.shared.factories.DeploymentFactoryManager
meth public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public javax.enterprise.deploy.spi.factories.DeploymentFactory[] getDeploymentFactories()
meth public static javax.enterprise.deploy.shared.factories.DeploymentFactoryManager getInstance()
meth public void registerDeploymentFactory(javax.enterprise.deploy.spi.factories.DeploymentFactory)
supr java.lang.Object
hfds deploymentFactories,deploymentFactoryManager

CLSS public abstract interface javax.enterprise.deploy.spi.DConfigBean
meth public abstract java.lang.String[] getXpaths()
meth public abstract javax.enterprise.deploy.model.DDBean getDDBean()
meth public abstract javax.enterprise.deploy.spi.DConfigBean getDConfigBean(javax.enterprise.deploy.model.DDBean) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void notifyDDChange(javax.enterprise.deploy.model.XpathEvent)
meth public abstract void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBean) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface javax.enterprise.deploy.spi.DConfigBeanRoot
intf javax.enterprise.deploy.spi.DConfigBean
meth public abstract javax.enterprise.deploy.spi.DConfigBean getDConfigBean(javax.enterprise.deploy.model.DDBeanRoot)

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentConfiguration
meth public abstract javax.enterprise.deploy.model.DeployableObject getDeployableObject()
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot getDConfigBeanRoot(javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract javax.enterprise.deploy.spi.DConfigBeanRoot restoreDConfigBean(java.io.InputStream,javax.enterprise.deploy.model.DDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void removeDConfigBean(javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
meth public abstract void restore(java.io.InputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void save(java.io.OutputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException
meth public abstract void saveDConfigBean(java.io.OutputStream,javax.enterprise.deploy.spi.DConfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException

CLSS public abstract interface javax.enterprise.deploy.spi.DeploymentManager
meth public abstract boolean isDConfigBeanVersionSupported(javax.enterprise.deploy.shared.DConfigBeanVersionType)
meth public abstract boolean isLocaleSupported(java.util.Locale)
meth public abstract boolean isRedeploySupported()
meth public abstract java.util.Locale getCurrentLocale()
meth public abstract java.util.Locale getDefaultLocale()
meth public abstract java.util.Locale[] getSupportedLocales()
meth public abstract javax.enterprise.deploy.shared.DConfigBeanVersionType getDConfigBeanVersion()
meth public abstract javax.enterprise.deploy.spi.DeploymentConfiguration createConfiguration(javax.enterprise.deploy.model.DeployableObject) throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getAvailableModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getNonRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getRunningModules(javax.enterprise.deploy.shared.ModuleType,javax.enterprise.deploy.spi.Target[]) throws javax.enterprise.deploy.spi.exceptions.TargetException
meth public abstract javax.enterprise.deploy.spi.Target[] getTargets()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.File,java.io.File)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],javax.enterprise.deploy.shared.ModuleType,java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.File,java.io.File)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],java.io.InputStream,java.io.InputStream)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject start(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject stop(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject undeploy(javax.enterprise.deploy.spi.TargetModuleID[])
meth public abstract void release()
meth public abstract void setDConfigBeanVersion(javax.enterprise.deploy.shared.DConfigBeanVersionType) throws javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException
meth public abstract void setLocale(java.util.Locale)

CLSS public abstract interface javax.enterprise.deploy.spi.Target
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()

CLSS public abstract interface javax.enterprise.deploy.spi.TargetModuleID
meth public abstract java.lang.String getModuleID()
meth public abstract java.lang.String getWebURL()
meth public abstract java.lang.String toString()
meth public abstract javax.enterprise.deploy.spi.Target getTarget()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID getParentTargetModuleID()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getChildTargetModuleID()

CLSS public javax.enterprise.deploy.spi.exceptions.BeanNotFoundException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.ClientExecuteException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.ConfigurationException
cons public init()
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.InvalidModuleException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public javax.enterprise.deploy.spi.exceptions.TargetException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public abstract interface javax.enterprise.deploy.spi.factories.DeploymentFactory
meth public abstract boolean handlesURI(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getProductVersion()
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException

CLSS public abstract interface javax.enterprise.deploy.spi.status.ClientConfiguration
intf java.io.Serializable
meth public abstract void execute() throws javax.enterprise.deploy.spi.exceptions.ClientExecuteException

CLSS public abstract interface javax.enterprise.deploy.spi.status.DeploymentStatus
meth public abstract boolean isCompleted()
meth public abstract boolean isFailed()
meth public abstract boolean isRunning()
meth public abstract java.lang.String getMessage()
meth public abstract javax.enterprise.deploy.shared.ActionType getAction()
meth public abstract javax.enterprise.deploy.shared.CommandType getCommand()
meth public abstract javax.enterprise.deploy.shared.StateType getState()

CLSS public javax.enterprise.deploy.spi.status.ProgressEvent
cons public init(java.lang.Object,javax.enterprise.deploy.spi.TargetModuleID,javax.enterprise.deploy.spi.status.DeploymentStatus)
meth public javax.enterprise.deploy.spi.TargetModuleID getTargetModuleID()
meth public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus()
supr java.util.EventObject
hfds statuscode,targetModuleID

CLSS public abstract interface javax.enterprise.deploy.spi.status.ProgressListener
intf java.util.EventListener
meth public abstract void handleProgressEvent(javax.enterprise.deploy.spi.status.ProgressEvent)

CLSS public abstract interface javax.enterprise.deploy.spi.status.ProgressObject
meth public abstract boolean isCancelSupported()
meth public abstract boolean isStopSupported()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs()
meth public abstract javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
meth public abstract javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus()
meth public abstract void addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public abstract void cancel() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
meth public abstract void removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public abstract void stop() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException

