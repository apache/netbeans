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
package org.netbeans.test.java.hints;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class AddImportTest extends HintsTestCase{

    public AddImportTest(String name) {
        super(name);
    }
    
    
    public void testAddImport1() {
	String file = "Imports";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(46,1);
	String pattern = ".*import java.net.URL;.*";
	useHint("Add import for java.net",new String[]{"Add import for java.net.URL","Add import for javax.print.DocFlavor.URL","Create class \"URL\""},pattern);
    }
    
    public void testAddImport2() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(47,1);        
        String pattern = ".*";
        useHint("Create",new String[]{"Create class \"NonExisting\""},pattern);
    }
    
    public void testAddImport3() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(48,1);        
        String pattern = ".*import javax.swing.JButton;.*";
        useHint("Add import",new String[]{"Add import for javax.swing.JButton"},pattern);
    }
       
    public void testRemoveImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);        
        String pattern = ".*import java\\.net\\.URL;\\simport java\\.util\\.List;.*";
        useHint("Remove Unused Import",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public void testRemoveAllImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);        
        String pattern = ".*package org.netbeans\\.test\\.java\\.hints\\.HintsTest;\\s*import java\\.io\\.FileReader;\\s*public class RemoveImport \\{.*";
        useHint("Remove All Unused Imports",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public static void main(String[] args) {
        new TestRunner().run(AddImportTest.class);                
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddImportTest.class).enableModules(".*").clusters(".*"));
    }

}
