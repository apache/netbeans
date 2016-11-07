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

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getCall
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.psi.Call
import org.jetbrains.kotlin.resolve.calls.model.VariableAsFunctionResolvedCall
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.synthetic.SyntheticJavaPropertyDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import com.intellij.util.SmartList
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.psi.KtConstructorDelegationReferenceExpression

interface KotlinReference {
    val referenceExpression: KtReferenceExpression

    fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor>
}

open class KotlinSimpleNameReference(override val referenceExpression: KtReferenceExpression) : KotlinReference {
    override fun getTargetDescriptors(context: BindingContext) = referenceExpression.getReferenceTargets(context)
}

class KotlinInvokeFunctionReference(override val referenceExpression: KtCallExpression) : KotlinReference {
    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        val call = referenceExpression.getCall(context)
        val resolvedCall = referenceExpression.getResolvedCall(context)
        
        return when {
            resolvedCall is VariableAsFunctionResolvedCall -> listOf(resolvedCall.functionCall.candidateDescriptor)
            call != null && resolvedCall != null && call.callType == Call.CallType.INVOKE -> listOf(resolvedCall.candidateDescriptor)
            else -> emptyList()
        }
    }
}

sealed class KotlinSyntheticPropertyAccessorReference(override val referenceExpression: KtNameReferenceExpression, private val getter: Boolean) 
        : KotlinSimpleNameReference(referenceExpression) {
    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        val descriptors = super.getTargetDescriptors(context)
        if (descriptors.none { it is SyntheticJavaPropertyDescriptor }) return emptyList()
        
        val result = SmartList<FunctionDescriptor>()
        for (descriptor in descriptors) {
            if (descriptor is SyntheticJavaPropertyDescriptor) {
                if (getter) {
                    result.add(descriptor.getMethod)
                } else {
                    result.addIfNotNull(descriptor.setMethod)
                }
            }
        }
        return result
    }
    
    class Getter(expression: KtNameReferenceExpression) : KotlinSyntheticPropertyAccessorReference(expression, true)
    class Setter(expression: KtNameReferenceExpression) : KotlinSyntheticPropertyAccessorReference(expression, false)
}

class KotlinConstructorDelegationReference(override val referenceExpression: KtConstructorDelegationReferenceExpression) : KotlinReference {
    override fun getTargetDescriptors(context: BindingContext) = referenceExpression.getReferenceTargets(context)
}

enum class ReferenceAccess(val isRead: Boolean, val isWrite: Boolean) {
    READ(true, false), WRITE(false, true), READ_WRITE(true, true)
}