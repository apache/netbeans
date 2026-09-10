#Signature file v4.1
#Version 1.75

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FilterOutputStream
cons public init(java.io.OutputStream)
fld protected java.io.OutputStream out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public static java.io.OutputStream nullOutputStream()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean forRemoval()
meth public abstract !hasdefault java.lang.String since()

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
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

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray(java.util.function.IntFunction<{%%0}[]>)
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract java.util.Dictionary<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.util.Enumeration<{java.util.Dictionary%0}> keys()
meth public abstract java.util.Enumeration<{java.util.Dictionary%1}> elements()
meth public abstract {java.util.Dictionary%1} get(java.lang.Object)
meth public abstract {java.util.Dictionary%1} put({java.util.Dictionary%0},{java.util.Dictionary%1})
meth public abstract {java.util.Dictionary%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> ofEntries(java.util.Map$Entry<? extends {%%0},? extends {%%1}>[])
 anno 0 java.lang.SafeVarargs()
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map$Entry<{%%0},{%%1}> entry({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> copyOf(java.util.Map<? extends {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public org.eclipse.core.runtime.adaptor.EclipseStarter
cons public init()
fld public final static java.lang.String PROP_ADAPTOR = "osgi.adaptor"
fld public final static java.lang.String PROP_ALLOW_APPRELAUNCH = "eclipse.allowAppRelaunch"
fld public final static java.lang.String PROP_ARCH = "osgi.arch"
fld public final static java.lang.String PROP_BUNDLES = "osgi.bundles"
fld public final static java.lang.String PROP_BUNDLES_STARTLEVEL = "osgi.bundles.defaultStartLevel"
fld public final static java.lang.String PROP_CHECK_CONFIG = "osgi.checkConfiguration"
fld public final static java.lang.String PROP_CLEAN = "osgi.clean"
fld public final static java.lang.String PROP_CONSOLE = "osgi.console"
fld public final static java.lang.String PROP_CONSOLE_CLASS = "osgi.consoleClass"
fld public final static java.lang.String PROP_CONSOLE_LOG = "eclipse.consoleLog"
fld public final static java.lang.String PROP_DEBUG = "osgi.debug"
fld public final static java.lang.String PROP_DEV = "osgi.dev"
fld public final static java.lang.String PROP_EXITCODE = "eclipse.exitcode"
fld public final static java.lang.String PROP_EXITDATA = "eclipse.exitdata"
fld public final static java.lang.String PROP_EXTENSIONS = "osgi.framework.extensions"
fld public final static java.lang.String PROP_FRAMEWORK = "osgi.framework"
fld public final static java.lang.String PROP_FRAMEWORK_SHAPE = "osgi.framework.shape"
fld public final static java.lang.String PROP_IGNOREAPP = "eclipse.ignoreApp"
fld public final static java.lang.String PROP_INITIAL_STARTLEVEL = "osgi.startLevel"
fld public final static java.lang.String PROP_INSTALL_AREA = "osgi.install.area"
fld public final static java.lang.String PROP_LOGFILE = "osgi.logfile"
fld public final static java.lang.String PROP_NL = "osgi.nl"
fld public final static java.lang.String PROP_NOSHUTDOWN = "osgi.noShutdown"
fld public final static java.lang.String PROP_OS = "osgi.os"
fld public final static java.lang.String PROP_REFRESH_BUNDLES = "eclipse.refreshBundles"
fld public final static java.lang.String PROP_SYSPATH = "osgi.syspath"
fld public final static java.lang.String PROP_WS = "osgi.ws"
fld public static boolean debug
meth protected static java.lang.String getSysPath()
meth public static boolean isRunning()
meth public static java.lang.Object run(java.lang.Object) throws java.lang.Exception
meth public static java.lang.Object run(java.lang.String[],java.lang.Runnable) throws java.lang.Exception
meth public static org.osgi.framework.BundleContext getSystemBundleContext()
meth public static org.osgi.framework.BundleContext startup(java.lang.String[],java.lang.Runnable) throws java.lang.Exception
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public static void setInitialProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public static void shutdown() throws java.lang.Exception
supr java.lang.Object
hfds ARCH,CLEAN,CONFIGURATION,CONSOLE,CONSOLE_LOG,DATA,DEBUG,DEFAULT_BUNDLES_STARTLEVEL,DEFAULT_INITIAL_STARTLEVEL,DEV,FILE_SCHEME,INITIALIZE,INITIAL_LOCATION,LAUNCHER,NL,NL_EXTENSIONS,NOEXIT,OS,PROP_APPLICATION_LAUNCHDEFAULT,PROP_NL_EXTENSIONS,REFERENCE_PROTOCOL,REFERENCE_SCHEME,USER,WS,allArgs,appArgs,appLauncher,appLauncherRegistration,configuration,consoleMgr,context,defaultMonitorRegistration,equinoxConfig,framework,frameworkArgs,initialize,log,running,searchCandidates,shutdownHandlers,splashStreamRegistration
hcls InitialBundle,StartupEventListener

CLSS public abstract interface org.eclipse.osgi.framework.console.CommandInterpreter
meth public abstract java.lang.Object execute(java.lang.String)
meth public abstract java.lang.String nextArgument()
meth public abstract void print(java.lang.Object)
meth public abstract void printBundleResource(org.osgi.framework.Bundle,java.lang.String)
meth public abstract void printDictionary(java.util.Dictionary<?,?>,java.lang.String)
meth public abstract void printStackTrace(java.lang.Throwable)
meth public abstract void println()
meth public abstract void println(java.lang.Object)

CLSS public abstract interface org.eclipse.osgi.framework.console.CommandProvider
meth public abstract java.lang.String getHelp()

CLSS public abstract org.eclipse.osgi.framework.console.ConsoleSession
cons public init()
intf org.osgi.framework.ServiceFactory<java.lang.Object>
meth protected abstract void doClose()
meth public abstract java.io.InputStream getInput()
meth public abstract java.io.OutputStream getOutput()
meth public final java.lang.Object getService(org.osgi.framework.Bundle,org.osgi.framework.ServiceRegistration<java.lang.Object>)
meth public final void close()
meth public final void ungetService(org.osgi.framework.Bundle,org.osgi.framework.ServiceRegistration<java.lang.Object>,java.lang.Object)
supr java.lang.Object
hfds sessionRegistration

CLSS public org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap<? extends {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0},? extends {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1}>)
intf java.util.Map<{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0},{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1}>
meth public <%0 extends {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0}> void putAll({%%0}[])
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Collection<{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0},{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1}>> entrySet()
meth public java.util.Set<{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0},? extends {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1}>)
meth public {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1} get(java.lang.Object)
meth public {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1} put({org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%0},{org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1})
meth public {org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds emptyArray,entries
hcls Entry,Snapshot

CLSS public abstract interface org.eclipse.osgi.framework.eventmgr.EventDispatcher<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
meth public abstract void dispatchEvent({org.eclipse.osgi.framework.eventmgr.EventDispatcher%0},{org.eclipse.osgi.framework.eventmgr.EventDispatcher%1},int,{org.eclipse.osgi.framework.eventmgr.EventDispatcher%2})

CLSS public org.eclipse.osgi.framework.eventmgr.EventListeners<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
meth public void addListener({org.eclipse.osgi.framework.eventmgr.EventListeners%0},{org.eclipse.osgi.framework.eventmgr.EventListeners%1})
meth public void removeAllListeners()
meth public void removeListener({org.eclipse.osgi.framework.eventmgr.EventListeners%0})
supr java.lang.Object
hfds list

CLSS public org.eclipse.osgi.framework.eventmgr.EventManager
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.ThreadGroup)
fld protected final java.lang.String threadName
fld protected final java.lang.ThreadGroup threadGroup
meth public void close()
supr java.lang.Object
hfds DEBUG,closed,thread
hcls EventThread

CLSS public org.eclipse.osgi.framework.eventmgr.ListenerQueue<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init(org.eclipse.osgi.framework.eventmgr.EventManager)
fld protected final org.eclipse.osgi.framework.eventmgr.EventManager manager
meth public void dispatchEventAsynchronous(int,{org.eclipse.osgi.framework.eventmgr.ListenerQueue%2})
meth public void dispatchEventSynchronous(int,{org.eclipse.osgi.framework.eventmgr.ListenerQueue%2})
meth public void queueListeners(java.util.Set<java.util.Map$Entry<{org.eclipse.osgi.framework.eventmgr.ListenerQueue%0},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%1}>>,org.eclipse.osgi.framework.eventmgr.EventDispatcher<{org.eclipse.osgi.framework.eventmgr.ListenerQueue%0},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%1},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%2}>)
meth public void queueListeners(org.eclipse.osgi.framework.eventmgr.EventListeners<{org.eclipse.osgi.framework.eventmgr.ListenerQueue%0},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%1}>,org.eclipse.osgi.framework.eventmgr.EventDispatcher<{org.eclipse.osgi.framework.eventmgr.ListenerQueue%0},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%1},{org.eclipse.osgi.framework.eventmgr.ListenerQueue%2}>)
supr java.lang.Object
hfds queue,readOnly

