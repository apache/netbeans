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
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.jelly.ImplementMethodsOperator;

/**
 *
 * @author Jiri Prox
 */
public class ImplementMethodTest extends GenerateCodeTestCase {

    /** Creates a new instance of CreateConstructor */
    public ImplementMethodTest(String name) {
        super(name);
    }

    public void testIssue112613() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "test112613");
        editor = new EditorOperator("test112613");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(18, 5);
            assertTrue("Expected items are not pressent",GenerateCodeOperator.checkItems(editor, GenerateCodeOperator.GENERATE_CONSTRUCTOR, GenerateCodeOperator.OVERRIDE_METHOD));
        } finally {
            editor.close(false);
        }
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "test112613b");
        editor = new EditorOperator("test112613b");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(17, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.IMPLEMENT_METHOD, editor);
            ImplementMethodsOperator imo = new ImplementMethodsOperator();
            JTreeOperator jto = imo.treeTreeView$ExplorerTree();
            jto.selectRow(1);
            imo.btGenerate().push();
            String expected = "" +
                    "    public void m() {\n" +
                    "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                    "    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public void testMoreIfaces() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testMoreIfaces");
        editor = new EditorOperator("testMoreIfaces");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(7, 5);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.IMPLEMENT_METHOD, editor);
            ImplementMethodsOperator imo = new ImplementMethodsOperator();
            JTreeOperator jto = imo.treeTreeView$ExplorerTree();
            jto.expandRow(4);
            jto.selectRow(1);
            jto.selectRow(5);
            imo.btGenerate().push();            
            String expected = "" +
                    "    @Override\n"+
                    "    public int getColumnCount() {\n" +
                    "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n"+
                    "    public void run() {\n" +
                    "        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                    "    }\n";            
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }

    }

    public static void main(String[] args) {
        TestRunner.run(ImplementMethodTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ImplementMethodTest.class).enableModules(".*").clusters(".*"));
    }
}
