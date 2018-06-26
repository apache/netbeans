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
