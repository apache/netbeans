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
package org.netbeans.modules.php.blade.editor.embedding;

import java.util.List;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.blade.editor.BladeTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class BladeHtmlEmbeddingProviderTest extends BladeTestBase {

    public BladeHtmlEmbeddingProviderTest(String testName) {
        super(testName);
    }

    private void checkPhpEmbedding(final String relFilePath) throws Exception {
        checkEmbedding(relFilePath, new BladeHtmlEmbeddingProvider());
    }

    private void checkEmbedding(final String relFilePath, EmbeddingProvider embeddingProvider) throws Exception {
        assertNotNull(embeddingProvider);
        String testedFilePath = "testfiles/embedding/" + relFilePath + ".blade.php";
        Source testSource = getTestSource(getTestFile(testedFilePath));
        List<Embedding> embeddings = embeddingProvider.getEmbeddings(testSource.createSnapshot());
        assertDescriptionMatches(testedFilePath, serializableEmbeddings(embeddings), true, ".embedding");
    }

    private String serializableEmbeddings(List<Embedding> embeddings) {
        StringBuilder sb = new StringBuilder();
        for (Embedding embedding : embeddings) {
            sb.append(embedding.getSnapshot().getText());
        }
        return sb.toString();
    }

    public void testHtmlEmbedding_01() throws Exception {
        checkPhpEmbedding("html_embedding_01");
    }

}
