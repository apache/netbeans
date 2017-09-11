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
package org.netbeans.qa.form.undoredo;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.modules.form.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jellytools.actions.*;

import org.netbeans.jemmy.operators.*;
import java.awt.Color;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Testing if Undo/Redo
 *
 * @author Unknown
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class BaseTest extends ExtJellyTestCase {

    public String FILE_NAME = "clear_JFrame";
    public String PACKAGE_NAME = "data";
    public String DATA_PROJECT_NAME = "SampleProject";
    public String FRAME_ROOT = "[JFrame]";
    public MainWindowOperator mainWindow;
    public ProjectsTabOperator pto;
    public Node formnode;
    ComponentInspectorOperator inspector;
    FormDesignerOperator formDesigner;
    ComponentPaletteOperator palette;
    EditorOperator editor;
    EditorWindowOperator ewo;
    String fileName;

    /** Test suite
     * @param args arguments from command line
     */
    public static Test suite() {

        if (OS.equals("sunos")) {
            System.out.println("Solaris is not supported");
        } else {

            return NbModuleSuite.create(NbModuleSuite.createConfiguration(BaseTest.class).addTest("testScenario").enableModules(".*").clusters(".*").gui(true));
        }
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(BaseTest.class).addTest("testNoScenario"));

    }

    /** */
    public BaseTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
    }

    /*
     * select tab in PropertySheet
     */
    public void selectPropertiesTab(PropertySheetOperator pso) {
        selectTab(pso, 0);
    }

    public void selectBindTab(PropertySheetOperator pso) {
        selectTab(pso, 1);
    }

    public void selectEventsTab(PropertySheetOperator pso) {
        selectTab(pso, 2);
    }

    public void selectCodeTab(PropertySheetOperator pso) {
        selectTab(pso, 3);
    }

    //select tab in PropertySheet
    public void selectTab(PropertySheetOperator pso, int index) {
        sleep(1000);
        JToggleButtonOperator tbo = null;
        if (tbo == null) {
            tbo = new JToggleButtonOperator(pso, " ", index);
        }
        tbo.push();
    }

    //method which avoid testing on solaris platform
    public void testNoScenario() {
        System.out.println("Solaris platform was detected");
    }

    public void testScenario() {
        mainWindow = MainWindowOperator.getDefault();
        pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();
        log("Form node selected.");

        //formDesigner = new FormDesignerOperator(FILE_NAME);

        EditAction editAction = new EditAction();
        editAction.perform(formnode);
        log("Source Editor window opened.");

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        log("Form Editor window opened.");

        ComponentInspectorOperator cio = new ComponentInspectorOperator();

        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();

        palette = new ComponentPaletteOperator();
        inspector = new ComponentInspectorOperator();
        formnode.select();
        formnode.performPopupAction("Open");
        FormDesignerOperator designer = new FormDesignerOperator(FILE_NAME);
        designer.source();
        designer.design();
        //init property sheet and select the proper "tab"
        PropertySheetOperator pso = cio.properties();

        cio.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {

                inspector.performAction(new Action(null, "Add From Palette|Swing Containers|Panel"), "[JFrame]");
                // selectPropertiesTab(pso);
                //new Action(null, "Add From Palette|Swing Containers|Panel").performPopup(new Node(inspector.treeComponents(), "[JFrame]"));

                // selectPropertiesTab(pso);
                inspector.performAction(new Action(null, "Add From Palette|Swing Containers|Panel"), "[JFrame]");
                //new Action(null, "Add From Palette|Swing Containers|Panel").performPopup(new Node(inspector.treeComponents(), "[JFrame]"));

                //change properties (color)
                inspector.selectComponent("[JFrame]|JPanel1 [JPanel]");
            }
        });

        selectPropertiesTab(pso);

        // new ColorProperty(pso, "background").setRGBValue(202,234,223);

        new ColorProperty(new PropertySheetOperator("jPanel1 [JPanel] - Properties"), "background").setColorValue(new Color(202, 234, 223));

        cio.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                inspector.selectComponent("[JFrame]|JPanel2 [JPanel]");
            }
        });
        selectPropertiesTab(pso);
        new ColorProperty(new PropertySheetOperator("jPanel2 [JPanel] - Properties"), "background").setRGBValue(252, 34, 3);

        cio.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                // add JButton1 to JPanel1
                inspector.performAction(new Action(null, "Add From Palette|Swing Controls|Button"), "[JFrame]|JPanel1 [JPanel]");
                // new Action(null, "Add From Palette|Swing Controls|Button").performPopup(new Node(inspector.treeComponents(), "[JFrame]|JPanel1 [JPanel]"));

                // add JButton2 to JPanel2
                inspector.performAction(new Action(null, "Add From Palette|Swing Controls|Button"), "[JFrame]|JPanel2 [JPanel]");
                //new Action(null, "Add From Palette|Swing Controls|Button").performPopup(new Node(inspector.treeComponents(), "[JFrame]|JPanel2 [JPanel]"));

                // cut-paste JButton1 from JPanel1 to JPanel2
                inspector.performAction(new Action(null, "Cut"), "[JFrame]|JPanel1 [JPanel]|jButton1 [JButton]");
                //new Action(null, "Cut").performPopup(new Node(inspector.treeComponents(), "[JFrame]|JPanel1 [JPanel]|jButton1 [JButton]"));
                inspector.performAction(new Action(null, "Paste"), "[JFrame]|JPanel2 [JPanel]");
                // new Action(null, "Paste").performPopup(new Node(inspector.treeComponents(), "[JFrame]|JPanel2 [JPanel]"));

                // change properties
                inspector.selectComponent("[JFrame]|JPanel2 [JPanel]|jButton1 [JButton]");
            }
        });

        new Property(pso, "text").setValue("<html><font color='red' size='+3'>QA</font> test");

        // change order
        //formnode.select();
        //formnode.performPopupAction("Open");
        //designer=new FormDesignerOperator(FILE_NAME);
        // designer.source();
        //designer.design();
        //inspector=new ComponentInspectorOperator();
        cio.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                inspector.performAction(new ActionNoBlock(null, "Change Order..."), "[JFrame]|JPanel2 [JPanel]");
            }
        });
        //new ActionNoBlock(null, "Change Order...").performPopup(new Node(inspector.treeComponents(), "[JFrame]|JPanel2 [JPanel]"));
        NbDialogOperator changeOrder = new NbDialogOperator("Change Order");
        new JListOperator(changeOrder).selectItem(1);
        new JButtonOperator(changeOrder, "Move up").doClick();
        changeOrder.btOK().doClick();

        cio.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                // change generated code
                inspector.selectComponent("[JFrame]|JPanel2 [JPanel]|jButton1 [JButton]");
            }
        });

        selectCodeTab(pso);
        sleep(1000);
        new Property(pso, "Pre-Creation Code").setValue("aaa");
        new Property(pso, "Post-Init Code").setValue("bbb");

        sleep(2000);


