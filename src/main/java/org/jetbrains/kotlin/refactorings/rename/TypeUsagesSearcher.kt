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

import com.sun.source.tree.ClassTree
import com.sun.source.tree.ImportTree
import com.sun.source.tree.InstanceOfTree
import com.sun.source.tree.MethodTree
import com.sun.source.tree.NewClassTree
import com.sun.source.tree.ParameterizedTypeTree
import com.sun.source.tree.Tree
import com.sun.source.tree.TypeCastTree
import com.sun.source.tree.TypeParameterTree
import com.sun.source.tree.VariableTree
import com.sun.source.util.TreePath
import com.sun.source.util.TreePathScanner
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeVariable
import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.api.java.source.CancellableTask
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.JavaSource
import org.netbeans.modules.csl.api.OffsetRange

class TypeUsagesSearcher(val toFind: ElementHandle<TypeElement>) : TreePathScanner<Tree, ElementHandle<*>>(),
        CancellableTask<CompilationController> {

    private lateinit var info: CompilationController
    val usages = hashSetOf<OffsetRange>()

    fun clearUsages() = usages.clear()

    override fun cancel() {
    }

    override fun run(info: CompilationController) {
        info.toPhase(JavaSource.Phase.RESOLVED)
        this.info = info

        val treePath = TreePath(info.compilationUnit)
        scan(treePath, toFind)
    }

    override fun visitMethod(node: MethodTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitMethod(node, handle)
        val el = info.trees.getElement(currentPath) as? ExecutableElement ?: return super.visitMethod(node, handle)

        addUsageIfApplicable(e, { el.returnType == e.asType() }, { indexOf(it) })

        return super.visitMethod(node, handle)
    }

    override fun visitVariable(node: VariableTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitVariable(node, handle)
        val el = info.trees.getElement(currentPath) ?: return super.visitVariable(node, handle)

        addUsageIfApplicable(e, { el.asType() == e.asType() }, { indexOf(it) })

        return super.visitVariable(node, handle)
    }

    override fun visitNewClass(node: NewClassTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitNewClass(node, handle)
        val el = info.trees.getElement(currentPath) ?: return super.visitNewClass(node, handle)

        addUsageIfApplicable(e, { el.enclosingElement.asType() == e.asType() })

        return super.visitNewClass(node, handle)
    }

    override fun visitTypeCast(node: TypeCastTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitTypeCast(node, handle)

        addUsageIfApplicable(e, { it.substring(1, it.indexOf(")")).endsWith(e.simpleName) })

        return super.visitTypeCast(node, handle)
    }

    override fun visitInstanceOf(node: InstanceOfTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info)

        addUsageIfApplicable(e, { it.substringAfterLast(" ").endsWith(e.simpleName) })

        return super.visitInstanceOf(node, handle)
    }

    override fun visitImport(node: ImportTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) as? TypeElement ?: return super.visitImport(node, handle)

        addUsageIfApplicable(e, { it.endsWith("${e.qualifiedName};") })

        return super.visitImport(node, handle)
    }

    override fun visitClass(node: ClassTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitClass(node, handle)
        val el = info.trees.getElement(currentPath) as? TypeElement ?: return super.visitClass(node, handle)

        addUsageIfApplicable(e, { el.interfaces.contains(e.asType()) || el.superclass == e.asType() })

        return super.visitClass(node, handle)
    }

    override fun visitTypeParameter(node: TypeParameterTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitTypeParameter(node, handle)
        val el = info.trees.getElement(currentPath) as? TypeParameterElement ?: return super.visitTypeParameter(node, handle)
        
        addUsageIfApplicable(e, { el.bounds.contains(e.asType()) })
        
        return super.visitTypeParameter(node, handle)
    }
    
    override fun visitParameterizedType(node: ParameterizedTypeTree?, handle: ElementHandle<*>): Tree? {
        val e = handle.resolve(info) ?: return super.visitParameterizedType(node, handle)
        
        addUsageIfApplicable(e, { currentPath.leaf.toString().contains(e.simpleName) })
        
        return super.visitParameterizedType(node, handle)
    }
    
    private fun addUsageIfApplicable(e: Element,
                                     condition: (String) -> Boolean,
                                     index: String.(String) -> Int = { lastIndexOf(it) }) {
        val start = info.trees.sourcePositions.
                getStartPosition(info.compilationUnit, currentPath.leaf).toInt()
        val end = info.trees.sourcePositions.
                getEndPosition(info.compilationUnit, currentPath.leaf).toInt()

        if (end - start <= 0) return
        
        val doc = ProjectUtils.getDocumentFromFileObject(info.fileObject) ?: return
        val text = doc.getText(start, end - start)

        if (condition(text)) {
            val startIndex = start + text.index(e.simpleName.toString())
            val endIndex = startIndex + e.simpleName.toString().length

            usages.add(OffsetRange(startIndex, endIndex))
        }
    }
}