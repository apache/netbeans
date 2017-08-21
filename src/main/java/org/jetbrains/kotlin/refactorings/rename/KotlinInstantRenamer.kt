/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.kotlin.refactorings.rename

import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.highlighter.occurrences.getKotlinElements
import org.jetbrains.kotlin.highlighter.occurrences.getSearchingElements
import org.jetbrains.kotlin.highlighter.occurrences.search
import org.jetbrains.kotlin.navigation.references.resolveToSourceDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.modules.csl.api.InstantRenamer
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.spi.ParserResult

class KotlinInstantRenamer : InstantRenamer {
    
    private var occurrencesRanges: Set<OffsetRange>? = null
    
    override fun getRenameRegions(info: ParserResult, caretOffset: Int) = occurrencesRanges ?: emptySet()

    override fun isRenameAllowed(info: ParserResult, caretOffset: Int, explanationRetValue: Array<String>?): Boolean {
        val fo = info.snapshot.source.fileObject
        
        val ktFile = KotlinPsiManager.getParsedFile(fo) ?: return false
        val psi = ktFile.findElementAt(caretOffset) ?: return false
        val ktElement: KtElement = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement::class.java) ?: return false
        
        val sourceElements = ktElement.resolveToSourceDeclaration()
        if (sourceElements.isEmpty()) return false

        val searchingElements = getSearchingElements(sourceElements)
        val searchKtElements = getKotlinElements(searchingElements)
    
        if (searchKtElements.isEmpty()) return false
        val searchingElement = searchKtElements.first()
        
        if (psi.text.contains(" ") || psi.text == "interface"
                || psi.text == "class" || psi.text == "fun" 
                || psi.text == "package") return false
        
        if (searchingElement.useScope is LocalSearchScope) {
            occurrencesRanges = search(searchingElements, psi.containingFile as KtFile).toSet()

            return true
        }
        
        return false
    }
    
}