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
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtDeclaration
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.NetBeansDescriptorUtils
import org.openide.filesystems.FileUtil
import java.io.File
import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.resolve.KotlinAnalyzer

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

fun List<SourceElement>.getContainingClassOrObjectForConstructor() 
        = this.filterIsInstance(KotlinSourceElement::class.java)
              .map { it.psi }
              .filterIsInstance(KtConstructor::class.java)
              .map { KotlinSourceElement(it.getContainingClassOrObject()) }

fun KtReferenceExpression.getReferenceTargets(context: BindingContext): Collection<DeclarationDescriptor> {
    val targetDescriptor = context[BindingContext.REFERENCE_TARGET, this]
    return if (targetDescriptor != null) {
            listOf(targetDescriptor) 
        } else {
            context[BindingContext.AMBIGUOUS_REFERENCE_TARGET, this].orEmpty()
        }
}

fun PsiElement.getReferenceExpression(): KtReferenceExpression? = PsiTreeUtil.getNonStrictParentOfType(this, KtReferenceExpression::class.java)

fun KtElement.resolveToSourceDeclaration(): List<SourceElement> {
    return when (this) {
        is KtDeclaration -> listOf(KotlinSourceElement(this))
        
        else -> {
            val referenceExpression = this.getReferenceExpression()
            if (referenceExpression == null) return emptyList()
            
            val reference = createReferences(referenceExpression)
            reference.resolveToSourceElements()
        } 
    }
}

fun List<KotlinReference>.resolveToSourceElements(): List<SourceElement> {
    if (isEmpty()) return emptyList()
    
    val ktFile = first().referenceExpression.getContainingKtFile()
    val path = ktFile.virtualFile.canonicalPath
    
    val normalizedPath = FileUtil.normalizePath(path)
    
    val file = FileUtil.toFileObject(File(normalizedPath)) ?: return emptyList()
    val project = ProjectUtils.getKotlinProjectForFileObject(file) ?: return emptyList()
    
    return this.resolveToSourceElements(
            KotlinAnalyzer.analyzeFile(project, ktFile).analysisResult.bindingContext, project)
}

fun List<KotlinReference>.resolveToSourceElements(context: BindingContext, project: Project) = flatMap { it.getTargetDescriptors(context) }
            .flatMap { NetBeansDescriptorUtils.descriptorToDeclarations(it, project) }