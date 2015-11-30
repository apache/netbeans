package org.black.kotlin.project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.black.kotlin.project.KotlinProject.KotlinSources;
import org.black.kotlin.run.KotlinCompiler;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinProjectOpenedHook extends ProjectOpenedHook {

    private final KotlinProject proj;
    private final GlobalPathRegistry reg;

    public KotlinProjectOpenedHook(KotlinProject proj) {
        super();
        this.proj = proj;
        reg = proj.getPathRegistry();
    }

    @Override
    protected void projectOpened() {
        try {
//            KotlinSources src = (KotlinSources) ProjectUtils.getSources(proj);
//            ClassPathProvider classPathProvider = proj.getLookup().lookup(ClassPathProvider.class);
//            List<FileObject> files = src.getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
//            files.addAll(src.getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE));
            KotlinCompiler.INSTANCE.antCompile(proj);
            List<ClassPath> paths = new ArrayList();
            
            FileObject srcRoot = null;
            
            while (srcRoot == null)
                srcRoot = proj.getProjectDirectory().getFileObject("build").getFileObject("classes");
            
            List<URL> jars = getJars();
            
            paths.add(ClassPathSupport.createClassPath(jars.toArray(new URL[jars.size()])));
            paths.add(ClassPathSupport.createClassPath(srcRoot.toURL()));
            
            reg.register(ClassPath.SOURCE, paths.toArray(new ClassPath[paths.size()]));
            reg.register(ClassPath.BOOT, paths.toArray(new ClassPath[paths.size()]));
            reg.register(ClassPath.COMPILE, paths.toArray(new ClassPath[paths.size()]));

//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                Message(reg.findResource("JMapViewer.jar")));
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.
                Message(reg.getSourceRoots()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedOperationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private List<URL> getJars() throws MalformedURLException {
        FileObject libs = proj.getProjectDirectory().getFileObject("lib");
        List<URL> jars = new ArrayList();
        for (FileObject fo : libs.getChildren()) {
            jars.add(new URL("jar:file:///" + fo.getPath() + "!/"));
        }
//        FileObject src = proj.getProjectDirectory().getFileObject("build").getFileObject("Kotlin_Project.jar");
//        jars.add(new URL("jar:file:///" + src.getPath() + "!/"));
        return jars;
    }
    
    private List<URL> getRoots() throws MalformedURLException{
        List<URL> roots = new ArrayList();
        KotlinSources src = (KotlinSources) ProjectUtils.getSources(proj);
        List<FileObject> files = src.getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
        files.addAll(src.getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE));
        for (FileObject fo : files){
//            roots.add(new URL("file://"+fo.getPath()+"/"));
        }
        roots.add(new URL("file://C:/Users/Александр/Documents/NetBeansProjects/KotlinJMapViewer/src/"));
        return roots;
    }

    @Override
    protected void projectClosed() {
//        try {
//            KotlinSources src = (KotlinSources) ProjectUtils.getSources(proj);
//            ClassPathProvider classPathProvider = proj.getLookup().lookup(ClassPathProvider.class);
//            List<FileObject> files = src.getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
//            files.addAll(src.getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE));
//            List<ClassPath> paths = new ArrayList();
//            for (FileObject file : files) {
//                paths.add(classPathProvider.findClassPath(file, ClassPath.SOURCE));
//                paths.add(classPathProvider.findClassPath(file, ClassPath.BOOT));
//                paths.add(classPathProvider.findClassPath(file, ClassPath.COMPILE));
//            }
//            List<URL> jars = getJars();
//            paths.add(ClassPathSupport.createClassPath(jars.toArray(new URL[jars.size()])));
//            reg.unregister(ClassPath.SOURCE, paths.toArray(new ClassPath[paths.size()]));
//            reg.unregister(ClassPath.BOOT, paths.toArray(new ClassPath[paths.size()]));
//            reg.unregister(ClassPath.COMPILE, paths.toArray(new ClassPath[paths.size()]));
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        }

    }

}
