package utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
    
}
