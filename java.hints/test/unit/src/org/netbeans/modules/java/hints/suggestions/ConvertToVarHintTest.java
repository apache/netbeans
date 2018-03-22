/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.suggestions;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author arusinha
 */
public class ConvertToVarHintTest {

    private static final String VAR_CONV_DESC = "Explict type can be replaced with 'var'"; //NOI18N
    private static final String VAR_CONV_WARNING = "verifier:" + VAR_CONV_DESC; //NOI18N

    @Test
    public void testIntLiteralRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final int i = 10^;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:18-3:19:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final var i = 10;\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testStringLiteralRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        String str = \"Hello\"^;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:15-3:18:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var str = \"Hello\";\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testLocalRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    {\n"
                        + "        final HashMap<String,String> map = new HashMap<String,String>()^;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:37-4:40:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    {\n"
                        + "        final var map = new HashMap<String,String>();\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testLambdaExprRefToVar() throws Exception {

        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        Runnable r = () ->^ {\n"
                        + "        };\n"
                        + "        r.run();\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:21-3:28:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        var r = (Runnable) () -> {\n"
                        + "        };\n"
                        + "        r.run();\n"
                        + "    }\n"
                        + "}\n");

    }

    @Test
    public void testLambdaExpr2RefToVar() throws Exception {

        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String javalatte[]) {\n"
                        + "        TwoArgInterface plusOperation = (a, b) -> a + b^;\n"
                        + "        System.out.println(\"Sum of 10,34 : \" + plusOperation.operation(10, 34));\n"
                        + "    }\n"
                        + "}\n"
                        + "interface TwoArgInterface {\n"
                        + "    public int operation(int a, int b);\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:40-3:55:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String javalatte[]) {\n"
                        + "        var plusOperation = (TwoArgInterface) (a, b) -> a + b;\n"
                        + "        System.out.println(\"Sum of 10,34 : \" + plusOperation.operation(10, 34));\n"
                        + "    }\n"
                        + "}\n"
                        + "interface TwoArgInterface {\n"
                        + "    public int operation(int a, int b);\n"
                        + "}");

    }

    @Test
    public void testAnonymusObjRefToVar() throws Exception {

        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        Runnable r = new Runnable()^ {\n"
                        + "            @Override\n"
                        + "            public void run() {\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:21-3:37:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var r = new Runnable() {\n"
                        + "            @Override\n"
                        + "            public void run() {\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n");

    }

    @Test
    public void testObjRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "Obj^ect obj = new Object();\n"
                        + "}\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:7-3:10:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "var obj = new Object();\n"
                        + "}\n"
                        + "}\n");
    }

    @Test
    public void testarrayRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "int[][] arr = new int[4][]^;\n"
                        + "}\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:14-3:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "    var arr = new int[4][];\n"
                        + "}\n"
                        + "}\n");
    }

    @Test
    public void testDiamondInterfaceRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final HashMap<String, String> map = new HashMap<>^();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:44-4:59:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final var map = new HashMap<String, String>();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testLiteralInitToVarRefInsideLoop() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        for (int i = 0^; i < 10; i++) {\n"
                        + "            i = i + 2;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:17-3:18:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        for (var i = 0; i < 10; i++) {\n"
                        + "            i = i + 2;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testHintForVarType() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "    var arr = 20^;\n"
                        + "}\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);
    }

    @Test
    public void testSuperTypeRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> list1 = new ArrayList<String>^();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

    @Test
    public void testSupportedSourceLevel() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final int i = 10^;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.9")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

}
