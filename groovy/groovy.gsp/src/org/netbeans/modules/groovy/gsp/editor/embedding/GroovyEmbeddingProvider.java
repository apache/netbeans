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
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.gsp.GspLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public class GroovyEmbeddingProvider extends EmbeddingProvider {

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (GspLanguage.GSP_MIME_TYPE.equals(snapshot.getMimeType())) {
            return Collections.singletonList(Embedding.create(translate(snapshot)));
        } else {
            return Collections.<Embedding>emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void cancel() {
        // FIXME parsing API
    }

    private List<Embedding> translate(Snapshot snapshot) {
        final Language<GspTokenId> gspLanguage = GspLexerLanguage.getLanguage();
        final TokenHierarchy<CharSequence> tokenHierarchy = TokenHierarchy.create(snapshot.getText(), gspLanguage);
        final TokenSequence<GspTokenId> tokenSequence = tokenHierarchy.tokenSequence(gspLanguage);

        if (tokenSequence != null) {
            return translate(snapshot, tokenSequence);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Perform groovy translation.
     *
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    private List<Embedding> translate(Snapshot snapshot, TokenSequence<GspTokenId> tokenSequence) {
        final List<Embedding> embeddings = new ArrayList<Embedding>();
        embeddings.add(snapshot.create("def _buf ='';", GroovyTokenId.GROOVY_MIME_TYPE));

        boolean skipNewline = false;
        while (tokenSequence.moveNext()) {
            Token<GspTokenId> token = tokenSequence.token();

            int sourceStart = tokenSequence.offset();
            int sourceEnd = sourceStart + token.length();
            String text = token.text().toString();

            switch (token.id()) {
                case HTML:
                    // If there is leading whitespace in this token followed by a newline,
                    // emit it directly first, then insert my buffer append. Otherwise,
                    // insert a semicolon if we're on the same line as the previous output.
                    boolean found = false;
                    int i = 0;
                    for (; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (c == '\n') {
                            i++; // include it
                            found = true;
                            break;
                        } else if (!Character.isWhitespace(c)) {
                            break;
                        }
                    }

                    if (found) {
                        embeddings.add(snapshot.create(sourceStart, i, GroovyTokenId.GROOVY_MIME_TYPE));
                        text = text.substring(i);
                    }

                    embeddings.add(snapshot.create("_buf += \"\"\"", GroovyTokenId.GROOVY_MIME_TYPE));
                    if (skipNewline && text.startsWith("\n")) { // NOI18N
                        text = text.substring(1);
                        sourceEnd--;
                    }
                    embeddings.add(snapshot.create(text.replace("\"", "\\\""), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create("\"\"\";", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                case COMMENT_HTML_STYLE_CONTENT:
                case COMMENT_GSP_STYLE_CONTENT:
                case COMMENT_JSP_STYLE_CONTENT:
                    translateComment(snapshot, embeddings, sourceStart, text);
                    break;
                case GSTRING_CONTENT:
                case SCRIPTLET_CONTENT:
                    embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(";", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                case SCRIPTLET_OUTPUT_VALUE_CONTENT:
                    embeddings.add(snapshot.create("_buf += (", GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                    embeddings.add(snapshot.create(";)", GroovyTokenId.GROOVY_MIME_TYPE));

                    skipNewline = false;
                    break;
                default:
                    break;
            }
        }
        return embeddings;
    }

    private void translateComment(Snapshot snapshot, List<Embedding> embeddings, int sourceStart, String text) {
        embeddings.add(snapshot.create("/*", GroovyTokenId.GROOVY_MIME_TYPE));
        embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
        embeddings.add(snapshot.create("*/", GroovyTokenId.GROOVY_MIME_TYPE));
    }

    public static final class Factory extends TaskFactory {

        public Factory() {
        }

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (GspLanguage.GSP_MIME_TYPE.equals(snapshot.getMimeType())) {
                return Collections.singleton(new GroovyEmbeddingProvider());
            } else {
                return Collections.<SchedulerTask>emptyList();
            }
        }
    }
}
