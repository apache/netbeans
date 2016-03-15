package org.black.kotlin.resolve.lang.java;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class NetBeansJavaProjectElementUtils {
    
    private static boolean isDeprecated = false;
    private static String binaryName = null;
    
    public static TypeElement findElement(Project javaProject, String fqName){
        ClassPath boot = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
        ClassPath src = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
        ClasspathInfo ci = ClasspathInfo.create(boot, src, compile);
        JavaSource js = JavaSource.create(ci);
        ElementSearcher searcher = new ElementSearcher(fqName);
        try {
            js.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    public static boolean isDeprecated(Project javaProject, final Element element){
        ClassPath boot = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
        ClassPath src = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
        ClasspathInfo ci = ClasspathInfo.create(boot, src, compile);
        JavaSource js = JavaSource.create(ci);
        
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>(){
                @Override
                public void cancel() {
                }
                
                @Override
                public void run(CompilationController info) throws Exception {
                    NetBeansJavaProjectElementUtils.isDeprecated = info.getElements().isDeprecated(element);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return isDeprecated;
    }
    
    public static String toBinaryName(Project javaProject, final String name){
        ClassPath boot = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
        ClassPath src = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = javaProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
        ClasspathInfo ci = ClasspathInfo.create(boot, src, compile);
        JavaSource js = JavaSource.create(ci);
        
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>(){
                @Override
                public void cancel() {
                }
                
                @Override
                public void run(CompilationController info) throws Exception {
                    TypeElement elem = info.getElements().getTypeElement(name);
                    NetBeansJavaProjectElementUtils.binaryName = info.getElements().getBinaryName(elem).toString();
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return binaryName;
    }
    
    private static class ElementSearcher implements CancellableTask<CompilationController>{

        private TypeElement element;
        private final String fqName;
        
        public ElementSearcher(String fqName){
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
