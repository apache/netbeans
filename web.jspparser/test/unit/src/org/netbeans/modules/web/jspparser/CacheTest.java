/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various test cases for jsp parser cache.
 * @author Tomas Mysik
 */
public class CacheTest extends NbTestCase {

    public CacheTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);
    }

    @Override
    protected void tearDown() throws Exception {
        getJspParser().parseSupports.clear();
        super.tearDown();
    }

    public void testJspParserImpl() throws Exception {
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        assertTrue(jspParser instanceof JspParserImpl);
    }

    public void testCachedWebModules() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo1 = TestUtil.getProjectFile(this, "project2", "/web/basic.jspx");
        WebModule webModule1 = TestUtil.getWebModule(jspFo1);
        jspParser.analyzePage(jspFo1, webModule1, JspParserAPI.ERROR_IGNORE);

        FileObject jspFo2 = TestUtil.getProjectFile(this, "project2", "/web/main.jsp");
        WebModule webModule2 = TestUtil.getWebModule(jspFo2);
        jspParser.analyzePage(jspFo1, webModule2, JspParserAPI.ERROR_IGNORE);

        assertTrue("Only 1 web module should be cached", jspParser.parseSupports.size() == 1);
    }

    public void testGCedWebModules() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "project2", "/web/basic.jspx");
        WebModule webModule = TestUtil.createWebModule(jspFo.getParent());
        jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);

        Reference<WebAppParseProxy> proxy = new WeakReference<WebAppParseProxy>(jspParser.parseSupports.get(webModule));
        assertNotNull("WebModule should be cached", proxy.get());

        Reference<WebModule> wmRef = new WeakReference<WebModule>(webModule);
        webModule = null;
        assertGC("web module should be garbage collected", wmRef);
        jspParser.parseSupports.size();
        assertGC("parse proxy should be garbage collected", proxy);
    }

    public void testCachedTagLibMaps() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);
        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("/TestTagLibrary")[0];
        String url2 = taglibMap2.get("/TestTagLibrary")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertSame("TagLibMaps should be exactly the same", url1, url2);
    }

    // disabled because this functionality is not implemented
    public void xxxtestCachedTagLibInfos() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "project2", "/web/basic.jspx");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        Collection<TagLibraryInfo> tagLibs1 = result.getPageInfo().getTaglibs();

        jspFo = TestUtil.getProjectFile(this, "project2", "/web/basic.jspx");
        result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        Collection<TagLibraryInfo> tagLibs2 = result.getPageInfo().getTaglibs();

        assertTrue(tagLibs1.size() > 0);
        assertTrue(tagLibs2.size() > 0);
        assertTrue(tagLibs1.size() == tagLibs2.size());

        Iterator<TagLibraryInfo> iter1 = tagLibs1.iterator();
        Iterator<TagLibraryInfo> iter2 = tagLibs2.iterator();
        while (iter1.hasNext()) {
            TagLibraryInfo tagLibraryInfo1 = iter1.next();
            TagLibraryInfo tagLibraryInfo2 = iter2.next();
            assertNotNull(tagLibraryInfo1);
            assertNotNull(tagLibraryInfo2);
            assertTrue("TagLibInfos should be exactly the same", tagLibraryInfo1 == tagLibraryInfo2);
        }
    }

    public void testChangedTldFile() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // touch file
        touchFile("emptyWebProject", "/web/WEB-INF/c.tld");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    public void testAddedTldFile() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        String[] url = taglibMap1.get("http://java.sun.com/jstl/xml");
        assertNull("Url should not be found", url);

        // add file
        addXmlTld();

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        url = taglibMap2.get("http://java.sun.com/jstl/xml");
        assertNotNull("Url should be found", url);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);

        // cleanup
        jspParser = null;
        removeXmlTld();
    }

    public void testRemovedTldFile() throws Exception {
        // add file to have possibility to remove it
        addXmlTld();

        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        String[] url = taglibMap1.get("http://java.sun.com/jstl/xml");
        assertNotNull("Url should be found", url);

        // touch file
        removeXmlTld();

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        url = taglibMap2.get("http://java.sun.com/jstl/xml");
        assertNull("Url should not be found", url);

        String url1 = taglibMap1.get("/TestTagLibrary")[0];
        String url2 = taglibMap2.get("/TestTagLibrary")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    public void testChangedWebXml() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // touch file
        touchFile("emptyWebProject", "/web/WEB-INF/web.xml");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    // #133702: Editor ignores web.xml <include-prelude> tag
	/* Commented out - see bug 194639.
    public void testIssue133702() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "project2", "/web/main_2.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        // web.xml with no include
        ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        List includePrelude = result.getPageInfo().getIncludePrelude();
        assertEquals(0, includePrelude.size());

        // web.xml with include
        copyXmlWithInclude();
        result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        includePrelude = result.getPageInfo().getIncludePrelude();
        assertEquals(1, includePrelude.size());

        // back web.xml with no include
        copyXmlWithoutInclude();
        result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        includePrelude = result.getPageInfo().getIncludePrelude();
        assertEquals(0, includePrelude.size());
    }
	 */

    private static JspParserImpl getJspParser() {
        return (JspParserImpl) JspParserFactory.getJspParser();
    }

    private void addXmlTld() throws Exception {
        FileObject xml = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/META-INF/x.tld");
        FileObject destDir = TestUtil.getProjectFile(this, "emptyWebProject", "/web/WEB-INF/");
        xml.copy(destDir, xml.getName(), xml.getExt());
        xml = TestUtil.getProjectFile(this, "emptyWebProject", "/web/WEB-INF/x.tld");
    }

    private void removeXmlTld() throws Exception {
        removeFile("emptyWebProject", "/web/WEB-INF/x.tld");
    }

    private void touchFile(String projectName, String projectFile) throws Exception {
        FileObject fmtFo = TestUtil.getProjectFile(this, projectName, projectFile);
        assertNotNull(fmtFo);
        File fmt = FileUtil.toFile(fmtFo);
        assertTrue("Changing timestamp should succeed", fmt.setLastModified(System.currentTimeMillis() + 1000));
        FileUtil.refreshFor(fmt);
    }

    private void removeFile(String projectName, String projectFile) throws Exception {
        FileObject fmtFo = TestUtil.getProjectFile(this, projectName, projectFile);
        assertNotNull(fmtFo);
        fmtFo.delete();
    }

    private void copyXmlWithInclude() throws Exception {
        FileObject source = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/web.xml.include-prelude");
        FileObject target = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/web.xml");
        copy(source, target);
    }

    private void copyXmlWithoutInclude() throws Exception {
        FileObject source = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/web.xml.default");
        FileObject target = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/web.xml");
        copy(source, target);
    }

    private void copy(FileObject source, FileObject target) throws Exception {
        InputStream is = source.getInputStream();
        OutputStream os = target.getOutputStream();
        try {
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }
}
