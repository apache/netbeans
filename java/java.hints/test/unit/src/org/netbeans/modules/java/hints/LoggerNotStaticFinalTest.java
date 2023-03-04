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

/**
 *
 * @author vita
 */
public class LoggerNotStaticFinalTest extends NbTestCase {

    public LoggerNotStaticFinalTest(String name) {
        super(name);
    }

    @Test
    public void testStaticMissing() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .assertWarnings("2:43-2:46:verifier:The logger declaration field LOG should be static and final");
    }

    @Test
    public void testStaticMissingFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private final java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .findWarning("2:43-2:46:verifier:The logger declaration field LOG should be static and final")
                .applyFix("MSG_LoggerNotStaticFinal_checkLoggerDeclaration_fix:LOG")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final java.util.logging.Logger LOG = null;\n" +
                              "}");
    }

    @Test
    public void testFinalMissing() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .assertWarnings("2:44-2:47:verifier:The logger declaration field LOG should be static and final");
    }

    @Test
    public void testFinalMissingFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .findWarning("2:44-2:47:verifier:The logger declaration field LOG should be static and final")
                .applyFix("MSG_LoggerNotStaticFinal_checkLoggerDeclaration_fix:LOG")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final java.util.logging.Logger LOG = null;\n" +
                              "}");
    }

    @Test
    public void testBothStaticAndFinalMissing() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .assertWarnings("2:37-2:40:verifier:The logger declaration field LOG should be static and final");
    }

    @Test
    public void testBothStaticAndFinalMissingFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .findWarning("2:37-2:40:verifier:The logger declaration field LOG should be static and final")
                .applyFix("MSG_LoggerNotStaticFinal_checkLoggerDeclaration_fix:LOG")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static final java.util.logging.Logger LOG = null;\n" +
                              "}");
    }

    @Test
    public void testBothStaticAndFinalPresent() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final java.util.logging.Logger LOG = null;\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .assertWarnings();
    }

    public void testInnerClass202795() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public class I {\n" +
                       "        private java.util.logging.Logger LOG = null;\n" +
                       "    }\n" +
                       "}")
                .run(LoggerNotStaticFinal.class)
                .assertWarnings();
    }
}