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
public class PersistentTimerInEjbLiteTest extends TestBase {

    public PersistentTimerInEjbLiteTest(String name) {
        super(name);
    }

    private String getTestBeanContent(boolean persistent) {
        return "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}";
    }

    private String getTestBeanMoreClassesContent(boolean persistent) {
        return "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}";
    }

    public void testNonPersistentTimerEE6Lite() throws Exception {
        TestBase.TestModule testModule = createWeb30Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_timer_in_ee6lite());
    }

    public void testPersistentTimerEE7Lite() throws Exception {
        TestBase.TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(true))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite());
    }

    public void testPersistentTimerEE7LiteMoreBeansInFile() throws Exception {
        TestBase.TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanMoreClassesContent(true))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite(),
                                        "9:14-9:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite());
    }

    public void testNonPersistentTimerEE6Full() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings();
    }

    public void testNonPersistentTimerEE7Full() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings();
    }
}
