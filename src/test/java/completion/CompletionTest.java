package completion;

import javaproject.JavaProject;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class CompletionTest extends NbTestCase {
    
    private final Project project;
    private final FileObject completionDir;
    
    public CompletionTest() {
        super("Completion test");
        project = JavaProject.INSTANCE.getJavaProject();
        completionDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("completion");
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(completionDir);
    }
    
}
