package completion;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.completion.KotlinCompletionItem;
import org.jetbrains.kotlin.completion.KotlinCompletionUtils;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import static junit.framework.TestCase.assertNotNull;

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
    
    private Document getDocumentForFileObject(String fileName) {
        FileObject file = completionDir.getFileObject(fileName);
        
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
    
    private void doTest(String fileName, Collection<String> items) {
        Document doc = getDocumentForFileObject(fileName);
        Collection<KotlinCompletionItem> completionItems = null;
        List<CharSequence> completions = new ArrayList<CharSequence>();
        
        Integer caret = TestCompletionUtils.getCaret(doc);
        assertNotNull(caret);
        
        try {
            completionItems = KotlinCompletionUtils.INSTANCE.createItems(doc, caret);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(completionItems);

        for (KotlinCompletionItem it : completionItems) {
            completions.add(it.getSortText());
        }
        
        boolean contains = false;
        if (completions.containsAll(items)) {
            contains = true;
        }
        
        assertEquals(true, contains);
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(completionDir);
    }
    
    @Test
    public void testStringCompletion() {
        doTest("checkStringCompletion.kt", Lists.newArrayList("toString()"));
    }
    
    @Test
    public void testBasicInt() {
        doTest("checkBasicInt.kt", Lists.newArrayList("Int"));
    }
    
    @Test
    public void testBasicAny() {
        doTest("checkBasicAny.kt", Lists.newArrayList("Any"));
    }
    
    @Test
    public void testAutoCastAfterIf() {
        doTest("checkAutoCastAfterIf.kt", Lists.newArrayList("value"));
    }
    
    @Test
    public void testAutoCastAfterIfMethod() {
        doTest("checkAutoCastAfterIfMethod.kt", Lists.newArrayList("test()"));
    }
    
    @Test
    public void testAutoCastForThis() {
        doTest("checkAutoCastForThis.kt", Lists.newArrayList("destroy()"));
    }
    
    @Test
    public void testAutoCastInWhen() {
        doTest("checkAutoCastInWhen.kt", Lists.newArrayList("left", "right"));
    }
    
    @Test
    public void testCompletionBeforeDotInCall() {
        doTest("checkCompletionBeforeDotInCall.kt", 
                Lists.newArrayList("TestSample", "testVar", "testTop()", "testFun()"));
    }
    
    @Test
    public void testLocalLambda() {
        doTest("checkLocalLambda.kt", Lists.newArrayList("test()"));
    }
    
    @Test
    public void checkCompanion() {
        doTest("checkCompanion.kt", Lists.newArrayList("companionVal", "companionFun()"));
    }
    
}
