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
package org.netbeans.jellytools.modules.j2ee;

import junit.framework.TestSuite;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCaseGlassfishTest extends NbTestCase {

    public J2eeTestCaseGlassfishTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new J2eeTestCaseGlassfishTest("testAddEmptyTestIntoEmptyConfiguration"));
        suite.addTest(new J2eeTestCaseGlassfishTest("testGlassfishWithoutDomain"));
        suite.addTest(new J2eeTestCaseGlassfishTest("testGlassfishWithDomain"));
        suite.addTest(new J2eeTestCaseGlassfishTest("testCreateAllModulesServerSuiteWithoutFiles"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        J2eeTestCase.isSelfTest = true;
        System.clearProperty("tomcat.home");
        System.clearProperty("jboss.home");
        System.clearProperty("glassfish.home");
        System.clearProperty("org.netbeans.modules.j2ee.jboss4.installRoot");
        System.clearProperty("testA");
        System.clearProperty("testB");
    }

    public void testAddEmptyTestIntoEmptyConfiguration() {
        Configuration conf = NbModuleSuite.emptyConfiguration();
        conf = J2eeTestCase.addServerTests(ANY, conf, TD.class).gui(false);
        Test t = conf.suite();
        t.run(new TestResult());
        assertEquals("just one empty test", 1, t.countTestCases());
        assertNull("testA was not running", System.getProperty("testA"));
    }

    public void testGlassfishWithoutDomain() throws Exception {
        setGlassfishHome(false);
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(GLASSFISH, conf, "testA", "testB").gui(false);
        Test t = conf.suite();
        t.run(new TestResult());
        assertEquals("just empty test", 1, t.countTestCases());
    }

    public void testGlassfishWithDomain() throws IOException {
        setGlassfishHome(true);
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(GLASSFISH, conf, "testA", "testB").gui(false);
        Test t = conf.suite();
        t.run(new TestResult());
        assertEquals("both tests", 2, t.countTestCases());
    }

    public void testCreateAllModulesServerSuiteWithoutFiles() throws IOException {
        setGlassfishHome(true);
        Test t = J2eeTestCase.createAllModulesServerSuite(ANY, TD.class);
        t.run(new TestResult());
        assertEquals("both tests", 3, t.countTestCases());
        assertEquals("testA was running", "AAA", System.getProperty("testA"));
    }

    private void setGlassfishHome(boolean withDomain) throws IOException {
        System.setProperty("glassfish.home", getWorkDirPath());
        if (withDomain) {
            new File(getWorkDir(), "glassfish/domains/domain1").mkdirs();
        }
    }

    public static class TD extends J2eeTestCase {

        public TD(String str) {
            super(str);
        }

        public void testA() {
            System.setProperty("testA", "AAA");
        }

        public void testB() {
            System.setProperty("testB", "BBB");
        }
    }
}
