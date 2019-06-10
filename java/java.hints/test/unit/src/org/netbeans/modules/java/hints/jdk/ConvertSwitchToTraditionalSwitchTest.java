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


public class ConvertSwitchToTraditionalSwitchTest extends NbTestCase {

    public ConvertSwitchToTraditionalSwitchTest(String name) {
        super(name);
    }

    public void testSwitch2RuleSwitch() throws Exception {
        HintTest.create()
                .input("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> System.out.println(\"one\"); \n"
                        + "             case 2 -> System.out.println(\"two\"); \n"
                        + "             default -> System.out.println(\"default\");\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToTraditionalSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToTraditionalSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 : System.out.println(\"one\"); break; \n"
                        + "             case 2 : System.out.println(\"two\"); break; \n"
                        + "             default : System.out.println(\"default\"); break; \n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n");
    }

    public void testRuleSwitch2TraditionalSwitchMultiLevel() throws Exception {
        HintTest.create()
                .input("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 0,1 -> System.out.println(\"one\"); \n"
                        + "             case 2 -> System.out.println(\"two\"); \n"
                        + "             default -> System.out.println(\"default\");\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToTraditionalSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToTraditionalSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 0 : \n"
                        + "             case 1 : System.out.println(\"one\"); break; \n"
                        + "             case 2 : System.out.println(\"two\"); break; \n"
                        + "             default : System.out.println(\"default\"); break; \n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n");
    }

    public void testRuleSwitch2TraditionalSwitchBlockCase() throws Exception {
        HintTest.create()
                .input("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> result = \"1\";\n"
                        + "             case 2 -> { if (true) result = \"2\"; }\n"
                        + "             case 3 -> { System.err.println(3); result = \"3\"; }\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToTraditionalSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToTraditionalSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1: result = \"1\"; break;\n"
                        + "             case 2: if (true) result = \"2\"; break;\n"
                        + "             case 3: System.err.println(3); result = \"3\"; break;\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n");
    }

    public void testRuleSwitch2TraditionalSwitchWithBreak() throws Exception {
        HintTest.create()
                .input("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1 -> { if (p == 1) { result = \"1\"; } else { result = \"2\"; } }\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToTraditionalSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToTraditionalSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 1: if (p == 1) { result = \"1\"; } else { result = \"2\"; } break; \n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n");
    }

    public void testRuleSwitch2TraditionalDefault() throws Exception {
        HintTest.create()
                .input("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 0 -> result = \"1\";\n"
                        + "             default -> {\n"
                        + "                 result = \"d\"; System.out.println(result);\n"
                        + "             }\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n")
                .sourceLevel(SourceVersion.latest().name())
                .options("--enable-preview")
                .run(ConvertSwitchToTraditionalSwitch.class)
                .findWarning("3:9-3:15:verifier:" + Bundle.ERR_ConvertSwitchToTraditionalSwitch())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;"
                        + "public class Test {\n"
                        + "     private void test(int p) {\n"
                        + "         String result;\n"
                        + "         switch (p) {\n"
                        + "             case 0: result = \"1\"; break;\n"
                        + "             default: result = \"d\"; System.out.println(result); break;\n"
                        + "         }\n"
                        + "     }\n"
                        + "}\n");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        try {
            Class.forName("com.sun.source.tree.CaseTree$CaseKind", false, JCTree.class.getClassLoader());
            suite.addTestSuite(ConvertSwitchToTraditionalSwitchTest.class);
        } catch (ClassNotFoundException ex) {
            //OK
            suite.addTest(new ConvertSwitchToTraditionalSwitchTest("noop"));
        }
        return suite;
    }

    public void noop() {
    }
}
