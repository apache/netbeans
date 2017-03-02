package diagnostics

import utils.*
import com.intellij.psi.PsiErrorElement
import org.netbeans.api.project.Project
import javaproject.JavaProject
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.openide.filesystems.FileObject

/**
 *
 * @author Alexander.Baratynski
 */
class DiagnosticsTest : KotlinTestCase("Diagnostics test", "diagnostics") {
    
    private fun getAnalysisResult(fileName: String): AnalysisResultWithProvider {
        val fileToAnalyze = dir.getFileObject(fileName)
        assertNotNull(fileToAnalyze)
        
        val ktFile = ProjectUtils.getKtFile(fileToAnalyze)
        return KotlinAnalyzer.analyzeFile(project, ktFile)
    }

    private fun doTest(fileName: String, 
                       diagnosticsRanges: List<Pair<Int, Int>>,
                       diagnosticsSeverity: List<Severity>, 
                       syntaxErrorsRanges: List<Pair<Int, Int>>) {
        val numberOfDiagnostics = diagnosticsRanges.size
        val numberOfSyntaxErrors = syntaxErrorsRanges.size
        
        val result = getAnalysisResult(fileName)
        val fileToAnalyze = dir.getFileObject(fileName)
        val ktFile = ProjectUtils.getKtFile(fileToAnalyze)
        
        val diagnostics = result.analysisResult.bindingContext.diagnostics.all()
        val syntaxErrors = AnalyzingUtils.getSyntaxErrorRanges(ktFile)
        
        assertEquals(numberOfDiagnostics, diagnostics.size)
        assertEquals(numberOfSyntaxErrors, syntaxErrors.size)
        
        diagnostics.forEachIndexed { i, it ->
            assertEquals(diagnosticsSeverity[i], it.severity)
                
            val startPosition = it.textRanges[0].startOffset
            val endPosition = it.textRanges[0].endOffset
                
            assertEquals(diagnosticsRanges[i].first, startPosition)
            assertEquals(diagnosticsRanges[i].second, endPosition)
        }
        
        syntaxErrors.forEachIndexed { i, it ->
            val startPosition = it.textRange.startOffset
            val endPosition = it.textRange.endOffset
                
            assertEquals(syntaxErrorsRanges[i].first, startPosition)
            assertEquals(syntaxErrorsRanges[i].second, endPosition)
        }
        
    }

    private fun doTest(fileName: String, 
                       diagnosticsRanges: List<Pair<Int, Int>>,
                       diagnosticsSeverity: List<Severity>) = doTest(fileName, diagnosticsRanges, diagnosticsSeverity, emptyList())
    
    private fun doTest(fileName: String,
                       syntaxErrorsRanges: List<Pair<Int, Int>> = emptyList()) = doTest(fileName, emptyList(), emptyList(), syntaxErrorsRanges)

    fun testKtHome() = assertNotNull(ProjectUtils.KT_HOME)

    fun testParameterIsNeverUsedWarning() = doTest("parameterIsNeverUsed.kt", listOf(Pair(30, 33)), listOf(Severity.WARNING))

    fun testExpectingATopLevelDeclarationError() = doTest("expectingATopLevelDeclaration.kt", listOf(Pair(21, 31)))

    fun testNoTypeMismatch() = doTest("checkNoTypeMismatch.kt")

    fun testTypeMismatch() = doTest("checkTypeMismatch.kt", listOf(Pair(151, 172)), listOf(Severity.ERROR))

    fun testNoValuePassed() = doTest("checkNoValuePassed.kt", listOf(Pair(80, 81)), listOf(Severity.ERROR))
    
    fun testWrongImport() = doTest("checkWrongImport.kt", listOf(Pair(28, 32)), listOf(Severity.ERROR))

    fun testCheckNullPointer() = doTest("checkNullPointer.kt", listOf(Pair(86, 90)), listOf(Severity.ERROR))

    fun testReassignValue() = doTest("checkReassignValue.kt", listOf(Pair(61, 66)), listOf(Severity.ERROR))

    fun testCastIsNeverSucceed() = doTest("checkCastIsNeverSucceed.kt", listOf(Pair(95, 97), Pair(79, 82)), listOf(Severity.WARNING, Severity.WARNING))

}