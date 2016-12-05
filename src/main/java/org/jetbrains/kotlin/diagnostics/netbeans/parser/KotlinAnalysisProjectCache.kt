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
package org.jetbrains.kotlin.diagnostics.netbeans.parser

import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.project.KotlinSources
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.NetBeansAnalyzerFacadeForJVM
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.java.source.ScanUtils
import org.netbeans.api.project.Project
import org.netbeans.modules.parsing.api.indexing.IndexingManager
import org.jetbrains.kotlin.resolve.lang.java.*

object KotlinAnalysisProjectCache {

    private val cache = hashMapOf<Project, AnalysisResultWithProvider>()
    
    fun getAnalysisResult(project: Project): AnalysisResultWithProvider {
        if (!cache.containsKey(project)) {
            return getResult(project)
        }
        
        return cache[project]!!
    }
    
    @Synchronized private fun getResult(project: Project): AnalysisResultWithProvider {
        if (!cache.containsKey(project)) {
            val startTime = System.nanoTime()
            val result = NetBeansAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                    project, KotlinEnvironment.getEnvironment(project).project, 
                    ProjectUtils.getSourceFilesWithDependencies(project))
            cache.put(project, result)
            KotlinLogger.INSTANCE.logInfo("Project ${project.projectDirectory.path} analysis result cached")
            KotlinLogger.INSTANCE.logInfo("Kotlin analysis took ${(System.nanoTime() - startTime)}")
        }
        
        return cache[project]!!
    }
    
    @Synchronized fun removeProjectCache(project: Project) {
        cache.remove(project)
    }
    
}