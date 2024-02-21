#Signature file v4.1
#Version 1.43

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

CLSS public final org.netbeans.modules.java.testrunner.GuiUtils
cons public init()
fld public final static java.lang.String CHK_ABSTRACT_CLASSES = "AbstractImpl"
fld public final static java.lang.String CHK_AFTER_CLASS = "AfterClass"
fld public final static java.lang.String CHK_BEFORE_CLASS = "BeforeClass"
fld public final static java.lang.String CHK_EXCEPTION_CLASSES = "Exceptions"
fld public final static java.lang.String CHK_HINTS = "Comments"
fld public final static java.lang.String CHK_INTEGRATION_TESTS = "IntegrationTests"
fld public final static java.lang.String CHK_JAVADOC = "JavaDoc"
fld public final static java.lang.String CHK_METHOD_BODIES = "Content"
fld public final static java.lang.String CHK_PACKAGE = "Package"
fld public final static java.lang.String CHK_PACKAGE_PRIVATE_CLASSES = "PackagePrivateClasses"
fld public final static java.lang.String CHK_PROTECTED = "Protected"
fld public final static java.lang.String CHK_PUBLIC = "Public"
fld public final static java.lang.String CHK_SETUP = "SetUp"
fld public final static java.lang.String CHK_SUITES = "GenerateSuites"
fld public final static java.lang.String CHK_TEARDOWN = "TearDown"
fld public final static java.lang.String JUNIT_TEST_FRAMEWORK = "JUnit"
fld public final static java.lang.String TEMPLATES_DIR = "Templates/JUnit"
fld public final static java.lang.String TESTNG_TEST_FRAMEWORK = "TestNG"
meth public static javax.swing.JCheckBox[] createCheckBoxes(java.lang.String[])
meth public static javax.swing.JComboBox createTemplateChooser(java.lang.String)
meth public static javax.swing.JComponent createChkBoxGroup(java.lang.String,javax.swing.JCheckBox[])
meth public static javax.swing.text.JTextComponent createMultilineLabel(java.lang.String)
meth public static javax.swing.text.JTextComponent createMultilineLabel(java.lang.String,java.awt.Color)
supr java.lang.Object

CLSS public final org.netbeans.modules.java.testrunner.JavaRegexpPatterns
fld public final static java.lang.String JAVA_ID_REGEX = "(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*"
fld public final static java.lang.String JAVA_ID_REGEX_FULL = "(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*"
supr java.lang.Object
hfds JAVA_ID_PART_REGEX,JAVA_ID_START_REGEX

