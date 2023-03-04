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
public class HintsTest extends HintsTestCase {

    public HintsTest(String testMethodName) {
        super(testMethodName);
    }
      
    
    public void testCast() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(9,1);        
        useHint("Cast ...new Object(...) to String",new String[]{"Cast ...new Object(...) to String","Change type of s to Object"},".*String s = \\(String\\) new Object\\(\\);.*");
    }
    
    public void testCast2() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(12,1);        
        useHint("Cast ...get(...) to File",new String[]{"Cast ...get(...) to File","Change type of i to Object"},
                ".*File i = \\(File\\) l.get\\(1\\);.*");
    }
    
    public void testCast3() {
        String file = "castHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(16,1);
        useHint("Cast ...get(...) to Integer",new String[]{"Cast ...get(...) to Integer","Change type of i to Number"},
                ".*Integer i = \\(Integer\\) nums.get\\(1\\);.*");
    }
    
    public void testAddParam() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(51,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"x\"",
                                                  "Create Local Variable \"x\"",
                                                  "Create Field \"x\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public addHint\\(String\\[\\] x\\) \\{.*");
    }
    
    public void testAddParam2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(60,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"a\"",
                                                  "Create Local Variable \"a\"",
                                                  "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,int a\\) \\{.*");
    }
    
    public void testAddParam3() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(61,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"b\"",
                                                  "Create Local Variable \"b\"",
                                                  "Create Field \"b\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,long b\\) \\{.*");
    }
    
    public void testAddParam4() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(62,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"c\"",
                                                  "Create Local Variable \"c\"",
                                                  "Create Field \"c\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,char c\\) \\{.*");
    }
    
    public void testAddParam5() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(63,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"d\"",
                                                  "Create Local Variable \"d\"",
                                                  "Create Field \"d\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,byte d\\) \\{.*");
    }
    
    public void testAddParam6() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(64,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"e\"",
                                                  "Create Local Variable \"e\"",
                                                  "Create Field \"e\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,double e\\) \\{.*");
    }
    
    public void testAddParam7() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(65,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"f\"",
                                                  "Create Local Variable \"f\"",
                                                  "Create Field \"f\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,Integer f\\) \\{.*");
    }
    
     public void testAddParam8() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(66,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"g\"",
                                                  "Create Local Variable \"g\"",
                                                  "Create Field \"g\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,LinkedList<String> g\\) \\{.*");
    }
     
    public void testAddParam9() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(67,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"h\"",
                                                  "Create Local Variable \"h\"",
                                                  "Create Field \"h\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,String h\\) \\{.*");
    } 
    
    public void testAddParamA() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(72,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"a\"",
                                                  "Create Local Variable \"a\"",
                                                  "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method2\\(double x,int a, int ... y\\) \\{.*");
    }
    
    public void testAddParamB() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(68,1);
        useHint("Create Parameter",new String[]{"Create Parameter \"i\"",
                                                  "Create Local Variable \"i\"",
                                                  "Create Field \"i\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*public void method\\(String p1, int p2,Map<String, List<String>> i\\) \\{.*");
    }
    
    public void testAddLocal() {
        String file = "addHint";
        setInPlaceCreation(true);
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(51,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"x\"",
                                                  "Create Local Variable \"x\"",
                                                  "Create Field \"x\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*String\\[\\] x = new String\\[\\]\\{\"array\"\\};.*");
    }
    
    public void testAddLocal2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(60,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"a\"",
                                                  "Create Local Variable \"a\"",
                                                  "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*int a = 3;.*");
    }
    
    public void testAddLocal3() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(61,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"b\"",
                                                  "Create Local Variable \"b\"",
                                                  "Create Field \"b\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*long b = 3l;.*");
    }
    
    public void testAddLocal4() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(62,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"c\"",
                                                  "Create Local Variable \"c\"",
                                                  "Create Field \"c\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*char c = 'c';.*");
    }
    
    public void testAddLocal5() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(63,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"d\"",
                                                  "Create Local Variable \"d\"",
                                                  "Create Field \"d\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*byte d = \\(byte\\) 2;.*");
    }
    
    public void testAddLocal6() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(64,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"e\"",
                                                  "Create Local Variable \"e\"",
                                                  "Create Field \"e\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*double e = 3.4;.*");
    }
    
    public void testAddLocal7() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(65,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"f\"",
                                                  "Create Local Variable \"f\"",
                                                  "Create Field \"f\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*Integer f = new Integer\\(1\\).*");
    }
    
     public void testAddLocal8() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(66,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"g\"",
                                                  "Create Local Variable \"g\"",
                                                  "Create Field \"g\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*LinkedList<String> g = new LinkedList<String>\\(\\);.*");
    }
     
    public void testAddLocal9() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(67,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"h\"",
                                                  "Create Local Variable \"h\"",
                                                  "Create Field \"h\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*String h = \"ssss\";.*");
    } 
    
    public void testAddLocalA() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(72,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"a\"",
                                                  "Create Local Variable \"a\"",
                                                  "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*int a = 3;.*");
    }
    
    public void testAddLocalB() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(68,1);
        useHint("Create Local Variable",new String[]{"Create Parameter \"i\"",
                                                  "Create Local Variable \"i\"",
                                                  "Create Field \"i\" in org.netbeans.test.java.hints.HintsTest.addHint"},                                                                                                    
                ".*Map<String, List<String>> i = getMap\\(\\);.*");
    }
                    
    public static void main(String[] args) {
        new TestRunner().run(HintsTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(HintsTest.class).enableModules(".*").clusters(".*"));
    }
    
}
