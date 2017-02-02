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

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.reformatting.format
import org.jetbrains.kotlin.hints.atomicChange

class RemoveBracesIntention(val parserResult: KotlinParserResult,
                            val psi: PsiElement) : ApplicableIntention {
    
    private var desc: String = "Remove braces"
    private var expression: KtElement? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtElement::class.java) ?: return false
        val element = expression ?: return false
        
        val block = element.findChildBlock() ?: return false
        val singleStatement = block.statements.singleOrNull() ?: return false
        val container = block.parent
        when (container) {
            is KtContainerNode -> {
                if (singleStatement is KtIfExpression && container.parent is KtIfExpression) return false

                val description = container.description() ?: return false
                desc = "Remove braces from '$description' statement"
                return true
            }
            is KtWhenEntry -> {
                desc = "Remove braces from 'when' entry"
                return singleStatement !is KtNamedDeclaration
            }
            else -> return false
        }
    }

    override fun getDescription() = desc

    override fun implement() {
        val element = expression ?: return
        
        val block = element.findChildBlock() ?: return
        val statement = block.statements.single()

        val doc = parserResult.snapshot.source.getDocument(false)
        
        val startOffset = block.textRange.startOffset
        val lengthToDelete = block.textLength
        
        doc.atomicChange {
            remove(startOffset, lengthToDelete)
            insertString(startOffset, statement.text, null)
            format(this, element.textRange.startOffset)
        }
    }
    
    private fun KtElement.findChildBlock() = when (this) {
        is KtBlockExpression -> this
        is KtLoopExpression -> body as? KtBlockExpression
        is KtWhenEntry -> expression as? KtBlockExpression
        else -> null
    }
    
}

fun KtContainerNode.description(): String? {
    when (node.elementType) {
        KtNodeTypes.THEN -> return "if"
        KtNodeTypes.ELSE -> return "else"
        KtNodeTypes.BODY -> {
            when (parent) {
                is KtWhileExpression -> return "while"
                is KtDoWhileExpression -> return "do...while"
                is KtForExpression -> return "for"
            }
        }
    }
    return null
}