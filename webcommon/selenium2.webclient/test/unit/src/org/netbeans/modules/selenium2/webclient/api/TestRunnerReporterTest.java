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
package org.netbeans.modules.selenium2.webclient.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Theofanis Oikonomou
 */
public class TestRunnerReporterTest extends TestCase {
    
    public TestRunnerReporterTest() {
    }
    
    @Test
    public void testFilePatternRegex() throws Exception {
        Pattern pattern_windows = TestRunnerReporter.CallStackCallback.FILE_LINE_PATTERN_WINDOWS;
        Pattern pattern_unix = TestRunnerReporter.CallStackCallback.FILE_LINE_PATTERN_UNIX;
        
        final String[][] matchingStrings = new String[][]{
            {"at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29", null, null, null, "/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1425", "29"},
            {"at webdriver.promise.ControlFlow.runInNewFrame_ (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1654:20)", null, null, null, "(/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1654", "20"},
            {"at C:\\Users\\toikonom\\AppData\\Local\\Temp\\AngularJSPhoneCat\\node_modules\\protractor\\lib\\protractor.js:1041:17", null, null, "C:", "\\Users\\toikonom\\AppData\\Local\\Temp\\AngularJSPhoneCat\\node_modules\\protractor\\lib\\protractor.js", "1041", "17"},
            {"at [object Object].webdriver.promise.ControlFlow.runInNewFrame_ (C:\\Users\\toikonom\\AppData\\Local\\Temp\\AngularJSPhoneCat\\node_modules\\protractor\\node_modules\\selenium-webdriver\\lib\\webdriver\\promise.js:1539:20)", null, null, "C:", "\\Users\\toikonom\\AppData\\Local\\Temp\\AngularJSPhoneCat\\node_modules\\protractor\\node_modules\\selenium-webdriver\\lib\\webdriver\\promise.js", "1539", "20"},
            {"[chrome #1] at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29", "chrome", "1", null, "/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1425", "29"},
            {"[chrome #1] at webdriver.promise.ControlFlow.runInNewFrame_ (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1654:20)", "chrome", "1", null, "(/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1654", "20"},
            {"[chrome #1]   at /Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1425:29", "chrome", "1", null, "/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1425", "29"},
            {"[chrome #1]   at webdriver.promise.ControlFlow.runInNewFrame_ (/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js:1654:20)", "chrome", "1", null, "(/Users/fanis/selenium2_work/NodeJsApplication/node_modules/selenium-webdriver/lib/webdriver/promise.js", "1654", "20"},
            {"at Context.<anonymous> (test/test.js:8:24)", null, null, null, "(test/test.js", "8", "24"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern_windows.matcher(string);
            boolean matchFound = matcher.find();
            String drive = null;
            if(matchFound) {
                drive = matcher.group("DRIVE"); // NOI18N
            } else {
                matcher = pattern_unix.matcher(string);
                matchFound = matcher.find();
            }
            assertTrue("should match: " + string, matchFound);
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], drive);
            assertEquals(matchingStrings[i][4], matcher.group("FILE"));
            assertEquals(matchingStrings[i][5], matcher.group("LINE"));
            assertEquals(matchingStrings[i][6], matcher.group("COLUMN"));
        }
    }
    
    @Test
    public void testOKRegex() throws Exception {
        Pattern pattern = TestRunnerReporter.OK_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"ok 1 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=1234", null, null, "1", "Google Search should append query to title", "Google Search", "should append query to title", "1234"},
            {"[chrome #1] ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=1234", "chrome", "1", "2", "Google Search should append query to title", "Google Search", "should append query to title", "1234"},
            {"[chrome #1] .F..ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=1234", "chrome", "1", "2", "Google Search should append query to title", "Google Search", "should append query to title", "1234"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][4], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][5], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][6], matcher.group("TESTCASE"));
            assertEquals(matchingStrings[i][7], matcher.group("DURATION"));
        }
    }
    
    @Test
    public void testOKSkipRegex() throws Exception {
        Pattern pattern = TestRunnerReporter.OK_SKIP_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"ok 1 Google Search should append query to title # SKIP -, suite=Google Search, testcase=should append query to title", null, null, "1", "Google Search should append query to title", "Google Search", "should append query to title"},
            {"[chrome #1] ok 1 Google Search should append query to title # SKIP -, suite=Google Search, testcase=should append query to title", "chrome", "1", "1", "Google Search should append query to title", "Google Search", "should append query to title"},
            {"[chrome #1] .F..ok 1 Google Search should append query to title # SKIP -, suite=Google Search, testcase=should append query to title", "chrome", "1", "1", "Google Search should append query to title", "Google Search", "should append query to title"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][4], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][5], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][6], matcher.group("TESTCASE"));
        }
    }
    
    @Test
    public void testNotOKRegex() throws Exception {
        Pattern pattern = TestRunnerReporter.NOT_OK_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"not ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=2345", null, null, "2", "Google Search should append query to title", "Google Search", "should append query to title", "2345"},
            {"[chrome #1] not ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=2345", "chrome", "1", "2", "Google Search should append query to title", "Google Search", "should append query to title", "2345"},
            {"[chrome #1] .F..not ok 2 Google Search should append query to title, suite=Google Search, testcase=should append query to title, duration=2345", "chrome", "1", "2", "Google Search should append query to title", "Google Search", "should append query to title", "2345"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], matcher.group("INDEX"));
            assertEquals(matchingStrings[i][4], matcher.group("FULLTITLE"));
            assertEquals(matchingStrings[i][5], matcher.group("SUITE"));
            assertEquals(matchingStrings[i][6], matcher.group("TESTCASE"));
            assertEquals(matchingStrings[i][7], matcher.group("DURATION"));
        }
    }
    
    @Test
    public void testSessionStartRegex() throws Exception {
        Pattern pattern = TestRunnerReporter.SESSION_START_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"1..8", null, null, "8"},
            {"[chrome #1] 1..8", "chrome", "1", "8"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], matcher.group("TOTAL"));
        }
    }
    
    @Test
    public void testSessionEndRegex() throws Exception {
        Pattern pattern = TestRunnerReporter.SESSION_END_PATTERN;
        
        final String[][] matchingStrings = new String[][]{
            {"tests 8, pass 6, fail 2, skip 1", null, null, "8", "6", "2", "1"},
            {"[chrome #1] tests 8, pass 6, fail 2, skip 1", "chrome", "1", "8", "6", "2", "1"},
            {"[chrome #1] .F..tests 8, pass 6, fail 2, skip 1", "chrome", "1", "8", "6", "2", "1"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("BROWSER"));
            assertEquals(matchingStrings[i][2], matcher.group("CAPABILITY"));
            assertEquals(matchingStrings[i][3], matcher.group("TOTAL"));
            assertEquals(matchingStrings[i][4], matcher.group("PASS"));
            assertEquals(matchingStrings[i][5], matcher.group("FAIL"));
            assertEquals(matchingStrings[i][6], matcher.group("SKIP"));
        }
    }
    
    @Test
    public void testMultiCapabilities() throws Exception {
        Pattern pattern = TestRunnerReporter.MULTI_CAPABILITIES;
        
        final String[][] matchingStrings = new String[][]{
            {"[launcher] Running 2 instances of WebDriver", "2"},
        };

        for (int i = 0; i < matchingStrings.length; i++) {
            String string = matchingStrings[i][0];
            Matcher matcher = pattern.matcher(string);
            assertTrue("should match: " + string, matcher.find());
            assertEquals(matchingStrings[i][1], matcher.group("MULTICAPABILITIES"));
        }
    }
    
}
