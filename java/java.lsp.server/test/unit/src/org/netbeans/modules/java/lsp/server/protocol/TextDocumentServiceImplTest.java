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
package org.netbeans.modules.java.lsp.server.protocol;

import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;

public class TextDocumentServiceImplTest extends NbTestCase {

    public TextDocumentServiceImplTest(String name) {
        super(name);
    }

    public void testNormalizeLineEndings1() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc\n", null);
        doc.addDocumentListener(new NoDocumentChanges());
        TextDocumentServiceImpl.updateDocumentIfNeeded("a\nb\nc\n", doc);
        assertEquals("a\nb\nc\n",
                     doc.getText(0, doc.getLength()));
    }

    public void testNormalizeLineEndings2() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc\n", null);
        doc.addDocumentListener(new NoDocumentChanges());
        TextDocumentServiceImpl.updateDocumentIfNeeded("a\rb\nc\r", doc);
        assertEquals("a\nb\nc\n",
                     doc.getText(0, doc.getLength()));
    }

    public void testNormalizeLineEndings3() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc\n", null);
        doc.addDocumentListener(new NoDocumentChanges());
        TextDocumentServiceImpl.updateDocumentIfNeeded("a\r\nb\nc\r", doc);
        assertEquals("a\nb\nc\n",
                     doc.getText(0, doc.getLength()));
    }

    public void testNormalizeLineEndings4() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc\n", null);
        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger removeCount = new AtomicInteger();
        doc.addDocumentListener(new NoDocumentChanges() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                insertCount.incrementAndGet();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                removeCount.incrementAndGet();
            }
        });
        TextDocumentServiceImpl.updateDocumentIfNeeded("a\r\nd\nc\r", doc);
        assertEquals("a\nd\nc\n",
                     doc.getText(0, doc.getLength()));
        assertEquals(1, insertCount.get());
        assertEquals(1, removeCount.get());
    }

    public void testNormalizeLineEndings5() throws BadLocationException {
        Document doc = new PlainDocument();
        doc.insertString(0, "a\nb\nc\n", null);
        AtomicInteger insertCount = new AtomicInteger();
        AtomicInteger removeCount = new AtomicInteger();
        doc.addDocumentListener(new NoDocumentChanges() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                insertCount.incrementAndGet();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                removeCount.incrementAndGet();
            }
        });
        TextDocumentServiceImpl.updateDocumentIfNeeded("a\nd\nc\n", doc);
        assertEquals("a\nd\nc\n",
                     doc.getText(0, doc.getLength()));
        assertEquals(1, insertCount.get());
        assertEquals(1, removeCount.get());
    }

    private static class NoDocumentChanges implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            fail(String.valueOf(e));
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            fail(String.valueOf(e));
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            fail(String.valueOf(e));
        }
    }
}
