/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools.modules.j2ee;

import org.netbeans.junit.NbTestSuite;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestResult;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import static org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server.*;

/**
 *
 * @author Jindrich Sedek
 */
public class J2eeTestCaseTest extends JellyTestCase {

    public J2eeTestCaseTest(String name) {
        super(name);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new J2eeTestCaseTest("testSetUp"));
        suite.addTest(new J2eeTestCaseTest("testIsRegistered"));
        suite.addTest(new J2eeTestCaseTest("testNoServer"));
        suite.addTest(new J2eeTestCaseTest("testGlassfishPreferedFromTomcat"));
        suite.addTest(new J2eeTestCaseTest("testAnyServer"));
        suite.addTest(new J2eeTestCaseTest("testAnyServerByNames"));
        suite.addTest(new J2eeTestCaseTest("testTomcatSByNames"));
        suite.addTest(new J2eeTestCaseTest("testCreateAllModulesServerSuite"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        J2eeTestCase.isSelfTest = true;
        System.clearProperty("tomcat.home");
        System.clearProperty("jboss.home");
        System.clearProperty("glassfish.home");
        System.clearProperty("testA");
    }

    public void testSetUp() throws Exception {
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);
        new J2eeTestCase("test").setUp();
        String[] files = tmpDir.list();
        int count = 0;
        for (String file : files) {
            if (file.startsWith("J2EE_TEST_CASE_PID_FILE")) {
                count++;
            }
        }
        assertEquals("just one file", 1, count);
    }

    public void testIsRegistered() {
        try {
            J2eeTestCase.isRegistered(ANY);
            fail("Exception should be thrown if isRegistered called in wrong context.");
        } catch (IllegalStateException e) {
            // OK
        }
    }

    public void testNoServer() {
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("just empty test - no server is registered", 1, t.countTestCases());
    }

    public void testGlassfishPreferedFromTomcat() throws IOException {
        System.setProperty("glassfish.home", getWorkDirPath());
        new File(getWorkDir(), "glassfish/domains/domain1").mkdirs();
        System.setProperty("tomcat.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        assertTrue("GlassFish not registered.", J2eeTestCase.isRegistered(GLASSFISH) && !J2eeTestCase.isRegistered(TOMCAT));
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 3, t.countTestCases());
    }

    public void testAnyServer() {
        System.setProperty("tomcat.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf).gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 3, t.countTestCases());
    }

    public void testAnyServerByNames() {
        System.setProperty("jboss.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(conf, "testA", "testB").gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 2, t.countTestCases());
    }

    public void testTomcatSByNames() {
        System.setProperty("tomcat.home", getDataDir().getPath());
        Configuration conf = NbModuleSuite.createConfiguration(TD.class);
        conf = J2eeTestCase.addServerTests(TOMCAT, conf, "testA", "testB").gui(false);
        Test t = NbModuleSuite.create(conf);
        t.run(new TestResult());
        assertEquals("both TD tests and emptyTest", 2, t.countTestCases());
    }

    public void testCreateAllModulesServerSuite() {
        System.setProperty("tomcat.home", getWorkDirPath());
        Test t = J2eeTestCase.createAllModulesServerSuite(ANY, TD.class, "testA", "testB");
        t.run(new TestResult());
        assertEquals("both tests", 2, t.countTestCases());
    }

    public static class TD extends J2eeTestCase {

        public TD(String str) {
            super(str);
        }

        public void testA() {
            System.setProperty("testA", "AAA");
        }

        public void testB() {
        }
    }
}
