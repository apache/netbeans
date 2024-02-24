#Signature file v4.1
#Version 9.32

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$Attr
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = 5978845941846736946
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth public abstract java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth public abstract void deleteAttributes(java.lang.String)
meth public abstract void renameAttributes(java.lang.String,java.lang.String)
meth public abstract void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$Change
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = -5841597109944924596
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract void createData(java.lang.String) throws java.io.IOException
meth public abstract void createFolder(java.lang.String) throws java.io.IOException
meth public abstract void delete(java.lang.String) throws java.io.IOException
meth public abstract void rename(java.lang.String,java.lang.String) throws java.io.IOException

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$Info
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = -2438286177948307985
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract boolean folder(java.lang.String)
meth public abstract boolean readOnly(java.lang.String)
meth public abstract java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public abstract java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String mimeType(java.lang.String)
meth public abstract java.util.Date lastModified(java.lang.String)
meth public abstract long size(java.lang.String)
meth public abstract void lock(java.lang.String) throws java.io.IOException
meth public abstract void markUnimportant(java.lang.String)
meth public abstract void unlock(java.lang.String)

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$List
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = -6242105832891012528
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract java.lang.String[] children(java.lang.String)

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$SymlinkInfo
 outer org.openide.filesystems.AbstractFileSystem
intf java.io.Serializable
meth public abstract boolean isSymbolicLink(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getCanonicalName(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String readSymbolicLink(java.lang.String) throws java.io.IOException

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$Transfer
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = -8945397853892302838
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract boolean copy(java.lang.String,org.openide.filesystems.AbstractFileSystem$Transfer,java.lang.String) throws java.io.IOException
meth public abstract boolean move(java.lang.String,org.openide.filesystems.AbstractFileSystem$Transfer,java.lang.String) throws java.io.IOException

CLSS public abstract org.openide.filesystems.AbstractFileSystemCompat
cons public init()
meth public org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
supr org.openide.filesystems.FileSystem
hfds NO_SYSTEM_ACTIONS,SYSTEM_ACTIONS

CLSS public org.openide.filesystems.CompatFileExtrasLkp
cons public init()
cons public init(org.openide.util.lookup.AbstractLookup$Content)
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
supr org.openide.util.lookup.AbstractLookup
hfds ic

CLSS public org.openide.filesystems.DefaultAttributes
cons protected init(org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List,java.lang.String)
cons public init(org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List)
fld public final static java.lang.String ATTR_EXT = "attributes"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ATTR_NAME = "filesystem"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ATTR_NAME_EXT = "filesystem.attributes"
 anno 0 java.lang.Deprecated()
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$List
meth public java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String[] children(java.lang.String)
meth public java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth public void deleteAttributes(java.lang.String)
meth public void renameAttributes(java.lang.String,java.lang.String)
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
hfds ATTR_NAME_EXT_XML,DTD_PATH,PUBLIC_ID,READONLY_ATTRIBUTES,cache,change,fileName,info,list,serialVersionUID
hcls ElementHandler,InnerParser,Table

CLSS public org.openide.filesystems.EnvironmentNotSupportedException
 anno 0 java.lang.Deprecated()
cons public init(org.openide.filesystems.FileSystem)
cons public init(org.openide.filesystems.FileSystem,java.lang.String)
meth public org.openide.filesystems.FileSystem getFileSystem()
supr java.io.IOException
hfds fs,serialVersionUID

CLSS public org.openide.filesystems.FileAlreadyLockedException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public org.openide.filesystems.FileAttributeEvent
cons public init(org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.Object,java.lang.Object,boolean)
meth public java.lang.Object getNewValue()
meth public java.lang.Object getOldValue()
meth public java.lang.String getName()
supr org.openide.filesystems.FileEvent
hfds name,newValue,oldValue,serialVersionUID

CLSS public org.openide.filesystems.FileChangeAdapter
cons public init()
intf org.openide.filesystems.FileChangeListener
meth public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public void fileChanged(org.openide.filesystems.FileEvent)
meth public void fileDataCreated(org.openide.filesystems.FileEvent)
meth public void fileDeleted(org.openide.filesystems.FileEvent)
meth public void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public void fileRenamed(org.openide.filesystems.FileRenameEvent)
supr java.lang.Object

CLSS public abstract interface org.openide.filesystems.FileChangeListener
intf java.util.EventListener
meth public abstract void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent)
meth public abstract void fileChanged(org.openide.filesystems.FileEvent)
meth public abstract void fileDataCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileDeleted(org.openide.filesystems.FileEvent)
meth public abstract void fileFolderCreated(org.openide.filesystems.FileEvent)
meth public abstract void fileRenamed(org.openide.filesystems.FileRenameEvent)

CLSS public org.openide.filesystems.FileEvent
cons public init(org.openide.filesystems.FileObject)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,boolean)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,boolean,long)
meth public boolean firedFrom(org.openide.filesystems.FileSystem$AtomicAction)
meth public final boolean isExpected()
meth public final long getTime()
meth public final org.openide.filesystems.FileObject getFile()
meth public final void runWhenDeliveryOver(java.lang.Runnable)
meth public java.lang.String toString()
supr java.util.EventObject
hfds LOG,atomActionID,expected,file,postNotify,serialVersionUID,time

