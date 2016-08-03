package highlighting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.highlighter.KotlinTokenScanner;
import org.jetbrains.kotlin.highlighter.TokenType;
import org.jetbrains.kotlin.highlighter.netbeans.KotlinToken;
import org.jetbrains.kotlin.highlighter.netbeans.KotlinTokenId;
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
public class HighlightingTest extends NbTestCase {
    
    private final Project project;
    private final FileObject highlightingDir;
    
    public HighlightingTest() {
        super("Highlighting test");
        project = JavaProject.INSTANCE.getJavaProject();
        highlightingDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("highlighting");
    }
    
    private void doTest(String fileName, TokenType... types) {
        Document doc = TestUtils.getDocumentForFileObject(highlightingDir, fileName);
        List<KotlinToken<KotlinTokenId>> tokens = null;
        try {
            tokens = new KotlinTokenScanner(doc.getText(0, doc.getLength())).getTokens();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(tokens);
        
        List<TokenType> parsedTypes = new ArrayList<TokenType>();
        for (KotlinToken kotlinToken : tokens) {
            TokenType type = kotlinToken.getType();
            if (type == TokenType.EOF || 
                    type == TokenType.WHITESPACE || 
                    type == TokenType.UNDEFINED) {
                continue;
            }
            parsedTypes.add(type);
        }
        
        List<TokenType> expectedTypes = Lists.newArrayList(types);
        
        assertEquals(parsedTypes, expectedTypes);
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(highlightingDir);
    }
    
    @Test
    public void testBlockComment() {
        doTest("blockComment.kt", TokenType.MULTI_LINE_COMMENT);
    }
    
    @Test
    public void testForKeyword() {
        doTest("forKeyword.kt", TokenType.KEYWORD);
    }
    
    @Test
    public void testFunction() {
        doTest("function.kt", TokenType.KEYWORD, TokenType.IDENTIFIER, 
                TokenType.KEYWORD, TokenType.IDENTIFIER);
    }
}
