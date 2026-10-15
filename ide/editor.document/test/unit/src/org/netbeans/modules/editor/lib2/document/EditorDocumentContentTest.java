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

package org.netbeans.modules.editor.lib2.document;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.random.RandomTestContainer;
import org.netbeans.lib.editor.util.random.RandomText;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

public class EditorDocumentContentTest extends NbTestCase {

    private static final int OP_COUNT = 100;

    public EditorDocumentContentTest(String testName) {
        super(testName);
        List<String> includes = new ArrayList<String>();
//        includes.add("testSimpleUndo");
//        includes.add("testSimplePositionSharingMods");
//        includes.add("testEndPosition");
//        includes.add("testRandomMods");
//        includes.add("testInsertAtZero");
//        includes.add("testRemoveAtZero");
//        includes.add("testBackwardBiasPositionsSimple");
//        includes.add("testBackwardBiasPositions");
//        includes.add("testRemoveSimple");
//        includes.add("testMarkSwapSimple");
//        includes.add("testMarkSwapMulti");
//        includes.add("testMarkSwapMultiBB");
//        includes.add("testWholeDocRemove");
//        includes.add("testPartDocRemove");
//        filterTests(includes);
    }
    
    private void filterTests(List<String> includeTestNames) {
        List<Filter.IncludeExclude> includeTests = new ArrayList<Filter.IncludeExclude>();
        for (String testName : includeTestNames) {
            includeTests.add(new Filter.IncludeExclude(testName, ""));
        }
        Filter filter = new Filter();
        filter.setIncludes(includeTests.toArray(new Filter.IncludeExclude[0]));
        setFilter(filter);
    }

    @Override
    protected Level logLevel() {
//        return Level.FINEST;
//        return Level.FINE;
//        return Level.INFO;
        return Level.FINE;
    }

    private RandomTestContainer createContainer() throws Exception {
        RandomTestContainer container = DocumentContentTesting.createContainer();
        container.setName(this.getName());
//        container.setLogOp(true);
//        DocumentContentTesting.setLogDoc(container, true);
//        Logger.getLogger("org.netbeans.modules.editor.lib2.document.EditorDocumentContent").setLevel(Level.FINEST);
        return container;
    }
    
    private static void initRandomText(RandomTestContainer container) throws Exception {
//        container.addOp(new Op());
        RandomText randomText = RandomText.join(
                RandomText.lowerCaseAZ(3),
                RandomText.spaceTabNewline(1),
                RandomText.phrase("ab\n", 1),
                RandomText.phrase("\nxy", 1),
                RandomText.phrase("\n\n\n", 1),
                RandomText.phrase(" \t\tabcdef\t", 1)
        );
        container.putProperty(RandomText.class, randomText);
    }

    private static RandomTestContainer.Round addRound(RandomTestContainer container) throws Exception {
        RandomTestContainer.Round round = container.addRound();
        round.setOpCount(OP_COUNT); // Initially set OP_COUNT but caller may modify
        round.setRatio(DocumentContentTesting.INSERT_CHAR, 50);
        round.setRatio(DocumentContentTesting.INSERT_TEXT, 30);
        round.setRatio(DocumentContentTesting.INSERT_PHRASE, 30);
        round.setRatio(DocumentContentTesting.REMOVE_CHAR, 30);
        round.setRatio(DocumentContentTesting.REMOVE_TEXT, 10);
        round.setRatio(DocumentContentTesting.UNDO, 10);
        round.setRatio(DocumentContentTesting.REDO, 10);
        round.setRatio(DocumentContentTesting.CREATE_POSITION, 40);
        round.setRatio(DocumentContentTesting.CREATE_BACKWARD_BIAS_POSITION, 20);
        round.setRatio(DocumentContentTesting.RELEASE_POSITION, 50);
        return round;
    }

    public void testSimplePositionSharingMods() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj");
        Position pos = DocumentContentTesting.createPosition(context, 1);
        Position pos1 = DocumentContentTesting.createPosition(context, 1);
        assertSame(pos, pos1); // Positions are shared
        Position pos2 = DocumentContentTesting.createPosition(context, 2);
        DocumentContentTesting.remove(context, 1, 1);
        Position pos11 = DocumentContentTesting.createPosition(context, 1);
        assertSame(pos2, pos11); // Reuse last position among ones with the same offset
        DocumentContentTesting.insert(context, 1, "b");

