package diagnostics;

import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import javaproject.JavaProject;
import static junit.framework.TestCase.assertEquals;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.psi.KtFile;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class NewDiagnosticsTest extends NbTestCase {
    
    private final Project project;
    private final FileObject diagnosticsDir;
    
    public NewDiagnosticsTest() {
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
        if (project == null){
            return;
        }
        
        AnalysisResultWithProvider result = getAnalysisResult("parameterIsNeverUsed.kt");
        
        Diagnostic diagnostic = 
                result.getAnalysisResult().getBindingContext().getDiagnostics().iterator().next();
        
        assertEquals(Severity.WARNING, diagnostic.getSeverity());
        
        int startPosition = diagnostic.getTextRanges().get(0).getStartOffset();
        int endPosition = diagnostic.getTextRanges().get(0).getEndOffset();
        
        assertEquals(startPosition, 30);
        assertEquals(endPosition, 33);
    }
    
}
