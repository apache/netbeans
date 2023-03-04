/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor.spi.embedding;

import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * PROTOTYPE of an extension of the {@link JsEmbeddingProvider}.
 * 
 * The {@link JsEmbeddingProvider} is lexer based so no parser result can be used here.
 * 
 * Register the plugin into mime lookup.
 * 
 * TODO: Possibly make the processing parser based.
 *
 * @since 2.21
 * @author marekfukala
 */
public abstract class JsEmbeddingProviderPlugin {
  
    /**
     * Called before the first call to {@link #processToken(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)}.
     * 
     * Clients may initialize resources here.
     * 
     * @param snapshot
     * @param ts
     * @param embeddings 
     * @since 2.32
     * @return true if this plugin is interested in processing the token sequence, false otherwise.
     */
    public abstract boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings);
    
    /**
     * Called after the last call to {@link #processToken(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)}.
     * 
     * Clients may release used resources here.
     * 
     * @since 2.28
     */
    public void endProcessing() {
    }
    
    /**
     * Adds one or more embeddings for the active token of the given token sequence.
     * 
     * The {@link TokenSequence<HTMLTokenId>} passed to the {@link #startProcessing(org.netbeans.modules.parsing.api.Snapshot, org.netbeans.api.lexer.TokenSequence, java.util.List)} method
     * is properly positioned before calling this method. The client must not call moveNext/Previous() methods on the 
     * token sequence itself. The client may obtain embedded token sequences and reposition these freely.
     * 
     * @return true if it embedding(s) were created or false if not.
     */
    public abstract boolean processToken();
    
}
