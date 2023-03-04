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
public class LeakingThisInConstructorTest extends NbTestCase {

    public LeakingThisInConstructorTest(String name) {
        super(name);
    }

    public void testDoNotReportMemberSelect() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { System.out.println(this.toString()); }\n" +
                       "}")
                .run(LeakingThisInConstructor.class)
                .assertWarnings();
    }

    public void testDoNotReportAssignment() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private boolean a;\n" +
                       "    public Test() { this.a = true; }\n" +
                       "}")
                .run(LeakingThisInConstructor.class)
                .assertWarnings();
    }

    public void testReportIt() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { System.out.println(this); }\n" +
                       "}")
                .run(LeakingThisInConstructor.class)
                .assertWarnings("2:39-2:43:verifier:Leaking this in constructor");
    }

    public void testReportInAssignment() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { B.x = this; }\n" +
                       "}\n" +
                       "class B { public static Object x; }")
                .run(LeakingThisInConstructor.class)
                .assertWarnings("2:20-2:30:verifier:Leaking this in constructor");
    }

    public void testDoNotReportAssignmentInMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void foo() { B.x = this; }\n" +
                       "}\n" +
                       "class B { public static Object x; }")
                .run(LeakingThisInConstructor.class)
                .assertWarnings();
    }

    public void testReportInAssignmentCheckForThis1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int a) { B.x = a; }\n" +
                       "}\n" +
                       "class B { public static Object x; }")
                .run(LeakingThisInConstructor.class)
                .assertWarnings();
    }

    public void testReportInAssignmentCheckForThis2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test() { B.x = B.y; }\n" +
                       "}\n" +
                       "class B { public static Object x; public static Object y; }")
                .run(LeakingThisInConstructor.class)
                .assertWarnings();
    }
}
