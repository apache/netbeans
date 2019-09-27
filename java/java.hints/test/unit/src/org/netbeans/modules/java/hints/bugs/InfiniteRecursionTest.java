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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class InfiniteRecursionTest extends NbTestCase {

    public InfiniteRecursionTest(String name) {
        super(name);
    }

    /**
     * Checks the most brutal recursion as a unconditional statement
     */
    public void testStatement() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        recurse(0);\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("4:8-4:18:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Errors in final class is reported although method itself is nonfinal
     *
     * @throws Exception
     */
    public void testFinalClass() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public int recurse(int var) {\n"
                + "        recurse(0);\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("4:8-4:18:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Recursions is reported as "might" in overridable methods
     *
     * @throws Exception
     */
    public void testOverridable() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public class Test {\n"
                + "    final int v = 1;\n"
                + "    public int recurse(int var) {\n"
                + "        recurse(0);\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("4:8-4:18:verifier:The method recurse may recurse if not overriden in subclasses");
    }

    /**
     * Recursion in return expression is detected although return terminates the method
     *
     * @throws Exception
     */
    public void testReturnExpression() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        return recurse(3);\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .assertWarnings("4:15-4:25:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Recursion is detected in if iff it occurs in both branches.
     *
     * @throws Exception
     */
    public void testBothIfBranches() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        if (Math.random() > 1) {\n"
                + "            return recurse(2);\n"
                + "        } else {\n"
                + "            return recurse(3);\n"
                + "        }\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("5:19-5:29:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Recursion in while condition is detected
     *
     * @throws Exception
     */
    public void testWhileCondition() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        while (recurse(33) > 3) {\n"
                + "        }\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("4:15-4:26:verifier:The method recurse will recurse infinitely");
    }

    /**
     * In switch, recursion is detected if if appears in all cases, including default.
     *
     * @throws Exception
     */
    public void testAllSwitchCases() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        int f = 0;\n"
                + "        switch (var) {\n"
                + "            case 1: f = recurse(1); break;\n"
                + "            case 2: f = recurse(3); break;\n"
                + "            default: f = recurse(4); break;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("6:24-6:34:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Checks that if a case does not contain a recursion, but falls through to a case which does, the recusion is
     * reported.
     *
     * @throws Exception
     */
    public void testSwitchFallthroughRecurses() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        int f = 0;\n"
                + "        switch (var) {\n"
                + "            case 1: f = 5;\n"
                + "            case 2: f = recurse(3); break;\n"
                + "            default: f = recurse(4); break;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("7:24-7:34:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Return in an unreachable if branch is ignored.
     *
     * @throws Exception
     */
    public void testReturnInUnreachableBranch() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        if (v > 2) {\n"
                + "            return 1;\n"
                + "        }\n"
                + "        return recurse(3);\n"
                + "    }\n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .findWarning("7:15-7:25:verifier:The method recurse will recurse infinitely");
    }

    /**
     * Recursion is bypassed by a break;
     *
     * @throws Exception
     */
    public void testRecursionBypassedBreak() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        do {\n"
                + "            if (Boolean.getBoolean(\"e\")) {\n"
                + "                break;\n"
                + "            }\n"
                + "            recurse(var);\n"
                + "        } while (true);\n"
                + "        return 1;\n"
                + "    }\n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Recursion is bypassed by a continue
     *
     * @throws Exception
     */
    public void testRecursionBypassContinue() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        do {\n"
                + "            if (Boolean.getBoolean(\"e\")) {\n"
                + "                continue;\n"
                + "            }\n"
                + "            recurse(var);\n"
                + "        } while (true);\n"
                + "    }\n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Recursion is bypassed by a labelled break, breaks into outer cycle.
     *
     * @throws Exception public void testRecursionBypassLabelledBreak() throws Exception { HintTest.create() .input(
     * "package test;\n" + "public final class Test {\n" + " final int v = 1;\n" + " public final int recurse(int var)
     * {\n" + " do {\n" + " if (Boolean.getBoolean(\"e\")) {\n" + " continue;\n" + " }\n" + " recurse(var);\n" + " }
     * while (true);\n" + " }\n" + "}\n" + "" ) .run(InfiniteRecursion.class) .findWarning(""); }
     */
    /**
     * Branch falls through to another branch that does not contain recursion, but breaks out of the switch
     *
     * @throws Exception
     */
    public void testSwitchFallthroughOK() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        int f = 0;\n"
                + "        switch (var) {\n"
                + "            case 1: f = 5;\n"
                + "            case 2: f = 3; break;\n"
                + "            default: recurse(4);\n"
                + "        }\n"
                + "        return 1;\n"
                + "    }\n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    public void testSwitchCasePasses() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        int f = 0;\n"
                + "        switch (var) {\n"
                + "            case 1: f = 5; break;\n"
                + "            case 2: f = 3; recurse(4); break;\n"
                + "            default: \n"
                + "        }\n"
                + "        return 1;\n"
                + "    }\n"
                + "}\n"
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Detects a possible return preceding the recursion point
     *
     * @throws Exception
     */
    public void testPossibleReturnBefore() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        if (var < 0) { \n"
                + "            return -1;\n"
                + "        }\n"
                + "        return recurse(var + 1);\n"
                + "    }\n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Detects possible return in one of switch branches
     *
     * @throws Exception
     */
    public void testSwitchPossiblyReturns() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        switch (var) {\n"
                + "            case 0: return -1;\n"
                + "            case 1: recurse(1); break;\n"
                + "        }\n"
                + "        return recurse(var + 1);\n"
                + "    } \n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Does not report recursion if it appears in just one of if branches
     *
     * @throws Exception
     */
    public void testOneIfBranch() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        if (var > 0) {\n"
                + "            recurse(var - 1);\n"
                + "        } else {\n"
                + "            return 1;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    } \n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Entire contents of while statement might be bypassed, the same for 'for' statement body and update clauses below
     *
     * @throws Exception
     */
    public void testNotInsideWhile() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        while (var > 0) {\n"
                + "            recurse(var);\n"
                + "            var--;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    } \n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    public void testNotInsideFor() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        for (int i = 0; i < 0; i ++) {\n"
                + "            recurse(var);\n"
                + "            var--;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    } \n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    public void testNotForUpdate() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        for (int i = 0; i < 0; i = recurse(var)) {\n"
                + "            recurse(var);\n"
                + "            var--;\n"
                + "        }\n"
                + "        return 0;\n"
                + "    } \n"
                + "}"
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }

    /**
     * Checks that implicit empty default suppresses recursion report
     *
     * @throws Exception
     */
    public void testNotSwitchWithoutDefault() throws Exception {
        HintTest.create()
                .input(
                "package test;\n"
                + "public final class Test {\n"
                + "    final int v = 1;\n"
                + "    public final int recurse(int var) {\n"
                + "        switch (var) {\n"
                + "            case 0: return recurse(var + 1);\n"
                + "            case 1: return recurse(var + 2); \n"
                + "        }\n"
                + "        return 0;\n"
                + "    } \n"
                + "}\n"
                + ""
                )
                .run(InfiniteRecursion.class)
                .assertWarnings();
    }
}
