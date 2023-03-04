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

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author rtaneja
 */
public class ConvertVarToExplicitTypeTest {
    private static final String VAR_CONV_DESC = "Convert var to explicit type";//NOI18N
    private static final String VAR_CONV_WARNING = "hint:" + VAR_CONV_DESC; //NOI18N

    @Test
    public void testConvertVartoIntType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        @Deprecated /*some var*/final /*s*/var var = 10;//s\n"                        
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("3:8-3:56:"+ VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        @Deprecated /*some var*/final /*s*/int var = 10;//s\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testConvertVarToString() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var str = \"Hello\";\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("3:8-3:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        String str = \"Hello\";\n"
                        + "    }\n"
                        + "}\n");
    }
    
    @Test
    public void testConvertVartoIntTypeInEnhancedForLoop() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        int[] offInt = {1, 2, 3};\n"
                        + "        for (var x : offInt)^ {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("4:8-4:30:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        int[] offInt = {1, 2, 3};\n"
                        + "        for (int x : offInt) {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }
     @Test
    public void testConvertVartoStringTypeInEnhancedForLoop() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (var x : offStr)^ {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("7:8-7:30:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (String x : offStr) {\n"
                        + "            \n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }
    
    @Test
    public void testConvertVartoStringTypeInEnhancedForLoopWithVarDeclaration() throws Exception {
        HintTest.create().setCaretMarker('^')
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (var x : offStr)^ {\n"
                        + "            var y = 10;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("7:8-7:30:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        List<String> offStr = new ArrayList<>();\n"
                        + "        offStr.add(\"a\");\n"
                        + "        for (String x : offStr) {\n"
                        + "            var y = 10;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testVartoHashMap() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    {\n"
                        + "        final var map = new HashMap<String, String>();\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("4:8-4:54:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.HashMap;\n"
                        + "public class Test {\n"
                        + "    {\n"
                        + "        final HashMap<String, String> map = new HashMap<String, String>();\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testNoVarHintForAnonymousObjType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1() {\n"
                        + "        var r = new Runnable() {\n"
                        + "            @Override\n"
                        + "            public void run() {\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);
    }

    @Test
    public void testVarToObjType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        var obj = new Object();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("3:8-3:31:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        Object obj = new Object();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testVarToArrayType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        var arr = new int[4][];\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("3:8-3:31:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m1(){\n"
                        + "        int[][] arr = new int[4][];\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testVarToIntInsideForLoop() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        for (var i = 0; i < 10; i++) {\n"
                        + "            i = i + 2;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("3:13-3:22:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "public class Test {\n"
                        + "    void m2() {\n"
                        + "        for (int i = 0; i < 10; i++) {\n"
                        + "            i = i + 2;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testNoHintForExplicitType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "void m1(){\n"
                        + "    int k = 20;\n"
                        + "}\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);
    }
    
    @Test
    public void testVarToMethodRetType1() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public void m() {\n"
                        + "        var obj = t();\n"
                        + "    }\n"
                        + "    Object t()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("4:8-4:22:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public void m() {\n"
                        + "        Object obj = t();\n"
                        + "    }\n"
                        + "    Object t()\n"
                        + "    {\n"
                        + "        return new ArrayList<String>();\n"
                        + "    }\n"
                        + "}");
    }

    @Test
    public void testVarToMethodRetType2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.Collections;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        var list = Collections.unmodifiableList(new ArrayList<String>());\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("6:8-6:73:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.Collections;\n"
                        + "import java.util.List;\n"
                        + "import java.util.ArrayList;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        List<String> list = Collections.unmodifiableList(new ArrayList<String>());\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testNoVarHintForIntersectionType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        var v = get();\n"
                        + "    }\n"
                        + "    <Z extends Runnable & CharSequence> Z get() { return null; }\n"
                        + "}\n")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .assertNotContainsWarnings(VAR_CONV_DESC);
    }
    
    @Test
    public void testVarToGenericWildCardType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        List<? extends String> ll = null;\n" 
                        + "        var l = ll.get(0);\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("5:8-5:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        List<? extends String> ll = null;\n" 
                        + "        String l = ll.get(0);\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testVarToGenericWildCardType2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        List<?> ll = null;\n" 
                        + "        var l = ll.get(0);\n"
                        + "    }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("5:8-5:26:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        List<?> ll = null;\n" 
                        + "        Object l = ll.get(0);\n"
                        + "    }\n"
                        + "}");
    }
    
    @Test
    public void testVarToGenericType2() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        var l = listOf(\"\");\n"
                        + "    }\n"
                        + "    <Z> List<Z> listOf(Z z) { return null; }\n"
                        + "}")
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .findWarning("4:8-4:27:" + VAR_CONV_WARNING)
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    void m() {\n"
                        + "        List<String> l = listOf(\"\");\n"
                        + "    }\n"
                        + "    <Z> List<Z> listOf(Z z) { return null; }\n"
                        + "}");
    } 
    
    @Test
    public void testNoVarHintForAnonymousType() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "void v() {\n"
                        + "  var v = get(new Object(){});\n" 
                        + "}\n"
                        + "\n" 
                        + "<Z> Z get(Z z) {\n" 
                        + "  return z; \n"
                        + "}"
                        + "}")                        
                .sourceLevel("1.10")
                .run(ConvertVarToExplicitType.class)
                .assertNotContainsWarnings(VAR_CONV_WARNING);
    }
}
