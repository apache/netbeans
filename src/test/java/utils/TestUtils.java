package utils;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class TestUtils {
    
    public static Integer getCaret(Document doc) {
        try {
            return doc.getText(0, doc.getLength()).indexOf("<caret>");
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    public static Document getDocumentForFileObject(FileObject dir, String fileName) {
        FileObject file = dir.getFileObject(fileName);
        
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
    
}
