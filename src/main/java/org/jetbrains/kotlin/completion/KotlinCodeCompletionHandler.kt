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

import java.util.concurrent.Callable
import javax.swing.text.Document
import javax.swing.text.JTextComponent
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.utils.ProjectUtils
import org.netbeans.modules.csl.api.CodeCompletionContext
import org.netbeans.modules.csl.api.CodeCompletionHandler2
import org.netbeans.modules.csl.api.CodeCompletionResult
import org.netbeans.modules.csl.api.CompletionProposal
import org.netbeans.modules.csl.api.Documentation
import org.netbeans.modules.csl.api.ElementHandle
import org.netbeans.modules.csl.api.ParameterInfo
import org.netbeans.modules.csl.spi.ParserResult
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType

class KotlinCodeCompletionHandler : CodeCompletionHandler2 {

    override fun documentElement(info: ParserResult, element: ElementHandle,
                                 cancel: Callable<Boolean>): Documentation {
        return if (element is ElementHandle.UrlHandle)
            return Documentation.create(element.url)
        else Documentation.create("")
    }

    override fun document(info: ParserResult, element: ElementHandle) = ""

    override fun resolveLink(link: String, handle: ElementHandle) = null

    override fun getPrefix(info: ParserResult, caretOffset: Int, upToOffset: Boolean) = null

    override fun resolveTemplateVariable(variable: String, info: ParserResult, caretOffset: Int,
                                         name: String, parameters: Map<*, *>) = null

    override fun getApplicableTemplates(doc: Document, selectionBegin: Int, selectionEnd: Int) = emptySet<String>()

    override fun parameters(info: ParserResult, caretOffset: Int, proposal: CompletionProposal) = ParameterInfo.NONE

    override fun getAutoQuery(component: JTextComponent, typedText: String): QueryType {
        if (typedText.isNotEmpty()) {
            if (typedText.endsWith(".")) return QueryType.COMPLETION
        }
        return QueryType.NONE
    }

    override fun complete(context: CodeCompletionContext): CodeCompletionResult? {
        val parserResult = context.parserResult as KotlinParserResult
        val file = parserResult.snapshot.source.fileObject

        val doc = ProjectUtils.getDocumentFromFileObject(file)
        val caretOffset = context.caretOffset
        val analysisResultWithProvider = parserResult.analysisResult ?: return null
        val prefix = context.prefix ?: ""

        return KotlinCodeCompletionResult(doc, caretOffset, analysisResultWithProvider, prefix)
    }

}