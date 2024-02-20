#Signature file v4.1
#Version 1.134.0

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface javax.enterprise.deploy.spi.TargetModuleID
meth public abstract java.lang.String getModuleID()
meth public abstract java.lang.String getWebURL()
meth public abstract java.lang.String toString()
meth public abstract javax.enterprise.deploy.spi.Target getTarget()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID getParentTargetModuleID()
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] getChildTargetModuleID()

CLSS public abstract interface javax.enterprise.deploy.spi.factories.DeploymentFactory
meth public abstract boolean handlesURI(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getProductVersion()
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException

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

CLSS public org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.Datasource
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDriverClassName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String getUsername()

CLSS public final org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
cons public init(java.util.List<org.netbeans.modules.j2ee.deployment.common.api.Datasource>)
cons public init(org.netbeans.modules.j2ee.deployment.common.api.Datasource)
meth public java.util.List<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources()
supr java.lang.Exception
hfds datasources

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor
meth public abstract boolean ejbsChanged()
meth public abstract java.lang.String[] getChangedEjbs()

CLSS public final org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider
cons public init()
fld public final static java.lang.String LIBRARY_TYPE = "j2ee"
fld public final static java.lang.String VOLUME_TYPE_CLASSPATH = "classpath"
fld public final static java.lang.String VOLUME_TYPE_JAVADOC = "javadoc"
fld public final static java.lang.String VOLUME_TYPE_SRC = "src"
intf org.netbeans.spi.project.libraries.LibraryTypeProvider
meth public java.beans.Customizer getCustomizer(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getLibraryType()
meth public java.lang.String[] getSupportedVolumeTypes()
meth public org.netbeans.spi.project.libraries.LibraryImplementation createLibrary()
meth public org.openide.util.Lookup getLookup()
meth public void libraryCreated(org.netbeans.spi.project.libraries.LibraryImplementation)
meth public void libraryDeleted(org.netbeans.spi.project.libraries.LibraryImplementation)
supr java.lang.Object
hfds VOLUME_TYPES

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
innr public final static !enum Type
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type getType()

CLSS public final static !enum org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type
 outer org.netbeans.modules.j2ee.deployment.common.api.MessageDestination
fld public final static org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type QUEUE
fld public final static org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type TOPIC
meth public static org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type>

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getFieldColumn(java.lang.String)
meth public abstract java.lang.String getRelationshipJoinTable(java.lang.String)
meth public abstract java.lang.String getTableName()
meth public abstract java.lang.String[] getRelationshipColumn(java.lang.String)
meth public abstract org.openide.filesystems.FileObject getSchema()

CLSS public abstract org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap
cons public init()
meth public abstract boolean add(java.lang.String,org.openide.filesystems.FileObject)
meth public abstract java.io.File getDistributionPath(org.openide.filesystems.FileObject)
meth public abstract java.io.File getEnterpriseResourceDir()
meth public abstract java.io.File[] getEnterpriseResourceDirs()
meth public abstract java.lang.String getContextName()
meth public abstract org.openide.filesystems.FileObject remove(java.lang.String)
meth public abstract org.openide.filesystems.FileObject[] findSourceFile(java.lang.String)
meth public abstract org.openide.filesystems.FileObject[] getSourceRoots()
meth public final static org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap findSourceMap(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public final static org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap findSourceMap(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.deployment.common.api.ValidationException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public final org.netbeans.modules.j2ee.deployment.common.api.Version
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean isAboveOrEqual(org.netbeans.modules.j2ee.deployment.common.api.Version)
meth public boolean isBelowOrEqual(org.netbeans.modules.j2ee.deployment.common.api.Version)
meth public int hashCode()
meth public java.lang.Integer getMajor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getMicro()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getMinor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Integer getUpdate()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getQualifier()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.deployment.common.api.Version expand(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.common.api.Version fromDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.j2ee.deployment.common.api.Version fromJsr277NotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.j2ee.deployment.common.api.Version fromJsr277OrDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DOTTED_PATTERN,JSR277_PATTERN,majorNumber,microNumber,minorNumber,qualifier,updateNumber,version

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper
cons public init()
meth public static java.io.File getDeploymentPropertiesFile(java.lang.String)
meth public static void writeDeploymentScript(java.io.File,java.lang.Object,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
innr public abstract interface static Logger
innr public final static !enum Mode
innr public final static DeploymentException
meth public boolean canFileDeploy(java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public boolean isRunning(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final void addInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public final void removeInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 java.lang.Deprecated()
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 java.lang.Deprecated()
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
meth public java.lang.String deploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode,java.lang.String,java.lang.String,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger,java.util.concurrent.Callable<java.lang.Void>) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 7 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String getDefaultServerInstanceID()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getServerDisplayName(java.lang.String)
meth public java.lang.String getServerID(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getServerInstanceDisplayName(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getInstancesOfServer(java.lang.String)
meth public java.lang.String[] getServerIDs()
meth public java.lang.String[] getServerInstanceIDs()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[],java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.lang.Object[],java.lang.String,java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>)
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>,org.netbeans.api.j2ee.core.Profile)
meth public java.lang.String[] getServerInstanceIDs(java.util.Collection<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type>,org.netbeans.api.j2ee.core.Profile,java.lang.String[])
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform getJ2eePlatform(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance getServerInstance(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment getDefault()
meth public void disableCompileOnSaveSupport(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void enableCompileOnSaveSupport(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void resumeDeployOnSave(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void suspendDeployOnSave(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider)
meth public void undeploy(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider,boolean,org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger) throws org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
supr java.lang.Object
hfds FILTER_PATTERN,LOGGER,alsoStartTargets,instance

CLSS public final static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$DeploymentException
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
meth public java.lang.String toString()
supr java.lang.Exception

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Logger
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
meth public abstract void log(java.lang.String)

CLSS public final static !enum org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode DEBUG
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode PROFILE
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode RUN
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment$Mode>

CLSS public org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule[] getModules()
meth public void addModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)
meth public void removeModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)
supr org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule
hfds impl,impl2

CLSS public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule
fld public final static java.lang.Object CLIENT
 anno 0 java.lang.Deprecated()
fld public final static java.lang.Object CONN
 anno 0 java.lang.Deprecated()
fld public final static java.lang.Object EAR
 anno 0 java.lang.Deprecated()
fld public final static java.lang.Object EJB
 anno 0 java.lang.Deprecated()
fld public final static java.lang.Object WAR
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String APP_XML = "META-INF/application.xml"
fld public final static java.lang.String CLIENT_XML = "META-INF/application-client.xml"
fld public final static java.lang.String CONNECTOR_XML = "META-INF/ra.xml"
fld public final static java.lang.String EJBJAR_XML = "META-INF/ejb-jar.xml"
fld public final static java.lang.String EJBSERVICES_XML = "META-INF/webservices.xml"
fld public final static java.lang.String J2EE_13 = "1.3"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String J2EE_14 = "1.4"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String JAVA_EE_5 = "1.5"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String MIME_J2EE_MODULE_TARGET = "MIME-org-nb-j2eeserver-J2eeModule-BuildTarget"
fld public final static java.lang.String PROP_MODULE_VERSION = "moduleVersion"
fld public final static java.lang.String PROP_RESOURCE_DIRECTORY = "resourceDir"
fld public final static java.lang.String WEBSERVICES_XML = "WEB-INF/webservices.xml"
fld public final static java.lang.String WEB_XML = "WEB-INF/web.xml"
innr public abstract interface static RootedEntry
innr public final static Type
meth public <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth public java.io.File getDeploymentConfigurationFile(java.lang.String)
meth public java.io.File getResourceDirectory()
meth public java.lang.Object getModuleType()
 anno 0 java.lang.Deprecated()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getModuleVersion()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getUrl()
meth public java.util.Iterator<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$RootedEntry> getArchiveContents() throws java.io.IOException
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type getType()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getArchive() throws java.io.IOException
meth public org.openide.filesystems.FileObject getContentDirectory() throws java.io.IOException
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOGGER,impl,j2eeModuleProvider

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$RootedEntry
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule
meth public abstract java.lang.String getRelativePath()
meth public abstract org.openide.filesystems.FileObject getFileObject()

CLSS public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type CAR
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type EAR
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type EJB
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type RAR
fld public final static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type WAR
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type fromJsrType(java.lang.Object)
supr java.lang.Object
hfds jsrType

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform
fld public final static java.lang.String CLIENT_PROP_DIST_ARCHIVE = "client.dist.archive"
fld public final static java.lang.String LIBRARY_TYPE = "serverlibrary"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_CLASSPATH = "classpath"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_PLATFORM_ROOTS = "platformRoots"
fld public final static java.lang.String TOOL_APP_CLIENT_RUNTIME = "appClientRuntime"
fld public final static java.lang.String TOOL_EMBEDDABLE_EJB = "embeddableejb"
fld public final static java.lang.String TOOL_JSR109 = "jsr109"
fld public final static java.lang.String TOOL_JWSDP = "jwsdp"
fld public final static java.lang.String TOOL_KEYSTORE = "keystore"
fld public final static java.lang.String TOOL_KEYSTORE_CLIENT = "keystoreClient"
fld public final static java.lang.String TOOL_PROP_CLIENT_JAR_LOCATION = "client.jar.location"
fld public final static java.lang.String TOOL_PROP_JVM_OPTS = "jvm.opts"
fld public final static java.lang.String TOOL_PROP_MAIN_CLASS = "main.class"
fld public final static java.lang.String TOOL_PROP_MAIN_CLASS_ARGS = "main.class.args"
fld public final static java.lang.String TOOL_TRUSTSTORE = "truststore"
fld public final static java.lang.String TOOL_TRUSTSTORE_CLIENT = "truststoreClient"
fld public final static java.lang.String TOOL_WSCOMPILE = "wscompile"
fld public final static java.lang.String TOOL_WSGEN = "wsgen"
fld public final static java.lang.String TOOL_WSIMPORT = "wsimport"
fld public final static java.lang.String TOOL_WSIT = "wsit"
intf org.openide.util.Lookup$Provider
meth public boolean equals(java.lang.Object)
meth public boolean isToolSupported(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean supportsProfiling()
meth public int hashCode()
meth public java.awt.Image getIcon()
meth public java.io.File getDomainHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getMiddlewareHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getServerHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File[] getClasspathEntries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.io.File[] getClasspathEntries(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.io.File[] getPlatformRoots()
 anno 0 java.lang.Deprecated()
meth public java.io.File[] getToolClasspathEntries(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getToolProperty(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public java.util.Set getSupportedJavaPlatformVersions()
meth public java.util.Set getSupportedModuleTypes()
 anno 0 java.lang.Deprecated()
meth public java.util.Set getSupportedSpecVersions()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> getSupportedSpecVersions(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type> getSupportedTypes()
meth public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public org.netbeans.api.project.libraries.Library createLibrary(java.io.File,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public org.openide.util.Lookup getLookup()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds DEFAULT_ICON,LOGGER,classpathCache,currentClasspath,impl,librariesChangeListener,serverInstance

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder
fld public final static java.lang.String SERVLET_FINDER_CHANGED = "servlet-finder-changed"
meth public java.io.File getServletTempDirectory()
meth public java.lang.String getServletBasePackageName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getServletEncoding(java.lang.String)
meth public java.lang.String getServletResourcePath(java.lang.String)
meth public java.lang.String getServletSourcePath(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug$JspSourceMapper getSourceMapper(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.JSPServletFinder findJSPServletFinder(org.openide.filesystems.FileObject)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds project

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter
meth public abstract boolean isManifestChanged(long)
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener
meth public abstract void addModule(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public abstract void removeModule(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter
meth public boolean isServerResourceChanged(long)
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance
innr public final Descriptor
innr public final LibraryManager
meth public boolean isDebuggingSupported() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public boolean isDeployOnSaveSupported() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public boolean isDeployOnSaveSupported(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public boolean isProfilingSupported() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public boolean isRunning() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public java.lang.String getDisplayName() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public java.lang.String getServerDisplayName() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public java.lang.String getServerID() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform getJ2eePlatform() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance$Descriptor getDescriptor() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance$LibraryManager getLibraryManager() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
supr java.lang.Object
hfds serverInstanceId

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance$Descriptor
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance)
meth public boolean isLocal() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public int getHttpPort() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
meth public java.lang.String getHostname() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance$LibraryManager
 outer org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance
cons public init(org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance)
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary> getDeployableLibraries() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary> getDeployedLibraries() throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getDeployableDependencies(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>) throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getMissingDependencies(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>) throws org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void deployLibraries(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.deployment.devmodules.api.ServerManager
meth public static java.lang.String showAddServerInstanceWizard()
meth public static java.lang.String showAddServerInstanceWizard(java.util.Map<java.lang.String,java.lang.String>)
meth public static void showCustomizer(java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener
innr public final static Artifact
meth public abstract void artifactsUpdated(java.lang.Iterable<org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact>)

CLSS public final static org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener
meth public boolean equals(java.lang.Object)
meth public boolean isReferencedLibrary()
meth public boolean isRelocatable()
meth public boolean isServerResource()
meth public int hashCode()
meth public java.io.File getDistributionPath()
meth public java.io.File getFile()
meth public java.lang.String getRelocation()
meth public java.lang.String toString()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact distributionPath(java.io.File)
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact referencedLibrary()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact relocatable()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact relocatable(java.lang.String)
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact serverResource()
meth public static org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact forFile(java.io.File)
supr java.lang.Object
hfds distributionPath,file,library,relocatable,relocation,serverResource

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener
meth public abstract void fileCreated(org.openide.filesystems.FileObject)
meth public abstract void fileDeleted(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener
intf java.util.EventListener
meth public abstract void instanceAdded(java.lang.String)
meth public abstract void instanceRemoved(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation
 anno 0 java.lang.Deprecated()
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule[] getModules()
meth public abstract void addModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)
meth public abstract void removeModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule[] getModules()
meth public abstract void addModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)
meth public abstract void removeModuleListener(org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener)

CLSS public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider
cons public init()
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider getChildModuleProvider(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider[] getChildModuleProviders()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getModuleDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
supr org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase
meth public abstract <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> getMetadataModel(java.lang.Class<{%%0}>)
meth public abstract java.io.File getDeploymentConfigurationFile(java.lang.String)
meth public abstract java.io.File getResourceDirectory()
meth public abstract java.lang.String getModuleVersion()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getUrl()
meth public abstract java.util.Iterator<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$RootedEntry> getArchiveContents() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getArchive() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getContentDirectory() throws java.io.IOException
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication createJ2eeApplication(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2)
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule createJ2eeModule(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule createJ2eeModule(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation
 anno 0 java.lang.Deprecated()
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase
meth public abstract java.lang.Object getModuleType()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2
intf org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleBase
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type getModuleType()

CLSS public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
cons public init()
innr public abstract interface static ConfigSupport
innr public abstract interface static DeployOnSaveClassInterceptor
innr public abstract interface static DeployOnSaveSupport
meth protected final void fireServerChange(java.lang.String,java.lang.String)
meth public abstract java.lang.String getServerID()
meth public abstract java.lang.String getServerInstanceID()
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter()
meth public abstract void setServerInstanceID(java.lang.String)
meth public boolean hasVerifierSupport()
meth public boolean isDatasourceCreationSupported()
meth public boolean isOnlyCompileOnSaveEnabled()
meth public final org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public final org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport getConfigSupport()
meth public final org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo getServerDebugInfo()
meth public final org.openide.filesystems.FileObject[] getConfigurationFiles()
meth public final org.openide.filesystems.FileObject[] getConfigurationFiles(boolean)
meth public final void addConfigurationFilesListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener)
meth public final void addInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public final void removeConfigurationFilesListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener)
meth public final void removeInstanceListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener)
meth public java.io.File[] getRequiredLibraries()
meth public java.lang.String getDeploymentName()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getModuleDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getServerDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap getSourceFileMap()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter getResourceChangeReporter()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor()
meth public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport getDeployOnSaveSupport()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties()
meth public org.openide.filesystems.FileObject[] getSourceRoots()
meth public void deployDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
 anno 0 java.lang.Deprecated()
meth public void verify(org.openide.filesystems.FileObject,java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ValidationException
supr java.lang.Object
hfds LOGGER,configFilesListener,configSupportImpl,configSupportImplLock,listeners
hcls WarningInstanceProperties

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
innr public abstract interface static DeployOnSaveListener
meth public abstract boolean createInitialConfiguration()
meth public abstract boolean ensureConfigurationReady()
meth public abstract boolean isDatasourceCreationSupported()
meth public abstract boolean isDescriptorRequired()
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String getContentRelativePath(java.lang.String)
meth public abstract java.lang.String getWebContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String[] getDeploymentConfigurationFileNames()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getServerMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getLibraries() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource findDatasource(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination findMessageDestination(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void addDeployOnSaveListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport$DeployOnSaveListener)
meth public abstract void addLibraryChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void configureLibrary(org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeDeployOnSaveListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport$DeployOnSaveListener)
meth public abstract void removeLibraryChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setCMPMappingInfo(org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping[]) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setCMPResource(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setWebContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport$DeployOnSaveListener
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$ConfigSupport
meth public abstract void deployed(java.lang.Iterable<org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact>)

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveClassInterceptor
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact convert(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener$Artifact)

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider$DeployOnSaveSupport
 outer org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider
meth public abstract boolean containsIdeArtifacts()
meth public abstract void addArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)
meth public abstract void removeArtifactListener(org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener)

CLSS public org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterFactory
cons public init()
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter createResourceChangeReporter(org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation
meth public abstract boolean isServerResourceChanged(long)

CLSS public org.netbeans.modules.j2ee.deployment.plugins.api.AlreadyRegisteredException
cons public init(java.lang.String)
supr org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor
intf org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor
intf org.netbeans.modules.j2ee.deployment.plugins.api.ModuleChangeDescriptor

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge
meth public static org.netbeans.api.server.ServerInstance getCommonInstance(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor
intf org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor
meth public boolean classesChanged()
meth public boolean descriptorChanged()
meth public boolean ejbsChanged()
meth public boolean manifestChanged()
meth public boolean serverDescriptorChanged()
meth public boolean serverResourcesChanged()
meth public java.io.File[] getChangedFiles()
meth public java.io.File[] getRemovedFiles()
meth public java.lang.String toString()
meth public java.lang.String[] getChangedEjbs()
supr java.lang.Object
hfds desc,serverResourcesChanged

CLSS public org.netbeans.modules.j2ee.deployment.plugins.api.FileJ2eeModuleQuery
meth public static org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
cons public init(java.lang.String)
supr java.io.IOException

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties
cons public init()
fld public final static java.lang.String DEPLOYMENT_TIMEOUT = "deploymentTimeout"
fld public final static java.lang.String DISPLAY_NAME_ATTR = "displayName"
fld public final static java.lang.String HTTP_PORT_NUMBER = "httpportnumber"
fld public final static java.lang.String PASSWORD_ATTR = "password"
fld public final static java.lang.String REGISTERED_WITHOUT_UI = "registeredWithoutUI"
fld public final static java.lang.String REMOVE_FORBIDDEN = "removeForbidden"
fld public final static java.lang.String SHUTDOWN_TIMEOUT = "shutdownTimeout"
fld public final static java.lang.String STARTUP_TIMEOUT = "startupTimeout"
fld public final static java.lang.String URL_ATTR = "url"
fld public final static java.lang.String USERNAME_ATTR = "username"
meth protected void firePropertyChange(java.beans.PropertyChangeEvent)
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.util.Enumeration propertyNames()
meth public abstract javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager()
 anno 0 java.lang.Deprecated()
meth public abstract void refreshServerInstance()
meth public abstract void setProperties(java.util.Properties)
meth public abstract void setProperty(java.lang.String,java.lang.String)
meth public static java.lang.String[] getInstanceList()
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstanceProperties(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstanceProperties(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstanceProperties(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstancePropertiesNonPersistent(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties createInstancePropertiesWithoutUI(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties getInstanceProperties(java.lang.String)
meth public static void removeInstance(java.lang.String)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds propertyChangeSupport

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.api.ModuleChangeDescriptor
meth public abstract boolean classesChanged()
meth public abstract boolean descriptorChanged()
meth public abstract boolean manifestChanged()
meth public abstract boolean serverDescriptorChanged()
meth public abstract java.io.File[] getChangedFiles()
meth public abstract java.io.File[] getRemovedFiles()

CLSS public org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.lang.String)
fld public final static java.lang.String TRANSPORT_SHMEM = "dt_shmem"
fld public final static java.lang.String TRANSPORT_SOCKET = "dt_socket"
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getShmemName()
meth public java.lang.String getTransport()
meth public void setHost(java.lang.String)
meth public void setPort(int)
meth public void setShmemName(java.lang.String)
meth public void setTransport(java.lang.String)
supr java.lang.Object
hfds host,port,shmemName,transport

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary
meth public java.lang.String getImplementationTitle()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getSpecificationTitle()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.common.api.Version getImplementationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.common.api.Version getSpecificationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency
meth public boolean equals(java.lang.Object)
meth public boolean isExactMatch()
meth public boolean specificationEquals(java.lang.Object)
meth public boolean versionMatches(org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public int hashCode()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.j2ee.deployment.common.api.Version getImplementationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.common.api.Version getSpecificationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency exactVersion(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.Version,org.netbeans.modules.j2ee.deployment.common.api.Version)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency minimalVersion(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.Version,org.netbeans.modules.j2ee.deployment.common.api.Version)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds exactMatch,implementationVersion,name,specificationVersion

CLSS public org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress
cons public init(java.lang.Object)
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress$Command START_SERVER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress$Command STOP_SERVER
innr public static Command
intf javax.enterprise.deploy.spi.status.ProgressObject
meth protected javax.enterprise.deploy.spi.status.DeploymentStatus createDeploymentStatus(javax.enterprise.deploy.shared.CommandType,java.lang.String,javax.enterprise.deploy.shared.StateType)
meth protected javax.enterprise.deploy.spi.status.ProgressEvent createCompletedProgressEvent(javax.enterprise.deploy.shared.CommandType,java.lang.String)
meth protected javax.enterprise.deploy.spi.status.ProgressEvent createFailedProgressEvent(javax.enterprise.deploy.shared.CommandType,java.lang.String)
meth protected javax.enterprise.deploy.spi.status.ProgressEvent createRunningProgressEvent(javax.enterprise.deploy.shared.CommandType,java.lang.String)
meth protected void notify(javax.enterprise.deploy.spi.status.ProgressEvent)
meth public boolean isCancelSupported()
meth public boolean isStopSupported()
meth public javax.enterprise.deploy.spi.TargetModuleID[] getResultTargetModuleIDs()
meth public javax.enterprise.deploy.spi.status.ClientConfiguration getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
meth public javax.enterprise.deploy.spi.status.DeploymentStatus getDeploymentStatus()
meth public void addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public void cancel() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
meth public void removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
meth public void setStatusStartCompleted(java.lang.String)
meth public void setStatusStartFailed(java.lang.String)
meth public void setStatusStartRunning(java.lang.String)
meth public void setStatusStopCompleted(java.lang.String)
meth public void setStatusStopFailed(java.lang.String)
meth public void setStatusStopRunning(java.lang.String)
meth public void stop() throws javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException
supr java.lang.Object
hfds listeners,server,status

CLSS public static org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress$Command
 outer org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress
cons public init(int,java.lang.String)
meth public java.lang.String toString()
supr javax.enterprise.deploy.shared.CommandType
hfds commandString

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.api.UISupport
innr public final static !enum ServerIcon
meth public static java.awt.Image getIcon(org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon)
meth public static org.openide.windows.InputOutput getServerIO(java.lang.String)
supr java.lang.Object
hfds ioWeakMap

CLSS public final static !enum org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon
 outer org.netbeans.modules.j2ee.deployment.plugins.api.UISupport
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EAR_ARCHIVE
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EAR_FOLDER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EAR_OPENED_FOLDER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EJB_ARCHIVE
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EJB_FOLDER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon EJB_OPENED_FOLDER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon WAR_ARCHIVE
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon WAR_FOLDER
fld public final static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon WAR_OPENED_FOLDER
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.deployment.plugins.api.UISupport$ServerIcon>

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider
meth public abstract java.io.File getDeploymentPropertiesFile()
meth public abstract void writeDeploymentScript(java.io.OutputStream,java.lang.Object) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void deployDatasources(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext
meth public java.io.File getDeploymentPlan()
meth public java.io.File getModuleFile()
meth public java.io.File[] getRequiredLibraries()
meth public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getModule()
meth public org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor getChanges()
supr java.lang.Object
hfds changes,deploymentPlan,module,moduleFile,requiredLibraries

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2
intf javax.enterprise.deploy.spi.DeploymentManager
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject distribute(javax.enterprise.deploy.spi.Target[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject redeploy(javax.enterprise.deploy.spi.TargetModuleID[],org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet
meth public abstract java.io.File getServletTempDirectory(java.lang.String)
meth public abstract java.lang.String getServletEncoding(java.lang.String,java.lang.String)
meth public abstract java.lang.String getServletResourcePath(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet2
intf org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet
meth public abstract java.lang.String getServletBasePackageName(java.lang.String)
meth public abstract java.lang.String getServletSourcePath(java.lang.String,java.lang.String)

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment
cons public init()
meth public abstract boolean canFileDeploy(javax.enterprise.deploy.spi.Target,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule)
meth public abstract java.io.File getDirectoryForModule(javax.enterprise.deploy.spi.TargetModuleID)
meth public abstract java.io.File getDirectoryForNewApplication(javax.enterprise.deploy.spi.Target,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getDirectoryForNewModule(java.io.File,java.lang.String,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject incrementalDeploy(javax.enterprise.deploy.spi.TargetModuleID,org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject initialDeploy(javax.enterprise.deploy.spi.Target,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration,java.io.File)
meth public boolean isDeployOnSaveSupported()
meth public java.io.File getDirectoryForNewApplication(java.lang.String,javax.enterprise.deploy.spi.Target,org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getModuleUrl(javax.enterprise.deploy.spi.TargetModuleID)
meth public javax.enterprise.deploy.spi.status.ProgressObject deployOnSave(javax.enterprise.deploy.spi.TargetModuleID,org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor)
meth public static org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment getIncrementalDeploymentForModule(org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment,org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws java.io.IOException
meth public void notifyDeployment(javax.enterprise.deploy.spi.TargetModuleID)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment2
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject incrementalDeploy(javax.enterprise.deploy.spi.TargetModuleID,org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject initialDeploy(javax.enterprise.deploy.spi.Target,org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext)

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory
cons public init()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl getJ2eePlatformImpl(javax.enterprise.deploy.spi.DeploymentManager)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl
cons public init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_LIBRARIES = "libraries"
fld public final static java.lang.String PROP_SERVER_LIBRARIES = "serverLibraries"
meth public abstract boolean isToolSupported(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.awt.Image getIcon()
meth public abstract java.io.File[] getPlatformRoots()
meth public abstract java.io.File[] getToolClasspathEntries(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.util.Set getSupportedJavaPlatformVersions()
meth public abstract org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
meth public abstract org.netbeans.spi.project.libraries.LibraryImplementation[] getLibraries()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getToolProperty(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Set getSupportedModuleTypes()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> getSupportedSpecVersions()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> getSupportedSpecVersions(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles()
meth public java.util.Set<org.netbeans.api.j2ee.core.Profile> getSupportedProfiles(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type)
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule$Type> getSupportedTypes()
meth public org.netbeans.spi.project.libraries.LibraryImplementation[] getLibraries(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
meth public org.openide.util.Lookup getLookup()
supr java.lang.Object
hfds supp

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2
cons public init()
meth public abstract java.io.File getDomainHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getMiddlewareHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.io.File getServerHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer
meth public abstract boolean supportsDeployJDBCDrivers(javax.enterprise.deploy.spi.Target)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject deployJDBCDrivers(javax.enterprise.deploy.spi.Target,java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource>)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.LookupProvider
meth public abstract org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void deployMessageDestinations(java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug
innr public abstract interface static JspSourceMapper
intf org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug$JspSourceMapper getSourceMapper(javax.enterprise.deploy.spi.TargetModuleID,java.lang.String)

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug$JspSourceMapper
 outer org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug
innr public abstract interface static NameConverter
meth public abstract boolean hasIncludeFiles()
meth public abstract boolean isEmpty()
meth public abstract boolean isJavaCodeInJspPage(int,int)
meth public abstract boolean isProperJspFileName(java.lang.String)
meth public abstract int mangle(int)
meth public abstract int mangle(int,int)
meth public abstract int mangle(java.lang.String,int)
meth public abstract int mangle(java.lang.String,int,int)
meth public abstract int size()
meth public abstract int unmangle(int)
meth public abstract int unmangle(int,int)
meth public abstract int unmangle(java.lang.String,int,int)
meth public abstract java.lang.String getJavaLineType(int,int)
meth public abstract java.lang.String getJspFileName(int,int) throws java.io.IOException
meth public abstract java.lang.String getPrimaryJspFileName()
meth public abstract java.util.Map getFileNames()
meth public abstract void setPrimaryJspFileName(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug$JspSourceMapper$NameConverter
 outer org.netbeans.modules.j2ee.deployment.plugins.spi.OldJSPDebug$JspSourceMapper
meth public abstract java.lang.String convert(java.lang.String) throws java.io.IOException

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory
cons public init()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager)
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager)
meth public boolean isCommonUIRequired()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider getAntDeploymentProvider(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager getDatasourceManager(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer getJDBCDriverDeployer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment getMessageDestinationDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor getServerInstanceDescriptor(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager getServerLibraryManager(javax.enterprise.deploy.spi.DeploymentManager)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver getTargetModuleIDResolver(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.openide.WizardDescriptor$InstantiatingIterator getAddInstanceIterator()
meth public void finishServerInitialization() throws org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory
 anno 0 java.lang.Deprecated()
meth public abstract org.openide.nodes.Node getManagerNode(org.openide.util.Lookup)
meth public abstract org.openide.nodes.Node getTargetNode(org.openide.util.Lookup)

CLSS public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor
meth public abstract boolean isLocal()
meth public abstract int getHttpPort()
meth public abstract java.lang.String getHostname()

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory
meth public static org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary createServerLibrary(org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryImplementation
meth public abstract java.lang.String getImplementationTitle()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getSpecificationTitle()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Version getImplementationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Version getSpecificationVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager
innr public static MissingLibrariesException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary> getDeployableLibraries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary> getDeployedLibraries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getDeployableDependencies(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getMissingDependencies(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void deployLibraries(java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public static org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager$MissingLibrariesException
 outer org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager
cons public init(java.lang.String,java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency>)
meth public java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getMissingLibraries()
supr org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
hfds missingLibraries

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer
cons public init()
meth public abstract boolean isAlsoTargetServer(javax.enterprise.deploy.spi.Target)
meth public abstract boolean isDebuggable(javax.enterprise.deploy.spi.Target)
meth public abstract boolean isRunning()
meth public abstract boolean needsStartForAdminConfig()
meth public abstract boolean needsStartForConfigure()
meth public abstract boolean needsStartForTargetList()
meth public abstract boolean supportsStartDeploymentManager()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject startDebugging(javax.enterprise.deploy.spi.Target)
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject startDeploymentManager()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject stopDeploymentManager()
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo getDebugInfo(javax.enterprise.deploy.spi.Target)
meth public boolean canStopDeploymentManagerSilently()
meth public boolean isRunning(javax.enterprise.deploy.spi.Target)
meth public boolean needsRestart(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartDebugging(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartProfiling(javax.enterprise.deploy.spi.Target)
meth public boolean supportsStartTarget(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject startProfiling(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject startTarget(javax.enterprise.deploy.spi.Target)
meth public javax.enterprise.deploy.spi.status.ProgressObject stopTarget(javax.enterprise.deploy.spi.Target)
meth public void stopDeploymentManagerSilently()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver
cons public init()
fld public final static java.lang.String KEY_CONTENT_DIR = "contentDirs"
fld public final static java.lang.String KEY_CONTEXT_ROOT = "contextRoot"
fld public final static javax.enterprise.deploy.spi.TargetModuleID[] EMPTY_TMID_ARRAY
meth public abstract javax.enterprise.deploy.spi.TargetModuleID[] lookupTargetModuleID(java.util.Map,javax.enterprise.deploy.spi.Target[])
meth public final java.lang.String[] getLookupKeys()
supr java.lang.Object
hfds lookupKeys

CLSS public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport
cons public init()
meth public abstract void verify(org.openide.filesystems.FileObject,java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ValidationException
meth public boolean supportsModuleType(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.WebTargetModuleID
intf javax.enterprise.deploy.spi.TargetModuleID
meth public abstract java.net.URL resolveWebURL()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration
meth public abstract java.lang.String getContextRoot() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setContextRoot(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration
meth public abstract boolean supportsCreateDatasource()
meth public abstract java.lang.String findDatasourceJndiName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.lang.String findDatasourceJndiNameForEjb(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.Datasource> getDatasources() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.Datasource createDatasource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException,org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException
meth public abstract void bindDatasourceReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindDatasourceReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentDescriptorConfiguration
meth public abstract boolean isDescriptorRequired()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration
meth public abstract void save(java.io.OutputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration
meth public abstract java.lang.String findJndiNameForEjb(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReference(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindEjbReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.MappingConfiguration
meth public abstract void setCMPResource(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void setMappingInfo(org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping[]) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration
meth public abstract boolean supportsCreateMessageDestination()
meth public abstract java.lang.String findMessageDestinationName(java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.common.api.MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract org.netbeans.modules.j2ee.deployment.common.api.MessageDestination createMessageDestination(java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMdbToMessageDestination(java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReference(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
meth public abstract void bindMessageDestinationReferenceForEjb(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.deployment.common.api.MessageDestination$Type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration
intf org.openide.util.Lookup$Provider
meth public abstract org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getJ2eeModule()
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void dispose()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2
intf org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory
meth public abstract org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration create(org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule,java.lang.String) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.plugins.spi.config.ServerLibraryConfiguration
meth public abstract java.util.Set<org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency> getLibraries() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addLibraryChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void configureLibrary(org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeLibraryChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport
meth public static org.openide.util.Lookup createCompositeLookup(org.openide.util.Lookup,java.lang.String)
supr java.lang.Object
hcls DelegatingLookupImpl

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.spi.support.ProxyDeploymentFactory
intf javax.enterprise.deploy.spi.factories.DeploymentFactory
meth public boolean handlesURI(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getProductVersion()
meth public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(java.lang.String,java.lang.String,java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(java.lang.String) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException
meth public static org.netbeans.modules.j2ee.deployment.plugins.spi.support.ProxyDeploymentFactory create(java.util.Map)
supr java.lang.Object
hfds attributes,delegate,urlPattern

CLSS public final org.netbeans.modules.j2ee.deployment.plugins.spi.support.ProxyOptionalFactory
meth public boolean isCommonUIRequired()
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider getAntDeploymentProvider(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager getDatasourceManager(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet getFindJSPServlet(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment getIncrementalDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer getJDBCDriverDeployer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment getMessageDestinationDeployment(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInstanceDescriptor getServerInstanceDescriptor(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryManager getServerLibraryManager(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer getStartServer(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver getTargetModuleIDResolver(javax.enterprise.deploy.spi.DeploymentManager)
meth public org.openide.WizardDescriptor$InstantiatingIterator getAddInstanceIterator()
meth public static org.netbeans.modules.j2ee.deployment.plugins.spi.support.ProxyOptionalFactory create(java.util.Map)
meth public void finishServerInitialization() throws org.netbeans.modules.j2ee.deployment.plugins.spi.ServerInitializationException
supr org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory
hfds attributes,delegate,noInitializationFinish

CLSS public final org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.api.java.platform.JavaPlatform,java.lang.String[],java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.lang.String[] getEnv()
meth public java.lang.String[] getJvmArgs()
meth public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform()
supr java.lang.Object
hfds env,javaPlatform,jvmArgs

CLSS public final org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport
cons public init()
fld public final static int STATE_BLOCKING = 2
fld public final static int STATE_INACTIVE = 0
fld public final static int STATE_PROFILING = 4
fld public final static int STATE_RUNNING = 3
fld public final static int STATE_STARTING = 1
meth public static int getState()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler
meth public abstract boolean attachProfiler(java.util.Map)
meth public abstract int getState()
meth public abstract javax.enterprise.deploy.spi.status.ProgressObject shutdown()
meth public abstract org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings getSettings(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings getSettings(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
meth public abstract void notifyStarting()

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryTypeProvider
intf org.openide.util.Lookup$Provider
meth public abstract java.beans.Customizer getCustomizer(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getLibraryType()
meth public abstract java.lang.String[] getSupportedVolumeTypes()
meth public abstract org.netbeans.spi.project.libraries.LibraryImplementation createLibrary()
meth public abstract void libraryCreated(org.netbeans.spi.project.libraries.LibraryImplementation)
meth public abstract void libraryDeleted(org.netbeans.spi.project.libraries.LibraryImplementation)

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

