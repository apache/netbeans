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
 * @author vita
 */
public class NoLoggersTest extends NbTestCase {

    public NoLoggersTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {}")
                .run(NoLoggers.class)
                .assertWarnings("1:13-1:17:verifier:No logger declared for test.Test class");
    }

    public void testSimpleFix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private static final Logger LOG = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testLoggerName1Fix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private String LOG;" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private String LOG;" +
                              "    private static final Logger LOGGER = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testLoggerName2Fix() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private String LOG;" +
                       "    private String LOGGER;" +
                       "}")
                .run(NoLoggers.class)
                .findWarning("1:13-1:17:verifier:No logger declared for test.Test class")
                .applyFix("MSG_NoLoggers_checkNoLoggers_Fix:test.Test")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private String LOG;" +
                              "    private String LOGGER;" +
                              "    private static final Logger LOG1 = Logger.getLogger(Test.class.getName());\n" +
                              "}");
    }

    public void testNoWarningsForAbstractClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public interface Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForEnum() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public enum Test {}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInnerClasses() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    public static class Inner {\n" +
                       "    }\n" +
                       "}")
                .run(NoLoggers.class)
                .assertWarnings();
    }

    public void testCustomLogger() throws Exception {
        HintTest
                .create()
                .preference(LoggerHintsCustomizer.CUSTOM_LOGGERS_ENABLED, true)
                .preference(LoggerHintsCustomizer.CUSTOM_LOGGERS, "java.lang.String")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final String LOG = null;\n" +
                       "}")
                .run(NoLoggers.class)
                .assertWarnings();
    }
}
