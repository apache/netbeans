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
package org.netbeans.test.java.editor.actions;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import junit.textui.TestRunner;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.openide.util.Exceptions;

/**
 * Basic Edit Actions Test class. The base edit actions can be found at:
 * http://editor.netbeans.org/doc/UserView/apdx_a_eshortcuts.html
 *
 * @author Martin Roskanin, Jiri Prox
 */
public class JavaEditActionsTest extends JavaEditorActionsTestCase {

    private static EditorOperator editor;
    private static JEditorPaneOperator txtOper;

    /**
     * Creates a new instance of Main
     */
    public JavaEditActionsTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); //To change body of generated methods, choose Tools | Templates.
        System.out.println("********" + this.getName() + "********");
        openDefaultProject();
    }

    @Override
    protected void tearDown() throws Exception {
        if (editor != null) {
            editor.closeDiscard();
        }
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }

    private void openFile(String fileName) {
        openSourceFile(this.getClass().getCanonicalName(), fileName + ".java");
        editor = new EditorOperator(fileName);
        editor.requestFocus();
        txtOper = editor.txtEditorPane();
    }

    private void openFileAndSetEditorState(String fileName, String goldenFile, int caretLine, int caretColumn) {
        openFile(fileName);
        setEditorState(editor, goldenFile, caretLine, caretColumn);
    }

    public void testEditActionsTestCase_0() {
        openFile("testEditActions");
        // 00 ---------------------- test insert action -----------------
        // 1. move to adequate place
        editor.setCaretPosition(5, 17);
        // 2. set insert Mode ON
        txtOper.pushKey(KeyEvent.VK_INSERT);
        // 3. type d
        txtOper.typeKey('d');
        // 4. set insert Mode OFF
        txtOper.pushKey(KeyEvent.VK_INSERT);
        // 5. type x
        txtOper.typeKey('x');
        // -> previous word "ins|ert", with caret at | should be modified to "insdx|rt"
        // 6. compare document content to golden file to check if the change took place
        compareToGoldenFile(editor, "testEditActionsTestCase_000", "testEditActions00", "testEditActions00");
    }

    public void testEditActionsTestCase_1() {
        // 01 -------- test delete word action. Caret in the middle of the word ---
        // remove-word action has been removed. Changing test to delete selected word
        openFileAndSetEditorState("testEditActions", "testEditActions00.pass", 5, 19);
        editor.setCaretPosition(17, 20);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DELETE);
        compareToGoldenFile(editor, "testEditActionsTestCase_100", "testEditActions01", "testEditActions01");
    }

    public void testEditActionsTestCase_2() {
        openFileAndSetEditorState("testEditActions", "testEditActions01.pass", 17, 17);
        // 02 -------- test delete previous word action. Caret after the word ------
        //  delete word - Caret after the word was removed
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_200", "testEditActions02", "testEditActions02");
    }

    public void testEditActionsTestCase_3() {
        openFileAndSetEditorState("testEditActions", "testEditActions02.pass", 17, 10);
        // 03 --------- test remove the current line --------------------
        txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_300", "testEditActions03", "testEditActions03");
    }

    public void testEditActionsTestCase_4() {
        openFileAndSetEditorState("testEditActions", "testEditActions03.pass", 17, 1);
        // 04 -- test Select the word the insertion point is on or
        // -- deselect any selected text (Alt + j)
        // -- after that test CUT action ---------------
        editor.setCaretPosition(9, 24);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_400", "testEditActions04", "testEditActions04");
    }

    public void testEditActionsTestCase_5() {
        openFileAndSetEditorState("testEditActions", "testEditActions04.pass", 9, 21);
        // 05 -- test PASTE ------
        editor.setCaretPosition(10, 14);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(11, 17);
        txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_500", "testEditActions05", "testEditActions05");
    }

    public void testEditActionsTestCase_6() throws InterruptedException {
        openFileAndSetEditorState("testEditActions", "testEditActions05.pass", 11, 23);
        // 06 -- test UNDO/REDO ----
        editor.setCaretPosition(11, 20);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        int oldDocLength = txtOper.getDocument().getLength();
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLength), Boolean.FALSE);
        oldDocLength = txtOper.getDocument().getLength();
        txtOper.pushKey(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLength), Boolean.FALSE);        
        compareToGoldenFile(editor, "testEditActionsTestCase_600", "testEditActions06", "testEditActions06");
    }

    public void testEditActionsTestCase_7() {
        openFileAndSetEditorState("testEditActions", "testEditActions06.pass", 9, 21);
        // 07 -- test CTRL+backspace -- delete previous word
        editor.setCaretPosition(9, 21);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_700", "testEditActions07", "testEditActions07");
    }

    public void testEditActionsTestCase_8() {
        openFileAndSetEditorState("testEditActions", "testEditActions07.pass", 9, 2);
        // 08 -- test CTRL+u -- delete the indentation level
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_800", "testEditActions08", "testEditActions08");
    }

    public void testEditActionsTestCase_9() {
        openFileAndSetEditorState("testEditActions", "testEditActions08.pass", 9, 2);
        // 09 -- test CTRL+u -- delete the line break
        editor.setCaretPosition(9, 2);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
//        txtOper.typeKey(' ');
        compareToGoldenFile(editor, "testEditActionsTestCase_900", "testEditActions09", "testEditActions09");
    }

    public void testEditActionsTestCase_10() {
        openFileAndSetEditorState("testEditActions", "testEditActions09.pass", 8, 6);
        // 10 -- test delete action
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        compareToGoldenFile(editor, "testEditActionsTestCase_1000", "testEditActions10", "testEditActions10");
    }

    public void testEditActionsTestCase_11() {
        openFileAndSetEditorState("testEditActions", "testEditActions10.pass", 8, 5);
        // 11 -- test delete selected block and selecting to end of the line
        txtOper.pushKey(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DELETE);        
        compareToGoldenFile(editor, "testEditActionsTestCase_1100", "testEditActions11", "testEditActions11");
    }

    public void testEditActionsTestCase_12() {
        openFileAndSetEditorState("testEditActions", "testEditActions11.pass", 8, 5);
        // 12 -- test COPY action ---
        editor.setCaretPosition(9, 15);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(10, 17);
        txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_1200", "testEditActions12", "testEditActions12");
    }

    public void testEditActionsTestCase_12a() {
        openFileAndSetEditorState("testEditActions", "testEditActions12.pass", 10, 23);
        // 12a -- test Select All ---
        txtOper.pushKey(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        if (txtOper.getSelectionStart() != 0 || txtOper.getSelectionEnd() != txtOper.getDocument().getLength()) {
            fail("Select all action fails. [start/end of selection] [docLength]: [" + txtOper.getSelectionStart() + "/" + txtOper.getSelectionEnd() + "] [" + txtOper.getDocument().getLength() + "]");
        }
    }

    public void testEditActionsTestCase_13() {
        openFileAndSetEditorState("testEditActions", "testEditActions12.pass", 10, 23);
        // 13 -- test Shift+delete (CUT) and shift+insert (PASTE)---
        editor.setCaretPosition(5, 17);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK);
        editor.setCaretPosition(13, 8);
        txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_1300", "testEditActions13", "testEditActions13");
    }

    public void testEditActionsTestCase_14() {
        openFileAndSetEditorState("testEditActions", "testEditActions13.pass", 13, 25);
        // 14 -- test ctrl+insert (COPY)---
        editor.setCaretPosition(10, 20);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(13, 15);
        txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_1400", "testEditActions14", "testEditActions14");
    }

    public void testEditActionsTestCase_15() {
        openFileAndSetEditorState("testEditActions", "testEditActions14.pass", 13, 31);
        // 15 -- test CTRL+K ----
        editor.setCaretPosition(6, 21);
        txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_1500", "testEditActions15", "testEditActions15");
    }

    public void testEditActionsTestCase_16() {
        openFileAndSetEditorState("testEditActions", "testEditActions15.pass", 6, 32);
        // 16 -- test CTRL+SHITF+K ----
        editor.setCaretPosition(10, 20);
        //type space to change String to Str ing
        txtOper.typeKey(' ');
        editor.setCaretPosition(10, 23);
        txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_1600", "testEditActions16", "testEditActions16");
    }

    public void testEditActionsTestCase_17() {
        openFileAndSetEditorState("testEditActions", "testEditActions16.pass", 10, 34);
        // 17 -- test expanding abbreviation
        editor.setCaretPosition(19, 12);
        txtOper.typeKey('s');
        txtOper.typeKey('t');
        txtOper.pressKey(KeyEvent.VK_TAB);
        compareToGoldenFile(editor, "testEditActionsTestCase_1700", "testEditActions17", "testEditActions17");
    }

    public void testEditActionsTestCase_18() {
        openFileAndSetEditorState("testEditActions", "testEditActions17.pass", 19, 19);
        // 18 -- test Insert space without expanding abbreviation (SPACE)
        editor.setCaretPosition(20, 9);
        txtOper.typeKey('s');
        txtOper.typeKey('t');
        txtOper.typeKey(' ');
        compareToGoldenFile(editor, "testEditActionsTestCase_1800", "testEditActions18", "testEditActions18");
    }

    public void testEditActionsTestCase_19() {
        openFileAndSetEditorState("testEditActions", "testEditActions18.pass", 20, 12);
        /* __________________ Capitlization ___________________ */
        // 19 -- w/o selection upper case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U);
        compareToGoldenFile(editor, "testEditActionsTestCase_1900", "testEditActions19", "testEditActions19");
    }

    public void testEditActionsTestCase_20() {
        openFileAndSetEditorState("testEditActions", "testEditActions19.pass", 13, 19);
        // 20 -- selection upper case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U);
        compareToGoldenFile(editor, "testEditActionsTestCase_2000", "testEditActions20", "testEditActions20");
    }

    public void testEditActionsTestCase_21() {
        openFileAndSetEditorState("testEditActions", "testEditActions20.pass", 13, 21);
        // 21 -- w/o selection lower case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_L);
        compareToGoldenFile(editor, "testEditActionsTestCase_2100", "testEditActions21", "testEditActions21");
    }

    public void testEditActionsTestCase_22() {
        openFileAndSetEditorState("testEditActions", "testEditActions21.pass", 13, 19);
        // 22 -- selection lower case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_L);
        compareToGoldenFile(editor, "testEditActionsTestCase_2200", "testEditActions22", "testEditActions22");
    }

    public void testEditActionsTestCase_23() {
        openFileAndSetEditorState("testEditActions", "testEditActions22.pass", 13, 21);
        // 23 -- w/o selection reverse case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_S);
        compareToGoldenFile(editor, "testEditActionsTestCase_2300", "testEditActions23", "testEditActions23");
    }

    public void testEditActionsTestCase_24() {
        openFileAndSetEditorState("testEditActions", "testEditActions23.pass", 13, 19);
        // 24 -- selection reverse case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_S);
        compareToGoldenFile(editor, "testEditActionsTestCase_2400", "testEditActions24", "testEditActions24");
    }

    public void testEditActionsTestCase_25() {
        openFileAndSetEditorState("testEditActions", "testEditActions24.pass", 13, 21);
        /* __________________ Several Indentation Actions ___________________ */
        // 25 -- Shift left  ------
        editor.setCaretPosition(10, 9);
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_2500", "testEditActions25", "testEditActions25");
    }

    public void testEditActionsTestCase_26() {
        openFileAndSetEditorState("testEditActions", "testEditActions25.pass", 10, 5);
        // 26 -- insert tab  ------
        txtOper.pushKey(KeyEvent.VK_TAB);
        compareToGoldenFile(editor, "testEditActionsTestCase_2600", "testEditActions26", "testEditActions26");
    }

    public void testEditActionsTestCase_27() {
        openFileAndSetEditorState("testEditActions", "testEditActions26.pass", 10, 9);
        // 27 -- Shift selection left  ------
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        // shift left
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_2700", "testEditActions27", "testEditActions27");
    }

    public void testEditActionsTestCase_28() {
        openFileAndSetEditorState("testEditActions", "testEditActions27.pass", 12, 1);        
        // 28 -- Shift  selection right  ------
        editor.setCaretPosition(9, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_TAB);
        compareToGoldenFile(editor, "testEditActionsTestCase_2800", "testEditActions28", "testEditActions28");
    }

    public void testEditActionsTestCase_29() {
        openFileAndSetEditorState("testEditActions", "testEditActions28.pass", 12, 1);
        // 29 -- Shift selection left (Alt+Shift+left) ------
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        // shift left
        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_2900", "testEditActions29", "testEditActions29");
    }

    public void testEditActionsTestCase_30() {
        openFileAndSetEditorState("testEditActions", "testEditActions29.pass", 12, 1);
        // 30 -- Shift  selection right (Alt+Shift+Right) ------
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_3000", "testEditActions30", "testEditActions30");
    }

    public void testEditActionsTestCase_31() {
        openFileAndSetEditorState("testEditActions", "testEditActions30.pass", 12, 1);
        // 31 -- reformat the selection + testing BACK_SPACE----
        //delete syntax error - otherwise reformat will not work
        editor.setCaretPosition(20, 1);
        txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        //make a mess
        editor.setCaretPosition(6, 5);
        txtOper.typeKey(' ');
        editor.setCaretPosition(9, 5);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_3100", "testEditActions31", "testEditActions31");
    }

    public void testEditActionsTestCase_32() {
        openFileAndSetEditorState("testEditActions", "testEditActions31.pass", 12, 1);
        //32 -- reformat the entire file ----
        // deselect
        txtOper.setSelectionStart(1);
        txtOper.setSelectionEnd(1);
        // invoke formatter
        txtOper.pushKey(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        compareToGoldenFile(editor, "testEditActionsTestCase_3200", "testEditActions32", "testEditActions32");
    }
//
//    public void testLineToolsTestCase_0() {
//        openFile("testLineTools");
//        editor.setCaretPosition(7, 25);
//        // 00
//        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_000", "testLineTools00", "testLineTools00");
//        compareToGoldenFile(editor, "testLineToolsTestCase_000", "testLineTools00", "testLineTools00");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools00.pass", 7, 21, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_1() {
//        //01
//        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_100", "testLineTools01", "testLineTools01");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools01.pass", 7, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_2() {
//        //02
//        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_200", "testLineTools02", "testLineTools02");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools02.pass", 6, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_3() {
//        //03
//        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_300", "testLineTools03", "testLineTools03");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools03.pass", 7, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_4() {
//        //04 - the same with block
//        editor.setCaretPosition(7, 25);
//        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_400", "testLineTools04", "testLineTools04");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools04.pass", 8, 21, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_5() {
//        //05
//        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_500", "testLineTools05", "testLineTools05");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools05.pass", 8, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_6() {
//        //06
//        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_600", "testLineTools06", "testLineTools06");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools06.pass", 7, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_7() {
//        //07
//        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_700", "testLineTools07", "testLineTools07");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools07.pass", 8, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_8() {
//        //08
//        editor.setCaretPosition(7, 25);
//        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_800", "testLineTools08", "testLineTools08");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools08.pass", 7, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_9() {
//        //09
//        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
//        editor.setCaretPosition(7, 25);
//        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_900", "testLineTools09", "testLineTools09");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools09.pass", 8, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_10() {
//        //10
//        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
//        editor.setCaretPosition(7, 25);
//        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//        compareToGoldenFile(editor, "testLineToolsTestCase_1000", "testLineTools10", "testLineTools10");
//        if (errMsg != null) {
//            setEditorStateWithGoldenFile(editor, "testLineTools10.pass", 8, 25, errMsg);
//        }
//    }
//
//    public void testLineToolsTestCase_11() {
//        try {
//            //11
//            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
//            editor.setCaretPosition(7, 25);
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(editor, "testLineToolsTestCase_1100", "testLineTools11", "testLineTools11");
//            if (errMsg != null) {
//                setEditorStateWithGoldenFile(editor, "testLineTools11.pass", 10, 25, errMsg);
//            }
//        } finally {
//            closeFileWithDiscard();
//        }
//    }

    public void testSyntaxSelection() {
        int[] begins = {602, 591, 587, 570, 550, 549, 548, 489, 473, 472, 471, 459, 447, 423, 422, 401, 393, 367, 363, 328};
        int[] ends =   {608, 609, 611, 612, 613, 629, 630, 630, 631, 643, 644, 644, 645, 654, 655, 655, 656, 661, 662, 663};
        String bs = "";
        String es = "";
        try {
            openFile("testSyntaxSelection");
            new EventTool().waitNoEvent(250);
            editor.setCaretPosition(27, 56);
            int x = 0;
            while (x < begins.length) {
                txtOper.pushKey(KeyEvent.VK_PERIOD, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();
                if (start != begins[x] || end != ends[x]) {
                    fail("Wrong selection expected <" + begins[x] + "," + ends[x] + "> but got <" + start + "," + end + ">");
                }
                x++;
            }
            x--;
            while (x > 0) {
                x--;
                txtOper.pushKey(KeyEvent.VK_COMMA, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();
                if (start != begins[x] || end != ends[x]) {
                    fail("Wrong selection expected <" + begins[x] + "," + ends[x] + "> but got <" + start + "," + end + ">");
                }
            }
        } finally {
            closeFileWithDiscard();
        }
    }

    public void testCommentUncommentTestCase_0() {
        openFile("testCommentUncomment");
        //00
        editor.setCaretPosition(6, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_000", "testCommentUncomment00", "testCommentUncomment00");
    }

    public void testCommentUncommentTestCase_1() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment00.pass", 6, 3);
        //01
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_100", "testCommentUncomment01", "testCommentUncomment01");
    }

    public void testCommentUncommentTestCase_2() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment01.pass", 6, 1);
        //02
        editor.setCaretPosition(10, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_200", "testCommentUncomment02", "testCommentUncomment02");
    }

    public void testCommentUncommentTestCase_3() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment02.pass", 12, 1);
        //03
        editor.setCaretPosition(10, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_300", "testCommentUncomment03", "testCommentUncomment03");
    }

    public void testCommentUncommentTestCase_4() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment03.pass", 12, 1);
        //04
        editor.setCaretPosition(15, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_400", "testCommentUncomment04", "testCommentUncomment04");
    }

    public void testCommentUncommentTestCase_5() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment04.pass", 17, 1);
        //05
        editor.setCaretPosition(15, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_500", "testCommentUncomment05", "testCommentUncomment05");
    }

    public void testCommentUncommentTestCase_6() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment05.pass", 17, 1);
        //06
        editor.setCaretPosition(20, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_600", "testCommentUncomment06", "testCommentUncomment06");
    }

    public void testCommentUncommentTestCase_7() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment06.pass", 20, 1);
        //07
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_700", "testCommentUncomment07", "testCommentUncomment07");
    }

    public void testCommentUncommentTestCase_8() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment07.pass", 20, 3);
        //08
        editor.setCaretPosition(21, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_800", "testCommentUncomment08", "testCommentUncomment08");
    }

    public void testCommentUncommentTestCase_9() {
        openFileAndSetEditorState("testCommentUncomment", "testCommentUncomment08.pass", 21, 1);
        //09
        editor.setCaretPosition(21, 1);        
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        compareToGoldenFile(editor, "testCommentUncommentTestCase_900", "testCommentUncomment09", "testCommentUncomment09");
    }

    public static void main(String[] args) {
        TestRunner.run(JavaEditActionsTest.class);
    }

    public static Test suite() {
        NbModuleSuite.Configuration config = NbModuleSuite.createConfiguration(JavaEditActionsTest.class);
        // Add testEditActions tests
        for (int i = 0; i < 33; i++) {
            config = config.addTest("testEditActionsTestCase_" + i);
            if (i == 12) {
                config = config.addTest("testEditActionsTestCase_12a");
            }
        }
        // Add testSyntaxSelection
        config = config.addTest("testSyntaxSelection");
        // Add testLineTools tests
        for (int i = 0; i < 12; i++) {
            config = config.addTest("testLineToolsTestCase_" + i);
        }
        // Add testCommentUncomment tests
        for (int i = 0; i < 10; i++) {
            config = config.addTest("testCommentUncommentTestCase_" + i);
        }
        config = config.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(config);
    }
}
