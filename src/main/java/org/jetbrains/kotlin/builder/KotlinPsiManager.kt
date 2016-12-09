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
package org.jetbrains.kotlin.builder

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.text.StringUtilRt
import com.intellij.openapi.vfs.CharsetToolkit
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.testFramework.LightVirtualFile
import java.io.IOException
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.model.KotlinLightVirtualFile
import org.jetbrains.kotlin.project.KotlinProjectConstants
import org.jetbrains.kotlin.utils.KotlinMockProject
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.psi.KtFile
import org.netbeans.api.project.Project
import org.netbeans.api.project.SourceGroup
import org.netbeans.api.project.ui.OpenProjects
import org.openide.filesystems.FileObject

object KotlinPsiManager {
    
    private val cachedKtFiles = hashMapOf<FileObject, KtFile>()
    
    fun getFilesByProject(project: Project) = KotlinProjectHelper.INSTANCE.getKotlinSources(project)
                .getSourceGroups(KotlinProjectConstants.KOTLIN_SOURCE)
                .flatMap { it.rootFolder.children.toList() }
                .filter { it.isKotlinFile() }
                .toSet()
        

    /**
     * This method parses the input file.
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return the result of {@link #parseText(java.lang.String, java.io.File) parseText} method
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun parseFile(file: FileObject) = parseText(StringUtilRt.convertLineSeparators(file.asText()), file)

    /**
     * This method parses text from the input file.
     * @param text Text of temporary file.
     * @param file syntaxFile that was created with createSyntaxFile method
     * @return {@link KtFile}
     */
    fun parseText(text: String, file: FileObject): KtFile? {
        StringUtil.assertValidSeparators(text)
        val kotlinProject = ProjectUtils.getKotlinProjectForFileObject(file) ?: ProjectUtils.getValidProject()
        if (kotlinProject == null) {
            KotlinLogger.INSTANCE.logWarning("Project is null")
            return null
        }
        
        val project = KotlinEnvironment.getEnvironment(kotlinProject).project
        val virtualFile = KotlinLightVirtualFile(file, text)
        virtualFile.setCharset(CharsetToolkit.UTF8_CHARSET)
        val psiFileFactory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
        
        return psiFileFactory.trySetupPsiForFile(virtualFile, KotlinLanguage.INSTANCE, true, false) as KtFile
    }

    fun parseTextForDiagnostic(text: String, file: FileObject): KtFile? {
        StringUtil.assertValidSeparators(text)
        if (cachedKtFiles.containsKey(file)) {
            updatePsiFile(text, file)
        } else {
            try {
                val ktFile = parseFile(file) ?: return null
                cachedKtFiles.put(file, ktFile)
            } catch (ex: IOException) {
                KotlinLogger.INSTANCE.logException("parseFile exception", ex)
            }
        }
        
        return cachedKtFiles[file]
    }

    @Throws(IOException::class)
    fun getParsedFile(file: FileObject): KtFile? {
        if (!cachedKtFiles.containsKey(file)) {
            val ktFile = parseFile(file) ?: return null
            cachedKtFiles.put(file, ktFile)
        } else {
            updatePsiFile(file)
        }
        return cachedKtFiles[file]
    }

    @Throws(IOException::class)
    private fun updatePsiFile(file: FileObject) {
        val code = file.asText()
        val sourceCodeWithoutCR = StringUtilRt.convertLineSeparators(code)
        val currentParsedFile = cachedKtFiles[file] ?: return
        if (currentParsedFile.text != sourceCodeWithoutCR) {
            val ktFile = parseText(sourceCodeWithoutCR, file) ?: return
            cachedKtFiles.put(file, ktFile)
        }
    }

    private fun updatePsiFile(sourceCode: String, file: FileObject) {
        val sourceCodeWithoutCR = StringUtilRt.convertLineSeparators(sourceCode)
        val currentParsedFile = cachedKtFiles[file] ?: return
        if (currentParsedFile.text != sourceCodeWithoutCR) {
            val ktFile = parseText(sourceCodeWithoutCR, file) ?: return
            cachedKtFiles.put(file, ktFile)
        }
    }

    fun getParsedKtFileForSyntaxHighlighting(text: String): KtFile? {
        val sourceCode = StringUtilRt.convertLineSeparators(text)
        var kotlinProject = OpenProjects.getDefault().openProjects
                .filter { KotlinProjectHelper.INSTANCE.checkProject(it) }
                .firstOrNull()
           
        if (kotlinProject == null) kotlinProject = KotlinMockProject.getMockProject() ?: return null
        
        val project = KotlinEnvironment.getEnvironment(kotlinProject).project
        val psiFileFactory = PsiFileFactory.getInstance(project) as PsiFileFactoryImpl
        
        return psiFileFactory.createFileFromText(KotlinLanguage.INSTANCE, sourceCode) as KtFile
    }
}


fun FileObject.isKotlinFile() = KotlinFileType.INSTANCE.getDefaultExtension() == ext
