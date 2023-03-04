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

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests that need "full" IDE can be placed here.
 * @author Tomas Mysik
 */
public class IdeEnvironmentTest extends NbTestCase {

    public IdeEnvironmentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestUtil.initParserJARs();
    }

    // test for issue #70426
    public void testGetTagLibMap70426() throws Exception {
        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "web/index.jsp");
        WebModule wm = TestUtil.getWebModule(jspFo);
        Map<String, String[]> library = JspParserFactory.getJspParser().getTaglibMap(wm);
        assertNull("The JSTL library should not be present.", library.get("http://java.sun.com/jsp/jstl/fmt"));

        List<URL> urls = TestUtil.getJARs("jstl.jars");
        addToProjectClasspath("emptyWebProject", urls);

        library = JspParserFactory.getJspParser().getTaglibMap(wm);
        assertNotNull("The JSTL library should be present.", library.get("http://java.sun.com/jsp/jstl/fmt"));
    }

    public void testAddedJarFile() throws Exception {
        JspParserAPI jspParser = JspParserFactory.getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // add library
        addPathToProjectClasspath("emptyWebProject");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);

        // cleanup
        jspParser = null;
    }

    public void testRemovedJarFile() throws Exception {
        // init
        addPathToProjectClasspath("emptyWebProject");

        JspParserAPI jspParser = JspParserFactory.getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // remove library
        removePathFromProjectClasspath("emptyWebProject");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    private void removePathFromProjectClasspath(String projectFolderName) throws Exception {
        Project project = TestUtil.getProject(this, projectFolderName);
        FileObject srcJava = project.getProjectDirectory().getFileObject("src/java");
        ProjectClassPathModifier.removeRoots(new URI[]{getWorkDir().toURI()}, srcJava, ClassPath.COMPILE);
    }

    private void addPathToProjectClasspath(String projectFolderName) throws Exception {
        Project project = TestUtil.getProject(this, projectFolderName);
        FileObject srcJava = project.getProjectDirectory().getFileObject("src/java");
        boolean added = ProjectClassPathModifier.addRoots(new URI[]{getWorkDir().toURI()}, srcJava, ClassPath.COMPILE);
        assertTrue("Library should be added to the class path", added);
    }

    private void addToProjectClasspath(String projectFolderName, List<URL> urls) throws Exception {
        Project project = TestUtil.getProject(this, projectFolderName);
        FileObject srcJava = project.getProjectDirectory().getFileObject("src/java");
        for (URL u: urls) {
            u = FileUtil.getArchiveRoot(u);
            assert u != null : urls;
            boolean added = ProjectClassPathModifier.addRoots(new URL[]{u}, srcJava, ClassPath.COMPILE);
            assertTrue("Library should be added to the class path", added);
        }
    }
}
