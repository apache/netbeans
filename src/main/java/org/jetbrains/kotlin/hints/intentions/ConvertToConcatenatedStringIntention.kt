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
package org.jetbrains.kotlin.hints.intentions

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analyzer.AnalysisResult
import javax.swing.text.Document
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange
import org.jetbrains.kotlin.reformatting.moveCursorTo

class ConvertToConcatenatedStringIntention(doc: Document,
                                           analysisResult: AnalysisResult?,
                                           psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {

    private var expression: KtStringTemplateExpression? = null

    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtStringTemplateExpression::class.java) ?: return false
        val element = expression ?: return false

        if (element.lastChild.node.elementType != KtTokens.CLOSING_QUOTE) return false
        return element.entries.any { it is KtStringTemplateEntryWithExpression }
    }

    override fun getDescription() = "Convert template to concatenated string"

    override fun implement() {
        val element = expression ?: return

        val tripleQuoted = isTripleQuoted(element.text!!)
        val quote = if (tripleQuoted) "\"\"\"" else "\""
        val entries = element.entries

        val text = entries
                .filterNot { it is KtStringTemplateEntryWithExpression && it.expression == null }
                .mapIndexed { index, entry ->
                    entry.toSeparateString(quote, convertExplicitly = (index == 0), isFinalEntry = (index == entries.lastIndex))
                }
                .joinToString(separator = "+")
                .replace("""$quote+$quote""", "")
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, text, null)
            moveCursorTo(element.textRange.startOffset)
        }
    }

    // copied from IDEA plugin
    
    private fun isTripleQuoted(str: String) = str.startsWith("\"\"\"") && str.endsWith("\"\"\"")

    private fun KtStringTemplateEntry.toSeparateString(quote: String, convertExplicitly: Boolean, isFinalEntry: Boolean): String {
        if (this !is KtStringTemplateEntryWithExpression) {
            return text.quote(quote)
        }

        val expression = expression!! // checked before

        val text = if (needsParenthesis(expression, isFinalEntry))
            "(${expression.text})"
        else
            expression.text

        return if (convertExplicitly && !expression.isStringExpression())
            "$text.toString()"
        else
            text
    }

    private fun needsParenthesis(expression: KtExpression, isFinalEntry: Boolean): Boolean {
        return when (expression) {
            is KtBinaryExpression -> true
            is KtIfExpression -> expression.`else` !is KtBlockExpression && !isFinalEntry
            else -> false
        }
    }

    private fun String.quote(quote: String) = "$quote${this}$quote"

    private fun KtExpression.isStringExpression(): Boolean {
        val context = analysisResult?.bindingContext ?: return false
        return KotlinBuiltIns.isString(context.getType(this))
    }
}