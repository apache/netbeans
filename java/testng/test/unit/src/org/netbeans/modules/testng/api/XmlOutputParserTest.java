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
