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
package org.netbeans.modules.xml.text.completion;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import junit.textui.TestRunner;
import org.netbeans.editor.ext.ListCompletionView;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.modules.xml.text.syntax.XMLOptions;
import org.netbeans.tests.xml.JXTest;
import org.openide.loaders.DataObject;
import org.openide.options.SystemOption;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: CompletionJTest
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * - basic functionality of XML code completion<br>
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * Creates simple XML document by code completion.
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * XML documents<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on April 03, 2003, 12:33 PM
 * <P>
 */

public class CompletionJTest extends JXTest {
    // constants for showCompl() method
    private static int NO_WAIT = 0;
    private static int EMPTY = 1;
    private static int NO_EMPTY = 2;
    
    /** Caret Column Position */
    int col;
    /** Editor Operator */
    EditorOperator editor;
    
    /**
     * Creates new CoreTemplatesTest
     * @param testName
     */
    public CompletionJTest(String testName) {
        super(testName);
    }
    
    /** Main test method. */
    public void test() {
        String folder;
        String name = "Document";
        String ext = "xml";
        XMLOptions options;
        DataObject dao;
        
        try {
            folder = getFilesystemName() + DELIM + getDataPackageName(DELIM);
            
            options = (XMLOptions) SystemOption.findObject(XMLOptions.class, true);
            options.setCompletionAutoPopup(false);
            
            dao = TestUtil.THIS.findData(name + "." + ext);
            if (dao != null) dao.delete();
            // catalog is only real XML template in the module :-(
            NewWizardOperator.create("XML" + DELIM + "OASIS XML Catalog", folder, name);
            editor = new EditorOperator(name);
        } catch (Exception ex) {
            log("Cannot setup test.", ex);
        }
        
        clearText();
        editor.txtEditorPane().setText(""
        + "<?xml version='1.0' encoding='UTF-8'?>\n"
        + "<!DOCTYPE html PUBLIC '-//Test//DTD XHTML 1.0 Subset//EN' 'xhtml.dtd'>\n");

        insert("<h");
        save();
        //tml>
        showCompl(NO_EMPTY);
        enter();
        insert(">\n");
        //<head>
        insertTag("<", ">\n", 1);
        //<title>Test page</title>
        
        /*!!! #36306 hack
        insertTag("<t", "Test page", -1);
         */
        insert("<t");
        showCompl(NO_EMPTY);
        esc();
        insert("Test page");
        //!!! end hack
        
        end();
        insert("\n");
        //</head>
        insertTag("</", "\n", -1);
        //<body>
        insertTag("<", ">\n", 0);
        //<h1 title="test">Test</h1>
        insertTag("<h", " ", 0);
        insertTag("t", "test\">Test", -1);
        insertTag("</", "\n", -1);
        //<table border="1">
        insertTag("<t", " ", 0);
        insertTag("b", "1\">\n", 1);
        //<tr align="center">
        insertTag("<t", " ", 4);
        insertTag("a", "center\">\n", -1);
        //<td>1</td><td>2</td>
        
        /*!!! #36306 hack
        insertTag("<td", "1", -1);
        end();
        insertTag("<td", "2", -1);
         */
        
        insert("<td");
        showCompl(NO_EMPTY);
        esc();
        insert("1");
        end();
        
        insert("<td");
        showCompl(NO_EMPTY);
        esc();
        insert("2");
        //!!! end hack
        
        end();
        insert("\n");
        //</tr>
        insertTag("</", "\n", -1);
        //</table>
        insertTag("</", "\n", -1);
        //</body>
        insertTag("</", "\n", -1);
        //</html>
        insertTag("</", "\n", -1);
        save();
        ref(editor.getText());
        compareReferenceFiles();
    }
    
    /** Inserts a tag using completion, i.e.:
     * <ul>
     * <li>types <i>pref</i> at caret position
     * <li>triggers code completion
     * <li>enters index-th element from completion list
     * <li>types <i>suf</i> at caret position
     * </ul>
     * @param pref prefix
     * @param suf sufix
     * @param index index of inserting item, -1 => 
     */
    protected final void insertTag(String pref, String suf, int index) {
        insert(pref);
        if (index < 0) {
            showCompl(NO_WAIT);
        } else {
            showCompl(NO_EMPTY);
        }
        for (int i = 0; i < index; i++) {
            down();
        }
        if (index > -1) {
            enter();
        }
        //!!! sometime completion doesn't finish on time
        sleepTest(500);
        insert(suf);
    }
    
