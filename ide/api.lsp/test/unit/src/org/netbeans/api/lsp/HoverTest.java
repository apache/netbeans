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
package org.netbeans.api.lsp;

import java.util.concurrent.CompletableFuture;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lsp.HoverProvider;

/**
 *
 * @author Dusan Balek
 */
public class HoverTest extends NbTestCase {

    public HoverTest(String name) {
        super(name);
    }

    @Override
    public void setUp () throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances (MimePath.get ("text/foo"), new FooHoverProvider());
    }

    public void testHoverContent() {
        Document doc = createDocument("text/foo", "");
        int offset = 0;
        // @start region="testHoverContent"

        // Resolve a hover information at the given document offset...
        CompletableFuture<String> future = Hover.getContent(doc, offset);

        // ...and get its content
        String content = future.getNow(null);
        assertEquals("Foo hover information", content);
        // @end region="testHoverContent"
    }

    private Document createDocument(String mimeType, String contents) {
        Document doc = new DefaultStyledDocument();
        doc.putProperty("mimeType", mimeType);
        try {
            doc.insertString(0, contents, null);
            return doc;
        } catch (BadLocationException ble) {
            throw new IllegalStateException(ble);
        }
    }

    private static class FooHoverProvider implements HoverProvider {

        @Override
        public CompletableFuture<String> getHoverContent(Document doc, int offset) {
            return CompletableFuture.completedFuture("Foo hover information");
        }
    }
}
