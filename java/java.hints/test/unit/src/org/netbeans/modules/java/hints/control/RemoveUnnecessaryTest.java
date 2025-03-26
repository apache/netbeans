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
package org.netbeans.modules.java.hints.control;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class RemoveUnnecessaryTest extends NbTestCase {

    public RemoveUnnecessaryTest(String name) {
        super(name);
    }

    public void testReturnSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn() {\n" +
                       "        return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("3:8-3:16:verifier:" + Bundle.ERR_UnnecessaryReturnStatement())
                .applyFix(Bundle.FIX_UnnecessaryReturnStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testReturn() {\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testReturnIfNoBlock() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(boolean b) {\n" +
                       "        if (b) return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("3:15-3:23:verifier:" + Bundle.ERR_UnnecessaryReturnStatement())
                .applyFix(Bundle.FIX_UnnecessaryReturnStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testReturn(boolean b) {\n" +
                              "        if (b) { }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testReturnNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(boolean b) {\n" +
                       "        if (b) { return ; }\n" +
                       "        System.err.println();\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "                    System.err.println();\n" +
                       "                    break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return 1; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "        return 0;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNegCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { return ; }\n" +
                       "            case 1: { System.err.println(1); break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnSwitchRemove1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; } break;\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } break; };\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: { ; break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnLastCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnNPE200462a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnNPE200462b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnInLoop201393() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "            System.err.println(1);\n" +
                       "            if (i == 3) return ;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNegFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            System.out.println(\"Do I ever get printed?\");\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void TODOtestPosFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            if (args.length == 0) { return ; }\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("<missing>");
    }

    public void testReturnIgnoreExtraBreak222422() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        if (true) return ;\n" +
                       "        break;\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testLambda227899a() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.Comparator;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        Comparator<String> c = (String s1, String s2) -> {\n" +
                       "               return s1.compareToIgnoreCase(s2);\n" +
                       "            };\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testLambda227899b() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        Runnable r = () -> {\n" +
                       "               return ;\n" +
                       "            };\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:15-4:23:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testContinueSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "            continue;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:21:verifier:" + Bundle.ERR_UnnecessaryContinueStatement())
                .applyFix(Bundle.FIX_UnnecessaryContinueStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        while (i-- > 0) {\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testContinueToOutter() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i, int j) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            while (j-- > 0) {\n" +
                       "                continue OUTER;\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testContinueWithLabel() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            continue OUTER;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:20:verifier:" + Bundle.ERR_UnnecessaryContinueStatementLabel())
                .applyFix(Bundle.FIX_UnnecessaryContinueStatementLabel())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        OUTER: while (i-- > 0) {\n" +
                              "            continue;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testBreakWithLabel() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            break OUTER;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:17:verifier:" + Bundle.ERR_UnnecessaryBreakStatementLabel())
                .applyFix(Bundle.FIX_UnnecessaryBreakStatementLabel())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        OUTER: while (i-- > 0) {\n" +
                              "            break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testBreakWithLabelInSwitch() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            switch (i) {\n" +
                       "                case 0: break OUTER;\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testRuleSwitch1() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       public class Test {
                           public void testContinue(int i) {
                               switch (i) {
                                   case 0 -> { return ; }
                                   case 1 -> { return ; }
                                   default -> { }
                               }
                               System.err.println("not end");
                           }
                       }
                       """)
                .sourceLevel("17")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testRuleSwitch2() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       public class Test {
                           public void testContinue(int i) {
                               switch (i) {
                                   case 0 -> { return ; }
                                   case 1 -> { return ; }
                                   default -> { }
                               }
                           }
                       }
                       """)
                .sourceLevel("17")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:24-4:32:verifier:Unnecessary return statement",
                                "5:24-5:32:verifier:Unnecessary return statement");
    }

    public void testRuleSwitchExpression() throws Exception {
        HintTest.create()
                .input("""
                       package test;
                       public class Test {
                           public int testContinue(int i) {
                               return switch (i) {
                                   case 0 -> { return 0; } //illegal
                                   case 1 -> { return 0; } //illegal
                                   default -> -1;
                               };
                           }
                       }
                       """,
                       false)
                .sourceLevel("17")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
}
