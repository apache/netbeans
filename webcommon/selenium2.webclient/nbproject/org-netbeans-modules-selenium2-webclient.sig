#Signature file v4.1
#Version 1.30

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

CLSS public abstract interface org.netbeans.modules.gsf.testrunner.api.RerunHandler
meth public abstract boolean enabled(org.netbeans.modules.gsf.testrunner.api.RerunType)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void rerun()
meth public abstract void rerun(java.util.Set<org.netbeans.modules.gsf.testrunner.api.Testcase>)

CLSS public org.netbeans.modules.selenium2.webclient.api.RunInfo
innr public final static Builder
meth public boolean isAbsoluteUrls()
meth public boolean isSelenium()
meth public boolean isShowOutput()
meth public boolean isTestingProject()
meth public java.lang.String getTestFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvVars()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.modules.gsf.testrunner.api.RerunHandler getRerunHandler()
meth public org.openide.filesystems.FileObject[] getActivatedFOs()
meth public void setAbsoluteUrls(boolean)
supr java.lang.Object
hfds absoluteUrls,activatedFOs,envVars,isSelenium,project,rerunHandler,showOutput,testFile,testingProject

CLSS public final static org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder
 outer org.netbeans.modules.selenium2.webclient.api.RunInfo
cons public init(org.openide.filesystems.FileObject[])
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo build()
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder addEnvVar(java.lang.String,java.lang.String)
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder addEnvVars(java.util.Map<java.lang.String,java.lang.String>)
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder setIsSelenium(boolean)
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder setRerunHandler(org.netbeans.modules.gsf.testrunner.api.RerunHandler)
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder setShowOutput(boolean)
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder setTestFile(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.selenium2.webclient.api.RunInfo$Builder setTestingProject(boolean)
supr java.lang.Object
hfds activatedFOs,envVars,isSelenium,nbConfigFile,project,projectConfigFile,rerunHandler,showOutput,testFile,testingProject

CLSS public final org.netbeans.modules.selenium2.webclient.api.SeleniumRerunHandler
cons public init(org.netbeans.api.project.Project,org.openide.filesystems.FileObject[],java.lang.String,boolean)
intf org.netbeans.modules.gsf.testrunner.api.RerunHandler
meth public boolean enabled(org.netbeans.modules.gsf.testrunner.api.RerunType)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void rerun()
meth public void rerun(java.util.Set<org.netbeans.modules.gsf.testrunner.api.Testcase>)
supr java.lang.Object
hfds RP,activatedFOs,changeSupport,enabled,identifier,isSelenium,project

CLSS public final org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider
meth public boolean isCoverageSupported(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public void debugTests(org.openide.filesystems.FileObject[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void runTests(org.openide.filesystems.FileObject[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds delegate

CLSS public final org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders
fld public final static java.lang.String CUSTOMIZER_SELENIUM_TESTING_IDENT = "SELENIUM_TESTING"
fld public final static java.lang.String SELENIUM_TESTING_PATH = "Selenium/Testing"
meth public java.util.List<org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider> getSeleniumTestingProviders()
meth public org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider findSeleniumTestingProvider(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider getSeleniumTestingProvider(org.netbeans.api.project.Project,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizer()
meth public static org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders getDefault()
meth public void setSeleniumTestingProvider(org.netbeans.api.project.Project,org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds INSTANCE,SELENIUM_TESTING_PROVIDERS,seleniumTestingProviders

CLSS public final org.netbeans.modules.selenium2.webclient.api.TestRunnerReporter
cons public init(org.netbeans.modules.selenium2.webclient.api.RunInfo,java.lang.String)
innr public final static CallStackCallback
meth public java.lang.String processLine(java.lang.String)
supr java.lang.Object
hfds CAPABILITY,DONE_PATTERN,LOGGER,MULTI_CAPABILITIES,NB_LINE,NOT_OK_PATTERN,OK_PATTERN,OK_SKIP_PATTERN,SESSION_END_PATTERN,SESSION_START_PATTERN,SKIP,browser,duration,hasTests,multiCapabilities,normalSessionEnd,runInfo,runningSuite,showOutput,stackTrace,testIndex,testSession,testSuite,testSuiteRuntime,testcase,trouble

CLSS public final static org.netbeans.modules.selenium2.webclient.api.TestRunnerReporter$CallStackCallback
 outer org.netbeans.modules.selenium2.webclient.api.TestRunnerReporter
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback
meth public org.openide.util.Pair<java.io.File,int[]> parseLocation(java.lang.String,boolean)
supr java.lang.Object
hfds FILE_LINE_PATTERN_UNIX,FILE_LINE_PATTERN_WINDOWS,project

CLSS public org.netbeans.modules.selenium2.webclient.api.Utilities
meth public static org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory getTestRunnerNodeFactory(org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback)
meth public static org.openide.filesystems.FileObject getTestsFolder(org.netbeans.api.project.Project,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.openide.filesystems.FileObject getTestsSeleniumFolder(org.netbeans.api.project.Project,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static void openCustomizer(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.selenium2.webclient.spi.JumpToCallStackCallback
meth public abstract org.openide.util.Pair<java.io.File,int[]> parseLocation(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.selenium2.webclient.spi.SeleniumTestingProviderImplementation
meth public abstract boolean isCoverageSupported(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean isEnabled(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIdentifier()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation createCustomizerPanel(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void debugTests(org.openide.filesystems.FileObject[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void notifyEnabled(org.netbeans.api.project.Project,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectClosed(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectOpened(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runTests(org.openide.filesystems.FileObject[])
 anno 1 org.netbeans.api.annotations.common.NonNull()

