#Signature file v4.1
#Version 1.92.0

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface org.netbeans.api.db.explorer.ConnectionListener
intf java.util.EventListener
meth public abstract void connectionsChanged()

CLSS public final org.netbeans.api.db.explorer.ConnectionManager
cons public init()
meth public boolean connect(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public org.netbeans.api.db.explorer.DatabaseConnection getConnection(java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection getPreferredConnection(boolean)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection showAddConnectionDialogFromEventThread(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.api.db.explorer.DatabaseConnection[] getConnections()
meth public static org.netbeans.api.db.explorer.ConnectionManager getDefault()
meth public void addConnection(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public void addConnectionListener(org.netbeans.api.db.explorer.ConnectionListener)
meth public void disconnect(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void refreshConnectionInExplorer(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void removeConnection(org.netbeans.api.db.explorer.DatabaseConnection) throws org.netbeans.api.db.explorer.DatabaseException
meth public void removeConnectionListener(org.netbeans.api.db.explorer.ConnectionListener)
meth public void selectConnectionInExplorer(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void setPreferredConnection(org.netbeans.api.db.explorer.DatabaseConnection)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String)
meth public void showAddConnectionDialog(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String)
meth public void showConnectionDialog(org.netbeans.api.db.explorer.DatabaseConnection)
supr java.lang.Object
hfds DEFAULT,LOGGER

CLSS public final org.netbeans.api.db.explorer.DatabaseConnection
meth public boolean isUseScrollableCursors()
meth public java.lang.String getDatabaseURL()
meth public java.lang.String getDisplayName()
meth public java.lang.String getDriverClass()
meth public java.lang.String getName()
meth public java.lang.String getPassword()
meth public java.lang.String getSchema()
meth public java.lang.String getUser()
meth public java.lang.String toString()
meth public java.sql.Connection getJDBCConnection()
meth public java.sql.Connection getJDBCConnection(boolean)
meth public java.util.Properties getConnectionProperties()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
meth public static org.netbeans.api.db.explorer.DatabaseConnection create(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static org.netbeans.api.db.explorer.DatabaseConnection create(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String)
meth public static org.netbeans.api.db.explorer.DatabaseConnection create(org.netbeans.api.db.explorer.JDBCDriver,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,java.util.Properties)
meth public void setUseScrollableCursors(boolean)
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.api.db.explorer.DatabaseException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
fld public static java.awt.datatransfer.DataFlavor COLUMN_FLAVOR
fld public static java.awt.datatransfer.DataFlavor CONNECTION_FLAVOR
fld public static java.awt.datatransfer.DataFlavor TABLE_FLAVOR
fld public static java.awt.datatransfer.DataFlavor VIEW_FLAVOR
innr public final static Column
innr public final static Connection
innr public final static Table
innr public final static View
supr java.lang.Object

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Column
 outer org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
meth public java.lang.String getColumnName()
meth public java.lang.String getTableName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds columnName,dbconn,jdbcDriver,tableName

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Connection
 outer org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table
 outer org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
meth public java.lang.String getTableName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver,tableName

CLSS public final static org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$View
 outer org.netbeans.api.db.explorer.DatabaseMetaDataTransfer
meth public java.lang.String getViewName()
meth public java.lang.String toString()
meth public org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public org.netbeans.api.db.explorer.JDBCDriver getJDBCDriver()
supr java.lang.Object
hfds dbconn,jdbcDriver,viewName

CLSS public final org.netbeans.api.db.explorer.JDBCDriver
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.net.URL[] getURLs()
meth public java.sql.Driver getDriver() throws org.netbeans.api.db.explorer.DatabaseException
meth public static org.netbeans.api.db.explorer.JDBCDriver create(java.lang.String,java.lang.String,java.lang.String,java.net.URL[])
supr java.lang.Object
hfds clazz,displayName,name,urls

CLSS public abstract interface org.netbeans.api.db.explorer.JDBCDriverListener
meth public abstract void driversChanged()

CLSS public final org.netbeans.api.db.explorer.JDBCDriverManager
meth public org.netbeans.api.db.explorer.JDBCDriver showAddDriverDialogFromEventThread()
meth public org.netbeans.api.db.explorer.JDBCDriver[] getDrivers()
meth public org.netbeans.api.db.explorer.JDBCDriver[] getDrivers(java.lang.String)
meth public static org.netbeans.api.db.explorer.JDBCDriverManager getDefault()
meth public void addDriver(org.netbeans.api.db.explorer.JDBCDriver) throws org.netbeans.api.db.explorer.DatabaseException
meth public void addDriverListener(org.netbeans.api.db.explorer.JDBCDriverListener)
meth public void removeDriver(org.netbeans.api.db.explorer.JDBCDriver) throws org.netbeans.api.db.explorer.DatabaseException
meth public void removeDriverListener(org.netbeans.api.db.explorer.JDBCDriverListener)
meth public void showAddDriverDialog()
supr java.lang.Object
hfds DEFAULT,listeners,result

CLSS public abstract org.netbeans.api.db.explorer.node.BaseNode
cons public init(org.netbeans.api.db.explorer.node.ChildNodeFactory,org.netbeans.modules.db.explorer.node.NodeDataLookup,java.lang.String,org.netbeans.api.db.explorer.node.NodeProvider)
cons public init(org.netbeans.modules.db.explorer.node.NodeDataLookup,java.lang.String,org.netbeans.api.db.explorer.node.NodeProvider)
fld protected final static java.lang.String CATALOG = "Catalog"
fld protected final static java.lang.String CATALOGDESC = "CatalogDescription"
fld protected final static java.lang.String COLUMNSIZE = "ColumnSize"
fld protected final static java.lang.String COLUMNSIZEDESC = "ColumnSizeDescription"
fld protected final static java.lang.String DATABASEURL = "DatabaseURL"
fld protected final static java.lang.String DATABASEURLDESC = "DatabaseURLDescription"
fld protected final static java.lang.String DATATYPE = "Datatype"
fld protected final static java.lang.String DATATYPEDESC = "DatatypeDescription"
fld protected final static java.lang.String DIGITS = "DecimalDigits"
fld protected final static java.lang.String DIGITSDESC = "DecimalDigitsDescription"
fld protected final static java.lang.String DISPLAYNAME = "DisplayName"
fld protected final static java.lang.String DISPLAYNAMEDESC = "DisplayNameDescription"
fld protected final static java.lang.String DRIVER = "DriverURL"
fld protected final static java.lang.String DRIVERDESC = "DriverURLDescription"
fld protected final static java.lang.String FKPOSITION = "PositionInFK"
fld protected final static java.lang.String FKPOSITIONDESC = "PositionInFKDescription"
fld protected final static java.lang.String FKREFERREDCOLUMN = "ReferredFKColumn"
fld protected final static java.lang.String FKREFERREDCOLUMNDESC = "ReferredFKColumn"
fld protected final static java.lang.String FKREFERREDSCHEMA = "ReferredFKSchema"
fld protected final static java.lang.String FKREFERREDSCHEMADESC = "ReferredFKSchema"
fld protected final static java.lang.String FKREFERREDTABLE = "ReferredFKTable"
fld protected final static java.lang.String FKREFERREDTABLEDESC = "ReferredFKTable"
fld protected final static java.lang.String FKREFERRINGCOLUMN = "ReferringFKColumn"
fld protected final static java.lang.String FKREFERRINGCOLUMNDESC = "ReferringFKColumn"
fld protected final static java.lang.String FKREFERRINGSCHEMA = "ReferringFKSchema"
fld protected final static java.lang.String FKREFERRINGSCHEMADESC = "ReferringFKSchema"
fld protected final static java.lang.String FKREFERRINGTABLE = "ReferringFKTable"
fld protected final static java.lang.String FKREFERRINGTABLEDESC = "ReferringFKTable"
fld protected final static java.lang.String INDEXPART = "IndexPart"
fld protected final static java.lang.String INDEXPARTDESC = "IndexPartDescription"
fld protected final static java.lang.String NULL = "Null"
fld protected final static java.lang.String NULLDESC = "NullDescription"
fld protected final static java.lang.String PKPART = "PKPart"
fld protected final static java.lang.String PKPARTDESC = "PKPartDescription"
fld protected final static java.lang.String POSITION = "Position"
fld protected final static java.lang.String POSITIONDESC = "PositionDescription"
fld protected final static java.lang.String PROP_DEFCATALOG = "DefaultCatalog"
fld protected final static java.lang.String PROP_DEFCATALOGDESC = "DefaultCatalog"
fld protected final static java.lang.String PROP_DEFSCHEMA = "DefaultSchema"
fld protected final static java.lang.String PROP_DEFSCHEMADESC = "DefaultSchema"
fld protected final static java.lang.String REMEMBERPW = "RememberPassword"
fld protected final static java.lang.String REMEMBERPWDESC = "RememberPasswordDescription"
fld protected final static java.lang.String SCHEMA = "Schema"
fld protected final static java.lang.String SCHEMADESC = "SchemaDescription"
fld protected final static java.lang.String TYPE = "Type"
fld protected final static java.lang.String TYPEDESC = "TypeDescription"
fld protected final static java.lang.String UNIQUE = "UniqueNoMnemonic"
fld protected final static java.lang.String UNIQUEDESC = "UniqueDescription"
fld protected final static java.lang.String USER = "User"
fld protected final static java.lang.String USERDESC = "UserDescription"
fld public boolean isRemoved
meth protected abstract void initialize()
meth protected org.openide.nodes.Sheet createSheet()
meth protected void addProperty(java.lang.String,java.lang.String,java.lang.Class,boolean,java.lang.Object)
meth protected void addProperty(org.openide.nodes.Node$Property)
meth protected void clearProperties()
meth protected void remove()
meth protected void remove(boolean)
meth protected void setup()
meth protected void updateProperties()
meth public abstract java.lang.String getIconBase()
meth public abstract java.lang.String getName()
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.lang.Object getPropertyValue(java.lang.String)
meth public java.util.Collection<? extends org.openide.nodes.Node> getChildNodes()
meth public java.util.Collection<? extends org.openide.nodes.Node> getChildNodesSync()
meth public java.util.Collection<org.openide.nodes.Node$Property> getProperties()
meth public javax.swing.Action[] getActions(boolean)
meth public org.netbeans.modules.db.explorer.node.NodeRegistry getNodeRegistry()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public void destroy()
meth public void refresh()
meth public void setPropertyValue(org.openide.nodes.Node$Property,java.lang.Object)
meth public void update()
supr org.openide.nodes.AbstractNode
hfds childNodeFactory,dataLookup,firePropChangeAfterRefresh,layerEntry,nodeProvider,nodeRegistry,propMap,refreshing

CLSS public org.netbeans.api.db.explorer.node.ChildNodeFactory
cons public init(org.openide.util.Lookup)
meth protected boolean createKeys(java.util.List<org.openide.util.Lookup>)
meth public org.openide.nodes.Node[] createNodesForKey(org.openide.util.Lookup)
meth public void refresh()
meth public void refreshSync()
supr org.openide.nodes.ChildFactory<org.openide.util.Lookup>
hfds dataLookup

CLSS public abstract org.netbeans.api.db.explorer.node.NodeProvider
cons public init(org.openide.util.Lookup)
cons public init(org.openide.util.Lookup,java.util.Comparator<org.openide.nodes.Node>)
fld protected boolean initialized
intf org.openide.util.Lookup$Provider
meth protected abstract void initialize()
meth protected java.util.Collection<org.openide.nodes.Node> getNodes(java.lang.Object)
meth public java.util.Collection<org.openide.nodes.Node> getNodes()
meth public org.openide.util.Lookup getLookup()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addNode(org.openide.nodes.Node)
meth public void refresh()
meth public void removeAllNodes()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeNode(org.openide.nodes.Node)
meth public void setNodes(java.util.Collection<org.openide.nodes.Node>)
meth public void setProxyNodes(java.util.Collection<org.openide.nodes.Node>)
supr java.lang.Object
hfds changeSupport,isProxied,lookup,nodeSet

CLSS public abstract interface org.netbeans.api.db.explorer.node.NodeProviderFactory
meth public abstract org.netbeans.api.db.explorer.node.NodeProvider createInstance(org.openide.util.Lookup)

CLSS public final org.netbeans.api.db.explorer.support.DatabaseExplorerUIs
meth public static org.openide.nodes.Node connectionsNode()
meth public static void connect(javax.swing.JComboBox,org.netbeans.api.db.explorer.ConnectionManager)
supr java.lang.Object
hcls ConnChildren,ConnectionComboBoxModel,ConnectionComparator,ConnectionDataComboBoxModel

CLSS public final org.netbeans.api.db.sql.support.SQLIdentifiers
innr public abstract static Quoter
meth public static org.netbeans.api.db.sql.support.SQLIdentifiers$Quoter createQuoter(java.sql.DatabaseMetaData)
supr java.lang.Object
hcls DatabaseMetaDataQuoter,FallbackQuoter

CLSS public abstract static org.netbeans.api.db.sql.support.SQLIdentifiers$Quoter
 outer org.netbeans.api.db.sql.support.SQLIdentifiers
meth public abstract java.lang.String quoteAlways(java.lang.String)
meth public abstract java.lang.String quoteIfNeeded(java.lang.String)
meth public java.lang.String getQuoteString()
meth public java.lang.String unquote(java.lang.String)
supr java.lang.Object
hfds quoteString

CLSS public abstract interface org.netbeans.spi.db.explorer.DatabaseRuntime
meth public abstract boolean acceptsDatabaseURL(java.lang.String)
meth public abstract boolean canStart()
meth public abstract boolean isRunning()
meth public abstract java.lang.String getJDBCDriverClass()
meth public abstract void start()
meth public abstract void stop()

CLSS public org.openide.nodes.AbstractNode
cons public init(org.openide.nodes.Children)
cons public init(org.openide.nodes.Children,org.openide.util.Lookup)
fld protected java.text.MessageFormat displayFormat
fld protected org.openide.util.actions.SystemAction[] systemActions
 anno 0 java.lang.Deprecated()
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final org.openide.nodes.Sheet getSheet()
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected final void setSheet(org.openide.nodes.Sheet)
meth protected org.openide.nodes.Sheet createSheet()
meth protected org.openide.util.actions.SystemAction[] createActions()
 anno 0 java.lang.Deprecated()
meth protected void createPasteTypes(java.awt.datatransfer.Transferable,java.util.List<org.openide.util.datatransfer.PasteType>)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean canCopy()
meth public boolean canCut()
meth public boolean canDestroy()
meth public boolean canRename()
meth public boolean hasCustomizer()
meth public final org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public final void setIconBaseWithExtension(java.lang.String)
meth public java.awt.Component getCustomizer()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public javax.swing.Action getPreferredAction()
meth public org.openide.nodes.Node cloneNode()
meth public org.openide.nodes.Node$Handle getHandle()
meth public org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.datatransfer.NewType[] getNewTypes()
meth public org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public void setDefaultAction(org.openide.util.actions.SystemAction)
 anno 0 java.lang.Deprecated()
meth public void setIconBase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
supr org.openide.nodes.Node
hfds DEFAULT_ICON,DEFAULT_ICON_BASE,DEFAULT_ICON_EXTENSION,ICON_BASE,NO_NEW_TYPES,NO_PASTE_TYPES,OPENED_ICON_BASE,iconBase,iconExtension,icons,lookup,overridesGetDefaultAction,preferredAction,sheet,sheetCookieL
hcls SheetAndCookieListener

CLSS public abstract org.openide.nodes.ChildFactory<%0 extends java.lang.Object>
cons public init()
innr public abstract static Detachable
meth protected abstract boolean createKeys(java.util.List<{org.openide.nodes.ChildFactory%0}>)
meth protected final void refresh(boolean)
meth protected org.openide.nodes.Node createNodeForKey({org.openide.nodes.ChildFactory%0})
meth protected org.openide.nodes.Node createWaitNode()
meth protected org.openide.nodes.Node[] createNodesForKey({org.openide.nodes.ChildFactory%0})
supr java.lang.Object
hfds observer
hcls Observer,WaitFilterNode

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

