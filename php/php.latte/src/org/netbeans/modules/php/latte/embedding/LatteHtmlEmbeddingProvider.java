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
package org.netbeans.modules.php.latte.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@EmbeddingProvider.Registration(mimeType = LatteLanguage.LATTE_MIME_TYPE, targetMimeType = LatteHtmlEmbeddingProvider.TARGET_MIME_TYPE)
public class LatteHtmlEmbeddingProvider extends EmbeddingProvider {
    public static final String TARGET_MIME_TYPE = "text/html"; //NOI18N
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), LatteTopTokenId.language());
        TokenSequence<LatteTopTokenId> ts = th.tokenSequence(LatteTopTokenId.language());
        if (ts == null) {
            return Collections.<Embedding>emptyList();
        }
        ts.moveStart();
        List<Embedding> embeddings = new ArrayList<>();
        int from = -1;
        int length = 0;
        while (ts.moveNext()) {
            Token<LatteTopTokenId> token = ts.token();
            if (token != null && isPureHtmlToken(token)) {
                if (from < 0) {
                    from = ts.offset();
                }
                length += token.length();
            } else {
                if (from >= 0) {
                    embeddings.add(snapshot.create(from, length, TARGET_MIME_TYPE));
                    embeddings.add(snapshot.create(GENERATED_CODE, TARGET_MIME_TYPE));
                    from = -1;
                    length = 0;
                }
            }
        }
        if (from >= 0) {
            embeddings.add(snapshot.create(from, length, TARGET_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", TARGET_MIME_TYPE)); //NOI18N
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    private boolean isPureHtmlToken(Token<LatteTopTokenId> token) {
        CharSequence tokenText = token.text();
        return token.id() == LatteTopTokenId.T_HTML
                && (tokenText == null || (!tokenText.toString().endsWith("{") && !tokenText.toString().startsWith("}"))); //NOI18N
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
    }

}
