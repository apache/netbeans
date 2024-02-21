#Signature file v4.1
#Version 2.26.0

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

CLSS public abstract org.netbeans.modules.masterfs.providers.AnnotationProvider
cons public init()
meth public abstract java.awt.Image annotateIcon(java.awt.Image,int,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public javax.swing.Action[] actions(java.util.Set<? extends org.openide.filesystems.FileObject>)
 anno 0 java.lang.Deprecated()
meth public org.openide.util.Lookup findExtrasFor(java.util.Set<? extends org.openide.filesystems.FileObject>)
supr org.netbeans.modules.masterfs.providers.BaseAnnotationProvider

CLSS public org.netbeans.modules.masterfs.providers.Attributes
cons public init(java.io.File,org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List)
cons public init(org.openide.filesystems.AbstractFileSystem$Info,org.openide.filesystems.AbstractFileSystem$Change,org.openide.filesystems.AbstractFileSystem$List)
fld public static java.lang.String ATTRNAME
meth public java.lang.Object readAttribute(java.lang.String,java.lang.String)
meth public java.lang.String[] children(java.lang.String)
meth public java.util.Enumeration<java.lang.String> attributes(java.lang.String)
meth public static java.io.File getRootForAttributes()
meth public void deleteAttributes(java.lang.String)
meth public void renameAttributes(java.lang.String,java.lang.String)
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.Object) throws java.io.IOException
supr org.openide.filesystems.DefaultAttributes
hfds BACKWARD_COMPATIBILITY,LOCATION,attributePrefix,list,rootForAttributes,sharedUserAttributes

CLSS public abstract org.netbeans.modules.masterfs.providers.BaseAnnotationProvider
cons public init()
meth protected final void fireFileStatusChanged(org.openide.filesystems.FileStatusEvent)
meth public abstract java.lang.String annotateName(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public abstract java.lang.String annotateNameHtml(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public abstract org.netbeans.modules.masterfs.providers.InterceptionListener getInterceptionListener()
meth public final void addFileStatusListener(org.openide.filesystems.FileStatusListener) throws java.util.TooManyListenersException
meth public final void removeFileStatusListener(org.openide.filesystems.FileStatusListener)
meth public org.openide.util.Lookup findExtrasFor(java.util.Set<? extends org.openide.filesystems.FileObject>)
supr java.lang.Object
hfds LOCK,fsStatusListener

CLSS public abstract interface org.netbeans.modules.masterfs.providers.InterceptionListener
meth public abstract void beforeCreate(org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public abstract void beforeDelete(org.openide.filesystems.FileObject)
meth public abstract void createFailure(org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public abstract void createSuccess(org.openide.filesystems.FileObject)
meth public abstract void deleteFailure(org.openide.filesystems.FileObject)
meth public abstract void deleteSuccess(org.openide.filesystems.FileObject)

CLSS public abstract org.netbeans.modules.masterfs.providers.Notifier<%0 extends java.lang.Object>
cons public init()
meth protected abstract java.lang.String nextEvent() throws java.io.IOException,java.lang.InterruptedException
meth protected abstract void removeWatch({org.netbeans.modules.masterfs.providers.Notifier%0}) throws java.io.IOException
meth protected abstract void start() throws java.io.IOException
meth protected abstract {org.netbeans.modules.masterfs.providers.Notifier%0} addWatch(java.lang.String) throws java.io.IOException
meth protected void stop() throws java.io.IOException
supr java.lang.Object

CLSS public org.netbeans.modules.masterfs.providers.ProvidedExtensions
cons public init()
cons public init(boolean)
innr public abstract interface static DeleteHandler
innr public abstract interface static IOHandler
intf org.netbeans.modules.masterfs.providers.InterceptionListener
meth public boolean canWrite(java.io.File)
meth public java.lang.Object getAttribute(java.io.File,java.lang.String)
meth public long refreshRecursively(java.io.File,long,java.util.List<? super java.io.File>)
meth public org.netbeans.modules.masterfs.providers.ProvidedExtensions$DeleteHandler getDeleteHandler(java.io.File)
meth public org.netbeans.modules.masterfs.providers.ProvidedExtensions$IOHandler getCopyHandler(java.io.File,java.io.File)
meth public org.netbeans.modules.masterfs.providers.ProvidedExtensions$IOHandler getMoveHandler(java.io.File,java.io.File)
meth public org.netbeans.modules.masterfs.providers.ProvidedExtensions$IOHandler getRenameHandler(java.io.File,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} priorityIO(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public void beforeChange(org.openide.filesystems.FileObject)
meth public void beforeCopy(org.openide.filesystems.FileObject,java.io.File)
meth public void beforeCreate(org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public void beforeDelete(org.openide.filesystems.FileObject)
meth public void beforeMove(org.openide.filesystems.FileObject,java.io.File)
meth public void copyFailure(org.openide.filesystems.FileObject,java.io.File)
meth public void copySuccess(org.openide.filesystems.FileObject,java.io.File)
meth public void createFailure(org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public void createSuccess(org.openide.filesystems.FileObject)
meth public void createdExternally(org.openide.filesystems.FileObject)
meth public void deleteFailure(org.openide.filesystems.FileObject)
meth public void deleteSuccess(org.openide.filesystems.FileObject)
meth public void deletedExternally(org.openide.filesystems.FileObject)
meth public void fileChanged(org.openide.filesystems.FileObject)
meth public void fileLocked(org.openide.filesystems.FileObject) throws java.io.IOException
meth public void fileUnlocked(org.openide.filesystems.FileObject)
meth public void moveFailure(org.openide.filesystems.FileObject,java.io.File)
meth public void moveSuccess(org.openide.filesystems.FileObject,java.io.File)
supr java.lang.Object
hfds providesCanWrite

CLSS public abstract interface static org.netbeans.modules.masterfs.providers.ProvidedExtensions$DeleteHandler
 outer org.netbeans.modules.masterfs.providers.ProvidedExtensions
meth public abstract boolean delete(java.io.File)

CLSS public abstract interface static org.netbeans.modules.masterfs.providers.ProvidedExtensions$IOHandler
 outer org.netbeans.modules.masterfs.providers.ProvidedExtensions
meth public abstract void handle() throws java.io.IOException

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

CLSS public abstract interface static org.openide.filesystems.AbstractFileSystem$List
 outer org.openide.filesystems.AbstractFileSystem
fld public final static long serialVersionUID = -6242105832891012528
 anno 0 java.lang.Deprecated()
intf java.io.Serializable
meth public abstract java.lang.String[] children(java.lang.String)

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

