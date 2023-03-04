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
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class StaticAccessTest extends NbTestCase {
    
    public StaticAccessTest(String testName) {
        super(testName);
    }

    public void testCallingStaticMethodInInitializer() throws Exception {
        String before = "package test; class Test {\n" +
            "{\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = (before + after).replace('\n', ' ').replace("b.value", "Boolean.value");
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:6-3:13:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingStaticMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = b.valu";
        String after = "eOf(true);\n" +
            "}\n" +
            "}\n";

        String golden = ("package test; class Test {\n" +
            "public void nic() {\n" +
            "Boolean b = null;\n" +
            "b = Boolean.valueOf(true);\n" +
            "}\n" +
            "}\n").replace('\n', ' ');
        
        
        performFixTest("test/Test.java", before + after, before.length(), 
            "3:6-3:13:verifier:AS1valueOf",
            "MSG_StaticAccessText",
            golden
        );
    }
    
    public void testCallingNonStaticStringMethod() throws Exception {
        String before = "package test; class Test {\n" +
            "public void nic() {\n" +
            "String s = \"some\";\n" +
            "int x = s.last";
        String after = "IndexOf('x');" +
            "}" +
            "";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    
    public void testOkCallingStaticMethod() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.valu";
        String after = "eOf(true);";
        
        performAnalysisTest("test/Test.java", before + after, before.length()); 
    }
    public void testAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = b.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:52-0:56:verifier:AS0TRUE"
        );
    }
    public void testOkAccessingStaticField() throws Exception {
        String before = "package test; class Test {" +
            "{" +
            " Boolean b = null;" +
            " b = Boolean.TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testAccessingStaticFieldViaMethod() throws Exception {
        String before = "package test; class Test {" +
            "static Boolean b() { return null; }" +
            "{" +
            " Object x = b().TR";
        String after = "UE;";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:78-0:82:verifier:AS0TRUE"
        );
    }
    public void testOkToCallEqualsOnString() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "String s = null;\n" +
            "boolean b = \"A\".e";
        String after =         "quals(s);\n" +
            "}" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testCorrectResolution() throws Exception {
        String before = "package test; class Test {" +
            "public void run() {\n" +
            "Test t = null;\n" +
            "t.t";
        String after =         "est(2);\n" +
            "}\n" +
            "public void test() {}\n" + 
            "public static void test(int i) {}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(),
                "2:2-2:6:verifier:AS1test"
        );
    }
    
    public void testIgnoreErrors1() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getClass().getName();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors2() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "aaa.getClass();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors3() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(String aaa) {\n" +
            "aaa.fff();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }
    
    public void testIgnoreErrors4() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run() {\n" +
            "super.fff();\n" +
            "}\n" +
            "}";
        
        performAnalysisTest("test/Test.java", code);
    }

    public void testInterface198646() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(A a) {\n" +
            "int i = a.III;\n" +
            "}\n" +
            "}\n" +
            "interface A {\n" +
            "    public static final int III = 0;\n" +
            "}";

        performAnalysisTest("test/Test.java", code, "2:10-2:13:verifier:AS0III");
    }

    public void testEnum198646() throws Exception {
        String code = "package test; class Test {\n" +
            "public void run(A a) {\n" +
            "int i = a.III;\n" +
            "}\n" +
            "}\n" +
            "enum A {\n" +
            "    A;\n" +
            "    public static final int III = 0;\n" +
            "}";

        performAnalysisTest("test/Test.java", code, "2:10-2:13:verifier:AS0III");
    }
    
    protected void performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        performFixTest(fileName, code, errorDescriptionToString, fixDebugString, golden);
    }
    
    protected void performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        HintTest.create()
                .input(fileName, code)
                .run(StaticAccess.class)
                .findWarning(errorDescriptionToString)
                .applyFix(fixDebugString)
                .assertCompilable()
                .assertOutput(fileName, golden);
    }
    
    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        HintTest.create()
                .input(fileName, code, false)
                .run(StaticAccess.class)
                .assertWarnings(golden);
    }

    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        performAnalysisTest(fileName, code, golden);
    }

}
