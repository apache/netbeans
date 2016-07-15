package org.black.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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
    
    private ClassPath getClasspath(List<String> paths) throws DependencyResolutionRequiredException, MalformedURLException {
        List<URL> classpaths = new ArrayList<URL>();
        
        for (String path : paths) {
            File file = new File(path);
            if (!file.canRead()) {
                continue;
            }

            FileObject fileObject = FileUtil.toFileObject(file);
            if (FileUtil.isArchiveFile(fileObject)) {
                fileObject = FileUtil.getArchiveRoot(fileObject);
            }
            if (fileObject != null) {
                classpaths.add(fileObject.toURL());
            }
        }
        
        return ClassPathSupport.createClassPath(classpaths.toArray(new URL[classpaths.size()]));
    }
    
    
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {
        if (type.equals(ClassPath.COMPILE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getCompileClasspathElements());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (type.equals(ClassPath.EXECUTE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getRuntimeClasspathElements());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (type.equals(ClassPath.SOURCE)) {
            try {
                return getClasspath(project.getOriginalMavenProject().getCompileSourceRoots());
            } catch (DependencyResolutionRequiredException ex) {
                Exceptions.printStackTrace(ex);
            } catch (MalformedURLException ex) {
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
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return null;
    }
    
}