CLSS public org.openide.filesystems.FileLock
cons public init()
fld protected java.lang.Throwable lockedBy
fld public final static org.openide.filesystems.FileLock NONE
intf java.lang.AutoCloseable
meth public boolean isValid()
meth public void close()
meth public void finalize()
meth public void releaseLock()
supr java.lang.Object
hfds locked

CLSS public abstract org.openide.filesystems.FileObject
cons public init()
fld public final static java.lang.String DEFAULT_LINE_SEPARATOR_ATTR = "default-line-separator"
fld public final static java.lang.String DEFAULT_PATHNAME_SEPARATOR_ATTR = "default-pathname-separator"
intf java.io.Serializable
intf org.openide.util.Lookup$Provider
meth protected void fireFileAttributeChangedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileAttributeEvent)
meth protected void fireFileChangedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileEvent)
meth protected void fireFileDataCreatedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileEvent)
meth protected void fireFileDeletedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileEvent)
meth protected void fireFileFolderCreatedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileEvent)
meth protected void fireFileRenamedEvent(java.util.Enumeration<org.openide.filesystems.FileChangeListener>,org.openide.filesystems.FileRenameEvent)
meth public !varargs java.lang.String getMIMEType(java.lang.String[])
meth public abstract boolean isData()
meth public abstract boolean isFolder()
meth public abstract boolean isReadOnly()
 anno 0 java.lang.Deprecated()
