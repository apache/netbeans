package diagnostics;

import mockproject.KotlinProjectCreator;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.psi.KtFile;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.implspi.EnvironmentFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Александр
 */
public class DiagnosticsTest extends NbTestCase {
    
    private KotlinProject kotlinProject = null;
    private FileObject diagnosticsDir;
    
    public DiagnosticsTest() throws Exception {
        super("Diagnostics test");
//        kotlinProject = KotlinProjectCreator.INSTANCE.getProject();
        if (kotlinProject != null)
        diagnosticsDir = kotlinProject.getProjectDirectory().
                getFileObject("src").getFileObject("diagnostics");
    }
    
    private AnalysisResultWithProvider getAnalysisResult(String fileName){
        FileObject fileToAnalyze = diagnosticsDir.getFileObject(fileName);
        KtFile ktFile = ProjectUtils.getKtFile(fileToAnalyze);
        return KotlinAnalyzer.analyzeFile(kotlinProject, ktFile);
    }
    
    @Test
    public void testParameterIsNeverUsedWarning(){
        if (kotlinProject == null){
            return;
        }
        
        AnalysisResultWithProvider result = getAnalysisResult("parameterIsNeverUsed.kt");
        
        Diagnostic diagnostic = 
                result.getAnalysisResult().getBindingContext().getDiagnostics().iterator().next();
        
        assertEquals(diagnostic.getSeverity(), Severity.WARNING);
        
        int startPosition = diagnostic.getTextRanges().get(0).getStartOffset();
        int endPosition = diagnostic.getTextRanges().get(0).getEndOffset();
        
        assertEquals(startPosition, 30);
        assertEquals(endPosition, 33);
    }
    
}
