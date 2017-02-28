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
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class ReplaceSizeCheckWithIsNotEmptyInspection(val parserResult: KotlinParserResult,
                                               override val element: KtElement) : Inspection(element) {
    override val description = "Use 'isNotEmpty' instead of size check"

    override fun isApplicable(): Boolean {
        if (element !is KtBinaryExpression) return false
        
        return element.isApplicable(parserResult)
    }
}

class ReplaceSizeCheckWithIsNotEmptyIntention(val parserResult: KotlinParserResult,
                                              val psi: PsiElement) : ApplicableIntention {

    private var expression: KtBinaryExpression? = null

    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtBinaryExpression::class.java) ?: return false
        val element = expression ?: return false

        return element.isApplicable(parserResult)
    }

    override fun getDescription() = "Replace size check with 'isNotEmpty'"

    override fun implement() {
        val element = expression ?: return

        val target = getTargetExpression(element)
        if (target !is KtDotQualifiedExpression) return

        val newText = "${target.receiverExpression.text}.isNotEmpty()"
        val doc = parserResult.snapshot.source.getDocument(false)

        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength

        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, newText, null)
        }

    }

}

fun KtElement?.isZero() = this?.text == "0"

fun KtElement?.isOne() = this?.text == "1"

private fun KtExpression.isExpressionOfTypeOrSubtype(context: BindingContext,
                                                     predicate: (KotlinType) -> Boolean): Boolean {
    val returnType = getResolvedCall(context)?.resultingDescriptor?.returnType
    return returnType != null && (returnType.constructor.supertypes + returnType).any(predicate)
}

fun KtElement?.isSizeOrLength(context: BindingContext): Boolean {
    if (this !is KtDotQualifiedExpression) return false

    return when (selectorExpression?.text) {
        "size" -> receiverExpression.isExpressionOfTypeOrSubtype(context) { type ->
            KotlinBuiltIns.isArray(type) ||
                    KotlinBuiltIns.isPrimitiveArray(type) ||
                    KotlinBuiltIns.isCollectionOrNullableCollection(type) ||
                    KotlinBuiltIns.isMapOrNullableMap(type)
        }
        "length" -> receiverExpression.isExpressionOfTypeOrSubtype(context, KotlinBuiltIns::isCharSequenceOrNullableCharSequence)
        else -> false
    }
}

private fun getTargetExpression(element: KtBinaryExpression): KtExpression? {
        return when (element.operationToken) {
            KtTokens.EXCLEQ -> when {
                element.right.isZero() -> element.left
                element.left.isZero() -> element.right
                else -> null
            }
            KtTokens.GT -> if (element.right.isZero()) element.left else null
            KtTokens.LT -> if (element.left.isZero()) element.right else null
            KtTokens.GTEQ -> if (element.right.isOne()) element.left else null
            KtTokens.LTEQ -> if (element.left.isOne()) element.right else null
            else -> null
        }
    }

private fun KtBinaryExpression.isApplicable(parserResult: KotlinParserResult): Boolean {
    val targetExpression = getTargetExpression(this) ?: return false
    val context = parserResult.analysisResult?.analysisResult?.bindingContext ?: return false

    return targetExpression.isSizeOrLength(context)
}