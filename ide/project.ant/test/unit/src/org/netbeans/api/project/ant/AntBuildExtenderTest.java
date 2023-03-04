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

package org.netbeans.api.project.ant;

import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelperTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test functionality of AntBuildExtender.
 * @author mkleint
 */
public class AntBuildExtenderTest extends NbTestCase {
    
    public AntBuildExtenderTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject extension1;
    private ProjectManager pm;
    private Project p;
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
        extension1 = TestUtil.createFileFromContent(GeneratedFilesHelperTest.class.getResource("data/extension1.xml"), projdir, "nbproject/extension1.xml");
        extenderImpl = new ExtImpl();
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType(extenderImpl));
        pm = ProjectManager.getDefault();
        p = pm.findProject(projdir);
        extenderImpl.project = p;
        gfh = p.getLookup().lookup(GeneratedFilesHelper.class);
        assertNotNull(gfh);
    }
    
    public void testGetExtendableTargets() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);

        List<String> result = instance.getExtensibleTargets();

        assertEquals(1, result.size());
        assertEquals("all", result.get(0));
    }

    public void testAddExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        instance.addExtension("milos", extension1);
        Element el = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(
                AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);
        assertNotNull(el);
        NodeList nl = el.getElementsByTagName(AntBuildExtenderAccessor.ELEMENT_EXTENSION);
        assertEquals(1, nl.getLength());
        Element extens = (Element) nl.item(0);
        assertEquals("milos", extens.getAttribute(AntBuildExtenderAccessor.ATTR_ID));
        assertEquals("extension1.xml", extens.getAttribute(AntBuildExtenderAccessor.ATTR_FILE));
    }

    public void testRemoveExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        testAddExtension();
        instance.removeExtension("milos");
        Element el = p.getLookup().lookup(AuxiliaryConfiguration.class).getConfigurationFragment(
                AntBuildExtenderAccessor.ELEMENT_ROOT, AntBuildExtenderAccessor.AUX_NAMESPACE, true);

        assertNotNull(el);
        NodeList nl = el.getElementsByTagName(AntBuildExtenderAccessor.ELEMENT_EXTENSION);
        assertEquals(0, nl.getLength());
    }

    public void testGetExtension() {
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        testAddExtension();
        AntBuildExtender.Extension ext = instance.getExtension("milos");
        assertNotNull(ext);
    }

    public void testBrokenProject() throws Exception { // ##192915
        AntBuildExtender instance = p.getLookup().lookup(AntBuildExtender.class);
        projdir.getFileObject("nbproject/project.xml").delete();
        assertNull(instance.getExtension("whatever"));
    }
    
    private static class ExtImpl implements AntBuildExtenderImplementation {
        Project project;
        List<String> targets = Collections.singletonList("all");

        public @Override List<String> getExtensibleTargets() {
            return targets;
        }

        public @Override Project getOwningProject() {
            return project;
        }

    }
    
}
