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
public class MultipleLoggersTest extends NbTestCase {

    public MultipleLoggersTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static final java.util.logging.Logger LOG1 = null;\n" +
                       "    private static final java.util.logging.Logger LOG2 = null;\n" +
                       "}")
                .run(MultipleLoggers.class)
                .assertWarnings("2:50-2:54:verifier:Multiple loggers LOG1, LOG2 declared for test.Test class",
                                "3:50-3:54:verifier:Multiple loggers LOG1, LOG2 declared for test.Test class");
    }

    public void testNoWarningsForAbstractClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public abstract class Test {\n" +
                       "    private static final java.util.logging.Logger LOG1 = null;\n" +
                       "    private static final java.util.logging.Logger LOG2 = null;\n" +
                       "}")
                .run(MultipleLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInterface() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public interface Test {\n" +
                       "    private static final java.util.logging.Logger LOG1 = null;\n" +
                       "    private static final java.util.logging.Logger LOG2 = null;\n" +
                       "}", false)
                .run(MultipleLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForEnum() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    private static final java.util.logging.Logger LOG1 = null;\n" +
                       "    private static final java.util.logging.Logger LOG2 = null;\n" +
                       "}", false)
                .run(MultipleLoggers.class)
                .assertWarnings();
    }

    public void testNoWarningsForInnerClasses() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static class Inner {\n" +
                       "        private static final java.util.logging.Logger LOG1 = null;\n" +
                       "        private static final java.util.logging.Logger LOG2 = null;\n" +
                       "    }\n" +
                       "}")
                .run(MultipleLoggers.class)
                .assertWarnings();
    }
}