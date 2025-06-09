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
package org.netbeans.modules.javascript2.vue.editor.embedding;

/**
 *
 * @author bhaidu
 */
public class VueStyleEmbeddingProviderTest extends EmbeddingTestBase {

    public VueStyleEmbeddingProviderTest(String testName) {
        super(testName);
       
    }

    private void checkScssEmbedding(final String relFilePath) throws Exception {
        checkEmbedding(relFilePath, new VueScssEmbeddingProvider());
    }
    
    private void checkLessEmbedding(final String relFilePath) throws Exception {
        checkEmbedding(relFilePath, new VueLessEmbeddingProvider());
    }

    public void testScssEmbedding_01() throws Exception {
        checkScssEmbedding("vue_scss_embedding_01");
    }
    
    public void testLessEmbedding_01() throws Exception {
        checkLessEmbedding("vue_less_embedding_01");
    }
}
