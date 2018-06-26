/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jspparser;

import java.lang.reflect.Field;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class ParserClassLoaderTest extends NbTestCase {

    private static final String ISSUE_WA_VENDOR = "Sun Microsystems";
    private static final String ISSUE_WA_VENDOR_ALT = "Oracle Corporation";
    private static final int ISSUE_WA_MAJOR_VERSION = 1;
    private static final int ISSUE_WA_MINOR_VERSION = 6;

    public ParserClassLoaderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);
    }

    public void testReusable() throws Exception {
        String vendor = System.getProperty("java.vendor");
        if (!vendor.contains(ISSUE_WA_VENDOR) && !vendor.contains(ISSUE_WA_VENDOR_ALT)) {
            return;
        }
        String version = System.getProperty("java.version");
        System.out.println(version);

        String[] parts = version.split("\\.");
        if (parts.length < 2 || Integer.parseInt(parts[0]) < ISSUE_WA_MAJOR_VERSION
                || (Integer.parseInt(parts[0]) == ISSUE_WA_MAJOR_VERSION && Integer.parseInt(parts[1]) < ISSUE_WA_MINOR_VERSION)) {
            return;
        }

        FileObject jspFo = TestUtil.getProjectFile(this, "project3", "/web/source.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);

        if (!(jspParser instanceof JspParserImpl)) {
            return;
        }

        JspParserImpl jspParserImpl = (JspParserImpl) jspParser;
        WebAppParseProxy proxy = jspParserImpl.parseSupports.get(webModule);

        if (!"org.netbeans.modules.web.jspparser_ext.WebAppParseSupport".equals(proxy.getClass().getName())) {
            return;
        }

        Field waClassLoaderField = proxy.getClass().getDeclaredField("waClassLoader");
        waClassLoaderField.setAccessible(true);
        ClassLoader waClassLoader = (ClassLoader) waClassLoaderField.get(proxy);
        Field waContextClassLoaderField = proxy.getClass().getDeclaredField("waContextClassLoader");
        waContextClassLoaderField.setAccessible(true);
        ClassLoader waContextClassLoader = (ClassLoader) waContextClassLoaderField.get(proxy);

        waClassLoader.loadClass("examples.LogTag");
        waClassLoader.loadClass("javax.servlet.jsp.jstl.sql.Result");
        waContextClassLoader.loadClass("examples.LogTag");
        waContextClassLoader.loadClass("javax.servlet.jsp.jstl.sql.Result");
    }

    public void testPerformanceDifference() throws Exception {
        String vendor = System.getProperty("java.vendor");
        if (!vendor.contains(ISSUE_WA_VENDOR) && !vendor.contains(ISSUE_WA_VENDOR_ALT)) {
            return;
        }
        String version = System.getProperty("java.version");
        System.out.println(version);

        String[] parts = version.split("\\.");
        if (parts.length < 2 || Integer.parseInt(parts[0]) < ISSUE_WA_MAJOR_VERSION
                || (Integer.parseInt(parts[0]) == ISSUE_WA_MAJOR_VERSION && Integer.parseInt(parts[1]) < ISSUE_WA_MINOR_VERSION)) {
            return;
        }

        FileObject jspFo = TestUtil.getProjectFile(this, "project3", "/web/source.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);
        JspParserAPI jspParser = JspParserFactory.getJspParser();

        long startWithReset = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        }
        long elapsedWithReset = System.nanoTime() - startWithReset;

        disableReset(jspParser, webModule);

        long startWithoutReset = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        }
        long elapsedWithoutReset = System.nanoTime() - startWithoutReset;

        System.out.println("Absolute difference " + ((elapsedWithReset - elapsedWithoutReset) / 1000000) + " ms");
        double perCent = ((double) elapsedWithReset/elapsedWithoutReset) * 100;
        System.out.println("Per cent difference " + perCent);

        assertTrue(perCent < 200);
    }

    private void disableReset(JspParserAPI jspParser, WebModule webModule) throws NoSuchFieldException, IllegalAccessException {
        if (!(jspParser instanceof JspParserImpl)) {
            return;
        }

        JspParserImpl jspParserImpl = (JspParserImpl) jspParser;
        WebAppParseProxy proxy = jspParserImpl.parseSupports.get(webModule);

        if (!"org.netbeans.modules.web.jspparser_ext.WebAppParseSupport".equals(proxy.getClass().getName())) {
            return;
        }

        Field noResetField = proxy.getClass().getDeclaredField("noReset");
        noResetField.setAccessible(true);
        noResetField.set(proxy, Boolean.TRUE);
    }
}
