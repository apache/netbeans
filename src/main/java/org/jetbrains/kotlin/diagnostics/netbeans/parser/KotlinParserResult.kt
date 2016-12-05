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

import com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.api.project.Project
import org.netbeans.modules.csl.api.Error
import org.netbeans.modules.csl.spi.ParserResult
import org.netbeans.modules.parsing.spi.ParseException
import org.openide.filesystems.FileObject
import org.netbeans.modules.parsing.api.Snapshot

class KotlinParserResult(snapshot: Snapshot,
                         val analysisResult: AnalysisResultWithProvider?,
                         val ktFile: KtFile, val project: Project) : ParserResult(snapshot) {

    private val file = snapshot.source.fileObject
    
    override fun invalidate() {}

    override fun getDiagnostics() = if (analysisResult == null) emptyList<Error>() else arrayListOf<Error>().apply {
        addAll(
                analysisResult.analysisResult.bindingContext.diagnostics.all()
                        .filter { it.psiFile.virtualFile.path == file.path }
                        .map { KotlinError(it, file) }
        )
        addAll(
                AnalyzingUtils.getSyntaxErrorRanges(ktFile)
                        .map { KotlinSyntaxError(it, file) }
        )
    }
    
}