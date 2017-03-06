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
package org.jetbrains.kotlin.hints

import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.netbeans.modules.csl.api.Hint
import org.netbeans.modules.csl.api.HintSeverity
import org.netbeans.modules.csl.api.OffsetRange

fun getSmartCastHover(expression: KtSimpleNameExpression, parserResult: KotlinParserResult): Hint? {
    val analysisResult = parserResult.analysisResult ?: return null
    val bindingContext = analysisResult.analysisResult.bindingContext
    
    val parentExpression = expression.parent
    
    if (parentExpression is KtThisExpression || parentExpression is KtSuperExpression) return null

    bindingContext[BindingContext.REFERENCE_TARGET, expression]?.let {
        if (it is ConstructorDescriptor) it.containingDeclaration else it
    } ?: return null

    val smartCast = bindingContext.get(BindingContext.SMARTCAST, expression)
    
    val description = smartCast?.defaultType?.let { "Smart cast to ${DescriptorRenderer.FQ_NAMES_IN_TYPES.renderType(it)}" } ?: return null
    
    return Hint(KotlinRule(HintSeverity.INFO),
            description,
            parserResult.snapshot.source.fileObject,
            OffsetRange(expression.textRange.startOffset, expression.textRange.endOffset),
            null,
            20
    )
}