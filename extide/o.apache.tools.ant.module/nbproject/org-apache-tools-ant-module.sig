#Signature file v4.1
#Version 3.107.0

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

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
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

CLSS public abstract interface org.apache.tools.ant.module.api.AntProjectCookie
innr public abstract interface static ParseStatus
intf org.openide.nodes.Node$Cookie
meth public abstract java.io.File getFile()
meth public abstract java.lang.Throwable getParseException()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract org.w3c.dom.Document getDocument()
meth public abstract org.w3c.dom.Element getProjectElement()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface static org.apache.tools.ant.module.api.AntProjectCookie$ParseStatus
 outer org.apache.tools.ant.module.api.AntProjectCookie
intf org.apache.tools.ant.module.api.AntProjectCookie
meth public abstract boolean isParsed()

CLSS public final org.apache.tools.ant.module.api.AntTargetExecutor
innr public final static Env
meth public org.openide.execution.ExecutorTask execute(org.apache.tools.ant.module.api.AntProjectCookie,java.lang.String[]) throws java.io.IOException
meth public static org.apache.tools.ant.module.api.AntTargetExecutor createTargetExecutor(org.apache.tools.ant.module.api.AntTargetExecutor$Env)
supr java.lang.Object
hfds env

CLSS public final static org.apache.tools.ant.module.api.AntTargetExecutor$Env
 outer org.apache.tools.ant.module.api.AntTargetExecutor
