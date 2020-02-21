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
package org.netbeans.modules.cnd.makefile.editor;

import org.netbeans.junit.NbTestCase;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.parsing.api.Embedding;
import java.util.List;
import org.netbeans.modules.parsing.api.Source;
import javax.swing.text.Document;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 */
public class ShellEmbeddingProviderTest extends NbTestCase {

    public ShellEmbeddingProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MimePath mimePath = MimePath.parse(MIMENames.MAKEFILE_MIME_TYPE);
        MockMimeLookup.setInstances(mimePath, MakefileTokenId.language());
    }

    @Test
    public void testOneLine() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo $${PATH} # echo PATH\n");
        assertEquals(1, embeddings.size());

        Embedding e = embeddings.get(0);
        assertEquals("echo ${PATH} # echo PATH", e.getSnapshot().getText().toString());
    }

    @Test
    public void testMultiLine() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a\n\techo b\n");
        assertEquals(2, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a", e1.getSnapshot().getText().toString());

        Embedding e2 = embeddings.get(1);
        assertEquals("echo b", e2.getSnapshot().getText().toString());
    }

    @Test
    public void testEscapedNewline() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a\\\n\techo b\n");
        assertEquals(1, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a\\\n\techo b", e1.getSnapshot().getText().toString());
    }

    @Test
    public void testNoNewline() throws Exception {
        List<Embedding> embeddings = getShellEmbeddings("a:\n\techo a");
        assertEquals(1, embeddings.size());

        Embedding e1 = embeddings.get(0);
        assertEquals("echo a", e1.getSnapshot().getText().toString());
    }

    private static List<Embedding> getShellEmbeddings(String text) throws BadLocationException {
        Document doc = new NbEditorDocument(MIMENames.MAKEFILE_MIME_TYPE);
        doc.insertString(0, text, null);
        return new ShellEmbeddingProvider().getEmbeddings(Source.create(doc).createSnapshot());
    }
}
