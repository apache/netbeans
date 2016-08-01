/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.project.KotlinSources;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.projectsextensions.maven.buildextender.PomXmlModifier;
import org.jetbrains.kotlin.utils.ProjectUtils;
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
                            new PomXmlModifier(project).checkPom();
                        }
                        
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
