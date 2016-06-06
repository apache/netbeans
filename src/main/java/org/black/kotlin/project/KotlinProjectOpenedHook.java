package org.black.kotlin.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.black.kotlin.model.KotlinEnvironment;
import static org.black.kotlin.utils.ProjectUtils.FILE_SEPARATOR;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Александр
 */
public class KotlinProjectOpenedHook extends ProjectOpenedHook {

    private final KotlinProject project;
    private final GlobalPathRegistry reg;

    public KotlinProjectOpenedHook(KotlinProject project) {
        super();
        this.project = project;
        reg = project.getPathRegistry();
        File path = new File(project.getProjectDirectory().getPath() + FILE_SEPARATOR + "build" + FILE_SEPARATOR + "classes");
        if (!path.exists()) {
            if(!path.mkdirs()){
                System.err.println("Cannot create a directory");
            }
        }
        
    }
    
    @Override
    public void projectOpened() {
        try {
            Thread thread = new Thread(){
                @Override
                public void run(){
                    try {
                        KotlinEnvironment.getEnvironment(project);
                        KotlinSources sources = new KotlinSources(project);
                        for (FileObject file : sources.getAllKtFiles()){
                            KotlinLightClassGeneration.INSTANCE.generate(file);
                        }
                        
                        List<ClassPath> paths = new ArrayList<ClassPath>();
                        FileObject classesRoot = project.getProjectDirectory().getFileObject("build").getFileObject("classes");
                        
                        List<URL> jars = getJars();
                        
                        paths.add(ClassPathSupport.createClassPath(jars.toArray(new URL[jars.size()])));
                        paths.add(ClassPathSupport.createClassPath(classesRoot.toURL()));
                        
                        reg.register(ClassPath.BOOT, paths.toArray(new ClassPath[paths.size()]));
                        reg.register(ClassPath.COMPILE, paths.toArray(new ClassPath[paths.size()]));
                        reg.register(ClassPath.EXECUTE, paths.toArray(new ClassPath[paths.size()]));
                        
                        FileObject srcRoot = project.getProjectDirectory().getFileObject("src");
                        reg.register(ClassPath.SOURCE, new ClassPath[]{ClassPathSupport.createClassPath(srcRoot.toURL())});
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            thread.start();
        } catch (UnsupportedOperationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private List<URL> getJars() throws MalformedURLException {
        FileObject libs = project.getProjectDirectory().getFileObject("lib");
        List<URL> jars = new ArrayList<URL>();
        for (FileObject fo : libs.getChildren()) {
            jars.add(new URL("jar:file:///" + fo.getPath() + "!/"));
        }
        return jars;
    }


    @Override
    protected void projectClosed() {
    }

}
