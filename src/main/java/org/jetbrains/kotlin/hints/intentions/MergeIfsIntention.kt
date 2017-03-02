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
import javax.swing.text.Document
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class MergeIfsIntention(doc: Document,
                        analysisResult: AnalysisResult?,
                        psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {
    
    private var expression: KtIfExpression? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtIfExpression::class.java) ?: return false
        val element = expression ?: return false
        
        if (element.`else` != null) return false
        val then = element.then ?: return false

        val nestedIf = then.nestedIf() ?: return false
        if (nestedIf.`else` != null) return false

        return true
    }

    override fun getDescription() = "Merge 'if's"

    override fun implement() {
        val element = expression ?: return
        
        val nestedIf = element.then?.nestedIf() ?: return
        val condition = element.condition ?: return
        val secondCondition = nestedIf.condition ?: return
        val nestedBody = nestedIf.then ?: return
        
        val text = "if (${condition.text} && ${secondCondition.text})  ${nestedBody.text} "
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, text, null)
            format(this, element.textRange.startOffset)
        }
    }
    
    private fun KtExpression.nestedIf() = when (this) {
            is KtBlockExpression -> this.statements.singleOrNull() as? KtIfExpression
            is KtIfExpression -> this
            else -> null
    }
    
}