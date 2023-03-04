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
package org.netbeans.api.lsp;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class CompletionTest extends NbTestCase {

    public CompletionTest(String name) {
        super(name);
    }

    @Override
    public void setUp () throws Exception {
        super.setUp();
        clearWorkDir();
        MockMimeLookup.setInstances (MimePath.get ("text/foo"), new FooCompletionCollector());
    }

    public void testCompletionCollect() {
        Document doc = createDocument("text/foo", "");
        int offset = 0;
        // @start region="testCompletionCollect"

        // Compute and collect completions for a document at a given offset
        boolean isComplete = Completion.collect(doc, offset, null, completion -> {

            // completion should never be 'null'
            assertNotNull(completion);

            // getting completion 'label'
            String label = completion.getLabel();
            assertEquals("label", label);

            // getting optional completion 'detail'
            CompletableFuture<String> detail = completion.getDetail();
            // check for 'null' value
            if (detail != null) {
                // value should be already computed
                assertTrue(detail.isDone());
                // getting the value
                String value = detail.getNow(null);
                assertEquals("detail", value);
            }

            // getting optional completion 'documentation'
            CompletableFuture<String> documentation = completion.getDocumentation();
            // check for 'null' value
            if (documentation != null) {
                // value computation should be deferred
                assertFalse(documentation.isDone());
                // getting the value
                try {
                    String value = documentation.get();
                    assertEquals("documentation", value);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        // @end region="testCompletionCollect"
        assertTrue(isComplete);
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

    private static class FooCompletionCollector implements CompletionCollector {

        @Override
        public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
            // @start region="builder"

            // Create a builder for creating 'Completion' instance providing its 'label'
            Completion c = CompletionCollector.newBuilder("label")

                    // set completion detail
                    .detail("detail")

                    // set completion documentation with deffered computation
                    .documentation(() -> {
                        return "documentation";
                    })

                    // create a new 'Completion' instance
                    .build();

            // @end region="builder"
            consumer.accept(c);
            return true;
        }
    }
}
