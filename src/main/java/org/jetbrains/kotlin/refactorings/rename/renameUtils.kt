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
import com.sun.source.tree.ClassTree
import com.sun.source.tree.ImportTree
import com.sun.source.tree.InstanceOfTree
import com.sun.source.tree.MemberReferenceTree
import com.sun.source.tree.MemberSelectTree
import com.sun.source.tree.MethodTree
import com.sun.source.tree.NewClassTree
import com.sun.source.tree.Tree
import com.sun.source.tree.TypeCastTree
import com.sun.source.tree.VariableTree
import com.sun.source.util.TreePath
import com.sun.source.util.TreePathScanner
import java.io.File
import java.util.ArrayList
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.swing.text.Position
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.fileClasses.*
import org.jetbrains.kotlin.highlighter.occurrences.*
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.navigation.references.resolveToSourceDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.java.source.CancellableTask
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
    val refactoringMap = hashMapOf<FileObject, ArrayList<OffsetRange>>()

    fun addToRefactoringMap(file: FileObject, range: OffsetRange) {
        if (refactoringMap.containsKey(file)) {
            val list = arrayListOf(range)
            list.addAll(refactoringMap[file]!!)
            refactoringMap.put(file, list)
        } else refactoringMap.put(file, arrayListOf(range))
    }

    when (searchingElement) {
        is KtClassOrObject -> getJavaRefactoringMapForClassOrObject(searchingElement, project, ::addToRefactoringMap)
        is KtNamedFunction -> getJavaRefactoringMapForNamedFunction(searchingElement, project, ::addToRefactoringMap)
    }

    return refactoringMap
}

private fun getJavaRefactoringMapForClassOrObject(searchingElement: KtClassOrObject,
                                                  project: Project,
                                                  addToRefactoringMap: (FileObject, OffsetRange) -> Unit) {
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
            addToRefactoringMap(it, usage)
        }
        usagesSearcher.clearUsages()
    }
}

private fun getJavaRefactoringMapForNamedFunction(searchingElement: KtNamedFunction,
                                                  project: Project,
                                                  addToRefactoringMap: (FileObject, OffsetRange) -> Unit) {
    val classOrObject = searchingElement.containingClassOrObject?.fqName ?:
            NoResolveFileClassesProvider.getFileClassFqName(searchingElement.getContainingKtFile())
    val elementHandle = project.findTypeElementHandle(classOrObject.asString()) ?: return
    val methods = elementHandle.getMethodsHandles(project)

    val methodName = searchingElement.name ?: return
    val numberOfValueParameters = searchingElement.valueParameters.size

    val methodToFind = methods.filter { it.getName(project).toString() == methodName }
            .filter { it.getElementHandleValueParameters(project).size == numberOfValueParameters }
            .firstOrNull() ?: return

    JavaEnvironment.checkJavaSource(project)
    JavaEnvironment.JAVA_SOURCE[project]!!.runUserActionTask({
        it.toPhase(JavaSource.Phase.RESOLVED)

        JavaRefactoringUtils.getInvocationsOf(methodToFind, it)
                .forEach { handle ->
                    val file = handle.fileObject
                    JavaSource.forFileObject(file).runUserActionTask({ fileCC ->
                        val treePath = handle.resolve(fileCC)
                        val start = fileCC.trees.sourcePositions.
                                getStartPosition(fileCC.compilationUnit, treePath.leaf)
                        val end = fileCC.trees.sourcePositions.
                                getEndPosition(fileCC.compilationUnit, treePath.leaf)

                        val range = getOffsetOfMethodInvocation(file, methodName, start.toInt(), end.toInt())
                        if (range != null) {
                            addToRefactoringMap(file, range)
                        }
                    }, true)
                }
    }, true)
}

private fun getOffsetOfMethodInvocation(fo: FileObject,
                                        name: String,
                                        start: Int,
                                        end: Int): OffsetRange? {
    val doc = ProjectUtils.getDocumentFromFileObject(fo) ?: return null
    val text = doc.getText(start, end - start)

    val startIndex = text.lastIndexOf(".") + 1

    return OffsetRange(start + startIndex, end)
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
        result.addDifferences(file, posBounds.map { Difference(Difference.Kind.CHANGE, it.begin, it.end, oldName, newName) })
    }

    return RefactoringCommit(listOf(result))
}

