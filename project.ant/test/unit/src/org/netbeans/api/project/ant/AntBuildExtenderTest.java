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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
