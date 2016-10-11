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
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format

class KotlinConvertToBlockBodyFix(val parserResult: KotlinParserResult,
                                 val psi: PsiElement) : ApplicableFix {

    override fun isApplicable(caretOffset: Int): Boolean {
        val declaration = PsiTreeUtil.getParentOfType(psi, KtDeclarationWithBody::class.java) ?: return false
        if (declaration is KtFunctionLiteral ||
                declaration.hasBlockBody() ||
                !declaration.hasBody()) return false

        when (declaration) {
            is KtNamedFunction -> {
                val bindingContext = parserResult.analysisResult.analysisResult.bindingContext
                val returnType: KotlinType = declaration.returnType(bindingContext) ?: return false

                // do not convert when type is implicit and unknown
                if (!declaration.hasDeclaredReturnType() && returnType.isError) return false

                return true
            }

            is KtPropertyAccessor -> return true

            else -> error("Unknown declaration type: $declaration")
        }
    }

    override fun getDescription() = "Convert to block body"
    override fun isSafe() = true
    override fun isInteractive() = false

    override fun implement() {
        val declaration = PsiTreeUtil.getParentOfType(psi, KtDeclarationWithBody::class.java)!!
        val context = parserResult.analysisResult.analysisResult.bindingContext

        val shouldSpecifyType = declaration is KtNamedFunction
                && !declaration.hasDeclaredReturnType()
                && !KotlinBuiltIns.isUnit(declaration.returnType(context)!!)

        val factory = KtPsiFactory(declaration)
        
        replaceBody(declaration, factory, context)
        
        if (shouldSpecifyType) specifyType(declaration, factory, context)
    }

    private fun convert(declaration: KtDeclarationWithBody, bindingContext: BindingContext, factory: KtPsiFactory): KtExpression {
        val body = declaration.getBodyExpression()!!

        fun generateBody(returnsValue: Boolean): KtExpression {
            val bodyType = bindingContext.getType(body)
            val needReturn = returnsValue &&
                    (bodyType == null || (!KotlinBuiltIns.isUnit(bodyType) && !KotlinBuiltIns.isNothing(bodyType)))

            val expression = factory.createExpression(body.getText())
            val block: KtBlockExpression = if (needReturn) {
                factory.createBlock("return xyz")
            } else {
                return factory.createBlock(expression.getText())
            }
            val returnExpression = PsiTreeUtil.getChildOfType(block, KtReturnExpression::class.java)
            val returned = returnExpression?.getReturnedExpression() ?: return factory.createBlock("return ${expression.getText()}")
            if (KtPsiUtil.areParenthesesNecessary(expression, returned, returnExpression!!)) {
                return factory.createBlock("return (${expression.getText()})")
            }
            return factory.createBlock("return ${expression.getText()}")
        }

        val newBody = when (declaration) {
            is KtNamedFunction -> {
                val returnType = declaration.returnType(bindingContext)!!
                generateBody(!KotlinBuiltIns.isUnit(returnType) && !KotlinBuiltIns.isNothing(returnType))
            }

            is KtPropertyAccessor -> generateBody(declaration.isGetter())

            else -> throw RuntimeException("Unknown declaration type: $declaration")
        }
        return newBody
    }

    private fun replaceBody(declaration: KtDeclarationWithBody, factory: KtPsiFactory, context: BindingContext) {
        val newBody = convert(declaration, context, factory)
        var newBodyText = newBody.getNode().text

        val anchorToken = declaration.getEqualsToken()
        if (anchorToken!!.getNextSibling() !is PsiWhiteSpace) {
            newBodyText = factory.createWhiteSpace().getText() + newBodyText
        }

        val startOffset = anchorToken.textRange.startOffset
        val endOffset = declaration.getBodyExpression()!!.textRange.endOffset
        val doc = parserResult.snapshot.source.getDocument(false)
        
        doc.remove(startOffset, endOffset - startOffset)
        doc.insertString(startOffset, newBodyText, null)
        
        format(parserResult.snapshot.source.getDocument(false), declaration.textRange.endOffset)
    }

    private fun specifyType(declaration: KtDeclarationWithBody, factory: KtPsiFactory, context: BindingContext) {
        val returnType = (declaration as KtNamedFunction).returnType(context).toString()
        val stringToInsert = listOf(factory.createColon(), factory.createWhiteSpace())
                .joinToString(separator = "") { it.getText() } + returnType
        val doc = parserResult.snapshot.source.getDocument(false)
        doc.insertString(declaration.getValueParameterList()!!.textRange.endOffset, stringToInsert, null)
    }
    
}

private fun KtNamedFunction.returnType(context: BindingContext): KotlinType? {
    val descriptor = context[BindingContext.DECLARATION_TO_DESCRIPTOR, this] ?: return null
    return (descriptor as FunctionDescriptor).getReturnType()
}