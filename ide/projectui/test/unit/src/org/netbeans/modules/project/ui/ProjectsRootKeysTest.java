/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

public class ProjectsRootKeysTest extends NbTestCase {
    static final Logger LOG = Logger.getLogger("test.ProjectsRootKeysTest");
    private Project[] projects = new Project[0];
    private ProjectsRootKeys rootKeys;
    private TestSupport.TestProject mainPrj1;
    private TestSupport.TestProject mainPrj2;
    private TestSupport.TestProject nestedPrj1;
    private final List<Project> depthUpdated = new ArrayList<>();

    public ProjectsRootKeysTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        this.rootKeys = new ProjectsRootKeys(0) {
            @Override
            Project[] listProjects() {
                return projects;
            }

            @Override
            void depthUpdated(PrjInfo info) {
                depthUpdated.add(info.project());
            }
        };

        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        FileObject prj1 = TestSupport.createTestProject(workDir, "prj1");
        FileObject prj2 = TestSupport.createTestProject(workDir, "prj2");
        FileObject nest1 = TestSupport.createTestProject(prj1, "nested1");
        mainPrj1 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj1);
        mainPrj2 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj2);
        nestedPrj1 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(nest1);
        assertNotNull("Project found", mainPrj1);
        assertNotNull("Project found", mainPrj2);
        assertNotNull("Project found", nestedPrj1);
    }

    public void testProjectsAreCoLocated() throws Exception {
        this.projects = new Project[] { mainPrj1, mainPrj2, nestedPrj1 };

        var keys = this.rootKeys.getKeys();
        assertEquals("Three keys found: " + keys, 3, keys.size());

        var it = keys.iterator();
        var k1 = it.next();
        var k2 = it.next();
        var k3 = it.next();
        assertFalse("Iterator is empty", it.hasNext());

        assertEquals("prj1 comes first", mainPrj1, k1.project());
        assertEquals("then prj1/nested1 is second", nestedPrj1, k2.project());
        assertEquals("prj2 is the last", mainPrj2, k3.project());

        assertEquals("No depth for prj1", 0, k1.depth());
        assertEquals("Depth one for nested prj", 1, k2.depth());
        assertEquals("No depth for prj2", 0, k3.depth());

        assertTrue("No depths were updated yet: " + depthUpdated, depthUpdated.isEmpty());

        //
        // now simulate closing of prj1
        //

        this.projects = new Project[] { mainPrj2, nestedPrj1 };

        it = this.rootKeys.getKeys().iterator();

        var n1 = it.next();
        var n2 = it.next();
        assertFalse("Iterator is empty", it.hasNext());

        assertEquals("prj1/nested1 comes first (alphabetically)", nestedPrj1, n1.project());
        assertEquals("prj2 is second", mainPrj2, n2.project());

        assertEquals("No depth for prj1/nested1 anymore", 0, n1.depth());
        assertEquals("No depth for prj2", 0, n2.depth());

        assertEquals("No depth in old prj1/nested1 either", 0, k2.depth());
        assertEquals("One project depth updated", 1, depthUpdated.size());
        assertEquals("It is prj/nested1", k2.project(), depthUpdated.get(0));
        depthUpdated.clear();
    }
}
