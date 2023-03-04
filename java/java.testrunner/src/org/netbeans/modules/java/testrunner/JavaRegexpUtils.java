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

package org.netbeans.modules.java.testrunner;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

/**
 * Utility class providing various parsing routines for parsing JUnit output.
 *
 * @author  Marian Petras
 */
public final class JavaRegexpUtils {
    
    /** */
    public static final String TESTSUITE_PREFIX = "Testsuite: ";               //NOI18N
    /** */
    public static final String TESTSUITE_STATS_PREFIX = "Tests run: ";         //NOI18N
    /** */
    public static final String FLOAT_NUMBER_REGEX
            = "[0-9]*(?:\\.[0-9]+)?";                                   //NOI18N
    /** */
    public static final String SECONDS_REGEX
            = "s(?:ec(?:ond)?(?:s|\\(s\\))?)?";                         //NOI18N
    /** */
    public static final String TESTSUITE_STATS_REGEX
        = "Tests run: +([0-9]+)," +                                     //NOI18N
          " +Failures: +([0-9]+), +Errors: +([0-9]+)," +                //NOI18N
          " +Time elapsed: +(.+)" + SECONDS_REGEX;                      //NOI18N
    public static final String TESTSUITE_STATS_190_REGEX
        = "Tests run: +([0-9]+)," +                                     //NOI18N
          " +Failures: +([0-9]+), +Errors: +([0-9]+)," +                //NOI18N
          "( +Skipped: +([0-9]+),)?" +                                  //NOI18N
          " +Time elapsed: +(.+)" + SECONDS_REGEX;                      //NOI18N
    /** */
    public static final String OUTPUT_DELIMITER_PREFIX = "--------";           //NOI18N
    /** */
    public static final String STDOUT_LABEL = "Output";                        //NOI18N
    /** */
    public static final String STDERR_LABEL = "Error";                         //NOI18N
    /** */
    public static final String OUTPUT_DELIMITER_REGEX
            = "-{8,} (?:Standard ("                                     //NOI18N
              + STDOUT_LABEL + '|' + STDERR_LABEL + ")|-{3,}) -{8,}";   //NOI18N
    /** */
    public static final String TESTCASE_PREFIX = "Testcase: ";                 //NOI18N
    /** */
    public static final String TESTCASE_ISSUE_REGEX
            = "\\p{Blank}*(?:(FAILED) *|(?i:.*\\berror\\b.*))";         //NOI18N
    /** */
    public static final String TESTCASE_HEADER_PLAIN_REGEX
            = "\\p{Blank}*(" + JavaRegexpPatterns.JAVA_ID_REGEX                           + ")\\p{Blank}+took\\p{Blank}+(.+)" + SECONDS_REGEX;      //NOI18N
    /** */
    public static final String TESTCASE_HEADER_BRIEF_REGEX
            = "\\p{Blank}*(" + JavaRegexpPatterns.JAVA_ID_REGEX                           + ") *\\( *(" + JavaRegexpPatterns.JAVA_ID_REGEX_FULL                       + ") *\\) *:" + TESTCASE_ISSUE_REGEX;                     //NOI18N
    /** */
    public static final String TESTCASE_EXCEPTION_REGEX
            = "((?:" + JavaRegexpPatterns.JAVA_ID_REGEX_FULL                              + "\\.?(?:Exception|Error|ComparisonFailure))"            //NOI18N
                        + "|java\\.lang\\.Throwable)"                   //NOI18N
              + "(?: *: *(.*))?";                                       //NOI18N
    /** */
    public static final String CALLSTACK_LINE_PREFIX = "at ";                  //NOI18N
    /** */
    public static final String CALLSTACK_LINE_PREFIX_CATCH = "[catch] ";       //NOI18N
    /** */
    public static final String CALLSTACK_LINE_REGEX
            = "(?:\\t\\t?|  +| *\\t? *\\[catch\\] )"                    //NOI18N
              + CALLSTACK_LINE_PREFIX
              + "(?:" + JavaRegexpPatterns.JAVA_ID_REGEX_FULL +"/)?"    //NOI18N
              + JavaRegexpPatterns.JAVA_ID_REGEX + "(?:\\."                 //NOI18N
              + JavaRegexpPatterns.JAVA_ID_REGEX + ")+"                     //NOI18N
              + "(?:\\.<init>)?"                                        //NOI18N
              + "(?: ?\\([^()]+\\))?";                                  //NOI18N
    /** */
    public static final String NESTED_EXCEPTION_PREFIX = "Caused by: ";        //NOI18N
    /** */
    public static final String NESTED_EXCEPTION_REGEX
            = "(" + JavaRegexpPatterns.JAVA_ID_REGEX_FULL + ")(?:: (.*))?";//NOI18N
    public static final String LOCATION_IN_FILE_REGEX
            = JavaRegexpPatterns.JAVA_ID_REGEX_FULL + "(?:\\:[0-9]+)?";     //NOI18N
    /** */
    public static final String XML_DECL_PREFIX = "<?xml";                      //NOI18N
    /** */
    public static final String XML_SPACE_REGEX
            = "[ \\t\\r\\n]";                                           //NOI18N
    /** */
    public static final String XML_EQ_REGEX
            = XML_SPACE_REGEX + '*' + '=' + XML_SPACE_REGEX + '*';
    /** */
    public static final String XML_ENC_REGEX
            = "[A-Za-z][-A-Za-z0-9._]*";                                //NOI18N
    /** */
    public static final String XML_DECL_REGEX
            = "\\Q" + XML_DECL_PREFIX + "\\E"                           //NOI18N
                  + XML_SPACE_REGEX + '+' + "version"     //version     //NOI18N
                    + XML_EQ_REGEX + "(?:\"1\\.0\"|'1\\.0')"            //NOI18N
              + "(?:"                                                   //NOI18N
                  + XML_SPACE_REGEX + '+' + "encoding"    //encoding    //NOI18N
                    + XML_EQ_REGEX + "(['\"])[A-Za-z][-A-Za-z0-9._]*\\1"//NOI18N
              + ")?"                                                    //NOI18N
              + "(?:"                                                   //NOI18N
                  + XML_SPACE_REGEX + '+' + "standalone"  //standalone  //NOI18N
                    + XML_EQ_REGEX + "(['\"])(?:yes|no)\\2"             //NOI18N
              + ")?"                                                    //NOI18N
                  + XML_SPACE_REGEX + '*' + "\\?>";                     //NOI18N
    
