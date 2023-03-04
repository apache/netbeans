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