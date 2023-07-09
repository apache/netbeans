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
import static org.netbeans.modules.j2ee.ejbverification.TestBase.copyStringToFileObject;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SBSuperClassNotSBTest extends TestBase {

    private static final String SUPER_CLASS = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class SuperTestBean {\n"
            + "  public void anything() { }"
            + "}";
    private static final String TEST_BEAN = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean extends SuperTestBean {\n"
            + "}";
    private static final String TEST_BEAN_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean extends SuperTestBean {\n"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 extends SuperTestBean {\n"
            + "}";

    public SBSuperClassNotSBTest(String name) {
        super(name);
    }

    public void createSuperclass(TestModule testModule) throws Exception {
        FileObject localIfaces = FileUtil.createData(testModule.getSources()[0], "test/SuperTestBean.java");
        copyStringToFileObject(localIfaces, SUPER_CLASS);
        RepositoryUpdater.getDefault().refreshAll(true, true, true, null, (Object[]) testModule.getSources());
    }

    public void testSBSuperClassNotSB() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createSuperclass(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN)
                .run(SBSuperClassNotSB.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.SBSuperClassNotSB_err());
    }

    public void testSBSuperClassNotSBMoreBeansInFile() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createSuperclass(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_CLASSES)
                .run(SBSuperClassNotSB.class)
                .assertWarnings("2:13-2:21:error:" + Bundle.SBSuperClassNotSB_err(),
                                        "5:6-5:15:error:" + Bundle.SBSuperClassNotSB_err());
    }
}