    /** */
    public static final String TEST_LISTENER_PREFIX
            = "junit.framework.TestListener: ";                         //NOI18N
    /** */
    public static final String TESTS_COUNT_PREFIX = "tests to run: ";          //NOI18N
    /** */
    public static final String START_OF_TEST_PREFIX = "startTest";             //NOI18N
    /** */
    public static final String END_OF_TEST_PREFIX = "endTest";                 //NOI18N
    public static final String ADD_FAILURE_PREFIX = "addFailure";      //NOI18N
    public static final String ADD_ERROR_PREFIX = "addError";          //NOI18N

    public static final String COMPARISON_REGEX = 
      ".*expected:<(.*)\\[(.*)\\](.*)> but was:<(.*)\\[(.*)\\](.*)>.*"; //NOI18N
    public static final String COMPARISON_HIDDEN_REGEX = 
      ".*expected:<(.*)> but was:<(.*)>.*"; //NOI18N

    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specificies the classpath.
     * Hack to find the classpath an Ant task is using.
     * Cf. Commandline.describeArguments, issue #28190.<br />
     * Captured groups:
     * <ol>
     *     <li>the classpath
     * </ol>
     * <!-- copied from JavaAntLogger -->
     */
    public static final Pattern CLASSPATH_ARGS
            = Pattern.compile("\r?\n'-classpath'\r?\n'(.*)'\r?\n");     //NOI18N
    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specificies java executable.
     * Hack to find JDK used for execution.
     * <!-- copied from JavaAntLogger -->
     */
    public static final Pattern JAVA_EXECUTABLE
            = Pattern.compile("^Executing '(.*)' with arguments:$",     //NOI18N
                              Pattern.MULTILINE);

    /** */
    private static Reference<JavaRegexpUtils> instRef;
    
