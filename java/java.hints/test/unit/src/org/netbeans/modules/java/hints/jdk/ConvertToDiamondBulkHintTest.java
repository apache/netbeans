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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.spiimpl.TestCompilerSettings;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class ConvertToDiamondBulkHintTest extends NbTestCase {

    public ConvertToDiamondBulkHintTest(String name) {
        super(name);
    }

    private String allBut(String key) {
        return ("," + ConvertToDiamondBulkHint.ALL + ",")
                .replace("," + key + ",", ",");
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.LinkedList<String> l = new java.util.LinkedList<String>();\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .findWarning("2:49-2:77:verifier:Redundant type arguments in new expression (use diamond operator instead).")
                .applyFix("FIX_ConvertToDiamond")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private java.util.LinkedList<String> l = new java.util.LinkedList<>();\n" +
                              "}\n");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "    private LinkedList<String> l = new LinkedList<String>();\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .findWarning("3:39-3:57:verifier:Redundant type arguments in new expression (use diamond operator instead).")
                .applyFix("FIX_ConvertToDiamond")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.LinkedList;\n" +
                              "public class Test {\n" +
                              "    private LinkedList<String> l = new LinkedList<>();\n" +
                              "}\n");
    }

    public void testConfiguration1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.LinkedList<String> l = new java.util.LinkedList<String>();\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("initializer"))
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { java.util.LinkedList<String> l = new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("initializer"))
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration2a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { java.util.LinkedList<String> l = new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:43-2:71:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration2b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { java.util.LinkedList<String> l = new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:43-2:71:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration2c() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { java.util.LinkedList<java.util.LinkedList<?>> l = new java.util.LinkedList<java.util.LinkedList<?>>(); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("initializer"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { final java.util.LinkedList<String> l = new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("initializer"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.LinkedList<String> l() { return new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("initializer"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:58-2:86:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration5() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.LinkedList<String> l() { return new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("return"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration6a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    private void l(java.util.LinkedList<? extends CharSequence> a) { l(new Test<CharSequence>()); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("argument"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration6b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    private void l(java.util.LinkedList<? extends CharSequence> a) { this.l(new Test<CharSequence>()); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("argument"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration6c() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    public Test(java.util.LinkedList<? extends CharSequence> a) { new Test(new Test<CharSequence>(null)); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("argument"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration6d() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    public Test(java.util.LinkedList<? extends CharSequence> a) { new Test<String>(new Test<CharSequence>(null)); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("argument"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    public void testConfiguration7a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    private void l(java.util.LinkedList<? extends CharSequence> a) { l(new Test<CharSequence>()); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:75-2:93:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration7b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    private void l(java.util.LinkedList<? extends CharSequence> a) { this.l(new Test<CharSequence>()); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:80-2:98:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration7c() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    public Test(java.util.LinkedList<? extends CharSequence> a) { new Test(new Test<CharSequence>(null)); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:79-2:97:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration7d() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test<T extends CharSequence> extends java.util.LinkedList<T> {\n" +
                       "    public Test(java.util.LinkedList<? extends CharSequence> a) { new Test<String>(new Test<CharSequence>(null)); }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings("2:87-2:105:verifier:Redundant type arguments in new expression (use diamond operator instead).");
    }

    public void testConfiguration8() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    { java.util.LinkedList<String> l; l = new java.util.LinkedList<String>(); }\n" +
                       "}\n")
                .preference(ConvertToDiamondBulkHint.KEY, allBut("assignment"))
                .sourceLevel("1.7")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }
    
    public void testConfiguration9() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        var list = new java.util.LinkedList<String>();\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.10")
                .run(ConvertToDiamondBulkHint.class)
                .assertWarnings();
    }

    static {
        TestCompilerSettings.commandLine = "-XDfind=diamond";
    }
}