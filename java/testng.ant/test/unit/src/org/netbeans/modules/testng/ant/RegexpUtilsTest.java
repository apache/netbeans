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

package org.netbeans.modules.testng.ant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Marian Petras
 */
@Test
public class RegexpUtilsTest extends TestCase {

    private final Field instRefField;
    private final Method methodSpecialTrim;

    private RegexpUtils inst;

    public RegexpUtilsTest(String testName) throws NoSuchFieldException,
                                                   NoSuchMethodException {
        super(testName);
        instRefField = RegexpUtils.class.getDeclaredField("instRef");
        instRefField.setAccessible(true);

        methodSpecialTrim = RegexpUtils.class.getDeclaredMethod(
                                    "specialTrim",
                                    new Class[] {String.class});
        methodSpecialTrim.setAccessible(true);
    }

    @Override
    @BeforeMethod
    public void setUp() throws IllegalAccessException {
        instRefField.set(null, null);

        inst = RegexpUtils.getInstance();
    }

    public void testParseTimeMillis() {
        assertEquals(0, inst.parseTimeMillis("0"));
        assertEquals(0, inst.parseTimeMillis("00"));
        assertEquals(1234000, inst.parseTimeMillis("1234"));
        assertEquals(1234500, inst.parseTimeMillis("1234.5"));
        assertEquals(1234560, inst.parseTimeMillis("1234.56"));
        assertEquals(1234567, inst.parseTimeMillis("1234.567"));
        assertEquals(1234567, inst.parseTimeMillis("1234.5670"));
        assertEquals(1234567, inst.parseTimeMillis("1234.5671"));
        assertEquals(1234567, inst.parseTimeMillis("1234.5674"));
        assertEquals(1234568, inst.parseTimeMillis("1234.5675"));
        assertEquals(1234568, inst.parseTimeMillis("1234.5676"));
        assertEquals(1234568, inst.parseTimeMillis("1234.56764"));
        assertEquals(1234568, inst.parseTimeMillis("1234.56766"));
        assertEquals(500, inst.parseTimeMillis(".5"));
        assertEquals(560, inst.parseTimeMillis(".56"));
        assertEquals(567, inst.parseTimeMillis(".567"));
        assertEquals(567, inst.parseTimeMillis(".5670"));
        assertEquals(567, inst.parseTimeMillis(".5671"));
        assertEquals(567, inst.parseTimeMillis(".5674"));
        assertEquals(568, inst.parseTimeMillis(".5675"));
        assertEquals(568, inst.parseTimeMillis(".5676"));
    }

