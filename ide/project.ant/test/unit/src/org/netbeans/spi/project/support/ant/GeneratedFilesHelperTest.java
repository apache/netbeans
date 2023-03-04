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

package org.netbeans.spi.project.support.ant;

import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test functionality of GeneratedFilesHelper.
 * @author Jesse Glick
 */
public class GeneratedFilesHelperTest extends NbTestCase {
    
    public GeneratedFilesHelperTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    private Project p;
    private AntProjectHelper h;
    private GeneratedFilesHelper gfh;
    private ExtImpl extenderImpl;
    
    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        TestUtil.createFileFromContent(GeneratedFilesHelperTest.class.getResource("data/project.xml"), projdir, "nbproject/project.xml");
        TestUtil.createFileFromContent(GeneratedFilesHelperTest.class.getResource("data/extension1.xml"), projdir, "nbproject/extension1.xml");
        extenderImpl = new ExtImpl();
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType(extenderImpl));
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        extenderImpl.project = p;
        h = p.getLookup().lookup(AntProjectHelper.class);
        gfh = p.getLookup().lookup(GeneratedFilesHelper.class);
        assertNotNull(gfh);
    }
    
    /**
     * Test that creating build-impl.xml from project.xml + build-impl.xsl works.
     * @throws Exception if anything unexpected happens
     */
    public void testGenerateBuildScriptFromStylesheet() throws Exception {
        // Make sure there is some build-impl.xml.
        FileObject bi = projdir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        assertNull("No build-impl.xml yet", bi);
        // Modify shared data in a project.
        Element primdata = h.getPrimaryConfigurationData(true);
        Element oldDisplayName = XMLUtil.findElement(primdata, "display-name", "urn:test:shared");
        assertNotNull("had a <display-name> before", oldDisplayName);
        Element displayName = primdata.getOwnerDocument().createElementNS("urn:test:shared", "display-name");
        displayName.appendChild(primdata.getOwnerDocument().createTextNode("New Name"));
        primdata.insertBefore(displayName, oldDisplayName);
        primdata.removeChild(oldDisplayName);
        h.putPrimaryConfigurationData(primdata, true);
        assertTrue("project is modified", pm.isModified(p));
        pm.saveProject(p);
        // Ensure that build-impl.xml was (correctly) regenerated.
        FileObject genfiles = projdir.getFileObject(GeneratedFilesHelper.GENFILES_PROPERTIES_PATH);
        assertNotNull("genfiles.properties exists", genfiles);
        bi = projdir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        assertNotNull("saving the project with a project.xml change regenerates build-impl.xml", bi);
        Document doc = AntBasedTestUtil.slurpXml(h, GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        Element el = doc.getDocumentElement();
        assertEquals("build-impl.xml is a <project>", "project", el.getLocalName());
        assertEquals("<project> has no namespace", null, el.getNamespaceURI());
        NodeList l = doc.getElementsByTagName("description");
        assertEquals("one <description> in build-impl.xml", 1, l.getLength());
        el = (Element)l.item(0);
        assertEquals("correct description", "New Name", XMLUtil.findText(el));
        // Clear build-impl.xml to test if it is rewritten.
        bi.delete();
        // Now make some irrelevant change - e.g. to private.xml - and check that there is no modification.
        Element data = h.getPrimaryConfigurationData(false);
        data.setAttribute("someattr", "someval");
        h.putPrimaryConfigurationData(data, false);
        assertTrue("project is modified", pm.isModified(p));
        pm.saveProject(p);
        bi = projdir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
        assertNull("saving a private.xml change does not regenerate build-impl.xml", bi);
    }
    
    /**
     * Test that fooling with a build script in various ways is correctly detected.
     * @throws Exception if anything unexpected happens
     */
    public void testGetBuildScriptState() throws Exception {
        URL xslt = GeneratedFilesHelperTest.class.getResource("data/build.xsl");
        URL xslt2 = GeneratedFilesHelperTest.class.getResource("data/build2.xsl");
        String path = GeneratedFilesHelper.BUILD_XML_PATH;
        assertEquals("initially there is no build.xml",
            GeneratedFilesHelper.FLAG_MISSING, gfh.getBuildScriptState(path, xslt));
        assertEquals("stylesheet version ignored for FLAG_MISSING",
            GeneratedFilesHelper.FLAG_MISSING, gfh.getBuildScriptState(path, xslt2));
        gfh.generateBuildScriptFromStylesheet(path, xslt);
        assertEquals("now build.xml is there and clean",
            0, gfh.getBuildScriptState(path, xslt));
        assertEquals("build.xml is using first stylesheet",
            GeneratedFilesHelper.FLAG_OLD_STYLESHEET, gfh.getBuildScriptState(path, xslt2));
        File buildXml= FileUtil.toFile(projdir.getFileObject("build.xml"));
        assertEquals("one replacement", 1, AntBasedTestUtil.replaceInFile(buildXml, "name=\"somename\"", "name=\"someothername\""));
        assertEquals("now build.xml is modified",
            GeneratedFilesHelper.FLAG_MODIFIED, gfh.getBuildScriptState(path, xslt));
        assertEquals("one replacement", 1, AntBasedTestUtil.replaceInFile(buildXml, "name=\"someothername\"", "name=\"somename\""));
        assertEquals("now build.xml is clean again",
            0, gfh.getBuildScriptState(path, xslt));
        File projectXml= FileUtil.toFile(projdir.getFileObject("nbproject/project.xml"));
        assertEquals("one replacement", 1, AntBasedTestUtil.replaceInFile(projectXml, "<name>somename</name>", "<name>newname</name>"));
        assertEquals("now build.xml is out of date w.r.t. project.xml",
            GeneratedFilesHelper.FLAG_OLD_PROJECT_XML, gfh.getBuildScriptState(path, xslt));
        assertEquals("build.xml is out of date w.r.t. project.xml and new XSLT",
            GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_OLD_STYLESHEET, gfh.getBuildScriptState(path, xslt2));
        assertEquals("one replacement", 1, AntBasedTestUtil.replaceInFile(buildXml, "name=\"somename\"", "name=\"someothername\""));
        assertEquals("build.xml is modified and out of date w.r.t. project.xml",
            GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_MODIFIED, gfh.getBuildScriptState(path, xslt));
        assertEquals("build.xml is modified and out of date w.r.t. project.xml and new XSLT",
            GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_MODIFIED | GeneratedFilesHelper.FLAG_OLD_STYLESHEET, gfh.getBuildScriptState(path, xslt2));
        gfh.generateBuildScriptFromStylesheet(path, xslt2);
        assertEquals("now regenerated build.xml is up to date",
            0, gfh.getBuildScriptState(path, xslt2));
        // Check newline conventions. First normalize project.xml if running on Windows or Mac.
        AntBasedTestUtil.replaceInFile(projectXml, "\r\n", "\n");
        AntBasedTestUtil.replaceInFile(projectXml, "\r", "\n");
        gfh.generateBuildScriptFromStylesheet(path, xslt);
        assertEquals("build.xml is clean",
            0, gfh.getBuildScriptState(path, xslt));
        int count = AntBasedTestUtil.replaceInFile(projectXml, "\n", "\r\n");
        assertTrue("Changed newlines", count > 0);
        assertEquals("build.xml is still clean w.r.t. changed newlines in project.xml",
            0, gfh.getBuildScriptState(path, xslt));
        // XXX check also newline changes in stylesheet and build.xml
    }
    
    /**
     * Test normalization of newlines in CRC-32 computations.
     * @throws Exception if anything unexpected happens
     */
    public void testComputeCrc32() throws Exception {
        String testDataNl = "hi mom\nhow are you\n";
        String testDataCrNl = "hi mom\r\nhow are you\r\n";
        String testDataCr = "hi mom\rhow are you\r";
        String crcNl = GeneratedFilesHelper.computeCrc32(new ByteArrayInputStream(testDataNl.getBytes(StandardCharsets.UTF_8)));
        String crcCrNl = GeneratedFilesHelper.computeCrc32(new ByteArrayInputStream(testDataCrNl.getBytes(StandardCharsets.UTF_8)));
        String crcCr = GeneratedFilesHelper.computeCrc32(new ByteArrayInputStream(testDataCr.getBytes(StandardCharsets.UTF_8)));
        assertEquals("CRNL normalized -> NL", crcNl, crcCrNl);
        assertEquals("CR normalized -> NL", crcNl, crcCr);
    }

    public void testEolOnWindows() throws Exception {
        if (Utilities.isWindows()) {
            URL xslt = GeneratedFilesHelperTest.class.getResource("data/build.xsl");
            String path = GeneratedFilesHelper.BUILD_XML_PATH;
            assertEquals("initially there is no build.xml",
                GeneratedFilesHelper.FLAG_MISSING, gfh.getBuildScriptState(path, xslt));
            gfh.generateBuildScriptFromStylesheet(path, xslt);
            assertEquals("now build.xml is there and clean",
                0, gfh.getBuildScriptState(path, xslt));
            File buildXml= FileUtil.toFile(projdir.getFileObject("build.xml"));
            StringBuilder sb = new StringBuilder(AntBasedTestUtil.slurpText(h, path));
            boolean ok = true;
            for (int i=1; i<sb.length(); i++) {
                if (sb.charAt(i) == '\n') {
                    if (sb.charAt(i-1) != '\r') {
                        ok = false;
                        break;
                    }
                }
            }
            assertTrue("generated file has platform line endings", ok);
        }
    }

    public void testVersionSeeSawing() throws Exception { // #42735
        URL xslt = GeneratedFilesHelperTest.class.getResource("data/build.xsl");
        URL xslt2 = GeneratedFilesHelperTest.class.getResource("data/build2.xsl");
        GeneratedFilesHelper.STYLESHEET_VERSIONS.put(xslt, new SpecificationVersion("1.0"));
        GeneratedFilesHelper.STYLESHEET_VERSIONS.put(xslt2, new SpecificationVersion("1.1"));
        assertTrue(gfh.refreshBuildScript("build.xml", xslt, true));
        FileObject buildXml = projdir.getFileObject("build.xml");
        assertTrue(buildXml.asText("UTF-8").contains("Build everything."));
        assertFalse(gfh.refreshBuildScript("build.xml", xslt, true));
        assertTrue(buildXml.asText("UTF-8").contains("Build everything."));
        assertTrue(gfh.refreshBuildScript("build.xml", xslt2, true));
        assertTrue(buildXml.asText("UTF-8").contains("Build everything at once."));
        assertFalse(gfh.refreshBuildScript("build.xml", xslt2, true));
        assertTrue(buildXml.asText("UTF-8").contains("Build everything at once."));
        assertFalse(gfh.refreshBuildScript("build.xml", xslt, true));
        assertTrue(buildXml.asText("UTF-8").contains("Build everything at once."));
    }
    
    private class ExtImpl implements AntBuildExtenderImplementation {
        Project project;
        Element newElement;
        Element oldElement;

        public List<String> getExtensibleTargets() {
            return Collections.singletonList("all");
        }

        public void updateBuildExtensionMetadata(Element element) {
            newElement = element;
        }

        public Element getBuildExtensionMetadata() {
            Element el = project.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(AntBuildExtenderAccessor.ELEMENT_ROOT, "urn:test:extension", true);
            if (el != null) {
                NodeList nl = el.getElementsByTagName(AntBuildExtenderAccessor.ELEMENT_ROOT);
                if (nl.getLength() == 1) {
                    return (Element) nl.item(0);
                }
            }
            return null;
        }

        public Project getOwningProject() {
            return project;
        }

    }
    
}
