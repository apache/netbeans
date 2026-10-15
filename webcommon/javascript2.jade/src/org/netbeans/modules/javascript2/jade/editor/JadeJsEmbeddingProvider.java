package org.netbeans.modules.javascript2.jade.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;

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
/**
 *
 * @author Petr Pisl
 */
@EmbeddingProvider.Registration(
        mimeType="text/jade",
        targetMimeType="text/javascript"
)

public class JadeJsEmbeddingProvider extends EmbeddingProvider {
    
    private static final Logger LOGGER = Logger.getLogger(JadeJsEmbeddingProvider.class.getName());
    private static final String JS_MIME_TYPE = "text/javascript"; //NOI18N
    
    private static final String SEMICOLON_EOL = ";\n"; //NOI18N
    private static final String EOL = "\n"; //NOI18N
    private static final String SCRIPT_TAG_NAME = "script";     // NOI18N
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        
        if (ts == null) {
            LOGGER.log(
                    Level.WARNING,
                    "TokenHierarchy.tokenSequence(JadeTokenId.jadeLanguage()) == null " + "for static immutable Jade TokenHierarchy!\nFile = ''{0}'' ;snapshot mimepath=''{1}''",
                    new Object[]{snapshot.getSource().getFileObject().getPath(), snapshot.getMimePath()});

            return Collections.emptyList();
        }
        
        ts.moveStart();
        
        List<Embedding> embeddings = new ArrayList<>();
        
        int from = -1;
        int len = 0;
        Token<JadeTokenId> lastTag = null;
        Token<JadeTokenId> lastMixing = null;
        
        while (ts.moveNext()) {
            Token<JadeTokenId> token = ts.token();
            if (token.id() == JadeTokenId.JAVASCRIPT) {
                if (from < 0) {
                    from = ts.offset();
                }
                len += token.length();
            } else {
                if (from >= 0) {
                    if (lastMixing != null) {
                        wrapAsFnParameter(snapshot, embeddings, lastMixing.text().toString(), from, len);
                    } else {
                        embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
                        addNewLine(snapshot, embeddings, from + len - 1);
                    }
                } 
                from = -1;
                len = 0;
            }
            if (token.id() == JadeTokenId.TAG) {
                lastTag = token;
                lastMixing = null;
            }
            if (token.id() == JadeTokenId.MIXIN_NAME) {
                lastTag = null;
                lastMixing = token;
            }
            if (token.id() == JadeTokenId.PLAIN_TEXT_DELIMITER) {
                // check whether there is not 
                if (lastTag != null && SCRIPT_TAG_NAME.equalsIgnoreCase(lastTag.text().toString()) && ts.moveNext()) {
                    token = ts.token();
                    while (token.id() == JadeTokenId.EOL && ts.moveNext()) {
                        token = ts.token();
                    }
                    if (token.id() == JadeTokenId.PLAIN_TEXT || token.id() == JadeTokenId.JAVASCRIPT) {
                        embeddings.add(snapshot.create(ts.offset(), token.length(), JS_MIME_TYPE));
                    }
                }
            }
        }
        if (from >= 0) {
            if (lastMixing != null) {
                wrapAsFnParameter(snapshot, embeddings, lastMixing.text().toString(), from, len);
            } else {
                embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
                addNewLine(snapshot, embeddings, from + len - 1);
            }
        }
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Embedding.create(embeddings));
    }

    private void addNewLine(Snapshot snapshot, List<Embedding> embeddings, int origOffset) {
        CharSequence text = snapshot.getText();
        int offset = origOffset;
        while (offset > 0 && Character.isWhitespace(text.charAt(offset))) {
            offset--;
        }
        if (offset > 0 ) {
            char ch = text.charAt(offset);
            if (ch == '{' || ch == '}' || ch == '[') {
                embeddings.add(snapshot.create(EOL, JS_MIME_TYPE));
            } else {
                embeddings.add(snapshot.create(SEMICOLON_EOL, JS_MIME_TYPE));
            }
        }
    }
    
    private void wrapAsFnParameter(Snapshot snapshot, List<Embedding> embeddings, String fnName, int from, int len) {
        embeddings.add(snapshot.create(fnName + "(", JS_MIME_TYPE));    //NOI18N
        embeddings.add(snapshot.create(from, len, JS_MIME_TYPE));
        embeddings.add(snapshot.create(");\n", JS_MIME_TYPE)); //NOI18N
    }
    
    @Override
    public int getPriority() {
        return 202;
    }

    @Override
    public void cancel() {
        // do nothig for now
    }
    
}
