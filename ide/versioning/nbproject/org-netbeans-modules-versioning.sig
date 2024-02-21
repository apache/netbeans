#Signature file v4.1
#Version 1.70.0

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

CLSS public abstract org.netbeans.modules.versioning.spi.VCSAnnotator
cons protected init()
innr public final static !enum ActionDestination
meth public java.awt.Image annotateIcon(java.awt.Image,org.netbeans.modules.versioning.spi.VCSContext)
meth public java.lang.String annotateName(java.lang.String,org.netbeans.modules.versioning.spi.VCSContext)
meth public javax.swing.Action[] getActions(org.netbeans.modules.versioning.spi.VCSContext,org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination)
supr java.lang.Object

CLSS public final static !enum org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination
 outer org.netbeans.modules.versioning.spi.VCSAnnotator
fld public final static org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination MainMenu
fld public final static org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination PopupMenu
meth public static org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination valueOf(java.lang.String)
meth public static org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination[] values()
supr java.lang.Enum<org.netbeans.modules.versioning.spi.VCSAnnotator$ActionDestination>

CLSS public final org.netbeans.modules.versioning.spi.VCSContext
fld public final static org.netbeans.modules.versioning.spi.VCSContext EMPTY
meth public boolean contains(java.io.File)
meth public java.util.Set<java.io.File> computeFiles(java.io.FileFilter)
meth public java.util.Set<java.io.File> getExclusions()
meth public java.util.Set<java.io.File> getFiles()
meth public java.util.Set<java.io.File> getRootFiles()
meth public org.openide.util.Lookup getElements()
meth public static org.netbeans.modules.versioning.spi.VCSContext forNodes(org.openide.nodes.Node[])
supr java.lang.Object
hfds computedFilesCached,delegate,exclusions,fileFilterCached,rootFiles,unfilteredRootFiles
hcls ProxyFileFilter

CLSS public abstract interface org.netbeans.modules.versioning.spi.VCSHistoryProvider
innr public abstract interface static HistoryChangeListener
innr public abstract interface static MessageEditProvider
innr public abstract interface static ParentProvider
innr public abstract interface static RevisionProvider
innr public final static HistoryEntry
innr public final static HistoryEvent
meth public abstract javax.swing.Action createShowHistoryAction(java.io.File[])
meth public abstract org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEntry[] getHistory(java.io.File[],java.util.Date)
meth public abstract void addHistoryChangeListener(org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryChangeListener)
meth public abstract void removeHistoryChangeListener(org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryChangeListener)

CLSS public abstract interface static org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryChangeListener
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
meth public abstract void fireHistoryChanged(org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEvent)

