/** *****************************************************************************
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
 ****************************************************************************** */
package org.jetbrains.kotlin.projectsextensions

import java.io.IOException
import java.util.HashMap
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.project.KotlinProjectConstants
import org.jetbrains.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider
import org.jetbrains.kotlin.project.KotlinSources
import org.jetbrains.kotlin.projectsextensions.maven.classpath.MavenExtendedClassPath
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lang.java.JavaEnvironment
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.java.classpath.ClassPath
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.Task
import org.netbeans.api.progress.ProgressHandle
import org.netbeans.api.progress.ProgressHandleFactory
import org.netbeans.api.project.Project
import org.netbeans.modules.parsing.api.indexing.IndexingManager
import org.netbeans.spi.java.classpath.support.ClassPathSupport
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil
import org.openide.util.RequestProcessor

/**
 *
 * @author Alexander.Baratynski
 */
object KotlinProjectHelper {
    
    private val kotlinSources = hashMapOf<Project, KotlinSources>()
    private val extendedClassPaths = hashMapOf<Project, ClassPathExtender>()
    private val fullClasspaths = hashMapOf<Project, ClassPath>()
    private val environmentLoader = RequestProcessor("Kotlin Environment loader")
    private val isScanning = hashMapOf<Project, Boolean>()
    private val hasJavaFiles = hashMapOf<Project, Boolean>()
    
    fun Project.isScanning() = isScanning[this] ?: false

    fun postTask(run: Runnable) = environmentLoader.post(run)
    
    fun hasJavaFiles(project: Project) = hasJavaFiles[project] ?: false
    
    fun setHasJavaFiles(project: Project) = hasJavaFiles.put(project, true)
    
    fun Project.doInitialScan() {
        hasJavaFiles.put(this, getJavaFilesByProject(this).isNotEmpty())
        JavaEnvironment.checkJavaSource(this)
        try {
            JavaEnvironment.JAVA_SOURCE[this]?.runWhenScanFinished({
                postTask(Runnable {
                    val progressBar = ProgressHandleFactory.createHandle("Kotlin files analysis...")
                    progressBar.start()
                    isScanning.put(this, true)
                    ProjectUtils.getSourceFiles(this).forEach {
                        KotlinParser.getAnalysisResult(it, this)
                    }
                    KotlinSources(this).getAllKtFiles().forEach {
                        IndexingManager.getDefault().refreshAllIndices(it)
                    }
                    isScanning.put(this, false)
                    progressBar.finish()
                })
            }, true)
        } catch (ex: IOException) {}
    }

    fun Project.checkProject(): Boolean {
        val className = javaClass.name
        return className == "org.netbeans.modules.java.j2seproject.J2SEProject" 
                || className == "org.netbeans.modules.maven.NbMavenProjectImpl"
    }

    fun Project.isMavenProject(): Boolean {
        return javaClass.name == "org.netbeans.modules.maven.NbMavenProjectImpl"
    }

    fun Project.removeProjectCache() {
        kotlinSources.remove(this)
        extendedClassPaths.remove(this)
        fullClasspaths.remove(this)
    }

    fun Project.getKotlinSources(): KotlinSources? {
        if (!checkProject()) return null
        
        if (!kotlinSources.containsKey(this)) {
            kotlinSources.put(this, KotlinSources(this))
        }
        return kotlinSources[this]
    }

    fun Project.getExtendedClassPath(): ClassPathExtender? {
        if (!checkProject()) return null
        
        if (!extendedClassPaths.containsKey(this)) {
            when (javaClass.name) {
                "org.netbeans.modules.java.j2seproject.J2SEProject" -> extendedClassPaths.put(this, J2SEExtendedClassPathProvider(this))
                "org.netbeans.modules.maven.NbMavenProjectImpl" -> extendedClassPaths.put(this, MavenExtendedClassPath(this))
            }
        }
        return extendedClassPaths[this]
    }

    fun Project.getFullClassPath(): ClassPath? {
        if (!fullClasspaths.containsKey(this)) {
            val classpath = getExtendedClassPath() ?: return null
            
            val boot = classpath.getProjectSourcesClassPath(ClassPath.BOOT)
            val compile = classpath.getProjectSourcesClassPath(ClassPath.COMPILE)
            val source = classpath.getProjectSourcesClassPath(ClassPath.SOURCE)
            val proxy = ClassPathSupport.createProxyClassPath(boot, compile, source)
            
            fullClasspaths.put(this, proxy)
        }
        return fullClasspaths[this]
    }

    private fun Project.updateFullClassPath() {
        val classpath = getExtendedClassPath() ?: return
        
        val boot = classpath.getProjectSourcesClassPath(ClassPath.BOOT)
        val compile = classpath.getProjectSourcesClassPath(ClassPath.COMPILE)
        val source = classpath.getProjectSourcesClassPath(ClassPath.SOURCE)
        val proxy = ClassPathSupport.createProxyClassPath(boot, compile, source)
        fullClasspaths.put(this, proxy)
    }

    fun Project.updateExtendedClassPath() {
        when (javaClass.name) {
            "org.netbeans.modules.java.j2seproject.J2SEProject" -> extendedClassPaths.put(this, J2SEExtendedClassPathProvider(this))
            "org.netbeans.modules.maven.NbMavenProjectImpl" -> extendedClassPaths.put(this, MavenExtendedClassPath(this))
        }
        
        updateFullClassPath()
        JavaEnvironment.updateClasspathInfo(this)
        KotlinEnvironment.updateKotlinEnvironment(this)
    }
    
    fun getJavaFilesByProject(project: Project) = project.getKotlinSources()
                ?.getSourceGroups(KotlinProjectConstants.JAVA_SOURCE)
                ?.flatMap { it.rootFolder.children.toList() }
                ?.filter { it.ext == "java" }
                ?.toSet() ?: emptySet()
    
}