    public void testTimeSecsRegex() throws Exception {
        Pattern pattern = getPattern("SECONDS_REGEX");

        final String[] matchingStrings = new String[] {
            "s",
            "sec",
            "secs",
            "sec(s)",
            "second",
            "seconds",
            "second(s)",
        };
        final String[] nonMatchingStrings = new String[] {
            "ss",
            "s(s)",
            "secss",
            "secs(s)",
            "secondss",
            "seconds(s)"
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    public void testTestcaseIssueRegex() throws Exception {
        Pattern pattern = getPattern("TESTCASE_ISSUE_REGEX");

        final String[] matchingStrings = new String[] {
            "FAILED",
            "Caused an ERROR",
            "error",
            "   FAILED",
            "\t \t FAILED",
            " \t \tFAILED",
            "\t \t FAILED ",
            " \t \tFAILED       ",
            "xxxxx ErRoR yyy"
        };
        final String[] nonMatchingStrings = new String[] {
            "failed",
            "Failed",
            "x FAILED",
            "xFAILED",
            "FAILEDx",
            "mistakeerror",
            "mistake errors",
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    public void testTestcaseHeaderPlainRegex() throws Exception {
        Pattern pattern = getPattern("TESTCASE_HEADER_PLAIN_REGEX");

        final String[] matchingStrings = new String[] {
            "testComputeSum took 0.002 sec",
            "testComputeSum took 0 sec",
            "testComputeSum took 0.002 s",
            " testComputeSum took 0.002 sec",
            "     testComputeSum took 0.002 sec",
            "\ttestComputeSum took 0.002 sec",
            "\t\t\testComputeSum took 0.002 sec",
            " \t\t testComputeSum took 0.002 sec",
            "\t\t  testComputeSum took 0.002 sec",
            "test took 12 seconds",
            "test\ttook 12 seconds",
            "test\t\ttook .5 seconds",
            "test\t  took .5 seconds",
            "test    took .5 seconds",
            "test12 took 12 secs"
        };
        final String[] nonMatchingStrings = new String[] {
            "12test took 12 seconds",
            "test tooks",
            "test took3 seconds",
            "test took 3 bflmpsvz",
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    public void testTestcaseHeaderBriefRegex() throws Exception {
        Pattern pattern = getPattern("TESTCASE_HEADER_BRIEF_REGEX");

        final String[] matchingStrings = new String[] {
            "testMain(javapplication2.MainTest): FAILED",
            "testMain(javapplication2.MainTest): Caused an ERROR",
            "testMain(MainTest): FAILED",
            "   testMain(javapplication2.MainTest): FAILED",
            "testMain(javapplication2.MainTest) :FAILED",
            "testMain(javapplication2.MainTest)   :    FAILED",
            "testMain(javapplication2.MainTest): mistake error"
        };
        final String[] nonMatchingStrings = new String[] {
            "testMain(javapplication2.MainTest)",
            "(javapplication2.MainTest): FAILED",
            "testMain(javapplication2.MainTest): Failed",
            "testMain(javapplication2.MainTest): mistake",
            "testMain(javapplication2.MainTest): errors",
            "testMain(javapplication2.MainTest): mistakeerror",
            "testMain(javapplication2.MainTest): mistake errors",
            "testMain(javapplication2.): FAILED",
            "testMain(.MainTest): FAILED",
            "testMain(javapplication2..MainTest): FAILED",
            "testMain(2.MainTest): FAILED",
            "testMain(javapplication2.2): FAILED"
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    public void testTestcaseExceptionRegex() throws Exception {
        Pattern pattern = getPattern("TESTCASE_EXCEPTION_REGEX");

        final String[] matchingStrings = new String[] {
                "junit.framework.AssertionFailedException",
                "junit.framework.AssertionFailedException: The test case is empty.",
                "java.lang.NullPointerException",
                "java.lang.Exception",
                "java.lang.Throwable",
                "MySpecialException",
                "MySpecialError",
                "foo.Exception",
                "foo.Error",
                "foo.bar.Exception",
                "foo.bar.Error" };
        final String[] nonMatchingStrings = new String[] {
                "Exception",
                "Error",
                "Throwable",
                "mypackage.Throwable",
                "foo.bar.Throwable",
                ".foo",
                ".Exception",
                ".Error",
                ".foo.Exception",
                ".foo.Error",
                "Exception.",
                "Error.",
                "foo.Exception.",
                "foo.Error.",
                "foo.bar.Exception.",
                "foo.bar.Error.",
                "foo..bar.Exception",
                "foo..bar.Error",
                "junit.framework.AssertionFailedException It failed" };
        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }

        Matcher matcher;

        matcher = pattern.matcher("java.lang.NullPointerException");
        assertTrue(matcher.matches());
        assertEquals("java.lang.NullPointerException", matcher.group(1));
        assertNull(matcher.group(2));

        matcher = pattern.matcher("java.lang.NullPointerException:");
        assertTrue(matcher.matches());
        assertEquals("java.lang.NullPointerException", matcher.group(1));
        assertEquals("", matcher.group(2));

        matcher = pattern.matcher("java.lang.NullPointerException  :   Failed");
        assertTrue(matcher.matches());
        assertEquals("java.lang.NullPointerException", matcher.group(1));
        assertEquals("Failed", matcher.group(2));
    }

    public void testCallstackLineRegex() throws Exception{
        Pattern pattern = getPattern("CALLSTACK_LINE_REGEX");

        final String[] matchingStrings = new String[] {
            "  at javaapplication.MainTest.test",
            "   at javaapplication.MainTest.test",
            "    at javaapplication.MainTest.test",
            "\tat javaapplication.MainTest.test",
            "\t\tat javaapplication.MainTest.test",
            "[catch] at javaapplication.MainTest.test",
            " [catch] at javaapplication.MainTest.test",
            "  [catch] at javaapplication.MainTest.test",
            "      [catch] at javaapplication.MainTest.test",
            "\t[catch] at javaapplication.MainTest.test",
            "\t [catch] at javaapplication.MainTest.test",
            " \t[catch] at javaapplication.MainTest.test",
            "\t  [catch] at javaapplication.MainTest.test",
            " \t [catch] at javaapplication.MainTest.test",
            "  \t[catch] at javaapplication.MainTest.test",
            "\t   [catch] at javaapplication.MainTest.test",
            " \t  [catch] at javaapplication.MainTest.test",
            "  \t [catch] at javaapplication.MainTest.test",
            "   \t[catch] at javaapplication.MainTest.test",
            "  at MainTest.test",
            "  at javaapplication.MainTest.test(a)",
            "  at javaapplication.MainTest.test (a)",
            "  at javaapplication.MainTest.test (Compiled)",
            "  at javaapplication.MainTest.test (Native method)",
            "  at javaapplication.MainTest.test (MainTest.java)",
            "  at javaapplication.MainTest.test (MainTest.java:32)",
            "  at javaapplication.MainTest.test(MainTest.java:32)"
        };
        final String[] nonMatchingStrings = new String[] {
            "javaapplication.MainTest.test",
            " javaapplication.MainTest.test",
            "at javaapplication.MainTest.test",
            " at javaapplication.MainTest.test",
            "  at  javaapplication.MainTest.test",
            "\t at javaapplication.MainTest.test",
            " \tat javaapplication.MainTest.test",
            "\t  at javaapplication.MainTest.test",
            " \t at javaapplication.MainTest.test",
            "  \tat javaapplication.MainTest.test",
            "\t\t at javaapplication.MainTest.test",
            "\t \tat javaapplication.MainTest.test",
            " \t\tat javaapplication.MainTest.test",
            "\t\t  at javaapplication.MainTest.test",
            "\t \t at javaapplication.MainTest.test",
            "\t  \tat javaapplication.MainTest.test",
            " \t\t at javaapplication.MainTest.test",
            " \t \tat javaapplication.MainTest.test",
            "  \t\tat javaapplication.MainTest.test",
            "\t\t[catch] at javaapplication.MainTest.test",
            " \t\t[catch] at javaapplication.MainTest.test",
            "\t \t[catch] at javaapplication.MainTest.test",
            "\t\t [catch] at javaapplication.MainTest.test",
            "  at test",
            "  at javaapplication.%dfsd",
            "  at 2application.MainTest",
            "  at javaapplication.MainTest.test()",
            "  at javaapplication.MainTest.test ()",
            "  at javaapplication.MainTest.test  (a)",
            "  at javaapplication.MainTest.test xyz",
            "  at javaapplication.MainTest.test (abc) x",
            "  at javaapplication.MainTest.test (abc) (de)",
            "  at javaapplication.MainTest.test (ab(cd)",
            "  at javaapplication.MainTest.test (ab)cd)",
            "  at javaapplication.MainTest.test (ab(cd))"
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    public void testXmlDeclRegex() throws Exception {
        Pattern pattern = getPattern("XML_DECL_REGEX");

        final String[] matchingStrings = new String[] {
            "<?xml version=\"1.0\"?>",
            "<?xml    version=\"1.0\"?>",
            "<?xml\tversion=\"1.0\"?>",
            "<?xml\t\t   version=\"1.0\"?>",
            "<?xml version =\"1.0\"?>",
            "<?xml version  =\"1.0\"?>",
            "<?xml version= \"1.0\"?>",
            "<?xml version=  \"1.0\"?>",
            "<?xml version = \"1.0\"?>",
            "<?xml version  \t=\t   \"1.0\"?>",
            "<?xml version=\"1.0\" encoding=\"abc\"?>",
            "<?xml version=\"1.0\" encoding=\'abc\'?>",
            "<?xml version=\"1.0\"\tencoding=\"abc\"?>",
            "<?xml version=\"1.0\"  encoding=\"abc\"?>",
            "<?xml version=\"1.0\"\t\tencoding=\"abc\"?>",
            "<?xml version=\"1.0\" \tencoding=\"abc\"?>",
            "<?xml version=\"1.0\"\t encoding=\"abc\"?>",
            "<?xml version=\"1.0\" encoding =\"abc\"?>",
            "<?xml version=\"1.0\" encoding= \"abc\"?>",
            "<?xml version=\"1.0\" encoding\t=\t\"abc\"?>",
            "<?xml version=\"1.0\" encoding\t = \t\"abc\"?>",
            "<?xml version=\"1.0\" encoding=\"ab1c\"?>",
            "<?xml version=\"1.0\" encoding=\"ab.c\"?>",
            "<?xml version=\"1.0\" encoding=\"ab_c\"?>",
            "<?xml version=\"1.0\" encoding=\"ab-c\"?>",

            "<?xml version=\"1.0\" standalone=\"yes\"?>",
            "<?xml version=\"1.0\" standalone=\'yes\'?>",
            "<?xml version=\"1.0\" standalone=\"no\"?>",
            "<?xml version=\"1.0\" standalone=\'no\'?>",
            "<?xml version=\"1.0\"\tstandalone=\"yes\"?>",
            "<?xml version=\"1.0\"  standalone=\"yes\"?>",
            "<?xml version=\"1.0\"\t\tstandalone=\"yes\"?>",
            "<?xml version=\"1.0\" \tstandalone=\"yes\"?>",
            "<?xml version=\"1.0\"\t standalone=\"yes\"?>",
            "<?xml version=\"1.0\" standalone =\"yes\"?>",
            "<?xml version=\"1.0\" standalone= \"yes\"?>",
            "<?xml version=\"1.0\" standalone\t=\t\"yes\"?>",
            "<?xml version=\"1.0\" standalone\t = \t\"yes\"?>",

            "<?xml version=\"1.0\" encoding=\"abc\" standalone=\"yes\"?>",
            "<?xml version=\"1.0\" encoding=\"abc\" standalone=\'yes\'?>",
            "<?xml version=\"1.0\" encoding=\'abc\' standalone=\"yes\"?>",
            "<?xml version=\"1.0\" encoding=\'abc\' standalone=\'yes\'?>",

            "<?xml version=\"1.0\" encoding=\"abc\" standalone=\"yes\"   ?>",
            "<?xml version=\"1.0\" encoding=\"abc\" standalone=\"yes\"\t?>"
        };
        final String[] nonMatchingStrings = new String[] {
            "<?xml?>",
            "<?xml ?>",
            "<?xml version=\"1.1\"?>",
            "<?xmlversion=\"1.0\"?>",
            "<Uxml version=\"1.0\"?>",
            "<?xml version=\"1.0\"U>",
            "<?xml version=\"1.0\"?> ",
            "<?xml version=\"1.0\"?>\t",
            "xml version=\"1.0\"?>",
            "<?xml version=\"1.0>",
            "<?xml version=\"1.0\"encoding=\"abc\"?>",
            "<?xml version=\"1.0\" encoding=\"ab%\"?>",
            "<?xml version=\"1.0\" encoding=\"1abc\"?>",
            "<?xml version=\"1.0\" encoding=\".abc\"?>",
            "<?xml version=\"1.0\" encoding=\"_abc\"?>",
            "<?xml version=\"1.0\" encoding=\"-abc\"?>",
            "<?xml version=\"1.0\"standalone=yes?>",
            "<?xml version=\"1.0\" standalone=yes>",
            "<?xml version=\"1.0\" standalone=\'yes\"?>",
            "<?xml version=\"1.0\" standalone=\"yes\'?>",
            "<?xml version=\"1.0\" standalone=\"yes?>",
            "<?xml version=\"1.0\" standalone=yes\"?>",
            "<?xml version=\"1.0\" standalone=\'yes?>",
            "<?xml version=\"1.0\" standalone=yes\'?>",
            "<?xml version=\"1.0\" standalone=\"maybe\"?>",
            "<?xml version=\"1.0\" encoding=\"abc\" standalone=\'yes\"?>",
            "<?xml version=\"1.0\" encoding=\'abc\" standalone=\"yes\"?>"
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
            assertTrue("should match: " + string,
                       pattern.matcher(string).matches());
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }
    }

    /**
     */
    public void testSpecialTrim() throws IllegalAccessException,
                                         InvocationTargetException {
        assertSame("", specialTrim(""));
        assertSame("abcde", specialTrim("abcde"));
        assertSame("ab\tc de", specialTrim("ab\tc de"));
        assertSame("ab c\tde", specialTrim("ab c\tde"));
        assertEquals("", specialTrim("    "));
        assertEquals("", specialTrim("\t\t"));
        assertEquals("abcde", specialTrim("  abcde"));
        assertEquals("abcde", specialTrim("abcde  "));
        assertEquals("abcde", specialTrim("  abcde  "));
        assertEquals("ab\tc de", specialTrim("  ab\tc de"));
        assertEquals("ab c\tde", specialTrim("\tab c\tde"));
        assertEquals("ab\tc de", specialTrim("ab\tc de "));
        assertEquals("ab c\tde", specialTrim("ab c\tde\t"));
    }

    public void testRunningSuite() throws Exception {
        Pattern pattern = getPattern("RUNNING_SUITE_REGEX");
        final String matchingString =
            "[NBTestListener] RUNNING: Suite: \"Junit via TestNG XML\" containing \"5\" Tests (config: /space/sources/tng_fork/src/test/resources/junit-suite.xml)";
        Matcher m = pattern.matcher(matchingString);
        assertTrue(m.matches());
        assertEquals(3, m.groupCount());
        assertEquals("Junit via TestNG XML", m.group(1));
        assertEquals("5", m.group(2));
        assertEquals("/space/sources/tng_fork/src/test/resources/junit-suite.xml", m.group(3));
        final String matchingString2 =
            "[NBTestListener] RUNNING: Suite: \"Junit via TestNG XML\" containing \"5\" Tests (config: c:\\test\\resources\\junit-suite.xml)";
        m = pattern.matcher(matchingString2);
        assertTrue(m.matches());
        assertEquals(3, m.groupCount());
        assertEquals("Junit via TestNG XML", m.group(1));
        assertEquals("5", m.group(2));
        assertEquals("c:\\test\\resources\\junit-suite.xml", m.group(3));
    }

    public void testTestRegexp() throws Exception {
        Pattern pattern = getPattern("TEST_REGEX");
        final String matchingString =
            "[NBTestListener] PASSED: \"Regression1\" - test.parameters.ParameterSample.testSingleString() finished in 4 ms";
        Matcher m = pattern.matcher(matchingString);
        assertTrue(m.matches());
        assertEquals(13, m.groupCount());
        assertEquals("Regression1", m.group(1));
        assertEquals("test.parameters.ParameterSample.testSingleString", m.group(2));
        assertEquals("", m.group(4));
        assertNull(m.group(5));
        assertNull(m.group(6));
        assertEquals("4", m.group(8));
        final String matchingString2 =
            "[NBTestListener] PASSED: \"Regression1\" - test.parameters.ParameterSample.testSingleString(java.lang.String)(value(s): \"Cedric\") finished in 2 ms";
        m = pattern.matcher(matchingString2);
        assertTrue(m.matches());
        assertEquals(13, m.groupCount());
        assertEquals("Regression1", m.group(1));
        assertEquals("test.parameters.ParameterSample.testSingleString", m.group(2));
        assertEquals("java.lang.String", m.group(4));
        assertEquals("(value(s): \"Cedric\")", m.group(5));
        assertEquals("\"Cedric\"", m.group(6));
        assertEquals("2", m.group(8));
        final String matchingString3 =
            "[NBTestListener] PASSED: \"Injection\" - test.inject.NoInjectionTest.withInjection(java.lang.reflect.Method)(value(s): public void test.inject.NoInjectionTest.withInjection(java.lang.reflect.Method)) finished in 15 ms";
        m = pattern.matcher(matchingString3);
        assertTrue(m.matches());
        assertEquals(13, m.groupCount());
        assertEquals("Injection", m.group(1));
        assertEquals("test.inject.NoInjectionTest.withInjection", m.group(2));
        assertEquals("java.lang.reflect.Method", m.group(4));
        assertEquals("(value(s): public void test.inject.NoInjectionTest.withInjection(java.lang.reflect.Method))", m.group(5));
        assertEquals("public void test.inject.NoInjectionTest.withInjection(java.lang.reflect.Method)", m.group(6));
        assertEquals("15", m.group(8));

        final String matchingString4 =
            "[VerboseTestNG] INVOKING: \"Ant test\" - test.MyTest";
        m = pattern.matcher(matchingString4);
        assertTrue(m.matches());
        assertEquals(13, m.groupCount());
        assertEquals("Ant test", m.group(1));
        assertEquals("test.MyTest", m.group(2));
        assertNull(m.group(3));
        assertNull(m.group(4));
        assertNull(m.group(5));
        assertNull(m.group(7));
    }
    
    public void testTestRegexp2() throws Exception {
        Pattern pattern = getPattern("TEST_REGEX_2");
        final String matchingString =
            "INVOKING: \"Mustache\" - test.mustache.MustacheTest.runTest(java.util.Map, java.lang.String, java.lang.String)(value(s): {}, \"Hello";
        Matcher m = pattern.matcher(matchingString);
        assertTrue(m.matches());
//        assertEquals(8, m.groupCount());
        assertEquals("Mustache", m.group(1));
        assertEquals("test.mustache.MustacheTest.runTest", m.group(2));
        assertEquals("java.util.Map, java.lang.String, java.lang.String", m.group(4));
//        assertNull(m.group(5));
//        assertNull(m.group(6));
//        assertEquals("4", m.group(8));
    }

    public void testRemovingNewLines() throws Exception {
        Pattern pattern = getPattern("TEST_REGEX");
        final String matchingString =
            "[NBTestListener] PASSED: \n\"Regression1\" - \ntest.parameters.ParameterSample.testSingleString() \nfinished in 4 ms";
        Matcher m = pattern.matcher(matchingString);
        assertFalse(m.matches());
        
        m = pattern.matcher(matchingString.replace("\n", ""));
        assertTrue(m.matches());
        assertEquals(13, m.groupCount());
        assertEquals("Regression1", m.group(1));
        assertEquals("test.parameters.ParameterSample.testSingleString", m.group(2));
        assertEquals("", m.group(4));
        assertNull(m.group(5));
        assertNull(m.group(6));
        assertEquals("4", m.group(8));
    }

    public void testStatsRegexp() throws Exception {
        Pattern pattern = getPattern("STATS_REGEX");
        final String matchingString =
            "[NBTestListener]     Tests run: 559, Failures: 4, Skips: 9";
        Matcher m = pattern.matcher(matchingString);
        assertTrue(m.matches());
        assertEquals(4, m.groupCount());
        assertEquals("559", m.group(1));
        assertEquals("4", m.group(2));
        assertEquals("9", m.group(4));
        final String matchingString2 =
            "[NBTestListener]     Configuration Failures: 22, Skips: 11";
        m = pattern.matcher(matchingString2);
        assertTrue(m.matches());
        assertEquals(4, m.groupCount());
        assertEquals("22", m.group(1));
        assertEquals("11", m.group(2));
        assertNull(m.group(3));
        assertNull(m.group(4));
    }

    private Pattern getPattern(String fieldName) throws Exception {
        return Pattern.compile(getRegex(fieldName));
    }

    private String getRegex(String fieldName) throws Exception {
        Field regexField = RegexpUtils.class.getDeclaredField(fieldName);
        regexField.setAccessible(true);
        return (String) regexField.get(null);
    }

    /**
     */
    private String specialTrim(String str) throws IllegalAccessException,
                                                  InvocationTargetException {
        Object result = methodSpecialTrim.invoke(null, new Object[] {str});
        return (String) result;
    }

}
