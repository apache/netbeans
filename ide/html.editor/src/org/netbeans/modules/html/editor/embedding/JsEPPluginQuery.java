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
package org.netbeans.modules.html.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author marekfukala
 */
public class JsEPPluginQuery {

    private static JsEPPluginQuery DEFAULT;

    public static synchronized JsEPPluginQuery getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new JsEPPluginQuery();
        }
        return DEFAULT;
    }
    private Lookup.Result<JsEmbeddingProviderPlugin> lookupResult;
    private Collection<? extends JsEmbeddingProviderPlugin> plugins;

    private JsEPPluginQuery() {
        Lookup lookup = MimeLookup.getLookup("text/html");
        lookupResult = lookup.lookupResult(JsEmbeddingProviderPlugin.class);
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        Collection<? extends JsEmbeddingProviderPlugin> allInstances = lookupResult.allInstances();
        plugins = allInstances;
    }

    public Session createSession() {
        return new Session();
    }

    public class Session {

        private Collection<JsEmbeddingProviderPlugin> activePlugins;

        public Session() {
            activePlugins = new ArrayList<>();
        }

        public void startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
            for (JsEmbeddingProviderPlugin jsep : plugins) {
                if(jsep.startProcessing(parserResult, snapshot, ts, embeddings)) {
                    activePlugins.add(jsep);
                }
            }
        }
        
        public boolean processToken() {
            for (JsEmbeddingProviderPlugin jsep : activePlugins) {
                if (jsep.processToken()) {
                    return true;
                }
            }
            return false;
        }

        public void endProcessing() {
            for (JsEmbeddingProviderPlugin jsep : activePlugins) {
                jsep.endProcessing();
            }
        }
    }
}
