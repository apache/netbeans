package org.black.kotlin.projectsextensions.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Collections;
import org.apache.maven.model.Resource;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.black.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.projectsextensions.j2se.buildextender.KotlinBuildExtender;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenProjectOpenedHook extends ProjectOpenedHook{

    private final NbMavenProjectImpl project;
    
    public MavenProjectOpenedHook(NbMavenProjectImpl project) {
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        Thread thread = new Thread(){
                @Override
                public void run(){
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
                        
                        KotlinSources sources = new KotlinSources(project);
                        for (FileObject file : sources.getAllKtFiles()){
                            KotlinLightClassGeneration.INSTANCE.generate(file);
                        }
                        
                        project.getOriginalMavenProject().addCompileSourceRoot(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath());
                        project.getProjectWatcher().addPropertyChangeListener(new PropertyChangeListener(){
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                            }
                        });
                        
                        
//                        DefaultMavenProjectHelper helper = new DefaultMavenProjectHelper();
//                        helper.addResource(project.getOriginalMavenProject(), KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).getPath(), 
//                                Collections.emptyList(), Collections.emptyList());
                        
                        KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                    }
            };
        thread.start();
    }

    @Override
    protected void projectClosed() {
    }
    
}
