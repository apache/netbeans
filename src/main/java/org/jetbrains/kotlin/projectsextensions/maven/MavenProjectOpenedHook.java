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
import java.lang.reflect.Method;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinAnalysisProjectCache;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class MavenProjectOpenedHook extends ProjectOpenedHook{

    private static volatile boolean progressHandleRun = false;
    private final Project project;
    
    public MavenProjectOpenedHook(Project project) {
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        Thread thread = new Thread(){
                @Override
                public void run(){
                        ProjectUtils.checkKtHome();
                        Runnable run = new Runnable(){
                            @Override
                            public void run(){
                                progressHandleRun = true;
                                final ProgressHandle progressbar = 
                                    ProgressHandleFactory.createHandle("Loading Kotlin environment");
                                progressbar.start();
                                KotlinEnvironment.getEnvironment(project);
                                progressbar.finish();
                                progressHandleRun = false;
                            }
                        };
                        if (!progressHandleRun) {
                            RequestProcessor.getDefault().post(run);
                        }
                        
                        NbMavenProject projectWatcher = getProjectWatcher();
                        if (projectWatcher == null) {
                            return;
                        }
                        
                        projectWatcher.addPropertyChangeListener(new PropertyChangeListener(){
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
                            }
                        });
                    }
            };
        thread.start();
    }

    private NbMavenProject getProjectWatcher() {
        Class clazz = project.getClass();
        try {
            Method getProjectWatcher = clazz.getMethod("getProjectWatcher");
            return (NbMavenProject) getProjectWatcher.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return null;
    }
    
    @Override
    protected void projectClosed() {
        KotlinAnalysisProjectCache.INSTANCE.removeProjectCache(project);
        KotlinProjectHelper.INSTANCE.removeProjectCache(project);
    }
    
}
