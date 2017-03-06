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

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import javax.swing.text.Document
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.psi.psiUtil.elementsInRange
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.hints.fixes.*
import org.jetbrains.kotlin.hints.intentions.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinError
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.modules.csl.api.*
import org.openide.text.NbDocument
import org.netbeans.modules.csl.api.HintsProvider.HintsManager
import org.netbeans.modules.csl.api.HintSeverity

class KotlinHintsProvider : HintsProvider {

    companion object {

        private fun listOfIntentions(parserResult: KotlinParserResult,
                                     psi: PsiElement) = parserResult.let {
            Pair(it.snapshot.source.getDocument(false), it.analysisResult?.analysisResult)
        }.let {
            listOf(
                    RemoveExplicitTypeIntention(it.first, it.second, psi),
                    SpecifyTypeIntention(it.first, it.second, psi),
                    ConvertToBlockBodyIntention(it.first, it.second, psi),
                    ConvertToExpressionBodyIntention(it.first, it.second, psi),
                    ChangeReturnTypeIntention(it.first, it.second, psi),
                    ConvertTryFinallyToUseCallIntention(it.first, it.second, psi),
                    ConvertForEachToForLoopIntention(it.first, it.second, psi),
                    ConvertEnumToSealedClassIntention(it.first, it.second, psi),
                    AddValToConstructorParameterIntention(it.first, it.second, psi),
                    ConvertPropertyInitializerToGetterIntention(it.first, it.second, psi),
                    ConvertToConcatenatedStringIntention(it.first, it.second, psi),
                    ConvertToStringTemplateIntention(it.first, it.second, psi),
                    ConvertTwoComparisonsToRangeCheckIntention(it.first, it.second, psi),
                    MergeIfsIntention(it.first, it.second, psi),
                    RemoveBracesIntention(it.first, it.second, psi),
                    SplitIfIntention(it.first, it.second, psi),
                    ToInfixIntention(it.first, it.second, psi),
                    RemoveEmptyClassBodyIntention(it.first, it.second, psi),
                    RemoveEmptyParenthesesFromLambdaCallIntention(it.first, it.second, psi),
                    RemoveEmptyPrimaryConstructorIntention(it.first, it.second, psi),
                    RemoveEmptySecondaryConstructorIntention(it.first, it.second, psi),
                    ReplaceSizeCheckWithIsNotEmptyIntention(it.first, it.second, psi)
            ).filter { it.isApplicable(psi.textRange.startOffset) }
        }

        private fun KotlinError.listOfQuickFixes(parserResult: KotlinParserResult) = listOf(
                RemoveUselessElvisFix(this, parserResult),
                ImplementMembersFix(this, parserResult),
                AutoImportFix(this, parserResult),
                RemoveUselessCastFix(this, parserResult),
                RemoveUnnecessarySafeCallFix(this, parserResult)
        ).filter(KotlinQuickFix::isApplicable)

        private val RuleContext.quickFixes
            get() = parserResult.diagnostics
                    .filterIsInstance(KotlinError::class.java)
                    .flatMap { it.listOfQuickFixes(parserResult as KotlinParserResult) }
                    .map(KotlinQuickFix::createHint)

    }

    override fun computeSuggestions(hintsManager: HintsManager, ruleContext: RuleContext,
                                    hints: MutableList<Hint>, offset: Int) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val doc = ruleContext.doc as StyledDocument

        val lineNumber = NbDocument.findLineNumber(doc, offset)
        val lastLine = NbDocument.findLineNumber(doc, doc.length)

        if (lineNumber == lastLine) return

        val lineStartOffset = NbDocument.findLineOffset(doc, lineNumber)
        val lineEndOffset = NbDocument.findLineOffset(doc, lineNumber + 1)

        val intentions = parserResult.ktFile.elementsInRange(TextRange(lineStartOffset, lineEndOffset))
                .toMutableList()
                .apply {
                    val elem = parserResult.ktFile.findElementAt(offset)
                    if (elem != null) {
                        add(elem)
                    }
                }
                .map { psi ->
                    listOfIntentions(parserResult, psi)
                            .map {
                                Hint(KotlinRule(HintSeverity.CURRENT_LINE_WARNING),
                                        it.description,
                                        parserResult.snapshot.source.fileObject,
                                        OffsetRange(offset, offset),
                                        listOf(it),
                                        20
                                )
                            }
                }

        hints.addAll(
                intentions.flatMap { it }
                        .distinctBy { it.description }
        )

    }

    override fun computeHints(hintsManager: HintsManager, ruleContext: RuleContext, hints: MutableList<Hint>) {
        val parserResult = ruleContext.parserResult as KotlinParserResult
        val ktFile = parserResult.ktFile
        val hintsComputer = KotlinHintsComputer(parserResult)
        val unusedComputer = UnusedImportsComputer(parserResult)
        
        ktFile.accept(hintsComputer)

        with(hints) {
            addAll(ruleContext.quickFixes)
            addAll(hintsComputer.hints)
            addAll(unusedComputer.getUnusedImports())
        }
    }

    override fun computeSelectionHints(hintsManager: HintsManager, ruleContext: RuleContext,
                                       list: List<Hint>, i: Int, i2: Int) {
    }

    override fun cancel() {}

    override fun getBuiltinRules() = emptyList<Rule>()

    override fun createRuleContext() = KotlinRuleContext()

    override fun computeErrors(hintsManager: HintsManager, ruleContext: RuleContext,
                               list: List<Hint>, errors: MutableList<Error>) {
        errors.addAll(ruleContext.parserResult.diagnostics)
    }

}

fun Document.atomicChange(change: Document.() -> Unit) = NbDocument.runAtomicAsUser(this as StyledDocument, { change() })