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
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.contentRange
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitReceiver
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class ConvertTryFinallyToUseCallIntention(val parserResult: KotlinParserResult,
                                          val psi: PsiElement) : ApplicableIntention {
    
    private var tryExpression: KtTryExpression? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        tryExpression = psi.getNonStrictParentOfType(KtTryExpression::class.java) ?: return false
        val element = tryExpression ?: return false
        
        val finallySection = element.finallyBlock ?: return false
        val finallyExpression = finallySection.finalExpression.statements.singleOrNull() ?: return false
        if (element.catchClauses.isNotEmpty()) return false

        val context = parserResult.analysisResult?.analysisResult?.bindingContext ?: return false
        val resolvedCall = finallyExpression.getResolvedCall(context) ?: return false
        if (resolvedCall.candidateDescriptor.name.asString() != "close") return false
        if (resolvedCall.extensionReceiver != null) return false
        val receiver = resolvedCall.dispatchReceiver ?: return false
        if (receiver.type.supertypes().all {
            it.constructor.declarationDescriptor?.fqNameSafe?.asString().let {
                it != "java.io.Closeable" && it != "java.lang.AutoCloseable"
            }
        }) return false

        when (receiver) {
            is ExpressionReceiver -> {
                val expression = receiver.expression
                if (expression !is KtThisExpression) {
                    val resourceReference = expression as? KtReferenceExpression ?: return false
                    val resourceDescriptor =
                            context[BindingContext.REFERENCE_TARGET, resourceReference] as? VariableDescriptor ?: return false
                    if (resourceDescriptor.isVar) return false
                }
            }
            is ImplicitReceiver -> {}
            else -> return false
}
        
        return true
    }

    override fun getDescription() = "Convert try-finally to .use()"

    override fun implement() {
        val element = tryExpression ?: return
        
        val finallySection = element.finallyBlock!!
        val finallyExpression = finallySection.finalExpression.statements.single()
        val finallyExpressionReceiver = (finallyExpression as? KtQualifiedExpression)?.receiverExpression
        val resourceReference = finallyExpressionReceiver as? KtNameReferenceExpression
        val resourceName = resourceReference?.getReferencedNameAsName()
        
        val useExpression = StringBuilder()
        
        with (useExpression) {
            if (resourceName != null) {
                append(resourceName).append(".")
            } else if (finallyExpressionReceiver is KtThisExpression) {
                append(finallyExpressionReceiver.text).append(".")
            }

            append("use {")

            if (resourceName != null) {
                append(resourceName).append("->")
            }
            append("\n")
            
            element.tryBlock.contentRange().forEach { append(it.text).append("\n") }
            
            append("}")
        }
        
        val doc = parserResult.snapshot.source.getDocument(false)
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.atomicChange { 
            remove(startOffset, lengthToDelete)
            insertString(startOffset, useExpression.toString(), null)
            format(this, psi.textRange.startOffset)
        }
    }
}