CLSS public final org.netbeans.modules.java.testrunner.JavaRegexpUtils
fld public final static java.lang.String ADD_ERROR_PREFIX = "addError"
fld public final static java.lang.String ADD_FAILURE_PREFIX = "addFailure"
fld public final static java.lang.String CALLSTACK_LINE_PREFIX = "at "
fld public final static java.lang.String CALLSTACK_LINE_PREFIX_CATCH = "[catch] "
fld public final static java.lang.String CALLSTACK_LINE_REGEX = "(?:\u005ct\u005ct?|  +| *\u005ct? *\u005c[catch\u005c] )at (?:(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*/)?(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)+(?:\u005c.<init>)?(?: ?\u005c([^()]+\u005c))?"
fld public final static java.lang.String COMPARISON_HIDDEN_REGEX = ".*expected:<(.*)> but was:<(.*)>.*"
fld public final static java.lang.String COMPARISON_REGEX = ".*expected:<(.*)\u005c[(.*)\u005c](.*)> but was:<(.*)\u005c[(.*)\u005c](.*)>.*"
fld public final static java.lang.String END_OF_TEST_PREFIX = "endTest"
fld public final static java.lang.String FLOAT_NUMBER_REGEX = "[0-9]*(?:\u005c.[0-9]+)?"
fld public final static java.lang.String LOCATION_IN_FILE_REGEX = "(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*(?:\u005c:[0-9]+)?"
fld public final static java.lang.String NESTED_EXCEPTION_PREFIX = "Caused by: "
fld public final static java.lang.String NESTED_EXCEPTION_REGEX = "((?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*)(?:: (.*))?"
fld public final static java.lang.String OUTPUT_DELIMITER_PREFIX = "--------"
fld public final static java.lang.String OUTPUT_DELIMITER_REGEX = "-{8,} (?:Standard (Output|Error)|-{3,}) -{8,}"
fld public final static java.lang.String SECONDS_REGEX = "s(?:ec(?:ond)?(?:s|\u005c(s\u005c))?)?"
fld public final static java.lang.String START_OF_TEST_PREFIX = "startTest"
fld public final static java.lang.String STDERR_LABEL = "Error"
fld public final static java.lang.String STDOUT_LABEL = "Output"
fld public final static java.lang.String TESTCASE_EXCEPTION_REGEX = "((?:(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*\u005c.?(?:Exception|Error|ComparisonFailure))|java\u005c.lang\u005c.Throwable)(?: *: *(.*))?"
fld public final static java.lang.String TESTCASE_HEADER_BRIEF_REGEX = "\u005cp{Blank}*((?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*) *\u005c( *((?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*(?:\u005c.(?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)*) *\u005c) *:\u005cp{Blank}*(?:(FAILED) *|(?i:.*\u005cberror\u005cb.*))"
fld public final static java.lang.String TESTCASE_HEADER_PLAIN_REGEX = "\u005cp{Blank}*((?:\u005cp{javaJavaIdentifierStart})(?:\u005cp{javaJavaIdentifierPart})*)\u005cp{Blank}+took\u005cp{Blank}+(.+)s(?:ec(?:ond)?(?:s|\u005c(s\u005c))?)?"
fld public final static java.lang.String TESTCASE_ISSUE_REGEX = "\u005cp{Blank}*(?:(FAILED) *|(?i:.*\u005cberror\u005cb.*))"
fld public final static java.lang.String TESTCASE_PREFIX = "Testcase: "
fld public final static java.lang.String TESTSUITE_PREFIX = "Testsuite: "
fld public final static java.lang.String TESTSUITE_STATS_190_REGEX = "Tests run: +([0-9]+), +Failures: +([0-9]+), +Errors: +([0-9]+),( +Skipped: +([0-9]+),)? +Time elapsed: +(.+)s(?:ec(?:ond)?(?:s|\u005c(s\u005c))?)?"
fld public final static java.lang.String TESTSUITE_STATS_PREFIX = "Tests run: "
fld public final static java.lang.String TESTSUITE_STATS_REGEX = "Tests run: +([0-9]+), +Failures: +([0-9]+), +Errors: +([0-9]+), +Time elapsed: +(.+)s(?:ec(?:ond)?(?:s|\u005c(s\u005c))?)?"
fld public final static java.lang.String TESTS_COUNT_PREFIX = "tests to run: "
fld public final static java.lang.String TEST_LISTENER_PREFIX = "junit.framework.TestListener: "
fld public final static java.lang.String XML_DECL_PREFIX = "<?xml"
fld public final static java.lang.String XML_DECL_REGEX = "\u005cQ<?xml\u005cE[ \u005ct\u005cr\u005cn]+version[ \u005ct\u005cr\u005cn]*=[ \u005ct\u005cr\u005cn]*(?:\u00221\u005c.0\u0022|'1\u005c.0')(?:[ \u005ct\u005cr\u005cn]+encoding[ \u005ct\u005cr\u005cn]*=[ \u005ct\u005cr\u005cn]*(['\u0022])[A-Za-z][-A-Za-z0-9._]*\u005c1)?(?:[ \u005ct\u005cr\u005cn]+standalone[ \u005ct\u005cr\u005cn]*=[ \u005ct\u005cr\u005cn]*(['\u0022])(?:yes|no)\u005c2)?[ \u005ct\u005cr\u005cn]*\u005c?>"
fld public final static java.lang.String XML_ENC_REGEX = "[A-Za-z][-A-Za-z0-9._]*"
fld public final static java.lang.String XML_EQ_REGEX = "[ \u005ct\u005cr\u005cn]*=[ \u005ct\u005cr\u005cn]*"
fld public final static java.lang.String XML_SPACE_REGEX = "[ \u005ct\u005cr\u005cn]"
fld public final static java.util.regex.Pattern CLASSPATH_ARGS
fld public final static java.util.regex.Pattern JAVA_EXECUTABLE
meth public int parseTimeMillis(java.lang.String)
meth public int parseTimeMillisNoNFE(java.lang.String)
meth public java.util.regex.Pattern getCallstackLinePattern()
meth public java.util.regex.Pattern getComparisonHiddenPattern()
meth public java.util.regex.Pattern getComparisonPattern()
meth public java.util.regex.Pattern getFloatNumPattern()
meth public java.util.regex.Pattern getFullJavaIdPattern()
meth public java.util.regex.Pattern getLocationInFilePattern()
meth public java.util.regex.Pattern getNestedExceptionPattern()
meth public java.util.regex.Pattern getOutputDelimPattern()
meth public java.util.regex.Pattern getSuiteStats190Pattern()
meth public java.util.regex.Pattern getSuiteStatsPattern()
meth public java.util.regex.Pattern getTestcaseExceptionPattern()
meth public java.util.regex.Pattern getTestcaseHeaderBriefPattern()
meth public java.util.regex.Pattern getTestcaseHeaderPlainPattern()
meth public java.util.regex.Pattern getTestcaseIssuePattern()
meth public java.util.regex.Pattern getXmlDeclPattern()
meth public static java.lang.String specialTrim(java.lang.String)
meth public static org.netbeans.modules.java.testrunner.JavaRegexpUtils getInstance()
supr java.lang.Object
hfds callstackLinePattern,comparisonHiddenPattern,comparisonPattern,floatNumPattern,fullJavaIdPattern,instRef,locationInFilePattern,nestedExceptPattern,outputDelimPattern,suiteStats190Pattern,suiteStatsPattern,testcaseExceptPattern,testcaseHeaderBriefPattern,testcaseHeaderPlainPattern,testcaseIssuePattern,xmlDeclPattern

