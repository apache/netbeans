#Signature file v4.1
#Version 1.85.0

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface org.netbeans.core.startup.base.LayerFactory
innr public abstract interface static Provider
meth public abstract java.util.List<java.net.URL> additionalLayers(java.util.List<java.net.URL>)
meth public abstract org.openide.filesystems.FileSystem createEmptyFileSystem() throws java.io.IOException
meth public abstract org.openide.filesystems.FileSystem loadCache() throws java.io.IOException
meth public abstract org.openide.filesystems.FileSystem store(org.openide.filesystems.FileSystem,java.util.List<java.net.URL>) throws java.io.IOException

CLSS public abstract interface static org.netbeans.core.startup.base.LayerFactory$Provider
 outer org.netbeans.core.startup.base.LayerFactory
meth public abstract org.netbeans.core.startup.base.LayerFactory create(boolean)

CLSS public org.netbeans.core.startup.layers.ArchiveURLMapper
cons public init()
meth public java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
supr org.openide.filesystems.URLMapper
hfds JAR_PROTOCOL,LOG,copiedJARs,mountRoots
hcls JFSReference

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

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

