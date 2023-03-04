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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.web.common.api.Constants;

/**
 * Provides model for html code
 *
 * @author Marek Fukala
 */
public class XhtmlElEmbeddingProvider extends EmbeddingProvider {

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<XhtmlElTokenId> sequence = th.tokenSequence(XhtmlElTokenId.language());
        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<>();
        boolean lastEmbeddingIsVirtual = false;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == XhtmlElTokenId.HTML) {
                //lets suppose the text is always html :-(
                embeddings.add(snapshot.create(sequence.offset(), t.length(), "text/html")); //NOI18N
                lastEmbeddingIsVirtual = false;
            } else {
                //replace templating tokens by generated code marker
                if (!lastEmbeddingIsVirtual) {
                    embeddings.add(snapshot.create(Constants.LANGUAGE_SNIPPET_SEPARATOR, "text/html"));
                    lastEmbeddingIsVirtual = true;
                }
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
        //do nothing
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Arrays.<SchedulerTask>asList(new XhtmlElEmbeddingProvider(), new ELEmbeddingProvider());
        }
    }
}
