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
public class BMnotPartOfRBIandLBITest extends TestBase {

    private static final String IFACE_REMOTE = "package test;\n"
            + "@javax.ejb.Remote\n"
            + "public interface RemoteOne {\n"
            + "  void anything();\n"
            + "}";
    private static final String IFACE_LOCAL = "package test;\n"
            + "@javax.ejb.Local\n"
            + "public interface LocalOne {\n"
            + "  void anything();\n"
            + "}";
    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements LocalOne, RemoteOne {\n"
            + "  public void anything() {}\n"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean implements LocalOne, RemoteOne {\n"
            + "  public void anything() {}\n"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 implements LocalOne, RemoteOne {\n"
            + "  public void anything() {}\n"
            + "}";

    public BMnotPartOfRBIandLBITest(String name) {
        super(name);
    }

    public void createInterfaces(TestModule testModule) throws Exception {
        FileObject localIfaces = FileUtil.createData(testModule.getSources()[0], "test/LocalOne.java");
        copyStringToFileObject(localIfaces, IFACE_LOCAL);
        FileObject remoteIfaces = FileUtil.createData(testModule.getSources()[0], "test/RemoteOne.java");
        copyStringToFileObject(remoteIfaces, IFACE_REMOTE);
        RepositoryUpdater.getDefault().refreshAll(true, true, true, null, (Object[]) testModule.getSources());
    }

    public void testBMnotPartOfRBIandLBI() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterfaces(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(BMnotPartOfRBIandLBI.class)
                .assertWarnings("2:13-2:21:warning:" + Bundle.BMnotPartOfRBIandLBI_err());
    }

    public void testBMnotPartOfRBIandLBIMoreBeansInFile() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createInterfaces(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(BMnotPartOfRBIandLBI.class)
                .assertWarnings("2:13-2:21:warning:" + Bundle.BMnotPartOfRBIandLBI_err(),
                                        "6:6-6:15:warning:" + Bundle.BMnotPartOfRBIandLBI_err());
    }

}