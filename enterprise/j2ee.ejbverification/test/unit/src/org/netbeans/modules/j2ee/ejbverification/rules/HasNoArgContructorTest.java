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
package org.netbeans.modules.j2ee.ejbverification.rules;

import org.netbeans.modules.j2ee.ejbverification.HintTestBase;
import org.netbeans.modules.j2ee.ejbverification.TestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class HasNoArgContructorTest extends TestBase {

    public HasNoArgContructorTest(String name) {
        super(name);
    }

    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  private TestBean() { }"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  private TestBean() { }"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 {\n"
            + "  private TestBean2() { }"
            + "}";

    public void testHasNoArgContructor() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(HasNoArgContructor.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.HasNoArgContructor_err());
    }

    public void testHasNoArgContructorMoreBeansInFile() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(HasNoArgContructor.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.HasNoArgContructor_err(),
                                        "5:6-5:15:error:" + Bundle.HasNoArgContructor_err());
    }
}
