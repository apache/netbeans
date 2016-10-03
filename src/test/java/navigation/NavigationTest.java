package navigation;

import javaproject.JavaProject;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import kotlin.Pair;
import org.jetbrains.kotlin.navigation.netbeans.KotlinHyperlinkProvider;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.openide.filesystems.FileObject;
import utils.TestUtilsKt;

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
    
    private void doTest(String fromName, String toName) {
        Document from = TestUtilsKt.getDocumentForFileObject(navigationDir, fromName);
        assertNotNull(from);
        
        Integer caret = TestUtilsKt.getCaret(from);
        assertNotNull(caret);
        
        int offset = caret + "<caret>".length() + 1;
        hyperlinkProvider.isHyperlinkPoint(from, offset, HyperlinkType.GO_TO_DECLARATION);
        hyperlinkProvider.performClickAction(from, offset, HyperlinkType.GO_TO_DECLARATION);
        
        Pair<Document, Integer> navigationData = hyperlinkProvider.getNavigationCache();
        assertNotNull(navigationData);
        
        Document to = navigationData.getFirst();
        FileObject toFO = ProjectUtils.getFileObjectForDocument(to);
        assertNotNull(toFO);
        
        FileObject expectedFO = navigationDir.getFileObject(toName);
        assertEquals(expectedFO.getPath(), toFO.getPath());
        
        Integer expectedOffset = TestUtilsKt.getCaret(
                TestUtilsKt.getDocumentForFileObject(navigationDir, toName.replace(".kt", ".caret")));
        assertEquals(expectedOffset, navigationData.getSecond());
    }    
        
    private void doTest(String name) {
        doTest(name, name);
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(navigationDir);
    }
    
//    @Test
    public void tstNavigationToFunction() {
        doTest("checkNavigationToFunction.kt","functionToNavigate.kt");
    }
    
//    @Test
    public void tstNavigationToClass() {
        doTest("checkNavigationToClass.kt", "KotlinClass.kt");
    }
    
//    @Test
    public void tstNavigationToVariable() {
        doTest("checkNavigationToVariable.kt");
    }
    
}
