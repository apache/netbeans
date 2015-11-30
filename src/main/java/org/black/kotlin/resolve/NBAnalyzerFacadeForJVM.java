/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.resolve;

import java.util.LinkedHashSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.context.GlobalContext;
import org.jetbrains.kotlin.context.ModuleContext;
//import org.jetbrains.kotlin.core.utils.ProjectUtils;//
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.utils.KotlinFrontEndException;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport.CliBindingTrace;

//import static org.jetbrains.kotlin.context.ContextPackage.ContextForNewModule;


/**
 *
 * @author polina
 */

public class NBAnalyzerFacadeForJVM {
    public static AnalysisResult analyzeFilesWithJavaIntegration(Project project, 
            Collection<KtFile> filesToAnalyze)
    {
        Set<KtFile> fileSet = Sets.newHashSet(filesToAnalyze);
        Set<KtFile> allFiles = Sets.newLinkedHashSet(fileSet);
//        val filesSet = filesToAnalyze.toSet()
//        if (filesSet.size != filesToAnalyze.size) {
//            KotlinLogger.logWarning("Analyzed files have duplicates")
//        }
        
//        val allFiles = LinkedHashSet<KtFile>(filesSet)
//        
//        
//        val addedFiles = filesSet.map { getPath(it) }.filterNotNull().toSet();
//        
//        
//                
//                
//        ProjectUtils.getSourceFilesWithDependencies(javaProject).filterNotTo(allFiles) {
//            getPath(it) in addedFiles
//        }
//        
        MutableModuleContext moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule((com.intellij.openapi.project.Project) project, project.toString());
        FileBasedDeclarationProviderFactory providerFactory = new FileBasedDeclarationProviderFactory(moduleContext.getStorageManager(), allFiles);
       // val moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, project.getName())
        //val providerFactory = FileBasedDeclarationProviderFactory(moduleContext.storageManager, allFiles)
        BindingTrace trace = new CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace();
        
        return TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationNoIncremental(
                moduleContext,
                allFiles,
                trace,
                TopDownAnalysisMode.TopLevelDeclarations, 
                PackagePartProvider.EMPTY);
        
//        ContainerForTopDownAnalyzerForJvm containerAndProvider = createContainerForTopDownAnalyzerForJvm
//        (moduleContext, 
//                trace, 
//                providerFactory, 
//                GlobalSearchScope.allScope((com.intellij.openapi.project.Project) project), 
//                project, 
//                LookupTracker.DO_NOTHING, 
//                PackagePartProvider.EMPTY);
//        val container = containerAndProvider.first
//        val additionalProviders = listOf(container.javaDescriptorResolver.packageFragmentProvider)
//        
//        try {
//            container.lazyTopDownAnalyzerForTopLevel.analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, filesSet, additionalProviders)
//        } catch(e: KotlinFrontEndException) {
////          Editor will break if we do not catch this exception
////          and will not be able to save content without reopening it.
////          In IDEA this exception throws only in CLI
//            KotlinLogger.logError(e)
//        }
//        
//        return AnalysisResultWithProvider(
//                AnalysisResult.success(trace.getBindingContext(), moduleContext.module),
//                containerAndProvider.second)
    }
    
    //private fun getPath(jetFile: KtFile): String? = jetFile.getVirtualFile()?.getPath()
}