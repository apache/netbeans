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
import static org.netbeans.modules.javascript2.vue.editor.embedding.VueCssEmbeddingProvider.TARGET_MIME_TYPE;
import org.netbeans.modules.javascript2.vue.editor.lexer.VueTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;


@EmbeddingProvider.Registration(
        mimeType = VueLanguage.MIME_TYPE,
        targetMimeType = TARGET_MIME_TYPE)
public class VueCssEmbeddingProvider extends EmbeddingProvider {

    public static final String TARGET_MIME_TYPE = "text/css"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<?> ts = tokenHierarchy.tokenSequence();

        if (ts == null || !ts.isValid()) {
            return Collections.emptyList();
        }

        ts.moveStart();

        List<Embedding> embeddings = new ArrayList<>();

        while (ts.moveNext()) {
            Token<?> token = ts.token();
            TokenId id = token.id();
            if (id.equals(VueTokenId.CSS)) {
                embeddings.add(snapshot.create(ts.offset(), token.length(), TARGET_MIME_TYPE));
            }
        }

        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Embedding.create(embeddings));
    }

    @Override
    public int getPriority() {
        return 220;
    }

    @Override
    public void cancel() {
        // nothing so far
    }
}
