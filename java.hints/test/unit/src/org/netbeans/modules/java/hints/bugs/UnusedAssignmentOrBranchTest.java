/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

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
}
