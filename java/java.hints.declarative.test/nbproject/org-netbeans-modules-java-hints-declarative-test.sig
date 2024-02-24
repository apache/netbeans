#Signature file v4.1
#Version 1.39.0

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

CLSS public org.netbeans.modules.java.hints.declarative.test.api.DeclarativeHintsTestBase
cons public init()
cons public init(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.netbeans.modules.java.hints.declarative.test.TestParser$TestCase)
meth protected void runTest() throws java.lang.Throwable
meth protected void setUp() throws java.lang.Exception
meth public static junit.framework.TestSuite suite(java.lang.Class<?>)
meth public static junit.framework.TestSuite suite(java.lang.Class<?>,java.lang.String)
supr org.netbeans.junit.NbTestCase
hfds hintFile,test,testFile

