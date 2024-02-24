#Signature file v4.1
#Version 1.42.0

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

CLSS public org.netbeans.modules.java.hints.test.api.HintTest
innr public final AppliedFix
innr public final HintOutput
innr public final HintWarning
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest classpath(java.net.URL[])
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest options(java.lang.String[])
meth public org.netbeans.modules.java.hints.test.api.HintTest input(java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest input(java.lang.String,boolean) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest input(java.lang.String,java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest input(java.lang.String,java.lang.String,boolean) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest preference(java.lang.String,boolean)
meth public org.netbeans.modules.java.hints.test.api.HintTest preference(java.lang.String,int)
meth public org.netbeans.modules.java.hints.test.api.HintTest preference(java.lang.String,java.lang.String)
meth public org.netbeans.modules.java.hints.test.api.HintTest setCaretMarker(char)
meth public org.netbeans.modules.java.hints.test.api.HintTest sourceLevel(java.lang.String)
meth public org.netbeans.modules.java.hints.test.api.HintTest$HintOutput run(java.lang.Class<?>) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$HintOutput run(java.lang.Class<?>,java.lang.String) throws java.lang.Exception
meth public static org.netbeans.modules.java.hints.test.api.HintTest create() throws java.lang.Exception
supr java.lang.Object
hfds DEBUGGING_HELPER,EMPTY_SFBQ_RESULT,ERRORS_COMPARATOR,INDEXING_LOGGER,JUNIT_PROPERTIES_FILENAME,JUNIT_PROPERTIES_LOCATION_PROPERTY,LEXER_LOG_LOCK,NBJUNIT_WORKDIR,buildRoot,cache,caret,caretMarker,checkCompilable,compileClassPath,extraOptions,hintSettings,log,sourceLevel,sourcePath,sourceRoot,testFile,testPreferences,usedPaths,workDir
hcls DeadlockTask,TempPreferences,TestCompilerOptionsQueryImplementation,TestProxyClassPathProvider,TestSourceForBinaryQuery,TestSourceLevelQueryImplementation

CLSS public final org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix
 outer org.netbeans.modules.java.hints.test.api.HintTest
cons public init(org.netbeans.modules.java.hints.test.api.HintTest)
meth public java.lang.String getOutput() throws java.lang.Exception
meth public java.lang.String getOutput(java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertCompilable() throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertCompilable(java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertOutput(java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertOutput(java.lang.String,java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertVerbatimOutput(java.lang.String) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix assertVerbatimOutput(java.lang.String,java.lang.String) throws java.lang.Exception
supr java.lang.Object

CLSS public final org.netbeans.modules.java.hints.test.api.HintTest$HintOutput
 outer org.netbeans.modules.java.hints.test.api.HintTest
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest$HintOutput assertContainsWarnings(java.lang.String[])
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest$HintOutput assertNotContainsWarnings(java.lang.String[])
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest$HintOutput assertWarnings(java.lang.String[])
meth public org.netbeans.modules.java.hints.test.api.HintTest$HintWarning findWarning(java.lang.String)
supr java.lang.Object
hfds errors,requiresJavaFix

CLSS public final org.netbeans.modules.java.hints.test.api.HintTest$HintWarning
 outer org.netbeans.modules.java.hints.test.api.HintTest
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest$HintWarning assertFixes(java.lang.String[]) throws java.lang.Exception
meth public !varargs org.netbeans.modules.java.hints.test.api.HintTest$HintWarning assertFixesNotPresent(java.lang.String[]) throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix applyFix() throws java.lang.Exception
meth public org.netbeans.modules.java.hints.test.api.HintTest$AppliedFix applyFix(java.lang.String) throws java.lang.Exception
supr java.lang.Object
hfds requiresJavaFix,warning

