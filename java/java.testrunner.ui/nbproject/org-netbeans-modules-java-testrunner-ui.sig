#Signature file v4.1
#Version 1.27

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

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

CLSS public abstract org.netbeans.modules.gsf.testrunner.api.CoreManager
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract void displayOutput(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String,boolean)
meth public abstract void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report)
meth public abstract void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report,boolean)
meth public abstract void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String)
meth public abstract void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public abstract void sessionFinished(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public abstract void testStarted(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public void registerNodeFactory()
supr java.lang.Object

CLSS public org.netbeans.modules.java.testrunner.ui.api.JavaManager
cons public init()
meth public void displayOutput(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String,boolean)
meth public void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report)
meth public void displayReport(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.Report,boolean)
meth public void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,java.lang.String)
meth public void displaySuiteRunning(org.netbeans.modules.gsf.testrunner.api.TestSession,org.netbeans.modules.gsf.testrunner.api.TestSuite)
meth public void sessionFinished(org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public void testStarted(org.netbeans.modules.gsf.testrunner.api.TestSession)
supr org.netbeans.modules.gsf.testrunner.api.CoreManager

CLSS public org.netbeans.modules.java.testrunner.ui.api.JumpAction
cons public init(org.openide.nodes.Node,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.Object getValue(java.lang.String)
meth public org.openide.nodes.Node getNode()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr javax.swing.AbstractAction
hfds RP,callstackFrameInfo,node,projectType,testingFramework

CLSS public abstract org.netbeans.modules.java.testrunner.ui.api.NodeOpener
cons public init()
innr public abstract interface static !annotation Registration
meth public abstract void openCallstackFrame(org.openide.nodes.Node,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void openTestMethod(org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode)
meth public abstract void openTestsuite(org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode)
supr java.lang.Object

CLSS public abstract interface static !annotation org.netbeans.modules.java.testrunner.ui.api.NodeOpener$Registration
 outer org.netbeans.modules.java.testrunner.ui.api.NodeOpener
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String projectType()
meth public abstract java.lang.String testingFramework()

CLSS public final org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils
cons public init()
meth public static java.lang.String determineStackFrame(org.netbeans.modules.gsf.testrunner.api.Trouble)
meth public static org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode getTestMethodNode(org.openide.nodes.Node)
meth public static org.openide.filesystems.FileObject getFile(java.lang.String,int[],org.netbeans.api.extexecution.print.LineConvertors$FileLocator)
meth public static void openCallstackFrame(org.openide.nodes.Node,java.lang.String,java.lang.String,java.lang.String)
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void openFile(org.openide.filesystems.FileObject,int)
meth public static void openFile(org.openide.filesystems.FileObject,int,int)
meth public static void openTestMethod(org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode,java.lang.String,java.lang.String)
meth public static void openTestsuite(org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode,java.lang.String,java.lang.String)
meth public static void searchAllMethods(org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode,org.openide.filesystems.FileObject[],long[],org.netbeans.api.java.source.CompilationController,javax.lang.model.element.Element)
supr java.lang.Object
hfds LOGGER,NO_ACTIONS

CLSS public abstract interface org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods
innr public abstract interface static Factory
meth public abstract java.util.List<org.netbeans.modules.gsf.testrunner.ui.api.TestMethodController$TestMethod> computeTestMethods(org.netbeans.api.java.source.CompilationInfo)
meth public abstract void cancel()

CLSS public abstract interface static org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods$Factory
 outer org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods
meth public abstract org.netbeans.modules.java.testrunner.ui.spi.ComputeTestMethods create()

