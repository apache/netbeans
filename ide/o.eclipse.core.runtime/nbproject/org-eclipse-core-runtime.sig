#Signature file v4.1
#Version ${mf.OpenIDE-Module-Specification-Version}

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

CLSS public abstract java.net.URLConnection
cons protected init(java.net.URL)
fld protected boolean allowUserInteraction
fld protected boolean connected
fld protected boolean doInput
fld protected boolean doOutput
fld protected boolean useCaches
fld protected java.net.URL url
fld protected long ifModifiedSince
meth public abstract void connect() throws java.io.IOException
meth public boolean getAllowUserInteraction()
meth public boolean getDefaultUseCaches()
meth public boolean getDoInput()
meth public boolean getDoOutput()
meth public boolean getUseCaches()
meth public int getConnectTimeout()
meth public int getContentLength()
meth public int getHeaderFieldInt(java.lang.String,int)
meth public int getReadTimeout()
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public java.lang.Object getContent() throws java.io.IOException
meth public java.lang.Object getContent(java.lang.Class[]) throws java.io.IOException
meth public java.lang.String getContentEncoding()
meth public java.lang.String getContentType()
meth public java.lang.String getHeaderField(int)
meth public java.lang.String getHeaderField(java.lang.String)
meth public java.lang.String getHeaderFieldKey(int)
meth public java.lang.String getRequestProperty(java.lang.String)
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public java.security.Permission getPermission() throws java.io.IOException
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getHeaderFields()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getRequestProperties()
meth public long getContentLengthLong()
meth public long getDate()
meth public long getExpiration()
meth public long getHeaderFieldDate(java.lang.String,long)
meth public long getHeaderFieldLong(java.lang.String,long)
meth public long getIfModifiedSince()
meth public long getLastModified()
meth public static boolean getDefaultAllowUserInteraction()
meth public static java.lang.String getDefaultRequestProperty(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String guessContentTypeFromName(java.lang.String)
meth public static java.lang.String guessContentTypeFromStream(java.io.InputStream) throws java.io.IOException
meth public static java.net.FileNameMap getFileNameMap()
meth public static void setContentHandlerFactory(java.net.ContentHandlerFactory)
meth public static void setDefaultAllowUserInteraction(boolean)
meth public static void setDefaultRequestProperty(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void setFileNameMap(java.net.FileNameMap)
meth public void addRequestProperty(java.lang.String,java.lang.String)
meth public void setAllowUserInteraction(boolean)
meth public void setConnectTimeout(int)
meth public void setDefaultUseCaches(boolean)
meth public void setDoInput(boolean)
meth public void setDoOutput(boolean)
meth public void setIfModifiedSince(long)
meth public void setReadTimeout(int)
meth public void setRequestProperty(java.lang.String,java.lang.String)
meth public void setUseCaches(boolean)
supr java.lang.Object
hfds connectTimeout,contentClassPrefix,contentPathProp,defaultAllowUserInteraction,defaultUseCaches,factory,fileNameMap,fileNameMapLoaded,handlers,readTimeout,requests

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract org.eclipse.core.internal.boot.PlatformURLConnection
cons protected init(java.net.URL)
fld public final static boolean DEBUG
fld public final static boolean DEBUG_CACHE_COPY
fld public final static boolean DEBUG_CACHE_LOOKUP
fld public final static boolean DEBUG_CONNECT
meth protected boolean allowCaching()
meth protected java.net.URL resolve() throws java.io.IOException
meth protected static java.lang.String getId(java.lang.String)
meth protected static java.lang.String getVersion(java.lang.String)
meth protected void debug(java.lang.String)
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.net.URL getResolvedURL()
meth public java.net.URL getURLAsLocal() throws java.io.IOException
meth public java.net.URL[] getAuxillaryURLs() throws java.io.IOException
meth public void connect() throws java.io.IOException
supr java.net.URLConnection
hfds BUF_SIZE,CACHE_DIR,CACHE_INDEX,CACHE_INDEX_PROP,CACHE_LOCATION_PROP,CACHE_PREFIX_PROP,CACHE_PROP,NOT_FOUND,OPTION_DEBUG,OPTION_DEBUG_CACHE_COPY,OPTION_DEBUG_CACHE_LOOKUP,OPTION_DEBUG_CONNECT,cacheIndex,cacheLocation,cachedURL,connection,filePrefix,indexName,isInCache,isJar,resolvedURL

CLSS public abstract org.eclipse.core.internal.jobs.InternalJob
cons protected init(java.lang.String)
fld protected final static org.eclipse.core.internal.jobs.JobManager manager
intf java.lang.Comparable
meth protected abstract org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor)
meth protected boolean belongsTo(java.lang.Object)
meth protected boolean cancel()
meth protected boolean isBlocking()
meth protected boolean isSystem()
meth protected boolean isUser()
meth protected boolean shouldSchedule()
meth protected boolean sleep()
meth protected int getPriority()
meth protected int getState()
meth protected java.lang.Object getProperty(org.eclipse.core.runtime.QualifiedName)
meth protected java.lang.String getName()
meth protected java.lang.Thread getThread()
meth protected org.eclipse.core.runtime.IStatus getResult()
meth protected org.eclipse.core.runtime.jobs.ISchedulingRule getRule()
meth protected org.eclipse.core.runtime.jobs.Job yieldRule(org.eclipse.core.runtime.IProgressMonitor)
meth protected void addJobChangeListener(org.eclipse.core.runtime.jobs.IJobChangeListener)
meth protected void canceling()
meth protected void done(org.eclipse.core.runtime.IStatus)
meth protected void join() throws java.lang.InterruptedException
meth protected void removeJobChangeListener(org.eclipse.core.runtime.jobs.IJobChangeListener)
meth protected void schedule(long)
meth protected void setName(java.lang.String)
meth protected void setPriority(int)
meth protected void setProgressGroup(org.eclipse.core.runtime.IProgressMonitor,int)
meth protected void setProperty(org.eclipse.core.runtime.QualifiedName,java.lang.Object)
meth protected void setRule(org.eclipse.core.runtime.jobs.ISchedulingRule)
meth protected void setSystem(boolean)
meth protected void setThread(java.lang.Thread)
meth protected void setUser(boolean)
meth protected void wakeUp(long)
meth public final int compareTo(java.lang.Object)
meth public java.lang.String toString()
supr org.eclipse.core.runtime.PlatformObject
hfds ABOUT_TO_RUN,ABOUT_TO_SCHEDULE,BLOCKED,M_ABOUT_TO_RUN_CANCELED,M_RUN_CANCELED,M_STATE,M_SYSTEM,M_USER,T_INFINITE,T_NONE,YIELDING,class$0,flags,jobNumber,jobStateLock,listeners,monitor,name,next,nextJobNumber,previous,priority,properties,result,schedulingRule,startTime,thread,waitQueueStamp

CLSS public abstract interface org.eclipse.core.internal.preferences.exchange.ILegacyPreferences
meth public abstract java.lang.Object init(java.lang.Object,java.lang.String)

CLSS public abstract interface org.eclipse.core.internal.preferences.exchange.IProductPreferencesService
meth public abstract java.util.Properties getProductCustomization()
meth public abstract java.util.Properties getProductTranslation()

CLSS public org.eclipse.core.internal.preferences.legacy.InitLegacyPreferences
cons public init()
intf org.eclipse.core.internal.preferences.exchange.ILegacyPreferences
meth public java.lang.Object init(java.lang.Object,java.lang.String)
supr java.lang.Object

CLSS public org.eclipse.core.internal.preferences.legacy.PreferenceForwarder
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.String)
intf org.eclipse.core.runtime.preferences.IEclipsePreferences$INodeChangeListener
intf org.eclipse.core.runtime.preferences.IEclipsePreferences$IPreferenceChangeListener
meth public boolean contains(java.lang.String)
meth public boolean getBoolean(java.lang.String)
meth public boolean getDefaultBoolean(java.lang.String)
meth public boolean isDefault(java.lang.String)
meth public boolean needsSaving()
meth public double getDefaultDouble(java.lang.String)
meth public double getDouble(java.lang.String)
meth public float getDefaultFloat(java.lang.String)
meth public float getFloat(java.lang.String)
meth public int getDefaultInt(java.lang.String)
meth public int getInt(java.lang.String)
meth public java.lang.String getDefaultString(java.lang.String)
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String[] defaultPropertyNames()
meth public java.lang.String[] propertyNames()
meth public long getDefaultLong(java.lang.String)
meth public long getLong(java.lang.String)
meth public void addPropertyChangeListener(org.eclipse.core.runtime.Preferences$IPropertyChangeListener)
meth public void added(org.eclipse.core.runtime.preferences.IEclipsePreferences$NodeChangeEvent)
meth public void flush() throws org.osgi.service.prefs.BackingStoreException
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences$PreferenceChangeEvent)
meth public void removePropertyChangeListener(org.eclipse.core.runtime.Preferences$IPropertyChangeListener)
meth public void removed(org.eclipse.core.runtime.preferences.IEclipsePreferences$NodeChangeEvent)
meth public void setDefault(java.lang.String,boolean)
meth public void setDefault(java.lang.String,double)
meth public void setDefault(java.lang.String,float)
meth public void setDefault(java.lang.String,int)
meth public void setDefault(java.lang.String,java.lang.String)
meth public void setDefault(java.lang.String,long)
meth public void setToDefault(java.lang.String)
meth public void setValue(java.lang.String,boolean)
meth public void setValue(java.lang.String,double)
meth public void setValue(java.lang.String,float)
meth public void setValue(java.lang.String,int)
meth public void setValue(java.lang.String,java.lang.String)
meth public void setValue(java.lang.String,long)
meth public void store(java.io.OutputStream,java.lang.String) throws java.io.IOException
supr org.eclipse.core.runtime.Preferences
hfds BYTE_ARRAY_DEFAULT_DEFAULT,defaultsRoot,notify,plugin,pluginID,pluginRoot

CLSS public org.eclipse.core.internal.preferences.legacy.ProductPreferencesService
cons public init()
fld public final static java.lang.String PRODUCT_KEY = "preferenceCustomization"
intf org.eclipse.core.internal.preferences.exchange.IProductPreferencesService
meth public java.util.Properties getProductCustomization()
meth public java.util.Properties getProductTranslation()
supr java.lang.Object
hfds LEGACY_PRODUCT_CUSTOMIZATION_FILENAME,NL_DIR,PROPERTIES_FILE_EXTENSION,customizationBundle,customizationValue,initialized,productID

