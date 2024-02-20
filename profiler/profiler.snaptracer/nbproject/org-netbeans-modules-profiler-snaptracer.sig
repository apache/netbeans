#Signature file v4.1
#Version 1.47

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.api.actions.Openable
meth public abstract void open()

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException
innr public final static LogRecordInfo
meth public boolean hasUiGestures()
meth public long getValue(int,int) throws java.io.IOException
meth public org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot$LogRecordInfo getLogInfoForValue(long)
supr java.lang.Object
hfds cpuSnapshot,infosMap,lastRecord,npssFileObject,recordsMap,xmlLogs
hcls LogRecordDecorator

CLSS public final static org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot$LogRecordInfo
 outer org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getToolTip()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds MAX_DISPLAY_NAME,displayName,icon,name,record,toolTip

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshotAction
cons public init()
intf java.awt.event.ActionListener
meth public void actionPerformed(java.awt.event.ActionEvent)
supr java.lang.Object
hfds lastDirectory
hcls Filter,IdeSnapshotComponent

CLSS public org.netbeans.modules.profiler.snaptracer.impl.NpssDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws java.io.IOException
intf org.openide.cookies.OpenCookie
meth protected org.openide.nodes.Node createNodeDelegate()
meth public org.openide.util.Lookup getLookup()
meth public void open()
supr org.openide.loaders.MultiDataObject

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.TracerController
cons public init(org.netbeans.modules.profiler.snaptracer.impl.TracerModel)
supr java.lang.Object
hfds LOGGER,PROPERTY_STATE,STATE_SESSION_IMPOSSIBLE,STATE_SESSION_INACTIVE,STATE_SESSION_RUNNING,STATE_SESSION_STARTING,STATE_SESSION_STOPPING,changeSupport,error,model,processor,progress,state,wasNegativeValue

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.TracerModel
cons public init(org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot)
supr java.lang.Object
hfds LOGGER,descriptorsCache,listeners,probesCache,snapshot,timelineSupport
hcls Listener

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.TracerSupportImpl
meth public boolean hasPackages(java.lang.Object)
meth public java.util.List<org.netbeans.modules.profiler.snaptracer.TracerPackage> getPackages(org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot)
meth public static org.netbeans.modules.profiler.snaptracer.impl.TracerSupportImpl getInstance()
meth public void perform(java.lang.Runnable)
meth public void registerPackageProvider(org.netbeans.modules.profiler.snaptracer.TracerPackageProvider)
meth public void unregisterPackageProvider(org.netbeans.modules.profiler.snaptracer.TracerPackageProvider)
supr java.lang.Object
hfds INSTANCE,PROCESSOR,providers

CLSS public final org.netbeans.modules.profiler.snaptracer.impl.TracerView
cons public init(org.netbeans.modules.profiler.snaptracer.impl.TracerModel,org.netbeans.modules.profiler.snaptracer.impl.TracerController)
meth public javax.swing.JComponent createComponent()
supr java.lang.Object
hfds controller,lsF,model,snapshotView,timelineView
hcls FindMethodAction,SnapshotView

CLSS public abstract interface org.openide.cookies.OpenCookie
intf org.netbeans.api.actions.Openable
intf org.openide.nodes.Node$Cookie

CLSS public abstract org.openide.loaders.DataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_FILES = "files"
fld public final static java.lang.String PROP_HELP = "helpCtx"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIMARY_FILE = "primaryFile"
fld public final static java.lang.String PROP_TEMPLATE = "template"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static Container
innr public abstract interface static Factory
innr public final static Registry
intf java.io.Serializable
intf org.openide.nodes.Node$Cookie
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(org.openide.loaders.DataShadow,java.lang.Class<{%%0}>)
meth protected abstract org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected abstract void handleDelete() throws java.io.IOException
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void markFiles() throws java.io.IOException
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void dispose()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isCopyAllowed()
meth public abstract boolean isDeleteAllowed()
meth public abstract boolean isMoveAllowed()
meth public abstract boolean isRenameAllowed()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public boolean isModified()
meth public boolean isShadowAllowed()
meth public final boolean isTemplate()
meth public final boolean isValid()
meth public final org.openide.filesystems.FileObject getPrimaryFile()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final org.openide.loaders.DataLoader getLoader()
meth public final org.openide.loaders.DataObject copy(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public final org.openide.loaders.DataShadow createShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.nodes.Node getNodeDelegate()
meth public final void delete() throws java.io.IOException
meth public final void move(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final void rename(java.lang.String) throws java.io.IOException
meth public final void setTemplate(boolean) throws java.io.IOException
meth public java.lang.Object writeReplace()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.loaders.DataObject find(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectNotFoundException
meth public static org.openide.loaders.DataObject$Registry getRegistry()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void setModified(boolean)
meth public void setValid(boolean) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds BEING_CREATED,EA_ASSIGNED_LOADER,EA_ASSIGNED_LOADER_MODULE,LOCK,LOG,OBJ_LOG,PROGRESS_INFO_TL,REGISTRY_INSTANCE,changeSupport,changeSupportUpdater,item,loader,modif,modified,nodeDelegate,serialVersionUID,syncModified,synchObject,vetoableChangeSupport,warnedClasses
hcls CreateAction,DOSavable,ModifiedRegistry,ProgressInfo,Replace

CLSS public org.openide.loaders.MultiDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
innr public abstract Entry
meth protected final org.openide.loaders.MultiDataObject$Entry registerEntry(org.openide.filesystems.FileObject)
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final void addSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void registerEditor(java.lang.String,boolean)
meth protected final void removeSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected int associateLookup()
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public final java.util.Set<org.openide.loaders.MultiDataObject$Entry> secondaryEntries()
meth public final org.openide.loaders.MultiDataObject$Entry findSecondaryEntry(org.openide.filesystems.FileObject)
meth public final org.openide.loaders.MultiDataObject$Entry getPrimaryEntry()
meth public final org.openide.loaders.MultiFileLoader getMultiFileLoader()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
supr org.openide.loaders.DataObject
hfds ERR,RECOGNIZER,TEMPLATE_ATTRIBUTES,chLis,checked,cookieSet,cookieSetLock,delayProcessor,delayedPropFilesLock,delayedPropFilesTask,firingProcessor,later,primary,secondary,secondaryCreationLock,serialVersionUID
hcls ChangeAndBefore,EmptyRecognizer,EntryReplace,Pair

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

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

