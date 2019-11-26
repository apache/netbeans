#Signature file v4.1
#Version 1.20

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
cons public init(java.lang.ThreadGroup,java.lang.String)
fld public final static int MAX_PRIORITY = 10
fld public final static int MIN_PRIORITY = 1
fld public final static int NORM_PRIORITY = 5
innr public abstract interface static UncaughtExceptionHandler
innr public final static !enum State
intf java.lang.Runnable
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean isInterrupted()
meth public final boolean isAlive()
meth public final boolean isDaemon()
meth public final int getPriority()
meth public final java.lang.String getName()
meth public final java.lang.ThreadGroup getThreadGroup()
meth public final void checkAccess()
meth public final void join() throws java.lang.InterruptedException
meth public final void join(long) throws java.lang.InterruptedException
meth public final void join(long,int) throws java.lang.InterruptedException
meth public final void resume()
 anno 0 java.lang.Deprecated()
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated()
meth public final void stop(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void suspend()
 anno 0 java.lang.Deprecated()
meth public int countStackFrames()
 anno 0 java.lang.Deprecated()
meth public java.lang.ClassLoader getContextClassLoader()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String toString()
meth public java.lang.Thread$State getState()
meth public java.lang.Thread$UncaughtExceptionHandler getUncaughtExceptionHandler()
meth public long getId()
meth public static boolean holdsLock(java.lang.Object)
meth public static boolean interrupted()
meth public static int activeCount()
meth public static int enumerate(java.lang.Thread[])
meth public static java.lang.Thread currentThread()
meth public static java.lang.Thread$UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
meth public static java.util.Map<java.lang.Thread,java.lang.StackTraceElement[]> getAllStackTraces()
meth public static void dumpStack()
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void destroy()
 anno 0 java.lang.Deprecated()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
supr java.lang.Object
hfds EMPTY_STACK_TRACE,SUBCLASS_IMPLEMENTATION_PERMISSION,blocker,blockerLock,contextClassLoader,daemon,defaultUncaughtExceptionHandler,eetop,group,inheritableThreadLocals,inheritedAccessControlContext,name,nativeParkEventPointer,parkBlocker,priority,single_step,stackSize,stillborn,target,threadInitNumber,threadLocalRandomProbe,threadLocalRandomSecondarySeed,threadLocalRandomSeed,threadLocals,threadQ,threadSeqNumber,threadStatus,tid,uncaughtExceptionHandler
hcls Caches,WeakClassKey

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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

CLSS public org.testng.Assert
cons protected init()
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(boolean,boolean,java.lang.String)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(byte,byte,java.lang.String)
meth public static void assertEquals(byte[],byte[])
meth public static void assertEquals(byte[],byte[],java.lang.String)
meth public static void assertEquals(char,char)
meth public static void assertEquals(char,char,java.lang.String)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(double,double,double,java.lang.String)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(float,float,float,java.lang.String)
meth public static void assertEquals(int,int)
meth public static void assertEquals(int,int,java.lang.String)
meth public static void assertEquals(java.lang.Iterable<?>,java.lang.Iterable<?>)
meth public static void assertEquals(java.lang.Iterable<?>,java.lang.Iterable<?>,java.lang.String)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertEquals(java.lang.Object[],java.lang.Object[])
meth public static void assertEquals(java.lang.Object[],java.lang.Object[],java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.util.Collection<?>,java.util.Collection<?>)
meth public static void assertEquals(java.util.Collection<?>,java.util.Collection<?>,java.lang.String)
meth public static void assertEquals(java.util.Iterator<?>,java.util.Iterator<?>)
meth public static void assertEquals(java.util.Iterator<?>,java.util.Iterator<?>,java.lang.String)
meth public static void assertEquals(java.util.Map<?,?>,java.util.Map<?,?>)
meth public static void assertEquals(java.util.Set<?>,java.util.Set<?>)
meth public static void assertEquals(java.util.Set<?>,java.util.Set<?>,java.lang.String)
meth public static void assertEquals(long,long)
meth public static void assertEquals(long,long,java.lang.String)
meth public static void assertEquals(short,short)
meth public static void assertEquals(short,short,java.lang.String)
meth public static void assertEqualsNoOrder(java.lang.Object[],java.lang.Object[])
meth public static void assertEqualsNoOrder(java.lang.Object[],java.lang.Object[],java.lang.String)
meth public static void assertFalse(boolean)
meth public static void assertFalse(boolean,java.lang.String)
meth public static void assertNotEquals(double,double,double)
meth public static void assertNotEquals(double,double,double,java.lang.String)
meth public static void assertNotEquals(float,float,float)
meth public static void assertNotEquals(float,float,float,java.lang.String)
meth public static void assertNotEquals(java.lang.Object,java.lang.Object)
meth public static void assertNotEquals(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.Object,java.lang.String)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.Object,java.lang.String)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertTrue(boolean)
meth public static void assertTrue(boolean,java.lang.String)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void fail(java.lang.String,java.lang.Throwable)
supr java.lang.Object

CLSS public org.testng.AssertJUnit
cons protected init()
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(byte[],byte[])
meth public static void assertEquals(char,char)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(int,int)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.String,boolean,boolean)
meth public static void assertEquals(java.lang.String,byte,byte)
meth public static void assertEquals(java.lang.String,byte[],byte[])
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
supr org.testng.internal.junit.ArrayAsserts

CLSS public org.testng.ClassMethodMap
cons public init(java.util.List<org.testng.ITestNGMethod>,org.testng.internal.XmlMethodSelector)
meth public boolean removeAndCheckIfLast(org.testng.ITestNGMethod,java.lang.Object)
meth public java.util.Map<org.testng.ITestClass,java.util.Set<java.lang.Object>> getInvokedAfterClassMethods()
meth public java.util.Map<org.testng.ITestClass,java.util.Set<java.lang.Object>> getInvokedBeforeClassMethods()
meth public void clear()
supr java.lang.Object
hfds m_afterClassMethods,m_beforeClassMethods,m_classMap

CLSS public org.testng.CommandLineArgs
cons public init()
fld public final static java.lang.Integer SUITE_THREAD_POOL_SIZE_DEFAULT
fld public final static java.lang.String CONFIG_FAILURE_POLICY = "-configfailurepolicy"
fld public final static java.lang.String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount"
fld public final static java.lang.String DEBUG = "-debug"
fld public final static java.lang.String EXCLUDED_GROUPS = "-excludegroups"
fld public final static java.lang.String GROUPS = "-groups"
fld public final static java.lang.String HOST = "-host"
fld public final static java.lang.String JUNIT = "-junit"
fld public final static java.lang.String LISTENER = "-listener"
fld public final static java.lang.String LOG = "-log"
fld public final static java.lang.String MASTER = "-master"
fld public final static java.lang.String METHODS = "-methods"
fld public final static java.lang.String METHOD_SELECTORS = "-methodselectors"
fld public final static java.lang.String MIXED = "-mixed"
fld public final static java.lang.String OBJECT_FACTORY = "-objectfactory"
fld public final static java.lang.String OUTPUT_DIRECTORY = "-d"
fld public final static java.lang.String PARALLEL = "-parallel"
fld public final static java.lang.String PORT = "-port"
fld public final static java.lang.String RANDOMIZE_SUITES = "-randomizesuites"
fld public final static java.lang.String REPORTER = "-reporter"
fld public final static java.lang.String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts"
fld public final static java.lang.String SLAVE = "-slave"
fld public final static java.lang.String SUITE_NAME = "-suitename"
fld public final static java.lang.String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize"
fld public final static java.lang.String TEST_CLASS = "-testclass"
fld public final static java.lang.String TEST_JAR = "-testjar"
fld public final static java.lang.String TEST_NAME = "-testname"
fld public final static java.lang.String TEST_NAMES = "-testnames"
fld public final static java.lang.String TEST_RUNNER_FACTORY = "-testrunfactory"
fld public final static java.lang.String THREAD_COUNT = "-threadcount"
fld public final static java.lang.String USE_DEFAULT_LISTENERS = "-usedefaultlisteners"
fld public final static java.lang.String VERBOSE = "-verbose"
fld public final static java.lang.String XML_PATH_IN_JAR = "-xmlpathinjar"
fld public final static java.lang.String XML_PATH_IN_JAR_DEFAULT = "testng.xml"
fld public java.lang.Boolean debug
fld public java.lang.Boolean junit
fld public java.lang.Boolean mixed
fld public java.lang.Boolean randomizeSuites
fld public java.lang.Boolean skipFailedInvocationCounts
fld public java.lang.Integer dataProviderThreadCount
fld public java.lang.Integer port
fld public java.lang.Integer suiteThreadPoolSize
fld public java.lang.Integer threadCount
fld public java.lang.Integer verbose
fld public java.lang.String configFailurePolicy
fld public java.lang.String excludedGroups
fld public java.lang.String groups
fld public java.lang.String host
fld public java.lang.String listener
fld public java.lang.String master
fld public java.lang.String methodSelectors
fld public java.lang.String objectFactory
fld public java.lang.String outputDirectory
fld public java.lang.String parallelMode
fld public java.lang.String reporter
fld public java.lang.String slave
fld public java.lang.String suiteName
fld public java.lang.String testClass
fld public java.lang.String testJar
fld public java.lang.String testName
fld public java.lang.String testNames
fld public java.lang.String testRunnerFactory
fld public java.lang.String useDefaultListeners
fld public java.lang.String xmlPathInJar
fld public java.util.List<java.lang.String> commandLineMethods
fld public java.util.List<java.lang.String> suiteFiles
supr java.lang.Object

CLSS public org.testng.ConversionUtils
cons public init()
meth public static java.lang.Object[] wrapDataProvider(java.lang.Class,java.util.Collection<java.lang.Object[]>)
supr java.lang.Object

CLSS public org.testng.Converter
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
supr java.lang.Object
hfds m_files,m_outputDirectory

CLSS public org.testng.DependencyMap
cons public init(org.testng.ITestNGMethod[])
meth public java.util.List<org.testng.ITestNGMethod> getMethodsThatBelongTo(java.lang.String,org.testng.ITestNGMethod)
meth public org.testng.ITestNGMethod getMethodDependingOn(java.lang.String,org.testng.ITestNGMethod)
supr java.lang.Object
hfds m_dependencies,m_groups

CLSS public org.testng.FileAssert
meth public static void assertDirectory(java.io.File)
meth public static void assertDirectory(java.io.File,java.lang.String)
meth public static void assertFile(java.io.File)
meth public static void assertFile(java.io.File,java.lang.String)
meth public static void assertLength(java.io.File,long)
meth public static void assertLength(java.io.File,long,java.lang.String)
meth public static void assertMaxLength(java.io.File,long)
meth public static void assertMaxLength(java.io.File,long,java.lang.String)
meth public static void assertMinLength(java.io.File,long)
meth public static void assertMinLength(java.io.File,long,java.lang.String)
meth public static void assertReadWrite(java.io.File)
meth public static void assertReadWrite(java.io.File,java.lang.String)
meth public static void assertReadable(java.io.File)
meth public static void assertReadable(java.io.File,java.lang.String)
meth public static void assertWriteable(java.io.File)
meth public static void assertWriteable(java.io.File,java.lang.String)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void fail(java.lang.String,java.lang.Throwable)
supr java.lang.Object

CLSS public abstract interface org.testng.IAnnotationTransformer
intf org.testng.ITestNGListener
meth public abstract void transform(org.testng.annotations.ITestAnnotation,java.lang.Class,java.lang.reflect.Constructor,java.lang.reflect.Method)

CLSS public abstract interface org.testng.IAnnotationTransformer2
intf org.testng.IAnnotationTransformer
meth public abstract void transform(org.testng.annotations.IConfigurationAnnotation,java.lang.Class,java.lang.reflect.Constructor,java.lang.reflect.Method)
meth public abstract void transform(org.testng.annotations.IDataProviderAnnotation,java.lang.reflect.Method)
meth public abstract void transform(org.testng.annotations.IFactoryAnnotation,java.lang.reflect.Method)

CLSS public abstract interface org.testng.IAttributes
intf java.io.Serializable
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.Object removeAttribute(java.lang.String)
meth public abstract java.util.Set<java.lang.String> getAttributeNames()
meth public abstract void setAttribute(java.lang.String,java.lang.Object)

CLSS public abstract interface org.testng.IClass
intf java.io.Serializable
meth public abstract int getInstanceCount()
meth public abstract java.lang.Class getRealClass()
meth public abstract java.lang.Object[] getInstances(boolean)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTestName()
meth public abstract long[] getInstanceHashCodes()
meth public abstract org.testng.xml.XmlClass getXmlClass()
meth public abstract org.testng.xml.XmlTest getXmlTest()
meth public abstract void addInstance(java.lang.Object)

CLSS public abstract interface org.testng.IConfigurable
intf org.testng.ITestNGListener
meth public abstract void run(org.testng.IConfigureCallBack,org.testng.ITestResult)

CLSS public abstract interface org.testng.IConfigurationListener
intf org.testng.ITestNGListener
meth public abstract void onConfigurationFailure(org.testng.ITestResult)
meth public abstract void onConfigurationSkip(org.testng.ITestResult)
meth public abstract void onConfigurationSuccess(org.testng.ITestResult)

CLSS public abstract interface org.testng.IConfigurationListener2
intf org.testng.IConfigurationListener
meth public abstract void beforeConfiguration(org.testng.ITestResult)

CLSS public abstract interface org.testng.IConfigureCallBack
meth public abstract java.lang.Object[] getParameters()
meth public abstract void runConfigurationMethod(org.testng.ITestResult)

CLSS public abstract interface org.testng.IExecutionListener
intf org.testng.ITestNGListener
meth public abstract void onExecutionFinish()
meth public abstract void onExecutionStart()

CLSS public abstract interface org.testng.IExtraOutput
intf java.io.Serializable
meth public abstract java.util.List<java.lang.String> getParameterOutput()

CLSS public abstract interface org.testng.IHookCallBack
meth public abstract java.lang.Object[] getParameters()
meth public abstract void runTestMethod(org.testng.ITestResult)

CLSS public abstract interface org.testng.IHookable
intf org.testng.ITestNGListener
meth public abstract void run(org.testng.IHookCallBack,org.testng.ITestResult)

CLSS public abstract interface org.testng.IInstanceInfo
meth public abstract java.lang.Class getInstanceClass()
meth public abstract java.lang.Object getInstance()

