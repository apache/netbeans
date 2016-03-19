package org.black.kotlin.model;

import com.google.common.collect.Lists;
import kotlin.jvm.Synchronized;
import kotlin.jvm.Volatile;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.NetBeansAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.api.project.Project;

public class KotlinAnalysisFileCache {
    
    class FileAnalysisResults{
        
        private final KtFile file;
        private final AnalysisResultWithProvider analysisResult;
        
        public FileAnalysisResults(KtFile file, AnalysisResultWithProvider analysisResult){
            this.file = file;
            this.analysisResult = analysisResult;
        }
        
        public KtFile getFile(){
            return file;
        }
        
        public AnalysisResultWithProvider getAnalysisResult(){
            return analysisResult;
        }
        
    } 
    
    public static KotlinAnalysisFileCache INSTANCE = new KotlinAnalysisFileCache();
    private @Volatile FileAnalysisResults lastAnalysedFileCache = null;
    
    private KotlinAnalysisFileCache(){}
    
    public @Synchronized AnalysisResultWithProvider getAnalysisResult(KtFile file, KotlinProject project){
        if (lastAnalysedFileCache != null && lastAnalysedFileCache.getFile() == file){
            return lastAnalysedFileCache.getAnalysisResult();
        } else {
            KotlinEnvironment kotlinEnvironment = KotlinEnvironment.getEnvironment(project);
            AnalysisResultWithProvider analysisResult = 
                    NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(
                            project, kotlinEnvironment.getProject(), Lists.newArrayList(file));
            lastAnalysedFileCache = new FileAnalysisResults(file, analysisResult);
            return lastAnalysedFileCache.getAnalysisResult();
        }
    }
    
    public void resetCache(){
        lastAnalysedFileCache = null;
    }
    
}
