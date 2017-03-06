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
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.analyzer.AnalysisResult
import javax.swing.text.Document
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class SplitIfIntention(doc: Document,
                       analysisResult: AnalysisResult?,
                       psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {

    private var expression: KtExpression? = null

    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtExpression::class.java) ?: return false
        val element = expression ?: return false

        return when (element) {
            is KtOperationReferenceExpression -> isOperatorValid(element)
            is KtIfExpression -> getFirstValidOperator(element) != null && element.ifKeyword.textRange.containsOffset(caretOffset)
            else -> false
        }
    }

    override fun getDescription() = "Split if into 2 if's"

    override fun implement() {
        val element = expression ?: return

        val operator = when (element) {
            is KtIfExpression -> getFirstValidOperator(element)!!
            else -> element as KtOperationReferenceExpression
        }

        val ifExpression = operator.getNonStrictParentOfType(KtIfExpression::class.java) ?: return

        val expression = operator.parent as KtBinaryExpression
        val rightExpression = KtPsiUtil.safeDeparenthesize(getRight(expression, ifExpression.condition!!))
        val leftExpression = KtPsiUtil.safeDeparenthesize(expression.left!!)
        val thenBranch = ifExpression.then!!
        val elseBranch = ifExpression.`else`

        val newIf = with(StringBuilder()) {
            when (operator.getReferencedNameElementType()) {
                KtTokens.ANDAND -> {

                    append("if (").append(leftExpression.text).append(") {\n")
                    append("if (").append(rightExpression.text).append(") ")
                    append(thenBranch.text).append("\n}")
                    if (elseBranch != null) {
                        append("else ").append(elseBranch.text)
                    }
                    toString()
                }
                KtTokens.OROR -> {
                    append("if (").append(leftExpression.text).append(")").append(thenBranch.text)
                    append("else if (").append(rightExpression.text).append(") ")
                    append(thenBranch.text)
                    if (elseBranch != null) {
                        append("else ").append(elseBranch.text)
                    }
                    toString()
                }
                else -> throw IllegalArgumentException()
            }
        }

        val startOffset = ifExpression.textRange.startOffset
        val lengthToDelete = ifExpression.textLength

        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, newIf, null)
            format(this, element.textRange.startOffset)
        }
    }

    private fun getRight(element: KtBinaryExpression, condition: KtExpression): KtExpression {
        val conditionRange = condition.textRange
        val startOffset = element.right!!.startOffset - conditionRange.startOffset
        val endOffset = conditionRange.length
        val rightString = condition.text.substring(startOffset, endOffset)

        val expression = KtPsiFactory(element).createExpression(rightString)

        return expression
    }

    private fun getFirstValidOperator(element: KtIfExpression): KtOperationReferenceExpression? {
        val condition = element.condition ?: return null
        val children: Collection<KtOperationReferenceExpression> = PsiTreeUtil.findChildrenOfType(condition, KtOperationReferenceExpression::class.java)

        return children.firstOrNull { isOperatorValid(it) }
    }

    private fun isOperatorValid(element: KtOperationReferenceExpression): Boolean {
        val operator = element.getReferencedNameElementType()
        if (operator != KtTokens.ANDAND && operator != KtTokens.OROR) return false

        var expression = element.parent as? KtBinaryExpression ?: return false

        if (expression.right == null || expression.left == null) return false

        while (true) {
            expression = expression.parent as? KtBinaryExpression ?: break
            if (expression.operationToken != operator) return false
        }

        val ifExpression = expression.parent?.parent as? KtIfExpression ?: return false

        if (ifExpression.condition == null) return false
        if (!PsiTreeUtil.isAncestor(ifExpression.condition, element, false)) return false

        if (ifExpression.then == null) return false

        return true
    }

}