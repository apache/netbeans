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

package org.netbeans.modules.junit.api;

import org.netbeans.modules.java.testrunner.JavaRegexpUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author Marian Petras
 */
public class RegexpUtilsTest extends TestCase {

    private final Field instRefField;
    private final Method methodSpecialTrim;

    private JavaRegexpUtils inst;

    public RegexpUtilsTest(String testName) throws NoSuchFieldException,
                                                   NoSuchMethodException {
        super(testName);
        instRefField = JavaRegexpUtils.class.getDeclaredField("instRef");
        instRefField.setAccessible(true);
        
        methodSpecialTrim = JavaRegexpUtils.class.getDeclaredMethod(
                                    "specialTrim",
                                    new Class[] {String.class});
        methodSpecialTrim.setAccessible(true);
    }

    @Override
    public void setUp() throws IllegalAccessException {
        instRefField.set(null, null);
        
        inst = JavaRegexpUtils.getInstance();
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

    public void testTestsuiteStatsRegex() throws Exception {
        Pattern pattern = getPattern("TESTSUITE_STATS_REGEX");

        String[] matchingStrings = new String[] {
            "Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.066 sec",
        };
        String[] nonMatchingStrings = new String[] {
            "Tests run: 1, Failures: 0, Errors: 0, Time elapse: 0.066 sec",
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
	    Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.matches());
	    assertTrue("matcher.group(4) should be durationn of test", matcher.group(4).trim().equals("0.066"));
        }
        for (int i = 0; i < nonMatchingStrings.length; i++) {
            String string = nonMatchingStrings[i];
            assertFalse("should not match: " + string,
                        pattern.matcher(string).matches());
        }

	pattern = getPattern("TESTSUITE_STATS_190_REGEX");

        matchingStrings = new String[] {
            "Tests run: 1, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.066 sec",
        };
        nonMatchingStrings = new String[] {
            "Tests run: 1, Failures: 0, Errors: 0, Skippe: 1, Time elapsed: 0.066 sec",
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i];
	    Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.matches());
	    assertTrue("matcher.group(6) should be durationn of test", matcher.group(6).trim().equals("0.066"));
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
    
    

    public void testComparisonRegex() throws Exception {
        Pattern pattern = getPattern("COMPARISON_REGEX");
        
        final String[] matchingStrings = new String[] {
            "expected:<Hello from hereThis is test[]> but was:<Hello from hereThis is test[2]>",
            "expected:<Hello from here[ This is test]> but was:<Hello from here[This is test2]>",
            "expected:<...om hereThis is test[]> but was:<...om hereThis is test[2]>",
        };
        final String[] nonMatchingStrings = new String[] {
            "expected:<Hello from here\nThis is test[]> but was:<Hello from here\nThis is test[2]>",
            "expected:<Hello from here[\nThis is test]> but was:<Hello from here[This is test2]>",
            "expected:<...om here\nThis is test[]> but was:<...om here\nThis is test[2]>",
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
            assertTrue("should match: " + string,
                        pattern.matcher(string.replace("\n", "")).matches());
        }
    }
    
    private Pattern getPattern(String fieldName) throws Exception {
        return Pattern.compile(getRegex(fieldName));
    }
    
    private String getRegex(String fieldName) throws Exception {
        Field regexField = JavaRegexpUtils.class.getDeclaredField(fieldName);
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
