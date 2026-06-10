#Signature file v4.1
#Version 1.46

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean forRemoval()
meth public abstract !hasdefault java.lang.String since()

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
innr public final static EnumDesc
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
intf java.lang.constant.Constable
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public final java.util.Optional<java.lang.Enum$EnumDesc<{java.lang.Enum%0}>> describeConstable()
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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
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

CLSS public abstract interface java.lang.constant.Constable
meth public abstract java.util.Optional<? extends java.lang.constant.ConstantDesc> describeConstable()

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

CLSS public org.testng.Assert
cons protected init()
fld public final static java.lang.String ARRAY_MISMATCH_TEMPLATE = "arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s"
innr public abstract interface static ThrowingRunnable
meth public static <%0 extends java.lang.Object> void assertListContains(java.util.List<{%%0}>,java.util.function.Predicate<{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> void assertListContainsObject(java.util.List<{%%0}>,{%%0},java.lang.String)
meth public static <%0 extends java.lang.Object> void assertListNotContains(java.util.List<{%%0}>,java.util.function.Predicate<{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> void assertListNotContainsObject(java.util.List<{%%0}>,{%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> void assertThrows(java.lang.Class<{%%0}>,org.testng.Assert$ThrowingRunnable)
meth public static <%0 extends java.lang.Throwable> void assertThrows(java.lang.String,java.lang.Class<{%%0}>,org.testng.Assert$ThrowingRunnable)
meth public static <%0 extends java.lang.Throwable> {%%0} expectThrows(java.lang.Class<{%%0}>,org.testng.Assert$ThrowingRunnable)
meth public static <%0 extends java.lang.Throwable> {%%0} expectThrows(java.lang.String,java.lang.Class<{%%0}>,org.testng.Assert$ThrowingRunnable)
meth public static void assertEquals(boolean,boolean)
meth public static void assertEquals(boolean,boolean,java.lang.String)
meth public static void assertEquals(boolean,java.lang.Boolean)
meth public static void assertEquals(boolean,java.lang.Boolean,java.lang.String)
meth public static void assertEquals(boolean[],boolean[])
meth public static void assertEquals(boolean[],boolean[],java.lang.String)
meth public static void assertEquals(byte,byte)
meth public static void assertEquals(byte,byte,java.lang.String)
meth public static void assertEquals(byte,java.lang.Byte)
meth public static void assertEquals(byte,java.lang.Byte,java.lang.String)
meth public static void assertEquals(byte[],byte[])
meth public static void assertEquals(byte[],byte[],java.lang.String)
meth public static void assertEquals(char,char)
meth public static void assertEquals(char,char,java.lang.String)
meth public static void assertEquals(char,java.lang.Character)
meth public static void assertEquals(char,java.lang.Character,java.lang.String)
meth public static void assertEquals(char[],char[])
meth public static void assertEquals(char[],char[],java.lang.String)
meth public static void assertEquals(double,double)
meth public static void assertEquals(double,double,double)
meth public static void assertEquals(double,double,double,java.lang.String)
meth public static void assertEquals(double,double,java.lang.String)
meth public static void assertEquals(double,java.lang.Double)
meth public static void assertEquals(double,java.lang.Double,java.lang.String)
meth public static void assertEquals(double[],double[])
meth public static void assertEquals(double[],double[],double)
meth public static void assertEquals(double[],double[],double,java.lang.String)
meth public static void assertEquals(double[],double[],java.lang.String)
meth public static void assertEquals(float,float)
meth public static void assertEquals(float,float,float)
meth public static void assertEquals(float,float,float,java.lang.String)
meth public static void assertEquals(float,float,java.lang.String)
meth public static void assertEquals(float,java.lang.Float)
meth public static void assertEquals(float,java.lang.Float,java.lang.String)
meth public static void assertEquals(float[],float[])
meth public static void assertEquals(float[],float[],float)
meth public static void assertEquals(float[],float[],float,java.lang.String)
meth public static void assertEquals(float[],float[],java.lang.String)
meth public static void assertEquals(int,int)
meth public static void assertEquals(int,int,java.lang.String)
meth public static void assertEquals(int,java.lang.Integer)
meth public static void assertEquals(int,java.lang.Integer,java.lang.String)
meth public static void assertEquals(int[],int[])
meth public static void assertEquals(int[],int[],java.lang.String)
meth public static void assertEquals(java.lang.Boolean,boolean)
meth public static void assertEquals(java.lang.Boolean,boolean,java.lang.String)
meth public static void assertEquals(java.lang.Boolean,java.lang.Boolean)
meth public static void assertEquals(java.lang.Boolean,java.lang.Boolean,java.lang.String)
meth public static void assertEquals(java.lang.Byte,byte)
meth public static void assertEquals(java.lang.Byte,byte,java.lang.String)
meth public static void assertEquals(java.lang.Byte,java.lang.Byte)
meth public static void assertEquals(java.lang.Byte,java.lang.Byte,java.lang.String)
meth public static void assertEquals(java.lang.Character,char)
meth public static void assertEquals(java.lang.Character,char,java.lang.String)
meth public static void assertEquals(java.lang.Character,java.lang.Character)
meth public static void assertEquals(java.lang.Character,java.lang.Character,java.lang.String)
meth public static void assertEquals(java.lang.Double,double)
meth public static void assertEquals(java.lang.Double,double,java.lang.String)
meth public static void assertEquals(java.lang.Double,java.lang.Double)
meth public static void assertEquals(java.lang.Double,java.lang.Double,java.lang.String)
meth public static void assertEquals(java.lang.Float,float)
meth public static void assertEquals(java.lang.Float,float,java.lang.String)
meth public static void assertEquals(java.lang.Float,java.lang.Float)
meth public static void assertEquals(java.lang.Float,java.lang.Float,java.lang.String)
meth public static void assertEquals(java.lang.Integer,int)
meth public static void assertEquals(java.lang.Integer,int,java.lang.String)
meth public static void assertEquals(java.lang.Integer,java.lang.Integer)
meth public static void assertEquals(java.lang.Integer,java.lang.Integer,java.lang.String)
meth public static void assertEquals(java.lang.Iterable<?>,java.lang.Iterable<?>)
meth public static void assertEquals(java.lang.Iterable<?>,java.lang.Iterable<?>,java.lang.String)
meth public static void assertEquals(java.lang.Long,java.lang.Long)
meth public static void assertEquals(java.lang.Long,java.lang.Long,java.lang.String)
meth public static void assertEquals(java.lang.Long,long)
meth public static void assertEquals(java.lang.Long,long,java.lang.String)
meth public static void assertEquals(java.lang.Object,java.lang.Object)
meth public static void assertEquals(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertEquals(java.lang.Object[],java.lang.Object[])
meth public static void assertEquals(java.lang.Object[],java.lang.Object[],java.lang.String)
meth public static void assertEquals(java.lang.Short,java.lang.Short)
meth public static void assertEquals(java.lang.Short,java.lang.Short,java.lang.String)
meth public static void assertEquals(java.lang.Short,short)
meth public static void assertEquals(java.lang.Short,short,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String)
meth public static void assertEquals(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertEquals(java.util.Collection<?>,java.util.Collection<?>)
meth public static void assertEquals(java.util.Collection<?>,java.util.Collection<?>,java.lang.String)
meth public static void assertEquals(java.util.Iterator<?>,java.util.Iterator<?>)
meth public static void assertEquals(java.util.Iterator<?>,java.util.Iterator<?>,java.lang.String)
meth public static void assertEquals(java.util.Map<?,?>,java.util.Map<?,?>)
meth public static void assertEquals(java.util.Map<?,?>,java.util.Map<?,?>,java.lang.String)
meth public static void assertEquals(java.util.Set<?>,java.util.Set<?>)
meth public static void assertEquals(java.util.Set<?>,java.util.Set<?>,java.lang.String)
meth public static void assertEquals(long,java.lang.Long)
meth public static void assertEquals(long,java.lang.Long,java.lang.String)
meth public static void assertEquals(long,long)
meth public static void assertEquals(long,long,java.lang.String)
meth public static void assertEquals(long[],long[])
meth public static void assertEquals(long[],long[],java.lang.String)
meth public static void assertEquals(short,java.lang.Short)
meth public static void assertEquals(short,java.lang.Short,java.lang.String)
meth public static void assertEquals(short,short)
meth public static void assertEquals(short,short,java.lang.String)
meth public static void assertEquals(short[],short[])
meth public static void assertEquals(short[],short[],java.lang.String)
meth public static void assertEqualsDeep(java.util.Map<?,?>,java.util.Map<?,?>)
meth public static void assertEqualsDeep(java.util.Map<?,?>,java.util.Map<?,?>,java.lang.String)
meth public static void assertEqualsDeep(java.util.Set<?>,java.util.Set<?>)
meth public static void assertEqualsDeep(java.util.Set<?>,java.util.Set<?>,java.lang.String)
meth public static void assertEqualsNoOrder(java.lang.Object[],java.lang.Object[])
meth public static void assertEqualsNoOrder(java.lang.Object[],java.lang.Object[],java.lang.String)
meth public static void assertEqualsNoOrder(java.util.Collection<?>,java.util.Collection<?>)
meth public static void assertEqualsNoOrder(java.util.Collection<?>,java.util.Collection<?>,java.lang.String)
meth public static void assertEqualsNoOrder(java.util.Iterator<?>,java.util.Iterator<?>)
meth public static void assertEqualsNoOrder(java.util.Iterator<?>,java.util.Iterator<?>,java.lang.String)
meth public static void assertFalse(boolean)
meth public static void assertFalse(boolean,java.lang.String)
meth public static void assertNotEquals(double,double,double)
meth public static void assertNotEquals(double,double,double,java.lang.String)
meth public static void assertNotEquals(float,float,float)
meth public static void assertNotEquals(float,float,float,java.lang.String)
meth public static void assertNotEquals(java.lang.Object,java.lang.Object)
meth public static void assertNotEquals(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertNotEquals(java.lang.Object[],java.lang.Object[],java.lang.String)
meth public static void assertNotEquals(java.util.Collection<?>,java.util.Collection<?>)
meth public static void assertNotEquals(java.util.Collection<?>,java.util.Collection<?>,java.lang.String)
meth public static void assertNotEquals(java.util.Iterator<?>,java.util.Iterator<?>)
meth public static void assertNotEquals(java.util.Iterator<?>,java.util.Iterator<?>,java.lang.String)
meth public static void assertNotEquals(java.util.Map<?,?>,java.util.Map<?,?>)
meth public static void assertNotEquals(java.util.Map<?,?>,java.util.Map<?,?>,java.lang.String)
meth public static void assertNotEquals(java.util.Set<?>,java.util.Set<?>)
meth public static void assertNotEquals(java.util.Set<?>,java.util.Set<?>,java.lang.String)
meth public static void assertNotEqualsDeep(java.util.Map<?,?>,java.util.Map<?,?>)
meth public static void assertNotEqualsDeep(java.util.Map<?,?>,java.util.Map<?,?>,java.lang.String)
meth public static void assertNotEqualsDeep(java.util.Set<?>,java.util.Set<?>)
meth public static void assertNotEqualsDeep(java.util.Set<?>,java.util.Set<?>,java.lang.String)
meth public static void assertNotNull(java.lang.Object)
meth public static void assertNotNull(java.lang.Object,java.lang.String)
meth public static void assertNotSame(java.lang.Object,java.lang.Object)
meth public static void assertNotSame(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertNull(java.lang.Object)
meth public static void assertNull(java.lang.Object,java.lang.String)
meth public static void assertSame(java.lang.Object,java.lang.Object)
meth public static void assertSame(java.lang.Object,java.lang.Object,java.lang.String)
meth public static void assertThrows(org.testng.Assert$ThrowingRunnable)
meth public static void assertTrue(boolean)
meth public static void assertTrue(boolean,java.lang.String)
meth public static void fail()
meth public static void fail(java.lang.String)
meth public static void fail(java.lang.String,java.lang.Throwable)
supr java.lang.Object

CLSS public abstract interface static org.testng.Assert$ThrowingRunnable
 outer org.testng.Assert
meth public abstract void run() throws java.lang.Throwable

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
hfds afterClassMethods,beforeClassMethods,classMap

CLSS public org.testng.CommandLineArgs
cons public init()
fld public boolean ignoreMissedTestNames
fld public final static java.lang.Integer SUITE_THREAD_POOL_SIZE_DEFAULT
fld public final static java.lang.String ALWAYS_RUN_LISTENERS = "-alwaysrunlisteners"
fld public final static java.lang.String CONFIG_FAILURE_POLICY = "-configfailurepolicy"
fld public final static java.lang.String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount"
fld public final static java.lang.String DEPENDENCY_INJECTOR_FACTORY = "-dependencyinjectorfactory"
fld public final static java.lang.String EXCLUDED_GROUPS = "-excludegroups"
fld public final static java.lang.String FAIL_IF_ALL_TESTS_SKIPPED = "-failwheneverythingskipped"
fld public final static java.lang.String GENERATE_RESULTS_PER_SUITE = "-generateResultsPerSuite"
fld public final static java.lang.String GROUPS = "-groups"
fld public final static java.lang.String IGNORE_MISSED_TEST_NAMES = "-ignoreMissedTestNames"
fld public final static java.lang.String INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING = "-includeAllDataDrivenTestsWhenSkipping"
fld public final static java.lang.String LISTENER = "-listener"
fld public final static java.lang.String LISTENERS_TO_SKIP_VIA_SPI = "-spilistenerstoskip"
fld public final static java.lang.String LISTENER_COMPARATOR = "-listenercomparator"
fld public final static java.lang.String LISTENER_FACTORY = "-listenerfactory"
fld public final static java.lang.String LOG = "-log"
fld public final static java.lang.String METHODS = "-methods"
fld public final static java.lang.String METHOD_SELECTORS = "-methodselectors"
fld public final static java.lang.String MIXED = "-mixed"
fld public final static java.lang.String OBJECT_FACTORY = "-objectfactory"
fld public final static java.lang.String OUTPUT_DIRECTORY = "-d"
fld public final static java.lang.String OVERRIDE_INCLUDED_METHODS = "-overrideincludedmethods"
fld public final static java.lang.String PARALLEL = "-parallel"
fld public final static java.lang.String PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE = "-propagateDataProviderFailureAsTestFailure"
fld public final static java.lang.String RANDOMIZE_SUITES = "-randomizesuites"
fld public final static java.lang.String REPORTER = "-reporter"
fld public final static java.lang.String SHARE_THREAD_POOL_FOR_DATA_PROVIDERS = "-shareThreadPoolForDataProviders"
fld public final static java.lang.String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts"
fld public final static java.lang.String SUITE_NAME = "-suitename"
fld public final static java.lang.String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize"
fld public final static java.lang.String TEST_CLASS = "-testclass"
fld public final static java.lang.String TEST_JAR = "-testjar"
fld public final static java.lang.String TEST_NAME = "-testname"
fld public final static java.lang.String TEST_NAMES = "-testnames"
fld public final static java.lang.String TEST_RUNNER_FACTORY = "-testrunfactory"
fld public final static java.lang.String THREAD_COUNT = "-threadcount"
fld public final static java.lang.String THREAD_POOL_FACTORY_CLASS = "-threadpoolfactoryclass"
fld public final static java.lang.String USE_DEFAULT_LISTENERS = "-usedefaultlisteners"
fld public final static java.lang.String USE_GLOBAL_THREAD_POOL = "-useGlobalThreadPool"
fld public final static java.lang.String VERBOSE = "-verbose"
fld public final static java.lang.String XML_PATH_IN_JAR = "-xmlpathinjar"
fld public final static java.lang.String XML_PATH_IN_JAR_DEFAULT = "testng.xml"
fld public java.lang.Boolean alwaysRunListeners
fld public java.lang.Boolean failIfAllTestsSkipped
fld public java.lang.Boolean generateResultsPerSuite
fld public java.lang.Boolean includeAllDataDrivenTestsWhenSkipping
fld public java.lang.Boolean mixed
fld public java.lang.Boolean overrideIncludedMethods
fld public java.lang.Boolean propagateDataProviderFailureAsTestFailure
fld public java.lang.Boolean randomizeSuites
fld public java.lang.Boolean shareThreadPoolForDataProviders
fld public java.lang.Boolean skipFailedInvocationCounts
fld public java.lang.Boolean useGlobalThreadPool
fld public java.lang.Integer dataProviderThreadCount
fld public java.lang.Integer suiteThreadPoolSize
fld public java.lang.Integer threadCount
fld public java.lang.Integer verbose
fld public java.lang.String configFailurePolicy
fld public java.lang.String dependencyInjectorFactoryClass
fld public java.lang.String excludedGroups
fld public java.lang.String groups
fld public java.lang.String listener
fld public java.lang.String listenerComparator
fld public java.lang.String listenerFactory
fld public java.lang.String methodSelectors
fld public java.lang.String objectFactory
fld public java.lang.String outputDirectory
fld public java.lang.String reporter
fld public java.lang.String spiListenersToSkip
fld public java.lang.String suiteName
fld public java.lang.String testClass
fld public java.lang.String testJar
fld public java.lang.String testName
fld public java.lang.String testNames
fld public java.lang.String testRunnerFactory
fld public java.lang.String threadPoolFactoryClass
fld public java.lang.String useDefaultListeners
fld public java.lang.String xmlPathInJar
fld public java.util.List<java.lang.String> commandLineMethods
fld public java.util.List<java.lang.String> suiteFiles
fld public org.testng.xml.XmlSuite$ParallelMode parallelMode
supr java.lang.Object

CLSS public org.testng.ConfigurationNotInvokedException
cons public init(org.testng.ITestNGMethod)
supr org.testng.TestNGException

CLSS public org.testng.ConversionUtils
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init()
meth public static java.lang.Object[] wrapDataProvider(java.lang.Class<?>,java.util.Collection<java.lang.Object[]>)
supr java.lang.Object

CLSS public org.testng.Converter
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException
supr java.lang.Object
hfds m_files,m_outputDirectory

CLSS public org.testng.DataProviderHolder
cons public init(org.testng.internal.IConfiguration)
meth public java.util.Collection<org.testng.IDataProviderInterceptor> getInterceptors()
meth public java.util.Collection<org.testng.IDataProviderListener> getListeners()
meth public void addInterceptor(org.testng.IDataProviderInterceptor)
meth public void addInterceptors(java.util.Collection<org.testng.IDataProviderInterceptor>)
meth public void addListener(org.testng.IDataProviderListener)
meth public void addListeners(java.util.Collection<org.testng.IDataProviderListener>)
meth public void merge(org.testng.DataProviderHolder)
supr java.lang.Object
hfds interceptors,listenerComparator,listeners

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

CLSS public abstract interface org.testng.IAlterSuiteListener
intf org.testng.ITestNGListener
meth public void alter(java.util.List<org.testng.xml.XmlSuite>)

CLSS public abstract interface org.testng.IAnnotationTransformer
intf org.testng.ITestNGListener
meth public void transform(org.testng.annotations.IConfigurationAnnotation,java.lang.Class,java.lang.reflect.Constructor,java.lang.reflect.Method)
meth public void transform(org.testng.annotations.IDataProviderAnnotation,java.lang.reflect.Method)
meth public void transform(org.testng.annotations.IFactoryAnnotation,java.lang.reflect.Method)
meth public void transform(org.testng.annotations.IListenersAnnotation,java.lang.Class<?>)
meth public void transform(org.testng.annotations.ITestAnnotation,java.lang.Class,java.lang.reflect.Constructor,java.lang.reflect.Method)

CLSS public abstract interface org.testng.IAttributes
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.Object removeAttribute(java.lang.String)
meth public abstract java.util.Set<java.lang.String> getAttributeNames()
meth public abstract void setAttribute(java.lang.String,java.lang.Object)

CLSS public abstract interface org.testng.IClass
meth public abstract java.lang.Class<?> getRealClass()
meth public abstract java.lang.Object[] getInstances(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTestName()
meth public abstract long[] getInstanceHashCodes()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.testng.xml.XmlClass getXmlClass()
meth public abstract org.testng.xml.XmlTest getXmlTest()
meth public abstract void addInstance(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.Object[] getInstances(boolean,java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")

CLSS public abstract interface org.testng.IClassListener
intf org.testng.ITestNGListener
meth public void onAfterClass(org.testng.ITestClass)
meth public void onBeforeClass(org.testng.ITestClass)

CLSS public abstract interface org.testng.IConfigurable
intf org.testng.ITestNGListener
meth public abstract void run(org.testng.IConfigureCallBack,org.testng.ITestResult)

CLSS public abstract interface org.testng.IConfigurationListener
intf org.testng.ITestNGListener
meth public void beforeConfiguration(org.testng.ITestResult)
meth public void beforeConfiguration(org.testng.ITestResult,org.testng.ITestNGMethod)
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationFailure(org.testng.ITestResult,org.testng.ITestNGMethod)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult,org.testng.ITestNGMethod)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult,org.testng.ITestNGMethod)

CLSS public abstract interface org.testng.IConfigurationListener2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
intf org.testng.IConfigurationListener

CLSS public abstract interface org.testng.IConfigureCallBack
meth public abstract java.lang.Object[] getParameters()
meth public abstract void runConfigurationMethod(org.testng.ITestResult)

CLSS public abstract interface org.testng.IDataProviderInterceptor
intf org.testng.ITestNGListener
meth public abstract java.util.Iterator<java.lang.Object[]> intercept(java.util.Iterator<java.lang.Object[]>,org.testng.IDataProviderMethod,org.testng.ITestNGMethod,org.testng.ITestContext)

CLSS public abstract interface org.testng.IDataProviderListener
intf org.testng.ITestNGListener
meth public void afterDataProviderExecution(org.testng.IDataProviderMethod,org.testng.ITestNGMethod,org.testng.ITestContext)
meth public void beforeDataProviderExecution(org.testng.IDataProviderMethod,org.testng.ITestNGMethod,org.testng.ITestContext)
meth public void onDataProviderFailure(org.testng.ITestNGMethod,org.testng.ITestContext,java.lang.RuntimeException)

CLSS public abstract interface org.testng.IDataProviderMethod
meth public abstract boolean isParallel()
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.String getName()
meth public abstract java.lang.reflect.Method getMethod()
meth public abstract java.util.List<java.lang.Integer> getIndices()
meth public boolean cacheDataForTestRetries()
meth public boolean propagateFailureAsTestFailure()
meth public java.lang.Class<? extends org.testng.IRetryDataProvider> retryUsing()

CLSS public abstract interface org.testng.IDynamicGraph<%0 extends java.lang.Object>
innr public final static !enum Status
meth public abstract boolean addNode({org.testng.IDynamicGraph%0})
meth public abstract int getNodeCount()
meth public abstract int getNodeCountWithStatus(org.testng.IDynamicGraph$Status)
meth public abstract java.lang.String toDot()
meth public abstract java.util.List<{org.testng.IDynamicGraph%0}> getDependenciesFor({org.testng.IDynamicGraph%0})
meth public abstract java.util.List<{org.testng.IDynamicGraph%0}> getFreeNodes()
meth public abstract java.util.Set<{org.testng.IDynamicGraph%0}> getNodesWithStatus(org.testng.IDynamicGraph$Status)
meth public abstract void addEdge(int,{org.testng.IDynamicGraph%0},{org.testng.IDynamicGraph%0})
meth public abstract void addEdges(int,{org.testng.IDynamicGraph%0},java.lang.Iterable<{org.testng.IDynamicGraph%0}>)
meth public abstract void setStatus(java.util.Collection<{org.testng.IDynamicGraph%0}>,org.testng.IDynamicGraph$Status)
meth public abstract void setStatus({org.testng.IDynamicGraph%0},org.testng.IDynamicGraph$Status)
meth public abstract void setVisualisers(java.util.Set<org.testng.IExecutionVisualiser>)
meth public java.util.List<{org.testng.IDynamicGraph%0}> getUpstreamDependenciesFor({org.testng.IDynamicGraph%0})

CLSS public final static !enum org.testng.IDynamicGraph$Status
 outer org.testng.IDynamicGraph
fld public final static org.testng.IDynamicGraph$Status FINISHED
fld public final static org.testng.IDynamicGraph$Status READY
fld public final static org.testng.IDynamicGraph$Status RUNNING
meth public static org.testng.IDynamicGraph$Status valueOf(java.lang.String)
meth public static org.testng.IDynamicGraph$Status[] values()
supr java.lang.Enum<org.testng.IDynamicGraph$Status>

CLSS public abstract interface org.testng.IExecutionListener
intf org.testng.ITestNGListener
meth public void onExecutionFinish()
meth public void onExecutionStart()

CLSS public abstract interface org.testng.IExecutionVisualiser
intf org.testng.ITestNGListener
meth public abstract void consumeDotDefinition(java.lang.String)

CLSS public abstract interface org.testng.IExecutorServiceFactory
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.util.concurrent.ExecutorService create(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.ThreadFactory)

CLSS public abstract interface org.testng.IExpectedExceptionsHolder
meth public abstract boolean isThrowableMatching(java.lang.Throwable)
meth public abstract java.lang.String getWrongExceptionMessage(java.lang.Throwable)

CLSS public abstract interface org.testng.IHookCallBack
meth public abstract java.lang.Object[] getParameters()
meth public abstract void runTestMethod(org.testng.ITestResult)

CLSS public abstract interface org.testng.IHookable
intf org.testng.ITestNGListener
meth public abstract void run(org.testng.IHookCallBack,org.testng.ITestResult)

CLSS public abstract interface org.testng.IInjectorFactory
meth public !varargs com.google.inject.Injector getInjector(com.google.inject.Injector,com.google.inject.Stage,com.google.inject.Module[])

CLSS public abstract interface org.testng.IInstanceInfo<%0 extends java.lang.Object>
meth public abstract java.lang.Class<{org.testng.IInstanceInfo%0}> getInstanceClass()
meth public abstract {org.testng.IInstanceInfo%0} getInstance()

CLSS public abstract interface org.testng.IInvokedMethod
meth public abstract boolean isConfigurationMethod()
meth public abstract boolean isTestMethod()
meth public abstract long getDate()
meth public abstract org.testng.ITestNGMethod getTestMethod()
meth public abstract org.testng.ITestResult getTestResult()

CLSS public abstract interface org.testng.IInvokedMethodListener
intf org.testng.ITestNGListener
meth public void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult,org.testng.ITestContext)
meth public void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult,org.testng.ITestContext)

CLSS public abstract interface org.testng.IMethodInstance
meth public abstract java.lang.Object getInstance()
meth public abstract org.testng.ITestNGMethod getMethod()

CLSS public abstract interface org.testng.IMethodInterceptor
intf org.testng.ITestNGListener
meth public abstract java.util.List<org.testng.IMethodInstance> intercept(java.util.List<org.testng.IMethodInstance>,org.testng.ITestContext)

CLSS public abstract interface org.testng.IMethodSelector
meth public abstract boolean includeMethod(org.testng.IMethodSelectorContext,org.testng.ITestNGMethod,boolean)
meth public abstract void setTestMethods(java.util.List<org.testng.ITestNGMethod>)

CLSS public abstract interface org.testng.IMethodSelectorContext
meth public abstract boolean isStopped()
meth public abstract java.util.Map<java.lang.Object,java.lang.Object> getUserData()
meth public abstract void setStopped(boolean)

CLSS public abstract interface org.testng.IModule
meth public abstract com.google.inject.Module getModule()

CLSS public abstract interface org.testng.IModuleFactory
meth public abstract com.google.inject.Module createModule(org.testng.ITestContext,java.lang.Class<?>)

CLSS public abstract interface org.testng.IReporter
intf org.testng.ITestNGListener
meth public org.testng.reporters.IReporterConfig getConfig()
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)

CLSS public abstract interface org.testng.IResultMap
meth public abstract int size()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getAllMethods()
meth public abstract java.util.Set<org.testng.ITestResult> getAllResults()
meth public abstract java.util.Set<org.testng.ITestResult> getResults(org.testng.ITestNGMethod)
meth public abstract void addResult(org.testng.ITestResult)
meth public abstract void removeResult(org.testng.ITestNGMethod)
meth public abstract void removeResult(org.testng.ITestResult)

CLSS public abstract interface org.testng.IRetryAnalyzer
meth public abstract boolean retry(org.testng.ITestResult)

CLSS public abstract interface org.testng.IRetryDataProvider
innr public static DisableDataProviderRetries
meth public abstract boolean retry(org.testng.IDataProviderMethod)

CLSS public static org.testng.IRetryDataProvider$DisableDataProviderRetries
 outer org.testng.IRetryDataProvider
cons public init()
intf org.testng.IRetryDataProvider
meth public boolean retry(org.testng.IDataProviderMethod)
supr java.lang.Object

CLSS public abstract interface org.testng.ISuite
intf org.testng.IAttributes
meth public abstract com.google.inject.Injector getParentInjector()
meth public abstract java.lang.String getGuiceStage()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOutputDirectory()
meth public abstract java.lang.String getParallel()
meth public abstract java.lang.String getParameter(java.lang.String)
meth public abstract java.lang.String getParentModule()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public abstract java.util.List<org.testng.IInvokedMethod> getAllInvokedMethods()
meth public abstract java.util.List<org.testng.ITestNGMethod> getAllMethods()
meth public abstract java.util.Map<java.lang.String,java.util.Collection<org.testng.ITestNGMethod>> getMethodsByGroups()
meth public abstract java.util.Map<java.lang.String,org.testng.ISuiteResult> getResults()
meth public abstract org.testng.ITestObjectFactory getObjectFactory()
meth public abstract org.testng.SuiteRunState getSuiteState()
meth public abstract org.testng.internal.annotations.IAnnotationFinder getAnnotationFinder()
meth public abstract org.testng.xml.XmlSuite getXmlSuite()
meth public abstract void addListener(org.testng.ITestNGListener)
meth public abstract void run()
meth public abstract void setParentInjector(com.google.inject.Injector)

CLSS public abstract interface org.testng.ISuiteListener
intf org.testng.ITestNGListener
meth public void onFinish(org.testng.ISuite)
meth public void onStart(org.testng.ISuite)

CLSS public abstract interface org.testng.ISuiteResult
meth public abstract org.testng.ITestContext getTestContext()

CLSS public abstract interface org.testng.ISuiteRunnerListener
meth public abstract org.testng.ITestListener getExitCodeListener()
meth public abstract void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public abstract void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)

CLSS public abstract interface org.testng.ITest
meth public abstract java.lang.String getTestName()

CLSS public abstract interface org.testng.ITestClass
intf org.testng.IClass
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
meth public abstract org.testng.IClass getIClass(java.lang.Class<?>)
meth public abstract org.testng.IClass[] findTestClasses()

CLSS public abstract interface org.testng.ITestContext
intf org.testng.IAttributes
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOutputDirectory()
meth public abstract java.lang.String[] getExcludedGroups()
meth public abstract java.lang.String[] getIncludedGroups()
meth public abstract java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public abstract java.util.Date getEndDate()
meth public abstract java.util.Date getStartDate()
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
meth public org.testng.IInjectorFactory getInjectorFactory()

CLSS public abstract interface org.testng.ITestListener
intf org.testng.ITestNGListener
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailedWithTimeout(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)

CLSS public abstract interface org.testng.ITestMethodFinder
meth public abstract org.testng.ITestNGMethod[] getAfterClassMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getAfterGroupsConfigurationMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getAfterSuiteMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getAfterTestConfigurationMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getAfterTestMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getBeforeClassMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getBeforeGroupsConfigurationMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getBeforeSuiteMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getBeforeTestConfigurationMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getBeforeTestMethods(java.lang.Class<?>)
meth public abstract org.testng.ITestNGMethod[] getTestMethods(java.lang.Class<?>,org.testng.xml.XmlTest)

CLSS public abstract interface org.testng.ITestNGListener
meth public boolean isEnabled()

CLSS public abstract interface org.testng.ITestNGListenerFactory
meth public abstract org.testng.ITestNGListener createListener(java.lang.Class<? extends org.testng.ITestNGListener>)

CLSS public abstract interface org.testng.ITestNGMethod
intf java.lang.Cloneable
meth public abstract boolean canRunFromClass(org.testng.IClass)
meth public abstract boolean getEnabled()
meth public abstract boolean hasMoreInvocation()
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
meth public abstract int getInterceptedPriority()
meth public abstract int getInvocationCount()
meth public abstract int getParameterInvocationCount()
meth public abstract int getPriority()
meth public abstract int getSuccessPercentage()
meth public abstract int getThreadPoolSize()
meth public abstract java.lang.Class getRealClass()
meth public abstract java.lang.Class<? extends org.testng.IRetryAnalyzer> getRetryAnalyzerClass()
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getMethodName()
meth public abstract java.lang.String getMissingGroup()
meth public abstract java.lang.String getQualifiedName()
meth public abstract java.lang.String[] getAfterGroups()
meth public abstract java.lang.String[] getBeforeGroups()
meth public abstract java.lang.String[] getGroups()
meth public abstract java.lang.String[] getGroupsDependedUpon()
meth public abstract java.lang.String[] getMethodsDependedUpon()
meth public abstract java.util.List<java.lang.Integer> getFailedInvocationNumbers()
meth public abstract java.util.List<java.lang.Integer> getInvocationNumbers()
meth public abstract java.util.Map<java.lang.String,java.lang.String> findMethodParameters(org.testng.xml.XmlTest)
meth public abstract long getDate()
meth public abstract long getInvocationTimeOut()
meth public abstract long getTimeOut()
meth public abstract long[] getInstanceHashCodes()
meth public abstract org.testng.IRetryAnalyzer getRetryAnalyzer(org.testng.ITestResult)
meth public abstract org.testng.ITestClass getTestClass()
meth public abstract org.testng.ITestNGMethod clone()
meth public abstract org.testng.internal.ConstructorOrMethod getConstructorOrMethod()
meth public abstract org.testng.xml.XmlTest getXmlTest()
meth public abstract void addFailedInvocationNumber(int)
meth public abstract void addMethodDependedUpon(java.lang.String)
meth public abstract void incrementCurrentInvocationCount()
meth public abstract void setDate(long)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setId(java.lang.String)
meth public abstract void setIgnoreMissingDependencies(boolean)
meth public abstract void setInterceptedPriority(int)
meth public abstract void setInvocationCount(int)
meth public abstract void setInvocationNumbers(java.util.List<java.lang.Integer>)
meth public abstract void setMissingGroup(java.lang.String)
meth public abstract void setMoreInvocationChecker(java.util.concurrent.Callable<java.lang.Boolean>)
meth public abstract void setParameterInvocationCount(int)
meth public abstract void setPriority(int)
meth public abstract void setRetryAnalyzerClass(java.lang.Class<? extends org.testng.IRetryAnalyzer>)
meth public abstract void setSkipFailedInvocations(boolean)
meth public abstract void setTestClass(org.testng.ITestClass)
meth public abstract void setThreadPoolSize(int)
meth public abstract void setTimeOut(long)
meth public boolean hasAfterGroupsConfiguration()
meth public boolean hasBeforeGroupsConfiguration()
meth public boolean isDataDriven()
meth public boolean isIgnoreFailure()
meth public java.lang.Class<?>[] getParameterTypes()
meth public java.util.Set<org.testng.ITestNGMethod> downstreamDependencies()
meth public java.util.Set<org.testng.ITestNGMethod> upstreamDependencies()
meth public org.testng.IDataProviderMethod getDataProviderMethod()
meth public org.testng.annotations.CustomAttribute[] getAttributes()
meth public org.testng.internal.IParameterInfo getFactoryMethodParamsInfo()

CLSS public abstract interface org.testng.ITestObjectFactory
meth public !varargs <%0 extends java.lang.Object> {%%0} newInstance(java.lang.Class<{%%0}>,java.lang.Object[])
meth public !varargs <%0 extends java.lang.Object> {%%0} newInstance(java.lang.String,java.lang.Object[])
meth public !varargs <%0 extends java.lang.Object> {%%0} newInstance(java.lang.reflect.Constructor<{%%0}>,java.lang.Object[])

CLSS public abstract interface org.testng.ITestResult
fld public final static int CREATED = -1
fld public final static int FAILURE = 2
fld public final static int SKIP = 3
fld public final static int STARTED = 16
fld public final static int SUCCESS = 1
fld public final static int SUCCESS_PERCENTAGE_FAILURE = 4
intf java.lang.Comparable<org.testng.ITestResult>
intf org.testng.IAttributes
meth public abstract boolean isSuccess()
meth public abstract boolean wasRetried()
meth public abstract int getStatus()
meth public abstract java.lang.Object getInstance()
meth public abstract java.lang.Object[] getFactoryParameters()
meth public abstract java.lang.Object[] getParameters()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getInstanceName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTestName()
meth public abstract java.lang.String id()
meth public abstract java.lang.Throwable getThrowable()
meth public abstract long getEndMillis()
meth public abstract long getStartMillis()
meth public abstract org.testng.IClass getTestClass()
meth public abstract org.testng.ITestContext getTestContext()
meth public abstract org.testng.ITestNGMethod getMethod()
meth public abstract void setEndMillis(long)
meth public abstract void setParameters(java.lang.Object[])
meth public abstract void setStatus(int)
meth public abstract void setTestName(java.lang.String)
meth public abstract void setThrowable(java.lang.Throwable)
meth public abstract void setWasRetried(boolean)
meth public boolean isNotRunning()
meth public java.util.List<org.testng.ITestNGMethod> getSkipCausedBy()
meth public static boolean wasFailureDueToTimeout(org.testng.ITestResult)
meth public static java.util.List<java.lang.String> finalStatuses()

CLSS public abstract interface org.testng.ITestRunnerFactory
meth public abstract org.testng.TestRunner newTestRunner(org.testng.ISuite,org.testng.xml.XmlTest,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>)
meth public org.testng.TestRunner newTestRunner(org.testng.ISuite,org.testng.xml.XmlTest,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>,java.util.Map<java.lang.Class<? extends org.testng.IDataProviderListener>,org.testng.IDataProviderListener>)
meth public org.testng.TestRunner newTestRunner(org.testng.ISuite,org.testng.xml.XmlTest,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>,org.testng.DataProviderHolder)

CLSS public abstract interface org.testng.ListenerComparator
 anno 0 java.lang.FunctionalInterface()
intf java.util.Comparator<org.testng.ITestNGListener>
meth public static <%0 extends org.testng.ITestNGListener> java.util.Collection<{%%0}> sort(java.util.Collection<{%%0}>,org.testng.ListenerComparator)
meth public static <%0 extends org.testng.ITestNGListener> java.util.List<{%%0}> sort(java.util.List<{%%0}>,org.testng.ListenerComparator)

CLSS public org.testng.Reporter
cons public init()
meth public static boolean getEscapeHtml()
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
hfds lockForLogging,lockForOutputs,m_currentTestResult,m_escapeHtml,m_methodOutputMap,m_orphanedOutput,m_output

CLSS public org.testng.SkipException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth protected void reduceStackTrace()
meth protected void restoreStackTrace()
meth public boolean isSkip()
supr java.lang.RuntimeException
hfds internalLock,m_stackReduced,m_stackTrace,serialVersionUID

CLSS public org.testng.SuiteRunState
cons public init()
meth public boolean isFailed()
meth public void failed()
supr java.lang.Object
hfds m_hasFailures

CLSS public org.testng.SuiteRunner
cons protected init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory,boolean,java.util.List<org.testng.IMethodInterceptor>,java.util.Collection<org.testng.IInvokedMethodListener>,org.testng.SuiteRunner$TestListenersContainer,java.util.Collection<org.testng.IClassListener>,org.testng.DataProviderHolder,java.util.Comparator<org.testng.ITestNGMethod>)
cons public init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory,boolean,java.util.Comparator<org.testng.ITestNGMethod>)
cons public init(org.testng.internal.IConfiguration,org.testng.xml.XmlSuite,java.lang.String,org.testng.ITestRunnerFactory,java.util.Comparator<org.testng.ITestNGMethod>)
intf org.testng.ISuite
intf org.testng.ISuiteRunnerListener
meth protected void addListener(org.testng.ISuiteListener)
meth public com.google.inject.Injector getParentInjector()
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.Object removeAttribute(java.lang.String)
meth public java.lang.String getGuiceStage()
meth public java.lang.String getHost()
meth public java.lang.String getName()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String getParallel()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getParentModule()
meth public java.util.Collection<org.testng.IDataProviderListener> getDataProviderListeners()
meth public java.util.Collection<org.testng.ITestNGMethod> getExcludedMethods()
meth public java.util.List<org.testng.IInvokedMethod> getAllInvokedMethods()
meth public java.util.List<org.testng.IReporter> getReporters()
meth public java.util.List<org.testng.ITestNGMethod> getAllMethods()
meth public java.util.Map<java.lang.String,java.util.Collection<org.testng.ITestNGMethod>> getMethodsByGroups()
meth public java.util.Map<java.lang.String,org.testng.ISuiteResult> getResults()
meth public java.util.Set<java.lang.String> getAttributeNames()
meth public org.testng.ITestListener getExitCodeListener()
meth public org.testng.ITestObjectFactory getObjectFactory()
meth public org.testng.SuiteRunState getSuiteState()
meth public org.testng.internal.annotations.IAnnotationFinder getAnnotationFinder()
meth public org.testng.xml.XmlSuite getXmlSuite()
meth public void addListener(org.testng.ITestNGListener)
meth public void afterInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void beforeInvocation(org.testng.IInvokedMethod,org.testng.ITestResult)
meth public void run()
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setHost(java.lang.String)
meth public void setObjectFactory(org.testng.ITestObjectFactory)
meth public void setParentInjector(com.google.inject.Injector)
meth public void setReportResults(boolean)
meth public void setSkipFailedInvocationCounts(java.lang.Boolean)
supr java.lang.Object
hfds DEFAULT_OUTPUT_DIR,attributes,classListeners,configuration,exitCodeListener,holder,invokedMethodListeners,listeners,objectFactory,outputDir,parentInjector,remoteHost,reporters,skipFailedInvocationCounts,suiteResults,suiteResultsLock,suiteState,testListeners,testRunners,tmpRunnerFactory,useDefaultListeners,visualisers,xmlSuite
hcls DefaultTestRunnerFactory,ProxyTestRunnerFactory,SuiteWorker,TestListenersContainer

CLSS public org.testng.SuiteRunnerWorker
cons public init(org.testng.ISuite,org.testng.internal.invokers.SuiteRunnerMap,int,java.lang.String)
intf org.testng.thread.IWorker<org.testng.ISuite>
meth public int compareTo(org.testng.thread.IWorker<org.testng.ISuite>)
meth public int getPriority()
meth public java.lang.String toString()
meth public java.util.List<org.testng.ISuite> getTasks()
meth public long getTimeOut()
meth public void run()
supr java.lang.Object
hfds LINE,m_defaultSuiteName,m_suiteRunner,m_suiteRunnerMap,m_verbose

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
meth public java.util.Collection<org.testng.ITestResult> getTimedoutTests()
meth public java.util.List<org.testng.ITestContext> getTestContexts()
meth public java.util.List<org.testng.ITestResult> getConfigurationFailures()
meth public java.util.List<org.testng.ITestResult> getConfigurationSkips()
meth public java.util.List<org.testng.ITestResult> getFailedButWithinSuccessPercentageTests()
meth public java.util.List<org.testng.ITestResult> getFailedTests()
meth public java.util.List<org.testng.ITestResult> getPassedTests()
meth public java.util.List<org.testng.ITestResult> getSkippedTests()
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailedWithTimeout(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
meth public void setAllTestMethods(java.util.List<org.testng.ITestNGMethod>)
meth public void setFailedButWithinSuccessPercentageTests(java.util.List<org.testng.ITestResult>)
meth public void setFailedTests(java.util.List<org.testng.ITestResult>)
meth public void setPassedTests(java.util.List<org.testng.ITestResult>)
meth public void setSkippedTests(java.util.List<org.testng.ITestResult>)
supr java.lang.Object
hfds m_allTestMethods,m_failedButWSPerTests,m_failedConfs,m_failedTests,m_passedConfs,m_passedTests,m_skippedConfs,m_skippedTests,m_testContexts,m_timedOutTests

CLSS public org.testng.TestNG
cons public init()
cons public init(boolean)
fld protected boolean m_useDefaultListeners
fld protected java.util.List<org.testng.xml.XmlSuite> m_suites
fld protected long m_end
fld protected long m_start
fld public final static java.lang.Integer DEFAULT_VERBOSE
fld public final static java.lang.String DEFAULT_COMMAND_LINE_SUITE_NAME = "Command line suite"
fld public final static java.lang.String DEFAULT_COMMAND_LINE_TEST_NAME = "Command line test"
fld public final static java.lang.String DEFAULT_OUTPUTDIR = "test-output"
meth protected java.util.List<org.testng.ISuite> runSuites()
meth protected long getEnd()
meth protected long getStart()
meth protected org.testng.internal.IConfiguration getConfiguration()
meth protected static void validateCommandLineParameters(org.testng.CommandLineArgs)
meth protected void configure(org.testng.CommandLineArgs)
meth protected void setTestRunnerFactory(org.testng.ITestRunnerFactory)
meth public !varargs void setListenersToSkipFromBeingWiredInViaServiceLoaders(java.lang.String[])
meth public boolean getReportAllDataDrivenTestsAsSkipped()
meth public boolean hasFailure()
meth public boolean hasFailureWithinSuccessPercentage()
meth public boolean hasSkip()
meth public boolean isPropagateDataProviderFailureAsTestFailure()
meth public boolean isShareThreadPoolForDataProviders()
meth public boolean useGlobalThreadPool()
meth public int getStatus()
meth public java.lang.Integer getSuiteThreadPoolSize()
meth public java.lang.String getDefaultSuiteName()
meth public java.lang.String getDefaultTestName()
meth public java.lang.String getOutputDirectory()
meth public java.util.List<org.testng.ISuite> runSuitesLocally()
meth public java.util.List<org.testng.ISuiteListener> getSuiteListeners()
meth public java.util.List<org.testng.ITestListener> getTestListeners()
meth public java.util.List<org.testng.ITestNGListener> getServiceLoaderListeners()
meth public java.util.Set<org.testng.IReporter> getReporters()
meth public org.testng.IAnnotationTransformer getAnnotationTransformer()
meth public org.testng.ListenerComparator getListenerComparator()
meth public org.testng.xml.XmlSuite$FailurePolicy getConfigFailurePolicy()
meth public static org.testng.TestNG getDefault()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.testng.TestNG privateMain(java.lang.String[],org.testng.ITestListener)
meth public static void main(java.lang.String[])
meth public void addClassLoader(java.lang.ClassLoader)
meth public void addListener(java.lang.Object)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void addListener(org.testng.ITestNGListener)
meth public void addMethodSelector(java.lang.String,int)
meth public void addMethodSelector(org.testng.xml.XmlMethodSelector)
meth public void alwaysRunListeners(boolean)
meth public void configure(java.util.Map)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void initializeEverything()
meth public void initializeSuitesAndJarFile()
meth public void propagateDataProviderFailureAsTestFailure()
meth public void run()
meth public void setCommandLineSuite(org.testng.xml.XmlSuite)
meth public void setConfigFailurePolicy(org.testng.xml.XmlSuite$FailurePolicy)
meth public void setDataProviderThreadCount(int)
meth public void setDefaultSuiteName(java.lang.String)
meth public void setDefaultTestName(java.lang.String)
meth public void setExcludedGroups(java.lang.String)
meth public void setExecutorServiceFactory(org.testng.IExecutorServiceFactory)
meth public void setGenerateResultsPerSuite(boolean)
meth public void setGroupByInstances(boolean)
meth public void setGroups(java.lang.String)
meth public void setInjectorFactory(org.testng.IInjectorFactory)
meth public void setListenerClasses(java.util.List<java.lang.Class<? extends org.testng.ITestNGListener>>)
meth public void setListenerComparator(org.testng.ListenerComparator)
meth public void setListenerFactory(org.testng.ITestNGListenerFactory)
meth public void setMethodInterceptor(org.testng.IMethodInterceptor)
meth public void setObjectFactory(java.lang.Class<? extends org.testng.ITestObjectFactory>)
meth public void setObjectFactory(org.testng.ITestObjectFactory)
meth public void setOutputDirectory(java.lang.String)
meth public void setParallel(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setParallel(org.testng.xml.XmlSuite$ParallelMode)
meth public void setPreserveOrder(boolean)
meth public void setRandomizeSuites(boolean)
meth public void setReportAllDataDrivenTestsAsSkipped(boolean)
meth public void setServiceLoaderClassLoader(java.net.URLClassLoader)
meth public void setSkipFailedInvocationCounts(java.lang.Boolean)
meth public void setSourcePath(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
meth public void shareThreadPoolForDataProviders(boolean)
meth public void shouldUseGlobalThreadPool(boolean)
meth public void toggleFailureIfAllTestsWereSkipped(boolean)
supr java.lang.Object
hfds DEFAULT_OBJECT_FACTORY,DEFAULT_THREADPOOL_FACTORY,LOGGER,exitCode,exitCodeListener,isSuiteInitialized,m_alterSuiteListeners,m_alwaysRun,m_annotationTransformer,m_classListeners,m_cmdlineSuites,m_commandLineMethods,m_commandLineTestClasses,m_configFailurePolicy,m_configurable,m_configuration,m_dataProviderInterceptors,m_dataProviderListeners,m_dataProviderThreadCount,m_defaultAnnoProcessor,m_defaultSuiteName,m_defaultTestName,m_excludedGroups,m_executionVisualisers,m_failIfAllTestsSkipped,m_generateResultsPerSuite,m_groupByInstances,m_hookable,m_ignoreMissedTestNames,m_includedGroups,m_instance,m_invokedMethodListeners,m_isInitialized,m_jCommander,m_jarPath,m_listenersToSkipFromBeingWiredIn,m_methodDescriptors,m_methodInterceptors,m_objectFactory,m_outputDir,m_parallelMode,m_preserveOrder,m_randomizeSuites,m_reporters,m_selectors,m_serviceLoaderClassLoader,m_skipFailedInvocationCounts,m_stringSuites,m_suiteListeners,m_suiteThreadPoolSize,m_testListeners,m_testNames,m_testRunnerFactory,m_threadCount,m_verbose,m_xmlPathInJar,serviceLoaderListeners

CLSS public org.testng.TestNGAntTask
hfds LOGGER,forkJvm,m_delegateCommandSystemProperties,m_dumpEnv,m_dumpSys,m_ignoreMissedTestNames,m_listeners,m_methods,m_methodselectors,m_objectFactory,m_skipFailedInvocationCounts,m_suiteName,m_suiteThreadPoolSize,m_testName,m_testNames,m_timeout,m_verbose,m_xmlPathInJar,reporterConfigs
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

CLSS public org.testng.TestNotInvokedException
cons public init(org.testng.ITestNGMethod)
supr org.testng.TestNGException

CLSS public org.testng.TestRunner
cons protected init(org.testng.internal.IConfiguration,org.testng.ISuite,org.testng.xml.XmlTest,java.lang.String,org.testng.internal.annotations.IAnnotationFinder,boolean,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>,java.util.Comparator<org.testng.ITestNGMethod>,org.testng.DataProviderHolder,org.testng.ISuiteRunnerListener)
cons public init(org.testng.internal.IConfiguration,org.testng.ISuite,org.testng.xml.XmlTest,boolean,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>,java.util.Comparator<org.testng.ITestNGMethod>,org.testng.ISuiteRunnerListener)
cons public init(org.testng.internal.IConfiguration,org.testng.ISuite,org.testng.xml.XmlTest,boolean,java.util.Collection<org.testng.IInvokedMethodListener>,java.util.List<org.testng.IClassListener>,org.testng.ISuiteRunnerListener)
innr public final static !enum PriorityWeight
intf org.testng.ITestContext
intf org.testng.internal.IConfigEavesdropper
intf org.testng.internal.ITestResultNotifier
intf org.testng.thread.IThreadWorkerFactory<org.testng.ITestNGMethod>
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
meth public java.util.List<org.testng.IConfigurationListener> getConfigurationListeners()
meth public java.util.List<org.testng.ITestListener> getTestListeners()
meth public java.util.List<org.testng.thread.IWorker<org.testng.ITestNGMethod>> createWorkers(java.util.List<org.testng.ITestNGMethod>)
meth public java.util.Set<java.lang.String> getAttributeNames()
meth public java.util.Set<org.testng.ITestResult> getFailedTests(org.testng.ITestNGMethod)
meth public java.util.Set<org.testng.ITestResult> getPassedTests(org.testng.ITestNGMethod)
meth public java.util.Set<org.testng.ITestResult> getSkippedTests(org.testng.ITestNGMethod)
meth public org.testng.IInjectorFactory getInjectorFactory()
meth public org.testng.IResultMap getConfigurationsScheduledForInvocation()
meth public org.testng.IResultMap getFailedButWithinSuccessPercentageTests()
meth public org.testng.IResultMap getFailedConfigurations()
meth public org.testng.IResultMap getFailedTests()
meth public org.testng.IResultMap getPassedConfigurations()
meth public org.testng.IResultMap getPassedTests()
meth public org.testng.IResultMap getSkippedConfigurations()
meth public org.testng.IResultMap getSkippedTests()
meth public org.testng.ISuite getSuite()
meth public org.testng.ITestListener getExitCodeListener()
meth public org.testng.ITestNGMethod[] getAfterSuiteMethods()
meth public org.testng.ITestNGMethod[] getAfterTestConfigurationMethods()
meth public org.testng.ITestNGMethod[] getAllTestMethods()
meth public org.testng.ITestNGMethod[] getBeforeSuiteMethods()
meth public org.testng.ITestNGMethod[] getBeforeTestConfigurationMethods()
meth public org.testng.internal.invokers.IInvoker getInvoker()
meth public org.testng.xml.XmlTest getCurrentXmlTest()
meth public org.testng.xml.XmlTest getTest()
meth public static int getVerbose()
meth public void addFailedButWithinSuccessPercentageTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addFailedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addListener(org.testng.ITestNGListener)
meth public void addPassedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void addSkippedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public void run()
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setOutputDirectory(java.lang.String)
meth public void setTestName(java.lang.String)
meth public void setVerbose(int)
supr java.lang.Object
hfds DEFAULT_PROP_OUTPUT_DIR,comparator,exitCodeListener,holder,m_afterSuiteMethods,m_afterXmlTestMethods,m_annotationFinder,m_attributes,m_beforeSuiteMethods,m_beforeXmlTestMethods,m_classListeners,m_classMap,m_classMethodMap,m_confListener,m_configsToBeInvoked,m_configuration,m_configurationListeners,m_endDate,m_excludedMethods,m_failedButWithinSuccessPercentageTests,m_failedConfigurations,m_failedTests,m_groupMethods,m_host,m_injectorFactory,m_invoker,m_metaGroups,m_methodInterceptors,m_objectFactory,m_outputDirectory,m_passedConfigurations,m_passedTests,m_runInfo,m_skippedConfigurations,m_skippedTests,m_startDate,m_suite,m_testClassFinder,m_testClassesFromXml,m_testListeners,m_testName,m_xmlMethodSelector,m_xmlTest,testMethodsContainer,visualisers
hcls ConfigurationListener

CLSS public final static !enum org.testng.TestRunner$PriorityWeight
 outer org.testng.TestRunner
fld public final static org.testng.TestRunner$PriorityWeight dependsOnGroups
fld public final static org.testng.TestRunner$PriorityWeight dependsOnMethods
fld public final static org.testng.TestRunner$PriorityWeight groupByInstance
fld public final static org.testng.TestRunner$PriorityWeight preserveOrder
fld public final static org.testng.TestRunner$PriorityWeight priority
meth public static org.testng.TestRunner$PriorityWeight valueOf(java.lang.String)
meth public static org.testng.TestRunner$PriorityWeight[] values()
supr java.lang.Enum<org.testng.TestRunner$PriorityWeight>

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
hfds FORMAT,m_expireDate,m_inFormat,m_outFormat,sdf,serialVersionUID

CLSS public abstract interface !annotation org.testng.annotations.AfterClass
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
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
meth public abstract !hasdefault java.lang.String[] onlyForGroups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.AfterSuite
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean ignoreFailure()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeGroups
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean firstTimeOnly()
meth public abstract !hasdefault boolean ignoreFailure()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault java.lang.String[] onlyForGroups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.BeforeSuite
 anno 0 java.lang.annotation.Documented()
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
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean ignoreFailure()
meth public abstract !hasdefault boolean inheritGroups()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long timeOut()

CLSS public abstract interface !annotation org.testng.annotations.CustomAttribute
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] values()
meth public abstract java.lang.String name()

CLSS public abstract interface !annotation org.testng.annotations.DataProvider
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean cacheDataForTestRetries()
meth public abstract !hasdefault boolean parallel()
meth public abstract !hasdefault boolean propagateFailureAsTestFailure()
meth public abstract !hasdefault int[] indices()
meth public abstract !hasdefault java.lang.Class<? extends org.testng.IRetryDataProvider> retryUsing()
meth public abstract !hasdefault java.lang.String name()

CLSS public abstract interface !annotation org.testng.annotations.Factory
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault int[] indices()
meth public abstract !hasdefault java.lang.Class<?> dataProviderClass()
meth public abstract !hasdefault java.lang.String dataProvider()

CLSS public abstract interface !annotation org.testng.annotations.Guice
 anno 0 java.lang.annotation.Documented()
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
meth public abstract boolean isIgnoreFailure()
meth public abstract java.lang.String[] getAfterGroups()
meth public abstract java.lang.String[] getBeforeGroups()
meth public boolean isAfterGroups()
meth public boolean isBeforeGroups()

CLSS public abstract interface org.testng.annotations.IDataProviderAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract boolean isCacheDataForTestRetries()
meth public abstract boolean isParallel()
meth public abstract boolean isPropagateFailureAsTestFailure()
meth public abstract java.lang.Class<? extends org.testng.IRetryDataProvider> retryUsing()
meth public abstract java.lang.String getName()
meth public abstract java.util.List<java.lang.Integer> getIndices()
meth public abstract void cacheDataForTestRetries(boolean)
meth public abstract void propagateFailureAsTestFailure()
meth public abstract void setIndices(java.util.List<java.lang.Integer>)
meth public abstract void setName(java.lang.String)
meth public abstract void setParallel(boolean)
meth public abstract void setRetryUsing(java.lang.Class<? extends org.testng.IRetryDataProvider>)

CLSS public abstract interface org.testng.annotations.IFactoryAnnotation
intf org.testng.annotations.IParameterizable
intf org.testng.internal.annotations.IDataProvidable
meth public abstract java.util.List<java.lang.Integer> getIndices()
meth public abstract void setIndices(java.util.List<java.lang.Integer>)

CLSS public abstract interface org.testng.annotations.IIgnoreAnnotation
intf org.testng.annotations.IAnnotation

CLSS public abstract interface org.testng.annotations.IListenersAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract java.lang.Class<? extends org.testng.ITestNGListener>[] getValue()
meth public abstract void setValue(java.lang.Class<? extends org.testng.ITestNGListener>[])

CLSS public abstract interface org.testng.annotations.IObjectFactoryAnnotation
intf org.testng.annotations.IAnnotation

CLSS public abstract interface org.testng.annotations.IParameterizable
intf org.testng.annotations.IAnnotation
meth public abstract boolean getEnabled()
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.testng.annotations.IParametersAnnotation
intf org.testng.annotations.IAnnotation
meth public abstract java.lang.String[] getValue()

CLSS public abstract interface org.testng.annotations.ITestAnnotation
intf org.testng.annotations.ITestOrConfiguration
intf org.testng.internal.annotations.IDataProvidable
meth public abstract boolean getAlwaysRun()
meth public abstract boolean getSingleThreaded()
meth public abstract boolean ignoreMissingDependencies()
meth public abstract boolean skipFailedInvocations()
meth public abstract int getInvocationCount()
meth public abstract int getPriority()
meth public abstract int getSuccessPercentage()
meth public abstract int getThreadPoolSize()
meth public abstract java.lang.Class<? extends org.testng.IRetryAnalyzer> getRetryAnalyzerClass()
meth public abstract java.lang.Class<?> getDataProviderClass()
meth public abstract java.lang.Class<?>[] getExpectedExceptions()
meth public abstract java.lang.String getDataProvider()
meth public abstract java.lang.String getDataProviderDynamicClass()
meth public abstract java.lang.String getExpectedExceptionsMessageRegExp()
meth public abstract java.lang.String getSuiteName()
meth public abstract java.lang.String getTestName()
meth public abstract long invocationTimeOut()
meth public abstract org.testng.annotations.CustomAttribute[] getAttributes()
meth public abstract void setAlwaysRun(boolean)
meth public abstract void setAttributes(org.testng.annotations.CustomAttribute[])
meth public abstract void setDataProvider(java.lang.String)
meth public abstract void setDataProviderClass(java.lang.Class<?>)
meth public abstract void setDataProviderDynamicClass(java.lang.String)
meth public abstract void setExpectedExceptions(java.lang.Class<?>[])
meth public abstract void setExpectedExceptionsMessageRegExp(java.lang.String)
meth public abstract void setIgnoreMissingDependencies(boolean)
meth public abstract void setInvocationCount(int)
meth public abstract void setInvocationTimeOut(long)
meth public abstract void setPriority(int)
meth public abstract void setRetryAnalyzer(java.lang.Class<? extends org.testng.IRetryAnalyzer>)
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

CLSS public abstract interface !annotation org.testng.annotations.Ignore
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation org.testng.annotations.Listeners
 anno 0 java.lang.annotation.Inherited()
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
fld public final static java.lang.String NULL_VALUE = "null"
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface !annotation org.testng.annotations.Test
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean alwaysRun()
meth public abstract !hasdefault boolean enabled()
meth public abstract !hasdefault boolean ignoreMissingDependencies()
meth public abstract !hasdefault boolean singleThreaded()
meth public abstract !hasdefault boolean skipFailedInvocations()
meth public abstract !hasdefault int invocationCount()
meth public abstract !hasdefault int priority()
meth public abstract !hasdefault int successPercentage()
meth public abstract !hasdefault int threadPoolSize()
meth public abstract !hasdefault java.lang.Class<? extends org.testng.IRetryAnalyzer> retryAnalyzer()
meth public abstract !hasdefault java.lang.Class<?> dataProviderClass()
meth public abstract !hasdefault java.lang.Class[] expectedExceptions()
meth public abstract !hasdefault java.lang.String dataProvider()
meth public abstract !hasdefault java.lang.String dataProviderDynamicClass()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String expectedExceptionsMessageRegExp()
meth public abstract !hasdefault java.lang.String suiteName()
meth public abstract !hasdefault java.lang.String testName()
meth public abstract !hasdefault java.lang.String[] dependsOnGroups()
meth public abstract !hasdefault java.lang.String[] dependsOnMethods()
meth public abstract !hasdefault java.lang.String[] groups()
meth public abstract !hasdefault long invocationTimeOut()
meth public abstract !hasdefault long timeOut()
meth public abstract !hasdefault org.testng.annotations.CustomAttribute[] attributes()

CLSS public abstract interface !annotation org.testng.annotations.TestInstance
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.testng.internal.IConfigEavesdropper
meth public abstract org.testng.IResultMap getConfigurationsScheduledForInvocation()

CLSS public abstract interface org.testng.internal.IResultListener
intf org.testng.IConfigurationListener
intf org.testng.ITestListener

CLSS public abstract interface org.testng.internal.IResultListener2
intf org.testng.IConfigurationListener
intf org.testng.internal.IResultListener

CLSS public abstract interface org.testng.internal.ITestResultNotifier
meth public abstract java.util.List<org.testng.IConfigurationListener> getConfigurationListeners()
meth public abstract java.util.List<org.testng.ITestListener> getTestListeners()
meth public abstract java.util.Set<org.testng.ITestResult> getFailedTests(org.testng.ITestNGMethod)
meth public abstract java.util.Set<org.testng.ITestResult> getPassedTests(org.testng.ITestNGMethod)
meth public abstract java.util.Set<org.testng.ITestResult> getSkippedTests(org.testng.ITestNGMethod)
meth public abstract org.testng.ITestListener getExitCodeListener()
meth public abstract org.testng.xml.XmlTest getTest()
meth public abstract void addFailedButWithinSuccessPercentageTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addFailedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addPassedTest(org.testng.ITestNGMethod,org.testng.ITestResult)
meth public abstract void addSkippedTest(org.testng.ITestNGMethod,org.testng.ITestResult)

CLSS public abstract interface org.testng.internal.annotations.IDataProvidable
meth public abstract java.lang.Class<?> getDataProviderClass()
meth public abstract java.lang.String getDataProvider()
meth public abstract java.lang.String getDataProviderDynamicClass()
meth public abstract void setDataProvider(java.lang.String)
meth public abstract void setDataProviderClass(java.lang.Class<?>)
meth public abstract void setDataProviderDynamicClass(java.lang.String)

CLSS public org.testng.internal.junit.ArrayAsserts
cons public init()
meth public static void assertArrayEquals(boolean[],boolean[])
meth public static void assertArrayEquals(byte[],byte[])
meth public static void assertArrayEquals(char[],char[])
meth public static void assertArrayEquals(double[],double[],double)
meth public static void assertArrayEquals(float[],float[],float)
meth public static void assertArrayEquals(int[],int[])
meth public static void assertArrayEquals(java.lang.Object[],java.lang.Object[])
meth public static void assertArrayEquals(java.lang.String,boolean[],boolean[])
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

CLSS public abstract org.testng.reporters.AbstractXmlReporter
cons public init()
innr public static Count
intf org.testng.IReporter
intf org.testng.reporters.ICustomizeXmlReport
meth protected final java.util.Properties writeSummaryCount(org.testng.reporters.AbstractXmlReporter$Count)
meth protected final org.testng.reporters.AbstractXmlReporter$Count computeCountForSuite(org.testng.ISuite)
meth protected final void writeReporterOutput(org.testng.reporters.XMLStringBuffer)
meth protected final void writeReporterOutput(org.testng.reporters.XMLStringBuffer,java.util.List<java.lang.String>)
meth protected final void writeSuite(org.testng.reporters.XMLStringBuffer,org.testng.ISuite)
meth protected static void setDurationAttributes(org.testng.reporters.XMLReporterConfig,java.util.Properties,java.util.Date,java.util.Date)
meth public java.lang.String fileName()
meth public org.testng.reporters.XMLReporterConfig getConfig()
meth public void addCustomTagsFor(org.testng.reporters.XMLStringBuffer,org.testng.ITestResult)
supr java.lang.Object
hfds config

CLSS public static org.testng.reporters.AbstractXmlReporter$Count
 outer org.testng.reporters.AbstractXmlReporter
innr public final static Builder
meth public int total()
meth public void add(org.testng.reporters.AbstractXmlReporter$Count)
supr java.lang.Object
hfds failed,ignored,passed,retried,skipped

CLSS public final static org.testng.reporters.AbstractXmlReporter$Count$Builder
 outer org.testng.reporters.AbstractXmlReporter$Count
meth public org.testng.reporters.AbstractXmlReporter$Count build()
meth public org.testng.reporters.AbstractXmlReporter$Count$Builder withFailed(int)
meth public org.testng.reporters.AbstractXmlReporter$Count$Builder withIgnored(int)
meth public org.testng.reporters.AbstractXmlReporter$Count$Builder withPassed(int)
meth public org.testng.reporters.AbstractXmlReporter$Count$Builder withRetried(int)
meth public org.testng.reporters.AbstractXmlReporter$Count$Builder withSkipped(int)
meth public static org.testng.reporters.AbstractXmlReporter$Count$Builder builder()
supr java.lang.Object
hfds failed,ignored,passed,retried,skipped

CLSS public org.testng.reporters.Buffer
cons public init()
meth public static org.testng.reporters.IBuffer create()
supr java.lang.Object

CLSS public org.testng.reporters.DotTestListener
cons public init()
intf org.testng.ITestListener
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object
hfds m_count

CLSS public org.testng.reporters.EmailableReporter2
cons public init()
fld protected final java.util.List<org.testng.reporters.EmailableReporter2$SuiteResult> suiteResults
fld protected java.io.PrintWriter writer
innr protected static ClassResult
innr protected static MethodResult
innr protected static SuiteResult
innr protected static TestResult
intf org.testng.IReporter
meth protected java.io.PrintWriter createWriter(java.lang.String) throws java.io.IOException
meth protected java.lang.String getFormattedStartTime(long)
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
meth public java.lang.String getFileName()
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
meth public void setFileName(java.lang.String)
supr java.lang.Object
hfds LOG,buffer,fileName

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
meth public org.testng.xml.XmlSuite$ParallelMode getParallelMode()
supr java.lang.Object
hfds mode,suiteName,testResults

CLSS protected static org.testng.reporters.EmailableReporter2$TestResult
 outer org.testng.reporters.EmailableReporter2
cons public init(org.testng.ITestContext)
fld protected final static java.util.Comparator<org.testng.ITestResult> RESULT_COMPARATOR
meth protected java.lang.String formatGroups(java.lang.String[])
meth protected java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> groupResults(java.util.Set<org.testng.ITestResult>)
meth public int getFailedTestCount()
meth public int getPassedTestCount()
meth public int getRetriedTestCount()
meth public int getSkippedTestCount()
meth public java.lang.String getExcludedGroups()
meth public java.lang.String getIncludedGroups()
meth public java.lang.String getTestName()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getFailedConfigurationResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getFailedTestResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getPassedTestResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getRetriedTestResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getSkippedConfigurationResults()
meth public java.util.List<org.testng.reporters.EmailableReporter2$ClassResult> getSkippedTestResults()
meth public long getDuration()
supr java.lang.Object
hfds duration,excludedGroups,failedConfigurationResults,failedTestCount,failedTestResults,includedGroups,passedTestCount,passedTestResults,retriedTestCount,retriedTestResults,skippedConfigurationResults,skippedTestCount,skippedTestResults,testName

CLSS public org.testng.reporters.ExitCodeListener
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init()
intf org.testng.internal.IResultListener2
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestStart(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object

CLSS public org.testng.reporters.FailedReporter
cons public init()
cons public init(org.testng.xml.XmlSuite)
fld public final static java.lang.String TESTNG_FAILED_XML = "testng-failed.xml"
intf org.testng.IReporter
meth protected void generateFailureSuite(org.testng.xml.XmlSuite,org.testng.ISuite,java.lang.String)
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr java.lang.Object
hfds keyCache,m_xmlSuite

CLSS public org.testng.reporters.FileStringBuffer
cons public init()
cons public init(int)
intf org.testng.reporters.IBuffer
meth public java.lang.String toString()
meth public org.testng.reporters.FileStringBuffer append(java.lang.CharSequence)
meth public void toWriter(java.io.Writer)
supr java.lang.Object
hfds LOGGER,MAX,VERBOSE,m_file,m_maxCharacters,m_sb

CLSS public final org.testng.reporters.Files
meth public static java.lang.String readFile(java.io.InputStream) throws java.io.IOException
supr java.lang.Object

CLSS public org.testng.reporters.HtmlHelper
cons public init()
meth public static java.lang.String getCssString()
meth public static java.lang.String getCssString(java.lang.String)
meth public static void generateStylesheet(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds CSS_FILE_NAME,MY_CSS_FILE_NAME

CLSS public abstract interface org.testng.reporters.IBuffer
meth public abstract org.testng.reporters.IBuffer append(java.lang.CharSequence)
meth public abstract void toWriter(java.io.Writer)

CLSS public abstract interface org.testng.reporters.ICustomizeXmlReport
meth public abstract void addCustomTagsFor(org.testng.reporters.XMLStringBuffer,org.testng.ITestResult)

CLSS public abstract interface org.testng.reporters.IReporterConfig
meth public void setProperties(java.util.List<org.testng.internal.ReporterConfig$Property>)
meth public void setProperty(java.lang.String,java.lang.String)

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
meth public void onConfigurationFailure(org.testng.ITestResult)
meth public void onConfigurationSkip(org.testng.ITestResult)
meth public void onConfigurationSuccess(org.testng.ITestResult)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
meth public void onTestFailedButWithinSuccessPercentage(org.testng.ITestResult)
meth public void onTestFailure(org.testng.ITestResult)
meth public void onTestSkipped(org.testng.ITestResult)
meth public void onTestSuccess(org.testng.ITestResult)
supr java.lang.Object
hfds ATTR_ESCAPES,ENTITY,GREATER,LESS,QUOTE,SINGLE_QUOTE,lock,m_allTests,m_configIssues,m_fileNameIncrementer,m_fileNameMap,m_numFailed

CLSS public org.testng.reporters.PerSuiteXMLReporter
cons public init()
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr org.testng.reporters.AbstractXmlReporter

CLSS public org.testng.reporters.PojoReporterConfig
cons public init(java.lang.Object)
intf org.testng.reporters.IReporterConfig
meth public void setProperty(java.lang.String,java.lang.String)
supr java.lang.Object
hfds target

CLSS public org.testng.reporters.RuntimeBehavior
fld public final static java.lang.String FILE_NAME = "testng-results.xml"
meth public static boolean verboseMode()
meth public static java.lang.String getDefaultEmailableReport2Name()
meth public static java.lang.String getDefaultEmailableReportName()
meth public static java.lang.String getDefaultFileNameForXmlReports()
meth public static java.lang.String getDefaultLineSeparator()
meth public static java.lang.String getDefaultStacktraceLevels()
meth public static java.lang.String getLineSeparatorOrNewLine()
supr java.lang.Object

CLSS public org.testng.reporters.SuiteHTMLReporter
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
hfds CLOSE_TD,SP,SP2,TD_A_TARGET_MAIN_FRAME_HREF,m_classes,m_outputDirectory

CLSS public org.testng.reporters.TestHTMLReporter
cons public init()
intf org.testng.ITestListener
meth public static void generateLog(org.testng.ITestContext,java.lang.String,java.lang.String,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>,java.util.Collection<org.testng.ITestResult>)
meth public static void generateTable(java.io.PrintWriter,java.lang.String,java.util.Collection<org.testng.ITestResult>,java.lang.String,java.util.Comparator<org.testng.ITestResult>)
meth public void onFinish(org.testng.ITestContext)
meth public void onStart(org.testng.ITestContext)
supr java.lang.Object
hfds CONFIGURATION_COMPARATOR,HEAD,NAME_COMPARATOR,m_testContext
hcls ConfigurationComparator,NameComparator

CLSS public org.testng.reporters.TextReporter
cons public init(java.lang.String,int)
intf org.testng.ITestListener
meth public void onFinish(org.testng.ITestContext)
supr java.lang.Object
hfds LINE,m_testName,m_verbose

CLSS public org.testng.reporters.VerboseReporter
cons public init(java.lang.String)
fld public final static java.lang.String LISTENER_PREFIX = "[VerboseTestNG] "
intf org.testng.IConfigurationListener
intf org.testng.ITestListener
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
supr java.lang.Object
hfds prefix,suiteName
hcls Status

CLSS public abstract interface org.testng.reporters.XMLConstants
fld public final static java.lang.String ATTR_CLASSNAME = "classname"
fld public final static java.lang.String ATTR_ERRORS = "errors"
fld public final static java.lang.String ATTR_FAILURES = "failures"
fld public final static java.lang.String ATTR_HOSTNAME = "hostname"
fld public final static java.lang.String ATTR_IGNORED = "ignored"
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
fld public final static java.lang.String SKIPPED = "skipped"
fld public final static java.lang.String SYSTEM_ERR = "system-err"
fld public final static java.lang.String SYSTEM_OUT = "system-out"
fld public final static java.lang.String TESTCASE = "testcase"
fld public final static java.lang.String TESTSUITE = "testsuite"
fld public final static java.lang.String TESTSUITES = "testsuites"

CLSS public org.testng.reporters.XMLReporter
cons public init()
meth public void generateReport(java.util.List<org.testng.xml.XmlSuite>,java.util.List<org.testng.ISuite>,java.lang.String)
supr org.testng.reporters.AbstractXmlReporter

CLSS public org.testng.reporters.XMLReporterConfig
cons public init()
fld public final static int FF_LEVEL_NONE = 1
fld public final static int FF_LEVEL_SUITE = 2
fld public final static int FF_LEVEL_SUITE_RESULT = 3
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
fld public final static java.lang.String ATTR_RETRIED = "retried"
fld public final static java.lang.String ATTR_STARTED_AT = "started-at"
fld public final static java.lang.String ATTR_STATUS = "status"
fld public final static java.lang.String ATTR_TEST_INSTANCE_NAME = "test-instance-name"
fld public final static java.lang.String ATTR_URL = "url"
fld public final static java.lang.String TAG_ATTRIBUTE = "attribute"
fld public final static java.lang.String TAG_ATTRIBUTES = "attributes"
fld public final static java.lang.String TAG_CLASS = "class"
fld public final static java.lang.String TAG_CUSTOM_ATTRIBUTE = "custom-attribute"
fld public final static java.lang.String TAG_CUSTOM_ATTRIBUTES = "custom-attributes"
fld public final static java.lang.String TAG_CUSTOM_ATTRIBUTE_NAME = "name"
fld public final static java.lang.String TAG_CUSTOM_ATTRIBUTE_VALUE = "value"
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
innr public final static !enum StackTraceLevels
intf org.testng.reporters.IReporterConfig
meth public boolean isGenerateDependsOnGroups()
meth public boolean isGenerateDependsOnMethods()
meth public boolean isGenerateGroupsAttribute()
meth public boolean isGenerateTestResultAttributes()
meth public boolean isSplitClassAndPackageNames()
meth public int getFileFragmentationLevel()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String getTimestampFormat()
meth public org.testng.reporters.XMLReporterConfig$StackTraceLevels getStackTraceOutput()
meth public org.testng.reporters.XMLReporterConfig$StackTraceLevels getStackTraceOutputLevelForPassedTests()
meth public static java.lang.Integer getStatus(java.lang.String)
meth public void setFileFragmentationLevel(int)
meth public void setGenerateDependsOnGroups(boolean)
meth public void setGenerateDependsOnMethods(boolean)
meth public void setGenerateGroupsAttribute(boolean)
meth public void setGenerateTestResultAttributes(boolean)
meth public void setOutputDirectory(java.lang.String)
meth public void setSplitClassAndPackageNames(boolean)
meth public void setStackTraceOutput(org.testng.reporters.XMLReporterConfig$StackTraceLevels)
meth public void setTimestampFormat(java.lang.String)
supr java.lang.Object
hfds FMT_DEFAULT,STATUSES,fileFragmentationLevel,generateDependsOnGroups,generateDependsOnMethods,generateGroupsAttribute,generateTestResultAttributes,outputDirectory,splitClassAndPackageNames,stackTraceOutputLevel,stackTraceOutputMethod,timestampFormat

CLSS public final static !enum org.testng.reporters.XMLReporterConfig$StackTraceLevels
 outer org.testng.reporters.XMLReporterConfig
fld public final static org.testng.reporters.XMLReporterConfig$StackTraceLevels BOTH
fld public final static org.testng.reporters.XMLReporterConfig$StackTraceLevels FULL
fld public final static org.testng.reporters.XMLReporterConfig$StackTraceLevels NONE
fld public final static org.testng.reporters.XMLReporterConfig$StackTraceLevels SHORT
meth public int getLevel()
meth public java.lang.String toString()
meth public static org.testng.reporters.XMLReporterConfig$StackTraceLevels parse(int)
meth public static org.testng.reporters.XMLReporterConfig$StackTraceLevels parse(java.lang.String)
meth public static org.testng.reporters.XMLReporterConfig$StackTraceLevels valueOf(java.lang.String)
meth public static org.testng.reporters.XMLReporterConfig$StackTraceLevels[] values()
supr java.lang.Enum<org.testng.reporters.XMLReporterConfig$StackTraceLevels>
hfds level

CLSS public org.testng.reporters.XMLStringBuffer
cons public init()
cons public init(java.lang.String)
cons public init(org.testng.reporters.IBuffer,java.lang.String)
fld public final static java.lang.String EOL
meth public !varargs void addEmptyElement(java.lang.String,java.lang.String[])
meth public !varargs void addOptional(java.lang.String,java.lang.String,java.lang.String[])
meth public !varargs void addRequired(java.lang.String,java.lang.String,java.lang.String[])
meth public !varargs void push(java.lang.String,java.lang.String[])
meth public java.lang.String getCurrentIndent()
meth public java.lang.String toXML()
meth public org.testng.reporters.IBuffer getStringBuffer()
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
meth public void setDefaultComment(java.lang.String)
meth public void setDocType(java.lang.String)
meth public void setXmlDetails(java.lang.String,java.lang.String)
meth public void toWriter(java.io.Writer)
supr java.lang.Object
hfds DEFAULT_INDENT_INCREMENT,INVALID_XML_CHARS,defaultComment,m_buffer,m_currentIndent,m_tagStack

CLSS public org.testng.reporters.XMLSuiteResultWriter
cons public init(org.testng.reporters.XMLReporterConfig,org.testng.reporters.ICustomizeXmlReport)
meth public void addTestMethodParams(org.testng.reporters.XMLStringBuffer,org.testng.ITestResult)
meth public void writeSuiteResult(org.testng.reporters.XMLStringBuffer,org.testng.ISuiteResult)
supr java.lang.Object
hfds config,customizer

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

CLSS public final org.testng.reporters.util.StackTraceTools
meth public static int getTestRoot(java.lang.StackTraceElement[],org.testng.ITestNGMethod)
meth public static java.lang.StackTraceElement[] getTestNGInfrastructure(java.lang.StackTraceElement[],org.testng.ITestNGMethod)
supr java.lang.Object

CLSS public abstract interface org.testng.thread.IThreadWorkerFactory<%0 extends java.lang.Object>
meth public abstract java.util.List<org.testng.thread.IWorker<{org.testng.thread.IThreadWorkerFactory%0}>> createWorkers(java.util.List<{org.testng.thread.IThreadWorkerFactory%0}>)

CLSS public abstract interface org.testng.thread.IWorker<%0 extends java.lang.Object>
intf java.lang.Comparable<org.testng.thread.IWorker<{org.testng.thread.IWorker%0}>>
intf java.lang.Runnable
meth public abstract int getPriority()
meth public abstract java.util.List<{org.testng.thread.IWorker%0}> getTasks()
meth public abstract long getTimeOut()
meth public boolean completed()
meth public long getCurrentThreadId()
meth public long getThreadIdToRunOn()
meth public void setThreadIdToRunOn(long)

CLSS public abstract org.testng.util.RetryAnalyzerCount
cons public init()
intf org.testng.IRetryAnalyzer
meth protected int getCount()
meth protected void setCount(int)
meth public abstract boolean retryMethod(org.testng.ITestResult)
meth public boolean retry(org.testng.ITestResult)
supr java.lang.Object
hfds count

CLSS public final org.testng.util.Strings
meth public static boolean isBlankStringList(java.util.List<java.lang.String>)
meth public static boolean isNotNullAndNotEmpty(java.lang.String)
meth public static boolean isNullOrEmpty(java.lang.String)
meth public static java.lang.String escapeHtml(java.lang.String)
meth public static java.lang.String join(java.lang.String,java.lang.String[])
meth public static java.lang.String valueOf(java.util.Map<?,?>)
supr java.lang.Object
hfds ESCAPE_HTML_MAP

CLSS public final org.testng.util.TimeUtils
innr public abstract interface static Task
meth public static java.lang.String formatTimeInLocalOrSpecifiedTimeZone(long,java.lang.String)
meth public static void computeAndShowTime(java.lang.String,org.testng.util.TimeUtils$Task)
supr java.lang.Object

CLSS public abstract interface static org.testng.util.TimeUtils$Task
 outer org.testng.util.TimeUtils
 anno 0 java.lang.FunctionalInterface()
meth public abstract void execute()

CLSS public abstract interface org.testng.xml.IFileParser<%0 extends java.lang.Object>
meth public abstract {org.testng.xml.IFileParser%0} parse(java.lang.String,java.io.InputStream,boolean)

CLSS public abstract interface org.testng.xml.IPostProcessor
meth public abstract java.util.Collection<org.testng.xml.XmlSuite> process(java.util.Collection<org.testng.xml.XmlSuite>)

CLSS public abstract interface org.testng.xml.ISuiteParser
intf org.testng.xml.IFileParser<org.testng.xml.XmlSuite>
meth public abstract boolean accept(java.lang.String)

CLSS public abstract interface org.testng.xml.IWeaveXml
meth public abstract java.lang.String asXml(org.testng.xml.XmlSuite)
meth public abstract java.lang.String asXml(org.testng.xml.XmlTest,java.lang.String)

CLSS public org.testng.xml.SuiteXmlParser
cons public init()
intf org.testng.xml.ISuiteParser
meth public boolean accept(java.lang.String)
meth public org.testng.xml.XmlSuite parse(java.lang.String,java.io.InputStream,boolean)
supr org.testng.xml.XMLParser<org.testng.xml.XmlSuite>

CLSS public org.testng.xml.TestNGContentHandler
cons public init(java.lang.String,boolean)
meth public org.testng.xml.XmlSuite getSuite()
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int)
meth public void endElement(java.lang.String,java.lang.String,java.lang.String)
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes)
meth public void xmlClasses(boolean)
meth public void xmlGroup(boolean,org.xml.sax.Attributes)
meth public void xmlGroups(boolean)
meth public void xmlListener(boolean,org.xml.sax.Attributes)
meth public void xmlListeners(boolean)
meth public void xmlMethodSelector(boolean)
meth public void xmlMethodSelectors(boolean)
meth public void xmlPackages(boolean)
meth public void xmlRun(boolean)
meth public void xmlSelectorClass(boolean,org.xml.sax.Attributes)
supr org.xml.sax.helpers.DefaultHandler
hfds isSuiteFileTag,m_currentClass,m_currentClassIndex,m_currentClassParameters,m_currentClasses,m_currentDefine,m_currentExcludedMethods,m_currentExpression,m_currentGroups,m_currentInclude,m_currentIncludeIndex,m_currentIncludedMethods,m_currentLanguage,m_currentPackage,m_currentPackages,m_currentRun,m_currentSelector,m_currentSelectors,m_currentSuite,m_currentSuiteParameters,m_currentTest,m_currentTestIndex,m_currentTestParameters,m_enabledTest,m_fileName,m_hasWarn,m_listeners,m_loadClasses,m_locations,m_redirectionAwareResolver,m_suiteFiles,m_suites,m_validate
hcls Include,Location

CLSS public abstract org.testng.xml.XMLParser<%0 extends java.lang.Object>
cons public init()
intf org.testng.xml.IFileParser<{org.testng.xml.XMLParser%0}>
meth public void parse(java.io.InputStream,org.xml.sax.helpers.DefaultHandler) throws java.io.IOException,org.xml.sax.SAXException
supr java.lang.Object
hfds lock,m_saxParser

CLSS public org.testng.xml.XmlClass
cons public init()
cons public init(java.lang.Class)
cons public init(java.lang.Class,boolean)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean loadClasses()
meth public int getIndex()
meth public int hashCode()
meth public java.lang.Class<?> getSupportClass()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.String> getExcludedMethods()
meth public java.util.List<org.testng.xml.XmlInclude> getIncludedMethods()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getLocalParameters()
meth public static java.lang.String listToString(java.util.List<java.lang.Integer>)
meth public void setClass(java.lang.Class)
meth public void setExcludedMethods(java.util.List<java.lang.String>)
meth public void setIncludedMethods(java.util.List<org.testng.xml.XmlInclude>)
meth public void setIndex(int)
meth public void setName(java.lang.String)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setXmlTest(org.testng.xml.XmlTest)
supr java.lang.Object
hfds m_class,m_excludedMethods,m_includedMethods,m_index,m_loadClasses,m_name,m_parameters,m_xmlTest

CLSS public org.testng.xml.XmlDefine
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
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
meth public boolean equals(java.lang.Object)
meth public int getIndex()
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String toXml(java.lang.String)
meth public java.util.List<java.lang.Integer> getInvocationNumbers()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getLocalParameters()
meth public void addInvocationNumbers(java.util.List<java.lang.Integer>)
meth public void addParameter(java.lang.String,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setName(java.lang.String)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setXmlClass(org.testng.xml.XmlClass)
supr java.lang.Object
hfds m_description,m_index,m_invocationNumbers,m_name,m_parameters,m_xmlClass

CLSS public org.testng.xml.XmlMethodSelector
cons public init()
meth public boolean equals(java.lang.Object)
meth public int getPriority()
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String toXml(java.lang.String)
meth public org.testng.xml.XmlScript getScript()
meth public void setClassName(java.lang.String)
meth public void setElement(java.lang.String,java.lang.String)
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
hfds m_exclude,m_include,m_name,m_xmlClasses

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
meth public java.lang.String getExpression()
meth public java.lang.String getLanguage()
meth public void setExpression(java.lang.String)
meth public void setLanguage(java.lang.String)
supr java.lang.Object
hfds expression,language

CLSS public org.testng.xml.XmlSuite
cons public init()
fld public final java.util.UUID SUITE_ID
fld public final static java.lang.Boolean DEFAULT_ALLOW_RETURN_VALUES
fld public final static java.lang.Boolean DEFAULT_GROUP_BY_INSTANCES
fld public final static java.lang.Boolean DEFAULT_MIXED
fld public final static java.lang.Boolean DEFAULT_PRESERVE_ORDER
fld public final static java.lang.Boolean DEFAULT_SKIP_FAILED_INVOCATION_COUNTS
fld public final static java.lang.Integer DEFAULT_DATA_PROVIDER_THREAD_COUNT
fld public final static java.lang.Integer DEFAULT_THREAD_COUNT
fld public final static java.lang.Integer DEFAULT_VERBOSE
fld public final static org.testng.xml.XmlSuite$FailurePolicy DEFAULT_CONFIG_FAILURE_POLICY
fld public final static org.testng.xml.XmlSuite$ParallelMode DEFAULT_PARALLEL
innr public final static !enum FailurePolicy
innr public final static !enum ParallelMode
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isParsed()
meth public boolean isShareThreadPoolForDataProviders()
meth public boolean useGlobalThreadPool()
meth public int getDataProviderThreadCount()
meth public int getThreadCount()
meth public int hashCode()
meth public java.lang.Boolean getAllowReturnValues()
meth public java.lang.Boolean getGroupByInstances()
meth public java.lang.Boolean getPreserveOrder()
meth public java.lang.Boolean skipFailedInvocationCounts()
meth public java.lang.Class<? extends org.testng.ITestObjectFactory> getObjectFactoryClass()
meth public java.lang.Integer getVerbose()
meth public java.lang.Object clone()
meth public java.lang.String getFileName()
meth public java.lang.String getGuiceStage()
meth public java.lang.String getName()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getParentModule()
meth public java.lang.String getTest()
meth public java.lang.String getTimeOut()
meth public java.lang.String toString()
meth public java.lang.String toXml()
meth public java.util.Collection<java.lang.String> getPackageNames()
meth public java.util.List<java.lang.String> getExcludedGroups()
meth public java.util.List<java.lang.String> getIncludedGroups()
meth public java.util.List<java.lang.String> getListeners()
meth public java.util.List<java.lang.String> getLocalListeners()
meth public java.util.List<java.lang.String> getSuiteFiles()
meth public java.util.List<org.testng.xml.XmlMethodSelector> getMethodSelectors()
meth public java.util.List<org.testng.xml.XmlPackage> getPackages()
meth public java.util.List<org.testng.xml.XmlPackage> getXmlPackages()
meth public java.util.List<org.testng.xml.XmlSuite> getChildSuites()
meth public java.util.List<org.testng.xml.XmlTest> getTests()
meth public java.util.Map<java.lang.String,java.lang.String> getAllParameters()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
meth public long getTimeOut(long)
meth public org.testng.xml.XmlGroups getGroups()
meth public org.testng.xml.XmlMethodSelectors getXmlMethodSelectors()
meth public org.testng.xml.XmlSuite getParentSuite()
meth public org.testng.xml.XmlSuite shallowCopy()
meth public org.testng.xml.XmlSuite$FailurePolicy getConfigFailurePolicy()
meth public org.testng.xml.XmlSuite$ParallelMode getParallel()
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
meth public void setConfigFailurePolicy(org.testng.xml.XmlSuite$FailurePolicy)
meth public void setDataProviderThreadCount(int)
meth public void setExcludedGroups(java.util.List<java.lang.String>)
meth public void setFileName(java.lang.String)
meth public void setGroupByInstances(boolean)
meth public void setGroups(org.testng.xml.XmlGroups)
meth public void setGuiceStage(java.lang.String)
meth public void setIncludedGroups(java.util.List<java.lang.String>)
meth public void setListeners(java.util.List<java.lang.String>)
meth public void setMethodSelectors(java.util.List<org.testng.xml.XmlMethodSelector>)
meth public void setMethodSelectors(org.testng.xml.XmlMethodSelectors)
meth public void setName(java.lang.String)
meth public void setObjectFactoryClass(java.lang.Class<? extends org.testng.ITestObjectFactory>)
meth public void setPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setParallel(org.testng.xml.XmlSuite$ParallelMode)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setParentModule(java.lang.String)
meth public void setParentSuite(org.testng.xml.XmlSuite)
meth public void setParsed(boolean)
meth public void setPreserveOrder(java.lang.Boolean)
meth public void setShareThreadPoolForDataProviders(boolean)
meth public void setSkipFailedInvocationCounts(boolean)
meth public void setSuiteFiles(java.util.List<java.lang.String>)
meth public void setTests(java.util.List<org.testng.xml.XmlTest>)
meth public void setThreadCount(int)
meth public void setTimeOut(java.lang.String)
meth public void setVerbose(java.lang.Integer)
meth public void setXmlMethodSelectors(org.testng.xml.XmlMethodSelectors)
meth public void setXmlPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void shouldUseGlobalThreadPool(boolean)
supr java.lang.Object
hfds DEFAULT_SUITE_NAME,m_allowReturnValues,m_childSuites,m_configFailurePolicy,m_dataProviderThreadCount,m_fileName,m_groupByInstances,m_guiceStage,m_listeners,m_methodSelectors,m_name,m_objectFactoryClass,m_parallel,m_parameters,m_parentModule,m_parentSuite,m_preserveOrder,m_skipFailedInvocationCounts,m_suiteFiles,m_test,m_tests,m_threadCount,m_timeOut,m_verbose,m_xmlGroups,m_xmlMethodSelectors,m_xmlPackages,parsed,shareThreadPoolForDataProviders,useGlobalThreadPool

CLSS public final static !enum org.testng.xml.XmlSuite$FailurePolicy
 outer org.testng.xml.XmlSuite
fld public final static org.testng.xml.XmlSuite$FailurePolicy CONTINUE
fld public final static org.testng.xml.XmlSuite$FailurePolicy SKIP
meth public java.lang.String toString()
meth public static org.testng.xml.XmlSuite$FailurePolicy getValidPolicy(java.lang.String)
meth public static org.testng.xml.XmlSuite$FailurePolicy valueOf(java.lang.String)
meth public static org.testng.xml.XmlSuite$FailurePolicy[] values()
supr java.lang.Enum<org.testng.xml.XmlSuite$FailurePolicy>
hfds name

CLSS public final static !enum org.testng.xml.XmlSuite$ParallelMode
 outer org.testng.xml.XmlSuite
fld public final static org.testng.xml.XmlSuite$ParallelMode CLASSES
fld public final static org.testng.xml.XmlSuite$ParallelMode INSTANCES
fld public final static org.testng.xml.XmlSuite$ParallelMode METHODS
fld public final static org.testng.xml.XmlSuite$ParallelMode NONE
fld public final static org.testng.xml.XmlSuite$ParallelMode TESTS
meth public boolean isParallel()
meth public java.lang.String toString()
meth public static org.testng.xml.XmlSuite$ParallelMode getValidParallel(java.lang.String)
meth public static org.testng.xml.XmlSuite$ParallelMode valueOf(java.lang.String)
meth public static org.testng.xml.XmlSuite$ParallelMode[] values()
supr java.lang.Enum<org.testng.xml.XmlSuite$ParallelMode>
hfds isParallel,name

CLSS public org.testng.xml.XmlTest
cons public init()
cons public init(org.testng.xml.XmlSuite)
cons public init(org.testng.xml.XmlSuite,int)
fld public final static int DEFAULT_TIMEOUT_MS = 2147483647
intf java.lang.Cloneable
meth public !varargs void addMetaGroup(java.lang.String,java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public boolean getGroupByInstances()
meth public boolean isGroupFilteringDisabled()
meth public boolean nameMatchesAny(java.util.List<java.lang.String>)
meth public boolean skipFailedInvocationCounts()
meth public final org.testng.xml.XmlSuite getSuite()
meth public int getIndex()
meth public int getThreadCount()
meth public int getVerbose()
meth public int hashCode()
meth public java.lang.Boolean getAllowReturnValues()
meth public java.lang.Boolean getPreserveOrder()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getTimeOut()
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
meth public java.util.Map<java.lang.String,java.lang.String> getXmlDependencyGroups()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getMetaGroups()
meth public long getTimeOut(long)
meth public org.testng.xml.XmlGroups getXmlGroups()
meth public org.testng.xml.XmlScript getScript()
meth public org.testng.xml.XmlSuite$ParallelMode getParallel()
meth public void addExcludedGroup(java.lang.String)
meth public void addIncludedGroup(java.lang.String)
meth public void addMetaGroup(java.lang.String,java.util.List<java.lang.String>)
meth public void addParameter(java.lang.String,java.lang.String)
meth public void addXmlDependencyGroup(java.lang.String,java.lang.String)
meth public void setAllowReturnValues(java.lang.Boolean)
meth public void setClasses(java.util.List<org.testng.xml.XmlClass>)
meth public void setExcludedGroups(java.util.List<java.lang.String>)
meth public void setGroupByInstances(boolean)
meth public void setGroups(org.testng.xml.XmlGroups)
meth public void setIncludedGroups(java.util.List<java.lang.String>)
meth public void setIndex(int)
meth public void setMetaGroups(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth public void setMethodSelectors(java.util.List<org.testng.xml.XmlMethodSelector>)
meth public void setName(java.lang.String)
meth public void setPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setParallel(org.testng.xml.XmlSuite$ParallelMode)
meth public void setParameters(java.util.Map<java.lang.String,java.lang.String>)
meth public void setPreserveOrder(java.lang.Boolean)
meth public void setScript(org.testng.xml.XmlScript)
meth public void setSkipFailedInvocationCounts(boolean)
meth public void setSuite(org.testng.xml.XmlSuite)
meth public void setThreadCount(int)
meth public void setTimeOut(long)
meth public void setVerbose(int)
meth public void setXmlClasses(java.util.List<org.testng.xml.XmlClass>)
meth public void setXmlDependencyGroups(java.util.Map<java.lang.String,java.lang.String>)
meth public void setXmlPackages(java.util.List<org.testng.xml.XmlPackage>)
meth public void setXmlSuite(org.testng.xml.XmlSuite)
supr java.lang.Object
hfds m_allowReturnValues,m_failedInvocationNumbers,m_groupByInstances,m_index,m_methodSelectors,m_name,m_parallel,m_parameters,m_preserveOrder,m_skipFailedInvocationCounts,m_suite,m_threadCount,m_timeOut,m_verbose,m_xmlClasses,m_xmlDependencyGroups,m_xmlGroups,m_xmlPackages

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
meth public void declaration(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

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

