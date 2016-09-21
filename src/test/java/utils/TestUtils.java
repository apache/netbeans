package utils;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
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
    
    public static List<Integer> getCarets(Document doc, int startOffset) {
        List<Integer> carets = Lists.newArrayList();
        
        try {
            int caretOffset = doc.getText(startOffset, doc.getLength()).indexOf("<caret>");
            if (caretOffset >=0) {
                carets.add(caretOffset);
                carets.addAll(getCarets(doc, caretOffset + "<caret>".length()));
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return carets;
    }
    
    public static Document getDocumentForFileObject(FileObject dir, String fileName) {
        FileObject file = dir.getFileObject(fileName);
        
        assertNotNull(file);
        
        return getDocumentForFileObject(file);
    }
    
    public static Document getDocumentForFileObject(FileObject fo) {
        Document doc = null;
        try {
            doc = ProjectUtils.getDocumentFromFileObject(fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assertNotNull(doc);
        
        return doc;
    }

    public static List<FileObject> getAllKtFilesInFolder(FileObject folder) {
        List<FileObject> ktFiles = Lists.newArrayList();
        for (FileObject child : folder.getChildren()) {
            if (child.hasExt("kt")) {
                ktFiles.add(child);
            }
        }
        
        return ktFiles;
    }
    
}
