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
package org.netbeans.modules.web.core.syntax.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.web.core.syntax.JspUtils;

/**
 * Provides model for html code
 *
 * @author Marek Fukala
 */
public class JspEmbeddingProvider extends EmbeddingProvider {

    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = JspUtils.createJspTokenHierarchy(snapshot);
        TokenSequence<JspTokenId> sequence = th.tokenSequence(JspTokenId.language());
        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<Embedding>();
        boolean lastEmbeddingIsVirtual = false;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == JspTokenId.TEXT) {
                //lets suppose the text is always html :-(
                embeddings.add(snapshot.create(sequence.offset(), t.length(), "text/html")); //NOI18N
                lastEmbeddingIsVirtual = false;
            } else {
                //replace templating tokens by generated code marker
                if (!lastEmbeddingIsVirtual) {
                    embeddings.add(snapshot.create(GENERATED_CODE, "text/html"));
                    lastEmbeddingIsVirtual = true;
                }
            }
        }
        if (embeddings.isEmpty()) {
            //always embed html even if there isn't any
            //this causes the parsing api to run tasks registered to text/html
            //even if there isn't any html content 
            return Collections.singletonList(snapshot.create("", "text/html")); 
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
            return Collections.<SchedulerTask>singletonList(new JspEmbeddingProvider());
        }
    }
}
