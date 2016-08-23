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
package org.jetbrains.kotlin.resolve

import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.util.LinkedHashSet
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.project.Project as NBProject
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.utils.KotlinFrontEndException
import org.jetbrains.kotlin.incremental.components.LookupTracker

object NetBeansAnalyzerFacadeForJVM {

    public fun analyzeFilesWithJavaIntegration(
            kotlinProject: NBProject,
            project : Project,
            filesToAnalyze: Collection<KtFile>): AnalysisResultWithProvider {
        val filesSet = filesToAnalyze.toSet()
        if (filesSet.size != filesToAnalyze.size) {
        }
        
        val allFiles = LinkedHashSet<KtFile>(filesSet)
        val addedFiles = filesSet.map { getPath(it) }.filterNotNull().toSet()
        ProjectUtils.getSourceFilesWithDependencies(kotlinProject).filterNotTo(allFiles) {
            getPath(it) in addedFiles
        }
        
        val moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(project, project.getName())
        val providerFactory = FileBasedDeclarationProviderFactory(moduleContext.storageManager, allFiles)
        val trace = CliLightClassGenerationSupport.CliBindingTrace()
        
        val containerAndProvider = createContainerForTopDownAnalyzerForJvm(
                moduleContext,
                trace,
                providerFactory, 
                GlobalSearchScope.allScope(project),
                kotlinProject,
                LookupTracker.DO_NOTHING,
                KotlinPackagePartProvider(kotlinProject),
                LanguageVersion.LATEST)
        val container = containerAndProvider.first
        val additionalProviders = listOf(container.javaDescriptorResolver.packageFragmentProvider)
        
        try {
            container.lazyTopDownAnalyzerForTopLevel.analyzeFiles(TopDownAnalysisMode.TopLevelDeclarations, filesSet, additionalProviders)
        } catch(e: KotlinFrontEndException) {
        }
        
        return AnalysisResultWithProvider(
                AnalysisResult.success(trace.getBindingContext(), moduleContext.module),
                containerAndProvider.second)
}
    
    private fun getPath(jetFile: KtFile): String? = jetFile.getVirtualFile()?.getPath()
    
}