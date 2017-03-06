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
package org.jetbrains.kotlin.model

import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInsight.InferredAnnotationsManager
import org.jetbrains.kotlin.resolve.lang.kotlin.NetBeansVirtualFileFinderFactory
import java.io.File
import org.jetbrains.kotlin.asJava.classes.KtLightClassForFacade
import org.jetbrains.kotlin.asJava.LightClassGenerationSupport
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.resolve.CodeAnalyzerInitializer
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.NullableNotNullManager
import com.intellij.codeInsight.runner.JavaMainMethodProvider
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreJavaFileManager
import com.intellij.core.JavaCoreApplicationEnvironment
import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.impl.ZipHandler
import com.intellij.psi.PsiElementFinder
import com.intellij.psi.PsiManager
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.psi.compiled.ClassFileDecompilers
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.impl.file.impl.JavaFileManager
import java.util.Collections
import org.jetbrains.kotlin.filesystem.KotlinLightClassManager
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.resolve.BuiltInsReferenceResolver
import org.jetbrains.kotlin.resolve.KotlinCacheServiceImpl
import org.jetbrains.kotlin.resolve.KotlinSourceIndex
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.utils.KotlinImportInserterHelper
import org.jetbrains.kotlin.caches.resolve.KotlinCacheService
import org.jetbrains.kotlin.cli.common.CliModuleVisibilityManagerImpl
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import com.intellij.formatting.KotlinLanguageCodeStyleSettingsProvider
import com.intellij.formatting.KotlinSettingsProvider
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import org.jetbrains.kotlin.cli.jvm.index.JavaRoot
import org.jetbrains.kotlin.cli.jvm.index.JvmDependenciesIndexImpl
import org.jetbrains.kotlin.cli.jvm.compiler.MockExternalAnnotationsManager
import org.jetbrains.kotlin.cli.jvm.compiler.MockInferredAnnotationsManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.util.ImportInsertHelper
import org.jetbrains.kotlin.js.resolve.diagnostics.DefaultErrorMessagesJs
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory
import org.jetbrains.kotlin.load.kotlin.ModuleVisibilityManager
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.resolve.diagnostics.SuppressStringProvider
import org.jetbrains.kotlin.resolve.jvm.diagnostics.DefaultErrorMessagesJvm
import org.jetbrains.kotlin.resolve.jvm.extensions.PackageFragmentProviderExtension
import org.jetbrains.kotlin.script.KotlinScriptDefinitionProvider
import org.jetbrains.kotlin.script.KotlinScriptExternalImportsProvider
import org.netbeans.api.project.Project as NBProject
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCliJavaFileManagerImpl
import org.jetbrains.kotlin.model.KotlinNullableNotNullManager
import com.intellij.openapi.util.SystemInfo

//copied from kotlin eclipse plugin to avoid RuntimeException: Could not find installation home path. 
//Please make sure bin/idea.properties is present in the installation directory
private fun setIdeaIoUseFallback() {
    if (SystemInfo.isWindows) {
        val properties = System.getProperties()

        properties.setProperty("idea.io.use.nio2", java.lang.Boolean.TRUE.toString());

        if (!(SystemInfo.isJavaVersionAtLeast("1.7") && !"1.7.0-ea".equals(SystemInfo.JAVA_VERSION))) {
            properties.setProperty("idea.io.use.fallback", java.lang.Boolean.TRUE.toString());
        }
    }
}

class KotlinEnvironment private constructor(kotlinProject: NBProject, disposable: Disposable) {

    companion object {
        val CACHED_ENVIRONMENT = hashMapOf<NBProject, KotlinEnvironment>()
        
        @Synchronized fun getEnvironment(kotlinProject: NBProject): KotlinEnvironment {
            if (!CACHED_ENVIRONMENT.containsKey(kotlinProject)) {
                CACHED_ENVIRONMENT.put(kotlinProject, KotlinEnvironment(kotlinProject, Disposer.newDisposable()))
            }
            
            return CACHED_ENVIRONMENT.get(kotlinProject)!!
        }
        
        @Synchronized fun updateKotlinEnvironment(kotlinProject: NBProject) {
            if (CACHED_ENVIRONMENT.containsKey(kotlinProject)) {
                val environment = CACHED_ENVIRONMENT.get(kotlinProject)!!
                Disposer.dispose(environment.applicationEnvironment.parentDisposable)
                ZipHandler.clearFileAccessorCache()
            }
            CACHED_ENVIRONMENT.put(kotlinProject, KotlinEnvironment(kotlinProject, Disposer.newDisposable()))
        }
    }
    
    val KOTLIN_COMPILER_PATH = ProjectUtils.buildLibPath("kotlin-compiler")
    
