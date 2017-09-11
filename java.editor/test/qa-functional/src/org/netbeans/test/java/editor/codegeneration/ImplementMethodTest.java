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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
