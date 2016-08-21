package org.jetbrains.kotlin.highlighter.occurrences

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.isImportDirectiveExpression
import org.jetbrains.kotlin.descriptors.SourceElement


interface SearchFilter {
    fun isApplicable(jetElement: KtElement): Boolean
}

interface SearchFilterAfterResolve {
    fun isApplicable(sourceElement: KtElement, originElement: KtElement): Boolean
    
    fun isApplicable(sourceElements: List<SourceElement>, originElement: KtElement): Boolean {
        val kotlinElements = getKotlinElements(sourceElements)
        return  kotlinElements.any { isApplicable(it, originElement) }
    }
}

fun getKotlinElements(sourceElements: List<SourceElement>): List<KtElement> {
    return sourceElements
            .filterIsInstance(KotlinSourceElement::class.java)
            .map { it.psi }
}

fun getBeforeResolveFilters(): List<SearchFilter> {
    val filters = arrayListOf<SearchFilter>()
    filters.add(NonImportFilter())
    filters.add(ReferenceFilter())
    
    return filters
}

fun getAfterResolveFilters(): List<SearchFilterAfterResolve> = listOf(ResolvedReferenceFilter())

class ReferenceFilter : SearchFilter {
    override fun isApplicable(jetElement: KtElement): Boolean = jetElement is KtReferenceExpression
}

class NonImportFilter : SearchFilter {
    override fun isApplicable(jetElement: KtElement): Boolean {
        return jetElement !is KtSimpleNameExpression || !jetElement.isImportDirectiveExpression()
    }
}

class ResolvedReferenceFilter : SearchFilterAfterResolve {
    override fun isApplicable(sourceElement: KtElement, originElement: KtElement): Boolean {
        return sourceElement == originElement
    }
}