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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import kotlin.Pair;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport;
import org.jetbrains.kotlin.config.LanguageVersion;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.MutableModuleContext;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.load.java.lazy.LazyJavaPackageFragmentProvider;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory;
import org.jetbrains.kotlin.utils.KotlinFrontEndException;


/**
 *
 * @author Александр
 */
public class NetBeansAnalyzerFacadeForJVM {
    
    public static NetBeansAnalyzerFacadeForJVM INSTANCE = new NetBeansAnalyzerFacadeForJVM();
    
    private NetBeansAnalyzerFacadeForJVM(){}
    
    public AnalysisResultWithProvider analyzeFilesWithJavaIntegration(org.netbeans.api.project.Project kotlinProject,
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
        
        for (KtFile file : ProjectUtils.getSourceFilesWithDependencies(kotlinProject)){
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
                
        Pair<ContainerForTopDownAnalyzerForJvm, StorageComponentContainer> containerAndProvider =
                Injection.createContainerForTopDownAnalyzerForJvm(moduleContext, trace, providerFactory, 
                        GlobalSearchScope.allScope(project), kotlinProject, LookupTracker.Companion.getDO_NOTHING(), 
                        new KotlinPackagePartProvider(kotlinProject), LanguageVersion.LATEST);
        
        ContainerForTopDownAnalyzerForJvm container = containerAndProvider.getFirst();
        List<LazyJavaPackageFragmentProvider> additionalProviders = 
                Lists.newArrayList(container.getJavaDescriptorResolver().getPackageFragmentProvider());
        
        try {
            container.getLazyTopDownAnalyzerForTopLevel().analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, 
                   filesSet, additionalProviders);
        } catch (KotlinFrontEndException e){
        }
        
        return new AnalysisResultWithProvider(
            AnalysisResult.success(trace.getBindingContext(), moduleContext.getModule()), 
                containerAndProvider.getSecond());
    }
    
    private String getPath(KtFile ktFile){
        return ktFile.getVirtualFile().getPath();
    }
    
}
