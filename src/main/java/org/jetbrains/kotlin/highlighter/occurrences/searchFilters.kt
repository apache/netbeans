package org.jetbrains.kotlin.highlighter.occurrences

import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.psiUtil.isImportDirectiveExpression
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.psi.KtNamedDeclaration

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

fun KtElement.filterBeforeResolve() = beforeResolveFilters().none { !it.isApplicable(this) }

fun List<SourceElement>.filterAfterResolve(origin: KtElement) = afterResolveFilters().none { !it.isApplicable(this, origin) }

private fun beforeResolveFilters() = listOf(NonImportFilter(), ReferenceFilter())

private fun afterResolveFilters() = listOf(ResolvedReferenceFilter())

class ReferenceFilter : SearchFilter {
    override fun isApplicable(jetElement: KtElement): Boolean = jetElement is KtReferenceExpression || jetElement is KtNamedDeclaration
}

class NonImportFilter : SearchFilter {
    override fun isApplicable(jetElement: KtElement): Boolean {
        return jetElement !is KtSimpleNameExpression || !jetElement.isImportDirectiveExpression()
    }
}

class ResolvedReferenceFilter : SearchFilterAfterResolve {
    override fun isApplicable(sourceElement: KtElement, originElement: KtElement) = sourceElement == originElement
}