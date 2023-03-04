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

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class PrintStackTraceTest extends NbTestCase {

    public PrintStackTraceTest(String name) {
        super(name);
    }

    @Test
    public void test1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        new Exception ().printStackTrace ();\n" +
                       "    }\n" +
                       "}")
                .run(PrintStackTrace.class)
                .findWarning("3:25-3:40:verifier:Print Stack Trace")
                .applyFix("MSG_PrintStackTrace_fix")
                .assertCompilable()
                .assertOutput(
                "package test;\n" +
                "class Test {\n" +
                "    void test () {\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void test2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        new Test ().printStackTrace ();\n" +
                       "    }\n" +
                       "    void printStackTrace () {\n" +
                       "    }\n" +
                       "}")
                .run(PrintStackTrace.class)
                .assertWarnings();
    }

    static {
        NbBundle
                .setBranding("test");
    }
}