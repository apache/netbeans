/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.selenium2.webclient.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
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
