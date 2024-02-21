#Signature file v4.1
#Version 3.53

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

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface javax.swing.event.ChangeListener
intf java.util.EventListener
meth public abstract void stateChanged(javax.swing.event.ChangeEvent)

CLSS public junit.framework.Assert
 anno 0 java.lang.Deprecated()
cons protected init()
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(char,char)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(int,int)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,boolean,boolean)
meth public static void assertEquals(java.lang.String,byte,byte)
meth public static void assertEquals(java.lang.String,char,char)
meth public static void assertEquals(java.lang.String,double,double,double)
meth public static void assertEquals(java.lang.String,float,float,float)
meth public static void assertEquals(java.lang.String,int,int)
meth public static void assertEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,long,long)
meth public static void assertEquals(java.lang.String,short,short)
meth public static void assertEquals(long,long)
meth public static void assertEquals(short,short)
meth public static void assertFalse(boolean)
meth public static void assertFalse(java.lang.String,boolean)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.String,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.String,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertTrue(boolean)
meth public static void assertTrue(java.lang.String,boolean)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void failNotEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failSame(java.lang.String)
supr java.lang.Object

CLSS public abstract interface junit.framework.Test
meth public abstract int countTestCases()
meth public abstract void run(junit.framework.TestResult)

CLSS public abstract junit.framework.TestCase
cons public init()
cons public init(java.lang.String)
intf junit.framework.Test
meth protected junit.framework.TestResult createResult()
meth protected void runTest() throws java.lang.Throwable
meth protected void setUp() throws java.lang.Exception
meth protected void tearDown() throws java.lang.Exception
meth public int countTestCases()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public junit.framework.TestResult run()
meth public static java.lang.String format(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(char,char)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(int,int)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,boolean,boolean)
meth public static void assertEquals(java.lang.String,byte,byte)
meth public static void assertEquals(java.lang.String,char,char)
meth public static void assertEquals(java.lang.String,double,double,double)
meth public static void assertEquals(java.lang.String,float,float,float)
meth public static void assertEquals(java.lang.String,int,int)
meth public static void assertEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,long,long)
meth public static void assertEquals(java.lang.String,short,short)
meth public static void assertEquals(long,long)
meth public static void assertEquals(short,short)
meth public static void assertFalse(boolean)
meth public static void assertFalse(java.lang.String,boolean)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.String,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.String,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void assertTrue(boolean)
meth public static void assertTrue(java.lang.String,boolean)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void failNotEquals(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failNotSame(java.lang.String,java.lang.Object,java.lang.Object)
meth public static void failSame(java.lang.String)
meth public void run(junit.framework.TestResult)
meth public void runBare() throws java.lang.Throwable
meth public void setName(java.lang.String)
supr junit.framework.Assert
hfds fName

CLSS public org.netbeans.jellytools.BuildAndRunActionsStepOperator
cons public init()
meth public java.lang.String getSelectedBuild()
meth public java.lang.String getSelectedClean()
meth public java.lang.String getSelectedGenerateJavadoc()
meth public java.lang.String getSelectedRun()
meth public java.lang.String getSelectedTest()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboBuild()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboClean()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboGenerateJavadoc()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboRun()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboTest()
meth public org.netbeans.jemmy.operators.JLabelOperator lblBuild()
meth public org.netbeans.jemmy.operators.JLabelOperator lblClean()
meth public org.netbeans.jemmy.operators.JLabelOperator lblGenerateJavadoc()
meth public org.netbeans.jemmy.operators.JLabelOperator lblOnlineError()
meth public org.netbeans.jemmy.operators.JLabelOperator lblRun()
meth public org.netbeans.jemmy.operators.JLabelOperator lblTest()
meth public void selectBuild(java.lang.String)
meth public void selectClean(java.lang.String)
meth public void selectGenerateJavadoc(java.lang.String)
meth public void selectRun(java.lang.String)
meth public void selectTest(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NewProjectWizardOperator
hfds _cboBuild,_cboClean,_cboGenerateJavadoc,_cboRun,_cboTest,_lblBuild,_lblClean,_lblGenerateJavadoc,_lblOnlineError,_lblRun,_lblTest

CLSS public org.netbeans.jellytools.Bundle
meth public static java.lang.String getString(java.lang.String,java.lang.String)
meth public static java.lang.String getString(java.lang.String,java.lang.String,java.lang.Object[])
meth public static java.lang.String getString(java.util.ResourceBundle,java.lang.String)
meth public static java.lang.String getString(java.util.ResourceBundle,java.lang.String,java.lang.Object[])
meth public static java.lang.String getStringTrimmed(java.lang.String,java.lang.String)
meth public static java.lang.String getStringTrimmed(java.lang.String,java.lang.String,java.lang.Object[])
meth public static java.util.ResourceBundle getBundle(java.lang.String)
supr java.lang.Object

CLSS public org.netbeans.jellytools.ClasspathStepOperator
cons public init()
meth public java.lang.String getOutputFolder()
meth public java.lang.String getSelectedSourcePackageFolder()
meth public org.netbeans.jemmy.operators.JButtonOperator btAddJARFolder()
meth public org.netbeans.jemmy.operators.JButtonOperator btBrowse()
meth public org.netbeans.jemmy.operators.JButtonOperator btMoveDown()
meth public org.netbeans.jemmy.operators.JButtonOperator btMoveUp()
meth public org.netbeans.jemmy.operators.JButtonOperator btRemove()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboSourcePackageFolder()
meth public org.netbeans.jemmy.operators.JLabelOperator lblClasspath()
meth public org.netbeans.jemmy.operators.JLabelOperator lblOnlineError()
meth public org.netbeans.jemmy.operators.JLabelOperator lblOutputFolderOrJAR()
meth public org.netbeans.jemmy.operators.JLabelOperator lblSourcePackageFolder()
meth public org.netbeans.jemmy.operators.JListOperator lstClasspath()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtOutputFolder()
meth public void addJARFolder()
meth public void browse()
meth public void moveDown()
meth public void moveUp()
meth public void remove()
meth public void selectClasspath(java.lang.String)
meth public void selectSourcePackageFolder(java.lang.String)
meth public void setOutputFolder(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NewProjectWizardOperator
hfds _btAddJARFolder,_btBrowse,_btMoveDown,_btMoveUp,_btRemove,_cboSourcePackageFolder,_lblClasspath,_lblOnlineError,_lblOutputFolderOrJAR,_lblSourcePackageFolder,_lstClasspath,_txtOutputFolder

CLSS public org.netbeans.jellytools.DocumentsDialogOperator
cons public init()
meth public java.lang.String getDescription()
meth public org.netbeans.jemmy.operators.JButtonOperator btClose()
meth public org.netbeans.jemmy.operators.JButtonOperator btCloseDocuments()
meth public org.netbeans.jemmy.operators.JButtonOperator btSaveDocuments()
meth public org.netbeans.jemmy.operators.JButtonOperator btSwitchToDocument()
meth public org.netbeans.jemmy.operators.JListOperator lstDocuments()
meth public org.netbeans.jemmy.operators.JTextAreaOperator txtDescription()
meth public static org.netbeans.jellytools.DocumentsDialogOperator invoke()
meth public void closeDocuments()
meth public void saveDocuments()
meth public void selectDocument(int)
meth public void selectDocument(java.lang.String)
meth public void selectDocuments(int[])
meth public void selectDocuments(java.lang.String[])
meth public void switchToDocument()
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _btActivate,_btClose,_btCloseDocuments,_btSaveDocuments,_lstDocuments,_txtDescription,title

CLSS public org.netbeans.jellytools.EditorOperator
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(javax.swing.JComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
innr public final static EditorSubchooser
meth public boolean contains(java.lang.String)
meth public boolean isCollapsed()
meth public boolean isCollapsed(int)
meth public int getLineNumber()
meth public java.lang.Object[] getAnnotations()
meth public java.lang.Object[] getAnnotations(int)
meth public java.lang.String getText()
meth public java.lang.String getText(int)
meth public org.netbeans.jemmy.operators.AbstractButtonOperator getToolbarButton(int)
meth public org.netbeans.jemmy.operators.AbstractButtonOperator getToolbarButton(java.lang.String)
meth public org.netbeans.jemmy.operators.JEditorPaneOperator txtEditorPane()
meth public org.netbeans.jemmy.operators.JLabelOperator lblInputMode()
meth public org.netbeans.jemmy.operators.JLabelOperator lblRowColumn()
meth public org.netbeans.jemmy.operators.JLabelOperator lblStatusBar()
meth public static java.lang.String getAnnotationShortDescription(java.lang.Object)
meth public static java.lang.String getAnnotationType(java.lang.Object)
meth public static void closeDiscardAll()
meth public void clickForPopup()
meth public void close(boolean)
meth public void collapseFold()
meth public void collapseFold(int)
meth public void delete(int)
meth public void delete(int,int)
meth public void delete(int,int,int)
meth public void deleteLine(int)
meth public void expandFold()
meth public void expandFold(int)
meth public void insert(java.lang.String)
meth public void insert(java.lang.String,int,int)
meth public void pushDownArrowKey()
meth public void pushEndKey()
meth public void pushHomeKey()
meth public void pushKey(int)
meth public void pushKey(int,int)
meth public void pushTabKey()
meth public void pushUpArrowKey()
meth public void replace(java.lang.String,java.lang.String)
meth public void replace(java.lang.String,java.lang.String,int)
meth public void save()
meth public void select(int)
meth public void select(int,int)
meth public void select(int,int,int)
meth public void select(java.lang.String)
meth public void select(java.lang.String,int)
meth public void setCaretPosition(int)
meth public void setCaretPosition(int,int)
meth public void setCaretPosition(java.lang.String,boolean)
meth public void setCaretPosition(java.lang.String,int,boolean)
meth public void setCaretPositionRelative(int)
meth public void setCaretPositionToEndOfLine(int)
meth public void setCaretPositionToLine(int)
meth public void typeKey(char)
meth public void typeKey(char,int)
meth public void typeKey(int,char,int)
meth public void verify()
meth public void waitCollapsed()
meth public void waitExpanded()
meth public void waitFolding()
meth public void waitModified(boolean)
supr org.netbeans.jellytools.TopComponentOperator
hfds WAIT_TIME,_lblInputMode,_lblRowColumn,_lblStatusBar,_txtEditorPane
hcls ToolbarButtonChooser

CLSS public final static org.netbeans.jellytools.EditorOperator$EditorSubchooser
 outer org.netbeans.jellytools.EditorOperator
cons public init()
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object

CLSS public org.netbeans.jellytools.EditorWindowOperator
cons public init()
 anno 0 java.lang.Deprecated()
meth public static boolean jumpLeft()
meth public static org.netbeans.jellytools.EditorOperator getEditor()
meth public static org.netbeans.jellytools.EditorOperator getEditor(int)
meth public static org.netbeans.jellytools.EditorOperator getEditor(java.lang.String)
meth public static org.netbeans.jellytools.EditorOperator selectPage(int)
meth public static org.netbeans.jellytools.EditorOperator selectPage(java.lang.String)
meth public static org.netbeans.jemmy.operators.JButtonOperator btDown()
meth public static org.netbeans.jemmy.operators.JButtonOperator btLeft()
meth public static org.netbeans.jemmy.operators.JButtonOperator btRight()
meth public static void closeDiscard()
meth public static void closeDiscard(org.openide.windows.Mode)
meth public static void moveTabsLeft()
meth public static void moveTabsRight()
meth public static void selectDocument(int)
meth public static void selectDocument(java.lang.String)
meth public static void verify()
supr java.lang.Object
hfds _btDown,_btLeft,_btRight

CLSS public org.netbeans.jellytools.FavoritesOperator
cons public init()
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public static org.netbeans.jellytools.FavoritesOperator invoke()
meth public void collapseAll()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds FAVORITES_CAPTION,_tree,viewAction
hcls FavoritesTabSubchooser

CLSS public org.netbeans.jellytools.FilesTabOperator
cons public init()
meth public org.netbeans.jellytools.nodes.Node getProjectNode(java.lang.String)
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public static org.netbeans.jellytools.FilesTabOperator invoke()
meth public void collapseAll()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds FILES_CAPTION,_tree,viewAction
hcls FilesTabSubchooser

CLSS public org.netbeans.jellytools.FindInFilesOperator
cons public init()
meth public org.netbeans.jellytools.SearchResultsOperator find()
meth public org.netbeans.jemmy.operators.JButtonOperator btFind()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbCase()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbWholeWords()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtPatterns()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtText()
meth public static org.netbeans.jellytools.FindInFilesOperator invoke(org.netbeans.jellytools.nodes.Node)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _btFind,invokeAction

CLSS public org.netbeans.jellytools.HelpOperator
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getContentText()
meth public java.lang.String getTitle()
meth public org.netbeans.jemmy.operators.JButtonOperator btBack()
meth public org.netbeans.jemmy.operators.JButtonOperator btNext()
meth public org.netbeans.jemmy.operators.JButtonOperator btPageSetup()
meth public org.netbeans.jemmy.operators.JButtonOperator btPrint()
meth public org.netbeans.jemmy.operators.JEditorPaneOperator txtContentViewer()
meth public org.netbeans.jemmy.operators.JSplitPaneOperator splpHelpSplitPane()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator tbpHelpTabPane()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtSearchFind()
meth public org.netbeans.jemmy.operators.JTreeOperator treeContents()
meth public org.netbeans.jemmy.operators.JTreeOperator treeSearch()
meth public static org.netbeans.jellytools.HelpOperator invoke()
meth public void back()
meth public void close()
meth public void next()
meth public void pageSetup()
meth public void print()
meth public void searchFind(java.lang.String)
meth public void selectPageContents()
meth public void selectPageSearch()
meth public void verify()
supr org.netbeans.jemmy.operators.WindowOperator
hfds _btBack,_btNext,_btPageSetup,_btPrint,_splpHelpSplitPane,_tbpHelpTabPane,_treeContents,_treeSearch,_txtContentViewer,_txtSearchFind,helpAction,helpButtonChooser,jHelpChooser
hcls HelpWindowChooser

CLSS public org.netbeans.jellytools.JavaProjectsTabOperator
cons public init()
meth public org.netbeans.jellytools.nodes.JavaProjectRootNode getJavaProjectRootNode(java.lang.String)
meth public static org.netbeans.jellytools.JavaProjectsTabOperator invoke()
supr org.netbeans.jellytools.ProjectsTabOperator

CLSS public org.netbeans.jellytools.JellyTestCase
cons public init(java.lang.String)
fld public boolean captureScreen
fld public boolean closeAllModal
fld public boolean dumpScreen
fld public boolean waitNoEvent
meth protected !varargs static junit.framework.Test createModuleTest(java.lang.Class,java.lang.String[])
meth protected !varargs static junit.framework.Test createModuleTest(java.lang.String,java.lang.String,java.lang.Class,java.lang.String[])
meth protected void clearTestStatus()
meth protected void endTest()
meth protected void failNotify(java.lang.Throwable)
meth protected void initEnvironment()
meth protected void runTest() throws java.lang.Throwable
meth protected void startTest()
meth public !varargs void closeOpenedProjects(java.lang.Object[])
meth public !varargs void openDataProjects(java.lang.String[]) throws java.io.IOException
meth public !varargs void openProjects(java.lang.String[]) throws java.io.IOException
meth public static org.netbeans.junit.NbModuleSuite$Configuration emptyConfiguration()
meth public static void closeAllModal()
meth public void closeOpenedProjects()
meth public void fail(java.lang.Throwable)
meth public void runBare() throws java.lang.Throwable
meth public void waitScanFinished()
supr org.netbeans.junit.NbTestCase
hfds isScreenCaptured,openedProjects,testStatus

CLSS public org.netbeans.jellytools.JellyVersion
cons public init()
meth public static java.lang.String getBuild()
meth public static java.lang.String getIDEVersion()
meth public static java.lang.String getJemmyMajorVersion()
meth public static java.lang.String getJemmyMinorVersion()
meth public static java.lang.String getJemmyVersion()
meth public static java.lang.String getMajorVersion()
meth public static java.lang.String getMinorVersion()
meth public static java.lang.String getVersion()
meth public static void checkJemmyVersion()
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds jemmyVersionChecked

CLSS public org.netbeans.jellytools.MainWindowOperator
cons public init()
innr public StatusTextTracer
meth public int getToolbarCount()
meth public java.lang.String getStatusText()
meth public java.lang.String getToolbarName(int)
meth public org.netbeans.jellytools.MainWindowOperator$StatusTextTracer getStatusTextTracer()
meth public org.netbeans.jemmy.operators.ContainerOperator getToolbar(int)
meth public org.netbeans.jemmy.operators.ContainerOperator getToolbar(java.lang.String)
meth public org.netbeans.jemmy.operators.JButtonOperator getToolbarButton(org.netbeans.jemmy.operators.ContainerOperator,int)
meth public org.netbeans.jemmy.operators.JButtonOperator getToolbarButton(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
meth public org.netbeans.jemmy.operators.JMenuBarOperator menuBar()
meth public static org.netbeans.jellytools.MainWindowOperator getDefault()
meth public void dragNDropToolbar(org.netbeans.jemmy.operators.ContainerOperator,int,int)
meth public void maximize()
meth public void pushToolbarPopupMenu(java.lang.String)
meth public void pushToolbarPopupMenuNoBlock(java.lang.String)
meth public void setStatusText(java.lang.String)
meth public void verify()
meth public void waitStatusText(java.lang.String)
supr org.netbeans.jemmy.operators.JFrameOperator
hfds _menuBar,statusTextTracer
hcls ToolbarButtonChooser,ToolbarChooser

CLSS public org.netbeans.jellytools.MainWindowOperator$StatusTextTracer
 outer org.netbeans.jellytools.MainWindowOperator
cons public init(org.netbeans.jellytools.MainWindowOperator)
intf javax.swing.event.ChangeListener
meth protected void finalize()
meth public boolean contains(java.lang.String,boolean)
meth public java.util.ArrayList getStatusTextHistory()
meth public void clear()
meth public void printStatusTextHistory(java.io.PrintStream)
meth public void start()
meth public void stateChanged(javax.swing.event.ChangeEvent)
meth public void stop()
meth public void waitText(java.lang.String)
meth public void waitText(java.lang.String,boolean)
supr java.lang.Object
hfds statusTextHistory

CLSS public org.netbeans.jellytools.NavigatorOperator
cons public init()
meth public org.netbeans.jemmy.operators.JTreeOperator getTree()
meth public static org.netbeans.jellytools.NavigatorOperator invokeNavigator()
supr org.netbeans.jellytools.TopComponentOperator
hfds NAVIGATOR_TITLE
hcls NavigatorAction,NavigatorComponentChooser

CLSS public org.netbeans.jellytools.NbDialogOperator
cons public init(java.lang.String)
cons public init(javax.swing.JDialog)
meth public org.netbeans.jemmy.operators.JButtonOperator btCancel()
meth public org.netbeans.jemmy.operators.JButtonOperator btClose()
meth public org.netbeans.jemmy.operators.JButtonOperator btHelp()
meth public org.netbeans.jemmy.operators.JButtonOperator btNo()
meth public org.netbeans.jemmy.operators.JButtonOperator btOK()
meth public org.netbeans.jemmy.operators.JButtonOperator btYes()
meth public void cancel()
meth public void close()
meth public void closeByButton()
meth public void help()
meth public void no()
meth public void ok()
meth public void yes()
supr org.netbeans.jemmy.operators.JDialogOperator
hfds _btCancel,_btClose,_btHelp,_btNo,_btOK,_btYes

CLSS public org.netbeans.jellytools.NewFileWizardOperator
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getSelectedFileType()
meth public java.lang.String getSelectedProject()
meth public javax.swing.tree.TreePath getSelectedCategory()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboProject()
meth public org.netbeans.jemmy.operators.JEditorPaneOperator txtDescription()
meth public org.netbeans.jemmy.operators.JLabelOperator lblCategories()
meth public org.netbeans.jemmy.operators.JLabelOperator lblDescription()
meth public org.netbeans.jemmy.operators.JLabelOperator lblFileTypes()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProject()
meth public org.netbeans.jemmy.operators.JListOperator lstFileTypes()
meth public org.netbeans.jemmy.operators.JTreeOperator treeCategories()
meth public static org.netbeans.jellytools.NewFileWizardOperator invoke()
meth public static org.netbeans.jellytools.NewFileWizardOperator invoke(java.lang.String)
meth public static org.netbeans.jellytools.NewFileWizardOperator invoke(org.netbeans.jellytools.nodes.Node,java.lang.String,java.lang.String)
meth public void selectCategory(java.lang.String)
meth public void selectFileType(java.lang.String)
meth public void selectProject(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.WizardOperator
hfds _cboProject,_lblCategories,_lblDescription,_lblFileTypes,_lblProject,_lstFileTypes,_treeCategories,_txtDescription

CLSS public org.netbeans.jellytools.NewJavaFileNameLocationStepOperator
cons public init()
cons public init(java.lang.String)
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboLocation()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboPackage()
meth public org.netbeans.jemmy.operators.JLabelOperator lblCreatedFile()
meth public org.netbeans.jemmy.operators.JLabelOperator lblLocation()
meth public org.netbeans.jemmy.operators.JLabelOperator lblObjectName()
meth public org.netbeans.jemmy.operators.JLabelOperator lblPackage()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProject()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtCreatedFile()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtObjectName()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtProject()
meth public void selectPackage(java.lang.String)
meth public void selectSourcePackagesLocation()
meth public void selectTestPackagesLocation()
meth public void setObjectName(java.lang.String)
meth public void setPackage(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NewFileWizardOperator
hfds _cboLocation,_cboPackage,_lblCreatedFile,_lblLocation,_lblObjectName,_lblPackage,_lblProject,_txtCreatedFile,_txtObjectName,_txtProject

CLSS public org.netbeans.jellytools.NewJavaFileWizardOperator
cons public init()
meth public static void create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr org.netbeans.jellytools.NewFileWizardOperator

CLSS public org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator
cons public init()
meth public org.netbeans.jemmy.operators.JButtonOperator btBrowseBuildScript()
meth public org.netbeans.jemmy.operators.JButtonOperator btBrowseLocation()
meth public org.netbeans.jemmy.operators.JButtonOperator btBrowseProjectFolder()
meth public org.netbeans.jemmy.operators.JButtonOperator btBrowseProjectLocation()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbCreateMainClass()
meth public org.netbeans.jemmy.operators.JLabelOperator lblBuildScript()
meth public org.netbeans.jemmy.operators.JLabelOperator lblLocation()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProjectFolder()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProjectLocation()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProjectName()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtBuildScript()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtCreateMainClass()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtLocation()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtProjectFolder()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtProjectLocation()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtProjectName()
meth public void verify()
supr org.netbeans.jellytools.NewProjectWizardOperator
hfds _btBrowseBuildScript,_btBrowseLocation,_btBrowseProjectFolder,_cbCreateMainClass,_lblBuildScript,_lblLocation,_lblProjectFolder,_lblProjectLocation,_lblProjectName,_txtBuildScript,_txtCreateMainClass,_txtLocation,_txtProjectFolder,_txtProjectLocation,_txtProjectName

CLSS public org.netbeans.jellytools.NewProjectWizardOperator
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getDescription()
meth public java.lang.String getSelectedProject()
meth public javax.swing.tree.TreePath getSelectedCategory()
meth public org.netbeans.jemmy.operators.JEditorPaneOperator txtDescription()
meth public org.netbeans.jemmy.operators.JLabelOperator lblCategories()
meth public org.netbeans.jemmy.operators.JLabelOperator lblDescription()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProjects()
meth public org.netbeans.jemmy.operators.JListOperator lstProjects()
meth public org.netbeans.jemmy.operators.JTreeOperator treeCategories()
meth public static org.netbeans.jellytools.NewProjectWizardOperator invoke()
meth public static org.netbeans.jellytools.NewProjectWizardOperator invoke(java.lang.String)
meth public void finish()
meth public void selectCategory(java.lang.String)
meth public void selectProject(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.WizardOperator
hfds _lblCategories,_lblDescription,_lblProjects,_lstProjects,_treeCategories,_txtDescription

CLSS public org.netbeans.jellytools.OptionsOperator
cons public init()
fld public final static int DEFAULT_LEVEL = 3
fld public final static int USER_LEVEL = 2
meth protected int getValue(int,int)
meth protected void clickOnSecondHeader()
meth protected void defineHere(int,int)
meth protected void editLevel(int,int,java.lang.String)
meth protected void revertLevel(int,int)
meth protected void setLevel(int,int)
meth public int getLevel(java.lang.String)
meth public int selectOption(java.lang.String)
meth public java.awt.Component getSource()
meth public org.netbeans.jellytools.TreeTableOperator treeTable()
meth public org.netbeans.jellytools.properties.PropertySheetOperator getPropertySheet(java.lang.String)
meth public static org.netbeans.jellytools.OptionsOperator invoke()
meth public void hideLevels()
meth public void selectCategory(java.lang.String)
meth public void selectEditor()
meth public void selectFontAndColors()
meth public void selectGeneral()
meth public void selectJava()
meth public void selectKeymap()
meth public void selectMiscellaneous()
meth public void setDefaultLevel(java.lang.String)
meth public void setLevel(java.lang.String,int)
meth public void setUserLevel(java.lang.String)
meth public void showLevels()
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds BEFORE_EDITING_TIMEOUT,DEFINE_HERE,invokeAction,optionsSubchooser,sourceInternal

CLSS public org.netbeans.jellytools.OutlineOperator
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.swing.outline.Outline)
meth protected int getPrecedingSiblingsRowSpan(javax.swing.tree.TreePath)
meth protected int getRowSpanOfLastElement(javax.swing.tree.TreePath)
meth protected int getVisibleRootModifier()
meth public boolean isExpanded(javax.swing.tree.TreePath)
meth public int getRowForPath(javax.swing.tree.TreePath)
meth public int getTreeColumnIndex()
meth public java.awt.Point getLocationForPath(javax.swing.tree.TreePath)
meth public javax.swing.tree.TreePath findNextPathElement(javax.swing.tree.TreePath,java.lang.String)
meth public javax.swing.tree.TreePath findNextPathElement(javax.swing.tree.TreePath,java.lang.String,int)
meth public javax.swing.tree.TreePath findPath(java.lang.String)
meth public javax.swing.tree.TreePath findPath(javax.swing.tree.TreePath,java.lang.String)
meth public org.netbeans.jellytools.nodes.OutlineNode getRootNode(java.lang.String)
meth public org.netbeans.jellytools.nodes.OutlineNode getRootNode(java.lang.String,int)
meth public org.netbeans.swing.outline.Outline getOutline()
meth public void expandPath(javax.swing.tree.TreePath)
meth public void scrollToPath(javax.swing.tree.TreePath)
meth public void selectPath(javax.swing.tree.TreePath)
meth public void waitExpanded(javax.swing.tree.TreePath)
supr org.netbeans.jemmy.operators.JTableOperator
hcls NodeWaiter,OutlineFinder

CLSS public org.netbeans.jellytools.OutputOperator
cons public init()
meth public java.lang.String getText()
meth public org.netbeans.jellytools.OutputTabOperator getOutputTab(java.lang.String)
meth public static org.netbeans.jellytools.OutputOperator invoke()
meth public void clear()
meth public void copy()
meth public void find()
meth public void findNext()
meth public void nextError()
meth public void previousError()
meth public void saveAs()
meth public void selectAll()
meth public void verify()
meth public void wrapText()
supr org.netbeans.jellytools.TopComponentOperator
hfds invokeAction,outputSubchooser

CLSS public org.netbeans.jellytools.OutputTabOperator
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(javax.swing.JComponent)
innr protected final static OutputTabSubchooser
meth public final void makeComponentVisible()
meth public int findLine(java.lang.String)
meth public int getLength()
meth public int getLineCount()
meth public java.lang.String getLine(int)
meth public java.lang.String getText()
meth public java.lang.String getText(int,int)
meth public org.netbeans.jemmy.operators.ComponentOperator outputPaneOperator()
meth public org.netbeans.jemmy.operators.JButtonOperator btnAntSettings()
meth public org.netbeans.jemmy.operators.JButtonOperator btnReRun()
meth public org.netbeans.jemmy.operators.JButtonOperator btnReRunWithDifferentParameters()
meth public org.netbeans.jemmy.operators.JButtonOperator btnStop()
meth public void clear()
meth public void close()
meth public void copy()
meth public void find()
meth public void findNext()
meth public void nextError()
meth public void previousError()
meth public void saveAs()
meth public void selectAll()
meth public void verify()
meth public void waitText(java.lang.String)
meth public void wrapText()
supr org.netbeans.jemmy.operators.JComponentOperator
hfds btnAntSettings,btnReRun,btnReRunWithDifferentParameters,btnStop,clearAction,closeAction,copyAction,findAction,findNextAction,nextErrorAction,outputPaneOperator,previousErrorAction,saveAsAction,wrapTextAction

CLSS protected final static org.netbeans.jellytools.OutputTabOperator$OutputTabSubchooser
 outer org.netbeans.jellytools.OutputTabOperator
cons public init()
cons public init(java.lang.String)
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object
hfds tabName

CLSS public org.netbeans.jellytools.PaletteOperator
cons public init()
meth public org.netbeans.jemmy.operators.JListOperator lstComponents()
meth public static org.netbeans.jellytools.PaletteOperator invoke()
meth public void expand(org.netbeans.jemmy.operators.JCheckBoxOperator,boolean)
meth public void selectComponent(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds PALETTE_TITLE,paletteAction
hcls PaletteTopComponentChooser

CLSS public org.netbeans.jellytools.PluginsOperator
cons public init()
meth public org.netbeans.jellytools.WizardOperator installer()
meth public org.netbeans.jemmy.operators.JButtonOperator btAddPlugins()
meth public org.netbeans.jemmy.operators.JButtonOperator btDeactivate()
meth public org.netbeans.jemmy.operators.JButtonOperator btInstall()
meth public org.netbeans.jemmy.operators.JButtonOperator btReloadCatalog()
meth public org.netbeans.jemmy.operators.JButtonOperator btUninstall()
meth public org.netbeans.jemmy.operators.JButtonOperator btUpdate()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbShowDetails()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectAvailablePlugins()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectDownloaded()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectInstalled()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectSettings()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectTab(java.lang.String)
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator selectUpdates()
meth public org.netbeans.jemmy.operators.JTabbedPaneOperator tabbedPane()
meth public org.netbeans.jemmy.operators.JTableOperator table()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtSearch()
meth public static org.netbeans.jellytools.PluginsOperator invoke()
meth public void addPlugin(java.lang.String)
meth public void addPlugins()
meth public void deactivate()
meth public void finishInstall()
meth public void install()
meth public void install(java.lang.String)
meth public void install(java.lang.String[])
meth public void reloadCatalog()
meth public void search(java.lang.String)
meth public void selectPlugin(java.lang.String)
meth public void selectPlugins(java.lang.String[])
meth public void uninstall()
meth public void update()
meth public void waitTabEnabled(java.lang.String)
supr org.netbeans.jellytools.NbDialogOperator
hfds AVAILABLE_PLUGINS_LABEL,INSTALLED_LABEL,PLUGINS_ITEM,TITLE,TOOLS_ITEM,_tabbedPane

CLSS public org.netbeans.jellytools.ProjectsTabOperator
cons public init()
fld protected final static org.netbeans.jellytools.actions.ProjectViewAction viewAction
meth public org.netbeans.jellytools.nodes.ProjectRootNode getProjectRootNode(java.lang.String)
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public static org.netbeans.jellytools.ProjectsTabOperator invoke()
meth public void collapseAll()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds PROJECT_CAPTION,_tree
hcls ProjectsTabSubchooser

CLSS public org.netbeans.jellytools.QuestionDialogOperator
cons public init()
cons public init(java.lang.String)
meth public org.netbeans.jemmy.operators.JLabelOperator lblQuestion()
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _lblQuestion

CLSS public org.netbeans.jellytools.RuntimeTabOperator
cons public init()
meth public org.netbeans.jellytools.nodes.Node getRootNode()
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public static org.netbeans.jellytools.RuntimeTabOperator invoke()
meth public void collapseAll()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds RUNTIME_CAPTION,_tree,viewAction
hcls RuntimeTabSubchooser

CLSS public org.netbeans.jellytools.SaveAsTemplateOperator
cons public init()
meth public org.netbeans.jellytools.nodes.Node getRootNode()
meth public org.netbeans.jemmy.operators.JLabelOperator lblSelectTheCategory()
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public static org.netbeans.jellytools.SaveAsTemplateOperator invoke(org.netbeans.jellytools.nodes.Node)
meth public static org.netbeans.jellytools.SaveAsTemplateOperator invoke(org.netbeans.jellytools.nodes.Node[])
meth public void selectTemplate(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _lblSelectTheCategory,_tree

CLSS public org.netbeans.jellytools.SearchResultsOperator
cons public init()
meth public org.netbeans.jellytools.FindInFilesOperator modifySearch()
meth public org.netbeans.jellytools.OutlineOperator outlineResult()
meth public org.netbeans.jellytools.OutputTabOperator showDetails()
meth public org.netbeans.jemmy.operators.JButtonOperator btModifySearch()
meth public org.netbeans.jemmy.operators.JButtonOperator btShowDetails()
meth public org.netbeans.jemmy.operators.JButtonOperator btStopSearch()
meth public void openResult(java.lang.String)
meth public void selectResult(java.lang.String)
meth public void stopSearch()
meth public void verify()
meth public void waitEndOfSearch()
supr org.netbeans.jellytools.TopComponentOperator
hfds SEARCH_TIME,TITLE,_btModifySearch,_btShowDetails,_btStop,_outlineResult

CLSS public org.netbeans.jellytools.SourcePackageFoldersStepOperator
cons public init()
meth public java.lang.String getSelectedSourceLevel()
meth public org.netbeans.jemmy.operators.JButtonOperator btAddFolder()
meth public org.netbeans.jemmy.operators.JButtonOperator btRemove()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboSourceLevel()
meth public org.netbeans.jemmy.operators.JLabelOperator lblOnlineError()
meth public org.netbeans.jemmy.operators.JLabelOperator lblSourceLevel()
meth public org.netbeans.jemmy.operators.JLabelOperator lblSourcePackageFolders()
meth public org.netbeans.jemmy.operators.JLabelOperator lblSpecifyFolders()
meth public org.netbeans.jemmy.operators.JTableOperator tblSourcePackageFolders()
meth public void addFolder()
meth public void remove()
meth public void selectSourceLevel(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NewProjectWizardOperator
hfds _btAddFolder,_btRemove,_cboSourceLevel,_lblOnlineError,_lblSourceLevel,_lblSourcePackageFolders,_lblSpecifyFolders,_tblSourcePackageFolders

CLSS public abstract org.netbeans.jellytools.TestBundleKeys
cons public init(java.lang.String)
meth protected abstract java.lang.ClassLoader getDescendantClassLoader()
meth protected abstract java.lang.String getPropertiesName()
meth protected static java.util.Properties getProperties(java.lang.ClassLoader,java.lang.String) throws java.io.IOException
meth protected static junit.framework.Test prepareSuite(java.lang.Class,java.lang.String)
meth protected void runTest() throws java.lang.Throwable
supr org.netbeans.junit.NbTestCase
hfds props

CLSS public org.netbeans.jellytools.TopComponentOperator
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(javax.swing.JComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
meth protected boolean isOpened()
meth protected org.netbeans.jellytools.TopComponentOperator findParentTopComponent()
meth protected static javax.swing.JComponent findTopComponent(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,org.netbeans.jemmy.ComponentChooser)
meth protected static javax.swing.JComponent waitTopComponent(java.lang.String,int)
meth protected static javax.swing.JComponent waitTopComponent(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,org.netbeans.jemmy.ComponentChooser)
meth public boolean isModified()
meth public java.awt.Container findTabDisplayer()
meth public org.netbeans.swing.tabcontrol.TabbedContainer findTabbedAdapter()
meth public static javax.swing.JComponent findTopComponent(java.lang.String,int)
meth public void attachTo(java.lang.String,java.lang.String)
meth public void attachTo(org.netbeans.jellytools.TopComponentOperator,java.lang.String)
meth public void cloneDocument()
meth public void close()
meth public void closeAllDocuments()
meth public void closeDiscard()
meth public void closeWindow()
meth public void makeComponentVisible()
meth public void maximize()
meth public void pushMenuOnTab(java.lang.String)
meth public void restore()
meth public void save()
meth public void setUnmodified()
meth public void waitClosed()
supr org.netbeans.jemmy.operators.JComponentOperator
hfds closeAllDocumentsItem,closeWindowItem

CLSS public org.netbeans.jellytools.TreeTableOperator
cons public init(javax.swing.JTable)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
innr public static RenderedMouseDriver
innr public static RenderedTreeOperator
meth public int selectNode(java.lang.String)
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public void verify()
supr org.netbeans.jemmy.operators.JTableOperator
hfds _tree
hcls TreeTableFinder

CLSS public static org.netbeans.jellytools.TreeTableOperator$RenderedMouseDriver
 outer org.netbeans.jellytools.TreeTableOperator
cons public init()
intf org.netbeans.jemmy.drivers.MouseDriver
meth public void clickMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public void dragMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void dragNDrop(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public void enterMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void exitMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public void moveMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public void pressMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public void releaseMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
supr org.netbeans.jemmy.drivers.SupportiveDriver

CLSS public static org.netbeans.jellytools.TreeTableOperator$RenderedTreeOperator
 outer org.netbeans.jellytools.TreeTableOperator
cons public init(org.netbeans.jellytools.TreeTableOperator,javax.swing.JTree)
meth public javax.swing.JPopupMenu callPopupOnPaths(javax.swing.tree.TreePath[],int)
meth public org.netbeans.jemmy.operators.ComponentOperator getRealOperator()
meth public void expandPath(javax.swing.tree.TreePath)
meth public void scrollToPath(javax.swing.tree.TreePath)
supr org.netbeans.jemmy.operators.JTreeOperator
hfds oper

CLSS public org.netbeans.jellytools.WizardOperator
cons public init(java.lang.String)
cons public init(javax.swing.JDialog)
meth protected void checkPanel(java.lang.String)
meth public int stepsGetSelectedIndex()
meth public java.lang.String stepsGetSelectedValue()
meth public org.netbeans.jemmy.operators.JButtonOperator btBack()
meth public org.netbeans.jemmy.operators.JButtonOperator btFinish()
meth public org.netbeans.jemmy.operators.JButtonOperator btNext()
meth public org.netbeans.jemmy.operators.JListOperator lstSteps()
meth public void back()
meth public void finish()
meth public void next()
meth public void stepsWaitSelectedIndex(int)
meth public void stepsWaitSelectedValue(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds WAIT_TIME,_btBack,_btFinish,_btNext,_lstSteps

CLSS public org.netbeans.jellytools.actions.Action
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,javax.swing.KeyStroke)
cons public init(java.lang.String,java.lang.String,java.lang.String,javax.swing.KeyStroke[])
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut[])
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,javax.swing.KeyStroke)
cons public init(java.lang.String,java.lang.String,javax.swing.KeyStroke[])
cons public init(java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut[])
 anno 0 java.lang.Deprecated()
fld protected final static long AFTER_ACTION_WAIT_TIME = 0
fld protected final static long SELECTION_WAIT_TIME = 300
fld protected final static long WAIT_AFTER_SHORTCUT_TIMEOUT = 0
fld protected java.lang.Class systemActionClass
fld protected java.lang.String layerInstancePath
fld protected java.lang.String menuPath
fld protected java.lang.String popupPath
fld protected javax.swing.KeyStroke[] keystrokes
fld public final static int API_MODE = 2
fld public final static int MENU_MODE = 0
fld public final static int POPUP_MODE = 1
fld public final static int SHORTCUT_MODE = 3
innr public static Shortcut
meth protected void testNodes(org.netbeans.jellytools.nodes.Node[])
meth public boolean isEnabled()
meth public boolean isEnabled(org.netbeans.jellytools.nodes.Node)
meth public boolean isEnabled(org.netbeans.jellytools.nodes.Node[])
meth public boolean isEnabled(org.netbeans.jemmy.operators.ComponentOperator)
meth public int getDefaultMode()
meth public int setDefaultMode(int)
meth public java.lang.Class getSystemActionClass()
meth public java.lang.String getMenuPath()
meth public java.lang.String getPopupPath()
meth public javax.swing.KeyStroke[] getKeyStrokes()
meth public org.netbeans.jemmy.operators.Operator$StringComparator getComparator()
meth public void perform()
meth public void perform(org.netbeans.jellytools.nodes.Node)
meth public void perform(org.netbeans.jellytools.nodes.Node[])
meth public void perform(org.netbeans.jellytools.nodes.OutlineNode)
meth public void perform(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performAPI()
meth public void performAPI(org.netbeans.jellytools.nodes.Node)
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performAPI(org.netbeans.jellytools.nodes.OutlineNode)
meth public void performAPI(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performMenu()
meth public void performMenu(org.netbeans.jellytools.nodes.Node)
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.OutlineNode)
meth public void performMenu(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performPopup()
meth public void performPopup(org.netbeans.jellytools.nodes.Node)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.nodes.OutlineNode)
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut()
meth public void performShortcut(org.netbeans.jellytools.nodes.Node)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
meth public void performShortcut(org.netbeans.jellytools.nodes.OutlineNode)
meth public void performShortcut(org.netbeans.jemmy.operators.ComponentOperator)
meth public void setComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
supr java.lang.Object
hfds comparator,defaultComparator,sequence

CLSS public static org.netbeans.jellytools.actions.Action$Shortcut
 outer org.netbeans.jellytools.actions.Action
 anno 0 java.lang.Deprecated()
cons public init(int)
cons public init(int,int)
fld protected int keyCode
fld protected int keyModifiers
meth public int getKeyCode()
meth public int getKeyModifiers()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.netbeans.jellytools.actions.ActionNoBlock
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,javax.swing.KeyStroke)
cons public init(java.lang.String,java.lang.String,java.lang.String,javax.swing.KeyStroke[])
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut[])
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,javax.swing.KeyStroke)
cons public init(java.lang.String,java.lang.String,javax.swing.KeyStroke[])
cons public init(java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.netbeans.jellytools.actions.Action$Shortcut[])
 anno 0 java.lang.Deprecated()
meth public void performAPI()
meth public void performMenu()
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.nodes.OutlineNode)
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut()
supr org.netbeans.jellytools.actions.Action

CLSS public org.netbeans.jellytools.actions.AddLocaleAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds addPopup,localePopup

CLSS public org.netbeans.jellytools.actions.AttachWindowAction
cons public init(java.lang.String,java.lang.String)
cons public init(org.netbeans.jellytools.TopComponentOperator,java.lang.String)
fld public final static java.lang.String AS_LAST_TAB = "As a Last Tab"
fld public final static java.lang.String BOTTOM = "bottom"
fld public final static java.lang.String LEFT = "left"
fld public final static java.lang.String RIGHT = "right"
fld public final static java.lang.String TOP = "top"
meth public void performAPI(org.netbeans.jellytools.TopComponentOperator)
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performAPI(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds sideConstant,targetTopComponentName,targetTopComponentOperator,windowItem

CLSS public org.netbeans.jellytools.actions.BuildJavaProjectAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds buildProjectPopup

CLSS public org.netbeans.jellytools.actions.CleanJavaProjectAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds cleanProjectPopup

CLSS public org.netbeans.jellytools.actions.CloneViewAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds configureWindowItem,menuPath,popupPath,windowItem

CLSS public org.netbeans.jellytools.actions.CloseAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds closePopup

CLSS public org.netbeans.jellytools.actions.CloseAllDocumentsAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds menuPath,popupPath,windowItem

CLSS public org.netbeans.jellytools.actions.CloseViewAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds menuPath,popupPath,windowItem

CLSS public org.netbeans.jellytools.actions.CompileJavaAction
cons public init()
meth public void performMenu(org.netbeans.jellytools.nodes.Node)
meth public void performPopup(org.netbeans.jellytools.nodes.Node)
supr org.netbeans.jellytools.actions.Action
hfds compileItem,compileMenu,compilePopup,keystroke

CLSS public org.netbeans.jellytools.actions.CopyAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds copyMenu,copyPopup,keystroke

CLSS public org.netbeans.jellytools.actions.CustomizeAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds customizePopup

CLSS public org.netbeans.jellytools.actions.CutAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds cutMenu,cutPopup,keystroke

CLSS public org.netbeans.jellytools.actions.DebugProjectAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds KEYSTROKE,debugProjectMenu,debugProjectPopup

CLSS public org.netbeans.jellytools.actions.DeleteAction
cons public init()
meth public static void confirmDeletion()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds deleteMenu,deletePopup,keystroke

CLSS public org.netbeans.jellytools.actions.DockWindowAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds configureWindowItem,menuPath,popupPath,windowItem

CLSS public org.netbeans.jellytools.actions.DocumentsAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds menuPath,windowItem

CLSS public org.netbeans.jellytools.actions.EditAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds editPopup

CLSS public org.netbeans.jellytools.actions.ExploreFromHereAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds explorerPopup

CLSS public org.netbeans.jellytools.actions.FavoritesAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds allFilesMenu,keystroke

CLSS public org.netbeans.jellytools.actions.FilesViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds filesMenu

CLSS public org.netbeans.jellytools.actions.FindAction
cons public init()
meth public void performAPI()
meth public void performShortcut()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds findMenu,findPopup,keystroke

CLSS public org.netbeans.jellytools.actions.FindInFilesAction
cons public init()
meth public void performAPI()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds menu,popup

CLSS public org.netbeans.jellytools.actions.HelpAction
cons public init()
meth public static java.lang.String getHelpMenu()
supr org.netbeans.jellytools.actions.Action
hfds helpMenu,keystroke,popupPath

CLSS public org.netbeans.jellytools.actions.MaximizeWindowAction
cons public init()
meth public void performAPI()
meth public void performAPI(org.netbeans.jellytools.TopComponentOperator)
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds configureWindowItem,popupPathMaximize,windowItem,windowMaximizePath

CLSS public org.netbeans.jellytools.actions.NewFileAction
cons public init()
cons public init(java.lang.String)
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds keystroke,menuPathNewFile,popupPathNew,popupSubPath

CLSS public org.netbeans.jellytools.actions.NewProjectAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds menuPath

CLSS public org.netbeans.jellytools.actions.OpenAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds openPopup

CLSS public org.netbeans.jellytools.actions.OptionsViewAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds menu

CLSS public org.netbeans.jellytools.actions.OutputWindowViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,menu

CLSS public org.netbeans.jellytools.actions.PaletteViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds projectMenu

CLSS public org.netbeans.jellytools.actions.PasteAction
cons public init()
fld protected final static java.lang.String MENU
fld protected final static java.lang.String POPUP
fld protected final static java.lang.String SYSTEMCLASS = "org.openide.actions.PasteAction"
fld protected final static javax.swing.KeyStroke KEYSTROKE
supr org.netbeans.jellytools.actions.Action

CLSS public org.netbeans.jellytools.actions.PasteActionNoBlock
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock

CLSS public org.netbeans.jellytools.actions.ProjectViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds projectMenu

CLSS public org.netbeans.jellytools.actions.PropertiesAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,propertiesMenu,propertiesPopup

CLSS public org.netbeans.jellytools.actions.RenameAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds renamePopup

CLSS public org.netbeans.jellytools.actions.ReplaceAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,replaceMenu

CLSS public org.netbeans.jellytools.actions.RestoreWindowAction
cons public init()
meth public void performAPI()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds configureWindowItem,popupPathMaximize,windowItem,windowMaximizePath

CLSS public org.netbeans.jellytools.actions.RuntimeViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds runtimeMenu

CLSS public org.netbeans.jellytools.actions.SaveAction
cons public init()
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
 anno 0 java.lang.Deprecated()
meth public void performPopup(org.netbeans.jellytools.nodes.Node)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
 anno 0 java.lang.Deprecated()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,saveMenu,savePopup

CLSS public org.netbeans.jellytools.actions.SaveAllAction
cons public init()
meth public void performAPI()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,saveAllMenu

CLSS public org.netbeans.jellytools.actions.SaveAsTemplateAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock

CLSS public org.netbeans.jellytools.actions.ShowDescriptionAreaAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds popupPath

CLSS public org.netbeans.jellytools.actions.SortByCategoryAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds popupPath

CLSS public org.netbeans.jellytools.actions.SortByNameAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds popupPath

CLSS public org.netbeans.jellytools.actions.UndockWindowAction
cons public init()
meth public void performAPI(org.netbeans.jellytools.nodes.Node[])
meth public void performMenu(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jellytools.TopComponentOperator)
meth public void performPopup(org.netbeans.jellytools.nodes.Node[])
meth public void performPopup(org.netbeans.jemmy.operators.ComponentOperator)
meth public void performShortcut(org.netbeans.jellytools.nodes.Node[])
supr org.netbeans.jellytools.actions.Action
hfds configureWindowItem,floatMenuPath,floatPopupPath,windowItem

CLSS public org.netbeans.jellytools.actions.ViewAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds viewPopup

CLSS public org.netbeans.jellytools.modules.debugger.AttachDialogOperator
cons public init()
fld public final static java.lang.String ITEM_SHARED_MEMORY_ATTACH = "SharedMemoryAttach (Attaches by shared memory to other VMs)"
fld public final static java.lang.String ITEM_SHARED_MEMORY_LISTEN = "SharedMemoryListen (Accepts shared memory connections initiated by other VMs)"
fld public final static java.lang.String ITEM_SOCKET_ATTACH = "SocketAttach (Attaches by socket to other VMs)"
fld public final static java.lang.String ITEM_SOCKET_LISTEN = "SocketListen (Accepts socket connections initiated by other VMs)"
meth public java.lang.String getHost()
meth public java.lang.String getName()
meth public java.lang.String getPort()
meth public java.lang.String getSelectedConnector()
meth public java.lang.String getSelectedDebugger()
meth public java.lang.String getTimeout()
meth public java.lang.String getTransport()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboConnector()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboDebugger()
meth public org.netbeans.jemmy.operators.JLabelOperator lblConnector()
meth public org.netbeans.jemmy.operators.JLabelOperator lblDebugger()
meth public org.netbeans.jemmy.operators.JLabelOperator lblHost()
meth public org.netbeans.jemmy.operators.JLabelOperator lblName()
meth public org.netbeans.jemmy.operators.JLabelOperator lblPort()
meth public org.netbeans.jemmy.operators.JLabelOperator lblTimeout()
meth public org.netbeans.jemmy.operators.JLabelOperator lblTransport()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtHost()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtName()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtPort()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtTimeout()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtTransport()
meth public static org.netbeans.jellytools.modules.debugger.AttachDialogOperator invoke()
meth public void selectConnector(java.lang.String)
meth public void selectDebugger(java.lang.String)
meth public void setHost(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPort(java.lang.String)
meth public void setTimeout(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _cboConnector,_cboDebugger,_lblConnector,_lblDebugger,_lblHost,_lblName,_lblPort,_lblTimeoutMs,_lblTransport,_txtHost,_txtName,_txtPort,_txtTimeoutMs,_txtTransport

CLSS public org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator
cons public init()
meth public static org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator invoke()
meth public void deleteAll()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds invokeAction,viewSubchooser

CLSS public org.netbeans.jellytools.modules.debugger.SessionsOperator
cons public init()
meth public org.netbeans.jellytools.TreeTableOperator treeTable()
meth public static org.netbeans.jellytools.modules.debugger.SessionsOperator invoke()
meth public void finishAll()
meth public void makeCurrent(java.lang.String)
supr org.netbeans.jellytools.TopComponentOperator
hfds invokeAction,viewSubchooser

CLSS public org.netbeans.jellytools.modules.debugger.SourcesOperator
cons public init()
meth public boolean isUsed(java.lang.String)
meth public org.netbeans.jellytools.TreeTableOperator treeTable()
meth public static org.netbeans.jellytools.modules.debugger.SourcesOperator invoke()
meth public void useSource(java.lang.String,boolean)
supr org.netbeans.jellytools.TopComponentOperator
hfds invokeAction,viewSubchooser

CLSS public org.netbeans.jellytools.modules.debugger.actions.ApplyCodeChangesAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.AttachDebuggerAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,menuPathBreakpoints

CLSS public org.netbeans.jellytools.modules.debugger.actions.ContinueAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds DEBUG_ITEM,POPUP_PATH,keystroke

CLSS public org.netbeans.jellytools.modules.debugger.actions.DebugProjectAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds debugProjectPopup

CLSS public org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds popup

CLSS public org.netbeans.jellytools.modules.debugger.actions.FinishAllAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds popupPathFinishAll

CLSS public org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.MakeCurrentAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds popupPathMakeCurrent

CLSS public org.netbeans.jellytools.modules.debugger.actions.NewBreakpointAction
cons public init()
supr org.netbeans.jellytools.actions.ActionNoBlock
hfds keystroke,mainMenuPath,newBreakpointItem,runItem

CLSS public org.netbeans.jellytools.modules.debugger.actions.PauseAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.SessionsAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,menuPathSessions

CLSS public org.netbeans.jellytools.modules.debugger.actions.SourcesAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,menuPathSources

CLSS public org.netbeans.jellytools.modules.debugger.actions.StepIntoAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.StepOutAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.StepOverAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.StepOverExpressionAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds keystroke,mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.TakeGUISnapshotAction
cons public init()
meth public void performMenu()
supr org.netbeans.jellytools.actions.Action
hfds mainMenuPath

CLSS public org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds debugItem,keystroke,mainMenuPath,toggleBreakpointItem

CLSS public org.netbeans.jellytools.modules.form.ComponentInspectorOperator
cons public init()
meth public org.netbeans.jellytools.properties.PropertySheetOperator properties()
meth public org.netbeans.jellytools.properties.PropertySheetOperator properties(java.lang.String)
meth public org.netbeans.jemmy.operators.JTreeOperator treeComponents()
meth public void freezeNavigatorAndRun(java.lang.Runnable)
meth public void performAction(org.netbeans.jellytools.actions.Action,java.lang.String)
meth public void selectComponent(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NavigatorOperator
hfds _properties
hcls FormTreeComponentChooser

CLSS public org.netbeans.jellytools.modules.form.ComponentPaletteOperator
cons public init()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbAWT()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbBeans()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbSwingContainers()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbSwingControls()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbSwingMenus()
meth public org.netbeans.jemmy.operators.JCheckBoxOperator cbSwingWindows()
meth public void collapseAWT()
meth public void collapseBeans()
meth public void collapseSwingContainers()
meth public void collapseSwingControls()
meth public void collapseSwingMenus()
meth public void collapseSwingWindows()
meth public void expandAWT()
meth public void expandBeans()
meth public void expandSwingContainers()
meth public void expandSwingControls()
meth public void expandSwingMenus()
meth public void expandSwingWindows()
meth public void verify()
supr org.netbeans.jellytools.PaletteOperator
hfds _cbAWT,_cbBeans,_cbSwingContainers,_cbSwingControls,_cbSwingMenus,_cbSwingWindows

CLSS public org.netbeans.jellytools.modules.form.FormDesignerOperator
cons public init(java.lang.String)
cons public init(java.lang.String,int)
innr public final static FormDesignerSubchooser
meth public java.awt.Component findComponent(java.lang.Class)
meth public java.awt.Component findComponent(java.lang.Class,int)
meth public java.awt.Component findComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component findComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Point convertCoords(java.awt.Component)
meth public java.awt.Point convertCoords(java.awt.Component,java.awt.Point)
meth public org.netbeans.jellytools.EditorOperator editor()
meth public org.netbeans.jemmy.operators.ComponentOperator handleLayer()
meth public org.netbeans.jemmy.operators.ContainerOperator componentLayer()
meth public org.netbeans.jemmy.operators.ContainerOperator fakePane()
meth public org.netbeans.jemmy.operators.JButtonOperator btPreviewForm()
meth public org.netbeans.jemmy.operators.JFrameOperator previewForm()
meth public org.netbeans.jemmy.operators.JFrameOperator previewForm(java.lang.String)
meth public org.netbeans.jemmy.operators.JToggleButtonOperator tbConnectionMode()
meth public org.netbeans.jemmy.operators.JToggleButtonOperator tbDesign()
meth public org.netbeans.jemmy.operators.JToggleButtonOperator tbSelectionMode()
meth public org.netbeans.jemmy.operators.JToggleButtonOperator tbSource()
meth public void clickOnComponent(java.awt.Component)
meth public void clickOnComponent(java.awt.Component,java.awt.Point)
meth public void connectionMode()
meth public void design()
meth public void selectionMode()
meth public void source()
meth public void verify()
supr org.netbeans.jellytools.TopComponentOperator
hfds _btPreviewForm,_componentLayer,_fakePane,_handleLayer,_tbConnectionMode,_tbDesign,_tbSelectionMode,_tbSource
hcls ComponentLayerChooser,FakePaneChooser,HandleLayerChooser,ToolTipChooser

CLSS public final static org.netbeans.jellytools.modules.form.FormDesignerOperator$FormDesignerSubchooser
 outer org.netbeans.jellytools.modules.form.FormDesignerOperator
cons public init()
intf org.netbeans.jemmy.ComponentChooser
meth public boolean checkComponent(java.awt.Component)
meth public java.lang.String getDescription()
supr java.lang.Object

CLSS public org.netbeans.jellytools.modules.form.actions.InspectorAction
 anno 0 java.lang.Deprecated()
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds inspectorMenu

CLSS public org.netbeans.jellytools.modules.form.properties.editors.FormCustomEditorOperator
cons public init(java.lang.String)
meth public java.lang.String getMode()
meth public org.netbeans.jemmy.operators.JButtonOperator btDefault()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboMode()
meth public void setDefault()
meth public void setMode(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _btDefault,_cboMode

CLSS public org.netbeans.jellytools.modules.form.properties.editors.MethodPickerOperator
cons public init()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboComponent()
meth public org.netbeans.jemmy.operators.JLabelOperator lblComponent()
meth public org.netbeans.jemmy.operators.JLabelOperator lblMethods()
meth public org.netbeans.jemmy.operators.JListOperator lstMethods()
meth public void setComponent(java.lang.String)
meth public void setMethods(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _cboComponent,_lblComponent,_lblMethods,_lstMethods

CLSS public org.netbeans.jellytools.modules.form.properties.editors.ParametersPickerOperator
cons public init(java.lang.String)
meth public org.netbeans.jemmy.operators.JButtonOperator btSelectMethod()
meth public org.netbeans.jemmy.operators.JButtonOperator btSelectProperty()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboComponent()
meth public org.netbeans.jemmy.operators.JLabelOperator lblGetParameterFrom()
meth public org.netbeans.jemmy.operators.JRadioButtonOperator rbComponent()
meth public org.netbeans.jemmy.operators.JRadioButtonOperator rbMethodCall()
meth public org.netbeans.jemmy.operators.JRadioButtonOperator rbProperty()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtMethodCall()
meth public org.netbeans.jemmy.operators.JTextFieldOperator txtProperty()
meth public void component()
meth public void methodCall()
meth public void property()
meth public void selectMethod()
meth public void selectProperty()
meth public void setComponent(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.modules.form.properties.editors.FormCustomEditorOperator
hfds _btSelectMethod,_btSelectProperty,_cboComponent,_lblGetParameterFrom,_rbComponent,_rbMethodCall,_rbProperty,_rbUserCode,_txtMethodCall,_txtProperty,_txtUserCode

CLSS public org.netbeans.jellytools.modules.form.properties.editors.PropertyPickerOperator
cons public init()
meth public org.netbeans.jemmy.operators.JComboBoxOperator cboComponent()
meth public org.netbeans.jemmy.operators.JLabelOperator lblComponent()
meth public org.netbeans.jemmy.operators.JLabelOperator lblProperties()
meth public org.netbeans.jemmy.operators.JListOperator lstProperties()
meth public void setComponent(java.lang.String)
meth public void setProperty(java.lang.String)
meth public void verify()
supr org.netbeans.jellytools.NbDialogOperator
hfds _cboComponent,_lblComponent,_lblProperties,_lstProperties

CLSS public org.netbeans.jellytools.modules.freeform.actions.RedeployFreeformAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds redeployProject

CLSS public org.netbeans.jellytools.modules.freeform.actions.RunFreeformAction
cons public init()
supr org.netbeans.jellytools.actions.Action
hfds runProject

CLSS public org.netbeans.jellytools.modules.freeform.nodes.FreeformProjectNode
cons public init(java.lang.String)
meth public void customAction(java.lang.String)
meth public void redeploy()
meth public void run()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.ProjectRootNode
hfds redeployFreeformAction,runFreeformAction

CLSS public org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator
cons public init()
fld public final static java.lang.String DELEGATE_METHOD
fld public final static java.lang.String GENERATE_CONSTRUCTOR
fld public final static java.lang.String GENERATE_EQUALS
fld public final static java.lang.String GENERATE_EQUALS_HASHCODE
fld public final static java.lang.String GENERATE_GETTER
fld public final static java.lang.String GENERATE_GETTER_SETTER
fld public final static java.lang.String GENERATE_HASHCODE
fld public final static java.lang.String GENERATE_SETTER
fld public final static java.lang.String IMPLEMENT_METHOD
fld public final static java.lang.String OVERRIDE_METHOD
meth public !varargs static boolean checkItems(org.netbeans.jellytools.EditorOperator,java.lang.String[])
meth public !varargs static boolean containsItems(org.netbeans.jellytools.EditorOperator,java.lang.String[])
meth public static boolean openDialog(java.lang.String,org.netbeans.jellytools.EditorOperator)
supr java.lang.Object

CLSS public org.netbeans.jellytools.nodes.ClassNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void properties()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds propertiesAction

CLSS public org.netbeans.jellytools.nodes.FolderNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void compile()
meth public void copy()
meth public void cut()
meth public void delete()
meth public void exploreFromHere()
meth public void find()
meth public void newFile()
meth public void newFile(java.lang.String)
meth public void paste()
meth public void properties()
meth public void rename()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds compileAction,copyAction,cutAction,deleteAction,exploreFromHereAction,findAction,newFileAction,pasteAction,propertiesAction,renameAction

CLSS public org.netbeans.jellytools.nodes.FormNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void compile()
meth public void copy()
meth public void cut()
meth public void delete()
meth public void edit()
meth public void open()
meth public void paste()
meth public void properties()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds compileAction,copyAction,cutAction,deleteAction,editAction,openAction,pasteAction,propertiesAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.nodes.HTMLNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void copy()
meth public void cut()
meth public void delete()
meth public void open()
meth public void paste()
meth public void properties()
meth public void rename()
meth public void saveAsTemplate()
meth public void verifyPopup()
meth public void view()
supr org.netbeans.jellytools.nodes.Node
hfds copyAction,cutAction,deleteAction,openAction,pasteAction,propertiesAction,saveAsTemplateAction,viewAction

CLSS public org.netbeans.jellytools.nodes.ImageNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void copy()
meth public void cut()
meth public void delete()
meth public void open()
meth public void paste()
meth public void properties()
meth public void rename()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds copyAction,cutAction,deleteAction,openAction,pasteAction,propertiesAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.nodes.JavaNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
meth public void copy()
meth public void cut()
meth public void delete()
meth public void open()
meth public void paste()
meth public void properties()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds copyAction,cutAction,deleteAction,openAction,pasteAction,propertiesAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.nodes.JavaProjectRootNode
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
meth public void buildProject()
meth public void cleanProject()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.ProjectRootNode
hfds buildProjectAction,cleanProjectAction

CLSS public org.netbeans.jellytools.nodes.Node
cons public init(org.netbeans.jellytools.nodes.Node,int)
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
fld protected java.lang.String stringPath
fld protected javax.swing.tree.TreePath treePath
fld protected org.netbeans.jemmy.operators.JTreeOperator treeOperator
meth public boolean isChildPresent(java.lang.String)
meth public boolean isCollapsed()
meth public boolean isExpanded()
meth public boolean isLeaf()
meth public boolean isLink()
meth public boolean isPresent()
meth public java.lang.Object getOpenideNode()
meth public java.lang.String getParentPath()
meth public java.lang.String getPath()
meth public java.lang.String getText()
meth public java.lang.String[] getChildren()
meth public javax.swing.tree.TreePath getTreePath()
meth public org.netbeans.jemmy.operators.JPopupMenuOperator callPopup()
meth public org.netbeans.jemmy.operators.JTreeOperator tree()
meth public org.netbeans.jemmy.operators.Operator$StringComparator getComparator()
meth public void addSelectionPath()
meth public void collapse()
meth public void expand()
meth public void performAPIAction(java.lang.String)
meth public void performAPIActionNoBlock(java.lang.String)
meth public void performMenuAction(java.lang.String)
meth public void performMenuActionNoBlock(java.lang.String)
meth public void performPopupAction(java.lang.String)
meth public void performPopupActionNoBlock(java.lang.String)
meth public void select()
meth public void setComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void verifyPopup(java.lang.String)
meth public void verifyPopup(java.lang.String[])
meth public void verifyPopup(org.netbeans.jellytools.actions.Action[])
meth public void waitChildNotPresent(java.lang.String)
meth public void waitCollapsed()
meth public void waitExpanded()
meth public void waitNotPresent()
supr java.lang.Object
hfds comparator,linkSuffix
hcls NodesJTreeOperator,StringArraySubPathChooser

CLSS public final org.netbeans.jellytools.nodes.OutlineNode
cons public init(org.netbeans.jellytools.OutlineOperator,java.lang.String)
cons public init(org.netbeans.jellytools.OutlineOperator,javax.swing.tree.TreePath)
cons public init(org.netbeans.jellytools.nodes.OutlineNode,java.lang.String)
meth public boolean isLeaf()
meth public javax.swing.tree.TreePath getTreePath()
meth public org.netbeans.jellytools.OutlineOperator getOutline()
meth public org.netbeans.jemmy.operators.JPopupMenuOperator callPopup()
meth public void expand()
meth public void performAPIAction(java.lang.String)
meth public void performAPIActionNoBlock(java.lang.String)
meth public void performMenuAction(java.lang.String)
meth public void performMenuActionNoBlock(java.lang.String)
meth public void performPopupAction(java.lang.String)
meth public void performPopupActionNoBlock(java.lang.String)
meth public void select()
supr java.lang.Object
hfds _outline,_treePath

CLSS public org.netbeans.jellytools.nodes.ProjectRootNode
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
meth public void debug()
meth public void find()
meth public void properties()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds debugProjectAction,findAction,propertiesAction

CLSS public org.netbeans.jellytools.nodes.PropertiesNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void addLocale()
meth public void copy()
meth public void customize()
meth public void cut()
meth public void delete()
meth public void edit()
meth public void open()
meth public void paste()
meth public void properties()
meth public void rename()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds addLocaleAction,copyAction,customizeAction,cutAction,deleteAction,editAction,openAction,pasteAction,propertiesAction,renameAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.nodes.SourcePackagesNode
cons public init(java.lang.String)
cons public init(org.netbeans.jellytools.nodes.Node)
meth public void exploreFromHere()
meth public void find()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds SOURCE_PACKAGES_LABEL,exploreFromHereAction,findAction

CLSS public org.netbeans.jellytools.nodes.URLNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void copy()
meth public void cut()
meth public void delete()
meth public void open()
meth public void paste()
meth public void properties()
meth public void rename()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds copyAction,cutAction,deleteAction,openAction,pasteAction,propertiesAction,renameAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.nodes.UnrecognizedNode
cons public init(org.netbeans.jellytools.nodes.Node,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.JTreeOperator,javax.swing.tree.TreePath)
meth public void copy()
meth public void cut()
meth public void delete()
meth public void open()
meth public void paste()
meth public void properties()
meth public void rename()
meth public void saveAsTemplate()
meth public void verifyPopup()
supr org.netbeans.jellytools.nodes.Node
hfds copyAction,cutAction,deleteAction,openAction,pasteAction,propertiesAction,renameAction,saveAsTemplateAction

CLSS public org.netbeans.jellytools.testutils.JavaNodeUtils
cons public init()
meth public static void closeSafeDeleteDialog()
meth public static void performSafeDelete(org.netbeans.jellytools.nodes.Node)
supr org.netbeans.jellytools.testutils.NodeUtils

CLSS public org.netbeans.jellytools.testutils.NodeUtils
cons public init()
meth public static void closeConfirmDeleteDialog()
meth public static void closeProperties(java.lang.String)
meth public static void closeRenameDialog()
meth public static void performDelete(org.netbeans.jellytools.nodes.Node)
meth public static void testClipboard(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.netbeans.jemmy.ComponentChooser
meth public abstract boolean checkComponent(java.awt.Component)
meth public abstract java.lang.String getDescription()

CLSS public abstract interface org.netbeans.jemmy.Outputable
meth public abstract org.netbeans.jemmy.TestOut getOutput()
meth public abstract void setOutput(org.netbeans.jemmy.TestOut)

CLSS public abstract interface org.netbeans.jemmy.Timeoutable
meth public abstract org.netbeans.jemmy.Timeouts getTimeouts()
meth public abstract void setTimeouts(org.netbeans.jemmy.Timeouts)

CLSS public abstract interface org.netbeans.jemmy.drivers.Driver
meth public abstract java.lang.Class[] getSupported()

CLSS public abstract interface org.netbeans.jemmy.drivers.MouseDriver
meth public abstract void clickMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,org.netbeans.jemmy.Timeout)
meth public abstract void dragMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public abstract void dragNDrop(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int,int,int,org.netbeans.jemmy.Timeout,org.netbeans.jemmy.Timeout)
meth public abstract void enterMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void exitMouse(org.netbeans.jemmy.operators.ComponentOperator)
meth public abstract void moveMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int)
meth public abstract void pressMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)
meth public abstract void releaseMouse(org.netbeans.jemmy.operators.ComponentOperator,int,int,int,int)

CLSS public abstract org.netbeans.jemmy.drivers.SupportiveDriver
cons public init(java.lang.Class[])
intf org.netbeans.jemmy.drivers.Driver
meth public java.lang.Class[] getSupported()
meth public void checkSupported(org.netbeans.jemmy.operators.ComponentOperator)
supr java.lang.Object
hfds supported

CLSS public org.netbeans.jemmy.operators.ComponentOperator
cons public init(java.awt.Component)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String HEIGHT_DPROP = "Height"
fld public final static java.lang.String IS_SHOWING_DPROP = "Showing"
fld public final static java.lang.String IS_VISIBLE_DPROP = "Visible"
fld public final static java.lang.String NAME_DPROP = "Name:"
fld public final static java.lang.String WIDTH_DPROP = "Width"
fld public final static java.lang.String X_DPROP = "X"
fld public final static java.lang.String Y_DPROP = "Y"
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Component waitComponent(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean contains(int,int)
meth public boolean contains(java.awt.Point)
meth public boolean hasFocus()
meth public boolean imageUpdate(java.awt.Image,int,int,int,int,int)
meth public boolean isDisplayable()
meth public boolean isDoubleBuffered()
meth public boolean isEnabled()
meth public boolean isFocusTraversable()
meth public boolean isLightweight()
meth public boolean isOpaque()
meth public boolean isShowing()
meth public boolean isValid()
meth public boolean isVisible()
meth public boolean prepareImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public boolean prepareImage(java.awt.Image,java.awt.image.ImageObserver)
meth public float getAlignmentX()
meth public float getAlignmentY()
meth public int checkImage(java.awt.Image,int,int,java.awt.image.ImageObserver)
meth public int checkImage(java.awt.Image,java.awt.image.ImageObserver)
meth public int getCenterX()
meth public int getCenterXForClick()
meth public int getCenterY()
meth public int getCenterYForClick()
meth public int getHeight()
meth public int getWidth()
meth public int getX()
meth public int getY()
meth public java.awt.Color getBackground()
meth public java.awt.Color getForeground()
meth public java.awt.Component getComponentAt(int,int)
meth public java.awt.Component getComponentAt(java.awt.Point)
meth public java.awt.Component getSource()
meth public java.awt.ComponentOrientation getComponentOrientation()
meth public java.awt.Container getContainer(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Container getParent()
meth public java.awt.Container[] getContainers()
meth public java.awt.Cursor getCursor()
meth public java.awt.Dimension getMaximumSize()
meth public java.awt.Dimension getMinimumSize()
meth public java.awt.Dimension getPreferredSize()
meth public java.awt.Dimension getSize()
meth public java.awt.Dimension getSize(java.awt.Dimension)
meth public java.awt.Font getFont()
meth public java.awt.FontMetrics getFontMetrics(java.awt.Font)
meth public java.awt.Graphics getGraphics()
meth public java.awt.Image createImage(int,int)
meth public java.awt.Image createImage(java.awt.image.ImageProducer)
meth public java.awt.Point getLocation()
meth public java.awt.Point getLocation(java.awt.Point)
meth public java.awt.Point getLocationOnScreen()
meth public java.awt.Rectangle getBounds()
meth public java.awt.Rectangle getBounds(java.awt.Rectangle)
meth public java.awt.Toolkit getToolkit()
meth public java.awt.Window getWindow()
meth public java.awt.dnd.DropTarget getDropTarget()
meth public java.awt.im.InputContext getInputContext()
meth public java.awt.im.InputMethodRequests getInputMethodRequests()
meth public java.awt.image.ColorModel getColorModel()
meth public java.lang.Object getTreeLock()
meth public java.lang.String getName()
meth public java.util.Hashtable getDump()
meth public java.util.Locale getLocale()
meth public org.netbeans.jemmy.EventDispatcher getEventDispatcher()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static java.awt.Component findComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Component findComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Component waitComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Component[] findComponents(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public void activateWindow()
meth public void add(java.awt.PopupMenu)
meth public void addComponentListener(java.awt.event.ComponentListener)
meth public void addFocusListener(java.awt.event.FocusListener)
meth public void addInputMethodListener(java.awt.event.InputMethodListener)
meth public void addKeyListener(java.awt.event.KeyListener)
meth public void addMouseListener(java.awt.event.MouseListener)
meth public void addMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void addNotify()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void clickForPopup()
meth public void clickForPopup(int)
meth public void clickForPopup(int,int)
meth public void clickForPopup(int,int,int)
meth public void clickMouse()
meth public void clickMouse(int)
meth public void clickMouse(int,int)
meth public void clickMouse(int,int,int)
meth public void clickMouse(int,int,int,int)
meth public void clickMouse(int,int,int,int,int)
meth public void clickMouse(int,int,int,int,int,boolean)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void dispatchEvent(java.awt.AWTEvent)
meth public void doLayout()
meth public void dragMouse(int,int)
meth public void dragMouse(int,int,int)
meth public void dragMouse(int,int,int,int)
meth public void dragNDrop(int,int,int,int)
meth public void dragNDrop(int,int,int,int,int)
meth public void dragNDrop(int,int,int,int,int,int)
meth public void enableInputMethods(boolean)
meth public void enterMouse()
meth public void exitMouse()
meth public void getFocus()
meth public void invalidate()
meth public void list()
meth public void list(java.io.PrintStream)
meth public void list(java.io.PrintStream,int)
meth public void list(java.io.PrintWriter)
meth public void list(java.io.PrintWriter,int)
meth public void makeComponentVisible()
meth public void moveMouse(int,int)
meth public void paint(java.awt.Graphics)
meth public void paintAll(java.awt.Graphics)
meth public void pressKey(int)
meth public void pressKey(int,int)
meth public void pressMouse()
meth public void pressMouse(int,int)
meth public void print(java.awt.Graphics)
meth public void printAll(java.awt.Graphics)
meth public void pushKey(int)
meth public void pushKey(int,int)
meth public void releaseKey(int)
meth public void releaseKey(int,int)
meth public void releaseMouse()
meth public void releaseMouse(int,int)
meth public void remove(java.awt.MenuComponent)
meth public void removeComponentListener(java.awt.event.ComponentListener)
meth public void removeFocusListener(java.awt.event.FocusListener)
meth public void removeInputMethodListener(java.awt.event.InputMethodListener)
meth public void removeKeyListener(java.awt.event.KeyListener)
meth public void removeMouseListener(java.awt.event.MouseListener)
meth public void removeMouseMotionListener(java.awt.event.MouseMotionListener)
meth public void removeNotify()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void repaint()
meth public void repaint(int,int,int,int)
meth public void repaint(long)
meth public void repaint(long,int,int,int,int)
meth public void requestFocus()
meth public void setBackground(java.awt.Color)
meth public void setBounds(int,int,int,int)
meth public void setBounds(java.awt.Rectangle)
meth public void setComponentOrientation(java.awt.ComponentOrientation)
meth public void setCursor(java.awt.Cursor)
meth public void setDropTarget(java.awt.dnd.DropTarget)
meth public void setEnabled(boolean)
meth public void setFont(java.awt.Font)
meth public void setForeground(java.awt.Color)
meth public void setLocale(java.util.Locale)
meth public void setLocation(int,int)
meth public void setLocation(java.awt.Point)
meth public void setName(java.lang.String)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setSize(int,int)
meth public void setSize(java.awt.Dimension)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setVisible(boolean)
meth public void transferFocus()
meth public void typeKey(char)
meth public void typeKey(char,int)
meth public void typeKey(int,char,int)
meth public void typedKey(char,int)
meth public void update(java.awt.Graphics)
meth public void validate()
meth public void waitComponentEnabled() throws java.lang.InterruptedException
meth public void waitComponentShowing(boolean)
meth public void waitComponentVisible(boolean)
meth public void waitHasFocus()
meth public void wtComponentEnabled()
supr org.netbeans.jemmy.operators.Operator
hfds AFTER_DRAG_TIMEOUT,BEFORE_DRAG_TIMEOUT,MOUSE_CLICK_TIMEOUT,PUSH_KEY_TIMEOUT,WAIT_COMPONENT_ENABLED_TIMEOUT,WAIT_COMPONENT_TIMEOUT,WAIT_FOCUS_TIMEOUT,WAIT_STATE_TIMEOUT,dispatcher,fDriver,kDriver,mDriver,output,source,timeouts
hcls VisibleComponentFinder

CLSS public org.netbeans.jemmy.operators.ContainerOperator<%0 extends java.awt.Container>
cons public init(java.awt.Container)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static ContainerFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean isAncestorOf(java.awt.Component)
meth public int getComponentCount()
meth public java.awt.Component add(java.awt.Component)
meth public java.awt.Component add(java.awt.Component,int)
meth public java.awt.Component add(java.lang.String,java.awt.Component)
meth public java.awt.Component findComponentAt(int,int)
meth public java.awt.Component findComponentAt(java.awt.Point)
meth public java.awt.Component findSubComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component findSubComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Component getComponent(int)
meth public java.awt.Component waitSubComponent(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Component waitSubComponent(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Component[] getComponents()
meth public java.awt.Insets getInsets()
meth public java.awt.LayoutManager getLayout()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ComponentOperator createSubOperator(org.netbeans.jemmy.ComponentChooser)
meth public org.netbeans.jemmy.operators.ComponentOperator createSubOperator(org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Container findContainer(java.awt.Container)
meth public static java.awt.Container findContainer(java.awt.Container,int)
meth public static java.awt.Container findContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container findContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Container findContainerUnder(java.awt.Component)
meth public static java.awt.Container findContainerUnder(java.awt.Component,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container waitContainer(java.awt.Container)
meth public static java.awt.Container waitContainer(java.awt.Container,int)
meth public static java.awt.Container waitContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Container waitContainer(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void add(java.awt.Component,java.lang.Object)
meth public void add(java.awt.Component,java.lang.Object,int)
meth public void addContainerListener(java.awt.event.ContainerListener)
meth public void paintComponents(java.awt.Graphics)
meth public void printComponents(java.awt.Graphics)
meth public void remove(int)
meth public void remove(java.awt.Component)
meth public void removeAll()
meth public void removeContainerListener(java.awt.event.ContainerListener)
meth public void setLayout(java.awt.LayoutManager)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
supr org.netbeans.jemmy.operators.ComponentOperator
hfds WAIT_SUBCOMPONENT_TIMEOUT,output,searcher,timeouts

CLSS public org.netbeans.jemmy.operators.DialogOperator
cons public init()
cons public init(int)
cons public init(java.awt.Dialog)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String IS_MODAL_DPROP = "Modal"
fld public final static java.lang.String IS_RESIZABLE_DPROP = "Resizable"
fld public final static java.lang.String TITLE_DPROP = "Title"
innr public static DialogByTitleFinder
innr public static DialogFinder
meth protected static java.awt.Dialog waitDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Dialog waitDialog(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Dialog waitDialog(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean isModal()
meth public boolean isResizable()
meth public java.lang.String getTitle()
meth public java.util.Hashtable getDump()
meth public void setModal(boolean)
meth public void setResizable(boolean)
meth public void setTitle(java.lang.String)
meth public void waitTitle(java.lang.String)
supr org.netbeans.jemmy.operators.WindowOperator

CLSS public org.netbeans.jemmy.operators.FrameOperator
cons public init()
cons public init(int)
cons public init(java.awt.Frame)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
fld public final static java.lang.String IS_RESIZABLE_DPROP = "Resizable"
fld public final static java.lang.String STATE_DPROP = "State"
fld public final static java.lang.String STATE_ICONIFIED_DPROP_VALUE = "ICONIFIED"
fld public final static java.lang.String STATE_NORMAL_DPROP_VALUE = "NORMAL"
fld public final static java.lang.String TITLE_DPROP = "Title"
innr public static FrameByTitleFinder
innr public static FrameFinder
intf org.netbeans.jemmy.Outputable
meth protected static java.awt.Frame waitFrame(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth public boolean isResizable()
meth public int getState()
meth public java.awt.Image getIconImage()
meth public java.awt.MenuBar getMenuBar()
meth public java.lang.String getTitle()
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void deiconify()
meth public void demaximize()
meth public void iconify()
meth public void maximize()
meth public void setIconImage(java.awt.Image)
meth public void setMenuBar(java.awt.MenuBar)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setResizable(boolean)
meth public void setState(int)
meth public void setTitle(java.lang.String)
meth public void waitState(int)
meth public void waitTitle(java.lang.String)
supr org.netbeans.jemmy.operators.WindowOperator
hfds driver,output

CLSS public org.netbeans.jemmy.operators.JComponentOperator
cons public init(javax.swing.JComponent)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String A11Y_DATA = "Accessible data (yes/no)"
fld public final static java.lang.String A11Y_DESCRIPTION_DPROP = "Accessible decription"
fld public final static java.lang.String A11Y_NAME_DPROP = "Accessible name"
fld public final static java.lang.String TOOLTIP_TEXT_DPROP = "Tooltip text"
innr public static JComponentByTipFinder
innr public static JComponentFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getAutoscrolls()
meth public boolean isFocusCycleRoot()
meth public boolean isManagingFocus()
meth public boolean isOptimizedDrawingEnabled()
meth public boolean isPaintingTile()
meth public boolean isRequestFocusEnabled()
meth public boolean isValidateRoot()
meth public boolean requestDefaultFocus()
meth public int getCenterXForClick()
meth public int getCenterYForClick()
meth public int getConditionForKeyStroke(javax.swing.KeyStroke)
meth public int getDebugGraphicsOptions()
meth public java.awt.Component getNextFocusableComponent()
meth public java.awt.Container getTopLevelAncestor()
meth public java.awt.Insets getInsets(java.awt.Insets)
meth public java.awt.Point getToolTipLocation(java.awt.event.MouseEvent)
meth public java.awt.Rectangle getVisibleRect()
meth public java.awt.event.ActionListener getActionForKeyStroke(javax.swing.KeyStroke)
meth public java.lang.Object getClientProperty(java.lang.Object)
meth public java.lang.String getToolTipText()
meth public java.lang.String getToolTipText(java.awt.event.MouseEvent)
meth public java.lang.String getUIClassID()
meth public java.util.Hashtable getDump()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JRootPane getRootPane()
meth public javax.swing.JToolTip createToolTip()
meth public javax.swing.JToolTip showToolTip()
meth public javax.swing.JToolTip waitToolTip()
meth public javax.swing.KeyStroke[] getRegisteredKeyStrokes()
meth public javax.swing.border.Border getBorder()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.ContainerOperator getWindowContainerOperator()
meth public static javax.swing.JComponent findJComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComponent findJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,java.lang.String,boolean,boolean)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JComponent waitJComponent(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addAncestorListener(javax.swing.event.AncestorListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void computeVisibleRect(java.awt.Rectangle)
meth public void firePropertyChange(java.lang.String,boolean,boolean)
meth public void firePropertyChange(java.lang.String,byte,byte)
meth public void firePropertyChange(java.lang.String,char,char)
meth public void firePropertyChange(java.lang.String,double,double)
meth public void firePropertyChange(java.lang.String,float,float)
meth public void firePropertyChange(java.lang.String,int,int)
meth public void firePropertyChange(java.lang.String,long,long)
meth public void firePropertyChange(java.lang.String,short,short)
meth public void grabFocus()
meth public void paintImmediately(int,int,int,int)
meth public void paintImmediately(java.awt.Rectangle)
meth public void putClientProperty(java.lang.Object,java.lang.Object)
meth public void registerKeyboardAction(java.awt.event.ActionListener,java.lang.String,javax.swing.KeyStroke,int)
meth public void registerKeyboardAction(java.awt.event.ActionListener,javax.swing.KeyStroke,int)
meth public void removeAncestorListener(javax.swing.event.AncestorListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void repaint(java.awt.Rectangle)
meth public void resetKeyboardActions()
meth public void revalidate()
meth public void scrollRectToVisible(java.awt.Rectangle)
meth public void setAlignmentX(float)
meth public void setAlignmentY(float)
meth public void setAutoscrolls(boolean)
meth public void setBorder(javax.swing.border.Border)
meth public void setDebugGraphicsOptions(int)
meth public void setDoubleBuffered(boolean)
meth public void setMaximumSize(java.awt.Dimension)
meth public void setMinimumSize(java.awt.Dimension)
meth public void setNextFocusableComponent(java.awt.Component)
meth public void setOpaque(boolean)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPreferredSize(java.awt.Dimension)
meth public void setRequestFocusEnabled(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setToolTipText(java.lang.String)
meth public void unregisterKeyboardAction(javax.swing.KeyStroke)
meth public void updateUI()
supr org.netbeans.jemmy.operators.ContainerOperator
hfds SHOW_TOOL_TIP_TIMEOUT,WAIT_TOOL_TIP_TIMEOUT,output,timeouts
hcls JToolTipFinder,JToolTipWindowFinder

CLSS public org.netbeans.jemmy.operators.JDialogOperator
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(javax.swing.JDialog)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.WindowOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
innr public static JDialogFinder
meth protected static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public static java.awt.Dialog getTopModalDialog()
meth public static javax.swing.JDialog findJDialog(java.awt.Window,java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog findJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog findJDialog(java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog findJDialog(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog findJDialog(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog findJDialog(org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog waitJDialog(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JDialog waitJDialog(java.lang.String,boolean,boolean)
meth public static javax.swing.JDialog waitJDialog(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JDialog waitJDialog(org.netbeans.jemmy.ComponentChooser,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
meth public void setLocationRelativeTo(java.awt.Component)
supr org.netbeans.jemmy.operators.DialogOperator

CLSS public org.netbeans.jemmy.operators.JFrameOperator
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,org.netbeans.jemmy.operators.Operator)
cons public init(javax.swing.JFrame)
cons public init(org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.ComponentChooser,int)
cons public init(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.operators.Operator)
innr public static JFrameFinder
meth public int getDefaultCloseOperation()
meth public java.awt.Component getGlassPane()
meth public java.awt.Container getContentPane()
meth public javax.accessibility.AccessibleContext getAccessibleContext()
meth public javax.swing.JLayeredPane getLayeredPane()
meth public javax.swing.JMenuBar getJMenuBar()
meth public javax.swing.JRootPane getRootPane()
meth public static javax.swing.JFrame findJFrame(java.lang.String,boolean,boolean)
meth public static javax.swing.JFrame findJFrame(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JFrame findJFrame(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JFrame findJFrame(org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JFrame waitJFrame(java.lang.String,boolean,boolean)
meth public static javax.swing.JFrame waitJFrame(java.lang.String,boolean,boolean,int)
meth public static javax.swing.JFrame waitJFrame(org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JFrame waitJFrame(org.netbeans.jemmy.ComponentChooser,int)
meth public void setContentPane(java.awt.Container)
meth public void setDefaultCloseOperation(int)
meth public void setGlassPane(java.awt.Component)
meth public void setJMenuBar(javax.swing.JMenuBar)
meth public void setLayeredPane(javax.swing.JLayeredPane)
supr org.netbeans.jemmy.operators.FrameOperator

CLSS public org.netbeans.jemmy.operators.JTableOperator
cons public init(javax.swing.JTable)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String CELL_PREFIX_DPROP = "Cell"
fld public final static java.lang.String COLUMN_COUNT_DPROP = "Column count"
fld public final static java.lang.String COLUMN_PREFIX_DPROP = "Column"
fld public final static java.lang.String ROW_COUNT_DPROP = "Row count"
fld public final static java.lang.String SELECTED_COLUMN_PREFIX_DPROP = "SelectedColumn"
fld public final static java.lang.String SELECTED_ROW_PREFIX_DPROP = "SelectedRow"
innr public abstract interface static TableCellChooser
innr public static JTableByCellFinder
innr public static JTableFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean editCellAt(int,int)
meth public boolean editCellAt(int,int,java.util.EventObject)
meth public boolean getAutoCreateColumnsFromModel()
meth public boolean getCellSelectionEnabled()
meth public boolean getColumnSelectionAllowed()
meth public boolean getRowSelectionAllowed()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getShowHorizontalLines()
meth public boolean getShowVerticalLines()
meth public boolean isCellEditable(int,int)
meth public boolean isCellSelected(int,int)
meth public boolean isColumnSelected(int)
meth public boolean isEditing()
meth public boolean isRowSelected(int)
meth public int columnAtPoint(java.awt.Point)
meth public int convertColumnIndexToModel(int)
meth public int convertColumnIndexToView(int)
meth public int findCellColumn(java.lang.String)
meth public int findCellColumn(java.lang.String,boolean,boolean)
meth public int findCellColumn(java.lang.String,boolean,boolean,int)
meth public int findCellColumn(java.lang.String,int)
meth public int findCellColumn(java.lang.String,int,int)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findCellColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int,int)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser,int)
meth public int findCellColumn(org.netbeans.jemmy.ComponentChooser,int,int)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public int findCellColumn(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int,int)
meth public int findCellRow(java.lang.String)
meth public int findCellRow(java.lang.String,boolean,boolean)
meth public int findCellRow(java.lang.String,boolean,boolean,int)
meth public int findCellRow(java.lang.String,int)
meth public int findCellRow(java.lang.String,int,int)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findCellRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int,int)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser,int)
meth public int findCellRow(org.netbeans.jemmy.ComponentChooser,int,int)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public int findCellRow(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int,int)
meth public int findColumn(java.lang.String)
meth public int findColumn(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int getAutoResizeMode()
meth public int getColumnCount()
meth public int getEditingColumn()
meth public int getEditingRow()
meth public int getRowCount()
meth public int getRowHeight()
meth public int getRowMargin()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectedColumn()
meth public int getSelectedColumnCount()
meth public int getSelectedRow()
meth public int getSelectedRowCount()
meth public int rowAtPoint(java.awt.Point)
meth public int[] getSelectedColumns()
meth public int[] getSelectedRows()
meth public java.awt.Color getGridColor()
meth public java.awt.Color getSelectionBackground()
meth public java.awt.Color getSelectionForeground()
meth public java.awt.Component getEditorComponent()
meth public java.awt.Component getRenderedComponent(int,int)
meth public java.awt.Component getRenderedComponent(int,int,boolean,boolean)
meth public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor,int,int)
meth public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer,int,int)
meth public java.awt.Component waitCellComponent(org.netbeans.jemmy.ComponentChooser,int,int)
meth public java.awt.Dimension getIntercellSpacing()
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point findCell(java.lang.String,int)
meth public java.awt.Point findCell(java.lang.String,int[],int[],int)
meth public java.awt.Point findCell(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public java.awt.Point findCell(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int[],int[],int)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Point findCell(org.netbeans.jemmy.ComponentChooser,int[],int[],int)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int)
meth public java.awt.Point findCell(org.netbeans.jemmy.operators.JTableOperator$TableCellChooser,int[],int[],int)
meth public java.awt.Point getPointToClick(int,int)
meth public java.awt.Rectangle getCellRect(int,int,boolean)
meth public java.lang.Class getColumnClass(int)
meth public java.lang.Object getValueAt(int,int)
meth public java.lang.String getColumnName(int)
meth public java.util.Hashtable getDump()
meth public javax.swing.JPopupMenu callPopupOnCell(int,int)
meth public javax.swing.ListSelectionModel getSelectionModel()
meth public javax.swing.plaf.TableUI getUI()
meth public javax.swing.table.JTableHeader getTableHeader()
meth public javax.swing.table.TableCellEditor getCellEditor()
meth public javax.swing.table.TableCellEditor getCellEditor(int,int)
meth public javax.swing.table.TableCellEditor getDefaultEditor(java.lang.Class)
meth public javax.swing.table.TableCellRenderer getCellRenderer(int,int)
meth public javax.swing.table.TableCellRenderer getDefaultRenderer(java.lang.Class)
meth public javax.swing.table.TableColumn getColumn(java.lang.Object)
meth public javax.swing.table.TableColumnModel getColumnModel()
meth public javax.swing.table.TableModel getModel()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.JTableHeaderOperator getHeaderOperator()
meth public static javax.swing.JTable findJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTable findJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int,int)
meth public static javax.swing.JTable findJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTable findJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,java.lang.String,boolean,boolean,int,int,int)
meth public static javax.swing.JTable waitJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTable waitJTable(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addColumn(javax.swing.table.TableColumn)
meth public void addColumnSelectionInterval(int,int)
meth public void addRowSelectionInterval(int,int)
meth public void changeCellObject(int,int,java.lang.Object)
meth public void changeCellText(int,int,java.lang.String)
meth public void clearSelection()
meth public void clickForEdit(int,int)
meth public void clickOnCell(int,int)
meth public void clickOnCell(int,int,int)
meth public void clickOnCell(int,int,int,int)
meth public void clickOnCell(int,int,int,int,int)
meth public void columnAdded(javax.swing.event.TableColumnModelEvent)
meth public void columnMarginChanged(javax.swing.event.ChangeEvent)
meth public void columnMoved(javax.swing.event.TableColumnModelEvent)
meth public void columnRemoved(javax.swing.event.TableColumnModelEvent)
meth public void columnSelectionChanged(javax.swing.event.ListSelectionEvent)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void createDefaultColumnsFromModel()
meth public void editingCanceled(javax.swing.event.ChangeEvent)
meth public void editingStopped(javax.swing.event.ChangeEvent)
meth public void moveColumn(int,int)
meth public void removeColumn(javax.swing.table.TableColumn)
meth public void removeColumnSelectionInterval(int,int)
meth public void removeEditor()
meth public void removeRowSelectionInterval(int,int)
meth public void scrollToCell(int,int)
meth public void selectAll()
meth public void selectCell(int,int)
meth public void setAutoCreateColumnsFromModel(boolean)
meth public void setAutoResizeMode(int)
meth public void setCellEditor(javax.swing.table.TableCellEditor)
meth public void setCellSelectionEnabled(boolean)
meth public void setColumnModel(javax.swing.table.TableColumnModel)
meth public void setColumnSelectionAllowed(boolean)
meth public void setColumnSelectionInterval(int,int)
meth public void setDefaultEditor(java.lang.Class,javax.swing.table.TableCellEditor)
meth public void setDefaultRenderer(java.lang.Class,javax.swing.table.TableCellRenderer)
meth public void setEditingColumn(int)
meth public void setEditingRow(int)
meth public void setGridColor(java.awt.Color)
meth public void setIntercellSpacing(java.awt.Dimension)
meth public void setModel(javax.swing.table.TableModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPreferredScrollableViewportSize(java.awt.Dimension)
meth public void setRowHeight(int)
meth public void setRowMargin(int)
meth public void setRowSelectionAllowed(boolean)
meth public void setRowSelectionInterval(int,int)
meth public void setSelectionBackground(java.awt.Color)
meth public void setSelectionForeground(java.awt.Color)
meth public void setSelectionMode(int)
meth public void setSelectionModel(javax.swing.ListSelectionModel)
meth public void setShowGrid(boolean)
meth public void setShowHorizontalLines(boolean)
meth public void setShowVerticalLines(boolean)
meth public void setTableHeader(javax.swing.table.JTableHeader)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TableUI)
meth public void setValueAt(java.lang.Object,int,int)
meth public void tableChanged(javax.swing.event.TableModelEvent)
meth public void valueChanged(javax.swing.event.ListSelectionEvent)
meth public void waitCell(java.lang.String,int,int)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds WAIT_EDITING_TIMEOUT,driver,output,timeouts
hcls ByRenderedComponentTableCellChooser,BySubStringTableCellChooser,CellComponentWaiter

CLSS public org.netbeans.jemmy.operators.JTreeOperator
cons public init(javax.swing.JTree)
cons public init(org.netbeans.jemmy.operators.ContainerOperator)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,java.lang.String,int,int)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.ContainerOperator,org.netbeans.jemmy.ComponentChooser,int)
fld public final static java.lang.String NODE_PREFIX_DPROP = "Node"
fld public final static java.lang.String ROOT_DPROP = "Root"
fld public final static java.lang.String SELECTION_FIRST_DPROP = "First selected"
fld public final static java.lang.String SELECTION_LAST_DPROP = "Last selected"
innr public NoSuchPathException
innr public abstract interface static TreePathChooser
innr public abstract interface static TreeRowChooser
innr public static JTreeByItemFinder
innr public static JTreeFinder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth public boolean getInvokesStopCellEditing()
meth public boolean getScrollableTracksViewportHeight()
meth public boolean getScrollableTracksViewportWidth()
meth public boolean getScrollsOnExpand()
meth public boolean getShowsRootHandles()
meth public boolean hasBeenExpanded(javax.swing.tree.TreePath)
meth public boolean isCollapsed(int)
meth public boolean isCollapsed(javax.swing.tree.TreePath)
meth public boolean isEditable()
meth public boolean isEditing()
meth public boolean isExpanded(int)
meth public boolean isExpanded(javax.swing.tree.TreePath)
meth public boolean isFixedRowHeight()
meth public boolean isLargeModel()
meth public boolean isPathEditable(javax.swing.tree.TreePath)
meth public boolean isPathSelected(javax.swing.tree.TreePath)
meth public boolean isRootVisible()
meth public boolean isRowSelected(int)
meth public boolean isSelectionEmpty()
meth public boolean isVisible(javax.swing.tree.TreePath)
meth public boolean stopEditing()
meth public int findRow(java.lang.String)
meth public int findRow(java.lang.String,boolean,boolean)
meth public int findRow(java.lang.String,boolean,boolean,int)
meth public int findRow(java.lang.String,int)
meth public int findRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public int findRow(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator,int)
meth public int findRow(org.netbeans.jemmy.ComponentChooser)
meth public int findRow(org.netbeans.jemmy.ComponentChooser,int)
meth public int findRow(org.netbeans.jemmy.operators.JTreeOperator$TreeRowChooser)
meth public int findRow(org.netbeans.jemmy.operators.JTreeOperator$TreeRowChooser,int)
meth public int getChildCount(java.lang.Object)
meth public int getChildCount(javax.swing.tree.TreePath)
meth public int getClosestRowForLocation(int,int)
meth public int getLeadSelectionRow()
meth public int getMaxSelectionRow()
meth public int getMinSelectionRow()
meth public int getRowCount()
meth public int getRowForLocation(int,int)
meth public int getRowForPath(javax.swing.tree.TreePath)
meth public int getRowHeight()
meth public int getScrollableBlockIncrement(java.awt.Rectangle,int,int)
meth public int getScrollableUnitIncrement(java.awt.Rectangle,int,int)
meth public int getSelectionCount()
meth public int getVisibleRowCount()
meth public int[] getSelectionRows()
meth public java.awt.Component getRenderedComponent(javax.swing.tree.TreePath)
meth public java.awt.Component getRenderedComponent(javax.swing.tree.TreePath,boolean,boolean,boolean)
meth public java.awt.Dimension getPreferredScrollableViewportSize()
meth public java.awt.Point getPointToClick(int)
meth public java.awt.Point getPointToClick(javax.swing.tree.TreePath)
meth public java.awt.Rectangle getPathBounds(javax.swing.tree.TreePath)
meth public java.awt.Rectangle getRowBounds(int)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,int)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,int,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object chooseSubnode(java.lang.Object,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public java.lang.Object getChild(java.lang.Object,int)
meth public java.lang.Object getLastSelectedPathComponent()
meth public java.lang.Object getRoot()
meth public java.lang.Object[] getChildren(java.lang.Object)
meth public java.lang.String convertValueToText(java.lang.Object,boolean,boolean,boolean,int,boolean)
meth public java.util.Enumeration getExpandedDescendants(javax.swing.tree.TreePath)
meth public java.util.Hashtable getDump()
meth public javax.swing.JPopupMenu callPopupOnPath(javax.swing.tree.TreePath)
meth public javax.swing.JPopupMenu callPopupOnPath(javax.swing.tree.TreePath,int)
meth public javax.swing.JPopupMenu callPopupOnPaths(javax.swing.tree.TreePath[])
meth public javax.swing.JPopupMenu callPopupOnPaths(javax.swing.tree.TreePath[],int)
meth public javax.swing.plaf.TreeUI getUI()
meth public javax.swing.tree.TreeCellEditor getCellEditor()
meth public javax.swing.tree.TreeCellRenderer getCellRenderer()
meth public javax.swing.tree.TreeModel getModel()
meth public javax.swing.tree.TreePath findPath(java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String[])
meth public javax.swing.tree.TreePath findPath(java.lang.String[],boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[])
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[],boolean,boolean)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],int[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(java.lang.String[],org.netbeans.jemmy.operators.Operator$StringComparator)
meth public javax.swing.tree.TreePath findPath(org.netbeans.jemmy.operators.JTreeOperator$TreePathChooser)
meth public javax.swing.tree.TreePath getChildPath(javax.swing.tree.TreePath,int)
meth public javax.swing.tree.TreePath getClosestPathForLocation(int,int)
meth public javax.swing.tree.TreePath getEditingPath()
meth public javax.swing.tree.TreePath getLeadSelectionPath()
meth public javax.swing.tree.TreePath getPathForLocation(int,int)
meth public javax.swing.tree.TreePath getPathForRow(int)
meth public javax.swing.tree.TreePath getSelectionPath()
meth public javax.swing.tree.TreePath[] getChildPaths(javax.swing.tree.TreePath)
meth public javax.swing.tree.TreePath[] getSelectionPaths()
meth public javax.swing.tree.TreeSelectionModel getSelectionModel()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public static javax.swing.JTree findJTree(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTree findJTree(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTree findJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTree findJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,java.lang.String,boolean,boolean,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,java.lang.String,boolean,boolean,int,int)
meth public static javax.swing.JTree waitJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser)
meth public static javax.swing.JTree waitJTree(java.awt.Container,org.netbeans.jemmy.ComponentChooser,int)
meth public void addSelectionInterval(int,int)
meth public void addSelectionPath(javax.swing.tree.TreePath)
meth public void addSelectionPaths(javax.swing.tree.TreePath[])
meth public void addSelectionRow(int)
meth public void addSelectionRows(int[])
meth public void addTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
meth public void addTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void cancelEditing()
meth public void changePathObject(javax.swing.tree.TreePath,java.lang.Object)
meth public void changePathText(javax.swing.tree.TreePath,java.lang.String)
meth public void clearSelection()
meth public void clickForEdit(javax.swing.tree.TreePath)
meth public void clickOnPath(javax.swing.tree.TreePath)
meth public void clickOnPath(javax.swing.tree.TreePath,int)
meth public void clickOnPath(javax.swing.tree.TreePath,int,int)
meth public void clickOnPath(javax.swing.tree.TreePath,int,int,int)
meth public void collapsePath(javax.swing.tree.TreePath)
meth public void collapseRow(int)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void doCollapsePath(javax.swing.tree.TreePath)
meth public void doCollapseRow(int)
meth public void doExpandPath(javax.swing.tree.TreePath)
meth public void doExpandRow(int)
meth public void doMakeVisible(javax.swing.tree.TreePath)
meth public void expandPath(javax.swing.tree.TreePath)
meth public void expandRow(int)
meth public void fireTreeCollapsed(javax.swing.tree.TreePath)
meth public void fireTreeExpanded(javax.swing.tree.TreePath)
meth public void fireTreeWillCollapse(javax.swing.tree.TreePath)
meth public void fireTreeWillExpand(javax.swing.tree.TreePath)
meth public void makeVisible(javax.swing.tree.TreePath)
meth public void removeSelectionInterval(int,int)
meth public void removeSelectionPath(javax.swing.tree.TreePath)
meth public void removeSelectionPaths(javax.swing.tree.TreePath[])
meth public void removeSelectionRow(int)
meth public void removeSelectionRows(int[])
meth public void removeTreeExpansionListener(javax.swing.event.TreeExpansionListener)
meth public void removeTreeSelectionListener(javax.swing.event.TreeSelectionListener)
meth public void removeTreeWillExpandListener(javax.swing.event.TreeWillExpandListener)
meth public void scrollPathToVisible(javax.swing.tree.TreePath)
meth public void scrollRowToVisible(int)
meth public void scrollToPath(javax.swing.tree.TreePath)
meth public void scrollToRow(int)
meth public void selectPath(javax.swing.tree.TreePath)
meth public void selectPaths(javax.swing.tree.TreePath[])
meth public void selectRow(int)
meth public void setCellEditor(javax.swing.tree.TreeCellEditor)
meth public void setCellRenderer(javax.swing.tree.TreeCellRenderer)
meth public void setEditable(boolean)
meth public void setInvokesStopCellEditing(boolean)
meth public void setLargeModel(boolean)
meth public void setModel(javax.swing.tree.TreeModel)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setRootVisible(boolean)
meth public void setRowHeight(int)
meth public void setScrollsOnExpand(boolean)
meth public void setSelectionInterval(int,int)
meth public void setSelectionModel(javax.swing.tree.TreeSelectionModel)
meth public void setSelectionPath(javax.swing.tree.TreePath)
meth public void setSelectionPaths(javax.swing.tree.TreePath[])
meth public void setSelectionRow(int)
meth public void setSelectionRows(int[])
meth public void setShowsRootHandles(boolean)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setUI(javax.swing.plaf.TreeUI)
meth public void setVisibleRowCount(int)
meth public void startEditingAtPath(javax.swing.tree.TreePath)
meth public void treeDidChange()
meth public void waitCollapsed(int)
meth public void waitCollapsed(javax.swing.tree.TreePath)
meth public void waitExpanded(int)
meth public void waitExpanded(javax.swing.tree.TreePath)
meth public void waitRow(java.lang.String,int)
meth public void waitSelected(int)
meth public void waitSelected(int[])
meth public void waitSelected(javax.swing.tree.TreePath)
meth public void waitSelected(javax.swing.tree.TreePath[])
meth public void waitVisible(javax.swing.tree.TreePath)
supr org.netbeans.jemmy.operators.JComponentOperator
hfds BEFORE_EDIT_TIMEOUT,WAIT_AFTER_NODE_EXPANDED_TIMEOUT,WAIT_EDITING_TIMEOUT,WAIT_NEXT_NODE_TIMEOUT,WAIT_NODE_COLLAPSED_TIMEOUT,WAIT_NODE_EXPANDED_TIMEOUT,WAIT_NODE_VISIBLE_TIMEOUT,driver,output,timeouts
hcls ByRenderedComponentTreeRowChooser,BySubStringTreeRowChooser,StringArrayPathChooser

CLSS public abstract org.netbeans.jemmy.operators.Operator
cons public init()
fld public final static java.lang.String CLASS_DPROP = "Class"
fld public final static java.lang.String TO_STRING_DPROP = "toString"
innr protected abstract MapAction
innr protected abstract MapBooleanAction
innr protected abstract MapByteAction
innr protected abstract MapCharacterAction
innr protected abstract MapDoubleAction
innr protected abstract MapFloatAction
innr protected abstract MapIntegerAction
innr protected abstract MapLongAction
innr protected abstract MapVoidAction
innr protected abstract NoBlockingAction
innr public abstract interface static ComponentVisualizer
innr public abstract interface static PathParser
innr public abstract interface static StringComparator
innr public static DefaultPathParser
innr public static DefaultStringComparator
innr public static Finder
intf org.netbeans.jemmy.Outputable
intf org.netbeans.jemmy.Timeoutable
meth protected boolean runMapping(org.netbeans.jemmy.operators.Operator$MapBooleanAction)
meth protected byte runMapping(org.netbeans.jemmy.operators.Operator$MapByteAction)
meth protected char runMapping(org.netbeans.jemmy.operators.Operator$MapCharacterAction)
meth protected double runMapping(org.netbeans.jemmy.operators.Operator$MapDoubleAction)
meth protected float runMapping(org.netbeans.jemmy.operators.Operator$MapFloatAction)
meth protected int runMapping(org.netbeans.jemmy.operators.Operator$MapIntegerAction)
meth protected java.lang.Object produceTimeRestricted(org.netbeans.jemmy.Action,java.lang.Object,java.lang.String)
meth protected java.lang.Object produceTimeRestricted(org.netbeans.jemmy.Action,java.lang.String)
meth protected java.lang.Object runMapping(org.netbeans.jemmy.operators.Operator$MapAction)
meth protected java.lang.String[] addToDump(java.util.Hashtable,java.lang.String,java.lang.Object[])
meth protected java.lang.String[] addToDump(java.util.Hashtable,java.lang.String,java.lang.Object[][])
meth protected long runMapping(org.netbeans.jemmy.operators.Operator$MapLongAction)
meth protected void lockQueue()
meth protected void produceNoBlocking(org.netbeans.jemmy.operators.Operator$NoBlockingAction)
meth protected void produceNoBlocking(org.netbeans.jemmy.operators.Operator$NoBlockingAction,java.lang.Object)
meth protected void runMapping(org.netbeans.jemmy.operators.Operator$MapVoidAction)
meth protected void unlockAndThrow(java.lang.Exception)
meth protected void unlockQueue()
meth public abstract java.awt.Component getSource()
meth public boolean getVerification()
meth public boolean isCaptionEqual(java.lang.String,java.lang.String)
meth public boolean setVerification(boolean)
meth public int getCharKey(char)
meth public int getCharModifiers(char)
meth public int[] getCharsKeys(char[])
meth public int[] getCharsKeys(java.lang.String)
meth public int[] getCharsModifiers(char[])
meth public int[] getCharsModifiers(java.lang.String)
meth public java.lang.String toStringSource()
meth public java.lang.String[] getParentPath(java.lang.String[])
meth public java.lang.String[] parseString(java.lang.String)
meth public java.lang.String[] parseString(java.lang.String,java.lang.String)
meth public java.util.Hashtable getDump()
meth public org.netbeans.jemmy.CharBindingMap getCharBindingMap()
meth public org.netbeans.jemmy.ComponentChooser[] getParentPath(org.netbeans.jemmy.ComponentChooser[])
meth public org.netbeans.jemmy.JemmyProperties getProperties()
meth public org.netbeans.jemmy.JemmyProperties setProperties(org.netbeans.jemmy.JemmyProperties)
meth public org.netbeans.jemmy.QueueTool getQueueTool()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public org.netbeans.jemmy.Timeouts getTimeouts()
meth public org.netbeans.jemmy.operators.Operator$ComponentVisualizer getVisualizer()
meth public org.netbeans.jemmy.operators.Operator$PathParser getPathParser()
meth public org.netbeans.jemmy.operators.Operator$StringComparator getComparator()
meth public static boolean getDefaultVerification()
meth public static boolean isCaptionEqual(java.lang.String,java.lang.String,boolean,boolean)
meth public static boolean isCaptionEqual(java.lang.String,java.lang.String,org.netbeans.jemmy.operators.Operator$StringComparator)
meth public static boolean setDefaultVerification(boolean)
meth public static int getDefaultMouseButton()
meth public static int getPopupMouseButton()
meth public static org.netbeans.jemmy.operators.ComponentOperator createOperator(java.awt.Component)
meth public static org.netbeans.jemmy.operators.Operator getEnvironmentOperator()
meth public static org.netbeans.jemmy.operators.Operator$ComponentVisualizer getDefaultComponentVisualizer()
meth public static org.netbeans.jemmy.operators.Operator$ComponentVisualizer setDefaultComponentVisualizer(org.netbeans.jemmy.operators.Operator$ComponentVisualizer)
meth public static org.netbeans.jemmy.operators.Operator$PathParser getDefaultPathParser()
meth public static org.netbeans.jemmy.operators.Operator$PathParser setDefaultPathParser(org.netbeans.jemmy.operators.Operator$PathParser)
meth public static org.netbeans.jemmy.operators.Operator$StringComparator getDefaultStringComparator()
meth public static org.netbeans.jemmy.operators.Operator$StringComparator setDefaultStringComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
meth public static void addOperatorPackage(java.lang.String)
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void printDump()
meth public void setCharBindingMap(org.netbeans.jemmy.CharBindingMap)
meth public void setComparator(org.netbeans.jemmy.operators.Operator$StringComparator)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void setPathParser(org.netbeans.jemmy.operators.Operator$PathParser)
meth public void setTimeouts(org.netbeans.jemmy.Timeouts)
meth public void setVisualizer(org.netbeans.jemmy.operators.Operator$ComponentVisualizer)
meth public void waitState(org.netbeans.jemmy.ComponentChooser)
supr java.lang.Object
hfds comparator,map,operatorPkgs,output,parser,properties,queueTool,timeouts,verification,visualizer
hcls NullOperator

CLSS public org.netbeans.jemmy.operators.WindowOperator
cons public init()
cons public init(int)
cons public init(int,org.netbeans.jemmy.operators.Operator)
cons public init(java.awt.Window)
cons public init(org.netbeans.jemmy.operators.WindowOperator)
cons public init(org.netbeans.jemmy.operators.WindowOperator,int)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser)
cons public init(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
intf org.netbeans.jemmy.Outputable
meth protected static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser,int,org.netbeans.jemmy.Timeouts,org.netbeans.jemmy.TestOut)
meth protected static java.awt.Window waitWindow(org.netbeans.jemmy.operators.WindowOperator,org.netbeans.jemmy.ComponentChooser,int)
meth public boolean isActive()
meth public boolean isFocused()
meth public java.awt.Component getFocusOwner()
meth public java.awt.Window findSubWindow(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Window findSubWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Window getOwner()
meth public java.awt.Window waitSubWindow(org.netbeans.jemmy.ComponentChooser)
meth public java.awt.Window waitSubWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public java.awt.Window[] getOwnedWindows()
meth public java.lang.String getWarningString()
meth public org.netbeans.jemmy.TestOut getOutput()
meth public static java.awt.Window findWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window findWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window findWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window findWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window waitWindow(java.awt.Window,org.netbeans.jemmy.ComponentChooser,int)
meth public static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser)
meth public static java.awt.Window waitWindow(org.netbeans.jemmy.ComponentChooser,int)
meth public void activate()
meth public void addWindowListener(java.awt.event.WindowListener)
meth public void applyResourceBundle(java.lang.String)
meth public void applyResourceBundle(java.util.ResourceBundle)
meth public void close()
meth public void copyEnvironment(org.netbeans.jemmy.operators.Operator)
meth public void dispose()
meth public void move(int,int)
meth public void pack()
meth public void removeWindowListener(java.awt.event.WindowListener)
meth public void requestClose()
meth public void requestCloseAndThenHide()
meth public void resize(int,int)
meth public void setOutput(org.netbeans.jemmy.TestOut)
meth public void toBack()
meth public void toFront()
meth public void waitClosed()
supr org.netbeans.jemmy.operators.ContainerOperator
hfds driver,output

CLSS public abstract interface org.netbeans.junit.NbTest
intf junit.framework.Test
meth public abstract boolean canRun()
meth public abstract java.lang.String getExpectedFail()
meth public abstract void setFilter(org.netbeans.junit.Filter)

CLSS public abstract org.netbeans.junit.NbTestCase
cons public init(java.lang.String)
intf org.netbeans.junit.NbTest
meth protected boolean runInEQ()
meth protected final int getTestNumber()
meth protected int timeOut()
meth protected java.lang.String logRoot()
meth protected java.util.logging.Level logLevel()
meth public boolean canRun()
meth public java.io.File getDataDir()
meth public java.io.File getGoldenFile()
meth public java.io.File getGoldenFile(java.lang.String)
meth public java.io.File getWorkDir() throws java.io.IOException
meth public java.io.PrintStream getLog()
meth public java.io.PrintStream getLog(java.lang.String)
meth public java.io.PrintStream getRef()
meth public java.lang.String getExpectedFail()
meth public java.lang.String getWorkDirPath()
meth public static int assertSize(java.lang.String,java.util.Collection<?>,int,org.netbeans.junit.MemoryFilter)
meth public static java.lang.String convertNBFSURL(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static void assertFile(java.io.File,java.io.File)
meth public static void assertFile(java.io.File,java.io.File,java.io.File)
meth public static void assertFile(java.io.File,java.io.File,java.io.File,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.io.File,java.io.File,java.io.File)
meth public static void assertFile(java.lang.String,java.io.File,java.io.File,java.io.File,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.junit.diff.Diff)
meth public static void assertFile(java.lang.String,java.lang.String,java.lang.String,org.netbeans.junit.diff.Diff)
meth public static void assertGC(java.lang.String,java.lang.ref.Reference<?>)
meth public static void assertGC(java.lang.String,java.lang.ref.Reference<?>,java.util.Set<?>)
meth public static void assertSize(java.lang.String,int,java.lang.Object)
meth public static void assertSize(java.lang.String,java.util.Collection<?>,int)
meth public static void assertSize(java.lang.String,java.util.Collection<?>,int,java.lang.Object[])
meth public static void failByBug(int)
meth public static void failByBug(int,java.lang.String)
meth public void clearWorkDir() throws java.io.IOException
meth public void compareReferenceFiles()
meth public void compareReferenceFiles(java.lang.String,java.lang.String,java.lang.String)
meth public void log(java.lang.String)
meth public void log(java.lang.String,java.lang.String)
meth public void ref(java.lang.String)
meth public void run(junit.framework.TestResult)
meth public void runBare() throws java.lang.Throwable
meth public void setFilter(org.netbeans.junit.Filter)
supr junit.framework.TestCase
hfds DEFAULT_TIME_OUT_CALLED,filter,lastTestMethod,logStreamTable,radix,systemOutPSWrapper,time,usedPaths,vmDeadline,workDirPath
hcls WFOS

