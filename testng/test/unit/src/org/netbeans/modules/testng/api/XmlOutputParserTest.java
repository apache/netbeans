/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.testng.api;

import java.io.*;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionType;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author lukas
 */
@Test
public class XmlOutputParserTest extends NbTestCase {

    public XmlOutputParserTest(String name) {
        super(name);
    }

    public void testParseSimpleXmlOutput() throws Exception {
        XmlResult suite = parseResultXML(new File(getDataDir(), "results/testng-results.xml"));
        assertEquals("Ant suite", suite.getName());
        List<TestNGTest> testNGTests = suite.getTestNGTests();
        assertEquals(2, testNGTests.size());
        assertEquals("Ant test", testNGTests.get(0).getName());
        assertEquals("", testNGTests.get(1).getName());
        List<TestNGTestSuite> testSuites = suite.getTestSuites();
        assertEquals(7, testSuites.size());

        TestNGTestSuite result = testSuites.get(0);
        assertEquals("test.FailPassSkipTest", result.getName());
        List<Testcase> testcases = result.getTestcases();
        assertEquals(3, testcases.size());
        Testcase[] tcs = testcases.toArray(new Testcase[3]);
        assertEquals("test.FailPassSkipTest.cTest", tcs[1].getName());
//        assertEquals(1, result.failures);
//        assertEquals(1, result.skips);
//        assertEquals(3, result.totalTests);
//        assertEquals(1, result.detectedPassedTests);
//        assertEquals(0, result.confSkips);
//        assertEquals(0, result.confFailures);
//        assertNotNull(tcs[1].trouble);
//        assertFalse(tcs[1].trouble.isFailure());
//        assertNotNull(tcs[0].trouble);
//        assertTrue(tcs[0].trouble.isFailure());
//
        result = testSuites.get(5);
        assertEquals("test.SetUpTest", result.getName());
        testcases = result.getTestcases();
        assertEquals(3, testcases.size());
        tcs = testcases.toArray(new Testcase[3]);
        assertEquals("test.SetUpTest.setUp", tcs[0].getName());
//        assertEquals(0, result.failures);
//        assertEquals(1, result.skips);
//        assertEquals(1, result.totalTests);
//        assertEquals(0, result.detectedPassedTests);
//        assertEquals(1, result.confSkips);
//        assertEquals(1, result.confFailures);
//        assertNotNull(tcs[0].trouble);
//        assertTrue(tcs[0].trouble.isFailure());
//        assertNotNull(tcs[1].trouble);
//        assertFalse(tcs[1].trouble.isFailure());
    }

    public void testParseXmlOutput() throws Exception {
        XmlResult reports = parseResultXML(new File(getDataDir(), "results/testng-results_1.xml"));
        assertEquals(22, reports.getTestNGTests().size());
        List<TestNGTestSuite> testSuites = reports.getTestSuites();
        assertEquals(71, testSuites.size());
    }

    public void testParseXmlOutput2() throws Exception {
        XmlResult reports = parseResultXML(new File(getDataDir(), "results/testng-results_2.xml"));
        List<TestNGTestSuite> testSuites = reports.getTestSuites();
        assertEquals(1, testSuites.size());
        int tc = 0;
        for (TestNGTestSuite s:testSuites) {
            tc += s.getTestcases().size();
        }
        assertEquals(6, tc);
    }

    XmlResult parseResultXML(File f) throws IOException, SAXException {
        Reader reader = new BufferedReader(new FileReader(f));
        TestSession ts = new TestSession("test", new P(), SessionType.TEST);
        return XmlOutputParser.parseXmlOutput(reader, ts);
    }

    private class P implements Project {

        public FileObject getProjectDirectory() {
            FileObject projectDir = null;
            try {
                projectDir = FileUtil.toFileObject(getWorkDir());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return projectDir;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
