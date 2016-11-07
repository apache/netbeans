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
package org.jetbrains.kotlin.navigation.references

import java.util.ArrayList
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelectorOrThis
import org.jetbrains.kotlin.psi.psiUtil.getAssignmentByLHS
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtAnnotatedExpression
import org.jetbrains.kotlin.psi.KtLabeledExpression
import org.jetbrains.kotlin.psi.KtUnaryExpression
import org.jetbrains.kotlin.utils.addToStdlib.constant

inline private fun <reified T> ArrayList<KotlinReference>.register(e: KtElement, action: (T) -> KotlinReference) {
    if (e is T) this.add(action(e))
}

inline private fun <reified T> ArrayList<KotlinReference>.registerMulti(e: KtElement, action: (T) -> List<KotlinReference>) {
    if (e is T) this.addAll(action(e))
}

private fun KtExpression.readWriteAccess(): ReferenceAccess {
    var expression = getQualifiedExpressionForSelectorOrThis()
    loop@ while(true) {
        val parent = expression.parent
        when(parent) {
            is KtParenthesizedExpression, is KtAnnotatedExpression, is KtLabeledExpression -> expression = parent as KtExpression
            else -> break@loop
        }
    }
    
    val assignment = expression.getAssignmentByLHS()
    if (assignment != null) {
        when (assignment.operationToken) {
            KtTokens.EQ -> return ReferenceAccess.WRITE
            else -> return ReferenceAccess.READ_WRITE
        }
    }
    
    return if ((expression.parent as? KtUnaryExpression)?.operationToken in constant { setOf(KtTokens.PLUSPLUS, KtTokens.MINUSMINUS) })
        ReferenceAccess.READ_WRITE
    else
        ReferenceAccess.READ
}

fun createReferences(element: KtReferenceExpression): List<KotlinReference> {
    return arrayListOf<KotlinReference>().apply {
        register<KtSimpleNameExpression>(element, ::KotlinSimpleNameReference)
        
        register<KtCallExpression>(element, ::KotlinInvokeFunctionReference)
        
        register<KtConstructorDelegationReferenceExpression>(element, ::KotlinConstructorDelegationReference)
        
        registerMulti<KtNameReferenceExpression>(element) {
            if (it.getReferencedNameElementType() != KtTokens.IDENTIFIER) return@registerMulti emptyList()
            
            when (it.readWriteAccess()) {
                ReferenceAccess.READ -> listOf(KotlinSyntheticPropertyAccessorReference.Getter(it))
                ReferenceAccess.WRITE -> listOf(KotlinSyntheticPropertyAccessorReference.Setter(it))
                ReferenceAccess.READ_WRITE -> listOf(
                            KotlinSyntheticPropertyAccessorReference.Getter(it), 
                            KotlinSyntheticPropertyAccessorReference.Setter(it))
            }
        }
    }
}