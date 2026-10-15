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
public class AnnotationPostContructTest extends TestBase {

    private static final String TEST_BEAN_OK = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod() {}\n"
            + "}";

    private static final String TEST_BEAN_TO_MANY_ANNOTATIONS = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod() {}\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod2() {}\n"
            + "}";

    private static final String TEST_BEAN_CHECKED_EXCEPTION = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod() throws Exception {}\n"
            + "}";

    private static final String TEST_BEAN_NPE = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod() throws NullPointerException {}\n"
            + "}";

    private static final String TEST_BEAN_RETURN_TYPE = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public String businessMethod() {return null;}\n"
            + "}";

    private static final String TEST_BEAN_WRONG_PARAM = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod(String param) {}\n"
            + "}";

    private static final String TEST_BEAN_CORRECT_PARAM = "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "@javax.ejb.LocalBean\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public void businessMethod(Any aaa) {}\n"
            + "public static class Any implements javax.interceptor.InvocationContext {\n" +
                "        public Object getTarget() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public Object getTimer() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public java.lang.reflect.Method getMethod() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public java.lang.reflect.Constructor<?> getConstructor() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public Object[] getParameters() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public void setParameters(Object[] params) {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public java.util.Map<String, Object> getContextData() {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "        public Object proceed() throws Exception {\n" +
                "            throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n" +
                "        }\n" +
                "    }"
            + "}";

    private static final String TEST_BEAN_MORE_ERRORS = "package test;\n"
            + "public class TestBean {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public String businessMethod() {return null;}\n"
            + "}"
            + "class TestBean2 {\n"
            + "  @javax.annotation.PostConstruct\n"
            + "  public String businessMethod() {return null;}\n"
            + "}";

    public AnnotationPostContructTest(String name) {
        super(name);
    }

    public void testAnnotationPostContructCorrectUsage() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_OK)
                .run(AnnotationPostContruct.class)
                .assertWarnings();
    }

    public void testAnnotationPostContructThowringException() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_CHECKED_EXCEPTION)
                .run(AnnotationPostContruct.class)
                .assertWarnings("5:14-5:28:error:" + Bundle.AnnotationPostContruct_thrown_checked_exceptions());
    }

    public void testAnnotationPostContructThowringRuntimeException() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_NPE)
                .run(AnnotationPostContruct.class)
                .assertWarnings();
    }

    public void testAnnotationPostContructWrongReturnType() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_RETURN_TYPE)
                .run(AnnotationPostContruct.class)
                .assertWarnings("5:16-5:30:error:" + Bundle.AnnotationPostContruct_wrong_return_type());
    }

    public void testAnnotationPostContructWrongParameter() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_WRONG_PARAM)
                .run(AnnotationPostContruct.class)
                .assertWarnings("5:14-5:28:error:" + Bundle.AnnotationPostContruct_wrong_parameters());
    }

    public void testAnnotationPostContructCorrectParameter() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_CORRECT_PARAM)
                .run(AnnotationPostContruct.class)
                .assertWarnings();
    }

    public void testAnnotationPostContructMoreAnnotations() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_TO_MANY_ANNOTATIONS)
                .run(AnnotationPostContruct.class)
                .assertWarnings("5:14-5:28:error:" + Bundle.AnnotationPostContruct_too_much_annotations(), "7:14-7:29:error:" + Bundle.AnnotationPostContruct_too_much_annotations());
    }

    public void testAnnotationPostContructMoreClasses() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", TEST_BEAN_MORE_ERRORS)
                .run(AnnotationPostContruct.class)
                .assertWarnings("3:16-3:30:error:" + Bundle.AnnotationPostContruct_wrong_return_type(),
                                        "6:16-6:30:error:" + Bundle.AnnotationPostContruct_wrong_return_type());
    }
}
