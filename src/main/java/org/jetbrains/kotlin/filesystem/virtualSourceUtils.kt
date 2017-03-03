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
package org.jetbrains.kotlin.filesystem

import java.io.File
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.NetBeansAnalyzerFacadeForJVM
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.utils.hasMain
import org.netbeans.api.project.Project
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil

fun translate(files: Iterable<File>, result: VirtualSourceProvider.Result) {
    KotlinLogger.INSTANCE.logInfo("KotlinVirtualSourceProvider translate $files")

    val filesToTranslate = if (files.firstOrNull().skipTranslating()) {
        KotlinLogger.INSTANCE.logInfo("No java files. Translating only kt files with main functions")
        files.mapNotNull { FileUtil.toFileObject(FileUtil.normalizeFile(it)) }
                .filter { ProjectUtils.getKtFile(it).hasMain() }
    } else files.mapNotNull { FileUtil.toFileObject(FileUtil.normalizeFile(it)) }

    val project = filesToTranslate.firstOrNull()?.let { ProjectUtils.getKotlinProjectForFileObject(it) } ?: return

    if (filesToTranslate.size == KotlinPsiManager.getFilesByProject(project, false).size) {
        val startTime = System.nanoTime()
        val analysisResult = NetBeansAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                project, KotlinEnvironment.getEnvironment(project).project,
                ProjectUtils.getSourceFilesWithDependencies(project))
        KotlinLogger.INSTANCE.logInfo("Kotlin analysis took ${(System.nanoTime() - startTime)}")
        filesToTranslate.translate(result, analysisResult, project)
        return
    }

    filesToTranslate.translate(result)
}

private fun List<FileObject>.translate(result: VirtualSourceProvider.Result,
                                       analysisResult: AnalysisResultWithProvider? = null,
                                       proj: Project? = null) = map { Pair(it, it.byteCode(analysisResult, proj)) }
        .filter { it.second.isNotEmpty() }
        .forEach {
            val stubs = JavaStubGenerator.gen(it.second)
            stubs.forEach { stub ->
                val code = stub.second
                val packageName = stub.first.name.substringBeforeLast("/")

                result.add(FileUtil.toFile(it.first), packageName, it.first.name, code)
            }
        }

private fun File?.skipTranslating(): Boolean {
    if (this == null) return true

    val normalizedFile = FileUtil.normalizeFile(this)
    val fo = FileUtil.toFileObject(normalizedFile) ?: return false
    val project = ProjectUtils.getKotlinProjectForFileObject(fo) ?: return false

    return !KotlinProjectHelper.hasJavaFiles(project)
}

private fun FileObject.byteCode(result: AnalysisResultWithProvider? = null,
                                proj: Project? = null): List<ByteArray> {
    val project = proj ?: ProjectUtils.getKotlinProjectForFileObject(this) ?: return emptyList()
    val ktFile = ProjectUtils.getKtFile(this) ?: return emptyList()
    val analysisResult = result ?: KotlinParser.getAnalysisResult(ktFile, project) ?: return emptyList()

    return KotlinLightClassGeneration.getByteCode(this, project, analysisResult.analysisResult)
}

