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

import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.api.project.ProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.test.MockLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test functionality of ProjectGenerator.
 * @author Jesse Glick
 */
public class ProjectGeneratorTest extends NbTestCase {
    
    /**
     * Create the test case.
     * @param name the test name
     */
    public ProjectGeneratorTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    
    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.getConfigFile("Services");
        if (fo != null) {
            fo.delete();
        }
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        MockLookup.setInstances(
            new AntBasedProjectFactorySingleton(),
            AntBasedTestUtil.testAntBasedProjectType());
    }
    
    @Override
    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        super.tearDown();
    }
    
    /**
     * Check that it is possible to create a complete new Ant-based project from scratch.
     * @throws Exception if anything fails unexpectedly
     */
    public void testCreateNewProject() throws Exception {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    // Create the new project.
                    AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
                    assertNotNull("Returned some project helper", h);
                    Project p = ProjectManager.getDefault().findProject(projdir);
                    assertNotNull("Project exists", p);
                    // Check that basic characteristics are correct.
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    assertEquals("correct directory", projdir, p.getProjectDirectory());
                    assertTrue("already modified", ProjectManager.getDefault().isModified(p));
                    // Configure it.
                    Element data = h.getPrimaryConfigurationData(true);
                    assertEquals("correct namespace for shared data", "urn:test:shared", data.getNamespaceURI());
                    assertEquals("empty initial shared data", 0, data.getChildNodes().getLength());
                    Element stuff = data.getOwnerDocument().createElementNS("urn:test:shared", "shared-stuff");
                    data.appendChild(stuff);
                    h.putPrimaryConfigurationData(data, true);
                    data = h.getPrimaryConfigurationData(false);
                    assertEquals("correct namespace for private data", "urn:test:private", data.getNamespaceURI());
                    assertEquals("empty initial private data", 0, data.getChildNodes().getLength());
                    stuff = data.getOwnerDocument().createElementNS("urn:test:private", "private-stuff");
                    data.appendChild(stuff);
                    h.putPrimaryConfigurationData(data, false);
                    EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    assertEquals("empty initial project.properties", 0, ep.size());
                    ep.setProperty("shared.prop", "val1");
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    assertEquals("empty initial private.properties", 0, ep.size());
                    ep.setProperty("private.prop", "val2");
                    h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    // Save it.
                    ProjectManager.getDefault().saveProject(p);
                    // Check that everything is OK on disk.
                    Document doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PROJECT_XML_PATH);
                    NodeList l = doc.getElementsByTagNameNS(AntProjectHelper.PROJECT_NS, "type");
                    assertEquals("one <type>", 1, l.getLength());
                    Element el = (Element)l.item(0);
                    assertEquals("correct saved type", "test", XMLUtil.findText(el));
                    l = doc.getElementsByTagNameNS("urn:test:shared", "shared-stuff");
                    assertEquals("one <shared-stuff>", 1, l.getLength());
                    doc = AntBasedTestUtil.slurpXml(h, AntProjectHelper.PRIVATE_XML_PATH);
                    l = doc.getElementsByTagNameNS("urn:test:private", "private-stuff");
                    assertEquals("one <private-stuff>", 1, l.getLength());
                    Properties props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    assertEquals("correct project.properties", Collections.singletonMap("shared.prop", "val1"), props);
                    props = AntBasedTestUtil.slurpProperties(h, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    assertEquals("correct project.properties", Collections.singletonMap("private.prop", "val2"), props);
                    doc = AntBasedTestUtil.slurpXml(h, "nbproject/build-impl.xml");
                    el = doc.getDocumentElement();
                    assertEquals("build-impl.xml is a <project>", "project", el.getLocalName());
                    assertEquals("<project> has no namespace", null, el.getNamespaceURI());
                    l = doc.getElementsByTagName("target");
                    assertEquals("two targets in build-impl.xml", 2, l.getLength());
                    el = (Element)l.item(1);
                    assertEquals("second target is \"x\"", "x", el.getAttribute("name"));
                    new GeneratedFilesHelper(h).generateBuildScriptFromStylesheet(
                        GeneratedFilesHelper.BUILD_XML_PATH, AntBasedTestUtil.testBuildXmlStylesheet());
                    doc = AntBasedTestUtil.slurpXml(h, "build.xml");
                    el = doc.getDocumentElement();
                    assertEquals("build.xml is a <project>", "project", el.getLocalName());
                    assertEquals("<project> has no namespace", null, el.getNamespaceURI());
                    l = doc.getElementsByTagName("target");
                    assertEquals("one target in build.xml", 1, l.getLength());
                    el = (Element)l.item(0);
                    assertEquals("target is \"all\"", "all", el.getAttribute("name"));
                    return null;
                }
            });
        } catch (MutexException e) {
            throw e.getException();
        }
    }
    
}
