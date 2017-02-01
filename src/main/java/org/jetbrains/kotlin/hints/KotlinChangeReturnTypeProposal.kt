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

import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.idea.core.quickfix.QuickFixUtil
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers
import org.jetbrains.kotlin.idea.util.approximateWithResolvableType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.getTargetFunction
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import org.jetbrains.kotlin.descriptors.ClassDescriptorWithResolutionScopes
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf

class KotlinChangeReturnTypeProposal(val parserResult: KotlinParserResult,
                                     val psi: PsiElement) : ApplicableFix {

    private lateinit var function: KtFunction
    private lateinit var type: KotlinType

    private val activeDiagnostics = listOf(
            Errors.CONSTANT_EXPECTED_TYPE_MISMATCH,
            Errors.TYPE_MISMATCH,
            Errors.NULL_FOR_NONNULL_TYPE,
            Errors.TYPE_INFERENCE_EXPECTED_TYPE_MISMATCH)

    override fun isApplicable(caretOffset: Int): Boolean {
        val bindingContext = parserResult.analysisResult?.analysisResult?.bindingContext ?: return false
        val activeDiagnostic = getActiveDiagnostic(psi.textOffset, bindingContext.diagnostics) ?: return false
        val expression: KtExpression = PsiTreeUtil.getNonStrictParentOfType(activeDiagnostic.psiElement, KtExpression::class.java) ?: return false
        
        val expressionType = when (activeDiagnostic.factory) {
            Errors.TYPE_MISMATCH -> {
                val diagnosticWithParameters = Errors.TYPE_MISMATCH.cast(activeDiagnostic)
                diagnosticWithParameters.getB()
            }
            
            Errors.NULL_FOR_NONNULL_TYPE -> {
                val diagnosticWithParameters = Errors.NULL_FOR_NONNULL_TYPE.cast(activeDiagnostic)
                val expectedType = diagnosticWithParameters.getA()
                expectedType.makeNullable()
            }
            
            Errors.CONSTANT_EXPECTED_TYPE_MISMATCH -> bindingContext.getType(expression)
            
            Errors.TYPE_INFERENCE_EXPECTED_TYPE_MISMATCH -> {
                val diagnosticWithParameters = Errors.TYPE_INFERENCE_EXPECTED_TYPE_MISMATCH.cast(activeDiagnostic)
                diagnosticWithParameters.getB()
            }
            
            else -> null
        } ?: return false
        
        val expressionParent = expression.parent
        val ktFunction = if (expressionParent is KtReturnExpression) {
            expressionParent.getTargetFunction(bindingContext)
        } else {
            PsiTreeUtil.getParentOfType(expression, KtFunction::class.java, true)
        }
        
        if (ktFunction !is KtFunction) return false
        
        return when {
            QuickFixUtil.canFunctionOrGetterReturnExpression(ktFunction, expression) -> {
                val scope = ktFunction.getResolutionScope(bindingContext)
                type = expressionType.approximateWithResolvableType(scope, false)
                function = ktFunction
                true
            }
            
            expression is KtCallExpression -> {
                type = expressionType
                function = ktFunction
                true
            }
            
            else -> false
        }
    }

    private fun getActiveDiagnostic(offset: Int, diagnostics: Diagnostics): Diagnostic? {
        return diagnostics.find { diagnostic ->
            if (diagnostic.textRanges.isEmpty()) return@find false

            val range = diagnostic.textRanges.first()
            range.startOffset <= offset && offset <= range.endOffset && diagnostic.factory in activeDiagnostics
        }
    }

    override fun getDescription(): String {
        val functionName = function.name
        val renderedType = IdeDescriptorRenderers.SOURCE_CODE_SHORT_NAMES_IN_TYPES.renderType(type)
        return if (functionName != null) {
            "Change '$functionName' function return type to '$renderedType'"
        } else {
            "Change function return type to '$renderedType'"
        }
    }
    
    override fun isSafe() = true
    override fun isInteractive() = false
    
    override fun implement() {
        val oldTypeRef = function.typeReference
        val renderedType = IdeDescriptorRenderers.SOURCE_CODE_SHORT_NAMES_IN_TYPES.renderType(type)
        val doc = parserResult.snapshot.source.getDocument(false)
        if (oldTypeRef != null) {
            val startOffset = oldTypeRef.textRange.startOffset
            val endOffset = oldTypeRef.textRange.endOffset
            doc.atomicChange {
                remove(startOffset, endOffset - startOffset)
                insertString(startOffset, renderedType, null)
            }
        } else {
            val anchor = function.valueParameterList
            if (anchor != null) {
                doc.insertString(anchor.textRange.endOffset, ": $renderedType", null)
            }
        }
    }
    
}

// from idea/idea-core/src/org/jetbrains/kotlin/idea/core/Utils.kt but without the second parameter
public fun PsiElement.getResolutionScope(bindingContext: BindingContext): LexicalScope {
    for (parent in parentsWithSelf) {
        if (parent is KtElement) {
            val scope = bindingContext[BindingContext.LEXICAL_SCOPE, parent]
            if (scope != null) return scope
        }

        if (parent is KtClassBody) {
            val classDescriptor = bindingContext[BindingContext.CLASS, parent.getParent()] as? ClassDescriptorWithResolutionScopes
            if (classDescriptor != null) {
                return classDescriptor.getScopeForMemberDeclarationResolution()
            }
        }
    }
    error("Not in JetFile")
}