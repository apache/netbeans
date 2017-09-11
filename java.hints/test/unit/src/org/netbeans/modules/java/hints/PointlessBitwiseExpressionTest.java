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
public class PointlessBitwiseExpressionTest extends NbTestCase {

    public PointlessBitwiseExpressionTest(String name) {
        super(name);
    }

    @Test
    public void test1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i & 0;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix2")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = 0;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = 0 & i;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix2")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = 0;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i | 0;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = 0 | i;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_const_1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i & T;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix2")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = T;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_const_2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = T & i;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix2")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = T;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_const_3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i | T;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_const_4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = T | i;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:21:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i >> 0;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:22:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_const_1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i >> T;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:22:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i >>> 0;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:23:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_const_2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i >>> T;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:23:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i << 0;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:22:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test_sh_const_3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int T = 0;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i << T;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("5:16-5:22:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    static final int T = 0;\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    public void test184758a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int modifiers = 0;\n" +
                       "        boolean addFlag = true;\n" +
                       "        modifiers = modifiers | (addFlag ? 1 : 0);\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .assertWarnings();
    }

    public void test184758b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        int b = i << (0+0);\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .findWarning("4:16-4:26:verifier:Pointless bitwise expression")
                .applyFix("MSG_PointlessBitwiseExpression_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "        int i = 10;\n" +
                "        int b = i;\n" +
                "    }\n" +
                "}");
    }

    public void test185010() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        byte[] b = null;\n" +
                       "        int a = b[0] & 0x80;\n" +
                       "    }\n" +
                       "}")
                .run(PointlessBitwiseExpression.class)
                .assertWarnings();
    }

    static {
        NbBundle
                .setBranding("test");
    }
}