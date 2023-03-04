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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class AssignmentToItselfTest extends NbTestCase {

    public AssignmentToItselfTest(String name) {
        super(name);
    }

    public void testAnalysis1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int i) {\n" +
                       "        i = i;\n" +
                       "    }\n" +
                       "}")
                .run(AssignmentToItself.class)
                .assertWarnings("3:8-3:13:verifier:ERR_AssignmentToItself");
    }

    public void testAnalysis2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int i;\n" +
                       "    public Test(int i) {\n" +
                       "        this.i = i;\n" +
                       "    }\n" +
                       "}")
                .run(AssignmentToItself.class)
                .assertWarnings();
    }

    public void testAnalysis3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int[] i, int j) {\n" +
                       "        i[0] = i[1];\n" +
                       "        i[j] = i[j + 1];\n" +
                       "        i[j] = i[j];\n" +
                       "    }\n" +
                       "}")
                .run(AssignmentToItself.class)
                .assertWarnings("5:8-5:19:verifier:ERR_AssignmentToItself");
    }

    public void testAnalysis4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private int i;\n" +
                       "    public Test() {\n" +
                       "        this.i = i;\n" +
                       "    }\n" +
                       "}")
                .run(AssignmentToItself.class)
                .assertWarnings("4:8-4:18:verifier:ERR_AssignmentToItself");
    }
}