CLSS public abstract interface org.testng.IInvokedMethod
meth public abstract boolean isConfigurationMethod()
meth public abstract boolean isTestMethod()
meth public abstract long getDate()
meth public abstract org.testng.ITestNGMethod getTestMethod()
meth public abstract org.testng.ITestResult getTestResult()

CLSS public abstract interface org.testng.IInvokedMethodListener
intf org.testng.ITestNGListener
meth public abstract void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public abstract void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)

CLSS public abstract interface org.testng.IInvokedMethodListener2
intf org.testng.IInvokedMethodListener
meth public abstract void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult,org.testng.ITestContext)
meth public abstract void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult,org.testng.ITestContext)

CLSS public abstract interface org.testng.IMethodInstance
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.Object[] getInstances()
meth public abstract org.testng.ITestNGMethod getMethod()

CLSS public abstract interface org.testng.IMethodInterceptor
intf org.testng.ITestNGListener
meth public abstract java.util.List<org.testng.IMethodInstance> intercept(java.util.List<org.testng.IMethodInstance>,org.testng.ITestContext)

CLSS public abstract interface org.testng.IMethodSelector
intf java.io.Serializable
meth public abstract boolean includeMethod(org.testng.IMethodSelectorContext,org.testng.ITestNGMethod,boolean)
meth public abstract void setTestMethods(java.util.List<org.testng.ITestNGMethod>)

CLSS public abstract interface org.testng.IMethodSelectorContext
meth public abstract boolean isStopped()
meth public abstract java.util.Map<java.lang.Object,java.lang.Object> getUserData()
meth public abstract void setStopped(boolean)

CLSS public abstract interface org.testng.IModuleFactory
meth public abstract com.google.inject.Module createModule(org.testng.ITestContext,java.lang.Class<?>)

CLSS public abstract interface org.testng.IObjectFactory
intf org.testng.ITestObjectFactory
meth public abstract !varargs java.lang.Object newInstance(java.lang.reflect.Constructor,java.lang.Object[])

CLSS public abstract interface org.testng.IObjectFactory2
intf org.testng.ITestObjectFactory
meth public abstract java.lang.Object newInstance(java.lang.Class<?>)

CLSS public abstract interface org.testng.IReporter
intf org.testng.ITestNGListener
meth public abstract void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)

CLSS public abstract interface org.testng.IResultMap
intf java.io.Serializable
meth public abstract int size()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getAllMethods()
meth public abstract java.util.Set<org.testng.ITestResult> getAllResults()
meth public abstract java.util.Set<org.testng.ITestResult> getResults(org.testng.ITestNGMethod)
meth public abstract void addResult(org.testng.ITestResult,org.testng.ITestNGMethod)
meth public abstract void removeResult(org.testng.ITestNGMethod)

CLSS public abstract interface org.testng.IRetryAnalyzer
meth public abstract boolean retry(org.testng.ITestResult)

CLSS public abstract interface org.testng.ISuite
intf org.testng.IAttributes
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOutputDirectory()
meth public abstract java.lang.String getParallel()
meth public abstract java.lang.String getParameter(java.lang.String)
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getInvokedMethods()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.List<org.testng.IInvokedMethod> getAllInvokedMethods()
meth public abstract java.util.List<org.testng.ITestNGMethod> getAllMethods()
meth public abstract java.util.Map<java.lang.String,java.util.Collection<org.testng.ITestNGMethod>> getMethodsByGroups()
meth public abstract java.util.Map<java.lang.String,org.testng.ISuiteResult> getResults()
meth public abstract org.testng.IObjectFactory getObjectFactory()
meth public abstract org.testng.IObjectFactory2 getObjectFactory2()
meth public abstract org.testng.SuiteRunState getSuiteState()
meth public abstract org.testng.internal.annotations.IAnnotationFinder getAnnotationFinder()
meth public abstract org.testng.xml.XmlSuite getXmlSuite()
meth public abstract void addListener(org.testng.ITestNGListener)
meth public abstract void run()

CLSS public abstract interface org.testng.ISuiteListener
intf org.testng.ITestNGListener
meth public abstract void onFinish(org.testng.ISuite)
meth public abstract void onStart(org.testng.ISuite)

CLSS public abstract interface org.testng.ISuiteResult
intf java.io.Serializable
meth public abstract java.lang.String getPropertyFileName()
meth public abstract org.testng.ITestContext getTestContext()

CLSS public abstract interface org.testng.ITest
meth public abstract java.lang.String getTestName()

CLSS public abstract interface org.testng.ITestClass
intf java.io.Serializable
intf org.testng.IClass
meth public abstract int getInstanceCount()
meth public abstract java.lang.Object[] getInstances(boolean)
meth public abstract long[] getInstanceHashCodes()
meth public abstract org.testng.ITestNGMethod[] getAfterClassMethods()
meth public abstract org.testng.ITestNGMethod[] getAfterGroupsMethods()
meth public abstract org.testng.ITestNGMethod[] getAfterSuiteMethods()
meth public abstract org.testng.ITestNGMethod[] getAfterTestConfigurationMethods()
meth public abstract org.testng.ITestNGMethod[] getAfterTestMethods()
meth public abstract org.testng.ITestNGMethod[] getBeforeClassMethods()
meth public abstract org.testng.ITestNGMethod[] getBeforeGroupsMethods()
meth public abstract org.testng.ITestNGMethod[] getBeforeSuiteMethods()
meth public abstract org.testng.ITestNGMethod[] getBeforeTestConfigurationMethods()
meth public abstract org.testng.ITestNGMethod[] getBeforeTestMethods()
meth public abstract org.testng.ITestNGMethod[] getTestMethods()

CLSS public abstract interface org.testng.ITestClassFinder
meth public abstract org.testng.IClass getIClass(java.lang.Class)
meth public abstract org.testng.IClass[] findTestClasses()

CLSS public abstract interface org.testng.ITestContext
intf org.testng.IAttributes
meth public abstract com.google.inject.Injector getInjector(java.util.List<com.google.inject.Module>)
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOutputDirectory()
meth public abstract java.lang.String[] getExcludedGroups()
meth public abstract java.lang.String[] getIncludedGroups()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public abstract java.util.Date getEndDate()
meth public abstract java.util.Date getStartDate()
meth public abstract java.util.List<com.google.inject.Module> getGuiceModules(java.lang.Class<? extends com.google.inject.Module>)
meth public abstract org.testng.IResultMap getFailedButWithinSuccessPercentageTests()
meth public abstract org.testng.IResultMap getFailedConfigurations()
meth public abstract org.testng.IResultMap getFailedTests()
meth public abstract org.testng.IResultMap getPassedConfigurations()
meth public abstract org.testng.IResultMap getPassedTests()
meth public abstract org.testng.IResultMap getSkippedConfigurations()
meth public abstract org.testng.IResultMap getSkippedTests()
meth public abstract org.testng.ISuite getSuite()
meth public abstract org.testng.ITestNGMethod[] getAllTestMethods()
meth public abstract org.testng.xml.XmlTest getCurrentXmlTest()
meth public abstract void addGuiceModule(java.lang.Class<? extends com.google.inject.Module>,com.google.inject.Module)
meth public abstract void addInjector(java.util.List<com.google.inject.Module>,com.google.inject.Injector)

CLSS public abstract interface org.testng.ITestListener
intf org.testng.ITestNGListener
meth public abstract void onFinish(org.testng.ITestContext)
meth public abstract void onStart(org.testng.ITestContext)
meth public abstract void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public abstract void onTestFailure(org.testng.ITestResult)
meth public abstract void onTestSkipped(org.testng.ITestResult)
meth public abstract void onTestStart(org.testng.ITestResult)
meth public abstract void onTestSuccess(org.testng.ITestResult)

CLSS public abstract interface org.testng.ITestMethodFinder
meth public abstract org.testng.ITestNGMethod[] getAfterClassMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getAfterGroupsConfigurationMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getAfterSuiteMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getAfterTestConfigurationMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getAfterTestMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getBeforeClassMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getBeforeGroupsConfigurationMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getBeforeSuiteMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getBeforeTestConfigurationMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getBeforeTestMethods(java.lang.Class)
meth public abstract org.testng.ITestNGMethod[] getTestMethods(java.lang.Class,org.testng.xml.XmlTest)

CLSS public abstract interface org.testng.ITestNGListener

CLSS public abstract interface org.testng.ITestNGListenerFactory
meth public abstract org.testng.ITestNGListener createListener(java.lang.Class<? extends org.testng.ITestNGListener>)

CLSS public abstract interface org.testng.ITestNGMethod
intf java.io.Serializable
intf java.lang.Cloneable
intf java.lang.Comparable
meth public abstract boolean canRunFromClass(org.testng.IClass)
meth public abstract boolean getEnabled()
meth public abstract boolean ignoreMissingDependencies()
meth public abstract boolean isAfterClassConfiguration()
meth public abstract boolean isAfterGroupsConfiguration()
meth public abstract boolean isAfterMethodConfiguration()
meth public abstract boolean isAfterSuiteConfiguration()
meth public abstract boolean isAfterTestConfiguration()
meth public abstract boolean isAlwaysRun()
meth public abstract boolean isBeforeClassConfiguration()
meth public abstract boolean isBeforeGroupsConfiguration()
meth public abstract boolean isBeforeMethodConfiguration()
meth public abstract boolean isBeforeSuiteConfiguration()
meth public abstract boolean isBeforeTestConfiguration()
meth public abstract boolean isTest()
meth public abstract boolean skipFailedInvocations()
meth public abstract int getCurrentInvocationCount()
meth public abstract int getInvocationCount()
meth public abstract int getParameterInvocationCount()
meth public abstract int getPriority()
meth public abstract int getSuccessPercentage()
meth public abstract int getThreadPoolSize()
meth public abstract java.lang.Class getRealClass()
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.Object[] getInstances()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getMethodName()
meth public abstract java.lang.String getMissingGroup()
meth public abstract java.lang.String[] getAfterGroups()
meth public abstract java.lang.String[] getBeforeGroups()
meth public abstract java.lang.String[] getGroups()
meth public abstract java.lang.String[] getGroupsDependedUpon()
meth public abstract java.lang.String[] getMethodsDependedUpon()
meth public abstract java.lang.reflect.Method getMethod()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.List<java.lang.Integer> getFailedInvocationNumbers()
meth public abstract java.util.List<java.lang.Integer> getInvocationNumbers()
meth public abstract java.util.Map<java.lang.String,java.lang.String> findMethodParameters(org.testng.xml.XmlTest)
meth public abstract long getDate()
meth public abstract long getInvocationTimeOut()
meth public abstract long getTimeOut()
meth public abstract long[] getInstanceHashCodes()
meth public abstract org.testng.IRetryAnalyzer getRetryAnalyzer()
meth public abstract org.testng.ITestClass getTestClass()
meth public abstract org.testng.ITestNGMethod clone()
meth public abstract org.testng.internal.ConstructorOrMethod getConstructorOrMethod()
meth public abstract org.testng.xml.XmlTest getXmlTest()
meth public abstract void addFailedInvocationNumber(int)
meth public abstract void addMethodDependedUpon(java.lang.String)
meth public abstract void incrementCurrentInvocationCount()
meth public abstract void setDate(long)
meth public abstract void setId(java.lang.String)
meth public abstract void setIgnoreMissingDependencies(boolean)
meth public abstract void setInvocationCount(int)
meth public abstract void setInvocationNumbers(java.util.List<java.lang.Integer>)
meth public abstract void setMissingGroup(java.lang.String)
meth public abstract void setParameterInvocationCount(int)
meth public abstract void setPriority(int)
meth public abstract void setRetryAnalyzer(org.testng.IRetryAnalyzer)
meth public abstract void setSkipFailedInvocations(boolean)
meth public abstract void setTestClass(org.testng.ITestClass)
meth public abstract void setThreadPoolSize(int)
meth public abstract void setTimeOut(long)

CLSS public abstract interface org.testng.ITestObjectFactory
intf java.io.Serializable

CLSS public abstract interface org.testng.ITestResult
fld public final static int FAILURE = 2
fld public final static int SKIP = 3
fld public final static int STARTED = 16
fld public final static int SUCCESS = 1
fld public final static int SUCCESS_PERCENTAGE_FAILURE = 4
intf java.lang.Comparable<org.testng.ITestResult>
intf org.testng.IAttributes
meth public abstract boolean isSuccess()
meth public abstract int getStatus()
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.Object[] getParameters()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getInstanceName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTestName()
meth public abstract java.lang.Throwable getThrowable()
meth public abstract long getEndMillis()
meth public abstract long getStartMillis()
meth public abstract org.testng.IClass getTestClass()
meth public abstract org.testng.ITestContext getTestContext()
meth public abstract org.testng.ITestNGMethod getMethod()
meth public abstract void setEndMillis(long)
meth public abstract void setParameters(java.lang.Object[])
meth public abstract void setStatus(int)
meth public abstract void setThrowable(java.lang.Throwable)

CLSS public abstract interface org.testng.ITestRunnerFactory
meth public abstract org.testng.TestRunner newTestRunner(org.testng.ISuite,org.testng.xml.XmlTest,java.util.List<org.testng.IInvokedMethodListener>)

CLSS public org.testng.Reporter
cons public init()
meth public static java.util.List<java.lang.String> getOutput()
meth public static java.util.List<java.lang.String> getOutput(org.testng.ITestResult)
meth public static org.testng.ITestResult getCurrentTestResult()
meth public static void clear()
meth public static void log(java.lang.String)
meth public static void log(java.lang.String,boolean)
meth public static void log(java.lang.String,int)
meth public static void log(java.lang.String,int,boolean)
meth public static void setCurrentTestResult(org.testng.ITestResult)
meth public static void setEscapeHtml(boolean)
supr java.lang.Object
hfds m_currentTestResult,m_escapeHtml,m_methodOutputMap,m_output

CLSS public org.testng.ReporterConfig
cons public init()
innr public static Property
meth public java.lang.Object newReporterInstance()
meth public java.lang.String getClassName()
meth public java.lang.String serialize()
meth public java.lang.String toString()
meth public java.util.List<org.testng.ReporterConfig$Property> getProperties()
meth public static org.testng.ReporterConfig deserialize(java.lang.String)
meth public void addProperty(org.testng.ReporterConfig$Property)
meth public void setClassName(java.lang.String)
supr java.lang.Object
hfds m_className,m_properties

CLSS public static org.testng.ReporterConfig$Property
 outer org.testng.ReporterConfig
cons public init()
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds name,value

