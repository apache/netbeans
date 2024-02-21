#Signature file v4.1
#Version 1.54.0

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

CLSS public final org.netbeans.modules.versioning.core.api.VCSFileProxy
intf java.lang.Comparable<org.netbeans.modules.versioning.core.api.VCSFileProxy>
meth public boolean canWrite()
meth public boolean equals(java.lang.Object)
meth public boolean exists()
meth public boolean isDirectory()
meth public boolean isFile()
meth public int compareTo(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public int hashCode()
meth public java.io.File toFile()
meth public java.io.InputStream getInputStream(boolean) throws java.io.FileNotFoundException
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.net.URI toURI() throws java.net.URISyntaxException
meth public long lastModified()
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy getParentFile()
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy normalizeFile()
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy[] listFiles()
meth public org.openide.filesystems.FileObject toFileObject()
meth public static org.netbeans.modules.versioning.core.api.VCSFileProxy createFileProxy(java.io.File)
meth public static org.netbeans.modules.versioning.core.api.VCSFileProxy createFileProxy(java.net.URI)
meth public static org.netbeans.modules.versioning.core.api.VCSFileProxy createFileProxy(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String)
meth public static org.netbeans.modules.versioning.core.api.VCSFileProxy createFileProxy(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds fileChangeListener,isDirectory,isFlat,path,proxy

CLSS public final org.netbeans.modules.versioning.core.api.VersioningSupport
fld public final static java.lang.String PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE = "textAnnotationsVisible"
meth public static boolean isExcluded(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static boolean isFlat(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static boolean isTextAnnotationVisible()
meth public static java.util.prefs.Preferences getPreferences()
meth public static org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static org.netbeans.modules.versioning.core.spi.VersioningSystem getOwner(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void refreshFor(org.netbeans.modules.versioning.core.api.VCSFileProxy[])
meth public static void versionedRootsChanged()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations
fld public final static java.lang.String ATTRIBUTE = "FileProxyOperations"
innr public abstract interface static Provider
meth public abstract !varargs void refreshFor(org.netbeans.modules.versioning.core.api.VCSFileProxy[])
meth public abstract boolean canWrite(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract boolean exists(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract boolean isDirectory(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract boolean isFile(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract java.io.InputStream getInputStream(org.netbeans.modules.versioning.core.api.VCSFileProxy,boolean) throws java.io.FileNotFoundException
meth public abstract java.lang.String getAbsolutePath(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract java.lang.String getName(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract java.net.URI toURI(org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.net.URISyntaxException
meth public abstract long lastModified(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.netbeans.api.extexecution.ProcessBuilder createProcessBuilder(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.netbeans.modules.versioning.core.api.VCSFileProxy getParentFile(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.netbeans.modules.versioning.core.api.VCSFileProxy normalize(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.netbeans.modules.versioning.core.api.VCSFileProxy[] list(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.openide.filesystems.FileObject toFileObject(org.netbeans.modules.versioning.core.api.VCSFileProxy)

CLSS public abstract interface static org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations$Provider
 outer org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations
meth public abstract org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations getVCSFileProxyOperations(java.net.URI)
meth public abstract org.netbeans.modules.versioning.core.filesystems.VCSFileProxyOperations getVCSFileProxyOperations(org.openide.filesystems.FileSystem)

CLSS public final org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor
innr public abstract interface static IOHandler
innr public abstract interface static VCSAnnotationListener
innr public final static VCSAnnotationEvent
meth public static boolean canWriteReadonlyFile(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static java.awt.Image annotateIcon(java.awt.Image,int,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public static java.lang.Object getAttribute(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String)
meth public static java.lang.String annotateNameHtml(java.lang.String,java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public static javax.swing.Action[] actions(java.util.Set<? extends org.openide.filesystems.FileObject>)
meth public static long listFiles(org.netbeans.modules.versioning.core.api.VCSFileProxy,long,java.util.List<? super org.netbeans.modules.versioning.core.api.VCSFileProxy>)
meth public static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$IOHandler getCopyHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$IOHandler getDeleteHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$IOHandler getMoveHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$IOHandler getRenameHandler(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String)
meth public static void afterMove(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void beforeChange(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void beforeCopy(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void beforeCreate(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String,boolean)
meth public static void copySuccess(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void createFailure(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String,boolean)
meth public static void createSuccess(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void createdExternally(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void deleteSuccess(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void deletedExternally(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void fileChanged(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void fileLocked(org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.io.IOException
meth public static void registerFileStatusListener(org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$VCSAnnotationListener)
supr java.lang.Object
hfds ATTRIBUTE_IS_MODIFIED,ATTRIBUTE_REFRESH,ATTRIBUTE_REMOTE_LOCATION,ATTRIBUTE_SEARCH_HISTORY,LOG,deletedFiles,filesBeingCreated,master,nullDelegatingInterceptor,nullInterceptor
hcls DelegatingInterceptor,FileEx

CLSS public abstract interface static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$IOHandler
 outer org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor
meth public abstract void handle() throws java.io.IOException

CLSS public final static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$VCSAnnotationEvent
 outer org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor
cons public init(boolean,boolean)
cons public init(java.util.Set<? extends org.openide.filesystems.FileObject>,boolean,boolean)
cons public init(org.openide.filesystems.FileObject,boolean,boolean)
meth public boolean isIconChange()
meth public boolean isNameChange()
meth public java.util.Set<? extends org.openide.filesystems.FileObject> getFiles()
supr java.lang.Object
hfds files,icon,name

CLSS public abstract interface static org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$VCSAnnotationListener
 outer org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor
meth public abstract void annotationChanged(org.netbeans.modules.versioning.core.filesystems.VCSFilesystemInterceptor$VCSAnnotationEvent)

CLSS public abstract org.netbeans.modules.versioning.core.spi.VCSAnnotator
cons protected init()
innr public final static !enum ActionDestination
meth public java.awt.Image annotateIcon(java.awt.Image,org.netbeans.modules.versioning.core.spi.VCSContext)
meth public java.lang.String annotateName(java.lang.String,org.netbeans.modules.versioning.core.spi.VCSContext)
meth public javax.swing.Action[] getActions(org.netbeans.modules.versioning.core.spi.VCSContext,org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination
 outer org.netbeans.modules.versioning.core.spi.VCSAnnotator
fld public final static org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination MainMenu
fld public final static org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination PopupMenu
meth public static org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.core.spi.VCSAnnotator$ActionDestination>

CLSS public final org.netbeans.modules.versioning.core.spi.VCSContext
fld public final static org.netbeans.modules.versioning.core.spi.VCSContext EMPTY
innr public abstract interface static FileFilter
meth public boolean contains(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy> computeFiles(org.netbeans.modules.versioning.core.spi.VCSContext$FileFilter)
meth public java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy> getExclusions()
meth public java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy> getFiles()
meth public java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy> getRootFiles()
meth public org.openide.util.Lookup getElements()
meth public static org.netbeans.modules.versioning.core.spi.VCSContext forNodes(org.openide.nodes.Node[])
supr java.lang.Object
hfds LOG,computedFilesCached,contextCached,contextNodesCached,elements,exclusions,fileFilterCached,rootFiles,unfilteredRootFiles

CLSS public abstract interface static org.netbeans.modules.versioning.core.spi.VCSContext$FileFilter
 outer org.netbeans.modules.versioning.core.spi.VCSContext
meth public abstract boolean accept(org.netbeans.modules.versioning.core.api.VCSFileProxy)

CLSS public abstract interface org.netbeans.modules.versioning.core.spi.VCSForbiddenFolderProvider
meth public abstract boolean isForbiddenFolder(org.netbeans.modules.versioning.core.api.VCSFileProxy)

CLSS public abstract interface org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
innr public abstract interface static HistoryChangeListener
innr public abstract interface static MessageEditProvider
innr public abstract interface static ParentProvider
innr public abstract interface static RevisionProvider
innr public final static HistoryEntry
innr public final static HistoryEvent
meth public abstract javax.swing.Action createShowHistoryAction(org.netbeans.modules.versioning.core.api.VCSFileProxy[])
meth public abstract org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry[] getHistory(org.netbeans.modules.versioning.core.api.VCSFileProxy[],java.util.Date)
meth public abstract void addHistoryChangeListener(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryChangeListener)
meth public abstract void removeHistoryChangeListener(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryChangeListener)

CLSS public abstract interface static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryChangeListener
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
meth public abstract void fireHistoryChanged(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEvent)

CLSS public final static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
cons public init(org.netbeans.modules.versioning.core.api.VCSFileProxy[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$RevisionProvider)
cons public init(org.netbeans.modules.versioning.core.api.VCSFileProxy[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$RevisionProvider,org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$MessageEditProvider)
cons public init(org.netbeans.modules.versioning.core.api.VCSFileProxy[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$RevisionProvider,org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$MessageEditProvider,org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$ParentProvider)
meth public boolean canEdit()
meth public java.lang.String getMessage()
meth public java.lang.String getRevision()
meth public java.lang.String getRevisionShort()
meth public java.lang.String getUsername()
meth public java.lang.String getUsernameShort()
meth public java.util.Date getDateTime()
meth public javax.swing.Action[] getActions()
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy[] getFiles()
meth public org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry getParentEntry(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void getRevisionFile(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void setMessage(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds actions,dateTime,files,lookupObjects,mep,message,parentProvider,revision,revisionProvider,revisionShort,username,usernameShort

CLSS public final static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEvent
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
cons public init(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider,org.netbeans.modules.versioning.core.api.VCSFileProxy[])
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy[] getFiles()
meth public org.netbeans.modules.versioning.core.spi.VCSHistoryProvider getSource()
supr java.lang.Object
hfds files,source

CLSS public abstract interface static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$MessageEditProvider
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
meth public abstract void setMessage(java.lang.String) throws java.io.IOException

CLSS public abstract interface static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$ParentProvider
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
meth public abstract org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry getParentEntry(org.netbeans.modules.versioning.core.api.VCSFileProxy)

CLSS public abstract interface static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$RevisionProvider
 outer org.netbeans.modules.versioning.core.spi.VCSHistoryProvider
meth public abstract void getRevisionFile(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)

CLSS public abstract org.netbeans.modules.versioning.core.spi.VCSInterceptor
cons protected init()
meth public boolean beforeCopy(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public boolean beforeCreate(org.netbeans.modules.versioning.core.api.VCSFileProxy,boolean)
meth public boolean beforeDelete(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public boolean beforeMove(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public boolean isMutable(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public java.lang.Object getAttribute(org.netbeans.modules.versioning.core.api.VCSFileProxy,java.lang.String)
meth public long refreshRecursively(org.netbeans.modules.versioning.core.api.VCSFileProxy,long,java.util.List<? super org.netbeans.modules.versioning.core.api.VCSFileProxy>)
meth public void afterChange(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void afterCopy(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void afterCreate(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void afterDelete(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void afterMove(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void beforeChange(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public void beforeEdit(org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.io.IOException
meth public void doCopy(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.io.IOException
meth public void doCreate(org.netbeans.modules.versioning.core.api.VCSFileProxy,boolean) throws java.io.IOException
meth public void doDelete(org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.io.IOException
meth public void doMove(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery
cons public init()
meth protected !varargs final void fireVisibilityChanged(org.netbeans.modules.versioning.core.api.VCSFileProxy[])
meth protected final void fireVisibilityChanged()
meth public abstract boolean isVisible(org.netbeans.modules.versioning.core.api.VCSFileProxy)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.core.spi.VersioningSystem
cons protected init()
innr public abstract interface static !annotation Registration
meth protected final void fireAnnotationsChanged(java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy>)
meth protected final void fireStatusChanged(java.util.Set<org.netbeans.modules.versioning.core.api.VCSFileProxy>)
meth protected final void fireStatusChanged(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth protected final void fireVersionedFilesChanged()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public org.netbeans.modules.versioning.core.api.VCSFileProxy getTopmostManagedAncestor(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public org.netbeans.modules.versioning.core.spi.VCSAnnotator getVCSAnnotator()
meth public org.netbeans.modules.versioning.core.spi.VCSHistoryProvider getVCSHistoryProvider()
meth public org.netbeans.modules.versioning.core.spi.VCSInterceptor getVCSInterceptor()
meth public org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery getVisibilityQuery()
meth public org.netbeans.spi.queries.CollocationQueryImplementation2 getCollocationQueryImplementation()
meth public void getOriginalFile(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
supr java.lang.Object
hfds support

CLSS public abstract interface static !annotation org.netbeans.modules.versioning.core.spi.VersioningSystem$Registration
 outer org.netbeans.modules.versioning.core.spi.VersioningSystem
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String actionsCategory()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String menuLabel()
meth public abstract java.lang.String[] metadataFolderNames()

CLSS public final org.netbeans.modules.versioning.core.util.Utils
fld public final static java.lang.String EVENT_ANNOTATIONS_CHANGED = "Set<File> VCS.AnnotationsChanged"
fld public final static java.lang.String EVENT_STATUS_CHANGED = "Set<File> VCS.StatusChanged"
fld public final static java.lang.String EVENT_VERSIONED_ROOTS = "null VCS.VersionedFilesChanged"
meth public !varargs static void fireVisibilityChanged(java.io.File[])
meth public static boolean isFlat(java.io.File)
meth public static boolean isForbiddenFolder(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static java.io.File getFlat(java.lang.String)
meth public static java.lang.Object[] getDelegateEntry(org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry)
meth public static java.lang.String[] getDisconnectedRoots(org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem)
meth public static org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$HistoryEntry createHistoryEntry(org.netbeans.modules.versioning.core.api.VCSFileProxy[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$RevisionProvider,org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$MessageEditProvider,org.netbeans.modules.versioning.core.spi.VCSHistoryProvider$ParentProvider,java.lang.Object[])
meth public static org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem getLocalHistory(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem getOwner(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public static void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void connectRepository(org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem,java.lang.String)
meth public static void disconnectRepository(org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem,java.lang.String)
meth public static void fireVisibilityChanged()
meth public static void flushNullOwners()
meth public static void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void versionedRootsChanged()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.core.util.VCSSystemProvider
cons public init()
innr public abstract interface static VersioningSystem
meth public abstract java.util.Collection<org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem> getVersioningSystems()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem<%0 extends java.lang.Object>
 outer org.netbeans.modules.versioning.core.util.VCSSystemProvider
meth public abstract boolean accept(org.netbeans.modules.versioning.core.spi.VCSContext)
meth public abstract boolean isExcluded(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract boolean isLocalHistory()
meth public abstract boolean isMetadataFile(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getMenuLabel()
meth public abstract org.netbeans.modules.versioning.core.api.VCSFileProxy getTopmostManagedAncestor(org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract org.netbeans.modules.versioning.core.spi.VCSAnnotator getVCSAnnotator()
meth public abstract org.netbeans.modules.versioning.core.spi.VCSHistoryProvider getVCSHistoryProvider()
meth public abstract org.netbeans.modules.versioning.core.spi.VCSInterceptor getVCSInterceptor()
meth public abstract org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery getVisibilityQuery()
meth public abstract org.netbeans.spi.queries.CollocationQueryImplementation2 getCollocationQueryImplementation()
meth public abstract void addPropertyCL(java.beans.PropertyChangeListener)
meth public abstract void getOriginalFile(org.netbeans.modules.versioning.core.api.VCSFileProxy,org.netbeans.modules.versioning.core.api.VCSFileProxy)
meth public abstract void removePropertyCL(java.beans.PropertyChangeListener)
meth public abstract {org.netbeans.modules.versioning.core.util.VCSSystemProvider$VersioningSystem%0} getDelegate()

