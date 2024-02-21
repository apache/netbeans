#Signature file v4.1
#Version 1.86.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract java.net.URLStreamHandler
cons public init()
meth protected abstract java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
meth protected boolean equals(java.net.URL,java.net.URL)
meth protected boolean hostsEqual(java.net.URL,java.net.URL)
meth protected boolean sameFile(java.net.URL,java.net.URL)
meth protected int getDefaultPort()
meth protected int hashCode(java.net.URL)
meth protected java.lang.String toExternalForm(java.net.URL)
meth protected java.net.InetAddress getHostAddress(java.net.URL)
meth protected java.net.URLConnection openConnection(java.net.URL,java.net.Proxy) throws java.io.IOException
meth protected void parseURL(java.net.URL,java.lang.String,int,int)
meth protected void setURL(java.net.URL,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected void setURL(java.net.URL,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public abstract org.netbeans.CLIHandler
cons protected init(int)
fld public final static int WHEN_BOOT = 1
fld public final static int WHEN_EXTRA = 3
fld public final static int WHEN_INIT = 2
innr public final static Args
meth protected abstract int cli(org.netbeans.CLIHandler$Args)
meth protected abstract void usage(java.io.PrintWriter)
meth protected static int notifyHandlers(org.netbeans.CLIHandler$Args,java.util.Collection<? extends org.netbeans.CLIHandler>,int,boolean,boolean)
meth protected static void showHelp(java.io.PrintWriter,java.util.Collection<? extends org.netbeans.CLIHandler>,int)
meth public static void stopServer()
supr java.lang.Object
hfds KEY_LENGTH,OUTPUT,REPLY_AVAILABLE,REPLY_DELAY,REPLY_ERROR,REPLY_EXIT,REPLY_FAIL,REPLY_OK,REPLY_READ,REPLY_VERSION,REPLY_WRITE,VERSION,doLater,secureCLIPort,server,when
hcls Execute,FileAndLock,Server,Status

CLSS public final org.netbeans.Stamps
innr public abstract interface static Updater
meth public boolean exists(java.lang.String)
meth public java.io.InputStream asStream(java.lang.String)
meth public java.nio.ByteBuffer asByteBuffer(java.lang.String)
meth public java.nio.MappedByteBuffer asMappedByteBuffer(java.lang.String)
meth public long lastModified()
meth public static org.netbeans.Stamps getModulesJARs()
meth public void discardCaches()
meth public void flush(int)
meth public void scheduleSave(org.netbeans.Stamps$Updater,java.lang.String,boolean)
meth public void shutdown()
supr java.lang.Object
hfds LOG,MODULES_JARS,clustersChanged,fallbackCache,moduleJARs,moduleNewestFile,populated,worker
hcls Store,Worker

CLSS public abstract interface static org.netbeans.Stamps$Updater
 outer org.netbeans.Stamps
meth public abstract void cacheReady()
meth public abstract void flushCaches(java.io.DataOutputStream) throws java.io.IOException

CLSS public final org.netbeans.core.startup.AutomaticDependencies
innr public final static Report
meth public java.lang.String refineDependenciesSimple(java.lang.String,java.util.Set<java.lang.String>)
meth public java.lang.String toString()
meth public org.netbeans.core.startup.AutomaticDependencies$Report refineDependenciesAndReport(java.lang.String,java.util.Set<org.openide.modules.Dependency>)
meth public static org.netbeans.core.startup.AutomaticDependencies empty()
meth public static org.netbeans.core.startup.AutomaticDependencies getDefault()
meth public static org.netbeans.core.startup.AutomaticDependencies parse(java.net.URL[]) throws java.io.IOException,org.xml.sax.SAXException
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public void refineDependencies(java.lang.String,java.util.Set<org.openide.modules.Dependency>)
supr java.lang.Object
hfds INSTANCE,LOG,groups
hcls Dep,Exclusion,Handler,ModuleDep,PackageDep,Parser,TokenDep,Transformation,TransformationGroup

CLSS public final static org.netbeans.core.startup.AutomaticDependencies$Report
 outer org.netbeans.core.startup.AutomaticDependencies
meth public boolean isModified()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getMessages()
meth public java.util.Set<org.openide.modules.Dependency> getAdded()
meth public java.util.Set<org.openide.modules.Dependency> getRemoved()
supr java.lang.Object
hfds added,cnb,messages,removed

CLSS public org.netbeans.core.startup.CLICoreBridge
cons public init()
meth protected int cli(org.netbeans.CLIHandler$Args)
meth protected void usage(java.io.PrintWriter)
supr org.netbeans.CLIHandler

CLSS public org.netbeans.core.startup.CLIOptions
cons public init()
fld protected static boolean noLogging
fld protected static java.lang.Class uiClass
meth protected int cli(org.netbeans.CLIHandler$Args)
meth protected static java.lang.String getSystemDir()
meth protected void usage(java.io.PrintWriter)
meth public final int cli(java.lang.String[])
meth public static boolean isGui()
meth public static int getFontSize()
meth public static java.io.File getCacheDir()
meth public static java.lang.String getHomeDir()
meth public static java.lang.String getLogDir()
meth public static java.lang.String getUserDir()
meth public static void initialize()
supr org.netbeans.CLIHandler
hfds DIR_MODULES,cacheDir,defaultsLoaded,fallbackToMemory,gui,homeDir,noSplash,systemDir,uiClassName,uiFontSize,userDir

CLSS public final org.netbeans.core.startup.CLITestModuleReload
cons public init()
meth protected int cli(org.netbeans.CLIHandler$Args)
meth protected void usage(java.io.PrintWriter)
supr org.netbeans.CLIHandler

CLSS public org.netbeans.core.startup.ConsistencyVerifier
meth public static java.util.SortedMap<java.lang.String,java.util.SortedSet<java.lang.String>> findInconsistencies(java.util.Set<java.util.jar.Manifest>)
meth public static java.util.SortedMap<java.lang.String,java.util.SortedSet<java.lang.String>> findInconsistencies(java.util.Set<java.util.jar.Manifest>,java.util.Set<java.lang.String>)
supr java.lang.Object
hcls DummyEvents,DummyInstaller

CLSS public abstract org.netbeans.core.startup.CoreBridge
cons public init()
meth protected abstract void attachToCategory(java.lang.Object)
meth protected abstract void loadActionSection(org.netbeans.core.startup.ManifestSection$ActionSection,boolean) throws java.lang.Exception
meth protected abstract void loadDefaultSection(org.netbeans.core.startup.ManifestSection,org.openide.util.lookup.InstanceContent$Convertor<org.netbeans.core.startup.ManifestSection,java.lang.Object>,boolean)
meth protected abstract void loadLoaderSection(org.netbeans.core.startup.ManifestSection$LoaderSection,boolean) throws java.lang.Exception
meth protected abstract void loaderPoolTransaction(boolean)
meth public abstract int cli(java.lang.String[],java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File)
meth public abstract org.openide.util.Lookup lookupCacheLoad()
meth public abstract void initializePlaf(java.lang.Class,int,java.net.URL)
meth public abstract void registerPropertyEditors()
meth public abstract void setStatusText(java.lang.String)
meth public static org.netbeans.core.startup.CoreBridge getDefault()
meth public static void defineOsTokens(java.util.Collection<? super java.lang.String>)
supr java.lang.Object
hcls FakeBridge

CLSS public final org.netbeans.core.startup.InstalledFileLocatorImpl
cons public init()
meth public java.io.File locate(java.lang.String,java.lang.String,boolean)
meth public java.util.Set<java.io.File> locateAll(java.lang.String,java.lang.String,boolean)
meth public static void discardCache()
meth public static void prepareCache()
supr org.openide.modules.InstalledFileLocator
hfds FILE_PATTERN,LOG,cacheMiss,clusterCache,dirs,fileCache,ownershipByModuleByCluster

CLSS public final org.netbeans.core.startup.Main
cons public init()
meth public static boolean isInitialized()
meth public static org.netbeans.core.startup.ModuleSystem getModuleSystem()
meth public static org.netbeans.core.startup.ModuleSystem getModuleSystem(boolean)
meth public static void initUICustomizations()
meth public static void initializeURLFactory()
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public static void setStatusText(java.lang.String)
supr java.lang.Object
hfds LOG,moduleSystem,moduleSystemInitialized

CLSS public final org.netbeans.core.startup.MainLookup
cons public init()
meth protected void beforeLookup(org.openide.util.Lookup$Template)
meth public final static void moduleClassLoadersUp()
meth public final static void moduleLookupReady(org.openide.util.Lookup)
meth public final static void modulesClassPathInitialized()
meth public final static void systemClassLoaderChanged(java.lang.ClassLoader)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void register({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> void unregister({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public static org.openide.util.Task warmUp(long)
meth public static void register(java.lang.Object)
meth public static void started()
meth public static void unregister(java.lang.Object)
supr org.openide.util.lookup.ProxyLookup
hfds changing,classLoader,instanceContent,instanceLookup,started

CLSS public abstract org.netbeans.core.startup.ManifestSection<%0 extends java.lang.Object>
cons protected init(java.lang.String,org.netbeans.Module,java.lang.Object) throws org.netbeans.InvalidException
innr public final static ActionSection
innr public final static ClipboardConvertorSection
innr public final static DebuggerSection
innr public final static LoaderSection
meth protected final java.lang.ClassLoader getClassLoader()
meth protected final java.lang.Object createInstance() throws java.lang.Exception
meth public final boolean isDefaultInstance()
meth public final java.lang.Class<?> getSectionClass() throws java.lang.Exception
meth public final java.lang.Class<?> getSuperclass()
meth public final java.lang.Object getInstance() throws java.lang.Exception
meth public final org.netbeans.Module getModule()
meth public java.lang.String getSectionClassName() throws java.lang.Exception
meth public java.lang.String toString()
meth public static org.netbeans.core.startup.ManifestSection create(java.lang.String,java.util.jar.Attributes,org.netbeans.Module) throws org.netbeans.InvalidException
meth public void dispose()
supr java.lang.Object
hfds className,clazz,module,name,problem,result,superclazz

CLSS public final static org.netbeans.core.startup.ManifestSection$ActionSection
 outer org.netbeans.core.startup.ManifestSection
supr org.netbeans.core.startup.ManifestSection

CLSS public final static org.netbeans.core.startup.ManifestSection$ClipboardConvertorSection
 outer org.netbeans.core.startup.ManifestSection
 anno 0 java.lang.Deprecated()
supr org.netbeans.core.startup.ManifestSection

CLSS public final static org.netbeans.core.startup.ManifestSection$DebuggerSection
 outer org.netbeans.core.startup.ManifestSection
 anno 0 java.lang.Deprecated()
supr org.netbeans.core.startup.ManifestSection

CLSS public final static org.netbeans.core.startup.ManifestSection$LoaderSection
 outer org.netbeans.core.startup.ManifestSection
meth public java.lang.String[] getInstallAfter()
meth public java.lang.String[] getInstallBefore()
supr org.netbeans.core.startup.ManifestSection
hfds installAfter,installBefore

CLSS public final org.netbeans.core.startup.MavenRepoURLHandler
cons public init()
meth protected java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
supr java.net.URLStreamHandler
hfds CENTRAL_REPO_URI

CLSS public final org.netbeans.core.startup.ModuleHistory
cons public init(java.lang.String)
meth public java.lang.String toString()
supr java.lang.Object
hfds info,jar

CLSS public org.netbeans.core.startup.ModuleLifecycleManager
cons public init()
meth public void exit()
meth public void exit(int)
meth public void markForRestart()
meth public void saveAll()
supr org.openide.LifecycleManager
hfds exiting

CLSS public final org.netbeans.core.startup.ModuleSystem
cons public init(org.openide.filesystems.FileSystem) throws java.io.IOException
meth public boolean isShowInAutoUpdateClient(org.openide.modules.ModuleInfo)
meth public boolean shutDown(java.lang.Runnable)
meth public final void refresh()
meth public java.lang.String getEffectiveClasspath(org.netbeans.Module)
meth public java.util.List<java.io.File> getModuleJars()
meth public java.util.concurrent.Future<java.lang.Boolean> shutDownAsync(java.lang.Runnable)
meth public org.netbeans.Events getEvents()
meth public org.netbeans.ModuleManager getManager()
meth public static void markForRestart()
meth public void loadBootModules()
meth public void readList()
meth public void restore()
supr java.lang.Object
hfds LOG,bootModules,ev,installer,list,mgr
hcls QuietEvents

CLSS public final org.netbeans.core.startup.NbPlaces
cons public init()
meth protected java.io.File findCacheDirectory()
meth protected java.io.File findUserDirectory()
supr org.openide.modules.Places

CLSS public final org.netbeans.core.startup.NbProblemDisplayer
meth public static java.lang.String messageForProblem(org.netbeans.Module,java.lang.Object)
supr java.lang.Object

CLSS public final org.netbeans.core.startup.NbRepository
cons public init()
meth protected void refreshAdditionalLayers()
meth public java.util.List<java.net.URL> additionalLayers(java.util.List<java.net.URL>)
supr org.openide.filesystems.Repository
hfds CONFIG_FOLDER

CLSS public final org.netbeans.core.startup.NbResourceStreamHandler
cons public init()
fld public final static java.lang.String PROTOCOL_LOCALIZED_SYSTEM_RESOURCE = "nbresloc"
fld public final static java.lang.String PROTOCOL_SYSTEM_RESOURCE = "nbres"
meth public java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
supr java.net.URLStreamHandler
hcls Connection

CLSS public abstract interface org.netbeans.core.startup.RunLevel
meth public abstract void run()

CLSS public final org.netbeans.core.startup.Splash
intf org.netbeans.Stamps$Updater
meth public java.awt.Component getComponent()
meth public static java.awt.Image loadContent(boolean)
meth public static org.netbeans.core.startup.Splash getInstance()
meth public static void configureDefaultRenderingHints(java.awt.Graphics2D)
meth public static void showAboutDialog(java.awt.Frame,javax.swing.JComponent)
meth public void addToMaxSteps(int)
meth public void cacheReady()
meth public void dispose()
meth public void flushCaches(java.io.DataOutputStream) throws java.io.IOException
meth public void increment(int)
meth public void print(java.lang.String)
meth public void setRunning(boolean)
supr java.lang.Object
hfds ICON_1024,ICON_16,ICON_256,ICON_32,ICON_48,ICON_512,USE_LAUNCHER_SPLASH,comp,frame,noBar,painter,splash,splashScreen
hcls SplashComponent,SplashDialog,SplashPainter,SplashRunner,TextBox

CLSS public org.netbeans.core.startup.StartLog
cons public init()
meth public static boolean willLog()
meth public static void logEnd(java.lang.String)
meth public static void logMeasuredStartupTime(long)
meth public static void logProgress(java.lang.String)
meth public static void logStart(java.lang.String)
supr java.lang.Object
hfds DEBUG_NESTING,LOG,actions,impl,logFileProp,logProp,places
hcls PerformanceTestsImpl,PrintImpl,SimplerFormatter,StartImpl

CLSS public final org.netbeans.core.startup.TestModuleDeployer
cons public init()
meth public static void deployTestModule(java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.core.startup.TopLogging
cons public init()
meth public static void initializeQuietly()
meth public static void printStackTrace(java.lang.Throwable,java.io.PrintWriter)
supr java.lang.Object
hfds OLD_ERR,defaultHandler,disabledConsole,previousUser,streamHandler
hcls AWTHandler,LookupDel

CLSS public abstract interface org.netbeans.core.startup.base.LayerFactory
innr public abstract interface static Provider
meth public abstract java.util.List<java.net.URL> additionalLayers(java.util.List<java.net.URL>)
meth public abstract org.openide.filesystems.FileSystem createEmptyFileSystem() throws java.io.IOException
meth public abstract org.openide.filesystems.FileSystem loadCache() throws java.io.IOException
meth public abstract org.openide.filesystems.FileSystem store(org.openide.filesystems.FileSystem,java.util.List<java.net.URL>) throws java.io.IOException

CLSS public org.netbeans.core.startup.layers.ArchiveURLMapper
cons public init()
meth public java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
supr org.openide.filesystems.URLMapper
hfds JAR_PROTOCOL,LOG,copiedJARs,mountRoots
hcls JFSReference

CLSS public abstract org.netbeans.core.startup.layers.LayerCacheManager
intf org.netbeans.core.startup.base.LayerFactory
meth public abstract org.openide.filesystems.FileSystem createEmptyFileSystem() throws java.io.IOException
meth public abstract org.openide.filesystems.FileSystem load(org.openide.filesystems.FileSystem,java.nio.ByteBuffer) throws java.io.IOException
meth public abstract void store(org.openide.filesystems.FileSystem,java.util.List<java.net.URL>,java.io.OutputStream) throws java.io.IOException
meth public final org.openide.filesystems.FileSystem loadCache() throws java.io.IOException
meth public final org.openide.filesystems.FileSystem store(org.openide.filesystems.FileSystem,java.util.List<java.net.URL>) throws java.io.IOException
meth public java.util.List<java.net.URL> additionalLayers(java.util.List<java.net.URL>)
meth public static org.netbeans.core.startup.layers.LayerCacheManager create(java.lang.String)
meth public static org.netbeans.core.startup.layers.LayerCacheManager manager(boolean)
supr java.lang.Object
hfds err,mgr,non
hcls NonCacheManager

CLSS public final org.netbeans.core.startup.layers.LocalFileSystemEx
cons public init()
meth protected void lock(java.lang.String) throws java.io.IOException
meth protected void unlock(java.lang.String)
meth public static boolean hasLocks()
meth public static java.lang.String[] getLocks()
meth public static void potentialLock(java.lang.String)
meth public static void potentialLock(java.lang.String,java.lang.String)
supr org.openide.filesystems.LocalFileSystem
hfds LOGGER,allLocks,pLocks
hcls DelegatingAttributes,WritableRemover

CLSS public org.netbeans.core.startup.layers.ModuleLayeredFileSystem
intf org.openide.util.LookupListener
meth public final org.openide.filesystems.FileSystem[] getLayers()
meth public static java.util.List<java.net.URL> collectLayers(java.lang.ClassLoader) throws java.io.IOException
meth public static org.netbeans.core.startup.layers.ModuleLayeredFileSystem getInstallationModuleLayer()
meth public static org.netbeans.core.startup.layers.ModuleLayeredFileSystem getUserModuleLayer()
meth public void addURLs(java.util.Collection<java.net.URL>) throws java.lang.Exception
meth public void removeURLs(java.util.Collection<java.net.URL>) throws java.lang.Exception
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void setURLs(java.util.List<java.net.URL>) throws java.lang.Exception
supr org.openide.filesystems.MultiFileSystem
hfds addLookupBefore,cacheLayer,err,fsResult,layerResult,manager,mutex,otherLayers,prevs,serialVersionUID,urls,user,writableLayer

CLSS public org.netbeans.core.startup.layers.NbinstURLMapper
cons public init()
fld public final static java.lang.String PROTOCOL = "nbinst"
meth public java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
supr org.openide.filesystems.URLMapper
hfds LOG

CLSS public org.netbeans.core.startup.layers.NbinstURLStreamHandler
cons public init()
meth protected boolean hostsEqual(java.net.URL,java.net.URL)
meth protected boolean sameFile(java.net.URL,java.net.URL)
meth protected int hashCode(java.net.URL)
meth protected java.net.InetAddress getHostAddress(java.net.URL)
meth protected java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
supr java.net.URLStreamHandler
hcls NbinstURLConnection

CLSS public final org.netbeans.core.startup.layers.SessionManager
fld public final static java.lang.String LAYER_INSTALL = "install"
fld public final static java.lang.String LAYER_SESSION = "session"
fld public final static java.lang.String PROP_CLOSE = "session_close"
fld public final static java.lang.String PROP_OPEN = "session_open"
meth public org.openide.filesystems.FileSystem create(java.io.File,java.io.File,java.io.File[]) throws java.beans.PropertyVetoException,java.io.IOException
meth public org.openide.filesystems.FileSystem getLayer(java.lang.String)
meth public static org.netbeans.core.startup.layers.SessionManager getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void close()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds layers,propertyChangeListeners,sm,systemFS

CLSS public final org.netbeans.core.startup.layers.SystemFileSystem
intf org.openide.filesystems.FileChangeListener
meth protected java.util.Set<? extends org.openide.filesystems.FileSystem> createLocksOn(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileSystem createWritableOn(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileSystem createWritableOnForRename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void notifyMigration(org.openide.filesystems.FileObject)
meth public final void setLayers(org.openide.filesystems.FileSystem[])
meth public java.lang.String getDisplayName()
meth public org.netbeans.core.startup.layers.ModuleLayeredFileSystem getInstallationLayer()
meth public org.netbeans.core.startup.layers.ModuleLayeredFileSystem getUserLayer()
meth public org.openide.filesystems.FileSystem[] getLayers()
meth public static org.netbeans.core.startup.layers.SystemFileSystem createInstallHomeSystem(java.io.File,java.io.File[],boolean,boolean) throws java.beans.PropertyVetoException,java.io.IOException
meth public static org.netbeans.core.startup.layers.SystemFileSystem createUserFileSystem(java.io.File,org.netbeans.core.startup.layers.ModuleLayeredFileSystem) throws java.beans.PropertyVetoException,java.io.IOException
meth public static void registerMutex(org.openide.util.Mutex)
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr org.openide.filesystems.MultiFileSystem
hfds LOG,SYSTEM_NAME,home,serialVersionUID,user
hcls SingletonSerializer

CLSS public abstract org.openide.LifecycleManager
cons protected init()
meth public abstract void exit()
meth public abstract void saveAll()
meth public static org.openide.LifecycleManager getDefault()
meth public void exit(int)
meth public void markForRestart()
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.filesystems.AbstractFileSystem
cons public init()
fld protected org.openide.filesystems.AbstractFileSystem$Attr attr
fld protected org.openide.filesystems.AbstractFileSystem$Change change
fld protected org.openide.filesystems.AbstractFileSystem$Info info
fld protected org.openide.filesystems.AbstractFileSystem$List list
fld protected org.openide.filesystems.AbstractFileSystem$Transfer transfer
innr public abstract interface static Attr
innr public abstract interface static Change
innr public abstract interface static Info
innr public abstract interface static List
innr public abstract interface static SymlinkInfo
innr public abstract interface static Transfer
meth protected <%0 extends org.openide.filesystems.FileObject> java.lang.ref.Reference<{%%0}> createReference({%%0})
meth protected boolean canRead(java.lang.String)
meth protected boolean canWrite(java.lang.String)
meth protected boolean checkVirtual(java.lang.String)
meth protected final int getRefreshTime()
meth protected final java.lang.ref.Reference<? extends org.openide.filesystems.FileObject> findReference(java.lang.String)
meth protected final java.util.Enumeration<? extends org.openide.filesystems.FileObject> existingFileObjects(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileObject refreshRoot()
meth protected final void refreshResource(java.lang.String,boolean)
meth protected final void setRefreshTime(int)
meth protected void markImportant(java.lang.String,boolean)
meth public abstract java.lang.String getDisplayName()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
meth public org.openide.filesystems.FileObject getRoot()
meth public void refresh(boolean)
supr org.openide.filesystems.FileSystem
hfds lastEnum,refresher,root,serialVersionUID

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public abstract org.openide.filesystems.FileSystem
cons public init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_HIDDEN = "hidden"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_READ_ONLY = "readOnly"
fld public final static java.lang.String PROP_ROOT = "root"
fld public final static java.lang.String PROP_SYSTEM_NAME = "systemName"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static AtomicAction
intf java.io.Serializable
meth protected final void fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void setSystemName(java.lang.String) throws java.beans.PropertyVetoException
 anno 0 java.lang.Deprecated()
meth public abstract boolean isReadOnly()
meth public abstract java.lang.String getDisplayName()
meth public abstract org.openide.filesystems.FileObject findResource(java.lang.String)
meth public abstract org.openide.filesystems.FileObject getRoot()
meth public final boolean isDefault()
meth public final boolean isValid()
meth public final java.lang.String getSystemName()
 anno 0 java.lang.Deprecated()
meth public final void addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void addFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public final void removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public final void runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject createTempFile(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.openide.filesystems.FileObject getTempFolder() throws java.io.IOException
meth public org.openide.filesystems.StatusDecorator getDecorator()
meth public org.openide.util.Lookup findExtrasFor(java.util.Set<org.openide.filesystems.FileObject>)
meth public void addNotify()
meth public void refresh(boolean)
meth public void removeNotify()
supr java.lang.Object
hfds LOG,SFS_STATUS,STATUS_NONE,assigned,changeSupport,defFS,fclSupport,fileStatusList,internLock,repository,serialVersionUID,statusResult,systemName,thrLocal,valid,vetoableChangeList
hcls AsyncAtomicAction,EventDispatcher,FileStatusDispatcher

CLSS public org.openide.filesystems.LocalFileSystem
cons public init()
innr public static Impl
meth protected boolean folder(java.lang.String)
meth protected boolean readOnly(java.lang.String)
meth protected java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth protected java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth protected java.lang.String computeSystemName(java.io.File)
meth protected java.lang.String mimeType(java.lang.String)
meth protected java.lang.String[] children(java.lang.String)
meth protected java.util.Date lastModified(java.lang.String)
meth protected long size(java.lang.String)
meth protected void createData(java.lang.String) throws java.io.IOException
meth protected void createFolder(java.lang.String) throws java.io.IOException
meth protected void delete(java.lang.String) throws java.io.IOException
meth protected void lock(java.lang.String) throws java.io.IOException
meth protected void markUnimportant(java.lang.String)
meth protected void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void unlock(java.lang.String)
meth public boolean isReadOnly()
meth public java.io.File getRootDirectory()
meth public java.lang.String getDisplayName()
meth public void setReadOnly(boolean)
meth public void setRootDirectory(java.io.File) throws java.beans.PropertyVetoException,java.io.IOException
supr org.openide.filesystems.AbstractFileSystem
hfds FAILURE,NOT_EXISTS,REFRESH_TIME,SUCCESS,readOnly,rootFile,serialVersionUID
hcls InnerAttrs

CLSS public org.openide.filesystems.MultiFileSystem
cons protected init()
cons public !varargs init(org.openide.filesystems.FileSystem[])
meth protected !varargs final void setDelegates(org.openide.filesystems.FileSystem[])
meth protected final org.openide.filesystems.FileSystem findSystem(org.openide.filesystems.FileObject)
meth protected final org.openide.filesystems.FileSystem[] getDelegates()
meth protected final void hideResource(java.lang.String,boolean) throws java.io.IOException
meth protected final void setPropagateMasks(boolean)
meth protected java.util.Set<? extends org.openide.filesystems.FileSystem> createLocksOn(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileObject findResourceOn(org.openide.filesystems.FileSystem,java.lang.String)
meth protected org.openide.filesystems.FileSystem createWritableOn(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileSystem createWritableOnForRename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected static java.util.Enumeration<java.lang.String> hiddenFiles(org.openide.filesystems.FileObject,boolean)
meth protected void markUnimportant(org.openide.filesystems.FileObject)
meth protected void notifyMigration(org.openide.filesystems.FileObject)
meth public boolean isReadOnly()
meth public final boolean getPropagateMasks()
meth public java.lang.String getDisplayName()
meth public org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
meth public org.openide.filesystems.FileObject getRoot()
meth public void addNotify()
meth public void refresh(boolean)
meth public void removeNotify()
supr org.openide.filesystems.FileSystem
hfds MASK,WRITE_SYSTEM_INDEX,insideWritableLayer,propagateMasks,root,rootAttributes,serialVersionUID,systems

CLSS public org.openide.filesystems.Repository
cons public init(org.openide.filesystems.FileSystem)
innr public abstract static LayerProvider
innr public abstract static LocalProvider
intf java.io.Serializable
meth protected final java.util.List<? extends java.net.URL> findLayers(org.openide.filesystems.Repository$LayerProvider)
meth protected final static void provideLayers(java.lang.ClassLoader,java.util.List<java.net.URL>) throws java.io.IOException
meth protected void refreshAdditionalLayers()
meth public final java.util.Enumeration<? extends org.openide.filesystems.FileObject> findAll(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final java.util.Enumeration<? extends org.openide.filesystems.FileObject> findAllResources(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final java.util.Enumeration<? extends org.openide.filesystems.FileSystem> fileSystems()
 anno 0 java.lang.Deprecated()
meth public final java.util.Enumeration<? extends org.openide.filesystems.FileSystem> getFileSystems()
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileObject findResource(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileSystem findFileSystem(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileSystem getDefaultFileSystem()
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileSystem[] toArray()
 anno 0 java.lang.Deprecated()
meth public final void addFileChangeListener(org.openide.filesystems.FileChangeListener)
 anno 0 java.lang.Deprecated()
meth public final void addFileSystem(org.openide.filesystems.FileSystem)
 anno 0 java.lang.Deprecated()
meth public final void addRepositoryListener(org.openide.filesystems.RepositoryListener)
 anno 0 java.lang.Deprecated()
meth public final void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
 anno 0 java.lang.Deprecated()
meth public final void removeFileChangeListener(org.openide.filesystems.FileChangeListener)
 anno 0 java.lang.Deprecated()
meth public final void removeFileSystem(org.openide.filesystems.FileSystem)
 anno 0 java.lang.Deprecated()
meth public final void removeRepositoryListener(org.openide.filesystems.RepositoryListener)
 anno 0 java.lang.Deprecated()
meth public final void reorder(int[])
 anno 0 java.lang.Deprecated()
meth public final void writeExternal(java.io.ObjectOutput) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.Repository getDefault()
supr java.lang.Object
hfds ADD_FS,NO_PROVIDER,fclSupport,fileSystems,fileSystemsClone,lastDefLookup,lastLocalProvider,listeners,names,propListener,repository,serialVersionUID,system,vetoListener
hcls MainFS,Replacer

CLSS public abstract org.openide.filesystems.URLMapper
cons public init()
fld public final static int EXTERNAL = 1
fld public final static int INTERNAL = 0
fld public final static int NETWORK = 2
meth public abstract java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public abstract org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
meth public static java.net.URL findURL(org.openide.filesystems.FileObject,int)
meth public static org.openide.filesystems.FileObject findFileObject(java.net.URL)
meth public static org.openide.filesystems.FileObject[] findFileObjects(java.net.URL)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds CACHE_JUST_COMPUTING,cache,result,threadCache
hcls DefaultURLMapper

CLSS public abstract org.openide.modules.InstalledFileLocator
cons protected init()
meth public abstract java.io.File locate(java.lang.String,java.lang.String,boolean)
meth public java.util.Set<java.io.File> locateAll(java.lang.String,java.lang.String,boolean)
meth public static org.openide.modules.InstalledFileLocator getDefault()
supr java.lang.Object
hfds DEFAULT,LOCK,instances,result

CLSS public abstract org.openide.modules.Places
cons protected init()
meth protected abstract java.io.File findCacheDirectory()
meth protected abstract java.io.File findUserDirectory()
meth public static java.io.File getCacheDirectory()
meth public static java.io.File getCacheSubdirectory(java.lang.String)
meth public static java.io.File getCacheSubfile(java.lang.String)
meth public static java.io.File getUserDirectory()
supr java.lang.Object
hfds LOG

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

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public org.openide.util.lookup.ProxyLookup
cons protected init()
cons public !varargs init(org.openide.util.Lookup[])
cons public init(org.openide.util.lookup.ProxyLookup$Controller)
innr public final static Controller
meth protected !varargs final void setLookups(java.util.concurrent.Executor,org.openide.util.Lookup[])
meth protected !varargs final void setLookups(org.openide.util.Lookup[])
meth protected final org.openide.util.Lookup[] getLookups()
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds data
hcls EmptyInternalData,ImmutableInternalData,LazyCollection,LazyList,LazySet,R,RealInternalData,SingleInternalData,WeakRef,WeakResult