CLSS public org.testng.SkipException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth protected void reduceStackTrace()
meth protected void restoreStackTrace()
meth public boolean isSkip()
supr java.lang.RuntimeException
hfds m_stackReduced,m_stackTrace,serialVersionUID

CLSS public org.testng.SuiteRunState
cons public init()
intf java.io.Serializable
meth public boolean isFailed()
meth public void failed()
supr java.lang.Object
hfds m_hasFailures,serialVersionUID

CLSS public org.testng.SuiteRunner
cons protected init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory,boolean,org.testng.IMethodInterceptor,java.util.List<org.testng.IInvokedMethodListener>,java.util.List<org.testng.ITestListener>)
cons public init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String)
cons public init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory)
cons public init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory,boolean)
intf java.io.Serializable
intf org.testng.IInvokedMethodListener
intf org.testng.ISuite
meth protected void addListener(org.testng.ISuiteListener)
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.Object removeAttribute(java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getName()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String getParallel()
meth public java.lang.String getParameter(java.lang.String)
meth public java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public java.util.Collection<org.testng.ITestNGMethod> getInvokedMethods()
meth public java.util.List<org.testng.IInvokedMethod> getAllInvokedMethods()
meth public java.util.List<org.testng.IReporter> getReporters()
meth public java.util.List<org.testng.ITestNGMethod> getAllMethods()
meth public java.util.Map<java.lang.String,java.util.Collection<org.testng.ITestNGMethod>> getMethodsByGroups()
meth public java.util.Map<java.lang.String,org.testng.ISuiteResult> getResults()
meth public java.util.Set<java.lang.String> getAttributeNames()
meth public org.testng.IObjectFactory getObjectFactory()
meth public org.testng.IObjectFactory2 getObjectFactory2()
meth public org.testng.SuiteRunState getSuiteState()
meth public org.testng.internal.annotations.IAnnotationFinder getAnnotationFinder()
meth public org.testng.xml.XmlSuite getXmlSuite()
meth public static void ppp(java.lang.String)
meth public void addListener(org.testng.ITestNGListener)
meth public void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void run()
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setHost(java.lang.String)
meth public void setObjectFactory(org.testng.ITestObjectFactory)
meth public void setReportResults(boolean)
meth public void setSkipFailedInvocationCounts(java.lang.Boolean)
supr java.lang.Object
hfds DEFAULT_OUTPUT_DIR,m_allTestMethods,m_attributes,m_configuration,m_host,m_invokedMethodListeners,m_invokedMethods,m_listeners,m_methodInterceptor,m_objectFactory,m_outputDir,m_reporters,m_runnerFactory,m_skipFailedInvocationCounts,m_suite,m_suiteResults,m_suiteState,m_testListeners,m_testRunners,m_textReporter,m_tmpRunnerFactory,m_useDefaultListeners,serialVersionUID
hcls DefaultTestRunnerFactory,ProxyTestRunnerFactory,SuiteWorker

CLSS public org.testng.SuiteRunnerWorker
cons public init(org.testng.ISuite,org.testng.internal.SuiteRunnerMap,int,java.lang.String)
intf org.testng.internal.thread.graph.IWorker<org.testng.ISuite>
meth public int compareTo(org.testng.internal.thread.graph.IWorker<org.testng.ISuite>)
meth public int getPriority()
meth public java.lang.String toString()
meth public java.util.List<org.testng.ISuite> getTasks()
meth public long getTimeOut()
meth public void run()
supr java.lang.Object
hfds m_defaultSuiteName,m_suiteRunner,m_suiteRunnerMap,m_verbose

CLSS public org.testng.TestException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr org.testng.TestNGException
hfds serialVersionUID

CLSS public org.testng.TestListenerAdapter
cons public init()
intf org.testng.internal.IResultListener2
meth protected org.testng.ITestNGMethod[] getAllTestMethods()
meth public java.lang.String toString()
meth public java.util.List<org.testng.ITestContext> getTestContexts()
meth public java.util.List<org.testng.ITestResult> getConfigurationFailures()
meth public java.util.List<org.testng.ITestResult> getConfigurationSkips()
meth public java.util.List<org.testng.ITestResult> getFailedButWithinSuccessPercentageTests()
meth public java.util.List<org.testng.ITestResult> getFailedTests()
meth public java.util.List<org.testng.ITestResult> getPassedTests()
meth public java.util.List<org.testng.ITestResult> getSkippedTests()
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
meth public void setAllTestMethods(java.util.List<org.testng.ITestNGMethod>)
meth public void setFailedButWithinSuccessPercentageTests(java.util.List<org.testng.ITestResult>)
meth public void setFailedTests(java.util.List<org.testng.ITestResult>)
meth public void setPassedTests(java.util.List<org.testng.ITestResult>)
meth public void setSkippedTests(java.util.List<org.testng.ITestResult>)
supr java.lang.Object
hfds m_allTestMethods,m_failedButWSPerTests,m_failedConfs,m_failedTests,m_passedConfs,m_passedTests,m_skippedConfs,m_skippedTests,m_testContexts

CLSS public org.testng.TestNG
cons public init()
cons public init(boolean)
fld protected boolean m_useDefaultListeners
fld protected final static int HAS_FAILURE = 1
fld protected final static int HAS_FSP = 4
fld protected final static int HAS_NO_TEST = 8
fld protected final static int HAS_SKIPPED = 2
fld protected java.util.List<org.testng.xml.XmlSuite> m_suites
fld protected long m_end
fld protected long m_start
fld public final static java.lang.Integer DEFAULT_VERBOSE
fld public final static java.lang.String DEFAULT_COMMAND_LINE_SUITE_NAME = "Command line suite"
fld public final static java.lang.String DEFAULT_COMMAND_LINE_TEST_NAME = "Command line test"
fld public final static java.lang.String DEFAULT_OUTPUTDIR = "test-output"
fld public final static java.lang.String SHOW_TESTNG_STACK_FRAMES = "testng.show.stack.frames"
fld public final static java.lang.String TEST_CLASSPATH = "testng.test.classpath"
innr public static ExitCodeListener
meth protected long getEnd()
meth protected long getStart()
meth protected org.testng.internal.IConfiguration getConfiguration()
meth protected static void validateCommandLineParameters(org.testng.CommandLineArgs)
meth protected void configure(org.testng.CommandLineArgs)
meth protected void setTestRunnerFactory(org.testng.ITestRunnerFactory)
meth public boolean hasFailure()
meth public boolean hasFailureWithinSuccessPercentage()
meth public boolean hasSkip()
meth public int getStatus()
meth public java.lang.Integer getSuiteThreadPoolSize()
meth public java.lang.String getConfigFailurePolicy()
meth public java.lang.String getDefaultSuiteName()
meth public java.lang.String getDefaultTestName()
meth public java.lang.String getOutputDirectory()
meth public java.util.List<org.testng.ISuite> runSuitesLocally()
meth public java.util.List<org.testng.ISuiteListener> getSuiteListeners()
meth public java.util.List<org.testng.ITestListener> getTestListeners()
meth public java.util.List<org.testng.ITestNGListener> getServiceLoaderListeners()
meth public java.util.Set<org.testng.IReporter> getReporters()
meth public org.testng.IAnnotationTransformer getAnnotationTransformer()
meth public static boolean isJdk14()
 anno 0 java.lang.Deprecated()
meth public static org.testng.TestNG getDefault()
 anno 0 java.lang.Deprecated()
meth public static org.testng.TestNG privateMain(java.lang.String[],org.testng.ITestListener)
meth public static void main(java.lang.String[])
meth public static void setTestNGVersion()
 anno 0 java.lang.Deprecated()
meth public void addClassLoader(java.lang.ClassLoader)
meth public void addExecutionListener(org.testng.IExecutionListener)
meth public void addInvokedMethodListener(org.testng.IInvokedMethodListener)
meth public void addListener(java.lang.Object)
meth public void addListener(org.testng.IInvokedMethodListener)
meth public void addListener(org.testng.IReporter)
meth public void addListener(org.testng.ISuiteListener)
meth public void addListener(org.testng.ITestListener)
meth public void addMethodSelector(java.lang.String,int)
meth public void configure(java.util.Map)
 anno 0 java.lang.Deprecated()
meth public void initializeSuitesAndJarFile()
meth public void run()
meth public void setAnnotationTransformer(org.testng.IAnnotationTransformer)
meth public void setCommandLineSuite(org.testng.xml.XmlSuite)
meth public void setConfigFailurePolicy(java.lang.String)
meth public void setDataProviderThreadCount(int)
meth public void setDefaultSuiteName(java.lang.String)
meth public void setDefaultTestName(java.lang.String)
meth public void setExcludedGroups(java.lang.String)
meth public void setGroupByInstances(boolean)
meth public void setGroups(java.lang.String)
meth public void setHasFailure(boolean)
 anno 0 java.lang.Deprecated()
meth public void setHasFailureWithinSuccessPercentage(boolean)
 anno 0 java.lang.Deprecated()
meth public void setHasSkip(boolean)
 anno 0 java.lang.Deprecated()
meth public void setJUnit(java.lang.Boolean)
meth public void setListenerClasses(java.util.List<java.lang.Class>)
meth public void setMaster(java.lang.String)
meth public void setMethodInterceptor(org.testng.IMethodInterceptor)
meth public void setMixed(java.lang.Boolean)
meth public void setObjectFactory(java.lang.Class)
meth public void setObjectFactory(org.testng.ITestObjectFactory)
meth public void setOutputDirectory(java.lang.String)
meth public void setParallel(java.lang.String)
meth public void setPreserveOrder(boolean)
meth public void setRandomizeSuites(boolean)
meth public void setServiceLoaderClassLoader(java.net.URLClassLoader)
meth public void setSkipFailedInvocationCounts(java.lang.Boolean)
meth public void setSlave(java.lang.String)
meth public void setSourcePath(java.lang.String)
meth public void setSuiteThreadPoolSize(java.lang.Integer)
meth public void setTestClasses(java.lang.Class[])
meth public void setTestJar(java.lang.String)
meth public void setTestNames(java.util.List<java.lang.String>)
meth public void setTestSuites(java.util.List<java.lang.String>)
meth public void setThreadCount(int)
meth public void setUseDefaultListeners(boolean)
meth public void setVerbose(int)
meth public void setXmlPathInJar(java.lang.String)
meth public void setXmlSuites(java.util.List<org.testng.xml.XmlSuite>)
supr java.lang.Object
hfds LOGGER,m_annotationTransformer,m_cmdlineSuites,m_commandLineMethods,m_commandLineTestClasses,m_configFailurePolicy,m_configurable,m_configuration,m_dataProviderThreadCount,m_defaultAnnoProcessor,m_defaultSuiteName,m_defaultTestName,m_excludedGroups,m_executionListeners,m_groupByInstances,m_hasTests,m_hookable,m_includedGroups,m_instance,m_invokedMethodListeners,m_isInitialized,m_isJUnit,m_isMixed,m_jCommander,m_jarPath,m_masterfileName,m_methodDescriptors,m_methodInterceptor,m_objectFactory,m_outputDir,m_parallelMode,m_preserveOrder,m_randomizeSuites,m_reporters,m_serviceLoaderClassLoader,m_serviceLoaderListeners,m_skipFailedInvocationCounts,m_slavefileName,m_status,m_stringSuites,m_suiteListeners,m_suiteThreadPoolSize,m_testListeners,m_testNames,m_testRunnerFactory,m_threadCount,m_useParallelMode,m_useThreadCount,m_verbose,m_xmlPathInJar

CLSS public static org.testng.TestNG$ExitCodeListener
 outer org.testng.TestNG
cons public init()
cons public init(org.testng.TestNG)
intf org.testng.internal.IResultListener2
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object
hfds m_mainRunner

CLSS public org.testng.TestNGAntTask
hfds m_delegateCommandSystemProperties,m_dumpEnv,m_dumpSys,m_listeners,m_methods,m_methodselectors,m_objectFactory,m_skipFailedInvocationCounts,m_suiteName,m_suiteThreadPoolSize,m_testName,m_testNames,m_timeout,m_verbose,m_xmlPathInJar,mode,reporterConfigs
hcls TestNGLogOS

CLSS public final static !enum org.testng.TestNGAntTask$Mode
 outer org.testng.TestNGAntTask
fld public final static org.testng.TestNGAntTask$Mode junit
fld public final static org.testng.TestNGAntTask$Mode mixed
fld public final static org.testng.TestNGAntTask$Mode testng
meth public static org.testng.TestNGAntTask$Mode valueOf(java.lang.String)
meth public static org.testng.TestNGAntTask$Mode[] values()
supr java.lang.Enum<org.testng.TestNGAntTask$Mode>

CLSS protected static org.testng.TestNGAntTask$TestNGLogSH
 outer org.testng.TestNGAntTask

CLSS public org.testng.TestNGException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public org.testng.TestNGUtils
cons public init()
meth public static org.testng.ITestNGMethod createITestNGMethod(org.testng.ITestNGMethod,java.lang.reflect.Method)
supr java.lang.Object

CLSS public org.testng.TestRunner
cons protected init(org.testng.internal.IConfiguration,org.testng.ISuite,org.testng.xml.XmlTest,java.lang.String,org.testng.internal.annotations.IAnnotationFinder,boolean,java.util.List<org.testng.IInvokedMethodListener>)
cons public init(org.testng.internal.IConfiguration,org.testng.ISuite,org.testng.xml.XmlTest,boolean,java.util.List<org.testng.IInvokedMethodListener>)
intf org.testng.ITestContext
intf org.testng.internal.ITestResultNotifier
intf org.testng.internal.thread.graph.IThreadWorkerFactory<org.testng.ITestNGMethod>
meth public com.google.inject.Injector getInjector(java.util.List<com.google.inject.Module>)
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.Object removeAttribute(java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getName()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String[] getExcludedGroups()
meth public java.lang.String[] getIncludedGroups()
meth public java.util.Collection<org.testng.ITestClass> getTestClasses()
meth public java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public java.util.Date getEndDate()
meth public java.util.Date getStartDate()
meth public java.util.List<com.google.inject.Module> getGuiceModules(java.lang.Class<? extends com.google.inject.Module>)
meth public java.util.List<org.testng.IConfigurationListener> getConfigurationListeners()
meth public java.util.List<org.testng.ITestListener> getTestListeners()
meth public java.util.List<org.testng.ITestNGMethod> getInvokedMethods()
meth public java.util.List<org.testng.internal.thread.graph.IWorker<org.testng.ITestNGMethod>> createWorkers(java.util.List<org.testng.ITestNGMethod>)
meth public java.util.Set<java.lang.String> getAttributeNames()
meth public java.util.Set<org.testng.ITestResult> getFailedTests(org.testng.ITestNGMethod)
meth public java.util.Set<org.testng.ITestResult> getPassedTests(org.testng.ITestNGMethod)
meth public java.util.Set<org.testng.ITestResult> getSkippedTests(org.testng.ITestNGMethod)
meth public org.testng.IResultMap getFailedButWithinSuccessPercentageTests()
meth public org.testng.IResultMap getFailedConfigurations()
meth public org.testng.IResultMap getFailedTests()
meth public org.testng.IResultMap getPassedConfigurations()
meth public org.testng.IResultMap getPassedTests()
meth public org.testng.IResultMap getSkippedConfigurations()
meth public org.testng.IResultMap getSkippedTests()
meth public org.testng.ISuite getSuite()
meth public org.testng.ITestNGMethod[] getAfterSuiteMethods()
meth public org.testng.ITestNGMethod[] getAfterTestConfigurationMethods()
meth public org.testng.ITestNGMethod[] getAllTestMethods()
meth public org.testng.ITestNGMethod[] getBeforeSuiteMethods()
meth public org.testng.ITestNGMethod[] getBeforeTestConfigurationMethods()
meth public org.testng.internal.IInvoker getInvoker()
meth public org.testng.xml.XmlTest getCurrentXmlTest()
meth public org.testng.xml.XmlTest getTest()
meth public static int getVerbose()
meth public void addFailedButWithinSuccessPercentageTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addFailedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addGuiceModule(java.lang.Class<? extends com.google.inject.Module>,com.google.inject.Module)
meth public void addInjector(java.util.List<com.google.inject.Module>,com.google.inject.Injector)
meth public void addInvokedMethod(org.testng.internal.InvokedMethod)
meth public void addListener(java.lang.Object)
meth public void addPassedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addSkippedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addTestListener(org.testng.ITestListener)
meth public void run()
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setMethodInterceptor(org.testng.IMethodInterceptor)
meth public void setOutputDirectory(java.lang.String)
meth public void setTestName(java.lang.String)
meth public void setVerbose(int)
supr java.lang.Object
hfds m_afterSuiteMethods,m_afterXmlTestMethods,m_allTestMethods,m_annotationFinder,m_attributes,m_beforeSuiteMethods,m_beforeXmlTestMethods,m_classMap,m_classMethodMap,m_confListener,m_configuration,m_configurationListeners,m_endDate,m_excludedMethods,m_failedButWithinSuccessPercentageTests,m_failedConfigurations,m_failedTests,m_groupMethods,m_guiceModules,m_host,m_injectors,m_invokedMethodListeners,m_invokedMethods,m_invoker,m_metaGroups,m_methodInterceptor,m_outputDirectory,m_packageNamesFromXml,m_passedConfigurations,m_passedTests,m_runInfo,m_skipFailedInvocationCounts,m_skippedConfigurations,m_skippedTests,m_startDate,m_suite,m_testClassFinder,m_testClassesFromXml,m_testListeners,m_testName,m_verbose,m_xmlMethodSelector,m_xmlTest,serialVersionUID
hcls ConfigurationListener,ListenerHolder

CLSS public org.testng.TimeBombSkipException
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.util.Date)
cons public init(java.lang.String,java.util.Date,java.lang.String)
cons public init(java.lang.String,java.util.Date,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.util.Date,java.lang.Throwable)
meth public boolean isSkip()
meth public java.lang.String getMessage()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr org.testng.SkipException
hfds SDF,m_expireDate,m_inFormat,m_outFormat,serialVersionUID

CLSS public abstract interface !annotation org.testng.annotations.AfterClass
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.AfterGroups
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault java.lang.String[] value()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.AfterMethod
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault boolean lastTimeOnly()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.AfterSuite
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.AfterTest
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeClass
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeGroups
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault java.lang.String[] value()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeMethod
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean firstTimeOnly()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeSuite
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeTest
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.Configuration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean afterSuite()
meth public abstract !hasdefault boolean afterTest()
meth public abstract !hasdefault boolean afterTestClass()
meth public abstract !hasdefault boolean afterTestMethod()
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean beforeSuite()
meth public abstract !hasdefault boolean beforeTest()
meth public abstract !hasdefault boolean beforeTestClass()
meth public abstract !hasdefault boolean beforeTestMethod()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] afterGroups()
meth public abstract !hasdefault java.lang.String[] beforeGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault java.lang.String[] parameters()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface !annotation org.testng.annotations.DataProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean parallel()
meth public abstract !hasdefault int[] indices()
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface !annotation org.testng.annotations.ExpectedExceptions
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class[] value()

