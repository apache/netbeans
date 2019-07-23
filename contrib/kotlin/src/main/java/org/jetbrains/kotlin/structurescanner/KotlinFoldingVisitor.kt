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
package org.jetbrains.kotlin.structurescanner

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import org.netbeans.modules.csl.api.OffsetRange

class KotlinFoldingVisitor(val ktFile: KtFile) : KtVisitorVoid() {

    private val comments = arrayListOf<OffsetRange>()
    private val imports = arrayListOf<OffsetRange>()
    private val codeBlocks = arrayListOf<OffsetRange>()
    
    fun computeFolds(): Map<String, List<OffsetRange>> {
        ktFile.acceptChildren(this)
        
        val found: Collection<PsiElement> = PsiTreeUtil.findChildrenOfType(ktFile, PsiComment::class.java)
        found.mapTo(comments) { it.textRange.toOffsetRange() }
        
        return mapOf("comments" to comments, 
                "codeblocks" to codeBlocks,
                "imports" to imports)
    }
    
    override fun visitElement(element: PsiElement) = element.acceptChildren(this)
    
    override fun visitClassBody(body: KtClassBody) {
        codeBlocks.add(body.textRange.toOffsetRange())
        super.visitClassBody(body)
    }
    
    override fun visitBlockExpression(expression: KtBlockExpression) {
        codeBlocks.add(expression.textRange.toOffsetRange())
        super.visitBlockExpression(expression)
    }
    
    override fun visitImportList(importList: KtImportList) {
        imports.add(importList.textRange.toOffsetRange())
        super.visitImportList(importList)
    }
    
    private fun TextRange.toOffsetRange() = OffsetRange(startOffset, endOffset)
    
}