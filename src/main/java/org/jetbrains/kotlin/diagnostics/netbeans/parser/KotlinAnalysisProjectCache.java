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
package org.jetbrains.kotlin.diagnostics.netbeans.parser;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.project.KotlinSources;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.NetBeansAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinAnalysisProjectCache {

    public static final KotlinAnalysisProjectCache INSTANCE = 
            new KotlinAnalysisProjectCache();
    
    private final Map<Project, AnalysisResultWithProvider> cache = new HashMap<Project, AnalysisResultWithProvider>();
    
    public AnalysisResultWithProvider getAnalysisResult(final Project project) {
        synchronized(project) {
            if (cache.get(project) == null) {
                final AnalysisResultWithProvider result = 
                        NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(project, 
                        KotlinEnvironment.getEnvironment(project).getProject(), ProjectUtils.getSourceFilesWithDependencies(project));
                cache.put(project, result);
                
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
                        KotlinSources sources = new KotlinSources(project);
                        for (FileObject file : sources.getAllKtFiles()){
                            KotlinLightClassGeneration.INSTANCE.generate(file, 
                                    project, result.getAnalysisResult());
                        }
//                    }
//                };
                
//                new Thread(runnable).start();
            }
            
            return cache.get(project);
        }
    }
    
}