CLSS public abstract interface !annotation org.testng.annotations.Factory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault java.lang.Class<?> dataProviderClass()
meth public abstract !hasdefault java.lang.String dataProvider()
meth public abstract !hasdefault java.lang.String[] parameters()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface !annotation org.testng.annotations.Guice
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends com.google.inject.Module>[] modules()
meth public abstract !hasdefault java.lang.Class<? extends org.testng.IModuleFactory> moduleFactory()

CLSS public abstract interface org.testng.annotations.IAnnotation

CLSS public abstract interface org.testng.annotations.IConfigurationAnnotation
intf org.testng.annotations.ITestOrConfiguration
meth public abstract boolean getAfterSuite()
meth public abstract boolean getAfterTest()
meth public abstract boolean getAfterTestClass()
meth public abstract boolean getAfterTestMethod()
meth public abstract boolean getAlwaysRun()
meth public abstract boolean getBeforeSuite()
meth public abstract boolean getBeforeTest()
meth public abstract boolean getBeforeTestClass()
meth public abstract boolean getBeforeTestMethod()
meth public abstract boolean getInheritGroups()
meth public abstract boolean isFakeConfiguration()
meth public abstract java.lang.String[] getAfterGroups()
meth public abstract java.lang.String[] getBeforeGroups()

CLSS public abstract interface org.testng.annotations.IDataProviderAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract boolean isParallel()
meth public abstract java.lang.String getName()
meth public abstract void setName(java.lang.String)
meth public abstract void setParallel(boolean)

CLSS public abstract interface org.testng.annotations.IExpectedExceptionsAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract java.lang.Class[] getValue()

CLSS public abstract interface org.testng.annotations.IFactoryAnnotation
intf org.testng.annotations.IParameterizable
intf org.testng.internal.annotations.IDataProvidable

CLSS public abstract interface org.testng.annotations.IObjectFactoryAnnotation
intf org.testng.annotations.IAnnotation

CLSS public abstract interface org.testng.annotations.IParameterizable
intf org.testng.annotations.IAnnotation
meth public abstract boolean getEnabled()
meth public abstract java.lang.String[] getParameters()
 anno 0 java.lang.Deprecated()
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.testng.annotations.IParametersAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract java.lang.String[] getValue()

CLSS public abstract interface org.testng.annotations.ITestAnnotation
intf org.testng.annotations.ITestOrConfiguration
intf org.testng.internal.annotations.IDataProvidable
meth public abstract boolean getAlwaysRun()
meth public abstract boolean getSequential()
meth public abstract boolean getSingleThreaded()
meth public abstract boolean ignoreMissingDependencies()
meth public abstract boolean skipFailedInvocations()
meth public abstract int getInvocationCount()
meth public abstract int getPriority()
meth public abstract int getSuccessPercentage()
meth public abstract int getThreadPoolSize()
meth public abstract java.lang.Class<?> getDataProviderClass()
meth public abstract java.lang.Class<?>[] getExpectedExceptions()
meth public abstract java.lang.String getDataProvider()
meth public abstract java.lang.String getExpectedExceptionsMessageRegExp()
meth public abstract java.lang.String getSuiteName()
meth public abstract java.lang.String getTestName()
meth public abstract long invocationTimeOut()
meth public abstract org.testng.IRetryAnalyzer getRetryAnalyzer()
meth public abstract void setAlwaysRun(boolean)
meth public abstract void setDataProvider(java.lang.String)
meth public abstract void setDataProviderClass(java.lang.Class<?>)
meth public abstract void setExpectedExceptions(java.lang.Class<?>[])
meth public abstract void setExpectedExceptionsMessageRegExp(java.lang.String)
meth public abstract void setIgnoreMissingDependencies(boolean)
meth public abstract void setInvocationCount(int)
meth public abstract void setInvocationTimeOut(long)
meth public abstract void setPriority(int)
meth public abstract void setRetryAnalyzer(java.lang.Class<?>)
meth public abstract void setSequential(boolean)
meth public abstract void setSingleThreaded(boolean)
meth public abstract void setSkipFailedInvocations(boolean)
meth public abstract void setSuccessPercentage(int)
meth public abstract void setSuiteName(java.lang.String)
meth public abstract void setTestName(java.lang.String)
meth public abstract void setThreadPoolSize(int)

CLSS public abstract interface org.testng.annotations.ITestOrConfiguration
intf org.testng.annotations.IParameterizable
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String[] getDependsOnGroups()
meth public abstract java.lang.String[] getDependsOnMethods()
meth public abstract java.lang.String[] getGroups()
meth public abstract long getTimeOut()
meth public abstract void setDependsOnGroups(java.lang.String[])
meth public abstract void setDependsOnMethods(java.lang.String[])
meth public abstract void setDescription(java.lang.String)
meth public abstract void setGroups(java.lang.String[])
meth public abstract void setTimeOut(long)

CLSS public abstract interface !annotation org.testng.annotations.Listeners
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<? extends org.testng.ITestNGListener>[] value()

CLSS public abstract interface !annotation org.testng.annotations.NoInjection
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.testng.annotations.ObjectFactory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.testng.annotations.Optional
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation org.testng.annotations.Parameters
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface !annotation org.testng.annotations.Test
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE, CONSTRUCTOR])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean ignoreMissingDependencies()
meth public abstract !hasdefault boolean sequential()
meth public abstract !hasdefault boolean singleThreaded()
meth public abstract !hasdefault boolean skipFailedInvocations()
meth public abstract !hasdefault int invocationCount()
meth public abstract !hasdefault int priority()
meth public abstract !hasdefault int successPercentage()
meth public abstract !hasdefault int threadPoolSize()
meth public abstract !hasdefault java.lang.Class retryAnalyzer()
meth public abstract !hasdefault java.lang.Class<?> dataProviderClass()
meth public abstract !hasdefault java.lang.Class[] expectedExceptions()
meth public abstract !hasdefault java.lang.String dataProvider()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String expectedExceptionsMessageRegExp()
meth public abstract !hasdefault java.lang.String suiteName()
meth public abstract !hasdefault java.lang.String testName()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault java.lang.String[] parameters()
 anno 0 java.lang.Deprecated()
meth public abstract !hasdefault long invocationTimeOut()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.TestInstance
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.testng.internal.IResultListener
intf org.testng.IConfigurationListener
intf org.testng.ITestListener

CLSS public abstract interface org.testng.internal.IResultListener2
intf org.testng.IConfigurationListener2
intf org.testng.internal.IResultListener

CLSS public abstract interface org.testng.internal.ITestResultNotifier
meth public abstract java.util.List<org.testng.IConfigurationListener> getConfigurationListeners()
meth public abstract java.util.List<org.testng.ITestListener> getTestListeners()
meth public abstract java.util.Set<org.testng.ITestResult> getFailedTests(org.testng.ITestNGMethod)
meth public abstract java.util.Set<org.testng.ITestResult> getPassedTests(org.testng.ITestNGMethod)
meth public abstract java.util.Set<org.testng.ITestResult> getSkippedTests(org.testng.ITestNGMethod)
meth public abstract org.testng.xml.XmlTest getTest()
meth public abstract void addFailedButWithinSuccessPercentageTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addFailedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addInvokedMethod(org.testng.internal.InvokedMethod)
meth public abstract void addPassedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addSkippedTest(org.testng.ITestNGMethod,org.testng.ITestResult)

CLSS public abstract interface org.testng.internal.annotations.IDataProvidable
meth public abstract java.lang.Class<?> getDataProviderClass()
meth public abstract java.lang.String getDataProvider()
meth public abstract void setDataProvider(java.lang.String)
meth public abstract void setDataProviderClass(java.lang.Class<?>)

CLSS public org.testng.internal.junit.ArrayAsserts
cons public init()
meth public static void assertArrayEquals(byte[],byte[])
meth public static void assertArrayEquals(char[],char[])
meth public static void assertArrayEquals(double[],double[],double)
meth public static void assertArrayEquals(float[],float[],float)
meth public static void assertArrayEquals(int[],int[])
meth public static void assertArrayEquals(java.lang.Object[],java.lang.Object[])
meth public static void assertArrayEquals(java.lang.String,byte[],byte[])
meth public static void assertArrayEquals(java.lang.String,char[],char[])
meth public static void assertArrayEquals(java.lang.String,double[],double[],double)
meth public static void assertArrayEquals(java.lang.String,float[],float[],float)
meth public static void assertArrayEquals(java.lang.String,int[],int[])
meth public static void assertArrayEquals(java.lang.String,java.lang.Object[],java.lang.Object[])
meth public static void assertArrayEquals(java.lang.String,long[],long[])
meth public static void assertArrayEquals(java.lang.String,short[],short[])
meth public static void assertArrayEquals(long[],long[])
meth public static void assertArrayEquals(short[],short[])
supr java.lang.Object

CLSS public abstract interface org.testng.internal.thread.graph.IThreadWorkerFactory<%0 extends java.lang.Object>
meth public abstract java.util.List<org.testng.internal.thread.graph.IWorker<{org.testng.internal.thread.graph.IThreadWorkerFactory%0}>> createWorkers(java.util.List<{org.testng.internal.thread.graph.IThreadWorkerFactory%0}>)

CLSS public abstract interface org.testng.internal.thread.graph.IWorker<%0 extends java.lang.Object>
intf java.lang.Comparable<org.testng.internal.thread.graph.IWorker<{org.testng.internal.thread.graph.IWorker%0}>>
intf java.lang.Runnable
meth public abstract int getPriority()
meth public abstract java.util.List<{org.testng.internal.thread.graph.IWorker%0}> getTasks()
meth public abstract long getTimeOut()

CLSS public org.testng.remote.ConnectionInfo
cons public init()
meth public java.io.ObjectInputStream getOis() throws java.io.IOException
meth public java.io.ObjectOutputStream getOos() throws java.io.IOException
meth public java.net.Socket getSocket()
meth public void setSocket(java.net.Socket)
supr java.lang.Object
hfds m_ois,m_oos,m_socket

