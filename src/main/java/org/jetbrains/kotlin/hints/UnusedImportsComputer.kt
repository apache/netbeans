/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.kotlin.hints

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.language.Priorities
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.descriptors.*
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.HintSeverity
import org.netbeans.modules.csl.api.OffsetRange

class UnusedImportsComputer(private val parserResult: KotlinParserResult) : KtVisitor<Unit, Any?>() {
    
    private val ktFile = parserResult.ktFile
    private val context = parserResult.analysisResult!!.analysisResult.bindingContext
    private val usedClasses = hashSetOf<ClassDescriptor>()
    
    fun getUnusedImports(): List<Hint> {
        ktFile.accept(this)
        val unusedImports = hashSetOf<KtImportDirective>()
        
        ktFile.importDirectives
                .forEach {
                    val ref = PsiTreeUtil.findChildrenOfType(it, KtReferenceExpression::class.java).lastOrNull()
                    val target = context[BindingContext.REFERENCE_TARGET, ref]
                    if (target is ClassDescriptor && target !in usedClasses) unusedImports.add(it)
                }
        
        return unusedImports.map {
            Hint(KotlinRule(HintSeverity.WARNING), 
                    "Unused import: ${it.importedFqName}",
                    parserResult.snapshot.source.fileObject,
                    OffsetRange(it.textRange.startOffset, it.textRange.endOffset),
                    null, 
                    Priorities.HINT_PRIORITY)
        }
    }
    
    override fun visitKtFile(ktFile: KtFile, data: Any?) {
        ktFile.acceptChildren(this)
    }
    
    override fun visitKtElement(element: KtElement, data: Any?) {
        element.acceptChildren(this)
    }
    
    override fun visitImportDirective(importDirective: KtImportDirective, data: Any?) {}
    
    override fun visitReferenceExpression(expression: KtReferenceExpression, data: Any?) {
        val reference = context[BindingContext.REFERENCE_TARGET, expression]?.let { 
            if (it is ClassDescriptor) it else it.containingDeclaration
        }
        
        if (reference is ClassDescriptor) usedClasses.add(reference)
        
        expression.acceptChildren(this)
    }
    
}