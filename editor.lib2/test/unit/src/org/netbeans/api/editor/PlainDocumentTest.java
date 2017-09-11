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

package org.netbeans.api.editor;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.undo.UndoManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of curiosities of Swing's PlainDocument implementation.
 *
 * @author Miloslav Metelka
 */
public class PlainDocumentTest extends NbTestCase {
    
    public PlainDocumentTest(String name) {
        super(name);
    }
    
    public void testBehaviour() throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, "test hello world", null);
        UndoManager undo = new UndoManager();
        doc.addUndoableEditListener(undo);
        Position pos = doc.createPosition(2);
        doc.remove(0, 3);
        assert (pos.getOffset() == 0);
        undo.undo();
        assert (pos.getOffset() == 2);
        
        Position pos2 = doc.createPosition(5);
        doc.remove(4, 2);
        Position pos3 = doc.createPosition(4);
        assertSame(pos2, pos3);
        undo.undo();
        assert (pos3.getOffset() == 5);
    }

    public void testCuriosities() throws Exception {
        // Test position at offset 0 does not move after insert
        Document doc = new PlainDocument();
        doc.insertString(0, "test", null);
        Position pos = doc.createPosition(0);
        assertEquals(0, pos.getOffset());
        doc.insertString(0, "a", null);
        assertEquals(0, pos.getOffset());
        
        // Test there is an extra newline above doc.getLength()
        assertEquals("\n", doc.getText(doc.getLength(), 1));
        assertEquals("atest\n", doc.getText(0, doc.getLength() + 1));
        
        // Test the last line element contains the extra newline
        Element lineElem = doc.getDefaultRootElement().getElement(0);
        assertEquals(0, lineElem.getStartOffset());
        assertEquals(doc.getLength() + 1, lineElem.getEndOffset());

        // Test that once position gets to zero it won't go anywhere else (unless undo performed)
        pos = doc.createPosition(1);
        doc.remove(0, 1);
        assertEquals(0, pos.getOffset());
        doc.insertString(0, "b", null);
        assertEquals(0, pos.getOffset());
    }
    
}
