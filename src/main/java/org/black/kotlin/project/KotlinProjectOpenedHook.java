package org.black.kotlin.project;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.project.KotlinProject.KotlinSources;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
            KotlinSources src = (KotlinSources) ProjectUtils.getSources(proj);
            ClassPathProvider classPathProvider = proj.getLookup().lookup(ClassPathProvider.class);
            List<FileObject> files = src.getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
            files.addAll(src.getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE));
            List<ClassPath> paths = new ArrayList();
            for (FileObject file : files) {
                paths.add(classPathProvider.findClassPath(file, ClassPath.SOURCE));
                paths.add(classPathProvider.findClassPath(file, ClassPath.BOOT));
                paths.add(classPathProvider.findClassPath(file, ClassPath.COMPILE));
            }
            List<URL> jars = getJars();
            paths.add(ClassPathSupport.createClassPath(jars.toArray(new URL[jars.size()])));
            reg.register(ClassPath.SOURCE, paths.toArray(new ClassPath[paths.size()]));
            reg.register(ClassPath.BOOT, paths.toArray(new ClassPath[paths.size()]));
            reg.register(ClassPath.COMPILE, paths.toArray(new ClassPath[paths.size()]));

//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                Message(reg.findResource("JMapViewer.jar")));
//        DialogDisplayer.getDefault().notify(new NotifyDescriptor.
//                Message(reg.getSourceRoots()));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private List<URL> getJars() throws MalformedURLException {
        FileObject libs = proj.getProjectDirectory().getFileObject("lib");
        List<URL> jars = new ArrayList();
        for (FileObject fo : libs.getChildren()) {
            jars.add(new URL("jar:file:///" + fo.getPath() + "!/"));
        }
        return jars;
    }

    @Override
    protected void projectClosed() {
        try {
            KotlinSources src = (KotlinSources) ProjectUtils.getSources(proj);
            ClassPathProvider classPathProvider = proj.getLookup().lookup(ClassPathProvider.class);
            List<FileObject> files = src.getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE);
            files.addAll(src.getSrcDirectories(KotlinProjectConstants.JAVA_SOURCE));
            List<ClassPath> paths = new ArrayList();
            for (FileObject file : files) {
                paths.add(classPathProvider.findClassPath(file, ClassPath.SOURCE));
                paths.add(classPathProvider.findClassPath(file, ClassPath.BOOT));
                paths.add(classPathProvider.findClassPath(file, ClassPath.COMPILE));
            }
            List<URL> jars = getJars();
            paths.add(ClassPathSupport.createClassPath(jars.toArray(new URL[jars.size()])));
            reg.unregister(ClassPath.SOURCE, paths.toArray(new ClassPath[paths.size()]));
            reg.unregister(ClassPath.BOOT, paths.toArray(new ClassPath[paths.size()]));
            reg.unregister(ClassPath.COMPILE, paths.toArray(new ClassPath[paths.size()]));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

}
