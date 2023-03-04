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

import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class UtilityClassTest extends NbTestCase {
    
    public UtilityClassTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
    }
    
    
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public static boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), 
            "0:27-0:31:verifier:Utility class without constructor"
        );
    }
    public void testClassWithOnlyStaticMethodsAndFields() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public static boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            " public static final String PROP_X = null;";
            ;
        
        String gold = before + after + " private Test() { } }";
        performFixTest(before + after + "}",
            "0:27-0:31:verifier:Utility class without constructor",
            gold
        );
    }
    public void testDisabledWhenNoMethodIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenMehtodIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public boolean isEventQueue() { return false; }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledExtendingNonObject() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends javax.swing.JPanel {" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenConstructorIsThere() throws Exception {
        String before = "package test; public class Te";
        String after = "st extends Object {" +
            " public Test() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length(), "0:56-0:60:hint:Utility class with visible constructor");
    }

    public void testNoExceptionForVeryBrokenClass() throws Exception {
        String before = "package test; public class Test { private static final cla";
        String after = "ss private static final class A{} }";
        
        HintTest.create()
                .input(before + after, false)
                .run(UtilityClass.class)
                .assertWarnings();
    }
    
    public void testDisabledOnEnums() throws Exception {
        String before = "package test; public enum Te";
        String after = "st {" +
            " ONE, TWO;" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledOnInterfaces() throws Exception {
        String before = "package test; public interface Te";
        String after = "st {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledOnAnnotations() throws Exception {
        String before = "package test; public @interface Te";
        String after = "st {" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testMultipleConstructors() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test {" +
                            "    public Test() { }" +
                            "    public Test(int i) { }" +
                            "}");
    }
    
    public void testDisabledWhenMain() throws Exception {
        HintTest.create()
                .input("package test; public class Test {" +
                       " public static void main(String... args) { }" +
                       "}")
                .run(UtilityClass.class)
                .assertWarnings();
    }
    
    //public/protected constructor in UtilityClass:
    public void testEnabledWhenConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " public Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        String golden = (before + after).replace("public Test()", "private Test()");
        performFixTest(before + after, 
            "0:56-0:60:hint:Utility class with visible constructor",
            golden
        );
    }
    public void testDisabledWhenPrivateConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " private Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    public void testDisabledWhenPackagePrivateConstructorIsThere() throws Exception {
        String before = "package test; public class Test extends Object {" +
            " Te";
        String after = "st() { }" +
            " public static String computeDiff(String x, String y) { return x + y; }" +
            "}";
        
        performAnalysisTest("test/Test.java", before + after, before.length());
    }
    
    public void testException197721() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test extends Exception {\n" +
                            "    private static final long serialVersionUID = 1L;\n" +
                            "    public Test() { }\n" +
                            "    public Test(int i) { }\n" +
                            "}");
    }
    
    private void performAnalysisTest(String fileName, String code, int ignore, String... golden) throws Exception {
        performAnalysisTest(fileName, code, golden);
    }
    
    private void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        HintTest.create()
                .input(fileName, code)
                .run(UtilityClass.class)
                .assertWarnings(golden);
    }

    private void performFixTest(String code, String warning, String result) throws Exception {
        HintTest.create()
                .input(code)
                .run(UtilityClass.class)
                .findWarning(warning)
                .applyFix()
                .assertCompilable()
                .assertOutput(result);
    }
    
}
