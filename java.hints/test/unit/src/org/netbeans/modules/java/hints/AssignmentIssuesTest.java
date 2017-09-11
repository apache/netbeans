/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
