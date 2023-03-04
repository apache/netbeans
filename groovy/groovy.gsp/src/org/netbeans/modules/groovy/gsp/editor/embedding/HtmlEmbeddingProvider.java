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
package org.netbeans.modules.groovy.gsp.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 * Provides model for html code.
 *
 * @author Marek Fukala
 */
public class HtmlEmbeddingProvider extends EmbeddingProvider {

    // Not sure what's the reason for this, but without it the HTML highlighting doesn't working
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        final TokenSequence<GspTokenId> sequence = getTokenSequence(snapshot);
        final List<Embedding> embeddings = new ArrayList<Embedding>();

        boolean lastEmbeddingIsVirtual = false;
        while (sequence.moveNext()) {

            Token<GspTokenId> token = sequence.token();
            switch (token.id()) {
                case HTML:
                    embeddings.add(snapshot.create(sequence.offset(), token.length(), "text/html")); //NOI18N
                    lastEmbeddingIsVirtual = false;
                    break;
                default:
                    if (!lastEmbeddingIsVirtual) {
                        embeddings.add(snapshot.create(GENERATED_CODE, "text/html"));
                        lastEmbeddingIsVirtual = true;
                    }
            }
        }
        return embeddings.isEmpty() ?
                Collections.<Embedding>emptyList() :
                Collections.singletonList(Embedding.create(embeddings));
    }

    private TokenSequence<GspTokenId> getTokenSequence(Snapshot snapshot) {
        final Language<GspTokenId> gspLanguage = GspLexerLanguage.getLanguage();
        final TokenHierarchy<CharSequence> tokenHierarchy = TokenHierarchy.create(snapshot.getText(), gspLanguage);
        final TokenSequence<GspTokenId> sequence = tokenHierarchy.tokenSequence(gspLanguage);

        sequence.moveStart();
        return sequence;
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new HtmlEmbeddingProvider());
        }
    }
}
