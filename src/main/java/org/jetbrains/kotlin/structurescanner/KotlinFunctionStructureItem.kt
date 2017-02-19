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

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.ImageIcon
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.netbeans.modules.csl.api.ElementHandle
import org.netbeans.modules.csl.api.ElementKind
import org.netbeans.modules.csl.api.HtmlFormatter
import org.netbeans.modules.csl.api.Modifier
import org.netbeans.modules.csl.api.StructureItem
import org.openide.util.ImageUtilities

class KotlinFunctionStructureItem(private val function: KtNamedFunction,
                                  private val isLeaf: Boolean,
                                  private val context: BindingContext) : StructureItem {

    override fun getName(): String {
        val name = function.name
        val valueParameters = function.valueParameters.joinToString(prefix = "(", postfix = ")") { it.text }
        
        val receiver = function.receiverTypeReference?.text?.let { "$it." } ?: ""
        
        val returnType = (context[BindingContext.DECLARATION_TO_DESCRIPTOR, function] as? SimpleFunctionDescriptor)
                ?.let { it.returnType
                        ?.takeIf { it.toString() != "Unit" }
                        ?.let { ": $it" } 
                }
            ?: function.colon?.let {
                val sibling = PsiTreeUtil.skipSiblingsForward(it, PsiWhiteSpace::class.java, PsiComment::class.java)
                if (sibling != null) {
                    ": ${sibling.text}"
                } else ""
            } ?: ""
        
        return "$receiver$name$valueParameters$returnType"
    }

    override fun getSortText() = function.name
    override fun getHtml(formatter: HtmlFormatter) = name
    override fun getElementHandle() = null
    override fun getKind() = ElementKind.METHOD
    override fun getModifiers() = emptySet<Modifier>()
    override fun isLeaf() = isLeaf
    override fun getNestedItems() = listOf<StructureItem>()
    override fun getPosition() = function.textRange.startOffset.toLong()
    override fun getEndPosition() = function.textRange.endOffset.toLong()
    override fun getCustomIcon() = ImageIcon(ImageUtilities.loadImage("org/jetbrains/kotlin/completionIcons/method.png"))

}