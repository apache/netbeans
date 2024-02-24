#Signature file v4.1
#Version 1.113

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.AssertionError
cons public init()
cons public init(boolean)
cons public init(char)
cons public init(double)
cons public init(float)
cons public init(int)
cons public init(java.lang.Object)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(long)
supr java.lang.Error

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Error
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
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

CLSS public abstract java.util.logging.Handler
cons protected init()
meth protected void reportError(java.lang.String,java.lang.Exception,int)
meth public abstract void close()
meth public abstract void flush()
meth public abstract void publish(java.util.logging.LogRecord)
meth public boolean isLoggable(java.util.logging.LogRecord)
meth public java.lang.String getEncoding()
meth public java.util.logging.ErrorManager getErrorManager()
meth public java.util.logging.Filter getFilter()
meth public java.util.logging.Formatter getFormatter()
meth public java.util.logging.Level getLevel()
meth public void setEncoding(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void setErrorManager(java.util.logging.ErrorManager)
meth public void setFilter(java.util.logging.Filter)
meth public void setFormatter(java.util.logging.Formatter)
meth public void setLevel(java.util.logging.Level)
supr java.lang.Object

CLSS public junit.extensions.TestDecorator
cons public init(junit.framework.Test)
fld protected junit.framework.Test fTest
intf junit.framework.Test
meth public int countTestCases()
meth public java.lang.String toString()
meth public junit.framework.Test getTest()
meth public void basicRun(junit.framework.TestResult)
meth public void run(junit.framework.TestResult)
supr junit.framework.Assert

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

CLSS public junit.framework.AssertionFailedError
cons public init()
cons public init(java.lang.String)
supr java.lang.AssertionError
hfds serialVersionUID

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

CLSS public junit.framework.TestSuite
cons public !varargs init(java.lang.Class<?>[])
cons public init()
cons public init(java.lang.Class<? extends junit.framework.TestCase>,java.lang.String)
cons public init(java.lang.Class<? extends junit.framework.TestCase>[],java.lang.String)
cons public init(java.lang.Class<?>)
cons public init(java.lang.String)
intf junit.framework.Test
meth public int countTestCases()
meth public int testCount()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Enumeration<junit.framework.Test> tests()
meth public junit.framework.Test testAt(int)
meth public static java.lang.reflect.Constructor<?> getTestConstructor(java.lang.Class<?>) throws java.lang.NoSuchMethodException
meth public static junit.framework.Test createTest(java.lang.Class<?>,java.lang.String)
meth public static junit.framework.Test warning(java.lang.String)
meth public void addTest(junit.framework.Test)
meth public void addTestSuite(java.lang.Class<? extends junit.framework.TestCase>)
meth public void run(junit.framework.TestResult)
meth public void runTest(junit.framework.Test,junit.framework.TestResult)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds fName,fTests

CLSS public org.netbeans.junit.AssertionFailedErrorException
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.Throwable nestedException
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr junit.framework.AssertionFailedError

CLSS public org.netbeans.junit.AssertionFileFailedError
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld protected java.lang.String diffFile
meth public java.lang.String getDiffFile()
supr junit.framework.AssertionFailedError

CLSS public org.netbeans.junit.AssertionKnownBugError
cons public init(int)
cons public init(int,java.lang.String)
fld protected int bugID
meth public int getBugID()
supr junit.framework.AssertionFailedError

CLSS public org.netbeans.junit.Filter
cons public init()
innr public static IncludeExclude
meth public boolean isIncluded(java.lang.String)
meth public java.lang.String getExpectedFail(java.lang.String)
meth public java.lang.String toString()
meth public org.netbeans.junit.Filter$IncludeExclude[] getExcludes()
meth public org.netbeans.junit.Filter$IncludeExclude[] getIncludes()
meth public static boolean match(java.lang.String,java.lang.String)
meth public void setExcludes(org.netbeans.junit.Filter$IncludeExclude[])
meth public void setIncludes(org.netbeans.junit.Filter$IncludeExclude[])
supr java.lang.Object
hfds exc,inc

CLSS public static org.netbeans.junit.Filter$IncludeExclude
 outer org.netbeans.junit.Filter
cons public init()
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getExpectedFail()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void setExpectedFail(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds expectedFail,name

CLSS public final org.netbeans.junit.Log
cons public init()
meth public !varargs static void assertInstances(java.lang.String,java.lang.String[])
meth public static java.lang.CharSequence enable(java.lang.String,java.util.logging.Level)
meth public static void assertInstances(java.lang.String)
meth public static void controlFlow(java.util.logging.Logger,java.util.logging.Logger,java.lang.String,int)
meth public static void enableInstances(java.util.logging.Logger,java.lang.String,java.util.logging.Level)
meth public void close()
meth public void flush()
meth public void publish(java.util.logging.LogRecord)
supr java.util.logging.Handler
hfds current,initialMessages,log,logger,messages
hcls IL,InstancesHandler

CLSS public org.netbeans.junit.Manager
fld protected final static java.lang.String DEFAULT_DIFF_IMPL = "org.netbeans.junit.diff.SimpleDiff"
fld protected final static java.lang.String PROP_DIFF_IMPL = "nbjunit.diff.impl"
fld protected static java.util.Properties fPreferences
fld protected static org.netbeans.junit.diff.Diff systemDiff
fld public final static java.lang.String JUNIT_PROPERTIES_FILENAME = "junit.properties"
fld public final static java.lang.String JUNIT_PROPERTIES_LOCATION_PROPERTY = "junit.properties.file"
fld public final static java.lang.String NBJUNIT_HOME = "nbjunit.home"
fld public final static java.lang.String NBJUNIT_WORKDIR = "nbjunit.workdir"
meth protected static org.netbeans.junit.diff.Diff instantiateDiffImpl(java.lang.String)
meth protected static void readProperties()
meth public static java.io.File getNbJUnitHome() throws java.io.IOException
meth public static java.io.File normalizeFile(java.io.File)
meth public static java.lang.String getNbJUnitHomePath() throws java.io.IOException
meth public static java.lang.String getWorkDirPath()
meth public static org.netbeans.junit.diff.Diff getSystemDiff()
supr java.lang.Object

CLSS public abstract interface org.netbeans.junit.MemoryFilter
meth public abstract boolean reject(java.lang.Object)

CLSS public org.netbeans.junit.MemoryMeasurement
fld public final static java.lang.String IDE_PID_SYSTEM_PROPERTY = "netbeans.pid"
meth public static long getIdeMemoryFootPrint()
meth public static long getProcessMemoryFootPrint(long)
supr java.lang.Object
hfds LINUX,SOLARIS,SUPPORTED_PLATFORMS,UNKNOWN,UNKNOWN_VALUE,WINDOWS,libraryLoaded

CLSS public org.netbeans.junit.MemoryMeasurementFailedException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public org.netbeans.junit.MockServices
meth public !varargs static void setServices(java.lang.Class<?>[])
supr java.lang.Object
hfds LOG
hcls ServiceClassLoader

CLSS public abstract org.netbeans.junit.MultiTestCase
cons public init()
cons public init(java.lang.String)
meth protected abstract void execute()
meth protected void runTest() throws java.lang.Throwable
supr org.netbeans.junit.NbTestCase
hfds err

CLSS public abstract org.netbeans.junit.MultiTestSuite
cons public init()
cons public init(java.lang.String)
meth protected abstract org.netbeans.junit.MultiTestCase nextTestCase()
meth protected void runAllTests(junit.framework.TestResult)
meth public void cleanup()
meth public void prepare()
meth public void run(junit.framework.TestResult)
supr org.netbeans.junit.NbTestSuite
hfds err

CLSS public org.netbeans.junit.NbModuleSuite
innr public final static Configuration
meth public !varargs static junit.framework.Test allModules(java.lang.Class<? extends junit.framework.TestCase>,java.lang.String[])
meth public !varargs static junit.framework.Test create(java.lang.Class<? extends junit.framework.TestCase>,java.lang.String,java.lang.String,java.lang.String[])
meth public static junit.framework.Test create(java.lang.Class<? extends junit.framework.TestCase>,java.lang.String,java.lang.String)
meth public static junit.framework.Test create(org.netbeans.junit.NbModuleSuite$Configuration)
meth public static org.netbeans.junit.NbModuleSuite$Configuration createConfiguration(java.lang.Class<? extends junit.framework.TestCase>)
meth public static org.netbeans.junit.NbModuleSuite$Configuration emptyConfiguration()
supr java.lang.Object
hfds LOG
hcls Item,NbTestSuiteLogCheck,S

CLSS public final static org.netbeans.junit.NbModuleSuite$Configuration
 outer org.netbeans.junit.NbModuleSuite
meth public !varargs org.netbeans.junit.NbModuleSuite$Configuration addStartupArgument(java.lang.String[])
meth public !varargs org.netbeans.junit.NbModuleSuite$Configuration addTest(java.lang.Class<? extends junit.framework.TestCase>,java.lang.String[])
meth public !varargs org.netbeans.junit.NbModuleSuite$Configuration addTest(java.lang.String[])
meth public junit.framework.Test suite()
meth public org.netbeans.junit.NbModuleSuite$Configuration addTest(java.lang.Class<? extends junit.framework.Test>)
meth public org.netbeans.junit.NbModuleSuite$Configuration clusters(java.lang.String)
meth public org.netbeans.junit.NbModuleSuite$Configuration enableClasspathModules(boolean)
meth public org.netbeans.junit.NbModuleSuite$Configuration enableModules(java.lang.String)
meth public org.netbeans.junit.NbModuleSuite$Configuration enableModules(java.lang.String,java.lang.String)
meth public org.netbeans.junit.NbModuleSuite$Configuration failOnException(java.util.logging.Level)
meth public org.netbeans.junit.NbModuleSuite$Configuration failOnMessage(java.util.logging.Level)
meth public org.netbeans.junit.NbModuleSuite$Configuration gui(boolean)
meth public org.netbeans.junit.NbModuleSuite$Configuration hideExtraModules(boolean)
meth public org.netbeans.junit.NbModuleSuite$Configuration honorAutoloadEager(boolean)
meth public org.netbeans.junit.NbModuleSuite$Configuration parentClassLoader(java.lang.ClassLoader)
meth public org.netbeans.junit.NbModuleSuite$Configuration reuseUserDir(boolean)
supr java.lang.Object
hfds clusterRegExp,enableClasspathModules,failOnException,failOnMessage,gui,hideExtraModules,honorAutoEager,latestTestCaseClass,moduleRegExp,parentClassLoader,reuseUserDir,startupArgs,tests

CLSS public abstract interface org.netbeans.junit.NbPerformanceTest
innr public static PerformanceData
intf org.netbeans.junit.NbTest
meth public abstract org.netbeans.junit.NbPerformanceTest$PerformanceData[] getPerformanceData()

CLSS public static org.netbeans.junit.NbPerformanceTest$PerformanceData
 outer org.netbeans.junit.NbPerformanceTest
cons public init()
fld public final static int NO_ORDER = 0
fld public final static long NO_THRESHOLD = 0
fld public int runOrder
fld public java.lang.String name
fld public java.lang.String unit
fld public long threshold
fld public long value
supr java.lang.Object

CLSS public org.netbeans.junit.NbPerformanceTestCase
cons public init(java.lang.String)
intf org.netbeans.junit.NbPerformanceTest
meth public org.netbeans.junit.NbPerformanceTest$PerformanceData[] getPerformanceData()
meth public void reportPerformance(java.lang.String,long)
meth public void reportPerformance(java.lang.String,long,java.lang.String,int)
meth public void reportPerformance(java.lang.String,long,java.lang.String,int,long)
meth public void reportPerformance(long)
meth public void reportPerformance(long,java.lang.String)
supr org.netbeans.junit.NbTestCase
hfds data

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

CLSS public org.netbeans.junit.NbTestDecorator
cons public init(junit.framework.Test)
intf org.netbeans.junit.NbTest
meth public boolean canRun()
meth public java.lang.String getExpectedFail()
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
meth public void setFilter(org.netbeans.junit.Filter)
supr junit.extensions.TestDecorator

CLSS public org.netbeans.junit.NbTestSetup
cons public init(junit.framework.Test)
meth protected void setUp() throws java.lang.Exception
meth protected void tearDown() throws java.lang.Exception
meth public void run(junit.framework.TestResult)
supr org.netbeans.junit.NbTestDecorator

CLSS public org.netbeans.junit.NbTestSuite
cons public init()
cons public init(java.lang.Class<? extends junit.framework.TestCase>)
cons public init(java.lang.String)
intf org.netbeans.junit.NbTest
meth public boolean canRun()
meth public java.lang.String getExpectedFail()
meth public static org.netbeans.junit.NbTestSuite linearSpeedSuite(java.lang.Class<? extends junit.framework.TestCase>,int,int)
meth public static org.netbeans.junit.NbTestSuite speedSuite(java.lang.Class<? extends junit.framework.TestCase>,int,int)
meth public void addTest(junit.framework.Test)
meth public void addTestSuite(java.lang.Class<? extends junit.framework.TestCase>)
meth public void setFilter(org.netbeans.junit.Filter)
supr junit.framework.TestSuite
hfds fFilter
hcls APIJail,SpeedSuite

CLSS public abstract org.netbeans.junit.ParametricTestCase
cons public init()
cons public init(java.lang.String)
meth protected void parametrize(java.lang.Object)
supr org.netbeans.junit.MultiTestCase

CLSS public abstract org.netbeans.junit.ParametricTestSuite
cons public init()
cons public init(java.lang.String)
meth protected abstract java.lang.Object[] getParameters()
meth protected abstract org.netbeans.junit.ParametricTestCase[] cases(java.lang.Object)
meth protected final org.netbeans.junit.MultiTestCase nextTestCase()
meth protected void runAllTests(junit.framework.TestResult)
supr org.netbeans.junit.MultiTestSuite

CLSS public abstract interface !annotation org.netbeans.junit.RandomlyFails
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.netbeans.junit.diff.Diff
meth public abstract boolean diff(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth public abstract boolean diff(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException

CLSS public org.netbeans.junit.diff.DiffException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

CLSS public org.netbeans.junit.diff.LineDiff
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
fld public static int CONTEXT
intf org.netbeans.junit.diff.Diff
meth protected boolean compareLines(java.lang.String,java.lang.String)
meth public boolean diff(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth public boolean diff(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean getIgnoreCase()
meth public int getNContextLines()
supr java.lang.Object
hfds contextLines,ignoreCase,ignoreEmptyLines
hcls Result

CLSS public org.netbeans.junit.diff.NativeDiff
cons public init()
intf org.netbeans.junit.diff.Diff
meth public boolean diff(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth public boolean diff(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String getCmdLine()
meth public void setCmdLine(java.lang.String)
supr java.lang.Object
hfds diffcmd
hcls StreamGobbler

CLSS public org.netbeans.junit.diff.SimpleDiff
cons public init()
intf org.netbeans.junit.diff.Diff
meth protected boolean binaryCompare(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth protected boolean isBinaryFile(java.io.File) throws java.io.IOException
meth protected boolean textualCompare(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth public boolean diff(java.io.File,java.io.File,java.io.File) throws java.io.IOException
meth public boolean diff(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds BUFSIZE,lineDiff

