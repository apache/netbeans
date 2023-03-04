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

package org.netbeans.modules.php.phpunit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;

/**
 * @author Tomas Mysik
 */
public class PhpUnitLogParserTest extends NbTestCase {

    public PhpUnitLogParserTest(String name) {
        super(name);
    }

    public void testParseLogWithMoreSuites() throws Exception {
        Reader reader = createReader("phpunit-log-more-suites.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertEquals(64, testSession.getTime());
        assertEquals(6, testSession.getTests());

        // test suites & test cases
        assertEquals(3, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("Calculator2Test", testSuite.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testSuite.getFile());
        assertEquals(11, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("testAdd", testCase.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testCase.getFile());
        assertEquals(43, testCase.getLine());
        assertEquals(11, testCase.getTime());

        // 2nd - pending test suite
        testSuite = testSession.getTestSuites().get(1);
        assertEquals("NoTestClassTest", testSuite.getName());
        assertEquals(1, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(0);
        assertEquals(TestCase.Status.SKIPPED, testCase.getStatus());
        assertFalse(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(0, testCase.getStackTrace().length);

        // 3rd
        testSuite = testSession.getTestSuites().get(2);
        assertEquals("CalculatorTest", testSuite.getName());
        assertEquals(5, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(1);
        assertEquals("testAdd2", testCase.getName());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("Failed asserting that two objects are equal.\n--- Expected\n+++ Actual\n@@ -1,3 +1 @@\n-MyObject Object\n-(\n-)\n+77\n\\ Chybi znak konce radku na konci souboru", testCase.getStackTrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:56", testCase.getStackTrace()[1]);

        testCase = testSuite.getTestCases().get(2);
        assertEquals("testAdd3", testCase.getName());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("my expected message\nFailed asserting that two strings are equal.\nexpected string <hello>\ndifference      < x???>\ngot string      <hi>", testCase.getStackTrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:64", testCase.getStackTrace()[1]);

        testCase = testSuite.getTestCases().get(3);
        assertEquals("testAdd4", testCase.getName());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("Failed asserting that <integer:2> matches expected value <integer:3>.", testCase.getStackTrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:75", testCase.getStackTrace()[1]);

        testCase = testSuite.getTestCases().get(4);
        assertEquals("testAdd5", testCase.getName());
        assertEquals(TestCase.Status.ERROR, testCase.getStatus());
        assertEquals(3, testCase.getStackTrace().length);
        assertEquals("Exception: my exception", testCase.getStackTrace()[0]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/src/Calculator.php:13", testCase.getStackTrace()[1]);
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/CalculatorTest.php:82", testCase.getStackTrace()[2]);
    }

    public void testParseLogWithOneSuite() throws Exception {
        Reader reader = createReader("phpunit-log-one-suite.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertEquals(10, testSession.getTime());
        assertEquals(1, testSession.getTests());

        // test suites & test cases
        assertEquals(1, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("Calculator2Test", testSuite.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testSuite.getFile());
        assertEquals(10, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("Calculator2Test", testCase.getClassName());
        assertEquals("testAdd", testCase.getName());
        assertEquals("/home/gapon/NetBeansProjects/PhpProject01/tests/hola/Calculator2Test.php", testCase.getFile());
        assertEquals(43, testCase.getLine());
        assertEquals(10, testCase.getTime());
    }

    public void testParseLogIssue157846() throws Exception {
        Reader reader = createReader("phpunit-log-issue157846.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(1, testSession.getTestSuites().size());
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("integration_REST_C_CustomersTest", testSuite.getName());

        assertSame(1, testSuite.getTestCases().size());
        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("testCheckNewRecord", testCase.getName());

        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertSame(1, testCase.getStackTrace().length);
    }

    public void testParseLogIssue159876() throws Exception {
        Reader reader = createReader("phpunit-log-issue159876.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(2, testSession.getTestSuites().size());

        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("LoginTest: Firefox on Windows", testSuite.getName());
        assertEquals("/Library/WebServer/Documents/acalog/tests/EmptyTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("testLogin", testCase.getName());

        testSuite = testSession.getTestSuites().get(1);
        assertEquals("LoginTest: Internet Explorer on Windows", testSuite.getName());
        assertEquals("/Library/WebServer/Documents/acalog/tests/EmptyTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testLogin", testCase.getName());
    }

    public void testParseLogIssue169433() throws Exception {
        Reader reader = createReader("phpunit-log-issue169433.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(4, testSession.getTestSuites().size());
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("E2_ConfigTest", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/E2_ConfigTest.php", testSuite.getFile());

        assertSame(4, testSuite.getTestCases().size());
        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("test__get", testCase.getName());
        testCase = testSuite.getTestCases().get(3);
        assertEquals("testIterator", testCase.getName());

        testSuite = testSession.getTestSuites().get(1);
        assertEquals("E2_ConfigTest::testConstructException", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/E2_ConfigTest.php", testSuite.getFile());

        assertSame(3, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testConstructException with data set #0", testCase.getName());
        testCase = testSuite.getTestCases().get(2);
        assertEquals("testConstructException with data set #2", testCase.getName());

        testSuite = testSession.getTestSuites().get(3);
        assertEquals("E2_Crypt_EncryptTest", testSuite.getName());
        assertEquals("/home/gapon/tmp/buga/SvnIPMCore/ipmcore/tests/ipmcore/lib/e2/E2/Crypt/E2_Crypt_McryptTest.php", testSuite.getFile());

        assertSame(1, testSuite.getTestCases().size());
        testCase = testSuite.getTestCases().get(0);
        assertEquals("testDecryption", testCase.getName());
    }

    public void testParseLogIssue198920() throws Exception {
        Reader reader = createReader("phpunit-log-issue198920.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(3, testSession.getTestSuites().size());
        assertSame(2, testSession.getTestSuites().get(0).getTestCases().size());
        assertSame(3, testSession.getTestSuites().get(1).getTestCases().size());
        assertSame(1, testSession.getTestSuites().get(2).getTestCases().size());
    }

    public void testParseLogIssue200503() throws Exception {
        Reader reader = createReader("phpunit-log-issue200503.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(3, testSession.getTestSuites().size());
        TestSuiteVo firstSuite = testSession.getTestSuites().get(0);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testSubstituteWorksAsItHasTo", firstSuite.getName());
        assertSame(3, firstSuite.getTestCases().size());
        TestSuiteVo secondSuite = testSession.getTestSuites().get(1);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testSubstitutionFails", secondSuite.getName());
        assertSame(3, secondSuite.getTestCases().size());
        TestSuiteVo thirdSuite = testSession.getTestSuites().get(2);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testUrlConcatContextWorksAsItHasTo", thirdSuite.getName());
        assertSame(2, thirdSuite.getTestCases().size());
    }

    public void testParseLogEmptySuite() throws Exception {
        Reader reader = createReader("phpunit-log-empty-suite.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(3, testSession.getTestSuites().size());
        TestSuiteVo firstSuite = testSession.getTestSuites().get(0);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testSubstituteWorksAsItHasTo", firstSuite.getName());
        assertSame(3, firstSuite.getTestCases().size());
        TestSuiteVo secondSuite = testSession.getTestSuites().get(1);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testSubstitutionFails", secondSuite.getName());
        assertSame(3, secondSuite.getTestCases().size());
        TestSuiteVo thirdSuite = testSession.getTestSuites().get(2);
        assertEquals("PrivatBankTest\\Root\\Helper\\StringTest::testUrlConcatContextWorksAsItHasTo", thirdSuite.getName());
        assertSame(1, thirdSuite.getTestCases().size());
        TestCaseVo testCase = thirdSuite.getTestCases().get(0);
        assertEquals("No valid test cases found.", testCase.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase.getStatus());
    }

    public void testParseLogEmptySuiteName() throws Exception {
        Reader reader = createReader("phpunit-log-phpunit56.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(46, testSession.getTestSuites().size());
        int tests = 0;
        for (TestSuiteVo testSuite : testSession.getTestSuites()) {
            tests += testSuite.getPureTestCases().size();
        }
        assertEquals(1465, tests);
    }

    public void testParseLogNETBEANS1851() throws Exception {
        Reader reader = createReader("phpunit-log-netbeans1851.xml");
        TestSessionVo testSession = new TestSessionVo(null);

        PhpUnitLogParser.parse(reader, testSession);

        assertSame(1, testSession.getTestSuites().size());
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("CalculatorTest", testSuite.getName());
        assertEquals("/tmp/Calculator-PHPUnit/test/src/CalculatorTest.php", testSuite.getFile());
        TestCaseVo testCase = testSuite.getTestCases().get(16);
        assertTrue(testCase.isError());
        assertEquals("Risky Test", testCase.getStackTrace()[0]);
    }

    private Reader createReader(String filename) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(getDataDir(), filename)));
    }

}
