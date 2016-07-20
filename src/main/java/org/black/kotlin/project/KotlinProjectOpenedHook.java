package org.black.kotlin.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.black.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.utils.ProjectUtils;
import static org.black.kotlin.utils.ProjectUtils.FILE_SEPARATOR;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Александр
 */
public class KotlinProjectOpenedHook extends ProjectOpenedHook {

    private final Project project;
    private final GlobalPathRegistry reg;

    public KotlinProjectOpenedHook(Project project) {
        super();
        this.project = project;
        reg = GlobalPathRegistry.getDefault();
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
                        ClassLoader cl = this.getClass().getClassLoader();
                        ProjectUtils.checkKtHome(cl);
                        Runnable run = new Runnable(){
                            @Override
                            public void run(){
                                final ProgressHandle progressbar = 
                                    ProgressHandleFactory.createHandle("Loading Kotlin environment");
                                progressbar.start();
                                KotlinEnvironment.getEnvironment(project);
                                progressbar.finish();
                            }
                        };
                        
                        RequestProcessor.getDefault().post(run);
                        
                        KotlinProjectHelper.INSTANCE.getKotlinClassPathProvider(project).updateClassPathProvider();
                        
//                        ((KotlinClassPathProvider) project.getLookup().lookup(ClassPathProvider.class)).updateClassPathProvider();
                        
                        KotlinSources sources = new KotlinSources(project);
                        for (FileObject file : sources.getAllKtFiles()){
                            KotlinLightClassGeneration.INSTANCE.generate(file, project);
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
        if (libs == null){
            return Collections.EMPTY_LIST;
        }
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
