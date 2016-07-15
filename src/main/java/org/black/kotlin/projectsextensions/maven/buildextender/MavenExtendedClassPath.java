package org.black.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenExtendedClassPath implements ClassPathExtender {

    private final NbMavenProjectImpl project;
    
    public MavenExtendedClassPath(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    private ClassPath getClasspath(List<String> paths) throws DependencyResolutionRequiredException {
        List<FileObject> classpaths = new ArrayList<FileObject>();
        
        for (String path : paths) {
            File file = new File(path);
            classpaths.add(FileUtil.toFileObject(file));
        }
        
        return ClassPathSupport.createClassPath(classpaths.toArray(new FileObject[classpaths.size()]));
    }
    
    
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {
        if (type.equals(ClassPath.COMPILE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getCompileClasspathElements());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (type.equals(ClassPath.EXECUTE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getRuntimeClasspathElements());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (type.equals(ClassPath.SOURCE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getCompileSourceRoots());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (type.equals(ClassPath.BOOT)) {
            try {
                String bootClassPath = System.getProperty("sun.boot.class.path");
                List<String> javaClasspathElements = new ArrayList<String>(Arrays.asList(bootClassPath.split(
                        Pattern.quote(System.getProperty("path.separator")))));
                javaClasspathElements.addAll(project.getOriginalMavenProject().getSystemClasspathElements());
                return getClasspath(javaClasspathElements);
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return null;
    }
    
}
