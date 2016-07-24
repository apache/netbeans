package org.black.kotlin.resolve.lang.java;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class NetBeansJavaProjectElementUtils {
    
    private static boolean isDeprecated = false;
    private static String binaryName = null;
    private static final Map<Project,JavaSource> JAVA_SOURCE = new HashMap<Project,JavaSource>();
    private static final Map<Project,ClasspathInfo> CLASSPATH_INFO = new HashMap<Project,ClasspathInfo>();
    
    private static ClasspathInfo getClasspathInfo(Project kotlinProject){
        
        assert kotlinProject != null : "Project cannot be null";
        
        ClassPathExtender extendedProvider = KotlinProjectHelper.INSTANCE.getExtendedClassPath(kotlinProject);
        
        ClassPath boot = extendedProvider.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath src = extendedProvider.getProjectSourcesClassPath(ClassPath.SOURCE);
        ClassPath compile = extendedProvider.getProjectSourcesClassPath(ClassPath.COMPILE);
        
        ClassPath bootProxy = ClassPathSupport.createProxyClassPath(boot, compile);
        
        return ClasspathInfo.create(bootProxy, src, compile);
    }
    
    public static void updateClasspathInfo(Project kotlinProject){
        CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        JAVA_SOURCE.put(kotlinProject, JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
    }
    
    public static TypeElement findTypeElement(Project kotlinProject, String fqName){
        if (!CLASSPATH_INFO.containsKey(kotlinProject)){
            CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        }
        if (!JAVA_SOURCE.containsKey(kotlinProject)){
            JAVA_SOURCE.put(kotlinProject,JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
        }
        TypeElementSearcher searcher = new TypeElementSearcher(fqName);
        try {
            JAVA_SOURCE.get(kotlinProject).runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    public static PackageElement findPackageElement(Project kotlinProject, String fqName){
        if (!CLASSPATH_INFO.containsKey(kotlinProject)){
            CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        }
        if (!JAVA_SOURCE.containsKey(kotlinProject)){
            JAVA_SOURCE.put(kotlinProject,JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
        }
        PackageElementSearcher searcher = new PackageElementSearcher(fqName);
        try {
            JAVA_SOURCE.get(kotlinProject).runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return searcher.getElement();
    }
    
    public static Project getProject(Element element){
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        
        if (projects.length == 1){
            return projects[0];
        }
        
        for (Project project : projects){
            if (!KotlinProjectHelper.INSTANCE.checkProject(project)){
                continue;
            }
            
            FileObject file = SourceUtils.getFile(ElementHandle.create(element), CLASSPATH_INFO.get(project));

            if (file != null){
                return project;
            }
            
        }
        return null;
    }
    
    public static boolean isDeprecated(final Element element){
        Project kotlinProject = NetBeansJavaProjectElementUtils.getProject(element);
        
        if (kotlinProject == null){
            return false;
        }
        
        if (!CLASSPATH_INFO.containsKey(kotlinProject)){
            CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        }
        if (!JAVA_SOURCE.containsKey(kotlinProject)){
            JAVA_SOURCE.put(kotlinProject,JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
        }
        try {
            JAVA_SOURCE.get(kotlinProject).runUserActionTask(new CancellableTask<CompilationController>(){
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
        if (!CLASSPATH_INFO.containsKey(kotlinProject)){
            CLASSPATH_INFO.put(kotlinProject, getClasspathInfo(kotlinProject));
        }
        if (!JAVA_SOURCE.containsKey(kotlinProject)){
            JAVA_SOURCE.put(kotlinProject,JavaSource.create(CLASSPATH_INFO.get(kotlinProject)));
        }
        try {
            JAVA_SOURCE.get(kotlinProject).runUserActionTask(new CancellableTask<CompilationController>(){
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
    
    public static FileObject getFileObjectForElement(Element element, Project kotlinProject){
        if (element == null){
            return null;
        }
        ElementHandle<? extends Element> handle = ElementHandle.create(element);
        return SourceUtils.getFile(handle, CLASSPATH_INFO.get(kotlinProject));
    }
    
    public static void openElementInEditor(Element element, Project kotlinProject){
        ElementHandle<? extends Element> handle = ElementHandle.create(element);
        ElementOpen.open(CLASSPATH_INFO.get(kotlinProject), handle);
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
