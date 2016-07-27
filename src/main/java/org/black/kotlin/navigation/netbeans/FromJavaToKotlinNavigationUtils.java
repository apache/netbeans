package org.black.kotlin.navigation.netbeans;

import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.editor.java.GoToSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class FromJavaToKotlinNavigationUtils {
    
    public static Element getElement(Document doc, int offset) {
        FileObject file = ProjectUtils.getFileObjectForDocument(doc);
        if (file == null) {
            return null;
        }
        
        JavaSource javaSource = JavaSource.forDocument(doc);
        ElementSearcher searcher = new ElementSearcher(offset);
        try {
            javaSource.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Element element = searcher.getElement();
        
        return element;
    }
    
    
    private static class ElementSearcher implements CancellableTask<CompilationController>{

        private Element element;
        private final int offset;
        
        public ElementSearcher(int offset){
            this.offset = offset;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            TreePath treePath = info.getTreeUtilities().pathFor(offset);
            element = info.getTrees().getElement(treePath);
        }
        
        public Element getElement(){
            return element;
        }
        
    }
    
}

