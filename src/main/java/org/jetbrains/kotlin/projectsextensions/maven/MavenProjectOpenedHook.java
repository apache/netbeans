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
import java.io.IOException;
import java.lang.reflect.Method;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinAnalysisProjectCache;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.project.KotlinSources;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.lang.java.JavaEnvironment;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;

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
                        Runnable run = new Runnable(){
                            @Override
                            public void run(){
                                progressHandleRun = true;
                                final ProgressHandle progressbar = 
                                    ProgressHandleFactory.createHandle("Loading Kotlin environment");
                                progressbar.start();
                                KotlinEnvironment.Companion.getEnvironment(project);
                                progressbar.finish();
                                progressHandleRun = false;
                            }
                        };
                        if (!progressHandleRun) {
                            KotlinProjectHelper.INSTANCE.postTask(run);
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
                        
                        JavaEnvironment.INSTANCE.checkJavaSource(project);
                        try {
                            JavaEnvironment.INSTANCE.getJAVA_SOURCE().get(project).runWhenScanFinished(
                                    new Task<CompilationController>(){
                                        Runnable run = new Runnable(){
                                            @Override
                                            public void run(){
                                                final ProgressHandle progressbar = 
                                                    ProgressHandleFactory.createHandle("Kotlin files analysis...");
                                                progressbar.start();

                                                for (KtFile ktFile : ProjectUtils.getSourceFiles(project)) {
                                                    KotlinParser.getAnalysisResult(ktFile, project);
                                                }
                                                for (FileObject ktFile : new KotlinSources(project).getAllKtFiles()) {
                                                    IndexingManager.getDefault().refreshAllIndices(ktFile);
                                                }

                                                progressbar.finish();
                                            }
                                        };

                                        @Override
                                        public void run(CompilationController parameter) {
                                            KotlinProjectHelper.INSTANCE.postTask(run);
                                        }
                                    }, true);
                        } catch (IOException ex) {}
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
