/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
