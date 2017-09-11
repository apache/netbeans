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

package org.netbeans.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.undo.UndoManager;
import junit.framework.TestCase;

/**
 * Test compatibility of the editor's document implementation
 * with the PlainDocument.
 *
 * @author mmetelka
 */
public class PlainDocumentCompatibilityRandomTest extends TestCase
implements DocumentListener {
    
    private static final boolean debug = false;
    private static final boolean debugLines = false;
    
    private static final int OP_COUNT_1 = 10000;
    private static final int INSERT_RATIO_1 = 100;
    private static final int INSERT_MAX_LENGTH_1 = 10;
    private static final float INSERT_NL_RATIO_1 = 0.5f;
    private static final int REMOVE_RATIO_1 = 70;
    private static final int REMOVE_MAX_LENGTH_1 = 10;
    // [TODO] Reset to zero temporarily
    // as the current document content has certain specifics for positions undo
    private static final int CREATE_POSITION_RATIO_1 = 0;
    private static final int RELEASE_POSITION_RATIO_1 = 20;
    private static final int UNDO_RATIO_1 = 30;
    private static final int UNDO_MAX_COUNT_1 = 5;
    private static final int REDO_RATIO_1 = 30;
    private static final int REDO_MAX_COUNT_1 = 5;
    
    private static final int OP_COUNT_2 = 10000;
    private static final int INSERT_RATIO_2 = 70;
    private static final int INSERT_MAX_LENGTH_2 = 10;
    private static final float INSERT_NL_RATIO_2 = 0.2f;
    private static final int REMOVE_RATIO_2 = 100;
    private static final int REMOVE_MAX_LENGTH_2 = 10;
    // [TODO] Reset to zero temporarily
    // as the current document content has certain specifics for positions undo
    private static final int CREATE_POSITION_RATIO_2 = 0;
    private static final int RELEASE_POSITION_RATIO_2 = 40;
    private static final int UNDO_RATIO_2 = 30;
    private static final int UNDO_MAX_COUNT_2 = 5;
    private static final int REDO_RATIO_2 = 30;
    private static final int REDO_MAX_COUNT_2 = 5;

    private PlainDocument masterDoc;
    
    private BaseDocument testDoc;
    
    private DocumentEvent masterEvent;
    
    private List masterPositions = new ArrayList();
    
    private List testPositions = new ArrayList();
    
    private UndoManager masterUndoManager = new UndoManager();
    
    private UndoManager testUndoManager = new UndoManager();
    
    public PlainDocumentCompatibilityRandomTest(String testName) {
        super(testName);
    }
    
    public void test() {
        testFresh(0);
    }

    public void testFresh(long seed) {
        if (seed == 0) {
            seed = System.currentTimeMillis();
            System.err.println("Chosen SEED=" + seed);
        }
        Random random = new Random(seed);

        
        if (debug) {
            System.err.println("TESTING with SEED=" + seed);
        }

        masterDoc = new PlainDocument();
        testDoc = new BaseDocument(BaseKit.class, false);
        
        // Atach document listener
        masterDoc.addDocumentListener(this);
        testDoc.addDocumentListener(this);
        
        // Attach undoable listeners
        masterDoc.addUndoableEditListener(masterUndoManager);
        testDoc.addUndoableEditListener(testUndoManager);
        
        testRound(random, OP_COUNT_1,
            INSERT_RATIO_1, INSERT_MAX_LENGTH_1, INSERT_NL_RATIO_1,
            REMOVE_RATIO_1, REMOVE_MAX_LENGTH_1,
            CREATE_POSITION_RATIO_1, RELEASE_POSITION_RATIO_1,
            UNDO_RATIO_1, UNDO_MAX_COUNT_1,
            REDO_RATIO_1, REDO_MAX_COUNT_1
        );

        testRound(random, OP_COUNT_2,
            INSERT_RATIO_2, INSERT_MAX_LENGTH_2, INSERT_NL_RATIO_2,
            REMOVE_RATIO_2, REMOVE_MAX_LENGTH_2,
            CREATE_POSITION_RATIO_2, RELEASE_POSITION_RATIO_2,
            UNDO_RATIO_2, UNDO_MAX_COUNT_2,
            REDO_RATIO_2, REDO_MAX_COUNT_2
        );
        
        // Detach undo managers
        masterDoc.removeUndoableEditListener(masterUndoManager);
        testDoc.removeUndoableEditListener(testUndoManager);
        
        // Clear undo managers
        masterUndoManager.discardAllEdits();
        testUndoManager.discardAllEdits();
        
        // Remove document listeners
        masterDoc.removeDocumentListener(this);
        testDoc.removeDocumentListener(this);
    }
    
    private void testRound(Random random, int opCount,
    int insertRatio, int insertMaxLength, float insertNlRatio,
    int removeRatio, int removeMaxLength,
    int createPositionRatio, int releasePositionRatio,
    int undoRatio, int undoMaxCount,
    int redoRatio, int redoMaxCount) {
        
        int ratioSum = insertRatio + removeRatio
            + createPositionRatio + releasePositionRatio
            + undoRatio + redoRatio;
        
        for (int op = 0; op < opCount; op++) {
            double r = random.nextDouble() * ratioSum;
            int docLength = masterDoc.getLength();
            
            if (debugLines) {
                System.err.println("LINES:\n" + linesToString());
            }
            
            if ((r -= insertRatio) < 0) {
                int offset = (int)((docLength + 1) * random.nextDouble());
                int length = (int)(insertMaxLength * random.nextDouble());
                StringBuffer sb = new StringBuffer();
                StringBuffer debugSb = debug ? new StringBuffer() : null;

                for (int i = length - 1; i >= 0; i--) {
                    char ch;
                    if (random.nextDouble() < insertNlRatio) { // insert '\n'
                        ch = '\n';
                        if (debug) {
                            debugSb.append("\\n");
                        }

                    } else { // insert regular char
                        ch = (char)('a' + (int)(26 * random.nextDouble()));
                        if (debug) {
                            debugSb.append(ch);
                        }
                    }
                    sb.append(ch);
                }
                if (debug) {
                    debugOp(op, "insertString(" + offset + ", \"" + debugSb + "\")");
                }

                try {
                    masterDoc.insertString(offset, sb.toString(), null);
                    testDoc.insertString(offset, sb.toString(), null);
                    // Reset undoable events merging
                    testDoc.resetUndoMerge();
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }

                
            } else if ((r -= removeRatio) < 0) {
                int offset = (int)((docLength + 1) * random.nextDouble());
                int length = (int)(removeMaxLength * random.nextDouble());
                length = Math.min(length, docLength - offset);

                if (debug) {
                    debugOp(op, "remove(" + offset + ", " + length + ")");
                }

                try {
                    masterDoc.remove(offset, length);
                    testDoc.remove(offset, length);
                    // Reset undoable events merging
                    testDoc.resetUndoMerge();
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }

            } else if ((r -= createPositionRatio) < 0) {
                // Allow position at docLength + 1
                int offset = (int)((docLength + 2) * random.nextDouble());

                if (debug) {
                    debugOp(op, "createPosition(" + offset + ")");
                }
                try {
                    masterPositions.add(masterDoc.createPosition(offset));
                    testPositions.add(testDoc.createPosition(offset));
                } catch (BadLocationException e) {
                    throw new RuntimeException(e);
                }

                
            } else if ((r -= releasePositionRatio) < 0) {
                int masterPositionsCount = masterPositions.size();
                if (masterPositionsCount > 0) {
                    int index = (int)(masterPositionsCount * random.nextDouble());
    
                    if (debug) {
                        debugOp(op, "release position at index=" + index);
                    }

                    masterPositions.remove(index);
                    testPositions.remove(index);
                }

            } else if ((r -= undoRatio) < 0) {
                int undoCount = (int)(undoMaxCount * random.nextDouble());

                if (debug) {
                    debugOp(op, "undo(" + undoCount + ")");
                }
                
                while (undoCount > 0) {
                    undoCount--;
                    if (masterUndoManager.canUndo()) {
                        masterUndoManager.undo();
                        testUndoManager.undo();
                        if (undoCount > 0) {
                            checkConsistency(); // Check consistency after each undo
                        }
                    }
                }

            } else if ((r -= redoRatio) < 0) {
                int redoCount = (int)(redoMaxCount * random.nextDouble());

                if (debug) {
                    debugOp(op, "redo(" + redoCount + ")");
                }
                
                while (redoCount > 0) {
                    redoCount--;
                    if (masterUndoManager.canRedo()) {
                        masterUndoManager.redo();
                        testUndoManager.redo();
                        if (redoCount > 0) {
                            checkConsistency(); // Check consistency after each redo
                        }
                    }
                }
            }

            checkConsistency();
        }
        
    }
        
    private void debugOp(int op, String s) {
        System.err.println("op: " + op + ", " + s);
    }
    
    private void checkConsistency() {
        try {
            int docLength = masterDoc.getLength();
            assertEquals(docLength, testDoc.getLength());

            String masterText = masterDoc.getText(0, docLength);
            String testText = testDoc.getText(0, docLength);
            assertEquals(masterText, testText);

            Element lineRoot = masterDoc.getDefaultRootElement();
            Element testLineRoot = testDoc.getDefaultRootElement();
            int lineCount = lineRoot.getElementCount();
            if (lineCount != testLineRoot.getElementCount()) {
                fail("Line count " + testLineRoot.getElementCount()
                    + " != " + lineCount);
            }
            // Compare line boundaries
            for (int i = 0; i < lineCount; i++) {
                Element masterLine = lineRoot.getElement(i);
                Element testLine = testLineRoot.getElement(i);
                if (masterLine.getStartOffset() != testLine.getStartOffset()) {
                    fail("Start of line " + i + ": Offset " + testLine.getStartOffset()
                        + " != " + masterLine.getStartOffset());
                }
                if (masterLine.getEndOffset() != testLine.getEndOffset()) {
                    fail("End of line " + i + ": Offset " + testLine.getEndOffset()
                        + " != " + masterLine.getEndOffset());
                }
            }

            int positionCount = masterPositions.size();
            for (int i = 0; i < positionCount; i++) {
                Position masterPos = (Position)masterPositions.get(i);
                Position testPos = (Position)testPositions.get(i);
                if (masterPos.getOffset() != testPos.getOffset()) {
                    fail("Tested position " + (i + 1) + " of " + positionCount
                        + ": " + testPos.getOffset()
                        + " != " + masterPos.getOffset());
                }
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private String linesToString() {
        StringBuffer sb = new StringBuffer();
        Element masterLineRoot = masterDoc.getDefaultRootElement();
        Element testLineRoot = testDoc.getDefaultRootElement();
        int masterLineCount = masterLineRoot.getElementCount();
        int testLineCount = testLineRoot.getElementCount();
        int lineCount = Math.max(masterLineCount, testLineCount);
        sb.append("Line count=" + lineCount + "\n");
        for (int i = 0; i < lineCount; i++) {
            if (i < testLineCount) {
                Element line = testLineRoot.getElement(i);
                sb.append("[" + i + "]: <" + line.getStartOffset() + ", " + line.getEndOffset() + "> ");
            } else {
                sb.append(" <!NONE!> ");
            }

            if (i < masterLineCount) {
                Element line = masterLineRoot.getElement(i);
                sb.append("[" + i + "]: <" + line.getStartOffset() + ", " + line.getEndOffset() + ">\n");
            } else {
                sb.append(" <!NONE!>\n");
            }
        }
        return sb.toString();
    }
    
    private void checkEventsEqual(DocumentEvent testEvent) {
        if (masterEvent.getOffset() != testEvent.getOffset()) {
            fail("masterEvent.getOffset()=" + masterEvent.getOffset()
                + " != testEvent.getOffset()=" + testEvent.getOffset());
        }
        if (masterEvent.getLength() != testEvent.getLength()) {
            fail("masterEvent.getLength()=" + masterEvent.getLength()
                + " != testEvent.getLength()=" + testEvent.getLength());
        }
        if (masterEvent.getType() != testEvent.getType()) {
            fail("masterEvent.getType()=" + masterEvent.getType()
                + " != testEvent.getType()=" + testEvent.getType());
        }
        DocumentEvent.ElementChange masterChange = masterEvent.getChange(masterDoc.getDefaultRootElement());
        DocumentEvent.ElementChange testChange = testEvent.getChange(testDoc.getDefaultRootElement());
        checkElementChangesEqual(masterChange, testChange);
    }
    
    private void checkElementChangesEqual(DocumentEvent.ElementChange masterChange,
    DocumentEvent.ElementChange testChange) {
        
        if (masterChange == null && testChange == null) { // no line changes
            return;
        }
        
        if (masterChange == null && testChange != null) {
            fail("masterChange is null");
        }
        if (masterChange != null && testChange == null) {
            fail("testChange is null");
        }
        
        // Both changes are not null
        int masterIndex = masterChange.getIndex();
        int testIndex = testChange.getIndex();
        if (masterIndex != testIndex) {
            fail("masterIndex=" + masterIndex + " != testIndex=" + testIndex);
        }
        Element[] masterAdded = masterChange.getChildrenAdded();
        Element[] testAdded = testChange.getChildrenAdded();
        if (masterAdded.length != testAdded.length) {
            fail("masterAdded.length=" + masterAdded.length 
                + "!= testAdded.length=" + testAdded.length);
        }
        Element[] masterRemoved = masterChange.getChildrenRemoved();
        Element[] testRemoved = testChange.getChildrenRemoved();
        if (masterRemoved.length != testRemoved.length) {
            fail("masterRemoved.length=" + masterRemoved.length 
                + "!= testRemoved.length=" + testRemoved.length);
        }
        for (int i = 0; i < masterAdded.length; i++) {
            Element masterElem = masterAdded[i];
            Element testElem = testAdded[i];
            checkElementOffsetsEqual(masterElem, testElem);
        }
        for (int i = 0; i < masterRemoved.length; i++) {
            Element masterElem = masterRemoved[i];
            Element testElem = testRemoved[i];
            checkElementOffsetsEqual(masterElem, testElem);
        }
    }
    
    private void checkElementOffsetsEqual(Element masterElem, Element testElem) {
        if (masterElem.getStartOffset() != testElem.getStartOffset()) {
            fail("masterElem.getStartOffset()=" + masterElem.getStartOffset()
                + " != testElem.getStartOffset()=" + testElem.getStartOffset());
        }
        if (masterElem.getEndOffset() != testElem.getEndOffset()) {
            fail("masterElem.getEndOffset()=" + masterElem.getEndOffset()
                + " != testElem.getEndOffset()=" + testElem.getEndOffset());
        }
    }
    
    private void processEvent(DocumentEvent evt) {
        // testDoc operations must always be done after the master ones.
        Document doc = evt.getDocument();
        if (doc == masterDoc) {
            masterEvent = evt;
        } else if (doc == testDoc) {
            checkEventsEqual(evt);
            masterEvent = null;
        } else {
            fail("Unknown document.");
        }
    }

    public void insertUpdate(DocumentEvent e) {
        processEvent(e);
    }

    public void removeUpdate(DocumentEvent e) {
        processEvent(e);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    
}
