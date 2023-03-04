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
package org.netbeans.modules.java.hints.finalize;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Tomas Zezula
 */
public class CallFinalizeTest extends NbTestCase {

    public CallFinalizeTest(final String name) {
        super(name);
    }

    public void testFinalizeCalled() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() throws Throwable {\n" +
                       "        this.finalize();\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings("3:13-3:21:verifier:finalize() called explicitly");
    }

    public void testNonFinalizeCalled() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        this.finalize(1);\n" +
                       "    }\n" +
                       "    protected void finalize(int a) {\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings();
    }

    public void testSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings({\"FinalizeCalledExplicitly\"})\n" +
                       "public class Test {\n" +
                       "    private void test() throws Throwable {\n" +
                       "        this.finalize();\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings();
    }

    public void testSuperFinalizeCalledInOverridenMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected void finalize() throws Throwable {\n" +
                       "        super.finalize();\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings();
    }

    public void testSuperFinalizeCalledInNonOverridenMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected void test() throws Throwable {\n" +
                       "        super.finalize();\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings("3:14-3:22:verifier:finalize() called explicitly");
    }

    public void testFinalizeOnThis() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected void test() throws Throwable {\n" +
                       "        finalize();\n" +
                       "    }\n" +
                       "}")
                .run(CallFinalize.class)
                .assertWarnings("3:8-3:16:verifier:finalize() called explicitly");
    }
}