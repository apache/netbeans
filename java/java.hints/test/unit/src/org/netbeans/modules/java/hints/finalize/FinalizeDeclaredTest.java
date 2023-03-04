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
public class FinalizeDeclaredTest extends NbTestCase {

    public FinalizeDeclaredTest(final String name) {
        super(name);
    }

    public void testFinalizeDeclared() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    protected final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDeclared.class)
                .assertWarnings("2:25-2:33:verifier:finalize() declared");
    }

    public void testSuppressed() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    @SuppressWarnings(\"FinalizeDeclaration\")\n\n" +
                       "    protected final void finalize() {\n" +
                       "    }\n" +
                       "}")
                .run(FinalizeDeclared.class)
                .assertWarnings();
    }
}