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

import java.io.File
import javax.swing.event.ChangeListener
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper.isScanning
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.api.java.source.SourceUtils
import org.netbeans.api.project.Project
import org.netbeans.modules.parsing.api.*
import org.netbeans.modules.parsing.spi.*
import org.openide.filesystems.FileUtil

class KotlinParser : Parser() {

    companion object {
        var file: KtFile? = null
            private set

        var project: Project? = null
            private set
        
        var analysisResult: AnalysisResultWithProvider? = null
        
        @JvmStatic fun getAnalysisResult(ktFile: KtFile,
                                         proj: Project) = if (ktFile upToDate file) analysisResult else analyze(ktFile, proj)
        
        private fun analyze(ktFile: KtFile, 
                            proj: Project): AnalysisResultWithProvider? = KotlinAnalyzer.analyzeFile(proj, ktFile)
                .also { 
                    project = proj 
                    file = ktFile
                    analysisResult = it
                }
        
        private infix fun KtFile.upToDate(ktFile: KtFile?) = 
                virtualFile.path == ktFile?.virtualFile?.path && text == ktFile.text
        
    }

    private lateinit var snapshot: Snapshot
    private var cancel = false

    override fun parse(snapshot: Snapshot, task: Task, event: SourceModificationEvent) {
        this.snapshot = snapshot
        cancel = false
        
        if (SourceUtils.isScanInProgress()) return
        
        val project = ProjectUtils.getKotlinProjectForFileObject(snapshot.source.fileObject)
        if (project.isScanning()) return
        if (cancel) return

        val ktFile = ProjectUtils.getKtFile(snapshot.text.toString(), snapshot.source.fileObject)

        getAnalysisResult(ktFile, project)
    }

    override fun getResult(task: Task): Result? {
        val project = project ?: return null
        val ktFile = if (snapshot.source.fileObject.path == file?.virtualFile?.path) file else null
        ktFile ?: return null
        val result = getAnalysisResult(ktFile, project) ?: return null

        return KotlinParserResult(snapshot, result, ktFile, project)
    }

    override fun addChangeListener(changeListener: ChangeListener) {}
    override fun removeChangeListener(changeListener: ChangeListener) {}

    override fun cancel(reason: CancelReason, event: SourceModificationEvent?) {
        cancel = true
        KotlinLogger.INSTANCE.logInfo("Parser cancel ${reason.name}")
    }
}