//        new Property(pso, "text").openEditor();
//        fceo.advanced();
//        FormCustomEditorAdvancedOperator fceao = new FormCustomEditorAdvancedOperator();
//        fceao.setGeneratePreInitializationCode(true);
//        fceao.setPreInitializationCode("aaa");
//        fceao.setGeneratePostInitializationCode(true);
//        fceao.setPostInitializationCode("bbb");
//        fceao.ok();
//        fceo.ok();

        // event
        selectEventsTab(pso);

        Property prop = new Property(pso, "actionPerformed");
        prop.setValue("myAction");

        //selectPropertiesTab(pso);

        // section undo testing
        openAction.perform(formnode);
        sleep(2000);


        assertTrue("check in Editor 11b", checkEditor("private void myAction"));

        log("undo 1");
        undo(1);

        // check if aaa, bbb are generated
        assertTrue("check in Editor 10b", checkEditor("aaa,bbb"));
        assertTrue("check in Editor 10c", !checkEditor("private void myAction"));
        log("undo 2");
        undo(1);

        // check if aaa, bbb are not in editor
        assertTrue("check ii Editor 10a", !checkEditor("aaa,bbb"));

        //now it's not possible to check, in editore, there is different code (no on the same row)
        assertTrue("check in Editor 9a", checkEditor("jPanel2.add(jButton1),jPanel2.add(jButton2)"));

        log("undo 3");
        undo(1);
        //check if panel order was changed
        assertTrue("check in Editor 9b", !checkEditor("jPanel2.add(jButton2),jPanel2.add(jButton1)"));
        assertTrue("check in Editor 8b", checkEditor("<html>"));

        log("undo 4");
        undo(2);

        //check if both buttons are in panel2
        assertTrue("check in Editor 7a", checkEditor("jPanel2.add(jButton1"));
        assertTrue("check in Editor 7b", checkEditor("jPanel2.add(jButton2"));

        log("undo 5");
        undo(1);
        //check if panel2 has only button2 and panel1 has only button1 as well
        assertTrue("check in Editor 8a", !checkEditor("<html>"));
        assertTrue("check in Editor 7c", !checkEditor("jPanel2.add(jButton1"));
        assertTrue("check in Editor 7d", checkEditor("jPanel1.add(jButton1"));
        assertTrue("check in Editor 7e", checkEditor("jPanel2.add(jButton2"));

        log("undo 6");
        undo(1);
        //check if button2 is not in jframe and background of panel2 is still set
        assertTrue("check in Editor 4a", !checkEditor("jButton2"));
        assertTrue("check in Editor 4b", checkEditor("jPanel2.setBackground"));

        log("undo 7");
        undo(1);
        //check if button1 is not in jframe and background of panel1 is still set
        assertTrue("check in Editor 3a", !checkEditor("jButton1"));
        assertTrue("check in Editor 3b", checkEditor("jPanel1.setBackground"));

        log("undo 8");
        undo(1);
        //check background if is not set on panel2
        assertTrue("check in Editor 2a", !checkEditor("jPanel2.setBackground"));

        log("undo 9");
        undo(1);
        //check background if is not set on panel1
        assertTrue("check in Editor 1a", !checkEditor("jPanel1.setBackground"));

        log("undo 10");
        undo(1);

        //check if panel2 disappeared from jframe
        assertTrue("check in Editor 0a", !checkEditor("jPanel2"));

        log("undo 10");
        undo(1);
        //check if panel1 disappeared from jframe
        assertTrue("check in Editor 0b", !checkEditor("jPanel1"));

        //redo
        log("redo 1");
        redo(1);
        //check if panel1 is in jframe
        assertTrue("check in Editor R01a", checkEditor("jPanel1"));

        log("redo 2");
        redo(1);
        //check if panel1 is in jframe
        assertTrue("check in Editor R02a", checkEditor("jPanel2"));

        log("redo 3");
        redo(1);
        //check if background was set for panel1
        assertTrue("check in Editor R03a", checkEditor("jPanel1.setBackground"));

        log("redo 4");
        redo(1);
        //check if background was set for panel2
        assertTrue("check in Editor R04a", checkEditor("jPanel2.setBackground"));

        log("redo 5");
        redo(1);
        //check if button1 was added in panel1
        assertTrue("check in Editor R05a", checkEditor("jPanel1.add(jButton1"));

        log("redo 6");
        redo(1);
        //check if button2 was added in panel2
        assertTrue("check in Editor R06a", checkEditor("jPanel2.add(jButton2"));

        log("redo 7");
        redo(1);
        //check if panel2 contains both buttons and panel1 is empty
        assertTrue("check in Editor R07a", checkEditor("jPanel2.add(jButton1"));
        assertTrue("check in Editor R07b", checkEditor("jPanel2.add(jButton2"));
        assertTrue("check in Editor R07c", !checkEditor("jPanel1.add(jButton2"));

        log("redo 8");
        redo(1);
        //check if text in html was added
        assertTrue("check in Editor R08a", checkEditor("<html>"));
        assertTrue("check in Editor R08b", checkEditor("jPanel2.add(jButton2),jPanel2.add(jButton1)"));

        log("redo 9");
        redo(1);
        //check if buttons order was changed
        assertTrue("check in Editor R09b", checkEditor("jPanel2.add(jButton1),jPanel2.add(jButton2)"));

        log("redo 10");
        redo(2);
        //check if string aaa, bbb was added in editor
        assertTrue("check in Editor R010b", checkEditor("aaa,bbb"));

        log("redo 11");
        redo(1);
        //check if action "myAction" was added in editor
        assertTrue("check in Editor R011b", checkEditor("private void myAction"));


        log("undo 12");
        undo(12);

