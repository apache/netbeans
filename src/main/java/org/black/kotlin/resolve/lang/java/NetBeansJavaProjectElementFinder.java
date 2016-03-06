package org.black.kotlin.resolve.lang.java;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class NetBeansJavaProjectElementFinder {
    
    public static TypeElement findElement(Project javaProject, String fqName){
        FileObject root = javaProject.getProjectDirectory().getFileObject("src");
        
        ClasspathInfo ci = ClasspathInfo.create(root);
        JavaSource js = JavaSource.create(ci);
        Searcher searcher = new Searcher(fqName);
        try {
            js.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    
    private static class Searcher implements CancellableTask<CompilationController>{

        private TypeElement element;
        private final String fqName;
        
        public Searcher(String fqName){
            this.fqName = fqName;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getTypeElement(fqName);
        }
        
        public TypeElement getElement(){
            return element;
        }
        
    }
    
}
