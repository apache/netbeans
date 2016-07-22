package org.black.kotlin.projectsextensions.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.black.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.black.kotlin.projectsextensions.maven.buildextender.PomXmlModifier;
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
                        
                        KotlinSources sources = KotlinProjectHelper.INSTANCE.getKotlinSources(project);
                        if (!sources.hasLightClasses()) {
                            for (FileObject file : sources.getAllKtFiles()){
                                KotlinLightClassGeneration.INSTANCE.generate(file, project);
                            }
                            sources.lightClassesGenerated();
                        }
                        if (!MavenHelper.hasParent(project)){
                            new PomXmlModifier(project);
                        }
//                        KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                        
                        project.getProjectWatcher().addPropertyChangeListener(new PropertyChangeListener(){
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                            }
                        });
                    }
            };
        thread.start();
    }

    @Override
    protected void projectClosed() {
    }
    
}
