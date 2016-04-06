package org.black.kotlin.resolve.lang.java;

import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.black.kotlin.project.KotlinProject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class NetBeansJavaProjectElementUtils {
    
    private static boolean isDeprecated = false;
    private static String binaryName = null;
    
    private static ClasspathInfo getClasspathInfo(Project kotlinProject){
        
        assert kotlinProject != null : "Project cannot be null";
        
        ClassPath boot = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
        ClassPath src = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
        return ClasspathInfo.create(boot, src, compile);
    }
    
    public static TypeElement findTypeElement(Project kotlinProject, String fqName){
        ClasspathInfo ci = getClasspathInfo(kotlinProject);
        
        JavaSource js = JavaSource.create(ci);
        TypeElementSearcher searcher = new TypeElementSearcher(fqName);
        try {
            js.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    public static PackageElement findPackageElement(Project kotlinProject, String fqName){
        ClasspathInfo ci = getClasspathInfo(kotlinProject);
        
        JavaSource js = JavaSource.create(ci);
        PackageElementSearcher searcher = new PackageElementSearcher(fqName);
        try {
            js.runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    public static KotlinProject getProject(Element element){
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        
        if (projects.length == 1){
            return (KotlinProject) projects[0];
        }
        
        for (Project project : projects){
            if (!(project instanceof KotlinProject)){
                continue;
            }
            ClasspathInfo ci = getClasspathInfo(project);
            
            FileObject file = SourceUtils.getFile(ElementHandle.create(element), ci);

            if (file != null){
                return (KotlinProject) project;
            }
        }
        return null;
    }
    
    public static boolean isDeprecated(final Element element){
        Project kotlinProject = NetBeansJavaProjectElementUtils.getProject(element);
        
        if (kotlinProject == null){
            return false;
        }
        
        ClassPath boot = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.BOOT);
        ClassPath src = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.SOURCE);
        ClassPath compile = kotlinProject.getLookup().lookup(ClassPathProvider.class).findClassPath(null, ClassPath.COMPILE);
        
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
    
    public static String toBinaryName(Project kotlinProject, final String name){
        ClasspathInfo ci = getClasspathInfo(kotlinProject);
        
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
    
    private static class TypeElementSearcher implements CancellableTask<CompilationController>{

        private TypeElement element;
        private final String fqName;
        
        public TypeElementSearcher(String fqName){
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
    
    private static class PackageElementSearcher implements CancellableTask<CompilationController>{

        private PackageElement element;
        private final String fqName;
        
        public PackageElementSearcher(String fqName){
            this.fqName = fqName;
        }
        
        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController info) throws Exception {
            element = info.getElements().getPackageElement(fqName);
        }
        
        public PackageElement getElement(){
            return element;
        }
        
    }
    
}
