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
 * @author David Strupl
 */
public class StaticNonFinalUsedInInitializationTest extends NbTestCase {

    public StaticNonFinalUsedInInitializationTest(String name) {
        super(name);
    }

    public void testDoNotReport() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final int A = 5;\n" +
                       "    public static final int B = A + 10;\n" +
                       "}")
                .run(StaticNonFinalUsedInInitialization.class)
                .assertWarnings();
    }

    public void testDoNotReport2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static int A = 5;\n" +
                       "    public int B = A + 10;\n" +
                       "    public int C;\n" +
                       "    {\n" +
                       "        C = A + 10;" +
                       "    }\n" +
                       "}")
                .run(StaticNonFinalUsedInInitialization.class)
                .assertWarnings();
    }

    public void testReportIt() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    static int A = 5;\n" +
                       "    static int B = A + 10;\n" +
                       "}")
                .run(StaticNonFinalUsedInInitialization.class)
                .assertWarnings("3:19-3:20:verifier:StaticNonFinalUsedInInitialization");
    }

    public void testReportIt2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    static int A = 5;\n" +
                       "    static int B;\n" +
                       "    static {\n" +
                       "        B = A + 10;\n" +
                       "    }\n" +
                       "}")
                .run(StaticNonFinalUsedInInitialization.class)
                .assertWarnings("5:12-5:13:verifier:StaticNonFinalUsedInInitialization");
    }
}
