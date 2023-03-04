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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testSimpleFlip() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return l.e|quals(this);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:19-3:19:hint:Flip .equals")
                .applyFix("Flip .equals")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.List;" +
                              "public class Test {\n" +
                              "     private boolean test(List l) {\n" +
                              "         return this.equals(l);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testFlipImplicitThis() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return e|quals(l);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:17-3:17:hint:Flip .equals")
                .applyFix("Flip .equals")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.List;" +
                              "public class Test {\n" +
                              "     private boolean test(List l) {\n" +
                              "         return l.equals(this);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConvertBase1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0x12;\n" +
                              "}\n");
    }

    public void testConvertBase2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_2")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0b10010;\n" +
                              "}\n");
    }

    public void testConvertBaseLong() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 42|94967296L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:30-2:30:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = 0x100000000L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = -42|94967296L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:31-2:31:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = 0xffffffff00000000L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 0xffffffff|00000000L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:38-2:38:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_10")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = -4294967296L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative203362() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = -|1;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0xffffffff;\n" +
                              "}\n");
    }

    public void testSplitDeclaration1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int I =| -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("3:15-3:15:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        int I;\n" +
                              "        I = -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        final int I =| -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("3:21-3:21:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        final int I;\n" +
                              "        I = -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration3() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        System.err.println(1);\n" +
                       "        @SuppressWarnings(\"dummy\") final int I =| -1;\n" +
                       "        System.err.println(2);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:48-4:48:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        System.err.println(1);\n" +
                              "        @SuppressWarnings(\"dummy\") final int I;" +
                              "        I = -1;\n" +
                              "        System.err.println(2);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration4() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "            int k =| -1,j = 1;\n" +
                       "            break; \n" +
                       "        } \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("6:19-6:19:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test() { \n" +
                              "        int i = 1; \n" +
                              "        switch(i){ \n" +
                              "            case 1: \n" +
                              "            int k;\n" +
                              "            k = -1;\n" +
                              "            int j = 1;\n" +
                              "            break; \n" +
                              "        } \n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration5() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() { \n" +
                       "        int i = 1; \n" +
                       "        switch(i){ \n" +
                       "            case 1: \n" +
                       "            final int k =| -1,j = 1;\n" +
                       "            break; \n" +
                       "        } \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("6:25-6:25:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test() { \n" +
                              "        int i = 1; \n" +
                              "        switch(i){ \n" +
                              "            case 1: \n" +
                              "            final int k;\n" +
                              "            k = -1;\n" +
                              "            final int j = 1;\n" +
                              "            break; \n" +
                              "        } \n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclarationForVar1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void m1(){\n" +
                       "        var v =| 10; \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_splitDeclaration");
    }
    
    public void testSplitDeclarationForVar2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void m1(){\n" +
                       "        final var i =| 10; \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_splitDeclaration");
    }
    
    public void testSplitDeclarationForVar3() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void m1(){\n" +
                       "        final/*comment*/var x =| 1.5; \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_splitDeclaration");
    }
    
    public void testSplitDeclarationForVar4() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void m1(){\n" +
                       "        var/*comment*/y =| 100; \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_splitDeclaration");
    }
    
    public void testSplitDeclarationForVar5() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void m1(){\n" +
                       "        Runnable r =| new Runnable(){ \n" +
                       "        @Override \n" +
                       "        public void run() { \n" +
                       "        var v = 10; \n" +
                       "        } \n" +
                       "      }; \n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(Tiny.class)
                .findWarning("3:20-3:20:hint:ERR_splitDeclaration");
    }
 
    public void testFillSwitch1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Test a) {\n" +
                       "        sw|itch (a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(Tiny.KEY_DEFAULT_ENABLED, false)
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Test a) {\n" +
                              "        switch (a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitch2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(Tiny.KEY_DEFAULT_ENABLED, false)
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "            default: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchGenerateDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCasesAndDefault")
                .applyFix("FIX_Tiny.fillSwitchCasesAndDefault")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: throw new AssertionError(((Test) a).name());\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchOnlyGenerateDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "            case B: break;\n" +
                       "            case C: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchDefault")
                .applyFix("FIX_Tiny.fillSwitchDefault")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: throw new AssertionError(((Test) a).name());\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testFillSwitchTypeVar222372() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static <T extends Enum> void t(T a) {\n" +
                       "        sw|itch (a) {\n" +
                       "            case A: break;\n" +
                       "            case B: break;\n" +
                       "            case C: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testInlineRedundantVariable1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        int i =| 10;\n" +
                       "        return i;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("3:12-3:13:hint:ERR_Tiny.inlineRedundantVar")
                .applyFix("FIX_Tiny.inlineRedundantVar")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test() {\n" +
                              "        return 10;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testInlineRedundantVariable2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        System.out.println(\"Start\");\n" +
                       "        int i =| 10;\n" +
                       "        return i;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:12-4:13:hint:ERR_Tiny.inlineRedundantVar")
                .applyFix("FIX_Tiny.inlineRedundantVar")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test() {\n" +
                              "        System.out.println(\"Start\");\n" +
                              "        return 10;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testInlineRedundantVariable3() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        System.out.println(\"Start\");\n" +
                       "        int i =| 10;\n" +
                       "        System.out.println(\"Stop\");\n" +
                       "        return i;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_Tiny.inlineRedundantVar");
    }

    public void testInlineRedundantVariable4() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        int i =| 10;\n" +
                       "        return System.identityHashCode(i);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_Tiny.inlineRedundantVar");
    }

    public void testInlineRedundantVariable5() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        @SuppressWarnings(\"test\")\n" +
                       "        int i =| 10;\n" +
                       "        return i;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_Tiny.inlineRedundantVar");
    }

    public void testInlineRedundantVariable6() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        System.out.println(\"Start\");\n" +
                       "        int i =| 10;\n" +
                       "        Object o = i;\n" +
                       "        return (int) o;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:12-4:13:hint:ERR_Tiny.inlineRedundantVar")
                .applyFix("FIX_Tiny.inlineRedundantVar")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private int test() {\n" +
                              "        System.out.println(\"Start\");\n" +
                              "        Object o = 10;\n" +
                              "        return (int) o;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testInlineRedundantVariable7() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int test() {\n" +
                       "        System.out.println(\"Start\");\n" +
                       "        int i =| 10;\n" +
                       "        Object o = i;\n" +
                       "        return (int) o + i;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertNotContainsWarnings("ERR_Tiny.inlineRedundantVar");
    }
}
