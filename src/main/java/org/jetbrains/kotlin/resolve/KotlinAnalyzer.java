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
package org.jetbrains.kotlin.resolve;

import java.util.Collection;
import org.jetbrains.kotlin.model.KotlinAnalysisFileCache;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.Project;

public class KotlinAnalyzer {
        
    @NotNull
    public static AnalysisResultWithProvider analyzeFile(@NotNull Project kotlinProject, @NotNull KtFile ktFile){
        return KotlinAnalysisFileCache.INSTANCE.getAnalysisResult(ktFile, kotlinProject);
    }

    @NotNull
    private static AnalysisResultWithProvider analyzeFiles(@NotNull Project kotlinProject, 
            @NotNull KotlinEnvironment kotlinEnvironment, @NotNull Collection<KtFile> filesToAnalyze){
        return NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(kotlinProject, kotlinEnvironment.getProject(), filesToAnalyze);
    }
    
    public static AnalysisResultWithProvider analyzeFiles(@NotNull Project kotlinProject, 
            @NotNull Collection<KtFile> filesToAnalyze){
        if (filesToAnalyze.size() == 1){
            return analyzeFile(kotlinProject, filesToAnalyze.iterator().next());
        }
        
        KotlinEnvironment kotlinEnvironment = KotlinEnvironment.Companion.getEnvironment(kotlinProject);
        return analyzeFiles(kotlinProject, kotlinEnvironment, filesToAnalyze);
    }
    
}
