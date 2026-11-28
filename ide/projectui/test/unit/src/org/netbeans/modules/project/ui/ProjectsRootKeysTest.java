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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class ProjectsRootKeysTest extends NbTestCase {
    static final Logger LOG = Logger.getLogger("test.ProjectsRootKeysTest");
    private Project[] projects = new Project[0];
    private ProjectsRootKeys rootKeys;
    private TestSupport.TestProject mainPrj1;
    private TestSupport.TestProject mainPrj2;
    private TestSupport.TestProject nestedPrj1;
    private TestSupport.TestProject nestedPrj2;
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
        FileObject nest2 = TestSupport.createTestProject(prj1, "nested2");
        mainPrj1 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj1);
        mainPrj2 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj2);
        nestedPrj1 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(nest1);
        nestedPrj2 = (TestSupport.TestProject) ProjectManager.getDefault().findProject(nest2);
        assertNotNull("Project found", mainPrj1);
        assertNotNull("Project found", mainPrj2);
        assertNotNull("Project found", nestedPrj1);
        assertNotNull("Project found", nestedPrj2);
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

    public void testProjectsAreCoLocatedAndSortedByName() throws Exception {
        this.projects = new Project[] { mainPrj1, mainPrj2, nestedPrj1, nestedPrj2 };
        mainPrj1.setLookup(Lookups.fixed(new MockInformation(mainPrj1, "Last parent")));
        mainPrj2.setLookup(Lookups.fixed(new MockInformation(mainPrj2, "First parent")));
        nestedPrj1.setLookup(Lookups.fixed(new MockInformation(nestedPrj1, "Last child")));
        nestedPrj2.setLookup(Lookups.fixed(new MockInformation(nestedPrj2, "First child")));

        var keys = this.rootKeys.getKeys();
        assertEquals("Four keys found: " + keys, 4, keys.size());

        var it = keys.iterator();
        var k1 = it.next();
        var k2 = it.next();
        var k3 = it.next();
        var k4 = it.next();
        assertFalse("Iterator is empty", it.hasNext());

        assertEquals("prj2 is alphabetically first parent", mainPrj2, k1.project());
        assertEquals("prj1 is alphabetically second parent", mainPrj1, k2.project());
        assertEquals("then prj2/nested2 is alphabetically first child", nestedPrj2, k3.project());
        assertEquals("then prj2/nested1 is alphabetically second child", nestedPrj1, k4.project());

        assertEquals("No depth for prj1", 0, k1.depth());
        assertEquals("No depth for prj2", 0, k2.depth());
        assertEquals("Depth one for nested prj2", 1, k3.depth());
        assertEquals("Depth one for nested prj1", 1, k4.depth());

        assertTrue("No depths were updated yet: " + depthUpdated, depthUpdated.isEmpty());

        //
        // now simulate closing of prj1
        //

        this.projects = new Project[] { mainPrj2, nestedPrj1, nestedPrj2 };

        it = this.rootKeys.getKeys().iterator();

        var n1 = it.next();
        var n2 = it.next();
        var n3 = it.next();

        assertFalse("Iterator is empty", it.hasNext());

        assertEquals("prj1/nested2 comes first (as First Child sorts first)", nestedPrj2, n1.project());
        assertEquals("prj2 comes second (as First Parent sorts second)", mainPrj2, n2.project());
        assertEquals("prj1/nested1 comes third (as Last Child sorts 3rd)", nestedPrj1, n3.project());

        assertEquals("No depth for prj2", 0, n1.depth());
        assertEquals("No depth for nested2", 0, n2.depth());
        assertEquals("No depth for nested1", 0, n3.depth());

        assertEquals("No depth in old prj1/nested1 either", 0, k2.depth());
        assertEquals("Two project depths updated", 2, depthUpdated.size());
        assertTrue("It is prj/nested2", depthUpdated.contains(k3.project()));
        assertTrue("It is prj/nested1", depthUpdated.contains(k4.project()));
        depthUpdated.clear();
    }

    public void testDuplicatedNames() throws Exception {
        this.projects = new Project[] { mainPrj1, mainPrj2, nestedPrj1, nestedPrj2 };
        mainPrj1.setLookup(Lookups.fixed(new MockInformation(mainPrj1, "Project")));
        mainPrj2.setLookup(Lookups.fixed(new MockInformation(mainPrj2, "Project")));
        nestedPrj1.setLookup(Lookups.fixed(new MockInformation(nestedPrj1, "Project")));
        nestedPrj2.setLookup(Lookups.fixed(new MockInformation(nestedPrj2, "Project")));

        var keys = this.rootKeys.getKeys();
        assertEquals("Four keys found: " + keys, 4, keys.size());

        var it = keys.iterator();
        var k1 = it.next();
        var k2 = it.next();
        var k3 = it.next();
        var k4 = it.next();
        assertFalse("Iterator is empty", it.hasNext());

        assertEquals("prj1 has the first path", mainPrj1, k1.project());
        assertEquals("then prj1/nested1 path", nestedPrj1, k2.project());
        assertEquals("then prj1/nested2 path", nestedPrj2, k3.project());
        assertEquals("prj2/ is lst", mainPrj2, k4.project());
        depthUpdated.clear();
    }

    private static final class MockInformation implements ProjectInformation {
        private final TestSupport.TestProject prj;
        private final String displayName;

        public MockInformation(TestSupport.TestProject prj, String displayName) {
            Objects.requireNonNull(prj);
            Objects.requireNonNull(displayName);
            this.prj = prj;
            this.displayName = displayName;
        }

        @Override
        public String getName() {
            return prj.getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public Project getProject() {
            return prj;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
