package navigation;

import java.io.IOException;
import javaproject.JavaProject;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import kotlin.Pair;
import org.jetbrains.kotlin.navigation.netbeans.KotlinHyperlinkProvider;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import utils.TestUtils;

/**
 *
 * @author Alexander.Baratynski
 */
public class NavigationTest extends NbTestCase {
    
    private final Project project;
    private final FileObject navigationDir;
    private final KotlinHyperlinkProvider hyperlinkProvider;
    
    public NavigationTest() {
        super("Navigation test");
        project = JavaProject.INSTANCE.getJavaProject();
        navigationDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("navigation");
        hyperlinkProvider = new KotlinHyperlinkProvider();
    }
    
        private Document getDocumentForFileObject(String fileName) {
        FileObject file = navigationDir.getFileObject(fileName);
        
        assertNotNull(file);
        
        Document doc = null;
        try {
            doc = ProjectUtils.getDocumentFromFileObject(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(doc);
        
        return doc;
    }
    
    private void doTest(String fromName, String toName) {
        Document from = getDocumentForFileObject(fromName);
        assertNotNull(from);
        
        Integer caret = TestUtils.getCaret(from);
        assertNotNull(caret);
        
        int offset = caret + "<caret>".length() + 1;
        hyperlinkProvider.isHyperlinkPoint(from, offset);
        hyperlinkProvider.performClickAction(from, offset);
        
        Pair<Document, Integer> navigationData = hyperlinkProvider.getNavigationCache();
        assertNotNull(navigationData);
        
        Document to = navigationData.getFirst();
        FileObject toFO = ProjectUtils.getFileObjectForDocument(to);
        assertNotNull(toFO);
        
        FileObject expectedFO = navigationDir.getFileObject(toName);
        assertEquals(expectedFO.getPath(), toFO.getPath());
        
        Integer expectedOffset = TestUtils.getCaret(getDocumentForFileObject(toName.replace(".kt", ".caret")));
        assertEquals(expectedOffset, navigationData.getSecond());
    }    
        
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(navigationDir);
    }
    
    @Test
    public void testNavigationToFunction() {
        doTest("checkNavigationToFunction.kt","functionToNavigate.kt");
    }
    
}
