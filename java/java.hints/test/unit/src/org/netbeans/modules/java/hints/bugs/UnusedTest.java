/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
 * @author lahvac
 */
public class UnusedTest extends NbTestCase {

    public UnusedTest(String name) {
        super(name);
    }

    public void testUnused() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private class UnusedClass {}\n" +
                       "    private void unusedMethod() {}\n" +
                       "    private int unusedField;\n" +
                       "    private void test(int unusedParam) {}\n" +
                       "    public void test2() {test(1);}\n" +
                       "    private Test() {}\n" +
                       "}\n")
                .run(Unused.class)
                .assertWarnings("2:18-2:29:verifier:" + Bundle.ERR_NotUsed("UnusedClass"),
                                "3:17-3:29:verifier:" + Bundle.ERR_NotUsed("unusedMethod"),
                                "4:16-4:27:verifier:" + Bundle.ERR_NotRead("unusedField"),
                                "5:26-5:37:verifier:" + Bundle.ERR_NotRead("unusedParam"),
                                "7:12-7:16:verifier:" + Bundle.ERR_NotUsedConstructor());
    }

    public void testNoFixForBindings() throws Exception {
        HintTest
                .create()
                .sourceLevel("17")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    boolean test(Object o) {\n" +
                       "        return o instanceof String s;\n" +
                       "    }\n" +
                       "}\n")
                .run(Unused.class)
                .findWarning("3:35-3:36:verifier:Variable s is never read")
                .assertFixes();
    }

    public void testUnusedNoPackagePrivate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void packagePrivate() {}\n" +
                       "}\n")
                .run(Unused.class)
                .assertWarnings("2:9-2:23:verifier:" + Bundle.ERR_NotUsed("packagePrivate"));
        HintTest
                .create()
                .preference(Unused.DETECT_UNUSED_PACKAGE_PRIVATE, false)
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void packagePrivate() {}\n" +
                       "}\n")
                .run(Unused.class)
                .assertWarnings();
    }

    public void testNoFixForTopLevelPackagePrivateClass() throws Exception {
        HintTest.create()
                .input(
                    """
                    package test;
                    class Test {
                    }
                    """)
                .run(Unused.class)
                .assertWarnings();
    }

    public void testNoFixForTopLevelPackagePrivateEnum() throws Exception {
        HintTest.create()
                .input(
                    """
                    package test;
                    enum Test {
                    }
                    """)
                .run(Unused.class)
                .assertWarnings();
    }

    public void testNoFixForTopLevelPackagePrivateInterface() throws Exception {
        HintTest.create()
                .input(
                    """
                    package test;
                    interface Test {
                    }
                    """)
                .run(Unused.class)
                .assertWarnings();
    }

    public void testNoFixForTopLevelPackagePrivateRecord() throws Exception {
        HintTest.create()
                .sourceLevel(17)
                .input(
                    """
                    package test;
                    record Test() {
                    }
                    """)
                .run(Unused.class)
                .assertWarnings();
    }

}
