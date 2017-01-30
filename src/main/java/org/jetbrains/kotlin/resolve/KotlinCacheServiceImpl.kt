package org.jetbrains.kotlin.resolve

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.caches.resolve.KotlinCacheService
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.analyzer.AnalysisResult
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.model.KotlinAnalysisFileCache
import org.jetbrains.kotlin.resolve.diagnostics.KotlinSuppressCache
import org.jetbrains.kotlin.container.ComponentProvider
import org.netbeans.api.project.Project as NBProject

class KotlinCacheServiceImpl(val ideaProject: Project, val project: NBProject) : KotlinCacheService {

    override fun getResolutionFacadeByFile(file: PsiFile, platform: TargetPlatform): ResolutionFacade {
        throw UnsupportedOperationException()
    }

    override fun getSuppressionCache(): KotlinSuppressCache {
        throw UnsupportedOperationException()
    }

    override fun getResolutionFacade(elements: List<KtElement>): ResolutionFacade {
        return KotlinSimpleResolutionFacade(ideaProject, elements, project)
    }
}

class KotlinSimpleResolutionFacade(
        override val project: Project,
        private val elements: List<KtElement>,
        private val nbProject: NBProject) : ResolutionFacade {
    
    override fun analyze(elements: Collection<KtElement>, bodyResolveMode: BodyResolveMode): BindingContext {
        if (elements.isEmpty()) {
            return BindingContext.EMPTY
        }
        val ktFile = elements.first().getContainingKtFile()
        return KotlinAnalysisFileCache.getAnalysisResult(ktFile, nbProject).analysisResult.bindingContext
    }

    override fun resolveToDescriptor(declaration: KtDeclaration, bodyResolveMode: BodyResolveMode): DeclarationDescriptor {
        throw UnsupportedOperationException()
    }

    override val moduleDescriptor: ModuleDescriptor
        get() = throw UnsupportedOperationException()

    override fun analyze(element: KtElement, bodyResolveMode: BodyResolveMode): BindingContext {
        val ktFile = element.getContainingKtFile()
        return KotlinAnalysisFileCache.getAnalysisResult(ktFile, nbProject).analysisResult.bindingContext
    }

    override fun analyzeFullyAndGetResult(elements: Collection<KtElement>): AnalysisResult {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getFrontendService(element: PsiElement, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getFrontendService(serviceClass: Class<T>): T {
        val files = elements.map { it.getContainingKtFile() }.toSet()
        if (files.isEmpty()) throw IllegalStateException("Elements should not be empty")

        val componentProvider = KotlinAnalyzer.analyzeFiles(nbProject, files).componentProvider

        return componentProvider.getService(serviceClass)
    }

    override fun <T : Any> getFrontendService(moduleDescriptor: ModuleDescriptor, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }

    override fun <T : Any> getIdeService(serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }
}

@Suppress("UNCHECKED_CAST") fun <T : Any> ComponentProvider.getService(request: Class<T>): T {
    return resolve(request)!!.getValue() as T
}