#Signature file v4.1
#Version 2.43

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

CLSS public org.netbeans.modules.gsf.testrunner.api.TestSuite
cons public init(java.lang.String)
fld public final static java.lang.String ANONYMOUS_SUITE
fld public final static org.netbeans.modules.gsf.testrunner.api.TestSuite ANONYMOUS_TEST_SUITE
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.gsf.testrunner.api.Testcase> getTestcases()
meth public org.netbeans.modules.gsf.testrunner.api.Testcase getLastTestCase()
meth public void addTestcase(org.netbeans.modules.gsf.testrunner.api.Testcase)
supr java.lang.Object
hfds MAX_TOOLTIP_LINES,name,testcases

CLSS public org.netbeans.modules.gsf.testrunner.api.Testcase
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public java.lang.String getClassName()
meth public java.lang.String getDisplayName()
meth public java.lang.String getLocation()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.gsf.testrunner.api.OutputLine> getOutput()
meth public long getTimeMillis()
meth public org.netbeans.modules.gsf.testrunner.api.Status getStatus()
meth public org.netbeans.modules.gsf.testrunner.api.TestSession getSession()
meth public org.netbeans.modules.gsf.testrunner.api.Trouble getTrouble()
meth public void addOutputLines(java.util.List<java.lang.String>)
meth public void setClassName(java.lang.String)
meth public void setLocation(java.lang.String)
meth public void setStatus(org.netbeans.modules.gsf.testrunner.api.Status)
meth public void setTimeMillis(long)
meth public void setTrouble(org.netbeans.modules.gsf.testrunner.api.Trouble)
supr java.lang.Object
hfds className,displayName,location,name,output,session,status,timeMillis,trouble,type

CLSS public final org.netbeans.modules.testng.api.TestNGSupport
innr public final static !enum Action
meth public final static boolean isActionSupported(org.netbeans.modules.testng.api.TestNGSupport$Action,org.netbeans.api.project.Project)
meth public final static boolean isSupportEnabled(org.openide.filesystems.FileObject[])
meth public final static org.netbeans.modules.testng.spi.TestNGSupportImplementation findTestNGSupport(org.netbeans.api.project.Project)
supr java.lang.Object
hfds cache,implementations

CLSS public final static !enum org.netbeans.modules.testng.api.TestNGSupport$Action
 outer org.netbeans.modules.testng.api.TestNGSupport
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action CREATE_TEST
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action DEBUG_TEST
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action DEBUG_TESTMETHOD
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action DEBUG_TESTSUITE
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action RUN_FAILED
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action RUN_TESTMETHOD
fld public final static org.netbeans.modules.testng.api.TestNGSupport$Action RUN_TESTSUITE
meth public static org.netbeans.modules.testng.api.TestNGSupport$Action valueOf(java.lang.String)
meth public static org.netbeans.modules.testng.api.TestNGSupport$Action[] values()
supr java.lang.Enum<org.netbeans.modules.testng.api.TestNGSupport$Action>

CLSS public final org.netbeans.modules.testng.api.TestNGTest
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.testng.api.TestNGTestSuite> getTestcases()
meth public void addTestsuite(org.netbeans.modules.testng.api.TestNGTestSuite)
supr java.lang.Object
hfds name,testCases

CLSS public org.netbeans.modules.testng.api.TestNGTestSuite
cons public init(java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
cons public init(java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession,int,java.lang.String)
meth public long getElapsedTime()
meth public org.netbeans.modules.testng.api.TestNGTestcase getTestCase(java.lang.String,java.lang.String)
meth public org.openide.filesystems.FileObject getSuiteFO()
meth public void finish(int,int,int,int,int)
meth public void setElapsedTime(long)
supr org.netbeans.modules.gsf.testrunner.api.TestSuite
hfds cfgFO,elapsedTime,expectedTestCases,session,suiteFO

CLSS public final org.netbeans.modules.testng.api.TestNGTestcase
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public boolean isConfigMethod()
meth public int getInvocationCount()
meth public java.lang.String getDescription()
meth public java.lang.String getParameters()
meth public java.lang.String getTestName()
meth public org.openide.filesystems.FileObject getClassFileObject()
meth public void addValues(java.lang.String)
meth public void setConfigMethod(boolean)
meth public void setDescription(java.lang.String)
supr org.netbeans.modules.gsf.testrunner.api.Testcase
hfds classFO,confMethod,description,parameters,testName,values

CLSS public org.netbeans.modules.testng.api.TestNGUtils
cons public init()
meth public static boolean createTestActionCalled(org.openide.filesystems.FileObject[])
meth public static org.netbeans.modules.java.testrunner.CommonSettings getTestNGSettings()
meth public static org.netbeans.modules.testng.spi.TestConfig getTestConfig(org.openide.filesystems.FileObject,boolean,java.lang.String,java.lang.String,java.lang.String)
meth public static org.openide.filesystems.FileObject[] createTests(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object>)
supr java.lang.Object

CLSS public final org.netbeans.modules.testng.api.XmlOutputParser
meth public static org.netbeans.modules.testng.api.XmlResult parseXmlOutput(java.io.Reader,org.netbeans.modules.gsf.testrunner.api.TestSession) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
supr org.xml.sax.helpers.DefaultHandler
hfds LOG,STATE_CLASS,STATE_EXCEPTION,STATE_FULL_STACKTRACE,STATE_GROUP,STATE_GROUPS,STATE_MESSAGE,STATE_METHOD,STATE_OUT_OF_SCOPE,STATE_SUITE,STATE_TEST,STATE_TEST_METHOD,STATE_TEST_PARAM,STATE_TEST_PARAMS,STATE_TEST_VALUE,allTestsCount,failedConfCount,failedTestsCount,passedTestsCount,reports,skippedConfCount,skippedTestsCount,state,status,suiteTime,tcClassName,test,testSession,testcase,testsuite,text,trouble,xmlReader

CLSS public org.netbeans.modules.testng.api.XmlResult
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.testng.api.TestNGTestSuite> getTestSuites()
meth public void addTestNGTest(org.netbeans.modules.testng.api.TestNGTest)
supr java.lang.Object
hfds name,suites

CLSS public final org.netbeans.modules.testng.spi.TestConfig
meth public boolean doRerun()
meth public java.lang.String getClassName()
meth public java.lang.String getMethodName()
meth public java.lang.String getPackageName()
meth public org.openide.filesystems.FileObject getTest()
supr java.lang.Object
hfds className,methodName,pkgName,rerun,test

CLSS public abstract org.netbeans.modules.testng.spi.TestNGSupportImplementation
cons public init()
innr public abstract interface static TestExecutor
meth public abstract boolean isActionSupported(org.netbeans.modules.testng.api.TestNGSupport$Action,org.netbeans.api.project.Project)
meth public abstract org.netbeans.modules.testng.spi.TestNGSupportImplementation$TestExecutor createExecutor(org.netbeans.api.project.Project)
meth public abstract void configureProject(org.openide.filesystems.FileObject)
meth public boolean isSupportEnabled(org.openide.filesystems.FileObject[])
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface static org.netbeans.modules.testng.spi.TestNGSupportImplementation$TestExecutor
 outer org.netbeans.modules.testng.spi.TestNGSupportImplementation
meth public abstract boolean hasFailedTests()
meth public abstract void execute(org.netbeans.modules.testng.api.TestNGSupport$Action,org.netbeans.modules.testng.spi.TestConfig) throws java.io.IOException

CLSS public final org.netbeans.modules.testng.spi.XMLSuiteSupport
meth public static java.io.File createSuiteforMethod(java.io.File,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
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

