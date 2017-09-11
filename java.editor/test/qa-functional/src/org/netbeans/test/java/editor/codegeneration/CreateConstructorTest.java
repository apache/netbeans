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
