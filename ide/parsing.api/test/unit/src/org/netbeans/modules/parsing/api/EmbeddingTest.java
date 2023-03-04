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
package org.netbeans.modules.parsing.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author vita
 */
public class EmbeddingTest extends ParsingTestBase {

    public EmbeddingTest(String name) {
        super(name);
    }

    public void testInjectedEmbeddings() throws ParseException {
        final String mimeType = "text/x-" + getName();
        final String embeddedMimeType = "text/x-embedded-" + getName();
        MockMimeLookup.setInstances(MimePath.parse(mimeType), new TaskFactory() {
            public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singleton(new EmbeddingProvider() {
                    public @Override List<Embedding> getEmbeddings(Snapshot snapshot) {
                        List<Embedding> embeddings = new ArrayList<Embedding>();
                        embeddings.add(snapshot.create("Embedded Section 1\n", embeddedMimeType));
                        embeddings.add(snapshot.create("Embedded Section 2\n", embeddedMimeType));
                        embeddings.add(snapshot.create("Embedded Section 3\n", embeddedMimeType));
                        return Collections.singletonList(Embedding.create(embeddings));
                    }

                    public @Override int getPriority() {
                        return Integer.MAX_VALUE;
                    }

                    public @Override void cancel() {
                    }
                });
            }
        });

        EditorKit kit = new DefaultEditorKit();
        Document doc = kit.createDefaultDocument();
        doc.putProperty("mimeType", mimeType);

        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                assertEquals(mimeType, resultIterator.getSnapshot().getMimeType());

                int cnt = 0;
                List<Embedding> embeddings = new ArrayList<Embedding>();
                for(Embedding e : resultIterator.getEmbeddings()) {
                    cnt++;
                    if (e.getMimeType().equals(embeddedMimeType)) {
                        embeddings.add(e);
                    }
                }

                assertEquals("Wrong number of embeddings", 1, cnt);
                assertEquals("Wrong number of our mebeddings", 1, embeddings.size());

                Embedding e = embeddings.get(0);
                CharSequence sourceSnapshot = resultIterator.getSnapshot().getText();
                for(int i = 0; i < sourceSnapshot.length(); i++) {
                    assertFalse("Injected embeddings should not contain offset: " + i, e.containsOriginalOffset(i));
                }
            }
        });
    }
}
