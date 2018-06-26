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
public class PersistentTimerInEjbLiteTest extends TestBase {

    public PersistentTimerInEjbLiteTest(String name) {
        super(name);
    }

    private String getTestBeanContent(boolean persistent) {
        return "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}";
    }

    private String getTestBeanMoreClassesContent(boolean persistent) {
        return "package test;\n"
            + "@javax.ejb.Stateless\n"
            + "public class TestBean {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}\n"
            + "@javax.ejb.Stateless\n"
            + "class TestBean2 {\n"
            + "  @javax.ejb.Schedule(persistent = " + String.valueOf(persistent) + ")\n"
            + "  public void myTimer() {}\n"
            + "}";
    }

    public void testNonPersistentTimerEE6Lite() throws Exception {
        TestBase.TestModule testModule = createWeb30Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_timer_in_ee6lite());
    }

    public void testPersistentTimerEE7Lite() throws Exception {
        TestBase.TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(true))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite());
    }

    public void testPersistentTimerEE7LiteMoreBeansInFile() throws Exception {
        TestBase.TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanMoreClassesContent(true))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings("4:14-4:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite(),
                                        "9:14-9:21:error:" + Bundle.PersistentTimerInEjbLite_err_nonpersistent_timer_in_ee7lite());
    }

    public void testNonPersistentTimerEE6Full() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings();
    }

    public void testNonPersistentTimerEE7Full() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        HintTestBase.create(testModule.getSources()[0])
                .input("test/TestBean.java", getTestBeanContent(false))
                .run(PersistentTimerInEjbLite.class)
                .assertWarnings();
    }
}
