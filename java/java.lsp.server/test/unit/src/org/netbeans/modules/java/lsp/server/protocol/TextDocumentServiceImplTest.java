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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.eclipse.lsp4j.FoldingRange;
import org.netbeans.junit.NbTestCase;

import static org.netbeans.modules.java.lsp.server.protocol.TextDocumentServiceImpl.convertToLineOnlyFolds;

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
    
    public void testConvertToLineOnlyFolds() {
        assertNull(convertToLineOnlyFolds(null));
        assertEquals(0, convertToLineOnlyFolds(Collections.emptyList()).size());
        List<FoldingRange> inputFolds, outputFolds;
        inputFolds = Collections.singletonList(createRange(10, 20));
        assertEquals(inputFolds, convertToLineOnlyFolds(inputFolds));

        // test stable sort by start index
        inputFolds = List.of(createRange(10, 20, 9, 9), createRange(5, 9, 9, 9), createRange(10, 19, 9, 9), createRange(10, 14, 13, 13));
        outputFolds = List.of(createRange(5, 9), createRange(10, 20), createRange(10, 19), createRange(10, 14));
        assertEquals(outputFolds, convertToLineOnlyFolds(inputFolds));

        // test already disjoint folds
        inputFolds = List.of(createRange(10, 20, 9, 9), createRange(5, 9, 9, 9), createRange(15, 19, 13, 13), createRange(10, 14, 13, 13));
        outputFolds = List.of(createRange(5, 9), createRange(10, 20), createRange(10, 14), createRange(15, 19));
        assertEquals(outputFolds, convertToLineOnlyFolds(inputFolds));

        // test invariant of range.endLine: there exists no otherRange.startLine == range.endLine.
        inputFolds = List.of(createRange(10, 20, 35, 9), createRange(5, 10, 12, 9), createRange(15, 19, 20, 13), createRange(10, 15, 51, 13));
        assertEquals(outputFolds, convertToLineOnlyFolds(inputFolds));

        // test a complex example of a full file:
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//
// /**
// * A top-class action performer
// *
// * @since 1.1
// */
//public class TopClass {
//
//    private final String action;
//    private final int index;
//
//    /**
//     * @param action Top action to be done
//     */
//    public TopClass(String action) {
//        this(action, 0);
//    }
//
//    /**
//     * @param action Top action to be done
//     * @param index Action index
//     */
//    public TopClass(String action, int index) {
//        this.action = action;
//        this.index = index;
//    }
//
//    public void doSomethingTopClass(TopClass tc) {
//        // what can we do
//        {
//            if (tc == this) {
//                return;
//            } else if (tc.getClass() == this.getClass()) {
//            } else if (tc.getClass().isAssignableFrom(this.getClass())) {
//
//            } else {
//                if (true) {
//                    switch (tc) {
//                        default: { /* this is some comment */ ; }
//                        /// some outside default
//                    }
//                } else { if (true) { { /* some */ } { /* bad blocks */ }
//                }}
//                /* done  */
//            }
//        }
//        tc.doSomethingTopClass(tc);
//    }
//
//    public class InnerClass {
//        @Override
//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            sb.append("InnerClass{");
//            sb.append("action=").append(action);
//            sb.append(", index=").append(index);
//            sb.append('}');
//            return sb.toString();
//        }
//    }
//}
        inputFolds = List.of(
                createRange(27, 30, 48, 5),
                createRange(0, 3, 7, 30),
                createRange(32, 52, 51, 5),
                createRange(37, 38, 59, 13),
                createRange(34, 50, 10, 9),
                createRange(46, 46, 39, 51),
                createRange(35, 37, 30, 13),
                createRange(38, 40, 74, 13),
                createRange(40, 49, 21, 13),
                createRange(46, 47, 37, 17),
                createRange(41, 46, 28, 17),
                createRange(42, 45, 34, 21),
                createRange(11, 66, 24, 1),
                createRange(43, 43, 35, 65),
                createRange(46, 47, 25, 18),
                createRange(54, 64, 30, 5),
                createRange(46, 46, 54, 72),
                createRange(6, 10, 4, 1),
                createRange(56, 63, 35, 9)
        );
        outputFolds = List.of(
                createRange(0, 3),
                createRange(6, 10),
                createRange(11, 66),
                createRange(27, 30),
                createRange(32, 52),
                createRange(34, 50),
                createRange(35, 36),
                createRange(38, 39),
                createRange(40, 49),
                createRange(41, 45),
                createRange(42, 45),
                createRange(46, 47),
                createRange(54, 64),
                createRange(56, 63)
        );
        assertEquals(outputFolds, convertToLineOnlyFolds(inputFolds));
    }
    
    private static FoldingRange createRange(int startLine, int endLine) {
        return new FoldingRange(startLine, endLine);
    }
    
    private static FoldingRange createRange(int startLine, int endLine, Integer startColumn, Integer endColumn) {
        FoldingRange foldingRange = new FoldingRange(startLine, endLine);
        foldingRange.setStartCharacter(startColumn);
        foldingRange.setEndCharacter(endColumn);
        return foldingRange;
    }
}
