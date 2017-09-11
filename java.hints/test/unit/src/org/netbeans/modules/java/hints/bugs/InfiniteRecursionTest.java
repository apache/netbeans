/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