    /** Types a text at caret position
     *
     * @param txt String
     */
    protected final void insert(String txt) {
        editor.txtEditorPane().typeText(txt);
    }
    
    /** Moves caret about <i>x</i> lines and <i>y</i> rows
     *
     * @param x delta X
     * @param y delta Y
     */
    protected final void move(int x, int y) {
        col += y;
        editor.setCaretPosition(editor.getLineNumber() + x, col);
    }
    
    /** Saves the document */
    protected final void save() {
        editor.save();
    }
    
    /** Clears the document */
    protected final void clearText() {
        col = 0;
        JTextComponentOperator text = new JTextComponentOperator(editor);
        
        text.setText("X"); //!!! because clearText is to slow.
        text.clearText();
    }
    
    /** Triggers code completion in given mode.
     * <ul>
     * <li>NO_WAIT - only trigger completion and continue
     * <li>EMPTY - trigger completion and wait for completion list
     * <li>NO_EMPTY - trigger completion and wait for non-empty completion list
     * </ul>
     */
    protected final void showCompl(int mode) {
        editor.pressKey(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
        if (mode == NO_WAIT) {
            return;
        } else if (mode == NO_EMPTY) {
            waitCompl(1);
        } else if (mode == EMPTY) {
            waitCompl(0);
        }
    }
    
    /** Triggers code completion and checks if completion list contains at least
     * <i>minSize</i> items.
     * @param minSize
     */
    protected final void checkCompletion(int minSize) {
        showCompl(NO_WAIT);
        waitCompl(minSize);
        esc();
    }
    
    /** Waits completion list with at least <i>minSize</i> items.
     * @param minSize - nuber of items
     */
    private void waitCompl(int minSize) {
        CompletionChooser completionChoser = new CompletionChooser(minSize);
        ListCompletionView completionView = (ListCompletionView) ComponentOperator
        .waitComponent((Container) editor.getWindowContainerOperator().getSource()
        , completionChoser);
        int size = completionView.getModel().getSize();
    }
    
    /** Searches for a completion listh with at least <i>minSize</i> items */
    private class CompletionChooser implements ComponentChooser {
        int minSize;
        
        public CompletionChooser() {
            this(0);
        }
        
        public CompletionChooser(int minSize) {
            this.minSize = minSize;
        }
        
        public boolean checkComponent(Component component) {
            //System.out.println("> " + component);
            
            if (component instanceof ListCompletionView) {
                ListCompletionView cmpl = (ListCompletionView) component;
                if (cmpl.getModel().getSize() >= minSize) return true;
            }
            return false;
        }
        
        public String getDescription() {
            return("Instace of ScrollCompletionPane");
        }
    }
    
    // KEYS
    
    /** Deletes given number of characters from current caret possition.
     * Position of caret will not change.
     * @param length number of characters to be deleted
     */
    protected final void delete(int len) {
        editor.delete(len);
    }
    
    /** Deletes given number of characters before current caret possition.
     * @param length number of characters to be deleted
     */
    protected final void backSp(int len) {
        for (int i = 0; i < len; i++) {
            editor.pushKey(KeyEvent.VK_BACK_SPACE);
        }
    }
    
    /** Presses key [ESC] */
    protected final void esc() {
        editor.pressKey(KeyEvent.VK_ESCAPE);
    }
    
    /** Presses key [ENTER] */
    protected final void enter() {
        editor.pressKey(KeyEvent.VK_ENTER);
    }
    
    /** Presses key [DOWN] */
    protected final void down() {
        editor.pressKey(KeyEvent.VK_DOWN);
    }
    
    /** Presses key [UP] */
    protected final void up() {
        editor.pressKey(KeyEvent.VK_UP);
    }
    
    /** Presses key [LEFT] */
    protected final void left() {
        editor.pressKey(KeyEvent.VK_LEFT);
    }
    
    /** Presses key [RIGHT] */
    protected final void right() {
        editor.pressKey(KeyEvent.VK_RIGHT);
    }
    
    /** Presses key [END] */
    protected final void end() {
        editor.pressKey(KeyEvent.VK_END);
    }
    
    /** Main method for debuging purpose
     *
     * @param args
     */
    public static void main(String[] args) {
        //JamController.setFast(false);
        DEBUG = true;
        TestRunner.run(CompletionJTest.class);
    }
}