        // Check subsequences correctness
        Document doc = DocumentContentTesting.getDocument(container);
        CharSequence docText = DocumentUtilities.getText(doc);
        CharSequence txt1 = docText.subSequence(0, 3);
        assertEquals('a', txt1.charAt(0));
        assertEquals('b', txt1.charAt(1));
        assertEquals('o', txt1.charAt(2));
        assertEquals("abo", txt1.toString());
        CharSequence subTxt1 = txt1.subSequence(1, 3);
        assertEquals("bo", subTxt1.toString());
        CharSequence subTxt2 = txt1.subSequence(0, 1);
        assertEquals("a", subTxt2.toString());
        CharSequence txt2 = docText.subSequence(0, 1);
        assertEquals('a', txt2.charAt(0));
        assertEquals("a", txt2.toString());
        CharSequence txt3 = docText.subSequence(2, 3);
        assertEquals('o', txt3.charAt(0));
        assertEquals("o", txt3.toString());

        Position pos111 = DocumentContentTesting.createPosition(context, 1);
        assertNotSame(pos2, pos111);
        DocumentContentTesting.undo(context, 1);
        container.runChecks(context);
    }

    public void testSimplePositionSharingModsBB() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj");
        Position pos = DocumentContentTesting.createPosition(context, 1, true);
        Position pos1 = DocumentContentTesting.createPosition(context, 1, true);
        assertSame(pos, pos1); // Positions are shared
        Position pos2 = DocumentContentTesting.createPosition(context, 2, true);
        DocumentContentTesting.remove(context, 1, 1);
        Position pos11 = DocumentContentTesting.createPosition(context, 1, true);
        if (pos11 != pos && pos11 != pos1 && pos11 != pos2) {
            fail("Existing position not reused: pos=" + pos);
        }
        DocumentContentTesting.insert(context, 1, "b");
        Position pos111 = DocumentContentTesting.createPosition(context, 1, true);
        assertSame(pos, pos111);
        DocumentContentTesting.undo(context, 1);
        container.runChecks(context);
    }

    public void testEndPosition() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        Position pos0 = DocumentContentTesting.createPosition(context, 0);
        Position pos1 = DocumentContentTesting.createPosition(context, 1);
        container.runChecks(context);
        DocumentContentTesting.insert(context, 0, "x");
        container.runChecks(context);
        Position pos11 = DocumentContentTesting.createPosition(context, 1);
        DocumentContentTesting.insert(context, 1, "abcdefghij");
        Position pos2 = DocumentContentTesting.createPosition(context, 2);
        container.runChecks(context);
        DocumentContentTesting.insert(context, 2, "u");
        container.runChecks(context);
        DocumentContentTesting.remove(context, 3, 5);
        container.runChecks(context);
    }
    
    public void testSimpleUndo() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj cau");
        Position pos4 = DocumentContentTesting.createPosition(context, 4);
        DocumentContentTesting.remove(context, 3, 4);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1);
        container.runChecks(context);
        assertEquals(pos4.getOffset(), 4);
    }

    public void testInsertAtZero() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        Position pos0 = DocumentContentTesting.createPosition(context, 0);
        container.runChecks(context);
        DocumentContentTesting.insert(context, 0, "ahoj cau");
        Position pos1 = DocumentContentTesting.createPosition(context, 1);
        Position pos3 = DocumentContentTesting.createPosition(context, 3);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 0, 4);
        container.runChecks(context);
        DocumentContentTesting.insert(context, 0, "next");
        NbTestCase.assertEquals(0, pos1.getOffset());
        container.runChecks(context);
        
    }

    public void testRemoveAtZero() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj cau");
        Position pos1 = DocumentContentTesting.createPosition(context, 1);
        Position pos3 = DocumentContentTesting.createPosition(context, 3);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 0, 4);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1);
        container.runChecks(context);
    }

    public void testRemoveSimple() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj cau");
        Position pos1 = DocumentContentTesting.createPosition(context, 1);
        Position pos2 = DocumentContentTesting.createPosition(context, 2);
        Position pos3 = DocumentContentTesting.createPosition(context, 3);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 1, 2);
        System.err.println("content:\n" +
            ((TestEditorDocument)DocumentContentTesting.getDocument(context)).getDocumentContent().toStringDetail());
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1);
        container.runChecks(context);
    }

    public void testBackwardBiasPositionsSimple() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();
        
        Position pos0 = DocumentContentTesting.createPosition(context, 0, true);
        DocumentContentTesting.insert(context, 0, "hlo world");
        container.runChecks(context);
        assertEquals(0, pos0.getOffset()); // Insert at 0 - BB position stays at 0
        Position pos1 = DocumentContentTesting.createPosition(context, 1, true);
        DocumentContentTesting.insert(context, 1, "el");
        container.runChecks(context);
        assertEquals(1, pos1.getOffset()); // Insert at 1 - BB position stays at 1
        Position pos3 = DocumentContentTesting.createPosition(context, 3, true);
        DocumentContentTesting.remove(context, 2, 2); // pos3 at 2
        container.runChecks(context);
        assertEquals(2, pos3.getOffset()); // Removal; pos3 inside => moved to 2
        Position pos22 = DocumentContentTesting.createPosition(context, 2, true); // pos at 2 after removal
        assertSame(pos22, pos3);
        DocumentContentTesting.undo(context, 1); // Undo of removal means insertion
        assertEquals(3, pos3.getOffset()); // Undo of removal => return BB pos to orig. offset
        container.runChecks(context);
    }

    public void testBackwardBiasPositions() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "ahoj cau");
        Position bbPos1 = DocumentContentTesting.createPosition(context, 1, true);
