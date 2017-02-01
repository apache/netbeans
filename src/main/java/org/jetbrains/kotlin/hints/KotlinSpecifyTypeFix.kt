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
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.ErrorUtils
import org.jetbrains.kotlin.types.KotlinType
import org.netbeans.modules.csl.api.HintFix
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers

class KotlinSpecifyTypeFix(val parserResult: KotlinParserResult, 
                           val psi: PsiElement) : ApplicableFix {

    private lateinit var displayString: String

    override fun isApplicable(caretOffset: Int): Boolean {
        val element = psi.getNonStrictParentOfType(KtCallableDeclaration::class.java) ?: return false
        
        if (element.containingFile is KtCodeFragment) return false
        if (element is KtFunctionLiteral) return false
        if (element is KtConstructor<*>) return false
        if (element.typeReference != null) return false

        val initializer = (element as? KtDeclarationWithInitializer)?.initializer
        if (initializer != null && initializer.textRange.containsOffset(caretOffset)) return false

        if (element is KtNamedFunction && element.hasBlockBody()) return false

        if (getTypeForDeclaration(element, parserResult).isError) return false

        displayString = if (element is KtFunction) "Specify return type explicitly" else "Specify type explicitly"

        return true
    }

    private fun getTypeForDeclaration(declaration: KtCallableDeclaration, parserResult: KotlinParserResult): KotlinType {
        val bindingContext = parserResult.analysisResult?.analysisResult?.bindingContext ?: return ErrorUtils.createErrorType("null type")

        val descriptor = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]
        val type = (descriptor as? CallableDescriptor)?.returnType
        return type ?: ErrorUtils.createErrorType("null type")
    }

    override fun getDescription() = displayString
    override fun isSafe() = true
    override fun isInteractive() = false

    override fun implement() {
        val element = psi.getNonStrictParentOfType(KtCallableDeclaration::class.java) ?: return
        val type = getTypeForDeclaration(element, parserResult)
        val anchor = getAnchor(element) ?: return

        addTypeAnnotation(anchor, type)
    }

    private fun addTypeAnnotation(element: PsiElement, type: KotlinType) {
        val text = ": ${IdeDescriptorRenderers.SOURCE_CODE_SHORT_NAMES_IN_TYPES.renderType(type)}"
        val doc = parserResult.snapshot.source.getDocument(false)

        doc.insertString(element.textRange.endOffset, text, null)
    }
}