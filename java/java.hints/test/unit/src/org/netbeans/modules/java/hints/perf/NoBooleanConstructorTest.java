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
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class NoBooleanConstructorTest extends NbTestCase {

    public NoBooleanConstructorTest(String name) {
        super(name);
    }

    public void testBoolean15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Boolean test(boolean b) {\n" +
                       "         return new Boolean(b);\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.5")
                .run(NoBooleanConstructor.class)
                .findWarning("3:16-3:30:verifier:ERR_NoBooleanConstructor")
                .applyFix("FIX_NoBooleanConstructorBoolean")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Boolean test(boolean b) {\n" +
                              "         return b;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testBoolean14() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Boolean test(boolean b) {\n" +
                       "         return new Boolean(b);\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.4")
                .run(NoBooleanConstructor.class)
                .findWarning("3:16-3:30:verifier:ERR_NoBooleanConstructor")
                .applyFix("FIX_NoBooleanConstructorBoolean")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Boolean test(boolean b) {\n" +
                              "         return Boolean.valueOf(b);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testBoolean13() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Boolean test(boolean b) {\n" +
                       "         return new Boolean(b);\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.3")
                .run(NoBooleanConstructor.class)
                .findWarning("3:16-3:30:verifier:ERR_NoBooleanConstructor")
                .applyFix("FIX_NoBooleanConstructorBoolean")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Boolean test(boolean b) {\n" +
                              "         return (b ? Boolean.TRUE : Boolean.FALSE);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testString() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Boolean test(String s) {\n" +
                       "         return new Boolean(s);\n" +
                       "     }\n" +
                       "}\n")
                .run(NoBooleanConstructor.class)
                .findWarning("3:16-3:30:verifier:ERR_NoBooleanConstructor")
                .applyFix("FIX_NoBooleanConstructorString")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Boolean test(String s) {\n" +
                              "         return Boolean.valueOf(s);\n" +
                              "     }\n" +
                              "}\n");
    }
}