CLSS public org.eclipse.core.internal.runtime.Activator
cons public init()
intf org.osgi.framework.BundleActivator
meth public java.lang.String getBundleId(java.lang.Object)
meth public java.util.ResourceBundle getLocalization(org.osgi.framework.Bundle,java.lang.String)
meth public org.eclipse.osgi.framework.log.FrameworkLog getFrameworkLog()
meth public org.eclipse.osgi.service.datalocation.Location getConfigurationLocation()
meth public org.eclipse.osgi.service.datalocation.Location getInstallLocation()
meth public org.eclipse.osgi.service.datalocation.Location getInstanceLocation()
meth public org.eclipse.osgi.service.debug.DebugOptions getDebugOptions()
meth public org.osgi.framework.Bundle getBundle(java.lang.String)
meth public org.osgi.framework.Bundle[] getFragments(org.osgi.framework.Bundle)
meth public static org.eclipse.core.internal.runtime.Activator getDefault()
meth public static org.eclipse.osgi.service.urlconversion.URLConverter getURLConverter(java.net.URL)
meth public static void message(java.lang.String)
meth public void start(org.osgi.framework.BundleContext) throws java.lang.Exception
meth public void stop(org.osgi.framework.BundleContext) throws java.lang.Exception
supr java.lang.Object
hfds adapterManagerService,bundleContext,bundleTracker,class$0,class$1,class$2,class$3,class$4,class$5,class$6,class$7,class$8,configLocationTracker,debugTracker,installLocationTracker,instanceLocationTracker,localizationTracker,logTracker,platformURLConverterService,singleton,urlTrackers

CLSS public final org.eclipse.core.internal.runtime.AdapterManager
intf org.eclipse.core.runtime.IAdapterManager
meth public boolean hasAdapter(java.lang.Object,java.lang.String)
meth public boolean unregisterLazyFactoryProvider(org.eclipse.core.internal.runtime.IAdapterManagerProvider)
meth public int queryAdapter(java.lang.Object,java.lang.String)
meth public java.lang.Class[] computeClassOrder(java.lang.Class)
meth public java.lang.Object getAdapter(java.lang.Object,java.lang.Class)
meth public java.lang.Object getAdapter(java.lang.Object,java.lang.String)
meth public java.lang.Object loadAdapter(java.lang.Object,java.lang.String)
meth public java.lang.String[] computeAdapterTypes(java.lang.Class)
meth public java.util.HashMap getFactories()
meth public static org.eclipse.core.internal.runtime.AdapterManager getDefault()
meth public void flushLookup()
meth public void registerAdapters(org.eclipse.core.runtime.IAdapterFactory,java.lang.Class)
meth public void registerFactory(org.eclipse.core.runtime.IAdapterFactory,java.lang.String)
meth public void registerLazyFactoryProvider(org.eclipse.core.internal.runtime.IAdapterManagerProvider)
meth public void unregisterAdapters(org.eclipse.core.runtime.IAdapterFactory)
meth public void unregisterAdapters(org.eclipse.core.runtime.IAdapterFactory,java.lang.Class)
meth public void unregisterAllAdapters()
supr java.lang.Object
hfds adapterLookup,classLookup,classLookupLock,classSearchOrderLookup,factories,lazyFactoryProviders,singleton

CLSS public org.eclipse.core.internal.runtime.CommonMessages
cons public init()
fld public static java.lang.String activator_not_available
fld public static java.lang.String meta_couldNotCreate
fld public static java.lang.String meta_instanceDataUnspecified
fld public static java.lang.String meta_noDataModeSpecified
fld public static java.lang.String meta_notDir
fld public static java.lang.String meta_pluginProblems
fld public static java.lang.String meta_readonly
fld public static java.lang.String ok
fld public static java.lang.String parse_doubleSeparatorVersion
fld public static java.lang.String parse_emptyPluginVersion
fld public static java.lang.String parse_fourElementPluginVersion
fld public static java.lang.String parse_numericMajorComponent
fld public static java.lang.String parse_numericMinorComponent
fld public static java.lang.String parse_numericServiceComponent
fld public static java.lang.String parse_oneElementPluginVersion
fld public static java.lang.String parse_postiveMajor
fld public static java.lang.String parse_postiveMinor
fld public static java.lang.String parse_postiveService
fld public static java.lang.String parse_separatorEndVersion
fld public static java.lang.String parse_separatorStartVersion
fld public static java.lang.String url_badVariant
fld public static java.lang.String url_createConnection
fld public static java.lang.String url_invalidURL
fld public static java.lang.String url_noOutput
fld public static java.lang.String url_noaccess
fld public static java.lang.String url_resolveFragment
fld public static java.lang.String url_resolvePlugin
meth public static void reloadMessages()
supr org.eclipse.osgi.util.NLS
hfds BUNDLE_NAME,class$0

CLSS public org.eclipse.core.internal.runtime.CompatibilityHelper
cons public init()
fld public final static boolean DEBUG
fld public final static java.lang.String PI_RUNTIME_COMPATIBILITY = "org.eclipse.core.runtime.compatibility"
meth public static boolean hasPluginObject(org.eclipse.core.runtime.IPluginDescriptor)
meth public static org.eclipse.core.runtime.IPluginDescriptor getPluginDescriptor(java.lang.String)
meth public static org.osgi.framework.Bundle initializeCompatibility()
meth public static void nullCompatibility()
meth public static void setActive(org.eclipse.core.runtime.IPluginDescriptor)
meth public static void setPlugin(org.eclipse.core.runtime.IPluginDescriptor,org.eclipse.core.runtime.Plugin)
supr java.lang.Object
hfds OPTION_DEBUG_COMPATIBILITY,class$0,class$1,compatibility

CLSS public org.eclipse.core.internal.runtime.DataArea
cons public init()
meth protected void assertLocationInitialized()
meth public org.eclipse.core.runtime.IPath getInstanceDataLocation()
meth public org.eclipse.core.runtime.IPath getLogLocation()
meth public org.eclipse.core.runtime.IPath getMetadataLocation()
meth public org.eclipse.core.runtime.IPath getPreferenceLocation(java.lang.String,boolean)
meth public org.eclipse.core.runtime.IPath getStateLocation(java.lang.String)
meth public org.eclipse.core.runtime.IPath getStateLocation(org.osgi.framework.Bundle)
meth public org.eclipse.core.runtime.IPath getTraceLocation()
supr java.lang.Object
hfds F_LOG,F_META_AREA,F_PLUGIN_DATA,F_TRACE,OPTION_DEBUG,PREFERENCES_FILE_NAME,initialized,location

CLSS public org.eclipse.core.internal.runtime.DevClassPathHelper
cons public init()
fld protected static boolean inDevelopmentMode
fld protected static java.lang.String[] devDefaultClasspath
fld protected static java.util.Properties devProperties
fld public final static java.lang.String PROP_DEV = "osgi.dev"
meth public static boolean inDevelopmentMode()
meth public static java.lang.String[] getArrayFromList(java.lang.String)
meth public static java.lang.String[] getDevClassPath(java.lang.String)
supr java.lang.Object

CLSS public org.eclipse.core.internal.runtime.FindSupport
cons public init()
fld public final static java.lang.String PROP_ARCH = "osgi.arch"
fld public final static java.lang.String PROP_NL = "osgi.nl"
fld public final static java.lang.String PROP_OS = "osgi.os"
fld public final static java.lang.String PROP_WS = "osgi.ws"
meth public final static java.io.InputStream openStream(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,boolean) throws java.io.IOException
meth public static java.net.URL find(java.net.URL)
meth public static java.net.URL find(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath)
meth public static java.net.URL find(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,java.util.Map)
meth public static java.net.URL[] findEntries(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath)
meth public static java.net.URL[] findEntries(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,java.util.Map)
supr java.lang.Object
hfds NL_JAR_VARIANTS

CLSS public abstract interface org.eclipse.core.internal.runtime.IAdapterFactoryExt
meth public abstract java.lang.String[] getAdapterNames()
meth public abstract org.eclipse.core.runtime.IAdapterFactory loadFactory(boolean)

CLSS public abstract interface org.eclipse.core.internal.runtime.IAdapterManagerProvider
meth public abstract boolean addFactories(org.eclipse.core.internal.runtime.AdapterManager)

CLSS public abstract interface org.eclipse.core.internal.runtime.IRuntimeConstants
fld public final static int FAILED_WRITE_METADATA = 5
fld public final static int PLUGIN_ERROR = 2
fld public final static java.lang.String PI_COMMON = "org.eclipse.equinox.common"
fld public final static java.lang.String PI_RUNTIME = "org.eclipse.core.runtime"