CLSS public abstract interface org.eclipse.osgi.framework.log.FrameworkLog
fld public final static java.lang.String SERVICE_PERFORMANCE = "performance"
meth public abstract java.io.File getFile()
meth public abstract void close()
meth public abstract void log(org.eclipse.osgi.framework.log.FrameworkLogEntry)
meth public abstract void log(org.osgi.framework.FrameworkEvent)
meth public abstract void setConsoleLog(boolean)
meth public abstract void setFile(java.io.File,boolean) throws java.io.IOException
meth public abstract void setWriter(java.io.Writer,boolean)

CLSS public org.eclipse.osgi.framework.log.FrameworkLogEntry
cons public init(java.lang.Object,java.lang.String,int,int,java.lang.String,int,java.lang.Throwable,org.eclipse.osgi.framework.log.FrameworkLogEntry[])
cons public init(java.lang.String,int,int,java.lang.String,int,java.lang.Throwable,org.eclipse.osgi.framework.log.FrameworkLogEntry[])
cons public init(java.lang.String,java.lang.String,int,java.lang.Throwable,org.eclipse.osgi.framework.log.FrameworkLogEntry[])
fld public final static int CANCEL = 8
fld public final static int ERROR = 4
fld public final static int INFO = 1
fld public final static int OK = 0
fld public final static int WARNING = 2
meth public int getBundleCode()
meth public int getSeverity()
meth public int getStackCode()
meth public java.lang.Object getContext()
meth public java.lang.String getEntry()
meth public java.lang.String getMessage()
meth public java.lang.Throwable getThrowable()
meth public org.eclipse.osgi.framework.log.FrameworkLogEntry[] getChildren()
supr java.lang.Object
hfds bundleCode,children,context,entry,message,severity,stackCode,throwable

CLSS public org.eclipse.osgi.framework.util.ArrayMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(int)
cons public init(java.util.List<{org.eclipse.osgi.framework.util.ArrayMap%0}>,java.util.List<{org.eclipse.osgi.framework.util.ArrayMap%1}>)
intf java.util.Collection<{org.eclipse.osgi.framework.util.ArrayMap%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({org.eclipse.osgi.framework.util.ArrayMap%0})
meth public boolean addAll(java.util.Collection<? extends {org.eclipse.osgi.framework.util.ArrayMap%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int size()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{org.eclipse.osgi.framework.util.ArrayMap%0}> iterator()
meth public java.util.List<{org.eclipse.osgi.framework.util.ArrayMap%0}> getKeys()
meth public java.util.List<{org.eclipse.osgi.framework.util.ArrayMap%1}> getValues()
meth public void clear()
meth public void put({org.eclipse.osgi.framework.util.ArrayMap%0},{org.eclipse.osgi.framework.util.ArrayMap%1})
meth public void sort(java.util.Comparator<{org.eclipse.osgi.framework.util.ArrayMap%0}>)
meth public {org.eclipse.osgi.framework.util.ArrayMap%0} getKey(int)
meth public {org.eclipse.osgi.framework.util.ArrayMap%1} get({org.eclipse.osgi.framework.util.ArrayMap%0})
meth public {org.eclipse.osgi.framework.util.ArrayMap%1} getValue(int)
supr java.lang.Object
hfds keys,values

CLSS public org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Dictionary<? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>)
cons public init(java.util.Map<? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>)
intf java.util.Map<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>
meth protected static int initialCapacity(int)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}> values()
meth public java.util.Dictionary<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}> asUnmodifiableDictionary()
meth public java.util.Enumeration<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0}> keys()
meth public java.util.Enumeration<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}> elements()
meth public java.util.Map<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}> asUnmodifiableMap()
meth public java.util.Set<java.util.Map$Entry<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>> entrySet()
meth public java.util.Set<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0}> keySet()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Dictionary<{%%0},{%%1}> unmodifiableDictionary(java.util.Dictionary<? extends {%%0},? extends {%%1}>)
meth public static java.lang.Object findCommonKeyIndex(java.lang.String)
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},? extends {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>)
meth public {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1} get(java.lang.Object)
meth public {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1} put({org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1})
meth public {org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1} remove(java.lang.Object)
supr java.util.Dictionary<{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%0},{org.eclipse.osgi.framework.util.CaseInsensitiveDictionaryMap%1}>
hfds KEY_BUNDLE_ACTIVATIONPOLICY,KEY_BUNDLE_ACTIVATOR,KEY_BUNDLE_CLASSPATH,KEY_BUNDLE_DESCRIPTION,KEY_BUNDLE_DYNAMICIMPORT_PACKAGE,KEY_BUNDLE_EXPORT_PACKAGE,KEY_BUNDLE_FRAGMENT_HOST,KEY_BUNDLE_IMPORT_PACKAGE,KEY_BUNDLE_LICENSE,KEY_BUNDLE_LOCALIZATION,KEY_BUNDLE_MANIFESTVERSION,KEY_BUNDLE_NAME,KEY_BUNDLE_NATIVECODE,KEY_BUNDLE_PROVIDE_CAPABILITY,KEY_BUNDLE_REQUIREDEXECUTIONENVIRONMENT,KEY_BUNDLE_REQUIRE_BUNDLE,KEY_BUNDLE_REQUIRE_CAPABILITY,KEY_BUNDLE_SCM,KEY_BUNDLE_SYMBOLICNAME,KEY_BUNDLE_VENDOR,KEY_BUNDLE_VERSION,KEY_COMPONENT_ID,KEY_COMPONENT_NAME,KEY_EVENT_FILTER,KEY_EVENT_TOPICS,KEY_JAR_MANIFESTVERSION,KEY_JMX_OBJECTNAME,KEY_METATYPE_FACTORY_PID,KEY_METATYPE_PID,KEY_SERVICE_BUNDLE_ID,KEY_SERVICE_CHANGECOUNT,KEY_SERVICE_DESCRIPTION,KEY_SERVICE_ID,KEY_SERVICE_OBJECTCLASS,KEY_SERVICE_PID,KEY_SERVICE_RANKING,KEY_SERVICE_SCOPE,KEY_SERVICE_VENDER,entrySet,keySet,map
hcls CaseInsensitiveKey,CaseInsentiveEntry,EntryIterator,EntrySet,KeyIterator,KeySet,UnmodifiableDictionary

CLSS public org.eclipse.osgi.framework.util.FilePath
cons public init(java.io.File)
cons public init(java.lang.String)
meth public boolean hasTrailingSlash()
meth public boolean isAbsolute()
meth public java.lang.String getDevice()
meth public java.lang.String makeRelative(org.eclipse.osgi.framework.util.FilePath)
meth public java.lang.String toString()
meth public java.lang.String[] getSegments()
supr java.lang.Object
hfds CURRENT_DIR,DEVICE_SEPARATOR,HAS_LEADING,HAS_TRAILING,NO_SEGMENTS,PARENT_DIR,SEPARATOR,UNC_SLASHES,WINDOWS,device,flags,segments

CLSS public org.eclipse.osgi.framework.util.Headers<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(int)
cons public init(java.util.Dictionary<? extends {org.eclipse.osgi.framework.util.Headers%0},? extends {org.eclipse.osgi.framework.util.Headers%1}>)
intf java.util.Map<{org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.eclipse.osgi.framework.util.Headers%1}> values()
meth public java.util.Enumeration<{org.eclipse.osgi.framework.util.Headers%0}> keys()
meth public java.util.Enumeration<{org.eclipse.osgi.framework.util.Headers%1}> elements()
meth public java.util.Set<java.util.Map$Entry<{org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1}>> entrySet()
meth public java.util.Set<{org.eclipse.osgi.framework.util.Headers%0}> keySet()
meth public static org.eclipse.osgi.framework.util.Headers<java.lang.String,java.lang.String> parseManifest(java.io.InputStream) throws org.osgi.framework.BundleException
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.eclipse.osgi.framework.util.Headers%0},? extends {org.eclipse.osgi.framework.util.Headers%1}>)
meth public void setReadOnly()
meth public {org.eclipse.osgi.framework.util.Headers%1} get(java.lang.Object)
meth public {org.eclipse.osgi.framework.util.Headers%1} put({org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1})
meth public {org.eclipse.osgi.framework.util.Headers%1} remove(java.lang.Object)
meth public {org.eclipse.osgi.framework.util.Headers%1} set({org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1})
meth public {org.eclipse.osgi.framework.util.Headers%1} set({org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1},boolean)
supr java.util.Dictionary<{org.eclipse.osgi.framework.util.Headers%0},{org.eclipse.osgi.framework.util.Headers%1}>
hfds headers,readOnly,size,values
hcls ArrayEnumeration