//        Position pos2 = DocumentContentTesting.createPosition(context, 2);
        Position bbPos3 = DocumentContentTesting.createPosition(context, 3, true);
        DocumentContentTesting.insert(context, 3, "test");
        container.runChecks(context);
        DocumentContentTesting.remove(context, 0, 4);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1); // checkContent() automatic
        DocumentContentTesting.undo(context, 1); // checkContent() automatic
        DocumentContentTesting.redo(context, 1); // checkContent() automatic
        DocumentContentTesting.redo(context, 1); // checkContent() automatic
        container.runChecks(context);
    }

    public void testMarkSwapSimple() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "abcdefg");
        Position pos2 = DocumentContentTesting.createPosition(context, 2); // pos2=2
        DocumentContentTesting.remove(context, 2, 1); // stays at 2
        DocumentContentTesting.insert(context, 2, "x"); // becomes: 3
        Position pos3 = DocumentContentTesting.createPosition(context, 2); // becomes: pos3=2 3
        assertNotSame(pos3, pos2);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1); // Undo Ins(2, "x") tj. Rem(2,1) => becomes: pos3=2 2
        DocumentContentTesting.undo(context, 1); // Undo Rem(2, 1) tj. Ins(2,"c") => becomes: pos3=3 2
        container.runChecks(context);
    }

    public void testMarkSwapMulti() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "abcdefghijkl");
        Position pos2 = DocumentContentTesting.createPosition(context, 2);
        Position pos3 = DocumentContentTesting.createPosition(context, 3);
        Position pos5 = DocumentContentTesting.createPosition(context, 5);
        DocumentContentTesting.remove(context, 2, 4);
        DocumentContentTesting.insert(context, 2, "xyzw");
        Position pos22 = DocumentContentTesting.createPosition(context, 2);
        Position pos23 = DocumentContentTesting.createPosition(context, 3);
        Position pos24 = DocumentContentTesting.createPosition(context, 4);
        Position pos25 = DocumentContentTesting.createPosition(context, 5);
        assertNotSame(pos3, pos2);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1); // Undo Ins(2, "x") tj. Rem(2,1) => becomes: pos3=2 2
        DocumentContentTesting.undo(context, 1); // Undo Rem(2, 1) tj. Ins(2,"c") => becomes: pos3=3 2
        container.runChecks(context);
    }

    public void testMarkSwapMultiBB() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();

        DocumentContentTesting.insert(context, 0, "abcdefghijkl");
        Position pos2 = DocumentContentTesting.createPosition(context, 2, true);
        Position pos3 = DocumentContentTesting.createPosition(context, 3, true);
        Position pos5 = DocumentContentTesting.createPosition(context, 5, true);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 2, 4);
        DocumentContentTesting.insert(context, 2, "xyzw");
        Position pos22 = DocumentContentTesting.createPosition(context, 2, true);
        Position pos23 = DocumentContentTesting.createPosition(context, 3, true);
        Position pos24 = DocumentContentTesting.createPosition(context, 4, true);
        Position pos25 = DocumentContentTesting.createPosition(context, 5, true);
        assertNotSame(pos3, pos2);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 1); // Undo Ins(2, "x") tj. Rem(2,1) => becomes: pos3=2 2
        DocumentContentTesting.undo(context, 1); // Undo Rem(2, 1) tj. Ins(2,"c") => becomes: pos3=3 2
        container.runChecks(context);
    }

    public void testWholeDocRemove() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();
        Document doc = DocumentContentTesting.getDocument(container);
        DocumentContentTesting.insert(context, 0, "ahoj");
