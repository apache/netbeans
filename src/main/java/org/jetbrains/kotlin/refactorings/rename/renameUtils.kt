@file:JvmName("RenamePerformer")
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
package org.jetbrains.kotlin.refactorings.rename

import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.io.File
import javax.lang.model.element.ElementKind
import javax.swing.text.Position
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.fileClasses.*
import org.jetbrains.kotlin.highlighter.occurrences.*
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.navigation.references.resolveToSourceDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.project.Project
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.spi.GsfUtilities
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil
import org.openide.text.CloneableEditorSupport
import org.openide.text.PositionBounds
import org.openide.text.PositionRef
import org.netbeans.modules.csl.spi.support.ModificationResult
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference
import org.netbeans.modules.refactoring.spi.Transaction
import org.netbeans.modules.refactoring.spi.RefactoringCommit

/*
  @author Alexander.Baratynski
  Created on Sep 13, 2016
*/

fun getRenameRefactoringMap(fo: FileObject, psi: PsiElement, newName: String): Map<FileObject, List<OffsetRange>> {
    val ranges = hashMapOf<FileObject, List<OffsetRange>>()
    
    val ktElement: KtElement? = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement::class.java)
    if (ktElement == null) return ranges
    
    val sourceElements = ktElement.resolveToSourceDeclaration()
    if (sourceElements.isEmpty()) return ranges
     
    val searchingElements = getSearchingElements(sourceElements)
    val project = ProjectUtils.getKotlinProjectForFileObject(fo)
    if (project == null) return ranges
    
    val searchKtElements = getKotlinElements(searchingElements)
    
    if (searchKtElements.isEmpty()) return ranges
    val searchingElement = searchKtElements.first()
    
    if (searchingElement.useScope is LocalSearchScope) {
        val occurrencesRanges = search(searchingElements, psi.containingFile as KtFile)
        if (occurrencesRanges.isNotEmpty()) {
            ranges.put(fo, occurrencesRanges)
        }
        
        return ranges
    }
    
    ranges.putAll(getJavaRefactoringMap(searchingElement, project))
    
    ProjectUtils.getSourceFiles(project).forEach { 
        val occurrencesRanges = search(searchingElement, it)
        val f = File(it.virtualFile.path)
        val file = FileUtil.toFileObject(f)
        if (file != null && occurrencesRanges.isNotEmpty()) {
            ranges.put(file, occurrencesRanges)
        }
    }
    
    return ranges
}

private fun getJavaRefactoringMap(searchingElement: KtElement,
                                  project: Project): Map<FileObject, List<OffsetRange>> {
    if (searchingElement is KtClass) {
        val fqName = searchingElement.fqName ?: return emptyMap()
        
        val kind = when {
            searchingElement.isInterface() -> ElementKind.INTERFACE
            searchingElement.isEnum() -> ElementKind.ENUM
            else -> ElementKind.CLASS
        }
        KotlinLogger.INSTANCE.logInfo("Type: $kind")
        
        val elemHandle = project.findType(fqName.asString())?.toString() ?: "NOT FOUND"
        KotlinLogger.INSTANCE.logInfo(elemHandle)
        
        
    } else if (searchingElement is KtObjectDeclaration) {
        val fqName = searchingElement.fqName ?: return emptyMap()
        
    } else if (searchingElement is KtNamedFunction) {
        val classOrObject = searchingElement.containingClassOrObject?.fqName ?: 
                NoResolveFileClassesProvider.getFileClassFqName(searchingElement.getContainingKtFile())
        
    }
    
    return emptyMap()
}

fun createPositionBoundsForFO(fo: FileObject, ranges: List<OffsetRange>): List<PositionBounds> {
    val ces = GsfUtilities.findCloneableEditorSupport(fo) ?: return emptyList()
    
    return ranges.map {
        PositionBounds(
                ces.createPositionRef(it.start, Position.Bias.Forward),
                ces.createPositionRef(it.end, Position.Bias.Forward)
        )
    }
}

fun getTransaction(renameMap: Map<FileObject, List<OffsetRange>>, 
                   newName: String, oldName: String): Transaction {
    val result = ModificationResult()
    for ((file, range) in renameMap) {
        val posBounds = createPositionBoundsForFO(file, range)
        result.addDifferences(file, posBounds.map{ Difference(Difference.Kind.CHANGE, it.begin, it.end, oldName, newName) })
    }
    
    return RefactoringCommit(listOf(result))
}