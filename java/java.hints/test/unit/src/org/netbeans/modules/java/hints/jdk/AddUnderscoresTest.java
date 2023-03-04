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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class AddUnderscoresTest extends NbTestCase {

    public AddUnderscoresTest(String name) {
        super(name);
    }

    public void testSimpleAdd() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 12345678;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(AddUnderscores.class)
                .findWarning("2:37-2:45:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores12_345_678")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final int CONST = 12_345_678;\n" +
                              "}\n");
    }

    public void testNegativeAdd() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = -12345678;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(AddUnderscores.class)
                .findWarning("2:37-2:46:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores-12_345_678")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final int CONST = -12_345_678;\n" +
                              "}\n");
    }


    public void testPositiveAdd() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = +12345678;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(AddUnderscores.class)
                .findWarning("2:38-2:46:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores12_345_678")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final int CONST = +12_345_678;\n" +
                              "}\n");
    }

    public void testSettings() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0B1010101010101010;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(AddUnderscores.KEY_SIZE_BINARY, 5)
                .run(AddUnderscores.class)
                .findWarning("2:37-2:55:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores0B1_01010_10101_01010")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final int CONST = 0B1_01010_10101_01010;\n" +
                              "}\n");
    }

    public void testHexLong() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final long CONST = 0xA5A5A5A5A5A5A5A5L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(AddUnderscores.KEY_SIZE_HEXADECIMAL, 3)
                .run(AddUnderscores.class)
                .findWarning("2:38-2:57:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores0xA_5A5_A5A_5A5_A5A_5A5L")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final long CONST = 0xA_5A5_A5A_5A5_A5A_5A5L;\n" +
                              "}\n");
    }

    public void testAlreadyHasUnderscores1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final long CONST = 0xA5A5A5A5_A5A5A5A5L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(AddUnderscores.class)
                .assertWarnings();
    }

    public void testAlreadyHasUnderscores2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final long CONST = 0xA5A5A5A5A5A5A5A_5L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(AddUnderscores.KEY_ALSO_WITH_UNDERSCORES, true)
                .run(AddUnderscores.class)
                .findWarning("2:38-2:58:hint:ERR_org.netbeans.modules.javahints.jdk.AddUnderscores")
                .applyFix("FIX_org.netbeans.modules.javahints.jdk.AddUnderscores0xA5A5_A5A5_A5A5_A5A5L")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final long CONST = 0xA5A5_A5A5_A5A5_A5A5L;\n" +
                              "}\n");
    }

    public void testZeroIsNotOctal() throws Exception {
        assertEquals(10, AddUnderscores
                .radixInfo("0").radix);
        assertEquals(10, AddUnderscores
                .radixInfo("0L").radix);
    }

    public void testIgnoreOctalConstantsForNow() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = 0123;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(AddUnderscores.class)
                .assertWarnings();
    }
    
    public void test220979a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = -123;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(AddUnderscores.KEY_SIZE_DECIMAL, 3)
                .run(AddUnderscores.class)
                .assertWarnings();
    }
    
    public void test220979b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int CONST = -/*foobar*/123;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(AddUnderscores.KEY_SIZE_DECIMAL, 3)
                .run(AddUnderscores.class)
                .assertWarnings();
    }
}
