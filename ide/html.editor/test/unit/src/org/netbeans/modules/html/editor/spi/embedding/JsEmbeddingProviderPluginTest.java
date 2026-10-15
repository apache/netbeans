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
package org.netbeans.modules.html.editor.spi.embedding;

import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.embedding.JsEmbeddingProviderTest;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class JsEmbeddingProviderPluginTest extends CslTestBase {

    public JsEmbeddingProviderPluginTest(String name) {
        super(name);
    }
    
    public void testPlugin() {
        BaseDocument document = getDocument("hello", "text/html");
        JsEmbeddingProviderTest.assertEmbedding(document, "world!");
        
        assertTrue(TestPlugin.started);
        assertTrue(TestPlugin.processed);
        assertTrue(TestPlugin.ended);
        
    }

    @MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
    public static class TestPlugin extends JsEmbeddingProviderPlugin {

        private Snapshot snapshot;
        private List<Embedding> embeddings;
        private TokenSequence<HTMLTokenId> ts;
        public static boolean started, ended, processed;
        
        @Override
        public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
            assertNotNull(snapshot);
            assertNotNull(parserResult);
            assertNotNull(ts);
            assertNotNull(embeddings);
            this.snapshot = snapshot;
            this.embeddings = embeddings;
            this.ts = ts;
            
            started = true;
            return true;
        }

        @Override
        public boolean processToken() {
            if(!processed) {
                if(LexerUtils.equals("hello", ts.token().text() , false, false)) {
                    embeddings.add(snapshot.create("world!", "text/javascript"));
                }
            }
            processed = true;
            return false;
        }

        @Override
        public void endProcessing() {
            ended = true;
        }

    }

}