    /**
     */
    public static synchronized JavaRegexpUtils getInstance() {
        JavaRegexpUtils instance = (instRef != null) ? instRef.get() : null;
        if (instance == null) {
            instance = new JavaRegexpUtils();
            instRef = new WeakReference<JavaRegexpUtils>(instance);
        }
        return instance;
    }
    
    /** Creates a new instance of RegexpUtils */
    private JavaRegexpUtils() { }
    
    private volatile Pattern fullJavaIdPattern, suiteStatsPattern, suiteStats190Pattern,
                             outputDelimPattern, testcaseIssuePattern,
                             testcaseExceptPattern, callstackLinePattern,
                             nestedExceptPattern,
                             locationInFilePattern,
                             testcaseHeaderBriefPattern,
                             testcaseHeaderPlainPattern,
                             xmlDeclPattern, floatNumPattern,
                             comparisonPattern, comparisonHiddenPattern;
    
    //<editor-fold defaultstate="collapsed" desc=" Note about synchronization ">
    /*
     * If-blocks in the following methods should be synchronized to ensure that
     * the patterns are not compiled twice if the methods are called by two or
     * more threads concurrently.
     *
     * But synchronization is quite expensive so I let them unsynchronized.
     * It may happen that a single pattern is compiled multiple times but
     * it does not cause any functional problem. I just marked the variables
     * as 'volatile' so that once the pattern is compiled (and the variable
     * set), subsequent invocations from other threads will find the actual
     * non-null value.
     */
    //</editor-fold>

    /** */
    public Pattern getFullJavaIdPattern() {
        if (fullJavaIdPattern == null) {
            fullJavaIdPattern
                    = Pattern.compile(JavaRegexpPatterns.JAVA_ID_REGEX_FULL);
        }
        return fullJavaIdPattern;
    }
    
    /** */
    public Pattern getSuiteStatsPattern() {
        if (suiteStatsPattern == null) {
            suiteStatsPattern = Pattern.compile(TESTSUITE_STATS_REGEX);
        }
        return suiteStatsPattern;
    }

    /** */
    public Pattern getSuiteStats190Pattern() {
        if (suiteStats190Pattern == null) {
            suiteStats190Pattern = Pattern.compile(TESTSUITE_STATS_190_REGEX);
        }
        return suiteStats190Pattern;
    }
    
    /** */
    public Pattern getOutputDelimPattern() {
        if (outputDelimPattern == null) {
            outputDelimPattern = Pattern.compile(OUTPUT_DELIMITER_REGEX);
        }
        return outputDelimPattern;
    }
    
    /** */
    public Pattern getTestcaseHeaderBriefPattern() {
        if (testcaseHeaderBriefPattern == null) {
            testcaseHeaderBriefPattern = Pattern.compile(TESTCASE_HEADER_BRIEF_REGEX);
        }
        return testcaseHeaderBriefPattern;
    }
    
    /** */
    public Pattern getTestcaseHeaderPlainPattern() {
        if (testcaseHeaderPlainPattern == null) {
            testcaseHeaderPlainPattern = Pattern.compile(TESTCASE_HEADER_PLAIN_REGEX);
        }
        return testcaseHeaderPlainPattern;
    }
    
    /** */
    public Pattern getTestcaseIssuePattern() {
        if (testcaseIssuePattern == null) {
            testcaseIssuePattern = Pattern.compile(TESTCASE_ISSUE_REGEX);
        }
        return testcaseIssuePattern;
    }
    
    /** */
    public Pattern getTestcaseExceptionPattern() {
        if (testcaseExceptPattern == null) {
            testcaseExceptPattern = Pattern.compile(TESTCASE_EXCEPTION_REGEX);
        }
        return testcaseExceptPattern;
    }
    
    /**
     */
    public Pattern getNestedExceptionPattern() {
        if (nestedExceptPattern == null) {
            nestedExceptPattern = Pattern.compile(NESTED_EXCEPTION_REGEX);
        }
        return nestedExceptPattern;
    }
    
    /** */
    public Pattern getCallstackLinePattern() {
        if (callstackLinePattern == null) {
            callstackLinePattern = Pattern.compile(CALLSTACK_LINE_REGEX);
        }
        return callstackLinePattern;
    }
    