CLSS public org.testng.remote.RemoteArgs
cons public init()
fld public boolean ack
fld public boolean dontExit
fld public final static java.lang.String ACK = "-ack"
fld public final static java.lang.String DONT_EXIT = "-dontexit"
fld public final static java.lang.String PORT = "-serport"
fld public java.lang.Integer serPort
supr java.lang.Object

CLSS public org.testng.remote.RemoteSuiteWorker
cons public init(org.testng.xml.XmlSuite,org.testng.internal.remote.SlavePool,org.testng.remote.adapter.RemoteResultListener)
intf java.lang.Runnable
meth public void run()
supr org.testng.remote.RemoteWorker
hfds m_suite

CLSS public org.testng.remote.RemoteTestNG
cons public init()
fld public final static java.lang.String DEBUG_PORT = "12345"
fld public final static java.lang.String DEBUG_SUITE_DIRECTORY
fld public final static java.lang.String DEBUG_SUITE_FILE = "testng-customsuite.xml"
fld public final static java.lang.String PROPERTY_DEBUG = "testng.eclipse.debug"
fld public final static java.lang.String PROPERTY_VERBOSE = "testng.eclipse.verbose"
meth protected org.testng.ITestRunnerFactory buildTestRunnerFactory()
meth public static boolean isDebug()
meth public static boolean isVerbose()
meth public static void main(java.lang.String[])
meth public void run()
meth public void setHost(java.lang.String)
supr org.testng.TestNG
hfds LOCALHOST,m_ack,m_customTestRunnerFactory,m_debug,m_dontExit,m_host,m_port,m_serPort
hcls DelegatingTestRunnerFactory,RemoteSuiteListener

CLSS public org.testng.remote.RemoteWorker
cons public init(org.testng.remote.adapter.RemoteResultListener,org.testng.internal.remote.SlavePool)
fld protected org.testng.remote.adapter.RemoteResultListener m_listener
meth protected org.testng.SuiteRunner sendSuite(org.testng.remote.ConnectionInfo,org.testng.xml.XmlSuite) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected org.testng.internal.remote.SlavePool getSlavePool()
supr java.lang.Object
hfds m_slavePool

CLSS public org.testng.remote.SuiteDispatcher
cons public init(java.lang.String)
fld public final static java.lang.String MASTER_ADPATER = "testng.master.adpter"
fld public final static java.lang.String MASTER_STRATEGY = "testng.master.strategy"
fld public final static java.lang.String STRATEGY_SUITE = "suite"
fld public final static java.lang.String STRATEGY_TEST = "test"
fld public final static java.lang.String VERBOSE = "testng.verbose"
meth public java.util.List<org.testng.ISuite> dispatch(org.testng.internal.IConfiguration,java.util.List<org.testng.xml.XmlSuite>,java.lang.String,java.util.List<org.testng.ITestListener>)
supr java.lang.Object
hfds m_isStrategyTest,m_masterAdpter,m_verbose

CLSS public org.testng.remote.SuiteSlave
cons public init(java.lang.String,org.testng.TestNG)
fld public final static java.lang.String SLAVE_ADPATER = "testng.slave.adpter"
fld public final static java.lang.String VERBOSE = "testng.verbose"
meth public void waitForSuites()
supr java.lang.Object
hfds m_slaveAdpter,m_testng,m_verbose

CLSS public org.testng.remote.adapter.DefaultMastertAdapter
cons public init()
fld public final static java.lang.String HOSTS = "testng.hosts"
intf org.testng.remote.adapter.IMasterAdapter
meth public void awaitTermination(long) throws java.lang.InterruptedException
meth public void init(java.util.Properties)
meth public void runSuitesRemotely(org.testng.xml.XmlSuite,org.testng.remote.adapter.RemoteResultListener) throws java.io.IOException
supr java.lang.Object
hfds m_hosts,m_slavePool,m_workers

CLSS public org.testng.remote.adapter.DefaultWorkerAdapter
cons public init()
fld public final static java.lang.String SLAVE_PORT = "slave.port"
intf org.testng.remote.adapter.IWorkerAdapter
meth public org.testng.xml.XmlSuite getSuite(long) throws java.io.IOException,java.lang.InterruptedException
meth public void init(java.util.Properties) throws java.lang.Exception
meth public void returnResult(org.testng.ISuite) throws java.io.IOException
supr java.lang.Object
hfds m_clientPort,m_connectionInfo

CLSS public abstract interface org.testng.remote.adapter.IMasterAdapter
meth public abstract void awaitTermination(long) throws java.lang.InterruptedException
meth public abstract void init(java.util.Properties) throws java.lang.Exception
meth public abstract void runSuitesRemotely(org.testng.xml.XmlSuite,org.testng.remote.adapter.RemoteResultListener) throws java.io.IOException

CLSS public abstract interface org.testng.remote.adapter.IWorkerAdapter
meth public abstract org.testng.xml.XmlSuite getSuite(long) throws java.io.IOException,java.lang.InterruptedException
meth public abstract void init(java.util.Properties) throws java.lang.Exception
meth public abstract void returnResult(org.testng.ISuite) throws java.io.IOException

CLSS public org.testng.remote.adapter.RemoteResultListener
cons public init(org.testng.SuiteRunner)
meth public void onResult(org.testng.ISuite)
supr java.lang.Object
hfds m_runner

CLSS public abstract org.testng.remote.strprotocol.AbstractRemoteTestRunnerClient
cons public init()
fld protected org.testng.remote.strprotocol.IRemoteSuiteListener[] m_suiteListeners
fld protected org.testng.remote.strprotocol.IRemoteTestListener[] m_testListeners
innr public abstract ServerConnection
meth protected abstract void notifyResultEvents(org.testng.remote.strprotocol.TestResultMessage)
meth protected abstract void notifyStart(org.testng.remote.strprotocol.GenericMessage)
meth protected abstract void notifySuiteEvents(org.testng.remote.strprotocol.SuiteMessage)
meth protected abstract void notifyTestEvents(org.testng.remote.strprotocol.TestMessage)
meth public boolean isRunning()
meth public org.testng.remote.strprotocol.IRemoteSuiteListener[] getSuiteListeners()
meth public org.testng.remote.strprotocol.IRemoteTestListener[] getTestListeners()
meth public void startListening(org.testng.remote.strprotocol.IRemoteSuiteListener[],org.testng.remote.strprotocol.IRemoteTestListener[],org.testng.remote.strprotocol.AbstractRemoteTestRunnerClient$ServerConnection)
meth public void stopTest()
supr java.lang.Object
hfds fServerSocket,fSocket,m_serverConnection

CLSS public abstract org.testng.remote.strprotocol.AbstractRemoteTestRunnerClient$ServerConnection
 outer org.testng.remote.strprotocol.AbstractRemoteTestRunnerClient
cons public init(org.testng.remote.strprotocol.AbstractRemoteTestRunnerClient,org.testng.remote.strprotocol.IMessageSender)
meth protected abstract void handleThrowable(java.lang.Throwable)
meth public void run()
supr java.lang.Thread
hfds m_messageHub

CLSS public abstract org.testng.remote.strprotocol.BaseMessageSender
cons public init(java.lang.String,int,boolean)
fld protected java.io.OutputStream m_outStream
fld protected java.lang.Object m_ackLock
fld protected java.net.Socket m_clientSocket
fld protected volatile java.io.BufferedReader m_inReader
fld protected volatile java.io.InputStream m_inStream
intf org.testng.remote.strprotocol.IMessageSender
meth protected void waitForAck()
meth public void connect() throws java.io.IOException
meth public void initReceiver() throws java.net.SocketTimeoutException
meth public void sendAck()
meth public void sendStop()
meth public void shutDown()
supr java.lang.Object
hfds m_ack,m_debug,m_host,m_latestAck,m_outWriter,m_port,m_readerThread,m_serial
hcls ReaderThread

CLSS public org.testng.remote.strprotocol.GenericMessage
cons public init(int)
fld protected final int m_messageType
intf org.testng.remote.strprotocol.IStringMessage
meth public int getSuiteCount()
meth public int getTestCount()
meth public java.lang.String getMessageAsString()
meth public java.lang.String toString()
meth public org.testng.remote.strprotocol.GenericMessage addProperty(java.lang.String,int)
meth public void setSuiteCount(int)
meth public void setTestCount(int)
supr java.lang.Object
hfds m_suiteCount,m_testCount

CLSS public abstract interface org.testng.remote.strprotocol.IMessage
intf java.io.Serializable

CLSS public abstract interface org.testng.remote.strprotocol.IMessageSender
meth public abstract org.testng.remote.strprotocol.IMessage receiveMessage() throws java.lang.Exception
meth public abstract void connect() throws java.io.IOException
meth public abstract void initReceiver() throws java.net.SocketTimeoutException
meth public abstract void sendAck()
meth public abstract void sendMessage(org.testng.remote.strprotocol.IMessage) throws java.lang.Exception
meth public abstract void sendStop()
meth public abstract void shutDown()

CLSS public abstract interface org.testng.remote.strprotocol.IRemoteSuiteListener
meth public abstract void onFinish(org.testng.remote.strprotocol.SuiteMessage)
meth public abstract void onInitialization(org.testng.remote.strprotocol.GenericMessage)
meth public abstract void onStart(org.testng.remote.strprotocol.SuiteMessage)

CLSS public abstract interface org.testng.remote.strprotocol.IRemoteTestListener
meth public abstract void onFinish(org.testng.remote.strprotocol.TestMessage)
meth public abstract void onStart(org.testng.remote.strprotocol.TestMessage)
meth public abstract void onTestFailedButWithinSuccessPercentage(org.testng.remote.strprotocol.TestResultMessage)
meth public abstract void onTestFailure(org.testng.remote.strprotocol.TestResultMessage)
meth public abstract void onTestSkipped(org.testng.remote.strprotocol.TestResultMessage)
meth public abstract void onTestStart(org.testng.remote.strprotocol.TestResultMessage)
meth public abstract void onTestSuccess(org.testng.remote.strprotocol.TestResultMessage)

CLSS public abstract interface org.testng.remote.strprotocol.IStringMessage
intf org.testng.remote.strprotocol.IMessage
meth public abstract java.lang.String getMessageAsString()

CLSS public org.testng.remote.strprotocol.MessageHelper
cons public init()
fld public final static char DELIMITER = '\u0001'
fld public final static char PARAM_DELIMITER = '\u0004'
fld public final static int FAILED_ON_PERCENTAGE_TEST = 1004
fld public final static int FAILED_TEST = 1002
fld public final static int GENERIC_SUITE_COUNT = 1
fld public final static int PASSED_TEST = 1001
fld public final static int SKIPPED_TEST = 1003
fld public final static int SUITE = 10
fld public final static int SUITE_FINISH = 12
fld public final static int SUITE_START = 11
fld public final static int TEST = 100
fld public final static int TEST_FINISH = 102
fld public final static int TEST_RESULT = 1000
fld public final static int TEST_START = 101
fld public final static int TEST_STARTED = 1016
fld public final static java.lang.String ACK_MSG = ">ACK"
fld public final static java.lang.String STOP_MSG = ">STOP"
meth public static int getMessageType(java.lang.String)
meth public static java.lang.String replaceAsciiCharactersWithUnicode(java.lang.String)
meth public static java.lang.String replaceNewLine(java.lang.String)
meth public static java.lang.String replaceNewLineReplacer(java.lang.String)
meth public static java.lang.String replaceUnicodeCharactersWithAscii(java.lang.String)
meth public static org.testng.remote.strprotocol.GenericMessage unmarshallGenericMessage(java.lang.String)
meth public static org.testng.remote.strprotocol.SuiteMessage createSuiteMessage(java.lang.String)
meth public static org.testng.remote.strprotocol.TestMessage createTestMessage(java.lang.String)
meth public static org.testng.remote.strprotocol.TestResultMessage unmarshallTestResultMessage(java.lang.String)
supr java.lang.Object
hfds LINE_SEP_DELIMITER_1,LINE_SEP_DELIMITER_2

CLSS public org.testng.remote.strprotocol.MessageHub
cons public init(org.testng.remote.strprotocol.IMessageSender)
meth public org.testng.remote.strprotocol.IMessage receiveMessage()
meth public org.testng.remote.strprotocol.IMessageSender getMessageSender()
meth public void connect() throws java.io.IOException
meth public void initReceiver() throws java.net.SocketTimeoutException
meth public void sendMessage(org.testng.remote.strprotocol.IMessage)
meth public void setDebug(boolean)
meth public void shutDown()
supr java.lang.Object
hfds m_debug,m_messageSender

CLSS public org.testng.remote.strprotocol.RemoteTestListener
cons public init(org.testng.ISuite,org.testng.xml.XmlTest,org.testng.remote.strprotocol.MessageHub)
intf org.testng.internal.IResultListener2
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object
hfds m_currentTestContext,m_sender,m_suite,m_xmlTest

CLSS public org.testng.remote.strprotocol.SerializedMessageSender
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
meth public org.testng.remote.strprotocol.IMessage receiveMessage() throws java.io.IOException,java.lang.ClassNotFoundException
meth public void sendMessage(org.testng.remote.strprotocol.IMessage) throws java.io.IOException
supr org.testng.remote.strprotocol.BaseMessageSender

CLSS public org.testng.remote.strprotocol.StringMessageSender
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
meth protected void handleThrowable(java.lang.Throwable)
meth public org.testng.remote.strprotocol.IMessage receiveMessage()
meth public void sendMessage(org.testng.remote.strprotocol.IMessage)
supr org.testng.remote.strprotocol.BaseMessageSender
hfds writer

CLSS public org.testng.remote.strprotocol.SuiteMessage
cons public init(java.lang.String,boolean,int)
cons public init(org.testng.ISuite,boolean)
fld protected final boolean m_startSuite
fld protected final int m_testMethodCount
fld protected final java.lang.String m_suiteName
intf org.testng.remote.strprotocol.IStringMessage
meth public boolean isMessageOnStart()
meth public int getTestMethodCount()
meth public java.lang.String getDescriptionForMethod(java.lang.String)
meth public java.lang.String getMessageAsString()
meth public java.lang.String getSuiteName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getExcludedMethods()
meth public void setExcludedMethods(java.util.List<java.lang.String>)
supr java.lang.Object
hfds m_descriptions,m_excludedMethods

