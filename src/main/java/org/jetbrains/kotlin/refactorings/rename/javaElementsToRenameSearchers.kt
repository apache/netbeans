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

import com.sun.source.tree.MemberSelectTree
import com.sun.source.tree.Tree
import com.sun.source.util.TreePath
import com.sun.source.util.TreePathScanner
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.resolve.lang.java.*
import org.netbeans.api.java.source.CancellableTask
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.JavaSource.Phase
import org.netbeans.api.java.source.TreePathHandle
import org.netbeans.api.project.Project

fun getReferencesToMember(member: ElementHandle<*>, 
                          project: Project) = JavaMembersToRenameSearcher(member).execute(project).usages

class JavaMembersToRenameSearcher(private val toFind: ElementHandle<*>) : TreePathScanner<Tree, ElementHandle<*>>(), 
        CancellableTask<CompilationController> {
    
    lateinit var compilationController: CompilationController
    val usages = hashSetOf<TreePathHandle>()
    
    override fun run(compilationController: CompilationController) {
        compilationController.toPhase(Phase.RESOLVED)
        this.compilationController = compilationController
        
        val path = TreePath(compilationController.compilationUnit)
        scan(path, toFind)
    }
    
    override fun cancel() {}
    
    override fun visitMemberSelect(node: MemberSelectTree, p: ElementHandle<*>): Tree {
        val e = p.resolve(compilationController)
        addIfMatch(getCurrentPath(), node, e);
        
        return super.visitMemberSelect(node, p)
    }
    
    private fun addIfMatch(path: TreePath, tree: Tree, elementToFind: Element) {
        if (compilationController.treeUtilities.isSynthetic(path)) return
        
        val el = compilationController.trees.getElement(path) ?: return
        
        if (elementToFind.kind == ElementKind.METHOD && el.kind== ElementKind.METHOD) {
            if (el == elementToFind || (compilationController.elements
                    .overrides(el as ExecutableElement, elementToFind as ExecutableElement, 
                            elementToFind.enclosingElement as TypeElement))) {
                usages.add(TreePathHandle.create(getCurrentPath(), compilationController))
            }
        } else if (el == elementToFind) {
            usages.add(TreePathHandle.create(getCurrentPath(), compilationController))
        }
    }
    
}