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
package org.jetbrains.kotlin.completion

import javax.swing.ImageIcon
import javax.swing.text.StyledDocument
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.KotlinImageProvider
import org.netbeans.modules.csl.api.ElementHandle
import org.netbeans.modules.csl.api.HtmlFormatter
import org.netbeans.modules.csl.spi.DefaultCompletionProposal

class KotlinCompletionProposal(val idenStartOffset: Int, caretOffset: Int, 
                               val descriptor: DeclarationDescriptor, val doc: StyledDocument,
                               val prefix: String) : DefaultCompletionProposal() {

    val text: String
    val proposal: String
    val type: String
    val proposalName: String
    val FIELD_ICON: ImageIcon
    
    init {
        text = descriptor.name.identifier
        proposal = DescriptorRenderer.ONLY_NAMES_WITH_SHORT_TYPES.render(descriptor)
        FIELD_ICON = KotlinImageProvider.INSTANCE.getImage(descriptor)
        val splitted = proposal.split(":")
        proposalName = splitted[0]
        type = if (splitted.size > 1) splitted[1] else ""
    }
    
    override fun getElement() = null
    override fun getLhsHtml(formatter: HtmlFormatter) = proposalName
    override fun getRhsHtml(formatter: HtmlFormatter) = type
    override fun getName() = proposalName
    override fun getInsertPrefix() = proposalName
    override fun getSortText() = proposalName
    override fun getAnchorOffset() = idenStartOffset
    override fun getIcon() = FIELD_ICON
    
    override fun getSortPrioOverride(): Int {
        return when(descriptor) {
            is VariableDescriptor -> 20
            is FunctionDescriptor -> 30
            is ClassDescriptor -> 40
            is PackageFragmentDescriptor -> 10
            is PackageViewDescriptor -> 10
            else -> 150
        }
    }
    
    private fun functionAction(doc: StyledDocument) {
        val functionDescriptor = descriptor as FunctionDescriptor
        val params = functionDescriptor.valueParameters
        
        doc.remove(idenStartOffset, prefix.length)
        
        if (params.size == 1) {
            if (name.contains("->")) {
                doc.insertString(idenStartOffset, text + "{  }", null)
                return
            }
        }
        
        val functionParams = StringBuilder().append("(")
        params.forEach { functionParams.append(getValueParameter(it)).append(",") }
        
        if (params.size > 0) {
            functionParams.deleteCharAt(functionParams.length - 1)
        }
        functionParams.append(")")
        doc.insertString(idenStartOffset, text + functionParams.toString(), null)
    }
    
    private fun getValueParameter(desc: ValueParameterDescriptor): String {
        val kotlinType = desc.type
        val classifierDescriptor = kotlinType.constructor.declarationDescriptor
        
        if (classifierDescriptor == null) return desc.name.asString()
        
        val typeName = classifierDescriptor.name.asString()
        return KotlinCompletionUtils.INSTANCE.getValueForType(typeName) ?: desc.name.asString()
    }
    
    fun doInsert() {
        if (descriptor is FunctionDescriptor) {
            functionAction(doc)
        } else {
            doc.remove(idenStartOffset, prefix.length)
            doc.insertString(idenStartOffset, text, null)
        }
    }
    
}