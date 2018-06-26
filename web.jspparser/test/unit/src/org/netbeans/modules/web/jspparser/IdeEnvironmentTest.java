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
