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

import org.netbeans.modules.html.editor.embedding.CssEmbeddingProvider;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.html.editor.test.TestBase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Marek Fukala
 */
public class CssEmbeddingProviderTest extends TestBase {

    public CssEmbeddingProviderTest(String name) {
        super(name);
    }

    public void testBasicFile() throws Exception {
        checkVirtualSource("testfiles/embedding/test1.html");
    }

    public void testIssue174241() throws Exception {
        checkVirtualSource("testfiles/embedding/testIssue174241.html");
        checkVirtualSource("testfiles/embedding/testIssue174241_1.html");
    }
    
    public void testIssue215267() throws Exception {
        checkVirtualSource("testfiles/embedding/issue215267.html");
    }
    
    public void testIssue215267_css_embedding_at_offset() throws Exception {
        FileObject fo = getTestFile("testfiles/embedding/issue215267.html");
        assertNotNull(fo);

        Source source = Source.create(fo);
        assertNotNull(source);
        
        final int caret = 147;
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Result parserResult = resultIterator.getParserResult(caret);
                assert parserResult instanceof CssParserResult;
            }
        });
        
    }

    private void checkVirtualSource(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        Source source = Source.create(fo);
        assertNotNull(source);

        Snapshot snapshot = source.createSnapshot();
        assertNotNull(snapshot);

        CssEmbeddingProvider provider = new CssEmbeddingProvider();

        List<Embedding> embeddings = provider.getEmbeddings(snapshot);
        assertNotNull(embeddings);
        assertEquals(1, embeddings.size());

        Embedding embedding = embeddings.get(0);
        Snapshot virtualSource = embedding.getSnapshot();
        String virtualSourceText = virtualSource.getText().toString();

        assertDescriptionMatches(file, virtualSourceText, false, ".virtual");
    }
}
