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
package org.netbeans.modules.php.atoum.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;

public class TapParserTest extends NbTestCase {

    public TapParserTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap.log"), 1000L);
        assertEquals(3, suites.size());

        TestSuiteVo suite1 = suites.get(0);
        assertEquals("tests\\unit\\StdClass", suite1.getName());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", suite1.getFile());

        List<TestCaseVo> testCases1 = suite1.getTestCases();
        assertEquals(6, testCases1.size());

        TestCaseVo testCase1 = testCases1.get(0);
        assertEquals("testVoid", testCase1.getName());
        assertEquals(TestCase.Status.PENDING, testCase1.getStatus());
        assertNull(testCase1.getMessage());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase1.getFile());
        assertEquals(-1, testCase1.getLine());
        assertEquals(100L, testCase1.getTime());

        TestCaseVo testCase2 = testCases1.get(1);
        assertEquals("testFail", testCase2.getName());
        assertEquals(TestCase.Status.FAILED, testCase2.getStatus());
        assertEquals("object(stdClass) is not null; second line of the message", testCase2.getMessage());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase2.getFile());
        assertEquals(19, testCase2.getLine());
        assertEquals(100L, testCase2.getTime());

        TestCaseVo testCase3 = testCases1.get(2);
        assertEquals("testPass", testCase3.getName());
        assertEquals(TestCase.Status.PASSED, testCase3.getStatus());
        assertNull(testCase3.getMessage());
        assertNull(testCase3.getFile());
        assertEquals(-1, testCase3.getLine());
        assertEquals(100L, testCase3.getTime());

        TestCaseVo testCase4 = testCases1.get(3);
        assertEquals("testSkipped", testCase4.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase4.getStatus());
        assertEquals("This test was skipped", testCase4.getMessage());
        assertEquals(Arrays.asList("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php:29"), testCase4.getStackTrace());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase4.getFile());
        assertEquals(29, testCase4.getLine());
        assertEquals(100L, testCase4.getTime());

        TestCaseVo testCase5 = testCases1.get(4);
        assertEquals("testException", testCase5.getName());
        assertEquals(TestCase.Status.ERROR, testCase5.getStatus());
        assertEquals("exception 'RuntimeException' with message 'This test triggered a \\RuntimeException' in /home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php:41", testCase5.getMessage());
        assertEquals(Arrays.asList(
                "/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php(838): tests\\unit\\StdClass->testException()",
                "-(1): mageekguy\\atoum\\test->runTestMethod('testException')",
                "{main}",
                "/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php:838"), testCase5.getStackTrace());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/vendor/atoum/atoum/classes/test.php", testCase5.getFile());
        assertEquals(838, testCase5.getLine());
        assertEquals(100L, testCase5.getTime());

        TestCaseVo testCase6 = testCases1.get(5);
        assertEquals("testError", testCase6.getName());
        // XXX should be ERROR
        assertEquals(TestCase.Status.FAILED, testCase6.getStatus());
        assertEquals("This test triggered an error", testCase6.getMessage());
        assertEquals("/home/gapon/NetBeansProjects/atoum-sample/tests/unit/StdClass.php", testCase6.getFile());
        assertEquals(36, testCase6.getLine());
        assertEquals(100L, testCase6.getTime());

        TestSuiteVo suite2 = suites.get(1);
        assertEquals("tests\\unit\\atoum\\sample\\Foobar", suite2.getName());
        assertNull(suite2.getFile());

        List<TestCaseVo> testCases2 = suite2.getTestCases();
        assertEquals(3, testCases2.size());

        TestCaseVo testCase7 = testCases2.get(0);
        assertEquals("test__construct", testCase7.getName());
        assertEquals(TestCase.Status.PASSED, testCase7.getStatus());
        assertNull(testCase7.getFile());
        assertEquals(-1, testCase7.getLine());
        assertEquals(100L, testCase7.getTime());

        TestCaseVo testCase8 = testCases2.get(1);
        assertEquals("testGetFoo", testCase8.getName());
        assertEquals(TestCase.Status.PASSED, testCase8.getStatus());
        assertNull(testCase8.getFile());
        assertEquals(-1, testCase8.getLine());
        assertEquals(100L, testCase8.getTime());

        TestCaseVo testCase9 = testCases2.get(2);
        assertEquals("testIncomplete", testCase9.getName());
        assertEquals(TestCase.Status.ERROR, testCase9.getStatus());
        assertEquals("I died", testCase9.getMessage());
        assertNull(testCase9.getFile());
        assertEquals(-1, testCase9.getLine());
        assertEquals(100L, testCase9.getTime());

        TestSuiteVo suite3 = suites.get(2);
        assertEquals("my\\project\\tests\\units\\helloWorld", suite3.getName());
        assertEquals("C:\\\\My Documents\\\\myproject\\\\test\\\\helloWorld.php", suite3.getFile());

        List<TestCaseVo> testCases3 = suite3.getTestCases();
        assertEquals(1, testCases3.size());

        TestCaseVo testCase10 = testCases3.get(0);
        assertEquals("testBye", testCase10.getName());
        assertEquals(TestCase.Status.FAILED, testCase10.getStatus());
        assertEquals("strings are not equals", testCase10.getMessage());
        assertEquals("string(10) \"Bye World!\"", testCase10.getDiff().getExpected());
        assertEquals("string(6) \"Hello!\"", testCase10.getDiff().getActual());
        assertEquals("C:\\\\My Documents\\\\myproject\\\\test\\\\helloWorld.php", testCase10.getFile());
        assertEquals(31, testCase10.getLine());
        assertEquals(100L, testCase10.getTime());
    }

    public void testParseIssue233908() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap-233908.log"), 100L);
        assertEquals(1, suites.size());
        TestCaseVo testCase = suites.get(0).getTestCases().get(0);
        assertEquals("testPlus", testCase.getName());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals("integer(2) is not equal to integer(3)", testCase.getMessage());
        assertNotNull(testCase.getDiff());
        assertEquals("int(3)", testCase.getDiff().getExpected());
        assertEquals("int(2)", testCase.getDiff().getActual());
        assertEquals("/home/gapon/NetBeansProjects/Calculator-atoum/test/Calculator.php", testCase.getFile());
        assertEquals(62, testCase.getLine());
        assertEquals(100L, testCase.getTime());
    }

    public void testParseIssue235280_1() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap-235280-1.log"), 100L);
        assertEquals(1, suites.size());
        TestCaseVo testCase1 = suites.get(0).getTestCases().get(0);
        assertEquals("testDivide", testCase1.getName());
        assertEquals(TestCase.Status.ERROR, testCase1.getStatus());
        assertEquals("Fatal error : Call to undefined method netbeans\\sample\\Calculator::divide()", testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertEquals("/Users/jubianchi/NetBeansProjects/nb-atoum-blog/tests/units/netbeans/sample/Calculator.php", testCase1.getFile());
        assertEquals(-1, testCase1.getLine());
        TestCaseVo testCase2 = suites.get(0).getTestCases().get(1);
        assertEquals("testAdd", testCase2.getName());
        assertEquals(TestCase.Status.ERROR, testCase2.getStatus());
        assertEquals("Fatal error : Call to undefined method netbeans\\sample\\Calculator::add()", testCase2.getMessage());
        assertNull(testCase2.getDiff());
        assertEquals("/Users/jubianchi/NetBeansProjects/nb-atoum-blog/tests/units/netbeans/sample/Calculator.php", testCase2.getFile());
        assertEquals(-1, testCase2.getLine());
    }

    public void testParseIssue235280_2() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap-235280-2.log"), 100L);
        assertEquals(1, suites.size());
        TestCaseVo testCase1 = suites.get(0).getTestCases().get(0);
        assertEquals("testDivide", testCase1.getName());
        assertEquals(TestCase.Status.ERROR, testCase1.getStatus());
        assertEquals("", testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertEquals("/Users/jubianchi/NetBeansProjects/nb-atoum-blog/tests/units/netbeans/sample/Calculator.php", testCase1.getFile());
        assertEquals(-1, testCase1.getLine());
        TestCaseVo testCase2 = suites.get(0).getTestCases().get(1);
        assertEquals("testAdd", testCase2.getName());
        assertEquals(TestCase.Status.PASSED, testCase2.getStatus());
        assertNull(testCase2.getMessage());
        assertNull(testCase2.getDiff());
        assertNull(testCase2.getFile());
        assertEquals(-1, testCase2.getLine());
    }

    public void testParseIssue235280_3() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap-235280-3.log"), 100L);
        assertEquals(1, suites.size());
        assertEquals(1, suites.get(0).getTestCases().size());
        TestCaseVo testCase1 = suites.get(0).getTestCases().get(0);
        assertEquals("testAdd", testCase1.getName());
        assertEquals(TestCase.Status.ERROR, testCase1.getStatus());
        assertEquals("E_USER_ERROR : Tested class 'netbeans\\sample\\Calculator' does not exist for test class 'tests\\units\\netbeans\\sample\\Calculator'", testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertEquals("/home/gapon/NetBeansProjects/atoum-issue/tests/units/netbeans/sample/Calculator.php", testCase1.getFile());
        assertEquals(-1, testCase1.getLine());
    }

    public void testParseIssue235482() throws Exception {
        List<TestSuiteVo> suites = new TapParser()
                .parse(getFileContent("atoum-tap-235482.log"), 100L);
        assertEquals(1, suites.size());
        assertEquals(3, suites.get(0).getTestCases().size());

        TestCaseVo testCase1 = suites.get(0).getTestCases().get(0);
        assertEquals("testAdd", testCase1.getName());
        assertEquals(TestCase.Status.PENDING, testCase1.getStatus());
        assertNull(testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertEquals("/var/www/Atom2/tests/unit/netbeans/sample/Calculator.php", testCase1.getFile());
        assertEquals(-1, testCase1.getLine());

        TestCaseVo testCase2 = suites.get(0).getTestCases().get(1);
        assertEquals("testAddDie2", testCase2.getName());
        assertEquals(TestCase.Status.ERROR, testCase2.getStatus());
        assertEquals("", testCase2.getMessage());
        assertNull(testCase2.getDiff());
        assertEquals("/var/www/Atom2/tests/unit/netbeans/sample/Calculator.php", testCase2.getFile());
        assertEquals(-1, testCase2.getLine());

        TestCaseVo testCase3 = suites.get(0).getTestCases().get(2);
        assertEquals("testAddDie", testCase3.getName());
        assertEquals(TestCase.Status.FAILED, testCase3.getStatus());
        assertEquals("E_USER_ERROR : Argument of mageekguy\\atoum\\asserters\\integer::isEqualTo() must be an integer", testCase3.getMessage());
        assertNull(testCase3.getDiff());
        assertEquals("/var/www/Atom2/tests/unit/netbeans/sample/Calculator.php", testCase3.getFile());
        assertEquals(25, testCase3.getLine());
    }

    private String getFileContent(String filePath) throws IOException {
        File file = new File(getDataDir(), filePath);
        assertTrue(file.getAbsolutePath(), file.isFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