//        Position pos7 = DocumentContentTesting.createPosition(context, doc.getLength() + 1, false);

//        Position pos1 = DocumentContentTesting.createPosition(context, 1, false);
        Position pos2 = DocumentContentTesting.createPosition(context, 2, false);
//        Position pos5 = DocumentContentTesting.createPosition(context, 5, false);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 0, doc.getLength());
        container.runChecks(context);
        DocumentContentTesting.insert(context, 0, "nazdar");
        container.runChecks(context);
//        Position pos21 = DocumentContentTesting.createPosition(context, 1, false);
//        Position pos22 = DocumentContentTesting.createPosition(context, 2, false);
        Position pos25 = DocumentContentTesting.createPosition(context, 5, false);
//        Position pos26 = DocumentContentTesting.createPosition(context, 6, false);
//        Position pos27 = DocumentContentTesting.createPosition(context, 7, false);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 2);
//        Position pos31 = DocumentContentTesting.createPosition(context, 1, false);
//        Position pos34 = DocumentContentTesting.createPosition(context, 4, false);
//        Position pos36 = DocumentContentTesting.createPosition(context, 6, false);
        container.runChecks(context);
        DocumentContentTesting.redo(context, 1);
        DocumentContentTesting.redo(context, 1);
        container.runChecks(context);
    }

    public void testPartDocRemove() throws Exception {
        RandomTestContainer container = createContainer();
        RandomTestContainer.Context context = container.context();
        container.setLogOp(true);
        Logger.getLogger("org.netbeans.modules.editor.lib2.document.EditorDocumentContent").setLevel(Level.FINEST);
        Document doc = DocumentContentTesting.getDocument(container);
        DocumentContentTesting.insert(context, 0, "ahoj");
        Position pos2 = DocumentContentTesting.createPosition(context, 3, false);
        container.runChecks(context);
        DocumentContentTesting.remove(context, 2, doc.getLength() - 2);
        container.runChecks(context);
        DocumentContentTesting.insert(context, 2, "nazdar");
        container.runChecks(context);
        Position pos25 = DocumentContentTesting.createPosition(context, 5, false);
        container.runChecks(context);
        DocumentContentTesting.undo(context, 2);
        container.runChecks(context);
        DocumentContentTesting.redo(context, 1);
        DocumentContentTesting.redo(context, 1);
        container.runChecks(context);
    }

    public void testRandomMods() throws Exception {
        RandomTestContainer container = createContainer();
//        final Document doc = DocumentContentTesting.getDocument(container);
        initRandomText(container);
        RandomTestContainer.Round round = addRound(container);
        round.setOpCount(10000);
        container.setLogOp(false);
        container.runInit(1303538860896L);
        container.runOps(771);
        container.runOps(0);

        // Testing a particular failure
        container.runInit(1401855220204L);
        container.runOps(7887);
        container.runOps(1);
        container.runOps(0);
        
        // Run random testing)
        container.run(0L);
    }

}
