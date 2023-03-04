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

package org.netbeans.modules.javascript.karma.browsers;

import org.junit.Test;
import org.netbeans.modules.javascript.karma.browsers.util.TestUtils;

public class ChromeBasedTest {

    @Test
    public void testValidFilePatterns() {
        assertFilePattern("at null.<anonymous> (/home/gapon/NetBeansProjects/angular.js/test/ngCookies/cookiesSpec.js:34:19)",
                "/home/gapon/NetBeansProjects/angular.js/test/ngCookies/cookiesSpec.js", 34);
        assertFilePattern("at null.<anonymous> (/home/gapon/NetBeans Projects/angular.js/test/ngCookies/cookiesSpec.js:34:19)",
                "/home/gapon/NetBeans Projects/angular.js/test/ngCookies/cookiesSpec.js", 34);
        assertFilePattern("at null.<anonymous> (C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:34:19)",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 34);
        assertFilePattern("at null.<anonymous> (C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:34:19)",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 34);
        assertFilePattern("at /home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js:16:19",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("at /home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js:16:19",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("at C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16:19",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
        assertFilePattern("at C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16:19",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
    }

    @Test
    public void testInvalidFilePatterns() {
        assertFilePattern("/home/gapon/NetBeansProjects/angular.js/src/auto/injector.js:6:12604",
                null, -1);
        assertFilePattern("C:\\NetBeansProjects\\angular.js\\src\\auto\\injector.js:6:12604",
                null, -1);
        assertFilePattern("(/home/gapon/NetBeansProjects/angular.js/src/auto/injector.js:6)",
                null, -1);
    }

    void assertFilePattern(String input, String file, int line) {
        TestUtils.assertFileLinePattern(ChromeBased.OUTPUT_FILE_LINE_PATTERN, input, file, line);
    }

}
