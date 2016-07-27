package org.black.kotlin.projectsextensions.maven.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.black.kotlin.utils.KotlinClasspath;
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
    private ClassPath boot = null;
    private ClassPath compile = null;
    private ClassPath execute = null;
    private ClassPath source = null;
    
    public MavenExtendedClassPath(NbMavenProjectImpl project) {
        this.project = project;
        createClasspath();
    }
    
    private ClassPath getClasspath(List<String> paths) throws DependencyResolutionRequiredException, MalformedURLException {
        List<URL> classpaths = new ArrayList<URL>();
        List<String> classpath = new ArrayList<String>();
        classpath.addAll(paths);
        classpath.add(KotlinClasspath.getKotlinBootClasspath());
        
        for (String path : classpath) {
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
    
    private void createClasspath() {
        try {
            compile = getClasspath(project.getOriginalMavenProject().getCompileClasspathElements());
            execute = getClasspath(project.getOriginalMavenProject().getRuntimeClasspathElements());
            source = getClasspath(project.getOriginalMavenProject().getCompileSourceRoots());
            String bootClassPath = System.getProperty("sun.boot.class.path");
            List<String> javaClasspathElements = new ArrayList<String>(Arrays.asList(bootClassPath.split(
                        Pattern.quote(System.getProperty("path.separator")))));
            javaClasspathElements.addAll(project.getOriginalMavenProject().getSystemClasspathElements());
            javaClasspathElements.addAll(project.getOriginalMavenProject().getTestClasspathElements());
            boot = getClasspath(javaClasspathElements);
            
        } catch (DependencyResolutionRequiredException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {

        if (type.equals(ClassPath.COMPILE)) {
            return compile;
        } else if (type.equals(ClassPath.EXECUTE)) {
            return execute;
        } else if (type.equals(ClassPath.SOURCE)) {
            return source;
        } else if (type.equals(ClassPath.BOOT)) {
            return boot;
        }
        
        return null;
    }
    
}
