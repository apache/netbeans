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
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format

class ConvertForEachToForLoopIntention(val parserResult: KotlinParserResult,
                                       val psi: PsiElement) : ApplicableFix {
    
    private val FOR_EACH_NAME = "forEach"
    private val FOR_EACH_FQ_NAMES = listOf("collections", "sequences", "text", "ranges").map { "kotlin.$it.$FOR_EACH_NAME" }.toSet()
    
    private var element: KtSimpleNameExpression? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        element = psi.getNonStrictParentOfType(KtSimpleNameExpression::class.java) ?: return false
        val expression = element ?: return false
        
        val data = extractData(expression) ?: return false
        if (data.functionLiteral.valueParameters.size > 1) return false
        if (data.functionLiteral.bodyExpression == null) return false

        return true
    }

    override fun isSafe() = true

    override fun isInteractive() = false

    override fun getDescription() = "Convert 'forEach' to for loop"

    override fun implement() {
        val expression = element ?: return
        
        val (expressionToReplace, receiver, functionLiteral) = extractData(expression)!!
        val loop = generateLoop(functionLiteral, receiver)
        
        val doc = parserResult.snapshot.source.getDocument(false)
        
        val startOffset = expressionToReplace.textRange.startOffset
        val lengthToDelete = expressionToReplace.textLength
        
        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, loop, null)
            format(this, psi.textRange.startOffset)
        }
    }
    
    private data class Data(
            val expressionToReplace: KtExpression,
            val receiver: KtExpression,
            val functionLiteral: KtLambdaExpression
    )
    
    private fun extractData(nameExpr: KtSimpleNameExpression): Data? {
        val parent = nameExpr.parent
        val expression = (when (parent) {
            is KtCallExpression -> parent.parent as? KtDotQualifiedExpression
            is KtBinaryExpression -> parent
            else -> null
        } ?: return null) as KtExpression

        val context = parserResult.analysisResult?.analysisResult?.bindingContext ?: return null
        
        val resolvedCall = expression.getResolvedCall(context) ?: return null
        if (DescriptorUtils.getFqName(resolvedCall.resultingDescriptor).toString() !in FOR_EACH_FQ_NAMES) return null

        val receiver = resolvedCall.call.explicitReceiver as? ExpressionReceiver ?: return null
        val argument = resolvedCall.call.valueArguments.singleOrNull() ?: return null
        val functionLiteral = argument.getArgumentExpression() as? KtLambdaExpression ?: return null
        
        return Data(expression, receiver.expression, functionLiteral)
    }
    
    private fun generateLoop(functionLiteral: KtLambdaExpression, receiver: KtExpression): String {
        val loopRange = KtPsiUtil.safeDeparenthesize(receiver)
        val body = functionLiteral.bodyExpression!!
        val parameter = functionLiteral.valueParameters.singleOrNull() ?: "it"
        
        return "for ($parameter in ${loopRange.text}) { ${body.text} }"
    }
    
}