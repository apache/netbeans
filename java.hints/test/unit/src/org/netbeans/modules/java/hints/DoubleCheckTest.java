/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class DoubleCheckTest extends NbTestCase {

    public DoubleCheckTest(String testName) {
        super(testName);
    }

    /**
     * The check will not report a code which is 
     * @throws Exception 
     */
    public void testDoNotReportVolatileButNoLocal() throws Exception {
        String code = "package test;\n" +
            "public class Test {\n" +
            "    private volatile String f;\n" +
            "    public void t() {\n" +
            "        if (f == null) {\n" +
            "            synchronized (this) {\n" +
            "                if (f == null) {\n" +
            "                    f = \"\";\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";
        HintTest.
                create().
                input(code).
                run(DoubleCheck.class).
                findWarning("5:12-5:24:verifier:ERR_DoubleCheck").
                assertFixes("Use local variable for better performance");
    }
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "public static Test factory() {" +
                        "  if (INST == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";

        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings("0:115-0:127:verifier:ERR_DoubleCheck");
    }

    public void testSomeCodeAfterTheOuterIf() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static int cnt;" +
                        "public static Test factory() {" +
                        "  if (INST == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "    cnt++;" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, probably
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testDifferentVariable() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static Object cnt;" +
                        "public static Test factory() {" +
                        "  if (cnt == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, for sure
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testNoNPEWhenBrokenCondition() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static Object cnt;" +
                        "public static Test factory() {" +
                        "  if (INST == nu) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == nu) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, for sure
        HintTest
                .create()
                .input(before + after, false)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testApplyClassWithOnlyStaticMethods() throws Exception {
        String before1 = "package test; public class Test {\n" +
                         "private static Test INST;\n" +
                         "public static Test factory() {\n";
        String before2 =
                "if (INST == null) {\n";
        String before3 =
                "synchro";
        String after1 = "nized (INST) {\n" +
                        "if (INST == null) {\n" +
                        "INST = new test.Test();\n" +
                        "}\n" +
                        "}\n";
        String after2 =
                "}\n";
        String after3 =
                "return INST;\n" +
                "}" +
                "}\n";
        String after4 = "\n";

        String before = before1 + before2 + before3;
        String after = after1 + after2 + after3 + after4;

        String golden = (before1 + before3 + after1 + after3)
                .replace("\n", " ");
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .findWarning("4:0-4:12:verifier:ERR_DoubleCheck")
                .applyFix("FIX_DoubleCheck")
                .assertCompilable()
                .assertOutput(golden);
    }

    public void testVolatileJDK5IZ153334() throws Exception {
        String code = "package test; public class Test {\n" +
                      "private static volatile Test INST;\n" +
                      "public static Test factory() {\n" +
                      "if (INST == null) {\n" +
                      "synchronized (INST) {\n" +
                      "if (INST == null) {\n" +
                      "INST = new test.Test();\n" +
                      "}\n" +
                      "}\n" +
                      "}\n" +
                      "return INST;\n" +
                      "}" +
                      "}\n";

        HintTest
                .create()
                .input(code)
                .run(DoubleCheck.class)
                .findWarning("4:0-4:12:verifier:ERR_DoubleCheck").
                assertFixes("Use local variable for better performance");
    }

    public void testVolatileJDK4IZ153334() throws Exception {
        String code = "package test; public class Test {\n" +
                      "private static volatile Test INST;\n" +
                      "public static Test factory() {\n" +
                      "if (INST == null) {\n" +
                      "synchronized (INST) {\n" +
                      "if (INST == null) {\n" +
                      "INST = new test.Test();\n" +
                      "}\n" +
                      "}\n" +
                      "}\n" +
                      "return INST;\n" +
                      "}" +
                      "}\n";

        HintTest
                .create()
                .input(code)
                .sourceLevel("1.4")
                .run(DoubleCheck.class)
                .assertWarnings("4:0-4:12:verifier:ERR_DoubleCheck");
    }
}