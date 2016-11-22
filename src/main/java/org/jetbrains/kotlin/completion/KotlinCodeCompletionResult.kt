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

import javax.swing.text.Document
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.netbeans.modules.csl.api.CodeCompletionResult
import org.netbeans.modules.csl.api.CompletionProposal

/**
 *
 * @author Alexander.Baratynski
 */
class KotlinCodeCompletionResult(private val doc: Document, offset: Int,
                                 analysisResult: AnalysisResultWithProvider,
                                 prefix: String) : CodeCompletionResult() {
    
    val proposals: List<CompletionProposal>
    
    init {
        proposals = createProposals(doc, offset, analysisResult, prefix)
    }
    
    override fun getItems() = proposals
    override fun isTruncated() = false
    override fun isFilterable() = false
    
    override fun insert(item: CompletionProposal): Boolean {
        (item as InsertableProposal).doInsert(doc)
        return true
    }
}