//        Action saveAction;
//        saveAction = new Action("File|Save", null);
//        saveAction.perform();

        editor = new EditorOperator(FILE_NAME);
        editor.close(false);
//        ewo = new EditorWindowOperator();
//        ewo.closeDiscard();

    }

    /** Run test.
     */
    void undo(int n) {
        //first switch to FormEditor tab

        for (int i = 0; i < n; i++) {
            sleep(500);
            new ActionNoBlock("Edit|Undo", null).perform();
            //mainWindow.getToolbarButton(mainWindow.getToolbar("Edit"), "Undo").push();
            sleep(500);
        }
    }

    void redo(int n) {
        //first switch to FormEditor tab
        //OpenAction openAction = new OpenAction();
        //openAction.perform(formnode);

        mainWindow = MainWindowOperator.getDefault();
        //inspector.selectComponent("[JFrame]");
        for (int i = 0; i < n; i++) {
            sleep(1000);
            new ActionNoBlock("Edit|Redo", null).perform();
            //mainWindow.getToolbarButton(mainWindow.getToolbar("Edit"), "Redo").push();
            sleep(1000);
        }
    }

    void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    boolean checkEditor(String regexp) {
        /*editor = ewo.getEditor("clear_JFrame");
        editor = new EditorOperator("clear_JFrame");
         */
        //EditAction editAction = new EditAction();
        //editAction.perform(formnode);
        log("Source Editor window opened.");

//        editor = ewo.getEditor();
        sleep(300);
//        String editortext = editor.getText();
        formDesigner = new FormDesignerOperator(FILE_NAME);
        String editortext = formDesigner.editor().getText();
        formDesigner.design();
        // text without escape characters
        /*
        StringBuffer newtext = new StringBuffer();
        for (int i=0;i<editortext.length();i++) {
        char ch = editortext.charAt(i);
        if (ch >= 32)
        newtext.append(ch);
        }
         */
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(regexp, ",");
        int pos = -1;
        boolean result = true;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            pos = editortext.indexOf(token, pos);
            if (pos == -1) {
                result = false;
                break;
            }
            pos += token.length();
        }
        System.out.println("Result: " + result);
        return result;
    }
}
