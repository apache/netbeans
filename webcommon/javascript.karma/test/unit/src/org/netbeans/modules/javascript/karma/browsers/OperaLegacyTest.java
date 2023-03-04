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

public class OperaLegacyTest {

    @Test
    public void testValidFilePatterns() {
        assertFilePattern("<anonymous function>([arguments not available])@/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("<anonymous function>([arguments not available])@/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("<anonymous function>([arguments not available])@C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
        assertFilePattern("<anonymous function>([arguments not available])@C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
        assertFilePattern("workFn([arguments not available])@/home/gapon/NetBeansProjects/AngularSeeed/test/lib/angular/angular-mocks.js:1952",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/lib/angular/angular-mocks.js", 1952);
        assertFilePattern("workFn([arguments not available])@/home/gapon/NetBeans Projects/AngularSeeed/test/lib/angular/angular-mocks.js:1952",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/lib/angular/angular-mocks.js", 1952);
        assertFilePattern("workFn([arguments not available])@C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1952",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1952);
        assertFilePattern("workFn([arguments not available])@C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1952",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1952);
        assertFilePattern("@/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("@/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("@C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
        assertFilePattern("@C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
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

    private void assertFilePattern(String input, String file, int line) {
        TestUtils.assertFileLinePattern(OperaLegacy.OUTPUT_FILE_LINE_PATTERN, input, file, line);
    }

}
