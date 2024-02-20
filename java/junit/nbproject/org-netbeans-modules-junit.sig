#Signature file v4.1
#Version 2.97

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

CLSS public abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin
cons protected init()
innr public final static !enum CreateTestParam
innr public final static Location
meth protected !varargs boolean canCreateTests(org.openide.filesystems.FileObject[])
meth protected abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location getTestLocation(org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location)
meth protected abstract org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location getTestedLocation(org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$Location)
meth protected abstract org.openide.filesystems.FileObject[] createTests(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object>)
meth protected boolean createTestActionCalled(org.openide.filesystems.FileObject[])
supr java.lang.Object

CLSS public org.netbeans.modules.java.testrunner.CommonSettings
cons public init()
fld public final static boolean DEFAULT_GENERATE_CLASS_SETUP
fld public final static boolean DEFAULT_GENERATE_CLASS_TEARDOWN
fld public final static boolean DEFAULT_GENERATE_SETUP
fld public final static boolean DEFAULT_GENERATE_TEARDOWN
fld public final static boolean GENERATE_TESTS_FROM_TEST_CLASSES
fld public final static java.lang.String INTEGRATION_TEST_CLASSNAME_SUFFIX
fld public final static java.lang.String PROP_BODY_COMMENTS = "bodyComments"
fld public final static java.lang.String PROP_BODY_CONTENT = "bodyContent"
fld public final static java.lang.String PROP_GENERATE_ABSTRACT_IMPL = "generateAbstractImpl"
fld public final static java.lang.String PROP_GENERATE_CLASS_SETUP = "generateClassSetUp"
fld public final static java.lang.String PROP_GENERATE_CLASS_TEARDOWN = "generateClassTearDown"
fld public final static java.lang.String PROP_GENERATE_EXCEPTION_CLASSES = "generateExceptionClasses"
fld public final static java.lang.String PROP_GENERATE_INTEGRATION_TESTS = "generateIntegrationTests"
fld public final static java.lang.String PROP_GENERATE_MAIN_METHOD = "generateMainMethod"
fld public final static java.lang.String PROP_GENERATE_MAIN_METHOD_BODY = "generateMainMethodBody"
fld public final static java.lang.String PROP_GENERATE_SETUP = "generateSetUp"
fld public final static java.lang.String PROP_GENERATE_SUITE_CLASSES = "generateSuiteClasses"
fld public final static java.lang.String PROP_GENERATE_TEARDOWN = "generateTearDown"
fld public final static java.lang.String PROP_GENERATOR = "generator"
fld public final static java.lang.String PROP_INCLUDE_PACKAGE_PRIVATE_CLASSES = "includePackagePrivateClasses"
fld public final static java.lang.String PROP_JAVADOC = "javaDoc"
fld public final static java.lang.String PROP_MEMBERS_PACKAGE = "membersPackage"
fld public final static java.lang.String PROP_MEMBERS_PROTECTED = "membersProtected"
fld public final static java.lang.String PROP_MEMBERS_PUBLIC = "membersPublic"
fld public final static java.lang.String PROP_RESULTS_SPLITPANE_DIVIDER = "resultsSplitDivider"
fld public final static java.lang.String PROP_ROOT_SUITE_CLASSNAME = "rootSuiteClassName"
fld public final static java.lang.String SUITE_CLASSNAME_PREFIX
fld public final static java.lang.String SUITE_CLASSNAME_SUFFIX
fld public final static java.lang.String TEST_CLASSNAME_PREFIX
fld public final static java.lang.String TEST_CLASSNAME_SUFFIX
meth public boolean isBodyComments()
meth public boolean isBodyContent()
meth public boolean isGenerateAbstractImpl()
meth public boolean isGenerateClassSetUp()
meth public boolean isGenerateClassTearDown()
meth public boolean isGenerateExceptionClasses()
meth public boolean isGenerateIntegrationTests()
meth public boolean isGenerateMainMethod()
meth public boolean isGenerateSetUp()
meth public boolean isGenerateSuiteClasses()
meth public boolean isGenerateTearDown()
meth public boolean isIncludePackagePrivateClasses()
meth public boolean isJavaDoc()
meth public boolean isMembersPackage()
meth public boolean isMembersProtected()
meth public boolean isMembersPublic()
meth public int getResultsSplitPaneDivider()
meth public java.lang.String displayName()
meth public java.lang.String getGenerateMainMethodBody()
meth public java.lang.String getGenerator()
meth public java.lang.String getRootSuiteClassName()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public static org.netbeans.modules.java.testrunner.CommonSettings getDefault()
meth public void setBodyComments(boolean)
meth public void setBodyContent(boolean)
meth public void setGenerateAbstractImpl(boolean)
meth public void setGenerateClassSetUp(boolean)
meth public void setGenerateClassTearDown(boolean)
meth public void setGenerateExceptionClasses(boolean)
meth public void setGenerateIntegrationTests(boolean)
meth public void setGenerateMainMethod(boolean)
meth public void setGenerateMainMethodBody(java.lang.String)
meth public void setGenerateSetUp(boolean)
meth public void setGenerateSuiteClasses(boolean)
meth public void setGenerateTearDown(boolean)
meth public void setGenerator(java.lang.String)
meth public void setIncludePackagePrivateClasses(boolean)
meth public void setJavaDoc(boolean)
meth public void setMembersPackage(boolean)
meth public void setMembersProtected(boolean)
meth public void setMembersPublic(boolean)
meth public void setResultsSplitPaneDivider(int)
meth public void setRootSuiteClassName(java.lang.String)
supr java.lang.Object
hfds INSTANCE,PROP_FILE_SYSTEM

