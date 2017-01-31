/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.highlighter.occurrences

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import kotlin.Pair
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.navigation.references.*
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.netbeans.modules.csl.api.OffsetRange

private fun getLengthOfIdentifier(ktElement: KtElement?) = when {
    ktElement is KtNamedDeclaration -> {
        val nameIdentifier = ktElement.nameIdentifier
        if (nameIdentifier == null) null else {
            Pair(nameIdentifier.textRange.startOffset, nameIdentifier.textRange.endOffset)
        }
    }
    ktElement is KtReferenceExpression -> Pair(ktElement.textRange.startOffset, ktElement.textRange.endOffset)
    else -> null
}

fun search(searchElement: KtElement, ktFile: KtFile) = searchTextOccurrences(ktFile, searchElement)
        .mapNotNull { getLengthOfIdentifier(it) }
        .map { OffsetRange(it.first, it.second) }

fun search(searchingElements: List<SourceElement>, ktFile: KtFile): List<OffsetRange> {
    val searchElements = getKotlinElements(searchingElements)
    if (searchElements.isEmpty()) return emptyList()
    
    val searchElement = searchElements.first()
    val occurrences = searchTextOccurrences(ktFile, searchElement)
    
    return occurrences.map { getLengthOfIdentifier(it) }
            .filterNotNull()
            .map { OffsetRange(it.first, it.second) }
}

fun getKotlinElements(sourceElements: List<SourceElement>) = sourceElements
        .filterIsInstance(KotlinSourceElement::class.java)
        .map { it.psi }

fun searchTextOccurrences(ktFile: KtFile, sourceElement: KtElement): List<KtElement> {
    val elementsToReturn = arrayListOf<KtElement>()
    val elementName = sourceElement.name ?: return emptyList()
    
    val elements = getAllOccurrencesInFile(ktFile, elementName)
            .mapNotNull { PsiTreeUtil.getNonStrictParentOfType(it, KtElement::class.java) }
    
    val beforeResolveFilters = getBeforeResolveFilters()
    val afterResolveFilters = getAfterResolveFilters()
//    
//    for (element in elements) {
//        var beforeResolveCheck = true
//        for (filter in beforeResolveFilters) {
//            if (!filter.isApplicable(element)) {
//                beforeResolveCheck = false
//                break
//            }
//        }
//        if (!beforeResolveCheck) continue
//        
//        val sourceElements = element.resolveToSourceDeclaration()
//        if (sourceElements.isEmpty()) continue
//        
//        for (filter in afterResolveFilters) {
//            if (filter.isApplicable(sourceElements, sourceElement)) {
//                elementsToReturn.add(element)
//            }
//        }
//    }
    return elements//ToReturn
}

private fun getAllOccurrencesInFile(ktFile: KtFile, text: String): List<PsiElement> {
    val elements = arrayListOf<PsiElement>()
    val source = ktFile.text
    var start = 0
    while (true) {
        val index = source.indexOf(text, start)
        if (index == -1) break
        
        val psiElement = ktFile.findElementAt(index)
        if (psiElement != null) elements.add(psiElement)
        
        start = index + text.length
    }
    return elements
}

fun getSearchingElements(sourceElements: List<SourceElement>): List<SourceElement> {
    val classOrObjects = sourceElements.getContainingClassOrObjectForConstructor()
    return if (classOrObjects.isEmpty()) sourceElements else classOrObjects
}