CLSS public org.testng.remote.strprotocol.TestMessage
cons public init(boolean,java.lang.String,java.lang.String,int,int,int,int,int)
cons public init(org.testng.ITestContext,boolean)
fld protected final boolean m_testStart
fld protected final int m_failedTestCount
fld protected final int m_passedTestCount
fld protected final int m_skippedTestCount
fld protected final int m_successPercentageFailedTestCount
fld protected final int m_testMethodCount
fld protected final java.lang.String m_suiteName
fld protected final java.lang.String m_testName
intf org.testng.remote.strprotocol.IStringMessage
meth public boolean isMessageOnStart()
meth public boolean isTestStart()
meth public int getFailedTestCount()
meth public int getPassedTestCount()
meth public int getSkippedTestCount()
meth public int getSuccessPercentageFailedTestCount()
meth public int getTestMethodCount()
meth public java.lang.String getMessageAsString()
meth public java.lang.String getSuiteName()
meth public java.lang.String getTestName()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.testng.remote.strprotocol.TestResultMessage
cons public init(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],long,long,java.lang.String,int,int)
cons public init(java.lang.String,java.lang.String,org.testng.ITestResult)
cons public init(org.testng.ITestContext,org.testng.ITestResult)
fld protected int m_messageType
fld protected java.lang.String m_stackTrace
fld protected java.lang.String m_suiteName
fld protected java.lang.String m_testClassName
fld protected java.lang.String m_testMethodName
fld protected java.lang.String m_testName
fld protected java.lang.String[] m_paramTypes
fld protected java.lang.String[] m_parameters
fld protected long m_endMillis
fld protected long m_startMillis
intf org.testng.remote.strprotocol.IStringMessage
meth public boolean equals(java.lang.Object)
meth public int getCurrentInvocationCount()
meth public int getInvocationCount()
meth public int getResult()
meth public int hashCode()
meth public java.lang.String getInstanceName()
meth public java.lang.String getMessageAsString()
meth public java.lang.String getMethod()
meth public java.lang.String getName()
meth public java.lang.String getStackTrace()
meth public java.lang.String getSuiteName()
meth public java.lang.String getTestClass()
meth public java.lang.String getTestDescription()
meth public java.lang.String toDisplayString()
meth public java.lang.String toString()
meth public java.lang.String[] getParameterTypes()
meth public java.lang.String[] getParameters()
meth public long getEndMillis()
meth public long getStartMillis()
meth public void setParameters(java.lang.String[])
supr java.lang.Object
hfds m_currentInvocationCount,m_instanceName,m_invocationCount,m_testDescription

CLSS public org.testng.reporters.Buffer
cons public init()
meth public static org.testng.reporters.IBuffer create()
supr java.lang.Object

CLSS public org.testng.reporters.DotTestListener
cons public init()
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr org.testng.TestListenerAdapter
hfds m_count

CLSS public org.testng.reporters.EmailableReporter
cons public init()
intf org.testng.IReporter
meth protected java.io.PrintWriter createWriter(java.lang.String) throws java.io.IOException
meth protected void endHtml(java.io.PrintWriter)
meth protected void generateExceptionReport(java.lang.Throwable,org.testng.ITestNGMethod)
meth protected void generateMethodDetailReport(java.util.List<org.testng.ISuite>)
meth protected void generateMethodSummaryReport(java.util.List<org.testng.ISuite>)
meth protected void startHtml(java.io.PrintWriter)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
meth public void generateSuiteSummaryReport(java.util.List<org.testng.ISuite>)
supr java.lang.Object
hfds L,m_methodIndex,m_out,m_row,m_testIndex
hcls TestSorter

CLSS public org.testng.reporters.EmailableReporter2
cons public init()
fld protected java.io.PrintWriter writer
fld protected java.util.List<org.testng.reporters.EmailableReporter2$SuiteResult> suiteResults
innr protected static ClassResult
innr protected static MethodResult
innr protected static SuiteResult
innr protected static TestResult
intf org.testng.IReporter
meth protected java.io.PrintWriter createWriter(java.lang.String) throws java.io.IOException
meth protected void writeBody()
meth protected void writeDocumentEnd()
meth protected void writeDocumentStart()
meth protected void writeHead()
meth protected void writeReporterMessages(java.util.List<java.lang.String>)
meth protected void writeScenarioDetails()
meth protected void writeScenarioSummary()
meth protected void writeStackTrace(java.lang.Throwable)
meth protected void writeStylesheet()
meth protected void writeSuiteSummary()
meth protected void writeTableData(java.lang.String)
meth protected void writeTableData(java.lang.String,java.lang.String)
meth protected void writeTableHeader(java.lang.String,java.lang.String)
meth protected void writeTag(java.lang.String,java.lang.String,java.lang.String)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr java.lang.Object
hfds LOG,buffer

CLSS protected static org.testng.reporters.EmailableReporter2$ClassResult
 outer org.testng.reporters.EmailableReporter2
cons public init(java.lang.String,java.util.List<org.testng.reporters.EmailableReporter2$MethodResult>)
meth public java.lang.String getClassName()
meth public java.util.List<org.testng.reporters.EmailableReporter2$MethodResult> getMethodResults()
supr java.lang.Object
hfds className,methodResults

CLSS protected static org.testng.reporters.EmailableReporter2$MethodResult
 outer org.testng.reporters.EmailableReporter2
cons public init(java.util.List<org.testng.ITestResult>)
meth public java.util.List<org.testng.ITestResult> getResults()
supr java.lang.Object
hfds results

CLSS protected static org.testng.reporters.EmailableReporter2$SuiteResult
 outer org.testng.reporters.EmailableReporter2
cons public init(org.testng.ISuite)
meth public java.lang.String getSuiteName()
meth public java.util.List<org.testng.reporters.EmailableReporter2$TestResult> getTestResults()
supr java.lang.Object
hfds suiteName,testResults

CLSS protected static org.testng.reporters.EmailableReporter2$TestResult
 outer org.testng.reporters.EmailableReporter2
cons public init(org.testng.ITestContext)
fld protected final static java.util.Comparator<org.testng.ITestResult> RESULT_COMPARATOR
meth protected java.lang.String formatGroups(java.lang.String[])
meth protected java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> groupResults(java.util.Set<org.testng.ITestResult>)
meth public int getFailedTestCount()
meth public int getPassedTestCount()
meth public int getSkippedTestCount()
meth public java.lang.String getExcludedGroups()
meth public java.lang.String getIncludedGroups()
meth public java.lang.String getTestName()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getFailedConfigurationResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getFailedTestResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getPassedTestResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getSkippedConfigurationResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getSkippedTestResults()
meth public long getDuration()
supr java.lang.Object
hfds duration,excludedGroups,failedConfigurationResults,failedTestCount,failedTestResults,includedGroups,passedTestCount,passedTestResults,skippedConfigurationResults,skippedTestCount,skippedTestResults,testName

CLSS public org.testng.reporters.ExitCodeListener
cons public init()
cons public init(org.testng.TestNG)
supr org.testng.TestNG$ExitCodeListener

CLSS public org.testng.reporters.FailedReporter
cons public init()
cons public init(org.testng.xml.XmlSuite)
fld public final static java.lang.String TESTNG_FAILED_XML = "testng-failed.xml"
intf org.testng.IReporter
meth protected void generateFailureSuite(org.testng.xml.XmlSuite,org.testng.ISuite,java.lang.String)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
meth public void onFinish(org.testng.ITestContext)
 anno 0 java.lang.Deprecated()
supr org.testng.TestListenerAdapter
hfds m_xmlSuite

CLSS public org.testng.reporters.FileStringBuffer
cons public init()
cons public init(int)
intf org.testng.reporters.IBuffer
meth public java.lang.String toString()
meth public org.testng.reporters.FileStringBuffer append(java.lang.CharSequence)
meth public static void main(java.lang.String[]) throws java.io.IOException
meth public void toWriter(java.io.Writer)
supr java.lang.Object
hfds MAX,VERBOSE,m_file,m_maxCharacters,m_sb

CLSS public org.testng.reporters.Files
cons public init()
meth public static java.lang.String readFile(java.io.File) throws java.io.IOException
meth public static java.lang.String readFile(java.io.InputStream) throws java.io.IOException
meth public static java.lang.String streamToString(java.io.InputStream) throws java.io.IOException
meth public static void copyFile(java.io.InputStream,java.io.File) throws java.io.IOException
meth public static void writeFile(java.lang.String,java.io.File) throws java.io.IOException
supr java.lang.Object

CLSS public org.testng.reporters.HtmlHelper
cons public init()
meth public static java.io.File generateStylesheet(java.lang.String) throws java.io.IOException
meth public static java.lang.String getCssString()
meth public static java.lang.String getCssString(java.lang.String)
supr java.lang.Object
hfds CSS_FILE_NAME,MY_CSS_FILE_NAME

CLSS public abstract interface org.testng.reporters.IBuffer
meth public abstract org.testng.reporters.IBuffer append(java.lang.CharSequence)
meth public abstract void toWriter(java.io.Writer)

CLSS public org.testng.reporters.JUnitReportReporter
cons public init()
intf org.testng.IReporter
meth protected java.lang.String getFileName(java.lang.Class)
meth protected java.lang.String getTestName(org.testng.ITestResult)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr java.lang.Object
hcls TestTag

CLSS public org.testng.reporters.JUnitXMLReporter
cons public init()
intf org.testng.internal.IResultListener2
meth protected void generateReport(org.testng.ITestContext)
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object
hfds ATTR_ESCAPES,ENTITY,GREATER,LESS,QUOTE,SINGLE_QUOTE,m_allTests,m_configIssues,m_fileNameIncrementer,m_fileNameMap,m_numFailed,m_numFailedButIgnored,m_numPassed,m_numSkipped

CLSS public org.testng.reporters.JqReporter
cons public init()
intf org.testng.IReporter
meth protected java.lang.String generateOutputDirectoryName(java.lang.String)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr java.lang.Object
hfds C,D,S,m_outputDirectory,m_testCount,m_testMap

CLSS public org.testng.reporters.SuiteHTMLReporter
cons public init()
fld public final static java.lang.String AFTER = "&lt;&lt;"
fld public final static java.lang.String BEFORE = "&gt;&gt;"
fld public final static java.lang.String CLASSES = "classes.html"
fld public final static java.lang.String GROUPS = "groups.html"
fld public final static java.lang.String METHODS_ALPHABETICAL = "methods-alphabetical.html"
fld public final static java.lang.String METHODS_CHRONOLOGICAL = "methods.html"
fld public final static java.lang.String METHODS_NOT_RUN = "methods-not-run.html"
fld public final static java.lang.String REPORTER_OUTPUT = "reporter-output.html"
fld public final static java.lang.String TESTNG_XML = "testng.xml.html"
intf org.testng.IReporter
meth protected java.lang.String generateOutputDirectoryName(java.lang.String)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr java.lang.Object
hfds SP,SP2,SP3,SP4,m_classes,m_outputDirectory

CLSS public org.testng.reporters.TestHTMLReporter
cons public init()
meth public static void generateLog(org.testng.ITestContext,java.lang.String,java.lang.String,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>)
meth public static void generateTable(java.lang.StringBuffer,java.lang.String,java.util.Collection<org.testng.ITestResult>,java.lang.String,java.util.Comparator<org.testng.ITestResult>)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
supr org.testng.TestListenerAdapter
hfds CONFIGURATION_COMPARATOR,HEAD,NAME_COMPARATOR,m_testContext
hcls ConfigurationComparator,NameComparator

CLSS public org.testng.reporters.TextReporter
cons public init(java.lang.String,int)
meth public void onFinish(org.testng.ITestContext)
meth public void ppp(java.lang.String)
supr org.testng.TestListenerAdapter
hfds m_testName,m_verbose

CLSS public org.testng.reporters.VerboseReporter
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String LISTENER_PREFIX = "[VerboseTestNG] "
meth protected void log(java.lang.String)
meth public java.lang.String toString()
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr org.testng.TestListenerAdapter
hfds prefix,suiteName
hcls Status

CLSS public abstract interface org.testng.reporters.XMLConstants
fld public final static java.lang.String ATTR_CLASSNAME = "classname"
fld public final static java.lang.String ATTR_ERRORS = "errors"
fld public final static java.lang.String ATTR_FAILURES = "failures"
fld public final static java.lang.String ATTR_HOSTNAME = "hostname"
fld public final static java.lang.String ATTR_MESSAGE = "message"
fld public final static java.lang.String ATTR_NAME = "name"
fld public final static java.lang.String ATTR_PACKAGE = "package"
fld public final static java.lang.String ATTR_TESTS = "tests"
fld public final static java.lang.String ATTR_TIME = "time"
fld public final static java.lang.String ATTR_TIMESTAMP = "timestamp"
fld public final static java.lang.String ATTR_TYPE = "type"
fld public final static java.lang.String ATTR_VALUE = "value"
fld public final static java.lang.String ERROR = "error"
fld public final static java.lang.String FAILURE = "failure"
fld public final static java.lang.String PROPERTIES = "properties"
fld public final static java.lang.String PROPERTY = "property"
fld public final static java.lang.String SYSTEM_ERR = "system-err"
fld public final static java.lang.String SYSTEM_OUT = "system-out"
fld public final static java.lang.String TESTCASE = "testcase"
fld public final static java.lang.String TESTSUITE = "testsuite"
fld public final static java.lang.String TESTSUITES = "testsuites"

CLSS public org.testng.reporters.XMLReporter
cons public init()
fld public final static java.lang.String FILE_NAME = "testng-results.xml"
intf org.testng.IReporter
meth public boolean isGenerateDependsOnGroups()
meth public boolean isGenerateDependsOnMethods()
meth public boolean isGenerateGroupsAttribute()
meth public boolean isGenerateTestResultAttributes()
meth public boolean isSplitClassAndPackageNames()
meth public int getFileFragmentationLevel()
meth public int getStackTraceOutputMethod()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String getTimestampFormat()
meth public static void addDurationAttributes(org.testng.reporters.XMLReporterConfig,java.util.Properties,java.util.Date,java.util.Date)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
meth public void setFileFragmentationLevel(int)
meth public void setGenerateDependsOnGroups(boolean)
meth public void setGenerateDependsOnMethods(boolean)
meth public void setGenerateGroupsAttribute(boolean)
meth public void setGenerateTestResultAttributes(boolean)
meth public void setOutputDirectory(java.lang.String)
meth public void setSplitClassAndPackageNames(boolean)
meth public void setStackTraceOutputMethod(int)
meth public void setTimestampFormat(java.lang.String)
supr java.lang.Object
hfds config,rootBuffer

