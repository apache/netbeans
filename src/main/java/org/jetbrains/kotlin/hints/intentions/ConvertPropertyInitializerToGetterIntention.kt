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
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange


class ConvertPropertyInitializerToGetterIntention(val parserResult: KotlinParserResult,
                                                  val psi: PsiElement) : ApplicableIntention {

    private var property: KtProperty? = null

    override fun isApplicable(caretOffset: Int): Boolean {
        property = psi.getNonStrictParentOfType(KtProperty::class.java) ?: return false
        val element = property ?: return false

        val initializer = element.initializer
        if (initializer != null && element.getter == null
                && !element.isExtensionDeclaration()
                && !element.isLocal) return true
        else return false
    }

    override fun getDescription() = "Convert property initializer to getter"

    override fun implement() {
        val element = property ?: return
        val textWithGetter = getTextWithGetter(element)
        
        val doc = parserResult.snapshot.source.getDocument(false)
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, textWithGetter, null)
            format(this, psi.textRange.startOffset)
        }
    }

    private fun getTextWithGetter(element: KtProperty): String = with(StringBuilder()) {
        append(element.modifierList?.text ?: "")

        if (element.isVar) {
            append(" var ")
        } else append(" val ")

        append(element.name)

        val type = getTypeForDeclaration(element, parserResult)

        append(": ").append(type)

        val initializer = element.initializer!!
        append("\nget() = ").append(initializer.text)
        
        val setter = element.setter
        if (setter != null) {
            append("\n").append(setter.text)
        }

        return toString()
    }

}