CLSS public abstract interface org.eclipse.osgi.framework.util.KeyedElement
meth public abstract boolean compare(org.eclipse.osgi.framework.util.KeyedElement)
meth public abstract int getKeyHashCode()
meth public abstract java.lang.Object getKey()

CLSS public org.eclipse.osgi.framework.util.ObjectPool
cons public init()
meth public static <%0 extends java.lang.Object> {%%0} intern({%%0})
supr java.lang.Object
hfds DEBUG_OBJECTPOOL_ADDS,DEBUG_OBJECTPOOL_DUPS,OPTION_DEBUG_OBJECTPOOL_ADDS,OPTION_DEBUG_OBJECTPOOL_DUPS,objectCache

CLSS public org.eclipse.osgi.framework.util.SecureAction
meth public !varargs void start(org.eclipse.osgi.container.Module,org.eclipse.osgi.container.Module$StartOptions[]) throws org.osgi.framework.BundleException
meth public <%0 extends java.lang.Object> {%%0} getService(org.osgi.framework.ServiceReference<{%%0}>,org.osgi.framework.BundleContext)
meth public boolean exists(java.io.File)
meth public boolean isDirectory(java.io.File)
meth public boolean mkdirs(java.io.File)
meth public java.io.File getAbsoluteFile(java.io.File)
meth public java.io.File getCanonicalFile(java.io.File) throws java.io.IOException
meth public java.io.FileInputStream getFileInputStream(java.io.File) throws java.io.FileNotFoundException
meth public java.io.FileOutputStream getFileOutputStream(java.io.File,boolean) throws java.io.FileNotFoundException
meth public java.lang.Class<?> forName(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Class<?> loadSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.String getCanonicalPath(java.io.File) throws java.io.IOException
meth public java.lang.String getLocation(org.osgi.framework.Bundle)
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String[] list(java.io.File)
meth public java.lang.Thread createThread(java.lang.Runnable,java.lang.String,java.lang.ClassLoader)
meth public java.net.URL getURL(java.lang.String,java.lang.String,int,java.lang.String,java.net.URLStreamHandler) throws java.net.MalformedURLException
meth public java.util.Properties getProperties()
meth public java.util.zip.ZipFile getZipFile(java.io.File,boolean) throws java.io.IOException
meth public long lastModified(java.io.File)
meth public long length(java.io.File)
meth public org.osgi.framework.BundleContext getContext(org.osgi.framework.Bundle)
meth public static java.security.PrivilegedAction<org.eclipse.osgi.framework.util.SecureAction> createSecureAction()
meth public void open(org.osgi.util.tracker.ServiceTracker<?,?>)
supr java.lang.Object
hfds controlContext
hcls BootClassLoaderHolder

CLSS public org.eclipse.osgi.framework.util.ThreadInfoReport
cons public init(java.lang.String)
meth public static java.lang.String getThreadDump(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface org.eclipse.osgi.service.datalocation.Location
fld public final static java.lang.String CONFIGURATION_AREA_TYPE = "osgi.configuration.area"
fld public final static java.lang.String CONFIGURATION_FILTER
fld public final static java.lang.String ECLIPSE_HOME_FILTER
fld public final static java.lang.String ECLIPSE_HOME_LOCATION_TYPE = "eclipse.home.location"
fld public final static java.lang.String INSTALL_AREA_TYPE = "osgi.install.area"
fld public final static java.lang.String INSTALL_FILTER
fld public final static java.lang.String INSTANCE_AREA_TYPE = "osgi.instance.area"
fld public final static java.lang.String INSTANCE_FILTER
fld public final static java.lang.String SERVICE_PROPERTY_DEFAULT_URL = "defaultUrl"
fld public final static java.lang.String SERVICE_PROPERTY_TYPE = "type"
fld public final static java.lang.String SERVICE_PROPERTY_URL = "url"
fld public final static java.lang.String USER_AREA_TYPE = "osgi.user.area"
fld public final static java.lang.String USER_FILTER
meth public abstract boolean allowsDefault()
meth public abstract boolean isLocked() throws java.io.IOException
meth public abstract boolean isReadOnly()
meth public abstract boolean isSet()
meth public abstract boolean lock() throws java.io.IOException
meth public abstract boolean set(java.net.URL,boolean) throws java.io.IOException
meth public abstract boolean set(java.net.URL,boolean,java.lang.String) throws java.io.IOException
meth public abstract boolean setURL(java.net.URL,boolean)
meth public abstract java.net.URL getDataArea(java.lang.String) throws java.io.IOException
meth public abstract java.net.URL getDefault()
meth public abstract java.net.URL getURL()
meth public abstract org.eclipse.osgi.service.datalocation.Location createLocation(org.eclipse.osgi.service.datalocation.Location,java.net.URL,boolean)
meth public abstract org.eclipse.osgi.service.datalocation.Location getParentLocation()
meth public abstract void release()

CLSS public abstract interface org.eclipse.osgi.service.debug.DebugOptions
fld public final static java.lang.String LISTENER_SYMBOLICNAME = "listener.symbolic.name"
meth public abstract boolean getBooleanOption(java.lang.String,boolean)
meth public abstract boolean isDebugEnabled()
meth public abstract int getIntegerOption(java.lang.String,int)
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getOption(java.lang.String)
meth public abstract java.lang.String getOption(java.lang.String,java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> getOptions()
meth public abstract org.eclipse.osgi.service.debug.DebugTrace newDebugTrace(java.lang.String)
meth public abstract org.eclipse.osgi.service.debug.DebugTrace newDebugTrace(java.lang.String,java.lang.Class<?>)
meth public abstract void removeOption(java.lang.String)
meth public abstract void setDebugEnabled(boolean)
meth public abstract void setFile(java.io.File)
meth public abstract void setOption(java.lang.String,java.lang.String)
meth public abstract void setOptions(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface org.eclipse.osgi.service.debug.DebugOptionsListener
intf java.util.EventListener
meth public abstract void optionsChanged(org.eclipse.osgi.service.debug.DebugOptions)

CLSS public abstract interface org.eclipse.osgi.service.debug.DebugTrace
meth public abstract void trace(java.lang.String,java.lang.String)
meth public abstract void trace(java.lang.String,java.lang.String,java.lang.Throwable)
meth public abstract void traceDumpStack(java.lang.String)
meth public abstract void traceEntry(java.lang.String)
meth public abstract void traceEntry(java.lang.String,java.lang.Object)
meth public abstract void traceEntry(java.lang.String,java.lang.Object[])
meth public abstract void traceExit(java.lang.String)
meth public abstract void traceExit(java.lang.String,java.lang.Object)

CLSS public abstract interface org.eclipse.osgi.service.environment.Constants
fld public final static java.lang.String ARCH_AMD64 = "x86_64"
fld public final static java.lang.String ARCH_IA64 = "ia64"
fld public final static java.lang.String ARCH_IA64_32 = "ia64_32"
fld public final static java.lang.String ARCH_PA_RISC = "PA_RISC"
fld public final static java.lang.String ARCH_PPC = "ppc"
fld public final static java.lang.String ARCH_PPC64 = "ppc64"
fld public final static java.lang.String ARCH_SPARC = "sparc"
fld public final static java.lang.String ARCH_X86 = "x86"
fld public final static java.lang.String ARCH_X86_64 = "x86_64"
fld public final static java.lang.String OS_AIX = "aix"
fld public final static java.lang.String OS_EPOC32 = "epoc32"
fld public final static java.lang.String OS_FREEBSD = "freebsd"
fld public final static java.lang.String OS_HPUX = "hpux"
fld public final static java.lang.String OS_LINUX = "linux"
fld public final static java.lang.String OS_MACOSX = "macosx"
fld public final static java.lang.String OS_OS390 = "os/390"
fld public final static java.lang.String OS_OS400 = "os/400"
fld public final static java.lang.String OS_QNX = "qnx"
fld public final static java.lang.String OS_SOLARIS = "solaris"
fld public final static java.lang.String OS_UNKNOWN = "unknown"
fld public final static java.lang.String OS_WIN32 = "win32"
fld public final static java.lang.String OS_ZOS = "z/os"
fld public final static java.lang.String WS_CARBON = "carbon"
fld public final static java.lang.String WS_COCOA = "cocoa"
fld public final static java.lang.String WS_GTK = "gtk"
fld public final static java.lang.String WS_MOTIF = "motif"
fld public final static java.lang.String WS_PHOTON = "photon"
fld public final static java.lang.String WS_S60 = "s60"
fld public final static java.lang.String WS_UNKNOWN = "unknown"
fld public final static java.lang.String WS_WIN32 = "win32"
fld public final static java.lang.String WS_WPF = "wpf"

CLSS public abstract interface org.eclipse.osgi.service.environment.EnvironmentInfo
meth public abstract boolean inDebugMode()
meth public abstract boolean inDevelopmentMode()
meth public abstract java.lang.String getNL()
meth public abstract java.lang.String getOS()
meth public abstract java.lang.String getOSArch()
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.lang.String getWS()
meth public abstract java.lang.String setProperty(java.lang.String,java.lang.String)
meth public abstract java.lang.String[] getCommandLineArgs()
meth public abstract java.lang.String[] getFrameworkArgs()
meth public abstract java.lang.String[] getNonFrameworkArgs()

CLSS public abstract interface org.eclipse.osgi.service.localization.BundleLocalization
meth public abstract java.util.ResourceBundle getLocalization(org.osgi.framework.Bundle,java.lang.String)

CLSS public abstract interface org.eclipse.osgi.service.localization.LocaleProvider
meth public abstract java.util.Locale getLocale()

CLSS public org.eclipse.osgi.service.pluginconversion.PluginConversionException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds cause,serialVersionUID

CLSS public abstract interface org.eclipse.osgi.service.pluginconversion.PluginConverter
meth public abstract java.io.File convertManifest(java.io.File,java.io.File,boolean,java.lang.String,boolean,java.util.Dictionary<java.lang.String,java.lang.String>) throws org.eclipse.osgi.service.pluginconversion.PluginConversionException
meth public abstract java.util.Dictionary<java.lang.String,java.lang.String> convertManifest(java.io.File,boolean,java.lang.String,boolean,java.util.Dictionary<java.lang.String,java.lang.String>) throws org.eclipse.osgi.service.pluginconversion.PluginConversionException
meth public abstract void writeManifest(java.io.File,java.util.Dictionary<java.lang.String,java.lang.String>,boolean) throws org.eclipse.osgi.service.pluginconversion.PluginConversionException

CLSS public abstract interface org.eclipse.osgi.service.resolver.BaseDescription
meth public abstract java.lang.Object getUserObject()
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getDeclaredAttributes()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getDeclaredDirectives()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getSupplier()
meth public abstract org.osgi.framework.Version getVersion()
meth public abstract org.osgi.framework.wiring.BundleCapability getCapability()
meth public abstract void setUserObject(java.lang.Object)

CLSS public abstract interface org.eclipse.osgi.service.resolver.BundleDelta
fld public final static int ADDED = 1
fld public final static int LINKAGE_CHANGED = 32
fld public final static int OPTIONAL_LINKAGE_CHANGED = 64
fld public final static int REMOVAL_COMPLETE = 256
fld public final static int REMOVAL_PENDING = 128
fld public final static int REMOVED = 2
fld public final static int RESOLVED = 8
fld public final static int UNRESOLVED = 16
fld public final static int UPDATED = 4
intf java.lang.Comparable<org.eclipse.osgi.service.resolver.BundleDelta>
meth public abstract int compareTo(org.eclipse.osgi.service.resolver.BundleDelta)
meth public abstract int getType()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundle()

CLSS public abstract interface org.eclipse.osgi.service.resolver.BundleDescription
intf org.eclipse.osgi.service.resolver.BaseDescription
intf org.osgi.framework.wiring.BundleRevision
meth public abstract boolean attachFragments()
meth public abstract boolean dynamicFragments()
meth public abstract boolean hasDynamicImports()
meth public abstract boolean isRemovalPending()
meth public abstract boolean isResolved()
meth public abstract boolean isSingleton()
meth public abstract java.lang.String getLocation()
meth public abstract java.lang.String getPlatformFilter()
meth public abstract java.lang.String getSymbolicName()
meth public abstract java.lang.String toString()
meth public abstract java.lang.String[] getExecutionEnvironments()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getAttributes()
meth public abstract long getBundleId()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getDependents()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getFragments()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getResolvedRequires()
meth public abstract org.eclipse.osgi.service.resolver.BundleSpecification[] getRequiredBundles()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getExportPackages()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getResolvedImports()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getSelectedExports()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getSubstitutedExports()
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription[] getGenericCapabilities()
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription[] getResolvedGenericRequires()
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription[] getSelectedGenericCapabilities()
meth public abstract org.eclipse.osgi.service.resolver.GenericSpecification[] getGenericRequires()
meth public abstract org.eclipse.osgi.service.resolver.HostSpecification getHost()
meth public abstract org.eclipse.osgi.service.resolver.ImportPackageSpecification[] getAddedDynamicImportPackages()
meth public abstract org.eclipse.osgi.service.resolver.ImportPackageSpecification[] getImportPackages()
meth public abstract org.eclipse.osgi.service.resolver.NativeCodeSpecification getNativeCodeSpecification()
meth public abstract org.eclipse.osgi.service.resolver.State getContainingState()

CLSS public abstract interface org.eclipse.osgi.service.resolver.BundleSpecification
intf org.eclipse.osgi.service.resolver.VersionConstraint
meth public abstract boolean isExported()
meth public abstract boolean isOptional()

CLSS public final org.eclipse.osgi.service.resolver.DisabledInfo
cons public init(java.lang.String,java.lang.String,org.eclipse.osgi.service.resolver.BundleDescription)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getMessage()
meth public java.lang.String getPolicyName()
meth public org.eclipse.osgi.service.resolver.BundleDescription getBundle()
supr java.lang.Object
hfds bundle,message,policyName

CLSS public abstract interface org.eclipse.osgi.service.resolver.ExportPackageDescription
intf org.eclipse.osgi.service.resolver.BaseDescription
meth public abstract boolean isRoot()
meth public abstract java.lang.Object getDirective(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getAttributes()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getDirectives()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getExporter()

CLSS public abstract interface org.eclipse.osgi.service.resolver.GenericDescription
fld public final static java.lang.String DEFAULT_TYPE = "generic"
intf org.eclipse.osgi.service.resolver.BaseDescription
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()
meth public abstract java.util.Dictionary<java.lang.String,java.lang.Object> getAttributes()
meth public abstract org.osgi.framework.Version getVersion()

CLSS public abstract interface org.eclipse.osgi.service.resolver.GenericSpecification
fld public final static int RESOLUTION_MULTIPLE = 2
fld public final static int RESOLUTION_OPTIONAL = 1
intf org.eclipse.osgi.service.resolver.VersionConstraint
meth public abstract int getResolution()
meth public abstract java.lang.String getMatchingFilter()
meth public abstract java.lang.String getType()
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription[] getSuppliers()

CLSS public abstract interface org.eclipse.osgi.service.resolver.HostSpecification
intf org.eclipse.osgi.service.resolver.VersionConstraint
meth public abstract boolean isMultiHost()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getHosts()

CLSS public abstract interface org.eclipse.osgi.service.resolver.ImportPackageSpecification
fld public final static java.lang.String RESOLUTION_DYNAMIC = "dynamic"
fld public final static java.lang.String RESOLUTION_OPTIONAL = "optional"
fld public final static java.lang.String RESOLUTION_STATIC = "static"
intf org.eclipse.osgi.service.resolver.VersionConstraint
meth public abstract java.lang.Object getDirective(java.lang.String)
meth public abstract java.lang.String getBundleSymbolicName()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getAttributes()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getDirectives()
meth public abstract org.eclipse.osgi.service.resolver.VersionRange getBundleVersionRange()

CLSS public abstract interface org.eclipse.osgi.service.resolver.NativeCodeDescription
intf java.lang.Comparable<org.eclipse.osgi.service.resolver.NativeCodeDescription>
intf org.eclipse.osgi.service.resolver.BaseDescription
meth public abstract boolean hasInvalidNativePaths()
meth public abstract int compareTo(org.eclipse.osgi.service.resolver.NativeCodeDescription)
meth public abstract java.lang.String[] getLanguages()
meth public abstract java.lang.String[] getNativePaths()
meth public abstract java.lang.String[] getOSNames()
meth public abstract java.lang.String[] getProcessors()
meth public abstract org.eclipse.osgi.service.resolver.VersionRange[] getOSVersions()
meth public abstract org.osgi.framework.Filter getFilter()

CLSS public abstract interface org.eclipse.osgi.service.resolver.NativeCodeSpecification
intf org.eclipse.osgi.service.resolver.VersionConstraint
meth public abstract boolean isOptional()
meth public abstract org.eclipse.osgi.service.resolver.NativeCodeDescription[] getPossibleSuppliers()

CLSS public abstract interface org.eclipse.osgi.service.resolver.PlatformAdmin
meth public abstract org.eclipse.osgi.service.resolver.Resolver createResolver()
meth public abstract org.eclipse.osgi.service.resolver.Resolver getResolver()
meth public abstract org.eclipse.osgi.service.resolver.State getState()
meth public abstract org.eclipse.osgi.service.resolver.State getState(boolean)
meth public abstract org.eclipse.osgi.service.resolver.StateHelper getStateHelper()
meth public abstract org.eclipse.osgi.service.resolver.StateObjectFactory getFactory()
meth public abstract void addDisabledInfo(org.eclipse.osgi.service.resolver.DisabledInfo)
meth public abstract void commit(org.eclipse.osgi.service.resolver.State) throws org.osgi.framework.BundleException
meth public abstract void removeDisabledInfo(org.eclipse.osgi.service.resolver.DisabledInfo)

CLSS public abstract interface org.eclipse.osgi.service.resolver.Resolver
meth public abstract java.util.Comparator<org.eclipse.osgi.service.resolver.BaseDescription> getSelectionPolicy()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription resolveDynamicImport(org.eclipse.osgi.service.resolver.BundleDescription,java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.State getState()
meth public abstract void bundleAdded(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract void bundleRemoved(org.eclipse.osgi.service.resolver.BundleDescription,boolean)
meth public abstract void bundleUpdated(org.eclipse.osgi.service.resolver.BundleDescription,org.eclipse.osgi.service.resolver.BundleDescription,boolean)
meth public abstract void flush()
meth public abstract void resolve(org.eclipse.osgi.service.resolver.BundleDescription[],java.util.Dictionary<java.lang.Object,java.lang.Object>[])
meth public abstract void setSelectionPolicy(java.util.Comparator<org.eclipse.osgi.service.resolver.BaseDescription>)
meth public abstract void setState(org.eclipse.osgi.service.resolver.State)

CLSS public abstract interface org.eclipse.osgi.service.resolver.ResolverError
fld public final static int DISABLED_BUNDLE = 262144
fld public final static int EXPORT_PACKAGE_PERMISSION = 256
fld public final static int FRAGMENT_BUNDLE_PERMISSION = 4096
fld public final static int FRAGMENT_CONFLICT = 16
fld public final static int HOST_BUNDLE_PERMISSION = 2048
fld public final static int IMPORT_PACKAGE_PERMISSION = 128
fld public final static int IMPORT_PACKAGE_USES_CONFLICT = 32
fld public final static int INVALID_NATIVECODE_PATHS = 131072
fld public final static int MISSING_EXECUTION_ENVIRONMENT = 16384
fld public final static int MISSING_FRAGMENT_HOST = 4
fld public final static int MISSING_GENERIC_CAPABILITY = 32768
fld public final static int MISSING_IMPORT_PACKAGE = 1
fld public final static int MISSING_REQUIRE_BUNDLE = 2
fld public final static int NO_NATIVECODE_MATCH = 65536
fld public final static int PLATFORM_FILTER = 8192
fld public final static int PROVIDE_BUNDLE_PERMISSION = 1024
fld public final static int PROVIDE_CAPABILITY_PERMISSION = 1048576
fld public final static int REQUIRE_BUNDLE_PERMISSION = 512
fld public final static int REQUIRE_BUNDLE_USES_CONFLICT = 64
fld public final static int REQUIRE_CAPABILITY_PERMISSION = 524288
fld public final static int SINGLETON_SELECTION = 8
meth public abstract int getType()
meth public abstract java.lang.String getData()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundle()
meth public abstract org.eclipse.osgi.service.resolver.VersionConstraint getUnsatisfiedConstraint()

CLSS public org.eclipse.osgi.service.resolver.ResolverHookException
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface org.eclipse.osgi.service.resolver.State
meth public abstract boolean addBundle(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract boolean isEmpty()
meth public abstract boolean isResolved()
meth public abstract boolean removeBundle(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract boolean setPlatformProperties(java.util.Dictionary<?,?>)
meth public abstract boolean setPlatformProperties(java.util.Dictionary<?,?>[])
meth public abstract boolean updateBundle(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract java.util.Collection<org.eclipse.osgi.service.resolver.BundleDescription> getDependencyClosure(java.util.Collection<org.eclipse.osgi.service.resolver.BundleDescription>)
meth public abstract java.util.Dictionary[] getPlatformProperties()
meth public abstract long getHighestBundleId()
meth public abstract long getTimeStamp()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundle(java.lang.String,org.osgi.framework.Version)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundle(long)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundleByLocation(java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription removeBundle(long)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getBundles()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getBundles(java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getDisabledBundles()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getRemovalPending()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getResolvedBundles()
meth public abstract org.eclipse.osgi.service.resolver.DisabledInfo getDisabledInfo(org.eclipse.osgi.service.resolver.BundleDescription,java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.DisabledInfo[] getDisabledInfos(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription linkDynamicImport(org.eclipse.osgi.service.resolver.BundleDescription,java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getExportedPackages()
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getSystemPackages()
meth public abstract org.eclipse.osgi.service.resolver.Resolver getResolver()
meth public abstract org.eclipse.osgi.service.resolver.ResolverError[] getResolverErrors(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.StateDelta compare(org.eclipse.osgi.service.resolver.State) throws org.osgi.framework.BundleException
meth public abstract org.eclipse.osgi.service.resolver.StateDelta getChanges()
meth public abstract org.eclipse.osgi.service.resolver.StateDelta resolve()
meth public abstract org.eclipse.osgi.service.resolver.StateDelta resolve(boolean)
meth public abstract org.eclipse.osgi.service.resolver.StateDelta resolve(org.eclipse.osgi.service.resolver.BundleDescription[])
meth public abstract org.eclipse.osgi.service.resolver.StateDelta resolve(org.eclipse.osgi.service.resolver.BundleDescription[],boolean)
meth public abstract org.eclipse.osgi.service.resolver.StateHelper getStateHelper()
meth public abstract org.eclipse.osgi.service.resolver.StateObjectFactory getFactory()
meth public abstract void addDisabledInfo(org.eclipse.osgi.service.resolver.DisabledInfo)
meth public abstract void addDynamicImportPackages(org.eclipse.osgi.service.resolver.BundleDescription,org.eclipse.osgi.service.resolver.ImportPackageSpecification[])
meth public abstract void addResolverError(org.eclipse.osgi.service.resolver.BundleDescription,int,java.lang.String,org.eclipse.osgi.service.resolver.VersionConstraint)
meth public abstract void removeBundleComplete(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract void removeDisabledInfo(org.eclipse.osgi.service.resolver.DisabledInfo)
meth public abstract void removeResolverErrors(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract void resolveBundle(org.eclipse.osgi.service.resolver.BundleDescription,boolean,org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[])
meth public abstract void resolveBundle(org.eclipse.osgi.service.resolver.BundleDescription,boolean,org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[])
meth public abstract void resolveBundle(org.eclipse.osgi.service.resolver.BundleDescription,boolean,org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.GenericDescription[],org.eclipse.osgi.service.resolver.BundleDescription[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],org.eclipse.osgi.service.resolver.GenericDescription[],java.util.Map<java.lang.String,java.util.List<org.eclipse.osgi.service.resolver.StateWire>>)
meth public abstract void resolveConstraint(org.eclipse.osgi.service.resolver.VersionConstraint,org.eclipse.osgi.service.resolver.BaseDescription)
meth public abstract void setNativePathsInvalid(org.eclipse.osgi.service.resolver.NativeCodeDescription,boolean)
meth public abstract void setOverrides(java.lang.Object)
meth public abstract void setResolver(org.eclipse.osgi.service.resolver.Resolver)
meth public abstract void setResolverHookFactory(org.osgi.framework.hooks.resolver.ResolverHookFactory)
meth public abstract void setTimeStamp(long)

CLSS public abstract interface org.eclipse.osgi.service.resolver.StateDelta
meth public abstract org.eclipse.osgi.service.resolver.BundleDelta[] getChanges()
meth public abstract org.eclipse.osgi.service.resolver.BundleDelta[] getChanges(int,boolean)
meth public abstract org.eclipse.osgi.service.resolver.ResolverHookException getResovlerHookException()
meth public abstract org.eclipse.osgi.service.resolver.State getState()

CLSS public abstract interface org.eclipse.osgi.service.resolver.StateHelper
fld public final static int ACCESS_DISCOURAGED = 2
fld public final static int ACCESS_ENCOURAGED = 1
fld public final static int VISIBLE_INCLUDE_ALL_HOST_WIRES = 2
fld public final static int VISIBLE_INCLUDE_EE_PACKAGES = 1
meth public abstract boolean isResolvable(org.eclipse.osgi.service.resolver.BundleSpecification)
meth public abstract boolean isResolvable(org.eclipse.osgi.service.resolver.HostSpecification)
meth public abstract boolean isResolvable(org.eclipse.osgi.service.resolver.ImportPackageSpecification)
meth public abstract int getAccessCode(org.eclipse.osgi.service.resolver.BundleDescription,org.eclipse.osgi.service.resolver.ExportPackageDescription)
meth public abstract java.lang.Object[][] sortBundles(org.eclipse.osgi.service.resolver.BundleDescription[])
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getDependentBundles(org.eclipse.osgi.service.resolver.BundleDescription[])
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription[] getPrerequisites(org.eclipse.osgi.service.resolver.BundleDescription[])
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getVisiblePackages(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription[] getVisiblePackages(org.eclipse.osgi.service.resolver.BundleDescription,int)
meth public abstract org.eclipse.osgi.service.resolver.VersionConstraint[] getUnsatisfiedConstraints(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.VersionConstraint[] getUnsatisfiedLeaves(org.eclipse.osgi.service.resolver.BundleDescription[])

CLSS public abstract interface org.eclipse.osgi.service.resolver.StateObjectFactory
fld public final static org.eclipse.osgi.service.resolver.StateObjectFactory defaultFactory
innr public static StateObjectFactoryProxy
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.BundleSpecification> createBundleSpecifications(java.lang.String)
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.ExportPackageDescription> createExportPackageDescriptions(java.lang.String)
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.GenericDescription> createGenericDescriptions(java.lang.String)
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.GenericSpecification> createGenericSpecifications(java.lang.String)
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.HostSpecification> createHostSpecifications(java.lang.String)
meth public abstract java.util.List<org.eclipse.osgi.service.resolver.ImportPackageSpecification> createImportPackageSpecifications(java.lang.String)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(java.util.Dictionary<java.lang.String,java.lang.String>,java.lang.String,long) throws org.osgi.framework.BundleException
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],boolean,boolean,boolean,java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[])
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],boolean,boolean,boolean,java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[],org.eclipse.osgi.service.resolver.NativeCodeSpecification)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[],org.eclipse.osgi.service.resolver.NativeCodeSpecification)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String[],boolean)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String[],boolean,boolean,boolean,java.lang.String,java.lang.String,org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[])
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(org.eclipse.osgi.service.resolver.State,java.util.Dictionary<java.lang.String,java.lang.String>,java.lang.String,long) throws org.osgi.framework.BundleException
meth public abstract org.eclipse.osgi.service.resolver.BundleSpecification createBundleSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,boolean,boolean)
meth public abstract org.eclipse.osgi.service.resolver.BundleSpecification createBundleSpecification(org.eclipse.osgi.service.resolver.BundleSpecification)
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription createExportPackageDescription(java.lang.String,org.osgi.framework.Version,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,?>,boolean,org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.ExportPackageDescription createExportPackageDescription(org.eclipse.osgi.service.resolver.ExportPackageDescription)
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription createGenericDescription(java.lang.String,java.lang.String,org.osgi.framework.Version,java.util.Map<java.lang.String,?>)
meth public abstract org.eclipse.osgi.service.resolver.GenericDescription createGenericDescription(java.lang.String,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,java.lang.String>,org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.GenericSpecification createGenericSpecification(java.lang.String,java.lang.String,java.lang.String,boolean,boolean) throws org.osgi.framework.InvalidSyntaxException
meth public abstract org.eclipse.osgi.service.resolver.HostSpecification createHostSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange)
meth public abstract org.eclipse.osgi.service.resolver.HostSpecification createHostSpecification(org.eclipse.osgi.service.resolver.HostSpecification)
meth public abstract org.eclipse.osgi.service.resolver.ImportPackageSpecification createImportPackageSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,?>,org.eclipse.osgi.service.resolver.BundleDescription)
meth public abstract org.eclipse.osgi.service.resolver.ImportPackageSpecification createImportPackageSpecification(org.eclipse.osgi.service.resolver.ImportPackageSpecification)
meth public abstract org.eclipse.osgi.service.resolver.NativeCodeDescription createNativeCodeDescription(java.lang.String[],java.lang.String[],java.lang.String[],org.eclipse.osgi.service.resolver.VersionRange[],java.lang.String[],java.lang.String) throws org.osgi.framework.InvalidSyntaxException
meth public abstract org.eclipse.osgi.service.resolver.NativeCodeSpecification createNativeCodeSpecification(org.eclipse.osgi.service.resolver.NativeCodeDescription[],boolean)
meth public abstract org.eclipse.osgi.service.resolver.State createState()
meth public abstract org.eclipse.osgi.service.resolver.State createState(boolean)
meth public abstract org.eclipse.osgi.service.resolver.State createState(org.eclipse.osgi.service.resolver.State)
meth public abstract org.eclipse.osgi.service.resolver.State readState(java.io.DataInputStream) throws java.io.IOException
meth public abstract org.eclipse.osgi.service.resolver.State readState(java.io.File) throws java.io.IOException
meth public abstract org.eclipse.osgi.service.resolver.State readState(java.io.InputStream) throws java.io.IOException
meth public abstract void writeState(org.eclipse.osgi.service.resolver.State,java.io.DataOutputStream) throws java.io.IOException
meth public abstract void writeState(org.eclipse.osgi.service.resolver.State,java.io.File) throws java.io.IOException
meth public abstract void writeState(org.eclipse.osgi.service.resolver.State,java.io.OutputStream) throws java.io.IOException

CLSS public static org.eclipse.osgi.service.resolver.StateObjectFactory$StateObjectFactoryProxy
 outer org.eclipse.osgi.service.resolver.StateObjectFactory
cons public init()
intf org.eclipse.osgi.service.resolver.StateObjectFactory
meth public java.util.List<org.eclipse.osgi.service.resolver.BundleSpecification> createBundleSpecifications(java.lang.String)
meth public java.util.List<org.eclipse.osgi.service.resolver.ExportPackageDescription> createExportPackageDescriptions(java.lang.String)
meth public java.util.List<org.eclipse.osgi.service.resolver.GenericDescription> createGenericDescriptions(java.lang.String)
meth public java.util.List<org.eclipse.osgi.service.resolver.GenericSpecification> createGenericSpecifications(java.lang.String)
meth public java.util.List<org.eclipse.osgi.service.resolver.HostSpecification> createHostSpecifications(java.lang.String)
meth public java.util.List<org.eclipse.osgi.service.resolver.ImportPackageSpecification> createImportPackageSpecifications(java.lang.String)
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(java.util.Dictionary<java.lang.String,java.lang.String>,java.lang.String,long) throws org.osgi.framework.BundleException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],boolean,boolean,boolean,java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[])
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],boolean,boolean,boolean,java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[],org.eclipse.osgi.service.resolver.NativeCodeSpecification)
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String,java.lang.String[],org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[],org.eclipse.osgi.service.resolver.NativeCodeSpecification)
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String[],boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,java.lang.String,org.osgi.framework.Version,java.lang.String,org.eclipse.osgi.service.resolver.BundleSpecification[],org.eclipse.osgi.service.resolver.HostSpecification,org.eclipse.osgi.service.resolver.ImportPackageSpecification[],org.eclipse.osgi.service.resolver.ExportPackageDescription[],java.lang.String[],boolean,boolean,boolean,java.lang.String,java.lang.String,org.eclipse.osgi.service.resolver.GenericSpecification[],org.eclipse.osgi.service.resolver.GenericDescription[])
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(long,org.eclipse.osgi.service.resolver.BundleDescription)
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(org.eclipse.osgi.service.resolver.BundleDescription)
meth public org.eclipse.osgi.service.resolver.BundleDescription createBundleDescription(org.eclipse.osgi.service.resolver.State,java.util.Dictionary<java.lang.String,java.lang.String>,java.lang.String,long) throws org.osgi.framework.BundleException
meth public org.eclipse.osgi.service.resolver.BundleSpecification createBundleSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,boolean,boolean)
meth public org.eclipse.osgi.service.resolver.BundleSpecification createBundleSpecification(org.eclipse.osgi.service.resolver.BundleSpecification)
meth public org.eclipse.osgi.service.resolver.ExportPackageDescription createExportPackageDescription(java.lang.String,org.osgi.framework.Version,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,?>,boolean,org.eclipse.osgi.service.resolver.BundleDescription)
meth public org.eclipse.osgi.service.resolver.ExportPackageDescription createExportPackageDescription(org.eclipse.osgi.service.resolver.ExportPackageDescription)
meth public org.eclipse.osgi.service.resolver.GenericDescription createGenericDescription(java.lang.String,java.lang.String,org.osgi.framework.Version,java.util.Map<java.lang.String,?>)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.GenericDescription createGenericDescription(java.lang.String,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,java.lang.String>,org.eclipse.osgi.service.resolver.BundleDescription)
meth public org.eclipse.osgi.service.resolver.GenericSpecification createGenericSpecification(java.lang.String,java.lang.String,java.lang.String,boolean,boolean) throws org.osgi.framework.InvalidSyntaxException
meth public org.eclipse.osgi.service.resolver.HostSpecification createHostSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange)
meth public org.eclipse.osgi.service.resolver.HostSpecification createHostSpecification(org.eclipse.osgi.service.resolver.HostSpecification)
meth public org.eclipse.osgi.service.resolver.ImportPackageSpecification createImportPackageSpecification(java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,java.lang.String,org.eclipse.osgi.service.resolver.VersionRange,java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,?>,org.eclipse.osgi.service.resolver.BundleDescription)
meth public org.eclipse.osgi.service.resolver.ImportPackageSpecification createImportPackageSpecification(org.eclipse.osgi.service.resolver.ImportPackageSpecification)
meth public org.eclipse.osgi.service.resolver.NativeCodeDescription createNativeCodeDescription(java.lang.String[],java.lang.String[],java.lang.String[],org.eclipse.osgi.service.resolver.VersionRange[],java.lang.String[],java.lang.String) throws org.osgi.framework.InvalidSyntaxException
meth public org.eclipse.osgi.service.resolver.NativeCodeSpecification createNativeCodeSpecification(org.eclipse.osgi.service.resolver.NativeCodeDescription[],boolean)
meth public org.eclipse.osgi.service.resolver.State createState()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.State createState(boolean)
meth public org.eclipse.osgi.service.resolver.State createState(org.eclipse.osgi.service.resolver.State)
meth public org.eclipse.osgi.service.resolver.State readState(java.io.DataInputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.eclipse.osgi.service.resolver.State readState(java.io.File) throws java.io.IOException
meth public org.eclipse.osgi.service.resolver.State readState(java.io.InputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void writeState(org.eclipse.osgi.service.resolver.State,java.io.DataOutputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void writeState(org.eclipse.osgi.service.resolver.State,java.io.File) throws java.io.IOException
meth public void writeState(org.eclipse.osgi.service.resolver.State,java.io.OutputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr java.lang.Object
hfds IMPL_NAME,implementation,monitor

CLSS public org.eclipse.osgi.service.resolver.StateWire
cons public init(org.eclipse.osgi.service.resolver.BundleDescription,org.eclipse.osgi.service.resolver.VersionConstraint,org.eclipse.osgi.service.resolver.BundleDescription,org.eclipse.osgi.service.resolver.BaseDescription)
meth public org.eclipse.osgi.service.resolver.BaseDescription getDeclaredCapability()
meth public org.eclipse.osgi.service.resolver.BundleDescription getCapabilityHost()
meth public org.eclipse.osgi.service.resolver.BundleDescription getRequirementHost()
meth public org.eclipse.osgi.service.resolver.VersionConstraint getDeclaredRequirement()
supr java.lang.Object
hfds capabilityHost,declaredCapability,declaredRequirement,requirementHost

CLSS public abstract interface org.eclipse.osgi.service.resolver.VersionConstraint
intf java.lang.Cloneable
meth public abstract boolean isResolved()
meth public abstract boolean isSatisfiedBy(org.eclipse.osgi.service.resolver.BaseDescription)
meth public abstract java.lang.Object getUserObject()
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.osgi.service.resolver.BaseDescription getSupplier()
meth public abstract org.eclipse.osgi.service.resolver.BundleDescription getBundle()
meth public abstract org.eclipse.osgi.service.resolver.VersionRange getVersionRange()
meth public abstract org.osgi.framework.wiring.BundleRequirement getRequirement()
meth public abstract void setUserObject(java.lang.Object)

CLSS public org.eclipse.osgi.service.resolver.VersionRange
cons public init(java.lang.String)
cons public init(org.osgi.framework.Version,boolean,org.osgi.framework.Version,boolean)
fld public final static org.eclipse.osgi.service.resolver.VersionRange emptyRange
meth public boolean getIncludeMaximum()
meth public boolean getIncludeMinimum()
meth public boolean isIncluded(org.osgi.framework.Version)
meth public org.osgi.framework.Version getMaximum()
meth public org.osgi.framework.Version getMinimum()
supr org.osgi.framework.VersionRange
hfds EXCLUDE_MAX,EXCLUDE_MIN,INCLUDE_MAX,INCLUDE_MIN,versionMax

CLSS public abstract interface org.eclipse.osgi.service.runnable.ApplicationLauncher
meth public abstract void launch(org.eclipse.osgi.service.runnable.ParameterizedRunnable,java.lang.Object)
meth public abstract void shutdown()

CLSS public abstract interface org.eclipse.osgi.service.runnable.ApplicationRunnable
intf org.eclipse.osgi.service.runnable.ParameterizedRunnable
meth public abstract void stop()

CLSS public abstract interface org.eclipse.osgi.service.runnable.ParameterizedRunnable
meth public abstract java.lang.Object run(java.lang.Object) throws java.lang.Exception

CLSS public abstract interface org.eclipse.osgi.service.runnable.StartupMonitor
meth public abstract void applicationRunning()
meth public abstract void update()

CLSS public abstract org.eclipse.osgi.service.security.TrustEngine
cons public init()
meth protected abstract java.lang.String doAddTrustAnchor(java.security.cert.Certificate,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth protected abstract void doRemoveTrustAnchor(java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth protected abstract void doRemoveTrustAnchor(java.security.cert.Certificate) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract boolean isReadOnly()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String[] getAliases() throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract java.security.cert.Certificate findTrustAnchor(java.security.cert.Certificate[]) throws java.io.IOException
meth public abstract java.security.cert.Certificate getTrustAnchor(java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public final void removeTrustAnchor(java.security.cert.Certificate) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.lang.String addTrustAnchor(java.security.cert.Certificate,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public void removeTrustAnchor(java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
supr java.lang.Object
hfds trustEngineListener

CLSS public abstract interface org.eclipse.osgi.service.urlconversion.URLConverter
meth public abstract java.net.URL resolve(java.net.URL) throws java.io.IOException
meth public abstract java.net.URL toFileURL(java.net.URL) throws java.io.IOException

CLSS public org.eclipse.osgi.signedcontent.InvalidContentException
cons public init(java.lang.String,java.lang.Throwable)
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
supr java.io.IOException
hfds cause,serialVersionUID

CLSS public abstract interface org.eclipse.osgi.signedcontent.SignedContent
meth public abstract boolean isSigned()
meth public abstract java.util.Date getSigningTime(org.eclipse.osgi.signedcontent.SignerInfo)
meth public abstract org.eclipse.osgi.signedcontent.SignedContentEntry getSignedEntry(java.lang.String)
meth public abstract org.eclipse.osgi.signedcontent.SignedContentEntry[] getSignedEntries()
meth public abstract org.eclipse.osgi.signedcontent.SignerInfo getTSASignerInfo(org.eclipse.osgi.signedcontent.SignerInfo)
meth public abstract org.eclipse.osgi.signedcontent.SignerInfo[] getSignerInfos()
meth public abstract void checkValidity(org.eclipse.osgi.signedcontent.SignerInfo) throws java.security.cert.CertificateExpiredException,java.security.cert.CertificateNotYetValidException

CLSS public abstract interface org.eclipse.osgi.signedcontent.SignedContentEntry
meth public abstract boolean isSigned()
meth public abstract java.lang.String getName()
meth public abstract org.eclipse.osgi.signedcontent.SignerInfo[] getSignerInfos()
meth public abstract void verify() throws java.io.IOException

CLSS public abstract interface org.eclipse.osgi.signedcontent.SignedContentFactory
meth public abstract org.eclipse.osgi.signedcontent.SignedContent getSignedContent(java.io.File) throws java.io.IOException,java.security.InvalidKeyException,java.security.NoSuchAlgorithmException,java.security.NoSuchProviderException,java.security.SignatureException,java.security.cert.CertificateException
meth public abstract org.eclipse.osgi.signedcontent.SignedContent getSignedContent(org.osgi.framework.Bundle) throws java.io.IOException,java.security.InvalidKeyException,java.security.NoSuchAlgorithmException,java.security.NoSuchProviderException,java.security.SignatureException,java.security.cert.CertificateException

CLSS public abstract interface org.eclipse.osgi.signedcontent.SignerInfo
meth public abstract boolean isTrusted()
meth public abstract java.lang.String getMessageDigestAlgorithm()
meth public abstract java.security.cert.Certificate getTrustAnchor()
meth public abstract java.security.cert.Certificate[] getCertificateChain()

CLSS public final org.eclipse.osgi.storagemanager.ManagedOutputStream
meth public void abort()
meth public void close() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds ST_CLOSED,ST_OPEN,manager,outputFile,state,streamSet,target

CLSS public final org.eclipse.osgi.storagemanager.StorageManager
cons public init(java.io.File,java.lang.String)
cons public init(java.io.File,java.lang.String,boolean)
meth public boolean isReadOnly()
meth public int getId(java.lang.String)
meth public java.io.File createTempFile(java.lang.String) throws java.io.IOException
meth public java.io.File getBase()
meth public java.io.File lookup(java.lang.String,boolean) throws java.io.IOException
meth public java.io.InputStream getInputStream(java.lang.String) throws java.io.IOException
meth public java.io.InputStream[] getInputStreamSet(java.lang.String[]) throws java.io.IOException
meth public java.lang.String[] getManagedFiles()
meth public org.eclipse.osgi.storagemanager.ManagedOutputStream getOutputStream(java.lang.String) throws java.io.IOException
meth public org.eclipse.osgi.storagemanager.ManagedOutputStream[] getOutputStreamSet(java.lang.String[]) throws java.io.IOException
meth public void add(java.lang.String) throws java.io.IOException
meth public void close()
meth public void open(boolean) throws java.io.IOException
meth public void remove(java.lang.String) throws java.io.IOException
meth public void update(java.lang.String[],java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds FILETYPE_RELIABLEFILE,FILETYPE_STANDARD,LOCK_FILE,MANAGER_FOLDER,MAX_LOCK_WAIT,TABLE_FILE,base,instanceFile,instanceLocker,lockFile,lockMode,locker,managerRoot,open,openCleanup,readOnly,saveCleanup,table,tableFile,tableStamp,tempCleanup,useReliableFiles
hcls Entry

CLSS public org.eclipse.osgi.util.ManifestElement
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getDirective(java.lang.String)
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.lang.String[] getAttributes(java.lang.String)
meth public java.lang.String[] getDirectives(java.lang.String)
meth public java.lang.String[] getValueComponents()
meth public java.util.Enumeration<java.lang.String> getDirectiveKeys()
meth public java.util.Enumeration<java.lang.String> getKeys()
meth public static java.lang.String[] getArrayFromList(java.lang.String)
meth public static java.lang.String[] getArrayFromList(java.lang.String,java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.String> parseBundleManifest(java.io.InputStream) throws java.io.IOException,org.osgi.framework.BundleException
meth public static java.util.Map<java.lang.String,java.lang.String> parseBundleManifest(java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,org.osgi.framework.BundleException
meth public static org.eclipse.osgi.util.ManifestElement[] parseHeader(java.lang.String,java.lang.String) throws org.osgi.framework.BundleException
supr java.lang.Object
hfds attributes,directives,mainValue,valueComponents

CLSS public abstract org.eclipse.osgi.util.NLS
cons protected init()
meth public !varargs static <%0 extends java.lang.Object> java.lang.String bind(java.lang.String,{%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public static java.lang.String bind(java.lang.String,java.lang.Object)
meth public static java.lang.String bind(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void initializeMessages(java.lang.String,java.lang.Class<?>)
supr java.lang.Object
hfds ASSIGNED,EMPTY_ARGS,EXTENSION,IGNORE,PROP_WARNINGS,SEVERITY_ERROR,SEVERITY_WARNING,frameworkLog,ignoreWarnings,nlSuffixes
hcls MessagesProperties

CLSS public org.eclipse.osgi.util.TextProcessor
meth public static java.lang.String deprocess(java.lang.String)
meth public static java.lang.String getDefaultDelimiters()
meth public static java.lang.String process(java.lang.String)
meth public static java.lang.String process(java.lang.String,java.lang.String)
supr java.lang.Object
hfds COLON,DOT,FILE_SEP_BSLASH,FILE_SEP_FSLASH,INDEX_NOT_SET,IS_PROCESSING_NEEDED,LRE,LRM,PDF,delimiterString

CLSS public abstract interface org.osgi.framework.BundleReference
meth public abstract org.osgi.framework.Bundle getBundle()

CLSS public abstract interface org.osgi.framework.ServiceFactory<%0 extends java.lang.Object>
meth public abstract void ungetService(org.osgi.framework.Bundle,org.osgi.framework.ServiceRegistration<{org.osgi.framework.ServiceFactory%0}>,{org.osgi.framework.ServiceFactory%0})
meth public abstract {org.osgi.framework.ServiceFactory%0} getService(org.osgi.framework.Bundle,org.osgi.framework.ServiceRegistration<{org.osgi.framework.ServiceFactory%0}>)

CLSS public org.osgi.framework.VersionRange
cons public init(char,org.osgi.framework.Version,org.osgi.framework.Version,char)
cons public init(java.lang.String)
fld public final static char LEFT_CLOSED = '['
fld public final static char LEFT_OPEN = '('
fld public final static char RIGHT_CLOSED = ']'
fld public final static char RIGHT_OPEN = ')'
meth public !varargs org.osgi.framework.VersionRange intersection(org.osgi.framework.VersionRange[])
meth public boolean equals(java.lang.Object)
meth public boolean includes(org.osgi.framework.Version)
meth public boolean isEmpty()
meth public boolean isExact()
meth public char getLeftType()
meth public char getRightType()
meth public int hashCode()
meth public java.lang.String toFilterString(java.lang.String)
meth public java.lang.String toString()
meth public org.osgi.framework.Version getLeft()
meth public org.osgi.framework.Version getRight()
meth public static org.osgi.framework.VersionRange valueOf(java.lang.String)
supr java.lang.Object
hfds ENDPOINT_DELIMITER,LEFT_CLOSED_DELIMITER,LEFT_DELIMITERS,LEFT_OPEN_DELIMITER,RIGHT_CLOSED_DELIMITER,RIGHT_DELIMITERS,RIGHT_OPEN_DELIMITER,empty,hash,left,leftClosed,right,rightClosed,versionRangeString

CLSS public abstract interface org.osgi.framework.wiring.BundleRevision
fld public final static int TYPE_FRAGMENT = 1
fld public final static java.lang.String BUNDLE_NAMESPACE = "osgi.wiring.bundle"
fld public final static java.lang.String HOST_NAMESPACE = "osgi.wiring.host"
fld public final static java.lang.String PACKAGE_NAMESPACE = "osgi.wiring.package"
intf org.osgi.framework.BundleReference
intf org.osgi.resource.Resource
meth public abstract int getTypes()
meth public abstract java.lang.String getSymbolicName()
meth public abstract java.util.List<org.osgi.framework.wiring.BundleCapability> getDeclaredCapabilities(java.lang.String)
meth public abstract java.util.List<org.osgi.framework.wiring.BundleRequirement> getDeclaredRequirements(java.lang.String)
meth public abstract java.util.List<org.osgi.resource.Capability> getCapabilities(java.lang.String)
meth public abstract java.util.List<org.osgi.resource.Requirement> getRequirements(java.lang.String)
meth public abstract org.osgi.framework.Version getVersion()
meth public abstract org.osgi.framework.wiring.BundleWiring getWiring()

CLSS public abstract interface org.osgi.resource.Resource
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.util.List<org.osgi.resource.Capability> getCapabilities(java.lang.String)
meth public abstract java.util.List<org.osgi.resource.Requirement> getRequirements(java.lang.String)

