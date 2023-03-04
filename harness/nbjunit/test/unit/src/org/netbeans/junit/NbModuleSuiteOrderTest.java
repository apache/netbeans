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
