#Signature file v4.1
#Version 1.38.0

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

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract javax.swing.AbstractAction
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,javax.swing.Icon)
fld protected boolean enabled
fld protected javax.swing.event.SwingPropertyChangeSupport changeSupport
intf java.io.Serializable
intf java.lang.Cloneable
intf javax.swing.Action
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public boolean isEnabled()
meth public java.beans.PropertyChangeListener[] getPropertyChangeListeners()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object[] getKeys()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected final java.lang.String frameInfo
meth public java.lang.String getDisplayName()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
supr org.openide.nodes.AbstractNode

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.DefaultTestRunnerNodeFactory
cons public init()
meth public org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode createTestSuiteNode(java.lang.String,boolean)
meth public org.openide.nodes.Node createCallstackFrameNode(java.lang.String,java.lang.String)
meth public org.openide.nodes.Node createTestMethodNode(org.netbeans.modules.gsf.testrunner.api.Testcase,org.netbeans.api.project.Project)
supr org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory

CLSS public final org.netbeans.modules.gsf.testrunner.ui.api.DiffViewAction
cons public init(org.netbeans.modules.gsf.testrunner.api.Testcase)
cons public init(org.netbeans.modules.gsf.testrunner.api.Trouble$ComparisonFailure)
meth public java.lang.Object getValue(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds comparisonFailure
hcls StringComparisonSource

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.ui.api.Locator
meth public abstract void jumpToSource(org.openide.nodes.Node)

CLSS public final org.netbeans.modules.gsf.testrunner.ui.api.Manager
fld public final static java.lang.String JUNIT_TF = "junit"
fld public final static java.lang.String TESTNG_TF = "testng"
meth public java.lang.String getTestingFramework()
meth public org.netbeans.modules.gsf.testrunner.ui.api.OutputLineHandler getOutputLineHandler()
meth public org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory getNodeFactory()
meth public static org.netbeans.modules.gsf.testrunner.ui.api.Manager getInstance()
meth public void displayOutput(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String,boolean)
meth public void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report)
meth public void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report,boolean)
meth public void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String)
meth public void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public void emptyTestRun(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public void sessionFinished(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public void setNodeFactory(org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory)
meth public void setOutputLineHandler(org.netbeans.modules.gsf.testrunner.ui.api.OutputLineHandler)
meth public void setTestingFramework(java.lang.String)
meth public void testStarted(org.netbeans.modules.gsf.testrunner.api.TestSession)
supr java.lang.Object
hfds DEFAULT_LINE_HANDLER,LOGGER,RP,bubbleNotification,bubbleTask,displayHandlers,displaysMap,instanceRef,lateWindowPromotion,lineHandler,lock,nodeFactory,testSessions
hcls Displayer

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.ui.api.OutputLineHandler
meth public abstract void handleLine(org.openide.windows.OutputWriter,java.lang.String)

CLSS public final org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer
innr public static TestCreatorPanelDisplayerProjectServiceProvider
meth public static org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer getDefault()
meth public void displayPanel(org.openide.filesystems.FileObject[],java.lang.Object,java.lang.String)
supr java.lang.Object
hfds INSTANCE,RP

CLSS public static org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer$TestCreatorPanelDisplayerProjectServiceProvider
 outer org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer
cons public init(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController
cons public init()
innr public final static TestMethod
meth public static void setTestMethods(javax.swing.text.Document,java.util.List<org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod>)
supr java.lang.Object

CLSS public final static org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod
 outer org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController
cons public init(java.lang.String,javax.swing.text.Position,org.netbeans.spi.project.SingleMethod,javax.swing.text.Position,javax.swing.text.Position,javax.swing.text.Position)
cons public init(java.lang.String,org.netbeans.spi.project.SingleMethod,javax.swing.text.Position,javax.swing.text.Position)
cons public init(java.lang.String,org.netbeans.spi.project.SingleMethod,javax.swing.text.Position,javax.swing.text.Position,javax.swing.text.Position)
cons public init(org.netbeans.spi.project.SingleMethod,javax.swing.text.Position,javax.swing.text.Position)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getTestClassName()
meth public javax.swing.text.Position end()
meth public javax.swing.text.Position getTestClassPosition()
meth public javax.swing.text.Position preferred()
meth public javax.swing.text.Position start()
meth public org.netbeans.spi.project.SingleMethod method()
supr java.lang.Object
hfds end,method,preferred,start,testClassName,testClassPosition

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestMethodDebuggerProvider
cons public init()
meth public abstract boolean canHandle(org.openide.nodes.Node)
meth public abstract org.netbeans.spi.project.SingleMethod getTestMethod(javax.swing.text.Document,int)
meth public boolean isTestClass(org.openide.nodes.Node)
meth public final void debugTestMethod(org.openide.nodes.Node)
supr java.lang.Object
hfds command,singleMethod,singleMethodTask

CLSS public final org.netbeans.modules.gsf.testrunner.ui.api.TestMethodFinder
cons public init()
meth public static java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod>> findTestMethods(java.lang.Iterable<org.openide.filesystems.FileObject>,java.util.function.BiConsumer<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod>>)
supr java.lang.Object

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode
cons protected init(org.netbeans.modules.gsf.testrunner.api.Testcase,org.netbeans.api.project.Project,org.openide.util.Lookup)
cons public init(org.netbeans.modules.gsf.testrunner.api.Testcase,org.netbeans.api.project.Project)
fld protected final org.netbeans.modules.gsf.testrunner.api.Testcase testcase
meth protected org.netbeans.api.project.Project getProject()
meth public boolean failed()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getHtmlDisplayName()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.netbeans.modules.gsf.testrunner.api.Testcase getTestcase()
supr org.openide.nodes.AbstractNode
hfds INLINE_RESULTS,project,projectURI
hcls DisplayNameMapper

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestMethodRunnerProvider
cons public init()
meth public abstract boolean canHandle(org.openide.nodes.Node)
meth public abstract org.netbeans.spi.project.SingleMethod getTestMethod(javax.swing.text.Document,int)
meth public boolean isTestClass(org.openide.nodes.Node)
meth public final void runTestMethod(org.openide.nodes.Node)
supr java.lang.Object
hfds command,singleMethod,singleMethodTask

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestNodeAction
cons public init()
meth protected abstract void doActionPerformed(java.awt.event.ActionEvent)
meth public final void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler
innr public abstract interface static Spi
meth public abstract int getTotalTests()
meth public abstract void displayMessage(java.lang.String)
meth public abstract void displayMessageSessionFinished(java.lang.String)
meth public abstract void displayOutput(java.lang.String,boolean)
meth public abstract void displayReport(org.netbeans.modules.gsf.testrunner.api.Report)
meth public abstract void displaySuiteRunning(java.lang.String)
meth public abstract void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public final static org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler create(org.netbeans.modules.gsf.testrunner.api.TestSession)
supr java.lang.Object
hcls Impl

CLSS public abstract interface static org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi<%0 extends java.lang.Object>
 outer org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler
meth public abstract int getTotalTests({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0})
meth public abstract void displayMessage({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},java.lang.String)
meth public abstract void displayMessageSessionFinished({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},java.lang.String)
meth public abstract void displayOutput({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},java.lang.String,boolean)
meth public abstract void displayReport({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},org.netbeans.modules.gsf.testrunner.api.Report)
meth public abstract void displaySuiteRunning({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},java.lang.String)
meth public abstract void displaySuiteRunning({org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0},org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public abstract {org.netbeans.modules.gsf.testrunner.ui.api.TestResultDisplayHandler$Spi%0} create(org.netbeans.modules.gsf.testrunner.api.TestSession)

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory
cons public init()
meth public abstract org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode createTestSuiteNode(java.lang.String,boolean)
meth public abstract org.openide.nodes.Node createCallstackFrameNode(java.lang.String,java.lang.String)
meth public abstract org.openide.nodes.Node createTestMethodNode(org.netbeans.modules.gsf.testrunner.api.Testcase,org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode
cons protected init(org.netbeans.modules.gsf.testrunner.api.Report,java.lang.String,boolean,org.openide.util.Lookup)
cons public init(java.lang.String,boolean)
cons public init(org.netbeans.modules.gsf.testrunner.api.Report,boolean)
fld protected int filterMask
fld protected java.lang.String suiteName
fld protected org.netbeans.modules.gsf.testrunner.api.Report report
fld protected org.netbeans.modules.gsf.testrunner.api.TestSuite suite
meth public java.awt.Image getIcon(int)
meth public java.awt.Image getOpenedIcon(int)
meth public java.lang.String getHtmlDisplayName()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.netbeans.modules.gsf.testrunner.api.Report getReport()
meth public org.netbeans.modules.gsf.testrunner.api.TestSuite getSuite()
meth public static java.lang.String cutLine(java.lang.String,int,boolean)
meth public void displayReport(org.netbeans.modules.gsf.testrunner.api.Report)
meth public void notifyTestSuiteFinished()
meth public void setFilterMask(int)
meth public void setSuite(org.netbeans.modules.gsf.testrunner.api.TestSuite)
supr org.openide.nodes.AbstractNode
hfds DISPLAY_TOOLTIPS,MAX_MSG_LINE_LENGTH,MAX_TOOLTIP_LINES,MAX_TOOLTIP_LINE_LENGTH,children

CLSS public org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils
cons public init()
meth public static org.openide.filesystems.FileObject getFileObjectFromNode(org.openide.nodes.Node)
meth public static org.openide.filesystems.FileObject[] getFileObjectsFromNodes(org.openide.nodes.Node[])
meth public static void notifyUser(java.lang.String)
meth public static void notifyUser(java.lang.String,int)
supr java.lang.Object
hfds LOG

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.ui.spi.ComputeTestMethods
meth public abstract java.util.List<org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod> computeTestMethods(org.netbeans.modules.parsing.spi.Parser$Result,java.util.concurrent.atomic.AtomicBoolean)

CLSS public abstract org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration
cons public init()
innr public abstract interface static Callback
innr public final static Context
meth public abstract boolean canHandleProject(java.lang.String)
meth public abstract java.lang.Object[] getTestSourceRoots(java.util.Collection<org.netbeans.api.project.SourceGroup>,org.openide.filesystems.FileObject)
meth public abstract org.openide.util.Pair<java.lang.String,java.lang.String> getSourceAndTestClassNames(org.openide.filesystems.FileObject,boolean,boolean)
meth public abstract void persistConfigurationPanel(org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Context)
meth public boolean isValid()
meth public boolean showClassNameInfo()
meth public boolean showClassToTestInfo()
meth public java.awt.Component getConfigurationPanel(org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Context)
meth public java.lang.String getErrorMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.swing.text.JTextComponent getMessagePanel(org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Context)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Callback
 outer org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration
meth public abstract void checkAcceptability()

CLSS public final static org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Context
 outer org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration
cons public init(boolean,org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Callback)
meth public boolean isMultipleClasses()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration$Callback getCallback()
supr java.lang.Object
hfds callback,multipleClasses,properties

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfigurationProvider
meth public abstract org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration createTestCreatorConfiguration(org.openide.filesystems.FileObject[])

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

