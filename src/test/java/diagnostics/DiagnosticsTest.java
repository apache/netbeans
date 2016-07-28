package diagnostics;

import com.intellij.psi.PsiErrorElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import javaproject.JavaProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalyzingUtils;
import org.openide.filesystems.FileObject;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import kotlin.Pair;

/**
 *
 * @author Alexander.Baratynski
 */
public class DiagnosticsTest extends NbTestCase {
    
    private final Project project;
    private final FileObject diagnosticsDir;
    
    public DiagnosticsTest() {
        super("Diagnostics test");
        project = JavaProject.INSTANCE.getJavaProject();
        diagnosticsDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("diagnostics");  
    }
 
    
    private AnalysisResultWithProvider getAnalysisResult(String fileName){
        FileObject fileToAnalyze = diagnosticsDir.getFileObject(fileName);
        
        assertNotNull(fileToAnalyze);
        
        KtFile ktFile = ProjectUtils.getKtFile(fileToAnalyze);
        return KotlinAnalyzer.analyzeFile(project, ktFile);
    }
    
    private void doTest(String fileName, int numberOfDiagnostics, 
            List<Pair<Integer,Integer>> diagnosticsRanges, 
            List<Severity> diagnosticsSeverity, int numberOfSyntaxErrors,
            List<Pair<Integer, Integer>> syntaxErrorsRanges) {
        
        AnalysisResultWithProvider result = getAnalysisResult(fileName);
        FileObject fileToAnalyze = diagnosticsDir.getFileObject(fileName);
        KtFile ktFile = ProjectUtils.getKtFile(fileToAnalyze);
        
        Collection<Diagnostic> diagnostics = 
                result.getAnalysisResult().getBindingContext().getDiagnostics().all();
        Collection<PsiErrorElement> syntaxErrors = AnalyzingUtils.getSyntaxErrorRanges(ktFile);
        
        assertEquals(numberOfDiagnostics, diagnostics.size());
        assertEquals(numberOfSyntaxErrors, syntaxErrors.size());
        
        if (numberOfDiagnostics > 0) {
            int i = 0;
            for (Diagnostic diagnostic : diagnostics) {
                assertEquals(diagnosticsSeverity.get(i), diagnostic.getSeverity());
                
                Integer startPosition = diagnostic.getTextRanges().get(0).getStartOffset();
                Integer endPosition = diagnostic.getTextRanges().get(0).getEndOffset();
                
                assertEquals(diagnosticsRanges.get(i).getFirst(), startPosition);
                assertEquals(diagnosticsRanges.get(i).getSecond(), endPosition);
                
                i++;
            }
        }
        
        if (numberOfSyntaxErrors > 0) {
            int i = 0;
            for (PsiErrorElement syntaxError : syntaxErrors) {
                Integer startPosition = syntaxError.getTextRange().getStartOffset();
                Integer endPosition = syntaxError.getTextRange().getEndOffset();
        
                assertEquals(syntaxErrorsRanges.get(i).getFirst(), startPosition);
                assertEquals(syntaxErrorsRanges.get(i).getSecond(), endPosition);
                
                i++;
            }
        }
    }
    
    private void doTest(String fileName, int numberOfDiagnostics, 
            List<Pair<Integer,Integer>> diagnosticsRanges, 
            List<Severity> diagnosticsSeverity) {
        doTest(fileName, numberOfDiagnostics, diagnosticsRanges, diagnosticsSeverity,
                0, new ArrayList<Pair<Integer, Integer>>());
    }
    
    private void doTest(String fileName, int numberOfSyntaxErrors,
            List<Pair<Integer, Integer>> syntaxErrorsRanges) {
        doTest(fileName, 0, new ArrayList<Pair<Integer, Integer>>(), 
                null, numberOfSyntaxErrors, syntaxErrorsRanges);
    }
    
    private void doTest(String fileName) {
        doTest(fileName, 0, new ArrayList<Pair<Integer, Integer>>());
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(diagnosticsDir);
    }
    
    @Test 
    public void testKtHome() {
        assertNotNull(ProjectUtils.KT_HOME);
    }
    
    @Test
    public void testParameterIsNeverUsedWarning(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(30,33));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.WARNING);
        
        doTest("parameterIsNeverUsed.kt", 1, diagnosticsRanges, severityList);
    }
    
    @Test
    public void testExpectingATopLevelDeclarationError(){
        List<Pair<Integer, Integer>> errorsRanges = 
                new ArrayList<Pair<Integer, Integer>>();
        errorsRanges.add(new Pair<Integer, Integer>(21,31));
        
        doTest("expectingATopLevelDeclaration.kt", 1, errorsRanges);
    }
    
    @Test
    public void testNoTypeMismatch(){
        doTest("checkNoTypeMismatch.kt");
    }
    
    @Test
    public void testTypeMismatch(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(151,172));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.ERROR);
        
        doTest("checkTypeMismatch.kt", 1, diagnosticsRanges, severityList);
    }
    
    @Test
    public void testNoValuePassed(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(80,81));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.ERROR);
        
        doTest("checkNoValuePassed.kt", 1, diagnosticsRanges, severityList);
    }
    
    @Test
    public void testWrongImport(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(28,32));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.ERROR);
        
        doTest("checkWrongImport.kt", 1, diagnosticsRanges, severityList);        
    }
    
    @Test
    public void testCheckNullPointer(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(86,90));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.ERROR);
        
        doTest("checkNullPointer.kt", 1, diagnosticsRanges, severityList);          
    }
    
    @Test
    public void testReassignValue(){
        List<Pair<Integer,Integer>> diagnosticsRanges = 
                new ArrayList<Pair<Integer,Integer>>();
        diagnosticsRanges.add(new Pair<Integer, Integer>(61,66));
        
        List<Severity> severityList = new ArrayList<Severity>();
        severityList.add(Severity.ERROR);
        
        doTest("checkReassignValue.kt", 1, diagnosticsRanges, severityList);         
    }
    
}
