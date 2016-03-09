package org.black.kotlin.resolve;

import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.container.ComponentProvider;

/**
 *
 * @author Александр
 */
public class AnalysisResultWithProvider {

    private final AnalysisResult analysisResult;
    private final ComponentProvider componentProvider;
    
    public AnalysisResultWithProvider(AnalysisResult analysisResult, ComponentProvider componentProvider){
        this.analysisResult = analysisResult;
        this.componentProvider = componentProvider;
    }
    
    public AnalysisResult getAnalysisResult(){
        return analysisResult;
    }
    
    public ComponentProvider getComponentProvider(){
        return componentProvider;
    }
    
}
