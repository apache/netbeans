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
package org.netbeans.modules.java.api.common.queries;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;
import org.w3c.dom.Element;

public class QuerySupportTest extends NbTestCase {

    public QuerySupportTest(String testName) {
        super(testName);
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
    }

    public void testAntHelperProject() throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
        Project p = ProjectManager.getDefault().findProject(projdir);

        ProjectInformation pi = QuerySupport.createProjectInformation(h, p, null);
        assertEquals("???", pi.getDisplayName());
        assertEquals("___", pi.getName());
        Element data = h.getPrimaryConfigurationData(true);
        Element name = data.getOwnerDocument().createElementNS("urn:test:shared", "name");
        name.setTextContent("Test AntProjectHelper");
        data.appendChild(name);
        h.putPrimaryConfigurationData(data, true);

        assertEquals("Test AntProjectHelper", pi.getDisplayName());
        assertEquals("Test_AntProjectHelper", pi.getName());
    }

    public void testAntUpdateHelperProject() throws IOException {
        AntProjectHelper ah = ProjectGenerator.createProject(projdir, "test");
        Project p = ProjectManager.getDefault().findProject(projdir);
        UpdateImplementation upi = createUpdateImpl(ah);
        UpdateHelper uh = new UpdateHelper(upi, ah);

        ProjectInformation pi = QuerySupport.createProjectInformation(uh, p, null);
        assertEquals("???", pi.getDisplayName());
        assertEquals("___", pi.getName());
        Element data = uh.getPrimaryConfigurationData(true);
        Element name = data.getOwnerDocument().createElementNS("urn:test:shared", "name");
        name.setTextContent("Test UpdateHelper");
        data.appendChild(name);
        uh.putPrimaryConfigurationData(data, true);

        assertEquals("Test UpdateHelper", pi.getDisplayName());
        assertEquals("Test_UpdateHelper", pi.getName());
    }

    private UpdateImplementation createUpdateImpl(final AntProjectHelper ah) {
        return new UpdateImplementation() {

            @Override
            public boolean isCurrent() {
                return false;
            }

            @Override
            public boolean canUpdate() {
                return true;
            }

            @Override
            public void saveUpdate(EditableProperties props) throws IOException {
                // do nothing
            }

            @Override
            public Element getUpdatedSharedConfigurationData() {
                return ah.getPrimaryConfigurationData(true);
            }

            @Override
            public EditableProperties getUpdatedProjectProperties() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