CLSS public org.testng.reporters.XMLReporterConfig
cons public init()
fld public final static int FF_LEVEL_NONE = 1
fld public final static int FF_LEVEL_SUITE = 2
fld public final static int FF_LEVEL_SUITE_RESULT = 3
fld public final static int STACKTRACE_BOTH = 3
fld public final static int STACKTRACE_FULL = 2
fld public final static int STACKTRACE_NONE = 0
fld public final static int STACKTRACE_SHORT = 1
fld public final static java.lang.String ATTR_CLASS = "class"
fld public final static java.lang.String ATTR_DATA_PROVIDER = "data-provider"
fld public final static java.lang.String ATTR_DEPENDS_ON_GROUPS = "depends-on-groups"
fld public final static java.lang.String ATTR_DEPENDS_ON_METHODS = "depends-on-methods"
fld public final static java.lang.String ATTR_DESC = "description"
fld public final static java.lang.String ATTR_DURATION_MS = "duration-ms"
fld public final static java.lang.String ATTR_FINISHED_AT = "finished-at"
fld public final static java.lang.String ATTR_GROUPS = "groups"
fld public final static java.lang.String ATTR_INDEX = "index"
fld public final static java.lang.String ATTR_IS_CONFIG = "is-config"
fld public final static java.lang.String ATTR_IS_NULL = "is-null"
fld public final static java.lang.String ATTR_METHOD_SIG = "signature"
fld public final static java.lang.String ATTR_NAME = "name"
fld public final static java.lang.String ATTR_PACKAGE = "package"
fld public final static java.lang.String ATTR_STARTED_AT = "started-at"
fld public final static java.lang.String ATTR_STATUS = "status"
fld public final static java.lang.String ATTR_TEST_INSTANCE_NAME = "test-instance-name"
fld public final static java.lang.String ATTR_URL = "url"
fld public final static java.lang.String TAG_ATTRIBUTE = "attribute"
fld public final static java.lang.String TAG_ATTRIBUTES = "attributes"
fld public final static java.lang.String TAG_CLASS = "class"
fld public final static java.lang.String TAG_EXCEPTION = "exception"
fld public final static java.lang.String TAG_FULL_STACKTRACE = "full-stacktrace"
fld public final static java.lang.String TAG_GROUP = "group"
fld public final static java.lang.String TAG_GROUPS = "groups"
fld public final static java.lang.String TAG_LINE = "line"
fld public final static java.lang.String TAG_MESSAGE = "message"
fld public final static java.lang.String TAG_METHOD = "method"
fld public final static java.lang.String TAG_PARAM = "param"
fld public final static java.lang.String TAG_PARAMS = "params"
fld public final static java.lang.String TAG_PARAM_VALUE = "value"
fld public final static java.lang.String TAG_REPORTER_OUTPUT = "reporter-output"
fld public final static java.lang.String TAG_SHORT_STACKTRACE = "short-stacktrace"
fld public final static java.lang.String TAG_SUITE = "suite"
fld public final static java.lang.String TAG_TEST = "test"
fld public final static java.lang.String TAG_TESTNG_RESULTS = "testng-results"
fld public final static java.lang.String TAG_TEST_METHOD = "test-method"
fld public final static java.lang.String TEST_FAILED = "FAIL"
fld public final static java.lang.String TEST_PASSED = "PASS"
fld public final static java.lang.String TEST_SKIPPED = "SKIP"
meth public boolean isGenerateDependsOnGroups()
meth public boolean isGenerateDependsOnMethods()
meth public boolean isGenerateGroupsAttribute()
meth public boolean isGenerateTestResultAttributes()
meth public boolean isSplitClassAndPackageNames()
meth public int getFileFragmentationLevel()
meth public int getStackTraceOutputMethod()
meth public java.lang.String getOutputDirectory()
meth public static java.lang.Integer getStatus(java.lang.String)
meth public static java.lang.String getTimestampFormat()
meth public static long convertDate(java.lang.String)
meth public void setFileFragmentationLevel(int)
meth public void setGenerateDependsOnGroups(boolean)
meth public void setGenerateDependsOnMethods(boolean)
meth public void setGenerateGroupsAttribute(boolean)
meth public void setGenerateTestResultAttributes(boolean)
meth public void setOutputDirectory(java.lang.String)
meth public void setSplitClassAndPackageNames(boolean)
meth public void setStackTraceOutputMethod(int)
meth public void setTimestampFormat(java.lang.String)
supr java.lang.Object
hfds FMT_DEFAULT,STATUSES,fileFragmentationLevel,generateDependsOnGroups,generateDependsOnMethods,generateGroupsAttribute,generateTestResultAttributes,outputDirectory,splitClassAndPackageNames,stackTraceOutputMethod,timestampFormat

CLSS public org.testng.reporters.XMLStringBuffer
cons public init()
cons public init(java.lang.String)
cons public init(org.testng.reporters.IBuffer,java.lang.String)
meth public !varargs void addEmptyElement(java.lang.String,java.lang.String[])
meth public !varargs void addOptional(java.lang.String,java.lang.String,java.lang.String[])
meth public !varargs void addRequired(java.lang.String,java.lang.String,java.lang.String[])
meth public !varargs void push(java.lang.String,java.lang.String[])
meth public java.lang.String getCurrentIndent()
meth public java.lang.String toXML()
meth public org.testng.reporters.IBuffer getStringBuffer()
meth public static void main(java.lang.String[])
meth public void addCDATA(java.lang.String)
meth public void addComment(java.lang.String)
meth public void addEmptyElement(java.lang.String)
meth public void addEmptyElement(java.lang.String,java.util.Properties)
meth public void addOptional(java.lang.String,java.lang.Boolean)
meth public void addOptional(java.lang.String,java.lang.Boolean,java.util.Properties)
meth public void addOptional(java.lang.String,java.lang.String)
meth public void addOptional(java.lang.String,java.lang.String,java.util.Properties)
meth public void addRequired(java.lang.String,java.lang.String)
meth public void addRequired(java.lang.String,java.lang.String,java.util.Properties)
meth public void addString(java.lang.String)
meth public void pop()
meth public void pop(java.lang.String)
meth public void push(java.lang.String)
meth public void push(java.lang.String,java.lang.String)
meth public void push(java.lang.String,java.lang.String,java.util.Properties)
meth public void push(java.lang.String,java.util.Properties)
meth public void setDocType(java.lang.String)
meth public void setXmlDetails(java.lang.String,java.lang.String)
meth public void toWriter(java.io.Writer)
supr java.lang.Object
hfds DEFAULT_INDENT_INCREMENT,EOL,INVALID_XML_CHARS,m_buffer,m_currentIndent,m_tagStack

CLSS public org.testng.reporters.XMLSuiteResultWriter
cons public init(org.testng.reporters.XMLReporterConfig)
meth public void addTestMethodParams(org.testng.reporters.XMLStringBuffer,org.testng.ITestResult)
meth public void writeSuiteResult(org.testng.reporters.XMLStringBuffer,org.testng.ISuiteResult)
supr java.lang.Object
hfds config

