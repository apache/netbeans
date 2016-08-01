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
package org.black.kotlin.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import kotlin.Pair;
import kotlin.Unit;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.black.kotlin.resolve.lang.java.resolver.NetBeansExternalAnnotationResolver;
import org.black.kotlin.resolve.lang.java.resolver.NetBeansJavaSourceElementFactory;
import org.black.kotlin.resolve.lang.java.resolver.NetBeansTraceBasedJavaResolverCache;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaPropertyInitializerEvaluator;
import org.jetbrains.kotlin.container.StorageComponentContainer;
import org.jetbrains.kotlin.context.ModuleContext;
import org.jetbrains.kotlin.descriptors.PackagePartProvider;
import org.jetbrains.kotlin.frontend.java.di.ContainerForTopDownAnalyzerForJvm;
import org.jetbrains.kotlin.incremental.components.LookupTracker;
import org.jetbrains.kotlin.resolve.BindingTrace;
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory;
import org.jetbrains.kotlin.container.DslKt;
import org.jetbrains.kotlin.container.ContainerKt;
import org.jetbrains.kotlin.frontend.di.InjectionKt;
import org.jetbrains.kotlin.load.java.components.SignaturePropagatorImpl;
import org.jetbrains.kotlin.load.java.components.TraceBasedErrorReporter;
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver;
import org.jetbrains.kotlin.load.java.sam.SamConversionResolverImpl;
import org.jetbrains.kotlin.load.kotlin.DeserializationComponentsForJava;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory;
import org.jetbrains.kotlin.resolve.CompilerEnvironment;
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer;
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzerForTopLevel;
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct;
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver;
import org.jetbrains.kotlin.resolve.jvm.JavaLazyAnalyzerPostConstruct;
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatform;
import org.jetbrains.kotlin.resolve.lazy.FileScopeProviderImpl;
import org.jetbrains.kotlin.resolve.lazy.ResolveSession;
import kotlin.jvm.functions.Function1;
import org.jetbrains.kotlin.config.LanguageFeatureSettings;
import org.jetbrains.kotlin.load.java.InternalFlexibleTypeTransformer;

/**
 *
 * @author Александр
 */
public class Injection {

    public static void configureJavaTopDownAnalysis(StorageComponentContainer container,
            GlobalSearchScope moduleContentScope, Project project, LookupTracker lookupTracker, 
            LanguageFeatureSettings languageFeatureSettings) {
        DslKt.useInstance(container, moduleContentScope);
        DslKt.useInstance(container, lookupTracker);

        ContainerKt.registerSingleton(container, ResolveSession.class);

        ContainerKt.registerSingleton(container, LazyTopDownAnalyzer.class);
        ContainerKt.registerSingleton(container, LazyTopDownAnalyzerForTopLevel.class);
        ContainerKt.registerSingleton(container, JavaDescriptorResolver.class);
        ContainerKt.registerSingleton(container, DeserializationComponentsForJava.class);

        DslKt.useInstance(container, JvmVirtualFileFinderFactory.SERVICE.getInstance(project).create(moduleContentScope));

        ContainerKt.registerSingleton(container, NetBeansJavaClassFinder.class);
        ContainerKt.registerSingleton(container, SignaturePropagatorImpl.class);
        ContainerKt.registerSingleton(container, NetBeansTraceBasedJavaResolverCache.class);
        ContainerKt.registerSingleton(container, TraceBasedErrorReporter.class);
        ContainerKt.registerSingleton(container, NetBeansExternalAnnotationResolver.class);
        ContainerKt.registerSingleton(container, NetBeansJavaPropertyInitializerEvaluator.class);
        DslKt.useInstance(container, SamConversionResolverImpl.INSTANCE);
        ContainerKt.registerSingleton(container, NetBeansJavaSourceElementFactory.class);
        ContainerKt.registerSingleton(container, JavaLazyAnalyzerPostConstruct.class);
        DslKt.useInstance(container, InternalFlexibleTypeTransformer.class);
        
        DslKt.useInstance(container, languageFeatureSettings);

    }

    public static Pair<ContainerForTopDownAnalyzerForJvm, StorageComponentContainer>
            createContainerForTopDownAnalyzerForJvm(
                    final ModuleContext moduleContext, final BindingTrace bindingTrace,
                    final DeclarationProviderFactory declarationProviderFactory,
                    final GlobalSearchScope moduleContentScope,
                    final org.netbeans.api.project.Project kotlinProject,
                    final LookupTracker lookupTracker,
                    final PackagePartProvider packagePartProvider,
                    final LanguageFeatureSettings languageFeatureSettings) {

        StorageComponentContainer container = DslKt.createContainer("TopDownAnalyzerForJvm", 
                new Function1<StorageComponentContainer, Unit>(){
            @Override
            public Unit invoke(StorageComponentContainer container) {
                DslKt.useInstance(container, packagePartProvider);

                InjectionKt.configureModule(container, moduleContext, JvmPlatform.INSTANCE, bindingTrace);
                configureJavaTopDownAnalysis(container, moduleContentScope, moduleContext.getProject(), lookupTracker, languageFeatureSettings);

                DslKt.useInstance(container, kotlinProject);
                DslKt.useInstance(container, declarationProviderFactory);

                CompilerEnvironment.INSTANCE.configure(container);

                ContainerKt.registerSingleton(container, SingleModuleClassResolver.class);
                ContainerKt.registerSingleton(container, FileScopeProviderImpl.class);
                
                return Unit.INSTANCE;
            }
        });
        
        javaAnalysisInit(container);
        
        return new Pair<ContainerForTopDownAnalyzerForJvm, StorageComponentContainer>(
                new ContainerForTopDownAnalyzerForJvm(container), container);
    }

    private static void javaAnalysisInit(StorageComponentContainer container) {
        DslKt.getService(container, JavaClassFinderPostConstruct.class).postCreate();
    }

}
