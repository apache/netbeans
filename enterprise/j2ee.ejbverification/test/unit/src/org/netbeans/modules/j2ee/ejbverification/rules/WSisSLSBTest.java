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
public class WSisSLSBTest extends TestBase {

    private static final String TEST_BEAN = "package test;\n"
            + "@javax.jws.WebService\n"
            + "public class TestBean {\n"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.jws.WebService\n"
            + "public class TestBean {\n"
            + "}\n"
            + "@javax.jws.WebService\n"
            + "class TestBean2 {\n"
            + "}";

    public WSisSLSBTest(String name) {
        super(name);
    }

    public void testWSisSLSB() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(WSisSLSB.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.WSisSLSB_err());
    }

    public void testWSisSLSBMoreBeansInFile() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(WSisSLSB.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.WSisSLSB_err(),
                                        "5:6-5:15:error:" + Bundle.WSisSLSB_err());
    }
}