    /** */
    public Pattern getLocationInFilePattern() {
        if (locationInFilePattern == null) {
            locationInFilePattern = Pattern.compile(LOCATION_IN_FILE_REGEX);
        }
        return locationInFilePattern;
    }
    
    /** */
    public Pattern getXmlDeclPattern() {
        if (xmlDeclPattern == null) {
            xmlDeclPattern = Pattern.compile(XML_DECL_REGEX);
        }
        return xmlDeclPattern;
    }
    
    /** */
    public Pattern getFloatNumPattern() {
        if (floatNumPattern == null) {
            floatNumPattern = Pattern.compile(FLOAT_NUMBER_REGEX);
        }
        return floatNumPattern;
    }

    /** */
    public Pattern getComparisonPattern() {
        if (comparisonPattern == null) {
            comparisonPattern = Pattern.compile(COMPARISON_REGEX);
        }
        return comparisonPattern;
    }

    /** */
    public Pattern getComparisonHiddenPattern() {
        if (comparisonHiddenPattern == null) {
            comparisonHiddenPattern = Pattern.compile(COMPARISON_HIDDEN_REGEX);
        }
        return comparisonHiddenPattern;
    }

    /**
     * Parses a floating-point number describing elapsed time.
     * The returned number is a number of elapsed milliseconds.
     *
     * @param  string represeting non-negative floating-point number of seconds
     * @return  integer representing number of milliseconds (rounded)
     * @exception  java.lang.NumberFormatException
     *             if the passed string does not match
     *             the {@link #FLOAT_NUMBER_REGEX} pattern
     */
    public int parseTimeMillis(String timeString) throws NumberFormatException {
        int secs, millis;
        final int dotIndex = timeString.indexOf('.');
        if (dotIndex == -1) {
            secs = Integer.parseInt(timeString);
            millis = 0;
        } else {
            secs = (dotIndex == 0)
                   ? 0
                   : Integer.parseInt(timeString.substring(0, dotIndex));

            String fractString = timeString.substring(dotIndex + 1);
            if (fractString.length() > 4) {
                fractString = fractString.substring(0, 4);
            }
            int fractNum = Integer.parseInt(fractString);
            switch (fractString.length()) {
                case 1:
                    millis = 100 * fractNum;
                    break;
                case 2:
                    millis = 10 * fractNum;
                    break;
                case 3:
                    millis = fractNum;
                    break;
                case 4:
                    millis = (fractNum + 5) / 10;
                    break;
                default:
                    assert false;
                    millis = 0;
                    break;
            }
        }
        return 1000 * secs + millis;
    }

    /**
     * Parses a floating-point number describing elapsed time.
     * The returned number is a number of elapsed milliseconds.
     *
     * @param  string represeting non-negative floating-point number of seconds
     * @return  integer representing number of milliseconds (rounded),
     *          or <code>-1</code> if the passed string is <code>null</code>
     *          or if it does not match the {@link #FLOAT_NUMBER_REGEX} pattern
     */
    public int parseTimeMillisNoNFE(String timeStr) {
        if ((timeStr == null)
                || !getFloatNumPattern().matcher(timeStr).matches()) {
            return -1;
        }
        try {
            return parseTimeMillis(timeStr);
        } catch (NumberFormatException ex) {
            assert false;
            return -1;
        }
    }

    /**
     * Trims leading and trailing spaces and tabs from a string.
     *
     * @param  string  string to remove spaces and tabs from
     * @return  the trimmed string, or the passed string if no trimming
     *          was necessary
     */
    public static String specialTrim(String string) {
        
        /* Handle the trivial case: */
        final int len = string.length();
        if (len == 0) {
            return string;
        }
        
        final char[] chars = string.toCharArray();
        char c;

        int lead = 0;
        while (lead < len) {
            c = chars[lead];
            if ((c != ' ') && (c != '\t')) {
                break;
            }
            lead++;
        }
        
        /* Handle a corner case: */
        if (lead == len) {
            return string.substring(len);                                              
        }
        
        int trail = len;
        do {
            c = chars[--trail];
        } while ((c == ' ') || (c == '\t'));
        
        if ((lead == 0) && (trail == len - 1)) {
            return string;
        } else {
            return string.substring(lead, trail + 1);
        }
    }
    
}
