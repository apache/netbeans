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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.Collections;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

/**
 *
 * @author Petr Pisl
 */
public class JadeJsEmbeddingProviderPluginTest extends CslTestBase {
    
    public JadeJsEmbeddingProviderPluginTest(String testName) {
        super(testName);
    }
    
    public void testIssue251143() throws Exception {
        checkVirtualSource("testfiles/jsEmbedding/issue251143.jade");
    }
    
    public void testIssue251150() throws Exception {
        checkVirtualSource("testfiles/lexer/issue251150.jade");
    }
    
    public void testIssue251144() throws Exception {
        checkVirtualSource("testfiles/lexer/issue251144.jade");
    }
    
    private void checkVirtualSource(final String testFile) throws Exception {
        Source testSource = getTestSource(getTestFile(testFile));
        
        ParserManager.parse(Collections.singleton(testSource), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Iterable<Embedding> embeddings = resultIterator.getEmbeddings();
                Embedding jsEmbedding = null;
                for (Embedding embedding : embeddings) {
                    if (embedding.getMimeType().equals("text/javascript")) {
                        jsEmbedding = embedding;
                        break;
                    }
                }
                assertNotNull("JS embeding was not found.", jsEmbedding);
                String text = jsEmbedding.getSnapshot().getText().toString();
                
                assertDescriptionMatches(testFile, text, true, ".vs.js");
            }
        });
    }
}
