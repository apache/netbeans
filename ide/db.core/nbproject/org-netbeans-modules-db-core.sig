#Signature file v4.1
#Version 1.60

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

CLSS public org.netbeans.modules.db.api.sql.execute.LogFileLogger
cons public init()
intf org.netbeans.modules.db.api.sql.execute.SQLExecuteLogger
meth public void cancel()
meth public void finish(long)
meth public void log(org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo)
supr java.lang.Object
hfds LOGGER,errorCount

CLSS public abstract interface org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public abstract void execute()
meth public abstract void setDatabaseConnection(org.netbeans.api.db.explorer.DatabaseConnection)

CLSS public abstract interface org.netbeans.modules.db.api.sql.execute.SQLExecuteLogger
meth public abstract void cancel()
meth public abstract void finish(long)
meth public abstract void log(org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo)

CLSS public org.netbeans.modules.db.api.sql.execute.SQLExecuteOptions
cons public init()
fld public final static java.lang.String PROP_KEEP_OLD_TABS = "keepOldResultTabs"
meth public boolean isKeepOldResultTabs()
meth public static org.netbeans.modules.db.api.sql.execute.SQLExecuteOptions getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setKeepOldResultTabs(boolean)
supr java.lang.Object
hfds DEFAULT,pcs

CLSS public abstract interface org.netbeans.modules.db.api.sql.execute.SQLExecution
fld public final static java.lang.String PROP_DATABASE_CONNECTION = "databaseConnection"
fld public final static java.lang.String PROP_EXECUTING = "executing"
meth public abstract boolean isExecuting()
meth public abstract boolean isSelection()
meth public abstract org.netbeans.api.db.explorer.DatabaseConnection getDatabaseConnection()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void execute()
meth public abstract void executeSelection()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setDatabaseConnection(org.netbeans.api.db.explorer.DatabaseConnection)
meth public abstract void showHistory()

CLSS public abstract interface org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo
meth public abstract boolean hasExceptions()
meth public abstract java.util.List<? extends java.lang.Throwable> getExceptions()
meth public abstract java.util.List<org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo> getStatementInfos()

CLSS public org.netbeans.modules.db.api.sql.execute.SQLExecutor
cons public init()
meth public static org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo execute(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String) throws org.netbeans.api.db.explorer.DatabaseException
meth public static org.netbeans.modules.db.api.sql.execute.SQLExecutionInfo execute(org.netbeans.api.db.explorer.DatabaseConnection,java.lang.String,org.netbeans.modules.db.api.sql.execute.SQLExecuteLogger) throws org.netbeans.api.db.explorer.DatabaseException
supr java.lang.Object
hfds LOGGER
hcls LoggerProxy,SQLExecutionInfoImpl,StatementExecutionInfoImpl

CLSS public final org.netbeans.modules.db.api.sql.execute.SQLScript
meth public org.netbeans.modules.db.api.sql.execute.SQLScriptStatement getStatementAtOffset(int)
meth public static org.netbeans.modules.db.api.sql.execute.SQLScript create(java.lang.String)
supr java.lang.Object
hfds statements

CLSS public final org.netbeans.modules.db.api.sql.execute.SQLScriptStatement
meth public int getEndOffset()
meth public int getStartOffset()
meth public java.lang.String getText()
supr java.lang.Object
hfds endOffset,startOffset,text

CLSS public abstract org.netbeans.modules.db.api.sql.execute.StatementExecutionInfo
cons public init()
meth public abstract boolean hasExceptions()
meth public abstract java.lang.String getSQL()
meth public abstract java.util.Collection<java.lang.Throwable> getExceptions()
meth public abstract long getExecutionTime()
meth public int getErrorPosition()
supr java.lang.Object

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

