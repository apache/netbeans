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
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class IncompatibleMaskTest extends NbTestCase {

    public IncompatibleMaskTest(String name) {
        super(name);
    }

    @Test
    public void testWarning1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i & 6) == 1;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (i & 6) == 1 is always false");
    }

    @Test
    public void testWarning2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 1 == (i & 6);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 1 == (i & 6) is always false");
    }

    @Test
    public void testWarning3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (6 & i) == 1;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (6 & i) == 1 is always false");
    }

    @Test
    public void testWarning4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 1 == (6 & i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 1 == (6 & i) is always false");
    }

    @Test
    public void testWarning5() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i | 6) == 2;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (i | 6) == 2 is always false");
    }

    @Test
    public void testWarning6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 2 == (i | 6);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 2 == (i | 6) is always false");
    }

    @Test
    public void testWarning7() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (6 | i) == 2;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (6 | i) == 2 is always false");
    }

    @Test
    public void testWarning8() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 2 == (6 | i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 2 == (6 | i) is always false");
    }

    @Test
    public void testWarning11() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i & a) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (i & a) == c is always false");
    }

    @Test
    public void testWarning12() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (i & a);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (i & a) is always false");
    }

    @Test
    public void testWarning13() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (a & i) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (a & i) == c is always false");
    }

    @Test
    public void testWarning14() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (a & i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (a & i) is always false");
    }

    @Test
    public void testWarning15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i | a) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (i | a) == c is always false");
    }

    @Test
    public void testWarning16() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (i | a);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (i | a) is always false");
    }

    @Test
    public void testWarning17() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (a | i) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (a | i) == c is always false");
    }

    @Test
    public void testWarning18() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (a | i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (a | i) is always false");
    }

    static {
        NbBundle
                .setBranding("test");
    }
}