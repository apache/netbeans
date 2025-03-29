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
package org.netbeans.modules.javascript2.vue.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.vue.editor.VueLanguage;
import org.netbeans.modules.javascript2.vue.editor.lexer.VueTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;

/**
 * this will enable braces matches of html elements
 * 
 * @author bhaidu
 */
@EmbeddingProvider.Registration(
        mimeType = VueLanguage.MIME_TYPE,
        targetMimeType = "text/html")
public class VueHtmlEmbeddingProvider extends EmbeddingProvider {
    public static final String FILLER = " "; //NOI18N
    public static final String TARGET_MIME_TYPE = "text/html"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(final Snapshot snapshot) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<?> ts = tokenHierarchy.tokenSequence();
        
        if (ts == null || !ts.isValid()) {
            return Collections.emptyList();
        }
        ts.moveStart();
        List<Embedding> embeddings = new ArrayList<>();

        try {
            while (ts.moveNext()) {
                Token<?> t = ts.token();
                TokenId id = t.id();

                if (id.equals(VueTokenId.HTML)) {
                    embeddings.add(snapshot.create(ts.offset(), t.length(), TARGET_MIME_TYPE));
                }
            }
        } catch (Exception ex) {
            return Collections.emptyList();
        }
        
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 210;
    }

    @Override
    public void cancel() { }
}
