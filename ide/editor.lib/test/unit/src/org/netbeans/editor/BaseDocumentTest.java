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
package org.netbeans.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.RequestProcessor;

/**
 * Test functionality of BaseDocument.
 *
 * @author Miloslav Metelka
 */
public class BaseDocumentTest extends NbTestCase {

    public BaseDocumentTest(String testName) {
        super(testName);
    }

    public void testRunExclusive() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        doc.insertString(0, "Nazdar", null);
        
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.getText(0, doc.getLength());
                } catch (Exception ex) {
                    fail("Unexpected exception ex=" + ex);
                }
            }
        });
        
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.insertString(0, "a", null);
                    fail("Exception expected upon insertString()");
                } catch (IllegalStateException ex) {
                    // Expected
                } catch (Exception ex) {
                    fail("Unexpected exception ex=" + ex);
                }
            }
        });
        
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.runAtomic(new Runnable() {
                        @Override
                        public void run() {
                            fail("Should never run");
                        }
                    });
                    fail("Exception expected upon runAtomic()");
                } catch (IllegalStateException ex) {
                    // Expected
                } catch (Exception ex) {
                    fail("Unexpected exception ex=" + ex);
                }
            }
        });
        
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                EditorDocumentUtils.runExclusive(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            fail("Unexpected exception ex=" + ex);
                        }
                    }
                });
            }
        });
        
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                EditorDocumentUtils.runExclusive(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.insertString(0, "a", null);
                        } catch (IllegalStateException ex) {
                            // Expected
                        } catch (BadLocationException ex) {
                            fail("Unexpected exception ex=" + ex);
                        }
                    }
                });
            }
        });
        
        // doc.render() in runExclusive()
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            fail("Unexpected exception ex=" + ex);
                        }
                    }
                });
            }
        });

        // Nested runExclusive()
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                EditorDocumentUtils.runExclusive(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            fail("Unexpected exception ex=" + ex);
                        }
                    }
                });
            }
        });
    }
    
    
    public void testReadLockInRunExclusive() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        doc.insertString(0, "Nazdar", null);

        // Test thread access (runExclusive() and attempt read lock.
        final boolean t2Started[] = new boolean[1];
        final boolean t2DocReadLockGranted[] = new boolean[1];
        final Object LOCK = new Object();
        EditorDocumentUtils.runExclusive(doc, new Runnable() {
            @Override
            public void run() {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        t2Started[0] = true;
                        doc.render(new Runnable() {
                            @Override
                            public void run() {
                                t2DocReadLockGranted[0] = true;
                            }
                        });
                    }
                });
                while (!t2Started[0]) {
                    tSleep(1);
                }
                tSleep(5);
                assertFalse("Read lock access granted when in runExclusive", t2DocReadLockGranted[0]);
            }
        });
        tSleep(5);
        assertTrue("Read lock access not granted in T2", t2DocReadLockGranted[0]);
    }
    
    public void testRunExclusiveInReadLock() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        doc.insertString(0, "Nazdar", null);

        // Reversed test (read lock and attempt runExclusive()).
        final boolean t2Started[] = new boolean[1];
        final boolean t2DocAccess[] = new boolean[1];
        doc.render(new Runnable() {
            @Override
            public void run() {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        t2Started[0] = true;
                        EditorDocumentUtils.runExclusive(doc, new Runnable() {
                            @Override
                            public void run() {
                                t2DocAccess[0] = true;
                            }
                        });
                    }
                });
                while (!t2Started[0]) {
                    tSleep(1);
                }
                tSleep(5);
                assertFalse("runExclusive doc access granted when in render()", t2DocAccess[0]);
            }
        });
        tSleep(5);
        assertTrue("runExclusive() access not granted in T2", t2DocAccess[0]);
    }
    
    private static final void tSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            fail("Failed sleep");
        }
    }
    
    public void testReleaseDocAndHoldPosition() throws Exception {
        releaseDocAndHoldPosition(new BaseDocument(false, "text/plain")); // NOI18N
        releaseDocAndHoldPosition(new PlainDocument()); // NOI18N
    }
    
    private void releaseDocAndHoldPosition(Document doc) throws Exception {
        doc.insertString(0, "Nazdar", null);
        Position pos = doc.createPosition(3);
        doc.insertString(2, "abc", null);
        assertEquals(6, pos.getOffset());
        WeakReference<Document> docRef = new WeakReference<Document>(doc);
        doc = null;
        assertGC("Doc not released", docRef);
        assertEquals(6, pos.getOffset()); // Doc released but position can still be referenced
    }    

    public void testBackwardBiasPosition() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        UndoManager undoManager = new UndoManager();
        doc.addUndoableEditListener(undoManager);
        
        Position pos0 = doc.createPosition(0, Bias.Backward);
        BackwardPosition bpos0 = new BackwardPosition(doc, 0);
        doc.insertString(0, "hlo world here is Prague", null);
        assertEquals(0, pos0.getOffset()); // Insert at 0 - BB position stays at 0
        assertEquals(0, bpos0.getOffset());
        Position pos1 = doc.createPosition(1, Bias.Backward);
        BackwardPosition bpos1 = new BackwardPosition(doc, 1);
        doc.insertString(1, "el", null);
        assertEquals(1, pos1.getOffset()); // Insert at 1 - BB position stays at 1
        assertEquals(1, bpos1.getOffset());
        Position pos3 = doc.createPosition(3, Bias.Backward);
        BackwardPosition bpos3 = new BackwardPosition(doc, 3);
        Position pos4 = doc.createPosition(4, Bias.Backward);
        BackwardPosition bpos4 = new BackwardPosition(doc, 4);
        doc.remove(2, 2);
        assertEquals(2, pos3.getOffset()); // Removal; pos3 inside => moved to 2
        assertEquals(2, bpos3.getOffset());
        assertEquals(2, pos4.getOffset()); // Removal; pos3 inside => moved to 2
        assertEquals(2, bpos4.getOffset());
        Position pos22 = doc.createPosition(2, Bias.Backward); // pos at 2 after removal (3 originally)
        BackwardPosition bpos22 = new BackwardPosition(doc, 2);
        undoManager.undo(); // Undo of removal means insertion
        assertEquals(3, pos3.getOffset()); // Undo of removal => return BB pos to orig. offset
        assertEquals(2, bpos3.getOffset());
        assertEquals(4, pos4.getOffset()); // Undo -> 4
        assertEquals(2, bpos4.getOffset());
        // BB position created prior undo of removal => position not moved (like on insert)
        assertEquals(3, pos22.getOffset());
        assertEquals(2, bpos22.getOffset());
        
        Position pos12 = doc.createPosition(10, Bias.Backward);
        doc.remove(8, 2);
        undoManager.undo();
        assertEquals(10, pos12.getOffset());
        
        Position pos16 = doc.createPosition(16, Bias.Backward);
        doc.remove(14, 2);
        doc.insertString(14, "haf", null);
        assertEquals(14, pos16.getOffset());

        Position pos19 = doc.createPosition(19, Bias.Backward);
        doc.remove(18, 2);
        doc.insertString(18, "haf", null);
        assertEquals(18, pos19.getOffset());
    }

    public void testRowUtilities() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain"); // NOI18N
        doc.insertString(0, "a\nbc", null);
        int offset = Utilities.getRowStart(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 2, offset); // NOI18N
        offset = Utilities.getFirstNonWhiteBwd(doc, doc.getLength(), 0);
        assertEquals("Invalid offset", doc.getLength() - 1, offset); // NOI18N
        offset = Utilities.getFirstNonWhiteBwd(doc, 1, 0);
        assertEquals("Invalid offset", 0, offset); // NOI18N
        offset = Utilities.getRowLastNonWhite(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 3, offset); // NOI18N
        offset = Utilities.getRowEnd(doc, doc.getLength() + 1);
        assertEquals("Invalid offset", 4, offset); // NOI18N
        int index = Utilities.getLineOffset(doc, doc.getLength() + 1);
        assertEquals("Invalid index", 1, index); // NOI18N
    }

    public void testGetText() throws Exception {
        BaseDocument doc = new BaseDocument(false, "text/plain");
        CharSequence text = DocumentUtilities.getText(doc);
        assertEquals(1, text.length());
        assertEquals('\n', text.charAt(0));

        text = DocumentUtilities.getText(doc);
        doc.insertString(0, "a\nb", null);
        for (int i = 0; i < doc.getLength() + 1; i++) {
            assertEquals(doc.getText(i, 1).charAt(0), text.charAt(i));
        }
    }

    public void testParagraphUpdates() throws Exception {
        paragraphUpdatesImpl(new PlainDocument());
        BaseDocument doc = new BaseDocument(false, "text/plain");
        paragraphUpdatesImpl(doc);
    }

    public void paragraphUpdatesImpl(Document doc) throws Exception {
        doc.addDocumentListener(new DocumentListener() {
            int version;

            @Override
            public void insertUpdate(DocumentEvent e) {
                switch (version++) {
                    case 0:
                        assertLineElementChange(e, 0, 17, 0, 3, 3, 7, 7, 8, 8, 14, 14, 17);
                        break;
                    default:
                        fail("Invalid insertUpdate version=" + version);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                switch (version++) {
                    case 1:
                        assertLineElementChange(e, 8, 10, 10, 11, 8, 11);
                        break;
                    default:
                        fail("Invalid insertUpdate version=" + version);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            
            private void assertLineElementChange(DocumentEvent evt, int... startEndOffsets) {
                int offsetsIndex = 0;
                DocumentEvent.ElementChange lineElementChange = evt.getChange(evt.getDocument().getDefaultRootElement());
                if (lineElementChange != null) {
                    Element[] removedLines = lineElementChange.getChildrenRemoved();
                    for (int i = 0; i < removedLines.length; i++) {
                        assertElementBounds(removedLines[i], startEndOffsets, offsetsIndex);
                        offsetsIndex += 2;
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    for (int i = 0; i < addedLines.length; i++) {
                        assertElementBounds(addedLines[i], startEndOffsets, offsetsIndex);
                        offsetsIndex += 2;
                    }
                }
            }
            
            private void assertElementBounds(Element line, int[] startEndOffsets, int index) {
                assertTrue("startEndOffsets.length=" + startEndOffsets.length + " < " + (index + 2), index + 2 <= startEndOffsets.length);
                assertEquals("Invalid line[" + (index >> 1) + "] startOffset", startEndOffsets[index], line.getStartOffset());
                assertEquals("Invalid line[" + (index >> 1) + "] endOffset", startEndOffsets[index + 1], line.getEndOffset());

            }
        });
        doc.insertString(0, "ab\ncde\n\nfghij\nkl", null);
        doc.remove(10, 6);
    }

    public void testRecursiveUndoableEdits() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        class UEL implements UndoableEditListener, Runnable {
            boolean undo;
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                //doc.runAtomic(this);
                doc.render(this);
                undo = e.getEdit().canUndo();
            }

            @Override
            public void run() {
            }
        }
        UEL uel = new UEL();
        doc.addUndoableEditListener(uel);

        class Atom implements Runnable {
            @Override
            public void run() {
                try {
                    doc.insertString(0, "Ahoj", null);
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        doc.runAtomicAsUser(new Atom());

        assertTrue("Can undo now", uel.undo);
    }

    public void testBreakAtomicLock() throws Exception {
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.breakAtomicLock();
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
        boolean failure = false;
        try {
            doc.runAtomic(new Runnable() {
                public @Override void run() {
                    throw new IllegalStateException("test");
                }
            });
            failure = true;
        } catch (Throwable t) {
            // Expected
        }
        if (failure) {
            throw new IllegalStateException("Unexpected");
        }
        doc.runAtomic(new Runnable() {
            public @Override void run() {
                try {
                    doc.insertString(0, "test1", null);
                    doc.insertString(10, "test2", null);
                } catch (BadLocationException e) {
                    // Expected
                }
            }
        });
    }

    public void testPropertyChangeEvents() {
        final List<PropertyChangeEvent> events = new LinkedList<PropertyChangeEvent>();
        final BaseDocument doc = new BaseDocument(false, "text/plain");
        final PropertyChangeListener l = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                events.add(evt);
            }
        };

        DocumentUtilities.addPropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-A", "value-A");
        assertEquals("No event fired", 1, events.size());
        assertEquals("Wrong property name", "prop-A", events.get(0).getPropertyName());
        assertNull("Wrong old property value", events.get(0).getOldValue());
        assertEquals("Wrong new property value", "value-A", events.get(0).getNewValue());

        events.clear();
        DocumentUtilities.removePropertyChangeListener(doc, l);
        assertEquals("No events expected", 0, events.size());

        doc.putProperty("prop-B", "value-B");
        assertEquals("Expecting no events on removed listener", 0, events.size());
    }

    static class BackwardPosition implements Position, DocumentListener { // Like in openide.text

        private int offset;

        BackwardPosition(Document doc, int offset) {
            this.offset = offset;
            doc.addDocumentListener(org.openide.util.WeakListeners.document(this, doc));
        }

        public int getOffset() {
            return offset;
        }

        public void insertUpdate(DocumentEvent e) {
            // less, not less and equal
            if (e.getOffset() < offset) {
                offset += e.getLength();
            }
        }

        public void removeUpdate(DocumentEvent e) {
            int o = e.getOffset();
            if (o < offset) {
                offset -= e.getLength();
                // was the position in deleted range? => go to its beginning
                if (offset < o) {
                    offset = o;
                }
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }

}
    