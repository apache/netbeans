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
import com.intellij.psi.PsiErrorElement
import javax.swing.text.Document
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.psi.psiUtil.elementsInRange
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.hints.fixes.*
import org.jetbrains.kotlin.hints.intentions.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinError
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinSyntaxError
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
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
import org.openide.text.NbDocument
import org.netbeans.modules.csl.api.HintsProvider.HintsManager
import org.jetbrains.kotlin.diagnostics.Errors
import org.netbeans.modules.csl.api.HintSeverity

class KotlinHintsProvider : HintsProvider {

    companion object {

        private fun listOfIntentions(parserResult: KotlinParserResult,
                                     psi: PsiElement) = listOf(
                RemoveExplicitTypeIntention(parserResult, psi),
                SpecifyTypeIntention(parserResult, psi),
                ConvertToBlockBodyIntention(parserResult, psi),
                ChangeReturnTypeIntention(parserResult, psi),
                ConvertTryFinallyToUseCallIntention(parserResult, psi),
                ConvertForEachToForLoopIntention(parserResult, psi),
                ConvertEnumToSealedClassIntention(parserResult, psi),
                AddValToConstructorParameterIntention(parserResult, psi),
                ConvertPropertyInitializerToGetterIntention(parserResult, psi),
                ConvertToConcatenatedStringIntention(parserResult, psi),
                ConvertToStringTemplateIntention(parserResult, psi),
                ConvertTwoComparisonsToRangeCheckIntention(parserResult, psi),
                MergeIfsIntention(parserResult, psi),
                RemoveBracesIntention(parserResult, psi),
                SplitIfIntention(parserResult, psi),
                ToInfixIntention(parserResult, psi),
                RemoveEmptyClassBodyIntention(parserResult, psi),
                RemoveEmptyParenthesesFromLambdaCallIntention(parserResult, psi),
                RemoveEmptyPrimaryConstructorIntention(parserResult, psi),
                RemoveEmptySecondaryConstructorIntention(parserResult, psi)
        ).filter { it.isApplicable(psi.textRange.startOffset) }

        private fun getHints(ruleContext: RuleContext) =
                ruleContext.parserResult.diagnostics
                        .filterIsInstance(KotlinError::class.java)
                        .map { it.createHint(ruleContext.parserResult as KotlinParserResult) }
                        .filterNotNull()

        private fun KotlinError.createHint(parserResult: KotlinParserResult) =
                when (diagnostic.factory) {
                    Errors.UNRESOLVED_REFERENCE -> createHintForUnresolvedReference(parserResult)
                    Errors.ABSTRACT_MEMBER_NOT_IMPLEMENTED,
                    Errors.ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED -> createImplementMembersHint(parserResult)
                    Errors.USELESS_ELVIS,
                    Errors.USELESS_ELVIS_ON_LAMBDA_EXPRESSION,
                    Errors.USELESS_ELVIS_RIGHT_IS_NULL -> createRemoveUselessElvisFix(parserResult)
                    else -> null
                }

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
        val ktFile = (ruleContext.parserResult as KotlinParserResult).ktFile
        val hintsComputer = KotlinHintsComputer(ruleContext.parserResult as KotlinParserResult)

        ktFile.accept(hintsComputer)

        hints.addAll(getHints(ruleContext))
        hints.addAll(hintsComputer.hints)
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