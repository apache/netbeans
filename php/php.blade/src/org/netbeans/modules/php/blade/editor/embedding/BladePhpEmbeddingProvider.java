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
package org.netbeans.modules.php.blade.editor.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;

/**
 * this will enable braces matches of html elements
 * 
 * @author bhaidu
 */
@EmbeddingProvider.Registration(
        mimeType = BladeLanguage.MIME_TYPE,
        targetMimeType = "text/x-php5")
public class BladePhpEmbeddingProvider extends EmbeddingProvider {
    public static final String TARGET_MIME_TYPE = "text/x-php5"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(final Snapshot snapshot) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        TokenSequence<?> sequence = tokenHierarchy.tokenSequence();
        if (sequence == null) {
            return Collections.emptyList();
        }
        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<>();

        int offset = 0;
        int len = 0;

        String fake;

        while (sequence.moveNext()) {
            Token<?> t = sequence.token();
            offset = sequence.offset();
            TokenId id = t.id();
            len += t.length();
            String tText = t.text().toString();
            if (len == 0) {
                continue;
            }
            if (id.equals(BladeTokenId.PHP_INLINE)) {
                embeddings.add(snapshot.create(offset, t.length(), TARGET_MIME_TYPE));
            } else {
                fake = new String(new char[tText.length()]).replace("\0", "@");
                embeddings.add(snapshot.create(fake, TARGET_MIME_TYPE));
            }
        }

        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", TARGET_MIME_TYPE));
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 210;
    }

    @Override
    public void cancel() {
        //TODO see html implementation
    }
}
