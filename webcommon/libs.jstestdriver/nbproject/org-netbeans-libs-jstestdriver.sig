#Signature file v4.1
#Version 1.33

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

CLSS public final org.netbeans.libs.jstestdriver.api.BrowserInfo
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getOs()
meth public java.lang.String getVersion()
supr java.lang.Object
hfds name,os,version

CLSS public final org.netbeans.libs.jstestdriver.api.JsTestDriver
cons public init(java.io.File)
meth public boolean isRunning()
meth public boolean wasStartedExternally()
meth public void runTests(java.lang.String,boolean,java.io.File,java.io.File,java.lang.String,org.netbeans.libs.jstestdriver.api.TestListener,org.netbeans.api.extexecution.print.LineConvertor)
meth public void startServer(int,boolean,org.netbeans.libs.jstestdriver.api.ServerListener)
meth public void stopServer()
supr java.lang.Object
hfds impl,jsTestDriverJar

CLSS public abstract interface org.netbeans.libs.jstestdriver.api.ServerListener
meth public abstract void serverStarted()

CLSS public abstract interface org.netbeans.libs.jstestdriver.api.TestListener
innr public static TestResult
meth public abstract void onTestComplete(org.netbeans.libs.jstestdriver.api.TestListener$TestResult)
meth public abstract void onTestingFinished()

CLSS public static org.netbeans.libs.jstestdriver.api.TestListener$TestResult
 outer org.netbeans.libs.jstestdriver.api.TestListener
cons public init(org.netbeans.libs.jstestdriver.api.BrowserInfo,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,long,java.lang.String)
innr public final static !enum Result
meth public java.lang.String getLog()
meth public java.lang.String getMessage()
meth public java.lang.String getStack()
meth public java.lang.String getTestCaseName()
meth public java.lang.String getTestName()
meth public long getDuration()
meth public org.netbeans.libs.jstestdriver.api.BrowserInfo getBrowserInfo()
meth public org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result getResult()
supr java.lang.Object
hfds browserInfo,log,message,result,stack,testCaseName,testName,time

CLSS public final static !enum org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result
 outer org.netbeans.libs.jstestdriver.api.TestListener$TestResult
fld public final static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result error
fld public final static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result failed
fld public final static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result passed
fld public final static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result started
meth public static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result valueOf(java.lang.String)
meth public static org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result[] values()
supr java.lang.Enum<org.netbeans.libs.jstestdriver.api.TestListener$TestResult$Result>

