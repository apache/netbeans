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
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class ToInfixIntention(doc: Document,
                       analysisResult: AnalysisResult?,
                       psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {
    
    private var expression: KtCallExpression? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtCallExpression::class.java) ?: return false
        val element = expression ?: return false
        
        val calleeExpr = element.calleeExpression as? KtNameReferenceExpression ?: return false
        if (!calleeExpr.textRange.containsOffset(caretOffset)) return false

        val dotQualified = element.getQualifiedExpressionForSelector() ?: return false

        if (element.typeArgumentList != null) return false

        val argument = element.valueArguments.singleOrNull() ?: return false
        if (argument.isNamed()) return false
        if (argument.getArgumentExpression() == null) return false

        val bindingContext = analysisResult?.bindingContext ?: return false
        val resolvedCall = element.getResolvedCall(bindingContext) ?: return false
        val function = resolvedCall.resultingDescriptor as? FunctionDescriptor ?: return false
        if (!function.isInfix) return false

        // check that receiver has type to filter out calls with package/java class qualifier
        if (bindingContext.getType(dotQualified.receiverExpression) == null) return false

        return true
    }

    override fun getDescription() = "Replace with infix function call"

    override fun implement() {
        val element = expression ?: return
        
        val dotQualified = element.parent as KtDotQualifiedExpression
        val receiver = dotQualified.receiverExpression
        val argument = element.valueArguments.single().getArgumentExpression()!!
        val name = element.calleeExpression!!.text
        
        val newText = "${receiver.text} $name ${argument.text}"
        
        val startOffset = receiver.textRange.startOffset
        val lengthToDelete = element.textLength + receiver.textLength + 1

        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, newText, null)
        }
    }
}