#Signature file v4.1
#Version 2.102

CLSS public java.awt.datatransfer.Clipboard
cons public init(java.lang.String)
fld protected java.awt.datatransfer.ClipboardOwner owner
fld protected java.awt.datatransfer.Transferable contents
meth public boolean isDataFlavorAvailable(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getAvailableDataFlavors()
meth public java.awt.datatransfer.FlavorListener[] getFlavorListeners()
meth public java.awt.datatransfer.Transferable getContents(java.lang.Object)
meth public java.lang.Object getData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String getName()
meth public void addFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void removeFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.lang.Object

CLSS public abstract interface java.awt.datatransfer.FlavorListener
intf java.util.EventListener
meth public abstract void flavorsChanged(java.awt.datatransfer.FlavorEvent)

CLSS public abstract interface java.awt.event.AWTEventListener
intf java.util.EventListener
meth public abstract void eventDispatched(java.awt.AWTEvent)

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
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

CLSS public java.lang.SecurityManager
cons public init()
fld protected boolean inCheck
 anno 0 java.lang.Deprecated()
meth protected boolean inClass(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected boolean inClassLoader()
 anno 0 java.lang.Deprecated()
meth protected int classDepth(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected int classLoaderDepth()
 anno 0 java.lang.Deprecated()
meth protected java.lang.Class<?> currentLoadedClass()
 anno 0 java.lang.Deprecated()
meth protected java.lang.ClassLoader currentClassLoader()
 anno 0 java.lang.Deprecated()
meth protected java.lang.Class[] getClassContext()
meth public boolean checkTopLevelWindow(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public boolean getInCheck()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object getSecurityContext()
meth public java.lang.ThreadGroup getThreadGroup()
meth public void checkAccept(java.lang.String,int)
meth public void checkAccess(java.lang.Thread)
meth public void checkAccess(java.lang.ThreadGroup)
meth public void checkAwtEventQueueAccess()
 anno 0 java.lang.Deprecated()
meth public void checkConnect(java.lang.String,int)
meth public void checkConnect(java.lang.String,int,java.lang.Object)
meth public void checkCreateClassLoader()
meth public void checkDelete(java.lang.String)
meth public void checkExec(java.lang.String)
meth public void checkExit(int)
meth public void checkLink(java.lang.String)
meth public void checkListen(int)
meth public void checkMemberAccess(java.lang.Class<?>,int)
 anno 0 java.lang.Deprecated()
meth public void checkMulticast(java.net.InetAddress)
meth public void checkMulticast(java.net.InetAddress,byte)
 anno 0 java.lang.Deprecated()
meth public void checkPackageAccess(java.lang.String)
meth public void checkPackageDefinition(java.lang.String)
meth public void checkPermission(java.security.Permission)
meth public void checkPermission(java.security.Permission,java.lang.Object)
meth public void checkPrintJobAccess()
meth public void checkPropertiesAccess()
meth public void checkPropertyAccess(java.lang.String)
meth public void checkRead(java.io.FileDescriptor)
meth public void checkRead(java.lang.String)
meth public void checkRead(java.lang.String,java.lang.Object)
meth public void checkSecurityAccess(java.lang.String)
meth public void checkSetFactory()
meth public void checkSystemClipboardAccess()
 anno 0 java.lang.Deprecated()
meth public void checkWrite(java.io.FileDescriptor)
meth public void checkWrite(java.lang.String)
supr java.lang.Object

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

CLSS public abstract interface java.net.URLStreamHandlerFactory
meth public abstract java.net.URLStreamHandler createURLStreamHandler(java.lang.String)

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.ArchiveResources
meth public abstract byte[] resource(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getIdentifier()

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

CLSS public final static org.netbeans.CLIHandler$Args
 outer org.netbeans.CLIHandler
meth public boolean isOpen()
meth public java.io.File getCurrentDirectory()
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getErrorStream()
meth public java.io.OutputStream getOutputStream()
meth public java.lang.String[] getArguments()
supr java.lang.Object
hfds args,argsBackup,closed,currentDir,err,is,os

CLSS public final org.netbeans.DuplicateException
meth public org.netbeans.Module getNewModule()
meth public org.netbeans.Module getOldModule()
supr java.lang.Exception
hfds nue,old

CLSS public abstract org.netbeans.Events
cons protected init()
fld public final static java.lang.String CANT_DELETE_ENABLED_AUTOLOAD = "cantDeleteEnabledAutoload"
fld public final static java.lang.String CLOSE = "close"
fld public final static java.lang.String CREATED_MODULE_SYSTEM = "createdModuleSystem"
fld public final static java.lang.String DELETE_MODULE = "deleteModule"
fld public final static java.lang.String EXTENSION_MULTIPLY_LOADED = "extensionMultiplyLoaded"
fld public final static java.lang.String FAILED_INSTALL_NEW = "failedInstallNew"
fld public final static java.lang.String FAILED_INSTALL_NEW_UNEXPECTED = "failedInstallNewUnexpected"
fld public final static java.lang.String FINISH_AUTO_RESTORE = "finishAutoRestore"
fld public final static java.lang.String FINISH_CREATE_BOOT_MODULE = "finishCreateBootModule"
fld public final static java.lang.String FINISH_CREATE_REGULAR_MODULE = "finishCreateRegularModule"
fld public final static java.lang.String FINISH_DEPLOY_TEST_MODULE = "finishDeployTestModule"
fld public final static java.lang.String FINISH_DISABLE_MODULES = "finishDisableModules"
fld public final static java.lang.String FINISH_ENABLE_MODULES = "finishEnableModules"
fld public final static java.lang.String FINISH_LOAD = "finishLoad"
fld public final static java.lang.String FINISH_LOAD_BOOT_MODULES = "finishLoadBootModules"
fld public final static java.lang.String FINISH_READ = "finishRead"
fld public final static java.lang.String FINISH_UNLOAD = "finishUnload"
fld public final static java.lang.String INSTALL = "install"
fld public final static java.lang.String LOAD_LAYERS = "loadLayers"
fld public final static java.lang.String LOAD_SECTION = "loadSection"
fld public final static java.lang.String MISC_PROP_MISMATCH = "miscPropMismatch"
fld public final static java.lang.String MISSING_JAR_FILE = "missingJarFile"
fld public final static java.lang.String MODULES_FILE_PROCESSED = "modulesFileProcessed"
fld public final static java.lang.String MODULES_FILE_SCANNED = "modulesFileScanned"
fld public final static java.lang.String PATCH = "patch"
fld public final static java.lang.String PERF_END = "perfEnd"
fld public final static java.lang.String PERF_START = "perfStart"
fld public final static java.lang.String PERF_TICK = "perfTick"
fld public final static java.lang.String PREPARE = "prepare"
fld public final static java.lang.String RESTORE = "restore"
fld public final static java.lang.String START_AUTO_RESTORE = "startAutoRestore"
fld public final static java.lang.String START_CREATE_BOOT_MODULE = "startCreateBootModule"
fld public final static java.lang.String START_CREATE_REGULAR_MODULE = "startCreateRegularModule"
fld public final static java.lang.String START_DEPLOY_TEST_MODULE = "startDeployTestModule"
fld public final static java.lang.String START_DISABLE_MODULES = "startDisableModules"
fld public final static java.lang.String START_ENABLE_MODULES = "startEnableModules"
fld public final static java.lang.String START_LOAD = "startLoad"
fld public final static java.lang.String START_LOAD_BOOT_MODULES = "startLoadBootModules"
fld public final static java.lang.String START_READ = "startRead"
fld public final static java.lang.String START_UNLOAD = "startUnload"
fld public final static java.lang.String UNINSTALL = "uninstall"
fld public final static java.lang.String UNLOAD_LAYERS = "unloadLayers"
fld public final static java.lang.String UPDATE = "update"
fld public final static java.lang.String WRONG_CLASS_LOADER = "wrongClassLoader"
meth protected abstract void logged(java.lang.String,java.lang.Object[])
meth public !varargs final void log(java.lang.String,java.lang.Object[])
supr java.lang.Object

CLSS public final org.netbeans.InvalidException
cons public init(java.lang.String)
cons public init(org.netbeans.Module,java.lang.String)
cons public init(org.netbeans.Module,java.lang.String,java.lang.String)
meth public java.lang.String getLocalizedMessage()
meth public java.util.jar.Manifest getManifest()
meth public org.netbeans.Module getModule()
supr java.io.IOException
hfds localizedMessage,m,man

CLSS public org.netbeans.JarClassLoader
cons public init(java.util.List<java.io.File>,java.lang.ClassLoader[])
cons public init(java.util.List<java.io.File>,java.lang.ClassLoader[],boolean)
cons public init(java.util.List<java.io.File>,java.lang.ClassLoader[],boolean,org.netbeans.Module)
meth protected java.lang.Class<?> doLoadClass(java.lang.String,java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth public java.net.URL findResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String)
meth public static boolean isArchivePopulated()
meth public static void saveArchive()
meth public void destroy()
supr org.netbeans.ProxyClassLoader
hfds BASE_VERSION,LOGGER,META_INF,MULTI_RELEASE,RUNTIME_VERSION,archive,cache,module,patchingBytecode,sources
hcls DirSource,JarSource,JarURLStreamHandler,NbJarURLConnection,Source

CLSS public final org.netbeans.JaveleonModule
cons public init(org.netbeans.ModuleManager,java.io.File,java.lang.Object,org.netbeans.Events) throws java.io.IOException
fld public final static boolean isJaveleonPresent
meth protected java.lang.ClassLoader createNewClassLoader(java.util.List<java.io.File>,java.util.List<java.lang.ClassLoader>)
meth protected java.lang.ClassLoader getParentLoader(org.netbeans.Module)
meth protected void classLoaderDown()
meth protected void cleanup()
meth public boolean isFixed()
meth public final void reload() throws java.io.IOException
meth public java.io.File getJarFile()
meth public java.lang.Object getLocalizedAttribute(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<java.io.File> getAllJars()
meth public java.util.jar.Manifest getManifest()
meth public static boolean incrementGlobalId()
meth public void classLoaderUp(java.util.Set<org.netbeans.Module>) throws java.io.IOException
meth public void destroy()
meth public void releaseManifest()
meth public void setReloadable(boolean)
supr org.netbeans.Module
hfds LOG,currentClassLoaders,incrementGlobalId,registerClassLoader

CLSS public final org.netbeans.Main
meth public static void finishInitialization()
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract org.netbeans.Module
cons protected init(org.netbeans.ModuleManager,org.netbeans.Events,java.lang.Object,boolean,boolean,boolean) throws java.io.IOException
cons protected init(org.netbeans.ModuleManager,org.netbeans.Events,java.lang.Object,java.lang.ClassLoader) throws org.netbeans.InvalidException
cons protected init(org.netbeans.ModuleManager,org.netbeans.Events,java.lang.Object,java.lang.ClassLoader,boolean,boolean) throws org.netbeans.InvalidException
fld protected boolean reloadable
fld protected final org.netbeans.Events events
fld protected final org.netbeans.ModuleManager mgr
fld protected java.lang.ClassLoader classloader
fld public final static java.lang.String PROP_CLASS_LOADER = "classLoader"
fld public final static java.lang.String PROP_MANIFEST = "manifest"
fld public final static java.lang.String PROP_PROBLEMS = "problems"
fld public final static java.lang.String PROP_RELOADABLE = "reloadable"
fld public final static java.lang.String PROP_VALID = "valid"
innr public final static PackageExport
meth protected abstract void classLoaderDown()
meth protected abstract void classLoaderUp(java.util.Set<org.netbeans.Module>) throws java.io.IOException
meth protected abstract void cleanup()
meth protected abstract void destroy()
meth protected void parseManifest() throws org.netbeans.InvalidException
meth public abstract boolean isFixed()
meth public abstract java.util.List<java.io.File> getAllJars()
meth public abstract java.util.jar.Manifest getManifest()
meth public abstract void reload() throws java.io.IOException
meth public abstract void setReloadable(boolean)
meth public boolean isAutoload()
meth public boolean isEager()
meth public boolean isEnabled()
meth public boolean isReloadable()
meth public boolean isValid()
meth public boolean owns(java.lang.Class<?>)
meth public final boolean isNetigso()
meth public final boolean provides(java.lang.String)
meth public final int getStartLevel()
meth public final java.lang.Object getHistory()
meth public final org.openide.modules.Dependency[] getDependenciesArray()
meth public int getCodeNameRelease()
meth public java.io.File getJarFile()
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.String getBuildVersion()
meth public java.lang.String getCodeName()
meth public java.lang.String getCodeNameBase()
meth public java.lang.String getImplementationVersion()
meth public java.lang.String toString()
meth public java.lang.String[] getProvides()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String)
meth public java.util.Set<java.lang.Object> getProblems()
meth public java.util.Set<org.openide.modules.Dependency> getDependencies()
meth public org.netbeans.Module$PackageExport[] getPublicPackages()
meth public org.netbeans.ModuleManager getManager()
meth public org.openide.modules.SpecificationVersion getSpecificationVersion()
meth public void releaseManifest()
supr org.openide.modules.ModuleInfo
hfds DATA_LOCK,autoload,data,eager,enabled,history,instr

CLSS public final static org.netbeans.Module$PackageExport
 outer org.netbeans.Module
cons public init(java.lang.String,boolean)
fld public final boolean recursive
fld public final java.lang.String pkg
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.netbeans.ModuleFactory
cons public init()
meth public boolean removeBaseClassLoader()
meth public java.lang.ClassLoader getClasspathDelegateClassLoader(org.netbeans.ModuleManager,java.lang.ClassLoader)
meth public org.netbeans.Module create(java.io.File,java.lang.Object,boolean,boolean,boolean,org.netbeans.ModuleManager,org.netbeans.Events) throws java.io.IOException
meth public org.netbeans.Module createFixed(java.util.jar.Manifest,java.lang.Object,java.lang.ClassLoader,boolean,boolean,org.netbeans.ModuleManager,org.netbeans.Events) throws org.netbeans.InvalidException
supr java.lang.Object

CLSS public abstract org.netbeans.ModuleInstaller
cons protected init()
meth protected java.util.Set<org.openide.modules.Dependency> loadDependencies(java.lang.String)
meth protected void classLoaderUp(java.lang.ClassLoader)
meth public abstract boolean closing(java.util.List<org.netbeans.Module>)
meth public abstract void close(java.util.List<org.netbeans.Module>)
meth public abstract void dispose(org.netbeans.Module)
meth public abstract void load(java.util.List<org.netbeans.Module>)
meth public abstract void prepare(org.netbeans.Module) throws org.netbeans.InvalidException
meth public abstract void unload(java.util.List<org.netbeans.Module>)
meth public boolean shouldDelegateClasspathResource(java.lang.String)
meth public boolean shouldDelegateResource(org.netbeans.Module,org.netbeans.Module,java.lang.String)
meth public java.lang.String[] refineProvides(org.netbeans.Module)
meth public java.util.jar.Manifest loadManifest(java.io.File) throws java.io.IOException
meth public org.openide.util.Task closeAsync(java.util.List<org.netbeans.Module>)
meth public void refineClassLoader(org.netbeans.Module,java.util.List<? extends java.lang.ClassLoader>)
meth public void refineDependencies(org.netbeans.Module,java.util.Set<org.openide.modules.Dependency>)
supr java.lang.Object

CLSS public final org.netbeans.ModuleManager
cons public init(org.netbeans.ModuleInstaller,org.netbeans.Events)
fld public final static java.lang.String PROP_CLASS_LOADER = "classLoader"
fld public final static java.lang.String PROP_ENABLED_MODULES = "enabledModules"
fld public final static java.lang.String PROP_MODULES = "modules"
meth public boolean hasToEnableCompatModules(java.util.Set<org.netbeans.Module>)
meth public boolean isOrWillEnable(org.netbeans.Module)
meth public boolean shouldDelegateResource(org.netbeans.Module,org.netbeans.Module,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean shouldDelegateResource(org.netbeans.Module,org.netbeans.Module,java.lang.String,java.lang.ClassLoader)
meth public boolean shutDown()
meth public boolean shutDown(java.lang.Runnable)
meth public final java.util.Set<org.netbeans.Module> getEnabledModules()
meth public final org.netbeans.Events getEvents()
meth public final org.netbeans.Module get(java.lang.String)
meth public final org.openide.util.Mutex mutex()
meth public final org.openide.util.Mutex$Privileged mutexPrivileged()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void disable(org.netbeans.Module)
meth public final void enable(org.netbeans.Module) throws org.netbeans.InvalidException
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.ClassLoader refineClassLoader(org.netbeans.Module,java.util.List<? extends java.lang.ClassLoader>)
meth public java.util.Collection<org.netbeans.Module> getAttachedFragments(org.netbeans.Module)
meth public java.util.List<org.netbeans.Module> simulateDisable(java.util.Set<org.netbeans.Module>)
meth public java.util.List<org.netbeans.Module> simulateEnable(java.util.Set<org.netbeans.Module>)
meth public java.util.List<org.netbeans.Module> simulateJaveleonReload(org.netbeans.Module)
meth public java.util.Set<org.netbeans.Module> getModuleInterdependencies(org.netbeans.Module,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.netbeans.Module> getModuleInterdependencies(org.netbeans.Module,boolean,boolean,boolean)
meth public java.util.Set<org.netbeans.Module> getModules()
meth public java.util.concurrent.Future<java.lang.Boolean> shutDownAsync(java.lang.Runnable)
meth public org.netbeans.Module create(java.io.File,java.lang.Object,boolean,boolean) throws java.io.IOException,org.netbeans.DuplicateException
 anno 0 java.lang.Deprecated()
meth public org.netbeans.Module create(java.io.File,java.lang.Object,boolean,boolean,boolean) throws java.io.IOException,org.netbeans.DuplicateException
meth public org.netbeans.Module createBundle(java.io.File,java.lang.Object,boolean,boolean,boolean,int) throws java.io.IOException,org.netbeans.DuplicateException
meth public org.netbeans.Module createFixed(java.util.jar.Manifest,java.lang.Object,java.lang.ClassLoader) throws org.netbeans.DuplicateException,org.netbeans.InvalidException
meth public org.netbeans.Module createFixed(java.util.jar.Manifest,java.lang.Object,java.lang.ClassLoader,boolean,boolean) throws org.netbeans.DuplicateException,org.netbeans.InvalidException
meth public org.openide.modules.ModuleInfo findCodeNameBase(java.lang.String)
meth public org.openide.modules.ModuleInfo ownerOf(java.lang.Class<?>)
meth public org.openide.util.Lookup getModuleLookup()
meth public void delete(org.netbeans.Module)
meth public void disable(java.util.Set<org.netbeans.Module>)
meth public void enable(java.util.Set<org.netbeans.Module>) throws org.netbeans.InvalidException
meth public void releaseModuleManifests()
meth public void reload(org.netbeans.Module) throws java.io.IOException
meth public void replaceJaveleonModule(org.netbeans.Module,org.netbeans.Module)
supr org.openide.modules.Modules
hfds DEPLOG,EMPTY_COLLECTION,MODULE_PROBLEMS_LOCK,MUTEX,MUTEX_PRIVILEGED,PRINT_TOPOLOGICAL_EXCEPTION_STACK_TRACES,PROBING_IN_PROCESS,addedBecauseOfDependent,bootstrapModules,changeSupport,classLoader,classLoaderLock,classLoaderPatches,completeLookup,eagerActivation,enableContext,environmentTokens,ev,firer,fragmentModules,installer,lookup,mdc,moduleFactory,moduleProblemsWithNeeds,moduleProblemsWithoutNeeds,modules,modulesByName,netigso,providersOf,readOnly,reported,reportedProblems
hcls CodeNameBaseComparator,EnableContext,ModuleDataCache,ProvidersOf,SystemClassLoader

CLSS public final org.netbeans.NbClipboard
cons public init()
intf java.awt.datatransfer.FlavorListener
intf java.awt.event.AWTEventListener
intf org.openide.util.LookupListener
meth protected org.openide.util.datatransfer.ExClipboard$Convertor[] getConvertors()
meth public java.awt.datatransfer.FlavorListener[] getFlavorListeners()
meth public java.awt.datatransfer.Transferable getContents(java.lang.Object)
meth public void addFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void eventDispatched(java.awt.AWTEvent)
meth public void flavorsChanged(java.awt.datatransfer.FlavorEvent)
meth public void resultChanged(org.openide.util.LookupEvent)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr org.openide.util.datatransfer.ExClipboard
hfds FIRING,RP,anyWindowIsActivated,convertors,getContentsTask,last,lastWindowActivated,lastWindowDeactivated,lastWindowDeactivatedSource,log,result,setContentsTask,slowSystemClipboard,systemClipboard
hcls GetContents,LoggableTransferable,SetContents

CLSS public org.netbeans.NbExecJavaStartTry
cons public init()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public abstract org.netbeans.NetigsoFramework
cons protected init()
meth protected abstract java.util.Set<java.lang.String> createLoader(org.openide.modules.ModuleInfo,org.netbeans.ProxyClassLoader,java.io.File) throws java.io.IOException
meth protected abstract java.util.Set<java.lang.String> start(java.util.Collection<? extends org.netbeans.Module>)
meth protected abstract void prepare(org.openide.util.Lookup,java.util.Collection<? extends org.netbeans.Module>)
meth protected abstract void reload(org.netbeans.Module) throws java.io.IOException
meth protected abstract void shutdown()
meth protected abstract void start()
meth protected abstract void stopLoader(org.openide.modules.ModuleInfo,java.lang.ClassLoader)
meth protected final byte[] fromArchive(org.netbeans.ArchiveResources,java.lang.String) throws java.io.IOException
meth protected final byte[] patchByteCode(java.lang.ClassLoader,java.lang.String,java.security.ProtectionDomain,byte[])
meth protected final java.lang.ClassLoader createClassLoader(java.lang.String)
meth protected final org.netbeans.Module findModule(java.lang.String)
meth protected int defaultStartLevel()
meth protected java.lang.ClassLoader findFrameworkClassLoader()
meth protected java.util.Enumeration<java.net.URL> findResources(org.netbeans.Module,java.lang.String)
supr java.lang.Object
hfds mgr

CLSS public final org.netbeans.PatchByteCode
meth public static byte[] patch(byte[])
supr java.lang.Object
hfds CONSTRUCTOR_NAME,DESC_CTOR_ANNOTATION,DESC_DEFAULT_CTOR,DESC_PATCHED_PUBLIC_ANNOTATION,DISABLE_PATCHING,LOG,NOP,PATCHED_PUBLIC,PREFIX_EXTEND,PUBLIC_ONLY,RUNTIME_INVISIBLE_ANNOTATIONS,classToExtend,patchAsmMethod,patchPublic,theClassLoader

CLSS public org.netbeans.ProxyClassLoader
cons public init(java.lang.ClassLoader[],boolean)
cons public init(java.lang.ClassLoader[],boolean,java.util.function.BiFunction<java.lang.String,java.lang.ClassLoader,java.lang.Boolean>)
meth protected boolean shouldDelegateResource(java.lang.String,java.lang.ClassLoader)
meth protected final void addCoveredPackages(java.lang.Iterable<java.lang.String>)
meth protected final void setSystemClassLoader(java.lang.ClassLoader)
meth protected java.lang.Class<?> doLoadClass(java.lang.String,java.lang.String)
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package getPackageFast(java.lang.String,boolean)
meth protected java.lang.Package[] getPackages()
meth public final java.net.URL getResource(java.lang.String)
meth public final java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public java.net.URL findResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public void append(java.lang.ClassLoader[])
meth public void destroy()
supr java.lang.ClassLoader
hfds LOGGER,LOG_LOADING,TOP_CL,arbitraryLoadWarnings,delegatingPredicate,packages,parents,sclPackages

CLSS public org.netbeans.ProxyURLStreamHandlerFactory
intf java.net.URLStreamHandlerFactory
intf org.openide.util.LookupListener
meth public java.net.URLStreamHandler createURLStreamHandler(java.lang.String)
meth public static void register()
meth public void resultChanged(org.openide.util.LookupEvent)
supr java.lang.Object
hfds LOG,delegate,handlers,originalJarHandler,proxyFactoryInitialized,r

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

CLSS public org.netbeans.TopSecurityManager
cons public init()
meth public boolean checkTopLevelWindow(java.lang.Object)
meth public final void checkPropertyAccess(java.lang.String)
meth public static void exit(int)
meth public static void install()
meth public static void makeSwingUseSpecialClipboard(java.awt.datatransfer.Clipboard)
meth public static void register(java.lang.SecurityManager)
meth public static void unregister(java.lang.SecurityManager)
meth public void checkConnect(java.lang.String,int)
meth public void checkConnect(java.lang.String,int,java.lang.Object)
meth public void checkDelete(java.lang.String)
meth public void checkExit(int)
meth public void checkMemberAccess(java.lang.Class<?>,int)
meth public void checkPermission(java.security.Permission)
meth public void checkPermission(java.security.Permission,java.lang.Object)
meth public void checkRead(java.io.FileDescriptor)
meth public void checkRead(java.lang.String)
meth public void checkWrite(java.io.FileDescriptor)
meth public void checkWrite(java.lang.String)
supr java.lang.SecurityManager
hfds CLIPBOARD_FORBIDDEN,LOG,URLClass,accessControllerClass,allPermission,awtPermissionClass,callerWhiteList,check,classLoaderClass,delegates,fsSecManager,officialExit,runtimePermissionClass,urlField,warnedClassesNDE,warnedClassesNH,warnedSunMisc
hcls PrivilegedCheck

CLSS public final org.netbeans.Util
fld public final static java.util.logging.Logger err
innr public abstract interface static ModuleProvider
meth public static boolean checkPackageDependency(org.openide.modules.Dependency,java.lang.ClassLoader)
meth public static java.lang.Object[] parseCodeName(java.lang.String)
meth public static java.lang.String createPackageName(java.lang.String)
meth public static java.lang.String[] getLocalizingSuffixesFast()
meth public static java.util.Map<org.netbeans.Module,java.util.List<org.netbeans.Module>> moduleDependencies(java.util.Collection<org.netbeans.Module>,java.util.Map<java.lang.String,org.netbeans.Module>,java.util.Map<java.lang.String,java.util.Set<org.netbeans.Module>>)
meth public static org.openide.modules.SpecificationVersion getModuleDep(java.util.Set<org.openide.modules.Dependency>,java.lang.String)
meth public static void transitiveClosureModuleDependencies(org.netbeans.ModuleManager,java.util.Set<org.netbeans.Module>)
supr java.lang.Object
hfds codeNameParseCache
hcls JarFilter,ModuleLookup

CLSS public abstract interface static org.netbeans.Util$ModuleProvider
 outer org.netbeans.Util
meth public abstract org.netbeans.Module getModule()

CLSS public abstract org.openide.modules.ModuleInfo
cons protected init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean isEnabled()
meth public abstract boolean owns(java.lang.Class<?>)
meth public abstract int getCodeNameRelease()
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.Object getLocalizedAttribute(java.lang.String)
meth public abstract java.lang.String getCodeName()
meth public abstract java.lang.String getCodeNameBase()
meth public abstract java.util.Set<org.openide.modules.Dependency> getDependencies()
meth public abstract org.openide.modules.SpecificationVersion getSpecificationVersion()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.String getBuildVersion()
meth public java.lang.String getDisplayName()
meth public java.lang.String getImplementationVersion()
meth public java.lang.String[] getProvides()
supr java.lang.Object
hfds changeSupport

CLSS public org.openide.modules.Modules
cons protected init()
meth public org.openide.modules.ModuleInfo findCodeNameBase(java.lang.String)
meth public org.openide.modules.ModuleInfo ownerOf(java.lang.Class<?>)
meth public static org.openide.modules.Modules getDefault()
supr java.lang.Object

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public abstract org.openide.util.datatransfer.ExClipboard
cons public init(java.lang.String)
innr public abstract interface static Convertor
meth protected abstract org.openide.util.datatransfer.ExClipboard$Convertor[] getConvertors()
meth protected final void fireClipboardChange()
meth public final void addClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public final void removeClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public java.awt.datatransfer.Transferable convert(java.awt.datatransfer.Transferable)
meth public static void transferableAccepted(java.awt.datatransfer.Transferable,int)
meth public static void transferableOwnershipLost(java.awt.datatransfer.Transferable)
meth public static void transferableRejected(java.awt.datatransfer.Transferable)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.awt.datatransfer.Clipboard
hfds listeners

