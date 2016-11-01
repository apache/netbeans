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

import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.idea.util.CallTypeAndReceiver
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.lang.java.findTypes
import org.netbeans.api.project.Project
import org.netbeans.api.java.source.ElementHandle as JavaElementHandle
import javax.lang.model.element.TypeElement
import org.netbeans.modules.csl.api.ElementHandle
import org.netbeans.modules.csl.api.Modifier
import javax.swing.ImageIcon
import org.netbeans.modules.csl.api.ElementKind
import org.netbeans.modules.csl.api.HtmlFormatter
import org.netbeans.modules.csl.spi.DefaultCompletionProposal
import org.jetbrains.kotlin.utils.KotlinImageProvider
import javax.swing.text.Document

fun generateNonImportedCompletionProposals(identifierPart: String,
                                           ktFile: KtFile, expression: KtSimpleNameExpression,
                                           project: Project, idenOffset: Int) =
        lookupNonImportedTypes(expression, identifierPart, ktFile, project)
                .map { NonImportedCompletionProposal(identifierPart, expression, it, idenOffset) }

private fun lookupNonImportedTypes(simpleNameExpression: KtSimpleNameExpression,
                                   identifierPart: String, ktFile: KtFile,
                                   project: Project): List<JavaElementHandle<TypeElement>> {
    val callTypeAndReceiver = CallTypeAndReceiver.detect(simpleNameExpression)

    if ((callTypeAndReceiver !is CallTypeAndReceiver.TYPE &&
            callTypeAndReceiver !is CallTypeAndReceiver.DEFAULT) ||
            callTypeAndReceiver.receiver != null) {
        return emptyList()
    }

    val importsSet = ktFile.getImportDirectives()
            .mapNotNull { it.getImportedFqName()?.asString() }
            .toSet()

    return project.findTypes(identifierPart).filter { it.qualifiedName !in importsSet }
}

class NonImportedCompletionProposal(val identifierPart: String,
                                    val expression: KtSimpleNameExpression,
                                    val type: JavaElementHandle<TypeElement>, 
                                    val idenOffset: Int) : DefaultCompletionProposal(), InsertableProposal {
    
    override fun doInsert(document: Document) {
        document.remove(idenOffset, identifierPart.length)
        document.insertString(idenOffset, sortText, null)
    }

    override fun getElement() = null

    override fun getIcon() = KotlinImageProvider.INSTANCE.typeImage

    override fun getAnchorOffset() = idenOffset

    override fun getKind() = ElementKind.CLASS

    override fun getName() = type.qualifiedName

    override fun getSortText() = type.qualifiedName

    override fun getSortPrioOverride() = 50

    override fun isSmart() = false

    override fun getInsertPrefix() = type.qualifiedName

    override fun getRhsHtml(hf: HtmlFormatter?) = ""

    override fun getLhsHtml(hf: HtmlFormatter?) = "<i>${type.qualifiedName}</i>"
}