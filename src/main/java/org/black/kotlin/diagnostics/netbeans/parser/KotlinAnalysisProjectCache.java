package org.black.kotlin.diagnostics.netbeans.parser;

import java.util.HashMap;
import java.util.Map;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.resolve.AnalysisResultWithProvider;
import org.black.kotlin.resolve.NetBeansAnalyzerFacadeForJVM;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinAnalysisProjectCache {

    public static final KotlinAnalysisProjectCache INSTANCE = 
            new KotlinAnalysisProjectCache();
    
    private final Map<Project, AnalysisResultWithProvider> cache = new HashMap<Project, AnalysisResultWithProvider>();
    
    public AnalysisResultWithProvider getAnalysisResult(Project project) {
        synchronized(project) {
            if (cache.get(project) == null) {
                AnalysisResultWithProvider result = 
                        NetBeansAnalyzerFacadeForJVM.INSTANCE.analyzeFilesWithJavaIntegration(project, 
                        KotlinEnvironment.getEnvironment(project).getProject(), ProjectUtils.getSourceFilesWithDependencies(project));
                cache.put(project, result);
            }
            
            return cache.get(project);
        }
    }
    
}
