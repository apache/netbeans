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

import org.netbeans.modules.java.hints.jdk.ConvertToVarHint;
import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author arusinha
 */
public class ConvertToVarHintTest {

    private static final String VAR_CONV_DESC = "Explict type can be replaced with 'var'"; //NOI18N
    private static final String VAR_CONV_WARNING = "hint:" + VAR_CONV_DESC; //NOI18N

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
                .findWarning("3:8-3:25:" + VAR_CONV_WARNING)
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
                .findWarning("3:8-3:29:" + VAR_CONV_WARNING)
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
                .findWarning("4:8-4:72:" + VAR_CONV_WARNING)
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
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }
    
    @Test
    public void testMethodRefToVar() throws Exception {

        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.function.Consumer;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "       final Consumer<String> println = System.out::println^;\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }
    
    @Test
    public void testArrayInitializerVar() throws Exception {

        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        int[] i = {1,2,3};\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }
    
    @Test
    public void testConvertIntToVarTypeInEnhancedForLoop() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        int[] offInt = {1, 2, 3};\n"
                        + "        for (int x : offInt)^ {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:8-4:30:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        int[] offInt = {1, 2, 3};\n"
                        + "        for (var x : offInt) {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }
     @Test
    public void testConvertStringtoVarTypeInEnhancedForLoop() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (String x : offStr)^ {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("7:8-7:33:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (var x : offStr) {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }
    
    @Test
    public void testWrongMatchForVarEnhancedForLoop() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (Object x : offStr)^ {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);
    }
    
    @Test
    public void testConvertStringtoVarTypeInEnhancedForLoopWithVarDeclaration() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (String x : offStr)^ {\n"
                        + "            var y = 10;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("7:8-7:33:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (var x : offStr) {\n"
                        + "            var y = 10;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
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
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

    @Test
    public void testObjRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        Obj^ect obj = new Object();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:8-3:34:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        var obj = new Object();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testArrayRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        int[][] arr = new int[4][]^;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:8-3:35:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        var arr = new int[4][];\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testDiamondInterfaceRefToVar() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        final HashMap<String, String> map = new HashMap<>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertContainsWarnings("4:8-4:60:"+VAR_CONV_WARNING);
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
                .findWarning("3:13-3:22:" + VAR_CONV_WARNING)
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
                        + "    var k = 20^;\n"
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

    @Test
    public void testClassMemberRefToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    int i =10 ^;\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

    @Test
    public void testMethodAssignToVar() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        Object obj = m1()^;\n"
                        + "    }\n"
                        + "    static Object m1()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:8-4:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        var obj = m1();\n"
                        + "    }\n"
                        + "    static Object m1()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testMethodAssignToVar2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        Object obj = m1()^;\n"
                        + "    }\n"
                        + "    static Object m1()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:8-4:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        var obj = m1();\n"
                        + "    }\n"
                        + "    static Object m1()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testCapturedTypeTypeParamsAssignToVar() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public void m() {\n"
                        + "        Class<? extends String> aClass = \"x\".getClass();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:8-3:56:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    public void m() {\n"
                        + "        var aClass = \"x\".getClass();\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testConvertToVarForCapturedType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<? extends String> ls = null;\n"
                        + "        String s = ls.get(0);\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("5:8-5:29:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<? extends String> ls = null;\n"
                        + "        var s = ls.get(0);\n"
                        + "    }\n"
                        + "}");
    } 
    
    @Test
    public void testConvertToVarWithDiamondOperator1() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        HashMap<String, String> list = new HashMap<>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:8-4:55:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var list = new HashMap<String, String>();\n"
                        + "    }\n"
                        + "}");
    } 
    
    @Test
    public void testConvertToVarWithDiamondOperator2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        ArrayList<java.util.LinkedList<?>> list = new ArrayList<>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("4:8-4:68:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var list = new ArrayList<java.util.LinkedList<?>>();\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testConvertToVarWithDiamondOperator3() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        java.util.HashMap<String, String> list = new java.util.HashMap<>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .findWarning("3:8-3:75:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var list = new java.util.HashMap<String, String>();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testCompoundVariableDeclStatement() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "         int i =10,j=20;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

    @Test
    public void testCompoundVariableDeclStatement2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        final int /*comment*/l =10/*comment*/,i=20/*comment*/,j=5/*comment*/;\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertToVarHint.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);

    }

    
}
