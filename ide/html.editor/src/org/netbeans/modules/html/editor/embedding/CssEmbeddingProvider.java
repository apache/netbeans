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
package org.netbeans.modules.html.editor.embedding;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;

import static org.netbeans.modules.html.editor.embedding.CssHtmlTranslator.CSS_MIME_TYPE;

/**
 *
 * @author marekfukala
 */
@EmbeddingProvider.Registration(
        mimeType="text/html",
        targetMimeType="text/css"
)
public class CssEmbeddingProvider extends EmbeddingProvider {

    private static final Logger LOG = Logger.getLogger(CssEmbeddingProvider.class.getSimpleName());
    private static final long MAX_SNAPSHOT_SIZE = 4 * 1024 * 1024; //4MB
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private String sourceMimeType;
    private Translator translator;

    public CssEmbeddingProvider() {
        this.sourceMimeType = HTML_MIME_TYPE;
        this.translator = new CssHtmlTranslator();
    }

    public static interface Translator {

        public List<Embedding> getEmbeddings(Snapshot snapshot);
    
    }

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (sourceMimeType.equals(snapshot.getMimeType())) {
            int slen = snapshot.getText().length();
            LOG.fine(String.format("CssEmbeddingProvider.create(snapshot): mimetype: %s, size: %s", snapshot.getMimeType(), slen)); //NOI18N
            if(slen > MAX_SNAPSHOT_SIZE) {
                LOG.fine(String.format("Size %s > maximum (%s) => providing no css embedding", slen, MAX_SNAPSHOT_SIZE)); //NOI18N
                return Collections.<Embedding>emptyList();
            }
            List<Embedding> embeddings = translator.getEmbeddings(snapshot);
            if(embeddings.isEmpty()) {
                // Add a dummy CSS embedding without content. The intention is
                // that the HtmlCssIndexContributor is always invoked and the
                // CSS index can be used to find classes and ids
                return Collections.singletonList(snapshot.create("", CSS_MIME_TYPE));
            } else {
                return Collections.singletonList(Embedding.create(embeddings));
            }
        } else {
            LOG.log(Level.WARNING, "Unexpected snapshot type: ''{0}''; expecting ''{1}''", new Object[]{snapshot.getMimeType(), sourceMimeType}); //NOI18N
            return Collections.<Embedding>emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE; //todo specify reasonable number
    }

    @Override
    public void cancel() {
        //ignore //todo resolve cancel operation
    }
}
