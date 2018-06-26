/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.browsers;

import org.junit.Test;
import org.netbeans.modules.javascript.karma.browsers.util.TestUtils;

public class FirefoxTest {

    @Test
    public void testValidFilePatterns() {
        assertFilePattern("angular.mock.inject@/home/gapon/NetBeansProjects/AngularSeeed/test/lib/angular/angular-mocks.js:1939",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/lib/angular/angular-mocks.js", 1939);
        assertFilePattern("@/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeansProjects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("angular.mock.inject@/home/gapon/NetBeans Projects/AngularSeeed/test/lib/angular/angular-mocks.js:1939",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/lib/angular/angular-mocks.js", 1939);
        assertFilePattern("@/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js:16",
                "/home/gapon/NetBeans Projects/AngularSeeed/test/unit/directivesSpec.js", 16);
        assertFilePattern("angular.mock.inject@C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1939",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1939);
        assertFilePattern("@C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
                "C:\\NetBeansProjects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
        assertFilePattern("angular.mock.inject@C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:1939",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 1939);
        assertFilePattern("@C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js:16",
                "C:\\NetBeans Projects\\angular.js\\test\\ngCookies\\cookiesSpec.js", 16);
    }

    @Test
    public void testInvalidFilePatterns() {
        assertFilePattern("/home/gapon/NetBeansProjects/angular.js/src/auto/injector.js:6",
                null, -1);
        assertFilePattern("C:\\NetBeansProjects\\angular.js\\src\\auto\\injector.js:6",
                null, -1);
    }

    @Test
    public void testPatternIssue246036() {
        assertFilePattern("@/home/gapon/NetBeansProjects/AngularJSPhoneCat1/test/unit/controllersSpec.js:31:7",
                "/home/gapon/NetBeansProjects/AngularJSPhoneCat1/test/unit/controllersSpec.js", 31);
    }

    private void assertFilePattern(String input, String file, int line) {
        TestUtils.assertFileLinePattern(Firefox.OUTPUT_FILE_LINE_PATTERN, input, file, line);
    }

}