cons public init()
meth public int getVerbosity()
meth public java.io.OutputStream getLogger()
meth public java.util.Properties getProperties()
meth public java.util.Set<java.lang.String> getConcealedProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void setConcealedProperties(java.util.Set<? extends java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setLogger(java.io.OutputStream)
 anno 0 java.lang.Deprecated()
meth public void setPreferredName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setProperties(java.util.Properties)
meth public void setSaveAllDocuments(boolean)
meth public void setTabReplaceStrategy(java.util.function.Predicate<java.lang.String>,java.util.function.Predicate<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void setUserAction(boolean)
meth public void setVerbosity(int)
supr java.lang.Object
hfds canBeReplaced,canReplace,concealedProperties,outputStream,preferredName,properties,shouldSaveAllDocs,userAction,verbosity

CLSS public abstract interface org.apache.tools.ant.module.api.ElementCookie
 anno 0 java.lang.Deprecated()
intf org.openide.nodes.Node$Cookie
meth public abstract org.w3c.dom.Element getElement()

CLSS public final org.apache.tools.ant.module.api.IntrospectedInfo
cons public init()
meth public boolean isKnown(java.lang.String)
meth public boolean supportsText(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String[] getTags(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getAttributes(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getDefs(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getElements(java.lang.String)
meth public static org.apache.tools.ant.module.api.IntrospectedInfo getDefaults()
meth public static org.apache.tools.ant.module.api.IntrospectedInfo getKnownInfo()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void register(java.lang.String,java.lang.Class,java.lang.String)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void scanProject(java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Class>>)
meth public void unregister(java.lang.String,java.lang.String)
supr java.lang.Object
hfds LOG,RP,antBridgeListener,clazzes,cs,defaults,defaultsEverInited,defaultsInited,holder,merged,namedefs
hcls ChangeTask,IntrospectedClass

CLSS public abstract interface org.apache.tools.ant.module.api.IntrospectionCookie
 anno 0 java.lang.Deprecated()
intf org.openide.nodes.Node$Cookie
meth public abstract java.lang.String getClassName()

CLSS public final org.apache.tools.ant.module.api.support.ActionUtils
meth public static java.lang.String antIncludesList(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject)
meth public static java.lang.String antIncludesList(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,boolean)
meth public static org.openide.execution.ExecutorTask runTarget(org.openide.filesystems.FileObject,java.lang.String[],java.util.Properties) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.execution.ExecutorTask runTarget(org.openide.filesystems.FileObject,java.lang.String[],java.util.Properties,java.util.Set<java.lang.String>) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.openide.filesystems.FileObject[] findSelectedFiles(org.openide.util.Lookup,org.openide.filesystems.FileObject,java.lang.String,boolean)
meth public static org.openide.filesystems.FileObject[] regexpMapFiles(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,java.util.regex.Pattern,org.openide.filesystems.FileObject,java.lang.String,boolean)
supr java.lang.Object

CLSS public org.apache.tools.ant.module.api.support.AntScriptUtils
meth public static java.lang.String getAntScriptName(org.openide.filesystems.FileObject)
meth public static java.util.List<java.lang.String> getCallableTargetNames(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.apache.tools.ant.module.api.AntProjectCookie antProjectCookieFor(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds antProjectCookies

CLSS public org.apache.tools.ant.module.api.support.TargetLister
innr public final static Target
meth public static java.util.Set<org.apache.tools.ant.module.api.support.TargetLister$Target> getTargets(org.apache.tools.ant.module.api.AntProjectCookie) throws java.io.IOException
supr java.lang.Object
hcls Script

CLSS public final static org.apache.tools.ant.module.api.support.TargetLister$Target
 outer org.apache.tools.ant.module.api.support.TargetLister
meth public boolean isDefault()
meth public boolean isDescribed()
meth public boolean isInternal()
meth public boolean isOverridden()
meth public java.lang.String getName()
meth public java.lang.String getQualifiedName()
meth public java.lang.String toString()
meth public org.apache.tools.ant.module.api.AntProjectCookie getOriginatingScript()
meth public org.apache.tools.ant.module.api.AntProjectCookie getScript()
meth public org.w3c.dom.Element getElement()
supr java.lang.Object
hfds el,name,script

CLSS public final org.apache.tools.ant.module.spi.AntEvent
fld public final static int LOG_DEBUG = 4
fld public final static int LOG_ERR = 0
fld public final static int LOG_INFO = 2
fld public final static int LOG_VERBOSE = 3
fld public final static int LOG_WARN = 1
meth public boolean isConsumed()
meth public int getLine()
meth public int getLogLevel()
meth public java.io.File getScriptLocation()
meth public java.lang.String evaluate(java.lang.String)
meth public java.lang.String getMessage()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getTargetName()
meth public java.lang.String getTaskName()
meth public java.lang.String toString()
meth public java.lang.Throwable getException()
meth public java.util.Set<java.lang.String> getPropertyNames()
meth public org.apache.tools.ant.module.spi.AntSession getSession()
meth public org.apache.tools.ant.module.spi.TaskStructure getTaskStructure()
meth public void consume()
supr java.lang.Object
hfds impl

CLSS public abstract org.apache.tools.ant.module.spi.AntLogger
cons protected init()
fld public final static java.lang.String[] ALL_TARGETS
fld public final static java.lang.String[] ALL_TASKS
fld public final static java.lang.String[] NO_TARGETS
fld public final static java.lang.String[] NO_TASKS
meth public boolean interestedInAllScripts(org.apache.tools.ant.module.spi.AntSession)
meth public boolean interestedInScript(java.io.File,org.apache.tools.ant.module.spi.AntSession)
meth public boolean interestedInSession(org.apache.tools.ant.module.spi.AntSession)
meth public int[] interestedInLogLevels(org.apache.tools.ant.module.spi.AntSession)
meth public java.lang.String[] interestedInTargets(org.apache.tools.ant.module.spi.AntSession)
meth public java.lang.String[] interestedInTasks(org.apache.tools.ant.module.spi.AntSession)
meth public void buildFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void buildInitializationFailed(org.apache.tools.ant.module.spi.AntEvent)
meth public void buildStarted(org.apache.tools.ant.module.spi.AntEvent)
meth public void messageLogged(org.apache.tools.ant.module.spi.AntEvent)
meth public void targetFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void targetStarted(org.apache.tools.ant.module.spi.AntEvent)
meth public void taskFinished(org.apache.tools.ant.module.spi.AntEvent)
meth public void taskStarted(org.apache.tools.ant.module.spi.AntEvent)
supr java.lang.Object

CLSS public abstract org.apache.tools.ant.module.spi.AntOutputStream
 anno 0 java.lang.Deprecated()
cons public init()
meth protected abstract void writeLine(java.lang.String) throws java.io.IOException
meth protected boolean writeLine(java.lang.String,java.net.URL,int,int,int,int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String formatMessage(java.lang.String,java.lang.String,int,int,int,int)
 anno 0 java.lang.Deprecated()
meth protected void handleClose() throws java.io.IOException
meth protected void writeLine(java.lang.String,org.openide.filesystems.FileObject,int,int,int,int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final void close() throws java.io.IOException
meth public final void flush() throws java.io.IOException
meth public final void write(byte[]) throws java.io.IOException
meth public final void write(byte[],int,int) throws java.io.IOException
meth public final void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds buffer,hadFirst

CLSS public final org.apache.tools.ant.module.spi.AntSession
meth public boolean isConcealed(java.lang.String)
meth public boolean isExceptionConsumed(java.lang.Throwable)
meth public int getVerbosity()
meth public java.io.File getOriginatingScript()
meth public java.lang.Object getCustomData(org.apache.tools.ant.module.spi.AntLogger)
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public java.lang.String[] getOriginatingTargets()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public org.openide.windows.InputOutput getIO()
meth public org.openide.windows.OutputListener createStandardHyperlink(java.net.URL,java.lang.String,int,int,int,int)
meth public void consumeException(java.lang.Throwable)
meth public void deliverMessageLogged(org.apache.tools.ant.module.spi.AntEvent,java.lang.String,int)
meth public void println(java.lang.String,boolean,org.openide.windows.OutputListener)
meth public void putCustomData(org.apache.tools.ant.module.spi.AntLogger,java.lang.Object)
supr java.lang.Object
hfds impl

CLSS public abstract interface org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider
meth public abstract java.io.File[] getClasspathItems()

CLSS public final org.apache.tools.ant.module.spi.TaskStructure
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getAttributeNames()
meth public org.apache.tools.ant.module.spi.TaskStructure[] getChildren()
supr java.lang.Object
hfds impl

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

