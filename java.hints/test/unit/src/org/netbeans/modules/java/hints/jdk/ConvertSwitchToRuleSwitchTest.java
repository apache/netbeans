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

import javax.lang.model.SourceVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**XXX: disable when not running on JDK 12+
 *
 * @author lahvac
 */
public class ConvertSwitchToRuleSwitchTest extends NbTestCase {
    
    public ConvertSwitchToRuleSwitchTest(String name) {
        super(name);
    }
    
    public void testSwitch2RuleSwitch() throws Exception {
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }
    
    public void testMissingLastBreak() throws Exception {
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
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
        HintTest.create()
                .input("package test;" +
                       "public class Test {\n" +
                       "     private void test(int p) {\n" +
                       "         String result;\n" +
                       "         switch (p) {\n" +
                       "             case 0: result = \"1\"; break;\n" +
                       "             default: result = \"d\"; break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;" +
                              "public class Test {\n" +
                              "     private void test(int p) {\n" +
                              "         String result;\n" +
                              "         switch (p) {\n" +
                              "             case 0 -> result = \"1\";\n" +
                              "             default -> result = \"d\";\n" +
                              "         }\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testVariables1() throws Exception {
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConverSwitchToRuleSwitch())
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

    public void testFallThroughDefault2() throws Exception {
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
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToRuleSwitch.class)
                .assertWarnings();
    }

}