CLSS public final org.testng.reporters.XMLUtils
meth public static java.lang.String escape(java.lang.String)
meth public static java.lang.String extractComment(java.lang.String,java.util.Properties)
meth public static java.lang.String xml(java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
meth public static void appendAttributes(org.testng.reporters.IBuffer,java.util.Properties)
meth public static void xmlClose(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.lang.String)
meth public static void xmlOpen(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.util.Properties)
meth public static void xmlOpen(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.util.Properties,boolean)
meth public static void xmlOptional(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.lang.Boolean,java.util.Properties)
meth public static void xmlOptional(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
meth public static void xmlRequired(org.testng.reporters.IBuffer,java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
supr java.lang.Object
hfds EOL

CLSS public org.testng.reporters.util.StackTraceTools
cons public init()
meth public static int getTestRoot(java.lang.StackTraceElement[],org.testng.ITestNGMethod)
meth public static java.lang.StackTraceElement[] getTestNGInstrastructure(java.lang.StackTraceElement[],org.testng.ITestNGMethod)
supr java.lang.Object

CLSS public abstract org.testng.util.RetryAnalyzerCount
cons public init()
intf org.testng.IRetryAnalyzer
meth protected int getCount()
meth protected void setCount(int)
meth public abstract boolean retryMethod(org.testng.ITestResult)
meth public boolean retry(org.testng.ITestResult)
supr java.lang.Object
hfds count

CLSS public org.testng.util.Strings
cons public init()
meth public static boolean isNullOrEmpty(java.lang.String)
meth public static java.lang.String escapeHtml(java.lang.String)
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds ESCAPE_HTML_LIST,ESCAPE_HTML_MAP

CLSS public abstract interface org.testng.xml.IFileParser<%0 extends java.lang.Object>
meth public abstract {org.testng.xml.IFileParser%0} parse(java.lang.String,java.io.InputStream,boolean)

CLSS public abstract interface org.testng.xml.IPostProcessor
meth public abstract java.util.Collection<org.testng.xml.XmlSuite> process(java.util.Collection<org.testng.xml.XmlSuite>)

CLSS public abstract org.testng.xml.LaunchSuite
cons protected init(boolean)
fld protected boolean m_temporary
innr public static ExistingSuite
meth public abstract java.io.File save(java.io.File)
meth public abstract org.testng.reporters.XMLStringBuffer getSuiteBuffer()
meth public boolean isTemporary()
supr java.lang.Object
hfds LOGGER
hcls ClassListSuite,ClassesAndMethodsSuite,CustomizedSuite,MethodsSuite

CLSS public static org.testng.xml.LaunchSuite$ExistingSuite
 outer org.testng.xml.LaunchSuite
cons public init(java.io.File)
meth public java.io.File save(java.io.File)
meth public org.testng.reporters.XMLStringBuffer getSuiteBuffer()
supr org.testng.xml.LaunchSuite
hfds m_suitePath

CLSS public org.testng.xml.Parameters
cons public init()
meth public java.util.List<java.lang.String> getAllValues(java.lang.String)
meth public java.util.List<java.lang.String> getLocalParameter(java.lang.String)
meth public java.util.List<java.lang.String> getValue(java.lang.String)
meth public void addAllParameter(java.lang.String,java.lang.String)
meth public void addLocalParameter(java.lang.String,java.lang.String)
supr java.lang.Object
hfds m_allParameters,m_localParameters

CLSS public org.testng.xml.Parser
cons public init() throws java.io.FileNotFoundException
cons public init(java.io.InputStream)
cons public init(java.lang.String)
fld public final static java.lang.String DEFAULT_FILENAME = "testng.xml"
fld public final static java.lang.String DEPRECATED_TESTNG_DTD_URL = "http://beust.com/testng/testng-1.0.dtd"
fld public final static java.lang.String TESTNG_DTD = "testng-1.0.dtd"
fld public final static java.lang.String TESTNG_DTD_URL = "http://testng.org/testng-1.0.dtd"
meth public java.util.Collection<org.testng.xml.XmlSuite> parse() throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public java.util.List<org.testng.xml.XmlSuite> parseToList() throws java.io.IOException,javax.xml.parsers.ParserConfigurationException,org.xml.sax.SAXException
meth public void setLoadClasses(boolean)
meth public void setPostProcessor(org.testng.xml.IPostProcessor)
supr java.lang.Object
hfds DEFAULT_FILE_PARSER,XML_PARSER,YAML_PARSER,m_fileName,m_inputStream,m_loadClasses,m_postProcessor

CLSS public org.testng.xml.ResultContentHandler
cons public init(org.testng.remote.strprotocol.IRemoteSuiteListener,org.testng.remote.strprotocol.IRemoteTestListener,boolean)
meth public void characters(char[],int,int)
meth public void endElement(java.lang.String,java.lang.String,java.lang.String)
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes)
supr org.xml.sax.helpers.DefaultHandler
hfds m_className,m_currentInvocationCount,m_currentSuite,m_currentTest,m_currentTestResult,m_failed,m_invocationCount,m_params,m_passed,m_skipped,m_suiteListener,m_suiteMethodCount,m_testListener,m_testMethodCount

CLSS public org.testng.xml.ResultXMLParser
cons public init(org.testng.remote.strprotocol.IRemoteSuiteListener,org.testng.remote.strprotocol.IRemoteTestListener)
meth public java.lang.Object parse(java.lang.String,java.io.InputStream,boolean)
meth public static void main(java.lang.String[]) throws java.io.FileNotFoundException
meth public void parse()
supr org.testng.xml.XMLParser<java.lang.Object>
hfds m_suiteListener,m_testListener

CLSS public org.testng.xml.SuiteGenerator
cons public init()
meth public static org.testng.xml.LaunchSuite createCustomizedSuite(java.lang.String,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public static org.testng.xml.LaunchSuite createProxiedXmlSuite(java.io.File)
meth public static org.testng.xml.LaunchSuite createSuite(java.lang.String,java.util.Collection<java.lang.String>,java.util.Map<java.lang.String,java.util.Collection<java.lang.String>>,java.util.Collection<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.lang.String,int)
supr java.lang.Object
hfds EMPTY_CLASS_LIST

CLSS public org.testng.xml.SuiteXmlParser
cons public init()
meth public org.testng.xml.XmlSuite parse(java.lang.String,java.io.InputStream,boolean)
supr org.testng.xml.XMLParser<org.testng.xml.XmlSuite>

CLSS public org.testng.xml.TestNGContentHandler
cons public init(java.lang.String,boolean)
meth public org.testng.xml.XmlSuite getSuite()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int)
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void xmlClasses(boolean,org.xml.sax.Attributes)
meth public void xmlGroup(boolean,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void xmlListener(boolean,org.xml.sax.Attributes)
meth public void xmlListeners(boolean,org.xml.sax.Attributes)
meth public void xmlMethodSelector(boolean,org.xml.sax.Attributes)
meth public void xmlMethodSelectors(boolean,org.xml.sax.Attributes)
meth public void xmlPackages(boolean,org.xml.sax.Attributes)
meth public void xmlRun(boolean,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void xmlSelectorClass(boolean,org.xml.sax.Attributes)
supr org.xml.sax.helpers.DefaultHandler
hfds m_currentClass,m_currentClassIndex,m_currentClassParameters,m_currentClasses,m_currentDefines,m_currentExcludedGroups,m_currentExcludedMethods,m_currentExpression,m_currentInclude,m_currentIncludeIndex,m_currentIncludedGroups,m_currentIncludedMethods,m_currentLanguage,m_currentMetaGroup,m_currentMetaGroupName,m_currentPackage,m_currentPackages,m_currentRuns,m_currentSelector,m_currentSelectors,m_currentSuite,m_currentSuiteParameters,m_currentTest,m_currentTestIndex,m_currentTestParameters,m_enabledTest,m_fileName,m_listeners,m_loadClasses,m_locations,m_suiteFiles,m_suites
hcls Include,Location

CLSS public abstract org.testng.xml.XMLParser<%0 extends java.lang.Object>
cons public init()
fld protected static javax.xml.parsers.SAXParser m_saxParser
intf org.testng.xml.IFileParser<{org.testng.xml.XMLParser%0}>
supr java.lang.Object

CLSS public org.testng.xml.XmlClass
cons public init()
cons public init(java.lang.Class)
cons public init(java.lang.Class,boolean)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean loadClasses()
meth public int getIndex()
meth public int hashCode()
meth public java.lang.Class getSupportClass()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.String> getExcludedMethods()
meth public java.util.List<org.testng.xml.XmlInclude> getIncludedMethods()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getLocalParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
 anno 0 java.lang.Deprecated()
meth public static java.lang.String listToString(java.util.List<java.lang.Integer>)
meth public void setClass(java.lang.Class)
meth public void setExcludedMethods(java.util.List<java.lang.String>)
meth public void setIncludedMethods(java.util.List<org.testng.xml.XmlInclude>)
meth public void setIndex(int)
meth public void setName(java.lang.String)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setXmlTest(org.testng.xml.XmlTest)
supr java.lang.Object
hfds m_class,m_excludedMethods,m_includedMethods,m_index,m_loadClasses,m_name,m_parameters,m_xmlTest,serialVersionUID

CLSS public org.testng.xml.XmlDefine
cons public init()
meth public java.lang.String getName()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.String> getIncludes()
meth public void onElement(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds m_includes,m_name

CLSS public org.testng.xml.XmlDependencies
cons public init()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getDependencies()
meth public void onGroup(java.lang.String,java.lang.String)
supr java.lang.Object
hfds m_xmlDependencyGroups

CLSS public org.testng.xml.XmlGroups
cons public init()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<org.testng.xml.XmlDefine> getDefines()
meth public java.util.List<org.testng.xml.XmlDependencies> getDependencies()
meth public org.testng.xml.XmlRun getRun()
meth public void addDefine(org.testng.xml.XmlDefine)
meth public void setDefines(java.util.List<org.testng.xml.XmlDefine>)
meth public void setRun(org.testng.xml.XmlRun)
meth public void setXmlDependencies(org.testng.xml.XmlDependencies)
supr java.lang.Object
hfds m_defines,m_dependencies,m_run

CLSS public org.testng.xml.XmlInclude
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.util.List<java.lang.Integer>,int)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int getIndex()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.Integer> getInvocationNumbers()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getLocalParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
 anno 0 java.lang.Deprecated()
meth public void addParameter(java.lang.String,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setXmlClass(org.testng.xml.XmlClass)
supr java.lang.Object
hfds m_description,m_index,m_invocationNumbers,m_name,m_parameters,m_xmlClass,serialVersionUID

CLSS public org.testng.xml.XmlMethodSelector
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getPriority()
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getExpression()
meth public java.lang.String getLanguage()
meth public java.lang.String toXml(java.lang.String)
meth public void setClassName(java.lang.String)
meth public void setElement(java.lang.String,java.lang.String)
meth public void setExpression(java.lang.String)
meth public void setLanguage(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPriority(int)
meth public void setScript(org.testng.xml.XmlScript)
supr java.lang.Object
hfds m_className,m_priority,m_script

CLSS public org.testng.xml.XmlMethodSelectors
cons public init()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<org.testng.xml.XmlMethodSelector> getMethodSelectors()
meth public void setMethodSelector(org.testng.xml.XmlMethodSelector)
supr java.lang.Object
hfds m_methodSelectors

CLSS public org.testng.xml.XmlPackage
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.String> getExclude()
meth public java.util.List<java.lang.String> getInclude()
meth public java.util.List<org.testng.xml.XmlClass> getXmlClasses()
meth public void setExclude(java.util.List<java.lang.String>)
meth public void setInclude(java.util.List<java.lang.String>)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds m_exclude,m_include,m_name,m_xmlClasses,serialVersionUID

CLSS public org.testng.xml.XmlRun
cons public init()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.String> getExcludes()
meth public java.util.List<java.lang.String> getIncludes()
meth public void onExclude(java.lang.String)
meth public void onInclude(java.lang.String)
supr java.lang.Object
hfds m_excludes,m_includes

CLSS public org.testng.xml.XmlScript
cons public init()
meth public java.lang.String getLanguage()
meth public java.lang.String getScript()
meth public void setLanguage(java.lang.String)
meth public void setScript(java.lang.String)
supr java.lang.Object
hfds m_language,m_script

CLSS public org.testng.xml.XmlSuite
cons public init()
fld public final static java.lang.Boolean DEFAULT_GROUP_BY_INSTANCES
fld public final static java.lang.Integer DEFAULT_DATA_PROVIDER_THREAD_COUNT
fld public final static java.lang.String CONTINUE = "continue"
fld public final static java.lang.String PARALLEL_CLASSES = "classes"
fld public final static java.lang.String PARALLEL_INSTANCES = "instances"
fld public final static java.lang.String PARALLEL_METHODS = "methods"
fld public final static java.lang.String PARALLEL_NONE = "none"
fld public final static java.lang.String PARALLEL_TESTS = "tests"
fld public final static java.lang.String SKIP = "skip"
fld public static java.lang.Boolean DEFAULT_ALLOW_RETURN_VALUES
fld public static java.lang.Boolean DEFAULT_JUNIT
fld public static java.lang.Boolean DEFAULT_MIXED
fld public static java.lang.Boolean DEFAULT_SKIP_FAILED_INVOCATION_COUNTS
fld public static java.lang.Integer DEFAULT_THREAD_COUNT
fld public static java.lang.Integer DEFAULT_VERBOSE
fld public static java.lang.String DEFAULT_CONFIG_FAILURE_POLICY
fld public static java.lang.String DEFAULT_PARALLEL
fld public static java.lang.String DEFAULT_PRESERVE_ORDER
fld public static java.util.Set<java.lang.String> PARALLEL_MODES
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int getDataProviderThreadCount()
meth public int getThreadCount()
meth public int hashCode()
meth public java.lang.Boolean getAllowReturnValues()
meth public java.lang.Boolean getGroupByInstances()
meth public java.lang.Boolean isJUnit()
meth public java.lang.Boolean skipFailedInvocationCounts()
meth public java.lang.Integer getVerbose()
meth public java.lang.Object clone()
meth public java.lang.String getConfigFailurePolicy()
meth public java.lang.String getFileName()
meth public java.lang.String getName()
meth public java.lang.String getParallel()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getPreserveOrder()
meth public java.lang.String getTest()
meth public java.lang.String getTimeOut()
meth public java.lang.String toString()
meth public java.lang.String toXml()
meth public java.util.Collection<java.lang.String> getPackageNames()
meth public java.util.List<java.lang.String> getExcludedGroups()
meth public java.util.List<java.lang.String> getIncludedGroups()
meth public java.util.List<java.lang.String> getListeners()
meth public java.util.List<java.lang.String> getSuiteFiles()
meth public java.util.List<org.testng.xml.XmlMethodSelector> getMethodSelectors()
meth public java.util.List<org.testng.xml.XmlPackage> getPackages()
meth public java.util.List<org.testng.xml.XmlPackage> getXmlPackages()
meth public java.util.List<org.testng.xml.XmlSuite> getChildSuites()
meth public java.util.List<org.testng.xml.XmlTest> getTests()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
meth public long getTimeOut(long)
meth public org.testng.ITestObjectFactory getObjectFactory()
meth public org.testng.xml.XmlGroups getGroups()
meth public org.testng.xml.XmlSuite getParentSuite()
meth public static boolean isParallel(java.lang.String)
meth public void addExcludedGroup(java.lang.String)
meth public void addIncludedGroup(java.lang.String)
meth public void addListener(java.lang.String)
meth public void addTest(org.testng.xml.XmlTest)
meth public void onListenerElement(java.lang.String)
meth public void onMethodSelectorElement(java.lang.String,java.lang.String,java.lang.String)
meth public void onPackagesElement(java.lang.String)
meth public void onParameterElement(java.lang.String,java.lang.String)
meth public void onSuiteFilesElement(java.lang.String)
meth public void setAllowReturnValues(java.lang.Boolean)
meth public void setConfigFailurePolicy(java.lang.String)
meth public void setDataProviderThreadCount(int)
meth public void setExcludedGroups(java.util.List<java.lang.String>)
meth public void setFileName(java.lang.String)
meth public void setGroupByInstances(boolean)
meth public void setGroups(org.testng.xml.XmlGroups)
meth public void setIncludedGroups(java.util.List<java.lang.String>)
meth public void setJUnit(java.lang.Boolean)
meth public void setJunit(java.lang.Boolean)
meth public void setListeners(java.util.List<java.lang.String>)
meth public void setMethodSelectors(java.util.List<org.testng.xml.XmlMethodSelector>)
meth public void setMethodSelectors(org.testng.xml.XmlMethodSelectors)
meth public void setName(java.lang.String)
meth public void setObjectFactory(org.testng.ITestObjectFactory)
meth public void setPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setParallel(java.lang.String)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setParentSuite(org.testng.xml.XmlSuite)
meth public void setPreserveOrder(java.lang.String)
meth public void setSkipFailedInvocationCounts(boolean)
meth public void setSuiteFiles(java.util.List<java.lang.String>)
meth public void setTests(java.util.List<org.testng.xml.XmlTest>)
meth public void setThreadCount(int)
meth public void setTimeOut(java.lang.String)
meth public void setVerbose(java.lang.Integer)
meth public void setXmlMethodSelectors(org.testng.xml.XmlMethodSelectors)
meth public void setXmlPackages(java.util.List<org.testng.xml.XmlPackage>)
supr java.lang.Object
hfds DEFAULT_SUITE_NAME,m_allowReturnValues,m_childSuites,m_configFailurePolicy,m_dataProviderThreadCount,m_excludedGroups,m_expression,m_fileName,m_groupByInstances,m_includedGroups,m_isJUnit,m_isMixed,m_listeners,m_methodSelectors,m_name,m_objectFactory,m_parallel,m_parameters,m_parentSuite,m_preserveOrder,m_skipFailedInvocationCounts,m_suiteFiles,m_test,m_tests,m_threadCount,m_timeOut,m_verbose,m_xmlGroups,m_xmlMethodSelectors,m_xmlPackages,serialVersionUID

CLSS public org.testng.xml.XmlTest
cons public init()
cons public init(org.testng.xml.XmlSuite)
cons public init(org.testng.xml.XmlSuite,int)
fld public static int DEFAULT_TIMEOUT_MS
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean getGroupByInstances()
meth public boolean isJUnit()
meth public boolean skipFailedInvocationCounts()
meth public int getIndex()
meth public int getThreadCount()
meth public int getVerbose()
meth public int hashCode()
meth public java.lang.Boolean getAllowReturnValues()
meth public java.lang.Object clone()
meth public java.lang.String getExpression()
meth public java.lang.String getName()
meth public java.lang.String getParallel()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getPreserveOrder()
meth public java.lang.String getTimeOut()
meth public java.lang.String toString()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.Integer> getInvocationNumbers(java.lang.String)
meth public java.util.List<java.lang.String> getExcludedGroups()
meth public java.util.List<java.lang.String> getIncludedGroups()
meth public java.util.List<org.testng.xml.XmlClass> getClasses()
meth public java.util.List<org.testng.xml.XmlClass> getXmlClasses()
meth public java.util.List<org.testng.xml.XmlMethodSelector> getMethodSelectors()
meth public java.util.List<org.testng.xml.XmlPackage> getPackages()
meth public java.util.List<org.testng.xml.XmlPackage> getXmlPackages()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getLocalParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,java.lang.String> getTestParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getXmlDependencyGroups()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getMetaGroups()
meth public long getTimeOut(long)
meth public org.testng.xml.XmlSuite getSuite()
meth public void addExcludedGroup(java.lang.String)
meth public void addIncludedGroup(java.lang.String)
meth public void addMetaGroup(java.lang.String,java.util.List<java.lang.String>)
meth public void addParameter(java.lang.String,java.lang.String)
meth public void addXmlDependencyGroup(java.lang.String,java.lang.String)
meth public void setAllowReturnValues(java.lang.Boolean)
meth public void setBeanShellExpression(java.lang.String)
meth public void setClassNames(java.util.List<org.testng.xml.XmlClass>)
 anno 0 java.lang.Deprecated()
meth public void setClasses(java.util.List<org.testng.xml.XmlClass>)
meth public void setExcludedGroups(java.util.List<java.lang.String>)
meth public void setExpression(java.lang.String)
meth public void setGroupByInstances(boolean)
meth public void setGroups(org.testng.xml.XmlGroups)
meth public void setIncludedGroups(java.util.List<java.lang.String>)
meth public void setJUnit(boolean)
meth public void setJunit(boolean)
meth public void setMetaGroups(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth public void setMethodSelectors(java.util.List<org.testng.xml.XmlMethodSelector>)
meth public void setName(java.lang.String)
meth public void setPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setParallel(java.lang.String)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setPreserveOrder(java.lang.String)
meth public void setSkipFailedInvocationCounts(boolean)
meth public void setSuite(org.testng.xml.XmlSuite)
meth public void setThreadCount(int)
meth public void setTimeOut(long)
meth public void setVerbose(int)
meth public void setXmlClasses(java.util.List<org.testng.xml.XmlClass>)
meth public void setXmlPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setXmlSuite(org.testng.xml.XmlSuite)
supr java.lang.Object
hfds m_allowReturnValues,m_excludedGroups,m_failedInvocationNumbers,m_groupByInstances,m_includedGroups,m_index,m_isJUnit,m_metaGroups,m_methodSelectors,m_name,m_parallel,m_parameters,m_preserveOrder,m_skipFailedInvocationCounts,m_suite,m_threadCount,m_timeOut,m_verbose,m_xmlClasses,m_xmlDependencyGroups,m_xmlGroups,m_xmlPackages,serialVersionUID

CLSS public org.testng.xml.XmlUtils
cons public init()
meth public static void dumpParameters(org.testng.reporters.XMLStringBuffer,java.util.Map<java.lang.String,java.lang.String>)
meth public static void setProperty(java.util.Properties,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

