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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.twig.editor.gsf.TwigLanguage;
import org.netbeans.modules.php.twig.editor.lexer.TwigTopTokenId;

@EmbeddingProvider.Registration(mimeType = TwigLanguage.TWIG_MIME_TYPE, targetMimeType = TwigHtmlEmbeddingProvider.TARGET_MIME_TYPE)
public class TwigHtmlEmbeddingProvider extends EmbeddingProvider {

    public static final String TARGET_MIME_TYPE = "text/html"; //NOI18N
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), TwigTopTokenId.language());
        TokenSequence<TwigTopTokenId> sequence = th.tokenSequence(TwigTopTokenId.language());
        if (sequence == null) {
            return Collections.emptyList();
        }
        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<>();
        int offset = -1;
        int length = 0;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == TwigTopTokenId.T_HTML) {
                if (offset < 0) {
                    offset = sequence.offset();
                }
                length += t.length();
            } else if (offset >= 0) {
                embeddings.add(snapshot.create(offset, length, TARGET_MIME_TYPE));
                embeddings.add(snapshot.create(GENERATED_CODE, TARGET_MIME_TYPE));
                offset = -1;
                length = 0;
            }
        }
        if (offset >= 0) {
            embeddings.add(snapshot.create(offset, length, TARGET_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", TARGET_MIME_TYPE));
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
    }

}
