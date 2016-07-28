package diagnostics;

import com.intellij.psi.PsiErrorElement;
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

/**
 *
 * @author Alexander.Baratynski
 */
public class DiagnosticsTest extends NbTestCase {
    
    private final Project project;
    private final FileObject diagnosticsDir;
    
    public DiagnosticsTest() {
        super("First test");
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
        AnalysisResultWithProvider result = getAnalysisResult("parameterIsNeverUsed.kt");
        
        Diagnostic diagnostic = 
                result.getAnalysisResult().getBindingContext().getDiagnostics().iterator().next();
        
        assertEquals(Severity.WARNING, diagnostic.getSeverity());
        
        int startPosition = diagnostic.getTextRanges().get(0).getStartOffset();
        int endPosition = diagnostic.getTextRanges().get(0).getEndOffset();
        
        assertEquals(startPosition, 30);
        assertEquals(endPosition, 33);
    }
    
    @Test
    public void testExpectingATopLevelDeclarationError(){
        AnalysisResultWithProvider result = getAnalysisResult("expectingATopLevelDeclaration.kt");
        FileObject fileToAnalyze = diagnosticsDir.getFileObject("expectingATopLevelDeclaration.kt");
        KtFile ktFile = ProjectUtils.getKtFile(fileToAnalyze);
        PsiErrorElement psiError = AnalyzingUtils.getSyntaxErrorRanges(ktFile).get(0);
        
        assertNotNull(psiError);
        
        int startPosition = psiError.getTextRange().getStartOffset();
        int endPosition = psiError.getTextRange().getEndOffset();
        
        assertEquals(21,startPosition);
        assertEquals(31,endPosition);
        assertTrue(result.getAnalysisResult().getBindingContext().getDiagnostics().isEmpty());
    }
    
    @Test
    public void testNoTypeMismatch(){
        AnalysisResultWithProvider result = getAnalysisResult("checkNoTypeMismatch.kt");
        
        assertTrue(result.getAnalysisResult().getBindingContext().getDiagnostics().isEmpty());
    }
}
