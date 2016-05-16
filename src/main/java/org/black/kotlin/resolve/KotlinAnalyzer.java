package org.black.kotlin.resolve;

import java.util.Collection;
import org.black.kotlin.model.KotlinAnalysisFileCache;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

public class KotlinAnalyzer {
        
    @NotNull
    public static AnalysisResultWithProvider analyzeFile(@NotNull KotlinProject kotlinProject, @NotNull KtFile ktFile){
        return KotlinAnalysisFileCache.INSTANCE.getAnalysisResult(ktFile, kotlinProject);
    }

    @NotNull
    private static AnalysisResultWithProvider analyzeFiles(@NotNull KotlinProject kotlinProject, 
            @NotNull KotlinEnvironment kotlinEnvironment, @NotNull Collection<KtFile> filesToAnalyze){
        return NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(kotlinProject, kotlinEnvironment.getProject(), filesToAnalyze);
    }
    
    public static AnalysisResultWithProvider analyzeFiles(@NotNull KotlinProject kotlinProject, 
            @NotNull Collection<KtFile> filesToAnalyze){
        if (filesToAnalyze.size() == 1){
            return analyzeFile(kotlinProject, filesToAnalyze.iterator().next());
        }
        
        KotlinEnvironment kotlinEnvironment = KotlinEnvironment.getEnvironment(kotlinProject);
        return analyzeFiles(kotlinProject, kotlinEnvironment, filesToAnalyze);
    }
    
}
