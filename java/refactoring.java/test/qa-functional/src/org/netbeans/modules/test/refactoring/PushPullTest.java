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


package org.netbeans.modules.test.refactoring;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.*;
import org.netbeans.modules.test.refactoring.operators.*;
import org.netbeans.modules.test.refactoring.operators.ErrorOperator;

/**
 * @author (stanislav.sazonov@oracle.com)
 */
public class PushPullTest extends ModifyingRefactoring {

    private enum currentTest {
        testSimple_A_A,
        testSimple_A_B,
        testSimple_A_C,
        testSimple_A_D,
        testSimple_A_E,
        testSimple_A_F,
        testSimple_A_G,
        testSimple_A_H,
        testSimple_A_I,
        nothing
    };

    public PushPullTest(String name) {
        super(name);
    }
    
    public static Test suite() {

        return JellyTestCase.emptyConfiguration().
                addTest(RenameTest.class, "testSimple_A_A").
                addTest(RenameTest.class, "testSimple_A_B").
                addTest(RenameTest.class, "testSimple_A_C").
                addTest(RenameTest.class, "testSimple_A_D").
                addTest(RenameTest.class, "testSimple_A_E").
                addTest(RenameTest.class, "testSimple_A_F").
                addTest(RenameTest.class, "testSimple_A_G").
                addTest(RenameTest.class, "testSimple_A_H").
                addTest(RenameTest.class, "testSimple_A_I").
                suite();
    }

    public void testSimple_A_A() {
        performTest(currentTest.testSimple_A_A);
    }

    public void testSimple_A_B() {
        performTest(currentTest.testSimple_A_B);
    }

    public void testSimple_A_C() {
        performTest(currentTest.testSimple_A_C);
    }
    
    public void testSimple_A_D() {
        performTest(currentTest.testSimple_A_D);
    }

    public void testSimple_A_E() {
        performTest(currentTest.testSimple_A_E);
    }
    
    public void testSimple_A_F() {
        performTest(currentTest.testSimple_A_F);
    }
    
    public void testSimple_A_G() {
        performTest(currentTest.testSimple_A_G);
    }
    
    public void testSimple_A_H() {
        performTest(currentTest.testSimple_A_H);
    }
    
    public void testSimple_A_I() {
        performTest(currentTest.testSimple_A_I);
    }

/*  Pull Up dialog:
----------------------------------------------------    
    javax.swing.JPanel
    __org.netbeans.modules.refactoring.spi.impl.ParametersPanel
    ____javax.swing.JPanel
    ______javax.swing.JPanel
    ________javax.swing.JPanel
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    __________javax.swing.JButton
    ________javax.swing.JCheckBox
    ______javax.swing.JPanel
    ________org.netbeans.modules.refactoring.spi.impl.TooltipLabel
    ____javax.swing.JPanel
    ______org.netbeans.modules.refactoring.java.ui.PullUpPanel
    ________javax.swing.JPanel
    __________javax.swing.JComboBox
    ____________com.sun.java.swing.plaf.windows.WindowsComboBoxUI$XPComboBoxButton
    ____________javax.swing.CellRendererPane
    ______________org.netbeans.modules.refactoring.java.ui.UIUtilities$JavaElementListCellRenderer
    __________javax.swing.JLabel
    __________javax.swing.JLabel
    ________javax.swing.JScrollPane
    __________javax.swing.JViewport
    ____________javax.swing.JTable
    ______________javax.swing.CellRendererPane
    __________javax.swing.JScrollPane$ScrollBar
    ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
    ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
    __________javax.swing.JScrollPane$ScrollBar
    ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
    ____________com.sun.java.swing.plaf.windows.WindowsScrollBarUI$WindowsArrowButton
    __________javax.swing.JViewport
    ____________javax.swing.table.JTableHeader
    ______________javax.swing.CellRendererPane
*/
    
