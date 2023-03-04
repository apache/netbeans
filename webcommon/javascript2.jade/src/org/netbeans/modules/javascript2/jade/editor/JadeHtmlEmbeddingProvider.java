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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.ArrayList;
import java.util.Collection;
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
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Petr Pisl
 */
@EmbeddingProvider.Registration(
        mimeType="text/jade",
        targetMimeType="text/html"
)
public class JadeHtmlEmbeddingProvider extends EmbeddingProvider {
    
    private static final Logger LOGGER = Logger.getLogger(JadeHtmlEmbeddingProvider.class.getName());
    private static final String HTML_MIME_TYPE = "text/html"; //NOI18N
    
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
        while (ts.moveNext()) {
            Token<JadeTokenId> token = ts.token();
            if (token.id() == JadeTokenId.PLAIN_TEXT) {
                if (from < 0) {
                    from = ts.offset();
                }
                len += token.length();
            } else {
                if (from >= 0) {
                    embeddings.add(snapshot.create(from, len + 1, HTML_MIME_TYPE));
                }
                from = -1;
                len = 0;
            }
        }
        if (from >= 0) {
            embeddings.add(snapshot.create(from, len, HTML_MIME_TYPE));
        }
        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(Embedding.create(embeddings));
        
    }

    @Override
    public int getPriority() {
        return 201;
    }

    @Override
    public void cancel() {
        // nothing so far
    }
    
    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new JadeHtmlEmbeddingProvider());
        }
    }
}
