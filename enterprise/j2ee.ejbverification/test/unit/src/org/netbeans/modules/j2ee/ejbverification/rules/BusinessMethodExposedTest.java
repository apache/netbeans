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
import static org.netbeans.modules.j2ee.ejbverification.TestBase.copyStringToFileObject;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class BusinessMethodExposedTest extends TestBase {

    public BusinessMethodExposedTest(String name) {
        super(name);
    }

    private static final String IFACE = "package test;\n"
            + "@javax.ejb.Local\n"
            + "public interface One {\n"
            + "}";
    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements One{\n"
            + "  public void anything() { }"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements One{\n"
            + "  public void anything() { }"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 implements One{\n"
            + "  public void anything() { }"
            + "}";

    public void createInterface(TestBase.TestModule testModule) throws Exception {
        FileObject iface = FileUtil.createData(testModule.getSources()[0], "test/One.java");
        copyStringToFileObject(iface, IFACE);
        RepositoryUpdater.getDefault().refreshAll(true, true, true, null, (Object[]) testModule.getSources());
    }

    public void testBusinessMethodExposed() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterface(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(BusinessMethodExposed.class)
                .assertWarnings("3:14-3:22:hint:" + Bundle.BusinessMethodExposed_hint());
    }

    public void testBusinessMethodExposedMoreBeansInFile() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterface(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(BusinessMethodExposed.class)
                .assertWarnings("3:14-3:22:hint:" + Bundle.BusinessMethodExposed_hint(),
                                        "6:14-6:22:hint:" + Bundle.BusinessMethodExposed_hint());
    }
}
