/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.codeception.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public class CodeceptionLogParserTest extends NbTestCase {

    public CodeceptionLogParserTest(String name) {
        super(name);
    }

    public void testParseLogWithOneCodeceptionSuite() throws Exception {
        Reader reader = createReader("codeception-log-one-codeception-suite.xml");
        TestSessionVo testSession = new TestSessionVo();

        CodeceptionLogParser.parse(reader, testSession);
        assertEquals(4, testSession.getTime());
        assertEquals(1, testSession.getTests());

        // test suites & test cases
        assertEquals(1, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("unit", testSuite.getName());
        assertEquals(null, testSuite.getLocation());
        assertEquals(4, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("App\\FizzBuzzTest", testCase.getClassName());
        assertEquals("testExec", testCase.getName());
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzzTest.php", testCase.getFile());
        assertEquals(-1, testCase.getLine());
        assertEquals(4, testCase.getTime());
    }

    public void testParseLogWithOnePhpUnitSuite() throws Exception {
        Reader reader = createReader("codeception-log-one-phpunit-suite.xml");
        TestSessionVo testSession = new TestSessionVo();

        CodeceptionLogParser.parse(reader, testSession);
        assertEquals(5, testSession.getTime());
        assertEquals(1, testSession.getTests());

        // test suites & test cases
        assertEquals(1, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("unit", testSuite.getName());
        assertEquals(null, testSuite.getLocation());
        assertEquals(5, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("App\\FizzBuzzPhpUnitTest", testCase.getClassName());
        assertEquals("testExec", testCase.getName());
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzzPhpUnitTest.php", testCase.getFile());
        assertEquals(17, testCase.getLine());
        assertEquals(5, testCase.getTime());
    }

    public void testParseLogWithMoreSuites() throws Exception {
        Reader reader = createReader("codeception-log-more-suites.xml");
        TestSessionVo testSession = new TestSessionVo();

        CodeceptionLogParser.parse(reader, testSession);

        assertEquals(268 + 175 + 19, testSession.getTime());
        assertEquals(4 + 4 + 1, testSession.getTests());

        assertEquals(3, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("functional", testSuite.getName());
        assertEquals(268, testSuite.getTime());
        assertEquals(4, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("About", testCase.getName());
        assertEquals("/home/junichi11/NetBeansProjects/yii2-codeception/tests/codeception/functional/AboutCept.php", testCase.getFile());
        assertEquals(-1, testCase.getLine());
        assertEquals(28, testCase.getTime());

        // 2nd
        testSuite = testSession.getTestSuites().get(1);
        assertEquals("unit", testSuite.getName());
        assertEquals(175, testSuite.getTime());
        assertEquals(4, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(0);
        assertEquals("testExec", testCase.getName());
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzz2Test.php", testCase.getFile());
        assertEquals(-1, testCase.getLine());
        assertEquals(56, testCase.getTime());

        testCase = testSuite.getTestCases().get(1);
        assertEquals("testFailure", testCase.getName());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("Failed asserting that two objects are equal.\n"
                + "--- Expected\n"
                + "+++ Actual\n"
                + "@@ @@\n"
                + " App\\FizzBuzz Object (\n"
                + "     'start' => 0\n"
                + "-    'end' => 200\n"
                + "+    'end' => 300\n"
                + " )",
                testCase.getStackTrace()[0]);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzz2Test.php:33", testCase.getStackTrace()[1]);

        testCase = testSuite.getTestCases().get(2);
        assertEquals("testFailure2", testCase.getName());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("Failed asserting that 2 matches expected 1.", testCase.getStackTrace()[0]);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzz2Test.php:37", testCase.getStackTrace()[1]);

        testCase = testSuite.getTestCases().get(3);
        assertEquals("testError", testCase.getName());
        assertTrue(testCase.isError());
        assertFalse(testCase.isFailure());
        assertEquals(TestCase.Status.ERROR, testCase.getStatus());
        assertEquals(2, testCase.getStackTrace().length);
        assertEquals("Exception: my exception", testCase.getStackTrace()[0]);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/unit/App/FizzBuzz2Test.php:42", testCase.getStackTrace()[1]);

        // 3rd
        testSuite = testSession.getTestSuites().get(2);
        assertEquals("acceptance", testSuite.getName());
        assertEquals(19, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        testCase = testSuite.getTestCases().get(0);
        assertEquals("Welcome", testCase.getName());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(3, testCase.getStackTrace().length);
        assertEquals("Failed asserting that   <bold>/</bold>\n"
                + "--> <info><!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <title>Home</title>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "        </body>\n"
                + "</html>\n"
                + "</info>\n"
                + "--> contains \"welcome\".",
                testCase.getStackTrace()[0]);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/_support/_generated/AcceptanceTesterActions.php:257", testCase.getStackTrace()[1]);
        assertEquals("/home/junichi11/NetBeansProjects/codeception/tests/acceptance/WelcomeCept.php:6", testCase.getStackTrace()[2]);
    }


    public void testParseLogWithWarningPhpUnitSuite() throws Exception {
        Reader reader = createReader("codeception-log-warning-phpunit-suite.xml");
        TestSessionVo testSession = new TestSessionVo();

        CodeceptionLogParser.parse(reader, testSession);
        assertEquals(20, testSession.getTime());
        assertEquals(1, testSession.getTests());

        // test suites & test cases
        assertEquals(1, testSession.getTestSuites().size());

        // 1st
        TestSuiteVo testSuite = testSession.getTestSuites().get(0);
        assertEquals("unit", testSuite.getName());
        assertEquals(null, testSuite.getLocation());
        assertEquals(20, testSuite.getTime());
        assertEquals(1, testSuite.getTestCases().size());

        TestCaseVo testCase = testSuite.getTestCases().get(0);
        assertEquals("ProjectX\\FooTest", testCase.getClassName());
        assertEquals("testGetBar", testCase.getName());
        assertEquals("/home/kacer/projectx/tests/unit/FooTest.php", testCase.getFile());
        assertEquals(6, testCase.getLine());
        assertEquals(20, testCase.getTime());
        assertTrue(testCase.isFailure());
        assertFalse(testCase.isError());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals(1, testCase.getStackTrace().length);
        assertEquals("Trying to configure method \"getBarAAA\" which cannot be configured because it does not exist, has not been specified, is final, or is static", testCase.getStackTrace()[0]);
    }

    private Reader createReader(String filename) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(getDataDir(), filename)));
    }

}
