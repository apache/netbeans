/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.text.Document
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.reformatting.moveCursorTo
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class ConvertToExpressionBodyIntention(doc: Document,
                                       analysisResult: AnalysisResult?,
                                       psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {

    override fun isApplicable(caretOffset: Int): Boolean {
        val element: KtDeclarationWithBody = PsiTreeUtil.getParentOfType(psi, KtDeclarationWithBody::class.java) ?: return false
        if (element is KtConstructor<*>) return false
        val value = calcValue(element) ?: return false
        
        return !value.anyDescendantOfType<KtReturnExpression>(
                canGoInside = { it !is KtFunctionLiteral && it !is KtNamedFunction && it !is KtPropertyAccessor }
)
    }

    override fun getDescription() = "Convert to expression body"

    override fun implement() {
        val element: KtDeclarationWithBody = PsiTreeUtil.getParentOfType(psi, KtDeclarationWithBody::class.java) ?: return
        val expression = calcValue(element) ?: return
        val context = analysisResult?.bindingContext ?: return
        val block = element.blockExpression() ?: return
        
        if (element !is KtCallableDeclaration) return
        
        val colon = element.colon?.textRange?.startOffset ?: element.bodyExpression!!.textRange.startOffset
        
        val type = if (!element.hasDeclaredReturnType() 
                && element is KtNamedFunction 
                && block.statements.isNotEmpty()) {
            val valueType = context.getType(expression)
            if (valueType == null || KotlinBuiltIns.isUnit(valueType)) {
                KotlinBuiltIns.FQ_NAMES.unit.shortName().asString()
            } else return
        } else if (element.hasDeclaredReturnType()) {
            element.typeReference!!.text
        } else ""
        
        
        val startOffset = colon
        val endOffset = element.bodyExpression!!.textRange.endOffset    
        
        doc.atomicChange { 
            remove(startOffset, endOffset - startOffset)
            insertString(startOffset, ": $type = ${expression.text}", null)
            moveCursorTo(startOffset)
        }
    }
    
    private fun KtDeclarationWithBody.blockExpression() = when (this) {
        is KtFunctionLiteral -> null
        else -> {
            val body = bodyExpression
            if (!hasBlockBody() || body !is KtBlockExpression) null else body
        }
    }

    private fun calcValue(declaration: KtDeclarationWithBody): KtExpression? {
        val body = declaration.blockExpression() ?: return null
        return calcValue(body)
    }

    private fun calcValue(body: KtBlockExpression): KtExpression? {
        val bodyStatements = body.statements
        if (bodyStatements.isEmpty()) {
            return KtPsiFactory(body).createExpression("Unit")
        }
        val statement = bodyStatements.singleOrNull() ?: return null
        when (statement) {
            is KtReturnExpression -> {
                return statement.returnedExpression
            }

            //TODO: IMO this is not good code, there should be a way to detect that JetExpression does not have value
            is KtDeclaration, is KtLoopExpression -> return null // is JetExpression but does not have value

            else -> {
                if (statement is KtBinaryExpression && statement.operationToken in KtTokens.ALL_ASSIGNMENTS) return null // assignment does not have value

                val context = analysisResult?.bindingContext ?: return null
                val expressionType = context.getType(statement) ?: return null
                val isUnit = KotlinBuiltIns.isUnit(expressionType)
                if (!isUnit && !KotlinBuiltIns.isNothing(expressionType)) return null
                if (isUnit) {
                    if (statement.hasResultingIfWithoutElse()) {
                        return null
                    }
                    val resultingWhens = statement.resultingWhens()
                    if (resultingWhens.any { it.elseExpression == null && context.get(BindingContext.EXHAUSTIVE_WHEN, it) != true }) {
                        return null
                    }
                }
                return statement
            }
        }
    }
    
}

fun KtExpression?.hasResultingIfWithoutElse(): Boolean = when (this) {
    is KtIfExpression -> `else` == null || then.hasResultingIfWithoutElse() || `else`.hasResultingIfWithoutElse()
    is KtWhenExpression -> entries.any { it.expression.hasResultingIfWithoutElse() }
    is KtBinaryExpression -> left.hasResultingIfWithoutElse() || right.hasResultingIfWithoutElse()
    is KtUnaryExpression -> baseExpression.hasResultingIfWithoutElse()
    is KtBlockExpression -> statements.lastOrNull().hasResultingIfWithoutElse()
    else -> false
}

fun KtExpression.resultingWhens(): List<KtWhenExpression> = when (this) {
    is KtWhenExpression -> listOf(this) + entries.map { it.expression?.resultingWhens() ?: listOf() }.flatten()
    is KtIfExpression -> (then?.resultingWhens() ?: listOf()) + (`else`?.resultingWhens() ?: listOf())
    is KtBinaryExpression -> (left?.resultingWhens() ?: listOf()) + (right?.resultingWhens() ?: listOf())
    is KtUnaryExpression -> this.baseExpression?.resultingWhens() ?: listOf()
    is KtBlockExpression -> statements.lastOrNull()?.resultingWhens() ?: listOf()
    else -> listOf()
}

inline fun <reified T : PsiElement> PsiElement.anyDescendantOfType(crossinline canGoInside: (PsiElement) -> Boolean, noinline predicate: (T) -> Boolean = { true }): Boolean {
    return findDescendantOfType(canGoInside, predicate) != null
}

inline fun <reified T : PsiElement> PsiElement.findDescendantOfType(crossinline canGoInside: (PsiElement) -> Boolean, noinline predicate: (T) -> Boolean = { true }): T? {
    var result: T? = null
    this.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is T && predicate(element)) {
                result = element
                stopWalking()
                return
            }

            if (canGoInside(element)) {
                super.visitElement(element)
            }
        }
    })
    return result
}