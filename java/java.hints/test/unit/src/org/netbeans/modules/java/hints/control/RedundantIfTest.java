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
public class RedundantIfTest {
    
    @Test
    public void testSimpleRedundantIf() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            return true;\n" +
                       "        } else {\n" +
                       "            return false;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i == 0;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testRedundantIfNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            return false;\n" +
                       "        } else {\n" +
                       "            return true;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return i != 0;\n" +
                              "    }\n" +
                              "}\n");
    }

    /**
     * Checks that De Morgan rules will not apply when negating an if-expression.
     * @throws Exception 
     */
    @Test
    public void testRedundantNoDeMorgan() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        if (i < 5 || i > 7) {\n" +
                       "            return false;\n" +
                       "        } else {\n" +
                       "            return true;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("3:8-3:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        return !(i < 5 || i > 7);\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testSimpleRedundantIfVar() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        if (i == 0) {\n" +
                       "            r = true;\n" +
                       "        } else {\n" +
                       "            r = false;\n" +
                       "        }\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
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
    public void testSimpleRedundantIfVarNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        boolean r;\n" +
                       "        if (i == 0) {\n" +
                       "            r = false;\n" +
                       "        } else {\n" +
                       "            r = true;\n" +
                       "        }\n" +
                       "        return r;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
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
    public void testRedundantIfImplicit() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        System.err.println(i);\n" +
                       "        if (i == 0) {\n" +
                       "            return true;\n" +
                       "        }\n" +
                       "        return false;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        System.err.println(i);\n" +
                              "        return i == 0;\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testRedundantIfImplicitNeg() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean t(int i) {\n" +
                       "        System.err.println(i);\n" +
                       "        if (i == 0) {\n" +
                       "            return false;\n" +
                       "        }\n" +
                       "        return true;\n" +
                       "    }\n" +
                       "}\n")
                .run(RedundantIf.class)
                .findWarning("4:8-4:10:verifier:" + Bundle.ERR_redundantIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private boolean t(int i) {\n" +
                              "        System.err.println(i);\n" +
                              "        return i != 0;\n" +
                              "    }\n" +
                              "}\n");
    }
}