    val applicationEnvironment: JavaCoreApplicationEnvironment
    val projectEnvironment: JavaCoreProjectEnvironment
    val project: MockProject
    val roots = hashSetOf<JavaRoot>()
    
    val index by lazy { JvmDependenciesIndexImpl(roots.toList()) }
    
    val configuration = CompilerConfiguration()
    
    init {
        val startTime = System.nanoTime()

        setIdeaIoUseFallback()
                
        applicationEnvironment = createJavaCoreApplicationEnvironment(disposable)
        projectEnvironment = object : JavaCoreProjectEnvironment(disposable, applicationEnvironment) {
            override fun preregisterServices() { 
                registerProjectExtensionPoints(Extensions.getArea(project)) 
            }
            
            override fun createCoreFileManager() = KotlinCliJavaFileManagerImpl(PsiManager.getInstance(project))
        }
        project = projectEnvironment.project
        
        with (project) {
            val scriptDefinitionProvider = KotlinScriptDefinitionProvider()
            registerService(KotlinScriptDefinitionProvider::class.java, scriptDefinitionProvider)
            registerService(
                    KotlinScriptExternalImportsProvider::class.java,
                    KotlinScriptExternalImportsProvider(project, scriptDefinitionProvider))
            
            registerService(ModuleVisibilityManager::class.java, CliModuleVisibilityManagerImpl())
            registerService(NullableNotNullManager::class.java, KotlinNullableNotNullManager(kotlinProject))
            registerService(CoreJavaFileManager::class.java,
                ServiceManager.getService(project, JavaFileManager::class.java) as CoreJavaFileManager)
            
            val cliLightClassGenerationSupport = CliLightClassGenerationSupport(project)
            registerService(LightClassGenerationSupport::class.java, cliLightClassGenerationSupport)
            registerService(CliLightClassGenerationSupport::class.java, cliLightClassGenerationSupport)
            registerService(CodeAnalyzerInitializer::class.java, cliLightClassGenerationSupport)
            
            registerService(KtLightClassForFacade.FacadeStubCache::class.java, KtLightClassForFacade.FacadeStubCache(project))
            registerService(KotlinLightClassManager::class.java, KotlinLightClassManager(kotlinProject))
            registerService(BuiltInsReferenceResolver::class.java, BuiltInsReferenceResolver(project))
            registerService(KotlinSourceIndex::class.java, KotlinSourceIndex())
            registerService(KotlinCacheService::class.java, KotlinCacheServiceImpl(project, kotlinProject))
            registerService(JvmVirtualFileFinderFactory::class.java, NetBeansVirtualFileFinderFactory(kotlinProject))
            registerService(ImportInsertHelper::class.java, KotlinImportInserterHelper())
            
            registerService(ExternalAnnotationsManager::class.java, MockExternalAnnotationsManager())
            registerService(InferredAnnotationsManager::class.java, MockInferredAnnotationsManager())
        }
        
        configuration.put<String>(CommonConfigurationKeys.MODULE_NAME, project.name)
        
        configureClasspath(kotlinProject)
        
        ExpressionCodegenExtension.Companion.registerExtensionPoint(project)
        
        getExtensionsFromCommonXml()
        getExtensionsFromKotlin2JvmXml()
        
        CACHED_ENVIRONMENT.put(kotlinProject, this)
        KotlinLogger.INSTANCE.logInfo("KotlinEnvironment init: ${(System.nanoTime() - startTime)} ns")
    }
    
    private fun registerProjectExtensionPoints(area: ExtensionsArea) {
        CoreApplicationEnvironment.registerExtensionPoint(area, 
                PsiTreeChangePreprocessor.EP_NAME, PsiTreeChangePreprocessor::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(area, 
                PsiElementFinder.EP_NAME, PsiElementFinder::class.java)
    }
    
    private fun getExtensionsFromCommonXml() {
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName("org.jetbrains.kotlin.diagnosticSuppressor"), DiagnosticSuppressor::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName("org.jetbrains.kotlin.defaultErrorMessages"), DefaultErrorMessages.Extension::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName("org.jetbrains.kotlin.suppressStringProvider"), SuppressStringProvider::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName(("org.jetbrains.kotlin.expressionCodegenExtension")), ExpressionCodegenExtension::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName(("org.jetbrains.kotlin.classBuilderFactoryInterceptorExtension")), ClassBuilderInterceptorExtension::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(
                ExtensionPointName(("org.jetbrains.kotlin.packageFragmentProviderExtension")), PackageFragmentProviderExtension::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME, KotlinSettingsProvider::class.java)
        CoreApplicationEnvironment.registerApplicationExtensionPoint(LanguageCodeStyleSettingsProvider.EP_NAME, KotlinLanguageCodeStyleSettingsProvider::class.java)
        
        with (Extensions.getRootArea()) {
            getExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME).registerExtension(KotlinSettingsProvider())
            getExtensionPoint(LanguageCodeStyleSettingsProvider.EP_NAME).registerExtension(KotlinLanguageCodeStyleSettingsProvider())
            getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(DefaultErrorMessagesJvm())
            getExtensionPoint(DefaultErrorMessages.Extension.EP_NAME).registerExtension(DefaultErrorMessagesJs())
        }
    }
    
