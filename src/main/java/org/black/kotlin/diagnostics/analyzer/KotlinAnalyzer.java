package org.black.kotlin.diagnostics.analyzer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.black.kotlin.model.KotlinEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.netbeans.api.project.Project;

public class KotlinAnalyzer {
    
    public static AnalysisResult analyzeFile(@NotNull Project project, @NotNull KtFile ktFile){
        return analyzeFiles(project, Lists.newArrayList(ktFile));
    }
    
    public static AnalysisResult analyzeFiles(@NotNull Project project, @NotNull Collection<KtFile> ktFiles){
        Set<KtFile> fileSet = Sets.newHashSet(ktFiles);
        Set<KtFile> allFiles = Sets.newLinkedHashSet(fileSet);
        
        MutableModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.
                createContextWithSealedModule(KotlinEnvironment.getEnvironment(project).getProject(), 
                        project.getProjectDirectory().getName());
        FileBasedDeclarationProviderFactory providerFactory = 
                new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
        BindingTrace trace = new CliLightClassGenerationSupport.CliBindingTrace();
        
        
        
        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationNoIncremental(
                moduleContext, allFiles, trace, 
                TopDownAnalysisMode.TopLevelDeclarations,
                null);
    }
    
    
}
