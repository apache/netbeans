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
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class AddElementHintTest extends HintsTestCase{

    public AddElementHintTest(String name) {
        super(name);
    }
    
    public void testAddElement() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(72,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter \"a\"",
        "Create Local Variable \"a\"",
        "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private int a;.*");
    }
    
    public void testAddElement2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(66,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter \"g\"",
        "Create Local Variable \"g\"",
        "Create Field \"g\" in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private LinkedList<String> g;.*");
    }
    
    public void testAddElement3() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field \"field\" in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*int field;.*");
    }
    
    public void testAddElement4() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(46,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field \"statField\" in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*static String statField;.*");
    }
    
    
    
    public static void main(String[] args) {
        new TestRunner().run(AddElementHintTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddElementHintTest.class).enableModules(".*").clusters(".*"));
    }
    
}
