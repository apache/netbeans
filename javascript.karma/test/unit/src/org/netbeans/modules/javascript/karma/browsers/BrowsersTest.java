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
