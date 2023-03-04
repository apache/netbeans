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

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author David Konecny
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testAuxiliaryConfiguration() throws Exception {
        File proj = new File(getWorkDir(), "aux_proj");
        proj.mkdir();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(proj, proj, "proj1", null);
        FileObject base = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(base);
        assertNotNull("project was created", p);
        assertEquals("expected project folder", base, p.getProjectDirectory());
        
        AuxiliaryConfiguration au = Util.getAuxiliaryConfiguration(helper);
        assertNotNull("project has AuxiliaryConfiguration", au);
    }
    
    public void testRelativizeLocation() throws Exception {
        File srcApp = Utilities.isWindows() ? new File("c:\\src\\app") : new File("/src/app");
        File srcAppFooBar = new File(srcApp, "foo" + File.separatorChar + "bar");
        File projApp = Utilities.isWindows() ? new File("c:\\proj\\app") : new File("/proj/app");
        File otherFooBar = Utilities.isWindows() ? new File("c:\\other\\foo\\bar") : new File("/other/foo/bar");
        assertEquals("foo/bar", Util.relativizeLocation(srcApp, srcApp, srcAppFooBar));
        assertEquals("${project.dir}/foo/bar", Util.relativizeLocation(srcApp, projApp, srcAppFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, srcApp, otherFooBar));
        assertEquals(otherFooBar.getAbsolutePath(), Util.relativizeLocation(srcApp, projApp, otherFooBar));
        // Mentioned incidentally in #54428:
        assertEquals(".", Util.relativizeLocation(srcApp, srcApp, srcApp));
        assertEquals("${project.dir}", Util.relativizeLocation(srcApp, projApp, srcApp));
    }

    public void testGetDefaultAntScript() throws Exception {
        assertNull("no default ant script", Util.getDefaultAntScript(extsrcroot));
        assertEquals("found build.xml", simple.getProjectDirectory().getFileObject("build.xml"), Util.getDefaultAntScript(simple));
        assertEquals("found build.xml", extbuildscript.getProjectDirectory().getFileObject("scripts/build.xml"), Util.getDefaultAntScript(extbuildscript));
    }

    public void testFormatUpgrade() throws Exception {
        AntProjectHelper helper = FreeformProjectGenerator.createProject(getWorkDir(), getWorkDir(), "prj", null);
        Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        FileObject pxml = helper.resolveFileObject(AntProjectHelper.PROJECT_XML_PATH);
        // To simplify test, overwrite project.xml with a basic version w/o <properties>, <view>, or <comment>.
        Element data = helper.getPrimaryConfigurationData(true);
        data = data.getOwnerDocument().createElementNS(data.getNamespaceURI(), data.getLocalName());
        data.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "name")).appendChild(data.getOwnerDocument().createTextNode("prj"));
        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(p);
        // Initial check.
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</></>", xmlSimplified(data));
        // Save something in a /1-compatible format.
        Element folder = (Element) data.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "folders")).
                appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "source-folder"));
        folder.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "label")).appendChild(data.getOwnerDocument().createTextNode("Sources"));
        folder.appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "location")).appendChild(data.getOwnerDocument().createTextNode("src"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</></></></>", xmlSimplified(data));
        // Save something that forces use of the /2 format.
        data.getElementsByTagName("source-folder").item(0).
                appendChild(data.getOwnerDocument().createElementNS(data.getNamespaceURI(), "excludes")).
                appendChild(data.getOwnerDocument().createTextNode("junk/"));
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</></><general-data/ff2><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</><excludes>junk/</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</><excludes>junk/</></></></>", xmlSimplified(data));
        // Save something old again.
        Element excludes = (Element) data.getElementsByTagName("excludes").item(0);
        excludes.getParentNode().removeChild(excludes);
        Util.putPrimaryConfigurationData(helper, data);
        ProjectManager.getDefault().saveProject(p);
        assertEquals("<project/p1><type.../><configuration><general-data/ff1><name>prj</>" +
                "<folders><source-folder><label>Sources</><location>src</></></></></></>", xmlSimplified(pxml));
        data = Util.getPrimaryConfigurationData(helper);
        assertEquals("<general-data/ff2><name>prj</><folders><source-folder><label>Sources</><location>src</></></></>", xmlSimplified(data));
    }
    private static String xmlSimplified(Element e) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.importNode(e, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        return xmlSimplified(baos.toString("UTF-8"));
    }
    private static String xmlSimplified(FileObject f) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = f.getInputStream();
        try {
            FileUtil.copy(is, baos);
        } finally {
            is.close();
        }
        return xmlSimplified(baos.toString("UTF-8"));
    }
    private static String xmlSimplified(String s) throws Exception {
        return s.replaceFirst("^<\\?xml.+\\?>", "").
                replaceAll("(\r|\n|\r\n) *", "").
                replace(" xmlns=\"http://www.netbeans.org/ns/project/1\"", "/p1").
                replaceAll(" xmlns=\"http://www\\.netbeans\\.org/ns/freeform-project/(\\d+)\"", "/ff$1").
                replaceAll("</[^>]+>", "</>").
                replace("<type>org.netbeans.modules.ant.freeform</>", "<type.../>");
    }
    
}
