package org.black.kotlin.projectsextensions.maven.buildextender;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingException;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.projectsextensions.maven.MavenHelper;
import org.black.kotlin.utils.KotlinClasspath;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectState;
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
    
    private NbMavenProjectImpl getParent(NbMavenProjectImpl proj, NbMavenProjectFactory projectFactory) throws IOException {
        if (projectFactory.isProject(proj.getProjectDirectory().getParent())){
            NbMavenProjectImpl projectParent = MavenHelper.getMavenProject(proj.getProjectDirectory().getParent());
            
            NbMavenProjectImpl parentsParent = getParent(projectParent, projectFactory);
            if (parentsParent != null) {
                projectParent = parentsParent;
            }
            
            return projectParent;
        }
        
        return null;
    }
    
    private NbMavenProjectImpl getMainParent(NbMavenProjectImpl proj) throws IOException {
        NbMavenProjectFactory projectFactory = new NbMavenProjectFactory();
        return getParent(proj, projectFactory);
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
                javaClasspathElements.addAll(project.getOriginalMavenProject().getTestClasspathElements());
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
