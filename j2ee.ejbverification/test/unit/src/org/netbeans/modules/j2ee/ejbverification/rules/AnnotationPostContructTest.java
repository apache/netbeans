/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbverification.rules;

import static junit.framework.Assert.assertNotNull;
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
