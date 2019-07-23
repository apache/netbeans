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
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.modules.refactoring.api.Problem
import org.netbeans.modules.refactoring.api.RenameRefactoring
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag
import org.netbeans.modules.refactoring.spi.RefactoringPlugin

class KotlinRenameRefactoring(val refactoring: RenameRefactoring) : ProgressProviderAdapter(), RefactoringPlugin {
    
    override fun prepare(bag: RefactoringElementsBag): Problem? {
        val newName = refactoring.newName
        val fo = ProjectUtils.getFileObjectForDocument(refactoring.refactoringSource.lookup(StyledDocument::class.java)) ?: return null
        val psi = refactoring.refactoringSource.lookup(PsiElement::class.java)
        
        val renameMap = getRenameRefactoringMap(fo, psi, newName)
        bag.registerTransaction(transaction(renameMap))
        bag.session.doRefactoring(true)
        
        return null
    }

    override fun checkParameters() = null

    override fun preCheck(): Problem? {
        val psi = refactoring.refactoringSource.lookup(PsiElement::class.java)
        val ktElement: KtElement = PsiTreeUtil.getNonStrictParentOfType(psi, KtElement::class.java) ?: return null
        
        if (ktElement !is KtClassOrObject 
                && ktElement !is KtNamedFunction 
                && ktElement !is KtProperty
                && ktElement !is KtParameter) return Problem(true, "")
                
        if (psi.text.contains(" ") || psi.text == "interface"
                || psi.text == "class" || psi.text == "fun" 
                || psi.text == "package") return Problem(true, "")
        
        return null
    }

    override fun fastCheckParameters() = null

    override fun cancelRequest() {}
}