meth public abstract boolean isRoot()
meth public abstract boolean isValid()
meth public abstract java.io.InputStream getInputStream() throws java.io.FileNotFoundException
meth public abstract java.io.OutputStream getOutputStream(org.openide.filesystems.FileLock) throws java.io.IOException
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getExt()
meth public abstract java.lang.String getName()
meth public abstract java.util.Date lastModified()
meth public abstract java.util.Enumeration<java.lang.String> getAttributes()
meth public abstract long getSize()
meth public abstract org.openide.filesystems.FileLock lock() throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject createData(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject createFolder(java.lang.String) throws java.io.IOException
meth public abstract org.openide.filesystems.FileObject getFileObject(java.lang.String,java.lang.String)
meth public abstract org.openide.filesystems.FileObject getParent()
meth public abstract org.openide.filesystems.FileObject[] getChildren()
meth public abstract org.openide.filesystems.FileSystem getFileSystem() throws org.openide.filesystems.FileStateInvalidException
meth public abstract void addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public abstract void delete(org.openide.filesystems.FileLock) throws java.io.IOException
meth public abstract void removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public abstract void rename(org.openide.filesystems.FileLock,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void setAttribute(java.lang.String,java.lang.Object) throws java.io.IOException
meth public abstract void setImportant(boolean)
 anno 0 java.lang.Deprecated()
meth public boolean canRead()
meth public boolean canWrite()
meth public boolean existsExt(java.lang.String)
meth public boolean isLocked()
meth public boolean isSymbolicLink() throws java.io.IOException
meth public boolean isVirtual()
meth public byte[] asBytes() throws java.io.IOException
meth public final boolean canRevert()
meth public final boolean hasExt(java.lang.String)
meth public final java.io.OutputStream getOutputStream() throws java.io.IOException
meth public final java.net.URI toURI()
meth public final java.net.URL getURL() throws org.openide.filesystems.FileStateInvalidException
 anno 0 java.lang.Deprecated()
meth public final java.net.URL toURL()
meth public final void delete() throws java.io.IOException
meth public final void revert() throws java.io.IOException
meth public java.io.OutputStream createAndOpen(java.lang.String) throws java.io.IOException
meth public java.lang.String asText() throws java.io.IOException
meth public java.lang.String asText(java.lang.String) throws java.io.IOException
meth public java.lang.String getMIMEType()
meth public java.lang.String getNameExt()
meth public java.lang.String getPackageName(char)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getPackageNameExt(char,char)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getPath()
meth public java.lang.String readSymbolicLinkPath() throws java.io.IOException
meth public java.lang.String toString()
meth public java.util.Enumeration<? extends org.openide.filesystems.FileObject> getChildren(boolean)
meth public java.util.Enumeration<? extends org.openide.filesystems.FileObject> getData(boolean)
meth public java.util.Enumeration<? extends org.openide.filesystems.FileObject> getFolders(boolean)
meth public java.util.List<java.lang.String> asLines() throws java.io.IOException
meth public java.util.List<java.lang.String> asLines(java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject copy(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject createData(java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject getCanonicalFileObject() throws java.io.IOException
meth public org.openide.filesystems.FileObject getFileObject(java.lang.String)
meth public org.openide.filesystems.FileObject getFileObject(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject move(org.openide.filesystems.FileLock,org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.openide.filesystems.FileObject readSymbolicLink() throws java.io.IOException
meth public org.openide.util.Lookup getLookup()
meth public void addRecursiveListener(org.openide.filesystems.FileChangeListener)
meth public void refresh()
meth public void refresh(boolean)
meth public void removeRecursiveListener(org.openide.filesystems.FileChangeListener)
supr java.lang.Object
hfds REMOVE_WRITABLES_ATTR,lkp,serialVersionUID
hcls ED,OnlyFolders,PriorityFileChangeListener

CLSS public org.openide.filesystems.FileRenameEvent
cons public init(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String,boolean)
meth public java.lang.String getExt()
meth public java.lang.String getName()
supr org.openide.filesystems.FileEvent
hfds ext,name,serialVersionUID

CLSS public org.openide.filesystems.FileStateInvalidException
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getFileSystemName()
supr java.io.IOException
hfds fileSystemName,serialVersionUID

CLSS public final org.openide.filesystems.FileStatusEvent
cons public init(org.openide.filesystems.FileSystem,boolean,boolean)
cons public init(org.openide.filesystems.FileSystem,java.util.Set<? extends org.openide.filesystems.FileObject>,boolean,boolean)
cons public init(org.openide.filesystems.FileSystem,org.openide.filesystems.FileObject,boolean,boolean)
meth public boolean hasChanged(org.openide.filesystems.FileObject)
meth public boolean isIconChange()
meth public boolean isNameChange()
meth public org.openide.filesystems.FileSystem getFileSystem()
supr java.util.EventObject
hfds files,icon,name,serialVersionUID

CLSS public abstract interface org.openide.filesystems.FileStatusListener
intf java.util.EventListener
meth public abstract void annotationChanged(org.openide.filesystems.FileStatusEvent)

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

CLSS public abstract interface static org.openide.filesystems.FileSystem$AtomicAction
 outer org.openide.filesystems.FileSystem
meth public abstract void run() throws java.io.IOException

CLSS public abstract org.openide.filesystems.FileSystem$Environment
 anno 0 java.lang.Deprecated()
cons public init()
meth public void addClassPath(java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public abstract interface org.openide.filesystems.FileSystem$HtmlStatus
intf org.openide.filesystems.FileSystem$Status
meth public abstract java.lang.String annotateNameHtml(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)

CLSS public abstract interface org.openide.filesystems.FileSystem$Status
meth public abstract java.awt.Image annotateIcon(java.awt.Image,int,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public abstract java.lang.String annotateName(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)

CLSS public org.openide.filesystems.FileSystemCapability
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static org.openide.filesystems.FileSystemCapability ALL
fld public final static org.openide.filesystems.FileSystemCapability COMPILE
 anno 0 java.lang.Deprecated()
fld public final static org.openide.filesystems.FileSystemCapability DEBUG
 anno 0 java.lang.Deprecated()
fld public final static org.openide.filesystems.FileSystemCapability DOC
 anno 0 java.lang.Deprecated()
fld public final static org.openide.filesystems.FileSystemCapability EXECUTE
 anno 0 java.lang.Deprecated()
innr public static Bean
meth public boolean capableOf(org.openide.filesystems.FileSystemCapability)
meth public final java.util.Enumeration<? extends org.openide.filesystems.FileObject> findAll(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileObject find(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Enumeration<? extends org.openide.filesystems.FileObject> findAllResources(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Enumeration<? extends org.openide.filesystems.FileSystem> fileSystems()
 anno 0 java.lang.Deprecated()
meth public org.openide.filesystems.FileObject findResource(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object

CLSS public static org.openide.filesystems.FileSystemCapability$Bean
 outer org.openide.filesystems.FileSystemCapability
 anno 0 java.lang.Deprecated()
cons public init()
intf java.io.Serializable
meth public boolean capableOf(org.openide.filesystems.FileSystemCapability)
meth public boolean getCompile()
 anno 0 java.lang.Deprecated()
meth public boolean getDebug()
 anno 0 java.lang.Deprecated()
meth public boolean getDoc()
 anno 0 java.lang.Deprecated()
meth public boolean getExecute()
 anno 0 java.lang.Deprecated()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setCompile(boolean)
 anno 0 java.lang.Deprecated()
meth public void setDebug(boolean)
 anno 0 java.lang.Deprecated()
meth public void setDoc(boolean)
 anno 0 java.lang.Deprecated()
meth public void setExecute(boolean)
 anno 0 java.lang.Deprecated()
supr org.openide.filesystems.FileSystemCapability
hfds compilation,debug,doc,execution,serialVersionUID,supp

CLSS public abstract org.openide.filesystems.FileSystemCompat
cons public init()
meth protected boolean isPersistent()
 anno 0 java.lang.Deprecated()
meth protected final void setCapability(org.openide.filesystems.FileSystemCapability)
 anno 0 java.lang.Deprecated()
meth public final boolean isHidden()
 anno 0 java.lang.Deprecated()
meth public final org.openide.filesystems.FileSystemCapability getCapability()
 anno 0 java.lang.Deprecated()
meth public final void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public org.openide.filesystems.FileSystem$Status getStatus()
meth public org.openide.util.actions.SystemAction[] getActions()
meth public org.openide.util.actions.SystemAction[] getActions(java.util.Set<org.openide.filesystems.FileObject>)
meth public static org.openide.filesystems.FileSystemCompat compat(org.openide.filesystems.FileSystem)
meth public void prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds NO_SYSTEM_ACTIONS,PROP_CAPABILITIES,capability,capabilityListener,hidden,status
hcls StatusImpl,SystemStatus

CLSS public final org.openide.filesystems.FileUtil
meth public !varargs static java.lang.String getMIMEType(org.openide.filesystems.FileObject,java.lang.String[])
meth public !varargs static void refreshFor(java.io.File[])
meth public final static void runAtomicAction(java.lang.Runnable)
meth public final static void runAtomicAction(org.openide.filesystems.FileSystem$AtomicAction) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} getConfigObject(java.lang.String,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} getSystemConfigObject(java.lang.String,java.lang.Class<{%%0}>)
meth public static boolean affectsOrder(org.openide.filesystems.FileAttributeEvent)
meth public static boolean isArchiveArtifact(java.net.URL)
meth public static boolean isArchiveArtifact(org.openide.filesystems.FileObject)
meth public static boolean isArchiveFile(java.net.URL)
meth public static boolean isArchiveFile(org.openide.filesystems.FileObject)
meth public static boolean isParentOf(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static boolean isRecursiveSymbolicLink(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static boolean isValidFileName(java.lang.String)
meth public static java.io.File archiveOrDirForURL(java.net.URL)
meth public static java.io.File normalizeFile(java.io.File)
meth public static java.io.File toFile(org.openide.filesystems.FileObject)
meth public static java.lang.String findFreeFileName(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static java.lang.String findFreeFolderName(org.openide.filesystems.FileObject,java.lang.String)
meth public static java.lang.String getExtension(java.lang.String)
meth public static java.lang.String getFileDisplayName(org.openide.filesystems.FileObject)
meth public static java.lang.String getMIMEType(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getMIMEType(org.openide.filesystems.FileObject)
meth public static java.lang.String getRelativePath(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
meth public static java.lang.String normalizePath(java.lang.String)
meth public static java.net.URL getArchiveFile(java.net.URL)
meth public static java.net.URL getArchiveRoot(java.net.URL)
meth public static java.net.URL urlForArchiveOrDir(java.io.File)
meth public static java.net.URLStreamHandler nbfsURLStreamHandler()
 anno 0 java.lang.Deprecated()
meth public static java.nio.file.Path toPath(org.openide.filesystems.FileObject)
meth public static java.util.List<java.lang.String> getMIMETypeExtensions(java.lang.String)
meth public static java.util.List<org.openide.filesystems.FileObject> getOrder(java.util.Collection<org.openide.filesystems.FileObject>,boolean)
meth public static java.util.function.BiFunction<java.lang.String,java.lang.Object,java.lang.Object> defaultAttributesTransformer()
meth public static org.openide.filesystems.FileChangeListener weakFileChangeListener(org.openide.filesystems.FileChangeListener,java.lang.Object)
meth public static org.openide.filesystems.FileObject copyFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject copyFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createData(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createData(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createFolder(java.io.File) throws java.io.IOException
meth public static org.openide.filesystems.FileObject createFolder(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject findBrother(org.openide.filesystems.FileObject,java.lang.String)
meth public static org.openide.filesystems.FileObject getArchiveFile(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject getArchiveRoot(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject getConfigFile(java.lang.String)
meth public static org.openide.filesystems.FileObject getConfigRoot()
meth public static org.openide.filesystems.FileObject getSystemConfigFile(java.lang.String)
meth public static org.openide.filesystems.FileObject getSystemConfigRoot()
meth public static org.openide.filesystems.FileObject moveFile(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.openide.filesystems.FileObject toFileObject(java.io.File)
meth public static org.openide.filesystems.FileObject toFileObject(java.nio.file.Path)
meth public static org.openide.filesystems.FileObject[] fromFile(java.io.File)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.FileStatusListener weakFileStatusListener(org.openide.filesystems.FileStatusListener,java.lang.Object)
meth public static org.openide.filesystems.FileSystem createMemoryFileSystem()
meth public static void addFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public static void addFileChangeListener(org.openide.filesystems.FileChangeListener,java.io.File)
meth public static void addRecursiveListener(org.openide.filesystems.FileChangeListener,java.io.File)
meth public static void addRecursiveListener(org.openide.filesystems.FileChangeListener,java.io.File,java.io.FileFilter,java.util.concurrent.Callable<java.lang.Boolean>)
meth public static void addRecursiveListener(org.openide.filesystems.FileChangeListener,java.io.File,java.util.concurrent.Callable<java.lang.Boolean>)
meth public static void copy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static void copyAttributes(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
meth public static void copyAttributes(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.util.function.BiFunction<java.lang.String,java.lang.Object,java.lang.Object>) throws java.io.IOException
meth public static void extractJar(org.openide.filesystems.FileObject,java.io.InputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void preventFileChooserSymlinkTraversal(javax.swing.JFileChooser,java.io.File)
 anno 0 java.lang.Deprecated()
meth public static void refreshAll()
meth public static void removeFileChangeListener(org.openide.filesystems.FileChangeListener)
meth public static void removeFileChangeListener(org.openide.filesystems.FileChangeListener,java.io.File)
meth public static void removeRecursiveListener(org.openide.filesystems.FileChangeListener,java.io.File)
meth public static void setMIMEType(java.lang.String,java.lang.String)
meth public static void setOrder(java.util.List<org.openide.filesystems.FileObject>) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT_ATTR_TRANSFORMER,ILLEGAL_FILENAME_CHARACTERS,LOG,REFRESH_RP,archiveRootProviderCache,archiveRootProviders,diskFileSystem,normalizedRef,refreshTask,transientAttributes
hcls NonCanonicalizingFile

CLSS public org.openide.filesystems.JarFileSystem
cons public init()
cons public init(java.io.File) throws java.io.IOException
innr public static Impl
meth protected <%0 extends org.openide.filesystems.FileObject> java.lang.ref.Reference<{%%0}> createReference({%%0})
meth protected boolean folder(java.lang.String)
meth protected boolean readOnly(java.lang.String)
meth protected java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth protected java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth protected java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth protected java.lang.String mimeType(java.lang.String)
meth protected java.lang.String[] children(java.lang.String)
meth protected java.util.Date lastModified(java.lang.String)
meth protected java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth protected long size(java.lang.String)
meth protected void createData(java.lang.String) throws java.io.IOException
meth protected void createFolder(java.lang.String) throws java.io.IOException
meth protected void delete(java.lang.String) throws java.io.IOException
meth protected void deleteAttributes(java.lang.String)
meth protected void finalize() throws java.lang.Throwable
meth protected void lock(java.lang.String) throws java.io.IOException
meth protected void markUnimportant(java.lang.String)
meth protected void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void renameAttributes(java.lang.String,java.lang.String)
meth protected void unlock(java.lang.String)
meth protected void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
meth public boolean isReadOnly()
meth public java.io.File getJarFile()
meth public java.lang.String getDisplayName()
meth public java.util.jar.Manifest getManifest()
meth public void removeNotify()
meth public void setJarFile(java.io.File) throws java.beans.PropertyVetoException,java.io.IOException
supr org.openide.filesystems.AbstractFileSystem
hfds CLOSE_DELAY_MAX,CLOSE_DELAY_MIN,LOGGER,MEM_STREAM_SIZE,REFRESH_TIME,aliveCount,checkTime,closeDelay,closeSync,closeTask,fcl,foRoot,jar,lastModification,manifest,openRequestTime,req,root,serialVersionUID,softCache,strongCache,watcherTask
hcls Cache,Ref

CLSS public static org.openide.filesystems.JarFileSystem$Impl
 outer org.openide.filesystems.JarFileSystem
cons public init(org.openide.filesystems.JarFileSystem)
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
meth public boolean folder(java.lang.String)
meth public boolean readOnly(java.lang.String)
meth public java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String mimeType(java.lang.String)
meth public java.lang.String[] children(java.lang.String)
meth public java.util.Date lastModified(java.lang.String)
meth public java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth public long size(java.lang.String)
meth public void createData(java.lang.String) throws java.io.IOException
meth public void createFolder(java.lang.String) throws java.io.IOException
meth public void delete(java.lang.String) throws java.io.IOException
meth public void deleteAttributes(java.lang.String)
meth public void lock(java.lang.String) throws java.io.IOException
meth public void markUnimportant(java.lang.String)
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void renameAttributes(java.lang.String,java.lang.String)
meth public void unlock(java.lang.String)
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
hfds fs,serialVersionUID

CLSS public abstract org.openide.filesystems.JarFileSystemCompat
cons public init()
meth public abstract java.io.File getJarFile()
meth public static void createJarFileSystemCompat(org.openide.filesystems.JarFileSystemCompat,org.openide.filesystems.FileSystemCapability) throws java.io.IOException
meth public void prepareEnvironment(org.openide.filesystems.FileSystem$Environment)
 anno 0 java.lang.Deprecated()
supr org.openide.filesystems.AbstractFileSystem

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

CLSS public static org.openide.filesystems.LocalFileSystem$Impl
 outer org.openide.filesystems.LocalFileSystem
cons public init(org.openide.filesystems.LocalFileSystem)
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
intf org.openide.filesystems.AbstractFileSystem$SymlinkInfo
meth public boolean folder(java.lang.String)
meth public boolean isSymbolicLink(java.lang.String) throws java.io.IOException
meth public boolean readOnly(java.lang.String)
meth public java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.String getCanonicalName(java.lang.String) throws java.io.IOException
meth public java.lang.String mimeType(java.lang.String)
meth public java.lang.String readSymbolicLink(java.lang.String) throws java.io.IOException
meth public java.lang.String[] children(java.lang.String)
meth public java.util.Date lastModified(java.lang.String)
meth public long size(java.lang.String)
meth public void createData(java.lang.String) throws java.io.IOException
meth public void createFolder(java.lang.String) throws java.io.IOException
meth public void delete(java.lang.String) throws java.io.IOException
meth public void lock(java.lang.String) throws java.io.IOException
meth public void markUnimportant(java.lang.String)
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void unlock(java.lang.String)
supr java.lang.Object
hfds fs,serialVersionUID

CLSS public abstract org.openide.filesystems.LocalFileSystemCompat
cons public init()
meth public abstract java.io.File getRootDirectory()
meth public static void createLocalFileSystemCompat(org.openide.filesystems.LocalFileSystemCompat,org.openide.filesystems.FileSystemCapability)
meth public void prepareEnvironment(org.openide.filesystems.FileSystem$Environment)
 anno 0 java.lang.Deprecated()
supr org.openide.filesystems.AbstractFileSystem

CLSS public abstract org.openide.filesystems.MIMEResolver
cons public !varargs init(java.lang.String[])
cons public init()
 anno 0 java.lang.Deprecated()
innr public abstract UIHelpers
innr public abstract interface static !annotation ExtensionRegistration
innr public abstract interface static !annotation NamespaceRegistration
innr public abstract interface static !annotation Registration
meth public abstract java.lang.String findMIMEType(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds resolvableMIMETypes

CLSS public abstract interface static !annotation org.openide.filesystems.MIMEResolver$ExtensionRegistration
 outer org.openide.filesystems.MIMEResolver
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String[] showInFileChooser()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String mimeType()
meth public abstract java.lang.String[] extension()

CLSS public abstract interface static !annotation org.openide.filesystems.MIMEResolver$NamespaceRegistration
 outer org.openide.filesystems.MIMEResolver
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String elementName()
meth public abstract !hasdefault java.lang.String[] acceptedExtension()
meth public abstract !hasdefault java.lang.String[] checkedExtension()
meth public abstract !hasdefault java.lang.String[] doctypePublicId()
meth public abstract !hasdefault java.lang.String[] elementNS()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String mimeType()

CLSS public abstract interface static !annotation org.openide.filesystems.MIMEResolver$Registration
 outer org.openide.filesystems.MIMEResolver
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String[] showInFileChooser()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String resource()

CLSS public abstract org.openide.filesystems.MIMEResolver$UIHelpers
 outer org.openide.filesystems.MIMEResolver
cons protected init(org.openide.filesystems.MIMEResolver)
meth protected final boolean isUserDefined(org.openide.filesystems.FileObject)
meth protected final java.util.Collection<? extends org.openide.filesystems.FileObject> getOrderedResolvers()
meth protected final java.util.Map<java.lang.String,java.util.Set<java.lang.String>> getMIMEToExtensions(org.openide.filesystems.FileObject)
meth protected final void storeUserDefinedResolver(java.util.Map<java.lang.String,java.util.Set<java.lang.String>>)
supr java.lang.Object

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

CLSS public abstract org.openide.filesystems.MultiFileSystemCompat
cons public init()
meth protected abstract org.openide.filesystems.FileSystem[] getDelegates()
meth public org.openide.util.actions.SystemAction[] getActions()
meth public org.openide.util.actions.SystemAction[] getActions(java.util.Set<org.openide.filesystems.FileObject>)
meth public void prepareEnvironment(org.openide.filesystems.FileSystem$Environment) throws org.openide.filesystems.EnvironmentNotSupportedException
 anno 0 java.lang.Deprecated()
supr org.openide.filesystems.FileSystem

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

CLSS public abstract static org.openide.filesystems.Repository$LayerProvider
 outer org.openide.filesystems.Repository
cons public init()
meth protected abstract void registerLayers(java.util.Collection<? super java.net.URL>)
meth protected final void refresh()
supr java.lang.Object

CLSS public abstract static org.openide.filesystems.Repository$LocalProvider
 outer org.openide.filesystems.Repository
cons public init()
meth protected final org.openide.filesystems.Repository delayFilesystemAttach(java.util.concurrent.Callable<org.openide.filesystems.Repository>) throws java.io.IOException
meth public abstract org.openide.filesystems.Repository getRepository() throws java.io.IOException
supr java.lang.Object

CLSS public org.openide.filesystems.RepositoryAdapter
cons public init()
intf org.openide.filesystems.RepositoryListener
meth public void fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public void fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public void fileSystemRemoved(org.openide.filesystems.RepositoryEvent)
supr java.lang.Object

CLSS public org.openide.filesystems.RepositoryEvent
cons public init(org.openide.filesystems.Repository,org.openide.filesystems.FileSystem,boolean)
meth public boolean isAdded()
meth public org.openide.filesystems.FileSystem getFileSystem()
meth public org.openide.filesystems.Repository getRepository()
supr java.util.EventObject
hfds add,fileSystem,serialVersionUID

CLSS public abstract interface org.openide.filesystems.RepositoryListener
intf java.util.EventListener
meth public abstract void fileSystemAdded(org.openide.filesystems.RepositoryEvent)
meth public abstract void fileSystemPoolReordered(org.openide.filesystems.RepositoryReorderedEvent)
meth public abstract void fileSystemRemoved(org.openide.filesystems.RepositoryEvent)

CLSS public org.openide.filesystems.RepositoryReorderedEvent
cons public init(org.openide.filesystems.Repository,int[])
meth public int[] getPermutation()
meth public org.openide.filesystems.Repository getRepository()
supr java.util.EventObject
hfds perm,serialVersionUID

CLSS public abstract interface org.openide.filesystems.StatusDecorator
meth public abstract java.lang.String annotateName(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public abstract java.lang.String annotateNameHtml(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)

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

CLSS public final org.openide.filesystems.XMLFileSystem
cons public init()
cons public init(java.lang.String) throws org.xml.sax.SAXException
cons public init(java.net.URL) throws org.xml.sax.SAXException
innr public static Impl
meth protected <%0 extends org.openide.filesystems.FileObject> java.lang.ref.Reference<{%%0}> createReference({%%0})
meth public boolean isReadOnly()
meth public java.lang.String getDisplayName()
meth public java.net.URL getXmlUrl()
meth public java.net.URL[] getXmlUrls()
meth public void addNotify()
meth public void removeNotify()
meth public void setXmlUrl(java.net.URL) throws java.beans.PropertyVetoException,java.io.IOException
meth public void setXmlUrl(java.net.URL,boolean) throws java.beans.PropertyVetoException,java.io.IOException
meth public void setXmlUrls(java.net.URL[]) throws java.beans.PropertyVetoException,java.io.IOException
supr org.openide.filesystems.AbstractFileSystem
hfds DTD_MAP,rootRef,serialVersionUID,urlsToXml
hcls FileObjRef,Handler,ResourceElem

CLSS public static org.openide.filesystems.XMLFileSystem$Impl
 outer org.openide.filesystems.XMLFileSystem
cons public init(org.openide.filesystems.XMLFileSystem)
intf org.openide.filesystems.AbstractFileSystem$Attr
intf org.openide.filesystems.AbstractFileSystem$Change
intf org.openide.filesystems.AbstractFileSystem$Info
intf org.openide.filesystems.AbstractFileSystem$List
meth public boolean folder(java.lang.String)
meth public boolean readOnly(java.lang.String)
meth public java.io.InputStream inputStream(java.lang.String) throws java.io.FileNotFoundException
meth public java.io.OutputStream outputStream(java.lang.String) throws java.io.IOException
meth public java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String mimeType(java.lang.String)
meth public java.lang.String[] children(java.lang.String)
meth public java.util.Date lastModified(java.lang.String)
meth public java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth public long size(java.lang.String)
meth public void createData(java.lang.String) throws java.io.IOException
meth public void createFolder(java.lang.String) throws java.io.IOException
meth public void delete(java.lang.String) throws java.io.IOException
meth public void deleteAttributes(java.lang.String)
meth public void lock(java.lang.String) throws java.io.IOException
meth public void markUnimportant(java.lang.String)
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void renameAttributes(java.lang.String,java.lang.String)
meth public void unlock(java.lang.String)
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr java.lang.Object
hfds fs,serialVersionUID

CLSS public abstract org.openide.filesystems.XMLFileSystemCompat
cons public init()
meth public abstract java.io.File getRootDirectory()
meth public static void createLocalFileSystemCompat(org.openide.filesystems.XMLFileSystemCompat,org.openide.filesystems.FileSystemCapability)
meth public void prepareEnvironment(org.openide.filesystems.FileSystem$Environment)
 anno 0 java.lang.Deprecated()
supr org.openide.filesystems.AbstractFileSystem

CLSS public abstract interface !annotation org.openide.modules.PatchFor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

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

CLSS public org.openide.util.lookup.AbstractLookup
cons protected init()
cons public init(org.openide.util.lookup.AbstractLookup$Content)
innr public abstract static Pair
innr public static Content
intf java.io.Serializable
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>,java.util.concurrent.Executor)
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth protected void initialize()
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds LOG,count,serialVersionUID,tree,treeLock
hcls CycleError,ISE,Info,NotifyListeners,R,ReferenceIterator,ReferenceToResult,Storage

