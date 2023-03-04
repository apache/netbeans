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
package org.netbeans.test.java.editor.codegeneration;


import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.jelly.GenerateConstructorOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class CreateConstructorTest extends GenerateCodeTestCase {

    /** Creates a new instance of CreateConstructor */
    public CreateConstructorTest(String name) {
        super(name);
    }

    public void testSuperConstructor() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(2);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(ThreadGroup group, Runnable target) {\n" +
                    "        super(group, target);\n" +
                    "    }\n";
            String expected2 = "" +
                    "    public testSimpleCase(ThreadGroup tg, Runnable r) {\n" +
                    "        super(tg, r);\n" +
                    "    }\n";
            waitAndCompare(expected, expected2);
        } finally {
            editor.close(false);
        }
    }

    public void testInitFields() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(1);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(int b) {\n" +
                    "        this.b = b;\n" +
                    "    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public void testInitFieldAndSuper() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(7);
            jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(2);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(double c, ThreadGroup group, Runnable target, String name, long stackSize) {\n" +
                    "        super(group, target, name, stackSize);\n" +
                    "        this.c = c;\n" +
                    "    }\n";
            String expected2 = "" +
                    "    public testSimpleCase(double c, ThreadGroup tg, Runnable r, String string, long l) {\n" +
                    "        super(tg, r, string, l);\n" +
                    "        this.c = c;\n" +
                    "    }\n";
            waitAndCompare(expected, expected2);
        } finally {
            editor.close(false);
        }
    }

    public void testMultipleSuperSelection() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(2);
            jto.selectRow(3);
            jto.selectRow(4);
            jto.selectRow(7);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(ThreadGroup group, Runnable target, String name, long stackSize) {\n" +
                    "        super(group, target, name, stackSize);\n" +
                    "    }\n";
            String expected2 = "" +
                    "    public testSimpleCase(ThreadGroup tg, Runnable r, String string, long l) {\n" +
                    "        super(tg, r, string, l);\n" +
                    "    }\n";
            waitAndCompare(expected, expected2);
        } finally {
            editor.close(false);
        }
    }

    public void testMultipleFiledSelection() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(0);
            jto.selectRow(1);
            jto.selectRow(2);
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(int b, double c) {\n" +
                    "        this.b = b;\n" +
                    "        this.c = c;\n" +
                    "    }";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public void testCancel() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(1);
            gco.btCancel().push();
            String expected = "" +
                    "public class testSimpleCase extends Thread {\n" +
                    "    \n" +
                    "    private String a;\n" +
                    "    \n" +
                    "    int b;\n" +
                    "    \n" +
                    "    public double c;\n" +
                    "           \n" +
                    "    \n" +
                    "    \n" +
                    "    /** Creates a new instance of testSimpleCase */\n" +
                    "    public testSimpleCase() {\n" +
                    "    }\n" +
                    "    \n" +
                    "}\n";                        
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public void testUndoRedo() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testSimpleCase");
        editor = new EditorOperator("testSimpleCase");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(12, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree2();
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "    public testSimpleCase(String a) {\n" +
                    "        this.a = a;\n" +
                    "    }";
            waitAndCompare(expected);
            new Action("Edit|Undo", null).perform();
            assertFalse("Constuctor not removed", editor.getText().contains(expected));
            MainWindowOperator.getDefault().menuBar().pushMenu("Edit");
            MainWindowOperator.getDefault().menuBar().closeSubmenus();
            new Action("Edit|Redo", null).perform();
            assertTrue("Constuctor not re-inserted", editor.getText().contains(expected));
        } finally {
            editor.close(false);
        }
    }

    public void testInnerClass() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "TestInnerClass");
        editor = new EditorOperator("TestInnerClass");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(20, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            GenerateConstructorOperator gco = new GenerateConstructorOperator();
            JTreeOperator jto = gco.treeTreeView$ExplorerTree();
            jto.selectRow(0);
            gco.btOK().push();
            String expected = "" +
                    "        public Inner(String afield) {\n" +
                    "            this.afield = afield;\n" +
                    "        }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }
    
    public void testIssue100341() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "test100341b");
        editor = new EditorOperator("test100341b");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(13, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_CONSTRUCTOR, editor);
            String expected = "" +
                    "    public test100341b(String data) {\n" +
                    "        super(data);\n" +
                    "    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public static void main(String[] args) {
        TestRunner.run(CreateConstructorTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateConstructorTest.class).enableModules(".*").clusters(".*"));
     }
}
