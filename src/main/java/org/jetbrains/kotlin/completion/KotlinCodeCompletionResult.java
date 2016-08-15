/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.completion;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinCodeCompletionResult extends CodeCompletionResult {

    private List<CompletionProposal> proposals = Lists.newArrayList();
    private final StyledDocument doc;

    public KotlinCodeCompletionResult(Document doc, int offset, AnalysisResultWithProvider analysisResultWithProvider) {
        this.doc = (StyledDocument) doc;
        try {
            proposals = KotlinCompletionUtils.INSTANCE.createProposals(doc, offset, analysisResultWithProvider);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public List<CompletionProposal> getItems() {
        return proposals;
    }

    @Override
    public boolean isTruncated() {
        return false;
    }

    @Override
    public boolean isFilterable() {
        return true;
    }

    @Override
    public boolean insert(CompletionProposal item) {
        KotlinCompletionProposal proposal = (KotlinCompletionProposal) item;
        
        proposal.doInsert();

        return true;
    }

}
