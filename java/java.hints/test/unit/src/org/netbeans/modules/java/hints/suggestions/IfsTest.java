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

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

public class IfsTest {

    @Test
    public void testComments() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f\n" +
                       "        // A\n" +
                       "        (args[0].isEmpty())\n" +
                       "        // B\n" +
                       "        { //1\n" +
                       "            //2\n" +
                       "            System.err.println(\"1\");\n" +
                       "            //3\n" +
                       "        } \n" +
                       "        // C\n" +
                       "        else\n" +
                       "        // D\n" +
                       "        {//4\n" +
                       "            //5\n" +
                       "            System.err.println(\"2\");\n" +
                       "            //6\n" +
                       "        } //E\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if\n" +
                              "        // A\n" +
                              "        (!args[0].isEmpty())\n" +
                              "        // D\n" +
                              "        {//4\n" +
                              "            //5\n" +
                              "            System.err.println(\"2\");\n" +
                              "            //6\n" +
                              "        } //E\n" +
                              "        // C\n" +
                              "        else\n" +
                              "        // B\n" +
                              "        { //1\n" +
                              "            //2\n" +
                              "            System.err.println(\"1\");\n" +
                              "            //3\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testCaretPosition() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        if (args[0].isEmpty()) |{\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }
    
    @Test
    public void testOptimizeNeg() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (!args[0].isEmpty()) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args[0].isEmpty()) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeNegParenthesized() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (!(args.length == 0)) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeEquals() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length == 0) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length != 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeNotEquals() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length != 0) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testTrue() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (true) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (false) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    @Test
    public void testFalse() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (false) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (true) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testDeMorganAnd() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length != 0 && true) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0 || false) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testDeMorganOr() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length != 0 || false) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0 && true) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testLess() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length < 5) {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length >= 5) {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testLessEq() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length <= 5) {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length > 5) {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testGreater() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length > 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length <= 5) {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testGreaterEq() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length >= 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length < 5) {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testWithoutElse225913() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length >= 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length < 5) {\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testNegInstanceof228864() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(Object o) {\n" +
                       "        i|f (o instanceof String) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(Object o) {\n" +
                              "        if (!(o instanceof String)) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testJoinIfs1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("5:12-5:12:verifier:" + Bundle.ERR_JoinElseIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(int i) {\n" +
                              "        if (i == 0) {\n" +
                              "            System.err.println(\"0\");\n" +
                              "        } else if (i == 1) {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testJoinIfs2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "            System.err.println(\"extra\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }

    @Test
    public void testJoinIfs3() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {^\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }
    
    @Test
    public void testToIfOr1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"0\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("5:12-5:12:verifier:" + Bundle.ERR_ToOrIf())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0 || i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testToIfOr2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        i^f (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else if (i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_ToOrIf())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0 || i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else {\n" +
                                      "            System.err.println(\"1\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testSplitOrIf1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0 |^| i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:20-3:20:verifier:" + Bundle.ERR_splitIfCondition())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else if (i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testSplitOrIf2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0 |^| i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:20-3:20:verifier:" + Bundle.ERR_splitIfCondition())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else if (i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else {\n" +
                                      "            System.err.println(\"1\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    public void testMergeIfs1() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        i|f (i == 0) {\n" +
                       "            if (j == 0) {\n" +
                       "                System.err.println(1);\n" +
                       "            }" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:9-3:9:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0 && j == 0) {\n" +
                              "            System.err.println(1);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testMergeIfs2() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        i|f (i == 0 || i == 1) {\n" +
                       "            if (j == 0 || j == 1) {\n" +
                       "                System.err.println(1);\n" +
                       "            }" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:9-3:9:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if ((i == 0 || i == 1) && (j == 0 || j == 1)) {\n" +
                              "            System.err.println(1);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testSplitIf1() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if (i =|= 0 && j == 0) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:15-3:15:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0) {\n" +
                              "            if (j == 0) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitIf2() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if ((i =|= 0 || i == 1) && (j == 0 || j == 1)) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:16:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0 || i == 1) {\n" +
                              "            if (j == 0 || j == 1) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitIf3() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if (i == 0 && j =|= 0) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:25-3:25:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0) {\n" +
                              "            if (j == 0) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
}
