package org.black.kotlin.resolve;

import com.google.common.collect.Lists;
import java.util.Collection;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.jetbrains.annotations.NotNull;
///import org.jetbrains.kotlin.core.model.KotlinAnalysisFileCache;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.analyzer.AnalysisResult;

import org.jetbrains.kotlin.psi.KtFile;

public class KotlinAnalyzer {
    @NotNull
    public static AnalysisResult analyzeFile(@NotNull Project javaProject, @NotNull KtFile jetFile) {
        return analyzeFiles(javaProject, Lists.newArrayList(jetFile));
    }
    
    @NotNull
    private static AnalysisResult analyzeFiles(@NotNull Project javaProject, @NotNull KotlinEnvironment kotlinEnvironment, 
            @NotNull Collection<KtFile> filesToAnalyze) 
    {
        return NBAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                javaProject, 
                //kotlinEnvironment.getProject(), 
                filesToAnalyze);
    }
    
    public static AnalysisResult analyzeFiles(@NotNull Project javaProject, @NotNull Collection<KtFile> filesToAnalyze) {
        if (filesToAnalyze.size() == 1) {
            return analyzeFile(javaProject, filesToAnalyze.iterator().next());
        }
        
        KotlinEnvironment kotlinEnvironment = KotlinEnvironment.getEnvironment(javaProject);
        return analyzeFiles(javaProject, kotlinEnvironment, filesToAnalyze);
    }
}