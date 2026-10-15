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
package org.netbeans.modules.java.hints.bugs;

import org.junit.Assume;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class UnusedAssignmentOrBranchTest extends NbTestCase {

    public UnusedAssignmentOrBranchTest(String name) {
        super(name);
    }

    public void testSimpleUnusedInitializer() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         int i = 0;\n" +
                       "         i = 1;\n" +
                       "         System.err.println(i);\n" +
                       "     }\n" +
                       "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings("3:17-3:18:verifier:LBL_UNUSED_ASSIGNMENT_LABEL");
    }

    public void testSimpleUnusedAssignment() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         int i;\n" +
                       "         i = 0;\n" +
                       "         i = 1;\n" +
                       "         System.err.println(i);\n" +
                       "     }\n" +
                       "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings("4:13-4:14:verifier:LBL_UNUSED_ASSIGNMENT_LABEL");
    }

    public void testNoHighlightingForUnusedVariables() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         int i = 0;\n" +
                       "         i = 1;\n" +
                       "     }\n" +
                       "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings();
    }

    public void testAttributeValues() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"a\")\n" +
                       "public class Test {\n" +
                       "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings();
    }
    
    /**
     * Checks that unused var value produced by compound assignment is detected.
     * Checks that the operation is corrected. Since the expression contains
     * method call, it may have side effects so the order of operands should be reversed
     * from the original
     * 
     * @throws Exception 
     */
    public void testCompoundAssignment267508Boolean() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                        "public class Test {\n"
                        + "    public static void bad(String... args) {\n"
                        + "        boolean done = false;\n"
                        + "        long counting = 0;\n"
                        + "        while (!(done |= Thread.currentThread().isInterrupted())) {\n"
                        + "            counting++;\n"
                        + "            done = counting > 1000000000;\n"
                        + "        }\n"
                        + "    }"
                        + "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .findWarning("5:17-5:63:verifier:LBL_UnusedCompoundAssignmentLabel").
                applyFix().assertOutput(
                        "package test;\n" +
                        "public class Test {\n"
                        + "    public static void bad(String... args) {\n"
                        + "        boolean done = false;\n"
                        + "        long counting = 0;\n"
                        + "        while (!(Thread.currentThread().isInterrupted() || done)) {\n"
                        + "            counting++;\n"
                        + "            done = counting > 1000000000;\n"
                        + "        }\n"
                        + "    }"
                        + "}\n"                
                );
    }

    /**
     * Checks that unused var value produced by compound assignment is detected.
     * Checks that the operation is corrected, in the original order of variable/expression
     * @throws Exception 
     */
    public void testCompoundAssignment267508Integer() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                        "public class Test {\n"
                        + "    public static void bad(String... args) {\n"
                        + "        long done = 0;\n"
                        + "        long counting = 0;\n"
                        + "        while ((done |= (int)Math.random()) == 0) {\n"
                        + "            counting++;\n"
                        + "            done = counting - 1000000000;\n"
                        + "        }\n"
                        + "    }"
                        + "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .findWarning("5:16-5:42:verifier:LBL_UnusedCompoundAssignmentLabel").
                applyFix().assertOutput(
                        "package test;\n" +
                        "public class Test {\n"
                        + "    public static void bad(String... args) {\n"
                        + "        long done = 0;\n"
                        + "        long counting = 0;\n"
                        + "        while ((done | (int)Math.random()) == 0) {\n"
                        + "            counting++;\n"
                        + "            done = counting - 1000000000;\n"
                        + "        }\n"
                        + "    }"
                        + "}\n"                
                );
    }

    public void testCompoundAssignment267508Negative() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                        "public class Test {\n"
                        + "    public static void bad(String... args) {\n"
                        + "        boolean done = false;\n"
                        + "        long counting = 0;\n"
                        + "        while (!(done |= Thread.currentThread().isInterrupted())) {\n"
                        + "            counting++;\n"
                        + "            // done = counting > 1_000_000_000;\n"
                        + "        }\n"
                        + "    }"
                        + "}\n")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings();
    }

    // #271736 - Wrong "not used" hint
    public void testImplicitlyStaticFieldWithInitializer() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "\n" +
                       "import java.util.function.Consumer;\n" +
                       "\n" +
                       "public interface Test {\n" +
                       "    Consumer<CharSequence> CONSUMER = new Consumer<CharSequence>() {\n" +
                       "        @Override\n" +
                       "        public void accept(CharSequence cs) {\n" +
                       "            final String str = (String) cs;\n" +
                       "            System.out.println(str.length());\n" +
                       "        }\n" +
                       "    };\n" +
                       "}" +
                       "\n" +
                       "enum SomeEnum {\n" +
                       "    ALT() {\n" +
                       "        public void doSomething(CharSequence cs) {\n" +
                       "            final String str = (String) cs;\n" +
                       "            System.out.println(str.length());\n" +
                       "        }\n" +
                       "    };\n" +
                       "\n" +
                       "    private SomeEnum() { }\n" +
                       "}")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings();
    }

    public void testRecordCompactConstructor() throws Exception {
        Assume.assumeTrue(isRecordClassPresent());

        HintTest
                .create()
                .input("package test;\n" +
                       "\n" +
                       "public record Test(int i, int j) {\n" +
                       "    public Test {\n" +
                       "        i = -i;\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("21")
                .run(UnusedAssignmentOrBranch.class)
                .assertWarnings();
    }

    private boolean isRecordClassPresent() {
        try {
            Class.forName("java.lang.Record");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
