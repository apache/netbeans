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
public class ValueNotSpecifiedForRemoteAnnotationInterfaceTest extends TestBase {

    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Remote(TestIface.class)\n"
            + "public interface TestIface {\n"
            + "}";

    public ValueNotSpecifiedForRemoteAnnotationInterfaceTest(String name) {
        super(name);
    }

    public void testValueNotSpecifiedForRemoteAnnotationInterface_err() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestIface.java", TEST_BEAN)
                .run(ValueNotSpecifiedForRemoteAnnotationInterface.class)
                .assertWarnings("2:17-2:26:error:" + Bundle.ValueNotSpecifiedForRemoteAnnotationInterface_err());
    }
}
