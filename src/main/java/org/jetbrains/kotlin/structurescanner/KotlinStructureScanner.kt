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

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiCoreCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.api.StructureItem
import org.netbeans.modules.csl.api.StructureScanner
import org.netbeans.modules.csl.spi.ParserResult
import org.openide.filesystems.FileObject
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportList
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.netbeans.modules.csl.api.StructureScanner.Configuration

class KotlinStructureScanner : StructureScanner {

    private fun getStartOffset(elem: PsiElement) = when (elem) {
        is KtNamedFunction -> elem.textRange.startOffset + elem.text.split("\\{")[0].length
        is KtImportList -> elem.textRange.startOffset + "import ".length
        else -> elem.textRange.startOffset
    }
    
    override fun getConfiguration() = Configuration(true, true)
    
    override fun scan(info: ParserResult): List<StructureItem> {
        val file = info.snapshot.source.fileObject ?: return emptyList()
        if (ProjectUtils.getKotlinProjectForFileObject(file) == null) return emptyList()
        
        val ktFile = ProjectUtils.getKtFile(file) ?: return emptyList()
        
        return ktFile.declarations.map {
            when(it) {
                is KtClass -> KotlinClassStructureItem(it, false)
                is KtNamedFunction -> KotlinFunctionStructureItem(it, false)
                is KtProperty -> KotlinPropertyStructureItem(it, false)
                else -> null
            }
        }.filterNotNull()
    }
    
    override fun folds(info: ParserResult): Map<String, List<OffsetRange>> {
        val file = info.snapshot.source.fileObject ?: return emptyMap()
        if (ProjectUtils.getKotlinProjectForFileObject(file) == null) return emptyMap()
        
        val ktFile = ProjectUtils.getKtFile(file) ?: return emptyMap()
        val elements: Collection<PsiElement> = PsiTreeUtil.findChildrenOfAnyType(ktFile, 
                KtImportList::class.java, PsiCoreCommentImpl::class.java, 
                KtNamedFunction::class.java)
        
        val comments = arrayListOf<OffsetRange>()
        val imports = arrayListOf<OffsetRange>()
        val functions = arrayListOf<OffsetRange>()
        
        for (it in elements) {
            val start = getStartOffset(it)
            val end  = it.textRange.endOffset
            if (start >= end) continue
            
            val range = OffsetRange(start, end)
            when (it) {
                is PsiCoreCommentImpl -> comments.add(range)
                is KtNamedFunction -> functions.add(range)
                is KtImportList -> imports.add(range)
            }
        }
        
        return mapOf("comments" to comments, 
                "codeblocks" to functions,
                "imports" to imports)
    }
    
}