CLSS public final org.netbeans.modules.java.testrunner.JavaUtils
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String PROP_JUNIT_SELECTED_VERSION = "junit.selected.version"
meth public java.lang.Object[] getTestTargets(org.netbeans.api.project.SourceGroup,boolean)
meth public org.netbeans.api.project.SourceGroup[] getJavaSourceGroups()
meth public org.openide.filesystems.FileObject[] getSourceFoldersRaw(org.openide.filesystems.FileObject)
meth public org.openide.filesystems.FileObject[] getTestFoldersRaw(org.openide.filesystems.FileObject)
meth public static boolean isSupportEnabled(java.lang.Class,org.openide.filesystems.FileObject[])
meth public static java.lang.Object[] getTestSourceRoots(java.util.Collection<org.netbeans.api.project.SourceGroup>,org.openide.filesystems.FileObject)
meth public static java.lang.String[] getSourceAndTestClassNames(org.openide.filesystems.FileObject,boolean,boolean)
meth public static java.util.Collection<org.openide.filesystems.FileObject> getTestFolders(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject[] skipNulls(org.openide.filesystems.FileObject[])
supr java.lang.Object
hfds foldersToSourceGroupsMap,javaSourceGroups,project,sourceGroupsOnly,sourcesToTestsMap

CLSS public final org.netbeans.modules.java.testrunner.OutputUtils
meth public static org.netbeans.api.java.platform.JavaPlatform getActivePlatform(java.lang.String)
meth public static org.netbeans.spi.project.ActionProvider getActionProvider(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds NO_ACTIONS

