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

package org.netbeans.modules.javascript.karma.run;

import org.junit.Assert;
import org.junit.Test;

public class TestRunnerTest {

    @Test
    public void testProcessDetailsChrome() {
        String details = "[\"Expected 3 to equal 1.\\n"
                + "Error: Expected 3 to equal 1.\\n"
                + "    at null.<anonymous> (http://localhost:9876/base/test/ngCookies/cookiesSpec.js?1383895872000:33:15)\\n"
                + "    at Object.invoke (http://localhost:9876/base/src/auto/injector.js?1381907169000:748:28)\\n"
                + "    at workFn (http://localhost:9876/base/src/ngMock/angular-mocks.js?1381907169000:2082:20)\"]";
        String[] expected = new String[] {
            "Expected 3 to equal 1.",
            "Error: Expected 3 to equal 1.",
            "at null.<anonymous> (/test/ngCookies/cookiesSpec.js:33:15)",
            "at Object.invoke (/src/auto/injector.js:748:28)",
            "at workFn (/src/ngMock/angular-mocks.js:2082:20)",
        };
        Assert.assertArrayEquals(expected, TestRunner.processDetails(details));
    }

    @Test
    public void testProcessDetailsFirefox() {
        String details = "Expected 3 to equal 1.\\n"
                + "@http://localhost:9876/base/test/ngCookies/cookiesSpec.js?1383895872000:33\\n"
                + "invoke@http://localhost:9876/base/src/auto/injector.js?1381907169000:748\\n"
                + "workFn@http://localhost:9876/base/src/ngMock/angular-mocks.js?1381907169000:2082\\n";
        String[] expected = new String[] {
            "Expected 3 to equal 1.",
            "@/test/ngCookies/cookiesSpec.js:33",
            "invoke@/src/auto/injector.js:748",
            "workFn@/src/ngMock/angular-mocks.js:2082",
        };
        Assert.assertArrayEquals(expected, TestRunner.processDetails(details));
    }

    @Test
    public void testIssue246885() {
        String details = "[\"Expected function not to throw 'Just a static class - can not be instantiated.'.\\n"
                + "Error: Expected function not to throw 'Just a static class - can not be instantiated.'.\\n"
                + "    at Object.<anonymous> (http://localhost:9876/base/test/modules/core/js/SettingsSpec.js?f0402995b7411305dea968049ed8d378d0665241:9:20)\"]";
        String[] expected = new String[] {
            "Expected function not to throw 'Just a static class - can not be instantiated.'.",
            "Error: Expected function not to throw 'Just a static class - can not be instantiated.'.",
            "at Object.<anonymous> (/test/modules/core/js/SettingsSpec.js:9:20)",
        };
        Assert.assertArrayEquals(expected, TestRunner.processDetails(details));
    }

    // XXX test karma output parsing

}
