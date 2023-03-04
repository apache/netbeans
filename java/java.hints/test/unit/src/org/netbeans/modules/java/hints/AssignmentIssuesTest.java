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
package org.netbeans.modules.java.hints;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Dusan Balek
 */
public class AssignmentIssuesTest extends NbTestCase {

    public AssignmentIssuesTest(String name) {
        super(name);
    }

    @Test
    public void testAssignmentToForLoopParam() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        for (int i = 0; i < args.length; i++) {\n" +
                 "            i = 10;" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings("4:12-4:18:verifier:Assignment to for-loop parameter i");
    }

    @Test
    public void testAssignmentToForLoopParamSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"AssignmentToForLoopParameter\")" +
                 "    public static void main(String... args) {\n" +
                 "        for (int i = 0; i < args.length; i++) {\n" +
                 "            i = 10;" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    @Test
    public void testAssignmentToCatchBlockParameter() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        try {\n" +
                 "        } catch (Exception e) {\n" +
                 "            e = null;" +
                 "        }" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings("5:12-5:20:verifier:Assignment to catch-block parameter e");
    }

    @Test
    public void testAssignmentToCatchBlockParameterSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"AssignmentToCatchBlockParameter\")\n" +
                 "    public static void main(String... args) {\n" +
                 "        try {\n" +
                 "        } catch (Exception e) {\n" +
                 "            e = null;" +
                 "        }" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    @Test
    public void testAssignmentToMethodParam() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        args = null;" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings("3:8-3:19:verifier:Assignment to method parameter args");
    }

    @Test
    public void testAssignmentToMethodParamSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"AssignmentToMethodParameter\")\n" +
                 "    public static void main(String... args) {\n" +
                 "        args = null;" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    @Test
    public void testNestedAssignment() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 10;\n" +
                 "        while ((i = 2 + i) > 10) {\n" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings("4:16-4:25:verifier:Nested assignment 'i = 2 + i'");
    }

    @Test
    public void testNestedAssignmentSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"NestedAssignment\")\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 10;\n" +
                 "        while ((i = 2 + i) > 10) {\n" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    @Test
    public void testIncrementDecrementUsed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 10;\n" +
                 "        while (i++ > 10) {\n" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings("4:15-4:18:verifier:Value of increment expression 'i++' is used");
    }

    @Test
    public void testIncrementDecrementUsedSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"ValueOfIncrementOrDecrementUsed\")\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 10;\n" +
                 "        while (i++ > 10) {\n" +
                 "        }\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    @Test
    public void testReplaceAssignWithOpAssign() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 0;\n" +
                 "        i = i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .findWarning("4:8-4:18:verifier:Assignment 'i = i - 10' is replacable with operator-assignment")
                .applyFix("Replace assignment 'i = i - 10' with operator-assignment")
                .assertCompilable()
                .assertOutput("package test;\n" +
                 "public class Test {\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 0;\n" +
                 "        i -= 10;\n" +
                 "    }\n" +
                 "}");
    }

    public void testReplaceAssignWithOpAssign185372a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {" +
                 "    private int i;\n" +
                 "    public void main(String... args) {\n" +
                 "        i = this.i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .findWarning("3:8-3:23:verifier:Assignment 'i = this.i - 10' is replacable with operator-assignment")
                .applyFix("Replace assignment 'i = this.i - 10' with operator-assignment")
                .assertCompilable()
                .assertOutput("package test;\n" +
                 "public class Test {\n" +
                 "    private int i;\n" +
                 "    public void main(String... args) {\n" +
                 "        i -= 10;\n" +
                 "    }\n" +
                 "}");
    }

    public void testReplaceAssignWithOpAssign185372b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {" +
                 "    private int i;\n" +
                 "    public void main(String... args) {\n" +
                 "        this.i = i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .findWarning("3:8-3:23:verifier:Assignment 'this.i = i - 10' is replacable with operator-assignment")
                .applyFix("Replace assignment 'this.i = i - 10' with operator-assignment")
                .assertCompilable()
                .assertOutput("package test;\n" +
                 "public class Test {\n" +
                 "    private int i;\n" +
                 "    public void main(String... args) {\n" +
                 "        this.i -= 10;\n" +
                 "    }\n" +
                 "}");
    }

    @Test
    public void testReplaceAssignWithOpAssignSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    @SuppressWarnings(\"AssignmentReplaceableWithOperatorAssignment\")\n" +
                 "    public static void main(String... args) {\n" +
                 "        int i = 0;\n" +
                 "        i = i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    public void testReplaceAssignWithOpAssign185372c() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    private int i;\n" +
                 "    public void main(Test t) {\n" +
                 "        i = t.i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }

    public void testReplaceAssignWithOpAssign185372d() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                 "public class Test {\n" +
                 "    private int i;\n" +
                 "    public void main(Test t) {\n" +
                 "        t.i = i - 10;\n" +
                 "    }\n" +
                 "}")
                .run(AssignmentIssues.class)
                .assertWarnings();
    }
}
