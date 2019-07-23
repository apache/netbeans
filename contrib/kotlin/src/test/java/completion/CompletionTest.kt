package completion

import utils.*
import org.jetbrains.kotlin.completion.*
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils

/**
 *
 * @author Alexander.Baratynski
 */
class CompletionTest : KotlinTestCase("Completion test", "completion") {
    
    private fun doTest(fileName: String, items: Collection<String> = emptyList()) {
        val doc = getDocumentForFileObject(dir, fileName)
        val caret = getCaret(doc)
        assertNotNull(caret)
        
        val file = ProjectUtils.getFileObjectForDocument(doc)
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        
        val completionItems = createProposals(doc, caret, resultWithProvider, "")
        assertNotNull(completionItems)
        
        val completions = completionItems.map { it.sortText }
        assertEquals(true, completions.containsAll(items))
    }

    fun testStringCompletion() = doTest("checkStringCompletion.kt", listOf("toString()"))

    fun testBasicInt() = doTest("checkBasicInt.kt", listOf("Int"))

    fun testBasicAny() = doTest("checkBasicAny.kt", listOf("Any"))

    fun testAutoCastAfterIf() = doTest("checkAutoCastAfterIf.kt", listOf("value"))
    
    fun testAutoCastAfterIfMethod() = doTest("checkAutoCastAfterIfMethod.kt", listOf("test()"))

    fun testAutoCastForThis() = doTest("checkAutoCastForThis.kt", listOf("destroy()"))

    fun testAutoCastInWhen() = doTest("checkAutoCastInWhen.kt", listOf("left", "right"))

    fun testCompletionBeforeDotInCall() = doTest("checkCompletionBeforeDotInCall.kt", listOf("TestSample", "testVar", "testTop()", "testFun()"))
    
    fun testLocalLambda() = doTest("checkLocalLambda.kt", listOf("test()"))

    fun testCompanion() = doTest("checkCompanion.kt", listOf("companionVal", "companionFun()"))

    fun testExtendsClass() = doTest("checkExtendClass.kt", listOf("MyFirstClass", "MySecondClass"))

    fun testImport() = doTest("checkImport.kt", listOf("Proxy"))

    fun testInCallExpression() = doTest("checkInCallExpression.kt", listOf("func()"))

    fun testInClassInit() = doTest("checkInClassInit.kt", listOf("valExternal", "valInternal"))

    fun testInClassPropertyAccessor() = doTest("checkInClassPropertyAccessor.kt", listOf("test", "testParam"))

    fun testInImport() = doTest("checkInImport.kt", listOf("Proxy", "Base"))

    fun testInParameterType() = doTest("checkInParameterType.kt", listOf("Int"))

    fun testUpperAndLowerCase() = doTest("checkUpperAndLowerCases.kt", listOf("method()"))
    
    fun testNonImported() = doTest("checkUnimported.kt", listOf("completion.pack.function1", "completion.pack.function2"))

    fun testNonImportedPrivate() = doTest("checkNonImportedPrivate.kt")
        
}