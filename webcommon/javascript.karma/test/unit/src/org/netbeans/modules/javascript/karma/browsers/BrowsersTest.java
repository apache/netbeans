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

import java.util.Collection;
import org.junit.Test;
import org.junit.Assert;

public class BrowsersTest {

    @Test
    public void testGetChromeBased() {
        Collection<Browser> chromeBrowsers = Browsers.getBrowsers("Chrome");
        Assert.assertEquals(1, chromeBrowsers.size());
        assertContainsClass(chromeBrowsers, ChromeBased.class);

        Collection<Browser> chromeCanaryBrowsers = Browsers.getBrowsers("Chrome Canary");
        Assert.assertEquals(1, chromeCanaryBrowsers.size());
        assertContainsClass(chromeCanaryBrowsers, ChromeBased.class);

        Collection<Browser> allChromeBrowsers = Browsers.getBrowsers("Chrome", "Chrome Canary");
        Assert.assertEquals(1, allChromeBrowsers.size());
        assertContainsClass(allChromeBrowsers, ChromeBased.class);

        Assert.assertEquals(chromeBrowsers, chromeCanaryBrowsers);
        Assert.assertEquals(chromeBrowsers, allChromeBrowsers);
    }

    @Test
    public void testGetFireFox() {
        Collection<Browser> browsers = Browsers.getBrowsers("Firefox");
        Assert.assertEquals(1, browsers.size());
        assertContainsClass(browsers, Firefox.class);
    }

    @Test
    public void testGetIe() {
        Collection<Browser> browsers = Browsers.getBrowsers("IE");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetSafari() {
        Collection<Browser> browsers = Browsers.getBrowsers("Safari");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetSafariIe() {
        Collection<Browser> browsers = Browsers.getBrowsers("Safari", "IE");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetPhantomJs() {
        Collection<Browser> browsers = Browsers.getBrowsers("PhantomJS");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetPhantomJsIe() {
        Collection<Browser> browsers = Browsers.getBrowsers("PhantomJS", "IE");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetPhantomJsIeSafari() {
        Collection<Browser> browsers = Browsers.getBrowsers("PhantomJS", "IE", "Safari");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetOpera() {
        Collection<Browser> browsers = Browsers.getBrowsers("Opera");
        Assert.assertEquals(2, browsers.size());
        assertContainsClass(browsers, ChromeBased.class);
        assertContainsClass(browsers, OperaLegacy.class);
    }

    @Test
    public void testGetOperaChromeFirefox() {
        Collection<Browser> browsers = Browsers.getBrowsers("Opera", "Chrome", "Firefox");
        Assert.assertEquals(3, browsers.size());
        assertContainsClass(browsers, ChromeBased.class);
        assertContainsClass(browsers, OperaLegacy.class);
        assertContainsClass(browsers, Firefox.class);
    }

    @Test
    public void testGetUnknown() {
        Collection<Browser> browsers = Browsers.getBrowsers("Unknown");
        Assert.assertTrue(browsers.toString(), browsers.isEmpty());
    }

    @Test
    public void testGetUnknownFirefox() {
        Collection<Browser> browsers = Browsers.getBrowsers("Unknown", "Firefox");
        Assert.assertEquals(1, browsers.size());
        assertContainsClass(browsers, Firefox.class);
    }

    private void assertContainsClass(Collection<Browser> browsers, Class<?> cls) {
        for (Browser browser : browsers) {
            if (cls.equals(browser.getClass())) {
                return;
            }
        }
        Assert.fail(cls + " should be found among " + browsers);
    }

}
