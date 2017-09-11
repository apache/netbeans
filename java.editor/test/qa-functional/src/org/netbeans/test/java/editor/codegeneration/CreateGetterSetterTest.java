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
import org.netbeans.test.java.editor.jelly.GenerateGettersAndSettersOperator;

/**
 *
 * @author jp159440
 */
public class CreateGetterSetterTest extends GenerateCodeTestCase {

    public CreateGetterSetterTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testAvailableGettersSetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",5,rowCount);
            
        } finally {
            editor.close(false);
        }        
    }    
    
    public void testAvailableGetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",6,rowCount);
        } finally {
            editor.close(false);
        }
    }
    
    public void testAvailableSetters() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.SETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            int rowCount = jto.getRowCount();
            ggso.cancel();
            assertEquals("Wrong number of rows",6,rowCount);
        } finally {
            editor.close(false);
        }        
    }    

        
    public void testPrimitiveType() {        
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(2);            
            ggso.generate();            
            String expected = "" +
                            "    public int getNum() {\n"+
                            "        return num;\n"+
                            "    }\n"+
                            "\n"+
                            "    public void setNum(int num) {\n"+
                            "        this.num = num;\n"+
                            "    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testObjectType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(4);            
            ggso.generate();            
            String expected = "" +
"    public List<? extends Thread> getThreads() {\n"+
"        return threads;\n"+
"    }\n"+
"\n"+
"    public void setThreads(List<? extends Thread> threads) {\n"+
"        this.threads = threads;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testBooleanType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(1);            
            ggso.generate();            
            String expected = "" +
"    public boolean isBool() {\n"+
"        return bool;\n"+
"    }\n"+
"\n"+
"    public void setBool(boolean bool) {\n"+
"        this.bool = bool;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testStaticType() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER,editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(3);            
            ggso.generate();            
            String expected = "" +
"    public static int getStatField() {\n"+
"        return statField;\n"+
"    }\n"+
"\n"+
"    public static void setStatField(int statField) {\n"+
"        CreateGetterSetter.statField = statField;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }        
    }
    
    public void testMultipleSetter() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_SETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.SETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(3);
            jto.selectRow(2);
            jto.selectRow(1);
            ggso.generate();
            String expected = "" +
"    public void setNum(int num) {\n"+
"        this.num = num;\n"+
"    }\n"+ 
"\n"+
"    public void setBool(boolean bool) {\n"+
"        this.bool = bool;\n"+
"    }\n"+
"\n"+
"    public void setHasGetter(int hasGetter) {\n"+
"        this.hasGetter = hasGetter;\n"+
"    }\n";

            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public void testMultipleGetter() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_ONLY);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();
            jto.selectRow(1);
            jto.selectRow(2);
            jto.selectRow(3);
            ggso.generate();
            String expected = "" +
                    "    public int getNum() {\n"+
"        return num;\n"+
"    }\n"+
"\n"+
"    public boolean isBool() {\n"+
"        return bool;\n"+
"    }\n"+
"\n"+
"    public int getHasSetter() {\n"+
"        return hasSetter;\n"+
"    }\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public void testArray() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "CreateGetterSetter");
        editor = new EditorOperator("CreateGetterSetter");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(11, 1);
            editor.txtEditorPane().typeText("int [] pole;");
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_GETTER_SETTER, editor);
            GenerateGettersAndSettersOperator ggso = new GenerateGettersAndSettersOperator(GenerateGettersAndSettersOperator.GETTERS_AND_SETTERS);
            JTreeOperator jto = ggso.treeTreeView$ExplorerTree();            
            jto.selectRow(3);
            ggso.generate();
            String expected = "" +
"    public int[] getPole() {\n"+
"        return pole;\n"+
"    }\n"+
"\n"+
"    public void setPole(int[] pole) {\n"+
"        this.pole = pole;\n"+
"    }\n"; 
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }                
    }
    
    public static void main(String[] args) {
        TestRunner.run(CreateGetterSetterTest.class);
    }

    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateGetterSetterTest.class).enableModules(".*").clusters(".*"));
    }
    
    
    
}
