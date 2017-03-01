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
package org.jetbrains.kotlin.navigation.netbeans

import javax.swing.text.Document
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lang.java.getJavaDoc
import org.jetbrains.kotlin.resolve.lang.java.resolver.NetBeansJavaSourceElement
import org.jetbrains.kotlin.resolve.lang.java.structure.*
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.utils.ProjectUtils

fun getSmartCastHover(offset: Int): String? {
    val ktFile = KotlinParser.file ?: return null
    val analysisResult = KotlinParser.getAnalysisResult() ?: return null
    val bindingContext = analysisResult.analysisResult.bindingContext
    
    val element = ktFile.findElementAt(offset)
    val expression = element?.getNonStrictParentOfType(KtSimpleNameExpression::class.java) ?: return null
    val parentExpression = expression.parent
    
    if (parentExpression is KtThisExpression || parentExpression is KtSuperExpression) return null

    val target = bindingContext[BindingContext.REFERENCE_TARGET, expression]?.let {
        if (it is ConstructorDescriptor) it.getContainingDeclaration() else it
    } ?: return null

    val smartCast = bindingContext.get(BindingContext.SMARTCAST, expression)
    
    return smartCast?.defaultType?.let { "Smart cast to ${DescriptorRenderer.FQ_NAMES_IN_TYPES.renderType(it)}" }
}

fun getToolTip(referenceExpression: KtReferenceExpression?,
               doc: Document, offset: Int): String {
    val smartCast = getSmartCastHover(offset) ?: ""
    referenceExpression ?: return smartCast
    
    val file = ProjectUtils.getFileObjectForDocument(doc) ?: return smartCast
    val project = ProjectUtils.getKotlinProjectForFileObject(file) ?: ProjectUtils.getValidProject() ?: return smartCast
    
    val navigationData = getNavigationData(referenceExpression, project) ?: return smartCast
    val sourceElement = navigationData.sourceElement
    
    when (sourceElement) {
        is KotlinSourceElement -> {
            val descriptor = navigationData.descriptor
            return "${descriptor.toString().substringBefore(" defined in")}${if (smartCast != "") "\n\n$smartCast" else ""}"
        }
        is NetBeansJavaSourceElement -> {
            val handle = sourceElement.getElementBinding()
            val javaDoc = handle.getJavaDoc(project)?.commentText()?.let { "\n\n$it" } ?: ""
            
            val element = sourceElement.javaElement
            when (element) {
                is NetBeansJavaClass -> return "${element.presentation()}$javaDoc"
                is NetBeansJavaMember<*> -> return "${element.presentation()}$javaDoc"
            }
            
        }
    }
    
    return ""
}