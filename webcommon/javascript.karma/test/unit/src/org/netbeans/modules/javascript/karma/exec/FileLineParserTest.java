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

package org.netbeans.modules.javascript.karma.exec;

import org.junit.Test;
import org.netbeans.modules.javascript.karma.browsers.util.TestUtils;

public class FileLineParserTest {

    @Test
    public void testValidFilePatterns() {
        assertFilePattern("/home/gapon/NetBeansProjects/Calculator-PHPUnit5/README.md:1",
                "/home/gapon/NetBeansProjects/Calculator-PHPUnit5/README.md", 1);
        assertFilePattern("/home/gapon/NetBeans Projects/Calculator-PHPUnit5/README.md:1",
                "/home/gapon/NetBeans Projects/Calculator-PHPUnit5/README.md", 1);

        assertFilePattern("C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1);
        assertFilePattern("C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1);

        assertFilePattern("at Module.load (/usr/lib/node_modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js:211:36)",
                "/usr/lib/node_modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js", 211);
        assertFilePattern("at Module.load (/usr/lib/node modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js:211:36)",
                "/usr/lib/node modules/karma/node_modules/coffee-script/lib/coffee-script/coffee-script.js", 211);

        assertFilePattern("at Module.load (C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:211:36)",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 211);
        assertFilePattern("at Module.load (C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:211:36)",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 211);

        assertFilePattern("at Object.<anonymous> (/home/gapon/worx/sun/nb-main/nbbuild/netbeans/webcommon/karma/karma-netbeans.conf.js:55:19)",
                "/home/gapon/worx/sun/nb-main/nbbuild/netbeans/webcommon/karma/karma-netbeans.conf.js", 55);
        assertFilePattern("at Object.<anonymous> (/home/gapon/worx/sun/nb-main/nbbuild/net beans/webcommon/karma/karma-netbeans.conf.js:55:19)",
                "/home/gapon/worx/sun/nb-main/nbbuild/net beans/webcommon/karma/karma-netbeans.conf.js", 55);
    }

    void assertFilePattern(String input, String file, int line) {
        TestUtils.assertFileLinePattern(KarmaExecutable.FileLineParser.OUTPUT_FILE_LINE_PATTERN, input, file, line);
    }

}
