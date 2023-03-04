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

package org.netbeans.modules.web.core.syntax;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.web.common.api.Constants;

/**
 *
 * An Expression Language EmbeddingProvider for text/x-jsp and text/x-tag mimetypes
 *
 * @author mfukala@netbeans.org
 */
final class JspELEmbeddingProvider extends EmbeddingProvider {

    private static final String ATTRIBUTE_EL_MARKER = "A"; //NOI18N
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<JspTokenId> sequence = th.tokenSequence(JspTokenId.language());
        List<Embedding> embeddings = new ArrayList<Embedding>();
        sequence.moveStart();
        boolean inAttributeValueWithEL = false;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == JspTokenId.ATTR_VALUE && t.length() == 1 && 
                    (t.text().charAt(0) == '"' || t.text().charAt(0) == '\'')) {
                //a quote before/after attribute value with EL inside
                inAttributeValueWithEL = !inAttributeValueWithEL;
            }
            if (t.id() == JspTokenId.EL) {
                embeddings.add(snapshot.create(sequence.offset(), t.length(), "text/x-el")); //NOI18N
                //XXX hack - there's a need to distinguish between ELs inside or outside of attribute values
                if(inAttributeValueWithEL) {
                    embeddings.add(snapshot.create(ATTRIBUTE_EL_MARKER, "text/x-el")); //NOI18N
                }
                
                // just to separate expressions for easier handling in EL parser
                embeddings.add(snapshot.create(Constants.LANGUAGE_SNIPPET_SEPARATOR, "text/x-el")); //NOI18N
             
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
    }

     public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Arrays.<SchedulerTask>asList(new JspELEmbeddingProvider());
        }
    }
}
