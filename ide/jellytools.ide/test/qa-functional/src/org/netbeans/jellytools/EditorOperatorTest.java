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
package org.netbeans.jellytools;

import java.io.IOException;
import java.lang.reflect.Method;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import org.netbeans.jellytools.actions.DockWindowAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.UndockWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.AbstractButtonOperator;

/**
 * Test of org.netbeans.jellytools.EditorOperator.
 * Order of tests is important.
 * @author Jiri Skrivanek
 */
public class EditorOperatorTest extends JellyTestCase {

    private static EditorOperator eo;
    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    public static final String[] tests = new String[]{
        "testTxtEditorPane",
        "testUndockWindow",
        "testLblRowColumn",
        "testLblStatusBar",
        "testLblInputMode",
        "testDockWindow",
        "testGetText",
        "testContains",
        "testSelect",
        "testGetLineNumber",
        "testPushHomeKey",
        "testPushEndKey",
        "testPushDownArrowKey",
        "testPushUpArrowKey",
        "testFolding",
        "testSetCaretPositionRelative",
        "testSetCaretPositionToLine",
        "testSetCaretPosition",
        "testGetToolbarButton",
        "testReplace",
        "testInsert",
        // annotations have to be tested after testInsert because of parser annotations
        "testGetAnnotations",
        "testGetAnnotationType",
        "testGetAnnotationShortDescription",
        "testDelete",
        "testPushTabKey",
        "testCloseDiscard",};

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public EditorOperatorTest(java.lang.String testName) {
        super(testName);
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(EditorOperatorTest.class, tests);
    }

