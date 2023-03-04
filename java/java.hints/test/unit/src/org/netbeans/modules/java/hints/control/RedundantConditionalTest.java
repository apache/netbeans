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
package org.netbeans.modules.java.hints.control;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class RedundantConditionalTest {
    
    @Test
    public void testSimpleRedundantConditional() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        return i == 0 ? true : false;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:15-3:36:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i == 0;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testRedundantConditionalNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        return i == 0 ? false : true;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:15-3:36:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i != 0;\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testSimpleRedundantConditionalVar() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        r = i == 0 ? true : false;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("4:12-4:33:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r;\n" +
                              "        r = i == 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testSimpleRedundantConditionalVarNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        r = i == 0 ? false : true;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("4:12-4:33:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r;\n" +
                              "        r = i != 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testSimpleRedundantConditionalVarInit() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r = i == 0 ? true : false;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:20-3:41:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r = i == 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testSimpleRedundantConditionalVarInitNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r = i == 0 ? false : true;\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantConditional.class)
                .findWarning("3:20-3:41:verifier:" + Bundle.ERR_redundantConditional())
                .applyFix(Bundle.FIX_redundantConditional())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        boolean r = i != 0;\n" +
                              "        return r;\n" +
                              "    }\n" +
                              "}\n");
    }
}
