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

import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import org.netbeans.modules.csl.api.HintFix
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class KotlinRemoveExplicitTypeFix(val parserResult: KotlinParserResult, 
                                  val psi: PsiElement) : ApplicableFix {

    override fun isApplicable(caretOffset: Int): Boolean {
        val element: KtCallableDeclaration = PsiTreeUtil.getNonStrictParentOfType(psi, KtCallableDeclaration::class.java) ?: return false
        
        if (element.containingFile is KtCodeFragment) return false
        if (element.typeReference == null) return false

        val initializer = (element as? KtDeclarationWithInitializer)?.initializer
        if (initializer != null && initializer.textRange.containsOffset(caretOffset)) return false

        return when (element) {
            is KtProperty -> initializer != null
            is KtNamedFunction -> !element.hasBlockBody() && initializer != null
            is KtParameter -> element.isLoopParameter
            else -> false
        }
    }

    override fun getDescription() = "Remove explicit type specification"
    override fun isSafe() = true
    override fun isInteractive() = false

    override fun implement() {
        val element: KtCallableDeclaration = PsiTreeUtil.getNonStrictParentOfType(psi, KtCallableDeclaration::class.java) ?: return
        val anchor = getAnchor(element) ?: return

        val doc = parserResult.snapshot.source.getDocument(false)
        val endOffset = anchor.textRange.endOffset
        val endOfType = element.typeReference!!.textRange.endOffset

        doc.remove(endOffset, endOfType - endOffset)
    }

}

fun getAnchor(element: KtCallableDeclaration): PsiElement? {
    return when (element) {
        is KtProperty, is KtParameter -> element.nameIdentifier
        is KtNamedFunction -> element.valueParameterList
        else -> null
    }
}