    private fun getExtensionsFromKotlin2JvmXml() {
        CoreApplicationEnvironment.registerComponentInstance<DefaultErrorMessages.Extension>(Extensions.getRootArea().getPicoContainer(), 
                DefaultErrorMessages.Extension::class.java, DefaultErrorMessagesJvm())
    }
    
    private fun registerApplicationExtensionPointsAndExtensionsFromConfigFile(configFilePath: String) {
        CoreApplicationEnvironment.registerExtensionPointAndExtensions(File(KOTLIN_COMPILER_PATH), configFilePath, Extensions.getRootArea())
    }
    
    private fun configureClasspath(kotlinProject: NBProject) {
        val classpath = ProjectUtils.getClasspath(kotlinProject)
        KotlinLogger.INSTANCE.logInfo("Project ${kotlinProject.projectDirectory.path} classpath is $classpath")
        classpath.forEach {
            if (it.endsWith("!/")) {
                addToClasspath(it.split("!/")[0].substringAfter("file:"), null)
            } else {
                addToClasspath(it, null)
            }
        }
    }
    
    private fun createJavaCoreApplicationEnvironment(disposable: Disposable): JavaCoreApplicationEnvironment {
        Extensions.cleanRootArea(disposable)
        registerAppExtensionPoints()
        val javaApplicationEnvironment = JavaCoreApplicationEnvironment(disposable)
        
        with (javaApplicationEnvironment) {
            registerFileType(PlainTextFileType.INSTANCE, "xml")
            registerFileType(KotlinFileType.INSTANCE, "kt")
            registerParserDefinition(KotlinParserDefinition())
            application.registerService(KotlinBinaryClassCache::class.java, KotlinBinaryClassCache())
        }
        
        return javaApplicationEnvironment
    }
    
    private fun registerAppExtensionPoints() {
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ContainerProvider.EP_NAME,
                ContainerProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ClsCustomNavigationPolicy.EP_NAME,
                ClsCustomNavigationPolicy::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), ClassFileDecompilers.EP_NAME,
                ClassFileDecompilers.Decompiler::class.java)
        
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), PsiAugmentProvider.EP_NAME, PsiAugmentProvider::class.java)
        CoreApplicationEnvironment.registerExtensionPoint(Extensions.getRootArea(), JavaMainMethodProvider.EP_NAME, JavaMainMethodProvider::class.java)
    }
    
    private fun addToClasspath(path: String, rootType: JavaRoot.RootType?) {
        val file = File(path)
        if (file.isFile()) {
            val jarFile = applicationEnvironment.jarFileSystem.findFileByPath("$path!/") ?: return
            projectEnvironment.addJarToClassPath(file)
            val type = rootType ?: JavaRoot.RootType.BINARY
            
            roots.add(JavaRoot(jarFile, type, null))
        } else {
            val root = applicationEnvironment.localFileSystem.findFileByPath(path) ?: return
            projectEnvironment.addSourcesToClasspath(root)
            val type = rootType ?: JavaRoot.RootType.SOURCE
            
            roots.add(JavaRoot(root, type, null))
        }
    }
    
    fun isJarFile(pathToJar: String): Boolean {
        val jarFile = applicationEnvironment.jarFileSystem.findFileByPath("$pathToJar!/") ?: return false
        return jarFile.isValid
    }
    
    fun getVirtualFile(location: String) = applicationEnvironment.localFileSystem.findFileByPath(location)
    
    fun getVirtualFileInJar(path: String): VirtualFile? {
        val decodedPath = URLDecoder.decode(path, "UTF-8") ?: path
        return applicationEnvironment.jarFileSystem.findFileByPath(decodedPath)
    }
    
    fun getVirtualFileInJar(pathToJar: String, relativePath: String): VirtualFile? {
        val decodedPathToJar = URLDecoder.decode(pathToJar, "UTF-8") ?: pathToJar
        val decodedRelativePath = URLDecoder.decode(relativePath, "UTF-8") ?: relativePath
        
        return applicationEnvironment.jarFileSystem.findFileByPath("$decodedPathToJar!/$decodedRelativePath")
    }
}