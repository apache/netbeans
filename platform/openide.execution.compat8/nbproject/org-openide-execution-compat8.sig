#Signature file v4.1
#Version 9.25

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public java.net.URLClassLoader
cons public init(java.net.URL[])
cons public init(java.net.URL[],java.lang.ClassLoader)
cons public init(java.net.URL[],java.lang.ClassLoader,java.net.URLStreamHandlerFactory)
intf java.io.Closeable
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected void addURL(java.net.URL)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URLClassLoader newInstance(java.net.URL[])
meth public static java.net.URLClassLoader newInstance(java.net.URL[],java.lang.ClassLoader)
meth public void close() throws java.io.IOException
supr java.security.SecureClassLoader

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public abstract org.openide.execution.ExecutionEngine
cons public init()
meth protected abstract java.security.PermissionCollection createPermissions(java.security.CodeSource,org.openide.windows.InputOutput)
meth protected abstract org.openide.execution.NbClassPath createLibraryPath()
 anno 0 java.lang.Deprecated()
meth public abstract org.openide.execution.ExecutorTask execute(java.lang.String,java.lang.Runnable,org.openide.windows.InputOutput)
meth public static org.openide.execution.ExecutionEngine getDefault()
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.execution.ExecutorTask
cons protected init(java.lang.Runnable)
meth public abstract int result()
meth public abstract org.openide.windows.InputOutput getInputOutput()
meth public abstract void stop()
supr org.openide.util.Task

CLSS public org.openide.execution.NbClassLoader
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.FileObject[],java.lang.ClassLoader,org.openide.windows.InputOutput)
cons public init(org.openide.filesystems.FileSystem[])
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.FileSystem[],java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
cons public init(org.openide.windows.InputOutput)
 anno 0 java.lang.Deprecated()
fld protected org.openide.windows.InputOutput inout
meth protected final java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected java.lang.Class findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public void setDefaultPermissions(java.security.PermissionCollection)
supr java.net.URLClassLoader
hfds defaultPermissions,f,fast,permissionCollections

CLSS public final org.openide.execution.NbClassPath
cons public init(java.io.File[])
cons public init(java.lang.String)
cons public init(java.lang.String[])
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public java.lang.Exception[] getExceptions()
meth public java.lang.String getClassPath()
meth public static java.io.File toFile(org.openide.filesystems.FileObject)
 anno 0 java.lang.Deprecated()
meth public static org.openide.execution.NbClassPath createBootClassPath()
 anno 0 java.lang.Deprecated()
meth public static org.openide.execution.NbClassPath createClassPath()
 anno 0 java.lang.Deprecated()
meth public static org.openide.execution.NbClassPath createLibraryPath()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds classpath,items,serialVersionUID

CLSS public org.openide.execution.NbClassPathCompat
cons public init()
meth public static org.openide.execution.NbClassPath createRepositoryPath()
 anno 0 java.lang.Deprecated()
meth public static org.openide.execution.NbClassPath createRepositoryPath(org.openide.filesystems.FileSystemCapability)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public final org.openide.execution.NbProcessDescriptor
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Process exec() throws java.io.IOException
meth public java.lang.Process exec(java.text.Format) throws java.io.IOException
meth public java.lang.Process exec(java.text.Format,java.lang.String[]) throws java.io.IOException
meth public java.lang.Process exec(java.text.Format,java.lang.String[],boolean,java.io.File) throws java.io.IOException
meth public java.lang.Process exec(java.text.Format,java.lang.String[],java.io.File) throws java.io.IOException
meth public java.lang.String getArguments()
meth public java.lang.String getInfo()
meth public java.lang.String getProcessName()
supr java.lang.Object
hfds arguments,execLog,info,processName,serialVersionUID

CLSS public abstract org.openide.execution.ScriptType
 anno 0 java.lang.Deprecated()
cons public init()
innr public static Context
meth public abstract boolean acceptFileObject(org.openide.filesystems.FileObject)
meth public abstract java.lang.Object eval(java.io.Reader,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract java.lang.Object eval(java.lang.String,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract void addVariable(java.lang.String,java.lang.Object)
meth public abstract void exec(java.io.Reader,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public abstract void exec(java.lang.String,org.openide.execution.ScriptType$Context) throws java.lang.reflect.InvocationTargetException
meth public final java.lang.Object eval(java.io.Reader) throws java.lang.reflect.InvocationTargetException
meth public final java.lang.Object eval(java.lang.String) throws java.lang.reflect.InvocationTargetException
meth public final void exec(java.io.Reader) throws java.lang.reflect.InvocationTargetException
meth public final void exec(java.lang.String) throws java.lang.reflect.InvocationTargetException
meth public static java.util.Enumeration scriptTypes()
meth public static org.openide.execution.ScriptType find(java.lang.Class)
meth public static org.openide.execution.ScriptType find(java.lang.String)
meth public static org.openide.execution.ScriptType getDefault()
supr org.openide.ServiceType
hfds serialVersionUID

CLSS public static org.openide.execution.ScriptType$Context
 outer org.openide.execution.ScriptType
cons public init()
supr java.lang.Object

CLSS public abstract interface !annotation org.openide.modules.PatchFor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public org.openide.util.Task
cons protected init()
cons public init(java.lang.Runnable)
fld public final static org.openide.util.Task EMPTY
intf java.lang.Runnable
meth protected final void notifyFinished()
meth protected final void notifyRunning()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public final boolean isFinished()
meth public java.lang.String toString()
meth public void addTaskListener(org.openide.util.TaskListener)
meth public void removeTaskListener(org.openide.util.TaskListener)
meth public void run()
meth public void waitFinished()
supr java.lang.Object
hfds LOG,RP,finished,list,overrides,run

