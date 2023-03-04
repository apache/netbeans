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
package org.netbeans.modules.j2ee.ejbverification.rules;

import org.netbeans.modules.j2ee.ejbverification.HintTestBase;
import org.netbeans.modules.j2ee.ejbverification.TestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class AsynchronousMethodInvocationTest extends TestBase {

    private static final String TEST_BEAN = "package test;\n"
                + "@javax.ejb.Stateless\n"
                + "@javax.ejb.LocalBean\n"
                + "public class TestBean {\n"
                + "  @javax.ejb.Asynchronous\n"
                + "  public void businessMethod() {}\n"
                + "}";

    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
                + "@javax.ejb.Stateless\n"
                + "@javax.ejb.LocalBean\n"
                + "public class TestBean {\n"
                + "  @javax.ejb.Asynchronous\n"
                + "  public void businessMethod() {}\n"
                + "}"
                + "@javax.ejb.Stateless\n"
                + "@javax.ejb.LocalBean\n"
                + "class TestBean2 {\n"
                + "  @javax.ejb.Asynchronous\n"
                + "  public void businessMethod2() {}\n"
                + "}";

    public AsynchronousMethodInvocationTest(String name) {
        super(name);
    }

    public void testAsynchronousSBInvocationEE6Lite() throws Exception {
        TestModule testModule = createWeb30Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(AsynchronousMethodInvocation.class)
                .assertWarnings("5:14-5:28:error:" + Bundle.AsynchronousMethodInvocation_err_asynchronous_in_ejb31());
    }

    public void testAsynchronousSBInvocationEE6LiteMoreBeansInFile() throws Exception {
        TestModule testModule = createWeb30Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(AsynchronousMethodInvocation.class)
                .assertWarnings("5:14-5:28:error:" + Bundle.AsynchronousMethodInvocation_err_asynchronous_in_ejb31(),
                                        "10:14-10:29:error:" +  Bundle.AsynchronousMethodInvocation_err_asynchronous_in_ejb31());
    }

    public void testAsynchronousSBInvocationEE7Lite() throws Exception {
        TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(AsynchronousMethodInvocation.class)
                .assertWarnings();
    }

    public void testAsynchronousSBInvocationEE6Full() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(AsynchronousMethodInvocation.class)
                .assertWarnings();
    }

    public void testAsynchronousSBInvocationEE7Full() throws Exception {
        TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(AsynchronousMethodInvocation.class)
                .assertWarnings();
    }
}