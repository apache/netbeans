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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class AssertWithSideEffectsTest extends NbTestCase {

    public AssertWithSideEffectsTest(String name) {
        super(name);
    }
    
    public void testDirectAssigns() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    int field;\n" +
                "    void test1() {\n" +
                "        int var = 2;\n" +
                "        assert (var = 1) > 0 : \"ble\";\n" +
                "        assert (var += 2) > 1 : \"fuj\";\n" +
                "        assert (field = 1) > 0 : \"cune\";\n" +
                "        assert (field-- > 0) : \"nemehlo\";\n" +
                "        assert (Math.min(var = var +3, 7)) > 0 : \"truhlik\";\n" +
                "    }\n" +
                "}"
                )
                .run(AssertWithSideEffects.class).
                assertWarnings(
                    "5:16-5:19:verifier:Assert condition produces side effects", 
                    "6:16-6:19:verifier:Assert condition produces side effects", 
                    "7:16-7:21:verifier:Assert condition produces side effects", 
                    "8:16-8:23:verifier:Assert condition produces side effects",
                    "9:25-9:28:verifier:Assert condition produces side effects"
                );
    }
    
    public void testAssignFromSelfMethod() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    int field;\n" +
                "    void test1() {\n" +
                "        int var = 2;\n" +
                "        assert m1() : \"ble\";\n" +
                "        assert m2() : \"fuj\";\n" +
                "        assert m3() : \"cune\"; \n" +
                "        assert m4() : \"ok\";\n" +
                "        assert m5() : \"eek\";\n" +
                "        assert m6() : \"ook\";\n" +
                "    } \n" +
                "    boolean m1() {\n" +
                "        field = 2;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m2() { \n" +
                "        field--;\n" +
                "        return true;\n" +
                "        \n" +
                "    }\n" +
                "    boolean m3() {\n" +
                "        field -= 2;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m4() {\n" +
                "        int var = 1;\n" +
                "        return true;\n" +
                "    }\n" +
                "    boolean m5() {\n" +
                "        class Local {\n" +
                "            void m() {\n" +
                "                field = 3;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "   }\n" +
                "    boolean m6() {\n" +
                "        class Local {\n" +
                "            int field;\n" +
                "            void m() {\n" +
                "                field = 3;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "    }\n" +
                "}"
                )
                .run(AssertWithSideEffects.class).
                assertWarnings(
                    "5:15-5:19:verifier:Assert condition produces side effects",
                    "6:15-6:19:verifier:Assert condition produces side effects", 
                    "7:15-7:19:verifier:Assert condition produces side effects", 
                    "9:15-9:19:verifier:Assert condition produces side effects"
                );
    }

}
