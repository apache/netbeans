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
public class OverridableMethodCallInConstructorTest extends NbTestCase {

    public OverridableMethodCallInConstructorTest(String name) {
        super(name);
    }

    public void testDoNotReportPrivateCall() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    private void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }

    public void testReportPackagePrivateCall() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings("2:20-2:23:verifier:Overridable method call in constructor");
    }

    public void testReportPublicCall() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    public void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings("2:20-2:23:verifier:Overridable method call in constructor");
    }

    public void testDoNotReportFinalCall() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    public final void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }

    public void testDoNotReportStaticCall() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    static void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }

    public void testDoNotReportOnFinalClass() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public final class Test {\n" +
                       "    public Test() { foo(); }\n" +
                       "    public void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }

    public void testDoNotReportForeignClassMethodInvocations() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { new Object().toString(); }\n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }

    public void testDoNotReportForeignObjectMethodInvocations() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { new Test().foo(); }\n" +
                       "    public void foo() { } \n" +
                       "}")
                .run(OverridableMethodCallInConstructor.class)
                .assertWarnings();
    }
}