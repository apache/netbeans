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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinError
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser.KotlinParserResult
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.resolve.AnalyzingUtils
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.modules.csl.api.Error
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.HintFix
import org.netbeans.modules.csl.api.HintsProvider
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.api.Rule
import org.netbeans.modules.csl.api.RuleContext
import org.openide.filesystems.FileObject
import org.netbeans.modules.csl.api.HintsProvider.HintsManager
import org.jetbrains.kotlin.diagnostics.Errors
import org.netbeans.modules.csl.api.HintSeverity


class KotlinHintsProvider : HintsProvider {

    override fun computeSuggestions(hintsManager: HintsManager, ruleContext: RuleContext,
                                    hints: MutableList<Hint>, offset: Int) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val psi = parserResult.ktFile.findElementAt(offset) ?: return

        hints.addAll(getSuggestions(parserResult, psi, offset)
                .map {
                    Hint(KotlinRule(HintSeverity.CURRENT_LINE_WARNING),
                            it.description,
                            parserResult.snapshot.source.fileObject,
                            OffsetRange(offset, offset),
                            listOf(it), 20)
                }
        )
    }

    private fun getSuggestions(parserResult: KotlinParserResult,
                               psi: PsiElement, offset: Int) = listOf<ApplicableFix>(
            KotlinRemoveExplicitTypeFix(parserResult, psi),
            KotlinSpecifyTypeFix(parserResult, psi))
            .filter { it.isApplicable(offset) }

    override fun computeSelectionHints(hintsManager: HintsManager, ruleContext: RuleContext,
                                       list: List<Hint>, i: Int, i2: Int) {
    }

    override fun cancel() {
    }

    override fun getBuiltinRules() = emptyList<Rule>()
    override fun createRuleContext() = KotlinRuleContext()

    override fun computeHints(hintsManager: HintsManager, ruleContext: RuleContext, hints: MutableList<Hint>) {
        hints.addAll(getHints(ruleContext))
    }

    private fun getHints(ruleContext: RuleContext) =
            (ruleContext.parserResult as KotlinParserResult).diagnostics
                    .filterIsInstance(KotlinError::class.java)
                    .map { it.createHint(ruleContext.parserResult as KotlinParserResult) }
                    .filterNotNull()

    private fun KotlinError.createHint(parserResult: KotlinParserResult) =
            when (diagnostic.factory) {
                Errors.UNRESOLVED_REFERENCE -> createHintForUnresolvedReference(parserResult)
                Errors.ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED -> createImplementMembersHint(parserResult)
                else -> null
            }

    private fun KotlinError.createHintForUnresolvedReference(parserResult: KotlinParserResult): Hint {
        val suggestions = parserResult.project.findFQName(this.psi.text)
        val fixes = suggestions.map { KotlinAutoImportFix(it, parserResult) }

        return Hint(KotlinRule(HintSeverity.ERROR), "Class not found", parserResult.snapshot.source.fileObject,
                OffsetRange(this.startPosition, this.endPosition), fixes, 10)
    }

    private fun KotlinError.createImplementMembersHint(parserResult: KotlinParserResult): Hint {
        val fix = KotlinImplementMembersFix(parserResult, this.psi)
        return Hint(KotlinRule(HintSeverity.ERROR), "Implement members", parserResult.snapshot.source.fileObject,
                OffsetRange(this.startPosition, this.endPosition), listOf(fix), 10)
    }

    override fun computeErrors(hintsManager: HintsManager, ruleContext: RuleContext,
                               list: List<Hint>, errors: MutableList<Error>) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val file = parserResult.snapshot.source.fileObject

        val analysisResult = parserResult.analysisResult ?: return
        errors.addAll(analysisResult.analysisResult.bindingContext.diagnostics.all()
                .filter { it.psiFile.virtualFile.path == file.path }
                .map { KotlinParser.KotlinError(it, file) })
        errors.addAll(AnalyzingUtils.getSyntaxErrorRanges(parserResult.ktFile)
                .map { KotlinParser.KotlinSyntaxError(it, file) })
    }

}

interface ApplicableFix : HintFix {
    fun isApplicable(caretOffset: Int): Boolean
}