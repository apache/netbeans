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
package org.jetbrains.kotlin.structurescanner

import javax.swing.ImageIcon
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.netbeans.modules.csl.api.ElementKind
import org.netbeans.modules.csl.api.HtmlFormatter
import org.netbeans.modules.csl.api.Modifier
import org.netbeans.modules.csl.api.StructureItem
import org.openide.util.ImageUtilities

class KotlinClassStructureItem(private val psiElement: KtClassOrObject,
                               private val isLeaf: Boolean,
                               private val context: BindingContext) : StructureItem {

    override fun getName(): String {
        val className = psiElement.name
        val superTypes = psiElement.superTypeListEntries.let {
            if (it.isNotEmpty()) it.joinToString(prefix = "::") { it.text } else ""
        }

        return "$className$superTypes"
    }

    override fun getSortText() = psiElement.name
    override fun getHtml(formatter: HtmlFormatter) = name
    override fun getElementHandle() = null
    override fun getKind() = ElementKind.CLASS
    override fun getModifiers() = emptySet<Modifier>()
    override fun isLeaf() = isLeaf
    override fun getPosition() = psiElement.textRange.startOffset.toLong()
    override fun getEndPosition() = psiElement.textRange.endOffset.toLong()
    override fun getCustomIcon() = ImageIcon(ImageUtilities.loadImage("org/jetbrains/kotlin/completionIcons/class.png"))

    override fun getNestedItems() = psiElement.declarations.mapNotNull {
        when (it) {
            is KtClass -> KotlinClassStructureItem(it, true, context)
            is KtNamedFunction -> KotlinFunctionStructureItem(it, true, context)
            is KtProperty -> KotlinPropertyStructureItem(it, true, context)
            else -> null
        }
    }

}