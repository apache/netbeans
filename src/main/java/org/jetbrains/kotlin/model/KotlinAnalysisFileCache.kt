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
package org.jetbrains.kotlin.model

import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.NetBeansAnalyzerFacadeForJVM
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.api.project.Project

data class FileAnalysisResults(val file: KtFile, val analysisResult: AnalysisResultWithProvider)

object KotlinAnalysisFileCache {

    private @Volatile var lastAnalysedFileCache: FileAnalysisResults? = null
    
    @Synchronized fun getAnalysisResult(file: KtFile, project: Project): AnalysisResultWithProvider {
        val cache = lastAnalysedFileCache
        if (cache != null && cache.file == file) {
            return cache.analysisResult
        } else {
            val kotlinEnvironment = KotlinEnvironment.Companion.getEnvironment(project)
            val analysisResult = NetBeansAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                    project, kotlinEnvironment.project, listOf(file))
            val fileAnalysisResult = FileAnalysisResults(file, analysisResult)
            lastAnalysedFileCache = fileAnalysisResult
            return fileAnalysisResult.analysisResult
        }
    }

    fun resetCache() {
        lastAnalysedFileCache = null
    }

}