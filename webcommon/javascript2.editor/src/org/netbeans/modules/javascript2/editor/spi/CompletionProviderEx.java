/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.spi;

import java.util.List;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.javascript2.editor.JsCompletionItem;

/**
 * Contributes {@CompletionProposal}s into javascript completion. Supersedes {@link CompletionProvider},
 * allows to pass more context in the {!link ProposalRequest} parameter object.
 *
 * @author sdedic
 * @since 0.84
 */
public interface CompletionProviderEx extends CompletionProvider {
    /**
     * Returns completion proposals relevant for the request.
     * @param request the completion request
     * @return list of proposals, potentially {@code null}.
     */
    List<CompletionProposal> complete(ProposalRequest request);

    /**
     * Convenience bridge for older API callers. Invokes {@link #complete(org.netbeans.modules.javascript2.editor.spi.ProposalRequest)}.
     * Do not override unless for very specific needs.
     *
     * @param ccContext parsing context
     * @param jsCompletionContext
     * @param prefix
     * @return
     */
    @Override
    default List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        return complete(JsCompletionItem.createRequest(ccContext, jsCompletionContext, prefix));
    }
}
