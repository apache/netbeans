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