CLSS public final org.eclipse.core.internal.runtime.InternalPlatform
fld public final static java.lang.String PROP_ACTIVATE_PLUGINS = "eclipse.activateRuntimePlugins"
fld public final static java.lang.String PROP_APPLICATION = "eclipse.application"
fld public final static java.lang.String PROP_ARCH = "osgi.arch"
fld public final static java.lang.String PROP_CONFIG_AREA = "osgi.configuration.area"
fld public final static java.lang.String PROP_CONSOLE_LOG = "eclipse.consoleLog"
fld public final static java.lang.String PROP_DEBUG = "osgi.debug"
fld public final static java.lang.String PROP_DEV = "osgi.dev"
fld public final static java.lang.String PROP_INSTALL_AREA = "osgi.install.area"
fld public final static java.lang.String PROP_NL = "osgi.nl"
fld public final static java.lang.String PROP_OS = "osgi.os"
fld public final static java.lang.String PROP_PRODUCT = "eclipse.product"
fld public final static java.lang.String PROP_WS = "osgi.ws"
fld public static boolean DEBUG
fld public static boolean DEBUG_PLUGIN_PREFERENCES
meth public boolean getBooleanOption(java.lang.String,boolean)
meth public boolean isFragment(org.osgi.framework.Bundle)
meth public boolean isRunning()
meth public java.lang.String getBundleId(java.lang.Object)
meth public java.lang.String getNL()
meth public java.lang.String getNLExtensions()
meth public java.lang.String getOS()
meth public java.lang.String getOSArch()
meth public java.lang.String getOption(java.lang.String)
meth public java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String)
meth public java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String,java.util.ResourceBundle)
meth public java.lang.String getWS()
meth public java.lang.String[] getApplicationArgs()
meth public java.lang.String[] getCommandLineArgs()
meth public java.lang.String[] knownOSArchValues()
meth public java.lang.String[] knownOSValues()
meth public java.lang.String[] knownWSValues()
meth public java.net.URL getInstallURL()
meth public java.net.URL[] getPluginPath(java.net.URL)
meth public java.util.ResourceBundle getResourceBundle(org.osgi.framework.Bundle)
meth public long getStateTimeStamp()
meth public org.eclipse.core.internal.runtime.DataArea getMetaArea()
meth public org.eclipse.core.runtime.IAdapterManager getAdapterManager()
meth public org.eclipse.core.runtime.IBundleGroupProvider[] getBundleGroupProviders()
meth public org.eclipse.core.runtime.IExtensionRegistry getRegistry()
meth public org.eclipse.core.runtime.ILog getLog(org.osgi.framework.Bundle)
meth public org.eclipse.core.runtime.IPath getLocation()
meth public org.eclipse.core.runtime.IPath getStateLocation(org.osgi.framework.Bundle)
meth public org.eclipse.core.runtime.IPath getStateLocation(org.osgi.framework.Bundle,boolean)
meth public org.eclipse.core.runtime.IProduct getProduct()
meth public org.eclipse.core.runtime.Plugin getRuntimeInstance()
meth public org.eclipse.core.runtime.content.IContentTypeManager getContentTypeManager()
meth public org.eclipse.core.runtime.preferences.IPreferencesService getPreferencesService()
meth public org.eclipse.osgi.framework.log.FrameworkLog getFrameworkLog()
meth public org.eclipse.osgi.service.datalocation.Location getConfigurationLocation()
meth public org.eclipse.osgi.service.datalocation.Location getInstallLocation()
meth public org.eclipse.osgi.service.datalocation.Location getInstanceLocation()
meth public org.eclipse.osgi.service.datalocation.Location getUserLocation()
meth public org.eclipse.osgi.service.environment.EnvironmentInfo getEnvironmentInfoService()
meth public org.eclipse.osgi.service.resolver.PlatformAdmin getPlatformAdmin()
meth public org.osgi.framework.Bundle getBundle(java.lang.String)
meth public org.osgi.framework.BundleContext getBundleContext()
meth public org.osgi.framework.Bundle[] getBundles(java.lang.String,java.lang.String)
meth public org.osgi.framework.Bundle[] getFragments(org.osgi.framework.Bundle)
meth public org.osgi.framework.Bundle[] getHosts(org.osgi.framework.Bundle)
meth public static org.eclipse.core.internal.runtime.InternalPlatform getDefault()
meth public static void message(java.lang.String)
meth public static void start(org.osgi.framework.Bundle) throws org.osgi.framework.BundleException
meth public void addLogListener(org.eclipse.core.runtime.ILogListener)
meth public void endSplash()
meth public void log(org.eclipse.core.runtime.IStatus)
meth public void registerBundleGroupProvider(org.eclipse.core.runtime.IBundleGroupProvider)
meth public void removeLogListener(org.eclipse.core.runtime.ILogListener)
meth public void setRuntimeInstance(org.eclipse.core.runtime.Plugin)
meth public void start(org.osgi.framework.BundleContext)
meth public void stop(org.osgi.framework.BundleContext)
meth public void unregisterBundleGroupProvider(org.eclipse.core.runtime.IBundleGroupProvider)
supr java.lang.Object
hfds ARCH_LIST,KEYRING,OS_LIST,PASSWORD,PLUGIN_PATH,WS_LIST,bundleTracker,cachedInstanceLocation,class$0,class$1,class$10,class$11,class$12,class$2,class$3,class$4,class$5,class$6,class$7,class$8,class$9,configurationLocation,contentTracker,context,customPreferencesService,debugTracker,environmentTracker,extendedLogTracker,groupProviderTracker,groupProviders,initialized,installLocation,instanceLocation,keyringFile,legacyPreferencesService,logReaderTracker,logTracker,logs,password,preferencesTracker,product,runtimeInstance,singleton,splashEnded,userLocation

CLSS public org.eclipse.core.internal.runtime.LocalizationUtils
cons public init()
meth public static java.lang.String safeLocalize(java.lang.String)
supr java.lang.Object

CLSS public org.eclipse.core.internal.runtime.Log
cons public init(org.osgi.framework.Bundle,org.eclipse.equinox.log.Logger)
intf org.eclipse.core.runtime.ILog
intf org.eclipse.equinox.log.LogFilter
intf org.eclipse.equinox.log.SynchronousLogListener
meth public boolean isLoggable(org.osgi.framework.Bundle,java.lang.String,int)
meth public org.osgi.framework.Bundle getBundle()
meth public void addLogListener(org.eclipse.core.runtime.ILogListener)
meth public void log(org.eclipse.core.runtime.IStatus)
meth public void logged(org.osgi.service.log.LogEntry)
meth public void removeLogListener(org.eclipse.core.runtime.ILogListener)
supr java.lang.Object
hfds bundle,logListeners,logger

CLSS public org.eclipse.core.internal.runtime.Messages
cons public init()
fld public static java.lang.String auth_notAvailable
fld public static java.lang.String line_separator_platform_mac_os_9
fld public static java.lang.String line_separator_platform_unix
fld public static java.lang.String line_separator_platform_windows
fld public static java.lang.String meta_appNotInit
fld public static java.lang.String meta_exceptionParsingLog
fld public static java.lang.String parse_badPrereqOnFrag
fld public static java.lang.String parse_duplicateFragment
fld public static java.lang.String parse_duplicateLib
fld public static java.lang.String parse_duplicatePlugin
fld public static java.lang.String parse_error
fld public static java.lang.String parse_errorNameLineColumn
fld public static java.lang.String parse_errorProcessing
fld public static java.lang.String parse_extPointDisabled
fld public static java.lang.String parse_extPointUnknown
fld public static java.lang.String parse_fragmentMissingAttr
fld public static java.lang.String parse_fragmentMissingIdName
fld public static java.lang.String parse_internalStack
fld public static java.lang.String parse_missingFPName
fld public static java.lang.String parse_missingFPVersion
fld public static java.lang.String parse_missingFragmentPd
fld public static java.lang.String parse_missingPluginId
fld public static java.lang.String parse_missingPluginName
fld public static java.lang.String parse_missingPluginVersion
fld public static java.lang.String parse_nullFragmentIdentifier
fld public static java.lang.String parse_nullPluginIdentifier
fld public static java.lang.String parse_pluginMissingAttr
fld public static java.lang.String parse_pluginMissingIdName
fld public static java.lang.String parse_prereqDisabled
fld public static java.lang.String parse_prereqLoop
fld public static java.lang.String parse_prereqOptLoop
fld public static java.lang.String parse_unknownAttribute
fld public static java.lang.String parse_unknownElement
fld public static java.lang.String parse_unknownEntry
fld public static java.lang.String parse_unknownLibraryType
fld public static java.lang.String parse_unknownTopElement
fld public static java.lang.String parse_unsatisfiedOptPrereq
fld public static java.lang.String parse_unsatisfiedPrereq
fld public static java.lang.String parse_validExport
fld public static java.lang.String parse_validMatch
fld public static java.lang.String plugin_deactivatedLoad
fld public static java.lang.String plugin_instantiateClassError
fld public static java.lang.String plugin_loadClassError
fld public static java.lang.String plugin_notPluginClass
fld public static java.lang.String plugin_pluginDisabled
fld public static java.lang.String plugin_shutdownProblems
fld public static java.lang.String plugin_startupProblems
fld public static java.lang.String plugin_unableToResolve
fld public static java.lang.String preferences_saveProblems
meth public static void reloadMessages()
supr org.eclipse.osgi.util.NLS
hfds BUNDLE_NAME,class$0

CLSS public org.eclipse.core.internal.runtime.MetaDataKeeper
cons public init()
meth public static org.eclipse.core.internal.runtime.DataArea getMetaArea()
supr java.lang.Object
hfds metaArea

CLSS public org.eclipse.core.internal.runtime.PerformanceStatsProcessor
meth protected org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor)
meth public boolean shouldRun()
meth public static void addListener(org.eclipse.core.runtime.PerformanceStats$PerformanceListener)
meth public static void changed(org.eclipse.core.runtime.PerformanceStats)
meth public static void failed(org.eclipse.core.runtime.PerformanceStats,java.lang.String,long)
meth public static void printStats(java.io.PrintWriter)
meth public static void removeListener(org.eclipse.core.runtime.PerformanceStats$PerformanceListener)
supr org.eclipse.core.runtime.jobs.Job
hfds SCHEDULE_DELAY,changes,class$0,failures,instance,listeners,log

CLSS public org.eclipse.core.internal.runtime.PlatformActivator
cons public init()
intf org.osgi.framework.BundleActivator
meth public static org.osgi.framework.BundleContext getContext()
meth public void start(org.osgi.framework.BundleContext) throws java.lang.Exception
meth public void stop(org.osgi.framework.BundleContext)
supr org.eclipse.core.runtime.Plugin
hfds context

CLSS public org.eclipse.core.internal.runtime.PlatformLogWriter
cons public init(org.eclipse.equinox.log.ExtendedLogService,org.osgi.service.packageadmin.PackageAdmin,org.osgi.framework.Bundle)
fld public final static java.lang.String EQUINOX_LOGGER_NAME = "org.eclipse.equinox.logger"
intf org.eclipse.equinox.log.LogFilter
intf org.eclipse.equinox.log.SynchronousLogListener
meth public boolean isLoggable(org.osgi.framework.Bundle,java.lang.String,int)
meth public static int getLevel(org.eclipse.core.runtime.IStatus)
meth public static org.eclipse.core.runtime.IStatus convertToStatus(org.osgi.service.log.LogEntry)
meth public static org.eclipse.osgi.framework.log.FrameworkLogEntry getLog(org.eclipse.core.runtime.IStatus)
meth public void logged(org.osgi.service.log.LogEntry)
supr java.lang.Object
hfds bundle,logService,packageAdmin

CLSS public org.eclipse.core.internal.runtime.PlatformURLConfigConnection
cons public init(java.net.URL)
fld public final static java.lang.String CONFIG = "config"
meth protected java.net.URL resolve() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public static void startup()
supr org.eclipse.core.internal.boot.PlatformURLConnection
hfds FILE_PROTOCOL,class$0,isRegistered,parentConfiguration

