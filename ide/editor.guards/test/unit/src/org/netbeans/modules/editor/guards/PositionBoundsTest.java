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
package org.netbeans.modules.editor.guards;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.Editor;
import org.netbeans.api.editor.guards.GuardUtils;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Pokorsky
 */
public class PositionBoundsTest extends TestCase {
    
    private Editor editor;
    private GuardedSectionsImpl guardsImpl;
    
    /** Creates a new instance of PositionBoundsTest */
    public PositionBoundsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        this.editor = new Editor();
        this.guardsImpl = new GuardedSectionsImpl(this.editor);
        GuardUtils.initManager(this.editor, this.guardsImpl);
    }
    
    public void testCreatePositionBounds() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 2), bounds.getText());
        assertEquals("getText2", "ac", bounds.getText());
    }
    
    public void testChangesInPositionBounds() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        editor.doc.insertString(2, "b", null);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 3), bounds.getText());
        assertEquals("getText2", "abc", bounds.getText());
    }
    
    public void testSetText() throws BadLocationException {
        editor.doc.insertString(0, "_abcd", null);
        PositionBounds bounds = PositionBounds.create(1, 4, guardsImpl);
        doTestSetText(bounds);
    }

    public void testSetTextWithUnresolvedBounds() throws BadLocationException {
        editor.doc.insertString(0, "_abcd", null);
        PositionBounds bounds = PositionBounds.createUnresolved(1, 4, guardsImpl);
        bounds.resolvePositions();
        doTestSetText(bounds);
    }

    public void testSetTextWithBodyBounds() throws BadLocationException {
        editor.doc.insertString(0, "_abcd", null);
        PositionBounds bounds = PositionBounds.createBodyBounds(1, 4, guardsImpl);
        doTestSetText(bounds);
    }

    public void testSetTextWithUnresolvedBodyBounds() throws BadLocationException {
        editor.doc.insertString(0, "_abcd", null);
        PositionBounds bounds = PositionBounds.createBodyUnresolved(1, 4, guardsImpl);
        bounds.resolvePositions();
        doTestSetText(bounds);
    }

    private void doTestSetText(PositionBounds bounds) throws BadLocationException {
        // test position bounds content changes; doc="_abcd"; pb="abc"
        bounds.setText("xy");
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_xyd".length(), editor.doc.getLength());

        // test position bounds content changes; doc="_xyd"; pb="xy"
        bounds.setText("1234");
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 5, bounds.getEnd().getOffset());
        assertEquals("getText", "1234", bounds.getText());
        assertEquals("doc length", "_1234d".length(), editor.doc.getLength());
    }

    public void testInsertionBeforeBounds() throws BadLocationException {
        editor.doc.insertString(0, "_xyd", null);
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        // test insertion before bounds; doc="_xyd"; pb="xy"
        editor.doc.insertString(1, "a", null);
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
    }
    
    public void testSetEmptyText() throws BadLocationException {
        editor.doc.insertString(0, "_axyd", null);
        PositionBounds bounds = PositionBounds.create(2, 4, guardsImpl);
        
        // test cleaning position bounds; doc="_axyd"; pb="xy"
        bounds.setText("");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 2, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        assertEquals("doc length", "_ad".length(), editor.doc.getLength());
        
        bounds.setText("xy");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
    }
    
    public void testDocumentClean() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        
        editor.doc.remove(0, editor.doc.getLength());
        assertEquals("start", 0, bounds.getBegin().getOffset());
        assertEquals("end", 0, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
    }
        
    public void testComplexSetText() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 2), bounds.getText());
        assertEquals("getText2", "ac", bounds.getText());
        
        // test document changes inside the position bounds
        editor.doc.insertString(2, "b", null);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 3), bounds.getText());
        assertEquals("getText2", "abc", bounds.getText());
        
        // test position bounds content changes; doc="_abcd"; pb="abc"
        bounds.setText("xy");
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_xyd".length(), editor.doc.getLength());
        
        // test insertion before bounds; doc="_xyd"; pb="xy"
        editor.doc.insertString(1, "a", null);
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
        
        // test cleaning position bounds; doc="_axyd"; pb="xy"
        bounds.setText("");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 2, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        assertEquals("doc length", "_ad".length(), editor.doc.getLength());
        
        // test cleaning document
        bounds.setText("xy");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
        
        editor.doc.remove(0, editor.doc.getLength());
        assertEquals("start", 0, bounds.getBegin().getOffset());
        assertEquals("end", 0, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        
    }
    
    public void testSetTextWithGuardMarks() throws Throwable {
        final Throwable[] ts = new Throwable[1];
        NbDocument.runAtomic(editor.doc, new Runnable() {
            public void run() {
                try {
                    doTestSetTextWithGuardMarks();
                } catch (Throwable ex) {
                    ts[0] = ex;
                }
            }
        });
        if (ts[0] != null) {
            throw ts[0];
        }
    }
    
    private void doTestSetTextWithGuardMarks() throws BadLocationException {
        StyledDocument doc = editor.doc;
        doc.insertString(0, "abcdef", null);
        Position p = doc.createPosition(1);
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        NbDocument.markGuarded(doc, 1, 3);
        // As of #174294 the GuardedDocument.isPosGuarded returns false
        // at the begining of an intra-line guarded section since an insert is allowed there.
        assertFalse(GuardUtils.isGuarded(doc, 1));
        assertTrue(GuardUtils.isGuarded(doc, 2));
        
        doc.insertString(1, "x", null);
        assertEquals(2, p.getOffset());
        assertTrue(GuardUtils.isGuarded(doc, 3));
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        
        doc.insertString(4, "x", null);
        assertEquals(2, p.getOffset());
        assertTrue(GuardUtils.isGuarded(doc, 4));
        assertTrue(GuardUtils.isGuarded(doc, 3));
        assertTrue(GuardUtils.isGuarded(doc, 5));
        assertFalse(GuardUtils.isGuarded(doc, 2));
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        GuardUtils.dumpGuardedAttr(doc);
        
        doc.remove(1, 1);
        assertEquals(1, p.getOffset());
    }
    
}
