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

import org.netbeans.modules.csl.spi.DefaultCompletionProposal
import org.netbeans.modules.csl.api.ElementHandle
import org.netbeans.modules.csl.api.ElementKind
import org.netbeans.modules.csl.api.HtmlFormatter
import com.intellij.psi.PsiElement
import javax.swing.text.Document

class KeywordCompletionProposal(val keyword: String, 
                                val idenStartOffset: Int,
                                val prefix: String) : DefaultCompletionProposal(), InsertableProposal {
    override fun getElement() = null
    
    override fun getSortPrioOverride() = 5
    override fun getKind() = ElementKind.KEYWORD
    override fun getLhsHtml(formatter: HtmlFormatter) = keyword
    override fun getRhsHtml(formatter: HtmlFormatter) = null
    override fun getInsertPrefix() = keyword
    override fun getName() = keyword
    override fun getSortText() = keyword
    override fun getAnchorOffset() = idenStartOffset
    override fun doInsert(document: Document) {
        document.remove(idenStartOffset, prefix.length)
        document.insertString(idenStartOffset, keyword, null)
    }
}