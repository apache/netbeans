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
package org.netbeans.modules.php.nette.tester.run;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.spi.testing.run.TestCase;
import org.openide.util.Pair;

public class TapParserTest extends NbTestCase {

    private static final String EXPECTED_CONTENT = "expected value";
    private static final String ACTUAL_CONTENT = "actual value";


    public TapParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        putFileContent("Greeting.test.expected", EXPECTED_CONTENT);
        putFileContent("Greeting.test.actual", ACTUAL_CONTENT);
    }

    public void testParse1() throws Exception {
        // prepare content
        String log = getFileContent("nette-tester-tap1.log");
        log = log.replace("%DIFF_BASE_DIR%", getWorkDirPath() + File.separator);
        // parse it
        TestSuiteVo suite = new TapParser()
                .parse(log, 700L);
        assertEquals("Tests", suite.getName());
        assertEquals(null, suite.getFile());

        List<TestCaseVo> testCases = suite.getTestCases();
        assertEquals(7, testCases.size());

        TestCaseVo testCase1 = testCases.get(0);
        assertEquals("nette-tester/tests/MyTests.phpt", testCase1.getName());
        assertEquals(TestCase.Status.PASSED, testCase1.getStatus());
        assertNull(testCase1.getMessage());
        assertNull(testCase1.getDiff());
        assertTrue(testCase1.getStackTrace().isEmpty());
        assertNull(testCase1.getFile());
        assertEquals(100L, testCase1.getTime());

        TestCaseVo testCase2 = testCases.get(1);
        assertEquals("nette-tester/tests/SkippedTests.phpt", testCase2.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase2.getStatus());
        assertNull(testCase2.getMessage());
        assertNull(testCase2.getDiff());
        assertTrue(testCase1.getStackTrace().isEmpty());
        assertNull(testCase2.getFile());
        assertEquals(100L, testCase2.getTime());


        TestCaseVo testCase3 = testCases.get(2);
        assertEquals("nette-tester/tests/Greeting.test.phpt", testCase3.getName());
        assertEquals(TestCase.Status.FAILED, testCase3.getStatus());
        assertEquals("'Hello JohnX' should be 10", testCase3.getMessage());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(57) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting.test.phpt(16) Tester\\Assert::same()"), testCase3.getStackTrace());
        assertNull(testCase3.getDiff());
        assertEquals("nette-tester/tests/Greeting.test.phpt", testCase3.getFile());
        assertEquals(16, testCase3.getLine());
        assertEquals(100L, testCase3.getTime());

        TestCaseVo testCase4 = testCases.get(3);
        assertEquals("nette-tester/tests/Greeting2.test.phpt", testCase4.getName());
        assertEquals(TestCase.Status.FAILED, testCase4.getStatus());
        assertEquals("'Hello JohnX' should be 'Hello John'", testCase4.getMessage());
        TestCase.Diff diff = testCase4.getDiff();
        assertNotNull(diff);
        assertEquals(EXPECTED_CONTENT, diff.getExpected());
        assertEquals(ACTUAL_CONTENT, diff.getActual());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(57) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting2.test.phpt(15) Tester\\Assert::same()"), testCase4.getStackTrace());
        assertEquals("nette-tester/tests/Greeting2.test.phpt", testCase4.getFile());
        assertEquals(15, testCase4.getLine());
        assertEquals(100L, testCase4.getTime());

        TestCaseVo testCase5 = testCases.get(4);
        assertEquals("nette-tester/tests/Greeting3.test.phpt", testCase5.getName());
        assertEquals(TestCase.Status.FAILED, testCase5.getStatus());
        assertEquals("InvalidArgumentException was expected, but none was thrown", testCase5.getMessage());
        assertNull(testCase5.getDiff());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(244) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting3.test.phpt(15) Tester\\Assert::exception()"), testCase5.getStackTrace());
        assertEquals("nette-tester/tests/Greeting3.test.phpt", testCase5.getFile());
        assertEquals(15, testCase5.getLine());
        assertEquals(100L, testCase5.getTime());

        TestCaseVo testCase6 = testCases.get(5);
        assertEquals("nette-tester/tests/Greeting4.test.phpt", testCase6.getName());
        assertEquals(TestCase.Status.FAILED, testCase6.getStatus());
        assertEquals("E_NOTICE with a message matching 'Undefined property: Greeting::$abc' was expected"
                + " but got 'Undefined property: Greeting::$say'", testCase6.getMessage());
        assertNull(testCase6.getDiff());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(365)",
                "in Tester/Framework/Assert.php(300) Tester\\Assert::fail()",
                "in nette-tester/tests/Greeting4.test.phpt(14) Tester\\Assert::Tester\\{closure}()",
                "in [internal function] {closure}()",
                "in Tester/Framework/Assert.php(304) call_user_func()",
                "in nette-tester/tests/Greeting4.test.phpt(15) Tester\\Assert::error()"), testCase6.getStackTrace());
        assertEquals("nette-tester/tests/Greeting4.test.phpt", testCase6.getFile());
        assertEquals(15, testCase6.getLine());
        assertEquals(100L, testCase6.getTime());

        TestCaseVo testCase7 = testCases.get(6);
        assertEquals("nette-tester/tests/VerboseSkippedTests.phpt", testCase7.getName());
        assertEquals(TestCase.Status.SKIPPED, testCase7.getStatus());
        assertEquals("My skip message", testCase7.getMessage());
        assertNull(testCase7.getDiff());
        assertTrue(testCase1.getStackTrace().isEmpty());
        assertNull(testCase7.getFile());
        assertEquals(100L, testCase7.getTime());
    }

    public void testParse2() throws Exception {
        TestSuiteVo suite = new TapParser()
                .parse(getFileContent("nette-tester-tap2.log"), 100L);
        assertEquals("Tests", suite.getName());
        assertEquals(null, suite.getFile());

        List<TestCaseVo> testCases = suite.getTestCases();
        assertEquals(1, testCases.size());

        TestCaseVo testCase = testCases.get(0);
        assertEquals("Calculator-Nette-Tester/test/Calculator.test.phpt", testCase.getName());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals("12 should be 3", testCase.getMessage());
        assertEquals(Arrays.asList(
                "in Tester/Framework/Assert.php(370)",
                "in Tester/Framework/Assert.php(52) Tester\\Assert::fail()",
                "in Calculator-Nette-Tester/test/Calculator.test.phpt(71) Tester\\Assert::same()"), testCase.getStackTrace());
        assertNull(testCase.getDiff());
        assertEquals("Calculator-Nette-Tester/test/Calculator.test.phpt", testCase.getFile());
        assertEquals(71, testCase.getLine());
        assertEquals(100L, testCase.getTime());
    }

    public void testGetFile() {
        Pair<String, Integer> fileLine = TapParser.getFileLine("in nette-tester/tests/Greeting2.test.phpt(15) Tester\\Assert::same()");
        assertNotNull(fileLine);
        assertEquals("nette-tester/tests/Greeting2.test.phpt", fileLine.first());
        assertEquals(15, fileLine.second().intValue());
        fileLine = TapParser.getFileLine("# in nette-tester/tests/Greeting2.test.phpt(15) Tester\\Assert::same()");
        assertNotNull(fileLine);
        assertEquals("nette-tester/tests/Greeting2.test.phpt", fileLine.first());
        assertEquals(15, fileLine.second().intValue());
        fileLine = TapParser.getFileLine("1  0.0002  257184  {main}(  )  .../Calculator.divide.test.phpt : 10");
        assertNotNull(fileLine);
        assertEquals(".../Calculator.divide.test.phpt", fileLine.first());
        assertEquals(10, fileLine.second().intValue());
        fileLine = TapParser.getFileLine("# 1  0.0002  257184  {main}(  )  .../Calculator.divide.test.phpt : 10");
        assertEquals(".../Calculator.divide.test.phpt", fileLine.first());
        assertEquals(10, fileLine.second().intValue());
        fileLine = TapParser.getFileLine("unknown");
        assertNull(fileLine);
        fileLine = TapParser.getFileLine("# unknown");
        assertNull(fileLine);
    }

    public void testParseIssue255351() throws Exception {
        TestSuiteVo suite = new TapParser()
                .parse(getFileContent("nette-tester-tap-issue-255351.log"), 100L);
        assertEquals("Tests", suite.getName());
        assertEquals(null, suite.getFile());

        List<TestCaseVo> testCases = suite.getTestCases();
        assertEquals(1, testCases.size());

        TestCaseVo testCase = testCases.get(0);
        assertEquals("test/src/Calculator.divide.test.phpt", testCase.getName());
        assertEquals(TestCase.Status.FAILED, testCase.getStatus());
        assertEquals("Exited with error code 255 (expected 0)"
                + " ( ! )  Warning: require(/home/gapon/NetBeansProjects/Calculator-Nette-Tester5/test/src/../vendor/autoload.php):"
                + " failed to open stream: No such file or directory"
                + " in /home/gapon/NetBeansProjects/Calculator-Nette-Tester5/test/src/Calculator.divide.test.phpt"
                + " on line  44", testCase.getMessage());
        assertEquals(Arrays.asList("1  0.0002  257184  {main}(  )  .../Calculator.divide.test.phpt : 0"), testCase.getStackTrace());
        assertNull(testCase.getDiff());
        assertEquals(".../Calculator.divide.test.phpt", testCase.getFile());
        assertEquals(0, testCase.getLine());
        assertEquals(100L, testCase.getTime());
    }

    private String getFileContent(String filePath) throws IOException {
        File file = new File(getDataDir(), filePath);
        assertTrue(file.getAbsolutePath(), file.isFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void putFileContent(String filePath, String content) throws IOException {
        File file = new File(getWorkDir(), filePath);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

}
