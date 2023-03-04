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
