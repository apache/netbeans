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
public class LegalModifiersTest extends TestBase {

    public LegalModifiersTest(String name) {
        super(name);
    }
    private static final String TEST_BEAN1 = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean1 {\n"
            + "  public TestBean1() { }"
            + "}";
    private static final String TEST_BEAN2 = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public abstract class TestBean2 {\n"
            + "  public TestBean2() { }"
            + "}";
    private static final String TEST_BEAN3 = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public final class TestBean3 {\n"
            + "  public TestBean3() { }"
            + "}";
    private static final String TEST_BEAN3_MORE_CLASSES = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public final class TestBean3 {\n"
            + "  public TestBean3() { }"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "final class TestBean4 {\n"
            + "  public TestBean4() { }"
            + "}";

    public void testLegalModifiersNotPublic() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean1.java", TEST_BEAN1)
                .run(LegalModifiers.class)
                .assertWarnings("2:6-2:15:error:" + Bundle.LegalModifiers_BeanClassMustBePublic());
    }

    public void testLegalModifiersAbstract() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean2.java", TEST_BEAN2)
                .run(LegalModifiers.class)
                .assertWarnings("2:22-2:31:error:" + Bundle.LegalModifiers_BeanClassNotBeAbstract());
    }

    public void testLegalModifiersFinal() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean3.java", TEST_BEAN3)
                .run(LegalModifiers.class)
                .assertWarnings("2:19-2:28:error:" + Bundle.LegalModifiers_BeanClassNotBeFinal());
    }

    public void testLegalModifiersFinalMoreBeansInFile() throws Exception {
        TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean3.java", TEST_BEAN3_MORE_CLASSES)
                .run(LegalModifiers.class)
                .assertWarnings("2:19-2:28:error:" + Bundle.LegalModifiers_BeanClassNotBeFinal(),
                                "5:12-5:21:error:" + Bundle.LegalModifiers_BeanClassMustBePublic(),
                                "5:12-5:21:error:" + Bundle.LegalModifiers_BeanClassNotBeFinal());
    }
}
