package diagnostics;

import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import javaproject.JavaProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.KotlinAnalyzer;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class NewDiagnosticsTest extends NbTestCase {
    
    private final Project project;
    private FileObject diagnosticsDir;
    
    public NewDiagnosticsTest() {
        super("First test");
        project = JavaProject.INSTANCE.getJavaProject();
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
        diagnosticsDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("diagnostics");
        assertNotNull(diagnosticsDir);
    }
    
    @Test 
    public void testKtHome() {
        assertNotNull(ProjectUtils.KT_HOME);
    }
    
    
}
