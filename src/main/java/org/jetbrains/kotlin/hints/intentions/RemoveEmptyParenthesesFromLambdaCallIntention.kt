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
import org.jetbrains.kotlin.analyzer.AnalysisResult
import javax.swing.text.Document
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult

class RemoveEmptyParenthesesFromLambdaCallIntention(doc: Document,
                                                    analysisResult: AnalysisResult?,
                                                    psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {
    
    private var expression: KtValueArgumentList? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtValueArgumentList::class.java) ?: return false
        val element = expression ?: return false
        
        if (element.arguments.isNotEmpty()) return false
        val parent = element.parent as? KtCallExpression ?: return false
        
        return (parent.lambdaArguments.count() == 1)
    }

    override fun getDescription() = "Remove unnecessary parentheses from function call with lambda"

    override fun implement() {
        val element = expression ?: return
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.remove(startOffset, lengthToDelete)
    }
}