    private void performTest(currentTest c) {

        PullUpOperator   pullUp   = null;
        PushDownOperator pushDown = null;
        
        ErrorOperator eo = null;
        String report = "";

        boolean debugMode = false;

        EditorOperator editor;

        // open source file
        String curClass = "";
        switch (c) {
            case testSimple_A_A: curClass = "Base_A"; break;
            case testSimple_A_B: curClass = "Base_B"; break;
            case testSimple_A_C: curClass = "Base_C"; break;
            case testSimple_A_D: curClass = "Base_D"; break;
            case testSimple_A_E: curClass = "Base_E"; break;
            case testSimple_A_F: curClass = "Base_F"; break;
            case testSimple_A_G: curClass = "Base_G"; break;
            case testSimple_A_H: curClass = "Base_H"; break;
            case testSimple_A_I: curClass = "Base_I"; break;
        }

        // open source file
        switch (c) {
            default:
                openSourceFile("pull_push", curClass);
                editor = new EditorOperator(curClass + ".java");
                break;
        }

        if (debugMode) {
            new EventTool().waitNoEvent(2000);
        }

        // select part of code    
                switch (c) {
            case testSimple_A_A:
            case testSimple_A_B:
            case testSimple_A_C:
            case testSimple_A_F:
            case testSimple_A_G:
                editor.setCaretPosition(11, 0);  //
                editor.select(11, 20, 23);       // public int fact(int i) {...}
                break;
            case testSimple_A_I:
                editor.setCaretPosition(40, 0);  //
                editor.select(40, 21, 22);       // public void m1() {...}
                break;
            case testSimple_A_D:
            case testSimple_A_H:
                editor.setCaretPosition(32, 0);  //
                editor.select(32, 21, 26);       // public void method() {...}
                break;
            case testSimple_A_E:
                editor.setCaretPosition(24, 0);  // @Override
                editor.select(24, 23, 33);       // public String getSequence(int i) {...}
                break;
        }

        if (debugMode) {
            new EventTool().waitNoEvent(1000);
        }

        // call Reafctor > Pull Up
        switch (c) {
            case testSimple_A_A:
            case testSimple_A_B:
            case testSimple_A_C:
            case testSimple_A_D:
            case testSimple_A_E:
                new RefactorPullUpAction().performPopup(editor);
                break;
            case testSimple_A_F:
            case testSimple_A_G:
            case testSimple_A_H:
            case testSimple_A_I:
                new RefactorPushDownAction().performPopup(editor);
                break;
        }

        // catch Pull Up dialog
        switch (c) {
            case testSimple_A_A:
            case testSimple_A_B:
            case testSimple_A_C:
            case testSimple_A_D:
            case testSimple_A_E:
                pullUp = new PullUpOperator();
                break;
            case testSimple_A_F:
            case testSimple_A_G:
            case testSimple_A_H:
            case testSimple_A_I:
                pushDown = new PushDownOperator();
                break;
        }

        if (debugMode) {
            new EventTool().waitNoEvent(2000);
        }

        new EventTool().waitNoEvent(1500); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // set destination
        switch(c){
            case testSimple_A_A:
            case testSimple_A_B:
            case testSimple_A_D:
            case testSimple_A_E:
                pullUp.setDestinationSupertype("abstract class ClassA");
                break;
            case testSimple_A_C:
                pullUp.setDestinationSupertype("interface InterfaceA");
                break;
        }
        
        if (debugMode) {
            new EventTool().waitNoEvent(3000);
        }

        // select class / mark abstract
        switch(c){
            case testSimple_A_B:
                pullUp.setValueAt(0, 2, true); // public int fact(int i) {...} - abstract
                break;
            case testSimple_A_D:
                pullUp.setValueAt(0, 0, true); // public int fact(int i) {...} - select
                pullUp.setValueAt(0, 2, true); // public int fact(int i) {...} - abstract
                pullUp.setValueAt(4, 0, true); // public void method() {...} - select
                break;
            case testSimple_A_F:
                pushDown.setValueAt(1, 2, true); // public int fact(int i) {...} - abstract
                break;
            case testSimple_A_G:
                pushDown.setValueAt(0, 0, true);  // InterfaceA - select
                pushDown.setValueAt(1, 0, false); // public int fact(int i) {...} - deseslect
                break;
            case testSimple_A_H:
                pushDown.setValueAt(4, 2, true); // public void method() {...} - abstract
                pushDown.setValueAt(2, 0, true); // InterfaceA - select
                break;
            case testSimple_A_I:
                pushDown.setValueAt(1, 0, true); // class c1 {...} - select
                break;
        }
                
        if (debugMode) {
            new EventTool().waitNoEvent(3000);
        }
        
        switch (c) {
            case testSimple_A_A:
            case testSimple_A_B:
            case testSimple_A_C:
            case testSimple_A_D:
            case testSimple_A_E:
                pullUp.pressRefactor();
                break;
            case testSimple_A_F:
            case testSimple_A_G:
            case testSimple_A_H:
            case testSimple_A_I:
                pushDown.pressRefactor();
                break;
        }
        
        if (debugMode) {
            new EventTool().waitNoEvent(3000);
        }
        
        new EventTool().waitNoEvent(1500); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        // evalue result and discard changes
        ref(editor.getText());
        editor.closeDiscard();
    }
}
