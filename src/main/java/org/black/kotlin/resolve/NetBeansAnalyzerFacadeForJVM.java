package org.black.kotlin.resolve;

import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import java.util.Collection;
import java.util.Set;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;


/**
 *
 * @author Александр
 */
public class NetBeansAnalyzerFacadeForJVM {
    
    public static NetBeansAnalyzerFacadeForJVM INSTANCE = new NetBeansAnalyzerFacadeForJVM();
    
    private NetBeansAnalyzerFacadeForJVM(){}
    
    public AnalysisResultWithProvider analyzeFilesWithJavaIntegration(org.netbeans.api.project.Project javaProject,
            Project project, Collection<KtFile> filesToAnalyze){
        
        Set<KtFile> filesSet = Sets.newLinkedHashSet(filesToAnalyze);
        if (filesSet.size() != filesToAnalyze.size()){
            System.out.println("Files have duplicates");
        }
        
        Set<KtFile> allFiles = Sets.newLinkedHashSet(filesSet);
        
        Set<String> addedFiles = Sets.newLinkedHashSet();
        for (KtFile file : filesSet){
            if (getPath(file) != null){
                addedFiles.add(getPath(file));
            }
        }
        
        for (KtFile file : ProjectUtils.getSourceFilesWithDependencies(javaProject)){
            if (!addedFiles.contains(getPath(file))){
                allFiles.add(file);
            }
        }
        
        MutableModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(
                project, project.getName());
        FileBasedDeclarationProviderFactory providerFactory =
                new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
        CliLightClassGenerationSupport.CliBindingTrace trace = 
                new CliLightClassGenerationSupport.CliBindingTrace();
        
        
        return null;
    }
    
    private String getPath(KtFile ktFile){
        return ktFile.getVirtualFile().getPath();
    }
    
}
