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
package org.netbeans.modules.javascript2.editor.embedding;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsEmbeddingTestBase extends CslTestBase {

    public JsEmbeddingTestBase(String testName) {
        super(testName);
    }

    public void checkTranslation(final String relFilePath, String mimeType) throws Exception {
        if (mimeType.equals("text/x-tpl")) {
            checkTranslation(relFilePath, new JsEmbeddingProvider.TplTranslator());
        } else {
            throw new UnsupportedOperationException("MimeType '" + mimeType + "' is not supported yet.");
        }
    }

    public void checkTranslation(final String relFilePath, final JsEmbeddingProvider.Translator translator) throws Exception {
        assertNotNull(translator);
        Source testSource = getTestSource(getTestFile(relFilePath));
        List<Embedding> embeddings = translator.translate(testSource.createSnapshot());
        assertDescriptionMatches(relFilePath, serializableEmbeddings(embeddings), true, ".tranlation");
    }

    protected String serializableEmbeddings(List<Embedding> embeddings) {
        StringBuilder sb = new StringBuilder();
        for (Embedding embedding : embeddings) {
            sb.append(embedding.getSnapshot().getText());
        }
        return sb.toString();
    }

}
