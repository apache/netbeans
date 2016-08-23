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
package org.jetbrains.kotlin.resolve

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.config.LanguageFeatureSettings
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.createContainer
import org.jetbrains.kotlin.container.registerSingleton
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.context.ModuleContext
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.frontend.di.configureModule
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.java.InternalFlexibleTypeTransformer
import org.jetbrains.kotlin.load.java.components.SignaturePropagatorImpl
import org.jetbrains.kotlin.load.java.components.TraceBasedErrorReporter
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver
import org.jetbrains.kotlin.load.java.sam.SamConversionResolverImpl
import org.jetbrains.kotlin.load.kotlin.DeserializationComponentsForJava
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzerForTopLevel
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver
import org.jetbrains.kotlin.resolve.jvm.JavaLazyAnalyzerPostConstruct
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaClassFinder
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansTraceBasedJavaResolverCache
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansExternalAnnotationResolver
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaPropertyInitializerEvaluator
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansJavaSourceElementFactory
import org.netbeans.api.project.Project as NBProject

fun StorageComponentContainer.configureJavaTopDownAnalysis(
        moduleContentScope: GlobalSearchScope,
        project: Project,
        lookupTracker: LookupTracker,
        languageFeatureSettings: LanguageFeatureSettings) {
    useInstance(moduleContentScope)
    useInstance(lookupTracker)

    useImpl<ResolveSession>()

    useImpl<LazyTopDownAnalyzer>()
    useImpl<LazyTopDownAnalyzerForTopLevel>()
    useImpl<JavaDescriptorResolver>()
    useImpl<DeserializationComponentsForJava>()

    useInstance(JvmVirtualFileFinderFactory.SERVICE.getInstance(project).create(moduleContentScope))

    useImpl<NetBeansJavaClassFinder>()
    useImpl<SignaturePropagatorImpl>()
    useImpl<NetBeansTraceBasedJavaResolverCache>()
    useImpl<TraceBasedErrorReporter>()
    useImpl<NetBeansExternalAnnotationResolver>()
    useImpl<NetBeansJavaPropertyInitializerEvaluator>()
    useInstance(SamConversionResolverImpl)
    useImpl<NetBeansJavaSourceElementFactory>()
    useImpl<JavaLazyAnalyzerPostConstruct>()
    useInstance(InternalFlexibleTypeTransformer)

    useInstance(languageFeatureSettings)
}

public fun createContainerForTopDownAnalyzerForJvm(
        moduleContext: ModuleContext, bindingTrace: BindingTrace,
        declarationProviderFactory: DeclarationProviderFactory,
        moduleContentScope: GlobalSearchScope,
        javaProject: NBProject,
        lookupTracker: LookupTracker,
        packagePartProvider: PackagePartProvider,
        languageFeatureSettings: LanguageFeatureSettings
): Pair<ContainerForTopDownAnalyzerForJvm, StorageComponentContainer> = createContainer("TopDownAnalyzerForJvm") {
    useInstance(packagePartProvider)

    configureModule(moduleContext, JvmPlatform, bindingTrace)
    configureJavaTopDownAnalysis(moduleContentScope, moduleContext.project, lookupTracker, languageFeatureSettings)

    useInstance(javaProject)
    useInstance(declarationProviderFactory)

    CompilerEnvironment.configure(this)

    useImpl<SingleModuleClassResolver>()
    useImpl<FileScopeProviderImpl>()
}.let {
    it.javaAnalysisInit()

    Pair(ContainerForTopDownAnalyzerForJvm(it), it)
}

fun StorageComponentContainer.javaAnalysisInit() {
    get<JavaClassFinderPostConstruct>().postCreate()
}

// Copy functions from Dsl.kt as they were shrinked by proguard
inline fun <reified T : Any> StorageComponentContainer.useImpl() {
    registerSingleton(T::class.java)
}

inline fun <reified T : Any> ComponentProvider.get(): T {
    return getService(T::class.java)
}