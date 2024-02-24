#Signature file v4.1
#Version 1.28

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

CLSS public final org.netbeans.modules.selenium2.api.Selenium2Support
fld public final static java.lang.String DEFAULT_SELENIUM_SERVER_PORT = "4444"
fld public final static java.lang.String DEFAULT_SERVER_PORT = "80"
fld public final static java.lang.String SELENIUM_FOLDER_NAME = "test"
fld public final static java.lang.String SELENIUM_LIBRARY_NAME = "Selenium2"
fld public final static java.lang.String SELENIUM_TESTCLASS_NAME_SUFFIX = "IT"
meth public final static org.netbeans.modules.selenium2.spi.Selenium2SupportImpl findSelenium2Support(org.netbeans.api.project.Project)
meth public static boolean isSupportEnabled(org.openide.filesystems.FileObject[])
meth public static java.util.ArrayList<org.openide.filesystems.FileObject> createTests(org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider$Context)
meth public static org.openide.loaders.DataObject loadTestTemplate(java.lang.String)
meth public static void runTests(org.openide.filesystems.FileObject[],boolean)
supr java.lang.Object
hfds cache,implementations

CLSS public final org.netbeans.modules.selenium2.api.Utils
cons public init()
meth public static void openFile(java.io.File,int)
meth public static void openFile(java.io.File,int,int)
supr java.lang.Object
hfds LOGGER

CLSS public abstract org.netbeans.modules.selenium2.spi.Selenium2SupportImpl
cons public init()
meth public abstract boolean isSupportActive(org.netbeans.api.project.Project)
meth public abstract boolean isSupportEnabled(org.openide.filesystems.FileObject[])
meth public abstract java.lang.String getTemplateID()
meth public abstract java.lang.String[] getSourceAndTestClassNames(org.openide.filesystems.FileObject,boolean,boolean)
meth public abstract java.util.List<java.lang.Object> getTestSourceRoots(java.util.Collection<org.netbeans.api.project.SourceGroup>,org.openide.filesystems.FileObject)
meth public abstract org.openide.WizardDescriptor$Panel createTargetChooserPanel(org.openide.WizardDescriptor)
meth public abstract void configureProject(org.openide.filesystems.FileObject)
meth public abstract void runTests(org.openide.filesystems.FileObject[],boolean)
supr java.lang.Object

