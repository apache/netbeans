package org.black.kotlin.projectsextensions.maven.buildextender;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.black.kotlin.projectsextensions.ClassPathExtender;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.projectsextensions.maven.MavenHelper;
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
public class MavenModuledProjectExtendedClassPath implements ClassPathExtender{

    private final NbMavenProjectImpl project;
    private final List<NbMavenProjectImpl> children = new ArrayList<NbMavenProjectImpl>();
    private final Set<ClassPath> sourceClasspath = Sets.newHashSet();
    private final Set<ClassPath> compileClasspath = Sets.newHashSet();
    private final Set<ClassPath> executeClasspath = Sets.newHashSet();
    private final Set<ClassPath> bootClasspath = Sets.newHashSet();
    
    private ClassPath compile = null;
    private ClassPath execute = null;
    private ClassPath source = null;
    private ClassPath boot = null;
    
    public MavenModuledProjectExtendedClassPath(NbMavenProjectImpl project) {
        this.project = project;
        try {
            addChildrenOfMavenProject();
            addClassPathFromChildren();
            createClasspath();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DependencyResolutionRequiredException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void addChildrenOfMavenProject() throws IOException {
        List modules = project.getOriginalMavenProject().getModules();
        
        for (Object module : modules) {
            NbMavenProjectImpl child = MavenHelper.getMavenProject(project.getProjectDirectory().getFileObject((String) module));
            if (child != null){
                children.add(child);
            }
        }
    }
    
    private void addClassPathFromChildren(){
        for (NbMavenProjectImpl child : children) {
            if (MavenHelper.isModuled(child)) {
                MavenModuledProjectExtendedClassPath extendedClasspath = new MavenModuledProjectExtendedClassPath(child);
                sourceClasspath.add(extendedClasspath.getProjectSourcesClassPath(ClassPath.SOURCE));
                bootClasspath.add(extendedClasspath.getProjectSourcesClassPath(ClassPath.BOOT));
                executeClasspath.add(extendedClasspath.getProjectSourcesClassPath(ClassPath.EXECUTE));
                compileClasspath.add(extendedClasspath.getProjectSourcesClassPath(ClassPath.COMPILE));
            } else {
                try {
                    sourceClasspath.add(getClasspath(child.getOriginalMavenProject().getCompileSourceRoots()));
                    
                    bootClasspath.add(getClasspath(child.getOriginalMavenProject().getSystemClasspathElements()));
                    bootClasspath.add(getClasspath(child.getOriginalMavenProject().getTestClasspathElements()));
                    
                    compileClasspath.add(getClasspath(project.getOriginalMavenProject().getCompileClasspathElements()));
                    
                    executeClasspath.add(getClasspath(project.getOriginalMavenProject().getRuntimeClasspathElements()));
                } catch (DependencyResolutionRequiredException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private void createClasspath() throws DependencyResolutionRequiredException, MalformedURLException {
        compileClasspath.add(getClasspath(project.getOriginalMavenProject().getCompileClasspathElements()));
        compile = ClassPathSupport.createProxyClassPath(
                compileClasspath.toArray(new ClassPath[compileClasspath.size()]));
        executeClasspath.add(getClasspath(project.getOriginalMavenProject().getRuntimeClasspathElements()));
        execute =  ClassPathSupport.createProxyClassPath(
                executeClasspath.toArray(new ClassPath[executeClasspath.size()]));
        sourceClasspath.add(getClasspath(project.getOriginalMavenProject().getCompileSourceRoots()));
        source = ClassPathSupport.createProxyClassPath(
                sourceClasspath.toArray(new ClassPath[sourceClasspath.size()]));
        String bootClassPath = System.getProperty("sun.boot.class.path");
        List<String> javaClasspathElements = new ArrayList<String>(Arrays.asList(bootClassPath.split(
                Pattern.quote(System.getProperty("path.separator")))));
        javaClasspathElements.addAll(project.getOriginalMavenProject().getSystemClasspathElements());
        javaClasspathElements.addAll(project.getOriginalMavenProject().getTestClasspathElements());
        javaClasspathElements.add(KotlinClasspath.getKotlinBootClasspath());
        bootClasspath.add(getClasspath(javaClasspathElements));
        boot = ClassPathSupport.createProxyClassPath(
                bootClasspath.toArray(new ClassPath[bootClasspath.size()]));
    }
    
    private ClassPath getClasspath(List<String> paths) throws DependencyResolutionRequiredException, MalformedURLException {
        Set<URL> classpaths = new HashSet<URL>();
        Set<String> classpath = new HashSet<String>();
        classpath.addAll(paths);
        
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
