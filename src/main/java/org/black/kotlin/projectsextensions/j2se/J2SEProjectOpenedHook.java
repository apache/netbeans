package org.black.kotlin.projectsextensions.j2se;

import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import java.io.File;
import org.black.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.black.kotlin.projectsextensions.j2se.buildextender.KotlinBuildExtender;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.utils.KotlinClasspath;
import org.black.kotlin.utils.ProjectUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class J2SEProjectOpenedHook extends ProjectOpenedHook{

    private final Project project;
    
    public J2SEProjectOpenedHook(Project project) {
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
                            KotlinLightClassGeneration.INSTANCE.generate(file, project);
                        }
                        
                        KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                        KotlinBuildExtender extender = new KotlinBuildExtender(project);
                        extender.addKotlinTasksToScript(project);
                        
                        J2SEProjectPropertiesModifier propsModifier = project.getLookup().lookup(J2SEProjectPropertiesModifier.class);
                        propsModifier.turnOffCompileOnSave();
                        propsModifier.addKotlinRuntime();
                    }
            };
        thread.start();
    }

    @Override
    protected void projectClosed() {
    }
    
}
