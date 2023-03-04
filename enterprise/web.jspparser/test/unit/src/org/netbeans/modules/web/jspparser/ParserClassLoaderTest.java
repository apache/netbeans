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
