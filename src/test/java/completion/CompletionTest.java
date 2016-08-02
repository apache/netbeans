package completion;

import java.io.IOException;
import java.util.Collection;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.jetbrains.kotlin.completion.KotlinCompletionItem;
import org.jetbrains.kotlin.completion.KotlinCompletionUtils;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

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
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(completionDir);
    }
    
    @Test
    public void testStringCompletion() {
        Document doc = getDocumentForFileObject("checkStringCompletion.kt");
        Collection<KotlinCompletionItem> items = null;
        
        Integer caret = TestCompletionUtils.getCaret(doc);
        assertNotNull(caret);
        
        try {
            items = KotlinCompletionUtils.INSTANCE.createItems(doc, caret);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(items);

        boolean hasToString = false;
        for (KotlinCompletionItem item : items) {
            if (item.getSortText().equals("toString()")) {
                hasToString = true;
            }
        }
        
        assertEquals(true, hasToString);
    }
    
    @Test
    public void testBasicInt() throws BadLocationException {
        Document doc = getDocumentForFileObject("checkBasicInt.kt");
        Collection<KotlinCompletionItem> items = null;
        
        Integer caret = TestCompletionUtils.getCaret(doc);
        assertNotNull(caret);
        
        try {
            items = KotlinCompletionUtils.INSTANCE.createItems(doc, caret);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(items);
        
        boolean hasInt = false;
        for (KotlinCompletionItem item : items) {
            if (item.getSortText().equals("Int")) {
                hasInt = true;
            }
        }
        
        assertEquals(true, hasInt);
    }
    
}