    /** Opens sample class and finds EditorOperator instance */
    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
        System.out.println("### " + getName() + " ###");
        if (eo == null) {
            Node sourcePackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode("SampleProject"), "Source Packages");
            Node sample1 = new Node(sourcePackagesNode, "sample1");  // NOI18N
            Node sampleClass1 = new Node(sample1, SAMPLE_CLASS_1);
            new OpenAction().perform(sampleClass1);
            eo = new EditorOperator(SAMPLE_CLASS_1);
        }
    }

    /** Test of txtEditorPane method. */
    public void testTxtEditorPane() throws IOException {
        String text = eo.txtEditorPane().getText();
        assertTrue("Wrong editor pane found.", text.indexOf(SAMPLE_CLASS_1) != -1);
    }

    /**
     * More than a test this is here, because the following three tests (testLblRowColumn(),
     * testLblInputMode() and testLblStatusBar()) fail if the editor window is docked. When docked,
     * these three labels are a part of MainWindow.     *
     */
    public void testUndockWindow() {
        (new UndockWindowAction()).perform();
    }

    /** Test of lblRowColumn method. */
    public void testLblRowColumn() {
        assertEquals("1:1", eo.lblRowColumn().getText());
    }

    /** Test of lblInputMode method. */
    public void testLblInputMode() {
        String expected = Bundle.getString("org.netbeans.editor.Bundle", "status-bar-insert");
        assertEquals(expected, eo.lblInputMode().getText());
    }

    /** Test of lblStatusBar method. */
    public void testLblStatusBar() {
        String expected = "Status bar text";
        // set text to status bar
        try {
            String className = "org.netbeans.editor.Utilities";
            Class<?> clazz = Class.forName(className);
            Method setStatusTextMethod = clazz.getDeclaredMethod("setStatusText", new Class[]{
                        JTextComponent.class,
                        String.class
                    });
            setStatusTextMethod.invoke(null, new Object[]{
                        (JTextComponent) eo.txtEditorPane().getSource(),
                        expected
                    });
        } catch (Exception e) {
            e.printStackTrace(getLog());
            fail("Error in reflection operations: " + e.getMessage());
        }
        assertEquals("Wrong label found.", expected, eo.lblStatusBar().getText());
    }

    /**
     * Dock after testing the previous three labels.
     */
    public void testDockWindow() {
        new DockWindowAction().perform(eo);
    }

    /** Test of getText method. */
    public void testGetText() {
        String text = eo.getText();
        String expected = SAMPLE_CLASS_1;
        assertTrue("Found \"" + text + "\" but expected \"" + expected + "\".",
                text.indexOf(expected) != -1);
        expected = "public static void main";
        eo.setCaretPosition(expected, true);
        text = eo.getText(eo.getLineNumber());
        assertTrue("Found \"" + text + "\" but expected \"" + expected + "\".",
                text.indexOf(expected) != -1);
    }

    /** Test of contains method. */
    public void testContains() {
        assertTrue("Editor should contain \"" + SAMPLE_CLASS_1 + "\".",
                eo.contains(SAMPLE_CLASS_1));
        String dummy = "Dummy string @#$%^&";
        assertTrue("Editor should not contain \"" + dummy + "\".", !eo.contains(dummy));
    }

    /** Test of select method. */
    public void testSelect() {
        eo.setCaretPosition("public static void main", true);
        int line = eo.getLineNumber();
        eo.select(line);
        String expected = eo.getText(line);
        String selected = eo.txtEditorPane().getSelectedText() + "\n";
        assertEquals("Wrong selection.", expected.trim(), selected.trim());
        eo.select(line, line + 1);
        expected = eo.getText(line) + eo.getText(line + 1);
        selected = eo.txtEditorPane().getSelectedText() + "\n";
        assertEquals("Wrong selection.", expected.trim(), selected.trim());
        eo.select(line, 5, 10);
        expected = eo.getText(line).substring(4, 10);
        selected = eo.txtEditorPane().getSelectedText();
        assertEquals("Wrong selection.", expected, selected);
        expected = "public static void main";
        eo.select(expected);
        selected = eo.txtEditorPane().getSelectedText();
        assertEquals("Wrong selection.", expected, selected);
        eo.select("public", 2);
        assertEquals("Second occurence of word \"public\" on line " + line
                + " should be selected.", line, eo.getLineNumber());
    }

    /** Test of getLineNumber method. */
    public void testGetLineNumber() {
        eo.setCaretPosition(0);
        assertEquals("Wrong line number.", 1, eo.getLineNumber());
    }

    /** Test of pushHomeKey method. */
    public void testPushHomeKey() {
        eo.setCaretPosition(eo.getText(1).length() - 1);
        eo.pushHomeKey();
        assertEquals("Wrong position after key pushed.", 0,
                eo.txtEditorPane().getCaretPosition());
    }

    /** Test of pushEndKey method. */
    public void testPushEndKey() {
        eo.setCaretPosition(0);
        eo.pushEndKey();
        assertEquals("Wrong position after key pushed.", eo.getText(1).length() - 1,
                eo.txtEditorPane().getCaretPosition());
    }

    /** Test of pushDownArrowKey method. */
    public void testPushDownArrowKey() {
        eo.setCaretPosition(0);
        eo.pushDownArrowKey();
        assertEquals("Wrong line after key pushed.", 2, eo.getLineNumber());
    }

    /** Test of pushUpArrowKey method. */
    public void testPushUpArrowKey() {
        eo.setCaretPositionToLine(2);
        eo.pushUpArrowKey();
        assertEquals("Wrong line after key pushed.", 1, eo.getLineNumber());
    }

    /** Test of setCaretPositionRelative method. */
    public void testSetCaretPositionRelative() {
        eo.setCaretPosition(0);
        int expected = 20;
        eo.setCaretPositionRelative(expected);
        assertEquals("Wrong caret position.", expected, eo.txtEditorPane().getCaretPosition());
    }

    /** Test of setCaretPositionToLine method. */
    public void testSetCaretPositionToLine() {
        int expected = 10;
        eo.setCaretPositionToLine(10);
        assertEquals("Wrong line.", expected, eo.getLineNumber());
    }

    /** Test of setCaretPosition method. */
    public void testSetCaretPosition() {
        eo.setCaretPosition(0);
        assertEquals("Wrong caret position.", 0, eo.txtEditorPane().getCaretPosition());
        String expected = "public static void main";
        eo.setCaretPosition(expected, true);
        int position = eo.txtEditorPane().getCaretPosition();
        String text = eo.txtEditorPane().getText(position, expected.length());
        assertEquals("Wrong caret position before text.", expected, text);
        eo.setCaretPosition(position);
        int newPosition = eo.txtEditorPane().getCaretPosition();
        assertEquals("Wrong caret position.", position, newPosition);
        eo.setCaretPosition(expected, false);
        position = eo.txtEditorPane().getCaretPosition();
        text = eo.txtEditorPane().getText(position - expected.length(), expected.length());
        assertEquals("Wrong caret position after text.", expected, text);
        eo.setCaretPosition("public", 2, true);
        position = eo.txtEditorPane().getCaretPosition();
        text = eo.txtEditorPane().getText(position, expected.length());
        assertEquals("Wrong caret position before text.", expected, text);
        eo.setCaretPosition("public", 2, false);
        position = eo.txtEditorPane().getCaretPosition();
        text = eo.txtEditorPane().getText(position - "public".length(), expected.length());
        assertEquals("Wrong caret position after text.", expected, text);
        eo.setCaretPositionToEndOfLine(eo.getLineNumber());
        position = eo.txtEditorPane().getCaretPosition();
        text = eo.txtEditorPane().getText(position - 1, 1);
        assertEquals("Caret not at the end of line.", "{", text);
    }

    /** Test of getToolbarButton method. Uses "Toggle bookmark button". */
    public void testGetToolbarButton() {
        String tooltip = Bundle.getStringTrimmed("org.netbeans.lib.editor.bookmarks.actions.Bundle", "bookmark-toggle");
        AbstractButtonOperator button1 = eo.getToolbarButton(tooltip);
        button1.push();
        AbstractButtonOperator button2 = eo.getToolbarButton(12);
        assertEquals("Toggle Bookmark button should have index 12",
                button1.getToolTipText(), button2.getToolTipText());
    }

    /** Test of replace method. */
    public void testReplace() {
        String oldText = "public static void main";
        String newText = "XXXXXXXXXXXXXXXXXX";
        eo.replace(oldText, newText);
        assertTrue("Replace of \"" + oldText + "\" by \"" + newText + "\" failed.",
                eo.contains(newText));
        // replace back
        eo.replace(newText, oldText);
    }

    /** Test of insert method. */
    public void testInsert() {
        eo.setCaretPosition(0);
        eo.insert("111 First line\n");
        eo.insert("222 Second line\n");
        eo.insert("333 Third line\n");
        assertEquals("Insertion failed on first line.", "111 First line\n", eo.getText(1));
        eo.insert(" addendum", 3, 15);
        assertEquals("Insertion failed on third line.", "333 Third line addendum\n",
                eo.getText(3));
    }

    /** Test of getAnnotations method. Expects bookmark 
     * created in test testGetToolbarButton() and parser annotations on 
     * inserted lines (testInsert).
     * @throws java.lang.InterruptedException
     */
    public void testGetAnnotations() throws InterruptedException {
        // wait parser annotations
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object oper) {
                return eo.getAnnotations().length > 1 ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait parser annotations."); // NOI18N
            }
        }).waitAction(null);
    }

    /** Test of getAnnotationType method. */
    public void testGetAnnotationType() {
        Object[] an = eo.getAnnotations();
        String type = EditorOperator.getAnnotationType(an[0]);
        assertNotNull("getAnnotationType return null.", type);
        assertTrue("getAnnotationType return empty string.", type.length() > 0);
    }

    /** Test of getAnnotationShortDescription method. */
    public void testGetAnnotationShortDescription() {
        Object[] an = eo.getAnnotations();
        String desc = EditorOperator.getAnnotationShortDescription(an[0]);
        assertNotNull("getAnnotationShortDescription return null.", desc);
        assertTrue("getAnnotationShortDescription return empty string.", desc.length() > 0);
    }

    /** Test of folding methods. */
    public static void testFolding() {
        eo.waitFolding();
        assertFalse("Initial comment at line 2 should be expanded.", eo.isCollapsed(2));
        eo.setCaretPositionToLine(3);
        assertFalse("Initial comment at line 3 should be expanded.", eo.isCollapsed(3));
        try {
            eo.setCaretPosition("package sample1;", true); // NOI18N
            int line = eo.getLineNumber();
            eo.isCollapsed(line);
            fail("JemmyException should be thrown because no fold is at line " + line);
        } catch (JemmyException e) {
            // OK.
        }
        eo.setCaretPositionToLine(2);
        eo.collapseFold();
        assertTrue("Initial comment should be collapsed now.", eo.isCollapsed(2));
        eo.expandFold();
        eo.collapseFold(5);
        eo.expandFold(5);
    }

    /** Test of delete method. */
    public void testDelete() {
        eo.delete(3, 15, 23);
        assertEquals("Delete on third line failed.", "333 Third line\n", eo.getText(3));
        // delete third line
        eo.deleteLine(3);
        assertTrue("Delete of third line failed.", !eo.contains("333 Third line"));
        // delete second line
        eo.delete("111 First line\n".length(), "222 Second line\n".length());
        assertTrue("Delete of second line failed.", !eo.contains("222 Second line"));
        // delete first line
        eo.setCaretPosition(0);
        eo.delete("111 First line\n".length());
        assertTrue("Delete of first line failed.", !eo.contains("111 First line"));
    }

    /** Test of pushTabKey method. */
    public void testPushTabKey() {
        int length1 = eo.getText(1).length();
        eo.setCaretPosition(0);
        eo.pushTabKey();
        int length2 = eo.getText(1).length();
        assertTrue("Tab key not pushed.", length2 > length1);
    }

    /** Test of closeDiscard method. */
    public void testCloseDiscard() {
        eo.closeDiscard();
        eo = null;
    }
}
