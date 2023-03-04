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

package org.netbeans.modules.html.editor.xhtml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.web.common.api.Constants;

/**
 * Embedding provider for Expression Language.
 * TODO: merge with XhtmlElEmbeddingProvider ?
 *
 * @author Erno Mononen
 */
final class ELEmbeddingProvider extends EmbeddingProvider {

    private static final String ATTRIBUTE_EL_MARKER = "A"; //NOI18N
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<XhtmlElTokenId> sequence = th.tokenSequence(XhtmlElTokenId.language());
        List<Embedding> embeddings = new ArrayList<>();
        sequence.moveStart();
        boolean htmlSectionEndsWithQuotation = false;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            //unbelievable hack
            if(t.id() == XhtmlElTokenId.HTML) {
                char c = t.text().charAt(t.length() - 1);
                htmlSectionEndsWithQuotation = c == '"' || c == '\'';
            }
            
            if (t.id() == XhtmlElTokenId.EL) {
                embeddings.add(snapshot.create(sequence.offset(), t.length(), "text/x-el")); //NOI18N
                
                if(htmlSectionEndsWithQuotation) {
                    //it *looks like* the EL is inside an attribute
                    //there's a need to distinguish between ELs inside or outside of attribute values
                    embeddings.add(snapshot.create(ATTRIBUTE_EL_MARKER, "text/x-el")); //NOI18N
                }
                
                // just to separate expressions for easier handling in EL parser
                embeddings.add(snapshot.create(Constants.LANGUAGE_SNIPPET_SEPARATOR, "text/x-el")); //NOI18N
            }
        }
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void cancel() {
    }

}
