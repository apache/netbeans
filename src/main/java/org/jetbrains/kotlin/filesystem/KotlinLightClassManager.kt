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
package org.jetbrains.kotlin.filesystem

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.io.File
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.filesystem.lightclasses.LightClassFile
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.fileClasses.*
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil
import org.netbeans.api.project.Project as NBProject

class KotlinLightClassManager(private val project: NBProject) {
    
    companion object {
        fun getInstance(project: NBProject): KotlinLightClassManager {
            val ideaProject = KotlinEnvironment.getEnvironment(project).project
            return ServiceManager.getService(ideaProject, KotlinLightClassManager::class.java)
        }

        fun getInternalName(classOrObject: KtClassOrObject): String? {
            val fullFqName = classOrObject.fqName ?: return null
            val topmostClassOrObject: KtClassOrObject = PsiTreeUtil.
                    getTopmostParentOfType(classOrObject, KtClassOrObject::class.java) ?: return makeInternalByToplevel(fullFqName)
            val topLevelFqName = topmostClassOrObject.fqName ?: return null
            
            val nestedPart = fullFqName.asString().substring(topLevelFqName.asString().length).replace(".", "$")
            return makeInternalByToplevel(topLevelFqName) + nestedPart
        }

        private fun makeInternalByToplevel(fqName: FqName): String {
            return fqName.asString().replace(".", "/")
        }
    }
    
    private val sourceFiles = hashMapOf<File, Set<FileObject>>()

    fun computeLightClassesSources() {
        val newSourceFilesMap = hashMapOf<File, MutableSet<FileObject>>()
        for (sourceFile in KotlinPsiManager.INSTANCE.getFilesByProject(project)) {
            val lightClassesPaths = getLightClassesPaths(sourceFile)
            for (path in lightClassesPaths) {
                val lightClassFile = LightClassFile(project, path)
                var newSourceFiles = newSourceFilesMap[lightClassFile.asFile()]
                if (newSourceFiles == null) {
                    newSourceFiles = hashSetOf<FileObject>()
                    newSourceFilesMap.put(lightClassFile.asFile(), newSourceFiles)
                }
                
                newSourceFiles.add(sourceFile)
            }
        }
        sourceFiles.clear()
        sourceFiles.putAll(newSourceFilesMap)
    }

    fun getLightClassesPaths(sourceFile: FileObject?): List<String> {
        val lightClasses = arrayListOf<String>()
        val ktFile = ProjectUtils.getKtFile(sourceFile)
        
        findLightClasses(ktFile).forEach {
            val internalName = getInternalName(it)
            if (internalName != null) lightClasses.add(computePathByInternalName(internalName))
        }
        
        if (PackagePartClassUtils.fileHasTopLevelCallables(ktFile)) {
            val newFacadeInternalName = NoResolveFileClassesProvider.getFileClassInternalName(ktFile)
            lightClasses.add(computePathByInternalName(newFacadeInternalName))
        }
        
        return lightClasses
    }

    private fun findLightClasses(ktFile: KtFile): List<KtClassOrObject> {
        val lightClasses = arrayListOf<KtClassOrObject>()
        
        ktFile.acceptChildren(object : KtVisitorVoid() {
            override fun visitClassOrObject(classOrObject: KtClassOrObject) {
                lightClasses.add(classOrObject)
                super.visitClassOrObject(classOrObject)
            }

            override fun visitNamedFunction(function: KtNamedFunction) {}

            override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor) {}

            override fun visitProperty(property: KtProperty) {}

            override fun visitElement(element: PsiElement?) {
                if (element != null) element.acceptChildren(this)
            }
        })
        
        return lightClasses
    }

    private fun computePathByInternalName(internalName: String) = "${internalName}.class"

    fun getSourceFiles(file: File): List<KtFile> {
        if (sourceFiles.isEmpty()) computeLightClassesSources()
        
        return getSourceKtFiles(file)
    }

    private fun getSourceKtFiles(file: File): List<KtFile> {
        val sourceIOFiles = sourceFiles[file] ?: return emptyList()
        
        return sourceIOFiles.mapNotNull { ProjectUtils.getKtFile(it) }
    }
}