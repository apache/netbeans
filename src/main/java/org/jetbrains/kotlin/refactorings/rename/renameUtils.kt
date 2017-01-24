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
import java.util.ArrayList
import javax.swing.text.Position
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.fileClasses.*
import org.jetbrains.kotlin.highlighter.occurrences.*
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.navigation.references.resolveToSourceDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.java.source.ClassIndex
import org.netbeans.api.java.source.ClasspathInfo
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.JavaSource
import org.netbeans.api.java.source.SourceUtils
import org.netbeans.api.java.source.TreePathHandle
import org.netbeans.api.project.Project
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.spi.GsfUtilities
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils
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

fun getRenameRefactoringMap(fo: FileObject, psi: PsiElement, newName: String): Map<FileObject, Map<OffsetRange, String>> {
    val ranges = hashMapOf<FileObject, Map<OffsetRange, String>>()
    
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
            ranges.put(fo, occurrencesRanges.associate { Pair(it, newName) })
        }

        return ranges
    }

    ranges.putAll(getJavaRefactoringMap(searchingElement, project, newName))

    ProjectUtils.getSourceFiles(project).forEach {
        val occurrencesRanges = search(searchingElement, it)
        val f = File(it.virtualFile.path)
        val file = FileUtil.toFileObject(f)
        if (file != null && occurrencesRanges.isNotEmpty()) {
            ranges.put(file, occurrencesRanges.associate { Pair(it, newName) })
        }
    }

    return ranges
}

private fun getJavaRefactoringMap(searchingElement: KtElement,
                                  project: Project,
                                  newName: String): Map<FileObject, Map<OffsetRange, String>> {
    val refactoringMap = hashMapOf<FileObject, Map<OffsetRange, String>>()

    fun addToRefactoringMap(file: FileObject, range: OffsetRange, newName: String) {
        if (refactoringMap.containsKey(file)) {
            val map = hashMapOf<OffsetRange, String>()
            map.put(range, newName)
            map.putAll(refactoringMap[file]!!)
            refactoringMap.put(file, map)
        } else refactoringMap.put(file, mapOf(Pair(range, newName)))
    }

    when (searchingElement) {
        is KtClassOrObject -> getJavaRefactoringMapForClassOrObject(searchingElement, project, 
                newName, ::addToRefactoringMap)
        is KtNamedFunction -> getJavaRefactoringMapForNamedFunction(searchingElement, project, 
                newName, ::addToRefactoringMap)
        is KtProperty -> getJavaRefactoringMapForProperty(searchingElement, project,
                newName, ::addToRefactoringMap)
    }

    return refactoringMap
}

private fun getJavaRefactoringMapForProperty(searchingElement: KtProperty,
                                             project: Project,
                                             newName: String,
                                             addToRefactoringMap: (FileObject, OffsetRange, String) -> Unit) {
    val name = searchingElement.name?.toString() ?: return
    
    val getterName = "get${name.capitalize()}"
    val setterName = "set${name.capitalize()}"
    
    val newGetterName = "get${newName.capitalize()}"
    val newSetterName = "set${newName.capitalize()}"
    
    val getter = getClassMethod(searchingElement, project, getterName)
    val setter = getClassMethod(searchingElement, project, setterName, 1)
    
    addMemberUsagesToRefactoringMap(getter, project, newGetterName, addToRefactoringMap)
    addMemberUsagesToRefactoringMap(setter, project, newSetterName, addToRefactoringMap)
}

private fun getJavaRefactoringMapForClassOrObject(searchingElement: KtClassOrObject,
                                                  project: Project,
                                                  newName: String,
                                                  addToRefactoringMap: (FileObject, OffsetRange, String) -> Unit) {
    val fqName = searchingElement.fqName ?: return
    val elementHandle = project.findTypeElementHandle(fqName.asString()) ?: return

    val references = JavaEnvironment.JAVA_SOURCE[project]!!.classpathInfo.
            classIndex.getResources(elementHandle,
            ClassIndex.SearchKind.values().toSet(),
            setOf(ClassIndex.SearchScope.SOURCE))

    val usagesSearcher = TypeUsagesSearcher(elementHandle)
    
    references.forEach {
        JavaSource.forFileObject(it).runUserActionTask(usagesSearcher, true)
        usagesSearcher.usages.forEach { usage ->
            addToRefactoringMap(it, usage, newName)
        }
        usagesSearcher.clearUsages()
    }
}

