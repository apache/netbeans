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
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class BeanHasDifferentLBIandRBITest extends TestBase {
    private static final String IFACE2 = "package test;\n"
            + "public interface Two {\n"
            + "}";
    private static final String IFACE = "package test;\n"
            + "@javax.ejb.Remote\n"
            + "@javax.ejb.Local\n"
            + "public interface One {\n"
            + "  void anything();\n"
            + "}";
    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements One, Two {\n"
            + "  public void anything() {}\n"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements One, Two {\n"
            + "  public void anything() {}\n"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 implements One, Two {\n"
            + "  public void anything() {}\n"
            + "}";

    public BeanHasDifferentLBIandRBITest(String name) {
        super(name);
    }

    public void createInterface(TestModule testModule) throws Exception {
        FileObject iface = FileUtil.createData(testModule.getSources()[0], "test/One.java");
        copyStringToFileObject(iface, IFACE);
        FileObject iface2 = FileUtil.createData(testModule.getSources()[0], "test/Two.java");
        copyStringToFileObject(iface2, IFACE2);
        RepositoryUpdater.getDefault().refreshAll(true, true, true, null, (Object[]) testModule.getSources());
    }

    public void testBMnotPartOfRBIandLBI() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterface(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(BeanHasDifferentLBIandRBI.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.BeanHasDifferentLBIandRBI_err());
    }

    public void testBMnotPartOfRBIandLBIMoreBeansInFile() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterface(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(BeanHasDifferentLBIandRBI.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.BeanHasDifferentLBIandRBI_err(),
                                        "6:6-6:15:error:" + Bundle.BeanHasDifferentLBIandRBI_err());
    }

}