CLSS public final static org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEntry
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
cons public init(java.io.File[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.spi.VCSHistoryProvider$RevisionProvider)
cons public init(java.io.File[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.spi.VCSHistoryProvider$RevisionProvider,org.netbeans.modules.versioning.spi.VCSHistoryProvider$MessageEditProvider)
cons public init(java.io.File[],java.util.Date,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Action[],org.netbeans.modules.versioning.spi.VCSHistoryProvider$RevisionProvider,org.netbeans.modules.versioning.spi.VCSHistoryProvider$MessageEditProvider,org.netbeans.modules.versioning.spi.VCSHistoryProvider$ParentProvider)
meth public boolean canEdit()
meth public java.io.File[] getFiles()
meth public java.lang.String getMessage()
meth public java.lang.String getRevision()
meth public java.lang.String getRevisionShort()
meth public java.lang.String getUsername()
meth public java.lang.String getUsernameShort()
meth public java.util.Date getDateTime()
meth public javax.swing.Action[] getActions()
meth public org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEntry getParentEntry(java.io.File)
meth public void getRevisionFile(java.io.File,java.io.File)
meth public void setMessage(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds actions,dateTime,files,message,messageEditProvider,parentProvider,revision,revisionProvider,revisionShort,username,usernameShort

CLSS public final static org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEvent
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
cons public init(org.netbeans.modules.versioning.spi.VCSHistoryProvider,java.io.File[])
meth public java.io.File[] getFiles()
meth public org.netbeans.modules.versioning.spi.VCSHistoryProvider getSource()
supr java.lang.Object
hfds files,source

CLSS public abstract interface static org.netbeans.modules.versioning.spi.VCSHistoryProvider$MessageEditProvider
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
meth public abstract void setMessage(java.lang.String) throws java.io.IOException

CLSS public abstract interface static org.netbeans.modules.versioning.spi.VCSHistoryProvider$ParentProvider
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
meth public abstract org.netbeans.modules.versioning.spi.VCSHistoryProvider$HistoryEntry getParentEntry(java.io.File)

CLSS public abstract interface static org.netbeans.modules.versioning.spi.VCSHistoryProvider$RevisionProvider
 outer org.netbeans.modules.versioning.spi.VCSHistoryProvider
meth public abstract void getRevisionFile(java.io.File,java.io.File)

CLSS public abstract org.netbeans.modules.versioning.spi.VCSInterceptor
cons protected init()
meth public boolean beforeCopy(java.io.File,java.io.File)
meth public boolean beforeCreate(java.io.File,boolean)
meth public boolean beforeDelete(java.io.File)
meth public boolean beforeMove(java.io.File,java.io.File)
meth public boolean isMutable(java.io.File)
meth public java.lang.Object getAttribute(java.io.File,java.lang.String)
meth public long refreshRecursively(java.io.File,long,java.util.List<? super java.io.File>)
meth public void afterChange(java.io.File)
meth public void afterCopy(java.io.File,java.io.File)
meth public void afterCreate(java.io.File)
meth public void afterDelete(java.io.File)
meth public void afterMove(java.io.File,java.io.File)
meth public void beforeChange(java.io.File)
meth public void beforeEdit(java.io.File)
meth public void doCopy(java.io.File,java.io.File) throws java.io.IOException
meth public void doCreate(java.io.File,boolean) throws java.io.IOException
meth public void doDelete(java.io.File) throws java.io.IOException
meth public void doMove(java.io.File,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.spi.VCSVisibilityQuery
cons public init()
meth protected !varargs final void fireVisibilityChanged(java.io.File[])
meth protected final void fireVisibilityChanged()
meth public abstract boolean isVisible(java.io.File)
supr java.lang.Object

CLSS public final org.netbeans.modules.versioning.spi.VersioningSupport
fld public final static java.lang.String PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE = "textAnnotationsVisible"
meth public static boolean isExcluded(java.io.File)
meth public static boolean isFlat(java.io.File)
meth public static boolean isTextAnnotationVisible()
meth public static java.io.File getFlat(java.lang.String)
meth public static java.util.prefs.Preferences getPreferences()
meth public static org.netbeans.modules.versioning.spi.VersioningSystem getOwner(java.io.File)
meth public static void versionedRootsChanged()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.versioning.spi.VersioningSystem
cons protected init()
fld public final static java.lang.String PROP_DISPLAY_NAME = "String VCS.DisplayName"
fld public final static java.lang.String PROP_LOCALHISTORY_VCS = "Boolean VCS.LocalHistory"
fld public final static java.lang.String PROP_MENU_LABEL = "String VCS.MenuLabel"
innr public abstract interface static !annotation Registration
meth protected final void fireAnnotationsChanged(java.util.Set<java.io.File>)
meth protected final void fireStatusChanged(java.io.File)
meth protected final void fireStatusChanged(java.util.Set<java.io.File>)
meth protected final void fireVersionedFilesChanged()
meth protected final void putProperty(java.lang.String,java.lang.Object)
meth public final java.lang.Object getProperty(java.lang.String)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.io.File getTopmostManagedAncestor(java.io.File)
meth public org.netbeans.modules.versioning.spi.VCSAnnotator getVCSAnnotator()
meth public org.netbeans.modules.versioning.spi.VCSHistoryProvider getVCSHistoryProvider()
meth public org.netbeans.modules.versioning.spi.VCSInterceptor getVCSInterceptor()
meth public org.netbeans.modules.versioning.spi.VCSVisibilityQuery getVisibilityQuery()
meth public org.netbeans.spi.queries.CollocationQueryImplementation getCollocationQueryImplementation()
meth public void getOriginalFile(java.io.File,java.io.File)
supr java.lang.Object
hfds properties,support

CLSS public abstract interface static !annotation org.netbeans.modules.versioning.spi.VersioningSystem$Registration
 outer org.netbeans.modules.versioning.spi.VersioningSystem
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String actionsCategory()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String menuLabel()
meth public abstract java.lang.String[] metadataFolderNames()

