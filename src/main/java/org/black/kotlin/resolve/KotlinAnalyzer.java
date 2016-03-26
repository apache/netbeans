package org.black.kotlin.resolve;

import java.util.Collection;
import org.black.kotlin.model.KotlinAnalysisFileCache;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

public class KotlinAnalyzer {
        
    @NotNull
    public static AnalysisResultWithProvider analyzeFile(@NotNull KotlinProject javaProject, @NotNull KtFile ktFile){
        return KotlinAnalysisFileCache.INSTANCE.getAnalysisResult(ktFile, javaProject);
    }

    @NotNull
    private static AnalysisResultWithProvider analyzeFiles(@NotNull KotlinProject javaProject, 
            @NotNull KotlinEnvironment kotlinEnvironment, @NotNull Collection<KtFile> filesToAnalyze){
        return NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(
                javaProject, kotlinEnvironment.getProject(), filesToAnalyze);
    }
    
    public static AnalysisResultWithProvider analyzeFiles(@NotNull KotlinProject javaProject, 
            @NotNull Collection<KtFile> filesToAnalyze){
        if (filesToAnalyze.size() == 1){
            return analyzeFile(javaProject, filesToAnalyze.iterator().next());
        }
        
        KotlinEnvironment kotlinEnvironment = KotlinEnvironment.getEnvironment(javaProject);
        return analyzeFiles(javaProject, kotlinEnvironment, filesToAnalyze);
    }
    
}
