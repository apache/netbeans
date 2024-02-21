#Signature file v4.1
#Version 1.37

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

CLSS public abstract interface org.netbeans.modules.weblogic.common.api.BatchDeployListener
intf org.netbeans.modules.weblogic.common.api.DeployListener
meth public abstract void onStepFinish(java.lang.String)
meth public abstract void onStepStart(java.lang.String)

CLSS public abstract interface org.netbeans.modules.weblogic.common.api.DeployListener
intf java.util.EventListener
meth public abstract void onException(java.lang.Exception)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void onFail(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void onFinish()
meth public abstract void onInterrupted()
meth public abstract void onStart()
meth public abstract void onTimeout()

CLSS public final org.netbeans.modules.weblogic.common.api.DeploymentTarget
innr public final static !enum Type
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type getType()
supr java.lang.Object
hfds name,type

CLSS public final static !enum org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type
 outer org.netbeans.modules.weblogic.common.api.DeploymentTarget
fld public final static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type CLUSTER
fld public final static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type JMS_SERVER
fld public final static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type SERVER
meth public static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type parse(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type[] values()
supr java.lang.Enum<org.netbeans.modules.weblogic.common.api.DeploymentTarget$Type>

CLSS public final org.netbeans.modules.weblogic.common.api.DomainConfiguration
meth public boolean isProduction()
meth public boolean isSecured()
meth public int getPort()
meth public java.io.File getLogFile()
meth public java.lang.String getAdminServer()
meth public java.lang.String getHost()
meth public java.lang.String getName()
meth public org.netbeans.modules.weblogic.common.api.Version getVersion()
supr java.lang.Object
hfds ADMIN_SERVER_PATTERN,DEFAULT_HOST,DEFAULT_PORT,DEFAULT_SECURED_PORT,DOMAIN_NAME_PATTERN,DOMAIN_VERSION_PATTERN,ENABLED_PATTERN,FILE_NAME_PATTERN,LISTEN_ADDRESS_PATTERN,LISTEN_PORT_PATTERN,LOGGER,LOG_PATTERN,NAME_PATTERN,PRODUCTION_MODE_PATTERN,SERVER_PATTERN,SSL_PATTERN,adminServer,domain,domainConfig,domainListener,host,logFile,name,port,production,secured,version
hcls DomainChangeListener,Server

CLSS public abstract interface org.netbeans.modules.weblogic.common.api.RuntimeListener
intf java.util.EventListener
meth public abstract void onException(java.lang.Exception)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void onExit()
meth public abstract void onFail()
meth public abstract void onFinish()
meth public abstract void onInterrupted()
meth public abstract void onProcessFinish()
meth public abstract void onProcessStart()
meth public abstract void onRunning()
meth public abstract void onStart()
meth public abstract void onTimeout()

CLSS public final org.netbeans.modules.weblogic.common.api.Version
meth public boolean equals(java.lang.Object)
meth public boolean isAboveOrEqual(org.netbeans.modules.weblogic.common.api.Version)
meth public boolean isBelowOrEqual(org.netbeans.modules.weblogic.common.api.Version)
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
meth public org.netbeans.modules.weblogic.common.api.Version expand(java.lang.String)
meth public static org.netbeans.modules.weblogic.common.api.Version fromDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.weblogic.common.api.Version fromJsr277NotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.weblogic.common.api.Version fromJsr277OrDottedNotationWithFallback(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DOTTED_PATTERN,JSR277_PATTERN,majorNumber,microNumber,minorNumber,qualifier,updateNumber,version

CLSS public final org.netbeans.modules.weblogic.common.api.WebLogicConfiguration
innr public abstract interface static Credentials
meth public boolean equals(java.lang.Object)
meth public boolean isRemote()
meth public boolean isSecured()
meth public int getPort()
meth public int hashCode()
meth public java.io.File getDomainHome()
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public java.io.File getLogFile()
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public java.io.File getServerHome()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getAdminURL()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDomainAdminServer()
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public java.lang.String getDomainName()
 anno 0 org.netbeans.api.annotations.common.NullUnknown()
meth public java.lang.String getHost()
meth public java.lang.String getId()
meth public java.lang.String getPassword()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getUsername()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.weblogic.common.api.Version getDomainVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.weblogic.common.api.WebLogicLayout getLayout()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.weblogic.common.api.WebLogicRemote getRemote()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.weblogic.common.api.WebLogicConfiguration forLocalDomain(java.io.File,java.io.File,org.netbeans.modules.weblogic.common.api.WebLogicConfiguration$Credentials)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.weblogic.common.api.WebLogicConfiguration forRemoteDomain(java.io.File,java.lang.String,int,boolean,org.netbeans.modules.weblogic.common.api.WebLogicConfiguration$Credentials)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INSTANCES,config,credentials,domainHome,host,id,layout,port,remote,secured,serverHome

CLSS public abstract interface static org.netbeans.modules.weblogic.common.api.WebLogicConfiguration$Credentials
 outer org.netbeans.modules.weblogic.common.api.WebLogicConfiguration
meth public abstract java.lang.String getPassword()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getUsername()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.weblogic.common.api.WebLogicDeployer
innr public final static Application
innr public final static Artifact
meth public java.util.concurrent.Future<java.lang.String> deploy(java.io.File,java.util.Collection<org.netbeans.modules.weblogic.common.api.DeploymentTarget>,org.netbeans.modules.weblogic.common.api.DeployListener,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.String> deploy(java.io.File,org.netbeans.modules.weblogic.common.api.DeployListener,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> deploy(java.util.List<org.netbeans.modules.weblogic.common.api.WebLogicDeployer$Artifact>,java.util.Collection<org.netbeans.modules.weblogic.common.api.DeploymentTarget>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> deploy(java.util.List<org.netbeans.modules.weblogic.common.api.WebLogicDeployer$Artifact>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> redeploy(java.lang.String,java.io.File,java.util.Collection<org.netbeans.modules.weblogic.common.api.DeploymentTarget>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> redeploy(java.lang.String,java.io.File,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> redeploy(java.util.List<java.lang.String>,java.util.Collection<org.netbeans.modules.weblogic.common.api.DeploymentTarget>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> redeploy(java.util.List<java.lang.String>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> start(java.util.Collection<java.lang.String>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> stop(java.util.Collection<java.lang.String>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.lang.Void> undeploy(java.util.Collection<java.lang.String>,org.netbeans.modules.weblogic.common.api.BatchDeployListener)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public java.util.concurrent.Future<java.util.Collection<org.netbeans.modules.weblogic.common.api.DeploymentTarget>> getTargets()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.concurrent.Future<java.util.Collection<org.netbeans.modules.weblogic.common.api.WebLogicDeployer$Application>> list(java.net.InetAddress)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.weblogic.common.api.WebLogicDeployer getInstance(org.netbeans.modules.weblogic.common.api.WebLogicConfiguration,java.io.File,java.util.concurrent.Callable<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds DEPLOYMENT_RP,LOGGER,TIMEOUT,VERSION_10,config,javaBinary,nonProxy
hcls LastLineProcessor,LoggingLineProcessor

CLSS public final static org.netbeans.modules.weblogic.common.api.WebLogicDeployer$Application
 outer org.netbeans.modules.weblogic.common.api.WebLogicDeployer
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String getWebContext()
meth public java.net.URL getUrl()
meth public java.util.List<java.net.URL> getServerUrls()
supr java.lang.Object
hfds name,serverUrls,type,url,webContext

CLSS public final static org.netbeans.modules.weblogic.common.api.WebLogicDeployer$Artifact
 outer org.netbeans.modules.weblogic.common.api.WebLogicDeployer
cons public init(java.io.File,java.lang.String,boolean)
meth public boolean isLibrary()
meth public java.io.File getFile()
meth public java.lang.String getName()
supr java.lang.Object
hfds file,library,name

CLSS public final org.netbeans.modules.weblogic.common.api.WebLogicLayout
meth public java.io.File getDomainConfigFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getDomainLibDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getMiddlewareHome()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getServerLibDirectory()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.io.File getWeblogicJar()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.io.File[] getClassPath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.ClassLoader getClassLoader()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.weblogic.common.api.Version getServerVersion()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isSupportedLayout(java.io.File)
meth public static boolean isSupportedVersion(org.netbeans.modules.weblogic.common.api.Version)
meth public static java.io.File getDomainConfigFile(java.io.File)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.io.File getMiddlewareHome(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.io.File getWeblogicJar(java.io.File)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.weblogic.common.api.DomainConfiguration getDomainConfiguration(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.weblogic.common.api.Version getServerVersion(java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds CLASSLOADERS,EXPECTED_FILES,LOGGER,WEBLOGIC_JAR,config
hcls ServerDescriptor,WebLogicClassLoader

CLSS public final org.netbeans.modules.weblogic.common.api.WebLogicRemote
innr public abstract interface static JmxAction
meth public <%0 extends java.lang.Object> {%%0} executeAction(java.util.concurrent.Callable<{%%0}>,java.util.concurrent.Callable<java.lang.String>) throws java.lang.Exception
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public <%0 extends java.lang.Object> {%%0} executeAction(org.netbeans.modules.weblogic.common.api.WebLogicRemote$JmxAction<{%%0}>,java.util.concurrent.Callable<java.lang.String>) throws java.lang.Exception
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds config

CLSS public abstract interface static org.netbeans.modules.weblogic.common.api.WebLogicRemote$JmxAction<%0 extends java.lang.Object>
 outer org.netbeans.modules.weblogic.common.api.WebLogicRemote
meth public abstract {org.netbeans.modules.weblogic.common.api.WebLogicRemote$JmxAction%0} execute(javax.management.MBeanServerConnection) throws java.lang.Exception

CLSS public final org.netbeans.modules.weblogic.common.api.WebLogicRuntime
innr public abstract interface static RunningCondition
meth public boolean isProcessRunning()
meth public boolean isRunning()
meth public org.netbeans.api.extexecution.base.input.InputReaderTask createLogReaderTask(org.netbeans.api.extexecution.base.input.LineProcessor,java.util.concurrent.Callable<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.weblogic.common.api.WebLogicRuntime getInstance(org.netbeans.modules.weblogic.common.api.WebLogicConfiguration)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void kill()
meth public void start(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.modules.weblogic.common.api.RuntimeListener,java.util.Map<java.lang.String,java.lang.String>,org.netbeans.modules.weblogic.common.api.WebLogicRuntime$RunningCondition)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public void startAndWait(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,java.util.Map<java.lang.String,java.lang.String>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public void stop(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.modules.weblogic.common.api.RuntimeListener)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public void stopAndWait(org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory,org.netbeans.api.extexecution.base.BaseExecutionDescriptor$InputProcessorFactory) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds CHECK_TIMEOUT,DELAY,LOGGER,LOG_PARSING_PATTERN,PROCESSES,RUNTIME_RP,SHUTDOWN_BAT,SHUTDOWN_SH,STARTUP_BAT,STARTUP_SH,START_KEY_UUID,STOP_KEY_UUID,TIMEOUT,config
hcls BlockingListener,LogFileProvider

CLSS public abstract interface static org.netbeans.modules.weblogic.common.api.WebLogicRuntime$RunningCondition
 outer org.netbeans.modules.weblogic.common.api.WebLogicRuntime
meth public abstract boolean isRunning()

CLSS public abstract interface org.netbeans.modules.weblogic.common.spi.WebLogicTrustHandler
meth public abstract java.util.Map<java.lang.String,java.lang.String> getTrustProperties(org.netbeans.modules.weblogic.common.api.WebLogicConfiguration)
meth public abstract javax.net.ssl.TrustManager getTrustManager(org.netbeans.modules.weblogic.common.api.WebLogicConfiguration) throws java.security.GeneralSecurityException

