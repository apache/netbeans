#Signature file v4.1
#Version 0.42

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

CLSS public final org.netbeans.modules.php.api.testing.PhpTesting
fld public final static java.lang.String CUSTOMIZER_IDENT = "Testing"
fld public final static java.lang.String TESTING_PATH = "PHP/Testing"
meth public static boolean isTestingProviderEnabled(java.lang.String,org.netbeans.modules.php.api.phpmodule.PhpModule)
meth public static java.util.List<org.netbeans.modules.php.spi.testing.PhpTestingProvider> getTestingProviders()
meth public static void addTestingProvidersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void removeTestingProvidersListener(org.openide.util.LookupListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds LOGGER,TESTING_PROVIDERS

CLSS public abstract interface org.netbeans.modules.php.spi.testing.PhpTestingProvider
innr public abstract interface static !annotation Registration
meth public abstract boolean isCoverageSupported(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean isTestCase(org.netbeans.modules.php.api.phpmodule.PhpModule,org.netbeans.modules.php.api.editor.PhpType$Method)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean isTestFile(org.netbeans.modules.php.api.phpmodule.PhpModule,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getIdentifier()
meth public abstract org.netbeans.modules.php.spi.testing.create.CreateTestsResult createTests(org.netbeans.modules.php.api.phpmodule.PhpModule,java.util.List<org.openide.filesystems.FileObject>,java.util.Map<java.lang.String,java.lang.Object>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.php.spi.testing.locate.Locations$Line parseFileFromOutput(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.php.spi.testing.locate.TestLocator getTestLocator(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider createCustomizer(org.netbeans.modules.php.api.phpmodule.PhpModule)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void runTests(org.netbeans.modules.php.api.phpmodule.PhpModule,org.netbeans.modules.php.spi.testing.run.TestRunInfo,org.netbeans.modules.php.spi.testing.run.TestSession) throws org.netbeans.modules.php.spi.testing.run.TestRunException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static !annotation org.netbeans.modules.php.spi.testing.PhpTestingProvider$Registration
 outer org.netbeans.modules.php.spi.testing.PhpTestingProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()

CLSS public abstract interface org.netbeans.modules.php.spi.testing.PhpTestingProviders
meth public abstract java.util.List<org.netbeans.modules.php.spi.testing.PhpTestingProvider> getEnabledTestingProviders()

CLSS public abstract interface org.netbeans.modules.php.spi.testing.coverage.Coverage
innr public abstract interface static File
innr public abstract interface static Line
meth public abstract java.util.List<org.netbeans.modules.php.spi.testing.coverage.Coverage$File> getFiles()

CLSS public abstract interface static org.netbeans.modules.php.spi.testing.coverage.Coverage$File
 outer org.netbeans.modules.php.spi.testing.coverage.Coverage
meth public abstract java.lang.String getPath()
meth public abstract java.util.List<org.netbeans.modules.php.spi.testing.coverage.Coverage$Line> getLines()
meth public abstract org.netbeans.modules.php.spi.testing.coverage.FileMetrics getMetrics()

CLSS public abstract interface static org.netbeans.modules.php.spi.testing.coverage.Coverage$Line
 outer org.netbeans.modules.php.spi.testing.coverage.Coverage
meth public abstract int getHitCount()
meth public abstract int getNumber()

CLSS public abstract interface org.netbeans.modules.php.spi.testing.coverage.FileMetrics
meth public abstract int getCoveredStatements()
meth public abstract int getLineCount()
meth public abstract int getStatements()

CLSS public final org.netbeans.modules.php.spi.testing.create.CreateTestsResult
cons public init(java.util.Set<org.openide.filesystems.FileObject>,java.util.Set<org.openide.filesystems.FileObject>)
meth public java.util.Set<org.openide.filesystems.FileObject> getFailed()
meth public java.util.Set<org.openide.filesystems.FileObject> getSucceeded()
supr java.lang.Object
hfds failed,succeeded

CLSS public final org.netbeans.modules.php.spi.testing.create.CreateTestsSupport
meth public boolean isEnabled()
meth public java.lang.Object[] getTestSourceRoots(java.util.Collection<org.netbeans.api.project.SourceGroup>,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration createEmptyConfiguration(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.api.phpmodule.PhpModule getPhpModule()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.php.spi.testing.create.CreateTestsSupport create(org.netbeans.modules.php.spi.testing.PhpTestingProvider,org.openide.filesystems.FileObject[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void createTests(java.util.Map<java.lang.String,java.lang.Object>)
supr java.lang.Object
hfds LOGGER,RP,activatedFileObjects,phpModule,project,testingProvider

CLSS public final org.netbeans.modules.php.spi.testing.locate.Locations
innr public final static Line
innr public final static Offset
supr java.lang.Object

CLSS public final static org.netbeans.modules.php.spi.testing.locate.Locations$Line
 outer org.netbeans.modules.php.spi.testing.locate.Locations
cons public init(org.openide.filesystems.FileObject,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public int getLine()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds file,line

CLSS public final static org.netbeans.modules.php.spi.testing.locate.Locations$Offset
 outer org.netbeans.modules.php.spi.testing.locate.Locations
cons public init(org.openide.filesystems.FileObject,int)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public int getOffset()
meth public java.lang.String toString()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds file,offset

CLSS public abstract interface org.netbeans.modules.php.spi.testing.locate.TestLocator
meth public abstract java.util.Set<org.netbeans.modules.php.spi.testing.locate.Locations$Offset> findSources(org.openide.filesystems.FileObject)
meth public abstract java.util.Set<org.netbeans.modules.php.spi.testing.locate.Locations$Offset> findTests(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.php.spi.testing.run.OutputLineHandler
meth public abstract void handleLine(org.openide.windows.OutputWriter,java.lang.String)

CLSS public abstract interface org.netbeans.modules.php.spi.testing.run.TestCase
innr public final static !enum Status
innr public final static Diff
meth public abstract void setClassName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setFailureInfo(java.lang.String,java.lang.String[],boolean,org.netbeans.modules.php.spi.testing.run.TestCase$Diff)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setLocation(org.netbeans.modules.php.spi.testing.locate.Locations$Line)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setStatus(org.netbeans.modules.php.spi.testing.run.TestCase$Status)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setTime(long)

CLSS public final static org.netbeans.modules.php.spi.testing.run.TestCase$Diff
 outer org.netbeans.modules.php.spi.testing.run.TestCase
cons public init(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.util.concurrent.Callable<java.lang.String>,java.util.concurrent.Callable<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Diff NOT_KNOWN
meth public boolean isValid()
meth public java.lang.String getActual()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getExpected()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
supr java.lang.Object
hfds LOGGER,actual,actualTask,expected,expectedTask

CLSS public final static !enum org.netbeans.modules.php.spi.testing.run.TestCase$Status
 outer org.netbeans.modules.php.spi.testing.run.TestCase
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status ABORTED
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status ERROR
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status FAILED
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status IGNORED
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status PASSED
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status PASSEDWITHERRORS
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status PENDING
fld public final static org.netbeans.modules.php.spi.testing.run.TestCase$Status SKIPPED
meth public static org.netbeans.modules.php.spi.testing.run.TestCase$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.php.spi.testing.run.TestCase$Status[] values()
supr java.lang.Enum<org.netbeans.modules.php.spi.testing.run.TestCase$Status>

CLSS public org.netbeans.modules.php.spi.testing.run.TestRunException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.netbeans.modules.php.spi.testing.run.TestRunInfo
innr public final static !enum SessionType
innr public final static Builder
innr public final static TestInfo
meth public <%0 extends java.lang.Object> {%%0} getParameter(java.lang.String,java.lang.Class<{%%0}>)
meth public boolean allTests()
meth public boolean isCoverageEnabled()
meth public boolean isRerun()
meth public java.lang.String getSuiteName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.List<org.netbeans.modules.php.spi.testing.run.TestRunInfo$TestInfo> getCustomTests()
meth public java.util.List<org.openide.filesystems.FileObject> getStartFiles()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType getSessionType()
meth public void removeParameter(java.lang.String)
meth public void resetCustomTests()
meth public void setCustomTests(java.util.Collection<org.netbeans.modules.php.spi.testing.run.TestRunInfo$TestInfo>)
meth public void setInitialTests(java.util.Collection<org.netbeans.modules.php.spi.testing.run.TestRunInfo$TestInfo>)
meth public void setParameter(java.lang.String,java.lang.Object)
meth public void setRerun(boolean)
supr java.lang.Object
hfds coverageEnabled,customTests,initialTests,parameters,rerun,sessionType,startFiles,suiteName

CLSS public final static org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder
 outer org.netbeans.modules.php.spi.testing.run.TestRunInfo
cons public init()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo build()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder setCoverageEnabled(boolean)
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder setSessionType(org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder setStartFile(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder setStartFiles(java.util.List<org.openide.filesystems.FileObject>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.php.spi.testing.run.TestRunInfo$Builder setSuiteName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds coverageEnabled,sessionType,startFiles,suiteName

CLSS public final static !enum org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType
 outer org.netbeans.modules.php.spi.testing.run.TestRunInfo
fld public final static org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType DEBUG
fld public final static org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType TEST
meth public static org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType valueOf(java.lang.String)
meth public static org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType[] values()
supr java.lang.Enum<org.netbeans.modules.php.spi.testing.run.TestRunInfo$SessionType>

CLSS public final static org.netbeans.modules.php.spi.testing.run.TestRunInfo$TestInfo
 outer org.netbeans.modules.php.spi.testing.run.TestRunInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
fld public final static java.lang.String UNKNOWN_TYPE = "UNKNOWN_TYPE"
meth public java.lang.String getClassName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getLocation()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds className,location,name,type

CLSS public abstract interface org.netbeans.modules.php.spi.testing.run.TestSession
meth public abstract org.netbeans.modules.php.spi.testing.run.TestSuite addTestSuite(java.lang.String,org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void printMessage(java.lang.String,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setCoverage(org.netbeans.modules.php.spi.testing.coverage.Coverage)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void setOutputLineHandler(org.netbeans.modules.php.spi.testing.run.OutputLineHandler)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.php.spi.testing.run.TestSuite
meth public abstract org.netbeans.modules.php.spi.testing.run.TestCase addTestCase(java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void finish(long)