private class TypeUsagesSearcher(val toFind: ElementHandle<TypeElement>) : TreePathScanner<Tree, ElementHandle<*>>(), 
        CancellableTask<CompilationController> {
    
    private lateinit var info: CompilationController
    val usages = hashSetOf<OffsetRange>()
    
    fun clearUsages() = usages.clear()
    
    override fun cancel() {}

    override fun run(info: CompilationController) {
        info.toPhase(JavaSource.Phase.RESOLVED)
        this.info = info
        
        val treePath = TreePath(info.compilationUnit)
        scan(treePath, toFind)
    }
    
    override fun visitImport(node: ImportTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info)
        
        return super.visitImport(node, handle)
    }
    
    override fun visitClass(node: ClassTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info)
        
        return super.visitClass(node, handle)
    }
    
    override fun visitMethod(node: MethodTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info)
        
        return super.visitMethod(node, handle)
    }
    
    override fun visitVariable(node: VariableTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitVariable(node, handle)
        val el = info.trees.getElement(currentPath) ?: return super.visitVariable(node, handle)
        
        if (el.asType() == e.asType()) {
            val start = info.trees.sourcePositions.
                        getStartPosition(info.compilationUnit, currentPath.leaf).toInt()
            val end = info.trees.sourcePositions.
                        getEndPosition(info.compilationUnit, currentPath.leaf).toInt()
                
            val doc = ProjectUtils.getDocumentFromFileObject(info.fileObject) ?: return super.visitVariable(node, handle)
            val text = doc.getText(start, end - start)
                
            val endIndex = start + text.indexOf(" ")
                
            usages.add(OffsetRange(start, endIndex))
        }
        
        return super.visitVariable(node, handle)
    }
    
    override fun visitNewClass(node: NewClassTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitNewClass(node, handle)
        val el = info.trees.getElement(currentPath) ?: return super.visitNewClass(node, handle)
        
        if (el !is ExecutableElement) return super.visitNewClass(node, handle)
        
        if (el.enclosingElement.asType() == e.asType()) {
            val start = info.trees.sourcePositions.
                        getStartPosition(info.compilationUnit, currentPath.leaf).toInt()
            val end = info.trees.sourcePositions.
                        getEndPosition(info.compilationUnit, currentPath.leaf).toInt()
            
            val doc = ProjectUtils.getDocumentFromFileObject(info.fileObject) ?: return super.visitNewClass(node, handle)
            val text = doc.getText(start, end - start)
            
            val startIndex = start + text.lastIndexOf(" ") + 1
            val endIndex = start + text.indexOf("(")
            
            usages.add(OffsetRange(startIndex, endIndex))
        }
               
        return super.visitNewClass(node, handle)
    }
    
    override fun visitTypeCast(node: TypeCastTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitTypeCast(node, handle)
        
        val start = info.trees.sourcePositions.
                    getStartPosition(info.compilationUnit, currentPath.leaf).toInt()
        val end = info.trees.sourcePositions.
                    getEndPosition(info.compilationUnit, currentPath.leaf).toInt()
        
        val doc = ProjectUtils.getDocumentFromFileObject(info.fileObject) ?: return super.visitTypeCast(node, handle)
        val text = doc.getText(start, end - start)
        val textBetwenBrackets = text.substring(1, text.indexOf(")"))
        
        if (textBetwenBrackets.endsWith("${e.simpleName}")) {
            val startIndex = start + text.lastIndexOf("${e.simpleName}")
            val endIndex = start + text.indexOf(")")
            
            usages.add(OffsetRange(startIndex, endIndex))
        }
        
        return super.visitTypeCast(node, handle)
    }
    
    override fun visitInstanceOf(node: InstanceOfTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info)
        
        val start = info.trees.sourcePositions.
                    getStartPosition(info.compilationUnit, currentPath.leaf).toInt()
        val end = info.trees.sourcePositions.
                    getEndPosition(info.compilationUnit, currentPath.leaf).toInt()
        
        val doc = ProjectUtils.getDocumentFromFileObject(info.fileObject) ?: return super.visitInstanceOf(node, handle)
        val text = doc.getText(start, end - start)
        
        val className = text.substringAfterLast(" ")
        if (className.endsWith(e.simpleName)) {
            val startIndex = start + text.lastIndexOf(e.simpleName.toString())
            
            usages.add(OffsetRange(startIndex, end))
        }
        
        return super.visitInstanceOf(node, handle)
    }
    
}