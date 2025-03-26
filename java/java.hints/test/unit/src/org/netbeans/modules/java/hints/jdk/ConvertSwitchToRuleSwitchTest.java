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
package org.netbeans.modules.java.hints.jdk;

import com.sun.tools.javac.tree.JCTree;
import javax.lang.model.SourceVersion;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class ConvertSwitchToRuleSwitchTest extends NbTestCase {
    
    public ConvertSwitchToRuleSwitchTest(String name) {
        super(name);
    }
    
    public static boolean isJDK14(){
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip test
            return false;
        }
        return true;
    }

    public void testSwitch2RuleSwitch() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: if (true) result = \"2\"; break;\n" +
                       "             case 3: System.err.println(3); result = \"3\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 1 -> result = \"1\";\n" +
                              "             case 2 -> { if (true) result = \"2\"; }\n" +
                              "             case 3 -> { System.err.println(3); result = \"3\"; }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testLastNotBreak() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: if (p == 1) { result = \"1\"; break; } else { result = \"2\"; break; }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 1 -> { if (p == 1) { result = \"1\"; } else { result = \"2\"; } }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testMultipleCases() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: result = \"2\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0, 1 -> result = \"1\";\n" +
                              "             case 2 -> result = \"2\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testFallThrough() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: System.err.println(0);\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }
    
    public void testMissingLastBreak() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: result = \"2\";\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0, 1 -> result = \"1\";\n" +
                              "             case 2 -> result = \"2\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testDefault() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: result = \"1\"; break;\n" +
                       "             default: result = \"d\"; System.out.println(result); break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> result = \"1\";\n" +
                              "             default -> {\n" +
                              "                 result = \"d\"; System.out.println(result);\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testVariables1() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "             default:\n" +
                       "                 i = 0;\n" +
                       "                 System.err.println(i);\n" +
                       "                 System.err.println(j = 15);\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 int i = 0;\n" +
                              "                 int j = 0;\n" +
                              "             }\n" +
                              "             default -> {\n" +
                              "                 int i = 0;\n" +
                              "                 System.err.println(i);\n" +
                              "                 int j;\n" +
                              "                 System.err.println(j = 15);\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testFallThroughDefault1() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             default:\n" +
                       "             case 0:\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testFallThroughDefault2() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "             default:\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testTrailingEmptyCase() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "             default:\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 int i = 0;\n" +
                              "                 int j = 0;\n" +
                              "             }\n" +
                              "             default -> { }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testNeedsPreview() throws Exception {
        if(ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0:\n" +
                       "                 int i = 0;\n" +
                       "                 int j = 0;\n" +
                       "                 break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testBreakInside1() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (true) break;\n" +
                       "             case 1: INNER: while (true) break INNER;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testBreakInside2() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (true) break;\n" +
                       "                     break;\n" +
                       "             case 1: INNER: while (true) break INNER;\n" +
                       "                     break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("4:9-4:15:verifier:Convert switch to rule switch")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> {\n" +
                              "                 while (true) break;\n" +
                              "             }\n" +
                              "             case 1 -> {\n" +
                              "                 INNER: while (true) break INNER;\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}");
    }

    public void testContinueInside1() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: while (p++ < 12) continue;\n" +
                       "             case 1: INNER: while (p++ < 12) continue INNER;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testContinueInside2() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         OUTER: while (p-- > 0) {\n" +
                       "             switch (p) {\n" +
                       "                 case 0: while (p++ < 12) continue OUTER;\n" +
                       "                         break;\n" +
                       "                 case 1: INNER: while (p++ < 12) continue OUTER;\n" +
                       "                         break;\n" +
                       "             }\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("5:13-5:19:verifier:Convert switch to rule switch")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         OUTER: while (p-- > 0) {\n" +
                              "             switch (p) {\n" +
                              "                 case 0 -> {\n" +
                              "                     while (p++ < 12) continue OUTER;\n" +
                              "                 }\n" +
                              "                 case 1 -> {\n" +
                              "                     INNER: while (p++ < 12) continue OUTER;\n" +
                              "                 }\n" +
                              "             }\n" +
                              "         }\n" +
                              "     }\n" +
                              "}");
    }

    //Test cases for switch expression

    public void testSwitch2SwitchExpression() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result = \"1\"; break;\n" +
                       "             case 2: result = \"2\"; break;\n" +
                       "             case 3: result = \"3\"; break;\n" +
                       "             default: result = \"default\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         result = switch (p) {\n" +
                              "             case 1 -> \"1\";\n" +
                              "             case 2 -> \"2\";\n" +
                              "             case 3 -> \"3\";\n" +
                              "             default -> \"default\";\n" +
                              "         };\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionMultiCase() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "    private void test(int p) {\n" +
                       "        String result;\n" +
                       "        switch (p) {\n" +
                       "            case 1:\n" +
                       "            case 2: result = \"2\"; break;\n" +
                       "            case 3: result = \"3\"; break;\n" +
                       "            default: result = \"default\"; break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:8-3:14:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         result = switch (p) {\n" +
                              "             case 1, 2 -> \"2\";\n" +
                              "             case 3 -> \"3\";\n" +
                              "             default -> \"default\";\n" +
                              "         };\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionMultiCase2() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "    private void test(int p) {\n" +
                       "        String result;\n" +
                       "        switch (p) {\n" +
                       "            case 1:\n" +
                       "            case 2: result = \"2\"; break;\n" +
                       "            case 3: result = \"3\"; break;\n" +
                       "            default: result = \"default\";\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:8-3:14:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "    private void test(int p) {\n" +
                              "        String result;\n" +
                              "        result = switch (p) {\n" +
                              "            case 1, 2 -> \"2\";\n" +
                              "            case 3 -> \"3\";\n" +
                              "            default -> \"default\";\n" +
                              "        };\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionOnlyDefault() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "    private void test(int p) {\n" +
                       "        String result;\n" +
                       "        switch (p) {\n" +
                       "            default: result = \"default\";\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:8-3:14:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "    private void test(int p) {\n" +
                              "        String result;\n" +
                              "        result = switch (p) {\n" +
                              "            default -> \"default\";\n" +
                              "        };\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionNestedInnerSwitchExpression() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         int i = 10;\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result = switch (i) {\n" +
                       "                         case 1 -> \"one\"; \n" +
                       "                         default -> \"Inner default\"; \n" +
                       "                     }; break;\n" +
                       "             case 2: result = \"2\"; break;\n" +
                       "             default: result = \"default\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("4:9-4:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         int i = 10;\n" +
                              "         String result;\n" +
                              "         result = switch (p) {\n" +
                              "             case 1 -> switch (i) {\n" +
                              "                 case 1 -> \"one\";\n" +
                              "                 default -> \"Inner default\";\n" +
                              "         };\n" +
                              "             case 2 -> \"2\";\n" +
                              "             default -> \"default\";\n" +
                              "         };\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionReturnValue() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private String test(int p) {\n" +
                       "         switch (p) {\n" +
                       "             case 1: return \"1\"; \n" +
                       "             default: return \"default\"; \n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("2:9-2:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private String test(int p) {\n" +
                              "         return switch (p) {\n" +
                              "             case 1 -> \"1\";\n" +
                              "             default -> \"default\";\n" +
                              "         };\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionTypeCast() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p, Object o1, Object o2) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result =  (String)o1; break;\n" +
                       "             default: result = (String)o2; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p, Object o1, Object o2) {\n" +
                              "         String result;\n" +
                              "         result = (String) (switch (p) {\n" +
                              "             case 1 -> o1;\n" +
                              "             default -> o2;\n" +
                              "         });\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionTypeCastReturn() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private String test(int p, Object o1, Object o2) {\n" +
                       "         switch (p) {\n" +
                       "             case 1: return (String)o1;\n" +
                       "             default: return (String)o2;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("2:9-2:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private String test(int p, Object o1, Object o2) {\n" +
                              "         return (String) (switch (p) {\n" +
                              "             case 1 -> o1;\n" +
                              "             default -> o2;\n" +
                              "         });\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionNestedSwitchExpression() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p, int q, Object o1, Object o2) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 1: result =  (String)(switch(q){ \n" +
                       "                        case 2 -> o2; \n" +
                       "                        default -> o1;\n" +
                       "                        }); break; \n" +
                       "             default: result = (String)o2; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p, int q, Object o1, Object o2) {\n" +
                              "         String result;\n" +
                              "         result = (String) (switch (p) {\n" +
                              "             case 1 -> switch(q){ \n" +
                              "                        case 2 -> o2; \n" +
                              "                        default -> o1;\n" +
                              "                        };\n" +
                              "             default -> o2;\n" +
                              "         });\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionNestedOuterSwitchStatement() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         int x = 10;\n" +
                       "         switch (p) {\n" +
                       "             case 1 : \n" +
                       "                switch (x) {\n" +
                       "                    case 1 : result = \"1\"; break;\n" +
                       "                    default : result =  \"Inner Default\"; break;\n" +
                       "                }\n" +
                       "             break;\n" +
                       "             default:\n" +
                       "                result = \"d\";\n" +
                       "                break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("4:9-4:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         int x = 10;\n" +
                              "         switch (p) {\n" +
                              "             case 1 -> {\n" +
                              "                switch (x) {\n" +
                              "                    case 1 : result = \"1\"; break;\n" +
                              "                    default : result =  \"Inner Default\"; break;\n" +
                              "                 }\n" +
                              "             }\n" +
                              "             default -> result = \"d\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testSwitch2SwitchExpressionNestedInnerSwitchStatement() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         int x = 10;\n" +
                       "         switch (p) {\n" +
                       "             case 1 : \n" +
                       "                switch (x) {\n" +
                       "                    case 1 : result = \"1\"; break;\n" +
                       "                    default : result =  \"Inner Default\"; break;\n" +
                       "                }\n" +
                       "                break;\n" +
                       "             default:\n" +
                       "                result = \"d\";\n" +
                       "                break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("6:16-6:22:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         int x = 10;\n" +
                              "         switch (p) {\n" +
                              "             case 1 :\n" +
                              "             result = switch (x) {\n" +
                              "                 case 1 -> \"1\";\n" +
                              "                 default ->  \"Inner Default\";\n" +
                              "             };\n" +
                              "                 break;\n\n" +
                              "             default:\n" +
                              "                 result = \"d\";\n" +
                              "                 break;\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }
    
    public void testSwitchToRuleSwitchFormattingMultiple() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                        "public class Test {\n" +
                        "     private void test(int p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case 0:\n" +
                        "            case 1:\n" +
                        "            case 2:\n" +
                        "            case 3: result=\"a\"; break;\n" +
                        "            default: System.err.println(\"No.\"); break;" +
                        "         }\n" +
                        "     }\n" +
                        "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;" +
                        "public class Test {\n" +
                        "     private void test(int p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case 0, 1, 2, 3 -> result=\"a\";\n" +
                        "            default -> System.err.println(\"No.\");\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n");
    }
    
    public void testSwitchToRuleSwitchFormattingSimple() throws Exception {
        if(!ConvertSwitchToRuleSwitchTest.isJDK14())
            return;
        HintTest.create()
                .input("package test;" +
                        "public class Test {\n" +
                        "     private void test(int p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case 0: result = \"a\"; break;\n" +
                        "            default: System.err.println(\"No.\"); break;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n")
                .sourceLevel(SourceVersion.latest().ordinal())
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;" +
                        "public class Test {\n" +
                        "     private void test(int p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case 0 -> result = \"a\";\n" +
                        "            default -> System.err.println(\"No.\");\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n");
    }
    
        public void testSwitchToRuleSwitchBindingPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip tests
            return;
        }
        HintTest.create()
                .input("package test;" +
                        "public class Test {\n" +
                        "     private void test(Object p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case Integer i : result = \"a\"; break;\n" +
                        "            default : System.err.println(\"No.\"); break;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n")
                .sourceLevel(21)
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;" +
                        "public class Test {\n" +
                        "     private void test(Object p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case Integer i -> result = \"a\";\n" +
                        "            default -> System.err.println(\"No.\");\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n");
    }
    
    public void testSwitchToRuleSwitchGuardedPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip tests
            return;
        }
        HintTest.create()
                .input("package test;" +
                        "public class Test {\n" +
                        "     private void test(Object p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case Integer i when (i > 10): result = \"a\"; break;\n" +
                        "            default: System.err.println(\"No.\"); break;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n")
                .sourceLevel(21)
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;" +
                        "public class Test {\n" +
                        "     private void test(Object p) {\n" +
                        "         String result;\n" +
                        "         switch (p) {\n" +
                        "            case Integer i when (i > 10) -> result = \"a\";\n" +
                        "            default -> System.err.println(\"No.\");\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n");
    }
    
    public void testSwitchExpressionGuardedPattern() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip tests
            return;
        }
        HintTest.create()
                .input("package test;"
                        + "class Test {\n"
                        + "    public String test(Object p, Object o1, Object o2) {\n"
                        + "        switch (p) {\n"
                        + "            case Integer i  when (i > 10):\n"
                        + "               return (String) o1;\n"
                        + "            default :\n"
                        + "                return (String) o2;\n"
                        + "        }\n"
                        + "    }\n"
                        + "}")
                .sourceLevel(21)
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("2:8-2:14:verifier:" + Bundle.ERR_ConvertSwitchToSwitchExpression())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;"
                            + "class Test {\n"
                            + "    public String test(Object p, Object o1, Object o2) {\n"
                            + "        return (String) (switch (p) {\n"
                            + "            case Integer i when (i > 10) -> o1;\n"
                            + "            default -> o2;\n"
                            + "        });\n"
                            + "    }\n"
                            + "}");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        try {
            Class.forName("com.sun.source.tree.CaseTree$CaseKind", false, JCTree.class.getClassLoader());
            suite.addTestSuite(ConvertSwitchToRuleSwitchTest.class);
        } catch (ClassNotFoundException ex) {
            //OK
            suite.addTest(new ConvertSwitchToRuleSwitchTest("noop"));
        }
        return suite;
    }

    public void noop() {}
}
