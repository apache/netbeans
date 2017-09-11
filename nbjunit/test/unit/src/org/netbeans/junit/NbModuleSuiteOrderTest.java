/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.junit;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class NbModuleSuiteOrderTest extends TestCase {
    static {
        System.setProperty("org.netbeans.junit.level", "FINER");
        System.setProperty("org.openide.util.lookup.init.level", "FINER");
    }

    private static final String TEST_ORDER_COUNTER = "order";

    public void testTestOrder(){
        System.setProperty("order", "1");
        Test instance =
                NbModuleSuite.emptyConfiguration().gui(false)
                .addTest(TT2.class, "testOne")
                .addTest(SS.class)
                .addTest(TT2.class, "testTwo").suite();
        junit.textui.TestRunner.run(instance);

        NbModuleSuiteTest.assertProperty("t.one", "2");
        NbModuleSuiteTest.assertProperty("s.one", "3");
        NbModuleSuiteTest.assertProperty("s.two", "4");
        NbModuleSuiteTest.assertProperty("t.two", "5");
        NbModuleSuiteTest.assertProperty(TEST_ORDER_COUNTER, "5");
    }

    public void testDontRunAllTests(){
        System.setProperty("order", "1");
        System.setProperty("t.two", "-1");
        Test instance =
                NbModuleSuite.createConfiguration(TT2.class).gui(false)
                .addTest(SS.class)
                .addTest(TT2.class, "testOne").suite();
        junit.textui.TestRunner.run(instance);

        NbModuleSuiteTest.assertProperty("s.one", "2");
        NbModuleSuiteTest.assertProperty("s.two", "3");
        NbModuleSuiteTest.assertProperty("t.one", "4");
        NbModuleSuiteTest.assertProperty("t.two", "-1");
        NbModuleSuiteTest.assertProperty(TEST_ORDER_COUNTER, "4");
    }

    public void testStaticOrder(){
        System.setProperty("order", "1");
        String[] methods = new String[]{"testTwo", "testOne"};
        Test instance = NbModuleSuite.createConfiguration(TT2.class).clusters(".*").enableModules(".*").gui(false).addTest(methods).suite();
        junit.textui.TestRunner.run(instance);

        NbModuleSuiteTest.assertProperty("t.two", "2");
        NbModuleSuiteTest.assertProperty("t.one", "3");
        NbModuleSuiteTest.assertProperty(TEST_ORDER_COUNTER, "3");
    }

    public void testStaticOrderInConfiguration(){
        System.setProperty("order", "1");
        String[] methods = new String[]{"testTwo", "testOne"};
        Test instance = NbModuleSuite.createConfiguration(TT2.class).gui(false).addTest(methods).suite();
        junit.textui.TestRunner.run(instance);

        NbModuleSuiteTest.assertProperty("t.two", "2");
        NbModuleSuiteTest.assertProperty("t.one", "3");
        NbModuleSuiteTest.assertProperty(TEST_ORDER_COUNTER, "3");
    }

    public void testTestCaseOrder(){
        System.setProperty("order", "1");
        Test instance =
                NbModuleSuite.emptyConfiguration().gui(false)
                .addTest(TT2.class, "testOne")
                .addTest(TT3.class)
                .addTest(TT2.class, "testTwo").suite();
        junit.textui.TestRunner.run(instance);

        NbModuleSuiteTest.assertProperty("t.one", "2");
        NbModuleSuiteTest.assertProperty("t3.one", "3");
        NbModuleSuiteTest.assertProperty("t.two", "4");
        NbModuleSuiteTest.assertProperty(TEST_ORDER_COUNTER, "4");
    }

    public static class SS extends NbTestSuite {

        public SS() {
            addTest(new TT("s.one"));
            addTest(new TT("s.two"));
        }

        public static class TT extends TestCase {

            private String name;

            public TT(String name) {
                super(name);
                this.name = name;
            }

            @Override
            public void runTest() {
                execute(name);
            }
        }
    }

    public static class TT2 extends TestCase {

        public TT2(String name) {
            super(name);
        }
        
        public void testOne(){
            execute("t.one");
        }

        public void testTwo(){
            execute("t.two");
        }
    }

    public static class TT3 extends TestCase {

        public TT3(String name) {
            super(name);
        }
        
        public void testOne(){
            execute("t3.one");
        }

    }

    private static void execute(String name) {
        String str = System.getProperty(TEST_ORDER_COUNTER, "0");
        String newValue = Integer.toString(Integer.parseInt(str) + 1);
        System.setProperty(name, newValue);
        System.setProperty(TEST_ORDER_COUNTER, newValue);
    }
}