CLSS public org.netbeans.modules.java.testrunner.CommonTestUtil
cons public init()
meth public static <%0 extends java.lang.Object> {%%0}[] skipNulls({%%0}[],{%%0}[])
meth public static boolean isJavaFile(org.openide.filesystems.FileObject)
meth public static java.lang.Object[] getTestTargets(org.openide.filesystems.FileObject)
meth public static java.util.Collection<org.netbeans.api.project.SourceGroup> findSourceGroupOwners(org.netbeans.api.project.Project,java.lang.String)
meth public static java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object> getSettingsMap(boolean)
meth public static java.util.Map<org.openide.filesystems.FileObject,org.netbeans.api.project.SourceGroup> getFileObject2SourceGroupMap(org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.SourceGroup findSourceGroupOwner(org.netbeans.api.project.Project,org.openide.filesystems.FileObject)
meth public static org.netbeans.api.project.SourceGroup findSourceGroupOwner(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds JAVA_SOURCES_SUFFIX

CLSS public org.netbeans.modules.junit.api.JUnitSettings
cons public init()
meth public java.lang.String getGenerator()
meth public static org.netbeans.modules.junit.api.JUnitSettings getDefault()
supr org.netbeans.modules.java.testrunner.CommonSettings
hfds DEFAULT_GENERATOR,INSTANCE,JUNIT3_GENERATOR,JUNIT4_GENERATOR,JUNIT5_GENERATOR,JUNIT_GENERATOR_ASK_USER

CLSS public org.netbeans.modules.junit.api.JUnitTestSuite
cons public init(java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public long getElapsedTime()
meth public org.openide.filesystems.FileObject getSuiteFO()
meth public void setElapsedTime(long)
supr org.netbeans.modules.gsf.testrunner.api.TestSuite
hfds elapsedTime,session,suiteFO

CLSS public org.netbeans.modules.junit.api.JUnitTestUtil
meth public !varargs static boolean canCreateTests(org.netbeans.modules.junit.plugin.JUnitPlugin,org.openide.filesystems.FileObject[])
meth public static boolean areAnnotationsSupported(java.lang.String)
meth public static boolean areAnnotationsSupported(org.openide.filesystems.FileObject)
meth public static boolean createTestActionCalled(org.netbeans.modules.junit.plugin.JUnitPlugin,org.openide.filesystems.FileObject[])
meth public static boolean isClassException(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement)
meth public static boolean isClassImplementingTestInterface(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement)
meth public static boolean isClassTest(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement)
meth public static boolean isValidPackageName(java.lang.String)
meth public static com.sun.source.tree.ClassTree findMainClass(org.netbeans.api.java.source.CompilationInfo)
meth public static java.lang.String convertClass2TestName(java.lang.String)
meth public static java.lang.String convertPackage2ITSuiteName(java.lang.String)
meth public static java.lang.String convertPackage2SuiteName(java.lang.String)
meth public static java.lang.String createNewName(int,java.util.Set)
meth public static java.lang.String getIntegrationTestClassName(java.lang.String)
meth public static java.lang.String getSimpleName(java.lang.String)
meth public static java.lang.String getSourceLevel(org.openide.filesystems.FileObject)
meth public static java.lang.String getTestClassFullName(java.lang.String,java.lang.String)
meth public static java.lang.String getTestClassName(java.lang.String)
meth public static java.util.List<java.lang.String> getJavaFileNames(org.openide.filesystems.FileObject,org.netbeans.api.java.source.ClasspathInfo)
meth public static org.netbeans.modules.junit.plugin.JUnitPlugin getITPluginForProject(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.junit.plugin.JUnitPlugin getPluginForProject(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.junit.plugin.JUnitPlugin getPluginForProject(org.netbeans.api.project.Project,org.netbeans.modules.junit.api.JUnitVersion)
meth public static org.openide.filesystems.FileObject[] createTests(org.netbeans.modules.junit.plugin.JUnitPlugin,org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject,java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object>)
meth public static void notifyUser(java.lang.String)
meth public static void notifyUser(java.lang.String,int)
supr org.netbeans.modules.java.testrunner.CommonTestUtil
hfds JAVA_MIME_TYPE,JAVA_SOURCES_SUFFIX

CLSS public org.netbeans.modules.junit.api.JUnitTestcase
cons public init(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.gsf.testrunner.api.TestSession)
meth public java.lang.String getName()
meth public org.openide.filesystems.FileObject getClassFileObject()
meth public org.openide.filesystems.FileObject getClassFileObject(boolean)
supr org.netbeans.modules.gsf.testrunner.api.Testcase
hfds classFO

CLSS public final org.netbeans.modules.junit.api.JUnitUtils
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String TESTS_ROOT_NAME = "test"
meth public java.lang.Object[] getTestTargets(org.netbeans.api.project.SourceGroup,boolean)
meth public java.util.Map<org.netbeans.api.project.SourceGroup,java.lang.Object[]> getSourcesToTestsMap(boolean)
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.api.project.SourceGroup[] getJavaSourceGroups()
meth public org.openide.filesystems.FileObject[] getSourceFoldersRaw(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject[] getTestFoldersRaw(org.openide.filesystems.FileObject)
meth public static boolean isInstanceOfDefaultPlugin(org.netbeans.modules.junit.plugin.JUnitPlugin)
meth public static boolean isValidClassName(java.lang.String)
meth public static java.util.Collection getTestTargets(org.netbeans.api.project.Project,boolean)
meth public static java.util.Collection<org.openide.filesystems.FileObject> getTestFolders(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject[] skipNulls(org.openide.filesystems.FileObject[])
meth public static org.openide.loaders.DataObject createSuiteTest(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String,java.util.Map<org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin$CreateTestParam,java.lang.Object>)
meth public static void logJUnitUsage(java.net.URI)
supr java.lang.Object
hfds foldersToSourceGroupsMap,javaSourceGroups,project,sourceGroupsOnly,sourcesToTestsMap

CLSS public final !enum org.netbeans.modules.junit.api.JUnitVersion
fld public final static org.netbeans.modules.junit.api.JUnitVersion JUNIT3
fld public final static org.netbeans.modules.junit.api.JUnitVersion JUNIT4
fld public final static org.netbeans.modules.junit.api.JUnitVersion JUNIT5
meth public static org.netbeans.modules.junit.api.JUnitVersion valueOf(java.lang.String)
meth public static org.netbeans.modules.junit.api.JUnitVersion[] values()
supr java.lang.Enum<org.netbeans.modules.junit.api.JUnitVersion>

CLSS public abstract org.netbeans.modules.junit.plugin.JUnitPlugin
cons public init()
supr org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin

