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
package org.jetbrains.kotlin.hints

import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.hints.intentions.*
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.hints.KotlinRule
import org.netbeans.modules.csl.api.HintFix
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.api.HintSeverity
import org.openide.filesystems.FileObject

class KotlinHintsComputer(val parserResult: KotlinParserResult) : KtVisitor<Unit, Unit>() {

    val hints = arrayListOf<Hint>()
    
    override fun visitKtFile(ktFile: KtFile, data: Unit?) {
        ktFile.children.forEach {
            it.accept(this)
        }
    }
    
    override fun visitKtElement(element: KtElement, data: Unit?) {
        hints.addAll(element.inspections())
        
        element.children.forEach { it.accept(this) }
    }
    
    private fun KtElement.inspections() = listOf(
            RemoveEmptyPrimaryConstructorInspection(parserResult, this),
            RemoveEmptyClassBodyInspection(parserResult, this),
            ConvertToStringTemplateInspection(parserResult, this),
            ConvertTryFinallyToUseCallInspection(parserResult, this),
            RemoveEmptySecondaryConstructorInspection(parserResult, this),
            ReplaceSizeCheckWithIsNotEmptyInspection(parserResult, this)
    )
            .filter(Inspection::isApplicable)
            .map { it.hint(parserResult.snapshot.source.fileObject) }
        
}