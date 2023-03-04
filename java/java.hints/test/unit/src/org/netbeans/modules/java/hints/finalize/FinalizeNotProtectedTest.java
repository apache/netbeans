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
public class FinalizeNotProtectedTest extends NbTestCase {

    public FinalizeNotProtectedTest(final String name) {
        super(name);
    }

    public void testFinalizeNotProtected() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeNotProtected.class)
                .assertWarnings("2:22-2:30:verifier:finalize() not declared protected");
    }

    public void testFinalizeProtected() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeNotProtected.class)
                .assertWarnings();
    }

    public void testNonFinalizeNotProtected() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void finalize(int i) {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeNotProtected.class)
                .assertWarnings();
    }

    public void testSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "@SuppressWarnings(\"FinalizeNotProtected\")\n" +
                       "public class Test {\n" +
                       "    public final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeNotProtected.class)
                .assertWarnings();
    }

    public void testFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeNotProtected.class)
                .findWarning("2:22-2:30:verifier:finalize() not declared protected")
                .applyFix("Make pretected")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    protected final void finalize() {\n" +
                              "    }\n" +
                              "}");
    }
}
