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
package org.netbeans.modules.php.twig.editor.embedding;

import java.util.List;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.php.twig.editor.TwigTestBase;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TwigHtmlEmbeddingProviderTest extends TwigTestBase {

    public TwigHtmlEmbeddingProviderTest(String testName) {
        super(testName);
    }

    private void checkHtmlEmbedding(final String relFilePath) throws Exception {
        checkEmbedding(relFilePath, new TwigHtmlEmbeddingProvider());
    }

    private void checkEmbedding(final String relFilePath, EmbeddingProvider embeddingProvider) throws Exception {
        assertNotNull(embeddingProvider);
        String testedFilePath = "testfiles/embedding/" + relFilePath + ".twig";
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

    public void testIssue227029() throws Exception {
        checkHtmlEmbedding("testIssue227029");
    }

}