private fun getJavaRefactoringMapForNamedFunction(searchingElement: KtNamedFunction,
                                                  project: Project,
                                                  newName: String,
                                                  addToRefactoringMap: (FileObject, OffsetRange, String) -> Unit) {
    val methodName = searchingElement.name ?: return
    val numberOfValueParameters = searchingElement.valueParameters.size

    val method = getClassMethod(searchingElement, project, methodName, numberOfValueParameters)
    
    addMemberUsagesToRefactoringMap(method, project, newName, addToRefactoringMap)
}

private fun addMemberUsagesToRefactoringMap(method: ElementHandle<*>?,
                                            project: Project,
                                            newName: String,
                                            addToRefactoringMap: (FileObject, OffsetRange, String) -> Unit) {
    if (method == null) return
    
    JavaEnvironment.checkJavaSource(project)
    JavaEnvironment.JAVA_SOURCE[project]!!.runUserActionTask({
        it.toPhase(JavaSource.Phase.RESOLVED)

        JavaRefactoringUtils.getInvocationsOf(method, it)
                .forEach { handle ->
                    val file = handle.fileObject
                    JavaSource.forFileObject(file).runUserActionTask({ fileCC ->
                        val treePath = handle.resolve(fileCC)
                        val start = fileCC.trees.sourcePositions.
                                getStartPosition(fileCC.compilationUnit, treePath.leaf)
                        val end = fileCC.trees.sourcePositions.
                                getEndPosition(fileCC.compilationUnit, treePath.leaf)

                        val range = getOffsetOfMethodInvocation(file, start.toInt(), end.toInt())
                        if (range != null) {
                            addToRefactoringMap(file, range, newName)
                        }
                    }, true)
                }
    }, true)
}

private fun getClassMethod(searchingElement: KtDeclaration,
                           project: Project,
                           methodName: String,
                           numberOfValueParameters: Int = 0): ElementHandle<*>? {
    val classOrObject = searchingElement.containingClassOrObject?.fqName ?:
            NoResolveFileClassesProvider.getFileClassFqName(searchingElement.getContainingKtFile())
    val elementHandle = project.findTypeElementHandle(classOrObject.asString()) ?: return null
    val methods = elementHandle.getMethodsHandles(project)

    val methodToFind = methods.filter { it.getName(project).toString() == methodName }
            .filter { it.getElementHandleValueParameters(project).size == numberOfValueParameters }
            .firstOrNull() ?: return null
    
    return methodToFind
}

private fun getOffsetOfMethodInvocation(fo: FileObject,
                                        start: Int,
                                        end: Int): OffsetRange? {
    val doc = ProjectUtils.getDocumentFromFileObject(fo) ?: return null
    val text = doc.getText(start, end - start)

    val startIndex = text.lastIndexOf(".") + 1

    return OffsetRange(start + startIndex, end)
}

fun createPositionBoundsForFO(fo: FileObject, ranges: Map<OffsetRange, String>): List<Pair<PositionBounds,String>> {
    val ces = GsfUtilities.findCloneableEditorSupport(fo) ?: return emptyList()

    return ranges.map { 
        Pair(
            PositionBounds(
                    ces.createPositionRef(it.key.start, Position.Bias.Forward),
                    ces.createPositionRef(it.key.end, Position.Bias.Forward)
            ),
            it.value
        )
    }
}

fun transaction(renameMap: Map<FileObject, Map<OffsetRange, String>>): Transaction {
    val result = ModificationResult()
    for ((file, rangeAndName) in renameMap) {
        val posBounds = createPositionBoundsForFO(file, rangeAndName)
        result.addDifferences(file, posBounds.map { Difference(Difference.Kind.CHANGE, it.first.begin, it.first.end, "", it.second) })
    }

    return RefactoringCommit(listOf(result))
}