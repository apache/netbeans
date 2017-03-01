package completion

import utils.*
import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.completion.*
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.modules.csl.api.CompletionProposal

/**
 *
 * @author Alexander.Baratynski
 */
class CompletionTest : NbTestCase("Completion test") {
    private val project: Project
    private val completionDir: FileObject

    init {
        project = JavaProject.javaProject
        completionDir = project.projectDirectory.getFileObject("src").getFileObject("completion")
    }

    private fun doTest(fileName: String, items: Collection<String>) {
        val doc = getDocumentForFileObject(completionDir, fileName)
        val caret = getCaret(doc)
        assertNotNull(caret)
        
        val file = ProjectUtils.getFileObjectForDocument(doc)
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        
        val resultWithProvider = KotlinAnalyzer.analyzeFile(project, ktFile)
        KotlinParser.setAnalysisResult(ktFile, resultWithProvider)
        
        val completionItems = createProposals(doc, caret, resultWithProvider, "")
        assertNotNull(completionItems)
        
        val completions = completionItems.map { it.sortText }
       
        assertEquals(true, completions.containsAll(items))
    }

    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(completionDir)
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
    
}