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
package org.jetbrains.kotlin.completion

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiType
import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.filters.position.PositionElementFilter

class TextFilter(val value: String) : ElementFilter {
    override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
        if (element == null) return false
        return getTextByElement(element) == value
    }

    override fun isClassAcceptable(hintClass: Class<*>): Boolean = true
    
    private fun getTextByElement(element: Any): String? {
        return when (element) {
            is PsiType -> element.presentableText
            is PsiNamedElement -> element.name
            is PsiElement -> element.text
            else -> null
        }
    }
}

class LeftNeighbour(filter: ElementFilter) : PositionElementFilter() {
    init {
        setFilter(filter)
    }
    
    override fun isAcceptable(element: Any?, context: PsiElement?): Boolean {
        if (element !is PsiElement) return false
        
        val previous = element.searchNonSpaceNonCommentBack()
        return if (previous != null) getFilter().isAcceptable(previous, context) else false
    }
}