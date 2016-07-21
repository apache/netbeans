package org.black.kotlin.projectsextensions.maven.buildextender;

import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    public MavenModuledProjectExtendedClassPath(NbMavenProjectImpl project) {
        this.project = project;
        try {
            addChildrenOfMavenProject();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        addClassPathFromChildren();
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
                sourceClasspath.add(new MavenModuledProjectExtendedClassPath(child).getProjectSourcesClassPath(ClassPath.SOURCE));
            } else {
                try {
                    sourceClasspath.add(getClasspath(child.getOriginalMavenProject().getCompileSourceRoots()));
                } catch (DependencyResolutionRequiredException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
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
                ClassPath source = getClasspath(project.getOriginalMavenProject().getCompileSourceRoots());
                sourceClasspath.add(source);
                
                return ClassPathSupport.createProxyClassPath(sourceClasspath.toArray(new ClassPath[sourceClasspath.size()]));
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