CLSS public org.eclipse.core.internal.runtime.PlatformURLConverter
cons public init()
intf org.eclipse.osgi.service.urlconversion.URLConverter
meth public java.net.URL resolve(java.net.URL) throws java.io.IOException
meth public java.net.URL toFileURL(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public org.eclipse.core.internal.runtime.PlatformURLFragmentConnection
cons public init(java.net.URL)
fld public final static java.lang.String FRAGMENT = "fragment"
meth protected boolean allowCaching()
meth protected java.net.URL resolve() throws java.io.IOException
meth public static void startup()
supr org.eclipse.core.internal.boot.PlatformURLConnection
hfds class$0,isRegistered,target

CLSS public org.eclipse.core.internal.runtime.PlatformURLMetaConnection
cons public init(java.net.URL)
fld public final static java.lang.String META = "meta"
meth protected java.net.URL resolve() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public static void startup()
supr org.eclipse.core.internal.boot.PlatformURLConnection
hfds class$0,isRegistered,target

CLSS public org.eclipse.core.internal.runtime.PlatformURLPluginConnection
cons public init(java.net.URL)
fld public final static java.lang.String PLUGIN = "plugin"
meth protected boolean allowCaching()
meth protected java.net.URL resolve() throws java.io.IOException
meth public java.net.URL[] getAuxillaryURLs() throws java.io.IOException
meth public static java.lang.Object[] parse(java.lang.String,java.net.URL) throws java.io.IOException
meth public static void startup()
supr org.eclipse.core.internal.boot.PlatformURLConnection
hfds class$0,isRegistered,target

CLSS public org.eclipse.core.internal.runtime.PrintStackUtil
cons public init()
meth public static void printChildren(org.eclipse.core.runtime.IStatus,java.io.PrintStream)
meth public static void printChildren(org.eclipse.core.runtime.IStatus,java.io.PrintWriter)
supr java.lang.Object

CLSS public org.eclipse.core.internal.runtime.Product
cons public init(org.eclipse.equinox.internal.app.IBranding)
intf org.eclipse.core.runtime.IProduct
meth public java.lang.String getApplication()
meth public java.lang.String getDescription()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getProperty(java.lang.String)
meth public org.osgi.framework.Bundle getDefiningBundle()
supr java.lang.Object
hfds branding

CLSS public org.eclipse.core.internal.runtime.ReferenceHashSet
cons public init()
cons public init(int)
fld public final static int HARD = 0
fld public final static int SOFT = 1
fld public final static int WEAK = 2
fld public int elementSize
meth public boolean contains(java.lang.Object)
meth public int size()
meth public java.lang.Object add(java.lang.Object,int)
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
supr java.lang.Object
hfds referenceQueue,threshold,values
hcls HashableSoftReference,HashableWeakReference,HashedReference,StrongReference

CLSS public org.eclipse.core.internal.runtime.ResourceTranslator
cons public init()
meth public static java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String)
meth public static java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String,java.util.ResourceBundle)
meth public static java.lang.String[] getResourceString(org.osgi.framework.Bundle,java.lang.String[],java.lang.String)
meth public static java.util.ResourceBundle getResourceBundle(org.osgi.framework.Bundle)
supr java.lang.Object
hfds KEY_DOUBLE_PREFIX,KEY_PREFIX

CLSS public final org.eclipse.core.internal.runtime.RuntimeLog
cons public init()
meth public static boolean contains(org.eclipse.core.runtime.ILogListener)
meth public static boolean hasListeners()
meth public static boolean isEmpty()
meth public static void addLogListener(org.eclipse.core.runtime.ILogListener)
meth public static void log(org.eclipse.core.runtime.IStatus)
meth public static void removeLogListener(org.eclipse.core.runtime.ILogListener)
supr java.lang.Object
hfds logListeners,logWriter,queuedMessages

CLSS public final org.eclipse.core.runtime.Assert
meth public static boolean isLegal(boolean)
meth public static boolean isLegal(boolean,java.lang.String)
meth public static boolean isTrue(boolean)
meth public static boolean isTrue(boolean,java.lang.String)
meth public static void isNotNull(java.lang.Object)
meth public static void isNotNull(java.lang.Object,java.lang.String)
supr java.lang.Object

CLSS public org.eclipse.core.runtime.AssertionFailedException
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final org.eclipse.core.runtime.ContributorFactoryOSGi
cons public init()
meth public static org.eclipse.core.runtime.IContributor createContributor(org.osgi.framework.Bundle)
meth public static org.osgi.framework.Bundle resolve(org.eclipse.core.runtime.IContributor)
supr java.lang.Object

CLSS public final org.eclipse.core.runtime.ContributorFactorySimple
cons public init()
meth public static org.eclipse.core.runtime.IContributor createContributor(java.lang.Object)
supr java.lang.Object

CLSS public org.eclipse.core.runtime.CoreException
cons public init(org.eclipse.core.runtime.IStatus)
meth public final org.eclipse.core.runtime.IStatus getStatus()
meth public java.lang.Throwable getCause()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.lang.Exception
hfds serialVersionUID,status

CLSS public final org.eclipse.core.runtime.FileLocator
meth public static java.io.File getBundleFile(org.osgi.framework.Bundle) throws java.io.IOException
meth public static java.io.InputStream openStream(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,boolean) throws java.io.IOException
meth public static java.net.URL find(java.net.URL)
meth public static java.net.URL find(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,java.util.Map)
meth public static java.net.URL resolve(java.net.URL) throws java.io.IOException
meth public static java.net.URL toFileURL(java.net.URL) throws java.io.IOException
meth public static java.net.URL[] findEntries(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath)
meth public static java.net.URL[] findEntries(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,java.util.Map)
supr java.lang.Object

CLSS public abstract interface org.eclipse.core.runtime.IAdaptable
meth public abstract java.lang.Object getAdapter(java.lang.Class)

CLSS public abstract interface org.eclipse.core.runtime.IAdapterFactory
meth public abstract java.lang.Class[] getAdapterList()
meth public abstract java.lang.Object getAdapter(java.lang.Object,java.lang.Class)

