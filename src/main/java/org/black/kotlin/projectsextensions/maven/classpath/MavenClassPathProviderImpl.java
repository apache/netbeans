package org.black.kotlin.projectsextensions.maven.classpath;

import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

@ProjectServiceProvider(service={ClassPathProvider.class}, projectType="org-netbeans-modules-maven")
public final class MavenClassPathProviderImpl implements ClassPathProvider{

    private final Project project;
    
    public MavenClassPathProviderImpl(Project project){
        this.project = project;
    }
    
    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        ClassPathProviderImpl impl = new ClassPathProviderImpl(project);
        
        ClassPath cp = impl.findClassPath(fo, type);
        if (cp == null) {
            return null;
        }
        
        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
        resources.add(ClassPathSupport.createResource(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).toURL()));
        
        ClassPath lightClasses = ClassPathSupport.createClassPath(resources);
        
        return ClassPathSupport.createProxyClassPath(cp, lightClasses);
    }
    
   
}
