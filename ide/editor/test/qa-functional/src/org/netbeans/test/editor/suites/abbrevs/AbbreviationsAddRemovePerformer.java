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
package org.netbeans.test.editor.suites.abbrevs;

import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.ListModel;
import javax.swing.table.TableModel;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.Abbreviations;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.editor.lib.EditorTestCase;
//import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.test.editor.lib.LineDiff;

/**
 * Test adding/removing of abbreviation via advanced options panel
 * @author Jan Lahoda
 * @author Max Sauer
 */
public class AbbreviationsAddRemovePerformer extends EditorTestCase {

    /** 'Source Packages' string from j2se project bundle */
    public static final String SRC_PACKAGES_PATH = "Source Packages";
            
    private static String getText(Object elementAt)  {
        try {
            Method method = elementAt.getClass().getMethod("getText", null);
            method.setAccessible(true);
            String desc = (String) method.invoke(elementAt, null);
            return desc;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private boolean isInFramework;

    /** Creates a new instance of AbbreviationsAddRemove */
    public AbbreviationsAddRemovePerformer(String name) {
        super(name);
        isInFramework = false;
    }

    public EditorOperator openFile(String fileName) {        
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                "editor_test");
        pn.select();
        //Open Test.java from editor_test project
        Node n = new Node(pn, SRC_PACKAGES_PATH + "|" + "abbrev" + "|" + fileName);
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(500);

        return new EditorOperator(fileName);
    }

    private void checkAbbreviation(String abbreviation) throws Exception {
        //Open an editor:
        System.out.println("### Checking abbreviation \"" + abbreviation + "\"");
        openSourceFile("abbrev", "Test.java");        
        EditorOperator editor = new EditorOperator("Test.java");
        try {

            //This line is reserved for testing. All previous content is destroyed
            //(and fails test!).
            editor.setCaretPosition(20, 1);
            //Write abbreviation:
            editor.txtEditorPane().typeText(abbreviation);
            //Expand abbreviation:
            editor.pushTabKey();
            //Flush current file to output (ref output!)
            ref(editor.getText());
        } finally {
            editor.closeDiscard();
        }
    }

    public void ref(String ref) {        
            getRef().println(ref);        
    }

    public void log(String log) {
        if (isInFramework) {
            getLog().println(log);
            getLog().flush();
        } else {
            System.err.println(log);
        }
    }

    @Override
    public void setUp() {
        isInFramework = true;
        openDefaultProject();
        log("Starting abbreviations test.");
        log("Test name=" + getName());
        System.out.println("########");
        System.out.println("Staring "+getName());
        System.out.println("########");
        try {
            //EditorOperator.closeDiscardAll();
            log("Closed Welcome screen.");
        } catch (Exception ex) {
        }
    }

    @Override
    public void tearDown() throws Exception {
        getRef().flush();
        log("Finishing abbreviations test.");        
        System.out.println("########");
        System.out.println("Finished "+getName());
        System.out.println("########");
        assertFile("Output does not match golden file.", getGoldenFile(), new File(getWorkDir(), this.getName() + ".ref"),
                new File(getWorkDir(), this.getName() + ".diff"), new LineDiff(false));
        isInFramework = false;
        new EventTool().waitNoEvent(1000);
    }

    public void testAllAbbrev() throws Exception {
        Abbreviations abbrevs = Abbreviations.invoke("Java");
        JTableOperator templateTable = abbrevs.getTemplateTable();
        TableModel model = templateTable.getModel();        
        for (int i = 0; i < model.getRowCount(); i++) {
            String abb = model.getValueAt(i, 0).toString();
            String exp = model.getValueAt(i, 1).toString();
            getRef().println(abb);
            getRef().println(exp);
        }
        abbrevs.ok();
    }
    
    public void testAddRemove() throws Exception {
        Abbreviations.addOrEditAbbreviation("Java", "aaa", "aaa", "test", "test");
        checkAbbreviation("aaa");

    }

    public void testRemove() throws Exception {
        Abbreviations.removeAbbreviation("Java", "tds");
        checkAbbreviation("tds");
    }

    public void testEdit() throws Exception {
        boolean editAbbreviation = Abbreviations.editAbbreviation("Java", "forst", "//new expansion text", null);
        assertTrue(editAbbreviation);
        checkAbbreviation("forst");

    }

    public void testCodeTemplate() throws Exception {
        checkAbbreviation("fori");
    }

    public void testSelection() throws Exception {
        final EditorOperator editor = openFile("Test2");
        try {
            final int lineNumber = 15;
            editor.select(lineNumber, 9, 33);
            useHint(editor, lineNumber, "<html>Surround with /*");
            ref(editor.getText());
        } finally {
            editor.closeDiscard();
        }

    }

    public void testCursorPosition() throws Exception {
        final EditorOperator editor = openFile("Test2");
        try {
            editor.setCaretPosition(16, 9);
            editor.txtEditorPane().typeText("serr");
            editor.pushTabKey();
            int caretPosition = editor.txtEditorPane().getCaretPosition();           
            assertEquals("Wrong caret position", caretPosition, 281);
            ref(editor.getText());
        } finally {
            editor.closeDiscard();
        }
    }

    public void testInInvalidMime() throws Exception {
        final EditorOperator editor = openFile("Test2");
        try {
            editor.setCaretPosition(10, 8);
            editor.txtEditorPane().typeText("sout");
            editor.pushTabKey();
            ref(editor.getText());
        } finally {
            editor.closeDiscard();
        }
    }

    public void testJavadocAbbrev() throws Exception {
        Abbreviations.addOrEditAbbreviation("text/x-javadoc", "jd", "jd", "javadoc", null);
        final EditorOperator editor = openFile("Test2");
        try {
            editor.setCaretPosition(10, 8);
            editor.txtEditorPane().typeText("jd");
            editor.pushTabKey();
            ref(editor.getText());
        } finally {
            editor.closeDiscard();
        }
    }

    public void testChangeExpansionKey() {
        Abbreviations abbrevs = Abbreviations.invoke("Java");
        JComboBoxOperator expandOnCombo = abbrevs.getExpandOnCombo();
        expandOnCombo.selectItem("Enter");                       
        abbrevs.ok();                
        new EventTool().waitNoEvent(2000);
        final EditorOperator editor = openFile("Test2");
        try {            
            editor.setCaretPosition(16, 9);
            System.out.println("here");
            editor.txtEditorPane().typeText("sout");            
            System.out.println("Typed");        
            new EventTool().waitNoEvent(2000);
            editor.pushKey(KeyEvent.VK_ENTER);
            System.out.println("here");
            ref(editor.getText());
        } finally {            
            editor.closeDiscard();
            abbrevs = Abbreviations.invoke("Java");
            expandOnCombo = abbrevs.getExpandOnCombo();
            expandOnCombo.selectItem("Tab");
            abbrevs.ok();
        }
    }

    public static void useHint(final EditorOperator editor, final int lineNumber, String hintPrefix) throws InterruptedException {
        Object annots = new Waiter(new Waitable() {

            public Object actionProduced(Object arg0) {
                Object[] annotations = editor.getAnnotations(lineNumber);                
                if (annotations.length == 0) {
                    return null;
                } else {
                    return annotations;
                }
            }

            public String getDescription() {
                return "Waiting for annotations for current line";
            }
        }).waitAction(null);
        
        editor.pressKey(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK);
        JListOperator jlo = new JListOperator(MainWindowOperator.getDefault());
        int index = -1;
        ListModel model = jlo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object element = model.getElementAt(i);
            String desc = getText(element);
            if (desc.startsWith(hintPrefix)) {
                index = i;
            }
        }        
        assertTrue("Requested hint not found", index != -1);        
        jlo.selectItem(index);
        jlo.pushKey(KeyEvent.VK_ENTER);
    }
    
    public static void main(String[] args) throws Exception {
        TestRunner.run( new AbbreviationsAddRemovePerformer("testChangeExpansionKey"));
    }
    
    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(AbbreviationsAddRemovePerformer.class)
              .addTest("testAllAbbrev")  
              .addTest("testAddRemove")  
//              .addTest("testChangeExpansionKey")  
              .addTest("testCodeTemplate")  
              .addTest("testCursorPosition")  
//              .addTest("testEdit")  
              .addTest("testInInvalidMime")  
//              .addTest("testJavadocAbbrev")  
//              .addTest("testRemove")  
              .addTest("testSelection")                              
              .enableModules(".*")
              .clusters(".*"));
   }
}