CLSS public abstract interface org.eclipse.core.runtime.IAdapterManager
fld public final static int LOADED = 2
fld public final static int NONE = 0
fld public final static int NOT_LOADED = 1
meth public abstract boolean hasAdapter(java.lang.Object,java.lang.String)
meth public abstract int queryAdapter(java.lang.Object,java.lang.String)
meth public abstract java.lang.Class[] computeClassOrder(java.lang.Class)
meth public abstract java.lang.Object getAdapter(java.lang.Object,java.lang.Class)
meth public abstract java.lang.Object getAdapter(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object loadAdapter(java.lang.Object,java.lang.String)
meth public abstract java.lang.String[] computeAdapterTypes(java.lang.Class)
meth public abstract void registerAdapters(org.eclipse.core.runtime.IAdapterFactory,java.lang.Class)
meth public abstract void unregisterAdapters(org.eclipse.core.runtime.IAdapterFactory)
meth public abstract void unregisterAdapters(org.eclipse.core.runtime.IAdapterFactory,java.lang.Class)

CLSS public abstract interface org.eclipse.core.runtime.IBundleGroup
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getIdentifier()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.lang.String getProviderName()
meth public abstract java.lang.String getVersion()
meth public abstract org.osgi.framework.Bundle[] getBundles()

CLSS public abstract interface org.eclipse.core.runtime.IBundleGroupProvider
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.core.runtime.IBundleGroup[] getBundleGroups()

CLSS public abstract interface org.eclipse.core.runtime.IConfigurationElement
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isValid()
meth public abstract java.lang.Object createExecutableExtension(java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public abstract java.lang.Object getParent()
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public abstract java.lang.String getAttributeAsIs(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespaceIdentifier()
meth public abstract java.lang.String getValue()
meth public abstract java.lang.String getValue(java.lang.String)
meth public abstract java.lang.String getValueAsIs()
meth public abstract java.lang.String[] getAttributeNames()
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getChildren()
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getChildren(java.lang.String)
meth public abstract org.eclipse.core.runtime.IContributor getContributor()
meth public abstract org.eclipse.core.runtime.IExtension getDeclaringExtension()

CLSS public abstract interface org.eclipse.core.runtime.IContributor
meth public abstract java.lang.String getName()

CLSS public abstract interface org.eclipse.core.runtime.IExecutableExtension
meth public abstract void setInitializationData(org.eclipse.core.runtime.IConfigurationElement,java.lang.String,java.lang.Object) throws org.eclipse.core.runtime.CoreException

CLSS public abstract interface org.eclipse.core.runtime.IExecutableExtensionFactory
meth public abstract java.lang.Object create() throws org.eclipse.core.runtime.CoreException

CLSS public abstract interface org.eclipse.core.runtime.IExtension
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isValid()
meth public abstract java.lang.String getExtensionPointUniqueIdentifier()
meth public abstract java.lang.String getLabel()
meth public abstract java.lang.String getLabel(java.lang.String)
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespaceIdentifier()
meth public abstract java.lang.String getSimpleIdentifier()
meth public abstract java.lang.String getUniqueIdentifier()
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElements()
meth public abstract org.eclipse.core.runtime.IContributor getContributor()

CLSS public abstract interface org.eclipse.core.runtime.IExtensionDelta
fld public final static int ADDED = 1
fld public final static int REMOVED = 2
meth public abstract int getKind()
meth public abstract org.eclipse.core.runtime.IExtension getExtension()
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint()

CLSS public abstract interface org.eclipse.core.runtime.IExtensionPoint
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isValid()
meth public abstract java.lang.String getLabel()
meth public abstract java.lang.String getLabel(java.lang.String)
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespaceIdentifier()
meth public abstract java.lang.String getSchemaReference()
meth public abstract java.lang.String getSimpleIdentifier()
meth public abstract java.lang.String getUniqueIdentifier()
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElements()
meth public abstract org.eclipse.core.runtime.IContributor getContributor()
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension[] getExtensions()

CLSS public abstract interface org.eclipse.core.runtime.IExtensionRegistry
meth public abstract boolean addContribution(java.io.InputStream,org.eclipse.core.runtime.IContributor,boolean,java.lang.String,java.util.ResourceBundle,java.lang.Object)
meth public abstract boolean isMultiLanguage()
meth public abstract boolean removeExtension(org.eclipse.core.runtime.IExtension,java.lang.Object)
meth public abstract boolean removeExtensionPoint(org.eclipse.core.runtime.IExtensionPoint,java.lang.Object)
meth public abstract java.lang.String[] getNamespaces()
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String)
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint[] getExtensionPoints()
meth public abstract org.eclipse.core.runtime.IExtensionPoint[] getExtensionPoints(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint[] getExtensionPoints(org.eclipse.core.runtime.IContributor)
meth public abstract org.eclipse.core.runtime.IExtension[] getExtensions(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension[] getExtensions(org.eclipse.core.runtime.IContributor)
meth public abstract void addListener(org.eclipse.core.runtime.IRegistryEventListener)
meth public abstract void addListener(org.eclipse.core.runtime.IRegistryEventListener,java.lang.String)
meth public abstract void addRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener)
meth public abstract void addRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener,java.lang.String)
meth public abstract void removeListener(org.eclipse.core.runtime.IRegistryEventListener)
meth public abstract void removeRegistryChangeListener(org.eclipse.core.runtime.IRegistryChangeListener)
meth public abstract void stop(java.lang.Object)

CLSS public abstract interface org.eclipse.core.runtime.ILibrary
fld public final static java.lang.String CODE = "code"
fld public final static java.lang.String RESOURCE = "resource"
meth public abstract boolean isExported()
meth public abstract boolean isFullyExported()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String[] getContentFilters()
meth public abstract java.lang.String[] getPackagePrefixes()
meth public abstract org.eclipse.core.runtime.IPath getPath()

CLSS public abstract interface org.eclipse.core.runtime.ILog
meth public abstract org.osgi.framework.Bundle getBundle()
meth public abstract void addLogListener(org.eclipse.core.runtime.ILogListener)
meth public abstract void log(org.eclipse.core.runtime.IStatus)
meth public abstract void removeLogListener(org.eclipse.core.runtime.ILogListener)

CLSS public abstract interface org.eclipse.core.runtime.ILogListener
intf java.util.EventListener
meth public abstract void logging(org.eclipse.core.runtime.IStatus,java.lang.String)

CLSS public abstract interface org.eclipse.core.runtime.IPath
fld public final static char DEVICE_SEPARATOR = ':'
fld public final static char SEPARATOR = '/'
intf java.lang.Cloneable
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean hasTrailingSeparator()
meth public abstract boolean isAbsolute()
meth public abstract boolean isEmpty()
meth public abstract boolean isPrefixOf(org.eclipse.core.runtime.IPath)
meth public abstract boolean isRoot()
meth public abstract boolean isUNC()
meth public abstract boolean isValidPath(java.lang.String)
meth public abstract boolean isValidSegment(java.lang.String)
meth public abstract int matchingFirstSegments(org.eclipse.core.runtime.IPath)
meth public abstract int segmentCount()
meth public abstract java.io.File toFile()
meth public abstract java.lang.Object clone()
meth public abstract java.lang.String getDevice()
meth public abstract java.lang.String getFileExtension()
meth public abstract java.lang.String lastSegment()
meth public abstract java.lang.String segment(int)
meth public abstract java.lang.String toOSString()
meth public abstract java.lang.String toPortableString()
meth public abstract java.lang.String toString()
meth public abstract java.lang.String[] segments()
meth public abstract org.eclipse.core.runtime.IPath addFileExtension(java.lang.String)
meth public abstract org.eclipse.core.runtime.IPath addTrailingSeparator()
meth public abstract org.eclipse.core.runtime.IPath append(java.lang.String)
meth public abstract org.eclipse.core.runtime.IPath append(org.eclipse.core.runtime.IPath)
meth public abstract org.eclipse.core.runtime.IPath makeAbsolute()
meth public abstract org.eclipse.core.runtime.IPath makeRelative()
meth public abstract org.eclipse.core.runtime.IPath makeRelativeTo(org.eclipse.core.runtime.IPath)
meth public abstract org.eclipse.core.runtime.IPath makeUNC(boolean)
meth public abstract org.eclipse.core.runtime.IPath removeFileExtension()
meth public abstract org.eclipse.core.runtime.IPath removeFirstSegments(int)
meth public abstract org.eclipse.core.runtime.IPath removeLastSegments(int)
meth public abstract org.eclipse.core.runtime.IPath removeTrailingSeparator()
meth public abstract org.eclipse.core.runtime.IPath setDevice(java.lang.String)
meth public abstract org.eclipse.core.runtime.IPath uptoSegment(int)

CLSS public abstract interface org.eclipse.core.runtime.IPlatformRunnable
fld public final static java.lang.Integer EXIT_OK
fld public final static java.lang.Integer EXIT_RELAUNCH
fld public final static java.lang.Integer EXIT_RESTART
meth public abstract java.lang.Object run(java.lang.Object) throws java.lang.Exception

CLSS public abstract interface org.eclipse.core.runtime.IPluginDescriptor
meth public abstract boolean isPluginActivated()
meth public abstract java.lang.ClassLoader getPluginClassLoader()
meth public abstract java.lang.String getLabel()
meth public abstract java.lang.String getProviderName()
meth public abstract java.lang.String getResourceString(java.lang.String)
meth public abstract java.lang.String getResourceString(java.lang.String,java.util.ResourceBundle)
meth public abstract java.lang.String getUniqueIdentifier()
meth public abstract java.net.URL find(org.eclipse.core.runtime.IPath)
meth public abstract java.net.URL find(org.eclipse.core.runtime.IPath,java.util.Map)
meth public abstract java.net.URL getInstallURL()
meth public abstract java.util.ResourceBundle getResourceBundle()
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint[] getExtensionPoints()
meth public abstract org.eclipse.core.runtime.IExtension[] getExtensions()
meth public abstract org.eclipse.core.runtime.ILibrary[] getRuntimeLibraries()
meth public abstract org.eclipse.core.runtime.IPluginPrerequisite[] getPluginPrerequisites()
meth public abstract org.eclipse.core.runtime.Plugin getPlugin() throws org.eclipse.core.runtime.CoreException
meth public abstract org.eclipse.core.runtime.PluginVersionIdentifier getVersionIdentifier()

CLSS public abstract interface org.eclipse.core.runtime.IPluginPrerequisite
meth public abstract boolean isExported()
meth public abstract boolean isMatchedAsCompatible()
meth public abstract boolean isMatchedAsEquivalent()
meth public abstract boolean isMatchedAsExact()
meth public abstract boolean isMatchedAsGreaterOrEqual()
meth public abstract boolean isMatchedAsPerfect()
meth public abstract boolean isOptional()
meth public abstract java.lang.String getUniqueIdentifier()
meth public abstract org.eclipse.core.runtime.PluginVersionIdentifier getResolvedVersionIdentifier()
meth public abstract org.eclipse.core.runtime.PluginVersionIdentifier getVersionIdentifier()

CLSS public abstract interface org.eclipse.core.runtime.IPluginRegistry
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String)
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IConfigurationElement[] getConfigurationElementsFor(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtension getExtension(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint getExtensionPoint(java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionPoint[] getExtensionPoints()
meth public abstract org.eclipse.core.runtime.IPluginDescriptor getPluginDescriptor(java.lang.String)
meth public abstract org.eclipse.core.runtime.IPluginDescriptor getPluginDescriptor(java.lang.String,org.eclipse.core.runtime.PluginVersionIdentifier)
meth public abstract org.eclipse.core.runtime.IPluginDescriptor[] getPluginDescriptors()
meth public abstract org.eclipse.core.runtime.IPluginDescriptor[] getPluginDescriptors(java.lang.String)

CLSS public abstract interface org.eclipse.core.runtime.IProduct
meth public abstract java.lang.String getApplication()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract org.osgi.framework.Bundle getDefiningBundle()

CLSS public abstract interface org.eclipse.core.runtime.IProductProvider
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.core.runtime.IProduct[] getProducts()

CLSS public abstract interface org.eclipse.core.runtime.IProgressMonitor
fld public final static int UNKNOWN = -1
meth public abstract boolean isCanceled()
meth public abstract void beginTask(java.lang.String,int)
meth public abstract void done()
meth public abstract void internalWorked(double)
meth public abstract void setCanceled(boolean)
meth public abstract void setTaskName(java.lang.String)
meth public abstract void subTask(java.lang.String)
meth public abstract void worked(int)

CLSS public abstract interface org.eclipse.core.runtime.IProgressMonitorWithBlocking
intf org.eclipse.core.runtime.IProgressMonitor
meth public abstract void clearBlocked()
meth public abstract void setBlocked(org.eclipse.core.runtime.IStatus)

CLSS public abstract interface org.eclipse.core.runtime.IRegistryChangeEvent
meth public abstract org.eclipse.core.runtime.IExtensionDelta getExtensionDelta(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionDelta[] getExtensionDeltas()
meth public abstract org.eclipse.core.runtime.IExtensionDelta[] getExtensionDeltas(java.lang.String)
meth public abstract org.eclipse.core.runtime.IExtensionDelta[] getExtensionDeltas(java.lang.String,java.lang.String)

CLSS public abstract interface org.eclipse.core.runtime.IRegistryChangeListener
intf java.util.EventListener
meth public abstract void registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)

CLSS public abstract interface org.eclipse.core.runtime.IRegistryEventListener
intf java.util.EventListener
meth public abstract void added(org.eclipse.core.runtime.IExtensionPoint[])
meth public abstract void added(org.eclipse.core.runtime.IExtension[])
meth public abstract void removed(org.eclipse.core.runtime.IExtensionPoint[])
meth public abstract void removed(org.eclipse.core.runtime.IExtension[])

CLSS public abstract interface org.eclipse.core.runtime.ISafeRunnable
meth public abstract void handleException(java.lang.Throwable)
meth public abstract void run() throws java.lang.Exception

CLSS public abstract interface org.eclipse.core.runtime.IStatus
fld public final static int CANCEL = 8
fld public final static int ERROR = 4
fld public final static int INFO = 1
fld public final static int OK = 0
fld public final static int WARNING = 2
meth public abstract boolean isMultiStatus()
meth public abstract boolean isOK()
meth public abstract boolean matches(int)
meth public abstract int getCode()
meth public abstract int getSeverity()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getPlugin()
meth public abstract java.lang.Throwable getException()
meth public abstract org.eclipse.core.runtime.IStatus[] getChildren()

CLSS public org.eclipse.core.runtime.InvalidRegistryObjectException
cons public init()
supr java.lang.RuntimeException
hfds MESSAGE,serialVersionUID

CLSS public org.eclipse.core.runtime.ListenerList
cons public init()
cons public init(int)
fld public final static int EQUALITY = 0
fld public final static int IDENTITY = 1
meth public boolean isEmpty()
meth public int size()
meth public java.lang.Object[] getListeners()
meth public void add(java.lang.Object)
meth public void clear()
meth public void remove(java.lang.Object)
supr java.lang.Object
hfds EmptyArray,identity,listeners

CLSS public org.eclipse.core.runtime.MultiStatus
cons public init(java.lang.String,int,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,int,org.eclipse.core.runtime.IStatus[],java.lang.String,java.lang.Throwable)
meth public boolean isMultiStatus()
meth public java.lang.String toString()
meth public org.eclipse.core.runtime.IStatus[] getChildren()
meth public void add(org.eclipse.core.runtime.IStatus)
meth public void addAll(org.eclipse.core.runtime.IStatus)
meth public void merge(org.eclipse.core.runtime.IStatus)
supr org.eclipse.core.runtime.Status
hfds children

CLSS public org.eclipse.core.runtime.NullProgressMonitor
cons public init()
intf org.eclipse.core.runtime.IProgressMonitor
meth public boolean isCanceled()
meth public void beginTask(java.lang.String,int)
meth public void done()
meth public void internalWorked(double)
meth public void setCanceled(boolean)
meth public void setTaskName(java.lang.String)
meth public void subTask(java.lang.String)
meth public void worked(int)
supr java.lang.Object
hfds cancelled

CLSS public final org.eclipse.core.runtime.OperationCanceledException
cons public init()
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public org.eclipse.core.runtime.Path
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld public final static org.eclipse.core.runtime.Path EMPTY
fld public final static org.eclipse.core.runtime.Path ROOT
intf java.lang.Cloneable
intf org.eclipse.core.runtime.IPath
meth public boolean equals(java.lang.Object)
meth public boolean hasTrailingSeparator()
meth public boolean isAbsolute()
meth public boolean isEmpty()
meth public boolean isPrefixOf(org.eclipse.core.runtime.IPath)
meth public boolean isRoot()
meth public boolean isUNC()
meth public boolean isValidPath(java.lang.String)
meth public boolean isValidSegment(java.lang.String)
meth public int hashCode()
meth public int matchingFirstSegments(org.eclipse.core.runtime.IPath)
meth public int segmentCount()
meth public java.io.File toFile()
meth public java.lang.Object clone()
meth public java.lang.String getDevice()
meth public java.lang.String getFileExtension()
meth public java.lang.String lastSegment()
meth public java.lang.String segment(int)
meth public java.lang.String toOSString()
meth public java.lang.String toPortableString()
meth public java.lang.String toString()
meth public java.lang.String[] segments()
meth public org.eclipse.core.runtime.IPath addFileExtension(java.lang.String)
meth public org.eclipse.core.runtime.IPath addTrailingSeparator()
meth public org.eclipse.core.runtime.IPath append(java.lang.String)
meth public org.eclipse.core.runtime.IPath append(org.eclipse.core.runtime.IPath)
meth public org.eclipse.core.runtime.IPath makeAbsolute()
meth public org.eclipse.core.runtime.IPath makeRelative()
meth public org.eclipse.core.runtime.IPath makeRelativeTo(org.eclipse.core.runtime.IPath)
meth public org.eclipse.core.runtime.IPath makeUNC(boolean)
meth public org.eclipse.core.runtime.IPath removeFileExtension()
meth public org.eclipse.core.runtime.IPath removeFirstSegments(int)
meth public org.eclipse.core.runtime.IPath removeLastSegments(int)
meth public org.eclipse.core.runtime.IPath removeTrailingSeparator()
meth public org.eclipse.core.runtime.IPath setDevice(java.lang.String)
meth public org.eclipse.core.runtime.IPath uptoSegment(int)
meth public static org.eclipse.core.runtime.IPath fromOSString(java.lang.String)
meth public static org.eclipse.core.runtime.IPath fromPortableString(java.lang.String)
supr java.lang.Object
hfds ALL_SEPARATORS,EMPTY_STRING,HASH_MASK,HAS_LEADING,HAS_TRAILING,IS_UNC,NO_SEGMENTS,ROOT_STRING,WINDOWS,device,segments,separators

CLSS public org.eclipse.core.runtime.PerformanceStats
fld public final static boolean ENABLED
innr public abstract static PerformanceListener
meth public boolean equals(java.lang.Object)
meth public boolean isFailure()
meth public int getRunCount()
meth public int hashCode()
meth public java.lang.Object getBlame()
meth public java.lang.String getBlameString()
meth public java.lang.String getContext()
meth public java.lang.String getEvent()
meth public java.lang.String toString()
meth public long getRunningTime()
meth public static boolean isEnabled(java.lang.String)
meth public static org.eclipse.core.runtime.PerformanceStats getStats(java.lang.String,java.lang.Object)
meth public static org.eclipse.core.runtime.PerformanceStats[] getAllStats()
meth public static void addListener(org.eclipse.core.runtime.PerformanceStats$PerformanceListener)
meth public static void clear()
meth public static void printStats()
meth public static void printStats(java.io.PrintWriter)
meth public static void removeListener(org.eclipse.core.runtime.PerformanceStats$PerformanceListener)
meth public static void removeStats(java.lang.String,java.lang.Object)
meth public void addRun(long,java.lang.String)
meth public void endRun()
meth public void reset()
meth public void startRun()
meth public void startRun(java.lang.String)
supr java.lang.Object
hfds EMPTY_STATS,NOT_STARTED,TRACE_SUCCESS,blame,blamePluginId,context,currentStart,event,isFailure,runCount,runningTime,statMap,thresholdMap

CLSS public abstract static org.eclipse.core.runtime.PerformanceStats$PerformanceListener
 outer org.eclipse.core.runtime.PerformanceStats
cons protected init()
meth public void eventFailed(org.eclipse.core.runtime.PerformanceStats,long)
meth public void eventsOccurred(org.eclipse.core.runtime.PerformanceStats[])
supr java.lang.Object

CLSS public final org.eclipse.core.runtime.Platform
fld public final static int FAILED_DELETE_METADATA = 6
fld public final static int FAILED_READ_METADATA = 4
fld public final static int FAILED_WRITE_METADATA = 5
fld public final static int INTERNAL_ERROR = 3
fld public final static int MAX_PERFORMANCE = 5
fld public final static int MIN_PERFORMANCE = 1
fld public final static int PARSE_PROBLEM = 1
fld public final static int PLUGIN_ERROR = 2
fld public final static java.lang.String ARCH_AMD64 = "x86_64"
fld public final static java.lang.String ARCH_IA64 = "ia64"
fld public final static java.lang.String ARCH_IA64_32 = "ia64_32"
fld public final static java.lang.String ARCH_PA_RISC = "PA_RISC"
fld public final static java.lang.String ARCH_PPC = "ppc"
fld public final static java.lang.String ARCH_SPARC = "sparc"
fld public final static java.lang.String ARCH_X86 = "x86"
fld public final static java.lang.String ARCH_X86_64 = "x86_64"
fld public final static java.lang.String OPTION_STARTTIME = "org.eclipse.core.runtime/starttime"
fld public final static java.lang.String OS_AIX = "aix"
fld public final static java.lang.String OS_HPUX = "hpux"
fld public final static java.lang.String OS_LINUX = "linux"
fld public final static java.lang.String OS_MACOSX = "macosx"
fld public final static java.lang.String OS_QNX = "qnx"
fld public final static java.lang.String OS_SOLARIS = "solaris"
fld public final static java.lang.String OS_UNKNOWN = "unknown"
fld public final static java.lang.String OS_WIN32 = "win32"
fld public final static java.lang.String PI_RUNTIME = "org.eclipse.core.runtime"
fld public final static java.lang.String PREF_LINE_SEPARATOR = "line.separator"
fld public final static java.lang.String PREF_PLATFORM_PERFORMANCE = "runtime.performance"
fld public final static java.lang.String PT_ADAPTERS = "adapters"
fld public final static java.lang.String PT_APPLICATIONS = "applications"
fld public final static java.lang.String PT_PREFERENCES = "preferences"
fld public final static java.lang.String PT_PRODUCT = "products"
fld public final static java.lang.String WS_CARBON = "carbon"
fld public final static java.lang.String WS_COCOA = "cocoa"
fld public final static java.lang.String WS_GTK = "gtk"
fld public final static java.lang.String WS_MOTIF = "motif"
fld public final static java.lang.String WS_PHOTON = "photon"
fld public final static java.lang.String WS_UNKNOWN = "unknown"
fld public final static java.lang.String WS_WIN32 = "win32"
fld public final static java.lang.String WS_WPF = "wpf"
meth public static boolean inDebugMode()
meth public static boolean inDevelopmentMode()
meth public static boolean isFragment(org.osgi.framework.Bundle)
meth public static boolean isRunning()
meth public static java.lang.String getDebugOption(java.lang.String)
meth public static java.lang.String getNL()
meth public static java.lang.String getNLExtensions()
meth public static java.lang.String getOS()
meth public static java.lang.String getOSArch()
meth public static java.lang.String getProtectionSpace(java.net.URL)
meth public static java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String)
meth public static java.lang.String getResourceString(org.osgi.framework.Bundle,java.lang.String,java.util.ResourceBundle)
meth public static java.lang.String getWS()
meth public static java.lang.String[] getApplicationArgs()
meth public static java.lang.String[] getCommandLineArgs()
meth public static java.lang.String[] knownOSArchValues()
meth public static java.lang.String[] knownOSValues()
meth public static java.lang.String[] knownWSValues()
meth public static java.net.URL asLocalURL(java.net.URL) throws java.io.IOException
meth public static java.net.URL find(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath)
meth public static java.net.URL find(org.osgi.framework.Bundle,org.eclipse.core.runtime.IPath,java.util.Map)
meth public static java.net.URL resolve(java.net.URL) throws java.io.IOException
meth public static java.util.Map getAuthorizationInfo(java.net.URL,java.lang.String,java.lang.String)
meth public static java.util.Map knownPlatformLineSeparators()
meth public static java.util.ResourceBundle getResourceBundle(org.osgi.framework.Bundle)
meth public static long getStateStamp()
meth public static org.eclipse.core.runtime.IAdapterManager getAdapterManager()
meth public static org.eclipse.core.runtime.IBundleGroupProvider[] getBundleGroupProviders()
meth public static org.eclipse.core.runtime.IExtensionRegistry getExtensionRegistry()
meth public static org.eclipse.core.runtime.ILog getLog(org.osgi.framework.Bundle)
meth public static org.eclipse.core.runtime.IPath getLocation()
meth public static org.eclipse.core.runtime.IPath getLogFileLocation()
meth public static org.eclipse.core.runtime.IPath getPluginStateLocation(org.eclipse.core.runtime.Plugin)
meth public static org.eclipse.core.runtime.IPath getStateLocation(org.osgi.framework.Bundle)
meth public static org.eclipse.core.runtime.IPluginRegistry getPluginRegistry()
meth public static org.eclipse.core.runtime.IProduct getProduct()
meth public static org.eclipse.core.runtime.Plugin getPlugin(java.lang.String)
meth public static org.eclipse.core.runtime.content.IContentTypeManager getContentTypeManager()
meth public static org.eclipse.core.runtime.jobs.IJobManager getJobManager()
meth public static org.eclipse.core.runtime.preferences.IPreferencesService getPreferencesService()
meth public static org.eclipse.osgi.service.datalocation.Location getConfigurationLocation()
meth public static org.eclipse.osgi.service.datalocation.Location getInstallLocation()
meth public static org.eclipse.osgi.service.datalocation.Location getInstanceLocation()
meth public static org.eclipse.osgi.service.datalocation.Location getUserLocation()
meth public static org.eclipse.osgi.service.resolver.PlatformAdmin getPlatformAdmin()
meth public static org.osgi.framework.Bundle getBundle(java.lang.String)
meth public static org.osgi.framework.Bundle[] getBundles(java.lang.String,java.lang.String)
meth public static org.osgi.framework.Bundle[] getFragments(org.osgi.framework.Bundle)
meth public static org.osgi.framework.Bundle[] getHosts(org.osgi.framework.Bundle)
meth public static void addAuthorizationInfo(java.net.URL,java.lang.String,java.lang.String,java.util.Map) throws org.eclipse.core.runtime.CoreException
meth public static void addLogListener(org.eclipse.core.runtime.ILogListener)
meth public static void addProtectionSpace(java.net.URL,java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public static void endSplash()
meth public static void flushAuthorizationInfo(java.net.URL,java.lang.String,java.lang.String) throws org.eclipse.core.runtime.CoreException
meth public static void registerBundleGroupProvider(org.eclipse.core.runtime.IBundleGroupProvider)
meth public static void removeLogListener(org.eclipse.core.runtime.ILogListener)
meth public static void run(org.eclipse.core.runtime.ISafeRunnable)
meth public static void unregisterBundleGroupProvider(org.eclipse.core.runtime.IBundleGroupProvider)
supr java.lang.Object
hfds LINE_SEPARATOR_KEY_MAC_OS_9,LINE_SEPARATOR_KEY_UNIX,LINE_SEPARATOR_KEY_WINDOWS,LINE_SEPARATOR_VALUE_CR,LINE_SEPARATOR_VALUE_CRLF,LINE_SEPARATOR_VALUE_LF,authNotAvailableLogged

CLSS public abstract org.eclipse.core.runtime.PlatformObject
cons public init()
intf org.eclipse.core.runtime.IAdaptable
meth public java.lang.Object getAdapter(java.lang.Class)
supr java.lang.Object

CLSS public abstract org.eclipse.core.runtime.Plugin
cons public init()
cons public init(org.eclipse.core.runtime.IPluginDescriptor)
fld public final static java.lang.String PLUGIN_PREFERENCE_SCOPE = "instance"
fld public final static java.lang.String PREFERENCES_DEFAULT_OVERRIDE_BASE_NAME = "preferences"
fld public final static java.lang.String PREFERENCES_DEFAULT_OVERRIDE_FILE_NAME = "preferences.ini"
intf org.osgi.framework.BundleActivator
meth protected void initializeDefaultPluginPreferences()
meth public boolean isDebugging()
meth public final java.io.InputStream openStream(org.eclipse.core.runtime.IPath) throws java.io.IOException
meth public final java.io.InputStream openStream(org.eclipse.core.runtime.IPath,boolean) throws java.io.IOException
meth public final java.net.URL find(org.eclipse.core.runtime.IPath)
meth public final java.net.URL find(org.eclipse.core.runtime.IPath,java.util.Map)
meth public final org.eclipse.core.runtime.ILog getLog()
meth public final org.eclipse.core.runtime.IPath getStateLocation()
meth public final org.eclipse.core.runtime.IPluginDescriptor getDescriptor()
meth public final org.eclipse.core.runtime.Preferences getPluginPreferences()
meth public final org.osgi.framework.Bundle getBundle()
meth public final void internalInitializeDefaultPluginPreferences()
meth public final void savePluginPreferences()
meth public java.lang.String toString()
meth public void setDebugging(boolean)
meth public void shutdown() throws org.eclipse.core.runtime.CoreException
meth public void start(org.osgi.framework.BundleContext) throws java.lang.Exception
meth public void startup() throws org.eclipse.core.runtime.CoreException
meth public void stop(org.osgi.framework.BundleContext) throws java.lang.Exception
supr java.lang.Object
hfds bundle,class$0,debug,debugTracker,descriptor,preferences

CLSS public final org.eclipse.core.runtime.PluginVersionIdentifier
cons public init(int,int,int)
cons public init(int,int,int,java.lang.String)
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean isCompatibleWith(org.eclipse.core.runtime.PluginVersionIdentifier)
meth public boolean isEquivalentTo(org.eclipse.core.runtime.PluginVersionIdentifier)
meth public boolean isGreaterOrEqualTo(org.eclipse.core.runtime.PluginVersionIdentifier)
meth public boolean isGreaterThan(org.eclipse.core.runtime.PluginVersionIdentifier)
meth public boolean isPerfect(org.eclipse.core.runtime.PluginVersionIdentifier)
meth public int getMajorComponent()
meth public int getMinorComponent()
meth public int getServiceComponent()
meth public int hashCode()
meth public java.lang.String getQualifierComponent()
meth public java.lang.String toString()
meth public static org.eclipse.core.runtime.IStatus validateVersion(java.lang.String)
supr java.lang.Object
hfds SEPARATOR,version

CLSS public org.eclipse.core.runtime.Preferences
cons public init()
fld protected boolean dirty
fld protected final static java.lang.String FALSE = "false"
fld protected final static java.lang.String TRUE = "true"
fld protected org.eclipse.core.runtime.ListenerList listeners
fld public final static boolean BOOLEAN_DEFAULT_DEFAULT = false
fld public final static double DOUBLE_DEFAULT_DEFAULT = 0.0
fld public final static float FLOAT_DEFAULT_DEFAULT = 0.0
fld public final static int INT_DEFAULT_DEFAULT = 0
fld public final static java.lang.String PT_PREFERENCES = "preferences"
fld public final static java.lang.String STRING_DEFAULT_DEFAULT = ""
fld public final static long LONG_DEFAULT_DEFAULT = 0
innr public abstract interface static IPropertyChangeListener
innr public static PropertyChangeEvent
meth protected void firePropertyChangeEvent(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean contains(java.lang.String)
meth public boolean getBoolean(java.lang.String)
meth public boolean getDefaultBoolean(java.lang.String)
meth public boolean isDefault(java.lang.String)
meth public boolean needsSaving()
meth public double getDefaultDouble(java.lang.String)
meth public double getDouble(java.lang.String)
meth public float getDefaultFloat(java.lang.String)
meth public float getFloat(java.lang.String)
meth public int getDefaultInt(java.lang.String)
meth public int getInt(java.lang.String)
meth public java.lang.String getDefaultString(java.lang.String)
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String[] defaultPropertyNames()
meth public java.lang.String[] propertyNames()
meth public long getDefaultLong(java.lang.String)
meth public long getLong(java.lang.String)
meth public static org.eclipse.core.runtime.IStatus validatePreferenceVersions(org.eclipse.core.runtime.IPath)
meth public static void exportPreferences(org.eclipse.core.runtime.IPath) throws org.eclipse.core.runtime.CoreException
meth public static void importPreferences(org.eclipse.core.runtime.IPath) throws org.eclipse.core.runtime.CoreException
meth public void addPropertyChangeListener(org.eclipse.core.runtime.Preferences$IPropertyChangeListener)
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void removePropertyChangeListener(org.eclipse.core.runtime.Preferences$IPropertyChangeListener)
meth public void setDefault(java.lang.String,boolean)
meth public void setDefault(java.lang.String,double)
meth public void setDefault(java.lang.String,float)
meth public void setDefault(java.lang.String,int)
meth public void setDefault(java.lang.String,java.lang.String)
meth public void setDefault(java.lang.String,long)
meth public void setToDefault(java.lang.String)
meth public void setValue(java.lang.String,boolean)
meth public void setValue(java.lang.String,double)
meth public void setValue(java.lang.String,float)
meth public void setValue(java.lang.String,int)
meth public void setValue(java.lang.String,java.lang.String)
meth public void setValue(java.lang.String,long)
meth public void store(java.io.OutputStream,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds EMPTY_STRING_ARRAY,defaultProperties,properties

CLSS public abstract interface static org.eclipse.core.runtime.Preferences$IPropertyChangeListener
 outer org.eclipse.core.runtime.Preferences
intf java.util.EventListener
meth public abstract void propertyChange(org.eclipse.core.runtime.Preferences$PropertyChangeEvent)

CLSS public static org.eclipse.core.runtime.Preferences$PropertyChangeEvent
 outer org.eclipse.core.runtime.Preferences
cons protected init(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public java.lang.Object getNewValue()
meth public java.lang.Object getOldValue()
meth public java.lang.String getProperty()
supr java.util.EventObject
hfds newValue,oldValue,propertyName,serialVersionUID

CLSS public abstract org.eclipse.core.runtime.ProgressMonitorWrapper
cons protected init(org.eclipse.core.runtime.IProgressMonitor)
intf org.eclipse.core.runtime.IProgressMonitor
intf org.eclipse.core.runtime.IProgressMonitorWithBlocking
meth public boolean isCanceled()
meth public org.eclipse.core.runtime.IProgressMonitor getWrappedProgressMonitor()
meth public void beginTask(java.lang.String,int)
meth public void clearBlocked()
meth public void done()
meth public void internalWorked(double)
meth public void setBlocked(org.eclipse.core.runtime.IStatus)
meth public void setCanceled(boolean)
meth public void setTaskName(java.lang.String)
meth public void subTask(java.lang.String)
meth public void worked(int)
supr java.lang.Object
hfds progressMonitor

CLSS public final org.eclipse.core.runtime.QualifiedName
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getLocalName()
meth public java.lang.String getQualifier()
meth public java.lang.String toString()
supr java.lang.Object
hfds localName,qualifier

CLSS public final org.eclipse.core.runtime.RegistryFactory
cons public init()
meth public static org.eclipse.core.runtime.IExtensionRegistry createRegistry(org.eclipse.core.runtime.spi.RegistryStrategy,java.lang.Object,java.lang.Object)
meth public static org.eclipse.core.runtime.IExtensionRegistry getRegistry()
meth public static org.eclipse.core.runtime.spi.RegistryStrategy createOSGiStrategy(java.io.File[],boolean[],java.lang.Object)
meth public static void setDefaultRegistryProvider(org.eclipse.core.runtime.spi.IRegistryProvider) throws org.eclipse.core.runtime.CoreException
supr java.lang.Object

CLSS public final org.eclipse.core.runtime.SafeRunner
cons public init()
meth public static void run(org.eclipse.core.runtime.ISafeRunnable)
supr java.lang.Object

CLSS public org.eclipse.core.runtime.Status
cons public init(int,java.lang.String,int,java.lang.String,java.lang.Throwable)
cons public init(int,java.lang.String,java.lang.String)
cons public init(int,java.lang.String,java.lang.String,java.lang.Throwable)
fld public final static org.eclipse.core.runtime.IStatus CANCEL_STATUS
fld public final static org.eclipse.core.runtime.IStatus OK_STATUS
intf org.eclipse.core.runtime.IStatus
meth protected void setCode(int)
meth protected void setException(java.lang.Throwable)
meth protected void setMessage(java.lang.String)
meth protected void setPlugin(java.lang.String)
meth protected void setSeverity(int)
meth public boolean isMultiStatus()
meth public boolean isOK()
meth public boolean matches(int)
meth public int getCode()
meth public int getSeverity()
meth public java.lang.String getMessage()
meth public java.lang.String getPlugin()
meth public java.lang.String toString()
meth public java.lang.Throwable getException()
meth public org.eclipse.core.runtime.IStatus[] getChildren()
supr java.lang.Object
hfds code,exception,message,pluginId,severity,theEmptyStatusArray,unknownId

CLSS public final org.eclipse.core.runtime.SubMonitor
fld public final static int SUPPRESS_ALL_LABELS = 7
fld public final static int SUPPRESS_BEGINTASK = 2
fld public final static int SUPPRESS_NONE = 0
fld public final static int SUPPRESS_SETTASKNAME = 4
fld public final static int SUPPRESS_SUBTASK = 1
intf org.eclipse.core.runtime.IProgressMonitorWithBlocking
meth protected static boolean eq(java.lang.Object,java.lang.Object)
meth public boolean isCanceled()
meth public org.eclipse.core.runtime.SubMonitor newChild(int)
meth public org.eclipse.core.runtime.SubMonitor newChild(int,int)
meth public org.eclipse.core.runtime.SubMonitor setWorkRemaining(int)
meth public static org.eclipse.core.runtime.SubMonitor convert(org.eclipse.core.runtime.IProgressMonitor)
meth public static org.eclipse.core.runtime.SubMonitor convert(org.eclipse.core.runtime.IProgressMonitor,int)
meth public static org.eclipse.core.runtime.SubMonitor convert(org.eclipse.core.runtime.IProgressMonitor,java.lang.String,int)
meth public void beginTask(java.lang.String,int)
meth public void clearBlocked()
meth public void done()
meth public void internalWorked(double)
meth public void setBlocked(org.eclipse.core.runtime.IStatus)
meth public void setCanceled(boolean)
meth public void setTaskName(java.lang.String)
meth public void subTask(java.lang.String)
meth public void worked(int)
supr java.lang.Object
hfds MINIMUM_RESOLUTION,flags,lastSubMonitor,root,totalForChildren,totalParent,usedForChildren,usedForParent
hcls RootInfo

CLSS public org.eclipse.core.runtime.SubProgressMonitor
cons public init(org.eclipse.core.runtime.IProgressMonitor,int)
cons public init(org.eclipse.core.runtime.IProgressMonitor,int,int)
fld public final static int PREPEND_MAIN_LABEL_TO_SUBTASK = 4
fld public final static int SUPPRESS_SUBTASK_LABEL = 2
meth public void beginTask(java.lang.String,int)
meth public void done()
meth public void internalWorked(double)
meth public void subTask(java.lang.String)
meth public void worked(int)
supr org.eclipse.core.runtime.ProgressMonitorWrapper
hfds hasSubTask,mainTaskLabel,nestedBeginTasks,parentTicks,scale,sentToParent,style,usedUp

CLSS public final org.eclipse.core.runtime.URIUtil
meth public static boolean isFileURI(java.net.URI)
meth public static boolean sameURI(java.net.URI,java.net.URI)
meth public static java.io.File toFile(java.net.URI)
meth public static java.lang.String lastSegment(java.net.URI)
meth public static java.lang.String toUnencodedString(java.net.URI)
meth public static java.net.URI append(java.net.URI,java.lang.String)
meth public static java.net.URI fromString(java.lang.String) throws java.net.URISyntaxException
meth public static java.net.URI makeAbsolute(java.net.URI,java.net.URI)
meth public static java.net.URI makeRelative(java.net.URI,java.net.URI)
meth public static java.net.URI removeFileExtension(java.net.URI)
meth public static java.net.URI toJarURI(java.net.URI,org.eclipse.core.runtime.IPath)
meth public static java.net.URI toURI(java.net.URL) throws java.net.URISyntaxException
meth public static java.net.URL toURL(java.net.URI) throws java.net.MalformedURLException
supr java.lang.Object
hfds JAR_SUFFIX,SCHEME_FILE,SCHEME_JAR,UNC_PREFIX,decodeResolved

CLSS public abstract org.eclipse.core.runtime.jobs.Job
cons public init(java.lang.String)
fld public final static int BUILD = 40
fld public final static int DECORATE = 50
fld public final static int INTERACTIVE = 10
fld public final static int LONG = 30
fld public final static int NONE = 0
fld public final static int RUNNING = 4
fld public final static int SHORT = 20
fld public final static int SLEEPING = 1
fld public final static int WAITING = 2
fld public final static org.eclipse.core.runtime.IStatus ASYNC_FINISH
intf org.eclipse.core.runtime.IAdaptable
meth protected abstract org.eclipse.core.runtime.IStatus run(org.eclipse.core.runtime.IProgressMonitor)
meth protected void canceling()
meth public boolean belongsTo(java.lang.Object)
meth public boolean shouldRun()
meth public boolean shouldSchedule()
meth public final boolean cancel()
meth public final boolean isBlocking()
meth public final boolean isSystem()
meth public final boolean isUser()
meth public final boolean sleep()
meth public final int getPriority()
meth public final int getState()
meth public final java.lang.Object getProperty(org.eclipse.core.runtime.QualifiedName)
meth public final java.lang.String getName()
meth public final java.lang.Thread getThread()
meth public final org.eclipse.core.runtime.IStatus getResult()
meth public final org.eclipse.core.runtime.jobs.ISchedulingRule getRule()
meth public final static org.eclipse.core.runtime.jobs.IJobManager getJobManager()
meth public final void addJobChangeListener(org.eclipse.core.runtime.jobs.IJobChangeListener)
meth public final void done(org.eclipse.core.runtime.IStatus)
meth public final void join() throws java.lang.InterruptedException
meth public final void removeJobChangeListener(org.eclipse.core.runtime.jobs.IJobChangeListener)
meth public final void schedule()
meth public final void schedule(long)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void setProgressGroup(org.eclipse.core.runtime.IProgressMonitor,int)
meth public final void setRule(org.eclipse.core.runtime.jobs.ISchedulingRule)
meth public final void setSystem(boolean)
meth public final void setThread(java.lang.Thread)
meth public final void setUser(boolean)
meth public final void wakeUp()
meth public final void wakeUp(long)
meth public java.lang.String toString()
meth public org.eclipse.core.runtime.jobs.Job yieldRule(org.eclipse.core.runtime.IProgressMonitor)
meth public void setProperty(org.eclipse.core.runtime.QualifiedName,java.lang.Object)
supr org.eclipse.core.internal.jobs.InternalJob

CLSS public abstract interface org.eclipse.core.runtime.preferences.IEclipsePreferences
innr public abstract interface static INodeChangeListener
innr public abstract interface static IPreferenceChangeListener
innr public final static NodeChangeEvent
innr public final static PreferenceChangeEvent
intf org.osgi.service.prefs.Preferences
meth public abstract org.osgi.service.prefs.Preferences node(java.lang.String)
meth public abstract void accept(org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor) throws org.osgi.service.prefs.BackingStoreException
meth public abstract void addNodeChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences$INodeChangeListener)
meth public abstract void addPreferenceChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences$IPreferenceChangeListener)
meth public abstract void removeNode() throws org.osgi.service.prefs.BackingStoreException
meth public abstract void removeNodeChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences$INodeChangeListener)
meth public abstract void removePreferenceChangeListener(org.eclipse.core.runtime.preferences.IEclipsePreferences$IPreferenceChangeListener)

CLSS public abstract interface static org.eclipse.core.runtime.preferences.IEclipsePreferences$INodeChangeListener
 outer org.eclipse.core.runtime.preferences.IEclipsePreferences
meth public abstract void added(org.eclipse.core.runtime.preferences.IEclipsePreferences$NodeChangeEvent)
meth public abstract void removed(org.eclipse.core.runtime.preferences.IEclipsePreferences$NodeChangeEvent)

CLSS public abstract interface static org.eclipse.core.runtime.preferences.IEclipsePreferences$IPreferenceChangeListener
 outer org.eclipse.core.runtime.preferences.IEclipsePreferences
meth public abstract void preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences$PreferenceChangeEvent)

CLSS public abstract interface org.eclipse.equinox.log.LogFilter
meth public abstract boolean isLoggable(org.osgi.framework.Bundle,java.lang.String,int)

CLSS public abstract interface org.eclipse.equinox.log.SynchronousLogListener
intf org.osgi.service.log.LogListener

CLSS public abstract interface org.eclipse.osgi.service.urlconversion.URLConverter
meth public abstract java.net.URL resolve(java.net.URL) throws java.io.IOException
meth public abstract java.net.URL toFileURL(java.net.URL) throws java.io.IOException

CLSS public abstract org.eclipse.osgi.util.NLS
cons protected init()
meth public static java.lang.String bind(java.lang.String,java.lang.Object)
meth public static java.lang.String bind(java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String bind(java.lang.String,java.lang.Object[])
meth public static void initializeMessages(java.lang.String,java.lang.Class<?>)
supr java.lang.Object
hfds ASSIGNED,EMPTY_ARGS,EXTENSION,IGNORE,PROP_WARNINGS,SEVERITY_ERROR,SEVERITY_WARNING,frameworkLog,ignoreWarnings,nlSuffixes
hcls MessagesProperties

CLSS public abstract interface org.osgi.framework.BundleActivator
meth public abstract void start(org.osgi.framework.BundleContext) throws java.lang.Exception
meth public abstract void stop(org.osgi.framework.BundleContext) throws java.lang.Exception

CLSS public abstract interface org.osgi.service.log.LogListener
intf java.util.EventListener
meth public abstract void logged(org.osgi.service.log.LogEntry)

CLSS public abstract interface org.osgi.service.prefs.Preferences
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract boolean nodeExists(java.lang.String) throws org.osgi.service.prefs.BackingStoreException
meth public abstract byte[] getByteArray(java.lang.String,byte[])
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.String absolutePath()
meth public abstract java.lang.String get(java.lang.String,java.lang.String)
meth public abstract java.lang.String name()
meth public abstract java.lang.String[] childrenNames() throws org.osgi.service.prefs.BackingStoreException
meth public abstract java.lang.String[] keys() throws org.osgi.service.prefs.BackingStoreException
meth public abstract long getLong(java.lang.String,long)
meth public abstract org.osgi.service.prefs.Preferences node(java.lang.String)
meth public abstract org.osgi.service.prefs.Preferences parent()
meth public abstract void clear() throws org.osgi.service.prefs.BackingStoreException
meth public abstract void flush() throws org.osgi.service.prefs.BackingStoreException
meth public abstract void put(java.lang.String,java.lang.String)
meth public abstract void putBoolean(java.lang.String,boolean)
meth public abstract void putByteArray(java.lang.String,byte[])
meth public abstract void putDouble(java.lang.String,double)
meth public abstract void putFloat(java.lang.String,float)
meth public abstract void putInt(java.lang.String,int)
meth public abstract void putLong(java.lang.String,long)
meth public abstract void remove(java.lang.String)
meth public abstract void removeNode() throws org.osgi.service.prefs.BackingStoreException
meth public abstract void sync() throws org.osgi.service.prefs.BackingStoreException

