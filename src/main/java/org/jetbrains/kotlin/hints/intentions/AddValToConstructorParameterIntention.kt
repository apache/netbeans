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
package org.jetbrains.kotlin.hints.intentions

import com.intellij.psi.PsiElement
import javax.swing.text.Document
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format

class AddValToConstructorParameterIntention(doc: Document,
                                            analysisResult: AnalysisResult?,
                                            psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {
    
    private var parameter: KtParameter? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        parameter = psi.getNonStrictParentOfType(KtParameter::class.java) ?: return false
        val element = parameter ?: return false
        
        if (!canInvoke(element)) return false
        if (element.getNonStrictParentOfType(KtClass::class.java)?.isData() ?: false) return false
        
        return element.nameIdentifier?.textRange != null
    }

    override fun getDescription() = "Add val to parameter '${parameter?.name ?: ""}'"

    override fun implement() {
        val element = parameter ?: return
        
        val startOffset = element.textRange.startOffset
        
        doc.insertString(startOffset, "val ", null)
    }

    fun canInvoke(element: KtParameter): Boolean {
        return element.valOrVarKeyword == null && (element.parent as? KtParameterList)?.parent is KtPrimaryConstructor
    }
    
}